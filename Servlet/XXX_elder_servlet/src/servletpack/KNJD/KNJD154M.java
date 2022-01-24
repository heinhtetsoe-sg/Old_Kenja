// kanji=漢字
/*
 * $Id: f9de79469a90a80a3f38c7118374ba0db046336a $
 *
 * 作成日: 2018/10/16
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
import java.util.Arrays;
import java.util.Collection;
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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績個人票
 */

public class KNJD154M {

    private static final Log log = LogFactory.getLog(KNJD154M.class);

    private static final String HYOTEI_TESTCD = "9990009";
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
        sd.setSvfInit( request, response, svf);
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        log.info(" $Revision: 75223 $");
        KNJServletUtils.debugParam(request, log);

        try {
           Param _param = new Param(request, db2);

            KNJD154MFormBase knjd154mForm = new KNJD154MForm(_param);

            final List students = createStudents(db2, _param);

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.info(" student = " + student + "(" + student._attendNo + ")");

                knjd154mForm.print(db2, svf, student);

                hasData = true;
            }

            if (!hasData) {
                log.warn("データがありません");
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
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

    private static String getSubclasscd(final Param param, ResultSet rs) throws SQLException {
        final String subclassCd;
        if ("1".equals(param._useCurriculumcd)) {
            subclassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");
        } else {
            subclassCd = rs.getString("SUBCLASSCD");
        }
        return subclassCd;
    }

    private static String[] toArray(final List list) {
        final String[] elems = new String[list.size()];
        list.toArray(elems);
        return elems;
    }

    private List createStudents(final DB2UDB db2, final Param param) {

        final List students = Student.getStudenList(db2, param);

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

        return students;
    }

    /**
     * 出欠データをセットする。
     * @param db2
     * @param students
     * @param param
     */
    private void setAttendData(final DB2UDB db2, final List students, final Param param) {

        final List targetSemesters = param.getTargetSemester();
        for (final Iterator it = targetSemesters.iterator(); it.hasNext();) {

            final String semester = (String) it.next();
            final Semester semesS = (Semester) param._semesterMap.get(param._semesterMap.containsKey(semester) ? semester : SEMEALL);
            final String sdate = param._isRuikei ? param._sdate : semesS._sdate;
            log.debug(sdate + " 〜 " + " (" + param._edate + " , " + semesS);
            final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;

            Attendance.setAttendance(db2, students, param, semester, semesS, sdate, edate);

            SubclassAttendance.setAttendSubclass(db2, students, param, semester, semesS, sdate, edate);
        }
    }


    private static class Student {

        private String _attendNo;
        private String _grade;
        private String _courseCd;
        private String _courseName;
        private String _majorCd;
        private String _majorName;
        private String _courseCode;
        private String _courseCodeName;
        private String _hrName;
        private String _hrNameAbbv;
        private String _name;

        final String _schregno;

        final List _subclassInfos = new ArrayList();

        private int _totalCredit = 0;

        final Map _subclassMap = new HashMap();
        final Map _attendMap = new TreeMap();
        final Map _spSubclassAbsenceHigh = new HashMap();
        final Map _specialGroupAttendanceMap = new HashMap();

        private Address _address;

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public Attendance getAttendance(final String semester) {
            final Attendance a = (Attendance) _attendMap.get(semester);
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
            return (Subclass) _subclassMap.get(subclassCd);
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        public Subclass getSubclass(final String subclassCd) {
            return (Subclass) _subclassMap.get(subclassCd);
        }

        public SpecialSubclassAttendance getSpecialSubclassAttendance(final String specialGroupCd, final String testcd) {
            final Map map = createSpecialSubclassAttendanceMap(testcd);
            if (null == map.get(specialGroupCd)) {
                map.put(specialGroupCd, new SpecialSubclassAttendance(specialGroupCd));
            }
            return (SpecialSubclassAttendance) map.get(specialGroupCd);
        }

        public Map createSpecialSubclassAttendanceMap(final String testcd) {
            if (null == _specialGroupAttendanceMap.get(testcd)) {
                _specialGroupAttendanceMap.put(testcd, new HashMap());
            }
            return (Map) _specialGroupAttendanceMap.get(testcd);
        }

        public String toString() {
            return _schregno + ":" + _name;
        }

        static Student getStudent(final String schregno, final List students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        static List getStudenList(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List students = new ArrayList();
            try {
                final String sql = sqlRegdData(param);
                // log.debug("sqlRegdData = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final Student student = new Student(rs.getString("SCHREGNO"));
                    students.add(student);

                    student._grade = rs.getString("GRADE");
                    student._courseCd = rs.getString("COURSECD");
                    student._majorCd = rs.getString("MAJORCD");
                    student._courseCode = rs.getString("COURSECODE");

                    student._courseName = rs.getString("COURSENAME");
                    student._courseCodeName = StringUtils.defaultString(rs.getString("COURSECODENAME"));
                    student._majorName = rs.getString("MAJORNAME");
                    student._hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                    student._hrNameAbbv = StringUtils.defaultString(rs.getString("HR_NAMEABBV"));
                    final String attendNo = rs.getString("ATTENDNO");
                    student._attendNo = null == attendNo || !NumberUtils.isDigits(attendNo) ? "" : Integer.valueOf(attendNo) + "番";
                    student._name = StringUtils.defaultString("1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"));

                    //log.debug("対象の生徒" + student);
                }
            } catch (Exception ex) {
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

        private static String sqlRegdData(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("    FROM    SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectSchregno) + " ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
            stb.append("               OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("              AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("    ) ");

            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T2.HR_NAMEABBV,");
            stb.append("        T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("        T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append("        T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append("                                          T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("                                          T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("        LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append("        LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append("        LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '04' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
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
        public static void setAddress(final DB2UDB db2, final Collection students, final Param param) {
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

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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
                final List students,
                final Param param,
                final String semester,
                final Semester semesS,
                final String sdate,
                final String edate) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        semesS._cd,
                        sdate,
                        edate,
                        param._attendParamMap
                );

                // log.debug(" attend semes sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final int lesson = rs.getInt("LESSON");
                    final int mourning = rs.getInt("MOURNING");
                    final int suspend = rs.getInt("SUSPEND");
                    final int abroad = rs.getInt("TRANSFER_DATE");
                    final int mlesson = rs.getInt("MLESSON");
                    final int absence = rs.getInt("SICK");
                    final int attend = rs.getInt("PRESENT");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int virus = rs.getInt("VIRUS");
                    final int koudome = rs.getInt("KOUDOME");

                    final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, virus, koudome);
                    log.debug(" schregno = " + student._schregno + ", semester = " + semester + " , attendance = " + attendance);
                    student._attendMap.put(semester, attendance);
                }

            } catch (SQLException e) {
                log.error("sql exception!", e);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
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

        private static void setAttendSubclass(final DB2UDB db2,
                final List students,
                final Param param,
                final String semester,
                final Semester semesS,
                final String sdate,
                final String edate) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;
            try {
                sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        semesS._cd,
                        sdate,
                        edate,
                        param._attendParamMap
                );

                // log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclassCdC005 = rs.getString("SUBCLASSCD");
                    final String subclassCd;
                    if ("1".equals(param._useCurriculumcd)) {
                        final String[] split = StringUtils.split(rs.getString("SUBCLASSCD"), "-");
                        subclassCd = split[0] + split[1] + split[2] + split[3];
                    } else {
                        subclassCd = rs.getString("SUBCLASSCD");
                    }

                    final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                    final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                    final BigDecimal sick = rs.getBigDecimal("SICK2");
                    final BigDecimal absent = rs.getBigDecimal("ABSENT");
                    final BigDecimal suspend = rs.getBigDecimal("SUSPEND");
                    final BigDecimal koudome = rs.getBigDecimal("KOUDOME");
                    final BigDecimal virus = rs.getBigDecimal("VIRUS");
                    final BigDecimal mourning = rs.getBigDecimal("MOURNING");
                    final BigDecimal late = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("LATE") : rs.getBigDecimal("LATE2");
                    final BigDecimal early = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("EARLY") :rs.getBigDecimal("EARLY2");
                    final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                    final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                    final SubclassAttendance sa = new SubclassAttendance(lesson, rawSick, sick, absent, suspend, koudome, virus, mourning, late, early, rawReplacedSick, replacedSick);

                    final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                    if (specialGroupCd != null) {
                        final int specialLessonMinutes = rs.getInt("SPECIAL_LESSON_MINUTES");

                        int spAbsenceMinutes = 0;
                        if (param._subClassC005.containsKey(subclassCdC005)) {
                            String is = (String) param._subClassC005.get(subclassCdC005);
                            if ("1".equals(is)) {
                                spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES3");
                            } else if ("2".equals(is)) {
                                spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES2");
                            }
                        } else {
                            spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES1");
                        }

                        final SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);
                        ssa.add(subclassCd, lesson.intValue(), rawSick.intValue(), specialLessonMinutes, spAbsenceMinutes);
                    }

                    student.findSubclass(subclassCd)._attendMap.put(semester, sa);
                    // log.debug("   schregno = " + student._schregno + ", semester = " + semester + " , subclasscd = " + subclassCd + " , spGroupCd = " + specialGroupCd + " , " + sa);

                }
            } catch (SQLException e) {
                log.error("sql exception! sql = " + sql, e);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
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
        final Map _spLessonMinutes;
        final Map _spAbsenceMinutes;
        final Map _spLessonKoma;
        final Map _spAbsenceKoma;
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

        public int spAbsenceKomaTotal() {
            return mapValueTotal(_spAbsenceKoma);
        }

        public int spLessonMinutesTotal() {
            return mapValueTotal(_spLessonMinutes);
        }

        public int spAbsenceMinutesTotal() {
            return mapValueTotal(_spAbsenceMinutes);
        }

        private static int mapValueTotal(final Map map) {
            int total = 0;
            for (final Iterator its = map.values().iterator(); its.hasNext();) {
                final Integer intnum = (Integer) its.next();
                total += intnum.intValue();
            }
            return total;
        }

        private static void add(final Map subclasscdIntMap, final String subclasscd, final int intnum) {
            if (!subclasscdIntMap.containsKey(subclasscd)) {
                subclasscdIntMap.put(subclasscd, new Integer(0));
            }
            final Integer intn = (Integer) subclasscdIntMap.get(subclasscd);
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
        private static void setSpecialSubclassAttendData(final Collection students, final Param param) {

            for (final Iterator its = students.iterator(); its.hasNext();) {
                final Student student = (Student) its.next();

                final List semesters = param.getTargetSemester();

                final Map spGroupLessons = new HashMap();
                final Map spGroupKekka = new HashMap();

                for (final Iterator itseme = semesters.iterator(); itseme.hasNext();) {
                    final String semester = (String) itseme.next();

                    BigDecimal spLessonJisu = new BigDecimal(0);
                    BigDecimal spKekkaJisu = new BigDecimal(0);
                    int spShrKoma = 0;
                    final Map specialSubclassAttendanceMap = student.createSpecialSubclassAttendanceMap(semester);

                    for (final Iterator it = specialSubclassAttendanceMap.keySet().iterator(); it.hasNext();) {
                        final String specialGroupCd = (String) it.next();
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
        final String _subclassCd;

        final Map _testScoreMap = new HashMap();
        final Map _attendMap = new HashMap();
        private AbsenceHigh _absenceHigh;

        public Subclass(
                final String schregno,
                final String subclassCd
        ) {
            _schregno = schregno;
            _subclassCd = subclassCd;
        }

        public SubclassAttendance getAttendance(final String semester) {
            return (SubclassAttendance) _attendMap.get(semester);
        }

        public String toString() {
            return _schregno + " : " + _subclassCd;
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
        private static void setAbsenceHigh(final DB2UDB db2, final Collection students, final Param param) {
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

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                    	if (rs.getString("ABSENCE_HIGH") == null && rs.getString("GET_ABSENCE_HIGH") == null) {
                    		continue;
                    	}
                        final String absenceHigh = rs.getString("ABSENCE_HIGH") == null ? "0" : rs.getString("ABSENCE_HIGH");
                        final String getAbsenceHigh = rs.getString("GET_ABSENCE_HIGH") == null ? "0" : rs.getString("GET_ABSENCE_HIGH");

                        final String subclassCd = getSubclasscd(param, rs);
                        student.findSubclass(subclassCd)._absenceHigh = new AbsenceHigh(absenceHigh, getAbsenceHigh);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }


            try {
                ps = db2.prepareStatement(spAbsenceHighSql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._spSubclassAbsenceHigh.clear();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        final String compAbsenceHigh = StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "0");
                        final String getAbsenceHigh = StringUtils.defaultString(rs.getString("GET_ABSENCE_HIGH"), "0");

                        student._spSubclassAbsenceHigh.put(rs.getString("SPECIAL_GROUP_CD"), new AbsenceHigh(compAbsenceHigh, getAbsenceHigh));
                    }
                    DbUtils.closeQuietly(rs);
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
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
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
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ");
                    }
                    stb.append("     T1.SUBCLASSCD = '" + subclassCd + "' ");
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
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
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
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD = T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
                } else {
                    stb.append("       T3.SUBCLASSCD = T1.SUBCLASSCD ");
                }
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
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ");
                    }
                    stb.append("     T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    private static class Score {

        String _testcd;
        String _score;
        String _gradeRank;
        String _gradeDeviation;
        String _classRank;
        String _classDeviation;
        String _courseRank;
        String _courseDeviation;
        String _majorRank;
        String _majorDeviation;
        String _passScore;
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
        private static void setScore(final DB2UDB db2, final Collection students, final Param param) {

            final String sql = sqlRecordScore(param);
            log.debug("setScoreValue sql = " + sql);

            PreparedStatement ps = null;

            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = getSubclasscd(param, rs);
                        final String testcd = rs.getString("TESTCD");

                        if (null == student.findSubclass(subclassCd)._testScoreMap.get(testcd)) {
                            student.findSubclass(subclassCd)._testScoreMap.put(testcd, new Score());
                        }
                        final Score subScore = (Score) student.findSubclass(subclassCd)._testScoreMap.get(testcd);

                        subScore._testcd = testcd;
                        subScore._score = rs.getString("SCORE");
                        subScore._gradeRank = rs.getString("GRADE_RANK");
                        subScore._gradeDeviation = rs.getString("GRADE_DEVIATION");
                        subScore._classRank = rs.getString("CLASS_RANK");
                        subScore._classDeviation = rs.getString("CLASS_DEVIATION");
                        subScore._courseRank = rs.getString("COURSE_RANK");
                        subScore._courseDeviation = rs.getString("COURSE_DEVIATION");
                        subScore._majorRank = rs.getString("MAJOR_RANK");
                        subScore._majorDeviation = rs.getString("MAJOR_DEVIATION");
                        subScore._passScore = rs.getString("PASS_SCORE");
                        subScore._assessLevel = rs.getString("ASSESS_LEVEL");
                        subScore._courseAvg = avgStr(rs.getBigDecimal("COURSE_AVG"));
                        subScore._hrAvg = avgStr(rs.getBigDecimal("HR_AVG"));
                        subScore._gradeAvg = avgStr(rs.getBigDecimal("GRADE_AVG"));
                        subScore._majorAvg = avgStr(rs.getBigDecimal("MAJOR_AVG"));
                        subScore._courseCnt = rs.getString("COURSE_COUNT");
                        subScore._hrCnt = rs.getString("HR_COUNT");
                        subScore._gradeCnt = rs.getString("GRADE_COUNT");
                        subScore._majorCnt = rs.getString("MAJOR_COUNT");

                    }
                    DbUtils.closeQuietly(rs);
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("   ) ");
            stb.append("   SELECT  T1.YEAR, T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           T1.CLASSCD, ");
                stb.append("           T1.SCHOOL_KIND, ");
                stb.append("           T1.CURRICULUM_CD, ");
            }
            stb.append("           T1.SUBCLASSCD, ");
            stb.append("           T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, ");
            stb.append("           T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("           T1.SCORE, ");
            stb.append("           CASE WHEN VALUE(TRC.COUNT, 0) > 0 THEN ");
            stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("            FROM RELATIVEASSESS_MST L3 ");
            stb.append("            WHERE L3.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("              AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("              AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND L3.CLASSCD = T1.CLASSCD ");
                stb.append("          AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           ) ELSE ");
            stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("            FROM ASSESS_MST L3 ");
            stb.append("            WHERE L3.ASSESSCD = '3' ");
            stb.append("              AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("           ) ");
            stb.append("           END AS ASSESS_LEVEL, ");
            stb.append("           PERF.PASS_SCORE, ");
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T3.GRADE = T2.GRADE ");
            stb.append("        AND T3.AVG_DIV = '3' ");
            stb.append("        AND T3.HR_CLASS = '000' ");
            stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
            // クラス平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("        AND T4.SEMESTER = T1.SEMESTER AND T4.TESTKINDCD = T1.TESTKINDCD AND T4.TESTITEMCD = T1.TESTITEMCD AND T4.SCORE_DIV = T1.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T4.GRADE = T2.GRADE ");
            stb.append("        AND T4.AVG_DIV = '2' ");
            stb.append("        AND T4.HR_CLASS = T2.HR_CLASS ");
            stb.append("        AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
            // 学年平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("        AND T5.SEMESTER = T1.SEMESTER AND T5.TESTKINDCD = T1.TESTKINDCD AND T5.TESTITEMCD = T1.TESTITEMCD AND T5.SCORE_DIV = T1.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T5.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T5.GRADE = T2.GRADE ");
            stb.append("        AND T5.AVG_DIV = '1' ");
            stb.append("        AND T5.HR_CLASS = '000' ");
            stb.append("        AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = '00000000' ");
            // 学科平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("        AND T6.SEMESTER = T1.SEMESTER AND T6.TESTKINDCD = T1.TESTKINDCD AND T6.TESTITEMCD = T1.TESTITEMCD AND T6.SCORE_DIV = T1.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T6.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T6.GRADE = T2.GRADE ");
            stb.append("        AND T6.AVG_DIV = '4' ");
            stb.append("        AND T6.HR_CLASS = '000' ");
            stb.append("        AND T6.COURSECD || T6.MAJORCD || T6.COURSECODE = T2.COURSECD || T2.MAJORCD || '0000' ");
            stb.append("   LEFT JOIN REL_COUNT TRC ON TRC.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND TRC.CLASSCD = T1.CLASSCD ");
                stb.append("     AND TRC.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND TRC.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("   LEFT JOIN PERFECT_RECORD_SDIV_DAT PERF ON PERF.YEAR = T1.YEAR ");
            stb.append("       AND PERF.SEMESTER = T1.SEMESTER ");
            stb.append("       AND PERF.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("       AND PERF.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("       AND PERF.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("       AND PERF.CLASSCD = T1.CLASSCD ");
            stb.append("       AND PERF.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("       AND PERF.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T2.GRADE END ");
            stb.append("       AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ");
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
        private static void setSlump(final DB2UDB db2, final Collection students, final Param param) {

            //不振科目
            final String sql = sqlRecordSlump(param);

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = getSubclasscd(param, rs);
                        final String testcd = rs.getString("TESTCD");

                        if (null == student.findSubclass(subclassCd)._testScoreMap.get(testcd)) {
                            student.findSubclass(subclassCd)._testScoreMap.put(testcd, new Score());
                        }
                        final Score subScore = (Score) student.findSubclass(subclassCd)._testScoreMap.get(testcd);
                        subScore._testcd = testcd;
                        subScore._slumpScore = rs.getString("SLUMP_SCORE");
                        subScore._slumpMark = rs.getString("SLUMP_MARK_CD");
                    }
                    DbUtils.closeQuietly(rs);
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("   ) ");

            stb.append("   SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, T1.SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , T1.CLASSCD ");
                stb.append("     , T1.SCHOOL_KIND ");
                stb.append("     , T1.CURRICULUM_CD ");
            }
            stb.append("        ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND L3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
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
            if (NumberUtils.isNumber(_score) && NumberUtils.isDigits(_passScore)) {
            	return Double.parseDouble(_score) < Integer.parseInt(_passScore);
            }
            return "1".equals(_assessLevel);
        }
    }


    private static class Semester {
        final String _cd;
        final String _name;
        final String _sdate;
        final String _edate;
        final List _testItemList;
        Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
            _testItemList = new ArrayList();
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
                final String calculateCreditFlg
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
        }

        public boolean isPrintCreditMstCredit() {
            if (_credits == null) {
                return false;
            }
            return 9 == _replaceflg && "1".equals(_calculateCreditFlg) || 9 != _replaceflg;
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
                final Collection students,
                final Param param
        ) {
            PreparedStatement ps = null;
            try {
                final String sql = sqlSubclass(param);
                log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._subclassInfos.clear();
                    student._totalCredit = 0;
                    int pp = 0;
                    ps.setString(++pp, student._schregno);
                    ps.setString(++pp, student._schregno);
                    ps.setString(++pp, student._schregno);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {

                        final String classname = rs.getString("CLASSNAME");
                        final String classabbv = rs.getString("CLASSABBV");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String credits = rs.getString("CREDITS");
                        final String compCredit = rs.getString("COMP_CREDIT");
                        final String getCredit = rs.getString("GET_CREDIT");
                        final String subclassCd = rs.getString("SUBCLASSCD");
                        final String namespare1 = rs.getString("NAMESPARE1");

                        final int replaceflg = rs.getInt("REPLACEFLG");
                        final String calculateCreditFlg = StringUtils.defaultString(rs.getString("CALCULATE_CREDIT_FLG"), "0");

                        final String classCd = subclassCd.substring(0, 2);
                        final boolean isTarget = KNJDefineSchool.subject_D.compareTo(classCd) <= 0 && classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T);
                        if (!isTarget) {
                            continue;
                        }

                        final Subclass subclass = student.getSubclass(subclassCd);

                        final SubclassInfo info = new SubclassInfo(classname, classabbv, subclassname, credits, compCredit, getCredit, subclassCd,
                                namespare1, subclass, replaceflg, calculateCreditFlg);
                        student._subclassInfos.add(info);

                        if (null == info._getCredit || 1 == info._replaceflg || 2 == info._replaceflg) {
                        } else {
                            student._totalCredit += Integer.parseInt(info._getCredit);
                        }
                    }

                    DbUtils.closeQuietly(rs);

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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("        SUBCLASSCD, ");
            }
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("        COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            }
            stb.append("        MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        MIN(ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD) AS ATTEND_SUBCLASSCD ");
            } else {
                stb.append("        MIN(ATTEND_SUBCLASSCD) AS ATTEND_SUBCLASSCD ");
            }
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("        COMBINED_SUBCLASSCD ");
            }
            stb.append(" )");

            stb.append(" ,ATTEND_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("        ATTEND_SUBCLASSCD AS SUBCLASSCD, ");
            }
            stb.append("        MAX(PRINT_FLG1) AS PRINT_FLG, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        MAX(COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD ");
            } else {
                stb.append("        MAX(COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD ");
            }
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("        ATTEND_SUBCLASSCD ");
            }
            stb.append(" )");

            stb.append(", CHAIR_A AS(");
            stb.append("   SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
                stb.append("        T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("        T1.SUBCLASSCD ");
            }
            stb.append("   FROM    RECORD_RANK_SDIV_DAT T1 ");
            stb.append("   WHERE   T1.SCHREGNO = ?");
            stb.append("       AND T1.YEAR = '" + param._year + "'");
            stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("   GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
                stb.append("        T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
            } else {
                stb.append("        T1.SUBCLASSCD ");
            }
            stb.append(" )");

            stb.append(", SUBCLASSNUM AS(");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   SELECT  SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,5,2) = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
                stb.append("         , SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,5,2) != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
            } else {
                stb.append("   SELECT  SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
                stb.append("         , SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,1,2) != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
            }
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
            //stb.append("       , N1.NAMECD2 AS NUM90_OTHER");
            //stb.append("       , (SELECT NUM90 FROM SUBCLASSNUM) AS NUM90");
            stb.append("       , (SELECT NUMTOTAL FROM SUBCLASSNUM) AS NUMTOTAL");
            stb.append("       , CASE WHEN '90' = SUBSTR(T2.SUBCLASSCD, 5, 2) THEN 3 ");
            // stb.append("              WHEN N1.NAMECD2 IS NOT NULL THEN 2 ");
            stb.append("              ELSE 1 END AS ORDER0");
            stb.append("       , CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN T9.COMBINED_SUBCLASSCD ELSE T2.SUBCLASSCD END AS ORDER1");
            stb.append("       , CASE WHEN T5.SUBCLASSCD IS NOT NULL THEN 1 WHEN T9.SUBCLASSCD IS NOT NULL THEN 2 ELSE 0 END AS ORDER2");
            stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
            stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
            stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
            stb.append("       , REC_SCORE.COMP_CREDIT ");
            stb.append("       , REC_SCORE.GET_CREDIT ");
            stb.append("       , REC_SCORE.ADD_CREDIT ");
            //stb.append("       , T10.STAFFNAME ");
            stb.append(" FROM    CHAIR_A T2");
            stb.append(" LEFT JOIN SUBCLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD = T2.SUBCLASSCD");
            } else {
                stb.append("        T4.SUBCLASSCD = T2.SUBCLASSCD ");
            }
            stb.append(" LEFT JOIN CLASS_MST T7 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T7.CLASSCD || T7.SCHOOL_KIND = SUBSTR(T2.SUBCLASSCD,1,3)");
            } else {
                stb.append("        T7.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2) ");
            }
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND REC_SCORE.CLASSCD || REC_SCORE.SCHOOL_KIND || REC_SCORE.CURRICULUM_CD || REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD");
            } else {
                stb.append("        AND REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD ");
            }
            stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD || REC_SCORE.SCORE_DIV = '" + HYOTEI_TESTCD + "' ");

            stb.append(" ORDER BY ORDER0, ORDER1, ORDER2");
            return stb.toString();
        }
    }

    private static abstract class KNJD154MFormBase {

        protected static final String AMIKAKE_RISHU = "Paint=(1,60,1),Bold=1";
        protected static final String AMIKAKE_SHUTOKU = "Paint=(1,80,1),Bold=1";
        protected static final String AMIKAKE_FUSHIN = "Paint=(1,70,1),Bold=1";

        protected Param _param;

        protected KNJD154MFormBase(final Param param) {
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

        protected static int getMS932ByteLength(final String name) {
            int len = 0;
            if (null != name) {
                try {
                    len = name.getBytes("MS932").length;
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return len;
        }
    }

    private static class KNJD154MForm extends KNJD154MFormBase {

        static final int MAX_LINE = 17;

        public KNJD154MForm(final Param param) {
            super(param);
        }

        public void print(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            final String form;
            form = "KNJD154M.frm";
            log.info(" form = " + form);

            final List printSubclassInfo = new ArrayList();
            List subList = new ArrayList();
            final int lineMax = 20;
            for (int i = 0; i < student._subclassInfos.size(); i++) {

                final SubclassInfo info = (SubclassInfo) student._subclassInfos.get(i);
                if (_param._d046List.contains(info._subclassCd)) {
                    continue;
                }
                if (i % lineMax == 0) {
                	subList = new ArrayList();
                	printSubclassInfo.add(subList);
                }
                subList.add(info);
            }
            for (Iterator ite = printSubclassInfo.iterator();ite.hasNext();) {
            	final List sList = (List)ite.next();
                svf.VrSetForm(form, 4);
                printKotei(db2, svf, student);
            	printSub(db2, svf, student, sList);
            	svf.VrEndPage();
            }
        }

        public void printSub(final DB2UDB db2, final Vrw32alp svf, final Student student, final List printSubclassInfo) {

            final List testItemAll = new ArrayList();
            for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
                final String semes = (String) it.next();
                final Semester semester = (Semester) _param._semesterMap.get(semes);
                testItemAll.addAll(semester._testItemList);
            }
            final int subformWidth = 3246 - 1346;
            int width = 0;
            final String fieldDiv = "1";
            final int testItemWidth = (1424 - 1148);
            for (final Iterator tit = testItemAll.iterator(); tit.hasNext();) {
                final TestItem testItem = (TestItem) tit.next();

                svf.VrsOut("SEMESTER" + fieldDiv, testItem._semester._name);
                svf.VrsOut("TESTITEM" + fieldDiv, testItem._testitemname);
                svf.VrsOut("SCORE_NAME" + fieldDiv, testItem._scoreDivName);
                svf.VrsOut("AVE_NAME" + fieldDiv, "平均点");
                svf.VrsOut("VAL_NAME" + fieldDiv, "偏差値");
                svf.VrsOut("RANK_NAME" + fieldDiv, "順位");

                if (_param.getTargetTestcds().contains(testItem.getTestcd())) {
                    String befClassabbv = null;
                    for (int subline = 0; subline < printSubclassInfo.size(); subline++) {
                        final SubclassInfo info = (SubclassInfo) printSubclassInfo.get(subline);
                        final Subclass subclass = student.getSubclass(info._subclassCd);
                        printSubclassScore(svf, fieldDiv, subline, info, befClassabbv, subclass, testItem);
                        befClassabbv = info._classabbv;
                    }
                }

                printKotei(db2, svf, student);
                svf.VrEndRecord();
                width += testItemWidth;
            }
//            //フィールド削除
//            final int[] divs = SubclassAttendance.getPrintDivs(_param);
//            final String title = divs.length > 2 ? "科目の欠席等" : "欠席等";
//            final int attendWidth = (2910 - 2810);
//            for (int subattidx = 0; subattidx < divs.length; subattidx++) {
//                final int div = divs[subattidx];
//                svf.VrsOut("ABSENT_GRP", "A");
//                svf.VrsOut("ABSENT_TITLE", StringUtils.center(title, 4 * divs.length / 2, '　').substring(4 * subattidx / 2));
//                svf.VrsOut("ABSENT_NAME", SubclassAttendance.getName(div));
//                String befClassabbv = null;
//                for (int subline = 0; subline < printSubclassInfo.size(); subline++) {
//
//                    final SubclassInfo info = (SubclassInfo) printSubclassInfo.get(subline);
//                    final Subclass subclass = student.getSubclass(info._subclassCd);
//                    if (null != subclass) {
//                        AbsenceHigh absenceHigh = subclass._absenceHigh;
//                        if (null == absenceHigh) {
//                        	SubclassMst mst = SubclassMst.getSubclassMst(_param._subclassMstMap, subclass._subclassCd);
//                        	if (null != mst && mst._isSaki) {
//                        		for (final Iterator atit = mst._attendSubclasscdSet.iterator(); atit.hasNext();) {
//                        			final String attendSubclasscd = (String) atit.next();
//                                	SubclassMst mstat = SubclassMst.getSubclassMst(_param._subclassMstMap, attendSubclasscd);
//                                	if (null != mstat) {
//                                		final Subclass attendSubclass = student.getSubclass(mstat._subclasscd);
//                                		if (null != attendSubclass && null != attendSubclass._absenceHigh) {
//                                			absenceHigh = attendSubclass._absenceHigh.add(absenceHigh);
//                                		}
//                                	}
//                        		}
//                        	}
//                        }
//						printSubclassAttendance(svf, subline, info, befClassabbv, absenceHigh, subclass.getAttendance(_param._semester), subattidx);
//                        befClassabbv = info._classabbv;
//                    }
//                }
//
//                printKotei(db2, svf, student);
//                svf.VrEndRecord();
//                width += attendWidth;
//            }
            if ("1".equals(_param._koketsuKibikiShutteiNasi)) {
                final int nokoriWidth = subformWidth - width;
                final int bikoWidth = (3070 - 2970);
                final int bikoColumns = nokoriWidth / bikoWidth;
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
            svf.VrsOut("NENDO", _param._isSeireki ? _param._year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);

            svf.VrsOut("PRESIDENT", _param._principalName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0) );
            }

            svf.VrsOut("COURSE",   student._courseCodeName);
            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("HR_NAME",  student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);

            if (null != _param._logoFile) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.getPath());
            }

            printAddress(svf, student);

//          //フィールド削除
//            printComment(svf);
        }

        private void printAddress(final Vrw32alp svf, final Student student) {
            if ("1".equals(_param._jusyoPrint)) {
            } else {
                if (student._address != null) {
                    final String addressee = student._address._addressee == null ? "" : student._address._addressee + "  様";

                    final int check1 = getMS932ByteLength(student._address._address1);
                    final int check2 = getMS932ByteLength(student._address._address2);
                    final int use = ("1".equals(_param._useAddrField2) && (check1 > 50 || check2 > 50)) ? 3 : (check1 > 40 || check2 > 40) ? 2 : 1;
                        svf.VrsOut(use == 3 ? "ADDR1_3" : use == 2 ? "ADDR1_2" : "ADDR1", student._address._address1);     //住所
                        svf.VrsOut(use == 3 ? "ADDR2_3" : use == 2 ? "ADDR2_2" : "ADDR2", student._address._address2);     //住所
                        svf.VrsOut("ZIPCD", student._address._zipcd);
                    svf.VrsOut(getMS932ByteLength(addressee) > 24 ? "ADDRESSEE2" : "ADDRESSEE", addressee);
                }
            }
            svf.VrsOut("HR_ATTNO_NAME", student._hrNameAbbv + "　" + student._attendNo + (_param._isNotPrintStudentNameWhenAddresseeIs2 ? "" : "　" + student._name));
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
            svf.VrsOutn("SUBCLASS" + (getMS932ByteLength(si._subclassname) > 26 ? "2" : "1"), i, si._subclassname);

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOutn("CREDIT", i, si._credits);
            }

            if (subclass == null) {
                return;
            }

            if (!(0 == si._replaceflg  || 1 == si._replaceflg  || 9 == si._replaceflg)) {
                return;
            }

            final Score s = (Score) subclass._testScoreMap.get(testItem.getTestcd());
            if (s != null) {
                if (s.isSlump(testItem, _param)) {
                    svf.VrAttributen("POINT" + fieldDiv, i, AMIKAKE_FUSHIN);
                }
                if (s._score != null) {
                    svf.VrsOutn("POINT" + fieldDiv, i, s._score);
                }
                if (!_param._isNotPrintAvg) {
                    svf.VrsOutn("AVE_POINT" + fieldDiv, i, s.getAvg(_param));
                }
                svf.VrsOutn("VAL" + fieldDiv, i, s.getDeviation(_param));
                String testkeycd = testItem._semester._cd + testItem._testkindcd + testItem._testitemcd + testItem._scoreDiv;
                if (Arrays.asList(_param._selectTestItem).contains(testkeycd) && null != s.getRank(_param) && !"".equals(s.getRank(_param))) {
                    svf.VrsOutn("RANK" + fieldDiv, i, s.getRank(_param) + "/" + s.getRankBunbo(_param));
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
            svf.VrsOutn("SUBCLASS" + (getMS932ByteLength(si._subclassname) > 26 ? "2" : "1"), i, si._subclassname);

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
    }

    private static class SubclassMst {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        boolean _isSaki;
        final Set _attendSubclasscdSet = new TreeSet();
        final Set _combinedSubclasscdSet = new TreeSet();
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

        private static SubclassMst getSubclassMst(final Map subclassMstMap, final String subclasscd) {
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
            return (SubclassMst) subclassMstMap.get(subclasscd);
        }

        private static Map getSubclassMstMap(
                final DB2UDB db2,
                final String year
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"));
                    subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            try {
                String sql = "";
                sql += " SELECT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += "     ,  COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String attendSubclasscd = rs.getString("ATTEND_SUBCLASSCD");
                	final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");

                    final SubclassMst atsub = SubclassMst.getSubclassMst(subclassMstMap, attendSubclasscd);
                    if (null == atsub) {
                    	log.warn("not found attend subclass : " + attendSubclasscd);
                    } else {
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
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
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

        final boolean _isRuikei;

        final String[] _selectSchregno;
        final String[] _selectTestItem;

        /** 平均値区分 1:学年、2:ホームルームクラス、3:コース、4:学科 */
        final String _avgDiv;

        final String _schooLkind;
        final String _schoolName;
        final String _principalName;
        final String _jobName;
        final String _hrJobName;
        final String _schoolAddress;

        final List _staffNames;

        final KNJSchoolMst _knjSchoolMst;

        final boolean _isSeireki;

        /** 学期・テスト種別と考査名称のマップ */
        final List _testItem;

        final TreeMap _semesterMap;

        /** 欠課数上限値 1:注意、2:超過 */
        final boolean _useAbsenceWarn;
        /** 凡例出力なし */
        final String _hanreiSyuturyokuNasi;
        /** 公欠・忌引・出停時数なし */
        final String _koketsuKibikiShutteiNasi;

        /** C005 */
        final Map _subClassC005;

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
        /** 名称マスタ「D046」 登録された学期に表示しない科目のリスト */
        final List _d046List;

        private String _d054Namecd2Max;

        final String _attendEndDateSemester;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useAddrField2;
        final String _setSchoolKind;

        final Map _attendParamMap;
        final Map _subclassMstMap;

        final String _d008Namecd1;

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
            _selectTestItem = request.getParameterValues("CATEGORY_SELECTED2");
            _avgDiv = request.getParameter("AVG_DIV");
            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _hanreiSyuturyokuNasi = request.getParameter("HANREI_SYUTURYOKU_NASI");
            _koketsuKibikiShutteiNasi = request.getParameter("KOKETSU_KIBIKI_SHUTTEI_NASI");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _jusyoPrint = request.getParameter("JUSYO_PRINT");
            _okurijouJusyo = request.getParameter("OKURIJOU_JUSYO");
            _isNotPrintStudentNameWhenAddresseeIs2 = !"1".equals(_okurijouJusyo) ? "1".equals(request.getParameter("NO_PRINT_STUDENT_NAME")) || "1".equals(request.getParameter("NO_PRINT_STUDENT_NAME2")) : true;
            _isNotPrintAvg = "1".equals(request.getParameter("AVG_PRINT"));

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAddrField2 = request.getParameter("useAddrField2");
            _setSchoolKind = request.getParameter("setSchoolKind");

            _isSeireki = loadNameMstZ012(db2);
            _subClassC005 = loadNameMstC005(db2);
            _absenceWarnIsUnitCount = loadNameMstC042(db2);
            _d046List = loadNameMstD046(db2);

            _knjSchoolMst = new KNJSchoolMst(db2, _year);

            _semesterMap = loadSemester(db2);
            _sdate = null == _semesterMap.get(SSEMESTER) ? null : ((Semester) _semesterMap.get(SSEMESTER))._sdate;
            final String siteiScoreDiv = _testcd.substring(_testcd.length() - 2);
            List testItem = getTestKindItemList(db2, siteiScoreDiv, _setSchoolKind);
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
            _schooLkind = getSchoolKind(db2, _grade_hr_class.substring(0, 2));
            _schoolName = setCertifSchoolDat(db2, "SCHOOL_NAME");
            _jobName = setCertifSchoolDat(db2, "JOB_NAME");
            _principalName = setCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _hrJobName = setCertifSchoolDat(db2, "REMARK2");
            _schoolAddress = setCertifSchoolDat(db2, "REMARK4");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade_hr_class.substring(0, 2));
            _attendParamMap.put("hrClass", _grade_hr_class.substring(2, 5));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _subclassMstMap = SubclassMst.getSubclassMstMap(db2, _year);

            final String tmpD008Cd = "D" + _schooLkind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        public List getTargetSemester() {
            final List list = new ArrayList();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                if (_semester.compareTo(semester) >= 0) {
                    list.add(semester);
                }
            }
            return list;
        }

        public List getTargetTestcds() {
            final List list = new ArrayList();
            for (int i = 0; i < _testItem.size(); i++) {
                final TestItem testItem = (TestItem) _testItem.get(i);
                if (_testcd.compareTo(testItem.getTestcd()) >= 0) {
                    list.add(testItem.getTestcd());
                }
            }
            return list;
        }

        private String getSchoolKind(final DB2UDB db2, final String grade) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String setCertifSchoolDat(final DB2UDB db2, final String field) {
        	String certifKindcd = "";
        	if ("H".equals(_schooLkind)) {
        		certifKindcd = "109";
        	} else if ("J".equals(_schooLkind)) {
        		certifKindcd = "110";
        	}

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT " + field + " FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private List getTestKindItemList(final DB2UDB db2, final String siteiScoreDiv, final String setSchoolKind) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
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
                stb.append("   AND T1.SEMESTER <> '" + SEMEALL + "' ");
                stb.append("   AND T1.TESTKINDCD <> '99' ");
                stb.append("   AND T1.SCORE_DIV = '" + siteiScoreDiv + "' ");
                stb.append("   AND NOT (T1.SEMESTER <> '" + SEMEALL + "' AND T1.SCORE_DIV = '09') ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) _semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    final String scoreDivName = rs.getString("SCORE_DIV_NAME");

                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf, scoreDivName);
                    semester._testItemList.add(testItem);
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info(" testcd = " + list);
            return list;
        }

        private void setD054Namecd2Max(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
                stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _d054Namecd2Max = rs.getString("NAMECD2");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private TreeMap loadSemester(final DB2UDB db2) {
            final TreeMap semesterMap = new TreeMap();
            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    final Semester semester = new Semester(cd, name, sdate, edate);
                    semesterMap.put(cd, semester);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterMap;
        }

        private boolean loadNameMstZ012(final DB2UDB db2) throws SQLException {
            boolean isSeireki = false;
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z012'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAME1");
                if ("2".equals(name)) isSeireki = true;
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            log.debug("(名称マスタZ012):西暦フラグ = " + isSeireki);
            return isSeireki;
        }

        private Map loadNameMstC005(final DB2UDB db2) throws SQLException {
            final Map subClassC005 = new HashMap();
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String is = rs.getString("NAMESPARE1");
                log.debug("(名称マスタC005):科目コード" + subclassCd);
                subClassC005.put(subclassCd, is);
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            return subClassC005;
        }

        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private boolean loadNameMstC042(final DB2UDB db2) throws SQLException {
            boolean absenceWarnIsUnitCount = false;
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                absenceWarnIsUnitCount = "1".equals(rs.getString("NAMESPARE1"));
                log.debug("(名称マスタ C042) =" + absenceWarnIsUnitCount);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            return absenceWarnIsUnitCount;
        }

        private String loadAttendEdateSemester(final DB2UDB db2) {
            String attendEndDateSemester = null;
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

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    attendEndDateSemester = rs.getString("SEMESTER");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return attendEndDateSemester;
        }

        private String loadControlMst(final DB2UDB db2, final String field) {
            String rtn = null;
            final String sql = "SELECT " + field + " FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        public List getStaffNames(final DB2UDB db2, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new LinkedList();
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
            final List d046List = new ArrayList();
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

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    d046List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return d046List;
        }
    }
}
