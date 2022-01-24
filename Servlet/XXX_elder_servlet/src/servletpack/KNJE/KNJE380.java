// kanji=漢字
/*
 * $Id: 45caae4118efef63f4273d75e48c2378360f832c $
 *
 * 作成日: 2009/10/19 11:25:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

import servletpack.KNJE.detail.AftGradCourseDat;
import servletpack.KNJE.detail.CourseHopeDat;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 45caae4118efef63f4273d75e48c2378360f832c $
 */
public class KNJE380 {

    private static final Log log = LogFactory.getLog("KNJE380.class");

    private static final String SELECT_GRADE = "1";

    private static final String SENKOU_SINGAKU = "0";
    private static final String SENKOU_SHUSHOKU = "1";

    private static final String COURSE_KIND_SIN = "1";
    private static final String COURSE_KIND_SHU = "2";
    private static final String COURSE_KIND_KAJI = "3";
    private static final String COURSE_KIND_MITEI = "4";
    private static final String ALL_MAJOR = "9999";

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            for (int i = 0; i < _param._majorSelected.length; i++) {
                final String majorCd = _param._majorSelected[i];
                printMain(db2, svf, majorCd);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String majorCd) throws SQLException {
        final List printStudents = getPrintStudent(db2, majorCd);

        KNJE380Abstract[] printClass = new KNJE380Abstract[7];
        if (_param._print1) {
            printClass[0] = new KNJE380_1(_param, db2, svf);
        }
        if (_param._print2) {
            printClass[1] = new KNJE380_2(_param, db2, svf);
        }
        if (_param._print3) {
            printClass[2] = new KNJE380_3(_param, db2, svf);
        }
        if (_param._print4) {
            printClass[3] = new KNJE380_4(_param, db2, svf);
        }
        if (_param._print5) {
            printClass[4] = new KNJE380_5(_param, db2, svf);
        }
        if (_param._print6) {
            printClass[5] = new KNJE380_6(_param, db2, svf);
        }
        if (_param._print7) {
            printClass[6] = new KNJE380_7(_param, db2, svf);
        }
        if (printStudents.size() > 0) {
            //印字処理
            for (int i = 0; i < printClass.length; i++) {
                _hasData = null != printClass[i] && printClass[i].printMain(printStudents, majorCd) ? true : _hasData;
            }
        }
    }

    private List getPrintStudent(final DB2UDB db2, final String majorCd) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement psSt = null;
        ResultSet rsSt = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            // 01(学年) OR 01001(年組)
            final String studentSql = getStudentSql(_param._classSelected[i], majorCd);
            try {
                psSt = db2.prepareStatement(studentSql);
                rsSt = psSt.executeQuery();
                while (rsSt.next()) {
                    final String schregno = rsSt.getString("SCHREGNO");
                    final String grade = rsSt.getString("GRADE");
                    final String hrClass = rsSt.getString("HR_CLASS");
                    final String hrName = rsSt.getString("HR_NAME");
                    final String attendno = rsSt.getString("ATTENDNO");
                    final String annual = rsSt.getString("ANNUAL");
                    final String name = rsSt.getString("NAME");
                    final String sexCd = rsSt.getString("SEX_CD");
                    final String sex = rsSt.getString("SEX");
                    final String grdDiv = rsSt.getString("GRD_DIV");
                    final String grdDate = rsSt.getString("GRD_DATE");
                    final String coursecd = rsSt.getString("COURSECD");
                    final String majorcd = rsSt.getString("MAJORCD");
                    final String coursecode = rsSt.getString("COURSECODE");
                    final String coursename = rsSt.getString("COURSENAME");
                    final String majorname = rsSt.getString("MAJORNAME");
                    final String coursecodename = rsSt.getString("COURSECODENAME");
                    final Student student = new Student(schregno,
                                                        grade,
                                                        hrClass,
                                                        hrName,
                                                        attendno,
                                                        annual,
                                                        name,
                                                        sexCd,
                                                        sex,
                                                        grdDiv,
                                                        grdDate,
                                                        coursecd,
                                                        majorcd,
                                                        coursecode,
                                                        coursename,
                                                        majorname,
                                                        coursecodename);
                    student._aftGradCourseDatSin = student.setAft(db2, "0");
                    student._aftGradCourseDatShu = student.setAft(db2, "1");
                    student.setHope(db2);
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, psSt, rsSt);
                db2.commit();
            }
        }
        return rtnList;
    }

    private String getStudentSql(final String selected, final String majorCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     HR.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.ANNUAL, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.SEX AS SEX_CD, ");
        stb.append("     N1.NAME2 AS SEX, ");
        stb.append("     BASE.GRD_DATE, ");
        stb.append("     BASE.GRD_DIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     COURSE.COURSENAME, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     COURSEC.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ");
        stb.append("          AND T1.SEMESTER = HR.SEMESTER ");
        stb.append("          AND T1.GRADE = HR.GRADE ");
        stb.append("          AND T1.HR_CLASS = HR.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = N1.NAMECD2 ");
        stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
        stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
        stb.append("          AND T1.MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        if (_param._kubun.equals(SELECT_GRADE)) {
            stb.append("     AND T1.GRADE = '" + selected + "' ");
        } else {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
        }
        if (!majorCd.equals(ALL_MAJOR)) {
            stb.append("     AND T1.COURSECD || T1.MAJORCD = '" + majorCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    public class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _annual;
        final String _name;
        final String _sexCd;
        final String _sex;
        final String _grdDiv;
        final String _grdDate;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _coursename;
        final String _majorname;
        final String _coursecodename;
        boolean _isSingakuHope = false;
        boolean _isShushokuHope = false;
        boolean _isKajiHope = false;
        boolean _isMiteiHope = false;
        boolean _isSingaku = false;
        boolean _isShushoku = false;
        AftGradCourseDat _aftGradCourseDatSin = null;
        AftGradCourseDat _aftGradCourseDatShu = null;
        CourseHopeDat _courseHopeDat = null;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String annual,
                final String name,
                final String sexCd,
                final String sex,
                final String grdDiv,
                final String grdDate,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String coursename,
                final String majorname,
                final String coursecodename
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _annual = annual;
            _name = name;
            _sexCd = sexCd;
            _sex = sex;
            _grdDiv = grdDiv;
            _grdDate = grdDate;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _coursename = coursename;
            _majorname = majorname;
            _coursecodename = coursecodename;
        }

        public void setHope(final DB2UDB db2) throws SQLException {
            final String hopeSql = getHopeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hopeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseKind = rs.getString("COURSE_KIND");
                    if (courseKind.equals(COURSE_KIND_SIN) && _isShushoku) {
                        break;
                    }

                    if (courseKind.equals(COURSE_KIND_SIN)) {
                        _isSingakuHope = true;
                    }
                    if (courseKind.equals(COURSE_KIND_SHU)) {
                        _isShushokuHope = true;
                    }
                    if (courseKind.equals(COURSE_KIND_KAJI)) {
                        _isKajiHope = true;
                    }
                    if (courseKind.equals(COURSE_KIND_MITEI)) {
                        _isMiteiHope = true;
                    }
                    final String questionnaireCd = rs.getString("QUESTIONNAIRECD");
                    final String schoolGroup1 = rs.getString("SCHOOL_GROUP1");
                    final String facultyGroup1 = rs.getString("FACULTY_GROUP1");
                    final String departmentGroup1 = rs.getString("DEPARTMENT_GROUP1");
                    final String schoolCd1 = rs.getString("SCHOOL_CD1");
                    final String facultyCd1 = rs.getString("FACULTYCD1");
                    final String departmentCd1 = rs.getString("DEPARTMENTCD1");
                    final String howtoexam1 = rs.getString("HOWTOEXAM1");
                    final String schoolGroup2 = rs.getString("SCHOOL_GROUP2");
                    final String facultyGroup2 = rs.getString("FACULTY_GROUP2");
                    final String departmentGroup2 = rs.getString("DEPARTMENT_GROUP2");
                    final String schoolCd2 = rs.getString("SCHOOL_CD2");
                    final String facultyCd2 = rs.getString("FACULTYCD2");
                    final String departmentCd2 = rs.getString("DEPARTMENTCD2");
                    final String howtoexam2 = rs.getString("HOWTOEXAM2");
                    final String jobtypeLCd1 = rs.getString("JOBTYPE_LCD1");
                    final String jobtypeMCd1 = rs.getString("JOBTYPE_MCD1");
                    final String jobtypeSCd1 = rs.getString("JOBTYPE_SCD1");
                    final String workArea1 = rs.getString("WORK_AREA1");
                    final String introductionDiv1 = rs.getString("INTRODUCTION_DIV1");
                    final String jobtypeLCd2 = rs.getString("JOBTYPE_LCD2");
                    final String jobtypeMCd2 = rs.getString("JOBTYPE_MCD2");
                    final String jobtypeSCd2 = rs.getString("JOBTYPE_SCD2");
                    final String workArea2 = rs.getString("WORK_AREA2");
                    final String introductionDiv2 = rs.getString("INTRODUCTION_DIV2");
                    final String remark = rs.getString("REMARK");
                    final String year = rs.getString("YEAR");
                    _courseHopeDat = new CourseHopeDat(
                            courseKind,
                            questionnaireCd,
                            schoolGroup1,
                            facultyGroup1,
                            departmentGroup1,
                            schoolCd1,
                            facultyCd1,
                            departmentCd1,
                            howtoexam1,
                            schoolGroup2,
                            facultyGroup2,
                            departmentGroup2,
                            schoolCd2,
                            facultyCd2,
                            departmentCd2,
                            howtoexam2,
                            jobtypeLCd1,
                            jobtypeMCd1,
                            jobtypeSCd1,
                            workArea1,
                            introductionDiv1,
                            jobtypeLCd2,
                            jobtypeMCd2,
                            jobtypeSCd2,
                            workArea2,
                            introductionDiv2,
                            remark,
                            year
                            );
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHopeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH HOPE_MAX_DATE AS ( ");
            stb.append(" SELECT ");
            stb.append("     MAX(T1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("     MAX(T1.SCHREGNO) AS SCHREGNO, ");
            stb.append("     MAX(T1.QUESTIONNAIRECD) AS QUESTIONNAIRECD, ");
            stb.append("     MAX(T1.YEAR) AS YEAR ");
            stb.append(" FROM ");
            stb.append("     COURSE_HOPE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.QUESTIONNAIRECD = '" + _param._questionNaireCd + "' ");
            stb.append(" ), HOPE_MAX AS ( ");
            stb.append(" SELECT ");
            stb.append("     MAX(T1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("     MAX(T1.SEQ) AS SEQ, ");
            stb.append("     MAX(T1.QUESTIONNAIRECD) AS QUESTIONNAIRECD, ");
            stb.append("     MAX(T1.SCHREGNO) AS SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     COURSE_HOPE_DAT T1, ");
            stb.append("     HOPE_MAX_DATE T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.ENTRYDATE = T2.ENTRYDATE ");
            stb.append("     AND T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     COURSE_HOPE_DAT T1, ");
            stb.append("     HOPE_MAX T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTRYDATE = T2.ENTRYDATE ");
            stb.append("     AND T1.SEQ = T2.SEQ ");
            stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            return stb.toString();
        }

        public AftGradCourseDat setAft(final DB2UDB db2, final String senkou) throws SQLException {
            AftGradCourseDat retAft = null;
            final String aftSql = getAftSql(senkou);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aftSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String statKind = rs.getString("STAT_KIND");
                    final String senkouKind = rs.getString("SENKOU_KIND");
                    final String statCd = rs.getString("STAT_CD");
                    final String statName = rs.getString("STAT_NAME");
                    final String schoolGroup = rs.getString("SCHOOL_GROUP");
                    final String jobtypeLcd = rs.getString("JOBTYPE_LCD");
                    final String jobtypeLname = rs.getString("JOBTYPE_LNAME");
                    final String jobtypeMcd = rs.getString("JOBTYPE_MCD");
                    final String jobtypeMname = rs.getString("JOBTYPE_MNAME");
                    final String jobtypeScd = rs.getString("JOBTYPE_SCD");
                    final String jobtypeSname = rs.getString("JOBTYPE_SNAME");
                    final String schoolSort = rs.getString("SCHOOL_SORT");
                    final String prefCd = rs.getString("PREF_CD");
                    final String cityCd = rs.getString("CITY_CD");
                    final String decision = rs.getString("DECISION");
                    final String planstat = rs.getString("PLANSTAT");
                    final String introductionDiv = rs.getString("INTRODUCTION_DIV");
                    // 進学
                    final String statDate1 = rs.getString("STAT_DATE1");
                    final String statStime = rs.getString("STAT_STIME");
                    final String statEtime = rs.getString("STAT_ETIME");
                    final String areaName = rs.getString("AREA_NAME");
                    final String statDate2 = rs.getString("STAT_DATE2");
                    final String contentexam = rs.getString("CONTENTEXAM");
                    final String reasonexam = rs.getString("REASONEXAM");
                    final String thinkexam = rs.getString("THINKEXAM");
                    // 就職"
                    final String jobDate1 = rs.getString("JOB_DATE1");
                    final String jobStime = rs.getString("JOB_STIME");
                    final String jobEtime = rs.getString("JOB_ETIME");
                    final String jobRemark = rs.getString("JOB_REMARK");
                    final String jobContent = rs.getString("JOB_CONTENT");
                    final String jobThink = rs.getString("JOB_THINK");
                    final String jobexDate1 = rs.getString("JOBEX_DATE1");
                    final String jobexStime = rs.getString("JOBEX_STIME");
                    final String jobexEtime = rs.getString("JOBEX_ETIME");
                    final String jobexRemark = rs.getString("JOBEX_REMARK");
                    final String jobexContent = rs.getString("JOBEX_CONTENT");
                    final String jobexThink = rs.getString("JOBEX_THINK");

                    retAft = new AftGradCourseDat(
                            statKind,
                            senkouKind,
                            statCd,
                            statName,
                            schoolGroup,
                            jobtypeLcd,
                            jobtypeLname,
                            jobtypeMcd,
                            jobtypeMname,
                            jobtypeScd,
                            jobtypeSname,
                            schoolSort,
                            prefCd,
                            cityCd,
                            decision,
                            planstat,
                            introductionDiv,
                            statDate1,
                            statStime,
                            statEtime,
                            areaName,
                            statDate2,
                            contentexam,
                            reasonexam,
                            thinkexam,
                            jobDate1,
                            jobStime,
                            jobEtime,
                            jobRemark,
                            jobContent,
                            jobThink,
                            jobexDate1,
                            jobexStime,
                            jobexEtime,
                            jobexRemark,
                            jobexContent,
                            jobexThink
                            );
                    if (senkouKind.equals(SENKOU_SINGAKU)) {
                        final String schoolCd = rs.getString("SCHOOL_CD");
                        final String schoolName = rs.getString("SCHOOL_NAME");
                        final String facultyCd = rs.getString("FACULTYCD");
                        final String facultyName = rs.getString("FACULTYNAME");
                        final String departmentCd = rs.getString("DEPARTMENTCD");
                        final String departmentName = rs.getString("DEPARTMENTNAME");
                        final String schoolTelno = rs.getString("SCHOOL_TELNO");
                        retAft.setCollegeDat(schoolCd, schoolName, facultyCd, facultyName, departmentCd, departmentName, schoolTelno);
                        _isSingaku = true;
                    }
                    if (senkouKind.equals(SENKOU_SHUSHOKU)) {
                        final String companyCd = rs.getString("COMPANY_CD");
                        final String companyName = rs.getString("COMPANY_NAME");
                        final String shushokuAddr = rs.getString("SHUSHOKU_ADDR");
                        final String shihonkin = rs.getString("SHIHONKIN");
                        final String soninzu = rs.getString("SONINZU");
                        final String toninzu = rs.getString("TONINZU");
                        final String industryLcd = rs.getString("INDUSTRY_LCD");
                        final String industryLname = rs.getString("INDUSTRY_LNAME");
                        final String industryMcd = rs.getString("INDUSTRY_MCD");
                        final String industryMname = rs.getString("INDUSTRY_MNAME");
                        final String companySort = rs.getString("COMPANY_SORT");
                        final String targetSex = rs.getString("TARGET_SEX");
                        final String zipcd = rs.getString("ZIPCD");
                        final String addr1 = rs.getString("ADDR1");
                        final String addr2 = rs.getString("ADDR2");
                        final String telno = rs.getString("COMPANY_TELNO");
                        final String remark = rs.getString("REMARK");
                        retAft.setCompanyMst(companyCd,
                                             companyName,
                                             shushokuAddr,
                                             shihonkin,
                                             soninzu,
                                             toninzu,
                                             industryLcd,
                                             industryLname,
                                             industryMcd,
                                             industryMname,
                                             companySort,
                                             targetSex,
                                             zipcd,
                                             addr1,
                                             addr2,
                                             telno,
                                             remark);
                        _isShushoku = true;
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAft;
        }

        private String getAftSql(final String senkou) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.STAT_KIND, ");
            stb.append("     T1.SENKOU_KIND, ");
            stb.append("     T1.STAT_CD, ");
            stb.append("     T1.STAT_NAME, ");
            stb.append("     T1.SCHOOL_GROUP, ");
            stb.append("     T1.JOBTYPE_LCD, ");
            stb.append("     JOB_L.JOBTYPE_LNAME, ");
            stb.append("     T1.JOBTYPE_MCD, ");
            stb.append("     JOB_M.JOBTYPE_MNAME, ");
            stb.append("     T1.JOBTYPE_SCD, ");
            stb.append("     JOB_S.JOBTYPE_SNAME, ");
            stb.append("     T1.SCHOOL_SORT, ");
            stb.append("     T1.PREF_CD, ");
            stb.append("     T1.CITY_CD, ");
            stb.append("     T1.DECISION, ");
            stb.append("     T1.PLANSTAT, ");
            stb.append("     T1.INTRODUCTION_DIV, ");
            stb.append("     T1.STAT_DATE1, ");
            stb.append("     T1.STAT_STIME, ");
            stb.append("     T1.STAT_ETIME, ");
            stb.append("     T1.AREA_NAME, ");
            stb.append("     T1.STAT_DATE2, ");
            stb.append("     T1.CONTENTEXAM, ");
            stb.append("     T1.REASONEXAM, ");
            stb.append("     T1.THINKEXAM, ");
            stb.append("     T1.JOB_DATE1, ");
            stb.append("     T1.JOB_STIME, ");
            stb.append("     T1.JOB_ETIME, ");
            stb.append("     T1.JOB_REMARK, ");
            stb.append("     T1.JOB_CONTENT, ");
            stb.append("     T1.JOB_THINK, ");
            stb.append("     T1.JOBEX_DATE1, ");
            stb.append("     T1.JOBEX_STIME, ");
            stb.append("     T1.JOBEX_ETIME, ");
            stb.append("     T1.JOBEX_REMARK, ");
            stb.append("     T1.JOBEX_CONTENT, ");
            stb.append("     T1.JOBEX_THINK, ");
            stb.append("     COLL_M.SCHOOL_CD, ");
            stb.append("     COLL_M.SCHOOL_NAME, ");
            stb.append("     T1.FACULTYCD, ");
            stb.append("     FACU.FACULTYNAME, ");
            stb.append("     T1.DEPARTMENTCD, ");
            stb.append("     DEPAR.DEPARTMENTNAME, ");
            stb.append("     COLL_M.TELNO AS SCHOOL_TELNO, ");
            stb.append("     COMPANY.COMPANY_CD, ");
            stb.append("     COMPANY.COMPANY_NAME, ");
            stb.append("     COMPANY.SHUSHOKU_ADDR, ");
            stb.append("     COMPANY.SHIHONKIN, ");
            stb.append("     COMPANY.SONINZU, ");
            stb.append("     COMPANY.TONINZU, ");
            stb.append("     COMPANY.INDUSTRY_LCD, ");
            stb.append("     INDU_L.INDUSTRY_LNAME, ");
            stb.append("     COMPANY.INDUSTRY_MCD, ");
            stb.append("     INDU_M.INDUSTRY_MNAME, ");
            stb.append("     COMPANY.COMPANY_SORT, ");
            stb.append("     COMPANY.TARGET_SEX, ");
            stb.append("     COMPANY.ZIPCD, ");
            stb.append("     COMPANY.ADDR1, ");
            stb.append("     COMPANY.ADDR2, ");
            stb.append("     COMPANY.TELNO AS COMPANY_TELNO, ");
            stb.append("     COMPANY.REMARK ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append("     LEFT JOIN JOBTYPE_L_MST JOB_L ON T1.JOBTYPE_LCD = JOB_L.JOBTYPE_LCD ");
            stb.append("     LEFT JOIN JOBTYPE_M_MST JOB_M ON T1.JOBTYPE_LCD = JOB_M.JOBTYPE_LCD ");
            stb.append("          AND T1.JOBTYPE_MCD = JOB_M.JOBTYPE_MCD ");
            stb.append("     LEFT JOIN JOBTYPE_S_MST JOB_S ON T1.JOBTYPE_LCD = JOB_S.JOBTYPE_LCD ");
            stb.append("          AND T1.JOBTYPE_MCD = JOB_S.JOBTYPE_MCD ");
            stb.append("          AND T1.JOBTYPE_SCD = JOB_S.JOBTYPE_SCD ");
            stb.append("     LEFT JOIN COLLEGE_MST COLL_M ON T1.STAT_CD = COLL_M.SCHOOL_CD ");
            stb.append("     LEFT JOIN COLLEGE_FACULTY_MST FACU ON T1.STAT_CD = FACU.SCHOOL_CD ");
            stb.append("          AND T1.FACULTYCD = FACU.FACULTYCD ");
            stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST DEPAR ON T1.STAT_CD = DEPAR.SCHOOL_CD ");
            stb.append("          AND T1.FACULTYCD = DEPAR.FACULTYCD ");
            stb.append("          AND T1.DEPARTMENTCD = DEPAR.DEPARTMENTCD ");
            stb.append("     LEFT JOIN COMPANY_MST COMPANY ON T1.STAT_CD = COMPANY.COMPANY_CD ");
            stb.append("     LEFT JOIN INDUSTRY_L_MST INDU_L ON COMPANY.INDUSTRY_LCD = INDU_L.INDUSTRY_LCD ");
            stb.append("     LEFT JOIN INDUSTRY_M_MST INDU_M ON COMPANY.INDUSTRY_LCD = INDU_M.INDUSTRY_LCD ");
            stb.append("          AND COMPANY.INDUSTRY_MCD = INDU_M.INDUSTRY_MCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND T1.SENKOU_KIND = '" + senkou + "' ");
            stb.append("     AND T1.PLANSTAT = '1' ");

            return stb.toString();
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _name;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    public class Param {
        final String _useSchool_KindField;
        final String _schoolCd;
        final String _schoolKindA023;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final boolean _print1;
        final boolean _print2;
        final boolean _print3;
        final boolean _print4;
        final boolean _print5;
        final boolean _print6;
        final boolean _print7;
        final String _questionNaireCd;
        final String _kubun;
        final String[] _classSelected;
        final String[] _majorSelected;
        final Map _majorName;
        final KNJSchoolMst _schoolMst;
        private boolean _seirekiFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKindA023 = request.getParameter("A023_SCHOOL_KIND");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _print1 = null != request.getParameter("PRINT1") ? true : false;
            _print2 = null != request.getParameter("PRINT2") ? true : false;
            _print3 = null != request.getParameter("PRINT3") ? true : false;
            _print4 = null != request.getParameter("PRINT4") ? true : false;
            _print5 = null != request.getParameter("PRINT5") ? true : false;
            _print6 = null != request.getParameter("PRINT6") ? true : false;
            _print7 = null != request.getParameter("PRINT7") ? true : false;
            _questionNaireCd = request.getParameter("QUESTIONNAIRECD");
            _kubun = request.getParameter("KUBUN");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _majorSelected = request.getParameterValues("MAJOR_SELECTED");
            _majorName = setMajorName(db2, _majorSelected);
            _schoolMst = new KNJSchoolMst(db2, _ctrlYear, setParamMap());
            setSeirekiFlg(db2);
        }

        private Map setParamMap() {
            final Map retMap = new HashMap();
            if ("1".equals(_useSchool_KindField)) {
                retMap.put("SCHOOLCD", _schoolCd);
                retMap.put("SCHOOL_KIND", _schoolKindA023);
            }
            return retMap;
        }

        private Map setMajorName(final DB2UDB db2, final String[] majorSelected) {
            final Map retMap = new HashMap();
            for (int i = 0; i < majorSelected.length; i++) {
                final String majorCd = majorSelected[i];
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     L1.COURSENAME || ' ' || T1.MAJORNAME AS MAJORNAME ");
                stb.append(" FROM ");
                stb.append("     MAJOR_MST T1 ");
                stb.append("     LEFT JOIN COURSE_MST L1 ON T1.COURSECD = L1.COURSECD ");
                stb.append(" WHERE ");
                stb.append("     T1.COURSECD || T1.MAJORCD = '" + majorCd + "' ");

                ResultSet rs = null;
                PreparedStatement ps = null;
                try {
                    ps = db2.prepareStatement(stb.toString());
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        retMap.put(majorCd, rs.getString("MAJORNAME"));
                    }
                } catch (SQLException e) {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            retMap.put("9999", "全学科");
            return retMap;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(date);
                }
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year) {
            if (null == year) {
                return "";
            }
            if (_seirekiFlg) {
                return year + "年";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年";
            }
        }

        public String getMajorName(final String majorCd) {
            return "学科：" + (String) _majorName.get(majorCd);
        }
    }
}

// eof
