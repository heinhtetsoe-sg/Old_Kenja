// kanji=漢字
/*
 * $Id: 32732207ba9ed9369babbd7daad67955f1272c37 $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 32732207ba9ed9369babbd7daad67955f1272c37 $
 */
public class KNJD192H {

    private static final Log log = LogFactory.getLog("KNJD192V.class");

    private Param _param;

    private static final String SPECIAL_ALL = "999";

    private static final String SSEMESTER = "1";
    private static final String SEMEALL = "9";

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

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
            hasData = printMain(db2, svf);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    private List getPageList(final List studentList, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String befGradeClass = "";
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (null == current || current.size() >= max || !befGradeClass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }
        return rtn;
    }

    private List getPageList2(final List studentList, final int max) {
        final List hrClasses = new ArrayList();
        final List attendnos = new ArrayList();
        final Map hrClassAttendnoStudent = new HashMap();
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (!hrClasses.contains(student._hrClass)) {
                hrClasses.add(student._hrClass);
            }
            if (!attendnos.contains(student._attendNo)) {
                attendnos.add(student._attendNo);
            }
            hrClassAttendnoStudent.put(student._hrClass + student._attendNo, student);
        }
        Collections.sort(attendnos);

        final List hrGroupList = new ArrayList();
        List hrGroup = null;
        for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
            final String hrClass = (String) it.next();
            if (null == hrGroup || hrGroup.size() >= max) {
                hrGroup = new ArrayList();
                hrGroupList.add(hrGroup);
            }
            hrGroup.add(hrClass);
        }

        final List rtn = new ArrayList();
        for (final Iterator ithp = hrGroupList.iterator(); ithp.hasNext();) {
            final List hrGroup_ = (List) ithp.next();
            for (final Iterator itatt = attendnos.iterator(); itatt.hasNext();) {
                final String attendno = (String) itatt.next();
                log.debug(" set page attendno = " + attendno);
                final List pageElemList = new ArrayList();
                rtn.add(pageElemList);
                for (final Iterator ith = hrGroup_.iterator(); ith.hasNext();) {
                    final String hrClass = (String) ith.next();
                    pageElemList.add(hrClassAttendnoStudent.get(hrClass + attendno));
                }
            }
        }
        return rtn;
    }

    private static boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASS9.equals(subclassCd) || "99999A".equals(subclassCd) || "99999B".equals(subclassCd);
    }

    private static String getSubclassCd(final ResultSet rs, final Param param) throws SQLException {
        final String subclassCd;
        if ("1".equals(param._useCurriculumcd) && !isSubclassAll(rs.getString("SUBCLASSCD"))) {
            subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
        } else {
            subclassCd = rs.getString("SUBCLASSCD");
        }
        return subclassCd;
    }

    private static String roundHalfUp(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List studentList = Student.getStudentList(db2, _param);

        Student.setMapTestSubclassAvg(db2, _param, studentList);

        TestScore.setScore(db2, _param, studentList);

        TestScore.setRank(db2, _param, studentList);

        return studentList;
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;
        final List studentListAll = getStudentList(db2);
        log.debug(" studentList size = " + studentListAll.size());

        final String form = "2".equals(_param._form) ? "KNJD192H_2.frm" : "KNJD192H.frm";
        final int max = 5;

        final List pageList;
        if ("1".equals(_param._sameAttendnoPage)) {
            pageList = getPageList2(studentListAll, max);
        } else {
            pageList = getPageList(studentListAll, max);
        }
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List studentList = (List) it.next();

            svf.VrSetForm(form, 1);

            for (int gyo = 1; gyo <=  studentList.size(); gyo++) {
                final Student student = (Student) studentList.get(gyo - 1);

                setPrintOut(svf, student, gyo);

                hasData = true;
            }
            svf.VrEndPage();
        }
        return hasData;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int gyo) {
        if (null == student) {
            return;
        }
        svf.VrsOutn("NENDO", gyo, _param.changePrintYear());
        svf.VrsOutn("SEMESTER", gyo, _param._semesterName);
        svf.VrsOutn("TESTNAME", gyo, _param._testName);
        svf.VrsOutn("HR_NAME", gyo, student._hrName + "(" + student._attendNo + ")");
        svf.VrsOutn("NAME", gyo, student._name);

        int j = 1;
        final List scoreList = new ArrayList();
        for (final Iterator its = student._testSubclass.keySet().iterator(); its.hasNext();) {
            final String subclassCd = (String) its.next();
            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
//            final String credit = null != testScore._credit ? "(" + testScore._credit + ")" : "";
//            svf.VrsOutn("SUBCLASS" + j, gyo, StringUtils.defaultString(testScore._name) + credit);
            final int nlen = KNJ_EditEdit.getMS932ByteLength(testScore._name);
            if (nlen > 6 ) {
                String[] nameStrs = KNJ_EditEdit.get_token(testScore._name, 6, 2);
                svf.VrsOutn("SUBCLASS" + j + "_2", gyo, nameStrs[0]);
                if (nameStrs[1] != null) svf.VrsOutn("SUBCLASS" + j + "_3", gyo, nameStrs[1]);
            } else {
                svf.VrsOutn("SUBCLASS" + j, gyo, testScore._name);
            }
            if (student.isPrintSubclassScoreAvg(subclassCd, _param)) {
                svf.VrsOutn("SCORE" + j, gyo, testScore._score);
                scoreList.add(testScore._score);
            }

            j++;
        }

        if (student._hasScore) {
        	if (_param._isOsakatoin) {
        		svf.VrsOutn("TOTAL_SCORE", gyo, calcSum(scoreList));
        	} else {
        		svf.VrsOutn("TOTAL_SCORE", gyo, student._testAll._score);
        	}
        }
    }

    private String calcSum(final List scoreList) {
    	if (scoreList.isEmpty()) {
    		return null;
    	}
    	BigDecimal sum = new BigDecimal(0);
    	int count = 0;
    	for (final Iterator it = scoreList.iterator(); it.hasNext();) {
    		final String score = (String) it.next();
    		if (NumberUtils.isNumber(score)) {
    			sum = sum.add(new BigDecimal(score));
    			count += 1;
    		}
    	}
		return count == 0 ? null : sum.toString();
	}

	private static class Student {
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
        final boolean _majorHas1Hr;
        final Map _testSubclass;
        final Map _testSubclassAvg;
        final Map _recordChkfinSubclass;
        final TestScore _testAll;
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
                final String courseCodeName,
                final boolean majorHas1Hr
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
            _majorHas1Hr = majorHas1Hr;
            _testSubclass = new TreeMap();
            _testSubclassAvg = new TreeMap();
            _recordChkfinSubclass = new TreeMap();
            _testAll = new TestScore();
            _testAll._score = "";
        }

        /** 指定科目コードの平均点を表示するか */
        public boolean isPrintSubclassScoreAvg(final String subclassCd, final Param param) {
            return true;
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
//                log.debug("getStudentInfoSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"),
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
                                                  rs.getString("COURSECODENAME"),
                                                  "1".equals(rs.getString("MAJOR_HAS_1_HR")));
                    studentList.add(student);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return studentList;
        }

        private static String getStudentInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAJOR_HR_COUNT AS ( ");
            stb.append(" SELECT ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     COUNT(DISTINCT HR_CLASS) AS MAJOR_HR_COUNT "); // 学科ごとのHR数
            stb.append(" FROM ");
            stb.append("     V_SCHREG_INFO VSCH ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD ");
            stb.append(" ) ");
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
            stb.append("     L2.COURSECODENAME, ");
            stb.append("     CASE WHEN VALUE(MAJOR_HR_COUNT, 0) = 1 THEN '1' END AS MAJOR_HAS_1_HR ");
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
            stb.append("     LEFT JOIN MAJOR_HR_COUNT L5 ON L5.COURSECD = VSCH.COURSECD AND L5.MAJORCD = VSCH.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._schregSemester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS IN " + SQLUtils.whereIn(true, param._selectData) + " ");
            } else {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("     AND VSCH.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectData) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }

        private static void setMapTestSubclassAvg(final DB2UDB db2, final Param param, final List studentList) throws SQLException {

            final Set grSet = new HashSet();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String key = student._grade + student._hrClass;
                grSet.add(key);
            }

            final Map grAvgMap = new HashMap();

            final String sql = TestScore.getAverageSql(param, param._year, param._semester, "2", param._testcd);
            PreparedStatement ps = db2.prepareStatement(sql);

            // 学級の平均
            for (final Iterator it = grSet.iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final Map avgMap = new HashMap();
                grAvgMap.put(key, avgMap);
                ResultSet rs = null;
                try {
                    ps.setString(1, key.substring(0, 2));
                    ps.setString(2, key.substring(2));
                    ps.setString(3, "00000000");
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        final String avg = rs.getString("AVG");
                        avgMap.put(subclassCd, avg);
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            }
            DbUtils.closeQuietly(ps);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String key = student._grade + student._hrClass;
                final Map avgMap = (Map) grAvgMap.get(key);
                if (null != avgMap) {
                    student._testSubclassAvg.putAll(avgMap);
                }
            }
        }
    }

    private static class TestScore {
        String _name;
        String _combinedSubclasscd;
        String _score;
        String _avg;
        String _avgHr;
        String _avgChair;
        String _rank;
        String _rankHr;
        String _rankChair;
        String _credit;
        String _average;
        String _slumpScore;
        String _slumpMark;
        String _sidouinput;
        String _sidouinputinf;
        String _passScore;
        int _cnt;
        int _cntHr;

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _avg
                    + " 席次：" + _rank;
        }

        private static void setRank(final DB2UDB db2, final Param param, final List studentList) throws SQLException {

            final String sql = getRecordRankTestAppointSql(param._year, param._semester, param._testcd);
            PreparedStatement ps = null;
            try {
//                log.debug(rankSql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    int subclassCount = 0;
                    boolean hasScore = false;
                    int totalScore = 0;
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            hasScore = true;
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                            testScore._score = rs.getString("SCORE");

                            if (!"欠".equals(testScore._score)) {
                                totalScore += Integer.parseInt(testScore._score);
                            }
                            subclassCount += 1;
                        }
                        if (subclassCd.equals(SUBCLASS9)) {
                            student._testAll._score = rs.getString("SCORE");
                            student._testAll._average = roundHalfUp(rs.getBigDecimal("AVG"));
                        }
                    }
                    DbUtils.closeQuietly(rs);
                    student._hasScore = hasScore;
                    if (hasScore) {
                        student._averageScore = divide(totalScore, subclassCount);
                    }
                }

            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 席次データの取得
         * @return sql
         */
        public static String getRecordRankTestAppointSql(
                final String year,
                final String semester,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static void setScore(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            ResultSet rs;
            PreparedStatement ps;
            String sql = getRecordScoreTestAppointSql(param);
            ps = db2.prepareStatement(sql);

            final TreeSet printSubclassSet = new TreeSet();
            final TreeSet notPrintAttendSubclassSet = new TreeSet();
            final TreeSet notPrintCombinedSubclassSet = new TreeSet();
            final TreeSet notPrintD046SubclassSet = new TreeSet();

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = getSubclassCd(rs, param);
                    final String subclassabbv = rs.getString("SUBCLASSABBV");

                    String strKetu = "";
                    if (student._testSubclassAvg.containsKey(subclassCd)) {
                        strKetu = "欠";
                    }
                    if (student._testSubclassAvg.containsKey(subclassCd)) {
                        if (param._isNoPrintMoto && param._attendSubclasscdList.contains(subclassCd)) {
                            notPrintAttendSubclassSet.add(subclassCd + " " + subclassabbv);
                            continue;
                        }
                        if (param._isNoPrintSaki && param._combinedSubclasscdList.contains(subclassCd)) {
                            notPrintCombinedSubclassSet.add(subclassCd + " " + subclassabbv);
                            continue;
                        }
                        if (param._d046List.contains(subclassCd)) {
                            notPrintD046SubclassSet.add(subclassCd + " " + subclassabbv);
                            continue;
                        }
                        printSubclassSet.add(subclassCd + " " + subclassabbv);

                        final TestScore testScore = new TestScore();
                        testScore._score = strKetu;
                        testScore._name = subclassabbv;
                        student._testSubclass.put(subclassCd, testScore);
                    }
                }
                DbUtils.closeQuietly(rs);
            }
            DbUtils.closeQuietly(ps);
            log.info(" 表示科目 " + printSubclassSet);
            if (notPrintAttendSubclassSet.size() > 0) {
                log.info(" 合併元 表示無し " + notPrintAttendSubclassSet);
            }
            if (notPrintCombinedSubclassSet.size() > 0) {
                log.info(" 合併先 表示無し " + notPrintCombinedSubclassSet);
            }
            if (notPrintD046SubclassSet.size() > 0) {
                log.info(" D046 表示無し " + notPrintD046SubclassSet);
            }
        }

        private static String getRecordScoreTestAppointSql(
                final Param param
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     L1.SUBCLASSNAME, ");
            stb.append("     L1.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD = L1.CLASSCD ");
                stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
                stb.append("     AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '01' AND '90') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

//        private static void setChairStd(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
//            ResultSet rs;
//            PreparedStatement ps;
//            String sql = getChairStdSql(param);
//            ps = db2.prepareStatement(sql);
//
//            for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                final Student student = (Student) it.next();
//
//                ps.setString(1, student._schregno);
//                log.debug(" schregno = " + student._schregno);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String subclassCd = getSubclassCd(rs, param);
//                    String strKetu = "";
//                    if (student._testSubclassAvg.containsKey(subclassCd)) {
//                        strKetu = "欠";
//                    }
//                    log.debug(" contain " + subclassCd + " = " + student._testSubclassAvg.containsKey(subclassCd));
//                    if (student._testSubclassAvg.containsKey(subclassCd)) {
//                        final TestScore testScore = new TestScore();
//                        testScore._score = strKetu;
//                        testScore._name = rs.getString("SUBCLASSABBV");
//                        testScore._combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");
//                        student._testSubclass.put(subclassCd, testScore);
//                    }
//                }
//                DbUtils.closeQuietly(rs);
//            }
//            DbUtils.closeQuietly(ps);
//        }
//
//        private static String getChairStdSql(final Param param) {
//            final StringBuffer stb = new StringBuffer();
//
//            stb.append(" WITH COMBINED_SUBCLASS AS (");
//            stb.append(" SELECT DISTINCT ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     T1.COMBINED_CLASSCD, ");
//                stb.append("     T1.COMBINED_SCHOOL_KIND, ");
//                stb.append("     T1.COMBINED_CURRICULUM_CD, ");
//            }
//            stb.append("     T1.COMBINED_SUBCLASSCD ");
//            stb.append(" FROM ");
//            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
//            stb.append(" WHERE ");
//            stb.append("     YEAR = '" + param._year + "' ");
//            stb.append(" )");
//
//            stb.append(" SELECT DISTINCT ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     T1.CLASSCD, ");
//                stb.append("     T1.SCHOOL_KIND, ");
//                stb.append("     T1.CURRICULUM_CD, ");
//            }
//            stb.append("     T1.SUBCLASSCD, ");
//            stb.append("     L2.COMBINED_SUBCLASSCD, ");
//            stb.append("     L1.SUBCLASSABBV ");
//            stb.append(" FROM ");
//            stb.append("     CHAIR_DAT T1 ");
//            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     AND T1.CLASSCD = L1.CLASSCD ");
//                stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
//                stb.append("     AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
//            }
//            stb.append("     LEFT JOIN COMBINED_SUBCLASS L2 ON L2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     AND T1.CLASSCD = L2.COMBINED_CLASSCD ");
//                stb.append("     AND T1.SCHOOL_KIND = L2.COMBINED_SCHOOL_KIND ");
//                stb.append("     AND T1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ");
//            }
//            stb.append("     , CHAIR_STD_DAT T2 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year + "' AND ");
//            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
//            stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
//            stb.append("     T2.YEAR = T1.YEAR AND ");
//            stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
//            stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
//            stb.append("     T2.SCHREGNO = ? ");
//            stb.append(" ORDER BY ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     T1.CLASSCD, ");
//                stb.append("     T1.SCHOOL_KIND, ");
//                stb.append("     T1.CURRICULUM_CD, ");
//            }
//            stb.append("     T1.SUBCLASSCD ");
//            return stb.toString();
//        }

        /**
         * 平均データの取得
         * @return sql
         */
        public static String getAverageSql(
                final Param param,
                final String year,
                final String semester,
                final String avgDiv,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");
            if (param._isBunkyo) {
                stb.append("     AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("               WHERE YEAR = '" + year + "' ");
                stb.append("                 AND ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("                 AND ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                 AND ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                 AND ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
            } else if (param._isOsakatoin) {
                stb.append("     AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("               WHERE YEAR = '" + year + "' ");
                stb.append("                 AND COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("                 AND COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                 AND COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                 AND COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ");
            }

            return stb.toString();
        }

        private static String divide(final int v1, final int v2) {
            return new BigDecimal(v1).divide(new BigDecimal(v2), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 71273 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _categoryIsClass;
        final String _testcd;
        final String _testName;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _sameAttendnoPage;
        final String[] _selectData;
        final String _z010;
        final boolean _isBunkyo;
        final boolean _isOsakatoin;
        final boolean _isSeireki;
        final String _schregSemester;
        private String _semesterName;
        private boolean _isNoPrintMoto;
        private boolean _isNoPrintSaki;
        private List _attendSubclasscdList = Collections.EMPTY_LIST;
        private List _combinedSubclasscdList = Collections.EMPTY_LIST;
        private List _d046List = Collections.EMPTY_LIST;
        final String _form;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
//        /** 「考査を実施しない講座は平均点を表示しない」を処理するか */
//        final String _knjd192AcheckNoExamChair;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _testcd = request.getParameter("SUB_TESTCD");
            _semester = request.getParameter("SEMESTER");
             _schregSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _sameAttendnoPage = request.getParameter("SAME_ATTENDNO_PAGE");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組

            _z010 = setNameMst(db2, "NAME1", "Z010", "00");
            _isBunkyo = "bunkyo".equals(_z010);
            _isOsakatoin = "osakatoin".equals(_z010);
            _isSeireki = "2".equals(setNameMst(db2, "NAME1", "Z012", "01"));

//            _knjd192AcheckNoExamChair = request.getParameter("knjd192AcheckNoExamChair");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            if (!_isBunkyo && !_isOsakatoin) {
                setNameMstD016(db2);
                setNameMstD021(db2);
                setAttendCombinedSubclass(db2);
                setNameMstD046(db2);
            }
            _form = request.getParameter("FORM");
        }

        public boolean isGakkimatsu() {
            if (null != _testcd && _testcd.length() >= 3 && "99".equals(_testcd.substring(1, 3))) {
                return true;
            }
            return false;
        }

        private String getTestName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String seme = rs.getString("SEMESTER");
                    if (_semester.equals(seme)) {
                        _semesterName = rs.getString("SEMESTERNAME");
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setAttendCombinedSubclass(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            TreeSet attendSubclasscdSet = new TreeSet();
            TreeSet combinedSubclasscdSet = new TreeSet();
            try {
                final String sql = "SELECT * FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String attendSubclasscd = rs.getString("ATTEND_CLASSCD") + "-" + rs.getString("ATTEND_SCHOOL_KIND") + "-" + rs.getString("ATTEND_CURRICULUM_CD") + "-" + rs.getString("ATTEND_SUBCLASSCD");
                    final String combinedSubclasscd = rs.getString("COMBINED_CLASSCD") + "-" + rs.getString("COMBINED_SCHOOL_KIND") + "-" + rs.getString("COMBINED_CURRICULUM_CD") + "-" + rs.getString("COMBINED_SUBCLASSCD");
                    attendSubclasscdSet.add(attendSubclasscd);
                    combinedSubclasscdSet.add(combinedSubclasscd);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _attendSubclasscdList = new ArrayList(attendSubclasscdSet);
            _combinedSubclasscdList = new ArrayList(combinedSubclasscdSet);
            log.info("合併元科目=" + _attendSubclasscdList);
            log.info("合併先科目=" + _combinedSubclasscdList);
        }

        private void setNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String namespare1 = setNameMst(db2, "NAMESPARE1", "D016", "01");
            if ("Y".equals(namespare1) || isGakkimatsu()) {
                _isNoPrintMoto = true;
            }
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void setNameMstD021(final DB2UDB db2) {
            _isNoPrintSaki = false;
            final String namespare1 = setNameMst(db2, "NAMESPARE3", "D021", "01");
            if ("Y".equals(namespare1)) { //  && !isGakkimatsu()) {
                _isNoPrintSaki = true;
            }
            log.info("(名称マスタD021):先科目を表示しない = " + _isNoPrintSaki);
        }


        private void setNameMstD046(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
//            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
//                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
//                sql.append(" SELECT CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
//                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '008' AND " + field + " = '1'  ");
//            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D046' AND " + field + " = '1'  ");
//            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d046List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d046List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info(" d046 = " + _d046List);
        }

        private String setNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNameMst(_year, namecd1, namecd2);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString(field);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
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

        private String changePrintYear() {
            if (_isSeireki) {
                return _year + "年度";
            } else {
                return KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            }
        }
    }
}

// eof
