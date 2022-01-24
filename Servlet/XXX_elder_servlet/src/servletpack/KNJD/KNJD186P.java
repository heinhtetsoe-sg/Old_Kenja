// kanji=漢字
/*
 * $Id: 95404e87e6c77a7a6115b809402c50600d2cde70 $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;


/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186P {
    private static final Log log = LogFactory.getLog(KNJD186P.class);

    private static final String SEME1 = "1";
    private static final String SEME2 = "2";
    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";
    private static final String KANENDO_GRADE01 = "01";
    private static final String KANENDO_GRADE02 = "02";

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
            Param param = createParam(request, db2);

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

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);
            param._form.init(param.getRecordMockOrderSdivDat(student._regd._grade, student._regd._coursecd, student._regd._majorcd));
            param._form.print(db2, ipdf, student);
        }
        _hasData = true;
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

    private static String mkString(final List textList, final String comma1) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (final Iterator it = textList.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            stb.append(comma).append(text);
            comma = comma1;
        }
        return stb.toString();
    }

    private static void loginfo(final Param param, final Object o) {
        if (param._isOutputDebug) {
            log.info(o);
        }
    }

    private static BigDecimal getIntlistSum(final List list) {
        BigDecimal bd = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Integer num = (Integer) it.next();
            if (null == bd) {
                bd = new BigDecimal(0);
            }
            bd = bd.add(new BigDecimal(num.intValue()));
        }
        return bd;
    }

    private static int getIntlistLength(final List list) {
        int length = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Integer num = (Integer) it.next();
            if (null != num) {
                length += 1;
            }
        }
        return length;
    }

    private static String getIntlistAverage(final List list, final Param param) {
        final int length = getIntlistLength(list);
        final BigDecimal sum = getIntlistSum(list);
        if (length <= 0) {
            return null;
        }
        BigDecimal avg = sum.divide(new BigDecimal(length), 1, BigDecimal.ROUND_HALF_UP);
        if (param._isOutputDebug) {
        	log.info(" average (sum " + sum +", length = " + length + ") = " + sum.divide(new BigDecimal(length), 4, BigDecimal.ROUND_HALF_UP) + " = " + avg);
        }
        return avg.toString();
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
        private static class Regd {
            final String _hrName;
            final String _staffName;
            final String _grade;
            final String _coursecd;
            final String _majorcd;
            final String _course;
            final String _majorname;
            final String _coursecodename;
            final String _attendno;
            final String _hrClassName1;

            Regd(final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String coursecodename, final String hrClassName1) {
                _hrName = hrName;
                _staffName = staffName;
                _attendno = attendno;
                _grade = grade;
                _coursecd = coursecd;
                _majorcd = majorcd;
                _course = course;
                _majorname = majorname;
                _coursecodename = coursecodename;
                _hrClassName1 = hrClassName1;
            }

            public String getPrintAttendno() {
                return (NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno)) + "番";
            }
        }

        final String _schregno;
        final String _name;
        final Regd _regd;
        final Map _kanendoGradeRegdMap; // 過年度在籍データ
        final Map _attendMap;
        final Map _attendRemarkMap;
        final Map _subclassMap;
        final Map _kanendoGradeSubclassMap; // 過年度科目
        final String _entyear;
        private String _specialactremark;
        final Map _semesCommitteeListMap = new HashMap();
        final Map _semesClubListMap = new HashMap();

        Student(final String schregno, final String name, final Regd regd, final String entyear) {
            _schregno = schregno;
            _name = name;
            _regd = regd;
            _entyear = entyear;
            _attendMap = new TreeMap();
            _attendRemarkMap = new TreeMap();
            _subclassMap = new TreeMap();
            _kanendoGradeRegdMap = new TreeMap();
            _kanendoGradeSubclassMap = new TreeMap();
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

        static SubClass getSubClass(final Map subclassMap, final String subclasscd) {
            if (null == subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubClass(new SubclassMst(new ClassMst(classcd, "", "", 999999), subclasscd, null, null, 999999, false, false));
            }
            return (SubClass) subclassMap.get(subclasscd);
        }

        public String getHrAttendNo(final Param param) {
            try {
                final String grade = String.valueOf(Integer.parseInt(param._gradeHrclass.substring(0, 2)));
                final String hrclass = String.valueOf(Integer.parseInt(param._gradeHrclass.substring(2)));
                final String attendno = String.valueOf(Integer.parseInt(_regd._attendno));
                return grade + "-" + hrclass + "-" + attendno + " " + StringUtils.defaultString(_name);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final String sql = getStudentSql(param, param._grade);
            if (param._isOutputDebug) {
                log.info(" student sql = " + sql);
            }

            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final Regd regd = new Regd(rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("COURSECODENAME"), rs.getString("HR_CLASS_NAME1"));
                    students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), regd, rs.getString("ENT_YEAR")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            final Form form = param._form;

            final Map courseStudentsMap = new HashMap();
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String key = student._regd._grade + "-" + student._regd._coursecd + "-" + student._regd._majorcd;
                getMappedList(courseStudentsMap, key).add(student);
            }

            for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final List studentList = (List) courseStudentsMap.get(key);
                final String[] split = StringUtils.split(key, "-");

                final List recordMockOrderSdivDatList = param.getRecordMockOrderSdivDat(split[0], split[1], split[2]);

                form.init(recordMockOrderSdivDatList);

                for (int i = 0; i < form._attendRanges.length; i++) {
                    final DateRange range = form._attendRanges[i];
                    Attendance.load(db2, param, studentList, range);
                }

                for (int i = 0; i < form._attendRemarkRanges.length; i++) {
                    final DateRange range = form._attendRemarkRanges[i];
                    Attendance.loadRemark(db2, param, studentList, range);
                }

                String testcdor = "";
                final StringBuffer stbtestcd = new StringBuffer();
                stbtestcd.append(" AND (");
                final List testcds = new ArrayList(form._testcds);
                testcds.add(TESTCD_GAKUNEN_HYOTEI);

                for (int i = 0; i < testcds.size(); i++) {
                    final String testcd = (String) testcds.get(i);
                    if (null == testcd) {
                        continue;
                    }
                    final String seme = testcd.substring(0, 1);
                    final String kind = testcd.substring(1, 3);
                    final String item = testcd.substring(3, 5);
                    final String sdiv = testcd.substring(5);
                    if (seme.compareTo(param._semester) <= 0 || TESTCD_GAKUNEN_HYOTEI.equals(testcd)) {
                        stbtestcd.append(testcdor);
                        stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                        testcdor = " OR ";
                    }
                }
                stbtestcd.append(") ");
                Score.load(db2, param, studentList, stbtestcd);

                if (param._kanendoGradeList.size() > 0) {
                    Student.setRegd(db2, param._kanendoGradeList, param, studentList);
                    Score.loadKanendoStudyrecDat(db2, param._kanendoGradeList, param, studentList);
                }

//                Student.setTotalStudy(db2, param, studentList);
                Student.setHreportremarkCommunication(param, db2, studentList);
                Student.setClub(db2, param, studentList);
                Student.setCommittee(db2, param, studentList);
            }

            return students;
        }

        private static String getStudentSql(final Param param, final String grade) {
            final boolean isKanendo = !param._grade.equals(grade);
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  W1.SCHREGNO");
            stb.append("            ,W1.SEMESTER ");
            stb.append("            ,W7.NAME ");
            stb.append("            ,W6.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W10.COURSECODENAME ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + grade + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = W1.COURSECODE ");
            if (isKanendo) {
                stb.append("     WHERE   W1.GRADE = '" + grade + "' ");
                stb.append("         AND W1.SCHREGNO = ? ");
                stb.append("         AND W1.YEAR = (SELECT MAX(YEAR) FROM SCHREG_REGD_DAT I1 WHERE I1.SCHREGNO = W1.SCHREGNO AND I1.GRADE = W1.GRADE) ");
                stb.append("         AND W1.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT I2 WHERE I2.SCHREGNO = W1.SCHREGNO AND I2.YEAR = W1.YEAR) ");
            } else {
                stb.append("     WHERE   W1.YEAR = '" + param._ctrlYear + "' ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("     AND W1.SEMESTER = '" + param._ctrlSemester + "' ");
                } else {
                    stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                    stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                    stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
                }
                stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
                stb.append("         AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
                stb.append("     ORDER BY ");
                stb.append("         W1.ATTENDNO ");
            }
            return stb.toString();
        }

        private static void setRegd(final DB2UDB db2, final List kanendoGradeList, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                for (final Iterator git = kanendoGradeList.iterator(); git.hasNext();) {
                    final String grade = (String) git.next();

                    final String sql = getStudentSql(param, grade);

                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                            final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                            final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                            final Regd regd = new Regd(rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("COURSECODENAME"), rs.getString("HR_CLASS_NAME1"));
                            student._kanendoGradeRegdMap.put(grade, regd);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SPECIALACTREMARK ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
            stb.append("   AND SEMESTER = '" + SEMEALL + "' ");
            stb.append("   AND SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._specialactremark = rs.getString("SPECIALACTREMARK");
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

        public static void setCommittee(final DB2UDB db2, final Param param,
                final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD, ");
            stb.append("     T1.CHARGENAME, ");
            stb.append("     T2.COMMITTEENAME ");
            stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.COMMITTEE_FLG IN ('1', '2') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        String name = null;
                        name = rs.getString("COMMITTEENAME");
                        if (StringUtils.isBlank(name)) {
                            continue;
                        }
                        getMappedList(student._semesCommitteeListMap, semester).add(name);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static void setClub(final DB2UDB db2, final Param param,
                final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     TSEM.SEMESTER, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME, ");
            stb.append("     CASE WHEN T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
            stb.append("                       VALUE(T1.EDATE, '9999-12-31') BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
            stb.append("                       TSEM.SDATE <= T1.SDATE AND T1.EDATE <= TSEM.EDATE OR ");
            stb.append("                       T1.SDATE <= TSEM.SDATE AND TSEM.EDATE <=  VALUE(T1.EDATE, TSEM.EDATE) THEN 1 END AS FLG ");
            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND TSEM.SEMESTER <> '9' ");
            stb.append("     AND TSEM.SEMESTER <= '" + param._semester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String clubname = rs.getString("CLUBNAME");
                        final String flg = rs.getString("FLG");

                        if (!"1".equals(flg) || StringUtils.isBlank(clubname)) {
                            continue;
                        }
                        getMappedList(student._semesClubListMap, semester).add(clubname);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String addLine(final String source, final String data) {
            if (StringUtils.isBlank(source)) {
                return data;
            }
            if (StringUtils.isBlank(data)) {
                return source;
            }
            return source + "\n" + data;
        }
    }

    private static class Attendance {

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _abroad;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        DateRange _dateRange;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int abroad,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._semester,
                        dateRange._sdate,
                        dateRange._edate,
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
                                rs.getInt("TRANSFER_DATE"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY")
                        );
                        student._attendMap.put(dateRange._key, attendance);
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
        

        private static void loadRemark(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance remark = " + dateRange);
            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._ctrlYear, dateRange._sdate, dateRange._edate);
            loadRemark(db2, param, (String) hasuuMap.get("attendSemesInState"), studentList, dateRange);
        }

        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List studentList, final DateRange dateRange) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.MONTH, T1.SEMESTER, T1.SCHREGNO, T1.REMARK1 ");
                stb.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   T1.COPYCD = '0' ");
                stb.append("   AND T1.YEAR = '" + param._ctrlYear + "' ");
                stb.append("   AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + " ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append("   AND T1.REMARK1 IS NOT NULL ");
                stb.append(" ORDER BY T1.MONTH, T1.SEMESTER ");

                if (param._isOutputDebug) {
                	log.info(" dateRange = " + dateRange + " /  remark sql = " + stb.toString());
                }
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    String comma = "";
                    final StringBuffer remark = new StringBuffer();
                    while (rs.next()) {
                        remark.append(comma).append(rs.getString("REMARK1"));
                        comma = "、";
                    }
                    if (remark.length() != 0) {
                        student._attendRemarkMap.put(dateRange._key, remark.toString());
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

        public int compareTo(final Object o) {
            final SubClass subclass = (SubClass) o;
            return _mst.compareTo(subclass._mst);
        }

        public String toString() {
            return "SubClass(" + _mst.toString() + ", score = " + _scoreMap + ")";
        }
    }

    /**
     * 成績
     */
    private static class Score {
        final String _testcd;
        final String _score;
        final String _assessLevel;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;
        final String _karihyotei;
        final String _replacemoto;
        final String _compCredit;
        final String _getCredit;
        final String _provFlg;

        Score(
                final String testcd,
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
            _testcd = testcd;
            _score = score;
            _assessLevel = assessLevel;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _provFlg = provFlg;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public String getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public String getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1) {
                return false;
            }
            return true;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final StringBuffer stbtestcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param, param._ctrlYear, stbtestcd);
                loginfo(param, " sql score = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String testcd = rs.getString("TESTCD");

                        final Rank gradeRank = new Rank(rs.getString("GRADE_AVG"));
                        final Rank hrRank = new Rank(rs.getString("HR_AVG"));
                        final Rank courseRank = new Rank(rs.getString("COURSE_AVG"));
                        final Rank majorRank = new Rank(rs.getString("MAJOR_AVG"));

                        final Score score = new Score(
                                testcd,
                                rs.getString("SCORE"),
                                rs.getString("ASSESS_LEVEL"),
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
                            if (SUBCLASSCD999999.equals(subclasscd)) {
                            	final SubClass subClass = new SubClass(param.createSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subClass);
                            } else {
                            	final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                            	student._subclassMap.put(subclasscd, subClass);
                            }
                        }
                        if (null == testcd) {
                            continue;
                        }
                        // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                        final SubClass subClass = Student.getSubClass(student._subclassMap, subclasscd);
                        subClass._scoreMap.put(testcd, score);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private static String sqlScore(final Param param, final  String year, final StringBuffer stbtestcd) {

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
            stb.append("         AND W1.SCHREGNO = ? ");
            //stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
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

            // クラス平均作成
            stb.append(",CLASS_AVG9 AS( ");
            stb.append("    SELECT ");
            stb.append("        W3.SEMESTER, W3.TESTKINDCD, W3.TESTITEMCD, W3.SCORE_DIV, ");
            stb.append("        REGD.GRADE, ");
            stb.append("        REGD.HR_CLASS, ");
            stb.append("        avg(W3.AVG) AS HR_AVG ");
            stb.append("    FROM ");
            stb.append("        RECORD_RANK_SDIV_DAT W3 ");
            stb.append("        INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = W3.YEAR ");
            stb.append("                                       AND REGD.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE ");
            stb.append("         REGD.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W3.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            stb.append(stbtestcd.toString());
            stb.append("    GROUP BY ");
            stb.append("        W3.SEMESTER, W3.TESTKINDCD, W3.TESTITEMCD, W3.SCORE_DIV, ");
            stb.append("        REGD.GRADE, REGD.HR_CLASS ");
            stb.append("     )");

            // コース平均作成
            stb.append(",COURSE_AVG9 AS( ");
            stb.append("    SELECT ");
            stb.append("        W3.SEMESTER, W3.TESTKINDCD, W3.TESTITEMCD, W3.SCORE_DIV, ");
            stb.append("        REGD.GRADE, REGD.COURSECD, ");
            stb.append("        REGD.MAJORCD, ");
            stb.append("        REGD.COURSECODE, ");
            stb.append("        avg(W3.AVG) AS COURSE_AVG ");
            stb.append("    FROM ");
            stb.append("        RECORD_RANK_SDIV_DAT W3 ");
            stb.append("        INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = W3.YEAR ");
            stb.append("                                       AND REGD.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE ");
            stb.append("         REGD.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W3.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            stb.append(stbtestcd.toString());
            stb.append("    GROUP BY ");
            stb.append("        W3.SEMESTER, W3.TESTKINDCD, W3.TESTITEMCD, W3.SCORE_DIV, ");
            stb.append("        REGD.GRADE, REGD.COURSECD ,REGD.MAJORCD ,REGD.COURSECODE ");
            stb.append("     )");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("     ,(SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM ASSESS_MST L3 ");
            stb.append("       WHERE L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("      ) ");
            stb.append("     AS ASSESS_LEVEL ");
            stb.append("    ,W3.AVG ");
            stb.append("    ,T_AVG1.AVG AS GRADE_AVG ");
            stb.append("    ,T_AVG2.AVG AS HR_AVG ");
            stb.append("    ,T_AVG3.AVG AS COURSE_AVG ");
            stb.append("    ,T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");

            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD AND T_AVG1.AVG_DIV = '1' ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
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
            stb.append("     ) ");

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
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' THEN VALUE(T33.SCORE, T3.SCORE) ELSE T3.SCORE END AS SCORE ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,T3.GRADE_AVG ");
            stb.append("        ,case when T1.SUBCLASSCD like '%"+ SUBCLASSCD999999 +"' then CLSS.HR_AVG     end AS HR_AVG ");
            stb.append("        ,case when T1.SUBCLASSCD like '%"+ SUBCLASSCD999999 +"' then COSE.COURSE_AVG end AS COURSE_AVG ");
            stb.append("        ,T3.MAJOR_AVG ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN SCHNO_A    SCHA  ON SCHA.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CLASS_AVG9 CLSS  ON CLSS.SEMESTER || CLSS.TESTKINDCD || CLSS.TESTITEMCD || CLSS.SCORE_DIV = T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ");
            stb.append("                           AND CLSS.GRADE = SCHA.GRADE AND CLSS.HR_CLASS = SCHA.HR_CLASS ");
            stb.append(" LEFT JOIN COURSE_AVG9 COSE ON COSE.SEMESTER || COSE.TESTKINDCD || COSE.TESTITEMCD || COSE.SCORE_DIV = T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ");
            stb.append("                           AND COSE.GRADE = SCHA.GRADE AND COSE.COURSECD = SCHA.COURSECD AND COSE.MAJORCD = SCHA.MAJORCD AND COSE.COURSECODE = SCHA.COURSECODE ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }

        public static void loadKanendoStudyrecDat(final DB2UDB db2, final List kanendoGradeList, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String testcd = "9990009";
            final Rank noRank = new Rank(null);
            try {
                for (final Iterator git = kanendoGradeList.iterator(); git.hasNext();) {
                    final String grade = (String) git.next();

                    final String sql = sqlKanendoStudyrec(param, grade);
                    if (param._isOutputDebug) {
                        log.info(" kanendo studyrec sql = " + sql);
                    }

                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        final Map subclassMap = getMappedMap(student._kanendoGradeSubclassMap, grade);

                        while (rs.next()) {
                            final Score score = new Score(
                                    testcd,
                                    rs.getString("VALUATION"),
                                    null,
                                    null,
                                    noRank,
                                    noRank,
                                    noRank,
                                    noRank,
                                    null, // rs.getString("KARI_HYOUTEI"),
                                    "0",
                                    rs.getString("COMP_CREDIT"),
                                    rs.getString("GET_CREDIT"),
                                    null
                                    );

                            final String subclasscd;
                            if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                                subclasscd = SUBCLASSCD999999;
                            } else {
                                subclasscd = rs.getString("SUBCLASSCD");
                            }
                            if (null == subclassMap.get(subclasscd)) {
                                final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                                subclassMap.put(subclasscd, subClass);
                            }
                            if (null == testcd) {
                                continue;
                            }
                            final SubClass subClass = Student.getSubClass(subclassMap, subclasscd);
                            subClass._scoreMap.put(testcd, score);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlKanendoStudyrec(final Param param, final  String grade) {

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
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            stb.append(" SELECT W1.SCHREGNO, ");
            stb.append("        W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD AS SUBCLASSCD, W1.VALUATION, W1.GET_CREDIT, W1.COMP_CREDIT ");
            stb.append("     FROM   SCHREG_STUDYREC_DAT W1 ");
            stb.append("     WHERE  W1.ANNUAL = '" + grade + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("        AND (W1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR W1.CLASSCD = '" + KNJDefineSchool.subject_T + "') ");
            return stb.toString();
        }


        public String toString() {
            return "(score = " + _score + "" + (TESTCD_GAKUNEN_HYOTEI.equals(_testcd) ? (" [getCredit = " + _getCredit + ", compCredit = " + _compCredit + "]") : "") +")";
        }
    }

    private static class Rank {
        final String _avg;
        public Rank(final String avg) {
            _avg = avg;
        }
    }

    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _semester;
        public String _scoreDivName;
        public String _semesterDetail;
        public DateRange _dateRange;
        public boolean _printScore;
        public String semester() {
            return _testcd.substring(0, 1);
        }
        public String scorediv() {
            return _testcd.substring(_testcd.length() - 2);
        }
        public boolean isKarihyotei() {
            return !SEMEALL.equals(semester()) && "09".equals(scorediv());
        }
        public String toString() {
            return "TestItem(" + _testcd + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semester, _semestername, sdate, edate);
        }
        public String toString() {
            return "Semester(" + _semester + ", " + _semestername + ")";
        }
    }

    private static class DateRange {
        final String _key;
        final String _semester;
        final String _name;
        final String _sdate;
        final String _edate;
        TestItem _testitem;
        public DateRange(final String key, final String semester, final String name, final String sdate, final String edate) {
            _key = key;
            _semester = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (!(o instanceof DateRange)) {
                return false;
            }
            final DateRange dr = (DateRange) o;
            return _key.equals(dr._key) && StringUtils.defaultString(_name).equals(StringUtils.defaultString(dr._name)) && rangeEquals(dr);
        }
        public boolean rangeEquals(final DateRange dr) {
            return StringUtils.defaultString(_sdate).equals(StringUtils.defaultString(dr._sdate)) && StringUtils.defaultString(_edate).equals(StringUtils.defaultString(dr._edate));
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _name + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class ClassMst implements Comparable {
        final String _classcd;
        final String _classabbv;
        final String _classname;
        final Integer _classShoworder3;
        public ClassMst(final String classcd, final String classabbv, final String classname,
                final int classShoworder3) {
            _classcd = classcd;
            _classabbv = classabbv;
            _classname = classname;
            _classShoworder3 = new Integer(classShoworder3);
        }
        public int compareTo(final Object o) {
            final ClassMst mst = (ClassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            return rtn;
        }
        public String toString() {
            return "(" + _classcd + ":" + _classname + ")";
        }
    }

    private static class SubclassMst implements Comparable {
        final ClassMst _classMst;
        final String _subclasscd;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final ClassMst classMst, final String subclasscd, final String subclassabbv, final String subclassname,
                final int subclassShoworder3,
                final boolean isSaki, final boolean isMoto) {
            _classMst = classMst;
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _subclassShoworder3 = new Integer(subclassShoworder3);
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classMst.compareTo(mst._classMst);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
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
        public String toString() {
            return "( class = " + _classMst + ", " + _subclasscd + ":" + _subclassname + ")";
        }
    }

    private static class Form {

        final int MAX_SUBCLASS = 17;
        final int COL_TOTAL = 18;
        final int COL_AVG = 19;
        final int COL_HRAVG = 20;
        final int COL_COURSEAVG = 21;
        final int COL_GET_CREDIT = 19;
        final int COL_COMP_CREDIT = 20;

        List _testcds;
        List _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendRemarkRanges;
        private Param _param;
        Map _fieldInfoMap = Collections.EMPTY_MAP;

        protected Param param() {
            return _param;
        }

        protected void initDebug() {
            if (_param._isOutputDebug) {
                for (int i = 0; i < _testcds.size(); i++) {
                    log.info(" testcds[" + i + "] = " + _testcds.get(i) + " : " + _testItems.get(i));
                }
                for (int i = 0; i < _attendRanges.length; i++) {
                    log.info(" attendRanges[" + i + "] = " + _attendRanges[i]);
                }
                for (int i = 0; i < _attendRemarkRanges.length; i++) {
                    log.info(" _attendRemarkRanges[" + i + "] = " + _attendRemarkRanges[i]);
                }
            }
        }

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

        protected Attendance[] getAttendances(final Student student) {
            final Attendance[] attendances = new Attendance[_attendRanges.length];
            for (int i = 0; i < _attendRanges.length; i++) {
                final DateRange dateRange = (DateRange) _attendRanges[i];
                if (null != dateRange) {
                    if (SEMEALL.equals(dateRange._key) && !SEMEALL.equals(param()._semester)) {
                        continue;
                    }
                    attendances[i] = (Attendance) student._attendMap.get(dateRange._key);
                    if (null != attendances[i]) {
                        attendances[i]._dateRange = dateRange;
                    }
                }
            }
            return attendances;
        }

        protected String[] getAttendanceRemarks(final Student student) {
            final String[] remarks = new String[_attendRemarkRanges.length];
            for (int i = 0; i < _attendRemarkRanges.length; i++) {
                final DateRange dateRange = (DateRange) _attendRemarkRanges[i];
                if (null != dateRange) {
                    if (SEMEALL.equals(dateRange._key)) {
                        continue;
                    }
                    remarks[i] = (String) student._attendRemarkMap.get(dateRange._key);
                }
            }
            return remarks;
        }

        protected List getTestItems(
                final Param param,
                final List testcds
        ) {
            final List testitems = new ArrayList();
            for (int i = 0; i < testcds.size(); i++) {
                testitems.add(null);
            }
            for (int j = 0; j < testcds.size(); j++) {
                testitems.set(j, (TestItem) param._testitemMap.get(testcds.get(j)));
            }
            final List notFoundTestcds = new ArrayList();
            for (int i = 0; i < testcds.size(); i++) {
                final String testcd = (String) testcds.get(i);
                if (null == testitems.get(i)) {
                    notFoundTestcds.add(testcd);
                }
            }
            if (!notFoundTestcds.isEmpty()) {
                log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + notFoundTestcds + " / 実際のマスタのコード:" + param._testitemMap.keySet());
            }
            return testitems;
        }

        protected List setTestcd3(final List testcdList, final String[] array, int min, final boolean has3Form) {
            List testcds;
            log.info(" db testcdList = " + testcdList);
            if (testcdList.isEmpty()) {
                testcds = new ArrayList(Arrays.asList(array));
            } else {
                testcds = new ArrayList(testcdList);
            }
            testcds.remove(TESTCD_GAKUNEN_HYOKA);
            testcds.remove(TESTCD_GAKUNEN_HYOTEI);
            if (testcds.size() > 5) {
                testcds = testcds.subList(0, 5);
            }
            while (testcds.size() < min) {
                testcds.add(null);
            }
            testcds.add(TESTCD_GAKUNEN_HYOKA);
            testcds.add(TESTCD_GAKUNEN_HYOTEI);
            return testcds;
        }

        protected static List getPrintSubclassList(final Map subclassMap, final Param param, final int max) {
            final List printSubclassList = new ArrayList();
            for (final Iterator it = subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final SubClass subClass = Student.getSubClass(subclassMap, subclasscd);
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                printSubclassList.add(subClass);
                if (printSubclassList.size() >= max) {
                    break;
                }
            }
            Collections.sort(printSubclassList);
            return printSubclassList;
        }

        protected TestItem testItem(final int i) {
            return (TestItem) _testItems.get(i);
        }

        public void setForm(final IPdf ipdf, final String form) {
            log.info(" form = " + form);
            int rtn = ipdf.VrSetForm(form, 1);
            if (rtn < 0) {
                throw new IllegalArgumentException("フォーム設定エラー:" + rtn);
            }
            if (ipdf instanceof SvfPdf) {
                SvfPdf svfpdf = (SvfPdf) ipdf;
                _fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(svfpdf.getVrw32alp());
            }
        }

        public boolean isKarihyoteiPrintAttendance(final TestItem testitem, final List testItems) {
            if (null == testitem) {
                return false;
            }
            if (!testitem.isKarihyotei()) {
                return true;
            }
            // 以下、仮評定
            // 学期詳細がなければ印字しない
            log.info(" karihyotei " + testitem + " ( semester_detail = " + testitem._semesterDetail + ")");
            if (null == testitem._semesterDetail) {
                return false;
            }
            for (int i = 0; i < testItems.size(); i++) {
                final TestItem ti = (TestItem) testItems.get(i);
                if (null == ti || ti == testitem || null == ti._semesterDetail) {
                    continue;
                }
                if (ti._semesterDetail.equals(testitem._semesterDetail)) {
                    log.warn("他の試験が同じ学期詳細をもつので出欠欄に表示しない: " + testitem);
                    return false;
                }
            }
            return true;
        }

        void init(final List testcdList) {
            _testcds = setTestcd3(testcdList, new String[] {"1010108", "1990008", "2010108"}, 4, false);
            _testItems = getTestItems(param(), _testcds);
            _attendRanges = new DateRange[2];
            _attendRemarkRanges = new DateRange[2];
            final Semester seme1 = param().getSemester(SEME1);
            if (null != seme1) {
                _attendRanges[0] = seme1._dateRange;
                _attendRemarkRanges[0] = seme1._dateRange;
            }
            final Semester seme2 = param().getSemester(SEME2);
            if (null != seme2) {
                _attendRemarkRanges[1] = seme2._dateRange;
            }
            final Semester seme9 = param().getSemester(SEMEALL);
            if (null != seme9) {
                _attendRanges[1] = seme9._dateRange;
            }
            initDebug();
        }

        void print(final DB2UDB db2, final IPdf ipdf, final Student student) {
            final String form;
            form = "KNJD186P.frm";
            setForm(ipdf, form);
            printHeader(db2, ipdf, student);
            printAttendance(ipdf, student);
            printShoken(ipdf, student);
            printScore(ipdf, student);
            printScoreKanendo(ipdf, "2", student, KANENDO_GRADE01);
            printScoreKanendo(ipdf, "3", student, KANENDO_GRADE02);
            printHyoteiHeikin(ipdf, student);
            ipdf.VrEndPage();
        }

        // 評定平均
        private void printHyoteiHeikin(final IPdf ipdf, final Student student) {
//            if (!_param._isLastSemester) {
//                // 最終学期のみ印字
//                return;
//            }
            final Map classMstMap = new HashMap();
            final Map classHyoteiListMap = new HashMap();
            final List printSubclassList = getPrintSubclassList(student._subclassMap, _param, 999);
            for (int subi = 0; subi < printSubclassList.size(); subi++) {
                final SubClass subClass = (SubClass) printSubclassList.get(subi);
                final Score scoreHyotei = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != scoreHyotei && NumberUtils.isDigits(scoreHyotei._score)) {
                   classMstMap.put(subClass._mst._classMst._classcd, subClass._mst._classMst);
                   getMappedList(classHyoteiListMap, subClass._mst._classMst._classcd).add(Integer.valueOf(scoreHyotei._score));
                }
            }
            for (final Iterator it = _param._kanendoGradeList.iterator(); it.hasNext();) {
                final String grade = (String) it.next();
                final Map subclassMap = getMappedMap(student._kanendoGradeSubclassMap, grade);
                final List printSubclassListKanendo = getPrintSubclassList(subclassMap, _param, 999);
                for (int subi = 0; subi < printSubclassListKanendo.size(); subi++) {
                    final SubClass subClass = (SubClass) printSubclassListKanendo.get(subi);
                    final Score scoreHyotei = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                    if (null != scoreHyotei && NumberUtils.isDigits(scoreHyotei._score)) {
                       classMstMap.put(subClass._mst._classMst._classcd, subClass._mst._classMst);
                       getMappedList(classHyoteiListMap, subClass._mst._classMst._classcd).add(Integer.valueOf(scoreHyotei._score));
                    }
                }
            }
            final List printClassList = new ArrayList(classMstMap.values());
            Collections.sort(printClassList);
            final List hyoteiAllList = new ArrayList();
            for (int i = 0; i < printClassList.size(); i++) {
                final ClassMst classMst = (ClassMst) printClassList.get(i);
                if (null != classMst._classcd && classMst._classcd.startsWith("90")) {
                	continue;
                }
                final List hyoteiList = getMappedList(classHyoteiListMap, classMst._classcd);
                final String classname = StringUtils.defaultString(classMst._classabbv, classMst._classname);
                final int keta = KNJ_EditEdit.getMS932ByteLength(classname);
                if (keta > 8) {
    				ipdf.VrsOutn("HYOTEIHEIKIN_CLASSNAME3_1", i + 1, classname);
                } else if (keta > 5) {
    				ipdf.VrsOutn("HYOTEIHEIKIN_CLASSNAME2", i + 1, classname);
                } else {
    				ipdf.VrsOutn("HYOTEIHEIKIN_CLASSNAME", i + 1, classname);
                }
                if (_param._isOutputDebug) {
                    log.info(" hyotei class = " + classMst + ", hyoteiList = " + hyoteiList);
                }
                ipdf.VrsOutn("HYOTEIHEIKIN_VAL", i + 1, getIntlistAverage(hyoteiList, _param));
                hyoteiAllList.addAll(hyoteiList);
            }
            ipdf.VrsOut("HYOTEIHEIKIN_VAL_LAST", getIntlistAverage(hyoteiAllList, _param));
        }

        // 過年度
        private void printScoreKanendo(final IPdf ipdf,
                final String fieldNum,
                final Student student,
                final String grade) {
            if (!_param._kanendoGradeList.contains(grade)) {
                return;
            }
            final Student.Regd regd = (Student.Regd) student._kanendoGradeRegdMap.get(grade);
            if (null != regd) {
                ipdf.VrsOut("RECORD_GRADE" + fieldNum + "_HEADER", StringUtils.defaultString(regd._hrName) + "　" + StringUtils.defaultString(regd._majorname) + " " + StringUtils.defaultString(regd._coursecodename)); // 評定
            }
            BigDecimal totalCompCredit = null;
            BigDecimal totalGetCredit = null;
            final Map subclassMap = getMappedMap(student._kanendoGradeSubclassMap, grade);
            final List printSubclassList = getPrintSubclassList(subclassMap, _param, MAX_SUBCLASS);
            for (int subi = 0; subi < printSubclassList.size(); subi++) {
                final int ci = subi + 1;
                final SubClass subClass = (SubClass) printSubclassList.get(subi);
                final String subclassname = StringUtils.defaultString(subClass._mst._subclassabbv, subClass._mst._subclassname);
                final int subclassnameLen = null == subclassname ? 0 : subclassname.length();
                //loginfo(param(), " kanendo subclass = " + subClass);
                if (subclassnameLen > 6) {
                    ipdf.VrsOutn("SUBCLASSNAME" + fieldNum + "_3_1", ci, subclassname.substring(0, 6)); // 科目名
                    ipdf.VrsOutn("SUBCLASSNAME" + fieldNum + "_3_2", ci, subclassname.substring(6)); // 科目名
                } else if (subclassnameLen == 6) {
                    ipdf.VrsOutn("SUBCLASSNAME" + fieldNum + "_2", ci, subclassname); // 科目名
                } else {
                    ipdf.VrsOutn("SUBCLASSNAME" + fieldNum, ci, subclassname); // 科目名
                }
                final Score scoreHyotei = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != scoreHyotei) {
                    ipdf.VrsOutn("VALUE" + fieldNum, ci, scoreHyotei._score); // 評定
                    ipdf.VrsOutn("COMP_CREDIT" + fieldNum, ci, scoreHyotei.getCompCredit()); // 履修単位
                    if (NumberUtils.isDigits(scoreHyotei.getCompCredit())) {
                        if (null == totalCompCredit) {
                            totalCompCredit = new BigDecimal(scoreHyotei.getCompCredit());
                        } else {
                            totalCompCredit = totalCompCredit.add(new BigDecimal(scoreHyotei.getCompCredit()));
                        }
                    }
                    if (NumberUtils.isDigits(scoreHyotei.getGetCredit())) {
                        if (null == totalGetCredit) {
                            totalGetCredit = new BigDecimal(scoreHyotei.getGetCredit());
                        } else {
                            totalGetCredit = totalGetCredit.add(new BigDecimal(scoreHyotei.getGetCredit()));
                        }
                    }
                }
            }
            if (null != totalCompCredit) {
                ipdf.VrsOutn("COMP_CREDIT" + fieldNum, COL_COMP_CREDIT, totalCompCredit.toString()); // 履修単位
            }
            if (null != totalGetCredit) {
                ipdf.VrsOutn("COMP_CREDIT" + fieldNum, COL_GET_CREDIT, totalGetCredit.toString()); // 修得単位
            }
        }

        void printAttendance(final IPdf ipdf, final Student student) {
            final Attendance[] attendances = getAttendances(student);
            final String[] remarks = getAttendanceRemarks(student);
            for (int i = 0; i < attendances.length; i++) {
                final int line = i + 1;
                final Attendance att = attendances[i];
                if (null == att) {
                    continue;
                }
                if (null != att && att._lesson != 0) {
                    ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    ipdf.VrsOutn("SUSPEND_MOURNING", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引
                    ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                    ipdf.VrsOutn("MLESSON", line, String.valueOf(att._mLesson)); // 出席すべき日数
                    ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                    ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                    ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                }
            }
            for (int i = 0; i < remarks.length; i++) {
            	final int line = i + 1;
            	final String remark = remarks[i];
            	if (null != remark) {
            		if (KNJ_EditEdit.getMS932ByteLength(remark) > 30) {
            			final List tokenList = KNJ_EditKinsoku.getTokenList(remark, 30);
            			for (int j = 0; j < tokenList.size(); j++) {
            				final String token = (String) tokenList.get(j);
                            ipdf.VrsOutn("ATTENDREMARK3_" + String.valueOf(j + 1), line, token); // 授業日数
            			}
            		} else if (KNJ_EditEdit.getMS932ByteLength(remark) > 18) {
                        ipdf.VrsOutn("ATTENDREMARK2", line, remark); // 授業日数
            		} else {
                        ipdf.VrsOutn("ATTENDREMARK", line, remark); // 授業日数
            		}
            	}
            }
        }

        void printShoken(final IPdf ipdf, final Student student) {


            //log.debug(" committee = " + getMappedList(student._debugDataMap, "COMMITTEE"));
            final List committeeList = new ArrayList();
            for (final Iterator it = student._semesCommitteeListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final List textList = (List) e.getValue();
                for (int ti = 0; ti < textList.size(); ti++) {
                    final String t = (String) textList.get(ti);
                    if (!committeeList.contains(t)) {
                        committeeList.add(t);
                    }
                }
            }
            final List committeeToken = KNJ_EditKinsoku.getTokenList(mkString(committeeList, "、"), 34, 4);
            for (int i = 0; i < committeeToken.size(); i++) {
                ipdf.VrsOutn("COMIITTEE", (i + 1), (String) committeeToken.get(i));
            }

            //log.debug(" student._semesClubListMap = " + student._debugDataMap);
            final List clubList = new ArrayList();
            for (final Iterator it = student._semesClubListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final List textList = (List) e.getValue();
                for (int ti = 0; ti < textList.size(); ti++) {
                    final String t = (String) textList.get(ti);
                    if (!clubList.contains(t)) {
                        clubList.add(t);
                    }
                }
            }
            final List clubToken = KNJ_EditKinsoku.getTokenList(mkString(clubList, "、"), 34, 4);
            for (int i = 0; i < clubToken.size(); i++) {
                ipdf.VrsOutn("CLUB", (i + 1), (String) clubToken.get(i));
            }

            final KNJPropertiesShokenSize shokenSize = KNJPropertiesShokenSize.getShokenSize(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H, 17, 4);
            final List token = KNJ_EditKinsoku.getTokenList(student._specialactremark, shokenSize.getKeta(), shokenSize._gyo);
            for (int i = 0; i < token.size(); i++) {
                ipdf.VrsOutn("COMMUNICATION", (i + 1), (String) token.get(i));
            }
        }

        void printScore(final IPdf ipdf, final Student student) {
            final List printSubclassList = getPrintSubclassList(student._subclassMap, _param, MAX_SUBCLASS);
            for (int subi = 0; subi < printSubclassList.size(); subi++) {
                final int ci = subi + 1;
                final SubClass subClass = (SubClass) printSubclassList.get(subi);
                // loginfo(param(), " subclass = " + subClass);
                final String subclassname = StringUtils.defaultString(subClass._mst._subclassabbv, subClass._mst._subclassname);
                loginfo(param(), " " + subClass._mst._subclasscd + " subclassname = " + subclassname);
                final int subclassnameLen = null == subclassname ? 0 : subclassname.length();
                if (subclassnameLen > 6) {
                    ipdf.VrsOutn("SUBCLASSNAME1_3_1", ci, subclassname.substring(0, 6)); // 科目名
                    ipdf.VrsOutn("SUBCLASSNAME1_3_2", ci, subclassname.substring(6)); // 科目名
                } else if (subclassnameLen == 6) {
                    ipdf.VrsOutn("SUBCLASSNAME1_2", ci, subclassname); // 科目名
                } else {
                    ipdf.VrsOutn("SUBCLASSNAME1", ci, subclassname); // 科目名
                }
//                Form.setRecordString(ipdf, "CREDIT", ci, param().getCredits(subClass._mst._subclasscd, student._course)); // 単位数
                for (int ti = 0; ti < _testItems.size(); ti++) {
                    final TestItem item = testItem(ti);
                    if (null == item || TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                        continue;
                    }
                    final Score score = subClass.getScore(item._testcd);
                    if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd)) {
                    	if (SEMEALL.equals(_param._semester)) {
                    		final String f = "4";
                    		if (null != score) {
                    			ipdf.VrsOutn("SCORE1_" + f, ci, score._score); // 評価
                    		}
                    		final Score scoreHyotei = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                    		if (null != scoreHyotei) {
                    			ipdf.VrsOutn("VALUE1", ci, scoreHyotei._score); // 評定
                    			ipdf.VrsOutn("GET_CREDIT1", ci, scoreHyotei.getGetCredit()); // 修得単位
                    			ipdf.VrsOutn("COMP_CREDIT1", ci, scoreHyotei.getCompCredit()); // 履修単位
                    		}
                    	}
                    } else {
                        final String[] fld = new String[] {"1", "2", "3"};
                        if (ti < fld.length && null != score) {
                            ipdf.VrsOutn("SCORE1_" + fld[ti], ci, score._score); // 評価
                        }
                    }
                }
            }
        }

        void printHeader(final DB2UDB db2, final IPdf ipdf, final Student student) {
            ipdf.VrsOut("MAJORNAME", student._regd._majorname); // 学科名
            ipdf.VrsOut("HR_NAME", StringUtils.defaultString(student._regd._hrName) + " " + StringUtils.defaultString(student._regd._majorname) + " " + StringUtils.defaultString(student._regd._coursecodename) + " " + student._regd.getPrintAttendno()); // クラス名
            ipdf.VrsOut("NAME", student._name); // 氏名
            ipdf.VrsOut("TITLE", StringUtils.defaultString(param()._schoolName) + "　成績通知表　（" + KNJ_EditDate.gengou(db2, Integer.parseInt(param()._ctrlYear)) + "年度）"); // 年度
            ipdf.VrsOut("ATTENDNO", student._regd._attendno); // 出席番号
            ipdf.VrsOut("PRINCIPAL_JOBNAME", param()._jobName); // 校長職名
            ipdf.VrsOut("STAFF_JOBNAME", param()._hrJobName); // 担任職名
            ipdf.VrsOut("PRINCIPAL_NAME", param()._principalName); // 校長
            ipdf.VrsOut("STAFFNAME", student._regd._staffName); // 担任
            ipdf.VrsOutn("SEMESTER", 1, _attendRanges[0]._name); // 学期
            ipdf.VrsOutn("SEMESTER", 2, _attendRanges[1]._name); // 学期

            ipdf.VrsOutn("SUBCLASSNAME1", COL_TOTAL, "総合点");
            ipdf.VrsOutn("SUBCLASSNAME1", COL_AVG, "平均");
            ipdf.VrsOutn("SUBCLASSNAME1", COL_HRAVG, "クラス平均");
            ipdf.VrsOutn("SUBCLASSNAME1", COL_COURSEAVG, "コース平均");
            ipdf.VrsOutn("SUBCLASSNAME2", COL_GET_CREDIT, "修得単位数");
            ipdf.VrsOutn("SUBCLASSNAME2", COL_COMP_CREDIT, "履修単位数");
            ipdf.VrsOutn("SUBCLASSNAME3", COL_GET_CREDIT, "修得単位数");
            ipdf.VrsOutn("SUBCLASSNAME3", COL_COMP_CREDIT, "履修単位数");
            ipdf.VrsOut("HYOTEIHEIKIN_CLASSNAME_LAST", "平均");

            final String[] fields;
            fields = new String[] {"1", "2", "3", "4"};
            final SubClass subClass999999 = Student.getSubClass(student._subclassMap, SUBCLASSCD999999);
            for (int i = 0; i < _testItems.size(); i++) {
                final TestItem testitem = testItem(i);
                if (null == testitem) {
                    continue;
                }
                final String f;
                if (TESTCD_GAKUNEN_HYOKA.equals(testitem._testcd)) {
                    f = "4";
                } else if (TESTCD_GAKUNEN_HYOTEI.equals(testitem._testcd)) {
                    f = "5";
                } else {
                    if (i >= fields.length) {
                        continue;
                    }
                    f = fields[i];
                }
                String testname = "";
                if (!SEMEALL.equals(testitem._semester)) {
                    testname += StringUtils.defaultString(param().getSemestername(testitem._semester));
                }
                testname += StringUtils.defaultString(testitem._testitemname);
                ipdf.VrsOut("TESTNAME" + f, testname); // テスト名
                if (null != subClass999999 && null != subClass999999.getScore(testitem._testcd)) {
                    final Score score = subClass999999.getScore(testitem._testcd);
                    if (TESTCD_GAKUNEN_HYOTEI.equals(testitem._testcd)) {
                    	if ("1".equals(score._provFlg) || !SEMEALL.equals(_param._semester)) {
                    	} else {
                            ipdf.VrsOutn("SCORE1_" + f, COL_TOTAL, score._score); // 合計
                            ipdf.VrsOutn("AVG" + f, COL_AVG, sishaGonyu(score._avg)); // 平均
                            ipdf.VrsOutn("AVG" + f, COL_HRAVG, sishaGonyu(score._hrRank._avg)); // 組平均
                            ipdf.VrsOutn("AVG" + f, COL_COURSEAVG, sishaGonyu(score._courseRank._avg)); // コース順位
                    	}
                    } else {
                        ipdf.VrsOutn("SCORE1_" + f, COL_TOTAL, score._score); // 合計
                        ipdf.VrsOutn("AVG" + f, COL_AVG, sishaGonyu(score._avg)); // 平均
                        ipdf.VrsOutn("AVG" + f, COL_HRAVG, sishaGonyu(score._hrRank._avg)); // 組平均
                        ipdf.VrsOutn("AVG" + f, COL_COURSEAVG, sishaGonyu(score._courseRank._avg)); // コース順位
                    }
                }
            }
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74335 $ $Date: 2020-05-15 16:40:10 +0900 (金, 15 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _ctrlSemester;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final Form _form;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMstMap;
//        private Map _creditMst;
        private Map _recordMockOrderSdivDatMap;

        private KNJSchoolMst _knjSchoolMst;

//        private int _gradCredits;  // 卒業認定単位数
        private String _lastSemester;
        private boolean _isLastSemester;

        private String _avgDiv;
        private String _d054Namecd2Max;
        private String _sidouHyoji;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private String _title;
        private boolean _isNoPrintMoto;
        final List _kanendoGradeList;
        final Map _testitemMap;
        final boolean _isOutputDebug;
        final Map _attendParamMap;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");

            _semesterMap = loadSemester(db2, _ctrlYear, request.getParameter("SEMESTER"), _grade);
            _semester = request.getParameter("SEMESTER");
            setCertifSchoolDat(db2);
            _form = new Form();
            _form._param = this;

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _kanendoGradeList = new ArrayList();
            if (Integer.parseInt(_grade) > 1) {
                _kanendoGradeList.add(KANENDO_GRADE01);
            }
            if (Integer.parseInt(_grade) > 2) {
                _kanendoGradeList.add(KANENDO_GRADE02);
            }

            _definecode = createDefineCode(db2);
            setSubclassMst(db2);
            setRecordMockOrderSdivDat(db2);
            loadNameMstD016(db2);
            _testitemMap = getTestItemMap(db2);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H");
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186P' AND NAME = '" + propName + "' "));
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _ctrlYear);         //各学校における定数等設定
            return definecode;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getSemestername(final String semester) {
            final Semester s = getSemester(semester);
            if (null == s) {
                log.warn(" no semester : " + s);
                return null;
            }
            return s._semestername;
        }

        private Semester getSemester(final String semester) {
            return (Semester) _semesterMap.get(semester);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String semester, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            String lastSemester = null;
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
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                        lastSemester = rs.getString("SEMESTER");
                    }
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _lastSemester = lastSemester;
            _isLastSemester = null != _lastSemester && _lastSemester.equals(semester);
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _jobName = KnjDbUtils.getString(row, "JOB_NAME");
            _principalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _hrJobName = KnjDbUtils.getString(row, "REMARK2");
            _title = KnjDbUtils.getString(row, "REMARK6");

            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
            	try {
            		throw new IllegalStateException(subclasscd);
            	} catch (Exception e) {
            		log.warn("not found subclasscd " + subclasscd, e);
            	}
                return createSubclassMst(subclasscd);
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }

		private SubclassMst createSubclassMst(final String subclasscd) {
			String classcd = null;
			if (null != subclasscd) {
			    final String[] split = StringUtils.split(subclasscd, "-");
			    if (null != split && split.length > 2) {
			        classcd = split[0] + "-" + split[1];
			    }
			}
			return new SubclassMst(new ClassMst(classcd, "", "", 999999), subclasscd, null, null, 999999, false, false);
		}

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " VALUE(T2.CLASSORDERNAME2, T2.CLASSABBV) AS CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, ";
                sql += " VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSABBV) AS SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " VALUE(T2.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int classShoworder3 = rs.getInt("CLASS_SHOWORDER3");
                    final int subclassShoworder3 = rs.getInt("SUBCLASS_SHOWORDER3");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final ClassMst classMst = new ClassMst(rs.getString("CLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), classShoworder3);
                    final SubclassMst mst = new SubclassMst(classMst, rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), subclassShoworder3, isSaki, isMoto);
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public List getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
            final String[] keys = {grade + "-" + coursecd + "-" + majorcd, "00" + "-" + coursecd + "-" + majorcd, grade + "-" + "0" + "-" + "000", "00" + "-" + "0" + "-" + "000"};
            for (int i = 0; i < keys.length; i++) {
                final List rtn = (List) _recordMockOrderSdivDatMap.get(keys[i]);
                if (null != rtn) {
                    log.info(" set key = " + keys[i]);
                    return rtn;
                }
            }
            log.info(" set key = " + ArrayUtils.toString(keys) + " but nothing");
            return Collections.EMPTY_LIST;
        }

        private void setRecordMockOrderSdivDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _recordMockOrderSdivDatMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
                sql += " T1.SEQ,  ";
                sql += " T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD ";
                sql += " FROM RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
                sql += " WHERE T1.YEAR = '" + _ctrlYear + "' ";
                sql += "   AND T1.TEST_DIV = '1' ";
                sql += " ORDER BY ";
                sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
                sql += " T1.SEQ  ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key = rs.getString("GRADE") + "-" + rs.getString("COURSECD") + "-" + rs.getString("MAJORCD");
                    if (null == _recordMockOrderSdivDatMap.get(key)) {
                        _recordMockOrderSdivDatMap.put(key, new ArrayList());
                    }
                    ((List) _recordMockOrderSdivDatMap.get(key)).add(rs.getString("TESTCD"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        protected Map getTestItemMap(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map testitemMap = new HashMap();
            try {
                final String sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD "
                                 +  " ,TESTITEMNAME "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
                                 + "  ,T1.SEMESTER_DETAIL "
                                 + "  ,T2.SDATE "
                                 + "  ,T2.EDATE "
                                 +  " ,CASE WHEN T1.SEMESTER <= '" + _semester + "' THEN 1 ELSE 0 END AS PRINT "
                                 +  " ,NMD053.NAME1 AS SCORE_DIV_NAME "
                                 +  "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 "
                                 +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
                                 +  " AND T2.SEMESTER = T1.SEMESTER "
                                 +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                                 +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' "
                                 +  "WHERE T1.YEAR = '" + _ctrlYear + "' "
                                 +  " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("TESTCD");
                    final TestItem testitem = new TestItem();
                    testitem._testcd = testcd;
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._sidouinput = rs.getString("SIDOU_INPUT");
                    testitem._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                    testitem._semester = rs.getString("SEMESTER");
                    testitem._semesterDetail = rs.getString("SEMESTER_DETAIL");
                    testitem._dateRange = new DateRange(testitem._testcd, testitem._semester, testitem._testitemname, rs.getString("SDATE"), rs.getString("EDATE"));
                    testitem._printScore = "1".equals(rs.getString("PRINT"));
                    testitem._scoreDivName = rs.getString("SCORE_DIV_NAME");
                    testitemMap.put(testcd, testitem);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemMap;
        }
    }
}
