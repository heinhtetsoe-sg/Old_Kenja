// kanji=漢字
/*
 * $Id: 891793e423b19242807fbb370ef5d3e3c262af78 $
 *
 * 作成日: 2007/12/10 17:34:00 - JST
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
 *  棄権者名簿
 * @author nakada
 * @version $Id: 891793e423b19242807fbb370ef5d3e3c262af78 $
 */
public class KNJL373J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL373J.class);

    private static final String FORM_FILE = "KNJL373J.frm";

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";

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

    /*
     * 文字数による出力項目切り分け基準
     */
    /** 氏名最大桁：10 */
    private static final int NAME_LENG = 10;
    /** 住所最大桁：25 */
    private static final int ADDRESS_LENG = 25;
    /** 塾名最大桁：10 */
    private static final int SCHOOL_LENG = 10;
    /** 備考最大桁：12 */
    private static final int REMARK_LENG = 12;

    /*
     * 塾名印字指示
     */
    /** 塾名印字 */
    private static final String PRINT_SCHOOLNAME_ON = "2";

    /*
     * 受験者区分
     */
    /** 欠席 */
    private static final String EXAMINEE_DIV_ABSENCE = "2";

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    private int _page = 0;
    private int _totalPage = 0;

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

            final List entexamDesireDats = createEntexamDesireDats(db2);

            if (entexamDesireDats != null) {
                _totalPage = getTotalPage(entexamDesireDats);
            }

            printMain(entexamDesireDats);

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

            if (i >= DETAILS_MAX) {
                printHeader();

                if (_page == (_totalPage)) {
                    printFooter(manNum, womanNum);
                }

                _form._svf.VrEndPage();
                _hasData = true;
                i = 0;
            }
        }

        if (i > 0) {
            printHeader();
            printFooter(manNum, womanNum);

            _form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private int getTotalPage(List applicants) {
        int totalPage = 0;

        int cnt = applicants.size();

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
        /* № */
        _form._svf.VrsOutn("NO", i, String.valueOf(no));
        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, applicant._examNo);
        /* 志願者氏名 */
        _form.printName(applicant, i);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._entexamApplicantbaseDat._sex));
        /* 住所 */
        _form.printAddr(applicant, i);
        /* 塾名 */
        _form.printPriSchoolName(applicant, i);
        /* 備考 */
        _form.printRemark(applicant, i);
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
        private final String _output;

        private Map _sexMap;
        private Map _examDivMap;
        private String _examDivName;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String output
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _output = output;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String _examTypeString(String examDiv) {
            return (String) _examDivMap.get(examDiv);
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);

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
        final String output = request.getParameter("OUTPUT");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                output
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

        public void printRemark(EntexamDesireDat applicant, int i) {
            String name = null;

            if (applicant._entexamApplicantbaseDat._remark1 != null) {
                name = applicant._entexamApplicantbaseDat._remark1;
            }

            if (name != null) {
                final String label;
                if (name.length() <= REMARK_LENG) {
                    label = "REMARK1";
                } else {
                    label = "REMARK2_1";
                }

                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printName(EntexamDesireDat applicant, int i) {
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

        public void printPriSchoolName(EntexamDesireDat applicant, int i) {
            if (_param._output.equals(PRINT_SCHOOLNAME_ON)) {
                String name = applicant._entexamApplicantbaseDat._priSchoolMst._priSchoolName;

                if (name != null) {
                    final String label;
                    if (name.length() <= SCHOOL_LENG) {
                        label = "PRISCHOOL_NAME1";
                    } else {
                        label = "PRISCHOOL_NAME2_1";
                    }

                    _form._svf.VrsOutn(label, i, name);
                }
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
    }

    // ======================================================================
    /**
     * 志願者データ。
     */
    private class EntexamDesireDat {
        private final String _applicantdiv;
        private final String _examNo;

        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ
        private EntexamApplicantaddrDat _entexamApplicantaddrDat;   // 志願者住所データ

        EntexamDesireDat(
                final String applicantdiv,
                final String examNo
        ) {
            _applicantdiv = applicantdiv;
            _examNo = examNo;
        }

        public void load(DB2UDB db2, String examno) throws SQLException, Exception {
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, examno, _applicantdiv);
            _entexamApplicantaddrDat = createEntexamApplicantaddrDat(db2, examno);
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

            while (rs.next()) {
                final String applicantdiv = rs.getString("applicantdiv");
                final String examNo = rs.getString("examNo");

                final EntexamDesireDat entexamDesireDat = new EntexamDesireDat(
                        applicantdiv,
                        examNo
                );

                entexamDesireDat.load(db2, entexamDesireDat._examNo);
                rtn.add(entexamDesireDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>ENTEXAM_DESIRE_DAT に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlEntexamDesireDats() {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("   T1.applicantdiv,");
        stb.append("   T1.EXAMNO as examNo");
        stb.append(" from");
        stb.append("      (select");
        stb.append("          ENTEXAMYEAR,");
        stb.append("          EXAMNO,");
        stb.append("          COUNT(*) as CNT,");
        stb.append("          MAX(APPLICANTDIV) as applicantdiv");
        stb.append("       from");
        stb.append("          ENTEXAM_DESIRE_DAT ");
        stb.append("       where ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("       group by ENTEXAMYEAR, EXAMNO) T1,");
        stb.append("      (select");
        stb.append("          ENTEXAMYEAR,");
        stb.append("          EXAMNO,");
        stb.append("          COUNT(*) as CNT,");
        stb.append("          MAX(APPLICANTDIV) as applicantdiv");
        stb.append("       from");
        stb.append("          ENTEXAM_DESIRE_DAT");
        stb.append("       where");
        stb.append("          ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("          and EXAMINEE_DIV = '" + EXAMINEE_DIV_ABSENCE + "'");
        stb.append("       group by ENTEXAMYEAR, EXAMNO) T2");
        stb.append(" where");
        stb.append("    T1.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T1.EXAMNO = T2.EXAMNO");
        stb.append("    and T1.CNT = T2.CNT");
        stb.append(" order by T1.EXAMNO");

        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _name;
        private final String _sex;
        private final String _priSchoolCd;
        private final String _remark1;
        private final String _remark2;

        private PriSchoolMst _priSchoolMst;

        EntexamApplicantbaseDat() {
            _name = "";
            _sex = "";
            _priSchoolCd = "";
            _remark1 = "";
            _remark2 = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String sex,
                final String priSchoolCd,
                final String remark1,
                final String remark2
        ) {
            _name = name;
            _sex = sex;
            _priSchoolCd = priSchoolCd;
            _remark1 = remark1;
            _remark2 = remark2;
        }

        public void load(DB2UDB db2, String pPriSchoolCd) throws SQLException, Exception {
            _priSchoolMst = createPriSchoolMst(db2, pPriSchoolCd);
        }
    }

    private EntexamApplicantbaseDat createEntexamApplicantbaseDat(
            final DB2UDB db2,
            String pExamNo,
            String pApplicantdiv
    )
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDat(pExamNo, pApplicantdiv));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String name = rs.getString("name");
            final String sex = rs.getString("sex");
            final String priSchoolCd = rs.getString("priSchoolCd");
            final String remark1 = rs.getString("remark1");
            final String remark2 = rs.getString("remark2");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    name,
                    sex,
                    priSchoolCd,
                    remark1,
                    remark2
            );
            entexamApplicantbaseDat.load(db2, entexamApplicantbaseDat._priSchoolCd);
            return entexamApplicantbaseDat;
        }

        return new EntexamApplicantbaseDat();
    }

    private String sqlEntexamApplicantbaseDat(String pExamNo, String pApplicantdiv) {
        return " select"
                + "    NAME as name,"
                + "    SEX as sex,"
                + "    PRISCHOOLCD as priSchoolCd,"
                + "    REMARK1 as remark1,"
                + "    REMARK2 as remark2"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    APPLICANTDIV = '" + pApplicantdiv + "' and"
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
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

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

    // ======================================================================
    /**
     * 塾マスタ。
     */
    private class PriSchoolMst {
        private final String _priSchoolName;

        PriSchoolMst() {
            _priSchoolName = "";
        }

        PriSchoolMst(
                final String priSchoolName
        ) {
            _priSchoolName = priSchoolName;
        }
    }

    private PriSchoolMst createPriSchoolMst(final DB2UDB db2, String pPriSchoolCd)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlPriSchoolMst(pPriSchoolCd));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String priSchoolName = rs.getString("priSchoolName");

            final PriSchoolMst priSchoolMst = new PriSchoolMst(priSchoolName);

            return priSchoolMst;
        }

        return new PriSchoolMst();

    }

    private String sqlPriSchoolMst(String pPriSchoolCd) {
        return " select"
                + "    PRISCHOOL_NAME as priSchoolName"
                + " from"
                + "    PRISCHOOL_MST"
                + " where"
                + "    PRISCHOOLCD = '" + pPriSchoolCd + "'"
                ;
    }
} // KNJL373J

// eof
