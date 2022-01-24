// kanji=漢字
/*
 * $Id: db92ebb8accf040716b1af6944f91ec780de1059 $
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
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
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: db92ebb8accf040716b1af6944f91ec780de1059 $
 */
public class KNJD105C {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD105C.class);

    /** レーダーチャート凡例画像. */
    private static final String RADER_CHART_LEGEND = "RaderChartLegend.png";
    /** 棒グラフ凡例画像1(学年). */
    private static final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
    /** 棒グラフ凡例画像2(コース). */
    private static final String BAR_CHART_LEGEND2 = "BarChartLegendCourse.png";

    /** 成績表の最大科目数. */
    private static final int TABLE_SUBCLASS_MAX = 20;
    /** 棒グラフの最大科目数. */
    private static final int BAR_GRAPH_MAX_ITEM = 11;
    /** 生徒氏名のフォント切替の境目文字数. */
    private static final int STUDENT_NAME_DELIMTER_COUNT = 10;

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";

    /** 中間のテスト種別コード。 */
    private static final String CYUKAN_KINDCD = "01";
    /** 期末のテスト種別コード。 */
    private static final String KIMATU_KINDCD = "02";

    /** 中間用フォーム(グラフ表示)。 */
    private static final String FORM_FILE1bg  = "KNJD105C_1.frm";
    /** 期末用フォーム(グラフ表示)。 */
    private static final String FORM_FILE2b  = "KNJD105C_2.frm";
    /** 中間用フォーム(グラフ表示・保護者欄無し)。 */
    private static final String FORM_FILE1b  = "KNJD105C_3.frm";

    /** 中間用フォーム(得点分布表示)。 */
    private static final String FORM_FILE1dg = "KNJD105C_4.frm";
    /** 期末用フォーム(得点分布表示)。 */
    private static final String FORM_FILE2d  = "KNJD105C_5.frm";
    /** 中間用フォーム(得点分布表示・保護者欄無し)。 */
    private static final String FORM_FILE1d  = "KNJD105C_6.frm";

    /** 中間用フォーム(グラフ表示・得点分布表示)。 */
    private static final String FORM_FILE1bdg  = "KNJD105C_7.frm";
    /** 期末用フォーム(グラフ表示・得点分布表示)。 */
    private static final String FORM_FILE2bd  = "KNJD105C_8.frm";
    /** 中間用フォーム(グラフ表示・得点分布表示・保護者欄無し)。 */
    private static final String FORM_FILE1bd  = "KNJD105C_9.frm";
    
    private static final int PRINT_SUBCLASS_BAR_CHART = 1;
    private static final int PRINT_SUBCLASS_RADAR_CHART = 2;
    private static final int PRINT_SUBCLASS_SCORE = 3;

    private Param _param;
    private boolean _hasData;
    private Form _form;
    private DB2UDB _db2;

    /** 科目マスタ。 */
    private Map _subClasses;

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);
        final String dbName = request.getParameter("DBNAME");
        _db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        if (openDb(_db2)) {
            return;
        }

        log.error("★マスタ関連の読込み");
        _param.load(_db2);
        _form = new Form(_param, response);

        // 対象の生徒たちを得る
        final String[] schregnos = _param.getScregnos(_db2);
        final List students = createStudents(schregnos);
        _hasData = students.size() > 0;

        final Exam exam = new Exam(_param, _param.getKindCd(), false, _subClasses);
        final Exam beforeExam = new Exam(_param, CYUKAN_KINDCD, true, _subClasses);
        
        // 成績のデータを読む
        log.error("★成績関連の読込み");
        loadSubClasses(_db2, students);
        exam.load(_db2, students);

        if (_param.isKimatu()) {
            log.error("★成績関連の読込み(前回の中間成績)");
            beforeExam.load(_db2, students);
        }

        // 印刷する
        log.error("★印刷");
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug("☆" + student + ", 科目の数=" + student._subclasses.size() + ", コースキー=" + student.courseKey());
            log.debug("今回の成績: " + student._record.values());
            if (_param.isKimatu()) {
                log.debug("前回の成績: " + student._beforeRecord.values());
            }

            if (_param._useSubclassGroup) {
                _param.setSubclasses(student);
            }
            _form.resetForm();
            _form.printStatic(_param, student);
            _form.printRecord(_param, student, exam, beforeExam);
        }

        log.error("★終了処理");
        _form.closeSvf();
        closeDb(_db2);
        removeImageFiles();
        log.info("Done.");
    }

    private void loadSubClasses(final DB2UDB db2, final List students) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlSubClasses());
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) _subClasses.get(subClasscd);
                    if (null != subClass) {
                        student._subclasses.put(subClasscd, subClass);
                    }
                }
            }
        } catch (final SQLException e) {
            log.fatal("履修科目の取得でエラー");
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private String sqlSubClasses() {
        final StringBuffer rtn = new StringBuffer();
        rtn.append(" select");
        rtn.append("   distinct ");
        if ("1".equals(_param._useCurriculumcd)) {
            rtn.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        rtn.append("   T1.SUBCLASSCD AS SUBCLASSCD ");
        rtn.append("  from");
        rtn.append("   CHAIR_DAT T1 inner join CHAIR_STD_DAT T2 on");
        rtn.append("   T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.CHAIRCD = T2.CHAIRCD");
        rtn.append("  where");
        rtn.append("   T1.YEAR = '" + _param._year + " ' and");
        rtn.append("   T2.SCHREGNO = ? and");
        rtn.append("   T2.APPDATE <= '" + _param._loginDate + " '");
        
        log.debug("履修科目のSQL=" + rtn.toString());
        return rtn.toString();
    }

    private List createStudents(final String[] schregnos) throws SQLException {
        final List rtn = new LinkedList();
        final String sql = studentsSQL(schregnos);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = _db2.prepareStatement(sql);
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
                final String name = rs.getString("name");
                final String zipCd = rs.getString("zipcd");
                final String addr1 = rs.getString("addr1");
                final String addr2 = rs.getString("addr2");
                final String addressee = rs.getString("addressee");
                final String remark = rs.getString("remark1");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        attendno,
                        hrName,
                        coursecd,
                        majorcd,
                        coursecode,
                        name,
                        zipCd,
                        addr1,
                        addr2,
                        addressee,
                        remark
                );
                rtn.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー");
            throw e;
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        Collections.sort(rtn);
        return rtn;
    }

    private String studentsSQL(final String[] selected) {
        final String students = SQLUtils.whereIn(true, selected);

        final StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append("  t1.schregno,");
        sql.append("  t1.grade,");
        sql.append("  t1.hr_class,");
        sql.append("  t1.attendno,");
        sql.append("  t1.hr_name,");
        sql.append("  t1.coursecd,");
        sql.append("  t1.majorcd,");
        sql.append("  t1.coursecode,");
        sql.append("  t1.name,");
        if ("3".equals(_param._useAddress)) {
            sql.append("  t2.guarantor_zipcd as zipcd,");
            sql.append("  t2.guarantor_addr1 as addr1,");
            sql.append("  t2.guarantor_addr2 as addr2,");
            sql.append("  t2.guarantor_name as addressee,");
        } else if ("4".equals(_param._useAddress)) {
            sql.append("  t4.send_zipcd as zipcd,");
            sql.append("  t4.send_addr1 as addr1,");
            sql.append("  t4.send_addr2 as addr2,");
            sql.append("  t4.send_name as addressee,");
        } else {
            sql.append("  t2.guard_zipcd as zipcd,");
            sql.append("  t2.guard_addr1 as addr1,");
            sql.append("  t2.guard_addr2 as addr2,");
            sql.append("  t2.guard_name as addressee,");
        }
        sql.append("  t3.remark1");
        sql.append(" FROM");
        sql.append("  v_schreg_info t1 LEFT JOIN guardian_dat t2 ON t1.schregno=t2.schregno");
        sql.append("    LEFT JOIN hexam_record_remark_dat t3 ON");
        sql.append("      t1.year=t3.year AND");
        sql.append("      t3.semester='" + _param._semester + "' AND");
        sql.append("      t3.testkindcd='" + _param.getKindCd() + "' AND");
        sql.append("      t3.testitemcd='" + _param.getItemCd() + "' AND");
        sql.append("      t1.schregno=t3.schregno AND");
        sql.append("      t3.remark_div='2'");// '2'固定
        if ("4".equals(_param._useAddress)) {
            sql.append("    LEFT JOIN schreg_send_address_dat t4 ON");
            sql.append("      t4.schregno=t1.schregno and t4.div='1' ");
        }
        sql.append(" WHERE");
        sql.append("  t1.year='" + _param._year + "' AND");
        sql.append("  t1.semester='" + _param.getRegdSemester() + "' AND");
        sql.append("  t1.schregno IN " + students);
        return sql.toString();
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
        log.fatal("$Revision: 56895 $ $Date: 2017-11-01 20:22:17 +0900 (水, 01 11 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
        final Param param = new Param(request);
        return param;
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
        final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

        /*
         * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
         * 拡大すると粗くなってしまうから。
         */
        return (int) (pixel * 1.3);
    }

    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlSeme;
        private final String _testCd;
        private final String _grade;

        /** 学年対象か? false ならコース対象。 */
        private final boolean _isGakunen;

        /** [クラス指定 or 生徒指定]の値。 */
        private final String _div;
        /** クラス or 生徒。 */
        private final String[] _values;

        private final String _useAddress;
        private final String _submitDate;
        private final String _loginDate;

        /** クラス指定 or 生徒指定。 */
        private final boolean _isClassMode;

        /** 西暦/和暦フラグ。 */
        private boolean _isWareki;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 偏差値を印字するか? */
        private final boolean _deviationPrint;
        /** 基準点に平均点を使用するか? */
        private boolean _useAverageAsKijunten;

        private final Map _staffs = new HashMap();
        private String _examName;
        private String _semesterName;

        /** 成績文面データ。全体評。 */
        private Map _document = new HashMap();

        /** 教科マスタ。 */
        private Map _classes;
        private final String _imagePath;

        private final boolean _useSubclassGroup;
        private final Map _subclassGroup3 = new HashMap();
        private final Map _subclassGroup5 = new HashMap();
        private final Map _subclassGroup9 = new HashMap();
        private final MultiMap _subclassGroupDat3 = new MultiHashMap();
        private final MultiMap _subclassGroupDat5 = new MultiHashMap();
        private final MultiMap _subclassGroupDat9 = new MultiHashMap();

        /** レーダーチャートの科目. */
        private List _fiveSubclass = new ArrayList();
        private List _threeSubclass = new ArrayList();
        private List _nineSubclass = new ArrayList();

        private final List _classOrder = new ArrayList();

        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;

        /** 得点分布表を表示するか */
        private final boolean _isPrintDistribution;
        
        private final String _useGraph; // 1:グラフ 2:得点分布 3:両方

        /** 保護者欄を表示するか */
        private final boolean _isPrintGuardianComment;
        
        /** 欠点 */
        private final int _borderScore;
        
        /** 100点満点に換算する */
        private final boolean _isConvertScoreTo100;
        
        /** 教育課程コード */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        
        private boolean _isChibenNaraCollege; // 智辯奈良カレッジ校ならtrue

        public Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _testCd = request.getParameter("TESTCD");
            _div = request.getParameter("CATEGORY_IS_CLASS");
            _values = request.getParameterValues("CATEGORY_SELECTED");
            _useAddress = request.getParameter("USE_ADDRESS");
            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");

            _isClassMode = "1".equals(request.getParameter("CATEGORY_IS_CLASS")) ? true : false;

            final String groupDiv = request.getParameter("GROUP_DIV");// 1=学年, 2=コース
            _isGakunen = "1".equals(groupDiv) ? true : false;
            _deviationPrint = "1".equals(request.getParameter("DEVIATION_PRINT"));

            _imagePath = request.getParameter("IMAGE_PATH");
            _useSubclassGroup = "1".equals(request.getParameter("SUBCLASS_GROUP"));
            _useAverageAsKijunten = "2".equals(request.getParameter("OUTPUT_KIJUN"));
            log.debug(" record_rank_dat use *_avg_rank ? = " + _useAverageAsKijunten);
            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            
            _useGraph = request.getParameter("USE_GRAPH");
            _isPrintDistribution = "2".equals(_useGraph) || "3".equals(_useGraph);
            _isPrintGuardianComment = "1".equals(request.getParameter("USE_HOGOSYA"));
            _borderScore = "".equals(request.getParameter("KETTEN")) || request.getParameter("KETTEN") == null ? 0 : Integer.parseInt(request.getParameter("KETTEN"));;
            _isConvertScoreTo100 = "1".equals(request.getParameter("KANSAN"));
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
        }

        private void imageFileCheck(final String fName) {
            final File f = new File(_imagePath + "/" + fName);
            if (!f.exists()) {
                log.fatal("画像ファイルが無い!⇒" + _imagePath + "/" + fName);
            }
        }

        public String getKindCd() {
            return _testCd.substring(0, 2);
        }

        public String getItemCd() {
            return _testCd.substring(2);
        }
        
        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSeme : _semester;
        }

        public String[] getScregnos(final DB2UDB db2) throws SQLException {
            final String separator = "-";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List result = new ArrayList();

            if (!_isClassMode) {
                return _values;
            }

            // 年組から学籍番号たちを得る
            for (int i = 0; i < _values.length; i++) {
                final String grade = StringUtils.split(_values[i], separator)[0];
                final String room = StringUtils.split(_values[i], separator)[1];
                
                try {
                    ps = db2.prepareStatement(hrToStudentsSQL(grade, room));
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("schregno");
                        result.add(schregno);
                    }
                } catch (final SQLException e) {
                    log.error("年組から学籍番号たちを得る際にエラー");
                    throw e;
                }
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final Iterator it = result.iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                rtn[i++] = schregno;
            }

            return rtn;
        }

        public void load(final DB2UDB db2) throws SQLException {
            loadIsJunior(db2);
            loadZ010(db2);
            loadWarekiFlg(db2);
            loadCertifSchool(db2);
            loadRegdHdat(db2);
            loadSemester(db2);
            loadExam(db2);
            loadDocumentDat(db2);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);
            setCombinedOnSubClass(db2);

            loadClassOrder(db2);
            
            if (_useSubclassGroup) {
                // rec_subclass_group_dat を読込んで _fiveSubclass や _threeSubclass の箱に入れる
                // TODO: rec_subclass_group_dat を読込んで 「高校の時、表に出さない集計科目」を求める⇒本当?データに下4桁がALLゼロないけど...
                loadSubclassGroup(db2);
                loadSubclassGroupDat(db2);
            } else {
                loadThreeOrFiveSubclass(db2, "5", "D006", "003", _fiveSubclass);
                loadThreeOrFiveSubclass(db2, "3", "D005", "002", _threeSubclass);
            }
        }
        
        public boolean isUnprint集計科目(final String subclassCd) {
            if (!_useSubclassGroup && subclassCd.endsWith("0000")) {
                final Set classCds = new HashSet();
                for (final Iterator it = _fiveSubclass.iterator(); it.hasNext();) {
                    final String subclass5Cd = (String) it.next();
                    classCds.add(subclass5Cd.substring(0, 2));
                }
                for (final Iterator it = _threeSubclass.iterator(); it.hasNext();) {
                    final String subclass3Cd = (String) it.next();
                    classCds.add(subclass3Cd.substring(0, 2));
                }
                if (classCds.contains(subclassCd.substring(0, 2))) {
                    return true;
                }
            }
            return false;
        }

        private void loadZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String name1 = rs.getString("NAME1");
                    final String schoolcd = rs.getString("NAME2");
                    _isChibenNaraCollege = "CHIBEN".equals(name1) && "30290086001".equals(schoolcd); // 30290086001は智辯ならカレッジの学校コード
                    log.fatal("カレッジ? " + _isChibenNaraCollege);
                }
            } catch (final Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
            final Set sakiSet = new HashSet();
            final Set motoSet = new HashSet();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCombined());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combined = rs.getString("COMBINED_SUBCLASSCD");
                    final String attend = rs.getString("ATTEND_SUBCLASSCD");
                    sakiSet.add(combined);
                    motoSet.add(attend);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // 合併先
            for (final Iterator it = sakiSet.iterator(); it.hasNext();) {
                final String saki = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(saki);
                if (null != subClass) {
                    subClass.setSaki();
                }
            }
            // 合併元
            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
                final String moto = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(moto);
                if (null != subClass) {
                    subClass.setMoto();
                }
            }
        }

        public String sqlCombined() {
            final StringBuffer stb = new StringBuffer();
            stb.append("select distinct");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD");
            stb.append(" from SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append(" where");
            stb.append("  YEAR = '" + _year + "'");
            return stb.toString();
        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
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
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        public String sqlSubClasses() {
            final StringBuffer stb = new StringBuffer();
            stb.append("select");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("   SUBCLASSCD AS SUBCLASSCD,");
            stb.append("   coalesce(SUBCLASSORDERNAME2, SUBCLASSNAME ) as NAME,");
            stb.append("   SUBCLASSABBV");
            stb.append(" from V_SUBCLASS_MST");
            stb.append(" where");
            stb.append("   YEAR = '" + _year + "'");
            stb.append(" order by");
            stb.append("   SUBCLASSCD");
            return stb.toString();
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
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
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        public String sqlClasses() {
            final StringBuffer stb = new StringBuffer();
            stb.append("select");
            stb.append("   CLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   || '-' || SCHOOL_KIND ");
            }
            stb.append("     AS CLASSCD,");
            stb.append("   CLASSNAME,");
            stb.append("   CLASSABBV");
            stb.append(" from V_CLASS_MST");
            stb.append(" where");
            stb.append("   YEAR = '" + _year + "'");
            stb.append(" order by");
            stb.append("   CLASSCD");
            return stb.toString();
        }

        private void loadDocumentDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  grade,");
            sql.append("  coursecd,");
            sql.append("  majorcd,");
            sql.append("  coursecode,");
            sql.append("  footnote");
            sql.append(" FROM");
            sql.append("  record_document_dat");
            sql.append(" WHERE");
            sql.append("  year='" + _param._year + "' AND");
            sql.append("  semester='" + _param._semester + "' AND");
            sql.append("  testkindcd='" + _param.getKindCd() + "' AND");
            sql.append("  testitemcd='" + _param.getItemCd() + "' AND");
            sql.append("  grade='" + _param._grade + "' AND");
            sql.append("  subclasscd='999999'");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
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
                log.error("成績文面データ取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _semesterName = "";
                final String sql = "SELECT value(semestername, '') AS semestername FROM semester_mst"
                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
                    ;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semesterName = rs.getString("semestername");
                }
            } catch (final SQLException e) {
                log.error("学期名取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadExam(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _examName = "";
                final String sql = "SELECT value(testitemname, '') AS testitemname FROM testitem_mst_countflg_new"
                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
                    + " AND testkindcd='" + getKindCd() + "' AND testitemcd='" + getItemCd() + "'"
                    ;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _examName = rs.getString("testitemname");
                }
            } catch (final SQLException e) {
                log.error("考査名取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadRegdHdat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year=t2.year AND t1.tr_cd1=t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + getRegdSemester() + "'";
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("staffname");
                    _staffs.put(code, name);
                }
            } catch (final SQLException e) {
                log.warn("担任名の取得でエラー");
            } finally {
                DbUtils.closeQuietly(rs);
            }
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
                log.error("学校名取得エラー。");
                throw e;
            }
        }

        private void loadClassOrder(final DB2UDB db2) {
            final String sql;
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                sql = "SELECT classcd || '-' || school_kind AS classcd FROM class_detail_dat"
                        + " WHERE year='" + _param._year + "' AND class_seq='004' "
                        + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
                        ;
            } else {
                final String field1 = _isJunior ? "name1" : "name2";
                final String field2 = _isJunior ? "namespare1" : "namespare2";
                sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
                    + " WHERE year='" + _param._year + "' AND namecd1='D009' AND " + field1 + " IS NOT NULL "
                    + " ORDER BY " + field2
                    ;
            }

            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _classOrder.add(rs.getString("classcd"));
                }
            } catch (final SQLException e) {
                log.error("教科表示順取得エラー。");
            }
            log.debug("教科表示順=" + _classOrder);
        }

        private void loadWarekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT name1 FROM v_name_mst WHERE year='" + _param._year + "' AND namecd1='Z012'");
                rs = ps.executeQuery();
                rs.next();
                final String name1 = rs.getString("name1");
                _isWareki = "2".equals(name1) ? true : false;
            } catch (final SQLException e) {
                log.error("西暦/和暦フラグ取得エラー。");
            }
            log.debug("和暦フラグ=" + _isWareki);
        }

        private void loadThreeOrFiveSubclass(final DB2UDB db2, final String hoge, final String key, final String classSeq, final List output) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql;
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    sql = "SELECT classcd || '-' || school_kind AS name FROM class_detail_dat WHERE year='" + _param._year + "' AND class_seq = '" + classSeq + "' order by int(value(class_remark1, '99')), classcd || '-' || school_kind ";
                } else {
                    final String field = _isJunior ? "name1" : "name2";
                    sql = "SELECT " + field + " AS name FROM v_name_mst WHERE year='" + _param._year + "' AND namecd1='" + key + "'";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("name");
                    if (null == subclassCd) {
                        continue;
                    }
                    output.add(subclassCd);
                }
            } catch (final SQLException e) {
                log.error(hoge + "科目取得エラー。");
            }

            log.debug(hoge + "科目=" + output);
        }

        private void loadIsJunior(DB2UDB db2) {
            _isJunior = Integer.parseInt(_grade) < 4;
            log.debug("中学校?:" + _isJunior);
            _isHigh = !_isJunior;
        }
        
        private String hrToStudentsSQL(final String grade, final String room) {
            return " select"
                    + "    SCHREGNO as schregno"
                    + " from"
                    + "    SCHREG_REGD_DAT"
                    + " where"
                    + "    YEAR = '" + _year + "' and"
                    + "    SEMESTER = '" + getRegdSemester() + "' and"
                    + "    GRADE = '" + grade + "' and"
                    + "    HR_CLASS = '" + room + "'"
                    ;
        }

        
        public String getWholeRemark(final Student student) {
            final String rtn = (String) _document.get(student._grade + student._courseCd + student._majorCd + student._courseCode);
            return rtn;
        }

        /**
         * 中間か?
         * @return 期末なら false
         */
        public boolean isChuukan() {
            return CYUKAN_KINDCD.equals(getKindCd());
        }

        /**
         * 期末か?
         * @return 中間なら false
         */
        public boolean isKimatu() {
            return KIMATU_KINDCD.equals(getKindCd());
        }

        public boolean isUnderScore(final String score) {
            if (StringUtils.isEmpty(score)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            return val < _borderScore;
        }

        /**
         * 教科コードが含まれているか?
         * @param classcd 教科コード
         * @return 含まれていれば true
         */
        public boolean fiveSubclassContains(final String classcd) {
            for (final Iterator it = _fiveSubclass.iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                if (subclassCd.startsWith(classcd)) {
                    return true;
                }
            }
            return false;
        }

        String get3title(final String courseKey) {
            if (_useSubclassGroup) {
                final String rtn = (String) _subclassGroup3.get(courseKey);
                return (null == rtn) ? "???" : rtn;
            }
            return "３教科";
        }

        String get5title(final String courseKey) {
            if (_useSubclassGroup) {
                final String rtn = (String) _subclassGroup5.get(courseKey);
                return rtn;
            }
            return "５教科";
        }

        public void setSubclasses(final Student student) {
            _fiveSubclass = (List) _subclassGroupDat5.get(student.courseKey());
            if (null == _fiveSubclass) {
                _fiveSubclass = Collections.EMPTY_LIST;
            }
            _threeSubclass = (List) _subclassGroupDat3.get(student.courseKey());
            if (null == _threeSubclass) {
                _threeSubclass = Collections.EMPTY_LIST;
            }
            _nineSubclass = (List) _subclassGroupDat9.get(student.courseKey());
            if (null == _nineSubclass) {
                _nineSubclass = Collections.EMPTY_LIST;
            }
        }

        private void loadSubclassGroup(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = subclassGroupSQL();
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
                    } else if ("5".equals(groupDiv)) {
                        _subclassGroup5.put(key, groupName);
                    } else {
                        _subclassGroup9.put(key, groupName);
                    }
                }
            } catch (final SQLException e) {
                log.error("科目取得エラー。");
            }

            log.debug("3教科の名称たち=" + _subclassGroup3);
            log.debug("5教科の名称たち=" + _subclassGroup5);
            log.debug("9教科の名称たち=" + _subclassGroup9);
        }

        private String subclassGroupSQL() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  group_div,");
            stb.append("  coursecd,");
            stb.append("  majorcd,");
            stb.append("  coursecode,");
            stb.append("  group_name");
            stb.append(" FROM");
            stb.append("  rec_subclass_group_mst");
            stb.append(" WHERE");
            stb.append("  year='" + _year + "' AND");
            stb.append("  grade='" + _grade + "' AND");
            stb.append("  group_div in ('3', '5', '9')");
            return stb.toString();
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
                        _subclassGroupDat3.put(key, subclassCd);
                    } else if ("5".equals(groupDiv)) {
                        _subclassGroupDat5.put(key, subclassCd);
                    } else {
                        _subclassGroupDat9.put(key, subclassCd);
                    }
                }
            } catch (final SQLException e) {
                log.error("rec_subclass_group_dat の取得エラー");
            }

            log.debug("3教科の科目CDたち=" + _subclassGroupDat3);
            log.debug("5教科の科目CDたち=" + _subclassGroupDat5);
            log.debug("9教科の科目CDたち=" + _subclassGroupDat9);
        }

        private String subclassGroupDatSQL() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  group_div,");
            stb.append("  coursecd,");
            stb.append("  majorcd,");
            stb.append("  coursecode,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("  subclasscd AS subclasscd ");
            stb.append(" FROM");
            stb.append("  rec_subclass_group_dat");
            stb.append(" WHERE");
            stb.append("  year='" + _year + "' AND");
            stb.append("  grade='" + _grade + "' AND");
            stb.append("  group_div in ('3', '5', '9')");   // 3教科, 5教科, 9教科
            return stb.toString();
        }

        
        private String getAverageKey(final String subclasscd, final String courseKey) {
            final String key;
            if (_isGakunen) {
                key = subclasscd;
            } else {
                key = subclasscd + courseKey;
            }
            return key;
        }
        
        private String getDistKey(final String grade, final String courseKey) {
            final String key;
            if (_isGakunen) {
                key = grade;
            } else {
                key = grade + courseKey;
            }
            return key;
        }
        
        private String getRankField() {
            if (_isGakunen) {
                return "grade_rank";
            }
            return "course_rank";
        }
        
        private String getDeviationField() {
            if (_param._isGakunen) {
                return "grade_deviation";
            }
            return "course_deviation";
        }
        
        private String getMsg() {
            final String msg;
            if (_isGakunen) {
                msg = "学年";
            } else {
                msg = "コース";
            }
            return msg;
        }
        
        private String getBarLegendImage() {
            final String barLegendImage;
            if (_isGakunen) {
                barLegendImage = BAR_CHART_LEGEND1;
            } else {
                barLegendImage = BAR_CHART_LEGEND2;
            }
            return barLegendImage;
        }

        public String getRecordScoreDiv(final String kindCd) {
            return "99".equals(kindCd) ? "00" : "01";
        }
    }

    private class Form {
        private Vrw32alp _svf;
        private final String _file;
        private final Param _param;

        /** メインの成績(サブフォーム). */
        private final MainRecTable _mainTable = new MainRecTable();
        /** 前回の中間成績. */
        private final MainRecTable _subTable;
        
        private int charSize = 6;

        public Form(final Param param, final HttpServletResponse response) throws IOException {
            _param = param;
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _file = getFormFile();
            log.debug("フォームファイル=" + _file);

            _subTable = _param.isKimatu() ? new SubRecTable() : null;
        }
        
        private String getFormFile() {
            final String file;
            if (_param._isChibenNaraCollege && !(_param.isKimatu() || _param.isChuukan())) {
                file = "3".equals(_param._useGraph) ? FORM_FILE2bd : "2".equals(_param._useGraph) ? FORM_FILE2d : FORM_FILE2b;
                charSize = 7;
            } else if (_param.isKimatu()) {
                file = "3".equals(_param._useGraph) ? FORM_FILE2bd : "2".equals(_param._useGraph) ? FORM_FILE2d : FORM_FILE2b;
                charSize = 7;
            } else {
                if (_param._isPrintGuardianComment) {
                    file = "3".equals(_param._useGraph) ? FORM_FILE1bdg : "2".equals(_param._useGraph) ? FORM_FILE1dg : FORM_FILE1bg;
                } else {
                    file = "3".equals(_param._useGraph) ? FORM_FILE1bd : "2".equals(_param._useGraph) ? FORM_FILE1d : FORM_FILE1b;
                }
                charSize = 6;
            }
            return file;
        }

        public void resetForm() {
            final int sts = _svf.VrSetForm(_file, 4);
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }
        }

        public void printStatic(final Param param, final Student student) {
            _svf.VrsOut("SCHOOL_NAME", param._schoolName);

            final String staffName = (String) param._staffs.get(student._grade + student._hrClass);
            _svf.VrsOut("STAFFNAME", param._remark2 + staffName);

            if (!"1".equals(param._useAddress) && !_param.isKimatu()) {
                _svf.VrsOut("ZIPCD", student._zipCd);
                final String addr1 = student._addr1;
                final String addr2 = student._addr2;
                final String field = (null != addr1 && addr1.length() > 25) || (null != addr2 && addr2.length() > 25) ? "_3" : (null != addr1 && addr1.length() > 19) || (null != addr2 && addr2.length() > 19) ? "_2" : "";
                _svf.VrsOut("ADDR1" + field, addr1);
                _svf.VrsOut("ADDR2" + field, addr2);
                if (null != student._addressee) {
                    final String setAddressee = student._addressee + "  様";
                    final String field2 = setAddressee.length() > 13 ? "_2" : "";
                    _svf.VrsOut("ADDRESSEE" + field2, setAddressee);
                }
            }
        
            final String nendo;
            if (param._isWareki) {
                nendo = KenjaProperties.gengou(Integer.parseInt(_param._year));
            } else {
                nendo = _param._year;
            }
            _svf.VrsOut("NENDO", nendo + "年度　" + _param._semesterName + "　" + _param._examName);
        
            final int attendNo = Integer.parseInt(student._attendNo);
            _svf.VrsOut("HR_NAME", student._hrName + attendNo + "番");

            if (student._name.length() <= STUDENT_NAME_DELIMTER_COUNT) {  // 全角で規定文字数を超えたらフォントを変える
                _svf.VrsOut("NAME", student._name);
            } else {
                _svf.VrsOut("NAME_2", student._name);
            }

            if (!_param.isKimatu()) {
                // 全体評
                KNJServletUtils.printDetail(_svf, "WHOLE_REMARK", param.getWholeRemark(student), 45 * 2, 7);

                // 提出日
                try {
                    final Calendar cal = KNJServletUtils.parseDate(param._submitDate.replace('/', '-'));
                    final String month = (cal.get(Calendar.MONTH) + 1) + "月";
                    final String day = cal.get(Calendar.DATE) + "日";
                    _svf.VrsOut("DATE", month + day);
                } catch (final ParseException e) {
                    log.error("提出日が解析できない");
                    _svf.VrsOut("DATE", param._submitDate);
                }
            }
            // 個人評(期末の場合は注意事項)
            KNJServletUtils.printDetail(_svf, "PERSONAL_REMARK", student._remark, 45 * 2, 3);

            final String get3title = null == _param.get3title(student.courseKey()) ? null : (_param.get3title(student.courseKey()) + "平均");
            _svf.VrsOut("ITEM_TOTAL3", get3title);
            final String get5title = null == _param.get5title(student.courseKey()) ? null : (_param.get5title(student.courseKey()) + "平均");
            if (null != get5title) {
                _svf.VrsOut("ITEM_TOTAL5", get5title);
            }
            if (_param._isPrintDistribution) {
                _svf.VrsOut("ITEM_TOTAL3_1", get3title);
                _svf.VrsOut("ITEM_TOTAL5_1", get5title);
            }

            _svf.VrsOut("ITEM_AVG", _param.getMsg() + "平均");
            _svf.VrsOut("ITEM_RANK", _param.getMsg() + "順位");

            if (_param._isPrintDistribution) {
                _svf.VrsOut("BAR_TITLE", "度数分布");
            } else {
                // 画像
                _svf.VrsOut("RADER_LEGEND", _param._imagePath + "/" + RADER_CHART_LEGEND);
                _svf.VrsOut("BAR_LEGEND", _param._imagePath + "/" + _param.getBarLegendImage());

                _svf.VrsOut("BAR_TITLE", "得点グラフ");
                _svf.VrsOut("RADER_TITLE", "教科間バランス");
            }
        }

        /**
         * 成績部分の印刷
         * @param param パラメータ
         * @param student 生徒
         */
        public void printRecord(final Param param, final Student student, final Exam exam, final Exam beforeExam) {

            // 3教科 & 5教科
            _mainTable.printRec3and5(student._recordOther, exam, student);
            if (_param.isKimatu()) {
                // 前回の中間成績
                _subTable.printRec3and5(student._beforeRecordOther, beforeExam, student);
            }

            if ("1".equals(param._useGraph) || "3".equals(param._useGraph)) {
                // グラフ印字(サブフォームよりも先に印字)
                printBarGraph(student, exam._averageDat);
            }
            if ("1".equals(param._useGraph)) {
                printRadarGraph(student._record);
            }
            // 成績
            for (int line = 1; line <= TABLE_SUBCLASS_MAX; line++) {
                _mainTable.print(param._classes, student, exam, student._record, line);

                if (_param.isKimatu()) {
                    // 前回の中間成績
                    _subTable.print(param._classes, student, beforeExam, student._beforeRecord, line);
                }
                _svf.VrEndRecord();
            }
            _svf.VrPrint();
        }

        public void printBarGraph(final Student student, final Map averages) {
            // グラフ用のデータ作成
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            int i = 0;
            for (final Iterator it = student._record.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();

                if (!isPrintSubclass(record._subClass, PRINT_SUBCLASS_BAR_CHART, record)) {
                    continue;
                }

                scoreDataset.addValue(record._score, "得点", record._subClass._abbv);
                final AverageDat avgDat = (AverageDat) averages.get(_param.getAverageKey(record._subClass._code, student.courseKey()));
                final BigDecimal avg = (null == avgDat) ? null : avgDat._avg;

                avgDataset.addValue(avg, "平均点", record._subClass._abbv);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + ", 平均=" + avg);
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }

            // チャート作成
            final JFreeChart chart = createBarChart(scoreDataset, avgDataset);

            // グラフのファイルを生成
            final int w = "3".equals(_param._useGraph) ? 2850 : 1940;
            final int h = "3".equals(_param._useGraph) ? 790 : 930;
            final File outputFile = graphImageFile(chart, w, h);
            _graphFiles.add(outputFile);

            // グラフの出力
            _svf.VrsOut("BAR_LABEL", "得点");
            _svf.VrsOut("BAR", outputFile.toString());
        }
        
        public boolean is集計科目(final SubClass subClass) {
            if (_param._isJunior) {
                return subclassGroupContains(subClass);
            } else {
                return subClass._code.endsWith("0000");
            }
        }

        private boolean subclassGroup9Contains(final SubClass subClass) {
            return _param._isChibenNaraCollege && !(_param.isChuukan() || _param.isKimatu()) && _param._nineSubclass.contains(subClass._code);
        }

        private boolean subclassGroup5Contains(final SubClass subClass) {
            final boolean hasSubclass5 = _param._fiveSubclass.contains(subClass._code);
            return hasSubclass5;
        }

        private boolean subclassGroupContains(final SubClass subClass) {
            final boolean hasSubclass5 = _param._fiveSubclass.contains(subClass._code);
            final boolean hasSubclass3 = _param._threeSubclass.contains(subClass._code);
            return hasSubclass5 || hasSubclass3;
        }

        private JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset
        ) {
            final boolean showLegend = "3".equals(_param._useGraph);
            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, showLegend, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            if ("3".equals(_param._useGraph)) {
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

        private void printRadarGraph(final Map records) {
            // データ作成
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final Iterator it0 = _param._classOrder.iterator(); it0.hasNext();) {
                final String classCd = (String) it0.next();
                
                for (final Iterator it = records.values().iterator(); it.hasNext();) {
                    final Record record = (Record) it.next();
                    if (!classCd.equals(record._subClass.getClassKey(_param))) {
                        continue;
                    }
                    if (!isPrintSubclass(record._subClass, PRINT_SUBCLASS_RADAR_CHART, record)) {
                        continue;
                    }
                    setDataset(dataset, record);
                }
            }

            // チャート作成
            final JFreeChart chart = createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = graphImageFile(chart, 930, 822);
            _graphFiles.add(outputFile);

            // グラフの出力
            if (0 < dataset.getColumnCount()) {
                _svf.VrsOut("RADER", outputFile.toString());
            }
        }

        private boolean isPrintSubclass(final SubClass subClass, final int flg, final Record record) {
            boolean rtn = false;
            if (PRINT_SUBCLASS_SCORE == flg) {
                if (_param._isChibenNaraCollege) {
                    if (null != record) {
                        return true;
                    }
                } else if (_param._isJunior) {
                    if (_param.fiveSubclassContains(subClass.getClassCd()) && !"90".equals(subClass.getClassCd())) {
                        rtn = true;
                    }
                } else if (_param._isHigh) {
                    if (subclassGroupContains(subClass) && !_param.isUnprint集計科目(subClass._code) && !"90".equals(subClass.getClassCd())) {
                        rtn = true;
                    }
                }
            } else if (PRINT_SUBCLASS_BAR_CHART == flg) {
                if (subClass._isMoto) {
                    rtn = false;
                } else if (_param._isChibenNaraCollege) {
                    return subclassGroup5Contains(subClass);
                } else if (is集計科目(subClass)) {
                    rtn = false;
                } else {
                    if (_param._isJunior) {
                        if (_param.fiveSubclassContains(subClass.getClassCd())) {
                            rtn = true;
                        }
                    } else if (_param._isHigh) {
                        if (subclassGroupContains(subClass)) {
                            rtn = true;
                        }
                    }
                }
            } else if (PRINT_SUBCLASS_RADAR_CHART == flg) {
                if (_param._isChibenNaraCollege) {
                    if (is集計科目(subClass)) {
                        rtn = true;
                    }
                } else if (_param._isJunior) {
                    if (_param._fiveSubclass.contains(subClass._code)) {
                        rtn = true;
                    }
                } else if (_param._isHigh) {
                    if (is集計科目(subClass)) {
                        rtn = true;
                    }
                }
            }
            return rtn;
        }
        
        private void setDataset(final DefaultCategoryDataset dataset, final Record record) {
            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
            final Class clazz = (Class) _param._classes.get(record._subClass.getClassKey(_param));
            final String name = (null == clazz) ? "???" : clazz._abbv;
            dataset.addValue(record._deviation, "本人偏差値", name);// TODO: MAX80, MIN20
            
            dataset.addValue(50.0, "偏差値50", name);
        }

        private JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
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

        void kasen(final String field, final String value) {
            _svf.VrAttribute(field, "UnderLine=(0,1,1)");
            _svf.VrsOut(field, value);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }
            _svf.VrQuit();
        }

        /**
         * 成績サブフォーム。
         */
        private class MainRecTable {
            String getFClass() { return "CLASS"; }
            String getFClass1() { return "CLASS1"; }
            String getFSubClass() { return "SUBCLASS"; }
            String getFSubClass_1() { return "SUBCLASS_1"; }
            String getFSubClass_2() { return "SUBCLASS_2"; }
            String getFSubClass1() { return "SUBCLASS1"; }
            String getFSubClass1_1() { return "SUBCLASS1_1"; }
            String getFSubClass1_2() { return "SUBCLASS1_2"; }
            String getFScore() { return "SCORE"; }
            String getFRank() { return "RANK"; }
            String getFDeviation() { return "DEVIATION"; }
            String getFAverage() { return "AVERAGE"; }
            String getFMaxScore() { return "MAX_SCORE"; }
            /** 受験者数 */
            String getFExaminee() { return "EXAMINEE"; }
            String getFSum_() { return "SUM"; }

            /*
             * 3教科
             */
            String getFScore3() { return "SCORE3"; }
            String getFRank3() { return "RANK3"; }
            String getFDeviation3() { return "DEVIATION3"; }
            String getFAverage3() { return "AVERAGE3"; }
            String getFMaxScore3() { return "MAX_SCORE3"; }
            String getFExaminee3() { return "EXAMINEE3"; }
            String getFAverage3_() { return "AVERAGE3_"; }

            /*
             * 5教科
             */
            String getFScore5() { return "SCORE5"; }
            String getFRank5() { return "RANK5"; }
            String getFDeviation5() { return "DEVIATION5"; }
            String getFAverage5() { return "AVERAGE5"; }
            String getFMaxScore5() { return "MAX_SCORE5"; }
            String getFExaminee5() { return "EXAMINEE5"; }
            String getFAverage5_() { return "AVERAGE5_"; }

            public void print(final Map classMst, final Student student, final Exam exam, final Map records, final int line) {
                
                int i = 0;
                for (final Iterator it = student._subclasses.values().iterator(); it.hasNext();) {
                    final SubClass subClass = (SubClass) it.next();

                    final Record record = (Record) records.get(subClass);
                    if (!isPrintSubclass(subClass, PRINT_SUBCLASS_SCORE, record)) {
                        continue;
                    }
                    i++;
                    if (i != line) {
                        continue;
                    }
                    final AverageDat avgDat = (AverageDat) exam._averageDat.get(_param.getAverageKey(subClass._code, student.courseKey()));
                    
                    ScoreDistribution dist = (ScoreDistribution) exam._scoreDistributions.get(_param.getDistKey(student._grade, student.courseKey()));

                    printRecord(classMst, subClass, record, avgDat, dist);
                }
            }

            private void printRecord(
                    final Map classMst,
                    final SubClass subClass,
                    final Record record,
                    final AverageDat avgDat,
                    final ScoreDistribution dist
            ) {
                // 教科
                final Class clazz = (Class) _param._classes.get(subClass.getClassKey(_param));

//                if (_fiveClassCd.contains(clazz._code)) {
//                    amikake(getFClass(), clazz._name);
//                } else {
//                    _svf.VrsOut(getFClass(), clazz._name);
//                }
                final String classstr = _param._isJunior ? clazz._name : clazz._abbv;
                _svf.VrsOut(getFClass(), classstr);

                // 科目
                final boolean isAmikake = !_param._isChibenNaraCollege && is集計科目(subClass);
                printSubclassName(getFSubClass(), getFSubClass_1(), getFSubClass_2(), isAmikake, subClass);

                if (_param._isPrintDistribution && null != dist) {
                    _svf.VrsOut(getFClass1(), classstr);

                    // 科目
                    printSubclassName(getFSubClass1(), getFSubClass1_1(), getFSubClass1_2(), isAmikake, subClass);
                }

                if (null == record) {
                    return;
                }

                final String score = record.getScore();
                if (_param.isUnderScore(score)) {
                    _svf.VrsOut(getFScore(), "(" + score + ")");
                } else {
                    _svf.VrsOut(getFScore(), score);
                }

                _svf.VrsOut(getFRank(),      record.getRank());
                if (_param._deviationPrint) {
                    _svf.VrsOut(getFDeviation(), record.getDeviation());
                }

                if (null != avgDat) {
                    _svf.VrsOut(getFAverage(),  avgDat.getAvgStr());
                    _svf.VrsOut(getFMaxScore(), avgDat._highScore.toString());
                    _svf.VrsOut(getFExaminee(), avgDat._count.toString());    // 受験者数
                }
                
                if (_param._isPrintDistribution && null != dist) {
                    final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(subClass._code);

                    ScoreCount[] scoreCounts = ssd._scoreCounts;
                    int total = 0;
                    for (int i = 0; i < scoreCounts.length; i++) {
                        final Integer count = ssd.getCount(scoreCounts[i]._key);
                        _svf.VrsOut(getFSum_() + distSuf(i), count.toString());
                        // log.debug(" 得点分布 (" + subClass + "):" + scoreKeys[i] + " = " + count);
                        total += count.intValue();
                    }
                    // log.debug(" total = " + total);
                }
            }
            private void printSubclassName(final String field0, final String field1, final String field2, final boolean isAmikake, final SubClass subClass) {
                final boolean useField12 = (null != subClass._name && charSize < subClass._name.length());
                if (isAmikake) {
                    if (useField12) {
                        _svf.VrAttribute(field1, "Paint=(2,70,1),Bold=1");
                        _svf.VrAttribute(field2, "Paint=(2,70,1),Bold=1");
                    } else {
                        _svf.VrAttribute(field0, "Paint=(2,70,1),Bold=1");
                    }
                }
                if (useField12) {
                    if (subClass._name.length() > 8) {
                        _svf.VrsOut(field1, subClass._name.substring(0, 8));
                        _svf.VrsOut(field2, subClass._name.substring(8));
                    } else {
                        _svf.VrsOut(field1, subClass._name);
                    }
                } else {
                    _svf.VrsOut(field0, subClass._name);
                }
                if (isAmikake) {
                    if (useField12) {
                        _svf.VrAttribute(field1, "Paint=(0,0,0),Bold=0");
                        _svf.VrAttribute(field2, "Paint=(0,0,0),Bold=0");
                    } else {
                        _svf.VrAttribute(field0, "Paint=(0,0,0),Bold=0");
                    }
                }
            }

            /**
             * 3,5教科の印字
             */
            public void printRec3and5(final Map recordOther, final Exam exam, final Student student) {

                printRecordRec3(recordOther, exam, student);

                //TAKAESU: 以下は分かりにくい。リファクタせよ!
                if (_param._useSubclassGroup) {
                    if (null != _param._subclassGroup5.get(student.courseKey())) {
                        printRecordRec5(recordOther, exam, student);
                    }
                } else {
                    printRecordRec5(recordOther, exam, student);
                }
            }

            private void printRecordRec3(final Map recordOther, final Exam exam, Student student) {
                
                final Record rec3 = (Record) recordOther.get(ALL3);
                if (null != rec3) {
                    _svf.VrsOut(getFScore3(), _param._isConvertScoreTo100 ? rec3.getAvg() : rec3.getScore());
                    _svf.VrsOut(getFRank3(), rec3.getRank());
                    if (_param._deviationPrint) {
                        _svf.VrsOut(getFDeviation3(), rec3.getDeviation());
                    }
                }
                final AverageDat avg3 = (AverageDat) exam._averageDatOther.get(_param.getAverageKey(ALL3, student.courseKey()));
                if (_param._isConvertScoreTo100) {
                    if (null != exam._convertedScore) {
                        _svf.VrsOut(getFAverage3(),  exam._convertedScore.getAvg("3", student));
                        _svf.VrsOut(getFMaxScore3(), exam._convertedScore.getHighscoreAvg("3", student));
                    }
                } else {
                    if (null != avg3) {
                        _svf.VrsOut(getFAverage3(),  avg3.getAvgStr());
                        _svf.VrsOut(getFMaxScore3(), avg3._highScore.toString());
                    }
                }
                if (null != avg3) {
                    _svf.VrsOut(getFExaminee3(), avg3._count.toString());
                }
                final ScoreDistribution dist = (ScoreDistribution) exam._scoreDistributions.get(_param.getDistKey(student._grade, student.courseKey()));
                if (_param._isPrintDistribution && null != dist) {
                    final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(ALL3);
                    ScoreCount[] scoreCounts = ssd._scoreCounts;
                    int total = 0;
                    for (int i = 0; i < scoreCounts.length; i++) {
                        final Integer count = ssd.getCount(scoreCounts[i]._key);
                        _svf.VrsOut(getFAverage3_() + distSuf(i), count.toString());
                        // log.debug(" 得点分布 (３教科):" + scoreKeys[i] + " = " + count);
                        total += count.intValue();
                    }
                    // log.debug(" total = " + total);
                }
            }

            private void printRecordRec5(final Map recordOther, final Exam exam, Student student) {
                
                final Record rec5 = (Record) recordOther.get(ALL5);
                if (null != rec5) {
                    _svf.VrsOut(getFScore5(), _param._isConvertScoreTo100 ? rec5.getAvg() : rec5.getScore());
                    _svf.VrsOut(getFRank5(),      rec5.getRank());
                    if (_param._deviationPrint) {
                        _svf.VrsOut(getFDeviation5(), rec5.getDeviation());
                    }
                }
                final AverageDat avg5 = (AverageDat) exam._averageDatOther.get(_param.getAverageKey(ALL5, student.courseKey()));
                if (_param._isConvertScoreTo100) {
                    if (null != exam._convertedScore) {
                        _svf.VrsOut(getFAverage5(),  exam._convertedScore.getAvg("5", student));
                        _svf.VrsOut(getFMaxScore5(), exam._convertedScore.getHighscoreAvg("5", student));
                    }
                } else {
                    if (null != avg5) {
                        _svf.VrsOut(getFAverage5(),  avg5.getAvgStr());
                        _svf.VrsOut(getFMaxScore5(), avg5._highScore.toString());
                    }
                }
                if (null != avg5) {
                    _svf.VrsOut(getFExaminee5(), avg5._count.toString());
                }
                final ScoreDistribution dist = (ScoreDistribution) exam._scoreDistributions.get(_param.getDistKey(student._grade, student.courseKey()));
                if (_param._isPrintDistribution && null != dist) {
                    final SubclassScoreDistribution ssd = dist.getSubclassDistributionMap(ALL5);
                    ScoreCount[] scoreCounts = ssd._scoreCounts;
                    int total = 0;
                    for (int i = 0; i < scoreCounts.length; i++) {
                        final Integer count = ssd.getCount(scoreCounts[i]._key);
                        _svf.VrsOut(getFAverage5_() + distSuf(i), count.toString());
                        // log.debug(" 得点分布 (５教科):" + scoreKeys[i] + " = " + count);
                        total += count.intValue();
                    }
                    // log.debug(" total = " + total);
                }
            }
            
            private int distSuf(final int i) {
                if ("3".equals(_param._useGraph)) {
                    return (11 - 3) - i;
                }
                return 11 - i;
            }
        }

        private class SubRecTable extends MainRecTable {
            String getFClass() { return "PRE_CLASS"; }
            String getFSubClass() { return "PRE_SUBCLASS"; }
            String getFSubClass_1() { return "PRE_SUBCLASS_1"; }
            String getFSubClass_2() { return "PRE_SUBCLASS_2"; }
            String getFScore() { return "PRE_SCORE"; }
            String getFRank() { return "PRE_RANK"; }
            String getFDeviation() { return "PRE_DEVIATION"; }
            String getFAverage() { return "PRE_AVERAGE"; }
            String getFMaxScore() { return "PRE_MAX_SCORE"; }
            /** 受験者数 */
            String getFExaminee() { return "PRE_EXAMINEE"; }

            /*
             * 3教科
             */
            String getFScore3() { return "PRE_SCORE3"; }
            String getFRank3() { return "PRE_RANK3"; }
            String getFDeviation3() { return "PRE_DEVIATION3"; }
            String getFAverage3() { return "PRE_AVERAGE3"; }
            String getFMaxScore3() { return "PRE_MAX_SCORE3"; }
            String getFExaminee3() { return "PRE_EXAMINEE3"; }

            /*
             * 5教科
             */
            String getFScore5() { return "PRE_SCORE5"; }
            String getFRank5() { return "PRE_RANK5"; }
            String getFDeviation5() { return "PRE_DEVIATION5"; }
            String getFAverage5() { return "PRE_AVERAGE5"; }
            String getFMaxScore5() { return "PRE_MAX_SCORE5"; }
            String getFExaminee5() { return "PRE_EXAMINEE5"; }
        }
    }

    private static class Exam {
        final Param _param;
        final String _kindCd;
        final boolean _isBefore;
        
        final Map _subClasses;
        
        /** 成績平均データ。 */
        private Map _averageDat = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map _averageDatOther = new HashMap();
        /** 度数分布データ。*/
        private Map _scoreDistributions = new HashMap();
        /** 前回の換算した得点、平均、最高点 */
        private ConvertedScore _convertedScore;
        
        Exam(final Param param, final String kindCd, final boolean isBefore, final Map subClasses) {
            _param = param;
            _kindCd = kindCd;
            _isBefore = isBefore;
            _convertedScore = new ConvertedScore(param);
            _subClasses = subClasses;
        }
        
        public void load(final DB2UDB db2, final List students) throws SQLException {
            final String itemCd = _param.getItemCd();
            loadAverageDat(db2, itemCd);
            loadRecord(db2, itemCd, students);
            loadRecordOther(db2, itemCd, students);
            if (_param._isConvertScoreTo100) {
                _convertedScore.load(db2, _kindCd, itemCd);

            }
        }

        /**
         * 成績平均データを読込み、セットする。
         * @param db2 DB
         * @param itemCd テスト項目コード
         * @param outAvgDat 成績平均データの格納場所
         * @param outAvgDatOther 成績平均データ(3,5教科)の格納場所
         * @throws SQLException SQL例外
         */
        private void loadAverageDat(
                final DB2UDB db2,
                final String itemCd
        ) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("  subclasscd AS subclasscd,");
            stb.append("  subclasscd AS rawSubclasscd,");
            stb.append("  avg,");
            stb.append("  highscore,");
            stb.append("  count,");
            stb.append("  coursecd,");
            stb.append("  majorcd,");
            stb.append("  coursecode");
            stb.append(" FROM");
            stb.append("  record_average_dat");
            if (_param._isGakunen) {
                stb.append(" WHERE");
                stb.append("    year='" + _param._year + "' AND");
                stb.append("    semester='" + _param._semester + "' AND");
                stb.append("    testkindcd='" + _kindCd + "' AND");
                stb.append("    testitemcd='" + itemCd + "' AND");
                stb.append("    avg_div='1' AND");
                stb.append("    grade='" + _param._grade + "' AND");
                stb.append("    hr_class='000' AND");
                stb.append("    coursecd='0' AND");
                stb.append("    majorcd='000' AND");
                stb.append("    coursecode='0000'");
            } else {
                stb.append(" WHERE");
                stb.append("    year='" + _param._year + "' AND");
                stb.append("    semester='" + _param._semester + "' AND");
                stb.append("    testkindcd='" + _kindCd + "' AND");
                stb.append("    testitemcd='" + itemCd + "' AND");
                stb.append("    avg_div='3' AND");
                stb.append("    grade='" + _param._grade + "' AND");
                stb.append("    hr_class='000'");
            }
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String subclasscd;
                    if (ALL3.equals(rs.getString("rawSubclasscd")) || ALL5.equals(rs.getString("rawSubclasscd"))) {
                        subclasscd = rs.getString("rawSubclasscd");
                    } else {
                        subclasscd = rs.getString("subclasscd");
                    }
                    final SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
                    }
                    final BigDecimal avg = rs.getBigDecimal("avg");
                    final Integer count = KNJServletUtils.getInteger(rs, "count");
                    final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");

                    final String key = _param.getAverageKey(subclasscd, coursecd + majorcd + coursecode);
                    if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd)) {
                        final SubClass subClass0 = new SubClass(subclasscd);
                        final AverageDat avgDat = new AverageDat(subClass0, avg, highScore, count, coursecd, majorcd, coursecode);
                        _averageDatOther.put(key, avgDat);
                    } else {
                        final AverageDat avgDat = new AverageDat(subClass, avg, highScore, count, coursecd, majorcd, coursecode);
                        _averageDat.put(key, avgDat);
                    }
                }
            } catch (final SQLException e) {
                log.warn("成績平均データの取得でエラー");
                throw e;
            } finally {
                DbUtils.closeQuietly(rs);
            }
            log.debug("テストコード=" + _kindCd + itemCd + " の成績平均データの件数=" + _averageDat.size());
        }
        

        /**
         * 生徒に成績データを関連付ける。
         * @param db2 DB
         * @param itemCd テスト項目コード
         * @param scoreDistributions 得点分布の格納場所
         * @param students 生徒たち
         */
        private void loadRecord(
                final DB2UDB db2,
                final String itemCd,
                final List students
        ) {
            final String gradeRank  = _param._useAverageAsKijunten ? "grade_avg_rank" : "grade_rank";
            final String courseRank = _param._useAverageAsKijunten ? "course_avg_rank" : "course_rank";

            StringBuffer sql;
            sql = new StringBuffer();
            /* 通常の成績 */
            sql.append("SELECT");
            if ("1".equals(_param._useCurriculumcd)) {
                sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            sql.append("  t1.subclasscd AS subclasscd,");
            sql.append("  t2.score,");
            sql.append("  t2." + gradeRank + " as grade_rank,");
            sql.append("  t2.grade_deviation,");
            sql.append("  t2." + courseRank + " as course_rank,");
            sql.append("  t2.course_deviation");
            sql.append(" FROM");
            sql.append("  record_score_dat t1 LEFT JOIN record_rank_dat t2 ON");
            sql.append("    t1.year=t2.year AND");
            sql.append("    t1.semester=t2.semester AND");
            sql.append("    t1.testkindcd=t2.testkindcd AND");
            sql.append("    t1.testitemcd=t2.testitemcd AND");
            sql.append("    t1.subclasscd=t2.subclasscd AND");
            sql.append("    t1.schregno=t2.schregno AND");
            sql.append("    t1.score_div='" + _param.getRecordScoreDiv(_kindCd) + "'");
            sql.append(" WHERE");
            sql.append("  t1.year='" + _param._year + "' AND");
            sql.append("  t1.semester='" + _param._semester + "' AND");
            sql.append("  t1.testkindcd='" + _kindCd + "' AND");
            sql.append("  t1.testitemcd='" + itemCd + "' AND");
            sql.append("  t1.schregno=?");
            
            loadRecord1(db2, students, sql.toString());

            sql = new StringBuffer();
            /* 集計科目(仮教科コード)を持った成績 */
            sql.append("SELECT");
            if ("1".equals(_param._useCurriculumcd)) {
                sql.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            sql.append("  subclasscd AS subclasscd,");
            sql.append("  score,");
            sql.append("  " + gradeRank + " as grade_rank,");
            sql.append("  grade_deviation,");
            sql.append("  " + courseRank + " as course_rank,");
            sql.append("  course_deviation");
            sql.append(" FROM");
            sql.append("  record_rank_dat");
            sql.append(" WHERE");
            sql.append("  year='" + _param._year + "' AND");
            sql.append("  semester='" + _param._semester + "' AND");
            sql.append("  testkindcd='" + _kindCd + "' AND");
            sql.append("  testitemcd='" + itemCd + "' AND");
            sql.append("  subclasscd like '%0000' AND");   // ★下4桁がゼロ
            sql.append("  schregno=?");

            loadRecord1(db2, students, sql.toString());

            if (_param._isPrintDistribution && !_isBefore) {
                sql = new StringBuffer();
                sql.append("SELECT");
                sql.append("  t3.grade,");
                sql.append("  t3.coursecd || t3.majorcd || t3.coursecode as courseKey,");
                if ("1".equals(_param._useCurriculumcd)) {
                    sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                sql.append("  t1.subclasscd AS subclasscd,");
                sql.append("  t2.score");
                sql.append(" FROM");
                sql.append("  record_score_dat t1 LEFT JOIN record_rank_dat t2 ON");
                sql.append("    t1.year=t2.year AND");
                sql.append("    t1.semester=t2.semester AND");
                sql.append("    t1.testkindcd=t2.testkindcd AND");
                sql.append("    t1.testitemcd=t2.testitemcd AND");
                sql.append("    t1.subclasscd=t2.subclasscd AND");
                sql.append("    t1.schregno=t2.schregno AND");
                sql.append("    t1.score_div='" + _param.getRecordScoreDiv(_kindCd) + "'");
                sql.append("  INNER JOIN schreg_regd_dat t3 ON");
                sql.append("    t1.year=t3.year AND");
                sql.append("    '" + _param.getRegdSemester() + "' =t3.semester AND");
                sql.append("    t1.schregno=t3.schregno");
                sql.append(" WHERE");
                sql.append("  t1.year='" + _param._year + "' AND");
                sql.append("  t1.semester='" + _param._semester + "' AND");
                sql.append("  t1.testkindcd='" + _kindCd + "' AND");
                sql.append("  t1.testitemcd='" + itemCd + "' AND");
                sql.append("  t3.grade='" + _param._grade + "'");
                loadScoreDistribution1(db2, students, sql.toString());
            }
        }

        private void loadScoreDistribution1(
                final DB2UDB db2,
                final List students,
                final String sql
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                        continue;
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    if (score == null) {
                        continue;
                    }
                    final String key = _param.getDistKey(rs.getString("grade"), rs.getString("courseKey"));
                    
                    if (!_scoreDistributions.containsKey(key)) {
                        _scoreDistributions.put(key, new ScoreDistribution(_param, key));
                    }
                    final ScoreDistribution sd = (ScoreDistribution) _scoreDistributions.get(key);
                    sd.add(subClass, score);
                }
                
                if (log.isDebugEnabled()) {
                    for (Iterator it = _scoreDistributions.keySet().iterator(); it.hasNext();) {
                        String key = (String) it.next();
                        ScoreDistribution sd = (ScoreDistribution) _scoreDistributions.get(key);
                        log.debug(" key = " + key + ", distribution = " + sd);
                    }
                }
                
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadRecordOther(
                final DB2UDB db2,
                final String itemCd,
                final List students
        ) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String gradeRank  = _param._useAverageAsKijunten ? "grade_avg_rank" : "grade_rank";
                final String courseRank = _param._useAverageAsKijunten ? "course_avg_rank" : "course_rank";
                final StringBuffer sql = new StringBuffer();
                sql.append("SELECT");
                sql.append("  subclasscd,");
                sql.append("  score,");
                sql.append("  avg,");
                sql.append("  " + gradeRank + " as grade_rank,");
                sql.append("  grade_deviation,");
                sql.append("  " + courseRank + " as course_rank,");
                sql.append("  course_deviation");
                sql.append(" FROM record_rank_dat");
                sql.append(" WHERE");
                sql.append("  year='" + _param._year + "' AND");
                sql.append("  semester='" + _param._semester + "' AND");
                sql.append("  testkindcd='" + _kindCd + "' AND");
                sql.append("  testitemcd='" + itemCd + "' AND");
                sql.append("  subclasscd IN ('" + ALL3 + "', '" + ALL5 + "') AND");
                sql.append("  schregno='" + student._schregno + "'");
                                    
                final Map recordOther = _isBefore ? student._beforeRecordOther : student._recordOther;
                ResultSet rs = null;
                try {
                    db2.query(sql.toString());
                    rs = db2.getResultSet();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        final Integer score = KNJServletUtils.getInteger(rs, "score");
                        final BigDecimal avg = rs.getBigDecimal("avg");
                        final Integer rank = KNJServletUtils.getInteger(rs, _param.getRankField());
                        final BigDecimal deviation = rs.getBigDecimal(_param.getDeviationField());

                        final SubClass subClass = new SubClass(subclasscd);
                        final Record rec = new Record(subClass, score, avg, rank, deviation);
                        recordOther.put(subClass._code, rec);
                    }
                } catch (final SQLException e) {
                    log.warn("成績データの取得でエラー");
                } finally {
                    DbUtils.closeQuietly(rs);
                }
            }
            
            if (_param._isPrintDistribution && !_isBefore) {
                final StringBuffer sql = new StringBuffer();
                sql.append("SELECT");
                if ("1".equals(_param._useCurriculumcd)) {
                    sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                sql.append("  t1.subclasscd AS subclasscd,");
                sql.append("  t1.subclasscd AS rawSubclasscd,");
                sql.append("  t2.grade,");
                sql.append("  t2.coursecd || t2.majorcd || t2.coursecode as courseKey,");
                sql.append("  t1.avg as score");
                sql.append(" FROM record_rank_dat t1");
                sql.append(" inner join schreg_regd_dat t2 on ");
                sql.append("  t1.year=t2.year AND");
                sql.append("  '" + _param.getRegdSemester() + "' =t2.semester AND");
                sql.append("  t1.schregno=t2.schregno");
                sql.append(" WHERE");
                sql.append("  t1.year='" + _param._year + "' AND");
                sql.append("  t1.semester='" + _param._semester + "' AND");
                sql.append("  t1.testkindcd='" + _kindCd + "' AND");
                sql.append("  t1.testitemcd='" + itemCd + "' AND");
                sql.append("  t1.subclasscd IN ('" + ALL3 + "', '" + ALL5 + "') AND");
                sql.append("  t2.grade='" + _param._grade + "' ");

                ResultSet rs = null;
                try {
                    db2.query(sql.toString());
                    rs = db2.getResultSet();
                    while (rs.next()) {
                        final String subclasscd;
                        if (ALL3.equals(rs.getString("rawSubclasscd")) || ALL5.equals(rs.getString("rawSubclasscd"))) {
                            subclasscd = rs.getString("rawSubclasscd");
                        } else {
                            subclasscd = rs.getString("subclasscd");
                        }
                        final Double score = KNJServletUtils.getDouble(rs, "score");

                        final String distKey = _param.getDistKey(rs.getString("grade"), rs.getString("courseKey"));
                        final SubClass subClass = new SubClass(subclasscd);
                        
                        if (!_scoreDistributions.containsKey(distKey)) {
                            _scoreDistributions.put(distKey, new ScoreDistribution(_param, distKey));
                        }

                        ScoreDistribution dist = (ScoreDistribution) _scoreDistributions.get(distKey);
                        dist.add(subClass, new Integer(score.intValue()));
                    }
                } catch (final SQLException e) {
                    log.warn("成績データの取得でエラー");
                } finally {
                    DbUtils.closeQuietly(rs);
                }
            }
        }

        private void loadRecord1(
                final DB2UDB db2,
                final List students,
                final String sql
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    final Map record = _isBefore ? student._beforeRecord : student._record;
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        SubClass subClass = (SubClass) _subClasses.get(subclasscd);
                        if (null == subClass) {
                            log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                            final Class clazz;
                            if ("1".equals(_param._useCurriculumcd)) {
                                clazz = (Class) _param._classes.get(SubClass.getClassCdSchoolKind(subclasscd));
                            } else {
                                clazz = (Class) _param._classes.get(subclasscd.substring(0, 2));
                            }
                            if (null == clazz) {
                                continue;
                            }
                            subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
                        }
                        final Integer score = KNJServletUtils.getInteger(rs, "score");
                        final Integer rank = KNJServletUtils.getInteger(rs, _param.getRankField());
                        final BigDecimal deviation = rs.getBigDecimal(_param.getDeviationField());

                        final Record rec = new Record(subClass, score, null, rank, deviation);
                        record.put(subClass, rec);
                    }
                }
            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー");
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }
    }
    
    private class Student implements Comparable {
        private final String _schregno;
        private final String _grade;
        private final String _hrClass;
        private final String _attendNo;
        private final String _hrName;

        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;

        private final String _name;

        private final String _zipCd;
        private final String _addr1;
        private final String _addr2;
        private final String _addressee;


        /** 成績所見データ. */
        private final String _remark;

        /** 履修科目. */
        private final Map _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _record = new TreeMap();

        /** 成績データ。3教科,5教科用 */
        private final Map _recordOther = new HashMap();

        /** 前回の成績データ。 */
        private final Map _beforeRecord = new TreeMap();

        /** 前回の成績データ。3教科,5教科用 */
        private final Map _beforeRecordOther = new HashMap();

        private Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String name,
                final String zipCd,
                final String addr1,
                final String addr2,
                final String addressee,
                final String remark
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _zipCd = zipCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addressee = addressee;
            _remark = remark;
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        public int compareTo(Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
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
    }

    /**
     * 教科。
     */
    private class Class {
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

        /** 合併情報を持っているか */
        private boolean _hasCombined;
        /** 合併先か? */
        private boolean _isSaki;
        /** 合併元か? */
        private boolean _isMoto;

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

        public String getClassKey(final Param param) {
            if ("1".equals(param._useCurriculumcd)) {
                return getClassCdSchoolKind();
            }
            return getClassCd();
        }

        public String getClassCdSchoolKind() {
            return getClassCdSchoolKind(_code);
        }

        public static String getClassCdSchoolKind(final String subclasscd) {
            String[] split = StringUtils.split(subclasscd, "-");
            if (split == null || split.length != 4) {
                log.fatal("科目コードフォーマットエラー:" + subclasscd);
                return "";
            }
            return split[0] + "-" + split[1];
        }

        public void setSaki() {
            _hasCombined = true;
            _isSaki = true;
        }

        public void setMoto() {
            _hasCombined = true;
            _isMoto = true;
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
    }

    private static class Record {
        private final SubClass _subClass;
        private final Integer _score;
        private final BigDecimal _avg;
        private final Integer _rank;
        private final BigDecimal _deviation;

        private Record(
                final SubClass subClass,
                final Integer score,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation
        ) {
            _subClass = subClass;
            _score = score;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
        }

        public String getScore() {
            return null == _score ? "" : _score.toString();
        }

        public String getAvg() {
            return null == _avg ? "" : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getRank() {
            return null == _rank ? "" : _rank.toString();
        }

        public String getDeviation() {
            return null == _deviation ? "" : _deviation.toString();
        }

        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }

    private static class AverageDat {
        private final SubClass _subClass;
        private final BigDecimal _avg;
        private final Integer _highScore;
        private final Integer _count;
        private final String _coursecd;
        private final String _majorcd;
        private final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
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
    }
    
    private static class ScoreDistribution {
        private Param _param;
        private final String _distKey;
        private final Map _subclassDistributions = new HashMap();
        
        private ScoreDistribution(final Param param, final String key) {
            _param = param;
            _distKey = key;
        }
        
        private SubclassScoreDistribution getSubclassDistributionMap(String subClassCd) {
            if (!_subclassDistributions.containsKey(subClassCd)) {
                _subclassDistributions.put(subClassCd, new SubclassScoreDistribution(_param));
            }
            return (SubclassScoreDistribution) _subclassDistributions.get(subClassCd);
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
            for (final Iterator it = _subclassDistributions.keySet().iterator(); it.hasNext();) {
                final String subClassCd = (String) it.next();
                final SubclassScoreDistribution ssd = (SubclassScoreDistribution) _subclassDistributions.get(subClassCd);
                stb.append("[subClass=").append(subClassCd);
                for (int i = 0; i < ssd._scoreCounts.length; i++) {
                    String scoreKey = ssd._scoreCounts[i]._key;
                    Integer count = getCount(subClassCd, scoreKey);
                    stb.append(comma).append("\"").append(scoreKey).append("\"=").append(count);
                    comma = ", ";
                }
                stb.append("] ");
            }
            return stb.toString();
        }

        public String toString() {
            return " dist = (" + distStr() + ")";
        }
    }
    
    private static class ScoreCount {
        final String _key;
        final int _rangeLower;
        final int _rangeUpper;
        final List _scoreList = new ArrayList();
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
        
        
        SubclassScoreDistribution(final Param param) {
            final int[][] nums;
            if ("3".equals(param._useGraph)) {
                nums = new int[8][3];
                nums[0] = new int[]{40, 0, 39};
                for (int i = 1; i < nums.length; i++) {
                    final int n = 3 + i;
                    nums[i] = new int[]{(n + 1) * 10, n * 10, n * 10 + 9};
                }
            } else {
                nums = new int[11][3];
                for (int i = 0; i < nums.length; i++) {
                    final int n = i;
                    nums[i] = new int[]{(n + 1) * 10, n * 10, n * 10 + 9};
                }
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
                if (_scoreCounts[i].equals(scoreKey)) {
                    ind = i;
                    break;
                }
            }
            if (ind == -1) {
                return -1;
            }
            return 11 - ind;
        }
    }
    

    /** 「100点に換算する」場合に表示するデータ */
    private static class ConvertedScore {
        
        final Param _param;
        
        Map _avg3Map; // 3教科の学年/コース平均に表示する値のマップ
        Map _avg5Map; // 5教科の学年/コース平均に表示する値のマップ
        Map _highscoreAvg3Map; // 3教科の学年/コース最高得点に表示する値のマップ
        Map _highscoreAvg5Map; // 5教科の学年/コース最高得点に表示する値のマップ
        
        public ConvertedScore(final Param param) {
            _param = param;
        }
        
        public void load(DB2UDB db2, final String testKindCd, final String testItemCd) {
            try {
                _avg3Map = loadAvg(db2, "3", testKindCd, testItemCd);
                _avg5Map = loadAvg(db2, "5", testKindCd, testItemCd);
                _highscoreAvg3Map = loadHighscoreAvg(db2, ALL3, testKindCd, testItemCd);
                _highscoreAvg5Map = loadHighscoreAvg(db2, ALL5, testKindCd, testItemCd);
            } catch (SQLException e) {
                log.error("Exception! ConvertedScore#load ", e);
            }
        }
        
        public String getAvg(String groupDiv, Student student) {
            final BigDecimal avg;
            if ("3".equals(groupDiv)) {
                avg = (BigDecimal) _avg3Map.get(_param.getDistKey(student._grade, student.courseKey()));
            } else {
                avg = (BigDecimal) _avg5Map.get(_param.getDistKey(student._grade, student.courseKey()));
            }
            return avg == null ? null : avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        public String getHighscoreAvg(String groupDiv, Student student) {
            final BigDecimal avg;
            if ("3".equals(groupDiv)) {
                avg = (BigDecimal) _highscoreAvg3Map.get(_param.getDistKey(student._grade, student.courseKey()));
            } else {
                avg = (BigDecimal) _highscoreAvg5Map.get(_param.getDistKey(student._grade, student.courseKey()));
            }
            return avg == null ? null : avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        /**
         * 学年/コースと3,5教科の学年/コース平均に表示する値のマップを得る。
         *  (最高得点 = 3,5教科の科目のRECORD_AVERAGE_DATのSCORE合計 / COUNT合計)
         * @param db2
         * @param groupDiv
         * @param testKindCd
         * @param testItemCd
         * @return
         * @throws SQLException
         */
        private Map loadAvg(DB2UDB db2, String groupDiv, final String testKindCd, final String testItemCd) throws SQLException {
            final Map map = new HashMap();
            final String sql = sqlAvg(groupDiv, testKindCd, testItemCd);
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final BigDecimal totalScore = rs.getBigDecimal("SCORE");
                final BigDecimal count = rs.getBigDecimal("COUNT");
                final BigDecimal avg = totalScore == null ? null : totalScore.divide(count, 1, BigDecimal.ROUND_HALF_UP);
                map.put(_param.getDistKey(_param._grade, rs.getString("course")), avg);
            }
            return map;
        }
        
        /**
         * 学年/コースと3,5教科の学年/コース最高得点のマップを得る。
         *  (最高得点 = 3,5教科の科目のRECORD_AVERAGE_DATのHIGHSCOREと一致する生徒のRECORD_RANK_DATのAVG)
         * @param db2
         * @param subclassCd
         * @param testKindCd
         * @param testItemCd
         * @return
         * @throws SQLException
         */
        private Map loadHighscoreAvg(DB2UDB db2, String subclassCd, final String testKindCd, final String testItemCd) throws SQLException {
            final Map map = new HashMap();
            final String sql = sqlHighscoreAvg(subclassCd, testKindCd, testItemCd);
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("AVG");
                map.put(_param.getDistKey(_param._grade, rs.getString("course")), avg);
            }
            return map;
        }

        // 2012/05/25 引数subclassCdはALL3・ALL5のみのため教育課程コードの修正はなし
        private String sqlHighscoreAvg(String allSubclassCd, final String testKindCd, final String testItemCd) {
            final String avgDiv = _param._isGakunen ? "1" : "3";

            final StringBuffer stb = new StringBuffer();
            stb.append(" with highscores as ( ");
            stb.append(" select ");
            stb.append("     t1.subclasscd, ");
            stb.append("     t1.highscore ");
            if (!_param._isGakunen) {
                stb.append("     , coursecd ");
                stb.append("     , majorcd ");
                stb.append("     , coursecode ");
            }
            stb.append(" from ");
            stb.append("     record_average_dat t1 ");
            stb.append(" where ");
            stb.append("     t1.year = '" + _param._year + "' ");
            stb.append("     and t1.semester = '" + _param._semester + "' ");
            stb.append("     and t1.testkindcd = '" + testKindCd + "' ");
            stb.append("     and t1.testitemcd = '" + testItemCd + "' ");
            stb.append("     and t1.subclasscd = '" + allSubclassCd + "' ");
            stb.append("     and t1.avg_div = '" + avgDiv + "' ");
            stb.append("     and t1.grade = '" + _param._grade + "' ");
            stb.append(" ) ");
            stb.append(" select ");
            if (_param._isGakunen) {
                stb.append("         '' as course, ");
            } else {
                stb.append("         t2.coursecd || t2.majorcd || t2.coursecode as course, ");
            }
            stb.append("     t2.highscore, ");
            stb.append("     t1.score, ");
            stb.append("     t1.avg ");
            stb.append(" from ");
            stb.append("     record_rank_dat t1 ");
            stb.append("     inner join highscores t2 on t2.highscore = t1.score ");
            stb.append("         and t2.subclasscd = t1.subclasscd ");
            stb.append("     inner join schreg_regd_dat t3 on t3.schregno = t1.schregno ");
            stb.append("         and t3.year = t1.year ");
            stb.append("         and t3.semester = '" + _param.getRegdSemester() + "' ");
            stb.append("         and t3.grade = '" + _param._grade + "' ");
            if (!_param._isGakunen) {
                stb.append("         and t3.coursecd = t2.coursecd ");
                stb.append("         and t3.majorcd = t2.majorcd ");
                stb.append("         and t3.coursecode = t2.coursecode ");
            }
            stb.append(" where ");
            stb.append("     t1.year = '" + _param._year + "' ");
            stb.append("     and t1.semester = '" + _param._semester + "' ");
            stb.append("     and t1.testkindcd = '" + testKindCd + "' ");
            stb.append("     and t1.testitemcd = '" + testItemCd + "' ");
            return stb.toString();
        }
        
        private String sqlAvg(final String groupDiv, final String testKindCd, final String testItemCd) {
            
            final String avgDiv = _param._isGakunen ? "1" : "3";
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with subclasscds as( ");
            stb.append("     select distinct ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("            subclasscd AS subclasscd ");
            if (!_param._isGakunen) {
                stb.append("     , coursecd ");
                stb.append("     , majorcd ");
                stb.append("     , coursecode ");
            }
            stb.append("     from rec_subclass_group_dat t1 ");
            stb.append("     where ");
            stb.append("         t1.year = '" + _param._year + "' ");
            stb.append("         and t1.group_div = '" + groupDiv + "' ");
            stb.append("         and t1.grade = '" + _param._grade + "' ");
            stb.append(" ) select ");
            if (_param._isGakunen) {
                stb.append("         '' as course, ");
            } else {
                stb.append("     coursecd || majorcd || coursecode as course, ");
            }
            stb.append("     sum(score) as score, ");
            stb.append("     sum(count) as count ");
            stb.append(" from ");
            stb.append("     record_average_dat t1 ");
            stb.append(" where ");
            stb.append("     t1.year = '" + _param._year + "' ");
            stb.append("     and t1.semester = '" + _param._semester + "' ");
            stb.append("     and t1.testkindcd = '" + testKindCd + "' ");
            stb.append("     and t1.testitemcd = '" + testItemCd + "' ");
            stb.append("     and avg_div = '" + avgDiv + "' ");
            stb.append("     and t1.grade = '" + _param._grade + "' ");
            stb.append("     and t1.hr_class = '000' ");
            stb.append("     and exists (select 'X' from subclasscds where subclasscd = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                 t1.subclasscd ");
            if (!_param._isGakunen) {
                stb.append("     and coursecd = t1.coursecd ");
                stb.append("     and majorcd = t1.majorcd ");
                stb.append("     and coursecode = t1.coursecode ");
            }
            stb.append("                   ) ");
            if (!_param._isGakunen) {
                stb.append("     group by t1.coursecd || t1.majorcd || t1.coursecode ");
            }
            return stb.toString();
        }
    }
} // KNJD105C

// eof
