// kanji=漢字
/*
 * $Id: 61f06d78dda134686a9d52bb6dacb250b9b03ac6 $
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
import servletpack.KNJZ.detail.ScoreInfo;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

import jp.co.alp.kenja.common.dao.SQLUtils;

/**
 * 成績個人票。A3版。
 * @author takaesu
 * @version $Id: 61f06d78dda134686a9d52bb6dacb250b9b03ac6 $
 */
public class KNJD102B {
    /**
     * 定期考査表の1教科における最大科目数。
     */
    private static final int _TEIKIKOUSA_TABLE_KAMOKU_MAX = 3;

    /*pkg*/static final Log log = LogFactory.getLog(KNJD102B.class);

    private static final String FORM_FILE = "KNJD102_2.frm";
    final BasicStroke _outlineStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    final Font _font = new Font("TimesRoman", Font.PLAIN, 13);
    final Font _smallFont = new Font("TimesRoman", Font.PLAIN, 11);

    final String LATE_CODE = "15";
    final String EARLY_CODE = "16";
    /**
     * グラフイメージファイルの Set&lt;File&gt;
     */
    private final Set _graphFiles = new HashSet();

    private final Clazz[] _clazz;
    private Form _form;

    private boolean _hasData;
    private KNJSchoolMst _knjSchoolMst;

    /** 印刷日時 */
    final Calendar _now = Calendar.getInstance();

    final StudentChooser _studentChooser = new StudentChooser();

    Param _param;

    /** RECORD_DAT。DBから、ほとんど全てを取り込む。PGではこのListから必要分だけ抽出して使っている */
    List _records;

    private List _mockDat;

    /** 講座グループ */
    private MultiMap _chairGroup;

    /** 定期考査のグラフ. */
    private final TeikikousaChart[] _charts;

    /** 開始学期. */
    final static String SSEMESTER = "1";

    public KNJD102B() {
        _now.setTime(new Date());

        final Clazz kokugo = new Clazz("11", "国語");
        final Clazz sugaku = new Clazz("14", "数学");
        final Clazz eigo   = new Clazz("18", "外国語");
        final Clazz rika   = new Clazz("15", "理科");
        final Clazz syakai = new Clazz("12", "地理歴史");
        final Clazz koumin = new Clazz("13", "公民");

        _clazz = new Clazz[] {kokugo, sugaku, eigo, rika, syakai, koumin};
        _charts = createTeikikousaChart();
    }

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _param._year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _param.load(db2);
            _records = loadRecord(db2);
            log.info("RECORD_DAT size=" + _records.size());

            _mockDat = loadMock(db2);
            log.debug("mock_dat size=" + _mockDat.size());

            _chairGroup = loadChairGroup(db2);
            log.debug("講座グループデータのグループ数=" + _chairGroup.size());

            final List students = createStudents(db2);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug("------ 生徒:" + student + " ----:" + student.attend());

                final boolean enableStudent = printSvfAt(db2, student);
                if (enableStudent) {
                    _hasData = true;
                }
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

    private List loadRecord(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtn = new ArrayList();
        try {
            ps = db2.prepareStatement(sqlRecord());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subClassCd;
                if ("1".equals(_param._useCurriculumcd)) {
                    subClassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                } else {
                    subClassCd = rs.getString("SUBCLASSCD");
                }
                final Map chairCdMap = chairCodeMap(rs);   // 対象のテスト項目の講座コードマップ
                final String abbv = rs.getString("ABBV");
                final SubClass subClass = new SubClass(subClassCd, chairCdMap, abbv);

                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE"); // 生徒の学年

                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final Course course = new Course(courseCd, majorCd, courseCode);

                final Map examScoreMap = examScoreMap(rs);

                final Record record = new Record(
                        schregno,
                        grade,
                        course,
                        subClass,
                        examScoreMap
                );
                rtn.add(record);
            }
        } catch (final Exception ex) {
            log.error("成績データの取得でエラー", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /*
     * 講座コードを割当てる。
     */
    private Map chairCodeMap(final ResultSet rs) throws SQLException {
        final Map rtn = new TreeMap();

        for (final Iterator it = _param._examMaster.values().iterator(); it.hasNext();) {   // イテレートの順番が重要
            final Exam exam = (Exam) it.next();

            final String testKind = _param.getTestKind(exam.getTestCd());
            final String chairCd;
            final int semester = Integer.parseInt(exam._semester);
            switch (semester) {
            case 1:
                chairCd = rs.getString("SEM1_" + testKind + "_CHAIRCD");
                break;
            case 2:
                chairCd = rs.getString("SEM2_" + testKind + "_CHAIRCD");
                break;
            case 3:
                chairCd = rs.getString("SEM3_" + testKind + "_CHAIRCD");
                break;
            default:
                log.fatal("指定学期が不正な為、講座コードが得られない。:" + exam);
                chairCd = null;
                break;
            }
            if (chairCd != null) {
                rtn.put(exam, chairCd);
            }
        }
        return rtn;
    }

    /*
     * 試験の得点を割当てる。
     */
    private Map examScoreMap(final ResultSet rs) throws SQLException {
        final Map rtn = new TreeMap();

        for (final Iterator it = _param._examMaster.values().iterator(); it.hasNext();) {   // イテレートの順番が重要
            final Exam exam = (Exam) it.next();

            final String testKind = _param.getTestKind(exam.getTestCd());
            final Integer score;
            final int semester = Integer.parseInt(exam._semester);
            switch (semester) {
            case 1:
                score = KNJServletUtils.getInteger(rs, "SEM1_" + testKind + "_SCORE");
                break;
            case 2:
                score = KNJServletUtils.getInteger(rs, "SEM2_" + testKind + "_SCORE");
                break;
            case 3:
                score = KNJServletUtils.getInteger(rs, "SEM3_" + testKind + "_SCORE");
                break;
            default:
                log.fatal("指定学期が不正な為、得点が得られない。:" + exam);
                score = null;
                break;
            }
            rtn.put(exam, score);
        }
        return rtn;
    }

    private String sqlRecord() {
        //TAKAESU: 条件に学年を指定してもいいのかなぁ?
        String rtn;
        rtn = " select"
                + "    T1.SUBCLASSABBV as abbv,"
                + "    T2.*,"
                + "    T3.GRADE,"
                + "    T3.COURSECD,"
                + "    T3.MAJORCD,"
                + "    T3.COURSECODE"
                + " from"
                + "    V_SUBCLASS_MST T1,"
                + "    RECORD_DAT T2,"
                + "    SCHREG_REGD_DAT T3"
                + " where"
                + "    T1.YEAR = T2.YEAR and"
                + "    T1.YEAR = T3.YEAR and"
                + "    T2.SCHREGNO = T3.SCHREGNO and"
                + "    T1.SUBCLASSCD = T2.SUBCLASSCD and"
                + "    T2.TAKESEMES = '0' and"
                + "    T2.YEAR = '" + _param._year + "' and"
                + "    T3.SEMESTER = '" + _param._semester + "'"
                ;
        if ("1".equals(_param._useCurriculumcd)) {
            rtn += "   and T2.CLASSCD = T1.CLASSCD "
                +  "   and T2.SCHOOL_KIND = T1.SCHOOL_KIND "
                +  "   and T2.CURRICULUM_CD = T1.CURRICULUM_CD "
                ;
        }
        log.debug("RECORD_DAT の SQL=" + rtn);
        return rtn;
    }

    private boolean printSvfAt(
            final DB2UDB db2,
            final Student student
    ) throws SQLException {
        _studentChooser.set(student._schregno);
        final Collection studentRecords = CollectionUtils.select(_records, _studentChooser);
        log.info("生徒のRECORD_DAT size=" + studentRecords.size());

        // ヘッダー
        printHeader(student);

        // 分布票
        printBunpu(db2, studentRecords, student);

        // 出欠情報
        printAttend(student);

        // 定期考査
        printTeikikousa(studentRecords);

        // 模試
        if (!_param._disableMosi) {
            printMosi(student);
        }

        // 連絡欄
        if (null != student._remark) {
            KNJServletUtils.printDetail(_form._svf, "REMARK", student._remark, 30 * 2, 5);
        }

        _form.endPage();
        return true;
    }

    private void printMosi(final Student student) {
        log.debug("=== 模試 ===");
        final DefaultCategoryDataset dataset = createMockDataset(student);
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

    private TeikikousaChart[] createTeikikousaChart() {
        final TeikikousaChart[] teikikousaChart = new TeikikousaChart[5];

        teikikousaChart[0] = new TeikikousaChart(_clazz[0]);
        teikikousaChart[1] = new TeikikousaChart(_clazz[1]);
        teikikousaChart[2] = new TeikikousaChart(_clazz[2]);
        teikikousaChart[3] = new TeikikousaChart(_clazz[3]);
        teikikousaChart[4] = new TeikikousaChart(_clazz[4]);
        // 公民(13)との混合
        teikikousaChart[4].addClazz(_clazz[5]);
        teikikousaChart[4].setName("地歴・公民");

        return teikikousaChart;
    }

    private void printTeikikousa(final Collection studentRecords) {
        log.debug("=== 定期考査 ===");

        printTeikikousaTitle(_charts);

        for (final Iterator it = studentRecords.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();

            final Clazz clazz = getClazz(record._subClass);
            if (null == clazz) {
                continue;
            }
            // 教科から、対応したチャートを得る。
            for (int i = 0; i < _charts.length; i++) {
                final TeikikousaChart teikikousaChart = _charts[i];
                if (teikikousaChart.hasClazz(clazz)) {
                    teikikousaChart.add(record);
                    break;
                }
            }
        }

        int subClassIdx = 1;
        for (int i = 0; i < _charts.length; i++) {
            final TeikikousaChart teikikousaChart = _charts[i];
            if (!teikikousaChart.hasData()) {
                subClassIdx += _TEIKIKOUSA_TABLE_KAMOKU_MAX;
                continue;
            }
            teikikousaChart.commit();

            final File file = graphImageFile(teikikousaChart._chart, 990, 890);
            _graphFiles.add(file);
            _charts[i].svfOut(file, i + 1);

            subClassIdx = teikikousaChart.printTable(subClassIdx);
            teikikousaChart.reset();
        }
    }

    private Clazz getClazz(final SubClass subClass) {
        for (int i = 0; i < _clazz.length; i++) {
            if (subClass._cd.startsWith(_clazz[i]._cd)) {
                return _clazz[i];
            }
        }
        return null;
    }

    private void printTeikikousaTitle(final TeikikousaChart[] charts) {
        // 教科名
        for (int i = 0; i < charts.length; i++) {
            _form.svfOut("CLASSABBV1_1_" + (i + 1), charts[i]._name);   // グラフのタイトル
            _form.svfOutn("CLASSABBV1_2", (i + 1), charts[i]._name);   // 偏差値一覧表の教科タイトル
        }

        // 表の列タイトル
        int i = 1;
        for (final Iterator it = _param._examMaster.values().iterator(); it.hasNext();) {
            final Exam exam = (Exam) it.next();
            _form.svfOut("STDDIV1_" + i, exam._name);
            i++;
        }
    }

    /**
     * 出欠情報
     */
    private void printAttend(final Student student) {
        _form.svfOut("ABSENCE", String.valueOf(student._attend));   // 欠席
        _form.svfOut("LATE", String.valueOf(student._late));    // 遅刻
        _form.svfOut("LEAVE", String.valueOf(student._early));  // 早退

        // 印刷日時
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.M.d H:m");
        _form.svfOut("P_DATE", sdf.format(_now.getTime()));
    }

    private String createNendo() {
        final Integer year = Integer.valueOf(_param._year);
        final String nendo = KenjaProperties.gengou(year.intValue());
        return nendo + "年度";
    }

    private void printHeader(final Student student) {
        // 年度、学期、試験名称
        _form.svfOut("NENDO", createNendo());
        _form.svfOut("SEMESTER", _param._semesterName);
        _form.svfOut("TEST", _param.getExamName());

        // 生徒氏名
        _form.svfOut("HR_NAME", student.getNenkumiban());   // 年組番
        _form.svfOut("NAME", student._name);

        // 学級担任
        _form.svfOut("STAFFNAME", student._staffName);
    }

    private void printBunpu(
            final DB2UDB db2,
            final Collection studentRecords,
            final Student student
    ) throws SQLException {
        log.debug("=== 分布 ===");
        // 行タイトル
        printMainTableHeader();

        // 分布票データ
        int i = 1;
        final BunpuChart bunpuChart = new BunpuChart();

        final Collection subClasses = loadSubClasses(db2, student);
        log.debug("印字する科目=" + subClasses);
        log.debug("RECORD_DATの中身=" + studentRecords);
        student.loadKekka(db2);

        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            final Record record = getRecord(studentRecords, subClass);

            final ScoreInfo chairInfo = convert(new ChairChooser(record, _param.getCurrentExam()));
            final ScoreInfo gradeInfo = convert(new GradeChooser(record));
            final ScoreInfo courseInfo = convert(new CourseChooser(record));

            if (null != record && null != record._currentScore) {
                log.info("record=" + record + "/" + record._subClass._chairCdMap);

                // グラフデータ作成
                final ScoreInfo graphInfo = getGraphInfo(chairInfo, gradeInfo, courseInfo);
                if (graphInfo.size() != 0) {
                    bunpuChart.add(record, graphInfo);
                }
            }
            
            if (!subClass._attendSubclasscdSet.isEmpty()) {
                log.info("先科目 " + subClass._cd + ":" + subClass._abbv + " は科目一覧に表示しない");
                continue;
            }

            //
            final Integer credit = getCredit(student, subClass);

            // 
            ScoreInfo otherInfo = null; // グループ or 学年 or コース
            String mark = "";
            if (null != record) {
                final String chairCd = (String) record._subClass._chairCdMap.get(_param.getCurrentExam());
                final Collection groupChairs = getChairGroups(chairCd, _param.getCurrentExam());

                otherInfo = getOtherInfo(record, groupChairs, gradeInfo, courseInfo);
                mark = _param.mode1AsGakunen() && hasGroup(groupChairs) ? "*" : "";
            }

            // 印字
            final Number kekka = (Number) student._kekkaMap.get(subClass._cd);
            _form.printColumnData(subClasses, subClass, record, credit, kekka, chairInfo, otherInfo, mark);

            if (i++ >= Form.MAX_SUBCLASS_COUNT) {
                break;
            }
        }
        bunpuChart.commit();

        // 総合成績
        _form.printTotalScore();

        // 素点度数分布票グラフ
        printBunpuGraph(bunpuChart);
    }

    private Record getRecord(final Collection studentRecords, final SubClass subClass) {
        for (final Iterator it = studentRecords.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            if (record._subClass._cd.equals(subClass._cd)) {
                return record;
            }
        }
        return null;
    }

    private Integer getCredit(final Student student, final SubClass subClass) {
        for (final Iterator it = _param._creditMst.iterator(); it.hasNext();) {
            final CreditMst cm = (CreditMst) it.next();

            if (!student._grade.equals(cm._grade)) {
                continue;
            }
            if (!student._courseCd.equals(cm._courseCd)) {
                continue;
            }
            if (!student._majorCd.equals(cm._majorCd)) {
                continue;
            }
            if (!student._courseCode.equals(cm._courseCode)) {
                continue;
            }

            if (!subClass._cd.equals(cm._subClassCd)) {
                continue;
            }
            return cm._credits;
        }
        return null;
    }

    private Collection loadSubClasses(final DB2UDB db2, final Student student) throws SQLException {
        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlSubClasses(student));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subClasscd = rs.getString("SUBCLASSCD");

                final SubClass subClass = _param.getSubClass(subClasscd);
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

    private String sqlSubClasses(final Student student) {
        /** 対象外の教科コード。このコード以上のデータは対象外 */
        final int ILLEGAL_CLASS_CODE_VALUE = 91;

        final String rtn;
        rtn = "select"
                + "    distinct " + (("1".equals(_param._useCurriculumcd)) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD" : " SUBCLASSCD")
                + " from"
                + "    CHAIR_DAT T1 inner join CHAIR_STD_DAT T2 on"
                + "    T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.CHAIRCD = T2.CHAIRCD"
                + " where"
                + "    T1.YEAR = '" + _param._year + "' and"
                + "    T2.SCHREGNO = '" + student._schregno + "' and"
                + "    T2.APPDATE <= '" + _param._date + "' and"
                + "    int(substr(T1.SUBCLASSCD, 1, 2)) <= " + ILLEGAL_CLASS_CODE_VALUE
                ;
        log.debug("履修科目のSQL=" + rtn);
        return rtn;
    }

    private void printBunpuGraph(final BunpuChart bunpuChart) {
        _form.svfOut("DISTRI_TYPE", _param.getMode2Str());

        final File bunpuFile = graphImageFile(bunpuChart._chart, 3010, 1368);
        _graphFiles.add(bunpuFile);

        _form.svfOut("FRE_CHART", bunpuFile.toString());
    }

    private ScoreInfo getOtherInfo(
            final Record record,
            final Collection groupChairs,
            final ScoreInfo gradeInfo,
            final ScoreInfo courseInfo
    ) {
        if (_param.mode1AsGakunen()) {
            if (hasGroup(groupChairs)) {
                return convert(new GroupChooser(record, _param.getCurrentExam(), groupChairs));
            } else {
                return gradeInfo;
            }
        }
        return courseInfo;
    }

    private boolean hasGroup(final Collection groupChairs) {
        return (null != groupChairs) && (!groupChairs.isEmpty());
    }

    private Collection getChairGroups(final String chairCd, final Exam exam) {
        final Collection coll = (Collection) _chairGroup.get(exam.getKey());
        if (null == coll) {
            return null;
        }

        String found = null;
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            final ChairGroup cg = (ChairGroup) it.next();
            if (cg._chaircd.equals(chairCd)) {
                found = cg._groupCd;
            }
        }
        if (null == found) {
            return null;
        }
        final List rtn = new ArrayList();
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            final ChairGroup cg = (ChairGroup) it.next();
            if (cg._groupCd.equals(found)) {
                rtn.add(cg._chaircd);
            }
        }
        log.info("講座グループあり。学期+試験=" + exam.getKey() + ", 講座コード=" + chairCd + ", GroupCode=" + found + ": " + rtn);
        return rtn;
    }

    private ScoreInfo getGraphInfo(final ScoreInfo chairInfo, final ScoreInfo gradeInfo, final ScoreInfo courseInfo) {
        if (_param.mode2AsChair()) {
            return chairInfo;
        } else if (_param.mode2AsGrade()) {
            return  gradeInfo;
        }

        return courseInfo;
    }

    private void printMainTableHeader() {
        _form.bunpuReset();

        final String mode1Str = _param.getMode1Str();
        for (int i = 1; i <= 2; i++) {
            _form.svfOut("ITEM" + i + "_1", "得点");
            _form.svfOut("ITEM" + i + "_2", "欠課数");
            _form.svfOut("ITEM" + i + "_3", "講座平均");
            _form.svfOut("ITEM" + i + "_4", mode1Str + "平均");
            _form.svfOut("ITEM" + i + "_5", "講座順位");
            _form.svfOut("ITEM" + i + "_6", mode1Str + "順位");
        }

        _form.svfOutn("SUBCLASS2_2", 8, "総合成績");
    }

    private ScoreInfo convert(final Predicate chooser) {
        final Collection coll = CollectionUtils.select(_records, chooser);

        final ScoreInfo scoreInfo = new ScoreInfo(2);
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            final Record chairRecord = (Record) it.next();
            if (null == chairRecord._currentScore) {
                continue;
            }
            scoreInfo.add(chairRecord._currentScore);
        }
        return scoreInfo;
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlStudents());
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
            loadAttendSemes(db2, student);
            loadHexamRecordRemark(db2, student);
        }

        return rtn;
    }

    private void loadAttendSemes(final DB2UDB db2, final Student student) throws SQLException {
        final String sql = AttendAccumulate.getAttendSemesSql(
                _param._semesFlg,
                null,
                _knjSchoolMst,
                _param._year,
                SSEMESTER,
                _param._semester,
                (String) _param._hasuuMap.get("attendSemesInState"),
                _param._periodInState,
                (String) _param._hasuuMap.get("befDayFrom"),
                (String) _param._hasuuMap.get("befDayTo"),
                (String) _param._hasuuMap.get("aftDayFrom"),
                (String) _param._hasuuMap.get("aftDayTo"),
                student._grade,
                student._hrclass,
                student._schregno,
                "SEMESTER",
                _param._useCurriculumcd,
                _param._useVirus,
                _param._useKoudome
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

    private void loadHexamRecordRemark(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlHexamRecordRemark(student));
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

    private String sqlHexamRecordRemark(final Student student) {
        final String rtn;
        rtn = " select"
                + "  REMARK1"
                + " from"
                + "  HEXAM_RECORD_REMARK_DAT"
                + " where"
                + "  YEAR = '" + _param._year + "' and"
                + "  SEMESTER = '" + _param._semester + "' and"
                + "  TESTKINDCD = '" + _param.getCurrentExam()._kindCd + "' and"
                + "  TESTITEMCD = '" + _param.getCurrentExam()._itemCd + "' and"
                + "  SCHREGNO = '" + student._schregno + "' and"
                + "  REMARK_DIV='1'"
                ;
        return rtn;
    }

    private String sqlStudents() {
        final String students = SQLUtils.whereIn(true, _param._schregno);
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
                + "    T1.SEMESTER = '" + _param._semester + "' and"
                + "    T1.SCHREGNO in " + students
                + " order by"
                + "    T1.GRADE,"
                + "    T1.HR_CLASS,"
                + "    T1.ATTENDNO";
        log.info("生徒のSQL=" + rtn);
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

    private Param createParam(final HttpServletRequest request) {
        final Param param = new Param(request);
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        log.debug("固定の教科(指定順)");
        for (int i = 0; i < _clazz.length; i++) {
            log.debug("\t" + _clazz[i]);
        }

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
    }

    private List loadMock(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlMock());
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

    private DefaultCategoryDataset createMockDataset(final Student student) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        final List studentMock = MockDat.getStudentMock(_mockDat, student._schregno);
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

    private String sqlMock() {
        final String rtn;
        rtn = "SELECT"
                + "    t1.schregno,"
                + "    t1.mockcd,"
                + "    t1.mock_subclass_cd AS subclasscd,"
                + "    t3.subclass_abbv,"
                + "    t2.mockname2 AS mockname,"
                + "    t1.score,"
                + "    t1.deviation"
                + " FROM"
                + "    mock_subclass_mst t3,"
                + "    mock_mst t2,"
                + "    mock_dat t1"
                + " WHERE"
                + "    t3.mock_subclass_cd = t1.mock_subclass_cd AND"
                + "    t1.mockcd = t2.mockcd"
                + " ORDER BY t1.year, t1.mockcd, t1.mock_subclass_cd"
                ;
        return rtn;
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

    private MultiMap loadChairGroup(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final MultiMap rtn = new MultiHashMap();

        try {
            ps = db2.prepareStatement(sqlChairGroup());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String groupCd = rs.getString("CHAIR_GROUP_CD");
                final String testKind = rs.getString("TESTKINDCD");
                final String testItem = rs.getString("TESTITEMCD");
                final String key = semester + testKind + testItem;
                final Exam exam = (Exam) _param._examMaster.get(key);
                if (null == exam) {
                    log.debug("試験がマスターに無い:学期=" + semester + ", " + testKind + testItem);
                    continue;
                }
                final String chaircd = rs.getString("CHAIRCD");

                final ChairGroup chairGroup = new ChairGroup(exam, groupCd, chaircd);
                rtn.put(key, chairGroup);
            }
        } catch (final Exception ex) {
            log.error("講座グループデータの取得でエラー", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlChairGroup() {
        final String rtn;
        rtn = "SELECT"
                + "    semester,"
                + "    chair_group_cd,"
                + "    testkindcd,"
                + "    testitemcd,"
                + "    chaircd"
                + " FROM"
                + "    chair_group_dat"
                + " WHERE"
                + "    year = '" + _param._year + "'"
                ;
        return rtn;
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
        private int _totalScore;

        private final String _file;
        private Vrw32alp _svf;

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

            _totalCredit = _totalKekka = _totalScore = 0;
        }

        public void printColumnData(
                final Collection subClasses,
                final SubClass subClass,
                final Record record,
                final Integer credit,
                final Number kekka,
                final ScoreInfo chairInfo,
                final ScoreInfo otherInfo,
                final String mark
        ) {
            _subClassIndex++;
            if (_subClassIndex > FIRST_MAX_COL) {
                _subClassIndex = 1;
                _dan = 2;
            }

            // 科目名
            printSubClassName("SUBCLASS" + _dan, _subClassIndex, subClass._abbv);

            // 単位数
            if (null != credit) {
                svfOutn("CREDIT" + _dan, _subClassIndex, credit.toString());
            }

            // 欠課数
            if (null != kekka) {
                svfOutn("KEKKA" + _dan, _subClassIndex, String.valueOf(kekka.intValue()).toString());
            }

            // 得点
            if (null != record) {
                svfOutn("POINT" + _dan, _subClassIndex, record.getScoreStr());

                // 講座平均、講座順位/総数
                if (null != chairInfo && chairInfo.size() != 0) {
                    printChairData(record, chairInfo);
                }

                // xx平均、xx順位/総数
                if (null != otherInfo && otherInfo.size() != 0) {
                    printOtherData(record, otherInfo, mark);
                }
            }

            // 総合成績の算出
            if (null != credit) {
                _totalCredit += credit.intValue();
            }
            if (null != kekka && isAddKekka(subClasses, subClass)) {
                _totalKekka += kekka.intValue();
            }
            if (null != record && null != record._currentScore) {
                _totalScore += record._currentScore.intValue();
            }
        }

        private boolean isAddKekka(final Collection subClasses, final SubClass subClass) {
            // 対象科目が合併先科目で、かつ表示一覧に元科目を含む場合、総合成績の欠課に加算しない
            for (final Iterator it = subClass._attendSubclasscdSet.iterator(); it.hasNext();) {
                final String attendSubclasscd = (String) it.next();
                for (final Iterator its = subClasses.iterator(); its.hasNext();) {
                    final SubClass s = (SubClass) its.next();
                    if (s._cd.equals(attendSubclasscd)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void printOtherData(final Record record, final ScoreInfo otherInfo, final String mark) {
            /*
             * ★1 潤オ ★4 が全て有効だと数回に1度、PDFが壊れて開けない。
             * ★1 潤オ ★4 のいづれかをコメントアウトすると何故だか壊れないようだ。
             * '07.11.29: どうやら高江洲の実行環境だけのようだ。本番では問題無い模様。だけど心配だからコメント残す。
             */

            // xx平均
            final BigDecimal avg = otherInfo.getAvg().setScale(1, BigDecimal.ROUND_HALF_UP);
            svfOutn("AVERAGE" + _dan + "_2", _subClassIndex, mark + avg.toString());    // ★1

            // xx順位/総数
            final Integer score = record._currentScore;
            if (null != score) {
                final String rankStr = mark + otherInfo.rank(score.intValue()) + "/" + otherInfo.size();
                svfOutn("RANK" + _dan + "_2", _subClassIndex, rankStr); // ★2
            }
        }

        private void printChairData(final Record record, final ScoreInfo chairInfo) {
            // 講座平均
            final BigDecimal avg = chairInfo.getAvg().setScale(1, BigDecimal.ROUND_HALF_UP);
            svfOutn("AVERAGE" + _dan + "_1", _subClassIndex, avg.toString());   // ★3

            // 講座順位/総数
            final Integer score = record._currentScore;
            if (null != score) {
                final int rank = chairInfo.rank(score.intValue());
                svfOutn("RANK" + _dan + "_1", _subClassIndex, rank + "/" + chairInfo.size());   // ★4
            }
        }

        private void printSubClassName(final String field, final int subClassIndex, final String abbv) {
            final String type = abbv.length() <= 3 ? "_1" : "_2";
            svfOutn(field + type, subClassIndex, abbv); // 科目名
        }

        /**
         * 総合成績
         */
        public void printTotalScore() {
            svfOutn("CREDIT2" , 8, String.valueOf(_totalCredit));   // 単位数
            svfOutn("POINT2", 8, String.valueOf(_totalScore)); // 得点
            svfOutn("KEKKA2", 8, String.valueOf(_totalKekka));  // 欠課数
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

    private class Exam implements Comparable {
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

        public String getTestCd() {
            return _kindCd + _itemCd;
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
    }

    // ======================================================================
    private class Student {
        private final String _schregno;
        private final String _grade;
        private final String _hrclass;
        private final String _attendno;
        private final String _name;
        /** 組略称。 */
        private final String _hrNameAbbv;
        private final String _staffName;

        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;

        public int _attend = 0;
        public int _early = 0;
        public int _late = 0;

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

            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
        }

        public void loadKekka(final DB2UDB db2) throws SQLException {
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    _param._semesFlg,
                    null,
                    _param._defineSchool,
                    _knjSchoolMst,
                    _param._year,
                    SSEMESTER,
                    _param._semester,
                    (String) _param._hasuuMap.get("attendSemesInState"),
                    _param._periodInState,
                    (String) _param._hasuuMap.get("befDayFrom"),
                    (String) _param._hasuuMap.get("befDayTo"),
                    (String) _param._hasuuMap.get("aftDayFrom"),
                    (String) _param._hasuuMap.get("aftDayTo"),
                    _grade,
                    _hrclass,
                    _schregno,
                    "1",
                    _param._useCurriculumcd,
                    _param._useVirus,
                    _param._useKoudome
            );
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                if (!"9".equals(semester)) {
                    continue;
                }
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String ac = _knjSchoolMst._absentCov;
                final Number kekka;
                if ("3".equals(ac) || "4".equals(ac)) {
                    kekka = KNJServletUtils.getDouble(rs, "ABSENT_SEM");
                } else {
                    kekka = KNJServletUtils.getInteger(rs, "ABSENT_SEM");
                }
                _kekkaMap.put(subclassCd, kekka);
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
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String[] _schregno;
        private final String _testCd;
        private final String _date;
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        /** 順位、平均の対象。1=学年, 2=コース */
        private final String _mode1;
        /** 素点度数分布票の種類。 1=講座別, 2=学年別, 3=コース別 */
        private final String _mode2;
        private final boolean _disableMosi;
        private final Calendar _cal;

        /**
         * テスト項目マスタ。
         */
        private Map _examMaster = new TreeMap();

        private String _semesterName;

        private Set _creditMst = new HashSet();

        private Set _subClassMst = new HashSet();

        final KNJDefineSchool _defineSchool = new KNJDefineSchool();
        String _periodInState;
        Map _attendSemesMap;
        Map _hasuuMap;
        boolean _semesFlg;
        String _sDate;

        public Param(final HttpServletRequest request
        ) {
            // 順位、平均の対象
            final String mode1 = request.getParameter("MODE1"); // 1=学年, 2=コース

            // 素点度数分布票の種類
            final String mode2 = request.getParameter("MODE2"); // 1=講座別, 2=学年別, 3=コース別

            final String disableMosi = request.getParameter("DISABLE_MOSI");  // 1=模試を扱わない

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _schregno = request.getParameterValues("CATEGORY_SELECTED");
            _testCd = request.getParameter("TESTCD");
            _date = request.getParameter("DATE").replace('/', '-');
            _cal = getCalendar(_date);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _mode1 = mode1;
            _mode2 = mode2;

            _disableMosi = "1".equals(disableMosi) ? true : false;

            log.debug("模試を印字しないフラグ=" + disableMosi + ", " + _disableMosi);
            log.debug("順位,平均,偏差値の対象=" + _mode1 + ":" + getMode1Str());
            log.debug("素点度数分布票の種類=" + _mode2 + ":" + getMode2Str());
        }

        private Calendar getCalendar(final String date) {
            Calendar cal = null;
            try {
                cal = KNJServletUtils.parseDate(date);
            } catch (ParseException e) {
                log.error(date + " をCalendarに変換できない", e);
                return null;
            }

            return cal;
        }

        public SubClass getSubClass(final String subClasscd) {
            for (final Iterator it = _subClassMst.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                if (subClasscd.equals(subClass._cd)) {
                    return subClass;
                }
            }
            return null;
        }

        public boolean mode2AsChair() {
            return "1".equals(_mode2);
        }

        public boolean mode2AsGrade() {
            return "2".equals(_mode2);
        }

        public String getMode1Str() {
            return mode1AsGakunen() ? "学年" : "コース";
        }

        public boolean mode1AsGakunen () {
            return "1".equals(_mode1);
        }

        public String getMode2Str() {
            if ("1".equals(_mode2)) {
                return "(講座別)";
            } else if ("2".equals(_mode2)) {
                return "(学年別)";
            }
            return "(コース別)";
        }

        public String chairField() {
            final String testKind = getTestKind(getCurrentExam().getTestCd());
            return "SEM" + _semester + "_" + testKind + "_CHAIRCD";
        }

        public String getTestKind(final String testCd) {
            if ("0101".equals(testCd)) {
                return "INTR";
            }
            return "0201".equals(testCd) ? "TERM" : "TERM2";
        }

        public String getExamName() {
            final String examName = getCurrentExam()._name;
            return examName;
        }

        private Exam getCurrentExam() {
            return (Exam) _examMaster.get(_semester + _testCd);
        }

        private void loadExam(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlExam());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String kindCd = rs.getString("TESTKINDCD");
                    final String itemCd = rs.getString("TESTITEMCD");
                    final String testName = rs.getString("TESTITEMNAME");
                    final Exam exam = new Exam(semester, kindCd, itemCd, testName);
                    log.debug("テスト項目=" + exam + ":" + exam._name);

                    _examMaster.put(semester + kindCd + itemCd, exam);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlExam() {
            final String sql;
            sql = "select"
                    + "    SEMESTER,"
                    + "    TESTKINDCD,"
                    + "    TESTITEMCD,"
                    + "    TESTITEMNAME"
                    + "  from"
                    + "    TESTITEM_MST_COUNTFLG_NEW"
                    + "  where"
                    + "    YEAR='" + _year + "'"
                    + "    AND TESTKINDCD <> '99' "
                    + "  order by"
                    + "    SEMESTER, TESTKINDCD, TESTITEMCD"
            ;
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

        private void loadCreditMst(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCreditMst());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String grade = rs.getString("GRADE");
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final Integer credits = KNJServletUtils.getInteger(rs, "CREDITS");

                    final CreditMst creditMst = new CreditMst(courseCd, majorCd, courseCode, grade, subClassCd, credits);
                    _creditMst.add(creditMst);
                }
            } catch (final Exception ex) {
                log.error("単位マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("単位マスタの総数=" + _creditMst.size());
        }

        private String sqlCreditMst() {
            String sql;
            sql = "select"
                    + "    COURSECD,"
                    + "    MAJORCD,"
                    + "    COURSECODE,"
                    + "    GRADE,"
                    + (("1".equals(_param._useCurriculumcd)) ? " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD," : "    SUBCLASSCD,")
                    + "    CREDITS"
                    + "  from"
                    + "    CREDIT_MST"
                    + "  where"
                    + "    YEAR='" + _year + "'"
            ;
            return sql;
        }


        private void loadSubClassMst(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map subclassMstMap = new HashMap();
            try {
                ps = db2.prepareStatement(sqlSubClassMst());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    if (null == subclassMstMap.get(code)) {
                        final String abbv = rs.getString("SUBCLASSABBV");
                        final SubClass subClass = new SubClass(code, null, abbv);
                        subclassMstMap.put(code, subClass);
                    }
                    final SubClass subClass = (SubClass) subclassMstMap.get(code);
                    if (null != rs.getString("ATTEND_SUBCLASSCD")) {
                        subClass._attendSubclasscdSet.add(rs.getString("ATTEND_SUBCLASSCD"));
                    }
                }
            } catch (final Exception ex) {
                log.error("科目マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _subClassMst.addAll(subclassMstMap.values());
            log.debug("科目マスタの総数=" + _subClassMst.size());
        }

        private String sqlSubClassMst() {
            final String sql;
            sql = "select"
                    + (("1".equals(_param._useCurriculumcd)) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD," : "    T1.SUBCLASSCD,")
                    + "    T1.SUBCLASSABBV,"
                    + "    T3.ATTEND_SUBCLASSCD"
                    + "  from"
                    + "    V_SUBCLASS_MST T1"
                    + "    left join SUBCLASS_REPLACE_COMBINED_DAT T3 on T1.YEAR = T3.YEAR "
                    + "    and " + (("1".equals(_param._useCurriculumcd)) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD  = T3.COMBINED_CLASSCD || '-' || T3.COMBINED_SCHOOL_KIND || '-' || T3.COMBINED_CURRICULUM_CD || '-' || T3.COMBINED_SUBCLASSCD " : " T1.SUBCLASSCD = T3.COMBINED_SUBCLASSCD")
                    + "  where"
                    + "    T1.YEAR='" + _year + "'"
            ;
            return sql;
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
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

        public void load(final DB2UDB db2) throws SQLException, ParseException {
            loadExam(db2);
            loadSemester(db2);
            loadCreditMst(db2);
            loadSubClassMst(db2);

            // 出欠の情報
            _defineSchool.defineCode(db2, _year);
            final KNJDefineCode definecode0 = setClasscode0(db2);
            final String z010Name1 = setZ010Name1(db2);
            _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date);
            _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
        }

        public int getExamIndex(final Exam exam) {
            int i = 1;
            for (final Iterator it = _examMaster.values().iterator(); it.hasNext();) {
                final Exam e = (Exam) it.next();

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

            return 0;
        }
    }

    // ======================================================================
    /**
     * 教科。
     */
    private class Clazz {
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
    private class SubClass {
        private final String _cd;
        private final Map _chairCdMap;
        private final String _abbv;
        private final Set _attendSubclasscdSet;

        private final Map _mock = new HashMap();

        public SubClass(
                final String cd,
                final Map chairCdMap,
                final String abbv
        ) {
            _cd = cd;
            _chairCdMap = chairCdMap;
            _abbv = abbv;
            _attendSubclasscdSet = new HashSet();
        }

        public void addMock(final String mockName, final Integer score) {
            _mock.put(mockName, score);
        }

        public String toString() {
            return _cd + ":" + _abbv;
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
    private class TeikikousaChart {
        private static final String EMPTY_NAME = "";

        final DefaultCategoryDataset _dataset = new DefaultCategoryDataset();
        JFreeChart _chart;
        private final List _classes = new ArrayList();
        private String _name;
        private boolean _changeOnce = false;
        private boolean _hasGraphData = false;

        TeikikousaChart(final Clazz clazz) {
            setName(clazz._abbv);
            addClazz(clazz);
        }

        public void reset() {
            _dataset.clear();
            _changeOnce = false;
        }

        boolean hasClazz(final Clazz clazz) {
            for (final Iterator it = _classes.iterator(); it.hasNext();) {
                final Clazz hoge = (Clazz) it.next();
                if (hoge.equals(clazz)) {
                    return true;
                }
            }
            return false;
        }

        void addClazz(final Clazz clazz) {
            _classes.add(clazz);
        }

        void setName(final String name) {
            _name = name;
        }

        public void svfOut(final File file, final int n) {
            _form.svfOut("CHART1_" + n, file.toString());
        }

        public void add(final Record record) {
            // テストの ScoreInfo を算出
            record.setHensati();
            log.debug(record._subClass._abbv + "の試験毎の情報=" + record._hensatiMap);

            if (!_changeOnce) {
                setExamNamesOnGraph();
                _changeOnce = true;
            }
            // RECORD_DAT にぶら下がる、各テストの偏差値を Dataset にセットする
            for (final Iterator it = record._examScoreMap.keySet().iterator(); it.hasNext();) {
                final Exam exam = (Exam) it.next();

                // 得点の取得から偏差値の算出
                final Integer score = (Integer) record._examScoreMap.get(exam);
                if (null == score) {
                    continue;
                }
                final ScoreInfo si = (ScoreInfo) record._hensatiMap.get(exam);
                if (null == si) {
                    continue;
                }
                final Double stdScore = si.getStdScoreDouble(score.intValue());

                final Integer hensati;
                if (null == stdScore) {
                    hensati = null;
                } else {
                    final BigDecimal bd = new BigDecimal(String.valueOf(stdScore)).setScale(0, BigDecimal.ROUND_HALF_UP);
                    hensati = new Integer(bd.intValue());
                }

                // 偏差値のセット
                _dataset.addValue(hensati, record._subClass._abbv, exam);
                _hasGraphData = true;
            }
        }

        private void setExamNamesOnGraph() {
            final int examCount = _param.getExamCount();

            int i = 1;
            for (final Iterator it = _param._examMaster.values().iterator(); it.hasNext();) {
                final Exam exam = (Exam) it.next();

                if (i > examCount) {
                    return;
                }
                _dataset.addValue(null, EMPTY_NAME, exam);
                i++;
            }
        }

        public int printTable(final int subClassIdx) {
            
            int i = subClassIdx;
            for (final Iterator it = _dataset.getRowKeys().iterator(); it.hasNext();) {
                final String subClassName = (String) it.next();
                if (EMPTY_NAME.equals(subClassName)) {
                    continue;
                }
                _form.svfOutn("SUBCLASSABBV", i, subClassName);   // 科目名

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
                    final int examIndex = _param.getExamIndex(exam);
                    _form.svfOutn("POINT1_" + examIndex, i, value.toString());
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

    // ======================================================================
    private class Record {
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

        // -----------------------------------

        /** 学期・試験と得点の関連付け */
        private final Map _examScoreMap;
        /** 定期考査の偏差値情報 */
        private final Map _hensatiMap = new TreeMap();

        public Record(
                final String schregno,
                final String grade,
                final Course course,
                final SubClass subClass,
                final Map examScoreMap
        ) {
            _schregno = schregno;
            _grade = grade;
            _course = course;
            _subClass = subClass;
            _examScoreMap = examScoreMap;

            _currentScore = (Integer) examScoreMap.get(_param.getCurrentExam());
        }

        public void setHensati() {
            // ScoreInfo を算出
            for (final Iterator it = _examScoreMap.keySet().iterator(); it.hasNext();) {
                final Exam exam = (Exam) it.next();

                final Predicate chooser;
                if (_param.mode1AsGakunen()) {
                    final String chairCd = (String) _subClass._chairCdMap.get(exam);
                    final Collection groupChairs = getChairGroups(chairCd, exam);
                    if (hasGroup(groupChairs)) {
                        chooser = new GroupChooser(this, exam, groupChairs);
                    } else {
                        chooser = new GradeChooser(this);
                    }
                } else {
                    chooser = new CourseChooser(this);
                }
                final Collection coll = CollectionUtils.select(_records, chooser);
                final ScoreInfo scoreInfo = convert(coll, exam);    // 試験の得点を集めて ScoreInfoにする
                if (scoreInfo.size() == 0) {
                    _hensatiMap.put(exam, null);
                } else {
                    _hensatiMap.put(exam, scoreInfo);
                }
            }
        }

        private ScoreInfo convert(final Collection coll, final Exam exam) {
            final ScoreInfo scoreInfo = new ScoreInfo(2);
            for (final Iterator it = coll.iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                scoreInfo.add(record.getExamScore(exam));
            }
            return scoreInfo;
        }

        private Integer getExamScore(final Exam exam) {
            return (Integer) _examScoreMap.get(exam);
        }

        public String getScoreStr() {
            if (null != _currentScore) {
                return _currentScore.toString();
            }
            return null;
        }

        public String toString() {
            return _subClass._cd + ":" + _subClass._abbv + "/" + _currentScore;
        }
    }
    // ======================================================================
    private class Course {
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
        private String _schregno;

        public void set(final String schregno) {
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
    private abstract class Chooser {
        /* pkg */String _subClassCd;
    }

    // ======================================================================
    private class ChairChooser extends Chooser implements Predicate {
        private final Exam _exam;
        private final String _chairCd;

        public ChairChooser(final Record record, final Exam exam) {
            if (null == record) {
                _subClassCd = null;
                _exam = null;
                _chairCd = null;
            } else {
                _subClassCd = record._subClass._cd;
                _exam = exam;
                _chairCd = (String) record._subClass._chairCdMap.get(exam);
            }
        }

        /** {@inheritDoc} */
        public boolean evaluate(final Object object) {
            if (object instanceof Record && _exam != null) {
                final Record record = (Record) object;
                if (null == record._subClass._cd || null == record._subClass._chairCdMap || null == record._subClass._chairCdMap.get(_exam)) {
                    return false;
                }
                if (!record._subClass._cd.equals(_subClassCd)) {
                    return false;
                }
                if (record._subClass._chairCdMap.get(_exam).equals(_chairCd)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ======================================================================
    private class GroupChooser extends Chooser implements Predicate {
        private final Exam _exam;
        private final String _chairCd;
        private final Collection _groupChairs;

        public GroupChooser(final Record record, final Exam exam, final Collection groupChairs) {
            _subClassCd = record._subClass._cd;
            _exam = exam;
            _chairCd = (String) record._subClass._chairCdMap.get(exam);

            _groupChairs = groupChairs;
            if (!hasGroup(groupChairs)) {
                log.fatal("講座グループがあるはずなのに無い!");
            }
        }

        /** {@inheritDoc} */
        public boolean evaluate(final Object object) {
            if (object instanceof Record) {
                final Record record = (Record) object;
                if (null == record._subClass._cd || null == record._subClass._chairCdMap || null == record._subClass._chairCdMap.get(_exam)) {
                    return false;
                }
                if (!record._subClass._cd.equals(_subClassCd)) {
                    return false;
                }
                if (_groupChairs.contains(record._subClass._chairCdMap.get(_exam))) {
                    return true;
                }
            }
            return false;
        }
    }
    // ======================================================================
    private class GradeChooser extends Chooser implements Predicate {
        private String _grade;

        public GradeChooser(final Record record) {
            if (null == record) {
                _subClassCd = null;
                _grade = null;
            } else {
                _subClassCd = record._subClass._cd;
                _grade = record._grade;
            }
        }

        /** {@inheritDoc} */
        public boolean evaluate(final Object object) {
            if (object instanceof Record) {
                final Record record = (Record) object;
                if (null == record._subClass._cd || null == record._grade) {
                    return false;
                }
                if (!record._subClass._cd.equals(_subClassCd)) {
                    return false;
                }
                if (record._grade.equals(_grade)) {
                    return true;
                }
            }
            return false;
        }
    }
    // ======================================================================
    private class CourseChooser extends Chooser implements Predicate {
        private Course _course;

        public CourseChooser(final Record record) {
            if (null == record) {
                _subClassCd = null;
                _course = null;
            } else {
                _subClassCd = record._subClass._cd;
                _course = record._course;
            }
        }

        /** {@inheritDoc} */
        public boolean evaluate(final Object object) {
            if (object instanceof Record) {
                final Record record = (Record) object;
                if (null == record._subClass._cd || null == record._course) {
                    return false;
                }
                if (!record._subClass._cd.equals(_subClassCd)) {
                    return false;
                }
                if (record._course.equals(_course)) {
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

        /**
         * @deprecated 学内の偏差値ではないとの事。偏差値は MOCK_DAT.DEVIATION から得るので廃止。
         * @param buf
         * @param mockDat
         * @return
         */
        public static Integer getStdScore(final List buf, final MockDat mockDat) {
            final ScoreInfo scoreInfo = new ScoreInfo(2);

            for (final Iterator it = buf.iterator(); it.hasNext();) {
                final MockDat wrk = (MockDat) it.next();
                if (!mockDat._mockCd.equals(wrk._mockCd)) {
                    continue;
                }
                if (!mockDat._mockSubclassCd.equals(wrk._mockSubclassCd)) {
                    continue;
                }
                scoreInfo.add(wrk._score);
            }
//            log.debug(mockDat._mockName + "/" + mockDat._subClassAbbv + "のScoreInfo=" + scoreInfo);

            final double result = scoreInfo.getStdScore(mockDat._score.intValue());

            final BigDecimal bd = new BigDecimal(String.valueOf(result)).setScale(0, BigDecimal.ROUND_HALF_UP);
            final Integer rtn = new Integer(bd.intValue());
            return rtn;
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

        public String toString() {
            return _schregno + ":" + _subClassAbbv + ":" + _mockName;
        }
    }
    // ======================================================================
    private class CreditMst {
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
    }

    private class ChairGroup {
        final Exam _exam;
        final String _groupCd;
        final String _chaircd;

        public ChairGroup(
                final Exam exam,
                final String groupCd,
                final String chairCd
        ) {
            _exam = exam;
            _groupCd = groupCd;
            _chaircd = chairCd;
        }
    }
} // KNJD102B

// eof
