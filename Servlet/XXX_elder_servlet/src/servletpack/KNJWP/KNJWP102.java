// kanji=漢字
/*
 * $Id: 3983143a64e60527531d904b0468858622f1cacb $
 *
 * 作成日: 2007/11/09 16:33:00 - JST
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
 * 仮想口座付請求書
 * 
 * @author nakada
 * @version $Id: 3983143a64e60527531d904b0468858622f1cacb $
 */
public class KNJWP102 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP102.class);

    private static final String FORM_FILE = "KNJWP102.frm";
    private static final String FORM_FILE2 = "KNJWP102_2.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /* 振込金受領書 --------------------------------------------------------------- */
    /** 銀行名称 */
    private static final int BANK_NAME1_LENG = 12;
    private static final int BANK_NAME2_LENG = 17;
    /** 受取人名前 */
    private static final int SCHOOL_NAME1_LENG = 10;
    /** 生徒名 */
    private static final int NAME1_LENG = 8;
    /** ご依頼人・住所 */
    private static final int SCHREG_ADD1_1_1_LENG = 15;
    private static final int SCHREG_ADD1_1_2_LENG = 30;
    /* 振込依頼書 --------------------------------------------------------------- */
    /** 受取人名前 */
    private static final int SCHOOL_NAME2_LENG = 15;
    /** 受取人・住所 */
    private static final int SCHOOL_ADD2_1_1_LENG = 18;
    /** 生徒名 */
    private static final int NAME2_LENG = 15;
    /** ご依頼人・住所 */
    private static final int SCHREG_ADD2_1_LENG = 24;

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 仮想口座付請求書印刷 */
    private static final String OUTPUT2_PRINT_ON = "1";

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
    /** 信販用 */
    private static final String MANNER_PAYMENT_CREDIT = "2";
    private static final String MANNER_PAYMENT_CREDIT_NAME = "信販用";
    /** 分割 */
    private static final String MANNER_PAYMENT_INSTALLMENTS = "3";
    private static final String MANNER_PAYMENT_DEFAULT_TEXT = "到着後５日以内に";

    /** 様 */
    private static final String PRINT_STATE = "　様";

    /*
     * 授業料区分
     */
    /** 増単位 */
    private static final String TUITION_DIV_PLUS = "2";    

    /*
     * 名称マスタキー（NAMECD1）
     */
    // 口座種別
    private static final String VACCOUNTDIV = "W015";

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;
    OldParam _oldParam; // 同一請求番号による集計キー
    private String _formDiv = FORM_FILE;

    public boolean svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Vrw32alp svf,
            final DB2UDB pDb2,
            final KNJWP102ParamList paramList
    ) throws Exception {
        _hasData = false;
        dumpParam(request);
        _param = createParam(request, paramList);

        _svf = svf;
        _form = new Form(FORM_FILE, response, _svf);
        db2 = pDb2;

        try {
            _param.load(db2);
            printPrc();

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printPrc() throws SQLException, Exception {
        _oldParam = new OldParam();
        boolean set1 = false;
        ApplicantBaseMst applicant = null;
        ClaimPrintHistDat claimPrintHistDat = null;
        ClaimDat claimDat = null;

        for (int i = 0; i < _param._claimNo.length; i++) {
            log.debug(">>>請求書番号=" + _param._claimNo[i]);
            log.debug(">>>分割回数=" + _param._seq[i]);
            log.debug(">>>請求回数=" + _param._reissueCnt[i]);
            log.debug(">>>発行回数=" + _param._reClaimCnt[i]);
            log.debug(">>>伝票番号=" + _param._slpNo[i]);

            if ((!_param._claimNo[i].equals(_oldParam._claimNo)) ||
                    (!_param._slpNo[i].equals(_oldParam._slipNo))){

                set1 = false;

                _oldParam = new OldParam(
                        _param._claimNo[i],
                        _param._slpNo[i]
                );
            }

            if (set1 == false) {
                applicant = createApplicant(db2);
                claimDat = createClaimDat(db2, _param._slpNo[i]);
            }

            claimPrintHistDat = createClaimPrintHistDat(
                    db2,
                    _param._claimNo[i],
                    _param._seq[i],
                    _param._reissueCnt[i],
                    _param._reClaimCnt[i]
            );

            printMain(applicant, claimPrintHistDat, claimDat, i);

        }
    }

    private void printMain(
            final ApplicantBaseMst applicant,
            final ClaimPrintHistDat claimPrintHistDat,
            final ClaimDat claimDat,
            final int i
    ) throws SQLException {

        if ((_param._schregno.length() != 0) && (claimDat._plus)) {
            // 進級生
            _form._svf.VrSetForm(FORM_FILE2, 1);
            _formDiv = FORM_FILE2;
        } else {
            _form._svf.VrSetForm(FORM_FILE, 1);
            _formDiv = FORM_FILE;
        }

        printApplicant(applicant, claimPrintHistDat, claimDat, i);
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void printApplicant( 
        final ApplicantBaseMst applicant,
        final ClaimPrintHistDat claimPrintHistDat,
        final ClaimDat claimDat,
        final int i
    )
        throws SQLException {

        if (claimDat._mannerPayment.equals(MANNER_PAYMENT_CREDIT)) {
            /* 信販用 */
            _form._svf.VrsOut("ITEM", MANNER_PAYMENT_CREDIT_NAME);
        }

        if (claimDat._mannerPayment.equals(MANNER_PAYMENT_INSTALLMENTS)) {
            /* 分割 */
            _form._svf.VrsOut("LIMIT_DAY", KNJ_EditDate.h_format_JP_MD(_param._timelimitDay[i]) 
                    + "(" + KNJ_EditDate.h_format_W(_param._timelimitDay[i]) + ")までに");
        } else {
            _form._svf.VrsOut("LIMIT_DAY", MANNER_PAYMENT_DEFAULT_TEXT); 
        }

        if (_formDiv.equals(FORM_FILE2)) {
            /* 学校名 */
            _form._svf.VrsOut("SCHOOLNAME", _param._staffSchoolMst._schoolName1);
            /* 送付先・住所 */
            _form.printShipAdd(applicant);

            if (!_param._select.equals(DESTINATION_STUDENT)) {
                /* 送付先氏名 */
                _form.printShipName(applicant);
            }
        }

        /*
         * 振込金受領書 ----------------------------------------------------------------------------
         */

        /* 金額 */
        _form._svf.VrsOut("MONEY1", Integer.toString(claimPrintHistDat._claimMoney));
        /* 銀行名 */
        _form.printBank1(applicant);
        /* 口座種別 */
        _form._svf.VrsOut("DEPOSIT_DIV1", _param._vaccountDivMapString(applicant._VAccountDat._virtualAccountDiv));
        /* 口座番号 */
        _form._svf.VrsOut("VIRTUAL_ACCOUNT_NO1", applicant._VAccountDat._virtualAccountNo);
        /* 受取人名 */
        _form.printSchoolName1(applicant);
        /* 生徒名 */
        _form.printName1(applicant);
        /* 生徒・住所 */
        _form.printShipAddHoge(applicant);

        /*
         * 振込依頼書 ------------------------------------------------------------------------------
         */
        /* 金額 */
        _form._svf.VrsOut("MONEY2", Integer.toString(claimPrintHistDat._claimMoney));
        /* 銀行名 */
        _form.printBank2(applicant);
        /* 口座種別 */
        _form._svf.VrsOut("DEPOSIT_DIV2", _param._vaccountDivMapString(applicant._VAccountDat._virtualAccountDiv));
        /* 口座番号 */
        _form._svf.VrsOut("VIRTUAL_ACCOUNT_NO2", applicant._VAccountDat._virtualAccountNo);
        /* 受取人・フリガナ */
        _form.printSchoolNameKana(applicant);
        /* 受取人・名前 */
        _form.printSchoolName2(applicant);
        /* 受取人・住所１ */
        _form.printSchoolAddr1(applicant);
        /* 受取人・電話番号 */
        _form._svf.VrsOut("ACCOUNT_TELNO", applicant._VBankMst._accountTelno);
        /* 生徒カナ */
        _form.printNameKana(applicant);
        /* 生徒名 */
        _form.printName2(applicant);
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
        private final String _date;         // 請求日付／作成日
        private final String _checkKasou1;  // 1:仮想口座付請求書印刷 本科生
        private final String _checkKasou2;  // 1:仮想口座付請求書印刷 科目履修
        private final String[] _claimNo;    // 請求書番号
        private final String[] _seq;        // 分割回数
        private final String[] _reissueCnt; // 請求回数
        private final String[] _reClaimCnt; // 発行回数
        private final String[] _slpNo;      // 伝票番号
        private final String _select;       // 送り先
        private final String _schregno;
        private final String[] _timelimitDay;

        private School _staffSchoolMst;             // 学校名
        private Map _prefMap;           // 都道府県
        private Map _vaccountDivMap;    // 講座種別

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String applicantNo,
                final String date,
                final String checkKasou1,
                final String checkKasou2,
                final String[] claimNo,
                final String[] seq,
                final String[] reissueCnt,
                final String[] reClaimCnt,
                final String[] slpNo,
                final String select,
                final String schregno,
                final String[] timelimitDay
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _date = date;
            _checkKasou1 = checkKasou1;
            _checkKasou2 = checkKasou2;
            _claimNo = claimNo;
            _seq = seq;
            _reissueCnt = reissueCnt;
            _reClaimCnt = reClaimCnt;
            _slpNo = slpNo;
            _select = select;
            _schregno = schregno;
            _timelimitDay = timelimitDay;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? 
                    (String) _prefMap.get(pref) : "";
        }

        public String _vaccountDivMapString(String vaccountDiv) {
            return (String) _vaccountDivMap.get(vaccountDiv) != null ? 
                    (String) _vaccountDivMap.get(vaccountDiv) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _staffSchoolMst = createSchool(db2, _param._year);
            _prefMap = getPrefMst();
            _vaccountDivMap = getNameMst(VACCOUNTDIV);

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

    private Param createParam(final HttpServletRequest request, KNJWP102ParamList paramList) {
        final String year = paramList.getYear();
        final String semester = paramList.getSemester();
        final String programId = paramList.getProgramId();
        final String dbName = paramList.getDbName();
        final String loginDate = paramList.getLoginDate();
        final String applicantNo = paramList.getApplicantNo();
        final String claimDate = paramList.getClaimDate();
        final String checkKasou1 = paramList.getCheckKasou1();
        final String checkKasou2 = paramList.getCheckKasou2();
        final String[] claimNo = paramList.getClaimNo();
        final String[] seq = paramList.getSeq();
        final String[] reissueCnt = paramList.getReissueCnt();
        final String[] reClaimCnt = paramList.getReClaimCnt();
        final String[] slipNo = paramList.getSlipNo();
        final String select = paramList.getSelect();
        final String schregno = paramList.getSchregno();
        final String[] timelimitDay = paramList.getTimelimitDay();

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                claimDate,
                checkKasou1,
                checkKasou2,
                claimNo,
                seq,
                reissueCnt,
                reClaimCnt,
                slipNo,
                select,
                schregno,
                timelimitDay
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private String cnvNull(String str) {
        if (str == null) {
            return "";
        }

        return str;
    }

    private String[] cnvNull(String[] str) {
        if (str == null) {
            String[] strDummy = {""};
            return strDummy;
        }

        return str;
    }

    // ======================================================================
    private class OldParam {
        private final String _claimNo;    // 請求書番号
        private final String _slipNo;

        public OldParam(
                final String claimNo,
                final String slipNo
        ) {
            _claimNo = claimNo;
            _slipNo = slipNo;
        }
        public OldParam(
        ) {
            _claimNo = null;
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

        /*
         * 振込金受領書 --------------------------------------------------------------
         */
        /** 受取人・先方銀行名 */
        public void printBank1(ApplicantBaseMst applicant) {
            String name = applicant._VBankMst._bankName + " " + applicant._VBankMst._branchName;

            if (name != null) {
                final String label;
                if (name.length() <= BANK_NAME1_LENG) {
                    label = "BANK_NAME1_1";
                } else {
                    label = "BANK_NAME1_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** 受取人・名前 */
        public void printSchoolName1(ApplicantBaseMst applicant) {
            String name = applicant._VBankMst._accountName;

            if (name != null) {
                final String label;
                if (name.length() <= SCHOOL_NAME1_LENG) {
                    label = "ACCOUNT_NAME1_1";
                } else {
                    label = "ACCOUNT_NAME1_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** ご依頼人・生徒名 */
        public void printName1(ApplicantBaseMst applicant) {
            String name = "";

            if (applicant._schregBaseMst._name.length() == 0) {
                name = applicant._name;
            } else {
                name = applicant._schregBaseMst._name;
            }

            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME1_1";
                } else {
                    label = "NAME1_2";
                }
                _form._svf.VrsOut(label, name);

                if (_formDiv.equals(FORM_FILE2)) {
                    _form._svf.VrsOut("NAME", name);
                }
            }
        }

        /*
         * 振込依頼書 -------------------------------------------------------------
         */
        /** 受取人・先方銀行名 */
        public void printBank2(ApplicantBaseMst applicant) {
            String name = applicant._VBankMst._bankName + " " + applicant._VBankMst._branchName;

            if (name != null) {
                final String label;
                if (name.length() <= BANK_NAME2_LENG) {
                    label = "BANK_NAME2_1";
                } else {
                    label = "BANK_NAME2_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** 受取人・カナ */
        public void printSchoolNameKana(ApplicantBaseMst applicant) {
            String name = zenkakuHiraganaToZenkakuKatakana(applicant._VBankMst._accountKana);

            if (name != null) {
                _form._svf.VrsOut("ACCOUNT_KANA", name);
            }
        }

        /** 受取人・名前 */
        public void printSchoolName2(ApplicantBaseMst applicant) {
            String name = applicant._VBankMst._accountName;

            if (name != null) {
                final String label;
                if (name.length() <= SCHOOL_NAME2_LENG) {
                    label = "ACCOUNT_NAME2_1";
                } else {
                    label = "ACCOUNT_NAME2_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** ご依頼人・生徒・カタカナ */
        public void printNameKana(ApplicantBaseMst applicant) {
            String name = zenkakuHiraganaToZenkakuKatakana(applicant._nameKana);

            if (name != null) {
                _form._svf.VrsOut("NAME2_1", name);
            }
        }

        /** ご依頼人・生徒名 */
        public void printName2(ApplicantBaseMst applicant) {
            String name = "";

            if (applicant._schregBaseMst._name.length() == 0) {
                name = applicant._name;
            } else {
                name = applicant._schregBaseMst._name;
            }

            if (name != null) {
                final String label;
                if (name.length() <= NAME2_LENG) {
                    label = "NAME2_2";
                } else {
                    label = "NAME2_3";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** 送付先別住所、電話番号印刷 */
        public void printShipAdd(ApplicantBaseMst applicant) {
            if (_param._schregno.length() == 0) {
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
            } else if (_param._select.equals(DESTINATION_PROTECT)) {
                prtDestinationApplicantProtect(applicant);
            } else {
                prtDestinationApplicantGuarant(applicant);
            }
        }

        private void prtDestinationApplicant(ApplicantBaseMst applicant) {
            printAddr1(applicant._zipcd,
                    applicant._prefCd,
                    applicant._addr1,
                    applicant._addr2);

            printAddr2(applicant._addr3);
        }

        private void prtDestinationApplicantProtect(ApplicantBaseMst applicant) {
            if (applicant._gaddr1 != null) {
                printAddr1(applicant._gzipcd,
                        applicant._gprefCd,
                        applicant._gaddr1,
                        applicant._gaddr2);

                printAddr2(applicant._gaddr3);
            } else {
                prtDestinationApplicant(applicant);
            }
        }

        private void prtDestinationApplicantGuarant(ApplicantBaseMst applicant) {
            if (applicant._guarantorAddr1 != null) {
                printAddr1(applicant._guarantorZipcd,
                        applicant._guarantorPrefCd,
                        applicant._guarantorAddr1,
                        applicant._guarantorAddr2);

                printAddr2(applicant._guarantorAddr3);
            } else {
                prtDestinationApplicantProtect(applicant);
            }
        }

        private void prtDestinationStudent(ApplicantBaseMst applicant) {
            if (applicant._SchregAddressDat._addr1.length() != 0) {
                printAddr1(applicant._SchregAddressDat._zipcd,
                        applicant._SchregAddressDat._prefCd,
                        applicant._SchregAddressDat._addr1,
                        applicant._SchregAddressDat._addr2);

                printAddr2(applicant._SchregAddressDat._addr3);
            } else {
                printShipAddApplicant(applicant);
            }
        }

        private void prtDestinationProtect(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guardAddr1.length() != 0) {
                printAddr1(applicant._guardianDat._guardZipcd,
                        applicant._guardianDat._guardPrefCd,
                        applicant._guardianDat._guardAddr1,
                        applicant._guardianDat._guardAddr2);

                printAddr2(applicant._guardianDat._guardAddr3);
            } else {
                prtDestinationStudent(applicant);
            }
        }

        private void prtDestinationGuarant(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guarantorAddr1.length() != 0) {
                printAddr1(applicant._guardianDat._guarantorZipcd,
                        applicant._guardianDat._guarantorPrefCd,
                        applicant._guardianDat._guarantorAddr1,
                        applicant._guardianDat._guarantorAddr2);

                printAddr2(applicant._guardianDat._guarantorAddr3);
            } else {
                prtDestinationProtect(applicant);
            }
        }

        public void printAddr1(String pZip, String pPrefCd, String pAdd1, String pAdd2) {
            String addres = nvlT(_param._prefMapString(pPrefCd)) + nvlT(pAdd1) + nvlT(pAdd2);

            if (_formDiv.equals(FORM_FILE2)) {
                _form._svf.VrsOut("GZIPCD", nvlT(pZip));
                _form._svf.VrsOut("G_ADDRESS1_1", addres);
            }
        }

        public void printAddr2(String pAdd3) {
            if (_formDiv.equals(FORM_FILE2)) {
                _form._svf.VrsOut("G_ADDRESS1_2", nvlT(pAdd3));
            }
        }

        /** 受取人・住所１ */
        public void printSchoolAddr1(ApplicantBaseMst applicant) {
            String name = (applicant._VBankMst._accountAddr1 != null ? applicant._VBankMst._accountAddr1 : "")
                + (applicant._VBankMst._accountAddr2 != null ? applicant._VBankMst._accountAddr2 : "");
          String name2 = applicant._VBankMst._accountAddr3 != null ? applicant._VBankMst._accountAddr3 : "";
            if (name != null) {
                final String label;
                final String label2;
                if (name.length() <= SCHOOL_ADD2_1_1_LENG) {
                    label = "ACCOUNT_ADDR1_1";
                    label2 = "ACCOUNT_ADDR2_1";
                } else {
                    label = "ACCOUNT_ADDR1_2";
                    label2 = "ACCOUNT_ADDR2_2";
                }
                _form._svf.VrsOut(label, name);
                _form._svf.VrsOut(label2, name2);
            }
        }

        /** 送付先氏名印刷 */
        public void printShipName(ApplicantBaseMst applicant) {
            if (_param._schregno.length() == 0) {
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
            if (_param._select.equals(DESTINATION_PROTECT)) {
                prtDestinationApplicantProtectName(applicant);
            } else if (_param._select.equals(DESTINATION_GUARANT)) {
                prtDestinationApplicantGuarantName(applicant);
            }
        }

        private void prtDestinationApplicantProtectName(ApplicantBaseMst applicant) {
            if (applicant._gname != null) {
                printApplicantName(applicant._gname);
            }
        }

        private void prtDestinationApplicantGuarantName(ApplicantBaseMst applicant) {
            if (applicant._guarantorName != null) {
                printApplicantName(applicant._guarantorName);
            } else {
                prtDestinationApplicantProtectName(applicant);
            }
        }

        private void prtDestinationProtectName(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guardName.length() != 0) {
                printApplicantName(applicant._guardianDat._guardName);
            } else {
                printShipNameApplicant(applicant);
            }
        }

        private void prtDestinationGuarantName(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guarantorName.length() != 0) {
                printApplicantName(applicant._guardianDat._guarantorName);
            } else {
                prtDestinationProtectName(applicant);
            }
        }

        /** 送付先氏名 */
        public void printApplicantName(String pName) {
            _form._svf.VrsOut("G_NAME", pName + PRINT_STATE);
        }

        /*
         * ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞ
         * ただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽ
         * まみむめもゃやゅゆょよらりるれろゎわゐゑをん
         * 
         * ァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾ
         * タダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポ
         * マミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶ
         */
        public String zenkakuHiraganaToZenkakuKatakana(String s) {
            if (s == null || s.equals("")) {
                return "";
            }

            StringBuffer sb = new StringBuffer(s);
            for (int i = 0; i < sb.length(); i++) {
              char c = sb.charAt(i);
              if (c >= 'ぁ' && c <= 'ん') {
                sb.setCharAt(i, (char)(c - 'ぁ' + 'ァ'));
              }
            }
            return sb.toString();    
          }

    

        /** 送付先別住所、電話番号印刷 */
        public void printShipAddHoge(ApplicantBaseMst applicant) {
            if (_param._schregno.length() == 0) {
                printShipAddApplicantHoge(applicant);
            } else {
                prtDestinationStudentHoge(applicant);
            }
        }

        public void printShipAddApplicantHoge(ApplicantBaseMst applicant) {
            printAddr1Hoge(applicant._zipcd,
                    applicant._prefCd,
                    applicant._addr1,
                    applicant._addr2);

            printAddr2Hoge(applicant._addr3);
            printApplicantTelHoge(applicant._telno);
        }

        private void prtDestinationStudentHoge(ApplicantBaseMst applicant) {
            if (applicant._SchregAddressDat._addr1.length() != 0) {
                printAddr1Hoge(applicant._SchregAddressDat._zipcd,
                        applicant._SchregAddressDat._prefCd,
                        applicant._SchregAddressDat._addr1,
                        applicant._SchregAddressDat._addr2);

                printAddr2Hoge(applicant._SchregAddressDat._addr3);
                printApplicantTelHoge(applicant._SchregAddressDat._telno);
            } else {
                printShipAddApplicantHoge(applicant);
            }
        }

        public void printAddr1Hoge(String pZip, String pPrefCd, String pAdd1, String pAdd2) {
            String addres = nvlT(_param._prefMapString(pPrefCd)) + nvlT(pAdd1) + nvlT(pAdd2);

            printApplicantAddr1_1Hoge(addres);
            printApplicantAddr2_1Hoge(addres);
        }

        public void printAddr2Hoge(String pAdd3) {
            printApplicantAddr1_2Hoge(nvlT(pAdd3));
            printApplicantAddr2_2Hoge(nvlT(pAdd3));
        }

        /** ご依頼人・住所 */
        public void printApplicantAddr1_1Hoge(String pAddres) {
            String name = pAddres;

            if (name != null) {
                final String label;
                if (name.length() <= SCHREG_ADD1_1_1_LENG) {
                    label = "ADDRESS1_1_1";
                } else if (name.length() <= SCHREG_ADD1_1_2_LENG) {
                    label = "ADDRESS1_1_2";
                } else {
                    label = "ADDRESS1_1_4";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** ご依頼人・住所２ */
        public void printApplicantAddr1_2Hoge(String pAddres) {
            String name = pAddres;

            if (name != null) {
                _form._svf.VrsOut("ADDERSS1_2", name);
            }
        }

        /** ご依頼人・住所１ */
        public void printApplicantAddr2_1Hoge(String pAddres) {
            String name = pAddres;

            if (name != null) {
                final String label;
                if (name.length() <= SCHREG_ADD2_1_LENG) {
                    label = "ADDRESS2_1_1";
                } else {
                    label = "ADDRESS2_1_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        /** ご依頼人・住所２ */
        public void printApplicantAddr2_2Hoge(String pAddres) {
            String name = pAddres;

            if (name != null) {
                _form._svf.VrsOut("ADDERSS2_2", name);
            }
        }

        /** ご依頼人・電話番号 */
        public void printApplicantTelHoge(String pTelno) {
            _form._svf.VrsOut("TELNO", pTelno);
        }
    
    
    
    
    
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
     * 志願者基礎マスタ。
     */
    private class ApplicantBaseMst {
        private final String _schregNo;        // 学籍番号
        private final String _name;            // 氏名
        private final String _nameKana;        // 氏名かな
        private final String _zipcd;
        private final String _prefCd;          // 都道府県コード
        private final String _addr1;           // 住所1
        private final String _addr2;           // 住所2
        private final String _addr3;           // 住所3
        private final String _telno;           // 電話番号
        private final String _gname;        // 保護者氏名
        private final String _gzipcd;
        private final String _gprefCd;          // 保護者都道府県コード
        private final String _gaddr1;           // 保護者住所1
        private final String _gaddr2;           // 保護者住所2
        private final String _gaddr3;           // 保護者住所3
        private final String _gtelno;           // 保護者電話番号
        private final String _guarantorName;    // 保証人氏名
        private final String _guarantorZipcd;   // 保証人郵便番号
        private final String _guarantorPrefCd;  // 保証人都道府県コード
        private final String _guarantorAddr1;   // 保証人住所1
        private final String _guarantorAddr2;   // 保証人住所2
        private final String _guarantorAddr3;   // 保証人住所3

        private VirtualAccountSchDat _VAccountSchDat;   // 生徒仮想口座データ
        private VirtualBankMst _VBankMst;               // 仮想口座銀行マスタ
        private VirtualAccountDat _VAccountDat;         // 仮想口座管理データ
        private SchregAddressDat _SchregAddressDat;     // 学籍住所データ
        private GuardianDat _guardianDat;
        private SchregBaseMst _schregBaseMst;

        ApplicantBaseMst(
                final String schregNo,
                final String name,
                final String nameKana,
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String telno,
                final String gname,
                final String gzipcd,
                final String gprefCd,
                final String gaddr1,
                final String gaddr2,
                final String gaddr3,
                final String gtelno,
                final String guarantorName,
                final String guarantorZipcd,
                final String guarantorPrefCd,
                final String guarantorAddr1,
                final String guarantorAddr2,
                final String guarantorAddr3
        ) {
            _schregNo = schregNo;
            _name = name;
            _nameKana = nameKana;
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _telno = telno;
            _gname = gname;
            _gzipcd = gzipcd;
            _gprefCd = gprefCd;
            _gaddr1 = gaddr1;
            _gaddr2 = gaddr2;
            _gaddr3 = gaddr3;
            _gtelno = gtelno;
            _guarantorName = guarantorName;
            _guarantorZipcd = guarantorZipcd;
            _guarantorPrefCd = guarantorPrefCd;
            _guarantorAddr1 = guarantorAddr1;
            _guarantorAddr2 = guarantorAddr2;
            _guarantorAddr3 = guarantorAddr3;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _VAccountSchDat = createVirtualAccountSchDat(db2, _param._applicantNo);
            _VBankMst = createVirtualBankMst(db2, _VAccountSchDat._virtualBankCd);
            _VAccountDat = createVirtualAccountDat(db2, _VAccountSchDat._virtualBankCd, _param._applicantNo);
            _SchregAddressDat = createStudentSchregAddressDat(db2, _schregNo);
            _guardianDat = createGuardianDat(db2, _schregNo);
            _schregBaseMst = createSCHREG_BASE_MST(db2);
        }
    }

    public ApplicantBaseMst createApplicant(DB2UDB db2)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlApplicantBaseMst(_param._applicantNo));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String nameKana = rs.getString("nameKana");
                final String zipcd = rs.getString("zipcd");
                final String prefCd = rs.getString("prefCd");
                final String addr1 = rs.getString("addr1");
                final String addr2 = rs.getString("addr2");
                final String addr3 = rs.getString("addr3");
                final String telno = rs.getString("telno");
                final String gname = rs.getString("gname");
                final String gzipcd = rs.getString("gzipcd");
                final String gprefCd = rs.getString("gprefCd");
                final String gaddr1 = rs.getString("gaddr1");
                final String gaddr2 = rs.getString("gaddr2");
                final String gaddr3 = rs.getString("gaddr3");
                final String gtelno = rs.getString("gtelno");
                final String guarantorName = rs.getString("guarantorName");
                final String guarantorZipcd = rs.getString("guarantorZipcd");
                final String guarantorPrefCd = rs.getString("guarantorPrefCd");
                final String guarantorAddr1 = rs.getString("guarantorAddr1");
                final String guarantorAddr2 = rs.getString("guarantorAddr2");
                final String guarantorAddr3 = rs.getString("guarantorAddr3");

                final ApplicantBaseMst applicantBaseMst = new ApplicantBaseMst(
                        schregNo,
                        name,
                        nameKana,
                        zipcd,
                        prefCd,
                        addr1,
                        addr2,
                        addr3,
                        telno,
                        gname,
                        gzipcd,
                        gprefCd,
                        gaddr1,
                        gaddr2,
                        gaddr3,
                        gtelno,
                        guarantorName,
                        guarantorZipcd,
                        guarantorPrefCd,
                        guarantorAddr1,
                        guarantorAddr2,
                        guarantorAddr3
                );

                applicantBaseMst.load(db2);
                return applicantBaseMst;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>APPLICANT_BASE_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlApplicantBaseMst(String applicantNo) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    NAME_KANA as nameKana,"
                + "    ZIPCD as zipcd,"
                + "    PREF_CD as prefCd,"
                + "    ADDR1 as addr1,"
                + "    ADDR2 as addr2,"
                + "    ADDR3 as addr3,"
                + "    TELNO as telno,"
                + "    GNAME as gname,"
                + "    GZIPCD as gzipcd,"
                + "    GPREF_CD as gprefCd,"
                + "    GADDR1 as gaddr1,"
                + "    GADDR2 as gaddr2,"
                + "    GADDR3 as gaddr3,"
                + "    GTELNO as gtelno,"
                + "    GUARANTOR_NAME as guarantorName,"
                + "    GUARANTOR_ZIPCD as guarantorZipcd,"
                + "    GUARANTOR_PREF_CD as guarantorPrefCd,"
                + "    GUARANTOR_ADDR1 as guarantorAddr1,"
                + "    GUARANTOR_ADDR2 as guarantorAddr2,"
                + "    GUARANTOR_ADDR3 as guarantorAddr3"
                + " from"
                + "    APPLICANT_BASE_MST"
                + " where"
                + "    APPLICANTNO = '" + applicantNo + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒仮想口座データ。
     */
    private class VirtualAccountSchDat {
        private final String _virtualBankCd;          // 仮想口座銀行コード1

        VirtualAccountSchDat(
            final String virtualBankCd) {
            _virtualBankCd = virtualBankCd;
        }
    }

    private VirtualAccountSchDat createVirtualAccountSchDat(DB2UDB db2, String applicantNo)
        throws SQLException, Exception {

        final String sql = sqlVirtualAccountSchDat(applicantNo);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            final String virtualBankCd = rs.getString("virtualBankCd");

            final VirtualAccountSchDat school = new VirtualAccountSchDat(virtualBankCd);
            return school;
        }

        log.debug(">>>VIRTUAL_ACCOUNT_SCH_DAT に該当するものがありません。");
        throw new Exception();
    }

    private String sqlVirtualAccountSchDat(String applicantNo) {
        return " select"
                + "    VIRTUAL_BANK_CD as virtualBankCd"
                + " from"
                + "    VIRTUAL_ACCOUNT_SCH_DAT"
                + " where"
                + "    APPLICANTNO = '" + applicantNo + "'"
                + " order by ADJUST_SDATE DESC"
                ;
    }

    // ======================================================================
    /**
     * 仮想口座銀行マスタ。
     */
    private class VirtualBankMst {
        private final String _bankName;             // 銀行名称
        private final String _branchName;           // 銀行名称
        private final String _accountName;          // 口座名義
        private final String _accountKana;          // 口座名義かな
        private final String _accountAddr1;         // 口座名義住所1
        private final String _accountAddr2;         // 口座名義住所2
        private final String _accountAddr3;         // 口座名義住所3
        private final String _accountTelno;         // 口座名義電話番号

        VirtualBankMst(
            final String bankName,
            final String branchName,
            final String accountName,
            final String accountKana,
            final String accountAddr1,
            final String accountAddr2,
            final String accountAddr3,
            final String accountTelno
        ) {
            _bankName = bankName;
            _branchName = branchName;
            _accountName = accountName;
            _accountKana = accountKana;
            _accountAddr1 = accountAddr1;
            _accountAddr2 = accountAddr2;
            _accountAddr3 = accountAddr3;
            _accountTelno = accountTelno;
        }
    }

    private VirtualBankMst createVirtualBankMst(DB2UDB db2, String VBankCd)
        throws SQLException, Exception {

        final String sql = sqlVirtualBankMst(VBankCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String bankName = rs.getString("bankName");
            final String branchName = rs.getString("branchName");
            final String accountName = rs.getString("accountName");
            final String accountKana = rs.getString("accountKana");
            final String accountAddr1 = rs.getString("accountAddr1");
            final String accountAddr2 = rs.getString("accountAddr2");
            final String accountAddr3 = rs.getString("accountAddr3");
            final String accountTelno = rs.getString("accountTelno");

            final VirtualBankMst virtualBankMst = new VirtualBankMst(
                        bankName,
                        branchName,
                        accountName,
                        accountKana,
                        accountAddr1,
                        accountAddr2,
                        accountAddr3,
                        accountTelno
            );
            return virtualBankMst;
        }

        log.debug(">>>VIRTUAL_BANK_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlVirtualBankMst(String virtualBankCd) {
        return " select"
                + "    BANK_NAME as bankName,"
                + "    BRANCH_NAME as branchName,"
                + "    ACCOUNT_NAME as accountName,"
                + "    ACCOUNT_KANA as accountKana,"
                + "    ACCOUNT_ADDR1 as accountAddr1,"
                + "    ACCOUNT_ADDR2 as accountAddr2,"
                + "    ACCOUNT_ADDR3 as accountAddr3,"
                + "    ACCOUNT_TELNO as accountTelno"
                + " from"
                + "    VIRTUAL_BANK_MST"
                + " where"
                + "    VIRTUAL_BANK_CD = '" + virtualBankCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 仮想口座管理データ。
     */
    private class VirtualAccountDat {
           
            
        private final String _virtualAccountDiv;    // 種別
        private final String _virtualAccountNo;     // 仮想口座番号
        VirtualAccountDat(
            final String virtualAccountDiv,
            final String virtualAccountNo
        ) {
            _virtualAccountDiv = virtualAccountDiv;
            _virtualAccountNo = virtualAccountNo;
        }
    }

    private VirtualAccountDat createVirtualAccountDat(DB2UDB db2, String VBankCd, String applicantNo)
        throws SQLException, Exception {

        final String sql = sqlVirtualAccountDat(VBankCd, applicantNo);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String virtualAccountDiv = rs.getString("virtualAccountDiv");
            final String virtualAccountNo = rs.getString("virtualAccountNo");

            final VirtualAccountDat virtualBankMst = new VirtualAccountDat(virtualAccountDiv, virtualAccountNo);
            return virtualBankMst;
        }

        log.debug(">>>VIRTUAL_ACCOUNT_DAT に該当するものがありません。");
        throw new Exception();
    }

    private String sqlVirtualAccountDat(String virtualBankCd, String applicantNo) {
        return " select"
                + "    VIRTUAL_ACCOUNT_DIV as virtualAccountDiv,"
                + "    VIRTUAL_ACCOUNT_NO as virtualAccountNo"
                + " from"
                + "    VIRTUAL_ACCOUNT_DAT"
                + " where"
                + "    VIRTUAL_BANK_CD = '" + virtualBankCd + "' and"
                + "    APPLICANTNO = '" + applicantNo + "'"
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
        private final String _telno; // 電話番号

        SchregAddressDat(
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String telno
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _telno = telno;
        }

        public SchregAddressDat() {
            _zipcd = "";
            _prefCd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
            _telno = "";
        }
    }

    /**
     * 学籍住所データ取得
     * @param db2
     * @param schregno
     */
    private SchregAddressDat createStudentSchregAddressDat(DB2UDB db2, String schregno)
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
            final String telno = rs.getString("telno");

            final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
                    zipcd,
                    prefCd,
                    addr1,
                    addr2,
                    addr3,
                    telno
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
                + "    ADDR3 as addr3,"
                + "    TELNO as telno"
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
     * 生徒。学籍保護者データ。
     */
    private class GuardianDat {
        private final String _guardName;        // 保護者氏名
        private final String _guardZipcd;       // 郵便番号
        private final String _guardPrefCd;      // 都道府県コード
        private final String _guardAddr1;       // 住所1
        private final String _guardAddr2;       // 住所2
        private final String _guardAddr3;       // 住所3
        private final String _guardTelno;       // 電話番号
        private final String _guarantorName;    // 保証人氏名
        private final String _guarantorZipcd;   // 保証人郵便番号
        private final String _guarantorPrefCd;  // 保証人都道府県コード
        private final String _guarantorAddr1;   // 保証人住所1
        private final String _guarantorAddr2;   // 保証人住所2
        private final String _guarantorAddr3;   // 保証人住所3
        private final String _guarantorTelno;   // 保証人電話番号

        GuardianDat(
                final String guardName,
                final String guardZipcd,
                final String guardPrefCd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardAddr3,
                final String guardTelno,
                final String guarantorName,
                final String guarantorZipcd,
                final String guarantorPrefCd,
                final String guarantorAddr1,
                final String guarantorAddr2,
                final String guarantorAddr3,
                final String guarantorTelno 
        ) {
            _guardName = guardName;
            _guardZipcd = guardZipcd;
            _guardPrefCd = guardPrefCd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardAddr3 = guardAddr3;
            _guardTelno = guardTelno;
            _guarantorName = guarantorName;
            _guarantorZipcd = guarantorZipcd;
            _guarantorPrefCd = guarantorPrefCd;
            _guarantorAddr1 = guarantorAddr1;
            _guarantorAddr2 = guarantorAddr2;
            _guarantorAddr3 = guarantorAddr3;
            _guarantorTelno = guarantorTelno;
        }

        public GuardianDat() {
            _guardName = "";
            _guardZipcd = "";
            _guardPrefCd = "";
            _guardAddr1 = "";
            _guardAddr2 = "";
            _guardAddr3 = "";
            _guardTelno = "";
            _guarantorName = "";
            _guarantorZipcd = "";
            _guarantorPrefCd = "";
            _guarantorAddr1 = "";
            _guarantorAddr2 = "";
            _guarantorAddr3 = "";
            _guarantorTelno = "";
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
            final String guardTelno = rs.getString("guardTelno");
            final String guarantorName = rs.getString("guarantorName");
            final String guarantorZipcd = rs.getString("guarantorZipcd");
            final String guarantorPrefCd = rs.getString("guarantorPrefCd");
            final String guarantorAddr1 = rs.getString("guarantorAddr1");
            final String guarantorAddr2 = rs.getString("guarantorAddr2");
            final String guarantorAddr3 = rs.getString("guarantorAddr3");
            final String guarantorTelno = rs.getString("guarantorTelno");

            final GuardianDat guardianDat = new GuardianDat(
                    guardName,
                    guardZipcd,
                    guardPrefCd,
                    guardAddr1,
                    guardAddr2,
                    guardAddr3,
                    guardTelno,
                    guarantorName,
                    guarantorZipcd,
                    guarantorPrefCd,
                    guarantorAddr1,
                    guarantorAddr2,
                    guarantorAddr3,
                    guarantorTelno
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
                + "    GUARD_TELNO as guardTelno,"
                + "    GUARANTOR_NAME as guarantorName,"
                + "    GUARANTOR_ZIPCD as guarantorZipcd,"
                + "    GUARANTOR_PREF_CD as guarantorPrefCd,"
                + "    GUARANTOR_ADDR1 as guarantorAddr1,"
                + "    GUARANTOR_ADDR2 as guarantorAddr2,"
                + "    GUARANTOR_ADDR3 as guarantorAddr3,"
                + "    GUARANTOR_TELNO as guarantorTelno"
                + " from"
                + "    GUARDIAN_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                ;
    }

    // ======================================================================
    /**
     * 伝票データ。
     */
    private class ClaimDat {
        private final String _mannerPayment;
        private final int _cnt1;
        private final int _cnt2;
        private final boolean _plus;

        public ClaimDat() {
            _mannerPayment = "";
            _cnt1 = 0;
            _cnt2 = 0;
            _plus = false;
        }

        ClaimDat(
                final String mannerPayment,
                final int cnt1,
                final int cnt2,
                final boolean plus
        ) {
            _mannerPayment = mannerPayment;
            _cnt1 = cnt1;
            _cnt2 = cnt2;
            _plus = plus;
        }
    }

    public ClaimDat createClaimDat(DB2UDB db2, String pSlipNo)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlClaimDat(pSlipNo));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String mannerPayment = rs.getString("mannerPayment");
                final int cnt1 = Integer.parseInt(rs.getString("cnt1"));
                final int cnt2 = Integer.parseInt(rs.getString("cnt2"));
                final boolean plus = (cnt1 == cnt2) ? true : false;

                final ClaimDat claimDat = new ClaimDat(
                        mannerPayment,
                        cnt1,
                        cnt2,
                        plus
                );

                return claimDat;
            }

            return new ClaimDat();
    }

    private String sqlClaimDat(String pSlipNo) {
        return " select"
                + "     value(T1.MANNER_PAYMENT, '') as mannerPayment,"
                + "     T2.cnt1,"
                + "     T3.cnt2"
                + " from"
                + "     CLAIM_DAT T1,"
                + "     (select count(*) as cnt1"
                + "         from"
                + "             CLAIM_DETAILS_DAT"
                + "         where"
                + "             SLIP_NO = '" + pSlipNo + "') T2,"
                + "     ( select count(*) as cnt2"
                + "         from"
                + "             CLAIM_DETAILS_DAT T4, COMMODITY_MST T5"
                + "         where"
                + "             T4.SLIP_NO = '" + pSlipNo + "'"
                + "             and value(T4.DUMMY_FLG, '0') <> '1'"
                + "             and T5.COMMODITY_CD = T4.COMMODITY_CD"
                + "             and T5.TUITION_DIV = '2') T3"
                + " where"
                + "     T1.SLIP_NO = '" + pSlipNo + "'"
                ;
        }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍基礎マスタ。
     */
    private class SchregBaseMst {
        private final String _name;          // 氏名

        SchregBaseMst(final String name) {
            _name = name;
        }

        public SchregBaseMst() {
            _name = "";
        }
    }

    public SchregBaseMst createSCHREG_BASE_MST(DB2UDB db2)
        throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregBaseMst(_param._schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");

            final SchregBaseMst schregBaseMst = new SchregBaseMst(name);

            return schregBaseMst;
        }

        return new SchregBaseMst();
    }

    private String sqlSchregBaseMst(String schregNo) {
        return " select"
                + "    value(NAME, '') as name"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where"
                + "    SCHREGNO = '" + schregNo + "'"
                ;
    }
} // KNJWP102
// eof
