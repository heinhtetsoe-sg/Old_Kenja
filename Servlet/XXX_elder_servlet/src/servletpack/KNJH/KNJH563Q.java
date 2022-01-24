// kanji=漢字
/*
 * $Id: 6325d15f75c52108e444cca5f9aabc4ab4359d9c $
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
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ObjectUtils;
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
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: 6325d15f75c52108e444cca5f9aabc4ab4359d9c $
 */
public class KNJH563Q {
    /*pkg*/static final Log log = LogFactory.getLog(KNJH563Q.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 全教科科目コード。 */
    private static final String ALL9 = "999999";

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

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }

        boolean _hasData = false;

        Vrw32alp svf = new Vrw32alp();
        if (svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        svf.VrSetSpoolFileStream(response.getOutputStream());
        response.setContentType("application/pdf");

        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        if (openDb(db2)) {
            return;
        }

        Param param = null;
        try {
            log.error("★マスタ関連の読込み");
            param = new Param(request, db2);

            // 対象の生徒たちを得る
            final List students = Student.createStudents(db2, param);
            _hasData = students.size() > 0;

            // 成績のデータを読む
            log.error("★成績関連の読込み");

            Exam.loadExam(db2, students, param._thisExam, param);

            // 印刷する
            log.error("★印刷");
            final String formname = "KNJH563Q.frm";

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug("☆" + student + ", 科目の数=" + student._data._subclasses.size() + ", コースキー=" + student._regd.courseKey());
                log.debug("今回の成績: " + student._data._record.values());

//                _param.setSubclasses(student);

                svf.VrSetForm(formname, 4);

                printRecord(svf, param, student);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            log.error("★終了処理");

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
                param.removeImageFiles();
            }
            log.info("Done.");
        }
    }

    /**
     * 成績部分の印刷
     * @param param パラメータ
     * @param student 生徒
     */
    public void printRecord(final Vrw32alp svf, final Param param, final Student student) {

        svf.VrsOut("SCHOOL_NAME", param._schoolName);
//      final String staffName = (String) param._staffs.get(student._regd._grade + student._regd._hrClass);
//      svf.VrsOut("STAFFNAME", (param._remark2 == null ? "" : param._remark2) + (staffName == null ? "" : staffName));
        svf.VrsOut("NENDO", param._thisExam._title);
        svf.VrsOut("HR_NAME", student._regd._hrName + student._regd.attendNoStr(param));

        svf.VrsOut(getMS932ByteLength(student._name) > 20 ? "NAME_2" : "NAME", student._name);

        svf.VrsOut("DATE", StringUtils.defaultString(KNJ_EditDate.h_format_JP(param._jisshiDate)) + "実施");

        final ExamData dat = student._data;
        dat._regd = student._regd;
        dat._exam = param._thisExam;
        // 全教科

        final Record rec9 = (Record) dat._recordOther.get(ALL9);
        if (null != rec9) {
            final int line = 1;
            svf.VrsOutn("TOTAL1", line, rec9.getScore()); // 合計点
            svf.VrsOutn("AVERAGE1", line, sishaGonyu(rec9._avg)); // 平均
            if (null != rec9._rank && "1".equals(param._juniPrint)) {
                svf.VrsOutn("RANK1", line, rec9._rank.toString()); // 席次
            }
        }

        final AverageDat avg9 = (AverageDat) dat._exam._averageDatOther.get(AverageDat.avgDatKey(param, dat, ALL9));
        if (null != avg9) {
            final int line = 2;
            svf.VrsOutn("TOTAL1", line, sishaGonyu(avg9._avg)); // 合計点
            svf.VrsOutn("AVERAGE1", line, sishaGonyu(avg9._avgAvg)); // 平均
//            svf.VrsOutn("RANK1", line, null); // 席次
        }

        // グラフ印字(サブフォームよりも先に印字)
        printBarGraph(svf, dat, param);
        printRadarGraph(svf, dat, param);

        printRecord(svf, param, dat);
    }

    private void printRecord(final Vrw32alp svf, final Param param, final ExamData dat) {
        /** 成績表の最大科目数. */
        final int TABLE_SUBCLASS_MAX = 20;

        int i = 0;
        for (final Iterator it = dat._subclasses.values().iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();

//            if ("90".equals(subClass.getClassCd())) {
//                continue;
//            }

//            final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//            final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//            if (!hasSubclass5 && !hasSubclass3) {
//                continue;
//            }
//            if (dat._attendSubclasses.contains(subClass)) {
//                continue; // 元科目は非表示
//            }

            final Record record = (Record) dat._record.get(subClass);

            // 教科
            final Class clazz = (Class) param._classes.get(subClass.getClassCd());
            svf.VrsOut("CLASS", clazz._name);

//            // 科目
//            svf.VrsOut("SUBCLASS", subClass._name);

            if (null != record) {
//                if (record.isUnderScore()) {
//                    svf.VrsOut("SCORE", "(" + record.getScore() + ")");
//                } else {
                    svf.VrsOut("SCORE", record.getScore());
//                }
                if ("1".equals(param._juniPrint)) {
                    svf.VrsOut("RANK",      ObjectUtils.toString(record._rank));
                }
//                if ("1".equals(param._deviationPrint)) {
//                    svf.VrsOut("DEVIATION", ObjectUtils.toString(record._deviation));
//                }

                final AverageDat avgDat = (AverageDat) dat._exam._averageDat.get(AverageDat.avgDatKey(param, dat, subClass._code));

                if (null != avgDat) {
                    svf.VrsOut("AVERAGE",  sishaGonyu(avgDat._avg));
//                    svf.VrsOut("MAX_SCORE", ObjectUtils.toString(avgDat._highScore));
//                    svf.VrsOut("EXAMINEE", ObjectUtils.toString(avgDat._count));    // 受験者数
//                    if ("2".equals(param._deviationPrint)) {
//                        svf.VrsOut("DEVIATION", sishaGonyu(avgDat._stddev));
//                    }
                }
            }

            svf.VrEndRecord();
            if (++i >= TABLE_SUBCLASS_MAX) {
                break;
            }
        }
        if (i == 0) {
            svf.VrsOut("CLASS", "\n");
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
    }

    public void printBarGraph(final Vrw32alp svf, final ExamData dat, final Param param) {
        /** 棒グラフの最大科目数. */
        final int BAR_GRAPH_MAX_ITEM = 11;

        // グラフ用のデータ作成
        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
        final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
        int i = 0;
        for (final Iterator it = dat._record.values().iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
//            if (dat._attendSubclasses.contains(record._subClass)) {
//                continue; // 元科目は非表示
//            }
            scoreDataset.addValue(record._graphScore, "本人得点", record._subClass._abbv);
            final AverageDat avgDat = (AverageDat) dat._exam._averageDat.get(AverageDat.avgDatKey(param, dat, record._subClass._code));
            final BigDecimal graphAvg = (null == avgDat) ? null : avgDat._graphAvg;

            avgDataset.addValue(graphAvg, param._barChartLegendTitle + "平均点", record._subClass._abbv);

            log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + "(" + record._graphScore + ")" + ((avgDat == null) ? "" : ", 平均=" + avgDat._avg + "(" + avgDat._graphAvg + ")"));
            if (i++ > BAR_GRAPH_MAX_ITEM) {
                break;
            }
        }

        try {
            // チャート作成
            final JFreeChart chart = Chart.createBarChart(scoreDataset, avgDataset);

            // グラフのファイルを生成
            final File outputFile = Chart.graphImageFile(chart, 1940, 930);
            param._graphFiles.add(outputFile);

            // グラフの出力
            svf.VrsOut("BAR_LABEL", "得点");
            svf.VrsOut("BAR", outputFile.toString());

            if (null != param._barChartLegendImageFile) {
                svf.VrsOut("BAR_LEGEND", param._barChartLegendImageFile);
            }

            svf.VrsOut("BAR_TITLE", "得点グラフ");

        } catch (Throwable t) {
            log.fatal("error or exception!", t);
        }
    }

    private void printRadarGraph(final Vrw32alp svf, final ExamData dat, final Param param) {
        // データ作成
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final Iterator it = dat._record.values().iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
//            if (dat._attendSubclasses.contains(record._subClass)) {
//                continue; // 元科目は非表示
//            }
            Chart.setDataset(dataset, record);
        }

        // チャート作成
        try {
            final JFreeChart chart = Chart.createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = Chart.graphImageFile(chart, 930, 822);
           param._graphFiles.add(outputFile);

            // グラフの出力
            if (0 < dataset.getColumnCount()) {
                svf.VrsOut("RADER", outputFile.toString());
            }

            if (null != param._radarChartLegendImageFile) {
                svf.VrsOut("RADER_LEGEND", param._radarChartLegendImageFile);
            }
            svf.VrsOut("RADER_TITLE", "教科間バランス");

        } catch (Throwable t) {
            log.fatal("error or exception!", t);
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Chart {

        private static JFreeChart createBarChart(
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

        private static void setDataset(final DefaultCategoryDataset dataset, final Record record) {
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

        private static int dot2pixel(final int dot) {
            final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

            /*
             * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
             * 拡大すると粗くなってしまうから。
             */
            return (int) (pixel * 1.3);
        }
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

    private static class Exam {

        final String _year;
        final String _semester;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _grade;
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
                    _examName = StringUtils.defaultString(rs.getString("proficiencyname1"));
                }
            } catch (final SQLException e) {
                log.error("proficiencymst の取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
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
//            final String nendo = KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
//            _title = nendo + "　" + _semestername + "　" + _examName;
            _title = StringUtils.defaultString(_examName) + "　";
        }

        public String toString() {
            return "Exam(" + _year + ":" + _semester + ":" + _proficiencydiv + ":" + _proficiencycd + ":" + _examName + ")";
        }

        private static void loadExam(final DB2UDB db2, final List students, final Exam exam, final Param param) throws SQLException {
            SubClass.loadSubClasses(db2, students, exam, param); // 科目は指示画面指定のテストの科目
            AverageDat.loadAverageDat(db2, exam, param);
            Record.loadRecord(db2, students, exam, param);
//            SubClass.loadAttendSubclass(db2, students, exam, param);
            Record.loadRecordOther(db2, students, exam, param);
        }
    }

    private static class Regd {
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

        public String hrKey() {
            return _hrClass;
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String majorKey() {
            return _courseCd + _majorCd + "0000";
        }

        public String coursegroupKey() {
            return "0" + _coursegroupCd + "0000";
        }

        public String attendNoStr(final Param param) {
            if (!StringUtils.isNumeric(_attendNo)) {
                return "";
            }
            final StringBuffer stb = new StringBuffer();
            final String n = String.valueOf(Integer.parseInt(_attendNo));
//            for (int i = 0; i < n.length(); i++) {
//                stb.append(param._nums.get(n.substring(i, i + 1)));
//            }
            stb.append(n);
            return stb.append("番").toString();
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

    private static class ExamData {

        private Exam _exam = new Exam(null, null, null, null, null);

        private Regd _regd = new Regd(null, null, null, null, null, null, null, null);

        /** 模試データにある科目. */
        private final Map _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _record = new TreeMap();

//        /** 元科目。 */
//        private final Set _attendSubclasses = new TreeSet();

        /** 成績データ。3教科,5教科用 */
        private final Map _recordOther = new HashMap();
    }

    private static class Student implements Comparable {
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

        private static List createStudents(final DB2UDB db2, final Param _param) {
            final List rtn = new LinkedList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = studentsSQL(_param);
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
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            Collections.sort(rtn);
            return rtn;
        }

        private static String studentsSQL(final Param param) {
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
                + "  t1.year='" + param._year + "' AND"
                + "  t1.semester='" + param._semester + "' AND"
                + "  t1.schregno IN " + SQLUtils.whereIn(true, param._schregnos)
                ;
            return sql;
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

        private static void loadSubClasses(final DB2UDB db2, final List students, final Exam exam, final Param param) throws SQLException {
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

                        final SubClass subClass = (SubClass) param._subClasses.get(subClasscd);
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

        private static String sqlSubClasses(final Exam exam) {
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

//        private static String sqlAttendSubclass(
//                final Regd regd,
//                final Exam exam
//        ) {
//            final StringBuffer sql = new StringBuffer();
//            /* 通常の成績 */
//            sql.append(" SELECT ");
//            sql.append("     T1.DIV, ");
//            sql.append("     T1.COMBINED_SUBCLASSCD, ");
//            sql.append("     T1.ATTEND_SUBCLASSCD ");
//            sql.append(" FROM ");
//            sql.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
//            sql.append(" WHERE ");
//            sql.append("     T1.YEAR = '" + exam._year + "' ");
//            sql.append("     AND T1.SEMESTER = '" + exam._semester + "' ");
//            sql.append("     AND T1.PROFICIENCYDIV = '" + exam._proficiencydiv + "' ");
//            sql.append("     AND T1.PROFICIENCYCD = '" + exam._proficiencycd + "' ");
//            sql.append("     AND T1.GRADE = '" + regd._grade + "' ");
//            sql.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
//            sql.append("         CASE WHEN T1.DIV = '04' THEN '" + regd.coursegroupKey() + "' ");
//            sql.append("         ELSE '" + regd.courseKey() + "' END ");
//            sql.append(" ORDER BY ");
//            sql.append("     T1.DIV, ");
//            sql.append("     T1.COMBINED_SUBCLASSCD, ");
//            sql.append("     T1.ATTEND_SUBCLASSCD ");
//            return sql.toString();
//        }

//        private static void loadAttendSubclass(
//                final DB2UDB db2,
//                final List students,
//                final Exam exam,
//                final Param param
//        ) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                for (final Iterator it = students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//                    final Regd regd = student._regd;
//                    if (null == regd._grade) {
//                        continue;
//                    }
//                    final String sql = sqlAttendSubclass(regd, exam);
//                    ps = db2.prepareStatement(sql);
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        final String subclasscd = rs.getString("ATTEND_SUBCLASSCD");
//                        SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
//                        if (null == subClass) {
//                            log.warn("合併科目のデータが模試科目マスタに無い!:" + subclasscd);
//                            final Class clazz = (Class) param._classes.get(subclasscd.substring(0, 2));
//                            if (null == clazz) {
//                                continue;
//                            }
//                            subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
//                        }
//                        student._data._attendSubclasses.add(subClass);
//                    }
//                }
//            } catch (final SQLException e) {
//                log.error("合併科目のデータの取得でエラー", e);
//            } finally {
//                DbUtils.closeQuietly(rs);
//            }
//        }
    }

    private static class Record {
        final SubClass _subClass;
        final Integer _score;
        final String _scoreDi;
        final Integer _graphScore;
        final BigDecimal _avg;
        final Integer _rank;
        final BigDecimal _deviation;
//        final Integer _passScore;

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
//            _passScore = passScore;
        }

        public String getScore() {
            return null == _score ? (null == _scoreDi ? "" : _scoreDi) : _score.toString();
        }

//        public boolean isUnderScore() {
//            final String score = getScore();
//            final String passScore = ObjectUtils.toString(_passScore);
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

        private static void loadRecord(
                final DB2UDB db2,
                final List students,
                final Exam exam,
                final Param param
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
//                + "  T4.rank as course_rank,"
//                + "  T4.deviation as course_deviation,"
//                + "  T6.rank as coursegroup_rank,"
//                + "  T6.deviation as coursegroup_deviation,"
//                + "  T7.rank as hr_rank,"
//                + "  T7.deviation as hr_deviation,"
//                + "  T8.rank as major_rank,"
//                + "  T8.deviation as major_deviation,"
                + "  T9.score as score_kansannashi,"
                + "  T5.PASS_SCORE"
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
//                + " LEFT JOIN proficiency_rank_dat T4 ON "
//                + "     T4.year = T1.year AND "
//                + "     T4.semester=T1.semester AND"
//                + "     T4.proficiencydiv=T1.proficiencydiv AND"
//                + "     T4.proficiencycd=T1.proficiencycd AND"
//                + "     T4.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
//                + "     T4.schregno = T1.schregno AND"
//                + "     T4.rank_data_div =t3.rank_data_div AND"
//                + "     T4.rank_div = '" + RANK_DIV_COURSE + "' "
//                + " LEFT JOIN proficiency_rank_dat T6 ON "
//                + "     T6.year = T1.year AND "
//                + "     T6.semester=T1.semester AND"
//                + "     T6.proficiencydiv=T1.proficiencydiv AND"
//                + "     T6.proficiencycd=T1.proficiencycd AND"
//                + "     T6.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
//                + "     T6.schregno = T1.schregno AND"
//                + "     T6.rank_data_div =t3.rank_data_div AND"
//                + "     T6.rank_div = '" + RANK_DIV_COURSEGROUP + "' "
                + " LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT T5 ON T5.YEAR = T1.YEAR AND "
                + "     T5.semester=T2.semester AND"
                + "     T5.proficiencydiv=T2.proficiencydiv AND"
                + "     T5.proficiencycd=T2.proficiencycd AND"
                + "     T5.proficiency_subclass_cd=T2.proficiency_subclass_cd "
                + "     AND T5.GRADE = CASE WHEN T5.DIV = '01' THEN '00' ELSE ? END  "
                + "     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = "
                + "       CASE WHEN T5.DIV = '01' OR T5.DIV = '02' THEN '00000000' ELSE ? END "
//                + " LEFT JOIN proficiency_rank_dat T7 ON "
//                + "     T7.year = T1.year AND "
//                + "     T7.semester=T1.semester AND"
//                + "     T7.proficiencydiv=T1.proficiencydiv AND"
//                + "     T7.proficiencycd=T1.proficiencycd AND"
//                + "     T7.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
//                + "     T7.schregno = T1.schregno AND"
//                + "     T7.rank_data_div =t3.rank_data_div AND"
//                + "     T7.rank_div = '" + RANK_DIV_HR + "' "
//                + " LEFT JOIN proficiency_rank_dat T8 ON "
//                + "     T8.year = T1.year AND "
//                + "     T8.semester=T1.semester AND"
//                + "     T8.proficiencydiv=T1.proficiencydiv AND"
//                + "     T8.proficiencycd=T1.proficiencycd AND"
//                + "     T8.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
//                + "     T8.schregno = T1.schregno AND"
//                + "     T8.rank_data_div =t3.rank_data_div AND"
//                + "     T8.rank_div = '" + RANK_DIV_MAJOR + "' "
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
            loadRecord1(db2, students, sql, param);
        }

        private static void loadRecord1(
                final DB2UDB db2,
                final List students,
                final String sql,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                log.info(" rec sql = " + sql);
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
                        SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                        if (null == subClass) {
                            log.warn("対象成績データが模試科目マスタに無い!:" + subclasscd);
                            final Class clazz = (Class) param._classes.get(subclasscd.substring(0, 2));
                            if (null == clazz) {
                                continue;
                            }
                            subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
                        }
                        final Integer score1 = KNJServletUtils.getInteger(rs, "score");
                        final Integer scoreKansannnashi = KNJServletUtils.getInteger(rs, "score_kansannashi");
                        final Integer score = "1".equals(param._knjh563PrintScoreKansannashi) ? scoreKansannnashi : score1;
                        final String scoreDi = rs.getString("score_di");
                        if (score == null && StringUtils.isEmpty(scoreDi)) {
                            continue;
                        }

                        final Integer graphScore = KNJServletUtils.getInteger(rs, "graphScore");
                        final Integer rank;
                        final BigDecimal deviation;
//                        if (param.isHr()) {
//                            rank = KNJServletUtils.getInteger(rs, "hr_rank");
//                            deviation = rs.getBigDecimal("hr_deviation");
//                        } else if (param.isCourse()) {
//                            rank = KNJServletUtils.getInteger(rs, "course_rank");
//                            deviation = rs.getBigDecimal("course_deviation");
//                        } else if (param.isMajor()) {
//                            rank = KNJServletUtils.getInteger(rs, "major_rank");
//                            deviation = rs.getBigDecimal("major_deviation");
//                        } else if (param.isCoursegroup()) {
//                            rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
//                            deviation = rs.getBigDecimal("coursegroup_deviation");
//                        } else {
                            rank = KNJServletUtils.getInteger(rs, "grade_rank");
                            deviation = rs.getBigDecimal("grade_deviation");
//                        }

                        final Integer passScore = null; // KNJServletUtils.getInteger(rs, "PASS_SCORE");

                        final Record rec = new Record(subClass, score, scoreDi, graphScore, null, rank, deviation, passScore);
                        student._data._record.put(subClass, rec);
                    }
                }
            } catch (final SQLException e) {
                log.error("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static void loadRecordOther(
                final DB2UDB db2,
                final List students,
                final Exam exam,
                final Param param
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
//                    + "  t3.rank as course_rank,"
//                    + "  t3.deviation as course_deviation,"
//                    + "  t4.rank as coursegroup_rank,"
//                    + "  t4.deviation as coursegroup_deviation,"
//                    + "  t5.rank as major_rank,"
//                    + "  t5.deviation as major_deviation,"
//                    + "  t6.rank as hr_rank,"
//                    + "  t6.deviation as hr_deviation,"
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
//                    + " LEFT JOIN proficiency_rank_dat T3 ON "
//                    + "     T1.year = T3.year AND "
//                    + "     T1.semester=T3.semester AND"
//                    + "     T1.proficiencydiv=T3.proficiencydiv AND"
//                    + "     T1.proficiencycd=T3.proficiencycd AND"
//                    + "     T1.proficiency_subclass_cd=T3.proficiency_subclass_cd AND"
//                    + "     T1.schregno = T3.schregno AND "
//                    + "     T1.rank_data_div = T3.rank_data_div AND "
//                    + "     T3.rank_div = '" + RANK_DIV_COURSE + "' "
//                    + " LEFT JOIN proficiency_rank_dat T4 ON "
//                    + "     T1.year = T4.year AND "
//                    + "     T1.semester=T4.semester AND"
//                    + "     T1.proficiencydiv=T4.proficiencydiv AND"
//                    + "     T1.proficiencycd=T4.proficiencycd AND"
//                    + "     T1.proficiency_subclass_cd=T4.proficiency_subclass_cd AND"
//                    + "     T1.schregno = T4.schregno AND "
//                    + "     T1.rank_data_div = T4.rank_data_div AND "
//                    + "     T4.rank_div = '" + RANK_DIV_COURSEGROUP + "' "
//                    + " LEFT JOIN proficiency_rank_dat T5 ON "
//                    + "     T1.year = T5.year AND "
//                    + "     T1.semester=T5.semester AND"
//                    + "     T1.proficiencydiv=T5.proficiencydiv AND"
//                    + "     T1.proficiencycd=T5.proficiencycd AND"
//                    + "     T1.proficiency_subclass_cd=T5.proficiency_subclass_cd AND"
//                    + "     T1.schregno = T5.schregno AND "
//                    + "     T1.rank_data_div = T5.rank_data_div AND "
//                    + "     T5.rank_div = '" + RANK_DIV_MAJOR + "' "
//                    + " LEFT JOIN proficiency_rank_dat T6 ON "
//                    + "     T1.year = T6.year AND "
//                    + "     T1.semester=T6.semester AND"
//                    + "     T1.proficiencydiv=T6.proficiencydiv AND"
//                    + "     T1.proficiencycd=T6.proficiencycd AND"
//                    + "     T1.proficiency_subclass_cd=T6.proficiency_subclass_cd AND"
//                    + "     T1.schregno = T6.schregno AND "
//                    + "     T1.rank_data_div = T6.rank_data_div AND "
//                    + "     T6.rank_div = '" + RANK_DIV_HR + "' "
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
                    + "  t1.schregno='" + student._schregno + "'"
                    ;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        final Integer score1 = KNJServletUtils.getInteger(rs, "score");
                        final Integer scoreKansannnashi = KNJServletUtils.getInteger(rs, "score_kansannashi");
                        final Integer score = "1".equals(param._knjh563PrintScoreKansannashi) ? scoreKansannnashi : score1;

                        final BigDecimal avg = rs.getBigDecimal("avg");
                        final Integer rank;
                        final BigDecimal deviation;
//                        if (param.isHr()) {
//                            rank = KNJServletUtils.getInteger(rs, "hr_rank");
//                            deviation = rs.getBigDecimal("hr_deviation");
//                        } else if (param.isCourse()) {
//                            rank = KNJServletUtils.getInteger(rs, "course_rank");
//                            deviation = rs.getBigDecimal("course_deviation");
//                        } else if (param.isMajor()) {
//                            rank = KNJServletUtils.getInteger(rs, "major_rank");
//                            deviation = rs.getBigDecimal("major_deviation");
//                        } else if (param.isCoursegroup()) {
//                            rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
//                            deviation = rs.getBigDecimal("coursegroup_deviation");
//                        } else {
                            rank = KNJServletUtils.getInteger(rs, "grade_rank");
                            deviation = rs.getBigDecimal("grade_deviation");
//                        }

                        final SubClass subClass = new SubClass(subclasscd);
                        final Record rec = new Record(subClass, score, null, null, avg, rank, deviation, null);
                        student._data._recordOther.put(subClass._code, rec);
                    }
                } catch (final SQLException e) {
                    log.error("成績データの取得でエラー", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }
    }

    private static class AverageDat {
        final SubClass _subClass;
        final BigDecimal _avg;
        final BigDecimal _avgAvg;
        final BigDecimal _graphAvg;
        final BigDecimal _stddev;
        final Integer _highScore;
        final Integer _count;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal avgAvg,
                final BigDecimal graphAvg,
                final BigDecimal stddev,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _avgAvg = avgAvg;
            _graphAvg = graphAvg;
            _stddev = stddev;
            _highScore = highScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        public String toString() {
            return _subClass + "/" + _avg + "/" + _count;
        }

        private static String avgDatKey(final Param param, final ExamData dat, final String subclasscd) {
            final String key;
//            if (param.isHr()) {
//                key = subclasscd + dat._regd.hrKey();
//            } else if (param.isCourse()) {
//                key = subclasscd + dat._regd.courseKey();
//            } else if (param.isMajor()) {
//                key = subclasscd + dat._regd.majorKey();
//            } else if (param.isCoursegroup()) {
//                key = subclasscd + dat._regd.coursegroupKey();
//            } else {
                key = subclasscd;
//            }
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
        private static void loadAverageDat(
                final DB2UDB db2,
                final Exam exam,
                final Param param
        ) {

            final Map outAvgDat = exam._averageDat;
            final Map outAvgDatOther = exam._averageDatOther;

            final String selectFrom;
            selectFrom = ""
                + "WITH RANK_AVG_AVG AS ("
                + "SELECT"
                + "  t1.proficiency_subclass_cd,"
                + "  '" + AVG_DIV_GRADE + "' as avg_div,"
                + "  t2.grade,"
                + "  avg(t1.avg) as avg_avg"
                + " FROM"
                + "  proficiency_rank_dat t1 "
                + "  INNER JOIN schreg_regd_dat t2 on t2.schregno = t1.schregno "
                + "      AND t2.year = t1.year "
                + "      AND t2.semester = t1.semester "
                + " WHERE"
                + "    t1.year='" + exam._year + "' AND"
                + "    t1.semester='" + exam._semester + "' AND"
                + "    t1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    t1.proficiencycd='" + exam._proficiencycd + "' AND"
                + "    t2.grade='" + exam._grade + "' "
                + " GROUP BY "
                + "  t1.proficiency_subclass_cd,"
                + "  t2.grade "
                + ") "
                + "SELECT"
                + "  t1.proficiency_subclass_cd as subclasscd,"
                + "  t1.avg,"
                + "  t2.avg_avg,"
                + "  t1.stddev,"
                + "  t1.highscore,"
                + "  t1.avg_kansan as graphAvg,"
                + "  t1.count,"
                + "  t1.hr_class,"
                + "  t1.coursecd,"
                + "  t1.majorcd,"
                + "  t1.coursecode"
                + " FROM"
                + "  proficiency_average_dat t1 "
                + "  LEFT JOIN rank_avg_avg t2 on t2.proficiency_subclass_cd = t1.proficiency_subclass_cd "
                + "      AND t2.avg_div = t1.avg_div "
                + "      AND t2.grade = t1.grade "
                ;
            final String where;
//            if (param.isHr()) {
//                where = " WHERE"
//                    + "    year='" + exam._year + "' AND"
//                    + "    semester='" + exam._semester + "' AND"
//                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
//                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
//                    + "    data_div = '" + param._avgDataDiv + "' AND"
//                    + "    avg_div='" + AVG_DIV_HR + "' AND"
//                    + "    grade='" + exam._grade + "'"
//                    ;
//            } else if (param.isCourse()) {
//                where = " WHERE"
//                    + "    year='" + exam._year + "' AND"
//                    + "    semester='" + exam._semester + "' AND"
//                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
//                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
//                    + "    data_div = '" + param._avgDataDiv + "' AND"
//                    + "    avg_div='" + AVG_DIV_COURSE + "' AND"
//                    + "    grade='" + exam._grade + "' AND"
//                    + "    hr_class='000'"
//                    ;
//            } else if (param.isMajor()) {
//                where = " WHERE"
//                    + "    year='" + exam._year + "' AND"
//                    + "    semester='" + exam._semester + "' AND"
//                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
//                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
//                    + "    data_div = '" + param._avgDataDiv + "' AND"
//                    + "    avg_div='" + AVG_DIV_MAJOR + "' AND"
//                    + "    grade='" + exam._grade + "' AND"
//                    + "    hr_class='000'"
//                    ;
//            } else if (param.isCoursegroup()) {
//                where = " WHERE"
//                    + "    year='" + exam._year + "' AND"
//                    + "    semester='" + exam._semester + "' AND"
//                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
//                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
//                    + "    data_div = '" + param._avgDataDiv + "' AND"
//                    + "    avg_div='" + AVG_DIV_COURSEGROUP + "' AND"
//                    + "    grade='" + exam._grade + "' AND"
//                    + "    hr_class='000'"
//                    ;
//            } else {
                where = " WHERE"
                        + "    t1.year='" + exam._year + "' AND"
                        + "    t1.semester='" + exam._semester + "' AND"
                        + "    t1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                        + "    t1.proficiencycd='" + exam._proficiencycd + "' AND"
                        + "    t1.data_div = '" + param._avgDataDiv + "' AND"
                        + "    t1.avg_div='" + AVG_DIV_GRADE + "' AND"
                        + "    t1.grade='" + exam._grade + "' AND"
                        + "    t1.hr_class='000' AND"
                        + "    t1.coursecd='0' AND"
                        + "    t1.majorcd='000' AND"
                        + "    t1.coursecode='0000'"
                        ;
//            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = selectFrom + where;
                log.info(" avg1 sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("模試成績平均データが模試科目マスタに無い!:" + subclasscd);
                    }
                    final BigDecimal avg = rs.getBigDecimal("avg");
                    final BigDecimal avgAvg = rs.getBigDecimal("avg_avg");
                    final BigDecimal stddev = rs.getBigDecimal("stddev");
                    final BigDecimal graphAvg = rs.getBigDecimal("graphAvg");
                    final Integer count = KNJServletUtils.getInteger(rs, "count");
                    final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
//                    final String hrClass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");

                    final String key;
//                    if (param.isHr()) {
//                        key = subclasscd + hrClass;
//                    } else if (param.isCourse()) {
//                        key = subclasscd + coursecd + majorcd + coursecode;
//                    } else if (param.isMajor()) {
//                        key = subclasscd + coursecd + majorcd + "0000";
//                    } else if (param.isCoursegroup()) {
//                        key = subclasscd + "0" + majorcd + "0000";
//                    } else {
                        key = subclasscd;
//                    }
                    if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                        final SubClass subClass0 = new SubClass(subclasscd);
                        final AverageDat avgDat = new AverageDat(subClass0, avg, avgAvg, graphAvg, stddev, highScore, count, coursecd, majorcd, coursecode);
                        outAvgDatOther.put(key, avgDat);
                    } else {
                        final AverageDat avgDat = new AverageDat(subClass, avg, avgAvg, graphAvg, stddev, highScore, count, coursecd, majorcd, coursecode);
                        outAvgDat.put(key, avgDat);
                    }
                }
            } catch (final SQLException e) {
                log.error("模試成績平均データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug("模試コード=" + exam._proficiencydiv + " : " + exam._proficiencycd + " の模試成績平均データの件数=" + outAvgDat.size());
        }
    }

    private static class Param {
        /** レーダーチャート凡例画像. */
        final String RADER_CHART_LEGEND = "RaderChartLegend.png";
        /** 棒グラフ凡例画像1(学年). */
        final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
        /** 棒グラフ凡例画像2(クラス). */
        final String BAR_CHART_LEGEND2 = "BarChartLegendHr.png";
        /** 棒グラフ凡例画像3(コース). */
        final String BAR_CHART_LEGEND3 = "BarChartLegendCourse.png";
        /** 棒グラフ凡例画像4(学科). */
        final String BAR_CHART_LEGEND4 = "BarChartLegendMajor.png";
        /** 棒グラフ凡例画像5(コースグループ). */
        final String BAR_CHART_LEGEND5 = "BarChartLegendCourseGroup.png";

        final String _year;
        final String _semester;

//        final String _formGroupDiv;
        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;
        /** 中高一貫フラグ。 */
        private boolean _isIkkan;

        /** [クラス指定 or 生徒指定]の値。 */
        final String _categoryIsClass;
        /** クラス or 生徒。 */
        final String[] _categorySelected;

        final String _jisshiDate;
        final String _loginDate;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
//        /** 偏差値を印字するか? */
//        private final String _deviationPrint;
        /** 順位を印字するか? */
        private final String _juniPrint;
//        /** 保護者欄を印刷しないか? */
//        private final boolean _hogoshaNotPrint;
        /** 順位の基準点 1:総合点,2:平均点,3:偏差値,11:傾斜総合点 */
        private final String _rankDataDiv;
        /** 平均の基準点 1:得点,2:傾斜総合点 */
        private final String _avgDataDiv;
        private final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        private Map _classes;
        /** 模試科目マスタ。 */
        private Map _subClasses;
        private final String _imagePath;

//        private final Map _subclassGroup3 = new HashMap();
//        private final Map _subclassGroup5 = new HashMap();
        private final Map _subclassGroupDat3 = new HashMap();
        private final Map _subclassGroupDat5 = new HashMap();

        /** レーダーチャートの科目. */
        private List _fiveSubclass = new ArrayList();
        private List _threeSubclass = new ArrayList();

        private final Exam _thisExam;

        private final String _knjh563PrintScoreKansannashi;

        private final String _radarChartLegendImageFile;
        private final String _barChartLegendImageFile;
        private final String _barChartLegendTitle;
        private String[] _schregnos;
        /** グラフイメージファイルの Set&lt;File&gt; */
        private final Set _graphFiles = new HashSet();

//        private final Map _nums = new HashMap();

        public Param(final HttpServletRequest request, final DB2UDB db2) {
//            if ("4".equals(request.getParameter("JUNI"))) {
//                _rankDataDiv = "11";
//                _avgDataDiv = "2";
//            } else {
//                final String rankDivTemp = request.getParameter("useKnjd106cJuni" + request.getParameter("JUNI"));
//                final String rankDataDiv0 = (rankDivTemp == null) ? request.getParameter("JUNI") : rankDivTemp;
//                _rankDataDiv = (null != rankDataDiv0 && rankDataDiv0.length() < 2 ? "0" : "") + rankDataDiv0;
//                _avgDataDiv = "1";
//            }
            _rankDataDiv = "01";
            _avgDataDiv = "1";

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _jisshiDate = request.getParameter("JISSHI_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");

//            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");
//            _deviationPrint = request.getParameter("DEVIATION_PRINT");
            _juniPrint = request.getParameter("JUNI_PRINT");
//            _hogoshaNotPrint = "1".equals(request.getParameter("HOGOSHA_PRINT"));

            _imagePath = request.getParameter("IMAGE_PATH");

            String barLegendImageFile = null;
//            if (isHr()) {
//                barLegendImageFile = BAR_CHART_LEGEND2;
//                _barChartLegendTitle = "クラス";
//            } else if (isCourse()){
//                barLegendImageFile = BAR_CHART_LEGEND3;
//                _barChartLegendTitle = "コース";
//            } else if (isMajor()) {
//                barLegendImageFile = BAR_CHART_LEGEND4;
//                _barChartLegendTitle = "学科";
//            } else if (isCoursegroup()) {
//                barLegendImageFile = BAR_CHART_LEGEND5;
//                _barChartLegendTitle = "グループ";
//            } else {
                barLegendImageFile = BAR_CHART_LEGEND1;
                _barChartLegendTitle = "学年";
//            }
            _barChartLegendImageFile = imageFileCheck(barLegendImageFile);
            _radarChartLegendImageFile = imageFileCheck(RADER_CHART_LEGEND);

            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            imageFileCheck(BAR_CHART_LEGEND3);
            imageFileCheck(BAR_CHART_LEGEND4);
            imageFileCheck(BAR_CHART_LEGEND5);

            _thisExam = new Exam(_year, _semester, request.getParameter("PROFICIENCYDIV"), request.getParameter("PROFICIENCYCD"), request.getParameter("GRADE"));
            _knjh563PrintScoreKansannashi = request.getParameter("knjh563PrintScoreKansannashi");

            _schregnos = getScregnos(db2);
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

//            _nums.put("0", "０");
//            _nums.put("1", "１");
//            _nums.put("2", "２");
//            _nums.put("3", "３");
//            _nums.put("4", "４");
//            _nums.put("5", "５");
//            _nums.put("6", "６");
//            _nums.put("7", "７");
//            _nums.put("8", "８");
//            _nums.put("9", "９");
        }

        public boolean isGakunen() {
            return true;
//            return "1".equals(_formGroupDiv);
        }

//        public boolean isHr() {
//            return "2".equals(_formGroupDiv);
//        }
//
//        public boolean isCourse() {
//            return "3".equals(_formGroupDiv);
//        }
//
//        public boolean isMajor() {
//            return "4".equals(_formGroupDiv);
//        }
//
//        public boolean isCoursegroup() {
//            return "5".equals(_formGroupDiv);
//        }

        private String imageFileCheck(final String fName) {
            final File f = new File(_imagePath + "/" + fName);
            if (!f.exists()) {
                log.fatal("画像ファイルが無い!⇒" + _imagePath + "/" + fName);
                return null;
            }
            return f.getAbsolutePath();
        }

        public String[] getScregnos(final DB2UDB db2) {
            final String separator = "-";
            final List result = new ArrayList();

            if (!"1".equals(_categoryIsClass)) {
                return _categorySelected;
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            // 年組から学籍番号たちを得る
            for (int i = 0; i < _categorySelected.length; i++) {
                final String grade = StringUtils.split(_categorySelected[i], separator)[0];
                final String room = StringUtils.split(_categorySelected[i], separator)[1];

                try {
                    ps = db2.prepareStatement(hrToStudentsSQL(grade, room));
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("schregno");
                        result.add(schregno);
                    }
                } catch (final SQLException e) {
                    log.error("年組から学籍番号たちを得る際にエラー", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final Iterator it = result.iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                rtn[i++] = schregno;
            }

            return rtn;
        }

        private Map setSubClasses(final DB2UDB db2) {
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
            } catch (Exception e) {
                log.error("exception!", e);
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

        private Map setClasses(final DB2UDB db2) {
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
            } catch (Exception e) {
                log.fatal("exception!", e);
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
                log.error("担任名の取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadCertifSchool(final DB2UDB db2) {
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
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadIkkanFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT namespare2 FROM v_name_mst WHERE year='" + _year + "' AND namecd1='Z010' AND namecd2='00'");
                rs = ps.executeQuery();
                rs.next();
                final String namespare2 = rs.getString("namespare2");
                _isIkkan = (namespare2 != null) ? true : false;

                final int gradeVal = Integer.parseInt(_thisExam._grade);
                _isJunior = (gradeVal <= 3 && _isIkkan) ? true : false;
                _isHigh = !_isJunior;
            } catch (final SQLException e) {
                log.error("中高一貫フラグ取得エラー。", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
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
//                log.error("proficiency_subclass_group_mst の取得エラー。", e);
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

        private static List getMappedList(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList());
            }
            return (List) map.get(key1);
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
                        getMappedList(_subclassGroupDat3, key).add(subclassCd);
                    } else {
                        getMappedList(_subclassGroupDat5, key).add(subclassCd);
                    }
                }
            } catch (final SQLException e) {
                log.error("proficiency_subclass_group_dat の取得エラー");
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
                    + "  proficiency_subclass_cd as subclasscd"
                    + " FROM"
                    + "  proficiency_subclass_group_dat"
                    + " WHERE"
                    + "  year='" + _thisExam._year + "' AND"
                    + "  semester='" + _thisExam._semester + "' AND"
                    + "  proficiencydiv='" + _thisExam._proficiencydiv + "' AND"
                    + "  proficiencycd='" + _thisExam._proficiencycd + "' AND"
                    + "  grade='" + _thisExam._grade + "' AND"
                    + "  group_div in ('3', '5')"   // 3教科, 5教科
                    ;
        }

        private void removeImageFiles() {
            int delCount = 0;
            for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
                final File imageFile = (File) it.next();
                if (null == imageFile) {
                    continue;
                }
                final boolean delete = imageFile.delete();
                if (delete) {
                    delCount += 1;
                }
//                log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + delete);
            }
            log.fatal("グラフ画像ファイル削除:" + delCount + "件");

        }
    }
}

// eof
