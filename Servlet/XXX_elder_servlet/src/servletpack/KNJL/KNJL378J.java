// kanji=漢字
/*
 * $Id: e4399af15bc9667f1e19cb10f70c84b0ecb3a507 $
 *
 * 作成日: 2007/12/12 09:28:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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
 *  合否判定資料
 * @author nakada
 * @version $Id: e4399af15bc9667f1e19cb10f70c84b0ecb3a507 $
 */
public class KNJL378J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL378J.class);


    /*
     * 文字数による出力項目切り分け基準
     */
    /** 名前 */
    private static final int NAME_LENG = 10;
    /** 住所 */
    private static final int ADDRESS_LENG = 25;

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";

    /*
     * 受験者区分
     */
    /** 有り */
    private static final String EXAMINEE_DIV_AVAILABLE = "1";
    /** 無し */
    private static final String EXAMINEE_DIV_UNAVAILABLE = "2";

    /*
     * 合否区分
     */
    /** 合格 */
    private static final String JUDGEDIV_PASS = "1";
    /** 不合格 */
    private static final String JUDGEDIV_FAILURE = "2";

    /*
     * 特別措置区分
     */
    /** 繰上合格 */
    private static final String SPECIAL_MEASURES_MOVING_UP_PASS = "1";

    /*
     * 入試区分
     */
    /** Ａ１： */
    private static final String TESTDIV_A1 = "1";
    /** Ａ２： */
    private static final String TESTDIV_A2 = "2";
    /** Ｂ： */
    private static final String TESTDIV_B = "3";
    /** Ｃ： */
    private static final String TESTDIV_C = "4";
    /** Ｄ： */
    private static final String TESTDIV_D = "6";
    /** 帰国生： */
    private static final String TESTDIV_RETURN = "5";

    /*
     * 記号
     */
    /** 合格 */
    private static final String SIGN_PASS = "◎";
    private static final String SIGN_FAILURE = "×";
    private static final String SIGN_NON_EXAMINATION = "-";
    private static final String SIGN_MOVING_UP_PASS = "◇";

    /*
     * 性別
     */
    /** 性別：男 */
    private static final String SEX_MAN = "1";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 20;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    private int _dataCnt;
    private int _page;
    private int _totalPage;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(response);

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            final List entexamApplicantbaseDats = createEntexamDesireDats(db2);

            printMain(entexamApplicantbaseDats);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List applicants) 
        throws SQLException, Exception {

        final String formName = (_param._infuruFlg) ? "KNJL378J_2.frm" : "KNJL378J.frm";
        _form._svf.VrSetForm(formName, 1);

        int i = 0; // １ページあたり件数
        int no = 0; // №

        int manNum = 0;
        int womanNum = 0;

        if (applicants != null) {
            _dataCnt = applicants.size();
            _totalPage = getTotalPage(applicants);
        }

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamDesireDat applicant = (EntexamDesireDat) it.next();

            i++;
            no++;


            printApplicant(i, no, applicant);

                if (applicant._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                    manNum++;
                } else {
                    womanNum++;
                }

                if (_dataCnt == no) {
                    printFooter(manNum, womanNum);
                }

            if (i >= DETAILS_MAX) {
                prtHeader();

                i = 0;
            }
        }

        if (i > 0) {
            printFooter(manNum, womanNum);
            prtHeader();
        }
    }

    private void prtHeader() {
        printHeader();

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private int getTotalPage(List applicants) {
        int cnt = 0;
        int totalPage = 0;

        cnt = applicants.size();
        
        totalPage = cnt / DETAILS_MAX;
        if (cnt % DETAILS_MAX != 0) {
            totalPage++;
        }

        return totalPage;
    }

    private void printHeader() {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 総ページ */
        _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
    }

    private void printFooter(int manNum, int womanNum) {
        /* 性別人数 */
        _form._svf.VrsOut("NOTE", "男 " + Integer.toString(manNum)
                + "名、女 " + Integer.toString(womanNum)
                + "名、合計 " + (Integer.toString(manNum + womanNum)) + "名"
        );
    }

    private void printApplicant(int i, int no, EntexamDesireDat applicant) {
        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, applicant._examNo);
        /* 志願者氏名 */
        _form.printName(i, applicant);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._entexamApplicantbaseDat._sex));
        /* 住所 */
        _form.printAddr(applicant, i);
        /* 合否 */
        _form.printResults(applicant, i);
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

        private Map _sexMap;

        private boolean _infuruFlg;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);
            _infuruFlg = getInfuruFlg(db2);

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
                    final String code = rs.getString("code");
                    final String name = rs.getString("name");
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
                    + "    NAMECD2 as code,"
                    + "    NAME1 as name"
                    + " from"
                    + "    V_NAME_MST"
                    + " where"
                    + "    year = '" + _year + "' AND"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }

        private boolean getInfuruFlg(final DB2UDB db2) throws SQLException {
            String str = "";
            final String sql = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1='L017' AND NAMECD2='01'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    str = rs.getString("NAMESPARE1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return "1".equals(str);
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate
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

        public Form(final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
        }

        public void printResults(EntexamDesireDat applicant, int i) {
            for (Iterator it = applicant._entexamReceptDats.iterator(); it.hasNext();) {
                final EntexamReceptDat entexamReceptDat = (EntexamReceptDat) it.next();
            
            String label = "";
            if (entexamReceptDat._testDiv.equals(TESTDIV_A1)) {
                label = "MARK1";
            } else if (entexamReceptDat._testDiv.equals(TESTDIV_A2)) {
                label = "MARK2";
            } else if (entexamReceptDat._testDiv.equals(TESTDIV_B)) {
                label = "MARK3";
            } else if (entexamReceptDat._testDiv.equals(TESTDIV_C)) {
                label = "MARK4";
            } else if (entexamReceptDat._testDiv.equals(TESTDIV_D)) {
                label = "MARK6";
            } else if (entexamReceptDat._testDiv.equals(TESTDIV_RETURN)) {
                label = "MARK5";
            }

            _form._svf.VrsOutn(label, i, getSign(applicant, entexamReceptDat));
            }
        }

        private String getSign(EntexamDesireDat applicant, final EntexamReceptDat entexamReceptDat) {
            String sign = "";
            if (entexamReceptDat._examineeDiv.equals(EXAMINEE_DIV_AVAILABLE) && entexamReceptDat._judgeDiv.equals(JUDGEDIV_PASS)) {
                sign = SIGN_PASS;
            } else if (entexamReceptDat._examineeDiv.equals(EXAMINEE_DIV_AVAILABLE) && entexamReceptDat._judgeDiv.equals(JUDGEDIV_FAILURE)) {
                sign = SIGN_FAILURE;
            } else if (entexamReceptDat._examineeDiv.equals(EXAMINEE_DIV_UNAVAILABLE)) {
                sign = SIGN_NON_EXAMINATION;
            }

            if (entexamReceptDat._judgeDiv.equals(JUDGEDIV_FAILURE) && applicant._entexamApplicantbaseDat._specialMeasures.equals(SPECIAL_MEASURES_MOVING_UP_PASS)) {
                sign = SIGN_MOVING_UP_PASS;
            }

            return sign;
        }

        public void printName(int i, EntexamDesireDat applicant) {
            String name = applicant._entexamApplicantbaseDat._name;

            if (name != null) {
                final String label;
                if (name.length() <= NAME_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2_1";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printAddr(EntexamDesireDat applicant, int i) {
            String name = (applicant._entexamApplicantaddrDat._address1 != null ?
                                applicant._entexamApplicantaddrDat._address1 : "")
                    + (applicant._entexamApplicantaddrDat._address2 != null ?
                            applicant._entexamApplicantaddrDat._address2 : "");

            if (name != null) {
                final String label;
                if (name.length() <= ADDRESS_LENG) {
                    label = "ADDRESS1";
                } else {
                    label = "ADDRESS2_1";
                }
                _form._svf.VrsOutn(label, i, name);
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
     * 志願者データ。
     */
    class EntexamDesireDat {
        private final String _applicantDiv;         // 入試制度
        private final String _testDiv;              // 入試区分
        private final String _examType;             // 受験型
        private final String _examNo;               // 受験番号
        private final String _receptNo;             // 受付№

        private List _entexamReceptDats;                            // 志願者受付データ
        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ
        private EntexamApplicantaddrDat _entexamApplicantaddrDat;   // 志願者住所データ

        EntexamDesireDat(
                final String applicantDiv,
                final String testDiv,
                final String examType,
                final String examNo,
                final String receptNo
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examType = examType;
            _examNo = examNo;
            _receptNo = receptNo;
        }

        public void load(DB2UDB db2o) throws SQLException, Exception {
            _entexamReceptDats = createEntexamApplicantbaseDats(db2, _applicantDiv, _examType, _examNo);
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, _applicantDiv, _examNo);
            _entexamApplicantaddrDat = createEntexamApplicantaddrDat(db2, _examNo);
        }
    }

    private List createEntexamDesireDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamDesireDats());
            rs = ps.executeQuery();

            String oldExamNo = null;
            while (rs.next()) {
                final String applicantDiv = rs.getString("applicantDiv");
                final String testDiv = rs.getString("testDiv");
                final String examType = rs.getString("examType");
                final String examNo = rs.getString("examNo");
                final String receptNo = rs.getString("receptNo");

                if (!examNo.equals(oldExamNo)) {
                    final EntexamDesireDat entexamDesireDat = new EntexamDesireDat(
                            applicantDiv,
                            testDiv,
                            examType,
                            examNo,
                            receptNo
                    );

                    entexamDesireDat.load(db2);
                    rtn.add(entexamDesireDat);
                    
                    oldExamNo = examNo;
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>EntexamDesireDat に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlEntexamDesireDats() {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    T1.ENTEXAMYEAR as entExamYear,");
        stb.append("    T1.APPLICANTDIV as applicantDiv,");
        stb.append("    T1.TESTDIV as testDiv,");
        stb.append("    T1.EXAM_TYPE as examType,");
        stb.append("    T1.EXAMNO as examNo,");
        stb.append("    value(T2.RECEPTNO, '') as receptNo");
        stb.append(" from");
        stb.append("    ENTEXAM_DESIRE_DAT T1 left join ENTEXAM_RECEPT_DAT T2 on (");
        stb.append("    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR");
        stb.append("    and T2.APPLICANTDIV    = T1.APPLICANTDIV");
        stb.append("    and T2.TESTDIV      = T1.TESTDIV");
        stb.append("    and T2.EXAM_TYPE   = T1.EXAM_TYPE");
        stb.append("    and T2.EXAMNO      = T1.EXAMNO)");
        stb.append(" where");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append(" order by T1.EXAMNO, T1.TESTDIV DESC");

        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者受付データ。
     */
    private class EntexamReceptDat {
        private final String _applicantDiv;     // 入試制度
        private final String _testDiv;          // 入試区分
        private final String _examno;           // 受験番号
        private final String _examType;         // 受験型
        private final String _examineeDiv;      // 受験者区分
        private final String _judgeDiv;         // 合否区分
         

        EntexamReceptDat(
                final String applicantDiv,
                final String testDiv,
                final String examno,
                final String examType,
                final String examineeDiv,
                final String judgeDiv
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examno = examno;
            _examType = examType;
            _examineeDiv = examineeDiv;
            _judgeDiv = judgeDiv;
        }
    }

    private List createEntexamApplicantbaseDats(final DB2UDB db2, String pApplicantDiv, String pExamType, String pExamNo)
        throws SQLException {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamApplicantbaseDats(pApplicantDiv, pExamType, pExamNo));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String applicantDiv = rs.getString("applicantDiv");
                final String testDiv = rs.getString("testDiv");
                final String examno = rs.getString("examno");
                final String examType = rs.getString("examType");
                final String examineeDiv = rs.getString("examineeDiv");
                final String judgeDiv = rs.getString("judgeDiv");

                final EntexamReceptDat entexamReceptDat = new EntexamReceptDat(
                        applicantDiv,
                        testDiv,
                        examno,
                        examType,
                        examineeDiv,
                        judgeDiv
                );

                rtn.add(entexamReceptDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String sqlEntexamApplicantbaseDats(String pApplicantDiv, String pExamType, String pExamNo) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    T1.APPLICANTDIV as applicantDiv,");
        stb.append("    T1.TESTDIV as testDiv,");
        stb.append("    T1.EXAMNO as examno,");
        stb.append("    T1.EXAM_TYPE as examType,");
        stb.append("    T1.EXAMINEE_DIV as examineeDiv,");
        stb.append("    value(T2.JUDGEDIV, '') as judgeDiv");
        stb.append(" from");
        stb.append("    ENTEXAM_DESIRE_DAT T1");
        stb.append("    left join ENTEXAM_RECEPT_DAT T2 on (");
        stb.append("    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR");
        stb.append("    and T2.APPLICANTDIV    = T1.APPLICANTDIV");
        stb.append("    and T2.TESTDIV    = T1.TESTDIV");
        stb.append("    and T2.EXAM_TYPE   = T1.EXAM_TYPE");
        stb.append("    and T2.EXAMNO      = T1.EXAMNO)");
        stb.append(" where" );
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and T1.APPLICANTDIV = '" + pApplicantDiv + "'");
//        stb.append("    and T1.EXAM_TYPE = '" + pExamType + "'");
        stb.append("    and T1.EXAMNO = '" + pExamNo + "'");
        stb.append("    and T1.TESTDIV is not NULL");
        stb.append(" order by T1.TESTDIV, T1.EXAM_TYPE, T1.EXAMNO");
        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _name;
        private final String _sex;
        private final String _specialMeasures;  // 特別措置区分

        EntexamApplicantbaseDat() {
            _name = "";
            _sex = "";
            _specialMeasures = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String sex,
                final String specialMeasures
        ) {
            _name = name;
            _sex = sex;
            _specialMeasures = specialMeasures;
        }
    }

    private EntexamApplicantbaseDat createEntexamApplicantbaseDat(final DB2UDB db2, String pApplicantDiv, String pExamNo)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamApplicantbaseDat(pApplicantDiv, pExamNo));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String name = rs.getString("name");
                final String sex = rs.getString("sex");
                final String specialMeasures = rs.getString("specialMeasures");
                
                final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                        name,
                        sex,
                        specialMeasures
                );
                
                return entexamApplicantbaseDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return new EntexamApplicantbaseDat();
    }

    private String sqlEntexamApplicantbaseDat(String pApplicantDiv, String pExamNo) {
        return " select"
                + "    NAME as name,"
                + "    SEX as sex,"
                + "    value(PRISCHOOLCD, '') as priSchoolCd,"
                + "    value(SPECIAL_MEASURES, '') as specialMeasures,"
                + "    VALUE(REMARK1, '') as remark1,"
                + "    VALUE(REMARK2, '') as remark2"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    APPLICANTDIV = '" + pApplicantDiv + "' and"
                + "    EXAMNO = '" + pExamNo + "'"
                ;
    }

    // ======================================================================
    /**
     * 志願者住所データ。
     */
    private class EntexamApplicantaddrDat {
        private final String _address1;     // 住所1
        private final String _address2;     // 住所2（方書）

        EntexamApplicantaddrDat() {
            _address1 = "";
            _address2 = "";
        }

        EntexamApplicantaddrDat(
                final String address1,
                final String address2
        ) {
            _address1 = address1;
            _address2 = address2;
        }
    }

    private EntexamApplicantaddrDat createEntexamApplicantaddrDat(final DB2UDB db2, String pExamNo)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamApplicantaddrDat(pExamNo));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String address1 = rs.getString("address1");
                final String address2 = rs.getString("address2");
                
                final EntexamApplicantaddrDat entexamApplicantaddrDat = new EntexamApplicantaddrDat(
                        address1,
                        address2
                );
                
                return entexamApplicantaddrDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return new EntexamApplicantaddrDat();

    }

    private String sqlEntexamApplicantaddrDat(String pExamNo) {
        return " select"
                + "    ADDRESS1 as address1,"
                + "    ADDRESS2 as address2"
                + " from"
                + "    ENTEXAM_APPLICANTADDR_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + pExamNo + "'"
                ;
    }
} // KNJL378J

// eof
