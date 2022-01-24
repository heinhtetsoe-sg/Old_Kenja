// kanji=漢字
/*
 * $Id: 8605dcd32e5028582bfac3693f0e8c2fafa86036 $
 *
 * 作成日: 2009/08/20
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */

public class KNJD185 {

    private static final String SLUMP_CD = "1";
    private static final String SUBCLASS_ALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD185.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasData = false;

        sd.setSvfInit(request, response, svf);
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        
        log.fatal(" $Revision: 74344 $ $Date: 2020-05-15 19:30:21 +0900 (金, 15 5 2020) $");
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        
        final KNJD185Form knjd185Form = new KNJD185Form(db2, svf, param);
        
        try {
            final List students = createStudents(db2, param);
            
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                knjd185Form.print(student);
                
                hasData = true;
            }
            
            if (!hasData) {
                log.warn("データがありません");
            }
            
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        
        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }
    
    /**
     * 表示するデータをセットした生徒のリストを得る
     * @param db2
     * @param param
     * @return
     */
    private List createStudents(final DB2UDB db2, final Param param) {
        
        final List students = Student.getStudents(db2, param);
        
        if (students.size() == 0) {
            log.warn("対象の生徒がいません");
            return students;
        }
        
        setAttendData(db2, students, param);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            
            setScoreValue(db2, student, param);
            AbsenceHigh.setAbsenceHigh(db2, student, param);
            setRecDetail(db2, student, param);
            if ("1".equals(param._addrPrint)) {
                Address.setAddress(db2, student, param);
            }
        }
        HReportRemark.setHReportRemark(db2, students, param);
        
        return students;
    }

    private static int getMS932ByteLength(final String s) {
        int length = 0;
        if (null != s) {
            try {
                length = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return length;
    }
    
    /**
     * 学籍番号の生徒を得る
     * @param schregno 学籍番号
     * @param students 生徒のリスト
     * @return 学籍番号の生徒
     */
    private Student getStudent(final String schregno, final List students) {
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }
    
    /**
     * 成績・序列・欠点をセットする
     * @param db2
     * @param student
     * @param param
     */
    private void setScoreValue(final DB2UDB db2, final Student student, final Param param) {
        
        final String prestatementRecordScore = sqlRecordScore(param, student._schregno);
        // log.debug("setScoreValue sql = " + prestatementRecordScore);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(prestatementRecordScore);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String testKindCd = rs.getString("TESTKINDCD");
                final String tableDiv = rs.getString("TABLE_DIV");
                
                if (SUBCLASS_ALL.equals(subclassCd)) {
                    final String gradeRank = rs.getString("GRADE_RANK");
                    final String gradeAvgRank = rs.getString("GRADE_AVG_RANK");
                    final String gradeCount = rs.getString("GRADE_COUNT");
                    final String courseRank = rs.getString("COURSE_RANK");
                    final String courseAvgRank = rs.getString("COURSE_AVG_RANK");
                    final String courseCount = rs.getString("COURSE_COUNT");
                    final String classRank = rs.getString("CLASS_RANK");
                    final String classAvgRank = rs.getString("CLASS_AVG_RANK");
                    final String classCount = rs.getString("CLASS_COUNT");
                    final String courseGroupRank = rs.getString("COURSE_GROUP_RANK");
                    final String courseGroupAvgRank = rs.getString("COURSE_GROUP_AVG_RANK");
                    final String courseGroupCount = rs.getString("COURSE_GROUP_COUNT");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final ScoreRank sr = new ScoreRank(tableDiv, testKindCd, score, avg, 
                            gradeRank, gradeAvgRank, gradeCount,
                            courseRank, courseAvgRank, courseCount,
                            classRank, classAvgRank, classCount,
                            courseGroupRank, courseGroupAvgRank, courseGroupCount);
                    
                    student.putScoreRank(testKindCd, tableDiv, sr);
                    // log.debug(" scoreRank = " + student + " , semTestKindCd = " + testKindCd + " , tableDiv = " + tableDiv + " "+ sr);
                } else {
                    final String value = rs.getString("VALUE");
                    final ScoreValue sv = new ScoreValue(testKindCd, subclassCd, value);
                    
                    student.putScoreValue(subclassCd, testKindCd, tableDiv, sv);
                    
                    // log.debug(" " + student + " , subclassCd = " + subclassCd + " , semTestKindCd = " + testKindCd + " , tableDiv = " + tableDiv + " "+ sv);
                }
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    /**
     * 各生徒に１日ごと、科目ごとの出欠データをセットする
     * @param db2
     * @param students
     * @param param
     */
    private void setAttendData(final DB2UDB db2, final List students, final Param param) {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final String[] targetSemesters = new String[]{"1", "2", "3", Param.SEMEALL};
        for (int i = 0; i < targetSemesters.length; i++) {
            try {
            final String semester = targetSemesters[i];
            // log.debug(" semester = " + semester + " , " + param._semesterMap.get(semester));
            final Semester semesS = (Semester) param._semesterMap.get(semester);
            if (null == semesS) {
                log.debug(" 対象学期がありません。:" + semester);
                continue;
            } else {
                log.debug(" 対象学期:" + semester);
            }
            final String sdate = Param.SEMEALL.equals(semester) ? param._sdate : semesS._sdate;
            final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;
            try {
                param._attendParamMap.put("grade", param._grade_hr_class.substring(0, 2));
                param._attendParamMap.put("hrClass", param._grade_hr_class.substring(2, 5));
                String sql;
                sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        sdate,
                        edate,
                        param._attendParamMap
                );
                
                //log.debug(" attend semes sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final int lesson = rs.getInt("LESSON");
                    final int mourning = rs.getInt("MOURNING");
                    final int suspend = rs.getInt("SUSPEND");
                    final int abroad = rs.getInt("TRANSFER_DATE");
                    final int mlesson = rs.getInt("MLESSON");
                    final int absence = rs.getInt("SICK");
                    final int attend = rs.getInt("PRESENT");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int virus = rs.getInt("VIRUS");
                    final int koudome = rs.getInt("KOUDOME");
                        
                    final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, virus, koudome);
                    // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                    student.setAttendance(semester, attendance);
                }
                
            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            String sql = null;
            try {
                param._attendParamMap.put("grade", param._grade_hr_class.substring(0, 2));
                param._attendParamMap.put("hrClass", param._grade_hr_class.substring(2, 5));

                sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        sdate,
                        edate,
                        param._attendParamMap
                );
                
                //log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    
                    final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                    final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                    final BigDecimal sick = rs.getBigDecimal("SICK2");
                    final BigDecimal absent = rs.getBigDecimal("ABSENT");
                    final BigDecimal late = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("LATE") : rs.getBigDecimal("LATE2");
                    final BigDecimal early = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("EARLY") : rs.getBigDecimal("EARLY2");
                    final BigDecimal rawReplacedAbsence = rs.getBigDecimal("RAW_REPLACED_SICK");
                    final BigDecimal replacedAbsence = rs.getBigDecimal("REPLACED_SICK");
                    
                    final SubclassAttendance sa = new SubclassAttendance(lesson, rawSick, sick, absent, late, early, rawReplacedAbsence, replacedAbsence);
                    
                    student.setSubclassAttendance(subclassCd, semester, sa);
//                    log.debug("   schregno = " + student._schregno + " , subclcasscd = " + subclassCd + " , subclass attendance = " + sa);
                }
            } catch (SQLException e) {
                log.debug("sql exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
    }
    
    private String sqlRecordScore(final Param param, final String schregno) {
        
        final String[] targets = param.getTargetTestKindCds();
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RECORD_VALUE AS ( ");
        stb.append("   SELECT  '" + Param.REC + "' AS TABLE_DIV, T1.YEAR, T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("           T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
        }
        stb.append("           T1.SUBCLASSCD, T1.SEMESTER, ");
        stb.append("           T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
        stb.append("           T1.SCORE, T1.AVG, ");
        stb.append("           T1.GRADE_RANK, T1.GRADE_AVG_RANK, T8.COUNT AS GRADE_COUNT, ");
        stb.append("           T1.COURSE_RANK, T1.COURSE_AVG_RANK, T3.COUNT AS COURSE_COUNT, ");
        stb.append("           T1.CLASS_RANK, T1.CLASS_AVG_RANK, T4.COUNT AS CLASS_COUNT, ");
        stb.append("           T1.MAJOR_RANK AS COURSE_GROUP_RANK, ");
        stb.append("           T1.MAJOR_AVG_RANK AS COURSE_GROUP_AVG_RANK, T7.COUNT AS COURSE_GROUP_COUNT, ");
        stb.append("           T6.SLUMP");
        stb.append("   FROM    RECORD_RANK_DAT T1");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
        stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
        stb.append("        AND T2.SCHREGNO = '" + schregno + "'");
        stb.append("   LEFT JOIN COURSE_GROUP_CD_DAT T2_2 ON T1.YEAR = T2_2.YEAR ");
        stb.append("        AND T2.GRADE = T2_2.GRADE ");
        stb.append("        AND T2.COURSECD = T2_2.COURSECD ");
        stb.append("        AND T2.MAJORCD = T2_2.MAJORCD ");
        stb.append("        AND T2.COURSECODE = T2_2.COURSECODE");
        
        stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("        AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("        AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        AND T3.GRADE = T2.GRADE ");
        stb.append("        AND T3.AVG_DIV = '3' ");
        stb.append("        AND T3.HR_CLASS = '000' ");
        stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
        stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("        AND T4.SEMESTER || T4.TESTKINDCD || T4.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("        AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        AND T4.GRADE = T2.GRADE ");
        stb.append("        AND T4.AVG_DIV = '2' ");
        stb.append("        AND T4.HR_CLASS = T2.HR_CLASS ");
        stb.append("        AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
        stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T7 ON T7.YEAR = T1.YEAR ");
        stb.append("        AND T7.SEMESTER || T7.TESTKINDCD || T7.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T7.CLASSCD = T1.CLASSCD AND T7.SCHOOL_KIND = T1.SCHOOL_KIND AND T7.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("        AND T7.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        AND T7.GRADE = T2.GRADE ");
        stb.append("        AND T7.AVG_DIV = '5' ");
        stb.append("        AND T7.HR_CLASS = '000' ");
        stb.append("        AND T7.COURSECD || T7.MAJORCD || T7.COURSECODE = '0' || T2_2.GROUP_CD || '0000' ");
        stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T8 ON T8.YEAR = T1.YEAR ");
        stb.append("        AND T8.SEMESTER || T8.TESTKINDCD || T8.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T8.CLASSCD = T1.CLASSCD AND T8.SCHOOL_KIND = T1.SCHOOL_KIND AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("        AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        AND T8.GRADE = T2.GRADE ");
        stb.append("        AND T8.AVG_DIV = '1' ");
        stb.append("        AND T8.HR_CLASS = '000' ");
        stb.append("        AND T8.COURSECD || T8.MAJORCD || T8.COURSECODE = '00000000' ");
        //不振科目 
        stb.append("   LEFT JOIN RECORD_SLUMP_DAT T6 ON T6.YEAR = T1.YEAR ");
        stb.append("        AND T6.SEMESTER || T6.TESTKINDCD || T6.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T6.CLASSCD = T1.CLASSCD AND T6.SCHOOL_KIND = T1.SCHOOL_KIND AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("        AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        AND T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("        AND T6.SLUMP = '1' ");
        stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
        stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, targets));
        stb.append(" ) ");
        stb.append("   SELECT  T1.TABLE_DIV ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("           , CASE WHEN T1.SUBCLASSCD = '" + SUBCLASS_ALL + "' THEN T1.SUBCLASSCD ");
            stb.append("              ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD END ");
            stb.append("             AS SUBCLASSCD ");
        } else {
            stb.append("           ,T1.SUBCLASSCD");
        }
        stb.append("           ,T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS TESTKINDCD ");
        stb.append("           ,T1.SCORE, T1.AVG ");
        stb.append("           ,T1.GRADE_RANK, T1.GRADE_AVG_RANK, T1.GRADE_COUNT ");
        stb.append("           ,T1.COURSE_RANK, T1.COURSE_AVG_RANK, T1.COURSE_COUNT ");
        stb.append("           ,T1.CLASS_RANK, T1.CLASS_AVG_RANK, T1.CLASS_COUNT ");
        stb.append("           ,T1.COURSE_GROUP_RANK, T1.COURSE_GROUP_AVG_RANK, T1.COURSE_GROUP_COUNT ");
        stb.append("           ,T1.VALUE, T1.SLUMP ");
        stb.append("   FROM    RECORD_VALUE T1");
        stb.append("   WHERE T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            stb.append("            OR T1.SUBCLASSCD = '" + SUBCLASS_ALL + "')");
        } else {
            stb.append("       AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "'");
            stb.append("            OR T1.SUBCLASSCD = '" + SUBCLASS_ALL + "')");
        }
        return stb.toString();
    }
    
    private String sqlSubclass (final Param param) {
        
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" SUBCLASS_CREDITS AS(");
        stb.append("   SELECT ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("          SUBCLASSCD AS SUBCLASSCD, CREDITS, L1.NAMESPARE1 ");
        stb.append("   FROM   CREDIT_MST T1");
        stb.append("          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z011' ");
        stb.append("               AND L1.NAMECD2 = T1.REQUIRE_FLG ");
        stb.append("        , (SELECT  T3.GRADE, T3.COURSECD, T3.MAJORCD, T3.COURSECODE");
        stb.append("           FROM    SCHREG_REGD_DAT T3");
        stb.append("           WHERE   T3.SCHREGNO = ?");
        stb.append("               AND T3.YEAR = '" + param._year + "'");
        stb.append("               AND T3.GRADE || T3.HR_CLASS = '" + param._grade_hr_class + "'");
        stb.append("               AND T3.SEMESTER = (SELECT  MAX(SEMESTER)");
        stb.append("                                  FROM    SCHREG_REGD_DAT T4");
        stb.append("                                  WHERE   T4.YEAR = '" + param._year + "'");
        stb.append("                                      AND T4.SEMESTER <= '" + param.getRegdSemester() + "'");
        stb.append("                                      AND T4.SCHREGNO = T3.SCHREGNO)");
        stb.append("          )T2 ");
        stb.append("   WHERE T1.YEAR = '" + param._year + "'");
        stb.append("     AND T1.GRADE = T2.GRADE");
        stb.append("     AND T1.COURSECD = T2.COURSECD");
        stb.append("     AND T1.MAJORCD = T2.MAJORCD");
        stb.append("     AND T1.COURSECODE = T2.COURSECODE");
        stb.append(" ) ");
        
        stb.append(" ,COMBINED_SUBCLASSCD AS(");
        stb.append("   SELECT ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("          COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, MIN( ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append("          ATTEND_SUBCLASSCD) AS ATTEND_SUBCLASSCD");
        stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("   WHERE  YEAR = '" + param._year + "' ");
        stb.append("   GROUP BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("          COMBINED_SUBCLASSCD");
        stb.append(" )");
        
        stb.append(" ,ATTEND_SUBCLASSCD AS(");
        stb.append("   SELECT ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append("          ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, MAX(PRINT_FLG1) AS PRINT_FLG, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, MAX(");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("          COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD");
        stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("   WHERE  YEAR = '" + param._year + "' ");
        stb.append("   GROUP BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append("          ATTEND_SUBCLASSCD");
        stb.append(" )");
        
        stb.append(", CHAIR_A AS(");
        stb.append("   SELECT  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("            T2.SUBCLASSCD AS SUBCLASSCD");
        stb.append("   FROM    CHAIR_STD_DAT T1, CHAIR_DAT T2");
        stb.append("   WHERE   T1.SCHREGNO = ?");
        stb.append("       AND T1.YEAR = '" + param._year + "'");
        stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
        stb.append("       AND T2.YEAR  = '" + param._year + "'");
        stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("       AND T2.SEMESTER = T1.SEMESTER");
        stb.append("       AND T2.CHAIRCD = T1.CHAIRCD");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "')");
        } else {
            stb.append("       AND (SUBSTR(T2.SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T2.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "')");
        }
        stb.append("   GROUP BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("             T2.SUBCLASSCD");
        stb.append(" )");
        
        stb.append(", SUBCLASSNUM AS(");
        stb.append("   SELECT  SUM(CASE WHEN ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" S1.CLASSCD ");
        } else {
            stb.append(" SUBSTR(S1.SUBCLASSCD,1,2) ");
        }
        stb.append("            = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
        stb.append("         , SUM(CASE WHEN "); 
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" S1.CLASSCD ");
        } else {
            stb.append(" SUBSTR(S1.SUBCLASSCD,1,2) ");
        }
        stb.append("           != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
        stb.append("   FROM    CHAIR_A S1");
        if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
            stb.append(" LEFT JOIN (SELECT CLASSCD || '-' || SCHOOL_KIND AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND CLASS_SEQ = '003') T1 ON T1.NAMECD2 = ");
            stb.append("   S1.CLASSCD || '-' || S1.SCHOOL_KIND ");
        } else {
            stb.append(" LEFT JOIN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1='D008') T1 ON T1.NAMECD2 = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" S1.CLASSCD ");
            } else {
                stb.append(" SUBSTR(S1.SUBCLASSCD,1,2) ");
            }
        }
        stb.append("), QUALIFIED AS(");
        stb.append("   SELECT ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T1.YEAR, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
        }
        stb.append("       T1.SUBCLASSCD, ");
        stb.append("       SUM(T1.CREDITS) AS CREDITS ");
        stb.append("   FROM ");
        stb.append("       SCHREG_QUALIFIED_DAT T1 ");
        stb.append("   WHERE ");
        stb.append("       T1.CREDITS IS NOT NULL ");
        stb.append("   GROUP BY ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T1.YEAR, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
        }
        stb.append("       T1.SUBCLASSCD ");
        stb.append(" )");
        
        stb.append(" SELECT  T2.SUBCLASSCD, T7.CLASSABBV, VALUE(T4.SUBCLASSORDERNAME2,T4.SUBCLASSNAME) AS SUBCLASSNAME");
        stb.append("       , T6.CREDITS, T6.NAMESPARE1 ");
        stb.append("       , CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL AND T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 2"); // 元科目かつ先科目
        stb.append("              WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN 9"); // 先科目
        stb.append("              WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 1"); // 元科目
        stb.append("              ELSE 0 END AS REPLACEFLG");
        stb.append("       , T5.ATTEND_SUBCLASSCD ");
        stb.append("       , T9.COMBINED_SUBCLASSCD ");
        stb.append("       , T9.PRINT_FLG");
        stb.append("       , N1.NAMECD2 AS NUM90_OTHER");
        stb.append("       , (SELECT NUM90 FROM SUBCLASSNUM) AS NUM90");
        stb.append("       , (SELECT NUMTOTAL FROM SUBCLASSNUM) AS NUMTOTAL");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       , CASE WHEN '90' = T2.CLASSCD THEN 3 ");
            stb.append("              WHEN N1.NAMECD2 IS NOT NULL THEN 2 ");
            stb.append("              ELSE 1 END AS ORDER0");
        } else {
            stb.append("       , CASE WHEN '90' = SUBSTR(T2.SUBCLASSCD, 1, 2) THEN 3 ");
            stb.append("              WHEN N1.NAMECD2 IS NOT NULL THEN 2 ");
            stb.append("              ELSE 1 END AS ORDER0");
        }
        stb.append("       , CASE WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN T9.COMBINED_SUBCLASSCD ELSE T2.SUBCLASSCD END AS ORDER1");
        stb.append("       , CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN 1 WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 2 ELSE 0 END AS ORDER2");
        stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
        stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
        stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
        stb.append("       , REC_SCORE.COMP_CREDIT ");
        stb.append("       , REC_SCORE.GET_CREDIT AS GET_CREDIT ");
        stb.append("       , REC_SCORE.ADD_CREDIT ");
        stb.append(" FROM    CHAIR_A T2");
        stb.append(" LEFT JOIN SUBCLASS_MST T4 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
        }
        stb.append("   T4.SUBCLASSCD = T2.SUBCLASSCD");
        stb.append(" LEFT JOIN CLASS_MST T7 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   T7.CLASSCD || '-' || T7.SCHOOL_KIND = ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
        } else {
            stb.append("   T7.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2)");
        }
        stb.append(" LEFT JOIN SUBCLASS_CREDITS T6 ON T6.SUBCLASSCD = T2.SUBCLASSCD");
        stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5 ON T5.COMBINED_SUBCLASSCD = T2.SUBCLASSCD");
        stb.append(" LEFT JOIN ATTEND_SUBCLASSCD T9 ON T9.ATTEND_SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
            stb.append(" LEFT JOIN (SELECT CLASSCD || '-' || SCHOOL_KIND AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND CLASS_SEQ = '003') N1 ON N1.NAMECD2 = ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
        } else {
            stb.append(" LEFT JOIN NAME_MST N1 ON N1.NAMECD1='D008' AND N1.NAMECD2 = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T2.CLASSCD ");
            } else {
                stb.append("    SUBSTR(T2.SUBCLASSCD,1,2)");
            }
        }
        stb.append(" LEFT JOIN RECORD_SCORE_DAT REC_SCORE ON REC_SCORE.YEAR = '" + param._year + "' ");
        stb.append("       AND REC_SCORE.SCHREGNO = ? AND ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    REC_SCORE.CLASSCD || '-' || REC_SCORE.SCHOOL_KIND || '-' || REC_SCORE.CURRICULUM_CD || '-' || ");
        }
        stb.append("       REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD = '" + Param._99900 + "' ");
        stb.append("       AND REC_SCORE.SCORE_DIV IN ('00', '01') ");
        stb.append(" LEFT JOIN QUALIFIED QUAL ON QUAL.YEAR = '" + param._year + "' ");
        stb.append("       AND QUAL.SCHREGNO = ? AND ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    QUAL.CLASSCD || '-' || QUAL.SCHOOL_KIND || '-' || QUAL.CURRICULUM_CD || '-' || ");
        }
        stb.append("       QUAL.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" ORDER BY ORDER0, ORDER1, ORDER2");
        return stb.toString();
    }
    
    private void setRecDetail (
            final DB2UDB db2,
            final Student student,
            final Param param
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        student._subclassInfos.clear();
        try {
            final String prestatementSubclass = sqlSubclass(param);
            // log.debug(" subclass sql = " + prestatementSubclass);
            ps = db2.prepareStatement(prestatementSubclass);
            
            int pp = 0;
            ps.setString(++pp, student._schregno);
            ps.setString(++pp, student._schregno);
            ps.setString(++pp, student._schregno);
            ps.setString(++pp, student._schregno);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String classabbv = rs.getString("CLASSABBV");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final String compCredit = rs.getString("COMP_CREDIT");
                final String getCredit = rs.getString("GET_CREDIT");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String attendSubclasscd = rs.getString("ATTEND_SUBCLASSCD");
                final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");

                final String namespare1 = rs.getString("NAMESPARE1");
                
                final int replaceflg = rs.getInt("REPLACEFLG");
                final String calculateCreditFlg = StringUtils.defaultString(rs.getString("CALCULATE_CREDIT_FLG"), "0");
                final String num90 = rs.getString("NUM90");
                final String num90Other = rs.getString("NUM90_OTHER");
                
                final Subclass subclass = student.getSubclass(subclassCd);
                
                final SubclassInfo info = new SubclassInfo(classabbv, subclassname, credits, compCredit, getCredit, subclassCd, 
                        attendSubclasscd, combinedSubclasscd, namespare1, subclass, replaceflg, calculateCreditFlg, num90, num90Other);
                student._subclassInfos.add(info);
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        int totalCredit = 0;
        for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
            final SubclassInfo info = (SubclassInfo) it.next();
            if (null == info._getCredit
                    || (1 == info._replaceflg && student.hasSubclassInfo(info._combinedSubclasscd))
                    || (2 == info._replaceflg && (student.hasSubclassInfo(info._attendSubclasscd) || student.hasSubclassInfo(info._combinedSubclasscd)))) {
                // 加算しない。
            } else {
                totalCredit += Integer.parseInt(info._getCredit);
            }
        }
        student._totalCredit = totalCredit;
    }
    
    private static class Student {
        
        public String _attendNo;
        public String _grade;
        
        public String _courseCd;
        public String _courseName;
        public String _majorCd;
        public String _majorName;
        public String _courseCode;
        public String _courseCodeName;
        
        public String _hrName;
        public String _hrNameAbbv;
        
        public String _name;
        
        final String _schregno;
        
        final List _subclassInfos = new ArrayList();
        
        int _totalCredit = 0;
        
        final Map _subclassScoreMap = new HashMap(); 
        final Map _attendMap = new TreeMap();
        final Map _scoreRankMap = new HashMap(); 
        final Map _subclassAbsenceHigh = new HashMap(); 
        final Map _spSubclassAbsenceHigh = new HashMap(); 
        final Map _specialGroupAttendanceMap = new HashMap();
        final Map _recordTotalStudieTimeDat = new HashMap();
        final Map _hReportRemarks = new HashMap();
        
        public Address _address;
        
        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        public void setAttendance(final String semester, final Attendance a) {
            _attendMap.put(semester, a);
        }
        
        public Attendance getAttendance(final String semester) {
            final Attendance a = (Attendance) _attendMap.get(semester);
            return (a == null) ? new Attendance() : a;
        }
        
        public void putSlump(final String subclassCd, final String testKindcd, final String slump) {
            findSubclass(subclassCd).setSlump(testKindcd, slump);
        }
        
        /**
         * 指定テスト種別の科目は欠点か
         * @param subclassCd 科目コード
         * @param testKindCd 指定テスト種別
         * @return
         */
        public boolean isSlump(final String subclassCd, final String testKindCd) {
            return findSubclass(subclassCd).isSlump(testKindCd);
        }
        
        public void setSubclassAttendance(final String subclassCd, final String semester, final SubclassAttendance sa) {
            findSubclass(subclassCd).setAttendance(semester, sa);
        }
        
        public SubclassAttendance getSubclassAttendance(final String subclassCd, final String semester) {
            return findSubclass(subclassCd).getAttendance(semester);
        }
        
        private static Map getMappedMap(final Map map, final String key) {
            if (!map.containsKey(key)) {
                map.put(key, new HashMap());
            }
            return (Map) map.get(key);
        }
        
        private Map getScoreRankMap(final String testKindcd) {
            return getMappedMap(_scoreRankMap, testKindcd);
        }
        
        /**
         * テスト種別ごとの序列をセットする
         * @param testKindcd
         * @param tableDiv
         * @param scoreRank
         */
        public void putScoreRank(final String testKindcd, final String tableDiv, final ScoreRank scoreRank) {
            getScoreRankMap(testKindcd).put(tableDiv, scoreRank);
        }
        
        /**
         * 指定テスト種別の序列データを得る
         * @param testKindcd 指定テスト種別
         * @return
         */
        public ScoreRank getScoreRank(final String testKindcd) {
            return (ScoreRank) getScoreRankMap(testKindcd).get(Param.REC);
        }
        
        public void putScoreValue(final String subclassCd, final String testKindcd, final String tableDiv, final ScoreValue scoreValue) {
            findSubclass(subclassCd).setScoreValue(testKindcd, tableDiv, scoreValue);
        }
        
        public ScoreValue getScoreValue(final String subclassCd, final String testKindCd, final String tableDiv) {
            return findSubclass(subclassCd).getScoreValue(testKindCd, tableDiv);
        }
        
        /**
         * 
         * @param subclassCd
         * @return
         */
        private Subclass findSubclass(final String subclassCd) {
            if (null == getSubclass(subclassCd)) {
                _subclassScoreMap.put(subclassCd, new Subclass(_schregno, subclassCd));
            }
            return getSubclass(subclassCd);
        }
        
        /**
         * 
         * @param subclassCd
         * @return
         */
        public Subclass getSubclass(final String subclassCd) {
            return (Subclass) _subclassScoreMap.get(subclassCd);
        }
        
        /**
         * 指定科目の欠課数上限値を得る
         * @param subclassCd 科目コード
         * @return
         */
        public AbsenceHigh getAbsenceHigh(final String subclassCd) {
            return (AbsenceHigh) _subclassAbsenceHigh.get(subclassCd);
        }
        
        /**
         * 指定特別活動グループの欠課数上限値を得る
         * @param subclassCd 特別活動グループ
         * @return
         */
        public AbsenceHigh getSpecialAbsenceHigh(final String specialSubclassGroupCd) {
            return (AbsenceHigh) _spSubclassAbsenceHigh.get(specialSubclassGroupCd);
        }
        
        /**
         * 指定の科目コードのレコードがあるか
         * @param subclassCd 科目コード
         * @return 指定の科目コードのレコードがあるならtrue
         */
        public boolean hasSubclassInfo(final String subclassCd) {
            if (null == subclassCd) {
                return false;
            }
            for (final Iterator it = _subclassInfos.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();
                if (subclassCd.equals(info._subclassCd)) {
                    return true;
                }
            }
            return false;
        }
        
        public String toString() {
            return _schregno + ":" + _name;
        }
        
        // -----
        
        public static List getStudents(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs1 = null;
            List students = new ArrayList();
            try {
                final String sqlRegdData = sqlRegdData(param);
                log.debug("sqlRegdData = " + sqlRegdData);
                ps = db2.prepareStatement(sqlRegdData);
                rs1 = ps.executeQuery();
                
                while( rs1.next() ){
                    
                    Student student = new Student(rs1.getString("SCHREGNO"));
                    students.add(student);
                    
                    student._grade = rs1.getString("GRADE");
                    student._courseCd = rs1.getString("COURSECD");
                    student._majorCd = rs1.getString("MAJORCD");
                    student._courseCode = rs1.getString("COURSECODE");
                    
                    student._courseName = rs1.getString("COURSENAME");
                    student._majorName = rs1.getString("MAJORNAME");
                    student._courseCodeName = StringUtils.defaultString(rs1.getString("COURSECODENAME"), "");
                    student._hrName = StringUtils.defaultString(rs1.getString("HR_NAME"), "");
                    student._hrNameAbbv = StringUtils.defaultString(rs1.getString("HR_NAMEABBV"), "");
                    student._attendNo = null == rs1.getString("ATTENDNO") || !NumberUtils.isDigits(rs1.getString("ATTENDNO"))? "" : Integer.parseInt(rs1.getString("ATTENDNO")) + "番";
                    final String name = StringUtils.defaultString(rs1.getString("NAME"), "");
                    final String realName = StringUtils.defaultString(rs1.getString("REAL_NAME"), "");
                    student._name = "1".equals(rs1.getString("USE_REAL_NAME")) ? realName : name;
                    
                    //log.debug("対象の生徒" + student);
                }
            } catch( Exception ex ) { 
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs1);
                db2.commit();
            }
            return students;
        }
        
        private static String sqlRegdData(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectSchregno) + " ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._edate + "' THEN T2.EDATE ELSE '" + param._edate + "' END) ");
            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._edate + "' THEN T2.EDATE ELSE '" + param._edate + "' END)) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._edate + "' THEN T2.EDATE ELSE '" + param._edate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");
            
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T2.HR_NAMEABBV, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append(        "T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '03' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }
    
    /**
     * 宛先住所データ
     */
    private static class Address {
        final String _addressee;
        final String _address1;
        final String _address2;
        final String _zipcd;
        
        public Address(final String addressee, final String address1, final String address2, final String zipcd) {
            _addressee = addressee;
            _address1 = address1;
            _address2 = address2;
            _zipcd = zipcd;
        }
        
        /**
         * 宛先の住所をセットする
         * @param db2
         * @param student
         * @param param
         */
        public static void setAddress(final DB2UDB db2, final Student student, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                
                if ("1".equals(param._addrDiv)) {
                    stb.append(" SELECT T0.SCHREGNO, T2.GUARD_NAME AS ADDRESSEE, T5.GUARD_NAME AS ADDRESSEE2, T4.GUARD_ADDR1 AS ADDR1, T4.GUARD_ADDR2 AS ADDR2, T4.GUARD_ZIPCD AS ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT WHERE '" + param._edate + "' BETWEEN ISSUEDATE AND EXPIREDATE GROUP BY SCHREGNO) T3 ON ");
                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                    stb.append(" LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                    stb.append(" LEFT JOIN GUARDIAN_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._edate + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = '" + student._schregno + "' ");
                } else if ("2".equals(param._addrDiv)) {
                    stb.append(" SELECT T0.SCHREGNO, T2.GUARANTOR_NAME AS ADDRESSEE, T5.GUARANTOR_NAME AS ADDRESSEE2, T4.GUARANTOR_ADDR1 AS ADDR1, T4.GUARANTOR_ADDR2 AS ADDR2, T4.GUARANTOR_ZIPCD AS ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARANTOR_ADDRESS_DAT WHERE '" + param._edate + "' BETWEEN ISSUEDATE AND EXPIREDATE GROUP BY SCHREGNO) T3 ON ");
                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                    stb.append(" LEFT JOIN GUARANTOR_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                    stb.append(" LEFT JOIN GUARANTOR_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._edate + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = '" + student._schregno + "' ");
                } else {
                    stb.append(" SELECT T0.SCHREGNO, T2.SEND_NAME AS ADDRESSEE, T2.SEND_NAME AS ADDRESSEE2, T2.SEND_ADDR1 AS ADDR1, T2.SEND_ADDR2 AS ADDR2, T2.SEND_ZIPCD AS ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO AND T2.DIV = '1' ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = '" + student._schregno + "' ");
                }

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String addressee = null != rs.getString("ADDRESSEE2") ? rs.getString("ADDRESSEE2") : rs.getString("ADDRESSEE");
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String zipcd = rs.getString("ZIPCD");
                    student._address = new Address(addressee, addr1, addr2, zipcd);
                }
                
            } catch (SQLException e) {
                log.error("SQLException!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
    
    /**
     * 1日出欠データ
     */
    private static class Attendance {
        
        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 公欠 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _leave;
        /** 出停伝染病 */
        final int _virus;
 
        final int _koudome;
        
        public Attendance() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int leave,
                final int virus,
                final int koudome
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _leave = leave;
            _virus = virus;
            _koudome = koudome;
        }
        
        public String toString() {
            return "[lesson=" + _lesson + 
            ",mlesson=" + _mlesson + 
            ",mourning=" + _mourning + 
            ",suspend=" + _suspend + 
            ",abroad=" + _abroad +
            ",absence=" + _absence + 
            ",attend=" + _attend + 
            ",late=" + _late +
            ",leave=" + _leave;
        }
    }
    
    /**
     * 科目ごとの出欠データ
     */
    private static class SubclassAttendance {
        final int _lesson;
        /** 換算前の欠席数 */
        final BigDecimal _rawSick;
        /** 換算後の欠課数 */
        final BigDecimal _sick;
        /** 公欠 */
        final int _absent;
        /** 遅刻早退 */
        final int _lateearly;
        /** 換算前の合併欠席数 */
        final BigDecimal _rawReplacedSick;
        /** 換算後の合併欠課数 */
        final BigDecimal _replacedSick;
        
        String _subclassCd = null;
        
        public SubclassAttendance(
                final BigDecimal lesson,
                final BigDecimal rawSick,
                final BigDecimal sick,
                final BigDecimal absent,
                final BigDecimal late,
                final BigDecimal early,
                final BigDecimal rawReplacedSick,
                final BigDecimal replacedSick) {
            _lesson = lesson.intValue();
            _rawSick = rawSick;
            _sick = sick;
            _absent = absent.intValue();
            _lateearly = late.add(early).intValue();
            _rawReplacedSick = rawReplacedSick;
            _replacedSick = replacedSick;
        }
        
        public String getRawSick() {
            return (_rawSick == null) ? null : formatBigDecimal(_rawSick);
        }
        
        public String getSick() {
            return (_sick == null) ? null : formatBigDecimal(_sick);
        }
        
        public String getRawReplacedSick() {
            return (_rawReplacedSick == null) ? null : formatBigDecimal(_rawReplacedSick);
        }
        
        public String getReplacedSick() {
            return (_replacedSick == null) ? null : formatBigDecimal(_replacedSick);
        }
        
        public String getLesson() {
            return String.valueOf(_lesson);
        }
        
        private String getKekkaString() {
            return "SubclassAttendance(lesson = " + _lesson + " , rawSick = " + _rawSick + ", sick = " + _sick + " , absent = " + _absent + 
            " , lateearly = " + _lateearly + ((_replacedSick.intValue() != 0) ? " , replacedSick = " + _replacedSick : "") + ((_rawReplacedSick.intValue() != 0) ? " , rawReplacedSick = " + _rawReplacedSick : "") + ")";
        }
        
        public String toString() {
            return getKekkaString();
        }
        
        public String getLateEarly() {
            return formatInt(_lateearly);
        }
        
        public String getKoketsu() {
            return formatInt(_absent);
        }
        
        private String formatInt(int n) {
            return n == 0 ? "" : String.valueOf(n);
        }
        
        private String formatBigDecimal(BigDecimal n) {
            return n == null ? null : (n.intValue() == 0) ? formatInt(0) : String.valueOf(n);
        }
    }
    
    /**
     * 生徒の科目ごとの成績・出欠データ
     */
    private static class Subclass {
        final String _schregno;
        final String _subclassCd;
        final String _classCd;
        
        final Map _testKindTableMap = new HashMap();
        
        final Map _attendSubclassMap = new HashMap();
        
        final Map _slumpMap = new HashMap();
        
        public Subclass(
                final String schregno,
                final String subclassCd
        ) {
            _schregno = schregno;
            _subclassCd = subclassCd;
            _classCd = subclassCd.substring(0, 2);
        }
        
        private Map findTestKindTableMap(final String testKindCd) {
            if (!_testKindTableMap.containsKey(testKindCd)) {
                _testKindTableMap.put(testKindCd, new HashMap());
            }
            return (Map) _testKindTableMap.get(testKindCd);
        }
        
        public void setScoreValue(final String testKindCd, final String tableDiv, final ScoreValue sv) {
            findTestKindTableMap(testKindCd).put(tableDiv, sv);
        }
        
        public ScoreValue getScoreValue(final String testKindCd, final String tableDiv) {
            return (ScoreValue) findTestKindTableMap(testKindCd).get(tableDiv);
        }
        
        public ScoreValue getScoreValue(final String testKindCd) {
            return (ScoreValue) findTestKindTableMap(testKindCd).get(Param.REC);
        }
        
        /**
         * 学期ごとの科目出欠をセットする
         * @param semester 学期
         * @param sa 科目出欠
         */
        public void setAttendance(final String semester, final SubclassAttendance sa) {
            _attendSubclassMap.put(semester, sa);
        }
        
        /**
         * 指定学期の科目出欠を得る
         * @param semester 指定学期
         * @return 指定学期の科目出欠
         */
        public SubclassAttendance getAttendance(final String semester) {
            return (SubclassAttendance) _attendSubclassMap.get(semester);
        }
        
        public void setSlump(final String testKind, final String slump) {
            _slumpMap.put(testKind, slump);
        }
        
        public boolean isSlump(final String testKind) {
            return SLUMP_CD.equals(_slumpMap.get(testKind));
        }
        
        public String toString() {
            return _schregno + " : " + _subclassCd + ":" + _testKindTableMap;
        }
    }
    
    /**
     * 欠課数上限値
     */
    private static class AbsenceHigh {
        /** 履修上限 */
        final String _compAbsenceHigh;
        /** 修得上限 */
        final String _getAbsenceHigh;
        
        public AbsenceHigh(final String absenceHigh, final String getAbsenceHigh) {
            _compAbsenceHigh = absenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
        }
        
        /**
         * 履修上限を超えているか
         * @param kekka 欠課数
         * @return
         */
        public boolean isRishuOver(final String kekka) {
            return isOver(kekka, _compAbsenceHigh);
        }
        
        /**
         * 修得上限を超えているか
         * @param kekka 欠課数
         * @return
         */
        public boolean isShutokuOver(final String kekka) {
            return isOver(kekka, _getAbsenceHigh);
        }
        
        private static boolean isOver(final String kekka, final String absenceHigh) {
            if (null == kekka || !NumberUtils.isNumber(kekka) || Double.parseDouble(kekka) == 0) {
                return false;
            }
            return absenceHigh == null || Double.parseDouble(absenceHigh) < Double.parseDouble(kekka);
        }
        
        public String toString() {
            return " 履修上限値" + _compAbsenceHigh + " , 修得上限値" + _getAbsenceHigh;
        }
        
        // ------
        
        /**
         * 生徒に欠課数上限値をセットする
         * @param db2
         * @param student 生徒
         * @param param
         */
        public static void setAbsenceHigh(final DB2UDB db2, final Student student, final Param param) {
            String absenceHighSql = "";
            String spAbsenceHighSql = "";
            if (param._knjSchoolMst.isHoutei()) {
                absenceHighSql = sqlHouteiJisu(student, null, param, false);
                spAbsenceHighSql = sqlHouteiJisu(student, null, param, true);
            } else {
                absenceHighSql = sqlJituJisuSql(student, null, param, false);
                spAbsenceHighSql = sqlJituJisuSql(student, null, param, true);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(absenceHighSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String absenceHigh = StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "0");
                    final String getAbsenceHigh = StringUtils.defaultString(rs.getString("GET_ABSENCE_HIGH"), "0");
                    
                    student._subclassAbsenceHigh.put(rs.getString("SUBCLASSCD"), new AbsenceHigh(absenceHigh, getAbsenceHigh));
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                ps = db2.prepareStatement(spAbsenceHighSql);
                rs = ps.executeQuery();
                student._spSubclassAbsenceHigh.clear();
                while (rs.next()) {
                    final String compAbsenceHigh = StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "0");
                    final String getAbsenceHigh = StringUtils.defaultString(rs.getString("GET_ABSENCE_HIGH"), "0");
                    
                    student._spSubclassAbsenceHigh.put(rs.getString("SPECIAL_GROUP_CD"), new AbsenceHigh(compAbsenceHigh, getAbsenceHigh));
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static String sqlHouteiJisu(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.GRADE = T1.GRADE AND ");
            stb.append("       T2.COURSECD = T1.COURSECD AND ");
            stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
            stb.append("       T2.YEAR = T1.YEAR AND ");
            stb.append("       T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T2.SCHREGNO = '" + student._schregno + "' ");
            return stb.toString();
        }
        
        private static String sqlJituJisuSql(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
            final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            stb.append("        AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            stb.append("        AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
            if (!isGroup) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                }
                stb.append("       T3.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.SUBCLASSCD ");
            } else {
                stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
            }
            stb.append("       AND T3.COURSECD = T2.COURSECD ");
            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       AND T3.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.DIV = '2' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
            return stb.toString();
        }
    }
    
    private static class ScoreValue {
        final String _testKindcd;
        final String _subclassCd;
        final String _value;
        
        public ScoreValue(
                final String testKindcd,
                final String subclassCd,
                final String value) {
            _testKindcd = testKindcd;
            _subclassCd = subclassCd;
            _value = value;
        }
        
        public String toString() {
            return "[value = " + _value + "]";
        }
    }
    
    /**
     * 序列データ
     */
    private static class ScoreRank {
        final String _tableDiv;
        final String _testKindcd;
        final String _totalScore;
        final String _avgScore;
        final String _gradeRank;
        final String _gradeAvgRank;
        final String _gradeCount;
        final String _courseRank;
        final String _courseAvgRank;
        final String _courseCount;
        final String _classRank;
        final String _classAvgRank;
        final String _classCount;
        final String _courseGroupRank;
        final String _courseGroupAvgRank;
        final String _courseGroupCount;
        
        public ScoreRank(final String tableDiv,
                final String testKindCd,
                final String totalScore,
                final String avgScore,
                final String gradeRank,
                final String gradeAvgRank,
                final String gradeCount,
                final String courseRank,
                final String courseAvgRank,
                final String courseCount,
                final String classRank,
                final String classAvgRank,
                final String classCount,
                final String courseGroupRank,
                final String courseGroupAvgRank,
                final String courseGroupCount) {
            _tableDiv = tableDiv;
            _testKindcd = testKindCd;
            _totalScore = totalScore;
            _avgScore = avgScore;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _gradeCount = gradeCount;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseCount = courseCount;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classCount = classCount;
            _courseGroupRank = courseGroupRank;
            _courseGroupAvgRank = courseGroupAvgRank;
            _courseGroupCount = courseGroupCount;
        }
        
        public String toString() {
            return " ScoreRank " + _testKindcd + " (" + _totalScore + " , " + _avgScore + ") " + 
            "[" + _courseRank + " (AVE " + _courseAvgRank +  ") /" + _courseCount + " , " + 
            _classRank + " (AVE " + _classAvgRank +  ") / " + _classCount + "] (" + _tableDiv + ")";  
        }
    }
    
    /**
     * 学期
     */
    private static class Semester {
        public final String _cd;
        public final String _name;
        public final String _sdate;
        public final String _edate;
        public Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        
        public String toString() {
            return "(" + _name + " [" + _sdate + "," + _edate + "])";
        }
    }
    
    
    /**
     * 通知書所見
     */
    private static class HReportRemark {
        public final String _semester;
        public final String _specialActRemark;
        public final String _totalStudyTime;
        public final String _communication;
        public final String _remark3;
        
        public HReportRemark(final String semester,
                final String specialActRemark,
                final String totalStudyTime,
                final String communication,
                final String remark3) {
            _semester = semester;
            _specialActRemark = specialActRemark;
            _totalStudyTime = totalStudyTime;
            _communication = communication;
            _remark3 = remark3;
        }
        
        // -----
        
        /**
         * 通知書所見をセットする
         * @param db2
         * @param students
         * @param param
         */
        public static void setHReportRemark(final DB2UDB db2, final List students, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER ");
            stb.append("     ,SPECIALACTREMARK ");
            stb.append("     ,TOTALSTUDYTIME ");
            stb.append("     ,COMMUNICATION");
            stb.append("     ,REMARK3");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHREGNO = ? ");
            
            final String sql = stb.toString();
            try {
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    student._hReportRemarks.clear();
                    
                    final Map hreportRemarks = new HashMap();
                    
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String specialActRemark = rs.getString("SPECIALACTREMARK");
                        final String totalStudyTime = rs.getString("TOTALSTUDYTIME");
                        final String communication = rs.getString("COMMUNICATION");
                        final String remark3 = rs.getString("REMARK3");
                        
                        final HReportRemark hreportremark = new HReportRemark(semester, specialActRemark, totalStudyTime, communication, remark3);
                        hreportRemarks.put(semester, hreportremark);
                    }
                    student._hReportRemarks.putAll(hreportRemarks);
                }
                
            } catch (SQLException e) {
                log.error("sql exception! :" + sql, e);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static class SubclassInfo {
        final static int GAPPEI_NASI = 0;
        final static int GAPPEI_MOTO = 1;
        final static int GAPPEI_MOTO_SAKI = 2;
        final static int GAPPEI_SAKI = 9;
        final static String GAPPEI_TANNI_KOTEI = "1";
        final static String GAPPEI_TANNI_KASAN = "2";
        
        final String _classabbv;
        final String _subclassname; 
        final String _credits;
        final String _compCredit;
        final String _getCredit;
        final String _subclassCd; 
        final String _attendSubclasscd;
        final String _combinedSubclasscd;
        final String _namespare1;
        final Subclass _subclass;
        
        final int _replaceflg; // 0:合併設定なし、1:元科目、2:先科目かつ元科目、9:先科目

        final String _calculateCreditFlg;
        
        final String _num90;
        final String _num90Other;
        
        public SubclassInfo(
                final String classabbv,
                final String subclassname,
                final String credits,
                final String compCredit,
                final String getCredit,
                final String subclassCd,
                final String attendSubclasscd,
                final String combinedSubclasscd,
                final String namespare1,
                final Subclass subclass,
                final int replaceFlg,
                final String calculateCreditFlg,
                final String num90,
                final String num90Other) {
            _classabbv = classabbv;
            _subclassname = subclassname;
            _credits = credits;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _subclassCd = subclassCd;
            _attendSubclasscd = attendSubclasscd;
            _combinedSubclasscd = combinedSubclasscd;
            _namespare1 = namespare1;
            _subclass = subclass;
            
            _replaceflg = replaceFlg;
            _calculateCreditFlg = calculateCreditFlg;            
            _num90 = num90;
            _num90Other = num90Other;
        }
        
        public boolean isPrintCreditMstCredit() {
            if (_credits == null || GAPPEI_SAKI == _replaceflg && GAPPEI_TANNI_KASAN.equals(_calculateCreditFlg)) {
                return false;
            }
            return true;
        }

        public boolean isNotPrintSubclassList(final Param param) {
            boolean rtn = false;
            
            if (GAPPEI_MOTO == _replaceflg && param._isNoPrintMoto) { rtn = true; }
            
            if (param.isD026ContainSubclasscd(_subclassCd)) { rtn = true; }
            
            return rtn;
        }
    }
    
    /**
     * パラメータクラス
     */
    private static class Param {
        
        static final String SEMEALL = "9";
        static final String REC = "REC  ";
        static final String _39900 = "39900";
        static final String _99900 = "99900";
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade_hr_class;
        
        private String _sdate;
        private final String _edate;
        private final String[] _dispSemester;
        private final String[] _testKindCds;
        private final String[] _selectSchregno;
        
        private final String _addrPrint;
        private final String _addrDiv;
        private final String _groupDiv;
        
        private String _schoolName;
        private String _z010name1;
        private String _principalName;
        private String _jobName;
        private String _hrJobName;
        private String _schoolAddress;
        private String _schoolTelNo;
        private List _staffNames;
        private KNJSchoolMst _knjSchoolMst;
        
        private KNJDefineSchool _definecode;
        
        private Map _attendSemesMap;
        private TreeMap _semesterMap;
        final String _rankDiv;
        /** D016 */
        private boolean _isNoPrintMoto;
        /** D026 */
        private final List _d026List = new ArrayList();
        /** D016 */
        private boolean _isMirishuu;
        
        final String _documentRoot;
        private String _imagePath;
        private String _extension;
        private File _logoFile;
        
        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;
        
        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final Map _attendParamMap;
        
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEME");            
            _edate = request.getParameter("DATE").replace('/', '-');
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            _selectSchregno = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _addrPrint = request.getParameter("ADDR_PRINT");
            _addrDiv = request.getParameter("ADDR_DIV");
            _groupDiv = request.getParameter("GROUP_DIV");
            
            _rankDiv = request.getParameter("RANK_DIV");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _dispSemester = new String[]{"1", "2", SEMEALL};

            final String gakunenmatuCd = _99900;  
            _testKindCds = new String[]{"19900", "29900", gakunenmatuCd};
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            load(db2);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        public boolean isGakunenmatsu() {
            return Param.SEMEALL.equals(_semester);
        }
        
        public boolean isGakunenmatu(final String semTestKindCd) {
            return SEMEALL.equals(semTestKindCd.substring(0,1));
        }
        
        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        public String getRegdSemester() {
            return isGakunenmatsu() ? _ctrlSemester : _semester;
        }
        
        public String getMaxSemester() {
            try {
                return _knjSchoolMst._semesterDiv;
            } catch (Exception ex) {
                log.error("getMaxSemester exception!", ex);
            }
            try {
                return String.valueOf(_definecode.semesdiv);
            } catch (Exception ex) {
                log.error("getMaxSemester exception!", ex);
            }
            return null;
        }
        
        public String[] getTargetSemester() {
            final List list = new ArrayList();
            for (int i = 0; i < _dispSemester.length; i++) {
                final String dseme = _dispSemester[i];
                if (_semester.compareTo(dseme) >= 0) {
                    list.add(dseme);
                }
            }
            
            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }
        
        public String[] getTargetTestKindCds() {
            final List list = new ArrayList();
            for (int i = 0; i < _testKindCds.length; i++) {
                final String testkind = _testKindCds[i];
                if (_semester.compareTo(testkind.substring(0, 1)) >= 0) {
                    list.add(testkind);
                }
            }
            
            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }
        
        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _hrJobName = rs.getString("REMARK2");
                    _schoolAddress = rs.getString("REMARK4");
                    _schoolTelNo = rs.getString("REMARK5");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        public void load(final DB2UDB db2) {
            try {                
                loadNameMstD016(db2);
                loadNameMstD026(db2);
                loadNameMstZ010(db2);
            } catch (SQLException e) {
                log.error("名称マスタ読み込みエラー", e);
            }
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }
            
            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);
            if (log.isDebugEnabled()) {
                log.debug("schoolmark=" + _definecode.schoolmark + " *** semesdiv=" + _definecode.semesdiv + " " + 
                        "*** absent_cov=" + _definecode.absent_cov + " *** absent_cov_late=" + _definecode.absent_cov_late);
            }
            
            loadSemester(db2);
            loadControlMst(db2);
            
            final File file = new File(_documentRoot + "/" + _imagePath + "/" + "SCHOOLLOGO." + _extension);
            _logoFile = file.exists() ? file : null;
            
            _staffNames = getStaffNames(db2, isGakunenmatsu() ? _ctrlSemester : _semester);
            
            setCertifSchoolDat(db2);
        }
        
        public String getNendo() {
            return _year + "年度";
        }
        
        private void loadSemester(final DB2UDB db2) {
            _semesterMap = new TreeMap();
            
            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' ORDER BY SEMESTER";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    final String cd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    final Semester semester = new Semester(cd, name, sdate, edate);
                    _semesterMap.put(cd, semester);
                    
                    if (first) {
                        _sdate = sdate;
                        first = false;
                    }
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        public String getSemesterName(final String semester) {
            Semester s = (Semester) _semesterMap.get(semester);
            return s == null ? null : s._name;
        }
        
        private void loadNameMstZ010(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z010'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                _z010name1 = rs.getString("NAME1");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.debug("(名称マスタZ010):学校名 = " + _z010name1);
        }
        
        private void loadNameMstD016(final DB2UDB db2) throws SQLException {
            _isNoPrintMoto = false;
            _isMirishuu = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAMESPARE1");
                final String name2 = rs.getString("NAMESPARE2");
                if ("Y".equals(name)) _isNoPrintMoto = true;
                if ("Y".equals(name2)) _isMirishuu = true;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.debug("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
            log.debug("(名称マスタD016):未履修 = " + _isMirishuu);
        }
        
        
        private void loadNameMstD026(final DB2UDB db2) {
            
            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE NAMECD1 = 'D026' AND " + field + " = '1'  ");
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public boolean isD026ContainSubclasscd(String subclasscd) {
            if (null == subclasscd) {
                return false;
            }
            if ("1".equals(_useCurriculumcd)) {
                if ("1".equals(_useClassDetailDat)) {
                } else if (StringUtils.split(subclasscd, "-").length == 3) {
                    subclasscd = StringUtils.split(subclasscd, "-")[3]; // clascd '-' school_kind '-' curriculum_cd '-' subclasscd
                }
            }
            return _d026List.contains(subclasscd);
        }
        
        public List getStaffNames(final DB2UDB db2, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List list = new LinkedList();
            try{
                StringBuffer stb = new StringBuffer();
                stb.append("SELECT  (SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD1) AS TR_NAME1 ");
                stb.append(       ",(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD2) AS TR_NAME2 ");
                stb.append(       ",(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD3) AS TR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
                stb.append("    AND T1.SEMESTER = '" + semester + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if( rs.getString("TR_NAME1") != null )list.add( rs.getString("TR_NAME1") );
                    if( rs.getString("TR_NAME2") != null )list.add( rs.getString("TR_NAME2") );
                    if( rs.getString("TR_NAME3") != null )list.add( rs.getString("TR_NAME3") );
                }
            } catch (Exception ex) {
                log.error("List Staff_name() Staff_name error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
    
    private static class KNJD185Form {
        protected DB2UDB _db2;
        protected Param _param;
        protected Vrw32alp _svf;
        
        private static final int MAX_RECORD = 17;

        public KNJD185Form(DB2UDB db2, Vrw32alp svf, KNJD185.Param param) {
            this._db2 = db2;
            this._svf = svf;
            this._param = param;
        }
        
        public void print(final Student student) {

            log.debug(" student = " + student + "(" + student._attendNo + ")");
            
            final String formName;
            if ("1".equals(_param._addrPrint)) {
                formName = "KNJD185.frm";
            } else {
                formName = "KNJD185_2.frm";
            }
            log.debug(" FORM = " + formName);

            _svf.VrSetForm(formName, 1);

            printHeader(_svf, student);
            printAddress(_svf, student);
            printTsushinran(_svf, student);

            printRecDetail(_svf, student);

            _svf.VrEndPage();
        }
        
        /**
         * 通信欄を印字
         * @param svf
         * @param student
         */
        private void printTsushinran(final Vrw32alp svf, final Student student) {
            final HReportRemark reportRemark9  = (HReportRemark) student._hReportRemarks.get("9");
            if (null != reportRemark9) {
                final String[] specialActRemark = get_token(reportRemark9._specialActRemark, 20, 10);
                for (int j = 0; j < specialActRemark.length; j++) {
                    svf.VrsOutn("SPECIALACTREMARK", j + 1, specialActRemark[j]); // 特別活動の記録
                }
            }
            final HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(_param.isGakunenmatsu() ? _param.getMaxSemester() : _param._semester);
            if (null != reportRemark) {
                final String[] communication = get_token(reportRemark._communication, 20, 10);
                for (int j = 0; j < communication.length; j++) {
                    svf.VrsOutn("COMMUNICATION", j + 1, communication[j]); // 通信欄
                }
            }
        }

        /**
         * ヘッダ印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alp svf, final Student student) {
            svf.VrsOut("TITLE", "通　知　表"); //
            svf.VrsOut("NENDO", _param.getNendo() + ((Semester) _param._semesterMap.get(_param._semester))._name); // 年度
            svf.VrsOut("COURSE", student._courseCodeName); // 課程、学科
            svf.VrsOut("HR_NAME", student._hrName + student._attendNo); // 年組番
            svf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 30 ? "2" : "1"), student._name); // 生徒氏名

            final String[] semesScore = new String[] {"1", "2", Param.SEMEALL};
            for (int i = 0; i < semesScore.length; i++) {
                svf.VrsOut("RANK_NAME" + String.valueOf(i + 1), ("1".equals(_param._groupDiv) ? "学年" : "2".equals(_param._groupDiv) ? "コース" : "3".equals(_param._groupDiv) ? "グループ" : "") + "順位");
                final Semester semesterObj = (Semester) _param._semesterMap.get(semesScore[i]);
                if (null != semesterObj) {
                    svf.VrsOut("SEMESTER" + String.valueOf(i + 1), semesterObj._name); // 考査名称
                }
            }
            final String[] semesAttend = new String[] {"1", "2", "3", Param.SEMEALL};
            for (int i = 0; i < semesAttend.length; i++) {
                final Semester semesterObj = (Semester) _param._semesterMap.get(semesAttend[i]);
                if (null != semesterObj) {
                    svf.VrsOutn("SEMESTERNAME", i + 1, semesterObj._name); // 学期名（出欠）
                }
            }
            svf.VrsOut("PRINCIPAL_NAME" + (getMS932ByteLength(_param._principalName) > 28 ? "2" : "1"), _param._principalName); // 校長名
            if (_param._staffNames != null) {
                final String staffname = (String) _param._staffNames.get(0);
                svf.VrsOut("STAFFNAME" + (getMS932ByteLength(staffname) > 28 ? "2" : "1"), staffname); // 担任名
            }
            svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }
        }

        /**
         * 宛先を印字
         * @param svf
         * @param student
         */
        private void printAddress(final Vrw32alp svf, final Student student) {
            if ("1".equals(_param._addrPrint) && student._address != null) {
                final String addressee = student._address._addressee == null ? "" : student._address._addressee + "  様";
                final int check1 = getMS932ByteLength(student._address._address1);
                final int check2 = getMS932ByteLength(student._address._address2);
                final boolean useAddressLarge = check1 > 36 || check2 > 36;
                final int check3 = getMS932ByteLength(addressee);
                final boolean useNameLarge = check3 > 36;
                
//                if (!_param._isNotPrintAddressZipcd) {
                    svf.VrsOut(useAddressLarge ? "ADDR1_2" : "ADDR1_1", student._address._address1);     //住所
                    svf.VrsOut(useAddressLarge ? "ADDR2_2" : "ADDR2_1", student._address._address2);     //住所
                    svf.VrsOut("ZIPCD", student._address._zipcd);
//                }
                svf.VrsOut(useNameLarge ? "ADDRESS2" : "ADDRESSEE", addressee); // 受取人
            }
        }

        private int printRecDetail (
                final Vrw32alp svf,
                final Student student
        ) {
            boolean hasData = false;
            int i = 0;
            
            boolean bsubclass90 = false;
            
            printAttendance(svf, student);
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                printAttendance(svf, student);
                printScoreRank(svf, student);
                
                if (_param.isGakunenmatsu()) {
                    _svf.VrsOut("GETCREDIT", String.valueOf(student._totalCredit));
                }

                final SubclassInfo info = (SubclassInfo) it.next();
                
                if (info.isNotPrintSubclassList(_param)) { continue; }

                if (hasData) {
                    i++;
                }

                if ((is90(info._subclassCd) || info._num90Other != null) && ! bsubclass90) {
                    i += blankRecord(svf, info._num90, i, MAX_RECORD, "CLASS");
                   bsubclass90 = true;
                }
                
                hasData= printSubclassInfo(svf, info, student, (i + 1));
            }
            return i;
        }

        private void printAttendance(final Vrw32alp svf, final Student student) {

            final String[] semes = new String[] {"1", "2", "3", Param.SEMEALL};
            for (int i = 0; i < semes.length; i++) {
                if (_param._semester.compareTo(semes[i]) < 0) {
                    continue;
                }
                final Attendance sum = student.getAttendance(semes[i]);
                svf.VrsOutn("LESSON", i + 1, String.valueOf(sum._lesson)); // 授業日数
                svf.VrsOutn("MOURNING", i + 1, String.valueOf(sum._mourning + sum._suspend + sum._virus + sum._koudome)); // 出停・忌引等日数
                svf.VrsOutn("PRESENT", i + 1, String.valueOf(sum._mlesson)); // 出席すべき日数
                svf.VrsOutn("ABSENCE", i + 1, String.valueOf(sum._absence)); // 欠席日数
                svf.VrsOutn("ATTEND", i + 1, String.valueOf(sum._attend)); // 出席日数
                svf.VrsOutn("LATE", i + 1, String.valueOf(sum._late)); // 遅刻回数
                svf.VrsOutn("EARLY", i + 1, String.valueOf(sum._leave)); // 早退回数
            }
        }
        
        private boolean printSubclassInfo (
                final Vrw32alp svf,
                final SubclassInfo si,
                final Student student,
                int line
        ) {
            svf.VrsOutn("CLASS", line, si._classabbv);
            final int subnamelen = getMS932ByteLength(si._subclassname);
            svf.VrsOutn("SUBCLASS" + (subnamelen > 30 ? "3" : subnamelen > 22 ? "2" : ""),  line, si._subclassname);
            
            if (si.isPrintCreditMstCredit()) {
                svf.VrsOutn("CREDIT", line, si._credits);
            }

            Subclass subclass = student.getSubclass(si._subclassCd);
            if (subclass == null) {
                return true;
            }

            if (SubclassInfo.GAPPEI_NASI == si._replaceflg  || SubclassInfo.GAPPEI_MOTO == si._replaceflg  || SubclassInfo.GAPPEI_SAKI == si._replaceflg) {
                // log.debug(" 科目= " + si._subclassname + " ( " + si._subclassCd + " )");
                
                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String fieldValue = "VALUE" + getFieldColumnTestKind(semTestKindCd);
                    final ScoreValue sv;
                    sv = subclass.getScoreValue(semTestKindCd);

                    if (sv != null) {
                        // log.debug(" semTestKindCd = " + semTestKindCd + " "+ sv);
                        
                        if (sv._value != null) {
                            svf.VrsOutn(fieldValue, line, sv._value);
                        }
                    }

                }
                final String[] semes = new String[] {"1", "2", Param.SEMEALL};
                
                for (int i = 0; i < semes.length; i++) {
                    if (_param._semester.compareTo(semes[i]) < 0) {
                        continue;
                    }
                    
                    SubclassAttendance sa = subclass.getAttendance(semes[i]);
                    if (sa == null) {
                        continue;
                    }
                    String sick = (SubclassInfo.GAPPEI_SAKI == si._replaceflg) ? sa.getReplacedSick() : sa.getSick();
                    
                    svf.VrsOutn("ABSENT" + (i + 1), line, sick == null ? "" : String.valueOf(sick)); // 欠課時数
                }
            }
            return true;
        }
        
       protected int blankRecord (
                final Vrw32alp svf,
                final String num90,
                final int line,
                final int maxLine,
                final String field
        ) {
            int intnum90 = parseInt(num90);
            if (0 == intnum90) return 0;
            int i = line + 1;
            i = (i % maxLine == 0)? maxLine: i % maxLine;
            if (1 == i) return 0;
            svf.VrsOutn(field, i, " ");
            return 1;
        }
        
        private void svfFieldAttribute_CLASS (
                final Vrw32alp svf,
                final String field,
                final String name,
                final int ln
        ) {
            svf.VrsOut(field,  name );
        }

        private void svfFieldAttribute_SUBCLASS (
                final Vrw32alp svf,
                final String field,
                final String name,
                final int ln
        ) {
            svf.VrsOut(field + (name != null && name.length() > 13 ? "2" : "1"),  name );
        }
        
        private Map getSubclassInfoMap(final List subclassInfoList) {
            final Map map = new HashMap();
            if (null != subclassInfoList) {
                for (Iterator it = subclassInfoList.iterator(); it.hasNext();) {
                    final SubclassInfo si = (SubclassInfo) it.next();
                    map.put(si._subclassCd, si);
                }
            }
            return map;
        }

        protected void printScoreRank(final Vrw32alp svf, final Student student) {

            final String[] semTestKindCds = _param.getTargetTestKindCds();
            for (int k = 0; k < semTestKindCds.length; k++) {
                final ScoreRank sr;
                sr = student.getScoreRank(semTestKindCds[k]);
                if (sr != null) {
                    if (NumberUtils.isNumber(sr._avgScore)) {
                        // log.debug(" score rank = " + sr);
                        String avgScore = new BigDecimal(sr._avgScore).setScale(1, BigDecimal.ROUND_HALF_UP).toString(); 
                        svf.VrsOut("AVERAGE" + getFieldColumnTestKind(semTestKindCds[k]), avgScore); // 平均
                    }
                    final String rank;
                    if ("1".equals(_param._rankDiv)) {
                        rank = "2".equals(_param._rankDiv) ? sr._gradeAvgRank : sr._gradeRank;
                    } else if ("2".equals(_param._rankDiv)) {
                        rank = "2".equals(_param._rankDiv) ? sr._courseAvgRank : sr._courseRank;
                    } else {
                        rank = "2".equals(_param._rankDiv) ? sr._courseGroupAvgRank : sr._courseGroupRank;
                    }
                    svf.VrsOut("RANK" + getFieldColumnTestKind(semTestKindCds[k]), rank); // 席次
                }
            }
        }
                
        protected boolean is90(final String  subclassCd) {
            String classcd;
            if ("1".equals(_param._useCurriculumcd) && StringUtils.split(subclassCd).length == 4) {
                classcd = StringUtils.split(subclassCd)[0];
            } else {
                classcd = subclassCd.substring(0, 2);
            }
            return KNJDefineSchool.subject_T.equals(classcd);
        }
        
        protected int parseInt(final String str) {
            if (null == str || StringUtils.isEmpty(str) || !StringUtils.isNumeric(str)) {
                return 0;
            }
            return Integer.parseInt(str);
        }
        

        protected boolean isCharacter (final String str) {
            return !StringUtils.isEmpty(str) && !StringUtils.isNumeric(str);
        }
        
        protected String zeroBlank(int n) {
            return (n == 0) ? "" : String.valueOf(n);
        }
        
        protected int getFieldColumnSemester(String semester) {
            final String[] semesters = _param.getTargetSemester();
            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equals(semester)) {
                    return (i + 1);
                }
            }
            return 0;
        }

        protected int getFieldColumnTestKind(String semTestKindCd) {
            final String[] testKindCds = _param.getTargetTestKindCds();
            for (int i = 0; i < testKindCds.length; i++) {
                if (testKindCds[i].equals(semTestKindCd)) {
                    return (i + 1);
                }
            }
            return 0;
        }
        
        protected static String[] get_token(String strx,int f_len,int f_cnt) {
            final String[] token = KNJ_EditEdit.get_token(strx, f_len, f_cnt);
            if (null == token) {
                return new String[]{};
            }
            return token;
        }
    }
}
