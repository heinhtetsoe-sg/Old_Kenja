// kanji=漢字
/*
 * $Id: 97a85bb43569bca3216d3667172aff0fedaf93e5 $
 *
 * 作成日: 2007/11/16 16:42:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

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
 * 督促状２
 * 
 * @author nakada
 * @version $Id: 97a85bb43569bca3216d3667172aff0fedaf93e5 $
 */
public class KNJWP142 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP142.class);

    private static final String FORM_FILE = "KNJWP142.frm";

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 督促状２印刷 */
    private static final String OUTPUT2_PRINT_ON = "1";

    /** 様 */
    private static final String PRINT_STATE = "　様";

    /*
     * 送り先
     */
    /** 生徒 */
    private static final String DESTINATION_STUDENT = "1";    
    /** 保護者 */
    private static final String DESTINATION_PROTECT = "2";    
    /** 負担者 */
    private static final String DESTINATION_GUARANT = "3";    

    /*
     * 学生区分
     */
    /** 2:サポート生 */
    private static final String STUDENT_DIV_SUPPORT = "02";    

    /*
     * 本校事務室
     */
    /** 本校事務室 */
    private static final String OFFICE = "本校事務室";    
    
    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    public boolean svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response,
            Vrw32alp svf,
            DB2UDB pDb2
    ) throws Exception {
        _hasData = false;
        dumpParam(request);
        _param = createParam(request);

        _svf = svf;
        _form = new Form(FORM_FILE, response, _svf);

        db2 = pDb2;

        try {

            _param.load(db2);

            log.debug(">>志願者番号=" + _param._applicantNo);
            log.debug(">>>学籍番号=" + _param._schregNo);
            
            printClaim();

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printClaim() throws SQLException, Exception {
        int sumAmount = 0;
        int sumCnt = 0;

        int i = 0;
        for (i = 0; i < _param._claimNo.length; i++) {
            log.debug(">>>請求書番号=" + _param._claimNo[i]);
            log.debug(">>>分割回数=" + _param._seq[i]);
            log.debug(">>>請求回数=" + _param._reissueCnt[i]);
            log.debug(">>>発行回数=" + _param._reClaimCnt[i]);
            log.debug(">>>伝票番号=" + _param._slpNo[i]);

            final ClaimPrintHistDat claimPrintHistDat = createClaimPrintHistDat(
                    db2,
                    _param._claimNo[i],
                    _param._seq[i],
                    _param._reissueCnt[i],
                    _param._reClaimCnt[i]
            );
            sumAmount += claimPrintHistDat._claimMoney;
            sumCnt++;
        }

        if (sumCnt != 0) {
            final ApplicantBaseMst applicant = createApplicant(db2, i);

            printMain(applicant, sumAmount, i);
            _form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printMain(final ApplicantBaseMst applicant, final int sumAmount, int i) 
        throws SQLException, Exception {
            prtApplicant(applicant, sumAmount);
    }

    private void prtApplicant(final ApplicantBaseMst applicant, final int sumAmount) {
        /* 送付先住所印刷 */
        _form.printShipAdd(applicant);        
        /* 送付先氏名印刷 */
        _form.printShipName(applicant);
        /* 送付先学生氏名印刷 */
        _form.printName(applicant);

        /* 作成日 */
        _form._svf.VrsOut("DATE1", getJDate(_param._date1));
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME1", _param._staffSchoolMst._schoolName1);
        _form._svf.VrsOut("SCHOOLNAME2", _param._staffSchoolMst._schoolName1);
        /* 支払期限・日付 */
        _form._svf.VrsOut("DATE2", KNJ_EditDate.h_format_JP(_param._date2));
        /* 支払期限・曜日 */
        _form._svf.VrsOut("YOUBI", "(" + KNJ_EditDate.h_format_W(_param._date2) + ")");

        _form.prtAmount(applicant, sumAmount);

        /* 学校宛先・郵便番号 */
        _form._svf.VrsOut("SCHOOLZIPCD", _param._staffSchoolMst._schoolZipCd);
        /* 学校宛先・住所１ */
        _form.printSchAddr1();
        /* 学校宛先・住所２ */
        _form.printSchAddr2();
        /* 学校宛先・学校名 */
        _form._svf.VrsOut("SCHOOLNAME2", _param._staffSchoolMst._schoolName1 + "　" + OFFICE);
        /* 学校宛先・電話番号 */
        _form._svf.VrsOut("SCHOOLTELNO", _param._staffSchoolMst._schoolTelNo);
        /* 学校宛先・ＦＡＸ番号 */
        _form._svf.VrsOut("SCHOOLFAXNO", _param._staffSchoolMst._schoolFaxNo);
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
        private final String _applicantNo;
        private final String _schregNo;     // 学籍番号　null:新入生
        private final String _date1;        // 督促日付
        private final String _grade;
        private final String _output1;      // 1:督促状２印刷
        private final String[] _claimNo;    // 請求書番号
        private final String[] _seq;        // 分割回数
        private final String[] _reissueCnt; // 請求回数
        private final String[] _reClaimCnt; // 発行回数
        private final String[] _slpNo;      // 伝票番号
        private final String _date2;        // 納入期限
        private final String _select;       // 送り先

        private Map _prefMap;               // 都道府県
        private School _staffSchoolMst;     // 学校名

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String applicantNo,
                final String schregNo,
                final String date1,
                final String grade,
                final String output1,
                final String[] claimNo,
                final String[] seq,
                final String[] reissueCnt,
                final String[] reClaimCnt,
                final String[] slpNo,
                final String date2,
                final String select
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _schregNo = schregNo;
            _date1 = date1;
            _grade = grade;
            _output1 = output1;
            _claimNo = claimNo;
            _seq = seq;
            _reissueCnt = reissueCnt;
            _reClaimCnt = reClaimCnt;
            _slpNo = slpNo;
            _date2 = date2;
            _select = select;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ?
                    (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();

            _staffSchoolMst = createSchool(db2, _param._year);

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
        final String applicantNo = request.getParameter("APPLICANTNO");
        final String schregNo = request.getParameter("SCHREGNO");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        final String grade = request.getParameter("GRADE");
        final String output1 = request.getParameter("CHECK_TOKU1");
        final String[] claimNo = request.getParameterValues("CLAIM_NO[]");
        final String[] seq = request.getParameterValues("SEQ[]");
        final String[] reissueCnt = request.getParameterValues("REISSUE_CNT[]");
        final String[] reClaimCnt = request.getParameterValues("RE_CLAIM_CNT[]");
        final String[] slpNo = request.getParameterValues("SLIP_NO[]");
        final String date2 = KNJ_EditDate.H_Format_Haifun(request.getParameter("PRINT_TIMELIMIT_DAY"));
        final String select = request.getParameter("SEND");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                schregNo,
                date,
                grade,
                output1,
                claimNo,
                seq,
                reissueCnt,
                reClaimCnt,
                slpNo,
                date2,
                select
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

        public Form(final String file,final HttpServletResponse response,
                final Vrw32alp svf) throws IOException {
            _svf = svf;
            _svf.VrSetForm(FORM_FILE, 1);
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

        void printName(final ApplicantBaseMst applicant) {
            if (_param._schregNo.length() == 0) {
                _svf.VrsOut("NAME1", applicant._name);
            } else {
                _svf.VrsOut("NAME1", applicant._schregBaseMst._name);
            }
        }

        void prtAmount(final ApplicantBaseMst applicant, final int sumAmount) {
            if (_param._schregNo.length() == 0) {
                _svf.VrsOut("SCHREGNO", applicant._applicantNo);
                _svf.VrsOut("NAME2", applicant._name);

                if (applicant._studentDiv.equals(STUDENT_DIV_SUPPORT)) {
                    _form._svf.VrsOut("CAMPUS", applicant._belongingDat._schoolName1);
                }
            } else {
                _svf.VrsOut("SCHREGNO", applicant._schregBaseMst._schregNo);
                _svf.VrsOut("NAME2", applicant._schregBaseMst._name);

                if (applicant._schregBaseMst._SchregRegdDat._studentDiv.equals(STUDENT_DIV_SUPPORT)) {
                    _form._svf.VrsOut("CAMPUS", applicant._schregBaseMst._SchregRegdDat._belongingDat._schoolName1);
                }
            }
            _svf.VrsOut("AMOUNT", Integer.toString(sumAmount));
        }

        /** 送付先印刷 */
        public void printShipAdd(ApplicantBaseMst applicant) {
            if (_param._schregNo.length() == 0) {
                printShipAddApplicant(applicant);
            } else {
                if (_param._select.equals(DESTINATION_STUDENT)) {
                    prtDestinationStudent(applicant);
                } else if (_param._select.equals(DESTINATION_PROTECT)) {
                    prtDestinationProtect(applicant);
                } else {
                    prtDestinationGuarant(applicant);
                }
            }
        }

        public void printShipAddApplicant(ApplicantBaseMst applicant) {
            if (_param._select.equals(DESTINATION_STUDENT)) {
                prtDestinationApplicant(applicant);
            } else {
                prtDestinationApplicantProtect(applicant);
            }
        }

        private void prtDestinationApplicant(ApplicantBaseMst applicant) {
            printApplicantZip(applicant._zipcd);
            printAddr1(applicant._prefCd, applicant._addr1, applicant._addr2);
            printAddr2(applicant._addr3);
        }

        private void prtDestinationApplicantProtect(ApplicantBaseMst applicant) {
            if (applicant._gaddr1 != null) {
                printApplicantZip(applicant._gzipCd);
                printAddr1(applicant._gprefCd, applicant._gaddr1, applicant._gaddr2);
                printAddr2(applicant._gaddr3);
            } else {
                prtDestinationApplicant(applicant);
            }
        }

        private void prtDestinationStudent(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._schregAddressDat._addr1.length() != 0) {
                printApplicantZip(applicant._schregBaseMst._schregAddressDat._zipcd);
                printAddr1(applicant._schregBaseMst._schregAddressDat._prefCd,
                        applicant._schregBaseMst._schregAddressDat._addr1,
                        applicant._schregBaseMst._schregAddressDat._addr2);
                printAddr2(applicant._schregBaseMst._schregAddressDat._addr3);
            } else {
                printShipAddApplicant(applicant);
            }
        }

        private void prtDestinationProtect(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._guardianDat._guardAddr1.length() != 0) {
                printApplicantZip(applicant._schregBaseMst._guardianDat._guardZipcd);
                printAddr1(applicant._schregBaseMst._guardianDat._guardPrefCd,
                        applicant._schregBaseMst._guardianDat._guardAddr1,
                        applicant._schregBaseMst._guardianDat._guardAddr2);
                printAddr2(applicant._schregBaseMst._guardianDat._guardAddr3);
            } else {
                prtDestinationStudent(applicant);
            }
        }

        private void prtDestinationGuarant(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._guardianDat._guarantorAddr1.length() != 0) {
                printApplicantZip(applicant._schregBaseMst._guardianDat._guarantorZipcd);
                printAddr1(applicant._schregBaseMst._guardianDat._guarantorPrefCd,
                        applicant._schregBaseMst._guardianDat._guarantorAddr1,
                        applicant._schregBaseMst._guardianDat._guarantorAddr2);
                printAddr2(applicant._schregBaseMst._guardianDat._guarantorAddr3);
            } else {
                prtDestinationProtect(applicant);
            }
        }

        public void printAddr1(String pPrefCd, String pAdd1, String pAdd2) {
            String addres = _param._prefMapString(pPrefCd)
                                + (pAdd1 != null ? pAdd1 : "")
                                + (pAdd2 != null ? pAdd2 : "");

            printApplicantAddr1_1(addres);
        }

        public void printAddr2(String pAdd3) {
            printApplicantAddr1_2(pAdd3 != null ? pAdd3 : "");
        }

        /** 郵便版号 */
        public void printApplicantZip(String pZip) {
            _form._svf.VrsOut("GZIPCD", pZip);
        }

        /** 送付先住所１ */
        public void printApplicantAddr1_1(String pAddres) {
            _form._svf.VrsOut("G_ADDRESS1_1", pAddres);
        }

        /** 送付先住所２ */
        public void printApplicantAddr1_2(String pAddres) {
            _form._svf.VrsOut("G_ADDRESS1_2", pAddres);
        }

        /** 送付先氏名 */
        public void printApplicantName(String pName) {
            _form._svf.VrsOut("G_NAME", pName);
        }

        /** 送付先氏名印刷 */
        public void printShipName(ApplicantBaseMst applicant) {
            if (_param._schregNo.length() == 0) {
                printShipNameApplicant(applicant);
            } else {
                if (_param._select.equals(DESTINATION_PROTECT) || _param._select.equals(DESTINATION_STUDENT)) {
                    prtDestinationProtectName(applicant);
                } else if (_param._select.equals(DESTINATION_GUARANT)){
                    prtDestinationGuarantName(applicant);
                }
            }
        }

        public void printShipNameApplicant(ApplicantBaseMst applicant) {
            if (!_param._select.equals(DESTINATION_STUDENT)) {
                prtDestinationApplicantProtectName(applicant);
            }
        }

        private void prtDestinationApplicantProtectName(ApplicantBaseMst applicant) {
            if (applicant._gname != null) {
                printApplicantName(applicant._gname + PRINT_STATE);
            } else {
                printApplicantName("　　　　　　");
            }
        }

        private void prtDestinationProtectName(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._guardianDat._guardName.length() != 0) {
                printApplicantName(applicant._schregBaseMst._guardianDat._guardName + PRINT_STATE);
            } else {
                printShipNameApplicant(applicant);
            }
        }

        private void prtDestinationGuarantName(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._guardianDat._guarantorName.length() != 0) {
                printApplicantName(applicant._schregBaseMst._guardianDat._guarantorName + PRINT_STATE);
            } else {
                prtDestinationProtectName(applicant);
            }
        }
    }

    // ======================================================================
    /**
     * 志願者基礎マスタ。
     */
    class ApplicantBaseMst {
        private final String _applicantNo;
        private final String _name;         // 氏名
        private final String _gname;        // 保護者氏名
        private final String _gzipCd;       // 保護者郵便番号
        private final String _gprefCd;      // 保護者都道府県コード
        private final String _gaddr1;       // 保護者住所1
        private final String _gaddr2;       // 保護者住所2
        private final String _gaddr3;       // 保護者住所3
        private final String _belongingDiv; // 所属(拠点)
        private final String _studentDiv;   // 学生区分
        private final String _zipcd;        // 郵便番号
        private final String _prefCd;       // 都道府県コード
        private final String _addr1;        // 住所1
        private final String _addr2;        // 住所2
        private final String _addr3;        // 住所3

        private SchregBaseMst _schregBaseMst;
        private BelongingDat _belongingDat;

        ApplicantBaseMst(
                final String applicantNo,
                final String name,
                final String gname,
                final String gzipCd,
                final String gprefCd,
                final String gaddr1,
                final String gaddr2,
                final String gaddr3,
                final String belongingDiv,
                final String studentDiv,
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3
        ) {
            _applicantNo = applicantNo;
            _name = name;
            _gname = gname;
            _gzipCd = gzipCd;
            _gprefCd = gprefCd;
            _gaddr1 = gaddr1;
            _gaddr2 = gaddr2;
            _gaddr3 = gaddr3;
            _belongingDiv = belongingDiv;
            _studentDiv = studentDiv;
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            if (_param._schregNo.length() == 0) {
                _belongingDat = createBelongingDat(db2, _belongingDiv);
            } else {
                _schregBaseMst = createSCHREG_BASE_MST(db2);
            }
        }
    }

    public ApplicantBaseMst createApplicant(DB2UDB db2, int i)
        throws SQLException, Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlApplicantBaseMst(_param._year, _param._applicantNo));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String applicantNo = rs.getString("applicantNo");
                final String name = rs.getString("name");
                final String gname = rs.getString("gname");
                final String gzipCd = rs.getString("gzipCd");
                final String gprefCd = rs.getString("gprefCd");
                final String gaddr1 = rs.getString("gaddr1");
                final String gaddr2 = rs.getString("gaddr2");
                final String gaddr3 = rs.getString("gaddr3");
                final String belongingDiv = rs.getString("belongingDiv");
                final String studentDiv = rs.getString("studentDiv");
                final String zipcd = rs.getString("zipcd");
                final String prefCd = rs.getString("prefCd");
                final String addr1 = rs.getString("addr1");
                final String addr2 = rs.getString("addr2");
                final String addr3 = rs.getString("addr3");

                final ApplicantBaseMst applicantDat = new ApplicantBaseMst(
                        applicantNo,
                        name,
                        gname,
                        gzipCd,
                        gprefCd,
                        gaddr1,
                        gaddr2,
                        gaddr3,
                        belongingDiv,
                        studentDiv,
                        zipcd,
                        prefCd,
                        addr1,
                        addr2,
                        addr3
                );

                applicantDat.load(db2);
                return applicantDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

            log.debug(">>>APPLICANT_BASE_MST に該当するものがありません。");
            throw new Exception();
    }

    private String sqlApplicantBaseMst(String year, String pApplicantNo) {
        return " select"
        + "    APPLICANTNO as applicantNo,"
        + "    NAME as name,"
        + "    GNAME as gname,"
        + "    GZIPCD as gzipCd,"
        + "    GPREF_CD as gprefCd,"
        + "    GADDR1 as gaddr1,"
        + "    GADDR2 as gaddr2,"
        + "    GADDR3 as gaddr3,"
        + "    BELONGING_DIV as belongingDiv,"
        + "    STUDENT_DIV as studentDiv,"
        + "    ZIPCD as zipcd,"
        + "    PREF_CD as prefCd,"
        + "    ADDR1 as addr1,"
        + "    ADDR2 as addr2,"
        + "    ADDR3 as addr3"
        + " from"
        + "    APPLICANT_BASE_MST"
        + " where"
        + "    APPLICANTNO = '" + pApplicantNo + "'"
        ;
    }

    // ======================================================================
    /**
     * 学校マスタ。
     */

    private class School {
        private final String _schoolName1;          // 学校名1
        private final String _schoolZipCd;          // 学校郵便番号
        private final String _schoolAddr1;          // 学校住所1
        private final String _schoolAddr2;          // 学校住所2
        private final String _schoolAddr3;          // 学校住所3
        private final String _schoolTelNo;          // 学校電話番号
        private final String _schoolFaxNo;          // 学校FAX番号

        School(
            final String schoolName1,
            final String schoolZipCd,
            final String schoolAddr1,
            final String schoolAddr2,
            final String schoolAddr3,
            final String schoolTelNo,
            final String schoolFaxNo
        ) {
            _schoolName1 = schoolName1;
            _schoolZipCd = schoolZipCd;
            _schoolAddr1 = schoolAddr1;
            _schoolAddr2 = schoolAddr2;
            _schoolAddr3 = schoolAddr3;
            _schoolTelNo = schoolTelNo;
            _schoolFaxNo = schoolFaxNo;
        }

        public School() {
            _schoolName1 = "";
            _schoolZipCd = "";
            _schoolAddr1 = "";
            _schoolAddr2 = "";
            _schoolAddr3 = "";
            _schoolTelNo = "";
            _schoolFaxNo = "";
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
            final String schoolTelNo = rs.getString("schoolTelNo");
            final String schoolFaxNo = rs.getString("schoolFaxNo");

            final School school = new School(
                schoolName1,
                schoolZipCd,
                schoolAddr1,
                schoolAddr2,
                schoolAddr3,
                schoolTelNo,
                schoolFaxNo
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
                + "    SCHOOLADDR3 as schoolAddr3,"
                + "    SCHOOLTELNO as schoolTelNo,"
                + "    SCHOOLFAXNO as schoolFaxNo"
                + " from"
                + "    SCHOOL_MST"
                + " where"
                + "    YEAR = '" + year + "'";
    }

    // ======================================================================
    /**
     * 生徒。学籍基礎マスタ。
     */
    private class SchregBaseMst {
        private final String _schregNo;      // 学籍番号
        private final String _name;          // 氏名

        private GuardianDat _guardianDat;
        private SchregAddressDat _schregAddressDat;
        private SchregRegdDat _SchregRegdDat;

        SchregBaseMst(
                final String schregNo,
                final String name
        ) {
            _schregNo = schregNo;
            _name = name;
        }

        public void load(DB2UDB db2, String slipNo) throws SQLException, Exception {
            _guardianDat = createGuardianDat(db2, _schregNo);
            _schregAddressDat = createSchregAddressDat(db2, _schregNo);
            _SchregRegdDat = createSchregRegdDat(db2, _param._year, _param._semester, _schregNo);
        }
    }

    public SchregBaseMst createSCHREG_BASE_MST(DB2UDB db2)
        throws SQLException, Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlSchregBaseMst(_param._year, _param._semester, _param._schregNo));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");

                final SchregBaseMst schregBaseMst = new SchregBaseMst(
                        schregNo,
                        name
                );
                schregBaseMst.load(db2, _param._schregNo);
                return schregBaseMst;
            }

            log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
            throw new Exception();
    }

    private String sqlSchregBaseMst(String year, String semester, String schregNo) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where"
                + "    SCHREGNO = '" + schregNo + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍保護者データ。
     */
    private class GuardianDat {
        private final String _guardName;        // 保護者氏名
        private final String _guardZipcd;       // 郵便番号
        private final String _guardPrefCd;      // 都道府県コード
        private final String _guardAddr1;       // 住所1
        private final String _guardAddr2;       // 住所2
        private final String _guardAddr3;       // 住所3
        private final String _guarantorName;    // 保証人氏名
        private final String _guarantorZipcd;   // 保証人郵便番号
        private final String _guarantorPrefCd;  // 保証人都道府県コード
        private final String _guarantorAddr1;   // 保証人住所1
        private final String _guarantorAddr2;   // 保証人住所2
        private final String _guarantorAddr3;   // 保証人住所3

        GuardianDat(
                final String guardName,
                final String guardZipcd,
                final String guardPrefCd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardAddr3,
                final String guarantorName,
                final String guarantorZipcd,
                final String guarantorPrefCd,
                final String guarantorAddr1,
                final String guarantorAddr2,
                final String guarantorAddr3 
        ) {
            _guardName = guardName;
            _guardZipcd = guardZipcd;
            _guardPrefCd = guardPrefCd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardAddr3 = guardAddr3;
            _guarantorName = guarantorName;
            _guarantorZipcd = guarantorZipcd;
            _guarantorPrefCd = guarantorPrefCd;
            _guarantorAddr1 = guarantorAddr1;
            _guarantorAddr2 = guarantorAddr2;
            _guarantorAddr3 = guarantorAddr3;
        }

        public GuardianDat() {
            _guardName = "";
            _guardZipcd = "";
            _guardPrefCd = "";
            _guardAddr1 = "";
            _guardAddr2 = "";
            _guardAddr3 = "";
            _guarantorName = "";
            _guarantorZipcd = "";
            _guarantorPrefCd = "";
            _guarantorAddr1 = "";
            _guarantorAddr2 = "";
            _guarantorAddr3 = "";
        }
    }

    /**
     * 学籍保護者データ取得
     * @param db2
     * @param schregno
     */
    private GuardianDat createGuardianDat(DB2UDB db2, String schregno)
        throws SQLException {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlGuardianDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String guardName = rs.getString("guardName");
            final String guardZipcd = rs.getString("guardZipcd");
            final String guardPrefCd = rs.getString("guardPrefCd");
            final String guardAddr1 = rs.getString("guardAddr1");
            final String guardAddr2 = rs.getString("guardAddr2");
            final String guardAddr3 = rs.getString("guardAddr3");
            final String guarantorName = rs.getString("guarantorName");
            final String guarantorZipcd = rs.getString("guarantorZipcd");
            final String guarantorPrefCd = rs.getString("guarantorPrefCd");
            final String guarantorAddr1 = rs.getString("guarantorAddr1");
            final String guarantorAddr2 = rs.getString("guarantorAddr2");
            final String guarantorAddr3 = rs.getString("guarantorAddr3");

            final GuardianDat guardianDat = new GuardianDat(
                    guardName,
                    guardZipcd,
                    guardPrefCd,
                    guardAddr1,
                    guardAddr2,
                    guardAddr3,
                    guarantorName,
                    guarantorZipcd,
                    guarantorPrefCd,
                    guarantorAddr1,
                    guarantorAddr2,
                    guarantorAddr3
            );
            return guardianDat;
        }                    
        return new GuardianDat();
    }

    private String sqlGuardianDat(String schregno) {
        return " select"
                + "    GUARD_NAME as guardName,"
                + "    GUARD_ZIPCD as guardZipcd,"
                + "    GUARD_PREF_CD as guardPrefCd,"
                + "    GUARD_ADDR1 as guardAddr1,"
                + "    GUARD_ADDR2 as guardAddr2,"
                + "    GUARD_ADDR3 as guardAddr3,"
                + "    GUARANTOR_NAME as guarantorName,"
                + "    GUARANTOR_ZIPCD as guarantorZipcd,"
                + "    GUARANTOR_PREF_CD as guarantorPrefCd,"
                + "    GUARANTOR_ADDR1 as guarantorAddr1,"
                + "    GUARANTOR_ADDR2 as guarantorAddr2,"
                + "    GUARANTOR_ADDR3 as guarantorAddr3"
                + " from"
                + "    GUARDIAN_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍住所データ。
     */
    private class SchregAddressDat {
        private final String _zipcd; // 郵便番号
        private final String _prefCd; // 都道府県
        private final String _addr1; // 住所１
        private final String _addr2; // 住所２
        private final String _addr3; // 住所３

        SchregAddressDat(
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
        }

        public SchregAddressDat() {
            _zipcd = "";
            _prefCd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
        }
    }

    private SchregAddressDat createSchregAddressDat(DB2UDB db2, String schregno)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlSchregAddressDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String zipcd = rs.getString("zipcd");
            final String prefCd = rs.getString("prefCd");
            final String addr1 = rs.getString("addr1");
            final String addr2 = rs.getString("addr2");
            final String addr3 = rs.getString("addr3");

            final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
                    zipcd,
                    prefCd,
                    addr1,
                    addr2,
                    addr3
            );
            return studentSchregAddressDat;
        }                    
        return new SchregAddressDat();
    }

    private String sqlSchregAddressDat(String schregno) {
        return " select"
                + "    ZIPCD as zipcd,"
                + "    PREF_CD as prefCd,"
                + "    ADDR1 as addr1,"
                + "    ADDR2 as addr2,"
                + "    ADDR3 as addr3"
                + " from"
                + "    SCHREG_ADDRESS_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                + " order by ISSUEDATE DESC";
    }

    // ======================================================================
    /**
     * 請求書発行履歴データ。
     */
    private class ClaimPrintHistDat {
        private final int _claimMoney;      // 請求額

        ClaimPrintHistDat(final int claimMoney) {
            _claimMoney = claimMoney;
        }
    }

    public ClaimPrintHistDat createClaimPrintHistDat(
                DB2UDB db2,
                String pClaimNo,
                String pSeq,
                String pReissueCnt,
                String pReClaimCnt
    )
        throws SQLException, Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlClaimPrintHistDat(
                    pClaimNo,
                    pSeq,
                    pReissueCnt,
                    pReClaimCnt
            ));

            rs = ps.executeQuery();
            while (rs.next()) {
                final int claimMoney = Integer.parseInt(rs.getString("claimMoney"));

                final ClaimPrintHistDat claimPrintHistDat = new ClaimPrintHistDat(
                        claimMoney
                );
                return claimPrintHistDat;
            }

            log.debug(">>>CLAIM_PRINT_HIST_DAT に該当するものがありません。");
            throw new Exception();
    }

    private String sqlClaimPrintHistDat(
                String claimNo,
                String seq, 
                String reissueCnt,
                String reClaimCnt
    ) {

        return " select"
                + "    CLAIM_MONEY as claimMoney"
                + " from"
                + "    CLAIM_PRINT_HIST_DAT"
                + " where"
                + "    CLAIM_NO = '" + claimNo + "' and"
                + "    SEQ = '" + seq + "' and"
                + "    REISSUE_CNT = '" + reissueCnt + "' and"
                + "    RE_CLAIM_CNT = '" + reClaimCnt + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _studentDiv;   // 学生区分
        private final String _grade;

        private BelongingDat _belongingDat;

        SchregRegdDat(
                final String studentDiv,
                final String grade
        ) {
            _studentDiv = studentDiv;
            _grade = grade;
        }

        public void load(DB2UDB db2) throws SQLException {
            _belongingDat = createBelongingDat(db2, _grade);
        }
    }

    public SchregRegdDat createSchregRegdDat(DB2UDB db2, String YEAR, String SEMESTER, String SCHREGNO)
        throws SQLException, Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String studentDiv = rs.getString("studentDiv");
                final String grade = rs.getString("grade");

                final SchregRegdDat schregRegdDat = new SchregRegdDat(
                        studentDiv,
                        grade
                );
                schregRegdDat.load(db2);
                return schregRegdDat;
            }

            log.debug(">>>SCHREG_REGD_DAT に該当するものがありません。");
            throw new Exception();
    }

    private String sqlSchregRegdDat(String year, String semester, String schregNo) {
        return " select"
                + "    STUDENT_DIV as studentDiv,"
                + "    GRADE as grade"
                + " from"
                + "    SCHREG_REGD_DAT"
                + " where"
                + "    SCHREGNO = '" + schregNo + "' and"
                + "    YEAR = '" + year + "' and"
                + "    SEMESTER = '" + semester + "'"
                ;
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    private class BelongingDat {
        private final String _schoolName1;

        BelongingDat() {
            _schoolName1 = "";
        }

        BelongingDat(
                final String schoolName1
        ) {
            _schoolName1 = schoolName1;
        }
    }

    public BelongingDat createBelongingDat(DB2UDB db2, String belongingDiv)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlBelongingDat(belongingDiv));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                
                final BelongingDat belonging = new BelongingDat(
                        name
                );
                return belonging;
            }
            return new BelongingDat();
    }

    private String sqlBelongingDat(String belongingDiv) {
        return " select"
                + "    SCHOOLNAME1 as name"
                + " from"
                + "    BELONGING_MST"
                + " where"
                + "    BELONGING_DIV = '" + belongingDiv + "'"
                ;
    }
} // KNJWP142

// eof
