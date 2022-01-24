// kanji=漢字
/*
 * $Id: 3f5d94b6eddd254079e634fbe92cf2cac5a01e13 $
 *
 * 作成日: 2007/11/21 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
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
 * 転学生徒の必要書類送付願
 * @author nakada
 * @version $Id: 3f5d94b6eddd254079e634fbe92cf2cac5a01e13 $
 */
public class KNJWA181 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA181.class);

    private static final String FORM_TENGAKU = "KNJWA181.frm";
    private static final String FORM_SINSOTU = "KNJWA181_2.frm";
    private static final String FORM_HENNYU = "KNJWA181_3.frm";

    private static final String CHECK_SYORUI = "0";
    private static final String CHECK_SINSOTU = "1";
    private static final String CHECK_HENNYU = "2";
    /*
     * 発行番号印刷不可
     */
    /** 発行番号印刷不可 */
    private static final String CERTIF_NO_ON = "0";

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 転学生徒の必要書類送付願印刷 */
    private static final String OUTPUT1_PRINT_ON = "1";

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _page;

    public boolean svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response,
            Vrw32alp svf,
            DB2UDB pDb2
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _svf = svf;
        _form = new Form(response, _svf);
        db2 = pDb2;

        try {

            _param.load(db2);

            log.debug(">>学籍番号=" + _param._schregno);

            final Student student = createStudent(db2);
            printMain(student);

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printMain(final Student student) throws SQLException, Exception {
        printApplicant(student);

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void printApplicant(Student student) throws SQLException, Exception {
        /* 学校宛先・郵便番号 */
        _form._svf.VrsOut("ZIPCD", _param._finHighschoolMst._zipcd);
        /* 学校宛先・住所１ */
        _form.printAddr1();
        /* 学校宛先・住所２ */
        _form.printAddr2();
        /* 学校宛先・学校名 */
        _form._svf.VrsOut("ANOTHER_SCHOOL", _param._finHighschoolMst._name);

        /* 証明書番号 */
        _form._svf.VrsOut("BONDNAME1", _param._certifIssueDat._certifSchoolDat._syosyoName);
        _form.printCertifIndex();

        /* 作成日 */
        _form._svf.VrsOut("DATE1", getJDate(_param._certifIssueDat._issueDate));
        
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME1", _param._certifIssueDat._certifSchoolDat._schoolName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME", _param._certifIssueDat._certifSchoolDat._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME", _param._certifIssueDat._certifSchoolDat._principalName);

        /* 発行日付 */
        _form.printCertifIssueDat();
        /* 証明書番号 */
        _form._svf.VrsOut("BONDNAME2", _param._certifIssueDat2._certifSchoolDat._syosyoName.trim());       

        int int1 = Integer.parseInt(_param._certifIssueDat._certifDetailEachtypeDat._remark3);
        _form._svf.VrsOut("CERTIF_NO2", String.valueOf(int1));

        /* 氏名 */
        _form._svf.VrsOut("NAME", student._name);
        /* 生年月日 */
        _form._svf.VrsOut("BIRTHDAY", getJDate(student._birthday) + "生");
        
        /* 学校宛先・郵便番号 */
        _form._svf.VrsOut("SCHOOLZIPCD", _param._staffSchoolMst._schoolZipCd);
        /* 学校宛先・住所１ */
        _form.printSchAddr1();
        /* 学校宛先・住所２ */
        _form.printSchAddr2();
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME2", _param._staffSchoolMst._schoolName1);
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

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String _schregno;
        private final String _type;         // 証明書種別コード
        private final String _certifYear;   // 転学生徒の必要書類送付願連番の年度
        private final String _certif;       // 転学生徒の必要書類送付願連番
        private final String _output;       // 1:転学生徒の必要書類送付願印刷
        private final String _checkSinsotu; // 1:新卒・既卒生徒の必要書類送付願

        private Map _prefMap;                       // 都道府県
        private School _staffSchoolMst;             // 学校名
        private CertifIssueDat _certifIssueDat;     // 本証書・証明書情報
        private CertifIssueDat _certifIssueDat2;    // 転学許可証・証明書情報
        private FinHighschoolMst _finHighschoolMst; // 送付先高校情報

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String schregno,
                final String type,
                final String certifYear,
                final String certif,
                final String output,
                final String checkSinsotu
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
            _type = type;
            _certifYear = certifYear;
            _certif = certif;
            _output = output;
            _checkSinsotu = checkSinsotu;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _prefMap = getPrefMst();

            _staffSchoolMst = createSchool(db2, _param._year);

            _certifIssueDat = createCertifIssueDat(db2, "HED", _param._certif, "", 0);

            final String certifIndex = "1".equals(_param._output) || "2".equals(_param._output) ? _param._certif : _certifIssueDat._certifDetailEachtypeDat._remark2;
            _certifIssueDat2 = createCertifIssueDat(db2, "", certifIndex,
                    _certifIssueDat._certifDetailEachtypeDat._remark2,
                    Integer.parseInt(_certifIssueDat._certifDetailEachtypeDat._remark3));

            _finHighschoolMst = createFinSchoolMst(db2, _certifIssueDat._certifDetailEachtypeDat._remark1, _certifIssueDat._certifDetailEachtypeDat._remark5);

            return;
        }

        private Map getPrefMst() throws SQLException {
            final String sql = sqlPrefMst();
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

        private String sqlPrefMst() {
            return " select"
            + "    PREF_CD as code,"
            + "    PREF_NAME as name"
            + " from"
            + "    PREF_MST"
            + " order by PREF_CD";
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));
        final String schregno = request.getParameter("SCHREGNO");
        final String type = request.getParameter("TYPE");
        final String certifYear = request.getParameter("CERTIF_YEAR");
        final String certif = request.getParameter("SYORUI");
        String output = null != request.getParameter("CHECK_SYORUI") ? "0" : null;
        output = null != request.getParameter("CHECK_SINSOTU") ? "1" : output;
        output = null != request.getParameter("CHECK_HENNYU") ? "2" : output;
        final String checkSinsotu = request.getParameter("CHECK_SINSOTU");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno,
                type,
                certifYear,
                certif,
                output,
                checkSinsotu
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

        public Form(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
            _svf = svf;

            final String file = CHECK_SYORUI.equals(_param._output) ? FORM_TENGAKU : CHECK_SINSOTU.equals(_param._output) ? FORM_SINSOTU : FORM_HENNYU;
            _svf.VrSetForm(file, 1);
        }

        public void printAddr1() {
            String name = _param._prefMapString(_param._finHighschoolMst._prefCd)
                + (_param._finHighschoolMst._add1 != null ? _param._finHighschoolMst._add1 : "")
                + (_param._finHighschoolMst._add2 != null ? _param._finHighschoolMst._add2 : "");
            _form._svf.VrsOut("ADDRESS1_1", name);
        }

        public void printAddr2() {
            String name = _param._finHighschoolMst._add3 != null ? _param._finHighschoolMst._add3 : "";
            _form._svf.VrsOut("ADDRESS1_2", name);
        }

        public void printSchAddr1() {
            String name = (_param._staffSchoolMst._schoolAddr1 != null ? _param._staffSchoolMst._schoolAddr1 : "")
                + (_param._staffSchoolMst._schoolAddr2 != null ? _param._staffSchoolMst._schoolAddr2 : "");
            _form._svf.VrsOut("SCHOOLADDRESS1", name);
        }

        public void printSchAddr2() {
            String name = _param._staffSchoolMst._schoolAddr3 != null ? _param._staffSchoolMst._schoolAddr3 : "";
            _form._svf.VrsOut("SCHOOLADDRESS2", name);
        }

        void printCertifIssueDat() throws SQLException, Exception {
            _svf.VrsOut("DATE2", KNJWA181.getJDate(_param._certifIssueDat2._issueDate));
        }

        void printCertifIndex() {
            if (_param._certifIssueDat._certifSchoolDat._certifNo.equals(KNJWA181.CERTIF_NO_ON)) {
                _svf.VrsOut("CERTIF_NO1", _param._certifIssueDat._certifNo);
            } else {
                _svf.VrsOut("CERTIF_NO1", "     ");
            }
        }
    }

    // ======================================================================
    class Student {
        private final String _schregNo;         // 学籍番号
        private final String _name;             // 氏名
        private final String _birthday;         // 生年月日

        Student(final String schregNo,
                final String name,
                final String birthday
        ) {
            _schregNo = schregNo;
            _name = name;
            _birthday = birthday;
        }
    }

    private Student createStudent(final DB2UDB db2) throws SQLException, Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudent());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String birthday = rs.getString("birthday");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        birthday
                );
                return studentDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlStudent() {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    BIRTHDAY as birthday"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + _param._schregno + "'";
    }

    // ======================================================================
    /**
     * 学校マスタ。
     */
    private class School {
        private final String _schoolName1; // 学校名1
        private final String _schoolZipCd;          // 学校郵便番号
        private final String _schoolAddr1;          // 学校住所1
        private final String _schoolAddr2;          // 学校住所2
        private final String _schoolAddr3;          // 学校住所3

        School(
            final String schoolName1,
            final String schoolZipCd,
            final String schoolAddr1,
            final String schoolAddr2,
            final String schoolAddr3
        ) {
            _schoolName1 = schoolName1;
            _schoolZipCd = schoolZipCd;
            _schoolAddr1 = schoolAddr1;
            _schoolAddr2 = schoolAddr2;
            _schoolAddr3 = schoolAddr3;
        }

        public School() {
            _schoolName1 = "";
            _schoolZipCd = "";
            _schoolAddr1 = "";
            _schoolAddr2 = "";
            _schoolAddr3 = "";
        }
    }

    private School createSchool(DB2UDB db2, String year) throws SQLException {
        final String sql = sqlSchool(year);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String schoolName1 = rs.getString("schoolName1");
            final String schoolZipCd = rs.getString("schoolZipCd");
            final String schoolAddr1 = rs.getString("schoolAddr1");
            final String schoolAddr2 = rs.getString("schoolAddr2");
            final String schoolAddr3 = rs.getString("schoolAddr3");

            final School school = new School(
                    schoolName1,
                    schoolZipCd,
                    schoolAddr1,
                    schoolAddr2,
                    schoolAddr3
            );
            return school;
        }

        return new School();
    }

    private String sqlSchool(String year) {
        return " select"
                + "    SCHOOLNAME1 as schoolName1,"
                + "    SCHOOLZIPCD as schoolZipCd,"
                + "    SCHOOLADDR1 as schoolAddr1,"
                + "    SCHOOLADDR2 as schoolAddr2,"
                + "    SCHOOLADDR3 as schoolAddr3"
                + " from"
                + "    SCHOOL_MST"
                + " where"
                + "    YEAR = '" + year + "'";
    }

    // ======================================================================
    /**
     * 生徒。出身高校マスタ。
     */
    private class FinHighschoolMst {
        private final String _name;
        private final String _zipcd;        // 郵便番号
        private final String _add1;         // 住所1
        private final String _add2;         // 住所2
        private final String _add3;         // 住所3
        private final String _prefCd;       // jis都道府県

        FinHighschoolMst(
                final String name,
                final String zipcd,
                final String add1,
                final String add2,
                final String add3,
                final String prefCd
        ) {
            _name = name;
            _zipcd = zipcd;
            _add1 = add1;
            _add2 = add2;
            _add3 = add3;
            _prefCd = prefCd;
        }

        public FinHighschoolMst() {
            _name = "";
            _zipcd = "";
            _add1 = "";
            _add2 = "";
            _add3 = "";
            _prefCd = "";
        }
    }

    private FinHighschoolMst createFinSchoolMst(final DB2UDB db2, final String schoolCd, final String schoolDiv)
        throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlFinSchoolMst(schoolCd, schoolDiv));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");
            final String zipcd = rs.getString("zipcd");
            final String add1 = rs.getString("add1");
            final String add2 = rs.getString("add2");
            final String add3 = rs.getString("add3");
            final String prefCd = rs.getString("prefCd");
            
            final FinHighschoolMst finHighschoolMst = new FinHighschoolMst(
                    name,
                    zipcd,
                    add1,
                    add2,
                    add3,
                    prefCd
            );
            return finHighschoolMst;
        }                    
        return new FinHighschoolMst();
    }

    private String sqlFinSchoolMst(final String schoolCd, final String schoolDiv) {
        final String schoolTbl = "H".equals(schoolDiv) ? "FIN_HIGH_SCHOOL_MST" : "FIN_JUNIOR_HIGHSCHOOL_MST";
        return " select"
                + "    NAME as name,"
                + "    ZIPCD as zipcd,"
                + "    ADDR1 as add1,"
                + "    ADDR2 as add2,"
                + "    ADDR3 as add3,"
                + "    PREF_CD as prefCd"
                + " from"
                + "    " + schoolTbl + " "
                + " where"
                + "    SCHOOL_CD = '" + schoolCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 証明書発行データ。
     */
    class CertifIssueDat {
        private final String _schregNo;     // 学籍番号
        private final String _type;         // 証明書種別
        private final String _certifKindCd; // 証明書種類コード
        private final String _issueDate;    // 証明書発行日付
        private final String _certifNo;     // 証明書番号

        private CertifDetailEachtypeDat _certifDetailEachtypeDat;
        private CertifSchoolDat _certifSchoolDat;
        
        CertifIssueDat(
                final String schregNo,
                final String type,
                final String certifKindCd,
                final String issueDate,
                final String certifNo
        ) {
            _schregNo = schregNo;
            _type = type;
            _certifKindCd = certifKindCd;
            _issueDate = issueDate;
            _certifNo = certifNo;
        }

        public void load(DB2UDB db2, String pKeyDiv, String certifIndex) throws SQLException, Exception {
            if (pKeyDiv.equals("HED")) {
                _certifDetailEachtypeDat = createCertifDetailEachtypeDat(db2, certifIndex);
            }

            _certifSchoolDat = createCertifSchoolDat(db2, _certifKindCd);
        }
    }

    private CertifIssueDat createCertifIssueDat(DB2UDB db2, String pKeyDiv, String pCertifIndex, String pType, int pCertifNo)
        throws SQLException, Exception {

        final String sql = sqlCertifIssueDat(pKeyDiv, pCertifIndex, pType, pCertifNo);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            final String schregNo = rs.getString("schregNo");
            final String type = rs.getString("type");
            final String certifKindCd = rs.getString("certifKindCd");
            final String issueDate = rs.getString("issueDate");
            final String certifNo = rs.getString("certifNo");

            final CertifIssueDat certifIssueDat = new CertifIssueDat(
                    schregNo,
                    type,
                    certifKindCd,
                    issueDate,
                    certifNo
            );

            certifIssueDat.load(db2, pKeyDiv, pCertifIndex);

            return certifIssueDat;
        }

        log.debug(">>>CERTIF_ISSUE_DAT に該当するものがありません。");
        throw new Exception();
    }
    
    

    private String sqlCertifIssueDat(String pKeyDiv, String pCertifIndex, String pType, int pCertifNo) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    SCHREGNO as schregNo,");
        stb.append("    TYPE as type,");
        stb.append("    CERTIF_KINDCD as certifKindCd,");
        stb.append("    ISSUEDATE as issueDate,");
        stb.append("    CERTIF_NO as certifNo");
        stb.append(" from");
        stb.append("    CERTIF_ISSUE_DAT");
        stb.append(" where");

        if (pKeyDiv.equals("HED") || "1".equals(_param._output) || "2".equals(_param._output)) {
            stb.append("    YEAR = '" + _param._year + "'");
            stb.append("    and CERTIF_INDEX = '" + pCertifIndex + "'");
        } else {
            stb.append("    YEAR = '" + _param._certifYear + "'");
            stb.append("    and TYPE = '" + pType + "'");
            if (null != _param._output) {
                stb.append("    and CERTIF_NO = " + pCertifNo);
            }
        }

        return stb.toString();
    }

    // ======================================================================
    /**
     * 証明書種別別詳細データ。
     */
    private class CertifDetailEachtypeDat {
        private final String _schregNo;     // 学籍番号
        private final String _type;         // 証明書種別
        private final String _remark1;      // 備考1
        private final String _remark2;      // 備考2
        private final String _remark3;      // 備考3
        private final String _remark4;      // 備考4
        private final String _remark5;      // 備考5
        private final String _remark6;      // 備考6
        private final String _remark7;      // 備考7

        CertifDetailEachtypeDat(
                final String schregNo,
                final String type,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7
        ) {
            _schregNo = schregNo;
            _type = type;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
        }
    }

    private CertifDetailEachtypeDat createCertifDetailEachtypeDat(DB2UDB db2, String pCertifIndex)
        throws SQLException, Exception {
        final String sql = sqlCertifDetailEachtypeDat(pCertifIndex);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String schregNo = rs.getString("schregNo");
            final String type = rs.getString("type");
            final String remark1 = rs.getString("remark1");
            final String remark2 = rs.getString("remark2");
            final String remark3 = rs.getString("remark3");
            final String remark4 = rs.getString("remark4");
            final String remark5 = rs.getString("remark5");
            final String remark6 = rs.getString("remark6");
            final String remark7 = rs.getString("remark7");

            final CertifDetailEachtypeDat certifDetailEachtypeDat = new CertifDetailEachtypeDat(
                    schregNo,
                    type,
                    remark1,
                    remark2,
                    remark3,
                    remark4,
                    remark5,
                    remark6,
                    remark7
            );
            return certifDetailEachtypeDat;
        }

        log.debug(">>>CERTIF_DETAIL_EACHTYPE_DAT に該当するものがありません。");
        throw new Exception();
    }

    private String sqlCertifDetailEachtypeDat(String certifIndex) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    TYPE as type,"
                + "    REMARK1 as remark1,"
                + "    REMARK2 as remark2,"
                + "    REMARK3 as remark3,"
                + "    REMARK4 as remark4,"
                + "    REMARK5 as remark5,"
                + "    REMARK6 as remark6,"
                + "    REMARK7 as remark7"
                + " from"
                + "    CERTIF_DETAIL_EACHTYPE_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "' and"
                + "    CERTIF_INDEX = '" + certifIndex + "'"
                ;
    }

    // ======================================================================
    /**
     * 証明書学校データ。
     */
    private class CertifSchoolDat {
        private final String _certifNo;         // 発行番号印刷不可
        private final String _syosyoName;       // 証書名
        private final String _schoolName;       // 学校名
        private final String _jobName;          // 役職名
        private final String _principalName;    // 校長名

        CertifSchoolDat(
                final String certifNo,
                final String syosyoName,
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _certifNo = certifNo;
            _syosyoName = syosyoName;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
        }
    }

    private CertifSchoolDat createCertifSchoolDat(DB2UDB db2, String pCertifKindCd) throws SQLException, Exception {
        final String sql = sqlCertifSchoolDat(pCertifKindCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String certifNo = rs.getString("certifNo");
            final String syosyoName = rs.getString("syosyoName");
            final String schoolName = rs.getString("schoolName");
            final String jobName = rs.getString("jobName");
            final String principalName = rs.getString("principalName");

            final CertifSchoolDat certifSchoolDat = new CertifSchoolDat(
                    certifNo,
                    syosyoName,
                    schoolName,
                    jobName,
                    principalName
            );
            return certifSchoolDat;
        }

        log.debug(">>>CERTIF_SCHOOL_DAT に該当するものがありません。");
        throw new Exception();
    }

    private String sqlCertifSchoolDat(String certifKindCd) {
        return " select"
                + "    CERTIF_NO as certifNo,"
                + "    SYOSYO_NAME as syosyoName,"
                + "    SCHOOL_NAME as schoolName,"
                + "    JOB_NAME as jobName,"
                + "    PRINCIPAL_NAME as principalName"
                + " from"
                + "    CERTIF_SCHOOL_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "' and"
                + "    CERTIF_KINDCD = '" + certifKindCd + "'"
                ;
    }
} // KNJWA181

// eof
