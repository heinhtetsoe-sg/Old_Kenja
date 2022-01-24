/*
 * $Id$
 *
 * 作成日: 2017/08/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD186U {

    private static final Log log = LogFactory.getLog(KNJD186U.class);

    private static final String TEST_HYOUTEI = "990009";
    private static final String TEST_HEIKIN = "990008";
    private static final String TEST_GOUKEI = "990008";

    private static final String CLASSCD_ALL = "99";
    private static final String CURRICULUM_ALL = "99";
    private static final String SUBCLASSCD_ALL = "999999";

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String frmName = "J".equals(_param._schoolKind) ? "KNJD186U_1.frm" : "KNJD186U_2.frm";
        log.info(" form = " + frmName);
        final List studentList = getStudentList(db2);
        for (int line = 0; line < studentList.size(); line++) {
            final Student student = (Student) studentList.get(line);
            svf.VrSetForm(frmName, 4);

            final String attendno = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);
            //保護者情報
            svf.VrsOut("ZIP_NO", student._hogosyaZip);
            svf.VrsOut("HR_NAME1", student._hrAbbv + " " + attendno + "番");
            final int addr1Len = KNJ_EditEdit.getMS932ByteLength(student._hogosyaAddr1);
            final int addr2Len = KNJ_EditEdit.getMS932ByteLength(student._hogosyaAddr2);
            final String addrField = addr1Len > 50 || addr2Len > 50 ? "_3" : addr1Len > 40 || addr2Len > 40 ? "_2" : "_1";
            svf.VrsOut("ADDRESS1" + addrField, student._hogosyaAddr1);
            svf.VrsOut("ADDRESS2" + addrField, student._hogosyaAddr2);
            if (student._hogosyaName != null) {
                final String sama = "　様";
                final int hogosyaNameLen = KNJ_EditEdit.getMS932ByteLength(student._hogosyaName) + KNJ_EditEdit.getMS932ByteLength(sama);
                final String guardNameField = hogosyaNameLen > 54 ? "4" : hogosyaNameLen > 44 ? "3" : hogosyaNameLen > 30 ? "2" : "1";
                svf.VrsOut("GUARD_NAME" + guardNameField, student._hogosyaName + sama);
            }

            //生徒情報
            svf.VrsOut("SCHREG_NO", student._schregno);
            svf.VrsOut("HR_NAME2", student._hrName + " " + attendno + "番");
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 40 ? "4" : KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            //タイトル
            svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度" + "　" + _param._semesterName + "　成績通知表");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);

            final String communication = (String) student._hreportremarkMap.get(_param._semester);
            final List tokenList = KNJ_EditKinsoku.getTokenList(communication, 44);
            for (int i = 0; i < tokenList.size(); i++) {
                svf.VrsOut("NOTE" + String.valueOf(i + 1), (String) tokenList.get(i));
            }

            //テスト名
            int testField = 1;
            for (Iterator itTestItem = _param._testItemMstList.iterator(); itTestItem.hasNext();) {
                final TestItemMst testItemMst = (TestItemMst) itTestItem.next();
                svf.VrsOut("TEST_NAME" + testField, testItemMst._testName);
                testField++;
            }
            if ("H".equals(_param._schoolKind)) {
                svf.VrsOut("CREDIT_NAME", "単位");
            }

            //科目欄下段のテスト毎の平均、順位欄
            printTestAllAvgRank(svf, student);

            //科目欄下段の平均の平均、順位欄
            printAvgAllAvgRank(svf, student);

            //出欠欄
            printAttendData(svf, student);

            //科目欄
            printSubclassRecord(svf, student);

            _hasData = true;
        }
    }

    //科目欄下段のテスト毎の平均、順位欄
    private void printTestAllAvgRank(final Vrw32alp svf, final Student student) {
        int scoreAllField = 1;
        for (Iterator itTestItem = _param._testItemMstList.iterator(); itTestItem.hasNext();) {
            final TestItemMst testItemMst = (TestItemMst) itTestItem.next();
            final ClassMst classMst = (ClassMst) student._classMstMap.get(CLASSCD_ALL);
            if (null != classMst) {
                final String subclassKey = CLASSCD_ALL + _param._schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL;
                final Subclass subclass = (Subclass) classMst._subclassMap.get(subclassKey);
                if (null == subclass) {
                    continue;
                }
                final Score score = (Score) subclass._scoreMap.get(testItemMst._semester + testItemMst._testCd);
                if (null != score) {
                    BigDecimal bdScore = new BigDecimal(score._avg);
                    svf.VrsOutn("AVE" + scoreAllField, 1, String.valueOf(bdScore.setScale(1, BigDecimal.ROUND_HALF_UP)));
                    svf.VrsOutn("AVE" + scoreAllField, 2, StringUtils.defaultString(score._classRank) + "/" + StringUtils.defaultString(score._hrCount));
                    svf.VrsOutn("AVE" + scoreAllField, 3, StringUtils.defaultString(score._courseRank) + "/" + StringUtils.defaultString(score._courseCount));
                }
            }
            scoreAllField++;
        }
    }

    //科目欄下段の平均の平均、順位欄
    private void printAvgAllAvgRank(final Vrw32alp svf, final Student student) {
        final List avgList = getSubclassAverageList(student);
        if (!avgList.isEmpty()) {
            BigDecimal avgAvg = average(avgList, 1);
            svf.VrsOutn("AVE_ALL", 1, avgAvg.toString());
        }
        if (_param._semester.equals(_param._lastSemester)) {
            final ClassMst classMst = (ClassMst) student._classMstMap.get(CLASSCD_ALL);
            final String subclassKey = CLASSCD_ALL + _param._schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL;
            if (null != classMst) {
                final Subclass subclassAll = (Subclass) classMst._subclassMap.get(subclassKey);
                if (null != subclassAll) {
                    final Score score = (Score) subclassAll._scoreMap.get("9" + TEST_HEIKIN);
                    if (null != score) {
//                      BigDecimal bdScore = new BigDecimal(score._avg);
//                      svf.VrsOutn("AVE_ALL", 1, String.valueOf(bdScore.setScale(1, BigDecimal.ROUND_HALF_UP)));
                        svf.VrsOutn("AVE_ALL", 2, StringUtils.defaultString(score._classRank) + "/" + StringUtils.defaultString(score._hrCount));
                        svf.VrsOutn("AVE_ALL", 3, StringUtils.defaultString(score._courseRank) + "/" + StringUtils.defaultString(score._courseCount));
                    }
                }
            }
        }
    }

    //出欠欄
    private void printAttendData(final Vrw32alp svf, final Student student) {
        int semesterCnt = 1;
        for (Iterator itSemesterMst = _param._semesterMstMap.keySet().iterator(); itSemesterMst.hasNext();) {
            final String semester = (String) itSemesterMst.next();
            final SemesterMst semesterMst = (SemesterMst) _param._semesterMstMap.get(semester);
            svf.VrsOut("SEMESTER" + semesterCnt, semesterMst._semesterName);
            semesterCnt++;
        }
        final String[] monthArray = {"04", "05", "06", "07", "09", "10", "11", "12", "01", "02", "03"};
        for (int monthCnt = 0; monthCnt < monthArray.length; monthCnt++) {
            final int monthField = monthCnt + 1;
            final Map attendMap = (Map) student._attendMap.get(monthArray[monthCnt]);
            svf.VrsOutn("MONTH1", monthField, String.valueOf(Integer.parseInt(monthArray[monthCnt])));
            if ("2".equals(_param._semester) && monthCnt > 7) {
                continue;
            }
            if (null != attendMap) {
                svf.VrsOutn("LESSON", monthField, (String) attendMap.get("JYUGYOU"));
                svf.VrsOutn("SUSPEND", monthField, (String) attendMap.get("SHUTTEI"));
                svf.VrsOutn("MUST", monthField, (String) attendMap.get("SHUSSEKISUBEKI"));
                svf.VrsOutn("NOTICE", monthField, (String) attendMap.get("KESSEKI"));
                svf.VrsOutn("ATTEND", monthField, (String) attendMap.get("SHUSSEKI"));
                svf.VrsOutn("BETWEEN", monthField, (String) attendMap.get("TOCHUKEKKA"));
                svf.VrsOutn("LATE", monthField, (String) attendMap.get("TIKOKU"));
                svf.VrsOutn("EARLY", monthField, (String) attendMap.get("SOUTAI"));
            }
        }
        final Map attendMap = (Map) student._attendMap.get("99");
        if (null != attendMap) {
            svf.VrsOutn("LESSON", 12, (String) attendMap.get("JYUGYOU"));
            svf.VrsOutn("SUSPEND", 12, (String) attendMap.get("SHUTTEI"));
            svf.VrsOutn("MUST", 12, (String) attendMap.get("SHUSSEKISUBEKI"));
            svf.VrsOutn("NOTICE", 12, (String) attendMap.get("KESSEKI"));
            svf.VrsOutn("ATTEND", 12, (String) attendMap.get("SHUSSEKI"));
            svf.VrsOutn("BETWEEN", 12, (String) attendMap.get("TOCHUKEKKA"));
            svf.VrsOutn("LATE", 12, (String) attendMap.get("TIKOKU"));
            svf.VrsOutn("EARLY", 12, (String) attendMap.get("SOUTAI"));
        }
    }

    private List getClassAbbvArray(final int subclassSize, final String s) {
        final List tokenList = new ArrayList();
        if (subclassSize == 1) {
            tokenList.add(s);
            return tokenList;
        }
        if (null != s) {
            final String[] arr;
            boolean reverse = false;
            if (subclassSize >= s.length()) {
                arr = KNJ_EditEdit.get_token(s, 2, 99);
            } else {
                arr = KNJ_EditEdit.get_token(new StringBuffer(s).reverse().toString(), 4, 99);
                reverse = true;
            }
            if (null != arr) {
                int last = arr.length - 1;
                for (int i = arr.length - 1; i >= 0; i--) {
                    if (!StringUtils.isBlank(arr[i])) {
                        break;
                    }
                    last = i - 1;
                }
                for (int i = 0; i <= last; i++) {
                    tokenList.add(arr[i]);
                }
            }
            if (reverse) {
                final List reversedTokenList = new ArrayList(tokenList);
                tokenList.clear();
                for (final ListIterator lit = reversedTokenList.listIterator(reversedTokenList.size()); lit.hasPrevious();) {
                    final String reversed = (String) lit.previous();
                    tokenList.add(new StringBuffer(StringUtils.defaultString(reversed)).reverse().toString());
                }
            }
        }
        return tokenList;
    }

    //科目欄の平均点のリストを取得
    private List getSubclassAverageList(final Student student) {
        final List averageList = new ArrayList();
        for (Iterator itClassMst = student._classCdOrder.iterator(); itClassMst.hasNext();) {
            final String classMstKey = (String) itClassMst.next();
            final ClassMst classMst = (ClassMst) student._classMstMap.get(classMstKey);

            for (Iterator itSubclass = classMst._subclassKeyOrder.iterator(); itSubclass.hasNext();) {
                final String subclassKey = (String) itSubclass.next();
                final Subclass subclass = (Subclass) classMst._subclassMap.get(subclassKey);

                if (null == subclass._subclassName) {
                    continue;
                }

                final List scoreList = new ArrayList();
                for (Iterator itTestItem = _param._testItemMstList.iterator(); itTestItem.hasNext();) {
                    final TestItemMst testItemMst = (TestItemMst) itTestItem.next();
                    final Score score = (Score) subclass._scoreMap.get(testItemMst._semester + testItemMst._testCd);
                    if (null != score) {
                        if (NumberUtils.isNumber(score._score)) {
                            scoreList.add(score._score);
                        }
                    }
                }
                if (!scoreList.isEmpty()) {
                    BigDecimal avg = average(scoreList, 0);
                    averageList.add(avg.toString());
                }
            }
        }
        return averageList;
    }

    //科目欄
    private void printSubclassRecord(final Vrw32alp svf, final Student student) {
        final int maxLine = 16;
        int line = 0;
        for (Iterator itClassMst = student._classCdOrder.iterator(); itClassMst.hasNext();) {
            final String classMstKey = (String) itClassMst.next();
            final ClassMst classMst = (ClassMst) student._classMstMap.get(classMstKey);

            int subClassCnt = 0;
            final List examSubclassList = new ArrayList();
            final List hyoteiSubclassList = new ArrayList();
            for (Iterator itSubclass = classMst._subclassKeyOrder.iterator(); itSubclass.hasNext();) {
                final String subclassKey = (String) itSubclass.next();
                final Subclass subclass = (Subclass) classMst._subclassMap.get(subclassKey);

                if (null == subclass._subclassName) {
                    continue;
                }
                if ("H".equals(_param._schoolKind)) {
                    if ("1".equals(subclass._gappeiSakiFlg)) {
                        if (_param._isOutputDebug) {
                            log.info("合併先科目は成績印字しない: " + subclass._subclassCd);
                        }
                    } else {
                        examSubclassList.add(subclass);
                    }
                } else {
                    examSubclassList.add(subclass);
                }
                if (!"1".equals(_param._gradingPrint)) {
                    if ("H".equals(_param._schoolKind)) {
                        if ("1".equals(subclass._gappeiMotoFlg)) {
                            if (_param._isOutputDebug) {
                                log.info("合併元科目は評定印字しない: " + subclass._subclassCd);
                            }
                        } else {
                            hyoteiSubclassList.add(subclass);
                        }
                    } else {
                        hyoteiSubclassList.add(subclass);
                    }
                }
            }

            final int maxSubclassLine = Math.max(examSubclassList.size(), hyoteiSubclassList.size());
            final List classAbbvArray = getClassAbbvArray(maxSubclassLine, classMst._classAbbv);
            for (int j = 0; j < maxSubclassLine; j++) {
                if (j < examSubclassList.size()) {
                    final Subclass subclass = (Subclass) examSubclassList.get(j);

                    //log.info(" subclass " + subclass._classCd + "-" + subclass._schoolKind + "-" + subclass._curriculumCd + "-" + subclass._subclassCd + " = " + subclass._subclassName);
                    int grpField = 1;
                    for (int i = 1; i < 12; i++) {
                        svf.VrsOut("GRP" + grpField, subclass._classCd);
                        svf.VrsOut("GRP" + grpField + "_2", subclass._classCd);
                        grpField++;
                    }
                    if (subClassCnt < classAbbvArray.size()) {
                        svf.VrsOut("CLASS_NAME", (String) classAbbvArray.get(subClassCnt));
                    }
                    svf.VrsOut("SUBCLASS_NAME1", subclass._subclassName);

                    int scoreField = 1;
                    final List scoreList = new ArrayList();
                    for (Iterator itTestItem = _param._testItemMstList.iterator(); itTestItem.hasNext();) {
                        final TestItemMst testItemMst = (TestItemMst) itTestItem.next();
                        final Score score = (Score) subclass._scoreMap.get(testItemMst._semester + testItemMst._testCd);
                        if (null != score) {
                            svf.VrsOut("SCORE" + scoreField, score._score);
                            if (NumberUtils.isNumber(score._score)) {
                                scoreList.add(score._score);
                            }
                        }
                        scoreField++;
                    }
                    if (!scoreList.isEmpty()) {
                        BigDecimal avg = average(scoreList, 0);
                        svf.VrsOut("SUBCLASS_AVE", avg.toString());
                    }
                }
                if (!"1".equals(_param._gradingPrint)) {
                    if (j < hyoteiSubclassList.size()) {
                        final Subclass subclass = (Subclass) hyoteiSubclassList.get(j);

                        svf.VrsOut("SUBCLASS_NAME2", subclass._subclassName);
                        final Score score09 = (Score) subclass._scoreMap.get("9" + TEST_HYOUTEI);
                        if (null != score09) {
                            svf.VrsOut("DIV", score09._score);
                        }
                        if ("H".equals(_param._schoolKind)) {
                            svf.VrsOut("CREDIT", subclass._credits);
                        }
                    }
                }
                subClassCnt++;
                svf.VrEndRecord();
            }
            line += subClassCnt;
        }
        for (int i = line; i < maxLine; i++) {
            svf.VrEndRecord();
        }
    }

    private BigDecimal average(final List scoreList, final int scale) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < scoreList.size(); i++) {
            sum = sum.add(new BigDecimal((String) scoreList.get(i)));
        }
        sum = sum.divide(new BigDecimal(scoreList.size()), scale, BigDecimal.ROUND_HALF_UP);
        return sum;
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String hogosyaName = rs.getString("GUARD_NAME");
                final String hogosyaZip = rs.getString("GUARD_ZIPCD");
                final String hogosyaAddr1 = rs.getString("GUARD_ADDR1");
                final String hogosyaAddr2 = rs.getString("GUARD_ADDR2");
                final Student student = new Student(schregno, grade, hrClass, attendno, coursecd, majorcd, coursecode, name, hrName, hrAbbv, hogosyaName, hogosyaZip, hogosyaAddr1, hogosyaAddr2);
                student.setAttendData(db2);
                student.setScoreData(db2);
                student.setHreportremark(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV, ");
        stb.append("     GUARDIAN.GUARD_NAME, ");
        stb.append("     GUARDIAN.GUARD_ZIPCD, ");
        stb.append("     GUARDIAN.GUARD_ADDR1, ");
        stb.append("     GUARDIAN.GUARD_ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ON REGD.SCHREGNO = GUARDIAN.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._sqlInstate + ") ");
        } else {
            stb.append("     AND REGD.SCHREGNO IN (" + _param._sqlInstate + ") ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _name;
        final String _hrName;
        final String _hrAbbv;
        final String _hogosyaName;
        final String _hogosyaZip;
        final String _hogosyaAddr1;
        final String _hogosyaAddr2;
        final List _classCdOrder = new ArrayList();
        Map _attendMap = new HashMap();
        Map _classMstMap = new TreeMap();
        Map _hreportremarkMap = new TreeMap();
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String name,
                final String hrName,
                final String hrAbbv,
                final String hogosyaName,
                final String hogosyaZip,
                final String hogosyaAddr1,
                final String hogosyaAddr2
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _name = name;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _hogosyaName = hogosyaName;
            _hogosyaZip = hogosyaZip;
            _hogosyaAddr1 = hogosyaAddr1;
            _hogosyaAddr2 = hogosyaAddr2;
        }

        public void setAttendData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAttendSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int totalJyugyou = 0;
                int totalShuttei = 0;
                int totalShussekiSubeki = 0;
                int totalKesseki = 0;
                int totalShusseki = 0;
                int totalTochuKekka = 0;
                int totalLate = 0;
                int totalEarly = 0;

                while (rs.next()) {
                    final String month = rs.getString("MONTH");
                    final int lesson = rs.getInt("LESSON");
                    final int offDays = rs.getInt("OFFDAYS");
                    final int suspend = rs.getInt("SUSPEND");
                    final int mourning = rs.getInt("MOURNING");
                    final int abroad = rs.getInt("ABROAD");
                    final int sick = rs.getInt("SICK");
                    final int notice = rs.getInt("NOTICE");
                    final int nonotice = rs.getInt("NONOTICE");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int tochuKekka = rs.getInt("TOCHU_KEKKA");

                    final Map setMap = new HashMap();
                    final int setJyugyou = "1".equals(_param._schoolMstSemOffDays) ? lesson - abroad : lesson - offDays - abroad;
                    setMap.put("JYUGYOU", String.valueOf(setJyugyou));
                    final int setShuttei = suspend + mourning;
                    setMap.put("SHUTTEI", String.valueOf(setShuttei));
                    final int setShussekiSubeki = setJyugyou - setShuttei;
                    setMap.put("SHUSSEKISUBEKI", String.valueOf(setShussekiSubeki));
                    final int setKesseki = "1".equals(_param._schoolMstSemOffDays) ? sick + notice + nonotice + offDays : sick + notice + nonotice;
                    setMap.put("KESSEKI", String.valueOf(setKesseki));
                    final int setShusseki = setJyugyou - setShuttei - setKesseki;
                    setMap.put("SHUSSEKI", String.valueOf(setShusseki));
                    setMap.put("TOCHUKEKKA", String.valueOf(tochuKekka));
                    setMap.put("TIKOKU", String.valueOf(late));
                    setMap.put("SOUTAI", String.valueOf(early));

                    totalJyugyou += setJyugyou;
                    totalShuttei += setShuttei;
                    totalShussekiSubeki += setShussekiSubeki;
                    totalKesseki += setKesseki;
                    totalShusseki += setShusseki;
                    totalTochuKekka += tochuKekka;
                    totalLate += late;
                    totalEarly += early;

                    _attendMap.put(month, setMap);
                }
                final Map setMap = new HashMap();
                setMap.put("JYUGYOU", String.valueOf(totalJyugyou));
                setMap.put("SHUTTEI", String.valueOf(totalShuttei));
                setMap.put("SHUSSEKISUBEKI", String.valueOf(totalShussekiSubeki));
                setMap.put("KESSEKI", String.valueOf(totalKesseki));
                setMap.put("SHUSSEKI", String.valueOf(totalShusseki));
                setMap.put("TOCHUKEKKA", String.valueOf(totalTochuKekka));
                setMap.put("TIKOKU", String.valueOf(totalLate));
                setMap.put("SOUTAI", String.valueOf(totalEarly));
                _attendMap.put("99", setMap);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getAttendSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MONTH, ");
            stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
            stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
            stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
            stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
            stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
            stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
            stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
            stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
            stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
            stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
            stb.append("     SUM(VALUE(TOCHU_KEKKA, 0)) AS TOCHU_KEKKA     ");
            stb.append(" FROM ");
            stb.append("     V_ATTEND_SEMES_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH ");

            return stb.toString();
        }

        public void setScoreData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getScoreSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String classAbbv = rs.getString("CLASSABBV");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String semester = rs.getString("SEMESTER");
                    final String testCd = rs.getString("TESTCD");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String courseRank = rs.getString("COURSE_AVG_RANK");
                    final String classRank = rs.getString("CLASS_AVG_RANK");
                    final String credits = rs.getString("CREDITS");
                    final String hrCount = rs.getString("HR_COUNT");
                    final String courseCount = rs.getString("COURSE_COUNT");
                    final String gappeiMotoFlg = rs.getString("GAPPEI_MOTO_FLG");
                    final String gappeiSakiFlg = rs.getString("GAPPEI_SAKI_FLG");

                    final String subclassKey = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_param._isNoPrintMoto && _param._replaceCombinedAttendSubclassList.contains(subclassKey)) {
                        continue;
                    }

                    ClassMst classMst = null;
                    if (_classMstMap.containsKey(classCd)) {
                        classMst = (ClassMst) _classMstMap.get(classCd);
                    } else {
                        classMst = new ClassMst(classCd, schoolKind, classAbbv);
                        _classMstMap.put(classCd, classMst);
                        _classCdOrder.add(classCd);
                    }

                    Subclass subclass = null;
                    if (classMst._subclassMap.containsKey(subclassKey)) {
                        subclass = (Subclass) classMst._subclassMap.get(subclassKey);
                    } else {
                        subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, credits, gappeiMotoFlg, gappeiSakiFlg);
                        classMst._subclassMap.put(subclassKey, subclass);
                        classMst._subclassKeyOrder.add(subclassKey);
                    }
                    subclass.setScoreMap(semester, testCd, score, avg, courseRank, classRank, hrCount, courseCount);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getScoreSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SUBCLASS AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            if (!("H".equals(_param._schoolKind) && "03".equals(_param._gradeCd))) {
                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.PROV_FLG = '1' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + _param._semester + "' ");
            if (!"1".equals(_param._gradingPrint)) {
                stb.append("     OR T1.SEMESTER = '9' ");
            }
            stb.append("     ) ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND T1.VALUE_DI IS NOT NULL ");
            if (!("H".equals(_param._schoolKind) && "03".equals(_param._gradeCd))) {
                stb.append("     AND NOT (VALUE(T2.PROV_FLG, '0') = '1' AND T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '09') ");
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            if (!("H".equals(_param._schoolKind) && "03".equals(_param._gradeCd))) {
                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.PROV_FLG = '1' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + _param._semester + "' ");
            if (!"1".equals(_param._gradingPrint)) {
                stb.append("     OR T1.SEMESTER = '9' ");
            }
            stb.append("     ) ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            if (!("H".equals(_param._schoolKind) && "03".equals(_param._gradeCd))) {
                stb.append("     AND NOT (VALUE(T2.PROV_FLG, '0') = '1' AND T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '09') ");
            }
            stb.append(" ) ");

            stb.append(" , ATTEND_COMBINED AS ( ");
            stb.append("     SELECT 'ATTEND' AS DIV, ATTEND_CLASSCD AS CLASSCD, ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT 'COMBINED' AS DIV, COMBINED_CLASSCD AS CLASSCD, COMBINED_SCHOOL_KIND AS SCHOOL_KIND, COMBINED_CURRICULUM_CD AS CURRICULUM_CD, COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _param._ctrlYear + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CLASS_M.CLASSABBV, ");
            stb.append("     VALUE(SUBCLASS.SUBCLASSORDERNAME2, SUBCLASS.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN ATTEND.DIV IS NOT NULL THEN '1' END AS GAPPEI_MOTO_FLG, ");
            stb.append("     CASE WHEN COMBINED.DIV IS NOT NULL THEN '1' END AS GAPPEI_SAKI_FLG, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("     RANK_SDIV.SCORE, ");
            stb.append("     RANK_SDIV.AVG, ");
            stb.append("     RANK_SDIV.COURSE_AVG_RANK, ");
            stb.append("     RANK_SDIV.CLASS_AVG_RANK, ");
            stb.append("     CREDIT.CREDITS, ");
            stb.append("     TAVG_HR.COUNT AS HR_COUNT, ");
            stb.append("     TAVG_COURSE.COUNT AS COURSE_COUNT, ");
            stb.append("     VALUE(CLASS_M.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ");
            stb.append("     VALUE(SUBCLASS.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3 ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS T1 ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE ON SCORE.YEAR = T1.YEAR ");
            stb.append("          AND SCORE.SEMESTER = T1.SEMESTER ");
            stb.append("          AND SCORE.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND SCORE.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND SCORE.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("          AND SCORE.CLASSCD = T1.CLASSCD ");
            stb.append("          AND SCORE.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND SCORE.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND SCORE.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND SCORE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ON RANK_SDIV.YEAR = T1.YEAR ");
            stb.append("          AND RANK_SDIV.SEMESTER = T1.SEMESTER ");
            stb.append("          AND RANK_SDIV.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND RANK_SDIV.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND RANK_SDIV.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("          AND RANK_SDIV.CLASSCD = T1.CLASSCD ");
            stb.append("          AND RANK_SDIV.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND RANK_SDIV.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND RANK_SDIV.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND RANK_SDIV.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST CREDIT ON T1.YEAR = CREDIT.YEAR ");
            stb.append("          AND CREDIT.COURSECD = '" + _coursecd + "' ");
            stb.append("          AND CREDIT.MAJORCD = '" + _majorcd + "' ");
            stb.append("          AND CREDIT.GRADE = '" + _grade + "' ");
            stb.append("          AND CREDIT.COURSECODE = '" + _coursecode + "' ");
            stb.append("          AND T1.CLASSCD = CREDIT.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = CREDIT.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = CREDIT.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = CREDIT.SUBCLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBCLASS ON T1.CLASSCD = SUBCLASS.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
            stb.append("     LEFT JOIN CLASS_MST CLASS_M ON T1.CLASSCD = CLASS_M.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = CLASS_M.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND REGD.YEAR = T1.YEAR ");
            stb.append("          AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT TAVG_HR ON TAVG_HR.YEAR = T1.YEAR ");
            stb.append("          AND TAVG_HR.SEMESTER = T1.SEMESTER ");
            stb.append("          AND TAVG_HR.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND TAVG_HR.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND TAVG_HR.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("          AND TAVG_HR.CLASSCD = T1.CLASSCD ");
            stb.append("          AND TAVG_HR.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND TAVG_HR.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND TAVG_HR.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND TAVG_HR.AVG_DIV = '2' ");
            stb.append("          AND TAVG_HR.GRADE = REGD.GRADE ");
            stb.append("          AND TAVG_HR.HR_CLASS = REGD.HR_CLASS ");
            stb.append("          AND TAVG_HR.COURSECD = '0' AND TAVG_HR.MAJORCD = '000' AND TAVG_HR.COURSECODE = '0000' ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT TAVG_COURSE ON TAVG_COURSE.YEAR = T1.YEAR ");
            stb.append("          AND TAVG_COURSE.SEMESTER       = T1.SEMESTER ");
            stb.append("          AND TAVG_COURSE.TESTKINDCD     = T1.TESTKINDCD ");
            stb.append("          AND TAVG_COURSE.TESTITEMCD     = T1.TESTITEMCD ");
            stb.append("          AND TAVG_COURSE.SCORE_DIV      = T1.SCORE_DIV ");
            stb.append("          AND TAVG_COURSE.CLASSCD        = T1.CLASSCD ");
            stb.append("          AND TAVG_COURSE.SCHOOL_KIND    = T1.SCHOOL_KIND ");
            stb.append("          AND TAVG_COURSE.CURRICULUM_CD  = T1.CURRICULUM_CD ");
            stb.append("          AND TAVG_COURSE.SUBCLASSCD     = T1.SUBCLASSCD ");
            stb.append("          AND TAVG_COURSE.AVG_DIV        = '3' ");
            stb.append("          AND TAVG_COURSE.GRADE          = REGD.GRADE ");
            stb.append("          AND TAVG_COURSE.HR_CLASS       = '000' ");
            stb.append("          AND TAVG_COURSE.COURSECD       = REGD.COURSECD ");
            stb.append("          AND TAVG_COURSE.MAJORCD        = REGD.MAJORCD ");
            stb.append("          AND TAVG_COURSE.COURSECODE     = REGD.COURSECODE ");
            stb.append("     LEFT JOIN ATTEND_COMBINED ATTEND ON ATTEND.DIV = 'ATTEND' ");
            stb.append("          AND ATTEND.CLASSCD             = T1.CLASSCD ");
            stb.append("          AND ATTEND.SCHOOL_KIND         = T1.SCHOOL_KIND ");
            stb.append("          AND ATTEND.CURRICULUM_CD       = T1.CURRICULUM_CD ");
            stb.append("          AND ATTEND.SUBCLASSCD          = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN ATTEND_COMBINED COMBINED ON COMBINED.DIV = 'COMBINED' ");
            stb.append("          AND COMBINED.CLASSCD           = T1.CLASSCD ");
            stb.append("          AND COMBINED.SCHOOL_KIND       = T1.SCHOOL_KIND ");
            stb.append("          AND COMBINED.CURRICULUM_CD     = T1.CURRICULUM_CD ");
            stb.append("          AND COMBINED.SUBCLASSCD        = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(CLASS_M.SHOWORDER3, 999999), ");
            stb.append("     RANK_SDIV.CLASSCD, ");
            stb.append("     RANK_SDIV.SCHOOL_KIND, ");
            stb.append("     VALUE(SUBCLASS.SHOWORDER3, 999999), ");
            stb.append("     RANK_SDIV.CURRICULUM_CD, ");
            stb.append("     RANK_SDIV.SUBCLASSCD, ");
            stb.append("     RANK_SDIV.SEMESTER, ");
            stb.append("     TESTCD ");

            return stb.toString();
        }

        public void setHreportremark(DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = " SELECT SEMESTER, COMMUNICATION FROM HREPORTREMARK_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND SCHREGNO = '" + _schregno + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _hreportremarkMap.put(rs.getString("SEMESTER"), rs.getString("COMMUNICATION"));
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }

    private class ClassMst {
        final String _classCd;
        final String _schoolKind;
        final String _classAbbv;
        final List _subclassKeyOrder = new ArrayList();
        Map _subclassMap = new HashMap();
        public ClassMst(
                final String classCd,
                final String schoolKind,
                final String classAbbv
                ) throws SQLException {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _classAbbv = classAbbv;
        }

    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _credits;
        final String _gappeiMotoFlg;
        final String _gappeiSakiFlg;
        Map _scoreMap = new HashMap();
        String _avgSansyutu;
        public Subclass(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName,
                final String credits,
                final String gappeiMotoFlg,
                final String gappeiSakiFlg
                ) throws SQLException {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits = credits;
            _gappeiMotoFlg = gappeiMotoFlg;
            _gappeiSakiFlg = gappeiSakiFlg;
        }

        public void setScoreMap(
                final String semester,
                final String testCd,
                final String score,
                final String avg,
                final String courseRank,
                final String classRank,
                final String hrCount,
                final String courseCount
        ) {
            final Score setScore = new Score(semester, testCd, score, avg, classRank, courseRank, hrCount, courseCount);
            _scoreMap.put(semester + testCd, setScore);
        }

    }

    private class Score {
        final String _semester;
        final String _testCd;
        final String _score;
        final String _avg;
        final String _classRank;
        final String _courseRank;
        final String _hrCount;
        final String _courseCount;
        public Score(
                final String semester,
                final String testCd,
                final String score,
                final String avg,
                final String classRank,
                final String courseRank,
                final String hrCount,
                final String courseCount
        ) {
            _semester = semester;
            _testCd = testCd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _courseRank = courseRank;
            _hrCount = hrCount;
            _courseCount = courseCount;
        }
    }

    private static class TestItemMst {
        final String _semester;
        final String _testCd;
        final String _testName;
        int _cnt = 0;
        int _totalScore = 0;
        String _assessLow = "";
        String _assessHigh = "";
        public TestItemMst(
                final String semester,
                final String testCd,
                final String testName
        ) throws SQLException {
            _semester = semester;
            _testCd = testCd;
            _testName = testName;
        }

    }

    private static class SemesterMst {
        final String _semester;
        final String _semesterName;
        public SemesterMst(
                final String semester,
                final String semesterName
        ) throws SQLException {
            _semester = semester;
            _semesterName = semesterName;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Id$");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _semester;
        final String _disp;
        final String _grade;
        final String _gradeHrClass;
        final String[] _categorySelected;
        final String _sqlInstate;
        final String _printDate;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _useschoolKindfield;
        final String _paraSchoolkind;
        final String _schoolcd;
        final String _printLogStaffcd;
        final String _gradingPrint;
        final String _semesterName;
        final TreeMap _semesterMstMap;
        final List _testItemMstList;
        final String _schoolMstSemOffDays;
        String _schoolKind;
        String _gradeCd;
        final String _schoolName;
        final String _semesterdiv;
        final boolean _hasSchoolMstSchoolKind;
        boolean _isNoPrintMoto;
        final List _replaceCombinedAttendSubclassList = new ArrayList();
        final boolean _isOutputDebug;
        private String _lastSemester;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _printDate = request.getParameter("PRINT_DATE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _paraSchoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _gradingPrint = request.getParameter("GRADING_PRINT");
            _semesterMstMap = getSemesterMstMap(db2);
            if (!_semesterMstMap.isEmpty()) {
                _lastSemester = (String) _semesterMstMap.lastKey();
            }
            _semesterName = ((SemesterMst) _semesterMstMap.get(_semester))._semesterName;
            _testItemMstList = getTestItemMstList(db2);
            String setInstate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                final String selectVal = _categorySelected[i];
                final String[] setVal = StringUtils.split(selectVal, "-");
                setInstate += sep + "'" + setVal[0] + "'";
                sep = ",";
            }
            _sqlInstate = setInstate;
            String grade = _grade;
            if ("2".equals(_disp)) {
                grade = _gradeHrClass.substring(0, 2);
            }
            setSchoolKind(db2, grade);
            _schoolMstSemOffDays = getSemOffDays(db2);
            _semesterdiv = KnjDbUtils.getOne(KnjDbUtils.query(db2, getSemesterdiv()));
            _schoolName = getSchoolName(db2);
            loadNameMstD016(db2);
            setReplaceCombined(db2);
            _hasSchoolMstSchoolKind = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186U' AND NAME = '" + propName + "' "));
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemester();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemester() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            return stb.toString();
        }

        private TreeMap getSemesterMstMap(final DB2UDB db2) throws SQLException {
            final TreeMap retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemesterMstSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semesterName = rs.getString("SEMESTERNAME");
                    final SemesterMst semesterMst = new SemesterMst(semester, semesterName);
                    retMap.put(semester, semesterMst);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getSemesterMstSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER < '9' ");
            return stb.toString();
        }

        private String getSemOffDays(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemOffDays();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEM_OFFDAYS");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemOffDays() {
            return getSchoolMst("VALUE(SEM_OFFDAYS, '0') AS SEM_OFFDAYS");
        }

        private String getSemesterdiv() {
            return getSchoolMst("VALUE(SEMESTERDIV, '0') AS SEMESTERDIV");
        }

        private String getSchoolMst(final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + field + " ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            if (_hasSchoolMstSchoolKind) {
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            }
            return stb.toString();
        }

        private List getTestItemMstList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getTestItemMst();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testCd = rs.getString("TESTCD");
                    final String testName = rs.getString("TESTITEMNAME");

                    final TestItemMst testItemMst = new TestItemMst(semester, testCd, testName);
                    retList.add(testItemMst);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getTestItemMst() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEM.SEMESTER, ");
            stb.append("     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV AS TESTCD, ");
            stb.append("     TESTITEM.TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ");
            stb.append(" WHERE ");
            stb.append("     TESTITEM.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND TESTITEM.SCORE_DIV = '01' ");
            stb.append(" ORDER BY ");
            stb.append("     TESTITEM.SEMESTER, ");
            stb.append("     TESTCD ");
            return stb.toString();
        }

        private void setSchoolKind(final DB2UDB db2, final String grade) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolKind(grade);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _schoolKind = rs.getString("SCHOOL_KIND");
                    _gradeCd = rs.getString("GRADE_CD");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getSchoolKind(final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     GDAT.GRADE_CD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GDAT.GRADE = '" + grade + "' ");
            return stb.toString();
        }

        private String getSchoolName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolNameSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SCHOOL_NAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolNameSql() {
            final StringBuffer stb = new StringBuffer();
            final String certifKind = "J".equals(_schoolKind) ? "103" : "104";
            stb.append(" SELECT ");
            stb.append("     SCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD = '" + certifKind + "' ");

            return stb.toString();
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D" + _schoolKind + "16' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void setReplaceCombined(final DB2UDB db2) {
            final String sql = "SELECT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
            final List rowList = KnjDbUtils.query(db2, sql);
            _replaceCombinedAttendSubclassList.addAll(KnjDbUtils.getColumnDataList(rowList, "ATTEND_SUBCLASSCD"));
        }

    }
}

// eof

