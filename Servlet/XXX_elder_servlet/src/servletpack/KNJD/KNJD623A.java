/*
 * $Id: c2153aa2d90d8c7597c9aa0721c8622ebe9049bd $
 *
 * 作成日: 2011/05/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.VerticalAlignment;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 考査平均点/度数分布 印刷
 * @version $Id: c2153aa2d90d8c7597c9aa0721c8622ebe9049bd $
 */
public class KNJD623A {

    private static final Log log = LogFactory.getLog(KNJD623A.class);
    
    private static final String _333333 = "0000333333";
    private static final String _555555 = "0000555555";
    private static final String _999999 = "0000999999";

    private boolean _hasData;

    private Param _param;

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
            removeImageFiles();
            log.fatal("Done.");
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJD623A.frm", 1);
        
        printHeader(db2, svf);
        
        printRecordAverage(db2, svf);

        try {
            printTotalDist(db2, svf);
        } catch (final Error e) {
            log.error("グラフ描画処理中にエラー発生.", e);
        } catch (final Exception e) {
            log.error("exception!", e);
        }

        printSubclassDist(db2, svf);
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    private void printHeader(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("year", _param._nendoString);
        svf.VrsOut("TITLE", _param._gradeName + " " + _param._semestername + " " + _param._testname + "の平均点／度数分布");
        svf.VrsOut("ymd1", _param._dateString);
        svf.VrsOut("GRADE_NAME", _param._gradeName);
    }
    
    private void printRecordAverage(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = sqlRecordAvearge();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            int idx = 1;
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String avg = !NumberUtils.isNumber(rs.getString("AVG")) ? "" : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                final String count = rs.getString("COUNT");
                if (_333333.equals(subclassCd)) {
                    svf.VrsOut("3CLASS_AVERAGE", avg);
                    svf.VrsOut("3CLASS_TOTAL_STUDENT", count);
                } else if (_555555.equals(subclassCd)) {
                    svf.VrsOut("5CLASS_AVERAGE", avg);
                    svf.VrsOut("5CLASS_TOTAL_STUDENT", count);
                } else if (_999999.equals(subclassCd)) {
                    svf.VrsOut("ALL_CLASS_AVERAGE", avg);
                    svf.VrsOut("ALL_CLASS_TOTAL_STUDENT", count);
                } else {
                    svf.VrsOutn("SUBCLASS", idx, rs.getString("SUBCLASSNAME"));
                    svf.VrsOutn("AVERAGE", idx, avg);
                    svf.VrsOutn("TOTAL_STUDENT", idx, count);
                    idx += 1;
                }
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String sqlRecordAvearge() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASSES AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.TESTKINDCD, ");
        stb.append("     T1.TESTITEMCD, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T3.SUBCLASSABBV AS SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("          AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlyear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testkindcd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T2.SUBCLASSCD, ");
        }
        stb.append("     T2.SUBCLASSNAME, ");
        stb.append("     T1.AVG, ");
        stb.append("     T1.COUNT ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASSES T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.GRADE = T1.GRADE ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append(" WHERE ");
        stb.append("     T1.AVG_DIV = '1' "); // 学年平均
        stb.append("     AND T1.HR_CLASS = '000' ");
        stb.append("     AND T1.COURSECD = '0' ");
        stb.append("     AND T1.MAJORCD = '000' ");
        stb.append("     AND T1.COURSECODE = '0000' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        } else {
            stb.append("     T1.SUBCLASSCD ");
        }
        return stb.toString();
    }
    
    private List getSubclassLevelList(final DB2UDB db2, final String trgtsubclasscd, final int max, final int kizami) {
        final List subclassLevelList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sqlSubclassDist(trgtsubclasscd, max, kizami);
            log.debug(" sql subclass dist =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String subclasscd = rs.getString("SUBCLASSCD");
                final Integer level = (Integer) rs.getObject("LEVEL");
                final Integer count = (Integer) rs.getObject("COUNT");
                if (null == subclasscd) {
                    continue;
                }
                
                SubclassLevel subclassLevel = null;
                
                for (final Iterator it = subclassLevelList.iterator(); it.hasNext() && subclassLevel == null;) {
                    final SubclassLevel sl = (SubclassLevel) it.next();
                    if (subclasscd.equals(sl._subclasscd)) {
                        subclassLevel = sl;
                    }
                }
                if (null == subclassLevel) {
                    subclassLevel = new SubclassLevel(subclasscd, null == trgtsubclasscd ? rs.getString("SUBCLASSNAME") : null);
                    subclassLevelList.add(subclassLevel);
                }
                subclassLevel.add(level, count);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return subclassLevelList;
    }
    
    private void printTotalDist(final DB2UDB db2, final Vrw32alp svf) {
        final int max = 800;
        final int kizami = 20;
        
        final List subclassLevelList = getSubclassLevelList(db2, _param._totalsubclasscd, max, kizami);
        if (subclassLevelList.isEmpty()) {
            return;
        }
        final SubclassLevel subclassAll = (SubclassLevel) subclassLevelList.get(0);

        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
        for (int lev = 1; lev <= max / kizami; lev++) {
            final String upperScore = String.valueOf(lev == 1 ? max : (max - kizami * (lev - 1)) - 1);
            final String lowerScore = String.valueOf(max - kizami * lev);
            final Integer level = new Integer(lev);
            final Integer count = (Integer) subclassAll._levelCountMap.get(level);
            final String title = format(lowerScore) + " 〜 " + format(upperScore);
            scoreDataset.addValue(count, "得点分布", title);
//            log.debug(" title = " + title + " , count = " + count);
        }
        
        // チャート作成
        final JFreeChart chart = createBarChart(scoreDataset);

        // グラフのファイルを生成
        final File outputFile = graphImageFile(chart, 1482, 2306);
        _graphFiles.add(outputFile);

        // グラフの出力
        svf.VrsOut("GRAPH", outputFile.toString());
    }
    
    private String format(final Object s) {
        if (null == s || s.toString().length() == 0) {
            return "   ";
        } else if (s.toString().length() == 1) {
            return "  " + s; 
        } else if (s.toString().length() == 2) {
            return " " + s;
        } else {
            return s.toString();
        }
    }
    
    private JFreeChart createBarChart(
            final DefaultCategoryDataset scoreDataset
    ) {
        final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.HORIZONTAL, false, false, false);
        final Font font = new Font("ＭＳ ゴシック", Font.PLAIN, 12);
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));
        
        final NumberAxis numberAxis = new NumberAxis();
        numberAxis.setTickUnit(new NumberTickUnit(5));
        numberAxis.setTickLabelsVisible(true);
        numberAxis.setTickLabelFont(font);
        numberAxis.setRange(0, 25);
        plot.setRangeAxis(numberAxis);
        
        final CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setTickLabelsVisible(true);
        categoryAxis.setTickLabelFont(font);
        categoryAxis.setCategoryMargin(0);
        categoryAxis.setUpperMargin(0.005);
        categoryAxis.setLowerMargin(0.005);
        
        final org.jfree.chart.renderer.category.BarRenderer renderer = new BarRenderer(); // InnerClass
        renderer.setSeriesPaint(0, new Color(225, 225, 225));
        renderer.setSeriesOutlinePaint(0, Color.black);
        renderer.setItemLabelFont(font);
        renderer.setItemLabelsVisible(true);
        final ItemLabelPosition pos = new ItemLabelPosition(ItemLabelAnchor.INSIDE3, TextAnchor.CENTER);
        renderer.setPositiveItemLabelPosition(pos);
        renderer.setPositiveItemLabelPositionFallback(pos);
        renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelAnchorOffset(10);
        renderer.setMaximumBarWidth(0.025);
        plot.setRenderer(renderer);
        
        int total = 0;
        for (final Iterator itr = scoreDataset.getRowKeys().iterator(); itr.hasNext();) {
            final Comparable row = (Comparable) itr.next();
            for (final Iterator itc = scoreDataset.getColumnKeys().iterator(); itc.hasNext();) {
                final Comparable col = (Comparable) itc.next();
                final Number n = scoreDataset.getValue(row, col);
                if (null != n) {
                    total += n.intValue();
                }
            }
        }
        chart.setBackgroundPaint(Color.white);
        final String text = "合計人数 " + String.valueOf(total);
        chart.setTitle(new TextTitle(text, font, Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM, new RectangleInsets(10, 40, 0, 0)));
        
        return chart;
    }
    
    private void printSubclassDist(final DB2UDB db2, final Vrw32alp svf) {
        final int max = 100;
        final int kizami = 5;
        final int toltan = 21;
        
        final List subclassLevelList = getSubclassLevelList(db2, null, max, kizami);
        
        int idx = 1;
        for (final Iterator it = subclassLevelList.iterator(); it.hasNext();) {
            final SubclassLevel subclassLevel = (SubclassLevel) it.next();
            svf.VrsOut("SUBCLASS" + idx, subclassLevel._subclassname);
            
            int total = 0;
            for (int lev = 1; lev <= max / kizami; lev++) {
                final Integer level = new Integer(lev);
                final Integer count = (Integer) subclassLevel._levelCountMap.get(level);
                if (null != count) {
                    svf.VrsOutn("TOTAL_NUM_SUBCLASS" + idx, lev, count.toString());
                    total += count.intValue();
                }
            }
            
            svf.VrsOutn("TOTAL_NUM_SUBCLASS" + idx, toltan, String.valueOf(total));
            idx += 1;
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
    
    private void removeImageFiles() {
        for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
            final File imageFile = (File) it.next();
            if (null == imageFile) {
                continue;
            }
            log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
        }
    }
    
    private String sqlSubclassDist(final String subclassCd, final int maxScore, final int kizami) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCORES AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SCORE, ");
        stb.append("     CASE WHEN T1.SCORE = " + maxScore + " THEN 1 ");
        stb.append("          ELSE (" + maxScore + " - 1 - T1.SCORE) / " + kizami + " + 1 ");
        stb.append("     END AS LEVEL ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlyear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testkindcd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        if (null == subclassCd) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN ('" + _333333 + "', '" + _555555 + "', '" + _999999  + "') ");
            } else {
                stb.append("     AND T1.SUBCLASSCD NOT IN ('" + _333333 + "', '" + _555555 + "', '" + _999999  + "') ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '" + subclassCd + "' ");
            } else {
                stb.append("     AND T1.SUBCLASSCD = '" + subclassCd + "' ");
            }
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        if (null == subclassCd) {
            stb.append("     MAX(T2.SUBCLASSABBV) AS SUBCLASSNAME, ");
        }
        stb.append("     T1.LEVEL, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     SCORES T1 ");
        if (null == subclassCd) {
            stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("           AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("           AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
        }
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        stb.append("     T1.LEVEL ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        stb.append("     T1.LEVEL ");
        return stb.toString();
    }
    
    private class SubclassLevel { 
        final String _subclasscd;
        final String _subclassname;
        final Map _levelCountMap;
        SubclassLevel(final String subclasscd, final String subclassname) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _levelCountMap = new HashMap();
        }
        public void add(final Integer level, final Integer count) {
            _levelCountMap.put(level, count);
        }
    }
    
    /**
     * @see org.jfree.chart.renderer.category.BarRenderer
     */
    private static class BarRenderer extends org.jfree.chart.renderer.category.BarRenderer {
        
        /**
         * Draws an item label.  This method is overridden so that the bar can be 
         * used to calculate the label anchor point.
         * 
         * @param g2  the graphics device.
         * @param data  the dataset.
         * @param row  the row.
         * @param column  the column.
         * @param plot  the plot.
         * @param generator  the label generator.
         * @param bar  the bar.
         * @param negative  a flag indicating a negative value.
         */
        protected void drawItemLabel(Graphics2D g2,
                                     CategoryDataset data,
                                     int row,
                                     int column,
                                     CategoryPlot plot,
                                     CategoryItemLabelGenerator generator,
                                     Rectangle2D bar,
                                     boolean negative) {
                                         
            String label = generator.generateLabel(data, row, column);
            if (label == null) {
                return;  // nothing to do   
            }
            
            Font labelFont = getItemLabelFont(row, column);
            g2.setFont(labelFont);
            Paint paint = getItemLabelPaint(row, column);
            g2.setPaint(paint);

            // find out where to place the label...
            ItemLabelPosition position = getPositiveItemLabelPosition(row, -1);

            // work out the label anchor point...
            Point2D anchorPoint = calculateLabelAnchorPoint(
                    position.getItemLabelAnchor(), bar, plot.getOrientation());
            
            if (isInternalAnchor(position.getItemLabelAnchor())) {
                Shape bounds = TextUtilities.calculateRotatedStringBounds(label, 
                        g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                        position.getTextAnchor(), position.getAngle(),
                        position.getRotationAnchor());
                
                if (bounds != null) {
                    if (!bar.contains(bounds.getBounds2D())) {
                        position = getPositiveItemLabelPositionFallback();
                        if (position != null) {
                            anchorPoint = calculateLabelAnchorPoint(
                                    position.getItemLabelAnchor(), bar, 
                                    plot.getOrientation());
                        }
                    }
                }
            }
            
            if (position != null) {
                TextUtilities.drawRotatedString(label, g2, 
                        (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                        position.getTextAnchor(), position.getAngle(), 
                        position.getRotationAnchor());
            }        
        }
        
        
        /**
         * Returns <code>true</code> if the specified anchor point is inside a bar.
         * 
         * @param anchor  the anchor point.
         * 
         * @return A boolean.
         */
        private boolean isInternalAnchor(ItemLabelAnchor anchor) {
            boolean ret = anchor == ItemLabelAnchor.CENTER 
                   || anchor == ItemLabelAnchor.INSIDE1
                   || anchor == ItemLabelAnchor.INSIDE2
                   || anchor == ItemLabelAnchor.INSIDE3
                   || anchor == ItemLabelAnchor.INSIDE4
                   || anchor == ItemLabelAnchor.INSIDE5
                   || anchor == ItemLabelAnchor.INSIDE6
                   || anchor == ItemLabelAnchor.INSIDE7
                   || anchor == ItemLabelAnchor.INSIDE8
                   || anchor == ItemLabelAnchor.INSIDE9
                   || anchor == ItemLabelAnchor.INSIDE10
                   || anchor == ItemLabelAnchor.INSIDE11
                   || anchor == ItemLabelAnchor.INSIDE12;
            return ret;
        }
        
        /**
         * Calculates the item label anchor point.
         *
         * @param anchor  the anchor.
         * @param bar  the bar.
         * @param orientation  the plot orientation.
         *
         * @return The anchor point.
         */
        private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor,
                                                  Rectangle2D bar, 
                                                  PlotOrientation orientation) {

            Point2D result = null;
            double offset = (bar.getMaxX() - bar.getX()) - 12; // getItemLabelAnchorOffset(); // BarRendererから変更：アンカーポイント（開始位置）を定位置とする
            double x0 = bar.getX() - offset;
            double x1 = bar.getX();
            double x2 = bar.getX() + offset;
            double x3 = bar.getCenterX();
            double x4 = bar.getMaxX() - offset;
            double x5 = bar.getMaxX();
            double x6 = bar.getMaxX() + offset;

            double y0 = bar.getMaxY() + offset;
            double y1 = bar.getMaxY();
            double y2 = bar.getMaxY() - offset;
            double y3 = bar.getCenterY();
            double y4 = bar.getMinY() + offset;
            double y5 = bar.getMinY();
            double y6 = bar.getMinY() - offset;

            if (anchor == ItemLabelAnchor.CENTER) {
                result = new Point2D.Double(x3, y3);
            }
            else if (anchor == ItemLabelAnchor.INSIDE1) {
                result = new Point2D.Double(x4, y4);
            }
            else if (anchor == ItemLabelAnchor.INSIDE2) {
                result = new Point2D.Double(x4, y4);
            }
            else if (anchor == ItemLabelAnchor.INSIDE3) {
                result = new Point2D.Double(x4, y3);
            }
            else if (anchor == ItemLabelAnchor.INSIDE4) {
                result = new Point2D.Double(x4, y2);
            }
            else if (anchor == ItemLabelAnchor.INSIDE5) {
                result = new Point2D.Double(x4, y2);
            }
            else if (anchor == ItemLabelAnchor.INSIDE6) {
                result = new Point2D.Double(x3, y2);
            }
            else if (anchor == ItemLabelAnchor.INSIDE7) {
                result = new Point2D.Double(x2, y2);
            }
            else if (anchor == ItemLabelAnchor.INSIDE8) {
                result = new Point2D.Double(x2, y2);
            }
            else if (anchor == ItemLabelAnchor.INSIDE9) {
                result = new Point2D.Double(x2, y3);
            }
            else if (anchor == ItemLabelAnchor.INSIDE10) {
                result = new Point2D.Double(x2, y4);
            }
            else if (anchor == ItemLabelAnchor.INSIDE11) {
                result = new Point2D.Double(x2, y4);
            }
            else if (anchor == ItemLabelAnchor.INSIDE12) {
                result = new Point2D.Double(x3, y4);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE1) {
                result = new Point2D.Double(x5, y6);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE2) {
                result = new Point2D.Double(x6, y5);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE3) {
                result = new Point2D.Double(x6, y3);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE4) {
                result = new Point2D.Double(x6, y1);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE5) {
                result = new Point2D.Double(x5, y0);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE6) {
                result = new Point2D.Double(x3, y0);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE7) {
                result = new Point2D.Double(x1, y0);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE8) {
                result = new Point2D.Double(x0, y1);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE9) {
                result = new Point2D.Double(x0, y3);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE10) {
                result = new Point2D.Double(x0, y5);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE11) {
                result = new Point2D.Double(x1, y6);
            }
            else if (anchor == ItemLabelAnchor.OUTSIDE12) {
                result = new Point2D.Double(x3, y6);
            }

            return result;

        }
    };

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlyear;
        final String _ctrlsemester;
        final String _ctrldate;
        final String _semester;
        final String _grade;
        final String _testkindcd;
        final String _totalsubclasscd;
        final String _nendoString;
        final String _dateString;
        final String _semestername;
        final String _testname;
        final String _gradeName;
        private final String _useCurriculumcd;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlyear = request.getParameter("CTRL_YEAR");
            _ctrlsemester = request.getParameter("CTRL_SEMESTER");
            _ctrldate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _testkindcd = request.getParameter("TESTKINDCD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            
            final String graph = request.getParameter("GRAPH");
            if ("2".equals(graph)) {
                _totalsubclasscd = _555555;
            } else if ("3".equals(graph)) {
                _totalsubclasscd = _333333;
            } else { // "1".equals(graph)
                _totalsubclasscd = _999999;
            }
            
            final boolean seirekiFlg = getSeirekiFlg(db2);
            final int nendo = Integer.parseInt(KNJ_EditDate.b_year(_ctrldate));
            _nendoString = seirekiFlg ? nendo + "年度" : KenjaProperties.gengou(nendo) + "度";
            _dateString = dateString(_ctrldate, seirekiFlg);
            _semestername = getSemestername(db2, _ctrlyear, _semester);
            _gradeName = getGradeName(db2, _ctrlyear, _grade);
            _testname = getTestitemname(db2, _ctrlyear, request.getParameter("COUNTFLG"), _semester, _testkindcd);
        }
        
        private String dateString(final String date, final boolean seirekiFlg) {
            if (seirekiFlg) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);  // 証明日付
            }
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean seirekiFlg = false;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String getSemestername(final DB2UDB db2, final String year, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String semestername = "";
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    semestername = null == rs.getString("SEMESTERNAME") ? "" : rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semestername;
        }

        private String getTestitemname(final DB2UDB db2, final String year, final String countflg, final String semester, final String testkindcd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String testitemname = "";
            try {
                final String table = "TESTITEM_MST_COUNTFLG_NEW".equals(countflg) ? "TESTITEM_MST_COUNTFLG_NEW" : "TESTITEM_MST_COUNTFLG";
                final String where = "TESTITEM_MST_COUNTFLG_NEW".equals(countflg) ? " AND SEMESTER = '" + semester + "' " : "";
                
                final String sql = "SELECT TESTITEMNAME FROM " + table + " WHERE YEAR = '" + year + "' AND TESTKINDCD || TESTITEMCD = '" + testkindcd + "' " + where;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    testitemname = null == rs.getString("TESTITEMNAME") ? "" : rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemname;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String gradename = "";
            try {
                final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    gradename = null == rs.getString("GRADE_NAME1") ? "" : rs.getString("GRADE_NAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradename;
        }
    }
}

// eof

