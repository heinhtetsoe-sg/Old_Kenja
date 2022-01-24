/*
 * $Id: 2a32fb4ec20a512338e35380e135a5e4a43f43d8 $
 *
 * 作成日: 2009/12/20
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 武蔵入試 判定会議資料(表3)
 */
public class KNJL374M {

    private static final Log log = LogFactory.getLog(KNJL374M.class);

    private boolean _hasData;

    Param _param;
    
    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();
    
    private final String KEY_PASS = "0-PASS";
    private final String KEY_EXAMINEE = "1-EXAMINEE";
    private final String KEY_NOT_PASS = "2-NOT_PASS";
    
    private final String TOTAL = "TOTAL";
    private final String SUBCLASS = "SUBCLASS";
    
    private final String[] _totalScoreKeys = new String[]{
            "0-99",
            "100-119",
            "120-139",
            "140-159",
            "160-179",
            "180-199",
            "200-219",
            "220-239",
            "240-259",
            "260-279",
            "280-300",
    };
    
    private final String _testSubclassCdKokugo = "1";
    private final String _testSubclassCdSansu = "2";
    private final String _testSubclassCdRika = "3";
    private final String _testSubclassCdShakai = "4";
    private final String[] _testSubclassCds = new String[]{
            _testSubclassCdKokugo,
            _testSubclassCdSansu,
            _testSubclassCdRika,
            _testSubclassCdShakai,
    };

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
            }
            svf.VrQuit();
        }

    }
    
    public void printMain(DB2UDB db2, Vrw32alp svf) {
        svf.VrSetForm("KNJL374M.frm", 1);
        _hasData = false;

        final String[] years = new String[]{_param._year, _param._lastYear};
        final int[] graphIndexBase = new int[]{0, 4};
        final boolean[] useJudgement = new boolean[]{false, true};
        
        for (int yearIndex = 0; yearIndex < 2; yearIndex++) {
            
            log.debug(" --- entexamyear = " + years[yearIndex] + " --- ");
            _param.setTestSubclasses(db2, years[yearIndex]);
            printTotal(db2, svf, years[yearIndex], yearIndex+1, useJudgement[yearIndex]);
            printSubclass(db2, svf, years[yearIndex], graphIndexBase[yearIndex], useJudgement[yearIndex]);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
        removeImageFiles();
    }
    
    public void printTotal(DB2UDB db2, Vrw32alp svf, String entexamYear, int yearIndex, boolean useJudgement) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._ctrlDate) + "度");
        svf.VrsOut("YEAR" + yearIndex, KenjaProperties.gengou(Integer.parseInt(entexamYear)) + "年入試");
        
        // 総点の人数分布表
        try {
            String sql = getTotalDistributionSql(entexamYear, useJudgement);
            log.debug(" totalDistribution sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            Map fields = new HashMap();
            for (int i = 0; i < _totalScoreKeys.length; i++) {
                String field = String.valueOf(i+1);
                svf.VrsOut("Y" + yearIndex + "SCORE" + field, _totalScoreKeys[i]);
                svf.VrsOut("Y" + yearIndex + "NUM" + field, "0"); // "0"で初期化
                fields.put(_totalScoreKeys[i], field);
            }
            
            while (rs.next()) {
                if (KEY_EXAMINEE.equals(rs.getString("KEY"))) {
                    final String scoreLevel = rs.getString("SCORE_KEY");
                    final String field = (String) fields.get(scoreLevel);
                    if (field == null) {
                        continue;
                    }
                    String count = rs.getString("COUNT");
                    svf.VrsOut("Y" + yearIndex + "NUM" + field, count);
                    // log.debug(" distribution total " + scoreLevel + " (" + field + ") = " + count);
                }
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        
        // 総点表の得点(最大、最小、平均)
        try {
            String sql = getMaxMinAvgSql(entexamYear, TOTAL, null, useJudgement);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            svf.VrsOut("Y" + yearIndex + "PASSMAX1", "0");
            svf.VrsOut("Y" + yearIndex + "PASSMIN1", "0");
            svf.VrsOut("Y" + yearIndex + "PASSAVE1", "0.0");
            svf.VrsOut("Y" + yearIndex + "NOTPASSMAX1", "0");
            svf.VrsOut("Y" + yearIndex + "NOTPASSMIN1", "0");
            svf.VrsOut("Y" + yearIndex + "NOTPASSAVE1", "0.0");
            svf.VrsOut("Y" + yearIndex + "TOTALMAX1", "0");
            svf.VrsOut("Y" + yearIndex + "TOTALMIN1", "0");
            svf.VrsOut("Y" + yearIndex + "TOTALAVE1", "0.0");

            while (rs.next()) {
                final String key = rs.getString("KEY");
                final String max = rs.getString("MAX");
                final String min = rs.getString("MIN");
                final String avg = rs.getString("AVG");

                if (KEY_PASS.equals(key)) {
                    svf.VrsOut("Y" + yearIndex + "PASSMAX1", max);
                    svf.VrsOut("Y" + yearIndex + "PASSMIN1", min);
                    svf.VrsOut("Y" + yearIndex + "PASSAVE1", avg);
                } else if (KEY_NOT_PASS.equals(key)) {
                    svf.VrsOut("Y" + yearIndex + "NOTPASSMAX1", max);
                    svf.VrsOut("Y" + yearIndex + "NOTPASSMIN1", min);
                    svf.VrsOut("Y" + yearIndex + "NOTPASSAVE1", avg);
                } else if (KEY_EXAMINEE.equals(key)) {
                    svf.VrsOut("Y" + yearIndex + "TOTALMAX1", max);
                    svf.VrsOut("Y" + yearIndex + "TOTALMIN1", min);
                    svf.VrsOut("Y" + yearIndex + "TOTALAVE1", avg);
                }
                log.debug(" total (key,max,min,avg) = ("+  key + "," + max + "," + min + "," + avg +")");
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        
        // 出欠カウント
        try {
            String sql = getExamineeDivSql(entexamYear);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            final String examineeDivAttend = "1";
            final String examineeDivAbsent = "2";
            final String examineeDivTotal = "9";
            
            svf.VrsOut("APPLI" + yearIndex, "0");
            svf.VrsOut("ABSENCE" + yearIndex, "0");
            svf.VrsOut("EXAM" + yearIndex, "0");
            
            while (rs.next()) {
                final String examineeDiv = rs.getString("EXAMINEE_DIV");
                final String field;
                if (examineeDivAttend.equals(examineeDiv)) {
                    field = "EXAM" + yearIndex;
                } else if (examineeDivAbsent.equals(examineeDiv)) {
                    field = "ABSENCE" + yearIndex;
                } else if (examineeDivTotal.equals(examineeDiv)) {
                    field = "APPLI" + yearIndex;
                } else {
                    field = null;
                }
                svf.VrsOut(field, rs.getString("COUNT"));
                log.debug(" (examineediv, count) = (" + examineeDiv + "," + rs.getString("COUNT") + ")");
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    /**
     * 各テスト科目のグラフ、最高点・最低点・平均点を表示する
     * @param db2
     * @param svf
     * @param year
     * @param graphIndexBase
     */
    public void printSubclass(DB2UDB db2, Vrw32alp svf, String year, int graphIndexBase, boolean useJudgement) {

        _param.clearTestSubclassData();

        PreparedStatement ps = null;
        ResultSet rs = null;
        // 分布取得
        try {
            String sql = getSubclassSql(year, useJudgement);
            log.debug(" sql subclass = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {

                String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                
                TestSubclass testSubclass = _param.getTestSubclass(testSubclassCd);
                
                if (testSubclass == null) {
                    log.debug(" TestSubclassがありません。: " + testSubclassCd);
                    continue;
                }
                
                String key = rs.getString("KEY");
                Integer count = Integer.valueOf(rs.getString("COUNT"));
                
                if (rs.getString("SCORE_KEY") != null) {
                    testSubclass.addDataSet(count, key, rs.getString("SCORE_KEY"));
                }
            }
            
        } catch (SQLException e) {
            log.error("Exception:", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        // 最高点・最低点・平均点の設定
        for (int i = 0; i < _testSubclassCds.length; i++) {

            String testSubclassCd = _testSubclassCds[i];
            TestSubclass testSubclass = _param.getTestSubclass(testSubclassCd);
            
            if (testSubclass == null) {
                continue;
            }
            
            try {
                String sql = getMaxMinAvgSql(year, SUBCLASS, testSubclass._code, useJudgement);
                log.debug(" " + testSubclass._code + "  sql MaxMinAvg = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {

                    final String key = rs.getString("KEY");
                    final int max = rs.getInt("MAX");
                    final int min = rs.getInt("MIN");
                    final String avg = rs.getString("AVG");
                    
                    testSubclass.setMaxMinAvg(key, max, min, avg);
                }
                
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        for (int i = 1; i <= 4; i++) {
            final int graphIndex = (i + graphIndexBase);
            // 最高点・最低点・平均点クリア
            svf.VrsOut("TOTALMAX" + graphIndex, "0");
            svf.VrsOut("TOTALMIN" + graphIndex, "0");
            svf.VrsOut("TOTALAVE" + graphIndex, "0.0");
            svf.VrsOut("PASSMAX" + graphIndex, "0");
            svf.VrsOut("PASSMIN" + graphIndex, "0");
            svf.VrsOut("PASSAVE" + graphIndex, "0.0");
        }

        for (int i = 0; i < _testSubclassCds.length; i++) {
            TestSubclass testSubclass = _param.getTestSubclass(_testSubclassCds[i]);
            
            if (testSubclass == null) {
                continue;
            }
            
            // log.debug(testSubclass._dataset.getColumnKeys());
            // グラフの出力
            final int graphIndex = (Integer.parseInt(testSubclass._code) + graphIndexBase);
            
            // チャート作成
            final JFreeChart chart = createBarChart(testSubclass);

            // グラフのファイルを生成
            final int dotWidth = 1350;
            final int dotHeight = 880;
            final File outputFile = graphImageFile(chart, dotWidth, dotHeight);
            svf.VrsOut("CLASSABBV1_1_" + graphIndex, testSubclass._name);
            svf.VrsOut("CHART1_" + graphIndex, outputFile.toString());
            _graphFiles.add(outputFile);
            
            if (testSubclass._passMin == 1000) testSubclass._passMin = 0;
            if (testSubclass._notPassMin == 1000) testSubclass._notPassMin = 0;
            if (testSubclass._examineeMin == 1000) testSubclass._examineeMin = 0;
            
            //log.debug(" PASS     : " + testSubclass._passMax + " , " + testSubclass._passMin + " , " + testSubclass._passAvg);
            //log.debug(" NOT PASS : " + testSubclass._maxNotPass + " , " + testSubclass._minNotPass + " , " + testSubclass._avgNotPass);
            //log.debug(" EXAMINEE : " + testSubclass._examineeMax + " , " + testSubclass._examineeMin + " , " + testSubclass._examineeAvg);
            

            // 最高点・最低点・平均点表示
            svf.VrsOut("TOTALMAX" + graphIndex, String.valueOf(testSubclass._examineeMax));
            svf.VrsOut("TOTALMIN" + graphIndex, String.valueOf(testSubclass._examineeMin));
            svf.VrsOut("TOTALAVE" + graphIndex, String.valueOf(testSubclass._examineeAvg));
            svf.VrsOut("PASSMAX" + graphIndex, String.valueOf(testSubclass._passMax));
            svf.VrsOut("PASSMIN" + graphIndex, String.valueOf(testSubclass._passMin));
            svf.VrsOut("PASSAVE" + graphIndex, String.valueOf(testSubclass._passAvg));
            _hasData = true;
        }
    }
    
    /**
     * 総合点の分布のSQL
     * @param entexamYear 入試年度
     * @return
     */
    private String getTotalDistributionSql(String entexamYear, boolean useJudgement) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCORE_KEY AS  ( ");
        stb.append(" SELECT ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T1.TOTAL4, ");
        stb.append("     T3.JUDGEMENT, ");
        stb.append("     (CASE ");
        stb.append("      WHEN T1.TOTAL4 < 100 THEN '" + _totalScoreKeys[0] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 120 THEN '" + _totalScoreKeys[1] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 140 THEN '" + _totalScoreKeys[2] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 160 THEN '" + _totalScoreKeys[3] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 180 THEN '" + _totalScoreKeys[4] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 200 THEN '" + _totalScoreKeys[5] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 220 THEN '" + _totalScoreKeys[6] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 240 THEN '" + _totalScoreKeys[7] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 260 THEN '" + _totalScoreKeys[8] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 280 THEN '" + _totalScoreKeys[9] + "' ");
        stb.append("      WHEN T1.TOTAL4 < 300 THEN '" + _totalScoreKeys[10] + "' ");
        stb.append("      END ) AS SCORE_KEY ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.RECEPTNO ");
        stb.append(" ) ");
        // 合格者
        stb.append(" SELECT ");
        stb.append("     '" + KEY_PASS + "' AS KEY, SCORE_KEY, COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_SCORE_KEY ");
        stb.append(" WHERE ");
        if (useJudgement) {
            stb.append("     VALUE(JUDGEMENT, '0') = '1' ");
        } else {
            stb.append("     TOTAL4 >= " + _param._passScore + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     SCORE_KEY ");
        // 不合格者
        stb.append(" UNION ALL");
        stb.append(" SELECT ");
        stb.append("     '" + KEY_NOT_PASS + "' AS KEY, SCORE_KEY, COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_SCORE_KEY ");
        stb.append(" WHERE ");
        if (useJudgement) {
            stb.append("     VALUE(JUDGEMENT, '0') <> '1' ");
        } else {
            stb.append("     TOTAL4 < " + _param._passScore + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     SCORE_KEy ");
        // 受験者
        stb.append(" UNION ALL");
        stb.append(" SELECT ");
        stb.append("     '" + KEY_EXAMINEE + "' AS KEY, SCORE_KEY, COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_SCORE_KEY ");
        stb.append(" GROUP BY ");
        stb.append("     SCORE_KEY ");
        return stb.toString();
    }

    /**
     * 欠席者数、受験者数を取得するSQL
     * @param entexamYear
     * @return
     */
    private String getExamineeDivSql(String entexamYear) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_EXAMINEE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.ENTEXAMYEAR, T1.APPLICANTDIV, T1.TESTDIV, T2.EXAM_TYPE, T1.EXAMNO, T2.EXAMINEE_DIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_DESIRE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T2.EXAM_TYPE = '1' ");
        stb.append(" ) ");
        // 欠席者
        stb.append(" SELECT ");
        stb.append("     EXAMINEE_DIV, COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_EXAMINEE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, EXAMINEE_DIV ");
        // 計
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '9' AS EXAMINEE_DIV, COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_EXAMINEE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE ");
        return stb.toString();
    }

    /**
     * 総点の最高点、最低点、平均点を取得するSQL
     * @param entexamYear
     * @param target 総点か各科目か
     * @param testSubclassCd テスト科目コード
     * @param useJudgement 合格判定にENTEXAM_APPLICANTBASE_DAT.JUDGEMENTを使用するか
     * @return
     */
    private String getMaxMinAvgSql(String entexamYear, String target, String testSubclassCd, boolean useJudgement) {
        StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_SCORE AS ( ");
        if (TOTAL.equals(target)) {
            stb.append(" SELECT ");
            stb.append("     T2.ENTEXAMYEAR, ");
            stb.append("     T2.APPLICANTDIV, ");
            stb.append("     T2.TESTDIV, ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     T2.TOTAL4 AS SCORE, ");
            stb.append("     T2.TOTAL4, ");
            stb.append("     T3.JUDGEMENT ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT T2 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T2.TESTDIV ");
            stb.append("         AND T3.EXAMNO = T2.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     T2.ENTEXAMYEAR = '" + entexamYear + "' ");
            stb.append("     AND T2.APPLICANTDIV = '" + _param._applicantDiv +"' ");
            stb.append("     AND T2.TESTDIV = '1' ");
            stb.append("     AND T2.TOTAL4 IS NOT NULL ");
        } else if (SUBCLASS.equals(target)) {
            stb.append(" SELECT ");
            stb.append("     T2.ENTEXAMYEAR, ");
            stb.append("     T2.APPLICANTDIV, ");
            stb.append("     T2.TESTDIV, ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     T4.SCORE, ");
            stb.append("     T2.TOTAL4, ");
            stb.append("     T3.JUDGEMENT ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT T2 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T2.TESTDIV ");
            stb.append("         AND T3.EXAMNO = T2.EXAMNO ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T4 ON T4.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         AND T4.TESTDIV = T2.TESTDIV ");
            stb.append("         AND T4.EXAM_TYPE = T2.EXAM_TYPE ");
            stb.append("         AND T4.RECEPTNO = T2.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     T2.ENTEXAMYEAR = '" + entexamYear + "' ");
            stb.append("     AND T2.APPLICANTDIV = '" + _param._applicantDiv +"' ");
            stb.append("     AND T2.TESTDIV = '1' ");
            stb.append("     AND T4.TESTSUBCLASSCD = '" + testSubclassCd + "' ");
        }
        
        stb.append(" ) ");
        // 受験者
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV, '" + KEY_EXAMINEE + "' AS KEY, ");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN, ");
        stb.append("     DECIMAL(ROUND(AVG(DECIMAL(SCORE)),1), 4, 1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_SCORE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV ");
        // 合格者
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV, '" + KEY_PASS + "' AS KEY, ");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN, ");
        stb.append("     DECIMAL(ROUND(AVG(DECIMAL(SCORE)),1), 4, 1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_SCORE ");
        stb.append(" WHERE ");
        if (useJudgement) {
            stb.append("     VALUE(JUDGEMENT, '0') = '1' ");
        } else {
            stb.append("     TOTAL4 >= " + _param._passScore + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV ");
        // 不合格者
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV, '" + KEY_NOT_PASS + "' AS KEY,");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN, ");
        stb.append("     DECIMAL(ROUND(AVG(DECIMAL(SCORE)),1), 4, 1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_SCORE ");
        stb.append(" WHERE ");
        if (useJudgement) {
            stb.append("     VALUE(JUDGEMENT, '0') <> '1' ");
        } else {
            stb.append("     TOTAL4 < " + _param._passScore + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, APPLICANTDIV, TESTDIV ");
        return stb.toString();
    }
    

    /**
     * 対象の受験者の分布を取得するSQLを得る
     * @param entexamyear 入試年度
     * @param useJudgement 合格判定にENTEXAM_APPLICANTBASE_DAT.JUDGEMENTを使用するか
     * @return 　
     */
    private String getSubclassSql(String entexamyear, boolean useJudgement) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERFECT AS ( ");
        stb.append("  SELECT ");
        stb.append("      ENTEXAMYEAR, ");
        stb.append("      APPLICANTDIV, ");
        stb.append("      TESTDIV, ");
        stb.append("      TESTSUBCLASSCD, ");
        stb.append("      PERFECT, ");
        stb.append("      (CASE WHEN PERFECT >= 100 THEN 10 ELSE 5 END) AS KIZAMI ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_PERFECT_MST ");
        stb.append("  WHERE ");
        stb.append("      COURSECD = '1' ");
        stb.append("      AND MAJORCD = '001' ");
        stb.append("      AND EXAMCOURSECD = '0001' ");
        stb.append(" ), T_SCORE_KEY AS ( ");
        stb.append("  SELECT ");
        stb.append("      T1.ENTEXAMYEAR, ");
        stb.append("      T1.APPLICANTDIV, ");
        stb.append("      T1.TESTDIV, ");
        stb.append("      T2.RECEPTNO, ");
        stb.append("      T1.TESTSUBCLASSCD, ");
        stb.append("      T3.JUDGEMENT, ");
        stb.append("      T2.TOTAL4, ");
        stb.append("      T1.SCORE, ");
        stb.append("      T4.PERFECT, ");
        stb.append("      T4.KIZAMI, ");
        stb.append("      (CASE WHEN T1.SCORE = 0 THEN 1 ");
        stb.append("            WHEN MOD(T1.SCORE, T4.KIZAMI) = 0 ");
        stb.append("                THEN T1.SCORE / T4.KIZAMI ");
        stb.append("                ELSE T1.SCORE / T4.KIZAMI + 1 END) * T4.KIZAMI AS SCORE_KEY ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_SCORE_DAT T1 ");
        stb.append("      INNER JOIN ENTEXAM_RECEPT_DAT T2 ON ");
        stb.append("          T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("          AND T2.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("          AND T2.RECEPTNO = T1.RECEPTNO ");
        stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON ");
        stb.append("          T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("          AND T3.EXAMNO = T2.EXAMNO ");
        stb.append("      LEFT JOIN PERFECT T4 ON ");
        stb.append("         T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T4.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
        stb.append("  WHERE ");
        stb.append("      T1.ENTEXAMYEAR = '" + entexamyear + "' ");
        stb.append("      AND T1.SCORE IS NOT NULL ");
        stb.append("      AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND T1.TESTDIV = '1' ");
        stb.append("  ORDER BY ");
        stb.append("      T1.ENTEXAMYEAR, T1.TESTSUBCLASSCD, T2.RECEPTNO ");
        stb.append(" ) ");
        /** */
        stb.append(" SELECT ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     '" + KEY_EXAMINEE + "' AS KEY, ");
        stb.append("      SCORE_KEY, ");
        stb.append("      COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_SCORE_KEY ");
        stb.append(" GROUP BY ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     SCORE_KEY ");
        /** */
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     '" + KEY_PASS + "' AS KEY, ");
        stb.append("      SCORE_KEY, ");
        stb.append("      COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_SCORE_KEY ");
        stb.append(" WHERE ");
        if (useJudgement) {
            stb.append("     JUDGEMENT = '1' ");
        } else {
            stb.append("     TOTAL4 >= " + _param._passScore + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     SCORE_KEY ");
        /** */
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     '" + KEY_NOT_PASS + "' AS KEY, ");
        stb.append("      SCORE_KEY, ");
        stb.append("      COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_SCORE_KEY ");
        stb.append(" WHERE ");
        if (useJudgement) {
            stb.append("     VALUE(JUDGEMENT, '0') <> '1' ");
        } else {
            stb.append("     TOTAL4 < " + _param._passScore + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     SCORE_KEY ");
        return stb.toString();
    }

    /**
     * 入試科目のグラフオブジェクトを作成する
     * @param testSubclass
     * @return
     */
    private JFreeChart createBarChart(TestSubclass testSubclass) {
        
        final DefaultCategoryDataset scoreDataset = testSubclass._graphDataset;
        
        final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setTickLabelsVisible(true); // 主軸の目盛りを表示する
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f)); // 目盛りの太さ
        
        final LayeredBarRenderer renderer = new ExamineeBarRenderer();
        plot.setRenderer(renderer);
        
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelAnchorOffset(6.0);

        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        final Font font = new Font("TimesRoman", Font.PLAIN, 12);
        renderer.setItemLabelFont(font);

        // 軸の設定
        final NumberAxis numberAxis = new NumberAxis();
        plot.setRangeAxis(numberAxis);

        numberAxis.setStandardTickUnits(_param.createIntegerTickUnits());

        numberAxis.setUpperMargin(0.15);
        numberAxis.setLowerBound(0);
        numberAxis.setLabelFont(font);
        
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(font);
        domainAxis.setCategoryMargin(0.0);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        
//        numberAxis.setTickUnit(new NumberTickUnit(memori)); // 目盛り:x
//        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        numberAxis.setTickLabelsVisible(true);
//        final Range range = renderer.findRangeBounds(scoreDataset); // 最大人数
//        log.debug("range = " + range);
//        final int memori = memori( ((int) range.getUpperBound() / 7 + 1), ((int) range.getUpperBound() / 5 + 1));
//        final int max = Math.max(memori, 2) * 5;
//        log.debug(" memori = " + memori + " , max = " + max);
//        numberAxis.setRange(0, max); // レンジ
        
        return chart;
    }
    
    private int memori(int low, int high) {
        for (int i = low; i <= high; i++) {
            if (i % 10 == 0) {
                return i;
            }
        }
        return high;
    }

    /**
     * グラフオブジェクトの画像ファイルを作成する
     * @param chart
     * @param dotWidth 
     * @param dotHeight
     * @return
     */
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

    /**
     * 作成された画像ファイルを削除する
     */
    private void removeImageFiles() {
        for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
            final File imageFile = (File) it.next();
            if (null == imageFile) {
                continue;
            }
            log.debug("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.debug("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /**
     * 入試科目データ
     */
    private class TestSubclass {
        /** テスト科目コード */
        private final String _code;
        /** テスト科目名称 */
        private final String _name;
        /** 満点 */
        private final int _perfect;
        /** グラフ分布表のきざみ幅 */
        private final int _kizami;
        // 合格者の最高点、最低点、平均点
        private int _passMax;
        private int _passMin;
        private String _passAvg;

        // 不合格者の最高点、最低点、平均点
        private int _notPassMax;
        private int _notPassMin;
        private String _notPassAvg;

        // 受験者の最高点、最低点、平均点
        private int _examineeMax;
        private int _examineeMin;
        private String _examineeAvg;

        /** グラフ描画対象のデータ */
        private DefaultCategoryDataset _graphDataset;
        
        public TestSubclass(final String code, final String name, final int perfect, final int kizami) {
            _code = code;
            _name = name;
            _perfect = perfect;
            _kizami = kizami;

            _graphDataset = new DefaultCategoryDataset();
            
            clear();
        }

        /**
         * 最高点、最低点、平均点をセットする
         * @param key
         * @param max
         * @param min
         * @param avg
         */
        public void setMaxMinAvg(String key, int max, int min, String avg) {
            // log.debug(" " + _code + " , " + key + " , " + max + " , "+  min + " , " + avg);
            if (KEY_PASS.equals(key)) {
                _passMax = max;
                _passMin = min;
                _passAvg = avg;
            } else if (KEY_NOT_PASS.equals(key)) {
                _notPassMax = max;
                _notPassMin = min;
                _notPassAvg= avg;
            } else if (KEY_EXAMINEE.equals(key)) {
                _examineeMax = max;
                _examineeMin = min;
                _examineeAvg = avg;
            }
        }

        /**
         * グラフ表示のデータを追加する
         * @param count 人数 (Barの縦軸)
         * @param key シリーズ (合格者/受験者)
         * @param scoreLevel (Barの横軸)
         */
        public void addDataSet(Number count, String key, String scoreLevel) {
            if (KEY_PASS.equals(key) || KEY_EXAMINEE.equals(key)) {
                _graphDataset.addValue(count, key, scoreLevel);
            }
        }

        /**
         * 設定値をクリアする
         */
        private void clear() {
            _graphDataset.clear();
            _passMax = _notPassMax = _examineeMax = 0;
            _passMin = _notPassMin = _examineeMin = 1000;
            _passAvg = _notPassAvg = _examineeAvg = "0.0";

            for (int i = 1; i <= _perfect / _kizami; i++) {
                String scoreLevel = new Integer(i * _kizami).toString();
                _graphDataset.addValue(0, KEY_PASS, scoreLevel);
                _graphDataset.addValue(0, KEY_EXAMINEE, scoreLevel);
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _applicantDiv;
        private final String _lastYear;
        private final String _ctrlDate;
        private Map _testSubclasses;
        
        private final int _passScore;
        
        private final Paint _oblique;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _ctrlDate = request.getParameter("CTRL_DATE");
            
            _lastYear = String.valueOf(Integer.parseInt(_year) - 1);
            final String passScore = request.getParameter("PASS_SCORE");
            _passScore = passScore == null || "".equals(passScore) ? 0 : Integer.parseInt(passScore);

            _oblique = createObliquePaint();
        }

        /**
         * 網掛けのイメージを作成する。
         * @return
         */
        private Paint createObliquePaint() {
            final int width = 150;
            final int height = 150;
            final int thick = 3;
            final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            final Graphics g = image.getGraphics();
            for (int h = 0; h < height * 2; h += (thick * 2)) {
                for (int t = - thick / 2; t <= thick / 2; t++) {
                    g.drawLine(0, h + t, width, h - height + t);
                }
            }
            return new TexturePaint(image, new Rectangle2D.Float(0, 0, width, height));
        }

        public void setTestSubclasses(DB2UDB db2, String entexamYear) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _testSubclasses = new TreeMap();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.TESTSUBCLASSCD, T2.NAME1 AS NAME, T3.PERFECT ");
                stb.append(" FROM ");
                stb.append("   ENTEXAM_SCORE_DAT T1   ");
                stb.append("   LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L009' ");
                stb.append("       AND T2.NAMECD2 = T1.TESTSUBCLASSCD ");
                stb.append("   LEFT JOIN ENTEXAM_PERFECT_MST T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("       AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("       AND T3.TESTDIV = T1.TESTDIV ");
                stb.append("       AND T3.COURSECD = '1' ");
                stb.append("       AND T3.MAJORCD = '001' ");
                stb.append("       AND T3.EXAMCOURSECD = '0001' ");
                stb.append("       AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("   T1.ENTEXAMYEAR = '" + entexamYear + "' ");
                stb.append("   AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
                stb.append(" ORDER BY ");
                stb.append("   T1.TESTSUBCLASSCD ");
                
                final String sql = stb.toString();
                log.debug(" testsubclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                    String name = rs.getString("NAME");
                    int perfect = rs.getInt("PERFECT");
                    int kizami = perfect >= 100 ? 10 : 5;
                    
                    _testSubclasses.put(testSubclassCd, new TestSubclass(testSubclassCd, name, perfect, kizami));
                }
            } catch (SQLException e) {
                log.error("Excepion:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        private TestSubclass getTestSubclass(String testSubclassCd) {
            return (TestSubclass) _testSubclasses.get(testSubclassCd);
        }
        
        private void clearTestSubclassData() {
            for (Iterator it = _testSubclasses.values().iterator(); it.hasNext();) {
                TestSubclass testSubclass = (TestSubclass) it.next();
                testSubclass.clear();
            }
        }
        
        private TickUnitSource createIntegerTickUnits() {
            TickUnits units = new TickUnits();
            DecimalFormat df0 = new DecimalFormat("0");
            units.add(new NumberTickUnit(1, df0));
            units.add(new NumberTickUnit(2, df0));
            units.add(new NumberTickUnit(5, df0));
            units.add(new NumberTickUnit(10, df0));
            units.add(new NumberTickUnit(20, df0));
            units.add(new NumberTickUnit(50, df0));
            units.add(new NumberTickUnit(100, df0));
            units.add(new NumberTickUnit(200, df0));
            units.add(new NumberTickUnit(500, df0));
            units.add(new NumberTickUnit(1000, df0));
            return units;
        }
    }
    
    /**
     * 受験者/合格者の数を描画するBar(棒)グラフ
     * 
     *
     * LayeredBarRendererを基に以下を修正。
     *
     * ・異なるシリーズでBarが細くならないようにした。
     *  　(細くならない場合合格者のBarがかくれてしまうが、かわりに値が大きい順に描画するよう指定することで対処。
     *       => setRowRenderingOrder(SortOrder.DESCENDING))
     *
     * ・受験者数と合格者数が同一の場合、Bar上のラベルが重ならないようにした。
     */
    public class ExamineeBarRenderer extends LayeredBarRenderer {

        // インデクス (データセットのKEYの順番に準じる)
        private int PASS_INDEX = 0;
        private int EXAMINEE_INDEX = 1;
        
        private Paint PASS_COLOR = _param._oblique; //Color.lightGray;
        private Paint EXAMINEE_COLOR = Color.white;
        
        /**
         * Creates a new renderer.
         */
        public ExamineeBarRenderer() {
            super(); 
            
            setOutlinePaint(Color.black);
            setSeriesPaint(PASS_INDEX, PASS_COLOR);
            setSeriesPaint(EXAMINEE_INDEX, EXAMINEE_COLOR);
            
            setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
            setPositiveItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
        }
        
        /**
         * Draws the bar for one item in the dataset.
         *
         * @param g2  the graphics device.
         * @param state  the renderer state.
         * @param dataArea  the plot area.
         * @param plot  the plot.
         * @param domainAxis  the domain (category) axis.
         * @param rangeAxis  the range (value) axis.
         * @param dataSet  the data.
         * @param row  the row index (zero-based).
         * @param column  the column index (zero-based).
         * @param pass  the pass index.
         */
        public void drawItem(Graphics2D g2,
                             CategoryItemRendererState state,
                             Rectangle2D dataArea,
                             CategoryPlot plot,
                             CategoryAxis domainAxis,
                             ValueAxis rangeAxis,
                             CategoryDataset dataSet,
                             int row,
                             int column,
                             int pass) {

            // nothing is drawn for null values...
            Number dataValue = dataSet.getValue(row, column);
            if (dataValue == null) {
                return;
            }

            // BAR X
            double rectX = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0;

            // BAR Y
            double value = dataValue.doubleValue();
            double base = 0.0;

            RectangleEdge edge = plot.getRangeAxisEdge();
            double transY1 = rangeAxis.valueToJava2D(base, dataArea, edge);
            double transY2 = rangeAxis.valueToJava2D(value, dataArea, edge);
            double rectY = Math.min(transY2, transY1);

            double rectWidth = state.getBarWidth();
            double rectHeight = Math.abs(transY2 - transY1);

            // draw the bar...
            double shift = 0.0;
            rectWidth = 0.0;
            double widthFactor = 1.0;
            double seriesBarWidth = getSeriesBarWidth(row);
            if (!Double.isNaN(seriesBarWidth)) {
                widthFactor = seriesBarWidth;
            } 
            rectWidth = widthFactor * state.getBarWidth();
            rectX = rectX + (1 - widthFactor) * state.getBarWidth() / 2.0;
            int seriesCount = getRowCount();
            if (seriesCount > 1) {
                // needs to be improved !!!
                // shift = rectWidth * 0.20 / (seriesCount - 1);
                shift = 0; // 複数シリーズでもBarが細くならないようにする。
            }

            Rectangle2D bar = new Rectangle2D.Double(
                (rectX + ((seriesCount - 1 - row) * shift)), rectY, (rectWidth - (seriesCount - 1 - row) * shift * 2), rectHeight);
            Paint itemPaint = getItemPaint(row, column);
            g2.setPaint(itemPaint);
            g2.fill(bar);

            // draw the outline...
            if (isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
                Stroke stroke = getItemOutlineStroke(row, column);
                Paint paint = getItemOutlinePaint(row, column);
                if (stroke != null && paint != null) {
                    g2.setStroke(stroke);
                    g2.setPaint(paint);
                    g2.draw(bar);
                }
            }

            CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
            // 合格者数のラベルを表示する
            if (row == PASS_INDEX) {
                if (generator != null && isItemLabelVisible(row, column)) {
                    String label = generator.generateLabel(dataSet, row, column);
                    if (label != null) {
                        g2.setFont(getItemLabelFont(row, column));
                        g2.setPaint(getItemLabelPaint(row, column));
                    
                        // find out where to place the label...
                        ItemLabelPosition pos = getPositiveItemLabelPosition(row, column);
                    
                        // work out the label anchor point...
                        Point2D anchorPoint = calculateLabelAnchorPoint(pos.getItemLabelAnchor(), bar);
                        
                        if (isInternalAnchor(pos.getItemLabelAnchor())) {
                            final float x = (float) anchorPoint.getX();
                            final float y = (float) anchorPoint.getY();
                            Shape bounds = TextUtilities.calculateRotatedStringBounds(label, g2, x, y,
                                    pos.getTextAnchor(), pos.getAngle(), pos.getRotationAnchor());
                            
                            if (bounds != null) {
                                if (!bar.contains(bounds.getBounds2D())) {
                                    pos = getPositiveItemLabelPositionFallback();
                                    if (pos != null) {
                                        anchorPoint = calculateLabelAnchorPoint(pos.getItemLabelAnchor(), bar);
                                    }
                                }
                            }
                        }
                        
                        if (pos != null) {
                            final float x = (float) anchorPoint.getX();
                            final float y = (float) (anchorPoint.getY() - 2.0);
                            TextUtilities.drawRotatedString(label, g2, x, y,
                                    pos.getTextAnchor(), pos.getAngle(), pos.getRotationAnchor());
                        }
                    }
                }
            } else if (row == EXAMINEE_INDEX) { // 受験者数のラベルを表示する
                String label = generator.generateLabel(dataSet, row, column);
                if (label != null) {
                    g2.setPaint(getItemLabelPaint(row, column));
                    double offset =  - 11.0; // 位置を調節する
                    TextUtilities.drawRotatedString(label, g2, (float) bar.getCenterX(), (float) (bar.getMinY() + offset),
                            TextAnchor.BOTTOM_CENTER, 0.0, TextAnchor.BOTTOM_CENTER);
                }
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
            return anchor == ItemLabelAnchor.CENTER 
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
                                                  Rectangle2D bar) {

            Point2D result = null;
            double offset = getItemLabelAnchorOffset();
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
    }
}

// eof

