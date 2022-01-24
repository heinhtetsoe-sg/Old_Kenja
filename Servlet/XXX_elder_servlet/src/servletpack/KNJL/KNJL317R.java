/*
 * $Id: 515169f3eff184015403e430553b667e30b76d5f $
 *
 * 作成日: 2019/01/09
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１５Ｙ＞  得点分布表
 **/
public class KNJL317R {

    private static final Log log = LogFactory.getLog(KNJL317R.class);

    private boolean _hasData;

    Param _param;

    private final int VAL = 10;

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
            removeImageFiles();
        }
    }

    private void setCourseCount(final DB2UDB db2, final List courseList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getDistributionCountSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                /*中学はコースが１つなので「1」固定のダミー*/
                final String examCourseCd = "1";

                final Course course = getCourse(courseList, examCourseCd);
                if (null == course) {
                    continue;
                }

                final String scoreLevel = rs.getString("SCORE_LEVEL");
                if (!NumberUtils.isNumber(scoreLevel)) {
                    continue;
                }

                final Count c = course.getCount(Integer.parseInt(scoreLevel));
                if (null == c) {
                    log.error(" not found Count : " + scoreLevel);
                    continue;
                }
                final int count = rs.getInt("COUNT");
                if ("1".equals(rs.getString("SHDIV"))) {
                	c._heigan += count;
                } else if ("2".equals(rs.getString("SHDIV"))) {
                	c._tangan += count;
                }
                c._jukensha += count;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private Course getCourse(final List courseList, final String examCourseCd) {
        Course course = null;
        for (Iterator it = courseList.iterator(); it.hasNext();) {
            Course c = (Course) it.next();
            if (c._examCourseCd.equals(examCourseCd)) {
                course = c;
            }
        }
        return course;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List courseList = createCourseList(db2);
        setCourseCount(db2, courseList);
        for (final Iterator itCourse = courseList.iterator(); itCourse.hasNext();) {
            final Course course = (Course) itCourse.next();

            svf.VrSetForm("KNJL317R.frm", 4);
            svf.VrsOut("NENDO", _param._entexamYear + "年度");
            svf.VrsOut("DATE", _param._dateString);
            svf.VrsOut("TITLE", _param._title);
            svf.VrsOut("SUBTITLE", _param._subTitle);
            svf.VrsOut("SUBJECT", _param._isKasan ? " (加点あり) " : " (加点なし)");

            final File graphFile = getGraphFile(course);
            svf.VrsOut("CHART", graphFile.toString());
            _graphFiles.add(graphFile);


            final Count total = new Count(-1);
            for (final Iterator it = course._counts.iterator(); it.hasNext();) {
                final Count c = (Count) it.next();

                svf.VrsOut("POINT",       String.valueOf(c._score));
                svf.VrsOut("EXAM_NUM",    String.valueOf(c._jukensha));
                svf.VrsOut("MORE_SCHOOL", String.valueOf(c._heigan));
                svf.VrsOut("ONE_SCHOOL",  String.valueOf(c._tangan));
                svf.VrEndRecord();

                total._jukensha += c._jukensha;
                total._heigan += c._heigan;
                total._tangan += c._tangan;
            }
            svf.VrsOut("TOTAL_NAME", "合計");
            svf.VrsOut("TOTAL_EXAM_NUM",    String.valueOf(total._jukensha));
            svf.VrsOut("TOTAL_MORE_SCHOOL", String.valueOf(total._heigan));
            svf.VrsOut("TOTAL_ONE_SCHOOL",  String.valueOf(total._tangan));
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private File getGraphFile(final Course course) {

        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();

        int maxCount = 0;
        if (course._counts.size() > 0) {
            for (final ListIterator it = course._counts.listIterator(course._counts.size()); it.hasPrevious(); ) {
                final Count count = (Count) it.previous();
                final Integer jukensha = new Integer(count._jukensha);
                scoreDataset.addValue(jukensha, "0", count._score);
                maxCount = Math.max(maxCount, jukensha.intValue());
            }
        }

        // チャート作成
        final JFreeChart chart = createLineChart(scoreDataset, maxCount);

        // グラフのファイルを生成
        final File graphFile = graphImageFile(chart, 4364 - 1622, 2532 - 792);
        return graphFile;
    }

    private JFreeChart createLineChart(final DefaultCategoryDataset scoreDataset, int maxCount) {
        final int kizamiY = maxCount < 50 ? 5 : maxCount < 100 ? 10 : maxCount < 200 ?  20 : maxCount < 300 ? 30 : maxCount < 500 ? 50 : 100;
        final double minY = 0;
        final double maxY = (maxCount / kizamiY + 1) * kizamiY + 10;

        final JFreeChart chart = ChartFactory.createLineChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlineStroke(new BasicStroke(1.0f)); // 目盛りの太さ
        plot.setRangeGridlinePaint(Color.black);

        final Font font = new Font("TimesRoman", Font.PLAIN, 15);
        // 追加する折れ線グラフの表示設定
        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setItemLabelsVisible(true);
        renderer2.setPaint(Color.gray);
        renderer2.setShape(new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
        renderer2.setItemLabelFont(font);
        renderer2.setSeriesStroke(0, new BasicStroke(3.0f));
        plot.setRenderer(renderer2);

        // Y軸の設定
        final NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setTickLabelsVisible(true);
        numberAxis.setTickLabelFont(font);
        numberAxis.setLabelFont(font);
        numberAxis.setLabel("人数");
        numberAxis.setTickUnit(new NumberTickUnit(kizamiY, new DecimalFormat("0")));
        numberAxis.setRange(minY, maxY);

        // X軸の設定
        final Font font2 = new Font("TimesRoman", Font.PLAIN, 12);
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(font2);
        domainAxis.setLabelFont(font);
        domainAxis.setLabel("点数");
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setCategoryMargin(0.95);

        chart.setBackgroundPaint(Color.white);

        return chart;
    }

    /*中学はコースが１つなので「1」固定のダミー*/
    private List createCourseList(final DB2UDB db2) {
        List courseList = new ArrayList();
        final String examCourseCd = "1";
        final String examCourseName = "";
        final Course course = new Course(examCourseCd, examCourseName);
        courseList.add(course);

        return courseList;
    }

    private String getDistributionCountSql() {
        String scoreField = "2";
        if (_param._isKasan) {
            if ("1".equals(_param._outkeisya)) {
                scoreField = "3";
            } else {
                scoreField = "1";
            }
        } else {
            if ("1".equals(_param._outkeisya)) {
                scoreField = "4";
            } else {
                scoreField = "2";
            }
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCORES AS( ");
        stb.append(" SELECT   ");
        stb.append("     BUN013.REMARK" + _param._testDiv + " AS SHDIV, ");
        stb.append("     BASE.ENTDIV, ");
        stb.append("     RECEPT.TOTAL" + scoreField + " AS SCORE, ");
        stb.append("     RECEPT.TOTAL" + scoreField + " / 10 * 10 AS SCORE_LEVEL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON BUN013.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND BUN013.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND BUN013.EXAMNO = RECEPT.EXAMNO ");
        stb.append("         AND BUN013.SEQ = '013' ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("     AND RECEPT.TOTAL" + scoreField + " IS NOT NULL ");
        stb.append("     AND VALUE(RECEPT.JUDGEDIV, '') <> '4' ");
        stb.append("     AND VALUE(RECEPT.ATTEND_ALL_FLG, '') = '1' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SHDIV, ");
        stb.append("     T1.ENTDIV, ");
        stb.append("     T1.SCORE_LEVEL, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM  ");
        stb.append("     SCORES T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHDIV, ");
        stb.append("     T1.ENTDIV, ");
        stb.append("     T1.SCORE_LEVEL ");

        return stb.toString();
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

    private class Course {
        final String _examCourseCd;
        final String _examCourseName;
        final List _counts;
        public Course(final String examCourseCd, final String examCourseName) {
            _examCourseCd = examCourseCd;
            _examCourseName = examCourseName;

            _counts = new ArrayList();
            for (int score = _param._maxScore.intValue(); score >= _param._minScore.intValue(); score -= VAL) {
                _counts.add(new Count(score));
            }
        }

        public Count getCount(int score) {
            final Integer key;
            final int level = score / 10 * 10;
            if (level < _param._minScore.intValue()) {
                key = _param._minScore;
            } else if (level >= _param._maxScore.intValue() + 10) {
                key = _param._maxScore;
            } else {
                key = new Integer(level);
            }
            for (Iterator it = _counts.iterator(); it.hasNext();) {
                final Count count = (Count) it.next();
                if (count._score.equals(key)) {
                    return count;
                }
            }
            return null;
        }
        public String toString() {
            return "[" + _examCourseCd + " : " + _examCourseName + "]";
        }
    }

    private static class Count {
        final Integer _score;
        int _jukensha;
        int _tangan;
        int _heigan;
        public Count(final int score) {
            _score = new Integer(score);
        }
        public String toString() {
            return "[" + _score + ":(" + _jukensha + ", " + _tangan + ", " + _heigan + ")";
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71698 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final Integer _maxScore;
        final Integer _minScore;
        final String _dateString;
        final String _outkeisya;    // 傾斜配点出力   1:する 2:しない
        final boolean _isKasan;    // 加算点含む

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            _maxScore = null == request.getParameter("MAX_SCORE") ? new Integer(440) : new Integer(Integer.parseInt(request.getParameter("MAX_SCORE")) / 10 * 10);
            _minScore = null == request.getParameter("MIN_SCORE") ? new Integer(100) : new Integer(Integer.parseInt(request.getParameter("MIN_SCORE")) / 10 * 10);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            final Calendar cal = Calendar.getInstance();
            final String loginDate = request.getParameter("LOGIN_DATE");
            _dateString = sdf.format(Date.valueOf(loginDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
            _outkeisya = request.getParameter("OUTKEISYA");
            _isKasan = "on".equals(request.getParameter("INC_KASAN"));
        }

        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "2".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof
