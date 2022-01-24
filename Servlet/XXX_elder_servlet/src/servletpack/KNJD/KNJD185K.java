/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 76b2bb407177e6d69b9dc326812aa320dfb30803 $
 *
 * 作成日: 2019/05/13
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD185K {

    private static final Log log = LogFactory.getLog(KNJD185K.class);

    private static final String SEMEALL = "9";
    private static final String SEME3 = "3";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SCORE990008 = "99-00-08";
    private static final String SCORE990009 = "99-00-09";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

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
        final List studentList = getList(db2);

        printOut(db2, svf, studentList);
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            svf.VrSetForm("KNJD185K.frm", 1);
            final Student student = (Student) iterator.next();

            final TestItemMstSdiv titleItemMstSdiv = (TestItemMstSdiv) _param._testItemMstSdivMap.get(_param._semester + "-" + _param._testcd);
            final Semester semeMst = (Semester) _param._semesterMap.get(_param._semester);
            svf.VrsOut("TITLE", _param._ctrlYear + "年度" + "　　　　　" + semeMst._semestername + titleItemMstSdiv._testitemName + "　　　　　成績票");

            svf.VrsOut("HR_NAME", student._hrname + "　" + student._attendno + "番");
            final int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String nameField = nameLen > 30 ? "3" : nameLen > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            final int priNameLen = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName);
            final String priNameField = priNameLen > 30 ? "3" : priNameLen > 20 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + priNameField, _param._certifSchoolPrincipalName);

            final int trNameLen = KNJ_EditEdit.getMS932ByteLength(student._staffname);
            final String trNameField = trNameLen > 20 ? "3" : trNameLen > 14 ? "2" : "1";
            svf.VrsOut("TR_NAME" + trNameField, student._staffname);

            //D026を抜く
            final List subclassList = subclassListRemoveD026();
            Collections.sort(subclassList);

            //学期名
            for (Iterator itSeme = _param._semesterMap.keySet().iterator(); itSeme.hasNext();) {
                final String semester = (String) itSeme.next();
                final Semester titleSemeMst = (Semester) _param._semesterMap.get(semester);
                svf.VrsOut("SEMESTER" + semester, titleSemeMst._semestername);
            }

            //テスト名,分布
            int testCnt = 1;
            int bunpuItemCnt = 1;
            String befSeme = "1";
            for (Iterator itTestItem = _param._testItemMstSdivMap.keySet().iterator(); itTestItem.hasNext();) {
                final String testCd = (String) itTestItem.next();
                final String[] testCdArray = StringUtils.split(testCd, "-");
                if (!befSeme.equals(testCdArray[0])) {
                    testCnt = 1;
                    if ("3".equals(testCdArray[0])) {
                        bunpuItemCnt = 5;
                    } else {
                        bunpuItemCnt = Integer.parseInt(testCdArray[0]) + 1;
                    }
                }
                final TestItemMstSdiv itemMstSdiv = (TestItemMstSdiv) _param._testItemMstSdivMap.get(testCd);
                if ("3".equals(testCdArray[0])) {
                    svf.VrsOut("TEST_NAME" + testCdArray[0], itemMstSdiv._testitemName);
                } else {
                    svf.VrsOut("TEST_NAME" + testCdArray[0] + "_" + testCnt, itemMstSdiv._testitemName);
                }
                if (bunpuItemCnt <= Integer.parseInt(testCdArray[0]) * 2) {
                    //指定学期外は出力しない
                    if (Integer.parseInt(testCdArray[0]) > Integer.parseInt(_param._semester)) {
                        continue;
                    }
                    //指定テストまで
                    if (!_param._testItemMstSdivMap.containsKey(testCd)) {
                        continue;
                    }

                    //分布
                    if (_param._scoreBunpuMap.containsKey(testCd)) {
                        final ScoreBunpu bunpu = (ScoreBunpu) _param._scoreBunpuMap.get(testCd);
                        svf.VrsOutn("DISTRI1", bunpuItemCnt, bunpu._score500);
                        svf.VrsOutn("DISTRI2", bunpuItemCnt, bunpu._score450);
                        svf.VrsOutn("DISTRI3", bunpuItemCnt, bunpu._score400);
                        svf.VrsOutn("DISTRI4", bunpuItemCnt, bunpu._score350);
                    }
                }
                testCnt++;
                befSeme = testCdArray[0];
                bunpuItemCnt++;
            }

            //学習の記録
            //科目マスタでループ
            int classCnt = 1;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                //生徒が受けていない科目は読み飛ばす
                if (!student._printSubclassMap.containsKey(subclassCd)) {
                    continue;
                }
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get(subclassCd);

                svf.VrsOut("CLASS_NAME" + classCnt, subclassMst._classname);

                //素点,平均
                int itemCnt = 1;
                befSeme = "1";
                for (Iterator itTestItem = _param._testItemMstSdivMap.keySet().iterator(); itTestItem.hasNext();) {
                    final String testCd = (String) itTestItem.next();
                    final String[] testCdArray = StringUtils.split(testCd, "-");
                    if (!befSeme.equals(testCdArray[0])) {
                        if ("3".equals(testCdArray[0])) {
                        	itemCnt = 5;
                        } else {
                        	itemCnt = Integer.parseInt(testCdArray[0]) + 1;
                        }
                    }
                    if (itemCnt <= Integer.parseInt(testCdArray[0]) * 2) {
                        //指定学期外は出力しない
                        if (Integer.parseInt(testCdArray[0]) > Integer.parseInt(_param._semester)) {
                            continue;
                        }
                        //指定テストまで
                        if (!_param._testItemMstSdivMap.containsKey(testCd)) {
                            continue;
                        }

                        //素点、平均
                        if (printSubclass._testScoreMap.containsKey(testCd)) {
                            final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get(testCd);
                            svf.VrsOutn("SCORE" + classCnt, itemCnt, scoreData._score);
                            svf.VrsOutn("AVE" + classCnt, itemCnt, scoreData._avg);
                        }
                        //合計
                        final PrintSubclass printSubclassTotal =  (PrintSubclass) student._printSubclassMap.get("99-" + _param._schoolKind + "-99-" + ALL9);
                        if (null != printSubclassTotal && printSubclassTotal._testScoreMap.containsKey(testCd)) {
                            final ScoreData scoreData = (ScoreData) printSubclassTotal._testScoreMap.get(testCd);
                            svf.VrsOutn("TOTAL", itemCnt, scoreData._score);
                        }
                    }
                    befSeme = testCdArray[0];
                    itemCnt++;
                }
                classCnt++;
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
        }
        return retList;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffCd = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._attendno = String.valueOf(Integer.parseInt(rs.getString("ATTENDNO")));
                student._gName = rs.getString("GUARD_NAME");
                student._gZip = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._gAddr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._gAddr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._courseCode = rs.getString("COURSECODE");
                student._course = rs.getString("COURSE");
                student._courseCodeName = rs.getString("COURSECODENAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student.setRankSdiv(db2);
                student.setHreport(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,GUARD.GUARD_NAME ");
        stb.append("            ,GUARD.GUARD_ZIPCD ");
        stb.append("            ,GUARD.GUARD_ADDR1 ");
        stb.append("            ,GUARD.GUARD_ADDR2 ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECODE ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,COURSECODE_M.COURSECODENAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("     FROM    SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARD ON BASE.SCHREGNO = GUARD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE_M ON COURSECODE_M.COURSECODE = REGD.COURSECODE ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffCd;
        String _attendno;
        String _gName;
        String _gZip;
        String _gAddr1;
        String _gAddr2;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _courseCode;
        String _course;
        String _courseCodeName;
        String _hrClassName1;
        String _entyear;
        String _communication;
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();
        Map _attendSemesMap = new TreeMap(); // 出欠の記録

        public Student() {
        }

        private void setRankSdiv(final DB2UDB db2) {
            final String scoreSql = getRankSdivSql();
log.debug(scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String semester = rs.getString("SEMESTER");
                    final String testkindCd = rs.getString("TESTKINDCD");
                    final String testitemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String credit = rs.getString("CREDITS");
                    final String score = rs.getString("SCORE");
                    final String avg = null == rs.getString("AVG") ? null : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String gradeRank = rs.getString("GRADE_RANK");
                    final String courseRank = rs.getString("COURSE_RANK");
                    final String count = rs.getString("COUNT");
                    final String valueDi = rs.getString("VALUE_DI");
                    PrintSubclass printSubclass = null;
                    if (!_printSubclassMap.containsKey(subclassCd)) {
                        printSubclass = new PrintSubclass(subclassCd, credit);
                        _printSubclassMap.put(subclassCd, printSubclass);
                    } else {
                        printSubclass = (PrintSubclass) _printSubclassMap.get(subclassCd);
                    }

                    final String key = semester + "-" + testkindCd + "-" + testitemCd + "-" + scoreDiv;
                    ScoreData scoreData = new ScoreData(score, avg, gradeRank, courseRank, count, valueDi);
                    if (printSubclass._testScoreMap.containsKey(key)) {
                        scoreData = (ScoreData) printSubclass._semesJviewMap.get(key);
                    }
                    printSubclass._testScoreMap.put(key, scoreData);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String getRankSdivSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     RANK_SDIV.CLASSCD || '-' || RANK_SDIV.SCHOOL_KIND || '-' || RANK_SDIV.CURRICULUM_CD || '-' || RANK_SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     RANK_SDIV.SEMESTER, ");
            stb.append("     RANK_SDIV.TESTKINDCD, ");
            stb.append("     RANK_SDIV.TESTITEMCD, ");
            stb.append("     RANK_SDIV.SCORE_DIV, ");
            stb.append("     CRE.CREDITS, ");
            stb.append("     RANK_SDIV.SCORE, ");
            stb.append("     AVG.AVG, ");
            stb.append("     RANK_SDIV.GRADE_RANK, ");
            stb.append("     RANK_SDIV.COURSE_RANK, ");
            stb.append("     AVG.COUNT, ");
            stb.append("     SCORE.VALUE_DI ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
            stb.append("     LEFT JOIN CREDIT_MST CRE ON RANK_SDIV.YEAR = CRE.YEAR ");
            stb.append("          AND CRE.COURSECD || CRE.MAJORCD || CRE.COURSECODE = '" + _course + "' ");
            stb.append("          AND CRE.GRADE = '" + _grade + "' ");
            stb.append("          AND RANK_SDIV.CLASSCD || RANK_SDIV.SCHOOL_KIND || RANK_SDIV.CURRICULUM_CD || RANK_SDIV.SUBCLASSCD  = CRE.CLASSCD || CRE.SCHOOL_KIND || CRE.CURRICULUM_CD || CRE.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE ON RANK_SDIV.YEAR = SCORE.YEAR ");
            stb.append("          AND RANK_SDIV.SEMESTER = SCORE.SEMESTER ");
            stb.append("          AND RANK_SDIV.TESTKINDCD = SCORE.TESTKINDCD ");
            stb.append("          AND RANK_SDIV.TESTITEMCD = SCORE.TESTITEMCD ");
            stb.append("          AND RANK_SDIV.SCORE_DIV = SCORE.SCORE_DIV ");
            stb.append("          AND RANK_SDIV.CLASSCD = SCORE.CLASSCD ");
            stb.append("          AND RANK_SDIV.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("          AND RANK_SDIV.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("          AND RANK_SDIV.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append("          AND RANK_SDIV.SCHREGNO = SCORE.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVG ON RANK_SDIV.YEAR = AVG.YEAR ");
            stb.append("          AND RANK_SDIV.SEMESTER = AVG.SEMESTER ");
            stb.append("          AND RANK_SDIV.TESTKINDCD = AVG.TESTKINDCD ");
            stb.append("          AND RANK_SDIV.TESTITEMCD = AVG.TESTITEMCD ");
            stb.append("          AND RANK_SDIV.SCORE_DIV = AVG.SCORE_DIV ");
            stb.append("          AND RANK_SDIV.CLASSCD = AVG.CLASSCD ");
            stb.append("          AND RANK_SDIV.SCHOOL_KIND = AVG.SCHOOL_KIND ");
            stb.append("          AND RANK_SDIV.CURRICULUM_CD = AVG.CURRICULUM_CD ");
            stb.append("          AND RANK_SDIV.SUBCLASSCD = AVG.SUBCLASSCD ");
            stb.append("          AND AVG.AVG_DIV = '1' ");
            stb.append("          AND AVG.GRADE = '" + _grade + "' ");
            stb.append(" WHERE ");
            stb.append("     RANK_SDIV.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND (RANK_SDIV.SEMESTER || '-' || RANK_SDIV.TESTKINDCD || '-' || RANK_SDIV.TESTITEMCD || '-' || RANK_SDIV.SCORE_DIV <= '" + _param._semester + "-" +  _param._testcd + "' ");
            stb.append("          OR ");
            stb.append("          RANK_SDIV.TESTKINDCD || '-' || RANK_SDIV.TESTITEMCD || '-' || RANK_SDIV.SCORE_DIV = '99-00-09' ");
            stb.append("         ) ");
            stb.append("     AND RANK_SDIV.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND (RANK_SDIV.CLASSCD IN ('11', '12', '13', '14', '20') OR RANK_SDIV.SUBCLASSCD = '" + ALL9 + "') ");
            stb.append("     AND RANK_SDIV.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     RANK_SDIV.SEMESTER, ");
            stb.append("     RANK_SDIV.TESTKINDCD, ");
            stb.append("     RANK_SDIV.TESTITEMCD, ");
            stb.append("     RANK_SDIV.SCORE_DIV ");

            return stb.toString();
        }

        private void setHreport(final DB2UDB db2) {
            _communication = "";
            final String hreportSemeSql = getHreportSemeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hreportSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _communication = rs.getString("COMMUNICATION");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getHreportSemeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COMMUNICATION ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

    }

    private class PrintSubclass {
        final String _subclassCd;
        final String _credit;
        final Map _semesJviewMap;
        final Map _semesHyoukaMap;
        /**
         * _semesScoreMap[学期]scoreMap[テストコード]ScoreData
         */
        final Map _testScoreMap;
        private PrintSubclass(
                final String subclassCd,
                final String credit
        ) {
            _subclassCd = subclassCd;
            _credit = credit;
            _semesJviewMap = new TreeMap();
            _semesHyoukaMap = new TreeMap();
            _testScoreMap = new TreeMap();
        }
    }

    private class ScoreData {
        final String _score;
        final String _avg;
        final String _gradeRank;
        final String _courseRank;
        final String _count;
        final boolean _isKesseki;
        private ScoreData(
                final String score,
                final String avg,
                final String gradeRank,
                final String courseRank,
                final String count,
                final String valueDi
        ) {
            _score = StringUtils.defaultString(score);
            _avg = avg;
            _gradeRank = gradeRank;
            _courseRank = courseRank;
            _count = count;
            _isKesseki = "*".equals(valueDi);
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst os = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(os._classShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classcd.compareTo(os._classcd);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassShoworder3.compareTo(os._subclassShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclasscd.compareTo(os._subclasscd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private class TestItemMstSdiv {
        final String _semester;
        final String _testKindCd;
        final String _testItemCd;
        final String _scoreDiv;
        final String _testitemName;
        final String _sDate;
        final String _eDate;
        public TestItemMstSdiv(
                final String semester,
                final String testKindCd,
                final String testItemCd,
                final String scoreDiv,
                final String testitemName,
                final String sDate,
                final String eDate
        ) {
            _semester = semester;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _scoreDiv = scoreDiv;
            _testitemName = testitemName;
            _sDate = sDate;
            _eDate = eDate;
        }
    }

    private class ScoreBunpu {
        final String _semester;
        final String _testKindCd;
        final String _testItemCd;
        final String _scoreDiv;
        final String _score350;
        final String _score400;
        final String _score450;
        final String _score500;
        public ScoreBunpu(
                final String semester,
                final String testKindCd,
                final String testItemCd,
                final String scoreDiv,
                final String score350,
                final String score400,
                final String score450,
                final String score500
        ) {
            _semester = semester;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _scoreDiv = scoreDiv;
            _score350 = score350;
            _score400 = score400;
            _score450 = score450;
            _score500 = score500;
        }
    }

    private class PerfectMst {
        final String _perfect;
        final String _passScore;
        public PerfectMst(
                final String perfect,
                final String passScore
        ) {
            _perfect = perfect;
            _passScore = passScore;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72664 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _disp;
        final String _documentroot;
        final String _grade;
        final String _gradeHr;
        final String _schoolKind;
        final String _prgid;
        final String _printDate;
        final String _printLogRemoteAddr;
        final String _printLogRemoteIdent;
        final String _printLogStaffcd;
        final String _schoolcd;
        final String _loginSchoolKind;
        final String _semester;
        final String _summaryDate;
        final String _testcd;
        final String _selectSchoolKind;
        final String _useSchool_KindField;
        final String _use_prg_schoolkind;
        final Map _testItemMstSdivMap;
        final Map _scoreBunpuMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        private Map _semesterMap;
        private Map _subclassMstMap;
        private List _d026List = Collections.EMPTY_LIST;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            if ("1".equals(_disp)) {
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                final String[] schregs = request.getParameterValues("CATEGORY_SELECTED");
                _categorySelected = new String[schregs.length];
                for (int i = 0; i < schregs.length; i++) {
                    final String[] schregArray = StringUtils.split(schregs[i], "-");
                    _categorySelected[i] = schregArray[0];
                }
            }
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _grade = request.getParameter("GRADE");
            _gradeHr = request.getParameter("GRADE_HR_CLASS");
            _schoolKind = getSchoolKind(db2);
            _prgid = request.getParameter("PRGID");
            _printDate = request.getParameter("PRINT_DATE");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _schoolcd = request.getParameter("SCHOOLCD");
            _loginSchoolKind = request.getParameter("SCHOOLKIND");
            _semester = request.getParameter("SEMESTER");
            _summaryDate = request.getParameter("SUMMARY_DATE");
            _testcd = request.getParameter("TESTCD");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");

            setCertifSchoolDat(db2);

            _semesterMap = loadSemester(db2);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _testItemMstSdivMap = getTestItemMstSdiv(db2);
            loadNameMstD026(db2);
            setSubclassMst(db2);
            _scoreBunpuMap = getScoreBunpuMap(db2);
        }

        private String getSchoolKind(final DB2UDB db2) {
            String grade = _grade;
            if ("2".equals(_disp)) {
                final String[] gradeHr = StringUtils.split(_gradeHr, "-");
                grade = gradeHr[0];
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GRADE = '" + grade + "' ");
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

            final String retStr = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_KIND"));
            return retStr;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '117' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                String grade = _grade;
                if ("2".equals(_disp)) {
                    final String[] gradeHr = StringUtils.split(_gradeHr, "-");
                    grade = gradeHr[0];
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER, ");
                stb.append("     SEMESTERNAME, ");
                stb.append("     SDATE, ");
                stb.append("     EDATE ");
                stb.append(" FROM ");
                stb.append("     V_SEMESTER_GRADE_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND GRADE = '" + grade + "' ");
                stb.append("     AND SEMESTER <> '9' ");
                stb.append(" ORDER BY ");
                stb.append("     SEMESTER ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new TreeMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getTestItemMstSdiv(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD, ");
                stb.append("     SDIV.SCORE_DIV, ");
                stb.append("     SDIV.TESTITEMNAME, ");
                stb.append("     SEME_DETAIL.SDATE, ");
                stb.append("     SEME_DETAIL.EDATE ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV ");
                stb.append("     LEFT JOIN SEMESTER_DETAIL_MST SEME_DETAIL ON SDIV.YEAR = SEME_DETAIL.YEAR ");
                stb.append("          AND SDIV.SEMESTER = SEME_DETAIL.SEMESTER ");
                stb.append("          AND SDIV.SEMESTER_DETAIL = SEME_DETAIL.SEMESTER_DETAIL ");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND SDIV.TESTKINDCD <> '99' ");
                stb.append("     AND SDIV.SEMESTER || '-' || SDIV.TESTKINDCD || '-' || SDIV.TESTITEMCD || '-' || SDIV.SCORE_DIV <= '" + _semester + "-" + _testcd + "' ");
                stb.append(" ORDER BY ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD, ");
                stb.append("     SDIV.SCORE_DIV ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String testItemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String testitemName = rs.getString("TESTITEMNAME");
                    final String sDate = rs.getString("SDATE");
                    final String eDate = rs.getString("EDATE");
                    final String key = semester + "-" + testKindCd + "-" + testItemCd + "-" + scoreDiv;
                    final TestItemMstSdiv itemMstSdiv = new TestItemMstSdiv(semester, testKindCd, testItemCd, scoreDiv, testitemName, sDate, eDate);
                    retMap.put(key, itemMstSdiv);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("非表示科目:" + _d026List);
        }

        private Map getScoreBunpuMap(final DB2UDB db2) {
            String grade = _grade;
            if ("2".equals(_disp)) {
                final String[] gradeHr = StringUtils.split(_gradeHr, "-");
                grade = gradeHr[0];
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     RANK_SDIV.SEMESTER, ");
                stb.append("     RANK_SDIV.TESTKINDCD, ");
                stb.append("     RANK_SDIV.TESTITEMCD, ");
                stb.append("     RANK_SDIV.SCORE_DIV, ");
                stb.append("     SUM(CASE WHEN VALUE(RANK_SDIV.SCORE, 0) BETWEEN 0 AND 350 THEN 1 ELSE 0 END) AS SCORE350, ");
                stb.append("     SUM(CASE WHEN VALUE(RANK_SDIV.SCORE, 0) BETWEEN 351 AND 400 THEN 1 ELSE 0 END) AS SCORE400, ");
                stb.append("     SUM(CASE WHEN VALUE(RANK_SDIV.SCORE, 0) BETWEEN 401 AND 450 THEN 1 ELSE 0 END) AS SCORE450, ");
                stb.append("     SUM(CASE WHEN VALUE(RANK_SDIV.SCORE, 0) >= 451 THEN 1 ELSE 0 END) AS SCORE500 ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = RANK_SDIV.SCHREGNO ");
                stb.append("         AND REGD.YEAR = '" + _ctrlYear + "' ");
                stb.append("         AND (RANK_SDIV.SEMESTER = '9' AND REGD.SEMESTER = '" + _semester + "' ");
                stb.append("           OR RANK_SDIV.SEMESTER <> '9' AND REGD.SEMESTER = RANK_SDIV.SEMESTER) ");
                stb.append("         AND REGD.GRADE = '" + grade + "' ");
                stb.append(" WHERE ");
                stb.append("     RANK_SDIV.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND RANK_SDIV.TESTKINDCD <> '99' ");
                stb.append("     AND RANK_SDIV.SUBCLASSCD = '999999' ");
                stb.append(" GROUP BY ");
                stb.append("     RANK_SDIV.SEMESTER, ");
                stb.append("     RANK_SDIV.TESTKINDCD, ");
                stb.append("     RANK_SDIV.TESTITEMCD, ");
                stb.append("     RANK_SDIV.SCORE_DIV ");
                stb.append(" ORDER BY ");
                stb.append("     RANK_SDIV.SEMESTER, ");
                stb.append("     RANK_SDIV.TESTKINDCD, ");
                stb.append("     RANK_SDIV.TESTITEMCD, ");
                stb.append("     RANK_SDIV.SCORE_DIV ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String testItemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String score350 = rs.getString("SCORE350");
                    final String score400 = rs.getString("SCORE400");
                    final String score450 = rs.getString("SCORE450");
                    final String score500 = rs.getString("SCORE500");
                    final String key = semester + "-" + testKindCd + "-" + testItemCd + "-" + scoreDiv;
                    final ScoreBunpu bunpu = new ScoreBunpu(semester, testKindCd, testItemCd, scoreDiv, score350, score400, score450, score500);
                    retMap.put(key, bunpu);
                }
            } catch (final Exception ex) {
                log.error("分布のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

    }
}

// eof
