// kanji=漢字
/*
 * $Id: 07e005c92e4089fd2d15ccb578d673eae2ed40eb $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
 * 通学証明書
 * @author nakada
 * @version $Id: 07e005c92e4089fd2d15ccb578d673eae2ed40eb $
 */
public class KNJWA172 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA172.class);

    private static final String FORM_FILE = "KNJWA172.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 名前 */
    private static final int NAME1_LENG = 34;
    /** 住所１ */
    private static final int ADD1_LENG = 50;

    /* 
     * 課程名と学科名の間の固定文字 
     */
    private static final String COURSE_HOKAN_NAME = "課程";
    
    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 通学証明書印刷 */
    private static final String OUTPUT2_PRINT_ON = "1";

    /*
     * 性別
     */
    /** 性別 */
    private static final String SEX_MAN = "1";
    private static final String SEX_WOMAN = "2";
    private static final String SEX_MISSING = "3";

    /*
     * 有効期間
     */
    /** 有効期間 */
    private static final int VALIDITY_1 = 1;

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

            for (int i = 0; i < _param._schregno.length; i++) {
                final String schregno = _param._schregno[i];
                log.debug(">>学籍番号=" + schregno);

                final Student student = createStudent(db2, schregno, i);
                printMain(student, i);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printMain(final Student student, int i) throws SQLException {
            printApplicant(student, i);
            _form._svf.VrEndPage();
            _hasData = true;
    }

    private void printApplicant(Student student, int i) throws SQLException {
        /* № */
        _form._svf.VrsOut("CERTIF_NO1", student._certifIssueDat._certifNo);

        /* 氏名 */
        _form.printName(student);
        /* 年齢 */
        _form.printAge(_param._loginDate, student._birthday);
        /* 性別 */
        if (student._sex.equals(SEX_MAN)) {
            _form._svf.VrsOut("SEX", SEX_MAN);
        } else if (student._sex.equals(SEX_WOMAN)) {
            _form._svf.VrsOut("SEX", SEX_WOMAN);
        }

        /* 住所１ */
        _form.printAddr1(student);
        /* 住所２ */
        _form.printAddr2(student);
        /* 電話番号 */
        _form._svf.VrsOut("TELNO", student._schregAddressDat._telno);
        /* 課程 */
        if(!student._course._courseName.equals("")){
            _form._svf.VrsOut("COURSENAME", student._course._courseName+COURSE_HOKAN_NAME);
        }
        /* 学科 */
        _form._svf.VrsOut("MAJORNAME", student._major._majorName);
        /* 学年 */
        _form._svf.VrsOut("ANNUAL", student._schregRegdDat._annual != "" ?
                Integer.toString(Integer.parseInt(student._schregRegdDat._annual)) : "");
        /* 証明書番号 */
        _form._svf.VrsOut("CERTIF_NO2", student._schregNo);

        /* 有効期限 */
        _form._svf.VrsOut("MONTH_DIV", student._certifIssueDat._certifDetailEachtypeDat._remark6);
        /* 発駅 */
        _form._svf.VrsOut("START_STATION", student._certifIssueDat._certifDetailEachtypeDat._remark1);
        /* 着駅 */
        _form._svf.VrsOut("END_STATION", student._certifIssueDat._certifDetailEachtypeDat._remark2);
        /* 経由駅 */
        _form._svf.VrsOut("BY_WAY_STATION", student._certifIssueDat._certifDetailEachtypeDat._remark3);
        /* 通学証明書の有効期間 */
        String[] dates = KNJ_EditDate.tate_format(getJDate(student._certifIssueDat._certifDetailEachtypeDat._remark5));
        _form._svf.VrsOut("LIMIT_YEAR", dates[1]);
        _form._svf.VrsOut("LIMIT_MONTH", dates[2]);
        _form._svf.VrsOut("LIMIT_DAY", dates[3]);
        /* 証明欄・発行日付 */
        dates = KNJ_EditDate.tate_format(getJDate(student._certifIssueDat._issueDate));
        _form._svf.VrsOut("ISSUE_YEAR", dates[1]);
        _form._svf.VrsOut("ISSUE_MONTH", dates[2]);
        _form._svf.VrsOut("ISSUE_DAY", dates[3]);

        /* 所属住所 */
        _form._svf.VrsOut("BLG_ADDR", student._schregRegdDat._belongAddr1);
        if (null != student._schregRegdDat._belongAddr2 && !student._schregRegdDat._belongAddr2.equals("")) {
            /* 所属情報 */
            _form._svf.VrsOut("BLG_INFO", student._schregRegdDat._belongAddr2);
            /* 所属名 */
            _form._svf.VrsOut("BLG_NAME", student._schregRegdDat._belongName);
        } else {
            /* 所属情報 */
            _form._svf.VrsOut("BLG_INFO", student._schregRegdDat._belongName);
        }

        /* 学校所在地 */
        _form._svf.VrsOut("REMARK", student._certifSchoolDat._remark1);
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME", student._certifSchoolDat._schoolName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME", student._certifSchoolDat._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME", student._certifSchoolDat._principalName);
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
        private final String[] _schregno;
        private final String[] _certif;            // 通学証明書番号
        private final String _type;                 // 証明書種別コード
        private final String _exeYear;              // 指示画面検索年度

        private Map _prefMap;                       // 都道府県

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] schregno,
                final String[] certif,
                final String type,
                final String exeYear
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
            _certif = certif;
            _type = type;
            _exeYear = exeYear;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();

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
        final String loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
        final String[] schregno = request.getParameterValues("SCHREGNO[]");
        final String[] certif = request.getParameterValues("TUGAKU[]");
        final String type = request.getParameter("TYPE");
        final String exeYear = request.getParameter("EXE_YEAR");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno,
                certif,
                type,
                exeYear
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

        public void printAge(String date, String birthday) {
            _form._svf.VrsOut("AGE", Integer.toString(getAge(date, birthday)));
        }

        public int getAge(String date, String birthday) {
            final Calendar nowCal;
            try {
                nowCal = parseDate(date);
                int nowYear = nowCal.get(Calendar.YEAR);
                int nowMonth = nowCal.get(Calendar.MONTH) + 1;
                int nowDay = nowCal.get(Calendar.DATE);

                if (birthday == null) {
                    return 0;
                }

                final Calendar cal = KNJServletUtils.parseDate(birthday);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DATE);

                 return Integer.parseInt(""+(nowYear-year -
                  ((month > nowMonth || (month == nowMonth &&
                          day > nowDay)) ? 1:0)));
            } catch (ParseException e) {
                return 0;
            }
        }

        public Calendar parseDate(final String dateStr) throws ParseException {
            return parseDate(dateStr, "yyyy/MM/dd");
        }

        private Calendar parseDate(final String dateStr, final String pattern) throws ParseException {
            // 文字列を Date型に
            final SimpleDateFormat format = new SimpleDateFormat(pattern);
            final Date date = format.parse(dateStr);

            // Date型を Calendar に
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        }

        public void printName(Student student) {
            String name = student._name;
            
            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printAddr1(Student student) {
            String name = (_param._prefMapString(student._schregAddressDat._prefCd) != null ?
                    _param._prefMapString(student._schregAddressDat._prefCd) : "")
                    + (student._schregAddressDat._addr1 != null ?
                            student._schregAddressDat._addr1 : "")
                    + (student._schregAddressDat._addr2 != null ?
                            student._schregAddressDat._addr2 : "");

            if (name != null) {
                final String label;
                if (name.length() <= ADD1_LENG) {
                    label = "ADDRESS1_1";
                } else {
                    label = "ADDRESS1_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printAddr2(Student student) {
            String name = student._schregAddressDat._addr3 != null ?
                    student._schregAddressDat._addr3 : "";
            _form._svf.VrsOut("ADDRESS2", name);
        }
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student {
        private final String _schregNo;         // 学籍番号
        private final String _name;             // 氏名
        private final String _birthday;         // 生年月日
        private final String _sex;              // 性別

        private SchregRegdDat _schregRegdDat;    // 生徒。学籍在籍データ
        private Course _course;                  // 課程データ
        private Major _major;                    // 学科データ
        private SchregAddressDat _schregAddressDat; // 生徒。学籍住所データ
        private CertifSchoolDat _certifSchoolDat;   // 証明書学校データ
        private CertifIssueDat _certifIssueDat;

        Student(final String schregNo,
                final String name,
                final String birthday,
                final String sex
        ) {
            _schregNo = schregNo;
            _name = name;
            _birthday = birthday;
            _sex = sex;
        }

        public void load(DB2UDB db2, int i) throws SQLException, Exception {
            _schregRegdDat = createSourseCodeDat(db2, _param._exeYear, _param._semester, _schregNo);
            _major = createMajorDat(db2, _schregRegdDat._courseCd, _schregRegdDat._majorCd);
            _course = createCourseDat(db2, _schregRegdDat._courseCd);
            _schregAddressDat = createSchregAddressDat(db2, _schregNo);
            _certifIssueDat = createCertifIssueDat(db2, _param._certif[i]);
            _certifSchoolDat = createSchoolDat(db2, _param._exeYear, _certifIssueDat._certifKindCd);
        }
    }

    private Student createStudent(final DB2UDB db2, String schregno, final int i)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String birthday = rs.getString("birthday");
                final String sex = rs.getString("sex");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        birthday,
                        sex
                );

                studentDat.load(db2, i);
                return studentDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    BIRTHDAY as birthday,"
                + "    SEX as sex"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _annual;        // 年次
        private final String _courseCd;      // 課程コード
        private final String _majorCd;       // 学科コード
        private final String _belongName;    // 所属名
        private final String _belongAddr1;    // 所属住所
        private final String _belongAddr2;

        SchregRegdDat(
                final String annual,
                final String courseCd,
                final String majorCd,
                final String belongName,
                final String pref,
                final String addr1,
                final String addr2,
                final String addr3
        ) {
            _annual = annual;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _belongName = belongName;
            _belongAddr1 = pref + addr1 + addr2;
            _belongAddr2 = addr3;
        }

        public SchregRegdDat() {
            _annual = "";
            _courseCd = "";
            _majorCd = "";
            _belongName = "";
            _belongAddr1 = "";
            _belongAddr2 = "";
        }
    }

    public SchregRegdDat createSourseCodeDat(DB2UDB db2, String YEAR, String SEMESTER, String SCHREGNO)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String annual = rs.getString("annual");
                final String courseCd = rs.getString("courseCd");
                final String majorCd = rs.getString("majorCd");
                final String belongName = null != rs.getString("SCHOOLNAME1") ? rs.getString("SCHOOLNAME1") : "";
                final String pref = null != rs.getString("PREF_NAME") ? rs.getString("PREF_NAME") : "";
                final String addr1 = null != rs.getString("SCHOOLADDR1") ? rs.getString("SCHOOLADDR1") : "";
                final String addr2 = null != rs.getString("SCHOOLADDR2") ? rs.getString("SCHOOLADDR2") : "";
                final String addr3 = null != rs.getString("SCHOOLADDR3") ? rs.getString("SCHOOLADDR3") : "";

                final SchregRegdDat schregRegdDat = new SchregRegdDat(
                        annual,
                        courseCd,
                        majorCd,
                        belongName,
                        pref,
                        addr1,
                        addr2,
                        addr3
                );
                return schregRegdDat;
            }
            return new SchregRegdDat();
    }

    private String sqlSchregRegdDat(String year, String semester, String schregNo) {
        return " select"
                + "    T1.ANNUAL as annual,"
                + "    T1.COURSECD as courseCd,"
                + "    T1.MAJORCD as majorCd,"
                + "    L1.SCHOOLNAME1,"
                + "    L2.PREF_NAME,"
                + "    L1.SCHOOLADDR1,"
                + "    L1.SCHOOLADDR2,"
                + "    L1.SCHOOLADDR3"
                + " from"
                + "    SCHREG_REGD_DAT T1 "
                + "    LEFT JOIN BELONGING_MST L1 ON T1.GRADE = L1.BELONGING_DIV "
                + "    LEFT JOIN PREF_MST L2 ON L1.SCHOOLPREF_CD = L2.PREF_CD "
                + " where"
                + "    T1.SCHREGNO = '" + schregNo + "' and"
                + "    T1.YEAR = '" + year + "' and"
                + "    T1.SEMESTER = '" + semester + "'"
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
     * 証明書学校データ。
     */
    private class CertifSchoolDat {
        private final String _remark1; 		// 学校所在地
        private final String _schoolName;		// 学校名
        private final String _jobName;			// 役職名
        private final String _principalName;	// 校長名

        CertifSchoolDat(
                final String remark1,
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
        	_remark1 = remark1;
        	_schoolName = schoolName;
        	_jobName = jobName;
        	_principalName = principalName;
        }

        public CertifSchoolDat() {
        	_remark1 = "";
        	_schoolName = "";
        	_jobName = "";
        	_principalName = "";
        }
    }

    private CertifSchoolDat createSchoolDat(DB2UDB db2, String year, String certifKind)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlCertifSchoolDat(year, certifKind));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String remark1 = nvlT(rs.getString("remark1"));
            final String schoolName = nvlT(rs.getString("schoolName"));
            final String jobName = nvlT(rs.getString("jobName"));
            final String principalName = nvlT(rs.getString("principalName"));

            final CertifSchoolDat certifSchoolDat = new CertifSchoolDat(
            		remark1,
            		schoolName,
            		jobName,
            		principalName
            );
            return certifSchoolDat;
        }                    
        return new CertifSchoolDat();
    }

    private String sqlCertifSchoolDat(String year, String certifKind) {
        return " select"
                + "    REMARK1 as remark1,"
                + "    SCHOOL_NAME as schoolName,"
                + "    JOB_NAME as jobName,"
                + "    PRINCIPAL_NAME as principalName"
                + " from"
                + "    CERTIF_SCHOOL_DAT"
                + " where"
                + "    YEAR = '" + year + "' and"
                + "    CERTIF_KINDCD = '" + certifKind + "'"
                ;
    }

    // ======================================================================
    /**
     * 課程データ。
     */
    private class Course {
        private final String _courseName;

        Course(
                final String courseName
        ) {
        	_courseName = courseName;
        }

        public Course() {
        	_courseName = "";
        }
    }


    public Course createCourseDat(DB2UDB db2, String courseCd)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlCourseDat(courseCd));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = nvlT(rs.getString("name"));
                
                final Course course = new Course(
                        name
                );
                return course;
            }
            return new Course();
    }

    private String sqlCourseDat(String coursecd) {
        return " select"
                + "    COURSENAME as name"
                + " from"
                + "    COURSE_MST"
                + " where"
                + "    COURSECD = '" + coursecd + "' "
                ;
    }

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
     * 証明書種別別発行データ。
     */
    private class CertifIssueDat {
        private final String _schregNo;     // 学籍番号
        private final String _type;         // 証明書種別
        private final String _certifKindCd; // 証明書種類コード
        private final String _issueDate;    // 証明書発行日付
        private final String _certifNo;     // 証明書番号

        private CertifDetailEachtypeDat _certifDetailEachtypeDat;
        
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
            _certifDetailEachtypeDat = createCertifDetailEachtypeDat(db2, certifIndex);
        }
    }

    private CertifIssueDat createCertifIssueDat(DB2UDB db2, String pCertifIndex)
        throws SQLException, Exception {

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
                + "    YEAR = '" + _param._exeYear + "' and"
                + "    CERTIF_INDEX = '" + certifIndex + "'"
                ;
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
                + "    YEAR = '" + _param._exeYear + "' and"
                + "    CERTIF_INDEX = '" + certifIndex + "'"
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
    
} // KNJWA172

// eof
