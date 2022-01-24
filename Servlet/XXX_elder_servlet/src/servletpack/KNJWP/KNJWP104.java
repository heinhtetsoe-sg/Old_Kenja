// kanji=漢字
/*
 * $Id: 13e71efaabe6250f57582f778072bf443904a638 $
 *
 * 作成日: 2007/11/08 14:39:00 - JST
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
 * 受験結果報告書（通知）
 * 
 * @author nakada
 * @version $Id: 13e71efaabe6250f57582f778072bf443904a638 $
 */
public class KNJWP104 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP104.class);

    private static final String FORM_FILE = "KNJWP104.frm";

    /*
     * 発行番号印刷不可
     */
    /** 発行番号印刷不可 */
    private static final String CERTIF_NO_ON = "0";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 志願者氏名 */
    private static final int NAME_LENG = 20;

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 合格通知書（学校）印刷 */
    private static final String OUTPUT4_PRINT_ON = "1";

    /** 受験区分 */
    private static final String APPLICANT_DIV = "W005";

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
        _hasData = false;
        dumpParam(request);
        _param = createParam(request);

        _svf = svf;
        _form = new Form(FORM_FILE, response, _svf);
        
        db2 = pDb2;

        try {

            _param.load(db2);
            log.debug(">>志願者番号=" + _param._applicantNo);

            final ApplicantBaseMst applicant = createApplicants(db2, _param._applicantNo);
            printMain(applicant);

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printMain(final ApplicantBaseMst applicant) throws SQLException {
            printApplicant(applicant);
            _form._svf.VrEndPage();
            _hasData = true;
    }

    private void printApplicant(ApplicantBaseMst applicant) throws SQLException {
        /* 証明書発行番号 */
        _form._svf.VrsOut("BONDNAME", _param._certifIssueDat._certifSchoolDat._syosyoName);
        _form.printCertifIndex();

        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._date));
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME", _param._certifIssueDat._certifSchoolDat._schoolName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME", _param._certifIssueDat._certifSchoolDat._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME", _param._certifIssueDat._certifSchoolDat._principalName);
        /* 学校宛先・郵便番号 */
        _form._svf.VrsOut("ZIPCD", applicant._finJHighSchool._zipcd);
        /* 学校宛先・住所１ */
        _form.printAddr1(applicant);
        /* 学校宛先・住所２ */
        _form.printAddr2(applicant);
        /* 学校宛先・学校名 */
        _form._svf.VrsOut("FIN_SCHOOL", applicant._finJHighSchool._name);
        /* 志願者氏名 */
        _form.printName(applicant);
        /* 貴校卒業日 */
        _form._svf.VrsOut("FD_GRDDATE", getJDate(applicant._fsGrddate));
        /* 入学形態 */
        _form._svf.VrsOut("IN_DIV", _param._applicantString(applicant._applicantDiv));
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
        private final String _date;                 // 請求日付／作成日
        private final String _certif;
        private final String _schregno;

        private Map _prefMap;           // 都道府県
        private Map _applicantMap;      // 受験区分
        private CertifIssueDat _certifIssueDat;     // 本証書・証明書情報

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String applicantNo,
                final String date,
                final String certif,
                final String schregno
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _date = date;
            _certif = certif;
            _schregno = schregno;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _prefMap = getPrefMst();
            _applicantMap = getNameMst(APPLICANT_DIV);
            _certifIssueDat = createCertifIssueDat(db2, _param._certif);
            
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

        public String _applicantString(String inDiv) {
            return (String) _applicantMap.get(inDiv);
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PROGRAMID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));
        final String applicantNo = request.getParameter("APPLICANTNO");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        final String certif = request.getParameter("GOUKAKUG1_VAL");
        final String schregno = request.getParameter("SCHREGNO");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                date,
                certif,
                schregno
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
            _svf.VrSetForm(file, 1);
        }

        public void printName(ApplicantBaseMst applicant) {
            String name = "";

            if (applicant._schregBaseMst._name.length() == 0) {
                name = applicant._name;
            } else {
                name = applicant._schregBaseMst._name;
            }

            if (name != null) {
                final String label;
                if (name.length() <= NAME_LENG) {
                    label = "APPLICANT_NAME1";
                } else {
                    label = "APPLICANT_NAME2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printAddr1(ApplicantBaseMst applicant) {
            String name = _param._prefMapString(applicant._finJHighSchool._prefCd)
                + (applicant._finJHighSchool._addr1 != null ? applicant._finJHighSchool._addr1 : "")
                + (applicant._finJHighSchool._addr2 != null ? applicant._finJHighSchool._addr2 : "");
            _form._svf.VrsOut("ADDR1", name);
        }

        public void printAddr2(ApplicantBaseMst applicant) {
            String name = applicant._finJHighSchool._addr3 != null ? applicant._finJHighSchool._addr3 : "";
            _form._svf.VrsOut("ADDR2", name);
        }

        void printCertifIndex() {
            if (_param._certifIssueDat._certifSchoolDat._certifNo.equals(CERTIF_NO_ON)) {
                _svf.VrsOut("CERTIF_NO", _param._certifIssueDat._certifNo);
            } else {
                _svf.VrsOut("CERTIF_NO", "     ");
            }
        }
    }

    // ======================================================================
    /**
     * 志願者基礎マスタ。
     */
    private class ApplicantBaseMst {
        private final String _applicantDiv;     // 受験区分
        private final String _name;             // 氏名
        private final String _fsCd;             // 出身中学校コード
        private final String _fsGrddate;        // 卒業年月日

        private FinJuniorHighSchoolMst _finJHighSchool; // 出身中学校マスタ
        private SchregBaseMst _schregBaseMst;

        ApplicantBaseMst(
                final String applicantDiv,
                final String name,
                final String fsCd,
                final String fsGrddate
        ) {
            _applicantDiv = applicantDiv;
            _name = name;
            _fsCd = fsCd;
            _fsGrddate = fsGrddate;
        }

        public void load(DB2UDB db2) throws SQLException {
            _finJHighSchool = createFinJuniorHighSchoolMst(db2, _fsCd);
            _schregBaseMst = createSCHREG_BASE_MST(db2);
        }
    }

    public ApplicantBaseMst createApplicants(DB2UDB db2, String applicantNo)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlApplicantBaseMst(applicantNo));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String applicantDiv = rs.getString("applicantDiv");
                final String name = rs.getString("name");
                final String fsCd = rs.getString("fsCd");
                final String fsGrddate = rs.getString("fsGrddate");

                final ApplicantBaseMst applicantBaseMst = new ApplicantBaseMst(
                        applicantDiv,
                        name,
                        fsCd,
                        fsGrddate
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
        + "    APPLICANT_DIV as applicantDiv,"
        + "    NAME as name,"
        + "    FS_CD as fsCd,"
        + "    FS_GRDDATE as fsGrddate"
        + " from"
        + "    APPLICANT_BASE_MST"
        + " where"
        + "    APPLICANTNO = '" + applicantNo + "'"
        ;
    }

    // ======================================================================
    /**
     * 出身中学マスタ
     */
    private class FinJuniorHighSchoolMst {
        private final String _name;     // 学校名1
        private final String _zipcd;    // 郵便番号
        private final String _addr1;     // 住所1
        private final String _addr2;     // 住所2
        private final String _addr3;     // 住所3
        private final String _prefCd;   // jis都道府県

        FinJuniorHighSchoolMst(
                final String name,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String prefCd
        ) {
            _name = name;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _prefCd = prefCd;
        }

        public FinJuniorHighSchoolMst() {
            _name = "";
            _zipcd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
            _prefCd = "";
        }
    }

    private FinJuniorHighSchoolMst createFinJuniorHighSchoolMst(DB2UDB db2, String schoolCd)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlFinJuniorHighSchoolMst(schoolCd));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");
            final String zipcd = rs.getString("zipcd");
            final String addr1 = rs.getString("addr1");
            final String addr2 = rs.getString("addr2");
            final String addr3 = rs.getString("addr3");
            final String prefCd = rs.getString("prefCd");

            final FinJuniorHighSchoolMst finJuniorHighSchoolMst = new FinJuniorHighSchoolMst(
                    name,
                    zipcd,
                    addr1,
                    addr2,
                    addr3,
                    prefCd
            );
            return finJuniorHighSchoolMst;
        }                    
        return new FinJuniorHighSchoolMst();
    }

    private String sqlFinJuniorHighSchoolMst(String schoolCd) {
        return " select"
                + "    NAME as name,"
                + "    ZIPCD as zipcd,"
                + "    ADDR1 as addr1,"
                + "    ADDR2 as addr2,"
                + "    ADDR3 as addr3,"
                + "    PREF_CD as prefCd"
                + " from"
                + "    FIN_JUNIOR_HIGHSCHOOL_MST"
                + " where"
                + "    SCHOOL_CD = '" + schoolCd + "'"
                ;
    }

//  ======================================================================
 /**
  * 証明書発行データ。
  */
     class CertifIssueDat {
         private final String _schregNo;     // 学籍番号
         private final String _type;         // 証明書種別
         private final String _certifKindCd; // 証明書種類コード
         private final String _issueDate;    // 証明書発行日付
         private final String _certifNo;     // 証明書番号

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

         public void load(DB2UDB db2, String certifIndex) throws SQLException, Exception {
             _certifSchoolDat = createCertifSchoolDat(db2, _certifKindCd);
         }
     }

     private CertifIssueDat createCertifIssueDat(DB2UDB db2, String pCertifIndex) throws SQLException, Exception {
         final String sql = sqlCertifIssueDat(pCertifIndex);

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
             certifIssueDat.load(db2, pCertifIndex);
             return certifIssueDat;
         }

         log.debug(">>>CERTIF_ISSUE_DAT に該当するものがありません。");
         throw new Exception();
     }

     private String sqlCertifIssueDat(String certifIndex) {
         return " select"
                 + "    SCHREGNO as schregNo,"
                 + "    TYPE as type,"
                 + "    CERTIF_KINDCD as certifKindCd,"
                 + "    ISSUEDATE as issueDate,"
                 + "    CERTIF_NO as certifNo"
                 + " from"
                 + "    CERTIF_ISSUE_DAT"
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

     private CertifSchoolDat createCertifSchoolDat(DB2UDB db2, String pCertifKindCd)
         throws SQLException, Exception {

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
} // KNJWP104

// eof
