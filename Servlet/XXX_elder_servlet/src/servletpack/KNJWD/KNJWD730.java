// kanji=漢字
/*
 * $Id: b115f6d897d9665848dd3fdfad9ed98686d7367c $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学習状況通知表（新課程）
 * @author nakada
 * @version $Id: b115f6d897d9665848dd3fdfad9ed98686d7367c $
 */
public class KNJWD730 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWD730.class);

    /**
     * 1授業の分。(1時間で何分授業か?)
     */
    public static final int PERIOD_MINUTE = 50;
    private static final String FORM_FILE = "KNJWD730.frm";

    /** スクーリング数 */
    private static final int _SCOOLING_NUM = 5;

    /** レポート回数 */
    private static final int _REPORT_NUM = 18;

    /** 試験月 */
    private static final String _MONTH3 = "03";
    private static final String _MONTH9 = "09";

    /*
     * 文字数による出力項目切り分け基準 
     */
    /** 名前 */
    private static final int NAME1_LENG = 25;
    /** 科目名 */
    private static final int SUBCLASSNAME_LENG = 10;

    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 16;
    private int _detailCnt;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws SQLException, IOException {
        dumpParam(request);
        _param = createParam(request, db2);
        _form = new Form(FORM_FILE, response, _svf);
        db2 = null;

        final List students = new LinkedList();
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }
            _param.load(db2);

            for (int i = 0; i < _param._schregno.length; i++) {
                final String schregno = _param._schregno[i];
                final Student student = createStudent(db2, schregno);
                if (null == student) {
                    log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
                    continue;
                }
                student.load(db2);
                students.add(student);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
        }

        Collections.sort(students);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            printMain(student);
        }
        _form.closeSvf();
    }

    private void printMain(final Student student) throws SQLException {
        _form._svf.VrAttribute( "RECORD1", "Print=1");
        _form._svf.VrAttribute( "RECORD2", "Print=0");
        _form._svf.VrAttribute( "RECORD3", "Print=0");
        _form._svf.VrAttribute( "SCHREGNO", "FF=1");

        printHeader(student);
        
        _detailCnt = 0;
        for (Iterator it = student._lineData.iterator(); it.hasNext();) {
            final LineData lineData = (LineData) it.next();

            if (_detailCnt == DETAILS_MAX) {
                _detailCnt = 0;
            }
            _detailCnt++;

            printApplicant(student, lineData);

            _form._svf.VrEndRecord();
            _hasData = true;
        }

        if (_detailCnt <= DETAILS_MAX && _hasData) {
            _form._svf.VrEndRecord();

            if (_detailCnt == DETAILS_MAX ) {
                _detailCnt = 0;
            }

            _form._svf.VrAttribute( "RECORD1", "Print=0");
            _form._svf.VrAttribute( "RECORD2", "Print=1");
            _form._svf.VrAttribute( "RECORD3", "Print=0");

            printDummyLine();
        }
    }

    private void printDummyLine() {
        for (int i = (DETAILS_MAX - _detailCnt); i != 0; i--) {
            _form._svf.VrAttribute( "KARA", "Print=1");
            _form._svf.VrsOut("KARA", "空行");
            _form._svf.VrEndRecord();
        }
    }

    private void printFoot() {
        /* 特別活動・ホームルーム */
        _form._svf.VrsOut("TIME", "dummy");      // TODO: 保留
        _form._svf.VrsOut("TOTAL_TIME", "dummy");      // TODO: 保留
    }

    private void printHeader(final Student student) {
        // 学籍番号
        _form._svf.VrsOut("SCHREGNO", student._schregNo);

        // 氏名
        _form._svf.VrsOut("NAME", student._name);

        // 所属
        if (null != student._schregRegdDat._grade) {
            _form._svf.VrsOut("SCHOOLNAME", _param.getBelongingName(student._schregRegdDat._grade));
        }

        // 入学年月日
        _form._svf.VrsOut("ENT_DATE", getJDate(student._entDate));

        // 作成日
        _form._svf.VrsOut("DATE", getJDate(KNJ_EditDate.H_Format_Haifun(_param._date)));

        // 年度
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/01/01") + "度");
    }

    private void printApplicant(
            final Student student,
            final LineData lineData
    ) throws SQLException {
        // 教科
        _form._svf.VrsOut("CLASS_NAME", _param.getClassName(lineData._classcd));

        // 科目
        final String key = lineData._classcd + lineData._curriculumCd + lineData._subclassCd;
        final String subclassName = _param.getSubclassName(key);
        _form.printSubclassName(subclassName);

        // 単位数
        _form._svf.VrsOut("CREDIT", lineData._credits);

        /* スクーリング */
        // 基準
        if (null != lineData._schoolingSeq) {
            _form._svf.VrsOut("SCHOOLING_SEQ", lineData._schoolingSeq.toString());
        }

        // 面接指導
        final BigDecimal rateSum = print面接指導(student, lineData);

        // メディア
        final BigDecimal mediaTotal = printMedia(rateSum, lineData);

        // 残
        if (null != lineData._schoolingSeq) {
            final BigDecimal schoolingSeq = new BigDecimal(lineData._schoolingSeq.intValue());
            final BigDecimal ans = schoolingSeq.subtract(mediaTotal);
            _form._svf.VrsOut("TIME_LEFT", (ans.signum() < 0) ? "0" : ans.toString());
        }

        /* レポート数 */
        // 基準
        if (null != lineData._reportSeq) {
            _form._svf.VrsOut("REPORT_SEQ", lineData._reportSeq.toString());
        }

        // レポート数
        printreport(lineData);    

        // テスト結果
        printTest(lineData);
    }

    /**
     * スクーリングの面接指導を印字する。
     * @param student 生徒
     * @param ld LineData
     * @return 面接指導時間
     */
    private BigDecimal print面接指導(final Student student, final LineData ld) {
        // 通信スクーリングの合計値を算出
        int rateSum = 0;
        for (Iterator it = ld._schoolingRateDats.iterator(); it.hasNext();) {
            final RecSchoolingRateDat rateDat = (RecSchoolingRateDat) it.next();
            if (null != rateDat._rate) {
                rateSum += rateDat._rate.intValue();
            }
        }

        final int schoolingSeq = (null == ld._schoolingSeq) ? 0 : ld._schoolingSeq.intValue();
        final double schoolingCount = schoolingSeq * ((double) rateSum / 10);

        // 通信スクーリング + 通学スクーリング
        final BigDecimal result = getHour(schoolingCount + ld._commutingDats.intValue());

        _form._svf.VrsOut("MASTER_TIME", result.toString());
        log.debug("科目コード=" + ld._subclassCd + "の面接指導=(通信S:" + schoolingCount + " + 通学S:" + ld._commutingDats + ")");

        return result;
    }

    private BigDecimal printMedia(final BigDecimal rateSum, final LineData lineData) {
        // 代替Sだけを抜き出す
        final List daitaiSchooing = new ArrayList();
        for (Iterator it = lineData._schoolingDats.iterator(); it.hasNext();) {
            final RecSchoolingDat schoolingDat = (RecSchoolingDat) it.next();
            if (!schoolingDat._is代替schooling) {
                continue;
            }
            daitaiSchooing.add(schoolingDat);
        }

        // 印刷および合計の算出
        BigDecimal rtn = rateSum;
        for (final Iterator it = daitaiSchooing.iterator(); it.hasNext();) {
            final RecSchoolingDat schoolingDat = (RecSchoolingDat) it.next();

            final double iMediaTime = schoolingDat._getValue.doubleValue() / PERIOD_MINUTE;
            final BigDecimal val = getHour(iMediaTime);

            final String num = schoolingDat._schoolingType.substring(1);    // 2バイト目に数字
            final int index = Integer.parseInt(num);
            _form._svf.VrsOut("MEDIA_TIME" + index, val.toString());

            rtn = rtn.add(val);
        }
        return rtn;
    }

    private void printTest(final LineData lineData) {
        for (Iterator it = lineData._testDats.iterator(); it.hasNext();) {
            final RecTestDat recTestDat = (RecTestDat) it.next();

            if (lineData._useTest) {
                if (recTestDat._score != null) {
                    if (recTestDat._month.equals(_MONTH9)) {
                        _form._svf.VrsOut("SCORE1", recTestDat._score.toString());
                    } else {
                        _form._svf.VrsOut("SCORE2", recTestDat._score.toString());
                    }
                }
            } else {
                // 科目詳細マスタのテスト実施フラグがNULLの場合、得点エリアにハイフンを設定
                if (recTestDat._month.equals(_MONTH9)) {
                    _form._svf.VrsOut("SCORE1", "-");   // 9月試験
                } else {
                    _form._svf.VrsOut("SCORE2", "-");   // 3月試験
                }
            }
        }
    }

    private void printreport(final LineData lineData) {
        for (Iterator it = lineData._reportDats.iterator(); it.hasNext();) {
            final RecReportDat reportDat = (RecReportDat) it.next();

            if (reportDat._reportSeq <= _REPORT_NUM) {
                final String index = String.valueOf(reportDat._reportSeq);
                if (reportDat._commitedScore2 != null) {
                    final int val = Integer.parseInt(reportDat._commitedScore2);
                    _form._svf.VrsOut("COMMITED_SCORE" + index, Integer.toString(val));    // zero サプレス
                } else {
                    final int val = (Integer.parseInt(reportDat._commitedScore1));
                    _form._svf.VrsOut("COMMITED_SCORE" + index, Integer.toString(val));    // zero サプレス
                }
            }
        }
    }

    private static BigDecimal getHour(final double hour) {
        final BigDecimal bd = new BigDecimal(String.valueOf(hour));

        return bd.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private static String getJDate(String date) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            
            return nao_package.KenjaProperties.gengou(year, month, dom);

        } catch (final Exception e) {
            return null;
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

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String _date;
        private final String[] _schregno;

        private Map _belongingMap;
        private Map _subclassMstMap;
        private Map _classMstMap;

        /** 通学区分. */
        private Map _commutingDivMap;

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String date,
                final String[] schregno
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _date = date;
            _schregno = schregno;
        }

        public void load(final DB2UDB db2) throws SQLException {
            _belongingMap = createBelongingDat(db2);
            _subclassMstMap = createSubclassMst(db2);
            _classMstMap = createClassMst(db2);
            _commutingDivMap = createStudentDivMst(db2);
        }

        public String getBelongingName(String code) {
            return (String) nvlT((String)_belongingMap.get(code));
        }

        public String getSubclassName(String code) {
            return (String) nvlT((String)_subclassMstMap.get(code));
        }

        public String getClassName(String code) {
            return (String) nvlT((String)_classMstMap.get(code));
        }

        /**
         * 教科マスタ。
         */
        private Map createClassMst(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "  CLASSCD as classcd,"
                + "  CLASSNAME as classname"
                + " from"
                + "   CLASS_MST"
                ;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String code = rs.getString("classcd");
                final String name = rs.getString("classname");

                rtn.put(code, name);
            }
            return rtn;
        }

        /**
         * 科目マスタ。
         */
        private Map createSubclassMst(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "  CLASSCD as classcd,"
                + "  CURRICULUM_CD as curriculumCd,"
                + "  SUBCLASSCD as subclasscd,"
                + "  SUBCLASSNAME as subclassname"
                + " from"
                + "  SUBCLASS_MST"
                ;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String code1 = rs.getString("classcd");
                final String code2 = rs.getString("curriculumCd");
                final String code3 = rs.getString("subclasscd");
                final String name = rs.getString("subclassname");

                rtn.put(code1 + code2 + code3, name);
            }

            return rtn;
        }

        /**
         * 所属データ。
         */
        private Map createBelongingDat(DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement("select BELONGING_DIV, SCHOOLNAME1 from BELONGING_MST");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("belonging_div");
                final String name = rs.getString("schoolname1");

                rtn.put(code, name);
            }
            return rtn;
        }

        /**
         * 学生区分マスタ。
         */
        private Map createStudentDivMst(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "  COURSE_DIV,"
                + "  STUDENT_DIV,"
                + "  COMMUTING_DIV"
                + " from"
                + "  STUDENTDIV_MST";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String code1 = rs.getString("COURSE_DIV");
                final String code2 = rs.getString("STUDENT_DIV");
                final String name = rs.getString("COMMUTING_DIV");

                rtn.put(code1 + code2, name);
            }
            return rtn;
        }
    }

    private Param createParam(final HttpServletRequest request, DB2UDB db2) throws SQLException {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
        final String date = request.getParameter("DATE");
        final String[] schregno = request.getParameterValues("CATEGORY_SELECTED");

        final Param param = new Param(
                year,
                semester,
                programId,
                dbName,
                loginDate,
                date,
                schregno
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================

    private class Form {
        private Vrw32alp _svf;

        public Form(
                final String file,
                final HttpServletResponse response,
                final Vrw32alp svf
        ) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
            _svf.VrSetForm(FORM_FILE, 4);
        }

        public void printName(final Student student) {
            final String name = student._name;

            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME";
                } else {
                    label = "NAME";     // TODO: 現行1項目のみ
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printSubclassName(final String pName) {
            if (pName != null) {
                final String label;
                if (pName.length() <= SUBCLASSNAME_LENG) {
                    label = "SUBCLASSNAME1_1";
                } else {
                    label = "SUBCLASSNAME1_2";
                }
                _form._svf.VrsOut(label, pName);
            }
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
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student implements Comparable {
        private final String _schregNo;
        private final String _name;
        private final String _entDate;

        private SchregRegdDat _schregRegdDat;

        /** 1行あたりのデータ. */
        private List _lineData;

        Student(
                final String schregNo,
                final String name,
                final String entDate
        ) {
            _schregNo = schregNo;
            _name = name;
            _entDate = entDate;
        }

        public void load(DB2UDB db2) throws SQLException {
            _schregRegdDat = createSchregRegdDat(db2, _param._year, _param._semester, _schregNo);
            _lineData = createLineData(db2, _schregNo);
            for (final Iterator it = _lineData.iterator(); it.hasNext();) {
                final LineData lineData = (LineData) it.next();
                lineData.load(db2, _schregNo);
            }
        }

        public int compareTo(Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            return _schregNo.compareTo(that._schregNo);
        }

        public String toString() {
            return _schregNo + "/" + _name;
        }
    }

    private Student createStudent(final DB2UDB db2, final String schregno) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            + "  SCHREGNO,"
            + "  NAME,"
            + "  ENT_DATE"
            + " from"
            + "  SCHREG_BASE_MST"
            + " where" 
            + "  SCHREGNO = '" + schregno + "'"
            ;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String entDate = rs.getString("ENT_DATE");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        entDate
                );
                log.debug("☆生徒=" + studentDat);
                return studentDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return null;
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _grade;
        private final String _courseDiv;
        private final String _studentDiv;

        SchregRegdDat(
                final String grade,
                final String courseDiv,
                final String studentDiv
        ) {
            _grade = grade;
            _courseDiv = courseDiv;
            _studentDiv = studentDiv;
        }

        public String getKey() {
            final String code1 = (null == _courseDiv) ? "" : _courseDiv;
            final String code2 = (null == _studentDiv) ? "" : _studentDiv;
            return code1 + code2;
        }
    }

    public SchregRegdDat createSchregRegdDat(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String schregno
    ) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            +"    GRADE,"
            +"    COURSE_DIV,"
            +"    STUDENT_DIV"
            +" from"
            +"    SCHREG_REGD_DAT"
            +" where"
            +" YEAR = '" + year + "'"
            +" and SEMESTER = '" + semester + "'"
            +" and SCHREGNO = '" + schregno + "'"
            ;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String grade = rs.getString("GRADE");
            final String courseDiv = rs.getString("COURSE_DIV");
            final String studentDiv = rs.getString("STUDENT_DIV");
 
            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    grade,
                    courseDiv,
                    studentDiv
            );
            return schregRegdDat;
        }
        return new SchregRegdDat(null, "", "");
    }

    // ======================================================================
    /**
     * 帳票での1行あたりのデータ。
     */
    private class LineData {
        private final String _classcd;
        private final String _curriculumCd;
        private final String _subclassCd;
        private final String _credits;

        /** 年間スクーリング回数. */
        private Integer _schoolingSeq;
        /** 年間レポート回数. */
        private Integer _reportSeq;
        /** テスト実施フラグ. */
        private boolean _useTest;

        /** 通信スクーリング実績. */
        private List _schoolingDats;

        /** 通信スクーリング割合. */
        private List _schoolingRateDats;

        /** レポート実績. */
        private List _reportDats;

        /** テスト実績. */
        private List _testDats;

        /** 通学スクーリング実績の件数(時間数). */
        private Integer _commutingDats;

        LineData(
                final String classcd,
                final String curriculumCd,
                final String subclasscde,
                final String credits
        ) {
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclasscde;
            _credits = credits;
        }

        public void load(final DB2UDB db2, final String schregno) throws SQLException {
            loadSubclassDetailsMst(db2);

            _schoolingDats = createRecSchoolingDats(db2, schregno);
            log.info("スクーリング実績の集計(科目CD:" + _subclassCd + ")=" + _schoolingDats);

            _schoolingRateDats = createRecSchoolingRateDats(db2, schregno);
            _reportDats = createRecReportDats(db2, schregno);
            _testDats = createRecTestDats(db2, schregno);
            _commutingDats = createRecCommutingDat(db2, schregno);
        }

        private Integer createRecCommutingDat(final DB2UDB db2, final String schregno) throws SQLException {
            Integer total = null;

            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "   count(*) as TOTAL"
                + " from"
                + "   REC_COMMUTING_DAT"
                + " where"
                + "   YEAR = '" + _param._year + "'"
                + "   and CLASSCD = '" + _classcd + "'"
                + "   and CURRICULUM_CD = '" + _curriculumCd + "'"
                + "   and SUBCLASSCD = '" + _subclassCd + "'"
                + "   and SCHREGNO = '" + schregno + "'"
                ;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                total = KNJServletUtils.getInteger(rs, "TOTAL");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            return total;
        }

        private void loadSubclassDetailsMst(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "  SCHOOLING_SEQ,"
                + "  REPORT_SEQ,"
                + "  TEST_FLG"
                + " from"
                + "  SUBCLASS_DETAILS_MST"
                + " where"
                + "  YEAR = '" + _param._year + "'"
                + " and CLASSCD = '" + _classcd + "'"
                + " and CURRICULUM_CD = '" + _curriculumCd + "'"
                + " and SUBCLASSCD = '" + _subclassCd + "'";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            _schoolingSeq = null;
            _reportSeq = null;
            _useTest = false;
            while (rs.next()) {
                final Integer schoolingSeq = KNJServletUtils.getInteger(rs, "SCHOOLING_SEQ");
                final Integer reportSeq = KNJServletUtils.getInteger(rs, "REPORT_SEQ");
                final String testFlg = rs.getString("TEST_FLG");

                _schoolingSeq = schoolingSeq;
                _reportSeq = reportSeq;
                _useTest = (null != testFlg) ? true : false;
                break;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private List createRecTestDats(final DB2UDB db2, final String schregno) throws SQLException {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "   MONTH,"
                + "   SCORE"
                + " from"
                + "   REC_TEST_DAT"
                + " where"
                + "   YEAR = '" + _param._year + "'"
                + "   and CLASSCD = '" + _classcd + "'"
                + "   and CURRICULUM_CD = '" + _curriculumCd + "'"
                + "   and SUBCLASSCD = '" + _subclassCd + "'"
                + "   and SCHREGNO = '" + schregno + "'"
                + "   and (MONTH = '" + _MONTH9 + "' or MONTH = '" + _MONTH3 + "')"
                + " order by MONTH DESC";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String month = rs.getString("MONTH");
                final Integer score = KNJServletUtils.getInteger(rs, "SCORE");

                final RecTestDat recTestDat = new RecTestDat(month, score);
                rtn.add(recTestDat);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        private List createRecReportDats(final DB2UDB db2, final String schregno) throws SQLException {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
      
            ps = db2.prepareStatement(sqlRecReportDats(schregno));
            rs = ps.executeQuery();
            while (rs.next()) {
                final int reportSeq = Integer.parseInt(rs.getString("REPORT_SEQ"));
                final String commitedScore1 = rs.getString("COMMITED_SCORE1");
                final String commitedScore2 = rs.getString("COMMITED_SCORE2");
      
                final RecReportDat recReportDat = new RecReportDat(
                        reportSeq,
                        commitedScore1,
                        commitedScore2
                );
                rtn.add(recReportDat);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }
      
        private String sqlRecReportDats(String schregno) {
            return "select"
                + "    REPORT_SEQ,"
                + "    COMMITED_SCORE1,"
                + "    COMMITED_SCORE2"
                + " from"
                + "    REC_REPORT_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "'"
                + "    and CLASSCD = '" + _classcd + "'"
                + "    and CURRICULUM_CD = '" + _curriculumCd + "'"
                + "    and SUBCLASSCD = '" + _subclassCd + "'"
                + "    and SCHREGNO = '" + schregno + "'"
                + "    and ((COMMITED_SCORE1 is not null)"
                + "        or (COMMITED_SCORE2 is not null))"
                + " order by REPORT_SEQ"
                ;
        }

        private List createRecSchoolingRateDats(final DB2UDB db2, final String schregno) throws SQLException {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
      
            final String sql = "select"
                + "    SUM(W1.RATE) as rate"
                + " from"
                + "    REC_SCHOOLING_RATE_DAT W1"
                + " inner join SCHOOLING_TYPE_MST W2 on"
                + "    W1.SCHOOLING_TYPE = W2.SCHOOLING_TYPE"
                + "    and W2.SCHOOLING_DIV = '01'"
                + " where"
                + "    W1.YEAR = '" + _param._year + "'"
                + "    and W1.CLASSCD = '" + _classcd + "'"
                + "    and W1.CURRICULUM_CD = '" + _curriculumCd + "'"
                + "    and W1.SUBCLASSCD = '" + _subclassCd + "'"
                + "    and W1.SCHREGNO = '" + schregno + "'"
                + "    and W1.COMMITED_S is not null"
                + "    and W1.COMMITED_E is not null"
                + " group by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO"
                + " order by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Integer rate = KNJServletUtils.getInteger(rs, "rate");
                final RecSchoolingRateDat recSchoolingRateDat = new RecSchoolingRateDat(rate);
                rtn.add(recSchoolingRateDat);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            return rtn;
        }

        private List createRecSchoolingDats(final DB2UDB db2, final String schregno) throws SQLException {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
      
            final String sql = sqlRecSchoolingDats(schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolingType = nvlT(rs.getString("schoolingType"));
                final Integer getValue = KNJServletUtils.getInteger(rs, "getValue");
                final String schoolingDiv = nvlT(rs.getString("schoolingDiv"));
      
                final RecSchoolingDat schoolingDat = new RecSchoolingDat(
                        schoolingType,
                        getValue,
                        schoolingDiv
                );
                rtn.add(schoolingDat);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }
      
        private String sqlRecSchoolingDats(final String schregno) {
            final String sql;
            sql = "select"
                    + "  W1.SCHOOLING_TYPE as schoolingType,"
                    + "  SUM(W1.GET_VALUE) as getValue,"
                    + "  W2.SCHOOLING_DIV as schoolingDiv"
                    + " from"
                    + "  REC_SCHOOLING_DAT W1"
                    + " inner join SCHOOLING_TYPE_MST W2 on"
                    + "  W1.SCHOOLING_TYPE = W2.SCHOOLING_TYPE"
                    + " where"
                    + "  W1.YEAR = '" + _param._year + "'"
                    + "  and W1.CLASSCD = '" + _classcd + "'"
                    + "  and W1.CURRICULUM_CD = '" + _curriculumCd + "'"
                    + "  and W1.SUBCLASSCD = '" + _subclassCd + "'"
                    + "  and W1.SCHREGNO = '" + schregno + "'"
                    + " group by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO, W1.SCHOOLING_TYPE, W2.SCHOOLING_DIV"
                    + " order by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO, W1.SCHOOLING_TYPE, W2.SCHOOLING_DIV"
                    ;
            return sql;
        }
    }

    public List createLineData(
            final DB2UDB db2,
            final String schregno
    ) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlLineData(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscde = rs.getString("subclasscde");
            final String credits = nvlT(rs.getString("credits"));

            final LineData lineData = new LineData(
                    classcd,
                    curriculumCd,
                    subclasscde,
                    credits
            );
            rtn.add(lineData);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>>学籍番号:" + schregno + " のデータが空っぽ");
        }

        return rtn;
    }

    private String sqlLineData(final String schregno) {
        final String sql;
        sql = " select"
        		+ "    W1.CLASSCD as classcd,"
        		+ "    W1.CURRICULUM_CD as curriculumCd,"
        		+ "    W1.SUBCLASSCD as subclasscde,"
        		+ "    W2.CREDITS as credits"
        		+ " from"
        		+ "    COMP_REGIST_DAT W1"
        		+ " left join SUBCLASS_DETAILS_MST W2 on "
        		+ "    W1.YEAR          = W2.YEAR and"
        		+ "    W1.CLASSCD       = W2.CLASSCD and"
        		+ "    W1.CURRICULUM_CD = W2.CURRICULUM_CD and"
        		+ "    W1.SUBCLASSCD    = W2.SUBCLASSCD"
                + " left join SUBCLASS_MST W3 on "
                + "    W1.CLASSCD       = W3.CLASSCD and"
                + "    W1.CURRICULUM_CD = W3.CURRICULUM_CD and"
                + "    W1.SUBCLASSCD    = W3.SUBCLASSCD"
                + " left join CLASS_MST W4 on "
                + "    W1.CLASSCD       = W4.CLASSCD"
        		+ " where"
        		+ "    W1.YEAR ='" + _param._year + "'"
        		+ "    and W1.SCHREGNO = '" + schregno + "'"
                + " order by W4.SHOWORDER3, W4.CLASSCD, W3.SHOWORDER3, W3.CURRICULUM_CD, W3.SUBCLASSCD"
        		;
        return sql;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {
        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    // ======================================================================
    /**
     * 通信スクーリング割合。
     */
    private class RecSchoolingRateDat {
        private final Integer _rate;
  
        RecSchoolingRateDat(final Integer rate) {
            _rate = (null == rate) ? ZERO : rate;
        }
    }
  
    // ======================================================================
    public final static Integer ZERO = new Integer(0);
    /**
     * 通信スクーリング実績。
     */
    private class RecSchoolingDat {
        private final String _schoolingType;

        /** 修得時間分. */
        private final Integer _getValue;

        /** 登校or代替区分. */
        private final String _schoolingDiv;

        private final boolean _is登校schooling;
        private final boolean _is代替schooling;

        RecSchoolingDat(
                final String schoolingType,
                final Integer getValue,
                final String schoolingDiv
        ) {
            _schoolingType = schoolingType;
            _getValue = (null == getValue) ? ZERO : getValue;
            _schoolingDiv = schoolingDiv;
            _is登校schooling = "01".equals(_schoolingDiv) ? true : false;
            _is代替schooling = "02".equals(_schoolingDiv) ? true : false;
        }

        public String toString() {
            final String hoge = _is登校schooling ? "登校S" : "代替S";
            return _schoolingType + "/" + _getValue + "/" + hoge;
        }
    }
  
    // ======================================================================
    /**
     * レポート実績。
     */
    private class RecReportDat {
        private final int _reportSeq;
        private final String _commitedScore1;
        private final String _commitedScore2;
  
        RecReportDat(
                final int reportSeq,
                final String commitedScore1,
                final String commitedScore2
        ) {
            _reportSeq = reportSeq;
            _commitedScore1 = commitedScore1;
            _commitedScore2 = commitedScore2;
        }
    }
  
    // ======================================================================
    /**
     * テスト実績。
     */
    private class RecTestDat {
        private final String _month;
        private final Integer _score;
  
        RecTestDat(final String month, final Integer score) {
            _month = month;
            _score = score;
        }
    }
} // KNJWD730

// eof
