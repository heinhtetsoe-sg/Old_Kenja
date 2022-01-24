// kanji=漢字
/*
 * $Id: ae5db7b794929ef84766fd95706c8b9811b6c0b1 $
 *
 * 作成日: 2007/12/04 13:22:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *  合格通知書・入学許可証
 *  合格通知台帳・入学許可台帳
 * @author nakada
 * @version $Id: ae5db7b794929ef84766fd95706c8b9811b6c0b1 $
 */
public class KNJL327J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL327J.class);

    private static final String FORM_FILE = "KNJL327J_1.frm";
    private static final String FORM_FILE2 = "KNJL327J_2.frm";
    private static final String FORM_FILE3 = "KNJL327J_3.frm";
    private static final String FORM_FILE4 = "KNJL327J_4.frm";

    /** 氏名 */
    private static final int NAME_LENG = 11;
    /** 住所 */
    private static final int ADDRESS_LENG = 25;

    /*
     * 出力種別
     */
    /** 出力種別：合格通知書 */
    private static final String OUTPUT_1 = "1";
    /** 出力種別：合格通知書台帳 */
    private static final String OUTPUT_2 = "2";
    private static final String OUTPUT_2_TITLE_NAME = "合格通知書";
    /** 出力種別：入学許可証 */
    private static final String OUTPUT_3 = "3";
    /** 出力種別：入学許可証台帳 */
    private static final String OUTPUT_4 = "4";
    private static final String OUTPUT_4_TITLE_NAME = "入学許可証";
    /** 出力種別：特待合格通知書 */
    private static final String OUTPUT_5 = "5";

    /*
     * 出力詳細
     */
    /** 出力詳細：全員 */
    private static final String OUTPUT2_1 = "1";
    /** 出力詳細：受験者指定 */
    private static final String OUTPUT2_2 = "2";

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";
    private static final String NAME_MST_TESTDIV = "L004";

    /*
     * 入試区分
     */
    /** Ａ-１ */
    private static final String TESTDIV_A1 = "1";
    /** Ａ-２ */
    private static final String TESTDIV_A2 = "2";
    /** Ｂ */
    private static final String TESTDIV_B = "3";
    /** Ｃ */
    private static final String TESTDIV_C = "4";
    /** 帰国生 */
    private static final String TESTDIV_R = "5";
    /** 全て */
    private static final String TESTDIV_ALL = "0";

    /*
     * 合否区分
     */
    /** 合否区分：合格 */
    private static final String JUDGEDIV_OK = "1";

    /*
     * 手続区分
     */
    /** 手続区分：済み */
    private static final String PROCEDUREDIV1_OK = "1";

    /*
     * 合否判定
     */
    /** 合否判定：合格 */
    private static final String JUDGEMENT_OK = "1";

    /*
     * 2次手続区分
     */
    /** 2次手続区分：済み */
    private static final String PROCEDUREDIV_OK = "1";

    /*
     * 入学区分
     */
    /** 入学区分：済み */
    private static final String ENTDIV_CANCEL = "2";

    /*
     * イメージファイル名
     */
    /** イメージファイル名 */
    private static final String DOC_NAME = "SCHOOLNAME_STAMP";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 15;

    /*
     * タイトル
     */
    /** タイトル：合格通知書台帳 */
    private static final String TITLE_1 = "合格通知書台帳";
    /** タイトル：入学許可証台帳 */
    private static final String TITLE_2 = "入学許可証台帳";
    /** タイトル：全て */
    private static final String TITLE_3 = "全て";

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

            log.debug(">>入試区分=" + _param._testDiv);
            log.debug(">>出力種別=" + _param._printType);
            log.debug(">>出力詳細=" + _param._printRange);
            log.debug(">>受験番号開始=" + _param._examnoFrom);
            log.debug(">>受験番号終了=" + _param._examnoTo);
            log.debug(">>ドキュメントベースパス=" + _param._docBase);

            final List entexamApplicantbaseDats = createEntexamApplicantbaseDats(db2);

            _totalPage = entexamApplicantbaseDats.size() / DETAILS_MAX;
            if (entexamApplicantbaseDats.size() % DETAILS_MAX != 0) {
                _totalPage++;
            }

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

        formSet();

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamApplicantbaseDat applicant = (EntexamApplicantbaseDat) it.next();

            if (_param._printType.equals(OUTPUT_1) || _param._printType.equals(OUTPUT_5)) {
                prtStudent1(applicant);
            } else if ((_param._printType.equals(OUTPUT_2)) || (_param._printType.equals(OUTPUT_4))) {
                i++;
                no++;
                printApplicant2(i, no, applicant);

                if (i >= DETAILS_MAX) {
                    prtHeader();

                    i = 0;
                }
            } else if (_param._printType.equals(OUTPUT_3)) {
                prtStudent3(applicant);
            }
        }

        if ((_param._printType.equals(OUTPUT_2)) || (_param._printType.equals(OUTPUT_4))) {
            if (i > 0) {
                prtHeader();
            }
        }
    }

    private void prtHeader() {
        printHeader();

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void prtStudent3(final EntexamApplicantbaseDat applicant) {
        printApplicant3(applicant);
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void prtStudent1(final EntexamApplicantbaseDat applicant) {
        printApplicant1(applicant);
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void formSet() throws Exception {
        if (_param._printType.equals(OUTPUT_1)) {
            _form._svf.VrSetForm(FORM_FILE, 1);
        } else if (_param._printType.equals(OUTPUT_2)) {
            _form._svf.VrSetForm(FORM_FILE3, 1);
        } else if (_param._printType.equals(OUTPUT_3)) {
            _form._svf.VrSetForm(FORM_FILE2, 1);
        } else if (_param._printType.equals(OUTPUT_4)) {
            _form._svf.VrSetForm(FORM_FILE3, 1);
        } else if (_param._printType.equals(OUTPUT_5)) {
            _form._svf.VrSetForm(FORM_FILE4, 1);
        } else {
            log.debug(">>>出力種別が不正です。: " + _param._printType);
            throw new Exception();
        }
    }

    private void printApplicant(final List applicants) 
        throws SQLException, Exception {

    }

    private void printHeader() {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));

        /* 出力種別 */
        if(_param._printType.equals("2")){
            _form._svf.VrsOut("ITEM", OUTPUT_2_TITLE_NAME);
        } else if(_param._printType.equals("4")){
            _form._svf.VrsOut("ITEM", OUTPUT_4_TITLE_NAME);
        } else {
            _form._svf.VrsOut("ITEM", "");
        }
        
        String subTitle = getSubTitle();
        prtTitle(subTitle);

        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 総ページ */
        _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
    }

    private void prtTitle(String subTitle) {
        if (_param._printType.equals(OUTPUT_2)) {
            _form._svf.VrsOut("TITLE", TITLE_1);
        } else if (_param._printType.equals(OUTPUT_4)) {
            _form._svf.VrsOut("TITLE", TITLE_2);
        }
    }

    private String getSubTitle() {
        String subTitle = "";

        if (_param._testDiv.equals(TESTDIV_ALL)) {
            subTitle = "（" + TITLE_3 + "）";
        } else {
            subTitle = "（" + _param._testDivString(_param._testDiv) + "）";
        }

        return subTitle;
    }

    private void printApplicant1(EntexamApplicantbaseDat applicant) {
        /* 受験番号 */
        _form._svf.VrsOut("EXAMNO", applicant._examno);
        /* 氏名 */
        _form.printName1(applicant._name);
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_M(_param._dateYear + "/" + _param._dateMonth + "/1")
                + "   日");
        /* 署名 */
        _form._svf.VrsOut("SCHOOLNAME_BMP", _param._imgFIleName);
    }

    private void printApplicant2(int i, int no, EntexamApplicantbaseDat applicant) {
        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, applicant._examno);
        /* 志願者氏名 */
        _form._svf.VrsOutn("NAME1", i, applicant._name);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._sex));
        /* 郵便番号 */
        _form._svf.VrsOutn("ZIPCD", i, applicant._entexamApplicantaddrDat._zipCd);
        /* 住所 */
        _form.printAddr(i, applicant);
        /* 電話番号 */
        _form._svf.VrsOutn("FINSCHOOL_TELNO1", i, applicant._entexamApplicantaddrDat._telNo);
    }

    private void printApplicant3(EntexamApplicantbaseDat applicant) {
        /* 受験番号 */
        _form._svf.VrsOut("EXAMNO", applicant._examno);
        /* 氏名 */
        _form.printName2(applicant._name);

        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_M(_param._dateYear + "/" + _param._dateMonth + "/1")
                + "   日");
        /* 署名 */
        _form._svf.VrsOut("SCHOOLNAME_BMP", _param._imgFIleName);
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
        private final String _printType;
        private final String _printRange;
        private final String _examnoFrom;
        private final String _examnoTo;
        private final String _testDiv;
        private final String _docBase;
        private final String _dateYear;
        private final String _dateMonth;

        private Map _sexMap;
        private Map _applicantDivMap;
        private Map _testDivMap;
        private String _imgFIleName;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String printType,
                final String printRange,
                final String examnoFrom,
                final String examnoTo,
                final String testDiv,
                final String docBase,
                final String dateYear,
                final String dateMonth
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _printType = printType;
            _printRange = printRange;
            _examnoFrom = examnoFrom;
            _examnoTo = examnoTo;
            _testDiv = testDiv;
            _docBase = docBase;
            _dateYear = dateYear;
            _dateMonth = dateMonth;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String _testDivString(String testDiv) {
            return (String) _testDivMap.get(testDiv);
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _sexMap = getNameMst(NAME_MST_SEX);
            _testDivMap = getNameMst(NAME_MST_TESTDIV);
            _imgFIleName = getImage(DOC_NAME);

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

        public String getImage(String fileName) throws Exception {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            String folder = null;
            String extension = null;

            returnval = getinfo.Control(db2);
            folder = returnval.val4;            //格納フォルダ
            extension = returnval.val5;         //拡張子

            String image_pass = _param._docBase + "/" + folder + "/";   //イメージパス
            String imgFIleName = image_pass + fileName + "." + extension;
            log.debug(">>>ドキュメントフルパス=" + imgFIleName);

            File f1 = new File(imgFIleName);

            if (!f1.exists()) {
                log.debug(">>>イメージファイルがありません。：" + imgFIleName);
                throw new Exception();
            }

            return imgFIleName;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String printType = request.getParameter("PRINT_TYPE");
        final String printRange = request.getParameter("PRINT_RANGE");
        final String examnoFrom = request.getParameter("EXAMNO_FROM") != null ?
                                    request.getParameter("EXAMNO_FROM") : "";
        final String examnoTo = request.getParameter("EXAMNO_TO") != null ?
                                    request.getParameter("EXAMNO_TO") : "";
        final String testDiv = request.getParameter("TESTDIV");
        final String docBase = request.getParameter("DOCUMENTROOT");
        final String dateYear = request.getParameter("DATE_YEAR");
        final String dateMonth = request.getParameter("DATE_MONTH");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                printType,
                printRange,
                examnoFrom,
                examnoTo,
                testDiv,
                docBase,
                dateYear,
                dateMonth
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

        public void printName1(String name) {
            if (name != null) {
                final String label;
                if (name.length() <= NAME_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printName2(String name) {
            if (name != null) {
                final String label;
                if (name.length() <= NAME_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printAddr(int i, EntexamApplicantbaseDat applicant) {
          String name = (applicant._entexamApplicantaddrDat._address1 != null ?
                          applicant._entexamApplicantaddrDat._address1 : "") 
                      + (applicant._entexamApplicantaddrDat._address2 != null ?
                          applicant._entexamApplicantaddrDat._address2 : "");

            if (name != "") {
                final String label;
                if (name.length() <= ADDRESS_LENG) {
                    label = "ADDRESS1";
                } else {
                    label = "ADDRESS2_1";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }
    }

    // ======================================================================
    /**
     * 志願者受付データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _examno;         // 受験番号

        private final String _name;
        private final String _sex;

        private EntexamApplicantaddrDat _entexamApplicantaddrDat;   // 志願者住所データ

        EntexamApplicantbaseDat(
                final String examno,
                final String name,
                final String sex
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
        }

        public void load(DB2UDB db2, String examno) throws SQLException, Exception {
            _entexamApplicantaddrDat = createEntexamApplicantaddrDat(db2, examno);
        }
    }

    private List createEntexamApplicantbaseDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamApplicantbaseDats());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("examno");
                final String name = rs.getString("name");
                final String sex = rs.getString("sex");

                final EntexamApplicantbaseDat entexamReceptDat = new EntexamApplicantbaseDat(
                        examno,
                        name,
                        sex
                );

                entexamReceptDat.load(db2, entexamReceptDat._examno);
                rtn.add(entexamReceptDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>EntexamReceptDat に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlEntexamApplicantbaseDats() {
        StringBuffer stb = new StringBuffer();

        if ((!_param._testDiv.equals(TESTDIV_ALL)) &&
                (!_param._testDiv.equals(TESTDIV_R))) {
            stb.append(" WITH TEST_T AS ( ");
            stb.append(" SELECT ");
            stb.append("    '1' AS FLG, ");
            stb.append("    T1.EXAMNO AS EXAMNO ");
            stb.append(" FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("    AND T1.APPLICANTDIV = '1' ");
            stb.append("    AND T1.TESTDIV1 is not null ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("    '2' AS FLG, ");
            stb.append("    T1.EXAMNO AS EXAMNO ");
            stb.append(" FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("    AND T1.APPLICANTDIV = '1' ");
            stb.append("    AND T1.TESTDIV2 is not null ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("    '3' AS FLG, ");
            stb.append("    T1.EXAMNO AS EXAMNO ");
            stb.append(" FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("    AND T1.APPLICANTDIV = '1' ");
            stb.append("    AND T1.TESTDIV3 is not null ");
            stb.append(") ");
        }

        stb.append(" SELECT ");
        stb.append("    T1.EXAMNO AS EXAMNO, ");
        if (_param._printType.equals(OUTPUT_2)) {
            stb.append("    case when T2.EXAMNO is not null then '* ' || T1.NAME ");
            stb.append("                                    else '  ' || T1.NAME end AS NAME, ");
        } else {
            stb.append("    T1.NAME AS NAME, ");
        }
        stb.append("    T1.SEX AS SEX ");
        stb.append(" FROM ");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT T1 ");
        if (_param._printType.equals(OUTPUT_2)) {
            stb.append("  LEFT JOIN ( ");
            stb.append("    SELECT ");
            stb.append("        EXAMNO ");
            stb.append("    FROM ");
            stb.append("        ENTEXAM_RECEPT_DAT ");
            stb.append("    WHERE ");
            stb.append("        ENTEXAMYEAR = '" + _param._year + "' ");
            if (!_param._testDiv.equals(TESTDIV_ALL)) {
                stb.append("    AND TESTDIV = '" + _param._testDiv + "' ");
            }
            stb.append("        AND JUDGEDIV = '1' ");
            stb.append("        AND HONORDIV = '1' ");
            stb.append("    GROUP BY ");
            stb.append("        EXAMNO ");
            stb.append(" ) T2 ON T1.EXAMNO = T2.EXAMNO ");
        } 
        stb.append(" WHERE ");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "' ");

        if (_param._testDiv.equals(TESTDIV_ALL)) {
            stb.append("    and (T1.APPLICANTDIV = '1' or T1.APPLICANTDIV = '2')");
        } else if (_param._testDiv.equals(TESTDIV_R)) {
            stb.append("    and T1.APPLICANTDIV = '2'");
            stb.append("    and T1.TESTDIV5 is not null");
        } else {
            stb.append("    and T1.APPLICANTDIV = '1'");
            stb.append("    AND T1.TESTDIV" + _param._testDiv + " is not null ");
            stb.append("    AND T1.EXAMNO NOT IN (SELECT DISTINCT ");
            stb.append("                              T2.EXAMNO ");
            stb.append("                          FROM ");
            stb.append("                              TEST_T T2 ");
            stb.append("                          WHERE ");
            stb.append("                              T2.FLG < '" + _param._testDiv + "' ");
            stb.append("                         ) ");
        }

        if (_param._printRange.equals(OUTPUT2_2)) {
            stb.append("    and T1.EXAMNO BETWEEN '" + _param._examnoFrom + "' AND '" + _param._examnoTo + "'");
        }
        stb.append(" order by T1.EXAMNO");
        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者住所データ。
     */
    private class EntexamApplicantaddrDat {
        private final String _zipCd;        // 郵便番号
        private final String _address1;     // 住所1
        private final String _address2;     // 住所2（方書）
        private final String _telNo;        // 電話番号

        EntexamApplicantaddrDat() {
            _zipCd = "";
            _address1 = "";
            _address2 = "";
            _telNo = "";
        }

        EntexamApplicantaddrDat(
                final String zipCd,
                final String address1,
                final String address2,
                final String telNo
        ) {
            _zipCd = zipCd;
            _address1 = address1;
            _address2 = address2;
            _telNo = telNo;
        }
    }

    private EntexamApplicantaddrDat createEntexamApplicantaddrDat(final DB2UDB db2, String pExamNo)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantaddrDat(pExamNo));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String zipCd = rs.getString("zipCd");
            final String address1 = rs.getString("address1");
            final String address2 = rs.getString("address2");
            final String telNo = rs.getString("telNo");

            final EntexamApplicantaddrDat entexamApplicantaddrDat = new EntexamApplicantaddrDat(
                    zipCd,
                    address1,
                    address2,
                    telNo
            );

            return entexamApplicantaddrDat;
        }

        return new EntexamApplicantaddrDat();

    }

    private String sqlEntexamApplicantaddrDat(String pExamNo) {
        return " select"
                + "    ZIPCD as zipCd,"
                + "    ADDRESS1 as address1,"
                + "    ADDRESS2 as address2,"
                + "    GTELNO as telNo"
                + " from"
                + "    ENTEXAM_APPLICANTADDR_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + pExamNo + "'"
                ;
    }
} // KNJL327J

// eof
