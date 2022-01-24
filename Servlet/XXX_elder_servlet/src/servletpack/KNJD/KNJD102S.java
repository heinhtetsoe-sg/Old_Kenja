// kanji=漢字
/*
 * $Id: 198bba12d231856eb3788b2e65beffcfcda28457 $
 *
 * 作成日: 2007/07/10 16:24:10 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.ui.RectangleEdge;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

import jp.co.alp.kenja.common.dao.SQLUtils;

/**
 * 成績個人票。A3版。RECORD_RANK_DAT版。<br>
 * KNJD102B.java の Revision 1.276のコピペPG
 * @author takaesu
 * @version $Id: 198bba12d231856eb3788b2e65beffcfcda28457 $
 */
public class KNJD102S {
    /**
     * 定期考査表の1教科における最大科目数。
     */
    private static final int _TEIKIKOUSA_TABLE_KAMOKU_MAX = 3;

    /*pkg*/static final Log log = LogFactory.getLog(KNJD102S.class);

    private static final String FORM_FILE = "KNJD102S.frm";
    private static final BasicStroke _outlineStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final Font _font = new Font("TimesRoman", Font.PLAIN, 13);
    private static final Font _smallFont = new Font("TimesRoman", Font.PLAIN, 11);

    private static final String LATE_CODE = "15";
    private static final String EARLY_CODE = "16";
    /**
     * グラフイメージファイルの Set&lt;File&gt;
     */
    private final Set _graphFiles = new HashSet();

    private Form _form;

    private Param _param;

    /** 開始学期. */
    private static final String SSEMESTER = "1";

    private static final String _999999 = "999999";

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch (final Exception ex) {
                log.error("db open error!", ex);
                return;
            }

            _param.load(db2);

            List mockDatList = Collections.EMPTY_LIST;
            if (!_param._disableMosi) {
                mockDatList = MockDat.loadMock(db2, _param);
                log.debug("mock_dat size=" + mockDatList.size());
            }

            final List students = Student.createStudents(db2, _param);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug("------ 生徒:" + student + " ----:" + student.attend());

                final boolean enableStudent = printSvfAt(db2, mockDatList, student);
                if (enableStudent) {
                    _form._hasData = true;
                }
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            // 終了処理
            _form.closeSvf();
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            removeImageFiles();
        }
    }

    private boolean printSvfAt(
            final DB2UDB db2,
            final List mockDatList,
            final Student student
    ) throws SQLException {
        final StudentChooser studentChooser = new StudentChooser(student._schregno);
        final List studentRecords = new ArrayList(CollectionUtils.select(_param._recordset._records.values(), studentChooser));
        log.info("生徒のRECORD_DAT size=" + studentRecords.size());
        Collections.sort(studentRecords);

        // ヘッダー
        // 年度、学期、試験名称
        _form.svfOut("NENDO", KenjaProperties.gengou(Integer.valueOf(_param._year).intValue()) + "年度");
        _form.svfOut("SEMESTER", _param._semesterName);
        _form.svfOut("TEST", _param.getCurrentExam()._name);

        // 生徒氏名
        _form.svfOut("HR_NAME", student.getNenkumiban());   // 年組番
        _form.svfOut("NAME", student._name);

        // 学級担任
        _form.svfOut("STAFFNAME", student._staffName);

        // 分布票
        printBunpu(db2, studentRecords, student);

        // 出欠情報
        _form.svfOut("ABSENCE", String.valueOf(student._attend));   // 欠席
        _form.svfOut("LATE", String.valueOf(student._late));    // 遅刻
        _form.svfOut("LEAVE", String.valueOf(student._early));  // 早退

        // 印刷日時
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.M.d H:m");
        _form.svfOut("P_DATE", sdf.format(_param._now.getTime()));

        // 定期考査
        printTeikikousa(studentRecords);

        // 模試
        if (!_param._disableMosi) {
            printMosi(mockDatList, student);
        }

        // 連絡欄
        if (null != student._remark) {
            KNJServletUtils.printDetail(_form._svf, "REMARK", student._remark, 30 * 2, 5);
        }

        _form.endPage();
        return true;
    }

    private void printMosi(final List mockDatList, final Student student) {
        log.debug("=== 模試 ===");
        final DefaultCategoryDataset dataset = createMockDataset(mockDatList, student);
        if (dataset.getColumnCount() == 0 && dataset.getRowCount() == 0) {
            return;
        }

        // グラフ
        final MosiChart mosiChart = new MosiChart(dataset);
        final File mosiFile = graphImageFile(mosiChart._chart, 1664, 1366);
        _graphFiles.add(mosiFile);
        _form.svfOut("CHART2", mosiFile.toString());

        // 表
        int i = 1;
        for (final Iterator it = dataset.getRowKeys().iterator(); it.hasNext();) {
            final String kamoku = (String) it.next();
            _form.svfOut("SUBCLASSABBV2_" + i, kamoku);   // 科目
            i++;
        }

        int idx = 1;
        for (final Iterator it = dataset.getColumnKeys().iterator(); it.hasNext();) {
            final String mosi = (String) it.next();
            final String wrk = mosi.length() > 3 ? mosi.substring(0, 3) : mosi;
            _form.svfOutn("MOCK", idx, wrk);   // 模試

            for (final Iterator it2 = dataset.getRowKeys().iterator(); it2.hasNext();) {
                final String kamoku = (String) it2.next();

                final Number value;
                try {
                    value = dataset.getValue(kamoku, mosi);
                } catch (final UnknownKeyException e) {
                    continue;
                }

                final int kamokuIdx = dataset.getRowKeys().indexOf(kamoku);
                if (-1 == kamokuIdx) {
                    continue;
                }
                _form.svfOutn("POINT2_" + (kamokuIdx + 1), idx, value.toString());    // データ
                log.info("row=" + kamoku + ", col=" + mosi + ", val=" + value);
            }

            idx++;
        }
    }

    private void printTeikikousa(final Collection studentRecords) {
        log.debug("=== 定期考査 ===");

        printTeikikousaTitle(_param._charts, studentRecords);

        for (final Iterator it = studentRecords.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();

            final Clazz clazz = _param.getClazz(record._subClass);
            if (null == clazz) {
                continue;
            }
            // 教科から、対応したチャートを得る。
            for (int i = 0; i < _param._charts.length; i++) {
                if (null == _param._charts[i]) {
                    continue;
                }
                final TeikikousaChart teikikousaChart = _param._charts[i];
                if (teikikousaChart.hasClazz(clazz)) {
                    teikikousaChart.add(_param, record);
                    break;
                }
            }
        }

        int subClassIdx = 1;
        for (int i = 0; i < _param._charts.length; i++) {
            if (null == _param._charts[i]) {
                continue;
            }
            final TeikikousaChart teikikousaChart = _param._charts[i];
            if (!teikikousaChart.hasData()) {
                subClassIdx += _TEIKIKOUSA_TABLE_KAMOKU_MAX;
                continue;
            }
            teikikousaChart.commit();

            final File file = graphImageFile(teikikousaChart._chart, 990, 890);
            _graphFiles.add(file);
            _form.svfOut("CHART1_" + (i + 1), file.toString());


            subClassIdx = teikikousaChart.printTable(_form, _param, subClassIdx);
            teikikousaChart.reset();
        }
    }

    private void printTeikikousaTitle(final TeikikousaChart[] charts, final Collection studentsRecords) {
        final Map nameMap = new HashMap();
        for (final Iterator it = studentsRecords.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();

            final Clazz clazz = _param.getClazz(record._subClass);
            if (null == clazz) {
                continue;
            }
            // 教科から、対応したチャートを得る。
            for (int i = 0; i < _param._charts.length; i++) {
                if (null == _param._charts[i]) {
                    continue;
                }
                final TeikikousaChart teikikousaChart = _param._charts[i];
                if (teikikousaChart.hasClazz(clazz)) {
                    teikikousaChart.add(_param, record);
                    nameMap.put(new Integer(i), teikikousaChart.getName(clazz._cd));
                    break;
                }
            }
        }

        // 教科名
        for (int i = 0; i < charts.length; i++) {
            if (null == charts[i]) {
                continue;
            }
            final String name = (String) nameMap.get(new Integer(i));
            _form.svfOut("CLASSABBV1_1_" + (i + 1), name);   // グラフのタイトル
            _form.svfOutn("CLASSABBV1_2", (i + 1), name);   // 偏差値一覧表の教科タイトル
        }

        // 表の列タイトル
        int i = 1;
        for (final Iterator it = _param._examMaster.values().iterator(); it.hasNext();) {
            final Exam exam = (Exam) it.next();
            if ("99".equals(exam._kindCd)) {
                continue;
            }
            _form.svfOut("STDDIV1_" + i, exam._name);
            i++;
        }
    }

    private void printBunpu(
            final DB2UDB db2,
            final Collection studentRecords,
            final Student student
    ) throws SQLException {
        log.debug("=== 分布 ===");
        // 行タイトル
        _form.bunpuReset();

        final String mode1Str = "1".equals(_param._mode1) ? "学年" : "コース";
        for (int i = 1; i <= 2; i++) {
            _form.svfOut("ITEM" + i + "_1", "得点");
            _form.svfOut("ITEM" + i + "_2", "欠課数");
            _form.svfOut("ITEM" + i + "_3", "講座平均");
            _form.svfOut("ITEM" + i + "_4", mode1Str + "平均");
            if (!"3".equals(_param._outputKijun)) {
                _form.svfOut("ITEM" + i + "_5", "講座順位");
            }
            _form.svfOut("ITEM" + i + "_6", mode1Str + "順位");
        }

        _form.svfOutn("SUBCLASS2_2", 8, "総合成績");

        // 分布票データ
        int i = 1;
        final BunpuChart bunpuChart = new BunpuChart();

        final Collection subClasses = SubClass.loadSubClasses(db2, student, _param, _param._subClassMst);
        log.debug("印字する科目=" + subClasses);
        log.debug("RECORD_DATの中身=" + studentRecords);

        final Exam currentExam = _param.getCurrentExam();
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            final Record record = Record.getRecord(studentRecords, subClass);
            if (null == record) {
                continue;
            }

            final ScoreInfo chairInfo = _param._recordset.createChairInfo(record);
            final ScoreInfo gradeInfo = _param._recordset.createAvgDatInfo(_param._recordset._avgGradeDat, subClass._cd, student, currentExam);
            final ScoreInfo courseInfo = _param._recordset.createAvgDatInfoCourse(_param._recordset._avgCourseDat, subClass._cd, student, currentExam);
            final RankDat rankDat = _param._recordset.getRankDat(student._schregno, subClass._cd, currentExam);

            if (null != record && null != record._currentScore) {
                log.info("record=" + record + "/" + record._subClass._chairCd);

                // グラフデータ作成
                final ScoreInfo graphInfo;
                if ("1".equals(_param._mode2)) {
                    graphInfo = chairInfo;
                } else if ("2".equals(_param._mode2)) {
                    graphInfo = gradeInfo;
                } else {
                    graphInfo = courseInfo;
                }
                if (graphInfo.size() != 0) {
                    bunpuChart.add(record, graphInfo);
                }
            }

            //
            final Integer credit = CreditMst.getCredit(_param._creditMst, student, subClass);

            // 
            ScoreInfo otherInfo = null; // 学年 or コース
            if (null != record) {
                if ("1".equals(_param._mode1)) {
                    otherInfo = gradeInfo;
                } else {
                    otherInfo = courseInfo;
                }
            }

            // 印字
            final Integer kekka = (Integer) student._kekkaMap.get(subClass._cd);
            _form.printColumnData(subClass, record, credit, kekka, chairInfo, otherInfo, rankDat);

            if (i++ >= Form.MAX_SUBCLASS_COUNT) {
                break;
            }
        }
        bunpuChart.commit();

        // 総合成績
        final ScoreInfo gradeInfo = _param._recordset.createAvgDatInfo(_param._recordset._avgGradeDat, _999999, student, currentExam);
        log.debug(" gradeInfo = " + gradeInfo);
        final ScoreInfo courseInfo = _param._recordset.createAvgDatInfoCourse(_param._recordset._avgCourseDat, _999999, student, currentExam);
        log.debug("courseInfo = " + courseInfo);
        final RankDat rankDat999999 = _param._recordset.getRankDat(student._schregno, _999999, currentExam);

        ScoreInfo otherInfo = null; // 学年 or コース
        if ("1".equals(_param._mode1)) {
            otherInfo = gradeInfo;
        } else {
            otherInfo = courseInfo;
        }
        _form.printTotalScore(student, otherInfo, rankDat999999);

        // 素点度数分布票グラフ
        printBunpuGraph(bunpuChart);
    }

    private void printBunpuGraph(final BunpuChart bunpuChart) {
        _form.svfOut("DISTRI_TYPE", ("1".equals(_param._mode2)) ? "(講座別)" : ("2".equals(_param._mode2)) ? "(学年別)" : "(コース別)");

        final File bunpuFile = graphImageFile(bunpuChart._chart, 3010, 1368);
        _graphFiles.add(bunpuFile);

        _form.svfOut("FRE_CHART", bunpuFile.toString());
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

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
        final Param param = new Param(request);
        return param;
    }

    private DefaultCategoryDataset createMockDataset(final List mockDatList, final Student student) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        final List studentMock = MockDat.getStudentMock(mockDatList, student._schregno);
        log.debug("生徒の模試データ=" + studentMock);
        for (final Iterator it = studentMock.iterator(); it.hasNext();) {
            final MockDat mockDat = (MockDat) it.next();

            final Number value;
            if (true) {
                value = mockDat._deviation; // 偏差値を得る
            } else {
                value = mockDat._score; // 得点を得る。（デバッグ用）
            }
            if (null == value) {
                continue;
            }
            dataset.addValue(value, mockDat._subClassAbbv, mockDat._mockName);
        }

        return dataset;
    }

    /* pkg */File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
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

    // ======================================================================

    private class Form {
        /** 1段目の表の最大科目数 */
        private final static int FIRST_MAX_COL = 9;

        /** 2段目の表の最大科目数 */
        private final static int SECOND_MAX_COL = 7;

        /**
         * 印字可能科目数。
         */
        public final static int MAX_SUBCLASS_COUNT = FIRST_MAX_COL + SECOND_MAX_COL;

        private int _subClassIndex;
        private int _dan;

        private int _totalCredit;
        private int _totalKekka;

        private final String _file;
        private Vrw32alp _svf;

        private boolean _hasData;

        public Form(final String file, final HttpServletResponse response) throws IOException {
            _file = file;
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(file, 1);
            bunpuReset();
        }

        public void bunpuReset() {
            _subClassIndex = 0;
            _dan = 1;

            _totalCredit = _totalKekka = 0;
        }

        public void printColumnData(
                final SubClass subClass,
                final Record record,
                final Integer credit,
                final Integer kekka,
                final ScoreInfo chairInfo,
                final ScoreInfo otherInfo,
                final RankDat rankDat
        ) {
            _subClassIndex++;
            if (_subClassIndex > FIRST_MAX_COL) {
                _subClassIndex = 1;
                _dan = 2;
            }
            final int subClassIndex = _subClassIndex;
            
            // 科目名
            if (null != subClass._abbv) {
                svfOutn("SUBCLASS" + _dan + (subClass._abbv.length() <= 3 ? "_1" : "_2"), subClassIndex, subClass._abbv); // 科目名
            }

            // 単位数
            if (null != credit) {
                svfOutn("CREDIT" + _dan, _subClassIndex, credit.toString());
            }

            // 欠課数
            if (null != kekka) {
                svfOutn("KEKKA" + _dan, _subClassIndex, kekka.toString());
            }

            // 得点
            if (null != record) {
                if (null != record._currentScore) {
                    svfOutn("POINT" + _dan, _subClassIndex, record._currentScore.toString());
                }

                // 講座平均、講座順位/総数
                if (null != chairInfo && chairInfo.size() != 0) {
                    printChairData(record, chairInfo);
                }

                // xx平均、xx順位/総数
                if (null != otherInfo && otherInfo.size() != 0) {
                    printOtherData(record, otherInfo, rankDat);
                }
            }

            // 総合成績の算出
            if (null != credit) {
                _totalCredit += credit.intValue();
            }
            if (null != kekka) {
                _totalKekka += kekka.intValue();
            }
        }

        private void printOtherData(final Record record, final ScoreInfo otherInfo, final RankDat rankDat) {
            // xx平均
            final BigDecimal avg = otherInfo.getAvg().setScale(1, BigDecimal.ROUND_HALF_UP);
            svfOutn("AVERAGE" + _dan + "_2", _subClassIndex, avg.toString());

            // xx順位/総数
            final Integer score = record._currentScore;
            if (null != score) {
                String rank = "";
                if (null != rankDat) {
                    Integer iRank = "1".equals(_param._mode1) ? rankDat._gradeRank : rankDat._courseRank;
                    rank = null == iRank ? rank : iRank.toString();
                }
                
                final String rankStr = rank + "/" + otherInfo.size();
                svfOutn("RANK" + _dan + "_2", _subClassIndex, rankStr);
            }
        }

        private void printChairData(final Record record, final ScoreInfo chairInfo) {
            // 講座平均
            final BigDecimal avg = chairInfo.getAvg().setScale(1, BigDecimal.ROUND_HALF_UP);
            svfOutn("AVERAGE" + _dan + "_1", _subClassIndex, avg.toString());

            // 講座順位/総数
            if (!"3".equals(_param._outputKijun)) {
                if (null != record._rank) {
                    svfOutn("RANK" + _dan + "_1", _subClassIndex, record._rank + "/" + chairInfo._size);
                }
            }
        }

        /**
         * 総合成績
         */
        public void printTotalScore(final Student student, final ScoreInfo otherInfo, final RankDat rankDat999999) {
            svfOutn("CREDIT2" , 8, String.valueOf(_totalCredit));   // 単位数

            if (null != rankDat999999) {
                svfOutn("POINT2", 8, rankDat999999._score.toString()); // 得点
            }
            
            svfOutn("KEKKA2", 8, String.valueOf(_totalKekka));  // 欠課数
            
            // 得点
            // xx平均、xx順位/総数
            if (null != otherInfo && otherInfo.size() != 0) {
                // xx平均
                final BigDecimal avg = otherInfo.getAvg().setScale(1, BigDecimal.ROUND_HALF_UP);
                svfOutn("AVERAGE2_2", 8, avg.toString());

                // xx順位/総数
                String rank = "";
                if (null != rankDat999999) {
                    Integer iRank = "1".equals(_param._mode1) ? rankDat999999._gradeRank : rankDat999999._courseRank;
                    rank = null == iRank ? rank : iRank.toString();
                }
                
                svfOutn("RANK2_2", 8, rank + "/" + otherInfo.size());
            }
        }

        public void svfOutn(final String field, final int gyo, final String data) {
            _svf.VrsOutn(field, gyo, data);
        }

        public int svfOut(String field, String abbr) {
            return _svf.VrsOut(field, abbr);
        }

        public int endPage() {
            return _svf.VrEndPage();
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    // ======================================================================

    private static class Exam implements Comparable {
        private final String _semester;
        private final String _kindCd;
        private final String _itemCd;
        private final String _name;

        public Exam(final String semester, final String kindCd, final String itemCd, final String name) {
            _semester = semester;
            _kindCd = kindCd;
            _itemCd = itemCd;
            _name = name;

            if (null == _semester) {
                throw new IllegalArgumentException("semester が null");
            }
            if (null == _kindCd) {
                throw new IllegalArgumentException("kindCd が null");
            }
            if (null == _itemCd) {
                throw new IllegalArgumentException("itemCd が null");
            }
        }

        public boolean isGakunenSeiseki() {
            return "9".equals(_semester) && "9900".equals(_kindCd + _itemCd);
        }

        public String toString() {
            return _name;
        }

        public String getKey() {
            return _semester + _kindCd + _itemCd;
        }
        public boolean equals(final Object obj) {
            if (obj instanceof Exam) {
                final Exam that = (Exam) obj;
                if (!this._semester.equals(that._semester)) {
                    return false;
                }
                return this._kindCd.equals(that._kindCd) && this._itemCd.equals(that._itemCd);
            }
            return false;
        }

        public int hashCode() {
            return getKey().hashCode();
        }

        public int compareTo(Object o) {
            if (!(o instanceof Exam)) {
                return -1;
            }
            final Exam that = (Exam) o;

            if (!this._semester.equals(that._semester)) {
                return this._semester.compareTo(that._semester);
            }
            if (!this._kindCd.equals(that._kindCd)) {
                return this._kindCd.compareTo(that._kindCd);
            }
            return this._itemCd.compareTo(that._itemCd);
        }
        
        private static Map loadExam(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                ps = db2.prepareStatement(sqlExam(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String kindCd = rs.getString("TESTKINDCD");
                    final String itemCd = rs.getString("TESTITEMCD");
                    final String testName = rs.getString("TESTITEMNAME");
                    final Exam exam = new Exam(semester, kindCd, itemCd, testName);
                    log.debug("テスト項目=" + exam + ":" + exam._name);

                    map.put(semester + kindCd + itemCd, exam);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private static String sqlExam(final Param param) {
            final String sql;
            sql = "select"
                    + "    SEMESTER,"
                    + "    TESTKINDCD,"
                    + "    TESTITEMCD,"
                    + "    TESTITEMNAME"
                    + "  from"
                    + "    TESTITEM_MST_COUNTFLG_NEW"
                    + "  where"
                    + "    YEAR='" + param._year + "' "
                    + "  order by"
                    + "    SEMESTER, TESTKINDCD, TESTITEMCD"
            ;
            return sql;
        }
    }

    // ======================================================================
    private static class Student {
        private final String _schregno;
        private final String _grade;
        private final String _hrclass;
        private final String _attendno;
        private final String _name;
        /** 組略称。 */
        private final String _hrNameAbbv;
        private final String _staffName;

        private final Course _course;

        public int _attend;
        public int _early;
        public int _late;

        /** 連絡欄. */
        private String _remark;

        /** 科目毎の欠課数. */
        private final Map _kekkaMap = new HashMap();

        Student(
                final String schregno,
                final String grade,
                final String hrclass,
                final String attendno,
                final String name,
                final String hrNameAbbv,
                final String staffName,
                final String courseCd,
                final String majorCd,
                final String courseCode

        ) {
            _schregno = schregno;
            _grade = grade;
            _hrclass = hrclass;
            _attendno = attendno;
            _name = name;
            _hrNameAbbv = hrNameAbbv;
            _staffName = staffName;

            _course = new Course(courseCd, majorCd, courseCode);
        }

        public void loadKekka(final DB2UDB db2, final Param param) throws SQLException {
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    param._semesFlg,
                    null,
                    param._defineSchool,
                    param._knjSchoolMst,
                    param._year,
                    SSEMESTER,
                    param._semester,
                    (String) param._hasuuMap.get("attendSemesInState"),
                    param._periodInState,
                    (String) param._hasuuMap.get("befDayFrom"),
                    (String) param._hasuuMap.get("befDayTo"),
                    (String) param._hasuuMap.get("aftDayFrom"),
                    (String) param._hasuuMap.get("aftDayTo"),
                    _grade,
                    _hrclass,
                    _schregno,
                    "1",
                    param._useCurriculumcd,
                    param._useVirus,
                    param._useKoudome
            );
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                if (!"9".equals(semester)) {
                    continue;
                }
                final String subclassCd = rs.getString("SUBCLASSCD");
                final Number kekka = (Number) rs.getObject("ABSENT_SEM");
                _kekkaMap.put(subclassCd, new Integer(kekka.intValue()));
            }
        }

        public String getNenkumiban() {
            final Integer attendNo = Integer.valueOf(_attendno);
            return _hrNameAbbv + " " + attendNo + "番";
        }

        public String attend() {
            return "欠席=" + _attend + ":遅刻=" + _late + ":早退=" + _early;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
        
        private static List createStudents(final DB2UDB db2, final Param param) throws SQLException {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStudents(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String grade = rs.getString("grade");
                    final String hrclass = rs.getString("hrclass");
                    final String attendno = rs.getString("attendno");
                    final String name = rs.getString("name");
                    final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                    final String staffName = rs.getString("STAFFNAME");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");

                    final Student student = new Student(
                            schregno,
                            grade,
                            hrclass,
                            attendno,
                            name,
                            hrNameAbbv,
                            staffName,
                            courseCd,
                            majorCd,
                            courseCode
                    );

                    rtn.add(student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            for (final Iterator it = rtn.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                loadAttendSemes(db2, student, param);
                loadHexamRecordRemark(db2, student, param);
                student.loadKekka(db2, param);
            }

            return rtn;
        }

        private static void loadAttendSemes(final DB2UDB db2, final Student student, final Param param) throws SQLException {
            final String sql = AttendAccumulate.getAttendSemesSql(
                    param._semesFlg,
                    null,
                    param._knjSchoolMst,
                    param._year,
                    SSEMESTER,
                    param._semester,
                    (String) param._hasuuMap.get("attendSemesInState"),
                    param._periodInState,
                    (String) param._hasuuMap.get("befDayFrom"),
                    (String) param._hasuuMap.get("befDayTo"),
                    (String) param._hasuuMap.get("aftDayFrom"),
                    (String) param._hasuuMap.get("aftDayTo"),
                    student._grade,
                    student._hrclass,
                    student._schregno,
                    "SEMESTER",
                    param._useCurriculumcd,
                    param._useVirus,
                    param._useKoudome
            );
            log.debug("出欠情報のSQL=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                if ("9".equals(semester)) {
                    continue;
                }
                final Integer sick = KNJServletUtils.getInteger(rs, "SICK");
                final Integer late = KNJServletUtils.getInteger(rs, "LATE");
                final Integer early = KNJServletUtils.getInteger(rs, "EARLY");
                log.debug(semester + "学期の欠席/遅刻/早退=" + sick + "/" + late + "/" + early);

                student._attend += sick.intValue();
                student._late += late.intValue();
                student._early += early.intValue();
            }
        }

        private static void loadHexamRecordRemark(final DB2UDB db2, final Student student, final Param param) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlHexamRecordRemark(student, param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String remark = rs.getString("REMARK1");
                    student._remark = remark;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private static String sqlHexamRecordRemark(final Student student, final Param param) {
            final String rtn;
            final String semester = "9".equals(param._semester) ? param._ctrlSemester : param._semester;
            rtn = " select"
                    + "  REMARK1"
                    + " from"
                    + "  HEXAM_RECORD_REMARK_DAT"
                    + " where"
                    + "  YEAR = '" + param._year + "' and"
                    + "  SEMESTER = '" + semester + "' and"
                    + "  TESTKINDCD = '" + param.getCurrentExam()._kindCd + "' and"
                    + "  TESTITEMCD = '" + param.getCurrentExam()._itemCd + "' and"
                    + "  SCHREGNO = '" + student._schregno + "' and"
                    + "  REMARK_DIV='1'"
                    ;
            return rtn;
        }
        
        private static String sqlStudents(final Param _param) {
            final String students = SQLUtils.whereIn(true, _param._schregno);
            final String semester = "9".equals(_param._semester) ? _param._ctrlSemester : _param._semester;
            final String rtn;
            rtn = " select"
                    + "    T1.SCHREGNO as schregno,"
                    + "    T1.GRADE as grade,"
                    + "    T1.HR_CLASS as hrclass,"
                    + "    T1.ATTENDNO as attendno,"
                    + "    T2.NAME as name,"
                    + "    T3.HR_NAMEABBV as hr_nameabbv,"
                    + "    T4.STAFFNAME,"
                    + "    T1.COURSECD,"
                    + "    T1.MAJORCD,"
                    + "    T1.COURSECODE"
                    + " from"
                    + "    SCHREG_REGD_DAT T1,"
                    + "    SCHREG_BASE_MST T2,"
                    + "    SCHREG_REGD_HDAT T3,"
                    + "    V_STAFF_MST T4"
                    + " where"
                    + "    T1.SCHREGNO = T2.SCHREGNO and"
                    + "    T1.YEAR = T3.YEAR and"
                    + "    T3.YEAR = T4.YEAR and"
                    + "    T3.TR_CD1 = T4.STAFFCD and"
                    + "    T1.SEMESTER = T3.SEMESTER and"
                    + "    T1.GRADE = T3.GRADE and"
                    + "    T1.HR_CLASS = T3.HR_CLASS and"
                    + "    T1.YEAR = '" + _param._year + "' and"
                    + "    T1.SEMESTER = '" + semester + "' and"
                    + "    T1.SCHREGNO in " + students
                    + " order by"
                    + "    T1.GRADE,"
                    + "    T1.HR_CLASS,"
                    + "    T1.ATTENDNO";
            log.info("生徒のSQL=" + rtn);
            return rtn;
        }
    }

    // ======================================================================
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlSemester;
        private final String[] _schregno;
        private final String _testCd;
        private final String _date;
        /** 順位、平均の対象。1=学年, 2=コース */
        private final String _mode1;
        /** 素点度数分布票の種類。 1=講座別, 2=学年別, 3=コース別 */
        private final String _mode2;
        private final boolean _disableMosi;
        /** 1=, 2=平均, 3=偏差値 */
        private final String _outputKijun;
        /** 印刷日時 */
        private final Calendar _now = Calendar.getInstance();
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;

        private KNJSchoolMst _knjSchoolMst;

        /**
         * テスト項目マスタ。
         */
        private Map _examMaster = new TreeMap();

        private String _semesterName;

        private Set _subClassMst = new HashSet();

        private Set _creditMst = new HashSet();

        private RecordSet _recordset;

        /** 定期考査の教科. */
        private Clazz[] _clazz;

        /** 定期考査のグラフ. */
        private TeikikousaChart[] _charts = new TeikikousaChart[5];

        final KNJDefineSchool _defineSchool = new KNJDefineSchool();
        String _periodInState;
        Map _attendSemesMap;
        Map _hasuuMap;
        boolean _semesFlg;
        String _sDate;

        public Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schregno = request.getParameterValues("CATEGORY_SELECTED");
            _testCd = request.getParameter("TESTCD");
            _date = request.getParameter("DATE").replace('/', '-');
            // 順位、平均の対象
            _mode1 = request.getParameter("MODE1"); // 1=学年, 2=コース
            // 素点度数分布票の種類
            _mode2 = request.getParameter("MODE2"); // 1=講座別, 2=学年別, 3=コース別

            // 1=模試を扱わない
            _disableMosi = "1".equals(request.getParameter("DISABLE_MOSI")) ? true : false;
            /** 1=RECORD_RANK_DATで{CLASS,GRADE,COURSE}_RANKの代わりに{CLASS,GRADE,COURSE}_AVG_RANKを使用する。*/
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            log.debug("模試を印字しないフラグ=" + _disableMosi);
            log.debug("RECORD_SCORE_DATの{CLASS,GRADE,COURSE}{_AVG,_DEVIATION,}_RANKフィールドを使用するフラグ=" + _outputKijun);
            _now.setTime(new Date());
        }
        
        private Clazz getClazz(final SubClass subClass) {
            for (int i = 0; i < _clazz.length; i++) {
                if (subClass._cd.startsWith(_clazz[i]._cd)) {
                    return _clazz[i];
                }
            }
            return null;
        }

        private Exam getCurrentExam() {
            return (Exam) _examMaster.get(_semester + _testCd);
        }

        private void loadTeikiKousaClazz(final DB2UDB db2) {
            final List clazzList = new ArrayList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlTeikiKousaClazz());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String clazzCd = rs.getString("name1");
                    final int index = (Integer.parseInt(rs.getString("name2")) - 1);
                    final String title = rs.getString("name3");
                    final String clazzName = rs.getString("namespare1");

                    final Clazz clazz = new Clazz(clazzCd, clazzName);
                    clazzList.add(clazz);

                    if (null == _charts[index]) {
                        _charts[index] = new TeikikousaChart();
                    }
                    _charts[index]._classes.put(clazz._cd, title);
                }
            } catch (final Exception ex) {
                log.error("定期考査グラフの教科項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            _clazz = new Clazz[clazzList.size()];
            int i = 0;
            log.debug("定期考査グラフの教科(指定順)");
            for (final Iterator it = clazzList.iterator(); it.hasNext();) {
                final Clazz clazz = (Clazz) it.next();
                _clazz[i] = clazz;
                log.debug("\t" + _clazz[i]);
                i++;
            }
        }

        private String sqlTeikiKousaClazz() {
            final String sql;
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                sql = "SELECT"
                        + "  classcd || '-' || school_kind as name1,"
                        + "  class_remark1 as name2,"
                        + "  class_remark2 as name3,"
                        + "  class_remark3 as namespare1"
                        + " FROM"
                        + "  class_detail_dat"
                        + " WHERE"
                        + "  year='" + _year + "' AND"
                        + "  class_seq='007' AND"
                        + "  INT(class_remark1) BETWEEN 1 AND 5"
                        + " ORDER BY"
                        + "  class_remark1, classcd || '-' || school_kind "
                ;
            } else {
                sql = "SELECT"
                        + "  name1,"
                        + "  name2,"
                        + "  name3,"
                        + "  namespare1"
                        + " FROM"
                        + "  v_name_mst"
                        + " WHERE"
                        + "  year='" + _year + "' AND"
                        + "  namecd1='D010' AND"
                        + "  INT(name2) BETWEEN 1 AND 5"
                        + " ORDER BY"
                        + "  name2, name1"
                ;
            }
            return sql;
        }

        private void loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            _semesterName = (String) map.get(_semester);
            log.debug("学期名称=[" + _semesterName + "], 年度の開始日=" + _sDate);
        }

        private String sqlSemester() {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + _year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        public void load(DB2UDB db2) throws SQLException, ParseException {
            loadTeikiKousaClazz(db2);
            _examMaster = Exam.loadExam(db2, this);
            loadSemester(db2);
            _creditMst = CreditMst.loadCreditMst(db2, this);
            _subClassMst = SubClass.loadSubClassMst(db2, this);
            _recordset = new RecordSet();
            _recordset.loadRecord(db2, this);

            // 出欠の情報
            _defineSchool.defineCode(db2, _year);
            
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }

            final String z010Name1 = setZ010Name1(db2);
            _periodInState = AttendAccumulate.getPeiodValue(db2, definecode, _year, SSEMESTER, _semester);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date);
            _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            
            log.debug("attendSemesMap = " + _attendSemesMap);
            log.debug("hasuuMap = " + _hasuuMap);
            log.debug("selemsFlg = " + _semesFlg);
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _recordset.load(db2, this);
        }

        public int getExamIndex(final Exam exam) {
            int i = 1;
            for (final Iterator it = _examMaster.values().iterator(); it.hasNext();) {
                final Exam e = (Exam) it.next();
                if ("99".equals(e._kindCd)) {
                    continue;
                }
                if (exam.equals(e)) {
                    return i;
                }
                i++;
            }
            return 0;
        }

        /**
         * 年度始めの試験から指定学期の試験までの数を得る。
         * @return 年度始めの試験から指定学期の試験までの数。
         */
        public int getExamCount() {
            if ("1".equals(_semester) && "0101".equals(_testCd)) {
                return 1;
            } else if ("1".equals(_semester) && "0201".equals(_testCd)) {
                return 2;
            } else if ("1".equals(_semester) && "0202".equals(_testCd)) {
                return 3;
            }
            
            if ("2".equals(_semester) && "0101".equals(_testCd)) {
                return 4;
            } else if ("2".equals(_semester) && "0201".equals(_testCd)) {
                return 5;
            } else if ("2".equals(_semester) && "0202".equals(_testCd)) {
                return 6;
            }

            if ("3".equals(_semester) && "0101".equals(_testCd)) {
                return 7;
            } else if ("3".equals(_semester) && "0201".equals(_testCd)) {
                return 8;
            }

            if ("9".equals(_semester) && "9900".equals(_testCd)) {
                return 9; // 値は関係ない。0では不都合なので
            }
            return 0;
        }
    }

    // ======================================================================
    /**
     * 教科。
     */
    private static class Clazz {
        private final String _cd;
        private final String _abbv;

        Clazz(final String cd, final String abbv) {
            _cd = cd;
            _abbv = abbv;
        }

        public String toString() {
            return _cd + ":" + _abbv;
        }
    }
    // ======================================================================
    private static class SubClass implements Comparable {
        private final String _cd;
        private final String _chairCd;
        private final String _abbv;

        private final Map _mock = new HashMap();

        public SubClass(
                final String cd,
                final String chairCd,
                final String abbv
        ) {
            _cd = cd;
            _chairCd = chairCd;
            _abbv = abbv;
        }
        
        public int compareTo(final Object o) {
            if (!(o instanceof SubClass)) {
                return -1;
            }
            final SubClass other = (SubClass) o;
            int cmp;
            cmp = _cd.compareTo(other._cd);
            if (0 != cmp) {
                return cmp;
            }
            if (null != _chairCd && null != other._chairCd) {
                cmp = _chairCd.compareTo(other._chairCd);
            }
            return cmp;
        }

        public void addMock(final String mockName, final Integer score) {
            _mock.put(mockName, score);
        }

        public String toString() {
            return _cd + ":" + _abbv;
        }
        
        private static Set loadSubClassMst(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Set set = new HashSet();
            try {
                ps = db2.prepareStatement(sqlSubClassMst(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    final String abbv = rs.getString("SUBCLASSABBV");

                    final SubClass subClass = new SubClass(code, null, abbv);
                    set.add(subClass);
                }
            } catch (final Exception ex) {
                log.error("科目マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("科目マスタの総数=" + set.size());
            return set;
        }

        private static String sqlSubClassMst(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append("select");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            sql.append("    SUBCLASSCD AS SUBCLASSCD,");
            sql.append("    SUBCLASSABBV");
            sql.append("  from");
            sql.append("    V_SUBCLASS_MST");
            sql.append("  where");
            sql.append("    YEAR='" + param._year + "'");
            return sql.toString();
        }
        
        public static SubClass getSubClass(final Set subClassMst, final String subClasscd) {
            for (final Iterator it = subClassMst.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                if (subClasscd.equals(subClass._cd)) {
                    return subClass;
                }
            }
            return null;
        }
        
        
        private static Collection loadSubClasses(final DB2UDB db2, final Student student, final Param param, final Set subClassMst) throws SQLException {
            final List rtn = new ArrayList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSubClasses(student, param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = SubClass.getSubClass(subClassMst, subClasscd);
                    if (null != subClass) {
                        rtn.add(subClass);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private static String sqlSubClasses(final Student student, final Param param) {
            String rtn;
            rtn = "select"
                    + "    distinct ";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +="    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
            }
            rtn     +="    T1.SUBCLASSCD AS SUBCLASSCD"
                    + " from"
                    + "    CHAIR_DAT T1 inner join CHAIR_STD_DAT T2 on"
                    + "    T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.CHAIRCD = T2.CHAIRCD"
                    + " where"
                    + "    T1.YEAR = '" + param._year + "' and"
                    + "    T2.SCHREGNO = '" + student._schregno + "' and"
                    + "    T2.APPDATE <= '" + param._date + "'"
                    ;
            log.debug("履修科目のSQL=" + rtn);
            return rtn;
        }
    }
    // ======================================================================
    private class MosiChart {
        final JFreeChart _chart;

        /* pkg */MosiChart(final CategoryDataset dataSet) {
            _chart = createChart(dataSet);
        }

        private JFreeChart createChart(final CategoryDataset dataset) {
            JFreeChart chart = ChartFactory.createLineChart(
                null,      // chart title
                null,                      // domain axis label
                null,                      // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, 
                true,                     // include legend. 凡例
                false,                     // tooltips
                false                     // urls
            );
            chart.setBackgroundPaint(Color.white);

            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setOutlineStroke(_outlineStroke);
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(Color.black);
            plot.setOutlinePaint(Color.black);

            // customise the range axis...
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setTickLabelFont(_font);
            rangeAxis.setRange(20, 85);
            rangeAxis.setTickUnit(new NumberTickUnit(10));

            final CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(_smallFont);

            // customise the renderer...
            LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
            renderer.setShapesVisible(true);    // ◆マークとか
            renderer.setBaseShapesFilled(true); // マークを塗り潰すか？⇒◇
            renderer.setDrawOutlines(true); // ?

            renderer.setStroke(new BasicStroke(4.0f));  // 線の太さ
//            renderer.setSeriesShape(0, ShapeUtilities.createRegularCross(6, 6));    // ■
//            renderer.setSeriesShape(1, new Ellipse2D.Double(-7, -7, 14, 14));   // ●
//            renderer.setSeriesShape(2, ShapeUtilities.createUpTriangle(6)); // ▲
//            renderer.setSeriesShape(3, ShapeUtilities.createDiamond(10));    // ◆ DiagonalCrossとの違い不明
//            renderer.setSeriesShape(4, ShapeUtilities.createDownTriangle(6));   // ▼

            final LegendTitle legend = chart.getLegend();
            legend.setItemFont(_font);
            legend.setBorder(BlockBorder.NONE);

            return chart;
        }
    }
    // ======================================================================
    private static class TeikikousaChart {
        private static final String EMPTY_NAME = "";

        final DefaultCategoryDataset _dataset = new DefaultCategoryDataset();
        JFreeChart _chart;
        private final Map _classes = new HashMap();
        private boolean _changeOnce = false;
        private boolean _hasGraphData = false;

        public void reset() {
            _dataset.clear();
            _changeOnce = false;
        }

        public String getName(final String cd) {
            return (String) _classes.get(cd);
        }

        boolean hasClazz(final Clazz clazz) {
            for (final Iterator it = _classes.keySet().iterator(); it.hasNext();) {
                final String cd = (String) it.next();
                if (cd.equals(clazz._cd)) {
                    return true;
                }
            }
            return false;
        }

        public void add(final Param param, final Record record) {
            // テストの ScoreInfo を算出
            record.setHensati(param._recordset, param);
            log.debug(record._subClass._abbv + "の試験毎の情報=" + record._hensatiMap);

            if (!_changeOnce) {
                setExamNamesOnGraph(param);
                _changeOnce = true;
            }
            // RECORD_DAT にぶら下がる、各テストの偏差値を Dataset にセットする
            for (final Iterator it = record._examScoreMap.keySet().iterator(); it.hasNext();) {
                final Exam exam = (Exam) it.next();
                if ("99".equals(exam._kindCd)) { // 学年成績は対象外
                    continue;
                }

                // 得点の取得から偏差値の算出
                final Integer score = (Integer) record._examScoreMap.get(exam);
                if (null == score) {
                    continue;
                }
                final BigDecimal si = (BigDecimal) record._hensatiMap.get(exam);
                if (null == si) {
                    continue;
                }
                final double stdScore = si.doubleValue();//TAKAESU:余分な変換は無いか?
                final BigDecimal bd = new BigDecimal(String.valueOf(stdScore)).setScale(0, BigDecimal.ROUND_HALF_UP);
                final Integer hensati = new Integer(bd.intValue());

                // 偏差値のセット
                _dataset.addValue(hensati, record._subClass._abbv, exam);
                _hasGraphData = true;
            }
        }

        private void setExamNamesOnGraph(final Param param) {
            final int examCount = param.getExamCount();

            int i = 1;
            for (final Iterator it = param._examMaster.values().iterator(); it.hasNext();) {
                final Exam exam = (Exam) it.next();
                if ("99".equals(exam._kindCd)) {
                    continue;
                }
                if (i > examCount) {
                    return;
                }
                _dataset.addValue(null, EMPTY_NAME, exam);
                i++;
            }
        }

        public int printTable(final Form form, final Param param, final int subClassIdx) {
            
            int i = subClassIdx;
            for (final Iterator it = _dataset.getRowKeys().iterator(); it.hasNext();) {
                final String subClassName = (String) it.next();
                if (EMPTY_NAME.equals(subClassName)) {
                    continue;
                }
                form.svfOutn("SUBCLASSABBV", i, subClassName);   // 科目名

                for (final Iterator it2 = _dataset.getColumnKeys().iterator(); it2.hasNext();) {
                    final Exam exam = (Exam) it2.next();

                    final Number value;
                    try {
                        value = _dataset.getValue(subClassName, exam);
                    } catch (final UnknownKeyException e) {
                        continue;
                    }
                    if (null == value) {
                        continue;
                    }
                    final int examIndex = param.getExamIndex(exam);
                    form.svfOutn("POINT1_" + examIndex, i, value.toString());
                }
                i++;
            }
            return subClassIdx + _TEIKIKOUSA_TABLE_KAMOKU_MAX;
        }

        public boolean hasData() {
            return _hasGraphData;
        }

        public void commit() {
//            for (final Iterator it = _dataset.getRowKeys().iterator(); it.hasNext();) {
//                final String subClassName = (String) it.next();
//
//                for (final Iterator it2 = _dataset.getColumnKeys().iterator(); it2.hasNext();) {
//                    final String examName = (String) it2.next();
//                    final Number value;
//                    try {
//                        value = _dataset.getValue(subClassName, examName);
//                    } catch (final UnknownKeyException e) {
//                        log.info(subClassName + ", " + examName + "⇒---");
//                        continue;
//                    }
//                    log.info(subClassName + ", " + examName + "⇒" + value);
//                }
//            }

            _chart = createChart(_dataset);
        }

        private JFreeChart createChart(final CategoryDataset dataset) {
            JFreeChart chart = ChartFactory.createLineChart(
                null,      // chart title
                null,                      // domain axis label
                null,                      // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, 
                true,                     // include legend. 凡例
                false,                     // tooltips
                false                     // urls
            );
            chart.setBackgroundPaint(Color.white);

            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setOutlineStroke(_outlineStroke);
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(Color.black);
            plot.setOutlinePaint(Color.black);

            // customise the range axis...
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setTickLabelFont(_font);
            rangeAxis.setRange(20, 85);
            rangeAxis.setTickUnit(new NumberTickUnit(10));

            final CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(_font);

            // customise the renderer...
            LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
            renderer.setShapesVisible(true);    // ◆マークとか
            renderer.setBaseShapesFilled(true); // マークを塗り潰すか？⇒◇
            renderer.setDrawOutlines(true); // ?

            renderer.setStroke(new BasicStroke(4.0f));  // 線の太さ
//            renderer.setShape(new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));    // マークの指定。ただし、1種類になっちゃう

            renderer.setSeriesVisible(0, Boolean.FALSE);    // 最初の科目はダミーなので描画しない

            final LegendTitle legend = chart.getLegend();
            legend.setItemFont(_font);
            legend.setBorder(BlockBorder.NONE);

            return chart;
        }
    }
    // ======================================================================
    private class BunpuChart {
        JFreeChart _chart;

        final DefaultCategoryDataset _pointDataset = new DefaultCategoryDataset();
        final DefaultCategoryDataset _avgDataset = new DefaultCategoryDataset();

        final List _startList = new ArrayList();
        final List _endList = new ArrayList();
        final List _categoryNameList = new ArrayList();

        public void add(final Record record, final ScoreInfo scoreInfo) {
            final String name = record._subClass._abbv;

            _pointDataset.addValue(record._currentScore.doubleValue(), "得点", name);
            _avgDataset.addValue(scoreInfo.getAvg().doubleValue(), "平均", name);

            _startList.add(scoreInfo.getLow());
            _endList.add(scoreInfo.getHigh());

            _categoryNameList.add(name);
        }

        public void commit() {
            final DefaultIntervalCategoryDataset dataset = createDataset();
            _chart = createChart((IntervalCategoryDataset) dataset);

            setLineGraph(_chart.getCategoryPlot(), _pointDataset);
            setLineGraph(_chart.getCategoryPlot(), _avgDataset);
        }

        private DefaultIntervalCategoryDataset createDataset() {
            final Integer[][] starts = new Integer[1][];
            final Integer[][] ends = new Integer[1][];

            final Integer[] aaa = new Integer[_startList.size()];
            _startList.toArray(aaa);
            starts[0] = aaa;

            final Integer[] bbb = new Integer[_endList.size()];
            _endList.toArray(bbb);
            ends[0] = bbb;

            final String[] categoryNames = new String[_categoryNameList.size()];
            _categoryNameList.toArray(categoryNames);

            final DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(
                    new String[] {"範囲"},
                    categoryNames,
                    starts,
                    ends
            );

            return dataset;
        }

        private JFreeChart createChart(final IntervalCategoryDataset dataset) {
            final CategoryAxis domainAxis = new CategoryAxis(null);
            final NumberAxis rangeAxis = new NumberAxis(null);

            domainAxis.setTickLabelFont(_font);
            rangeAxis.setTickLabelFont(_font);
//            domainAxis.setCategoryLabelPositions(
//                    CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
//            );
            rangeAxis.setTickUnit(new NumberTickUnit(10));

//            System.err.println(">>>>col=" + dataset.getColumnCount());
//            System.err.println(">>>>row=" + dataset.getRowCount());
//            for (final Iterator it = dataset.getColumnKeys().iterator(); it.hasNext();) {
//                final Object element = (Object) it.next();
//                System.err.println("col keys=" + element);
//            }
//            for (final Iterator it = dataset.getRowKeys().iterator(); it.hasNext();) {
//                final Object element = (Object) it.next();
//                System.err.println("row keys=" + element);
//            }
            final IntervalBarRenderer renderer = new AlpIntervalBarRenderer();
            renderer.setMaximumBarWidth(0.03);
            renderer.setBase(100);  // 100点満点
            renderer.setPaint(Color.lightGray);
            renderer.setOutlinePaint(Color.black);
            final CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            final JFreeChart chart = new JFreeChart(plot);
            plot.setOutlineStroke(_outlineStroke);

            // set the background color for the chart...
            chart.setBackgroundPaint(Color.white);
            plot.setBackgroundPaint(Color.white);
            plot.setOutlinePaint(Color.black);
            plot.setRangeGridlinePaint(Color.black);

            final LegendTitle legend = chart.getLegend();
            legend.setItemFont(_font);
            legend.setBorder(BlockBorder.NONE);

            return chart;
         }

        int index = 1;
        private void setLineGraph(final CategoryPlot plot, final DefaultCategoryDataset dataset) {
            plot.setDataset(index, dataset);

            final LineAndShapeRenderer rend = new LineAndShapeRenderer();
//            rend.setLinesVisible(false);
            plot.setRenderer(index, rend);
            index++;
        }

        /**
         * IntervalBarRenderer#setMaximumBarWidthメソッドのバグ修正版。
         * drawInterval メソッドのロジックをパクってから、★部分を追加した。
         */
        class AlpIntervalBarRenderer extends IntervalBarRenderer {
            protected void drawInterval(Graphics2D g2,
                    CategoryItemRendererState state,
                    Rectangle2D dataArea,
                    CategoryPlot plot,
                    CategoryAxis domainAxis,
                    ValueAxis rangeAxis,
                    IntervalCategoryDataset dataset,
                    int row,
                    int column
            ) {
                int seriesCount = getRowCount();
                int categoryCount = getColumnCount();

                PlotOrientation orientation = plot.getOrientation();

                double rectX = 0.0;
                double rectY = 0.0;

                RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
                RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

                // Y0
                Number value0 = dataset.getEndValue(row, column);
                if (value0 == null) {
                    return;
                }
                double java2dValue0 = rangeAxis.valueToJava2D(value0.doubleValue(), dataArea, rangeAxisLocation);

                // Y1
                Number value1 = dataset.getStartValue(row, column);
                if (value1 == null) {
                    return;
                }
                double java2dValue1 = rangeAxis.valueToJava2D(value1.doubleValue(), dataArea, rangeAxisLocation);

                if (java2dValue1 < java2dValue0) {
                    double temp = java2dValue1;
                    java2dValue1 = java2dValue0;
                    java2dValue0 = temp;
                    Number tempNum = value1;
                    value1 = value0;
                    value0 = tempNum;
                }

                // BAR WIDTH
                double rectWidth = state.getBarWidth();

                // BAR HEIGHT
                double rectHeight = Math.abs(java2dValue1 - java2dValue0);

                if (orientation == PlotOrientation.HORIZONTAL) {
                    // BAR Y
                    rectY = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, domainAxisLocation);
                    if (seriesCount > 1) {
                        double seriesGap = dataArea.getHeight() * getItemMargin() / (categoryCount * (seriesCount - 1));
                        rectY = rectY + row * (state.getBarWidth() + seriesGap);
                    } else {
                        rectY = rectY + row * state.getBarWidth();
                    }

                    rectX = java2dValue0;

                    rectHeight = state.getBarWidth();
                    rectWidth = Math.abs(java2dValue1 - java2dValue0);

                } else if (orientation == PlotOrientation.VERTICAL) {
                    // BAR X
                    rectX = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, domainAxisLocation);
                    if (seriesCount > 1) {
                        double seriesGap = dataArea.getWidth() * getItemMargin() / (categoryCount * (seriesCount - 1));
                        rectX = rectX + row * (state.getBarWidth() + seriesGap);
                    } else {
                        rectX = rectX + row * state.getBarWidth();
                    }

                    /*
                     * (★)以下のロジックを追加した。
                     */
                    // ↓↓↓↓↓↓↓
                    final double normalWidth = dataArea.getWidth() * 0.7 / getColumnCount();
                    final double centerSabun;
                    if (categoryCount == 1) {
                        final double sa = dataArea.getWidth() / 10;
                        centerSabun = Math.abs((normalWidth - rectWidth) / 2) + sa;
                    } else {
                        centerSabun = Math.abs((normalWidth - rectWidth) / 2);
                    }
                    rectX += centerSabun;
                    // ↑↑↑↑↑↑↑
                    /*
                     * 上記の解説。
                     * +-----------------------------------------+
                     * |         描画エリア                      |
                     * | +---------------------------(1)-------+ |
                     * | |             データエリア            | |
                     * | |                                     | |
                     * | |                                     | |
                     * | |     +----------(2)------------+     | |
                     * | |     |                         |     | |
                     * | |<-A->|                         |<-A->| |
                     * | |     |                         |     | |
                     * |<--B-->|                         |     | |
                     * | |     |                         |     | |
                     * | +-----+-------------------------+-----+ |
                     * |                                         |
                     * +-----------------------------------------+
                     *
                     * (1) : dataArea.getWidth()
                     * (2) : normalWidth : 棒幅が全体の70%の時の1本あたりの幅
                     * 
                     * A : グラフエリアの両サイドマージンは、各々 15 のようだ。
                     * B : rectX : 棒グラフ左端の座標。基点は描画エリア左端(グラフエリアではない)
                     * 
                     */

                    rectY = java2dValue0;

                }
                Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
                Paint seriesPaint = getItemPaint(row, column);
                g2.setPaint(seriesPaint);
                g2.fill(bar);

                // draw the outline...
                if (state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
                    Stroke stroke = getItemOutlineStroke(row, column);
                    Paint paint = getItemOutlinePaint(row, column);
                    if (stroke != null && paint != null) {
                        g2.setStroke(stroke);
                        g2.setPaint(paint);
                        g2.draw(bar);
                    }
                }

                CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
                if (generator != null && isItemLabelVisible(row, column)) {
                    drawItemLabel(g2, dataset, row, column, plot, generator, bar, false);
                }

                // collect entity and tool tip information...
                if (state.getInfo() != null) {
                    EntityCollection entities = state.getEntityCollection();
                    if (entities != null) {
                        String tip = null;
                        CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
                        if (tipster != null) {
                            tip = tipster.generateToolTip(dataset, row, column);
                        }
                        String url = null;
                        if (getItemURLGenerator(row, column) != null) {
                            url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
                        }
                        CategoryItemEntity entity = new CategoryItemEntity(bar, tip, url, dataset, row, dataset.getColumnKey(column), column);
                        entities.add(entity);
                    }
                }
            }
        }
    }
    
    private static class RecordSet {

        /** 成績。DBから、ほとんど全てを取り込む。PGではこの値から必要分だけ抽出して使っている.<schregno,Record> */
        MultiMap _records;

        /** [学年]の平均/総数, 最高点, 最低点. */
        final Map _avgGradeDat = new HashMap();
        /** [コース]の平均/総数, 最高点, 最低点. */
        final MultiMap _avgCourseDat = new MultiHashMap();
        /** 講座の平均/総数, 最高点, 最低点. */
        final Map _avgChairDat = new HashMap();
        /** 成績席次データ.<schregno,RankDat> */
        final MultiMap _rankDat = new MultiHashMap();

        public void loadRecord(final DB2UDB db2, final Param param) {
            loadAvgChairDat(db2, param);
            loadAvgDat(db2, "1", "(学年)", param);
            loadAvgDat(db2, "3", "(コース)", param);
            loadRankDat(db2, param);
        }

        public void load(final DB2UDB db2, final Param param) {
            _records = loadRecord0(db2, param);
            log.info("RECORD_DAT size=" + _records.size());
            loadRecord1(db2, param);
        }

        private static MultiMap loadRecord0(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final MultiMap rtn = new MultiHashMap();
            try {
                ps = db2.prepareStatement(sqlRecord0(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final String chairCd = rs.getString("CHAIRCD");
                    final String abbv = rs.getString("ABBV");
                    final SubClass subClass = new SubClass(subClassCd, chairCd, abbv);

                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE"); // 生徒の学年

                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final Course course = new Course(courseCd, majorCd, courseCode);

                    final Integer score = KNJServletUtils.getInteger(rs, "SCORE");

                    final Record record = new Record(
                            schregno,
                            grade,
                            course,
                            subClass,
                            score
                    );
                    rtn.put(schregno, record);
                }
            } catch (final Exception ex) {
                log.error("成績データの取得でエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        private static String sqlRecord0(final Param param) {
            final String semester = "9".equals(param._semester) ? param._ctrlSemester : param._semester;
            final String tablename = param.getCurrentExam().isGakunenSeiseki() ? " RECORD_RANK_CHAIR_V_DAT " : " RECORD_RANK_CHAIR_DAT ";
            String rtn;
            
            rtn = " with rank_chair as ("
                    + " select "
                    + "    T2.YEAR,"
                    + "    T2.SEMESTER,"
                    + "    T2.TESTKINDCD,"
                    + "    T2.TESTITEMCD,";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +=    "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ";
            }
            rtn     +="    T2.subclasscd AS subclasscd,"
                    + "    T2.schregno,"
                    + "    T2.chaircd,"
                    + "    T2.SCORE"
                    + " from"
                    + "    " + tablename + " T2 "
                    + " where"
                    + "    T2.YEAR = '" + param._year + "' and "
                    + "    T2.SEMESTER = '" + param._semester + "' and"
                    + "    T2.TESTKINDCD = '" + param.getCurrentExam()._kindCd + "' and"
                    + "    T2.TESTITEMCD = '" + param.getCurrentExam()._itemCd + "' and"
                    + "    T2.CHAIRCD = ( select MIN(CHAIRCD) from "
                    + "        " + tablename + " T3 "
                    + "     where"
                    + "        T3.YEAR = T2.YEAR and "
                    + "        T3.SEMESTER = T2.SEMESTER and"
                    + "        T3.TESTKINDCD = T2.TESTKINDCD and"
                    + "        T3.TESTITEMCD = T2.TESTITEMCD and";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +="        T3.CLASSCD = T2.CLASSCD and ";
                rtn +="        T3.SCHOOL_KIND = T2.SCHOOL_KIND and ";
                rtn +="        T3.CURRICULUM_CD = T2.CURRICULUM_CD and ";
            }
            rtn     +="        T3.SUBCLASSCD = T2.SUBCLASSCD and ";
            rtn     +="        T3.SCHREGNO =  T2.SCHREGNO "
                    + "    ) "
                    + " ) "
                    + " select"
                    + "    T1.SUBCLASSABBV as abbv,";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +=    "    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ";
            }
            rtn     +="    T2.subclasscd AS subclasscd,"
                    + "    T2.schregno,"
                    + "    T2.chaircd,"
                    + "    T3.GRADE,"
                    + "    T3.COURSECD,"
                    + "    T3.MAJORCD,"
                    + "    T3.COURSECODE,"
                    + "    T2.SCORE"
                    + " from"
                    + "    V_SUBCLASS_MST T1,"
                    + "    rank_chair T2,"
                    + "    SCHREG_REGD_DAT T3"
                    + " where"
                    + "    T1.YEAR = T2.YEAR and"
                    + "    T1.YEAR = T3.YEAR and"
                    + "    T2.SCHREGNO = T3.SCHREGNO and";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +=    "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
            }
            rtn     +="    T1.SUBCLASSCD = ";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +=    "    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ";
            }
            rtn     +="    T2.SUBCLASSCD and"
                    + "    T3.SEMESTER = '" + semester + "'"
                    ;
            log.debug("全生徒、全科目の SQL=" + rtn);
            return rtn;
        }

        private Record findRecord(final String schregno, final String subclassCd) {
            final Collection coll = (Collection) _records.get(schregno);
            if (null == coll) {
                return null;
            }
            for (final Iterator it = coll.iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                if (record._subClass._cd.equals(subclassCd)) {
                    return record;
                }
            }
            return null;
        }
        
        private void loadRecord1(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlRecord1(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String subclassCd = rs.getString("subclasscd");
                    final Record record = findRecord(schregno, subclassCd);
                    if (null == record) {
                        continue;
                    }
                    final String semester = rs.getString("semester");
                    final String kindCd = rs.getString("testkindcd");
                    final String itemCd = rs.getString("testitemcd");

                    final Exam exam = (Exam) param._examMaster.get(semester + kindCd + itemCd);
                    if (null == exam) {
                        log.warn("考査種別なし:" + semester + kindCd + itemCd);
                        continue;
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    record._examScoreMap.put(exam, score);

                    if (!"3".equals(param._outputKijun)) {
                        if (exam.equals(param.getCurrentExam())) {
                            final Integer gradeRank = KNJServletUtils.getInteger(rs, "grade_rank");
                            record._rank = gradeRank;
                        }
                    }
                }
            } catch (final Exception ex) {
                log.error("成績データの取得でエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        private static String sqlRecord1(final Param param) {
            final String gradeRank = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? null : "grade_rank";
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  schregno,");
            sql.append("  semester,");
            sql.append("  testkindcd,");
            sql.append("  testitemcd,");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("  CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN ");
                sql.append("    SUBCLASSCD ");
                sql.append("  ELSE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD END ");
            } else {
                sql.append("  subclasscd ");
            }
            sql.append("   AS subclasscd,");
            sql.append(null == gradeRank ? "" : (gradeRank + " as grade_rank, "));
            sql.append("  score");
            sql.append(" FROM");
            sql.append("  record_rank_chair_dat t1");
            sql.append(" WHERE");
            sql.append("  year='" + param._year + "' AND");
            sql.append("  semester<>'9' AND");
            sql.append("  testkindcd<>'99' AND");
            sql.append("  chaircd  = (SELECT MIN(chaircd) ");
            sql.append("              FROM record_rank_chair_dat t2");
            sql.append("              WHERE");
            sql.append("               t2.year = t1.year AND");
            sql.append("               t2.semester = t1.semester AND");
            sql.append("               t2.testkindcd = t1.testkindcd AND");
            sql.append("               t2.testitemcd = t1.testitemcd AND");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("       t2.classcd = t1.classcd AND ");
                sql.append("       t2.school_kind = t1.school_kind AND ");
                sql.append("       t2.curriculum_cd = t1.curriculum_cd AND ");
            }
            sql.append("               t2.subclasscd = t1.subclasscd AND");
            sql.append("               t2.schregno = t1.schregno ");
            sql.append("             ) ");
            if (param.getCurrentExam().isGakunenSeiseki()) {
                sql.append(" UNION ALL ");
                sql.append("SELECT");
                sql.append("  schregno,");
                sql.append("  semester,");
                sql.append("  testkindcd,");
                sql.append("  testitemcd,");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("  CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN ");
                    sql.append("    SUBCLASSCD ");
                    sql.append("  ELSE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD END ");
                } else {
                    sql.append("  subclasscd ");
                }
                sql.append("   AS subclasscd,");
                sql.append(null == gradeRank ? "" : (gradeRank + " as grade_rank, "));
                sql.append("  score");
                sql.append(" FROM");
                sql.append("  record_rank_chair_v_dat t1");
                sql.append(" WHERE");
                sql.append("  year='" + param._year + "' AND");
                sql.append("  semester='9' AND");
                sql.append("  testkindcd || testitemcd = '9900' AND");
                sql.append("  chaircd  = (SELECT MIN(chaircd) ");
                sql.append("              FROM record_rank_chair_v_dat t2");
                sql.append("              WHERE");
                sql.append("               t2.year = t1.year AND");
                sql.append("               t2.semester = t1.semester AND");
                sql.append("               t2.testkindcd = t1.testkindcd AND");
                sql.append("               t2.testitemcd = t1.testitemcd AND");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("       t2.classcd = t1.classcd AND ");
                    sql.append("       t2.school_kind = t1.school_kind AND ");
                    sql.append("       t2.curriculum_cd = t1.curriculum_cd AND ");
                }
                sql.append("               t2.subclasscd = t1.subclasscd AND");
                sql.append("               t2.schregno = t1.schregno ");
                sql.append("             ) ");
            }
            sql.append(" ORDER BY");
            sql.append("  semester,");
            sql.append("  testkindcd,");
            sql.append("  testitemcd");
            return sql.toString();
        }

        private void loadRankDat(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlRankDat(param));
                log.debug(" sql rank = " + sqlRankDat(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final RankDat rankDat = new RankDat();

                    final String semester = rs.getString("semester");
                    final String kindCd = rs.getString("testkindcd");
                    final String itemCd = rs.getString("testitemcd");
                    final Exam exam = (Exam) param._examMaster.get(semester + kindCd + itemCd);
                    if (null == exam) {
                        log.warn("考査種別なし:" + semester + kindCd + itemCd);
                        continue;
                    }
                    rankDat._exam = exam;

                    rankDat._subclassCd = rs.getString("subclasscd");
                    rankDat._schregno = rs.getString("schregno");
                    rankDat._gradeRank = KNJServletUtils.getInteger(rs, "grade_rank");
                    rankDat._gradeDeviation = rs.getBigDecimal("grade_deviation");
                    rankDat._courseRank = KNJServletUtils.getInteger(rs, "course_rank");
                    rankDat._courseDeviation = rs.getBigDecimal("course_deviation");
                    rankDat._score = KNJServletUtils.getInteger(rs, "score");

                    _rankDat.put(rankDat._schregno, rankDat);
                }
            } catch (final Exception ex) {
                log.error("成績席次データのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("成績席次データの総数=" + _rankDat.size());
        }
        
        private List getRankDatList(final String schregno, final String subClassCd) {
            final List rtn = new ArrayList();
            final Collection rankDats = (Collection) _rankDat.get(schregno);
            if (null != rankDats) {
                for (final Iterator it = rankDats.iterator(); it.hasNext();) {
                    final RankDat rankDat = (RankDat) it.next();
                    if (subClassCd.equals(rankDat._subclassCd)) {
                        rtn.add(rankDat);
                    }
                }
            }
            return rtn;
        }
        
        private RankDat getRankDat(final String schregno, final String subClassCd, final Exam exam) {
            final List list = getRankDatList(schregno, subClassCd);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final RankDat rankDat = (RankDat) it.next();
                if (rankDat._exam.equals(exam)) {
                    return rankDat;
                }
            }
            return null;
        }

        private String sqlRankDat(final Param param) {
            final String gradeRank = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
            final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  semester,");
            sql.append("  testkindcd,");
            sql.append("  testitemcd,");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("  CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN ");
                sql.append("    SUBCLASSCD ");
                sql.append("  ELSE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD END ");
            } else {
                sql.append("  subclasscd ");
            }
            sql.append("   AS subclasscd,");
            sql.append("  schregno,");
            sql.append(gradeRank + " as grade_rank,");
            sql.append("  grade_deviation,");
            sql.append(courseRank + " as course_rank,");
            sql.append("  course_deviation,");
            sql.append("  score");
            sql.append(" FROM");
            sql.append("  record_rank_dat");
            sql.append(" WHERE");
            sql.append("  year='" + param._year + "' AND");
            sql.append("  semester<>'9' AND ");
            sql.append("  testkindcd<> '99' AND");
            sql.append("  schregno IN " + SQLUtils.whereIn(true, param._schregno));
            if (param.getCurrentExam().isGakunenSeiseki()) {
                sql.append(" UNION ALL ");
                sql.append(" SELECT");
                sql.append("  semester,");
                sql.append("  testkindcd,");
                sql.append("  testitemcd,");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("  CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN ");
                    sql.append("    SUBCLASSCD ");
                    sql.append("  ELSE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD END ");
                } else {
                    sql.append("  subclasscd ");
                }
                sql.append("   AS subclasscd,");
                sql.append("  schregno,");
                sql.append(gradeRank + " as grade_rank,");
                sql.append("  grade_deviation,");
                sql.append(courseRank + " as course_rank,");
                sql.append("  course_deviation,");
                sql.append("  score");
                sql.append(" FROM");
                sql.append("  record_rank_v_dat");
                sql.append(" WHERE");
                sql.append("  year='" + param._year + "' AND");
                sql.append("  semester='9' AND ");
                sql.append("  testkindcd || testitemcd ='9900' AND");
                sql.append("  schregno IN " + SQLUtils.whereIn(true, param._schregno));
            }
            return sql.toString();
        }

        private ScoreInfo createAvgDatInfoCourse(final MultiMap map, final String subClassCd, final Student student, final Exam exam) {
            final ScoreInfo rtn = new ScoreInfo();
            
            final Collection coll = (Collection) map.get(student._grade + subClassCd);
            if (null != coll) {
                for (final Iterator it = coll.iterator(); it.hasNext();) {
                    final AverageDat avgDat = (AverageDat) it.next();
                    if (null == avgDat) {
                        continue;
                    }
                    if (student._course.equals(avgDat._course) && student._grade.equals(avgDat._grade)) {
                        rtn._high = avgDat._highScore;
                        rtn._low = avgDat._lowScore;
                        rtn._avg = avgDat._avg;
                        rtn._size = avgDat._count.intValue();
                    }
                }
            }
            return rtn;
        }

        private ScoreInfo createAvgDatInfo(final Map map, final String subClassCd, final Student student, final Exam exam) {
            final ScoreInfo rtn = new ScoreInfo();
            final AverageDat avgDat = (AverageDat) map.get(student._grade + subClassCd);
            if (null != avgDat) {
                rtn._high = avgDat._highScore;
                rtn._low = avgDat._lowScore;
                rtn._avg = avgDat._avg;
                rtn._size = avgDat._count.intValue();
            }
            return rtn;
        }
        
        private ScoreInfo createChairInfo(final Record record) {
            final SubClass subClass = record._subClass;
            if (null == subClass) {
                return null;
            }

            final ScoreInfo rtn = new ScoreInfo();
            final AverageDat avgDat = (AverageDat) _avgChairDat.get(subClass._chairCd);
            if (null != avgDat) {
                rtn._high = avgDat._highScore;
                rtn._low = avgDat._lowScore;
                rtn._avg = avgDat._avg;
                rtn._size = avgDat._count.intValue();
            }
            return rtn;
        }

        private void loadAvgDat(final DB2UDB db2, final String avgDiv, final String msg, final Param param) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlAvgDat(avgDiv, param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final AverageDat averageDat = new AverageDat();
                    averageDat._subclassCd = rs.getString("subclasscd");
                    averageDat._grade = rs.getString("grade");
                    averageDat._hrClass = rs.getString("hr_class");

                    final String courseCd = rs.getString("coursecd");
                    final String majorCd = rs.getString("majorcd");
                    final String courseCode = rs.getString("coursecode");
                    averageDat._course = new Course(courseCd, majorCd, courseCode);

                    averageDat._highScore = KNJServletUtils.getInteger(rs, "highscore");
                    averageDat._lowScore = KNJServletUtils.getInteger(rs, "lowscore");
                    averageDat._count = KNJServletUtils.getInteger(rs, "count");
                    averageDat._avg = rs.getBigDecimal("avg");

                    if ("1".equals(avgDiv)) {
                        _avgGradeDat.put(averageDat._grade + averageDat._subclassCd, averageDat);
                    } else {
                        _avgCourseDat.put(averageDat._grade + averageDat._subclassCd, averageDat);
                    }
                }
            } catch (final Exception ex) {
                log.error("成績平均データのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if ("1".equals(avgDiv)) {
                log.debug("成績平均データ" + msg + "の総数=" + _avgGradeDat.size());
            } else {
                log.debug("成績平均データ" + msg + "の総数=" + _avgCourseDat.size());
            }
        }

        private String sqlAvgDat(final String avgDiv, final Param param) {
            final String tablename = param.getCurrentExam().isGakunenSeiseki() ? " record_average_v_dat " : " record_average_dat ";
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("  CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN ");
                sql.append("    SUBCLASSCD ");
                sql.append("  ELSE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD END ");
            } else {
                sql.append("  subclasscd ");
            }
            sql.append("   AS subclasscd,");
            sql.append("  grade,");
            sql.append("  hr_class,");
            sql.append("  coursecd,");
            sql.append("  majorcd,");
            sql.append("  coursecode,");
            sql.append("  highscore,");
            sql.append("  lowscore,");
            sql.append("  count,");
            sql.append("  avg");
            sql.append(" FROM");
            sql.append("  " + tablename + " ");
            sql.append(" WHERE");
            sql.append("  year='" + param._year + "' AND");
            sql.append("  semester='" + param._semester + "' AND");
            sql.append("  testkindcd='" + param.getCurrentExam()._kindCd + "' AND");
            sql.append("  testitemcd='" + param.getCurrentExam()._itemCd + "' AND");
            sql.append("  avg_div='" + avgDiv + "'");

            log.info("sql=" + sql);
            return sql.toString();
        }

        private void loadAvgChairDat(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlAvgChairDat(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final AverageDat averageDat = new AverageDat();
                    averageDat._chairCd = rs.getString("chaircd");
                    averageDat._subclassCd = rs.getString("subclasscd");
                    averageDat._grade = rs.getString("grade");
                    averageDat._hrClass = rs.getString("hr_class");

                    final String courseCd = rs.getString("coursecd");
                    final String majorCd = rs.getString("majorcd");
                    final String courseCode = rs.getString("coursecode");
                    averageDat._course = new Course(courseCd, majorCd, courseCode);

                    averageDat._highScore = KNJServletUtils.getInteger(rs, "highscore");
                    averageDat._lowScore = KNJServletUtils.getInteger(rs, "lowscore");
                    averageDat._count = KNJServletUtils.getInteger(rs, "count");
                    averageDat._avg = rs.getBigDecimal("avg");

                    _avgChairDat.put(averageDat._chairCd, averageDat);//TAKAESU:とりえあず講座コードをキー
                }
            } catch (final Exception ex) {
                log.error("成績講座平均データのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("成績講座平均データの総数=" + _avgChairDat.size());
        }

        private String sqlAvgChairDat(final Param param) {
            final String tablename = param.getCurrentExam().isGakunenSeiseki() ? " record_average_chair_v_dat " : " record_average_chair_dat ";
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  chaircd,");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("  CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN ");
                sql.append("    SUBCLASSCD ");
                sql.append("  ELSE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD END ");
            } else {
                sql.append("  subclasscd ");
            }
            sql.append("   AS subclasscd,");
            sql.append("  grade,");
            sql.append("  hr_class,");
            sql.append("  coursecd,");
            sql.append("  majorcd,");
            sql.append("  coursecode,");
            sql.append("  highscore,");
            sql.append("  lowscore,");
            sql.append("  count,");
            sql.append("  avg");
            sql.append(" FROM");
            sql.append("  " + tablename + " ");
            sql.append(" WHERE");
            sql.append("  year='" + param._year + "' AND");
            sql.append("  semester='" + param._semester + "' AND");
            sql.append("  testkindcd='" + param.getCurrentExam()._kindCd + "' AND");
            sql.append("  testitemcd='" + param.getCurrentExam()._itemCd + "' AND");
            sql.append("  avg_div='1'");
            return sql.toString();
        }
    }

    // ======================================================================
    private static class Record implements Comparable {
        /** 生徒情報: 学籍番号 */
        private final String _schregno;
        /** 生徒情報: 学年 */
        private final String _grade;
        /** 生徒情報: コース情報 */
        private final Course _course;

        /** 得点情報: 科目 */
        private final SubClass _subClass;
        /** 得点情報: 対象学期・試験の得点 */
        private final Integer _currentScore;
        /** 講座順位. */
        private Integer _rank;

        // -----------------------------------

        /** 学期・試験と得点の関連付け */
        private final Map _examScoreMap = new TreeMap();
        /** 定期考査の偏差値情報 */
        private final Map _hensatiMap = new TreeMap();

        public Record(
                final String schregno,
                final String grade,
                final Course course,
                final SubClass subClass,
                final Integer score
        ) {
            _schregno = schregno;
            _grade = grade;
            _course = course;
            _subClass = subClass;
            _currentScore = score;
        }
        
        public int compareTo(final Object o) { // とりあえず科目コードのみ
            if (!(o instanceof Record)) {
                return -1;
            }
            final Record other = (Record) o;
            int cmp = 0;
            if (null != _subClass && null != other._subClass) {
                cmp = _subClass.compareTo(other._subClass);
            }
            return cmp;
        }

        public void setHensati(final RecordSet recordset, final Param param) {
            final List rankDatList = recordset.getRankDatList(_schregno, _subClass._cd);
            for (final Iterator it = rankDatList.iterator(); it.hasNext();) {
                final RankDat rankDat = (RankDat) it.next();
                final BigDecimal deviation = "1".equals(param._mode1) ? rankDat._gradeDeviation : rankDat._courseDeviation;
                _hensatiMap.put(rankDat._exam, deviation);
            }
        }

        private Integer getExamScore(final Exam exam) {
            return (Integer) _examScoreMap.get(exam);
        }

        public String toString() {
            return _subClass._cd + ":" + _subClass._abbv + "/" + _currentScore;
        }
        
        private static Record getRecord(final Collection studentRecords, final SubClass subClass) {
            for (final Iterator it = studentRecords.iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                if (record._subClass._cd.equals(subClass._cd)) {
                    return record;
                }
            }
            return null;
        }
    }
    // ======================================================================
    private static class Course {
        private final String _courseCd;
        private final String _majorcd;
        private final String _courseCode;

        /**
         * コンストラクタ。
         * @param courseCd 課程
         * @param majorcd 学科
         * @param courseCode コース
         */
        /* pkg */Course(
                final String courseCd,
                final String majorcd,
                final String courseCode
        ) {
            _courseCd = courseCd;
            _majorcd = majorcd;
            _courseCode = courseCode;
        }

        /** {@inheritDoc} */
        public boolean equals(final Object obj) {
            if (obj instanceof Course) {
                final Course that = (Course) obj;
                if (!this._courseCd.equals(that._courseCd)) {
                    return false;
                }
                if (!this._majorcd.equals(that._majorcd)) {
                    return false;
                }
                return this._courseCode.equals(that._courseCode);
            }
            return false;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return toString().hashCode();
        }

        /** {@inheritDoc} */
        public String toString() {
            return _courseCd + "_" + _majorcd + "_" + _courseCode;
        }
    } // Course
    // ======================================================================
    private class StudentChooser implements Predicate {
        final String _schregno;

        StudentChooser(final String schregno) {
            _schregno = schregno;
        }

        /** {@inheritDoc} */
        public boolean evaluate(final Object object) {
            if (object instanceof Record) {
                final Record record = (Record) object;
                if (record._schregno.equals(_schregno)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ======================================================================
    private static class MockDat {
        private final String _schregno;
        private final String _mockCd;
        private final String _mockSubclassCd;
        private final String _subClassAbbv;
        private final String _mockName;
        private final Integer _score;
        private final BigDecimal _deviation;

        public MockDat(
                final String schregno,
                final String mockCd,
                final String mockSubclassCd,
                final String subClassAbbv,
                final String mockName,
                final Integer score,
                final BigDecimal deviation
        ) {
            _schregno = schregno;
            _mockCd = mockCd;
            _mockSubclassCd = mockSubclassCd;

            _subClassAbbv = subClassAbbv;
            _mockName = mockName;
            _score = score;
            _deviation = deviation;
        }

        static List getStudentMock(final List buf, final String schregno) {
            final List rtn = new ArrayList();
            for (final Iterator it = buf.iterator(); it.hasNext();) {
                final MockDat mockDat = (MockDat) it.next();
                if (schregno.equals(mockDat._schregno)) {
                    rtn.add(mockDat);
                }
            }
            return rtn;
        }
        
        private static List loadMock(final DB2UDB db2, final Param param) throws SQLException {
            final List rtn = new ArrayList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlMock(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String mockCd = rs.getString("MOCKCD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subClassAbbv = rs.getString("SUBCLASS_ABBV");
                    final String mockName = rs.getString("MOCKNAME");
                    final Integer score = KNJServletUtils.getInteger(rs, "SCORE");
                    final BigDecimal deviation = rs.getBigDecimal("DEVIATION"); 

                    final MockDat mockDat = new MockDat(schregno, mockCd, subclassCd, subClassAbbv, mockName, score, deviation);
                    rtn.add(mockDat);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }
        
        private static String sqlMock(final Param param) {
            final StringBuffer rtn = new StringBuffer();
            rtn.append("SELECT");
            rtn.append("    t1.schregno,");
            rtn.append("    t1.mockcd,");
            rtn.append("    t1.mock_subclass_cd AS subclasscd,");
            rtn.append("    t3.subclass_abbv,");
            rtn.append("    t2.mockname2 AS mockname,");
            rtn.append("    t1.score,");
            rtn.append("    t1.deviation");
            rtn.append(" FROM");
            rtn.append("    mock_subclass_mst t3,");
            rtn.append("    mock_mst t2,");
            rtn.append("    mock_dat t1");
            rtn.append(" WHERE");
            rtn.append("    t1.year = '" + param._year + "' AND");
            rtn.append("    t3.mock_subclass_cd = t1.mock_subclass_cd AND");
            rtn.append("    t1.mockcd = t2.mockcd AND");
            rtn.append("    t1.deviation IS NOT NULL AND");
            rtn.append("    t2.mockname2 IS NOT NULL");
            rtn.append(" ORDER BY t1.mockcd, t1.mock_subclass_cd");
            return rtn.toString();
        }

        public String toString() {
            return _schregno + ":" + _subClassAbbv + ":" + _mockName;
        }
    }
    // ======================================================================
    private static class CreditMst {
        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final String _grade;

        private final String _subClassCd;
        private final Integer _credits;

        public CreditMst(
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String grade,
                final String subClassCd,
                final Integer credits
        ) {
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _grade = grade;

            _subClassCd = subClassCd;
            _credits = credits;
        }

        public String toString() {
            return _courseCode + ":" + _majorCd + ":" + _courseCd + "," + _grade + ":" + _subClassCd + "=" + _credits;
        }
        
        private static Integer getCredit(final Set creditMst, final Student student, final SubClass subClass) {
            for (final Iterator it = creditMst.iterator(); it.hasNext();) {
                final CreditMst cm = (CreditMst) it.next();

                if (!student._grade.equals(cm._grade)) {
                    continue;
                }
                if (!student._course._courseCd.equals(cm._courseCd)) {
                    continue;
                }
                if (!student._course._majorcd.equals(cm._majorCd)) {
                    continue;
                }
                if (!student._course._courseCode.equals(cm._courseCode)) {
                    continue;
                }

                if (!subClass._cd.equals(cm._subClassCd)) {
                    continue;
                }
                return cm._credits;
            }
            return null;
        }

        private static Set loadCreditMst(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Set rtn = new HashSet();
            try {
                ps = db2.prepareStatement(sqlCreditMst(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String grade = rs.getString("GRADE");
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final Integer credits = KNJServletUtils.getInteger(rs, "CREDITS");

                    final CreditMst creditMst = new CreditMst(courseCd, majorCd, courseCode, grade, subClassCd, credits);
                    rtn.add(creditMst);
                }
            } catch (final Exception ex) {
                log.error("単位マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("単位マスタの総数=" + rtn.size());
            return rtn;
        }

        private static String sqlCreditMst(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append("select");
            sql.append("    COURSECD,");
            sql.append("    MAJORCD,");
            sql.append("    COURSECODE,");
            sql.append("    GRADE,");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            sql.append("    SUBCLASSCD AS SUBCLASSCD,");
            sql.append("    CREDITS");
            sql.append("  from");
            sql.append("    CREDIT_MST");
            sql.append("  where");
            sql.append("    YEAR='" + param._year + "'");
            return sql.toString();
        }
    }

    private static class AverageDat {
        private String _chairCd;
        private String _subclassCd;
        private String _grade;
        private String _hrClass;
        private Course _course;

        private Integer _highScore;
        private Integer _lowScore;
        private Integer _count;
        private BigDecimal _avg;

        public String toString() {
            return _subclassCd + "/" + _highScore + "/" + _lowScore;
        }
    }

    private static class RankDat {
        private Exam _exam;
        private String _subclassCd;
        private String _schregno;

        private Integer _gradeRank;
        private BigDecimal _gradeDeviation;
        private Integer _courseRank;
        private BigDecimal _courseDeviation;
        private Integer _score;

        public String toString() {
            return _subclassCd + "/" + _schregno + "/" + _gradeRank + "/" + _courseRank;
        }
    }

    /**
     * servletpack.KNJZ.detail.ScoreInfoの代替。
     */
    private static class ScoreInfo {
        private Integer _high;
        private Integer _low;
        private BigDecimal _avg;
        private int _size;
        private Integer _rank;

        public ScoreInfo() {}

        public Integer getHigh() {
            return _high;
        }

        public Integer getLow() {
            return _low;
        }

        public BigDecimal getAvg() {
            return _avg;
        }

        public int size() {
            return _size;
        }
        
        public String toString() {
            return " ScoreInfo [avg = " + _avg + " / size = " + _size + " / rank = " + _rank + " / high = " + _high + " / low = " + _low + "]";
        }
    }
} // KNJD102B

// eof
