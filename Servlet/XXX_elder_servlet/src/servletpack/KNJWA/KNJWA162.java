// kanji=漢字
/*
 * $Id: a63a6b3ba9f84827b4e70fdbb870dcb38b01a884 $
 *
 * 作成日: 2007/11/06 14:34:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

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
 * 入学許可証
 * 
 * @author nakada
 * @version $Id: a63a6b3ba9f84827b4e70fdbb870dcb38b01a884 $
 */
public class KNJWA162 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA162.class);

    private static final String FORM_FILE = "KNJWA162.frm";

    /*
     * 校長のスタッフコード
     */
    /** 校長のスタッフコード */
    private static final String STAFF_CD = "0001";

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
    private static final String TEXT_2 = "への入学を許可します。";
    
    private Form _form;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _page;

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

            for (int i = 0; i < _param._categorySelected.length; i++) {
                final String schregno = _param._categorySelected[i];
                log.debug(">>学籍番号=" + schregno);

                final List students = createStudents(db2, schregno);
                printMain(students, i);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List student, int i) throws SQLException {

        for (Iterator it = student.iterator(); it.hasNext();) {
            final Student sudent = (Student) it.next();

            printStudent(sudent, i);

            _form._svf.VrEndPage();
            _hasData = true;

        }
    }

    private void printStudent(Student student, int i) throws SQLException {
        /* 送付先住所印刷 */
        _form.printShipAdd(student);        
        /* 送付先氏名印刷 */
        _form.printShipName(student);
        /* 氏名漢字 */
        _form.printName(student);
        /* 学籍番号 */
        _form._svf.VrsOut("SCHREGNO", _param._categorySelected[i]);
        /* 氏名漢字 */
        _form.printName2(student);

        /* 本文 */
        _form.printText(student);

        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(student._entDate));
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
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String _grade;
        private final String[] _categorySelected;   // 学籍番号
        private final String _destination;          // 送り先

        private Map _prefMap;                       // 都道府県
        private CertifSchoolDat _certifSchoolDat;   // 証明書学校データ

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String grade,
                final String[] categorySelected,
                final String destination
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _grade = grade;
            _categorySelected = categorySelected;
            _destination = destination;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ?
                    (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();
            _certifSchoolDat = createCertifSchoolDat(db2);

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
        final String programId = request.getParameter("PROGRAMID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));
        final String grade = request.getParameter("GRADE");
        final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        final String destination = "2"; // 保護者固定とする。

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                grade,
                categorySelected,
                destination
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

        public Form(final String file, final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(file, 1);
        }

        public void printText(Student student) {
            final String kamokuRishu ;
            if ("35080006001".equals(student._school._schoolCd) &&
                    "05".equals(student._schregRegdDat._studentDiv)) {
                kamokuRishu = "科目履修";
            } else {
                kamokuRishu = "";
            }
            
            _form._svf.VrsOut("SCHOOLNAME1_1", TEXT_1
                  + student._school._schoolName1
                  + "　"
                  + student._major._majorName
                  + kamokuRishu
                  + TEXT_2);
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

        public void printName(Student student) {
            String name = student._name;
            
            if (name != null) {
                final String label;
                label = "NAME1";
                _form._svf.VrsOut(label, name);
            }
        }

        public void printName2(Student student) {
            String name = student._name;
            
            if (name != null) {
                final String label;
                label = "NAME2";
                _form._svf.VrsOut(label, name);
            }
        }

        /** 送付先印刷 */
        public void printShipAdd(Student student) {
            if (_param._destination.equals(DESTINATION_STUDENT)) {
                prtDestinationStudent(student);
            } else if (_param._destination.equals(DESTINATION_PROTECT)) {
                prtDestinationProtect(student);
            } else {
                prtDestinationGuarant(student);
            }
        }

        private void prtDestinationStudent(Student student) {
            if (student._schregAddressDat._addr1.length() != 0) {
                printApplicantZip(student._schregAddressDat._zipcd);
                printAddr1(student._schregAddressDat._prefCd, student._schregAddressDat._addr1, student._schregAddressDat._addr2);
                printAddr2(student._schregAddressDat._addr3);
            }
        }

        private void prtDestinationProtect(Student student) {
            if (student._guardianDat._guardAddr1 != null && student._guardianDat._guardAddr1.length() != 0) {
                printApplicantZip(student._guardianDat._guardZipcd);
                printAddr1(student._guardianDat._guardPrefCd, student._guardianDat._guardAddr1, student._guardianDat._guardAddr2);
                printAddr2(student._guardianDat._guardAddr3);
            } else {
                prtDestinationStudent(student);
            }
        }

        private void prtDestinationGuarant(Student student) {
            if (student._guardianDat._guarantorAddr1 != null) {
                printApplicantZip(student._guardianDat._guarantorZipcd);
                printAddr1(student._guardianDat._guarantorPrefCd, student._guardianDat._guarantorAddr1, student._guardianDat._guarantorAddr2);
                printAddr2(student._guardianDat._guarantorAddr3);
            } else {
                prtDestinationProtect(student);
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
        public void printShipName(Student student) {
            if (_param._destination.equals(DESTINATION_PROTECT) || _param._destination.equals(DESTINATION_STUDENT)) {
                prtDestinationProtectName(student);
            } else if (_param._destination.equals(DESTINATION_GUARANT)){
                prtDestinationGuarantName(student);
            }
        }

        private void prtDestinationProtectName(Student student) {
            if (student._guardianDat._guardName != null && student._guardianDat._guardName.length() != 0) {
                printApplicantName(student._guardianDat._guardName);
            }
        }

        private void prtDestinationGuarantName(Student student) {
            if (student._guardianDat._guarantorName != null) {
                printApplicantName(student._guardianDat._guarantorName);
            } else {
                prtDestinationProtectName(student);
            }
        }
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student {
        private final String _schregNo;         // 学籍番号
        private final String _name;             // 氏名
        private final String _entDate;          //入学日

        private SchregRegdDat _schregRegdDat;    // 生徒。学籍在籍データ
        private Major _major;                    // 学科データ
        private GuardianDat _guardianDat;        // 生徒。学籍保護者データ
        private School _school;                  // 生徒。学校マスタ
        private SchregAddressDat _schregAddressDat; // 生徒。学籍住所データ

        Student(final String schregNo,
                final String name,
                final String entDate
        ) {
            _schregNo = schregNo;
            _name = name;
            _entDate = entDate;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregRegdDat = createSourseCodeDat(db2, _param._year, _param._semester, _schregNo);
            _major = createMajorDat(db2, _schregRegdDat._courseCd, _schregRegdDat._majorCd);
            _guardianDat = createGuardianDat(db2, _schregNo);
            _school = createSchool(db2, _param._year);
            _schregAddressDat = createSchregAddressDat(db2, _schregNo);
        }
    }

    private List createStudents(final DB2UDB db2, String schregno)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String entDate = rs.getString("ENT_DATE");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        entDate
                );

                studentDat.load(db2);
                rtn.add(studentDat);
            }

            if (rtn.isEmpty()) {
                log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
                throw new Exception();
            } else {
                return rtn;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    ENT_DATE"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
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
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _grade;         // 課程コード
        private final String _courseCd;      // 課程コード
        private final String _majorCd;       // 学科コード
        private final String _studentDiv;    // 学生区分

        SchregRegdDat(
                final String grade,
                final String courseCd,
                final String majorCd,
                final String studentDiv
        ) {
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _studentDiv = studentDiv;
        }

        SchregRegdDat() {
            _grade = "";
            _courseCd = "";
            _majorCd = "";
            _studentDiv = "";
        }
    }

    public SchregRegdDat createSourseCodeDat(DB2UDB db2, String YEAR, String SEMESTER, String SCHREGNO)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String grade = rs.getString("grade");
            final String courseCd = rs.getString("courseCd");
            final String majorCd = rs.getString("majorCd");
            final String studentDiv = rs.getString("studentDiv");

            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    grade,
                    courseCd,
                    majorCd,
                    studentDiv
            );
            return schregRegdDat;
        }

        return new SchregRegdDat();
    }

    private String sqlSchregRegdDat(String year, String semester, String schregNo) {
        return " select"
                + "    GRADE as grade,"
                + "    COURSECD as courseCd,"
                + "    MAJORCD as majorCd,"
                + "    STUDENT_DIV as studentDiv"
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
     * 生徒。学籍保護者データ。
     */
    private class GuardianDat {
        private final String _guardName;        // 保護者氏名
        private final String _guardKana;        // 保護者氏名かな
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

        GuardianDat(
                final String guardName,
                final String guardKana,
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
                final String guarantorAddr3 
        ) {
            _guardName = guardName;
            _guardKana = guardKana;
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
        }

        public GuardianDat() {
            _guardName = "";
            _guardKana = "";
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
            final String guardKana = rs.getString("guardKana");
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

            final GuardianDat guardianDat = new GuardianDat(
                    guardName,
                    guardKana,
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
                    guarantorAddr3
            );
            return guardianDat;
        }                    
        return new GuardianDat();
    }

    private String sqlGuardianDat(String schregno) {
        return " select"
                + "    GUARD_NAME as guardName,"
                + "    GUARD_KANA as guardKana,"
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
                + "    GUARANTOR_ADDR3 as guarantorAddr3"
                + " from"
                + "    GUARDIAN_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                ;
    }

    // ======================================================================
    /**
     * 学校マスタ。
     */
    private class School {
        private final String _schoolName1; // 学校名1
        private final String _schoolCd;    // 学校コード

        School(final String schoolName1,
                final String schoolCd) {
            _schoolName1 = schoolName1;
            _schoolCd = schoolCd;
        }

        public School() {
            _schoolName1 = "";
            _schoolCd = "";
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
            final String schoolCd = rs.getString("schoolCd");

            final School school = new School(schoolName1, schoolCd);
            return school;
        }

        return new School();
    }

    private String sqlSchool(String year) {
        return " select"
                + "    SCHOOLNAME1 as schoolName1,"
                + "    SCHOOLCD as schoolCd"
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
} // KNJWA162

// eof
