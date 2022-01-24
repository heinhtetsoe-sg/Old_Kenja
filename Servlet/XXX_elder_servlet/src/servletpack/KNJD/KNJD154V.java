// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2009/08/20
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績個人票
 */

public class KNJD154V {

    private static final Log log = LogFactory.getLog(KNJD154V.class);

    private static final String SPECIAL_ALL = "999";
    private static final String HYOTEI_TESTCD = "9990009";
    private static final String GAKUNENHYOKA_TESTCD = "9990008";
    private static final String SEMEALL = "9";
    private static final String SSEMESTER = "1";
    private static final String SIDOU_INF_MARK = "1";
    private static final String SIDOU_INF_SCORE = "2";

    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasData = false;

        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
        sd.setSvfInit(request, response, svf);
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        log.debug(" $Revision: 77386 $");
        KNJServletUtils.debugParam(request, log);

        Param param = null;
        try {
            param = new Param(request, db2);

            KNJD154VFormBase knjd154vForm = new KNJD154VForm(param);

            final List<Student> students = createStudents(db2, param);

            for (final Student student : students) {
                log.debug(" student = " + student + "(" + student._attendNo + ")");

                knjd154vForm.print(db2, svf, student);

                hasData = true;
            }

            if (!hasData) {
                log.warn("データがありません");
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
            if (null != param) {
                param.close();
            }
        }

        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }

    private static String formatInt(final int n) {
        return n == 0 ? "" : String.valueOf(n);
    }

    private static String formatBigDecimal(final BigDecimal n) {
        return n == null ? null : (n.intValue() == 0) ? formatInt(0) : String.valueOf(n);
    }

    private static String getSubclasscd(final Param param, Map<String, String> row) throws SQLException {
        final String subclassCd;
        if ("1".equals(param._useCurriculumcd)) {
            subclassCd = KnjDbUtils.getString(row, "CLASSCD") + KnjDbUtils.getString(row, "SCHOOL_KIND") + KnjDbUtils.getString(row, "CURRICULUM_CD") + KnjDbUtils.getString(row, "SUBCLASSCD");
        } else {
            subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
        }
        return subclassCd;
    }

    private static String[] toArray(final List<String> list) {
        final String[] elems = new String[list.size()];
        list.toArray(elems);
        return elems;
    }

    private List<Student> createStudents(final DB2UDB db2, final Param param) {

        final List<Student> students = Student.getStudenList(db2, param);

        if (students.size() == 0) {
            log.warn("対象の生徒がいません");
            return students;
        }

        setAttendData(db2, students, param);
        Score.setScore(db2, students, param);
        Score.setSlump(db2, students, param);
        SpecialSubclassAttendance.setSpecialSubclassAttendData(students, param);
        AbsenceHigh.setAbsenceHigh(db2, students, param);
        SubclassInfo.setRecDetail(db2, students, param);
        if (!"1".equals(param._jusyoPrint)) {
            Address.setAddress(db2, students, param);
        }
        Student.setHexamRecordRemarkDat(db2, students, param);
        Student.setRecordDocumentKindDatFootnote(db2, students, param);
        Student.setTotalStudy(db2, students, param);

        return students;
    }

    /**
     * 出欠データをセットする。
     * @param db2
     * @param students
     * @param param
     */
    private void setAttendData(final DB2UDB db2, final List<Student> studentList, final Param param) {

        final Map<String, Student> students = new HashMap<String, Student>();
        for (final Student student : studentList) {
            students.put(student._schregno, student);
        }

        for (final String semester : param.getTargetSemester()) {

            final Semester semesS = param._semesterMap.get(param._semesterMap.containsKey(semester) ? semester : SEMEALL);
            final String sdate = param._isRuikei ? param._sdate : semesS._sdate;
            log.debug(sdate + " ～ " + " (" + param._edate + " , " + semesS);
            final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;

            Attendance.setAttendance(db2, students, param, semester, semesS, sdate, edate);

            SubclassAttendance.setAttendSubclass(db2, students, param, semester, semesS, sdate, edate);
        }
    }


    private static class Student {

        private String _attendNo;
        private String _gradeCd;
        private String _grade;
        private String _courseCd;
        private String _courseName;
        private String _majorCd;
        private String _majorName;
        private String _courseCode;
        private String _courseCodeName;
        private String _curriculumYear;
        private String _hrName;
        private String _hrNameAbbv;
        private String _name;

        final String _schregno;

        final List<SubclassInfo> _subclassInfos = new ArrayList<SubclassInfo>();

        private int _totalCredit = 0;

        final Map<String, Subclass> _subclassMap = new HashMap<String, Subclass>();
        final Map<String, Attendance> _attendMap = new TreeMap<String, Attendance>();
        final Map<String, AbsenceHigh> _spSubclassAbsenceHigh = new HashMap<String, AbsenceHigh>();
        final Map<String, Map<String, SpecialSubclassAttendance>> _specialGroupAttendanceMap = new HashMap<String, Map<String, SpecialSubclassAttendance>>();

        final Map<String, RecordTotalStudyTimeDat> _recordTotalStudieTimeDat = new HashMap();
        private String _hexamRecordRemarkRemark1;
        private String _recordDocumentKindDatFootnote;

        private Address _address;

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public Attendance getAttendance(final String semester) {
            final Attendance a = _attendMap.get(semester);
            return (a == null) ? new Attendance() : a;
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        private Subclass findSubclass(final String subclassCd) {
            if (null == getSubclass(subclassCd)) {
                _subclassMap.put(subclassCd, new Subclass(_schregno, subclassCd));
            }
            return _subclassMap.get(subclassCd);
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        public Subclass getSubclass(final String subclassCd) {
            return _subclassMap.get(subclassCd);
        }

        public SpecialSubclassAttendance getSpecialSubclassAttendance(final String specialGroupCd, final String testcd) {
            final Map<String, SpecialSubclassAttendance> map = createSpecialSubclassAttendanceMap(testcd);
            if (null == map.get(specialGroupCd)) {
                map.put(specialGroupCd, new SpecialSubclassAttendance(specialGroupCd));
            }
            return map.get(specialGroupCd);
        }

        public Map<String, SpecialSubclassAttendance> createSpecialSubclassAttendanceMap(final String testcd) {
            if (null == _specialGroupAttendanceMap.get(testcd)) {
                _specialGroupAttendanceMap.put(testcd, new HashMap<String, SpecialSubclassAttendance>());
            }
            return _specialGroupAttendanceMap.get(testcd);
        }

        public boolean isSogotekinaTankyunojikan(final Param param) {
            if (!"H".equals(param._schooLkind)) {
                return false;
            }
            final int tankyuStartYear = NumberUtils.isDigits(param._sogoTankyuStartYear) ? Integer.parseInt(param._sogoTankyuStartYear) : 2019;
            boolean isTankyu = false;
            final int year = NumberUtils.isDigits(param._year) ? Integer.parseInt(param._year) : 0;
            final int gradeCdInt = NumberUtils.isDigits(_gradeCd) ? Integer.parseInt(_gradeCd) : 0;
            if (NumberUtils.isDigits(_curriculumYear)) {
                isTankyu = Integer.parseInt(_curriculumYear) >= tankyuStartYear;
            } else {
                if (year == tankyuStartYear     && gradeCdInt <= 1
                 || year == tankyuStartYear + 1 && gradeCdInt <= 2
                 || year == tankyuStartYear + 2 && gradeCdInt <= 3
                 || year >= tankyuStartYear + 3
                        ) {
                    isTankyu = true;
                }
            }
            if (param._isOutputDebug) {
                log.info(" 探究? " + isTankyu + ", year = " + year + ", gradeCdInt = " + gradeCdInt + ", curriculumYear = " + _curriculumYear);
            }
            return isTankyu;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }

        static List<Student> getStudenList(final DB2UDB db2, final Param param) {
            final List<Student> students = new ArrayList<Student>();
            final String sql = sqlRegdData(param);
            // log.debug("sqlRegdData = " + sql);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"));
                students.add(student);

                student._grade = KnjDbUtils.getString(row, "GRADE");
                student._gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                student._courseCd = KnjDbUtils.getString(row, "COURSECD");
                student._majorCd = KnjDbUtils.getString(row, "MAJORCD");
                student._courseCode = KnjDbUtils.getString(row, "COURSECODE");

                student._courseName = KnjDbUtils.getString(row, "COURSENAME");
                student._courseCodeName = StringUtils.defaultString(KnjDbUtils.getString(row, "COURSECODENAME"));
                student._majorName = KnjDbUtils.getString(row, "MAJORNAME");
                student._hrName = StringUtils.defaultString(KnjDbUtils.getString(row, "HR_NAME"));
                student._hrNameAbbv = StringUtils.defaultString(KnjDbUtils.getString(row, "HR_NAMEABBV"));
                student._curriculumYear = KnjDbUtils.getString(row, "CURRICULUM_YEAR");
                final String attendNo = KnjDbUtils.getString(row, "ATTENDNO");
                student._attendNo = null == attendNo || !NumberUtils.isDigits(attendNo) ? "" : Integer.valueOf(attendNo) + "番";
                student._name = StringUtils.defaultString("1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME")) ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME"));

                //log.debug("対象の生徒" + student);
            }
            return students;
        }

        private static String sqlRegdData(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, GDAT.GRADE_CD, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, BASE.NAME, BASE.REAL_NAME, ");
            stb.append("            HDAT.HR_NAME, HDAT.HR_NAMEABBV, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("            T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append("            EGHIST.CURRICULUM_YEAR ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("        INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = '" + param._year + "' AND ");
            stb.append("                                          HDAT.SEMESTER = T1.SEMESTER AND ");
            stb.append("                                          HDAT.GRADE = T1.GRADE AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append("        INNER JOIN V_SEMESTER_GRADE_MST T2 ON T1.YEAR = T2.YEAR ");
            stb.append("            AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("            AND T1.GRADE = T2.GRADE ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ");
            stb.append("            AND T1.GRADE = GDAT.GRADE ");
            stb.append("        LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append("        LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append("        INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("        LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '04' ");
            stb.append("        LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON T1.SCHREGNO = EGHIST.SCHREGNO ");
            stb.append("            AND GDAT.SCHOOL_KIND = EGHIST.SCHOOL_KIND ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectSchregno) + " ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
            stb.append("               OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("              AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("ORDER BY T1.ATTENDNO");

            return stb.toString();
        }

        /**
         * 通知表所見
         * @param db2
         * @param students
         * @param param
         */
        private static void setHexamRecordRemarkDat(final DB2UDB db2, final List<Student> students, final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARK1");
            stb.append(" FROM ");
            stb.append("     HEXAM_RECORD_REMARK_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND REMARK_DIV = '4' ");

            final String sql = stb.toString();

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : students) {
                    student._hexamRecordRemarkRemark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static void setRecordDocumentKindDatFootnote(final DB2UDB db2, final List<Student> students, final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.FOOTNOTE ");
            stb.append(" FROM ");
            stb.append("     RECORD_DOCUMENT_KIND_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year +"' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' ");
            stb.append("     AND T1.HR_CLASS = '000' ");
            stb.append("     AND T1.COURSECD = '0' ");
            stb.append("     AND T1.MAJORCD = '000' ");
            stb.append("     AND T1.COURSECODE = '0000' ");
            stb.append("     AND T1.CLASSCD = '00' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schooLkind + "' ");
            stb.append("     AND T1.CURRICULUM_CD = '00' ");
            stb.append("     AND T1.SUBCLASSCD = '000000' ");
            stb.append("     AND T1.KIND_DIV = '1' ");

            final String footnote = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

            for (final Student student : students) {
                student._recordDocumentKindDatFootnote = footnote;
            }
        }

        private static void setTotalStudy(final DB2UDB db2, final List<Student> students, final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER ");
            stb.append("     ,TOTALSTUDYTIME ");
            stb.append("     ,TOTALSTUDYACT");
            stb.append(" FROM ");
            stb.append("     RECORD_TOTALSTUDYTIME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SCHOOL_KIND = '" + param._schooLkind + "' ");

            for (final Student student : students) {

                student._recordTotalStudieTimeDat.clear();

                final Map<String, RecordTotalStudyTimeDat> recordTotalStudys = new HashMap();
                for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString(), new Object[] {student._schregno})) {
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    final String totalStudyTime = KnjDbUtils.getString(row, "TOTALSTUDYTIME");
                    final String totalStudyAct = KnjDbUtils.getString(row, "TOTALSTUDYACT");

                    recordTotalStudys.put(semester, new RecordTotalStudyTimeDat(totalStudyTime, totalStudyAct));
                }
                student._recordTotalStudieTimeDat.putAll(recordTotalStudys);
            }
        }
    }

    private static class Address {
        final String _addressee;
        final String _address1;
        final String _address2;
        final String _zipcd;

        public Address(final String addressee, final String address1, final String address2, final String zipcd) {
            _addressee = addressee;
            _address1 = address1;
            _address2 = address2;
            _zipcd = zipcd;
        }

        /**
         * あて先住所をセットする。
         * @param db2
         * @param student
         * @param param
         */
        public static void setAddress(final DB2UDB db2, final Collection<Student> students, final Param param) {
            final StringBuffer stb = new StringBuffer();

            if ("1".equals(param._okurijouJusyo)) {
                stb.append(" SELECT T0.SCHREGNO, ");
                stb.append("        CASE WHEN T5.SCHREGNO IS NOT NULL THEN T0.REAL_NAME ELSE T0.NAME END AS ADDRESSEE, ");
                stb.append("        T4.ADDR1, T4.ADDR2, T4.ZIPCD ");
                stb.append(" FROM SCHREG_BASE_MST T0 ");
                stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) T3 ON ");
                stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                stb.append(" LEFT JOIN SCHREG_NAME_SETUP_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO AND T5.DIV = '04' ");
            } else if ("2".equals(param._okurijouJusyo)) {
                stb.append(" SELECT T0.SCHREGNO, T2.GUARD_NAME AS ADDRESSEE, T5.GUARD_NAME AS ADDRESSEE2, T4.GUARD_ADDR1 AS ADDR1, T4.GUARD_ADDR2 AS ADDR2, T4.GUARD_ZIPCD AS ZIPCD ");
                stb.append(" FROM SCHREG_BASE_MST T0 ");
                stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT WHERE '" + param._ctrlDate + "' BETWEEN ISSUEDATE AND EXPIREDATE GROUP BY SCHREGNO) T3 ON ");
                stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                stb.append(" LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                stb.append(" LEFT JOIN GUARDIAN_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._ctrlDate + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
            } else {
                stb.append(" SELECT T0.SCHREGNO, T2.SEND_NAME AS ADDRESSEE, T2.SEND_NAME AS ADDRESSEE2, T2.SEND_ADDR1 AS ADDR1, T2.SEND_ADDR2 AS ADDR2, T2.SEND_ZIPCD AS ZIPCD ");
                stb.append(" FROM SCHREG_BASE_MST T0 ");
                stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO AND T2.DIV = '1' ");
            }
            stb.append(" WHERE ");
            stb.append("     T0.SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Student student : students) {

                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        final String addressee = "2".equals(param._okurijouJusyo) && null != rs.getString("ADDRESSEE2") ? rs.getString("ADDRESSEE2") : rs.getString("ADDRESSEE");
                        final String addr1 = rs.getString("ADDR1");
                        final String addr2 = rs.getString("ADDR2");
                        final String zipcd = rs.getString("ZIPCD");
                        student._address = new Address(addressee, addr1, addr2, zipcd);
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("SQLException!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class Attendance {

        static final String GROUP_LHR = "001";
        static final String GROUP_ASS = "002";
        static final String GROUP_SHR = "004";

        final int _lesson;
        final int _mourning;
        final int _suspend;
        final int _abroad;
        final int _mlesson;
        final int _absence;
        final int _attend;
        final int _late;
        final int _leave;
        final int _virus;
        final int _koudome;

        Map _spGroupLessons = Collections.EMPTY_MAP;
        Map<String, Integer> _spGroupKekka = Collections.EMPTY_MAP;
        int _spLesson;
        int _spKekka;
        int _spShrKoma;

        Attendance() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int leave,
                final int virus,
                final int koudome
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _leave = leave;
            _virus = virus;
            _koudome = koudome;
        }

        public int getSpGroupKekka(final String groupCd) {
            final Integer kekka = _spGroupKekka.get(groupCd);
            return kekka == null ? 0 : kekka.intValue();
        }

        public String toString() {
            return "[lesson=" + _lesson +
            ",mlesson=" + _mlesson +
            ",mourning=" + _mourning +
            ",suspend=" + _suspend +
            ",abroad=" + _abroad +
            ",absence=" + _absence +
            ",attend=" + _attend +
            ",late=" + _late +
            ",leave=" + _leave;
        }

        private static void setAttendance(final DB2UDB db2,
                final Map<String, Student> students,
                final Param param,
                final String semester,
                final Semester semesS,
                final String sdate,
                final String edate) {

            final String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    semesS._cd,
                    sdate,
                    edate,
                    param._attendParamMap
            );

            // log.debug(" attend semes sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final Student student = students.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                    continue;
                }
                final int lesson = KnjDbUtils.getInt(row, "LESSON", 0);
                final int mourning = KnjDbUtils.getInt(row, "MOURNING", 0);
                final int suspend = KnjDbUtils.getInt(row, "SUSPEND", 0);
                final int abroad = KnjDbUtils.getInt(row, "TRANSFER_DATE", 0);
                final int mlesson = KnjDbUtils.getInt(row, "MLESSON", 0);
                final int absence = KnjDbUtils.getInt(row, "SICK", 0);
                final int attend = KnjDbUtils.getInt(row, "PRESENT", 0);
                final int late = KnjDbUtils.getInt(row, "LATE", 0);
                final int early = KnjDbUtils.getInt(row, "EARLY", 0);
                final int virus = KnjDbUtils.getInt(row, "VIRUS", 0);
                final int koudome = KnjDbUtils.getInt(row, "KOUDOME", 0);

                final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, virus, koudome);
                log.debug(" schregno = " + student._schregno + ", semester = " + semester + " , attendance = " + attendance);
                student._attendMap.put(semester, attendance);
            }
        }
    }

    private static class SubclassAttendance {

        static final int KETSUJI = 0;
        static final int CHIKOKU_SOUTAI = 1;
        static final int KOUKETSU = 2;
        static final int KIBIKI = 3;
        static final int SHUTTEI = 4;
        static String getName(final int div) {
            if (div == KETSUJI) return "欠時";
            if (div == CHIKOKU_SOUTAI) return "遅早";
            if (div == KOUKETSU) return "公欠";
            if (div == KIBIKI) return "忌引";
            if (div == SHUTTEI) return "出停";
            return null;
        }
        static int[] getPrintDivs(final Param param) {
            final int[] divs;
            if ("1".equals(param._koketsuKibikiShutteiNasi)) {
                divs = new int[] {KETSUJI, CHIKOKU_SOUTAI};
            } else {
                divs = new int[] {KETSUJI, CHIKOKU_SOUTAI, KOUKETSU, KIBIKI, SHUTTEI};
            }
            return divs;
        }

        final int _lesson;
        final BigDecimal _rawSick;
        final BigDecimal _sick;
        final int _absent;
        final int _suspend;
        final int _koudome;
        final int _virus;
        final int _mourning;
        final int _lateearly;
        final BigDecimal _rawReplacedSick;
        final BigDecimal _replacedSick;

        SubclassAttendance(final BigDecimal lesson, final BigDecimal rawSick, final BigDecimal sick, final BigDecimal absent, final BigDecimal suspend, final BigDecimal koudome, final BigDecimal virus, final BigDecimal mourning,
                final BigDecimal late, final BigDecimal early, final BigDecimal rawReplacedSick, final BigDecimal replacedSick) {
            _lesson = lesson.intValue();
            _rawSick = rawSick;
            _sick = sick;
            _absent = absent.intValue();
            _suspend = suspend.intValue();
            _koudome = koudome.intValue();
            _virus = virus.intValue();
            _mourning = mourning.intValue();
            _lateearly = late.add(early).intValue();
            _rawReplacedSick = rawReplacedSick;
            _replacedSick = replacedSick;
        }

        public String getLesson() {
            return String.valueOf(_lesson);
        }

        private String getKekkaString() {
            return "lesson = " + _lesson + " , rawSick = " + _rawSick + " , sick = " + _sick + " , absent = " + _absent + " , susmour = (" + _suspend + " , " + _mourning +
            ") , lateearly = " + _lateearly + ((_replacedSick.intValue() != 0) ? " , replacedSick = " + _replacedSick : "");
        }

        public String toString() {
            return getKekkaString();
        }

        public String getLateEarly() {
            return formatInt(_lateearly);
        }

        public String getKoketsu() {
            return formatInt(_absent);
        }

        public String getMourning() {
            return formatInt(_mourning);
        }

        public String getSuspend() {
            return formatInt(_suspend + _koudome + _virus);
        }

        public String getMourningSuspend() {
            return formatInt(_mourning + _suspend + _koudome + _virus);
        }

        private static void setAttendSubclass(final DB2UDB db2,
                final Map<String, Student> students,
                final Param param,
                final String semester,
                final Semester semesS,
                final String sdate,
                final String edate) {
            String sql = null;
            sql = AttendAccumulate.getAttendSubclassSql(
                    param._year,
                    semesS._cd,
                    sdate,
                    edate,
                    param._attendParamMap
            );

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql.toString())) {
                final Student student = students.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                    continue;
                }
                final String subclassCdC005 = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String subclassCd;
                if ("1".equals(param._useCurriculumcd)) {
                    final String[] split = StringUtils.split(KnjDbUtils.getString(row, "SUBCLASSCD"), "-");
                    subclassCd = split[0] + split[1] + split[2] + split[3];
                } else {
                    subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }

                final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "MLESSON", null);
                final BigDecimal rawSick = KnjDbUtils.getBigDecimal(row, "SICK1", null);
                final BigDecimal sick = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                final BigDecimal absent = KnjDbUtils.getBigDecimal(row, "ABSENT", null);
                final BigDecimal suspend = KnjDbUtils.getBigDecimal(row, "SUSPEND", null);
                final BigDecimal koudome = KnjDbUtils.getBigDecimal(row, "KOUDOME", null);
                final BigDecimal virus = KnjDbUtils.getBigDecimal(row, "VIRUS", null);
                final BigDecimal mourning = KnjDbUtils.getBigDecimal(row, "MOURNING", null);
                final BigDecimal late = "1".equals(param._chikokuHyoujiFlg) ? KnjDbUtils.getBigDecimal(row, "LATE", null) : KnjDbUtils.getBigDecimal(row, "LATE2", null);
                final BigDecimal early = "1".equals(param._chikokuHyoujiFlg) ? KnjDbUtils.getBigDecimal(row, "EARLY", null) :KnjDbUtils.getBigDecimal(row, "EARLY2", null);
                final BigDecimal rawReplacedSick = KnjDbUtils.getBigDecimal(row, "RAW_REPLACED_SICK", null);
                final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", null);

                final SubclassAttendance sa = new SubclassAttendance(lesson, rawSick, sick, absent, suspend, koudome, virus, mourning, late, early, rawReplacedSick, replacedSick);

                final String specialGroupCd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
                if (specialGroupCd != null) {
                    final int specialLessonMinutes = KnjDbUtils.getInt(row, "SPECIAL_LESSON_MINUTES", 0);

                    int spAbsenceMinutes = 0;
                    if (param._subClassC005.containsKey(subclassCdC005)) {
                        String is = (String) param._subClassC005.get(subclassCdC005);
                        if ("1".equals(is)) {
                            spAbsenceMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES3", 0);
                        } else if ("2".equals(is)) {
                            spAbsenceMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES2", 0);
                        }
                    } else {
                        spAbsenceMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES1", 0);
                    }

                    final SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);
                    ssa.add(subclassCd, lesson.intValue(), rawSick.intValue(), specialLessonMinutes, spAbsenceMinutes);
                }

                student.findSubclass(subclassCd)._attendMap.put(semester, sa);
                // log.debug("   schregno = " + student._schregno + ", semester = " + semester + " , subclasscd = " + subclassCd + " , spGroupCd = " + specialGroupCd + " , " + sa);

            }
        }

        public String getKind(final int idx) {
            switch (idx) {
            case KETSUJI: return null;
            case CHIKOKU_SOUTAI: return getLateEarly();
            case KOUKETSU: return getKoketsu();
            case KIBIKI: return getMourning();
            case SHUTTEI: return getSuspend();
            }
            return null;
        }
    }

    private static class SpecialSubclassAttendance {
        final Map<String, Integer> _spLessonMinutes;
        final Map<String, Integer> _spAbsenceMinutes;
        final Map<String, Integer> _spLessonKoma;
        final Map<String, Integer> _spAbsenceKoma;
        final String _spGroupCd;

        public SpecialSubclassAttendance(final String spGroupCd) {
            _spGroupCd = spGroupCd;
            _spLessonMinutes = new HashMap();
            _spAbsenceMinutes = new HashMap();
            _spLessonKoma = new HashMap();
            _spAbsenceKoma = new HashMap();
        }

        public void add(final String subclasscd, final int splessonKoma, final int spAbsenceKoma, final int splessonMinutes, final int spAbsenceMinutes) {
            add(_spLessonKoma, subclasscd, splessonKoma);
            add(_spAbsenceKoma, subclasscd, spAbsenceKoma);
            add(_spLessonMinutes, subclasscd, splessonMinutes);
            add(_spAbsenceMinutes, subclasscd, spAbsenceMinutes);
        }

        public int spLessonKomaTotal() {
            return mapValueTotal(_spLessonKoma);
        }

        public int spAbsenceKomaTotal() {
            return mapValueTotal(_spAbsenceKoma);
        }

        public int spLessonMinutesTotal() {
            return mapValueTotal(_spLessonMinutes);
        }

        public int spAbsenceMinutesTotal() {
            return mapValueTotal(_spAbsenceMinutes);
        }

        private static int mapValueTotal(final Map<String, Integer> map) {
            int total = 0;
            for (final Integer intnum : map.values()) {
                total += intnum.intValue();
            }
            return total;
        }

        private static void add(final Map<String, Integer> subclasscdIntMap, final String subclasscd, final int intnum) {
            if (!subclasscdIntMap.containsKey(subclasscd)) {
                subclasscdIntMap.put(subclasscd, new Integer(0));
            }
            final Integer intn = subclasscdIntMap.get(subclasscd);
            subclasscdIntMap.put(subclasscd, new Integer(intn.intValue() + intnum));
        }

        public String toString() {
            return " spGroupCd = " + _spGroupCd + " , spLessonMinutes = " + _spLessonMinutes + " , spAbsenceMinutes = " + _spAbsenceMinutes;
        }


        /**
         * 特活の出欠データをセットする。
         * @param student
         * @param param
         */
        private static void setSpecialSubclassAttendData(final Collection<Student> students, final Param param) {

            for (final Student student : students) {

                final List<String> semesters = param.getTargetSemester();

                final Map<String, Integer> spGroupLessons = new HashMap<String, Integer>();
                final Map<String, Integer> spGroupKekka = new HashMap<String, Integer>();

                for (final String semester : semesters) {

                    BigDecimal spLessonJisu = new BigDecimal(0);
                    BigDecimal spKekkaJisu = new BigDecimal(0);
                    int spShrKoma = 0;
                    final Map<String, SpecialSubclassAttendance> specialSubclassAttendanceMap = student.createSpecialSubclassAttendanceMap(semester);

                    for (final String specialGroupCd : specialSubclassAttendanceMap.keySet()) {
                        final SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);

                        final BigDecimal spGroupLessonJisu = getSpecialAttendExe(ssa.spLessonMinutesTotal(), param);
                        final BigDecimal spGroupAbsenceJisu = getSpecialAttendExe(ssa.spAbsenceMinutesTotal(), param);

                        spGroupLessons.put(specialGroupCd, new Integer(spGroupLessonJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
                        spGroupKekka.put(specialGroupCd, new Integer(spGroupAbsenceJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));

                        spLessonJisu = spLessonJisu.add(spGroupLessonJisu);
                        spKekkaJisu = spKekkaJisu.add(spGroupAbsenceJisu);
                        if (Attendance.GROUP_SHR.equals(specialGroupCd)) {
                            spShrKoma += ssa.spAbsenceKomaTotal();
                        }
                    }

                    final Attendance att = student.getAttendance(semester);
                    att._spLesson = spLessonJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    att._spKekka = spKekkaJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    att._spShrKoma = spShrKoma;
                    att._spGroupLessons = spGroupLessons;
                    att._spGroupKekka = spGroupKekka;
                }
            }
        }

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
            final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
            int hasu = 0;
            final String retSt = bigD.toString();
            final int retIndex = retSt.indexOf(".");
            if (retIndex > 0) {
                hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
            }
            final BigDecimal rtn;
            if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
                rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
            } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
            } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
            } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
                rtn = bigD;
            } else {
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            }
            return rtn;
        }
    }


    private static class Subclass {
        final String _schregno;
        final String _subclasscd;

        final Map<String, Score> _testScoreMap = new HashMap<String, Score>();
        final Map<String, SubclassAttendance> _attendMap = new HashMap<String, SubclassAttendance>();
        private AbsenceHigh _absenceHigh;

        public Subclass(
                final String schregno,
                final String subclasscd
        ) {
            _schregno = schregno;
            _subclasscd = subclasscd;
        }

        public String toString() {
            return _schregno + " : " + _subclasscd;
        }
    }

    private static class AbsenceHigh {
        final String _compAbsenceHigh;
        final String _getAbsenceHigh;

        public AbsenceHigh(final String absenceHigh, final String getAbsenceHigh) {
            _compAbsenceHigh = absenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
        }

        public boolean isRishuOver(final String kekka) {
            return isOver(kekka, _compAbsenceHigh);
        }

        public boolean isShutokuOver(final String kekka) {
            return isOver(kekka, _getAbsenceHigh);
        }

        private static boolean isOver(final String kekka, final String absenceHigh) {
            if (null == kekka || !NumberUtils.isNumber(kekka) || Double.parseDouble(kekka) == 0) {
                return false;
            }
            return absenceHigh == null || Double.parseDouble(absenceHigh) < Double.parseDouble(kekka);
        }

        public AbsenceHigh add(final AbsenceHigh o) {
            if (null == o) {
                return this;
            }
            return new AbsenceHigh(add(_compAbsenceHigh, o._compAbsenceHigh), add(_getAbsenceHigh, o._getAbsenceHigh));
        }

        private static final String add(final String num1, final String num2) {
            if (!NumberUtils.isNumber(num1)) return num2;
            if (!NumberUtils.isNumber(num2)) return num1;
            return new BigDecimal(num1).add(new BigDecimal(num2)).toString();
        }

        public String toString() {
            return " 履修上限値" + _compAbsenceHigh + " , 修得上限値" + _getAbsenceHigh;
        }

        /**
         * 欠課数上限値を得る。
         * @param db2
         * @param student
         * @param param
         */
        private static void setAbsenceHigh(final DB2UDB db2, final Collection<Student> students, final Param param) {
            PreparedStatement ps = null;

            final String absenceHighSql;
            final String spAbsenceHighSql;
            if (param._knjSchoolMst.isHoutei()) {
                absenceHighSql = sqlHouteiJisu(null, param, false);
                spAbsenceHighSql = sqlHouteiJisu(null, param, true);
            } else {
                absenceHighSql = sqlJituJisuSql(null, param, false);
                spAbsenceHighSql = sqlJituJisuSql(null, param, true);
            }

            try {
                ps = db2.prepareStatement(absenceHighSql);

                for (final Student student : students) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        if (KnjDbUtils.getString(row, "ABSENCE_HIGH") == null && KnjDbUtils.getString(row, "GET_ABSENCE_HIGH") == null) {
                            continue;
                        }
                        final String absenceHigh = KnjDbUtils.getString(row, "ABSENCE_HIGH") == null ? "0" : KnjDbUtils.getString(row, "ABSENCE_HIGH");
                        final String getAbsenceHigh = KnjDbUtils.getString(row, "GET_ABSENCE_HIGH") == null ? "0" : KnjDbUtils.getString(row, "GET_ABSENCE_HIGH");

                        final String subclassCd = getSubclasscd(param, row);
                        student.findSubclass(subclassCd)._absenceHigh = new AbsenceHigh(absenceHigh, getAbsenceHigh);
                    }
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }


            try {
                ps = db2.prepareStatement(spAbsenceHighSql);

                for (final Student student : students) {

                    student._spSubclassAbsenceHigh.clear();

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        final String compAbsenceHigh = StringUtils.defaultString(KnjDbUtils.getString(row, "ABSENCE_HIGH"), "0");
                        final String getAbsenceHigh = StringUtils.defaultString(KnjDbUtils.getString(row, "GET_ABSENCE_HIGH"), "0");

                        student._spSubclassAbsenceHigh.put(KnjDbUtils.getString(row, "SPECIAL_GROUP_CD"), new AbsenceHigh(compAbsenceHigh, getAbsenceHigh));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private static String sqlHouteiJisu(final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_RISHU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_SHUTOKU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.GRADE = T1.GRADE AND ");
            stb.append("       T2.COURSECD = T1.COURSECD AND ");
            stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
            stb.append("       T2.YEAR = T1.YEAR AND ");
            stb.append("       T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T2.SCHREGNO = ? ");
            return stb.toString();
        }

        private static String sqlJituJisuSql(final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
            final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("        AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("        AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
            if (!isGroup) {
                stb.append("       T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD = T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
            } else {
                stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
            }
            stb.append("       AND T3.COURSECD = T2.COURSECD ");
            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       AND T3.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.DIV = '2' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    private static class Score {

        String _testcd;
        String _score;
        String _passScore;
        String _gradeRank;
        String _gradeDeviation;
        String _classRank;
        String _classDeviation;
        String _courseRank;
        String _courseDeviation;
        String _majorRank;
        String _majorDeviation;
        String _assessLevel;
        String _courseAvg;
        String _hrAvg;
        String _gradeAvg;
        String _majorAvg;
        String _courseCnt;
        String _hrCnt;
        String _gradeCnt;
        String _majorCnt;
        String _slumpScore;
        String _slumpMark;

        public String getAvg(final Param param) {
            String avg = null;
            if ("1".equals(param._avgDiv)) {
                avg = _gradeAvg;
            } else if ("2".equals(param._avgDiv)) {
                avg = _hrAvg;
            } else if ("3".equals(param._avgDiv)) {
                avg = _courseAvg;
            } else if ("4".equals(param._avgDiv)) {
                avg = _majorAvg;
            }
            return avg;
        }

        public String getRank(final Param param) {
            String rank = null;
            if ("1".equals(param._avgDiv)) {
                rank = _gradeRank;
            } else if ("2".equals(param._avgDiv)) {
                rank = _classRank;
            } else if ("3".equals(param._avgDiv)) {
                rank = _courseRank;
            } else if ("4".equals(param._avgDiv)) {
                rank = _majorRank;
            }
            return rank;
        }

        public String getRankBunbo(final Param param) {
            String rank = null;
            if ("1".equals(param._avgDiv)) {
                rank = _gradeCnt;
            } else if ("2".equals(param._avgDiv)) {
                rank = _hrCnt;
            } else if ("3".equals(param._avgDiv)) {
                rank = _courseCnt;
            } else if ("4".equals(param._avgDiv)) {
                rank = _majorCnt;
            }
            return rank;
        }

        public String getDeviation(final Param param) {
            String deviation = null;
            if ("1".equals(param._avgDiv)) {
                deviation = _gradeDeviation;
            } else if ("2".equals(param._avgDiv)) {
                deviation = _classDeviation;
            } else if ("3".equals(param._avgDiv)) {
                deviation = _courseDeviation;
            } else if ("4".equals(param._avgDiv)) {
                deviation = _majorDeviation;
            }
            return deviation;
        }

        public String toString() {
            return " Score " + _testcd + " (" + _score + " , avg = [" + _courseAvg + " , " + _hrAvg + " , " + _gradeAvg + " , " + _majorAvg + "], slump = [" + _slumpScore + ", " + _slumpMark + "]) )";
        }

        /**
         * 素点、平均をセットする。
         * @param db2
         * @param student
         * @param param
         */
        private static void setScore(final DB2UDB db2, final Collection<Student> students, final Param param) {

            final String sql = sqlRecordScore(param);
            if (param._isOutputDebug) {
                log.info("setScoreValue sql = " + sql);
            }

            PreparedStatement ps = null;

            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : students) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        final String subclassCd = getSubclasscd(param, row);
                        final String testcd = KnjDbUtils.getString(row, "TESTCD");

                        if (null == student.findSubclass(subclassCd)._testScoreMap.get(testcd)) {
                            student.findSubclass(subclassCd)._testScoreMap.put(testcd, new Score());
                        }
                        final Score subScore = student.findSubclass(subclassCd)._testScoreMap.get(testcd);

                        subScore._testcd = testcd;
                        subScore._score = KnjDbUtils.getString(row, "SCORE");
                        subScore._passScore = KnjDbUtils.getString(row, "PASS_SCORE");
                        subScore._gradeRank = KnjDbUtils.getString(row, "GRADE_RANK");
                        subScore._gradeDeviation = KnjDbUtils.getString(row, "GRADE_DEVIATION");
                        subScore._classRank = KnjDbUtils.getString(row, "CLASS_RANK");
                        subScore._classDeviation = KnjDbUtils.getString(row, "CLASS_DEVIATION");
                        subScore._courseRank = KnjDbUtils.getString(row, "COURSE_RANK");
                        subScore._courseDeviation = KnjDbUtils.getString(row, "COURSE_DEVIATION");
                        subScore._majorRank = KnjDbUtils.getString(row, "MAJOR_RANK");
                        subScore._majorDeviation = KnjDbUtils.getString(row, "MAJOR_DEVIATION");
                        subScore._assessLevel = KnjDbUtils.getString(row, "ASSESS_LEVEL");
                        subScore._courseAvg = avgStr(KnjDbUtils.getBigDecimal(row, "COURSE_AVG", null));
                        subScore._hrAvg = avgStr(KnjDbUtils.getBigDecimal(row, "HR_AVG", null));
                        subScore._gradeAvg = avgStr(KnjDbUtils.getBigDecimal(row, "GRADE_AVG", null));
                        subScore._majorAvg = avgStr(KnjDbUtils.getBigDecimal(row, "MAJOR_AVG", null));
                        subScore._courseCnt = KnjDbUtils.getString(row, "COURSE_COUNT");
                        subScore._hrCnt = KnjDbUtils.getString(row, "HR_COUNT");
                        subScore._gradeCnt = KnjDbUtils.getString(row, "GRADE_COUNT");
                        subScore._majorCnt = KnjDbUtils.getString(row, "MAJOR_COUNT");

                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String avgStr(final BigDecimal bd) {
            return bd == null ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static String sqlRecordScore(final Param param) {

            final StringBuffer stb = new StringBuffer();

            stb.append("   WITH REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");
            stb.append("   SELECT  T1.YEAR, T1.SCHREGNO, ");
            stb.append("           T1.CLASSCD, ");
            stb.append("           T1.SCHOOL_KIND, ");
            stb.append("           T1.CURRICULUM_CD, ");
            stb.append("           T1.SUBCLASSCD, ");
            stb.append("           T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, ");
            stb.append("           T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("           T1.SCORE, ");
            stb.append("           PERF.PASS_SCORE, ");
            stb.append("           CASE WHEN VALUE(TRC.COUNT, 0) > 0 THEN ");
            stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("            FROM RELATIVEASSESS_MST L3 ");
            stb.append("            WHERE L3.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("              AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("              AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("              AND L3.CLASSCD = T1.CLASSCD ");
            stb.append("              AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("              AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           ) ELSE ");
            stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("            FROM ASSESS_MST L3 ");
            stb.append("            WHERE L3.ASSESSCD = '3' ");
            stb.append("              AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("           ) ");
            stb.append("           END AS ASSESS_LEVEL, ");
            stb.append("           T1.AVG, ");
            stb.append("           T1.GRADE_RANK, ");
            stb.append("           T1.GRADE_DEVIATION, ");
            stb.append("           T1.CLASS_RANK, ");
            stb.append("           T1.CLASS_DEVIATION, ");
            stb.append("           T1.COURSE_RANK, ");
            stb.append("           T1.COURSE_DEVIATION, ");
            stb.append("           T1.MAJOR_RANK, ");
            stb.append("           T1.MAJOR_DEVIATION, ");
            stb.append("           T3.AVG AS COURSE_AVG, ");
            stb.append("           T4.AVG AS HR_AVG, ");
            stb.append("           T5.AVG AS GRADE_AVG, ");
            stb.append("           T6.AVG AS MAJOR_AVG, ");
            stb.append("           T3.COUNT AS COURSE_COUNT, ");
            stb.append("           T4.COUNT AS HR_COUNT, ");
            stb.append("           T5.COUNT AS GRADE_COUNT, ");
            stb.append("           T6.COUNT AS MAJOR_COUNT ");
            stb.append("   FROM    RECORD_RANK_SDIV_DAT T1");
            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            // コース平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.SEMESTER = T1.SEMESTER AND T3.TESTKINDCD = T1.TESTKINDCD AND T3.TESTITEMCD = T1.TESTITEMCD AND T3.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("        AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T3.GRADE = T2.GRADE ");
            stb.append("        AND T3.AVG_DIV = '3' ");
            stb.append("        AND T3.HR_CLASS = '000' ");
            stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
            // クラス平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("        AND T4.SEMESTER = T1.SEMESTER AND T4.TESTKINDCD = T1.TESTKINDCD AND T4.TESTITEMCD = T1.TESTITEMCD AND T4.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("        AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T4.GRADE = T2.GRADE ");
            stb.append("        AND T4.AVG_DIV = '2' ");
            stb.append("        AND T4.HR_CLASS = T2.HR_CLASS ");
            stb.append("        AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
            // 学年平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("        AND T5.SEMESTER = T1.SEMESTER AND T5.TESTKINDCD = T1.TESTKINDCD AND T5.TESTITEMCD = T1.TESTITEMCD AND T5.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("        AND T5.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T5.GRADE = T2.GRADE ");
            stb.append("        AND T5.AVG_DIV = '1' ");
            stb.append("        AND T5.HR_CLASS = '000' ");
            stb.append("        AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = '00000000' ");
            // 学科平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("        AND T6.SEMESTER = T1.SEMESTER AND T6.TESTKINDCD = T1.TESTKINDCD AND T6.TESTITEMCD = T1.TESTITEMCD AND T6.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("        AND T6.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T6.GRADE = T2.GRADE ");
            stb.append("        AND T6.AVG_DIV = '4' ");
            stb.append("        AND T6.HR_CLASS = '000' ");
            stb.append("        AND T6.COURSECD || T6.MAJORCD || T6.COURSECODE = T2.COURSECD || T2.MAJORCD || '0000' ");
            stb.append("   LEFT JOIN REL_COUNT TRC ON TRC.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND TRC.CLASSCD = T1.CLASSCD ");
            stb.append("     AND TRC.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND TRC.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = T1.YEAR AND PERF.SEMESTER = T1.SEMESTER AND PERF.TESTKINDCD = T1.TESTKINDCD AND PERF.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("     AND PERF.CLASSCD = T1.CLASSCD ");
            stb.append("     AND PERF.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND PERF.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T2.GRADE END ");
            stb.append("     AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ");

            stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = ? ");
            stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN " + SQLUtils.whereIn(true, toArray(param.getTargetTestcds())));
            stb.append("       AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");

            return stb.toString();
        }

        /**
         * 素点、平均をセットする。
         * @param db2
         * @param student
         * @param param
         */
        private static void setSlump(final DB2UDB db2, final Collection<Student> students, final Param param) {

            //不振科目
            final String sql = sqlRecordSlump(param);

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : students) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        final String subclassCd = getSubclasscd(param, row);
                        final String testcd = KnjDbUtils.getString(row, "TESTCD");

                        if (null == student.findSubclass(subclassCd)._testScoreMap.get(testcd)) {
                            student.findSubclass(subclassCd)._testScoreMap.put(testcd, new Score());
                        }
                        final Score subScore = student.findSubclass(subclassCd)._testScoreMap.get(testcd);
                        subScore._testcd = testcd;
                        subScore._slumpScore = KnjDbUtils.getString(row, "SLUMP_SCORE");
                        subScore._slumpMark = KnjDbUtils.getString(row, "SLUMP_MARK_CD");
                    }
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String sqlRecordSlump(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("   WITH REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            stb.append("   SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("        ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = T1.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END AS SLUMP_SCORE ");
            stb.append("        ,MARK AS SLUMP_MARK_CD ");
            stb.append("        FROM RECORD_SLUMP_SDIV_DAT T1 ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   WHERE T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

        public boolean isSlump(final TestItem testItem, final Param param) {
            if (null != testItem._sidouInput) {
                if (SIDOU_INF_MARK.equals(testItem._sidouInputInf)) { // 記号
                    if (null != param._d054Namecd2Max && param._d054Namecd2Max.equals(_slumpMark)) {
                        return true;
                    }
                } else if (SIDOU_INF_SCORE.equals(testItem._sidouInputInf)) { // 得点
                    if (null != _slumpScore) {
                        return 1 == Integer.parseInt(_slumpScore);
                    }
                }
            }
            if ("09".equals(testItem._scoreDiv)) {
                return "1".equals(_score);
            }
            final boolean setPassScore = NumberUtils.isDigits(_passScore);
            return !setPassScore && "1".equals(_assessLevel) || setPassScore && NumberUtils.isDigits(_score) && Integer.parseInt(_score) < Integer.parseInt(_passScore);
        }
    }


    private static class Semester {
        final String _cd;
        final String _name;
        final String _sdate;
        final String _edate;
        final List<TestItem> _testItemList;
        Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
            _testItemList = new ArrayList<TestItem>();
        }

        public String toString() {
            return "(" + _name + " [" + _sdate + "," + _edate + "])";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final String _scoreDivName;
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf, final String scoreDivName) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _scoreDivName = scoreDivName;
        }
        public String getTestcd() {
            return _semester._cd +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._cd + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private static class RecordTotalStudyTimeDat {
        public final String _totalStudyTime;
        public final String _totalStudyAct;

        public RecordTotalStudyTimeDat(final String totalStudyTime, final String totalStudyAct) {
            _totalStudyTime = totalStudyTime;
            _totalStudyAct = totalStudyAct;
        }
    }

    private static class SubclassInfo {
        final String _classname;
        final String _classabbv;
        final String _subclassname;
        final String _credits;
        final String _compCredit;
        final String _getCredit;
        final String _subclassCd;
        final String _namespare1;
        final Subclass _subclass;

        final int _replaceflg;

        final String _calculateCreditFlg;

        final String _chairabbv;

        public SubclassInfo(
                final String classname,
                final String classabbv,
                final String subclassname,
                final String credits,
                final String compCredit,
                final String getCredit,
                final String subclassCd,
                final String namespare1,
                final Subclass subclass,
                final int replaceFlg,
                final String calculateCreditFlg,
                final String chairabbv
                ) {
            _classname = classname;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _credits = credits;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _subclassCd = subclassCd;
            _namespare1 = namespare1;
            _subclass = subclass;

            _replaceflg = replaceFlg;
            _calculateCreditFlg = calculateCreditFlg;
            _chairabbv = chairabbv;
        }

        public boolean isPrintCreditMstCredit() {
            if (_credits == null) {
                return false;
            }
            return 9 == _replaceflg && "1".equals(_calculateCreditFlg) || 9 != _replaceflg;
        }

        public boolean isPrintSubclassGradGrades(final Param param) {
            boolean rtn = true;
            if (1 == _replaceflg || 2 == _replaceflg) {
                rtn = false;
            }
            return rtn;
        }

        public String toString() {
            return _subclassCd + " : " + _subclassname;
        }

        /**
         * 成績データを得る。
         * @param db2
         * @param student
         * @param param
         */
        private static void setRecDetail (
                final DB2UDB db2,
                final Collection<Student> students,
                final Param param
        ) {
            PreparedStatement ps = null;
            try {
                final String sql = sqlSubclass(param);
                log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Student student : students) {

                    student._subclassInfos.clear();
                    student._totalCredit = 0;

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno, student._schregno, student._schregno})) {

                        final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                        final String classabbv = KnjDbUtils.getString(row, "CLASSABBV");
                        final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                        final String credits = KnjDbUtils.getString(row, "CREDITS");
                        final String compCredit = KnjDbUtils.getString(row, "COMP_CREDIT");
                        final String getCredit = KnjDbUtils.getString(row, "GET_CREDIT");
                        final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String namespare1 = KnjDbUtils.getString(row, "NAMESPARE1");
                        final String chairabbv = KnjDbUtils.getString(row, "CHAIRABBV");

                        final int replaceflg = KnjDbUtils.getInt(row, "REPLACEFLG", 0);
                        final String calculateCreditFlg = StringUtils.defaultString(KnjDbUtils.getString(row, "CALCULATE_CREDIT_FLG"), "0");

                        final String classCd = subclassCd.substring(0, 2);
                        final boolean isTarget = KNJDefineSchool.subject_D.compareTo(classCd) <= 0 && classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T);
                        if (!isTarget) {
                            continue;
                        }

                        final Subclass subclass = student.getSubclass(subclassCd);

                        final SubclassInfo info = new SubclassInfo(classname, classabbv, subclassname, credits, compCredit, getCredit, subclassCd,
                                namespare1, subclass, replaceflg, calculateCreditFlg, chairabbv);
                        student._subclassInfos.add(info);

                        if (null == info._getCredit || 1 == info._replaceflg || 2 == info._replaceflg) {
                        } else {
                            student._totalCredit += Integer.parseInt(info._getCredit);
                        }
                    }

                }

            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String sqlSubclass(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ");
            stb.append(" SUBCLASS_CREDITS AS(");
            stb.append("   SELECT ");
            stb.append("        CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        CREDITS, L1.NAMESPARE1 ");
            stb.append("   FROM   CREDIT_MST T1");
            stb.append("          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z011' ");
            stb.append("               AND L1.NAMECD2 = T1.REQUIRE_FLG ");
            stb.append("        , (SELECT  T3.GRADE, T3.COURSECD, T3.MAJORCD, T3.COURSECODE");
            stb.append("           FROM    SCHREG_REGD_DAT T3");
            stb.append("           WHERE   T3.SCHREGNO = ?");
            stb.append("               AND T3.YEAR = '" + param._year + "'");
            stb.append("               AND T3.GRADE || T3.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("               AND T3.SEMESTER = (SELECT  MAX(SEMESTER)");
            stb.append("                                  FROM    SCHREG_REGD_DAT T4");
            stb.append("                                  WHERE   T4.YEAR = '" + param._year + "'");
            stb.append("                                      AND T4.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("                                      AND T4.SCHREGNO = T3.SCHREGNO)");
            stb.append("          )T2 ");
            stb.append("   WHERE T1.YEAR = '" + param._year + "'");
            stb.append("     AND T1.GRADE = T2.GRADE");
            stb.append("     AND T1.COURSECD = T2.COURSECD");
            stb.append("     AND T1.MAJORCD = T2.MAJORCD");
            stb.append("     AND T1.COURSECODE = T2.COURSECODE");
            stb.append(" ) ");

            stb.append(" ,COMBINED_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            stb.append("        COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, ");
            stb.append("        MIN(ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD) AS ATTEND_SUBCLASSCD ");
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            stb.append("        COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            stb.append(" )");

            stb.append(" ,ATTEND_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            stb.append("        ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        MAX(PRINT_FLG1) AS PRINT_FLG, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, ");
            stb.append("        MAX(COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD ");
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            stb.append("        ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            stb.append(" )");

            stb.append(", CHAIR_A AS(");
            stb.append("   SELECT ");
            if (param._isMeikei) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
                stb.append("        T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("        '' AS CHAIRABBV ");
                stb.append("   FROM    RECORD_RANK_SDIV_DAT T1 ");
                stb.append("   WHERE   T1.SCHREGNO = ?");
                stb.append("       AND T1.YEAR = '" + param._year + "'");
                stb.append("       AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <= '" + param._testcd + "'");
                stb.append("   GROUP BY ");
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
                stb.append("        T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
            } else if (StringUtils.isNotBlank(param._useFormNameD154V) && "KNJD154V_2_3".equals(param._useFormNameD154V)) {
                stb.append("        T2.CLASSCD, ");
                stb.append("        T2.SCHOOL_KIND, ");
                stb.append("        T2.CURRICULUM_CD, ");
                stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("        T2.CHAIRABBV ");
                stb.append("   FROM    CHAIR_STD_DAT T1 ");
                stb.append("   INNER JOIN CHAIR_DAT T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER AND T1.CHAIRCD = T2.CHAIRCD ");
                stb.append("   WHERE   T1.SCHREGNO = ?");
                stb.append("       AND T1.YEAR = '" + param._year + "'");
                stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
                stb.append("   GROUP BY ");
                stb.append("        T2.CLASSCD, ");
                stb.append("        T2.SCHOOL_KIND, ");
                stb.append("        T2.CURRICULUM_CD, ");
                stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD, ");
                stb.append("        T2.CHAIRABBV ");
            } else {
                stb.append("        T2.CLASSCD, ");
                stb.append("        T2.SCHOOL_KIND, ");
                stb.append("        T2.CURRICULUM_CD, ");
                stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("        '' AS CHAIRABBV ");
                stb.append("   FROM    CHAIR_STD_DAT T1 ");
                stb.append("   INNER JOIN CHAIR_DAT T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER AND T1.CHAIRCD = T2.CHAIRCD ");
                stb.append("   WHERE   T1.SCHREGNO = ?");
                stb.append("       AND T1.YEAR = '" + param._year + "'");
                stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
                stb.append("   GROUP BY ");
                stb.append("        T2.CLASSCD, ");
                stb.append("        T2.SCHOOL_KIND, ");
                stb.append("        T2.CURRICULUM_CD, ");
                stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            }
            stb.append(" )");

            stb.append(", SUBCLASSNUM AS(");
            stb.append("   SELECT  SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,5,2) = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
            stb.append("         , SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,5,2) != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
            stb.append("   FROM    CHAIR_A S1");
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                stb.append(" LEFT JOIN (SELECT CLASSCD, SCHOOL_KIND, CLASSCD AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND N1.CLASS_SEQ = '003') T1 ON T1.CLASSCD = S1.CLASSCD AND T1.SCHOOL_KIND = S1.SCHOOL_KIND ");
            } else {
                stb.append(" LEFT JOIN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1='" + param._d008Namecd1 + "') T1 ON T1.NAMECD2 = SUBSTR(S1.SUBCLASSCD,5,2)");
            }
            stb.append(" )");

            stb.append(" SELECT  T2.SUBCLASSCD, T7.CLASSNAME, T7.CLASSABBV, VALUE(T4.SUBCLASSORDERNAME2,T4.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       , T6.CREDITS, T6.NAMESPARE1 ");
            stb.append("       , CASE WHEN T5.SUBCLASSCD IS NOT NULL AND T9.SUBCLASSCD IS NOT NULL THEN 2");
            stb.append("              WHEN T5.SUBCLASSCD IS NOT NULL THEN 9");
            stb.append("              WHEN T9.SUBCLASSCD IS NOT NULL THEN 1");
            stb.append("              ELSE 0 END AS REPLACEFLG");
            stb.append("       , T9.PRINT_FLG");
            stb.append("       , (SELECT NUMTOTAL FROM SUBCLASSNUM) AS NUMTOTAL");
            stb.append("       , CASE WHEN '90' = SUBSTR(T2.SUBCLASSCD, 5, 2) THEN 3 ");
            stb.append("              ELSE 1 END AS ORDER0");
            stb.append("       , CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN T9.COMBINED_SUBCLASSCD ELSE T2.SUBCLASSCD END AS ORDER1");
            stb.append("       , CASE WHEN T5.SUBCLASSCD IS NOT NULL THEN 1 WHEN T9.SUBCLASSCD IS NOT NULL THEN 2 ELSE 0 END AS ORDER2");
            stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
            stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
            stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
            stb.append("       , REC_SCORE.COMP_CREDIT ");
            stb.append("       , REC_SCORE.GET_CREDIT ");
            stb.append("       , REC_SCORE.ADD_CREDIT ");
            stb.append("       , T2.CHAIRABBV ");
            stb.append(" FROM    CHAIR_A T2");
            stb.append(" LEFT JOIN SUBCLASS_MST T4 ON ");
            stb.append("        T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T7 ON ");
            stb.append("        T7.CLASSCD || T7.SCHOOL_KIND = SUBSTR(T2.SUBCLASSCD,1,3)");
            stb.append(" LEFT JOIN SUBCLASS_CREDITS T6 ON T6.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5 ON T5.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN ATTEND_SUBCLASSCD T9 ON T9.SUBCLASSCD = T2.SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                stb.append(" LEFT JOIN (SELECT CLASSCD, SCHOOL_KIND, CLASSCD AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND N1.CLASS_SEQ ='003') N1 ON N1.CLASSCD = T2.CLASSCD AND N1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append(" LEFT JOIN NAME_MST N1 ON N1.NAMECD1='" + param._d008Namecd1 + "' AND N1.NAMECD2 = SUBSTR(T2.SUBCLASSCD,5,2)");
            }
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC_SCORE ON REC_SCORE.YEAR = '" + param._year + "' ");
            stb.append("       AND REC_SCORE.SCHREGNO = ? ");
            stb.append("        AND REC_SCORE.CLASSCD || REC_SCORE.SCHOOL_KIND || REC_SCORE.CURRICULUM_CD || REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD || REC_SCORE.SCORE_DIV = '" + HYOTEI_TESTCD + "' ");

            stb.append(" ORDER BY ORDER0, ORDER1, ORDER2, T2.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private static abstract class KNJD154VFormBase {

        protected static final String AMIKAKE_RISHU = "Paint=(1,60,1),Bold=1";
        protected static final String AMIKAKE_SHUTOKU = "Paint=(1,80,1),Bold=1";
        protected static final String AMIKAKE_FUSHIN = "Paint=(1,70,1),Bold=1";

        protected Param _param;

        protected KNJD154VFormBase(final Param param) {
            this._param = param;
        }

        public abstract void print(final DB2UDB db2, final Vrw32alp svf, final Student student);

        protected void printComment(final Vrw32alp svf) {
            if ("1".equals(_param._hanreiSyuturyokuNasi)) {
            } else {
                String comment = _param._useAbsenceWarn ? "注意" : "超過";
                svf.VrAttribute("MARK102",  AMIKAKE_RISHU);
                svf.VrsOut("MARK102",  " " );
                svf.VrsOut("NOTE2_2",  "　：未履修" + comment + ",特活進級" + comment );

                svf.VrAttribute("MARK103",  AMIKAKE_SHUTOKU);
                svf.VrsOut("MARK103",  " " );
                svf.VrsOut("NOTE2_3",  "　：未修得" + comment );

                svf.VrAttribute("MARK104",  AMIKAKE_FUSHIN);
                svf.VrsOut("MARK104",  " " );
                svf.VrsOut("NOTE2_4",  "　：不振");
            }
        }

        protected void setAbsenceFieldAttribute(
                final Vrw32alp svf,
                final AbsenceHigh absenceHigh,
                final String absent1,
                final String field,
                final Integer line
        ) {
            String amikake = null;
            if (absenceHigh == null) {
                if (NumberUtils.isNumber(absent1) && Double.parseDouble(absent1) != 0.0) {
                    amikake = AMIKAKE_RISHU;
                }
            } else if (absenceHigh.isRishuOver(absent1)) {
                amikake = AMIKAKE_RISHU;
            } else if (absenceHigh.isShutokuOver(absent1)) {
                amikake = AMIKAKE_SHUTOKU;
            }
            if (null != amikake) {
                if (null == line) {
                    svf.VrAttribute(field, amikake);
                } else {
                    svf.VrAttributen(field, line.intValue(), amikake);
                }
            }
        }

        private void printSubclassCompCredits (
                final Vrw32alp svf,
                final SubclassInfo si
        ) {
            String credits = si._compCredit;
            if (null == credits || "0".equals(credits)) { return; }

            if (1 == si._replaceflg || 2 == si._replaceflg) {
                if ("2".equals(si._calculateCreditFlg)) {
                    credits = "(" + credits + ")";
                    credits = credits.length() == 3 ? " " + credits : credits.length() == 2 ? "  " + credits : credits;
                    svf.VrsOut("R_CREDIT",  credits);
                }
            } else {
                credits = credits.length() == 1 ? "  " + credits : credits.length() == 2 ? " " + credits : credits;
                svf.VrsOut("R_CREDIT",  credits);
            }
        }

        protected static void svfVrsOutSeq(final Vrw32alp svf, final String s, final int keta, final int gyo, final String field) {
            final String[] token = KNJ_EditEdit.get_token(s, keta, gyo);
            if (token != null) {
                for (int i = 0; i < token.length; i++) {
                    svf.VrsOut(field + (i + 1), token[i]);
                }
            }
        }
    }

    private static class KNJD154VForm extends KNJD154VFormBase {

        static final int MAX_LINE = 17;

        public KNJD154VForm(final Param param) {
            super(param);
        }

        public void print(final DB2UDB db2, final Vrw32alp svf, final Student student) {

            String form;
            if (StringUtils.isNotBlank(_param._useFormNameD154V)) {
                if ("KNJD154V_2_5".equals(_param._useFormNameD154V) && "H".equals(_param._schooLkind)) {
                        form = "KNJD154V_2.frm";
                  } else {
                    form = _param._useFormNameD154V + ".frm";
                   }
            } else if (_param._isMeikei) {
                form = "KNJD154V_3.frm";
            } else if ("1".equals(_param._koketsuKibikiShutteiNasi)) {
                form = "KNJD154V_2.frm";
            } else {
                form = "KNJD154V.frm";
            }
            log.info(" form = " + form);
            svf.VrSetForm(form, 4);
            String form1 = setConfigForm(svf, form, student);
            if (!form.equals(form1)) {
                form = form1;
                svf.VrSetForm(form, 4);
            }

            printKotei(db2, svf, student);

            final List<SubclassInfo> printSubclassInfo = new ArrayList<SubclassInfo>();
            for (int i = 0; i < student._subclassInfos.size(); i++) {

                final SubclassInfo info = student._subclassInfos.get(i);
                if (_param._d046List.contains(info._subclassCd)) {
                    continue;
                }
                printSubclassInfo.add(info);
            }

            final List<TestItem> testItemAll = new ArrayList<TestItem>();
            for (final String semes : _param._semesterMap.keySet()) {
                final Semester semester = _param._semesterMap.get(semes);
                testItemAll.addAll(semester._testItemList);
            }
            final int subformWidth = 3246 - 1346;
            int width = 0;
            final String fieldDiv;
            final int testItemWidth;
            if (_param._isMeikei) {
                testItemWidth = 1424 - 1148;
                fieldDiv = "1";
            } else {
                testItemWidth = !"1".equals(_param._koketsuKibikiShutteiNasi) && testItemAll.size() > 5 ? (2224 - 2000) : testItemAll.size() > 4 ? (1960 - 1702) : (1622 - 1326);
                fieldDiv = !"1".equals(_param._koketsuKibikiShutteiNasi) && testItemAll.size() > 5 ? "3" : testItemAll.size() > 4 ? "2" : "1";
            }
            for (final TestItem testItem : testItemAll) {

                svf.VrsOut("SEMESTER" + fieldDiv, GAKUNENHYOKA_TESTCD.equals(testItem.getTestcd()) ? "年間" : testItem._semester._name);
                svf.VrsOut("TESTITEM" + fieldDiv, testItem._testitemname);
                svf.VrsOut("SCORE_NAME" + fieldDiv, testItem._scoreDivName);
                svf.VrsOut("AVE_NAME" + fieldDiv, "平均点");
                if (_param._isMeikei) {
                    svf.VrsOut("VAL_NAME" + fieldDiv, "偏差値");
                    svf.VrsOut("RANK_NAME" + fieldDiv, "順位");
                }

                if (_param.getTargetTestcds().contains(testItem.getTestcd())) {
                    String befClassabbv = null;
                    for (int subline = 0; subline < printSubclassInfo.size(); subline++) {

                        final SubclassInfo info = printSubclassInfo.get(subline);
                        final Subclass subclass = student.getSubclass(info._subclassCd);
                        printSubclassScore(svf, fieldDiv, subline, info, befClassabbv, subclass, testItem);
                        befClassabbv = info._classabbv;
                    }
                }

                printKotei(db2, svf, student);
                svf.VrEndRecord();
                width += testItemWidth;
            }


            final int[] divs = SubclassAttendance.getPrintDivs(_param);
            final String title = divs.length > 2 ? "科目の欠席等" : "欠席等";
            final int attendWidth = (2910 - 2810);
            for (int subattidx = 0; subattidx < divs.length; subattidx++) {
                final int div = divs[subattidx];
                svf.VrsOut("ABSENT_GRP", "A");
                svf.VrsOut("ABSENT_TITLE", StringUtils.center(title, 4 * divs.length / 2, '　').substring(4 * subattidx / 2));
                svf.VrsOut("ABSENT_NAME", SubclassAttendance.getName(div));
                String befClassabbv = null;
                for (int subline = 0; subline < printSubclassInfo.size(); subline++) {

                    final SubclassInfo info = printSubclassInfo.get(subline);
                    final Subclass subclass = student.getSubclass(info._subclassCd);
                    if (null != subclass) {
                        AbsenceHigh absenceHigh = subclass._absenceHigh;
                        if (null == absenceHigh) {
                            SubclassMst mst = SubclassMst.getSubclassMst(_param._subclassMstMap, subclass._subclasscd);
                            if (null != mst && mst._isSaki) {
                                for (final String attendSubclasscd : mst._attendSubclasscdSet) {
                                    SubclassMst mstat = SubclassMst.getSubclassMst(_param._subclassMstMap, attendSubclasscd);
                                    if (null != mstat) {
                                        final Subclass attendSubclass = student.getSubclass(mstat._subclasscd);
                                        if (null != attendSubclass && null != attendSubclass._absenceHigh) {
                                            absenceHigh = attendSubclass._absenceHigh.add(absenceHigh);
                                        }
                                    }
                                }
                            }
                        }
                        printSubclassAttendance(svf, subline, info, befClassabbv, absenceHigh, subclass._attendMap.get(_param._semester), subattidx);
                        befClassabbv = info._classabbv;
                    }
                }

                printKotei(db2, svf, student);
                svf.VrEndRecord();
                width += attendWidth;
            }
            if ("1".equals(_param._koketsuKibikiShutteiNasi)) {
                final int nokoriWidth = subformWidth - width;
                final int bikoWidth = (3070 - 2970);
                final int bikoColumns = nokoriWidth / bikoWidth;
                // log.debug(" bikoColumns = " + bikoColumns);
                for (int i = 0; i < bikoColumns; i++) {
                    svf.VrsOut("REM_GRP", "R");
                    svf.VrsOut("REM_GRP2", "R");
                    svf.VrsOut("REM_GRP3", "R");
                    svf.VrsOut("REM_TITLE", StringUtils.center("備考", 4 * bikoColumns / 2, '　').substring(4 * i / 2));
                    for (int j = 0; j < MAX_LINE; j++) {
                        svf.VrsOut("REMARK" + String.valueOf(j + 1), "_");
                    }
                    printKotei(db2, svf, student);
                    svf.VrEndRecord();
                }
            }
        }

        private void printKotei(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo);
            if (_param._isHanazono) {
                svf.VrsOut("SCHOOLNAME2", _param._schoolName);
            } else {
                svf.VrsOut("SCHOOLNAME", _param._schoolName);
            }
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            if (_param._isHanazono) {
                svf.VrsOut("JOB_NAME2_2", _param._hrJobName);
            } else {
                svf.VrsOut("JOB_NAME2", _param._hrJobName);
            }

            svf.VrsOut("PRESIDENT", _param._principalName);
            if (_param._staffNames.size() > 0) {
                if (_param._isHanazono) {
                    svf.VrsOut("TEACHER2", _param._staffNames.get(0));
                    if (_param._staffNames.size() > 1) {
                        svf.VrsOut("TEACHER2_2", _param._staffNames.get(1));
                    }
                } else {
                    svf.VrsOut("TEACHER", _param._staffNames.get(0));
                }
            }

            svf.VrsOut("COURSE",   student._courseCodeName);
            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("HR_NAME",  student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);

            if (null != _param._logoFile) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.getPath());
            }

            // 総合的な学習の時間所見
            final RecordTotalStudyTimeDat recordTotalStudy = student._recordTotalStudieTimeDat.get(SEMEALL);
            if (null != recordTotalStudy) {
                svfVrsOutSeq(svf, recordTotalStudy._totalStudyAct, 50, 2, "SP_CONENT");
                if (_param.isPrintSogotekinaGakushunoJikanHyoka()) {
                    svfVrsOutSeq(svf, recordTotalStudy._totalStudyTime, 50, 3, "SP_EVA");
                }
            }

            printSogotekinaGakushunoJikanAttendance(svf, student);

            // 通信欄
            svfVrsOutSeq(svf, student._hexamRecordRemarkRemark1, 60, 5, "CORRE");

            // 備考
            svfVrsOutSeq(svf, student._recordDocumentKindDatFootnote, 100, 5, "COMMUNICATION");

            printAddress(svf, student);

            printAttendance(svf, student);
            printComment(svf);
        }

        private void printAddress(final Vrw32alp svf, final Student student) {
            if ("1".equals(_param._jusyoPrint)) {
            } else {
                if (student._address != null) {
                    final String addressee = student._address._addressee == null ? "" : student._address._addressee + "  様";

                    final int check1 = KNJ_EditEdit.getMS932ByteLength(student._address._address1);
                    final int check2 = KNJ_EditEdit.getMS932ByteLength(student._address._address2);
                    final int use = ("1".equals(_param._useAddrField2) && (check1 > 50 || check2 > 50)) ? 3 : (check1 > 40 || check2 > 40) ? 2 : 1;
                        svf.VrsOut(use == 3 ? "ADDR1_3" : use == 2 ? "ADDR1_2" : "ADDR1", student._address._address1);     //住所
                        svf.VrsOut(use == 3 ? "ADDR2_3" : use == 2 ? "ADDR2_2" : "ADDR2", student._address._address2);     //住所
                        svf.VrsOut("ZIPCD", student._address._zipcd);
                    svf.VrsOut(KNJ_EditEdit.getMS932ByteLength(addressee) > 24 ? "ADDRESSEE2" : "ADDRESSEE", addressee);
                }
            }
            svf.VrsOut("HR_ATTNO_NAME", student._hrNameAbbv + "　" + student._attendNo + (_param._isNotPrintStudentNameWhenAddresseeIs2 ? "" : "　" + student._name));
        }

        private Map getSubclassInfoMap(final List subclassInfoList) {
            final Map map = new HashMap();
            if (null != subclassInfoList) {
                for (Iterator it = subclassInfoList.iterator(); it.hasNext();) {
                    final SubclassInfo si = (SubclassInfo) it.next();
                    map.put(si._subclassCd, si);
                }
            }
            return map;
        }

        private void printSogotekinaGakushunoJikanAttendance(final Vrw32alp svf, final Student student) {
            // 欠時数、欠課数： 科目コード頭2桁が90、合併先科目 + 合併設定なしの科目
            // 欠課数上限値：科目コード頭2桁が90、合併科目があるなら先科目の科目コードのMIN、ないなら合併設定なしの科目の科目コードのMIN)
            final Map subclassInfoMap = getSubclassInfoMap(student._subclassInfos);
            String minSubclassCd = null;
            int sick = 0;
            int rawSick = 0;
            int lateEarly = 0;
            int absent = 0;
            int mourning = 0;
            int suspend = 0;
            int koudome = 0;
            int virus = 0;
            boolean hasGappeiSubclasscd = false;
            for (final Iterator it = student._subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                if (null == subclassCd || !subclassCd.substring(0, 2).equals("90")) {
                    continue;
                }

                final SubclassInfo si = (SubclassInfo) subclassInfoMap.get(subclassCd);
                if (null != si) {
                    if (si._replaceflg == 9 && (!hasGappeiSubclasscd || null == minSubclassCd || si._subclassCd.compareTo(minSubclassCd) < 0)) {
                        minSubclassCd = si._subclassCd;
                        // log.debug(" 合併先 minSubclassCd = " + minSubclassCd);
                        hasGappeiSubclasscd = true;
                    }
                }
                if (!hasGappeiSubclasscd && (null == minSubclassCd || subclassCd.compareTo(minSubclassCd) < 0)) {
                    // log.debug(" 合併設定無し minSubclassCd = " + minSubclassCd);
                    minSubclassCd = subclassCd;
                }
                final Subclass subclass = student.getSubclass(subclassCd);
                if (null == subclass) {
                    continue;
                }
                final SubclassAttendance sa = subclass._attendMap.get(_param._semester);
                if (null == sa) {
                    continue;
                }
                if (null == si || si._replaceflg == 0) { // 合併設定無し
                    sick      += null == sa._sick ? 0 : sa._sick.intValue();
                    rawSick   += null == sa._rawSick ? 0 : sa._rawSick.intValue();
                    lateEarly += sa._lateearly;
                    absent    += sa._absent;
                    mourning  += sa._mourning;
                    suspend   += sa._suspend;
                    koudome   += sa._koudome;
                    virus     += sa._virus;
                } else if (9 == si._replaceflg) { // 合併先科目
                    sick      += null == sa._replacedSick ? 0 : sa._replacedSick.intValue();
                    rawSick   += null == sa._rawReplacedSick ? 0 : sa._rawReplacedSick.intValue();
                    lateEarly += sa._lateearly;
                    absent    += sa._absent;
                    mourning  += sa._mourning;
                    suspend   += sa._suspend;
                    koudome   += sa._koudome;
                    virus     += sa._virus;
                } else if (1 == si._replaceflg) { // 合併元科目
                }
            }
            final Subclass minSubclass = student.getSubclass(minSubclassCd);
            if (null != minSubclass) {
                setAbsenceFieldAttribute(svf, minSubclass._absenceHigh, String.valueOf(sick), "SP_ABSENCE", null);
                setAbsenceFieldAttribute(svf, minSubclass._absenceHigh, String.valueOf(sick), "SP_EARLY", null);
            }

            svf.VrsOut("SP_ABSENCE", String.valueOf(rawSick));
            svf.VrsOut("SP_EARLY", String.valueOf(lateEarly));
            if ("1".equals(_param._koketsuKibikiShutteiNasi)) {
            } else {
                svf.VrsOut("SP_PUB_ABSENCE", String.valueOf(absent));
                svf.VrsOut("SP_MOURNING", String.valueOf(mourning));
                svf.VrsOut("SP_SUSPEND", String.valueOf(suspend + koudome + virus));
            }
        }

        private void printAttendance(final Vrw32alp svf, final Student student) {
            final Attendance sum = student.getAttendance(_param._semester);

            svf.VrsOut("REC_LESSON" , String.valueOf(sum._lesson));
            svf.VrsOut("REC_MOURNING" , String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
            svf.VrsOut("REC_PRESENT", String.valueOf(sum._mlesson));
            svf.VrsOut("REC_ABSENCE", String.valueOf(sum._absence));
            svf.VrsOut("REC_ATTEND" , String.valueOf(sum._attend));

            {
                final String spGroupCd = Attendance.GROUP_LHR;
                final int spGroupKekka = sum.getSpGroupKekka(spGroupCd);
                final String field1 = "REC_LHR_ABSENCE";
                svf.VrsOut(field1, String.valueOf(spGroupKekka));
                final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd);

                if (ah == null) {
                    if (spGroupKekka != 0) {
                        svf.VrAttribute(field1, AMIKAKE_RISHU);
                    }
                } else if (ah.isRishuOver(String.valueOf(spGroupKekka))) {
                    svf.VrAttribute(field1, AMIKAKE_RISHU);
                }
            }

            final String[][] fields = new String[2][];
            fields[0] = new String[] {"ABSENCE_NAME1", "REC_ASSEMBLY_ABSENCE"};
            fields[1] = new String[] {"ABSENCE_NAME2", "REC_SHR_ABSENCE"};
            int idx = 0;
            if (!"1".equals(_param._gyojiKessekiNasi)) {
                svf.VrsOut(fields[idx][0], "学校行事の欠席時数");
                final String spGroupCd = Attendance.GROUP_ASS;
                final String field = fields[idx][1];
                final int spGroupKekka = sum.getSpGroupKekka(spGroupCd);
                svf.VrsOut(field, String.valueOf(spGroupKekka));
                final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd);
                if (ah == null) {
                    if (spGroupKekka != 0) {
                        svf.VrAttribute(field, AMIKAKE_RISHU);
                    }
                } else if (ah.isRishuOver(String.valueOf(spGroupKekka))) {
                    svf.VrAttribute(field, AMIKAKE_RISHU);
                }
                idx += 1;
            }
            if (!"1".equals(_param._shrKessekiNasi)) {
                svf.VrsOut(fields[idx][0], "SHRの欠席時数");
                final String spGroupCd = Attendance.GROUP_SHR;
                final String field = fields[idx][1];
                final int spKomaKekka = sum._spShrKoma;
                svf.VrsOut(field, String.valueOf(spKomaKekka));
                final int spGroupKekka = sum.getSpGroupKekka(spGroupCd);
                final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd);

                if (ah == null) {
                    if (spGroupKekka != 0) {
                        svf.VrAttribute(field, AMIKAKE_RISHU);
                    }
                } else if (ah.isRishuOver(String.valueOf(spGroupKekka))) {
                    svf.VrAttribute(field, AMIKAKE_RISHU);
                }
                idx += 1;
            }
        }


        private void printSubclassScore(
                final Vrw32alp svf,
                final String fieldDiv,
                final int subline,
                final SubclassInfo si,
                final String classabbv,
                final Subclass subclass,
                final TestItem testItem
        ) {
            final int i = subline + 1;
            if (null == classabbv || !classabbv.equals(si._classabbv)) {
                svf.VrsOutn("CLASS", i, si._classabbv);
            }
            if (StringUtils.isNotBlank(_param._useFormNameD154V) && "KNJD154V_2_3".equals(_param._useFormNameD154V)) {
                svf.VrsOutn("SUBCLASS" + (KNJ_EditEdit.getMS932ByteLength(si._subclassname) <= 20 ? "1" : KNJ_EditEdit.getMS932ByteLength(si._subclassname) <= 30 ? "2": "3"), i, si._subclassname);
                svf.VrsOutn("CHAIR_NAME" + (KNJ_EditEdit.getMS932ByteLength(si._chairabbv) <= 6 ? "1" : "2"), i, si._chairabbv);
            } else {
                svf.VrsOutn("SUBCLASS" + (KNJ_EditEdit.getMS932ByteLength(si._subclassname) > 26 ? "2" : "1"), i, si._subclassname);
            }

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOutn("CREDIT", i, si._credits);
            }

            if (subclass == null) {
                return;
            }

            if (!(0 == si._replaceflg  || 1 == si._replaceflg  || 9 == si._replaceflg)) {
                return;
            }

            final Score s = subclass._testScoreMap.get(testItem.getTestcd());
            if (s != null) {
                if (_param._isOutputDebug) {
                    log.info(" testcd " + testItem.getTestcd() + ", subclass " + subclass._subclasscd + " " + si._subclassname + " = " + s._score + " (passScore = " + s._passScore + ")");
                }
                if (s.isSlump(testItem, _param)) {
                    svf.VrAttributen("POINT" + fieldDiv, i, AMIKAKE_FUSHIN);
                }
                if (s._score != null) {
                    svf.VrsOutn("POINT" + fieldDiv, i, s._score);
                }
                if (!_param._isNotPrintAvg) {
                    svf.VrsOutn("AVE_POINT" + fieldDiv, i, s.getAvg(_param));
                }
                if (_param._isMeikei) {
                    svf.VrsOutn("VAL" + fieldDiv, i, s.getDeviation(_param));
                    if (null != s.getRank(_param) && !"".equals(s.getRank(_param))) {
                        svf.VrsOutn("RANK" + fieldDiv, i, s.getRank(_param) + "/" + s.getRankBunbo(_param));
                    }
                }
            }
        }

        private void printSubclassAttendance(final Vrw32alp svf,
                final int subline,
                final SubclassInfo si,
                final String classabbv,
                final AbsenceHigh absenceHigh,
                final SubclassAttendance sa,
                final int attidx) {

            final int i = subline + 1;
            if (null == classabbv || !classabbv.equals(si._classabbv)) {
                svf.VrsOutn("CLASS", i, si._classabbv);
            }

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOutn("CREDIT", i, si._credits);
            }

            if (sa == null) {
                return;
            }
            final String field = "ABSENCE";

            final String sick = (9 == si._replaceflg) ? formatBigDecimal(sa._replacedSick) : formatBigDecimal(sa._sick);
            if (attidx == SubclassAttendance.KETSUJI || attidx == SubclassAttendance.CHIKOKU_SOUTAI) {
                setAbsenceFieldAttribute(svf, absenceHigh, sick, field, new Integer(i));
            }

            final String v;
            if (attidx == SubclassAttendance.KETSUJI) {
                String rawSick = (9 == si._replaceflg) ? formatBigDecimal(sa._rawReplacedSick) : formatBigDecimal(sa._rawSick);
                if (rawSick == null) {
                    rawSick = "";
                } else if (NumberUtils.isNumber(rawSick)) {
                    rawSick = String.valueOf(new BigDecimal(rawSick).intValue());
                }
                v = StringUtils.defaultString(rawSick);
            } else {
                v = sa.getKind(attidx);
            }
            svf.VrsOutn(field, i, v);
        }

        /**
         * フォームを修正する
         * @param svf
         * @param formName
         * @param student
         * @return
         */
        public String setConfigForm(final Vrw32alp svf, String formName, final Student student) {
            final TreeMap<String, String> modifyFormFlgMap = new TreeMap<String, String>();

            final String FLG_SOGAKU_REPLACE = "FLG_SOGAKU_REPLACE";

            if (student.isSogotekinaTankyunojikan(_param)) {
                // 生徒が総合的な探究の時間なら「総合的な学習の時間」を「総合的な探究の時間」に置き換え
                modifyFormFlgMap.put(FLG_SOGAKU_REPLACE, "1");
            }

            if (modifyFormFlgMap.isEmpty()) {
                return formName;
            }
            String modifyFormKey = Param.mkString(modifyFormFlgMap, "|");
            if (!StringUtils.isEmpty(modifyFormKey)) {
                modifyFormKey = formName + "::" + modifyFormKey;
            }
            if (_param._isOutputDebug) {
                log.info(" check config form = " + modifyFormKey + ", (new? " + !_param.modifyFormNameMap().containsKey(modifyFormKey) + " / " + _param.modifyFormNameMap().get(modifyFormKey) + " / " + formName + ")");
            }
            if (StringUtils.isEmpty(modifyFormKey)) {
                return formName;
            }
            if (_param.modifyFormNameMap().containsKey(modifyFormKey)) {
                formName = _param.modifyFormNameMap().get(modifyFormKey);
                return formName;
            }

            File newFile = null;
            try {
                // 進学用
                final String formPath = svf.getPath(formName);
                final File formFile = new File(formPath);
                if (_param._isOutputDebug) {
                    log.info(" form path = " + formPath);
                }
                if (!formFile.exists()) {
                    log.warn("no form : " + formPath);
                } else {
                    SvfForm svfForm = new SvfForm(formFile);
                    //svfForm._debug = true;
                    if (svfForm.readFile()) {
                        if (modifyFormFlgMap.containsKey(FLG_SOGAKU_REPLACE)) {
                            final String replace = "総　合　的　な　学　習　の　時　間";
                            final String with = "総　合　的　な　探　究　の　時　間";
                            List<KoteiMoji> motoText = svfForm.getKoteiMojiListWithText(replace);
                            log.info(" motoText = " + motoText);
                            for (final KoteiMoji koteiMoji : motoText) {
                                svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(with));
                            }
                        }

                        newFile = svfForm.writeTempFile();
                    } else {
                        log.error("read file error: " + formPath);
                    }
                }
            } catch (Throwable e) {
                if (_param._isOutputDebug) {
                    log.error("throwed ", e);
                } else {
                    log.error("throwed " + e.getMessage());
                }
            }
            String newFormname = null;
            String newFormPath = null;
            if (null != newFile) {
                newFormname = newFile.getName();
                newFormPath = newFile.getAbsolutePath();
            }
            _param.modifyFormPathMap().put(modifyFormKey, newFormPath);
            _param.modifyFormNameMap().put(modifyFormKey, newFormname);
            if (null != newFormname && !newFormname.equals(formName)) {
                formName = newFormname;
            }
            return formName;
        }
    }

    private static class SubclassMst {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        boolean _isSaki;
        boolean _isMoto;
        final Set<String>_attendSubclasscdSet = new TreeSet<String>();
        final Set<String> _combinedSubclasscdSet = new TreeSet<String>();
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
        }
        public String toString() {
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }

        private static SubclassMst getSubclassMst(final Map<String, SubclassMst> subclassMstMap, final String subclasscd) {
            if (null == subclassMstMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null);
            }
            return subclassMstMap.get(subclasscd);
        }

        private static Map getSubclassMstMap(
                final DB2UDB db2,
                final String year
        ) {
            final Map subclassMstMap = new HashMap();
            String sql;
            sql = "";
            sql += " SELECT ";
            sql += " T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD, ";
            sql += " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ";
            sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME ";
            sql += " FROM SUBCLASS_MST T1 ";
            sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"));
                subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
            }

            sql = "";
            sql += " SELECT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
            sql += "     ,  COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
            sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + year + "' ";
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");
                final String combinedSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");

                final SubclassMst atsub = SubclassMst.getSubclassMst(subclassMstMap, attendSubclasscd);
                if (null == atsub) {
                    log.warn("not found attend subclass : " + attendSubclasscd);
                } else {
                    atsub._isMoto = true;
                    atsub._combinedSubclasscdSet.add(combinedSubclasscd);
                }

                final SubclassMst comsub = SubclassMst.getSubclassMst(subclassMstMap, combinedSubclasscd);
                if (null == comsub) {
                    log.warn("not found combined subclass : " + combinedSubclasscd);
                } else {
                    comsub._isSaki = true;
                    comsub._attendSubclasscdSet.add(attendSubclasscd);
                }
            }
            return subclassMstMap;
        }
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade_hr_class;

        final String _testcd;

        final String _sdate;
        final String _edate;
        final String _ctrlDate;
        final String _nendo;

        final boolean _isRuikei;

        final String[] _selectSchregno;

        /** 平均値区分 1:学年、2:ホームルームクラス、3:コース、4:学科 */
        final String _avgDiv;

        final String _schooLkind;
        final String _schoolName;
        final String _z010name1;
        final boolean _isMeikei;
        final boolean _isHanazono;
        final String _principalName;
        final String _jobName;
        final String _hrJobName;
        final String _schoolAddress;
        final String _schoolTelNo;

        final List<String> _staffNames;

        final KNJSchoolMst _knjSchoolMst;

        final boolean _isSeireki;

        /** 学期・テスト種別と考査名称のマップ */
        final List<TestItem> _testItem;

        final TreeMap<String, Semester> _semesterMap;

        /** 欠課数上限値 1:注意、2:超過 */
        final boolean _useAbsenceWarn;
        /** 凡例出力なし */
        final String _hanreiSyuturyokuNasi;
        /** 公欠・忌引・出停時数なし */
        final String _koketsuKibikiShutteiNasi;

        /** C005 */
        final Map _subClassC005;
//        final String _hitsuRishuNote;

        /** SHRの欠席時数なし */
        final String _shrKessekiNasi;
        /** 行事の欠席時数なし */
        final String _gyojiKessekiNasi;
        /** 住所・郵便番号を表示しない */
        final String _jusyoPrint;
        /** 送り状住所 1:生徒 2:保護者 */
        final String _okurijouJusyo;
        /** 送り状住所 2:保護者 の場合生徒名出力なし */
        final boolean _isNotPrintStudentNameWhenAddresseeIs2;

        final File _logoFile;

        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;
        /** 平均値を表示しない */
        final boolean _isNotPrintAvg;
        /** 単位マスタの警告数は単位が回数か */
        final boolean _absenceWarnIsUnitCount;
        /** 年間通算を表示 */
        final String _printNenkan;
        /** 名称マスタ「D046」 登録された学期に表示しない科目のリスト */
        final List _d046List;

        private String _d054Namecd2Max;
        private String _sidouHyoji;

        final String _attendEndDateSemester;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useAddrField2;
        final String _setSchoolKind;

        final Map _attendParamMap;
        final Map _subclassMstMap;
        final boolean _isOutputDebug;

        final String _useFormNameD154V;

        final String _d008Namecd1;

        final String _sogoTankyuStartYear;
        final Map _sessionCache = new HashMap();

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException, ParseException {

            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _testcd = request.getParameter("TEST_CD");
            _isRuikei = "1".equals(request.getParameter("DATE_DIV"));
            _edate = request.getParameter("DATE").replace('/', '-');
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            _selectSchregno = request.getParameterValues("CATEGORY_SELECTED");
            _avgDiv = request.getParameter("AVG_DIV");
            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _hanreiSyuturyokuNasi = request.getParameter("HANREI_SYUTURYOKU_NASI");
            _koketsuKibikiShutteiNasi = request.getParameter("KOKETSU_KIBIKI_SHUTTEI_NASI");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _shrKessekiNasi = request.getParameter("SHR_KESSEKI_NASI");
            _gyojiKessekiNasi = request.getParameter("GYOJI_KESSEKI_NASI");
            _jusyoPrint = request.getParameter("JUSYO_PRINT");
            _okurijouJusyo = request.getParameter("OKURIJOU_JUSYO");
            _isNotPrintStudentNameWhenAddresseeIs2 = !"1".equals(_okurijouJusyo) ? "1".equals(request.getParameter("NO_PRINT_STUDENT_NAME")) || "1".equals(request.getParameter("NO_PRINT_STUDENT_NAME2")) : true;
            _isNotPrintAvg = "1".equals(request.getParameter("AVG_PRINT"));
            _printNenkan = request.getParameter("PRINT_NENKAN");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAddrField2 = request.getParameter("useAddrField2");
            _setSchoolKind = request.getParameter("setSchoolKind");
            _sogoTankyuStartYear = request.getParameter("sogoTankyuStartYear");

            _useFormNameD154V = request.getParameter("useFormNameD154V");

            _z010name1 = loadNameMstZ010(db2);
            _isMeikei = "meikei".equals(_z010name1);
            _isHanazono = "hanazono".equals(_z010name1);
            _isSeireki = loadNameMstZ012(db2);
            _nendo = _isSeireki ? _year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _subClassC005 = loadNameMstC005(db2);
            _absenceWarnIsUnitCount = loadNameMstC042(db2);
            _d046List = loadNameMstD046(db2);


            _schooLkind = getSchoolKind(db2, _grade_hr_class.substring(0, 2));
            final Map knjSchoolMstParamMap = new HashMap();
            if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                knjSchoolMstParamMap.put("SCHOOL_KIND", _schooLkind);
            }
            _knjSchoolMst = new KNJSchoolMst(db2, _year, knjSchoolMstParamMap);

            _semesterMap = loadSemester(db2);
            _sdate = null == _semesterMap.get(SSEMESTER) ? null : _semesterMap.get(SSEMESTER)._sdate;
            final String siteiScoreDiv = _testcd.substring(_testcd.length() - 2);
            List<TestItem> testItem = getTestKindItemList(db2, siteiScoreDiv, _setSchoolKind);
            if (testItem.isEmpty()) {
                testItem = getTestKindItemList(db2, siteiScoreDiv, "00");
            }
            _testItem = testItem;
            setD054Namecd2Max(db2);
            final String documentRoot = request.getParameter("DOCUMENTROOT");
            final File logoFile = new File(documentRoot + "/" + loadControlMst(db2, "IMAGEPATH") + "/" + "SCHOOLLOGO." + loadControlMst(db2, "EXTENSION"));
            _logoFile = !logoFile.exists() ? null : logoFile;
            _attendEndDateSemester = loadAttendEdateSemester(db2);

            _staffNames = getStaffNames(db2, getRegdSemester());
            _schoolName = setCertifSchoolDat(db2, "SCHOOL_NAME");
            _jobName = setCertifSchoolDat(db2, "JOB_NAME");
            _principalName = setCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _hrJobName = setCertifSchoolDat(db2, "REMARK2");
            _schoolAddress = setCertifSchoolDat(db2, "REMARK4");
            _schoolTelNo = setCertifSchoolDat(db2, "REMARK5");
//            _hitsuRishuNote = setHitsuRishuNote(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade_hr_class.substring(0, 2));
            _attendParamMap.put("hrClass", _grade_hr_class.substring(2, 5));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _subclassMstMap = SubclassMst.getSubclassMstMap(db2, _year);

            final String tmpD008Cd = "D" + _setSchoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        public void close() {
            for (final String path : modifyFormPathMap().values()) {
                if (null == path) {
                    continue;
                }
                final File file = new File(path);
                if (_isOutputDebug) {
                    log.info(" file " + path + " " + file.exists());
                }
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD154V' AND NAME = '" + propName + "' "));
        }

        public boolean isPrintSogotekinaGakushunoJikanHyoka() {
            return "02".equals(_testcd.substring(1, 3)); // TESTKINDCD = '02'
        }

        public List<String> getTargetSemester() {
            final List<String> list = new ArrayList<String>();
            for (final String semester : _semesterMap.keySet()) {
                if (_semester.compareTo(semester) >= 0) {
                    list.add(semester);
                }
            }
            return list;
        }

        public List<String> getTargetTestcds() {
            final List<String> list = new ArrayList<String>();
            for (final TestItem testItem : _testItem) {
                if (_testcd.compareTo(testItem.getTestcd()) >= 0 || GAKUNENHYOKA_TESTCD.equals(testItem.getTestcd()) && "1".equals(_printNenkan)) {
                    list.add(testItem.getTestcd());
                }
            }
            return list;
        }

        private String getSchoolKind(final DB2UDB db2, final String grade) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' "));
        }

        private String setCertifSchoolDat(final DB2UDB db2, final String field) {
            String certifKindcd = "";
            if ("H".equals(_schooLkind)) {
                certifKindcd = "109";
            } else if ("J".equals(_schooLkind)) {
                certifKindcd = "110";
            }
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "' "));
        }

        private List<TestItem> getTestKindItemList(final DB2UDB db2, final String siteiScoreDiv, final String setSchoolKind) {
            final List<TestItem> list = new ArrayList<TestItem>();

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
            stb.append("   SELECT DISTINCT ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
            stb.append("   WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.CLASSCD || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-000000' ");
            stb.append("     AND T1.SCHOOL_KIND = '" +  setSchoolKind + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T1.TESTITEMNAME, ");
            stb.append("     T1.TESTITEMABBV1, ");
            stb.append("     T1.SIDOU_INPUT, ");
            stb.append("     T1.SIDOU_INPUT_INF, ");
            stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T12.NAME1 AS SCORE_DIV_NAME ");
            stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
            stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
            stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
            stb.append(" LEFT JOIN NAME_MST T12 ON T12.NAMECD1 = 'D053' ");
            stb.append("    AND T12.NAMECD2 = T1.SCORE_DIV ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-"学校校種"-00-000000を使用する
            stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
            stb.append("   AND (T1.SEMESTER <> '" + SEMEALL + "' ");
            stb.append("   AND T1.TESTKINDCD <> '99' ");
            stb.append("   AND T1.SCORE_DIV = '" + siteiScoreDiv + "' ");
            stb.append("   AND NOT (T1.SEMESTER <> '" + SEMEALL + "' AND T1.SCORE_DIV = '09') ");
            if ("1".equals(_printNenkan)) {
                stb.append("   OR (T1.SEMESTER = '" + SEMEALL + "' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '08') ");
            }
            stb.append("       ) ");
            stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

            log.debug(" testitem sql ="  + stb.toString());

            String adminSdivSubclasscd = null;
            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                adminSdivSubclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                final String scoreDiv = KnjDbUtils.getString(row, "SCORE_DIV");
                final String sidouInput = KnjDbUtils.getString(row, "SIDOU_INPUT");
                final String sidouInputInf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
                Semester semester = _semesterMap.get(KnjDbUtils.getString(row, "SEMESTER"));
                if (null == semester) {
                    continue;
                }
                final String testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                final String testitemabbv1 = KnjDbUtils.getString(row, "TESTITEMABBV1");
                final String scoreDivName = KnjDbUtils.getString(row, "SCORE_DIV_NAME");

                final TestItem testItem = new TestItem(
                        year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf, scoreDivName);
                semester._testItemList.add(testItem);
                list.add(testItem);
            }
            log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            log.info(" testcd = " + list);
            return list;
        }

        private void setD054Namecd2Max(final DB2UDB db2) {
            final String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ";
            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            _d054Namecd2Max = KnjDbUtils.getString(row, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(row, "NAME1");
        }

        public String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private TreeMap<String, Semester> loadSemester(final DB2UDB db2) {
            final TreeMap<String, Semester> semesterMap = new TreeMap<String, Semester>();
            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String cd = KnjDbUtils.getString(row, "SEMESTER");
                final String name = KnjDbUtils.getString(row, "SEMESTERNAME");
                final String sdate = KnjDbUtils.getString(row, "SDATE");
                final String edate = KnjDbUtils.getString(row, "EDATE");
                final Semester semester = new Semester(cd, name, sdate, edate);
                semesterMap.put(cd, semester);
            }

            return semesterMap;
        }

        private String loadNameMstZ010(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private boolean loadNameMstZ012(final DB2UDB db2) {
            return KNJ_EditDate.isSeireki(db2);
        }

        private Map loadNameMstC005(final DB2UDB db2) {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            final Map subClassC005 = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "SUBCLASSCD", "NAMESPARE1");
            log.debug("(名称マスタC005):科目コード" + subClassC005);
            return subClassC005;
        }

        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private boolean loadNameMstC042(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
            boolean absenceWarnIsUnitCount = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            log.debug("(名称マスタ C042) =" + absenceWarnIsUnitCount);
            return absenceWarnIsUnitCount;
        }

        private String loadAttendEdateSemester(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND (('" + _edate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR ('" + _edate + "' BETWEEN T1.EDATE AND VALUE(T2.SDATE, '9999-12-30'))) ");

            return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "SEMESTER");
        }

        private String loadControlMst(final DB2UDB db2, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + field + " FROM CONTROL_MST WHERE CTRL_NO = '01' "));
        }

        public List<String> getStaffNames(final DB2UDB db2, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List<String> list = new LinkedList<String>();
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  (SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD1) AS TR_NAME1 ");
                stb.append("       ,(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD2) AS TR_NAME2 ");
                stb.append("       ,(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD3) AS TR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append("    AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
                stb.append("    AND T1.SEMESTER = '" + semester + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("TR_NAME1") != null) list.add(rs.getString("TR_NAME1"));
                    if (rs.getString("TR_NAME2") != null) list.add(rs.getString("TR_NAME2"));
                    if (rs.getString("TR_NAME3") != null) list.add(rs.getString("TR_NAME3"));
                }
            } catch (Exception ex) {
                log.error("List Staff_name() Staff_name error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private List loadNameMstD046(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '008' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D046' AND " + field + " = '1'  ");
            }
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD");
        }

        private Map<String, String> modifyFormNameMap() {
            return getMappedMap(getMappedHashMap(_sessionCache, "MODIFY_FORM_MAP"), "NAME");
        }

        private Map<String, String> modifyFormPathMap() {
            return getMappedMap(getMappedHashMap(_sessionCache, "MODIFY_FORM_MAP"), "PATH");
        }

        public static <K, T, U> Map<T, U> getMappedHashMap(final Map<K, Map<T, U>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap<T, U>());
            }
            return map.get(key1);
        }

        public static <K, T, U> Map<T, U> getMappedMap(final Map<K, Map<T, U>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap<T, U>());
            }
            return map.get(key1);
        }

        public static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<T>());
            }
            return map.get(key1);
        }

        public static String mkString(final Collection<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String comma0 = "";
            for (final String s : list) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                stb.append(comma0).append(s);
                comma0 = comma;
            }
            return stb.toString();
        }

        public static String mkString(final TreeMap<String, String> map, final String comma) {
            final List<String> list = new ArrayList<String>();
            for (final Map.Entry<String, String> e : map.entrySet()) {
                if (StringUtils.isEmpty(e.getKey()) || StringUtils.isEmpty(e.getValue())) {
                    continue;
                }
                list.add(e.getKey() + "=" + e.getValue());
            }
            return mkString(list, comma);
        }
    }
}
