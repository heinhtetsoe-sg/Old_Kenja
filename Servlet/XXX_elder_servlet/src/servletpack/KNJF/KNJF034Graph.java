/*
 * $Id: 2f3ca961c990dbc5be0d520f56a62206e5573a8a $
 *
 * 作成日: 2011/12/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.TextAnchor;

import nao_package.db.DB2UDB;

/**
 * 発育の記録・視力検査の結果 (明治)
 *
 */
public class KNJF034Graph {
    
    private static Log log = LogFactory.getLog(KNJF034Graph.class);

    private static final BasicStroke mainStroke = new BasicStroke(2.0f);
    private static final BasicStroke subStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f, new float[] {6.0f, 3.0f}, 0.0f);
    
    private static final double HEIGHT_Y_MIN =  40.0;
    private static final double HEIGHT_Y_MAX = 190.0;
    private static final double WEIGHT_Y_MIN =   0.0;
    private static final double WEIGHT_Y_MAX = 150.0;
    
    private static final String fontname = "SansSerif";
    private static final Font font9 = new Font(fontname, Font.PLAIN, 10);
    private static final Font font12 = new Font(fontname, Font.BOLD, 12);
    private static final Font font18 = new Font(fontname, Font.BOLD, 18);
    private static final Font font22 = new Font(fontname, Font.BOLD, 22);
    private static final Font font24 = new Font(fontname, Font.BOLD, 24);

    private static final int FLG_HEIGHT = 0;
    private static final int FLG_WEIGHT = 1;
    

    /**
     * 学年平均と個人得点のレーダーチャートを作成する
     * @param Map
     * @return
     */
    public static JFreeChart createChart(final KNJF034.Student student, final KNJF034.Param param, final String sexName, final List list) {
        
        final int DATA_IDX0 = 0;
        final int DATA_IDX1 = 1;
        final int DATA_IDX2 = 2;
        final int DATA_IDX3 = 3;
        final int AXIS_IDX0 = 0;
        final int AXIS_IDX1 = 1;
        
        final XYPlot plot = new XYPlot();
        
        final DefaultXYDataset heightDataset = getDataSet(list, plot, FLG_HEIGHT, new double[] {+2, +1, 0, -1, -2, -2.5, -3}); // 身長 DATA_IDX0
        final DefaultXYDataset weightDataset = getDataSet(list, plot, FLG_WEIGHT, new double[] {+2, +1, 0, -1, -2}); // 体重 DATA_IDX1

        if ("1".equals(student._sex)) {
            plot.addAnnotation(getXYTextAnnotation("身長", font18, FLG_HEIGHT, 13.5, 185.0));
            plot.addAnnotation(getXYTextAnnotation("体重", font18, FLG_WEIGHT, 13.5,  85.0));
        } else {
            plot.addAnnotation(getXYTextAnnotation("身長", font18, FLG_HEIGHT, 13.5, 175.0));
            plot.addAnnotation(getXYTextAnnotation("体重", font18, FLG_WEIGHT, 13.5,  75.0));
        }

        final DefaultXYDataset studentHeightDataset = getStudentData(student, param, FLG_HEIGHT); // 生徒の身長 DATA_IDX2
        final DefaultXYDataset studentWeightDataset = getStudentData(student, param, FLG_WEIGHT); // 生徒の体重 DATA_IDX3

        final ValueAxis domainAxis = new NumberAxis(""); // 暦年齢
        domainAxis.setRange(0.0, 20.0);
        final TickUnits tickUnits = new TickUnits();
        tickUnits.add(new NumberTickUnit(1.0));
        domainAxis.setStandardTickUnits(tickUnits);
        domainAxis.setTickLabelFont(font12);
        final ValueAxis heightRangeAxis = getRangeAxis("身長（ｃｍ）", HEIGHT_Y_MIN, HEIGHT_Y_MAX, 10.0); // AXIS_IDX0
        final ValueAxis weightRangeAxis = getRangeAxis("体重（ｋｇ）", WEIGHT_Y_MIN, WEIGHT_Y_MAX, 10.0); // AXIS_IDX1
        final XYLineAndShapeRenderer renderer = createSplineRenderer(); // 平均データ描画用
        final XYLineAndShapeRenderer heightRenderer = createDefaultXYItemRenderer(1); // 生徒個人データ描画用
        final XYLineAndShapeRenderer weightRenderer = createDefaultXYItemRenderer(2); // 生徒個人データ描画用

        plot.setRenderer(DATA_IDX0, renderer);
        plot.setRenderer(DATA_IDX1, renderer);
        plot.setRenderer(DATA_IDX2, heightRenderer);
        plot.setRenderer(DATA_IDX3, weightRenderer);
        plot.setDomainAxis(domainAxis);
        plot.setDataset(DATA_IDX0, heightDataset);
        plot.setDataset(DATA_IDX1, weightDataset);
        plot.setDataset(DATA_IDX2, studentHeightDataset);
        plot.setDataset(DATA_IDX3, studentWeightDataset);
        plot.setRangeAxis(AXIS_IDX0, heightRangeAxis);
        plot.setRangeAxis(AXIS_IDX1, weightRangeAxis);
        plot.mapDatasetToRangeAxis(DATA_IDX0, AXIS_IDX0); // 身長の軸に関連付け
        plot.mapDatasetToRangeAxis(DATA_IDX1, AXIS_IDX1); // 体重の軸に関連付け
        plot.mapDatasetToRangeAxis(DATA_IDX2, AXIS_IDX0); // 身長の軸に関連付け
        plot.mapDatasetToRangeAxis(DATA_IDX3, AXIS_IDX1); // 体重の軸に関連付け
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        
        final JFreeChart chart = new JFreeChart(null, font24, plot, true);
        chart.setBackgroundPaint(Color.white);
        chart.setTextAntiAlias(true);
        chart.setAntiAlias(true);
        chart.setTitle("横断的標準身長・体重曲線 " + sexName + "（0-18歳）");
        chart.removeLegend(); // のぞいておく
//        final LegendTitle legend = chart.getLegend();
//        legend.setFrame(BlockBorder.NONE);
//        legend.setItemFont(chartFont);
//        legend.setPosition(RectangleEdge.BOTTOM);
        return chart;
    }
    
    public static JFreeChart createPercentileChart(final DB2UDB db2, final Map paramMap) {
        final String dataDiv = (String) paramMap.get("DATA_DIV");
        final KNJF034.Param param = (KNJF034.Param) paramMap.get("Param"); 
        final Map percentileDataListMap = getMappedMap(getMappedMap(HexamPhysicalPercentileDat.getHexamPhysicalPercentileList(db2, paramMap, param), dataDiv), paramMap.get("SEX"));
        final String title = (String) paramMap.get("TITLE");
        
        final int DATA_IDX0 = 0;
        final int DATA_IDX1 = 1;
        
        final XYPlot plot = new XYPlot();
        
//        log.info(" percentileDataListMap = " + percentileDataListMap);
        final double tickUnit = Double.parseDouble((String) paramMap.get("TICK_UNIT"));
        final KNJF034.Student student = (KNJF034.Student) paramMap.get("STUDENT");
        final DefaultXYDataset dataset = getPercentileDataSet(percentileDataListMap, plot, FLG_HEIGHT); // 身長 DATA_IDX0
        final DefaultXYDataset studentDataset = getStudentDataSet(param, student, dataDiv); // 生徒 DATA_IDX1

        final List yearList = getYearList(percentileDataListMap);
        if (!yearList.isEmpty()) {
            paramMap.put("USED_DATA_YEAR", yearList.get(0));
        }

        final ValueAxis domainAxis = new NumberAxis("（歳）"); // 暦年齢
        domainAxis.setLabelFont(font22);
        final List nenreiList = getNenreiList(percentileDataListMap);
        double xmin = 0.0;
        double xmax = 20.0;
        if (!nenreiList.isEmpty()) {
            xmin = ((Double) nenreiList.get(0)).doubleValue();
            xmax = ((Double) nenreiList.get(nenreiList.size() - 1)).doubleValue() + 0.96;
        }
        domainAxis.setRange(xmin, xmax);
        final TickUnits tickUnits = new TickUnits();
        tickUnits.add(new NumberTickUnit(1.0));
        domainAxis.setStandardTickUnits(tickUnits);
        domainAxis.setTickLabelFont(font18);

        final List valueList = getValueList(percentileDataListMap);
        final ValueAxis rangeAxis = getRangeAxis((String) paramMap.get("UNIT_LABEL"), xmin, xmax, tickUnit); // AXIS_IDX0
        if (!valueList.isEmpty()) {
            final double ymin = ((int) (((BigDecimal) valueList.get(0)).doubleValue() - tickUnit)) / (int) tickUnit * tickUnit;
            final double ymax = ((int) (((BigDecimal) valueList.get(valueList.size() - 1)).doubleValue() + tickUnit)) / (int) tickUnit * tickUnit - tickUnit * 0.1;
            rangeAxis.setRange(ymin, ymax);
        }
        final TickUnits rangeTickUnits = new TickUnits();
        rangeTickUnits.add(new NumberTickUnit(tickUnit));
        rangeAxis.setStandardTickUnits(rangeTickUnits);

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true);
        final Color lineColor = Color.black.brighter().brighter();
        for (int i = 0; i < percentileDataListMap.size(); i++) {
            renderer.setSeriesPaint(i, lineColor);
            renderer.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
            final double r = 0.5;
            renderer.setSeriesShape(i, new Ellipse2D.Double(-r, -r, 2 * r, 2 * r));
        }
        final XYLineAndShapeRenderer studentDataRenderer = createDefaultXYItemRenderer(2); // 生徒個人データ描画用
        
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));
        plot.setRangeMinorGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setDomainMinorGridlinesVisible(false);
        plot.setRenderer(0, renderer);
        plot.setRenderer(1, studentDataRenderer);
        plot.setDomainAxis(domainAxis);
        plot.setDataset(DATA_IDX0, dataset);
        plot.setDataset(DATA_IDX1, studentDataset);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        plot.setRangeAxis(rangeAxis);
        
        final JFreeChart chart = new JFreeChart(null, font24, plot, true);
        chart.setBorderStroke(new BasicStroke(2.0f));
        chart.setBackgroundPaint(Color.white);
        chart.setTextAntiAlias(true);
        chart.setAntiAlias(true);
        chart.setTitle(title);
        chart.removeLegend();

        return chart;
    }
    
    private static List getYearList(final Map percentileDataListMap) {
        final Set yearSet = new TreeSet();
        for (final Iterator it = percentileDataListMap.keySet().iterator(); it.hasNext();) {
            final String percentile = (String) it.next();
            final List list = (List) percentileDataListMap.get(percentile);
            for (int i = 0; i < list.size(); i++) {
                final HexamPhysicalPercentileDat dat = (HexamPhysicalPercentileDat) list.get(i);
                yearSet.add(dat._year);
            }
        }
        return new ArrayList(yearSet);
    }
    
    private static List getNenreiList(final Map percentileDataListMap) {
        final Set nenreiSet = new TreeSet();
        for (final Iterator it = percentileDataListMap.keySet().iterator(); it.hasNext();) {
            final String percentile = (String) it.next();
            final List list = (List) percentileDataListMap.get(percentile);
            for (int i = 0; i < list.size(); i++) {
                final HexamPhysicalPercentileDat dat = (HexamPhysicalPercentileDat) list.get(i);
                nenreiSet.add(new Double(dat._nenrei));
            }
        }
        return new ArrayList(nenreiSet);
    }
    
    private static List getValueList(final Map percentileDataListMap) {
        final Set valueSet = new TreeSet();
        for (final Iterator it = percentileDataListMap.keySet().iterator(); it.hasNext();) {
            final String percentile = (String) it.next();
            final List list = (List) percentileDataListMap.get(percentile);
            for (int i = 0; i < list.size(); i++) {
                final HexamPhysicalPercentileDat dat = (HexamPhysicalPercentileDat) list.get(i);
                if (null != dat._valueBd) {
                    valueSet.add(dat._valueBd);
                }
            }
        }
        return new ArrayList(valueSet);
    }

    private static DefaultXYDataset getPercentileDataSet(final Map percentileDataListMap, final XYPlot plot, final int tgtFlg) {
        final DefaultXYDataset dataset = new DefaultXYDataset();

        int percentileScale = 0;
        for (final Iterator it = percentileDataListMap.keySet().iterator(); it.hasNext();) {
            final String percentile = (String) it.next();
            if (NumberUtils.isNumber(percentile)) {
                BigDecimal percentileBd = new BigDecimal(percentile);
                if (percentileBd.setScale(1).doubleValue() != percentileBd.setScale(0).doubleValue()) {
                    percentileScale = 1;
                    break;
                }
            }
        }
        
        for (final Iterator it = percentileDataListMap.keySet().iterator(); it.hasNext();) {
            final String percentile = (String) it.next();
            final List list = (List) percentileDataListMap.get(percentile);
            double[][] data = new double[2][list.size()];
            double last = 0.0;
            for (int i = 0; i < list.size(); i++) {
                final HexamPhysicalPercentileDat dat = (HexamPhysicalPercentileDat) list.get(i);
                data[0][i] = dat._nenrei;
                data[1][i] = last = null == dat._valueBd ? 0.0 : dat._valueBd.doubleValue();
            }
            final String annotText = getPercnetileAnnotationText(new BigDecimal(percentile).setScale(percentileScale).toString()); 
            dataset.addSeries(annotText, data);
//            log.info(" data " + percentile + "  = " + ArrayUtils.toString(data));
            plot.addAnnotation(getPercentileXYTextAnnotation(annotText, font9, tgtFlg, 17.3, last));
        }
        return dataset;
    }
    

    private static DefaultXYDataset getStudentDataSet(final KNJF034.Param param, final KNJF034.Student student, final String dataDiv) {
        final DefaultXYDataset dataset = new DefaultXYDataset();

        double[][] studentData = new double[2][student._medexamDetNoDatList.size()];
        for (int i = 0; i < student._medexamDetNoDatList.size(); i++) {
            final KNJF034.MedexamDetNoDat mdnd = (KNJF034.MedexamDetNoDat) student._medexamDetNoDatList.get(i);
            String val = null;
            if ("1".equals(dataDiv)) {
                val = mdnd._height;
            } else if ("2".equals(dataDiv)) {
                val = mdnd._weight;
            }
            if (!NumberUtils.isNumber(val) || null == mdnd._date) {
                continue;
            }
            final double nenrei = KNJF034.getNenrei(student, mdnd._date, param._year, mdnd._year);
            studentData[0][i] = nenrei;
            studentData[1][i] = new BigDecimal(val).doubleValue();
        }
        dataset.addSeries("生徒", studentData);
        
        return dataset;
    }
    
    private static XYTextAnnotation getPercentileXYTextAnnotation(final String text, final Font font, final int tgtFlg, double x, double y) {
        if (FLG_HEIGHT == tgtFlg) { // 身長
        } else if (FLG_WEIGHT == tgtFlg) { // 体重
            y = weightYpointToHeightYpoint(y); // 座標の取り方が異なるが対応していないため手動で計算する
        }
        final XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
        annotation.setFont(font);
        annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        return annotation;
    }
    
    private static String getPercnetileAnnotationText(final String percentile) {
        return percentile + "%";
    }
    
    private static XYSplineRenderer createSplineRenderer() {
        final XYSplineRenderer renderer = new XYSplineRenderer(5);
        renderer.setBaseShapesVisible(false);
        for (int i = 0; i < 7; i++) {
            renderer.setSeriesPaint(i, Color.black); // 線の色はすべて黒
        }
        return renderer;
    }
    
    private static XYLineAndShapeRenderer createDefaultXYItemRenderer(int shapeFlg) {
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(); // 生徒個人データ描画用
        renderer.setSeriesPaint(0, Color.black); // 線の色は黒
        renderer.setUseFillPaint(true);
        final double r = 4.0;
        renderer.setSeriesShape(0, new Ellipse2D.Double(-r, -r, 2 * r, 2 * r));
        if (1 == shapeFlg) {
            renderer.setBaseFillPaint(Color.black);
        } else if (2 == shapeFlg) {
            renderer.setBaseFillPaint(Color.white);
        }
        return renderer;
    }
    
    private static double weightYpointToHeightYpoint(double weightYpoint) {
        return (weightYpoint - WEIGHT_Y_MIN) / (WEIGHT_Y_MAX - WEIGHT_Y_MIN) * (HEIGHT_Y_MAX - HEIGHT_Y_MIN) + HEIGHT_Y_MIN;
    }
    
    private static DefaultXYDataset getStudentData(final KNJF034.Student student, final KNJF034.Param param, final int tgtFlg) {
        final List dataList = new ArrayList();
        for (final Iterator it = student._medexamDetNoDatList.iterator(); it.hasNext();) {
            final KNJF034.MedexamDetNoDat mdnd = (KNJF034.MedexamDetNoDat) it.next();
            String tgt = null;
            if (FLG_HEIGHT == tgtFlg) {
                tgt = mdnd._height;
            } else if (FLG_WEIGHT == tgtFlg) {
                tgt = mdnd._weight;
            }
            if (mdnd._date == null || !NumberUtils.isNumber(tgt)) {
                continue;
            }
            dataList.add(new double[] {KNJF034.getNenrei(student, mdnd._date, param._year, mdnd._year), Double.parseDouble(tgt)});
        }
        final DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            final double[] dat = (double[]) dataList.get(i);
            data[0][i] = dat[0];
            data[1][i] = dat[1];
        }
        dataset.addSeries("0", data);
        return dataset;
    }

    private static DefaultXYDataset getDataSet(final List list, final XYPlot plot, final int tgtFlg, final double[] ks) {
        final DefaultXYDataset dataset = new DefaultXYDataset();
        for (int k = 0; k < ks.length; k++) {
            double[][] data = new double[2][list.size()];
            double last = 0.0;
            final double slideNenrei = ks[k] == 0.0 ? 0 : (k == 0 || k == ks.length - 1) ? 0.5 : 1.0;
            int slideCount = 0;
            for (int i = 0; i < list.size(); i++) {
                final KNJF034.HexamPhysicalAvgDat dat = (KNJF034.HexamPhysicalAvgDat) list.get(i);
                data[0][i] = dat._nenrei;
                if (FLG_HEIGHT == tgtFlg) { // 身長
                    if (null != dat._heightAvg && null != dat._heightSd) {
                        data[1][i] = last = dat._heightAvg.doubleValue() + dat._heightSd.doubleValue() * ks[k];
                    }
                } else if (FLG_WEIGHT == tgtFlg) { // 体重
                    if (null != dat._weightAvg && null != dat._weightSd) {
                        data[1][i] = last = dat._weightAvg.doubleValue() + dat._weightSd.doubleValue() * ks[k];
                    }
                }
                if (dat._nenreiYear <= slideNenrei) {
                    slideCount++;
                }
            }
//            data = toMovingAverage(data, 6);
            if (ks[k] != 0.0) { // 0歳〜1歳が見えにくいので"平均"以外は0歳を表示しない
                double[][] dataSlide = new double[2][list.size() - slideCount];
                for (int i = 0; i < list.size() - slideCount; i++) {
                    dataSlide[0][i] = data[0][i + slideCount];
                    dataSlide[1][i] = data[1][i + slideCount];
                }
                data = dataSlide;
            }
            
            final String annotText = getAnnotationText(ks[k]); 
            dataset.addSeries(annotText, data);
            plot.addAnnotation(getXYTextAnnotation(annotText, font12, tgtFlg, 17.68, last));
        }
        return dataset;
    }
    
    private static XYTextAnnotation getXYTextAnnotation(final String text, final Font font, final int tgtFlg, double x, double y) {
        if (FLG_HEIGHT == tgtFlg) { // 身長
        } else if (FLG_WEIGHT == tgtFlg) { // 体重
            y = weightYpointToHeightYpoint(y); // 座標の取り方が異なるが対応していないため手動で計算する
        }
        final XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
        annotation.setFont(font);
        annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        return annotation;
    }

    private static ValueAxis getRangeAxis(final String label, final double rangeMin, final double rangeMax, final double tickUnit) {
        final ValueAxis rangeAxis = new NumberAxis(label);
        rangeAxis.setRange(rangeMin, rangeMax);
        final TickUnits tickUnits = new TickUnits();
        tickUnits.add(new NumberTickUnit(tickUnit));
        rangeAxis.setStandardTickUnits(tickUnits);
        rangeAxis.setTickLabelFont(font18);
        rangeAxis.setMinorTickMarksVisible(true);
        rangeAxis.setMinorTickCount(2);
        rangeAxis.setLabelFont(font22);
        return rangeAxis;
    }

    private static String getAnnotationText(final double v) {
        if (v == 0.0) {
            return " 平均";
        }
        return ((v < 0 ? "-" : "+") + new BigDecimal(Math.abs(v)).toString() + "SD");
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

    public static class HexamPhysicalPercentileDat {
        final String _year;
        final String _nenreiYear;
        final String _nenreiMonth;
        final double _nenrei;
        final String _percentile;
        final String _value;
        BigDecimal _valueBd;

        HexamPhysicalPercentileDat(
            final String year,
            final String nenreiYear,
            final String nenreiMonth,
            final String percentile,
            final String value
        ) {
            _year = year;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = Double.parseDouble(_nenreiYear) + Double.parseDouble(_nenreiMonth) / 12;
            _percentile = percentile;
            _value = value;
            if (NumberUtils.isNumber(_value)) {
                _valueBd = new BigDecimal(_value);
            }
        }
        
        public String toString() {
            return "HexamPhysicalPercentileDat(" + _nenrei + ", " + _percentile + " = " + _valueBd + ")";
        }

        public static Map getHexamPhysicalPercentileList(final DB2UDB db2, final Map paramMap, final KNJF034.Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Object pYear = paramMap.get("YEAR");
            final Object pDataDiv = paramMap.get("DATA_DIV");
            final Object pSex = paramMap.get("SEX");
            
            final String key = "PERCENTILE_DATA:" + pYear + "-" + pDataDiv + "-" + pSex;
            try {
                if (null == param._sessionMap.get(key)) {
                    final String sql = sql(pYear, pDataDiv, pSex);
                    log.info(" sql = " + sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    final Map map = new HashMap();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String nenreiYear = rs.getString("NENREI_YEAR");
                        final String nenreiMonth = rs.getString("NENREI_MONTH");
                        final String percentile = rs.getString("PERCENTILE");
                        final String value = rs.getString("VALUE");
                        final HexamPhysicalPercentileDat hexamphysicalpercentile = new HexamPhysicalPercentileDat(year, nenreiYear, nenreiMonth, percentile, value);
                        
                        getMappedList(getMappedMap(getMappedMap(map, rs.getString("DATA_DIV")), rs.getString("SEX")), rs.getString("PERCENTILE")).add(hexamphysicalpercentile);
                    }
                    param._sessionMap.put(key,  map);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return (Map) param._sessionMap.get(key);
        }

        public static String sql(final Object year, final Object dataDiv, final Object sex) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_PERCENTILE_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_PERCENTILE_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("      T1.YEAR ");
            stb.append("      , T1.DATA_DIV ");
            stb.append("      , T1.SEX ");
            stb.append("      , T1.PERCENTILE  ");
            stb.append("      , T1.NENREI_YEAR  ");
            stb.append("      , T1.NENREI_MONTH  ");
            stb.append("      , T1.VALUE  ");
            stb.append("  FROM HEXAM_PHYSICAL_PERCENTILE_DAT T1  ");
            stb.append("  INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            String where = null;
            if (null != dataDiv) {
                where = null == where ? " WHERE " : " AND ";
                stb.append("      " + where + " T1.DATA_DIV = '" + dataDiv + "' ");
            }
            if (null != sex) {
                where = null == where ? " WHERE " : " AND ";
                stb.append("      " + where + " T1.SEX = '" + sex + "' ");
            }
            stb.append("  ORDER BY  ");
            stb.append("      T1.YEAR ");
            stb.append("      , T1.DATA_DIV ");
            stb.append("      , T1.SEX ");
            stb.append("      , T1.PERCENTILE  ");
            stb.append("      , T1.NENREI_YEAR  ");
            stb.append("      , T1.NENREI_MONTH  ");
            return stb.toString();
        }
    }

    
//    private static double[][] toMovingAverage(double[][] data, int range) {
//        final int len = data[0].length;
//        double[][] rtn = new double[data.length][len];
//        for (int i = 0; i < len; i++) {
//            rtn[0][i] = data[0][i];
//            double n = 0;
//            int cnt = 0;
//            for (int k = i - range; k <= i + range; k++) {
//                if (k < 0 || k >= len) {
//                    continue;
//                }
//                n += data[1][k];
//                cnt += 1;
//            }
//            rtn[1][i] = n / cnt;
//        }
//        return rtn;
//    }

}

// eof

