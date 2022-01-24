// kanji=漢字
/*
 * $Id: 391f3bf715c435a5bb227d63fe5e655588be2530 $
 *
 * 作成日: 2007/11/15 11:09:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

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
 * 督促状
 * 
 * @author nakada
 * @version $Id: 391f3bf715c435a5bb227d63fe5e655588be2530 $
 */
public class KNJWP141 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP141.class);

    private static final String FORM_FILE = "KNJWP141.frm";

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 督促状印刷 */
    private static final String OUTPUT1_PRINT_ON = "1";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int CLAIM_DETAILS_MAX = 10;

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
     * 支払方法
     */
    /** 分割 */
    private static final String PAYMENT_DIVISION = "3";    

    /*
     * 本文テキスト
     */
    /** 本文１ */
    private static final String TEXT1 = "謹啓";    
    /** 季節語 */
    private static final String TEXT2 = "W023";    
    /** 本文２ */
    private static final String TEXT3 = "、保護者の皆様方におかれましては、"
                                        + "ますますご盛栄のこととお慶び申し上げます。";

    /** 様 */
    private static final String PRINT_STATE = "　様";

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
    OldParam _oldParam; // 同一請求番号による集計キー

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

            printPrc();

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printPrc() throws SQLException, Exception {
        _oldParam = new OldParam();
        int sumAmount = 0;
        int sumCnt = 0;
        String skipFlg = null;

        for (int i = 0; i < _param._claimNo.length; i++) {
            log.debug(">>>請求書番号=" + _param._claimNo[i]);
            log.debug(">>>分割回数=" + _param._seq[i]);
            log.debug(">>>請求回数=" + _param._reissueCnt[i]);
            log.debug(">>>発行回数=" + _param._reClaimCnt[i]);
            log.debug(">>>伝票番号=" + _param._slpNo[i]);

            if (!_param._claimNo[i].equals(_oldParam._claimNo)) {
                if (_oldParam._claimNo != null && skipFlg.equals("sum")) {
                    final ApplicantBaseMst applicant = createApplicant(db2);

                    printMain(applicant, sumAmount);
                }

               _oldParam = new OldParam(
                       _param._claimNo[i],
                       _param._seq[i],
                       _param._reissueCnt[i],
                       _param._reClaimCnt[i],
                       _param._slpNo[i]
               );

               sumAmount = 0;
               sumCnt = 0;

               skipFlg = chekClaim(i);
            }

            if (skipFlg == "sum") {
                sumAmount = sumProc(sumAmount, i);
                sumCnt++;
            }
        }

        if (sumCnt != 0) {
            final ApplicantBaseMst applicant = createApplicant(db2);

            printMain(applicant, sumAmount);
        }
    }

    private String chekClaim(int i) throws SQLException, Exception {
        String skipFlg;
        ClaimDat claimDat = createClaimDat(db2, _param._slpNo[i]);

           if (claimDat._mannerPayment.equals(PAYMENT_DIVISION)) {
               log.debug(">>>>支払方法が分割は対象外です。処理しません。");
               skipFlg = "skip";
           } else {
               skipFlg = "sum";
           }
        return skipFlg;
    }

    private int sumProc(int sumAmount, int i) throws SQLException, Exception {
        final ClaimPrintHistDat claimPrintHistDat = createClaimPrintHistDat(
                db2,
                _param._claimNo[i],
                _param._seq[i],
                _param._reissueCnt[i],
                _param._reClaimCnt[i]
        );
        sumAmount += claimPrintHistDat._claimMoney;
        return sumAmount;
    }

    private void printMain(final ApplicantBaseMst applicant, final int sumAmount) 
        throws SQLException {

            printApplicant(applicant, sumAmount);
    }

    private void printApplicant(final ApplicantBaseMst applicant, final int sumAmount) 
        throws SQLException {

        if (chkClaimDetailCnt(applicant)) {
            prtApplicant(applicant, sumAmount);
            _form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private void prtApplicant(final ApplicantBaseMst applicant, final int sumAmount) {
        /* 送付先住所印刷 */
        _form.printShipAdd(applicant);        
        /* 送付先氏名印刷 */
        _form.printShipName(applicant);
        /* 送付先志願者氏名印刷 */
        _form.printName(applicant);

        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._date1));
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME1_1", _param._staffSchoolMst._schoolName1);
        _form._svf.VrsOut("SCHOOLNAME1_2", _param._staffSchoolMst._schoolName1);
        /* 本文 */
        _form._svf.VrsOut("TEXT1", "　" + TEXT1 + "　" + _param._seasonWordString() + TEXT3);

        prtClaimDetailFrom(applicant, sumAmount);

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

    private int prtClaimDetailFrom(final ApplicantBaseMst applicant, final int sumAmount) {
        int i = 0;

        for (Iterator it = applicant._claimDetailsDats.iterator(); it.hasNext();) {
            i++;

            final ClaimDetailsDat detailsDat = (ClaimDetailsDat) it.next();

            _form._svf.VrsOutn("COMMODITY_NAME", i, detailsDat._commodityMst._commodityName);
            _form._svf.VrsOutn("PRICE1", i, Integer.toString(detailsDat._totalClaimMoney));
        }
        _form._svf.VrsOutn("FLG", i, "1");

        i++;
        _form._svf.VrsOutn("COMMODITY_NAME", i, "合　計");
        _form._svf.VrsOutn("PRICE1", i, Integer.toString(sumAmount));
        return i;
    }

    private boolean chkClaimDetailCnt(final ApplicantBaseMst applicant) {
        if (applicant._claimDetailsDats.size() > CLAIM_DETAILS_MAX) {
            log.debug(">>>>伝票明細数が、最大件数：" 
                    + CLAIM_DETAILS_MAX + "件を超えています。");
            return false;
        }

        return true;
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
        private final String _output1;      // 1:送り状印刷
        private final String[] _claimNo;    // 請求書番号
        private final String[] _seq;        // 分割回数
        private final String[] _reissueCnt; // 請求回数
        private final String[] _reClaimCnt; // 発行回数
        private final String[] _slpNo;      // 伝票番号
        private final String _date2;        // 納入期限
        private final String _select;       // 送り先
        private final String _nameCd2;

        private Map _prefMap;               // 都道府県
        private School _staffSchoolMst;     // 学校名
        private Map _seasonWordMap;

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
                final String select,
                final String nameCd2
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
            _nameCd2 = nameCd2;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ?
                    (String) _prefMap.get(pref) : "";
        }

        public String _seasonWordString() {
            return (String) _seasonWordMap.get(_param._nameCd2) != null ?
                    (String) _seasonWordMap.get(_param._nameCd2) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();

            _staffSchoolMst = createSchool(db2, _param._year);
            _seasonWordMap = getNameMst(TEXT2);

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
        // 送り状の時候の挨拶
        final String nameCd2 = request.getParameter("NAMECD2");

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
                select,
                nameCd2
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================
    private class OldParam {
        private final String _claimNo;    // 請求書番号
        private final String _seq;        // 分割回数
        private final String _reissueCnt; // 請求回数
        private final String _reClaimCnt; // 発行回数
        private final String _slipNo;

        public OldParam(
                final String claimNo,
                final String seq,
                final String reissueCnt,
                final String reClaimCnt,
                final String slipNo
        ) {
            _claimNo = claimNo;
            _seq = seq;
            _reissueCnt = reissueCnt;
            _reClaimCnt = reClaimCnt;
            _slipNo = slipNo;
        }
        public OldParam(
        ) {
            _claimNo = null;
            _seq = null;
            _reissueCnt = null;
            _reClaimCnt = null;
            _slipNo = null;
        }
    }

    // ======================================================================

    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response,
                final Vrw32alp svf) throws IOException {
            _svf = svf;
            _svf.VrSetForm(file, 1);
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
                _svf.VrsOut("NAME", applicant._name);
            } else {
                _svf.VrsOut("NAME", applicant._schregBaseMst._name);
            }
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
            printApplicantZip(applicant._zipCd);
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
                if (_param._select.equals(DESTINATION_PROTECT)) {
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
    class ApplicantBaseMst {
        private final String _applicantNo;
        private final String _name;     // 氏名
        private final String _gname;    // 保護者氏名
        private final String _gzipCd;   // 保護者郵便番号
        private final String _gprefCd;  // 保護者都道府県コード
        private final String _gaddr1;   // 保護者住所1
        private final String _gaddr2;   // 保護者住所2
        private final String _gaddr3;   // 保護者住所3
        private final String _schregNo; // 学籍番号
        private final String _zipCd;    // 郵便番号
        private final String _prefCd;   // 都道府県コード
        private final String _addr1;    // 住所1
        private final String _addr2;    // 住所2
        private final String _addr3;    // 住所3

        private SchregBaseMst _schregBaseMst;
        private List _claimDetailsDats;         // 伝票明細データ

        ApplicantBaseMst(
                final String applicantNo,
                final String name,
                final String gname,
                final String gzipCd,
                final String gprefCd,
                final String gaddr1,
                final String gaddr2,
                final String gaddr3,
                final String schregNo,
                final String zipCd,
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
            _schregNo = schregNo;
            _zipCd = zipCd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregBaseMst = createSCHREG_BASE_MST(db2);
            _claimDetailsDats = createClaimDetailsDats(db2, _oldParam._slipNo);
        }
    }

    public ApplicantBaseMst createApplicant(DB2UDB db2)
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
                final String schregNo = rs.getString("schregNo");
                final String zipCd = rs.getString("zipCd");
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
                        schregNo,
                        zipCd,
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
        + "    SCHREGNO as schregNo,"
        + "    ZIPCD as zipCd,"
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
     * 伝票明細データ。
     */
    private class ClaimDetailsDat {
        private final String _commodityCd;      // 商品コード
        private final int _totalClaimMoney;  // 請求額

        private CommodityMst _commodityMst;      // 商品マスタ

        ClaimDetailsDat(
                final String commodityCd,
                final int totalClaimMoney
        ) {
            _commodityCd = commodityCd;
            _totalClaimMoney = totalClaimMoney;
        }

        public void load(DB2UDB db2, String commodityCd) throws SQLException {
            _commodityMst = createCommodityMst(db2, commodityCd);
        }
    }

    private List createClaimDetailsDats(DB2UDB db2, String slipNo) throws SQLException, Exception {
        final List rtn = new ArrayList();
        final String sql = sqlClaimDetailsDat(slipNo);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            final String commodityCd = rs.getString("commodityCd");
            final int totalClaimMoney = Integer.parseInt(rs.getString("totalClaimMoney"));

            final ClaimDetailsDat claimDetailsDat = new ClaimDetailsDat(
                    commodityCd,
                    totalClaimMoney
            );

            claimDetailsDat.load(db2, commodityCd);
            rtn.add(claimDetailsDat);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>CLAIM_DETAILS_DAT に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlClaimDetailsDat(String slipNo) {
        return " select"
                + "    COMMODITY_CD as commodityCd,"
                + "    TOTAL_CLAIM_MONEY as totalClaimMoney"
                + " from"
                + "    CLAIM_DETAILS_DAT"
                + " where"
                + "    SLIP_NO = '" + slipNo + "'"
                + " order by SEQ"
                ;
    }

    // ======================================================================
    /**
     * 商品マスタ。
     */
    private class CommodityMst {
        private final String _commodityName;      // 商品名称

        CommodityMst(
                final String commodityName
        ) {
            _commodityName = commodityName;
        }

        public CommodityMst() {
            _commodityName = "";
        }
    }

    private CommodityMst createCommodityMst(DB2UDB db2, String commodityCd) throws SQLException {
        final String sql = sqlCommodityMst(commodityCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String commodityName = rs.getString("commodityAbbv");

            final CommodityMst commodityMst = new CommodityMst(
                    commodityName
            );
            return commodityMst;
        }

        return new CommodityMst();
    }

    private String sqlCommodityMst(String commodityCd) {
        return " select"
                + "    COMMODITY_ABBV as commodityAbbv"
                + " from"
                + "    COMMODITY_MST"
                + " where"
                + "    COMMODITY_CD = '" + commodityCd + "'"
                ;
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

        SchregBaseMst(
                final String schregNo,
                final String name
        ) {
            _schregNo = schregNo;
            _name = name;
        }

        public SchregBaseMst() {
            _schregNo = "";
            _name = "";
        }

        public void load(DB2UDB db2, String slipNo) throws SQLException {
            _guardianDat = createGuardianDat(db2, _schregNo);
            _schregAddressDat = createSchregAddressDat(db2, _schregNo);
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

        return new SchregBaseMst();
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
        private final String _slipNo;           // 伝票番号
        private final int _claimMoney;      // 請求額

        ClaimPrintHistDat(final String slipNo, final int claimMoney) {
            _slipNo = slipNo;
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
                final String slipNo = rs.getString("slipNo");
                final int claimMoney = Integer.parseInt(rs.getString("claimMoney"));

                final ClaimPrintHistDat claimPrintHistDat = new ClaimPrintHistDat(
                    slipNo,
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
                + "    SLIP_NO as slipNo,"
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
     * 伝票データ。
     */

    private class ClaimDat {
        private final String _mannerPayment;    // 支払い方法
        private final String _totalClaimMoney;  // 請求額

        ClaimDat(
                final String mannerPayment,
                final String totalClaimMoney
        ) {
            _mannerPayment = mannerPayment;
            _totalClaimMoney = totalClaimMoney;
        }
    }

    private ClaimDat createClaimDat(DB2UDB db2, String slipNo) throws SQLException, Exception {
        final String sql = sqlClaimDat(slipNo);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String mannerPayment = rs.getString("mannerPayment");
            final String totalClaimMoney = rs.getString("totalClaimMoney");

            final ClaimDat claimDat = new ClaimDat(mannerPayment, totalClaimMoney);
            return claimDat;
        }

        log.debug(">>>CLAIM_DAT に該当するものがありません。");
        throw new Exception();
    }

    private String sqlClaimDat(String slipNo) {
        return " select"
                + "    MANNER_PAYMENT as mannerPayment,"
                + "    TOTAL_CLAIM_MONEY as totalClaimMoney"
                + " from"
                + "    CLAIM_DAT"
                + " where"
                + "    SLIP_NO = '" + slipNo + "'";
    }
} // KNJWP141
// eof
