// kanji=漢字
/*
 * $Id: 7160af31435f746d9810f8096c75357aeafd7e6f $
 *
 * 作成日: 2007/05/10 17:56:16 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.awt.BasicStroke;
import java.awt.Font;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

import jp.co.alp.kenja.common.dao.SQLUtils;

/**
 * 科目別得点分布表。
 * @author takaesu
 * @version $Id: 7160af31435f746d9810f8096c75357aeafd7e6f $
 */
public class KNJD623 {

    /*pkg*/static final Log log = LogFactory.getLog(KNJD623.class);

    /**
     * 年組グラフの最大人数(横軸)
     */
    private static final double _RANGE_VALUE_NORMAL = 25.0;

    /**
     * 学年のグラフの最大人数(横軸)
     */
    private static final double _RANGE_VALUE_GRADE = 150.0;

    /** 特殊な科目コード */
    private static final String _ALL5 = "555555";

    /**
     * 未到達者/欠点者の判定点数。
     * この点数未満が未到達者/欠点者となる。
     */
    private static final int _LESS_THAN_POINT = 30;

    /** 科目別 */
    private static final String FORM_FILE1 = "KNJD623_1.frm";
    /** レッスン別(講座) */
    private static final String FORM_FILE2 = "KNJD623_2.frm";

    private Form _form;
    private DB2UDB db2;

    private boolean _hasData;

    /** 印刷日時 */
    final Calendar _now = Calendar.getInstance();

    public KNJD623() {
        _now.setTime(new Date());
    }

    /**
     * グラフイメージファイルの Set&lt;File&gt;
     */
    private final Set _graphFiles = new HashSet();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log(request);

        final Param param = createParam(request);
        try {
            _form = createForm(response, param);
        } catch (final IOException e) {
            log.error("IOException:", e);
            throw e;
        }

        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            param.loadTestName(db2);

            final Map subClasses = loadSubClass(db2, param);

            final List homeRooms;
            if (param._isSubclassDiv) {
                homeRooms = createHomeRooms(db2, param._year, param._semester, param._grade);
                log.debug("年組=" + homeRooms);

                RecordAverageDat.load(db2, param);

            } else {
                homeRooms = null;
                RecordAverageChairDat.load(db2, param);
            }

            for (int i = 0; i < param._subClasses.length; i++) {
                final SubClass subClass = (SubClass) subClasses.get(param._subClasses[i]);
                log.debug("★" + subClass);

                subClass.setHomeRooms(homeRooms);
                printSvfMain(db2, param, subClass);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            // 終了処理
            _form.closeSvf();
            closeDb(db2);
            removeImageFiles();
        }
    }

    private Form createForm(final HttpServletResponse response, final Param param) throws IOException {
        if (param._isSubclassDiv) {
            return new SubClassForm(param._printName, response);
        } else {
            return new LessonForm(param._printName, response);
        }
    }

    private void log(HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        log.debug("未到達者/欠点者の判定点数=" + _LESS_THAN_POINT);
        log.debug("グラフの最大人数(横軸)=" + _RANGE_VALUE_NORMAL);
        log.debug("5科合計のグラフの最大人数(横軸)=" + _RANGE_VALUE_GRADE);
    }

    private List createHomeRooms(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String grade
    ) throws Exception {
        final List rtn = new ArrayList();
        final String sql = "SELECT HR_CLASS,HR_NAMEABBV FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "'"
                + " AND SEMESTER = '" + semester + "' AND GRADE = '" + grade + "'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String hrnameabbv = rs.getString("HR_NAMEABBV");
                final String hrclass = rs.getString("HR_CLASS");
                final HomeRoom hr = new HomeRoom(grade, hrclass, hrnameabbv);
                rtn.add(hr);
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
        Collections.sort(rtn);
        return rtn;
    }

    private void loadChairs(final Param param, final SubClass subClass) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlChairs(param, subClass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String chairCd = rs.getString("CHAIRCD");
                final String groupCd = rs.getString("GROUPCD");
                final String name = rs.getString("CHAIRNAME");
                final Chair chair = new Chair(chairCd, groupCd, name);
                log.debug("講座:" + chair);

                subClass._chairs.put(chairCd, chair);
            }
        } catch (final Exception ex) {
            log.error("講座のロードでエラー:" + sql, ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlChairs(final Param param, final SubClass subClass) {
        final String rtn;
        rtn = "SELECT t1.chaircd, t1.groupcd, t1.chairname"
                + " FROM chair_dat t1, chair_cls_dat t2"
                + " WHERE t1.year = t2.year"
                + " AND t1.semester = t2.semester"
                + " AND t1.chaircd = t2.chaircd"
                + " AND t1.groupcd = t2.groupcd"
                + " AND t2.trgtgrade='" + param._grade + "'"
                + " AND t1.year='" + param._year + "'"
                + " AND t1.semester='" + param._semester + "'"
                + " AND t1.subclasscd='" + subClass._code + "'"
                + " UNION"
                + " SELECT t1.chaircd, t1.groupcd, t1.chairname"
                + " FROM chair_dat t1, chair_cls_dat t2"
                + " WHERE t1.year = t2.year"
                + " AND t1.semester = t2.semester"
                + " AND t2.chaircd = '0000000'"
                + " AND t1.groupcd = t2.groupcd"
                + " AND t2.trgtgrade='" + param._grade + "'"
                + " AND t1.year='" + param._year + "'"
                + " AND t1.semester='" + param._semester + "'"
                + " AND t1.subclasscd='" + subClass._code + "'"
            ;
        return rtn;
    }

    private Map loadSubClass(final DB2UDB db2, final Param param) {
        final Map rtn = new HashMap();

        // 科目を取得する。
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlSubClasses(param));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("SUBCLASSCD");
                final String name = rs.getString("SUBCLASSNAME");
                final String abbv = rs.getString("ABBV");

                final SubClass subClass = new SubClass(code, name, abbv);
                log.debug("科目=" + subClass);
                rtn.put(code, subClass);
            }
        } catch (final Exception ex) {
            log.error("科目一覧取得エラー", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (Arrays.asList(param._subClasses).contains(_ALL5)) {
            final SubClass subClass = new SubClass(_ALL5, "５科合計", "5科計");
            subClass._distributeMax = "500";
            subClass._kizami = 500;

            rtn.put(_ALL5, subClass);
        }

        // 科目に講座をぶら下げる
        for (final Iterator it = rtn.values().iterator(); it.hasNext();) {
            final SubClass subclass = (SubClass) it.next();
            loadChairs(param, subclass);
        }

        return rtn;
    }

    private String sqlSubClasses(final Param param) {
        final String rtn;
        rtn = "SELECT"
            + "    subclasscd,"
            + "    subclassname,"
            + "    subclassabbv AS abbv"
            + " FROM"
            + "    v_subclass_mst"
            + " WHERE"
            + "    year = '" + param._year + "'"
            + " AND subclasscd in " + SQLUtils.whereIn(true, param._subClasses)
            ;
        return rtn;
    }

    private void printSvfMain(
            final DB2UDB db2,
            final Param param,
            final SubClass subClass
    ) {
        _hasData = true;
        _form.setSubClass(subClass, param);

        final int[] gradeDistribute;
        if (param._isSubclassDiv) {
            // 成績席次データ(最高点、最低点、30点未満者数、グラフ分布)を得る。
            RecordRankDat.load(db2, param, subClass, subClass._homeRooms);
            gradeDistribute = printHrDetail(db2, param, subClass);

            // 最後の項目は合計(学年)
            final RecordAverageDat avgDat = RecordAverageDat.getGrade(subClass._code);
            _form.printSvfGrade(db2, param, subClass, avgDat, gradeDistribute);
        } else {
            // 成績講座席次データ(最高点、最低点、30点未満者数、グラフ分布)を得る。
            RecordRankChairDat.load(db2, param, subClass);
            gradeDistribute = printChairDetail(db2, param, subClass);

            // 最後の項目は合計(学年)
// TODO: レッスン別は、科目別と同じロジックになる '07.06.12 宮城さんからの仕様
//            final RecordAverageChairDat avgDat = RecordAverageChairDat.getGrade(subClass._code);
//            _form.printSvfGrade(db2, param, subClass, avgDat, gradeDistribute);
        }

        //
        _form.endPage();
    }

    private int[] printHrDetail(final DB2UDB db2, final Param param, final SubClass subClass) {
        final int[] gradeDistribute = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (final Iterator it = subClass._homeRooms.iterator(); it.hasNext();) {
            final HomeRoom hr = (HomeRoom) it.next();

            final RecordAverageDat rad = RecordAverageDat.get1record(subClass._code, hr._room);
            final String person = getSubClassPerson(db2, param, subClass, hr);

            final MaxMin maxMin = RecordRankDat.getMaxMin(hr);
            final String lessThan = RecordRankDat.lessThan(_LESS_THAN_POINT, hr);
            final int[] distribute = RecordRankDat.distribute(hr, subClass._kizami);

            final String count = (null == rad) ? "" : String.valueOf(rad._count);
            final String avg = (null == rad) ? "" : String.valueOf(rad._avg);

            _form.printSvfDetail(db2, param, subClass, count, avg, (Hoge) hr, person, maxMin, lessThan, distribute);

            final int[] hrDistribute = RecordRankDat.distribute(hr, subClass._kizami);
            if (null != hrDistribute) {
                for (int i = 0; i < gradeDistribute.length; i++) {
                    gradeDistribute[i] += hrDistribute[i];
                }
            }
        }
        return gradeDistribute;
    }

    private int[] printChairDetail(final DB2UDB db2, final Param param, final SubClass subClass) {
        final int[] gradeDistribute = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (final Iterator it = subClass._chairs.values().iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();

            final RecordAverageChairDat rad = RecordAverageChairDat.get1record0(subClass._code, chair.getKey());
            final String person = getChairPerson(db2, param, subClass, chair);

            final MaxMin maxMin = RecordRankChairDat.getMaxMin(chair);
            final String lessThan = RecordRankChairDat.lessThan(_LESS_THAN_POINT, chair);
            final int[] distribute = RecordRankChairDat.distribute(chair, subClass._kizami);

            final String count = (null == rad) ? "" : String.valueOf(rad._count);
            final String avg = (null == rad) ? "" : String.valueOf(rad._avg);

            _form.printSvfDetail(db2, param, subClass, count, avg, (Hoge) chair, person, maxMin, lessThan, distribute);

            final int[] hrDistribute = RecordRankChairDat.distribute(chair, subClass._kizami);
            if (null != hrDistribute) {
                for (int i = 0; i < gradeDistribute.length; i++) {
                    gradeDistribute[i] += hrDistribute[i];
                }
            }
        }
        return gradeDistribute;
    }

    private String getSubClassPerson(final DB2UDB db2, final Param param, final SubClass subClass, final HomeRoom hr) {
        final String person;
        if (_ALL5.startsWith(subClass._code)) {
            person = "";
        } else {
            person = loadPerson(db2, param, subClass, hr);
        }
        return person;
    }

    private String getChairPerson(final DB2UDB db2, final Param param, final SubClass subClass, final Chair chair) {
        final String person;
        if (_ALL5.startsWith(subClass._code)) {
            person = "";
        } else {
            person = loadPerson(db2, param, subClass, chair);
        }
        return person;
    }

    private String loadPerson(
            final DB2UDB db2,
            final Param param,
            final SubClass subClass,
            final HomeRoom hr
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String rtn = "";
        try {
            ps = db2.prepareStatement(sqlPerson(param, subClass, hr));
            rs = ps.executeQuery();
            while (rs.next()) {
                rtn = rs.getString("STAFFNAME");
                break;
            }
        } catch (final Exception ex) {
            log.error("担当者名の取得でエラー", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String loadPerson(
            final DB2UDB db2,
            final Param param,
            final SubClass subClass,
            final Chair chair
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String rtn = "";
        try {
            ps = db2.prepareStatement(sqlChairPerson(param, subClass, chair));
            rs = ps.executeQuery();
            while (rs.next()) {
                rtn = rs.getString("STAFFNAME");
                break;
            }
        } catch (final Exception ex) {
            log.error("担当者名の取得でエラー", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlPerson(
            final Param param,
            final SubClass subClass,
            final HomeRoom hr
    ) {
        final String chairs = SQLUtils.whereIn(true, subClass.chairsArray());

        final String rtn;
        rtn = "SELECT"
            + "    t3.staffname"
            + " FROM"
            + "    chair_stf_dat t1, chair_cls_dat t2, v_staff_mst t3"
            + " WHERE t1.year = t2.year"
            + " AND t1.year = t3.year"
            + " AND t1.semester = t2.semester"
            + " AND t1.chaircd = t2.chaircd"
            + " AND t1.staffcd = t3.staffcd"
            + " AND t1.chargediv = 1" // 正担任
            + " AND t1.year = '" + param._year + "'"
            + " AND t1.semester = '" + param._semester + "'"
            + " AND t1.chaircd in " + chairs
            + " AND t2.trgtgrade = '" + hr._grade + "'"
            + " AND t2.trgtclass = '" + hr._room + "'"
            + " ORDER BY t1.staffcd"
            ;
        return rtn;
    }

    private String sqlChairPerson(
            final Param param,
            final SubClass subClass,
            final Chair chair
    ) {
        final String rtn;
        rtn = "SELECT t2.staffname"
            + " FROM chair_stf_dat t1, v_staff_mst t2"
            + " WHERE t1.year = t2.year"
            + " AND t1.staffcd = t2.staffcd"
            + " AND t1.chargediv = 1" // 正担任
            + " AND t1.year = '" + param._year + "'"
            + " AND t1.semester = '" + param._semester + "'"
            + " AND t1.chaircd = '" + chair._code + "'"
            + " ORDER BY t1.staffcd"
            ;
        return rtn;
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

    private JFreeChart createChart(final int[] pointArray, final double rangeValue) {
        // カテゴリーの作成
        final String[] category = {
                "0-9",
                "10-19",
                "20-29",
                "30-39",
                "40-49",
                "50-59",
                "60-69",
                "70-79",
                "80-89",
                "90-100",
        };

        // 棒グラフのデータセットの生成
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        // グラフは横置きで、上から100点台, 90点台... と続くので、降順に配置する
        for (int i = category.length -1; i >= 0; i--) {
            barDataset.addValue(new Integer(pointArray[i]), "ほげ", category[i]);
        }

        // ベースとなるグラフを生成
        JFreeChart chart = ChartFactory.createBarChart(null, null, null, barDataset, PlotOrientation.HORIZONTAL, false, false, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
        plot.getRangeAxis().setTickLabelsVisible(false);
        plot.getDomainAxis().setTickLabelsVisible(false);

        // 棒の上に数値
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
//        renderer.setBaseItemLabelFont(new Font("TimesRoman", Font.PLAIN, 20));
//        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.PLAIN, 20));
        renderer.setBaseItemLabelFont(new Font("Dialog", Font.PLAIN, 20));

        plot.getRangeAxis().setRange(0, rangeValue);
//        plot.getRangeAxis().setAutoRange(false);    // true だと上記の上限が無効になってしまう。

//        CategoryItemRenderer renderer = plot.getRenderer();
//        renderer.setSeriesPaint(0, Color.gray);
//        renderer.setItemLabelGenerator(new LabelGenerator(index));
//        final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
//        renderer.setItemLabelFont(itemLabelFont);
//        renderer.setItemLabelsVisible(true);

        return chart;
    }

    private File createGraphFile(final JFreeChart chart, final int width, final int height) {
        final String tmpFileName = createTmpFile();

        final File outputFile = new File(tmpFileName);
        try {
            ChartUtilities.saveChartAsPNG(outputFile, chart, width, height);
        } catch (final IOException ioEx) {
            log.error("グラフイメージをファイル化できません。", ioEx);
        }

        return outputFile;
    }

    private static String createTmpFile() {
        final String tmpFolder = SystemUtils.JAVA_IO_TMPDIR;
        final String prefix = Long.toString(System.currentTimeMillis());

        return tmpFolder + SystemUtils.FILE_SEPARATOR + prefix + ".png";
    }

    private Param createParam(final HttpServletRequest req) {
        final String year = req.getParameter("YEAR");
        final String semester = req.getParameter("SEMESTER");
        final String grade = req.getParameter("GRADE");
        final String testCd = req.getParameter("TESTCD");
        final String[] subClasses = req.getParameterValues("CATEGORY_SELECTED");

        final String subClassDiv = req.getParameter("SUBCLASSDIV");
        final boolean isSubclassDiv = "1".equals(subClassDiv);
        final String hoge = isSubclassDiv ? "科目別" : "レッスン別";
        log.debug("出力区分=" + subClassDiv + "(" + hoge + ")");

        boolean graphBool;
        final String useGraph = req.getParameter("DISABLE_GRAPH");
        graphBool = !"1".equals(useGraph);
        log.debug("グラフ描画の指示:" + graphBool);

        if (graphBool && KNJServletUtils.isEnableGraph(log)) {
            graphBool = true;
        }
        log.debug("最終的なグラフの描画:" + graphBool);

        final String csv = req.getParameter("USE_CSV");
        final boolean useCsv = "1".equals(csv);
        log.debug("CSV出力:" + useCsv);

        final String printName = req.getParameter("PRINTNAME");

        final Param param = new Param(year, semester, grade, testCd, subClasses, graphBool, useCsv, isSubclassDiv, printName);

        return param;
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

    // ====

    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String[] _subClasses;
        private final String _testCd;
        private final String _testItem;
        private final String _testKind;
        private final boolean _useGraph;
        private final boolean _useCsv;
        /** 科目別か? false ならレッスン別 */
        private final boolean _isSubclassDiv;
        private final String _printName;
        
        private String _testName;

        /* pkg */Param(
                final String year,
                final String gakki,
                final String grade,
                final String testCd,
                final String[] subClasses,
                final boolean useGraph,
                final boolean useCsv,
                final boolean isSubclassDiv,
                final String printName
        ) {
            _year = year;
            _semester = gakki;
            _grade = grade;
            _testCd = testCd;
            _subClasses = subClasses;
            _useGraph = useGraph;
            _useCsv = useCsv;
            _isSubclassDiv = isSubclassDiv;
            _printName = printName;

            if (null == _testCd || _testCd.length() != 4) {
                throw new IllegalArgumentException("テスト種別・項目が不正: " + _testCd);
            }
            _testKind = _testCd.substring(0, 2);
            _testItem = _testCd.substring(2, 4);
        }

        /**
         * 中学か？
         * 判定できない場合は中学とする。
         * @return 中学なら true
         */
        public boolean isLow() {
            if (!StringUtils.isNumeric(_grade)) {
                return true;
            }
            final int grade = Integer.parseInt(_grade);
            return grade <= 3;
        }

        void loadTestName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testName = rs.getString("TESTITEMNAME");
                    log.debug("テスト名称=" + _testName);
                    break;
                }
            } catch (final Exception ex) {
                log.error("テスト名称のロードでエラー", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sql() {
            return "select"
                    + "    TESTITEMNAME"
                    + "  from"
                    + "    TESTITEM_MST_COUNTFLG_NEW"
                    + "  where"
                    + "    YEAR='" + _year + "' and"
                    + "    SEMESTER = '" + _semester + "' and"
                    + "    TESTKINDCD = '" + _testKind + "' and"
                    + "    TESTITEMCD = '" + _testItem + "'"
            ;
        }
    } // Param

    // ======================================================================

    private abstract class Form {
        /** 1頁に印字できる列 */
        public int MAX_COLUMN;

        private Vrw32alp _svf;
        private PrintWriter _outStrm;
        private String _printName;

        int _hrIndex;

        /* pkg */Form() {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
        }

        public void setSubClass(final SubClass subClass, final Param param) {
            printHeader(param);
            printStatic(param, subClass);

            vrsOut("CLASS1", subClass._name);
            _hrIndex = 1;
        }

        /* pkg */Form(final String printName, HttpServletResponse response) throws IOException {
            this();
            _printName = printName;
            if (null != printName) {
                setPrinter("", printName);
                response.setContentType("text/html");
                _outStrm = new PrintWriter(response.getOutputStream());
            } else {
                setSpoolFileStream(response.getOutputStream());
                response.setContentType("application/pdf");
                _outStrm = null;
            }
        }

        /* pkg */void setMaxColumn(final int column) {
            MAX_COLUMN = column;
        }

        private void closeSvf() {
            if (_printName != null) {
                outputHtml();
            } else if (!_hasData) {
                setForm("MES001.frm", 0);
                vrsOut("note", "note");
                endPage();
            }

            final int ret = quit();
            log.info("===> VrQuit():" + ret);

            if (null != _outStrm) {
                _outStrm.close(); // ストリームを閉じる
            }
        }

        private void outputHtml() {
            _outStrm.println("<HTML>");
            _outStrm.println("<HEAD>");
            _outStrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
            _outStrm.println("</HEAD>");
            _outStrm.println("<BODY>");
            if (_hasData) {
                _outStrm.println("<H1>印刷しました。</h1>");
            } else {
                _outStrm.println("<H1>対象データはありません。</h1>");
            }
            _outStrm.println("</BODY>");
            _outStrm.println("</HTML>");
        }

        public int setForm(String form, int mode) {
            return _svf.VrSetForm(form, mode);
        }

        public int setSpoolFileStream(ServletOutputStream outputStream) {
            return _svf.VrSetSpoolFileStream(outputStream);
        }

        public int setPrinter(String string, String printName) {
            return _svf.VrSetPrinter("", printName);
        }

        public int vrsOut(String field, String data) {
            return _svf.VrsOut(field, data);
        }

        public int endPage() {
            return _svf.VrEndPage();
        }

        public int quit() {
            return _svf.VrQuit();
        }

        private void printHeader(final Param param) {
            final Integer year = Integer.valueOf(param._year);
            final String nendo = KenjaProperties.gengou(year.intValue()) + "年度";
            final String title = nendo + " " + param._testName + " 科目別分布表（成績判定資料）";
            vrsOut("TITLE", title);

            final String nowYear = KenjaProperties.gengou(_now.get(Calendar.YEAR));
            final SimpleDateFormat sdf = new SimpleDateFormat("M月d日H時m分");
            final String sakuseibi = "作成日：" + nowYear + "年" + sdf.format(_now.getTime());
            vrsOut("NOW", sakuseibi);
        }

        private void printStatic(final Param param, final SubClass subClass) {
            if (param._isSubclassDiv) {
                vrsOut("ITEM1_1", "クラス");
            } else {
                vrsOut("ITEM1_1", "講座");
            }
            vrsOut("ITEM1_2", "人数");
            vrsOut("ITEM1_3", "担当者");
            vrsOut("ITEM1_4", "平均");
            vrsOut("ITEM1_5", "最高点");
            vrsOut("ITEM1_6", "最低点");
            vrsOut("ITEM1_7", subClass.getLessTitle(param.isLow()));
        }

        private void printSvfDetail(
                final DB2UDB db2,
                final Param param,
                final SubClass subClass,
                final String count,
                final String avg,
                final Hoge hoge,
                final String person,
                final MaxMin maxMin,
                final String lessThan,
                final int[] distribute
        ) {
            final String svfField = param._isSubclassDiv ? "HR_NAME1_" : "CHAIR1_";
            vrsOut(svfField + _hrIndex, hoge.getAbbv());

            vrsOut("COUNT1_" + _hrIndex, count);
            vrsOut("AVERAGE1_" + _hrIndex, avg);

            vrsOut("PERSONCHARGE1_" + _hrIndex, person);

            if (null != maxMin) {
                vrsOut("HIGH1_" + _hrIndex, maxMin.getMax());
                vrsOut("LOW1_" + _hrIndex, maxMin.getMin());
            }
            if (!_ALL5.startsWith(subClass._code)) {
                vrsOut("WEAK1_" + _hrIndex, lessThan);
            }

            vrsOut("POINT1", "0");
            vrsOut("POINT2", subClass._distributeMax);

            if (param._useGraph) {
                printGraph(_hrIndex, distribute, subClass, _RANGE_VALUE_NORMAL, 200);
            }
            _hrIndex++;
            if (_hrIndex > MAX_COLUMN) {
                endPage();
                setSubClass(subClass, param);   // TAKAESU: ページがピッタリで終わった時は大丈夫?
            }
        }

        public void printSvfGrade(
                final DB2UDB db2,
                final Param param,
                final SubClass subClass,
                final RecordAverageDat rad,
                final int[] gradeDistribute
        ) {
            vrsOut("HR_NAME1_" + _hrIndex, "学年");
            final String count = (null == rad) ? "" : String.valueOf(rad._count);
            vrsOut("COUNT1_" + _hrIndex, count);
            
            final String avg = (null == rad) ? "" : String.valueOf(rad._avg);
            vrsOut("AVERAGE1_" + _hrIndex, avg);

            vrsOut("HIGH1_" + _hrIndex, RecordRankDat.getGradeMax());
            vrsOut("LOW1_" + _hrIndex, RecordRankDat.getGradeMin());
            if (!_ALL5.startsWith(subClass._code)) {
                vrsOut("WEAK1_" + _hrIndex, String.valueOf(RecordRankDat._gradeLessThan));
            }
            if (param._useGraph) {
                printGraph(_hrIndex, gradeDistribute, subClass, _RANGE_VALUE_GRADE, 200);
            }
        }

        private void printGraph(final int hrIndex, final int[] distribute, final SubClass subClass, final double rangeValue, final int width) {
            if (null != distribute) {
                final JFreeChart chart = createChart(distribute, rangeValue);
                final File graphFile = createGraphFile(chart, width, 200);
                _graphFiles.add(graphFile);
                vrsOut("CHART1_" + hrIndex, graphFile.toString());
            }
        }
    } // Form

    private class SubClassForm extends Form {
        public SubClassForm(String printName, HttpServletResponse response) throws IOException {
            super(printName, response);
            setForm(FORM_FILE1, 1);
            setMaxColumn(5);
        }
    } // SubClassForm

    private class LessonForm extends Form {
        public LessonForm(String printName, HttpServletResponse response) throws IOException {
            super(printName, response);
            setForm(FORM_FILE2, 1);
            setMaxColumn(8);
        }
    } // LessonForm

    private class SubClass {
        public String _distributeMax;
        public int _kizami;

        private final String _code;
        private final String _name;
        private final String _abbv;
        private Map _chairs = new TreeMap();

        private List _homeRooms;

        public SubClass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;

            _distributeMax = "100";
            _kizami = 100;
        }

        public void setHomeRooms(final List homeRooms) {
            _homeRooms = homeRooms;
        }

        public String getLessTitle(final boolean isLow) {
            if (_ALL5.startsWith(_code)) {
                return "";
            }
            return isLow ? "未到達者数" : "欠点者数";
        }

        public String[] chairsArray() {
            final String[] codes = new String[_chairs.size()];

            int i = 0;
            for (final Iterator it = _chairs.keySet().iterator(); it.hasNext();) {
                final String code = (String) it.next();
                codes[i++] = code;
            }

            return codes;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }
    } // SubClass

    private class Chair implements Hoge {
        private final String _code;
        private final String _groupCd;
        private final String _name;

        public Chair(final String chairCd, final String groupCd, final String name) {
            _code = chairCd;
            _groupCd = groupCd;
            _name = name;
        }

        public String toString() {
            return _code + ":" + _name;
        }

        public String getKey() {
            return _code;
        }

        public String getAbbv() {
            return _name;
        }
    } // Chair

    private class HomeRoom implements Comparable, Hoge {
        private final String _grade;
        private final String _room;
        private final String _nameabbv;

        HomeRoom(
                final String grade,
                final String room,
                final String name
        ) {
            _grade = grade;
            _room = room;
            _nameabbv = name;
        }
        public String toString() {
            return _grade + "-" + _room + ":" + _nameabbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HomeRoom)) {
                return -1;
            }
            final HomeRoom that = (HomeRoom) o;

            // 学年
            if (!this._grade.equals(that._grade)) {
                return this._grade.compareTo(that._grade);
            }

            // 組（学年が同じだった場合）
            return this._room.compareTo(that._room);
        }

        public String getKey() {
            return _room;
        }

        public String getAbbv() {
            return _nameabbv;
        }
    } // HomeRoom

    private static class RecordAverageDat {
        /**
         * 平均区分。学年
         */
        public static final String M_GRADE = "1";
        /**
         * 平均区分。クラス
         */
        public static final String M_CLASS = "2";

        private static final List _list = new ArrayList();

        /* pkg */final int _count;
        /* pkg */final BigDecimal _avg;
        /* pkg */final String _subClassCd;
        /* pkg */final String _avgDiv;
        /* pkg */final String _hrClass;

        RecordAverageDat(
                final int count,
                final BigDecimal avg,
                final String subClassCd,
                final String avgDiv,
                final String hrClass
        ) {
            _count = count;
            _avg = avg;

            _subClassCd = subClassCd;
            _avgDiv = avgDiv;
            _hrClass = hrClass;
        }

        /**
         * クラスの成績平均データを得る。
         * @param subClassCd 科目コード
         * @param hrClass 組
         * @return クラスの成績平均データ。該当データが無ければ null
         */
        public static RecordAverageDat get1record(
                final String subClassCd,
                final String hrClass
        ) {
            for (final Iterator it = _list.iterator(); it.hasNext();) {
                final RecordAverageDat data = (RecordAverageDat) it.next();

                // search
                if (!data._subClassCd.equals(subClassCd)) {
                    continue;
                }
                if (data._avgDiv.equals(M_CLASS) && data._hrClass.equals(hrClass)) {
                    return data;
                }
            }
            return null;
        }

        /**
         * 学年の成績平均データを得る。
         * @param subClassCd 科目コード
         * @return 学年の成績平均データ。該当データが無ければ null
         */
        public static RecordAverageDat getGrade(final String subClassCd) {
            for (final Iterator it = _list.iterator(); it.hasNext();) {
                final RecordAverageDat data = (RecordAverageDat) it.next();

                // search
                if (!data._subClassCd.equals(subClassCd)) {
                    continue;
                }
                if (data._avgDiv.equals(M_GRADE) && data._hrClass.equals("000")) {  // 組は"000"固定
                    return data;
                }
            }
            return null;
        }

        public synchronized static void load(final DB2UDB db2, final Param param) {
            _list.clear();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlAverageDat(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer count = KNJServletUtils.getInteger(rs, "COUNT");
                    final BigDecimal avg0 = rs.getBigDecimal("AVG");
                    final BigDecimal avg = (null == avg0) ? null : avg0.setScale(1, BigDecimal.ROUND_HALF_UP);
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final String avgDiv = rs.getString("AVG_DIV");
                    final String hrClass = rs.getString("HR_CLASS");
                    final RecordAverageDat avgDat = new RecordAverageDat(
                            count.intValue(),
                            avg,
                            subClassCd,
                            avgDiv,
                            hrClass
                    );
                    _list.add(avgDat);
                }
            } catch (final Exception ex) {
                log.error("ロードでエラー", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("成績平均データの件数=" + _list.size());
        }
        
        private static String sqlAverageDat(final Param param) {
            final String rtn;
            rtn = "SELECT"
                + "    count,"
                + "    avg,"
                + "    subclasscd,"
                + "    avg_div,"
                + "    hr_class"
                + " FROM"
                + "    record_average_dat"
                + " WHERE year = '" + param._year + "'"
                + " AND semester = '" + param._semester + "'"
                + " AND testkindcd = '" + param._testKind + "'"
                + " AND testitemcd = '" + param._testItem + "'"
                + " AND avg_div in ('1', '2')"  // 1:学年 2:クラス 3:コース
                + " AND grade = '" + param._grade + "'"
                + " AND coursecd = '0'"
                + " AND majorcd = '000'"
                + " AND coursecode = '0000'"
                ;
            log.debug("成績平均データのsql=" + rtn);
            return rtn;
        }

        public String toString() {
            return "成績平均データ、人数と平均:" + _count + ", " + _avg;
        }
    } // RecordAverageDat

    private static class RecordAverageChairDat extends RecordAverageDat {
        private static final List _list = new ArrayList();

        private String _chairCd;

        private RecordAverageChairDat(
                final int count,
                final BigDecimal avg,
                final String subClassCd,
                final String avgDiv,
                final String hrClass
        ) {
            super(count, avg, subClassCd, avgDiv, hrClass);
        }

        /* pkg */RecordAverageChairDat(
                final int count,
                final BigDecimal avg,
                final String subClassCd,
                final String chairCd,
                final String avgDiv,
                final String hrClass
        ) {
            this(count, avg, subClassCd, avgDiv, hrClass);
            _chairCd = chairCd;
        }

        /**
         * 講座の成績平均データを得る。
         * @param subClassCd 科目コード
         * @param chairCd 講座
         * @return 講座の成績平均データ。該当データが無ければ null
         */
        public static RecordAverageChairDat get1record0(
                final String subClassCd,
                final String chairCd
        ) {
            for (final Iterator it = _list.iterator(); it.hasNext();) {
                final RecordAverageChairDat data = (RecordAverageChairDat) it.next();

                // search
                if (!data._subClassCd.equals(subClassCd)) {
                    continue;
                }
                if (data._avgDiv.equals(M_GRADE) && data._chairCd.equals(chairCd)) {
                    return data;
                }
            }
            return null;
        }

        public static RecordAverageDat get1record(
                final String subClassCd,
                final String hrClass
        ) {
            throw new IllegalAccessError("使えません");
        }

        public static RecordAverageChairDat getGrade(final String subClassCd, final String chairCd) {
            for (final Iterator it = _list.iterator(); it.hasNext();) {
                final RecordAverageChairDat data = (RecordAverageChairDat) it.next();

                // search
                if (!data._subClassCd.equals(subClassCd)) {
                    continue;
                }
                if (!data._chairCd.equals(chairCd)) {
                    continue;
                }
                if (data._avgDiv.equals(M_GRADE) && data._hrClass.equals("000")) {  // 組は"000"固定
                    return data;
                }
            }
            return null;
        }

        public synchronized static void load(final DB2UDB db2, final Param param) {
            _list.clear();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlAverageDat(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer count = KNJServletUtils.getInteger(rs, "COUNT");
                    final BigDecimal avg0 = rs.getBigDecimal("AVG");
                    final BigDecimal avg = (null == avg0) ? null : avg0.setScale(1, BigDecimal.ROUND_HALF_UP);
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final String chairCd = rs.getString("CHAIRCD");
                    final String avgDiv = rs.getString("AVG_DIV");
                    final String hrClass = rs.getString("HR_CLASS");
                    final RecordAverageChairDat avgDat = new RecordAverageChairDat(
                            count.intValue(),
                            avg,
                            subClassCd,
                            chairCd,
                            avgDiv,
                            hrClass
                    );
                    _list.add(avgDat);
                }
            } catch (final Exception ex) {
                log.error("ロードでエラー", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("成績平均データの件数=" + _list.size());
        }
        
        private static String sqlAverageDat(final Param param) {
            final String rtn;
            rtn = "SELECT"
                + "    count,"
                + "    avg,"
                + "    subclasscd,"
                + "    chaircd,"
                + "    avg_div,"
                + "    hr_class"
                + " FROM"
                + "    record_average_chair_dat"
                + " WHERE year = '" + param._year + "'"
                + " AND semester = '" + param._semester + "'"
                + " AND testkindcd = '" + param._testKind + "'"
                + " AND testitemcd = '" + param._testItem + "'"
                + " AND avg_div in ('1', '2')"  // 1:学年 2:クラス 3:コース
                + " AND grade = '" + param._grade + "'"
                + " AND coursecd = '0'"
                + " AND majorcd = '000'"
                + " AND coursecode = '0000'"
                ;
            log.debug("成績平均データのsql=" + rtn);
            return rtn;
        }

        public String toString() {
            return "講座成績平均データ(講座:人数:平均)=" + _chairCd + ":" + _count + ":" + _avg;
        }

    } // RecordAverageChairDat

    private static class RecordRankDat implements Comparable {
        final static Map _scoreMap = new HashMap();   // <HomeRoom, List>

        static int _gradeMax;
        static int _gradeMin;
        static int _gradeLessThan;

        /* pkg */final String _schregno;
        /* pkg */final Integer _score;

        static {
            clear();
        }

        /* pkg */RecordRankDat(final String schregno, final Integer score) {
            _schregno = schregno;
            _score = score;
        }

        public static String getGradeMax() {
            if (_gradeMax == Integer.MIN_VALUE) {
                return "";
            }
            return String.valueOf(_gradeMax);
        }

        public static String getGradeMin() {
            if (_gradeMin == Integer.MAX_VALUE) {
                return "";
            }
            return String.valueOf(_gradeMin);
        }

        /**
         * 分配する。
         * @param hr 年組
         * @param radix 分布の最大数
         * @return 分配した結果
         */
        public static int[] distribute(final HomeRoom hr, final int radix) {
            // TODO: 範囲は仕様書に明記されている。
            /*
             * [0] = 0 〜 10
             * [1] = 11 〜 20
             * [9] = 99 〜 100
             */
            final int[] hoge = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            final List list = (List) _scoreMap.get(hr);
            if (null == list) {
                return null;
            }
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final RecordRankDat rankDat = (RecordRankDat) it.next();
                if (null != rankDat._score) {
                    final int idx = whereIndexIs(rankDat._score.intValue(), radix);
                    hoge[idx]++;
                }
            }
            return hoge;
        }

        // TODO: KNJD104Dでも使用。KNJDServletUtils で共有せよ
        /* pkg */static int whereIndexIs(final int value, final int radix) {
            // マイナスはゼロ番目
            if (value < 0) {
                return 0;
            }
            // 最高点以上は9番目
            if (radix <= value) {
                return 9;
            }
            // 算出
            return value / (radix / 10);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof RecordRankDat)) {
                return -1;
            }
            final RecordRankDat that = (RecordRankDat) o;
            return this._score.compareTo(that._score);
        }

        public String toString() {
            return _schregno + ":" + _score;
        }

        /**
         * n点未満の数
         * @param n 点数
         * @param hr 年組
         * @return n点未満の数
         */
        public static String lessThan(final int n, final HomeRoom hr) {
            final List list = (List) _scoreMap.get(hr);
            if (null == list) {
                return null;
            }
            log.info("list=" + list);

            final Collection filterd = CollectionUtils.select(list, new Predicate() {
                public boolean evaluate(final Object object) {
                    if (!(object instanceof RecordRankDat)) {
                        return false;
                    }
                    final RecordRankDat rankDat = (RecordRankDat) object;
                    return rankDat._score.intValue() < n;
                }
            });
            if (null == filterd) {
                return null;
            }
            return String.valueOf(filterd.size());
        }

        public static MaxMin getMaxMin(final HomeRoom hr) {
            final List list = (List) _scoreMap.get(hr);
            if (null == list) {
                return null;
            }
            final RecordRankDat max = (RecordRankDat) Collections.max(list);
            final RecordRankDat min = (RecordRankDat) Collections.min(list);
            return new MaxMin(max._score, min._score);
        }

        synchronized public static void load(final DB2UDB db2, final Param param, final SubClass subClass, final List homeRooms) {
            clear();

            for (final Iterator it = homeRooms.iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                final List list = loadRRD(db2, param, subClass, hr);
                log.debug(hr + "のrecord_rank_datの数=" + list.size());
                if (list.size() > 0) {
                    _scoreMap.put(hr, list);
                }
            }
        }

        synchronized private static void clear() {
            _scoreMap.clear();
            _gradeMax = Integer.MIN_VALUE;
            _gradeMin = Integer.MAX_VALUE;
            _gradeLessThan = 0;
        }

        private static List loadRRD(
                final DB2UDB db2,
                final Param param,
                final SubClass subClass,
                final HomeRoom hr
        ) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlRankDat(param, subClass, hr));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final Integer score = KNJServletUtils.getInteger(rs, "SCORE");

                    setGradeInfo(score);

                    final RecordRankDat rankDat = new RecordRankDat(schregno, score);
                    rtn.add(rankDat);
                }
            } catch (final Exception ex) {
                log.error("record_rank_datの生成で失敗:" + subClass + ", " + hr, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static void setGradeInfo(final Integer score) {
            if (null == score) {
                return;
            }

            final int intValue = score.intValue();

            if (_gradeMax < intValue) {
                _gradeMax = intValue;
            }

            if (_gradeMin > intValue) {
                _gradeMin = intValue;
            }

            if (intValue < _LESS_THAN_POINT) {
                _gradeLessThan++;
            }
        }

        private static String sqlRankDat(final Param param, final SubClass subClass, final HomeRoom hr) {
            final String rtn;
            rtn = "SELECT"
                + "    schregno,"
                + "    score"
                + " FROM"
                + "    record_rank_dat"
                + " WHERE year = '" + param._year + "'"
                + " AND semester = '" + param._semester + "'"
                + " AND testkindcd = '" + param._testKind + "'"
                + " AND testitemcd = '" + param._testItem + "'"
                + " AND subclasscd = '" + subClass._code + "'"
                + " AND schregno in (" + sqlHrStudent(param, hr) + ")"
                ;
            return rtn;
        }

        private static String sqlHrStudent(final Param param, final HomeRoom hr) {
            final String rtn;
            rtn = "SELECT"
                + "    schregno"
                + " FROM"
                + "    schreg_regd_dat"
                + " WHERE year = '" + param._year + "'"
                + " AND semester = '" + param._semester + "'"
                + " AND grade = '" + hr._grade + "'"
                + " AND hr_class = '" + hr._room + "'"
                ;
            return rtn;
        }
    } // RecordRankDat

    private static class RecordRankChairDat extends RecordRankDat {
        final static Map _scoreMap = new HashMap();   // <Chair, List>

        static int _gradeMax;
        static int _gradeMin;
        static int _gradeLessThan;

        private RecordRankChairDat(final String schregno, final Integer score) {
            super(schregno, score);
        }

        public static MaxMin getMaxMin(final Chair chair) {
            final List list = (List) _scoreMap.get(chair);
            if (null == list) {
                return null;
            }
            final RecordRankChairDat max = (RecordRankChairDat) Collections.max(list);  // 上位クラスに任せる
            final RecordRankChairDat min = (RecordRankChairDat) Collections.min(list);  // 上位クラスに任せる
            return new MaxMin(max._score, min._score);
        }

        /**
         * 分配する。
         * @param chair 講座
         * @param radix 分布の最大数
         * @return 分配した結果
         */
        public static int[] distribute(final Chair chair, final int radix) {
            // TODO: 範囲は仕様書に明記されている。
            /*
             * [0] = 0 〜 10
             * [1] = 11 〜 20
             * [9] = 99 〜 100
             */
            final int[] hoge = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            final List list = (List) _scoreMap.get(chair);
            if (null == list) {
                return null;
            }
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final RecordRankChairDat rankDat = (RecordRankChairDat) it.next();
                if (null != rankDat._score) {
                    final int idx = whereIndexIs(rankDat._score.intValue(), radix);
                    hoge[idx]++;
                }
            }
            return hoge;
        }

        /**
         * n点未満の数
         * @param n 点数
         * @param chair 講座
         * @return n点未満の数
         */
        public static String lessThan(final int n, final Chair chair) {
            final List list = (List) _scoreMap.get(chair);
            if (null == list) {
                return null;
            }

            final Collection filterd = CollectionUtils.select(list, new Predicate() {
                public boolean evaluate(final Object object) {
                    if (!(object instanceof RecordRankChairDat)) {
                        return false;
                    }
                    final RecordRankChairDat rankDat = (RecordRankChairDat) object;
                    return rankDat._score.intValue() < n;
                }
            });
            if (null == filterd) {
                return null;
            }
            return String.valueOf(filterd.size());
        }

        synchronized public static void load(
                final DB2UDB db2,
                final Param param,
                final SubClass subClass
        ) {
            clear();

            for (final Iterator it = subClass._chairs.values().iterator(); it.hasNext();) {
                final Chair chair = (Chair) it.next();
                final List list = loadRRD(db2, param, subClass, chair);
                if (list.size() > 0) {
                    _scoreMap.put(chair, list);
                }
                if (list.size() < 100) {
                    log.debug(chair + "のrank_chair_datの数=" + list.size() + ", 詳細=" + list);
                } else {
                    log.debug(chair + "のrank_chair_datの数=" + list.size());
                }
            }
        }

        synchronized private static void clear() {
            _scoreMap.clear();
            _gradeMax = Integer.MIN_VALUE;
            _gradeMin = Integer.MAX_VALUE;
            _gradeLessThan = 0;
        }

        private static List loadRRD(
                final DB2UDB db2,
                final Param param,
                final SubClass subClass,
                final Chair chair
        ) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlRankChairDat(param, subClass, chair));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final Integer score = KNJServletUtils.getInteger(rs, "SCORE");

                    setGradeInfo(score);

                    final RecordRankChairDat rankDat = new RecordRankChairDat(schregno, score);
                    rtn.add(rankDat);
                }
            } catch (final Exception ex) {
                log.error("record_rank_chair_datの生成で失敗:" + subClass + ", " + chair, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static void setGradeInfo(final Integer score) {
            if (null == score) {
                return;
            }

            final int intValue = score.intValue();

            if (_gradeMax < intValue) {
                _gradeMax = intValue;
            }

            if (_gradeMin > intValue) {
                _gradeMin = intValue;
            }

            if (intValue < _LESS_THAN_POINT) {
                _gradeLessThan++;
            }
        }

        private static String sqlRankChairDat(
                final Param param,
                final SubClass subClass,
                final Chair chair
        ) {
            final String rtn;
            rtn = "SELECT"
                + "    schregno,"
                + "    score"
                + " FROM"
                + "    record_rank_chair_dat"
                + " WHERE year = '" + param._year + "'"
                + " AND semester = '" + param._semester + "'"
                + " AND testkindcd = '" + param._testKind + "'"
                + " AND testitemcd = '" + param._testItem + "'"
                + " AND subclasscd = '" + subClass._code + "'"
                + " AND chaircd = '" + chair._code + "'";
                ;
            return rtn;
        }

    } // RecordRankChairDat
    private static class MaxMin {
        private final Integer _max;
        private final Integer _min;
        public MaxMin(final Integer max, final Integer min) {
            _max = max;
            _min = min;
        }

        public String getMax() {
            return null == _max ? "" : _max.toString();
        }

        public String getMin() {
            return null == _min ? "" : _min.toString();
        }

        public String toString() {
            return "(" + _max + "," + _min + ")";
        }
    } // MaxMin

    private interface Hoge {
        String getKey();
        String getAbbv();
    }
} // KNJD623

// eof
