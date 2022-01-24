//kanji=漢字
/*
 * $Id: 04472b16f6fbc0b158f07d97a58b113331a9a131 $
 *
 * 作成日: 2008/05/19 15:38:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 04472b16f6fbc0b158f07d97a58b113331a9a131 $
 */
public class KNJH564G {

    private static final Log log = LogFactory.getLog(KNJH564G.class);

    private Param _param;

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASSALL = "999999";
    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HR = "02";
    private static final String RANK_DIV_COURSE = "03";
    private static final String RANK_DIV_MAJOR = "04";
    private static final String RANK_DIV_COURSEGROUP = "05";
    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_CLASS = "02";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_MAJOR = "04";
    private static final String AVG_DIV_COURSEGROUP = "05";
    private static final String AVG_DATA_SCORE = "1";

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
        } catch (final Exception e) {
            log.fatal("exception!", e);
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
        final List studentList = Student.getStudentList(db2, _param);
        setPrintData(db2, studentList);
        final String form;
        form = "KNJH564G.frm";

        svf.VrSetForm(form, 1);
        int cnt = 0;
        String befGradeClass = "";
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (hasData && !befGradeClass.equals(student._grade + student._hrClass)) {
                svf.VrEndPage();
                cnt = 0;
            }

            cnt++;
            setPrintOut(svf, student, cnt);

            if (cnt == 5) {
                svf.VrEndPage();
                cnt = 0;
            }

            befGradeClass = student._grade + student._hrClass;
            hasData = true;
        }
        if (cnt > 0) {
            svf.VrEndPage();
        }
        return hasData;
    }

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int fieldNo) {
        log.debug(student);
        svf.VrsOutn("NENDO", fieldNo, _param._nendo);
        svf.VrsOutn("SEMESTER", fieldNo, _param._semesterName);
        svf.VrsOutn("TESTNAME", fieldNo, _param._testName);
        final String hrName = student._hrName + "(" + student._attendNo + ")";
        svf.VrsOutn("HR_NAME", fieldNo, hrName);
        svf.VrsOutn("NAME" + (getMS932ByteLength(student._name) > 16 ? "2" : "1"), fieldNo, student._name);

        int fieldCnt = 1;
        for (final Iterator itSubclass = student._testSubclass.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
            svf.VrsOutn("SUBCLASS" + fieldCnt, fieldNo, testScore._name);
            svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
            fieldCnt++;
            log.debug(testScore);
        }

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", fieldNo, student._testAll._score);
            if ("1".equals(_param._notPrintJuni)) {
                // 順位を出力しない
            } else {
                svf.VrsOutn("TOTAL_RANK2", fieldNo, student._testAll._rank);
                svf.VrsOutn("TOTAL_RANK1", fieldNo, student._testAll._rank2);
                svf.VrsOutn("TOTAL_STUDENT2", fieldNo, student._testAll._count);
                svf.VrsOutn("TOTAL_STUDENT1", fieldNo, student._testAll._count2);
            }
            svf.VrsOutn("AVERAGEL_SCORE", fieldNo, String.valueOf(student._averageScore));
            log.debug("個人平均点 = " + student._averageScore);
        }
    }

    private void setPrintData(final DB2UDB db2, final List studentList) throws SQLException {
        TestScore.setRank(db2, studentList, _param);
        TestScore.setRankALL(db2, studentList, _param);
        TestScore.setAvg(db2, studentList, _param);
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
        final String _groupCd;
        final Map _testSubclass;
        private TestScore _testAll;
        private boolean _hasScore;
        /** 個人の平均点 */
        private String _averageScore;
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
                final String groupCd
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
            _groupCd = groupCd;
            _testSubclass = new TreeMap();
            _testAll = new TestScore();
            _testAll._score = "";
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) throws SQLException  {
            final List rtnStudent = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
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
                                                  rs.getString("GROUP_CD"));
                    rtnStudent.add(student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnStudent;
        }

        private static String getStudentInfoSql(final Param param) {
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
            stb.append("     L2.COURSECODENAME, ");
            stb.append("     L5.GROUP_CD ");
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
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT L5 ON L5.YEAR = VSCH.YEAR ");
            stb.append("         AND L5.GRADE = VSCH.GRADE ");
            stb.append("         AND L5.COURSECD = VSCH.COURSECD ");
            stb.append("         AND L5.MAJORCD = VSCH.MAJORCD ");
            stb.append("         AND L5.COURSECODE = VSCH.COURSECODE ");
            stb.append("  ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._semester + "' ");
            if (param.isClass()) {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS IN " + param._selectInstate + " ");
            } else {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("     AND VSCH.SCHREGNO IN " + param._selectInstate + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }
    }

    private static class TestScore {
        String _name;
        String _score;
        String _avg;
        String _avg2;
        String _rank;
        String _count;
        String _rank2;
        String _count2;
        String _slump;
        String _passScore;

        private int getFailValue(final Param param) {
            if (param.isPerfectRecord() && null != _passScore) {
                return Integer.parseInt(_passScore);
            } else if (param.isKetten() && null != param._ketten && !"".equals(param._ketten)) {
                return Integer.parseInt(param._ketten);
            }
            return -1;
        }

        private boolean isKetten(final int score, final Param param) {
            if (param.isRecordSlump()) {
                return "1".equals(_slump);
            } else {
                return score < getFailValue(param);
            }
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _avg;
        }

        public static void setRank(final DB2UDB db2, final List studentList, final Param param) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRankSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._groupCd);
                    ps.setString(2, student._courseCd + student._majorCd + student._courseCode);
                    ps.setString(3, student._schregno);
                    rs = ps.executeQuery();

                    boolean hasScore = false;
                    while (rs.next()) {
                        final String subclassCd = rs.getString("SUBCLASSCD");
                        final String subclassAbbv = rs.getString("SUBCLASS_ABBV");
                        final String score = "03".equals(param._rankDataDiv) ? rs.getString("SCORE0101") : rs.getString("SCORE"); // 偏差値の場合はrank_div = '01', rank_data_div = '01'

                        final TestScore testScore = new TestScore();
                        testScore._score = "欠";
                        testScore._name = subclassAbbv;
                        testScore._score = score;
                        student._testSubclass.put(subclassCd, testScore);

                        hasScore = true;
                    }
                    student._hasScore = hasScore;
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private static String getRankSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH PROFICIENCY_SUBCLASS_REPLACE_COMB AS ( ");
            stb.append("   SELECT ");
            stb.append("       T1.DIV, ");
            stb.append("       T1.COMBINED_SUBCLASSCD, ");
            stb.append("       T1.ATTEND_SUBCLASSCD ");
            stb.append("   FROM ");
            stb.append("       PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' ");
            stb.append("       AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("       AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("       AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            stb.append("       AND T1.GRADE = '" + param._grade + "' ");
            stb.append("       AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
            stb.append("           CASE WHEN T1.DIV = '04' THEN '0' || ? || '0000' ");
            stb.append("           ELSE ? END ");
            stb.append(" ) ");
            stb.append("SELECT");
            stb.append("  T1.proficiency_subclass_cd as subclasscd,");
            stb.append("  L1.SUBCLASS_ABBV,");
            stb.append("  L3.score,");
            stb.append("  L4.score as score0101,");
            stb.append("  T1.score_di");
            stb.append(" FROM proficiency_dat T1 ");
            stb.append(" LEFT JOIN PROFICIENCY_SUBCLASS_MST L1 ON ");
            stb.append("     L1.PROFICIENCY_SUBCLASS_CD=T1.PROFICIENCY_SUBCLASS_CD");
            stb.append(" LEFT JOIN PROFICIENCY_SUBCLASS_REPLACE_COMB L2 ON ");
            stb.append("     L2.ATTEND_SUBCLASSCD=T1.PROFICIENCY_SUBCLASS_CD");
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT L3 ON ");
            stb.append("     L3.year=T1.year AND");
            stb.append("     L3.semester=T1.semester AND");
            stb.append("     L3.proficiencydiv=T1.proficiencydiv AND");
            stb.append("     L3.proficiencycd=T1.proficiencycd AND");
            stb.append("     L3.schregno=T1.schregno AND");
            stb.append("     L3.proficiency_subclass_cd=T1.proficiency_subclass_cd ");
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT L4 ON ");
            stb.append("     L4.year=T1.year AND");
            stb.append("     L4.semester=T1.semester AND");
            stb.append("     L4.proficiencydiv=T1.proficiencydiv AND");
            stb.append("     L4.proficiencycd=T1.proficiencycd AND");
            stb.append("     L4.schregno=T1.schregno AND");
            stb.append("     L4.proficiency_subclass_cd=T1.proficiency_subclass_cd AND");
            stb.append("     L4.rank_data_div='01' AND");
            stb.append("     L4.rank_div='01' ");
            stb.append(" WHERE");
            stb.append("  T1.year='" + param._year + "' AND");
            stb.append("  T1.semester='" + param._semester + "' AND");
            stb.append("  T1.proficiencydiv='" + param._proficiencydiv + "' AND");
            stb.append("  T1.proficiencycd='" + param._proficiencycd + "' AND");
            stb.append("  T1.proficiency_subclass_cd NOT IN ('" + SUBCLASS3 + "', '" + SUBCLASS5 + "', '" + SUBCLASSALL + "') AND");
            stb.append("  L3.rank_data_div ='" + param._rankDataDiv + "' AND");
            if (param.isHr(param._formGroupDiv)) {
                stb.append("  l3.rank_div ='" + RANK_DIV_HR + "' AND");
            } else if (param.isCourse(param._formGroupDiv)) {
                stb.append("  l3.rank_div ='" + RANK_DIV_COURSE + "' AND");
            } else if (param.isMajor(param._formGroupDiv)) {
                stb.append("  l3.rank_div ='" + RANK_DIV_MAJOR + "' AND");
            } else if (param.isCoursegroup(param._formGroupDiv)) {
                stb.append("  l3.rank_div ='" + RANK_DIV_COURSEGROUP + "' AND");
            } else {
                stb.append("  l3.rank_div ='" + RANK_DIV_GRADE + "' AND");
            }
            stb.append("  L2.ATTEND_SUBCLASSCD IS NULL AND");
            stb.append("  T1.schregno=?");
            return stb.toString();
        }

        public static void setRankALL(final DB2UDB db2, final List studentList, final Param param) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRankALLSql(param, param._formGroupDiv);
                //log.debug(sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = rs.getString("SUBCLASSCD");

                        if (subclassCd.equals(SUBCLASSALL)) {
                            student._testAll._score = "03".equals(param._rankDataDiv) ? rs.getString("SCORE0101") : rs.getString("SCORE"); // 偏差値の場合はrank_div = '01', rank_data_div = '01'
                            student._testAll._rank = rs.getString("RANK");
                            student._averageScore = getAvgString("03".equals(param._rankDataDiv) ? rs.getBigDecimal("AVG0101") : rs.getBigDecimal("AVG"));
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // HR順位
            try {
                final String sql = getRankALLSql(param, "2");
                //log.debug(sql);
                ps = db2.prepareStatement(sql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = rs.getString("SUBCLASSCD");

                        if (subclassCd.equals(SUBCLASSALL)) {
                            student._testAll._rank2 = rs.getString("RANK");
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private static String getRankALLSql(final Param param, final String formGroupDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  t1.proficiency_subclass_cd as subclasscd,");
            stb.append("  t1.score,");
            stb.append("  L4.score as score0101,");
            stb.append("  t1.avg,");
            stb.append("  L4.avg as avg0101,");
            stb.append("  t1.rank ");
            stb.append(" FROM proficiency_rank_dat T1 ");
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT L4 ON ");
            stb.append("     L4.year=T1.year AND");
            stb.append("     L4.semester=T1.semester AND");
            stb.append("     L4.proficiencydiv=T1.proficiencydiv AND");
            stb.append("     L4.proficiencycd=T1.proficiencycd AND");
            stb.append("     L4.schregno=T1.schregno AND");
            stb.append("     L4.proficiency_subclass_cd=T1.proficiency_subclass_cd AND");
            stb.append("     L4.rank_data_div='01' AND");
            stb.append("     L4.rank_div='01' ");
            stb.append(" WHERE");
            stb.append("  T1.year='" + param._year + "' AND");
            stb.append("  T1.semester='" + param._semester + "' AND");
            stb.append("  T1.proficiencydiv='" + param._proficiencydiv + "' AND");
            stb.append("  T1.proficiencycd='" + param._proficiencycd + "' AND");
            stb.append("  T1.proficiency_subclass_cd IN ('" + SUBCLASS3 + "', '" + SUBCLASS5 + "', '" + SUBCLASSALL + "') AND");
            stb.append("  t1.rank_data_div ='" + param._rankDataDiv + "' AND");
            if (param.isHr(formGroupDiv)) {
                stb.append("  t1.rank_div ='" + RANK_DIV_HR + "' AND");
            } else if (param.isCourse(formGroupDiv)) {
                stb.append("  t1.rank_div ='" + RANK_DIV_COURSE + "' AND");
            } else if (param.isMajor(formGroupDiv)) {
                stb.append("  t1.rank_div ='" + RANK_DIV_MAJOR + "' AND");
            } else if (param.isCoursegroup(formGroupDiv)) {
                stb.append("  t1.rank_div ='" + RANK_DIV_COURSEGROUP + "' AND");
            } else {
                stb.append("  t1.rank_div ='" + RANK_DIV_GRADE + "' AND");
            }
            stb.append("  t1.schregno=?");
            return stb.toString();
        }

        public static void setAvg(final DB2UDB db2, final List studentList, final Param param) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String[] formGroupDivs = {param._formGroupDiv, "2"};
            for (int i = 0; i < formGroupDivs.length; i++) {
                try {
                    final String formGroupDiv = formGroupDivs[i];
                    final String sql = getAvgSql(param, formGroupDiv);
                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        if (param.isHr(formGroupDiv)) {
                            ps.setString(1, student._hrClass);
                        } else if (param.isCourse(formGroupDiv)) {
                            ps.setString(1, student._courseCd + student._majorCd + student._courseCode);
                        } else if (param.isMajor(formGroupDiv)) {
                            ps.setString(1, student._courseCd + student._majorCd + "0000");
                        } else if (param.isCoursegroup(formGroupDiv)) {
                            ps.setString(1, student._groupCd);
                        } else {
                        }

                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String subclassCd = rs.getString("SUBCLASSCD");
                            if (i == 1) {
                                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                                    final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                                    testScore._avg2 = getAvgString(rs.getBigDecimal("AVG"));
                                }
                                if (SUBCLASSALL.equals(subclassCd)) {
                                    student._testAll._avg2 = getAvgString(rs.getBigDecimal("AVG"));
                                    student._testAll._count2 = rs.getString("COUNT");
                                }
                            } else {
                                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                                    final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                                    testScore._avg = getAvgString(rs.getBigDecimal("AVG"));
                                }
                                if (SUBCLASSALL.equals(subclassCd)) {
                                    student._testAll._avg = getAvgString(rs.getBigDecimal("AVG"));
                                    student._testAll._count = rs.getString("COUNT");
                                }
                            }
                        }
                        DbUtils.closeQuietly(rs);
                    }
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
        }

        private static String getAvgString(final BigDecimal avg) {
            if (avg == null) {
                return null;
            }
            return avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static String getAvgSql(final Param param, final String formGroupDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     AVG, ");
            stb.append("     COUNT ");
            stb.append(" from ");
            stb.append("     PROFICIENCY_AVERAGE_DAT T1 ");
            stb.append(" where ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     and T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     and T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     and T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            stb.append("     and T1.DATA_DIV = '" + param._avgDataDiv + "' ");
            if (param.isHr(formGroupDiv)) {
                stb.append("     and T1.AVG_DIV = '" + AVG_DIV_CLASS + "' ");
                stb.append("     and T1.GRADE = '" + param._grade + "' ");
                stb.append("     and T1.HR_CLASS = ? ");
                stb.append("     and T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
            } else if (param.isCourse(formGroupDiv)) {
                stb.append("     and T1.AVG_DIV = '" + AVG_DIV_COURSE + "' ");
                stb.append("     and T1.GRADE = '" + param._grade + "' ");
                stb.append("     and T1.HR_CLASS = '000' ");
                stb.append("     and T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");
            } else if (param.isMajor(formGroupDiv)) {
                stb.append("     and T1.AVG_DIV = '" + AVG_DIV_MAJOR + "' ");
                stb.append("     and T1.GRADE = '" + param._grade + "' ");
                stb.append("     and T1.HR_CLASS = '000' ");
                stb.append("     and T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");
            } else if (param.isCoursegroup(formGroupDiv)) {
                stb.append("     and T1.AVG_DIV = '" + AVG_DIV_COURSEGROUP + "' ");
                stb.append("     and T1.GRADE = '" + param._grade + "' ");
                stb.append("     and T1.HR_CLASS = '000' ");
                stb.append("     and T1.COURSECD = '0' ");
                stb.append("     and T1.MAJORCD = ? ");
                stb.append("     and T1.COURSECODE = '0000' ");
            } else {
                stb.append("     and T1.AVG_DIV = '" + AVG_DIV_GRADE + "' ");
                stb.append("     and T1.GRADE = '" + param._grade + "' ");
                stb.append("     and T1.HR_CLASS = '000' ");
                stb.append("     and T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
            }
            return stb.toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 69424 $"); // CVSキーワードの取り扱いに注意
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Param {
        final String _year;
        final String _categoryIsClass;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _testName;
        final String _semester;
        final String _semesterName;
        final String _grade;
        final String _hrClass;
        final String _formGroupDiv;//平均点
        final String _rankDataDiv;//基準点 1(01):総合点,2(02):平均点,3(03):偏差値,4(11):傾斜総合点
        /** 平均の基準点: 得点=1 / 傾斜総合点=2 */
        final String _avgDataDiv;
        final String[] _selectData;
        final String _selectInstate;
        final String _nendo;
        final String _ketten;
        final String _checkKettenDiv; //欠点プロパティ 1,2,設定無し(1,2以外)
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        final boolean _isChiben;
        final boolean _isTokiwa;
        final String _notPrintJuni;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");
            final String juni = request.getParameter("JUNI");
            if ("4".equals(juni)) {
                _rankDataDiv = "11";
                _avgDataDiv = "2";
            } else {
                _rankDataDiv = (null != juni && juni.length() < 2 ? "0" : "") + juni;
                _avgDataDiv = "1";
            }
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _selectInstate = getInstate(_selectData);
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";

            _z010 = setNameMst(db2, "Z010", "00");
            _z012 = setNameMst(db2, "Z012", "01");
            _isSeireki = _z012.equals("2") ? true : false;
            _isChiben = "CHIBEN".equals(_z010);
            _isTokiwa = "tokiwa".equals(_z010);
            _notPrintJuni = request.getParameter("NOT_PRINT_JUNI");
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("PROFICIENCYNAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
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
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        /**
         * @param selectData
         * @return
         */
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

        private boolean isClass() {
            return _categoryIsClass.equals("1");
        }

        private boolean isGakunen(final String formGroupDiv) {
            return "1".equals(formGroupDiv);
        }

        private boolean isHr(final String formGroupDiv) {
            return "2".equals(formGroupDiv);
        }

        private boolean isCourse(final String formGroupDiv) {
            return "3".equals(formGroupDiv);
        }

        private boolean isMajor(final String formGroupDiv) {
            return "4".equals(formGroupDiv);
        }

        private boolean isCoursegroup(final String formGroupDiv) {
            return "5".equals(formGroupDiv);
        }

        private String getAvgName() {
            if (isHr(_formGroupDiv)) {
                return "クラス";
            } else if (isCourse(_formGroupDiv)) {
                return "コース";
            } else if (isMajor(_formGroupDiv)) {
                return "学科";
            } else if (isCoursegroup(_formGroupDiv)) {
                return "グループ";
            } else {
                return "学年";
            }
        }

        /** RECORD_SLUMP_DATを参照するテストコード */

        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return "1".equals(_checkKettenDiv) && !"9".equals(_semester);
        }

        /** 欠点対象：満点マスタ(PERFECT_RECORD_DAT)の合格点(PASS_SCORE)を参照して判断するか */
        private boolean isPerfectRecord() {
            return "2".equals(_checkKettenDiv);
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        private boolean isKetten() {
            return !isRecordSlump() && !isPerfectRecord();
        }

        private boolean isKyoto() {
            return "kyoto".equals(_z010);
        }
    }
}

// eof
