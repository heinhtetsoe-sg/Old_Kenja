/*
 * $Id$
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 */
public class KNJG046 {

    private static final Log log = LogFactory.getLog(KNJG046.class);

    private static final String SEMEALL = "9";

    private static final String PRT_HOLIDAY = "1";

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

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws ParseException {
        final List dateList = getDateList(db2, _param._sdate, _param._edate);
        for (final Iterator it = dateList.iterator(); it.hasNext();) {
            final String date = (String) it.next();
            log.debug(" date = " + date);
            printDate(svf, db2, date);
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
            if (allClassHolidayList.contains(date) && !PRT_HOLIDAY.equals(_param._printHoliday)) {
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
            stb.append("    FROM SCHREG_REGD_GDAT T3, SCHREG_REGD_HDAT T1 ");
            stb.append("    WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("        AND T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.GRADE = T1.GRADE ");
            stb.append("        AND T3.SCHOOL_KIND IN ('J','H') ");
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
            stb.append("    FROM SCHREG_REGD_GDAT T3, EVENT_DAT T1 ");
            stb.append("    LEFT JOIN T_SEMESTER1 T2 ON T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("    WHERE ");
            stb.append("        T1.EXECUTEDATE BETWEEN '" + sdate + "' AND '" + edate + "' ");
            stb.append("        AND T1.HOLIDAY_FLG = '1' ");
            stb.append("        AND T3.YEAR = FISCALYEAR(T1.EXECUTEDATE) ");
            stb.append("        AND T3.GRADE = T1.GRADE ");
            stb.append("        AND T3.SCHOOL_KIND IN ('J','H') ");
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

    private void printDate(final Vrw32alp svf, final DB2UDB db2, final String date) throws ParseException {
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

        final List hrClassList = new ArrayList();
        try {
            final String sql = sqlRegdHdat(semester);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxlineHrclass = 10;//学年毎に１０組まで
            int line = 0;
            String oldgrade = "";
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String school_kind = rs.getString("SCHOOL_KIND");
                final String grade_cd = rs.getString("GRADE_CD");

                if (!"J".equals(school_kind) && !"H".equals(school_kind)) {
                    continue;
                }
                //校種毎に３学年まで
                if (!"01".equals(grade_cd) && !"02".equals(grade_cd) && !"03".equals(grade_cd)) {
                    continue;
                }
                if (!oldgrade.equals(grade)) {
                    oldgrade = grade;
                    line = 0;
                }
                line++;
                //学年毎に１０組まで
                if (maxlineHrclass < line) {
                    continue;
                }
                final int maxlineGrade = "H".equals(school_kind) ? 3 : 0;
                final int printline = (Integer.parseInt(grade_cd) - 1 + maxlineGrade) * maxlineHrclass + line;

                final HrClass hrClasses = new HrClass(grade, hrclass, school_kind, printline);
                hrClassList.add(hrClasses);
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final String form = "KNJG046.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度" + "　教務日誌");
        svf.VrsOut("DATE", hiduke(date)); // 日付

        try {
            final String sql = getSchoolDiarysql(date, "H");
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String weather1 = rs.getString("WEATHER_NAME");
                final String weather2 = rs.getString("WEATHER_NAME2");
                String weatherName = "";
                if (!StringUtils.isBlank(weather1) && !StringUtils.isBlank(weather2)) {
                    weatherName = weather1 + "/" + weather2;
                } else if (!StringUtils.isBlank(weather1)) {
                    weatherName = weather1;
                } else if (!StringUtils.isBlank(weather2)) {
                    weatherName = weather2;
                }
                svf.VrsOut("WEATHER", weatherName); // 天候
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = getSchoolDiarysql(date, null);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            final StringBuffer stb = new StringBuffer();
            while (rs.next()) {
                final String news = rs.getString("NEWS");
                if (!StringUtils.isBlank(news)) {
                    if (stb.length() > 0) {
                        stb.append("\n");
                    }
                    stb.append(news);
                }
            }
            printShoken(svf, "ARTICLE", 110, 3, stb.toString()); // 記事

        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final Map<String, List<String>> staffDivStaffnameList = new HashMap<String, List<String>>();
            final String gradeHrclass = "00000";
            final String sql = getSchoolDiaryDetailsql(date, gradeHrclass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int line = 0;
            String staffDiv = "";
            while (rs.next()) {
                final String staffName = rs.getString("STAFFNAME_SHOW");
                if (!staffDiv.equals(rs.getString("STAFF_DIV"))) {
                    staffDiv = rs.getString("STAFF_DIV");
                    line = 0;
                }
                if (getMappedList(staffDivStaffnameList, staffDiv).contains(staffName)) { // 複数校種を出力する際に重複する職員を表示しない
                    continue;
                }
                getMappedList(staffDivStaffnameList, staffDiv).add(staffName);

                line++;
                if ("1".equals(staffDiv)) {
                    if (line <= 10) {
                        svf.VrsOutn("T_ABSENT1", line, staffName); // 欠勤1
                    } else {
                        svf.VrsOutn("T_ABSENT2", (line-10), staffName); // 欠勤2
                    }
                } else if ("2".equals(staffDiv)) {
                    if (line <= 10) {
                        svf.VrsOutn("T_LATE1", line, staffName); // 遅参1
                    } else {
                        svf.VrsOutn("T_LATE2", (line-10), staffName); // 遅参2
                    }
                } else if ("3".equals(staffDiv)) {
                    if (line <= 10) {
                        svf.VrsOutn("T_EARLY1", line, staffName); // 早退
                    } else {
                        svf.VrsOutn("T_EARLY2", (line-10), staffName); // 早退
                    }
                } else if ("4".equals(staffDiv)) {
                    if (line <= 10) {
                        svf.VrsOutn("T_TRIP", line, staffName); // 出張
                    } else {
                        svf.VrsOutn("T_TRIP2", (line-10), staffName); // 出張
                    }
                } else {
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final String[] attends = new String[] {"ENROLL", "REG_ABSENT", "ABSENT", "LATE", "EARLY", "SUSPEND", "MOURNING"};
        final int[] total = new int[attends.length];
        for (int i = 0; i < attends.length; i++) {
            total[i] = 0;
        }
        final int totalline = 61;

        try {
            final String gradeHrclass = "00000";
            final String sql = getSchoolDiaryDetailsql(date, gradeHrclass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int line = 1;
            while (rs.next()) {
                if ("6".equals(rs.getString("STAFF_DIV"))) {
                    final String countStr = null == rs.getString("COUNT") ? "" : "(" + String.valueOf(rs.getInt("COUNT")) + ")";
                    final String hoketsu = StringUtils.defaultString(rs.getString("STAFFNAME_SHOW")) + countStr;
                    final String[] token = KNJ_EditEdit.get_token(hoketsu, 15, 5);
                    if (null != token) {
                        for (int i = 0; i < token.length; i++) {
                            if (null != token[i]) {
                                svf.VrsOutn("OTHER_ALTERNATE", line, token[i]); // その他補欠
                                line += 1;
                            }
                        }
                    }
                } else {
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClasses = (HrClass) it.next();
            svf.VrsOutn("CLASS", hrClasses._printline, String.valueOf(Integer.parseInt(hrClasses._hrclass))); // 組

            try {
                final String sql = getSchoolDiaryDetailsql(date, hrClasses._gradehrclass);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String staffDiv = "";
                String staffName = "";
                String seq = "";
                while (rs.next()) {
                    if (!staffDiv.equals(rs.getString("STAFF_DIV"))) {
                        staffDiv = rs.getString("STAFF_DIV");
                        staffName = "";
                        seq = "";
                    }
                    final String countStr = null == rs.getString("COUNT") ? "" : "(" + String.valueOf(rs.getInt("COUNT")) + ")";
                    staffName = staffName + seq + StringUtils.defaultString(rs.getString("STAFFNAME_SHOW")) + countStr;
                    seq = ",";

                    if ("5".equals(staffDiv)) {
                        svf.VrsOutn("ALTERNATE", hrClasses._printline, staffName); // 補欠授業
                    } else {
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = sqlRegdDatsql(date, hrClasses._gradehrclass, semester);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int zaiCnt = rs.getInt("ZAI_CNT");
                    final int kyuCnt = rs.getInt("KYU_CNT");
                    if (0 < zaiCnt) {
                        svf.VrsOutn("ENROLL", hrClasses._printline, chk1(zaiCnt,  zaiCnt)); // 在籍
                        setTotal(attends, total, "ENROLL", zaiCnt);
                    }
                    if (0 < kyuCnt) {
                        svf.VrsOutn("REG_ABSENT", hrClasses._printline, chk1(kyuCnt,  kyuCnt)); // 休学
                        setTotal(attends, total, "REG_ABSENT", kyuCnt);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = getAttendDayDatsql(date, hrClasses._gradehrclass, semester);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String diCd = rs.getString("DI_CD");
                    final int cnt = rs.getInt("CNT");
                    if ("6".equals(diCd)) {
                        svf.VrsOutn("ABSENT", hrClasses._printline, chk1(cnt,  cnt)); // 欠席
                        setTotal(attends, total, "ABSENT", cnt);
                    } else if ("15".equals(diCd)) {
                        svf.VrsOutn("LATE", hrClasses._printline, chk1(cnt,  cnt)); // 遅刻
                        setTotal(attends, total, "LATE", cnt);
                    } else if ("16".equals(diCd)) {
                        svf.VrsOutn("EARLY", hrClasses._printline, chk1(cnt,  cnt)); // 早退
                        setTotal(attends, total, "EARLY", cnt);
                    } else if ("2".equals(diCd)) {
                        svf.VrsOutn("SUSPEND", hrClasses._printline, chk1(cnt,  cnt)); // 出停
                        setTotal(attends, total, "SUSPEND", cnt);
                    } else if ("3".equals(diCd)) {
                        svf.VrsOutn("MOURNING", hrClasses._printline, chk1(cnt,  cnt)); // 忌引
                        setTotal(attends, total, "MOURNING", cnt);
                    } else {
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        for (int i = 0; i < attends.length; i++) {
            svf.VrsOutn(attends[i], totalline, chk1(total[i],  total[i])); // 合計
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private static String chk1(final int n1,  final int n) {
        return n1 <= 0 ? "" : String.valueOf(n);
    }

    private void setTotal(final String[] attends, final int[] total, final String field, final int n) {
        for (int i = 0; i < attends.length; i++) {
            if (attends[i].equals(field)) {
                total[i] += n;
            }
        }
    }

    private String sqlRegdHdat(final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT  T1.GRADE, T1.HR_CLASS, T2.SCHOOL_KIND, T2.GRADE_CD ");
        stb.append("    FROM    SCHREG_REGD_HDAT T1, ");
        stb.append("            SCHREG_REGD_GDAT T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ semester +"' ");
        stb.append("        AND T2.YEAR = T1.YEAR ");
        stb.append("        AND T2.GRADE = T1.GRADE ");
        stb.append("        AND T2.SCHOOL_KIND IN ('J','H') ");
        stb.append("    ORDER BY T1.GRADE, T1.HR_CLASS ");
        return stb.toString();
    }

    private static String mkString(final Collection<String> list, final String comma) {
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

    private static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }

    private String getSchoolDiarysql(final String date, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    T1.NEWS, ");
        stb.append("    T2.NAME1 AS WEATHER_NAME, ");
        stb.append("    T3.NAME1 AS WEATHER_NAME2 ");
        stb.append(" FROM ");
        stb.append("    SCHOOL_DIARY_DAT T1 ");
        stb.append("    LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A006' ");
        stb.append("        AND T2.NAMECD2 = T1.WEATHER ");
        stb.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'A006' ");
        stb.append("        AND T3.NAMECD2 = T1.WEATHER2 ");
        stb.append(" WHERE ");
        stb.append("    T1.DIARY_DATE = '" + date + "' ");
        if (_param._has_SCHOOL_DIARY_DAT_SCHOOL_KIND) {
            if (null != schoolKind) {
                stb.append("    AND T1.SCHOOL_KIND = '" + schoolKind + "' ");
            } else if ("1".equals(_param._use_prg_schoolkind)) {
                stb.append("    AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("    AND T1.SCHOOL_KIND IN ('" + mkString(Arrays.asList(_param._selectSchoolKind.split(":")), "', '") + "') ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
                stb.append("    AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("    AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
            if (_param._has_SCHOOL_DIARY_DAT_SCHOOL_KIND) {
                stb.append(" ORDER BY ");
                stb.append("    CASE WHEN T1.SCHOOL_KIND = 'J' THEN 1 ");
                stb.append("         WHEN T1.SCHOOL_KIND = 'H' THEN 2 ");
                stb.append("    END ");
            }
        }
        return stb.toString();
    }

    private String getSchoolDiaryDetailsql(final String date, final String gradeHrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.*, ");
        stb.append("    L2.STAFFNAME_SHOW ");
        stb.append("FROM ");
        stb.append("    SCHOOL_DIARY_DETAIL_DAT T1 ");
        stb.append("    LEFT JOIN V_STAFF_MST L2 ON L2.STAFFCD = T1.STAFFCD ");
        stb.append("WHERE ");
        stb.append("    L2.YEAR = '" + _param._year + "' ");
        if (_param._has_SCHOOL_DIARY_DETAIL_DAT_SCHOOL_KIND) {
            if ("1".equals(_param._use_prg_schoolkind)) {
                stb.append("    AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("    AND T1.SCHOOL_KIND IN ('" + mkString(Arrays.asList(_param._selectSchoolKind.split(":")), "', '") + "') ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
                stb.append("    AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("    AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
        }
        stb.append("    AND T1.DIARY_DATE = '" + date + "' ");
        stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append("ORDER BY ");
        stb.append("    T1.STAFF_DIV, ");
        if (_param._has_SCHOOL_DIARY_DETAIL_DAT_SCHOOL_KIND) {
            stb.append("    CASE WHEN T1.SCHOOL_KIND = 'J' THEN 1 ");
            stb.append("         WHEN T1.SCHOOL_KIND = 'H' THEN 2 ");
            stb.append("    END, ");
        }
        stb.append("    T1.STAFFCD ");
        return stb.toString();
    }

    private String sqlRegdDatsql(final String date, final String gradeHrclass, final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO_A AS( ");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2  ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "'  ");
        stb.append("        AND T1.SEMESTER = '" + semester + "'  ");
        stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "'  ");
        stb.append("        AND T1.YEAR = T2.YEAR  ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)  ");
        stb.append("                 OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)) )  ");
        stb.append("    )  ");
        stb.append(", SCHNO_B AS( ");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2  ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "'  ");
        stb.append("        AND T1.SEMESTER = '" + semester + "'  ");
        stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "'  ");
        stb.append("        AND T1.YEAR = T2.YEAR  ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)  ");
        stb.append("                 OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)) )  ");
        stb.append("        AND EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("               AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append("    )  ");
        stb.append("SELECT  T1.GRADE, T1.HR_CLASS, ");
        stb.append("        COUNT(*) AS ZAI_CNT, ");
        stb.append("        COUNT(T2.SCHREGNO) AS KYU_CNT ");
        stb.append("FROM    SCHNO_A T1  ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO  ");
        stb.append("        LEFT JOIN SCHNO_B T2 ON T1.SCHREGNO = T2.SCHREGNO  ");
        stb.append("GROUP BY T1.GRADE, T1.HR_CLASS ");
        return stb.toString();
    }

    private String getAttendDayDatsql(final String date, final String gradeHrclass, final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.DI_CD, ");
        stb.append("    COUNT(*) AS CNT ");
        stb.append("FROM ");
        stb.append("    ATTEND_DAY_DAT T1, ");
        stb.append("    SCHREG_REGD_DAT T2 ");
        stb.append("WHERE ");
        stb.append("    T1.ATTENDDATE = '" + date + "' ");
        stb.append("    AND T1.YEAR = '" + _param._year + "' ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + semester + "' ");
        stb.append("    AND T2.GRADE || T2.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append("GROUP BY ");
        stb.append("    T1.DI_CD ");
        stb.append("ORDER BY ");
        stb.append("    T1.DI_CD ");
        return stb.toString();
    }

    private String hiduke(final String date) {
        if (null == date) {
            return date;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        final String youbi = "(" + new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)] + ")";
        return KNJ_EditDate.h_format_JP_MD(date) + youbi;
    }

    private void printShoken(final Vrw32alp svf, final String field, final int keta, final int maxLine, final String data0) {
        final List data = get_token(data0, keta, maxLine);
        for (int i = 0; i < Math.min(data.size(), maxLine); i++) {
            svf.VrsOut(field + String.valueOf(i + 1), (String) data.get(i));
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
            } else {
                final String str = ch;
                if (st + KNJ_EditEdit.getMS932ByteLength(str) > ketamax) {
                    rtn.add(cur);
                    cur = "";
                    st = 0;
                }
            }
            cur += ch;
            final String str1 = ch;
            st += KNJ_EditEdit.getMS932ByteLength(str1);
        }
        if (st > 0) {
            rtn.add(cur);
        }
        return rtn;
    }

    private static class HrClass {

        final String _gradehrclass;
        final String _grade;
        final String _hrclass;
        final String _school_kind;
        final int _printline;

        public HrClass(final String grade,
                       final String hrclass,
                       final String school_kind,
                       final int printline
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _gradehrclass = grade + hrclass;
            _school_kind = school_kind;
            _printline = printline;
        }

        public String toString() {
            return _gradehrclass;
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

        /** 教育課程コードを使用するか */
        final String SSEMESTER = "1";
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;
        final String _printHoliday;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        final boolean _has_SCHOOL_DIARY_DAT_SCHOOL_KIND;
        final boolean _has_SCHOOL_DIARY_DETAIL_DAT_SCHOOL_KIND;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgId = request.getParameter("PRGID");
            if ("KNJG046".equals(_prgId)) {
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
            _printHoliday = request.getParameter("PRINT_HOLIDAY");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");

            _has_SCHOOL_DIARY_DAT_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_DIARY_DAT", "SCHOOL_KIND");
            _has_SCHOOL_DIARY_DETAIL_DAT_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_DIARY_DETAIL_DAT", "SCHOOL_KIND");
        }

    }
}

// eof

