/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: aaeaf9735645db2e0df649d158585b7ed4ce43f5 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD105I {

    private static final Log log = LogFactory.getLog(KNJD105I.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final int MAXCOL = 5;
    private static final int SCORE_RANGE = 5;
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
        svf.VrSetForm("KNJD105I.frm", 1);
        final List studentList = createStudents(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　成績個人票(" + _param._testName + ")");
            final Student student = (Student) iterator.next();
            svf.VrsOut("HR_NAME", student._hrName + "(" + (NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : StringUtils.defaultString(student._attendNo)) + ")");
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);
            
            for (int i = 0; i < _param._comment.length; i++) {
            	svf.VrsOutn("COMMENT", i + 1, _param._comment[i]);
            }

            int rowCnt = 1;
            int colCnt = 1;
            int all9Cnt = 1;
            for (Iterator itSubclass = student._subclassRankList.iterator(); itSubclass.hasNext();) {
                final SubclassRank subclassRank = (SubclassRank) itSubclass.next();
                final String key = subclassRank.getKey();
                if (_param._subclassBunpuMap.containsKey(key)) {
                    if (colCnt > MAXCOL) {
                        colCnt = 1;
                        rowCnt++;
                    }
                    final SubclassBunpu bunpu = (SubclassBunpu) _param._subclassBunpuMap.get(key);
                    svf.VrsOutn("SUBCLASS_NAME" + rowCnt, colCnt, bunpu._subclassname);
                    svf.VrsOutn("EXAM_NUM" + rowCnt, colCnt, subclassRank._gCnt);
                    svf.VrsOutn("SCORE" + rowCnt, colCnt, String.valueOf(subclassRank._score));
                    if (!_param._d017Name1List.contains(subclassRank._classcd + "-" + subclassRank._schoolKind + "-" + subclassRank._curriculumCd + "-" + subclassRank._subclasscd)) {
                    	final String gAvg = null != subclassRank._gAvg && !"".equals(subclassRank._gAvg) ? subclassRank._gAvg : "0";
                    	final BigDecimal setVal = new BigDecimal(gAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
                    	svf.VrsOutn("AVERAGE" + rowCnt, colCnt, setVal.toString());
                    	svf.VrsOutn("DEVI" + rowCnt, colCnt, subclassRank._gradeDeviation);
                    	svf.VrsOutn("RANK" + rowCnt, colCnt, subclassRank._gradeRank);
                    	printRange(svf, bunpu._score9, 1, colCnt, rowCnt, 90, 100, subclassRank._score);
                    	printRange(svf, bunpu._score8, 2, colCnt, rowCnt, 80, 89, subclassRank._score);
                    	printRange(svf, bunpu._score7, 3, colCnt, rowCnt, 70, 79, subclassRank._score);
                    	printRange(svf, bunpu._score6, 4, colCnt, rowCnt, 60, 69, subclassRank._score);
                    	printRange(svf, bunpu._score5, 5, colCnt, rowCnt, 50, 59, subclassRank._score);
                    	printRange(svf, bunpu._score4, 6, colCnt, rowCnt, 40, 49, subclassRank._score);
                    	printRange(svf, bunpu._score3, 7, colCnt, rowCnt, 30, 39, subclassRank._score);
                    	printRange(svf, bunpu._score2, 8, colCnt, rowCnt, 20, 29, subclassRank._score);
                    	printRange(svf, bunpu._score1, 9, colCnt, rowCnt, 10, 19, subclassRank._score);
                    	printRange(svf, bunpu._score0, 10, colCnt, rowCnt, 0, 9, subclassRank._score);
                    }
                    colCnt++;
                }
                if (ALL9.equals(subclassRank._subclasscd)) {
                    for (Iterator itTest = _param._testItemMstMap.keySet().iterator(); itTest.hasNext();) {
                        final String testKey = (String) itTest.next();
                        final TestItemMst testItemMst = (TestItemMst) _param._testItemMstMap.get(testKey);
                        if (subclassRank._testcd.equals(testItemMst.getKey())) {
                            svf.VrsOutn("TEST_NAME", all9Cnt, testItemMst._semesterName + testItemMst._testitemName);
                            if (student._subclassAvgMap.containsKey(testKey)) {
                                final AllAvg allAvg = (AllAvg) student._subclassAvgMap.get(testKey);
                                svf.VrsOutn("SUBCLASS_DEVI_AVE", all9Cnt, allAvg._deviationAvg);
                            }
                            final BigDecimal setVal = new BigDecimal(subclassRank._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOutn("SUBCLASS_SCORE_AVE", all9Cnt, setVal.toString());
                            svf.VrsOutn("GRADE_RANK", all9Cnt, subclassRank._gradeAvgRank);
                            svf.VrsOutn("HR_RANK", all9Cnt, subclassRank._classAvgRank);
                            svf.VrsOutn("COURSE_RANK", all9Cnt, subclassRank._courseAvgRank);
                            all9Cnt++;
                        }
                    }
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printRange(final Vrw32alp svf, final int score, final int setField, final int colCnt, final int rowCnt, final int minScore, final int maxScore, final int stdScore) {
        final int rangeCnt = score / SCORE_RANGE;
        final int amariCnt = score % SCORE_RANGE > 0 ? 1 : 0;
        final int totalCnt = rangeCnt + amariCnt;
        String setSp = score == 0 ? "  " : score < 100 ? " " : "";
        boolean thisRange = minScore <= stdScore && stdScore <= maxScore;
        for (int squareCnt = 0; squareCnt < totalCnt; squareCnt++) {
            if (thisRange) {
                svf.VrsOutn("STAR" + rowCnt + "_" + setField, colCnt, "☆");
            }
            setSp = setSp + " ";
            svf.VrAttributen("BAR" + rowCnt + "_" + setField + "_" + (squareCnt + 1), colCnt, "Paint=(1,60,1),Bold=1");
        }
        svf.VrsOutn("BAR_NUM" + rowCnt + "_" + setField, colCnt, setSp + score);
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
                        hrclass,
                        hrName,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName,
                        courseCodeName
                );
                student.setSubclassRank(db2);
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
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final List _subclassRankList;
        final Map _subclassAvgMap;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _subclassRankList = new ArrayList();
            _subclassAvgMap = new TreeMap();
        }

        public void setSubclassRank(final DB2UDB db2) throws SQLException {
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
            stb.append("          AND REC_AVG.GRADE = '" + _param._grade + "' ");
            stb.append(" WHERE ");
            stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
            stb.append("     AND REC_RANK.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + _param._testcd + "' ");
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
            stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
            stb.append("     AND REC_RANK.SCORE_DIV = '01' ");
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
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
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
            stbAvg.append("     REC_RANK.YEAR = '" + _param._year + "' ");
            stbAvg.append("     AND REC_RANK.SCORE_DIV = '01' ");
            stbAvg.append("     AND REC_RANK.SCHREGNO = '" + _schregno + "' ");
            stbAvg.append("     AND REC_RANK.CLASSCD || '-' || REC_RANK.SCHOOL_KIND || '-' || REC_RANK.CURRICULUM_CD || '-' || REC_RANK.SUBCLASSCD ");
            stbAvg.append("          NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'D017') ");
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
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psAvg, rsAvg);
            }
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

    private class SubclassBunpu {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final int _score0;
        final int _score1;
        final int _score2;
        final int _score3;
        final int _score4;
        final int _score5;
        final int _score6;
        final int _score7;
        final int _score8;
        final int _score9;

        public SubclassBunpu(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final int score0,
                final int score1,
                final int score2,
                final int score3,
                final int score4,
                final int score5,
                final int score6,
                final int score7,
                final int score8,
                final int score9
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score0 = score0;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _score6 = score6;
            _score7 = score7;
            _score8 = score8;
            _score9 = score9;
        }
    }

    private class TestItemMst {
        final String _semester;
        final String _semesterName;
        final String _testKindCd;
        final String _testItemCd;
        final String _scoreDiv;
        final String _testitemName;

        public TestItemMst(
                final String semester,
                final String semesterName,
                final String testKindCd,
                final String testItemCd,
                final String scoreDiv,
                final String testitemName
        ) {
            _semester = semester;
            _semesterName = semesterName;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _scoreDiv = scoreDiv;
            _testitemName = testitemName;
        }

        public String getKey() {
            return _semester + _testKindCd + _testItemCd + _scoreDiv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63884 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _semester;
        final String _testcd;
        final String _testName;
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
        final Map _testItemMstMap;
        final Map _subclassBunpuMap;
        final List _d017Name1List;
        final String[] _comment;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _semester = request.getParameter("SEMESTER");
            _testcd = request.getParameter("TESTCD");
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
            _testName = getTestname(db2);
            _testItemMstMap = getTestMap(db2);
            _subclassBunpuMap = getSubclassBunpu(db2);
            _d017Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year +"' AND NAMECD1 = 'D017' "), "NAME1");
            _comment = new String[5];
            for (int i = 0; i < _comment.length; i++) {
            	final String commenti = request.getParameter("COMMENT" + String.valueOf(i + 1));
            	try {
                	_comment[i] = new String(commenti.getBytes("ISO8859-1"));
            	} catch (Exception e) {
            		log.error("exception!", e);
            	}
            }
        }

        private String getTestname(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.TESTITEMNAME, ");
                stb.append("     SM.SEMESTERNAME ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV");
                stb.append("     LEFT JOIN SEMESTER_MST SM ");
                stb.append("        ON SM.YEAR = SDIV.YEAR ");
                stb.append("       AND SM.SEMESTER = SDIV.SEMESTER");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _year + "' ");
                stb.append("     AND SDIV.SEMESTER = '" + _semester + "' ");
                stb.append("     AND SDIV.SCORE_DIV = '01' ");
                stb.append("     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _testcd + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("SEMESTERNAME")) + StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

        private Map getTestMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.*, ");
                stb.append("     SM.SEMESTERNAME ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV");
                stb.append("     LEFT JOIN SEMESTER_MST SM ");
                stb.append("        ON SM.YEAR = SDIV.YEAR ");
                stb.append("       AND SM.SEMESTER = SDIV.SEMESTER");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _year + "' ");
                stb.append("     AND SDIV.SCORE_DIV = '01' ");
                stb.append(" ORDER BY ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String semesterName = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
                    final String testKindCd = StringUtils.defaultString(rs.getString("TESTKINDCD"));
                    final String testItemCd = StringUtils.defaultString(rs.getString("TESTITEMCD"));
                    final String scoreDiv = StringUtils.defaultString(rs.getString("SCORE_DIV"));
                    final String testitemName = StringUtils.defaultString(rs.getString("TESTITEMNAME"));

                    final TestItemMst testItemMst = new TestItemMst(semester, semesterName, testKindCd, testItemCd, scoreDiv, testitemName);
                    final String setTestCd = semester + testKindCd + testItemCd + scoreDiv;
                    retMap.put(setTestCd, testItemMst);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getSubclassBunpu(final DB2UDB db2) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
                stb.append("     REC_RANK.SUBCLASSCD, ");
                stb.append("     MAX(SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS SCORE0, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS SCORE1, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS SCORE2, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS SCORE3, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS SCORE4, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS SCORE5, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 60 AND 69 THEN 1 ELSE 0 END) AS SCORE6, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 70 AND 79 THEN 1 ELSE 0 END) AS SCORE7, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 80 AND 89 THEN 1 ELSE 0 END) AS SCORE8, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 90 AND 100 THEN 1 ELSE 0 END) AS SCORE9 ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                stb.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
                stb.append("           AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                stb.append("           AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                stb.append("           AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     REC_RANK.YEAR = '" + _year + "' ");
                stb.append("     AND REC_RANK.SEMESTER = '" + _semester + "' ");
                stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + _testcd + "' ");
                stb.append("     AND EXISTS ( ");
                stb.append("         SELECT ");
                stb.append("             'x' ");
                stb.append("         FROM ");
                stb.append("             SCHREG_REGD_DAT REGD ");
                stb.append("         WHERE ");
                stb.append("             REC_RANK.YEAR = REGD.YEAR ");
                stb.append("             AND REC_RANK.SEMESTER = REGD.SEMESTER ");
                stb.append("             AND REGD.GRADE = '" + _grade + "' ");
                stb.append("             AND REC_RANK.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     ) ");
                stb.append(" GROUP BY ");
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
                stb.append("     REC_RANK.SUBCLASSCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final int score0 = rs.getInt("SCORE0");
                    final int score1 = rs.getInt("SCORE1");
                    final int score2 = rs.getInt("SCORE2");
                    final int score3 = rs.getInt("SCORE3");
                    final int score4 = rs.getInt("SCORE4");
                    final int score5 = rs.getInt("SCORE5");
                    final int score6 = rs.getInt("SCORE6");
                    final int score7 = rs.getInt("SCORE7");
                    final int score8 = rs.getInt("SCORE8");
                    final int score9 = rs.getInt("SCORE9");

                    final SubclassBunpu subclassBunpu = new SubclassBunpu(classcd, schoolKind, curriculumCd, subclasscd, subclassname, score0, score1, score2, score3, score4, score5, score6, score7, score8, score9);
                    final String setSubclassCd = classcd + schoolKind + curriculumCd + subclasscd;
                    retMap.put(setSubclassCd, subclassBunpu);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}

// eof
