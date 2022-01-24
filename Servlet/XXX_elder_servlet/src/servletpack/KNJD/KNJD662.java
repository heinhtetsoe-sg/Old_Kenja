/*
 * $Id: 60b0a0725f485d43c7b2ce149ddf5f4a1230f373 $
 *
 * 作成日: 2011/12/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Arc2D;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 熊本 県下高校一斉テスト個人成績表
 *
 */
public class KNJD662 {

    private static final Log log = LogFactory.getLog(KNJD662.class);

    private boolean _hasData;
    
    private static final Font chartFont = new Font("TimesRoman", Font.BOLD, 18);
    
    private static final BasicStroke mainStroke = new BasicStroke(2.0f);
    private static final BasicStroke subStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f, new float[] {6.0f, 3.0f}, 0.0f);

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

    private File graphImageFile(final JFreeChart chart) {
        
        final int dotWidth = 1236;
        final int dotHeight = 1236;
        
        final String tmpFileName = KNJServletUtils.createTmpFile(".png");
        log.fatal("\ttmp file name=" + tmpFileName);

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
        final int pixel = dot / 4;

        /*
         * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
         * 拡大すると粗くなってしまうから。
         */
        return (int) (pixel * 1.3);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List studentList = getStudentList(db2);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            
            final Student student = (Student) it.next();
            
            if (student._scoreList.isEmpty()) { // データ件数が0なら表示しない
                continue;
            }
            
            svf.VrSetForm("KNJD662.frm", 1);
            
            printSvfHead(svf, student);
            
            printSvfGraph(svf, student);
            
            printSvfScore(svf, student);
            
            svf.VrEndPage();
            
            _hasData = true;
        }
    }

    private void printSvfHead(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("TESTNAME", "県下高校一斉テスト");
        svf.VrsOut("SUBJECT", student._majorname);
        svf.VrsOut("HR_NAME", student._hrname);
        final String attendno;
         if (null == student._attendno) {
             attendno = null;
         } else if (StringUtils.isNumeric(student._attendno)) {
             attendno = (String.valueOf(Integer.valueOf(student._attendno)) + "番");
         } else {
             attendno = student._attendno;
         }
        svf.VrsOut("ATTENDNO", attendno);
        svf.VrsOut("NAME", student._name);
    }

    private void printSvfScore(final Vrw32alp svf, final Student student) {
        
        final int max = 10;
        svf.VrsOutn("CLASS", max, "合計");
        if (null != student._score999999) {
            final Record record = student._score999999;
            final String gradeCourseDev = "2".equals(_param._groupDiv) ? record._courseDeviation : record._gradeDeviation;
            final String gradeCourseRank = "2".equals(_param._groupDiv) ? record._courseRank : record._gradeRank;
            final String gradeCourseCount = "2".equals(_param._groupDiv) ? record._courseCount : record._gradeCount;
            final String gradeCourseAvg = "2".equals(_param._groupDiv) ? record._courseAvg : record._gradeAvg;
            
            svf.VrsOutn("POINT",       max, record._score);
            svf.VrsOutn("SCHOOL_DIV",  max, gradeCourseDev);
            svf.VrsOutn("CLASS_RANK",  max, (formatRankCount(record._classRank) + "/" + formatRankCount(record._classCount)));
            svf.VrsOutn("GRADE_RANK",  max, (formatRankCount(gradeCourseRank) + "/" + formatRankCount(gradeCourseCount)));
            svf.VrsOutn("GRADE_AVE",   max, gradeCourseAvg);
            svf.VrsOutn("PREF_AVE",    max, record._prefAvg);
        }
        
        for (int i = 0, k = 1; i < student._scoreList.size() && k <= max - 1; i++) {
            final Record record = (Record) student._scoreList.get(i);
            
            if (StringUtils.isBlank(record._score)) {
                continue;
            }
            final String gradeCourseDev = "2".equals(_param._groupDiv) ? record._courseDeviation : record._gradeDeviation;
            final String gradeCourseRank = "2".equals(_param._groupDiv) ? record._courseRank : record._gradeRank;
            final String gradeCourseCount = "2".equals(_param._groupDiv) ? record._courseCount : record._gradeCount;
            final String gradeCourseAvg = "2".equals(_param._groupDiv) ? record._courseAvg : record._gradeAvg;
            
            svf.VrsOutn("CLASS",       k, record._prefSubclassAbbv);
            svf.VrsOutn("POINT",       k, record._score);
            svf.VrsOutn("SCHOOL_DIV",  k, gradeCourseDev);
            svf.VrsOutn("CLASS_RANK",  k, (formatRankCount(record._classRank) + "/" + formatRankCount(record._classCount)));
            svf.VrsOutn("GRADE_RANK",  k, (formatRankCount(gradeCourseRank) + "/" + formatRankCount(gradeCourseCount)));
            svf.VrsOutn("GRADE_AVE",   k, gradeCourseAvg);
            svf.VrsOutn("PREF_AVE",    k, record._prefAvg);
            k += 1;
        }
    }
    
    private String getRankCountString(final String rank, final String count) {
        return formatRankCount(rank) + "/" + formatRankCount(count);
    }
    private String formatRankCount(final String s) {
        return null == s ? "   " : s;
    }
    private Number percenttage(final BigDecimal bd, final int perfect) {
        return (null == bd) ? null : bd.divide(new BigDecimal(perfect / 100), BigDecimal.ROUND_HALF_UP);
    }

    // グラフの出力
    private void printSvfGraph(final Vrw32alp svf, final Student student) {
        
        svf.VrsOut("RADAR1_NAME1", "学年平均と得点");
        svf.VrsOut("RADAR1_NAME2", "学年偏差値と個人偏差値");
        svf.VrsOut("RADAR1_NAME3", "県平均と得点");
        
        try {
            final File file1 = graphImageFile(createRaderChart1(student));
            if (null != file1) {
                svf.VrsOut("RADAR1", file1.toString());
                _graphFiles.add(file1);
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        try {
            final File file2 = graphImageFile(createRaderChart2(student));
            if (null != file2) {
                svf.VrsOut("RADAR2", file2.toString());
                _graphFiles.add(file2);
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        try {
            final File file3 = graphImageFile(createRaderChart3(student));
            if (null != file3) {
                svf.VrsOut("RADAR3", file3.toString());
                _graphFiles.add(file3);
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
    }
    
    /**
     * 学年平均と個人得点のレーダーチャートを作成する
     * @param student
     * @return
     */
    private JFreeChart createRaderChart1(final Student student) {
        
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final Iterator it = student._scoreList.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            if (StringUtils.isBlank(record._score)) {
                continue;
            }
            final String gradeCourseName = "2".equals(_param._groupDiv) ? "コース" : "学年";
            final String gradeCourseAvg = "2".equals(_param._groupDiv) ? record._courseAvg : record._gradeAvg;
            
//            log.info("レーダーグラフ⇒" + record._mockSubclassCd + ", " + toBD(record._score) + " : " + toBD(record._gradeAvg) + ", " + percenttage(toBD(record._score), record._perfect) + " : " + percenttage(toBD(gradeCourseAvg), record._perfect));
            dataset.addValue(percenttage(toBD(record._score), record._perfect), "得点", record._prefSubclassAbbv);
            dataset.addValue(percenttage(toBD(gradeCourseAvg), record._perfect), gradeCourseName + "平均点", record._prefSubclassAbbv);
        }
        
        final SpiderWebPlot plot = new SpiderWebPlotKNJD662(dataset);
        plot.setWebFilled(false);
        plot.setMaxValue(100);
        plot.setLabelFont(chartFont);
        plot.setOutlinePaint(Color.white);
        plot.setLegendItemShape(Plot.DEFAULT_LEGEND_ITEM_BOX);

        // 実データ
        plot.setSeriesOutlineStroke(0, mainStroke);
        plot.setSeriesPaint(0, Color.black);

        // 学年・コース平均点
        plot.setSeriesOutlineStroke(1, subStroke);
        plot.setSeriesPaint(1, Color.gray);

        
        final JFreeChart chart = new JFreeChart(null, chartFont, plot, true);
        chart.setBackgroundPaint(Color.white);
        chart.setAntiAlias(true);
        final LegendTitle legend = chart.getLegend();
        legend.setBorder(BlockBorder.NONE);
        legend.setItemFont(chartFont);
        legend.setPosition(RectangleEdge.BOTTOM);

        return chart;
    }

    /**
     * 学年偏差値(50)と個人偏差値のレーダーチャートを作成する
     * @param student
     * @return
     */
    private JFreeChart createRaderChart2(final Student student) {
        
        final Double dev50 = new Double(50.0);
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final Iterator it = student._scoreList.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            if (StringUtils.isBlank(record._score)) {
                continue;
            }
            final String gradeCourseName = "2".equals(_param._groupDiv) ? "コース" : "学年";
            final String gradeCourseDeviation = "2".equals(_param._groupDiv) ? record._courseDeviation : record._gradeDeviation;
//            log.info("レーダーグラフ⇒" + record._subclassName + ", " + deviation);
            dataset.addValue(toBD(gradeCourseDeviation), gradeCourseName + "偏差値", record._prefSubclassAbbv);
            dataset.addValue(dev50, "偏差値50", record._prefSubclassAbbv);
        }
        
        final SpiderWebPlot plot = new SpiderWebPlotKNJD662(dataset);
        plot.setWebFilled(false);
        plot.setMaxValue(80);
        plot.setLabelFont(chartFont);
        plot.setOutlinePaint(Color.white);
        plot.setLegendItemShape(Plot.DEFAULT_LEGEND_ITEM_BOX);

        // 実データ
        plot.setSeriesOutlineStroke(0, mainStroke);
        plot.setSeriesPaint(0, Color.black);

        // 偏差値50
        plot.setSeriesOutlineStroke(1, subStroke);
        plot.setSeriesPaint(1, Color.gray);
        
        final JFreeChart chart = new JFreeChart(null, chartFont, plot, true);
        chart.setBackgroundPaint(Color.white);
        chart.setAntiAlias(true);
        final LegendTitle legend = chart.getLegend();
        legend.setBorder(BlockBorder.NONE);
        legend.setItemFont(chartFont);
        legend.setPosition(RectangleEdge.BOTTOM);

        return chart;
    }

    /**
     * 県平均点と個人得点のレーダーチャートを作成する
     * @param student
     * @return
     */
    private JFreeChart createRaderChart3(final Student student) {
        
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final Iterator it = student._scoreList.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            if (StringUtils.isBlank(record._score)) {
                continue;
            }
            
//            log.info("レーダーグラフ⇒" + record._subclassName + ", " + toBD(record._score) + ", " + toBD(record._prefAvg));
            dataset.addValue(percenttage(toBD(record._score), record._perfect), "得点", record._prefSubclassAbbv);
            dataset.addValue(percenttage(toBD(record._prefAvg), record._perfect), "県平均点", record._prefSubclassAbbv);
        }
        
        final SpiderWebPlot plot = new SpiderWebPlotKNJD662(dataset);
        plot.setWebFilled(false);
        plot.setMaxValue(100);
        plot.setLabelFont(chartFont);
        plot.setOutlinePaint(Color.white);
        plot.setLegendItemShape(Plot.DEFAULT_LEGEND_ITEM_BOX);

        // 実データ
        plot.setSeriesOutlineStroke(0, mainStroke);
        plot.setSeriesPaint(0, Color.black);

        // 県平均点
        plot.setSeriesOutlineStroke(1, subStroke);
        plot.setSeriesPaint(1, Color.gray);

        final JFreeChart chart = new JFreeChart(null, chartFont, plot, true);
        chart.setBackgroundPaint(Color.white);
        chart.setAntiAlias(true);
        final LegendTitle legend = chart.getLegend();
        legend.setBorder(BlockBorder.NONE);
        legend.setItemFont(chartFont);
        legend.setPosition(RectangleEdge.BOTTOM);

        return chart;
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final Map studentMap = new HashMap();
        try {
            final String sql = getRegdSql(_param);
//            log.debug(" regdSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final Student student = new Student(schregno);
                student._name = rs.getString("NAME");
                student._hrname = rs.getString("HR_NAME");
                student._attendno = rs.getString("ATTENDNO");
                student._majorname = rs.getString("MAJORNAME");
                
                studentList.add(student);
                studentMap.put(student._schregno, student);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        try {
            
            final String sql = getMockRankSql(_param);
//            log.debug(" mockRankSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                
                final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                String subclassName = null;
                String prefSubclassName = null;
                String prefSubclassAbbv = null;
                if ("999999".equals(mockSubclassCd)) {
                    subclassName = "９教科合計";
                    prefSubclassName = "９教科合計";
                    prefSubclassAbbv = "９教科合計";
                } else if ("555555".equals(mockSubclassCd)) {
                } else if ("333333".equals(mockSubclassCd)) {
                } else {
                    subclassName = rs.getString("SUBCLASS_NAME");
                    prefSubclassName = rs.getString("PREF_SUBCLASSNAME");
                    prefSubclassAbbv = rs.getString("PREF_SUBCLASSABBV");
                }
                final String score = rs.getString("SCORE");
                final String gradeDeviation = getSisyaGonyu(rs.getString("GRADE_DEVIATION"));
                final String coruseDeviation = getSisyaGonyu(rs.getString("COURSE_DEVIATION"));
                final String classRank = rs.getString("CLASS_RANK");
                final String classCount = rs.getString("CLASS_COUNT");
                final String gradeRank = rs.getString("GRADE_RANK");
                final String gradeCount = rs.getString("GRADE_COUNT");
                final String courseRank = rs.getString("COURSE_RANK");
                final String courseCount = rs.getString("COURSE_COUNT");
                final String gradeAvg = getSisyaGonyu(rs.getString("GRADE_AVG"));
                final String classAvg = getSisyaGonyu(rs.getString("CLASS_AVG"));
                final String courseAvg = getSisyaGonyu(rs.getString("COURSE_AVG"));
                final String prefAvg = getSisyaGonyu(rs.getString("PREF_AVG"));
                final int perfect = rs.getInt("PERFECT");
                
                final Record record = new Record(mockSubclassCd, subclassName, score, gradeDeviation, coruseDeviation,
                        classRank, classCount, gradeRank, gradeCount, courseRank, courseCount, 
                        gradeAvg, classAvg, courseAvg, prefSubclassName, prefSubclassAbbv, prefAvg, perfect);
                if ("999999".equals(record._mockSubclassCd)) {
                    student._score999999 = record;
                } else if ("555555".equals(record._mockSubclassCd)) {
                } else if ("333333".equals(record._mockSubclassCd)) {
                } else {
                    student._scoreList.add(record);
                }
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }
    
    /**
     * 四捨五入した値を得る。
     * @param doubleValue
     * @return 四捨五入した値
     */
    private static String getSisyaGonyu(final String doubleValue) {
        return null == doubleValue ? null : new BigDecimal(doubleValue).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    /**
     * BigDecimal型に変換する。変換不可の場合、nullを得る。
     * @param doubleValue
     * @return BigDecimal型に変換した値
     */
    private static BigDecimal toBD(final String doubleValue) {
        return (!NumberUtils.isNumber(doubleValue)) ? null : new BigDecimal(doubleValue);
    }
    
    /** 
     * <pre>
     * 生徒の学籍等の情報および総合的な学習の時間の所見・通信欄を取得するＳＱＬ文を戻します。
     * ・指定された生徒全員を対象とします。
     * </pre>
     */
    private String getRegdSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(_param._categoryIsClass)) {
            stb.append("        AND T1.GRADE || '-' || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        } else {
            stb.append("        AND T1.GRADE || '-' || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        }
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._loginDate + "' THEN T2.EDATE ELSE '" + param._loginDate + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._loginDate + "' THEN T2.EDATE ELSE '" + param._loginDate + "' END)) ) ");
        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._loginDate + "' THEN T2.EDATE ELSE '" + param._loginDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");
        
        //メイン表
        stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T5.NAME, ");
        stb.append("        T3.COURSENAME, T4.MAJORNAME ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("        INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND ");
        stb.append("                                          T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("                                          T2.GRADE || T2.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
        stb.append("        LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
        stb.append("        LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
        stb.append("ORDER BY T2.GRADE, T2.HR_CLASS, ATTENDNO");
        return stb.toString();
    }
    
    private String getMockRankSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS(YEAR, MOCKCD, SCHREGNO, MOCK_SUBCLASS_CD) AS ( ");
        stb.append(" SELECT T1.YEAR, T1.MOCKCD, T1.SCHREGNO, T1.MOCK_SUBCLASS_CD ");
        stb.append(" FROM MOCK_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.MOCKCD = '" + _param._mockCd + "' ");
        stb.append(" UNION ");
        stb.append(" SELECT T1.YEAR, T1.MOCKCD, T1.SCHREGNO, T1.MOCK_SUBCLASS_CD ");
        stb.append(" FROM MOCK_RANK_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.MOCKCD = '" + _param._mockCd + "' ");
        stb.append(" ) SELECT  ");
        stb.append("     T1.MOCKCD, T1.SCHREGNO, T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T6.SUBCLASS_NAME, ");
        stb.append("     T6.SUBCLASS_ABBV, ");
        stb.append("     T2.SCORE, ");
        stb.append("     T2.GRADE_DEVIATION, ");
        stb.append("     T2.COURSE_DEVIATION, ");
        stb.append("     T2.CLASS_RANK, ");
        stb.append("     T2.GRADE_RANK, ");
        stb.append("     T2.COURSE_RANK, ");
        stb.append("     T3.COUNT AS GRADE_COUNT, ");
        stb.append("     T4.COUNT AS CLASS_COUNT, ");
        stb.append("     T5.COUNT AS COURSE_COUNT, ");
        stb.append("     T3.AVG AS GRADE_AVG, ");
        stb.append("     T4.AVG AS CLASS_AVG, ");
        stb.append("     T5.AVG AS COURSE_AVG, ");
        stb.append("     T7.SUBCLASS_NAME AS PREF_SUBCLASSNAME,");
        stb.append("     T7.SUBCLASS_ABBV AS PREF_SUBCLASSABBV,");
        stb.append("     T8.AVG AS PREF_AVG, ");
        stb.append("     VALUE(T9.PERFECT, 100) AS PERFECT ");
        stb.append(" FROM SCHREG_REGD_DAT T0 ");
        stb.append(" INNER JOIN SUBCLASS T1 ON T1.SCHREGNO = T0.SCHREGNO ");
        stb.append(" LEFT JOIN MOCK_RANK_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T2.MOCKDIV = '" + _param._mockDiv + "' ");
        stb.append(" LEFT JOIN MOCK_AVERAGE_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T3.AVG_DIV = '1' ");
        stb.append("     AND T3.GRADE = T0.GRADE AND T3.HR_CLASS = '000' ");
        stb.append("     AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '00000000' ");
        stb.append(" LEFT JOIN MOCK_AVERAGE_DAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("     AND T4.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T4.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T4.AVG_DIV = '2' ");
        stb.append("     AND T4.GRADE = T0.GRADE AND T4.HR_CLASS = T0.HR_CLASS ");
        stb.append("     AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
        stb.append(" LEFT JOIN MOCK_AVERAGE_DAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("     AND T5.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T5.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T5.AVG_DIV = '3' ");
        stb.append("     AND T5.GRADE = T0.GRADE AND T5.HR_CLASS = '000' ");
        stb.append("     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = T0.COURSECD || T0.MAJORCD || T0.COURSECODE ");
        stb.append(" LEFT JOIN MOCK_SUBCLASS_MST T6 ON T6.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append(" LEFT JOIN MOCK_PREF_SUBCLASS_MST T7 ON T7.PREF_SUBCLASSCD = T6.PREF_SUBCLASSCD ");
        stb.append(" LEFT JOIN MOCK_PREF_AVG_DAT T8 ON T8.YEAR = T1.YEAR ");
        stb.append("     AND T8.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T8.GRADE = T0.GRADE ");
        stb.append("     AND T8.PREF_SUBCLASSCD = T6.PREF_SUBCLASSCD ");
        stb.append(" LEFT JOIN MOCK_PERFECT_COURSE_DAT T9 ON T9.YEAR = T1.YEAR ");
        stb.append("     AND T9.COURSE_DIV = '0' ");
        stb.append("     AND T9.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T9.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T9.GRADE = CASE WHEN T9.DIV = '01' THEN '00' ELSE T0.GRADE END  ");
        stb.append("     AND T9.COURSECD || T9.MAJORCD || T9.COURSECODE = ");
        stb.append("       CASE WHEN T9.DIV = '01' OR T9.DIV = '02' THEN '00000000' ELSE T0.COURSECD || T0.MAJORCD || T0.COURSECODE END ");
        stb.append(" WHERE ");
        stb.append("     T0.YEAR = '" + _param._year + "' ");
        stb.append("     AND T0.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._categoryIsClass)) {
            stb.append("        AND T0.GRADE || '-' || T0.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        } else {
            stb.append("        AND T0.GRADE || '-' || T0.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T0.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T0.SCHREGNO, T1.MOCK_SUBCLASS_CD ");

        return stb.toString();
    }

    public class Student {
        final String _schregno;
        final List _scoreList;
        String _hrname;
        String _name;
        String _attendno;
        String _majorname;
        Record _score999999;
        Student(final String schregno) {
            _schregno = schregno;
            _scoreList = new ArrayList();
        }
    }
    
    public class Record {
        final String _mockSubclassCd;
        final String _subclassName;
        final String _score;
        final String _gradeDeviation;
        final String _courseDeviation;
        final String _classRank;
        final String _classCount;
        final String _gradeRank;
        final String _gradeCount;
        final String _courseRank;
        final String _courseCount;
        final String _gradeAvg;
        final String _classAvg;
        final String _courseAvg;
        final String _prefSubclassName;
        final String _prefSubclassAbbv;
        final String _prefAvg;
        final int _perfect;
        public Record(
                final String mockSubclassCd,
                final String subclassName,
                final String score,
                final String gradeDeviation,
                final String coruseDeviation,
                final String classRank,
                final String classCount,
                final String gradeRank,
                final String gradeCount,
                final String courseRank,
                final String courseCount,
                final String gradeAvg,
                final String classAvg,
                final String courseAvg,
                final String prefSubclassName,
                final String prefSubclassAbbv,
                final String prefAvg,
                final int perfect) {
            _mockSubclassCd = mockSubclassCd;
            _subclassName = subclassName;
            _score = score;
            _gradeDeviation = gradeDeviation;
            _courseDeviation = coruseDeviation;
            _classRank = classRank;
            _classCount = classCount;
            _gradeRank = gradeRank;
            _gradeCount = gradeCount;
            _courseRank = courseRank;
            _courseCount = courseCount;
            _gradeAvg = gradeAvg;
            _classAvg = classAvg;
            _courseAvg = courseAvg;
            _prefSubclassName = prefSubclassName;
            _prefSubclassAbbv = prefSubclassAbbv;
            _prefAvg = prefAvg;
            _perfect = perfect;
        }
        public String toString() {
            return "Record(subclassCd=" + _mockSubclassCd + ", prefAbbv=" + _prefSubclassAbbv + ", score=" + _score + ")";
        }
    }
    
    private static class SpiderWebPlotKNJD662 extends SpiderWebPlot {

        public SpiderWebPlotKNJD662(final DefaultCategoryDataset dataset) {
            super(dataset);
        }
        
        /**
         * Returns the location for a label
         * 
         * @param labelBounds the label bounds.
         * @param ascent the ascent (height of font).
         * @param plotArea the plot area
         * @param startAngle the start angle for the pie series.
         * 
         * @return The location for a label.
         */
        protected Point2D calculateLabelLocation(Rectangle2D labelBounds, 
                                                 double ascent,
                                                 Rectangle2D plotArea, 
                                                 double startAngle)
        {
            final Arc2D arc1 = new Arc2D.Double(plotArea, startAngle, 0, Arc2D.OPEN);
            final Point2D point1 = arc1.getEndPoint();

            final double deltaX = -(point1.getX() - plotArea.getCenterX()) * super.getAxisLabelGap();
            final double deltaY = -(point1.getY() - plotArea.getCenterY()) * super.getAxisLabelGap();

            double labelX = point1.getX() - deltaX;
            double labelY = point1.getY() - deltaY;

            if (labelX < plotArea.getCenterX()) {
                labelX -= labelBounds.getWidth();
            }
        
            if (labelX == plotArea.getCenterX()) {
                labelX -= labelBounds.getWidth() / 2;
            }
            
            labelX = plotArea.getCenterX() + (int) ((labelX - plotArea.getCenterX()) * 0.75);

            if (labelY > plotArea.getCenterY()) {
                labelY += ascent;
            }

            return new Point2D.Double(labelX, labelY);
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _categoryIsClass;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _loginDate;
        final String _mockCd;
        final String _mockDiv;
        final String _groupDiv; // 1:学年 2:コース
        

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("HR_CLASS");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            
            _mockCd = request.getParameter("MOCKCD");
//            final String juni = request.getParameter("JUNI");
//            final String rankDivTemp = request.getParameter("useKnjd106cJuni" + juni);
            _mockDiv = "1"; // (rankDivTemp == null) ? juni : rankDivTemp;
            _groupDiv = request.getParameter("GROUP_DIV");
        }
    }
}

// eof

