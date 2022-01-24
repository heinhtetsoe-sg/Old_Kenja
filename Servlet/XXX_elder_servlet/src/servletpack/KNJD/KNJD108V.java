/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 048387dd6dc90f0b8a1da6a1882572f9a395206b $
 *
 * 作成日: 2018/07/19
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
import java.util.Iterator;
import java.util.LinkedList;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD108V {

    private static final Log log = LogFactory.getLog(KNJD108V.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final String ZENKI_TYUKAN = "1010101";
    private static final String ZENKI_KIMATSU = "1020101";
    private static final String ZENKI_HYOUKA = "1990008";
    private static final String KOUKI_TYUKAN = "2010101";
    private static final String KOUKI_KIMATSU = "2020101";
    private static final String KOUKI_HYOUKA = "2990008";
    private static final String GAKUNEN_HYOUKA = "9990008";
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJD108V.frm", 1);

        final List studentList = createStudents(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {

            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　成績個人票(３年間)");

            final Student student = (Student) iterator.next();
            svf.VrsOut("HR_NAME", student._hrName + "(" + String.valueOf(Integer.parseInt(student._attendNo)) + ")");
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            //生徒の各学年毎の最大年度のリスト
            for (Iterator itGradeYear = student._maxGradeYearList.iterator(); itGradeYear.hasNext();) {
                final GradeYear gradeYear = (GradeYear) itGradeYear.next();

                int gradeCdInt = Integer.parseInt(gradeYear._gradeCd);
                svf.VrsOut("GRADE" + gradeCdInt, gradeYear._gradeName);

                for (Iterator itSubclass = gradeYear._subclassRankList.iterator(); itSubclass.hasNext();) {
                    final SubclassRank subclassRank = (SubclassRank) itSubclass.next();
                    final String subclassCdAll = subclassRank.getKey();
                    if (!ALL9.equals(subclassRank._subclasscd) && gradeYear._subclassMap.containsKey(subclassCdAll)) {
                        final SubclassData subclassData = (SubclassData) gradeYear._subclassMap.get(subclassCdAll);
                        int subNameLen = KNJ_EditEdit.getMS932ByteLength(subclassData._subclassname);
                        final String subclassField = subNameLen > 24 ? "4" : subNameLen > 20 ? "3" : subNameLen > 16 ? "2" : "1";
                        svf.VrsOutn("SUBCLASS_NAME" + gradeCdInt +  "_" + subclassField, subclassData._fieldNo, subclassData._subclassname);

                        svf.VrsOutn(getPrintTestField(gradeCdInt, subclassRank._testcd), subclassData._fieldNo, String.valueOf(subclassRank._score));
                    }
                    if (ALL9.equals(subclassRank._subclasscd)) {
                        final String rankField = getPrintRankField(gradeCdInt, subclassRank._testcd);
                        if (gradeYear._subclassAvgMap.containsKey(subclassRank._testcd)) {
                            final AllAvg allAvg = (AllAvg) gradeYear._subclassAvgMap.get(subclassRank._testcd);
                            svf.VrsOut("SUBCLASS_DEVI_AVE" + rankField, allAvg._deviationAvg);
                        }

                        final BigDecimal setVal = new BigDecimal(subclassRank._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                        svf.VrsOut("SUBCLASS_SCORE_AVE" + rankField, setVal.toString());
                        svf.VrsOut("GRADE_RANK" + rankField, String.valueOf(subclassRank._gradeAvgRank));
                        svf.VrsOut("HR_RANK" + rankField, String.valueOf(subclassRank._classAvgRank));
                        svf.VrsOut("COURSE_RANK" + rankField, String.valueOf(subclassRank._courseAvgRank));
                    }
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private String getPrintTestField(final int gradeCd, final String testCd) {

        if (ZENKI_TYUKAN.equals(testCd)) {
            return "SCORE" + gradeCd + "_1";
        }
        if (ZENKI_KIMATSU.equals(testCd)) {
            return "SCORE" + gradeCd + "_2";
        }
        if (KOUKI_TYUKAN.equals(testCd)) {
            return "SCORE" + gradeCd + "_3";
        }
        if (KOUKI_KIMATSU.equals(testCd)) {
            return "SCORE" + gradeCd + "_4";
        }
        if (ZENKI_HYOUKA.equals(testCd)) {
            return "DIV" + gradeCd + "_1";
        }
        if (GAKUNEN_HYOUKA.equals(testCd)) {
            return "DIV" + gradeCd + "_2";
        }
        return "";
    }

    private String getPrintRankField(final int gradeCd, final String testCd) {

        if (ZENKI_TYUKAN.equals(testCd)) {
            return gradeCd + "_1";
        }
        if (ZENKI_KIMATSU.equals(testCd)) {
            return gradeCd + "_2";
        }
        if (KOUKI_TYUKAN.equals(testCd)) {
            return gradeCd + "_3";
        }
        if (KOUKI_KIMATSU.equals(testCd)) {
            return gradeCd + "_4";
        }
        if (ZENKI_HYOUKA.equals(testCd)) {
            return gradeCd + "_5";
        }
        if (GAKUNEN_HYOUKA.equals(testCd)) {
            return gradeCd + "_6";
        }
        return "";
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List retList = new LinkedList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
            stb.append("          AND REGD.GRADE = GDAT.GRADE ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + _param._semester + "' ");
            if ("1".equals(_param._categoryIsClass)) {
                stb.append("  AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
            } else {
                stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            }
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO  ");

            //log.debug(" regd sql = " + sql);
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String hrclass = rs.getString("hr_class");
                final String hrName = rs.getString("hr_name");
                final String attendno = rs.getString("attendno");
                final String name = rs.getString("name");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODENAME");

                final Student student = new Student(
                        schregno,
                        grade,
                        schoolKind,
                        hrclass,
                        hrName,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName, courseCodeName
                );
                student.setLoadData(db2);
                retList.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _schoolKind;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final List _maxGradeYearList;

        public Student(
                final String schregno,
                final String grade,
                final String schoolKind,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName, final String courseCodeName
        ) {
            _schregno = schregno;
            _grade = grade;
            _schoolKind = schoolKind;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _maxGradeYearList = new ArrayList();
        }

        public void setLoadData(final DB2UDB db2) throws SQLException {

            final StringBuffer stbGy = new StringBuffer();
            stbGy.append(" WITH MAX_GRADE_YEAR_T AS ( ");
            stbGy.append(" SELECT ");
            stbGy.append("     REGD.SCHREGNO, ");
            stbGy.append("     REGD.GRADE, ");
            stbGy.append("     MAX(REGD.YEAR) AS YEAR ");
            stbGy.append(" FROM ");
            stbGy.append("     SCHREG_REGD_DAT REGD ");
            stbGy.append(" WHERE ");
            stbGy.append("     REGD.YEAR <= '" + _param._year + "' ");
            stbGy.append("     AND REGD.SCHREGNO = '" + _schregno + "' ");
            stbGy.append(" GROUP BY ");
            stbGy.append("     REGD.SCHREGNO, ");
            stbGy.append("     REGD.GRADE ");
            stbGy.append(" ) ");
            stbGy.append(" SELECT ");
            stbGy.append("     GY.YEAR, ");
            stbGy.append("     GY.GRADE, ");
            stbGy.append("     GDAT.GRADE_CD, ");
            stbGy.append("     GDAT.GRADE_NAME1 ");
            stbGy.append(" FROM ");
            stbGy.append("     MAX_GRADE_YEAR_T GY ");
            stbGy.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GY.YEAR = GDAT.YEAR ");
            stbGy.append("           AND GY.GRADE = GDAT.GRADE ");
            stbGy.append("           AND GDAT.SCHOOL_KIND = '" + _schoolKind + "' ");
            stbGy.append(" ORDER BY ");
            stbGy.append("     GY.YEAR, ");
            stbGy.append("     GY.GRADE ");

            PreparedStatement psGy = null;
            ResultSet rsGy = null;
            try {
                psGy = db2.prepareStatement(stbGy.toString());
                rsGy = psGy.executeQuery();
                while (rsGy.next()) {
                    final String year = rsGy.getString("YEAR");
                    final String grade = rsGy.getString("GRADE");
                    final String gradeCd = rsGy.getString("GRADE_CD");
                    final String gradeName = StringUtils.defaultString(rsGy.getString("GRADE_NAME1"));

                    final GradeYear gradeYear = new GradeYear(
                            _schregno,
                            year,
                            grade,
                            gradeCd,
                            gradeName
                    );
                    gradeYear.setRankData(db2);
                    _maxGradeYearList.add(gradeYear);
                }
            } catch (final SQLException e) {
                log.error("生徒の在籍情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psGy, rsGy);
            }

        }
    }

    private class GradeYear {
        final String _schregno;
        final String _year;
        final String _grade;
        final String _gradeCd;
        final String _gradeName;
        final List _subclassRankList;
        final Map _subclassMap;
        final Map _subclassAvgMap;

        public GradeYear(
                final String schregno,
                final String year,
                final String grade,
                final String gradeCd,
                final String gradeName
        ) {
            _schregno = schregno;
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
            _subclassRankList = new ArrayList();
            _subclassMap = new TreeMap();
            _subclassAvgMap = new TreeMap();
        }

        public void setRankData(final DB2UDB db2) throws SQLException {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
            stb.append("     REC_RANK.CLASSCD, ");
            stb.append("     REC_RANK.SCHOOL_KIND, ");
            stb.append("     REC_RANK.CURRICULUM_CD, ");
            stb.append("     REC_RANK.SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     REC_RANK.SCORE, ");
            stb.append("     REC_RANK.AVG, ");
            stb.append("     REC_AVG.COUNT AS GCNT, ");
            stb.append("     REC_AVG.AVG AS GAVG, ");
            stb.append("     REC_RANK.GRADE_RANK, ");
            stb.append("     REC_RANK.GRADE_AVG_RANK, ");
            stb.append("     REC_RANK.GRADE_DEVIATION, ");
            stb.append("     REC_RANK.GRADE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CLASS_RANK, ");
            stb.append("     REC_RANK.CLASS_AVG_RANK, ");
            stb.append("     REC_RANK.CLASS_DEVIATION, ");
            stb.append("     REC_RANK.CLASS_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_RANK, ");
            stb.append("     REC_RANK.COURSE_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.MAJOR_RANK, ");
            stb.append("     REC_RANK.MAJOR_AVG_RANK, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
            stb.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
            stb.append("           AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("           AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("           AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT REC_AVG ON REC_RANK.YEAR = REC_AVG.YEAR ");
            stb.append("          AND REC_RANK.SEMESTER = REC_AVG.SEMESTER ");
            stb.append("          AND REC_RANK.TESTKINDCD = REC_AVG.TESTKINDCD ");
            stb.append("          AND REC_RANK.TESTITEMCD = REC_AVG.TESTITEMCD ");
            stb.append("          AND REC_RANK.SCORE_DIV = REC_AVG.SCORE_DIV ");
            stb.append("          AND REC_RANK.CLASSCD = REC_AVG.CLASSCD ");
            stb.append("          AND REC_RANK.SCHOOL_KIND = REC_AVG.SCHOOL_KIND ");
            stb.append("          AND REC_RANK.CURRICULUM_CD = REC_AVG.CURRICULUM_CD ");
            stb.append("          AND REC_RANK.SUBCLASSCD = REC_AVG.SUBCLASSCD ");
            stb.append("          AND REC_AVG.AVG_DIV = '1' ");
            stb.append("          AND REC_AVG.GRADE = '" + _grade + "' ");
            stb.append(" WHERE ");
            stb.append("     REC_RANK.YEAR = '" + _year + "' ");
            stb.append("     AND REC_RANK.SCHREGNO = '" + _schregno + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
            stb.append("     REC_RANK.CLASSCD, ");
            stb.append("     REC_RANK.SCHOOL_KIND, ");
            stb.append("     REC_RANK.CURRICULUM_CD, ");
            stb.append("     REC_RANK.SUBCLASSCD, ");
            stb.append("     '全科目' AS SUBCLASSNAME, ");
            stb.append("     REC_RANK.SCORE, ");
            stb.append("     REC_RANK.AVG, ");
            stb.append("     0 AS GCNT, ");
            stb.append("     0 AS GAVG, ");
            stb.append("     REC_RANK.GRADE_RANK, ");
            stb.append("     REC_RANK.GRADE_AVG_RANK, ");
            stb.append("     REC_RANK.GRADE_DEVIATION, ");
            stb.append("     REC_RANK.GRADE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CLASS_RANK, ");
            stb.append("     REC_RANK.CLASS_AVG_RANK, ");
            stb.append("     REC_RANK.CLASS_DEVIATION, ");
            stb.append("     REC_RANK.CLASS_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_RANK, ");
            stb.append("     REC_RANK.COURSE_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.MAJOR_RANK, ");
            stb.append("     REC_RANK.MAJOR_AVG_RANK, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
            stb.append(" WHERE ");
            stb.append("     REC_RANK.YEAR = '" + _year + "' ");
            stb.append("     AND REC_RANK.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND REC_RANK.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     TESTCD, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("TESTCD");
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final int score = rs.getInt("SCORE");
                    final String avg = StringUtils.defaultString(rs.getString("AVG"));
                    final String gCnt = StringUtils.defaultString(rs.getString("GCNT"));
                    final String gAvg = StringUtils.defaultString(rs.getString("GAVG"));
                    final String gradeRank = StringUtils.defaultString(rs.getString("GRADE_RANK"));
                    final String gradeAvgRank = StringUtils.defaultString(rs.getString("GRADE_AVG_RANK"));
                    final String gradeDeviation = StringUtils.defaultString(rs.getString("GRADE_DEVIATION"));
                    final String gradeDeviationRank = StringUtils.defaultString(rs.getString("GRADE_DEVIATION_RANK"));
                    final String classRank = StringUtils.defaultString(rs.getString("CLASS_RANK"));
                    final String classAvgRank = StringUtils.defaultString(rs.getString("CLASS_AVG_RANK"));
                    final String classDeviation = StringUtils.defaultString(rs.getString("CLASS_DEVIATION"));
                    final String classDeviationRank = StringUtils.defaultString(rs.getString("CLASS_DEVIATION_RANK"));
                    final String courseRank = StringUtils.defaultString(rs.getString("COURSE_RANK"));
                    final String courseAvgRank = StringUtils.defaultString(rs.getString("COURSE_AVG_RANK"));
                    final String courseDeviation = StringUtils.defaultString(rs.getString("COURSE_DEVIATION"));
                    final String courseDeviationRank = StringUtils.defaultString(rs.getString("COURSE_DEVIATION_RANK"));
                    final String majorRank = StringUtils.defaultString(rs.getString("MAJOR_RANK"));
                    final String majorAvgRank = StringUtils.defaultString(rs.getString("MAJOR_AVG_RANK"));
                    final String majorDeviation = StringUtils.defaultString(rs.getString("MAJOR_DEVIATION"));
                    final String majorDeviationRank = StringUtils.defaultString(rs.getString("MAJOR_DEVIATION_RANK"));
                    final String courseGroupRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_RANK"));
                    final String courseGroupAvgRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_AVG_RANK"));
                    final String courseGroupDeviation = StringUtils.defaultString(rs.getString("COURSE_GROUP_DEVIATION"));
                    final String courseGroupDeviationRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_DEVIATION_RANK"));
                    final String chairGroupRank = StringUtils.defaultString(rs.getString("CHAIR_GROUP_RANK"));
                    final String chairGroupAvgRank = StringUtils.defaultString(rs.getString("CHAIR_GROUP_AVG_RANK"));
                    final String chairGroupDeviation = StringUtils.defaultString(rs.getString("CHAIR_GROUP_DEVIATION"));
                    final String chairGroupDeviationRank = StringUtils.defaultString(rs.getString("CHAIR_GROUP_DEVIATION_RANK"));

                    final String setSubclassCd = classcd + schoolKind + curriculumCd + subclasscd;
                    final SubclassData subclassData = new SubclassData(
                            classcd,
                            schoolKind,
                            curriculumCd,
                            subclasscd,
                            subclassname
                    );
                    _subclassMap.put(setSubclassCd, subclassData);

                    final SubclassRank subclassRank = new SubclassRank(
                            testcd,
                            classcd,
                            schoolKind,
                            curriculumCd,
                            subclasscd,
                            subclassname,
                            score,
                            avg,
                            gCnt,
                            gAvg,
                            gradeRank,
                            gradeAvgRank,
                            gradeDeviation,
                            gradeDeviationRank,
                            classRank,
                            classAvgRank,
                            classDeviation,
                            classDeviationRank,
                            courseRank,
                            courseAvgRank,
                            courseDeviation,
                            courseDeviationRank,
                            majorRank,
                            majorAvgRank,
                            majorDeviation,
                            majorDeviationRank,
                            courseGroupRank,
                            courseGroupAvgRank,
                            courseGroupDeviation,
                            courseGroupDeviationRank,
                            chairGroupRank,
                            chairGroupAvgRank,
                            chairGroupDeviation,
                            chairGroupDeviationRank
                    );
                    _subclassRankList.add(subclassRank);
                }
                int setFieldNo = 1;
                for (Iterator iterator = _subclassMap.keySet().iterator(); iterator.hasNext();) {
                    final String msubKey = (String) iterator.next();
                    final SubclassData subclassData = (SubclassData) _subclassMap.get(msubKey);
                    subclassData._fieldNo = setFieldNo;
                    setFieldNo++;
                }

            } catch (final SQLException e) {
                log.error("生徒のRANK情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            final StringBuffer stbAvg = new StringBuffer();
            stbAvg.append(" SELECT ");
            stbAvg.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
            stbAvg.append("     DECIMAL(ROUND(AVG(FLOAT(REC_RANK.GRADE_DEVIATION))*10,0)/10,5,1) AS DEVIATION_AVG ");
            stbAvg.append(" FROM ");
            stbAvg.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
            stbAvg.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
            stbAvg.append("           AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stbAvg.append("           AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stbAvg.append("           AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stbAvg.append(" WHERE ");
            stbAvg.append("     REC_RANK.YEAR = '" + _year + "' ");
            stbAvg.append("     AND REC_RANK.SCORE_DIV = '01' ");
            stbAvg.append("     AND REC_RANK.SCHREGNO = '" + _schregno + "' ");
            stbAvg.append(" GROUP BY ");
            stbAvg.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV ");

            PreparedStatement psAvg = null;
            ResultSet rsAvg = null;
            try {
                psAvg = db2.prepareStatement(stbAvg.toString());
                rsAvg = psAvg.executeQuery();
                while (rsAvg.next()) {
                    final String testcd = rsAvg.getString("TESTCD");
                    final String deviationAvg = StringUtils.defaultString(rsAvg.getString("DEVIATION_AVG"));

                    final AllAvg allAvg = new AllAvg(
                            testcd,
                            deviationAvg
                    );
                    _subclassAvgMap.put(testcd, allAvg);
                }
            } catch (final SQLException e) {
                log.error("偏差値平均情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psAvg, rsAvg);
            }
        }

        public String toString() {
            return _year + ":" + _gradeName;
        }
    }

    private class SubclassData {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        int _fieldNo;

        public SubclassData(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

    }

    private class AllAvg {
        final String _testcd;
        final String _deviationAvg;

        public AllAvg(
                final String testcd,
                final String deviationAvg
        ) {
            _testcd = testcd;
            _deviationAvg = deviationAvg;
        }
    }

    private class SubclassRank {
        final String _testcd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final int _score;
        final String _avg;
        final String _gCnt;
        final String _gAvg;
        final String _gradeRank;
        final String _gradeAvgRank;
        final String _gradeDeviation;
        final String _gradeDeviationRank;
        final String _classRank;
        final String _classAvgRank;
        final String _classDeviation;
        final String _classDeviationRank;
        final String _courseRank;
        final String _courseAvgRank;
        final String _courseDeviation;
        final String _courseDeviationRank;
        final String _majorRank;
        final String _majorAvgRank;
        final String _majorDeviation;
        final String _majorDeviationRank;
        final String _courseGroupRank;
        final String _courseGroupAvgRank;
        final String _courseGroupDeviation;
        final String _courseGroupDeviationRank;
        final String _chairGroupRank;
        final String _chairGroupAvgRank;
        final String _chairGroupDeviation;
        final String _chairGroupDeviationRank;

        public SubclassRank(
                final String testcd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final int score,
                final String avg,
                final String gCnt,
                final String gAvg,
                final String gradeRank,
                final String gradeAvgRank,
                final String gradeDeviation,
                final String gradeDeviationRank,
                final String classRank,
                final String classAvgRank,
                final String classDeviation,
                final String classDeviationRank,
                final String courseRank,
                final String courseAvgRank,
                final String courseDeviation,
                final String courseDeviationRank,
                final String majorRank,
                final String majorAvgRank,
                final String majorDeviation,
                final String majorDeviationRank,
                final String courseGroupRank,
                final String courseGroupAvgRank,
                final String courseGroupDeviation,
                final String courseGroupDeviationRank,
                final String chairGroupRank,
                final String chairGroupAvgRank,
                final String chairGroupDeviation,
                final String chairGroupDeviationRank
        ) {
            _testcd = testcd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _avg = avg;
            _gCnt = gCnt;
            _gAvg = gAvg;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _gradeDeviation = gradeDeviation;
            _gradeDeviationRank = gradeDeviationRank;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classDeviation = classDeviation;
            _classDeviationRank = classDeviationRank;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseDeviation = courseDeviation;
            _courseDeviationRank = courseDeviationRank;
            _majorRank = majorRank;
            _majorAvgRank = majorAvgRank;
            _majorDeviation = majorDeviation;
            _majorDeviationRank = majorDeviationRank;
            _courseGroupRank = courseGroupRank;
            _courseGroupAvgRank = courseGroupAvgRank;
            _courseGroupDeviation = courseGroupDeviation;
            _courseGroupDeviationRank = courseGroupDeviationRank;
            _chairGroupRank = chairGroupRank;
            _chairGroupAvgRank = chairGroupAvgRank;
            _chairGroupDeviation = chairGroupDeviation;
            _chairGroupDeviationRank = chairGroupDeviationRank;
        }

        public String getKey() {
            return _classcd + _schoolKind + _curriculumCd + _subclasscd;
        }
    }

    private class TestItemMst {
        final String _semester;
        final String _testKindCd;
        final String _testItemCd;
        final String _scoreDiv;
        final String _testitemName;

        public TestItemMst(
                final String semester,
                final String testKindCd,
                final String testItemCd,
                final String scoreDiv,
                final String testitemName
        ) {
            _semester = semester;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _scoreDiv = scoreDiv;
            _testitemName = testitemName;
        }

        public String getKey() {
            return _semester + _testKindCd + _testItemCd + _scoreDiv;
        }

        public String toString() {
            return _semester + _testKindCd + _testItemCd + _scoreDiv + ":" + _testitemName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66253 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String[] _categorySelected;
        final String _year;
        final String _ctrlSeme;
        final String _loginDate;
        final String _prgid;
        final String _imagePath;
        final String _usecurriculumcd;
        final String _useclassdetaildat;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _printLogRemoteAddr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _year = request.getParameter("YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _imagePath = request.getParameter("IMAGE_PATH");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useclassdetaildat = request.getParameter("useClassDetailDat");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
        }

    }
}

// eof
