// kanji=漢字
/*
 * $Id: 0cd398246133dbaea7dc553350ad7b7f822afbaf $
 *
 * 作成日: 2009/08/20
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

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
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績個人票
 */

public class KNJD154N {

    private static final Log log = LogFactory.getLog(KNJD154N.class);

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

        log.debug(" $Revision: 75754 $");
        KNJServletUtils.debugParam(request, log);

        try {
           Param _param = new Param(request, db2);

            KNJD154NFormBase knjd154nForm = new KNJD154NForm(_param);

            final List students = createStudents(db2, _param);

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug(" student = " + student + "(" + student._attendNo + ")");

                knjd154nForm.print(db2, svf, student);

                hasData = true;
            }

            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
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

    private static String getSubclasscd(final Param param, ResultSet rs) throws SQLException {
        final String subclassCd;
        subclassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");
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
        Score.setTotalScore(db2, students, param);
        Score.setSlump(db2, students, param);
        SpecialSubclassAttendance.setSpecialSubclassAttendData(students, param);
        AbsenceHigh.setAbsenceHigh(db2, students, param);
        SubclassInfo.setRecDetail(db2, students, param);
        Student.setHexamRecordRemarkDat(db2, students, param);

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

        final Map _testTotalMap = new HashMap();
        final Map _subclassMap = new HashMap();
        final Map _attendMap = new TreeMap();
        final Map _spSubclassAbsenceHigh = new HashMap();
        final Map _specialGroupAttendanceMap = new HashMap();

        private String _hexamRecordRemarkRemark1;

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public Attendance getAttendance(final String semester) {
            final Attendance a = (Attendance) _attendMap.get(semester);
            return (a == null) ? new Attendance() : a;
        }

        public TestTotalInfo getTestTotalInfo(final String testCd) {
            return (TestTotalInfo) _testTotalMap.get(testCd);
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

        /**
         * 通知表所見
         * @param db2
         * @param students
         * @param param
         */
        private static void setHexamRecordRemarkDat(final DB2UDB db2, final List students, final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER ");
            stb.append("     ,REMARK1");
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

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    student._hexamRecordRemarkRemark1 = null;

                    while (rs.next()) {
                        String remark1 = rs.getString("REMARK1");
                        student._hexamRecordRemarkRemark1 = remark1;
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("sql exception! :" + sql, e);
            } catch (Exception e) {
                log.error("exception!", e);
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
        Map _spGroupKekka = Collections.EMPTY_MAP;
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
            final Integer kekka = (Integer) _spGroupKekka.get(groupCd);
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

                log.debug(" attend semes sql = " + sql);
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
            divs = new int[] {KETSUJI, CHIKOKU_SOUTAI, KOUKETSU, KIBIKI, SHUTTEI};
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
            _replacedSick = replacedSick;
        }

        private String getKekkaString() {
            return "lesson = " + _lesson + " , rawSick = " + _rawSick + " , sick = " + _sick + " , absent = " + _absent + " , susmour = (" + _suspend + " , " + _mourning +
            ") , lateearly = " + _lateearly + ((_replacedSick.intValue() != 0) ? " , replacedSick = " + _replacedSick : "");
        }

        public String toString() {
            return getKekkaString();
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

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclassCdC005 = rs.getString("SUBCLASSCD");
                    final String subclassCd;
                    final String[] split = StringUtils.split(rs.getString("SUBCLASSCD"), "-");
                    subclassCd = split[0] + split[1] + split[2] + split[3];

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

        public String toString() {
            return _schregno + " : " + _subclassCd;
        }
    }

    private static class TestTotalInfo {
    	final String _totalScore;
    	final String _totalAvg;
        public TestTotalInfo(
                final String totalScore,
                final String totalAvg
        ) {
        	_totalScore = totalScore;
        	_totalAvg = totalAvg;
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
            	stb.append("     T1.CLASSCD, ");
            	stb.append("     T1.SCHOOL_KIND, ");
            	stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
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
                    stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ");
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
            	stb.append("     T1.CLASSCD, ");
            	stb.append("     T1.SCHOOL_KIND, ");
            	stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            stb.append("        AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
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
                    stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ");
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
                avg = _hrAvg;
            } else if ("2".equals(param._avgDiv)) {
                avg = _courseAvg;
            } else if ("3".equals(param._avgDiv)) {
                avg = _gradeAvg;
            } else if ("4".equals(param._avgDiv)) {
                avg = _majorAvg;
            }
            return avg;
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
            if (param._isOutputDebug) {
            	log.debug("setScoreValue sql = " + sql);
            }

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
                        subScore._score = !StringUtils.isBlank(rs.getString("VALUE_DI")) ? rs.getString("VALUE_DI") : rs.getString("SCORE");
                        subScore._passScore = rs.getString("PASS_SCORE");
                        subScore._gradeRank = rs.getString("GRADE_RANK");
                        subScore._gradeDeviation = rs.getString("GRADE_DEVIATION");
                        subScore._classRank = rs.getString("CLASS_RANK");
                        subScore._classDeviation = rs.getString("CLASS_DEVIATION");
                        subScore._courseRank = rs.getString("COURSE_RANK");
                        subScore._courseDeviation = rs.getString("COURSE_DEVIATION");
                        subScore._majorRank = rs.getString("MAJOR_RANK");
                        subScore._majorDeviation = rs.getString("MAJOR_DEVIATION");
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
            stb.append("   SELECT  T0.YEAR, T0.SCHREGNO, ");
            stb.append("           T0.CLASSCD, ");
            stb.append("           T0.SCHOOL_KIND, ");
            stb.append("           T0.CURRICULUM_CD, ");
            stb.append("           T0.SUBCLASSCD, ");
            stb.append("           T0.SEMESTER, T0.TESTKINDCD, T0.TESTITEMCD, T0.SCORE_DIV, ");
            stb.append("           T0.SEMESTER || T0.TESTKINDCD || T0.TESTITEMCD || T0.SCORE_DIV AS TESTCD, ");
            stb.append("           T0.VALUE_DI, ");
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
            stb.append("   FROM    RECORD_SCORE_DAT T0 ");
            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T0.YEAR ");
            stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("        AND T2.SCHREGNO = T0.SCHREGNO ");
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("        AND T1.SEMESTER = T0.SEMESTER AND T1.TESTKINDCD = T0.TESTKINDCD AND T1.TESTITEMCD = T0.TESTITEMCD AND T1.SCORE_DIV = T0.SCORE_DIV ");
            stb.append("        AND T1.CLASSCD = T0.CLASSCD ");
            stb.append("        AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("        AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("        AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("        AND T1.SCHREGNO = T0.SCHREGNO ");
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
            stb.append("     AND PERF.GRADE = T2.GRADE");
            stb.append("     AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ");

            stb.append("   WHERE   T0.YEAR = '" + param._year + "' AND T0.SCHREGNO = ? ");
            stb.append("           AND T0.SEMESTER || T0.TESTKINDCD || T0.TESTITEMCD || T0.SCORE_DIV IN " + SQLUtils.whereIn(true, toArray(param.getTargetTestcds())));
            stb.append("       AND (SUBSTR(T0.SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR SUBSTR(T0.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            stb.append("       AND (T0.VALUE_DI IS NOT NULL OR T0.SCORE IS NOT NULL) ");

            return stb.toString();
        }

        /**
         * 合計点、(総合)平均点をセットする。
         * @param db2
         * @param student
         * @param param
         */
        private static void setTotalScore(final DB2UDB db2, final Collection students, final Param param) {

            final String sql = sqlRecordTotalScore(param);
            if (param._isOutputDebug) {
            	log.info("setScoreValue sql = " + sql);
            }

            PreparedStatement ps = null;

            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                    	final String testcd = rs.getString("TESTCD");
                        final String score = rs.getString("SCORE");
                        final String avg = rs.getString("AVG");
                        if (null == student.getTestTotalInfo(testcd)) {
                            student._testTotalMap.put(testcd, new TestTotalInfo(score, avg));
                        }
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

        /**
         * 合計点、平均点を取得する
         * @param param
         * @return
         */
        private static String sqlRecordTotalScore(final Param param) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("   SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, ");
            stb.append("   SCORE, ");
            stb.append("   AVG ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + param._year + "' ");
            stb.append("   AND SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("   AND CLASSCD = '99' ");
            stb.append("   AND CURRICULUM_CD = '99' ");
            stb.append("   AND SUBCLASSCD = '999999' ");
            stb.append("   AND SCHREGNO = ? ");

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
            stb.append("        T2.CLASSCD, ");
            stb.append("        T2.SCHOOL_KIND, ");
            stb.append("        T2.CURRICULUM_CD, ");
            stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
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
            stb.append(" )");

            stb.append(", SUBCLASSNUM AS(");
            stb.append("   SELECT  SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,5,2) = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
            stb.append("         , SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,5,2) != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
            stb.append("   FROM    CHAIR_A S1");
            stb.append(" LEFT JOIN (SELECT CLASSCD, SCHOOL_KIND, CLASSCD AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND N1.CLASS_SEQ = '003') T1 ON T1.CLASSCD = S1.CLASSCD AND T1.SCHOOL_KIND = S1.SCHOOL_KIND ");
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
            stb.append(" FROM    CHAIR_A T2");
            stb.append(" LEFT JOIN SUBCLASS_MST T4 ON ");
            stb.append("        T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T7 ON ");
            stb.append("        T7.CLASSCD || T7.SCHOOL_KIND = SUBSTR(T2.SUBCLASSCD,1,3)");
            stb.append(" LEFT JOIN SUBCLASS_CREDITS T6 ON T6.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5 ON T5.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN ATTEND_SUBCLASSCD T9 ON T9.SUBCLASSCD = T2.SUBCLASSCD");
            if ("1".equals(param._useClassDetailDat)) {
                stb.append(" LEFT JOIN (SELECT CLASSCD, SCHOOL_KIND, CLASSCD AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND N1.CLASS_SEQ ='003') N1 ON N1.CLASSCD = T2.CLASSCD AND N1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append(" LEFT JOIN NAME_MST N1 ON N1.NAMECD1='" + param._d008Namecd1 + "' AND N1.NAMECD2 = SUBSTR(T2.SUBCLASSCD,5,2)");
            }
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC_SCORE ON REC_SCORE.YEAR = '" + param._year + "' ");
            stb.append("       AND REC_SCORE.SCHREGNO = ? ");
            stb.append("        AND REC_SCORE.CLASSCD || REC_SCORE.SCHOOL_KIND || REC_SCORE.CURRICULUM_CD || REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD || REC_SCORE.SCORE_DIV = '" + HYOTEI_TESTCD + "' ");

            stb.append(" ORDER BY ORDER0, ORDER1, ORDER2");
            return stb.toString();
        }
    }

    private static abstract class KNJD154NFormBase {

        protected static final String AMIKAKE_RISHU = "Paint=(1,60,1),Bold=1";
        protected static final String AMIKAKE_SHUTOKU = "Paint=(1,80,1),Bold=1";
        protected static final String AMIKAKE_FUSHIN = "Paint=(1,70,1),Bold=1";

        protected Param _param;

        protected KNJD154NFormBase(final Param param) {
            this._param = param;
        }

        public abstract void print(final DB2UDB db2, final Vrw32alp svf, final Student student);

        protected static void svfVrsOutSeq(final Vrw32alp svf, final String s, final int keta, final int gyo, final String field) {
            final String[] token = KNJ_EditEdit.get_token(s, keta, gyo);
            if (token != null) {
                for (int i = 0; i < token.length; i++) {
                    svf.VrsOut(field + (i + 1), token[i]);
                }
            }
        }
    }

    private static class KNJD154NForm extends KNJD154NFormBase {

    	boolean isEndRecord = false;

        public KNJD154NForm(final Param param) {
            super(param);
        }

        public void print(final DB2UDB db2, final Vrw32alp svf, final Student student) {

            final String form;
            isEndRecord = false;
            if ("2".equals(_param._outputPattern)) {
                form = "KNJD154N_2.frm";
                log.info(" form = " + form);
                svf.VrSetForm(form, 4);
                //タイトル出力
                printKotei(db2, svf, student);
                //度数分布表出力
                if (!"1".equals(_param._dosuBunpuHiHyoujiFlg)) {
                    printDosuBunpu(db2, svf, student);
                }
                //出欠欄
                printAttendance(svf, student);
                //得点出力(合計・平均含む)
                printScore2(db2, svf, student);
                if (!isEndRecord) {
                	svf.VrEndRecord();
                }
                svf.VrEndPage();
            } else {
                form = "KNJD154N_1.frm";
                log.info(" form = " + form);
                svf.VrSetForm(form, 1);
                //タイトル出力
                printKotei(db2, svf, student);
                //得点出力(合計・平均含む)
                printScore1(db2, svf, student);
                //出欠欄
                printAttendance(svf, student);
                // 備考
                svfVrsOutSeq(svf, student._hexamRecordRemarkRemark1, 88, 3, "REMARK");
                // 通信欄
                if (!"1".equals(_param._hensinranHiHyoujiFlg)) {
                    svf.VrsOut("HR_NAME2",  student._hrName + student._attendNo);
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
                    final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                    svf.VrsOut("NAME2_" + nfield, student._name);
                } else {
                	//空白図を出力
                	if (_param._whiteSpaceImagePath != null) {
                	    svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
                	}
                }
                svf.VrEndPage();
            }
        }

        private void printKotei(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        	final String nendo = _param._isSeireki ? _param._year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
            final Semester semester = (Semester) _param._semesterMap.get(_param._semester);
            String testName = "";
            for (final Iterator tit = semester._testItemList.iterator(); tit.hasNext();) {
                final TestItem testItem = (TestItem) tit.next();
                testName = testItem._testitemname;
            }

            svf.VrsOut("TITLE", nendo + semester._name + " " + testName + "成績連絡票");
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);

            //担任名称
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
            	final String outPositionName = "2".equals(_param._printStafftypeFlg) ? "チューター" : "担任";
        		final String t1Name = (String)_param._staffNames.get(0);
    			final int t1nlen = KNJ_EditEdit.getMS932ByteLength(t1Name);
    			final String t1field = t1nlen > 30 ? "3" : t1nlen > 20 ? "2" : "1";
            	if (_param._staffNames.size() > 1) {
        			final String t2Name = (String) _param._staffNames.get(1);
            		if (!"".equals(t2Name)) {
            			svf.VrsOut("JOB_NAME1", outPositionName);
                        svf.VrsOut("TR_NAME1_" + t1field, t1Name ); //1行目に出力
            			final int t2nlen = KNJ_EditEdit.getMS932ByteLength(t2Name);
            			final String t2field = t2nlen > 30 ? "3" : t2nlen > 20 ? "2" : "1";
            			svf.VrsOut("JOB_NAME2", outPositionName);
                        svf.VrsOut("TR_NAME2_" + t2field, t2Name ); //2行目に出力
            		} else {
            			svf.VrsOut("JOB_NAME2", outPositionName);
                        svf.VrsOut("TR_NAME2_" + t1field, t1Name ); //2行目に出力
            		}
            	} else {
        			svf.VrsOut("JOB_NAME2", outPositionName);
                    svf.VrsOut("TR_NAME2_" + t1field, t1Name ); //2行目に出力
            	}
            }

            svf.VrsOut("HR_NAME1",  student._hrName + student._attendNo);
            if ("2".equals(_param._outputPattern)) {
                svf.VrsOut("NAME", student._name);
            } else {
                final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                svf.VrsOut("NAME1_" + nfield, student._name);
            }
        }

        private static String sishagonyu(final String s) {
        	if (!NumberUtils.isNumber(s)) {
        		return null;
        	}
        	return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private void printScore1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            final List printSubclassInfo = getPrintSubclassInfoList(student);
            int tablemaxline = 19;

            final List testItemAll = new ArrayList();
            for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
                final String semes = (String) it.next();
                final Semester semester = (Semester) _param._semesterMap.get(semes);
                testItemAll.addAll(semester._testItemList);
            }

            //合計点、平均点
            TestTotalInfo outTotalwk = (TestTotalInfo)student._testTotalMap.get(_param._testcd);
            if (null != outTotalwk) {
            	svf.VrsOutn("SCORE", 21, outTotalwk._totalScore);
            	svf.VrsOutn("SCORE", 22, sishagonyu(outTotalwk._totalAvg));
            }

            for (final Iterator it = printSubclassInfo.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();
                final Subclass subclass = student.getSubclass(info._subclassCd);
                
                boolean hasTarget = false;
                for (final Iterator tit = testItemAll.iterator(); tit.hasNext();) {
                    final TestItem testItem = (TestItem) tit.next();
                    if (_param.getTargetTestcds().contains(testItem.getTestcd())) {
                        final Score s = (Score) subclass._testScoreMap.get(testItem.getTestcd());
                        if (null != s) {
                        	hasTarget = true;
                        	break;
                        }
                    }
                }
                if (!hasTarget) {
                	if (_param._isOutputDebug) {
                		log.info(" no score subclass : " + info._subclassCd);
                	}
                	it.remove();
                }
            }
            int slumpCnt = 0;
            for (final Iterator tit = testItemAll.iterator(); tit.hasNext();) {
                final TestItem testItem = (TestItem) tit.next();

                if (_param.getTargetTestcds().contains(testItem.getTestcd())) {
                    String befClassabbv = null;
                    int printline = 0;
                    for (int subline = 0; subline < Math.min(printSubclassInfo.size(), tablemaxline); subline++) {
                        final SubclassInfo info = (SubclassInfo) printSubclassInfo.get(subline);
                        final Subclass subclass = student.getSubclass(info._subclassCd);
                        final Score s = (Score) subclass._testScoreMap.get(testItem.getTestcd());
                        if (s != null) {
                            printSubclassScore1(svf, printline++, info, befClassabbv, subclass, testItem);
                            if (s.isSlump(testItem, _param)) {
                                slumpCnt++;
                            }
                            befClassabbv = info._classabbv;
                        }
                    }
                }
            }

            //欠点数
            svf.VrsOutn("SCORE", 20, String.valueOf(slumpCnt));
        }

        private void printScore2(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            final List printSubclassInfo = getPrintSubclassInfoList(student);
            int tablemaxline = 16;

            final List testItemAll = new ArrayList();
            for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
                final String semes = (String) it.next();
                final Semester semester = (Semester) _param._semesterMap.get(semes);
                testItemAll.addAll(semester._testItemList);
            }

            //合計点、平均点
            TestTotalInfo outTotalwk = (TestTotalInfo)student._testTotalMap.get(_param._testcd);
            if (null != outTotalwk) {
            	svf.VrsOut("TOTAL_SCORE", outTotalwk._totalScore);
            	svf.VrsOut("AVERAGE_SCORE", sishagonyu(outTotalwk._totalAvg));
            }

            for (final Iterator tit = testItemAll.iterator(); tit.hasNext();) {
                final TestItem testItem = (TestItem) tit.next();

                if (_param.getTargetTestcds().contains(testItem.getTestcd())) {
                    for (int subline = 0; subline < Math.min(printSubclassInfo.size(), tablemaxline); subline++) {
                        final SubclassInfo info = (SubclassInfo) printSubclassInfo.get(subline);
                        final Subclass subclass = student.getSubclass(info._subclassCd);
                        final Score s = (Score) subclass._testScoreMap.get(testItem.getTestcd());
                        if (s != null) {
                            printSubclassScore2(svf, info, subclass, testItem);
                        }
                    }
                }
            }
        }

		private List getPrintSubclassInfoList(final Student student) {
			final List printSubclassInfo = new ArrayList();
            for (int i = 0; i < student._subclassInfos.size(); i++) {
                final SubclassInfo info = (SubclassInfo) student._subclassInfos.get(i);
                if (_param._ignoreSubclsCdList.contains(info._subclassCd)) {
                	if (_param._isOutputDebug) {
                		log.info("  ignore subclass : " + info._subclassCd);
                	}
                    continue;
                }
                final Subclass subclass = student.getSubclass(info._subclassCd);
                if (null == subclass) {
                	if (_param._isOutputDebug) {
                		log.info("  null subclass : " + info._subclassCd);
                	}
                	continue;
                }
                if (!(0 == info._replaceflg  || 1 == info._replaceflg  || 9 == info._replaceflg)) {
                	if (_param._isOutputDebug) {
                		log.info("  skip subclass : " + info._subclassCd + " / " + info._replaceflg);
                	}
                	continue;
                }
                printSubclassInfo.add(info);
            }
			return printSubclassInfo;
		}

        private void printDosuBunpu(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            int tablemaxline =  16;
            int colCnt = 1;
            for (int i = 0; i < student._subclassInfos.size(); i++) {
                final SubclassInfo info = (SubclassInfo) student._subclassInfos.get(i);
                if (_param._ignoreSubclsCdList.contains(info._subclassCd) || colCnt > tablemaxline) {
                    continue;
                }
                Map dosuInfo = (Map)_param._subclasscdScoreDistributionMap.get(info._subclassCd);
        		if (dosuInfo != null) {
        			final int subclsnlen = KNJ_EditEdit.getMS932ByteLength(info._subclassname);
        			final String subclsfield = subclsnlen > 10 ? "2" : "1";
        			svf.VrsOutn("SUBCLASS_NAME2_" + subclsfield, colCnt, info._subclassname);

        			final Subclass subclass = student.getSubclass(info._subclassCd);
        			Score s = null;
        			if (subclass != null) {
        				s = (Score) subclass._testScoreMap.get(_param._testcd);
        				if (s != null) {
        					svf.VrsOutn("DISTRI_AVERAGE", colCnt, StringUtils.defaultString(s.getAvg(_param), ""));
        				}
        			}
        		    for (Iterator ite = dosuInfo.keySet().iterator();ite.hasNext();) {
        			    Integer kInt = (Integer)ite.next();
                	    final String distfield = String.valueOf(Math.min((int)Math.floor((100.0 - kInt.intValue()) / (double)_param._kizami)+1, 7));
        			    List wkArry = (List)dosuInfo.get(kInt);
                        final String count = wkArry.size() == 0 ? "" : String.valueOf(wkArry.size());
                        svf.VrsOutn("DISTRI"+distfield, colCnt, count);
                        if (s != null && NumberUtils.isDigits(s._score)) {
                        	if (wkArry.size() > 0 && wkArry.contains(Integer.valueOf(s._score))) {
                        		svf.VrsOutn("DISTRI_MARK"+distfield, colCnt, "★");
                        	}
                        }
        		    }
        		    colCnt++;
        		}
        	}
        }

        private void printAttendance(final Vrw32alp svf, final Student student) {
            final Attendance sum = student.getAttendance(_param._semester);

            svf.VrsOutn("ATTEND" , 1, String.valueOf(sum._lesson));   //授業日数
            svf.VrsOutn("ATTEND" , 2, String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus)); //忌引出席停止日数
            if ("1".equals(_param._transJyugyouNissuHiHyoujiFlg) && _param._backSlashImagePath != null) {
            	svf.VrsOut("BACKSLASH1", _param._backSlashImagePath);  //★
            } else {
                svf.VrsOutn("ATTEND" , 3, String.valueOf(sum._abroad));   //留学中の授業日数
            }
            svf.VrsOutn("ATTEND" , 4, String.valueOf(sum._mlesson));  //出席すべき日数
            svf.VrsOutn("ATTEND" , 5, String.valueOf(sum._absence));  //欠席日数
            svf.VrsOutn("ATTEND" , 6, String.valueOf(sum._attend));   //出席日数
            svf.VrsOutn("ATTEND" , 7, String.valueOf(sum._late));     //遅刻
            svf.VrsOutn("ATTEND" , 8, String.valueOf(sum._leave));    //早退

            if ("1".equals(_param._kekkaJisuLhrHiHyoujiFlg) && _param._backSlashImagePath != null) {
            	svf.VrsOut("BACKSLASH2", _param._backSlashImagePath);  //★
            } else {
	            final String spGroupCd = Attendance.GROUP_LHR;
	            final int spGroupKekka = sum.getSpGroupKekka(spGroupCd);
	            final String field1 = "ATTEND";
	            svf.VrsOutn(field1, 9, String.valueOf(spGroupKekka));    //LHR欠課時数
            }

            if ("1".equals(_param._gyojiKessekiNasi) && _param._backSlashImagePath != null) {
            	svf.VrsOut("BACKSLASH3", _param._backSlashImagePath);  //★
            } else {
	            final String[][] fields = new String[2][];
	            fields[0] = new String[] {"ABSENCE_NAME1", "ATTEND"};
	            fields[1] = new String[] {"ABSENCE_NAME2", "ATTEND"};
	            int idx = 0;
	            if (!"1".equals(_param._gyojiKessekiNasi)) {
	                final String spGroupCd = Attendance.GROUP_ASS;
	                final String field = fields[idx][1];
	                final int spGroupKekka = sum.getSpGroupKekka(spGroupCd);
	                svf.VrsOutn(field, 10, String.valueOf(spGroupKekka));  //行事欠課時数
	                idx += 1;
	            }
            }
        }


        private void printSubclassScore1(
                final Vrw32alp svf,
                final int subline,
                final SubclassInfo si,
                final String classabbv,
                final Subclass subclass,
                final TestItem testItem
        ) {
            final int i = subline + 1;
        	if (null == classabbv || !classabbv.equals(si._classabbv)) {
        		svf.VrsOutn("CLASS_NAME", i, si._classabbv);
        	}
    		final int nsubcls = KNJ_EditEdit.getMS932ByteLength(si._subclassname);
        	final String subclsfield = nsubcls > 26 ? "3" : nsubcls > 20 ? "2" : "1";
        	svf.VrsOutn("SUBCLASS_NAME" + subclsfield, i, si._subclassname);

            final Score s = (Score) subclass._testScoreMap.get(testItem.getTestcd());
            if (s != null) {
            	if (_param._isOutputDebug) {
            		log.info(" testcd " + testItem.getTestcd() + ", subclass " + subclass._subclassCd + " " + si._subclassname + " = " + s._score + " (passScore = " + s._passScore + ")");
            	}
                if (s.isSlump(testItem, _param)) {
                    svf.VrAttributen("SCORE", i, AMIKAKE_FUSHIN);
                }
                if (s._score != null) {
                	svf.VrsOutn("SCORE", i, s._score);
                }
            	svf.VrsOutn("AVERAGE", i, s.getAvg(_param));
            }
        }


        private void printSubclassScore2(
                final Vrw32alp svf,
                final SubclassInfo si,
                final Subclass subclass,
                final TestItem testItem
        ) {
            svf.VrsOut("CLASS_NAME", si._classabbv);
    		final int nsubcls = KNJ_EditEdit.getMS932ByteLength(si._subclassname);
    		final String scnfield = nsubcls > 18 ? "2" : "1";
    		svf.VrsOut("SUBCLASS_NAME1_" + scnfield, si._subclassname);

            final Score s = (Score) subclass._testScoreMap.get(testItem.getTestcd());
            if (s != null) {
                if (_param._isOutputDebug) {
                	log.info(" testcd " + testItem.getTestcd() + ", subclass " + subclass._subclassCd + " " + si._subclassname + " = " + s._score + " (passScore = " + s._passScore + ")");
                }
                if (s._score != null) {
                	svf.VrsOut("SCORE", s._score);
                }
                svf.VrsOut("AVERAGE", s.getAvg(_param));
            }
        	svf.VrEndRecord();
        	isEndRecord = true;
        }
    }

    private static class Param {

        final int _kizami = 10;

        final String _year;
        final String _semester;
        final String _grade_hr_class;
        final String _grade;

        final String _testcd;
        final String _sdate;
        final String _edate;

        final String[] _selectSchregno;

        final String _schoolKind;
        final String _schoolCd;

        /** 平均値区分 1:学年、2:クラス、3:コース */
        String _avgDiv;

        //帳票パターン
        String _outputPattern;

        /** 学期・テスト種別と考査名称のマップ */
        final List _testItem;

        final TreeMap _semesterMap;

        final String _schoolName;
        final List _staffNames;

        final boolean _isSeireki;

        //出欠の記録
        //LHR欠課時数非表示
        String _kekkaJisuLhrHiHyoujiFlg;
        //留学中の授業日数非表示
        String _transJyugyouNissuHiHyoujiFlg;
        //行事欠課時数非表示
        String _gyojiKessekiNasi;
        //出力教師役職フラグ
        String _printStafftypeFlg;
        //返信欄非表示(Aタイプのみ)
        String _hensinranHiHyoujiFlg;
        //度数分布表非表示
        String _dosuBunpuHiHyoujiFlg;

        final String _documentRoot;
        final String _imagePath;
        final String _whiteSpaceImagePath;
        final String _backSlashImagePath;

        final Map _subclasscdScoreDistributionMap;

        final String _ctrlSemester;
        final String _ctrlDate;

        final boolean _isRuikei;

        final KNJSchoolMst _knjSchoolMst;

        /** C005 */
        final Map _subClassC005;

        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;

        /** 名称マスタ「D046」 登録された学期に表示しない科目のリスト */
        final List _ignoreSubclsCdList;

        private String _d054Namecd2Max;

        /** 教育課程コードを使用するか */
        final String _useClassDetailDat;
        final String _setSchoolKind;

        final Map _attendParamMap;
        final boolean _isOutputDebug;
        
        final String _d008Namecd1;

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException, ParseException {

            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _testcd = request.getParameter("TEST_CD");
            _schoolCd = request.getParameter("SCHOOLCD");
            _isRuikei = "1".equals(request.getParameter("DATE_DIV"));
            _edate = request.getParameter("DATE").replace('/', '-');
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            if (_grade_hr_class.length() > 3) {
            	_grade = StringUtils.substring(_grade_hr_class, 0, 2);
            } else {
            	_grade = "";
            }
            _selectSchregno = request.getParameterValues("CATEGORY_SELECTED");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");

            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _setSchoolKind = request.getParameter("setSchoolKind");

            _isSeireki = loadNameMstZ012(db2);
            _subClassC005 = loadNameMstC005(db2);
            _schoolKind = getSchoolKind(db2, _grade_hr_class.substring(0, 2));
            _ignoreSubclsCdList = loadNameMstD046(db2);

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

            _documentRoot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagePath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");

            _staffNames = getStaffNames(db2, getRegdSemester());
            _schoolName = setCertifSchoolDat(db2, "SCHOOL_NAME");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade_hr_class.substring(0, 2));
            _attendParamMap.put("hrClass", _grade_hr_class.substring(2, 5));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            loadPrintOption(db2);
            _subclasscdScoreDistributionMap = getSubclassScoreDistributionMap(db2);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        public String getImageFilePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        public Map getSubclassScoreDistributionMap(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  t1.classcd || t1.school_kind || t1.curriculum_cd || t1.subclasscd as subclasscd,");
            sql.append("  t1.score ");
            sql.append(" FROM");
            sql.append("  RECORD_RANK_SDIV_DAT t1 ");
            sql.append("  INNER JOIN schreg_regd_dat regd ON t1.schregno = regd.schregno ");
            sql.append("    AND t1.year = regd.year ");
            sql.append("    AND t1.semester = regd.semester ");
            sql.append("    AND regd.grade = '" + _grade + "' ");
            sql.append(" WHERE");
            sql.append("  t1.year = '" + _year + "' AND");
            sql.append("  t1.semester = '" + _semester + "' AND");
            sql.append("  t1.semester || t1.testkindcd || t1.testitemcd || t1.score_div = '" + _testcd + "' AND");
            sql.append("  regd.GRADE = '" + _grade + "' AND ");
            if ("1".equals(_avgDiv)) {
                sql.append("  regd.GRADE || regd.HR_CLASS = '" + _grade_hr_class + "' AND ");
            } else if ("2".equals(_avgDiv)) {
                sql.append("  regd.COURSECD || regd.MAJORCD || regd.COURSECODE IN ( ");
                sql.append("      SELECT DISTINCT ");
                sql.append("        COURSECD || MAJORCD || COURSECODE ");
                sql.append("      FROM ");
                sql.append("        SCHREG_REGD_DAT ");
                sql.append("      WHERE ");
                sql.append("        YEAR = '" + _year + "' ");
                sql.append("        AND SEMESTER = '" + _semester + "' ");
                sql.append("        AND SCHREGNO IN " + SQLUtils.whereIn(true, _selectSchregno) + " ");
                sql.append("  ) AND ");
            }
            sql.append("  t1.score IS NOT NULL ");

            //log.info(" dist sql = " + sql.toString());

            final Map subclassScoreListMap = new HashMap();
            final Map subclasscdScoreDistributionMap = new HashMap();
            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                getMappedList(subclassScoreListMap, KnjDbUtils.getString(row, "SUBCLASSCD")).add(KnjDbUtils.getInt(row, "SCORE", null));
            }
            for (final Iterator it = subclassScoreListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclasscd = (String) e.getKey();
                final List scoreList = (List) e.getValue();
                subclasscdScoreDistributionMap.put(subclasscd, getHistgramMap(scoreList));
            }

            return subclasscdScoreDistributionMap;
        }

        private List getMappedList(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList());
            }
            return (List) map.get(key1);
        }

        public Map getHistgramMap(final List scoreList0) {
            final List scoreList = new ArrayList(scoreList0);
            final Map histgramMap = new TreeMap();
            for (final Iterator it = getKeyList().iterator(); it.hasNext();) {
                final Integer lower = (Integer) it.next();
                for (final Iterator sit = scoreList.iterator(); sit.hasNext();) {
                    final Integer score = (Integer) sit.next();
                    if (isScoreInRange(score, lower)) {
                        getMappedList(histgramMap, lower).add(score);
                        sit.remove();
                    }
                }
                if (scoreList.isEmpty()) {
                    break;
                }
            }
            return histgramMap;
        }

        public boolean isScoreInRange(final Integer score, final Integer lower) {
            if (null == score) {
                return false;
            }
            //50点以下をひとくくりにするため、0の時は0<=x<50にする。
            int kizamiWk = lower.intValue() == 0 ? 50 : _kizami;
            return lower.intValue() <= score.intValue() && score.intValue() < lower.intValue() + kizamiWk;
        }

        public List getKeyList() {
            final List list = new ArrayList();
            //範囲としては、50-100は10点刻み、0-49はひとくくりにする。
            list.add(new Integer(100));
            list.add(new Integer(90));
            list.add(new Integer(80));
            list.add(new Integer(70));
            list.add(new Integer(60));
            list.add(new Integer(50));
            list.add(new Integer(0));
            return list;
        }

        private void loadPrintOption(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("  HCD051.REMARK1 AS PRTPATTERN, ");
        	stb.append("  HCD058.REMARK1 AS NOTPRTLHRABS, ");
        	stb.append("  HCD057.REMARK1 AS NOTPRTTRANS, ");
        	stb.append("  HCD059.REMARK1 AS NOTPRTGYOJIKEKKA, ");
        	stb.append("  HCD054.REMARK1 AS PRTAVG, ");
        	stb.append("  HCD062.REMARK1 AS PRTSTAFF, ");
        	stb.append("  HCD063.REMARK1 AS NOTPRTHENSIN, ");
        	stb.append("  HCD064.REMARK1 AS NOTPRTHISTOGRAM ");
        	stb.append(" FROM ");
        	stb.append("  HREPORT_CONDITION_DAT HCD051 ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD058 ");
        	stb.append("    ON HCD058.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD058.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD058.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD058.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD058.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD058.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD058.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD058.SEQ = '058' ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD057 ");
        	stb.append("    ON HCD057.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD057.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD057.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD057.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD057.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD057.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD057.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD057.SEQ = '057' ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD059 ");
        	stb.append("    ON HCD059.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD059.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD059.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD059.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD059.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD059.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD059.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD059.SEQ = '059' ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD054 ");
        	stb.append("    ON HCD054.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD054.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD054.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD054.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD054.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD054.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD054.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD054.SEQ = '054' ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD062 ");
        	stb.append("    ON HCD062.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD062.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD062.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD062.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD062.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD062.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD062.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD062.SEQ = '062' ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD063 ");
        	stb.append("    ON HCD063.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD063.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD063.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD063.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD063.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD063.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD063.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD063.SEQ = '063' ");
        	stb.append("  LEFT JOIN HREPORT_CONDITION_DAT HCD064 ");
        	stb.append("    ON HCD064.YEAR = HCD051.YEAR ");
        	stb.append("   AND HCD064.SCHOOLCD = HCD051.SCHOOLCD ");
        	stb.append("   AND HCD064.SCHOOL_KIND = HCD051.SCHOOL_KIND ");
        	stb.append("   AND HCD064.GRADE = HCD051.GRADE ");
        	stb.append("   AND HCD064.COURSECD = HCD051.COURSECD ");
        	stb.append("   AND HCD064.MAJORCD = HCD051.MAJORCD ");
        	stb.append("   AND HCD064.COURSECODE = HCD051.COURSECODE ");
        	stb.append("   AND HCD064.SEQ = '064' ");
        	stb.append(" WHERE ");
        	stb.append("  HCD051.YEAR = '" +_year + "' ");
        	stb.append("  AND HCD051.SCHOOLCD = '" + _schoolCd + "' ");
        	stb.append("  AND HCD051.SCHOOL_KIND = '" + _schoolKind + "' ");
        	stb.append("  AND HCD051.GRADE = '00' ");
        	stb.append("  AND HCD051.COURSECD || HCD051.MAJORCD || HCD051.COURSECODE = '00000000' ");
        	stb.append("  AND HCD051.SEQ = '051' ");
            if (_isOutputDebug) {
        	    log.debug(" loadPrintOption sql = " + stb.toString());
        	}
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    //帳票パターン
                	_outputPattern = rs.getString("PRTPATTERN");
                    //LHR欠課時数非表示
                	_kekkaJisuLhrHiHyoujiFlg = rs.getString("NOTPRTLHRABS");
                    //留学中の授業日数非表示
                	_transJyugyouNissuHiHyoujiFlg = rs.getString("NOTPRTTRANS");
                	//行事欠課時数非表示
                	_gyojiKessekiNasi = rs.getString("NOTPRTGYOJIKEKKA");
                    //平均値出力フラグ
                	_avgDiv = rs.getString("PRTAVG");
                    //出力教師役職フラグ
                	_printStafftypeFlg = rs.getString("PRTSTAFF");
                    //返信欄非表示(Aタイプのみ)
                	_hensinranHiHyoujiFlg = rs.getString("NOTPRTHENSIN");
                    //度数分布表非表示
                	_dosuBunpuHiHyoujiFlg = rs.getString("NOTPRTHISTOGRAM");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD154N' AND NAME = '" + propName + "' "));
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
        	if ("H".equals(_schoolKind)) {
        		certifKindcd = "109";
        	} else if ("J".equals(_schoolKind)) {
        		certifKindcd = "110";
        	}

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT " + field + " FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "' ");
            //log.debug("certif_school_dat sql = " + sql.toString());
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
                stb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' ");
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
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' AND SEMESTER = '" + _semester + "' ORDER BY SEMESTER";
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
            if ("1".equals(_useClassDetailDat)) {
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
            //合計を保持する科目コードは無視。
            final String cutchkcd1 = "33"+_schoolKind + "99333333";
            if (!d046List.contains(cutchkcd1)) d046List.add(cutchkcd1);
            final String cutchkcd2 = "55"+_schoolKind + "99555555";
            if (!d046List.contains(cutchkcd2)) d046List.add(cutchkcd2);
            final String cutchkcd3 = "77"+_schoolKind + "99777777";
            if (!d046List.contains(cutchkcd3)) d046List.add(cutchkcd3);
            return d046List;
        }

    }
}
