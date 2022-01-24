/*
 * $Id: 9899fc5f1f702f3857aebab8a1c63ffa428299e5 $
 *
 * 作成日: 2017/03/09
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJG049 {

    private static final Log log = LogFactory.getLog(KNJG049.class);

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

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws ParseException, SQLException {
        final List dateList = getDateList(db2, _param._sdate, _param._edate);
        for (final Iterator it = dateList.iterator(); it.hasNext();) {
            final String date = (String) it.next();
            log.debug(" date = " + date);

            //校種ごとに印刷
            for (Iterator it2 = _param._schoolKindList.iterator(); it2.hasNext();) {
                String schoolKind = (String) it2.next();

                printDate(svf, db2, date, schoolKind);
            }
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

    private void printDate(final Vrw32alp svf, final DB2UDB db2, final String date, final String schoolKind) throws ParseException, SQLException {
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
            final String sql = sqlRegdHdat(semester, schoolKind);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxlineHrclass = 15;//学年毎に１５組まで
            String oldgrade = "";
            int gradeCnt = 0;
            int printline = 0;
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrNameAbbv = rs.getString("HR_NAMEABBV");
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
                    printline = 0;
                    gradeCnt++;
                }
                //最大３学年のみ
                if (gradeCnt > 3) {
                    break;
                }
                printline++;
                //学年毎に１５組まで
                if (maxlineHrclass < printline) {
                    continue;
                }

                final HrClass hrClasses = new HrClass(grade, grade_cd, hrclass, hrNameAbbv, school_kind, gradeCnt, printline);
                hrClassList.add(hrClasses);
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final String form;
        if (_param._useKNJG049_2) {
            form = "KNJG049_2.frm";
        } else {
            form = "KNJG049.frm";
        }
        svf.VrSetForm(form, 1);

        if (_param._isHigashiosakaKashiwara) {
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度" + "　学籍簿");
        } else {
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度" + "　学校日誌");
        }

        String weatherName = "";
        try {
            final String sql = getSchoolDiarysql(date, schoolKind);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String weather1 = rs.getString("WEATHER_NAME");
                final String weather2 = rs.getString("WEATHER_NAME2");
                if (!StringUtils.isBlank(weather1) && !StringUtils.isBlank(weather2)) {
                    weatherName = weather1 + "/" + weather2;
                } else if (!StringUtils.isBlank(weather1)) {
                    weatherName = weather1;
                } else if (!StringUtils.isBlank(weather2)) {
                    weatherName = weather2;
                }
                printShoken(svf, "RECORD", 46, 9, rs.getString("NEWS"));       // 記事
                printShoken(svf, "MATTER", 46, 12, rs.getString("STAFFNEWS")); // 職員事項
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        svf.VrsOut("DATE", hiduke(date, true, "　天候(" + weatherName + ")")); // 日付

        final String[] attends = new String[] {"M_ENROLL", "W_ENROLL", "M_ATTEND", "W_ATTEND", "M_ACSENCE", "W_ACSENCE", "M_LATE", "W_LATE", "M_EARLY", "W_EARLY", "M_NOTICE", "W_NOTICE", "M_MOURNING", "W_MOURNING"};
        final int[] total01 = new int[attends.length];
        final int[] total02 = new int[attends.length];
        final int[] total03 = new int[attends.length];
        final int[] total = new int[attends.length];
        for (int i = 0; i < attends.length; i++) {
            total01[i] = 0;
            total02[i] = 0;
            total03[i] = 0;
            total[i] = 0;
        }

        final Map schregHrclassMap = new HashMap();
        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClasses = (HrClass) it.next();

            try {
                final String sql = sqlRegdDatsql(date, hrClasses._gradehrclass, semester);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int manZaiCnt = 0;
                int woManZaiCnt = 0;
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    if ("1".equals(sex)) {
                        manZaiCnt++;
                    } else {
                        woManZaiCnt++;
                    }
                    hrClasses._schregMap.put(rs.getString("SCHREGNO"), sex);
                    schregHrclassMap.put(rs.getString("SCHREGNO"), hrClasses);
                }
                hrClasses._manZaiCnt = manZaiCnt;
                hrClasses._woManZaiCnt = woManZaiCnt;
                if (!"0".equals(_param._hibiNyuuryokuNasi)) {
                    hrClasses._manAttendCnt = manZaiCnt;
                    hrClasses._woManAttendCnt = woManZaiCnt;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        if ("0".equals(_param._hibiNyuuryokuNasi)) {
            //SCH_CHR_DAT
            try {
                final String sql = getSchChrDatSql(date);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final HrClass hrClasses = (HrClass) schregHrclassMap.get(schregno);
                    if (null == hrClasses) {
                        continue;
                    }
                    final String sex = (String) hrClasses._schregMap.get(schregno);
                    if ("1".equals(sex)) {
                        hrClasses._manAttendCnt += 1;
                    } else {
                        hrClasses._woManAttendCnt += 1;
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        _param._attendParamMap.put("semesFlg", new Boolean(false));
        _param._attendParamMap.put("befDayFrom", date);
        _param._attendParamMap.put("befDayTo", date);
        _param._attendParamMap.put("groupByDiv", "SCHREGNO");
        _param._attendParamMap.put("outputDebug", "1");

        if ("ATTEND_DAY_DAT".equals(_param._hibiNyuuryoku)) {
            //ATTEND_SEMES
            try {
                final String attendSql = AttendAccumulate.getAttendDayDatSql(
                        _param._year,
                        date,
                        date,
                        semester,
                        _param._attendParamMap
                        );
                ps = db2.prepareStatement(attendSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HrClass hrClasses = (HrClass) schregHrclassMap.get(rs.getString("SCHREGNO"));
                    if (null == hrClasses) {
                        //log.warn("no hrclass: schregno = " + rs.getString("SCHREGNO"));
                        continue;
                    }
                    final String sex = (String) hrClasses._schregMap.get(rs.getString("SCHREGNO"));
                    if ("1".equals(sex)) {
                        hrClasses._manSickCnt += rs.getInt("SICK");
                        hrClasses._manLateCnt += rs.getInt("LATE");
                        hrClasses._manEarlyCnt += rs.getInt("EARLY");
                        hrClasses._manMourningCnt += rs.getInt("MOURNING");
                    } else {
                        hrClasses._woManSickCnt += rs.getInt("SICK");
                        hrClasses._woManLateCnt += rs.getInt("LATE");
                        hrClasses._woManEarlyCnt += rs.getInt("EARLY");
                        hrClasses._woManMourningCnt += rs.getInt("MOURNING");
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        } else {

            //ATTEND_SEMES
            try {
                final String attendSql = AttendAccumulate.getAttendDatOneDaySql(
                        _param._year,
                        semester,
                        date,
                        _param._attendParamMap
                        );
                ps = db2.prepareStatement(attendSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HrClass hrClasses = (HrClass) schregHrclassMap.get(rs.getString("SCHREGNO"));
                    if (null == hrClasses) {
                        //log.warn("no hrclass: schregno = " + rs.getString("SCHREGNO"));
                        continue;
                    }
                    final String sex = (String) hrClasses._schregMap.get(rs.getString("SCHREGNO"));
                    if ("1".equals(sex)) {
                        hrClasses._manSickCnt += rs.getInt("SICK");
                        hrClasses._manLateCnt += rs.getInt("LATE");
                        hrClasses._manEarlyCnt += rs.getInt("EARLY");
                        hrClasses._manMourningCnt += rs.getInt("MOURNING");
                    } else {
                        hrClasses._woManSickCnt += rs.getInt("SICK");
                        hrClasses._woManLateCnt += rs.getInt("LATE");
                        hrClasses._woManEarlyCnt += rs.getInt("EARLY");
                        hrClasses._woManMourningCnt += rs.getInt("MOURNING");
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }


            //ATTEND_SUBCLASS
            try {
                final String subclassSql = AttendAccumulate.getAttendSubclassSql(
                        _param._year,
                        semester,
                        date,
                        date,
                        _param._attendParamMap
                        );
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final HrClass hrClasses = (HrClass) schregHrclassMap.get(rs.getString("SCHREGNO"));
                    if (null == hrClasses) {
                        //log.warn("no hrclass: schregno = " + rs.getString("SCHREGNO"));
                        continue;
                    }
                    final String sex = (String) hrClasses._schregMap.get(rs.getString("SCHREGNO"));
                    if ("1".equals(sex)) {
                        hrClasses._manNoticeCnt += rs.getInt("SICK2");
                    } else {
                        hrClasses._woManNoticeCnt += rs.getInt("SICK2");
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClasses = (HrClass) it.next();
            svf.VrsOutn("HR_NAME" + Integer.parseInt(hrClasses._gradeCd), hrClasses._printline, hrClasses._hrNameAbbv);
            svf.VrsOutn("SEX" + Integer.parseInt(hrClasses._gradeCd) + "_1", hrClasses._printline, "男");
            svf.VrsOutn("SEX" + Integer.parseInt(hrClasses._gradeCd) + "_2", hrClasses._printline, "女");
            setTotalData(svf, "ENROLL", attends, total01, total02, total03, total, hrClasses, hrClasses._manZaiCnt, hrClasses._woManZaiCnt);

            //ATTEND_SEMES
            try {
                setTotalData(svf, "ATTEND", attends, total01, total02, total03, total, hrClasses, hrClasses._manAttendCnt - hrClasses._manSickCnt, hrClasses._woManAttendCnt - hrClasses._woManSickCnt);
                setTotalData(svf, "ACSENCE", attends, total01, total02, total03, total, hrClasses, hrClasses._manSickCnt, hrClasses._woManSickCnt);
                setTotalData(svf, "LATE", attends, total01, total02, total03, total, hrClasses, hrClasses._manLateCnt, hrClasses._woManLateCnt);
                setTotalData(svf, "EARLY", attends, total01, total02, total03, total, hrClasses, hrClasses._manEarlyCnt, hrClasses._woManEarlyCnt);
                setTotalData(svf, "MOURNING", attends, total01, total02, total03, total, hrClasses, hrClasses._manMourningCnt, hrClasses._woManMourningCnt);
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }


            //ATTEND_SUBCLASS
            try {
                setTotalData(svf, "NOTICE", attends, total01, total02, total03, total, hrClasses, hrClasses._manNoticeCnt, hrClasses._woManNoticeCnt);
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
        }

        printTotalData(svf, "TOTAL_ENROLL", total01[0] + total01[1], total02[0] + total02[1], total03[0] + total03[1]);
        printTotalData(svf, "TOTAL_ATTEND", total01[2] + total01[3], total02[2] + total02[3], total03[2] + total03[3]);
        printTotalData(svf, "TOTAL_ACSENCE", total01[4] + total01[5], total02[4] + total02[5], total03[4] + total03[5]);
        printTotalData(svf, "TOTAL_LATE", total01[6] + total01[7], total02[6] + total02[7], total03[6] + total03[7]);
        printTotalData(svf, "TOTAL_EARLY", total01[8] + total01[9], total02[8] + total02[9], total03[8] + total03[9]);
        printTotalData(svf, "TOTAL_NOTICE", total01[10] + total01[11], total02[10] + total02[11], total03[10] + total03[11]);
        printTotalData(svf, "TOTAL_MOURNING", total01[12] + total01[13], total02[12] + total02[13], total03[12] + total03[13]);

        //総計欄男女
        for (int itemCnt = 1; itemCnt < 8; itemCnt++) {
            svf.VrsOut("SEX4_" + itemCnt + "_1", "男");
            svf.VrsOut("SEX4_" + itemCnt + "_2", "女");
        }
        final Map totalMap = new HashMap();
        totalMap.put("1", total01);
        totalMap.put("2", total02);
        totalMap.put("3", total03);

        //総計欄
        String befAttends = "";
        int attendTotal = 0;
        for (int i = 0; i < attends.length; i++) {
            int sexTotal = 0;
            if (!"".equals(befAttends) && attends[i].indexOf(befAttends) == -1) {
                svf.VrsOut("TOTAL_" + befAttends, chk1(attendTotal, attendTotal));
                attendTotal = 0;
            }
            for (int gradeCnt = 1; gradeCnt < 4; gradeCnt++) {
                final int[] setTotal = (int[]) totalMap.get(String.valueOf(gradeCnt));
                if (attends[i].indexOf("M_") > -1) {
                    svf.VrsOutn(attends[i].substring(attends[i].indexOf("M_") + 2) + "4_1", gradeCnt, chk1(setTotal[i], setTotal[i]));
                    befAttends = attends[i].substring(attends[i].indexOf("M_") + 2);
                } else {
                    svf.VrsOutn(attends[i].substring(attends[i].indexOf("W_") + 2) + "4_2", gradeCnt, chk1(setTotal[i], setTotal[i]));
                    befAttends = attends[i].substring(attends[i].indexOf("W_") + 2);
                }
                sexTotal += setTotal[i];
                attendTotal += setTotal[i];
            }
            if (attends[i].indexOf("M_") > -1) {
                svf.VrsOutn(attends[i].substring(attends[i].indexOf("M_") + 2) + "4_1", 4, chk1(sexTotal, sexTotal));
            } else {
                svf.VrsOutn(attends[i].substring(attends[i].indexOf("W_") + 2) + "4_2", 4, chk1(sexTotal, sexTotal));
            }
        }
        svf.VrsOut("TOTAL_" + befAttends, chk1(attendTotal, attendTotal));

        //異動情報
        PreparedStatement psIdou = null;
        ResultSet rsIdou = null;
        final String idouSql = getIdouDataSql(date, semester, schoolKind);
        try {
            psIdou = db2.prepareStatement(idouSql);
            rsIdou = psIdou.executeQuery();
            int idouCnt = 1;
            while (rsIdou.next()) {
                final String setDate = hiduke(rsIdou.getString("ACT_DATE"), false, "　" + StringUtils.defaultString(rsIdou.getString("NAME1")));
                svf.VrsOutn("CHANGE", idouCnt, rsIdou.getString("HR_NAMEABBV") + "-" + rsIdou.getString("ATTENDNO") + "　" + rsIdou.getString("NAME") + "　" + setDate);
                idouCnt++;
            }
        } finally {
            DbUtils.closeQuietly(null, psIdou, rsIdou);
            db2.commit();
        }

        //押印欄名称
        PreparedStatement psStampTitle = null;
        ResultSet rsStampTitle = null;
        final String stampTitleSql = "SELECT * FROM PRG_STAMP_DAT WHERE YEAR = '" + _param._year + "' AND SEMESTER = '9' AND SCHOOLCD = '" + _param._schoolCd + "' AND SCHOOL_KIND = '" + schoolKind + "' AND PROGRAMID = 'KNJG049' ORDER BY SEQ ";
        try {
            psStampTitle = db2.prepareStatement(stampTitleSql);
            rsStampTitle = psStampTitle.executeQuery();
            int stampCnt = 1;
            while (rsStampTitle.next()) {
                final String title = null != rsStampTitle.getString("TITLE") ? rsStampTitle.getString("TITLE") : "";
                svf.VrsOut("JOB_NAME" + stampCnt, title);
                stampCnt++;
            }
        } finally {
            DbUtils.closeQuietly(null, psStampTitle, rsStampTitle);
            db2.commit();
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private void printTotalData(final Vrw32alp svf, final String setFiledNm, final int set01Val, final int set02Val, final int set03Val) {
        svf.VrsOut(setFiledNm + "1", chk1(set01Val,  set01Val));
        svf.VrsOut(setFiledNm + "2", chk1(set02Val,  set02Val));
        svf.VrsOut(setFiledNm + "3", chk1(set03Val,  set03Val));
    }

    private void setTotalData(final Vrw32alp svf,
            final String setName,
            final String[] attends,
            final int[] total01,
            final int[] total02,
            final int[] total03,
            final int[] total,
            final HrClass hrClasses,
            int manCnt,
            int woManCnt) {
        svf.VrsOutn(setName + Integer.parseInt(hrClasses._gradeCd) + "_1", hrClasses._printline, chk1(manCnt,  manCnt));
        svf.VrsOutn(setName + Integer.parseInt(hrClasses._gradeCd) + "_2", hrClasses._printline, chk1(woManCnt,  woManCnt));
        setTotal(attends, total, "M_" + setName, manCnt);
        setTotal(attends, total, "W_" + setName, woManCnt);
        if (hrClasses._printGradeField == 1) {
            setTotal(attends, total01, "M_" + setName, manCnt);
            setTotal(attends, total01, "W_" + setName, woManCnt);
        } else if (hrClasses._printGradeField == 2) {
            setTotal(attends, total02, "M_" + setName, manCnt);
            setTotal(attends, total02, "W_" + setName, woManCnt);
        } else if (hrClasses._printGradeField == 3) {
            setTotal(attends, total03, "M_" + setName, manCnt);
            setTotal(attends, total03, "W_" + setName, woManCnt);
        }
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

    private String getIdouDataSql(final String date, final String semester, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH GRD_T AS( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.GRD_DATE AS ACT_DATE, ");
        stb.append("     NM.NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A003' ");
        stb.append("          AND BASE.GRD_DIV = NM.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.GRD_DATE = '" + date + "' ");
        stb.append("     AND BASE.GRD_DIV IN ('2', '3', '6', '7') ");
        stb.append(" ), ENT_T AS( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.ENT_DATE AS ACT_DATE, ");
        stb.append("     NM.NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A002' ");
        stb.append("          AND BASE.ENT_DIV = NM.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_DATE = '" + date + "' ");
        stb.append("     AND BASE.ENT_DIV IN ('4', '5', '7') ");
        stb.append(" ), TRANS_T AS( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     TRANSFER.TRANSFER_SDATE AS ACT_DATE, ");
        stb.append("     NM.NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     INNER JOIN SCHREG_TRANSFER_DAT TRANSFER ON BASE.SCHREGNO = TRANSFER.SCHREGNO ");
        stb.append("           AND TRANSFER.TRANSFER_SDATE = '" + date + "' ");
        stb.append("     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A004' ");
        stb.append("          AND TRANSFER.TRANSFERCD = NM.NAMECD2 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.ACT_DATE, ");
        stb.append("     BASE.NAME1 ");
        stb.append(" FROM ");
        stb.append("     GRD_T BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
        stb.append("          AND REGD.SEMESTER = '" + semester + "' ");
        stb.append("          AND BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ");
        stb.append("                                    AND GDAT.GRADE = REGD.GRADE ");
        stb.append(" WHERE ");
        stb.append("     GDAT.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.ACT_DATE, ");
        stb.append("     BASE.NAME1 ");
        stb.append(" FROM ");
        stb.append("     ENT_T BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
        stb.append("          AND REGD.SEMESTER = '" + semester + "' ");
        stb.append("          AND BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ");
        stb.append("                                    AND GDAT.GRADE = REGD.GRADE ");
        stb.append(" WHERE ");
        stb.append("     GDAT.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.ACT_DATE, ");
        stb.append("     BASE.NAME1 ");
        stb.append(" FROM ");
        stb.append("     TRANS_T BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
        stb.append("          AND REGD.SEMESTER = '" + semester + "' ");
        stb.append("          AND BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ");
        stb.append("                                    AND GDAT.GRADE = REGD.GRADE ");
        stb.append(" WHERE ");
        stb.append("     GDAT.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO ");
        return stb.toString();
    }

    private String sqlRegdHdat(final String semester, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT  T1.GRADE, T1.HR_CLASS, HDAT.HR_NAMEABBV, T2.SCHOOL_KIND, T2.GRADE_CD ");
        stb.append("    FROM    SCHREG_REGD_HDAT T1 ");
        stb.append("            LEFT JOIN SCHREG_REGD_HDAT HDAT ON T1.YEAR = HDAT.YEAR ");
        stb.append("                 AND T1.SEMESTER = HDAT.SEMESTER ");
        stb.append("                 AND T1.GRADE = HDAT.GRADE ");
        stb.append("                 AND T1.HR_CLASS = HDAT.HR_CLASS, ");
        stb.append("            SCHREG_REGD_GDAT T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ semester +"' ");
        stb.append("        AND T2.YEAR = T1.YEAR ");
        stb.append("        AND T2.GRADE = T1.GRADE ");
        stb.append("        AND T2.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append("    ORDER BY T1.GRADE, T1.HR_CLASS ");
        return stb.toString();
    }

    private String getSchoolDiarysql(final String date, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    T1.NEWS, ");
        stb.append("    T1.STAFFNEWS, ");
        stb.append("    T2.NAME1 AS WEATHER_NAME, ");
        stb.append("    T3.NAME1 AS WEATHER_NAME2 ");
        stb.append(" FROM ");
        stb.append("    SCHOOL_DIARY_DAT T1 ");
        stb.append("    LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A006' ");
        stb.append("        AND T2.NAMECD2 = T1.WEATHER ");
        stb.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'A006' ");
        stb.append("        AND T3.NAMECD2 = T1.WEATHER2 ");
        stb.append(" WHERE ");
        stb.append("        T1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("    AND T1.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append("    AND T1.DIARY_DATE  = '" + date + "' ");
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
        stb.append("SELECT  T1.GRADE, T1.HR_CLASS, T1.SCHREGNO, T5.SEX ");
        stb.append("FROM    SCHNO_A T1  ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO  ");
        stb.append("        LEFT JOIN SCHNO_B T2 ON T1.SCHREGNO = T2.SCHREGNO  ");
        return stb.toString();
    }

    private String getSchChrDatSql(final String date) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T2.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1, ");
        stb.append("     CHAIR_STD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE = '" + date + "' ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.CHAIRCD = T2.CHAIRCD ");
        stb.append("     AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        return stb.toString();
    }

    private String hiduke(final String date, final boolean getWeek, final String addText) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        if (getWeek) {
            sdf.applyPattern("yyyy年M月d日 E曜日");
        } else {
            sdf.applyPattern("yyyy年M月d日 ");
        }
        retVal = sdf.format(java.sql.Date.valueOf(date));
//        Locale local = new Locale("ja","JP");

        return retVal + addText;
    }

    private void printShoken(final Vrw32alp svf, final String field, final int keta, final int maxLine, final String data0) {
        final List data = get_token(data0, keta, maxLine);
        for (int i = 0; i < Math.min(data.size(), maxLine); i++) {
            svf.VrsOutn(field, (i + 1), (String) data.get(i));
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

    private static class HrClass {

        final String _gradehrclass;
        final String _grade;
        final String _gradeCd;
        final String _hrNameAbbv;
        final String _hrclass;
        final String _school_kind;
        final int _printGradeField;
        final int _printline;
        int _manZaiCnt = 0;
        int _woManZaiCnt = 0;
        final Map _schregMap;

        int _manAttendCnt = 0;
        int _woManAttendCnt = 0;
        int _manSickCnt = 0;
        int _woManSickCnt = 0;
        int _manLateCnt = 0;
        int _woManLateCnt = 0;
        int _manEarlyCnt = 0;
        int _woManEarlyCnt = 0;
        int _manMourningCnt = 0;
        int _woManMourningCnt = 0;

        int _manNoticeCnt = 0;
        int _woManNoticeCnt = 0;

        public HrClass(final String grade,
                       final String gradeCd,
                       final String hrclass,
                       final String hrNameAbbv,
                       final String school_kind,
                       final int gradeCnt,
                       final int printline
        ) {
            _grade = grade;
            _gradeCd = gradeCd;
            _hrclass = hrclass;
            _hrNameAbbv = hrNameAbbv;
            _gradehrclass = grade + hrclass;
            _school_kind = school_kind;
            _printGradeField = gradeCnt;
            _printline = printline;
            _schregMap = new HashMap();
        }

        public String toString() {
            return _gradehrclass;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 75272 $");
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
        final String _schoolCd;
        final String _schoolKind;
        final String _hibiNyuuryoku;
        final String _hibiNyuuryokuNasi;

        private Map _attendParamMap;
        final String _z010Name1;
        final String _z010Name2;
        final boolean _isHigashiosakaKashiwara;
        final boolean _useKNJG049_2;

        final List _schoolKindList;

        public Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException, ParseException {
            _prgId = request.getParameter("PRGID");
            if ("KNJG049".equals(_prgId)) {
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
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _hibiNyuuryoku = request.getParameter("hibiNyuuryoku");
            _hibiNyuuryokuNasi = request.getParameter("hibiNyuuryokuNasi");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _z010Name1= setZ010Name1(db2, "NAME1");
            _z010Name2 = setZ010Name1(db2, "NAME2");
            _isHigashiosakaKashiwara = "higashiosaka".equals(_z010Name1) && "30270247001".equals(_z010Name2);
            _useKNJG049_2 = "1".equals(request.getParameter("useKNJG049_2"));

            _schoolKindList = getSchoolKindList(db2, _year);
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /** 校種取得 */
        private List getSchoolKindList(DB2UDB db2, final String year) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     NAME1 ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("         YEAR    = '" + year + "' ");
                stb.append("     AND NAMECD1 = 'A023' ");
                stb.append("     AND NAME1  in ('J', 'H') ");
                stb.append(" ORDER BY ");
                stb.append("     NAMECD2 ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retList.add(rs.getString("NAME1"));
                }
            } catch (Exception ex) {
                log.debug("getA023 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }
    }
}

// eof

