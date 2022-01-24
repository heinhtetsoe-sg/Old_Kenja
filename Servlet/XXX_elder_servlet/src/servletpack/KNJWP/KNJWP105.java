// kanji=漢字
/*
 * $Id: c2b74360a4ef0e752f1844bf7de5b48c05029039 $
 *
 * 作成日: 2007/11/08 17:22:00 - JST
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
 * 転学照会に対する回答の件
 * 
 * @author nakada
 * @version $Id: c2b74360a4ef0e752f1844bf7de5b48c05029039 $
 */
public class KNJWP105 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP105.class);

    private static final String FORM_FILE = "KNJWP105.frm";

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 転学回答書（個人）印刷 */
    private static final String OUTPUT5_PRINT_ON = "1";

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    private static final String PRINT_ON = "1";
    private static final String PRINT_TITLE1 = "受領書";
    private static final String PRINT_TITLE2 = "入学検定料";
    private static final String PRINT_TITLE3 = "登録料";

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
     * 本文テキスト
     */
    /** 本文テキスト */
    private static final String TEXT = "より照会のありました転入受入れに関し、"
        + "本校への転入学を許可致します。";

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

            final ApplicantBaseMst applicant = createApplicant(db2, _param._applicantNo);
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
        /* 送付先住所印刷 */
        _form.printShipAdd(applicant);        
        /* 送付先氏名印刷 */
        _form.printShipName(applicant);
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._date));
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME1", _param._certifIssueDat._certifSchoolDat._schoolName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME", _param._certifIssueDat._certifSchoolDat._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME", _param._certifIssueDat._certifSchoolDat._principalName);
        /* 前籍高校名 ＋本文*/
        _form.printAnotherSchool(applicant);
        /* 生徒氏名印刷 */
        _form.printName2(applicant);
        /* 生年月日 */
        _form._svf.VrsOut("BIRTHDAY", getJDate(applicant._birthday) + "生");
        /* 転入学希望日 */
        _form._svf.VrsOut("ENT_SCHDULE_DATE", getJDate(applicant._entScheduleDate));
        /* 学校宛先・郵便番号 */
        _form._svf.VrsOut("SCHOOLZIPCD", _param._staffSchoolMst._schoolZipCd);
        /* 学校宛先・住所１ */
        _form.printAddr2_1();
        /* 学校宛先・住所２ */
        _form.printAddr2_2();
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME2", _param._staffSchoolMst._schoolname1);
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
        private final String _checkKaitouK1;        // 1:転学回答書（個人）印刷
        private final String _select;               // 送り先
        private final String _schregno;
        private final String _certif;

        private Map _prefMap;                       // 都道府県
        private School _staffSchoolMst;             // 学校住所
        private CertifIssueDat _certifIssueDat;     // 本証書・証明書情報
        
        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String applicantNo,
                final String date,
                final String checkKaitouK1,
                final String select,
                final String schregno,
                final String certif
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _date = date;
            _checkKaitouK1 = checkKaitouK1;
            _select = select;
            _schregno = schregno;
            _certif = certif;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ?
                    (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _prefMap = getPrefMst();
            _certifIssueDat = createCertifIssueDat(db2, _param._certif);
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
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        final String checkKaitouK1 = request.getParameter("CHECK_KAITOU_K1");
        final String select = request.getParameter("SEND");
        final String schregno = request.getParameter("SCHREGNO");
        final String certif = request.getParameter("KAITOUG1_VAL");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                date,
                checkKaitouK1,
                select,
                schregno,
                certif
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

        public void printName2(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._name.length() == 0) {
//                _form._svf.VrsOut("APPLICANT_NAME1", applicant._name);
                _form._svf.VrsOut("APPLICANT_NAME2", applicant._name);
            } else {
//                _form._svf.VrsOut("APPLICANT_NAME1", applicant._schregBaseMst._name);
                _form._svf.VrsOut("APPLICANT_NAME2", applicant._schregBaseMst._name);
            }
        }

        public void printAddr2_1() {
            String name = (_param._staffSchoolMst._schoolAddr1 != null ?
                    _param._staffSchoolMst._schoolAddr1 : "")
                + (_param._staffSchoolMst._schoolAddr2 != null ?
                    _param._staffSchoolMst._schoolAddr2 : "");
            _form._svf.VrsOut("SCHOOLADDRESS1", name);
        }

        public void printAddr2_2() {
            String name = _param._staffSchoolMst._schoolAddr3 != null ?
                    _param._staffSchoolMst._schoolAddr3 : "";
            _form._svf.VrsOut("SCHOOLADDRESS2", name);
        }

        public void printAnotherSchool(ApplicantBaseMst applicant) {
            String name = "　" + applicant._anotherSchoolHistDat._anoName + TEXT;

            if (name != null) {
                _form._svf.VrsOut("TEXT1_1", name);
            }
        }

        /** 送付先印刷 */
        public void printShipAdd(ApplicantBaseMst applicant) {
            if (_param._schregno.length() == 0) {
                printShipAddApplicant(applicant);
            } else {
                prtDestinationProtect(applicant);
            }
        }

        public void printShipAddApplicant(ApplicantBaseMst applicant) {
            prtDestinationApplicantProtect(applicant);
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
            if (applicant._SchregAddressDat._addr1.length() != 0) {
                printApplicantZip(applicant._SchregAddressDat._zipcd);
                printAddr1(applicant._SchregAddressDat._prefCd, applicant._SchregAddressDat._addr1, applicant._SchregAddressDat._addr2);
                printAddr2(applicant._SchregAddressDat._addr3);
            } else {
                printShipAddApplicant(applicant);
            }
        }

        private void prtDestinationProtect(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guardAddr1.length() != 0) {
                printApplicantZip(applicant._guardianDat._guardZipcd);
                printAddr1(applicant._guardianDat._guardPrefCd, applicant._guardianDat._guardAddr1, applicant._guardianDat._guardAddr2);
                printAddr2(applicant._guardianDat._guardAddr3);
            } else {
                prtDestinationStudent(applicant);
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
            _form._svf.VrsOut("ZIPCD", pZip);
        }

        /** 送付先住所１ */
        public void printApplicantAddr1_1(String pAddres) {
            _form._svf.VrsOut("ADDRESS1_1", pAddres);
        }

        /** 送付先住所２ */
        public void printApplicantAddr1_2(String pAddres) {
            _form._svf.VrsOut("ADDRESS1_2", pAddres);
        }
        /** 送付先氏名印刷 */
        public void printShipName(ApplicantBaseMst applicant) {
            if (!valueCheck(_param._schregno)) {
                printShipNameApplicant(applicant);
            } else {
                prtDestinationProtectName(applicant);
            }
        }

        public void printShipNameApplicant(ApplicantBaseMst applicant) {
            prtDestinationApplicantProtectName(applicant);
        }

        private void prtDestinationApplicantName(ApplicantBaseMst applicant) {
            printApplicantName(applicant._name);
        }

        private void prtDestinationApplicantProtectName(ApplicantBaseMst applicant) {
            if (applicant._gname != null) {
                printApplicantName(applicant._gname);
            } else {
                prtDestinationApplicantName(applicant);
            }
        }

        private void prtDestinationName(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._name.length() != 0) {
                printApplicantName(applicant._schregBaseMst._name);
            } else {
                printShipNameApplicant(applicant);
            }
        }

        private void prtDestinationProtectName(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guardName.length() != 0) {
                printApplicantName(applicant._guardianDat._guardName);
            } else {
                prtDestinationName(applicant);
            }
        }

        /** 送付先氏名 */
        public void printApplicantName(String pName) {
            _form._svf.VrsOut("APPLICANT_NAME1", pName);
        }

        private boolean valueCheck(final String terget) {
            boolean rtn = false;
            if ((terget != null) && (!terget.equals("")) && (terget.length() != 0)) {
                rtn = true;
            }

            return rtn;
        }
    }

    // ======================================================================
    /**
     * 志願者基礎マスタ。
     */
    private class ApplicantBaseMst {
        private final String _schregNo;     // 学籍番号
        private final String _entScheduleDate;  // 入学予定日
        private final String _name;             // 氏名
        private final String _birthday;         // 生年月日
        private final String _zipCd;            // 郵便番号
        private final String _prefCd;           // 都道府県コード
        private final String _addr1;            // 住所1
        private final String _addr2;            // 住所2
        private final String _addr3;            // 住所3
        private final String _gname;        // 保護者氏名
        private final String _gzipCd;       // 保護者郵便番号
        private final String _gprefCd;      // 保護者都道府県コード
        private final String _gaddr1;       // 保護者住所1
        private final String _gaddr2;       // 保護者住所2
        private final String _gaddr3;       // 保護者住所3

        private AnotherSchoolHistDat _anotherSchoolHistDat;
        private SchregAddressDat _SchregAddressDat;
        private GuardianDat _guardianDat;
        private SchregBaseMst _schregBaseMst;

        ApplicantBaseMst(
                final String schregNo,
                final String entScheduleDate,
                final String name,
                final String birthday,
                final String zipCd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String gname,
                final String gzipCd,
                final String gprefCd,
                final String gaddr1,
                final String gaddr2,
                final String gaddr3
        ) {
            _schregNo = schregNo;
            _entScheduleDate = entScheduleDate;
            _name = name;
            _birthday = birthday;
            _zipCd = zipCd;
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
        }

        public void load(DB2UDB db2, String applicantNo) throws SQLException {
            _anotherSchoolHistDat = createStudentAnotherSchoolHistDat(db2, applicantNo);
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
                final String entScheduleDate = rs.getString("entScheduleDate");
                final String name = rs.getString("name");
                final String birthday = rs.getString("birthday");
                final String zipCd = rs.getString("zipCd");
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

                final ApplicantBaseMst applicantDat = new ApplicantBaseMst(
                        schregNo,
                        entScheduleDate,
                        name,
                        birthday,
                        zipCd,
                        prefCd,
                        addr1,
                        addr2,
                        addr3,
                        gname,
                        gzipCd,
                        gprefCd,
                        gaddr1,
                        gaddr2,
                        gaddr3
                );

                applicantDat.load(db2, applicantNo);
                return applicantDat;
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
        + "    ENT_SCHEDULE_DATE as entScheduleDate,"
        + "    NAME as name,"
        + "    BIRTHDAY as birthday,"
        + "    ZIPCD as zipCd,"
        + "    PREF_CD as prefCd,"
        + "    ADDR1 as addr1,"
        + "    ADDR2 as addr2,"
        + "    ADDR3 as addr3,"
        + "    GNAME as gname,"
        + "    GZIPCD as gzipCd,"
        + "    GPREF_CD as gprefCd,"
        + "    GADDR1 as gaddr1,"
        + "    GADDR2 as gaddr2,"
        + "    GADDR3 as gaddr3"
        + " from"
        + "    APPLICANT_BASE_MST"
        + " where"
        + "    APPLICANTNO = '" + applicantNo + "'"
        ;
    }

    // ======================================================================
    /**
     * 学校マスタ。
     */
    private class School {
        private final String _schoolZipCd; // 学校郵便番号
        private final String _schoolAddr1; // 学校住所1
        private final String _schoolAddr2; // 学校住所2
        private final String _schoolAddr3; // 学校住所3
        private final String _schoolname1; // 学校名1

        School(
            final String schoolZipCd,
            final String schoolAddr1,
            final String schoolAddr2,
            final String schoolAddr3,
            final String schoolname1
        ) {
            _schoolZipCd = schoolZipCd;
            _schoolAddr1 = schoolAddr1;
            _schoolAddr2 = schoolAddr2;
            _schoolAddr3 = schoolAddr3;
            _schoolname1 = schoolname1;
        }

        public School() {
            _schoolZipCd = "";
            _schoolAddr1 = "";
            _schoolAddr2 = "";
            _schoolAddr3 = "";
            _schoolname1 = "";
        }
    }

    private School createSchool(DB2UDB db2, String year) throws SQLException {
        final String sql = sqlSchool(year);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String schoolZipCd = rs.getString("schoolZipCd");
            final String schoolAddr1 = rs.getString("schoolAddr1");
            final String schoolAddr2 = rs.getString("schoolAddr2");
            final String schoolAddr3 = rs.getString("schoolAddr3");
            final String schoolname1 = rs.getString("schoolname1");

            final School school = new School(
                schoolZipCd,
                schoolAddr1,
                schoolAddr2,
                schoolAddr3,
                schoolname1
            );
            return school;
        }

        return new School();
    }

    private String sqlSchool(String year) {
        return " select"
                + "    SCHOOLZIPCD as schoolZipCd,"
                + "    SCHOOLADDR1 as schoolAddr1,"
                + "    SCHOOLADDR2 as schoolAddr2,"
                + "    SCHOOLADDR3 as schoolAddr3,"
                + "    SCHOOLNAME1 as schoolname1"
                + " from"
                + "    SCHOOL_MST"
                + " where"
                + "    YEAR = '" + year + "'";
    }

    // ======================================================================
    /**
     * 生徒。前籍校履歴データ。
     */
    private class AnotherSchoolHistDat {
        private final String _anoName; // 漢字学校名

        AnotherSchoolHistDat(
                final String anoName
        ) {
            _anoName = anoName;
        }

        public AnotherSchoolHistDat() {
            _anoName = "";
        }
    }

    /**
     * 前籍校履歴データ取得
     * @param db2
     * @param applicantNo
     */
    private AnotherSchoolHistDat createStudentAnotherSchoolHistDat(DB2UDB db2, String applicantNo)
        throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlAnotherSchoolHistDat(applicantNo));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String anoName = rs.getString("anoName");
            
            final AnotherSchoolHistDat studentAnotherSchoolHistDat = new AnotherSchoolHistDat(
                    anoName
            );
            return studentAnotherSchoolHistDat;
        }                    
        return new AnotherSchoolHistDat();
    }

    private String sqlAnotherSchoolHistDat(String applicantNo) {
        return " select"
                + "    T2.NAME as anoName"
                + " from"
                + "    ANOTHER_SCHOOL_HIST_DAT T1 left join FIN_HIGH_SCHOOL_MST T2 on T1.FORMER_REG_SCHOOLCD = T2.SCHOOL_CD"
                + " where"
                + "    T1.APPLICANTNO = '" + applicantNo + "'"
                + " order by T1.REGD_S_DATE DESC";
    }

    // ======================================================================
    /**
     * 職名マスタ。
     */
    private class JobMst {
        private final String _jobName; // 職名

        JobMst(final String jobName) {
            _jobName = jobName;
        }

        public JobMst() {
            _jobName = "";
        }
    }

    private JobMst createJobMst(DB2UDB db2, String jobCd) throws SQLException {
        final String sql = sqlJobMst(jobCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String jobName = rs.getString("jobName");

            final JobMst jobMst = new JobMst(jobName);
            return jobMst;
        }

        return new JobMst();
    }

    private String sqlJobMst(String jobCd) {
        return " select"
                + "    JOBNAME as  jobName"
                + " from"
                + "    JOB_MST"
                + " where"
                + "    JOBCD = '" + jobCd + "'"
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
                + "    value(GUARD_NAME, '') as guardName,"
                + "    GUARD_ZIPCD as guardZipcd,"
                + "    GUARD_PREF_CD as guardPrefCd,"
                + "    value(GUARD_ADDR1, '') as guardAddr1,"
                + "    GUARD_ADDR2 as guardAddr2,"
                + "    GUARD_ADDR3 as guardAddr3,"
                + "    value(GUARANTOR_NAME, '') as guarantorName,"
                + "    GUARANTOR_ZIPCD as guarantorZipcd,"
                + "    GUARANTOR_PREF_CD as guarantorPrefCd,"
                + "    value(GUARANTOR_ADDR1, '') as guarantorAddr1,"
                + "    GUARANTOR_ADDR2 as guarantorAddr2,"
                + "    GUARANTOR_ADDR3 as guarantorAddr3"
                + " from"
                + "    GUARDIAN_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
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

         private CertifSchoolDat _certifSchoolDat;
         
         CertifIssueDat(
                 final String schregNo,
                 final String type,
                 final String certifKindCd,
                 final String issueDate
         ) {
             _schregNo = schregNo;
             _type = type;
             _certifKindCd = certifKindCd;
             _issueDate = issueDate;
         }

         public void load(DB2UDB db2) throws SQLException, Exception {
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

             final CertifIssueDat certifIssueDat = new CertifIssueDat(
                     schregNo,
                     type,
                     certifKindCd,
                     issueDate
             );
             certifIssueDat.load(db2);
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
                 + "    ISSUEDATE as issueDate"
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
} // KNJWP105

// eof
