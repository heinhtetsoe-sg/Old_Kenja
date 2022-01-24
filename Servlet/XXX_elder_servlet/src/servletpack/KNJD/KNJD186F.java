// kanji=漢字
/*
 * $Id: e9cf4f333715a263066ef3fb49b35cdcefba6b5a $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186F {
    private static final Log log = LogFactory.getLog(KNJD186F.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String SUBCLASSCD999999AVG = "999999AVG";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            final IPdf ipdf = new SvfPdf(svf);

            response.setContentType("application/pdf");

            outputPdf(ipdf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    public void outputPdf(
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            log.fatal("$Revision: 70933 $ $Date: 2019-11-29 16:05:01 +0900 (金, 29 11 2019) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(request, db2);

            printMain(db2, ipdf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    protected void printMain(
            final DB2UDB db2,
            final IPdf ipdf,
            final Param param
    ) {
        final List studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }
        load(param, db2, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);

            if ("1".equals(param._printSide1)) {
                print1(db2, ipdf, param, student);
                _hasData = true;
            }
            if ("1".equals(param._printSide2)) {
                print2(db2, ipdf, param, student);
                _hasData = true;
            }
        }
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List studentList
    ) {

        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }

        if ("1".equals(param._printSide2)) {
            final Semester semester9 = (Semester) param._semesterMap.get(SEMEALL);
            if (null != semester9) {
                Attendance.load(db2, param, studentMap, semester9._dateRange);
                SubclassAttendance.load(db2, param, studentMap, semester9._dateRange);
            }

            String testcdor = "";
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            for (int i = 0; i < param._testcds.length; i++) {
                final String testcd = param._testcds[i];
                if (null == testcd) {
                    continue;
                }
                final String seme = testcd.substring(0, 1);
                final String kind = testcd.substring(1, 3);
                final String item = testcd.substring(3, 5);
                final String sdiv = testcd.substring(5);
                if (seme.compareTo(param._semester) <= 0) {
                    stbtestcd.append(testcdor);
                    stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                    testcdor = " OR ";
                }
            }
            stbtestcd.append(") ");
            Score.load(db2, param, studentMap, stbtestcd);

            ProfScore.load(db2, param, studentMap);

            try {
                PreparedStatement ps1 = db2.prepareStatement(" SELECT * FROM V_MEDEXAM_DET_DAT WHERE YEAR = '" + param._ctrlYear + "' AND SCHREGNO = ? ");
                PreparedStatement ps2 = db2.prepareStatement(" SELECT * FROM V_MEDEXAM_TOOTH_DAT WHERE YEAR = '" + param._ctrlYear + "' AND SCHREGNO = ? ");
                PreparedStatement ps3 = db2.prepareStatement(" SELECT * FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '" + param._ctrlYear + "' AND DET_SEQ = '010' AND SCHREGNO = ? ");
                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._medexamDetDat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps1, new Object[] {student._schregno}));
                    student._medexamToothDat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps2, new Object[] {student._schregno}));
                    student._medexamDetDetailDat010 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps3, new Object[] {student._schregno}));
                }
                DbUtils.closeQuietly(ps1);
                DbUtils.closeQuietly(ps2);
                DbUtils.closeQuietly(ps3);
                db2.commit();
            } catch (Exception e) {
                log.error("exception!", e);
            }

            try {
                final String sqlHreport = " SELECT SEMESTER, SPECIALACTREMARK, COMMUNICATION, REMARK2 FROM HREPORTREMARK_DAT WHERE YEAR = '" + param._ctrlYear + "' AND (SEMESTER = '9' OR SEMESTER <= '" + param._semester + "') AND SCHREGNO = ? ";
                PreparedStatement ps = db2.prepareStatement(sqlHreport);
                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._semesterHreportremarkDatMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}), "SEMESTER");

                }
                DbUtils.closeQuietly(ps);
                db2.commit();
            } catch (Exception e) {
                log.error("exception!", e);
            }

            try {
                final String sqlSemesRemark = " SELECT SEMESTER, MONTH, REMARK1 FROM ATTEND_SEMES_REMARK_DAT WHERE YEAR = '" + param._ctrlYear + "' AND SEMESTER <= '" + param._semester + "' AND SCHREGNO = ? AND REMARK1 IS NOT NULL ORDER BY SEMESTER, MONTH ";
                PreparedStatement ps = db2.prepareStatement(sqlSemesRemark);
                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map remarkMap = new HashMap();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();
                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
                        final String remark1 = KnjDbUtils.getString(row, "REMARK1");

                        String remark = (String) remarkMap.get(semester);
                        if (null == remark) {
                            remark = "";
                        } else if (!StringUtils.isBlank(remark)) {
                            remark += "  ";
                        }
                        remarkMap.put(semester, remark + remark1);
                    }

                    student._semesRemarkMap = new HashMap(remarkMap);
                }
                DbUtils.closeQuietly(ps);
                db2.commit();
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _hrClass;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
        final Map _subclassMap;
        final List _proficiencySubclassScoreList;
        final String _entyear;
        private String _coursecodeAbbv;
        private Map _medexamDetDat;
        private Map _medexamDetDetailDat010;
        private Map _medexamToothDat;
        private Map _semesterHreportremarkDatMap;
        private Map _semesRemarkMap;
//        private String _communication;

//        private Map _specialGroupKekkaMinutes;
//        private String _totalStudyKatsudo;
//        private String _totalStudyHyoka;
//        private String _careerPlanKatsudo;
//        private String _careerPlanHyoka;

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String hrClass, final String coursecd, final String majorcd, final String coursecode, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _hrClass = hrClass;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
            _attendMap = new TreeMap();
            _subclassMap = new TreeMap();
            _proficiencySubclassScoreList = new ArrayList();
            _semesterHreportremarkDatMap = new HashMap();
            _semesRemarkMap = new HashMap();
//            _specialGroupKekkaMinutes = new HashMap();
        }

        Subclass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    } else {
                        classcd = subclasscd;
                    }
                }
                return new Subclass(new SubclassMst(classcd, subclasscd, null, null, null, null, new Integer(99999), new Integer(99999)));
            }
            return (Subclass) _subclassMap.get(subclasscd);
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,REGDH.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.HR_CLASS ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECODE ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W10.COURSECODEABBV1 ");
            stb.append("            ,REGDH.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._ctrlYear + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSECODE"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR"));
                    student._coursecodeAbbv = rs.getString("COURSECODEABBV1");
                    students.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return students;
        }

        public String getAttendno() {
            return NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno);
        }
    }

    private static class Attendance {
//        private static final String GROUP_LHR = "001";
//        private static final String GROUP_EVENT = "002";
//        private static final String GROUP_COMMITTEE = "003";

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
//        BigDecimal _lhrKekka = new BigDecimal("0");
//        BigDecimal _gyojiKekka = new BigDecimal("0");
//        BigDecimal _iinkaiKekka = new BigDecimal("0");
//        DateRange _dateRange;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final Attendance attendance = new Attendance(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY")
                        );
                        student._attendMap.put(rs.getString("SEMESTER"), attendance);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /**
     * 科目
     */
    private static class Subclass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        final Map _attendMap;
        Subclass _sakiSubclass;

        Subclass(
                final SubclassMst mst
        ) {
            _mst = mst;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
        }

        public Score getScore(final String testcd) {
            if (null == testcd || null == _scoreMap.get(testcd)) {
                return Score.nullScore;
            }
            return (Score) _scoreMap.get(testcd);
        }

        public SubclassAttendance getAttendance(final String key) {
            if (null == key) {
                return null;
            }
            return (SubclassAttendance) _attendMap.get(key);
        }

        public int compareTo(final Object o) {
            final Subclass subclass = (Subclass) o;
            return _mst.compareTo(subclass._mst);
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal sick) {
            _lesson = lesson;
            _sick = sick;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._ctrlYear,
                        SEMEALL,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );

                // log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

//                    final Map specialGroupKekkaMinutes = new HashMap();

                    while (rs.next()) {
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst || "90".equals(mst._subclasscd.substring(0, 2))) {
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            // final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            // final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? replacedSick : sick);

                            if (null == student._subclassMap.get(subclasscd)) {
                                final Subclass subClass = new Subclass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subClass);
                            }
                            final Subclass subClass = student.getSubClass(subclasscd);
                            subClass._attendMap.put(rs.getString("SEMESTER"), subclassAttendance);
                        }

//                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
//                        if (null != specialGroupCd) {
//                            // 特別活動科目の処理 (授業分数と結果数の加算)
//                            final String subclassCd = rs.getString("SUBCLASSCD");
//                            final String kekkaMinutes = rs.getString("SPECIAL_SICK_MINUTES1");
//
//                            getMappedMap(specialGroupKekkaMinutes, specialGroupCd).put(subclassCd, kekkaMinutes);
//                        }
                    }

//                    for (final Iterator spit = specialGroupKekkaMinutes.entrySet().iterator(); spit.hasNext();) {
//                        final Map.Entry e = (Map.Entry) spit.next();
//                        final String specialGroupCd = (String) e.getKey();
//                        final Map subclassKekkaMinutesMap = (Map) e.getValue();
//
//                        int totalMinutes = 0;
//                        for (final Iterator subit = subclassKekkaMinutesMap.entrySet().iterator(); subit.hasNext();) {
//                            final Map.Entry subMinutes = (Map.Entry) subit.next();
//                            final String minutes = (String) subMinutes.getValue();
//                            if (NumberUtils.isDigits(minutes)) {
//                                totalMinutes += Integer.parseInt(minutes);
//                            }
//                        }
//
//                        final BigDecimal spGroupKekkaJisu = getSpecialAttendExe(totalMinutes, param);
//
//                        if (null == student._attendMap.get(dateRange._key)) {
//                            student._attendMap.put(dateRange._key, new Attendance(0, 0, 0, 0, 0, 0, 0, 0));
//                        }
//                        final Attendance attendance = (Attendance) student._attendMap.get(dateRange._key);
//
//                        if (Attendance.GROUP_LHR.equals(specialGroupCd)) {
//                            attendance._lhrKekka = spGroupKekkaJisu;
//                        } else if (Attendance.GROUP_EVENT.equals(specialGroupCd)) {
//                            attendance._gyojiKekka = spGroupKekkaJisu;
//                        } else if (Attendance.GROUP_COMMITTEE.equals(specialGroupCd)) {
//                            attendance._iinkaiKekka = spGroupKekkaJisu;
//                        }
//                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

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
    }

    /**
     * 成績
     */
    private static class Score {

        static final Rank nullRank = new Rank(null, null, null, null, null);
        static final Score nullScore = new Score(null, null, null, nullRank, nullRank, nullRank, nullRank, null, null, null, null, null);

        final String _score;
//        final String _assessLevel;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;
        final String _karihyotei;
        final String _replacemoto;
//        final String _compCredit;
//        final String _getCredit;
        final String _provFlg;

        Score(
                final String score,
                final String assessLevel,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank,
                final String karihyotei,
                final String replacemoto,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
            _score = score;
//            _assessLevel = assessLevel;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
//            _compCredit = compCredit;
//            _getCredit = getCredit;
            _provFlg = provFlg;
        }

//        /**
//         * @return 合併元科目はnullを、以外はcompCreditを戻します。
//         */
//        public String getCompCredit() {
//            return enableCredit() ? _compCredit : null;
//        }
//
//        /**
//         * @return 合併元科目はnullを、以外はgetCreditを戻します。
//         */
//        public String getGetCredit() {
//            return enableCredit() ? _getCredit : null;
//        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1) {
                return false;
            }
            return true;
        }

//        private int getFailValue(final Param param) {
//            if (param.isPerfectRecord() && null != _passScore) {
//                return Integer.parseInt(_passScore);
//            } else if (param.isKetten() && !StringUtils.isBlank(param._ketten)) {
//                return Integer.parseInt(param._ketten);
//            }
//            return -1;
//        }

        private boolean isFail(final Param param, final String testcd) {
//            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
//                return "1".equals(_score);
//            }
//            return "1".equals(_assessLevel);
            return NumberUtils.isDigits(_score) && Integer.parseInt(_score) <= 30;
        }

        public String toString() {
            return "Score(" + _score + ")";
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final StringBuffer stbtestcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param, stbtestcd);
                log.info(" subclass query start.");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" subclass query end.");

                while (rs.next()) {
                    final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final String testcd = rs.getString("TESTCD");

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    String scoreString = rs.getString("SCORE");
                    if ("*".equals(rs.getString("VALUE_DI"))) {
                    	if (!("99".equals(rs.getString("TESTKINDCD")) && "00".equals(rs.getString("TESTITEMCD")))) {
                    		scoreString = "欠";
                    	}
                    }

                    final Score score = new Score(
                            scoreString,
                            null, // rs.getString("ASSESS_LEVEL"),
                            rs.getString("AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // rs.getString("KARI_HYOUTEI"),
                            rs.getString("REPLACEMOTO"),
                            rs.getString("COMP_CREDIT"),
                            rs.getString("GET_CREDIT"),
                            rs.getString("PROV_FLG")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final Subclass subClass = new Subclass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subClass);
                    }
                    if (null == testcd) {
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final Subclass subClass = student.getSubClass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlScore(final Param param, final StringBuffer stbtestcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");

            stb.append("     WHERE   W1.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, ");
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

//            stb.append("   , REL_COUNT AS (");
//            stb.append("   SELECT SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("     , COUNT(*) AS COUNT ");
//            stb.append("          FROM RELATIVEASSESS_MST ");
//            stb.append("          WHERE GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND ASSESSCD = '3' ");
//            stb.append("   GROUP BY SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
//            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("       FROM RELATIVEASSESS_MST L3 ");
//            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
//            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("         AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("      ) ELSE ");
//            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("       FROM ASSESS_MST L3 ");
//            stb.append("       WHERE L3.ASSESSCD = '3' ");
//            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("      ) ");
//            stb.append("     END AS ASSESS_LEVEL ");
            stb.append("    ,W3.AVG ");
            stb.append("    ,W3.GRADE_RANK ");
            stb.append("    ,W3.GRADE_AVG_RANK ");
            stb.append("    ,W3.CLASS_RANK ");
            stb.append("    ,W3.CLASS_AVG_RANK ");
            stb.append("    ,W3.COURSE_RANK ");
            stb.append("    ,W3.COURSE_AVG_RANK ");
            stb.append("    ,W3.MAJOR_RANK ");
            stb.append("    ,W3.MAJOR_AVG_RANK ");
            stb.append("    ,T_AVG1.AVG AS GRADE_AVG ");
            stb.append("    ,T_AVG1.COUNT AS GRADE_COUNT ");
            stb.append("    ,T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
            stb.append("    ,T_AVG2.AVG AS HR_AVG ");
            stb.append("    ,T_AVG2.COUNT AS HR_COUNT ");
            stb.append("    ,T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
            stb.append("    ,T_AVG3.AVG AS COURSE_AVG ");
            stb.append("    ,T_AVG3.COUNT AS COURSE_COUNT ");
            stb.append("    ,T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
            stb.append("    ,T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("    ,T_AVG4.COUNT AS MAJOR_COUNT ");
            stb.append("    ,T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG1.AVG_DIV = '1' ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("     AND (W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("       OR W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.VALUE_DI ");
            stb.append("    ,W3.COMP_CREDIT ");
            stb.append("    ,W3.GET_CREDIT ");
            stb.append("    ,W2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("            AND (SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("              OR SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append("     ) ");

//            //成績不振科目データの表
//            stb.append(",RECORD_SLUMP AS(");
//            stb.append("    SELECT  W3.SCHREGNO ");
//            stb.append("    ,W3.SEMESTER ");
//            stb.append("    ,W3.TESTKINDCD ");
//            stb.append("    ,W3.TESTITEMCD ");
//            stb.append("    ,W3.SCORE_DIV, ");
//            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN T4.NAME1 END AS SLUMP_MARK ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN W3.SCORE END AS SLUMP_SCORE ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
//            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM RELATIVEASSESS_MST L3 ");
//            stb.append("           WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("          ) ELSE ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM ASSESS_MST L3 ");
//            stb.append("           WHERE L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("          ) ");
//            stb.append("         END ");
//            stb.append("    END AS SLUMP_SCORE_KANSAN ");
//            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
//            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
//            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
//            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
//            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
//            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("        LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D054' AND T4.NAMECD2 = W3.MARK ");
//            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
//            stb.append(stbtestcd.toString());
//            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            stb.append("            AND (W1.SIDOU_INPUT_INF = '1' AND W3.MARK IS NOT NULL ");
//            stb.append("              OR W1.SIDOU_INPUT_INF = '2' AND W3.SCORE IS NOT NULL ");
//            stb.append("                ) ");
//            stb.append("     ) ");
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._ctrlYear + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._ctrlYear + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SCORE ");
//            stb.append("    UNION ");
//            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
//            stb.append("    UNION ");
//            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SLUMP ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER ");
            stb.append("        ,T2.TESTKINDCD ");
            stb.append("        ,T2.TESTITEMCD ");
            stb.append("        ,T2.SCORE_DIV ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
//            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,T3.GRADE_RANK ");
            stb.append("        ,T3.GRADE_AVG_RANK ");
            stb.append("        ,T3.CLASS_RANK ");
            stb.append("        ,T3.CLASS_AVG_RANK ");
            stb.append("        ,T3.COURSE_RANK ");
            stb.append("        ,T3.COURSE_AVG_RANK ");
            stb.append("        ,T3.MAJOR_RANK ");
            stb.append("        ,T3.MAJOR_AVG_RANK ");
            stb.append("        ,T3.GRADE_AVG ");
            stb.append("        ,T3.GRADE_COUNT ");
            stb.append("        ,T3.GRADE_HIGHSCORE ");
            stb.append("        ,T3.HR_AVG ");
            stb.append("        ,T3.HR_COUNT ");
            stb.append("        ,T3.HR_HIGHSCORE ");
            stb.append("        ,T3.COURSE_AVG ");
            stb.append("        ,T3.COURSE_COUNT ");
            stb.append("        ,T3.COURSE_HIGHSCORE ");
            stb.append("        ,T3.MAJOR_AVG ");
            stb.append("        ,T3.MAJOR_COUNT ");
            stb.append("        ,T3.MAJOR_HIGHSCORE ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,T33.VALUE_DI ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

//            //成績不振科目データの表
//            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) < '90' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
        public String getRank(final Param param) {
            return "2".equals(param._rankDiv) ? _avgRank : _rank;
        }
    }

    private static class Average {

        private static Average NULL = new Average(null, null, null, null, null, null, null);

        final String _subclasscd;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final String _avg;
        final String _stddev;
        public Average(final String subclasscd, final String score, final String highscore, final String lowscore, final String count, final String avg, final String stddev) {
            _subclasscd = subclasscd;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }

        public static Average getAverage(final Map avgMap, final String avgKey) {
            Average average = (Average) avgMap.get(avgKey);
            if (null == average) {
                return NULL;
            }
            return average;
        }

        public static String avgKey(final String testcd, final String avgDiv, final Student student, final String subclasscd) {
            if ("1".equals(avgDiv)) {
                return avgKey(testcd, avgDiv, student._grade, "000", "00000000", subclasscd);
//            } else if ("2".equals(avgDiv)) {
//                return avgKey(testcd, avgDiv, student._grade, student._hrclass, "00000000", subclasscd);
//            } else if ("3".equals(avgDiv)) {
//                return avgKey(testcd, avgDiv, student._grade, "000", student._course, subclasscd);
            }
            return null;
        }

        public static String avgKey(final String testcd, final String avgDiv, final String grade, final String hrClass, final String course, final String subclasscd) {
            return testcd + "-" + avgDiv + "-" + grade + "-" + hrClass + "-" + course + ":" + subclasscd;
        }

        private static String bdSetScale(final String val) {
            if (!NumberUtils.isNumber(val)) {
                return val;
            }
            final BigDecimal kirisage = new BigDecimal(val).setScale(0, BigDecimal.ROUND_FLOOR);
            if (new BigDecimal(val).compareTo(kirisage) == 0) {
                return kirisage.toString();
            }
            return val;
        }

        private static Map getAverageMap(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            final String sql = Average.sqlAverage(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String subclasscd;
                if (!SUBCLASSCD999999.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) && !SUBCLASSCD999999AVG.equals(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
                    subclasscd = KnjDbUtils.getString(row, "CLASSCD") + '-' + KnjDbUtils.getString(row, "SCHOOL_KIND") + '-' + KnjDbUtils.getString(row, "CURRICULUM_CD") + '-' + KnjDbUtils.getString(row, "SUBCLASSCD");
                } else {
                    subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }
                final String testcd = KnjDbUtils.getString(row, "TESTCD");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String course = KnjDbUtils.getString(row, "COURSE");
                final String score = bdSetScale(KnjDbUtils.getString(row, "SCORE"));
                final String highscore = bdSetScale(KnjDbUtils.getString(row, "HIGHSCORE"));
                final String lowscore = bdSetScale(KnjDbUtils.getString(row, "LOWSCORE"));
                final String count = bdSetScale(KnjDbUtils.getString(row, "COUNT"));
                final String avg = KnjDbUtils.getString(row, "AVG");
                final String stddev = KnjDbUtils.getString(row, "STDDEV");
                final Average average = new Average(subclasscd, score, highscore, lowscore, count, avg, stddev);

                final String avgKey = avgKey(testcd, KnjDbUtils.getString(row, "AVG_DIV"), grade, hrClass, course, subclasscd);
                rtn.put(avgKey, average);
            }
            return rtn;
        }

        private static String sqlAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH RANK9 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ");
            stb.append("     , CASE WHEN N1.NAMESPARE1 IN ('2','4') THEN '1' END AS RYOUSEI_FLG ");
            stb.append("     , T1.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
            stb.append("                 FROM SCHREG_REGD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._ctrlYear +"' ");
            stb.append("                 AND   SEMESTER = '" + param._schregSemester + "' ");
            stb.append("                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT S3 ");
            stb.append("             ON S3.YEAR         = REGD.YEAR ");
            stb.append("            AND S3.GRADE        = REGD.GRADE ");
            stb.append("        LEFT JOIN SCHREG_BRANCH_DAT S4 ");
            stb.append("             ON S4.SCHOOLCD     = '000000000000' ");
            stb.append("            AND S4.SCHOOL_KIND  = S3.SCHOOL_KIND ");
            stb.append("            AND S4.YEAR         = REGD.YEAR ");
            stb.append("            AND S4.SCHREGNO     = REGD.SCHREGNO ");
            stb.append("        LEFT JOIN V_NAME_MST N1 ");
            stb.append("             ON N1.YEAR         = REGD.YEAR ");
            stb.append("            AND N1.NAMECD1      = 'J008' ");
            stb.append("            AND N1.NAMECD2      = S4.RESIDENTCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.STDDEV ");
            stb.append(" FROM ");
            stb.append("  RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.AVG_DIV IN ('1') "); // 学年
            stb.append("     AND (T2.SUBCLASSCD IS NOT NULL OR T1.SUBCLASSCD = '" + SUBCLASSCD999999 + "') ");

            // 学年
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.TESTCD, ");
            stb.append("     '1' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTCD, T1.GRADE ");
//            // 年組
//            stb.append(" UNION ALL ");
//            stb.append(" SELECT ");
//            stb.append("     T1.TESTCD, ");
//            stb.append("     '2' AS AVG_DIV, T1.GRADE, T1.HR_CLASS, '00000000' AS COURSE, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
//            stb.append("     '999999AVG' AS SUBCLASSCD, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
//            stb.append("     SUM(AVG) AS SCORE, ");
//            stb.append("     MAX(AVG) AS HIGHSCORE, ");
//            stb.append("     MIN(AVG) AS LOWSCORE, ");
//            stb.append("     COUNT(T1.AVG) AS COUNT, ");
//            stb.append("     AVG(T1.AVG) AS AVG, ");
//            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
//            stb.append(" FROM ");
//            stb.append("     RANK9 T1 ");
//            stb.append(" GROUP BY ");
//            stb.append("     T1.TESTCD, T1.GRADE, T1.HR_CLASS ");

            return stb.toString();
        }
    }

    private static class ProfScore {
        final String _schregno;
        final String _semtestcd;
        final String _proficiencyname3;
        final String _proficiencySubclassCd;
        final String _subclassName;
        final String _score;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;

        public ProfScore(
                final String schregno,
                final String semtestcd,
                final String proficiencyname3,
                final String proficiencySubclassCd,
                final String subclassName,
                final String score,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank) {
            _schregno = schregno;
            _semtestcd = semtestcd;
            _proficiencyname3 = proficiencyname3;
            _proficiencySubclassCd = proficiencySubclassCd;
            _subclassName = subclassName;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param);
                log.debug(" proficiency sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        final String semtestcd = rs.getString("SEMTESTCD");
                        final String proficiencyname3 = rs.getString("PROFICIENCYNAME3");
                        final String proficiencySubclassCd = rs.getString("PROFICIENCY_SUBCLASS_CD");
                        final String subclassName = rs.getString("SUBCLASS_NAME");
                        final String score = rs.getString("SCORE");
                        final String avg = rs.getString("AVG");
                        final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), null, rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), null);
                        final Rank hrRank = new Rank(rs.getString("HR_RANK"), null, rs.getString("HR_COUNT"), rs.getString("HR_AVG"), null);
                        final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), null, rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), null);
                        final Rank majorRank =  new Rank(rs.getString("MAJOR_RANK"), null, rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), null);
                        final ProfScore pscore = new ProfScore(schregno, semtestcd, proficiencyname3, proficiencySubclassCd, subclassName, score, avg, gradeRank, hrRank, courseRank, majorRank);
                        student._proficiencySubclassScoreList.add(pscore);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlScore(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS SEMTESTCD, ");
            stb.append("     T2.PROFICIENCYNAME3, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD, T1.YEAR, T1.SEMESTER, T1.PROFICIENCYDIV, T1.PROFICIENCYCD, ");
            stb.append("     VALUE(T9.SUBCLASS_ABBV, T9.SUBCLASS_NAME) AS SUBCLASS_NAME, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.RANK AS GRADE_RANK, ");
            stb.append("     T4.RANK AS HR_RANK, ");
            stb.append("     T5.RANK AS COURSE_RANK, ");
            stb.append("     T6.RANK AS MAJOR_RANK, ");
            stb.append("     T11.COUNT AS GRADE_COUNT, ");
            stb.append("     T14.COUNT AS HR_COUNT, ");
            stb.append("     T15.COUNT AS COURSE_COUNT, ");
            stb.append("     T16.COUNT AS MAJOR_COUNT, ");
            stb.append("     T11.AVG AS GRADE_AVG, ");
            stb.append("     T14.AVG AS HR_AVG, ");
            stb.append("     T15.AVG AS COURSE_AVG, ");
            stb.append("     T16.AVG AS MAJOR_AVG ");
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append(" INNER JOIN PROFICIENCY_MST T2 ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append(" LEFT JOIN PROFICIENCY_SUBCLASS_MST T9 ON T9.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T4 ON  T4.SCHREGNO = T1.SCHREGNO AND T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T4.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T4.PROFICIENCYCD = T1.PROFICIENCYCD AND T4.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T4.RANK_DATA_DIV = T1.RANK_DATA_DIV ");
            stb.append("     AND T4.RANK_DIV = '02' "); // HR
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T5 ON  T5.SCHREGNO = T1.SCHREGNO AND T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T5.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T5.PROFICIENCYCD = T1.PROFICIENCYCD AND T5.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T5.RANK_DATA_DIV = T1.RANK_DATA_DIV ");
            stb.append("     AND T5.RANK_DIV = '03' "); // コース
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T6 ON  T6.SCHREGNO = T1.SCHREGNO AND T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T6.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T6.PROFICIENCYCD = T1.PROFICIENCYCD AND T6.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T6.RANK_DATA_DIV = T1.RANK_DATA_DIV ");
            stb.append("     AND T6.RANK_DIV = '04' "); // 学科
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T11 ON T11.YEAR = T1.YEAR AND T11.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T11.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T11.PROFICIENCYCD = T1.PROFICIENCYCD AND T11.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T11.DATA_DIV = '1' AND T11.GRADE = T3.GRADE ");
            stb.append("     AND T11.AVG_DIV = '01' "); // 学年
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T14 ON T14.YEAR = T1.YEAR AND T14.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T14.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T14.PROFICIENCYCD = T1.PROFICIENCYCD AND T14.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T14.DATA_DIV = '1' AND T14.GRADE = T3.GRADE ");
            stb.append("     AND T14.AVG_DIV = '02' AND T14.HR_CLASS = T3.HR_CLASS "); // HR
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T15 ON T15.YEAR = T1.YEAR AND T15.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T15.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T15.PROFICIENCYCD = T1.PROFICIENCYCD AND T15.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T15.DATA_DIV = '1' AND T15.GRADE = T3.GRADE ");
            stb.append("     AND T15.AVG_DIV = '03' AND T15.COURSECD = T3.COURSECD AND T15.MAJORCD = T3.MAJORCD AND T15.COURSECODE = T3.COURSECODE "); // コース
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T16 ON T16.YEAR = T1.YEAR AND T16.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T16.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T16.PROFICIENCYCD = T1.PROFICIENCYCD AND T16.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T16.DATA_DIV = '1' AND T16.GRADE = T3.GRADE ");
            stb.append("     AND T16.AVG_DIV = '04' AND T16.COURSECD = T3.COURSECD AND T16.MAJORCD = T3.MAJORCD AND T16.COURSECODE = '0000' "); // 学科
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append("     AND (T2.PROFICIENCYNAME1 IS NOT NULL OR T1.PROFICIENCY_SUBCLASS_CD = '" + SUBCLASSCD999999 + "') ");
            if ("2".equals(param._rankDiv)) {
                stb.append("     AND T1.RANK_DATA_DIV = '02' "); // 平均点
            } else {
                stb.append("     AND T1.RANK_DATA_DIV = '01' "); // 総合点
            }
            stb.append("     AND T1.RANK_DIV = '01' "); // 学年順位
            return stb.toString();
        }
    }

    private static class ProfAverage {

        private static final ProfAverage NULL = new ProfAverage(null, null, null, null, null);
        private static final String AVG_DIV_GRADE = "01";
        private static final String AVG_DIV_HR = "02";
        private static final String AVG_DIV_COURSE = "03";
        private static final String AVG_DIV_MAJOR = "04";
        private static final String AVG_DIV_COURSEGROUP = "05";

        final String _subclasscd;
        final String _avg;
        final String _stddev;
        final String _highScore;
        final Integer _count;

        private ProfAverage(
                final String subclasscd,
                final String avg,
                final String stddev,
                final String highScore,
                final Integer count
        ) {
            _subclasscd = subclasscd;
            _avg = avg;
            _stddev = stddev;
            _highScore = highScore;
            _count = count;
        }

        public String toString() {
            return _subclasscd + "/" + _avg + "/" + _count;
        }

        public static ProfAverage getAverage(final Map proficiencyAvgMap, final String avgKey) {
            ProfAverage average = (ProfAverage) proficiencyAvgMap.get(avgKey);
            if (null == average) {
//                log.info(" null average : key = " + avgKey);
                return NULL;
            }
            return average;
        }

        private static String avgKey(final String testcd, final String subclasscd, final String avgDiv, final Student student) {
            return avgKey(testcd, subclasscd, avgDiv, student._grade, student._hrClass, student._coursecd, student._majorcd, student._coursecode);
        }

        private static String avgKey(final String testcd, final String subclasscd, final String avgDiv, final String grade, final String hrClass, final String coursecd, final String majorcd, final String coursecode) {
            final String key;
            if (AVG_DIV_GRADE.equals(avgDiv)) {
                key = testcd + "-" + subclasscd + "-" + grade + "000" + "0" + "000" + "0000";
//            } else if (AVG_DIV_HR.equals(avgDiv)) {
//                key = testcd + "-" + subclasscd + "-" + grade + hrClass + "0" + "000" + "0000";
//            } else if (AVG_DIV_COURSE.equals(avgDiv)) {
//                key = testcd + "-" + subclasscd + "-" + grade + "000" + coursecd + majorcd + coursecode;
//            } else if (AVG_DIV_COURSEGROUP.equals(avgDiv)) {
//                key = testcd + "-" + subclasscd + "-" + grade + "000" + "0" + majorcd + "0000";
            } else {
                key = "invalid_avg_div:" + avgDiv;
            }
            return key;
        }

        private static Map getAverageMap(
                final DB2UDB db2,
                final Param param
        ) {

            final String avgDataDiv = "1";

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH RANK9 AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS TESTCD, ");
            stb.append("     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD,");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ");
            stb.append("     , T1.AVG ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
            stb.append("                 FROM SCHREG_REGD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._ctrlYear +"' ");
            stb.append("                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO AND (REGD.SEMESTER = T1.SEMESTER OR T1.SEMESTER = '9' AND REGD.SEMESTER = '" + param._schregSemester + "') ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.PROFICIENCY_SUBCLASS_CD = '" + SUBCLASSCD999999 + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.RANK_DIV = '01' ");
            if ("2".equals(param._rankDiv)) {
                stb.append("     AND T1.RANK_DATA_DIV = '02' "); // 平均点
            } else {
                stb.append("     AND T1.RANK_DATA_DIV = '01' "); // 総合点
            }
            stb.append(" ) ");
            stb.append("SELECT");
            stb.append("  SEMESTER || PROFICIENCYDIV || PROFICIENCYCD AS TESTCD,");
            stb.append("  AVG_DIV,");
            stb.append("  PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD,");
            stb.append("  AVG,");
            stb.append("  STDDEV,");
            stb.append("  HIGHSCORE,");
            stb.append("  COUNT,");
            stb.append("  GRADE,");
            stb.append("  HR_CLASS,");
            stb.append("  COURSECD,");
            stb.append("  MAJORCD,");
            stb.append("  COURSECODE");
            stb.append(" FROM");
            stb.append("  PROFICIENCY_AVERAGE_DAT");
            stb.append(" WHERE");
            stb.append("    YEAR = '" + param._ctrlYear + "' AND");
            stb.append("    SEMESTER <= '" + param._semester + "' AND");
            stb.append("    DATA_DIV = '" + avgDataDiv + "' AND");
            stb.append("    GRADE = '" + param._grade + "'");
            stb.append(" UNION ALL ");
            stb.append("SELECT");
            stb.append("  T1.TESTCD,");
            stb.append("  '01' AS AVG_DIV,");
            stb.append("  T1.SUBCLASSCD || 'AVG' AS SUBCLASSCD,");
            stb.append("  AVG(T1.AVG) AS AVG,");
            stb.append("  STDDEV(T1.AVG) AS STDDEV,");
            stb.append("  MAX(T1.AVG) AS HIGHSCORE,");
            stb.append("  COUNT(T1.AVG) AS COUNT,");
            stb.append("  T1.GRADE,");
            stb.append("  '000' AS HR_CLASS,");
            stb.append("  '0' AS COURSECD,");
            stb.append("  '000' AS MAJORCD,");
            stb.append("  '0000' AS COURSECODE ");
            stb.append(" FROM");
            stb.append("  RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("    T1.TESTCD, T1.SUBCLASSCD, T1.GRADE ");

            final String sql = stb.toString();
            log.info(" avg sql = " + sql);

            final Map avgMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("testcd");
                    final String subclasscd = rs.getString("subclasscd");
                    final String avgDiv = rs.getString("avg_div");
                    final String avg = rs.getString("avg");
                    final String stddev = rs.getString("stddev");
                    final Integer count = KNJServletUtils.getInteger(rs, "count");
                    final String highScore = rs.getString("highscore");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");

                    final ProfAverage avgDat = new ProfAverage(subclasscd, avg, stddev, highScore, count);
                    avgMap.put(avgKey(testcd, subclasscd, avgDiv, grade, hrClass, coursecd, majorcd, coursecode), avgDat);
                }
            } catch (final SQLException e) {
                log.fatal("模試成績平均データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return avgMap;
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        boolean _isSaki;
        SubclassMst _combinedSubclassMst;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3, final Integer subclassShoworder3) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int cmp = 0;
            if (0 != cmp) return cmp;
            cmp = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != cmp) return cmp;
            if (null != _classcd && null != mst._classcd) {
                cmp = _classcd.compareTo(mst._classcd);
                if (0 != cmp) return cmp;
            }
            cmp = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != cmp) return cmp;
            if (null != _subclasscd && null != mst._subclasscd) {
                cmp = _subclasscd.compareTo(mst._subclasscd);
            }
            return cmp;
        }
    }

    private void print1(final DB2UDB db2, final IPdf ipdf, final Param param, final Student student) {
        final String form = "KNJD186F_1.frm";
        ipdf.VrSetForm(form, 1);

        // 年号と年度の間にスペース追加
        String nengonendo = KNJ_EditDate.gengou(db2, Integer.parseInt(param._ctrlYear));
        int firstnumidx = -1;
        for (int n = 0; n <= 9; n++) {
            final int idx = nengonendo.indexOf(String.valueOf(n));
            if (0 <= idx) {
                if (-1 == firstnumidx) {
                    firstnumidx = idx;
                } else {
                    firstnumidx = Math.min(firstnumidx, idx);
                }
            }
        }
        if (0 <= firstnumidx) {
            nengonendo = nengonendo.substring(0, firstnumidx) + " " + nengonendo.substring(firstnumidx);
        }
        ipdf.VrsOut("NENDO", nengonendo + " 年度"); // 年度
        ipdf.VrsOut("SCHOOL_NAME", param._schoolName); // 学校名
        ipdf.VrsOut("GRADE_NAME", "第 " + (NumberUtils.isDigits(param._gradeCd) ? String.valueOf(Integer.parseInt(param._gradeCd)) : StringUtils.defaultString(param._gradeCd)) + " 学年"); // 学年名
        if (null != param._schoolLogoImagePath) {
            ipdf.VrsOut("Bitmap_Field1", param._schoolLogoImagePath); //
        }
        ipdf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrClassName1) + "　ホーム"); // 組名称
        ipdf.VrsOut("NO", student.getAttendno() + " 番"); // 出席番号

        final int ketaName = getMS932ByteLength(student._name);
        ipdf.VrsOut("NAME" + (ketaName <= 24 ? "1" : ketaName <= 30 ? "2" : "3"), student._name);

        final int keta1 = getMS932ByteLength(param._principalName);
        ipdf.VrsOut("TEACHER_NAME1_" + (keta1 <= 24 ? "1" : keta1 <= 30 ? "2" : "3"), param._principalName);

        final int keta2 = getMS932ByteLength(student._staffName);
        ipdf.VrsOut("TEACHER_NAME2_" + (keta2 <= 24 ? "1" : keta2 <= 30 ? "2" : "3"), student._staffName);

        ipdf.VrEndPage();
    }

    private void print2(final DB2UDB db2, final IPdf ipdf, final Param param, final Student student) {
        final String form = "KNJD186F_2.frm";
        ipdf.VrSetForm(form, 4);

        printHeader(ipdf, param, student);

        printShoken(ipdf, student);

        printAttendance(ipdf, student);

        printJitsuryoku(ipdf, param, student);

        printScore(ipdf, param, student);
    }

    private void printShoken(final IPdf ipdf, final Student student) {
        // 道徳
        final String moral = KnjDbUtils.getString(getMappedMap(student._semesterHreportremarkDatMap, "9"), "REMARK2");
        final List moralToken = KNJ_EditKinsoku.getTokenList(moral, 82);
        for (int i = 0; i < moralToken.size(); i++) {
            ipdf.VrsOut("MORAL" + String.valueOf(i + 1), (String) moralToken.get(i)); // 道徳
        }

        // 特別活動の記録
        final String communication = KnjDbUtils.getString(getMappedMap(student._semesterHreportremarkDatMap, "9"), "COMMUNICATION");
        final List communicationToken = KNJ_EditKinsoku.getTokenList(communication, 78);
        for (int i = 0; i < communicationToken.size(); i++) {
            ipdf.VrsOut("REMARK" + String.valueOf(i + 1), (String) communicationToken.get(i)); // 備考
        }

        // 備考
        for (int j = 0; j < 3; j++) {
            final int semester = j + 1;

            final String specialactremark = KnjDbUtils.getString(getMappedMap(student._semesterHreportremarkDatMap, String.valueOf(semester)), "SPECIALACTREMARK");

            final List specialactremarkToken = KNJ_EditKinsoku.getTokenList(specialactremark, 74);

            for (int i = 0; i < specialactremarkToken.size(); i++) {
                final String ssi = String.valueOf(i + 1);
                ipdf.VrsOutn("SP_ACT" + ssi, semester, (String) specialactremarkToken.get(i)); // 特別活動の記録
            }
        }
    }

    private static String medexamString(final String val) {
        if (null == val || "".equals(val) || "3".equals(val) || "9".equals(val)) {
            return "未";
        }
        if ("1".equals(val)) {
            return "有";
        }
        return "無";
    }

    private void printHeader(final IPdf ipdf, final Param param, final Student student) {
        ipdf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrClassName1) + " ホーム " + student.getAttendno() + "番"); // 年組番

        final int ketaName = getMS932ByteLength(student._name);
        ipdf.VrsOut("NAME" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name);


//        svf.VrsOut("TEXT1", null); // 注釈
//        svf.VrsOut("TEXT2", null); // 注釈

        ipdf.VrsOut("HIGHT", sishaGonyu(KnjDbUtils.getString(student._medexamDetDat, "HEIGHT"))); // 身長
        ipdf.VrsOut("WEIGHT", sishaGonyu(KnjDbUtils.getString(student._medexamDetDat, "WEIGHT"))); // 体重
        ipdf.VrsOut("L_EYE1", KnjDbUtils.getString(student._medexamDetDat, "L_BAREVISION_MARK")); // 視力
        ipdf.VrsOut("R_EYE1", KnjDbUtils.getString(student._medexamDetDat, "R_BAREVISION_MARK")); // 視力
        ipdf.VrsOut("L_EYE2", KnjDbUtils.getString(student._medexamDetDat, "L_VISION_MARK")); // 視力
        ipdf.VrsOut("R_EYE2", KnjDbUtils.getString(student._medexamDetDat, "R_VISION_MARK")); // 視力
        ipdf.VrsOut("URINE", medexamString(KnjDbUtils.getString(student._medexamDetDetailDat010, "DET_REMARK1"))); // 尿検査所見
        ipdf.VrsOut("TOOTH", medexamString(KnjDbUtils.getString(student._medexamDetDetailDat010, "DET_REMARK2"))); // う歯
        if ("01".equals(param._gradeCd)) {
            ipdf.VrsOut("HEART", medexamString(KnjDbUtils.getString(student._medexamDetDetailDat010, "DET_REMARK3"))); // 心電図所見
        } else {
            ipdf.VrsOut("SLASH", "／"); // 心電図スラッシュ用
        }
        ipdf.VrsOut("TUBER", medexamString(KnjDbUtils.getString(student._medexamDetDetailDat010, "DET_REMARK4"))); // 結核問診所見
        ipdf.VrsOut("MEDICINE", medexamString(KnjDbUtils.getString(student._medexamDetDetailDat010, "DET_REMARK5"))); // 内科所見

        for (int si = 0; si < 3; si++) {
            final String ssi = String.valueOf(si + 1);
            final Semester semester = (Semester) param._semesterMap.get(ssi);
            if (null != semester) {
                ipdf.VrsOut("SEMESTER1_" + ssi, semester._semestername); // 学期名
                ipdf.VrsOutn("SEMESTER2", si + 1, semester._semestername); // 学期名
                ipdf.VrsOutn("SEMESTER3", si + 1, semester._semestername); // 学期名
            }
        }
        ipdf.VrsOut("SEMESTER1_4", "学年成績"); // 学期名
        ipdf.VrsOutn("SEMESTER2", 4, "学　年"); // 学期名
    }

    private void printAttendance(final IPdf ipdf, final Student student) {
        for (int j = 0; j < 4; j++) {
            final int line = j + 1;
            final String semester;
            if (line == 4) {
                semester = SEMEALL;
            } else {
                semester = String.valueOf(line);
            }
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if (null != att) {
                ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend)); // 出停
                ipdf.VrsOutn("MOURNING", line, String.valueOf(att._mourning)); // 忌引
                ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
                ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
            }

            final String attendRemark = (String) student._semesRemarkMap.get(semester);
            final int ketaAttend_remark = getMS932ByteLength(attendRemark);
            ipdf.VrsOutn("ATTEND_REMARK" + (ketaAttend_remark <= 34 ? "1" : ketaAttend_remark <= 40 ? "2" : ketaAttend_remark <= 50 ? "3" : "4_1"), line, attendRemark); // 出欠備考
        }
    }

    private void printScore(final IPdf ipdf, final Param param, final Student student) {

        for (int ti = 0; ti < param._testcds.length; ti++) {
            final String testcd = param._testcds[ti];
            final Score score999999 = student.getSubClass(SUBCLASSCD999999).getScore(testcd);
            if (null == score999999) {
                continue;
            }
            final String sfx = param._testcdsField[ti];
            ipdf.VrsOutn("TOTAL_SCORE"   + sfx, 1, score999999._score); // 素点
            ipdf.VrsOutn("TOTAL_SCORE"   + sfx, 2, sishaGonyu(score999999._avg)); // 素点
            ipdf.VrsOutn("TOTAL_SCORE"   + sfx, 3, score999999._hrRank.getRank(param)); // 素点
            ipdf.VrsOutn("TOTAL_SCORE"   + sfx, 4, score999999._hrRank._count); // 素点
            ipdf.VrsOutn("TOTAL_SCORE"   + sfx, 5, score999999._gradeRank.getRank(param)); // 素点
            ipdf.VrsOutn("TOTAL_SCORE"   + sfx, 6, score999999._gradeRank._count); // 素点
        }

        final String[] gradeAvgTestcds = {"1990008", "2990008", "3990008", "9990008"};
        for (int ti = 0; ti < gradeAvgTestcds.length; ti++) {
            final String ssi = String.valueOf(ti + 1);
            final Average average = Average.getAverage(param._avgMap, Average.avgKey(gradeAvgTestcds[ti], "1", student, SUBCLASSCD999999));
            if (null != average) {
                ipdf.VrsOutn("TOTAL_GRADE_AVE" + ssi, 1, sishaGonyu(average._avg)); // 学年平均
            }
            final Average averageAverage = Average.getAverage(param._avgMap, Average.avgKey(gradeAvgTestcds[ti], "1", student, SUBCLASSCD999999AVG));
            if (null != averageAverage) {
                ipdf.VrsOutn("TOTAL_GRADE_AVE" + ssi, 2, sishaGonyu(averageAverage._avg)); // 学年平均
            }
        }

        final int maxRecord = 15;

        final List subclassList = new ArrayList(student._subclassMap.values());
        final List sakiSubclassList = new ArrayList();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._mst._isSaki) {
                sakiSubclassList.add(subclass);
            }
            if (SUBCLASSCD999999.equals(subclass._mst._subclasscd)) {
                it.remove();
            }
        }
        final List removeSakiSubclassList = new ArrayList();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (null != subclass._mst._combinedSubclassMst) {
                for (final Iterator cit = sakiSubclassList.iterator(); cit.hasNext();) {
                    final Subclass sakiSubclass = (Subclass) cit.next();
                    if (sakiSubclass._mst == subclass._mst._combinedSubclassMst) {
                        subclass._sakiSubclass = sakiSubclass;
                        removeSakiSubclassList.add(sakiSubclass);
                    }
                }
            }
        }
        if (param._isOutputDebug) {
            for (final Iterator it = sakiSubclassList.iterator(); it.hasNext();) {
                final Subclass sakiSubclass = (Subclass) it.next();
                log.info(" | saki subclass = " + sakiSubclass._mst._subclasscd + " " + sakiSubclass._mst._subclassname + " / score = " + sakiSubclass._scoreMap);
            }
        }
        subclassList.removeAll(removeSakiSubclassList);
        Collections.sort(subclassList);
        final int printCount = Math.min(subclassList.size(),  maxRecord);
        for (int i = 0; i < printCount; i++) {
            final Subclass subclass = (Subclass) subclassList.get(i);
//            log.info(" subclasscd = " + subclass._mst._subclasscd);

            final boolean isPrintSakikamoku = subclass._sakiSubclass != null;
            final String div = isPrintSakikamoku ? "1" : "2";

            ipdf.VrsOut("GRP" + div, subclass._mst._classcd); // 科目名
            ipdf.VrsOut("SUBCLASS_NAME" + div + (getMS932ByteLength(subclass._mst._subclassname) > 10 ? "_2" : ""), subclass._mst._subclassname); // 科目名

            if (param._isOutputDebug) {
                log.info(" subclass = " + subclass._mst._subclasscd + " " + subclass._mst._subclassname + " / score = " + subclass._scoreMap + " " + (subclass._mst._isSaki ? "[saki]" : "") + ", saki = " + (isPrintSakikamoku ? subclass._sakiSubclass._mst._subclassname : "null"));
            }

            for (int ti = 0; ti < param._testcds.length; ti++) {
                final String testcd = param._testcds[ti];
                final String sfx = param._testcdsField[ti];
                final Score score = student.getSubClass(subclass._mst._subclasscd).getScore(testcd);
                String field;
                field = "SCORE" + div + "_" + sfx;
                ipdf.VrsOut(field, score._score); // 素点
                if (isAkaten(score, param)) {
                    ipdf.VrAttribute(field, "Palette=9"); // 素点
                }
                if (TESTCD_GAKUNEN_HYOKA.equals(testcd) && isPrintSakikamoku) {
                    final Score sakiScore = subclass._sakiSubclass.getScore(testcd);
                    field = "SCORE" + div + "_" + "4_2";
                    if (null == sakiScore._score) {
                    	ipdf.VrsOut(field, "DUMMY" + removeSakiSubclassList.indexOf(subclass._sakiSubclass)); // 素点
                    	ipdf.VrAttribute(field, "X=10000"); // 素点
                    } else {
                    	ipdf.VrsOut(field, sakiScore._score); // 素点
                    	if (isAkaten(sakiScore, param)) {
                    		ipdf.VrAttribute(field, "Palette=9"); // 素点
                    	}
                    }
                }
            }
//            if ("1".equals(div)) {
//                ipdf.VrsOut("SCORE1_4_1", null); // 素点
//                ipdf.VrsOut("SCORE1_4_2", null); // 素点
//            } else if ("2".equals(div)) {
//                ipdf.VrsOut("SCORE2_4_1", null); // 素点
//            }
            for (int ti = 0; ti < gradeAvgTestcds.length; ti++) {
                final String ssi = String.valueOf(ti + 1);
                final Average average = Average.getAverage(param._avgMap, Average.avgKey(gradeAvgTestcds[ti], "1", student, subclass._mst._subclasscd));
                if (null != average) {
                    ipdf.VrsOut("GRADE_AVE" + div + "_" + ssi, sishaGonyu(average._avg)); // 学年平均
                }
            }

            for (int k = 0; k < 4; k++) {
                final int line = k + 1;
                final String semester;
                if (line == 4) {
                    semester = SEMEALL;
                } else {
                    semester = String.valueOf(line);
                }
                final SubclassAttendance sa = (SubclassAttendance) subclass.getAttendance(semester);
                if (null != sa && null != sa._sick) {
                    ipdf.VrsOut("KEKKA" + div + "_" + String.valueOf(line), sa._sick.toString()); // 欠課 TODO
                }
            }

            ipdf.VrEndRecord();
        }
        for (int i = printCount; i < maxRecord; i++) {
            final boolean isPrintSakikamoku = false;
            final String div = isPrintSakikamoku ? "1" : "2";
            ipdf.VrsOut("GRP" + div, String.valueOf(i)); // 科目名
            ipdf.VrsOut("SUBCLASS_NAME" + div, "DUMMY"); // 科目名
            ipdf.VrAttribute("SUBCLASS_NAME" + div, "X=10000");
            ipdf.VrEndRecord();
        }
    }

    private static boolean isAkaten(final Score score, final Param param) {
        if (NumberUtils.isNumber(score._score) && Double.parseDouble(score._score)  < 30) {
            return true;
        }
        return false;
    }

    private void printJitsuryoku(final IPdf ipdf, final Param param, final Student student) {

        final Map testnames = new TreeMap();
        final Map subclassnames = new TreeMap();
        final Set subclasses = new TreeSet();
        final Map scoreMap = new HashMap();
        for (final Iterator it = student._proficiencySubclassScoreList.iterator(); it.hasNext();) {
            final ProfScore pscore = (ProfScore) it.next();
            testnames.put(pscore._semtestcd, pscore._proficiencyname3);
            if (!("333333".equals(pscore._proficiencySubclassCd) || "555555".equals(pscore._proficiencySubclassCd) || SUBCLASSCD999999.equals(pscore._proficiencySubclassCd))) {
                subclasses.add(pscore._proficiencySubclassCd);
            }
            scoreMap.put(pscore._semtestcd + ":" + pscore._proficiencySubclassCd, pscore);
            subclassnames.put(pscore._proficiencySubclassCd, pscore._subclassName);
        }
        final List subclasscdList = new ArrayList(subclasses);

        for (int i = 0; i < subclasscdList.size(); i++) {
            final String subclasscd = (String) subclasscdList.get(i);
            final String subclassname = (String) subclassnames.get(subclasscd);
            ipdf.VrsOut("MOCK_CLASS_NAME" + String.valueOf(i + 1), subclassname); // 実力教科名
        }

        final int mockMax = 3;
        int testline = 1;
        for (final Iterator itt = testnames.keySet().iterator(); itt.hasNext();) {
            final String testcd = (String) itt.next();
            final String testname = (String) testnames.get(testcd);
            ipdf.VrsOutn("MOCK_DATE", testline, testname); // 実力実施日

            for (int i = 0; i < subclasscdList.size(); i++) {
                final String si = String.valueOf(i + 1);
                final String subclasscd = (String) subclasscdList.get(i);

                final ProfScore pscore = (ProfScore) scoreMap.get(testcd + ":" + subclasscd);
                if (null != pscore) {
                    ipdf.VrsOutn("MOCK_SCORE" + si, testline, pscore._score); // 実力得点
                }
                final ProfAverage avg = ProfAverage.getAverage(param._profAvgMap, ProfAverage.avgKey(testcd, subclasscd, ProfAverage.AVG_DIV_GRADE, student));
                ipdf.VrsOutn("MOCK_AVE" + si, testline, sishaGonyu(avg._avg)); // 実力得点
            }

            final ProfScore pscore999999 = (ProfScore) scoreMap.get(testcd + ":" + SUBCLASSCD999999);
            if (null != pscore999999) {

                ipdf.VrsOutn("MOCK_TOTAL", testline, pscore999999._score); // 実力合計
                ipdf.VrsOutn("MOCK_AVE", testline, sishaGonyu(pscore999999._avg)); // 実力平均

                ipdf.VrsOutn("MOCK_HR_RANK1", testline, pscore999999._hrRank._rank); // ホーム順位
                ipdf.VrsOutn("MOCK_HR_RANK2", testline, pscore999999._hrRank._count); // ホーム順位
                ipdf.VrsOutn("MOCK_GRADE_RANK1", testline, pscore999999._gradeRank._rank); // 学年順位
                ipdf.VrsOutn("MOCK_GRADE_RANK2", testline, pscore999999._gradeRank._count); // 学年順位
//              ipdf.VrsOutn("MOCK_RANK", testline, null); // 実力学年順位
            }

            final ProfAverage avg999999 = ProfAverage.getAverage(param._profAvgMap, ProfAverage.avgKey(testcd, SUBCLASSCD999999, ProfAverage.AVG_DIV_GRADE, student));
            ipdf.VrsOutn("MOCK_TOTAL_AVE", testline, sishaGonyu(avg999999._avg)); // 実力合計平均
            final ProfAverage avg999999Avg = ProfAverage.getAverage(param._profAvgMap, ProfAverage.avgKey(testcd, SUBCLASSCD999999AVG, ProfAverage.AVG_DIV_GRADE, student));
            ipdf.VrsOutn("MOCK_AVE_AVE", testline, sishaGonyu(avg999999Avg._avg)); // 実力平均平均
            testline += 1;
            if (mockMax < testline) {
                break;
            }
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _ctrlSemester;
        final String _schregSemester;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        final String _printSide1;
        final String _printSide2;

        final String _rankDiv = "2"; // 順位の基準点 1:総合点 2:平均点
        final String _tutisyoPrintKariHyotei; // 仮評定を表示する
        final String _gradeCd;
        final String _documentroot;
        final boolean _isOutputDebug;
        final String _imagePath;
        final String _extension;
        final String _schoolLogoImagePath;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMstMap;

        private KNJSchoolMst _knjSchoolMst;

        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private boolean _isNoPrintMoto;
        final String[] _testcds;
        final String[] _testcdsField;
        final Map _attendParamMap;
        final Map _avgMap;
        final Map _profAvgMap;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _printSide1 = request.getParameter("PRINT_SIDE1");
            _printSide2 = request.getParameter("PRINT_SIDE2");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _tutisyoPrintKariHyotei = request.getParameter("tutisyoPrintKariHyotei");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _semester = _knjSchoolMst._semesterDiv.equals(request.getParameter("SEMESTER")) ? SEMEALL : request.getParameter("SEMESTER");
            _schregSemester = request.getParameter("SEMESTER");
            _semesterMap = loadSemester(db2, _ctrlYear, _grade);
            _gradeCd = getGradeCd(db2);
            setCertifSchoolDat(db2);

            setSubclassMst(db2);
            loadNameMstD016(db2);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            final KNJ_Control.ReturnVal returnval = getImagepath(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _schoolLogoImagePath = getImagePath();

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _testcds = new String[] {
                    "1010101",
                    "1020101",
                    "1990008",
                    "2010101",
                    "2020101",
                    "2990008",
                    "3020101",
                    "3990008",
                    TESTCD_GAKUNEN_HYOKA,
                    TESTCD_GAKUNEN_HYOTEI,
            };

            _testcdsField = new String[] {
                    "1_1",
                    "1_2",
                    "1_3",
                    "2_1",
                    "2_2",
                    "2_3",
                    "3_1",
                    "3_2",
                    "4_1",
                    null,
            };
            _avgMap = Average.getAverageMap(db2, this);
            _profAvgMap = ProfAverage.getAverageMap(db2, this);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186F' AND NAME = '" + propName + "' "));
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getImagepath(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        public String getImagePath() {
            final String path = _documentroot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO_J." + _extension;
            final boolean exists = new java.io.File(path).exists();
            log.info(" image path " + path + " exists? " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private Semester getSemester(final String semester) {
            return (Semester) _semesterMap.get(semester);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private String getGradeCd(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT GRADE_CD FROM SCHREG_REGD_GDAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ");
            log.debug("gdat sql = " + sql.toString());

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _jobName = KnjDbUtils.getString(row, "JOB_NAME");
            _principalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _hrJobName = KnjDbUtils.getString(row, "REMARK2");

            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    } else {
                        classcd = subclasscd;
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null, new Integer(99999), new Integer(99999));
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer classShoworder3 = Integer.valueOf(rs.getString("CLASS_SHOWORDER3"));
                    final Integer subclassShoworder3 = Integer.valueOf(rs.getString("SUBCLASS_SHOWORDER3"));
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), classShoworder3, subclassShoworder3);
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            String sql1 = "";
            sql1 += " SELECT ";
            sql1 += "    COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            sql1 += "    ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
            sql1 += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";

            for (final Iterator it = KnjDbUtils.query(db2, sql1).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String combiendSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");

                SubclassMst combinedSubclassMst = (SubclassMst) _subclassMstMap.get(combiendSubclasscd);
                SubclassMst attendSubclassMst = (SubclassMst) _subclassMstMap.get(attendSubclasscd);
                if (null != combinedSubclassMst && null != attendSubclassMst) {
                    attendSubclassMst._combinedSubclassMst = combinedSubclassMst;
                    combinedSubclassMst._isSaki = true;
                }
            }
        }
    }
}
