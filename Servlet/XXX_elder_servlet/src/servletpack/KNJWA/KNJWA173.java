// kanji=漢字
/*
 * $Id: f05902eca2ae120a847c81e2fd68689beb965389 $
 *
 * 作成日: 2007/11/21 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.text.ParseException;
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
 * 運賃割引証
 * @author nakada
 * @version $Id: f05902eca2ae120a847c81e2fd68689beb965389 $
 */
public class KNJWA173 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA173.class);

    private static final String FORM_FILE = "KNJWA173.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 名前 */
    private static final int NAME1_LENG = 20;

    /* 
     * 課程名と学科名の間の固定文字 
     */
    private static final String COURSE_HOKAN_NAME = "課程";
    
    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 運賃割引証印刷 */
    private static final String OUTPUT3_PRINT_ON = "1";

    /*
     * 学生区分
     */
    /** 2:サポート生 */
    private static final String STUDENT_DIV_SUPPORT = "02";    
    /** 5:科目履修生 */
    private static final String STUDENT_DIV_STUDY = "05";    

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _detailCnt;

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
            _detailCnt = 0;
            for (int i = 0; i < _param._schregno.length; i++) {
                final String schregno = _param._schregno[i];
                log.debug(">>学籍番号=" + schregno);

                final Student student = createStudent(db2, schregno, i);

                if (!student._schregRegdDat._studentDiv.equals(STUDENT_DIV_STUDY)) {
                    printMain(student, i);
                }
            }

            if (_detailCnt >= 1) {
                _form._svf.VrEndPage();
                _hasData = true;
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printMain(final Student student, int i) throws SQLException {
        _detailCnt++;
        printApplicant(student, i);

        if (_detailCnt >= 4) {
            _form._svf.VrEndPage();
            _hasData = true;

            _detailCnt = 0;
        }
    }

    private void printApplicant(Student student, int i) throws SQLException {
        /* 第号 */
        _form._svf.VrsOut("CERTIF_NO1_" + _detailCnt, student._certifIssueDat._certifNo);

        /* 発駅 */
        _form._svf.VrsOut("START_STATION" + _detailCnt, student._certifIssueDat._certifDetailEachtypeDat._remark1);
        /* 着駅 */
        _form._svf.VrsOut("END_STATION" + _detailCnt, student._certifIssueDat._certifDetailEachtypeDat._remark2);
        /* 経由駅 */
        _form._svf.VrsOut("BY_WAY_STATION" + _detailCnt, student._certifIssueDat._certifDetailEachtypeDat._remark3);
        /* 課程 */
        if(!student._course._courseName.equals("")){
            _form._svf.VrsOut("COURSENAME" + _detailCnt, student._course._courseName+COURSE_HOKAN_NAME);
        }
        /* 学科 */
        _form._svf.VrsOut("MAJORNAME" + _detailCnt, student._major._majorName);
        /* 学年 */
        _form._svf.VrsOut("ANNUAL" + _detailCnt, 
                Integer.toString(Integer.parseInt(student._schregRegdDat._annual)));

        /* 証明書番号 */
        _form._svf.VrsOut("CERTIF_NO2_" + _detailCnt, student._certifIssueDat._certifNo);

        /* 氏名 */
        _form.printName(student);

        /* 年齢 */
        _form.printAge(_param._loginDate, student._birthday);
        
        /* 有効期間・開始日付 */
        String[] dates = KNJ_EditDate.tate_format(getJDate(student._certifIssueDat._certifDetailEachtypeDat._remark4));
        _form._svf.VrsOut("START_YEAR" + _detailCnt, dates[1]);
        _form._svf.VrsOut("START_MONTH" + _detailCnt, dates[2]);
        _form._svf.VrsOut("START_DAY" + _detailCnt, dates[3]);

        /* 有効期間・終了日付 */
        dates = KNJ_EditDate.tate_format(getJDate(student._certifIssueDat._certifDetailEachtypeDat._remark5));
        _form._svf.VrsOut("LIMIT_YEAR" + _detailCnt, dates[1]);
        _form._svf.VrsOut("LIMIT_MONTH" + _detailCnt, dates[2]);
        _form._svf.VrsOut("LIMIT_DAY" + _detailCnt, dates[3]);

        /* 発行日付 */
        dates = KNJ_EditDate.tate_format(getJDate(student._certifIssueDat._issueDate));
        _form._svf.VrsOut("ISSUE_YEAR" + _detailCnt, dates[1]);
        _form._svf.VrsOut("ISSUE_MONTH" + _detailCnt, dates[2]);
        _form._svf.VrsOut("ISSUE_DAY" + _detailCnt, dates[3]);
        
        /* 学校所在地 */
        _form._svf.VrsOut("REMARK" + _detailCnt, student._certifSchoolDat._remark1);
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME" + _detailCnt, student._certifSchoolDat._schoolName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME" + _detailCnt, student._certifSchoolDat._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME" + _detailCnt, student._certifSchoolDat._principalName);

        /* 所属センター */
        _form.prtSaportCenter(student);
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
        private final String[] _certif;             // 証明書番号
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
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String[] schregno = request.getParameterValues("SCHREGNO[]");
        final String[] certif = request.getParameterValues("UNTIN[]");
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
            String age = Integer.toString(getAge(date, birthday));

            _form._svf.VrsOut("AGE" + _detailCnt, age);
        }

        public int getAge(String date, String birthday) {
            final Calendar nowCal;
            try {
                nowCal = KNJServletUtils.parseDate(date);
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

        public void printName(Student student) {
            String name = student._name;
            
            if (name != null) {
                if (name.length() <= NAME1_LENG) {
                  _form._svf.VrsOut("NAME" + _detailCnt + "_1", name);
                } else {
                    _form._svf.VrsOut("NAME" + _detailCnt + "_2", name);
                }
            }
        }

        void prtSaportCenter(final Student student) {
            if (student._schregRegdDat._studentDiv.equals(STUDENT_DIV_SUPPORT)) {
                String string = (student._schregRegdDat._belongingDat._schoolAddr1 != null ?
                                student._schregRegdDat._belongingDat._schoolAddr1 : "")
                        + (student._schregRegdDat._belongingDat._schoolAddr2 != null ?
                                student._schregRegdDat._belongingDat._schoolAddr2 : "")
                        + (student._schregRegdDat._belongingDat._schoolAddr3 != null ?
                                student._schregRegdDat._belongingDat._schoolAddr3 : "")
                        + "　" + (student._schregRegdDat._belongingDat._schoolName1 != null ?
                                student._schregRegdDat._belongingDat._schoolName1 : "");

                _form._svf.VrsOut("SCHOOL_ADDR" + _detailCnt, string);
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
        private final String _birthday;         // 生年月日

        private SchregRegdDat _schregRegdDat;    // 生徒。学籍在籍データ
        private CertifIssueDat _certifIssueDat;
        private Course _course;                  // 課程データ
        private Major _major;                    // 学科データ
        private CertifSchoolDat _certifSchoolDat;// 証明書学校データ

        Student(final String schregNo,
                final String name,
                final String birthday
        ) {
            _schregNo = schregNo;
            _name = name;
            _birthday = birthday;
        }

        public void load(DB2UDB db2, int i) throws SQLException, Exception {
            _schregRegdDat = createSourseCodeDat(db2, _param._exeYear, _param._semester, _schregNo);
            _course = createCourseDat(db2, _schregRegdDat._courseCd);
            _major = createMajorDat(db2, _schregRegdDat._courseCd, _schregRegdDat._majorCd);
            _certifIssueDat = createCertifIssueDat(db2, _param._certif[i]);
            _certifSchoolDat = createSchoolDat(db2, _param._year, _certifIssueDat._certifKindCd);
        }
    }

    private Student createStudent(final DB2UDB db2, String schregno, int i)
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

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        birthday
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
                + "    BIRTHDAY as birthday"
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
        private final String _grade;         // 所属
        private final String _majorCd;       // 学科コード
        private final String _courseCd;      // 課程コード
        private final String _studentDiv;   // 学生区分

        private BelongingDat _belongingDat;

        SchregRegdDat() {
            _annual = "";
            _grade = "";
            _majorCd = "";
            _courseCd = "";
            _studentDiv = "";
        }

        SchregRegdDat(
                final String annual,
                final String grade,
                final String majorCd,
                final String courseCd,
                final String studentDiv
        ) {
            _annual = annual;
            _grade = grade;
            _majorCd = majorCd;
            _courseCd = courseCd;
            _studentDiv = studentDiv;
        }

        public void load(DB2UDB db2) throws SQLException {
            _belongingDat = createBelongingDat(db2, _grade);
        }
    }

    public SchregRegdDat createSourseCodeDat(DB2UDB db2, String YEAR, String SEMESTER, String SCHREGNO)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String annual = rs.getString("annual");
            final String grade = rs.getString("grade");
            final String majorCd = rs.getString("majorCd");
            final String courseCd = rs.getString("courseCd");
            final String studentDiv = rs.getString("studentDiv");

            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    annual,
                    grade,
                    majorCd,
                    courseCd,
                    studentDiv
            );
            schregRegdDat.load(db2);
            return schregRegdDat;
        }

        return new SchregRegdDat();
    }

    private String sqlSchregRegdDat(String year, String semester, String schregNo) {
        return " select"
                + "    ANNUAL as annual,"
                + "    GRADE as grade,"
                + "    MAJORCD as majorCd,"
                + "    COURSECD as courseCd,"
                + "    value(STUDENT_DIV, '') as studentDiv"
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
     * 所属データ。
     */
    private class BelongingDat {
        private final String _schoolName1;
        private final String _schoolprefCd;
        private final String _schoolAddr1;
        private final String _schoolAddr2;
        private final String _schoolAddr3;

        BelongingDat() {
            _schoolName1 = "";
            _schoolprefCd = "";
            _schoolAddr1 =  "";
            _schoolAddr2 =  "";
            _schoolAddr3 =  "";
        }

        BelongingDat(
                final String schoolName1,
                final String schoolprefCd,
                final String schoolAddr1,
                final String schoolAddr2,
                final String schoolAddr3
        ) {
            _schoolName1 = schoolName1;
            _schoolprefCd = schoolprefCd;
            _schoolAddr1 =  schoolAddr1;
            _schoolAddr2 =  schoolAddr2;
            _schoolAddr3 =  schoolAddr3;
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
                final String schoolprefCd = rs.getString("schoolprefCd");
                final String schoolAddr1 = rs.getString("schoolAddr1");
                final String schoolAddr2 = rs.getString("schoolAddr2");
                final String schoolAddr3 = rs.getString("schoolAddr3");
                
                final BelongingDat belonging = new BelongingDat(
                        name,
                        schoolprefCd,
                        schoolAddr1,
                        schoolAddr2,
                        schoolAddr3
                );
                return belonging;
            }
            return null;
    }

    private String sqlBelongingDat(String belongingDiv) {
        return " select"
                + "    SCHOOLNAME1 as name,"
                + "    SCHOOLPREF_CD as schoolprefCd,"
                + "    SCHOOLADDR1 as schoolAddr1,"
                + "    SCHOOLADDR2 as schoolAddr2,"
                + "    SCHOOLADDR3 as schoolAddr3"
                + " from"
                + "    BELONGING_MST"
                + " where"
                + "    BELONGING_DIV = '" + belongingDiv + "'"
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
                + "    YEAR = '" + _param._year + "' and"
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
                + "    YEAR = '" + _param._year + "' and"
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
    
} // KNJWA173

// eof
