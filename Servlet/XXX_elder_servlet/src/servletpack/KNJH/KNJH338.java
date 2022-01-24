// kanji=漢字
/*
 * $Id: 789875cd611fcf10a293a32e2082e067147d3d9d $
 *
 * 作成日: 2007/12/07 10:58:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

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
 *  受験出欠表
 * @author nakada
 * @version $Id: 789875cd611fcf10a293a32e2082e067147d3d9d $
 */
public class KNJH338 {
    /* pkg */static final Log log = LogFactory.getLog(KNJH338.class);

    private static final String FORM_FILE = "KNJH338.frm";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 50;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    private int _page;
    private int _totalPage;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            log.debug(">>年組=" + _param._gradeHrClass);
            log.debug(">>模試コード=" + _param._mockCd);
            log.debug(">>テスト科目=" + _param._mockSubclassCd);

            final List students = createSchregRegdDats(db2);

            printMain(students);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List students) 
        throws SQLException, Exception {

        int i = 0; // １ページあたり件数

        int studentNum = 0;
        int avrMark = 0;
        int lowMark = 999;
        int highMark = 0;
        int totalScore = 0;

        for (Iterator it = students.iterator(); it.hasNext();) {
            final SchregRegdDat student = (SchregRegdDat) it.next();
            printHeader();

            i++;

            if (student._mockDat._val1) {
                studentNum++;
                totalScore += student._mockDat._score;
            }

            printStudent(i, student);

            if (student._mockDat._val1) {
                avrMark += student._mockDat._score;
    
                if (student._mockDat._score < lowMark) {
                    lowMark = student._mockDat._score;
                }
    
                if (student._mockDat._score > highMark) {
                    highMark = student._mockDat._score;
                }
            }

            if (i >= DETAILS_MAX) {
                i = 0;
            }
            _form._svf.VrEndRecord();
            _hasData = true;
        }

        printFooter(avrMark, lowMark, highMark, totalScore, i, studentNum);

    }

    private void printHeader() {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "度");
        /* テスト名称 */
        _form._svf.VrsOut("MOCKNAME", _param._mockMst._name + "点票");
        /* 科目名称 */
        _form._svf.VrsOut("SUBCLASS_ABBV", _param._mockSubclassMst._name);
        /* 学級名称 */
        _form._svf.VrsOut("HR_NAME", _param._schregRegdHdat._hrName);
        /* 担任者名称 */
        _form._svf.VrsOut("STAFF_NAME", _param._trName);
        /* 担当者名称 */
        _form._svf.VrsOut("STAFF_NAME2", _param._staffName);
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
    }

    private void printFooter(
            final int avrMark,
            final int lowMark,
            final int highMark,
            final int totalScore,
            final int i,
            final int studentNum
    ) {
        printHeader();

        for (int j = i + 1; j <= DETAILS_MAX; j++) {
            String fieldNo = "";
            if (j % 5 == 0) {
                fieldNo = "5";
            } else {
            }
            _form._svf.VrsOut("KARA" + fieldNo, "1");
            _form._svf.VrEndRecord();
        }

        double scale1 = 0;
        if (studentNum > 0) {
            double db = (double)avrMark / (double)studentNum;
            BigDecimal bd = new BigDecimal(String.valueOf(db));
            scale1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        }
        /* 合計 */
        _form._svf.VrsOut("ITEMNAME", "合計");
        _form._svf.VrsOut("ITEM_SCORE", String.valueOf(totalScore));
        _form._svf.VrEndRecord();

        /* 人数 */
        _form._svf.VrsOut("ITEMNAME", "人数");
        _form._svf.VrsOut("ITEM_SCORE", String.valueOf(studentNum));
        _form._svf.VrEndRecord();

        /* 平均点 */
        _form._svf.VrsOut("ITEMNAME", "平均点");
        _form._svf.VrsOut("ITEM_SCORE", String.valueOf(scale1));
        _form._svf.VrEndRecord();
    }

    private void printStudent(final int i, final SchregRegdDat student) {
        String fieldNo = "";
        if (i % 5 == 0) {
            fieldNo = "5";
        }
        /* 出席番号 */
        _form._svf.VrsOut("ATTENDNO" + fieldNo, student._attendNo);
        /* 学生番号 */
        _form._svf.VrsOut("SCHREGNO" + fieldNo, student._schregNo);
        /* 氏名 */
        _form._svf.VrsOut("NAME" + fieldNo, student._schregBaseMst._name);

        if (student._mockDat._val1) {
            /* 得点 */
            _form._svf.VrsOut("SCORE" + fieldNo, Integer.toString(student._mockDat._score));
        }
        if (student._mockDat._val3) {
            /* 席次 */
            _form._svf.VrsOut("RANK" + fieldNo, Integer.toString(student._mockDat._rank));
        }
        if (student._mockDat._val2) {
            /* 偏差値 */
            _form._svf.VrsOut("DEVIATION" + fieldNo, student._mockDat._deviation.toString());
        }
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
        private final String _prgrId;
        private final String _dbName;
        private final String _loginDate;
        private final String _gradeHrClass;
        private final String _grade;
        private final String _hrClass;
        private final String _mockCd;
        private final String _mockSubclassCd;
        private final String _staffCd;

        private SchregRegdHdat _schregRegdHdat;
        private String _trName;
        private String _staffName;
        private MockMst _mockMst;
        private MockSubclassMst _mockSubclassMst;
        
        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String gradeHrClass,
                final String grade,
                final String hrClass,
                final String mockCd,
                final String mockSubclassCd,
                final String staffCd
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _gradeHrClass = gradeHrClass;
            _grade = grade;
            _hrClass = hrClass;
            _mockCd = mockCd;
            _mockSubclassCd = mockSubclassCd;
            _staffCd = staffCd;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregRegdHdat = createSchregRegdHdat(db2);

            _trName = "";
            Staff staff = new Staff();
            if (_schregRegdHdat._trCd1 != null) {
                staff = createStaff(db2, _schregRegdHdat._trCd1);
                _trName += staff._name != "" ? (staff._name) : "";
            }
            if (_schregRegdHdat._trCd2 != null) {
                staff = createStaff(db2, _schregRegdHdat._trCd2);
                _trName += staff._name != "" ? ("," + staff._name) : "";
            }
            if (_schregRegdHdat._trCd3 != null) {
                staff = createStaff(db2, _schregRegdHdat._trCd3);
                _trName += staff._name != "" ? ("," + staff._name) : "";
            }
            if (_staffCd != null) {
                staff = createStaff(db2, _staffCd);
                _staffName = staff._name;
            }

            _mockMst = createMockMst(db2, _param._mockCd);
            _mockSubclassMst = createMockSubclassMst(db2, _param._mockSubclassCd);

            return;
        }

        private Map getNameMst(String nameCd1) throws SQLException {
            final String sql = sqlNameMst(nameCd1);
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String name = rs.getString("name");
                    final String code = rs.getString("code");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlNameMst(String nameCd1) {
            return " select"
                    + "    NAME1 as name,"
                    + "    NAMECD2 as code"
                    + " from"
                    + "    V_NAME_MST"
                    + " where"
                    + "    year = '" + _year + "' AND"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String gradeHrClass = request.getParameter("GRADE_HR_CLASS");
        final String grade = gradeHrClass.substring(0, 2);
        final String hrClass = gradeHrClass.substring(2, 5);
        final String mockCd = request.getParameter("MOCK_TARGET");
        final String mockSubclassCd = request.getParameter("MOCK_SUBCLASS_CD");
        final String staffCd = request.getParameter("STAFFCD");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                gradeHrClass,
                grade,
                hrClass,
                mockCd,
                mockSubclassCd,
                staffCd
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 69867 $ $Date: 2019-09-25 14:25:36 +0900 (水, 25 9 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================
    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
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
     * 学籍在籍ヘッダデータ。
     */
    private class SchregRegdHdat {
        private final String _hrName;       // 組名称
        private final String _trCd1;        // 担任コード1
        private final String _trCd2;        // 担任コード2
        private final String _trCd3;        // 担任コード3

        SchregRegdHdat(final String hrName,
                final String trCd1,
                final String trCd2,
                final String trCd3
        ) {
            _hrName = hrName;
            _trCd1 = trCd1;
            _trCd2 = trCd2;
            _trCd3 = trCd3;
        }
    }

    private SchregRegdHdat createSchregRegdHdat(final DB2UDB db2) throws SQLException, Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdHdat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String hrName = rs.getString("hrName");
            final String trCd1 = rs.getString("trCd1");
            final String trCd2 = rs.getString("trCd2");
            final String trCd3 = rs.getString("trCd3");

            final SchregRegdHdat schregRegdHdat = new SchregRegdHdat(
                    hrName,
                    trCd1,
                    trCd2,
                    trCd3
            );

            return schregRegdHdat;
        }

        log.debug(">>>SchregRegdHdat に該当するものがありません。");
        throw new Exception();
    }

    private String sqlSchregRegdHdat() {
        return " select"
                + "    HR_NAME as hrName,"
                + "    TR_CD1 as trCd1,"
                + "    TR_CD2 as trCd2,"
                + "    TR_CD3 as trCd3"
                + " from"
                + "    SCHREG_REGD_HDAT"
                + " where" 
                + "    YEAR = '" + _param._year + "' and"
                + "    SEMESTER = '" + _param._semester + "' and"
                + "    GRADE = '" + _param._grade + "' and"
                + "    HR_CLASS = '" + _param._hrClass + "'";
    }

    // ======================================================================
    /**
     * 職員。
     */
    private class Staff {
        private final String _name; // 職員氏名

        Staff(final String name) {
            _name = name;
        }

        public Staff() {
            _name = "";
        }
    }

    private Staff createStaff(DB2UDB db2, String staffCd) throws SQLException {
        final String sql = sqlStaff(staffCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String staffname = rs.getString("staffName");

            final Staff staff = new Staff(staffname);
            return staff;
        }

        return new Staff();
    }

    private String sqlStaff(String staffCd) {
        return " select"
                + "    STAFFNAME as  staffName"
                + " from"
                + "    STAFF_MST"
                + " where"
                + "    STAFFCD = '" + staffCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 模試名称マスタ。
     */
    private class MockMst {
        private final String _name;

        MockMst(final String name) {
            _name = name;
        }

        public MockMst() {
            _name = "";
        }
    }

    private MockMst createMockMst(DB2UDB db2, String mockCd) throws SQLException {
        final String sql = sqlMockMst(mockCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");

            final MockMst mockMst = new MockMst(name);
            return mockMst;
        }

        return new MockMst();
    }

    private String sqlMockMst(String mockCd) {
        return " select"
                + "    MOCKNAME1 as  name"
                + " from"
                + "    MOCK_MST"
                + " where"
                + "    MOCKCD = '" + mockCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 模試科目マスタ。
     */
    private class MockSubclassMst {
        private final String _name;

        MockSubclassMst(final String name) {
            _name = name;
        }

        public MockSubclassMst() {
            _name = "";
        }
    }

    private MockSubclassMst createMockSubclassMst(DB2UDB db2, String mockSubclassCd) throws SQLException {
        final String sql = sqlMockSubclassMst(mockSubclassCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");

            final MockSubclassMst mockSubclassMst = new MockSubclassMst(name);
            return mockSubclassMst;
        }

        return new MockSubclassMst();
    }

    private String sqlMockSubclassMst(String mockSubclassCd) {
        return " select"
                + "    SUBCLASS_NAME as  name"
                + " from"
                + "    MOCK_SUBCLASS_MST"
                + " where"
                + "    MOCK_SUBCLASS_CD = '" + mockSubclassCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _schregNo;
        private final String _attendNo;

        private SchregBaseMst _schregBaseMst;
        private MockDat _mockDat;

        SchregRegdDat(
                final String schregNo,
                final String attendNo
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregBaseMst = createSchregBaseMst(db2, _schregNo);
            _mockDat = createMockDat(db2, _schregNo);
        }
    }

    public List createSchregRegdDats(DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlSchregRegdDat());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String attendNo = rs.getString("attendNo");

                final SchregRegdDat schregRegdDat = new SchregRegdDat(
                        schregNo,
                        attendNo
                );

                schregRegdDat.load(db2);
                rtn.add(schregRegdDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>SchregRegdDat に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlSchregRegdDat() {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    ATTENDNO as attendNo"
                + " from"
                + "    SCHREG_REGD_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "' and"
                + "    SEMESTER = '" + _param._semester + "' and"
                + "    GRADE = '" + _param._grade + "' and"
                + "    HR_CLASS = '" + _param._hrClass + "'"
                + " order by attendNo";
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class SchregBaseMst {
        private final String _name;

        SchregBaseMst(final String name) {
            _name = name;
        }

        SchregBaseMst() {
            _name = "";
        }
    }

    private SchregBaseMst createSchregBaseMst(final DB2UDB db2, String schregno) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregBaseMst(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");
            final SchregBaseMst schregBaseMst = new SchregBaseMst(name);

            return schregBaseMst;
        }

        return new SchregBaseMst();
    }

    private String sqlSchregBaseMst(String schregno) {
        return " select"
                + "    NAME as name"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
    }

    // ======================================================================
    /**
     * 模試データ。
     */
    private class MockDat {
        private final boolean _val1;
        private final int _score;
        private final boolean _val2;
        private final BigDecimal _deviation;
        private final boolean _val3;
        private final int _rank;

        MockDat(
                final boolean val1,
                final int score,
                final boolean val2,
                final BigDecimal deviation,
                final boolean val3,
                final int rank
        ) {
            _val1 = val1;
            _score = score;
            _val2 = val2;
            _deviation = deviation;
            _val3 = val3;
            _rank = rank;
        }

        public MockDat() {
            _val1 = false;
            _score = 0;
            _val2 = false;
            _deviation = new BigDecimal("0");
            _val3 = false;
            _rank = 0;
        }
    }

    private MockDat createMockDat(DB2UDB db2, String schregNo) throws SQLException {
        final String sql = sqlMockDat(schregNo);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            boolean val1 = false;
            boolean val2 = false;
            boolean val3 = false;

            if (rs.getString("score") != null) {
                val1 = true;
            }
            if (rs.getString("deviation") != null) {
                val2 = true;
            }
            if (rs.getString("rank") != null) {
                val3 = true;
            }

            final int score = rs.getString("score") != null ? Integer.parseInt(rs.getString("score")) : 0; 

            BigDecimal bd = new BigDecimal("0");
            if (rs.getString("deviation") != null) {
                bd = new BigDecimal(rs.getString("deviation"));
            }
            final BigDecimal deviation = bd; 
            
            final int rank = rs.getString("rank") != null ? Integer.parseInt(rs.getString("rank")) : 0; 

            final MockDat mockDat = new MockDat(
                    val1,
                    score,
                    val2,
                    deviation,
                    val3,
                    rank
            );

            return mockDat;
        }

        return new MockDat();
    }

    private String sqlMockDat(String schregNo) {
        return " select"
                + "    score as  score,"
                + "    DEVIATION as  deviation,"
                + "    RANK as  rank"
                + " from"
                + "    MOCK_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "' and"
                + "    MOCKCD = '" + _param._mockCd + "' and"
                + "    SCHREGNO = '" + schregNo + "' and"
                + "    MOCK_SUBCLASS_CD = '" + _param._mockSubclassCd + "'"
                ;
    }
} // KNJH338

// eof
