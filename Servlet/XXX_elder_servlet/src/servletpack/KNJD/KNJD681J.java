/*
 * $Id: 2bf0e8adad3181fba6fe8c8b9b53c4eef057e01a $
 *
 * 作成日: 2015/08/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 文京学園 中学校 学力向上の記録
 */
public class KNJD681J {

    private static final Log log = LogFactory.getLog(KNJD681J.class);

    private boolean _hasData;

    private static String SUBCLASSCD_333333 = "333333";
    private static String SUBCLASSCD_555555 = "555555";
    private static String SUBCLASSCD_777777 = "777777";
    private static String SUBCLASSCD_888888 = "888888";
    private static String SUBCLASSCD_999999 = "999999";

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

    private static String toString(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final Map testcdAvgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);

        final List studentList = Student.getStudentList(db2, _param);
        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            final String form = "KNJD681J.frm";
            svf.VrSetForm(form, 1);

            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　学力向上の記録"); // タイトル
            svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + "　" + (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) + "番" : (StringUtils.defaultString(student._attendno)) + "番")); // 年組
            svf.VrsOut("NAME", student._name); // 氏名
            if (null != student._substaffname) {
                svf.VrsOut("TEACHER_NAME", "担任　" + StringUtils.defaultString(student._staffname)); // 担任名
                svf.VrsOut("TEACHER_NAME_MARU1", "○");
                svf.VrsOut("TEACHER_NAME_IN1", "印");

                svf.VrsOut("TEACHER_NAME2", "副担任　" + StringUtils.defaultString(student._substaffname)); // 担任名
                svf.VrsOut("TEACHER_NAME_MARU2", "○");
                svf.VrsOut("TEACHER_NAME_IN2", "印");

            } else {
                svf.VrsOut("TEACHER_NAME2", "担任　" + StringUtils.defaultString(student._staffname)); // 担任名
                svf.VrsOut("TEACHER_NAME_MARU2", "○");
                svf.VrsOut("TEACHER_NAME_IN2", "印");
            }

            for (int ti = 0; ti < _param._testitemList.size(); ti++) {
                final Testitem testitem = (Testitem) _param._testitemList.get(ti);
                final String tline = String.valueOf(ti + 1);

                svf.VrsOut("SEMESTER_NAME" + testitem._semester, testitem._semestername); // 学期名
                svf.VrsOut("TEST_NAME" + tline, testitem._testitemabbv1); // 考査名
            }

            for (int i = 0; i < 3; i++) {
                final String sg = String.valueOf(i + 1);
                svf.VrsOut("GRADE" + sg, sg + "年"); // 学年
            }

            final List gradeList = new ArrayList(student._regdMap.keySet());
            for (int gi = 0; gi < gradeList.size(); gi++) { // 学年ごとに出力
                final Integer igrade = (Integer) gradeList.get(gi);
                final String sg = igrade.toString();

                final ScoreRegd regd = (ScoreRegd) student._regdMap.get(igrade);

                final Map subclassMap = new TreeMap(getMappedMap(student._subclassMap, igrade));
                final List subclasss = new ArrayList(subclassMap.values());
                Collections.sort(subclasss);

                for (int subi = 0; subi < subclasss.size(); subi++) {
                    final String ssubi = String.valueOf(subi + 1);
                    final Subclass subclass = (Subclass) subclasss.get(subi);

                    svf.VrsOut("SUBCLASS_NAME" + sg + "_" + ssubi, subclass._subclassabbv); // 科目名
                }

                for (int ti = 0; ti < _param._testitemList.size(); ti++) { // テストごとに出力
                    final Testitem testitem = (Testitem) _param._testitemList.get(ti);
                    final int line = ti + 1;
                    final String testcd = regd._year + "-" + testitem._semester + testitem._testcd;
                    final boolean preferCourseThanGrade = false; // igrade.intValue() == 3;

                    final Map scoreMap = getMappedMap(student._testcdScoreMap, testcd);
                    final Map avgDatMap = getMappedMap(testcdAvgDatMap, testcd);
                    final String gradeAvgDivKey = RecordAverageDat.getGradeAvgDivKey(regd._grade);
                    final String hrAvgDivKey = RecordAverageDat.getHrAvgDivKey(regd._grade, regd._hrClass);
                    final String courseAvgDivKey = RecordAverageDat.getCourseAvgDivKey(regd._grade, regd._coursecd, regd._majorcd, regd._coursecode);

//                    log.debug(" -- testcd = " + testcd);
//                    log.debug("    scoreMap = " + scoreMap);
//                    log.debug("    avgDatMap = " + avgDatMap);
//                    log.debug("    subclasscds = " + subclasscds);

                    for (int subi = 0; subi < subclasss.size(); subi++) {
                        final String ssubi = String.valueOf(subi + 1);
                        final Subclass subclass = (Subclass) subclasss.get(subi);

                        final Score s = (Score) scoreMap.get(subclass._subclasscd);
                        if (null != s) {
                            svf.VrsOutn("SCORE" + sg + "_" + ssubi, line, s._score); // 素点

                            final String deviation = sishaGonyu(preferCourseThanGrade ? s._courseDeviation : s._gradeDeviation);
                            svf.VrsOutn("DIV" + sg + "_" + (StringUtils.defaultString(deviation).length() > 4 ? "2" : "1") + "_" + ssubi, line, deviation); // 偏差値  学年(3年はコース)

                            svf.VrsOutn("COURSE_RANK" + sg + "_" + ssubi, line, toString(preferCourseThanGrade ? s._courseRank : s._gradeRank));
                        }

                        final RecordAverageDat avgDat = RecordAverageDat.get(avgDatMap, preferCourseThanGrade ? courseAvgDivKey : gradeAvgDivKey, subclass._subclasscd);
                        if (null != avgDat) {
                            final String avg = sishaGonyu(avgDat._avg);
                            svf.VrsOutn("AVERAGE" + sg + "_" + (StringUtils.defaultString(avg).length() > 4 ? "2" : "1") + "_" + ssubi, line, avg); // 平均 学年
                        }
                    }

                    final Score s3 = (Score) scoreMap.get(SUBCLASSCD_333333);
                    if (null != s3) {
                        svf.VrsOutn("RANK" + sg + "_1", line, toString(s3._classRank)); // 3科科目ランク クラス
                        svf.VrsOutn("RANK" + sg + "_2", line, toString(preferCourseThanGrade ? s3._courseRank : s3._gradeRank)); // 3科科目ランク 学年

                        final String dev3 = sishaGonyu(preferCourseThanGrade ? s3._courseDeviation : s3._gradeDeviation);
                        svf.VrsOutn("DIV" + sg + "_" + (StringUtils.defaultString(dev3).length() > 4 ? "2" : "1") + "_6", line, dev3); // 3科偏差値
                    }
                    final Score s5 = (Score) scoreMap.get(SUBCLASSCD_555555);
                    if (null != s5) {
                        svf.VrsOutn("RANK" + sg + "_3", line, toString(s5._classRank)); // 5科科目ランク クラス
                        svf.VrsOutn("RANK" + sg + "_4", line, toString(preferCourseThanGrade ? s5._courseRank : s5._gradeRank)); // 5科科目ランク 学年(3年はコース)

                        final String dev5 = sishaGonyu(preferCourseThanGrade ? s5._courseDeviation : s5._gradeDeviation);
                        svf.VrsOutn("DIV" + sg + "_" + (StringUtils.defaultString(dev5).length() > 4 ? "2" : "1") + "_7", line, dev5); // 5科偏差値
                    }

                    final RecordAverageDat avgDat3cl = RecordAverageDat.get(avgDatMap, hrAvgDivKey, SUBCLASSCD_333333);
                    if (null != avgDat3cl) {
                        svf.VrsOutn("NUM" + sg + "_1", line, avgDat3cl._count); // 3科人数 クラス
                    }
                    final RecordAverageDat avgDat3gr = RecordAverageDat.get(avgDatMap, preferCourseThanGrade ? courseAvgDivKey : gradeAvgDivKey, SUBCLASSCD_333333);
                    if (null != avgDat3gr) {
                        svf.VrsOutn("NUM" + sg + "_2", line, avgDat3gr._count); // 3科人数 学年(3年はコース)
                    }
                    final RecordAverageDat avgDat5cl = RecordAverageDat.get(avgDatMap, hrAvgDivKey, SUBCLASSCD_555555);
                    if (null != avgDat5cl) {
                        svf.VrsOutn("NUM" + sg + "_3", line, avgDat5cl._count); // 5科人数 クラス
                    }
                    final RecordAverageDat avgDat5gr = RecordAverageDat.get(avgDatMap, preferCourseThanGrade ? courseAvgDivKey : gradeAvgDivKey, SUBCLASSCD_555555);
                    if (null != avgDat5gr) {
                        svf.VrsOutn("NUM" + sg + "_4", line, avgDat5gr._count); // 5科人数 学年(3年はコース)
                    }
                }
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static class Score {
        final String _subclasscd;
        String _score;
        final BigDecimal _avg;
        final Integer _classRank;
        final Integer _classAvgRank;
        final BigDecimal _classDeviation;
        final Integer _courseRank;
        final Integer _courseAvgRank;
        final BigDecimal _courseDeviation;
        final Integer _gradeRank;
        final Integer _gradeAvgRank;
        final BigDecimal _gradeDeviation;

        Score(
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final Integer classRank,
                final Integer classAvgRank,
                final BigDecimal classDeviation,
                final Integer courseRank,
                final Integer courseAvgRank,
                final BigDecimal courseDeviation,
                final Integer gradeRank,
                final Integer gradeAvgRank,
                final BigDecimal gradeDeviation) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classDeviation = classDeviation;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseDeviation = courseDeviation;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _gradeDeviation = gradeDeviation;
        }

        public String toString() {
            return " Score { subclasscd : " + _subclasscd + ", score : " + _score + " ... } ";
        }
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;

        Subclass(
                final String subclasscd,
                final String subclassname,
                final String subclassabbv) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }

        public int compareTo(Object o) {
            final Subclass subclass = (Subclass) o;
            return _subclasscd.compareTo(subclass._subclasscd);
        }
    }

    private static class ScoreRegd {
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        public ScoreRegd(final String year, final String semester, final String grade, final String hrClass,
                final String coursecd,
                final String majorcd,
                final String coursecode) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }
        public String toString() {
            return " Regd { year : " + _year + ", semester : " + _semester + ", grade : " + _grade + " , hrClass : " + _hrClass + "}";
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffname;
        final String _substaffname;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final Map _subclassMap = new HashMap();
        final Map _regdMap = new TreeMap();
        final Map _testcdScoreMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String staffname,
            final String substaffname,
            final String attendno,
            final String schregno,
            final String name,
            final String coursecd,
            final String majorcd,
            final String coursecode) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffname = staffname;
            _substaffname = substaffname;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        private static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map schregMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("     SELECT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.SEMESTER, ");
            stb.append("       GDAT.GRADE_NAME1, ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       HDAT.HR_NAME, ");
            stb.append("       HRSTF.STAFFNAME, ");
            stb.append("       HRSUBSTF.STAFFNAME AS SUBSTAFFNAME, ");
            stb.append("       T1.ATTENDNO, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.COURSECD, ");
            stb.append("       T1.MAJORCD, ");
            stb.append("       T1.COURSECODE, ");
            stb.append("       BASE.NAME, ");
            stb.append("       REGD_SCORE.YEAR       AS SCORE_YEAR, ");
            stb.append("       REGD_SCORE.SEMESTER   AS SCORE_SEMESTER, ");
            stb.append("       REGD_SCORE.GRADE      AS SCORE_GRADE, ");
            stb.append("       REGD_SCORE.HR_CLASS   AS SCORE_HR_CLASS, ");
            stb.append("       REGD_SCORE.COURSECD   AS SCORE_COURSECD, ");
            stb.append("       REGD_SCORE.MAJORCD    AS SCORE_MAJORCD, ");
            stb.append("       REGD_SCORE.COURSECODE AS SCORE_COURSECODE ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("         AND GDAT.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
            stb.append("         AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("         AND HDAT.GRADE = T1.GRADE ");
            stb.append("         AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST HRSUBSTF ON HRSUBSTF.STAFFCD = HDAT.SUBTR_CD1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) SEMESTER ");
            stb.append("             FROM SCHREG_REGD_DAT ");
            stb.append("             WHERE (YEAR = '" + param._year + "' AND SEMESTER = '" + param._semester + "' OR YEAR < '" + param._year + "') ");
            stb.append("             GROUP BY SCHREGNO, YEAR ");
            stb.append("     ) SCORE_REGD1 ON SCORE_REGD1.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD_SCORE ON REGD_SCORE.SCHREGNO = SCORE_REGD1.SCHREGNO AND REGD_SCORE.YEAR = SCORE_REGD1.YEAR AND REGD_SCORE.SEMESTER = SCORE_REGD1.SEMESTER ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT_SCORE ON GDAT_SCORE.YEAR = REGD_SCORE.YEAR ");
            stb.append("         AND GDAT_SCORE.GRADE = REGD_SCORE.GRADE ");
            stb.append("         AND GDAT_SCORE.SCHOOL_KIND = 'J' ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester()  + "' ");
            stb.append("         AND T1.GRADE = '" + param._grade + "' ");
            stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregSelected) + " ");
            stb.append(" ) ");
            stb.append(" , TSUBGRP AS ( ");
            stb.append(" SELECT DISTINCT T1.SCHREGNO, T1.SCORE_YEAR, G3.CLASSCD, G3.SCHOOL_KIND, G3.CURRICULUM_CD, G3.SUBCLASSCD ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT G3 ON T1.YEAR = G3.YEAR ");
            stb.append("     AND G3.GROUP_DIV = '3' ");
            stb.append("     AND T1.GRADE = G3.GRADE ");
            stb.append("     AND T1.COURSECD = G3.COURSECD ");
            stb.append("     AND T1.MAJORCD = G3.MAJORCD ");
            stb.append("     AND T1.COURSECODE = G3.COURSECODE ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT T1.SCHREGNO, T1.SCORE_YEAR, G5.CLASSCD, G5.SCHOOL_KIND, G5.CURRICULUM_CD, G5.SUBCLASSCD ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT G5 ON T1.YEAR = G5.YEAR ");
            stb.append("     AND G5.GROUP_DIV = '5' ");
            stb.append("     AND T1.GRADE = G5.GRADE ");
            stb.append("     AND T1.COURSECD = G5.COURSECD ");
            stb.append("     AND T1.MAJORCD = G5.MAJORCD ");
            stb.append("     AND T1.COURSECODE = G5.COURSECODE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.HR_NAME, ");
            stb.append("   T1.STAFFNAME, ");
            stb.append("   T1.SUBSTAFFNAME, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   T1.SCORE_YEAR, ");
            stb.append("   T1.SCORE_SEMESTER, ");
            stb.append("   T1.SCORE_GRADE, ");
            stb.append("   T1.SCORE_HR_CLASS, ");
            stb.append("   T1.SCORE_COURSECD, ");
            stb.append("   T1.SCORE_MAJORCD, ");
            stb.append("   T1.SCORE_COURSECODE, ");
            stb.append("   T1.SCORE_YEAR AS YEAR, ");
            stb.append("   T1.SCORE_SEMESTER AS SEMESTER, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
            stb.append("   TRANK.YEAR || '-' || TRANK.SEMESTER || TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
            stb.append("   TREC.VALUE_DI, ");
            stb.append("   TRANK.SCORE, ");
            stb.append("   TRANK.AVG, ");
            stb.append("   TRANK.CLASS_RANK, ");
            stb.append("   TRANK.CLASS_AVG_RANK, ");
            stb.append("   TRANK.CLASS_DEVIATION, ");
            stb.append("   TRANK.COURSE_RANK, ");
            stb.append("   TRANK.COURSE_AVG_RANK, ");
            stb.append("   TRANK.COURSE_DEVIATION, ");
            stb.append("   TRANK.GRADE_RANK, ");
            stb.append("   TRANK.GRADE_AVG_RANK, ");
            stb.append("   TRANK.GRADE_DEVIATION ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN TSUBGRP T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCORE_YEAR = T1.SCORE_YEAR ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON ");
            stb.append("     TRANK.YEAR = T1.SCORE_YEAR ");
            stb.append("     AND TRANK.SEMESTER <= T1.SCORE_SEMESTER ");
            stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND TRANK.SEMESTER <> '9' ");
            stb.append("     AND TRANK.TESTKINDCD <> '99' ");
            stb.append("     AND TRANK.SCORE_DIV = '01' ");
            stb.append("     AND TRANK.CLASSCD = T2.CLASSCD ");
            stb.append("     AND TRANK.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND TRANK.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND TRANK.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T2.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = TRANK.YEAR ");
            stb.append("     AND TREC.SEMESTER = TRANK.SEMESTER ");
            stb.append("     AND TREC.TESTKINDCD = TRANK.TESTKINDCD ");
            stb.append("     AND TREC.TESTITEMCD = TRANK.TESTITEMCD ");
            stb.append("     AND TREC.SCORE_DIV = TRANK.SCORE_DIV ");
            stb.append("     AND TREC.CLASSCD = TRANK.CLASSCD ");
            stb.append("     AND TREC.SCHOOL_KIND = TRANK.SCHOOL_KIND ");
            stb.append("     AND TREC.CURRICULUM_CD = TRANK.CURRICULUM_CD ");
            stb.append("     AND TREC.SUBCLASSCD = TRANK.SUBCLASSCD ");
            stb.append("     AND TREC.SCHREGNO = TRANK.SCHREGNO ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.HR_NAME, ");
            stb.append("   T1.STAFFNAME, ");
            stb.append("   T1.SUBSTAFFNAME, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   T1.SCORE_YEAR, ");
            stb.append("   T1.SCORE_SEMESTER, ");
            stb.append("   T1.SCORE_GRADE, ");
            stb.append("   T1.SCORE_HR_CLASS, ");
            stb.append("   T1.SCORE_COURSECD, ");
            stb.append("   T1.SCORE_MAJORCD, ");
            stb.append("   T1.SCORE_COURSECODE, ");
            stb.append("   TRANK.YEAR, ");
            stb.append("   TRANK.SEMESTER, ");
            stb.append("   TRANK.CLASSCD || '-' || TRANK.SCHOOL_KIND || '-' || TRANK.CURRICULUM_CD || '-' || TRANK.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   TRANK.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
            stb.append("   TRANK.YEAR || '-' || TRANK.SEMESTER || TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ");
            stb.append("   TRANK.SCORE, ");
            stb.append("   TRANK.AVG, ");
            stb.append("   TRANK.CLASS_RANK, ");
            stb.append("   TRANK.CLASS_AVG_RANK, ");
            stb.append("   TRANK.CLASS_DEVIATION, ");
            stb.append("   TRANK.COURSE_RANK, ");
            stb.append("   TRANK.COURSE_AVG_RANK, ");
            stb.append("   TRANK.COURSE_DEVIATION, ");
            stb.append("   TRANK.GRADE_RANK, ");
            stb.append("   TRANK.GRADE_AVG_RANK, ");
            stb.append("   TRANK.GRADE_DEVIATION ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON ");
            stb.append("     TRANK.YEAR = T1.SCORE_YEAR ");
            stb.append("     AND TRANK.SEMESTER <= T1.SCORE_SEMESTER ");
            stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND TRANK.SEMESTER <> '9' ");
            stb.append("     AND TRANK.TESTKINDCD <> '99' ");
            stb.append("     AND TRANK.SCORE_DIV = '01' ");
            stb.append("     AND (TRANK.SUBCLASSCD = '333333' OR TRANK.SUBCLASSCD = '555555' OR TRANK.SUBCLASSCD = '999999') ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     SCORE_YEAR, ");
            stb.append("     SCORE_SEMESTER, ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     SUBCLASSCD ");

            try {
                final String sql = stb.toString();
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");

                    if (null == schregMap.get(schregno)) {

                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        final String substaffname = rs.getString("SUBSTAFFNAME");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final String coursecd = rs.getString("COURSECD");
                        final String majorcd = rs.getString("MAJORCD");
                        final String coursecode = rs.getString("COURSECODE");

                        final Student student = new Student(grade, hrClass, hrName, staffname, substaffname, attendno, schregno, name, coursecd, majorcd, coursecode);
                        schregMap.put(schregno, student);
                        studentList.add(student);
                    }

                    final Student student = (Student) schregMap.get(schregno);

                    if (null != rs.getString("SCORE_GRADE")) {
                        final String testcd = rs.getString("TESTCD");
                        final Integer iGrade = Integer.valueOf(rs.getString("SCORE_GRADE"));

                        student._regdMap.put(iGrade, new ScoreRegd(rs.getString("SCORE_YEAR"), rs.getString("SCORE_SEMESTER"), rs.getString("SCORE_GRADE"), rs.getString("SCORE_HR_CLASS"), rs.getString("SCORE_COURSECD"), rs.getString("SCORE_MAJORCD"), rs.getString("SCORE_COURSECODE")));

                        final String subclasscdOnly = rs.getString("SUBCLASSCD_ONLY");

                        final boolean isTotal = SUBCLASSCD_333333.equals(subclasscdOnly) || SUBCLASSCD_555555.equals(subclasscdOnly) || SUBCLASSCD_777777.equals(subclasscdOnly) || SUBCLASSCD_888888.equals(subclasscdOnly) || SUBCLASSCD_999999.equals(subclasscdOnly);
                        final String subclasscd = isTotal ? subclasscdOnly : rs.getString("SUBCLASSCD");
                        if (null != subclasscd) {

                            if (!isTotal) {
                                final String subclassname = rs.getString("SUBCLASSNAME");
                                final String subclassabbv = rs.getString("SUBCLASSABBV");

                                final Subclass subclass = new Subclass(subclasscd, subclassname, subclassabbv);
                                getMappedMap(student._subclassMap, iGrade).put(subclasscd, subclass);
                            }

                            final String score = rs.getString("SCORE"); // null != rs.getString("VALUE_DI") ? rs.getString("VALUE_DI") : rs.getString("SCORE");
                            final Score s = new Score(subclasscd, score, rs.getBigDecimal("AVG")
                                    , toInteger(rs.getString("CLASS_RANK")), toInteger(rs.getString("CLASS_AVG_RANK")), rs.getBigDecimal("CLASS_DEVIATION")
                                    , toInteger(rs.getString("COURSE_RANK")), toInteger(rs.getString("COURSE_AVG_RANK")), rs.getBigDecimal("COURSE_DEVIATION")
                                    , toInteger(rs.getString("GRADE_RANK")), toInteger(rs.getString("GRADE_AVG_RANK")), rs.getBigDecimal("GRADE_DEVIATION"));
                            getMappedMap(student._testcdScoreMap, testcd).put(subclasscd, s);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return studentList;
        }

        private static Integer toInteger(final String s) {
            if (NumberUtils.isDigits(s)) {
                return Integer.valueOf(s);
            }
            return null;
        }
    }

    private static class RecordAverageDat {
        final String _subclasscd;
        final String _avgDivKey;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        final BigDecimal _stddev;

        RecordAverageDat(
            final String subclasscd,
            final String avgDivKey,
            final String score,
            final String highscore,
            final String lowscore,
            final String count,
            final BigDecimal avg,
            final BigDecimal stddev
        ) {
            _subclasscd = subclasscd;
            _avgDivKey = avgDivKey;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }

        public static Map getSubclassMap(final Map avgDatMap, final String avgDivKey) {
            return getMappedMap(avgDatMap, avgDivKey);
        }

        public static RecordAverageDat get(final Map avgDatMap, final String avgDivKey, final String subclasscd) {
            return (RecordAverageDat) getSubclassMap(avgDatMap, avgDivKey).get(subclasscd);
        }

        public static String getGradeAvgDivKey(final String grade) {
            return "1" + "-" + grade + "-" + "000" + "-" + "00000000";
        }

        public static String getHrAvgDivKey(final String grade, final String hrClass) {
            return "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
        }

        public static String getCourseAvgDivKey(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            return "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
        }

        public static String getCourseGroupAvgDivKey(final String grade, final String coursegroupCd) {
            return "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
        }

        public static Map getRecordAverageDatMap(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("  YEAR || '-' || SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, ");
            stb.append("  CLASSCD, ");
            stb.append("  SCHOOL_KIND, ");
            stb.append("  CURRICULUM_CD, ");
            stb.append("  SUBCLASSCD, ");
            stb.append("  AVG_DIV || '-' || GRADE || '-' || HR_CLASS || '-' || COURSECD || MAJORCD || COURSECODE AS AVG_DIV_KEY, ");
            stb.append("  SCORE, ");
            stb.append("  HIGHSCORE, ");
            stb.append("  LOWSCORE, ");
            stb.append("  COUNT, ");
            stb.append("  AVG, ");
            stb.append("  STDDEV ");
            stb.append(" FROM  ");
            stb.append("  RECORD_AVERAGE_SDIV_DAT  ");
            stb.append(" WHERE  ");
            stb.append("  (YEAR BETWEEN '" + String.valueOf(Integer.parseInt(param._year) - 3) + "' AND '" + String.valueOf(Integer.parseInt(param._year) - 1) + "' ");
            stb.append("   OR YEAR = '" + param._year + "' AND SEMESTER <= '" + param._semester + "') ");
            try {

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    final String testcd = rs.getString("TESTCD");
                    if ("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_777777.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_888888.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_999999.equals(rs.getString("SUBCLASSCD"))) {
                        subclasscd = rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }
                    final String avgDivKey = rs.getString("AVG_DIV_KEY");
                    final String score = rs.getString("SCORE");
                    final String highscore = rs.getString("HIGHSCORE");
                    final String lowscore = rs.getString("LOWSCORE");
                    final String count = rs.getString("COUNT");
                    final BigDecimal avg = rs.getBigDecimal("AVG");
                    final BigDecimal stddev = rs.getBigDecimal("STDDEV");
                    final RecordAverageDat recordaveragedat = new RecordAverageDat(subclasscd, avgDivKey, score, highscore, lowscore, count, avg, stddev);
                    getMappedMap(getMappedMap(map, testcd), avgDivKey).put(subclasscd, recordaveragedat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        public String toString() {
            return "Average { avg : " + _avg + " ... }";
        }
    }

    private static class Testitem {
        final String _semester;
        final String _testcd;
        final String _semestername;
        final String _testitemname;
        final String _testitemabbv1;
        public Testitem(final String semester, final String testcd, final String semestername, final String testitemname, final String testitemabbv1) {
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _semestername = semestername;
        }

        public static List getTestitemList(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, T2.SEMESTERNAME, T1.TESTITEMNAME, T1.TESTITEMABBV1 ";
            sql += " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + param._year + "' ";
            sql += "   AND T1.SEMESTER <> '99' ";
            sql += "   AND T1.TESTKINDCD <> '99' ";
            sql += "   AND T1.SCORE_DIV = '01' ";
            sql += " ORDER BY T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtn = new ArrayList();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    final Testitem testitem = new Testitem(semester, testcd, semestername, testitemname, testitemabbv1);
                    rtn.add(testitem);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade;
        final String _gradeHrclass;
        final String _loginDate;
        final String _prgid;
        final String[] _schregSelected;

        final String _semestername;
        final List _testitemList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _schregSelected = request.getParameterValues("SCHREG_SELECTED");

            _semestername = getSemestername(db2);
            _testitemList = Testitem.getTestitemList(db2, this);
        }

        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getSemestername(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SEMESTERNAME FROM SEMESTER_MST ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

