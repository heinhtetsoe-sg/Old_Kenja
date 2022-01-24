// kanji=漢字
/*
 * $Id: 6116ba123287e7c3cadcfe9d68a448ecc9c9313a $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.ui.RectangleEdge;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 個人成績票。
 * @version $Id: 6116ba123287e7c3cadcfe9d68a448ecc9c9313a $
 */
public class KNJD105F {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD105F.class);

    /** 成績表の最大科目数. */
    private static final int TABLE_SUBCLASS_MAX = 20;
    /** 棒グラフの最大科目数. */
    private static final int BAR_GRAPH_MAX_ITEM = 11;
    /** 生徒氏名のフォント切替の境目文字数. */
    private static final int STUDENT_NAME_DELIMTER_COUNT = 10;

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 共通科目合計科目コード。 */
    private static final String ALL8 = "888888";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final String FORM_FILE   = "KNJD105F.frm";

    private static final String AVG_DIV_GRADE = "1";
    private static final String AVG_DIV_COURSE = "3";
    private static final String AVG_DIV_CHAIRGROUP = "6";

    private Param _param;
    private boolean _hasData;
    private Form _form;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);
        _form = new Form(response);
        final String dbName = request.getParameter("DBNAME");
        final DB2UDB db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        if (openDb(db2)) {
            return;
        }

        log.error("★マスタ関連の読込み");
        _param.load(db2);

        // 対象の生徒たちを得る
        final List students = createStudents(db2, _param);
        _hasData = students.size() > 0;

        // 成績のデータを読む
        log.error("★成績関連の読込み");

        loadExam(db2, _param, students, _param._examList);

        // 印刷する
        // log.error("★印刷");
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            // log.debug("☆" + student + ", 科目の数=" + student._subclasses.size() + ", コースキー=" + student.courseKey());
            // log.debug("今回の成績: " + student._record.values());

            _form.resetForm();
            _form.printStudent(_param, student);
        }

        // log.error("★終了処理");
        _form.closeSvf(_hasData);
        closeDb(db2);
        _param.removeImageFiles();
        log.info("Done.");
    }

    private void loadExam(final DB2UDB db2,final Param param,  final List students, final List examList) throws SQLException {
        loadSubClasses(db2, param, students);
        for (final Iterator it = examList.iterator(); it.hasNext();) {
            final Exam exam = (Exam) it.next();
            loadAverageDat(db2, param, exam);
            loadRecord(db2, param, exam, students);
            loadRecordOther(db2, param, exam, students);
        }
    }

    private void loadSubClasses(final DB2UDB db2, final Param param, final List students) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            StringBuffer stb = new StringBuffer();
            stb.append("select");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("  distinct T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD");
            } else {
                stb.append("  distinct T1.SUBCLASSCD");
            }
            stb.append(" from");
            stb.append("  RECORD_SCORE_DAT T1 ");
            
            stb.append(" where");
            stb.append("  T1.YEAR = '" + _param._year + "' and");
            stb.append("  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '" + _param._thisExam._semester + _param._thisExam._testcd + "' and");
            stb.append("  T1.SCHREGNO = ? ");

            log.debug("成績入力データの科目のSQL=" + stb);
            
            ps = db2.prepareStatement(stb.toString());
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) _param._subClasses.get(subClasscd);
                    if (null != subClass) {
                        student._subclasses.put(subClasscd, subClass);
                    }
                }
            }
        } catch (final SQLException e) {
            log.fatal("成績入力データの科目取得でエラー");
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    /**
     * 成績平均データを読込み、セットする。
     * @param db2 DB
     * @param kindCd テスト種別コード
     * @param itemCd テスト項目コード
     * @param outAvgDat 成績平均データの格納場所
     * @param outAvgDatOther 成績平均データ(3,5教科)の格納場所
     * @throws SQLException SQL例外
     */
    private void loadAverageDat(
            final DB2UDB db2,
            final Param param,
            final Exam exam
    ) throws SQLException {
        
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT");
        stb.append("  t1.avg_div,");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("  case when t1.subclasscd in ('" + ALL3 + "', '" + ALL5 + "', '" + ALL8 + "', '" + ALL9 + "') then t1.subclasscd  ");
            stb.append("   else t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd ");
            stb.append("  end as subclasscd,");
        } else {
            stb.append("  t1.subclasscd,");
        }
        stb.append("  t1.avg,");
        stb.append("  t1.stddev,");
        stb.append("  t1.highscore,");
        stb.append("  t1.lowscore,");
        stb.append("  t1.count,");
        stb.append("  t1.coursecd,");
        stb.append("  t1.majorcd,");
        stb.append("  t1.coursecode,");
        stb.append("  value(t2.perfect, 100) as perfect");
        stb.append(" FROM");
        stb.append("  record_average_dat t1");
        stb.append("  LEFT JOIN perfect_record_dat t2 ON t2.year = t1.year AND ");
        stb.append("    t2.semester= t1.semester AND");
        stb.append("    t2.testkindcd= t1.testkindcd AND");
        stb.append("    t2.testitemcd= t1.testitemcd AND");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    t2.classcd= t1.classcd AND");
            stb.append("    t2.school_kind= t1.school_kind AND");
            stb.append("    t2.curriculum_cd= t1.curriculum_cd AND");
        }
        stb.append("    t2.subclasscd= t1.subclasscd AND");
        stb.append("    t2.grade = (case when t2.div = '01' then '00' else '" + param._grade + "' end) AND");
        stb.append("    t2.coursecd || t2.majorcd || t2.coursecode = ");
        stb.append("        (case when t2.div in ('01', '02') then '00000000' ");
        stb.append("              when t2.div = '04' then '0' || t1.majorcd || '0000' ");
        stb.append("              else t1.coursecd || t1.majorcd || t1.coursecode end) ");
        stb.append(" WHERE");
        stb.append("    t1.year='" + param._year + "' AND");
        stb.append("    t1.semester='" + exam._semester + "' AND");
        stb.append("    t1.testkindcd='" + exam.getKindCd() + "' AND");
        stb.append("    t1.testitemcd='" + exam.getItemCd() + "' AND");
        stb.append("    t1.avg_div in ('" + AVG_DIV_COURSE + "', '" + AVG_DIV_CHAIRGROUP + "') AND");
        stb.append("    t1.grade = '" + param._grade + "' AND");
        stb.append("    t1.hr_class='000'");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                final SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
                }
                final String avgDiv = rs.getString("avg_div");
                final BigDecimal avg = rs.getBigDecimal("avg");
                BigDecimal avgPercent = null;
                final int perfect = rs.getInt("PERFECT");
                if (100 == perfect) {
                    avgPercent = avg;
                } else if (null != avg){
                    avgPercent = avg.multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP);
                }
                final BigDecimal stdDev = rs.getBigDecimal("STDDEV");
                final Integer count = KNJServletUtils.getInteger(rs, "count");
                final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                final Integer lowScore = KNJServletUtils.getInteger(rs, "lowscore");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                String key = null;
                if (AVG_DIV_CHAIRGROUP.equals(avgDiv)) {
                    key = avgDiv + subclasscd + "0" + majorcd + "0000";
                } else if (AVG_DIV_COURSE.equals(avgDiv)) {
                    key = avgDiv + subclasscd + coursecd + majorcd + coursecode;
                }
                if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL8.equals(subclasscd) || ALL9.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    final AverageDat avgDat = new AverageDat(subClass0, avg, avgPercent, stdDev, highScore, lowScore, count, coursecd, majorcd, coursecode);
                    exam._averageDatOther.put(key, avgDat);
                } else {
                    final AverageDat avgDat = new AverageDat(subClass, avg, avgPercent, stdDev, highScore, lowScore, count, coursecd, majorcd, coursecode);
                    exam._averageDat.put(key, avgDat);
                }
            }
        } catch (final SQLException e) {
            log.warn("成績平均データの取得でエラー");
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.debug("テストコード=" + exam._semester + exam.getKindCd() + exam.getItemCd() + " の成績平均データの件数=" + exam._averageDat.size());
    }

    /*
     * 生徒に成績データを関連付ける。
     */
    private void loadRecord(
            final DB2UDB db2,
            final Param param,
            final Exam exam,
            final List students
    ) {
        
        final String gradeRankFld  = "2".equals(_param._outputKijun) ? "grade_avg_rank" : "3".equals(_param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
        final String courseRankFld = "2".equals(_param._outputKijun) ? "course_avg_rank" : "3".equals(_param._outputKijun) ? "course_deviation_rank" : "course_rank";
        final String majorRankFld = "2".equals(_param._outputKijun) ? "major_avg_rank" : "3".equals(_param._outputKijun) ? "major_deviation_rank" : "major_rank";

        final StringBuffer stb = new StringBuffer();
        stb.append("WITH CHAIR_GROUP AS (");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.CHAIR_GROUP_CD,  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T2.CHAIRCD ");
        stb.append(" FROM CHAIR_GROUP_MST T1 ");
        stb.append(" INNER JOIN CHAIR_GROUP_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.CHAIR_GROUP_CD = T1.CHAIR_GROUP_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER <= '" + _param._thisExam._semester + "' ");
        stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD = '0000' "); // 固定
        stb.append(" ) ");
        
        stb.append(" , CHAIR_GROUP_SUBCLASS AS (");
        stb.append("select distinct ");
        stb.append("  T1.SCHREGNO, T1.SEMESTER, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("  T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("  MIN(T1.CHAIRCD) AS CHAIRCD, ");
        stb.append("  MIN(T3.CHAIR_GROUP_CD) AS CHAIR_GROUP_CD ");
        stb.append(" from");
        stb.append("  CHAIR_STD_DAT T1 ");
        stb.append(" left join CHAIR_DAT T2 on T2.YEAR = T1.YEAR ");
        stb.append("     and T2.SEMESTER = T1.SEMESTER ");
        stb.append("     and T2.CHAIRCD = T1.CHAIRCD ");
        stb.append(" left join CHAIR_GROUP T3 on T3.YEAR = T1.YEAR ");
        stb.append("     and T3.SEMESTER = T1.SEMESTER ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     and T3.CLASSCD = T2.CLASSCD ");
            stb.append("     and T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     and T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("     and T3.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("     and T3.CHAIRCD = T1.CHAIRCD ");
        stb.append(" where");
        stb.append("  T1.YEAR = '" + _param._year + "' and");
        stb.append("  T1.SEMESTER <= '" + _param._thisExam._semester + "' and");
        stb.append("  T1.SCHREGNO = ? ");
        stb.append(" GROUP BY ");
        stb.append("  T1.SCHREGNO, T1.SEMESTER, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("  T2.SUBCLASSCD ");
        stb.append(")");
        /* 通常の成績 */
        stb.append("SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
        }
        stb.append("  t1.subclasscd as subclasscd, ");
        stb.append("  t2.score,");
        stb.append("  t2." + gradeRankFld + " as grade_rank,");
        stb.append("  t2.grade_deviation,");
        stb.append("  t2." + courseRankFld + " as course_rank,");
        stb.append("  t2.course_deviation,");
        stb.append("  t2." + majorRankFld + " as major_rank,");
        stb.append("  t2.major_deviation, ");
        stb.append("  t3.CHAIRCD, ");
        stb.append("  t3.CHAIR_GROUP_CD ");
        stb.append(" FROM");
        stb.append("  record_score_dat t1 ");
        stb.append(" LEFT JOIN record_rank_dat t2 ON");
        stb.append("    t1.year=t2.year AND");
        stb.append("    t1.semester=t2.semester AND");
        stb.append("    t1.testkindcd=t2.testkindcd AND");
        stb.append("    t1.testitemcd=t2.testitemcd AND");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    t1.classcd=t2.classcd AND");
            stb.append("    t1.school_kind=t2.school_kind AND");
            stb.append("    t1.curriculum_cd=t2.curriculum_cd AND");
        }
        stb.append("    t1.subclasscd=t2.subclasscd AND");
        stb.append("    t1.schregno=t2.schregno ");
        stb.append(" LEFT JOIN CHAIR_GROUP_SUBCLASS T3 ON T3.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("    T3.SEMESTER = T1.SEMESTER AND ");
        stb.append("    T3.SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
        }
        stb.append("  t1.subclasscd ");
        stb.append(" WHERE");
        stb.append("  t1.year='" + _param._year + "' AND");
        stb.append("  t1.semester='" + exam._semester + "' AND");
        stb.append("  t1.testkindcd='" + exam.getKindCd() + "' AND");
        stb.append("  t1.testitemcd='" + exam.getItemCd() + "' AND");
        stb.append("  t1.score_div='01' AND");
        stb.append("  t1.schregno=?");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(" record sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                ps.setString(2, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = (SubClass) _param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                        final Class clazz = (Class) _param._classes.get(subclasscd.substring(0, 2));
                        if (null == clazz) {
                            continue;
                        }
                        subClass = new SubClass(subclasscd, clazz._name, clazz._abbv, null);
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    final BigDecimal avg = null;
                    Integer scorePercent = score;
//                    Integer scorePercent = null;
//                    final int perfect = rs.getInt("PERFECT");
//                    if (100 == perfect) {
//                        scorePercent = score;
//                    } else if (null != score) {
//                        scorePercent = new Integer(new BigDecimal(rs.getString("SCORE")).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 0, BigDecimal.ROUND_HALF_UP).intValue());
//                    }
                    final Integer gradeRank = KNJServletUtils.getInteger(rs, "grade_rank");
                    final Integer majorRank = KNJServletUtils.getInteger(rs, "major_rank");
                    final Integer courseRank = KNJServletUtils.getInteger(rs, "course_rank");
                    final BigDecimal deviation;
                    if ("1".equals(_param._groupDiv)) {
                        deviation = rs.getBigDecimal("grade_deviation");
                    } else if ("3".equals(_param._groupDiv)) {
                        deviation = rs.getBigDecimal("major_deviation");
                    } else {
                        deviation = rs.getBigDecimal("course_deviation");
                    }
                    final String chaircd = rs.getString("CHAIRCD");
                    final String chairGroupcd = rs.getString("CHAIR_GROUP_CD");
                    log.debug(" student " + student._schregno + ", subclass = " + subClass._code + ", " + chaircd + ", " + chairGroupcd);
                    final Record rec = new Record(subClass, score, scorePercent, avg, gradeRank, majorRank, courseRank, deviation, chaircd, chairGroupcd);
                    student.setRecord(exam, subClass, rec);
                }
            }
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private void loadRecordOther(
            final DB2UDB db2,
            final Param param,
            final Exam exam,
            final List students
    ) {
        final String kindCd = exam.getKindCd();
        final String itemCd = exam.getItemCd();
        
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String gradeRankFld  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
            final String courseRankFld = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
            final String majorRankFld = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  subclasscd,");
            stb.append("  score,");
            stb.append("  avg,");
            stb.append("  " + gradeRankFld + " as grade_rank,");
            stb.append("  grade_deviation,");
            stb.append("  " + courseRankFld + " as course_rank,");
            stb.append("  course_deviation,");
            stb.append("  " + majorRankFld + " as major_rank,");
            stb.append("  major_deviation");
            stb.append(" FROM record_rank_dat");
            stb.append(" WHERE");
            stb.append("  year='" + param._year + "' AND");
            stb.append("  semester='" + exam._semester + "' AND");
            stb.append("  testkindcd='" + kindCd + "' AND");
            stb.append("  testitemcd='" + itemCd + "' AND");
            stb.append("  subclasscd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL8 + "', '" + ALL9 + "') AND");
            stb.append("  schregno='" + student._schregno + "'");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug(" rank sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    final BigDecimal avg = rs.getBigDecimal("avg");
                    final Integer gradeRank = KNJServletUtils.getInteger(rs, "grade_rank");
                    final Integer majorRank = KNJServletUtils.getInteger(rs, "major_rank");
                    final Integer courseRank = KNJServletUtils.getInteger(rs, "course_rank");
                    final BigDecimal deviation;
                    if ("1".equals(param._groupDiv)) {
                        deviation = rs.getBigDecimal("grade_deviation");
                    } else if ("3".equals(param._groupDiv)) {
                        deviation = rs.getBigDecimal("major_deviation");
                    } else {
                        deviation = rs.getBigDecimal("course_deviation");
                    }

                    final SubClass subClass = new SubClass(subclasscd);
                    final Record rec = new Record(subClass, score, null, avg, gradeRank, majorRank, courseRank, deviation, null, null);
                    student.setRecordOther(exam, subClass._code, rec);
                }
                log.debug(" student other = " + student._recordOther);

            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private List createStudents(final DB2UDB db2, final Param param) throws SQLException {
        final List rtn = new LinkedList();
        final String[] schregnos = param.getScregnos(db2);
        final String sql = studentsSQL(param, schregnos);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String hrclass = rs.getString("hr_class");
                final String attendno = rs.getString("attendno");
                final String hrName = rs.getString("hr_name");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String courseCodeName = rs.getString("coursecodename");
                final String name = rs.getString("name");
                final String remark = rs.getString("remark1");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        attendno,
                        hrName,
                        coursecd,
                        majorcd,
                        coursecode,
                        courseCodeName,
                        name,
                        remark
                );
                rtn.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー");
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        Collections.sort(rtn);
        return rtn;
    }

    private String studentsSQL(final Param param, final String[] selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT");
        stb.append("  t1.schregno,");
        stb.append("  t1.grade,");
        stb.append("  t1.hr_class,");
        stb.append("  t1.attendno,");
        stb.append("  t1.hr_name,");
        stb.append("  t1.coursecd,");
        stb.append("  t1.majorcd,");
        stb.append("  t1.coursecode,");
        stb.append("  t1.name,");
        stb.append("  t3.remark1,");
        stb.append("  t5.coursecodename");
        stb.append(" FROM");
        stb.append("  v_schreg_info t1 ");
        stb.append("    LEFT JOIN hexam_record_remark_dat t3 ON");
        stb.append("      t1.year=t3.year AND");
        stb.append("      t1.semester=t3.semester AND");
        stb.append("      t3.testkindcd='" + param._thisExam.getKindCd() + "' AND");
        stb.append("      t3.testitemcd='" + param._thisExam.getItemCd() + "' AND");
        stb.append("      t1.schregno=t3.schregno AND");
        stb.append("      t3.remark_div='2'"); // '2'固定
        stb.append("    LEFT JOIN coursecode_mst t5 ON t1.coursecode=t5.coursecode");
        stb.append(" WHERE");
        stb.append("  t1.year='" + param._year + "' AND");
        stb.append("  t1.semester='" + param._semester + "' AND");
        stb.append("  t1.schregno IN " + SQLUtils.whereIn(true, selected));            ;
        return stb.toString();
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
        final Param param = new Param(request);
        return param;
    }
    
    private static class Exam implements Comparable {

        final String _semester;
        final String _testcd;
        final String _useCurriculumcd;
        final String _testitemname;
        final String _semestername;
        final String _printFlg;
        
        /** 成績平均データ。 */
        private Map _averageDat = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map _averageDatOther = new HashMap();
//        /** 合併元科目コード */
//        private Map _attendSubclassCdMap = Collections.EMPTY_MAP;
        
        public Exam(final String semester, final String testCd, final String useCurriculumcd, final String testitemname, final String semestername, final String printFlg) {
            _semester = semester;
            _testcd = testCd;
            _useCurriculumcd = useCurriculumcd;
            _semestername = semestername;
            _testitemname = testitemname;
            _printFlg = printFlg;
        }

        public String getKindCd() {
            return null == _testcd ? null : _testcd.substring(0, 2);
        }

        public String getItemCd() {
            return null == _testcd ? null : _testcd.substring(2);
        }

        public int compareTo(Object o) {
            if (!(o instanceof Exam)) {
                return -1;
            }
            final Exam oe = (Exam) o;
            return (_semester + _testcd).compareTo(oe._semester + oe._testcd);
        }
        
//        private void load(final Param param, final DB2UDB db2) {
//            loadExam(param, db2);
//            loadAttendSubclassCdMap(db2);
//        }
        
//        private void loadAttendSubclassCdMap(final DB2UDB db2) {
//            final Map map = new HashMap();
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            final String flg = "9900".equals(_testCd) ? "2" : "1";
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append("   SELECT ");
//                stb.append("       T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ");
//                if ("1".equals(_useCurriculumcd)) {
//                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
//                }
//                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
//                stb.append("   FROM ");
//                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
//                stb.append("   WHERE ");
//                stb.append("       T1.YEAR = '" + _year + "' ");
//                stb.append("       AND T1.FLG = '" + flg + "' ");
//                
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String key = rs.getString("KEY");
//                    if (null == map.get(key)) {
//                        map.put(key, new ArrayList());
//                    }
//                    final List list = (List) map.get(key);
//                    list.add(rs.getString("ATTEND_SUBCLASSCD"));
//                }
//            } catch (Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            _attendSubclassCdMap = map;
//        }

//        public List getAttendSubclassCdList(final Student student) {
//            final List list = (List) _attendSubclassCdMap.get(student.attendSubclassCdKey());
//            if (null == list) {
//                return Collections.EMPTY_LIST;
//            }
//            return list;
//        }
        
//        private void loadExam(final Param param, final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            _examName = "";
//            try {
//                final String sql = "SELECT testitemname FROM testitem_mst_countflg_new WHERE year='" + _year + "' AND semester='" + _semester + "' AND testkindcd='" + getKindCd() + "' AND testitemcd='" + getItemCd() + "'"
//                    ;
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _examName = rs.getString("testitemname");
//                }
//            } catch (final SQLException e) {
//                log.error("考査名取得エラー。");
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            _semestername = "";
//            try {
//                final String sql = "SELECT semestername FROM semester_mst WHERE year='" + _year + "' AND semester='" + _semester + "'"
//                    ;
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _semestername = null == rs.getString("semestername") ? "" : rs.getString("semestername");
//                }
//            } catch (final SQLException e) {
//                log.error("学期名取得エラー。");
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            _title = nendo() + "　" + _semestername + "　" + _examName;
//        }
//        
//        String nendo() {
//            return KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
//        }
        public String toString() {
            return "Exam(" + _semester + _testcd + ")";
        }
    }

    private class Param {
        private final String _year;
        private final String _semester;
        private final String _testCd;
        private final String _grade;

        /** 2:コース 3:講座グループ */
        private String _groupDiv;

        /** [クラス指定 or 生徒指定]の値。 */
        private final String _div;
        /** クラス or 生徒。 */
        private final String[] _values;

//        private final String _submitDate;
        private final String _loginDate;

        /** クラス指定 or 生徒指定。 */
        private final boolean _isClassMode;

        /** 担任職種名。 */
        private String _remark2;
//        /** 偏差値を印字するか? */
//        private final String _deviationPrint;
        /** 基準点 */
        private final String _outputKijun;

        private final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        private Map _classes;
        /** 科目マスタ。 */
        private Map _subClasses;
        private final String _imagePath;

        private final String _useCurriculumcd;
        private final String _useClassDetailDat;

//        private final List _classOrder = new ArrayList();

//        /** 中学か? false なら高校. */
//        private boolean _isJunior;
//        private boolean _isHigh;

//        /** 欠点 */
//        private final int _borderScore;
        
        private List _examList;
        private Exam _thisExam;
        
        /** グラフイメージファイルの Set&lt;File&gt; */
        private final Set _graphFiles = new HashSet();
        
        public Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _div = request.getParameter("CATEGORY_IS_CLASS");
            _values = request.getParameterValues("CATEGORY_SELECTED");
//            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");

            _isClassMode = "1".equals(request.getParameter("CATEGORY_IS_CLASS")) ? true : false;

            final String groupDiv = request.getParameter("GROUP_DIV");// 1=学年, 2=コース
            _groupDiv =  groupDiv;
            // _deviationPrint = request.getParameter("DEVIATION_PRINT");

            _imagePath = request.getParameter("IMAGE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            log.debug(" record_rank_dat outputKijun ? = " + _outputKijun);
            
//            _borderScore = "".equals(request.getParameter("KETTEN")) || request.getParameter("KETTEN") == null ? 0 : Integer.parseInt(request.getParameter("KETTEN"));;
        }
        
        private void removeImageFiles() {
            for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
                final File imageFile = (File) it.next();
                if (null == imageFile) {
                    continue;
                }
                log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
            }
        }
        
        private void setExamList(final DB2UDB db2) {
            _examList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T2.SEMESTERNAME, ");
                stb.append("     T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '" + _semester + _testCd + "' THEN 1 ELSE 0 END AS PRINT_FLG, ");
                stb.append("     CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '" + _semester + _testCd + "' THEN 1 ELSE 0 END AS THIS_EXAM_FLG ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW T1 ");
                stb.append(" INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.TESTKINDCD IN ('01', '02') ");
                stb.append(" ORDER BY ");
                stb.append("     T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testcd = rs.getString("TESTCD");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String printFlg = rs.getString("PRINT_FLG");
                    final Exam exam = new Exam(semester, testcd, _useCurriculumcd, testitemname, semestername, printFlg);
                    _examList.add(exam);
                    if ("1".equals(rs.getString("THIS_EXAM_FLG"))) {
                        _thisExam = exam;
                    }
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void imageFileCheck(final String fName) {
            final File f = new File(_imagePath + "/" + fName);
            if (!f.exists()) {
                log.fatal("画像ファイルが無い!⇒" + _imagePath + "/" + fName);
            }
        }

        public String[] getScregnos(final DB2UDB db2) throws SQLException {
            final String separator = "-";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List result = new ArrayList();

            if (!_isClassMode) {
                return _values;
            }

            // 年組から学籍番号たちを得る
            for (int i = 0; i < _values.length; i++) {
                final String grade = StringUtils.split(_values[i], separator)[0];
                final String room = StringUtils.split(_values[i], separator)[1];
                
                try {
                    final String sql = " select"
                    + "    SCHREGNO as schregno"
                    + " from"
                    + "    SCHREG_REGD_DAT"
                    + " where"
                    + "    YEAR = '" + _year + "' and"
                    + "    SEMESTER = '" + _semester + "' and"
                    + "    GRADE = '" + grade + "' and"
                    + "    HR_CLASS = '" + room + "'"
                    ;
                    
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("schregno");
                        result.add(schregno);
                    }
                } catch (final SQLException e) {
                    log.error("年組から学籍番号たちを得る際にエラー");
                    throw e;
                }
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final Iterator it = result.iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                rtn[i++] = schregno;
            }

            return rtn;
        }

        public void load(final DB2UDB db2) throws SQLException {
//            loadIsJunior(db2);
//            loadCertifSchool(db2);
            loadRegdHdat(db2);
//            _thisExam.load(this, db2);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);
            setExamList(db2);
//            setCombinedOnSubClass(db2);

//            loadClassOrder(db2);
        }

//        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
//            final Set sakiSet = new HashSet();
//            final Set motoSet = new HashSet();
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sqlCombined());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String combined = rs.getString("COMBINED_SUBCLASSCD");
//                    final String attend = rs.getString("ATTEND_SUBCLASSCD");
//                    sakiSet.add(combined);
//                    motoSet.add(attend);
//                }
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//
//            // 合併先
//            for (final Iterator it = sakiSet.iterator(); it.hasNext();) {
//                final String saki = (String) it.next();
//                final SubClass subClass = (SubClass) _subClasses.get(saki);
//                if (null != subClass) {
//                    subClass.setSaki();
//                }
//            }
//            // 合併元
//            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
//                final String moto = (String) it.next();
//                final SubClass subClass = (SubClass) _subClasses.get(moto);
//                if (null != subClass) {
//                    subClass.setMoto();
//                }
//            }
//        }

//        public String sqlCombined() {
//            String rtn;
//            rtn = "select distinct";
//            if ("1".equals(_useCurriculumcd)) {
//                rtn +="  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ";
//            }
//            rtn +="  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,";
//            if ("1".equals(_useCurriculumcd)) {
//                rtn +="  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ";
//            }
//            rtn +="  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD"
//                + " from SUBCLASS_REPLACE_COMBINED_DAT"
//                + " where"
//                + "  YEAR = '" + _year + "'"
//                ;
//            return rtn;
//        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append("select ");
                if  ("1".equals(_useCurriculumcd)) {
                    sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD,");
                } else {
                    sql.append("   T1.SUBCLASSCD,");
                }
                sql.append("   coalesce(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME ) as NAME,");
                sql.append("   T1.SUBCLASSABBV, ");
                sql.append("   T1.ELECTDIV, ");
                sql.append("   T3.COURSECD || T3.MAJORCD || T3.COURSECODE AS COURSE, ");
                sql.append("   T3.REQUIRE_FLG ");
                sql.append(" from V_SUBCLASS_MST T1");
                sql.append(" left join CREDIT_MST T3 ON T3.YEAR = '" + _year + "' ");
                sql.append("     and T3.GRADE = '" + _grade + "' ");
                sql.append("     and ");
                if  ("1".equals(_useCurriculumcd)) {
                    sql.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
                } else {
                    sql.append("   T3.SUBCLASSCD ");
                }
                sql.append("      = ");
                if  ("1".equals(_useCurriculumcd)) {
                    sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                } else {
                    sql.append("   T1.SUBCLASSCD ");
                }
                sql.append(" where");
                sql.append("   T1.YEAR = '" + _year + "'");
                sql.append(" order by");
                if  ("1".equals(_useCurriculumcd)) {
                    sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD");
                } else {
                    sql.append("   T1.SUBCLASSCD");
                }

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    if (!rtn.containsKey(code)) {
                        final String name = rs.getString("NAME");
                        final String abbv = rs.getString("SUBCLASSABBV");
                        final String electdiv = rs.getString("ELECTDIV");
                        rtn.put(code, new SubClass(code, name, abbv, electdiv));
                    }
                    final SubClass subClass = (SubClass) rtn.get(code);
                    subClass._creditMstRequireflg.put(rs.getString("COURSE"), rs.getString("REQUIRE_FLG"));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += "select";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
                } else {
                    sql +="   CLASSCD,";
                }
                sql +="   CLASSNAME,"
                    + "   CLASSABBV"
                    + " from V_CLASS_MST"
                    + " where"
                    + "   YEAR = '" + _year + "'"
                    + " order by";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="   CLASSCD || '-' || SCHOOL_KIND ";
                } else {
                    sql +="   CLASSCD";
                }
                ;
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CLASSCD");
                    final String name = rs.getString("CLASSNAME");
                    final String abbv = rs.getString("CLASSABBV");
                    rtn.put(code, new Class(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        private void loadRegdHdat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year=t2.year AND t1.tr_cd1=t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("staffname");
                    _staffs.put(code, name);
                }
            } catch (final SQLException e) {
                log.warn("担任名の取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

//        private void loadCertifSchool(final DB2UDB db2) throws SQLException {
//            final String key = _isJunior ? "110" : "109";
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = "SELECT school_name, remark2 FROM certif_school_dat"
//                    + " WHERE year='" + _year + "' AND certif_kindcd='" + key + "'";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _schoolName = rs.getString("school_name");
//                    _remark2 = rs.getString("remark2");
//                }
//            } catch (final SQLException e) {
//                log.error("学校名取得エラー。");
//                throw e;
//            }
//        }

//        private void loadClassOrder(final DB2UDB db2) {
//            final String sql;
//            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
//                sql = "SELECT classcd || '-' || school_kind AS classcd FROM class_detail_dat"
//                        + " WHERE year='" + _param._year + "' AND class_seq='004' "
//                        + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
//                        ;
//            } else {
//                final String field1 = _isJunior ? "name1" : "name2";
//                final String field2 = _isJunior ? "namespare1" : "namespare2";
//                sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
//                    + " WHERE year='" + _param._year + "' AND namecd1='D009' AND " + field1 + " IS NOT NULL "
//                    + " ORDER BY " + field2
//                    ;
//            }
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _classOrder.add(rs.getString("classcd"));
//                }
//            } catch (final SQLException e) {
//                log.error("教科表示順取得エラー。");
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            log.debug("教科表示順=" + _classOrder);
//        }

//        private void loadIsJunior(DB2UDB db2) {
//            _isJunior = Integer.parseInt(_grade) < 4;
//            log.debug("中学校?:" + _isJunior);
//            _isHigh = !_isJunior;
//        }
        
        public boolean isUnderScore(final String score) {
            return false;
//            if (StringUtils.isEmpty(score)) {
//                return false;
//            }
//            final int val = Integer.parseInt(score);
//            return val < _borderScore;
        }
    }

    private static class Form {
        private Vrw32alp _svf;

        public Form(final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
        }
        
        public void printStudent(final Param param, final Student student) {
            printStatic(param, student);
            // グラフ印字(サブフォームよりも先に印字)
            printBarGraph(param, student);
            // 成績
            printScore(param, student);
            
            _svf.VrEndPage();
        }

        public void resetForm() {
            final int sts;
            sts = _svf.VrSetForm(FORM_FILE, 1);
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }
        }
        
        private static int getMS932ByteLength(final String s) {
            int length = 0;
            try {
                length = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return length;
        } 
        
        public void printStatic(final Param param, final Student student) {
            _svf.VrsOut("SEMESTER", param._thisExam._semestername);
            _svf.VrsOut("TESTNAME", param._thisExam._testitemname);
            final String staffName = (String) param._staffs.get(student._grade + student._hrClass);
            _svf.VrsOut("STAFFNAME", param._remark2 + staffName);
            _svf.VrsOut("NENDO", param._year + "年度　");
            _svf.VrsOut("NENDO2", param._year + "年度　" + param._thisExam._semestername + param._thisExam._testitemname);
            final int attendNo = Integer.parseInt(student._attendNo);
            _svf.VrsOut("HR_NAME", student._hrName + "　" + attendNo + "番");
            _svf.VrsOut("NAME", student._name); // 生徒氏名
            
            _svf.VrsOut("COURSE", StringUtils.defaultString(student._courseCodeName)); // コース名
            _svf.VrsOut("BAR_TITLE", "今回の各科目結果"); // タイトル
            _svf.VrsOut("TITLE", param._year + "年度　年間定期考査通知票") ; // タイトル

            
//            // 提出日
//            try {
//                final Calendar cal = KNJServletUtils.parseDate(param._submitDate.replace('/', '-'));
//                final String month = (cal.get(Calendar.MONTH) + 1) + "月";
//                final String day = cal.get(Calendar.DATE) + "日";
//                _svf.VrsOut("DATE", month + day);
//            } catch (final ParseException e) {
//                log.error("提出日が解析できない");
//                _svf.VrsOut("DATE", param._submitDate);
//            }

//            // 個人評(期末の場合は注意事項)
//            KNJServletUtils.printDetail(_svf, "PERSONAL_REMARK", student._remark, 45 * 2, 3);

            final String msg;
            if ("1".equals(param._groupDiv)) {
                msg = "学年";
            } else if ("3".equals(param._groupDiv)) {
                msg = "グループ";
            } else {
                msg = "コース";
            }
            final String rankName = msg + "順位";
            _svf.VrsOut("RANK_NAME" + (getMS932ByteLength(rankName) > 10 ? "2" : "1"), rankName); // 順位名称
            
            _svf.VrsOut("SUBTITLE", "（" + StringUtils.defaultString(param._thisExam._semestername) + "　" + StringUtils.defaultString(param._thisExam._testitemname) + "）"); // サブタイトル
            final File logoFile = new File(param._imagePath + "/" + "SCHOOLLOGO" + ".jpg");
            if (logoFile.exists()) {
                _svf.VrsOut("SCHOOL_LOGO", logoFile.getAbsolutePath());
            }

        }

        public void printBarGraph(final Param param, final Student student) {
            // グラフ用のデータ作成
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            final List minMaxLows = new ArrayList();
            final List minMaxHighes = new ArrayList();
            final List minMaxCategoryKeys = new ArrayList();
            int i = 0;
            final Map examRecordMap = null == student._record.get(param._thisExam) ? Collections.EMPTY_MAP : (Map) student._record.get(param._thisExam);
            for (final Iterator it = examRecordMap.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
//                if (record._subClass._isMoto) {
//                    continue;
//                }
                
                if ("90".equals(record._subClass.getClassCd())) {
                    continue;
                }

//                final List attendSubclassCdList = _param._thisExam.getAttendSubclassCdList(student);
//                if (attendSubclassCdList.contains(record._subClass._code)) {
//                    continue;
//                }

                final String subclassKey = StringUtils.defaultString(record._subClass._abbv).length() > 4 ? record._subClass._abbv.substring(0, 4) : record._subClass._abbv;
                scoreDataset.addValue(record._scorePercent, "本人の素点", subclassKey);
                final AverageDat avgDat = (AverageDat) param._thisExam._averageDat.get(getAverageDatKey(param, student, record));
                final BigDecimal avgPercent;
                Integer highScore = null;
                Integer lowScore = null;
                if (null == avgDat) {
                    avgPercent = null;
                } else {
                    avgPercent = avgDat._avgPercent;
                    highScore = avgDat._highScore;
                    lowScore = avgDat._lowScore;
                }
                minMaxHighes.add(highScore);
                minMaxLows.add(lowScore);
                minMaxCategoryKeys.add(record._subClass._abbv);

                final String msg = "1".equals(param._groupDiv) ? "学年" : "3".equals(param._groupDiv) ? "グループ" : "コース";
                avgDataset.addValue(avgPercent, msg + "平均点", subclassKey);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + ", 平均=" + avgPercent + ", 最低点=" + lowScore + ", 最高点=" + highScore);
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }

            final DefaultIntervalCategoryDataset minMaxDataset = createIntervalDataset(minMaxLows, minMaxHighes, minMaxCategoryKeys);

            try {
                // チャート作成
                final JFreeChart chart = createBarChart(scoreDataset, avgDataset, minMaxDataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 1940, 930);
                param._graphFiles.add(outputFile);

                // グラフの出力
                _svf.VrsOut("BAR_LABEL", "得点");
                
                if (outputFile.exists()) {
                    _svf.VrsOut("BAR", outputFile.toString());
                }
            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
        }
        
        private DefaultIntervalCategoryDataset createIntervalDataset(final List minMaxLows, final List minMaxHighes, final List minMaxCategoryKeys) {
            Integer[] buf;
            final Integer[][] lows = new Integer[1][minMaxLows.size()];
            buf = new Integer[lows[0].length];
            minMaxLows.toArray(buf);
            lows[0] = buf;
            final Integer[][] highes = new Integer[1][minMaxHighes.size()];
            buf = new Integer[highes[0].length];
            minMaxHighes.toArray(buf);
            highes[0] = buf;
            
            String[] sbuf = new String[minMaxCategoryKeys.size()];
            minMaxCategoryKeys.toArray(sbuf);
            final String[] categoryKeys = sbuf;
            
            final DefaultIntervalCategoryDataset minMaxDataset = new DefaultIntervalCategoryDataset(new String[] {"範囲"}, categoryKeys, highes, lows);
            return minMaxDataset;
        }
        
        private String getAverageDatKey(final Param param, final Student student, final Record record) {
            final SubClass subClass = record._subClass;
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = AVG_DIV_GRADE + subClass._code;
            } else if ("3".equals(param._groupDiv)) {
                final boolean useChairGroup = !ALL8.equals(record._subClass._code) && (!subClass._isCommonSubclass && null != record._chairGroupcd);
                log.fatal(" subclass " + subClass + " isCommon " + subClass._isCommonSubclass + ", useChairGroup = " + useChairGroup);
                if (useChairGroup) {
                    key = AVG_DIV_CHAIRGROUP + subClass._code + student.chairGroupKey(record._chairGroupcd);
                } else {
                    key = AVG_DIV_COURSE + subClass._code + student.courseKey(); // コースの平均点
                }
            } else {
                key = AVG_DIV_COURSE + subClass._code + student.courseKey();
            }
            return key;
        }

        private JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset,
                final DefaultIntervalCategoryDataset minMaxDataset
        ) {
            final CategoryPlot plot = new CategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            final CategoryAxis axis = new CategoryAxis();
            plot.setDomainAxis(axis);
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            final NumberAxis numberAxis = new NumberAxis();
            numberAxis.setTickUnit(new NumberTickUnit(50));
            numberAxis.setTickLabelsVisible(true);
            numberAxis.setRange(0, 100.0);
            plot.setRangeAxis(numberAxis);

            // 追加する折れ線グラフの表示設定
            final IntervalBarRenderer renderer0 = new AlpIntervalBarRenderer();
            renderer0.setMaximumBarWidth(0.05);
            renderer0.setPaint(Color.lightGray);
            renderer0.setOutlinePaint(Color.black);
            renderer0.setItemLabelsVisible(true);
            plot.setDataset(0, minMaxDataset);
            plot.setRenderer(0, renderer0);

            // 追加する折れ線グラフの表示設定
            final LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
            renderer1.setItemLabelsVisible(true);
            renderer1.setPaint(Color.black);
            plot.setDataset(1, scoreDataset);
            plot.setRenderer(1, renderer1);
            
            // 追加する折れ線グラフの表示設定
            final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
            renderer2.setItemLabelsVisible(true);
            renderer2.setPaint(Color.gray);
            renderer2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[] {3.0f}, 0.0f));
            plot.setDataset(2, avgDataset);
            plot.setRenderer(2, renderer2);

            final JFreeChart chart = new JFreeChart(plot);
            chart.setBackgroundPaint(Color.white);

            return chart;
        }

//        private void printRadarGraph(final Map records, final List attendSubclassCdList) {
//            // データ作成
//            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//            for (final Iterator it0 = _param._classOrder.iterator(); it0.hasNext();) {
//                final String classCd = (String) it0.next();
//                
//                for (final Iterator it = records.values().iterator(); it.hasNext();) {
//                    final Record record = (Record) it.next();
//                    if (!classCd.equals(record._subClass.getClassKey())) {
//                        continue;
//                    }
//                    if (attendSubclassCdList.contains(record._subClass._code)) {
//                        continue;
//                    }
//                    setDataset(dataset, record);
//                }
//            }
//
//            try {
//                // チャート作成
//                final JFreeChart chart = createRaderChart(dataset);
//
//                // グラフのファイルを生成
//                final File outputFile = graphImageFile(chart, 938, 784);
//                _graphFiles.add(outputFile);
//
//                // グラフの出力
//                if (0 < dataset.getColumnCount() && outputFile.exists()) {
//                    _svf.VrsOut("RADER", outputFile.toString());
//                }
//            } catch (Throwable e) {
//                log.error("exception or error!", e);
//            }
//        }

//        private void setDataset(final DefaultCategoryDataset dataset, final Record record) {
//            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
//            final Class clazz = (Class) _param._classes.get(record._subClass.getClassKey());
//            final String name = (null == clazz) ? "" : clazz._abbv;
//            dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20
//            
//            dataset.addValue(50.0, "偏差値50", name);
//        }

//        /**
//         * 網掛けする。
//         * @param field フィールド
//         * @param value 値
//         */
//        void amikake(final String field, final String value) {
//            _svf.VrAttribute(field, "Paint=(2,70,1),Bold=1");
//            _svf.VrsOut(field, value);
//            _svf.VrAttribute(field, "Paint=(0,0,0),Bold=0");
//        }
//
//        void kasen(final String field, final String value) {
//            _svf.VrAttribute(field, "UnderLine=(0,1,1)");
//            _svf.VrsOut(field, value);
//        }

        private void closeSvf(boolean hasData) {
            if (!hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }
            _svf.VrQuit();
        }
        public void printScore(final Param param, final Student student) {
            
            for (int i = 0; i < param._examList.size(); i++) {
                final Exam exam = (Exam) param._examList.get(i);
                final String ii = String.valueOf(i + 1);
                _svf.VrsOut("TEST_NAME" + ii, exam._semestername + exam._testitemname); // 考査名称
            }
            
            int line = 1;
            for (final Iterator it = student._subclasses.values().iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();

                if ("90".equals(subClass.getClassCd())) {
                    continue;
                }
                
//                final List attendSubclassCdList = _param._thisExam.getAttendSubclassCdList(student);
//                if (attendSubclassCdList.contains(subClass._code)) {
//                    continue;
//                }

//                // 教科
//                final Class clazz = (Class) param._classes.get(subClass.getClassKey());
//                _svf.VrsOut("CLASS", clazz._name);

                // 科目
                _svf.VrsOutn("SUBCLASS" + (getMS932ByteLength(subClass._name) > 30 ? "3" : getMS932ByteLength(subClass._name) > 20 ? "2" : ""), line, subClass._name); // 科目

                subClass.setCommonSubclass(param, student);
                if (subClass._isCommonSubclass) {
                    _svf.VrsOutn("COMMOM_SUBCLASS", line, "○"); // 共通科目
                }

                for (int exi = 0; exi < param._examList.size(); exi++) {
                    final Exam exam = (Exam) param._examList.get(exi);
                    if (!"1".equals(exam._printFlg)) {
                        continue;
                    }
                    final String exline = String.valueOf(exi + 1);
                    
                    final Record record = (Record) student.getRecord(exam, subClass);
                    if (null != record) {
                        final String score = record.getScore();
                        if (param.isUnderScore(score)) {
                            _svf.VrsOutn("SCORE" + exline, line, "(" + score + ")");
                        } else {
                            _svf.VrsOutn("SCORE" + exline, line, score);
                        }

                        _svf.VrsOutn("RANK" + exline + "_1", line, record.getRank(param));
                        
                        final AverageDat avgDat = (AverageDat) exam._averageDat.get(getAverageDatKey(param, student, record));
                        if (null != avgDat && null != avgDat._count) {
//                            _svf.VrsOutn("AVERAGE" + exline,  line, avgDat.getAvgStr());
                            _svf.VrsOutn("RANK" + exline + "_2", line, avgDat._count.toString());
//                            _svf.VrsOut("MAX_SCORE", avgDat._highScore.toString());
                        }
                    }
                }

                if (++line >= TABLE_SUBCLASS_MAX) {
                    break;
                }
            }
            
            for (int exi = 0; exi < param._examList.size(); exi++) {
                final Exam exam = (Exam) param._examList.get(exi);
                if (!"1".equals(exam._printFlg)) {
                    continue;
                }
                final String exline = String.valueOf(exi + 1);
                Record record999 = student.getRecordOther(exam, ALL9);
                if (null != record999) {
                    _svf.VrsOutn("TOTAL_SCORE" + exline, 1, record999.getScore()); // 科目合計
                }
                Record record888 = student.getRecordOther(exam, ALL8);
                if (null != record888) {
                    _svf.VrsOutn("TOTAL_SCORE" + exline, 2, record888.getScore()); // 共通科目合計
                    _svf.VrsOutn("TOTAL_RANK" + exline + "_1", 2, record888.getCourseRank()); // 共通コース順位（共通合計は講座グループ順位はないためコース順位を表示する）
                    AverageDat avg888 = (AverageDat) exam._averageDatOther.get(getAverageDatKey(param, student, record888));
                    if (null != avg888 && null != avg888._count) {
                        _svf.VrsOutn("TOTAL_RANK" + exline + "_2", 2, avg888._count.toString()); // 共通コース順位
                    }
                }
            }
        }

        private File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
            final String tmpFileName = KNJServletUtils.createTmpFile(".png");
            log.debug("\ttmp file name=" + tmpFileName);

            final File outputFile = new File(tmpFileName);
            try {
                ChartUtilities.saveChartAsPNG(outputFile, chart, dot2pixel(dotWidth), dot2pixel(dotHeight));
            } catch (final IOException ioEx) {
                log.error("グラフイメージをファイル化できません。", ioEx);
            }

            return outputFile;
        }

        private static int dot2pixel(final int dot) {
            final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

            /*
             * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
             * 拡大すると粗くなってしまうから。
             */
            return (int) (pixel * 1.3);
        }
    }

    private class Student implements Comparable {
        private final String _schregno;
        private final String _grade;
        private final String _hrClass;
        private final String _attendNo;
        private final String _hrName;

        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final String _courseCodeName;

        private final String _name;

        /** 成績所見データ. */
        private final String _remark;

        /** 成績科目 */
        private final Map _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _record = new TreeMap();

        /** 成績データ。3教科,5教科用 */
        private final Map _recordOther = new HashMap();

        private Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String courseCodeName,
                final String name,
                final String remark
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _name = name;
            _remark = remark;
        }
        
        public String chairGroupKey(final String chairGroupcd) {
            final String majorcd = "000" + StringUtils.defaultString(chairGroupcd);
            return "0" + majorcd.substring(majorcd.length() - 3) + "0000";
        }

        private Map createMap(final Map m, final Object key) {
            if (null == m.get(key)) {
                m.put(key, new TreeMap());
            }
            return (Map) m.get(key);
        }
        
        public Record getRecord(final Exam exam, final SubClass subClass) {
            return (Record) createMap(_record, exam).get(subClass);
        }
        
        public void setRecord(final Exam exam, final SubClass subClass, final Record record) {
            createMap(_record, exam).put(subClass, record);
        }
        
        public Record getRecordOther(final Exam exam, final String subclasscd) {
            return (Record) createMap(_recordOther, exam).get(subclasscd);
        }
        
        public void setRecordOther(final Exam exam, final String subclasscd, final Record record) {
            createMap(_recordOther, exam).put(subclasscd, record);
        }
        
        public String attendSubclassCdKey() {
            return _grade + courseKey();
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        public int compareTo(Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            int rtn;
            rtn = this._grade.compareTo(that._grade);
            if (0 != rtn) {
                return rtn;
            }
            rtn = this._hrClass.compareTo(that._hrClass);
            if (0 != rtn) {
                return rtn;
            }
            return this._attendNo.compareTo(that._attendNo);
        }
    }

    /**
     * 教科。
     */
    private class Class {
        private final String _code;
        private final String _name;
        private final String _abbv;

        public Class(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }
    }

    /**
     * 科目。
     */
    private class SubClass implements Comparable {
        private final String _code;
        private final String _name;
        private final String _abbv;
        private final String _electdiv;
        private final Map _creditMstRequireflg = new HashMap(); // Map<coursecd+majorcd+coursecode, requireFlg>
        private boolean _isCommonSubclass;

//        /** 合併情報を持っているか */
//        private boolean _hasCombined;
//        /** 合併先か? */
//        private boolean _isSaki;
//        /** 合併元か? */
//        private boolean _isMoto;

        public SubClass(final String code, final String name, final String abbv, final String electdiv) {
            _code = code;
            _name = name;
            _abbv = abbv;
            _electdiv = electdiv;
        }

        /**
         * 科目マスタの、選択（ELECTDIV）’1’以外
         * 且つ
         * 単位マスタの、必履修区分（REQUIRE_FLG）の'3'以外
         *   名称マスタ「Z011」
         *   （東京都仕様）
         *   1:必履修
         *   2:学校必履修
         *   3:選択
         * 且つ
         * 講座グループデータ（CHAIR_GROUP_DAT）設定している科目
         * @param student
         * @return
         */
        public void setCommonSubclass(final Param param, final Student student) {
            boolean isCommonSubclass = true;
            if ("1".equals(_electdiv)) {
//                log.debug(" false: electDiv:" + _electdiv + ", " + _chairGroupSubclasscd);
                isCommonSubclass = false;
            }
            final String requireFlg = (String) _creditMstRequireflg.get(student.courseKey());
            if ("3".equals(requireFlg)) { // 必履修フラグが「選択」
//                log.debug(" false: requireflg:" + student.courseKey() + " = " + requireFlg);
                isCommonSubclass = false;
            }
            for (int exi = 0; exi < param._examList.size(); exi++) {
                final Exam exam = (Exam) param._examList.get(exi);
                final Record record = (Record) student.getRecord(exam, this);
                if (null != record && null != record._chairGroupcd) {
                    isCommonSubclass = false;
                }
            }
            _isCommonSubclass = isCommonSubclass;
        }

        public SubClass(final String code) {
            this(code, "xxx", "yyy", null);
        }

        public String getClassKey() {
            final String classCd;
            if ("1".equals(_param._useCurriculumcd)) {
                final String[] split = StringUtils.split(_code, "-");
                classCd =  split[0] + "-" + split[1];
            } else {
                classCd = _code.substring(0, 2);
            }
            return classCd;
        }

        public String getClassCd() {
            final String classCd;
            if ("1".equals(_param._useCurriculumcd)) {
                final String[] split = StringUtils.split(_code, "-");
                classCd =  split[0];
            } else {
                classCd = _code.substring(0, 2);
            }
            return classCd;
        }

//        public void setSaki() {
//            _hasCombined = true;
//            _isSaki = true;
//        }
//
//        public void setMoto() {
//            _hasCombined = true;
//            _isMoto = true;
//        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof SubClass)) {
                return -1;
            }
            final SubClass that = (SubClass) o;
            return this._code.compareTo(that._code);
        }
    }

    private class Record {
        final SubClass _subClass;
        final Integer _score;
        final Integer _scorePercent;
        final BigDecimal _avg;
        final Integer _gradeRank;
        final Integer _majorRank;
        final Integer _courseRank;
        final BigDecimal _deviation;
        final String _chaircd;
        final String _chairGroupcd;

        private Record(
                final SubClass subClass,
                final Integer score,
                final Integer scorePercent,
                final BigDecimal avg,
                final Integer gradeRank,
                final Integer majorRank,
                final Integer courseRank,
                final BigDecimal deviation,
                final String chaircd,
                final String chairGroupcd
        ) {
            _subClass = subClass;
            _score = score;
            _scorePercent = scorePercent;
            _avg = avg;
            _gradeRank = gradeRank;
            _majorRank = majorRank;
            _courseRank = courseRank;
            _deviation = deviation;
            _chaircd = chaircd;
            _chairGroupcd = chairGroupcd;
        }

        public String getRank(final Param param) {
            String rank = null;
            if ("2".equals(param._groupDiv)) {
                rank = getCourseRank(); 
            } else if ("3".equals(param._groupDiv)) {
                final boolean useChairGroup = !_subClass._isCommonSubclass && null != _chairGroupcd;
                log.fatal(" subclass " + _subClass + " isCommon " + _subClass._isCommonSubclass + ", useChairGroup = " + useChairGroup);
                if (useChairGroup) {
                    rank = getMajorRank();
                } else {
                    rank = getCourseRank();
                }
            }
            return rank;
        }

        public String getScore() {
            return null == _score ? "" : _score.toString();
        }

        public String getAvg() {
            return null == _avg ? "" : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getGradeRank() {
            return null == _gradeRank ? "" : _gradeRank.toString();
        }

        public String getMajorRank() {
            return null == _majorRank ? "" : _majorRank.toString();
        }

        public String getCourseRank() {
            return null == _courseRank ? "" : _courseRank.toString();
        }

        public String getDeviation() {
            return null == _deviation ? "" : _deviation.toString();
        }

        public String toString() {
            return _subClass + "/" + _score + "/(" + _gradeRank + ", " + _majorRank + ", " + _courseRank + ")/" + _deviation;
        }
    }

    private class AverageDat {
        private final SubClass _subClass;
        private final BigDecimal _avg;
        private final BigDecimal _avgPercent;
        private final BigDecimal _stdDev;
        private final Integer _highScore;
        private final Integer _lowScore;
        private final Integer _count;
        private final String _coursecd;
        private final String _majorcd;
        private final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal avgPercent,
                final BigDecimal stdDev,
                final Integer highScore,
                final Integer lowScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _avgPercent = avgPercent;
            _stdDev = stdDev;
            _highScore = highScore;
            _lowScore = lowScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        public String getStdDevStr() {
            return null == _stdDev ? null : _stdDev.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getAvgStr() {
            return null == _avg ? null : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String toString() {
            return _subClass + "/" + _avg + "/" + _count;
        }
    }
    
    /**
     * IntervalBarRenderer#setMaximumBarWidthメソッドのバグ修正版。
     * drawInterval メソッドのロジックをパクってから、★部分を追加した。
     */
    static class AlpIntervalBarRenderer extends IntervalBarRenderer {
        protected void drawInterval(Graphics2D g2,
                CategoryItemRendererState state,
                Rectangle2D dataArea,
                CategoryPlot plot,
                CategoryAxis domainAxis,
                ValueAxis rangeAxis,
                IntervalCategoryDataset dataset,
                int row,
                int column
        ) {
            int seriesCount = getRowCount();
            int categoryCount = getColumnCount();

            PlotOrientation orientation = plot.getOrientation();

            double rectX = 0.0;
            double rectY = 0.0;

            RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

            // Y0
            Number value0 = dataset.getEndValue(row, column);
            if (value0 == null) {
                return;
            }
            double java2dValue0 = rangeAxis.valueToJava2D(value0.doubleValue(), dataArea, rangeAxisLocation);

            // Y1
            Number value1 = dataset.getStartValue(row, column);
            if (value1 == null) {
                return;
            }
            double java2dValue1 = rangeAxis.valueToJava2D(value1.doubleValue(), dataArea, rangeAxisLocation);

            if (java2dValue1 < java2dValue0) {
                double temp = java2dValue1;
                java2dValue1 = java2dValue0;
                java2dValue0 = temp;
                Number tempNum = value1;
                value1 = value0;
                value0 = tempNum;
            }
            
            // BAR WIDTH
            double rectWidth = state.getBarWidth();

            // BAR HEIGHT
            double rectHeight = Math.abs(java2dValue1 - java2dValue0);

            if (orientation == PlotOrientation.HORIZONTAL) {
                // BAR Y
                rectY = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, domainAxisLocation);
                if (seriesCount > 1) {
                    double seriesGap = dataArea.getHeight() * getItemMargin() / (categoryCount * (seriesCount - 1));
                    rectY = rectY + row * (state.getBarWidth() + seriesGap);
                } else {
                    rectY = rectY + row * state.getBarWidth();
                }

                rectX = java2dValue0;

                rectHeight = state.getBarWidth();
                rectWidth = Math.abs(java2dValue1 - java2dValue0);

            } else if (orientation == PlotOrientation.VERTICAL) {
                // BAR X
                rectX = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, domainAxisLocation);
                if (seriesCount > 1) {
                    double seriesGap = dataArea.getWidth() * getItemMargin() / (categoryCount * (seriesCount - 1));
                    rectX = rectX + row * (state.getBarWidth() + seriesGap);
                } else {
                    rectX = rectX + row * state.getBarWidth();
                }

                /*
                 * (★)以下のロジックを追加した。
                 */
                // ↓↓↓↓↓↓↓
                final double normalWidth = dataArea.getWidth() * 0.7 / getColumnCount();
                final double centerSabun;
                if (categoryCount == 1) {
                    final double sa = dataArea.getWidth() / 10;
                    centerSabun = Math.abs((normalWidth - rectWidth) / 2) + sa;
                } else {
                    centerSabun = Math.abs((normalWidth - rectWidth) / 2);
                }
                rectX += centerSabun;
                // ↑↑↑↑↑↑↑
                /*
                 * 上記の解説。
                 * +-----------------------------------------+
                 * |         描画エリア                      |
                 * | +---------------------------(1)-------+ |
                 * | |             データエリア            | |
                 * | |                                     | |
                 * | |                                     | |
                 * | |     +----------(2)------------+     | |
                 * | |     |                         |     | |
                 * | |<-A->|                         |<-A->| |
                 * | |     |                         |     | |
                 * |<--B-->|                         |     | |
                 * | |     |                         |     | |
                 * | +-----+-------------------------+-----+ |
                 * |                                         |
                 * +-----------------------------------------+
                 *
                 * (1) : dataArea.getWidth()
                 * (2) : normalWidth : 棒幅が全体の70%の時の1本あたりの幅
                 * 
                 * A : グラフエリアの両サイドマージンは、各々 15 のようだ。
                 * B : rectX : 棒グラフ左端の座標。基点は描画エリア左端(グラフエリアではない)
                 * 
                 */

                rectY = java2dValue0;

            }
            Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
            Paint seriesPaint = getItemPaint(row, column);
            g2.setPaint(seriesPaint);
            g2.fill(bar);

            // draw the outline...
            if (state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
                Stroke stroke = getItemOutlineStroke(row, column);
                Paint paint = getItemOutlinePaint(row, column);
                if (stroke != null && paint != null) {
                    g2.setStroke(stroke);
                    g2.setPaint(paint);
                    g2.draw(bar);
                }
            }

            CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
            if (generator != null && isItemLabelVisible(row, column)) {
                drawItemLabel(g2, dataset, row, column, plot, generator, bar, false);
            }

            // collect entity and tool tip information...
            if (state.getInfo() != null) {
                EntityCollection entities = state.getEntityCollection();
                if (entities != null) {
                    String tip = null;
                    CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
                    if (tipster != null) {
                        tip = tipster.generateToolTip(dataset, row, column);
                    }
                    String url = null;
                    if (getItemURLGenerator(row, column) != null) {
                        url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
                    }
                    CategoryItemEntity entity = new CategoryItemEntity(bar, tip, url, dataset, row, dataset.getColumnKey(column), column);
                    entities.add(entity);
                }
            }
        }
    }
    
} // KNJD105F

// eof
