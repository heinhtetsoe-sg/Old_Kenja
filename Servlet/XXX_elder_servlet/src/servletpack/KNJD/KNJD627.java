// kanji=漢字
/*
 * $Id: b81ffc4f0a593086ce998260a8c30305d49d3da8 $
 *
 * 作成日: 2007/06/04 13:28:59 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: b81ffc4f0a593086ce998260a8c30305d49d3da8 $
 */
public class KNJD627 {

    private static final Log log = LogFactory.getLog(KNJD627.class);

    private static final String FORM_FILE627 = "KNJD627.frm";
    private static final String FORM_FILE628 = "KNJD628.frm";

    private static final int BORDER_SCORE1 = 29;
    private static final int BORDER_SCORE2 = 59;
    private static final int BORDER_SCORE3 = 59;

    // KNJD627
    private static final int MAXLINE = 25;
    private static final int MAXROW  = 19;

    // KNJD628
    private static final int ROWCNT = 10;

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
        log.debug("学校種別=" + param._schooldiv + " テスト=" + param._testname + " 対象者=" + param._title + " 日付=" + param._date);
        final List courseSorted = new ArrayList(param._course.keySet());
        Collections.sort(courseSorted);
        for (final Iterator it = courseSorted.iterator(); it.hasNext();) {
            final String key = (String) it.next();
            log.debug(key + " = " + param._course.get(key));
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
        private final String _outputDiv;
        private final String _title;
        private final String _date;
        private final String _testname;
        private final String _prgId;
        private final Form _form;
        private final Map _course;
        private final boolean _isjunior;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _gengou = gengou + "年度";
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _isjunior = Integer.parseInt(_grade) < 4 ? true : false;
            _testcd = request.getParameter("TESTCD");
            _schooldiv = (3 >= Integer.parseInt(_grade)) ? "J" : "H";
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _prgId = request.getParameter("PRGID");
            _title = getTitleName(_outputDiv, _prgId);
            _form = createForm(_prgId);

            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            _date = stb.toString();

            // DBより取得
            _semesterName = getSemesterName(db2, _year, _semester);
            _testname = getTestName(db2, _year, _semester, _testcd);
            _course = new HashMap(getCourse(db2, _year, _semester, _grade));
        }

        private Form createForm(final String prgId) {
            final Form form;
            if (_prgId.equals("KNJD627")) {
                form = new Form627();
            } else {
                form = new Form628();
            }
            return form;
        }

        private String getTitleName(final String outputDiv, final String prgId) {
            final Map title = new HashMap();
            final String titleLast = prgId.equals("KNJD627") ? "対象者一覧" : "結果一覧";
            title.put("1", "未受験・未到達・欠点" + titleLast);
            title.put("2", "未受験" + titleLast);
            title.put("3", "未到達" + titleLast);
            title.put("4", "追試未到達" + titleLast);
            if (prgId.equals("KNJD627")) {
                title.put("5", "再試験未到達" + titleLast);
            } else {
                title.put("5", "確認テスト・再試験" + titleLast);
            }

            return (String) title.get(outputDiv);
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

        private Map getCourse(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade
        ) throws Exception {
            final Map rtnMap = new HashMap();
            final String sql = "SELECT "
                             + "    T1.COURSECD || T1.MAJORCD || T2.COURSECODE AS VALUE, "
                             + "    T1.MAJORNAME || T2.COURSECODENAME AS NAME "
                             + "FROM "
                             + "    MAJOR_MST T1, "
                             + "    COURSECODE_MST T2 "
                             + "WHERE "
                             + "    T1.COURSECD || T1.MAJORCD || T2.COURSECODE IN "
                             + "    (SELECT "
                             + "         T3.COURSECD || T3.MAJORCD || T3.COURSECODE "
                             + "     FROM "
                             + "         SCHREG_REGD_DAT T3 "
                             + "     WHERE "
                             + "         T3.YEAR = '" + year + "' "
                             + "         AND T3.SEMESTER = '" + semester + "' "
                             + "         AND T3.GRADE = '" + grade + "' "
                             + "     GROUP BY "
                             + "         T3.COURSECD || T3.MAJORCD || T3.COURSECODE) ";
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnMap.put(rs.getString("VALUE"), rs.getString("NAME"));
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }

    /** フォームクラス */
    abstract class Form {
        private String _name;
        private int _type;

        void setName(final String name) {
            _name = name;
        }

        void setType(final int type) {
            _type = type;
        }

        String getName() {
            return _name;
        }

        int getType() {
            return _type;
        }

        String scoreSql(
                final String fieldVal,
                final Param param,
                final String loadDiv
        ) {
            // 未到達条件
            final String borderCnt = "L1.SCORE <= " + BORDER_SCORE1;
            // 未受験条件
            final String nullCnt   = "L1.SCORE IS NULL";

            final StringBuffer stbCase = new StringBuffer();
            stbCase.append((param._outputDiv.equals("2") || param._outputDiv.equals("4")) ? nullCnt : borderCnt);

            // 追試・再試験のボーダー
            final int suppBorder = param._outputDiv.equals("4") ? BORDER_SCORE1 : BORDER_SCORE2;

            if (param._outputDiv.equals("1") || (param._outputDiv.equals("5") && param._prgId.equals("KNJD628"))) {
                stbCase.append(" OR " + nullCnt);
            } else if (param._outputDiv.equals("4") || param._outputDiv.equals("5")) {
                stbCase.append("    AND VALUE(L2.SCORE_PASS, 0) <= " + suppBorder);
            }

            final String scoreFlg  = param._outputDiv.equals("4") ? "'1', '2'" : "'3'";

            final StringBuffer stb = new StringBuffer();
            stb.append(getTitle(param, fieldVal, loadDiv));
            stb.append("SELECT ");
            stb.append("    T1.SUBCLASSCD, ");
            stb.append("    T1.SUBCLASSABBV, ");

            stb.append(selectSql(stbCase.toString(), param));

            stb.append("FROM ");
            stb.append("    MAIN_T T1 ");
            stb.append("    LEFT JOIN RECORD_RANK_DAT L1 ON L1.YEAR = '" + param._year + "' ");
            stb.append("         AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L1.TESTKINDCD || L1.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L1.SCHREGNO = '" + fieldVal + "' ");

            stb.append(joinSql(fieldVal, param, scoreFlg));

            stb.append("ORDER BY ");
            stb.append("    T1.SUBCLASSCD ");

            return stb.toString();
        }

        abstract String selectSql(final String stbCase, final Param param);

        abstract String joinSql(final String fieldVal, final Param param, final String scoreFlg);

        abstract boolean formPrintOut(final Vrw32alp svf, final Map courseData);
    }

    /** フォーム627 */
    private class Form627 extends Form {
        public Form627() {
            setName(FORM_FILE627);
            setType(1);
        }

        String joinSql(final String fieldVal, final Param param, final String scoreFlg) {
            final StringBuffer stb = new StringBuffer();

            stb.append("    LEFT JOIN SUPP_EXA_DAT L2 ON L2.YEAR = '" + param._year + "' ");
            stb.append("         AND L2.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L2.TESTKINDCD || L2.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("         AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L2.SCHREGNO = '" + fieldVal + "' ");
            stb.append("         AND L2.SCORE_FLG IN (" + scoreFlg + ") ");

            return stb.toString();
        }

        String selectSql(final String stbCase, final Param param) {
            final StringBuffer stb = new StringBuffer();

            if (param._outputDiv.equals("4") || param._outputDiv.equals("5")) {
                stb.append("    CASE WHEN " + stbCase + " THEN L2.SCORE_PASS ELSE L1.SCORE END AS VALUE1, ");
            } else {
                stb.append("    L1.SCORE AS VALUE1, ");
            }
            stb.append("    0 AS VALUE2, ");
            stb.append("    0 AS VALUE3, ");
            stb.append("    CASE WHEN " + stbCase + " THEN 1 ELSE 0 END AS DATADIV ");

            return stb.toString();
        }

        boolean formPrintOut(final Vrw32alp svf, final Map courseData) {
            return printOut(svf, courseData);
        }

    }

    /** フォーム628 */
    private class Form628 extends Form {
        public Form628() {
            setName(FORM_FILE628);
            setType(4);
        }

        String joinSql(final String fieldVal, final Param param, final String scoreFlg) {
            final StringBuffer stb = new StringBuffer();

            stb.append("    LEFT JOIN SUPP_EXA_DAT L2 ON L2.YEAR = '" + param._year + "' ");
            stb.append("         AND L2.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L2.TESTKINDCD || L2.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("         AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L2.SCHREGNO = '" + fieldVal + "' ");
            stb.append("         AND L2.SCORE_FLG IN (" + scoreFlg + ") ");
            stb.append("    LEFT JOIN SUPP_EXA_DAT L3 ON L3.YEAR = '" + param._year + "' ");
            stb.append("         AND L3.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L3.TESTKINDCD || L3.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("         AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L3.SCHREGNO = '" + fieldVal + "' ");
            stb.append("         AND L3.SCORE_FLG IN ('3') ");
            stb.append("    LEFT JOIN SUPP_EXA_DAT L4 ON L4.YEAR = '" + param._year + "' ");
            stb.append("         AND L4.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L4.TESTKINDCD || L4.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("         AND L4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L4.SCHREGNO = '" + fieldVal + "' ");
            stb.append("         AND L4.SCORE_FLG IN ('1', '2') ");

            return stb.toString();
        }

        String selectSql(final String stbCase, final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("    L1.SCORE AS VALUE1, ");
            if (_param._outputDiv.equals("5")) {
                stb.append("    L3.SCORE AS VALUE2, ");
                stb.append("    L4.SCORE AS VALUE3, ");
            } else {
                stb.append("    L3.SCORE_PASS AS VALUE2, ");
                stb.append("    L4.SCORE_PASS AS VALUE3, ");
            }
            stb.append("    CASE WHEN " + stbCase + " THEN 1 ELSE 0 END AS DATADIV ");

            return stb.toString();
        }

        boolean formPrintOut(final Vrw32alp svf, final Map courseData) {

            boolean rtnflg = false;

            final List courseSorted = new ArrayList(_param._course.keySet());
            Collections.sort(courseSorted);
            for (final Iterator it = courseSorted.iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final Course course = (Course) courseData.get(key);

                setHead(svf, course._name);

                rtnflg = dataPrint(svf, course) ? true : rtnflg;
            }

            if (rtnflg) {
                svf.VrEndPage();
            }
            return rtnflg;
        }

        private boolean dataPrint(final Vrw32alp svf, final Course course) {

            boolean rtnflg = false;
            for (final Iterator it = course._students.iterator(); it.hasNext();) {

                final Student student = (Student) it.next();

                outputStudent(svf, student);

                rtnflg = true;
            }
            return rtnflg;
        }

        private void outputStudent(final Vrw32alp svf, final Student student) {

            boolean namePrint = false;
            int cnt = 1;
            for (final Iterator it = student._scoreData.iterator(); it.hasNext();) {
                final Score score = (Score) it.next();

                if (score._dataDiv.equals("0")) {
                    continue;
                }

                studentPrint(svf, student, namePrint);
                namePrint = true;

                scorePrint(svf, score, cnt);
                cnt++;

                if (cnt > ROWCNT) {
                    cnt = endRecordProcess(svf);
                }
            }

            if (cnt > 1) {
                cnt = endRecordProcess(svf);
            }
        }

        private void studentPrint(final Vrw32alp svf, final Student student, final boolean namePrint) {
            if (!namePrint && null != student._name) {
                final String fieldNo = (20 < student._name.getBytes().length) ? "2" : "1";
                svf.VrsOut("NAME" + fieldNo, student._name);
            }
            svf.VrsOut("HR_NAME", student._homeRoom + "-" + String.valueOf(Integer.parseInt(student._attendNo)));
            svf.VrsOut("MASK", student._homeRoom + "-" + String.valueOf(Integer.parseInt(student._attendNo)));
        }

        void scorePrint(
                final Vrw32alp svf,
                final Score score,
                final int cnt
        ) {
            svf.VrsOut("SUBCLASSABBV" + cnt + "_1", score._subClassAbbv);
            svf.VrsOut("SCORE" + cnt + "_1", score._score1);
            boolean isNullScore1 = score._score1 == null;

            final String fuga = "SCORE" + cnt + "_2";
            if (_param._outputDiv.equals("5")) {
                final int borderScore = _param._isjunior ? BORDER_SCORE3 : BORDER_SCORE2;

                final String hoge;
                if ((score._score2 == null || (Integer.parseInt(score._score2) <= borderScore)) && !isNullScore1) {
                    hoge = "Paint=(1,70,2),Bold=1";
                } else {
                    hoge = "Paint=(1,100,1),Bold=1";
                }
                svf.VrAttribute(fuga, hoge);
            }
            svf.VrsOut(fuga, score._score2);

            final String fuga3 = "SCORE" + cnt + "_3";
            if (_param._outputDiv.equals("5")) {
                final String hoge;
                if (score._score3 == null && isNullScore1) {
                    hoge = "Paint=(1,70,2),Bold=1";
                } else {
                    hoge = "Paint=(1,100,1),Bold=1";
                }
                svf.VrAttribute(fuga3, hoge);
            }
            svf.VrsOut(fuga3, score._score3);
        }

        private int endRecordProcess(final Vrw32alp svf) {
            svf.VrEndRecord();
            return 1;
        }
    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final Map courseData = new HashMap();

        final List courseSorted = new ArrayList(_param._course.keySet());
        Collections.sort(courseSorted);
        for (final Iterator it = courseSorted.iterator(); it.hasNext();) {
            final String key = (String) it.next();
            courseData.put(key, new Course(db2, key, (String) _param._course.get(key)));
        }

        final boolean rtnflg = _param._form.formPrintOut(svf, courseData);

        if (log.isDebugEnabled()) {
            debugPrint(courseData);
        }

        return rtnflg;
    }

    /** コースクラス */
    private class Course {

        private final String _cd;
        private final String _name;
        private final List _subclasses;
        private final List _students = new ArrayList();

        Course(final DB2UDB db2, final String courseCd, final String courseName) throws Exception {
            _cd = courseCd;
            _name = courseName;
            _subclasses = new ArrayList(Score.load(db2, _param, courseCd, "TITLE"));

            PreparedStatement ps = null;
            final String sql = getStudentInfoSql(courseCd);
            ps = db2.prepareStatement(sql);
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    _students.add(new Student(db2, rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAMEABBV"), rs.getString("ATTENDNO")));
                }
            } finally {
                DbUtils.closeQuietly(rs);
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        // CSOFF: ExecutableStatementCount
        private String getStudentInfoSql(final String courseCd) {
            final String borderCnt = "L1.SCORE <= " + BORDER_SCORE1;
            final String nullCnt   = "L1.SCORE IS NULL";
            final String supp  = _param._outputDiv.equals("4") ? nullCnt : borderCnt;

            final String scoreFlg  = _param._outputDiv.equals("4") ? "'1', '2'" : "'3'";
            final int border = _param._outputDiv.equals("4") ? BORDER_SCORE1 : BORDER_SCORE2;

            final StringBuffer stb = new StringBuffer();
            stb.append(getTitle(_param, courseCd, "OUTPUT"));
            stb.append("SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    L3.NAME, ");
            stb.append("    L5.HR_CLASS, ");
            stb.append("    L5.HR_NAMEABBV, ");
            stb.append("    L4.ATTENDNO, ");
            stb.append("    COUNT(*) AS CNT, ");
            stb.append("    SUM(CASE WHEN " + borderCnt + " THEN 1 ELSE 0 END) AS BORDER_CNT, ");
            stb.append("    SUM(CASE WHEN " + nullCnt + " THEN 1 ELSE 0 END) AS NULL_CNT, ");
            stb.append("    SUM(CASE WHEN " + supp + " AND VALUE(L2.SCORE_PASS, 0) <= " + border + " THEN 1 ELSE 0 END) AS SUPP_CNT ");
            stb.append("FROM ");
            stb.append("    MAIN_T T1 ");
            stb.append("    LEFT JOIN RECORD_RANK_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
            stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SUPP_EXA_DAT L2 ON L2.YEAR = '" + _param._year + "' ");
            stb.append("         AND L2.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND L2.TESTKINDCD || L2.TESTITEMCD = '" + _param._testcd + "' ");
            stb.append("         AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND L2.SCORE_FLG IN (" + scoreFlg + ") ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCH_T L4 ON L4.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT L5 ON L5.YEAR = '" + _param._year + "' ");
            stb.append("         AND L5.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND L5.GRADE = L4.GRADE ");
            stb.append("         AND L5.HR_CLASS = L4.HR_CLASS ");
            stb.append("GROUP BY ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    L3.NAME, ");
            stb.append("    L5.HR_CLASS, ");
            stb.append("    L5.HR_NAMEABBV, ");
            stb.append("    L4.ATTENDNO ");
            stb.append("HAVING ");

            if (_param._outputDiv.equals("2") || _param._outputDiv.equals("4")) {
                stb.append("    SUM(CASE WHEN " + nullCnt + " THEN 1 ELSE 0 END) > 0 ");
            } else {
                stb.append("    SUM(CASE WHEN " + borderCnt + " THEN 1 ELSE 0 END) > 0 ");
            }

            if (_param._outputDiv.equals("1") || (_param._outputDiv.equals("5") && _param._prgId.equals("KNJD628"))) {
                stb.append("    OR SUM(CASE WHEN " + nullCnt + " THEN 1 ELSE 0 END) > 0 ");
            } else if (_param._outputDiv.equals("4") || _param._outputDiv.equals("5")) {
                stb.append("    AND SUM(CASE WHEN " + supp + " AND VALUE(L2.SCORE_PASS, 0) <= " + border + " THEN 1 ELSE 0 END) > 0 ");
            }

            stb.append("ORDER BY ");
            stb.append("    L5.HR_CLASS, ");
            stb.append("    L4.ATTENDNO, ");
            stb.append("    T1.SCHREGNO ");

            return stb.toString();
        }

        public String toString() {
            return "コースコード=" + _cd + "コース名称=" + _name;
        }
    }
    // CSON: ExecutableStatementCount

    /** 得点クラス */
    private static class Score {
        private static final List SCORES = new ArrayList();

        private final String _subClassCd;
        private final String _subClassAbbv;
        private final String _score1;
        private final String _score2;
        private final String _score3;
        private final String _dataDiv;

        Score(
                final String subclassCd,
                final String subclassName,
                final String score1,
                final String score2,
                final String score3,
                final String dataDiv
        ) {
            _subClassCd = subclassCd;
            _subClassAbbv = subclassName;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _dataDiv = dataDiv;
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
                    final Score score = new Score(
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSABBV"),
                            rs.getString("VALUE1"),
                            rs.getString("VALUE2"),
                            rs.getString("VALUE3"),
                            rs.getString("DATADIV"));
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
                sql = param._form.scoreSql(fieldVal, param, loadDiv);
            } else {
                sql = getTitle(param, fieldVal, loadDiv);
            }

            return sql;
        }

        public String toString() {
            return " 科目：" + _subClassCd + "-" + _subClassAbbv
                  + " 得点：" + _score1
                  + " 区分：" + _dataDiv + "\n";
        }
    }

    // CSOFF: ExecutableStatementCount
    // CSOFF: MethodLength
    private static String getTitle(final Param param, final String condition, final String conditionDiv) {

        final String field = conditionDiv.equals("SCORE") ? "SCHREGNO" : "COURSECD || MAJORCD || COURSECODE";
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
        stb.append("    SCHREGNO, ");
        stb.append("    GRADE, ");
        stb.append("    HR_CLASS, ");
        stb.append("    ATTENDNO ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT ");
        stb.append("WHERE ");
        stb.append("    YEAR = '" + param._year + "' ");
        stb.append("    AND SEMESTER = '" + param._semester + "' ");
        stb.append("    AND GRADE = '" + param._grade + "' ");
        stb.append("    AND " + field + " = '" + condition + "' ");
        stb.append("), CHR_T AS ( ");
        stb.append("SELECT DISTINCT ");

        if (conditionDiv.equals("OUTPUT")) {
            stb.append("    T1.SCHREGNO, ");
        }

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

        if (conditionDiv.equals("OUTPUT")) {
            stb.append("    T3.SCHREGNO, ");
        }

        stb.append("    L2.SUBCLASSCD, ");
        stb.append("    L2.SUBCLASSABBV, ");
        stb.append("    0 AS VALUE1, ");
        stb.append("    0 AS VALUE2, ");
        stb.append("    0 AS VALUE3, ");
        stb.append("    '' AS DATADIV ");
        stb.append("FROM ");
        stb.append("    SCH_CHR_TEST T1 ");
        stb.append("    LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = '" + param._year + "' ");
        stb.append("         AND L1.SEMESTER = '" + param._semester + "' ");
        stb.append("         AND L1.CHAIRCD = T1.CHAIRCD ");
        stb.append("    LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = L1.SUBCLASSCD, ");
        stb.append("    SEM_T T2, ");
        stb.append("    CHR_T T3 ");
        stb.append("WHERE ");
        stb.append("    T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testcd + "' ");
        stb.append("    AND T1.CHAIRCD = T3.CHAIRCD ");

        if (!conditionDiv.equals("TITLE")) {
            stb.append(") ");
        } else {
            stb.append("ORDER BY ");
            stb.append("    L2.SUBCLASSCD ");
        }

        return stb.toString();
    }
    // CSON: MethodLength
    // CSON: ExecutableStatementCount

    /** 生徒クラス */
    private class Student {

        private final String _schregno;
        private final String _name;
        private final String _homeRoom;
        private final String _attendNo;
        private final List _scoreData;

        public Student(
                final DB2UDB db2,
                final String schregno,
                final String name,
                final String homeRoom,
                final String attendNo
        ) throws Exception {
            _schregno = schregno;
            _name = name;
            _homeRoom = homeRoom;
            _attendNo = attendNo;
            _scoreData = new ArrayList(Score.load(db2, _param, schregno, "SCORE"));
        }

        public String toString() {
            return "学籍番号：" + _schregno + " 氏名：" + _name + " クラス：" + _homeRoom + " 番号：" + _attendNo;
        }
    }

    private boolean printOut(final Vrw32alp svf, final Map courseData) {

        boolean rtnflg = false;

        final List courseSorted = new ArrayList(_param._course.keySet());
        Collections.sort(courseSorted);
        for (final Iterator it = courseSorted.iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final Course course = (Course) courseData.get(key);
            final List outputPageData = new ArrayList(getOutputPageData(course));
            final Map studentCountData = studentCount(course);

            rtnflg = dataPrint(svf, course, outputPageData, studentCountData) ? true : rtnflg;
        }

        return rtnflg;
    }

    /**
     * ページ単位にデータを分割
     */
    private List getOutputPageData(final Course course) {

        final List rtnList = new ArrayList();
        final List lineData = new ArrayList();
        int i = 1;

        for (final Iterator it = course._students.iterator(); it.hasNext();) {
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

    /** 科目別対象人数をカウント */
    private Map studentCount(final Course course) {
        final Map rtnMap = new HashMap();

        for (final Iterator itsub = course._subclasses.iterator(); itsub.hasNext();) {
            final Score subclass = (Score) itsub.next();

            int count = 0;
            for (final Iterator itstudent = course._students.iterator(); itstudent.hasNext();) {
                final Student student = (Student) itstudent.next();

                for (final Iterator it = student._scoreData.iterator(); it.hasNext();) {
                    final Score score = (Score) it.next();

                    if (score._subClassCd.equals(subclass._subClassCd)) {
                        if (score._dataDiv.equals("1")) {
                            count++;
                        }
                    }
                }
            }

            if (0 < count) rtnMap.put(subclass._subClassCd, String.valueOf(count));
        }

        return rtnMap;
    }

    /**
     * データ印字
     */
    private boolean dataPrint(
            final Vrw32alp svf,
            final Course course,
            final List outputPageData,
            final Map studentCountData
    ) {
        boolean rtnflg = false;

        int pageMax = outputPageData.size();
        int pageCnt = 1;

        for (final Iterator it = outputPageData.iterator(); it.hasNext();) {
            final List printData = (List) it.next();

            setHead(svf, course._name);

            int rowCnt = 1;
            for (final Iterator itTitle = course._subclasses.iterator(); itTitle.hasNext();) {
                if (rowCnt > MAXROW) {
                    svf.VrEndPage();
                    setHead(svf, course._name);
                    rowCnt = 1;
                }

                final Score title = (Score) itTitle.next();
                printScore(svf, course, title, printData, rowCnt);
                if (studentCountData.containsKey(title._subClassCd) && pageCnt == pageMax) {
                    svf.VrsOutn("STUDENT", rowCnt, (String) studentCountData.get(title._subClassCd) + "名");
                }
                rowCnt++;
                rtnflg = true;
            }

            svf.VrEndPage();
            pageCnt++;
        }
        return rtnflg;

    }

    private void setHead(final Vrw32alp svf, final String courseName) {
        svf.VrSetForm(_param._form.getName(), _param._form.getType());
        svf.VrsOut("YEAR", _param._gengou);
        svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._grade)));
        svf.VrsOut("TESTNAME", _param._testname);
        svf.VrsOut("TITLE", _param._title);
        svf.VrsOut("COURSE_NAME", courseName);
        svf.VrsOut("NOW", _param._date);

        // 一覧表枠外の文言
        svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
        svf.VrsOut("NOTE1",  " ");
        svf.VrsOut("NOTE2", "：追試・見込点、再試験点");
    }

    private void printScore(
            final Vrw32alp svf,
            final Course course,
            final Score title,
            final List printData,
            final int rowCnt
    ) {

        printTitle(svf, title, rowCnt);

        int cnt = 1;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            printStudent(svf, student, title, cnt, rowCnt);
            cnt++;
        }

    }

    private void printTitle(final Vrw32alp svf, final Score title, final int rowCnt) {
        svf.VrsOutn("SUBCLASSABBV", rowCnt, title._subClassAbbv);
    }

    private void printStudent(
            final Vrw32alp svf,
            final Student student,
            final Score title,
            final int cnt,
            final int rowCnt
    ) {
        if (null != student._name) {
            final String fieldNo = (20 < student._name.getBytes().length) ? "2" : "1";
            svf.VrsOutn("NAME" + fieldNo, cnt, student._name);
        }
        svf.VrsOutn("HRNAME", cnt, student._homeRoom + "-" + String.valueOf(Integer.parseInt(student._attendNo)));

        for (final Iterator it = student._scoreData.iterator(); it.hasNext();) {
            final Score score = (Score) it.next();
            if (score._subClassCd.equals(title._subClassCd)) {
                if (score._dataDiv.equals("1")) {
                    svf.VrAttributen("SCORE" + rowCnt, cnt, "Paint=(2,70,2),Bold=1");
                }
                svf.VrsOutn("SCORE" + rowCnt, cnt, score._score1);
            }
        }
    }

    /** デバック用 */
    private void debugPrint(final Map courseData) {
        final List courseSorted = new ArrayList(_param._course.keySet());
        Collections.sort(courseSorted);
        for (final Iterator it = courseSorted.iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final Course course = (Course) courseData.get(key);

            log.debug(course);
            for (final Iterator itsub = course._subclasses.iterator(); itsub.hasNext();) {
                final Score subclass = (Score) itsub.next();
                log.debug(subclass);
            }
            for (final Iterator itstudent = course._students.iterator(); itstudent.hasNext();) {
                final Student student = (Student) itstudent.next();
                log.debug(student);
            }
        }
    }
}

