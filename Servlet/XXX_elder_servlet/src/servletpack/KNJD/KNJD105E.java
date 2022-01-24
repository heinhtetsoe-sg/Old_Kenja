// kanji=漢字
/*
 * $Id: c4bf6902746469e595f0ea25f2bda77aa823ac3b $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
import org.jfree.data.category.DefaultCategoryDataset;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: c4bf6902746469e595f0ea25f2bda77aa823ac3b $
 */
public class KNJD105E {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD105E.class);

    /** レーダーチャート凡例画像. */
    private static final String RADER_CHART_LEGEND = "RaderChartLegend.png";
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
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private Param _param;
    private boolean _hasData;
    private Form _form;
    private DB2UDB _db2;

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);
        _form = new Form(response);
        _db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        if (openDb(_db2)) {
            return;
        }

        log.error("★マスタ関連の読込み");
        _param.load(_db2);

        // 対象の生徒たちを得る
        final String[] schregnos = _param.getScregnos(_db2);
        final List students = createStudents(_db2, schregnos, _param);
        _hasData = students.size() > 0;

        // 成績のデータを読む
        log.error("★成績関連の読込み");

        loadExam(_db2, students, _param._thisExam, false, _param);

        log.error("★成績関連の読込み(前回の成績)");
        loadExam(_db2, students, _param._beforeExam, true, _param);

        // 印刷する
        log.error("★印刷");
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug("☆" + student + ", 科目の数=" + student._subclasses.size() + ", コースキー=" + student.courseKey());
            log.debug("今回の成績: " + student._record.values());

            log.debug("前回の成績: " + student._beforeRecord.values());

            _form.resetForm(1, _param);
            _form.printCover(_param, student);
            
            _form.resetForm(2, _param);
            _form.printStatic(_param, student);
            _form.printRecord(_param, student);
        }

        log.error("★終了処理");
        _form.closeSvf();
        closeDb(_db2);
        removeImageFiles();
        log.info("Done.");
    }

    private static void loadExam(final DB2UDB db2, final List students, final Exam exam, final boolean isBefore, final Param param) throws SQLException {
        loadSubClasses(db2, exam, students, isBefore, param);
        loadAverageDat(db2, exam, param);
        loadRecord(db2, exam, students, isBefore, param);
        loadRecordOther(db2, exam, students, isBefore, param);
//        if (_param._isConvertScoreTo100) {
//            loadConvertScore(_db2, exam);
//        }
    }

//    private void loadConvertScore(DB2UDB db2, Exam exam) {
//        exam._convertedScore.load(db2, exam);
//    }

    private static void loadSubClasses(final DB2UDB db2, final Exam exam, final List students, final boolean isBefore, final Param param) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlSubClasses(exam, param));
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) param._subClasses.get(subClasscd);
                    if (null != subClass) {
                        if (isBefore) {
                            student._beforeSubclasses.put(subClasscd, subClass);
                        } else {
                            student._subclasses.put(subClasscd, subClass);
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            log.fatal("成績入力データの科目取得でエラー");
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private static String sqlSubClasses(final Exam exam, final Param param) {
        String rtn;
        rtn = "select";
        if ("1".equals(param._useCurriculumcd)) {
            rtn +="  distinct T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD";
        } else {
            rtn +="  distinct T1.SUBCLASSCD";
        }
        rtn     +=" from"
                + "  RECORD_SCORE_DAT T1 "
                + " where"
                + "  T1.YEAR = '" + exam._year + "' and"
                + "  T1.SEMESTER = '" + exam._semester + "' and"
                + "  T1.TESTKINDCD || T1.TESTITEMCD = '" + exam._testCd + "' and"
                + "  T1.SCHREGNO = ? "
                ;
        log.debug("成績入力データの科目のSQL=" + rtn);
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
    private static void loadAverageDat(
            final DB2UDB db2,
            final Exam exam,
            final Param param
    ) throws SQLException {
        
        final String year = exam._year;
        final String semester = exam._semester;
        final String kindCd = exam.getKindCd();
        final String itemCd = exam.getItemCd();
        
        String selectFrom;
        selectFrom = "SELECT";
        if ("1".equals(param._useCurriculumcd)) {
            selectFrom += "  case when t1.subclasscd in ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') then t1.subclasscd  ";
            selectFrom += "   else t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd ";
            selectFrom += "  end as subclasscd,";
        } else {
            selectFrom += "  t1.subclasscd,";
        }
        selectFrom += ""
            + "  t1.avg,"
            + "  t1.stddev,"
            + "  t1.highscore,"
            + "  t1.count,"
            + "  t1.coursecd,"
            + "  t1.majorcd,"
            + "  t1.coursecode,"
            + "  value(t2.perfect, 100) as perfect"
            + " FROM"
            + "  record_average_dat t1"
            + "  LEFT JOIN perfect_record_dat t2 ON t2.year = t1.year AND "
            + "    t2.semester= t1.semester AND"
            + "    t2.testkindcd= t1.testkindcd AND"
            + "    t2.testitemcd= t1.testitemcd AND";
            if ("1".equals(param._useCurriculumcd)) {
                selectFrom += ""
                    + "    t2.classcd= t1.classcd AND"
                    + "    t2.school_kind= t1.school_kind AND"
                    + "    t2.curriculum_cd= t1.curriculum_cd AND";
            }
        selectFrom += ""
            + "    t2.subclasscd= t1.subclasscd AND"
            + "    t2.grade = (case when t2.div = '01' then '00' else '" + exam._grade + "' end) AND"
            + "    t2.coursecd || t2.majorcd || t2.coursecode = "
            + "        (case when t2.div in ('01', '02') then '00000000' "
            + "              when t2.div = '04' then '0' || t1.majorcd || '0000' "
            + "              else t1.coursecd || t1.majorcd || t1.coursecode end) "
            ;
        final String where;
        if (param.isGakunen()) {
            where = " WHERE"
                + "    t1.year='" + year + "' AND"
                + "    t1.semester='" + semester + "' AND"
                + "    t1.testkindcd='" + kindCd + "' AND"
                + "    t1.testitemcd='" + itemCd + "' AND"
                + "    t1.avg_div='1' AND"
                + "    t1.grade= '" + exam._grade + "' AND"
                + "    t1.hr_class='000' AND"
                + "    t1.coursecd='0' AND"
                + "    t1.majorcd='000' AND"
                + "    t1.coursecode='0000'"
                ;
        } else if (param.isCourseGroup()) {
            where = " WHERE"
                + "    t1.year='" + year + "' AND"
                + "    t1.semester='" + semester + "' AND"
                + "    t1.testkindcd='" + kindCd + "' AND"
                + "    t1.testitemcd='" + itemCd + "' AND"
                + "    t1.avg_div='5' AND"
                + "    t1.grade = '" + exam._grade + "' AND"
                + "    t1.hr_class='000'"
                ;
        } else {
            where = " WHERE"
                + "    t1.year='" + year + "' AND"
                + "    t1.semester='" + semester + "' AND"
                + "    t1.testkindcd='" + kindCd + "' AND"
                + "    t1.testitemcd='" + itemCd + "' AND"
                + "    t1.avg_div='3' AND"
                + "    t1.grade = '" + exam._grade + "' AND"
                + "    t1.hr_class='000'"
                ;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = selectFrom + where;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                final SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
                }
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
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                final String key;
                if (param.isGakunen()) {
                    key = subclasscd;
                } else if (param.isCourseGroup()) {
                    key = subclasscd + "0" + majorcd + "0000";
                } else {
                    key = subclasscd + coursecd + majorcd + coursecode;
                }
                if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    final AverageDat avgDat = new AverageDat(subClass0, avg, avgPercent, stdDev, highScore, count, coursecd, majorcd, coursecode);
                    exam._averageDatOther.put(key, avgDat);
                } else {
                    final AverageDat avgDat = new AverageDat(subClass, avg, avgPercent, stdDev, highScore, count, coursecd, majorcd, coursecode);
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
        log.debug("テストコード=" + kindCd + itemCd + " の成績平均データの件数=" + exam._averageDat.size());
    }

    /**
     * 生徒に成績データを関連付ける。
     * @param db2 DB
     * @param kindCd テスト種別コード
     * @param itemCd テスト項目コード
     * @param scoreDistributions 得点分布の格納場所
     * @param students 生徒たち
     * @param isBefore 前回か?
     */
    private static void loadRecord(
            final DB2UDB db2,
            final Exam exam,
            final List students,
            final boolean isBefore,
            final Param param
    ) {
        final String year = exam._year;
        final String semester = exam._semester;
        final String kindCd = exam.getKindCd();
        final String itemCd = exam.getItemCd();
        
        final String gradeRank  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
        final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
        final String majorRank = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";

        String sql;
        /* 通常の成績 */
        sql = "SELECT";
        if ("1".equals(param._useCurriculumcd)) {
            sql +="  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as subclasscd,";
        } else {
            sql +="  t1.subclasscd,";
        }
        sql +="  t2.score,"
            + "  t2." + gradeRank + " as grade_rank,"
            + "  t2.grade_deviation,"
            + "  t2." + courseRank + " as course_rank,"
            + "  t2.course_deviation,"
            + "  t2." + majorRank + " as major_rank,"
            + "  t2.major_deviation,"
            + "  t3.div,"
            + "  value(t3.perfect, 100) as perfect"
            + " FROM"
            + "  record_score_dat t1 "
            + " LEFT JOIN record_rank_dat t2 ON"
            + "    t1.year=t2.year AND"
            + "    t1.semester=t2.semester AND"
            + "    t1.testkindcd=t2.testkindcd AND"
            + "    t1.testitemcd=t2.testitemcd AND";
        if ("1".equals(param._useCurriculumcd)) {
            sql +="    t1.classcd=t2.classcd AND"
                + "    t1.school_kind=t2.school_kind AND"
                + "    t1.curriculum_cd=t2.curriculum_cd AND";
        }
        sql +="    t1.subclasscd=t2.subclasscd AND"
            + "    t1.schregno=t2.schregno "
            + " LEFT JOIN perfect_record_dat t3 ON"
            + "    t1.year=t3.year AND"
            + "    t1.semester=t3.semester AND"
            + "    t1.testkindcd=t3.testkindcd AND"
            + "    t1.testitemcd=t3.testitemcd AND";
        if ("1".equals(param._useCurriculumcd)) {
            sql +="    t1.classcd=t3.classcd AND"
                + "    t1.school_kind=t3.school_kind AND"
                + "    t1.curriculum_cd=t3.curriculum_cd AND";
        }
        sql +="    t1.subclasscd=t3.subclasscd AND"
            + "    t3.grade = (case when t3.div = '01' then '00' else ? end) AND "
            + "    t3.coursecd || t3.majorcd || t3.coursecode = "
            + "        (case when t3.div in ('01', '02') then '00000000' "
            + "              when t3.div = '04' then '0' || ? || '0000' "
            + "              else ? end) "
            + " WHERE"
            + "  t1.year='" + year + "' AND"
            + "  t1.semester='" + semester + "' AND"
            + "  t1.testkindcd='" + kindCd + "' AND"
            + "  t1.testitemcd='" + itemCd + "' AND"
            + "  t1.score_div='01' AND"
            + "  t1.schregno=?"
            ;
        loadRecord1(db2, kindCd, itemCd, students, isBefore, param, sql);
    }

    private static void loadRecord1(
            final DB2UDB db2,
            final String kindCd,
            final String itemCd,
            final List students,
            final boolean isBefore,
            final Param param,
            final String sql
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._grade);
                ps.setString(2, student._coursegroupCd);
                ps.setString(3, student._courseCd + student._majorCd + student._courseCode);
                ps.setString(4, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                        final Class clazz;
                        if ("1".equals(param._useCurriculumcd)) {
                            final String[] split = StringUtils.split(subclasscd, "-");
                            clazz = (Class) param._classes.get(split[0] + "-" + split[1]);
                        } else {
                            clazz = (Class) param._classes.get(subclasscd.substring(0, 2));
                        }
                        if (null == clazz) {
                            continue;
                        }
                        subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    Integer scorePercent = null;
                    final int perfect = rs.getInt("PERFECT");
                    if (100 == perfect) {
                        scorePercent = score;
                    } else if (null != score) {
                        scorePercent = new Integer(new BigDecimal(rs.getString("SCORE")).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 0, BigDecimal.ROUND_HALF_UP).intValue());
                    }
                    final Integer rank;
                    final BigDecimal deviation;
                    if (param.isGakunen()) {
                        rank = KNJServletUtils.getInteger(rs, "grade_rank");
                        deviation = rs.getBigDecimal("grade_deviation");
                    } else if (param.isCourseGroup()) {
                        rank = KNJServletUtils.getInteger(rs, "major_rank");
                        deviation = rs.getBigDecimal("major_deviation");
                    } else {
                        rank = KNJServletUtils.getInteger(rs, "course_rank");
                        deviation = rs.getBigDecimal("course_deviation");
                    }

                    final Record rec = new Record(subClass, score, scorePercent, null, rank, deviation);
                    if (isBefore) {
                        student._beforeRecord.put(subClass, rec);
                    } else {
                        student._record.put(subClass, rec);
                    }
                }
            }
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private static void loadRecordOther(
            final DB2UDB db2,
            final Exam exam,
            final List students,
            final boolean isBefore,
            final Param param
    ) {
        final String kindCd = exam.getKindCd();
        final String itemCd = exam.getItemCd();
        
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String gradeRank  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
            final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
            final String majorRank = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";
            String sql;
            sql = "SELECT"
                + "  subclasscd,"
                + "  score,"
                + "  avg,"
                + "  " + gradeRank + " as grade_rank,"
                + "  grade_deviation,"
                + "  " + courseRank + " as course_rank,"
                + "  course_deviation,"
                + "  " + majorRank + " as major_rank,"
                + "  major_deviation"
                + " FROM record_rank_dat"
                + " WHERE"
                + "  year='" + exam._year + "' AND"
                + "  semester='" + exam._semester + "' AND"
                + "  testkindcd='" + kindCd + "' AND"
                + "  testitemcd='" + itemCd + "' AND"
                + "  subclasscd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
                + "  schregno='" + student._schregno + "'"
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
                    if (param.isGakunen()) {
                        rank = KNJServletUtils.getInteger(rs, "grade_rank");
                        deviation = rs.getBigDecimal("grade_deviation");
                    } else if (param.isCourseGroup()) {
                        rank = KNJServletUtils.getInteger(rs, "major_rank");
                        deviation = rs.getBigDecimal("major_deviation");
                    } else {
                        rank = KNJServletUtils.getInteger(rs, "course_rank");
                        deviation = rs.getBigDecimal("course_deviation");
                    }

                    final SubClass subClass = new SubClass(subclasscd);
                    final Record rec = new Record(subClass, score, null, avg, rank, deviation);
                    if (isBefore) {
                        student._beforeRecordOther.put(subClass._code, rec);
                    } else {
                        student._recordOther.put(subClass._code, rec);
                    }
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static List createStudents(final DB2UDB db2, final String[] schregnos, final Param param) throws SQLException {
        final List rtn = new LinkedList();
        final String sql = studentsSQL(schregnos, param);

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
                final String coursegroupCd = rs.getString("coursegroupCd");
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
                        coursegroupCd,
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

    private static String studentsSQL(final String[] selected, final Param param) {
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
            + "  t3.remark1,"
            + "  t4.group_cd as coursegroupCd"
            + " FROM"
            + "  v_schreg_info t1 "
            + "    LEFT JOIN hexam_record_remark_dat t3 ON"
            + "      t1.year=t3.year AND"
            + "      t1.semester=t3.semester AND"
            + "      t3.testkindcd='" + param._thisExam.getKindCd() + "' AND"
            + "      t3.testitemcd='" + param._thisExam.getItemCd() + "' AND"
            + "      t1.schregno=t3.schregno AND"
            + "      t3.remark_div='2'"// '2'固定
            + "    LEFT JOIN course_group_cd_dat t4 ON t1.year=t4.year AND"
            + "      t1.grade=t4.grade AND"
            + "      t1.coursecd=t4.coursecd AND"
            + "      t1.majorcd=t4.majorcd AND"
            + "      t1.coursecode=t4.coursecode"
            + " WHERE"
            + "  t1.year='" + param._year + "' AND"
            + "  t1.semester='" + param._semester + "' AND"
            + "  t1.schregno IN " + students
            ;
        return sql;
    }

    private static boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private static void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private static Param createParam(final HttpServletRequest request) {
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
        return (int) (pixel * 1.3);
    }
    
    private static class Exam {

        private final String _year;
        private final String _semester;
        private final String _testCd;
        private String _grade;
        private String _examName = "";
        private String _semestername = "";
        private String _title = "";
        
        /** 成績平均データ。 */
        private Map _averageDat = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map _averageDatOther = new HashMap();
        /** 前回の換算した得点、平均、最高点 */
        private ConvertedScore _convertedScore;
        /** 合併元科目コード */
        private Map _attendSubclassCdMap = Collections.EMPTY_MAP;
        
        public Exam(final String year, final String semester, final String testCd) {
            _year = year;
            _semester = semester;
            _testCd = testCd;
        }

        public String getKindCd() {
            return null == _testCd ? null : _testCd.substring(0, 2);
        }

        public String getItemCd() {
            return null == _testCd ? null : _testCd.substring(2);
        }
        
        private void load(final Param param, final DB2UDB db2) {
            _convertedScore = null;
            loadExam(db2);
            loadAttendSubclassCdMap(db2, param);
        }
        
        private void loadAttendSubclassCdMap(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String flg = "9900".equals(_testCd) ? "2" : "1";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("       T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
                stb.append("   FROM ");
                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR = '" + _year + "' ");
                stb.append("       AND T1.FLG = '" + flg + "' ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key = rs.getString("KEY");
                    if (null == map.get(key)) {
                        map.put(key, new ArrayList());
                    }
                    final List list = (List) map.get(key);
                    list.add(rs.getString("ATTEND_SUBCLASSCD"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _attendSubclassCdMap = map;
        }

        public List getAttendSubclassCdList(final Student student) {
            final List list = (List) _attendSubclassCdMap.get(student.attendSubclassCdKey());
            if (null == list) {
                return Collections.EMPTY_LIST;
            }
            return list;
        }
        
        private void loadExam(final DB2UDB db2) {
            if (null == _year) {
                _title = "";
                return;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            _examName = "";
            try {
                final String sql = "SELECT testitemname FROM testitem_mst_countflg_new"
                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
                    + " AND testkindcd='" + getKindCd() + "' AND testitemcd='" + getItemCd() + "'"
                    ;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _examName = rs.getString("testitemname");
                }
            } catch (final SQLException e) {
                log.error("考査名取得エラー。");
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
            _title = nendo() + "　" + _semestername + "　" + _examName;
        }
        
        String nendo() {
            return KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
        }
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _testCd;
        private final String _grade;

        private String _groupDiv;

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
        /** 基準点 */
        private final String _outputKijun;

        private final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        private Map _classes;
        private final String _imagePath;

        private final String _useCurriculumcd;
        private final String _useClassDetailDat;

        private final List _classOrder = new ArrayList();

        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;

        /** 欠点 */
        private final int _borderScore;
        /** 科目マスタ。 */
        private Map _subClasses;
        
//        /** 100点満点に換算する */
//        private final boolean _isConvertScoreTo100;
        
        private final Exam _thisExam;
        private Exam _beforeExam = new Exam(null, null, null);
        
        public Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _div = request.getParameter("CATEGORY_IS_CLASS");
            _values = request.getParameterValues("CATEGORY_SELECTED");
            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");

            _isClassMode = "1".equals(request.getParameter("CATEGORY_IS_CLASS")) ? true : false;

            final String groupDiv = request.getParameter("GROUP_DIV");// 1=学年, 2=コース
            _groupDiv =  groupDiv;
            _deviationPrint = request.getParameter("DEVIATION_PRINT");

            _imagePath = request.getParameter("IMAGE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            log.debug(" record_rank_dat outputKijun ? = " + _outputKijun);
            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            
            _borderScore = "".equals(request.getParameter("KETTEN")) || request.getParameter("KETTEN") == null ? 0 : Integer.parseInt(request.getParameter("KETTEN"));;
//            _isPrintDistribution = "2".equals(request.getParameter("USE_GRAPH"));
//            _isPrintGuardianComment = "1".equals(request.getParameter("USE_HOGOSYA"));
//            _isConvertScoreTo100 = "1".equals(request.getParameter("KANSAN"));
            
            _thisExam = new Exam(_year, _semester, _testCd);
            _thisExam._grade = _grade;
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
            loadIsJunior(db2);
            loadCertifSchool(db2);
            loadRegdHdat(db2);
            loadBefore(db2);
            _thisExam.load(this, db2);
            _beforeExam.load(this, db2);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);
            setCombinedOnSubClass(db2);

            loadClassOrder(db2);
        }

        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
            final Set sakiSet = new HashSet();
            final Set motoSet = new HashSet();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCombined());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combined = rs.getString("COMBINED_SUBCLASSCD");
                    final String attend = rs.getString("ATTEND_SUBCLASSCD");
                    sakiSet.add(combined);
                    motoSet.add(attend);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // 合併先
            for (final Iterator it = sakiSet.iterator(); it.hasNext();) {
                final String saki = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(saki);
                if (null != subClass) {
                    subClass.setSaki();
                }
            }
            // 合併元
            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
                final String moto = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(moto);
                if (null != subClass) {
                    subClass.setMoto();
                }
            }
        }

        public String sqlCombined() {
            String rtn;
            rtn = "select distinct";
            if ("1".equals(_useCurriculumcd)) {
                rtn +="  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ";
            }
            rtn +="  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,";
            if ("1".equals(_useCurriculumcd)) {
                rtn +="  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ";
            }
            rtn +="  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD"
                + " from SUBCLASS_REPLACE_COMBINED_DAT"
                + " where"
                + "  YEAR = '" + _year + "'"
                ;
            return rtn;
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
            log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        public String sqlSubClasses() {
            String rtn = "select";
            if  ("1".equals(_useCurriculumcd)) {
                rtn +="   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD,";
            } else {
                rtn +="   SUBCLASSCD,";
            }
            rtn +="   coalesce(SUBCLASSORDERNAME2, SUBCLASSNAME ) as NAME,"
                + "   SUBCLASSABBV"
                + " from V_SUBCLASS_MST"
                + " where"
                + "   YEAR = '" + _year + "'"
                + " order by";
            if  ("1".equals(_useCurriculumcd)) {
                rtn +="   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD";
            } else {
                rtn +="   SUBCLASSCD";
            }                    
            return rtn;
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
            String rtn = "";
            rtn += "select";
            if ("1".equals(_useCurriculumcd)) {
                rtn +="   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
            } else {
                rtn +="   CLASSCD,";
            }
            rtn +="   CLASSNAME,"
                + "   CLASSABBV"
                + " from V_CLASS_MST"
                + " where"
                + "   YEAR = '" + _year + "'"
                + " order by";
            if ("1".equals(_useCurriculumcd)) {
                rtn +="   CLASSCD || '-' || SCHOOL_KIND ";
            } else {
                rtn +="   CLASSCD";
            }
            ;
            return rtn;
        }

        private void loadBefore(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" with TESTITEMS as ( ");
                stb.append("   select ");
                stb.append("     t1.YEAR, ");
                stb.append("     t1.SEMESTER, ");
                stb.append("     t1.TESTKINDCD, ");
                stb.append("     t1.TESTITEMCD, ");
                stb.append("     case when t2.YEAR is not null then 1 else 0 end as CURRENT, ");
                stb.append("     ROW_NUMBER() OVER(ORDER BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD) as ORDER ");
                stb.append("   from  ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW t1 ");
                stb.append("     left join TESTITEM_MST_COUNTFLG_NEW t2 ON ");
                stb.append("       t2.YEAR = t1.YEAR ");
                stb.append("       and t2.SEMESTER = '" + _semester + "' ");
                stb.append("       and t2.TESTKINDCD = '" + _thisExam.getKindCd() + "' ");
                stb.append("       and t2.TESTITEMCD = '" + _thisExam.getItemCd() + "' ");
                stb.append("       and t2.YEAR = t1.YEAR ");
                stb.append("       and t2.SEMESTER = t1.SEMESTER ");
                stb.append("       and t2.TESTKINDCD = t1.TESTKINDCD ");
                stb.append("       and t2.TESTITEMCD = t1.TESTITEMCD ");
                stb.append("   where ");
                stb.append("     t1.YEAR IN ('" + _year + "') ");
                stb.append("     and t1.SEMESTER <> '9' ");
                stb.append("     and t1.TESTKINDCD <> '99' ");
                stb.append("     and not (t1.SEMESTER = '3' AND t1.TESTKINDCD = '01' AND t1.TESTITEMCD = '01') ");
                stb.append("   order by ");
                stb.append("     t1.YEAR, ");
                stb.append("     t1.SEMESTER, ");
                stb.append("     t1.TESTKINDCD, ");
                stb.append("     t1.TESTITEMCD ");
                stb.append(" ) ");
                stb.append(" select ");
                stb.append("   t2.YEAR as year, ");
                stb.append("   t2.SEMESTER as semester, ");
                stb.append("   t2.TESTKINDCD || T2.TESTITEMCD as testcd ");
                stb.append(" from ");
                stb.append("   TESTITEMS t1 ");
                stb.append("   inner join TESTITEMS t2 on t2.ORDER = t1.ORDER - 1 ");
                stb.append(" where ");
                stb.append("   t1.CURRENT = 1 ");
                
                log.debug(" loadBefore sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String beforeYear = rs.getString("year");
                    final String beforeSemester = rs.getString("semester");
                    final String beforeTestCd = rs.getString("testcd");
                    
                    _beforeExam = new Exam(beforeYear, beforeSemester, beforeTestCd);
                    _beforeExam._grade = _grade;
                }
            } catch (final SQLException e) {
                log.error("以前の考査取得エラー。");
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
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

        private void loadClassOrder(final DB2UDB db2) {
            final String sql;
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                sql = "SELECT classcd || '-' || school_kind AS classcd FROM class_detail_dat"
                        + " WHERE year='" + _year + "' AND class_seq='004' "
                        + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
                        ;
            } else {
                final String field1 = _isJunior ? "name1" : "name2";
                final String field2 = _isJunior ? "namespare1" : "namespare2";
                sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
                    + " WHERE year='" + _year + "' AND namecd1='D009' AND " + field1 + " IS NOT NULL "
                    + " ORDER BY " + field2
                    ;
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _classOrder.add(rs.getString("classcd"));
                }
            } catch (final SQLException e) {
                log.error("教科表示順取得エラー。");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug("教科表示順=" + _classOrder);
        }

        private void loadIsJunior(DB2UDB db2) {
            _isJunior = Integer.parseInt(_grade) < 4;
            log.debug("中学校?:" + _isJunior);
            _isHigh = !_isJunior;
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

        public boolean isUnderScore(final String score) {
            if (StringUtils.isEmpty(score)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            return val < _borderScore;
        }

        String get9title() {
            return "全教科";
        }

        public boolean isGakunen() {
            return "1".equals(_groupDiv);
        }

        public boolean isCourseGroup() {
            return "3".equals(_groupDiv);
        }
    }

    private class Form {
        private Vrw32alp _svf;

        /** メインの成績(サブフォーム). */
        private final MainRecTable _mainTable = new MainRecTable();
        /** 前回の成績. */
        private final MainRecTable _subTable = new SubRecTable();

        public Form(final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

        }
        
        public void resetForm(final int i, final Param param) {
            final int sts;
            if (1 == i) {
                sts = _svf.VrSetForm("KNJD105E_1.frm", 1);
            } else {
                if (null == param._beforeExam._year) {
                    sts = _svf.VrSetForm("KNJD105E_3.frm", 4);
                } else {
                    sts = _svf.VrSetForm("KNJD105E_2.frm", 4);
                }
            }
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }
        }
        
        public void printCover(final Param param, final Student student) {
            
            _svf.VrsOut("NENDO", param._thisExam.nendo());

            _svf.VrsOut("SEMESTER", param._thisExam._semestername);

            _svf.VrsOut("TESTNAME", param._thisExam._examName);
            
            final int attendNo = Integer.parseInt(student._attendNo);
            _svf.VrsOut("HR_NAME", student._hrName + toZenkaku(attendNo) + "番");

            if (student._name.length() <= STUDENT_NAME_DELIMTER_COUNT) {
                _svf.VrsOut("NAME", student._name);
            } else {
                _svf.VrsOut("NAME_2", student._name);
            }
            _svf.VrEndPage();
        }
        
        private String toZenkaku(final int num) {
            final Map m = new HashMap();
            m.put("0", "０");
            m.put("1", "１");
            m.put("2", "２");
            m.put("3", "３");
            m.put("4", "４");
            m.put("5", "５");
            m.put("6", "６");
            m.put("7", "７");
            m.put("8", "８");
            m.put("9", "９");
            
            final String s = String.valueOf(num);
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                final String c = String.valueOf(s.charAt(i));
                stb.append(null == m.get(c) ? c : (String) m.get(c));
            }
            return stb.toString();
        }

        public void printStatic(final Param param, final Student student) {
            _svf.VrsOut("SCHOOL_NAME", param._schoolName);

            final String staffName = (String) param._staffs.get(student._grade + student._hrClass);
            _svf.VrsOut("STAFFNAME", param._remark2 + staffName);

            _svf.VrsOut("NENDO", param._thisExam._title);
            _svf.VrsOut("LAST_EXAM_TITLE", StringUtils.isBlank(param._beforeExam._title) ? "" : (param._beforeExam._title + "の成績"));
        
            final int attendNo = Integer.parseInt(student._attendNo);
            _svf.VrsOut("HR_NAME", student._hrName + toZenkaku(attendNo) + "番");

            if (student._name.length() <= STUDENT_NAME_DELIMTER_COUNT) {  // 全角で規定文字数を超えたらフォントを変える
                _svf.VrsOut("NAME", student._name);
            } else {
                _svf.VrsOut("NAME_2", student._name);
            }

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

            // 個人評(期末の場合は注意事項)
            KNJServletUtils.printDetail(_svf, "PERSONAL_REMARK", student._remark, 45 * 2, 3);

//            final String get3title = _param.get3title(student.courseKey());
//            _svf.VrsOut("ITEM_TOTAL3", get3title);
//            final String get5title = _param.get5title(student.courseKey());
//            if (null != get5title) {
//                _svf.VrsOut("ITEM_TOTAL5", get5title);
//            }
//            if (null != get3title) {
//                _svf.VrsOut("ITEM_TOTAL3_1", get3title + "平均");
//            }
//            if (null != get5title) {
//                _svf.VrsOut("ITEM_TOTAL5_1", get5title + "平均");
//            }
            final String get9title = param.get9title();
            _svf.VrsOut("ITEM_TOTAL5", get9title);

            final String barLegendImage;
            final String msg;
            if (param.isGakunen()) {
                barLegendImage = BAR_CHART_LEGEND1;
                msg = "学年";
            } else if (param.isCourseGroup()) {
                barLegendImage = BAR_CHART_LEGEND3;
                msg = "グループ";
            } else {
                barLegendImage = BAR_CHART_LEGEND2;
                msg = "コース";
            }

            _svf.VrsOut("ITEM_AVG", msg + "平均");
            _svf.VrsOut("ITEM_RANK", msg + "順位");
            _svf.VrsOut("ITEM_RANK", msg + "順位");
            if ("1".equals(param._deviationPrint)) {
                _svf.VrsOut("ITEM_DEVIATION", "偏差値");
            } else if ("2".equals(param._deviationPrint)) {
                _svf.VrsOut("ITEM_DEVIATION", "標準偏差");
            }
            
            // 画像
            _svf.VrsOut("RADER_LEGEND", param._imagePath + "/" + RADER_CHART_LEGEND);
            _svf.VrsOut("BAR_LEGEND", param._imagePath + "/" + barLegendImage);

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
            _mainTable.printRec3and5and9(param, student._recordOther, param._thisExam._averageDatOther, param._thisExam._convertedScore, student);

            // グラフ印字(サブフォームよりも先に印字)
            final List attendSubclassCdList = param._thisExam.getAttendSubclassCdList(student);
            printBarGraph(param, student, param._thisExam._averageDat, attendSubclassCdList);
            printRadarGraph(param, student._record, attendSubclassCdList);

            // 成績
            _mainTable.print(student, student._subclasses, param._thisExam._averageDat, attendSubclassCdList, student._record, param);

            // 前回の中間成績
            _subTable.printRec3and5and9(param, student._beforeRecordOther, param._beforeExam._averageDatOther, param._beforeExam._convertedScore, student);
            final List befAttendSubclassCdList = param._beforeExam.getAttendSubclassCdList(student);
            _subTable.print(student, student._beforeSubclasses, param._beforeExam._averageDat, befAttendSubclassCdList, student._beforeRecord, param);
        }

        public void printBarGraph(final Param param, final Student student, final Map averages, final List attendSubclassCdList) {
            // グラフ用のデータ作成
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            int i = 0;
            for (final Iterator it = student._record.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                if (record._subClass._isMoto) {
                    continue;
                }
                
                if ("90".equals(record._subClass.getClassCd(param))) {
                    continue;
                }

                if (attendSubclassCdList.contains(record._subClass._code)) {
                    continue;
                }

                final String abbv = record._subClass._abbv;
                final String subclassKey = null != abbv && abbv.length() > 4 ? abbv.substring(0, 4) : abbv;
                scoreDataset.addValue(record._scorePercent, "本人得点", subclassKey);
                final String key;
                if (param.isGakunen()) {
                    key = record._subClass._code;
                } else if (param.isCourseGroup()) {
                    key = record._subClass._code + student.courseGroupKey();
                } else {
                    key = record._subClass._code + student.courseKey();
                }
                final AverageDat avgDat = (AverageDat) averages.get(key);
                final BigDecimal avgPercent = (null == avgDat) ? null : avgDat._avgPercent;

                final String msg = param.isGakunen() ? "学年" : param.isCourseGroup() ? "グループ" : "コース";
                avgDataset.addValue(avgPercent, msg + "平均点", subclassKey);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + ", 平均=" + avgPercent);
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }

            try {
                // チャート作成
                final JFreeChart chart = createBarChart(scoreDataset, avgDataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 1940, 930);
                _graphFiles.add(outputFile);

                // グラフの出力
                _svf.VrsOut("BAR_LABEL", "得点");
                
                if (outputFile.exists()) {
                    _svf.VrsOut("BAR", outputFile.toString());
                }
            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
        }

        private JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset
        ) {
            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));

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

        private void printRadarGraph(final Param param, final Map records, final List attendSubclassCdList) {
            // データ作成
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final Iterator it0 = param._classOrder.iterator(); it0.hasNext();) {
                final String classCd = (String) it0.next();
                
                for (final Iterator it = records.values().iterator(); it.hasNext();) {
                    final Record record = (Record) it.next();
                    if (!classCd.equals(record._subClass.getClassCd(param))) {
                        continue;
                    }
                    if (attendSubclassCdList.contains(record._subClass._code)) {
                        continue;
                    }
                    setDataset(dataset, record, param);
                }
            }

            try {
                // チャート作成
                final JFreeChart chart = createRaderChart(dataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 938, 784);
                _graphFiles.add(outputFile);

                // グラフの出力
                if (0 < dataset.getColumnCount() && outputFile.exists()) {
                    _svf.VrsOut("RADER", outputFile.toString());
                }
            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
        }

        private void setDataset(final DefaultCategoryDataset dataset, final Record record, final Param param) {
            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
            final Class clazz = (Class) param._classes.get(record._subClass.getClassKey(param));
            final String name = (null == clazz) ? "" : clazz._abbv;
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

        /**
         * 網掛けする。
         * @param field フィールド
         * @param value 値
         */
        void amikake(final String field, final String value) {
            _svf.VrAttribute(field, "Paint=(2,70,1),Bold=1");
            _svf.VrsOut(field, value);
            _svf.VrAttribute(field, "Paint=(0,0,0),Bold=0");
        }

        void kasen(final String field, final String value) {
            _svf.VrAttribute(field, "UnderLine=(0,1,1)");
            _svf.VrsOut(field, value);
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
            String getFClass1() { return "CLASS1"; }
            String getFSubClass() { return "SUBCLASS"; }
            String getFSubClass1() { return "SUBCLASS1"; }
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

            /*
             * 9教科
             */
            String getFScore9() { return "SCORE5"; }
            String getFRank9() { return "RANK5"; }
            String getFDeviation9() { return "DEVIATION5"; }
            String getFAverage9() { return "AVERAGE5"; }
            String getFMaxScore9() { return "MAX_SCORE5"; }
            String getFExaminee9() { return "EXAMINEE5"; }

            public void print(final Student student, final Map subclasses, final Map averages, final List attendSubclassCdList, final Map records, final Param param) {
                int i = 0;
                for (final Iterator it = subclasses.values().iterator(); it.hasNext();) {
                    final SubClass subClass = (SubClass) it.next();

                    if ("90".equals(subClass.getClassCd(param))) {
                        continue;
                    }
                    
                    if (attendSubclassCdList.contains(subClass._code)) {
                        continue;
                    }

                    final Record record = (Record) records.get(subClass);
                    final String key;
                    if (param.isGakunen()) {
                        key = subClass._code;
                    } else if (param.isCourseGroup()) {
                        key = subClass._code + student.courseGroupKey();
                    } else {
                        key = subClass._code + student.courseKey();
                    }
                    final AverageDat avgDat = (AverageDat) averages.get(key);
                    
                    printRecord(subClass, record, avgDat, param);

                    _svf.VrEndRecord();
                    if (++i >= TABLE_SUBCLASS_MAX) {
                        break;
                    }
                }
                if (i == 0) {
                    _svf.VrsOut(getFClass(), "\n");
                    _svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
                }
            }

            private void printRecord(
                    final SubClass subClass,
                    final Record record,
                    final AverageDat avgDat,
                    final Param param
            ) {
                final Map classMst = param._classes;
                final Class clazz = (Class) classMst.get(subClass.getClassKey(param));
                if (null == clazz) {
                    return;
                }
//                if (_fiveClassCd.contains(clazz._code)) {
//                    amikake(getFClass(), clazz._name);
//                } else {
//                    _svf.VrsOut(getFClass(), clazz._name);
//                }
                _svf.VrsOut(getFClass(), clazz._name);

                // 科目
                _svf.VrsOut(getFSubClass(), subClass._name);

                if (null == record) {
                    return;
                }

                final String score = record.getScore();
                if (param.isUnderScore(score)) {
                    _svf.VrsOut(getFScore(), "(" + score + ")");
                } else {
                    _svf.VrsOut(getFScore(), score);
                }

                _svf.VrsOut(getFRank(),      record.getRank());
                if ("1".equals(param._deviationPrint)) {
                    _svf.VrsOut(getFDeviation(), record.getDeviation());
                }

                if (null != avgDat) {
                    _svf.VrsOut(getFAverage(),  avgDat.getAvgStr());
                    _svf.VrsOut(getFMaxScore(), avgDat._highScore.toString());
                    _svf.VrsOut(getFExaminee(), avgDat._count.toString());    // 受験者数
                    if ("2".equals(param._deviationPrint)) {
                        _svf.VrsOut(getFDeviation(), avgDat.getStdDevStr());
                    }
                }
            }

            /**
             * 3,5教科の印字
             */
            public void printRec3and5and9(final Param param, final Map recordOther, final Map avgDatOther, final ConvertedScore convScore, final Student student) {
//                printRecordRec3(recordOther, avgDatOther, convScore, student);
//
//                //TAKAESU: 以下は分かりにくい。リファクタせよ!
//                if (_param._useSubclassGroup) {
//                    if (null != _param._subclassGroup5.get(student.courseKey())) {
//                        printRecordRec5(recordOther, avgDatOther, convScore, student);
//                    }
//                } else {
//                    printRecordRec5(recordOther, avgDatOther, convScore, student);
//                }
                printRecordRec9(param, recordOther, avgDatOther, student);

            }

//            private void printRecordRec3(final Map recordOther, final Map avgDatOther, final ConvertedScore convScore, Student student) {
//                final Record rec3 = (Record) recordOther.get(ALL3);
//                if (null != rec3) {
////                    _svf.VrsOut(getFScore3(), _param._isConvertScoreTo100 ? rec3.getAvg() : rec3.getScore());
//                    _svf.VrsOut(getFScore3(), rec3.getScore());
//                    _svf.VrsOut(getFRank3(), rec3.getRank());
//                    if (_param._deviationPrint) {
//                        _svf.VrsOut(getFDeviation3(), rec3.getDeviation());
//                    }
//                }
//                final String key;
//                if (_param._isGakunen) {
//                    key = ALL3;
//                } else {
//                    key = ALL3 + student.courseKey();
//                }
//                final AverageDat avg3 = (AverageDat) avgDatOther.get(key);
////                if (_param._isConvertScoreTo100) {
////                    if (null != convScore) {
////                        _svf.VrsOut(getFAverage3(),  convScore.getAvg("3", student));
////                        _svf.VrsOut(getFMaxScore3(), convScore.getHighscoreAvg("3", student));
////                    }
////                } else {
//                    if (null != avg3) {
//                        _svf.VrsOut(getFAverage3(),  avg3.getAvgStr());
//                        _svf.VrsOut(getFMaxScore3(), avg3._highScore.toString());
//                    }
////                }
//                if (null != avg3) {
//                    _svf.VrsOut(getFExaminee3(), avg3._count.toString());
//                }
//            }

//            private void printRecordRec5(final Map recordOther, final Map avgDatOther, final ConvertedScore convScore, Student student) {
//                final Record rec5 = (Record) recordOther.get(ALL5);
//                if (null != rec5) {
////                    _svf.VrsOut(getFScore5(), _param._isConvertScoreTo100 ? rec5.getAvg() : rec5.getScore());
//                    _svf.VrsOut(getFScore5(), rec5.getScore());
//                    _svf.VrsOut(getFRank5(),      rec5.getRank());
//                    if (_param._deviationPrint) {
//                        _svf.VrsOut(getFDeviation5(), rec5.getDeviation());
//                    }
//                }
//                final String key;
//                if (_param._isGakunen) {
//                    key = ALL5;
//                } else {
//                    key = ALL5 + student.courseKey();
//                }
//                final AverageDat avg5 = (AverageDat) avgDatOther.get(key);
////                if (_param._isConvertScoreTo100) {
////                    if (null != convScore) {
////                        _svf.VrsOut(getFAverage5(),  convScore.getAvg("5", student));
////                        _svf.VrsOut(getFMaxScore5(), convScore.getHighscoreAvg("5", student));
////                    }
////                } else {
//                    if (null != avg5) {
//                        _svf.VrsOut(getFAverage5(),  avg5.getAvgStr());
//                        _svf.VrsOut(getFMaxScore5(), avg5._highScore.toString());
//                    }
////                }
//                if (null != avg5) {
//                    _svf.VrsOut(getFExaminee5(), avg5._count.toString());
//                }
//            }
            
            private void printRecordRec9(final Param param, final Map recordOther, final Map avgDatOther, Student student) {
                final Record rec9 = (Record) recordOther.get(ALL9);
                if (null != rec9) {
                    _svf.VrsOut(getFScore9(), rec9.getScore());
                    _svf.VrsOut(getFRank9(),      rec9.getRank());
                    if ("1".equals(param._deviationPrint)) {
                        _svf.VrsOut(getFDeviation9(), rec9.getDeviation());
                    }
                }
                final String key;
                if (param.isGakunen()) {
                    key = ALL9;
                } else if (param.isCourseGroup()) {
                    key = ALL9 + student.courseGroupKey();
                } else {
                    key = ALL9 + student.courseKey();
                }
                final AverageDat avg9 = (AverageDat) avgDatOther.get(key);
                if (null != avg9) {
                    _svf.VrsOut(getFAverage9(),  avg9.getAvgStr());
                    _svf.VrsOut(getFMaxScore9(), avg9._highScore.toString());
                    _svf.VrsOut(getFExaminee9(), avg9._count.toString());
                    if ("2".equals(param._deviationPrint)) {
                        _svf.VrsOut(getFDeviation9(), avg9.getStdDevStr());
                    }
                }
            }
        }

        private class SubRecTable extends MainRecTable {
            String getFClass() { return "PRE_CLASS"; }
            String getFSubClass() { return "PRE_SUBCLASS"; }
            String getFScore() { return "PRE_SCORE"; }
            String getFRank() { return "PRE_RANK"; }
            String getFDeviation() { return "PRE_DEVIATION"; }
            String getFAverage() { return "PRE_AVERAGE"; }
            String getFMaxScore() { return "PRE_MAX_SCORE"; }
            /** 受験者数 */
            String getFExaminee() { return "PRE_EXAMINEE"; }

            /*
             * 3教科
             */
            String getFScore3() { return "PRE_SCORE3"; }
            String getFRank3() { return "PRE_RANK3"; }
            String getFDeviation3() { return "PRE_DEVIATION3"; }
            String getFAverage3() { return "PRE_AVERAGE3"; }
            String getFMaxScore3() { return "PRE_MAX_SCORE3"; }
            String getFExaminee3() { return "PRE_EXAMINEE3"; }

            /*
             * 5教科
             */
            String getFScore5() { return "PRE_SCORE5"; }
            String getFRank5() { return "PRE_RANK5"; }
            String getFDeviation5() { return "PRE_DEVIATION5"; }
            String getFAverage5() { return "PRE_AVERAGE5"; }
            String getFMaxScore5() { return "PRE_MAX_SCORE5"; }
            String getFExaminee5() { return "PRE_EXAMINEE5"; }

            /*
             * 9教科
             */
            String getFScore9() { return "PRE_SCORE5"; }
            String getFRank9() { return "PRE_RANK5"; }
            String getFDeviation9() { return "PRE_DEVIATION5"; }
            String getFAverage9() { return "PRE_AVERAGE5"; }
            String getFMaxScore9() { return "PRE_MAX_SCORE5"; }
            String getFExaminee9() { return "PRE_EXAMINEE5"; }
        }
    }

    private static class Student implements Comparable {
        private final String _schregno;
        private final String _grade;
        private final String _hrClass;
        private final String _attendNo;
        private final String _hrName;

        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final String _coursegroupCd;

        private final String _name;

        /** 成績所見データ. */
        private final String _remark;

        /** 成績科目 */
        private final Map _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _record = new TreeMap();

        /** 成績データ。3教科,5教科用 */
        private final Map _recordOther = new HashMap();

        /** 前回の成績科目 */
        private final Map _beforeSubclasses = new TreeMap();

        /** 前回の成績データ。 */
        private final Map _beforeRecord = new TreeMap();

        /** 前回の成績データ。3教科,5教科用 */
        private final Map _beforeRecordOther = new HashMap();

        private Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String coursegroupCd,
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
            _coursegroupCd = coursegroupCd;
            _name = name;
            _remark = remark;
        }
        
        public String attendSubclassCdKey() {
            return _grade + courseKey();
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String courseGroupKey() {
            return "0" + _coursegroupCd + "0000";
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
    private static class Class {
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
    private static class SubClass implements Comparable {
        private final String _code;
        private final String _name;
        private final String _abbv;

        /** 合併情報を持っているか */
        private boolean _hasCombined;
        /** 合併先か? */
        private boolean _isSaki;
        /** 合併元か? */
        private boolean _isMoto;

        public SubClass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public SubClass(final String code) {
            this(code, "xxx", "yyy");
        }

        public String getClassKey(final Param param) {
            final String classCd;
            if ("1".equals(param._useCurriculumcd)) {
                final String[] split = StringUtils.split(_code, "-");
                classCd =  split[0] + "-" + split[1];
            } else {
                classCd = _code.substring(0, 2);
            }
            return classCd;
        }

        public String getClassCd(final Param param) {
            final String classCd;
            if ("1".equals(param._useCurriculumcd)) {
                final String[] split = StringUtils.split(_code, "-");
                classCd =  split[0];
            } else {
                classCd = _code.substring(0, 2);
            }
            return classCd;
        }

        public void setSaki() {
            _hasCombined = true;
            _isSaki = true;
        }

        public void setMoto() {
            _hasCombined = true;
            _isMoto = true;
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

    private static class Record {
        private final SubClass _subClass;
        private final Integer _score;
        private final Integer _scorePercent;
        private final BigDecimal _avg;
        private final Integer _rank;
        private final BigDecimal _deviation;

        private Record(
                final SubClass subClass,
                final Integer score,
                final Integer scorePercent,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation
        ) {
            _subClass = subClass;
            _score = score;
            _scorePercent = scorePercent;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
        }

        public String getScore() {
            return null == _score ? "" : _score.toString();
        }

        public String getAvg() {
            return null == _avg ? "" : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getRank() {
            return null == _rank ? "" : _rank.toString();
        }

        public String getDeviation() {
            return null == _deviation ? "" : _deviation.toString();
        }

        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }

    private static class AverageDat {
        private final SubClass _subClass;
        private final BigDecimal _avg;
        private final BigDecimal _avgPercent;
        private final BigDecimal _stdDev;
        private final Integer _highScore;
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
    
    private static class ScoreDistribution {

        public final static String[] _scoreKeys = new String[]{"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
        public final Set _keySubClasses = new HashSet();
        private final String _key;
        private final Map _distributions = new HashMap();
        
        private ScoreDistribution(String key) {
            _key = key;
        }
        
        private Map getSubclassDistributionMap(SubClass subClass) {
            return getSubclassDistributionMap(subClass._code);
        }
        
        private Map getSubclassDistributionMap(String subClassCd) {
            if (!_distributions.containsKey(subClassCd)) {
                _distributions.put(subClassCd, new HashMap());
            }
            return (Map) _distributions.get(subClassCd);
        }

        public void add(SubClass subClass, Integer score) {
            int scoreKeyInd = (score.intValue() / 10);
            if (scoreKeyInd <= _scoreKeys.length) {
                _keySubClasses.add(subClass);
                increment(subClass, _scoreKeys[scoreKeyInd]);
            }
        }
        
        private void increment(SubClass subClass, String scoreKey) {
            Integer count = getCount(subClass._code, scoreKey);
            getSubclassDistributionMap(subClass).put(scoreKey, new Integer(count.intValue() + 1));
        }
        
        public Integer getCount(String subClassCd, String scoreKey) {
            Map subclassScoreDist = getSubclassDistributionMap(subClassCd);
            final Integer count;
            if (subclassScoreDist.containsKey(scoreKey)) {
                count = (Integer) subclassScoreDist.get(scoreKey);
            } else {
                count = Integer.valueOf("0");
            }
            return count;
        }

        private String distStr() {
            StringBuffer stb = new StringBuffer();
            String comma = "";
            for (Iterator it = _keySubClasses.iterator(); it.hasNext();) {
                SubClass subClass = (SubClass) it.next();
                stb.append("[subClass=").append(subClass.toString());
                for (int i = 0; i < _scoreKeys.length; i++) {
                    String scoreKey = _scoreKeys[i];
                    Integer count = getCount(subClass._code, scoreKey);
                    stb.append(comma).append("\"").append(scoreKey).append("\"=").append(count);
                    comma = ", ";
                }
                stb.append("] ");
            }
            return stb.toString();
        }
        
        public int getFieldIndex(String scoreKey) {
            int ind = -1;
            for (int i = 0; i < _scoreKeys.length; i++) {
                if (_scoreKeys[i].equals(scoreKey)) {
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
            return " dist = (" + distStr() + ")";
        }
    }
    

    /** 「100点に換算する」場合に表示するデータ */
    private static class ConvertedScore {
    }
} // KNJD105C

// eof
