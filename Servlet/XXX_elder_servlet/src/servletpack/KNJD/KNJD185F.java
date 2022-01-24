// kanji=漢字
/*
 * $Id: d241630a335ba3cc276bd6927f865bcdcb635418 $
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
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD185F {
    private static final Log log = LogFactory.getLog(KNJD185F.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private Param _param;
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

            response.setContentType("application/pdf");

            outputPdf(svf, request);

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
            final Vrw32alp svf,
            final HttpServletRequest request
    ) throws ServletException, IOException {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = createParam(request, db2);

            printMain(db2, svf);

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

    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        final List studentList = Student.getStudentList(db2, _param);
        if (studentList.isEmpty()) {
            return;
        }

        load(_param, db2, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);

            //表紙
            if ("1".equals(_param._printHyoshi)) {
            	setHyoushi(svf);
            }

            _param._form.print(db2, svf, student);
        }
        _hasData = true;
    }

    private void setHyoushi(final Vrw32alp svf) {
        svf.VrSetForm("KNJD185F_3.frm", 1);

        //学校ロゴ
        if (null != _param.getImagePath()) {
            svf.VrsOut("LOGO", _param.getImagePath());
        }
        //学校名
        svf.VrsOut("SCHOOLNAME", _param._schoolName);

        svf.VrEndPage();
    }

    public void load(
            final Param param,
            final DB2UDB db2,
            final List studentList0
    ) {
        final Map courseStudentsMap = new HashMap();
        for (final Iterator it = studentList0.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            if (null == courseStudentsMap.get(key)) {
                courseStudentsMap.put(key, new ArrayList());
            }
            ((List) courseStudentsMap.get(key)).add(student);
        }

        param._form._stbtestcd = param._form.setStbTestCd("W3");

        for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final List studentList = (List) courseStudentsMap.get(key);

            Score.load(db2, _param, studentList);

            final String[] printSeme = _param.getSemesterCd();
            for (int i = 0; i < printSeme.length; i++) {
                final Semester semester = _param.getSemester(printSeme[i]);
                if (null != semester) {
                    Attendance.load(db2, _param, studentList, printSeme[i], semester._sdate, semester._edate);
                    SubclassAttendance.load(db2, _param, studentList, printSeme[i], semester._sdate, semester._edate);
                }
            }

            Student.setHreportremarkCommunication(_param, db2, studentList);
        }
    }

    private static Student getStudent(final List studentList, final String code) {
        if (code == null) {
            return null;
        }
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (code.equals(student._schregno)) {
                return student;
            }
        }
        return null;
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
        final String _staffName1;
        final String _staffName2;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
        final Map _subclassMap;
        private Map _communication;

        Student(final String schregno, final String name, final String hrName, final String staffName1, final String staffName2, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffName1 = staffName1;
            _staffName2 = staffName2;
            _attendno = attendno;
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _attendMap = new TreeMap();
            _subclassMap = new LinkedMap();
            _communication = new HashMap();
        }

        SubClass getSubClass(final String subclasscd) {
            return (SubClass) _subclassMap.get(subclasscd);
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  W1.SCHREGNO");
            stb.append("            ,W1.SEMESTER ");
            stb.append("            ,W7.NAME ");
            stb.append("            ,W6.HR_NAME ");
            stb.append("            ,W8.STAFFNAME AS STAFFNAME1 ");
            stb.append("            ,W11.STAFFNAME AS STAFFNAME2 ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST W11 ON W11.STAFFCD = W6.TR_CD2 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         W1.ATTENDNO ");
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
                    final String staffname1 = StringUtils.defaultString(rs.getString("STAFFNAME1"));
                    final String staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                    students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname1, staffname2, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            PreparedStatement ps = null;

            final StringBuffer sql2 = new StringBuffer();
            sql2.append(" SELECT SEMESTER, COMMUNICATION ");
            sql2.append(" FROM HREPORTREMARK_DAT ");
            sql2.append(" WHERE YEAR = '" + param._year + "' ");
            sql2.append("   AND SEMESTER <= '" + param._knjSchoolMst._semesterDiv + "' ");
            sql2.append("   AND SCHREGNO = ? ");
            sql2.append(" ORDER BY SEMESTER ");

            try {
                ps = db2.prepareStatement(sql2.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._communication.put(rs.getString("SEMESTER"), rs.getString("COMMUNICATION"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class Attendance {
        final int _lesson;    //授業日数
        final int _mLesson;   //出席しなければならない日数
        final int _suspend;   //忌引出停日数1
        final int _mourning;  //忌引出停日数2
        final int _absent;    //欠席日数
        final int _present;   //出席日数
        final int _late;      //遅刻
        final int _early;     //早退
        final int _transDays; //

        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final String key,
                final String sdate,
                final String edate0
        ) {
            if (null == sdate || null == edate0 || sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = edate0.compareTo(param._date) > 0 ? param._date : edate0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("grade", param._grade);
                param._attendParamMap.put("hrClass", param._gradeHrclass.substring(2));
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY"),
                                rs.getInt("TRANSFER_DATE")
                        );
                        student._attendMap.put(key, attendance);
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
    private static class SubClass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        final Map _attendMap;

        SubClass(
                final SubclassMst mst
        ) {
            _mst = mst;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
        }

        public Score getScore(final String testcd) {
            if (null == testcd) {
                return null;
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
        	if (o instanceof SubClass) {
        		return _mst.compareTo(((SubClass) o)._mst);
        	}
        	return -1;
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
                final List studentList,
                final String key,
                final String sdate,
                final String edate0) {
            if (null == sdate || null == edate0 || sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = edate0.compareTo(param._date) > 0 ? param._date : edate0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
                param._attendParamMap.put("grade", param._gradeHrclass.substring(0, 2));
                param._attendParamMap.put("hrClass", param._gradeHrclass.substring(2));
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        sdate,
                        edate,
                        param._attendParamMap
                );

                log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if (!(Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd < Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            continue;
                        }

                        final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                        final BigDecimal sick = rs.getBigDecimal("SICK2");

                        final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, sick);

                        if (null == student._subclassMap.get(subclasscd)) {
                            continue;
                        }
                        final SubClass subClass = student.getSubClass(subclasscd);
                        subClass._attendMap.put(key, subclassAttendance);
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /**
     * 成績
     */
    private static class Score {
        final String _score;
        final String _scoreKansan;
        final String _avg;
        final String _replacemoto;

        Score(
                final String score,
                final String scoreKansan,
                final String avg,
                final String replacemoto
        ) {
            _score = score;
            _scoreKansan = scoreKansan;
            _avg = avg;
            _replacemoto = replacemoto;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param);
                log.info(" subclass query start." + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" subclass query end.");

                while (rs.next()) {
                    final Student student = getStudent(studentList, rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Score score = new Score(
                            rs.getString("SCORE"),
                            rs.getString("SCORE_KANSAN"),
                            rs.getString("AVG"),
                            rs.getString("REPLACEMOTO")
                    );

                    final String subclasscd;
                    subclasscd = rs.getString("SUBCLASSCD");
                    if (null == student._subclassMap.get(subclasscd)) {
                        final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subClass);
                    }
                    if (null == testcd) {
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final SubClass subClass = student.getSubClass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
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

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
                stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,W3.AVG ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
            stb.append(param._form._stbtestcd.toString());
            stb.append("     ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append(" ) ");

            //メイン表
            stb.append(" ,UNIONDAT AS ( ");
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,VALUE(T11_2.ASSESSLEVEL, T11.ASSESSLEVEL) AS SCORE_KANSAN ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");
            // 評定マスタ100段階 -> 10段階変換
            stb.append("  LEFT JOIN ASSESS_COURSE_MST T11_2 ON T11_2.ASSESSCD = '2' ");
            stb.append("      AND T11_2.COURSECD = SCH.COURSECD ");
            stb.append("      AND T11_2.MAJORCD = SCH.MAJORCD ");
            stb.append("      AND T11_2.COURSECODE = SCH.COURSECODE ");
            stb.append("      AND T3.SCORE BETWEEN T11_2.ASSESSLOW AND T11_2.ASSESSHIGH ");
            stb.append("  LEFT JOIN ASSESS_MST T11 ON T11.ASSESSCD = '2' AND T3.SCORE BETWEEN T11.ASSESSLOW AND T11.ASSESSHIGH ");

            //成績不振科目データの表
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" UNION ");
            stb.append(" SELECT  TX1.CLASSCD || '-' || TX1.SCHOOL_KIND || '-' || TX1.CURRICULUM_CD || '-' || TX1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("        ,TX1.SCHREGNO ");
            stb.append("        ,TX1.SEMESTER ");
            stb.append("        ,TX1.SEMESTER || TX1.TESTKINDCD || TX1.TESTITEMCD || TX1.SCORE_DIV AS TESTCD ");
            stb.append("        ,TX1.SCORE ");
            stb.append("        ,T11.ASSESSLEVEL AS SCORE_KANSAN ");
            stb.append("        ,TX1.AVG ");
            stb.append("        ,0 AS REPLACEMOTO ");
            stb.append("        ,'0' AS PRINT_FLG");
            stb.append(" FROM RECORD_RANK_SDIV_DAT TX1");
            // 評定マスタ100段階 -> 10段階変換
            stb.append("  LEFT JOIN ASSESS_MST T11 ON T11.ASSESSCD = '2' AND TX1.SCORE BETWEEN T11.ASSESSLOW AND T11.ASSESSHIGH ");
            stb.append(" WHERE ");
            stb.append("   TX1.YEAR = '" + param._year + "'");
            stb.append("   " + param._form.setStbTestCd("TX1").toString() + " ");
            stb.append("   " + param._form.setStbSubClassCd("TX1").toString() + " ");
            stb.append(" ) SELECT ");
            stb.append("     * ");
            stb.append(" FROM UNIONDAT ");
            stb.append(" ORDER BY ");
            stb.append("     SCHREGNO, ");
            stb.append("     SEMESTER, ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _sdate = sdate;
            _edate = edate;
        }
    }

    private static class SubclassMst implements Comparable {
    	final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final String _isAttendSubclass;
        final int _classShoworder3;
        final int _subclassShoworder3;
        final List _attendSubclassCds = new ArrayList();
        public SubclassMst(final String classcd, String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname, final String isAttendSubclass, final int classShoworder3, final int subclassShoworder3) {
        	_classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isAttendSubclass = isAttendSubclass;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
        }

        public int compareTo(final Object o) {
        	if (o instanceof SubclassMst) {
                final SubclassMst mst = (SubclassMst) o;
                int rtn;
                rtn = _classShoworder3 - mst._classShoworder3;
                if (0 != rtn) { return rtn; }
                if (null == _classcd && null == mst._classcd) {
                    return 0;
                } else if (null == _classcd) {
                    return 1;
                } else if (null == mst._classcd) {
                    return -1;
                }
                rtn = _classcd.compareTo(mst._classcd);
                if (0 != rtn) { return rtn; }
                rtn = _subclassShoworder3 - mst._subclassShoworder3;
                if (0 != rtn) { return rtn; }
                if (null == _subclasscd && null == mst._subclasscd) {
                    return 0;
                } else if (null == _subclasscd) {
                    return 1;
                } else if (null == mst._subclasscd) {
                    return -1;
                }
                return _subclasscd.compareTo(mst._subclasscd);
        	}
        	return -1;
        }
    }

    private static class Form {

        final String[] _testcds_J = new String[] {"1990008", "2990008", "3990008", TESTCD_GAKUNEN_HYOKA};

        String[] _testcds;
        private Param _param;

        private StringBuffer _stbtestcd = new StringBuffer();

        private StringBuffer setStbTestCd(String tblAssignName) {
            StringBuffer stbtestcdbuf = new StringBuffer();
            stbtestcdbuf.append(" AND (");
            stbtestcdbuf.append(makeStbTestCdCondition(tblAssignName));
            stbtestcdbuf.append(") ");
            return stbtestcdbuf;
        }

        private StringBuffer setStbSubClassCd(String tblAssignName) {
            StringBuffer stbtestcdbuf = new StringBuffer();
            stbtestcdbuf.append(" AND (");
            stbtestcdbuf.append(makeStbSubClassCdCondition(tblAssignName));
            stbtestcdbuf.append(") ");
            return stbtestcdbuf;
        }

        private String makeStbSubClassCdCondition(String tblAssignName) {
        	String retStr;
        	retStr = " " + tblAssignName +".SUBCLASSCD NOT IN ('333333', '555555', '777777') ";
        	return retStr;
        }
        private String makeStbTestCdCondition(String tblAssignName) {
            StringBuffer stbtestcdbuf = new StringBuffer();
            String testcdor = "";
            for (int i = 0; i < _testcds.length; i++) {
                final String testcd = _testcds[i];
                if (null == testcd) {
                    continue;
                }
                final String seme = testcd.substring(0, 1);
                final String kind = testcd.substring(1, 3);
                final String item = testcd.substring(3, 5);
                final String sdiv = testcd.substring(5);
                if (seme.compareTo(_param._semester) <= 0 || SEMEALL.equals(seme) && null != _param._lastsemester && _param._lastsemester.equals(_param._semester)) {
                	stbtestcdbuf.append(testcdor);
                	stbtestcdbuf.append(" " + tblAssignName + ".SEMESTER = '" + seme + "' AND " + tblAssignName + ".TESTKINDCD = '" + kind + "' AND " + tblAssignName + ".TESTITEMCD = '" + item + "' AND " + tblAssignName + ".SCORE_DIV = '" + sdiv + "' ");
                    testcdor = " OR ";
                }
            }
            return stbtestcdbuf.toString();
        }

        void print(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        	final String form;
        	if ("J".equals(_param._schoolkind)) {
        		if (_param.isNotLastSemester()) {
        			form = "KNJD185F_1.frm";
        		} else {
        			form = "KNJD185F_2.frm";
        		}
        	} else {
        		return;
        	}
        	log.debug("form:"+form);
            svf.VrSetForm(form, 1);

            printHeader(db2, svf, student);
            printAttendance(svf, student);
            printCommunication(svf, student);
            printScore(svf, student);
            svf.VrEndPage();
        }

        void printAttendance(final Vrw32alp svf, final Student student) {
            final String[] seme = _param.getSemesterCd();
            final int lastrow = 4;
            for (int i = 0; i < lastrow; i++) {
            	if (seme.length <= i) {
            		continue;
            	}
                final String semes = seme[i];
                final Semester semester = _param.getSemester(semes);
                if (semester == null) {
                	continue;
                }
                if (i < Integer.parseInt(_param._lastsemester)) {
                	//(各学校で決まっている)各学期の名称を設定
          	        svf.VrsOutn("SEMESTER2", i+1, semester._semestername);
                } else if (i + 1 == lastrow) {
                	//学年を設定
          	        svf.VrsOutn("SEMESTER2", i+1, semester._semestername);
                } else {
                	continue;
                }
                //指定学期以降のデータは出力しない。
                if (!"9".equals(semes) && semes.compareTo(_param._semester) > 0) {
                	continue;
                }
                if ("9".equals(semes) && !_param._lastsemester.equals(_param._semester)) {
                	continue;
                }

                final Attendance att = (Attendance) student._attendMap.get(semes);
                if (null == att) {
                    continue;
                }
                final int line = "9".equals(semes) ? lastrow : i + 1;
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));       // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引等日数
                svf.VrsOutn("MUST", line, String.valueOf(att._mLesson));        // 出席すべき日数
                svf.VrsOutn("PRESENT", line, String.valueOf(att._present));     // 出席日数
                svf.VrsOutn("NOTICE", line, String.valueOf(att._absent));       // 欠席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late));           // 遅刻回数
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));         // 早退回数
            }
        }


        void printCommunication(final Vrw32alp svf, final Student student) {
            // 通信欄
        	final int lpcnt = student._communication.size() < Integer.parseInt(_param._semester) ? student._communication.size() : Integer.parseInt(_param._semester);
        	for (int i = 0;i < Integer.parseInt(_param._lastsemester);i++) {
        	    svf.VrsOutn("SEMESTER3", i+1, "(" + String.valueOf(i+1) + "学期)");
        	    //項目のタイトルは出力して、データ部は"データがあれば"出力
        	    if (i < lpcnt) {
                    final List commComment = KNJ_EditKinsoku.getTokenList((String) student._communication.get(String.valueOf(i + 1)), 60, 4);
                    for (int j = 0;j < commComment.size(); j++) {
                        svf.VrsOutn("COMM" + String.valueOf(j + 1), i+1, (String) commComment.get(j));
                    }
        	    }
        	}
        }

        void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        	final String nendostr = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
        	//タイトル
            svf.VrsOut("TITLE", nendostr + "年度 第" + String.valueOf(Integer.parseInt(_param._gradecd)) + "学年成績通知表"); // タイトル

            //日付(中学のみ?)
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._prtdate)); //日付

            svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            String prncplfield = getMS932ByteLength(_param._principalName) > 20 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + prncplfield, _param._principalName); // 校長

            if (student._staffName2 == null || "".equals(student._staffName2)) {
            	final String teacherfield1 = getMS932ByteLength(student._staffName1) > 20 ? "2" : "1";
                svf.VrsOut("TEACHER_NAME1_" + teacherfield1, student._staffName1); // 担任1
            } else {
            	final String teacherfield2 = getMS932ByteLength(student._staffName1) > 20 ? "2" : "1";
                svf.VrsOut("TEACHER_NAME2_" + teacherfield2, student._staffName1); // 担任1
            	final String teacherfield3 = getMS932ByteLength(student._staffName2) > 20 ? "2" : "1";
                svf.VrsOut("TEACHER_NAME3_" + teacherfield3, student._staffName2); // 担任2
            }

            //ヘッダの名前欄(中学ではクラス名不要)
            svf.VrsOut("HR_NAME", student._hrName + " " + (StringUtils.isBlank(student._attendno) ? "" : student._attendno + "番")); // クラス名
            svf.VrsOut("NAME", student._name); // 氏名

            //学年末のみ
            if (_param.isNotLastSemester()) {
                svf.VrsOut("HR_NAME2", student._hrName + " " + (StringUtils.isBlank(student._attendno) ? "" : student._attendno + "番")); //クラス名+出席番号
                svf.VrsOut("NAME2", student._name); // 氏名
            }
        }

        void printScoreTableTitle(final Vrw32alp svf, final String subclasscd, final SubClass subClass, final int brkcnt, final int subclscnt) {
            //科目名称を出力する。
        	if (subclasscd.indexOf("999999") < 0) {
                if (subclscnt < brkcnt+1 && subClass._mst._subclassname != null) {
                    svf.VrsOut("CLASS_NAME" + subclscnt, subClass._mst._subclassname); // 科目名
                }
        	}
        }

        void printScore(final Vrw32alp svf, final Student student) {
            int count = 0;
            int brkcnt = 10;
            int lastrow = 4;

            //表の左側の項目名称を設定する。
            final String[] printSeme = _param.getPrintSemester();
            for (int kk = 0;kk < lastrow;kk++) {
                final Semester semester = _param.getSemester(printSeme[kk]);
                if (semester != null) {
            	    svf.VrsOutn("SEMESTER1", kk+1, semester._semestername);
                } else {
                    //学年末として最終行に出力
                	if (kk+1 == lastrow) {
                	    svf.VrsOutn("SEMESTER1", lastrow, printSeme[kk]);
                	}
                }
            }

            final List subclassList = new ArrayList(student._subclassMap.values());
            Collections.sort(subclassList);

            int subclscnt = 1;
            String bksubclscd = "";
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                if (count >= brkcnt) {
                    break;
                }
                final SubClass subClass = (SubClass) it.next();
                if (null == subClass) {
                    continue;
                }
                if ("1".equals(subClass._mst._isAttendSubclass)) {
                    // 元科目を表示しない
                    continue;
                }
                //subclasscdが、教科コード-校種-カリキュラムコード-科目コードで、学期でソートしているので、
                //その該当学期の箇所に出力する
                log.debug(" subclasscd = " + subClass._mst._subclasscd);
                String[] subclspart = StringUtils.split(subClass._mst._subclasscd, "-");
                String[] bksubclspart = StringUtils.split(bksubclscd, "-");
                //中学では、総合計の出力が無いので、除外する。
                if (student.getSubClass(SUBCLASSCD999999) == subClass) {
                    continue;
                }
                //処理上入っている無効データ(SUBCLASSCDはあるけど、科目名称が紐づかない)を除外する。
                if (subClass._mst._subclasscd == null && subClass._mst._subclassname == null) {
                	continue;
                }
                if (!"".equals(bksubclscd)) {
                	if (!bksubclspart[3].equals(subclspart[3])) {
                        subclscnt++;
                    	printScoreTableTitle(svf, subClass._mst._subclasscd, subClass, brkcnt, subclscnt);
                    }
                } else {
                	printScoreTableTitle(svf, subClass._mst._subclasscd, subClass, brkcnt, subclscnt);
                }
                for (int i = 0; i < _testcds.length; i++) {
                    final Score score = subClass.getScore(_testcds[i]);
                	String datSemester = _testcds[i].substring(0,1);
                    if (score != null) {
               	        if (!"9".equals(datSemester)) {
                            svf.VrsOutn("VALUE"+subclscnt, i+1, score._scoreKansan);
               	        } else {
                            svf.VrsOutn("VALUE"+subclscnt, lastrow, score._scoreKansan);
               	        }
                    }
                }
                bksubclscd = subClass._mst._subclasscd;
            }
        }

        private static int getMS932ByteLength(final String s) {
            return KNJ_EditEdit.getMS932ByteLength(s);
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 69005 $ $Date: 2019-07-31 16:33:23 +0900 (水, 31 7 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _lastsemester;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        final String _prtdate;
        final String _printHyoshi;
        
        final String _gradecd;

        final String _schoolkind;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final Form _form;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMst;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final Map _attendParamMap;

        private KNJSchoolMst _knjSchoolMst;

        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _principalNameSpc;
        private String _hrJobName;

        final String _documentRoot;
        final String _imagePath;
        final String _extension;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _prtdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("PRTDATE"));
            _printHyoshi = request.getParameter("PRINT_HYOSHI");

            _semesterMap = loadSemester(db2, _year, _grade);
            _lastsemester = getLastSemester();
            setCertifSchoolDat(db2);
            _form = new Form();
            _form._param = this;
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _definecode = createDefineCode(db2);
            setSubclassMst(db2);

            _schoolkind = getSchoolKind(db2);
            _gradecd =  getGradeCd(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _form._testcds = _form._testcds_J;

            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath    = request.getParameter("IMAGEPATH");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
        }

        public String getImagePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO_J.jpg";
            if (new java.io.File(path).exists()) {
                return path;
            }
            return null;
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        private boolean isNotLastSemester() {
        	return Integer.parseInt(_semester) < Integer.parseInt(_lastsemester);
        }

        private String getLastSemester() {
        	int semeid = 0;
        	String retStr = "";
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
        		String kit = (String)it.next();
        		Semester semobj = (Semester)_semesterMap.get(kit);
        		if (!"9".equals(semobj._semester) && Integer.parseInt(semobj._semester) > semeid) {
        			semeid = Integer.parseInt(semobj._semester);
        			retStr = semobj._semester;
        		}
        	}
        	return retStr;
        }

        private String getSchoolKind(final DB2UDB db2) {
        	String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
        	String retstr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retstr = rs.getString("SCHOOL_KIND");
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        	return retstr;
        }

        private String getGradeCd(final DB2UDB db2) {
        	String sql = "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
        	String retstr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retstr = rs.getString("GRADE_CD");
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        	return retstr;
        }

        private String[] getSemesterCd() {
        	//3学期想定なのは、帳票が3学期ベースのため。このデータにDB情報を合わせて利用する。
          return new String[] {"1", "2", "3", "9"};
        }

        private String[] getPrintSemester() {
        	//3学期想定なのは、帳票が3学期ベースのため。このデータにDB情報を合わせて利用する。
            return new String[] {"1", "2", "3", "学年"};
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            return definecode;
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
                        + "   AND GRADE = '" + grade + "' "
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

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ");
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
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
            String spc = "";
            for (int i = 0; i < _principalName.length(); i++) {
                final char c = _principalName.charAt(i);
                if (c == ' ' || c == '　') {
                    spc += String.valueOf(c);
                } else {
                    break;
                }
            }
            _principalNameSpc = spc;
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null, null, null, 999999, 999999);
            }
            return (SubclassMst) _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMst = new HashMap();
            try {
                String sql = "";

                sql += " WITH ATTEND_SUBCLASS AS ( ";
                sql += " SELECT DISTINCT ";
                sql += " T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ";
                sql += " WHERE T1.YEAR = '" + _year + "' ";
                sql += " ) ";

                sql += " SELECT ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN T3.ATTEND_SUBCLASSCD IS NOT NULL THEN 1 END AS IS_ATTEND_SUBCLASS, ";
                sql += " VALUE(T2.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3, ";
                sql += " T4.ATTEND_CLASSCD || '-' || T4.ATTEND_SCHOOL_KIND || '-' || T4.ATTEND_CURRICULUM_CD || '-' || T4.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN ATTEND_SUBCLASS T3 ON T3.ATTEND_SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ON T4.YEAR = '" + _year + "' AND T4.COMBINED_CLASSCD || '-' || T4.COMBINED_SCHOOL_KIND || '-' || T4.COMBINED_CURRICULUM_CD || '-' || T4.COMBINED_SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
                log.debug("sql:"+sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == _subclassMst.get(subclasscd)) {
                        final int classShoworder3 = rs.getInt("CLASS_SHOWORDER3");
                        final int subclassShoworder3 = rs.getInt("SUBCLASS_SHOWORDER3");
                        final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), subclasscd, rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), rs.getString("IS_ATTEND_SUBCLASS"), classShoworder3, subclassShoworder3);
                        _subclassMst.put(subclasscd, mst);
                    }
                    if (null != rs.getString("ATTEND_SUBCLASSCD")) {
                        final SubclassMst mst = (SubclassMst) _subclassMst.get(subclasscd);
                        mst._attendSubclassCds.add(rs.getString("ATTEND_SUBCLASSCD"));
                        log.info(" " + subclasscd + " <-" + rs.getString("ATTEND_SUBCLASSCD"));
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}
