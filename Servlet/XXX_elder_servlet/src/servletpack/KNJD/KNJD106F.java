// kanji=漢字
/*
 * $Id: d15f1bf5ca5d5c0c61b014e4bee3646e6da7a2c7 $
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: d15f1bf5ca5d5c0c61b014e4bee3646e6da7a2c7 $
 */
public class KNJD106F {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD106F.class);

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

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 全教科科目コード。 */
    private static final String ALL9 = "999999";

    /** フォーム。 */
    private static final String FORM_FILE1 = "KNJD106F.frm";
    /** 得点分布票表示用フォーム。 */
    private static final String FORM_FILE2 = "KNJD106F_2.frm";

    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HR = "02";
    private static final String RANK_DIV_COURSE = "03";
    private static final String RANK_DIV_MAJOR = "04";
    private static final String RANK_DIV_COURSEGROUP = "05";
    
    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_HR = "02";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_MAJOR = "04";
    private static final String AVG_DIV_COURSEGROUP = "05";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 76937 $ $Date: 2020-09-16 21:16:12 +0900 (水, 16 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
        
        final String _file;
        final Vrw32alp svf = new Vrw32alp();
        if (svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        svf.VrSetSpoolFileStream(response.getOutputStream());
        response.setContentType("application/pdf");

        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }
        final Param param = new Param(request, db2);

        _file = "2".equals(param._formDiv) ? FORM_FILE2 : FORM_FILE1;
        log.debug("フォームファイル=" + _file);

        try {
            log.info("★マスタ関連の読込み");
            param.load(db2);
            
            // 対象の生徒たちを得る
            final List<Student> students = Student.createStudents(db2, param, param.getScregnos(db2));
            _hasData = students.size() > 0;
            
            // 成績のデータを読む
            final Exam exam = new Exam(param._year, param._semester, param._proficiencydiv, param._proficiencycd, param._grade);
            exam.load(db2, param, students);

            // 印刷する
            log.info("★印刷");
            for (final Student student : students) {
                log.debug("☆" + student + ", 科目の数=" + student._subclasses.size() + ", コースキー=" + student.courseKey());
                log.debug("今回の成績: " + student._record.values());
                
                exam.setSubclasses(student);
                
                final int sts = svf.VrSetForm(_file, 4);
                if (0 != sts) {
                    log.error("VrSetFromエラー:sts=" + sts);
                }
                printStatic(svf, param, exam, student);
                printSubclass(svf, param, exam, student);
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            log.info("★終了処理");
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            Chart.removeImageFiles(param);
            log.info("Done.");
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

    public void printStatic(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {
        svf.VrsOut("SCHOOL_NAME", param._schoolName);

        final String staffName = param._staffs.get(student._grade + student._hrClass);
        svf.VrsOut("STAFFNAME", (param._remark2 == null ? "" : param._remark2) + (staffName == null ? "" : staffName));

        final Address address = student.getAddress(param);
        if (!"1".equals(param._useAddress)) {
            final String zipCd = address._zipCd;
            if (zipCd != null && !"".equals(zipCd)) {
                svf.VrsOut("ZIPCD", "〒" + zipCd);
            }
            final String addr1 = address._addr1;
            final String addr2 = address._addr2;
            final String field = (null != addr1 && addr1.length() > 25) || (null != addr2 && addr2.length() > 25) ? "_3" : (null != addr1 && addr1.length() > 19) || (null != addr2 && addr2.length() > 19) ? "_2" : "";
            svf.VrsOut("ADDR1" + field, addr1);
            svf.VrsOut("ADDR2" + field, addr2);
            final String addressee = address._addresee;
            if (null != addressee) {
                final String setAddressee = addressee + "  様";
                final String field2 = setAddressee.length() > 13 ? "_2" : "";
                svf.VrsOut("ADDRESSEE" + field2, setAddressee);
            }
        }
    
        svf.VrsOut("NENDO", param._nendo + "年度　" + exam._examName);
    
        final int attendNo = Integer.parseInt(student._attendNo);
        svf.VrsOut("HR_NAME", student._hrName + attendNo + "番");

        if (KNJ_EditEdit.getMS932ByteLength(student._name) <= 20) {  // 全角で規定文字数を超えたらフォントを変える
            svf.VrsOut("NAME", student._name);
        } else {
            svf.VrsOut("NAME_2", student._name);
        }

        // 全体評
        KNJServletUtils.printDetail(svf, "WHOLE_REMARK", exam.getWholeRemark(student), 45 * 2, 7);
        
        // 提出日
        try {
            final Calendar cal = KNJServletUtils.parseDate(param._submitDate.replace('/', '-'));
            final String month = (cal.get(Calendar.MONTH) + 1) + "月";
            final String day = cal.get(Calendar.DATE) + "日";
            svf.VrsOut("DATE", month + day);
        } catch (final ParseException e) {
            log.error("提出日が解析できない");
            svf.VrsOut("DATE", param._submitDate);
        }

        final String get3title = exam.get3title(student.courseKey());
        if (null != get3title) {
            svf.VrsOut("ITEM_TOTAL3", get3title);
        }
        final String get5title = exam.get5title(student.courseKey());
        if (null != get5title) {
            svf.VrsOut("ITEM_TOTAL5", get5title);
        }

        final String barLegendImage;
        final String msg;
        if (param.isCourse()) {
            barLegendImage = BAR_CHART_LEGEND2;
            msg = "コース";
        } else {
            barLegendImage = BAR_CHART_LEGEND1;
            msg = "学年";
        }

        svf.VrsOut("ITEM_AVG", msg + "平均");
        svf.VrsOut("ITEM_RANK", msg + "順位");

        // 画像
        svf.VrsOut("RADER_LEGEND", param._imagePath + "/" + RADER_CHART_LEGEND);
        svf.VrsOut("BAR_LEGEND", param._imagePath + "/" + barLegendImage);

        svf.VrsOut("BAR_TITLE", "得点グラフ（％）");
        svf.VrsOut("RADER_TITLE", "教科間バランス");
    }

    /**
     * 成績部分の印刷
     * @param param パラメータ
     * @param student 生徒
     */
    public void printSubclass(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {

        // 3教科 & 5教科
        if (null != exam._subclassGroup3.get(student.courseKey())) {
            printRecordRec3(svf, param, exam, student);
        }
        if (null != exam._subclassGroup5.get(student.courseKey())) {
            printRecordRec5(svf, param, exam, student);
        }

        // グラフ印字(サブフォームよりも先に印字)
        final File outputBarFile = Chart.barGraphFile(param, student, exam._averageDatMap);

        // グラフの出力
        svf.VrsOut("BAR_LABEL", "得点率");
        svf.VrsOut("BAR", outputBarFile.toString());

        if (!"2".equals(param._formDiv)) {
            final File outputRadarFile = Chart.radarGraphFile(param, student._record);
            // グラフの出力
            if (null != outputRadarFile) {
                svf.VrsOut("RADER", outputRadarFile.toString());
            }
        }

        // 成績
        print(svf, param, student, exam, student._record);

        svf.VrPrint();
    }

    public void print(final Vrw32alp svf, final Param param, final Student student, final Exam exam, final Map<SubClass, Record> records) {
        int subcCount = 0;
        final ScoreDistribution dist = exam._scoreDistributionMap.get(param.getDistKey(student._grade, student.courseKey(), student._coursegroupCd));
        for (final SubClass subClass : student._subclasses.values()) {

//            if ("90".equals(subClass.getClassCd())) {
//                continue;
//            }

//            final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//            final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//            if (!hasSubclass5 && !hasSubclass3) {
//                continue;
//            }

            final Record record = records.get(subClass);
            final String key = param.subclassKey(subClass._code, student.courseKey(), student._coursegroupCd);
            final AverageDat avgDat = exam._averageDatMap.get(key);

            // 教科
            final Class clazz = param._classes.get(subClass.getClassCd());
            svf.VrsOut("CLASS", (null != clazz) ? clazz._name : "");

            // 科目
            svf.VrsOut("SUBCLASS", subClass._name);

            if (null != record) {
                final String score = record.getScore();
                if (param.isUnderScore(score) && !param._isKumamoto) {
                    svf.VrsOut("SCORE", "(" + score + ")");
                } else {
                    svf.VrsOut("SCORE", score);
                }
                if (!param._rankNotPrint) {
                    svf.VrsOut("RANK",      record.getRank());
                }
                if (param._deviationPrint) {
                    svf.VrsOut("DEVIATION", record.getDeviation());
                }
            }

            if (null != avgDat) {
                svf.VrsOut("AVERAGE",  avgDat.getAvgStr());
                svf.VrsOut("MAX_SCORE", avgDat._highScore.toString());
                svf.VrsOut("EXAMINEE", avgDat._count.toString());    // 受験者数
            }
            
            if ("2".equals(param._formDiv) && null != dist) {
                final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(subClass._code);

                ScoreCount[] scoreCounts = ssd._scoreCounts;
                //int total = 0;
                for (int i = 0; i < scoreCounts.length; i++) {
                    final Integer count = ssd.getCount(scoreCounts[i]._key);
                    svf.VrsOut("SUM" + distSuf(i), count.toString());
                    // log.debug(" 得点分布 (" + subClass + "):" + scoreKeys[i] + " = " + count);
                    //total += count.intValue();
                }
                // log.debug(" total = " + total);
            }
            svf.VrEndRecord();
            if (++subcCount >= TABLE_SUBCLASS_MAX) {
                break;
            }
        }
        if (subcCount == 0) {
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
    }

    private void printRecordRec3(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {
        final Record rec3 = student._recordOther.get(ALL3);
        if (null != rec3) {
            svf.VrsOut("SCORE3", rec3.getScore()  + "/" + rec3.getAvg());
            if (!param._rankNotPrint) {
                svf.VrsOut("RANK3", rec3.getRank());
            }
            if (param._deviationPrint) {
                svf.VrsOut("DEVIATION3", rec3.getDeviation());
            }
        }
        final AverageDat avg3 = exam._averageDatOtherMap.get(param.subclassKey(ALL3, student.courseKey(), student._coursegroupCd));
        if (null != avg3) {
            svf.VrsOut("AVERAGE3",  avg3.getAvgStr());
            svf.VrsOut("MAX_SCORE3", avg3._highScore.toString());
            svf.VrsOut("EXAMINEE3", avg3._count.toString());
        }
        
        final ScoreDistribution dist = exam._scoreDistributionMap.get(param.getDistKey(student._grade, student.courseKey(), student._coursegroupCd));
        if ("2".equals(param._formDiv) && null != dist) {
            final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(ALL3);
            if (ssd._added) {
                ScoreCount[] scoreCounts = ssd._scoreCounts;
//                int total = 0;
                for (int i = 0; i < scoreCounts.length; i++) {
                    final Integer count = ssd.getCount(scoreCounts[i]._key);
                    svf.VrsOut("AVERAGE3_" + distSuf(i), count.toString());
//                    total += count.intValue();
                }
                // log.debug(" total = " + total);
            }
        }
    }

    private void printRecordRec5(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {
        final Record rec5 = student._recordOther.get(ALL5);
        if (null != rec5) {
            svf.VrsOut("SCORE5",     rec5.getScore() + "/" + rec5.getAvg());
            if (!param._rankNotPrint) {
                svf.VrsOut("RANK5",      rec5.getRank());
            }
            if (param._deviationPrint) {
                svf.VrsOut("DEVIATION5", rec5.getDeviation());
            }
        }
        final AverageDat avg5 = exam._averageDatOtherMap.get(param.subclassKey(ALL5, student.courseKey(), student._coursegroupCd));
        if (null != avg5) {
            svf.VrsOut("AVERAGE5",  avg5.getAvgStr());
            svf.VrsOut("MAX_SCORE5", avg5._highScore.toString());
            svf.VrsOut("EXAMINEE5", avg5._count.toString());
        }
        
        final ScoreDistribution dist = exam._scoreDistributionMap.get(param.getDistKey(student._grade, student.courseKey(), student._coursegroupCd));
        if ("2".equals(param._formDiv) && null != dist) {
            final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(ALL5);
            if (ssd._added) {
                ScoreCount[] scoreCounts = ssd._scoreCounts;
                //int total = 0;
                for (int i = 0; i < scoreCounts.length; i++) {
                    final Integer count = ssd.getCount(scoreCounts[i]._key);
                    svf.VrsOut("AVERAGE5_" + distSuf(i), count.toString());
                    //total += count.intValue();
                }
                // log.debug(" total = " + total);
            }
        }
    }
    
    private int distSuf(final int i) {
        return (11 - 3) - i;
    }
    
    private static class Chart {

        private static File barGraphFile(final Param param, final Student student, final Map<String, AverageDat> averages) {
            // グラフ用のデータ作成
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            int i = 0;
            for (final Record record : student._record.values()) {
                scoreDataset.addValue(record._graphScore, "本人得点", record._subClass._abbv);
                final String key = param.subclassKey(record._subClass._code, student.courseKey(), student._coursegroupCd);
                final AverageDat avgDat = averages.get(key);
                final BigDecimal avgKansan = (null == avgDat) ? null : avgDat._avgKansan;

                final String msg = param.isCourse() ? "コース" : "学年";
                avgDataset.addValue(avgKansan, msg + "平均点", record._subClass._abbv);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + "(" + record._graphScore + ")" + ((avgDat == null) ? "" : ", 平均=" + avgDat._avg + "(" + avgDat._avgKansan + ")"));
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }

            // チャート作成
            final JFreeChart chart = Chart.createBarChart(param, scoreDataset, avgDataset);

            // グラフのファイルを生成
            final int w = "2".equals(param._formDiv) ? 2810 : 1940;
            final int h = "2".equals(param._formDiv) ? 790 : 930;
            final File outputFile = graphImageFile(chart, w, h);
            param._graphFiles.add(outputFile);
            return outputFile;
        }

        private static File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
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

        private static void removeImageFiles(final Param param) {
            for (final File imageFile : param._graphFiles) {
                if (null == imageFile) {
                    continue;
                }
                log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
            }
        }

        private static JFreeChart createBarChart(
                final Param param,
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset
        ) {
            final boolean createLegend = "2".equals(param._formDiv);
            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, createLegend, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            if ("2".equals(param._formDiv)) {
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
            plot.setBackgroundPaint(Color.white);

            final CategoryItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.darkGray);
            final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
            renderer.setItemLabelFont(itemLabelFont);
            renderer.setItemLabelsVisible(true);

            ((BarRenderer) renderer).setMaximumBarWidth(0.05);
            ((BarRenderer) renderer).setBarPainter(new StandardBarPainter());

            chart.setBackgroundPaint(Color.white);

            return chart;
        }
        
        public static File radarGraphFile(final Param param, final Map<SubClass, Record> records) {
            // データ作成
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final String classCd : param._classOrder) {
                
                for (final Record record : records.values()) {
                    if (!classCd.equals(record._subClass._code)) {
                        continue;
                    }
                    Chart.setDataset(dataset, record);
                }
            }

            if (0 == dataset.getColumnCount()) {
                return null;
            }
            // チャート作成
            final JFreeChart chart = Chart.createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = graphImageFile(chart, 930, 822);
            param._graphFiles.add(outputFile);
            return outputFile;
        }

        private static void setDataset(final DefaultCategoryDataset dataset, final Record record) {
            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
            if (record._deviation == null) {
                return;
            }
//            final Class clazz = _param._classes.get(record._subClass.getClassCd());
//            final String name = (null == clazz) ? "???" : clazz._abbv;
//            final String name = (null == clazz) ? record._subClass._abbv : clazz._abbv;
            final String name = record._subClass._abbv;
            dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20
            
            dataset.addValue(50.0, "偏差値50", name);
        }

        private static JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
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

    }

    private static class Student implements Comparable<Student> {
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
        private final Map<String, SubClass> _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map<SubClass, Record> _record = new TreeMap();

        /** 成績データ。3教科,5教科用 */
        private final Map<String, Record> _recordOther = new HashMap();

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
            _coursegroupCd = coursegroupCd;
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
        
        public Address getAddress(final Param param) {
            final Address address = new Address();
            if ("1".equals(param._useAddress)) { // 印刷なし
            } else if ("2".equals(param._useAddress)) { // 保護者
                address._zipCd = _guardZipCd;
                address._addr1 = _guardAddr1;
                address._addr2 = _guardAddr2;
                address._addresee = _guardName;
            } else if ("3".equals(param._useAddress)) { // 負担者
                address._zipCd = _guarantorZipCd;
                address._addr1 = _guarantorAddr1;
                address._addr2 = _guarantorAddr2;
                address._addresee = _guarantorName;
            } else {
                address._zipCd = _sendAddressZipCd;
                address._addr1 = _sendAddressAddr1;
                address._addr2 = _sendAddressAddr2;
                address._addresee = _sendAddressName;
            }
            return address;
        }

        public int compareTo(final Student that) {
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
        
        private static List<Student> createStudents(final DB2UDB db2, final Param param, final String[] schregnos) throws SQLException {
            final List<Student> rtn = new LinkedList();
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
                    final String coursegroupCd = rs.getString("group_cd");
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
                            coursegroupCd,
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
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            Collections.sort(rtn);
            return rtn;
        }

        private static String studentsSQL(final Param param, final String[] selected) {
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
                + "  t4.group_cd,"
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
                + " LEFT JOIN course_group_cd_dat t4 ON t4.year = t1.year and t4.grade = t1.grade and t4.coursecd = t1.coursecd and t4.majorcd = t1.majorcd and t4.coursecode = t1.coursecode "
                + " WHERE"
                + "  t1.year='" + param._year + "' AND"
                + "  t1.semester='" + param._semester + "' AND"
                + "  t1.schregno IN " + students
                ;
            return sql;
        }
    }
    
    private static class Address {
        String _zipCd;
        String _addr1;
        String _addr2;
        String _addresee;
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
    private static class SubClass implements Comparable<SubClass> {
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

        public int compareTo(final SubClass that) {
            return this._code.compareTo(that._code);
        }
    }

    private static class Record {
        private final SubClass _subClass;
        private final Integer _score;
        private final String _scoreDi;
        private final Integer _graphScore;
        private final BigDecimal _avg;
        private final Integer _rank;
        private final BigDecimal _deviation;
//        private final Integer _passScore;

        private Record(
                final SubClass subClass,
                final Integer score,
                final String scoreDi,
                final Integer graphScore,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation
//                final Integer passScore
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
            _graphScore = graphScore;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
//            _passScore = passScore;
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

//        public String getPassScore() {
//            return null == _passScore ? "" : _passScore.toString();
//        }
        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
        
        private static void loadRecord1(
                final DB2UDB db2,
                final Param param,
                final Exam exam,
                final List<Student> students
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

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
                + "  T7.rank as hr_rank,"
                + "  T7.deviation as hr_deviation,"
                + "  T8.rank as major_rank,"
                + "  T8.deviation as major_deviation,"
                + "  T9.score as score_kansannashi"
//                + "  ,T5.PASS_SCORE"
                + " FROM proficiency_dat T1 "
                + " LEFT JOIN proficiency_rank_dat T3 ON "
                + "     T3.year = T1.year AND "
                + "     T3.semester=T1.semester AND"
                + "     T3.proficiencydiv=T1.proficiencydiv AND"
                + "     T3.proficiencycd=T1.proficiencycd AND"
                + "     T3.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
                + "     T3.schregno = T1.schregno AND"
                + "     T3.rank_data_div ='" + param._rankDataDiv + "' AND"
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
//                + " LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT T5 ON T5.YEAR = T1.YEAR AND "
//                + "     T5.semester=T2.semester AND"
//                + "     T5.proficiencydiv=T2.proficiencydiv AND"
//                + "     T5.proficiencycd=T2.proficiencycd AND"
//                + "     T5.proficiency_subclass_cd=T2.proficiency_subclass_cd "
//                + "     AND T5.GRADE = CASE WHEN T5.DIV = '01' THEN '00' ELSE ? END  "
//                + "     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = "
//                + "       CASE WHEN T5.DIV = '01' OR T5.DIV = '02' THEN '00000000' ELSE ? END "
                + " LEFT JOIN proficiency_rank_dat T7 ON "
                + "     T7.year = T1.year AND "
                + "     T7.semester=T1.semester AND"
                + "     T7.proficiencydiv=T1.proficiencydiv AND"
                + "     T7.proficiencycd=T1.proficiencycd AND"
                + "     T7.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
                + "     T7.schregno = T1.schregno AND"
                + "     T7.rank_data_div =t3.rank_data_div AND"
                + "     T7.rank_div = '" + RANK_DIV_HR + "' "
                + " LEFT JOIN proficiency_rank_dat T8 ON "
                + "     T8.year = T1.year AND "
                + "     T8.semester=T1.semester AND"
                + "     T8.proficiencydiv=T1.proficiencydiv AND"
                + "     T8.proficiencycd=T1.proficiencycd AND"
                + "     T8.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
                + "     T8.schregno = T1.schregno AND"
                + "     T8.rank_data_div =t3.rank_data_div AND"
                + "     T8.rank_div = '" + RANK_DIV_MAJOR + "' "
                + " LEFT JOIN proficiency_rank_dat T9 ON "
                + "     T1.year = T9.year AND "
                + "     T1.semester=T9.semester AND"
                + "     T1.proficiencydiv=T9.proficiencydiv AND"
                + "     T1.proficiencycd=T9.proficiencycd AND"
                + "     T1.proficiency_subclass_cd=T9.proficiency_subclass_cd AND"
                + "     T1.schregno = T9.schregno AND "
                + "     T9.rank_data_div = '01' AND "
                + "     T9.rank_div = '" + RANK_DIV_GRADE + "' "
                + " WHERE"
                + "  T1.year='" + exam._year + "' AND"
                + "  T1.semester='" + exam._semester + "' AND"
                + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                + "  T1.proficiency_subclass_cd NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
                + "  T1.schregno=?"
                ;
            try {
                final String rankField = param.isCourse() ? "course_rank" : "grade_rank";
                final String deviationField = param.isCourse() ? "course_deviation" : "grade_deviation";

                ps = db2.prepareStatement(sql);
                for (final Student student : students) {
                    int i = 0;
                    //欠点（満点マスタの合格点）
//                    ps.setString(++i, student._grade);
//                    ps.setString(++i, student.courseKey());
                    ps.setString(++i, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        SubClass subClass = param._subClasses.get(subclasscd);
                        if (null == subClass) {
                            log.warn("対象成績データが模試科目マスタに無い!:" + subclasscd);
                            final Class clazz = param._classes.get(subclasscd.substring(0, 2));
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
                        final Integer rank = KNJServletUtils.getInteger(rs, rankField);
                        final BigDecimal deviation = rs.getBigDecimal(deviationField);

                        final Record rec = new Record(subClass, score, scoreDi, graphScore, null, rank, deviation);
                        student._record.put(subClass, rec);
                    }
                }
            } catch (final SQLException e) {
                log.error("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            
            final String sql2 = "SELECT"
                    + "  t1.proficiency_subclass_cd as subclasscd,"
                    + "  t1.score,"
                    + "  t2.avg,"
                    + "  t1.rank as grade_rank,"
                    + "  t1.deviation as grade_deviation,"
                    + "  t3.rank as course_rank,"
                    + "  t3.deviation as course_deviation,"
                    + "  t4.rank as coursegroup_rank,"
                    + "  t4.deviation as coursegroup_deviation,"
                    + "  t5.rank as major_rank,"
                    + "  t5.deviation as major_deviation,"
                    + "  t6.rank as hr_rank,"
                    + "  t6.deviation as hr_deviation,"
                    + "  T7.score as score_kansannashi"
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
                    + " LEFT JOIN proficiency_rank_dat T5 ON "
                    + "     T1.year = T5.year AND "
                    + "     T1.semester=T5.semester AND"
                    + "     T1.proficiencydiv=T5.proficiencydiv AND"
                    + "     T1.proficiencycd=T5.proficiencycd AND"
                    + "     T1.proficiency_subclass_cd=T5.proficiency_subclass_cd AND"
                    + "     T1.schregno = T5.schregno AND "
                    + "     T1.rank_data_div = T5.rank_data_div AND "
                    + "     T5.rank_div = '" + RANK_DIV_MAJOR + "' "
                    + " LEFT JOIN proficiency_rank_dat T6 ON "
                    + "     T1.year = T6.year AND "
                    + "     T1.semester=T6.semester AND"
                    + "     T1.proficiencydiv=T6.proficiencydiv AND"
                    + "     T1.proficiencycd=T6.proficiencycd AND"
                    + "     T1.proficiency_subclass_cd=T6.proficiency_subclass_cd AND"
                    + "     T1.schregno = T6.schregno AND "
                    + "     T1.rank_data_div = T6.rank_data_div AND "
                    + "     T6.rank_div = '" + RANK_DIV_HR + "' "
                    + " LEFT JOIN proficiency_rank_dat T7 ON "
                    + "     T1.year = T7.year AND "
                    + "     T1.semester=T7.semester AND"
                    + "     T1.proficiencydiv=T7.proficiencydiv AND"
                    + "     T1.proficiencycd=T7.proficiencycd AND"
                    + "     T1.proficiency_subclass_cd=T7.proficiency_subclass_cd AND"
                    + "     T1.schregno = T7.schregno AND "
                    + "     '01' = T7.rank_data_div AND "
                    + "     T7.rank_div = '" + RANK_DIV_GRADE + "' "
                    + " WHERE"
                    + "  T1.year='" + exam._year + "' AND"
                    + "  T1.semester='" + exam._semester + "' AND"
                    + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                    + "  T1.proficiency_subclass_cd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
                    + "  t1.rank_data_div ='" + param._rankDataDiv + "' AND"
                    + "  t1.rank_div ='" + RANK_DIV_GRADE + "' AND"
                    + "  t1.schregno=? "
                    ;
            try {
                ps = db2.prepareStatement(sql2);

                for (final Student student : students) {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        final Integer score = KNJServletUtils.getInteger(rs, "score");
                        final BigDecimal avg = rs.getBigDecimal("avg");
                        
                        final Integer rank;
                        final BigDecimal deviation;
                        if (param.isHr()) {
                            rank = KNJServletUtils.getInteger(rs, "hr_rank");
                            deviation = rs.getBigDecimal("hr_deviation");
                        } else if (param.isCourse()) {
                            rank = KNJServletUtils.getInteger(rs, "course_rank");
                            deviation = rs.getBigDecimal("course_deviation");
                        } else if (param.isMajor()) {
                            rank = KNJServletUtils.getInteger(rs, "major_rank");
                            deviation = rs.getBigDecimal("major_deviation");
                        } else if (param.isCoursegroup()) {
                            rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
                            deviation = rs.getBigDecimal("coursegroup_deviation");
                        } else {
                            rank = KNJServletUtils.getInteger(rs, "grade_rank");
                            deviation = rs.getBigDecimal("grade_deviation");
                        }

                        final SubClass subClass = new SubClass(subclasscd);
                        final Record rec = new Record(subClass, score, null, null, avg, rank, deviation);
                        student._recordOther.put(subClass._code, rec);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            DbUtils.closeQuietly(ps);
        }
        
        private static void loadStudentSubClasses(final DB2UDB db2, final Param param, final Exam exam, final List<Student> students) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql;
                sql = "select"
                        + "  distinct T1.PROFICIENCY_SUBCLASS_CD as SUBCLASSCD"
                        + " from"
                        + "  PROFICIENCY_DAT T1"
                        + " where"
                        + "  T1.YEAR = '" + exam._year + "' and"
                        + "  t1.semester ='" + exam._semester + "' AND"
                        + "  t1.proficiencydiv ='" + exam._proficiencydiv + "' AND"
                        + "  t1.proficiencycd ='" + exam._proficiencycd + "' AND"
                        + "  T1.SCHREGNO = ? "
                        ;
                log.debug("模試データにある科目のSQL=" + sql);

                ps = db2.prepareStatement(sql);
                for (final Student student : students) {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subClasscd = rs.getString("SUBCLASSCD");

                        final SubClass subClass = param._subClasses.get(subClasscd);
                        if (null != subClass) {
                            student._subclasses.put(subClasscd, subClass);
                        }
                    }
                }
            } catch (final SQLException e) {
                log.fatal("模試データにある科目の取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
    
    private static class ScoreDistribution {
        private final String _distKey;
        private final Map<String, SubclassScoreDistribution> _subclassDistributions = new HashMap();
        
        private ScoreDistribution(final String key) {
            _distKey = key;
        }
        
        private SubclassScoreDistribution getSubclassDistributionMap(String subClassCd) {
            if (!_subclassDistributions.containsKey(subClassCd)) {
                _subclassDistributions.put(subClassCd, new SubclassScoreDistribution());
            }
            return _subclassDistributions.get(subClassCd);
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
            for (final String subClassCd : _subclassDistributions.keySet()) {
                final SubclassScoreDistribution ssd = _subclassDistributions.get(subClassCd);
                stb.append(comma).append("[subClass=").append(subClassCd).append(" ").append(ssd.toString()).append("]");
                comma = ",";
            }
            return stb.toString();
        }

        public String toString() {
            return " dist = (" + distStr() + ")";
        }
        
        private static void loadScoreDistribution(
                final DB2UDB db2,
                final Param param,
                final Exam exam,
                final List students
        ) {
            final String sql;
            sql = "SELECT"
                    + "  regd.grade,"
                    + "  regd.majorcd,"
                    + "  regd.coursecd || regd.majorcd || regd.coursecode as courseKey,"
                    + "  T1.proficiency_subclass_cd as subclasscd,"
                    + "  T1.score"
                    + " FROM"
                    + "  proficiency_rank_dat T1 "
                    + "  INNER JOIN schreg_regd_dat regd ON"
                    + "    T1.year=regd.year AND"
                    + "    '" + param._semester + "' =regd.semester AND"
                    + "    T1.schregno=regd.schregno"
                    + " WHERE"
                    + "  T1.year='" + exam._year + "' AND"
                    + "  T1.semester='" + exam._semester + "' AND"
                    + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                    + "  T1.proficiency_subclass_cd NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
                    + "  t1.rank_data_div ='" + param._rankDataDiv + "' AND"
                    + "  t1.rank_div ='" + RANK_DIV_GRADE + "' AND"
                    + "  regd.grade='" + param._grade + "'"
                    ;

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                        continue;
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    if (score == null) {
                        continue;
                    }
                    final String key = param.getDistKey(rs.getString("grade"), rs.getString("courseKey"), rs.getString("majorcd"));
                    
                    if (!exam._scoreDistributionMap.containsKey(key)) {
                        exam._scoreDistributionMap.put(key, new ScoreDistribution(key));
                    }
                    final ScoreDistribution sd = exam._scoreDistributionMap.get(key);
                    sd.add(subClass, score);
                }
                
                if (log.isDebugEnabled()) {
                    for (final String key : exam._scoreDistributionMap.keySet()) {
                        ScoreDistribution sd = exam._scoreDistributionMap.get(key);
                        log.debug(" key = " + key + ", distribution = " + sd);
                    }
                }
                
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final String sql2 = "SELECT"
                + "  t1.proficiency_subclass_cd as subclasscd,"
                + "  regd.grade,"
                + "  regd.majorcd,"
                + "  regd.coursecd || regd.majorcd || regd.coursecode as courseKey,"
                + "  t1.avg as score"
                + " FROM PROFICIENCY_rank_dat t1"
                + " inner join schreg_regd_dat regd on "
                + "  t1.year=regd.year AND"
                + "  '" + param._semester + "' =regd.semester AND"
                + "  t1.schregno=regd.schregno"
                + " WHERE"
                + "  t1.year='" + param._year + "' AND"
                + "  t1.semester ='" + exam._semester + "' AND"
                + "  t1.proficiencydiv ='" + exam._proficiencydiv + "' AND"
                + "  t1.proficiencycd ='" + exam._proficiencycd + "' AND"
                + "  t1.proficiency_subclass_cd IN ('" + ALL3 + "', '" + ALL5 + "') AND"
                + "  t1.rank_data_div ='" + param._rankDataDiv + "' AND"
                + "  t1.rank_div ='" + RANK_DIV_GRADE + "' AND"
                + "  regd.grade='" + param._grade + "' AND"
                + "  t1.avg is not null"
                ;
            try {
                ps = db2.prepareStatement(sql2);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Double score = KNJServletUtils.getDouble(rs, "score");

                    final String distKey = param.getDistKey(rs.getString("grade"), rs.getString("courseKey"), rs.getString("majorcd"));
                    final SubClass subClass = new SubClass(subclasscd);
                    
                    if (!exam._scoreDistributionMap.containsKey(distKey)) {
                        exam._scoreDistributionMap.put(distKey, new ScoreDistribution(distKey));
                    }

                    ScoreDistribution dist = exam._scoreDistributionMap.get(distKey);
                    dist.add(subClass, new Integer(score.intValue()));
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
    
    private static class ScoreCount {
        final String _key;
        final int _rangeLower;
        final int _rangeUpper;
        final List<Integer> _scoreList = new ArrayList();
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
                if (_scoreCounts[i]._key.equals(scoreKey)) {
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

    private static class AverageDat {
        private final SubClass _subClass;
        private final BigDecimal _avg;
        private final BigDecimal _avgKansan;
        private final Integer _highScore;
        private final Integer _count;
        private final String _coursecd;
        private final String _majorcd;
        private final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal avgKansan,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _avgKansan = avgKansan;
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
        
        /**
         * 成績平均データを読込み、セットする。
         * @param db2 DB
         * @param kindCd テスト種別コード
         * @param itemCd テスト項目コード
         * @param outAvgDat 成績平均データの格納場所
         * @param outAvgDatOther 成績平均データ(3,5教科)の格納場所
         * @throws SQLException SQL例外
         */
        public static void loadAverageDat(
                final DB2UDB db2,
                final Param param,
                final Exam exam
        ) throws SQLException {
            final String selectFrom;
            selectFrom = "SELECT"
                + "  PROFICIENCY_subclass_cd as subclasscd,"
                + "  avg,"
                + "  highscore,"
                + "  avg_kansan,"
                + "  count,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode"
                + " FROM"
                + "  proficiency_average_dat"
                ;
            final String where;
            if (param.isHr()) {
                where = " WHERE"
                    + "    year='" + exam._year + "' AND"
                    + "    semester='" + exam._semester + "' AND"
                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
                    + "    data_div = '" + param._avgDataDiv + "' AND"
                    + "    avg_div='" + AVG_DIV_HR + "' AND"
                    + "    grade='" + exam._grade + "'"
                    ;
            } else if (param.isCourse()) {
                where = " WHERE"
                    + "    year='" + exam._year + "' AND"
                    + "    semester='" + exam._semester + "' AND"
                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
                    + "    data_div = '" + param._avgDataDiv + "' AND"
                    + "    avg_div='" + AVG_DIV_COURSE + "' AND"
                    + "    grade='" + exam._grade + "' AND"
                    + "    hr_class='000'"
                    ;
            } else if (param.isMajor()) {
                where = " WHERE"
                    + "    year='" + exam._year + "' AND"
                    + "    semester='" + exam._semester + "' AND"
                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
                    + "    data_div = '" + param._avgDataDiv + "' AND"
                    + "    avg_div='" + AVG_DIV_MAJOR + "' AND"
                    + "    grade='" + exam._grade + "' AND"
                    + "    hr_class='000'"
                    ;
            } else if (param.isCoursegroup()) {
                where = " WHERE"
                    + "    year='" + exam._year + "' AND"
                    + "    semester='" + exam._semester + "' AND"
                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
                    + "    data_div = '" + param._avgDataDiv + "' AND"
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
                        + "    data_div = '" + param._avgDataDiv + "' AND"
                        + "    avg_div='" + AVG_DIV_GRADE + "' AND"
                        + "    grade='" + exam._grade + "' AND"
                        + "    hr_class='000' AND"
                        + "    coursecd='0' AND"
                        + "    majorcd='000' AND"
                        + "    coursecode='0000'"
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
                    final SubClass subClass = param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("模試成績平均データが模試科目マスタに無い!:" + subclasscd);
                    }
                    final BigDecimal avg;
                    if ("1".equals(param._knjd106fPrintAvgKansannashi)) {
                        avg = rs.getBigDecimal("avg");
                    } else {
                        avg = rs.getBigDecimal("avg_kansan");
                    }
                    final BigDecimal avgKansan = rs.getBigDecimal("avg_kansan");
                    final Integer count = KNJServletUtils.getInteger(rs, "count");
                    final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");

                    final String key = param.subclassKey(subclasscd, coursecd + majorcd + coursecode, majorcd);
                    if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd)) {
                        final SubClass subClass0 = new SubClass(subclasscd);
                        final AverageDat avgDat = new AverageDat(subClass0, avg, avgKansan, highScore, count, coursecd, majorcd, coursecode);
                        exam._averageDatOtherMap.put(key, avgDat);
                    } else {
                        final AverageDat avgDat = new AverageDat(subClass, avg, avgKansan, highScore, count, coursecd, majorcd, coursecode);
                        exam._averageDatMap.put(key, avgDat);
                    }
                }
            } catch (final SQLException e) {
                log.warn("模試成績平均データの取得でエラー");
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug("模試コード=" + param._semester + " " + param._proficiencydiv + param._proficiencycd + " の模試成績平均データの件数=" + exam._averageDatMap.size());
        }
    }
    
    private static class Exam {

        private final String _year;
        private final String _semester;
        private final String _proficiencydiv;
        private final String _proficiencycd;
        private final String _grade;
        private String _examName = "";
//        private String _semestername = "";
//        private String _title = "";
        
        /** 成績平均データ。 */
        private Map<String, AverageDat> _averageDatMap = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map<String, AverageDat> _averageDatOtherMap = new HashMap();
        /** 度数分布データ。*/
        private Map<String, ScoreDistribution> _scoreDistributionMap = new HashMap();

        private final Map<String, String> _subclassGroup3 = new HashMap();
        private final Map<String, String> _subclassGroup5 = new HashMap();
        private final Map<String, List<String>> _subclassGroupDat3 = new HashMap();
        private final Map<String, List<String>> _subclassGroupDat5 = new HashMap();
        
        /** レーダーチャートの科目. */
        private List<String> _fiveSubclass = new ArrayList();
        private List<String> _threeSubclass = new ArrayList();

        /** 模試文面データ。全体評。 */
        private Map<String, String> _document = new HashMap();

        public Exam(final String year, final String semester, final String proficiencydiv, final String proficiencycd, final String grade) {
            _year = year;
            _semester = semester;
            _proficiencydiv = proficiencydiv;
            _proficiencycd = proficiencycd;
            _grade = grade;
        }

        private void load(final DB2UDB db2, final Param param, final List<Student> students) throws SQLException {
            loadExam(db2);
            // proficiency_subclass_group_dat を読込んで _fiveSubclass や _threeSubclass の箱に入れる
            loadSubclassGroup(db2, param);
            loadSubclassGroupDat(db2, param);
            
            // loadExamname(db2);
            loadDocumentDat(db2);
            
            log.info("★成績関連の読込み");
            Record.loadStudentSubClasses(db2, param, this, students);
            AverageDat.loadAverageDat(db2, param, this);
            Record.loadRecord1(db2, param, this, students);
            if ("2".equals(param._formDiv)) {
                ScoreDistribution.loadScoreDistribution(db2, param, this, students);
            }
        }

        private void loadExam(final DB2UDB db2) {
            if (null == _year) {
                //_title = "";
                return;
            }
            _examName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT proficiencyname1 FROM proficiency_mst WHERE proficiencydiv = '" + _proficiencydiv + "' and proficiencycd = '" + _proficiencycd + "' "));
//            _semestername = "";
//            try {
//                final String sql = "SELECT semestername FROM semester_mst"
//                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
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
        }
        
        String get3title(final String courseKey) {
            return _subclassGroup3.get(courseKey);
        }

        String get5title(final String courseKey) {
            return _subclassGroup5.get(courseKey);
        }

        public void setSubclasses(final Student student) {
            _fiveSubclass = _subclassGroupDat5.get(student.courseKey());
            _threeSubclass = _subclassGroupDat3.get(student.courseKey());
        }

        private void loadSubclassGroup(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT"
                        + "  group_div,"
                        + "  coursecd,"
                        + "  majorcd,"
                        + "  coursecode,"
                        + "  group_name"
                        + " FROM"
                        + "  PROFICIENCY_SUBCLASS_GROUP_MST "
                        + " WHERE"
                        + "  year='" + _year + "' AND"
                        + "  semester ='" + _semester + "' AND"
                        + "  proficiencydiv ='" + _proficiencydiv + "' AND"
                        + "  proficiencycd ='" + _proficiencycd + "' AND"
                        + "  grade='" + _grade + "' AND"
                        + "  group_div in ('3', '5')"   // 3教科, 5教科
                        ;

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
                log.error("PROFICIENCY_SUBCLASS_GROUP_MST の取得エラー。", e);
            }

            log.debug("3教科の名称たち=" + _subclassGroup3);
            log.debug("5教科の名称たち=" + _subclassGroup5);
        }

        public static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<T>());
            }
            return map.get(key1);
        }

        private void loadSubclassGroupDat(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT"
                        + "  group_div,"
                        + "  coursecd,"
                        + "  majorcd,"
                        + "  coursecode,"
                        + "  PROFICIENCY_SUBCLASS_CD as subclasscd"
                        + " FROM"
                        + "  PROFICIENCY_SUBCLASS_GROUP_DAT "
                        + " WHERE"
                        + "  year='" + _year + "' AND"
                        + "  semester ='" + _semester + "' AND"
                        + "  proficiencydiv ='" + _proficiencydiv + "' AND"
                        + "  proficiencycd ='" + _proficiencycd + "' AND"
                        + "  grade='" + _grade + "' AND"
                        + "  group_div in ('3', '5')"   // 3教科, 5教科
                        ;

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
                    	getMappedList(_subclassGroupDat3, key).add(subclassCd);
                    } else {
                    	getMappedList(_subclassGroupDat5, key).add(subclassCd);
                    }
                }
            } catch (final SQLException e) {
                log.error("proficiency_subclass_group_dat の取得エラー", e);
            }

            log.debug("3教科の科目CDたち=" + _subclassGroupDat3);
            log.debug("5教科の科目CDたち=" + _subclassGroupDat5);
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
                + "  proficiency_document_dat"
                + " WHERE"
                + "  year='" + _year + "' AND"
                + "  semester ='" + _semester + "' AND"
                + "  proficiencydiv ='" + _proficiencydiv + "' AND"
                + "  proficiencycd ='" + _proficiencycd + "' AND"
                + "  grade='" + _grade + "' AND"
                + "  proficiency_subclass_cd='999999'"
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
                log.error("模試文面データ取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

//        private void loadExamname(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = "SELECT proficiencyname1 FROM proficiency_mst WHERE "
//                + "  proficiencydiv ='" + _proficiencydiv + "' AND"
//                + "  proficiencycd ='" + _proficiencycd + "' ";
//
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _examName = rs.getString("proficiencyname1");
//                }
//            } catch (final SQLException e) {
//                log.error("proficiency_mst の取得エラー。");
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//        }

        public String getWholeRemark(final Student student) {
            final String rtn = _document.get(student._grade + student._courseCd + student._majorCd + student._courseCode);
            return rtn;
        }
        
//        String nendo() {
//            return KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
//        }
        
        public String toString() {
            return "Exam(" + _year + ":" + _semester + ":" + _proficiencydiv + ":" + _proficiencycd + ":" + _examName + ")";
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _grade;
        final String _schoolKind;

        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;
        /** 中高一貫フラグ。 */
        private boolean _isIkkan;
        final String _nendo;

        /** [クラス指定 or 生徒指定]の値。 */
        final String _categoryIsClass;
        /** クラス or 生徒。 */
        final String[] _categorySelected;

        final String _useAddress;
        final String _submitDate;
        final String _loginDate;

        /** クラス指定 or 生徒指定。 */
        final boolean _isClassMode;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 偏差値を印字するか? */
        final boolean _deviationPrint;
        /** 順位を印字しないか? */
        final boolean _rankNotPrint;

        private final Map<String, String> _staffs = new HashMap();

        /** 教科マスタ。 */
        private Map<String, Class> _classes;

        /** 模試科目マスタ。 */
        private Map<String, SubClass> _subClasses;

        private final String _imagePath;

        private final List<String> _classOrder = new ArrayList();
        
        private boolean _isKumamoto;
        
        private final int _ketten;
        
        /** 得点分布表を表示するか */
        private final String _formDiv;
        
        final String _groupDiv;
        /** 順位の基準点 1:総合点,2:平均点,3:偏差値 */
        final String _rankDataDiv;
        final String _avgDataDiv;
        final String _knjd106fPrintAvgKansannashi;

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set<File> _graphFiles = new HashSet<File>();

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _useAddress = request.getParameter("USE_ADDRESS");
            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = ? AND GRADE = ? ", new Object[] {_year, _grade}));
            _groupDiv = request.getParameter("FORM_GROUP_DIV");

            if ("4".equals(request.getParameter("JUNI"))) {
                _rankDataDiv = "11";
                _avgDataDiv = "2";
            } else {
                final String rankDivTemp = request.getParameter("useKnjd106cJuni" + request.getParameter("JUNI"));
                final String rankDataDiv0 = (rankDivTemp == null) ? request.getParameter("JUNI") : rankDivTemp;
                _rankDataDiv = (null != rankDataDiv0 && rankDataDiv0.length() < 2 ? "0" : "") + rankDataDiv0;
                _avgDataDiv = "1";
            }
            _knjd106fPrintAvgKansannashi = request.getParameter("knjd106fPrintAvgKansannashi");
            _isClassMode = "1".equals(request.getParameter("CATEGORY_IS_CLASS"));

            _deviationPrint = "1".equals(request.getParameter("DEVIATION_PRINT"));
            _rankNotPrint = "1".equals(request.getParameter("JUNI_PRINT"));
            _imagePath = request.getParameter("IMAGE_PATH");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year));

            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            
            final String ketten = (null == request.getParameter("KETTEN") || "".equals(request.getParameter("KETTEN"))) ? null : request.getParameter("KETTEN");
            _ketten = StringUtils.isNumeric(ketten) ? Integer.parseInt(ketten) : 0;
            _formDiv = request.getParameter("FORM_DIV");
        }

        private void imageFileCheck(final String fName) {
            final File f = new File(_imagePath + "/" + fName);
            if (!f.exists()) {
                log.fatal("画像ファイルが無い!⇒" + _imagePath + "/" + fName);
            }
        }

        private String getDistKey(final String grade, final String courseKey, final String coursegroupKey) {
            final String key;
            if (isCourse()) {
                key = grade + courseKey;
            } else if (isCoursegroup()) {
            	key = grade + coursegroupKey;
            } else {
                key = grade;
            }
            return key;
        }
        
        private String subclassKey(final String subclasscd, final String courseKey, final String coursegroupKey) {
            final String key;
            if (isCourse()) {
                key = subclasscd + courseKey;
            } else if (isCoursegroup()) {
            	key = subclasscd + coursegroupKey;
            } else {
                key = subclasscd;
            }
            return key;
        }

        public String[] getScregnos(final DB2UDB db2) throws SQLException {
            final List<String> result = new ArrayList();

            if (!_isClassMode) {
                return _categorySelected;
            }

            // 年組から学籍番号たちを得る
            for (int i = 0; i < _categorySelected.length; i++) {
                final String grade = StringUtils.split(_categorySelected[i], "-")[0];
                final String room = StringUtils.split(_categorySelected[i], "-")[1];
                
                final String sql = " select"
                        + "    SCHREGNO "
                        + " from"
                        + "    SCHREG_REGD_DAT"
                        + " where"
                        + "    YEAR = '" + _year + "' and"
                        + "    SEMESTER = '" + _semester + "' and"
                        + "    GRADE = '" + grade + "' and"
                        + "    HR_CLASS = '" + room + "'"
                        + " order by "
                        + "    ATTENDNO "
                        ;

                result.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "SCHREGNO"));
            }

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final String schregno : result) {
                rtn[i++] = schregno;
            }

            return rtn;
        }

        public void load(final DB2UDB db2) throws SQLException {
            loadZ010(db2);
            loadIkkanFlg(db2);
            loadCertifSchool(db2);
            loadStaffname(db2);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);

            loadClassOrder(db2);
        }

        private void loadZ010(DB2UDB db2) {
        	final String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'Z010' AND YEAR = '"  + _year  + "' "));
        	_isKumamoto = "kumamoto".equals(name1);
        }

        private Map<String, SubClass> setSubClasses(final DB2UDB db2) throws SQLException {
            final Map<String, SubClass> rtn = new HashMap();
            final String sql = "select"
                    + "   PROFICIENCY_SUBCLASS_CD as SUBCLASSCD,"
                    + "   SUBCLASS_NAME as NAME,"
                    + "   SUBCLASS_ABBV as SUBCLASSABBV"
                    + " from PROFICIENCY_SUBCLASS_MST "
                    + " order by"
                    + "   PROFICIENCY_SUBCLASS_CD";

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String code = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String name = KnjDbUtils.getString(row, "NAME");
                final String abbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                rtn.put(code, new SubClass(code, name, abbv));
            }
            log.debug("模試科目マスタ総数=" + rtn.size());
            return rtn;
        }

        private Map<String, Class> setClasses(final DB2UDB db2) throws SQLException {
            final Map<String, Class> rtn = new HashMap();
            final String sql = "select"
            		+ "   CLASSCD,"
            		+ "   CLASSNAME,"
            		+ "   CLASSABBV"
            		+ " from V_CLASS_MST"
            		+ " where"
            		+ "   YEAR = '" + _year + "' " + (KnjDbUtils.setTableColumnCheck(db2, "V_CLASS_MST", "SCHOOL_KIND") ? " AND SCHOOL_KIND = '" + _schoolKind + "' " : "")
            		+ " order by"
            		+ "   CLASSCD"
            		;
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String code = KnjDbUtils.getString(row, "CLASSCD");
                final String name = KnjDbUtils.getString(row, "CLASSNAME");
                final String abbv = KnjDbUtils.getString(row, "CLASSABBV");
                rtn.put(code, new Class(code, name, abbv));
            }
            log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        private void loadStaffname(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS GRADE_HR_CLASS, t2.STAFFNAME "
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year=t2.year AND t1.tr_cd1=t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            _staffs.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "GRADE_HR_CLASS", "STAFFNAME"));
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
                log.error("学校名取得エラー。", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadClassOrder(final DB2UDB db2) {
            final String field1 = _isJunior ? "name1" : "name2";
            final String field2 = _isJunior ? "namespare1" : "namespare2";
            final String sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
                + " WHERE year='" + _year + "' AND namecd1='D014'"
                + " ORDER BY " + field2
                ;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _classOrder.add(rs.getString("classcd"));
                }
            } catch (final SQLException e) {
                log.error("教科表示順取得エラー。", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info("教科表示順=" + _classOrder);
        }

        private void loadIkkanFlg(final DB2UDB db2) {
        	final String namespare2 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT namespare2 FROM v_name_mst WHERE year='" + _year + "' AND namecd1='Z010' AND namecd2='00'"));
        	_isIkkan = (namespare2 != null);
        	
        	final int gradeVal = Integer.parseInt(_grade);
        	_isJunior = (gradeVal <= 3 && _isIkkan);
        	_isHigh = !_isJunior;
            log.debug("中高一貫フラグ=" + _isIkkan);
        }

        public boolean isUnderScore(final String score) {
            if (StringUtils.isEmpty(score) || !StringUtils.isNumeric(score)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            return val < _ketten;
        }

        public boolean isHr() {
            return "2".equals(_groupDiv);
        }

        public boolean isCourse() {
            return "3".equals(_groupDiv);
        }
        
        public boolean isMajor() {
            return "4".equals(_groupDiv);
        }
        
        public boolean isCoursegroup() {
            return "5".equals(_groupDiv);
        }
    }

} // KNJD106F

// eof
