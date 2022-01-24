// kanji=漢字
/*
 * $Id: 2b7b9d6bf18cb974f78c53d74f696e460d6a5000 $
 *
 * 作成日: 2010/05/27 10:38:42 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 2b7b9d6bf18cb974f78c53d74f696e460d6a5000 $
 */
public class KNJD194 {

    private static final Log log = LogFactory.getLog("KNJD194.class");

    private static final String FORM_NAME = "KNJD194.frm";

    private boolean _hasData;

    Param _param;

    private static final String SOTEN = "1";
    private static final String HYOUKA = "2";

    private static final String JOUGEN_RISYU = "1";
    private static final String JOUGEN_SYUTOKU = "2";

    private static final String JOUGEN_WARN = "1";
    private static final String JOUGEN_OVER = "2";

    private static final String ALL_OUT = "ALL_OUT";
    private static final String SCORE_OUT = "SCORE_OUT";
    private static final String ATTEND_OUT = "ATTEND_OUT";

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if ("csv".equals(_param._cmd)) {
                final List outputList = new ArrayList();
                printMain(db2, outputList);
                if (_hasData) {
                    final String filename = KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度 " + StringUtils.defaultString(_param._semesterName) + " " + StringUtils.defaultString(_param._testName) + getTitle(null) + ".csv";
                    CsvUtils.outputLines(log, response, filename, outputList);
                } else {
                    response.setContentType("text/html; charset=UTF-8");

                    final String message = "データは存在していません。";
                    final String js = "<script text=\"javascript\">alert(\"" + message + "\");top.window.close();</script>";
                    response.setContentLength(js.getBytes("UTF-8").length);

                    final PrintStream printStream = new PrintStream(response.getOutputStream());
                    printStream.println(js);
                }
            } else {
                svf = new Vrw32alp();
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != svf) {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }

                svf.VrQuit();
            }
            closeDb(db2);
        }

    }

    private void printMain(final DB2UDB db2, final Object output) throws SQLException {

        if (output instanceof List) {
            List outputList = (List) output;
            outputList.add(Arrays.asList(new String[] {"学籍番号", "学年", "組", "出席番号", "氏名", "科目コード", "科目名", "教科担当者", "単位", "素点", "欠時", "欠課", "遅早", "公欠", "忌引", "出停", "不振区分"})); // 担任
        }


        final List gradeDatas = getPrintGradeData(db2);

        String befGrade = "";
        int grpCnt = 1;
        for (final Iterator iter = gradeDatas.iterator(); iter.hasNext();) {

            final GradeData gradeData = (GradeData) iter.next();
            for (final Iterator itGrade = gradeData._studentList.iterator(); itGrade.hasNext();) {
                final Student student = (Student) itGrade.next();
                if (!befGrade.equals(student._grade)) {
                    if (output instanceof Vrw32alp) {
                        Vrw32alp svf = (Vrw32alp) output;
                        svf.VrSetForm(FORM_NAME, 4);
                    }
                }

                grpCnt = outPutMain(db2, output, gradeDatas, student, ALL_OUT, grpCnt);
                grpCnt = outPutMain(db2, output, gradeDatas, student, SCORE_OUT, grpCnt);
                grpCnt = outPutMain(db2, output, gradeDatas, student, ATTEND_OUT, grpCnt);

                befGrade = student._grade;
            }
        }
    }

    private int outPutMain(final DB2UDB db2, final Object output, final List gradeDatas, final Student student, final String printDiv, int grpCnt) throws SQLException {
        Set printDataSet = student.getSubclassCds(true, false);
        if (ALL_OUT.equals(printDiv)) {
            printDataSet = student.getSubclassCds(true, true);
        }
        if (ATTEND_OUT.equals(printDiv)) {
            printDataSet = student.getSubclassCds(false, true);
        }

        for (final Iterator iterator = printDataSet.iterator(); iterator.hasNext();) {

            final String subclassCd = (String) iterator.next();
            final ScoreData scoreData = student._printData.containsKey(subclassCd) ? (ScoreData) student._printData.get(subclassCd) : getScoreData(db2, subclassCd, student);
            final Syukketu syukketu = (Syukketu) student._syukketuMap.get(subclassCd);

            if (!outPutCheck(printDiv, scoreData, syukketu)) {
                continue;
            }
            final String nendo = KNJ_EditDate.h_format_JP_N(_param._ctrlYear + "/04/01") + "度";
            final List teacherList = getTeacherName(db2, subclassCd, student._schregNo);
            if (output instanceof Vrw32alp) {
                final Vrw32alp svf = (Vrw32alp) output;
                svf.VrsOut("TITLE", nendo + "　" + getTitle(printDiv));
                svf.VrsOut("SEMESTER", _param._semesterName);
                svf.VrsOut("TESTITEMNAME", _param._testName);
                svf.VrsOut("SCORE_ITEM", SOTEN.equals(_param._sotenHyouka) ? "素点" : "評価");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("GRADE", student._gradeName);

                svf.VrsOut("SUBCLASS", scoreData._name);
                svf.VrsOut("CREDIT", scoreData._credit);
                svf.VrsOut("SCORE", scoreData._score);
                svf.VrsOut("SLUMP_DIV", getSlumpDiv(printDiv));
                if (null != syukketu) {
                    svf.VrsOut("KETSUJI", String.valueOf(syukketu._ketsuji.intValue()));
                    svf.VrsOut("KEKKA", String.valueOf(syukketu._sick.intValue()));
                    svf.VrsOut("EARLY", String.valueOf(syukketu._late.intValue() + syukketu._early.intValue()));
                    svf.VrsOut("PUB_KEKKA", String.valueOf(syukketu._absent.intValue()));
                    svf.VrsOut("MOURNING", String.valueOf(syukketu._mourning.intValue()));
                    svf.VrsOut("SUSPEND", String.valueOf(syukketu._suspend.intValue()));
                }
                for (Iterator itTeacher = teacherList.iterator(); itTeacher.hasNext();) {
                    String teacher = (String) itTeacher.next();
                    svf.VrsOut("HR_NAME_ATTENDNO", student._hrName + student._attendNo + "番");
                    svf.VrsOut("NAME", student._name);

                    for (int grpSeq = 1; grpSeq < 12; grpSeq++) {
                        svf.VrsOut("GRP" + grpSeq, String.valueOf(grpCnt));
                    }
                    svf.VrsOut("TEACHER", teacher);
                    svf.VrEndRecord();
                }
                grpCnt++;
            } else {
                boolean blank = false;
                for (Iterator itTeacher = teacherList.iterator(); itTeacher.hasNext();) {
                    final List line = new ArrayList();
                    String teacher = (String) itTeacher.next();
                    line.add(blank ? "" : student._schregNo);
                    line.add(blank ? "" : student._grade);
                    line.add(blank ? "" : student._hrClass);
                    line.add(blank ? "" : student._attendNo);
                    line.add(blank ? "" : student._name);
                    line.add(blank ? "" : scoreData._subclassCd);
                    line.add(blank ? "" : scoreData._name);
                    line.add(teacher);
                    line.add(blank ? "" : scoreData._credit);
                    line.add(blank ? "" : scoreData._score);
                    if (null != syukketu) {
                        line.add(blank ? "" : String.valueOf(syukketu._ketsuji.intValue()));
                        line.add(blank ? "" : String.valueOf(syukketu._sick.intValue()));
                        line.add(blank ? "" : String.valueOf(syukketu._late.intValue() + syukketu._early.intValue()));
                        line.add(blank ? "" : String.valueOf(syukketu._absent.intValue()));
                        line.add(blank ? "" : String.valueOf(syukketu._mourning.intValue()));
                        line.add(blank ? "" : String.valueOf(syukketu._suspend.intValue()));
                    } else {
                        line.add(blank ? "" : null);
                        line.add(blank ? "" : null);
                        line.add(blank ? "" : null);
                        line.add(blank ? "" : null);
                        line.add(blank ? "" : null);
                        line.add(blank ? "" : null);
                    }
                    line.add(blank ? "" : getSlumpDiv(printDiv));
                    //new String[] {"学籍番号", "学年", "組", "出席番号", "氏名", "科目コード", "科目名", "教科担当者", "単位", "素点", "欠時", "欠課", "遅早", "公欠", "忌引", "出停", "不振区分"}
                    final List outputList = (List) output;
                    outputList.add(line);
                    blank = true;
                }
            }
            _hasData = true;
        }
        return grpCnt;
    }

    private List getTeacherName(final DB2UDB db2, final String subclassCd, final String schregNo) throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     MIN(T1.CHAIRCD) AS CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("        T1.SUBCLASSCD = '" + subclassCd + "' ");
        stb.append("     AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             CHAIR_STD_DAT E1 ");
        stb.append("             INNER JOIN CHAIR_DAT E2 ON E2.YEAR = E1.YEAR ");
        stb.append("                 AND E2.SEMESTER = E1.SEMESTER ");
        stb.append("                 AND E2.CHAIRCD = E1.CHAIRCD ");
        stb.append("             INNER JOIN (SELECT EE1.YEAR, EE1.SEMESTER, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                EE2.CLASSCD || '-' || EE2.SCHOOL_KIND || '-' || EE2.CURRICULUM_CD || '-' || ");
        }
        stb.append("                                EE2.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("                              , EE1.SCHREGNO, MAX(EE1.APPDATE) AS APPDATE ");
        stb.append("                         FROM CHAIR_STD_DAT EE1 ");
        stb.append("                         INNER JOIN CHAIR_DAT EE2 ON EE2.YEAR = EE1.YEAR ");
        stb.append("                             AND EE2.SEMESTER = EE1.SEMESTER ");
        stb.append("                             AND EE2.CHAIRCD = EE1.CHAIRCD ");
        stb.append("                         GROUP BY EE1.YEAR, EE1.SEMESTER, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                EE2.CLASSCD || '-' || EE2.SCHOOL_KIND || '-' || EE2.CURRICULUM_CD || '-' || ");
        }
        stb.append("                                  EE2.SUBCLASSCD ");
        stb.append("                                , EE1.SCHREGNO) L1 ON L1.YEAR = E1.YEAR ");
        stb.append("                 AND L1.SEMESTER = E1.SEMESTER ");
        stb.append("                 AND L1.SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                E2.CLASSCD || '-' || E2.SCHOOL_KIND || '-' || E2.CURRICULUM_CD || '-' || ");
        }
        stb.append("                     E2.SUBCLASSCD ");
        stb.append("                 AND L1.SCHREGNO = E1.SCHREGNO ");
        stb.append("                 AND L1.APPDATE = E1.APPDATE ");
        stb.append("         WHERE ");
        stb.append("             E1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("             AND E1.SEMESTER = '" + _param._eDateMaxSeme + "' ");
        stb.append("             AND E1.CHAIRCD = T1.CHAIRCD ");
        stb.append("             AND E1.SCHREGNO = '" + schregNo + "' ");
        stb.append("     ) ");
        stb.append(" ), STAFF_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._eDateMaxSeme + "' ");
        stb.append("     AND T1.CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_T) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     STAFF_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_T) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.STAFFCD ");

        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                retList.add(rs.getString("STAFFNAME"));
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retList;
    }

    private String getTitle(String printDiv) {
        String retTitle = "成績不振・欠時過多生徒一覧";
//        // 成績・欠課共に不振
//        if (ALL_OUT.equals(printDiv)) {
//            retTitle = "成績不振・欠時過多生徒一覧";
//        }
//
//        // 成績不振
//        if (SCORE_OUT.equals(printDiv)) {
//            retTitle = "成績不振生徒一覧";
//        }
//
//        // 欠課不振
//        if (ATTEND_OUT.equals(printDiv)) {
//            retTitle = "欠時過多生徒一覧";
//        }

        return retTitle;
    }

    private String getSlumpDiv(String printDiv) {
        String retSlump = "";
        // 成績・欠課共に不振
        if (ALL_OUT.equals(printDiv)) {
            retSlump = "成/" + getSyukketuHusinDiv();
        }

        // 成績不振
        if (SCORE_OUT.equals(printDiv)) {
            retSlump = "成";
        }

        // 欠課不振
        if (ATTEND_OUT.equals(printDiv)) {
            retSlump = getSyukketuHusinDiv();
        }

        return retSlump;
    }

    private String getSyukketuHusinDiv() {
        return (JOUGEN_RISYU.equals(_param._jougenDiv) ? "履" : "修") + (JOUGEN_OVER.equals(_param._overDiv) ? "超" : "注");
    }

    private boolean outPutCheck(final String printDiv, final ScoreData scoreData, final Syukketu syukketu) {
        boolean retFlg = false;
        // 成績・欠課共に不振
        if (ALL_OUT.equals(printDiv)) {
            if (scoreData._isSlump && null != syukketu && syukketu._isOver) {
                retFlg = true;
            }
        }

        // 成績不振
        if (SCORE_OUT.equals(printDiv)) {
            if (scoreData._isSlump && (null == syukketu || !syukketu._isOver)) {
                retFlg = true;
            }
        }

        // 欠課不振
        if (ATTEND_OUT.equals(printDiv)) {
            if (!scoreData._isSlump && null != syukketu && syukketu._isOver) {
                retFlg = true;
            }
        }

        return retFlg;
    }

    private ScoreData getScoreData(final DB2UDB db2, final String subclassCd, final Student student) throws SQLException {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUB_M.SUBCLASSCD, ");
        stb.append("     SUB_M.SUBCLASSNAME, ");
        stb.append("     CRE.CREDITS ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_MST SUB_M ");
        stb.append("     LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND CRE.COURSECD = '" + student._courseCd + "' ");
        stb.append("          AND CRE.MAJORCD = '" + student._majorCd + "' ");
        stb.append("          AND CRE.GRADE = '" + student._grade + "' ");
        stb.append("          AND CRE.COURSECODE = '" + student._courseCode + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND SUB_M.CLASSCD = CRE.CLASSCD ");
            stb.append("          AND SUB_M.SCHOOL_KIND = CRE.SCHOOL_KIND ");
            stb.append("          AND SUB_M.CURRICULUM_CD = CRE.CURRICULUM_CD ");
        } else {
            stb.append("          AND substr(SUB_M.SUBCLASSCD, 1, 2) = CRE.CLASSCD ");
        }
        stb.append("          AND SUB_M.SUBCLASSCD = CRE.SUBCLASSCD ");
        stb.append(" WHERE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUB_M.SUBCLASSCD = '" + subclassCd + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUB_M.SUBCLASSCD ");


        ScoreData scoreData = new ScoreData(subclassCd, "", "", "", false);
        PreparedStatement psKamoku = null;
        ResultSet rsKamoku = null;
        try {
            psKamoku = db2.prepareStatement(stb.toString());
            rsKamoku = psKamoku.executeQuery();
            while (rsKamoku.next()) {

                final String name = rsKamoku.getString("SUBCLASSNAME");
                final String credit = rsKamoku.getString("CREDITS");

                scoreData = new ScoreData(subclassCd, name, credit, "", false);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psKamoku, rsKamoku);
        }
        return scoreData;
    }

    private List getPrintGradeData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        GradeData gradeData = new GradeData(_param._grade);
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String schregNo = rsStudent.getString("SCHREGNO");
                final String name = rsStudent.getString("NAME");
                final String grade = rsStudent.getString("GRADE");
                final String gradeName = rsStudent.getString("GRADE_NAME");
                final String hrClass = rsStudent.getString("HR_CLASS");
                final String HrName = null == rsStudent.getString("HR_NAME") ? "" : rsStudent.getString("HR_NAME");
                final int attendNo = null == rsStudent.getString("ATTENDNO") ? 0 : rsStudent.getInt("ATTENDNO");
                final String courseCd = rsStudent.getString("COURSECD");
                final String majorCd = rsStudent.getString("MAJORCD");
                final String courseCode = rsStudent.getString("COURSECODE");

                final Student student = new Student(schregNo, name, hrClass, attendNo, HrName, grade, gradeName, courseCd, majorCd, courseCode);

                gradeData._studentList.add(student);
            }

            gradeData.setSyukketu(db2);
            gradeData.setPrintData(db2);
            retList.add(gradeData);
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }

        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     REGD_D.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGD_D.GRADE, ");
        stb.append("     REGD_G.GRADE_NAME1 AS GRADE_NAME, ");
        stb.append("     REGD_D.HR_CLASS, ");
        stb.append("     REGD_H.HR_NAME, ");
        stb.append("     REGD_D.ATTENDNO, ");
        stb.append("     REGD_D.COURSECD, ");
        stb.append("     REGD_D.MAJORCD, ");
        stb.append("     REGD_D.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD_D ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD_D.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_D.YEAR = REGD_H.YEAR ");
        stb.append("          AND REGD_D.SEMESTER = REGD_H.SEMESTER ");
        stb.append("          AND REGD_D.GRADE = REGD_H.GRADE ");
        stb.append("          AND REGD_D.HR_CLASS = REGD_H.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD_D.YEAR = REGD_G.YEAR ");
        stb.append("          AND REGD_D.GRADE = REGD_G.GRADE ");
        stb.append(" WHERE ");
        stb.append("     REGD_D.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD_D.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("     AND REGD_D.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     REGD_D.GRADE, ");
        stb.append("     REGD_D.HR_CLASS, ");
        stb.append("     REGD_D.ATTENDNO ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _hrClass;
        private final String _attendNo;
        private final String _hrName;
        private final String _grade;
        private final String _gradeName;
        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final Map _printData;
        private final Map _syukketuMap;

        public Student(
                final String schregNo,
                final String name,
                final String hrClass,
                final int attendNo,
                final String hrName,
                final String grade,
                final String gradeName,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            _schregNo = schregNo;
            _name = name;
            _hrClass = hrClass;
            _attendNo = attendNo < 10 ? "  " + attendNo : attendNo < 100 ? " " + attendNo : String.valueOf(attendNo);
            _hrName = hrName;
            _grade = grade;
            _gradeName = gradeName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _printData = new HashMap();
            _syukketuMap = new HashMap();
        }

        public Set getSubclassCds(final boolean isDate, final boolean isSyukketuMap) {
            final Set set = new TreeSet();
            if (isDate) {
                set.addAll(_printData.keySet());
            }
            if (isSyukketuMap) {
                set.addAll(_syukketuMap.keySet());
            }
            return set;
        }

    }

    private class GradeData {
        private final String _grade;
        private final List _studentList;

        public GradeData(final String grade) {
            _grade = grade;
            _studentList = new ArrayList();
        }

        private Student getStudent(final String schregNo) {
            if (null != schregNo) {
                for (Iterator it = _studentList.iterator(); it.hasNext();) {
                    Student student = (Student) it.next();
                    if (schregNo.equals(student._schregNo)) {
                        return student;
                    }
                }
            }
            return null;
        }

        private void setSyukketu(final DB2UDB db2) {

            final String absenceSql = getAbsenceSql();
            PreparedStatement psAbsence = null;
            ResultSet rsAbsence = null;

            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;
            try {
                psAbsence = db2.prepareStatement(absenceSql);

                _param._attendParamMap.put("grade", _grade);
                sql = AttendAccumulate.getAttendSubclassSql(
                        _param._ctrlYear,
                        _param._eDateMaxSeme,
                        _param._sDate,
                        _param._eDate,
                        _param._attendParamMap
                );

                // log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }

                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclassCd = rs.getString("SUBCLASSCD");

                    final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                    final BigDecimal ketsuji = rs.getBigDecimal("SICK1");
                    final BigDecimal sick = rs.getBigDecimal("SICK2");
                    final BigDecimal absent = rs.getBigDecimal("ABSENT");
                    final BigDecimal suspend = rs.getBigDecimal("SUSPEND").add(rs.getBigDecimal("VIRUS")).add(rs.getBigDecimal("KOUDOME"));
                    final BigDecimal mourning = rs.getBigDecimal("MOURNING");
                    final BigDecimal late = "1".equals(_param._chikokuHyoujiFlg) ? rs.getBigDecimal("LATE") : rs.getBigDecimal("LATE2");
                    final BigDecimal early = "1".equals(_param._chikokuHyoujiFlg) ? rs.getBigDecimal("EARLY") : rs.getBigDecimal("EARLY2");
                    final BigDecimal replacedAbsence = rs.getBigDecimal("REPLACED_SICK");

                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    String rawSubclassCd = subclassCd;
                    if ("1".equals(_param._useCurriculumcd) && StringUtils.split(subclassCd, "-").length == 4) {
                        String[] arr = StringUtils.split(subclassCd, "-");
                        classcd = arr[0];
                        schoolKind = arr[1];
                        curriculumCd = arr[2];
                        rawSubclassCd = arr[3];
                    }

                    int setI = 1;
                    psAbsence.setString(setI++, student._courseCd);
                    psAbsence.setString(setI++, student._majorCd);
                    psAbsence.setString(setI++, student._grade);
                    psAbsence.setString(setI++, student._courseCode);
                    if (_param._schoolMst.isHoutei()) {
                        if ("1".equals(_param._useCurriculumcd)) {
                            psAbsence.setString(setI++, classcd);
                            psAbsence.setString(setI++, schoolKind);
                            psAbsence.setString(setI++, curriculumCd);
                            psAbsence.setString(setI++, rawSubclassCd);
                        } else {
                            psAbsence.setString(setI++, subclassCd.substring(0, 2));
                            psAbsence.setString(setI++, subclassCd);
                        }
                    } else {
                        if ("1".equals(_param._useCurriculumcd)) {
                            psAbsence.setString(setI++, classcd);
                            psAbsence.setString(setI++, schoolKind);
                            psAbsence.setString(setI++, curriculumCd);
                            psAbsence.setString(setI++, rawSubclassCd);
                        } else {
                            psAbsence.setString(setI++, subclassCd);
                        }
                        psAbsence.setString(setI++, student._schregNo);
                    }
                    rsAbsence = psAbsence.executeQuery();
                    BigDecimal checkAbsence = null;
                    BigDecimal checkGetAbsence = null;
                    BigDecimal shutokuWarn = null;
                    BigDecimal rishuWarn = null;
                    while (rsAbsence.next()) {
                        checkAbsence = rsAbsence.getBigDecimal("ABSENCE_HIGH");
                        checkGetAbsence = rsAbsence.getBigDecimal("GET_ABSENCE_HIGH");
                        shutokuWarn = rsAbsence.getBigDecimal("SHUTOKU_WARN");
                        rishuWarn = rsAbsence.getBigDecimal("RISHU_WARN");
                    }
                    boolean overFlg = false;

                    if (JOUGEN_RISYU.equals(_param._jougenDiv)) {
                        if (JOUGEN_WARN.equals(_param._overDiv)) {
                            if (null != rishuWarn &&  0 < rishuWarn.intValue() && rishuWarn.compareTo(sick) < 0) {
                                overFlg = true;
                            }
                        } else {
                            if (null != checkAbsence && 0 < checkAbsence.intValue() && checkAbsence.compareTo(sick) < 0) {
                                overFlg = true;
                            }
                        }
                    } else {
                        if (JOUGEN_WARN.equals(_param._overDiv)) {
                            if (null != shutokuWarn && 0 < shutokuWarn.intValue() && shutokuWarn.compareTo(sick) < 0) {
                                overFlg = true;
                            }
                        } else {
                            if (null != checkGetAbsence && 0 < checkGetAbsence.intValue()  && checkGetAbsence.compareTo(sick) < 0) {
                                overFlg = true;
                            }
                        }
                    }
                    final Syukketu syukketu = new Syukketu(subclassCd, lesson, ketsuji, sick, absent, suspend, mourning, late, early, replacedAbsence, overFlg);
                    student._syukketuMap.put(subclassCd, syukketu);
                }
            } catch (SQLException e) {
                log.debug("sql exception! sql = " + sql, e);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getAbsenceSql() {
            final StringBuffer stb = new StringBuffer();

            if (_param._schoolMst.isHoutei()) {
                stb.append(" SELECT ");
                stb.append("     T1.ABSENCE_HIGH AS ABSENCE_HIGH, ");
                stb.append("     T1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH, ");
                stb.append("     T1.ABSENCE_HIGH - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + _param.getSemester() + ", 0) AS RISHU_WARN, ");
                stb.append("     T1.GET_ABSENCE_HIGH - VALUE(T1.ABSENCE_WARN_SHUTOKU_SEM" + _param.getSemester() + ", 0) AS  SHUTOKU_WARN");
                stb.append(" FROM ");
                stb.append("     V_CREDIT_MST T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND T1.COURSECD = ? ");
                stb.append("     AND T1.MAJORCD = ? ");
                stb.append("     AND T1.GRADE = ? ");
                stb.append("     AND T1.COURSECODE = ? ");
                stb.append("     AND T1.CLASSCD = ? ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     AND T1.SCHOOL_KIND = ? ");
                    stb.append("     AND T1.CURRICULUM_CD = ? ");
                }
                stb.append("     AND T1.SUBCLASSCD = ? ");
            } else {
                stb.append(" SELECT ");
                stb.append("     T1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH, ");
                stb.append("     T1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH, ");
                stb.append("     T1.COMP_ABSENCE_HIGH - VALUE(L1.ABSENCE_WARN_RISHU_SEM" + _param.getSemester() + ", 0) AS RISHU_WARN, ");
                stb.append("     T1.GET_ABSENCE_HIGH - VALUE(L1.ABSENCE_WARN_SHUTOKU_SEM" + _param.getSemester() + ", 0) AS SHUTOKU_WARN");
                stb.append(" FROM ");
                stb.append("     SCHREG_ABSENCE_HIGH_DAT T1 ");
                stb.append("     LEFT JOIN V_CREDIT_MST L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND L1.COURSECD = ? ");
                stb.append("          AND L1.MAJORCD = ? ");
                stb.append("          AND L1.GRADE = ? ");
                stb.append("          AND L1.COURSECODE = ? ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND L1.CLASSCD = T1.CLASSCD ");
                    stb.append("          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                } else {
                    stb.append("          AND L1.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
                }
                stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND T1.DIV = '2' ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     AND T1.CLASSCD = ? ");
                    stb.append("     AND T1.SCHOOL_KIND = ? ");
                    stb.append("     AND T1.CURRICULUM_CD = ? ");
                }
                stb.append("     AND T1.SUBCLASSCD = ? ");
                stb.append("     AND T1.SCHREGNO = ? ");
            }

            return stb.toString();
        }

        private void setPrintData(final DB2UDB db2) throws SQLException {

            final String kamokuSql = getKamokuSql();

            PreparedStatement psKamoku = null;
            ResultSet rsKamoku = null;
            try {
                psKamoku = db2.prepareStatement(kamokuSql);
                rsKamoku = psKamoku.executeQuery();
                while (rsKamoku.next()) {
                    Student student = getStudent(rsKamoku.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }

                    final String subclassCd = rsKamoku.getString("SUBCLASSCD");
                    final String name = rsKamoku.getString("SUBCLASSNAME");
                    final String credit = rsKamoku.getString("CREDITS");
                    final String score = rsKamoku.getString("SCORE");
                    final boolean isSlump = "1".equals(rsKamoku.getString("SLUMP_FLG")) ? true : false;

                    final ScoreData scoreData = new ScoreData(subclassCd, name, credit, score, isSlump);
                    student._printData.put(subclassCd, scoreData);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psKamoku, rsKamoku);
            }
        }

        private String getKamokuSql() {
            final StringBuffer stb = new StringBuffer();

            if ("1".equals(_param._checkKettenDiv)) {
                stb.append(" WITH MAIN_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T2.GRADE, ");
                stb.append("     T2.COURSECD, ");
                stb.append("     T2.MAJORCD, ");
                stb.append("     T2.COURSECODE ");
                stb.append(" FROM ");
                stb.append("     RECORD_SLUMP_DAT T1");
                stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.SEMESTER = '" + _param.getSemester() + "' ");
                stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.GRADE = '" + _grade + "' ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testCd + "' ");
                stb.append("     AND T1.SLUMP = '1' ");
            } else if ("2".equals(_param._checkKettenDiv)) {
                stb.append(" WITH PERFECT_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     YEAR, ");
                stb.append("     SEMESTER, ");
                stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
                stb.append("     CLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     SUBCLASSCD, ");
                stb.append("     MIN(DIV) AS DIV ");
                stb.append(" FROM ");
                stb.append("     PERFECT_RECORD_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testCd + "' ");
                stb.append(" GROUP BY ");
                stb.append("     YEAR, ");
                stb.append("     SEMESTER, ");
                stb.append("     TESTKINDCD || TESTITEMCD, ");
                stb.append("     CLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     SUBCLASSCD ");
                stb.append(" ), PERFECT_MAIN AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.* ");
                stb.append(" FROM ");
                stb.append("     PERFECT_RECORD_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     EXISTS( ");
                stb.append("         SELECT ");
                stb.append("             'x' ");
                stb.append("         FROM ");
                stb.append("             PERFECT_T E1 ");
                stb.append("         WHERE ");
                stb.append("             E1.YEAR = T1.YEAR ");
                stb.append("             AND E1.SEMESTER = T1.SEMESTER ");
                stb.append("             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ");
                stb.append("             AND E1.CLASSCD = T1.CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("             AND E1.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("             AND E1.DIV = T1.DIV ");
                stb.append("     ) ");
                stb.append(" ), SCH_PERFECT AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     L1.SEMESTER, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.GRADE, ");
                stb.append("     T1.HR_CLASS, ");
                stb.append("     T1.COURSECD, ");
                stb.append("     T1.MAJORCD, ");
                stb.append("     T1.COURSECODE, ");
                stb.append("     L1.TESTKINDCD, ");
                stb.append("     L1.TESTITEMCD, ");
                stb.append("     L1.CLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     L1.SCHOOL_KIND, ");
                    stb.append("     L1.CURRICULUM_CD, ");
                }
                stb.append("     L1.SUBCLASSCD, ");
                stb.append("     L1.PASS_SCORE ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT T1 ");
                stb.append("     LEFT JOIN PERFECT_MAIN L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND L1.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T1.GRADE END ");
                stb.append("          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND T1.SEMESTER = '" + _param.getSemester() + "' ");
                stb.append("     AND T1.GRADE = '" + _grade + "' ");
                stb.append(" ), MAIN_T AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     L1.GRADE, ");
                stb.append("     L1.COURSECD, ");
                stb.append("     L1.MAJORCD, ");
                stb.append("     L1.COURSECODE ");
                stb.append(" FROM ");
                stb.append("     " + _param.getUseRecordTableName() + " T1 ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.SEMESTER = '" + _param.getSemester() + "' ");
                stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.GRADE = '" + _grade + "' ");
                stb.append("     LEFT JOIN SCH_PERFECT L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
                stb.append("          AND T1.TESTKINDCD || T1.TESTITEMCD = L1.TESTKINDCD || L1.TESTITEMCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
                    stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
                    stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                }
                stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testCd + "' ");
                stb.append("     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ");
                stb.append("     AND T1.SCORE < VALUE(L1.PASS_SCORE, -1) ");
            } else {
                stb.append(" WITH MAIN_T AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T2.GRADE, ");
                stb.append("     T2.COURSECD, ");
                stb.append("     T2.MAJORCD, ");
                stb.append("     T2.COURSECODE ");
                stb.append(" FROM ");
                stb.append("     " + _param.getUseRecordTableName() + " T1 ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.SEMESTER = '" + _param.getSemester() + "' ");
                stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.GRADE = '" + _grade + "' ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testCd + "' ");
                stb.append("     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ");
                stb.append("     AND T1.SCORE < " + _param._ketten + " ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     SUB_M.SUBCLASSNAME, ");
            stb.append("     CRE.CREDITS, ");
            stb.append("     CASE WHEN MAIN_T.SCHREGNO IS NOT NULL ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '0' ");
            stb.append("     END AS SLUMP_FLG ");
            stb.append(" FROM ");
            stb.append("     " + _param.getUseRecordTableName() + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = '" + _param.getSemester() + "' ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.GRADE = '" + _grade + "' ");
            stb.append("     LEFT JOIN MAIN_T ON MAIN_T.YEAR = T1.YEAR ");
            stb.append("          AND MAIN_T.SEMESTER = T1.SEMESTER ");
            stb.append("          AND MAIN_T.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND MAIN_T.TESTITEMCD = T1.TESTITEMCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND MAIN_T.CLASSCD = T1.CLASSCD ");
                stb.append("          AND MAIN_T.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND MAIN_T.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("          AND MAIN_T.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND MAIN_T.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUB_M ON T1.SUBCLASSCD = SUB_M.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND SUB_M.CLASSCD = T1.CLASSCD ");
                stb.append("          AND SUB_M.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND SUB_M.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
            stb.append("          AND CRE.COURSECD = T2.COURSECD ");
            stb.append("          AND CRE.MAJORCD = T2.MAJORCD ");
            stb.append("          AND CRE.GRADE = T2.GRADE ");
            stb.append("          AND CRE.COURSECODE = T2.COURSECODE ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND CRE.CLASSCD = T1.CLASSCD ");
                stb.append("          AND CRE.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND CRE.CURRICULUM_CD = T1.CURRICULUM_CD ");
            } else {
                stb.append("          AND CRE.CLASSCD = substr(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("          AND CRE.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testCd + "' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ");
            if ("1".equals(_param._checkKettenDiv)) {
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("     MAIN_T.SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     MAIN_T.CLASSCD || '-' || MAIN_T.SCHOOL_KIND || '-' || MAIN_T.CURRICULUM_CD || '-' || ");
                }
                stb.append("     MAIN_T.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T1.SCORE, ");
                stb.append("     SUB_M.SUBCLASSNAME, ");
                stb.append("     CRE.CREDITS, ");
                stb.append("     '1' AS SLUMP_FLG ");
                stb.append(" FROM ");
                stb.append("     MAIN_T ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = MAIN_T.YEAR ");
                stb.append("         AND T2.SEMESTER = '" + _param.getSemester() + "' ");
                stb.append("         AND T2.SCHREGNO = MAIN_T.SCHREGNO ");
                stb.append("         AND T2.GRADE = '" + _grade + "' ");
                stb.append("     LEFT JOIN " + _param.getUseRecordTableName() + " T1 ON MAIN_T.YEAR = T1.YEAR ");
                stb.append("          AND MAIN_T.SEMESTER = T1.SEMESTER ");
                stb.append("          AND MAIN_T.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("          AND MAIN_T.TESTITEMCD = T1.TESTITEMCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND MAIN_T.CLASSCD = T1.CLASSCD ");
                    stb.append("          AND MAIN_T.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("          AND MAIN_T.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("          AND MAIN_T.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("          AND MAIN_T.SCHREGNO = T1.SCHREGNO ");
                stb.append("     LEFT JOIN SUBCLASS_MST SUB_M ON MAIN_T.SUBCLASSCD = SUB_M.SUBCLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND MAIN_T.CLASSCD = SUB_M.CLASSCD ");
                    stb.append("          AND MAIN_T.SCHOOL_KIND = SUB_M.SCHOOL_KIND ");
                    stb.append("          AND MAIN_T.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
                }
                stb.append("     LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = MAIN_T.YEAR ");
                stb.append("          AND CRE.COURSECD = T2.COURSECD ");
                stb.append("          AND CRE.MAJORCD = T2.MAJORCD ");
                stb.append("          AND CRE.GRADE = T2.GRADE ");
                stb.append("          AND CRE.COURSECODE = T2.COURSECODE ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND CRE.CLASSCD = MAIN_T.CLASSCD ");
                    stb.append("          AND CRE.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ");
                    stb.append("          AND CRE.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ");
                } else {
                    stb.append("          AND CRE.CLASSCD = substr(MAIN_T.SUBCLASSCD, 1, 2) ");
                }
                stb.append("          AND CRE.SUBCLASSCD = MAIN_T.SUBCLASSCD ");
            }
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }
    }

    private class Syukketu {
        private final String _subclassCd;
        private BigDecimal _lesson;
        private BigDecimal _ketsuji;
        private BigDecimal _sick;
        private BigDecimal _absent;
        private BigDecimal _suspend;
        private BigDecimal _mourning;
        private BigDecimal _late;
        private BigDecimal _early;
        private BigDecimal _replacedAbsence;
        private boolean _isOver;

        public Syukketu(
                final String subclassCd,
                final BigDecimal lesson,
                final BigDecimal ketsuji,
                final BigDecimal sick,
                final BigDecimal absent,
                final BigDecimal suspend,
                final BigDecimal mourning,
                final BigDecimal late,
                final BigDecimal early,
                final BigDecimal replacedAbsence,
                final boolean overFlg
        ) {
            _subclassCd = subclassCd;
            _lesson = lesson;
            _ketsuji = ketsuji;
            _sick = sick;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
            _late = late;
            _early = early;
            _replacedAbsence = replacedAbsence;
            _isOver = overFlg;
        }
    }

    private class ScoreData {
        private final String _subclassCd;
        private final String _name;
        private final String _credit;
        private final String _score;
        private final boolean _isSlump;

        public ScoreData(
                final String subclassCd,
                final String name,
                final String credit,
                final String score,
                final boolean slumpFlg
        ) {
            _subclassCd = subclassCd;
            _name = name;
            _credit = credit;
            _score = score;
            _isSlump = slumpFlg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56807 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _semesterName;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _grade;
        final String _checkKettenDiv;
        final String _chikokuHyoujiFlg;
        final String _ketten;
        final String _sotenHyouka;
        final String _jougenDiv;
        final String _overDiv;
        final String _testCd;
        final String _testName;
        final String _dateDiv;
        final String _sDate;
        final String _eDate;
        final String _eDateMaxSeme;
        final String _cmd;

        private final KNJSchoolMst _schoolMst;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, ParseException {
            _semester = request.getParameter("SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _sotenHyouka = request.getParameter("SOTEN_HYOUKA");
            _jougenDiv = request.getParameter("JOUGEN_DIV");
            _overDiv = request.getParameter("OVER_DIV");
            _testCd = request.getParameter("TEST_CD");
            _dateDiv = request.getParameter("DATE_DIV");
            _cmd = request.getParameter("cmd");
            _sDate = request.getParameter("SDATE").replace('/', '-');
            _eDate = request.getParameter("EDATE").replace('/', '-');

            _semesterName = getSemesterName(db2, _ctrlYear, _semester);
            _eDateMaxSeme = getEdateMaxSemester(db2, _ctrlYear, _eDate);
            _testName = getTestName(db2, _semester);
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            try {
                _schoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } finally {
                db2.commit();
            }

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) throws SQLException {
            final String semeNameSql = "SELECT SEMESTERNAME FROM SEMESTER_MST "
                                     + " WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ";
            String semeName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(semeNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    semeName = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.close(rs);
                db2.commit();
            }
            return semeName;
        }

        private String getEdateMaxSemester(final DB2UDB db2, final String year, final String eDate) throws SQLException {
            final String maxSemeSql = "SELECT MAX(SEMESTER) AS SEMESTER FROM SEMESTER_MST "
                                     + " WHERE YEAR = '" + year + "' AND SEMESTER < '9' AND SDATE <= '" + eDate + "' ";
            String maxSeme = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(maxSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    maxSeme = rs.getString("SEMESTER");
                }
            } finally {
                DbUtils.close(rs);
                db2.commit();
            }
            return maxSeme;
        }

        private String getTestName(final DB2UDB db2, final String semester) throws SQLException {
            final String testNameSql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW "
                                     + " WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD = '" + _testCd + "'";
            String testName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(testNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    testName = rs.getString("TESTITEMNAME");
                }
            } finally {
                DbUtils.close(rs);
                db2.commit();
            }
            return testName;
        }

        private String getSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getUseRecordTableName() {
            String tableName = "RECORD_RANK_DAT";
            if (("9900".equals(_testCd) && SOTEN.equals(_sotenHyouka)) ||
                (!"9900".equals(_testCd) && HYOUKA.equals(_sotenHyouka))
            ) {
                tableName = "RECORD_RANK_V_DAT";
            }
            return tableName;
        }
    }
}

// eof
