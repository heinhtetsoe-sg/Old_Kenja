// kanji=漢字
/*
 * $Id$
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186M {
    private static final Log log = LogFactory.getLog(KNJD186M.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";

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
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            log.fatal("$Revision: 68659 $ $Date: 2019-07-11 17:04:50 +0900 (木, 11 7 2019) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(request, db2);

            printMain(db2, svf, param);

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
            param._form.print(svf, student);
        }
        _hasData = true;
    }

    private static Map rsToMap(final ResultSet rs) throws SQLException {
        final Map m = new HashMap();
        final ResultSetMetaData meta = rs.getMetaData();
        for (int col = 1; col <= meta.getColumnCount(); col++) {
            final String key = meta.getColumnName(col);
            final String val = rs.getString(col);
            m.put(key, val);
        }
        return m;
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
        final Form form = param._form;

        form.init(db2);

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


        Score.load(db2, param, studentList, stbtestcd);
        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }
        Student.setClub(db2, param, studentMap);
        Student.setClubHdetail(db2, param, studentMap);
        Student.setCommittee(db2, param, studentMap);
        Student.setQualifiedList(db2, param, studentList);
        Student.setHreportremark(db2, param, studentMap);
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
        final String _staffName;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _coursecodename;
        final String _attendno;
        final String _hrClassName1;
        final String _curriculumYear;
        final Map _attendMap;
        final Map _subclassMap;
        final String _entyear;
        final List _clubHdetail = new ArrayList();
        final Map _semesCommitteeListMap = new HashMap();
        final Map _semesClubListMap = new HashMap();
        final Map _hreportRemarkAttendrecRemarkMap = new HashMap();
        List _qualifiedList;
        Map _debugDataMap = new HashMap();

//        private String _totalStudyKatsudo;
//        private String _totalStudyHyoka;

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String coursecodename, final String hrClassName1, final String entyear, final String curriculumYear) {
            _schregno = schregno;
            _name = name;
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
            _entyear = entyear;
            _curriculumYear = curriculumYear;
            _attendMap = new TreeMap();
            _subclassMap = new TreeMap();
//            _proficiencySubclassScoreList = new ArrayList();
        }

        SubClass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                return new SubClass(new SubclassMst(null, null, null, null, null, null, false, false, 999999));
            }
            return (SubClass) _subclassMap.get(subclasscd);
        }

        public String getAttendNo(final Param param) {
            if (NumberUtils.isDigits(_attendno)) {
                return String.valueOf(Integer.parseInt(_attendno)) + " 番";
            }
            return StringUtils.defaultString(_attendno);
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
            stb.append("            ,W10.COURSECODENAME ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("            ,EGHIST.CURRICULUM_YEAR ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
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
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = W1.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR ");
            stb.append("                  AND GDAT.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
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
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("COURSECODENAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR"), rs.getString("CURRICULUM_YEAR")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

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

        public static void setClubHdetail(final DB2UDB db2, final Param param,
                final Map studentMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.DETAIL_DATE, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.MEET_NAME, ");
            stb.append("     T1.DOCUMENT ");
            stb.append(" FROM SCHREG_CLUB_HDETAIL_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     FISCALYEAR(T1.DETAIL_DATE) = '" + param._year + "' ");
            stb.append("     AND T1.DETAIL_DATE <= '" + param._date + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.DETAIL_DATE ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final String remark = StringUtils.defaultString(rs.getString("MEET_NAME")) + StringUtils.defaultString(rs.getString("DOCUMENT"));
                        if (StringUtils.isEmpty(remark)) {
                            continue;
                        }
                        if (!student._clubHdetail.contains(remark)) {
                            student._clubHdetail.add(remark);
                        }
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

        public static void setCommittee(final DB2UDB db2, final Param param,
                final Map studentMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if (param._isKashiwara) {
                stb.append("     T1.SEMESTER, ");
            }
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD, ");
            stb.append("     T1.CHARGENAME, ");
            stb.append("     T2.COMMITTEENAME ");
            if (param._isKashiwara) {
                stb.append("     ,T3.NAME1 AS SEMESTERNAME ");
            }
            stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J004' ");
            stb.append("     AND T3.NAMECD2 = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._isKashiwara) {
                stb.append("     AND (T1.SEMESTER = '9' OR T1.SEMESTER <= '" + param.getRegdSemester() + "') ");
            }
            stb.append("     AND T1.COMMITTEE_FLG IN ('1', '2') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            if (param._isKashiwara) {
                stb.append("     CASE WHEN T1.SEMESTER = '9' THEN '0' ELSE T1.SEMESTER END, ");
            }
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

//                        final String semester = rs.getString("SEMESTER");
//                        final String committeeFlg = rs.getString("COMMITTEE_FLG");
                        String name = null;
//                        if ("2".equals(committeeFlg)) {
//                            name = rs.getString("CHARGENAME");
//                        } else if ("1".equals(committeeFlg)) {
                        if (param._isKashiwara) {
                            name = rs.getString("SEMESTERNAME") + " " + rs.getString("COMMITTEENAME");
                        } else {
                            name = rs.getString("COMMITTEENAME");
                        }
//                        }
                        if (StringUtils.isBlank(name)) {
                            continue;
                        }
//                        getMappedList(student._semesCommitteeListMap, semester).add(name);

                        if (!getMappedList(student._semesCommitteeListMap, "9").contains(name)) {
                            getMappedList(student._semesCommitteeListMap, "9").add(name);
                        }

                        getMappedList(student._debugDataMap, "COMMITTEE").add(rsToMap(rs));
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
                final Map studentMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     TSEM.SEMESTER, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME, ");
            stb.append("     CASE WHEN (T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE ");
            stb.append("                OR VALUE(T1.EDATE, '9999-12-31') BETWEEN TSEM.SDATE AND TSEM.EDATE ");
            stb.append("                OR TSEM.SDATE <= T1.SDATE AND T1.EDATE <= TSEM.EDATE ");
            stb.append("                OR T1.SDATE <= TSEM.SDATE AND TSEM.EDATE <=  VALUE(T1.EDATE, TSEM.EDATE) ");
            stb.append("               ) ");
            if (param._isKashiwara) {
                stb.append("           AND T1.EDATE IS NULL ");
            }
            stb.append("          THEN 1 ");
            stb.append("     END AS FLG ");
            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
            stb.append("     AND TSEM.SEMESTER <> '9' ");
            stb.append("     AND TSEM.SEMESTER <= '" + param.getRegdSemester() + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final String clubname = rs.getString("CLUBNAME");
                        final String flg = rs.getString("FLG");

                        if (!"1".equals(flg) || StringUtils.isBlank(clubname)) {
                            continue;
                        }
                        if (!getMappedList(student._semesClubListMap, "9").contains(clubname)) {
                            getMappedList(student._semesClubListMap, "9").add(clubname);
                        }

                        getMappedList(student._debugDataMap, "CLUB").add(rsToMap(rs));
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

        private static void setQualifiedList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("    T2.QUALIFIED_NAME ");
                stb.append("    , NMH312.NAME1 AS RANK_NAME ");
                stb.append("    , T1.REGDDATE ");
                stb.append("    , T1.SEQ ");
                stb.append(" FROM SCHREG_QUALIFIED_HOBBY_DAT T1 ");
                stb.append(" INNER JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
                stb.append(" LEFT JOIN NAME_MST NMH312 ON NMH312.NAMECD1 = 'H312' ");
                stb.append("     AND NMH312.NAMECD2 = T1.RANK ");
                stb.append(" WHERE ");
                stb.append(" T1.YEAR = '" + param._year + "' ");
                stb.append(" AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("   T1.REGDDATE ");
                stb.append("   , T1.SEQ ");

                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    student._qualifiedList = new ArrayList();

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String date = KNJ_EditDate.h_format_JP_MD(rs.getString("REGDDATE"));
                        final String remark = date + "　" + StringUtils.defaultString(rs.getString("QUALIFIED_NAME")) + StringUtils.defaultString(rs.getString("RANK_NAME"));
                        student._qualifiedList.add(remark);
                    }
                    if (param._isKashiwara) {
                        final List reversed = new ArrayList();

                        for (int i = student._qualifiedList.size() - 1; i >= 0; i--) {
                            reversed.add(student._qualifiedList.get(i));
                        }

                        student._qualifiedList = reversed;
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static void setHreportremark(final DB2UDB db2, final Param param,
                final Map studentMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDREC_REMARK ");
            stb.append(" FROM HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param.getRegdSemester() + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        student._hreportRemarkAttendrecRemarkMap.put(rs.getString("SEMESTER"), rs.getString("ATTENDREC_REMARK"));
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

        private static String addNumber(final String i1, final String i2) {
            if (!NumberUtils.isDigits(i1)) return i2;
            if (!NumberUtils.isDigits(i2)) return i1;
            return String.valueOf((!NumberUtils.isDigits(i1) ? 0 : Integer.parseInt(i1)) + (!NumberUtils.isDigits(i2) ? 0 : Integer.parseInt(i2)));
        }

        public String getTotalKekka(final String semester, final boolean isSogaku) {
            String rtn = null;
            for (final Iterator it = _subclassMap.values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                final SubclassAttendance subatt = (SubclassAttendance) subclass._attendMap.get(semester);
                if (null != subatt && null != subatt._sick && subatt._sick.intValue() > 0 && null != subclass && null != subclass._mst) {
                    if (isSogaku) {
                        if (subclass._mst._subclasscode.startsWith("90")) {
                            rtn = addNumber(rtn, subatt._sick.toString());
                        }
                    } else {
                        //if (!subclass._mst._subclasscode.startsWith("90")) {
                            rtn = addNumber(rtn, subatt._sick.toString());
                        //}
                    }
                }
            }
            return rtn;
        }
    }

    private static class Attendance {

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
        final int _koudome;
        final int _virus;
        DateRange _dateRange;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays,
                final int koudome,
                final int virus
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
            _koudome = koudome;
            _virus = virus;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
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
                        param._year,
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
                                rs.getInt("TRANSFER_DATE"),
                                rs.getInt("KOUDOME"),
                                rs.getInt("VIRUS")
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
            final SubClass s = (SubClass) o;
            return _mst.compareTo(s._mst);
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
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
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
                                final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subClass);
                            }
                            final SubClass subClass = student.getSubClass(subclasscd);
                            subClass._attendMap.put(dateRange._key, subclassAttendance);
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
        final String _scoreDi;
        final String _passScore;
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

        Score(
                final String score,
                final String scoreDi,
                final String passScore,
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
            _scoreDi = scoreDi;
            _passScore = passScore;
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

        private static void load(final DB2UDB db2, final Param param, final List studentList, final StringBuffer stbtestcd) {
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
                            rs.getString("SCORE_DI"),
                            rs.getString("PASS_SCORE"),
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

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
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

        private static String sqlScore(final Param param, final StringBuffer stbtestcd) {
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
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
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
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
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
            stb.append("    ,W3.VALUE_DI AS SCORE_DI ");
            stb.append("    ,VALUE(T4.PASS_SCORE, 30) AS PASS_SCORE ");
            stb.append("    ,W2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    LEFT JOIN SCHNO_A W1 ON W1.SCHReGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");

            stb.append("        LEFT JOIN PERFECT_RECORD_SDIV_DAT T4 ON T4.YEAR = W3.YEAR ");
            stb.append("     AND T4.TESTKINDCD = W3.TESTKINDCD ");
            stb.append("     AND T4.TESTITEMCD = W3.TESTITEMCD ");
            stb.append("     AND T4.SCORE_DIV = W3.SCORE_DIV ");
            stb.append("     AND T4.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T4.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T4.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("     AND T4.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND (T4.DIV = '01' ");
            stb.append("       OR T4.DIV = '02' AND T4.GRADE = W1.GRADE ");
            stb.append("       OR T4.DIV = '03' AND T4.GRADE = W1.GRADE AND T4.COURSECD = W1.COURSECD AND T4.MAJORCD = W1.MAJORCD AND T4.COURSECODE = W1.COURSECODE ");
            stb.append("         ) ");

            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("            AND (SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("              OR SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN W3.SCORE END AS SLUMP_SCORE ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END ");
            stb.append("    END AS SLUMP_SCORE_KANSAN ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
            stb.append("            AND W3.SCORE IS NOT NULL ");
            stb.append("     ) ");
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
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
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SLUMP ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T33.PASS_SCORE ");
            stb.append("        ,T33.SCORE_DI ");
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
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_SCORE ");
            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");

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

            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
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
            return "Rank( rank = " + _rank + ", avg = " + _avg + ")";
        }
    }

    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _semester;
        public String _scoreDivName;
        public DateRange _dateRange;
        public boolean _printScore;
        public String toString() {
            return "TestItem(" + _testcd + ":" + _testitemname + ")";
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
        final String _subclasscode;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        final int _showorder3;
        public SubclassMst(final String classcd, final String subclasscode, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final boolean isSaki, final boolean isMoto, final int showorder3) {
            _classcd = classcd;
            _subclasscode = subclasscode;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _showorder3 = showorder3;
        }
        public int compareTo(final Object o) {
            final SubclassMst s = (SubclassMst) o;
            if (_showorder3 < s._showorder3) {
                return -1;
            } else if (s._showorder3 < _showorder3) {
                return 1;
            }
            return StringUtils.defaultString(_subclasscode).compareTo(StringUtils.defaultString(s._subclasscode));
        }
    }

    private static class Form {

        String[] _testcds;
        TestItem[] _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendSubclassRanges;
        private Param _param;

        protected Param param() {
            return _param;
        }

        protected void initDebug() {
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
//                    if (SEMEALL.equals(dateRange._key) && !SEMEALL.equals(param()._semester)) {
//                        continue;
//                    }
                    attendances[i] = (Attendance) student._attendMap.get(dateRange._key);
                    if (null != attendances[i]) {
                        attendances[i]._dateRange = dateRange;
                    }
                }
            }
            return attendances;
        }

        protected TestItem[] getTestItems(
                final DB2UDB db2,
                final Param param,
                final String[] testcds
        ) {
            final TestItem[] testitems = new TestItem[testcds.length];
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD "
                                 +  " ,TESTITEMNAME "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
                                 + "  ,T2.SDATE "
                                 + "  ,T2.EDATE "
                                 +  " ,CASE WHEN T1.SEMESTER <= '" + param._semester + "' THEN 1 ELSE 0 END AS PRINT "
                                 +  " ,NMD053.NAME1 AS SCORE_DIV_NAME "
                                 +  "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 "
                                 +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
                                 +  " AND T2.SEMESTER = T1.SEMESTER "
                                 +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                                 +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' "
                                 +  "WHERE T1.YEAR = '" + param._year + "' "
                                 +  "  AND T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV IN " + SQLUtils.whereIn(true, testcds) + " "
                                 +  " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int i = -1;
                    final String testcd = rs.getString("TESTCD");
                    for (int j = 0; j < testcds.length; j++) {
                        if (null != testcds[j] && testcds[j].equals(testcd)) {
                            i = j;
                        }
                    }
                    if (-1 == i) {
                        continue;
                    }
                    final TestItem testitem = new TestItem();
                    testitem._testcd = testcd;
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._semester = rs.getString("SEMESTER");
                    testitem._dateRange = new DateRange(testitem._testcd, testitem._testitemname, rs.getString("SDATE"), rs.getString("EDATE"));
                    testitem._printScore = "1".equals(rs.getString("PRINT"));
                    testitem._scoreDivName = rs.getString("SCORE_DIV_NAME");
                    testitems[i] = testitem;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (int i = 0; i < testcds.length; i++) {
                final String testcd = testcds[i];
                if (null == testitems[i]) {
                    log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + testcd);
                }
            }
            return testitems;
        }

        protected DateRange[] getSemesterDetails(
                final DB2UDB db2,
                final Param param,
                final int max
        ) {
            final DateRange[] semesterDetails = new DateRange[max];
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME, T1.SEMESTER_DETAIL "
                                 + "  ,T1.SDATE "
                                 + "  ,T1.EDATE "
                                 + " FROM SEMESTER_DETAIL_MST T1 "
                                 + " WHERE T1.YEAR = '" + param._year + "' "
                                 + " ORDER BY T1.SEMESTER_DETAIL ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int i = 0;
                while (rs.next()) {
                    semesterDetails[i++] = new DateRange(rs.getString("SEMESTER_DETAIL"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE"));
                    if (i >= max) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterDetails;
        }

        protected Map getTestItemMap() {
            final Map map = new HashMap();
            for (int i = 0; i < _testItems.length; i++) {
                if (null != _testItems[i]) {
                    map.put(_testItems[i]._testcd, _testItems[i]);
                }
            }
            return map;
        }

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

        /**
         * @param source 元文字列
         * @param bytePerLine 1行あたりのバイト数
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        public static List getTokenList(final String source, final int bytePerLine) {
            if (source == null || source.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            final List tokenList = new ArrayList();        //分割後の文字列の配列
            int startIndex = 0;                         //文字列の分割開始位置
            int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
            for (int idx = 0; idx < source.length(); idx += 1) {
                //改行マークチェック
                if (source.charAt(idx) == '\r') {
                    continue;
                }
                if (source.charAt(idx) == '\n') {
                    tokenList.add(source.substring(startIndex, idx));
                    byteLengthInLine = 0;
                    startIndex = idx + 1;
                } else {
                    final int sbytelen = getMS932ByteLength(source.substring(idx, idx + 1));
                    byteLengthInLine += sbytelen;
                    if (byteLengthInLine > bytePerLine) {
                        tokenList.add(source.substring(startIndex, idx));
                        byteLengthInLine = sbytelen;
                        startIndex = idx;
                    }
                }
            }
            if (byteLengthInLine > 0) {
                tokenList.add(source.substring(startIndex));
            }
            return tokenList;
        }

        protected Rank getGroupDivRank(final Score score) {
            return "4".equals(param()._groupDiv) ? score._majorRank : "3".equals(param()._groupDiv) ? score._courseRank : score._gradeRank;
        }

        protected String getGroupDivName() {
            return "4".equals(param()._groupDiv) ? "学科順位" : "3".equals(param()._groupDiv) ? "コース順位" : "学年順位";
        }


        void init(final DB2UDB db2) {

            _testcds = new String[] {"1990008", "2990008", "3990008", TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI};

            _testItems = getTestItems(db2, param(), _testcds);
            final int attendCount = 4; // 出欠欄
            _attendRanges = new DateRange[attendCount];
            _attendSubclassRanges = new DateRange[attendCount];
            for (int i = 0; i < 3; i++) {
                final String seme = String.valueOf(i + 1);
                if (Integer.parseInt(seme) <= Integer.parseInt(_param._semester)) {
                    final Semester semester = (Semester) _param._semesterMap.get(seme);
                    if (null != semester) {
                        _attendRanges[i] = semester._dateRange;
                        _attendSubclassRanges[i] = _attendRanges[i];
                    }
                }
            }
            if (null != param().getSemester(SEMEALL)) {
                final Semester seme9 = param().getSemester(SEMEALL);
                final int i = attendCount - 1;
                _attendRanges[i] = new DateRange(SEMEALL, "", seme9._dateRange._sdate, _param._date);
                _attendSubclassRanges[i] = _attendRanges[i];
            }

            initDebug();
        }

        void print(final Vrw32alp svf, final Student student) {

            final String form = "KNJD186M.frm";
            svf.VrSetForm(form, 4);
            printHeader(svf, student);
            printAttendance(svf, student);
            printCommitteeClubQualified(svf, student);
            printSubclass(svf, student);
        }

        private void printSubclass(final Vrw32alp svf, final Student student) {

            final String[] semes = {"1", "2", "3", "9"};
            final Map printSubclassMap = new HashMap(student._subclassMap);
            printSubclassMap.remove("333333");
            printSubclassMap.remove("555555");
            printSubclassMap.remove("999999");
            printSubclassMap.remove("99999B");
            final List subclassList = new ArrayList(printSubclassMap.values());
            Collections.sort(subclassList);
            for (int i = 0; i < Math.min(subclassList.size(), 17); i++) {
                final SubClass subclass = (SubClass) subclassList.get(i);
                final String credit = (String) _param._creditMst.get(subclass._mst._subclasscode + ":" + student._course);
                if ("90".equals(subclass._mst._classcd)) {
                    continue;
                }

                svf.VrsOut("COMP_CREDIT", credit); // 単位数

                if (StringUtils.defaultString(subclass._mst._subclassabbv).length() <= 6) {
                    svf.VrsOut("SUBCLASS1", subclass._mst._subclassabbv); // 科目
                } else {
                    svf.VrsOut("SUBCLASS2_1", subclass._mst._subclassabbv.substring(0, 6)); // 科目
                    svf.VrsOut("SUBCLASS2_2", subclass._mst._subclassabbv.substring(6, Math.min(subclass._mst._subclassabbv.length(), 12))); // 科目
                }

                for (int sfi = 0; sfi < semes.length; sfi++) {
                    final String seme = semes[sfi];
                    if (Integer.parseInt(seme) > Integer.parseInt(_param._semester) && !SEMEALL.equals(seme)) {
                        continue;
                    }
                    for (int ti = 0; ti < _testItems.length; ti++) {
                        final TestItem item = (TestItem) _testItems[ti];
                        if (null == item) {
                            continue;
                        }
                        if (item._semester.equals(seme)) {
                            final Score score = subclass.getScore(item._testcd);
                            if (null != score) {
                                if (TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                                    svf.VrsOut("GRAD_VALUE", score._score); // 学年評定
                                } else {
                                    svf.VrsOut("SCORE" + semes[sfi], score._score); // 素点
                                    if (NumberUtils.isNumber(score._score) && NumberUtils.isNumber(score._passScore) && Double.parseDouble(score._score)< Double.parseDouble(score._passScore)) {
                                        svf.VrAttribute("SCORE" + semes[sfi], "UnderLine=(0,1,1)"); // 赤点に下線
                                    }
                                }
                            }
                        }
                    }

                    final SubclassAttendance subatt = subclass.getAttendance(seme);
                    if (null != subatt) {
                        svf.VrsOut("KEKKA" + semes[sfi], null == subatt._sick || subatt._sick.floatValue() == 0.0 ? "" : subatt._sick.toString()); // 欠課時数
                    }
                }
                svf.VrEndRecord();
            }
        }

        private void printCommitteeClubQualified(final Vrw32alp svf, final Student student) {
            svfVrsoutnRepeat(svf, "PRIZE", KNJ_EditKinsoku.getTokenList(mkString(student._clubHdetail, "\n"), 50)); // 資格
            svfVrsoutnRepeat(svf, "QUALIFY", KNJ_EditKinsoku.getTokenList(mkString(student._qualifiedList, "\n"), 50)); // 資格

            final StringBuffer committee = new StringBuffer();
            for (final Iterator it = student._semesCommitteeListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
//                final String semes = (String) e.getKey();
                final List committees = (List) e.getValue();
//                final String semestername;
//                if ("9".equals(semes)) {
//                    semestername = "通年";
//                } else {
//                    final Semester semester = param().getSemester(semes);
//                    if (null == semester) {
//                        continue;
//                    }
//                    semestername = semester._semestername;
//                }
//                committee.append(semestername).append("　").append(mkString(committees, "、"));
                committee.append(mkString(committees, "\n"));
            }
            svfVrsoutnRepeat(svf, "CIMMITTEE", KNJ_EditKinsoku.getTokenList(committee.toString(), 50)); // 部活


            final StringBuffer club = new StringBuffer();
            for (final Iterator it = getMappedList(student._semesClubListMap, SEMEALL).iterator(); it.hasNext();) {
                if (club.length() > 0) {
                    club.append("\n");
                }
                club.append(StringUtils.defaultString((String) it.next()));
            }
            svfVrsoutnRepeat(svf, "CLUB", KNJ_EditKinsoku.getTokenList(club.toString(), 50)); // 部活
        }

        public void svfVrsoutnRepeat(final Vrw32alp svf, final String field, final List token) {
            for (int i = 0; i < token.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) token.get(i));
            }
        }

        private String mkString(final List list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < list.size(); i++) {
                final String val = (String) list.get(i);
                if (null == val) {
                    continue;
                }
                if (stb.length() > 0) {
                    stb.append(comma);
                }
                stb.append(val);
            }
            return stb.toString();
        }

        private void printAttendance(final Vrw32alp svf, final Student student) {
            final Attendance[] attendances = getAttendances(student);
            final String[] semes = {"1", "2", "3", "9"};
            for (int i = 0; i < semes.length; i++) {
                final Attendance att = attendances[i];
                if (null != att && att._lesson != 0) {
                    final int line = i + 1;
                    svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    svf.VrsOutn("MOURNING", line, String.valueOf(att._suspend + att._mourning + att._virus + att._koudome)); // 出停・忌引等日数
                    svf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
                    svf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    svf.VrsOutn("ABSENCE", line, String.valueOf(att._absent)); // 欠席日数
                    svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                    svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数

                    final String remark = (String) student._hreportRemarkAttendrecRemarkMap.get(String.valueOf(semes[i]));
                    if (null != remark) {
                        final String[] split = StringUtils.split(remark, "\n");
                        int maxlen = 0;
                        for (int j = 0; j < split.length; j++) {
                            maxlen = Math.max(maxlen, getMS932ByteLength(split[j]));
                        }
                        if (split.length > 2 || maxlen > 30) {
                            svf.VrsOutn("ATTEND_REMARK2_4", line, remark); // 出欠備考
                        } else if (split.length > 1) {
                            svf.VrsOutn("ATTEND_REMARK2_2", line, remark); // 出欠備考
                        } else {
                            if (getMS932ByteLength(remark) < 20) {
                                svf.VrsOutn("ATTEND_REMARK1", line, remark); // 出欠備考
                            } else if (getMS932ByteLength(remark) < 30) {
                                svf.VrsOutn("ATTEND_REMARK2_1", line, remark); // 出欠備考
                            } else {
                                svf.VrsOutn("ATTEND_REMARK2_2", line, remark); // 出欠備考
                            }
                        }
                    }
                }
            }
        }

        private void printHeader(final Vrw32alp svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo); // 年度
            svf.VrsOut("HR_NAME", hrnameSpaced(student._hrName) + "　" + student.getAttendNo(_param)); // 年組番
            svf.VrsOut("COURSE", student._coursecodename); // 課程、学科
            svf.VrsOut("NAME", student._name); // 生徒氏名

            svf.VrsOut("SCHOOL_NAME", param()._schoolName); // 学校名
            svf.VrsOut("STAFFNAME", param()._hrJobName + "　" +  student._staffName); // 担任名
//            svf.VrsOut("STAFFBTM", null); //
//            svf.VrsOut("STAFFBTM_C", null); //

            svf.VrsOut("SOGO_SUBCLASSNAME", param().getSogoSubclassname(student._curriculumYear)); // 学校名

            final SubClass subClass999999 = student.getSubClass(SUBCLASSCD999999);
            if (SEMEALL.equals(_param._semester)) {
                if (null != subClass999999) {
                    final Score gakunenHyotei = subClass999999.getScore(TESTCD_GAKUNEN_HYOTEI);
                    if (null != gakunenHyotei) {
                        svf.VrsOut("GRAD_AVERAGE", sishaGonyu(gakunenHyotei._avg)); // 平均（学年評定）
                    }
                }
            }
//          svf.VrsOut("CLASS_GRAD_RANK", null); // クラス学年評定席次
//          svf.VrsOut("GRAD_RANK", null); // 学年評定席次

            final String[] semes = {"1", "2", "3", "9"};
            for (int sfi = 0; sfi < semes.length; sfi++) {
                final Semester semester = _param.getSemester(semes[sfi]);
                if (null != semester) {
                    svf.VrsOut("SEM_NAME" + semes[sfi], semester._semestername); // 学期名
                    svf.VrsOutn("SEMESTERNAME", sfi + 1, semester._semestername); // 学期名（出欠）
                }
                for (int i = 0; i < _testItems.length; i++) {
                    final TestItem item = (TestItem) _testItems[i];
                    if (null == item) {
                        continue;
                    }
                    if (item._testcd.equals(TESTCD_GAKUNEN_HYOTEI)) {
                        continue;
                    }
                    if (item._semester.equals(semes[sfi])) {
                        svf.VrsOut("SEM_TESTNAME" + semes[sfi], item._testitemname); // テスト名
                    }
                }

                if (Integer.parseInt(semes[sfi]) <= Integer.parseInt(_param._semester)) {
                    if (null != subClass999999) {
                        final String testcd = semes[sfi] + "990008";
                        final Score score = (Score) subClass999999._scoreMap.get(testcd);
                        log.info(" testcd = " + testcd + ", score = " + score);
                        if (null != score) {
                            svf.VrsOut("TOTAL" + semes[sfi], score._score); // 総点
                            svf.VrsOut("AVERAGE" + semes[sfi], sishaGonyu(score._avg)); // 平均
                            final Rank rank = "1".equals(_param._groupDiv) ? score._gradeRank : score._courseRank;
                            log.info(" rank = " + rank);
                            svf.VrsOut("RANK" + semes[sfi], rank.getRank(param())); // 席次
                            svf.VrsOut("CLASS_RANK" + semes[sfi], score._hrRank.getRank(param())); // クラス席次
                        }
                    }
                }

                if (Integer.parseInt(semes[sfi]) <= Integer.parseInt(_param._semester) || SEMEALL.equals(semes[sfi])) {
                    svf.VrsOut("TOTAL_ACT_KEKKA" + semes[sfi], student.getTotalKekka(semes[sfi], true)); // 欠課時数
                    svf.VrsOut("TOTAL_KEKKA" + semes[sfi], student.getTotalKekka(semes[sfi], false)); // 欠課時数
                }
            }

            final Map printSubclassMap = new HashMap(student._subclassMap);
            printSubclassMap.remove("333333");
            printSubclassMap.remove("555555");
            printSubclassMap.remove("999999");
            printSubclassMap.remove("99999B");
            final List subclassList = new ArrayList(printSubclassMap.values());
            for (int i = 0; i < Math.min(subclassList.size(), 17); i++) {
                final SubClass subclass = (SubClass) subclassList.get(i);
                final String credit = (String) _param._creditMst.get(subclass._mst._subclasscode + ":" + student._course);
                if ("90".equals(subclass._mst._classcd)) {
                    svf.VrsOut("TOTAL_ACT", credit); // 総学
                }
            }
        }

        private String hrnameSpaced(final String hrname) {
            if (null == hrname || hrname.length() == 0) {
                return "";
            }
            final StringBuffer stb = new StringBuffer();
            final int idxNen = hrname.indexOf("年");
            final int idxKumi = hrname.indexOf("組");
            if (0 < idxNen && idxNen < idxKumi) {
                stb.append(hrname.substring(0, idxNen));
                stb.append(" 年 ");
                stb.append(hrname.substring(idxNen + 1, idxKumi));
                stb.append(" 組");
            } else {
                String space = "";
                for (int i = 0; i < hrname.length(); i++) {
                    stb.append(space).append(hrname.charAt(i));
                    space = " ";
                }
            }
            return stb.toString();
        }

    }

    protected static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;

        final String _grade;
        final String _gradeCd;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _date;

        final String _groupDiv; // 総合順位出力 1:学年 4:学科 3:コース
        final String _rankDiv; // 順位の基準点 1:総合点 2:平均点
        final String _tutisyoPrintKariHyotei; // 仮評定を表示する

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final String _nendo;
        private final Form _form;

        private Map _semesterMap;
        private Map _subclassMst;
        private Map _creditMst;

        private KNJSchoolMst _knjSchoolMst;

        private String _avgDiv;
        private String _sidouHyoji;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private boolean _isNoPrintMoto;
        final boolean _isKeiai;
        final boolean _isKashiwara;
        final Map _attendParamMap;


        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _groupDiv = "3"; // request.getParameter("GROUP_DIV");
            _rankDiv = request.getParameter("RANK_DIV");
            _tutisyoPrintKariHyotei = request.getParameter("tutisyoPrintKariHyotei");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _form = new Form();
            _form._param = this;

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _definecode = createDefineCode(db2);
            setSubclassMst(db2);
            setCreditMst(db2);
            loadNameMstD016(db2);

            _isKeiai = "30270254001".equals(getNameMstZ010Name2(db2));
            _isKashiwara = "30270247001".equals(getNameMstZ010Name2(db2));


            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
        }

        public String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSeme : _semester;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            return definecode;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
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

        private String getNameMstZ010Name2(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private String getSogoSubclassname(final String curriculumYear) {
            final int tankyuStartYear = 2019;
            boolean isTankyu = false;
            if (NumberUtils.isDigits(curriculumYear)) {
                isTankyu = tankyuStartYear <= Integer.parseInt(curriculumYear);
            } else {
                final int year = Integer.parseInt(_year);
                final int gradeCdInt = NumberUtils.isDigits(_gradeCd) ? Integer.parseInt(_gradeCd) : 99;
                if (year == tankyuStartYear     && gradeCdInt <= 1
                        || year == tankyuStartYear + 1 && gradeCdInt <= 2
                        || year == tankyuStartYear + 2 && gradeCdInt <= 3
                        || year >= tankyuStartYear + 3
                        ) {
                    isTankyu = true;
                }
            }
            //log.info(" 探究? " + isTankyu);
            return isTankyu ? "総合的な探究の時間" : "総合的な学習の時間";
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
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
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

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null, null, false, false, 999999);
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
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " VALUE(T1.SHOWORDER3, 9999) AS SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), isSaki, isMoto, rs.getInt("SHOWORDER3"));
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
                sql += " WHERE T1.YEAR = '" + _year + "' ";
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

    }
}
