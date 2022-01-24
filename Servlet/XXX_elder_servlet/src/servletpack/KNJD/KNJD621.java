// kanji=漢字
/*
 * $Id: 40d0546d92c9e23850ab0554f9d05c37d7736c0b $
 *
 * 作成日: 2007/05/21 18:31:18 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * 科目別得点上位一覧
 * @author m-yama
 * @version $Id: 40d0546d92c9e23850ab0554f9d05c37d7736c0b $
 */
public class KNJD621 {

    private static final Log log = LogFactory.getLog(KNJD621.class);

    private static final String FORM_FILE = "KNJD621.frm";
    private static final String SUBCLASS_ALL = "999999";
    private static final int MAX_LINE = 20;

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
        log.debug("学校種別=" + param._schooldiv + " テスト=" + param._testname + " Form-File=" + FORM_FILE);
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
        private final String[] _subclassCd;
        private final String[] _subclassName;
        private final String _testname;
        private final int _rankRange;
        private final String _standard;
        private final String _useCurriculumcd;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _schooldiv = (3 >= Integer.parseInt(_grade)) ? "J" : "H";
            _subclassCd = request.getParameterValues("CATEGORY_SELECTED");
            _rankRange = Integer.parseInt(request.getParameter("RANK_RANGE"));
            _standard = request.getParameter("STANDARD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            // DBより取得
            _subclassName = getSubclassName(db2, _subclassCd);
            _semesterName = getSemesterName(db2, _year, _semester);
            _testname = getTestName(db2, _year, _semester, _testcd);
        }

        private String[] getSubclassName(
                final DB2UDB db2,
                final String[] subclassCd
        ) throws Exception {
            final String[] rtnVal = new String[subclassCd.length];

            for (int i = 0; i < subclassCd.length; i++) {
                if (subclassCd[i].equals(SUBCLASS_ALL)) {
                    rtnVal[i] = "合計";
                } else {
                    String sql = "";
                    sql += "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE ";
                    if ("1".equals(_useCurriculumcd)) {
                        sql += "  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
                    }
                    sql += "  SUBCLASSCD = '" + subclassCd[i] + "' ";
                    db2.query(sql);
                    final ResultSet rs = db2.getResultSet();
                    try {
                        rs.next();
                        rtnVal[i] = rs.getString("SUBCLASSNAME");
                    } finally {
                        DbUtils.closeQuietly(rs);
                        db2.commit();
                    }
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

        final Map subclassData = new HashMap();
        for (int i = 0; i < _param._subclassCd.length; i++) {
            final List students = new ArrayList(Student.loadStudents(db2, _param._subclassCd[i], _param));

            subclassData.put(_param._subclassCd[i], students);
        }

        final boolean rtnflg = printOut(svf, subclassData);
        return rtnflg;
    }

    /** 出力データ */
    private static class Student {

        private static final List PRINTDATA = new ArrayList();

        private final int _rank;
        private final String _studentName;
        private final String _hrClass;
        private final String _score;

        Student(final int rank,
                final String studentName,
                final String hrClass,
                final String score
        ) {
            _rank = rank;
            _studentName = studentName;
            _hrClass = hrClass;
            _score = score;
        }

        public static List loadStudents(
                final DB2UDB db2,
                final String subclassCd,
                final Param param
        ) throws Exception {
            PRINTDATA.clear();

            final String sql = getStudentsSql(subclassCd, param);
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    final int rank = ("1".equals(param._standard)) ? rs.getInt("GRADE_RANK") : rs.getInt("GRADE_AVG_RANK");
                    final String score = rs.getString("SCORE") == null ? "" : "1".equals(param._standard) ? rs.getString("SCORE") : new BigDecimal(rs.getString("SCORE")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String avg = rs.getString("AVG") == null ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String val = ("1".equals(param._standard) || !SUBCLASS_ALL.equals(subclassCd)) ? score : avg;
                    final String hrName = rs.getString("HR_NAME") == null ? "" : rs.getString("HR_NAME");
                    final Student student = new Student(rank, rs.getString("NAME"), hrName, val);
                    PRINTDATA.add(student);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return PRINTDATA;
        }

        private static String getStudentsSql(final String subclassCd, final Param param) {
            String rtnSql = "SELECT "
                                + "    T1.SCHREGNO, "
                                + "        T1.GRADE_RANK, "
                                + "        T1.GRADE_AVG_RANK, "
                                + "        L1.NAME_SHOW as NAME, "
                                + "        L3.HR_NAME, "
                                + "        T1.SCORE, "
                                + "        T1.AVG "
                                + "FROM "
                                + "    RECORD_RANK_DAT T1 "
                                + "    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO "
                                + "    LEFT JOIN SCHREG_REGD_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO "
                                + "         AND L2.YEAR = T1.YEAR "
                                + "         AND L2.SEMESTER = T1.SEMESTER "
                                + "    LEFT JOIN SCHREG_REGD_HDAT L3 ON  L3.YEAR = T1.YEAR "
                                + "         AND L3.SEMESTER = T1.SEMESTER "
                                + "         AND L3.GRADE = L2.GRADE "
                                + "         AND L3.HR_CLASS = L2.HR_CLASS "
                                + "WHERE "
                                + "    T1.YEAR = '" + param._year + "' "
                                + "    AND T1.SEMESTER = '" + param._semester + "' "
                                + "    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testcd + "' "
                                + "    AND ";
            if (!SUBCLASS_ALL.equals(subclassCd) && "1".equals(param._useCurriculumcd)) {
                        rtnSql += "        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
            }
                        rtnSql += "        T1.SUBCLASSCD = '" + subclassCd + "' "
                                + "    AND " + ("1".equals(param._standard) ? " T1.GRADE_RANK " : "T1.GRADE_AVG_RANK" ) + " <= " + param._rankRange + " "
                                + "    AND L2.GRADE = '" + param._grade + "' "
                                + "ORDER BY "
                                + "    " + ("1".equals(param._standard) ? " T1.GRADE_RANK " : "T1.GRADE_AVG_RANK" ) + ", "
                                + "    L2.HR_CLASS, "
                                + "    L2.ATTENDNO ";
            return rtnSql;
        }

        public String toString() {
            final StringBuffer rtnStb = new StringBuffer();
            for (final Iterator it = PRINTDATA.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                rtnStb.append("席次 " + student._rank + "名前 " + student._studentName + "クラス " + student._hrClass + "得点 " + student._score + "\n");
            }
            return rtnStb.toString();
        }
    }

    /** 出力処理 */
    private boolean printOut(final Vrw32alp svf, final Map subclassData) {

        boolean rtnflg = false;
        int seq = 1;
        for (int i = 0; i < _param._subclassCd.length; i++) {

            seq = endPage(svf, seq, i);

            final List student = (List) subclassData.get(_param._subclassCd[i]);

            for (final Iterator it = student.iterator(); it.hasNext();) {
                if (seq > MAX_LINE) {
                    seq = endPage(svf, seq, i);
                }
                final Student subclassStudents = (Student) it.next();
                svf.VrsOut("RANK", String.valueOf(subclassStudents._rank));
                svf.VrsOut("NAME",  String.valueOf(subclassStudents._studentName));
                svf.VrsOut("HR_NAME", String.valueOf(subclassStudents._hrClass));
                svf.VrsOut("SCORE", String.valueOf(subclassStudents._score));
                svf.VrEndRecord();
                log.debug(String.valueOf(subclassStudents._rank));
                seq++;
                rtnflg = true;
            }
        }
        if (seq > 1) {
            svf.VrEndPage();
        }
        return rtnflg;
    }

    private void setHead(final Vrw32alp svf, final int i) {
        svf.VrSetForm(FORM_FILE, 4);
        svf.VrsOut("YEAR", _param._gengou);
        svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._grade)));
        svf.VrsOut("TESTNAME", _param._testname);
        svf.VrsOut("TITLE", _param._subclassName[i]);
        svf.VrsOut("SCORE_HEADER", ("1".equals(_param._standard)) ? "得点" : "平均点");
    }

    private int endPage(final Vrw32alp svf, final int seq, final int i) {
        if (seq > 1) {
            svf.VrEndPage();
        }
        setHead(svf, i);
        return 1;
    }
}
