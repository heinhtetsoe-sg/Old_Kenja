// kanji=漢字
/*
 * $Id: 8970a49ece3ea76cbd24d29a9f56066606323781 $
 *
 * 作成日: 2007/05/30 13:09:29 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 期末考査順位一覧
 * @author m-yama
 * @version $Id: 8970a49ece3ea76cbd24d29a9f56066606323781 $
 */
public class KNJD622 {

    private static final Log log = LogFactory.getLog(KNJD622.class);

    private static final String FORM_FILE1 = "KNJD622_1.frm";
    private static final String FORM_FILE2 = "KNJD622_2.frm";

    private static final int MAXLINE = 25;
    private static final String TUUJOU_CHAIRCD = "0000000";

    Param _param;

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            boolean hasData = false;
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
        }
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
        log.debug("学校種別=" + param._schooldiv + " テスト=" + param._testname);
        for (int i = 0; i < param._courseName.length; i++) {
            log.debug("コースコード=" + param._courseCd[i] + "  コース名=" + param._courseName[i]);
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gengou;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlSemester;
        private final String _grade;
        private final String _testcd;
        private final String _schooldiv;
        private final String[] _courseCd;
        private final String[] _courseName;
        private final String _testname;
        private final String _date;
        private final String _namecd2;
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _gengou = gengou + "年度";
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _schooldiv = (3 >= Integer.parseInt(_grade)) ? "J" : "H";
            _courseCd = request.getParameterValues("CATEGORY_SELECTED");
            _namecd2 = (_schooldiv.equals("J")) ? "D003" : "D002";
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            _date = stb.toString();

            // DBより取得
            _courseName = getCourseName(db2, _courseCd);
            _semesterName = getSemesterName(db2, _year, _semester);
            _testname = getTestName(db2, _year, _semester, _testcd);
        }

        private String[] getCourseName(
                final DB2UDB db2,
                final String[] courseCd
        ) throws Exception {
            final String[] rtnVal = new String[courseCd.length];

            for (int i = 0; i < courseCd.length; i++) {
                final String sql = "SELECT "
                                 + "    T1.MAJORNAME || T2.COURSECODENAME AS NAME "
                                 + "FROM "
                                 + "    MAJOR_MST T1, "
                                 + "    COURSECODE_MST T2 "
                                 + "WHERE "
                                 + "    T1.COURSECD || T1.MAJORCD || T2.COURSECODE = '" + courseCd[i] + "' ";

                db2.query(sql);
                final ResultSet rs = db2.getResultSet();
                try {
                    rs.next();
                    rtnVal[i] = rs.getString("NAME");
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            }
            return rtnVal;
        }
    }

    private String getSemesterName(
            final DB2UDB db2,
            final String year,
            final String semester
    ) throws Exception {
        String rtnVal = "";

        if ("9".equals(semester)) {
            return rtnVal;
        }

        final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            rs.next();
            rtnVal = rs.getString("SEMESTERNAME");
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnVal;
    }

    private String getTestName(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String testcd
    ) throws Exception {
        String rtnVal = "";
        final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + year + "'"
            + " AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD = '" + testcd + "'";

        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            rs.next();
            rtnVal = rs.getString("TESTITEMNAME");
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnVal;
    }

    /** 印刷処理メイン */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        final Map coursesData = new HashMap();
        for (int i = 0; i < _param._courseCd.length; i++) {
            coursesData.put(_param._courseCd[i], new Course(db2, _param._courseCd[i], _param._courseName[i]));
        }

        final boolean rtnflg = printOut(svf, coursesData);
        return rtnflg;
    }

    /** コースクラス */
    private class Course {

        private final String _cd;
        private final String _name;
        private final List _subclasses;
        private final List _avgData;
        private final List _studens = new ArrayList();

        Course(final DB2UDB db2, final String courseCd, final String courseName) throws Exception {
            _cd = courseCd;
            _name = courseName;
            _subclasses = new ArrayList(Score.load(db2, _param, courseCd, "TITLE"));
            _avgData = new ArrayList(Score.load(db2, _param, courseCd, "AVG"));

            PreparedStatement ps = null;
            final String studentsSql = getStudentInfoSql(courseCd);
            ps = db2.prepareStatement(studentsSql);
            ResultSet stRs = null;

            try {
                stRs = ps.executeQuery();
                while (stRs.next()) {
                    _studens.add(new Student(db2,
                                             stRs.getString("SCHREGNO"),
                                             stRs.getString("NAME"),
                                             stRs.getString("HR_NAMEABBV"),
                                             stRs.getString("ATTENDNO")));
                }
            } finally {
                DbUtils.closeQuietly(stRs);
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String getStudentInfoSql(final String courseCd) {
            final String sql;
            sql = "SELECT "
                + "    T1.SCHREGNO, "
                + "    L1.NAME, "
                + "    L2.HR_NAMEABBV, "
                + "    T1.ATTENDNO "
                + "FROM "
                + "    SCHREG_REGD_DAT T1 "
                + "    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO "
                + "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR "
                + "         AND L2.SEMESTER = T1.SEMESTER "
                + "         AND L2.GRADE = T1.GRADE "
                + "         AND L2.HR_CLASS = T1.HR_CLASS "
                + "WHERE "
                + "    T1.YEAR = '" + _param._year + "' "
                + "    AND T1.SEMESTER = '" + _param._semester + "' "
                + "    AND T1.GRADE = '" + _param._grade + "' "
                + "    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + courseCd + "' "
                + "ORDER BY "
                + "    T1.GRADE, "
                + "    T1.HR_CLASS, "
                + "    T1.ATTENDNO ";

            return sql;
        }
    }

    /** 生徒クラス */
    private class Student {

        private final String _schregno;
        private final String _name;
        private final String _homeRoom;
        private final String _attendNo;
        private final List _scoreData;

        Student(
                final DB2UDB db2,
                final String schregno,
                final String name,
                final String homeRoom,
                final String attendno
        ) throws Exception {
            _schregno = schregno;
            _name = name;
            _homeRoom = homeRoom;
            _attendNo = attendno;
            _scoreData = new ArrayList(Score.load(db2, _param, schregno, "SCORE"));
        }

        public String toString() {
            return "学籍番号：" + _schregno + " 氏名：" + _name + " クラス：" + _homeRoom + " 番号：" + _attendNo;
        }
    }

    /** 得点クラス */
    private static class Score {
        private static final List SCORES = new ArrayList();
        private final String _classCd;
        private final String _className;
        private final String _chairCd;
        private final String _subClassCd;
        private final String _subClassName;
        private final String _score;
        private final String _absence;
        Score(
                final String classCd,
                final String className,
                final String chairCd,
                final String subClassCd,
                final String subClassName,
                final String score,
                final String absence
        ) {
            _classCd = classCd;
            _className = className;
            _chairCd = chairCd;
            _subClassCd = subClassCd;
            _subClassName = subClassName;
            _score = score;
            _absence = absence;
        }

        public static List load(
                final DB2UDB db2,
                final Param param,
                final String fieldVal,
                final String loadDiv
        ) throws Exception {
            SCORES.clear();

            final String sql = getSql(param, fieldVal, loadDiv);

            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    String value;
                    if (loadDiv.equals("AVG") && null != rs.getString("VALUE")) {
                        value = String.valueOf(KNJServletUtils.roundHalfUp(rs.getDouble("VALUE"), 1));
                    } else {
                        value = rs.getString("VALUE");
                    }
                    final Score score = new Score(
                            rs.getString("CLASSCD"),
                            rs.getString("CLASSABBV"),
                            rs.getString("CHAIRCD"),
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSABBV"),
                            value,
                            rs.getString("ABSENCE")
                    );
                    SCORES.add(score);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }

            return SCORES;
        }

        private static String getSql(final Param param, final String fieldVal, final String loadDiv) {
            final String sql;
            if (loadDiv.equals("SCORE")) {
                sql = getScoreSql(fieldVal, param, loadDiv);
            } else if (loadDiv.equals("AVG")) {
                sql = getAvgSql(fieldVal, param, loadDiv);
            } else {
                sql = getTitle(param, fieldVal, loadDiv);
            }
            return sql;
        }

        // CSOFF: MethodLength
        private static String getScoreSql(final String schregno, final Param param, final String conditionDiv) {

            String sql;
            sql = getTitle(param, schregno, conditionDiv)
                + ", ABSENCE_T AS ( "
                + "SELECT DISTINCT "
                + "    T1.CHAIRCD, "
                + "    T2.SUBCLASSCD, "
                + "    '欠席' AS ABSENCE "
                + "FROM "
                + "    ATTEND_DAT T1, "
                + "    (SELECT DISTINCT CHAIRCD, SUBCLASSCD, EXECUTEDATE, PERIODCD FROM MAIN_T) T2 "
                + "WHERE "
                + "    T1.SCHREGNO = '" + schregno + "' "
                + "    AND T1.ATTENDDATE = T2.EXECUTEDATE "
                + "    AND T1.PERIODCD = T2.PERIODCD "
                + "    AND T1.CHAIRCD = T2.CHAIRCD "
                + "    AND T1.DI_CD IN ('1','2','3','4','5','6','8','9','10','11','12','13','14','19','20','25','26') "
                + "), SELECT_T AS ( "
                + "SELECT "
                + "    T1.CHAIRCD, "
                + "    T1.CLASSCD, "
                + "    T1.CLASSABBV, "
                + "    T1.SUBCLASSCD, "
                + "    T1.SUBCLASSABBV, "
                + "    CASE WHEN L4.NAMECD2 IS NULL "
                + "         THEN L1.COURSE_RANK "
                + "         ELSE L3.COURSE_RANK "
                + "    END AS VALUE, "
                + "    L2.ABSENCE AS ABSENCE "
                + "FROM "
                + "    (SELECT DISTINCT "
                + "         CHAIRCD, "
                + "         CLASSCD, "
                + "         CLASSABBV, ";
            if ("1".equals(param._useCurriculumcd)) {
                sql +="         RAW_SUBCLASSCD, ";
            }
            sql +="         SUBCLASSCD, "
                + "         SUBCLASSABBV, "
                + "         VALUE, "
                + "         ABSENCE "
                + "     FROM "
                + "         MAIN_T "
                + "    ) T1 "
                + "    LEFT JOIN RECORD_RANK_DAT L1 ON L1.YEAR = '" + param._year + "'"
                + "         AND L1.SEMESTER = '" + param._semester + "' "
                + "         AND L1.TESTKINDCD || L1.TESTITEMCD = '" + param._testcd + "' "
                + "         AND L1.SCHREGNO = '" + schregno + "' ";
           sql += "         AND ";
           if ("1".equals(param._useCurriculumcd)) {
               sql += "             L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ";
           }
           sql += "             L1.SUBCLASSCD = T1.SUBCLASSCD "
                + "    LEFT JOIN ABSENCE_T L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD "
                + "         AND L2.CHAIRCD = T1.CHAIRCD "
                + "    LEFT JOIN RECORD_RANK_CHAIR_DAT L3 ON L3.YEAR = '" + param._year + "' "
                + "         AND L3.SEMESTER = '" + param._semester + "' "
                + "         AND L3.TESTKINDCD || L3.TESTITEMCD = '" + param._testcd + "' "
                + "         AND ";
           if ("1".equals(param._useCurriculumcd)) {
               sql += "             L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || ";
           }
           sql += "             L3.SUBCLASSCD = T1.SUBCLASSCD "
                + "         AND L3.CHAIRCD = T1.CHAIRCD "
                + "         AND L3.SCHREGNO = '" + schregno + "' "
                + "    LEFT JOIN V_NAME_MST L4 ON L4.YEAR = '" + param._year + "' "
                + "         AND L4.NAMECD1 = '" + param._namecd2 + "' "
                + "         AND L4.NAME1 = ";
           if ("1".equals(param._useCurriculumcd)) {
               sql += "         T1.RAW_SUBCLASSCD ";
           } else {
               sql += "         T1.SUBCLASSCD ";
           }
           sql += ") "
                + "SELECT "
                + "    CHAIRCD, "
                + "    CLASSCD, "
                + "    CLASSABBV, "
                + "    SUBCLASSCD, "
                + "    SUBCLASSABBV, "
                + "    VALUE, "
                + "    CASE WHEN VALUE IS NULL AND ABSENCE IS NULL "
                + "         THEN '　' "
                + "         ELSE CASE WHEN VALUE IS NULL "
                + "                   THEN ABSENCE "
                + "                   ELSE NULL "
                + "              END "
                + "    END AS ABSENCE "
                + "FROM "
                + "    SELECT_T "
                + "ORDER BY "
                + "    CLASSCD, "
                + "    SUBCLASSCD, "
                + "    CHAIRCD ";

            return sql;
        }
        // CSON: MethodLength

        private static String getAvgSql(final String courseCd, final Param param, final String conditionDiv) {
            String sql;
            sql = getTitle(param, courseCd, conditionDiv)
                + "SELECT "
                + "    T1.CHAIRCD, "
                + "    T1.CLASSCD, "
                + "    T1.CLASSABBV, "
                + "    T1.SUBCLASSCD, "
                + "    T1.SUBCLASSABBV, "
                + "    CASE WHEN L3.NAMECD2 IS NULL "
                + "         THEN L1.AVG "
                + "         ELSE L2.AVG "
                + "    END AS VALUE, "
                + "    '' AS ABSENCE "
                + "FROM "
                + "    MAIN_T T1 "
                + "    LEFT JOIN RECORD_AVERAGE_DAT L1 ON L1.YEAR = '" + param._year + "' "
                + "         AND L1.SEMESTER = '" + param._semester + "' "
                + "         AND L1.TESTKINDCD || L1.TESTITEMCD = '" + param._testcd + "' "
                + "         AND ";
            if ("1".equals(param._useCurriculumcd)) {
                sql += " L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ";
            }
            sql +="             L1.SUBCLASSCD = T1.SUBCLASSCD "
                + "         AND L1.AVG_DIV = '3' "
                + "         AND L1.GRADE = '" + param._grade + "' "
                + "         AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = '" + courseCd + "' "
                + "    LEFT JOIN RECORD_AVERAGE_CHAIR_DAT L2 ON L2.YEAR = '" + param._year + "' "
                + "         AND L2.SEMESTER = '" + param._semester + "' "
                + "         AND L2.TESTKINDCD || L2.TESTITEMCD = '" + param._testcd + "' "
                + "         AND ";
            if ("1".equals(param._useCurriculumcd)) {
                sql += " L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ";
            }
            sql +="             L2.SUBCLASSCD = T1.SUBCLASSCD "
                + "         AND L2.CHAIRCD = T1.CHAIRCD "
                + "         AND L2.AVG_DIV = '3' "
                + "         AND L2.GRADE = '" + param._grade + "' "
                + "         AND L2.COURSECD || L2.MAJORCD || L2.COURSECODE = '" + courseCd + "' "
                + "    LEFT JOIN V_NAME_MST L3 ON L3.YEAR = '" + param._year + "' "
                + "         AND L3.NAMECD1 = '" + param._namecd2 + "' "
                + "         AND L3.NAME1 = ";
            if ("1".equals(param._useCurriculumcd)) {
                sql += "         T1.RAW_SUBCLASSCD ";
            } else {
                sql += "         T1.SUBCLASSCD ";
            }
            sql +="ORDER BY "
                + "    T1.CLASSCD, "
                + "    T1.SUBCLASSCD, "
                + "    T1.CHAIRCD ";

            return sql;
        }

        // CSOFF: ExecutableStatementCount
        // CSOFF: MethodLength
        private static String getTitle(final Param param, final String condition, final String conditionDiv) {

            final String field = (conditionDiv.equals("SCORE")) ? "SCHREGNO" : "COURSECD || MAJORCD || COURSECODE";
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH SEM_T AS ( ");
            stb.append("SELECT ");
            stb.append("    SDATE, ");
            stb.append("    EDATE ");
            stb.append("FROM ");
            stb.append("    SEMESTER_MST ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + param._year + "' ");
            stb.append("    AND SEMESTER = '" + param._semester + "' ");
            stb.append("), SCH_T AS ( ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + param._year + "' ");
            stb.append("    AND SEMESTER = '" + param._semester + "' ");
            stb.append("    AND GRADE = '" + param._grade + "' ");
            stb.append("    AND " + field + " = '" + condition + "' ");
            stb.append("), CHR_T AS ( ");
            stb.append("SELECT DISTINCT ");
            stb.append("    T1.CHAIRCD ");
            stb.append("FROM ");
            stb.append("    CHAIR_STD_DAT T1 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.SCHREGNO IN (SELECT ");
            stb.append("                            W1.SCHREGNO ");
            stb.append("                        FROM ");
            stb.append("                            SCH_T W1 ");
            stb.append("                       ) ");
            if (!conditionDiv.equals("TITLE")) {
                stb.append("), MAIN_T AS ( ");
            } else {
                stb.append(") ");
            }
            stb.append("SELECT DISTINCT ");
            if (conditionDiv.equals("SCORE")) {
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.PERIODCD, ");
            }
            if (!conditionDiv.equals("TITLE")) {
                stb.append("    T1.CHAIRCD, ");
            } else {
                stb.append("    CASE WHEN L4.NAMECD2 IS NULL ");
                stb.append("    THEN '" + TUUJOU_CHAIRCD + "' ");
                stb.append("    ELSE T1.CHAIRCD END AS CHAIRCD, ");
            }
            stb.append("    L3.CLASSCD, ");
            stb.append("    L3.CLASSABBV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    L2.SUBCLASSCD AS RAW_SUBCLASSCD, ");
                stb.append("    L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
            }
            stb.append("    L2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    L2.SUBCLASSABBV, ");
            stb.append("    0 AS VALUE, ");
            stb.append("    '' AS ABSENCE ");
            stb.append("FROM ");
            stb.append("    SCH_CHR_TEST T1 ");
            stb.append("    LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = '" + param._year + "' ");
            stb.append("         AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L1.CHAIRCD = T1.CHAIRCD ");
            stb.append("    LEFT JOIN SUBCLASS_MST L2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
            }
            stb.append("         L2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         L1.SUBCLASSCD ");
            stb.append("    LEFT JOIN CLASS_MST L3 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" L3.CLASSCD || '-' || L3.SCHOOL_KIND =");
                stb.append(" L1.CLASSCD || '-' || L1.SCHOOL_KIND ");
            } else {
                stb.append("         L3.CLASSCD = SUBSTR(L1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("    LEFT JOIN V_NAME_MST L4 ON L4.YEAR = '" + param._year + "' ");
            stb.append("         AND L4.NAMECD1 = '" + param._namecd2 + "' ");
            stb.append("         AND L4.NAME1 = L1.SUBCLASSCD, ");
            stb.append("    SEM_T T2 ");
            stb.append("WHERE ");
            stb.append("    T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("    AND T1.CHAIRCD IN (SELECT ");
            stb.append("                           W1.CHAIRCD ");
            stb.append("                       FROM ");
            stb.append("                           CHR_T W1 ");
            stb.append("                      ) ");
            if (!conditionDiv.equals("TITLE")) {
                stb.append(") ");
            } else {
                stb.append("ORDER BY ");
                stb.append("    L3.CLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
                }
                stb.append("    L2.SUBCLASSCD, ");
                stb.append("    CHAIRCD ");
            }

            return stb.toString();
        }
        // CSON: MethodLength
        // CSON: ExecutableStatementCount

        public String toString() {
            return "講座：" + _chairCd
                    + " 教科：" + _classCd + "-" + _className
                    + " 科目：" + _subClassCd + "-" + _subClassName
                    + " 得点：" + _score
                    + " 欠課：" + _absence + "\n";
        }
    }

    private boolean printOut(final Vrw32alp svf, final Map courseData) {
        boolean rtnflg = false;

        for (int i = 0; i < _param._courseCd.length; i++) {
            final String courseCd = _param._courseCd[i];

            final Course course = (Course) courseData.get(courseCd);
            final List outputPageData = new ArrayList(getOutputPageData(course));

            rtnflg = dataPrint(svf, course, outputPageData) ? true : rtnflg;
        }

//        if (log.isDebugEnabled()) {
//            debugPrint(courseData);
//        }

        return rtnflg;
    }

    /**
     * ページ単位にデータを分割
     */
    private List getOutputPageData(final Course course) {
        final List rtnList = new ArrayList();
        final List lineData = new ArrayList();

        int i = 1;
        for (final Iterator it = course._studens.iterator(); it.hasNext();) {
            if (i > MAXLINE) {
                rtnList.add(new ArrayList(lineData));
                lineData.clear();
                i = 1;
            }

            final Student student = (Student) it.next();
            lineData.add(student);
            i++;
        }
        if (lineData.size() > 0) {
            rtnList.add(lineData);
        }
        return rtnList;
    }

    /**
     * データ印字
     */
    private boolean dataPrint(
            final Vrw32alp svf,
            final Course course,
            final List outputPageData
    ) {
        boolean rtnflg = false;
        String formFile = null;
        for (final Iterator it = outputPageData.iterator(); it.hasNext();) {
            final List printData = (List) it.next();

            if (isOneForm()) {
                formFile = FORM_FILE1;
            } else {
                formFile = (printData.size() < MAXLINE) ? FORM_FILE2 : FORM_FILE1;
            }

            setHead(svf, formFile, course._name);

            for (final Iterator itTitle = course._subclasses.iterator(); itTitle.hasNext();) {
                final Score title = (Score) itTitle.next();
                printScore(svf, formFile, course, title, printData);
                rtnflg = true;
            }
        }

        // 最終データが25行の場合、空頁を出力する。
        if (!isOneForm()) {
            if (!formFile.equals(FORM_FILE2)) {
                setHead(svf, FORM_FILE2, course._name);
                for (final Iterator itTitle = course._subclasses.iterator(); itTitle.hasNext();) {
                    final Score title = (Score) itTitle.next();
                    printTitle(svf, title);
                    printAvg(svf, course, title);
                    svf.VrEndRecord();
                }
                svf.VrEndPage();
            }
        }
        return rtnflg;
    }

    /*
     * FORM_FILE2を使うか否か？
     * 仕様が、不確定の為、どちらに転んでも大丈夫なように
     * @return FORM_FILE2 を使わないなら true
     */
    private boolean isOneForm() {
        return true;
    }

    private void setHead(final Vrw32alp svf, final String formFile, final String courseName) {
        svf.VrSetForm(formFile, 4);
        svf.VrsOut("YEAR", _param._gengou);
        svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._grade)));
        svf.VrsOut("TESTNAME", _param._testname);
        svf.VrsOut("COURSE_NAME", courseName);
        svf.VrsOut("NOW", _param._date);
    }

    private void printScore(
            final Vrw32alp svf,
            final String formFile,
            final Course course,
            final Score title,
            final List printData
    ) {
        printTitle(svf, title);
        int cnt = 1;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            printStudent(svf, student, title, cnt);
            cnt++;
        }

        if (formFile.equals(FORM_FILE2)) {
            printAvg(svf, course, title);
        }
        svf.VrEndRecord();
    }

    private void printAvg(final Vrw32alp svf, final Course course, final Score title) {
        for (final Iterator it = course._avgData.iterator(); it.hasNext();) {
            final Score avg = (Score) it.next();
            if ((TUUJOU_CHAIRCD.equals(title._chairCd) && avg._subClassCd.equals(title._subClassCd))
                    || (avg._chairCd.equals(title._chairCd) && avg._subClassCd.equals(title._subClassCd))) {
                svf.VrsOut("AVERAGE", avg._score);
            }
        }
    }

    private void printTitle(final Vrw32alp svf, final Score title) {
        svf.VrsOut("CLASSABBV", title._className);
        svf.VrsOut("SUBCLASSABBV", title._subClassName);
    }

    private void printStudent(final Vrw32alp svf, final Student student, final Score title, final int cnt) {
        svf.VrsOutn("SCHREGNO", cnt, student._schregno);
        if (null != student._name) {
            final String fieldNo = (20 < student._name.getBytes().length) ? "2" : "1";
            svf.VrsOutn("NAME" + fieldNo, cnt, student._name);
        }
        svf.VrsOutn("HR_NAMEABBV", cnt, student._homeRoom + "-" + String.valueOf(Integer.parseInt(student._attendNo)));

        for (final Iterator it = student._scoreData.iterator(); it.hasNext();) {
            final Score score = (Score) it.next();
            if ((TUUJOU_CHAIRCD.equals(title._chairCd) && score._subClassCd.equals(title._subClassCd))
                || (score._chairCd.equals(title._chairCd) && score._subClassCd.equals(title._subClassCd))) {
                final String val = (null != score._absence) ? score._absence : score._score;
                svf.VrsOut("SCORE" + cnt, val);
            }
        }
    }

    private void debugPrint(final Map courseData) {
        for (int i = 0; i < _param._courseCd.length; i++) {
            final Course course = (Course) courseData.get(_param._courseCd[i]);
            log.debug(course._cd + " " + course._name);

            for (final Iterator it = course._subclasses.iterator(); it.hasNext();) {
                final Score score = (Score) it.next();
                log.debug("TITLE:\n" + score);
            }

            for (final Iterator it = course._avgData.iterator(); it.hasNext();) {
                final Score score = (Score) it.next();
                log.debug("AVG:\n" + score);
            }

            for (final Iterator it = course._studens.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug(student);
                for (final Iterator itst = student._scoreData.iterator(); itst.hasNext();) {
                    final Score score = (Score) itst.next();
                    log.debug("STUDENT:\n" + score);
                }
            }
        }
    }

}
