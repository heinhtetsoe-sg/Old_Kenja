// kanji=漢字
/*
 * $Id: 6b9a7c28ef0399f80898c914bf2dcb9c4f5b74f1 $
 *
 * 作成日: 2007/12/27 16:49:00 - JST
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
 *  入学証書
 * @author nakada
 * @version $Id: 6b9a7c28ef0399f80898c914bf2dcb9c4f5b74f1 $
 */
public class KNJL343J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL343J.class);

    private static final String FORM_FILE = "KNJL343J.frm";

    /*
     * 出力詳細
     */
    /** 受験番号指定 */
    private static final String PRINT_RANGE_EXAMNO = "2";

    /*
     * 合否区分
     */
    /** 合格 */
    private static final String JUDGEDIV_PASS = "1";

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
     * 文字数による出力項目切り分け基準
     */
    /** 名前 */
    private static final int NAME_LENG = 10;

    /*
     * イメージファイル名
     */
    /** イメージファイル名 */
    private static final String DOC_NAME = "SCHOOLNAME_STAMP";  // TODO: 名称確認

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

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

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamApplicantbaseDat applicant = (EntexamApplicantbaseDat) it.next();

            printApplicant(applicant);
            prtHeader();

        }
    }

    private void prtHeader() {
        printHeader();

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void printHeader() {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* 署名 */
        _form._svf.VrsOut("SCHOOLNAME_BMP", _param._imgFIleName);
    }

    private void printApplicant(EntexamApplicantbaseDat applicant) {
        /* 証書番号 */
        _form._svf.VrsOut("BOND_NO", applicant._successNoticeno);
        /* 志願者氏名 */
        _form.printName(applicant);
        /* 生年月日 */
        _form._svf.VrsOut("BIRTHDAY", getJDate(applicant._bitthday));
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
        private final String _date;
        private final String _printRange;
        private final String _examno;
        private final String _docBase;

        private Map _sexMap;
        private Map _testDivMap;
        private Map _examTypeMap;
        private String _imgFIleName;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String date,
                final String printRange,
                final String examno,
                final String docBase
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _date = date;
            _printRange = printRange;
            _examno = examno;
            _docBase = docBase;
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

        public void load(DB2UDB db2) throws SQLException, Exception {
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
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
        final String printRange = request.getParameter("PRINT_RANGE");
        final String examno = request.getParameter("EXAMNO") != null ?
                                    request.getParameter("EXAMNO") : "";
        final String docBase = request.getParameter("DOCUMENTROOT");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                date,
                printRange,
                examno,
                docBase
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

        public void printName(EntexamApplicantbaseDat applicant) {
            String name = applicant._name;

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
        private final String _judgement;        // 合否判定
        private final String _procedurediv;     // 合否判定
        private final String _entdiv;           // 入学区分
        private final String _successNoticeno;  // 合格通知№
        private final String _bitthday;
        private final String _name;

        EntexamApplicantbaseDat(
                final String applicantdiv,
                final String examno,
                final String judgement,
                final String procedurediv,
                final String entdiv,
                final String successNoticeno,
                final String bitthday,
                final String name
        ) {
            _applicantdiv = applicantdiv;
            _examno = examno;
            _judgement = judgement;
            _procedurediv = procedurediv;
            _entdiv = entdiv;
            _successNoticeno = successNoticeno;
            _bitthday = bitthday;
            _name = name;
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
            final String entdiv = rs.getString("entdiv");
            final String successNoticeno = rs.getString("successNoticeno");
            final String bitthday = rs.getString("bitthday");
            final String name = rs.getString("name");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    applicantdiv,
                    examno,
                    judgement,
                    procedurediv,
                    entdiv,
                    successNoticeno,
                    bitthday,
                    name
            );

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
        StringBuffer stb = new StringBuffer();
        stb.append(" select");
        stb.append("    APPLICANTDIV as applicantdiv,");
        stb.append("    EXAMNO as examno,");
        stb.append("    JUDGEMENT as judgement,");
        stb.append("    PROCEDUREDIV as procedurediv,");
        stb.append("    value(ENTDIV, '') as entdiv,");
        stb.append("    value(SUCCESS_NOTICENO, '') as successNoticeno,");
        stb.append("    BIRTHDAY as bitthday,");
        stb.append("    NAME as name");
        stb.append(" from");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT");
        stb.append(" where");
        stb.append("    ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and ENTDIV <> '" + ENTDIV_REFUSAL + "'");
        stb.append("    and JUDGEMENT = '" + JUDGEDIV_PASS + "'");
        stb.append("    and PROCEDUREDIV = '" + PROCEDURE_FINISH + "'");

        if (_param._printRange.equals(PRINT_RANGE_EXAMNO)) {
            stb.append("    and EXAMNO = '" + _param._examno + "'");
        }

        stb.append("    order by EXAMNO");
         return stb.toString();
     }
} // KNJL343J

// eof
