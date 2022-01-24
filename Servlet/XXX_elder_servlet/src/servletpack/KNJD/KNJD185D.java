/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 58b4ab4032549266a5099fb4cd999f8f74ac17c3 $
 *
 * 作成日: 2019/03/26
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185D {

    private static final Log log = LogFactory.getLog(KNJD185D.class);

    private boolean _hasData;
    private static final String SEMEALL = "9";
    private static final String SEME3 = "3";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SCORE990008 = "990008";
    private static final String SCORE990009 = "990009";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

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
        //下段の出欠
        AttendSemesDat.setAttendSemesDatMap(db2, _param, studentList);
        //欠課
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            SubclassAttendance.load(db2, _param, studentList, range);
        }
        printOut(db2, svf, studentList);
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            svf.VrSetForm("KNJD185D.frm", 4);
            final Student student = (Student) iterator.next();

            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._loginYear)) + "年度");
            svf.VrsOut("TITLE", "成績通知表");

            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);

            svf.VrsOut("COURSE_NAME", student._courseCodeName);
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._grade)));
            svf.VrsOut("HR", String.valueOf(Integer.parseInt(student._hrClass)));
            svf.VrsOut("NO", student._attendno);
            final int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String nameField = nameLen > 30 ? "3" : nameLen > 20 ? "3" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            final String priField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 30 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + priField, _param._certifSchoolPrincipalName);
            svf.VrsOut("STAFFBTM_1_1C", _param.getImageFilePath(_param._priCd));
            final String stfField = KNJ_EditEdit.getMS932ByteLength(student._staffname) > 30 ? "2" : "1";
            svf.VrsOut("TR_NAME" + stfField, student._staffname);
            svf.VrsOut("STAFFBTM_2_1C", _param.getImageFilePath(student._staffCd));

            //出欠記録
            printAttend(svf, student);

            //総合的な学習の評価
//            printHaveNewLine(svf, _param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE, student._totalstudytime, "TOTALSTUDYTIME", 60, 6);
            printHaveNewLine(svf, "", student._totalstudytime, "TOTALSTUDYTIME", 60, 6);

            //通信欄
//            printHaveNewLine(svf, _param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, student._communication, "COMMUNICATION", 60, 7);
            printHaveNewLine(svf, "", student._communication, "COMMUNICATION", 60, 7);

            //明細部分
            final List subclassList = subclassListRemoveD026();
            Collections.sort(subclassList);

            //学習の記録
            int totalCredit = 0;
            int line = 1;
            //科目マスタでループ
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                //生徒が受けていない科目は読み飛ばす
                if (!student._printSubclassMap.containsKey(subclassCd)) {
                    continue;
                }
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get(subclassCd);

                final int subclassNameLen = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                final String subclassNameField = subclassNameLen > 18 ? "_2" : "_1";
                svf.VrsOutn("SUBCLASS_NAME1" + subclassNameField, line, subclassMst._subclassname);

                svf.VrsOutn("CREDIT", line, printSubclass._credit);
                totalCredit += StringUtils.isEmpty(printSubclass._credit) ? 0 : Integer.parseInt(printSubclass._credit);

                for (Iterator itScore = printSubclass._semesScoreMap.keySet().iterator(); itScore.hasNext();) {
                    final String semester = (String) itScore.next();
                    if("2".equals(semester) && !_param._semes2Flg) continue;
                    final Map scoreMap = (Map) printSubclass._semesScoreMap.get(semester);
                    final String scoreSemes = "9".equals(semester) ? "3" : semester;
                    if (scoreMap.containsKey(SCORE990008)) {
                        final ScoreData scoreData = (ScoreData) scoreMap.get(SCORE990008);
                        svf.VrsOutn("SCORE" + scoreSemes, line, scoreData._score);

                        //学年成績が1の時追試に*
                        if (SEMEALL.equals(semester) && "1".equals(scoreData._score)) {
                            svf.VrsOutn("SUPP", line, "*");
                            //追試合格フラグ
                            if ("1".equals(scoreData._passFlg)) {
                                svf.VrsOutn("SUPP_PASS", line, "合");
                            } else {
                                svf.VrsOutn("SUPP_PASS", line, "不");
                            }
                        }
                    }
                }
                if (student._attendSubClassMap.containsKey(subclassCd)) {
                    final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
                    int totalAbsence = 0;
                    for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
                        final String semester = (String) it.next();
                        if (atSubSemeMap.containsKey(semester)) {
                            final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(semester);
                            totalAbsence += attendance._sick.intValue();
                        }
                    }
                    svf.VrsOutn("ABSENCE", line, String.valueOf(totalAbsence));
                }

                line++;
            }
            svf.VrsOut("TOTAL_CREDIT", String.valueOf(totalCredit));

            //ALL9の合計、平均、順位
            for (Iterator itSubclass = student._printSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get(subclassCd);

                final String[] subclassArray = StringUtils.split(subclassCd, "-");
                if (ALL9.equals(subclassArray[3])) {
                    for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                    	final String semester = (semei==3) ? "9" : String.valueOf(semei);
                        if("2".equals(semester) && !_param._semes2Flg) continue;
                        if (printSubclass._semesScoreMap.containsKey(semester)) {
                            final Map scoreMap = (Map) printSubclass._semesScoreMap.get(semester);
                            if (scoreMap.containsKey(SCORE990008)) {
                                final ScoreData scoreData = (ScoreData) scoreMap.get(SCORE990008);
                                //ALL9
                                svf.VrsOut("TOTAL_SCORE" + semei, scoreData._score);
                                if (!StringUtils.isEmpty(scoreData._avg)) {
                                    final BigDecimal avgBD = new BigDecimal(scoreData._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    svf.VrsOut("AVG_SCORE" + semei, avgBD.toString());
                                }
                                svf.VrsOut("CLASS_RANK" + semei, scoreData._classRank);
                                svf.VrsOut("COURSE_RANK" + semei, scoreData._courseRank);
                            }
                        }
                    }
                }
            }

            final String setValueAvg = (String) student._studyRecClassMap.get("VALUE_AVG");
            svf.VrsOut("VALUE_AVG", StringUtils.defaultString(setValueAvg));
            final String setGaihyou = (String) student._studyRecClassMap.get("GAIHYOU");
            svf.VrsOut("GAIHYOU", StringUtils.defaultString(setGaihyou));
            printSvfStudyRec(svf, student);

            _hasData = true;
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

    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */

    private void printAttend(final Vrw32alp svf, final Student student) {
        final String[] months = new String[] {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};
        final String[] monthsName = new String[] {"４月", "５月", "６月", "７月", "８月", "９月", "１０月", "１１月", "１２月", "１月", "２月", "３月"};

        int lessonTotal = 0;
        int suspendTotal = 0;
        int abroadTotal = 0;
        int mlessonTotal = 0;
        int sickTotal = 0;
        int presentTotal = 0;
        int lateTotal = 0;
        int earlyTotal = 0;
        for (int i = 0; i < months.length; i++) {
            final AttendSemesDat attSemes = (AttendSemesDat) student._attendSemesMap.get(months[i]);
            final int j = i + 2;

            svf.VrsOutn("MONTH", j, monthsName[i]);
            if (null == attSemes) {
                continue;
            }
            svf.VrsOutn("LESSON", j, attendVal(attSemes._lesson));
            svf.VrsOutn("SUSPEND", j, attendVal(attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome));
            svf.VrsOutn("ABROAD", j, attendVal(attSemes._abroad));
            svf.VrsOutn("MUST", j, attendVal(attSemes._mlesson));
            svf.VrsOutn("NOTICE", j, attendVal(attSemes._sick));
            svf.VrsOutn("ATTEND", j, attendVal(attSemes._present));
            svf.VrsOutn("LATE", j, attendVal(attSemes._late));
            svf.VrsOutn("EARLY", j, attendVal(attSemes._early));
            lessonTotal  += attSemes._lesson;
            suspendTotal += attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome;
            abroadTotal  += attSemes._abroad;
            mlessonTotal += attSemes._mlesson;
            sickTotal    += attSemes._sick;
            presentTotal += attSemes._present;
            lateTotal    += attSemes._late;
            earlyTotal   += attSemes._early;
        }
        svf.VrsOutn("MONTH", 1, "計");
        svf.VrsOutn("LESSON", 1, attendVal(lessonTotal));
        svf.VrsOutn("SUSPEND", 1, attendVal(suspendTotal));
        svf.VrsOutn("ABROAD", 1, attendVal(abroadTotal));
        svf.VrsOutn("MUST", 1, attendVal(mlessonTotal));
        svf.VrsOutn("NOTICE", 1, attendVal(sickTotal));
        svf.VrsOutn("ATTEND", 1, attendVal(presentTotal));
        svf.VrsOutn("LATE", 1, attendVal(lateTotal));
        svf.VrsOutn("EARLY", 1, attendVal(earlyTotal));
    }

    private static String attendVal(int n) {
        return String.valueOf(n);
    }

    //備考欄印刷
    private void printHaveNewLine(final Vrw32alp svf, final String propertie, final String printText, final String fieldName, final int defLen, final int defRow) {
        if (!StringUtils.isEmpty(printText)) {
            final String[] nums = StringUtils.split(StringUtils.replace(propertie, "+", " "), " * ");
            int rLen = defLen;
            int rRow = defRow;
            if (null != nums && nums.length == 2) {
                rLen = Integer.parseInt(nums[0]);
                rRow = Integer.parseInt(nums[1]);
            }
            final String[] remarkArray = KNJ_EditEdit.get_token(printText, rLen, rRow);
            for (int i = 0; i < remarkArray.length; i++) {
                final String setRemark = remarkArray[i];
                svf.VrsOutn(fieldName, (i + 1), setRemark);
            }
        }
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfStudyRec(final Vrw32alp svf, final Student student) {

        String befClassCd = "";
        String befSubClassCd = "";
        for (final Iterator itv = student._studyRecList.iterator(); itv.hasNext();) {
            final StudyRec studyRec = (StudyRec) itv.next();

            if (!"".equals(befSubClassCd) && !befSubClassCd.equals(studyRec._subclasscd)) {
                svf.VrEndRecord();
            }

            svf.VrsOut("GRPCD1", studyRec._classcd);
            svf.VrsOut("GRPCD2", studyRec._classcd);
            if (!befClassCd.equals(studyRec._classcd)) {
                svf.VrsOut("CLASS_NAME2_1", studyRec._className);
                if (student._studyRecClassMap.containsKey(studyRec._classcd)) {
                    final BigDecimal setSubValueAvg = (BigDecimal) student._studyRecClassMap.get(studyRec._classcd);
                    svf.VrsOut("SUB_VALUE_AVG", StringUtils.defaultString(setSubValueAvg.toString()));
                }
            }
            final int subclassNameLen = KNJ_EditEdit.getMS932ByteLength(studyRec._subclassname);
            final String subclassNameField = subclassNameLen > 18 ? "_2" : "_1";
            svf.VrsOut("SUBCLASS_NAME2" + subclassNameField, studyRec._subclassname);

            svf.VrsOut("STUDYREC" + studyRec._fieldNo, studyRec._value);

            befClassCd = studyRec._classcd;
            befSubClassCd = studyRec._subclasscd;
        }
        svf.VrEndRecord();
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
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._courseCodeName = rs.getString("COURSECODENAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student.setRankSdiv(db2);
                student.setHreport(db2);
                student._studyRecList = StudyRec.getStudyRecList(db2, _param, student._schregno);
                student.setStudyRecAvgANDGaihyou(db2);
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
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
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
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE_M ON COURSECODE_M.COURSECODE = REGD.COURSECODE ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
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

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffCd;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _courseCodeName;
        String _hrClassName1;
        String _entyear;
        String _communication;
        String _totalstudytime;
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();
        List _studyRecList = Collections.EMPTY_LIST; // STUDYREC
        final Map _studyRecClassMap = new HashMap(); // 教科毎の
        Map _attendSemesMap = Collections.EMPTY_MAP; // 出欠の記録

        public Student() {
        }

        private void setRankSdiv(final DB2UDB db2) {
            final String scoreSql = getRankSdivSql();
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
                    final String avg = rs.getString("AVG");
                    final String classRank = rs.getString("CLASS_RANK");
                    final String courseRank = rs.getString("COURSE_RANK");
                    final String passFlg = rs.getString("PASS_FLG");
                    PrintSubclass printSubclass = null;
                    if (!_printSubclassMap.containsKey(subclassCd)) {
                        printSubclass = new PrintSubclass(subclassCd, credit);
                        _printSubclassMap.put(subclassCd, printSubclass);
                    } else {
                        printSubclass = (PrintSubclass) _printSubclassMap.get(subclassCd);
                    }

                    Map scoreMap = new TreeMap();
                    if (printSubclass._semesScoreMap.containsKey(semester)) {
                        scoreMap = (Map) printSubclass._semesJviewMap.get(semester);
                    }
                    final String testKey = testkindCd + testitemCd + scoreDiv;
                    ScoreData scoreData = new ScoreData(score, avg, classRank, courseRank, passFlg);
                    if (scoreMap.containsKey(testKey)) {
                        scoreData = (ScoreData) scoreMap.get(testKey);
                    }
                    scoreMap.put(testKey, scoreData);
                    printSubclass._semesScoreMap.put(semester, scoreMap);
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
            stb.append("     RANK_SDIV.AVG, ");
            stb.append("     RANK_SDIV.CLASS_RANK, ");
            stb.append("     RANK_SDIV.COURSE_RANK, ");
            stb.append("     SLUMP.PASS_FLG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
            stb.append("     LEFT JOIN CREDIT_MST CRE ON RANK_SDIV.YEAR = CRE.YEAR ");
            stb.append("          AND CRE.COURSECD || CRE.MAJORCD || CRE.COURSECODE = '" + _course + "' ");
            stb.append("          AND CRE.GRADE = '" + _grade + "' ");
            stb.append("          AND RANK_SDIV.CLASSCD || RANK_SDIV.SCHOOL_KIND || RANK_SDIV.CURRICULUM_CD || RANK_SDIV.SUBCLASSCD  = CRE.CLASSCD || CRE.SCHOOL_KIND || CRE.CURRICULUM_CD || CRE.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_SLUMP_SDIV_DAT SLUMP ON RANK_SDIV.YEAR = SLUMP.YEAR ");
            stb.append("          AND RANK_SDIV.SEMESTER = SLUMP.SEMESTER ");
            stb.append("          AND SLUMP.TESTKINDCD = '99' ");
            stb.append("          AND SLUMP.TESTITEMCD = '00' ");
            stb.append("          AND SLUMP.SCORE_DIV = '08' ");
            stb.append("          AND RANK_SDIV.CLASSCD || RANK_SDIV.SCHOOL_KIND || RANK_SDIV.CURRICULUM_CD || RANK_SDIV.SUBCLASSCD  = SLUMP.CLASSCD || SLUMP.SCHOOL_KIND || SLUMP.CURRICULUM_CD || SLUMP.SUBCLASSCD ");
            stb.append("          AND RANK_SDIV.SCHREGNO = SLUMP.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     RANK_SDIV.YEAR = '" + _param._loginYear + "' ");
            stb.append("     AND ( RANK_SDIV.SEMESTER <= '" + _param._semester + "' OR RANK_SDIV.SEMESTER = '9' ) ");
            stb.append("     AND RANK_SDIV.TESTKINDCD = '99' ");
            stb.append("     AND RANK_SDIV.TESTITEMCD = '00' ");
            stb.append("     AND RANK_SDIV.SCORE_DIV = '08' ");
            stb.append("     AND RANK_SDIV.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND (RANK_SDIV.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' OR RANK_SDIV.SUBCLASSCD = '" + ALL9 + "') ");
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

            _totalstudytime = "";
            final String hreportSeme9Sql = hreportSeme9Sql();
            ps = null;
            rs = null;
            try {
                ps = db2.prepareStatement(hreportSeme9Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalstudytime = rs.getString("TOTALSTUDYTIME");
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
            stb.append("     YEAR = '" + _param._loginYear + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

        private String hreportSeme9Sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TOTALSTUDYTIME ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._loginYear + "' ");
            stb.append("     AND SEMESTER = '9' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

        private void setStudyRecAvgANDGaihyou(final DB2UDB db2) {
            String befClassCd = "";
            int totalClassVal = 0;
            int totalClassCnt = 0;
            final List valueList = new ArrayList();
            for (Iterator itStudyRec = _studyRecList.iterator(); itStudyRec.hasNext();) {
                final StudyRec studyRec = (StudyRec) itStudyRec.next();
                if ("".equals(befClassCd) || befClassCd.equals(studyRec._classcd)) {
                    if (!StringUtils.isEmpty(studyRec._value)) {
                        totalClassVal += Integer.parseInt(studyRec._value);
                        totalClassCnt++;
                    }
                } else {
                    if (totalClassCnt > 0) {
                        final BigDecimal studyBd = new BigDecimal(totalClassVal).divide(new BigDecimal(totalClassCnt), 1, BigDecimal.ROUND_HALF_UP);
                        _studyRecClassMap.put(befClassCd, studyBd);
                    }
                    totalClassCnt = 0;
                    totalClassVal = 0;
                    if (!StringUtils.isEmpty(studyRec._value)) {
                        totalClassVal += Integer.parseInt(studyRec._value);
                        totalClassCnt++;
                    }
                }
                befClassCd = studyRec._classcd;
                if (!StringUtils.isEmpty(studyRec._value)) {
                	valueList.add(studyRec._value);
                }
            }
            if (totalClassCnt > 0) {
                final BigDecimal studyBd = new BigDecimal(totalClassVal).divide(new BigDecimal(totalClassCnt), 1, BigDecimal.ROUND_HALF_UP);
                _studyRecClassMap.put(befClassCd, studyBd);
            }

            _studyRecClassMap.put("VALUE_AVG", "");
            _studyRecClassMap.put("GAIHYOU", "");
            if (valueList.size() > 0) {
                final String avg = average(valueList);
                _studyRecClassMap.put("VALUE_AVG", avg);

                if (NumberUtils.isNumber(avg)) {
                	final String assessMstSql = getAssessMstSql(avg);
                	PreparedStatement ps = null;
                	ResultSet rs = null;
                	try {
                		ps = db2.prepareStatement(assessMstSql);
                		rs = ps.executeQuery();
                		while (rs.next()) {
                			final String assessMark = rs.getString("ASSESSMARK");
                			_studyRecClassMap.put("GAIHYOU", assessMark);
                		}
                	} catch (Exception e) {
                		log.error("Exception", e);
                	} finally {
                		DbUtils.closeQuietly(ps);
                		db2.commit();
                	}
                }
            }

        }

        private String average(final List scoreList) {
        	BigDecimal sum = new BigDecimal(0);
        	int count = 0;
        	for (final Iterator it = scoreList.iterator(); it.hasNext();) {
        		final String score = (String) it.next();
        		if (NumberUtils.isNumber(score)) {
        			sum = sum.add(new BigDecimal(score));
        			count += 1;
        		}
        	}
        	//log.info(" scoreList = " + scoreList + ", sum = " + sum + ", count = " + count + ", avg = " + sum.divide(new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP));
        	return count == 0 ? null : sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private String getAssessMstSql(final String score) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ASSESSMARK ");
            stb.append(" FROM ");
            stb.append("     ASSESS_MST ASSESS ");
            stb.append(" WHERE ");
            stb.append("     ASSESS.ASSESSCD = '4' ");
            stb.append("     AND " + score + " BETWEEN ASSESS.ASSESSLOW AND ASSESS.ASSESSHIGH ");

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
        final Map _semesScoreMap;
        private PrintSubclass(
                final String subclassCd,
                final String credit
        ) {
            _subclassCd = subclassCd;
            _credit = credit;
            _semesJviewMap = new TreeMap();
            _semesHyoukaMap = new TreeMap();
            _semesScoreMap = new TreeMap();
        }
    }

    private class ScoreData {
        final String _score;
        final String _avg;
        final String _classRank;
        final String _courseRank;
        final String _passFlg;
        private ScoreData(
                final String score,
                final String avg,
                final String classRank,
                final String courseRank,
                final String passFlg
        ) {
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _courseRank = courseRank;
            _passFlg = passFlg;
        }
    }

    /**
     * StudyRec
     */
    private static class StudyRec {
        final String _year;
        final String _fieldNo;
        final String _gradeName;
        final String _classcd;
        final String _className;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        StudyRec(
                final String year,
                final String fieldNo,
                final String gradeName,
                final String classcd,
                final String className,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _year = year;
            _fieldNo = fieldNo;
            _gradeName = gradeName;
            _classcd = classcd;
            _className = className;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }

        public static List getStudyRecList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudyRecSql(param, schregno);
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String year = rs.getString("YEAR");
                    final String fieldNo = rs.getString("FIELD_NO");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    final String classcd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String value = rs.getString("VALUE");
                    final StudyRec studyRec = new StudyRec(year, fieldNo, gradeName, classcd, className, subclasscd, subclassname, value);

                    list.add(studyRec);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getStudyRecSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEAR_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     MAX(REGD.YEAR) AS YEAR, ");
            stb.append("     ROW_NUMBER() OVER (ORDER BY REGD.GRADE) AS FIELD_NO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
            stb.append("           AND REGD.GRADE = GDAT.GRADE ");
            stb.append("           AND GDAT.SCHOOL_KIND = 'H' ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR <= '" + param._loginYear + "' ");
            stb.append("     AND REGD.SCHREGNO = '" + schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     GDAT.GRADE_NAME1 ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE ");
            stb.append(" FETCH FIRST 3 ROWS ONLY ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     STUDYREC.YEAR, ");
            stb.append("     YEAR_T.GRADE_NAME1, ");
            stb.append("     YEAR_T.FIELD_NO, ");
            stb.append("     STUDYREC.CLASSCD AS CLASSCD, ");
            stb.append("     STUDYREC.CLASSCD || STUDYREC.SCHOOL_KIND || STUDYREC.CURRICULUM_CD || STUDYREC.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CASE WHEN STUDYREC.SUBCLASSNAME IS NOT NULL ");
            stb.append("          THEN STUDYREC.SUBCLASSNAME ");
            stb.append("          ELSE VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN STUDYREC.CLASSNAME IS NOT NULL ");
            stb.append("          THEN STUDYREC.CLASSNAME ");
            stb.append("          ELSE VALUE(CLAM.CLASSORDERNAME2, CLAM.CLASSNAME) ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     STUDYREC.VALUATION AS VALUE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT STUDYREC ");
            stb.append("     INNER JOIN YEAR_T ON YEAR_T.YEAR = STUDYREC.YEAR ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = STUDYREC.SUBCLASSCD ");
            stb.append("          AND SUBM.CLASSCD = STUDYREC.CLASSCD ");
            stb.append("          AND SUBM.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ");
            stb.append("          AND SUBM.CURRICULUM_CD = STUDYREC.CURRICULUM_CD ");
            stb.append("     LEFT JOIN CLASS_MST CLAM ON CLAM.CLASSCD = STUDYREC.CLASSCD ");
            stb.append("          AND CLAM.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     STUDYREC.YEAR <= '" + param._loginYear + "' ");
            stb.append("     AND STUDYREC.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND STUDYREC.CLASSCD < '" + KNJDefineSchool.subject_T + "' ");
            stb.append("     AND STUDYREC.VALUATION IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     STUDYREC.CLASSCD || STUDYREC.SCHOOL_KIND || STUDYREC.CURRICULUM_CD || STUDYREC.SUBCLASSCD, ");
            stb.append("     STUDYREC.YEAR ");

            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _month;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _abroad;
        int _offdays;
        int _virus;
        int _koudome;

        private AttendSemesDat(
                final String month
        ) {
            _month = month;
        }

        private void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _abroad += o._abroad;
            _offdays += o._offdays;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT * ");
                sql.append(" FROM ATTEND_SEMES_DAT ");
                sql.append(" WHERE YEAR = '" + param._loginYear + "' ");
                sql.append("   AND SEMESTER <= '" + param._semester + "' ");
                sql.append("   AND SCHREGNO = ? ");
                sql.append(" ORDER BY SEMESTER,  INT(MONTH) + CASE WHEN MONTH < '04' THEN 12 ELSE 0 END ");

                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._attendSemesMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        if (!NumberUtils.isDigits(rs.getString("APPOINTED_DAY"))) {
                            log.warn("ATTEND_SEMES_DAT.APPOINTED_DAY = " + rs.getString("APPOINTED_DAY") + ", YEAR = " + rs.getString("YEAR") + ", SEMESTER = " + rs.getString("SEMESTER") + ", MONTH = " + rs.getString("MONTH") + ", SCHREGNO = " + rs.getString("SCHREGNO"));
                            continue;
                        }

                        final String month = rs.getString("MONTH");
                        final String year = String.valueOf(rs.getInt("YEAR") + (Integer.parseInt(month) < 4 ? 1 : 0));
                        final String semesDate = year + "-" + month + "-" + param._df02.format(Integer.parseInt(rs.getString("APPOINTED_DAY")));
                        if (semesDate.compareTo(param._date) > 0) {
                            break;
                        }

                        final int lesson0 = rs.getInt("LESSON");
                        final int abroad = rs.getInt("ABROAD");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int lesson = lesson0 - abroad - offdays + ("1".equals(param._knjSchoolMst._semOffDays) ? offdays : 0);
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int sick = rs.getInt("SICK") + rs.getInt("NOTICE") + rs.getInt("NONOTICE");
                        final int virus = "true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0;
                        final int koudome = "true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0;
                        final int mlesson = lesson - suspend - virus - koudome - mourning;
                        final int present = mlesson - sick;

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(month);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._abroad = abroad;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = rs.getInt("ABSENT");
                        attendSemesDat._present = present;
                        attendSemesDat._late = rs.getInt("LATE");
                        attendSemesDat._early = rs.getInt("EARLY");
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;
                        attendSemesDat._mlesson = mlesson;

                        if (null != student._attendSemesMap.get(month)) {
                            final AttendSemesDat before = (AttendSemesDat) student._attendSemesMap.get(month);
                            before.add(attendSemesDat);
                        } else {
                            student._attendSemesMap.put(month, attendSemesDat);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._loginYear,
                        SEMEALL,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (null == student._printSubclassMap.get(subclasscd)) {
                                continue;
                            }

                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                            final BigDecimal late = rs.getBigDecimal("LATE");
                            final BigDecimal early = rs.getBigDecimal("EARLY");

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);
                            Map setSubAttendMap = null;
                            if (student._attendSubClassMap.containsKey(subclasscd)) {
                                setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                            } else {
                                setSubAttendMap = new TreeMap();
                            }
                            setSubAttendMap.put(dateRange._key, subclassAttendance);

                            student._attendSubClassMap.put(subclasscd, setSubAttendMap);
                        }

                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
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

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75184 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _disp;
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _loginDate;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _printLogRemoteAddr;
        final String _printLogRemoteIdent;
        final String _printLogStaffcd;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE;
        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE;
        final String _semester;
        final boolean _semes2Flg;
        final String _useVirus;
        final String _useKoudome;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;

        /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final DecimalFormat _df02 = new DecimalFormat("00");

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map _semesterMap;
        private Map _subclassMstMap;
        private List _d026List = Collections.EMPTY_LIST;
        Map _attendRanges;

        final String _documentroot;
        /** 写真データ格納フォルダ */
        final String _imageDir;
        /** 写真データの拡張子 */
        final String _imageExt;
        String _priCd;
        final Map _stampMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = _gradeHrClass;
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _prgid = request.getParameter("PRGID");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE");
            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE = request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE");
            _semester = request.getParameter("SEMESTER");
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            loadNameMstD026(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterMap = loadSemester(db2);
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);

            setPriStaffCd(db2);
            _stampMap = getStampNoMap(db2);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imageDir = "image/stamp";
            _imageExt = ".bmp";

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _loginYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER, ");
                stb.append("     SEMESTERNAME, ");
                stb.append("     SDATE, ");
                stb.append("     EDATE ");
                stb.append(" FROM ");
                stb.append("     V_SEMESTER_GRADE_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _loginYear + "' ");
                stb.append("     AND GRADE = '" + _grade + "' ");
                stb.append("     AND SEMESTER <= '" + _semester + "' ");
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
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
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

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        private void setPriStaffCd(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     STAFF_PRINCIPAL_HIST_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHOOL_KIND = 'H' ");
            stb.append("     AND '" + _date + "' BETWEEN FROM_DATE AND VALUE(TO_DATE, '9999-12-31') ");

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

            _priCd = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFCD"));
        }

        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public String getImageFilePath(final String staffCd) {
            final String stampNo = (String) _stampMap.get(staffCd);
            final String path = _documentroot + "/" + _imageDir + "/" + stampNo + _imageExt;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

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

    }
}

// eof
