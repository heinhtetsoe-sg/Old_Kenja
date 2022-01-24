// kanji=漢字
/*
 * $Id: 140e8190b6670c2a865c28deb20b1c06757232f3 $
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
import java.sql.SQLException;
import java.util.ArrayList;
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: 140e8190b6670c2a865c28deb20b1c06757232f3 $
 */
public class KNJD105U {
    private static final Log log = LogFactory.getLog(KNJD105U.class);

    private static final String ALL9 = "999999";

    private static final String FORM = "KNJD105U.frm";

    private boolean _hasData;

    /** 成績表の最大科目数. */
    private final int TABLE_SUBCLASS_MAX = 20;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }

        log.fatal("$Revision: 65982 $ $Date: 2019-03-01 15:45:54 +0900 (金, 01 3 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);

        if ("csv".equals(param._cmd)) {
        } else {
            Vrw32alp svf = new Vrw32alp();
            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            try {
                if (!KNJServletUtils.isEnableGraph(log)) {
                    log.fatal("グラフを使える環境ではありません。");
                }

                printMain(param, svf, db2);

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
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

                if (null != param) {
                    int count = 0;
                    for (final Iterator it = param._graphFiles.iterator(); it.hasNext();) {
                        final File imageFile = (File) it.next();
                        if (null == imageFile) {
                            continue;
                        }
                        boolean deleted = imageFile.delete();
                        if (param._isOutputDebug) {
                            log.info("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
                        }
                        if (deleted) {
                            count += 1;
                        }
                    }
                    log.fatal("グラフ画像ファイル削除:" + count + "件");
                }
            }
        }
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String tostr(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishagonyu(final BigDecimal bd, final int scale) {
        return null == bd ? null : bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final Param param, final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        final List students = Student.createStudentList(db2, param);

        // 成績のデータを読む
        param._thisExam.load2(db2, students, param);

        // 印刷
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            printStudent(db2, param, svf, student);

            _hasData = true;
        }
    }

    private void printStudent(final DB2UDB db2, final Param param, final Vrw32alp svf, final Student student) {

        log.info(" student " + student._schregno + " , subclass count = " + getMappedMap(student._examSubclasses, param._thisExam.getExamKey()).size() + ", course key = " + student.courseKey());
//      log.info("record this: " + getMappedMap(student._examRecord, param._thisExam.getExamKey()).values());
//      log.info("record bef: " + getMappedMap(student._examRecord, param._beforeExam.getExamKey()).values());

        svf.VrSetForm(FORM, 4);

        svf.VrsOut("NENDO", (param._isSeireki ? param._year : KNJ_EditDate.gengou(db2, Integer.parseInt(param._year))) + "年度");
        svf.VrsOut("TITLE", tostr(param._thisExam._examName) + "　個人成績表");
        svf.VrsOut("HR_NAME", tostr(student._hrName) + String.valueOf(Integer.parseInt(student._attendNo)) + "番");
        svf.VrsOut(KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "NAME_2" : "NAME", student._name);

        final String msg = param.getItemMsg();

        svf.VrsOut("ITEM_AVG", msg + "平均");
        svf.VrsOut("ITEM_RANK", msg + "順位");
        svf.VrsOut("ITEM_DEVIATION", "偏差値");

        // 画像
        if (null != param._radarLegendImage) {
            svf.VrsOut("RADER_LEGEND", param._radarLegendImage);
        }
//        if (null != param._barLegendImage) {
//            svf.VrsOut("BAR_LEGEND", param._barLegendImage);
//        }

        svf.VrsOut("DIST_TITLE", "度数分布表");
        svf.VrsOut("RADER_TITLE", "教科間バランス");
        svf.VrsOut("BAR_TITLE", "偏差値推移表");

        svf.VrsOut("ITEM_TOTAL5", "合計"); // 項目（5教科）

//        final File barChartFile = Chart.getBarChartFile(param, student, param._thisExam);
//        if (null != barChartFile) {
//            svf.VrsOut("BAR", barChartFile.toString());
//        }

        final File radarChartFile = Chart.getRadarChartFile(param, student, param._thisExam);
        if (null != radarChartFile) {
            svf.VrsOut("RADER", radarChartFile.toString());
        }

        final File lineChartFile = Chart.getLineChartFile(param, student, param._thisExam);
        if (null != lineChartFile) {
            svf.VrsOut("LINE", lineChartFile.toString());
        }

        printScoreDist(svf, param, student, param._thisExam);

        // 成績
        printRecord(svf, param, student, param._thisExam);
    }

    private void printScoreDist(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {
        final List keyList = ScoreDistribution.getKeyList();
        for (int i = 0; i < keyList.size(); i++) {
            final Integer key = (Integer) keyList.get(i);
            svf.VrsOutn("DIST_COUNT", i + 1, tostr(key)); //
        }

        final String avgMark = "☆";
        final String scoreMark = "◇";

        svf.VrsOut("LEGEND1", avgMark + " 平均点");

        svf.VrsOut("LEGEND2", scoreMark + " 得点");

        final List subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
        int line = 0;
        for (int j = 0; j < Math.min(subclassList.size(), TABLE_SUBCLASS_MAX); j++) {
            final Subclass subclass = (Subclass) subclassList.get(j);

            if ("90".equals(subclass.getClassCd(param))) {
                continue;
            }
            final Class clazz = (Class) param._classes.get(subclass.getClassKey(param));
            final Record record = (Record) getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
            if (null == clazz) {
                continue;
            }
            if (null == record || null == record._score) {
                continue;
            }

            final Map keyScoreListMap = getMappedMap(exam._subclasscdScoreDistributionMap, subclass._code);
            if (!keyScoreListMap.isEmpty()) {
                line += 1;
                svf.VrsOutn("CLASS2", line, subclass._abbv);

                final AverageDat avgDat = (AverageDat) exam._averageDatMap.get(AverageDat.averageKey(param, student, record._subclasscd));
                Integer average = null;
                if (null != avgDat && null != avgDat._avg) {
                    average = new Integer(avgDat._avg.setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                }

                for (int si = 0; si < keyList.size(); si++) {
                    final Integer key = (Integer) keyList.get(si);
                    final String ssi = String.valueOf(si + 1);
                    final List scoreList = getMappedList(keyScoreListMap, key);
                    final String count = scoreList.size() == 0 ? "" : String.valueOf(scoreList.size());

                    String mark = "";
                    if (ScoreDistribution.isScoreInRange(average, key)) {
                        mark += avgMark;
                    }
                    if (ScoreDistribution.isScoreInRange(record._score, key)) {
                        mark += scoreMark;
                    }
                    mark += StringUtils.replace(StringUtils.repeat(" ", 6 - mark.length() * 2 - count.length()), "  ", "　");

                    svf.VrsOutn("COUNT" + ssi, line, mark + count); //
                }
            }
        }
    }

    private void printRecord(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {

        final Record rec9 = (Record) getMappedMap(student._examRecord, exam.getExamKey()).get(ALL9);
        if (null != rec9) {
            svf.VrsOut("SCORE5", tostr(rec9._score));
            svf.VrsOut("RANK5", tostr(rec9._rank));
            svf.VrsOut("DEVIATION5", sishagonyu(rec9._deviation, 1));
        }
        final AverageDat avg9 = (AverageDat) exam._averageDatMap.get(AverageDat.averageKey(param, student, ALL9));
        if (null != avg9) {
            svf.VrsOut("AVERAGE5", sishagonyu(avg9._avg, 1));
            svf.VrsOut("MAX_SCORE5", tostr(avg9._highScore));
            svf.VrsOut("EXAMINEE5", tostr(avg9._count));
        }

        final List subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
        for (int i = 0; i < Math.min(subclassList.size(), TABLE_SUBCLASS_MAX); i++) {
            final Subclass subclass = (Subclass) subclassList.get(i);

            if ("90".equals(subclass.getClassCd(param))) {
                continue;
            }

//            if (exam.getAttendSubclassCdList(student).contains(subclass._code)) {
//                continue;
//            }

            final Record record = (Record) getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
            final AverageDat avgDat = (AverageDat) exam._averageDatMap.get(AverageDat.averageKey(param, student, subclass._code));
            final Class clazz = (Class) param._classes.get(subclass.getClassKey(param));
            if (null == clazz) {
                continue;
            }
            if (null == record || null == record._score) {
                continue;
            }
            if (param._isOutputDebug) {
                log.info(" subclass " + subclass._code + ":" + subclass._name);
            }

//            if (_fiveClassCd.contains(clazz._code)) {
//                amikake(getFClass(), clazz._name);
//            } else {
//                _svf.VrsOut(getFClass(), clazz._name);
//            }
            svf.VrsOut("CLASS_2", clazz._abbv); // 教科
            svf.VrsOut("CLASS", clazz._abbv); // 教科
            if (KNJ_EditEdit.getMS932ByteLength(clazz._abbv) > 6) {
                svf.VrAttribute("CLASS", "X=10000"); // 教科
            } else {
                svf.VrAttribute("CLASS_2", "X=10000"); // 教科
            }
            svfVrsOutWithCheckMojisu(svf, param, new String[] {"SUBCLASS", "SUBCLASS2", "SUBCLASS3_1"}, subclass._name);

            final String score = tostr(record._score);
            svf.VrsOut("SCORE", param.isUnderScore(score) ? "(" + score + ")" : score);

            svf.VrsOut("RANK", tostr(record._rank));
            svf.VrsOut("DEVIATION", sishagonyu(record._deviation, 1));

            if (null != avgDat) {
                svf.VrsOut("AVERAGE", sishagonyu(avgDat._avg, 1));
                svf.VrsOut("MAX_SCORE", tostr(avgDat._highScore));
                svf.VrsOut("EXAMINEE", tostr(avgDat._count));    // 受験者数
            }
            svf.VrEndRecord();
        }
        if (subclassList.size() == 0) {
            svf.VrsOut("CLASS", "\n");
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
        log.info(" student done.");
    }

    private void svfVrsOutWithCheckMojisu(final Vrw32alp svf, final Param param, final String[] fields, final String data) {
        if (null == data) {
            return;
        }
        if (null == fields || fields.length == 0) {
            throw new IllegalArgumentException("フィールド名指定不正");
        }
        final int dataKeta = data.length();
        String lastField = null;
        String firstValidField = null;
        for (int i = 0; i < fields.length; i++) {
            final int fieldKeta = getFieldKeta(svf, param, fields[i], 0) / 2; // 縦書きなので / 2
            if (fieldKeta > 0) {
                if (null == firstValidField && dataKeta <= fieldKeta) {
                    firstValidField = fields[i];
                }
                lastField = fields[i];
            }
        }
        if (null != firstValidField) {
            svf.VrsOut(firstValidField, data);
        } else if (null != lastField) {
            svf.VrsOut(lastField, data);
        } else {
            log.warn("no such field:" + ArrayUtils.toString(fields));
        }
    }

    public static int getFieldKeta(final Vrw32alp svf, final Param param, final String fieldname, final int defaultKeta) {
        try {
            if (null == param._formfieldInfoMap.get(FORM)) {
                param._formfieldInfoMap.put(FORM, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
            }
            SvfField field = (SvfField) ((Map) param._formfieldInfoMap.get(FORM)).get(fieldname);
            if (null != field) {
//                log.info(" field " + fieldname + " = " + field._fieldLength);
                return field._fieldLength;
            }
        } catch (Throwable t) {
            log.warn(t);
        }
        return defaultKeta;
    }

    private static class Chart {

        private static File getLineChartFile(final Param param, final Student student, final Exam exam) {

            // グラフ用のデータ作成
            final DefaultCategoryDataset deviationDataset = new DefaultCategoryDataset();
            int i = 0;

            for (final Iterator it = param._testitemList.iterator(); it.hasNext();) {
                final Map testMap = (Map) it.next();
                final String testcd = KnjDbUtils.getString(testMap, "TESTCD");
//                final String testitemname = tostr(KnjDbUtils.getString(testMap, "SEMESTERNAME")) + "" + tostr(KnjDbUtils.getString(testMap, "TESTITEMNAME"));
                final String testitemname = tostr(KnjDbUtils.getString(testMap, "TESTITEMNAME"));
                BigDecimal deviation = null;
                if (!"1".equals(KnjDbUtils.getString(testMap, "HON"))) {
                    deviation = (BigDecimal) student._testcdDeviationMap.get(testcd);
                }
                deviationDataset.addValue(deviation, "本人偏差値", testitemname);
                if (param._isOutputDebug) {
                    log.info(" deviation " + deviation + " at " + testcd + ":" + testitemname);
                }
                i += 1;
            }

            log.info("折れ線グラフ考査数: " + i);

            try {
                // チャート作成
                final JFreeChart chart = Chart.createLineChart(deviationDataset);

                // グラフのファイルを生成
                final File outputFile = Chart.graphImageFile(chart, 3224 - 1652, 4324 - 3412);
                param._graphFiles.add(outputFile);

                if (outputFile.exists()) {
                    return outputFile;
                }
            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
            return null;
        }

        private static JFreeChart createLineChart(
                final DefaultCategoryDataset deviationDataset
        ) {
            final JFreeChart chart = ChartFactory.createLineChart(null, null, null, deviationDataset, PlotOrientation.VERTICAL, false, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            final CategoryAxis categoryAxis = plot.getDomainAxis();
            categoryAxis.setTickLabelsVisible(true);
            categoryAxis.setTickLabelFont(categoryAxis.getTickLabelFont().deriveFont(4));
//            categoryAxis.setTickLabelFont(new Font("TimesRoman", Font.PLAIN, 8));
//            log.info(" ratio = " + categoryAxis.getMaximumCategoryLabelWidthRatio());
//            categoryAxis.setMaximumCategoryLabelWidthRatio(0.5f);
            plot.setRangeGridlinePaint(Color.gray);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            plot.setBackgroundPaint(Color.lightGray.brighter());

            final NumberAxis numberAxis = new NumberAxis();
            numberAxis.setTickUnit(new NumberTickUnit(10));
            numberAxis.setTickLabelsVisible(true);
            numberAxis.setRange(0, 100.0);
            plot.setRangeAxis(numberAxis);

            final CategoryItemRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, Color.black);
//            final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 20);
//            renderer.setItemLabelFont(itemLabelFont);
//            renderer.setItemLabelsVisible(true);
            renderer.setSeriesStroke(0, new BasicStroke(1.5f));
            plot.setRenderer(renderer);

            chart.setBackgroundPaint(Color.white);

            return chart;
        }

//        private static File getBarChartFile(final Param param, final Student student, final Exam exam) {
//
//            /** 棒グラフの最大科目数. */
//            final int BAR_GRAPH_MAX_ITEM = 11;
//            // グラフ用のデータ作成
//            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
//            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
//            int i = 0;
//            for (final Iterator it = getMappedMap(student._examRecord, exam.getExamKey()).values().iterator(); it.hasNext();) {
//                final Record record = (Record) it.next();
//                final Subclass subclass = (Subclass) param._subclasses.get(record._subclasscd);
//                if (null == subclass) {
//                    continue;
//                }
//
////                if (subclass._isMoto) {
////                    continue;
////                }
//
//                if ("90".equals(record._classcd)) {
//                    continue;
//                }
//
////                if (exam.getAttendSubclassCdList(student).contains(record._subclasscd)) {
////                    continue;
////                }
//
//                final String subclassKey = null != subclass._abbv && subclass._abbv.length() > 4 ? subclass._abbv.substring(0, 4) : subclass._abbv;
//                scoreDataset.addValue(record._scorePercent, "本人得点", subclassKey);
//                final AverageDat avgDat = (AverageDat) exam._averageDatMap.get(AverageDat.averageKey(param, student, record._subclasscd));
//                final BigDecimal avgPercent = (null == avgDat) ? null : avgDat._avgPercent;
//
//                avgDataset.addValue(avgPercent, param.getItemMsg() + "平均点", subclassKey);
//
//                //log.info("棒グラフ⇒" + record._subclasscd + ":素点=" + record._score + ", 平均=" + avgPercent);
//                if (i++ > BAR_GRAPH_MAX_ITEM) {
//                    break;
//                }
//            }
//            log.info("棒グラフ科目数: " + i);
//
//            try {
//                // チャート作成
//                final JFreeChart chart = Chart.createBarChart(scoreDataset, avgDataset);
//
//                // グラフのファイルを生成
//                final File outputFile = Chart.graphImageFile(chart, 1940, 930);
//                param._graphFiles.add(outputFile);
//
//                if (outputFile.exists()) {
//                    return outputFile;
//                }
//            } catch (Throwable e) {
//                log.error("exception or error!", e);
//            }
//            return null;
//        }

//        private static JFreeChart createBarChart(
//                final DefaultCategoryDataset scoreDataset,
//                final DefaultCategoryDataset avgDataset
//        ) {
//            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
//            final CategoryPlot plot = chart.getCategoryPlot();
//            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
//            plot.getDomainAxis().setTickLabelsVisible(true);
//            plot.setRangeGridlinePaint(Color.black);
//            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
//
//            // 追加する折れ線グラフの表示設定
//            final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
//            renderer2.setItemLabelsVisible(true);
//            renderer2.setPaint(Color.gray);
//            plot.setDataset(1, avgDataset);
//            plot.setRenderer(1, renderer2);
//            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
//
//            final NumberAxis numberAxis = new NumberAxis();
//            numberAxis.setTickUnit(new NumberTickUnit(10));
//            numberAxis.setTickLabelsVisible(true);
//            numberAxis.setRange(0, 100.0);
//            plot.setRangeAxis(numberAxis);
//
//            final CategoryItemRenderer renderer = plot.getRenderer();
//            renderer.setSeriesPaint(0, Color.darkGray);
//            final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
//            renderer.setItemLabelFont(itemLabelFont);
//            renderer.setItemLabelsVisible(true);
//
//            ((BarRenderer) renderer).setMaximumBarWidth(0.05);
//
//            chart.setBackgroundPaint(Color.white);
//
//            return chart;
//        }

        private static File getRadarChartFile(final Param param, final Student student, final Exam exam) {

//          final List attendSubclassCdList = exam.getAttendSubclassCdList(student);

            // データ作成
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final Iterator it0 = param._classOrder.iterator(); it0.hasNext();) {
                final String classCd = (String) it0.next();

                for (final Iterator it = getMappedMap(student._examRecord, exam.getExamKey()).values().iterator(); it.hasNext();) {
                    final Record record = (Record) it.next();
                    if (!classCd.equals(record._classkey)) {
                        continue;
                    }
//                    if (attendSubclassCdList.contains(record._subclasscd)) {
//                        continue;
//                    }
//                    log.info("レーダーグラフ⇒" + record._subclasscd + ", " + record._deviation);
//                    final Class clazz = (Class) param._classes.get(record._classkey);
//                    final String name = (null == clazz) ? "" : clazz._abbv;
                    final Subclass subclass = (Subclass) param._subclasses.get(record._subclasscd);
                    final String name = (null == subclass) ? "" : subclass._abbv;
                    if (null == record._deviation) {
                        continue;
                    }
                    dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20

                    dataset.addValue(50.0, "偏差値50", name);
                }
            }

            try {
                // チャート作成
                final JFreeChart chart = Chart.createRaderChart(dataset);

                // グラフのファイルを生成
                final File outputFile = Chart.graphImageFile(chart, 1492 - 276, 4336 - 3412);
                param._graphFiles.add(outputFile);

                if (0 < dataset.getColumnCount() && outputFile.exists()) {
                    return outputFile;
                }

            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
            return null;
        }


        private static JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
            final SpiderWebPlot plot = new SpiderWebPlot(dataset);
            plot.setWebFilled(false);
            plot.setMaxValue(80.0);

            // 実データ
            plot.setSeriesOutlineStroke(0, new BasicStroke(1.5f));
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

        private static File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
            final String tmpFileName = KNJServletUtils.createTmpFile(".png");
            //log.info("\ttmp file name=" + tmpFileName);

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

    private static class Exam {

        final String _year;
        final String _semester;
        final String _testCd;
        private String _grade;
        private String _examName = "";

        /** 成績平均データ。 */
        private Map _averageDatMap = new HashMap();
//        /** 合併元科目コード */
//        private Map _attendSubclassCdMap = Collections.EMPTY_MAP;
        private Map _subclasscdScoreDistributionMap = new HashMap();

        public Exam(final String year, final String semester, final String testCd) {
            _year = year;
            _semester = semester;
            _testCd = testCd;
        }

        public void load2(final DB2UDB db2, final List students, final Param param) {
            log.error("成績関連の読込み " + _testCd);
            Student.getSubClassMap(db2, this, students, param);
            _averageDatMap = AverageDat.getAverageDatMap(db2, this, param);
            Student.getRecordScoreMap(db2, this, students, param);
            _subclasscdScoreDistributionMap = ScoreDistribution.getSubclassScoreDistributionMap(db2, this, param);
        }

        public String getExamKey() {
            return _year + "-" + _semester + "-" + _testCd;
        }

        public String getKindCd() {
            return null == _testCd ? null : _testCd.substring(0, 2);
        }

        public String getItemCd() {
            return null == _testCd ? null : _testCd.substring(2, 4);
        }

        public String getScoreDiv() {
            return null == _testCd ? null : _testCd.substring(4);
        }

        private void load(final Param param, final DB2UDB db2) {
            if (null != _year) {
                final StringBuffer sql = new StringBuffer();
                sql.append("SELECT testitemname FROM testitem_mst_countflg_new_sdiv ");
                sql.append(" WHERE year = '" + _year + "' ");
                sql.append("   AND semester = '"  + _semester + "'");
                sql.append("   AND testkindcd = '" + getKindCd() + "' ");
                sql.append("   AND testitemcd = '" + getItemCd() + "'");
                sql.append("   AND score_div = '" + getScoreDiv() + "'");

                _examName = tostr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString())));
            }
//            loadAttendSubclassCdMap(db2, param);
        }

//        private void loadAttendSubclassCdMap(final DB2UDB db2, final Param param) {
//            final Map map = new HashMap();
//            try {
//                final StringBuffer stb = new StringBuffer();
////                final String flg = "9900".equals(_testCd) ? "2" : "1";
////                stb.append("   SELECT ");
////                stb.append("       T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ");
////                if ("1".equals(param._useCurriculumcd)) {
////                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
////                }
////                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
////                stb.append("   FROM ");
////                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
////                stb.append("   WHERE ");
////                stb.append("       T1.YEAR = '" + _year + "' ");
////                stb.append("       AND T1.FLG = '" + flg + "' ");
////
////                ps = db2.prepareStatement(stb.toString());
////                rs = ps.executeQuery();
////                while (rs.next()) {
////                    getMappedList(map, rs.getString("KEY")).add(rs.getString("ATTEND_SUBCLASSCD"));
////                }
//
//                stb.append("   SELECT ");
//                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
//                stb.append("   FROM ");
//                stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
//                stb.append("   WHERE ");
//                stb.append("       T1.YEAR = '" + _year + "' ");
//
//                for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
//                    final Map row = (Map) it.next();
//                    getMappedList(map, "0").add(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
//                }
//            } catch (Exception e) {
//                log.error("exception!", e);
//            }
//            _attendSubclassCdMap = map;
//        }

//        public List getAttendSubclassCdList(final Student student) {
////            return getMappedList(_attendSubclassCdMap, student.attendSubclassCdKey());
//            return getMappedList(_attendSubclassCdMap, "0");
//        }

    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;

        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _coursegroupCd;

        final String _name;

        /** 成績科目 */
        private final Map _examSubclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _examRecord = new TreeMap();

        final Map _testcdDeviationMap = new HashMap();

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
                final String name
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

        private static List createStudentList(final DB2UDB db2, final Param param) throws SQLException {
            final List studentList = new LinkedList();

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  regd.schregno,");
            sql.append("  regd.grade,");
            sql.append("  regd.hr_class,");
            sql.append("  regd.attendno,");
            sql.append("  hdat.hr_name,");
            sql.append("  regd.coursecd,");
            sql.append("  regd.majorcd,");
            sql.append("  regd.coursecode,");
            sql.append("  t4.group_cd as coursegroupCd,");
            sql.append("  base.name ");
            sql.append(" FROM");
            sql.append("  schreg_regd_dat regd ");
            sql.append("    INNER JOIN schreg_base_mst base ON base.schregno = regd.schregno");
            sql.append("    INNER JOIN schreg_regd_hdat hdat ON hdat.year = regd.year AND");
            sql.append("      hdat.semester = regd.semester AND");
            sql.append("      hdat.grade = regd.grade AND");
            sql.append("      hdat.hr_class = regd.hr_class ");
            sql.append("    LEFT JOIN course_group_cd_dat t4 ON regd.year = t4.year AND");
            sql.append("      regd.grade = t4.grade AND");
            sql.append("      regd.coursecd = t4.coursecd AND");
            sql.append("      regd.majorcd = t4.majorcd AND");
            sql.append("      regd.coursecode = t4.coursecode");
            sql.append(" WHERE");
            sql.append("  regd.year = '" + param._year + "' AND");
            sql.append("  regd.semester = '" + param._semester + "' AND");
//            if ("1".equals(param._categoryIsClass)) {
//                sql.append("  regd.grade || '-' || regd.hr_class IN " + SQLUtils.whereIn(true, param._categorySelected));
//            } else {
                sql.append("  regd.schregno IN " + SQLUtils.whereIn(true, param._categorySelected));
//            }
            sql.append(" ORDER BY regd.grade, regd.hr_class, regd.attendno ");

            //log.info(" regd sql = " + sql);

            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String schregno = KnjDbUtils.getString(row, "schregno");
                final String grade = KnjDbUtils.getString(row, "grade");
                final String hrclass = KnjDbUtils.getString(row, "hr_class");
                final String attendno = KnjDbUtils.getString(row, "attendno");
                final String hrName = KnjDbUtils.getString(row, "hr_name");
                final String coursecd = KnjDbUtils.getString(row, "coursecd");
                final String majorcd = KnjDbUtils.getString(row, "majorcd");
                final String coursecode = KnjDbUtils.getString(row, "coursecode");
                final String coursegroupCd = KnjDbUtils.getString(row, "coursegroupCd");
                final String name = KnjDbUtils.getString(row, "name");

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
                        name
                );
                studentList.add(student);
            }
            return studentList;
        }

        private static void getSubClassMap(final DB2UDB db2, final Exam exam, final List students, final Param param) {
            PreparedStatement ps = null;

            try {
                final StringBuffer sql = new StringBuffer();
                sql.append("select");
                sql.append("  distinct T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD");
                sql.append(" from");
                sql.append("  RECORD_SCORE_DAT T1 ");
                sql.append(" where");
                sql.append("  T1.YEAR = '" + exam._year + "' and");
                sql.append("  T1.SEMESTER = '" + exam._semester + "' and");
                sql.append("  T1.TESTKINDCD = '" + exam.getKindCd() + "' and");
                sql.append("  T1.TESTITEMCD = '" + exam.getItemCd() + "' and");
                sql.append("  T1.SCORE_DIV = '" + exam.getScoreDiv() + "' and");
                sql.append("  T1.SCHREGNO = ? ");
                //log.info("成績入力データの科目のSQL=" + sql);

                ps = db2.prepareStatement(sql.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, sql.toString(), new String[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        final Subclass subClass = (Subclass) param._subclasses.get(subclasscd);
                        if (null != subClass) {
                            getMappedMap(student._examSubclasses, exam.getExamKey()).put(subclasscd, subClass);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 生徒に成績データを関連付ける。
         * @param db2 DB
         * @param kindCd テスト種別コード
         * @param itemCd テスト項目コード
         * @param scoreDistributions 得点分布の格納場所
         * @param students 生徒たち
         */
        private static void getRecordScoreMap(
                final DB2UDB db2,
                final Exam exam,
                final List students,
                final Param param
        ) {
            final String gradeRank  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
            final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
            final String majorRank = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";

            final StringBuffer sql = new StringBuffer();
            /* 通常の成績 */
            sql.append("SELECT");
            sql.append("  t1.classcd,");
            sql.append("  t1.classcd || '-' || t1.school_kind as class_key,");
            sql.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as subclasscd,");
            sql.append("  t2.score,");
            sql.append("  t2." + gradeRank + " as grade_rank,");
            sql.append("  t2.grade_deviation,");
            sql.append("  t2." + courseRank + " as course_rank,");
            sql.append("  t2.course_deviation,");
            sql.append("  t2." + majorRank + " as major_rank,");
            sql.append("  t2.major_deviation,");
            sql.append("  t3.div,");
            sql.append("  value(t3.perfect, 100) as perfect");
            sql.append(" FROM");
            sql.append("  record_score_dat t1 ");
            sql.append(" LEFT JOIN record_rank_sdiv_dat t2 ON t1.year = t2.year AND");
            sql.append("    t1.semester = t2.semester AND");
            sql.append("    t1.testkindcd = t2.testkindcd AND");
            sql.append("    t1.testitemcd = t2.testitemcd AND");
            sql.append("    t1.score_div = t2.score_div AND");
            sql.append("    t1.classcd = t2.classcd AND");
            sql.append("    t1.school_kind = t2.school_kind AND");
            sql.append("    t1.curriculum_cd = t2.curriculum_cd AND");
            sql.append("    t1.subclasscd = t2.subclasscd AND");
            sql.append("    t1.schregno = t2.schregno ");
            sql.append(" LEFT JOIN perfect_record_sdiv_dat t3 ON t1.year = t3.year AND");
            sql.append("    t1.semester = t3.semester AND");
            sql.append("    t1.testkindcd = t3.testkindcd AND");
            sql.append("    t1.testitemcd = t3.testitemcd AND");
            sql.append("    t1.score_div = t3.score_div AND");
            sql.append("    t1.classcd = t3.classcd AND");
            sql.append("    t1.school_kind = t3.school_kind AND");
            sql.append("    t1.curriculum_cd = t3.curriculum_cd AND");
            sql.append("    t1.subclasscd = t3.subclasscd AND");
            sql.append("    t3.grade = (case when t3.div = '01' then '00' else ? end) AND ");
            sql.append("    t3.coursecd || t3.majorcd || t3.coursecode = ");
            sql.append("        (case when t3.div in ('01', '02') then '00000000' ");
            sql.append("              when t3.div = '04' then '0' || ? || '0000' ");
            sql.append("              else ? end) ");
            sql.append(" WHERE");
            sql.append("  t1.year = '" + exam._year + "' AND");
            sql.append("  t1.semester = '" + exam._semester + "' AND");
            sql.append("  t1.testkindcd = '" + exam.getKindCd() + "' AND");
            sql.append("  t1.testitemcd = '" + exam.getItemCd() + "' AND");
            sql.append("  t1.score_div = '" + exam.getScoreDiv() + "' AND");
            sql.append("  t1.schregno = ?");

            PreparedStatement ps = null;

            try {
                ps = db2.prepareStatement(sql.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final String[] qparam = new String[] {student._grade, student._coursegroupCd, student._courseCd + student._majorCd + student._courseCode, student._schregno};
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, qparam).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final String subclasscd = KnjDbUtils.getString(row, "subclasscd");
                        Subclass subClass = (Subclass) param._subclasses.get(subclasscd);
                        if (null == subClass) {
                            log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                            final String key;
                            final String[] split = StringUtils.split(subclasscd, "-");
                            key = split[0] + "-" + split[1];
                            final Class clazz = (Class) param._classes.get(key);
                            if (null == clazz) {
                                continue;
                            }
                            subClass = new Subclass(subclasscd, clazz._name, clazz._abbv);
                        }

                        final Integer score = KnjDbUtils.getInt(row, "score", null);
                        Integer scorePercent = null;
                        final int perfect = KnjDbUtils.getInt(row, "PERFECT", new Integer(0)).intValue();
                        if (100 == perfect) {
                            scorePercent = score;
                        } else if (null != score) {
                            scorePercent = new Integer(new BigDecimal(KnjDbUtils.getString(row, "SCORE")).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 0, BigDecimal.ROUND_HALF_UP).intValue());
                        }
                        final Integer rank;
                        final BigDecimal deviation;
                        if ("1".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "grade_rank", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "grade_deviation", null);
                        } else if ("3".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "major_rank", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "major_deviation", null);
                        } else {
                            rank = KnjDbUtils.getInt(row, "course_rank", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "course_deviation", null);
                        }

                        final Record rec = new Record(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "CLASS_KEY"), subclasscd, score, scorePercent, null, rank, deviation);
                        getMappedMap(student._examRecord, exam.getExamKey()).put(subclasscd, rec);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT");
            sql1.append("  subclasscd,");
            sql1.append("  score,");
            sql1.append("  avg,");
            sql1.append("  " + gradeRank + " as grade_rank,");
            sql1.append("  grade_deviation,");
            sql1.append("  " + courseRank + " as course_rank,");
            sql1.append("  course_deviation,");
            sql1.append("  " + majorRank + " as major_rank,");
            sql1.append("  major_deviation");
            sql1.append(" FROM record_rank_sdiv_dat");
            sql1.append(" WHERE");
            sql1.append("  year = '" + exam._year + "' AND");
            sql1.append("  semester = '" + exam._semester + "' AND");
            sql1.append("  testkindcd = '" + exam.getKindCd() + "' AND");
            sql1.append("  testitemcd = '" + exam.getItemCd() + "' AND");
            sql1.append("  score_div = '" + exam.getScoreDiv() + "' AND");
            sql1.append("  subclasscd IN ('" + ALL9 + "') AND");
            sql1.append("  schregno= ?");

            try {
                ps = db2.prepareStatement(sql1.toString());

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final String[] qparam = new String[] {student._schregno};
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, qparam).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final String subclasscd = KnjDbUtils.getString(row, "subclasscd");
                        final Integer score = KnjDbUtils.getInt(row, "score", null);
                        final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "avg", null);
                        final Integer rank;
                        final BigDecimal deviation;
                        if ("1".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "grade_rank", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "grade_deviation", null);
                        } else if ("3".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "major_rank", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "major_deviation", null);
                        } else {
                            rank = KnjDbUtils.getInt(row, "course_rank", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "course_deviation", null);
                        }

                        final Record rec = new Record("99", "99", subclasscd, score, null, avg, rank, deviation);
                        getMappedMap(student._examRecord, exam.getExamKey()).put(subclasscd, rec);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }



            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT");
            sql2.append("  semester || testkindcd || testitemcd || score_div as testcd,");
            sql2.append("  grade_deviation ");
            sql2.append(" FROM record_rank_sdiv_dat");
            sql2.append(" WHERE");
            sql2.append("  year = '" + exam._year + "' AND");
            sql2.append("  subclasscd IN ('" + ALL9 + "') AND");
            sql2.append("  schregno= ?");

            try {
                ps = db2.prepareStatement(sql2.toString());

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final String[] qparam = new String[] {student._schregno};
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, qparam).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final String testcd = KnjDbUtils.getString(row, "testcd");
                        final BigDecimal deviation = KnjDbUtils.getBigDecimal(row, "grade_deviation", null);

                        student._testcdDeviationMap.put(testcd, deviation);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
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
    private static class Subclass implements Comparable {
        private final String _code;
        private final String _name;
        private final String _abbv;

//        /** 合併情報を持っているか */
//        private boolean _hasCombined;
//        /** 合併先か? */
//        private boolean _isSaki;
//        /** 合併元か? */
//        private boolean _isMoto;

        public Subclass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public String getClassKey(final Param param) {
            final String classCd;
            final String[] split = StringUtils.split(_code, "-");
            classCd =  split[0] + "-" + split[1];
            return classCd;
        }

        public String getClassCd(final Param param) {
            final String classCd;
            final String[] split = StringUtils.split(_code, "-");
            classCd =  split[0];
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
            if (!(o instanceof Subclass)) {
                return -1;
            }
            final Subclass that = (Subclass) o;
            return this._code.compareTo(that._code);
        }
    }

    private static class Record {
        final String _classcd;
        final String _classkey;
        final String _subclasscd;
        final Integer _score;
        final Integer _scorePercent;
        final BigDecimal _avg;
        final Integer _rank;
        final BigDecimal _deviation;

        private Record(
                final String classcd,
                final String classkey,
                final String subclasscd,
                final Integer score,
                final Integer scorePercent,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation
        ) {
            _classcd = classcd;
            _classkey = classkey;
            _subclasscd = subclasscd;
            _score = score;
            _scorePercent = scorePercent;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
        }

        public String toString() {
            return _subclasscd + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }

    private static class AverageDat {
        final String _subclassCd;
        final BigDecimal _avg;
        final BigDecimal _avgPercent;
        final BigDecimal _stdDev;
        final Integer _highScore;
        final Integer _count;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        private AverageDat(
                final String subclassCd,
                final BigDecimal avg,
                final BigDecimal avgPercent,
                final BigDecimal stdDev,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subclassCd = subclassCd;
            _avg = avg;
            _avgPercent = avgPercent;
            _stdDev = stdDev;
            _highScore = highScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        private static String averageKey(final Param param, final String subclasscd, final String coursecd, final String majorcd, final String coursecode) {
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = subclasscd;
            } else if ("3".equals(param._groupDiv)) {
                key = subclasscd + "0" + majorcd + "0000";
            } else {
                key = subclasscd + coursecd + majorcd + coursecode;
            }
            return key;
        }

        private static String averageKey(final Param param, final Student student, final String subclasscd) {
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = subclasscd;
            } else if ("3".equals(param._groupDiv)) {
                key = subclasscd + student.courseGroupKey();
            } else {
                key = subclasscd + student.courseKey();
            }
            return key;
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
        private static Map getAverageDatMap(
                final DB2UDB db2,
                final Exam exam,
                final Param param
        ) {

            String selectFrom;
            selectFrom = "SELECT";
            selectFrom += "  case when t1.subclasscd in ('" + ALL9 + "') then t1.subclasscd  ";
            selectFrom += "   else t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd ";
            selectFrom += "  end as subclasscd,";
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
                + "  record_average_sdiv_dat t1"
                + "  LEFT JOIN perfect_record_sdiv_dat t2 ON t2.year = t1.year AND "
                + "    t2.semester = t1.semester AND"
                + "    t2.testkindcd = t1.testkindcd AND"
                + "    t2.testitemcd = t1.testitemcd AND"
                + "    t2.score_div = t1.score_div AND";
            selectFrom += ""
                    + "    t2.classcd = t1.classcd AND"
                    + "    t2.school_kind = t1.school_kind AND"
                    + "    t2.curriculum_cd = t1.curriculum_cd AND";
            selectFrom += ""
                + "    t2.subclasscd = t1.subclasscd AND"
                + "    t2.grade = (case when t2.div = '01' then '00' else '" + exam._grade + "' end) AND"
                + "    t2.coursecd || t2.majorcd || t2.coursecode = "
                + "        (case when t2.div in ('01', '02') then '00000000' "
                + "              when t2.div = '04' then '0' || t1.majorcd || '0000' "
                + "              else t1.coursecd || t1.majorcd || t1.coursecode end) "
                ;
            String where;
            where = " WHERE"
                    + "    t1.year = '" + exam._year + "' AND"
                    + "    t1.semester = '" + exam._semester + "' AND"
                    + "    t1.testkindcd = '" + exam.getKindCd() + "' AND"
                    + "    t1.testitemcd = '" + exam.getItemCd() + "' AND"
                    + "    t1.score_div = '" + exam.getScoreDiv() + "' AND";
            if ("1".equals(param._groupDiv)) {
                where += " t1.avg_div = '1' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000' AND"
                    + "    t1.coursecd = '0' AND"
                    + "    t1.majorcd = '000' AND"
                    + "    t1.coursecode = '0000'"
                    ;
            } else if ("3".equals(param._groupDiv)) {
                where += " t1.avg_div = '5' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000'"
                    ;
            } else {
                where += " t1.avg_div = '3' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000'"
                    ;
            }

            final Map averageDatMap = new HashMap();
            final String sql = selectFrom + where;
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String subclasscd = KnjDbUtils.getString(row, "subclasscd");
                final Subclass subclass = (Subclass) param._subclasses.get(subclasscd);
                if (null == subclass) {
                    if (!(ALL9.equals(subclasscd))) {
                        log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
                    }
                }
                final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "avg", null);
                BigDecimal avgPercent = null;
                final int perfect = KnjDbUtils.getInt(row, "PERFECT", new Integer(0)).intValue();
                if (100 == perfect) {
                    avgPercent = avg;
                } else if (null != avg){
                    avgPercent = avg.multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP);
                }
                final BigDecimal stdDev = KnjDbUtils.getBigDecimal(row, "STDDEV", null);
                final Integer count = KnjDbUtils.getInt(row, "count", null);
                final Integer highScore = KnjDbUtils.getInt(row, "highscore", null);
                final String coursecd = KnjDbUtils.getString(row, "coursecd");
                final String majorcd = KnjDbUtils.getString(row, "majorcd");
                final String coursecode = KnjDbUtils.getString(row, "coursecode");

                final AverageDat avgDat = new AverageDat(subclasscd, avg, avgPercent, stdDev, highScore, count, coursecd, majorcd, coursecode);
                averageDatMap.put(AverageDat.averageKey(param, subclasscd, coursecd, majorcd, coursecode), avgDat);
            }
            log.info("テストコード=" + exam.getExamKey() + " の成績平均データの件数=" + averageDatMap.size());
            return averageDatMap;
        }
    }

    private static class ScoreDistribution {

        final static int _kizami = 5;

        public static List getKeyList() {
            final List list = new ArrayList();
            for (int score = 100; score >= 0; score -= _kizami) {
                final Integer s = new Integer(score);
                list.add(s);
            }
            return list;
        }

        public static boolean isScoreInRange(final Integer score, final Integer lower) {
            if (null == score) {
                return false;
            }
            return lower.intValue() <= score.intValue() && score.intValue() < lower.intValue() + _kizami;
        }

        public static Map getHistgramMap(final List scoreList0) {
            final List scoreList = new ArrayList(scoreList0);
            final Map histgramMap = new TreeMap();
            for (final Iterator it = getKeyList().iterator(); it.hasNext();) {
                final Integer lower = (Integer) it.next();
                for (final Iterator sit = scoreList.iterator(); sit.hasNext();) {
                    final Integer score = (Integer) sit.next();
                    if (isScoreInRange(score, lower)) {
                        getMappedList(histgramMap, lower).add(score);
                        sit.remove();
                    }
                }
                if (scoreList.isEmpty()) {
                    break;
                }
            }
            return histgramMap;
        }

        public static Map getSubclassScoreDistributionMap(final DB2UDB db2, final Exam exam, final Param param) {

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as subclasscd,");
            sql.append("  t1.score ");
            sql.append(" FROM");
            sql.append("  record_score_dat t1 ");
            sql.append(" INNER JOIN schreg_regd_dat regd ON t1.schregno = regd.schregno ");
            sql.append("    AND t1.year = regd.year ");
            sql.append("    AND t1.semester = regd.semester ");
            sql.append("    AND regd.grade = '" + param._grade + "' ");
            sql.append(" WHERE");
            sql.append("  t1.year = '" + exam._year + "' AND");
            sql.append("  t1.semester = '" + exam._semester + "' AND");
            sql.append("  t1.testkindcd = '" + exam.getKindCd() + "' AND");
            sql.append("  t1.testitemcd = '" + exam.getItemCd() + "' AND");
            sql.append("  t1.score_div = '" + exam.getScoreDiv() + "' AND ");
            sql.append("  t1.score IS NOT NULL ");

            //log.info(" dist sql = " + sql.toString());

            final Map subclassScoreListMap = new HashMap();
            final Map subclasscdScoreDistributionMap = new HashMap();
            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                getMappedList(subclassScoreListMap, KnjDbUtils.getString(row, "SUBCLASSCD")).add(KnjDbUtils.getInt(row, "SCORE", null));
            }
            for (final Iterator it = subclassScoreListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclasscd = (String) e.getKey();
                final List scoreList = (List) e.getValue();
                subclasscdScoreDistributionMap.put(subclasscd, getHistgramMap(scoreList));
            }

            return subclasscdScoreDistributionMap;
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _testcd;
        final String _grade;
        final String _groupDiv; // 1:学年 3:コース

//        final String _categoryIsClass; // [クラス指定 or 生徒指定]の値。
        final String[] _categorySelected; // クラス or 生徒。

        final String _loginDate;

//        private String _schoolName; // 学校名
//        private String _remark2;    // 担任職種名
        final String _outputKijun; // 基準点
        final String _cmd;
        final Map _formfieldInfoMap = new HashMap();

//        final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        final Map _classes;
        final String _imagePath;
        final boolean _isSeireki;

        final String _useCurriculumcd;
        final String _useClassDetailDat;

        final List _classOrder;

        /** 中学か? false なら高校. */
        final boolean _isJunior;
        final String _schoolKind;
        List _testitemList;

//        /** 欠点 */
//        final int _borderScore;
        /** 科目マスタ。 */
        final Map _subclasses;

        final Exam _thisExam;

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set _graphFiles = new HashSet();

//        final String _barLegendImage;
        final String _radarLegendImage;
        final boolean _isOutputDebug;

        public Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _testcd = request.getParameter("TESTCD");
//            _categoryIsClass = "2"; // request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = tostr(request.getParameter("HR_CLASS")).substring(0, 2);
            _groupDiv =  request.getParameter("GROUP_DIV");// 1=学年, 2=コース
            _cmd = request.getParameter("cmd");

            _imagePath = request.getParameter("IMAGE_PATH");
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00'")));
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");

//            _borderScore = StringUtils.isBlank(request.getParameter("KETTEN")) ? 0 : Integer.parseInt(request.getParameter("KETTEN"));

            _thisExam = new Exam(_year, _semester, _testcd);
            _thisExam._grade = _grade;

            _schoolKind = getSchoolKind(db2);
            _isJunior = "J".equals(_schoolKind);
            _testitemList = getTestItemList(db2, _schoolKind);
            if (_testitemList.isEmpty()) {
                _testitemList = getTestItemList(db2, "00");
            }

//            getCertifSchool(db2);
//            loadRegdHdat(db2);
            _thisExam.load(this, db2);
            log.info(" this Exam testcd = " + _thisExam.getExamKey());
            _classes = setClasses(db2);
            _subclasses = setSubClasses(db2);
//            setCombinedOnSubClass(db2);

            _classOrder = getClassOrder(db2);
            log.info(" class order = " + _classOrder);

//            String barLegendImage;
//            if ("1".equals(_groupDiv)) {
//                /** 棒グラフ凡例画像1(学年). */
//                barLegendImage = "BarChartLegendGrade.png";
//            } else if ("3".equals(_groupDiv)) {
//                /** 棒グラフ凡例画像3(コースグループ). */
//                barLegendImage = "BarChartLegendCourseGroup.png";
//            } else {
//                /** 棒グラフ凡例画像2(コース). */
//                barLegendImage = "BarChartLegendCourse.png";
//            }
//            _barLegendImage = checkImageFile(barLegendImage);
            /** レーダーチャート凡例画像. */
            _radarLegendImage = checkImageFile("RaderChartLegend.jpg");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD105U' AND NAME = '" + propName + "' "));
        }

        public String checkImageFile(String image) {
            image = _imagePath + "/" + image;
            final boolean exists = new File(image).exists();
            log.fatal("画像ファイル exists?" + exists + " " + image);
            if (!exists) {
                image = null;
            }
            return image;
        }

        private String getItemMsg() {
            final String msg;
            if ("1".equals(_groupDiv)) {
                msg = "学年";
            } else if ("3".equals(_groupDiv)) {
                msg = "グループ";
            } else {
                msg = "コース";
            }
            return msg;
        }

//        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
//            final Set sakiSet = new HashSet();
//            final Set motoSet = new HashSet();
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                String sql;
//                sql = "select distinct";
//                sql +="  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ";
//                sql +="  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,";
//                sql +="  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ";
//                sql +="  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD"
//                    + " from SUBCLASS_REPLACE_COMBINED_DAT"
//                    + " where"
//                    + "  YEAR = '" + _year + "'"
//                    ;
//
//                ps = db2.prepareStatement(sql);
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
//                final Subclass subClass = (Subclass) _subclasses.get(saki);
//                if (null != subClass) {
//                    subClass.setSaki();
//                }
//            }
//            // 合併元
//            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
//                final String moto = (String) it.next();
//                final Subclass subClass = (Subclass) _subclasses.get(moto);
//                if (null != subClass) {
//                    subClass.setMoto();
//                }
//            }
//        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            String sql = "select";
            sql +="   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD,";
            sql +="   coalesce(SUBCLASSORDERNAME2, SUBCLASSNAME) as NAME,"
                + "   SUBCLASSABBV"
                + " from V_SUBCLASS_MST"
                + " where"
                + "   YEAR = '" + _year + "'";

            final Map rtn = new HashMap();
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String code = KnjDbUtils.getString(row, "SUBCLASSCD");
                rtn.put(code, new Subclass(code, KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "SUBCLASSABBV")));
            }
            //log.info("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            String sql = "";
            sql += "select";
            sql +="   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
            sql +="   CLASSNAME,"
                + "   CLASSABBV"
                + " from V_CLASS_MST"
                + " where"
                + "   YEAR = '" + _year + "'";

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String code = KnjDbUtils.getString(row, "CLASSCD");
                rtn.put(code, new Class(code, KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "CLASSABBV")));
            }
            //log.info("教科マスタ=" + rtn);
            return rtn;
        }

//        private void loadRegdHdat(final DB2UDB db2) {
//            final String sql;
//            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
//                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year = t2.year AND t1.tr_cd1 = t2.staffcd"
//                + " WHERE t1.year = '" + _year + "'"
//                + " AND t1.semester = '" + _semester + "'";
//            _staffs.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "code", "staffname"));
//        }

//        private void getCertifSchool(final DB2UDB db2) throws SQLException {
//            final String certifKindcd = _isJunior ? "110" : "109";
//
//            final String sql = "SELECT school_name, remark2 FROM certif_school_dat WHERE year = '" + _year + "' AND certif_kindcd = '" + certifKindcd + "'";
//            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
//            _schoolName = KnjDbUtils.getString(row, "school_name");
//            _remark2 = KnjDbUtils.getString(row, "remark2");
//        }

        private List getClassOrder(final DB2UDB db2) {
            final String sql;
            if ("1".equals(_useClassDetailDat)) {
                sql = "SELECT classcd || '-' || school_kind AS classKey FROM class_detail_dat"
                    + " WHERE year = '" + _year + "' AND class_seq = '004' "
                    + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
                    ;
            } else {
                final String field1 = _isJunior ? "name1" : "name2";
                final String field2 = _isJunior ? "namespare1" : "namespare2";
                sql = "SELECT " + field1 + " AS classKey FROM v_name_mst"
                    + " WHERE year = '" + _year + "' AND namecd1 = 'D009' AND " + field1 + " IS NOT NULL "
                    + " ORDER BY INT(" + field2 + ")"
                    ;
            }

            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "classKey");
        }

        private boolean isUnderScore(final String score) {
            return false; // NumberUtils.isDigits(score) && Integer.parseInt(score) < _borderScore;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        SCHOOL_KIND ");
            stb.append("FROM    SCHREG_REGD_GDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + _year + "' ");
            stb.append(    "AND T1.GRADE = '" + _grade + "' ");

            String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            return schoolKind;
        }

        private List getTestItemList(final DB2UDB db2, final String setSchoolKind) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("     SEME.SEMESTERNAME, ");
            stb.append("     T1.TESTITEMNAME, ");
            stb.append("     CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV > '" + _semester + _testcd + "' THEN 1 END AS HON ");
            stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
            stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("   AND T0.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T0.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("   AND T0.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("   AND T0.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("   AND T0.CLASSCD = '00' ");
            stb.append("   AND T0.SCHOOL_KIND = '" + setSchoolKind +"' ");
            stb.append("   AND T0.CURRICULUM_CD = '00' ");
            stb.append("   AND T0.SUBCLASSCD = '000000' ");
            stb.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ");
            stb.append("    AND SEME.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("    AND T1.SEMESTER <> '9' ");
            stb.append("    AND T1.TESTKINDCD <> '99' ");
            stb.append("    AND T1.SCORE_DIV <> '09' ");
            stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

            return KnjDbUtils.query(db2, stb.toString());
        }
    }

} // KNJD105U

// eof
