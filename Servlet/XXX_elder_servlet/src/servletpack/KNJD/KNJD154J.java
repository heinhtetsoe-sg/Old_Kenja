// kanji=漢字
/*
 * $Id: 6a6f6ff3a2c7d4c5c6cdd433e3c61fa28a726096 $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */

public class KNJD154J {

    private static final Log log = LogFactory.getLog(KNJD154J.class);

    private static final String USEKNJD154J2CD = "KNJD154J_2";

    private static final String SLUMP_CD = "1";
    private static final String SPECIAL_ALL = "999";

    private static final String PATTERN_B = "2";

    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alpWrap svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasData = false;

        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
        sd.setSvfInit( request, response, svf);
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }

        log.info(" $Revision: 74345 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);

        KNJD154JBase knjd154j;
        knjd154j = new KNJD154JA(db2, svf, param);

        try {
            final List students = createStudents(db2, param);

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug(" student = " + student + "(" + student._attendNo + ")");

                knjd154j.print(student);

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

        sd.closeSvf( svf, hasData );
        sd.closeDb(db2);
    }


    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private List createStudents(final DB2UDB db2, final Param param) {

        PreparedStatement ps = null;
        ResultSet rs1 = null;
        final List students = new ArrayList();
        try {
            final String sqlRegdData = Student.sqlRegdData(param);
            log.debug("sqlRegdData = " + sqlRegdData);
            ps = db2.prepareStatement(sqlRegdData);
            rs1 = ps.executeQuery();

            while (rs1.next()) {

                final Student student = new Student(rs1.getString("SCHREGNO"), param);
                students.add(student);

                student._grade = rs1.getString("GRADE");
                student._courseCd = rs1.getString("COURSECD");
                student._majorCd = rs1.getString("MAJORCD");
                student._courseCode = rs1.getString("COURSECODE");

                student._courseName = rs1.getString("COURSENAME");
                student._courseCodeName = null == rs1.getString("COURSECODENAME") ? "" : rs1.getString("COURSECODENAME");
                student._majorName = rs1.getString("MAJORNAME");
                student._hrName = null == rs1.getString("HR_NAME") ? "" : rs1.getString("HR_NAME");
                student._hrNameAbbv = null == rs1.getString("HR_NAMEABBV") ? "" : rs1.getString("HR_NAMEABBV");
                final String attendNo = rs1.getString("ATTENDNO");
                student._attendNo = null == attendNo || !NumberUtils.isDigits(attendNo) ? "" : Integer.valueOf(attendNo) + "番";
                final String name = "1".equals(rs1.getString("USE_REAL_NAME")) ? rs1.getString("REAL_NAME") : rs1.getString("NAME");
                student._name = null == name ? "" : name;

                //log.debug("対象の生徒" + student);
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs1);
            db2.commit();
        }

        if (students.size() == 0) {
            log.warn("対象の生徒がいません");
            return students;
        }

        Attendance.setAttendData(db2, students, param);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            Score.setScore(db2, student, param);
            Student.setSlump(db2, student, param);
//            Attendance.setSpecialSubclassAttendData(student, param);
//            AbsenceHigh.setAbsenceHigh(db2, student, param);
            Subclass.setRecDetail(db2, student, param);
        }
//        Student.setHExamRecordRemarkDat(db2, students, param);
//        Student.setRecordDocumentKindDatFootnote(db2, students, param);
//        RecordTotalStudyTimeDat.setTotalStudy(db2, students, param);

        return students;
    }

    private static class Student {

        public String _attendNo;
        public String _grade;

        public String _courseCd;
        public String _courseName;

        public String _majorCd;
        public String _majorName;

        public String _courseCode;
        public String _courseCodeName;

        public String _hrName;
        public String _hrNameAbbv;

        public String _name;

        final String _schregno;

        final List _subclassInfos = new ArrayList();

        int _totalCredit = 0;

        final Map _subclassScoreMap;

//        final Map _attendMap = new TreeMap();

        final Map _scoreMap = new HashMap();

//        final Map _subclassAbsenceHigh = new HashMap();
//
//        final Map _spSubclassAbsenceHigh = new HashMap();
//
//        final Map _specialGroupAttendanceMap = new HashMap();

        final Map _recordTotalStudieTimeDat = new HashMap();

        final Map _scoreTotalInfo = new LinkedMap();

//        String _hexamRecordRemarkRemark1;

//        String _recordDocumentKindDatFootnote;

//        public Address _address;

        public Student(final String schregno, final Param param) {
            _schregno = schregno;
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                _subclassScoreMap = new LinkedMap();
            } else {
                _subclassScoreMap = new HashMap();
            }
        }

//        public void setAttendance(final String semester, final Attendance a) {
//            _attendMap.put(semester, a);
//        }
//
//        public Attendance getAttendance(final String semester) {
//            final Attendance a = (Attendance) _attendMap.get(semester);
//            return (a == null) ? new Attendance() : a;
//        }

        public void setScoreTotalInfo(final String seme_testkindcd, final TotalScoreInfo vl) {
            _scoreTotalInfo.put(seme_testkindcd, vl);
        }

        public TotalScoreInfo getScoreTotalInfo(final String seme_testkindcd) {
            return (TotalScoreInfo)_scoreTotalInfo.get(seme_testkindcd);
        }

//        public void setSubclassAttendance(final String subclassCd, final String semester, final SubclassAttendance sa) {
//            findSubclass(subclassCd).setAttendance(semester, sa);
//        }
//
//        public SubclassAttendance getSubclassAttendance(final String subclassCd, final String semester) {
//            return findSubclass(subclassCd).getAttendance(semester);
//        }

        private static Map getMappedMap(final Map map, final String key) {
            if (!map.containsKey(key)) {
                map.put(key, new HashMap());
            }
            return (Map) map.get(key);
        }

        public void putSlump(final String subclassCd, final String testKindcd, final String slump) {
            findSubclass(subclassCd).setSlump(testKindcd, slump);
        }

        public boolean isSlump(final String subclassCd, final String testKindCd) {
            return findSubclass(subclassCd).isSlump(testKindCd);
        }

        public void putScore(final String subclassCd, final String testKindcd, final String tableDiv, final Score score) {
            findSubclass(subclassCd).setScoreValue(testKindcd, tableDiv, score);
        }

        public Score getScore(final String subclassCd, final String testKindCd, final String tableDiv) {
            return findSubclass(subclassCd).getScore(testKindCd, tableDiv);
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        private Subclass findSubclass(final String subclassCd) {
            if (null == getSubclass(subclassCd)) {
                _subclassScoreMap.put(subclassCd, new Subclass(_schregno, subclassCd));
            }
            return (Subclass) _subclassScoreMap.get(subclassCd);
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        public Subclass getSubclass(final String subclassCd) {
            return (Subclass) _subclassScoreMap.get(subclassCd);
        }

//        public SpecialSubclassAttendance getSpecialSubclassAttendance(final String specialGroupCd, final String testKindCd) {
//            Map map = getSpecialSubclassAttendanceMap(testKindCd);
//            if (!map.containsKey(specialGroupCd)) {
//                map.put(specialGroupCd, new SpecialSubclassAttendance(specialGroupCd));
//            }
//            return (SpecialSubclassAttendance) map.get(specialGroupCd);
//        }
//
//        public Map getSpecialSubclassAttendanceMap(final String testKindCd) {
//            if (!_specialGroupAttendanceMap.containsKey(testKindCd)) {
//                _specialGroupAttendanceMap.put(testKindCd, new HashMap());
//            }
//            return (Map) _specialGroupAttendanceMap.get(testKindCd);
//        }
//
//        public AbsenceHigh getAbsenceHigh(final String subclassCd) {
//            return (AbsenceHigh) _subclassAbsenceHigh.get(subclassCd);
//        }
//
//        public AbsenceHigh getSpecialAbsenceHigh(final String specialSubclassGroupCd) {
//            return (AbsenceHigh) _spSubclassAbsenceHigh.get(specialSubclassGroupCd);
//        }

        private static Student getStudent(final String schregno, final List students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        /**
         * 素点、平均をセットする。
         * @param db2
         * @param student
         * @param param
         */
        private static void setSlump(final DB2UDB db2, final Student student, final Param param) {

            //不振科目
            final String prestatementRecordSlump = sqlRecordSlump(param, student._schregno);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(prestatementRecordSlump);

                rs = ps.executeQuery();
                while (rs.next()) {
                    String subclassCd = rs.getString("SUBCLASSCD");
                    if ("1".equals(param._useCurriculumcd)) {
                        subclassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");
                    }
                    final String testKindCd = rs.getString("TESTKINDCD");

                    student.putSlump(subclassCd, testKindCd, SLUMP_CD);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlRecordSlump(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT SEMESTER || TESTKINDCD || TESTITEMCD AS TESTKINDCD, SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("        FROM RECORD_SLUMP_DAT ");
            stb.append("   WHERE YEAR = '" + param._year + "' AND SCHREGNO = '" + schregno +"' ");
            stb.append("        AND SLUMP = '" + SLUMP_CD + "' ");
            return stb.toString();
        }

        private static String sqlRegdData (final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1,V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectSchregno) + " ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");

            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T2.HR_NAMEABBV,");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append(        "T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '04' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }

//        private static void setRecordDocumentKindDatFootnote(final DB2UDB db2, final List students, final Param param) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T1.FOOTNOTE ");
//            stb.append(" FROM ");
//            stb.append("     RECORD_DOCUMENT_KIND_DAT T1 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year +"' ");
//            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
//            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testKindCd + "' ");
//            stb.append("     AND T1.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' ");
//            stb.append("     AND T1.HR_CLASS = '000' ");
//            stb.append("     AND T1.COURSECD = '0' ");
//            stb.append("     AND T1.MAJORCD = '000' ");
//            stb.append("     AND T1.COURSECODE = '0000' ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     AND T1.CLASSCD = '00' ");
//                stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
//                stb.append("     AND T1.CURRICULUM_CD = '00' ");
//            }
//            stb.append("     AND T1.SUBCLASSCD = '000000' ");
//            stb.append("     AND T1.KIND_DIV = '1' ");
//
//            final String sql = stb.toString();
//            String footnote = "";
//            try {
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    footnote = rs.getString("FOOTNOTE");
//                }
//            } catch (SQLException e) {
//                log.error("sql exception! :" + sql, e);
//            } catch (Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//
//            for (final Iterator it = students.iterator(); it.hasNext();) {
//                final Student student= (Student) it.next();
//                student._recordDocumentKindDatFootnote = footnote;
//            }
//        }

//        /**
//         * 通知表所見
//         * @param db2
//         * @param students
//         * @param param
//         */
//        private static void setHExamRecordRemarkDat(final DB2UDB db2, final List students, final Param param) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     SEMESTER ");
//            stb.append("     ,REMARK1");
//            stb.append(" FROM ");
//            stb.append("     HEXAM_RECORD_REMARK_DAT ");
//            stb.append(" WHERE ");
//            stb.append("     YEAR = '" + param._year + "' ");
//            stb.append("     AND SEMESTER = '" + param._semester + "' ");
//            stb.append("     AND SCHREGNO = ? ");
//            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testKindCd + "' ");
//            stb.append("     AND REMARK_DIV = '4' ");
//
//            final String sql = stb.toString();
//            try {
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = students.iterator(); it.hasNext();) {
//                    final Student student= (Student) it.next();
//                    ps.setString(1, student._schregno);
//                    rs = ps.executeQuery();
//                    student._hexamRecordRemarkRemark1 = null;
//
//                    while (rs.next()) {
//                        String remark1 = rs.getString("REMARK1");
//                        student._hexamRecordRemarkRemark1 = remark1;
//                    }
//                }
//
//            } catch (SQLException e) {
//                log.error("sql exception! :" + sql, e);
//            } catch (Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

//    private static class Address {
//        final String _addressee;
//        final String _address1;
//        final String _address2;
//        final String _zipcd;
//
//        public Address(final String addressee, final String address1, final String address2, final String zipcd) {
//            _addressee = addressee;
//            _address1 = address1;
//            _address2 = address2;
//            _zipcd = zipcd;
//        }
//
//    }

    private static class Attendance {

        public static final String GROUP_LHR = "001";
        public static final String GROUP_ASS = "002";
        public static final String GROUP_SHR = "004";

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

//        Map _spGroupLessons = new HashMap();
//
//        Map _spGroupKekka = new HashMap();
//
//        int _spLesson;
//
//        int _spKekka;
//
//        int _spShrKoma;

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

//        public int getSpGroupKekka(final String groupCd) {
//            Integer kekka = (Integer) _spGroupKekka.get(groupCd);
//            return kekka == null ? 0 : kekka.intValue();
//        }

//        /**
//         * 特活の出欠データをセットする。
//         * @param student
//         * @param param
//         */
//        private static void setSpecialSubclassAttendData(final Student student, final Param param) {
//
//            final String[] semesters = param.getTargetSemester();
//
//            final Map spGroupLessons = new HashMap();
//            final Map spGroupKekka = new HashMap();
//
//            for (int i = 0; i < semesters.length; i++) {
//                final String semester = semesters[i];
//
//                BigDecimal spLessonJisu = new BigDecimal(0);
//                BigDecimal spKekkaJisu = new BigDecimal(0);
//                int spShrKoma = 0;
//                final Map specialSubclassAttendanceMap = student.getSpecialSubclassAttendanceMap(semester);
//
//                for (final Iterator it = specialSubclassAttendanceMap.keySet().iterator(); it.hasNext();) {
//                    final String specialGroupCd = (String) it.next();
//                    final SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);
//
//                    final BigDecimal spGroupLessonJisu = getSpecialAttendExe(ssa.spLessonMinutesTotal(), param);
//                    final BigDecimal spGroupAbsenceJisu = getSpecialAttendExe(ssa.spAbsenceMinutesTotal(), param);
//
//                    spGroupLessons.put(specialGroupCd, new Integer(spGroupLessonJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
//                    spGroupKekka.put(specialGroupCd, new Integer(spGroupAbsenceJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
//
//                    spLessonJisu = spLessonJisu.add(spGroupLessonJisu);
//                    spKekkaJisu = spKekkaJisu.add(spGroupAbsenceJisu);
//                    if (Attendance.GROUP_SHR.equals(specialGroupCd)) {
//                        spShrKoma += ssa.spAbsenceKomaTotal();
//                    }
//                }
//
//                final Attendance att = student.getAttendance(semester);
//                att._spLesson = spLessonJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//                att._spKekka = spKekkaJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//                att._spShrKoma = spShrKoma;
//                att._spGroupLessons = spGroupLessons;
//                att._spGroupKekka = spGroupKekka;
//            }
//        }


//        /**
//         * 欠課時分を欠課時数に換算した値を得る
//         * @param kekka 欠課時分
//         * @return 欠課時分を欠課時数に換算した値
//         */
//        private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
//            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
//            final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
//            int hasu = 0;
//            final String retSt = bigD.toString();
//            final int retIndex = retSt.indexOf(".");
//            if (retIndex > 0) {
//                hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
//            }
//            final BigDecimal rtn;
//            if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
//                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
//            } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
//                rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
//            } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
//                rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
//            } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
//                rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
//            } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
//                rtn = bigD;
//            } else {
//                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
//            }
//            return rtn;
//        }

        /**
         * 出欠データをセットする。
         * @param db2
         * @param students
         * @param param
         */
        private static void setAttendData(final DB2UDB db2, final List students, final Param param) {

            PreparedStatement ps = null;
            ResultSet rs = null;

//            final String[] targetSemesters = param.getTargetSemester();
//            for (int i = 0; i < targetSemesters.length; i++) {
//
//                String semester = targetSemesters[i];
//                final Semester semesS = (Semester) param._semesterMap.get(param._semesterMap.containsKey(semester) ? semester : Param.SEMEALL);
//                final String sdate = param._isRuikei ? param._sdate : semesS._sdate;
//                log.debug(sdate + " 〜 " + " (" + param._edate + " , " + semesS);
//                final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;
//                try {
//
//                    final String sql = AttendAccumulate.getAttendSemesSql(
//                            param._year,
//                            param._semester,
//                            sdate,
//                            edate,
//                            param._attendParamMap
//                    );
//
//                    log.debug(" attend semes sql = " + sql);
//                    ps = db2.prepareStatement(sql);
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
//                        if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
//                            continue;
//                        }
//                        int lesson = rs.getInt("LESSON");
//                        int mourning = rs.getInt("MOURNING");
//                        int suspend = rs.getInt("SUSPEND");
//                        int abroad = rs.getInt("TRANSFER_DATE");
//                        int mlesson = rs.getInt("MLESSON");
//                        int absence = rs.getInt("SICK");
//                        int attend = rs.getInt("PRESENT");
//                        int late = rs.getInt("LATE");
//                        int early = rs.getInt("EARLY");
//                        int virus = "true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0;
//                        int koudome = "true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0;
//
//                        final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, virus, koudome);
//                        log.debug(" schregno = " + student._schregno + ", semester = " + semester + " , attendance = " + attendance);
//                        student.setAttendance(semester, attendance);
//                    }
//
//                } catch (SQLException e) {
//                    log.error("sql exception!", e);
//                } catch (Exception e) {
//                    log.error("exception!", e);
//                } finally {
//                    DbUtils.closeQuietly(null, ps, rs);
//                    db2.commit();
//                }

//                String sql = null;
//                try {
//                    sql = AttendAccumulate.getAttendSubclassSql(
//                            param._year,
//                            Param.SEMEALL,
//                            sdate,
//                            edate,
//                            param._attendParamMap
//                    );
//
//                    // log.debug(" attend subclass sql = " + sql);
//                    ps = db2.prepareStatement(sql);
//                    rs = ps.executeQuery();
//
//                    while (rs.next()) {
//                        final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
//                        if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
//                            continue;
//                        }
//                        final String subclassCdC005 = rs.getString("SUBCLASSCD");
//                        String subclassCd = rs.getString("SUBCLASSCD");
//                        if ("1".equals(param._useCurriculumcd)) {
//                            final String[] split = StringUtils.split(subclassCd, "-");
//                            subclassCd = split[0] + split[1] + split[2] + split[3];
//                        }
//
//                        final BigDecimal lesson = rs.getBigDecimal("MLESSON");
//                        final BigDecimal rawSick = rs.getBigDecimal("SICK1");
//                        final BigDecimal sick = rs.getBigDecimal("SICK2");
//                        final BigDecimal absent = rs.getBigDecimal("ABSENT");
//                        final BigDecimal suspend = rs.getBigDecimal("SUSPEND");
//                        final BigDecimal koudome = rs.getBigDecimal("KOUDOME");
//                        final BigDecimal virus = rs.getBigDecimal("VIRUS");
//                        final BigDecimal mourning = rs.getBigDecimal("MOURNING");
//                        final BigDecimal late = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("LATE") : rs.getBigDecimal("LATE2");
//                        final BigDecimal early = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("EARLY") :rs.getBigDecimal("EARLY2");
//                        final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
//                        final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
//
//                        final SubclassAttendance sa = new SubclassAttendance(lesson, rawSick, sick, absent, suspend, koudome, virus, mourning, late, early, rawReplacedSick, replacedSick);
//
//                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
//                        if (specialGroupCd != null) {
//                            final int specialLessonMinutes = rs.getInt("SPECIAL_LESSON_MINUTES");
//
//                            int spAbsenceMinutes = 0;
//                            if (param._subClassC005.containsKey(subclassCdC005)) {
//                                String is = (String) param._subClassC005.get(subclassCdC005);
//                                if ("1".equals(is)) {
//                                    spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES3");
//                                } else if ("2".equals(is)) {
//                                    spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES2");
//                                }
//                            } else {
//                                spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES1");
//                            }
//
//                            final SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);
//                            ssa.add(subclassCd, lesson.intValue(), sick.intValue(), specialLessonMinutes, spAbsenceMinutes);
//                        }
//
//                        student.setSubclassAttendance(subclassCd, semester, sa);
//                        log.debug("   schregno = " + student._schregno + ", semester = " + semester + " , subclasscd = " + subclassCd + " , spGroupCd = " + specialGroupCd + " , " + sa);
//
//                    }
//                } catch (SQLException e) {
//                    log.error("sql exception! sql = " + sql, e);
//                } catch (Exception e) {
//                    log.error("exception!", e);
//                } finally {
//                    DbUtils.closeQuietly(null, ps, rs);
//                    db2.commit();
//                }
//
//            }
            
            //5/7教科合計のデータを取得する。
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
            	String sql = null;
            	try {
            		sql = getScoreTotalInfoSql(param, "1", students);
            		
            		log.debug(" attend subclass sql = " + sql);
            		ps = db2.prepareStatement(sql);
            		rs = ps.executeQuery();
            		
            		while (rs.next()) {
            			final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
            			if (student == null || "9".equals(rs.getString("SEMESTER"))) {
            				continue;
            			}
            			final String wksemester = rs.getString("SEMESTER");
            			final String subclasscd = rs.getString("SUBCLASSCD");
            			final String totaltestkindcd = rs.getString("TESTKINDCD");
            			final String totaltestitemcd = rs.getString("TESTITEMCD");
            			final String totalscore = rs.getString("SCORE");
            			final String totalgradeavg = rs.getString("GRADEAVG");
            			final String totalhravg = rs.getString("HRAVG");
            			final String totalcourseavg = rs.getString("COURSEAVG");
            			final String totalgradecount = rs.getString("GRADECNT");
            			final String totalhrcount = rs.getString("HRCNT");
            			final String totalcoursecount = rs.getString("COURSECNT");
            			final String totalgraderank = rs.getString("GRADE_RANK");
            			final String totalclassrank = rs.getString("CLASS_RANK");
            			final String totalcouserank = rs.getString("COURSE_RANK");
            			TotalScoreInfo addwk = new TotalScoreInfo(student._schregno, wksemester, subclasscd, totaltestkindcd, totaltestitemcd, "5",
            					totalscore, totalgradeavg, totalhravg, totalcourseavg, totalgradecount, totalhrcount, totalcoursecount,
            					totalgraderank, totalclassrank, totalcouserank);
            			student.setScoreTotalInfo("5"+wksemester+totaltestkindcd+totaltestitemcd, addwk);
            		}
            	} catch (SQLException e) {
            		log.error("sql exception! sql = " + sql, e);
            	} catch (Exception e) {
            		log.error("exception!", e);
            	} finally {
            		DbUtils.closeQuietly(null, ps, rs);
            		db2.commit();
            	}
            	
            	sql = null;
            	try {
            		sql = getScoreTotalInfoSql(param, "0", students);
            		
            		// log.debug(" attend subclass sql = " + sql);
            		ps = db2.prepareStatement(sql);
            		rs = ps.executeQuery();
            		
            		while (rs.next()) {
            			final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
            			if (student == null || "9".equals(rs.getString("SEMESTER"))) {
            				continue;
            			}
            			final String wksemester = rs.getString("SEMESTER");
            			final String subclasscd = rs.getString("SUBCLASSCD");
            			final String totaltestkindcd = rs.getString("TESTKINDCD");
            			final String totaltestitemcd = rs.getString("TESTITEMCD");
            			final String totalscore = rs.getString("SCORE");
            			final String totalgradeavg = rs.getString("GRADEAVG");
            			final String totalhravg = rs.getString("HRAVG");
            			final String totalcourseavg = rs.getString("COURSEAVG");
            			final String totalgradecount = rs.getString("GRADECNT");
            			final String totalhrcount = rs.getString("HRCNT");
            			final String totalcoursecount = rs.getString("COURSECNT");
            			final String totalgraderank = rs.getString("GRADE_RANK");
            			final String totalclassrank = rs.getString("CLASS_RANK");
            			final String totalcouserank = rs.getString("COURSE_RANK");
            			TotalScoreInfo addwk = new TotalScoreInfo(student._schregno, wksemester, subclasscd, totaltestkindcd, totaltestitemcd, "7",
            					totalscore, totalgradeavg, totalhravg, totalcourseavg, totalgradecount, totalhrcount, totalcoursecount,
            					totalgraderank, totalclassrank, totalcouserank);
            			student.setScoreTotalInfo("7"+wksemester+totaltestkindcd+totaltestitemcd, addwk);
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

        private static String getScoreTotalInfoSql(final Param param, final String getType, final List students) {
            final String[] targets = param.getTargetTestKindCds();
            int rank = 0;

            for (int i = 0; i < targets.length; i++) {
                if (param.isScoreFromRecordRankVDat(targets[i])) {
                } else {
                    rank += 1;
                }
            }
            final String[] fromRank = new String[rank];

            if (rank != 0) {
                for (int c = 0, i = 0; i < targets.length; i++) {
                    if (!param.isScoreFromRecordRankVDat(targets[i])) {
                        fromRank[c] = targets[i];
                        c += 1;
                    }
                }
            }

            String sep = "";
            String findStudentstr = "";
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                findStudentstr +=  sep + "'" + student._schregno + "'";
                sep = ",";
            }
            findStudentstr = "(" + findStudentstr + ")";

            final String chkavgsubclscd;

            if ("1".equals(getType)) {
                chkavgsubclscd = "333333";
            } else {
                chkavgsubclscd = "555555";
            }

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH T3DAT AS ( ");
            stb.append(" select  ");
            stb.append("     T3.YEAR, ");
            stb.append("     T3.SEMESTER, ");
            stb.append("     T3.TESTKINDCD, ");
            stb.append("     T3.TESTITEMCD, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
            stb.append("     T3.SUBCLASSCD, ");
            stb.append("     T3.AVG_DIV, ");
            stb.append("     T3.GRADE, ");
            stb.append("     T3.HR_CLASS, ");
            stb.append("     '0' AS COURSECD, ");
            stb.append("     '000' AS MAJORCD, ");
            stb.append("     '0000' AS COURSECODE, ");
            stb.append("     T3.SCORE, ");
            stb.append("     T3.HIGHSCORE, ");
            stb.append("     T3.LOWSCORE, ");
            stb.append("     T3.AVG, ");
            stb.append("     T3.COUNT, ");
            stb.append("     T3.STDDEV ");
            stb.append(" FROM RECORD_AVERAGE_DAT T3 ");
            stb.append(" WHERE ");
            stb.append("      T3.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("      AND T3.AVG_DIV = '1' ");
            stb.append("      AND T3.HR_CLASS = '000' ");
            stb.append("      AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '00000000' ");
            stb.append(" ), T4DAT AS ( ");
            stb.append(" SELECT ");
            stb.append("     T4.YEAR, ");
            stb.append("     T4.SEMESTER, ");
            stb.append("     T4.TESTKINDCD, ");
            stb.append("     T4.TESTITEMCD, ");
            stb.append("     T4.CLASSCD, ");
            stb.append("     T4.SCHOOL_KIND, ");
            stb.append("     T4.CURRICULUM_CD, ");
            stb.append("     T4.SUBCLASSCD, ");
            stb.append("     T4.AVG_DIV, ");
            stb.append("     T4.GRADE, ");
            stb.append("     T4.HR_CLASS, ");
            stb.append("     T4.COURSECD, ");
            stb.append("     T4.MAJORCD, ");
            stb.append("     T4.COURSECODE, ");
            stb.append("     T4.SCORE, ");
            stb.append("     T4.HIGHSCORE, ");
            stb.append("     T4.LOWSCORE, ");
            stb.append("     T4.AVG, ");
            stb.append("     T4.COUNT, ");
            stb.append("     T4.STDDEV ");
            stb.append(" FROM RECORD_AVERAGE_DAT T4 ");
            stb.append(" WHERE ");
            stb.append("     T4.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("     AND T4.AVG_DIV = '2' ");
            stb.append("     AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
            stb.append(" ),T5DAT AS ( ");
            stb.append(" SELECT ");
            stb.append("     T5.YEAR, ");
            stb.append("     T5.SEMESTER, ");
            stb.append("     T5.TESTKINDCD, ");
            stb.append("     T5.TESTITEMCD, ");
            stb.append("     T5.CLASSCD, ");
            stb.append("     T5.SCHOOL_KIND, ");
            stb.append("     T5.CURRICULUM_CD, ");
            stb.append("     T5.SUBCLASSCD, ");
            stb.append("     T5.AVG_DIV, ");
            stb.append("     T5.GRADE, ");
            stb.append("     T5.HR_CLASS, ");
            stb.append("     T5.COURSECD, ");
            stb.append("     T5.MAJORCD, ");
            stb.append("     T5.COURSECODE, ");
            stb.append("     T5.SCORE, ");
            stb.append("     T5.HIGHSCORE, ");
            stb.append("     T5.LOWSCORE, ");
            stb.append("     T5.AVG, ");
            stb.append("     T5.COUNT, ");
            stb.append("     T5.STDDEV ");
            stb.append(" FROM RECORD_AVERAGE_DAT T5 ");
            stb.append(" WHERE ");
            stb.append("     T5.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("     AND T5.AVG_DIV = '3' ");
            stb.append("     AND T5.HR_CLASS = '000' ");
            stb.append(" ), BASEDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
            stb.append("     T2.COURSECD, ");
            stb.append("     T2.MAJORCD, ");
            stb.append("     T2.COURSECODE, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.GRADE_RANK, ");
            stb.append("     T1.CLASS_RANK, ");
            stb.append("     T1.COURSE_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append("      LEFT JOIN SCHREG_REGD_DAT T2 ");
            stb.append("         ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T2.SCHREGNO IN " + findStudentstr + " ");
            stb.append("     AND T1.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("     AND T1.TESTKINDCD <> '99' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, fromRank));
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.YEAR, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.TESTKINDCD, ");
            stb.append("    T1.TESTITEMCD, ");
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.SCHOOL_KIND, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SUBCLASSCD, ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.SCORE, ");
            stb.append("    T1.GRADE_RANK, ");
            stb.append("    T1.CLASS_RANK, ");
            stb.append("    T1.COURSE_RANK, ");
            stb.append("    T3.AVG AS GRADEAVG, ");
            stb.append("    T3.COUNT AS GRADECNT, ");
            stb.append("    T4.AVG AS HRAVG, ");
            stb.append("    T4.COUNT AS HRCNT, ");
            stb.append("    T5.AVG AS COURSEAVG, ");
            stb.append("    T5.COUNT AS COURSECNT ");
            stb.append(" FROM ");
            stb.append("    BASEDAT T1 ");
            stb.append("    LEFT JOIN T3DAT T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR ");
            stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T3.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("     AND T3.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("     AND T3.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("     AND T3.GRADE = T1.GRADE ");
            stb.append("    LEFT JOIN T4DAT T4 ");
            stb.append("     ON T4.YEAR = T1.YEAR ");
            stb.append("     AND T4.SEMESTER =  T1.SEMESTER ");
            stb.append("     AND T4.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("     AND T4.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("     AND T4.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("     AND T4.GRADE = T1.GRADE ");
            stb.append("     AND T4.HR_CLASS = T1.HR_CLASS ");
            stb.append("    LEFT JOIN T5DAT T5 ");
            stb.append("     ON T5.YEAR = T1.YEAR ");
            stb.append("     AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T5.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("     AND T5.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("     AND T5.SUBCLASSCD = '" + chkavgsubclscd + "' ");
            stb.append("     AND T5.GRADE = T1.GRADE ");
            stb.append("     AND T5.COURSECD = T1.COURSECD ");
            stb.append("     AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("     AND T5.COURSECODE = T1.COURSECODE ");
            
            return stb.toString();
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
    }

//    private static class SubclassAttendance {
//        private final int _lesson;
//
//        final BigDecimal _rawSick;
//
//        final BigDecimal _sick;
//
//        final int _absent;
//
//        final int _suspend;
//
//        final int _koudome;
//
//        final int _virus;
//
//        final int _mourning;
//
//        final int _lateearly;
//
//        final BigDecimal _rawReplacedSick;
//
//        final BigDecimal _replacedSick;
//
//        SubclassAttendance(final BigDecimal lesson, final BigDecimal rawSick, final BigDecimal sick, final BigDecimal absent, final BigDecimal suspend, final BigDecimal koudome, final BigDecimal virus, final BigDecimal mourning,
//                final BigDecimal late, final BigDecimal early, final BigDecimal rawReplacedSick, final BigDecimal replacedSick) {
//            _lesson = lesson.intValue();
//            _rawSick = rawSick;
//            _sick = sick;
//            _absent = absent.intValue();
//            _suspend = suspend.intValue();
//            _koudome = koudome.intValue();
//            _virus = virus.intValue();
//            _mourning = mourning.intValue();
//            _lateearly = late.add(early).intValue();
//            _rawReplacedSick = rawReplacedSick;
//            _replacedSick = replacedSick;
//        }
//
//        public String getRawSick() {
//            return (_rawSick == null) ? null : formatBigDecimal(_rawSick);
//        }
//
//        public String getSick() {
//            return (_sick == null) ? null : formatBigDecimal(_sick);
//        }
//
//        public String getRawReplacedSick() {
//            return (_rawReplacedSick == null) ? null : formatBigDecimal(_rawReplacedSick);
//        }
//
//        public String getReplacedSick() {
//            return (_replacedSick == null) ? null : formatBigDecimal(_replacedSick);
//        }
//
//        public String getLesson() {
//            return String.valueOf(_lesson);
//        }
//
//        private String getKekkaString() {
//            return "lesson = " + _lesson + " , rawSick = " + _rawSick + " , sick = " + _sick + " , absent = " + _absent + " , susmour = (" + _suspend + " , " + _mourning +
//            ") , lateearly = " + _lateearly + ((_replacedSick.intValue() != 0) ? " , replacedSick = " + _replacedSick : "");
//        }
//
//        public String toString() {
//            return getKekkaString();
//        }
//
//        public String getLateEarly() {
//            return formatInt(_lateearly);
//        }
//
//        public String getKoketsu() {
//            return formatInt(_absent);
//        }
//
//        public String getMourning() {
//            return formatInt(_mourning);
//        }
//
//        public String getSuspend() {
//            return formatInt(_suspend + _koudome + _virus);
//        }
//
//        public String getMourningSuspend() {
//            return formatInt(_mourning + _suspend + _koudome + _virus);
//        }
//
//        private String formatInt(int n) {
//            return n == 0 ? "" : String.valueOf(n);
//        }
//
//        private String formatBigDecimal(BigDecimal n) {
//            return n == null ? null : (n.intValue() == 0) ? formatInt(0) : String.valueOf(n);
//        }
//    }

//    private static class SpecialSubclassAttendance {
//        final Map _spLessonMinutes;
//        final Map _spAbsenceMinutes;
//        final Map _spLessonKoma;
//        final Map _spAbsenceKoma;
//        final String _spGroupCd;
//
//        public SpecialSubclassAttendance(final String spGroupCd) {
//            _spGroupCd = spGroupCd;
//            _spLessonMinutes = new HashMap();
//            _spAbsenceMinutes = new HashMap();
//            _spLessonKoma = new HashMap();
//            _spAbsenceKoma = new HashMap();
//        }
//
//        public void add(final String subclasscd, final int splessonKoma, final int spAbsenceKoma, final int splessonMinutes, final int spAbsenceMinutes) {
//            add(_spLessonKoma, subclasscd, splessonKoma);
//            add(_spAbsenceKoma, subclasscd, spAbsenceKoma);
//            add(_spLessonMinutes, subclasscd, splessonMinutes);
//            add(_spAbsenceMinutes, subclasscd, spAbsenceMinutes);
//        }
//
//        public int spLessonKomaTotal() {
//            return mapValueTotal(_spLessonKoma);
//        }
//
//        public int spAbsenceKomaTotal() {
//            return mapValueTotal(_spAbsenceKoma);
//        }
//
//        public int spLessonMinutesTotal() {
//            return mapValueTotal(_spLessonMinutes);
//        }
//
//        public int spAbsenceMinutesTotal() {
//            return mapValueTotal(_spAbsenceMinutes);
//        }
//
//        private int mapValueTotal(final Map map) {
//            int total = 0;
//            for (final Iterator its = map.values().iterator(); its.hasNext();) {
//                final Integer intnum = (Integer) its.next();
//                total += intnum.intValue();
//            }
//            return total;
//        }
//
//        private void add(final Map subclasscdIntMap, final String subclasscd, final int intnum) {
//            if (!subclasscdIntMap.containsKey(subclasscd)) {
//                subclasscdIntMap.put(subclasscd, new Integer(0));
//            }
//            final Integer intn = (Integer) subclasscdIntMap.get(subclasscd);
//            subclasscdIntMap.put(subclasscd, new Integer(intn.intValue() + intnum));
//        }
//
//        public String getMinutesString() {
//            return " spGroupCd = " + _spGroupCd + " , spLessonMinutes = " + _spLessonMinutes + " , spAbsenceMinutes = " + _spAbsenceMinutes;
//        }
//
//        public String toString() {
//            return getMinutesString();
//        }
//    }

    private static class TotalScoreInfo {
        final String _schregno;
        final String _semester;
        final String _subclasscd;
        final String _testkindcd;
        final String _testitemcd;
        final String _totaltype;
        final String _score;
        final String _gradeavg;
        final String _hravg;
        final String _courseavg;
        final String _gradecount;
        final String _hrcount;
        final String _coursecount;
        final String _graderank;
        final String _classrank;
        final String _courserank;

        public TotalScoreInfo(
                final String schregno,
                final String semester,
                final String subclasscd,
                final String testkindcd,
                final String testitemcd,
                final String totaltype,
                final String score,
                final String gradeavg,
                final String hravg,
                final String courseavg,
                final String gradecount,
                final String hrcount,
                final String coursecount,
                final String graderank,
                final String classrank,
                final String courserank
                ) {

            _schregno = schregno;
            _semester = semester;
            _subclasscd = subclasscd;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _totaltype = totaltype;
            _score = score;
            _gradeavg = gradeavg;
            _hravg = hravg;
            _courseavg = courseavg;
            _gradecount = gradecount;
            _hrcount = hrcount;
            _coursecount = coursecount;
            _graderank = graderank;
            _classrank = classrank;
            _courserank = courserank;
        }
        public String getAvgScore(final Param param) {
            String avgScore = null;
            if ("1".equals(param._avgDiv)) {
                avgScore = _gradeavg;
            } else if ("2".equals(param._avgDiv)) {
                avgScore = _hravg;
            } else if ("3".equals(param._avgDiv)) {
                avgScore = _courseavg;
            }
            return avgScore;
        }
        public String getCount(final Param param) {
            String cntScore = null;
            if ("1".equals(param._avgDiv)) {
                cntScore = _gradecount;
            } else if ("2".equals(param._avgDiv)) {
                cntScore = _hrcount;
            } else if ("3".equals(param._avgDiv)) {
                cntScore = _coursecount;
            }
            return cntScore;
        }
        public String getRankScore(final Param param) {
            String rankScore = null;
            if ("1".equals(param._avgDiv)) {
                rankScore = _graderank;
            } else if ("2".equals(param._avgDiv)) {
                rankScore = _classrank;
            } else if ("3".equals(param._avgDiv)) {
                rankScore = _courserank;
            }
            return rankScore;
        }
    }

    private static class Subclass {
        final String _schregno;
        final String _subclassCd;
        final String _classCd;

        Map _testKindScoreMap = new HashMap();

//        Map _attendSubclassMap = new HashMap();

        Map _slumpMap = new HashMap();

        public Subclass(
                final String schregno,
                final String subclassCd
        ) {
            _schregno = schregno;
            _subclassCd = subclassCd;
            _classCd = subclassCd.substring(0, 2);
        }

        public Map findTestKindTableMap(final String testKindCd) {
            if (!_testKindScoreMap.containsKey(testKindCd)) {
                _testKindScoreMap.put(testKindCd, new HashMap());
            }
            return (Map) _testKindScoreMap.get(testKindCd);
        }

        public void setScoreValue(final String testKindCd, final String tableDiv, final Score score) {
            findTestKindTableMap(testKindCd).put(tableDiv, score);
        }

        private Score getScore(final String testKindCd, final String tableDiv) {
            return (Score) findTestKindTableMap(testKindCd).get(tableDiv);
        }

        public Score getScore(final String testKindCd) {
            return (Score) findTestKindTableMap(testKindCd).get(Param.REC);
        }

//        public void setAttendance(final String semester, final SubclassAttendance sa) {
//            _attendSubclassMap.put(semester, sa);
//        }
//
//        public SubclassAttendance getAttendance(final String semester) {
//            return (SubclassAttendance) _attendSubclassMap.get(semester);
//        }

        public void setSlump(final String testKind, final String slump) {
            _slumpMap.put(testKind, slump);
        }

        public boolean isSlump(final String testKind) {
            return SLUMP_CD.equals(_slumpMap.get(testKind));
        }

        private static String sqlSubclass (final Param param) {

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
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T2.CLASSCD, ");
                stb.append("        T2.SCHOOL_KIND, ");
                stb.append("        T2.CURRICULUM_CD, ");
                stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("        T2.SUBCLASSCD, ");
            }
            stb.append("        MIN(T3.STAFFCD) AS STAFFCD");
            stb.append("   FROM    CHAIR_STD_DAT T1 ");
            stb.append("   INNER JOIN CHAIR_DAT T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("   LEFT JOIN CHAIR_STF_DAT T3 ON T1.YEAR = T3.YEAR AND T1.SEMESTER = T3.SEMESTER AND T1.CHAIRCD = T3.CHAIRCD ");
            stb.append("   WHERE   T1.SCHREGNO = ?");
            stb.append("       AND T1.YEAR = '" + param._year + "'");
            stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("   GROUP BY ");
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T2.CLASSCD, ");
                stb.append("        T2.SCHOOL_KIND, ");
                stb.append("        T2.CURRICULUM_CD, ");
                stb.append("        T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            } else {
                stb.append("        T2.SUBCLASSCD ");
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
            stb.append("       , N1.NAMECD2 AS NUM90_OTHER");
            stb.append("       , (SELECT NUM90 FROM SUBCLASSNUM) AS NUM90");
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
            stb.append("       , CASE WHEN T101.STAFFCD IS NOT NULL THEN VALUE(T10.STAFFNAME_REAL, T10.STAFFNAME) ELSE T10.STAFFNAME END AS STAFFNAME ");
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                stb.append("   , CASE WHEN N005.YEAR IS NOT NULL THEN '5' WHEN N006.YEAR IS NOT NULL THEN '7' ELSE '' END AS WKSUBCLASSTYPE ");
            }
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
            stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD = '99900' ");
            stb.append(" LEFT JOIN STAFF_MST T10 ON T10.STAFFCD = T2.STAFFCD ");
            stb.append(" LEFT JOIN STAFF_NAME_SETUP_DAT T101 ON T101.YEAR = '" + param._year + "' AND T101.STAFFCD = T10.STAFFCD AND T101.DIV = '04' ");
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                stb.append(" LEFT JOIN SCHREG_REGD_DAT SRD ");
                stb.append("    ON SRD.SCHREGNO = T2.SCHREGNO ");
                stb.append("   AND SRD.YEAR = '" + param._year + "' ");
                stb.append("   AND SRD.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT N005 ");
                stb.append("    ON N005.YEAR = '" + param._year + "' ");
                stb.append("   AND N005.GROUP_DIV = '3' ");
                stb.append("   AND N005.GRADE = SRD.GRADE ");
                stb.append("   AND N005.COURSECD = SRD.COURSECD ");
                stb.append("   AND N005.MAJORCD = SRD.MAJORCD ");
                stb.append("   AND N005.COURSECODE = SRD.COURSECODE ");
                stb.append("   AND N005.CLASSCD || N005.SCHOOL_KIND || N005.CURRICULUM_CD || N005.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT N006 ");
                stb.append("    ON N006.YEAR = '" + param._year + "' ");
                stb.append("   AND N006.GROUP_DIV = '5' ");
                stb.append("   AND N006.GRADE = SRD.GRADE ");
                stb.append("   AND N006.COURSECD = SRD.COURSECD ");
                stb.append("   AND N006.MAJORCD = SRD.MAJORCD ");
                stb.append("   AND N006.COURSECODE = SRD.COURSECODE ");
                stb.append("   AND N006.CLASSCD || N006.SCHOOL_KIND || N006.CURRICULUM_CD || N006.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("   (N005.YEAR IS NOT NULL OR N006.YEAR IS NOT NULL) ");
            }
            
            stb.append(" ORDER BY ");
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                stb.append("    WKSUBCLASSTYPE, ");
            }
            stb.append("   ORDER0, ORDER1, ORDER2 ");
            stb.append("   ,T2.SUBCLASSCD ");
            return stb.toString();
        }

        /**
         * 成績データを得る。
         * @param db2
         * @param student
         * @param param
         */
        private static void setRecDetail (
                final DB2UDB db2,
                final Student student,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            student._subclassInfos.clear();
            student._totalCredit = 0;
            try {
                final String prestatementSubclass = sqlSubclass(param);
                // log.debug(" subclass sql = " + prestatementSubclass);
                ps = db2.prepareStatement(prestatementSubclass);

                int pp = 0;
                ps.setString(++pp, student._schregno);
                ps.setString(++pp, student._schregno);
                ps.setString(++pp, student._schregno);
                rs = ps.executeQuery();

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
                    final String calculateCreditFlg = (null == rs.getString("CALCULATE_CREDIT_FLG")) ? "0" : rs.getString("CALCULATE_CREDIT_FLG");
                    final String num90 = rs.getString("NUM90");
                    final String num90Other = rs.getString("NUM90_OTHER");
                    final String staffname = rs.getString("STAFFNAME");

                    final String classCd = subclassCd.substring(0, 2);
                    final String wksubclasstype;
                    if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                        wksubclasstype = rs.getString("WKSUBCLASSTYPE");
                    } else {
                        wksubclasstype = "";
                    }
                    boolean isTarget = KNJDefineSchool.subject_D.compareTo(classCd) <= 0 && classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T);
                    if (!isTarget) {
                        continue;
                    }

                    final Subclass subclass = student.getSubclass(subclassCd);

                    final SubclassInfo info = new SubclassInfo(classname, classabbv, subclassname, credits, compCredit, getCredit, subclassCd,
                            namespare1, subclass, replaceflg, calculateCreditFlg, num90, num90Other, staffname, wksubclasstype);
                    student._subclassInfos.add(info);

                    if (null == info._getCredit || 1 == info._replaceflg || 2 == info._replaceflg) {
                    } else {
                        student._totalCredit += Integer.parseInt(info._getCredit);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String toString() {
            return _schregno + " : " + _subclassCd;
        }
    }

//    private static class AbsenceHigh {
//        final String _compAbsenceHigh;
//        final String _getAbsenceHigh;
//
//        public AbsenceHigh(final String absenceHigh, final String getAbsenceHigh) {
//            _compAbsenceHigh = absenceHigh;
//            _getAbsenceHigh = getAbsenceHigh;
//        }
//
//        public boolean isRishuOver(final String kekka) {
//            return isOver(kekka, _compAbsenceHigh);
//        }
//
//        public boolean isShutokuOver(final String kekka) {
//            return isOver(kekka, _getAbsenceHigh);
//        }
//
//        private static boolean isOver(final String kekka, final String absenceHigh) {
//            if (null == kekka || !NumberUtils.isNumber(kekka) || Double.parseDouble(kekka) == 0) {
//                return false;
//            }
//            return absenceHigh == null || Double.parseDouble(absenceHigh) < Double.parseDouble(kekka);
//        }
//
//        /**
//         * 欠課数上限値を得る。
//         * @param db2
//         * @param student
//         * @param param
//         */
//        private static void setAbsenceHigh(final DB2UDB db2, final Student student, final Param param) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            final String absenceHighSql;
//            final String spAbsenceHighSql;
//            if (param._knjSchoolMst.isHoutei()) {
//                absenceHighSql = sqlHouteiJisu(student, null, param, false);
//                spAbsenceHighSql = sqlHouteiJisu(student, null, param, true);
//            } else {
//                absenceHighSql = sqlJituJisuSql(student, null, param, false);
//                spAbsenceHighSql = sqlJituJisuSql(student, null, param, true);
//            }
//
//            try {
//                ps = db2.prepareStatement(absenceHighSql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String absenceHigh = rs.getString("ABSENCE_HIGH") == null ? "0" : rs.getString("ABSENCE_HIGH");
//                    final String getAbsenceHigh = rs.getString("GET_ABSENCE_HIGH") == null ? "0" : rs.getString("GET_ABSENCE_HIGH");
//
//                    String subclassCd = rs.getString("SUBCLASSCD");
//                    if ("1".equals(param._useCurriculumcd)) {
//                        subclassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");
//                    }
//                    student._subclassAbsenceHigh.put(subclassCd, new AbsenceHigh(absenceHigh, getAbsenceHigh));
//                }
//            } catch (SQLException e) {
//                log.error(e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//
//
//            try {
//                ps = db2.prepareStatement(spAbsenceHighSql);
//                rs = ps.executeQuery();
//                student._spSubclassAbsenceHigh.clear();
//                while (rs.next()) {
//                    final String compAbsenceHigh = rs.getString("ABSENCE_HIGH") == null ? "0" : rs.getString("ABSENCE_HIGH");
//                    final String getAbsenceHigh = rs.getString("GET_ABSENCE_HIGH") == null ? "0" : rs.getString("GET_ABSENCE_HIGH");
//
//                    student._spSubclassAbsenceHigh.put(rs.getString("SPECIAL_GROUP_CD"), new AbsenceHigh(compAbsenceHigh, getAbsenceHigh));
//                }
//            } catch (SQLException e) {
//                log.error(e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//
//        }
//
//        private static String sqlHouteiJisu(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
//            final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T2.SCHREGNO, ");
//            if (!isGroup) {
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("     T1.CLASSCD, ");
//                    stb.append("     T1.SCHOOL_KIND, ");
//                    stb.append("     T1.CURRICULUM_CD, ");
//                }
//                stb.append("     T1.SUBCLASSCD, ");
//            } else {
//                stb.append("     T1.SPECIAL_GROUP_CD, ");
//            }
//            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
//            stb.append("       AS ABSENCE_HIGH, ");
//            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
//            stb.append("       AS GET_ABSENCE_HIGH ");
//            stb.append(" FROM ");
//            stb.append("     " + tableName + " T1 ");
//            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
//            stb.append("       T2.GRADE = T1.GRADE AND ");
//            stb.append("       T2.COURSECD = T1.COURSECD AND ");
//            stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
//            stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
//            stb.append("       T2.YEAR = T1.YEAR AND ");
//            stb.append("       T2.SEMESTER = '" + param.getRegdSemester() + "' ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year + "' ");
//            if (!isGroup) {
//                if (null != subclassCd) {
//                    if ("1".equals(param._useCurriculumcd)) {
//                        stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '" + subclassCd + "' ");
//                    } else {
//                        stb.append("     AND T1.SUBCLASSCD = '" + subclassCd + "' ");
//                    }
//                }
//            }
//            stb.append("     AND T2.SCHREGNO = '" + student._schregno + "' ");
//            return stb.toString();
//        }
//
//        private static String sqlJituJisuSql(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
//            final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
//            final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T2.SCHREGNO, ");
//            if (!isGroup) {
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("     T1.CLASSCD, ");
//                    stb.append("     T1.SCHOOL_KIND, ");
//                    stb.append("     T1.CURRICULUM_CD, ");
//                }
//                stb.append("     T1.SUBCLASSCD, ");
//            } else {
//                stb.append("     T1.SPECIAL_GROUP_CD, ");
//            }
//            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
//            stb.append("        AS ABSENCE_HIGH, ");
//            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
//            stb.append("        AS GET_ABSENCE_HIGH ");
//            stb.append(" FROM ");
//            stb.append("     " + tableName + " T1 ");
//            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
//            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("       AND T2.YEAR = T1.YEAR ");
//            stb.append("       AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
//            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
//            if (!isGroup) {
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("       T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD = T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
//                } else {
//                    stb.append("       T3.SUBCLASSCD = T1.SUBCLASSCD ");
//                }
//            } else {
//                stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
//            }
//            stb.append("       AND T3.COURSECD = T2.COURSECD ");
//            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
//            stb.append("       AND T3.GRADE = T2.GRADE ");
//            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
//            stb.append("       AND T3.YEAR = T1.YEAR ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year + "' ");
//            stb.append("     AND T1.DIV = '2' ");
//            if (!isGroup) {
//                if (null != subclassCd) {
//                    if ("1".equals(param._useCurriculumcd)) {
//                        stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '" + subclassCd + "' ");
//                    } else {
//                        stb.append("     AND T1.SUBCLASSCD = '" + subclassCd + "' ");
//                    }
//                }
//            }
//            stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
//            return stb.toString();
//        }
//
//        public String toString() {
//            return " 履修上限値" + _compAbsenceHigh + " , 修得上限値" + _getAbsenceHigh;
//        }
//    }

    private static class Score {

        final String _tableDiv;
        final String _testKindcd;
        final String _score;
        final String _courseScore;
        final String _hrScore;
        final String _gradeScore;
        final String _wkClassType;

        Score(final String tableDiv,
                final String testKindCd,
                final String score,
                final String courseScore,
                final String hrScore,
                final String gradeScore,
                final String wkClassType) {
            _tableDiv = tableDiv;
            _testKindcd = testKindCd;
            _score = score;
            _courseScore = courseScore;
            _hrScore = hrScore;
            _gradeScore = gradeScore;
            _wkClassType = wkClassType;
        }

        public String getAvgScore(final Param param) {
            String avgScore = null;
            if ("1".equals(param._avgDiv)) {
                avgScore = _gradeScore;
            } else if ("2".equals(param._avgDiv)) {
                avgScore = _hrScore;
            } else if ("3".equals(param._avgDiv)) {
                avgScore = _courseScore;
            }
            return avgScore;
        }

        /**
         * 素点、平均をセットする。
         * @param db2
         * @param student
         * @param param
         */
        private static void setScore(final DB2UDB db2, final Student student, final Param param) {

            final String prestatementRecordScore = sqlRecordScore(param, student._schregno);
            log.debug("setScoreValue sql = " + prestatementRecordScore);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(prestatementRecordScore);

                rs = ps.executeQuery();
                while (rs.next()) {
                    String subclassCd = rs.getString("SUBCLASSCD");
                    if ("1".equals(param._useCurriculumcd)) {
                        subclassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");
                    }
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String tableDiv = rs.getString("TABLE_DIV");

                    final String score = rs.getString("SCORE");
                    final String courseScore = rs.getString("COURSE_SCORE") == null ? null : rs.getBigDecimal("COURSE_SCORE").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String hrScore = rs.getString("HR_SCORE") == null ? null : rs.getBigDecimal("HR_SCORE").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String gradeScore = rs.getString("GRADE_SCORE") == null ? null : rs.getBigDecimal("GRADE_SCORE").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String wkClassType;
                    if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                        wkClassType = rs.getString("WKCLASSTYPE");
                    } else {
                        wkClassType = "";
                    }
                    final Score s = new Score(tableDiv, testKindCd, score, courseScore, hrScore, gradeScore, wkClassType);

                    student.putScore(subclassCd, testKindCd, tableDiv, s);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlRecordScore(final Param param, final String schregno) {

            final String[] targets = param.getTargetTestKindCds();
            int rank = 0;

            for (int i = 0; i < targets.length; i++) {
                if (param.isScoreFromRecordRankVDat(targets[i])) {
                } else {
                    rank += 1;
                }
            }
            final String[] fromRank = new String[rank];

            if (rank != 0) {
                for (int c = 0, i = 0; i < targets.length; i++) {
                    if (!param.isScoreFromRecordRankVDat(targets[i])) {
                        fromRank[c] = targets[i];
                        c += 1;
                    }
                }
            }

            final StringBuffer stb = new StringBuffer();
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                //RECORD_SCORE_DATをベースにして基礎情報を作成(VALUE_DI='*' or RECORD_RANK_DAT.SCORE != null)
                stb.append(" WITH BASEINFO AS ( ");
                stb.append("  SELECT ");
                stb.append("    T1.YEAR, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.TESTKINDCD, ");
                stb.append("    T1.TESTITEMCD, ");
                stb.append("    T1.SCORE_DIV, ");
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.SCHOOL_KIND, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SUBCLASSCD, ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T4.SCORE, ");
                stb.append("    T4.AVG ");
                stb.append("  FROM ");
                stb.append("    RECORD_SCORE_DAT T1 ");
                stb.append("    LEFT JOIN RECORD_RANK_DAT T4 ");
                stb.append("       ON T4.YEAR = T1.YEAR ");
                stb.append("      AND T4.SEMESTER = T1.SEMESTER ");
                stb.append("      AND T4.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("      AND T4.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("      AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("      AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("      AND T4.SCHREGNO = T1.SCHREGNO ");
                stb.append("  WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
//                stb.append("    AND T1.SCORE_DIV = '00' ");
                stb.append("    AND (T1.VALUE_DI = '*' OR T4.SCORE IS NOT NULL) ");
                stb.append(" ), RRDINFO AS ( ");
                //上記データにSCHREG_REGD_DATから必要な情報を追加
                stb.append("  SELECT ");
                stb.append("    SRD1.SCHREGNO, ");
                stb.append("    SRD1.YEAR, ");
                stb.append("    SRD1.SEMESTER, ");
                stb.append("    T1.TESTKINDCD, ");
                stb.append("    T1.TESTITEMCD, ");
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.SCHOOL_KIND, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SUBCLASSCD, ");
                stb.append("    SRD1.GRADE, ");
                stb.append("    SRD1.HR_CLASS, ");
                stb.append("    SRD1.COURSECD, ");
                stb.append("    SRD1.MAJORCD, ");
                stb.append("    SRD1.COURSECODE, ");
                stb.append("    T1.SCORE, ");
                stb.append("    T1.AVG ");
                stb.append("  FROM  ");
                stb.append("    BASEINFO T1 ");
                stb.append("    LEFT JOIN SCHREG_REGD_DAT SRD1 ");
                stb.append("       ON SRD1.YEAR = T1.YEAR ");
                stb.append("      AND SRD1.SEMESTER = T1.SEMESTER ");
                stb.append("      AND SRD1.SCHREGNO = T1.SCHREGNO ");
                stb.append("  WHERE ");
                stb.append("    SRD1.YEAR = '" + param._year + "' ");
                stb.append("), WK_RECORD_RANK_BASE AS (");
                //名称M'D005'、'D006'に紐づくデータのみ利用する(ここで5/7教科を区別する)。
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   CASE WHEN N005.YEAR IS NOT NULL THEN '5' WHEN N006.YEAR IS NOT NULL THEN '7' ELSE '' END AS WKCLASSTYPE ");
                stb.append(" FROM ");
                stb.append("   RRDINFO T1 ");
                stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT N005 ");
                stb.append("    ON N005.YEAR = '" + param._year + "' ");
                stb.append("   AND N005.GROUP_DIV = '3' ");
                stb.append("   AND N005.GRADE = T1.GRADE ");
                stb.append("   AND N005.COURSECD = T1.COURSECD ");
                stb.append("   AND N005.MAJORCD = T1.MAJORCD ");
                stb.append("   AND N005.COURSECODE = T1.COURSECODE ");
                stb.append("   AND N005.CLASSCD = T1.CLASSCD ");
                stb.append("   AND N005.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND N005.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND N005.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT N006 ");
                stb.append("    ON N006.YEAR = '" + param._year + "' ");
                stb.append("   AND N006.GROUP_DIV = '5' ");
                stb.append("   AND N006.GRADE = T1.GRADE ");
                stb.append("   AND N006.COURSECD = T1.COURSECD ");
                stb.append("   AND N006.MAJORCD = T1.MAJORCD ");
                stb.append("   AND N006.COURSECODE = T1.COURSECODE ");
                stb.append("   AND N006.CLASSCD = T1.CLASSCD ");
                stb.append("   AND N006.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND N006.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND N006.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("   (N005.YEAR IS NOT NULL OR N006.YEAR IS NOT NULL) ");
            } else {
                //既存処理としてRECORD_RANK_DATを用意
                stb.append(" WITH WK_RECORD_RANK_BASE AS (");
                stb.append(" SELECT ");
                stb.append("  * ");
                stb.append(" FROM ");
                stb.append("  RECORD_RANK_DAT ");
            }
            stb.append("), RECORD_VALUE AS ( ");
            stb.append("   SELECT  '" + Param.REC + "' AS TABLE_DIV, T1.YEAR, T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           T1.CLASSCD, ");
                stb.append("           T1.SCHOOL_KIND, ");
                stb.append("           T1.CURRICULUM_CD, ");
            }
            stb.append("           T1.SUBCLASSCD, ");
            stb.append("           T1.SEMESTER, ");
            stb.append("           T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
            stb.append("           T1.SCORE, T1.AVG, ");
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                stb.append("           T1.WKCLASSTYPE, ");
            }
            stb.append("           T3.AVG AS COURSE_SCORE, ");
            stb.append("           T4.AVG AS HR_SCORE, ");
            stb.append("           T5.AVG AS GRADE_SCORE ");
            stb.append("   FROM    WK_RECORD_RANK_BASE T1");
            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            // コース平均
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
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
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("        AND T4.SEMESTER || T4.TESTKINDCD || T4.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
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
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("        AND T5.SEMESTER || T5.TESTKINDCD || T5.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
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
            stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
            stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, fromRank));

            stb.append(" ) ");
            stb.append("   SELECT  T1.TABLE_DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           T1.CLASSCD, ");
                stb.append("           T1.SCHOOL_KIND, ");
                stb.append("           T1.CURRICULUM_CD, ");
            }
            stb.append("            T1.SUBCLASSCD, T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS TESTKINDCD ");
            stb.append("           ,T1.SCORE, T1.AVG, T1.COURSE_SCORE, T1.HR_SCORE, T1.GRADE_SCORE ");
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                stb.append("           ,T1.WKCLASSTYPE ");
            }
            stb.append("   FROM    RECORD_VALUE T1");
            stb.append("   WHERE T1.YEAR = '" + param._year + "' ");
            stb.append("       AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            if (USEKNJD154J2CD.equals(param._useFormNameD154J)) {
                stb.append("  ORDER BY T1.TABLE_DIV, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("           T1.CLASSCD, ");
                    stb.append("           T1.SCHOOL_KIND, ");
                    stb.append("           T1.CURRICULUM_CD, ");
                }
                stb.append("            T1.SUBCLASSCD, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD ");
                stb.append("        ,T1.WKCLASSTYPE ");
            }

            return stb.toString();
        }

        public String toString() {
            return " Score " + _testKindcd + " (" + _score + " , " + _courseScore + " , " + _hrScore + " , " + _gradeScore + ") " + _tableDiv + ")";
        }
    }


    private static class Semester {
        public final String _cd;
        public final String _name;
        public final String _sdate;
        public final String _edate;
        Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }

        public String toString() {
            return "(" + _name + " [" + _sdate + "," + _edate + "])";
        }
    }

//    private static class RecordTotalStudyTimeDat {
//        public final String _totalStudyTime;
//        public final String _totalStudyAct;
//
//        public RecordTotalStudyTimeDat(final String totalStudyTime, final String totalStudyAct) {
//            _totalStudyTime = totalStudyTime;
//            _totalStudyAct = totalStudyAct;
//        }
//
//
//        private static void setTotalStudy(final DB2UDB db2, final List students, final Param param) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     SEMESTER ");
//            stb.append("     ,TOTALSTUDYTIME ");
//            stb.append("     ,TOTALSTUDYACT");
//            stb.append(" FROM ");
//            stb.append("     RECORD_TOTALSTUDYTIME_DAT ");
//            stb.append(" WHERE ");
//            stb.append("     YEAR = '" + param._year + "' ");
//            stb.append("     AND SCHREGNO = ? ");
//
//            final String sql = stb.toString();
//            try {
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = students.iterator(); it.hasNext();) {
//                    final Student student= (Student) it.next();
//                    ps.setString(1, student._schregno);
//                    rs = ps.executeQuery();
//                    final Map recordTotalStudys = new HashMap();
//
//                    student._recordTotalStudieTimeDat.clear();
//
//                    while (rs.next()) {
//                        final String semester = rs.getString("SEMESTER");
//                        final String totalStudyTime = rs.getString("TOTALSTUDYTIME");
//                        final String totalStudyAct = rs.getString("TOTALSTUDYACT");
//
//                        final RecordTotalStudyTimeDat totalStudy = new RecordTotalStudyTimeDat(totalStudyTime, totalStudyAct);
//                        recordTotalStudys.put(semester, totalStudy);
//                    }
//                    student._recordTotalStudieTimeDat.putAll(recordTotalStudys);
//                }
//
//            } catch (SQLException e) {
//                log.error("sql exception! :" + sql, e);
//            } catch (Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }
//    }

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

        final String _num90;
        final String _num90Other;
        final String _staffname;
        final String _wksubclasstype;

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
                final String num90,
                final String num90Other,
                final String staffname,
                final String wksubclasstype) {
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
            _num90 = num90;
            _num90Other = num90Other;
            _staffname = staffname;
            _wksubclasstype = wksubclasstype;
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
    }

    private static abstract class KNJD154JBase {

        private static Log log = LogFactory.getLog(KNJD154JBase.class);

        protected DB2UDB _db2;
        protected Param _param;
        protected Vrw32alpWrap _svf;

//        protected Map _amikakeMap;
//
//        protected String _fieldKamokuSick;
//        protected String _fieldKamokuLateEarly;
//        protected String _fieldKamokuKouketsu;
//        protected String _fieldKamokuMourning;
//        protected String _fieldKamokuSuspend;

        protected int _classFieldKeta;
        protected int _class2FieldKeta;

        public KNJD154JBase(DB2UDB db2, Vrw32alpWrap svf, Param param) {
            _db2 = db2;
            _svf = svf;
            _param = param;
        }

        public abstract void print(final Student student);

//        protected void setAmikakeBase(Vrw32alpWrap svf) {
//            if (_amikakeMap == null) {
//                return;
//            }
//            for (Iterator it = _amikakeMap.keySet().iterator(); it.hasNext();) {
//                String field = (String) it.next();
//                String attribute = (String) _amikakeMap.get(field);
//
//                final int commaIndex = field.indexOf(",");
//                if (commaIndex == -1) {
//                    svf.VrAttribute(field, attribute);
//                } else {
//                    String field1 = field.substring(0, commaIndex);
//                    int idx = Integer.parseInt(field.substring(commaIndex + 1));
//                    svf.VrAttributen(field1, idx, attribute);
//                }
//            }
//        }

//        protected void setAbsenceFieldAttributeBase(
//                final Vrw32alpWrap svf,
//                final AbsenceHigh absenceHigh,
//                final String absent1,
//                final String fieldKekka,
//                final String fieldLateEarly
//        ) {
//            if (absenceHigh == null) {
//                if (absent1 != null && NumberUtils.isNumber(absent1) && Double.parseDouble(absent1) != 0.0) {
//                    svf.VrAttribute(fieldKekka, "Paint=(1,40,1),Bold=1");
//                    svf.VrAttribute(fieldLateEarly, "Paint=(1,40,1),Bold=1");
//                    if (_amikakeMap != null) {
//                        _amikakeMap.put(fieldKekka, "Paint=(1,40,1),Bold=1");
//                        _amikakeMap.put(fieldLateEarly, "Paint=(1,40,1),Bold=1");
//                    }
//                }
//            } else if (absenceHigh.isRishuOver(absent1)) {
//                svf.VrAttribute(fieldKekka, "Paint=(1,40,1),Bold=1");
//                svf.VrAttribute(fieldLateEarly, "Paint=(1,40,1),Bold=1");
//                if (_amikakeMap != null) {
//                    _amikakeMap.put(fieldKekka, "Paint=(1,40,1),Bold=1");
//                    _amikakeMap.put(fieldLateEarly, "Paint=(1,40,1),Bold=1");
//                }
//            } else if (absenceHigh.isShutokuOver(absent1)) {
//                svf.VrAttribute(fieldKekka, "Paint=(1,70,1),Bold=1");
//                svf.VrAttribute(fieldLateEarly, "Paint=(1,70,1),Bold=1");
//                if (_amikakeMap != null) {
//                    _amikakeMap.put(fieldKekka, "Paint=(1,70,1),Bold=1");
//                    _amikakeMap.put(fieldLateEarly, "Paint=(1,70,1),Bold=1");
//                }
//            }
//        }

//        /**
//         * @param svf
//         * @param si
//         * @param student
//         * @param replaceflg
//         * @param subclass
//         * @param sa
//         */
//        protected void printSubclassAttendanceBase(final Vrw32alpWrap svf,
//                final SubclassInfo si,
//                final Student student,
//                final int replaceflg,
//                Subclass subclass,
//                SubclassAttendance sa) {
//
//            if (sa == null) {
//                return;
//            }
//            String sick = (9 == replaceflg) ? sa.getReplacedSick() : sa.getSick();
//
//            setAbsenceFieldAttributeBase(svf, student.getAbsenceHigh(subclass._subclassCd), sick, _fieldKamokuSick, _fieldKamokuLateEarly);
//
//            String rawSick = (9 == replaceflg) ? sa.getRawReplacedSick() : sa.getRawSick();
//
//            if (rawSick == null) {
//                rawSick = "";
//            } else if (NumberUtils.isNumber(rawSick)) {
//                rawSick = String.valueOf(new BigDecimal(rawSick).intValue());
//            }
//            svf.VrsOut(_fieldKamokuSick, rawSick == null ? "" : String.valueOf(rawSick));
//            svf.VrsOut(_fieldKamokuLateEarly, sa.getLateEarly());
//            svf.VrsOut(_fieldKamokuKouketsu, sa.getKoketsu());
//            svf.VrsOut(_fieldKamokuMourning, sa.getMourning());
//            svf.VrsOut(_fieldKamokuSuspend, sa.getSuspend());
//        }

        protected int getFieldColumnSemester(String semester) {
            final String[] semesters = _param._dispSemester;
            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equals(semester)) {
                    return (i + 1);
                }
            }
            return 0;
        }

        protected int getFieldColumnTestKind(String semTestKindCd) {
            final String[] testKindCds = _param._semTestKindCds;
            for (int i = 0; i < testKindCds.length; i++) {
                if (testKindCds[i].equals(semTestKindCd)) {
                    return (i + 1);
                }
            }
            return 0;
        }

        protected void printHeaderBase(final Vrw32alpWrap svf, final Student student) {
            svf.VrsOut("NENDO", _param.getNendo());
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);

            svf.VrsOut("PRESIDENT", _param._principalName);

            svf.VrsOut("JOB_PRI", _param._jobName);
            svf.VrsOut("PRI_NAME", _param._principalName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0) + "　印" );
            }

            svf.VrsOut("COURSE",   student._courseCodeName);
            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("HR_NAME",  student._hrName + " " + student._attendNo);
            svf.VrsOut("NAME",     "氏名　" + student._name);

            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }
        }

        protected static String[] get_token(final String s, final int keta, final int gyo) {
            String[] rtn = KNJ_EditEdit.get_token(s, keta, gyo);
            if (null == rtn) {
                return new String[] {};
            }
            return rtn;
        }

        protected int getFieldKeta(final Vrw32alpWrap svf, String fieldname) {
            try {
                Map fieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(svf);
                SvfField svfField = (SvfField) fieldMap.get(fieldname);
                if (null != svfField) {
                    return svfField._fieldLength;
                }
            } catch (Throwable t) {
                log.error("exceptioN!", t);
            }
            return -1;
        }

        protected void printClassname(final String data) {
            if (_class2FieldKeta < 0) {
                // CLASS2がない
                _svf.VrsOut("CLASS", data);
            } else {
                final int keta = getMS932ByteLength(data);
                _svf.VrsOut("CLASS2", data);
                _svf.VrsOut("CLASS", data);
                if (_classFieldKeta < keta) {
                    _svf.VrAttribute("CLASS", "X=10000");
                } else {
                    _svf.VrAttribute("CLASS2", "X=10000");
                }
            }
        }
    }

    private static class KNJD154JA extends KNJD154JBase {

        final int MAX_RECORD = 17;

        public KNJD154JA(DB2UDB db2, Vrw32alpWrap svf, KNJD154J.Param param) {
            super(db2, svf, param);

//            _fieldKamokuSick = "ABSENCE";
//            _fieldKamokuLateEarly = "EARLY1";
//            _fieldKamokuKouketsu = "PUB_KEKKA1";
//            _fieldKamokuMourning = "MOURNING1";
//            _fieldKamokuSuspend = "SUSPEND1";
        }

        public void print(final Student student) {

            if (USEKNJD154J2CD.equals(_param._useFormNameD154J)) {
                _svf.VrSetForm("KNJD154J_2.frm", 4);
            } else {
                _svf.VrSetForm("KNJD154J.frm", 4);
            }
            _classFieldKeta = getFieldKeta(_svf, "CLASS");
            _class2FieldKeta = getFieldKeta(_svf, "CLASS2");

            printHeaderBase(_svf, student);

//            printSogotekinaGakushunoJikan(_svf, student);
//
//            printSogotekinaGakushunoJikanAttendance(_svf, student);
//
//            printTushinran(_svf, student);
//
//            printBiko(_svf, student);

            printTitle(_svf);

            int line = printRecDetail(_svf, student);

            if (line == 0) {
                _svf.VrsOut("CLASS", "\n");
            }
            _svf.VrEndRecord();
            line ++;
        }

//        private void printSogotekinaGakushunoJikan(final Vrw32alpWrap svf, final Student student) {
//            RecordTotalStudyTimeDat recordTotalStudy = (RecordTotalStudyTimeDat) student._recordTotalStudieTimeDat.get(Param.SEMEALL);
//            if (null != recordTotalStudy) {
//                String[] spContent = get_token(recordTotalStudy._totalStudyAct, 50, 2);
//                for (int i = 0; i < spContent.length; i++) {
//                    svf.VrsOut("SP_CONENT" + (i + 1), spContent[i]);
//                }
//                if (_param.isPrintSogotekinaGakushunoJikanHyoka()) {
//                    String[] spEva = get_token(recordTotalStudy._totalStudyTime, 50, 3);
//                    for (int i = 0; i < spEva.length; i++) {
//                        svf.VrsOut("SP_EVA" + (i + 1), spEva[i]);
//                    }
//                }
//            }
//        }
//
//        private void printTushinran(final Vrw32alpWrap svf, final Student student) {
//            String hrecordRemarkDatRemark1  = student._hexamRecordRemarkRemark1;
//            if (null != hrecordRemarkDatRemark1) {
//                String[] corre = get_token(hrecordRemarkDatRemark1, 60, 5);
//                for (int i = 0; i < corre.length; i++) {
//                    svf.VrsOut("CORRE" + (i + 1), corre[i]);
//                }
//            }
//        }
//
//        private void printBiko(final Vrw32alpWrap svf, final Student student) {
//            String[] communication = get_token(student._recordDocumentKindDatFootnote, 100, 5);
//            for (int i = 0; i < communication.length; i++) {
//                svf.VrsOut("COMMUNICATION" + (i + 1), communication[i]);
//            }
//        }

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

//        private void printSogotekinaGakushunoJikanAttendance(final Vrw32alpWrap svf, final Student student) {
//            // 欠時数、欠課数： 科目コード頭2桁が90、合併先科目 + 合併設定なしの科目
//            // 欠課数上限値：科目コード頭2桁が90、合併科目があるなら先科目の科目コードのMIN、ないなら合併設定なしの科目の科目コードのMIN)
//            final Map subclassInfoMap = getSubclassInfoMap(student._subclassInfos);
//            String minSubclassCd = null;
//            int sick = 0;
//            int rawSick = 0;
//            int lateEarly = 0;
//            int absent = 0;
//            int mourning = 0;
//            int suspend = 0;
//            int koudome = 0;
//            int virus = 0;
//            boolean hasGappeiSubclasscd = false;
//            for (final Iterator it = student._subclassScoreMap.keySet().iterator(); it.hasNext();) {
//                final String subclassCd = (String) it.next();
//                if (null == subclassCd || !subclassCd.substring(0, 2).equals("90")) {
//                    continue;
//                }
//
//                final SubclassInfo si = (SubclassInfo) subclassInfoMap.get(subclassCd);
//                if (null != si) {
//                    if (si._replaceflg == 9 && (!hasGappeiSubclasscd || null == minSubclassCd || si._subclassCd.compareTo(minSubclassCd) < 0)) {
//                        minSubclassCd = si._subclassCd;
//                        // log.debug(" 合併先 minSubclassCd = " + minSubclassCd);
//                        hasGappeiSubclasscd = true;
//                    }
//                }
//                if (!hasGappeiSubclasscd && (null == minSubclassCd || subclassCd.compareTo(minSubclassCd) < 0)) {
//                    // log.debug(" 合併設定無し minSubclassCd = " + minSubclassCd);
//                    minSubclassCd = subclassCd;
//                }
//                final SubclassAttendance sa = student.getSubclassAttendance(subclassCd, _param._semester);
//                if (null == sa) {
//                    continue;
//                }
//                if (null == si || si._replaceflg == 0) { // 合併設定無し
//                    sick      += null == sa._sick ? 0 : sa._sick.intValue();
//                    rawSick   += null == sa._rawSick ? 0 : sa._rawSick.intValue();
//                    lateEarly += sa._lateearly;
//                    absent    += sa._absent;
//                    mourning  += sa._mourning;
//                    suspend   += sa._suspend;
//                    koudome   += sa._koudome;
//                    virus     += sa._virus;
//                } else if (9 == si._replaceflg) { // 合併先科目
//                    sick      += null == sa._replacedSick ? 0 : sa._replacedSick.intValue();
//                    rawSick   += null == sa._rawReplacedSick ? 0 : sa._rawReplacedSick.intValue();
//                    lateEarly += sa._lateearly;
//                    absent    += sa._absent;
//                    mourning  += sa._mourning;
//                    suspend   += sa._suspend;
//                    koudome   += sa._koudome;
//                    virus     += sa._virus;
//                } else if (1 == si._replaceflg) { // 合併元科目
//                }
//            }
//            setAbsenceFieldAttributeBase(svf, student.getAbsenceHigh(minSubclassCd), String.valueOf(sick), "SP_ABSENCE", "SP_EARLY");
//
//            svf.VrsOut("SP_ABSENCE", String.valueOf(rawSick));
//            svf.VrsOut("SP_EARLY", String.valueOf(lateEarly));
//            svf.VrsOut("SP_PUB_ABSENCE", String.valueOf(absent));
//            svf.VrsOut("SP_MOURNING", String.valueOf(mourning));
//            svf.VrsOut("SP_SUSPEND", String.valueOf(suspend + koudome + virus));
//        }

        private void printTitle(final Vrw32alpWrap svf) {
            for (int i = 0; i < _param._dispSemester.length; i++) {
                final String semes = _param._dispSemester[i];
                final Semester semester = (Semester) _param._semesterMap.get(semes);
                if (semester != null) {
                    svf.VrsOut("SEMESTER" + getFieldColumnSemester(semes), semester._name);
                }
            }
            svf.VrsOut("SEMESTER3", "課題テスト");
            for (int i = 0; i < _param._semTestKindCds.length; i++) {
                final String semTestKindCd =_param._semTestKindCds[i];
                final String testItemName = (String) _param._testItemNames.get(semTestKindCd);
                if (testItemName != null) {
                    svf.VrsOut("TESTITEM" + getFieldColumnTestKind(semTestKindCd), testItemName);
                }
            }
        }

        private int printRecDetail (
                final Vrw32alpWrap svf,
                final Student student
        ) {
            boolean hasData = false;
            int i = 0;
            String beforeWkSubclsType = "";
            SubclassInfo info = null;

//            printAttendance(svf, student);
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
//                printAttendance(svf, student);

                info = (SubclassInfo) it.next();

                if (_param.isD046ContainSubclasscd(info._subclassCd)) {
                    continue;
                }

                if (hasData) {
                    svf.VrEndRecord();
                    i++;
                    if (USEKNJD154J2CD.equals(_param._useFormNameD154J)) {
                        if (!"".equals(beforeWkSubclsType) && !beforeWkSubclsType.equals(info._wksubclasstype)) {
                            //5/7教科個別合計出力
                            printTotalSubClassInfo(svf, student, beforeWkSubclsType, (i + 1));
                            svf.VrEndRecord();
                            i++;
                            //余白出力
                            svf.VrsOut("BLANK1", "yohaku");
                            svf.VrEndRecord();
                            i++;
                        }

                    }
                }

                hasData= printSubclassInfo(svf, info, student, (i + 1));
                beforeWkSubclsType = info._wksubclasstype;
            }
            if (USEKNJD154J2CD.equals(_param._useFormNameD154J)) {
                //※最後のレコード出力処理が改行対応していないため、追加。
                svf.VrEndRecord();
                i++;
                //何か出力しているなら、合計を出力。
                if (!"".equals(beforeWkSubclsType) && hasData) {
                    //5/7教科個別合計出力
                    printTotalSubClassInfo(svf, student, info._wksubclasstype, (i + 1));
                    svf.VrEndRecord();
                    i++;
                }
                //余白出力
                svf.VrsOut("BLANK2", "yohaku1");
                svf.VrEndRecord();
                i++;
                //順位/人数集計表
                svf.VrsOut("RANK_TITLE", "yohaku2");  //表の列タイトル行
                svf.VrEndRecord();
                i++;
                printTotalRankCountInfo(svf, student, "5");
                svf.VrEndRecord();
                i++;
                printTotalRankCountInfo(svf, student, "7");
                svf.VrEndRecord();
                i++;
            }
            return i;
        }

        private void printTotalRankCountInfo(
                final Vrw32alpWrap svf,
                final Student student,
                final String wkSubclsType
                ) {
            Map aryclstypetbl = new HashMap();
            aryclstypetbl.put("5", "333333");  //5教科の合計データで設定されている科目コード
            aryclstypetbl.put("7", "555555");  //7教科の合計データで設定されている科目コード

            String totalclsSubTitle = "";
            if ("5".equals(wkSubclsType)) {
                totalclsSubTitle = "5教科";
                svf.VrsOut("RANK_NAME1", totalclsSubTitle);
            } else {
                totalclsSubTitle = "7教科";
                svf.VrsOut("RANK_NAME2", totalclsSubTitle);
            }
            String getclstypekey = (String)aryclstypetbl.get(wkSubclsType);
            for (final Iterator it = student._scoreTotalInfo.keySet().iterator(); it.hasNext();) {
                final String seme_testkindcd = (String) it.next();
                TotalScoreInfo ttlscorewk = student.getScoreTotalInfo(seme_testkindcd);
                if (ttlscorewk._subclasscd.equals(getclstypekey)) {
//                    final String[] semTestKindCds = _param.getTargetTestKindCds();
                    final String pos = String.valueOf(getFieldColumnTestKind(StringUtils.substring(seme_testkindcd, 1)));
                    if (!"0".equals(pos)) {
                        if ("5".equals(wkSubclsType)) {
                            int subpos = (Integer.parseInt(pos) % 2) == 0 ? 2 : 1;
                            int mainpos = ((Integer.parseInt(pos) + 1)/ 2);
                            if (ttlscorewk.getRankScore(_param) != null) {
                                svf.VrsOut("RANK" + mainpos + "_" + subpos, ttlscorewk.getRankScore(_param));
                            }
                            if (ttlscorewk.getCount(_param) != null) {
                                svf.VrsOut("NUM" + mainpos + "_" + subpos, ttlscorewk.getCount(_param));
                            }
                        } else {
                            if ("02".equals(ttlscorewk._testkindcd) && ttlscorewk.getRankScore(_param) != null) {
                                svf.VrsOut("RANK" + (3 + Integer.parseInt(ttlscorewk._semester)), ttlscorewk.getRankScore(_param));
                            }
                            if ("02".equals(ttlscorewk._testkindcd) && ttlscorewk.getCount(_param) != null) {
                                svf.VrsOut("NUM" + (3 + Integer.parseInt(ttlscorewk._semester)), ttlscorewk.getCount(_param));
                            }
                        }
                    }
                }
            }
        }

        private void printTotalSubClassInfo(
                final Vrw32alpWrap svf,
                final Student student,
                final String beforeWkSubclsType,
                int line
                ) {
            Map aryclstypetbl = new HashMap();
            aryclstypetbl.put("5", "333333");  //5教科の合計データで設定されている科目コード
            aryclstypetbl.put("7", "555555");  //7教科の合計データで設定されている科目コード

            String totalclsTitle = "";
            String totalclsSubTitle = "";
            if ("5".equals(beforeWkSubclsType)) {
                totalclsTitle = "5教科";
                totalclsSubTitle = "5教科 合計";
            } else {
                totalclsTitle = "7教科";
                totalclsSubTitle = "7教科 合計";
            }
            svf.VrsOut("CLASS", totalclsTitle);
            svf.VrsOut("SUBCLASS" + (totalclsSubTitle.length() > 13 ? "2" : "1"), totalclsSubTitle);
            String getclstypekey = (String)aryclstypetbl.get(beforeWkSubclsType);
            for (final Iterator it = student._scoreTotalInfo.keySet().iterator(); it.hasNext();) {
                final String seme_testkindcd = (String) it.next();
                TotalScoreInfo ttlscorewk = student.getScoreTotalInfo(seme_testkindcd);
                if (ttlscorewk._subclasscd.equals(getclstypekey)) {
//                    final String[] semTestKindCds = _param.getTargetTestKindCds();
                    final String pos = String.valueOf(getFieldColumnTestKind(StringUtils.substring(seme_testkindcd, 1)));
                    if (!"0".equals(pos)) {
                        if (ttlscorewk._score != null) {
                            svf.VrsOut("POINT" + pos, ttlscorewk._score);
                        }
                        if (!_param._isNotPrintAvg && ttlscorewk.getAvgScore(_param) != null) {
                            svf.VrsOut("AVE_POINT" + pos, ttlscorewk.getAvgScore(_param));
                        }
                    }
                }
            }
        }

//        private void printAttendance(final Vrw32alpWrap svf, final Student student) {
//            final Attendance sum = student.getAttendance(_param._semester);
//
//            svf.VrsOut("REC_LESSON" , String.valueOf(sum._lesson));
//            svf.VrsOut("REC_MOURNING" , String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
//            svf.VrsOut("REC_PRESENT", String.valueOf(sum._mlesson));
//            svf.VrsOut("REC_ABSENCE", String.valueOf(sum._absence));
//            svf.VrsOut("REC_ATTEND" , String.valueOf(sum._attend));
//
//            final String[] spGroupCd = new String[]{Attendance.GROUP_LHR, Attendance.GROUP_ASS};
//            final String[] field = new String[]{"REC_LHR_ABSENCE", "REC_ASSEMBLY_ABSENCE"};
//            for (int i = 0; i < spGroupCd.length; i++) {
//
//                final int spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
//                svf.VrsOut(field[i], String.valueOf(spGroupKekka));
//                final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd[i]);
//
//                if (ah == null) {
//                    if (spGroupKekka != 0) {
//                        svf.VrAttribute(field[i], "Paint=(1,40,1),Bold=1");
//                    }
//                } else if (ah.isRishuOver(String.valueOf(spGroupKekka))) {
//                    svf.VrAttribute(field[i], "Paint=(1,40,1),Bold=1");
//                }
//
//            }
//        }


        private boolean printSubclassInfo (
                final Vrw32alpWrap svf,
                final SubclassInfo si,
                final Student student,
                int line
        ) {
            if (MAX_RECORD < line) {
                line = (line % MAX_RECORD == 0)? MAX_RECORD: line % MAX_RECORD;
            }

            printClassname(si._classabbv);
            svf.VrsOut("SUBCLASS" + (si._subclassname != null && si._subclassname.length() > 13 ? "2" : "1"),  si._subclassname);

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOut("CREDIT", si._credits);
            }

            Subclass subclass = student.getSubclass(si._subclassCd);
//            log.debug(" 科目= " + si._subclassname + " ( " + si._subclassCd + " )");
            if (subclass == null) {
                return true;
            }

            if (0 == si._replaceflg  || 1 == si._replaceflg  || 9 == si._replaceflg) {

                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String pos = String.valueOf(getFieldColumnTestKind(semTestKindCd));
                    if (student.isSlump(subclass._subclassCd, semTestKindCd)) {
                        svf.VrAttribute("POINT" + pos,  "Paint=(1,50,1),Bold=1");
                    }
                    Score s = subclass.getScore(semTestKindCd);
                    if (s != null) {
                        if (s._score != null) {
                            svf.VrsOut("POINT" + pos, s._score);
                        }
                        if (!_param._isNotPrintAvg && s.getAvgScore(_param) != null) {
                            svf.VrsOut("AVE_POINT" + pos, s.getAvgScore(_param));
                        }
                    }
                }
//                printSubclassAttendanceBase(svf, si, student, si._replaceflg, subclass, subclass.getAttendance(_param._semester));
            }
            return true;
        }

    }

    private static class Param {

        static final String SEMEALL = "9";

        static final String REC = "REC  ";

        // 科目コード:総合的な学習の時間

        final String _year;
        final String _semester;
        final String _semeFlg;
        final String _ctrlSemester;
        final String _grade_hr_class;

        /** パターンＡ，Ｂ，Ｃ，Ｄ */
        private final String _testKindCd;

        private String _sdate;
        private final String _edate;
        private final String _ctrlDate;

        private final boolean _isRuikei;

        final String[] _dispSemester;

        // 素点等を表示する成績のテスト種別コード
        final String[] _semTestKindCds;

        private final String[] _selectSchregno;

        /** 平均値区分 1:学年、2:ホームルームクラス、3:コース */
        final String _avgDiv;

        private String _schoolName;

        private String _z010name1;

        private String _principalName;

        private String _jobName;

        private String _hrJobName;

        private String _schoolAddress;

        private String _schoolTelNo;

        private final List _staffNames;

        private KNJSchoolMst _knjSchoolMst;

        private boolean _isSeireki;

        /** 学期・テスト種別と考査名称のマップ */
        final Map _testItemNames = new HashMap();

        final TreeMap _semesterMap = new TreeMap();

        /** C005 */
        private final Map _subClassC005 = new HashMap();

        final String _documentRoot;

        /** 欠課換算前の遅刻・早退を表示する */
//        final String _chikokuHyoujiFlg;

        /** 平均値を表示しない */
        final boolean _isNotPrintAvg;

        private String _imagePath;

        private String _extension;

        private final File _logoFile;

        /** 名称マスタ「D046」 登録された学期に表示しない科目のリスト */
        private List _d046List;

        final String _schoolKind;

        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;
        private final String _useFormNameD154J;

        final String _d008Namecd1;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            String ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlDate = ctrlDate.substring(0,4) + "-" + ctrlDate.substring(5,7) + "-" + ctrlDate.substring(8);
            _testKindCd = request.getParameter("TEST_CD");

            _isRuikei = "1".equals(request.getParameter("DATE_DIV"));
            _edate = request.getParameter("DATE").replace('/', '-');

            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");

            _selectSchregno = request.getParameterValues("CATEGORY_SELECTED");

            _avgDiv = request.getParameter("AVG_DIV");

            _documentRoot = request.getParameter("DOCUMENTROOT");

//            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");

            _isNotPrintAvg = "1".equals(request.getParameter("AVG_PRINT"));

            _dispSemester = new String[]{"1", "2", "3"};

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useFormNameD154J = request.getParameter("useFormNameD154J");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }

            if ("2".equals(_knjSchoolMst._semesterDiv)) {
                _semeFlg = "2";
                _semTestKindCds = new String[]{"10101", "10201", "20101", "20201", "10102", "20102"};
            } else {
                _semeFlg = "3";
                _semTestKindCds = new String[]{"10101", "10201", "20101", "20201", "30101", "30201"};
            }

            try {
                loadNameMstZ010(db2);
                loadNameMstZ012(db2);
                loadNameMstC005(db2);
                loadNameMstD046(db2);
            } catch (SQLException e) {
                log.error("名称マスタ読み込みエラー", e);
            }

            loadSemester(db2);
            loadTestItemMstCountflgNew(db2);
            loadControlMst(db2);

            _logoFile = new File(_documentRoot + "/" + _imagePath + "/" + "SCHOOLLOGO." + _extension);

            _staffNames = getStaffNames(db2, getRegdSemester());

            setCertifSchoolDat(db2);
            _schoolKind = getSchoolKind(db2);

            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        public String[] getTargetSemester() {
            final List list = new ArrayList();
            for (int i = 0; i < _dispSemester.length; i++) {
                if (_semester.compareTo(_dispSemester[i]) >= 0) {
                    list.add(_dispSemester[i]);
                }
            }

            String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }

        public String[] getTargetTestKindCds() {
            final List list = new ArrayList();
            for (int i = 0; i < _semTestKindCds.length; i++) {
                if ((_semester + _testKindCd).compareTo(_semTestKindCds[i]) >= 0) {
                    list.add(_semTestKindCds[i]);
                }
            }

            String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '109' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _hrJobName = rs.getString("REMARK2");
                    _schoolAddress = rs.getString("REMARK4");
                    _schoolTelNo = rs.getString("REMARK5");
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadTestItemMstCountflgNew(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD AS KEY, TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW ");
            sql.append(" WHERE YEAR = '" + _year + "' ");
            log.debug("testitemmstcountflgnew sql = " + sql.toString());
            _testItemNames.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testItemNames.put(rs.getString("KEY"), rs.getString("TESTITEMNAME"));
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getNendo() {
            return _isSeireki ? _year + "年度" : nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
        }

        public String getRegdSemester() {
            return Param.SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private void loadSemester(final DB2UDB db2) {
            _semesterMap.clear();

            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    String cd = rs.getString("SEMESTER");
                    String name = rs.getString("SEMESTERNAME");
                    String sdate = rs.getString("SDATE");
                    String edate = rs.getString("EDATE");
                    Semester semester = new Semester(cd, name, sdate, edate);
                    _semesterMap.put(cd, semester);

                    if (first) {
                        _sdate = sdate;
                        first = false;
                    }
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadNameMstZ010(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z010'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                _z010name1 = rs.getString("NAME1");
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            log.debug("(名称マスタZ010):学校名 = " + _z010name1);
        }

        private void loadNameMstZ012(final DB2UDB db2) throws SQLException {
            _isSeireki = false;
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z012'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAME1");
                if ("2".equals(name)) _isSeireki = true;
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            log.debug("(名称マスタZ012):西暦フラグ = " + _isSeireki);
        }

        private void loadNameMstC005(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String is = rs.getString("NAMESPARE1");
                log.debug("(名称マスタC005):科目コード" + subclassCd);
                _subClassC005.put(subclassCd, is);
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public List getStaffNames(final DB2UDB db2, final String semester)
        {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new LinkedList();
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        CASE WHEN L11.STAFFCD IS NOT NULL THEN VALUE(L1.STAFFNAME_REAL, L1.STAFFNAME) ELSE L1.STAFFNAME END AS TR_NAME1 ");
                stb.append("       ,CASE WHEN L21.STAFFCD IS NOT NULL THEN VALUE(L2.STAFFNAME_REAL, L2.STAFFNAME) ELSE L2.STAFFNAME END AS TR_NAME2 ");
                stb.append("       ,CASE WHEN L31.STAFFCD IS NOT NULL THEN VALUE(L3.STAFFNAME_REAL, L3.STAFFNAME) ELSE L3.STAFFNAME END AS TR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L11 ON L11.YEAR = T1.YEAR AND L11.STAFFCD = L1.STAFFCD AND L11.DIV = '04' ");
                stb.append("LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.TR_CD2 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L21 ON L21.YEAR = T1.YEAR AND L21.STAFFCD = L2.STAFFCD AND L21.DIV = '04' ");
                stb.append("LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T1.TR_CD3 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L31 ON L31.YEAR = T1.YEAR AND L31.STAFFCD = L3.STAFFCD AND L31.DIV = '04' ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
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

        public boolean isD046ContainSubclasscd(String subclasscd) {
            return _d046List.contains(subclasscd);
        }

        private void loadNameMstD046(final DB2UDB db2) {

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
            _d046List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d046List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 素点をRECORD_RANK_VDATから読み込むか
         * @param semTestKindCd
         * @return
         */
        public boolean isScoreFromRecordRankVDat(final String semTestKindCd) {
            return "99".equals(semTestKindCd.substring(1,3));
        }

        private String getSchoolKind(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String schoolKind = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        SCHOOL_KIND ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade_hr_class.substring(0, 2) + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
        }
    }
}
