// kanji=漢字
/*
 * $Id: 324c78f9a340201d4de24d74f42fa56aa69c71bf $
 *
 * 作成日: 2015/02/05 15:38:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2015-2019 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfo;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 324c78f9a340201d4de24d74f42fa56aa69c71bf $
 */
public class KNJD192G {

    private static final Log log = LogFactory.getLog(KNJD192G.class);

    Param _param;

    private static final String SCORE = "SCORE";
    private static final String AVG = "AVG";
    private static final String RANK = "RANK";
    private static final String RANK2 = "RANK2";

    private static final String SUBCLASSALL = "999999";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(response, db2, svf);

        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    /**
     * @param response
     * @param db2
     */
    private boolean printMain(final HttpServletResponse response, final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        final List studentList = getStudentList(db2);
        final List printDataList = getPrintData(db2, studentList);
        final Map courseWeightingSubclassCdListMap = getCourseWeightingSubclassCdListMap(db2);
        setHead(svf);
        int cnt = 0;
        String befGradeClass = "";
        for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (hasData && !befGradeClass.equals(student._grade + student._hrClass)) {
                cnt = pageChange(svf);
            }

            cnt++;
            setPrintOut(svf, student, cnt, courseWeightingSubclassCdListMap);

            if (cnt == 5) {
                cnt = pageChange(svf);
            }

            befGradeClass = student._grade + student._hrClass;
            hasData = true;
        }
        if (cnt > 0) {
            svf.VrEndPage();
        }
        return hasData;
    }

    private int pageChange(final Vrw32alp svf) {
        svf.VrEndPage();
        return 0;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int fieldNo, final Map courseWeightingSubclassCdListMap) {
        log.debug(student);
        svf.VrsOutn("NENDO", fieldNo, _param.changePrintYear());
        svf.VrsOutn("SEMESTER", fieldNo, _param._semesterName);
        svf.VrsOutn("TESTNAME", fieldNo, _param._testName);
        final String hrName = student._hrName + "(" + student._attendNo + ")";
        svf.VrsOutn("HR_NAME", fieldNo, hrName);
        svf.VrsOutn("NAME", fieldNo, student._name);
        
        final String gradeCourse = student._grade + student._courseCd + student._majorCd + student._courseCode;
        
        final List notTargetSubclassCdList;
        if ("9900".equals(_param._kindItem)) {
            notTargetSubclassCdList = Collections.EMPTY_LIST; // getMappedList(getMappedMap(courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
        } else {
            // [学期末、学年末]以外は先を表示しない
            notTargetSubclassCdList = getMappedList(getMappedMap(courseWeightingSubclassCdListMap, gradeCourse), "COMBINED_SUBCLASS");
        }

        int fieldCnt = 1;
        for (final Iterator itSubclass = student._testSubclass.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            if (notTargetSubclassCdList.contains(subclassCd)) {
                //log.debug(" not print subclass " + subclassCd);
                continue;
            }
            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
            svf.VrsOutn("SUBCLASS" + fieldCnt, fieldNo, testScore._name);
            svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
            fieldCnt++;
            log.debug(testScore);
        }

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", fieldNo, student._testAll._score);
        }
    }

    private void setHead(final Vrw32alp svf) {
        svf.VrSetForm("KNJD192G.frm", 1);
    }

    private List getPrintData(final DB2UDB db2, final List studentList) throws SQLException {
        List printDataList = new ArrayList();
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            setMapTestSubclassAvg(db2, student);
            setScore(db2, student);
            setRank(db2, student);
            printDataList.add(student);
        }
        return printDataList;
    }

    private String getSubclasscd(final ResultSet rs) throws SQLException {
        final String subclassCd = rs.getString("SUBCLASSCD");
        if (SUBCLASSALL.equals(subclassCd)) {
            return subclassCd;
        }
        if ("1".equals(_param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclassCd;
        }
        return subclassCd;
    }

    private void setMapTestSubclassAvg(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        // 学級の平均
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, course, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, course, _param._kindItem);
            }
            log.debug(" rankSql = " + rankSql);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                final String avg = rankRs.getString("AVG");
                student._testSubclassAvg.put(subclassCd, avg);
            }
        } finally {
            DbUtils.closeQuietly(rankRs);
            db2.commit();
        }
    }

    private void setScore(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet scoreRs = null;
        try {
            final String scoreSql = getReportCardInfo.getRecordScoreTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._scoreDiv, _param._kindItem, _param._useCurriculumcd);
            ps = db2.prepareStatement(scoreSql);
            scoreRs = ps.executeQuery();
            while (scoreRs.next()) {
                final String subclassCd = getSubclasscd(scoreRs);
                if (student._testSubclassAvg.containsKey(subclassCd)) {
                    final TestScore testScore = new TestScore("欠");
                    testScore._name = scoreRs.getString("SUBCLASSABBV");
                    student._testSubclass.put(subclassCd, testScore);
                }
            }
        } finally {
            db2.commit();
        }
    }

    private void setRank(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        try {
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordRankVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordRankTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem);
            }
            log.debug(rankSql);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            boolean hasScore = false;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                if (student._testSubclass.containsKey(subclassCd)) {
                    hasScore = true;
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._score = rankRs.getString("SCORE");
                    testScore._rankGrade = rankRs.getString(_param.getGradeRankField());
                    testScore._rankCourse = rankRs.getString(_param.getCourseRankField());
                    testScore._rankHr = rankRs.getString(_param.getClassRankField());
                }
                if (subclassCd.equals(SUBCLASSALL)) {
                    student._testAll._score = rankRs.getString("SCORE");
                    student._testAll._rankGrade = rankRs.getString(_param.getGradeRankField());
                    student._testAll._rankCourse = rankRs.getString(_param.getCourseRankField());
                    student._testAll._rankHr = rankRs.getString(_param.getClassRankField());
                    if (null != rankRs.getString("AVG")) {
                        student._averageScore = new BigDecimal(rankRs.getDouble("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
            }
            student._hasScore = hasScore;

        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List rtnStudent = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentInfoSql();
            log.debug(" student sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
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
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnStudent;
    }

    
    private Map getCourseWeightingSubclassCdListMap(final DB2UDB db2) {
        final Map courseWeightingSubclassCdListMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String flg = "9900".equals(_param._kindItem) ? "2" : "1";
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            } else {
                stb.append("       T1.ATTEND_SUBCLASSCD ");
            }
            stb.append("       , ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            } else {
                stb.append("       T1.COMBINED_SUBCLASSCD ");
            }
            stb.append("   FROM ");
            stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.FLG = '" + flg + "' ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "ATTEND_SUBCLASS").add(rs.getString("ATTEND_SUBCLASSCD"));
                getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "COMBINED_SUBCLASS").add(rs.getString("COMBINED_SUBCLASSCD"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return courseWeightingSubclassCdListMap;
    }
    
    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
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
        final Map _testSubclass;
        final Map _testSubclassAvg;
        TestScore _testAll;
        boolean _hasScore;
        /** 個人の平均点 */
        String _averageScore;
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
            _testSubclass = new TreeMap();
            _testAll = new TestScore("");
            _testSubclassAvg = new TreeMap();
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
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
        stb.append("     VSCH.YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._semester)) {
            stb.append("     AND VSCH.SEMESTER = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("     AND VSCH.SEMESTER = '" + _param._semester + "' ");
        }
        if (_param.isClass()) {
            stb.append("     AND VSCH.GRADE = '" + _param._gradeHrclass + "' ");
            stb.append("     AND VSCH.GRADE || '-' || VSCH.HR_CLASS IN " + SQLUtils.whereIn(true, _param._selectData) + " ");
        } else {
            stb.append("     AND VSCH.GRADE || '-' || VSCH.HR_CLASS = '" + _param._gradeHrclass + "' ");
            stb.append("     AND VSCH.SCHREGNO IN " + SQLUtils.whereIn(true, _param._selectData) + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO ");

        return stb.toString();
    }

    class TestScore {
        String _name;
        String _score;
        String _avgGrade;
        String _avgCourse;
        String _avgHr;
        String _rankGrade;
        String _rankCourse;
        String _rankHr;
        String _slump;
        String _passScore;
        String _assessLevelGrade;
        String _assessLevelCourse;

        public TestScore(String score) {
            _score = score;
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _avgGrade
                    + " 席次：" + _rankGrade;
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
    }

    private class Param {
        final String _year;
        final String _categoryIsClass;
        final String _kindItem;
        final String _scoreDiv;
        final String _testName;
        final String _semester;
        final String _ctrlSeme;
        final String _semesterName;
        final String _gradeHrclass;
        final String _rankDiv;
        final String[] _selectData;
        final String _useCurriculumcd; //教育課程コード
        String _z010 = "";
        String _z012 = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _kindItem = request.getParameter("TESTCD");
            _scoreDiv = _kindItem.equals("9900") ? "00" : "01";
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _rankDiv = request.getParameter("RANK_DIV");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _z010 = setNameMst(db2, "Z010", "00");
            _z012 = setNameMst(db2, "Z012", "01");
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND TESTKINDCD || TESTITEMCD = '" + _kindItem + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(getNameMst(_year, namecd1, namecd2));
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } finally {
                db2.commit();
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

        private boolean isClass() {
            return _categoryIsClass.equals("1");
        }

        private String getGradeRankField() {
            return "1".equals(_rankDiv) ? "GRADE_RANK" : "2".equals(_rankDiv) ? "GRADE_AVG_RANK" : "GRADE_DEVIATION_RANK";
        }

        private String getCourseRankField() {
            return "1".equals(_rankDiv) ? "COURSE_RANK" : "2".equals(_rankDiv) ? "COURSE_AVG_RANK" : "COURSE_DEVIATION_RANK";
        }

        private String getClassRankField() {
            return "1".equals(_rankDiv) ? "CLASS_RANK" : "2".equals(_rankDiv) ? "CLASS_AVG_RANK" : "CLASS_DEVIATION_RANK";
        }

        private String changePrintYear() {
            return _param._year + "年度";
        }

        private boolean isVdat() {
            return "9".equals(_semester) && "9900".equals(_kindItem);
        }
    }
}
 // KNJD192

// eof
