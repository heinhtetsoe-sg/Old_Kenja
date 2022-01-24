// kanji=漢字
/*
 * $Id: 127926967544e13e65a7a18caba32f1a1a176704 $
 *
 * 作成日: 2007/12/05 10:58:00 - JST
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
 *  受験出欠表
 * @author nakada
 * @version $Id: 127926967544e13e65a7a18caba32f1a1a176704 $
 */
public class KNJL353J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL353J.class);

    private static final String FORM_FILE = "KNJL353J.frm";

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";
    private static final String NAME_MST_TESTDIV = "L004";
    private static final String EXAM_TYPE = "L005";

    /*
     * 入試制度
     */
    /** 入試制度：中学 */
    private static final String APPLICANTDIV_JH_SCHOOL = "1";

    /*
     * 入試区分
     */
    /** 入試区分：全て */
    private static final String TESTDIV_ALL = "0";

    /*
     * 性別
     */
    /** 性別：男 */
    private static final String SEX_MAN = "1";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 40;

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

        String newTestDiv = null;
        String newExamType = null;
        String oldTestDiv = null;
        String oldExamType = null;
        int manNum = 0;
        int womanNum = 0;

        if (!applicants.isEmpty()) {
            final EntexamReceptDat EntexamReceptDatKey = (EntexamReceptDat) applicants.get(0);
            newTestDiv = EntexamReceptDatKey._testDiv;
            oldTestDiv = EntexamReceptDatKey._testDiv;
            newExamType = EntexamReceptDatKey._examType;
            oldExamType = EntexamReceptDatKey._examType;
        }

        _totalPage = 0;
        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamReceptDat applicant = (EntexamReceptDat) it.next();
            
            i++;
            no++;

            newTestDiv = applicant._testDiv;
            newExamType = applicant._examType;

            if ((_totalPage == 0) ||
                    (!newTestDiv.equals(oldTestDiv)) ||
                            (!newExamType.equals(oldExamType))
            ) {
                _totalPage = getTotalPage(applicants, oldTestDiv, oldExamType);
            }

            if (newTestDiv.equals(oldTestDiv) && newExamType.equals(oldExamType)) {
                printApplicant(i, no, applicant);
                
                if (applicant._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                    manNum++;
                } else {
                    womanNum++;
                }
            } else {

                prtHeader(oldTestDiv, manNum, womanNum);

                i = 1;
                no = 1;
                printApplicant(i, no, applicant);

                oldTestDiv = newTestDiv;
                oldExamType = newExamType;

                _page = 0;
                _totalPage = 0;

                manNum = 0;
                womanNum = 0;
                if (applicant._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                    manNum = 1;
                } else {
                    womanNum = 1;
                }
            }

            if (i >= DETAILS_MAX) {
                prtHeader2(oldTestDiv);

                i = 0;
            }
        }

        if (i > 0) {
            prtHeader(oldTestDiv, manNum, womanNum);
        }

    }

    private void prtHeader2(String oldTestDiv) {
        printHeader(oldTestDiv);

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void prtHeader(String oldTestDiv, int manNum, int womanNum) {
        printHeader(oldTestDiv);
        printFooter(manNum, womanNum);
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private int getTotalPage(List applicants, String testDiv, String examType) {
        int cnt = 0;
        int totalPage = 0;

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamReceptDat applicant = (EntexamReceptDat) it.next();

            if (testDiv.equals(applicant._testDiv) && examType.equals(applicant._examType)) {
                cnt++;
            }
        }

        totalPage = cnt / DETAILS_MAX;
        if (cnt % DETAILS_MAX != 0) {
            totalPage++;
        }

        return totalPage;
    }

    private void printHeader(String testDiv) {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 総ページ */
        _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
        /* 入試区分 */
        _form._svf.VrsOut("TESTDIV", _param._testDivString(testDiv));
    }

    private void printFooter(int manNum, int womanNum) {
        /* 性別人数 */
        _form._svf.VrsOut("NOTE", "男 " + Integer.toString(manNum)
                + "名、女 " + Integer.toString(womanNum)
                + "名、合計 " + (Integer.toString(manNum + womanNum)) + "名"
        );
    }

    private void printApplicant(int i, int no, EntexamReceptDat applicant) {
        /* № */
        _form._svf.VrsOutn("NO", i, String.valueOf(no));
        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, applicant._examno);
        /* 志願者氏名 */
        _form._svf.VrsOutn("NAME", i, applicant._entexamApplicantbaseDat._name);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._entexamApplicantbaseDat._sex));
        /* 科 */
        _form._svf.VrsOutn("EXAM_TYPE", i, _param._examTypeString(applicant._examType));
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
        private final String _testDiv;

        private Map _sexMap;
        private Map _testDivMap;
        private Map _examTypeMap;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String testDiv
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _testDiv = testDiv;
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
            _examTypeMap = getNameMst(EXAM_TYPE);

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
//        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGINDATE"));
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String testDiv = request.getParameter("TESTDIV");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                testDiv
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
     * 志願者受付データ。
     */
    private class EntexamReceptDat {
        private final String _applicantDiv;     // 入試制度
        private final String _testDiv;          // 入試区分
        private final String _examno;           // 受験番号
        private final String _examType;         // 受験型

        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ

        EntexamReceptDat(
                final String applicantDiv,
                final String testDiv,
                final String examno,
                final String examType
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examno = examno;
            _examType = examType;
        }

        public void load(DB2UDB db2, String examno) throws SQLException, Exception {
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, _examno, _applicantDiv);
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
                final String applicantDiv = rs.getString("applicantDiv");
                final String testDiv = rs.getString("testDiv");
                final String examno = rs.getString("examno");
                final String examType = rs.getString("examType");

                final EntexamReceptDat entexamReceptDat = new EntexamReceptDat(
                        applicantDiv,
                        testDiv,
                        examno,
                        examType
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

        stb.append(" select");
        stb.append("    APPLICANTDIV as applicantDiv,");
        stb.append("    '" + _param._testDiv + "' as testDiv,");
        stb.append("    EXAMNO as examno,");
        stb.append("    TESTDIV" + _param._testDiv + " as examType");
        stb.append(" from");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT");
        stb.append(" where" );
        stb.append("    ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and TESTDIV" + _param._testDiv + " is not null");
        stb.append("    and value(JUDGEMENT,'0') <> '1'");
        stb.append(" order by 2,4, EXAMNO");
        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _name;
        private final String _sex;

        EntexamApplicantbaseDat() {
            _name = "";
            _sex = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String sex
        ) {
            _name = name;
            _sex = sex;
        }
    }

    private EntexamApplicantbaseDat createEntexamApplicantbaseDat(
            final DB2UDB db2,
            String pExamNo,
            String pApplicantDiv
    )
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDat(pExamNo, pApplicantDiv));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String name = rs.getString("name");
            final String sex = rs.getString("sex");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    name,
                    sex
            );

            return entexamApplicantbaseDat;
        }

        return new EntexamApplicantbaseDat();

    }

    private String sqlEntexamApplicantbaseDat(String pExamNo, String pApplicantDiv) {
        return " select"
                + "    NAME as name,"
                + "    SEX as sex"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    APPLICANTDIV = '" + pApplicantDiv + "' and"
                + "    EXAMNO = '" + pExamNo + "'"
                ;
    }
} // KNJL353J

// eof
