/*
 * $Id: 07406eeec8b9fabbd93ea17f74eed8376626f272 $
 *
 * 作成日: 2014/04/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 */
public class KNJG046A {

    private static final Log log = LogFactory.getLog(KNJG046A.class);

    private static final String SEMEALL = "9";

    private boolean _hasData;

    private Param _param;

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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(svf, db2);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws ParseException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List gradeList = new ArrayList();
        try {
            final String sql = sqlRegdGdat();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                gradeList.add(rs.getString("GRADE"));
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final List dateList = getDateList(db2, _param._sdate, _param._edate);
        for (final Iterator it = dateList.iterator(); it.hasNext();) {
            final String date = (String) it.next();
            log.debug(" date = " + date);
            printDate(svf, db2, gradeList, date);
        }
    }

    private List getDateList(final DB2UDB db2, final String sdate, final String edate) {
        final List allClassHolidayList = getAllClassHolidayList(db2, sdate, edate);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final List dateList = new ArrayList();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(sdate));
        final Calendar cale = Calendar.getInstance();
        cale.setTime(Date.valueOf(edate));
        while (cal.before(cale) || cal.equals(cale)) {
            final String date = df.format(cal.getTime());
            if (allClassHolidayList.contains(date)) {
                // すべてのHRが休日の場合、その日付を出力しない
                log.info(" 休日:" + date);
            } else {
                dateList.add(date);
            }
            cal.add(Calendar.DATE, 1);
        }
        return dateList;
    }

    /**
     * 指定校種のクラスが全て休日の日付リスト
     * @param db2
     * @param sdate 範囲開始日付
     * @param edate 範囲終了日付
     * @return　指定校種のクラスが全て休日の日付リスト
     */
    private List getAllClassHolidayList(final DB2UDB db2, final String sdate, final String edate) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List allClassHolidayList = new ArrayList();
        try {
            final StringBuffer stb = new StringBuffer();
            // HRのカウント
            stb.append(" WITH T_HR AS (  ");
            stb.append("    SELECT T1.SEMESTER, COUNT(T1.HR_CLASS) AS HR_COUNT ");
            stb.append("    FROM SCHREG_REGD_HDAT T1 ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("        AND T3.GRADE = T1.GRADE ");
            stb.append("    WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("    GROUP BY T1.SEMESTER ");
            stb.append(" ) ");
            stb.append(" , T_SEMESTER AS ( ");
            stb.append("    SELECT * ");
            stb.append("    FROM SEMESTER_MST ");
            stb.append("    WHERE SEMESTER <> '9' ");
            stb.append(" ) ");
            // 開始日付: 最初の学期ならその学期の開始日付の一日、それ以外は開始日付
            // 終了日付: 最後の学期ならその学期の終了日付の月の最後の日付、それ以外はMAX(ひとつあとの学期の開始日付の前日, その学期の終了日付)
            stb.append(" , T_SEMESTER1 AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        CASE WHEN T1.SEMESTER = T3.MIN_SEMESTER THEN DATE(RTRIM(CHAR(YEAR(T1.SDATE))) || '-' || RTRIM(CHAR(MONTH(T1.SDATE))) || '-01') ");
            stb.append("             ELSE T1.SDATE ");
            stb.append("        END AS SDATE, ");
            stb.append("        CASE WHEN T1.SEMESTER = T2.MAX_SEMESTER THEN LAST_DAY(T1.EDATE) ");
            stb.append("             WHEN AFT.SDATE - 1 DAY > T1.EDATE THEN AFT.SDATE - 1 DAY  ");
            stb.append("             ELSE T1.EDATE ");
            stb.append("        END AS EDATE ");
            stb.append("    FROM T_SEMESTER T1 ");
            stb.append("    LEFT JOIN (SELECT YEAR, MAX(SEMESTER) AS MAX_SEMESTER ");
            stb.append("               FROM T_SEMESTER ");
            stb.append("               GROUP BY YEAR) T2 ON T2.YEAR = T1.YEAR  ");
            stb.append("    LEFT JOIN (SELECT YEAR, MIN(SEMESTER) AS MIN_SEMESTER ");
            stb.append("               FROM T_SEMESTER ");
            stb.append("               GROUP BY YEAR) T3 ON T3.YEAR = T1.YEAR  ");
            stb.append("    LEFT JOIN T_SEMESTER AFT ON AFT.YEAR = T1.YEAR ");
            stb.append("        AND INT(AFT.SEMESTER) = INT(T1.SEMESTER) + 1  ");
            stb.append("    WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append(" ), EVENT_DATE AS ( ");
            stb.append("    SELECT T2.SEMESTER, T1.EXECUTEDATE, COUNT(T1.HR_CLASS) AS HR_COUNT ");
            stb.append("    FROM EVENT_DAT T1 ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT T3 ON T3.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("          AND T3.GRADE = T1.GRADE ");
            stb.append("    LEFT JOIN T_SEMESTER1 T2 ON T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("    WHERE ");
            stb.append("        T1.EXECUTEDATE BETWEEN '" + sdate + "' AND '" + edate + "' ");
            stb.append("        AND T1.HOLIDAY_FLG = '1' ");
            stb.append("        AND T3.YEAR = FISCALYEAR(T1.EXECUTEDATE) ");
            stb.append("    GROUP BY T2.SEMESTER, T1.EXECUTEDATE ");
            stb.append(" ) ");
            stb.append(" SELECT T1.EXECUTEDATE  ");
            stb.append(" FROM EVENT_DATE T1 ");
            stb.append(" INNER JOIN T_HR T3 ON T3.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("    T1.HR_COUNT = T3.HR_COUNT ");

            final String sql = stb.toString();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                allClassHolidayList.add(rs.getString("EXECUTEDATE"));
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return allClassHolidayList;
    }

    private void printDate(final Vrw32alp svf, final DB2UDB db2, final List gradeList, final String date) throws ParseException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String semester = _param._semester;
        try {
            final String sql = "SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER <> '" + SEMEALL + "' AND '" + date + "' BETWEEN SDATE AND EDATE ORDER BY SEMESTER ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                semester = rs.getString("SEMESTER");
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final List students = new ArrayList();
        try {
            final String sql = sqlRegdData(semester, date);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student(rs.getString("SCHREGNO"));
                students.add(student);

                student._grade = rs.getString("GRADE");
                student._sex = rs.getString("SEX");
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final Map hasuuMap = AttendAccumulate.getHasuuMap(_param._attendSemesMap, _param._yearSdate, date);

        setAttendData(db2, students, gradeList, date, hasuuMap);

        final String form = "KNJG046A.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("TITLE", "学校日誌");

        for (int i = 0; i < Math.min(gradeList.size(), 4); i++) {
            svf.VrsOut("GRADE1_" + String.valueOf(i + 1), String.valueOf(i + 1) + "年"); // 学年
            svf.VrsOut("GRADE2_" + String.valueOf(i + 1), (new String[] {"", "一", "二", "三", "四"}[i + 1]) + "学年"); // 学年
        }

        svf.VrsOut("DATE", hiduke(date)); // 日付
        svf.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名

        final String staffname = null;
        svf.VrsOut("STAFF_NAME" + (getMS932ByteLength(staffname) > 10 ? "2_1" : "1"), staffname); // 記載者


        try {
            final String sql = getSchoolDiarysql(date);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("WEATHER", rs.getString("WEATHER_NAME")); // 天候
                printShoken(svf, "IMPORTANT", 44, 10, rs.getString("IMPORTANT_MATTER")); // 重要事項
                printShoken(svf, "GUEST", 44, 10, rs.getString("GUEST")); // 来客者
                printShoken(svf, "REPORT", 94, 10, rs.getString("REPORT")); // 記事
                printShoken(svf, "REC_DOC", 44, 10, rs.getString("RECEIVE_OFFICIAL_DOCUMENTS")); // 収受公文書
                printShoken(svf, "SEND_DOC", 44, 10, rs.getString("SENDING_OFFICIAL_DOCUMENTS")); // 発送公文書
                printShoken(svf, "TEACHER_ARRIVE", 44, 7, rs.getString("BUSINESS_TRIP")); // 出張
                printShoken(svf, "TEACHER_HOLIDAY", 44, 7, rs.getString("VACATION")); // 休暇
                printShoken(svf, "TEACHER_MOURNING", 44, 7, rs.getString("SPECIAL_LEAVE")); // 慶弔
                printShoken(svf, "TEACHER_ABSENT", 44, 5, rs.getString("ABSENCE")); // 欠勤
                printShoken(svf, "TEACHER_LATE_EARLY", 44, 5, rs.getString("LATE_EARLY")); // 遅刻・早退
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final int statlinei = 5 - 1;
        final String[] sexes = new String[] {"1", "2", null};
        final Attendance[] total = new Attendance[sexes.length];
        for (int i = 0; i < total.length; i++) {
            total[i] = new Attendance();
        }
        for (int gi = 0; gi < Math.min(gradeList.size(), statlinei); gi++) {
            final String grade = (String) gradeList.get(gi);
            for (int j = 0; j < sexes.length; j++) {
                final Attendance a = getAttendanceSum(students, grade, sexes[j]);
                final int line = gi * sexes.length + j + 1;
                final int attend = a._attend;
                svf.VrsOutn("REGD", line, chk1(a._zaiseki, a._zaiseki)); // 在籍
                svf.VrsOutn("ATTEND", line, chk1(attend, attend)); // 出席
                svf.VrsOutn("ABSENT", line, chk1(attend, a._absence)); // 欠席
                svf.VrsOutn("LATE", line, chk1(attend, a._late)); // 遅刻
                svf.VrsOutn("EARLY", line, chk1(attend, a._leave)); // 早退
                total[j].add(a);
            }
        }
        for (int j = 0; j < sexes.length; j++) {
            final int line = statlinei * sexes.length + j + 1;
            final Attendance a = total[j];
            final int attend = a._attend;
            svf.VrsOutn("REGD", line, chk1(a._zaiseki, a._zaiseki)); // 在籍
            svf.VrsOutn("ATTEND", line, chk1(attend, attend)); // 出席
            svf.VrsOutn("ABSENT", line, chk1(attend, a._absence)); // 欠席
            svf.VrsOutn("LATE", line, chk1(attend, a._late)); // 遅刻
            svf.VrsOutn("EARLY", line, chk1(attend, a._leave)); // 早退
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private static String chk1(final int n1,  final int n) {
        return n1 <= 0 ? "" : String.valueOf(n);
    }

    private String sqlRegdGdat() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT  T1.GRADE ");
        stb.append("    FROM    SCHREG_REGD_GDAT T1 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("    ORDER BY GRADE ");
        return stb.toString();
    }

    private String sqlRegdData(final String semester, final String date) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ semester +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END) ");
        stb.append("               OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)) ) ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("              AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");
        stb.append("SELECT  T1.SCHREGNO, ");
        stb.append("        T1.GRADE, T5.SEX ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("ORDER BY ATTENDNO");
        return stb.toString();
    }

    private String getSchoolDiarysql(final String date) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    T1.*, ");
        stb.append("    L1.REMARK1  AS IMPORTANT_MATTER, ");
        stb.append("    L1.REMARK2  AS GUEST, ");
        stb.append("    L1.REMARK3  AS REPORT, ");
        stb.append("    L1.REMARK4  AS RECEIVE_OFFICIAL_DOCUMENTS, ");
        stb.append("    L1.REMARK5  AS SENDING_OFFICIAL_DOCUMENTS, ");
        stb.append("    L1.REMARK6  AS BUSINESS_TRIP, ");
        stb.append("    L1.REMARK7  AS VACATION, ");
        stb.append("    L1.REMARK8  AS SPECIAL_LEAVE, ");
        stb.append("    L1.REMARK9  AS ABSENCE, ");
        stb.append("    L1.REMARK10 AS LATE_EARLY, ");
        stb.append("    T2.NAME1 AS WEATHER_NAME ");
        stb.append(" FROM ");
        stb.append("    SCHOOL_DIARY_DAT T1 ");
        stb.append("    LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A006' ");
        stb.append("        AND T2.NAMECD2 = T1.WEATHER ");
        stb.append("    LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT L1 ON T1.SCHOOLCD = L1.SCHOOLCD ");
        stb.append("        AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("        AND T1.DIARY_DATE = L1.DIARY_DATE ");
        stb.append("        AND L1.SEQ = '001' ");
        stb.append(" WHERE ");
        stb.append("    T1.DIARY_DATE = '" + date + "' ");
        stb.append("    AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        return stb.toString();
    }

    private Attendance getAttendanceSum(final List students, final String grade, final String sex) {
        final Attendance rtn = new Attendance();
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == student._attendance) {
                continue;
            }
            if (!(null == grade || grade.equals(student._grade))) {
                continue;
            }
            if (!(null == sex || sex.equals(student._sex))) {
                continue;
            }
            rtn._zaiseki += 1;
            rtn._absence += student._attendance._absence;
            rtn._late += student._attendance._late;
            rtn._leave += student._attendance._leave;
            rtn._attend += student._attendance._attend;
        }
        return rtn;
    }

    private String hiduke(final String date) {
        if (null == date) {
            return date;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        final String youbi = "(" + new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)] + ")";
        return KNJ_EditDate.h_format_JP(date) + youbi;
    }

    private void printShoken(final Vrw32alp svf, final String field, final int keta, final int maxLine, final String data0) {
        final List data = get_token(data0, keta, maxLine);
        for (int i = 0; i < Math.min(data.size(), maxLine); i++) {
            svf.VrsOutn(field, i + 1, (String) data.get(i)); // 備考
        }
    }

    public static List get_token(final String s0, final int ketamax, final int linecnt) {
        if (s0 == null || s0.length() == 0) {
            return Collections.EMPTY_LIST;
        }
        final List rtn = new ArrayList();
        final String s = StringUtils.replace(StringUtils.replace(s0, "\r\n", "\n"), "\r", "\n");
        int st = 0;
        String cur = "";
        for (int j = 0; j < s.length(); j++) {
            String ch = String.valueOf(s.charAt(j));
            if ("\n".equals(ch)) {
                rtn.add(cur);
                cur = "";
                st = 0;
                ch = "";
            } else if (st + getMS932ByteLength(ch) > ketamax) {
                rtn.add(cur);
                cur = "";
                st = 0;
            }
            cur += ch;
            st += getMS932ByteLength(ch);
        }
        if (st > 0) {
            rtn.add(cur);
        }
        return rtn;
    }

    private static class Student {

        final String _schregno;
        private String _sex;
        private String _grade;
        private int _lesson;
        private Attendance _attendance;

        final Map _attendMap = new TreeMap();

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public String toString() {
            return _schregno;
        }

        /**
         * 学籍番号の生徒を得る
         * @param schregno 学籍番号
         * @param students 生徒のリスト
         * @return 学籍番号の生徒
         */
        public static Student getStudent(final String schregno, final List students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {

        int _zaiseki = 0;
        /** 公欠 */
        int _absence;
        int _attend;
        /** 遅刻 */
        int _late;
        /** 早退 */
        int _leave;

        void add(Attendance a) {
            _zaiseki += a._zaiseki;
            _absence += a._absence;
            _attend += a._attend;
            _late += a._late;
            _leave += a._leave;
        }

        public String toString() {
            return "[" +
            "absence=" + _absence +
            ",attend=" + _attend +
            ",late=" + _late +
            ",leave=" + _leave;
        }
    }

    /**
     * 各生徒に１日ごと、科目ごとの出欠データをセットする
     * @param db2
     * @param students
     * @param param
     */
    private void setAttendData(final DB2UDB db2, final List students, final List gradeList, final String date, final Map hasuuMap) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            boolean semesFlg = false; // 指定日付のみ
            String sql = AttendAccumulate.getAttendSemesSql(
                    semesFlg,
                    _param._definecode,
                    _param._knjSchoolMst,
                    _param._year,
                    _param.SSEMESTER,
                    SEMEALL,
                    null,
                    _param._periodInState,
                    null,
                    null,
                    date,
                    date,
                    "?",
                    null,
                    null,
                    "SEMESTER",
                    _param._useCurriculumcd,
                    _param._useVirus,
                    _param._useKoudome
            );

            // log.info(" 2 attend semes sql = " + sql);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = gradeList.iterator(); it.hasNext();) {
                final String grade = (String) it.next();

                ps.setString(1, grade);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !SEMEALL.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final int absence = rs.getInt("SICK");
                    final int attend = rs.getInt("PRESENT");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");

                    final Attendance a = new Attendance();
                    a._absence = absence;
                    a._attend = attend;
                    a._late = late;
                    a._leave = early;
                    student._attendance = a;
                }
            }

        } catch (SQLException e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /**
     * パラメータクラス
     */
    private static class Param {

        final String _year;
        final String _semester;
        final String _loginDate;
        final String _prgId;

        final String _sdate;
        final String _edate;
        final String _schoolKind;
        final String _nameMstA023Abbv1;

        private String _yearSdate;
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool _definecode;
        private String _periodInState;
        private Map _attendSemesMap;

        /** 教育課程コードを使用するか */
        final String SSEMESTER = "1";
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgId = request.getParameter("PRGID");
            if (null != request.getParameter("DATE_TO")) {
                _year = request.getParameter("YEAR");
                _semester = request.getParameter("SEMESTER");
                _edate = request.getParameter("DATE_TO").replace('/', '-');
                _sdate = request.getParameter("DATE_FROM").replace('/', '-');
            } else {
                _year = request.getParameter("CTRL_YEAR");
                _semester = request.getParameter("CTRL_SEMESTER");
                _edate = request.getParameter("DIARY_DATE").replace('/', '-');
                _sdate = _edate;
            }
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _schoolKind = request.getParameter("SCHOOL_KIND");

            try {
                final Map paramMap = new HashMap();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                    paramMap.put("SCHOOL_KIND", _schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }

            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);

            loadSemester(db2);

            try {
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = getNameMst(db2, "Z010", "00", null, "NAME1");
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                // log.info(_attendSemesMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
            _nameMstA023Abbv1 = getNameMst(db2, "A023", null, _schoolKind, "ABBV1");
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String name1, final String field) {
            String v = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += "SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ";
                if (null != namecd2) {
                    sql += " AND NAMECD2 = '" + namecd2 + "' ";
                }
                if (null != name1) {
                    sql += " AND NAME1 = '" + name1 + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    v = rs.getString(field);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return v;
        }

        private void loadSemester(final DB2UDB db2) {

            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST "
                + " WHERE YEAR = '" + _year + "' AND SEMESTER = '" + SEMEALL + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _yearSdate = rs.getString("SDATE");
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof

