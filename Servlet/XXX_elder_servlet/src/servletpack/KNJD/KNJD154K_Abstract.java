// kanji=漢字
/*
 * $Id: a1a5f3c76930a66ef941165a005d17a87fb21143 $
 *
 * 作成日: 2011/05/02 16:01:11 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: a1a5f3c76930a66ef941165a005d17a87fb21143 $
 */
public abstract class KNJD154K_Abstract {

    private static final Log log = LogFactory.getLog("KNJD154K_Abstract.class");

    protected boolean _hasData;

    Param _param;
    protected DB2UDB _db2;
    protected Vrw32alp _svf;

    protected static final String AVG_GRADE = "1";
    protected static final String AVG_HR = "2";
    protected static final String AVG_COURSE = "3";
    protected static final String AVG_MAJOR = "4";

    protected static final String SUBCLASS3   = "333333";
    protected static final String SUBCLASS5   = "555555";
    protected static final String SUBCLASSALL = "999999";

    protected KNJSchoolMst _knjSchoolMst;

    /**
     * コンストラクタ。
     */
    public KNJD154K_Abstract() {
        super();
    }

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _svf = new Vrw32alp();
        try {
            init(response);

            _db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            _db2.open();

            _param = createParam(request);
            _knjSchoolMst = new KNJSchoolMst(_db2, _param._ctrlYear);

            _hasData = false;

        } catch (final Exception e) {
            log.error("Exception:", e);
        }

    }

    private void init(
            final HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/pdf");
        _svf.VrInit();
        _svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    protected void printMain() throws Exception {
    }

    protected List getStudentList() throws SQLException  {
        final List rtnStudent = new ArrayList();
        ResultSet rs = null;
        try {
            final String sql = getStudentInfoSql();
            _db2.query(sql);
            rs = _db2.getResultSet();
            while (rs.next()) {
                Student student = new Student(rs.getString("SCHREGNO"),
                                              rs.getString("GRADE"),
                                              rs.getString("HR_CLASS"),
                                              rs.getString("ATTENDNO"),
                                              rs.getString("HR_NAME"),
                                              rs.getString("HR_NAMEABBV"),
                                              "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"),
                                              rs.getString("COURSECD"),
                                              rs.getString("COURSENAME"),
                                              rs.getString("MAJORCD"),
                                              rs.getString("MAJORNAME"),
                                              rs.getString("COURSECODE"),
                                              rs.getString("COURSECODENAME"));
                rtnStudent.add(student);
            }
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnStudent;
    }

    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VSCH.SCHREGNO, ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO, ");
        stb.append("     VSCH.HR_NAME, ");
        stb.append("     VSCH.HR_NAMEABBV, ");
        stb.append("     VSCH.NAME, ");
        stb.append("     BASE.REAL_NAME, ");
        stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
        stb.append("     VSCH.COURSECD, ");
        stb.append("     L1.COURSENAME, ");
        stb.append("     VSCH.MAJORCD, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     VSCH.COURSECODE, ");
        stb.append("     L2.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO VSCH ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
        stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
        stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
        stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     VSCH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND VSCH.SEMESTER = '" + _param.getSeme() + "' ");
        stb.append("     AND VSCH.GRADE || '-' || VSCH.HR_CLASS = '" + _param._gradeHrClass + "' ");
        stb.append("     AND VSCH.SCHREGNO IN " + _param._selectInstate + " ");
        stb.append(" ORDER BY ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO ");

        return stb.toString();
    }

    class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        Map _testMap;
        Map _testAllMap;
        Map _mockMap;
        Map _mockAllMap;
        List _mockList;

        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _testMap = new TreeMap();
            _testAllMap = new TreeMap();
            _mockMap = new TreeMap();
            _mockAllMap = new TreeMap();
            _mockList = new ArrayList();
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }

    class TestScore {
        String _name;
        String _score;
        String _gradeAvg;
        String _gradeRank;
        String _gradeCnt;
        String _gradeHighScore;
        String _gradeLowScore;
        String _hrAvg;
        String _hrRank;
        String _hrCnt;
        String _hrHighScore;
        String _hrLowScore;
        String _courseAvg;
        String _courseRank;
        String _courseCnt;
        String _courseHighScore;
        String _courseLowScore;
        String _majorAvg;
        String _majorRank;
        String _majorCnt;
        String _majorHighScore;
        String _majorLowScore;
        String _chairGradeRank;
        String _chairHrRank;
        String _chairMajorRank;
        String _chairCourseRank;
        String _credit;
        String _absenceHigh;
        String _getAbsenceHigh;
        String _average;
        String _chaircd;
        String _slump;
        String _passScore;

        public TestScore(final String score) {
            _score = score;
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _hrAvg
                    + " 席次：" + _hrRank;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request) throws Exception {
        final Param param = new Param(request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    protected class Param {
        final String _semester;
        final String _gradeHrClass;
        final String _testCd;
        final String[] _selectData;
        final String _selectInstate;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useCurriculumcd;
        final String _output;
        final List _testList;
        final String _tableDiv;
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        final String _schoolName;

        Param(final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _testCd = request.getParameter("TEST_CD");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号
            _selectInstate = getInstate(_selectData);
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _output = request.getParameter("OUTPUT");
            _testList = getTestList();
            _tableDiv = "1";

            _z010 = setNameMst("Z010", "00");
            _z012 = setNameMst("Z012", "00");
            _isSeireki = _z012.equals("2") ? true : false;
            _schoolName = setSchoolName();
        }

        private String getInstate(final String[] selectData) {
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("('");
            for (int i = 0; i < selectData.length; i++) {
                stb.append(sep + selectData[i]);
                sep = "','";
            }
            stb.append("')");

            return stb.toString();
        }

        private String setNameMst(final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            _db2.query(getNameMst(_ctrlYear, namecd1, namecd2));
            ResultSet rs = _db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } finally {
                _db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private List getTestList() throws SQLException {
            final List retList = new ArrayList();
            final String testSql = getTestSql();
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                ps = _db2.prepareStatement(testSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String kindCd = rs.getString("TESTKINDCD");
                    final String itemCd = rs.getString("TESTITEMCD");
                    final String name = rs.getString("TESTITEMNAME");
                    final String countFlg = rs.getString("COUNTFLG");
                    final String semeDetail = rs.getString("SEMESTER_DETAIL");
                    final TestItem testItem = new TestItem(semester, kindCd, itemCd, name, countFlg, semeDetail);
                    retList.add(testItem);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retList;
        }

        private String getTestSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW ");
            stb.append(" WHERE ");
            stb.append("     YEAR     = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER || TESTKINDCD || TESTITEMCD <= '" + _semester + _testCd + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD <> '9900' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER, ");
            stb.append("     TESTKINDCD, ");
            stb.append("     TESTITEMCD ");

            return stb.toString();
        }

        String getSeme() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        String getRankAvgField() {
            return "2".equals(_output) ? "AVG_" : "";
        }

        String changePrintYear() {
            if (_isSeireki) {
                return _param._ctrlYear + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度";
            }
        }

        String changePrintDate() {
            if (_isSeireki) {
                return _param._ctrlYear + "年度" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate);
            } else {
                return KNJ_EditDate.h_format_JP(_param._ctrlDate);
            }
        }

        private String setSchoolName() {
            
            String schoolkind = null;
            final String sqlSchoolkind = " SELECT * FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _gradeHrClass.substring(0, 2) + "' ";
            try {
                PreparedStatement ps = _db2.prepareStatement(sqlSchoolkind);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolkind = rs.getString("SCHOOL_KIND");
                }
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            } catch (SQLException e) {
                log.error("exception!", e);
            }

            final String certifKindCd = "J".equals(schoolkind) ? "110" : "H".equals(schoolkind) ? "109" : null;
            String schoolName = null;
            final String sqlCertif = " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            try {
                PreparedStatement ps = _db2.prepareStatement(sqlCertif);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolName = rs.getString("SCHOOL_NAME");
                }
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return schoolName;
        }
    }

    class TestItem {
        final String _semester;
        final String _kindCd;
        final String _itemCd;
        final String _name;
        final String _countFlg;
        final String _semeDetail;
        final String _key;
        /**
         * コンストラクタ。
         */
        public TestItem(
                final String semester,
                final String kindCd,
                final String itemCd,
                final String name,
                final String countFlg,
                final String semeDetail
        ) {
            _semester = semester;
            _kindCd = kindCd;
            _itemCd = itemCd;
            _name = name;
            _countFlg = countFlg;
            _semeDetail = semeDetail;
            _key = semester + _kindCd + _itemCd;
        }
    }
}

// eof
