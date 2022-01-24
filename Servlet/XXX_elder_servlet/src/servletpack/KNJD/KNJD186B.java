// kanji=漢字
/*
 * $Id: f69e62982ed03964a35eae6c0affd2424e792aa4 $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186B {
    private static final Log log = LogFactory.getLog(KNJD186B.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                try {
                    db2.commit();
                    db2.close();
                } catch (Exception ex) {
                    log.error("db close error!", ex);
                }
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
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
            param._form.print1(svf, param, student);
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

    private static String zeroBlank(final int v) {
        if (v == 0) {
            return "";
        }
        return String.valueOf(v);
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List studentList0
    ) {
        final Form form = param._form;

        final Map courseStudentsMap = new HashMap();
        for (final Iterator it = studentList0.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            getMappedList(courseStudentsMap, key).add(student);
        }

        for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final List studentList = (List) courseStudentsMap.get(key);

            for (int i = 0; i < form._attendRanges.length; i++) {
                final DateRange range = form._attendRanges[i];
                Attendance.load(db2, param, studentList, range);
            }
            for (int i = 0; i < form._attendSubclassRanges.length; i++) {
                final DateRange range = form._attendSubclassRanges[i];
                SubclassAttendance.load(db2, param, studentList, range);
            }

            String testcdor = "";
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            for (int i = 0; i < form._testcds.length; i++) {
                final String testcd = form._testcds[i];
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
            log.info(" stbtestcd =  "+ stbtestcd);
            Score.load(db2, param, studentList, stbtestcd);
        }
        Qualified.setQualifiedList(db2, param, studentList0);
        DetailHist.setDetailHistList(db2, param, studentList0);
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
        final String _gradeCd;
        final String _gradeName1;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
//        final Map _attendRemarkMap;
        final Map _subclassMap;
//        final List _proficiencySubclassScoreList;
        final String _entyear;
//        private int _previousCredits;  // 前年度までの修得単位数
//        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private List _qualifiedList = Collections.EMPTY_LIST;
        private List _detailList = Collections.EMPTY_LIST;

        Student(final String schregno, final String name, final String gradeCd, final String gradeName1, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _name = name;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
            _attendMap = new TreeMap();
//            _attendRemarkMap = new TreeMap();
            _subclassMap = new TreeMap();
//            _proficiencySubclassScoreList = new ArrayList();
        }

        Subclass getSubclass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                return new Subclass(new SubclassMst(null, null, null, null, null, false, false, new Integer(999999)));
            }
            return (Subclass) _subclassMap.get(subclasscd);
        }

        public String getHrAttendNo(final Param param) {
            try {
                final String grade = String.valueOf(Integer.parseInt(_gradeCd)) + "年";
                final String hrclass = StringUtils.defaultString(_hrClassName1) + "組";
                final String attendno = String.valueOf(Integer.parseInt(_attendno)) + "番";
                return grade + " " + hrclass + " " + attendno;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
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
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,T10.GRADE_CD ");
            stb.append("            ,T10.GRADE_NAME1 ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._ctrlYear + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT T10 ON T10.YEAR = W1.YEAR AND T10.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
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
            final String sql = stb.toString();
            log.info(" student sql = " + sql);

            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("GRADE_CD"), rs.getString("GRADE_NAME1"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

//        // 仮評定があるか
//        public boolean hasKari(final Param param) {
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd) || subclasscd.endsWith("333333") || subclasscd.endsWith("555555") || subclasscd.endsWith("99999B")) {
//                    continue;
//                }
//                final Subclass subclass = getSubclass(subclasscd);
//                if (null == subclass || param._isNoPrintMoto && subclass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
//                if (null != score && NumberUtils.isDigits(score._score) && "1".equals(score._provFlg)) {
//                    return true;
//                }
//            }
//            return false;
//        }

//        public String getTotalGetCredit(final Param param) {
//            int totalGetCredit = 0;
//            int totalGetCreditKari = 0;
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd)) {
//                    continue;
//                }
//                final Subclass subclass = getSubclass(subclasscd);
//                if (null == subclass || param._isNoPrintMoto && subclass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
//                if (null != score && NumberUtils.isDigits(score.getGetCredit())) {
//                    final int iCredit = Integer.parseInt(score.getGetCredit());
//                    if ("1".equals(score._provFlg)) {
//                        totalGetCreditKari += iCredit;
//                    } else {
//                        totalGetCredit += iCredit;
//                    }
//                }
//            }
//            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
//                return "(" + String.valueOf(totalGetCreditKari) + ")";
//            }
//            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
//        }

        private static Map createMap(final ResultSet rs) throws SQLException {
            final Map m = new HashMap();
            final ResultSetMetaData meta = rs.getMetaData();
            for (int col = 1; col <= meta.getColumnCount(); col++) {
                final String key = meta.getColumnName(col);
                final String val = rs.getString(col);
                m.put(key, val);
            }
            return m;
        }

//        private static String addLine(final String source, final String data) {
//            if (StringUtils.isBlank(source)) {
//                return data;
//            }
//            if (StringUtils.isBlank(data)) {
//                return source;
//            }
//            return source + "\n" + data;
//        }
    }

    private static class Attendance {

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
//        BigDecimal _lhrKekka = new BigDecimal("0");
//        BigDecimal _gyojiKekka = new BigDecimal("0");
//        BigDecimal _iinkaiKekka = new BigDecimal("0");
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._edate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._edate) > 0 ? param._edate : dateRange._edate;
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
//            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._ctrlYear, dateRange._sdate, edate);
//            loadRemark(db2, param, (String) hasuuMap.get("attendSemesInState"), studentList, dateRange);
        }

//        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List studentList, final DateRange dateRange) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT T1.MONTH, T1.SEMESTER, T1.SCHREGNO, T1.REMARK1 ");
//                stb.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
//                stb.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//                stb.append(" WHERE ");
//                stb.append("   T1.COPYCD = '0' ");
//                stb.append("   AND T1.YEAR = '" + param._ctrlYear + "' ");
//                stb.append("   AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + " ");
//                stb.append("   AND T1.SCHREGNO = ? ");
//                stb.append("   AND T1.REMARK1 IS NOT NULL ");
//                stb.append(" ORDER BY T1.MONTH, T1.SEMESTER ");
//
//                //log.debug(" dateRange = " + dateRange + " /  remark sql = " + stb.toString());
//                ps = db2.prepareStatement(stb.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    ps.setString(1, student._schregno);
//                    rs = ps.executeQuery();
//
//                    String comma = "";
//                    final StringBuffer remark = new StringBuffer();
//                    while (rs.next()) {
//                        remark.append(comma).append(rs.getString("REMARK1"));
//                        comma = "、";
//                    }
//                    if (remark.length() != 0) {
//                        student._attendRemarkMap.put(dateRange._key, remark.toString());
//                    }
//
//                    DbUtils.closeQuietly(rs);
//                }
//
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }
    }

    /**
     * 科目
     */
    private static class Subclass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        final Map _attendMap;

        Subclass(
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
            if (!(o instanceof Subclass)) return -1;
            final Subclass os = (Subclass) o;
            if (null == os._mst || null == os._mst._subclasscode) return -1;
            int cmp;
            cmp = _mst._showOrder.compareTo(os._mst._showOrder);
            if (0 != cmp) {
                return cmp;
            }
            cmp = _mst._subclasscode.compareTo(os._mst._subclasscode);
            return cmp;
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
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._edate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._edate) > 0 ? param._edate : dateRange._edate;
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

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
                        if (null == mst) {
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd < Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            // final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            // final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? replacedSick : sick);

                            if (null == student._subclassMap.get(subclasscd)) {
                                final Subclass subclass = new Subclass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subclass);
                            }
                            final Subclass subclass = student.getSubclass(subclasscd);
                            subclass._attendMap.put(dateRange._key, subclassAttendance);
                        }
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
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;
        final String _slumpScoreKansan;
        final String _provFlg;
        String _avgCourseAvg;

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
                final String slump,
                final String slumpMark,
                final String slumpScore,
                final String slumpScoreKansan,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
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
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpScoreKansan = slumpScoreKansan;
            _slumpMark = slumpMark;
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

//        private int getFailValue(final Param param) {
//            if (param.isPerfectRecord() && null != _passScore) {
//                return Integer.parseInt(_passScore);
//            } else if (param.isKetten() && !StringUtils.isBlank(param._ketten)) {
//                return Integer.parseInt(param._ketten);
//            }
//            return -1;
//        }

        private String getPrintSlump(final TestItem testItem) {
            return null;
        }

        private boolean isFail(final Param param, final TestItem testItem) {
            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
                return "1".equals(_score);
            }
            return "1".equals(_assessLevel);
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
                final String sql = sqlScore(param, stbtestcd);
                log.info(" subclass query start.");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" subclass query end.");

                while (rs.next()) {
                    final Student student = getStudent(studentList, rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                            rs.getString("SCORE"),
                            rs.getString("ASSESS_LEVEL"),
                            rs.getString("AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // rs.getString("KARI_HYOUTEI"),
                            rs.getString("REPLACEMOTO"),
                            rs.getString("SLUMP"),
                            rs.getString("SLUMP_MARK"),
                            rs.getString("SLUMP_SCORE"),
                            rs.getString("SLUMP_SCORE_KANSAN"),
                            rs.getString("COMP_CREDIT"),
                            rs.getString("GET_CREDIT"),
                            rs.getString("PROV_FLG")
                    );
                    score._avgCourseAvg = rs.getString("AVG_COURSE_AVG");

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final Subclass subclass = new Subclass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subclass);
                    }
                    if (null == testcd) {
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final Subclass subclass = student.getSubclass(subclasscd);
                    subclass._scoreMap.put(testcd, score);
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

            stb.append("   , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W1.GRADE ");
            stb.append("    ,W1.COURSECD ");
            stb.append("    ,W1.MAJORCD ");
            stb.append("    ,W1.COURSECODE ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM RELATIVEASSESS_MST L3 ");
            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("         AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("      ) ELSE ");
            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM ASSESS_MST L3 ");
            stb.append("       WHERE L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("      ) ");
            stb.append("     END AS ASSESS_LEVEL ");
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
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
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
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK ");
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
//            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' ");
//            stb.append(stbtestcd.toString());
//            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            stb.append("            AND W3.SCORE IS NOT NULL ");
//            stb.append("     ) ");


            stb.append(",T_AVG_COURSE_AVG AS(");
            stb.append("    SELECT ");
            stb.append("     W3.SEMESTER,W3.TESTKINDCD,W3.TESTITEMCD,W3.SCORE_DIV ");
            stb.append("    ,W3.SUBCLASSCD ");
            stb.append("    ,W4.GRADE,W4.COURSECD,W4.MAJORCD,W4.COURSECODE ");
            stb.append("    ,AVG(W3.AVG) AS AVG_COURSE_AVG ");
            stb.append("    FROM  RECORD_REC W3 ");
            stb.append("    INNER JOIN SCHNO_A W4 ON W4.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE W3.SCORE IS NOT NULL ");
            stb.append("    GROUP BY ");
            stb.append("     W3.SEMESTER,W3.TESTKINDCD,W3.TESTITEMCD,W3.SCORE_DIV ");
            stb.append("    ,W3.SUBCLASSCD ");
            stb.append("    ,W4.GRADE,W4.COURSECD,W4.MAJORCD,W4.COURSECODE ");
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
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.ASSESS_LEVEL ");
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
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,T44.AVG_COURSE_AVG ");
            stb.append("        ,CAST(NULL AS VARCHAR(1)) AS SLUMP ");
            stb.append("        ,CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK ");
            stb.append("        ,CAST(NULL AS SMALLINT) AS SLUMP_SCORE ");
            stb.append("        ,CAST(NULL AS SMALLINT) AS SLUMP_SCORE_KANSAN ");
//            stb.append("        ,K1.SLUMP ");
//            stb.append("        ,K1.SLUMP_MARK ");
//            stb.append("        ,K1.SLUMP_SCORE ");
//            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN T_AVG_COURSE_AVG T44 ON T44.SUBCLASSCD = T2.SUBCLASSCD AND T44.GRADE = T3.GRADE AND T44.COURSECD = T3.COURSECD AND T44.MAJORCD = T3.MAJORCD AND T44.COURSECODE = T3.COURSECODE  AND T44.SEMESTER = T2.SEMESTER AND T44.TESTKINDCD = T2.TESTKINDCD AND T44.TESTITEMCD = T2.TESTITEMCD AND T44.SCORE_DIV = T2.SCORE_DIV ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

//            //成績不振科目データの表
//            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
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
        public String toString() {
            return "Rank { rank = (" + _rank + ", " + _avgRank + "), count = " + _count + ", avg = " + _avg + "}";
        }
    }

    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _semester;
        public String _scoreDivName;
        public DateRange _dateRange;
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

    private static class SubclassMst {
        final String _subclasscode;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        final Integer _showOrder;
        public SubclassMst(final String subclasscode, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final boolean isSaki, final boolean isMoto, final Integer showOrder) {
            _subclasscode = subclasscode;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _showOrder = showOrder;
        }
    }

    private static class Qualified {
        final String _seq;
        final String _regddate;
        final String _qualifiedCd;
        final String _qualifiedName;
        final String _rank;
        final String _rankName;
        final String _remark;

        Qualified(
            final String seq,
            final String regddate,
            final String qualifiedCd,
            final String qualifiedName,
            final String rank,
            final String rankName,
            final String remark
        ) {
            _seq = seq;
            _regddate = regddate;
            _qualifiedCd = qualifiedCd;
            _qualifiedName = qualifiedName;
            _rank = rank;
            _rankName = rankName;
            _remark = remark;
        }

        public static void setQualifiedList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    final List list = new ArrayList();
                    while (rs.next()) {
                        final String seq = rs.getString("SEQ");
                        final String regddate = rs.getString("REGDDATE");
                        final String qualifiedCd = rs.getString("QUALIFIED_CD");
                        final String qualifiedName = rs.getString("QUALIFIED_NAME");
                        final String rank = rs.getString("RANK");
                        final String rankName = rs.getString("RANK_NAME");
                        final String remark = rs.getString("REMARK");
                        final Qualified qualified = new Qualified(seq, regddate, qualifiedCd, qualifiedName, rank, rankName, remark);
                        list.add(qualified);
                    }
                    student._qualifiedList = list;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.SEQ ");
            stb.append("     , T1.REGDDATE ");
            stb.append("     , T1.QUALIFIED_CD ");
            stb.append("     , T2.QUALIFIED_NAME ");
            stb.append("     , T1.RANK ");
            stb.append("     , T3.NAME1 AS RANK_NAME ");
            stb.append("     , T1.REMARK ");
            stb.append(" FROM SCHREG_QUALIFIED_HOBBY_DAT T1 ");
            stb.append(" LEFT JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
            stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'H312' ");
            stb.append("     AND T3.NAMECD2 = T1.RANK ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("     AND FISCALYEAR(T1.REGDDATE) = '" + param._ctrlYear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEQ ");
            stb.append("  ");
            return stb.toString();
        }
    }

    private static class DetailHist {
        final String _schregno;
        final String _detailSdate;
        final String _detailcdName;
        final String _remark;

        DetailHist(
            final String schregno,
            final String detailSdate,
            final String detailcdName,
            final String remark
        ) {
            _schregno = schregno;
            _detailSdate = detailSdate;
            _detailcdName = detailcdName;
            _remark = remark;
        }

        public static void setDetailHistList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final List list = new ArrayList();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        final String detailSdate = rs.getString("DETAIL_SDATE");
                        final String detailcdName = rs.getString("DETAILCD_NAME");
                        final String remark = rs.getString("REMARK");
                        final DetailHist detailhist = new DetailHist(schregno, detailSdate, detailcdName, remark);
                        list.add(detailhist);
                    }
                    student._detailList = list;
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("        T1.SCHREGNO  ");
            stb.append("      , T1.DETAIL_SDATE  ");
            stb.append("      , T3.NAME1 AS DETAILCD_NAME  ");
            stb.append("      , T1.REMARK  ");
            stb.append("  FROM SCHREG_DETAILHIST_DAT T1  ");
            stb.append("  LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'H303'  ");
            stb.append("      AND T3.NAMECD2 = T1.DETAILCD  ");
            stb.append("  WHERE  ");
            stb.append("      T1.YEAR = '" + param._ctrlYear + "'  ");
            stb.append("      AND T1.SCHREGNO = ?  ");
            stb.append("      AND DETAIL_DIV = '1'  ");
            stb.append("  ORDER BY  ");
            stb.append("      T1.DETAIL_SDATE, T1.DETAILCD  ");
            return stb.toString();
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

    private static class Form {

        String[] _testcds;
        TestItem[] _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendSubclassRanges;
        final Param _param;

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

//        protected String[] getAttendanceRemarks(final Student student) {
//            final String[] remarks = new String[_attendRanges.length];
//            for (int i = 0; i < _attendRanges.length; i++) {
//                final DateRange dateRange = (DateRange) _attendRanges[i];
//                if (null != dateRange) {
//                    if (SEMEALL.equals(dateRange._key)) {
//                        continue;
//                    }
//                    remarks[i] = (String) student._attendRemarkMap.get(dateRange._key);
//                }
//            }
//            return remarks;
//        }

        Form(final DB2UDB db2, final Param param) {
            _param = param;
            _testItems = getTestItems(db2, _param);
            _attendRanges = new DateRange[] { new DateRange(SEMEALL, "", _param._sdate, _param._edate) };
            _attendSubclassRanges = _attendRanges;
            for (int i = 0; i < _testcds.length; i++) {
                log.info(" testcds[" + i + "] = " + _testcds[i] + " : " + _testItems[i]);
            }
            for (int i = 0; i < _attendRanges.length; i++) {
                log.debug(" attendRanges[" + i + "] = " + _attendRanges[i]);
            }
            for (int i = 0; i < _attendSubclassRanges.length; i++) {
                log.debug(" attendSubclassRanges[" + i + "] = " + _attendSubclassRanges[i]);
            }
        }

        protected TestItem[] getTestItems(
                final DB2UDB db2,
                final Param param
        ) {
            final List testitemList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql;
                sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD ";
                sql +=  " ,TESTITEMNAME ";
                sql += "  ,SIDOU_INPUT ";
                sql += "  ,SIDOU_INPUT_INF ";
                sql += "  ,T1.SEMESTER ";
                sql += "  ,T2.SDATE ";
                sql += "  ,T2.EDATE ";
                sql +=  " ,NMD053.NAME1 AS SCORE_DIV_NAME ";
                sql +=  "FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
                sql +=  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ";
                sql +=  " AND T2.SEMESTER = T1.SEMESTER ";
                sql +=  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ";
                sql +=  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' ";
                sql +=  "WHERE T1.YEAR = '" + param._ctrlYear + "' ";
                sql +=  "  AND T1.SCORE_DIV <> '08' ";
                sql +=  "  AND NOT (T1.SEMESTER <> '9' AND SCORE_DIV = '09') ";
                sql += " AND GRADE = '00' ";
                sql += " AND COURSECD || '-' || MAJORCD = '" + param._major + "' ";
                sql += " AND SCHOOLCD = '" + param.SCHOOLCD + "' ";
                sql += " AND SCHOOL_KIND = '" + param.SCHOOLKIND + "' ";
                sql += " AND T1.SEMESTER <= '" + param._semester + "' "; //指定した学期まで
                sql += " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
                log.info(" testitem sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final TestItem testitem = new TestItem();
                    testitem._testcd = rs.getString("TESTCD");
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._sidouinput = rs.getString("SIDOU_INPUT");
                    testitem._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                    testitem._semester = rs.getString("SEMESTER");
                    testitem._dateRange = new DateRange(testitem._testcd, testitem._testitemname, rs.getString("SDATE"), rs.getString("EDATE"));
                    testitem._scoreDivName = rs.getString("SCORE_DIV_NAME");
                    testitemList.add(testitem);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final TestItem[] testitems = new TestItem[testitemList.size()];
            testitemList.toArray(testitems);
            _testcds = new String[testitems.length];
            for (int i = 0; i < testitems.length; i++) {
                _testcds[i] = testitems[i]._testcd;
            }
            return testitems;
        }

//        protected DateRange[] getSemesterDetails(
//                final DB2UDB db2,
//                final Param param,
//                final int max
//        ) {
//            final DateRange[] semesterDetails = new DateRange[max];
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME, T1.SEMESTER_DETAIL "
//                                 + "  ,T1.SDATE "
//                                 + "  ,T1.EDATE "
//                                 + " FROM SEMESTER_DETAIL_MST T1 "
//                                 + " WHERE T1.YEAR = '" + param._ctrlYear + "' "
//                                 + " ORDER BY T1.SEMESTER_DETAIL ";
//                log.debug(" sql = " + sql);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                int i = 0;
//                while (rs.next()) {
//                    semesterDetails[i++] = new DateRange(rs.getString("SEMESTER_DETAIL"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE"));
//                    if (i >= max) {
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return semesterDetails;
//        }

//        protected Map getTestItemMap() {
//            final Map map = new HashMap();
//            for (int i = 0; i < _testItems.length; i++) {
//                if (null != _testItems[i]) {
//                    map.put(_testItems[i]._testcd, _testItems[i]);
//                }
//            }
//            return map;
//        }

        public static String[] get_token(final String strx0, final int f_len, final int f_cnt) {

            if (strx0 == null || strx0.length() == 0) {
                return new String[] {};
            }
            final String strx = StringUtils.replace(StringUtils.replace(strx0, "\r\n", "\n"), "\r", "\n");
            final String[] stoken = new String[f_cnt];        //分割後の文字列の配列
            int slen = 0;                               //文字列のバイト数カウント
            int s_sta = 0;                              //文字列の開始位置
            int ib = 0;
            for (int s_cur = 0; s_cur < strx.length() && ib < f_cnt; s_cur++) {
                //改行マークチェック
                if (strx.charAt(s_cur) == '\n') {
                    stoken[ib] = strx.substring(s_sta, s_cur);
                    ib++;
                    slen = 0;
                    s_sta = s_cur + 1;
                } else{
                    //文字数チェック
                    int blen = 0;
                    try{
                        blen = (strx.substring(s_cur,s_cur+1)).getBytes("MS932").length;
                    } catch (Exception e) {
                        log.fatal("get_token exception", e);
                    }
                    slen += blen;
                    if (slen > f_len) {
                        stoken[ib] = strx.substring(s_sta, s_cur);
                        ib++;
                        slen = blen;
                        s_sta = s_cur;
                    }
                }
            }
            if (slen > 0 && ib < f_cnt) {
                stoken[ib] = strx.substring(s_sta);
            }
            return stoken;

        }

//        /**
//         * @param source 元文字列
//         * @param bytePerLine 1行あたりのバイト数
//         * @return bytePerLineのバイト数ごとの文字列リスト
//         */
//        public static List getTokenList(final String source, final int bytePerLine) {
//            if (source == null || source.length() == 0) {
//                return Collections.EMPTY_LIST;
//            }
//            final List tokenList = new ArrayList();        //分割後の文字列の配列
//            int startIndex = 0;                         //文字列の分割開始位置
//            int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
//            for (int idx = 0; idx < source.length(); idx += 1) {
//                //改行マークチェック
//                if (source.charAt(idx) == '\r') {
//                    continue;
//                }
//                if (source.charAt(idx) == '\n') {
//                    tokenList.add(source.substring(startIndex, idx));
//                    byteLengthInLine = 0;
//                    startIndex = idx + 1;
//                } else {
//                    final int sbytelen = getMS932ByteLength(source.substring(idx, idx + 1));
//                    byteLengthInLine += sbytelen;
//                    if (byteLengthInLine > bytePerLine) {
//                        tokenList.add(source.substring(startIndex, idx));
//                        byteLengthInLine = sbytelen;
//                        startIndex = idx;
//                    }
//                }
//            }
//            if (byteLengthInLine > 0) {
//                tokenList.add(source.substring(startIndex));
//            }
//            return tokenList;
//        }

        private void print1(final Vrw32alp svf, final Param param, final Student student) {
            final String form = "KNJD186B.frm";
            svf.VrSetForm(form, 1);

            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(param._ctrlYear)) + "年度　成 績 通 知 表"); // タイトル
            svf.VrsOut("NAME", student._name); // 氏名
            svf.VrsOut("HR_NAME", student.getHrAttendNo(param)); // 年組番
            svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(_param._schoolName) + " " + StringUtils.defaultString(student._majorname)); // 学校名

            for (int ti = 0; ti < _testItems.length; ti++) {
                final String j = String.valueOf(ti + 1);
                final TestItem item = _testItems[ti];
                if (null == item || TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                    continue;
                }
                svf.VrsOut("EXAM_NAME" + j, item._testitemname); // 考査名称

                final Score score = student.getSubclass(SUBCLASSCD999999).getScore(item._testcd);
                if (null != score) {
                    svf.VrsOut("SCORE_TOTAL_AVE" + j + "", sishaGonyu(score._avg)); // 中間点合計平均
                    svf.VrsOut("AVE_TOTAL_AVE" + j + "", sishaGonyu(score._avgCourseAvg)); // 集団平均合計平均
                    svf.VrsOut("CLASS_RANK" + j + "_1", score._hrRank.getRank(param)); // クラス順位
                    svf.VrsOut("CLASS_RANK_SLASH" + j + "", "/"); // クラス順位スラッシュ
                    svf.VrsOut("CLASS_RANK" + j + "_2", score._hrRank._count); // クラス順位
                    svf.VrsOut("COURSE_RANK" + j + "_1", score._courseRank.getRank(param)); // コース順位
                    svf.VrsOut("COURSE_RANK_SLASH" + j + "", "/"); // コース順位スラッシュ
                    svf.VrsOut("COURSE_RANK" + j + "_2", score._courseRank._count); // コース順位
                }
            }

            if (null != student._attendMap.get(SEMEALL)) {
                Attendance att = (Attendance) student._attendMap.get(SEMEALL);
                svf.VrsOut("LESSON", zeroBlank(att._lesson)); // 授業日数
                svf.VrsOut("SUSPEND", zeroBlank(att._suspend + att._mourning)); // 出停忌引
                svf.VrsOut("MUST", zeroBlank(att._mLesson)); // 出席すべき日数
                svf.VrsOut("ABSENCE", zeroBlank(att._sick)); // 欠席日数
                svf.VrsOut("ATTEND", zeroBlank(att._present)); // 出席日数
                svf.VrsOut("LATE", zeroBlank(att._late)); // 遅刻
                svf.VrsOut("EARLY", zeroBlank(att._early)); // 早退
            }

            final List subclasscdList = new ArrayList(student._subclassMap.keySet());
            if (_param._isNoPrintMoto) {
                for (final Iterator it = subclasscdList.iterator(); it.hasNext();) {
                    final String subclasscd = (String) it.next();
                    final Subclass subclass = student.getSubclass(subclasscd);
                    if (null != subclass && subclass._mst._isMoto) {
                        it.remove(); // 合併元科目をのぞく
                    }
                }
            }
            final List subclassList = new ArrayList();
            for (final Iterator it = subclasscdList.iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final String[] split = StringUtils.split(subclasscd, "-");
                if (null != split && split.length >= 4 && _param._d026List.contains(split[3])) {
                    log.info(" not print subclass = " + subclasscd);
                    continue;
                }
                final Subclass subclass = student.getSubclass(subclasscd);
                subclassList.add(subclass);
            }
            Collections.sort(subclassList);

            final int maxSubclassLine = 15;
            for (int subi = 0; subi < Math.min(subclassList.size(), maxSubclassLine); subi++) {
                final int subcline = subi + 1;
                final Subclass subclass = (Subclass) subclassList.get(subi);

                if (getMS932ByteLength(subclass._mst._subclassabbv) > 12) {
                    svf.VrsOutn("SUBCLASS1_3", subcline, subclass._mst._subclassabbv); // 科目名
                    svf.VrsOutn("SUBCLASS2_3", subcline, subclass._mst._subclassabbv); // 科目名
                } else if (getMS932ByteLength(subclass._mst._subclassabbv) > 8) {
                    svf.VrsOutn("SUBCLASS1_2", subcline, subclass._mst._subclassabbv); // 科目名
                    svf.VrsOutn("SUBCLASS2_2", subcline, subclass._mst._subclassabbv); // 科目名
                } else {
                    svf.VrsOutn("SUBCLASS1_1", subcline, subclass._mst._subclassabbv); // 科目名
                    svf.VrsOutn("SUBCLASS2_1", subcline, subclass._mst._subclassabbv); // 科目名
                }

                for (int ti = 0; ti < _testItems.length; ti++) {
                    final String j = String.valueOf(ti + 1);
                    final TestItem item = _testItems[ti];
                    if (null == item || TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                        continue;
                    }
                    final Score score = subclass.getScore(item._testcd);
                    if (null != score) {
                        svf.VrsOutn("SCORE" + j, subcline, score._score); // 中間点
                        svf.VrsOutn("AVE" + j, subcline, sishaGonyu(score._courseRank._avg)); // 集団平均
                    }
                }

                final SubclassAttendance subatt = (SubclassAttendance) subclass._attendMap.get(SEMEALL);
                if (null != subatt && null != subatt._sick) {
                    svf.VrsOutn("SUBCLASS_ABSENCE", subcline, zeroBlank(subatt._sick.intValue())); // 科目欠課
                }
                final Score hyoteiScore = (Score) subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != hyoteiScore) {
                    svf.VrsOutn("SUBCLASS_DIV", subcline, hyoteiScore._score); // 科目評定
                }
                svf.VrsOutn("SUBCLASS_CREDIT", subcline, param.getCredits(subclass._mst._subclasscode, student._course)); // 科目単位
            }
            final int qualifiedMax = 10;
            final List remarkList = new ArrayList();
            for (int j = 0; j < Math.min(qualifiedMax, student._qualifiedList.size()); j++) {
                final int line = j + 1;
                final Qualified qualified = (Qualified) student._qualifiedList.get(j);
                svf.VrsOutn("QUALIFY_NAME", line, StringUtils.defaultString(qualified._qualifiedName) + " " + StringUtils.defaultString(qualified._rankName)); // 資格名称
                svf.VrsOutn("QUALIFY_DATE", line, KNJ_EditDate.h_format_JP(qualified._regddate)); // 資格年月日
                if (null != qualified._remark) {
                    remarkList.add(qualified._remark);
                }
            }
            for (int j = 0; j < Math.min(qualifiedMax, student._detailList.size()); j++) {
                final int line = j + 1;
                final DetailHist detailHist = (DetailHist) student._detailList.get(j);
                svf.VrsOutn("PRIZE_NAME", line, StringUtils.defaultString(detailHist._detailcdName)); // 資格名称
                svf.VrsOutn("PRIZE_DATE", line, KNJ_EditDate.h_format_JP(detailHist._detailSdate)); // 資格年月日
                if (null != detailHist._remark) {
                    remarkList.add(detailHist._remark);
                }
            }
            for (int j = 0; j < Math.min(qualifiedMax, remarkList.size()); j++) {
                final int line = j + 1;
                svf.VrsOutn("REMARK", line, (String) remarkList.get(j)); // 備考
            }
            svf.VrEndPage();
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 57090 $ $Date: 2017-11-14 16:54:55 +0900 (火, 14 11 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _ctrlSemester;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _sdate;
        final String _edate;

        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        final String _rankDiv = null; // 順位の基準点 1:総合点 2:平均点

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final Form _form;

        private Map _semesterMap;
        private Map _subclassMst;
        private Map _creditMst;

        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private boolean _isNoPrintMoto;
        final Map _attendParamMap;

        final List _d026List = new ArrayList();

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HRCLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _edate = KNJ_EditDate.H_Format_Haifun(request.getParameter("EDATE"));
//            _rankDiv = request.getParameter("RANK_DIV");
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            SCHOOLKIND = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));
            SCHOOLCD = request.getParameter("SCHOOLCD");

            _semesterMap = loadSemester(db2, _ctrlYear, _grade);
            setCertifSchoolDat(db2);

            _definecode = createDefineCode(db2);
            setSubclassMst(db2);
            setCreditMst(db2);
            loadNameMstD026(db2);
//            setRecordMockOrderSdivDat(db2);
            loadNameMstD016(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");

            _form = new Form(db2, this);
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
            PreparedStatement ps = null;
            ResultSet rs = null;
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namespare1 = rs.getString("NAMESPARE1");
                    if ("Y".equals(namespare1)) _isNoPrintMoto = true;
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
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

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '104' ");
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
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        " + field + " ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _ctrlYear + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade + "' ");
                ps = db2.prepareStatement(stb.toString());
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

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null, false, false, new Integer(999999));
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
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " VALUE(T1.SHOWORDER3, 999999) AS SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final String showOrder = rs.getString("SHOWORDER3");
                    final SubclassMst mst = new SubclassMst(rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), isSaki, isMoto, Integer.valueOf(showOrder));
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getCredits(final String subclasscd, final String course) {
            return (String) _creditMst.get(subclasscd + ":" + course);
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMst = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD,  ";
                sql += " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE,  ";
                sql += " T1.CREDITS  ";
                sql += " FROM CREDIT_MST T1 ";
                sql += " WHERE T1.YEAR = '" + _ctrlYear + "' ";
                sql += "   AND T1.GRADE = '" + _grade + "' ";
                sql += "   AND T1.CREDITS IS NOT NULL";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _creditMst.put(rs.getString("SUBCLASSCD") + ":" + rs.getString("COURSE"), rs.getString("CREDITS"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            final String field;
            if ("1".equals(_semester)) {
                field = "ABBV1";
            } else if ("2".equals(_semester)) {
                field = "ABBV2";
            } else {
                field = "ABBV3";
            }
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1' OR NAMESPARE1 = '1' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _d026List.add(rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

//        public List getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
//            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
//            final String[] keys = {grade + "-" + coursecd + "-" + majorcd, "00" + "-" + coursecd + "-" + majorcd, grade + "-" + "0" + "-" + "000", "00" + "-" + "0" + "-" + "000"};
//            for (int i = 0; i < keys.length; i++) {
//                log.info(" set key = " + keys[i]);
//                final List rtn = (List) _recordMockOrderSdivDatMap.get(keys[i]);
//                if (null != rtn) return rtn;
//            }
//            return Collections.EMPTY_LIST;
//        }

//        private void setRecordMockOrderSdivDat(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            _recordMockOrderSdivDatMap = new HashMap();
//            try {
//                String sql = "";
//                sql += " SELECT ";
//                sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
//                sql += " T1.SEQ,  ";
//                sql += " T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD ";
//                sql += " FROM RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
//                sql += " WHERE T1.YEAR = '" + _year + "' ";
//                sql += "   AND T1.TEST_DIV = '1' ";
//                sql += " ORDER BY ";
//                sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
//                sql += " T1.SEQ  ";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String key = rs.getString("GRADE") + "-" + rs.getString("COURSECD") + "-" + rs.getString("MAJORCD");
//                    if (null == _recordMockOrderSdivDatMap.get(key)) {
//                        _recordMockOrderSdivDatMap.put(key, new ArrayList());
//                    }
//                    ((List) _recordMockOrderSdivDatMap.get(key)).add(rs.getString("TESTCD"));
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }
    }
}
