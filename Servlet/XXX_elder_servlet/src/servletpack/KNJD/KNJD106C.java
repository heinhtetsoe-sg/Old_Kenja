// kanji=漢字
/*
 * $Id: 021ef42e800bbdc01e444330d60d1cafed725f02 $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

import jp.co.alp.kenja.common.dao.SQLUtils;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: 021ef42e800bbdc01e444330d60d1cafed725f02 $
 */
public class KNJD106C {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD106C.class);

    /** レーダーチャート凡例画像. */
    private static final String RADER_CHART_LEGEND = "RaderChartLegend.png";
    /** 棒グラフ凡例画像1(学年). */
    private static final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
    /** 棒グラフ凡例画像2(コース). */
    private static final String BAR_CHART_LEGEND2 = "BarChartLegendCourse.png";

    /** 成績表の最大科目数. */
    private static final int TABLE_SUBCLASS_MAX = 20;
    /** 棒グラフの最大科目数. */
    private static final int BAR_GRAPH_MAX_ITEM = 10;
    /** 生徒氏名のフォント切替の境目文字数. */
    private static final int STUDENT_NAME_DELIMTER_COUNT = 10;

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 全教科科目コード。 */
    private static final String ALL9 = "999999";

    /** フォーム。 */
    private static final String FORM_FILE1 = "KNJD106C.frm";
    /** 得点分布票表示用フォーム。 */
    private static final String FORM_FILE2 = "KNJD106C_2.frm";

    private Param _param;
    private boolean _hasData;
    private Form _form;
    private DB2UDB _db2;

    /** 模試科目マスタ。 */
    private Map _subClasses;

    /** 模試成績平均データ。 */
    private Map _averageDat = new HashMap();
    /** 模試成績平均データ。3教科,5教科用 */
    private Map _averageDatOther = new HashMap();
    /** 度数分布データ。*/
    private Map _scoreDistributions = new HashMap();

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
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
        final String[] schregnos = _param.getScregnos(_db2);
        final List students = createStudents(schregnos);
        _hasData = students.size() > 0;

        // 成績のデータを読む
        log.error("★成績関連の読込み");
        loadSubClasses(_db2, students);
        loadAverageDat(_db2, _averageDat, _averageDatOther);
        loadRecord(_db2, students);
        loadRecordOther(_db2, students);

        // 印刷する
        log.error("★印刷");
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug("☆" + student + ", 科目の数=" + student._subclasses.size() + ", コースキー=" + student.courseKey());
            log.debug("今回の成績: " + student._record.values());

            _param.setSubclasses(student);

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

    private void loadSubClasses(final DB2UDB db2, final List students) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlSubClasses());
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) _subClasses.get(subClasscd);
                    if (null != subClass) {
                        student._subclasses.put(subClasscd, subClass);
                    }
                }
            }
        } catch (final SQLException e) {
            log.fatal("模試データにある科目の取得でエラー");
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private String sqlSubClasses() {
        final String rtn;
        rtn = "select"
                + "  distinct T1.MOCK_SUBCLASS_CD as SUBCLASSCD"
                + " from"
                + "  MOCK_DAT T1"
                + " where"
                + "  T1.YEAR = '" + _param._year + "' and"
                + "  T1.MOCKCD = '" + _param._mockCd + "' and"
                + "  T1.SCHREGNO = ? "
                ;
        log.debug("模試データにある科目のSQL=" + rtn);
        return rtn;
    }
    
    private String subclassKey(final String subclasscd, final String courseKey) {
        final String key;
        if (_param._isGakunen) {
            key = subclasscd;
        } else {
            key = subclasscd + courseKey;
        }
        return key;
    }
    
    private String getRankField() {
        if (_param._isGakunen) {
            return "grade_rank";
        }
        return "course_rank";
    }
    
    private String getDeviationField() {
        if (_param._isGakunen) {
            return "grade_deviation";
        }
        return "course_deviation";
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
            final Map outAvgDat,
            final Map outAvgDatOther
    ) throws SQLException {
        final String selectFrom;
        selectFrom = "SELECT"
            + "  mock_subclass_cd as subclasscd,"
            + "  avg_kansan as avg,"
            + "  highscore,"
            + "  avg_kansan as graphAvg,"
            + "  count,"
            + "  coursecd,"
            + "  majorcd,"
            + "  coursecode"
            + " FROM"
            + "  mock_average_dat"
            ;
        final String where;
        if (_param._isGakunen) {
            where = " WHERE"
                + "    year='" + _param._year + "' AND"
                + "    mockcd='" + _param._mockCd + "' AND"
                + "    avg_div='1' AND"
                + "    grade='" + _param._grade + "' AND"
                + "    hr_class='000' AND"
                + "    coursecd='0' AND"
                + "    majorcd='000' AND"
                + "    coursecode='0000'"
                ;
        } else {
            where = " WHERE"
                + "    year='" + _param._year + "' AND"
                + "    mockcd='" + _param._mockCd + "' AND"
                + "    avg_div='3' AND"
                + "    grade='" + _param._grade + "' AND"
                + "    hr_class='000'"
                ;
        }
        ResultSet rs = null;
        try {
            final String sql = selectFrom + where;
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                final SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("模試成績平均データが模試科目マスタに無い!:" + subclasscd);
                }
                final BigDecimal avg = rs.getBigDecimal("avg");
                final BigDecimal graphAvg = rs.getBigDecimal("graphAvg");
                final Integer count = KNJServletUtils.getInteger(rs, "count");
                final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                final String key = subclassKey(subclasscd, coursecd + majorcd + coursecode);
                if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    final AverageDat avgDat = new AverageDat(subClass0, avg, graphAvg, highScore, count, coursecd, majorcd, coursecode);
                    outAvgDatOther.put(key, avgDat);
                } else {
                    final AverageDat avgDat = new AverageDat(subClass, avg, graphAvg, highScore, count, coursecd, majorcd, coursecode);
                    outAvgDat.put(key, avgDat);
                }
            }
        } catch (final SQLException e) {
            log.warn("模試成績平均データの取得でエラー");
            throw e;
        } finally {
            DbUtils.closeQuietly(rs);
        }
        log.debug("模試コード=" + _param._mockCd + " の模試成績平均データの件数=" + outAvgDat.size());
    }

    /**
     * 生徒に成績データを関連付ける。
     * @param db2 DB
     * @param kindCd テスト種別コード
     * @param itemCd テスト項目コード
     * @param students 生徒たち
     * @param isBefore 前回か?
     */
    private void loadRecord(
            final DB2UDB db2,
            final List students
    ) {
        String sql;
        /* 通常の成績 */
        sql = "SELECT"
            + "  T1.mock_subclass_cd as subclasscd,"
            + "  T1.score,"
            + "  T1.score_di,"
            + "  T2.score as graphScore,"
            + "  T3.grade_rank,"
            + "  T3.grade_deviation,"
            + "  T3.course_rank,"
            + "  T3.course_deviation,"
            + "  T4.ASSESSLEVEL,"
            + "  T5.PASS_SCORE"
            + " FROM mock_dat T1 "
            + " LEFT JOIN mock_rank_dat T3 ON "
            + "     T3.year = T1.year AND "
            + "     T3.mockcd = T1.mockcd AND "
            + "     T3.mock_subclass_cd = T1.mock_subclass_cd AND "
            + "     T3.schregno = T1.schregno "
            + " LEFT JOIN mock_rank_dat T2 ON "
            + "     T1.year = T2.year AND "
            + "     T1.mockcd = T2.mockcd AND "
            + "     T1.mock_subclass_cd = T2.mock_subclass_cd AND "
            + "     T1.schregno = T2.schregno AND "
            + "     T2.mockdiv = '3' "
            + " LEFT JOIN MOCK_ASSESS_LEVEL_MST T4 ON  T4.YEAR = T1.YEAR "
            + "      AND T4.MOCKCD = T1.MOCKCD "
            + "      AND T4.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD "
            + "      AND T4.DIV = ? "
            + "      AND T4.GRADE = ? "
            + "      AND T4.HR_CLASS = ? "
            + "      AND T4.COURSECD = ? "
            + "      AND T4.MAJORCD = ? "
            + "      AND T4.COURSECODE = ? "
            + "      AND T1.SCORE BETWEEN T4.ASSESSLOW AND T4.ASSESSHIGH "
            + " LEFT JOIN MOCK_PERFECT_COURSE_DAT T5 ON T5.YEAR = T1.YEAR "
            + "     AND T5.COURSE_DIV = '0' "
            + "     AND T5.MOCKCD = T1.MOCKCD "
            + "     AND T5.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD "
            + "     AND T5.GRADE = CASE WHEN T5.DIV = '01' THEN '00' ELSE ? END  "
            + "     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = "
            + "       CASE WHEN T5.DIV = '01' OR T5.DIV = '02' THEN '00000000' ELSE ? END "
            + " WHERE"
            + "  T1.year='" + _param._year + "' AND"
            + "  T1.mockcd='" + _param._mockCd + "' AND"
            + "  T1.mock_subclass_cd NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
            + "  (T3.mockdiv is null or T3.mockdiv='" + _param._rankDiv + "') AND"
            + "  T1.schregno=?"
            ;
        loadRecord1(db2, students, sql);
        
        if ("2".equals(_param._formDiv)) {
            sql = "SELECT"
                + "  T3.grade,"
                + "  T3.coursecd || T3.majorcd || T3.coursecode as courseKey,"
                + "  T1.mock_subclass_cd as subclasscd,"
                + "  T1.score"
                + " FROM"
                + "  mock_rank_dat T1 "
                + "  INNER JOIN schreg_regd_dat T3 ON"
                + "    T1.year=T3.year AND"
                + "    '" + _param._semester + "' =T3.semester AND"
                + "    T1.schregno=T3.schregno"
                + " WHERE"
                + "  T1.year='" + _param._year + "' AND"
                + "  T1.mockcd='" + _param._mockCd + "' AND"
                + "  T1.mock_subclass_cd NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
                + "  (T1.mockdiv is null or T1.mockdiv='" + _param._rankDiv + "') AND"
                + "  T3.grade='" + _param._grade + "'"
                ;
            
            loadScoreDistribution(db2, students, sql);
        }
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
                int i = 0;
                //段階値（1:学年 3:コース）
                if (_param._isGakunen) {
                    ps.setString(++i, "1");
                    ps.setString(++i, student._grade);
                    ps.setString(++i, "000");
                    ps.setString(++i, "0");
                    ps.setString(++i, "000");
                    ps.setString(++i, "0000");
                } else {
                    ps.setString(++i, "3");
                    ps.setString(++i, student._grade);
                    ps.setString(++i, "000");
                    ps.setString(++i, student._courseCd);
                    ps.setString(++i, student._majorCd);
                    ps.setString(++i, student._courseCode);
                }
                //欠点（満点マスタの合格点）
                String course = student._courseCd + student._majorCd + student._courseCode;
                ps.setString(++i, student._grade);
                ps.setString(++i, course);
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
                    final Integer rank = KNJServletUtils.getInteger(rs, getRankField());
                    final BigDecimal deviation = rs.getBigDecimal(getDeviationField());
                    
                    final Integer assessLevel = KNJServletUtils.getInteger(rs, "ASSESSLEVEL");
                    final Integer passScore = KNJServletUtils.getInteger(rs, "PASS_SCORE");

                    final Record rec = new Record(subClass, score, scoreDi, graphScore, null, rank, deviation, assessLevel, passScore);
                    student._record.put(subClass, rec);
                }
            }
        } catch (final SQLException e) {
            log.error("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private void loadRecordOther(
            final DB2UDB db2,
            final List students
    ) {
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String sql;
            sql = "SELECT"
                + "  t1.mock_subclass_cd as subclasscd,"
                + "  t1.score,"
                + "  t2.avg,"
                + "  t1.grade_rank,"
                + "  t1.grade_deviation,"
                + "  t1.course_rank,"
                + "  t1.course_deviation"
                + " FROM mock_rank_dat T1 "
                + " LEFT JOIN mock_rank_dat T2 ON "
                + "     T1.year = T2.year AND "
                + "     T1.mockcd = T2.mockcd AND "
                + "     T1.mock_subclass_cd = T2.mock_subclass_cd AND "
                + "     T1.schregno = T2.schregno AND "
                + "     T2.mockdiv = '4' "
                + " WHERE"
                + "  t1.year='" + _param._year + "' AND"
                + "  t1.mockcd='" + _param._mockCd + "' AND"
                + "  t1.mock_subclass_cd IN ('" + ALL3 + "', '" + ALL5 + "') AND"
                + "  t1.mockdiv='" + _param._rankDiv + "' AND"
                + "  t1.schregno='" + student._schregno + "'"
                ;
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    final BigDecimal avg = rs.getBigDecimal("avg");
                    final Integer rank = KNJServletUtils.getInteger(rs, getRankField());
                    final BigDecimal deviation = rs.getBigDecimal(getDeviationField());

                    final SubClass subClass = new SubClass(subclasscd);
                    final Record rec = new Record(subClass, score, null, null, avg, rank, deviation, null, null);
                    student._recordOther.put(subClass._code, rec);
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー");
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }
        
        if ("2".equals(_param._formDiv)) {
            final String sql = "SELECT"
                + "  t1.mock_subclass_cd as subclasscd,"
                + "  t2.grade,"
                + "  t2.coursecd || t2.majorcd || t2.coursecode as courseKey,"
                + "  t3.avg as score"
                + " FROM mock_rank_dat t1"
                + " inner join schreg_regd_dat t2 on "
                + "  t1.year=t2.year AND"
                + "  '" + _param._semester + "' =t2.semester AND"
                + "  t1.schregno=t2.schregno"
                + " LEFT JOIN mock_rank_dat T3 ON "
                + "     T1.year = T3.year AND "
                + "     T1.mockcd = T3.mockcd AND "
                + "     T1.mock_subclass_cd = T3.mock_subclass_cd AND "
                + "     T1.schregno = T3.schregno AND "
                + "     T3.mockdiv = '4' "
                + " WHERE"
                + "  t1.year='" + _param._year + "' AND"
                + "  t1.mockcd='" + _param._mockCd + "' AND"
                + "  t1.mock_subclass_cd IN ('" + ALL3 + "', '" + ALL5 + "') AND"
                + "  t1.mockdiv='" + _param._rankDiv + "' AND"
                + "  t2.grade='" + _param._grade + "' AND"
                + "  t3.avg IS NOT NULL "
                ;
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Double score = KNJServletUtils.getDouble(rs, "score");

                    final String distKey = getDistKey(rs.getString("grade"), rs.getString("courseKey"));
                    final SubClass subClass = new SubClass(subclasscd);
                    
                    if (!_scoreDistributions.containsKey(distKey)) {
                        _scoreDistributions.put(distKey, new ScoreDistribution(distKey));
                    }

                    ScoreDistribution dist = (ScoreDistribution) _scoreDistributions.get(distKey);
                    dist.add(subClass, new Integer(score.intValue()));
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }
    }
    
    
    private String getDistKey(final String grade, final String courseKey) {
        final String key;
        if (_param._isGakunen) {
            key = grade;
        } else {
            key = grade + courseKey;
        }
        return key;
    }
    
    private void loadScoreDistribution(
            final DB2UDB db2,
            final List students,
            final String sql
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                    continue;
                }
                final Integer score = KNJServletUtils.getInteger(rs, "score");
                if (score == null) {
                    continue;
                }
                final String key = getDistKey(rs.getString("grade"), rs.getString("courseKey"));
                
                if (!_scoreDistributions.containsKey(key)) {
                    _scoreDistributions.put(key, new ScoreDistribution(key));
                }
                final ScoreDistribution sd = (ScoreDistribution) _scoreDistributions.get(key);
                sd.add(subClass, score);
            }
            
            if (log.isDebugEnabled()) {
                for (Iterator it = _scoreDistributions.keySet().iterator(); it.hasNext();) {
                    String key = (String) it.next();
                    ScoreDistribution sd = (ScoreDistribution) _scoreDistributions.get(key);
                    log.debug(" key = " + key + ", distribution = " + sd);
                }
            }
            
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private List createStudents(final String[] schregnos) throws SQLException {
        final List rtn = new LinkedList();
        final String sql = studentsSQL(schregnos);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
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
                final String name = rs.getString("name");
                final String guardZipCd = rs.getString("guard_zipcd");
                final String guardAddr1 = rs.getString("guard_addr1");
                final String guardAddr2 = rs.getString("guard_addr2");
                final String guardName = rs.getString("guard_name");
                final String guarantorZipCd = rs.getString("guarantor_zipcd");
                final String guarantorAddr1 = rs.getString("guarantor_addr1");
                final String guarantorAddr2 = rs.getString("guarantor_addr2");
                final String guarantorName = rs.getString("guarantor_name");
                final String sendAddressZipCd = rs.getString("send_zipcd");
                final String sendAddressAddr1 = rs.getString("send_addr1");
                final String sendAddressAddr2 = rs.getString("send_addr2");
                final String sendAddressName = rs.getString("send_name");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        attendno,
                        hrName,
                        coursecd,
                        majorcd,
                        coursecode,
                        name,
                        guardZipCd,
                        guardAddr1,
                        guardAddr2,
                        guardName,
                        guarantorZipCd,
                        guarantorAddr1,
                        guarantorAddr2,
                        guarantorName,
                        sendAddressZipCd,
                        sendAddressAddr1,
                        sendAddressAddr2,
                        sendAddressName
                        
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

    private String studentsSQL(final String[] selected) {
        final String students = SQLUtils.whereIn(true, selected);

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
            + "  t1.name,"
            + "  t2.guard_zipcd,"
            + "  t2.guard_addr1,"
            + "  t2.guard_addr2,"
            + "  t2.guard_name,"
            + "  t2.guarantor_zipcd,"
            + "  t2.guarantor_addr1,"
            + "  t2.guarantor_addr2,"
            + "  t2.guarantor_name,"
            + "  t3.send_zipcd,"
            + "  t3.send_addr1,"
            + "  t3.send_addr2,"
            + "  t3.send_name"
            + " FROM"
            + "  v_schreg_info t1 "
            + " LEFT JOIN guardian_dat t2 ON t1.schregno=t2.schregno"
            + " LEFT JOIN schreg_send_address_dat t3 ON t1.schregno=t3.schregno and t3.div='1' "
            + " WHERE"
            + "  t1.year='" + _param._year + "' AND"
            + "  t1.semester='" + _param._semester + "' AND"
            + "  t1.schregno IN " + students
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

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
//        final String testCd = request.getParameter("TESTCD");
        final String mockCd = request.getParameter("MOCKCD");
        final String div = request.getParameter("CATEGORY_IS_CLASS");
        final String[] values = request.getParameterValues("CATEGORY_SELECTED");
        final String useAddress = request.getParameter("USE_ADDRESS");
        final String submitDate = request.getParameter("SUBMIT_DATE");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String grade = request.getParameter("GRADE");
        final String groupDiv = request.getParameter("GROUP_DIV");// 1=学年, 2=コース
        final String deviationPrint = request.getParameter("DEVIATION_PRINT");
        final String imagePath = request.getParameter("IMAGE_PATH");
        final String rankNotPrint = request.getParameter("JUNI_PRINT");
        final String juni = request.getParameter("JUNI");
        final String rankDivTemp = request.getParameter("useKnjd106cJuni" + juni);
        final String rankDiv = (rankDivTemp == null) ? juni : rankDivTemp;
        final String ketten = (null == request.getParameter("KETTEN") || "".equals(request.getParameter("KETTEN"))) ? null : request.getParameter("KETTEN");
        final String assessLevelPrint =  request.getParameter("ASSESS_LEVEL_PRINT");
        final String formDiv = request.getParameter("FORM_DIV");

        final Param param = new Param(
                year,
                semester,
                mockCd,
                div,
                values,
                useAddress,
                submitDate,
                loginDate,
                grade,
                groupDiv,
                deviationPrint,
                imagePath,
                rankNotPrint,
                rankDiv,
                ketten,
                assessLevelPrint,
                formDiv
        );
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
        return (int) (pixel * 1.3);
    }

    private class Param {
        private final String _year;
        private final String _semester;
        private final String _mockCd;
        private final String _grade;

        /** 学年対象か? false ならコース対象。 */
        private final boolean _isGakunen;
        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;
        /** 中高一貫フラグ。 */
        private boolean _isIkkan;

        /** [クラス指定 or 生徒指定]の値。 */
        private final String _div;
        /** クラス or 生徒。 */
        private final String[] _values;

        private final String _useAddress;
        private final String _submitDate;
        private final String _loginDate;

        /** クラス指定 or 生徒指定。 */
        private final boolean _isClassMode;

        /** 西暦/和暦フラグ。 */
        private boolean _isWareki;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 偏差値を印字するか? */
        private final boolean _deviationPrint;
        /** 順位を印字しないか? */
        private final boolean _rankNotPrint;
        /** 順位の基準点 1:総合点,2:平均点,3:偏差値 */
        private final String _rankDiv;

        private final Map _staffs = new HashMap();
        private String _examName;

        /** 模試文面データ。全体評。 */
        private Map _document = new HashMap();

        /** 教科マスタ。 */
        private Map _classes;
        private final String _imagePath;

        private final Map _subclassGroup3 = new HashMap();
        private final Map _subclassGroup5 = new HashMap();
        private final MultiMap _subclassGroupDat3 = new MultiHashMap();
        private final MultiMap _subclassGroupDat5 = new MultiHashMap();

        /** レーダーチャートの科目. */
        private List _fiveSubclass = new ArrayList();
        private List _threeSubclass = new ArrayList();

        private final List _classOrder = new ArrayList();
        
        private boolean _isKumamoto;
        
        private final int _ketten;
        
        /** 段階値出力するか？ */
        private final boolean _isAssessLevelPrint;
        
        /** 得点分布表を表示するか */
        private final String _formDiv;

        public Param(
                final String year,
                final String semester,
                final String mockCd,
                final String div,
                final String[] values,
                final String useAddress,
                final String submitDate,
                final String loginDate,
                final String grade,
                final String groupDiv,
                final String deviationPrint,
                final String imagePath,
                final String rankNotPrint,
                final String rankDiv,
                final String ketten,
                final String assessLevelPrint,
                final String formDiv
        ) {
            _year = year;
            _semester = semester;
            _mockCd = mockCd;
            _div = div;
            _values = values;
            _useAddress = useAddress;
            _submitDate = submitDate;
            _loginDate = loginDate;
            _grade = grade;

            _isClassMode = "1".equals(div) ? true : false;

            _isAssessLevelPrint = "1".equals(assessLevelPrint);
            _isGakunen = "1".equals(groupDiv) ? true : false;
            _deviationPrint = "1".equals(deviationPrint);
            _rankNotPrint = (_isAssessLevelPrint) ? true : "1".equals(rankNotPrint);
            _rankDiv = rankDiv;

            _imagePath = imagePath;

            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            
            _ketten = StringUtils.isNumeric(ketten) ? Integer.parseInt(ketten) : 0;
            _formDiv = formDiv;
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
            loadWarekiFlg(db2);
            loadCertifSchool(db2);
            loadRegdHdat(db2);
            loadExam(db2);
            loadDocumentDat(db2);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);

            loadClassOrder(db2);
            
            // mock_subclass_group_dat を読込んで _fiveSubclass や _threeSubclass の箱に入れる
            loadSubclassGroup(db2);
            loadSubclassGroupDat(db2);
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
                    + "   MOCK_SUBCLASS_CD as SUBCLASSCD,"
                    + "   SUBCLASS_NAME as NAME,"
                    + "   SUBCLASS_ABBV as SUBCLASSABBV"
                    + " from MOCK_SUBCLASS_MST"
                    + " order by"
                    + "   MOCK_SUBCLASS_CD"
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

        private void loadDocumentDat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT"
                + "  grade,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode,"
                + "  footnote"
                + " FROM"
                + "  mock_document_dat"
                + " WHERE"
                + "  year='" + _param._year + "' AND"
                + "  mockcd='" + _param._mockCd + "' AND"
                + "  grade='" + _param._grade + "' AND"
                + "  mock_subclass_cd='999999'"
                ;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("grade");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");
                    final String footnote = rs.getString("footnote");
                    _document.put(grade + coursecd + majorcd + coursecode, footnote);
                }
            } catch (final SQLException e) {
                log.error("模試文面データ取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadExam(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT mockname1 FROM mock_mst WHERE mockcd='" + _mockCd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _examName = rs.getString("mockname1");
                }
            } catch (final SQLException e) {
                log.error("mock_mst の取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadRegdHdat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year=t2.year AND t1.tr_cd1=t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("staffname");
                    _staffs.put(code, name);
                }
            } catch (final SQLException e) {
                log.warn("担任名の取得でエラー");
            } finally {
                DbUtils.closeQuietly(rs);
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

        private void loadClassOrder(final DB2UDB db2) {
            final String field1 = _isJunior ? "name1" : "name2";
            final String field2 = _isJunior ? "namespare1" : "namespare2";
            final String sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
                + " WHERE year='" + _param._year + "' AND namecd1='D014'"
                + " ORDER BY " + field2
                ;

            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _classOrder.add(rs.getString("classcd"));
                }
            } catch (final SQLException e) {
                log.error("教科表示順取得エラー。");
            }
            log.debug("教科表示順=" + _classOrder);
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

                final int gradeVal = Integer.parseInt(_grade);
                _isJunior = (gradeVal <= 3 && _isIkkan) ? true : false;
                _isHigh = !_isJunior;
            } catch (final SQLException e) {
                log.error("中高一貫フラグ取得エラー。");
            }
            log.debug("中高一貫フラグ=" + _isIkkan);
        }

        private void loadWarekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT name1 FROM v_name_mst WHERE year='" + _param._year + "' AND namecd1='Z012'");
                rs = ps.executeQuery();
                rs.next();
                final String name1 = rs.getString("name1");
                _isWareki = "2".equals(name1) ? true : false;
            } catch (final SQLException e) {
                log.error("西暦/和暦フラグ取得エラー。");
            }
            log.debug("和暦フラグ=" + _isWareki);
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

        public String getZipCd(final Student student) {
            if (is住所印刷なし()) {
                return null;
            }
            return useGuard() ? student._guardZipCd : useGuarantor() ? student._guarantorZipCd : student._sendAddressZipCd;
        }

        public String getAddr1(final Student student) {
            if (is住所印刷なし()) {
                return null;
            }
            return useGuard() ? student._guardAddr1 : useGuarantor() ? student._guarantorAddr1 : student._sendAddressAddr1;
        }

        public String getAddr2(final Student student) {
            if (is住所印刷なし()) {
                return null;
            }
            return useGuard() ? student._guardAddr2 : useGuarantor() ? student._guarantorAddr2 : student._sendAddressAddr2;
        }

        public String getAddressee(final Student student) {
            if (is住所印刷なし()) {
                return null;
            }
            return useGuard() ? student._guardName : useGuarantor() ? student._guarantorName : student._sendAddressName;
        }

        private boolean is住所印刷なし() {
            return "1".equals(_useAddress);
        }

        /**
         * 保護者を使うか?
         * @return true なら保護者
         */
        private boolean useGuard() {
            return "2".equals(_useAddress);
        }

        /**
         * 負担者を使うか?
         * @return true なら負担者
         */
        private boolean useGuarantor() {
            return "3".equals(_useAddress);
        }

        public String getWholeRemark(final Student student) {
            final String rtn = (String) _document.get(student._grade + student._courseCd + student._majorCd + student._courseCode);
            return rtn;
        }

        public boolean isUnderScore(final String score) {
            if (StringUtils.isEmpty(score) || !StringUtils.isNumeric(score)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            return val < _param._ketten;
        }

        String get3title(final String courseKey) {
            final String rtn = (String) _subclassGroup3.get(courseKey);
            return rtn;
        }

        String get5title(final String courseKey) {
            final String rtn = (String) _subclassGroup5.get(courseKey);
            return rtn;
        }

        public void setSubclasses(final Student student) {
            _fiveSubclass = (List) _subclassGroupDat5.get(student.courseKey());
            _threeSubclass = (List) _subclassGroupDat3.get(student.courseKey());
        }

        private void loadSubclassGroup(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = subclassGroupSQL();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String groupDiv = rs.getString("group_div");
                    final String courseCd = rs.getString("coursecd");
                    final String majorCd = rs.getString("majorcd");
                    final String courseCode = rs.getString("coursecode");
                    final String groupName = rs.getString("group_name");

                    final String key = courseCd + majorCd + courseCode;
                    if ("3".equals(groupDiv)) {
                        _subclassGroup3.put(key, groupName);
                    } else {
                        _subclassGroup5.put(key, groupName);
                    }
                }
            } catch (final SQLException e) {
                log.error("mock_subclass_group_mst の取得エラー。");
            }

            log.debug("3教科の名称たち=" + _subclassGroup3);
            log.debug("5教科の名称たち=" + _subclassGroup5);
        }

        private String subclassGroupSQL() {
            return "SELECT"
                    + "  group_div,"
                    + "  coursecd,"
                    + "  majorcd,"
                    + "  coursecode,"
                    + "  group_name"
                    + " FROM"
                    + "  mock_subclass_group_mst"
                    + " WHERE"
                    + "  year='" + _year + "' AND"
                    + "  mockcd='" + _mockCd + "' AND"
                    + "  grade='" + _grade + "' AND"
                    + "  group_div in ('3', '5')"   // 3教科, 5教科
                    ;
        }

        private void loadSubclassGroupDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = subclassGroupDatSQL();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String groupDiv = rs.getString("group_div");
                    final String courseCd = rs.getString("coursecd");
                    final String majorCd = rs.getString("majorcd");
                    final String courseCode = rs.getString("coursecode");
                    final String subclassCd = rs.getString("subclasscd");

                    final String key = courseCd + majorCd + courseCode;
                    if ("3".equals(groupDiv)) {
                        _subclassGroupDat3.put(key, subclassCd);
                    } else {
                        _subclassGroupDat5.put(key, subclassCd);
                    }
                }
            } catch (final SQLException e) {
                log.error("mock_subclass_group_dat の取得エラー");
            }

            log.debug("3教科の科目CDたち=" + _subclassGroupDat3);
            log.debug("5教科の科目CDたち=" + _subclassGroupDat5);
        }

        private String subclassGroupDatSQL() {
            return "SELECT"
                    + "  group_div,"
                    + "  coursecd,"
                    + "  majorcd,"
                    + "  coursecode,"
                    + "  mock_subclass_cd as subclasscd"
                    + " FROM"
                    + "  mock_subclass_group_dat"
                    + " WHERE"
                    + "  year='" + _year + "' AND"
                    + "  mockcd='" + _mockCd + "' AND"
                    + "  grade='" + _grade + "' AND"
                    + "  group_div in ('3', '5')"   // 3教科, 5教科
                    ;
        }

    }

    private class Form {
        private Vrw32alp _svf;
        private final String _file;
        private final Param _param;

        /** メインの成績(サブフォーム). */
        private final MainRecTable _mainTable = new MainRecTable();

        public Form(final Param param, final HttpServletResponse response) throws IOException {
            _param = param;
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _file = "2".equals(param._formDiv) ? FORM_FILE2 : FORM_FILE1;
            log.debug("フォームファイル=" + _file);
        }

        public void resetForm() {
            final int sts = _svf.VrSetForm(_file, 4);
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }
        }

        public void printStatic(final Param param, final Student student) {
            _svf.VrsOut("SCHOOL_NAME", param._schoolName);

            final String staffName = (String) param._staffs.get(student._grade + student._hrClass);
            _svf.VrsOut("STAFFNAME", (param._remark2 == null ? "" : param._remark2) + (staffName == null ? "" : staffName));

            if (!param.is住所印刷なし()) {
                final String zipCd = param.getZipCd(student);
                if (zipCd != null && !"".equals(zipCd)) {
                    _svf.VrsOut("ZIPCD", "〒" + zipCd);
                }
                final String addr1 = param.getAddr1(student);
                final String addr2 = param.getAddr2(student);
                final String field = (null != addr1 && addr1.length() > 25) || (null != addr2 && addr2.length() > 25) ? "_3" : (null != addr1 && addr1.length() > 19) || (null != addr2 && addr2.length() > 19) ? "_2" : "";
                _svf.VrsOut("ADDR1" + field, addr1);
                _svf.VrsOut("ADDR2" + field, addr2);
                final String addressee = param.getAddressee(student);
                if (null != addressee) {
                    final String setAddressee = addressee + "  様";
                    final String field2 = setAddressee.length() > 13 ? "_2" : "";
                    _svf.VrsOut("ADDRESSEE" + field2, setAddressee);
                }
            }
        
            final String nendo;
            if (param._isWareki) {
                nendo = KenjaProperties.gengou(Integer.parseInt(_param._year));
            } else {
                nendo = _param._year;
            }
            _svf.VrsOut("NENDO", nendo + "年度　" + _param._examName);
        
            final int attendNo = Integer.parseInt(student._attendNo);
            _svf.VrsOut("HR_NAME", student._hrName + attendNo + "番");

            if (student._name.length() <= STUDENT_NAME_DELIMTER_COUNT) {  // 全角で規定文字数を超えたらフォントを変える
                _svf.VrsOut("NAME", student._name);
            } else {
                _svf.VrsOut("NAME_2", student._name);
            }

            // 全体評
            KNJServletUtils.printDetail(_svf, "WHOLE_REMARK", param.getWholeRemark(student), 45 * 2, 7);
            
            // 提出日
            try {
                final Calendar cal = KNJServletUtils.parseDate(param._submitDate.replace('/', '-'));
                final String month = (cal.get(Calendar.MONTH) + 1) + "月";
                final String day = cal.get(Calendar.DATE) + "日";
                _svf.VrsOut("DATE", month + day);
            } catch (final ParseException e) {
                log.error("提出日が解析できない");
                _svf.VrsOut("DATE", param._submitDate);
            }

            final String get3title = _param.get3title(student.courseKey());
            if (null != get3title) {
                _svf.VrsOut("ITEM_TOTAL3", get3title);
            }
            final String get5title = _param.get5title(student.courseKey());
            if (null != get5title) {
                _svf.VrsOut("ITEM_TOTAL5", get5title);
            }

            final String barLegendImage;
            final String msg;
            if (param._isGakunen) {
                barLegendImage = BAR_CHART_LEGEND1;
                msg = "学年";
            } else {
                barLegendImage = BAR_CHART_LEGEND2;
                msg = "コース";
            }

            _svf.VrsOut("ITEM_AVG", msg + "平均");
            _svf.VrsOut("ITEM_RANK", (_param._isAssessLevelPrint) ? "段階値" : msg + "順位");

            // 画像
            _svf.VrsOut("RADER_LEGEND", _param._imagePath + "/" + RADER_CHART_LEGEND);
            _svf.VrsOut("BAR_LEGEND", _param._imagePath + "/" + barLegendImage);

            _svf.VrsOut("BAR_TITLE", "得点グラフ");
            _svf.VrsOut("RADER_TITLE", "教科間バランス");
        }

        /**
         * 成績部分の印刷
         * @param param パラメータ
         * @param student 生徒
         */
        public void printRecord(final Param param, final Student student) {

            // 3教科 & 5教科
            _mainTable.printRec3and5(student._recordOther, _averageDatOther, student);

            // グラフ印字(サブフォームよりも先に印字)
            printBarGraph(student, _averageDat);
            if (!"2".equals(param._formDiv)) {
                printRadarGraph(student._record);
            }

            // 成績
            _mainTable.print(param._classes, student, _averageDat, student._record);

            _svf.VrPrint();
        }

        public void printBarGraph(final Student student, final Map averages) {
            // グラフ用のデータ作成
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            int i = 0;
            for (final Iterator it = student._record.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                scoreDataset.addValue(record._graphScore, "本人得点", record._subClass._abbv);
                final String key = subclassKey(record._subClass._code, student.courseKey());
                final AverageDat avgDat = (AverageDat) averages.get(key);
                final BigDecimal graphAvg = (null == avgDat) ? null : avgDat._graphAvg;

                final String msg = _param._isGakunen ? "学年" : "コース";
                avgDataset.addValue(graphAvg, msg + "平均点", record._subClass._abbv);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + "(" + record._graphScore + ")" + ((avgDat == null) ? "" : ", 平均=" + avgDat._avg + "(" + avgDat._graphAvg + ")"));
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }

            // チャート作成
            final JFreeChart chart = createBarChart(scoreDataset, avgDataset);

            // グラフのファイルを生成
            final int w = "2".equals(_param._formDiv) ? 2810 : 1940;
            final int h = "2".equals(_param._formDiv) ? 790 : 930;
            final File outputFile = graphImageFile(chart, w, h);
            _graphFiles.add(outputFile);

            // グラフの出力
            _svf.VrsOut("BAR_LABEL", "得点");
            _svf.VrsOut("BAR", outputFile.toString());
        }

        private JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset
        ) {
            final boolean createLegend = "2".equals(_param._formDiv);
            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, createLegend, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            if ("2".equals(_param._formDiv)) {
                final Font font = new Font("TimesRoman", Font.PLAIN, 15);
                plot.getDomainAxis().setTickLabelFont(font);
                plot.getRangeAxis().setTickLabelFont(font);
                final LegendTitle legend = chart.getLegend();
                legend.setPosition(RectangleEdge.RIGHT);
                legend.setItemFont(font);
            }

            // 追加する折れ線グラフの表示設定
            final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
            renderer2.setItemLabelsVisible(true);
            renderer2.setPaint(Color.gray);
            plot.setDataset(1, avgDataset);
            plot.setRenderer(1, renderer2);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            final NumberAxis numberAxis = new NumberAxis();
            numberAxis.setTickUnit(new NumberTickUnit(10));
            numberAxis.setTickLabelsVisible(true);
            numberAxis.setRange(0, 100.0);
            plot.setRangeAxis(numberAxis);

            final CategoryItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.darkGray);
            final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
            renderer.setItemLabelFont(itemLabelFont);
            renderer.setItemLabelsVisible(true);

            ((BarRenderer) renderer).setMaximumBarWidth(0.05);

            chart.setBackgroundPaint(Color.white);

            return chart;
        }

        private void printRadarGraph(final Map records) {
            // データ作成
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final Iterator it0 = _param._classOrder.iterator(); it0.hasNext();) {
                final String classCd = (String) it0.next();
                
                for (final Iterator it = records.values().iterator(); it.hasNext();) {
                    final Record record = (Record) it.next();
                    if (!classCd.equals(record._subClass._code)) {
                        continue;
                    }
                    setDataset(dataset, record);
                }
            }

            // チャート作成
            final JFreeChart chart = createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = graphImageFile(chart, 930, 822);
            _graphFiles.add(outputFile);

            // グラフの出力
            if (0 < dataset.getColumnCount()) {
                _svf.VrsOut("RADER", outputFile.toString());
            }
        }

        private void setDataset(final DefaultCategoryDataset dataset, final Record record) {
            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
            if (record._deviation == null) {
                return;
            }
//            final Class clazz = (Class) _param._classes.get(record._subClass.getClassCd());
//            final String name = (null == clazz) ? "???" : clazz._abbv;
//            final String name = (null == clazz) ? record._subClass._abbv : clazz._abbv;
            final String name = record._subClass._abbv;
            dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20
            
            dataset.addValue(50.0, "偏差値50", name);
        }

        private JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
            final SpiderWebPlot plot = new SpiderWebPlot(dataset);
            plot.setWebFilled(false);
            plot.setMaxValue(80.0);

            // 実データ
            plot.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
            plot.setSeriesOutlinePaint(0, Color.black);
            plot.setSeriesPaint(0, Color.black);

            // 偏差値50
            final BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {4.0f, 4.0f}, 0.0f);
            plot.setSeriesOutlineStroke(1, stroke);
            plot.setSeriesOutlinePaint(1, Color.darkGray);
            plot.setSeriesPaint(1, Color.darkGray);

            plot.setOutlinePaint(Color.white);

            final JFreeChart chart = new JFreeChart(null, new Font("TimesRoman", Font.PLAIN, 15), plot, false);
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

        /**
         * 成績サブフォーム。
         */
        private class MainRecTable {
            String getFClass() { return "CLASS"; }
            String getFSubClass() { return "SUBCLASS"; }
            String getFScore() { return "SCORE"; }
            String getFRank() { return "RANK"; }
            String getFDeviation() { return "DEVIATION"; }
            String getFAverage() { return "AVERAGE"; }
            String getFMaxScore() { return "MAX_SCORE"; }
            /** 受験者数 */
            String getFExaminee() { return "EXAMINEE"; }
            String getFSum_() { return "SUM"; }

            /*
             * 3教科
             */
            String getFScore3() { return "SCORE3"; }
            String getFRank3() { return "RANK3"; }
            String getFDeviation3() { return "DEVIATION3"; }
            String getFAverage3() { return "AVERAGE3"; }
            String getFMaxScore3() { return "MAX_SCORE3"; }
            String getFExaminee3() { return "EXAMINEE3"; }
            String getFAverage3_() { return "AVERAGE3_"; }

            /*
             * 5教科
             */
            String getFScore5() { return "SCORE5"; }
            String getFRank5() { return "RANK5"; }
            String getFDeviation5() { return "DEVIATION5"; }
            String getFAverage5() { return "AVERAGE5"; }
            String getFMaxScore5() { return "MAX_SCORE5"; }
            String getFExaminee5() { return "EXAMINEE5"; }
            String getFAverage5_() { return "AVERAGE5_"; }

            public void print(final Map classMst, final Student student, final Map averages, final Map records) {
                int i = 0;
                final ScoreDistribution dist = (ScoreDistribution) _scoreDistributions.get(getDistKey(student._grade, student.courseKey()));
                for (final Iterator it = student._subclasses.values().iterator(); it.hasNext();) {
                    final SubClass subClass = (SubClass) it.next();

//                    if ("90".equals(subClass.getClassCd())) {
//                        continue;
//                    }

//                    final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//                    final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//                    if (!hasSubclass5 && !hasSubclass3) {
//                        continue;
//                    }

                    final Record record = (Record) records.get(subClass);
                    final String key = subclassKey(subClass._code, student.courseKey());
                    final AverageDat avgDat = (AverageDat) averages.get(key);

                    printRecord(classMst, subClass, record, avgDat, dist);

                    _svf.VrEndRecord();
                    if (++i >= TABLE_SUBCLASS_MAX) {
                        break;
                    }
                }
                if (i == 0) {
                    _svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
                }
            }

            private void printRecord(
                    final Map classMst,
                    final SubClass subClass,
                    final Record record,
                    final AverageDat avgDat,
                    final ScoreDistribution dist
            ) {
                // 教科
                final Class clazz = (Class) classMst.get(subClass.getClassCd());
                _svf.VrsOut(getFClass(), (null != clazz) ? clazz._name : "なし");

                // 科目
                _svf.VrsOut(getFSubClass(), subClass._name);

                if (null == record) {
                    return;
                }

                final String score = record.getScore();
                if (_param.isUnderScore(score) && !_param._isKumamoto) {
                    _svf.VrsOut(getFScore(), "(" + score + ")");
                } else {
                    _svf.VrsOut(getFScore(), score);
                }
                if (!_param._rankNotPrint) {
                    _svf.VrsOut(getFRank(),      record.getRank());
                }
                if (_param._isAssessLevelPrint) {
                    _svf.VrsOut(getFRank(),      record.getAssessLevel());
                }
                if (_param._deviationPrint) {
                    _svf.VrsOut(getFDeviation(), record.getDeviation());
                }

                if (null != avgDat) {
                    _svf.VrsOut(getFAverage(),  avgDat.getAvgStr());
                    _svf.VrsOut(getFMaxScore(), avgDat._highScore.toString());
                    _svf.VrsOut(getFExaminee(), avgDat._count.toString());    // 受験者数
                }
                
                if ("2".equals(_param._formDiv) && null != dist) {
                    final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(subClass._code);

                    ScoreCount[] scoreCounts = ssd._scoreCounts;
                    int total = 0;
                    for (int i = 0; i < scoreCounts.length; i++) {
                        final Integer count = ssd.getCount(scoreCounts[i]._key);
                        _svf.VrsOut(getFSum_() + distSuf(i), count.toString());
                        // log.debug(" 得点分布 (" + subClass + "):" + scoreKeys[i] + " = " + count);
                        total += count.intValue();
                    }
                    // log.debug(" total = " + total);
                }
            }

            /**
             * 3,5教科の印字
             */
            public void printRec3and5(final Map recordOther, final Map avgDatOther, final Student student) {
                printRecordRec3(recordOther, avgDatOther, student);

                if (null != _param._subclassGroup5.get(student.courseKey())) {
                    printRecordRec5(recordOther, avgDatOther, student);
                }
            }

            private void printRecordRec3(final Map recordOther, final Map avgDatOther, Student student) {
                final Record rec3 = (Record) recordOther.get(ALL3);
                if (null != rec3) {
                    _svf.VrsOut(getFScore3(), rec3.getScore()  + "/" + rec3.getAvg());
                    if (!_param._rankNotPrint) {
                        _svf.VrsOut(getFRank3(), rec3.getRank());
                    }
                    if (_param._deviationPrint) {
                        _svf.VrsOut(getFDeviation3(), rec3.getDeviation());
                    }
                }
                final String key = subclassKey(ALL3, student.courseKey());
                final AverageDat avg3 = (AverageDat) avgDatOther.get(key);
                if (null != avg3) {
                    _svf.VrsOut(getFAverage3(),  avg3.getAvgStr());
                    _svf.VrsOut(getFMaxScore3(), avg3._highScore.toString());
                    _svf.VrsOut(getFExaminee3(), avg3._count.toString());
                }
                
                final ScoreDistribution dist = (ScoreDistribution) _scoreDistributions.get(getDistKey(student._grade, student.courseKey()));
                if ("2".equals(_param._formDiv) && null != dist) {
                    final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(ALL3);
                    if (ssd._added) {
                        ScoreCount[] scoreCounts = ssd._scoreCounts;
                        int total = 0;
                        for (int i = 0; i < scoreCounts.length; i++) {
                            final Integer count = ssd.getCount(scoreCounts[i]._key);
                            _svf.VrsOut(getFAverage3_() + distSuf(i), count.toString());
                            total += count.intValue();
                        }
                        // log.debug(" total = " + total);
                    }
                }
            }

            private void printRecordRec5(final Map recordOther, final Map avgDatOther, Student student) {
                final Record rec5 = (Record) recordOther.get(ALL5);
                if (null != rec5) {
                    _svf.VrsOut(getFScore5(),     rec5.getScore() + "/" + rec5.getAvg());
                    if (!_param._rankNotPrint) {
                        _svf.VrsOut(getFRank5(),      rec5.getRank());
                    }
                    if (_param._deviationPrint) {
                        _svf.VrsOut(getFDeviation5(), rec5.getDeviation());
                    }
                }
                final String key = subclassKey(ALL5, student.courseKey());
                final AverageDat avg5 = (AverageDat) avgDatOther.get(key);
                if (null != avg5) {
                    _svf.VrsOut(getFAverage5(),  avg5.getAvgStr());
                    _svf.VrsOut(getFMaxScore5(), avg5._highScore.toString());
                    _svf.VrsOut(getFExaminee5(), avg5._count.toString());
                }
                
                final ScoreDistribution dist = (ScoreDistribution) _scoreDistributions.get(getDistKey(student._grade, student.courseKey()));
                if ("2".equals(_param._formDiv) && null != dist) {
                    final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(ALL5);
                    if (ssd._added) {
                        ScoreCount[] scoreCounts = ssd._scoreCounts;
                        int total = 0;
                        for (int i = 0; i < scoreCounts.length; i++) {
                            final Integer count = ssd.getCount(scoreCounts[i]._key);
                            _svf.VrsOut(getFAverage5_() + distSuf(i), count.toString());
                            total += count.intValue();
                        }
                        // log.debug(" total = " + total);
                    }
                }
            }
            
            private int distSuf(final int i) {
                return (11 - 3) - i;
            }
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

        private final String _name;

        /*
         * 保護者情報
         */
        private final String _guardZipCd;
        private final String _guardAddr1;
        private final String _guardAddr2;
        /** 保護者氏名。 */
        private final String _guardName;

        /*
         * 負担者情報
         */
        private final String _guarantorZipCd;
        private final String _guarantorAddr1;
        private final String _guarantorAddr2;
        /** 負担者氏名。 */
        private final String _guarantorName;

        /*
         * 送付先情報
         */
        private final String _sendAddressZipCd;
        private final String _sendAddressAddr1;
        private final String _sendAddressAddr2;
        /** 送付先氏名。 */
        private final String _sendAddressName;

        /** 模試データにある科目. */
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
                final String name,
                final String guardZipCd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardName,
                final String guarantorZipCd,
                final String guarantorAddr1,
                final String guarantorAddr2,
                final String guarantorName,
                final String sendAddressZipCd,
                final String sendAddressAddr1,
                final String sendAddressAddr2,
                final String sendAddressName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _guardZipCd = guardZipCd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardName = guardName;
            _guarantorZipCd = guarantorZipCd;
            _guarantorAddr1 = guarantorAddr1;
            _guarantorAddr2 = guarantorAddr2;
            _guarantorName = guarantorName;
            _sendAddressZipCd = sendAddressZipCd;
            _sendAddressAddr1 = sendAddressAddr1;
            _sendAddressAddr2 = sendAddressAddr2;
            _sendAddressName = sendAddressName;

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
        private final Integer _assessLevel;
        private final Integer _passScore;

        private Record(
                final SubClass subClass,
                final Integer score,
                final String scoreDi,
                final Integer graphScore,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation,
                final Integer assessLevel,
                final Integer passScore
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
            _graphScore = graphScore;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
            _assessLevel = assessLevel;
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

        public String getAssessLevel() {
            return null == _assessLevel ? "" : _assessLevel.toString();
        }

        public String getPassScore() {
            return null == _passScore ? "" : _passScore.toString();
        }

//        public boolean isUnderScore() {
//            final String score = getScore();
//            final String passScore = getPassScore();
//            if (StringUtils.isEmpty(score) || !StringUtils.isNumeric(score)) {
//                return false;
//            }
//            if (StringUtils.isEmpty(passScore) || !StringUtils.isNumeric(passScore)) {
//                return false;
//            }
//            final int val = Integer.parseInt(score);
//            final int pass = Integer.parseInt(passScore);
//            return val < pass;
//        }

        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }
    
    private static class ScoreDistribution {
        private final String _distKey;
        private final Map _subclassDistributions = new HashMap();
        
        private ScoreDistribution(final String key) {
            _distKey = key;
        }
        
        private SubclassScoreDistribution getSubclassDistributionMap(String subClassCd) {
            if (!_subclassDistributions.containsKey(subClassCd)) {
                _subclassDistributions.put(subClassCd, new SubclassScoreDistribution());
            }
            return (SubclassScoreDistribution) _subclassDistributions.get(subClassCd);
        }

        public void add(SubClass subClass, Integer score) {
            final SubclassScoreDistribution subclassScoreDist = getSubclassDistributionMap(subClass._code);
            subclassScoreDist.add(score.intValue());
        }
        
        public Integer getCount(final String subClassCd, final String scoreKey) {
            final SubclassScoreDistribution subclassScoreDist = getSubclassDistributionMap(subClassCd);
            return subclassScoreDist.getCount(scoreKey);
        }

        private String distStr() {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (final Iterator it = _subclassDistributions.keySet().iterator(); it.hasNext();) {
                final String subClassCd = (String) it.next();
                final SubclassScoreDistribution ssd = (SubclassScoreDistribution) _subclassDistributions.get(subClassCd);
                stb.append(comma).append("[subClass=").append(subClassCd).append(" ").append(ssd.toString()).append("]");
                comma = ",";
            }
            return stb.toString();
        }

        public String toString() {
            return " dist = (" + distStr() + ")";
        }
    }
    
    private static class ScoreCount {
        final String _key;
        final int _rangeLower;
        final int _rangeUpper;
        final List _scoreList = new ArrayList();
        ScoreCount(final String key, final int lower, final int upper) {
            _key = key;
            _rangeLower = lower;
            _rangeUpper = upper;
        }
        boolean scoreIsInRange(final int score) {
            return _rangeLower <= score && score <= _rangeUpper;
        }
    }
    
    private static class SubclassScoreDistribution {
        public final ScoreCount[] _scoreCounts;
        public final ScoreCount _disposed;
        private boolean _added = false;
        
        SubclassScoreDistribution() {
            final int[][] nums;
            nums = new int[8][3];
            nums[0] = new int[]{40, 0, 39};
            for (int i = 1; i < nums.length; i++) {
                final int n = 3 + i;
                nums[i] = new int[]{(n + 1) * 10, n * 10, n * 10 + 9};
            }
            _scoreCounts = new ScoreCount[nums.length];
            for (int i = 0; i < nums.length; i++) {
                _scoreCounts[i] = new ScoreCount(String.valueOf(nums[i][0]), nums[i][1], nums[i][2]);
            }
            _disposed = new ScoreCount(null, -1, -1);
        }

        public ScoreCount get(final String scoreKey) {
            for (int i = 0; i < _scoreCounts.length; i++) {
                if (_scoreCounts[i]._key.equals(scoreKey)) {
                    return _scoreCounts[i];
                }
            }
            return _disposed;
        }
        
        public ScoreCount get(final int score) {
            for (int i = 0; i < _scoreCounts.length; i++) {
                if (_scoreCounts[i].scoreIsInRange(score)) {
                    return _scoreCounts[i];
                }
            }
            return _disposed;
        }
        
        public void add(final int score) {
            _added = true;
            get(score)._scoreList.add(new Integer(score));
        }
        
        public int getCount(final int score) {
            return get(score)._scoreList.size();
        }
        
        public Integer getCount(final String scoreKey) {
            return new Integer(get(scoreKey)._scoreList.size());
        }
        
        public int getFieldIndex(String scoreKey) {
            int ind = -1;
            for (int i = 0; i < _scoreCounts.length; i++) {
                if (_scoreCounts[i].equals(scoreKey)) {
                    ind = i;
                    break;
                }
            }
            if (ind == -1) {
                return -1;
            }
            return 11 - ind;
        }
        
        public String toString() {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (int i = 0; i < _scoreCounts.length; i++) {
                final String scoreKey = _scoreCounts[i]._key;
                final Integer count = getCount(scoreKey);
                stb.append(comma).append("\"").append(scoreKey).append("\"=").append(count);
                comma = ", ";
            }
            return stb.toString();
        }
    }

    private class AverageDat {
        private final SubClass _subClass;
        private final BigDecimal _avg;
        private final BigDecimal _graphAvg;
        private final Integer _highScore;
        private final Integer _count;
        private final String _coursecd;
        private final String _majorcd;
        private final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal graphAvg,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _graphAvg = graphAvg;
            _highScore = highScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        public String getAvgStr() {
            return _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String toString() {
            return _subClass + "/" + _avg + "/" + _count;
        }
    }
} // KNJD106C

// eof
