// kanji=漢字
/*
 * $Id: 1287fcd6e99ccd911c1c69e50c272625093c3d1a $
 *
 * 作成日: 2007/11/06 14:34:00 - JST
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
 * 合格通知書
 *
 * @author nakada
 * @version $Id: 1287fcd6e99ccd911c1c69e50c272625093c3d1a $
 */
public class KNJWP103 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP103.class);

    private static final String FORM_FILE = "KNJWP103.frm";

    /*
     * 証明書学校データ・証明書種類コード
     */
    /** 証明書種類コード */
    private static final String CERTIF_KINDCD_310 = "310";

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
     * 本文
     */
    /** 本文１ */
    private static final String TEXT_1 = "　上記の者、";
    /** 本文２ */
    private static final String TEXT_2 = "への入学選考に合格したことをここに通知いたします。";

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

                log.debug(">>志願者番号=" + _param._schregno);

                final ApplicantBaseMst applicant = createApplicant(db2, _param._applicantNo);
                printMain(applicant);

        } catch (final Exception e) {
            log.error("Exception:", e);
        }

        return _hasData;
    }

    private void printMain(final ApplicantBaseMst applicant) throws SQLException {

            printStudent(applicant);

            _form._svf.VrEndPage();
            _hasData = true;
    }

    private void printStudent(final ApplicantBaseMst applicant) throws SQLException {
        /* 送付先住所印刷 */
        _form.printShipAdd(applicant);
        /* 送付先氏名印刷 */
        _form.printShipName(applicant);
        /* 氏名漢字 */
        _form.printName(applicant);
        /* 学籍番号 */
        _form._svf.VrsOut("APPLICANTNO", _param._applicantNo);
        /* 氏名漢字 */
        _form.printName2(applicant);

        /* 本文 */
        _form.printText(applicant);

        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._date));
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME2", _param._certifSchoolDat._schoolName);
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME", _param._certifSchoolDat._principalName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME", _param._certifSchoolDat._jobName + "　");
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
        private final String _destination;          // 送り先
        private final String _applicantNo;
        private final String _date;                 // 請求日付／作成日

        private Map _prefMap;                       // 都道府県
        private CertifSchoolDat _certifSchoolDat;   // 証明書学校データ
        private School _staffSchoolMst;            // 学校名

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String schregno,
                final String destination,
                final String applicantNo,
                final String date
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
            _destination = destination;
            _applicantNo = applicantNo;
            _date = date;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ?
                    (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();
            _certifSchoolDat = createCertifSchoolDat(db2);
            _staffSchoolMst = createSchool(db2, _param._year);

            return;
        }

        /**
         * @return
         */
        private String getStaffSchoolName() {
            return null;
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
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String schregno = request.getParameter("SCHREGNO");
        final String destination = "2"; // 保護者固定とする。
        final String applicantNo = request.getParameter("APPLICANTNO");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno,
                destination,
                applicantNo,
                date
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

        public Form(final String file, final HttpServletResponse response,
                final Vrw32alp svf) throws IOException {

            _svf = svf;

            _svf.VrSetForm(file, 1);
        }

        public void printText(final ApplicantBaseMst applicant) {

          _form._svf.VrsOut("SCHOOLNAME1_1", TEXT_1
                  + _param._staffSchoolMst._schoolName1
                  + "　"
                  + applicant._major._majorName
                  + TEXT_2);
        }

        public void printName(final ApplicantBaseMst applicant) {
            String name = applicant._name;

            if (name != null) {
                final String label;
                label = "NAME1";
                _form._svf.VrsOut(label, name);
            }
        }

        public void printName2(final ApplicantBaseMst applicant) {
            String name = applicant._name;

            if (name != null) {
                final String label;
                label = "NAME2";
                _form._svf.VrsOut(label, name);
            }
        }

        /** 送付先印刷 */
        public void printShipAdd(final ApplicantBaseMst applicant) {
            if (_param._destination.equals(DESTINATION_STUDENT)) {
                prtDestinationStudent(applicant);
            } else if (_param._destination.equals(DESTINATION_PROTECT)) {
                prtDestinationProtect(applicant);
            } else {
                prtDestinationGuarant(applicant);
            }
        }

        private void prtDestinationStudent(final ApplicantBaseMst applicant) {
            if (applicant._SchregAddressDat._addr1.length() != 0) {
                printApplicantZip(applicant._SchregAddressDat._zipcd);
                printAddr1(applicant._SchregAddressDat._prefCd,
                        applicant._SchregAddressDat._addr1,
                        applicant._SchregAddressDat._addr2);
                printAddr2(applicant._SchregAddressDat._addr3);
            }
        }

        private void prtDestinationProtect(final ApplicantBaseMst applicant) {
            if (applicant._schregNo == null) {
                if (applicant._gaddr1 != null) {
                    printApplicantZip(applicant._gzipCd);
                    printAddr1(applicant._gprefCd,
                            applicant._gaddr1,
                            applicant._gaddr2);
                    printAddr2(applicant._gaddr3);
                } else {
                    printApplicantZip(applicant._zipcd);
                    printAddr1(applicant._prefCd,
                            applicant._addr1,
                            applicant._addr2);
                    printAddr2(applicant._addr3);
                }
            } else if (applicant._guardianDat._guardAddr1.length() != 0) {
                printApplicantZip(applicant._guardianDat._guardZipcd);
                printAddr1(applicant._guardianDat._guardPrefCd,
                        applicant._guardianDat._guardAddr1,
                        applicant._guardianDat._guardAddr2);
                printAddr2(applicant._guardianDat._guardAddr3);
            } else {
                prtDestinationStudent(applicant);
            }
        }

        private void prtDestinationGuarant(final ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guarantorAddr1 != null) {
                printApplicantZip(applicant._guardianDat._guarantorZipcd);
                printAddr1(applicant._guardianDat._guarantorPrefCd, applicant._guardianDat._guarantorAddr1, applicant._guardianDat._guarantorAddr2);
                printAddr2(applicant._guardianDat._guarantorAddr3);
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
            _form._svf.VrsOut("GUARD_ZIPCD", pZip);
        }

        /** 送付先住所１ */
        public void printApplicantAddr1_1(String pAddres) {
            _form._svf.VrsOut("GUARD_ADDRESS1_1", pAddres);
        }

        /** 送付先住所２ */
        public void printApplicantAddr1_2(String pAddres) {
            _form._svf.VrsOut("GUARD_ADDRESS1_2", pAddres);
        }

        /** 送付先氏名 */
        public void printApplicantName(String pName) {
            _form._svf.VrsOut("GUARD_NAME", pName);
        }

        /** 送付先氏名印刷 */
        public void printShipName(final ApplicantBaseMst applicant) {
            if (_param._destination.equals(DESTINATION_PROTECT) || _param._destination.equals(DESTINATION_STUDENT)) {
                prtDestinationProtectName(applicant);
            } else if (_param._destination.equals(DESTINATION_GUARANT)){
                prtDestinationGuarantName(applicant);
            }
        }

        private void prtDestinationProtectName(final ApplicantBaseMst applicant) {
            if (applicant._schregNo == null) {
                if (applicant._gname != null) printApplicantName(applicant._gname);
            } else if (applicant._guardianDat._guardName.length() != 0) {
                printApplicantName(applicant._guardianDat._guardName);
            }
        }

        private void prtDestinationGuarantName(final ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guarantorName != null) {
                printApplicantName(applicant._guardianDat._guarantorName);
            } else {
                prtDestinationProtectName(applicant);
            }
        }
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


         CertifSchoolDat() {
             _certifNo = "";
             _syosyoName = "";
             _schoolName = "";
             _jobName = "";
             _principalName = "";
         }

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

     private CertifSchoolDat createCertifSchoolDat(DB2UDB db2)
         throws SQLException {

         final String sql = sqlCertifSchoolDat();

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
         return new CertifSchoolDat();
     }

     private String sqlCertifSchoolDat() {
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
                 + "    CERTIF_KINDCD = '" + CERTIF_KINDCD_310 + "'"
                 ;
     }

     // ======================================================================
     /**
      * 志願者基礎マスタ。
      */
     private class ApplicantBaseMst {
         private final String _schregNo;     // 学籍番号
         private final String _courseCd;     // 課程コード
         private final String _majorCd;      // 学科コード
         private final String _courseCode;   // コースコード
         private final String _name;         // 氏名
         private final String _zipcd;        // 郵便番号
         private final String _prefCd;       // 都道府県コード
         private final String _addr1;        // 住所1
         private final String _addr2;        // 住所2
         private final String _addr3;        // 住所3
         private final String _gname;        // 保護者氏名
         private final String _gzipCd;       // 保護者郵便番号
         private final String _gprefCd;      // 保護者都道府県コード
         private final String _gaddr1;       // 保護者住所1
         private final String _gaddr2;       // 保護者住所2
         private final String _gaddr3;       // 保護者住所3
         private final String _claimSend;    // 請求先

         private Major _major;                               // 学科データ
         private CourseCodeMst _courseCodeMst;               // コースコード　データ
         private SchregAddressDat _SchregAddressDat;     // 学籍住所データ
         private GuardianDat _guardianDat;
         private SchregBaseMst _schregBaseMst;

         ApplicantBaseMst(
                 final String schregNo,
                 final String courseCd,
                 final String majorCd,
                 final String courseCode,
                 final String name,
                 final String zipcd,
                 final String prefCd,
                 final String addr1,
                 final String addr2,
                 final String addr3,
                 final String gname,
                 final String gzipCd,
                 final String gprefCd,
                 final String gaddr1,
                 final String gaddr2,
                 final String gaddr3,
                 final String claimSend
         ) {
             _schregNo = schregNo;
             _courseCd = courseCd;
             _majorCd = majorCd;
             _courseCode = courseCode;
             _name = name;
             _zipcd = zipcd;
             _prefCd = prefCd;
             _addr1 = addr1;
             _addr2 = addr2;
             _addr3 = addr3;
             _gname = gname;
             _gzipCd = gzipCd;
             _gprefCd = gprefCd;
             _gaddr1 = gaddr1;
             _gaddr2 = gaddr2;
             _gaddr3 = gaddr3;
             _claimSend = claimSend;
         }

         public void load(DB2UDB db2) throws SQLException {
             _major = createMajorDat(db2, _courseCd, _majorCd);
             _courseCodeMst = createCourseCodeDat(db2, _courseCode);
             _SchregAddressDat = createStudentSchregAddressDat(db2, _schregNo);
             _guardianDat = createGuardianDat(db2, _schregNo);
             _schregBaseMst = createSCHREG_BASE_MST(db2);
         }
     }

     public ApplicantBaseMst createApplicant(DB2UDB db2, String applicantNo)
         throws SQLException, Exception {

         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
             ps = db2.prepareStatement(sqlApplicantBaseMst(applicantNo));
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String schregNo = rs.getString("schregNo");
                 final String courseCd = rs.getString("courseCd");
                 final String majorCd = rs.getString("majorCd");
                 final String courseCode = rs.getString("courseCode");
                 final String name = rs.getString("name");
                 final String zipcd = rs.getString("zipcd");
                 final String prefCd = rs.getString("prefCd");
                 final String addr1 = rs.getString("addr1");
                 final String addr2 = rs.getString("addr2");
                 final String addr3 = rs.getString("addr3");
                 final String gname = rs.getString("gname");
                 final String gzipCd = rs.getString("gzipCd");
                 final String gprefCd = rs.getString("gprefCd");
                 final String gaddr1 = rs.getString("gaddr1");
                 final String gaddr2 = rs.getString("gaddr2");
                 final String gaddr3 = rs.getString("gaddr3");
                 final String claimSend = rs.getString("claimSend");

                 final ApplicantBaseMst applicantBaseMst = new ApplicantBaseMst(
                         schregNo,
                         courseCd,
                         majorCd,
                         courseCode,
                         name,
                         zipcd,
                         prefCd,
                         addr1,
                         addr2,
                         addr3,
                         gname,
                         gzipCd,
                         gprefCd,
                         gaddr1,
                         gaddr2,
                         gaddr3,
                         claimSend
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
         + "    COURSECD as courseCd,"
         + "    MAJORCD as majorCd,"
         + "    COURSECODE as courseCode,"
         + "    NAME as name,"
         + "    ZIPCD as zipcd,"
         + "    PREF_CD as prefCd,"
         + "    ADDR1 as addr1,"
         + "    ADDR2 as addr2,"
         + "    ADDR3 as addr3,"
         + "    GNAME as gname,"
         + "    GZIPCD as gzipCd,"
         + "    GPREF_CD as gprefCd,"
         + "    GADDR1 as gaddr1,"
         + "    GADDR2 as gaddr2,"
         + "    GADDR3 as gaddr3,"
         + "    CLAIM_SEND as claimSend"
         + " from"
         + "    APPLICANT_BASE_MST"
         + " where"
         + "    APPLICANTNO = '" + applicantNo + "'"
         ;
     }

     // ======================================================================
     /**
      * 学科データ。
      */
     private class Major {
         private final String _majorName;

         Major(
                 final String majorName
         ) {
             _majorName = majorName;
         }

         public Major() {
             _majorName = "";
         }
     }

     public Major createMajorDat(DB2UDB db2, String courseCd, String majorCd)
         throws SQLException {
             PreparedStatement ps = null;
             ResultSet rs = null;

             ps = db2.prepareStatement(sqlMajorDat(courseCd, majorCd));
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String name = rs.getString("name");

                 final Major major = new Major(
                         name
                 );
                 return major;
             }
             return new Major();
     }

     private String sqlMajorDat(String coursecd, String majorCd) {
         return " select"
                 + "    MAJORNAME as name"
                 + " from"
                 + "    MAJOR_MST"
                 + " where"
                 + "    COURSECD = '" + coursecd + "' and"
                 + "    MAJORCD = '" + majorCd + "'"
                 ;
     }

     // ======================================================================
     /**
      * コースコード　データ。
      */
     private class CourseCodeMst {
         private final String _courseCodeName;

         CourseCodeMst(
                 final String courseCodeName
         ) {
             _courseCodeName = courseCodeName;
         }

         public CourseCodeMst() {
             _courseCodeName = "";
         }
     }

     public CourseCodeMst createCourseCodeDat(DB2UDB db2, String courseCode)
         throws SQLException {
             PreparedStatement ps = null;
             ResultSet rs = null;

             ps = db2.prepareStatement(sqlCourseCodeDat(courseCode));
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String name = rs.getString("name");

                 final CourseCodeMst courseCodeMst = new CourseCodeMst(
                         name
                 );
                 return courseCodeMst;
             }
             return new CourseCodeMst();
     }

     private String sqlCourseCodeDat(String courseCode) {
         return " select"
                 + "    COURSECODENAME as name"
                 + " from"
                 + "    COURSECODE_MST"
                 + " where"
                 + "    COURSECODE = '" + courseCode + "'"
                 ;
     }

     // ======================================================================
     /**
      * 学校マスタ。
      */
     private class School {
         private final String _schoolName1; // 学校名1

         School(final String schoolName1) {
             _schoolName1 = schoolName1;
         }

         public School() {
             _schoolName1 = "";
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

             final School school = new School(schoolName1);
             return school;
         }

         return new School();
     }

     private String sqlSchool(String year) {
         return " select"
                 + "    SCHOOLNAME1 as schoolName1"
                 + " from"
                 + "    SCHOOL_MST"
                 + " where"
                 + "    YEAR = '" + year + "'";
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
} // KNJWP103

// eof
