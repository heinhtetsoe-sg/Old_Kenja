// kanji=漢字
/*
 * $Id: 33991c4dfb64398fcca6f8662d9896b1d82d0c53 $
 *
 * 作成日: 2007/12/26 18:29:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.text.ParseException;
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
 *  合格手続状況（一次・二次）
 * @author nakada
 * @version $Id: 33991c4dfb64398fcca6f8662d9896b1d82d0c53 $
 */
public class KNJL377J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL377J.class);

    private static final String FORM_FILE = "KNJL377J.frm";

    /*
     * 文字数による出力項目切り分け基準
     */
    /** 名前 */
    private static final int NAME_LENG = 10;
    /** 住所 */
    private static final int ADDRESS_LENG = 25;

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";
    private static final String NAME_MST_TESTDIV = "L004";
    private static final String NAME_MST_EXAM_TYPE = "L005";

    /*
     * 手続区分
     */
    /** 済み */
    private static final String PROCEDURE_FINISH = "1";

    /*
     * 入学区分
     */
    /** 辞退 */
    private static final String ENTDIV_REFUSAL = "2";

    /*
     * 記号
     */
    /** 辞退 */
    private static final String SIGN_REFUSAL = "レ";

    /*
     * 合否区分
     */
    /** 合格 */
    private static final String JUDGEDIV_PASS = "1";

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

        _form = new Form(FORM_FILE, response);

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            final List entexamApplicantbaseDats = createEntexamApplicantbaseDats(db2);

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

        int i = 0; // １ページあたり件数
        int no = 0; // №

        int manNum = 0;
        int womanNum = 0;

        if (applicants != null) {
            _dataCnt = applicants.size();
            _totalPage = getTotalPage(applicants);
        }

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamApplicantbaseDat applicant = (EntexamApplicantbaseDat) it.next();

            i++;
            no++;


            printApplicant(i, no, applicant);

                if (applicant._sex.equals(SEX_MAN)) {
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

    private void printApplicant(int i, int no, EntexamApplicantbaseDat applicant) {
        /* NO */
        _form._svf.VrsOutn("NO", i, Integer.toString(no));
        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, applicant._examno);
        /* 志願者氏名 */
        _form.printName(i, applicant);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._sex));
        /* 手続区分１ */
        _form.printProcedure1(i, applicant);
        /* 手続区分２ */
        _form.printProcedure2(i, applicant);
        /* 辞退者 */
        _form.printDecliner(i, applicant);
        /* 合格入試区分 */
        _form._svf.VrsOutn("PASSDIV", i, _param._testDivString(applicant._entexamReceptDats._testDiv));        
        /* ２科／３科 */
        _form._svf.VrsOutn("EXAM_TYPE", i, _param._examTypeString(applicant._entexamReceptDats._examType));        
        /* 郵便番号 */
        _form._svf.VrsOutn("ZIPCD", i, applicant._entexamApplicantaddrDat._zip);
        /* 住所 */
        _form.printAddr(applicant, i);
        /* 電話番号 */
        _form._svf.VrsOutn("TELNO", i, applicant._entexamApplicantaddrDat._telno);
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
        private Map _testDivMap;
        private Map _examTypeMap;

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

        public String _testDivString(String testDiv) {
            return (String) _testDivMap.get(testDiv);
        }

        public String _examTypeString(String examType) {
            return (String) _examTypeMap.get(examType);
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);
            _testDivMap = getNameMst(NAME_MST_TESTDIV);
            _examTypeMap = getNameMst(NAME_MST_EXAM_TYPE);

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

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 1);
        }

        public void printProcedure1(int i, EntexamApplicantbaseDat applicant) {
            String str = "";

            try {
                if ((applicant._entexamReceptDats._proceduredate1 != null) &&
                        (applicant._entexamReceptDats._procedurediv1 != "") &&
                        applicant._entexamReceptDats._procedurediv1.equals(PROCEDURE_FINISH)) {
                    final Calendar cal = KNJServletUtils.parseDate(applicant._entexamReceptDats._proceduredate1);
                    int month = cal.get(Calendar.MONTH) + 1;
                    int dom = cal.get(Calendar.DAY_OF_MONTH);

                    str = month + "/" +  dom;

                }
            } catch (ParseException e) {
                    // 何も印字しない
            }

            _form._svf.VrsOutn("PROCEDUREDIVDATE1", i, str);
        }

        public void printProcedure2(int i, EntexamApplicantbaseDat applicant) {
            String str = "";

            try {
                if ((applicant._proceduredate != null) &&
                        (applicant._procedurediv != null)) {
                    if (applicant._procedurediv.equals(PROCEDURE_FINISH)) {
                        final Calendar cal = KNJServletUtils.parseDate(applicant._proceduredate);
                        int month = cal.get(Calendar.MONTH) + 1;
                        int dom = cal.get(Calendar.DAY_OF_MONTH);
    
                        str = month + "/" +  dom;
                    }
                }
            } catch (ParseException e) {
                // 何も印字しない
            }

            _form._svf.VrsOutn("PROCEDUREDIVDATE2", i, str);
        }

        public void printDecliner(int i, EntexamApplicantbaseDat applicant) {
            if (applicant._entdiv.equals(ENTDIV_REFUSAL)) {
                _form._svf.VrsOutn("MARK", i, SIGN_REFUSAL);
            }
        }

        public void printName(int i, EntexamApplicantbaseDat applicant) {
            String name = applicant._name;

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

        public void printAddr(EntexamApplicantbaseDat applicant, int i) {
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
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _applicantdiv;     // 入試制度
        private final String _examno;           // 受験番号
        private final String _judgement;           // 合否判定
        private final String _procedurediv;     // 合否判定
        private final String _proceduredate;    // 2次手続日
        private final String _entdiv;           // 入学区分
        private final String _name;
        private final String _sex;
        private final String _specialMeasures;  // 特別措置区分

        private EntexamReceptDat _entexamReceptDats;                // 志願者受付データ
        private EntexamApplicantaddrDat _entexamApplicantaddrDat;   // 志願者住所データ

        EntexamApplicantbaseDat(
                final String applicantdiv,
                final String examno,
                final String judgement,
                final String procedurediv,
                final String proceduredate,
                final String entdiv,
                final String name,
                final String sex,
                final String specialMeasures
        ) {
            _applicantdiv = applicantdiv;
            _examno = examno;
            _judgement = judgement;
            _procedurediv = procedurediv;
            _proceduredate = proceduredate;
            _entdiv = entdiv;
            _name = name;
            _sex = sex;
            _specialMeasures = specialMeasures;
        }

        public void load(DB2UDB db2o) throws SQLException, Exception {
            _entexamReceptDats = createEntexamReceptDat(db2, _examno);
            _entexamApplicantaddrDat = createEntexamApplicantaddrDat(db2, _examno);
        }
    }

    private List createEntexamApplicantbaseDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDat());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String applicantdiv = rs.getString("applicantdiv");
            final String examno = rs.getString("examno");
            final String judgement = rs.getString("judgement");
            final String procedurediv = rs.getString("procedurediv");
            final String proceduredate = rs.getString("proceduredate");
            final String entdiv = rs.getString("entdiv");
            final String name = rs.getString("name");
            final String sex = rs.getString("sex");
            final String specialMeasures = rs.getString("specialMeasures");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    applicantdiv,
                    examno,
                    judgement,
                    procedurediv,
                    proceduredate,
                    entdiv,
                    name,
                    sex,
                    specialMeasures
            );

            entexamApplicantbaseDat.load(db2);
            rtn.add(entexamApplicantbaseDat);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>EntexamApplicantbaseDat に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlEntexamApplicantbaseDat() {
        return " select"
                + "    APPLICANTDIV as applicantdiv,"
                + "    EXAMNO as examno,"
                + "    JUDGEMENT as judgement,"
                + "    PROCEDUREDIV as procedurediv,"
                + "    PROCEDUREDATE as proceduredate,"
                + "    value(ENTDIV, '') as entdiv,"
                + "    NAME as name,"
                + "    SEX as sex,"
                + "    SPECIAL_MEASURES as specialMeasures"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    JUDGEMENT = '" + JUDGEDIV_PASS + "'"
                + "    order by EXAMNO"
                ;
    }
       
      

    // ======================================================================
    /**
     * 志願者受付データ。
     */
    private class EntexamReceptDat {
        private final String _testDiv;          // 入試区分
        private final String _examType;         // 受験型
        private final String _judgeDiv;         // 合否区分
        private final String _procedurediv1;    // 1次手続区分
        private final String _proceduredate1;   // 1次手続日
         

        EntexamReceptDat() {
            _testDiv = "";
            _examType = "";
            _judgeDiv = "";
            _procedurediv1 = "";
            _proceduredate1 = "";
        }
        EntexamReceptDat(
                final String testDiv,
                final String examType,
                final String judgeDiv,
                final String procedurediv1,
                final String proceduredate1
        ) {
            _testDiv = testDiv;
            _examType = examType;
            _judgeDiv = judgeDiv;
            _procedurediv1 = procedurediv1;
            _proceduredate1 = proceduredate1;
        }
    }

    private EntexamReceptDat createEntexamReceptDat(final DB2UDB db2, String pExamNo)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

            ps = db2.prepareStatement(sqlEntexamApplicantbaseDats(pExamNo));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testDiv = rs.getString("testDiv");
                final String examType = rs.getString("examType");
                final String judgeDiv = rs.getString("judgeDiv");
                final String procedurediv1 = rs.getString("procedurediv1");
                final String proceduredate1 = rs.getString("proceduredate1");

                final EntexamReceptDat entexamReceptDat = new EntexamReceptDat(
                        testDiv,
                        examType,
                        judgeDiv,
                        procedurediv1,
                        proceduredate1
                );

                return entexamReceptDat;
            }

        return new EntexamReceptDat();
    }

    private String sqlEntexamApplicantbaseDats(String pExamNo) {
        StringBuffer stb = new StringBuffer();
        stb.append(" select");
        stb.append("    TESTDIV as testDiv,");
        stb.append("    EXAM_TYPE as examType,");
        stb.append("    value(JUDGEDIV, '') as judgeDiv,");
        stb.append("    value(PROCEDUREDIV1, '') as procedurediv1,");
        stb.append("    PROCEDUREDATE1 as proceduredate1");
        stb.append(" from");
        stb.append("    ENTEXAM_RECEPT_DAT");
        stb.append(" where" );
        stb.append("    ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and EXAMNO = '" + pExamNo + "'");
        stb.append("    and JUDGEDIV = '" + JUDGEDIV_PASS + "'");
        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者住所データ。
     */
    private class EntexamApplicantaddrDat {
        private final String _zip;          // 住所1
        private final String _address1;     // 住所1
        private final String _address2;     // 住所2（方書）
        private final String _telno;

        EntexamApplicantaddrDat() {
            _zip = "";
            _address1 = "";
            _address2 = "";
            _telno = "";
        }

        EntexamApplicantaddrDat(
                final String zip,
                final String address1,
                final String address2,
                final String telno
        ) {
            _zip = zip;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
        }
    }

    private EntexamApplicantaddrDat createEntexamApplicantaddrDat(final DB2UDB db2, String pExamNo)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantaddrDat(pExamNo));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String zip = rs.getString("zip");
            final String address1 = rs.getString("address1");
            final String address2 = rs.getString("address2");
            final String telno = rs.getString("telno");

            final EntexamApplicantaddrDat entexamApplicantaddrDat = new EntexamApplicantaddrDat(
                    zip,
                    address1,
                    address2,
                    telno
            );

            return entexamApplicantaddrDat;
        }

        return new EntexamApplicantaddrDat();

    }

    private String sqlEntexamApplicantaddrDat(String pExamNo) {
        return " select"
                + "    ZIPCD as zip,"
                + "    ADDRESS1 as address1,"
                + "    ADDRESS2 as address2,"
                + "    GTELNO as telno"
                + " from"
                + "    ENTEXAM_APPLICANTADDR_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + pExamNo + "'"
                ;
    }
} // KNJL377J

// eof
