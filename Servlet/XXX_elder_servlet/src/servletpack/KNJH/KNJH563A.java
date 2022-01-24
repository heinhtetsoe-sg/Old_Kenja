// kanji=漢字
/*
 * $Id: 0280c0461b21456cc89b0a88a58b11cc93f1ddaf $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
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
 * @author takaesu
 * @version $Id: 0280c0461b21456cc89b0a88a58b11cc93f1ddaf $
 */
public class KNJH563A {
    /*pkg*/static final Log log = LogFactory.getLog(KNJH563A.class);

    /** 棒グラフ凡例画像1(学年). */
    private static final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
    /** 棒グラフ凡例画像2(コース). */
    private static final String BAR_CHART_LEGEND2 = "BarChartLegendCourse.png";
    /** 棒グラフ凡例画像3(コースグループ). */
    private static final String BAR_CHART_LEGEND3 = "BarChartLegendCourseGroup.png";
    
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
    /** 全教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final String FORM_FILE = "KNJH563A.frm";
    
    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_COURSE = "03";
    private static final String RANK_DIV_COURSEGROUP = "05";
    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_COURSEGROUP = "05";

    private Param _param;
    private boolean _hasData;
    private Form _form;
    private DB2UDB _db2;

    /** 模試科目マスタ。 */
    private Map _subClasses;

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);
        _form = new Form(_param, response);
        final String dbName = request.getParameter("DBNAME");
        _db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        if (openDb(_db2)) {
            return;
        }

        log.error("★マスタ関連の読込み");
        _param.load(_db2);

        // 対象の生徒たちを得る
        final List students = createStudents(_param.getScregnos(_db2));
        _hasData = students.size() > 0;

        // 成績のデータを読む
        log.error("★成績関連の読込み");
        
        loadExam(students, _param._thisExam);

        // 印刷する
        log.error("★印刷");
        
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug("☆" + student + ", 科目の数=" + student._data._subclasses.size() + ", コースキー=" + student._regd.courseKey());
            log.debug("今回の成績: " + student._data._record.values());

//            _param.setSubclasses(student);

            _form.resetForm();
            _form.printStatic(_param, student);
            _form.printRecord(_param, student);
        }

        log.error("★終了処理");
        _form.closeSvf();
        closeDb(_db2);
        removeImageFiles();
        log.info("Done.");
    }

    private void loadExam(final List students, final Exam exam) throws SQLException {
        loadSubClasses(_db2, students, exam); // 科目は指示画面指定のテストの科目
        loadAverageDat(_db2, exam);
        loadRecord(_db2, students, exam);
//        loadAttendSubclass1(_db2, students, exam);
        loadRecordOther(_db2, students, exam);
    }

    private void loadSubClasses(final DB2UDB db2, final List students, final Exam exam) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlSubClasses(exam));
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) _subClasses.get(subClasscd);
                    if (null != subClass) {
                        student._data._subclasses.put(subClasscd, subClass);
                    }
                }
            }
        } catch (final SQLException e) {
            log.fatal("模試データにある科目の取得でエラー");
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlSubClasses(final Exam exam) {
        final String rtn;
        rtn = "select"
                + "  distinct T1.PROFICIENCY_SUBCLASS_CD as SUBCLASSCD"
                + " from"
                + "  PROFICIENCY_DAT T1"
                + " where"
                + "  T1.year='" + exam._year + "' AND"
                + "  T1.semester='" + exam._semester + "' AND"
                + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                + "  T1.SCHREGNO = ? "
                ;
        log.debug("模試データにある科目のSQL=" + rtn);
        return rtn;
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
            final Exam exam
    ) throws SQLException {
        
        final String selectFrom;
        selectFrom = "SELECT"
            + "  proficiency_subclass_cd as subclasscd,"
            + "  avg,"
            + "  stddev,"
            + "  highscore,"
            + "  lowscore,"
            + "  avg_kansan as graphAvg,"
            + "  count,"
            + "  coursecd,"
            + "  majorcd,"
            + "  coursecode"
            + " FROM"
            + "  proficiency_average_dat"
            ;
        final String where;
        if (_param.isGakunen()) {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + _param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_GRADE + "' AND"
                + "    grade='" + exam._grade + "' AND"
                + "    hr_class='000' AND"
                + "    coursecd='0' AND"
                + "    majorcd='000' AND"
                + "    coursecode='0000'"
                ;
        } else if (_param.isCoursegroup()) {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + _param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_COURSEGROUP + "' AND"
                + "    grade='" + exam._grade + "' AND"
                + "    hr_class='000'"
                ;
        } else {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + _param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_COURSE + "' AND"
                + "    grade='" + exam._grade + "' AND"
                + "    hr_class='000'"
                ;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = selectFrom + where;
//            log.debug(" avg1 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                final SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("模試成績平均データが模試科目マスタに無い!:" + subclasscd);
                }
                final BigDecimal avg = rs.getBigDecimal("avg");
                final BigDecimal stddev = rs.getBigDecimal("stddev");
                final BigDecimal graphAvg = rs.getBigDecimal("graphAvg");
                final Integer count = KNJServletUtils.getInteger(rs, "count");
                final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                final Integer lowScore = KNJServletUtils.getInteger(rs, "lowscore");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                final String key;
                if (_param.isGakunen()) {
                    key = subclasscd;
                } else if (_param.isCoursegroup()) {
                    key = subclasscd + "0" + majorcd + "0000";
                } else {
                    key = subclasscd + coursecd + majorcd + coursecode;
                }
                if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    final AverageDat avgDat = new AverageDat(subClass0, avg, graphAvg, stddev, highScore, lowScore, count, coursecd, majorcd, coursecode);
                    exam._averageDatOther.put(key, avgDat);
                } else {
                    final AverageDat avgDat = new AverageDat(subClass, avg, graphAvg, stddev, highScore, lowScore, count, coursecd, majorcd, coursecode);
                    exam._averageDat.put(key, avgDat);
                }
            }
        } catch (final SQLException e) {
            log.warn("模試成績平均データの取得でエラー", e);
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.debug("模試コード=" + exam._proficiencydiv + " : " + exam._proficiencycd + " の模試成績平均データの件数=" + exam._averageDat.size());
    }

//    private String sqlAttendSubclass(
//            final Regd regd,
//            final Exam exam
//    ) {
//        final StringBuffer sql = new StringBuffer();
//        /* 通常の成績 */
//        sql.append(" SELECT ");
//        sql.append("     T1.DIV, ");
//        sql.append("     T1.COMBINED_SUBCLASSCD, ");
//        sql.append("     T1.ATTEND_SUBCLASSCD ");
//        sql.append(" FROM ");
//        sql.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
//        sql.append(" WHERE ");
//        sql.append("     T1.YEAR = '" + exam._year + "' ");
//        sql.append("     AND T1.SEMESTER = '" + exam._semester + "' ");
//        sql.append("     AND T1.PROFICIENCYDIV = '" + exam._proficiencydiv + "' ");
//        sql.append("     AND T1.PROFICIENCYCD = '" + exam._proficiencycd + "' ");
//        sql.append("     AND T1.GRADE = '" + regd._grade + "' ");
//        sql.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
//        sql.append("         CASE WHEN T1.DIV = '04' THEN '" + regd.coursegroupKey() + "' ");
//        sql.append("         ELSE '" + regd.courseKey() + "' END ");
//        sql.append(" ORDER BY ");
//        sql.append("     T1.DIV, ");
//        sql.append("     T1.COMBINED_SUBCLASSCD, ");
//        sql.append("     T1.ATTEND_SUBCLASSCD ");
//        return sql.toString();
//    }

//    private void loadAttendSubclass1(
//            final DB2UDB db2,
//            final List students,
//            final Exam exam
//    ) {
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            for (final Iterator it = students.iterator(); it.hasNext();) {
//                final Student student = (Student) it.next();
//                final Regd regd = student._regd;
//                if (null == regd._grade) {
//                    continue;
//                }
//                final String sql = sqlAttendSubclass(regd, exam);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String subclasscd = rs.getString("ATTEND_SUBCLASSCD");
//                    SubClass subClass = (SubClass) _subClasses.get(subclasscd);
//                    if (null == subClass) {
//                        log.warn("合併科目のデータが模試科目マスタに無い!:" + subclasscd);
//                        final Class clazz = (Class) _param._classes.get(subclasscd.substring(0, 2));
//                        if (null == clazz) {
//                            continue;
//                        }
//                        subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
//                    }
//                    student._data._attendSubclasses.add(subClass);
//                }
//            }
//        } catch (final SQLException e) {
//            log.warn("合併科目のデータの取得でエラー", e);
//        } finally {
//            DbUtils.closeQuietly(rs);
//        }
//    }

    private void loadRecord(
            final DB2UDB db2,
            final List students,
            final Exam exam
    ) {
        String sql;
        /* 通常の成績 */
        sql = "SELECT"
            + "  T1.proficiency_subclass_cd as subclasscd,"
            + "  T3.score,"
            + "  T1.score_di,"
            + "  T2.score as graphScore,"
            + "  T3.rank as grade_rank,"
            + "  T3.deviation as grade_deviation,"
            + "  T4.rank as course_rank,"
            + "  T4.deviation as course_deviation,"
            + "  T6.rank as coursegroup_rank,"
            + "  T6.deviation as coursegroup_deviation,"
            + "  T5.PASS_SCORE"
            + " FROM proficiency_dat T1 "
            + " LEFT JOIN proficiency_rank_dat T3 ON "
            + "     T3.year = T1.year AND "
            + "     T3.semester=T1.semester AND"
            + "     T3.proficiencydiv=T1.proficiencydiv AND"
            + "     T3.proficiencycd=T1.proficiencycd AND"
            + "     T3.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T3.schregno = T1.schregno AND"
            + "     T3.rank_data_div ='" + _param._rankDataDiv + "' AND"
            + "     T3.rank_div = '" + RANK_DIV_GRADE + "' "
            + " LEFT JOIN proficiency_rank_dat T2 ON "
            + "     T1.year = T2.year AND "
            + "     T1.semester=T2.semester AND"
            + "     T1.proficiencydiv=T2.proficiencydiv AND"
            + "     T1.proficiencycd=T2.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T2.proficiency_subclass_cd AND"
            + "     T1.schregno = T2.schregno AND "
            + "     T2.rank_data_div = '03' AND "
            + "     T2.rank_div = '" + RANK_DIV_GRADE + "' "
            + " LEFT JOIN proficiency_rank_dat T4 ON "
            + "     T4.year = T1.year AND "
            + "     T4.semester=T1.semester AND"
            + "     T4.proficiencydiv=T1.proficiencydiv AND"
            + "     T4.proficiencycd=T1.proficiencycd AND"
            + "     T4.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T4.schregno = T1.schregno AND"
            + "     T4.rank_data_div =t3.rank_data_div AND"
            + "     T4.rank_div = '" + RANK_DIV_COURSE + "' "
            + " LEFT JOIN proficiency_rank_dat T6 ON "
            + "     T6.year = T1.year AND "
            + "     T6.semester=T1.semester AND"
            + "     T6.proficiencydiv=T1.proficiencydiv AND"
            + "     T6.proficiencycd=T1.proficiencycd AND"
            + "     T6.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T6.schregno = T1.schregno AND"
            + "     T6.rank_data_div =t3.rank_data_div AND"
            + "     T6.rank_div = '" + RANK_DIV_COURSEGROUP + "' "
            + " LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT T5 ON T5.YEAR = T1.YEAR AND "
            + "     T5.semester=T2.semester AND"
            + "     T5.proficiencydiv=T2.proficiencydiv AND"
            + "     T5.proficiencycd=T2.proficiencycd AND"
            + "     T5.proficiency_subclass_cd=T2.proficiency_subclass_cd "
            + "     AND T5.GRADE = CASE WHEN T5.DIV = '01' THEN '00' ELSE ? END  "
            + "     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = "
            + "       CASE WHEN T5.DIV = '01' OR T5.DIV = '02' THEN '00000000' ELSE ? END "
            + " WHERE"
            + "  T1.year='" + exam._year + "' AND"
            + "  T1.semester='" + exam._semester + "' AND"
            + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
            + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
            + "  T1.proficiency_subclass_cd NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
            + "  T1.schregno=?"
            ;
        loadRecord1(db2, students, sql);
    }

    private void loadRecord1(
            final DB2UDB db2,
            final List students,
            final String sql
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final Regd regd = student._regd;
                int i = 0;
                if (null == regd._grade) {
                    continue;
                }
                //欠点（満点マスタの合格点）
                ps.setString(++i, regd._grade);
                ps.setString(++i, regd.courseKey());
                ps.setString(++i, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが模試科目マスタに無い!:" + subclasscd);
                        final Class clazz = (Class) _param._classes.get(subclasscd.substring(0, 2));
                        if (null == clazz) {
                            continue;
                        }
                        subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    final String scoreDi = rs.getString("score_di");
                    if (score == null && StringUtils.isEmpty(scoreDi)) {
                        continue;
                    }
                    
                    final Integer graphScore = KNJServletUtils.getInteger(rs, "graphScore");
                    final Integer rank;
                    final BigDecimal deviation;
                    if (_param.isGakunen()) {
                        rank = KNJServletUtils.getInteger(rs, "grade_rank");
                        deviation = rs.getBigDecimal("grade_deviation");
                    } else if (_param.isCoursegroup()) {
                        rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
                        deviation = rs.getBigDecimal("coursegroup_deviation");
                    } else {
                        rank = KNJServletUtils.getInteger(rs, "course_rank");
                        deviation = rs.getBigDecimal("course_deviation");
                    }
                    
                    final Integer passScore = KNJServletUtils.getInteger(rs, "PASS_SCORE");

                    final Record rec = new Record(subClass, score, scoreDi, graphScore, null, rank, deviation, passScore);
                    student._data._record.put(subClass, rec);
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
            final List students,
            final Exam exam
    ) {
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String sql;
            sql = "SELECT"
                + "  t1.proficiency_subclass_cd as subclasscd,"
                + "  t1.score,"
                + "  t2.avg,"
                + "  t1.rank as grade_rank,"
                + "  t1.deviation as grade_deviation,"
                + "  t3.rank as course_rank,"
                + "  t3.deviation as course_deviation,"
                + "  t4.rank as coursegroup_rank,"
                + "  t4.deviation as coursegroup_deviation"
                + " FROM proficiency_rank_dat T1 "
                + " LEFT JOIN proficiency_rank_dat T2 ON "
                + "     T1.year = T2.year AND "
                + "     T1.semester=T2.semester AND"
                + "     T1.proficiencydiv=T2.proficiencydiv AND"
                + "     T1.proficiencycd=T2.proficiencycd AND"
                + "     T1.proficiency_subclass_cd=T2.proficiency_subclass_cd AND"
                + "     T1.schregno = T2.schregno AND "
                + "     T2.rank_data_div = '04' AND "
                + "     T2.rank_div = T1.rank_div "
                + " LEFT JOIN proficiency_rank_dat T3 ON "
                + "     T1.year = T3.year AND "
                + "     T1.semester=T3.semester AND"
                + "     T1.proficiencydiv=T3.proficiencydiv AND"
                + "     T1.proficiencycd=T3.proficiencycd AND"
                + "     T1.proficiency_subclass_cd=T3.proficiency_subclass_cd AND"
                + "     T1.schregno = T3.schregno AND "
                + "     T1.rank_data_div = T3.rank_data_div AND "
                + "     T3.rank_div = '" + RANK_DIV_COURSE + "' "
                + " LEFT JOIN proficiency_rank_dat T4 ON "
                + "     T1.year = T4.year AND "
                + "     T1.semester=T4.semester AND"
                + "     T1.proficiencydiv=T4.proficiencydiv AND"
                + "     T1.proficiencycd=T4.proficiencycd AND"
                + "     T1.proficiency_subclass_cd=T4.proficiency_subclass_cd AND"
                + "     T1.schregno = T4.schregno AND "
                + "     T1.rank_data_div = T4.rank_data_div AND "
                + "     T4.rank_div = '" + RANK_DIV_COURSEGROUP + "' "
                + " WHERE"
                + "  T1.year='" + exam._year + "' AND"
                + "  T1.semester='" + exam._semester + "' AND"
                + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                + "  T1.proficiency_subclass_cd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
                + "  t1.rank_data_div ='" + _param._rankDataDiv + "' AND"
                + "  t1.rank_div ='" + RANK_DIV_GRADE + "' AND"
                + "  t1.schregno='" + student._schregno + "'"
                ;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    final BigDecimal avg = rs.getBigDecimal("avg");
                    final Integer rank;
                    final BigDecimal deviation;
                    if (_param.isGakunen()) {
                        rank = KNJServletUtils.getInteger(rs, "grade_rank");
                        deviation = rs.getBigDecimal("grade_deviation");
                    } else if (_param.isCoursegroup()) {
                        rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
                        deviation = rs.getBigDecimal("coursegroup_deviation");
                    } else {
                        rank = KNJServletUtils.getInteger(rs, "course_rank");
                        deviation = rs.getBigDecimal("course_deviation");
                    }

                    final SubClass subClass = new SubClass(subclasscd);
                    final Record rec = new Record(subClass, score, null, null, avg, rank, deviation, null);
                    student._data._recordOther.put(subClass._code, rec);
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private List createStudents(final String[] schregnos) throws SQLException {
        final List rtn = new LinkedList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = studentsSQL(schregnos, _param._year, _param._semester);
            ps = _db2.prepareStatement(sql);
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
                final String coursegroupcd = rs.getString("coursegroupcd");
                final String name = rs.getString("name");
                
                final Regd regd = new Regd(grade, hrclass, attendno, hrName, coursecd, majorcd, coursecode, coursegroupcd);

                final Student student = new Student(
                        schregno,
                        regd,
                        name
                );
                rtn.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー");
            throw e;
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        Collections.sort(rtn);
        return rtn;
    }

    private String studentsSQL(final String[] selected, final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.grade,"
            + "  t1.hr_class,"
            + "  t1.attendno,"
            + "  t1.hr_name,"
            + "  t1.coursecd,"
            + "  t1.majorcd,"
            + "  t1.coursecode,"
            + "  l2.group_cd as coursegroupcd,"
            + "  t1.name"
            + " FROM"
            + "  v_schreg_info t1 "
            + "  left join course_group_cd_dat l1 on l1.year = t1.year "
            + "      and l1.grade = t1.grade "
            + "      and l1.coursecd = t1.coursecd "
            + "      and l1.majorcd = t1.majorcd "
            + "      and l1.coursecode = t1.coursecode "
            + "  left join course_group_cd_hdat l2 on l2.year = l1.year "
            + "      and l2.grade = l1.grade "
            + "      and l2.group_cd = l1.group_cd "
            + " WHERE"
            + "  t1.year='" + year + "' AND"
            + "  t1.semester='" + semester + "' AND"
            + "  t1.schregno IN " + SQLUtils.whereIn(true, selected)
            ;
        return sql;
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

    private void removeImageFiles() {
        for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
            final File imageFile = (File) it.next();
            if (null == imageFile) {
                continue;
            }
            log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
        }
    }

    private static int dot2pixel(final int dot) {
        final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

        /*
         * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
         * 拡大すると粗くなってしまうから。
         */
        return (int) (pixel * 1);
    }

    private static class Exam {

        private final String _year;
        private final String _semester;
        private final String _proficiencydiv;
        private final String _proficiencycd;
        private final String _grade;
        private String _examName = "";
        private String _semestername = "";
        private String _title = "";
        
        /** 成績平均データ。 */
        private Map _averageDat = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map _averageDatOther = new HashMap();
        
        public Exam(final String year, final String semester, final String proficiencydiv, final String proficiencycd, final String grade) {
            _year = year;
            _semester = semester;
            _proficiencydiv = proficiencydiv;
            _proficiencycd = proficiencycd;
            _grade = grade;
        }

        private void load(final DB2UDB db2) {
            loadExam(db2);
        }

        private void loadExam(final DB2UDB db2) {
            if (null == _year) {
                _title = "";
                return;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT proficiencyname1 FROM proficiency_mst WHERE proficiencydiv = '" + _proficiencydiv + "' and proficiencycd = '" + _proficiencycd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _examName = null == rs.getString("proficiencyname1") ? "" : rs.getString("proficiencyname1");
                }
            } catch (final SQLException e) {
                log.error("proficiencymst の取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _semestername = "";
            try {
                final String sql = "SELECT semestername FROM semester_mst"
                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
                    ;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semestername = null == rs.getString("semestername") ? "" : rs.getString("semestername");
                }
            } catch (final SQLException e) {
                log.error("学期名取得エラー。");
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            // _title = nendo() + "　" + _semestername + "　" + _examName;
            _title = nendo() + "　" + StringUtils.defaultString(_examName, "");
        }
        
        String nendo() {
            // return KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
            return _year + "年度";
        }
        
        public String toString() {
            return "Exam(" + _year + ":" + _semester + ":" + _proficiencydiv + ":" + _proficiencycd + ":" + _examName + ")";
        }
    }

    private class Param {
        private final String _year;
        private final String _semester;

        private final String _groupDiv; // 1=学年, 2=コース, 3=コースグループ
        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;
        /** 中高一貫フラグ。 */
        private boolean _isIkkan;

        /** [クラス指定 or 生徒指定]の値。 */
        private final String _div;
        /** クラス or 生徒。 */
        private final String[] _values;

        private final String _submitDate;
        private final String _loginDate;

        /** クラス指定 or 生徒指定。 */
        private final boolean _isClassMode;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 偏差値を印字するか? */
        private final String _deviationPrint;
        /** 順位を印字しないか? */
        private final boolean _rankNotPrint;
        /** 順位の基準点 1:総合点,2:平均点,3:偏差値,11:傾斜総合点 */
        private final String _rankDataDiv;
        /** 平均の基準点 1:得点,2:傾斜総合点 */
        private final String _avgDataDiv;
        private final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        private Map _classes;
        private final String _imagePath;

//        private final Map _subclassGroup3 = new HashMap();
//        private final Map _subclassGroup5 = new HashMap();
//        private final MultiMap _subclassGroupDat3 = new MultiHashMap();
//        private final MultiMap _subclassGroupDat5 = new MultiHashMap();

        /** レーダーチャートの科目. */
        private List _fiveSubclass = new ArrayList();
        private List _threeSubclass = new ArrayList();

        private boolean _isKumamoto;
        
        private final Exam _thisExam;

        public Param(HttpServletRequest request) {
            
            final String rankDataDiv;
            final String avgDataDiv;
            if ("4".equals(request.getParameter("JUNI"))) {
                rankDataDiv = "11";
                avgDataDiv = "2";
            } else {
                final String rankDivTemp = request.getParameter("useKnjd106cJuni" + request.getParameter("JUNI"));
                final String rankDataDiv0 = (rankDivTemp == null) ? request.getParameter("JUNI") : rankDivTemp;
                rankDataDiv = (null != rankDataDiv0 && rankDataDiv0.length() < 2 ? "0" : "") + rankDataDiv0;
                avgDataDiv = "1";
            }

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _div = request.getParameter("CATEGORY_IS_CLASS");
            _values = request.getParameterValues("CATEGORY_SELECTED");
            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");

            _isClassMode = "1".equals(request.getParameter("CATEGORY_IS_CLASS")) ? true : false;

            _groupDiv = request.getParameter("GROUP_DIV");
            _deviationPrint = request.getParameter("DEVIATION_PRINT");
            _rankNotPrint = "1".equals(request.getParameter("JUNI_PRINT"));
            _rankDataDiv = rankDataDiv;
            _avgDataDiv = avgDataDiv;

            _imagePath = request.getParameter("IMAGE_PATH");

            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            imageFileCheck(BAR_CHART_LEGEND3);
            
            _thisExam = new Exam(_year, _semester, request.getParameter("PROFICIENCYDIV"), request.getParameter("PROFICIENCYCD"), request.getParameter("GRADE"));
        }
        
        public boolean isGakunen() {
            return "1".equals(_groupDiv);
        }
        
        public boolean isCoursegroup() {
            return "3".equals(_groupDiv);
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
                    ps = db2.prepareStatement(hrToStudentsSQL(grade, room));
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
            loadZ010(db2);
            loadIkkanFlg(db2);
            loadCertifSchool(db2);
            loadRegdHdat(db2);
            _thisExam.load(db2);
            log.debug(" thisExam   = "+ _thisExam);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);

            // proficiency_subclass_group_dat を読込んで _fiveSubclass や _threeSubclass の箱に入れる
//            loadSubclassGroup(db2);
//            loadSubclassGroupDat(db2);
        }

        private void loadZ010(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = " SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'Z010' AND YEAR = '"  + _year  + "' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _isKumamoto = "kumamoto".equals(rs.getString("NAME1"));
                }
            } catch (final SQLException e) {
                log.error("名称マスタ取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSubClasses());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    final String name = rs.getString("NAME");
                    final String abbv = rs.getString("SUBCLASSABBV");
                    rtn.put(code, new SubClass(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("模試科目マスタ総数=" + rtn.size());
            return rtn;
        }

        public String sqlSubClasses() {
            return "select"
                    + "   PROFICIENCY_SUBCLASS_CD as SUBCLASSCD,"
                    + "   SUBCLASS_NAME as NAME,"
                    + "   SUBCLASS_ABBV as SUBCLASSABBV"
                    + " from PROFICIENCY_SUBCLASS_MST"
                    + " order by"
                    + "   PROFICIENCY_SUBCLASS_CD"
                ;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlClasses());
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

        public String sqlClasses() {
            return "select"
                    + "   CLASSCD,"
                    + "   CLASSNAME,"
                    + "   CLASSABBV"
                    + " from V_CLASS_MST"
                    + " where"
                    + "   YEAR = '" + _year + "'"
                    + " order by"
                    + "   CLASSCD"
                ;
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

        private void loadCertifSchool(final DB2UDB db2) throws SQLException {
            final String key = _isJunior ? "110" : "109";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT school_name, remark2 FROM certif_school_dat"
                    + " WHERE year='" + _year + "' AND certif_kindcd='" + key + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("school_name");
                    _remark2 = rs.getString("remark2");
                }
            } catch (final SQLException e) {
                log.error("学校名取得エラー。");
                throw e;
            }
        }

        private void loadIkkanFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT namespare2 FROM v_name_mst WHERE year='" + _param._year + "' AND namecd1='Z010' AND namecd2='00'");
                rs = ps.executeQuery();
                rs.next();
                final String namespare2 = rs.getString("namespare2");
                _isIkkan = (namespare2 != null) ? true : false;

                final int gradeVal = Integer.parseInt(_thisExam._grade);
                _isJunior = (gradeVal <= 3 && _isIkkan) ? true : false;
                _isHigh = !_isJunior;
            } catch (final SQLException e) {
                log.error("中高一貫フラグ取得エラー。");
            }
            log.debug("中高一貫フラグ=" + _isIkkan);
        }

        private String hrToStudentsSQL(final String grade, final String room) {
            return " select"
                    + "    SCHREGNO as schregno"
                    + " from"
                    + "    SCHREG_REGD_DAT"
                    + " where"
                    + "    YEAR = '" + _year + "' and"
                    + "    SEMESTER = '" + _semester + "' and"
                    + "    GRADE = '" + grade + "' and"
                    + "    HR_CLASS = '" + room + "'"
                    ;
        }

//        String get3title(final String courseKey) {
//            final String rtn = (String) _subclassGroup3.get(courseKey);
//            return rtn;
//        }
//
//        String get5title(final String courseKey) {
//            final String rtn = (String) _subclassGroup5.get(courseKey);
//            return rtn;
//        }
//
//        public void setSubclasses(final Student student) {
//            _fiveSubclass = (List) _subclassGroupDat5.get(student.courseKey());
//            _threeSubclass = (List) _subclassGroupDat3.get(student.courseKey());
//        }

//        private void loadSubclassGroup(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = subclassGroupSQL();
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String groupDiv = rs.getString("group_div");
//                    final String courseCd = rs.getString("coursecd");
//                    final String majorCd = rs.getString("majorcd");
//                    final String courseCode = rs.getString("coursecode");
//                    final String groupName = rs.getString("group_name");
//
//                    final String key = courseCd + majorCd + courseCode;
//                    if ("3".equals(groupDiv)) {
//                        _subclassGroup3.put(key, groupName);
//                    } else {
//                        _subclassGroup5.put(key, groupName);
//                    }
//                }
//            } catch (final SQLException e) {
//                log.error("proficiency_subclass_group_mst の取得エラー。");
//            }
//
//            log.debug("3教科の名称たち=" + _subclassGroup3);
//            log.debug("5教科の名称たち=" + _subclassGroup5);
//        }

//        private String subclassGroupSQL() {
//            return "SELECT"
//                    + "  group_div,"
//                    + "  coursecd,"
//                    + "  majorcd,"
//                    + "  coursecode,"
//                    + "  group_name"
//                    + " FROM"
//                    + "  proficiency_subclass_group_mst"
//                    + " WHERE"
//                    + "  year='" + _thisExam._year + "' AND"
//                    + "  semester='" + _thisExam._semester + "' AND"
//                    + "  proficiencydiv='" + _thisExam._proficiencydiv + "' AND"
//                    + "  proficiencycd='" + _thisExam._proficiencycd + "' AND"
//                    + "  grade='" + _grade + "' AND"
//                    + "  group_div in ('3', '5')"   // 3教科, 5教科
//                    ;
//        }

//        private void loadSubclassGroupDat(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = subclassGroupDatSQL();
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String groupDiv = rs.getString("group_div");
//                    final String courseCd = rs.getString("coursecd");
//                    final String majorCd = rs.getString("majorcd");
//                    final String courseCode = rs.getString("coursecode");
//                    final String subclassCd = rs.getString("subclasscd");
//
//                    final String key = courseCd + majorCd + courseCode;
//                    if ("3".equals(groupDiv)) {
//                        _subclassGroupDat3.put(key, subclassCd);
//                    } else {
//                        _subclassGroupDat5.put(key, subclassCd);
//                    }
//                }
//            } catch (final SQLException e) {
//                log.error("proficiency_subclass_group_dat の取得エラー");
//            }
//
//            log.debug("3教科の科目CDたち=" + _subclassGroupDat3);
//            log.debug("5教科の科目CDたち=" + _subclassGroupDat5);
//        }

//        private String subclassGroupDatSQL() {
//            return "SELECT"
//                    + "  group_div,"
//                    + "  coursecd,"
//                    + "  majorcd,"
//                    + "  coursecode,"
//                    + "  proficiency_subclass_cd as subclasscd"
//                    + " FROM"
//                    + "  proficiency_subclass_group_dat"
//                    + " WHERE"
//                    + "  year='" + _thisExam._year + "' AND"
//                    + "  semester='" + _thisExam._semester + "' AND"
//                    + "  proficiencydiv='" + _thisExam._proficiencydiv + "' AND"
//                    + "  proficiencycd='" + _thisExam._proficiencycd + "' AND"
//                    + "  grade='" + _grade + "' AND"
//                    + "  group_div in ('3', '5')"   // 3教科, 5教科
//                    ;
//        }

    }

    private class Form {
        private Vrw32alp _svf;
        private final Param _param;

        public Form(final Param param, final HttpServletResponse response) throws IOException {
            _param = param;
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
        }

        public void resetForm() {
            final int sts = _svf.VrSetForm(FORM_FILE, 4);
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }
        }
        
        public void printStatic(final Param param, final Student student) {

//            _svf.VrsOut("SEMESTER", param._thisExam._semestername);
//            _svf.VrsOut("TESTNAME", param._thisExam._examName);
            _svf.VrsOut("TITLE", param._thisExam._title);
            _svf.VrsOut("SCHOOL_NAME", param._schoolName);
            final String staffName = (String) param._staffs.get(student._regd._grade + student._regd._hrClass);
            _svf.VrsOut("STAFFNAME", (param._remark2 == null ? "" : param._remark2) + (staffName == null ? "" : staffName));
            _svf.VrsOut("NENDO", _param._thisExam._title);
            _svf.VrsOut("NENDO2", "家庭から　　（" + _param._thisExam._title + "）");
            _svf.VrsOut("HR_NAME", student._regd._hrName + student._regd.attendNoStr());
            _svf.VrsOut("NAME" + (StringUtils.defaultString(student._name).length() <= STUDENT_NAME_DELIMTER_COUNT ? "" : "_2"), student._name); // 全角で規定文字数を超えたらフォントを変える

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

//            final String get3title = _param.get3title(student.courseKey());
//            if (null != get3title) {
//                _svf.VrsOut("ITEM_TOTAL3", get3title);
//            }
//            final String get5title = _param.get5title(student.courseKey());
//            if (null != get5title) {
//                _svf.VrsOut("ITEM_TOTAL5", get5title);
//            }
            
            final File logoFile = new File(_param._imagePath + "/" + "SCHOOLLOGO" + ".jpg");
            if (logoFile.exists()) {
                _svf.VrsOut("SCHOOL_LOGO", logoFile.getAbsolutePath());
            }
            
            _svf.VrsOut("ITEM_TOTAL5", "全教科");

            final String barLegendImage;
            final String msg;
            if (param.isGakunen()) {
                barLegendImage = BAR_CHART_LEGEND1;
                msg = "学年";
            } else if (param.isCoursegroup()) {
                barLegendImage = BAR_CHART_LEGEND3;
                msg = "グループ";
            } else {
                barLegendImage = BAR_CHART_LEGEND2;
                msg = "コース";
            }

            _svf.VrsOut("CLASS_NUM", "3教科");
            _svf.VrsOut("ITEM_AVG", msg + "平均");
            _svf.VrsOut("ITEM_RANK", msg + "順位");

            // 画像
            _svf.VrsOut("BAR_LEGEND", _param._imagePath + "/" + barLegendImage);

            _svf.VrsOut("BAR_TITLE", "今回の各科目結果");
        }

        /**
         * 成績部分の印刷
         * @param param パラメータ
         * @param student 生徒
         */
        public void printRecord(final Param param, final Student student) {

            // 3教科 & 5教科
            printRecordRec9(student);

            // グラフ印字(サブフォームよりも先に印字)
            printBarGraph(student);

            // 成績
            print(student);

            _svf.VrPrint();
        }
        
        public void print(final Student student) {

            int i = 0;
            for (final Iterator it = student._data._subclasses.values().iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();

//                if ("90".equals(subClass.getClassCd())) {
//                    continue;
//                }

//                final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//                final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//                if (!hasSubclass5 && !hasSubclass3) {
//                    continue;
//                }
//                if (attendSubclasses.contains(subClass)) {
//                    continue; // 元科目は非表示
//                }

                printRecord(student, subClass);

                _svf.VrEndRecord();
                if (++i >= TABLE_SUBCLASS_MAX) {
                    break;
                }
            }
            for (int j = i; j < 8; j++) {
                _svf.VrsOut("CLASS", "　　　 　 " + String.valueOf(j));
                _svf.VrEndRecord();
            }
        }

        private void printRecord(
                final Student student,
                final SubClass subClass
        ) {
            // 教科
            final Class clazz = (Class) _param._classes.get(subClass.getClassCd());
            _svf.VrsOut("CLASS", (null != clazz) ? clazz._name : "なし");

            // 科目
            _svf.VrsOut("SUBCLASS", subClass._name);

            final Record record = (Record) student._data._record.get(subClass);
            if (null == record) {
                return;
            }

            if (record.isUnderScore() && !_param._isKumamoto) {
                _svf.VrsOut("SCORE", "(" + record.getScore() + ")");
            } else {
                _svf.VrsOut("SCORE", record.getScore());
            }
            if (!_param._rankNotPrint) {
                _svf.VrsOut("RANK",      record.getRank());
            }
            if ("1".equals(_param._deviationPrint)) {
                _svf.VrsOut("DEVIATION", record.getDeviation());
            }

            final String key;
            if (_param.isGakunen()) {
                key = subClass._code;
            } else if (_param.isCoursegroup()) {
                key = subClass._code + student._regd.coursegroupKey();
            } else {
                key = subClass._code + student._regd.courseKey();
            }
            final AverageDat avgDat = (AverageDat) _param._thisExam._averageDat.get(key);
            if (null != avgDat) {
                _svf.VrsOut("AVERAGE",  avgDat.getAvgStr());
                _svf.VrsOut("MAX_SCORE", avgDat._highScore.toString());
                _svf.VrsOut("EXAMINEE", avgDat._count.toString());    // 受験者数
            }
        }

        private void printRecordRec9(final Student student) {

            final String subclassCd = ALL3;
            final Record rec = (Record) student._data._recordOther.get(subclassCd);
            if (null != rec) {
                _svf.VrsOut("TOTAL_SCORE",     rec.getScore());
                if (!_param._rankNotPrint) {
                    _svf.VrsOut("TOTAL_RANK",      rec.getRank());
                }
                if ("1".equals(_param._deviationPrint)) {
                    _svf.VrsOut("TOTAL_DEVIATION", rec.getDeviation());
                }
            }
            final String key;
            if (_param.isGakunen()) {
                key = subclassCd;
            } else if (_param.isCoursegroup()) {
                key = subclassCd + student._regd.coursegroupKey();
            } else {
                key = subclassCd + student._regd.courseKey();
            }
            final AverageDat avg = (AverageDat) _param._thisExam._averageDatOther.get(key);
            if (null != avg) {
                _svf.VrsOut("TOTAL_AVERAGE",  avg.getAvgStr());
                _svf.VrsOut("TOTAL_MAX_SCORE", avg._highScore.toString());
                _svf.VrsOut("TOTAL_EXAMINEE", avg._count.toString());
            }
        }

        public void printBarGraph(final Student student) {

            // グラフ用のデータ作成
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            final List minMaxLows = new ArrayList();
            final List minMaxHighes = new ArrayList();
            final List minMaxCategoryKeys = new ArrayList();
            int i = 0;
            for (final Iterator it = student._data._record.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
//                if (attendSubclasses.contains(record._subClass)) {
//                    continue; // 元科目は非表示
//                }
                scoreDataset.addValue(record._graphScore, "本人の得点", record._subClass._abbv);
                final String key;
                if (_param.isGakunen()) {
                    key = record._subClass._code;
                } else if (_param.isCoursegroup()) {
                    key = record._subClass._code + student._regd.coursegroupKey();
                } else {
                    key = record._subClass._code + student._regd.courseKey();
                }
                final AverageDat avgDat = (AverageDat) _param._thisExam._averageDat.get(key);
                final BigDecimal graphAvg;
                if (null == avgDat) {
                    graphAvg = null;
                } else {
                    graphAvg = avgDat._graphAvg;
                }
                minMaxHighes.add(avgDat._highScore);
                minMaxLows.add(avgDat._lowScore);
                minMaxCategoryKeys.add(record._subClass._abbv);

                final String msg = _param.isGakunen() ? "学年" : _param.isCoursegroup() ? "グループ" : "コース";
                avgDataset.addValue(graphAvg, msg + "平均点", record._subClass._abbv);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + "(" + record._graphScore + ")" + ((avgDat == null) ? "" : ", 平均=" + avgDat._avg + "(" + avgDat._graphAvg + ")"));
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }
            
            final DefaultIntervalCategoryDataset minMaxDataset = createIntervalDataset(minMaxLows, minMaxHighes, minMaxCategoryKeys);

            try {
                // チャート作成
                final JFreeChart chart = createBarChart(scoreDataset, avgDataset, minMaxDataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 2456, 1128);
                _graphFiles.add(outputFile);

                // グラフの出力
                _svf.VrsOut("BAR_LABEL", "得点");
                _svf.VrsOut("BAR", outputFile.toString());
            } catch (Throwable t) {
                log.fatal("error or exception!", t);
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

        private JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset,
                final DefaultIntervalCategoryDataset minMaxDataset
        ) {
            final CategoryPlot plot = new CategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            final CategoryAxis axis = new CategoryAxis();
            plot.setDomainAxis(axis);
            plot.getDomainAxis().setTickLabelFont(new Font("TimesRoman", Font.BOLD, 12));
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            final NumberAxis numberAxis = new NumberAxis();
            numberAxis.setTickUnit(new NumberTickUnit(50));
            numberAxis.setTickLabelsVisible(true);
            numberAxis.setRange(0, 100.0);
            numberAxis.setTickLabelFont(new Font("TimesRoman", Font.PLAIN, 10));
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

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }
            _svf.VrQuit();
        }
    }

    private class Regd {
        private final String _grade;
        private final String _hrClass;
        private final String _attendNo;
        private final String _hrName;

        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final String _coursegroupCd;
        
        Regd(
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String coursegroupCd
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _coursegroupCd = coursegroupCd;
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String coursegroupKey() {
            return "0" + _coursegroupCd + "0000";
        }
        
        public String attendNoStr() {
            if (!StringUtils.isNumeric(_attendNo)) {
                return "";
            }
            return String.valueOf(Integer.parseInt(_attendNo)) + "番";
        }
        
        public int compare(final Regd regd) {
            int rtn;
            rtn = this._grade.compareTo(regd._grade);
            if (0 != rtn) {
                return rtn;
            }
            rtn = this._hrClass.compareTo(regd._hrClass);
            if (0 != rtn) {
                return rtn;
            }
            return this._attendNo.compareTo(regd._attendNo);
        }
    }
    
    private class ExamData {
        
        /** 模試データにある科目. */
        private final Map _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _record = new TreeMap();

        /** 元科目。 */
        private final Set _attendSubclasses = new TreeSet();

        /** 成績データ。3教科,5教科用 */
        private final Map _recordOther = new HashMap();
    }

    private class Student implements Comparable {
        private final String _schregno;
        
        private final Regd _regd;

        private final String _name;
        
        private final ExamData _data = new ExamData();
        
        private Student(
                final String schregno,
                final Regd regd,
                final String name
        ) {
            _schregno = schregno;
            _regd = regd;
            _name = name;
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        public int compareTo(Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            return _regd.compare(that._regd);
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

        public SubClass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public SubClass(final String code) {
            this(code, "xxx", "yyy");
        }

        public String getClassCd() {
            return _code.substring(0, 2);
        }

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
        private final SubClass _subClass;
        private final Integer _score;
        private final String _scoreDi;
        private final Integer _graphScore;
        private final BigDecimal _avg;
        private final Integer _rank;
        private final BigDecimal _deviation;
        private final Integer _passScore;

        private Record(
                final SubClass subClass,
                final Integer score,
                final String scoreDi,
                final Integer graphScore,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation,
                final Integer passScore
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
            _graphScore = graphScore;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
            _passScore = passScore;
        }

        public String getAvg() {
            return null == _avg ? "" : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getScore() {
            return null == _score ? (null == _scoreDi ? "" : _scoreDi) : _score.toString();
        }

        public String getGraphScore() {
            return null == _graphScore ? "" : _graphScore.toString();
        }

        public String getRank() {
            return null == _rank ? "" : _rank.toString();
        }

        public String getDeviation() {
            return null == _deviation ? "" : _deviation.toString();
        }

        public String getPassScore() {
            return null == _passScore ? "" : _passScore.toString();
        }

        public boolean isUnderScore() {
            final String score = getScore();
            final String passScore = getPassScore();
            if (StringUtils.isEmpty(score) || !StringUtils.isNumeric(score)) {
                return false;
            }
            if (StringUtils.isEmpty(passScore) || !StringUtils.isNumeric(passScore)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            final int pass = Integer.parseInt(passScore);
            return val < pass;
        }

        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }

    private class AverageDat {
        private final SubClass _subClass;
        private final BigDecimal _avg;
        private final BigDecimal _graphAvg;
        private final BigDecimal _stddev;
        private final Integer _highScore;
        private final Integer _lowScore;
        private final Integer _count;
        private final String _coursecd;
        private final String _majorcd;
        private final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal graphAvg,
                final BigDecimal stddev,
                final Integer highScore,
                final Integer lowScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _graphAvg = graphAvg;
            _stddev = stddev;
            _highScore = highScore;
            _lowScore = lowScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        public String getAvgStr() {
            return _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getStdDevStr() {
            return _stddev.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
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
} // KNJH563A

// eof
