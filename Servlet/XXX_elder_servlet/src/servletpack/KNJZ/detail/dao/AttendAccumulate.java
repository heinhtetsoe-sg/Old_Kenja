// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2007/12/04 10:49:01 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail.dao;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 出欠累積。
 * @author takaesu
 * @version $Id$
 */
public class AttendAccumulate {

    private static Log log = LogFactory.getLog(AttendAccumulate.class);
    private static String revision = "$Revision: 76900 $ $Date: 2020-09-16 10:49:10 +0900 (水, 16 9 2020) $++";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static String GRADE_ALL = "ZZZZZZZZ";

    /**
     * ATTEND_SEMES_DATの情報を返す。
     *  -- Key = 学期＋月
     *  -- Val = Map：SDAY(開始日付)
     *              ：EDAY(終了日付)
     * @param db2 db2
     * @param z010 近大か否かを判断する為の文字列
     * @param year 年度
     * @return ATTEND_SEMES_DATの各学期＋月毎の開始日付、終了日付を<code>Map</code>で返す。
     * @throws SQLException
     * @throws ParseException
     */
    public static Map getAttendSemesMap(
            final DB2UDB db2,
            final String z010,
            final String year
    ) throws SQLException, ParseException {
        return getAttendSemesMap(db2, z010, year, null);
    }

    /**
     * ATTEND_SEMES_DATの情報を返す。
     *  -- Key = 学期＋月
     *  -- Val = Map：SDAY(開始日付)
     *              ：EDAY(終了日付)
     * @param db2 db2
     * @param z010 近大か否かを判断する為の文字列
     * @param year 年度
     * @param paramMap パラメータのマップ
     * @return ATTEND_SEMES_DATの各学期＋月毎の開始日付、終了日付を<code>Map</code>で返す。
     * @throws SQLException
     * @throws ParseException
     */
    public static Map getAttendSemesMap(
            final DB2UDB db2,
            final String z010,
            final String year,
            final Map paramMap
    ) throws SQLException, ParseException {

        final String semesQuery = "SELECT SEMESTER, SDATE FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER < '9'";
        final Map semesMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, semesQuery), "SEMESTER", "SDATE");

        try {
            final String gSemesQuery = "SELECT GRADE, SEMESTER, SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER < '9'";
            for (final Map<String, String> row : KnjDbUtils.query(db2, gSemesQuery)) {
                getMappedMap(semesMap, "GRADE:" + getString(row, "GRADE")).put(getString(row, "SEMESTER"), getString(row, "SDATE"));
            }
        } catch (Exception e) {
            log.error("V_SEMESTER_GRADE_MST取得エラー:", e);
        }

        final Map rtnMap = new TreeMap();
        // {
        //   "GRADE:01":
        //     { "104": {"SDAY": "2019-04-01", "EDAY": "2019-04-30", "SM": "104"}
        //     , "105": {"SDAY": "2019-05-01", "EDAY": "2019-05-31", "SM": "105"}
        //     }
        // , "GRADE:02":
        //     { "104": {"SDAY": "2019-04-01", "EDAY": "2019-04-30", "SM": "104"}
        //     , "105": {"SDAY": "2019-05-01", "EDAY": "2019-05-31", "SM": "105"}
        //     }
        // , "GRADE:03":
        //     { "104": {"SDAY": "2019-04-01", "EDAY": "2019-04-30", "SM": "104"}
        //     , "105": {"SDAY": "2019-05-01", "EDAY": "2019-05-31", "SM": "105"}
        //     }
        //  ,    "104": {"SDAY": "2019-04-01", "EDAY": "2019-04-30", "SM": "104"}
        //  ,    "105": {"SDAY": "2019-05-01", "EDAY": "2019-05-31", "SM": "105"}
        // }
        try {
            final String sql = getAttendSemesAllSql(year, paramMap);
            logInfo(paramMap, "attendSemesAllSql = " + sql);

            final String defSday = isKindai(z010) ? "02" : "01";
            final String nextYear = String.valueOf(Integer.parseInt(year) + 1);

            Map gradeSemes = new HashMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                if (null == getString(row, "MAX_APPOINTED_DAY")) {
                    log.warn(" ignore appointed_day null month : semester = " + getString(row, "SEMESTER") + ", month = " + getString(row, "MONTH"));
                    continue;
                }

                final String endDay = getEndDay(getString(row, "ENDDAY"), z010);

                final String month = getString(row, "MONTH");

                String startDay = (Integer.parseInt(month) <= 3 ? nextYear : year) + "-" + month + "-" + defSday;

                final String grade = getString(row, "GRADE"); // 全体のデータは"X"
                final String bef_seme = null == gradeSemes.get(grade) ? "" : (String) gradeSemes.get(grade);
                final String semester = getString(row, "SEMESTER");
                if (!bef_seme.equals(semester)) {
                    String semeStartDay = null;
                    if (!"X".equals(grade)) {
                        semeStartDay = StringUtils.defaultString(getString(getMappedMap(semesMap, "GRADE:" + grade), semester), getString(semesMap, semester));
                    } else {
                        semeStartDay = getString(semesMap, semester);
                    }
                    if (toCalendar(semeStartDay).get(Calendar.MONTH) + 1 == Integer.parseInt(month)) {
                        startDay = semeStartDay;
                    }
                }
                final Map dataMap = new HashMap();
                dataMap.put("SDAY", startDay);
                dataMap.put("EDAY", endDay);
                dataMap.put("SM"  , getString(row, "SM"));
                final Map gradeMap;
                if (!"X".equals(grade)) {
                    gradeMap = getMappedMap(rtnMap, "GRADE:" + grade);
                } else {
                    gradeMap = rtnMap;
                }
                gradeMap.put(getString(row, "SM_KEY"), dataMap);

                gradeSemes.put(grade, semester);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }

        return rtnMap;
    }

    private static String getString(final Map m, final String key) {
        return (String) m.get(key);
    }

    private static Map getMappedMap(final Map m, final String key) {
        if (null == m.get(key)) {
            m.put(key, new TreeMap());
        }
        return (Map) m.get(key);
    }

    private static String getEndDay(final String endDay, final String z010) throws ParseException {
        if (isKindai(z010)) {

            final Calendar calEndDay = toCalendar(endDay);

            // 集計日が１の場合、翌月の１日と判断する
            if (calEndDay.get(Calendar.DATE) == 1) {
                calEndDay.add(Calendar.MONTH, 1);
            }
            return toDate(calEndDay);
        } else {
            return endDay;
        }
    }

    private static String getAttendSemesAllSql(final String year, final Map paramMap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_ATTEND AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SEMESTER || T1.MONTH AS SM, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.MONTH, ");
        stb.append("         VALUE(T2.GRADE, 'X') AS GRADE, ");
        stb.append("         MAX((CASE WHEN MONTH BETWEEN '01' AND '03' ");
        stb.append("                   THEN RTRIM(CHAR(INT(T1.YEAR) + 1)) ");
        stb.append("                   ELSE T1.YEAR ");
        stb.append("              END ) || '-' || T1.MONTH || '-' || T1.APPOINTED_DAY) AS ENDDAY, ");
        stb.append("         MAX(T1.APPOINTED_DAY) AS MAX_APPOINTED_DAY ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SEMES_DAT T1 ");
        stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ");
        if (null != paramMap) {
            if (!StringUtils.isEmpty((String) paramMap.get("attendSemesGrade"))) {
                stb.append("     INNER JOIN SCHREG_REGD_DAT I1 ON I1.SCHREGNO = T1.SCHREGNO AND I1.YEAR = T1.YEAR AND I1.SEMESTER = T1.SEMESTER AND I1.GRADE = '" + paramMap.get("attendSemesGrade") + "' ");
            } else if (!StringUtils.isEmpty((String) paramMap.get("attendSemesSchoolKind"))) {
                stb.append("     INNER JOIN SCHREG_REGD_DAT I1 ON I1.SCHREGNO = T1.SCHREGNO AND I1.YEAR = T1.YEAR AND I1.SEMESTER = T1.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = T1.YEAR AND I2.GRADE = T1.GRADE AND I2.SCHOOL_KIND = '" + paramMap.get("attendSemesSchoolKind") + "' ");
            }
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + year + "' ");
        stb.append("         AND T1.SEMESTER <> '9' "); // ゴミデータを含めない
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS((T1.SEMESTER || T1.MONTH, T1.SEMESTER, T1.MONTH), (T1.SEMESTER || T1.MONTH, T1.SEMESTER, T1.MONTH, T2.GRADE)) ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SM, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.MONTH, ");
        stb.append("     T1.ENDDAY, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.MAX_APPOINTED_DAY, ");
        stb.append("     (CASE WHEN INT(T1.MONTH) < 4 ");
        stb.append("           THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1)) ");
        stb.append("           ELSE T1.SEMESTER ");
        stb.append("      END ) || T1.MONTH AS SM_KEY ");
        stb.append(" FROM ");
        stb.append("     T_ATTEND T1 ");
        stb.append(" ORDER BY ");
        stb.append("     SM_KEY, GRADE ");

        return stb.toString();
    }

    public static String getPeiodValue(
            final DB2UDB db2,
            final KNJDefineCode definecode,
            final String year,
            final String sSemester,
            final String eSemester) throws SQLException {
        return getPeiodValue(db2, (KNJDefineSchool) definecode, year, sSemester, eSemester);
    }

    public static String getPeiodValue(
            final DB2UDB db2,
            final KNJDefineSchool definecode,
            final String year,
            final String sSemester,
            final String eSemester
    ) throws SQLException {
        //  校時名称
        StringBuffer stb2 = null;         //05/04/16
        final int periodnum;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    NAMECD2, ");
            stb.append("    NAME1, ");
            if (definecode != null && definecode.usefromtoperiod) {
                stb.append("    CASE WHEN NAMECD2 BETWEEN S_PERIODCD AND E_PERIODCD THEN 1 ELSE 0 END AS ONPERIOD ");
            } else {
                stb.append("    1 AS ONPERIOD ");
            }
            if (definecode != null && definecode.usefromtoperiod) {
                stb.append(" FROM ");
                stb.append("    NAME_MST W1, ");
                stb.append("    COURSE_MST W2 ");
                stb.append(" WHERE ");
                stb.append("    NAMECD1 = 'B001' ");
                stb.append("    AND COURSECD IN(SELECT ");
                stb.append("                        MIN(COURSECD) ");
                stb.append("                    FROM ");
                stb.append("                        SCHREG_REGD_DAT W3 ");
                stb.append("                    WHERE ");
                stb.append("                        W3.YEAR = '" + year + "' ");
                stb.append("                        AND W3.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
                stb.append("                    ) ");
            } else {
                stb.append(" FROM ");
                stb.append("    NAME_MST W1 ");
                stb.append(" WHERE ");
                stb.append("    NAMECD1 = 'B001' ");
            }
            stb.append("    AND EXISTS(SELECT ");
            stb.append("                   'X' ");
            stb.append("               FROM ");
            stb.append("                   NAME_YDAT W2 ");
            stb.append("               WHERE ");
            stb.append("                   W2.YEAR = '" + year + "' ");
            stb.append("                   AND W2.NAMECD1 = 'B001' ");
            stb.append("                   AND W2.NAMECD2 = W1.NAMECD2) ");
            stb.append(" ORDER BY ");
            stb.append("    NAMECD2 ");

            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, stb.toString());
            final Iterator it = rowList.iterator();
            List periodlist = new ArrayList();
            String sep = "";
            for (int i = 0; i < 16  &&  it.hasNext(); i++) {
                final Map row = (Map) it.next();
                if (Integer.parseInt(getString(row, "ONPERIOD")) == 1) {
                    periodlist.add(getString(row, "NAME1"));
                }
                if (Integer.parseInt(getString(row, "ONPERIOD")) == 1) {
                    if (stb2 == null) {
                        stb2 = new StringBuffer();
                        stb2.append("(");
                    }
                    stb2.append(sep + "'").append(getString(row, "NAMECD2")).append("'");
                    sep = ",";
                }
            }
            periodnum = ( periodlist.size() <= 9 ) ? 9: 16;

            if (stb2 != null) {
                stb2.append(")");
            } else if (periodnum == 9) {
                stb2 = new StringBuffer("('1','2','3','4','5','6','7','8','9')");
            } else {
                stb2 = new StringBuffer("('1','2','3','4','5','6','7','8','9','A','B','C','D','E','F''G')");
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return stb2.toString();
    }

    /**
     * 出欠データの端数取得
     * @param db2   DB2UDB
     * @param year              年度
     * @param sDate             指定範囲の開始日付
     * @param eDate             指定範囲の終了日付
     * @return 出欠データ端数取得の<code>Map</code>を返す
     */
    public static Map getHasuuMap(
            final DB2UDB db2,
            final String year,
            final String sDate,
            final String eDate
    ) {

        Map paramMap;
        paramMap = new HashMap();
        paramMap.put(KEY_DB2UDB, db2);
        paramMap.put("year", year);
        paramMap.put("sdate", sDate);
        paramMap = setParameterMap(paramMap, eDate);

        return (Map) paramMap.get(KEY_hasuuMap);
    }

    private static String mkString(final List<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String comma0 = "";
        for (final String s : list) {
            if (null == s || s.length() == 0) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    /**
     * 出欠データの端数取得
     * @param attendSemAllMap   getAttendSemesMap()で取得したMap
     * @param sDate             指定範囲の開始日付
     * @param eDate             指定範囲の終了日付
     * @return 出欠データ端数取得の<code>Map</code>を返す
     * @throws ParseException
     */
    public static Map getHasuuMap(
            final Map<String, Map> attendSemAllMap,
            final String sDate0,
            final String eDate0) throws ParseException {
        return getHasuuMap(null, attendSemAllMap, sDate0, eDate0);
    }


    /**
     * 出欠データの端数取得
     * @param paramMap
     * @param attendSemAllMap   getAttendSemesMap()で取得したMap
     * @param sDate             指定範囲の開始日付
     * @param eDate             指定範囲の終了日付
     * @return 出欠データ端数取得の<code>Map</code>を返す
     * @throws ParseException
     */
    public static Map getHasuuMap(
            final Map paramMap,
            final Map<String, Map> attendSemAllMap,
            final String sDate0,
            final String eDate0
    ) throws ParseException {
        final String sDate = sDate0.replace('/', '-');
        final String eDate = eDate0.replace('/', '-');

        String befSemMonth = null;
        String aftSemMonth = null;
        boolean semesFlg = false;

        //logInfo(paramMap, " attendSemAllMap keySet = " + attendSemAllMap.keySet());
        final List<String> smList = new ArrayList<String>();
        for (final String key : attendSemAllMap.keySet()) {
            final Map semeAllMap = attendSemAllMap.get(key);
            final String sday = (String) semeAllMap.get("SDAY");
            final String eday = (String) semeAllMap.get("EDAY");
            final String sm = (String) semeAllMap.get("SM");

            if (key.startsWith("GRADE:")) {
                continue;
            }

            try {
                /** 指定開始日付以上且つ、指定終了日付以下 */
                boolean dateCheck = (toCalendar(sday).after(toCalendar(sDate)) || toCalendar(sday).equals(toCalendar(sDate))) &&
                    (toCalendar(eday).before(toCalendar(eDate)) || toCalendar(eday).equals(toCalendar(eDate)));
                //logInfo(paramMap, " process key " + key + ", dateCheck = " + dateCheck);
                if (dateCheck) {
                    smList.add(sm);
                    aftSemMonth = key;

                    semesFlg = true;
                }

                if (smList.size() == 1 && sm.equals(smList.get(0))) {
                    befSemMonth = key;
                }
            } catch (RuntimeException e) {
                log.warn(" key = " + key + ", sday = " + sday + ", eday = " + eday + ", sm = " + sm + ", sDate = " + sDate + ", eDate = " + eDate + ", semeAllMap = " + semeAllMap, e);
                throw e;
            }
        }

        final String befDayFrom;  // 累積データ前の端数処理開始日
        final String befDayTo;    // 累積データ前の端数処理終了日
        final String aftDayFrom;  // 累積データ後の端数処理開始日
        final String aftDayTo;    // 累積データ後の端数処理終了日

        final StringBuffer attendSemesInState = new StringBuffer();

        if (semesFlg) {
            final String befSemMonthSday = (String) (attendSemAllMap.get(befSemMonth)).get("SDAY");
            if (sDate.equals(befSemMonthSday)) { // 集計範囲開始日が最初の累積データの開始日と同一なら、累積データ前の端数処理は不要
                befDayFrom = null;
                befDayTo = null;
            } else {
                befDayFrom = sDate;
                befDayTo = toDate(addDate(toCalendar(befSemMonthSday), -1));
            }
            logInfo(paramMap, "befSemMonthSday = " + befSemMonthSday + ", sDate = " + sDate + ", befDayFrom = " + befDayFrom + ", befDayTo = " + befDayTo);

            final String aftSemMonthEday = (String) (attendSemAllMap.get(aftSemMonth)).get("EDAY");
            if (eDate.equals(aftSemMonthEday)) { // 集計範囲終了日が最後の累積データの〆日と同一なら、累積データ後の端数処理は不要
                aftDayFrom = null;
                aftDayTo = null;
            } else {
                aftDayFrom = toDate(addDate(toCalendar(aftSemMonthEday), 1));
                aftDayTo = eDate;
            }
            attendSemesInState.append("('").append(mkString(smList, "','")).append("')");
            logInfo(paramMap, "aftSemMonthEday = " + aftSemMonthEday + ", eDate = " + eDate + ", aftDayFrom = " + aftDayFrom + ", aftDayTo = " + aftDayTo + ", attendSemesInState = " + attendSemesInState);

        } else {
            befDayFrom = sDate;
            befDayTo = eDate;
            aftDayFrom = null;
            aftDayTo = null;

            attendSemesInState.append("('')");
        }

        final Map rtnMap = createHasuuMap(semesFlg, attendSemesInState.toString(), befDayFrom, befDayTo, aftDayFrom, aftDayTo, smList);
        return rtnMap;
    }

    private static Calendar addDate(final Calendar cal, final int n) {
        cal.add(Calendar.DATE, n);
        return cal;
    }

    // TODO: 戻り値は Map ではなく、独自のclass を設けた方がよくね?
    public static Map createHasuuMap(final boolean semesFlg, final String attendSemesInState, final String befDayFrom, final String befDayTo, final String aftDayFrom, final String aftDayTo, final List attendSemesInList) {
        final Map rtnMap = new TreeMap();
        rtnMap.put(KEY_semesFlg, new Boolean(semesFlg));
        rtnMap.put(KEY_attendSemesInState, attendSemesInState);
        rtnMap.put(KEY_attendSemesInState + "List", attendSemesInList);
        rtnMap.put("befDayFrom", befDayFrom);
        rtnMap.put("befDayTo", befDayTo);
        rtnMap.put("aftDayFrom", aftDayFrom);
        rtnMap.put("aftDayTo", aftDayTo);
        return rtnMap;
    }

    private static String toDate(final Calendar cal) throws ParseException {
        return sdf.format(cal.getTime());
    }

    private static Calendar toCalendar(final String date) throws ParseException {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(date.replace('/', '-')));
        return cal;
    }

    private static void logInfo(final Map paramMap, final Object o) {
        logInfo(paramMap, o, true);
    }

    private static void logInfo(final Map paramMap, final Object o, final boolean outputFlg) {
        if (null != paramMap) {
            if ("1".equals(paramMap.get(KEY_outputDebug)) && outputFlg) {
                log.info(o);
            }
        }
    }

    private static String debugMapToStr(final String debugText, final Map map0) {
        final Map m = new HashMap();
        m.putAll(map0);
        for (final Iterator<Map.Entry> it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = it.next();
            if (e.getKey() instanceof Integer) {
                it.remove();
            } else if (e.getKey() instanceof String) {
                final String key = (String) e.getKey();
                final int numIndex = StringUtils.indexOfAny(key, "123456789");
                if (0 <= numIndex && StringUtils.repeat("_", numIndex).equals(key.substring(0, numIndex))) {
                    it.remove();
                }
            }
        }
        final Map map = new TreeMap(m);
        final StringBuffer stb = new StringBuffer();
        stb.append(StringUtils.defaultString(debugText));
        stb.append(" [");
        final List keys = new ArrayList(map.keySet());
        try {
            Collections.sort(keys);
        } catch (Exception e) {
        }
        for (int i = 0; i < keys.size(); i++) {
            final Object key = keys.get(i);
            stb.append(i == 0 ? "\n   " : " , ").append(key).append(": ").append(map.get(key)).append("\n");
        }
        stb.append("]");
        return stb.toString();
    }

    private static void setOutputDebug(final Map paramMap) {
        if (paramMap.containsKey(KEY_outputDebug)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        final String[] split = StringUtils.split(getDbPrginfoProperties(db2, KEY_outputDebug));
        if (null != split) {
            for (final String s : split) {
                if ("1".equals(s)) {
                    paramMap.put(KEY_outputDebug, s);
                } else if (-1 < s.indexOf("=")) {
                    final String[] pair = StringUtils.split(s, "=");
                    paramMap.put(pair[0], pair[1]);
                } else {
                    paramMap.put(s, "1");
                }
            }
        }
    }

    public static Map setParameterMap(Map paramMap, String date) {
        setOutputDebug(paramMap);
        setHasTableColumn(paramMap, "SCH_CHR_DAT", "EXECUTEDIV");
        setHasTableColumn(paramMap, "V_ATTEND_SEMES_DAT", "KEKKA_JISU");
        setHasTableColumn(paramMap, "V_ATTEND_SEMES_DAT", "TOCHU_KEKKA");
        setHasTableColumn(paramMap, "ATTEND_DI_CD_DAT", null);
        setHasTableColumn(paramMap, "CHAIR_DETAIL_DAT", null);
        setHasTableColumn(paramMap, "SCHOOL_MST", "SCHOOL_KIND");
//        setKNJSchoolMst(paramMap); // 引数にKNJSchoolMstが含まれていない場合、作成してパラメータとして使用する
        setKNJDefineSchool(paramMap); // 引数にKNJDefineSchoolが含まれていない場合
        setSsemester(paramMap);  // 引数にsSemesterが含まれていない場合
        setAbsentCovList(paramMap);  // 引数にsSemesterが含まれていない場合
        setPrgInfoPropertiesParameter(paramMap, KEY_DOCUMENTROOT);
        setProperties(paramMap);
        setPrgInfoPropertiesParameter(paramMap, KEY_useCurriculumcd);
        setPrgInfoPropertiesParameter(paramMap, KEY_useVirus);
        setPrgInfoPropertiesParameter(paramMap, KEY_useKoudome);
        setPrgInfoPropertiesParameter(paramMap, KEY_useTestCountflg);
        setPrgInfoPropertiesParameter(paramMap, KEY_use_school_detail_gcm_dat);
        paramMap = new HashMap(paramMap);
        setPeriodInState(paramMap);  // 引数にperiodInStateが含まれていない場合
        setHasuuMap(paramMap, date);  // 引数にhasuuMapが含まれていない場合
        for (final Iterator it = paramMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            logInfo(paramMap, " attendAccumulate parameter " + e);
        }
        return paramMap;
    }

    private static String KEY_DB2UDB = "DB2UDB";
    private static String KEY_outputDebug = "outputDebug";
    private static String KEY_sSemester = "sSemester";
    private static String KEY_semesFlg = "semesFlg";
    private static String KEY_hasuuMap = "hasuuMap";
    private static String KEY_attendSemesInState = "attendSemesInState";
    private static String KEY_attendSemesMap = "attendSemesMap";
    private static String KEY_periodInState = "periodInState";
    private static String KEY_knjDefineSchool = "knjDefineSchool";
    private static String KEY_knjSchoolMst = "knjSchoolMst";
    private static String KEY_z010Name1 = "z010Name1";
    private static String KEY_sdate = "sdate";
    private static String KEY_grade = "grade";
    private static String KEY_DOCUMENTROOT = "DOCUMENTROOT";
    private static String KEY_PropertyFile = "PropertyFile";
    private static String KEY_useCurriculumcd = "useCurriculumcd";
    private static String KEY_useVirus = "useVirus";
    private static String KEY_useKoudome = "useKoudome";
    private static String KEY_useTestCountflg = "useTestCountflg";
    private static String KEY_use_school_detail_gcm_dat = "use_school_detail_gcm_dat";
    private static String KEY_printSubclassLastChairStd = "printSubclassLastChairStd";
    private static String KEY_attendKekkaCalcKwanseiMethod = "attendKekkaCalcKwanseiMethod";
    private static String KEY_noOutputSql = "noOutputSql";

    private static String getRequiredParam(final Map paramMap, final String key) {
        if (!paramMap.containsKey(key)) {
            log.warn("not found param:" + key);
        }
        return (String) paramMap.get(key);
    }
    private static void setKNJDefineSchool(final Map paramMap) {
        if (null != paramMap.get(KEY_knjDefineSchool)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        final DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        try {
            logInfo(paramMap, "=AttendAccumulate.setKNJDefineSchool=");
            final KNJDefineSchool defineSchool = new KNJDefineSchool();
            defineSchool.defineCode(db2, getRequiredParam(paramMap, "year"));
            paramMap.put(KEY_knjDefineSchool, defineSchool);
        } catch (Exception ex) {
            log.error("exception!", ex);
        }
    }

    private static void setKNJSchoolMst(final Map paramMap) {
        if (null != paramMap.get(KEY_knjSchoolMst)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        final DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        try {
            logInfo(paramMap, "=AttendAccumulate.setKNJSchoolMst=");
            final KNJSchoolMst knjSchoolMst = new KNJSchoolMst(db2, getRequiredParam(paramMap, "year"));
            setZ010Name1(paramMap);  // 引数にz010Name1が含まれていない場合
            final String z010Name1 = (String) paramMap.get(KEY_z010Name1);
            if (isKindai(z010Name1)) {
                // 近大はテーブルに設定していないためプログラムで設定する
                knjSchoolMst._absentCov = "1";
                knjSchoolMst._absentCovLate = "3";
            }
            paramMap.put(KEY_knjSchoolMst, knjSchoolMst);
        } catch (Exception ex) {
            log.error("exception!", ex);
        }
    }

    /**
     * 指定テーブル名、カラム
     * @param paramMap
     * @param tabname テーブル名
     * @param colname カラム名（テーブルの有無のみ場合、null）
     */
    private static void setHasTableColumn(final Map paramMap, final String tabname, final String colname) {
        final String key = "hasTableColumn:" + tabname + (StringUtils.isBlank(colname) ? "" :  ("." + colname));
        if (paramMap.containsKey(key)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            paramMap.put(key, Boolean.FALSE); // テーブル、カラムはない
            return;
        }
        final DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        final boolean hasTableColumn = KnjDbUtils.setTableColumnCheck(db2, tabname, colname);
        paramMap.put(key, new Boolean(hasTableColumn));
        if (!hasTableColumn) {
            log.warn("=AttendAccumulate.setHasTableColumn= " + key + " = " + paramMap.get(key));
        } else {
            logInfo(paramMap, "=AttendAccumulate.setHasTableColumn= " + key + " = " + paramMap.get(key));
        }
    }

    private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
        if (!KnjDbUtils.setTableColumnCheck(db2, "PRGINFO_PROPERTIES", "PROGRAMID")) {
            return null;
        }
        return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'AttendAccumulate' AND NAME = '" + propName + "' ", null));
    }

    private static void setSsemester(final Map paramMap) {
        if (null != paramMap.get(KEY_sSemester)) {
            return;
        }
        paramMap.put(KEY_sSemester, "1"); // 開始学期をデフォルトで1学期とする
    }

    private static void setAbsentCovList(final Map paramMap) {
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        if (!paramMap.containsKey("year")) {
            return;
        }
        final DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        final String year = (String) paramMap.get("year");

        final String sql;
        if ("1".equals(paramMap.get(KEY_use_school_detail_gcm_dat))) {
            sql = " SELECT DISTINCT ABSENT_COV FROM V_SCHOOL_GCM_MST WHERE YEAR = '" + year + "' AND ABSENT_COV IS NOT NULL ";
        } else {
            sql = " SELECT DISTINCT ABSENT_COV FROM V_SCHOOL_MST WHERE YEAR = '" + year + "' AND ABSENT_COV IS NOT NULL ";
        }
        paramMap.put("ABSENT_COV_LIST", KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "ABSENT_COV"));
        logInfo(paramMap, "=AttendAccumulate.ABSENT_COV_LIST=" + paramMap.get("ABSENT_COV_LIST"));
    }

    private static void setZ010Name1(final Map paramMap) {
        if (paramMap.containsKey(KEY_z010Name1)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        final DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        paramMap.put(KEY_z010Name1, queryZ010Name1(db2));
        logInfo(paramMap, "=AttendAccumulate.setZ010Name1=" + paramMap.get(KEY_z010Name1));
    }

    /**
     * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
     */
    private static String queryZ010Name1(DB2UDB db2) {
        String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        return name1;
    }

    private static void setPeriodInState(final Map paramMap) {
        if (null != paramMap.get(KEY_periodInState)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        final DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        final KNJDefineSchool defineSchool = (KNJDefineSchool) paramMap.get(KEY_knjDefineSchool);
        final String year = (String) paramMap.get("year");
        final String sSemester = (String) paramMap.get(KEY_sSemester);
        final String semester = (String) paramMap.get("semester");
        try {
            paramMap.put(KEY_periodInState, getPeiodValue(db2, defineSchool, year, sSemester, semester));
            logInfo(paramMap, "=AttendAccumulate.setPeriodInState=" + paramMap.get(KEY_periodInState));
        } catch (Exception e) {
            log.fatal("excepion!", e);
        }
    }

    private static void setHasuuMap(final Map paramMap, final String date) {
        if (paramMap.containsKey(KEY_hasuuMap)) {
            return;
        }
        if (!paramMap.containsKey(KEY_DB2UDB)) {
            return;
        }
        DB2UDB db2 = (DB2UDB) paramMap.get(KEY_DB2UDB);
        if (!paramMap.containsKey(KEY_attendSemesMap)) {
            final String year = (String) paramMap.get("year");
            setZ010Name1(paramMap);  // 引数にz010Name1が含まれていない場合
            final String z010Name1 = (String) paramMap.get(KEY_z010Name1);
            try {
                paramMap.put(KEY_attendSemesMap, getAttendSemesMap(db2, z010Name1, year, paramMap));
                logInfo(paramMap, debugMapToStr("=AttendAccumulate.setAttendSemesMap=", (Map) paramMap.get(KEY_attendSemesMap)));
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }

        boolean setKeySdateDefault = false;
        if (null == paramMap.get(KEY_sdate)) {
            final String year = (String) paramMap.get("year");
            setSemester1Sdate(paramMap, KEY_sdate, db2, year);
            logInfo(paramMap, "=AttendAccumulate.setSemester1Sdate=" + paramMap.get(KEY_sdate));
            setKeySdateDefault = true;
        }

        final Map attendSemesMap = (Map) paramMap.get(KEY_attendSemesMap);
        if (null == date) {
            throw new IllegalArgumentException("引数paramMapにhasuuMapが含まれておらず引数dateがnullです。どちらかを指定してください");
        }
        try {
            final Map gradeHasuuMap = new HashMap();
            for (final Iterator<String> iter = attendSemesMap.keySet().iterator(); iter.hasNext();) {
                final String key = iter.next();
                if (null != key && key.startsWith("GRADE:")) {
                    final String semeGrade = key.substring(6);
                    final String sdate;
                    if (setKeySdateDefault) {
                        sdate = StringUtils.defaultString(getString(paramMap, KEY_sdate + "GRADE:" + semeGrade), getString(paramMap, KEY_sdate)); // ???
                    } else {
                        sdate = getString(paramMap, KEY_sdate);
                    }
                    gradeHasuuMap.put(key, getHasuuMap(paramMap, (Map) attendSemesMap.get(key), sdate, date));
                    logInfo(paramMap, " key = " + key + ", sdate = " + sdate + ", hasuuMap = " + gradeHasuuMap.get(key));
                    iter.remove();
                }
            }
            final String sdate = (String) paramMap.get(KEY_sdate);
            if (StringUtils.isBlank(sdate) || StringUtils.isBlank(date)) {
                logInfo(paramMap, " sdate = " + sdate + ", date = " + date);
            }
            final Map hasuuMap = getHasuuMap(paramMap, attendSemesMap, sdate, date);
            paramMap.put(KEY_hasuuMap, hasuuMap);
            if (!gradeHasuuMap.isEmpty()) {
                hasuuMap.putAll(gradeHasuuMap);
                hasuuMap.put(GRADE_ALL, createHasuuMap(false, null, sdate, date, null, null, null)); // 指定学年の累積データがない場合の日付範囲（指定の開始日付～終了日付）
            }
            logInfo(paramMap, debugMapToStr("=AttendAccumulate.setHasuuMap=", (Map) paramMap.get(KEY_hasuuMap)));
        } catch (Exception e) {
            log.fatal("excepion!", e);
        }
    }


    /**
     * 学期マスタ (SEMESTER_MST) をロードする
     * @param db2
     */
    private static void setSemester1Sdate(final Map paramMap, final String key, final DB2UDB db2, final String year) {
        final String[] semes = {"1", "9"};
        boolean hasRecord = false;
        try {
            hasRecord = false;
            for (int i = 0; i < semes.length; i++) {
                final String sql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semes[i] + "' ORDER BY SEMESTER";
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    final String sdate = getString(row, "SDATE");
                    paramMap.put(key, sdate);
                    logInfo(paramMap, "set " + year + "-" + semes[i] + " sdate = " + sdate);
                    if (null != sdate) {
                        hasRecord = true;
                    }
                }
                if (hasRecord) {
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        }
        try {
            hasRecord = false;
            for (int i = 0; i < semes.length; i++) {
                final String sql = "SELECT GRADE, SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semes[i] + "' ORDER BY SEMESTER";
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    final String sdate = getString(row, "SDATE");
                    paramMap.put(key + "GRADE:" + getString(row, "GRADE"), sdate);
                    if (null != sdate) {
                        hasRecord = true;
                    }
                }
                if (hasRecord) {
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("V_SEMESTER_GRADE_MST取得エラー:", ex);
        }
    }

    /**
     * パラメータがマップに含まれておらずHttpServletRequestがマップに含まれている場合、HttpServletRequestからパラメータを読み取る
     * @param paramMap セットするマップ
     * @param key パラメータ名
     */
    private static void setPrgInfoPropertiesParameter(final Map paramMap, final String key) {
        if (!paramMap.containsKey(key)) {
            if (null != paramMap.get("HttpServletRequest")) {
                final HttpServletRequest request = (HttpServletRequest) paramMap.get("HttpServletRequest");
                if (!request.getParameterMap().containsKey(key)) {
                    final Properties props = (Properties) paramMap.get(KEY_PropertyFile);
                    if (null != props && props.containsKey(key)) {
                        paramMap.put(key, props.getProperty(key));
                        logInfo(paramMap, "=AttendAccumulate.setPrgInfoPropertiesParameter(f)=(" + key + "=" + paramMap.get(key) + ")");
                        return;
                    }
                }
                paramMap.put(key, request.getParameter(key));
                logInfo(paramMap, "=AttendAccumulate.setPrgInfoPropertiesParameter=(" + key + "=" + paramMap.get(key) + ")");
            }
        }
    }

    private static void setProperties(final Map paramMap) {
        final String documentroot = getString(paramMap, KEY_DOCUMENTROOT);
        if (null == documentroot) {
            return;
        }
        final String filename = "prgInfo.properties";
        File file = null;
        if (null != documentroot) {
            file = new File(new File(documentroot).getParentFile().getAbsolutePath() + "/config/" + filename);
            if ("1".equals(paramMap.get(KEY_outputDebug))) {
                log.info("check prop : " + file.getAbsolutePath() + ", exists? " + file.exists());
            }
            if (!file.exists()) {
                file = null;
            }
        }
        if (null == file) {
            file = new File(documentroot + "/" + filename);
        }
        if (!file.exists()) {
            logInfo(paramMap, "file not exists: " + file.getAbsolutePath());
            return;
        }
        logInfo(paramMap, "file : " + file.getAbsolutePath() + ", " + file.length());
        final Properties props = new Properties();
        FileReader r = null;
        try {
            r = new FileReader(file);
            props.load(r);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != r) {
                try {
                    r.close();
                } catch (Exception _ignored) {
                }
            }
        }
        paramMap.put(KEY_PropertyFile, props);
    }

    private static boolean isKindai(final String z010) {
        return "KINDAI".equals(z010);
    }

    private static String whereIn(final boolean skipNull, final String[] array) {
        if (null == array || 0 == array.length) {
            return null;
        }

        final StringBuffer sb = new StringBuffer();
        int n = 0;
        for (int i = 0; i < array.length; i++) {
            if (null == array[i] && skipNull) {
                continue;
            }

            if (0 == n) { sb.append("("); }
            if (0 != n) { sb.append(", "); }

            if (null == array[i]) {
                sb.append(String.valueOf(array[i])); // "null"
            } else {
                sb.append('\'');
                sb.append(array[i]);
                sb.append('\'');
            }
            //--
            n++;
        }

        if (0 == n) {
            return null;
        }

        sb.append(")");
        return sb.toString();
    }

    private static void putParam(final Map paramMap,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String sSemester,
            final String periodInState,
            final Map hasuuMap,
            final String grade,
            final String hrClass,
            final String schregno
            ) {
        paramMap.put(KEY_knjDefineSchool, defineSchool);
        paramMap.put(KEY_knjSchoolMst, knjSchoolMst);
        paramMap.put(KEY_sSemester, sSemester);
        paramMap.put(KEY_periodInState, periodInState);
        paramMap.put(KEY_hasuuMap, hasuuMap);
        paramMap.put(KEY_grade, grade);
        paramMap.put("hrClass", hrClass);
        paramMap.put("schregno", schregno);
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSemesSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String groupByDiv
    ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("groupByDiv", groupByDiv);

        return getAttendSemesSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSemesSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String groupByDiv,
            final String useCurriculumcd
          ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("groupByDiv", groupByDiv);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);

        return getAttendSemesSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * 1日出欠データSQLを返す
     * @param year          年度
     * @param eSemester     対象学期範囲To
     * @param day           日付
     * @param paramMap      その他パラメータ
     * @return 出欠データSQL<code>String</code>を返す
     */
    public static String getAttendDatOneDaySql(
            final String year,
            final String eSemester,
            final String day,
            Map paramMap
    ) {
        final Map hasuuMap = createHasuuMap(false, null, day, day, null, null, null);
        putParam(paramMap, null, null, null, null, hasuuMap, null, null, null);

        return getAttendSemesSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * 出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * -- 実行結果の SEMESTER が "9" は学期の総合計
     * -- 学年、クラス、学籍番号に"?"を指定したときはPreparedStatementで代入する
     * @param semesFlg      true:ATTEND_SEMES_DAT使用
     * @param defineSchool  KNJDefineSchool
     * @param year          年度
     * @param sSemester     対象学期範囲From
     * @param eSemester     対象学期範囲To
     * @param semesInState  ATTEND_SEMES_DATの対象(学期＋月)
     * @param periodInState 対象校時
     * @param befDayFrom    開始日付の端数用From
     * @param befDayTo      開始日付の端数用To
     * @param aftDayFrom    終了日付の端数用From
     * @param aftDayTo      終了日付の端数用To
     * @param grade         学年：指定しない場合は、Null
     * @param hrClass       クラス：指定しない場合は、Null
     * @param schregno      学籍番号：指定しない場合は、Null
     * @param groupByDiv    グループ化区分：HR_CLASS(クラス単位)、GRADE(学年単位)、SCHREGNO(学籍単位)、SEMESTER(生徒学期単位)
     * @param useCurriculumcd 1=教育課程コードを使用する
     * @param useVirus 1=VIRUSフィールドを使用する
     * @return 出欠データSQL<code>String</code>を返す
     */
    public static String getAttendSemesSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String groupByDiv,
            final String useCurriculumcd,
            final String useVirus,
            final String useKoudome) {

        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("groupByDiv", groupByDiv);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);
        paramMap.put(KEY_useVirus, useVirus);
        paramMap.put(KEY_useKoudome, useKoudome);

        return getAttendSemesSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * 出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * -- 実行結果の SEMESTER が "9" は学期の総合計
     * -- 学年、クラス、学籍番号に"?"を指定したときはPreparedStatementで代入する
     * @param semesFlg      true:ATTEND_SEMES_DAT使用
     * @param defineSchool  KNJDefineSchool
     * @param year          年度
     * @param sSemester     対象学期範囲From
     * @param eSemester     対象学期範囲To
     * @param semesInState  ATTEND_SEMES_DATの対象(学期＋月)
     * @param periodInState 対象校時
     * @param befDayFrom    開始日付の端数用From
     * @param befDayTo      開始日付の端数用To
     * @param aftDayFrom    終了日付の端数用From
     * @param aftDayTo      終了日付の端数用To
     * @param grade         学年：指定しない場合は、Null
     * @param hrClass       クラス：指定しない場合は、Null
     * @param schregno      学籍番号：指定しない場合は、Null
     * @param groupByDiv    グループ化区分：HR_CLASS(クラス単位)、GRADE(学年単位)、SCHREGNO(学籍単位)、SEMESTER(生徒学期単位)
     * @param paramMap      その他パラメータ
     * @return 出欠データSQL<code>String</code>を返す
     */
    public static String getAttendSemesSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String groupByDiv,
            Map paramMap
    ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("groupByDiv", groupByDiv);

        return getAttendSemesSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    private static int FLG_ATTEND_SEMES = 0;
    private static int FLG_ATTEND_SUBCLASS_ABSENCE = 1;
    private static int FLG_ATTEND_SUBCLASS = 2;

    /**
     * 出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * -- 実行結果の SEMESTER が "9" は学期の総合計
     * -- 学年、クラス、学籍番号に"?"を指定したときはPreparedStatementで代入する
     * @param year          年度
     * @param eSemester     対象学期範囲To
     * @param sDate0        出欠集計開始日付
     * @param date          集計日付
     * @param paramMap      その他パラメータ
     * @return 出欠データSQL<code>String</code>を返す
     */
    public static String getAttendSemesSql(
            final String year,
            final String eSemester,
            final String sDate0,
            final String date,
            Map paramMap
    ) {
        logInfo(paramMap, "call getAttendSemesSql('" + year + "', '" + eSemester + "', '" + sDate0 + "', '" + date + "', " + paramMap + ")");
        paramMap.put("year", year);
        paramMap.put(KEY_sdate, sDate0);
        paramMap = setParameterMap(paramMap, date);

        final String sSemester = getString(paramMap, KEY_sSemester);
        final Map hasuuMap = (Map) paramMap.get(KEY_hasuuMap);
        final boolean semesFlg = ((Boolean) hasuuMap.get(KEY_semesFlg)).booleanValue();
        final String semesInState = semesFlg ? StringUtils.defaultString((String) hasuuMap.get(KEY_attendSemesInState), " ('') ") : " ('') ";
        final StringBuffer gradeSemeMonth = getGradeSemeMonthString(paramMap, "W2.GRADE", "W1.SEMESTER", "W1.MONTH", hasuuMap);

        final String useCurriculumcd = getString(paramMap, KEY_useCurriculumcd); // 1=教育課程コードを使用する
        final String useVirus = getString(paramMap, KEY_useVirus); // true=VIRUSフィールドを使用する
        final String useKoudome = getString(paramMap, KEY_useKoudome); // true=KOUDOMEフィールドを使用する

        final String groupByDiv = StringUtils.defaultString(getString(paramMap, "groupByDiv"), "SEMESTER");

        final boolean hasVAttendSemesDatKekkaJisu = ((Boolean) paramMap.get("hasTableColumn:V_ATTEND_SEMES_DAT.KEKKA_JISU")).booleanValue();
        final boolean hasVAttendSemesDatTochuKekka = ((Boolean) paramMap.get("hasTableColumn:V_ATTEND_SEMES_DAT.TOCHU_KEKKA")).booleanValue();
        final boolean kouketsuhaD110toOnaji = true; // 公欠をD110と同様にカウントする。処理を元に戻すにはfalseにする

        final StringBuffer stb = new StringBuffer();

        appendSchregScheduleCommon(FLG_ATTEND_SEMES, stb, paramMap, hasuuMap, year, eSemester);

        //対象生徒の出欠データ
        stb.append(" ), T_ATTEND_DAT AS( ");
        stb.append(" SELECT ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    T0.ATTENDDATE, ");
        stb.append("    T0.PERIODCD, ");
        stb.append("    VALUE(L1.REP_DI_CD, T0.DI_CD) AS DI_CD, ");
        stb.append("    T1.DATADIV, ");
        stb.append("    T10.SYUKESSEKI_HANTEI_HOU ");
        stb.append(" FROM ");
        stb.append("    ATTEND_DAT T0 ");
        stb.append("    INNER JOIN SCHEDULE_SCHREG T1 ON T0.YEAR = '" + year + "' ");
        stb.append("        AND T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("        AND T0.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("        AND T0.PERIODCD = T1.PERIODCD ");
        stb.append("    INNER JOIN SCHNO T10 ON T10.SCHREGNO = T0.SCHREGNO ");
        stb.append("        AND T10.SEMESTER = T1.SEMESTER ");
        stb.append("    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + year + "' AND L1.DI_CD = T0.DI_CD ");

        //対象生徒の科目コードごとの出欠データ
        stb.append(" ), T_ATTEND_DAT_SUBCLASS AS( ");
        stb.append(" SELECT ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    T0.ATTENDDATE, ");
        stb.append("    T0.PERIODCD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T2.CLASSCD, ");
            stb.append("    T2.CURRICULUM_CD, ");
            stb.append("    T2.SCHOOL_KIND, ");
        }
        stb.append("    T2.SUBCLASSCD, ");
        stb.append("    L1.REP_DI_CD AS DI_CD, ");
        stb.append("    L1.MULTIPLY, ");
        stb.append("    L1.ATSUB_REPL_DI_CD ");
        stb.append(" FROM ");
        stb.append("    ATTEND_DAT T0 ");
        stb.append("    INNER JOIN SCHEDULE_SCHREG T1 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("        AND T0.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("        AND T0.PERIODCD = T1.PERIODCD ");
        stb.append("    INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T0.YEAR ");
        stb.append("        AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("        AND T2.CHAIRCD = T0.CHAIRCD ");
        stb.append("    INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD ");
        stb.append(" WHERE ");
        stb.append("    T0.YEAR = '" + year + "' ");

        //対象生徒の出欠データ（忌引・出停した日）
        stb.append(" ), T_ATTEND_DAT_B AS( ");
        stb.append(" SELECT ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T0.SEMESTER, ");
        stb.append("    T0.ATTENDDATE, ");
        stb.append("    T0.SYUKESSEKI_HANTEI_HOU, ");
        stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
        stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
        stb.append(" FROM ");
        stb.append("    T_ATTEND_DAT T0 ");
        stb.append(" WHERE ");
        stb.append("    DI_CD IN ('2','3','9','10' ");
        if ("true".equals(useVirus)) {
            stb.append("    , '19','20' ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("    , '25','26' ");
        }
        stb.append("    ) ");
        stb.append(" GROUP BY ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T0.SEMESTER, ");
        stb.append("    T0.ATTENDDATE, ");
        stb.append("    T0.SYUKESSEKI_HANTEI_HOU ");

        //対象生徒の日単位の最小校時・最大校時・校時数
        stb.append(" ), T_PERIOD_CNT AS( ");
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    T1.EXECUTEDATE, ");
        stb.append("    MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
        stb.append("    MAX(T1.PERIODCD) AS LAST_PERIOD, ");
        stb.append("    COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
        stb.append(" FROM ");
        stb.append("    SCHEDULE_SCHREG T1 ");
        stb.append(" GROUP BY ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    T1.EXECUTEDATE ");

        //対象生徒の日単位のデータ（忌引・出停した日）
        stb.append(" ), T_PERIOD_SUSPEND_MOURNING AS( ");
        stb.append(" SELECT ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T0.EXECUTEDATE ");
        stb.append(" FROM ");
        stb.append("    T_PERIOD_CNT T0 ");
        stb.append("    INNER JOIN T_ATTEND_DAT_B T1 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("        AND T0.EXECUTEDATE = T1.ATTENDDATE ");
        stb.append("        AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
        stb.append("        AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
        stb.append(" WHERE ");
        stb.append("    T1.SYUKESSEKI_HANTEI_HOU = '1' ");

        if (kouketsuhaD110toOnaji) {
            stb.append(" ), T_KOUKETSU AS ( ");
            stb.append("    SELECT ");
            stb.append("        W0.SCHREGNO, ");
            stb.append("        VALUE(W0.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        W0.ATTENDDATE ");
            stb.append("    FROM ");
            stb.append("        T_ATTEND_DAT W0 ");
            stb.append("    WHERE ");
            stb.append("        W0.DI_CD IN ('1', '8') ");
            stb.append("    GROUP BY ");
            stb.append("        W0.SCHREGNO, ");
            stb.append("        VALUE(W0.SEMESTER, '9'), ");
            stb.append("        W0.ATTENDDATE ");
        }

        stb.append(" ), T_KESSEKI AS ( ");
        stb.append("    SELECT ");
        stb.append("        W0.SCHREGNO, ");
        stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        W0.DI_CD, ");
        stb.append("        W0.ATTENDDATE ");
        stb.append("    FROM ");
        stb.append("        ATTEND_DAT W0 ");
        stb.append("        INNER JOIN SEMESTER_MST SEME ON SEME.SEMESTER <> '9' AND W0.ATTENDDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
        stb.append("        INNER JOIN SCHNO T10 ON T10.SCHREGNO = W0.SCHREGNO ");
        stb.append("                            AND T10.SEMESTER = SEME.SEMESTER ");
        stb.append("        INNER JOIN (SELECT ");
        stb.append("             T0.SCHREGNO, ");
        stb.append("             T0.SEMESTER, ");
        stb.append("             T0.EXECUTEDATE, ");
        stb.append("             CASE WHEN T10.SYUKESSEKI_HANTEI_HOU = '1' THEN T2.FIRST_PERIOD ELSE T0.FIRST_PERIOD END AS FIRST_PERIOD ");
        stb.append("         FROM ");
        stb.append("             T_PERIOD_CNT T0 ");
        stb.append("             INNER JOIN SCHNO T10 ON T10.SCHREGNO = T0.SCHREGNO ");
        stb.append("                                 AND T10.SEMESTER = T0.SEMESTER ");
        stb.append("             INNER JOIN ( ");
        stb.append("              SELECT ");
        stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
        stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
        stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append("              FROM ");
        stb.append("                  T_ATTEND_DAT W1 ");
        stb.append("              WHERE ");
        stb.append("                  (W1.SYUKESSEKI_HANTEI_HOU = '0' AND W1.DI_CD IN ( ");
        if (kouketsuhaD110toOnaji) {
            stb.append("                      '4','5','6','11','12','13' ");
        } else {
            stb.append("                  '1','4','5','6','11','12','13' ");
        }
        stb.append("                  ) OR W1.SYUKESSEKI_HANTEI_HOU = '1' AND W1.DI_CD IN ( ");
        if (kouketsuhaD110toOnaji) {
            stb.append("                      '4','5','6','11','12','13', '2','9','3','10' ");
        } else {
            stb.append("                  '1','4','5','6','11','12','13', '2','9','3','10' ");
        }
        if ("true".equals(useVirus)) {
            stb.append("    , '19','20' ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("    , '25','26' ");
        }
        stb.append("                  )) ");
        stb.append("              GROUP BY ");
        stb.append("                  W1.SCHREGNO, ");
        stb.append("                  W1.ATTENDDATE ");
        stb.append("             ) T1 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("                 AND T0.EXECUTEDATE = T1.ATTENDDATE ");
        stb.append("                 AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
        stb.append("                 AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
        stb.append("         LEFT JOIN ( ");
        stb.append("              SELECT ");
        stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
        stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
        stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append("              FROM ");
        stb.append("                  T_ATTEND_DAT W1 ");
        stb.append("              WHERE ");
        if (kouketsuhaD110toOnaji) {
            stb.append("                  W1.DI_CD IN (    '4','5','6','11','12','13') ");
        } else {
            stb.append("                  W1.DI_CD IN ('1','4','5','6','11','12','13') ");
        }
        stb.append("              GROUP BY ");
        stb.append("                  W1.SCHREGNO, ");
        stb.append("                  W1.ATTENDDATE ");
        stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
        stb.append("            WHERE  ");
        stb.append("               (T10.SYUKESSEKI_HANTEI_HOU = '0' OR T10.SYUKESSEKI_HANTEI_HOU = '1' AND T2.SCHREGNO IS NOT NULL) ");
        stb.append("        ) W1 ON W0.SCHREGNO = W1.SCHREGNO ");
        stb.append("        AND W0.ATTENDDATE = W1.EXECUTEDATE ");
        stb.append("        AND W0.PERIODCD = W1.FIRST_PERIOD ");
        stb.append("        LEFT JOIN ATTEND_DI_CD_DAT W2 ON W2.YEAR = '" + year + "' AND W2.DI_CD = W0.DI_CD ");
        stb.append(" WHERE (T10.SYUKESSEKI_HANTEI_HOU = '0' OR T10.SYUKESSEKI_HANTEI_HOU = '1' AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0)) ");

        //対象生徒の出欠データ
        stb.append(" ), T_ATTEND_DAT_JISU AS( ");
        stb.append(" SELECT ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T0.SEMESTER, ");
        stb.append("    SUM(CASE WHEN ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T0.CLASSCD = '92' ");
        } else {
            stb.append("    SUBSTR(T0.SUBCLASSCD, 1, 2) = '92' ");
        }
        stb.append("       AND (CASE WHEN T0.DI_CD IN ('29','30','31') THEN VALUE(T0.ATSUB_REPL_DI_CD, T0.DI_CD) ELSE T0.DI_CD END) IN ('4','5','6','11','12','13') THEN 1 ELSE 0 END) AS REIHAI_KEKKA, ");
        stb.append("    SUM(CASE WHEN ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T0.CLASSCD <> '92' ");
        } else {
            stb.append("    SUBSTR(T0.SUBCLASSCD, 1, 2) <> '92' ");
        }
        stb.append("       AND (CASE WHEN T0.DI_CD IN ('29','30','31') THEN VALUE(T0.ATSUB_REPL_DI_CD, T0.DI_CD) ELSE T0.DI_CD END) IN ('4','5','6','11','12','13') THEN 1 ELSE 0 END) AS KEKKA_JISU, ");
        stb.append("    SUM(CASE WHEN ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T0.CLASSCD = '92' ");
        } else {
            stb.append("    SUBSTR(T0.SUBCLASSCD, 1, 2) = '92' ");
        }
        stb.append("       AND (CASE WHEN T0.DI_CD IN ('29','30','31') THEN VALUE(T0.ATSUB_REPL_DI_CD, T0.DI_CD) ELSE T0.DI_CD END) IN ('15','23','24') THEN SMALLINT(VALUE(T0.MULTIPLY, '1')) ELSE 0 END ");
        stb.append("       ) AS REIHAI_TIKOKU, ");
        stb.append("    SUM(CASE WHEN ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T0.CLASSCD <> '92' ");
        } else {
            stb.append("    SUBSTR(T0.SUBCLASSCD, 1, 2) <> '92' ");
        }
        stb.append("       AND (CASE WHEN T0.DI_CD IN ('29','30','31') THEN VALUE(T0.ATSUB_REPL_DI_CD, T0.DI_CD) ELSE T0.DI_CD END) IN ('15','23','24') THEN SMALLINT(VALUE(T0.MULTIPLY, '1')) ELSE 0 END ");
        stb.append("       ) AS JYUGYOU_TIKOKU, ");
        stb.append("    SUM(CASE WHEN ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T0.CLASSCD <> '92' ");
        } else {
            stb.append("    SUBSTR(T0.SUBCLASSCD, 1, 2) <> '92' ");
        }
        stb.append("       AND (CASE WHEN T0.DI_CD IN ('29','30','31') THEN VALUE(T0.ATSUB_REPL_DI_CD, T0.DI_CD) ELSE T0.DI_CD END) IN ('16') THEN SMALLINT(VALUE(T0.MULTIPLY, '1')) ELSE 0 END ");
        stb.append("       ) AS JYUGYOU_SOUTAI ");
        stb.append(" FROM ");
        stb.append("    T_ATTEND_DAT_SUBCLASS T0 ");
        stb.append("    LEFT JOIN T_KESSEKI L2 ON L2.SCHREGNO = T0.SCHREGNO AND L2.SEMESTER = T0.SEMESTER AND L2.ATTENDDATE = T0.ATTENDDATE ");
        stb.append(" WHERE ");
        stb.append("    L2.SCHREGNO IS NULL ");
        stb.append(" GROUP BY ");
        stb.append("    T0.SCHREGNO, ");
        stb.append("    T0.SEMESTER ");
        stb.append(     ") ");

        //個人別出停日数
        stb.append(" , SCHRENGO_SUSPEND(SCHREGNO, SEMESTER, SUSPEND) AS ( ");
        stb.append("    SELECT ");
        stb.append("        W1.SCHREGNO, ");
        stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS SUSPEND ");
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT W1 ");
        stb.append("    WHERE ");
        stb.append("        W1.DI_CD IN ('2','9') ");
        stb.append("    AND (W1.SYUKESSEKI_HANTEI_HOU = '0' OR W1.SYUKESSEKI_HANTEI_HOU = '1' AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0)) ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
        stb.append(" ) ");

        //個人別出停伝染病日数
        stb.append(" , SCHRENGO_VIRUS(SCHREGNO, SEMESTER, VIRUS) AS ( ");
        stb.append("    SELECT ");
        stb.append("        W1.SCHREGNO, ");
        stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
        if ("true".equals(useVirus)) {
            stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS VIRUS ");
        } else {
            stb.append("        0 AS VIRUS ");
        }
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT W1 ");
        stb.append("    WHERE ");
        stb.append("        W1.DI_CD IN ('19','20') ");
        stb.append("    AND (W1.SYUKESSEKI_HANTEI_HOU = '0' OR W1.SYUKESSEKI_HANTEI_HOU = '1' AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0)) ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
        stb.append(" ) ");

        //個人別出停公止日数
        stb.append(" , SCHRENGO_KOUDOME(SCHREGNO, SEMESTER, KOUDOME) AS ( ");
        stb.append("    SELECT ");
        stb.append("        W1.SCHREGNO, ");
        stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
        if ("true".equals(useKoudome)) {
            stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS KOUDOME ");
        } else {
            stb.append("        0 AS KOUDOME ");
        }
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT W1 ");
        stb.append("    WHERE ");
        stb.append("        W1.DI_CD IN ('25','26') ");
        stb.append("    AND (W1.SYUKESSEKI_HANTEI_HOU = '0' OR W1.SYUKESSEKI_HANTEI_HOU = '1' AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0)) ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
        stb.append(" ) ");

        //個人別忌引日数
        stb.append(" , SCHRENGO_MOURNING(SCHREGNO, SEMESTER, MOURNING) AS ( ");
        stb.append("    SELECT ");
        stb.append("        W1.SCHREGNO, ");
        stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS MOURNING ");
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT W1 ");
        stb.append("    WHERE ");
        stb.append("        W1.DI_CD IN ('3','10') ");
        stb.append("    AND (W1.SYUKESSEKI_HANTEI_HOU = '0' OR W1.SYUKESSEKI_HANTEI_HOU = '1' AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0)) ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
        stb.append(" ) ");

        if (kouketsuhaD110toOnaji) {
            //個人別公欠日数
            stb.append(" , SCHRENGO_KOUKETSU(SCHREGNO, SEMESTER, ABSENT) AS ( ");
            stb.append("    SELECT ");
            stb.append("        W0.SCHREGNO, ");
            stb.append("        VALUE(W0.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        COUNT(ATTENDDATE) AS ABSENT ");
            stb.append("    FROM ");
            stb.append("        T_KOUKETSU W0 ");
            stb.append("    GROUP BY ");
            stb.append("        GROUPING SETS ((W0.SCHREGNO, W0.SEMESTER), (W0.SCHREGNO)) ");
            stb.append(" ) ");
        }

        //個人別欠席日数
        if (kouketsuhaD110toOnaji) {
            stb.append(" , SCHRENGO_ABSENT(SCHREGNO, SEMESTER, SICK, NOTICE, NONOTICE) AS ( ");
        } else {
            stb.append(" , SCHRENGO_ABSENT(SCHREGNO, SEMESTER, ABSENT, SICK, NOTICE, NONOTICE) AS ( ");
        }

        stb.append("    SELECT ");
        stb.append("        W0.SCHREGNO, ");
        stb.append("        VALUE(W0.SEMESTER, '9') AS SEMESTER, ");
        if (kouketsuhaD110toOnaji) {
        } else {
            stb.append("        SUM(CASE W0.DI_CD WHEN '1' THEN 1 ELSE 0 END) AS ABSENT, ");
        }
        stb.append("        SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
        stb.append("        SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
        stb.append("        SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
        stb.append("    FROM ");
        stb.append("        T_KESSEKI W0 ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((W0.SCHREGNO, W0.SEMESTER), (W0.SCHREGNO)) ");
        stb.append(" ) ");

        //個人別遅刻・早退回数
        stb.append(" , SCHRENGO_LATE_EARLY(SCHREGNO, SEMESTER, LATE, EARLY) AS ( ");
        stb.append("    SELECT ");
        stb.append("        T0.SCHREGNO, ");
        stb.append("        VALUE(T0.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        COUNT(T2.ATTENDDATE) AS LATE, ");
        stb.append("        COUNT(T3.ATTENDDATE) AS EARLY ");
        stb.append("    FROM ");
        stb.append("        T_PERIOD_CNT T0 ");
        stb.append("        INNER JOIN( ");
        stb.append("            SELECT ");
        stb.append("                W1.SCHREGNO, ");
        stb.append("                W1.ATTENDDATE, ");
        stb.append("                COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append("            FROM ");
        stb.append("                T_ATTEND_DAT W1 ");
        stb.append("                INNER JOIN SCHNO T10 ON T10.SCHREGNO = W1.SCHREGNO ");
        stb.append("                    AND T10.SEMESTER = W1.SEMESTER ");
        stb.append("            WHERE ");
        stb.append("                W1.DI_CD NOT IN ('0','14','15','16','23','24') ");
        stb.append("                AND (W1.SYUKESSEKI_HANTEI_HOU = '1' OR W1.SYUKESSEKI_HANTEI_HOU <> '1' AND NOT EXISTS (SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             T_ATTEND_DAT_B W2 ");
        stb.append("                         WHERE ");
        stb.append("                             W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("                             AND W2.ATTENDDATE = W1.ATTENDDATE ");
        stb.append("                        ) ");
        stb.append("                    ) ");
        stb.append("            GROUP BY ");
        stb.append("                W1.SCHREGNO, ");
        stb.append("                W1.ATTENDDATE ");
        stb.append("        )T1 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T0.EXECUTEDATE = T1.ATTENDDATE ");
        stb.append("            AND T0.PERIOD_CNT != T1.PERIOD_CNT ");
        stb.append("        LEFT OUTER JOIN( ");
        stb.append("            SELECT ");
        stb.append("                SCHREGNO, ");
        stb.append("                ATTENDDATE, ");
        stb.append("                PERIODCD ");
        stb.append("            FROM ");
        stb.append("                T_ATTEND_DAT ");
        stb.append("            WHERE ");
        stb.append("                DI_CD IN ('4','5','6','11','12','13') ");
        stb.append("        )T2 ON T0.SCHREGNO = T2.SCHREGNO ");
        stb.append("            AND T0.EXECUTEDATE  = T2.ATTENDDATE ");
        stb.append("            AND T0.FIRST_PERIOD = T2.PERIODCD ");
        stb.append("        LEFT OUTER JOIN(");
        stb.append("            SELECT ");
        stb.append("                SCHREGNO, ");
        stb.append("                ATTENDDATE, ");
        stb.append("                PERIODCD ");
        stb.append("            FROM ");
        stb.append("                T_ATTEND_DAT ");
        stb.append("            WHERE ");
        stb.append("                DI_CD IN ('4','5','6') ");
        stb.append("        )T3 ON T0.SCHREGNO= T3.SCHREGNO ");
        stb.append("            AND T0.EXECUTEDATE = T3.ATTENDDATE ");
        stb.append("            AND T0.LAST_PERIOD = T3.PERIODCD ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ");
        stb.append(" ) ");


        //個人別遅刻・早退回数 (DI_CD追加(29〜32))
        stb.append(" , SCHRENGO_LATE_EARLY2 (SCHREGNO, SEMESTER, LATE, EARLY) AS ( ");
        stb.append("    SELECT ");
        stb.append("        T0.SCHREGNO ");
        stb.append("      , VALUE(T0.SEMESTER, '9') AS SEMESTER ");
        stb.append("      , COUNT(CASE WHEN T2.ATTENDDATE IS NOT NULL OR T4.ATTENDDATE IS NOT NULL THEN 1 END) AS LATE ");
        stb.append("      , COUNT(CASE WHEN T3.ATTENDDATE IS NOT NULL OR T4.ATTENDDATE IS NOT NULL THEN 1 END) AS EARLY ");
        stb.append("    FROM ");
        stb.append("        T_PERIOD_CNT T0 ");
        stb.append("        INNER JOIN(");
        stb.append("           SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append("           FROM    T_ATTEND_DAT W1 ");
        stb.append("           WHERE   W1.DI_CD IN ('4','5','6','11','12','13','29','30','31','32') ");
        stb.append("                AND (W1.SYUKESSEKI_HANTEI_HOU = '1' OR W1.SYUKESSEKI_HANTEI_HOU <> '1' AND NOT EXISTS (SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             T_ATTEND_DAT_B W2 ");
        stb.append("                         WHERE ");
        stb.append("                             W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("                             AND W2.ATTENDDATE = W1.ATTENDDATE ");
        stb.append("                        ) ");
        stb.append("                    ) ");
        stb.append("           GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
        stb.append("        )T1 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND T0.EXECUTEDATE = T1.ATTENDDATE ");
        stb.append("           AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
        stb.append("        LEFT JOIN ( ");
        stb.append("           SELECT  SCHREGNO ,ATTENDDATE ");
        stb.append("           FROM    T_ATTEND_DAT ");
        stb.append("           WHERE   DI_CD IN ('29') ");
        stb.append("           GROUP BY SCHREGNO ,ATTENDDATE ");
        stb.append("         )T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE ");
        stb.append("        LEFT JOIN ( ");
        stb.append("           SELECT  SCHREGNO ,ATTENDDATE ");
        stb.append("           FROM    T_ATTEND_DAT ");
        stb.append("           WHERE   DI_CD IN ('30') ");
        stb.append("           GROUP BY SCHREGNO ,ATTENDDATE ");
        stb.append("         )T3 ON T0.SCHREGNO = T3.SCHREGNO AND T0.EXECUTEDATE  = T3.ATTENDDATE ");
        stb.append("        LEFT JOIN ( ");
        stb.append("           SELECT  SCHREGNO ,ATTENDDATE ");
        stb.append("           FROM    T_ATTEND_DAT ");
        stb.append("           WHERE   DI_CD IN ('31','32') ");
        stb.append("           GROUP BY SCHREGNO ,ATTENDDATE ");
        stb.append("         )T4 ON T0.SCHREGNO = T4.SCHREGNO AND T0.EXECUTEDATE  = T4.ATTENDDATE ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ");
        stb.append(" ) ");

        //個人別遅刻回数
        stb.append(" , SCHRENGO_LATE (SCHREGNO, SEMESTER, LATE) AS ( ");
        stb.append("    SELECT ");
        stb.append("        T0.SCHREGNO, ");
        stb.append("        VALUE(T0.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        COUNT(T2.ATTENDDATE) AS LATE ");
        stb.append("    FROM ");
        stb.append("        T_PERIOD_CNT T0 ");
        stb.append("        INNER JOIN( ");
        stb.append("            SELECT ");
        stb.append("                SCHREGNO, ");
        stb.append("                ATTENDDATE, ");
        stb.append("                PERIODCD ");
        stb.append("            FROM ");
        stb.append("                T_ATTEND_DAT W1 ");
        stb.append("            WHERE ");
        stb.append("                DI_CD IN ('15','23','24') ");
        stb.append("                AND (W1.SYUKESSEKI_HANTEI_HOU = '1' OR W1.SYUKESSEKI_HANTEI_HOU <> '1' AND NOT EXISTS (SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             T_ATTEND_DAT_B W2 ");
        stb.append("                         WHERE ");
        stb.append("                             W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("                             AND W2.ATTENDDATE = W1.ATTENDDATE ");
        stb.append("                        ) ");
        stb.append("                    ) ");
        stb.append("        )T2 ON T0.SCHREGNO = T2.SCHREGNO ");
        stb.append("            AND T0.EXECUTEDATE = T2.ATTENDDATE ");
        stb.append("            AND T0.FIRST_PERIOD = T2.PERIODCD ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ");
        stb.append(" ) ");

        //個人別早退回数
        stb.append(" , SCHRENGO_EARLY(SCHREGNO, SEMESTER, EARLY) AS ( ");
        stb.append("    SELECT ");
        stb.append("        T0.SCHREGNO, ");
        stb.append("        VALUE(T0.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        COUNT(T2.ATTENDDATE) AS EARLY ");
        stb.append("    FROM ");
        stb.append("        T_PERIOD_CNT T0 ");
        stb.append("        INNER JOIN ( ");
        stb.append("            SELECT ");
        stb.append("                SCHREGNO, ");
        stb.append("                ATTENDDATE, ");
        stb.append("                PERIODCD ");
        stb.append("            FROM ");
        stb.append("                T_ATTEND_DAT W1 ");
        stb.append("            WHERE ");
        stb.append("                DI_CD IN ('16') ");
        stb.append("                AND (W1.SYUKESSEKI_HANTEI_HOU = '1' OR W1.SYUKESSEKI_HANTEI_HOU <> '1' AND NOT EXISTS (SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             T_ATTEND_DAT_B W2 ");
        stb.append("                         WHERE ");
        stb.append("                             W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("                             AND W2.ATTENDDATE = W1.ATTENDDATE ");
        stb.append("                        ) ");
        stb.append("                    ) ");
        stb.append("        )T2 ON T0.SCHREGNO = T2.SCHREGNO ");
        stb.append("            AND T0.EXECUTEDATE = T2.ATTENDDATE ");
        stb.append("            AND T0.LAST_PERIOD = T2.PERIODCD ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ");
        stb.append(" ) ");

        //授業時数
        stb.append(" , SCHRENGO_LESSON(SCHREGNO, SEMESTER, LESSON) AS ( ");
        stb.append("    SELECT ");
        stb.append("        T0.SCHREGNO, ");
        stb.append("        VALUE(T0.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        SUM(CASE WHEN VALUE(T0.PERIOD_CNT, 0) > 0 ");
        stb.append("                 THEN 1 ");
        stb.append("                 ELSE 0 ");
        stb.append("            END ");
        stb.append("        ) AS LESSON ");
        stb.append("    FROM ");
        stb.append("        T_PERIOD_CNT T0 ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ");
        stb.append(" ) ");

        // 留学、休学
        stb.append(" , SCHRENGO_TRANSFER_DATE (SCHREGNO, TRANSFERCD, SEMESTER, TRANSFER_DATE) AS ( ");
        stb.append("    SELECT ");
        stb.append("       T1.SCHREGNO,");
        stb.append("       T2.TRANSFERCD,");
        stb.append("       VALUE(T1.SEMESTER, '9') AS SEMESTER, ");
        stb.append("       COUNT(DISTINCT T1.EXECUTEDATE) AS TRANSFER_DATE ");
        stb.append("    FROM ");
        stb.append("       SCHEDULE_SCHREG_R T1 ");
        stb.append("       INNER JOIN SCHREG_TRANSFER_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND T2.TRANSFERCD IN('1', '2') ");
        stb.append("           AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((T1.SCHREGNO, T2.TRANSFERCD, T1.SEMESTER), (T1.SCHREGNO, T2.TRANSFERCD)) ");
        stb.append(" ) ");

        //個人別授業欠課時数
        stb.append(" , SCHRENGO_ATTEND_JISU (SCHREGNO, SEMESTER, REIHAI_KEKKA, KEKKA_JISU, M_KEKKA_JISU, REIHAI_TIKOKU, JYUGYOU_TIKOKU, JYUGYOU_SOUTAI) AS ( ");
        stb.append("    SELECT ");
        stb.append("        W0.SCHREGNO, ");
        stb.append("        VALUE(W0.SEMESTER, '9') AS SEMESTER, ");
        stb.append("        SUM(VALUE(W0.REIHAI_KEKKA, 0)) AS REIHAI_KEKKA, ");
        stb.append("        SUM(VALUE(W0.KEKKA_JISU, 0)) AS KEKKA_JISU, ");
        stb.append("        0 AS M_KEKKA_JISU, ");
        stb.append("        SUM(VALUE(W0.REIHAI_TIKOKU, 0)) AS REIHAI_TIKOKU, ");
        stb.append("        SUM(VALUE(W0.JYUGYOU_TIKOKU, 0)) AS JYUGYOU_TIKOKU, ");
        stb.append("        SUM(VALUE(W0.JYUGYOU_SOUTAI, 0)) AS JYUGYOU_SOUTAI ");
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT_JISU W0 ");
        stb.append("    GROUP BY ");
        stb.append("        GROUPING SETS ((W0.SCHREGNO, W0.SEMESTER), (W0.SCHREGNO)) ");
        stb.append(" ) ");

        //月別集計データから集計した表
        stb.append(" , SCHRENGO_ATTEND_SEMES (SCHREGNO, SEMESTER, GRADE, HR_CLASS, SEM_OFFDAYS, LESSON, MOURNING, SUSPEND, ABSENT, SICK, NOTICE, NONOTICE, REIHAI_KEKKA, KEKKA_JISU, M_KEKKA_JISU, REIHAI_TIKOKU, JYUGYOU_TIKOKU, JYUGYOU_SOUTAI, LATE, EARLY, VIRUS, KOUDOME, ABROAD, OFFDAYS, TOCHU_KEKKA) AS ( ");
        stb.append("    SELECT ");
        stb.append("        W1.SCHREGNO, ");
        stb.append("        W1.SEMESTER, ");
        stb.append("        W2.GRADE, ");
        stb.append("        W2.HR_CLASS, ");
        stb.append("        MAX(W2.SEM_OFFDAYS) AS SEM_OFFDAYS, ");
        stb.append("        SUM(VALUE(LESSON,0)) AS LESSON, ");
        stb.append("        SUM(VALUE(MOURNING,0)) AS MOURNING, ");
        stb.append("        SUM(VALUE(SUSPEND,0)) AS SUSPEND, ");
        stb.append("        SUM(VALUE(ABSENT,0)) AS ABSENT, ");
        stb.append("        SUM(VALUE(SICK,0)) AS SICK, ");
        stb.append("        SUM(VALUE(NOTICE,0)) AS NOTICE, ");
        stb.append("        SUM(VALUE(NONOTICE,0)) AS NONOTICE, ");
        stb.append("        SUM(VALUE(REIHAI_KEKKA,0)) AS REIHAI_KEKKA, ");
        if (hasVAttendSemesDatKekkaJisu) {
            stb.append("        SUM(VALUE(KEKKA_JISU,0)) AS KEKKA_JISU, ");
        } else {
            stb.append("        0 AS KEKKA_JISU, ");
        }
        stb.append("        SUM(VALUE(M_KEKKA_JISU,0)) AS M_KEKKA_JISU, ");
        stb.append("        SUM(VALUE(REIHAI_TIKOKU,0)) AS REIHAI_TIKOKU, ");
        stb.append("        SUM(VALUE(JYUGYOU_TIKOKU,0)) AS JYUGYOU_TIKOKU, ");
        stb.append("        SUM(VALUE(JYUGYOU_SOUTAI,0)) AS JYUGYOU_SOUTAI, ");
        stb.append("        SUM(VALUE(LATE, 0)) AS LATE, ");
        stb.append("        SUM(VALUE(EARLY, 0)) AS EARLY, ");
        if ("true".equals(useVirus)) {
            stb.append("        SUM(VALUE(VIRUS, 0)) AS VIRUS, ");
        } else {
            stb.append("        0 AS VIRUS, ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("        SUM(VALUE(KOUDOME, 0)) AS KOUDOME, ");
        } else {
            stb.append("        0 AS KOUDOME, ");
        }
        stb.append("        SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
        stb.append("        SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
        if (hasVAttendSemesDatTochuKekka) {
            stb.append("        SUM(VALUE(TOCHU_KEKKA, 0)) AS TOCHU_KEKKA ");
        } else {
            stb.append("        0 AS TOCHU_KEKKA ");
        }
        stb.append("    FROM ");
        stb.append("        V_ATTEND_SEMES_DAT W1 ");
        stb.append("        INNER JOIN (SELECT DISTINCT SCHREGNO, SEMESTER, GRADE, HR_CLASS, SEM_OFFDAYS FROM SCHNO) W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SEMESTER = W1.SEMESTER ");
        stb.append("    WHERE ");
        stb.append("        W1.YEAR = '" + year + "' ");
        stb.append("        AND W1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
        if (gradeSemeMonth.length() != 0) {
            stb.append("        AND " + gradeSemeMonth + " ");
        } else {
            stb.append("        AND W1.SEMESTER || W1.MONTH IN " + semesInState + " ");
        }
        stb.append("    GROUP BY ");
        stb.append("        W1.SCHREGNO, W1.SEMESTER, W2.GRADE, W2.HR_CLASS ");
        stb.append(" ) ");

        stb.append(" , MAIN0 (SOURCE_DIV, SCHREGNO, SEMESTER, GRADE, HR_CLASS, LESSON, SUSPEND, VIRUS, KOUDOME, MOURNING, LESSON_FOR_CALC, SICK_SUM, SICK, NOTICE, NONOTICE, REIHAI_KEKKA, KEKKA_JISU, M_KEKKA_JISU, REIHAI_TIKOKU, JYUGYOU_TIKOKU, JYUGYOU_SOUTAI, ABSENT, LATE, EARLY, ABROAD, OFFDAYS, TOCHU_KEKKA) AS ( ");
        stb.append(" SELECT ");
        stb.append("    'HASU' AS SOURCE_DIV, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER , ");
        stb.append("    T1.GRADE , ");
        stb.append("    T1.HR_CLASS , ");
        stb.append("    VALUE(HASU_LESSON.LESSON,0) + VALUE(HASU_ABROAD.TRANSFER_DATE,0) + VALUE(CASE WHEN T1.SEM_OFFDAYS = '1' THEN HASU_OFFDAYS.TRANSFER_DATE END, 0) AS LESSON, ");
        stb.append("    VALUE(TT3.SUSPEND,0) AS SUSPEND, ");
        stb.append("    VALUE(TT3_2.VIRUS,0) AS VIRUS, ");
        stb.append("    VALUE(TT3_3.KOUDOME,0) AS KOUDOME, ");
        stb.append("    VALUE(TT4.MOURNING,0) AS MOURNING, ");
        stb.append("    VALUE(HASU_LESSON.LESSON,0) + VALUE(HASU_ABROAD.TRANSFER_DATE,0) + VALUE(CASE WHEN T1.SEM_OFFDAYS = '1' THEN HASU_OFFDAYS.TRANSFER_DATE END, 0) AS LESSON_FOR_CALC, ");
        stb.append("    VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(CASE WHEN T1.SEM_OFFDAYS = '1' THEN HASU_OFFDAYS.TRANSFER_DATE END, 0) AS SICK_SUM, ");
        stb.append("    VALUE(TT5.SICK,0) AS SICK, ");
        stb.append("    VALUE(TT5.NOTICE,0) AS NOTICE, ");
        stb.append("    VALUE(TT5.NONOTICE,0) AS NONOTICE, ");
        stb.append("    VALUE(TT15.REIHAI_KEKKA,0) AS REIHAI_KEKKA, ");
        stb.append("    VALUE(TT15.KEKKA_JISU,0) AS KEKKA_JISU, ");
        stb.append("    VALUE(TT15.M_KEKKA_JISU,0) AS M_KEKKA_JISU, ");
        stb.append("    VALUE(TT15.REIHAI_TIKOKU,0) AS REIHAI_TIKOKU, ");
        stb.append("    VALUE(TT15.JYUGYOU_TIKOKU,0) AS JYUGYOU_TIKOKU, ");
        stb.append("    VALUE(TT15.JYUGYOU_SOUTAI,0) AS JYUGYOU_SOUTAI, ");
        if (kouketsuhaD110toOnaji) {
            stb.append("    VALUE(TT5_0.ABSENT,0) AS ABSENT, ");
        } else {
            stb.append("    VALUE(TT5.ABSENT,0) AS ABSENT, ");
        }
        stb.append("    VALUE(TT6.LATE,0) + VALUE(TT6_2.LATE,0) + VALUE(TT10.LATE,0) AS LATE, ");
        stb.append("    VALUE(TT6.EARLY,0) + VALUE(TT6_2.EARLY,0) + VALUE(TT11.EARLY,0) AS EARLY, ");
        stb.append("    VALUE(HASU_ABROAD.TRANSFER_DATE,0) AS ABROAD, ");
        stb.append("    VALUE(HASU_OFFDAYS.TRANSFER_DATE,0) AS OFFDAYS, ");
        stb.append("    0 AS TOCHU_KEKKA ");
        stb.append(" FROM ");
        stb.append("    SCHNO T1 ");
        stb.append(" LEFT JOIN SCHRENGO_SUSPEND TT3 ON T1.SCHREGNO = TT3.SCHREGNO AND T1.SEMESTER = TT3.SEMESTER "); //個人別出停日数
        stb.append(" LEFT JOIN SCHRENGO_VIRUS TT3_2 ON T1.SCHREGNO = TT3_2.SCHREGNO AND T1.SEMESTER = TT3_2.SEMESTER "); //個人別出停伝染病日数
        stb.append(" LEFT JOIN SCHRENGO_KOUDOME TT3_3 ON T1.SCHREGNO = TT3_3.SCHREGNO AND T1.SEMESTER = TT3_3.SEMESTER "); //個人別出停公止日数
        stb.append(" LEFT JOIN SCHRENGO_MOURNING TT4 ON T1.SCHREGNO = TT4.SCHREGNO AND T1.SEMESTER = TT4.SEMESTER "); //個人別忌引日数
        if (kouketsuhaD110toOnaji) {
            stb.append(" LEFT JOIN SCHRENGO_KOUKETSU TT5_0 ON T1.SCHREGNO = TT5_0.SCHREGNO AND T1.SEMESTER = TT5_0.SEMESTER "); //個人別公欠日数
        }
        stb.append(" LEFT JOIN SCHRENGO_ABSENT TT5 ON T1.SCHREGNO = TT5.SCHREGNO AND T1.SEMESTER = TT5.SEMESTER "); //個人別欠席日数
        stb.append(" LEFT JOIN SCHRENGO_LATE_EARLY TT6 ON T1.SCHREGNO = TT6.SCHREGNO AND T1.SEMESTER = TT6.SEMESTER "); //個人別遅刻・早退回数
        stb.append(" LEFT JOIN SCHRENGO_LATE_EARLY2 TT6_2 ON T1.SCHREGNO = TT6_2.SCHREGNO AND T1.SEMESTER = TT6_2.SEMESTER "); //個人別遅刻・早退回数 (DI_CD追加(29〜32))
        stb.append(" LEFT JOIN SCHRENGO_LATE TT10 ON T1.SCHREGNO = TT10.SCHREGNO AND T1.SEMESTER = TT10.SEMESTER "); //個人別遅刻回数
        stb.append(" LEFT JOIN SCHRENGO_EARLY TT11 ON T1.SCHREGNO = TT11.SCHREGNO AND T1.SEMESTER = TT11.SEMESTER "); //個人別早退回数
        stb.append(" LEFT JOIN SCHRENGO_LESSON HASU_LESSON ON T1.SCHREGNO = HASU_LESSON.SCHREGNO AND T1.SEMESTER = HASU_LESSON.SEMESTER "); //授業時数
        stb.append(" LEFT JOIN SCHRENGO_TRANSFER_DATE HASU_ABROAD ON T1.SCHREGNO = HASU_ABROAD.SCHREGNO AND HASU_ABROAD.TRANSFERCD = '1' AND T1.SEMESTER = HASU_ABROAD.SEMESTER "); // 留学中の授業日数
        stb.append(" LEFT JOIN SCHRENGO_TRANSFER_DATE HASU_OFFDAYS ON T1.SCHREGNO = HASU_OFFDAYS.SCHREGNO AND HASU_OFFDAYS.TRANSFERCD = '2' AND T1.SEMESTER = HASU_OFFDAYS.SEMESTER "); // 休学中の授業日数
        stb.append(" LEFT JOIN SCHRENGO_ATTEND_JISU TT15 ON T1.SCHREGNO = TT15.SCHREGNO  AND T1.SEMESTER = TT15.SEMESTER "); //個人別授業欠課時数
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("    'SEMES' AS SOURCE_DIV, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER , ");
        stb.append("    T1.GRADE , ");
        stb.append("    T1.HR_CLASS , ");
        stb.append("    VALUE(T1.LESSON,0) - VALUE(T1.OFFDAYS,0)                     + VALUE(CASE WHEN T1.SEM_OFFDAYS = '1' THEN T1.OFFDAYS END, 0) AS LESSON, ");
        stb.append("    VALUE(T1.SUSPEND,0) AS SUSPEND, ");
        stb.append("    VALUE(T1.VIRUS,0) AS VIRUS, ");
        stb.append("    VALUE(T1.KOUDOME,0) AS KOUDOME, ");
        stb.append("    VALUE(T1.MOURNING,0) AS MOURNING, ");
        stb.append("    VALUE(T1.LESSON,0) - VALUE(T1.OFFDAYS,0)                     + VALUE(CASE WHEN T1.SEM_OFFDAYS = '1' THEN T1.OFFDAYS END, 0) - VALUE(T1.ABROAD,0) AS LESSON_FOR_CALC, ");
        stb.append("    VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(CASE WHEN T1.SEM_OFFDAYS = '1' THEN T1.OFFDAYS END, 0) AS SICK_SUM, ");
        stb.append("    VALUE(T1.SICK,0) AS SICK, ");
        stb.append("    VALUE(T1.NOTICE,0) AS NOTICE, ");
        stb.append("    VALUE(T1.NONOTICE,0) AS NONOTICE, ");
        stb.append("    VALUE(T1.REIHAI_KEKKA,0) AS REIHAI_KEKKA, ");
        stb.append("    VALUE(T1.KEKKA_JISU,0) AS KEKKA_JISU, ");
        stb.append("    VALUE(T1.M_KEKKA_JISU,0) AS M_KEKKA_JISU, ");
        stb.append("    VALUE(T1.REIHAI_TIKOKU,0) AS REIHAI_TIKOKU, ");
        stb.append("    VALUE(T1.JYUGYOU_TIKOKU,0) AS JYUGYOU_TIKOKU, ");
        stb.append("    VALUE(T1.JYUGYOU_SOUTAI,0) AS JYUGYOU_SOUTAI, ");
        stb.append("    VALUE(T1.ABSENT,0) AS ABSENT, ");
        stb.append("    VALUE(T1.LATE,0) AS LATE, ");
        stb.append("    VALUE(T1.EARLY,0) AS EARLY, ");
        stb.append("    VALUE(T1.ABROAD,0) AS ABROAD, ");
        stb.append("    VALUE(T1.OFFDAYS,0) AS OFFDAYS, ");
        stb.append("    VALUE(T1.TOCHU_KEKKA,0) AS TOCHU_KEKKA ");
        stb.append(" FROM ");
        stb.append("    SCHRENGO_ATTEND_SEMES T1 "); //月別集計データから集計した表
        stb.append(" ) ");

        //メイン表
        stb.append(" SELECT ");
        if (groupByDiv == "HR_CLASS") {
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
        } else if (groupByDiv == "GRADE") {
            stb.append("    T1.GRADE, ");
        } else if (groupByDiv == "SCHREGNO") {
            stb.append("    T1.SCHREGNO, ");
        } else if (groupByDiv == "SEMESTER") {
            stb.append("    T1.SCHREGNO, ");
            stb.append("    VALUE(T1.SEMESTER, '9') AS SEMESTER, ");
        }
        stb.append("    SUM(T1.LESSON) AS LESSON, ");
        stb.append("    SUM(T1.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(T1.VIRUS) AS VIRUS, ");
        stb.append("    SUM(T1.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(T1.MOURNING) AS MOURNING, ");
        stb.append("    SUM(T1.LESSON_FOR_CALC - T1.SUSPEND - T1.VIRUS - T1.KOUDOME - T1.MOURNING) AS MLESSON, ");
        stb.append("    SUM(T1.SICK_SUM) AS SICK, ");
        stb.append("    SUM(T1.SICK) AS SICK_ONLY, ");
        stb.append("    SUM(T1.NOTICE) AS NOTICE_ONLY, ");
        stb.append("    SUM(T1.NONOTICE) AS NONOTICE_ONLY, ");
        stb.append("    SUM(T1.REIHAI_KEKKA) AS REIHAI_KEKKA, ");
        stb.append("    SUM(T1.KEKKA_JISU) AS KEKKA_JISU, ");
        stb.append("    SUM(T1.M_KEKKA_JISU) AS M_KEKKA_JISU, ");
        stb.append("    SUM(T1.REIHAI_TIKOKU) AS REIHAI_TIKOKU, ");
        stb.append("    SUM(T1.JYUGYOU_TIKOKU) AS JYUGYOU_TIKOKU, ");
        stb.append("    SUM(T1.JYUGYOU_SOUTAI) AS JYUGYOU_SOUTAI, ");
        stb.append("    SUM(T1.ABSENT) AS ABSENT, ");
        stb.append("    SUM(T1.LESSON_FOR_CALC - T1.SUSPEND - T1.VIRUS - T1.KOUDOME - T1.MOURNING - T1.SICK_SUM) AS PRESENT, ");
        stb.append("    SUM(T1.LATE) AS LATE, ");
        stb.append("    SUM(T1.EARLY) AS EARLY, ");
        stb.append("    SUM(T1.ABROAD) AS TRANSFER_DATE, ");
        stb.append("    SUM(T1.OFFDAYS) AS OFFDAYS, ");
        stb.append("    SUM(T1.TOCHU_KEKKA) AS TOCHU_KEKKA ");
        stb.append(" FROM ");
        stb.append("    MAIN0 T1 ");
        stb.append(" GROUP BY ");
        if (groupByDiv == "HR_CLASS") {
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS ");
        } else if (groupByDiv == "GRADE") {
            stb.append("    T1.GRADE ");
        } else if (groupByDiv == "SCHREGNO") {
            stb.append("    T1.SCHREGNO ");
        } else if (groupByDiv == "SEMESTER") {
            stb.append("    GROUPING SETS ( ");
            stb.append("        (T1.SCHREGNO, T1.SEMESTER), ");
            stb.append("        (T1.SCHREGNO)) ");
        }
        stb.append(" ORDER BY ");
        if (groupByDiv == "HR_CLASS") {
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS ");
        } else if (groupByDiv == "GRADE") {
            stb.append("    T1.GRADE ");
        } else if (groupByDiv == "SCHREGNO") {
            stb.append("    T1.SCHREGNO ");
        } else if (groupByDiv == "SEMESTER") {
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER ");
        }
        logInfo(paramMap, " getAttendSemesSql = " + stb.toString(), !"1".equals(paramMap.get(KEY_noOutputSql)));
        return stb.toString();
    }


    public static String getAttendDayDatSql(
            final String year,
            final String sdate,
            final String edate,
            final String semester,
            final Map paramMap
            ) {

        final String grade = (String) paramMap.get(KEY_grade);
        final String hrClass = (String) paramMap.get("hrClass");
        final String schregno = (String) paramMap.get("schregno");

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNOS AS (SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     , BASE.NAME ");
        stb.append("     , T1.ATTENDNO ");
        stb.append("     , REGD.GRADE ");
        stb.append("     , REGD.HR_CLASS ");
        stb.append("     , T2.ENT_DIV ");
        stb.append("     , T2.GRD_DIV ");
        stb.append("     , T4.SCHOOL_KIND ");
        stb.append("     , T4.GRADE_NAME1 ");
        stb.append("     , NMA002.NAME1 AS ENT_DIV_NAME ");
        stb.append("     , NMA003.NAME1 AS GRD_DIV_NAME ");
        stb.append("     , VALUE(T2.ENT_DATE, '1900-01-01') AS ENT_DATE ");
        stb.append("     , VALUE(T2.GRD_DATE, '9999-12-31') AS GRD_DATE ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = REGD.YEAR AND T4.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = REGD.SCHREGNO AND T2.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = T2.ENT_DIV ");
        stb.append("     LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = T2.GRD_DIV ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        if (null != schregno) {
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        }
        if (null != grade) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
        }
        if (null != hrClass) {
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        }
        stb.append(" ) ");

        stb.append(" , DI_CDS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '1',  '8') THEN 1 END) AS ABSENT ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '2',  '9') THEN 1 END) AS SUSPEND ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ('19', '20') THEN 1 END) AS VIRUS ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ('25', '26') THEN 1 END) AS KOUDOME ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '3', '10') THEN 1 END) AS MOURNING ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ('15'      ) THEN 1 END) AS LATE ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ('16'      ) THEN 1 END) AS EARLY ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '4', '11') THEN 1 END) AS SICK_ONLY ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '5', '12') THEN 1 END) AS NOTICE_ONLY ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '6', '13') THEN 1 END) AS NONOTICE_ONLY ");
        stb.append("   , SUM(CASE WHEN T2.DI_CD IN ( '4', '5', '6', '11', '12', '13') THEN 1 END) AS SICK ");
        stb.append(" FROM ");
        stb.append("     SCHREGNOS T1 ");
        stb.append("     LEFT JOIN ATTEND_DAY_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = '" + year + "' ");
        stb.append("         AND T2.ATTENDDATE BETWEEN '" + sdate + "' AND '" + edate + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("   , T1.NAME ");
        stb.append("   , T1.GRADE ");
        stb.append("   , T1.HR_CLASS ");
        stb.append("   , T1.ATTENDNO ");
        stb.append("   , T1.SCHOOL_KIND ");
        stb.append("   , T1.ENT_DIV ");
        stb.append("   , T1.GRD_DIV ");
        stb.append("   , T1.ENT_DATE ");
        stb.append("   , T1.GRD_DATE ");
        stb.append("   , T1.GRADE_NAME1 ");
        stb.append("   , T2.ABSENT ");
        stb.append("   , T2.SUSPEND ");
        stb.append("   , T2.VIRUS ");
        stb.append("   , T2.KOUDOME ");
        stb.append("   , T2.MOURNING ");
        stb.append("   , T2.LATE ");
        stb.append("   , T2.EARLY ");
        stb.append("   , T2.SICK_ONLY ");
        stb.append("   , T2.NOTICE_ONLY ");
        stb.append("   , T2.NONOTICE_ONLY ");
        stb.append("   , T2.SICK ");
        stb.append(" FROM ");
        stb.append("     SCHREGNOS T1 ");
        stb.append(" LEFT JOIN DI_CDS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        logInfo(paramMap, " getAttendDayDatSql = " + stb.toString(), !"1".equals(paramMap.get(KEY_noOutputSql)));
        return stb.toString();
    }

    private static void appendSchregScheduleCommon(
            final int flg,
            final StringBuffer stb,
            final Map paramMap,
            final Map hasuuMap,
            final String year,
            final String eSemester) {

        final String periodInState = getString(paramMap, KEY_periodInState);
        final KNJDefineSchool defineSchool = (KNJDefineSchool) paramMap.get(KEY_knjDefineSchool);
        final String printSubclassLastChairStd = (String) paramMap.get(KEY_printSubclassLastChairStd);

        final String sSemester = (String) paramMap.get(KEY_sSemester);

        final String grade = getString(paramMap, KEY_grade);
        final String hrClass = getString(paramMap, "hrClass");
        final String schregno = getString(paramMap, "schregno");

        final boolean hasSchChrDatExecuteDiv = ((Boolean) paramMap.get("hasTableColumn:SCH_CHR_DAT.EXECUTEDIV")).booleanValue();
        final boolean hasChairDetailDat = ((Boolean) paramMap.get("hasTableColumn:CHAIR_DETAIL_DAT")).booleanValue();
        final StringBuffer gradeSemeScheduleDates = getGradeSemeHasuuDates(paramMap, hasuuMap);

        //対象生徒
        stb.append("WITH SCHNO0 AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("    W1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT W1 ");
        stb.append(" WHERE ");
        stb.append("    W1.YEAR = '" + year + "' ");
        stb.append("    AND W1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
        if (schregno != null) {
            if ("?".equals(schregno)) {
                stb.append("    AND W1.SCHREGNO = ? ");
            } else {
                stb.append("    AND W1.SCHREGNO = '" + schregno + "' ");
            }
        }
        if (grade != null) {
            if ("?".equals(grade)) {
                stb.append("    AND W1.GRADE = ? ");
            } else {
                stb.append("    AND W1.GRADE = '" + grade + "' ");
            }
        }
        if (hrClass != null) {
            if ("?".equals(hrClass)) {
                stb.append("    AND W1.HR_CLASS = ? ");
            } else {
                stb.append("    AND W1.HR_CLASS = '" + hrClass + "' ");
            }
        }
        stb.append(" ), SCHNO AS( ");
        stb.append(" SELECT ");
        stb.append("    W1.SCHREGNO ");
        stb.append("  , W1.SEMESTER ");
        stb.append("  , W1.GRADE ");
        stb.append("  , W1.HR_CLASS ");
        stb.append("  , W1.COURSECD ");
        stb.append("  , W1.MAJORCD ");
        stb.append("  , W1.COURSECODE ");
        stb.append("  , ENTGRD.ENT_DIV ");
        stb.append("  , ENTGRD.ENT_DATE ");
        stb.append("  , ENTGRD.GRD_DIV ");
        stb.append("  , ENTGRD.GRD_DATE ");
        stb.append("  , VALUE(SCM.SUB_OFFDAYS, '0') AS SUB_OFFDAYS ");
        stb.append("  , VALUE(SCM.SUB_ABSENT, '0') AS SUB_ABSENT ");
        stb.append("  , VALUE(SCM.SUB_SUSPEND, '0') AS SUB_SUSPEND ");
        stb.append("  , VALUE(SCM.SUB_MOURNING, '0') AS SUB_MOURNING ");
        stb.append("  , VALUE(SCM.SUB_VIRUS, '0') AS SUB_VIRUS ");
        stb.append("  , VALUE(SCM.SEM_OFFDAYS, '0') AS SEM_OFFDAYS ");
        stb.append("  , VALUE(SCM.SUB_KOUDOME, '0') AS SUB_KOUDOME ");
        if (isKindai(getString(paramMap, KEY_z010Name1))) {
            stb.append("  , '1' AS ABSENT_COV ");
            stb.append("  , '3' AS ABSENT_COV_LATE ");
        } else {
            stb.append("  , SCM.ABSENT_COV ");
            stb.append("  , VALUE(SCM.ABSENT_COV_LATE, 0) AS ABSENT_COV_LATE ");
        }
        stb.append("  , SCM.AMARI_KURIAGE ");
        stb.append("  , VALUE(SCM.SYUKESSEKI_HANTEI_HOU, '0') AS SYUKESSEKI_HANTEI_HOU ");
        stb.append("  , VALUE(SCM.JUGYOU_JISU_FLG, '1') AS JUGYOU_JISU_FLG ");
        stb.append("  , INT(VALUE(SCM.JITU_JIFUN, '50')) AS JITU_JIFUN ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT W1 ");
        stb.append("    INNER JOIN SCHNO0 W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
        stb.append("    LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = W1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        if ("1".equals(paramMap.get(KEY_use_school_detail_gcm_dat))) {
            stb.append("    LEFT JOIN V_SCHOOL_GCM_MST SCM ON SCM.YEAR = W1.YEAR ");
            stb.append("        AND SCM.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("        AND SCM.GRADE = '00' ");
            stb.append("        AND SCM.COURSECD = W1.COURSECD ");
            stb.append("        AND SCM.MAJORCD = W1.MAJORCD ");
        } else {
            stb.append("    LEFT JOIN V_SCHOOL_MST SCM ON SCM.YEAR = W1.YEAR ");
            if (((Boolean) paramMap.get("hasTableColumn:SCHOOL_MST.SCHOOL_KIND")).booleanValue()) {
                stb.append("    AND SCM.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            }
        }
        stb.append(" WHERE ");
        stb.append("    W1.YEAR = '" + year + "' ");
        stb.append("    AND W1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
        stb.append(" ) ");

        if (gradeSemeScheduleDates.length() != 0) {
            stb.append(" , SCHEDULE_DATE_GRADE (GRADE, BEF_AFT_FLG, DAY_FROM, DAY_TO) AS( ");
            stb.append(gradeSemeScheduleDates);
        } else {
            final String befDayFrom = getString(hasuuMap, "befDayFrom");
            final String befDayTo = getString(hasuuMap, "befDayTo");
            final String aftDayFrom = getString(hasuuMap, "aftDayFrom");
            final String aftDayTo = getString(hasuuMap, "aftDayTo");

            stb.append(" , SCHEDULE_DATE (BEF_AFT_FLG, DAY_FROM, DAY_TO) AS( ");
            final StringBuffer bef = new StringBuffer();
            if ("?".equals(befDayFrom)) {
                bef.append(" VALUES('BEF', ?, ? )");
            } else if (null != befDayFrom){
                bef.append(" VALUES('BEF', '" + befDayFrom + "', '" + befDayTo + "') ");
            }
            final StringBuffer aft = new StringBuffer();
            if ("?".equals(aftDayFrom)) {
                aft.append(" VALUES('AFT', ?, ? )");
            } else if (null != aftDayFrom){
                aft.append(" VALUES('AFT', '" + aftDayFrom + "', '" + aftDayTo + "') ");
            }
            if (bef.length() > 0) {
                stb.append(bef);
            }
            if (bef.length() > 0 && aft.length() > 0) {
                stb.append(" UNION ALL ");
            }
            if (aft.length() > 0) {
                stb.append(aft);
            }
            if (bef.length() == 0 && aft.length() == 0) {
                stb.append(" VALUES(CAST(NULL AS VARCHAR(1)), CAST(NULL AS DATE), CAST(NULL AS DATE)) ");
            }
        }
        stb.append(" ) ");

        //対象生徒の時間割データ
        stb.append(" , SCHEDULE_SCHREG_R AS( ");
        stb.append(" SELECT ");
        stb.append("    T2.SCHREGNO ");
        stb.append("  , T1.SEMESTER ");
        stb.append("  , T1.EXECUTEDATE ");
        if (flg == FLG_ATTEND_SEMES) {
        } else {
            stb.append(" ,  T1.CHAIRCD ");
        }
        stb.append("  , T1.PERIODCD ");
        stb.append("  , T1.DATADIV ");
        stb.append("  , VALUE(INT(B001.NAMESPARE3), SCH1.JITU_JIFUN) AS PERIOD_MINUTES ");
        stb.append(" FROM ");
        stb.append("    SCH_CHR_DAT T1 ");
        stb.append("    INNER JOIN CHAIR_STD_DAT T2 ON T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append("        AND T1.CHAIRCD = T2.CHAIRCD ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(printSubclassLastChairStd)) {
            stb.append("    INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = T2.YEAR ");
            stb.append("        AND STDSEME.SEMESTER = T2.SEMESTER ");
            stb.append("        AND STDSEME.EDATE = T2.APPENDDATE ");
        }
        stb.append("    INNER JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE, MAX(JITU_JIFUN) AS JITU_JIFUN FROM SCHNO GROUP BY SCHREGNO) SCH1 ON SCH1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    LEFT JOIN SCHNO NOE1 ON NOE1.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND  NOE1.SEMESTER = T1.SEMESTER ");
        stb.append("                       AND    ((NOE1.ENT_DIV IN('1', '2', '3', '4', '5') AND T1.EXECUTEDATE < NOE1.ENT_DATE) ");
        stb.append("                            OR (NOE1.GRD_DIV IN('1', '2', '3')           AND T1.EXECUTEDATE > NOE1.GRD_DATE)) ");
        if (gradeSemeScheduleDates.length() != 0) {
            stb.append("    INNER JOIN (SELECT T2.SCHREGNO, MIN(T1.GRADE) AS GRADE "); // 指定学年のデータがなければGRADE_ALL
            stb.append("                FROM SCHEDULE_DATE_GRADE T1 ");
            stb.append("                INNER JOIN SCHNO T2 ON T2.GRADE = T1.GRADE OR '" + GRADE_ALL + "' = T1.GRADE ");
            stb.append("                GROUP BY T2.SCHREGNO ");
            stb.append("               ) DATE_GRADE ON DATE_GRADE.SCHREGNO = T2.SCHREGNO ");
            stb.append("    INNER JOIN SCHEDULE_DATE_GRADE T3 ON T1.EXECUTEDATE BETWEEN T3.DAY_FROM AND T3.DAY_TO ");
            stb.append("       AND T3.DAY_FROM IS NOT NULL ");
            stb.append("       AND T3.DAY_TO IS NOT NULL ");
            stb.append("       AND DATE_GRADE.GRADE = T3.GRADE ");
        } else {
            stb.append("    INNER JOIN SCHEDULE_DATE T3 ON T1.EXECUTEDATE BETWEEN T3.DAY_FROM AND T3.DAY_TO ");
            stb.append("       AND T3.DAY_FROM IS NOT NULL ");
            stb.append("       AND T3.DAY_TO IS NOT NULL ");
        }
        stb.append("    INNER JOIN NAME_MST B001 ON B001.NAMECD1 = 'B001' AND B001.NAMECD2 = T1.PERIODCD ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + year + "' ");
        stb.append("    AND T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
        if (flg == FLG_ATTEND_SEMES) {
            if (defineSchool != null && defineSchool.usefromtoperiod) {
                stb.append("    AND T1.PERIODCD IN " + periodInState + " ");
            }
        }
        stb.append("    AND NOE1.SCHREGNO IS NULL ");
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        if (flg == FLG_ATTEND_SEMES) {
            stb.append("    AND (T2.SCHREGNO, T1.EXECUTEDATE             ) NOT IN (SELECT T4.SCHREGNO, T4.ATTENDDATE ");
        } else {
            stb.append("    AND (T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD) NOT IN (SELECT T4.SCHREGNO, T4.ATTENDDATE, T4.PERIODCD ");
        }
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       L1.REP_DI_CD = '27' ");
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("        AND (T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD) NOT IN (SELECT T4.SCHREGNO, T4.ATTENDDATE, T4.PERIODCD ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        if (flg == FLG_ATTEND_SEMES) {
            if (hasChairDetailDat) {
                stb.append("    AND NOT EXISTS(SELECT 'X' FROM CHAIR_DETAIL_DAT L4 ");
                stb.append("                   WHERE L4.YEAR = T1.YEAR ");
                stb.append("                         AND L4.SEMESTER = T1.SEMESTER ");
                stb.append("                         AND L4.CHAIRCD = T1.CHAIRCD ");
                stb.append("                         AND L4.SEQ = '001' ");
                stb.append("                         AND L4.REMARK2 = '1' "); // 1日出欠の対象外
                stb.append("                  ) ");
            }
        }
        if (hasSchChrDatExecuteDiv) {
            stb.append("    AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append(" GROUP BY ");
        stb.append("    T2.SCHREGNO ");
        stb.append("  , T1.SEMESTER ");
        stb.append("  , T1.EXECUTEDATE ");
        if (flg == FLG_ATTEND_SEMES) {
        } else {
            stb.append("  , T1.CHAIRCD ");
        }
        stb.append("  , T1.PERIODCD ");
        stb.append("  , T1.DATADIV ");
        stb.append("  , B001.NAMESPARE3 ");
        stb.append("  , SCH1.JITU_JIFUN ");
        stb.append(" ), SCHEDULE_SCHREG AS( ");
        if (flg == FLG_ATTEND_SEMES) {
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD, ");
            stb.append("    T1.DATADIV ");
            stb.append(" FROM ");
            stb.append("    SCHEDULE_SCHREG_R T1 ");
            stb.append(" WHERE ");
            stb.append("    NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       SCHREG_TRANSFER_DAT T3 ");
            stb.append("                   WHERE ");
            stb.append("                       T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("                       AND TRANSFERCD IN('1','2') ");
            stb.append("                       AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
            stb.append("                  ) ");
            stb.append(" GROUP BY ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD, ");
            stb.append("    T1.DATADIV ");
        } else {
            final String useCurriculumcd = (String) paramMap.get(KEY_useCurriculumcd);

            if (flg == FLG_ATTEND_SUBCLASS_ABSENCE) {
                stb.append(" SELECT ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG_R T1 ");
                stb.append(" WHERE ");
                stb.append("    NOT EXISTS(SELECT ");
                stb.append("                       'X' ");
                stb.append("                   FROM ");
                stb.append("                       SCHREG_TRANSFER_DAT T3 ");
                stb.append("                   WHERE ");
                stb.append("                       T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("                       AND TRANSFERCD IN('1','2') ");
                stb.append("                       AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
                stb.append("                  ) ");
                stb.append(" GROUP BY ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    T1.DATADIV ");

            } else if (flg == FLG_ATTEND_SUBCLASS) {
                stb.append(" SELECT ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.CHAIRCD, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    T1.DATADIV, ");
                stb.append("    T1.PERIOD_MINUTES ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG_R T1 ");
                stb.append("    INNER JOIN (");
                stb.append("     SELECT ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SEMESTER, ");
                stb.append("        T1.EXECUTEDATE, ");
                stb.append("        MIN(T1.CHAIRCD) AS CHAIRCD, ");
                stb.append("        T1.PERIODCD ");
                stb.append("     FROM ");
                stb.append("        SCHEDULE_SCHREG_R T1 ");
                stb.append("     WHERE ");
                stb.append("        NOT EXISTS(SELECT ");
                stb.append("                           'X' ");
                stb.append("                       FROM ");
                stb.append("                           SCHREG_TRANSFER_DAT T3 ");
                stb.append("                       WHERE ");
                stb.append("                           T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("                           AND TRANSFERCD IN('1','2') ");
                stb.append("                           AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
                stb.append("                      ) ");
                stb.append("     GROUP BY ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SEMESTER, ");
                stb.append("        T1.EXECUTEDATE, ");
                stb.append("        T1.PERIODCD ");
                stb.append("    ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SEMESTER = T1.SEMESTER AND T1.EXECUTEDATE = T2.EXECUTEDATE ");
                stb.append("      AND T2.PERIODCD = T1.PERIODCD ");
            }

            // テスト項目マスタの集計フラグ
            stb.append(" ),TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            if ("TESTITEM_MST_COUNTFLG".equals(paramMap.get(KEY_useTestCountflg))) {
                stb.append("         TESTITEM_MST_COUNTFLG T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(paramMap.get(KEY_useTestCountflg))) {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
                stb.append("         AND T2.SCORE_DIV  = '01' ");
            } else {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            }
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");

            //端数計算有無の判定
            //対象生徒の出欠データ
            stb.append(" ), T_ATTEND_DAT AS ( ");
            if (flg == FLG_ATTEND_SUBCLASS_ABSENCE) {
                stb.append(" SELECT ");
                stb.append("    T0.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T2.CLASSCD, ");
                    stb.append("    T2.CURRICULUM_CD, ");
                    stb.append("    T2.SCHOOL_KIND, ");
                }
                stb.append("    T2.SUBCLASSCD, ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T0.ATTENDDATE, ");
                stb.append("    T0.PERIODCD, ");
                stb.append("    CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END AS REP_DI_CD, ");
                stb.append("    L1.MULTIPLY, ");
                stb.append("    T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("    ATTEND_DAT T0 ");
                stb.append("    INNER JOIN SCHEDULE_SCHREG T1 ON T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("        AND T0.ATTENDDATE = T1.EXECUTEDATE ");
                stb.append("        AND T0.PERIODCD = T1.PERIODCD ");
                stb.append("    INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T0.YEAR ");
                stb.append("        AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("        AND T2.CHAIRCD = T0.CHAIRCD ");
                stb.append("    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD ");
                stb.append(" WHERE ");
                stb.append("    T0.YEAR = '" + year + "' ");

            } else if (flg == FLG_ATTEND_SUBCLASS) {
                stb.append(" SELECT ");
                stb.append("    T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T2.CLASSCD, ");
                    stb.append("    T2.CURRICULUM_CD, ");
                    stb.append("    T2.SCHOOL_KIND, ");
                }
                stb.append("    T2.SUBCLASSCD, ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    'DI_ALL' AS REP_DI_CD, ");
                stb.append("    CAST(NULL AS VARCHAR(1)) AS MULTIPLY, ");
                stb.append("    T1.DATADIV, ");
                stb.append("    T1.PERIOD_MINUTES ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG_R T1 ");
                stb.append("    INNER JOIN CHAIR_DAT T2 ON T2.SEMESTER = T1.SEMESTER ");
                stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append("        AND T2.YEAR = '" + year + "' ");

                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("    T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T2.CLASSCD, ");
                    stb.append("    T2.CURRICULUM_CD, ");
                    stb.append("    T2.SCHOOL_KIND, ");
                }
                stb.append("    T2.SUBCLASSCD, ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END AS REP_DI_CD, ");
                stb.append("    L1.MULTIPLY, ");
                stb.append("    T1.DATADIV, ");
                stb.append("    T1.PERIOD_MINUTES ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG T1 ");
                stb.append("    INNER JOIN CHAIR_DAT T2 ON T2.SEMESTER = T1.SEMESTER ");
                stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append("        AND T2.YEAR = '" + year + "' ");
                stb.append("    INNER JOIN ATTEND_DAT T0 ON T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("        AND T0.ATTENDDATE = T1.EXECUTEDATE ");
                stb.append("        AND T0.PERIODCD = T1.PERIODCD ");
                stb.append("    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD ");

                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T3.CLASSCD, ");
                    stb.append("    T3.CURRICULUM_CD, ");
                    stb.append("    T3.SCHOOL_KIND, ");
                }
                stb.append("     T3.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     VALUE(T1.SEMESTER, '9') AS SEMESTER, ");
                stb.append("     T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("     T1.PERIODCD, ");
                stb.append("     'DI_ABROAD' AS REP_DI_CD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS MULTIPLY, ");
                stb.append("     T1.DATADIV, ");
                stb.append("     T1.PERIOD_MINUTES ");
                stb.append(" FROM ");
                stb.append("     SCHEDULE_SCHREG_R T1 ");
                stb.append("     INNER JOIN SCHREG_TRANSFER_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("         AND T2.TRANSFERCD = '1' ");
                stb.append("         AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
                stb.append("     INNER JOIN CHAIR_DAT T3 ON T3.YEAR = '" + year + "' ");
                stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
                stb.append("         AND T3.CHAIRCD = T1.CHAIRCD  ");
                stb.append(" GROUP BY ");
                stb.append("     GROUPING SETS ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES), ");
                    stb.append("       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES)) ");
                } else {
                    stb.append("      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES), ");
                    stb.append("       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES)) ");
                }

                // 休学中の授業日数
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T3.CLASSCD, ");
                    stb.append("    T3.CURRICULUM_CD, ");
                    stb.append("    T3.SCHOOL_KIND, ");
                }
                stb.append("     T3.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     VALUE(T1.SEMESTER, '9') AS SEMESTER, ");
                stb.append("     T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("     T1.PERIODCD, ");
                stb.append("     'DI_OFFDAYS' AS REP_DI_CD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS MULTIPLY, ");
                stb.append("     T1.DATADIV, ");
                stb.append("     T1.PERIOD_MINUTES ");
                stb.append(" FROM ");
                stb.append("     SCHEDULE_SCHREG_R T1 ");
                stb.append("     INNER JOIN SCHREG_TRANSFER_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("         AND T2.TRANSFERCD = '2' ");
                stb.append("         AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
                stb.append("     INNER JOIN CHAIR_DAT T3 ON T3.YEAR = '" + year + "' ");
                stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
                stb.append("         AND T3.CHAIRCD = T1.CHAIRCD  ");
                stb.append(" GROUP BY ");
                stb.append("     GROUPING SETS ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("      ((T1.CHAIRCD, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES), ");
                    stb.append("       (T1.CHAIRCD, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND, T3.SUBCLASSCD, T1.SCHREGNO,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES)) ");
                } else {
                    stb.append("      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES), ");
                    stb.append("       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T1.PERIOD_MINUTES)) ");
                }
            }
        }
    }

    private static StringBuffer getGradeSemeMonthString(final Map paramMap, final String gradeField, final String semesterField, final String monthField, final Map<String, Object> hasuuMap) {
        final String paramGrade = getString(paramMap, KEY_grade);
        final StringBuffer gradeSemeMonth = new StringBuffer();
        String commaGradeSemeMonth = null;
        for (final String key : hasuuMap.keySet()) {
            if (null != key && key.startsWith("GRADE:")) {
                final String smGrade = key.substring(6);
                if (!"?".equals(paramGrade) && null != paramGrade && !paramGrade.equals(smGrade)) {
                    continue;
                }
                final Map gradeHasuuMap = (Map) hasuuMap.get(key);
                final List<String> smList = (List) gradeHasuuMap.get(KEY_attendSemesInState + "List");
                for (final String sm : smList) {
                    gradeSemeMonth.append(StringUtils.defaultString(commaGradeSemeMonth))
                    .append(gradeField).append(" = '").append(smGrade).append("' ")
                    .append(" AND ").append(semesterField).append(" = '").append(sm.substring(0, 1)).append("' ")
                    .append(" AND ").append(monthField).append(" = '").append(sm.substring(1)).append("' ");
                    commaGradeSemeMonth = " OR ";
                }
            }
        }
        if (null != commaGradeSemeMonth) {
            gradeSemeMonth.insert(0, "(").append(")");
        }
        logInfo(paramMap, " gradeSemeMonth = [" + gradeSemeMonth + "], length = " + gradeSemeMonth.length());
        logInfo(paramMap, " revision = " + revision);
        return gradeSemeMonth;
    }

    private static StringBuffer getGradeSemeHasuuDates(final Map paramMap, final Map<String, Object> hasuuMap) {
        final String paramGrade = getString(paramMap, KEY_grade);
        final StringBuffer gradeSemeHasuuDates = new StringBuffer();
        String commaGradeSemeMonth = null;
        for (final String key : hasuuMap.keySet()) {
            if (null != key && key.startsWith("GRADE:") || GRADE_ALL.equals(key)) {
                final String smGrade = GRADE_ALL.equals(key) ? key : key.substring(6);
                if (null == paramGrade || "?".equals(paramGrade) || null != paramGrade && paramGrade.equals(smGrade) || GRADE_ALL.equals(key)) {
                    final Map gradeHasuuMap = (Map) hasuuMap.get(key);

                    final String befDayFrom = getString(gradeHasuuMap, "befDayFrom");
                    final String befDayTo = getString(gradeHasuuMap, "befDayTo");
                    final String aftDayFrom = getString(gradeHasuuMap, "aftDayFrom");
                    final String aftDayTo = getString(gradeHasuuMap, "aftDayTo");

                    gradeSemeHasuuDates.append(StringUtils.defaultString(commaGradeSemeMonth));
                    String bef = null;
                    if (null != befDayFrom) {
                        bef = "VALUES('" + smGrade + "', 'BEF', '" + befDayFrom + "', '" + befDayTo + "')";
                    }
                    String aft = null;
                    if (null != aftDayFrom) {
                        aft = "VALUES('" + smGrade + "', 'AFT', '" + aftDayFrom + "', '" + aftDayTo + "')";
                    }
                    if (null != bef) {
                        gradeSemeHasuuDates.append(bef);
                    }
                    if (null != bef && null != aft) {
                        gradeSemeHasuuDates.append(" UNION ALL ");
                    }
                    if (null != aft) {
                        gradeSemeHasuuDates.append(aft);
                    }
                    if (null == bef && null == aft) {
                        gradeSemeHasuuDates.append("VALUES('" + smGrade + "', CAST(NULL AS VARCHAR(1)), CAST(NULL AS DATE), CAST(NULL AS DATE))");
                    }
                    commaGradeSemeMonth = " UNION ALL ";
                }
            }
        }
        if (null != commaGradeSemeMonth) {
            gradeSemeHasuuDates.insert(0, "(").append(")");
            logInfo(paramMap, " gradeSemeHasuuDates = " + gradeSemeHasuuDates);
        }
        return gradeSemeHasuuDates;
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineCode definecode, // KNJDefineSchoolに変更するには利用プログラムの再コンパイルが必要
            final KNJDefineSchool defineSchool,
            KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno
    ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final String nenkan = "1";
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("absenceDiv", nenkan);

        return getAttendSubclassAbsenceSql(
                                     year,
                                     eSemester,
                                     null,
                                     null,
                                     paramMap);
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineCode definecode, // KNJDefineSchoolに変更するには利用プログラムの再コンパイルが必要
            final KNJDefineSchool defineSchool,
            KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String absenceDiv
    ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("absenceDiv", absenceDiv);

        return getAttendSubclassAbsenceSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineCode definecode, // KNJDefineSchoolに変更するには利用プログラムの再コンパイルが必要
            final KNJDefineSchool defineSchool,
            KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String absenceDiv,
            final String useCurriculumcd) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("absenceDiv", absenceDiv);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);

        return getAttendSubclassAbsenceSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineCode definecode, // KNJDefineSchoolに変更するには利用プログラムの再コンパイルが必要
            final KNJDefineSchool defineSchool,
            KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String absenceDiv,
            final String useCurriculumcd,
            final String useVirus,
            final String useKoudome) {

        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put("absenceDiv", absenceDiv);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);
        paramMap.put(KEY_useVirus, useVirus);
        paramMap.put(KEY_useKoudome, useKoudome);

        return getAttendSubclassAbsenceSql(
                year,
                eSemester,
                null,
                null,
                paramMap
                );
    }

    /**
     * 科目別出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * 実行結果の SEMESTER が "9" は学期の総合計
     * @param semesFlg      true:ATTEND_SUBCLASS_DAT使用
     * @param definecode    KNJDefineCode
     * @param defineSchool    KNJDefineSchool
     * @param year          年度
     * @param sSemester     対象学期範囲From
     * @param eSemester     対象学期範囲To
     * @param semesInState  ATTEND_SUBCLASS_DATの対象(学期＋月)
     * @param periodInState 対象校時
     * @param befDayFrom    開始日付の端数用From
     * @param befDayTo      開始日付の端数用To
     * @param aftDayFrom    終了日付の端数用From
     * @param aftDayTo      終了日付の端数用To
     * @param grade         学年：指定しない場合は、Null
     * @param hrClass       クラス：指定しない場合は、Null
     * @param schregno      学籍番号：指定しない場合は、Null
     * @param paramMap     その他パラメータのマップ
     * (absenceDiv    1=年間、2=随時
     *  useCurriculumcd 1=教育課程コードを使用する
     *  useVirus true=VIRUSフィールドを使用する
     *  useKoudome true=KOUDOMEフィールドを使用する
     *  useTestCountflg テスト項目マスタのテーブル
     *  )
     * @return 出欠データSQL<code>String</code>を返す
     */
    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineCode definecode, // KNJDefineSchoolに変更するには利用プログラムの再コンパイルが必要
            final KNJDefineSchool defineSchool,
            KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final Map paramMap
    ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);

        return getAttendSubclassAbsenceSql(
                year,
                eSemester,
                null,
                null,
                paramMap
                );
    }

    /**
     * 科目別出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * 実行結果の SEMESTER が "9" は学期の総合計
     * @param semesFlg      true:ATTEND_SUBCLASS_DAT使用
     * @param year          年度
     * @param eSemester     対象学期範囲To
     * @param sDate0        出欠集計開始日付
     * @param date          出欠集計日付
     * @param paramMap0     その他パラメータのマップ
     * (absenceDiv    1=年間、2=随時
     *  useCurriculumcd 1=教育課程コードを使用する
     *  useVirus true=VIRUSフィールドを使用する
     *  useKoudome true=KOUDOMEフィールドを使用する
     *  useTestCountflg テスト項目マスタのテーブル
     *  )
     * @return 出欠データSQL<code>String</code>を返す
     */
    public static String getAttendSubclassAbsenceSql(
            final String year,
            final String eSemester,
            final String sDate0,
            final String date,
            Map paramMap0
    ) {
        Map paramMap = null == paramMap0 ? new HashMap() : paramMap0;
        paramMap.put("year", year);
        paramMap.put(KEY_sdate, sDate0);
        paramMap = setParameterMap(paramMap, date);

        final KNJDefineSchool defineSchool = (KNJDefineSchool) paramMap.get(KEY_knjDefineSchool);

        final String sSemester = (String) paramMap.get(KEY_sSemester);
        final Map hasuuMap = (Map) paramMap.get(KEY_hasuuMap);

        final boolean semesFlg = ((Boolean) hasuuMap.get(KEY_semesFlg)).booleanValue();
        final String semesInState = semesFlg ? StringUtils.defaultString((String) hasuuMap.get(KEY_attendSemesInState), " ('') ") : " ('') ";
        final StringBuffer gradeSemeMonth = getGradeSemeMonthString(paramMap, "T2.GRADE", "T1.SEMESTER", "T1.MONTH", hasuuMap);

        final String subclasscd = (String) paramMap.get("subclasscd");
        final String absenceDiv = StringUtils.defaultString((String) paramMap.get("absenceDiv"), "1");
        final String useCurriculumcd = getString(paramMap, KEY_useCurriculumcd); // 1=教育課程コードを使用する
        final String useVirus = getString(paramMap, KEY_useVirus); // true=VIRUSフィールドを使用する
        final String useKoudome = getString(paramMap, KEY_useKoudome); // true=KOUDOMEフィールドを使用する

        final StringBuffer stb = new StringBuffer();
        appendSchregScheduleCommon(FLG_ATTEND_SUBCLASS_ABSENCE, stb, paramMap, hasuuMap, year, eSemester);

        //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
        stb.append("), SCH_ATTEND_SUM AS(");
        //端数計算有無の判定
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.SEMESTER, ");
        stb.append("        SUM(CASE WHEN T0.SUB_OFFDAYS = '1' AND T1.REP_DI_CD = 'DI_OFFDAYS' THEN 1 ");
        stb.append("                 WHEN T0.SUB_SUSPEND = '1' AND (T1.REP_DI_CD = '2' OR T1.REP_DI_CD = '9') THEN 1 ");
        if ("true".equals(useVirus)) {
            stb.append("                 WHEN T0.SUB_VIRUS = '1' AND (T1.REP_DI_CD = '19' OR T1.REP_DI_CD = '20') THEN 1 ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("                 WHEN T0.SUB_KOUDOME = '1' AND (T1.REP_DI_CD = '25' OR T1.REP_DI_CD = '26') THEN 1 ");
        }
        stb.append("                 WHEN T0.SUB_MOURNING = '1' AND (T1.REP_DI_CD = '3' OR T1.REP_DI_CD = '10') THEN 1 ");
        stb.append("                 WHEN T0.SUB_ABSENT = '1' AND (T1.REP_DI_CD = '1' OR T1.REP_DI_CD = '8') THEN 1 ");
        stb.append("                 WHEN T1.REP_DI_CD IN ('4','5','6','14','11','12','13') THEN 1 ELSE 0 ");
        stb.append("            END) AS ABSENT1, ");
        stb.append("        SUM(CASE WHEN T1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT T1 ");
        stb.append("        , SCHNO T0 ");
        stb.append("    WHERE ");
        stb.append("        T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("        AND T1.SEMESTER = T0.SEMESTER ");
        if (defineSchool.useschchrcountflg) {
            stb.append("        AND NOT EXISTS(SELECT ");
            stb.append("                           'X' ");
            stb.append("                       FROM ");
            stb.append("                           SCH_CHR_COUNTFLG T4 ");
            stb.append("                       WHERE ");
            stb.append("                           T4.EXECUTEDATE = T1.ATTENDDATE ");
            stb.append("                           AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append("                           AND T1.DATADIV IN ('0', '1') ");
            stb.append("                           AND T4.GRADE = T0.GRADE ");
            stb.append("                           AND T4.HR_CLASS = T0.HR_CLASS ");
            stb.append("                           AND T4.COUNTFLG = '0') ");
            stb.append("        AND NOT EXISTS(SELECT ");
            stb.append("                           'X' ");
            stb.append("                       FROM ");
            stb.append("                           TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = T1.ATTENDDATE ");
            stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
        }
        stb.append("    GROUP BY ");
        stb.append("        T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.SEMESTER ");
        stb.append("    UNION ALL ");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.SEMESTER, ");
        stb.append("        SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0) ");
        stb.append("             + VALUE(CASE WHEN T2.SUB_OFFDAYS = '1' THEN T1.OFFDAYS END, 0) ");
        stb.append("             + VALUE(CASE WHEN T2.SUB_SUSPEND = '1' THEN T1.SUSPEND END, 0) ");
        if ("true".equals(useVirus)) {
            stb.append("             + VALUE(CASE WHEN T2.SUB_VIRUS = '1' THEN T1.VIRUS END, 0) ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("             + VALUE(CASE WHEN T2.SUB_KOUDOME = '1' THEN T1.KOUDOME END, 0) ");
        }
        stb.append("             + VALUE(CASE WHEN T2.SUB_MOURNING = '1' THEN T1.MOURNING END, 0) ");
        stb.append("             + VALUE(CASE WHEN T2.SUB_ABSENT = '1' THEN T1.ABSENT END, 0) ");
        stb.append("        ) AS ABSENT1, ");
        stb.append("        SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) AS LATE_EARLY ");
        stb.append("    FROM ");
        stb.append("        ATTEND_SUBCLASS_DAT T1 ");
        stb.append("        INNER JOIN (SELECT SCHREGNO, MAX(SEMESTER) AS SEMESTER FROM SCHNO GROUP BY SCHREGNO) T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("        INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SEMESTER = T3.SEMESTER ");
        stb.append("    WHERE ");
        stb.append("        T1.YEAR = '" + year + "' ");
        stb.append("        AND T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
        if (gradeSemeMonth.length() != 0) {
            stb.append("        AND " + gradeSemeMonth + " ");
        } else {
            stb.append("        AND T1.SEMESTER || T1.MONTH IN " + semesInState + " ");
        }
        stb.append("    GROUP BY ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD ");

        //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
        stb.append("), ATTEND_B AS(");
        if (defineSchool.absent_cov == 1 || defineSchool.absent_cov == 3) {
            //学期でペナルティ欠課を算出する場合
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    CLASSCD, ");
                stb.append("    CURRICULUM_CD, ");
                stb.append("    SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD, ");
            stb.append("        VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1, ");
            stb.append("        VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2, ");
            stb.append("        VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3, ");
            stb.append("        VALUE(SUM(ABSENT),0) AS ABSENT_SEM9 ");
            stb.append("    FROM (SELECT ");
            stb.append("              SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("          CLASSCD, ");
                stb.append("          CURRICULUM_CD, ");
                stb.append("          SCHOOL_KIND, ");
            }
            stb.append("              SUBCLASSCD, ");
            stb.append("              SEMESTER, ");
            if (defineSchool.absent_cov == 1) {
                stb.append("                 VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + defineSchool.absent_cov_late + " AS ABSENT ");
            } else {
                stb.append("                 FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + defineSchool.absent_cov_late + ",5,1)) AS ABSENT ");
            }
            stb.append("          FROM ");
            stb.append("              SCH_ATTEND_SUM T1 ");
            stb.append("          GROUP BY ");
            stb.append("              SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("          CLASSCD, ");
                stb.append("          CURRICULUM_CD, ");
                stb.append("          SCHOOL_KIND, ");
            }
            stb.append("              SUBCLASSCD, ");
            stb.append("              SEMESTER ");
            stb.append("          ) T1 ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("          CLASSCD, ");
                stb.append("          CURRICULUM_CD, ");
                stb.append("          SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD ");
        } else if (defineSchool.absent_cov == 2 || defineSchool.absent_cov == 4) {
            //通年でペナルティ欠課を算出する場合
            //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T1.ABSENT_SEM9, ");
            stb.append("        T2.ABSENT_SEM1, ");
            stb.append("        T2.ABSENT_SEM2, ");
            stb.append("        T2.ABSENT_SEM3 ");
            stb.append("    FROM ");
            stb.append("        (SELECT ");
            stb.append("             SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("         CLASSCD, ");
                stb.append("         CURRICULUM_CD, ");
                stb.append("         SCHOOL_KIND, ");
            }
            stb.append("             SUBCLASSCD, ");
            if (defineSchool.absent_cov == 2) {
                stb.append("       VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + defineSchool.absent_cov_late + " AS ABSENT_SEM9 ");
            } else {
                stb.append("       FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + defineSchool.absent_cov_late + ",5,1)) AS ABSENT_SEM9 ");
            }
            stb.append("         FROM ");
            stb.append("             SCH_ATTEND_SUM T1 ");
            stb.append("         GROUP BY ");
            stb.append("             SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("         CLASSCD, ");
                stb.append("         CURRICULUM_CD, ");
                stb.append("         SCHOOL_KIND, ");
            }
            stb.append("             SUBCLASSCD ");
            stb.append("        )T1, ");
            stb.append("        (SELECT ");
            stb.append("             SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("         CLASSCD, ");
                stb.append("         CURRICULUM_CD, ");
                stb.append("         SCHOOL_KIND, ");
            }
            stb.append("             SUBCLASSCD, ");
            stb.append("             VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1, ");
            stb.append("             VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2, ");
            stb.append("             VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ");
            stb.append("         FROM ");
            stb.append("             (SELECT ");
            stb.append("                  SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("              CLASSCD, ");
                stb.append("              CURRICULUM_CD, ");
                stb.append("              SCHOOL_KIND, ");
            }
            stb.append("                  SUBCLASSCD, ");
            stb.append("                  SEMESTER, ");
            if (defineSchool.absent_cov == 2) {
                stb.append("       VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + defineSchool.absent_cov_late + " AS ABSENT ");
            } else {
                stb.append("       FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + defineSchool.absent_cov_late + ",5,1)) AS ABSENT ");
            }
            stb.append("              FROM ");
            stb.append("                  SCH_ATTEND_SUM T1 ");
            stb.append("              GROUP BY ");
            stb.append("                  SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("              CLASSCD, ");
                stb.append("              CURRICULUM_CD, ");
                stb.append("              SCHOOL_KIND, ");
            }
            stb.append("                  SUBCLASSCD, ");
            stb.append("                  SEMESTER ");
            stb.append("              ) T1 ");
            stb.append("         GROUP BY ");
            stb.append("             SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("         CLASSCD, ");
                stb.append("         CURRICULUM_CD, ");
                stb.append("         SCHOOL_KIND, ");
            }
            stb.append("             SUBCLASSCD ");
            stb.append("        ) T2 ");
            stb.append("    WHERE ");
            stb.append("        T1.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            }
            stb.append("        AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
        } else{
            //ペナルティ欠課なしの場合
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    CLASSCD, ");
                stb.append("    CURRICULUM_CD, ");
                stb.append("    SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD, ");
            stb.append("        VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM1, ");
            stb.append("        VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM2, ");
            stb.append("        VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM3, ");
            stb.append("        VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_SUM T1 ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    CLASSCD, ");
                stb.append("    CURRICULUM_CD, ");
                stb.append("    SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD ");
        }
        stb.append(     ") ");
        //単位マスタの欠課時数上限値
        stb.append(" ,T_CREDIT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     CREDIT_MST T1 ");
        stb.append("     INNER JOIN (SELECT SCHREGNO,GRADE,COURSECD,MAJORCD,COURSECODE FROM SCHNO ");
        stb.append("     WHERE JUGYOU_JISU_FLG = '1' "); // 法定
        stb.append("     GROUP BY SCHREGNO,GRADE,COURSECD,MAJORCD,COURSECODE) T2 ON ");
        stb.append("         T1.COURSECD = T2.COURSECD AND ");
        stb.append("         T1.MAJORCD = T2.MAJORCD AND ");
        stb.append("         T1.GRADE = T2.GRADE AND ");
        stb.append("         T1.COURSECODE = T2.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR='" + year + "' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ABSENCE_HIGH_DAT T1 ");
        stb.append("     INNER JOIN (SELECT SCHREGNO FROM SCHNO ");
        stb.append("     WHERE JUGYOU_JISU_FLG = '2' "); // 実時数
        stb.append("     GROUP BY SCHREGNO) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' AND ");
        stb.append("     T1.DIV = '" + absenceDiv + "' ");
        stb.append(" )   ");
        //合併先科目の欠課時数
        stb.append(" , ATTEND_COMBINED AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T2.COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("    T2.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("    T2.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
        }
        stb.append("     T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM1 THEN 0 ELSE T1.ABSENT_SEM1 END) AS ABSENT_SEM1, ");
        stb.append("     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM2 THEN 0 ELSE T1.ABSENT_SEM2 END) AS ABSENT_SEM2, ");
        stb.append("     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM3 THEN 0 ELSE T1.ABSENT_SEM3 END) AS ABSENT_SEM3, ");
        stb.append("     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM9 THEN 0 ELSE T1.ABSENT_SEM9 END) AS ABSENT_SEM9 ");
        stb.append(" FROM ");
        stb.append("     ATTEND_B T1 ");
        stb.append("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append("              ON T2.REPLACECD='1' ");
        stb.append("             AND T2.YEAR='" + year + "' ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("             AND T2.ATTEND_CLASSCD=T1.CLASSCD ");
            stb.append("             AND T2.ATTEND_CURRICULUM_CD=T1.CURRICULUM_CD ");
            stb.append("             AND T2.ATTEND_SCHOOL_KIND=T1.SCHOOL_KIND ");
        }
        stb.append("             AND T2.ATTEND_SUBCLASSCD=T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN T_CREDIT T3 ");
        stb.append("              ON T3.SCHREGNO=T1.SCHREGNO ");
        stb.append("             AND T3.SUBCLASSCD=T1.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("             AND T3.CLASSCD=T1.CLASSCD ");
            stb.append("             AND T3.CURRICULUM_CD=T1.CURRICULUM_CD ");
            stb.append("             AND T3.SCHOOL_KIND=T1.SCHOOL_KIND ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T2.COMBINED_CLASSCD, ");
            stb.append("    T2.COMBINED_CURRICULUM_CD, ");
            stb.append("    T2.COMBINED_SCHOOL_KIND, ");
        }
        stb.append("     T2.COMBINED_SUBCLASSCD ");
        stb.append(" )   ");

        //メイン表
        stb.append(" SELECT ");
        stb.append("    SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("    SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    '1' AS SEMESTER, ");
        stb.append("    ABSENT_SEM1 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("    ATTEND_B TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("    SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("    SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    '2' AS SEMESTER, ");
        stb.append("    ABSENT_SEM2 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("    ATTEND_B TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("    SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("    SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    '3' AS SEMESTER, ");
        stb.append("    ABSENT_SEM3 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("    ATTEND_B TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("    SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("    SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    '9' AS SEMESTER, ");
        stb.append("    ABSENT_SEM9 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("    ATTEND_B TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION   ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBCLASSCD  AS SUBCLASSCD, ");
        stb.append("     '1' AS SEMESTER, ");
        stb.append("     ABSENT_SEM1 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("     ATTEND_COMBINED TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION   ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     '2' AS SEMESTER, ");
        stb.append("     ABSENT_SEM2 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("     ATTEND_COMBINED TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION   ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     '3' AS SEMESTER, ");
        stb.append("     ABSENT_SEM3 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("     ATTEND_COMBINED TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" UNION   ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     '9' AS SEMESTER, ");
        stb.append("     ABSENT_SEM9 AS ABSENT_SEM ");
        stb.append(" FROM ");
        stb.append("     ATTEND_COMBINED TT0 ");
        if (null != subclasscd) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" ORDER BY ");
        stb.append("    SCHREGNO, ");
        stb.append("    SEMESTER ");

        logInfo(paramMap, " getAttendSubclassAbsenceSql = " + stb.toString(), !"1".equals(paramMap.get(KEY_noOutputSql)));
        return stb.toString();
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSubclassSql(
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno
    ) {
        final Map hasuuMap = createHasuuMap(true, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        return getAttendSubclassSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSubclassSql(
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String useCurriculumcd
    ) {

        final Map hasuuMap = createHasuuMap(true, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);

        return getAttendSubclassSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    /**
     * @deprecated 未適用プログラムコール用
     */
    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String useCurriculumcd) {

        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);

        return getAttendSubclassSql(
                year,
                eSemester,
                null,
                null,
                paramMap);
    }

    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final String useCurriculumcd,
            final String useVirus,
            final String useKoudome) {

        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        final Map paramMap = new HashMap();
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        paramMap.put(KEY_useCurriculumcd, useCurriculumcd);
        paramMap.put(KEY_useVirus, useVirus);
        paramMap.put(KEY_useKoudome, useKoudome);

        return getAttendSubclassSql(
                        year,
                        eSemester,
                        null,
                        null,
                        paramMap
                );
    }


    /**
     * 科目別出欠データSQLを返す(KNJD154から移行)
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * 実行結果の SEMESTER が "9" は学期の総合計
     * @param defineSchool    KNJDefineSchool
     * @param knjSchoolMst        KNJSchoolMst
     * @param year          年度
     * @param sSemester     対象学期範囲From
     * @param eSemester     対象学期範囲To
     * @param semesInState  ATTEND_SUBCLASS_DATの対象(学期＋月)
     * @param periodInState 対象校時
     * @param befDayFrom    開始日付の端数用From
     * @param befDayTo      開始日付の端数用To
     * @param aftDayFrom    終了日付の端数用From
     * @param aftDayTo      終了日付の端数用To
     * @param grade         学年：指定しない場合は、Null
     * @param hrClass       クラス：指定しない場合は、Null
     * @param schregno      学籍番号：指定しない場合は、Null
     * @param paramMap0     その他パラメータのマップ
     * (absenceDiv    1=年間、2=随時
     *  useCurriculumcd 1=教育課程コードを使用する
     *  useVirus true=VIRUSフィールドを使用する
     *  useKoudome true=KOUDOMEフィールドを使用する
     *  useTestCountflg テスト項目マスタのテーブル
     *  )
     */
    public static String getAttendSubclassSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchool,
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final String schregno,
            final Map paramMap
    ) {
        final Map hasuuMap = createHasuuMap(semesFlg, semesInState, befDayFrom, befDayTo, aftDayFrom, aftDayTo, null);
        putParam(paramMap, defineSchool, knjSchoolMst, sSemester, periodInState, hasuuMap, grade, hrClass, schregno);
        return getAttendSubclassSql(
                year,
                eSemester,
                null,
                null,
                paramMap
        );
    }

    /**
     * 科目別出欠データSQLを返す(KNJD154から移行)
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * 実行結果の SEMESTER が "9" は学期の総合計
     * @param year          年度
     * @param eSemester     対象学期範囲To
     * @param sDate0        出欠集計開始日付
     * @param date          出欠集計日付
     * @param paramMap0     その他パラメータのマップ
     * (absenceDiv    1=年間、2=随時
     *  useCurriculumcd 1=教育課程コードを使用する
     *  useVirus true=VIRUSフィールドを使用する
     *  useKoudome true=KOUDOMEフィールドを使用する
     *  useTestCountflg テスト項目マスタのテーブル
     *  )
     */
    public static String getAttendSubclassSql(
            final String year,
            final String eSemester,
            final String sDate0,
            final String date,
            Map paramMap0
    ) {
        Map paramMap = null == paramMap0 ? new HashMap() : paramMap0;
        paramMap.put("year", year);
        paramMap.put(KEY_sdate, sDate0);
        paramMap = setParameterMap(paramMap, date);
        final KNJDefineSchool defineSchool = (KNJDefineSchool) paramMap.get(KEY_knjDefineSchool);
        final KNJSchoolMst knjSchoolMst = (KNJSchoolMst) paramMap.get(KEY_knjSchoolMst);

        final String sSemester = (String) paramMap.get(KEY_sSemester);
        final Map hasuuMap = (Map) paramMap.get(KEY_hasuuMap);

        final boolean semesFlg = ((Boolean) hasuuMap.get(KEY_semesFlg)).booleanValue();
        final String semesInState = semesFlg ? StringUtils.defaultString((String) hasuuMap.get(KEY_attendSemesInState), " ('') ") : " ('') ";
        final StringBuffer gradeSemeMonth = getGradeSemeMonthString(paramMap, "T2.GRADE", "T1.SEMESTER", "T1.MONTH", hasuuMap);

        final String[] subclasscdArray = (String[]) paramMap.get("subclasscdArray");
        if (null != subclasscdArray) {
            log.info(" subclasscdArray = " + ArrayUtils.toString(subclasscdArray));
        }
        final String subclasscd = (String) paramMap.get("subclasscd");
        final String absenceDiv = StringUtils.defaultString((String) paramMap.get("absenceDiv"), "1");
        final String useCurriculumcd = getString(paramMap, KEY_useCurriculumcd); // 1=教育課程コードを使用する
        final String useVirus = getString(paramMap, KEY_useVirus); // true=VIRUSフィールドを使用する
        final String useKoudome = getString(paramMap, KEY_useKoudome); // true=KOUDOMEフィールドを使用する
        final String z010 = (String) paramMap.get(KEY_z010Name1);
        final boolean isCombinedSubclassNotRequiredCreditMst = "tosa".equals(z010) || "sundaikoufu".equals(z010);
        final boolean isAttendCalcKwansei = "1".equals(paramMap.get(KEY_attendKekkaCalcKwanseiMethod)); // 関西学院の欠課算出 (1日欠席分を除く)
        final List absentCovList = (List) paramMap.get("ABSENT_COV_LIST");
        String useAbsentCov = "USE_STUDENT_ABSENT_COV";
        if (null == absentCovList || absentCovList.size() > 1) {
            if (null != knjSchoolMst) {
                useAbsentCov = knjSchoolMst._absentCov;
            }
        } else {
            if (absentCovList.size() == 0) {
                useAbsentCov = null;
            } else if (absentCovList.size() == 1) {
                useAbsentCov = (String) absentCovList.get(0);
            }
        }

        final StringBuffer stb = new StringBuffer();
        appendSchregScheduleCommon(FLG_ATTEND_SUBCLASS, stb, paramMap, hasuuMap, year, eSemester);

        String unionAll = "";
        stb.append("), SCH_ATTEND_SUM_HASU_MINUTES AS(");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      , T1.CLASSCD ");
            stb.append("      , T1.CURRICULUM_CD ");
            stb.append("      , T1.SCHOOL_KIND ");
        }
        stb.append("      , T1.SUBCLASSCD ");
        stb.append("      , T1.SEMESTER ");
        stb.append("      , T0.JITU_JIFUN ");
        stb.append("      , (SUM(CASE WHEN T1.REP_DI_CD = 'DI_ALL' THEN T1.PERIOD_MINUTES ELSE 0 END) ");
        stb.append("        - SUM(CASE ");
        stb.append("                  WHEN T0.SUB_OFFDAYS <> '1' AND T1.REP_DI_CD = 'DI_OFFDAYS' OR T1.REP_DI_CD = 'DI_ABROAD' THEN T1.PERIOD_MINUTES ");
        stb.append("              ELSE 0 END) ");
        stb.append("                                                                                                                        ) AS LESSON_MINUTES ");
        stb.append("      , (SUM(CASE WHEN T1.REP_DI_CD = 'DI_ALL' THEN T1.PERIOD_MINUTES ELSE 0 END) ");
        stb.append("        - SUM(CASE ");
        stb.append("                  WHEN T0.SUB_OFFDAYS <> '1' AND T1.REP_DI_CD = 'DI_OFFDAYS' OR T1.REP_DI_CD = 'DI_ABROAD' THEN T1.PERIOD_MINUTES ELSE 0 END) ");
        stb.append("        - SUM(CASE ");
        stb.append("                  WHEN T0.SUB_SUSPEND <> '1' AND (T1.REP_DI_CD = '2' OR T1.REP_DI_CD = '9')         THEN T1.PERIOD_MINUTES ");
        stb.append("                  WHEN T0.SUB_MOURNING <> '1' AND (T1.REP_DI_CD = '3' OR T1.REP_DI_CD = '10')       THEN T1.PERIOD_MINUTES ");
        if ("true".equals(useVirus)) {
            stb.append("              WHEN T0.SUB_VIRUS <> '1' AND (T1.REP_DI_CD = '19' OR T1.REP_DI_CD = '20')         THEN T1.PERIOD_MINUTES ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("              WHEN T0.SUB_KOUDOME <> '1' AND (T1.REP_DI_CD = '25' OR T1.REP_DI_CD = '26')       THEN T1.PERIOD_MINUTES ");
        }
        stb.append("           ELSE 0 END)");
        stb.append("                                                                                                                       ) AS MLESSON_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD = 'DI_ABROAD'       THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS ABROAD_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD = 'DI_OFFDAYS'      THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS OFFDAYS_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('1', '8')       THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS KOUKETSU_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('2', '9')       THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS SUSPEND_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('3', '10')      THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS MOURNING_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD = '14'              THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS NURSEOFF_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('15','23','24') THEN INT(VALUE(T1.MULTIPLY, '1')) * T1.PERIOD_MINUTES ELSE 0 END) AS LATE_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD = '16'              THEN                                T1.PERIOD_MINUTES ELSE 0 END) AS EARLY_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('19', '20') THEN ");
        if ("true".equals(useVirus)) {
            stb.append("        T1.PERIOD_MINUTES ELSE 0 ");
        } else {
            stb.append("        0 ");
        }
        stb.append("                                                                                                                    END) AS VIRUS_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('25', '26') THEN ");
        if ("true".equals(useKoudome)) {
            stb.append("        T1.PERIOD_MINUTES ELSE 0 ");
        } else {
            stb.append("        0 ");
        }
        stb.append("                                                                                                                    END) AS KOUDOME_MINUTES ");

        stb.append("      , SUM(CASE WHEN T0.SUB_OFFDAYS = '1'     AND T1.REP_DI_CD = 'DI_OFFDAYS'                   THEN T1.PERIOD_MINUTES ");
        stb.append("                 WHEN T0.SUB_SUSPEND = '1'     AND (T1.REP_DI_CD = '2' OR T1.REP_DI_CD = '9')    THEN T1.PERIOD_MINUTES ");
        if ("true".equals(useVirus)) {
            stb.append("             WHEN T0.SUB_VIRUS = '1'       AND (T1.REP_DI_CD = '19' OR T1.REP_DI_CD = '20')  THEN T1.PERIOD_MINUTES ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("             WHEN T0.SUB_KOUDOME = '1'     AND (T1.REP_DI_CD = '25' OR T1.REP_DI_CD = '26')  THEN T1.PERIOD_MINUTES ");
        }
        stb.append("                 WHEN T0.SUB_MOURNING = '1'    AND (T1.REP_DI_CD = '3' OR T1.REP_DI_CD = '10')   THEN T1.PERIOD_MINUTES ");
        stb.append("                 WHEN T0.SUB_ABSENT = '1'      AND (T1.REP_DI_CD = '1' OR T1.REP_DI_CD = '8')    THEN T1.PERIOD_MINUTES ");
        stb.append("                 WHEN T1.REP_DI_CD IN ('4','5','6','14','11','12','13')                          THEN T1.PERIOD_MINUTES ");
        stb.append("                                                                                                             ELSE 0 END) AS ABSENT1_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('4', '11')                                                THEN T1.PERIOD_MINUTES ");
        stb.append("                                                                                                             ELSE 0 END) AS SICK_ONLY_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('5', '12')                                                THEN T1.PERIOD_MINUTES ");
        stb.append("                                                                                                             ELSE 0 END) AS NOTICE_ONLY_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN ('6', '13')                                                THEN T1.PERIOD_MINUTES ");
        stb.append("                                                                                                             ELSE 0 END) AS NONOTICE_ONLY_MINUTES ");
        stb.append("      , SUM(CASE WHEN T1.REP_DI_CD IN('15','16','23','24') THEN INT(VALUE(T1.MULTIPLY, '1')) * T1.PERIOD_MINUTES ");
        stb.append("                                                                                                             ELSE 0 END) AS LATE_EARLY_MINUTES ");
        stb.append("    FROM ");
        stb.append("        T_ATTEND_DAT T1 ");
        stb.append("        , SCHNO T0 ");
        stb.append("    WHERE ");
        stb.append("        T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("        AND T1.SEMESTER = T0.SEMESTER ");
        if (defineSchool.useschchrcountflg) {
            stb.append("        AND NOT EXISTS(SELECT ");
            stb.append("                           'X' ");
            stb.append("                       FROM ");
            stb.append("                           SCH_CHR_COUNTFLG T4 ");
            stb.append("                       WHERE ");
            stb.append("                           T4.EXECUTEDATE = T1.ATTENDDATE ");
            stb.append("                           AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append("                           AND T1.DATADIV IN ('0', '1') ");
            stb.append("                           AND T4.GRADE = T0.GRADE ");
            stb.append("                           AND T4.HR_CLASS = T0.HR_CLASS ");
            stb.append("                           AND T4.COUNTFLG = '0') ");
            stb.append("        AND NOT EXISTS(SELECT ");
            stb.append("                           'X' ");
            stb.append("                       FROM ");
            stb.append("                           TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = T1.ATTENDDATE ");
            stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
        }
        stb.append("    GROUP BY ");
        stb.append("        T1.SCHREGNO ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      , T1.CLASSCD ");
            stb.append("      , T1.CURRICULUM_CD ");
            stb.append("      , T1.SCHOOL_KIND ");
        }
        stb.append("      , T1.SUBCLASSCD ");
        stb.append("      , T1.SEMESTER ");
        stb.append("      , T1.PERIOD_MINUTES ");
        stb.append("      , T0.JITU_JIFUN ");

        stb.append("), SCH_ATTEND_SUM_HASU_MINUTES_D AS(");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      , T1.CLASSCD ");
            stb.append("      , T1.CURRICULUM_CD ");
            stb.append("      , T1.SCHOOL_KIND ");
        }
        stb.append("      , T1.SUBCLASSCD ");
        stb.append("      , T1.SEMESTER ");
        stb.append("      , T1.LESSON_MINUTES ");
        stb.append("      , CEIL(T1.LESSON_MINUTES     * 1.0 / T1.JITU_JIFUN) AS LESSON ");
        stb.append("      , T1.MLESSON_MINUTES ");
        stb.append("      , CEIL(T1.MLESSON_MINUTES    * 1.0 / T1.JITU_JIFUN) AS MLESSON ");
        stb.append("      , T1.ABROAD_MINUTES ");
        stb.append("      , CEIL(T1.ABROAD_MINUTES     * 1.0 / T1.JITU_JIFUN) AS ABROAD ");
        stb.append("      , T1.OFFDAYS_MINUTES ");
        stb.append("      , CEIL(T1.OFFDAYS_MINUTES    * 1.0 / T1.JITU_JIFUN) AS OFFDAYS ");
        stb.append("      , T1.KOUKETSU_MINUTES ");
        stb.append("      , CEIL(T1.KOUKETSU_MINUTES   * 1.0 / T1.JITU_JIFUN) AS KOUKETSU ");
        stb.append("      , T1.SUSPEND_MINUTES ");
        stb.append("      , CEIL(T1.SUSPEND_MINUTES    * 1.0 / T1.JITU_JIFUN) AS SUSPEND ");
        stb.append("      , T1.MOURNING_MINUTES ");
        stb.append("      , CEIL(T1.MOURNING_MINUTES   * 1.0 / T1.JITU_JIFUN) AS MOURNING ");
        stb.append("      , T1.NURSEOFF_MINUTES ");
        stb.append("      , CEIL(T1.NURSEOFF_MINUTES   * 1.0 / T1.JITU_JIFUN) AS NURSEOFF ");
        stb.append("      , T1.LATE_MINUTES ");
        stb.append("      , CEIL(T1.LATE_MINUTES       * 1.0 / T1.JITU_JIFUN) AS LATE ");
        stb.append("      , T1.EARLY_MINUTES ");
        stb.append("      , CEIL(T1.EARLY_MINUTES      * 1.0 / T1.JITU_JIFUN) AS EARLY ");
        stb.append("      , T1.VIRUS_MINUTES ");
        stb.append("      , CEIL(T1.VIRUS_MINUTES      * 1.0 / T1.JITU_JIFUN) AS VIRUS ");
        stb.append("      , T1.KOUDOME_MINUTES ");
        stb.append("      , CEIL(T1.KOUDOME_MINUTES    * 1.0 / T1.JITU_JIFUN) AS KOUDOME ");
        stb.append("      , T1.ABSENT1_MINUTES ");
        stb.append("      , CEIL(T1.ABSENT1_MINUTES    * 1.0 / T1.JITU_JIFUN) AS ABSENT1 ");
        stb.append("      , T1.SICK_ONLY_MINUTES ");
        stb.append("      , CEIL(T1.SICK_ONLY_MINUTES  * 1.0 / T1.JITU_JIFUN) AS SICK_ONLY ");
        stb.append("      , T1.NOTICE_ONLY_MINUTES ");
        stb.append("      , CEIL(T1.NOTICE_ONLY_MINUTES * 1.0 / T1.JITU_JIFUN) AS NOTICE_ONLY ");
        stb.append("      , T1.NONOTICE_ONLY_MINUTES ");
        stb.append("      , CEIL(T1.NONOTICE_ONLY_MINUTES * 1.0 / T1.JITU_JIFUN) AS NONOTICE_ONLY ");
        stb.append("      , T1.LATE_EARLY_MINUTES ");
        stb.append("      , CEIL(T1.LATE_EARLY_MINUTES * 1.0 / T1.JITU_JIFUN) AS LATE_EARLY ");
        stb.append("    FROM ");
        stb.append("        SCH_ATTEND_SUM_HASU_MINUTES T1 ");

        stb.append("), SCH_ATTEND_SUM1 AS(");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      , T1.CLASSCD ");
            stb.append("      , T1.CURRICULUM_CD ");
            stb.append("      , T1.SCHOOL_KIND ");
        }
        stb.append("      , T1.SUBCLASSCD ");
        stb.append("      , T1.SEMESTER ");
        stb.append("      , T1.LESSON ");
        stb.append("      , T1.MLESSON ");
        stb.append("      , T1.ABROAD ");
        stb.append("      , T1.OFFDAYS ");
        stb.append("      , T1.KOUKETSU ");
        stb.append("      , T1.SUSPEND ");
        stb.append("      , T1.MOURNING ");
        stb.append("      , T1.NURSEOFF ");
        stb.append("      , T1.LATE ");
        stb.append("      , T1.EARLY ");
        stb.append("      , T1.VIRUS ");
        stb.append("      , T1.KOUDOME ");
        stb.append("      , T1.ABSENT1 ");
        stb.append("      , T1.SICK_ONLY ");
        stb.append("      , T1.NOTICE_ONLY ");
        stb.append("      , T1.NONOTICE_ONLY ");
        stb.append("      , T1.LATE_EARLY ");
        stb.append("    FROM ");
        stb.append("        SCH_ATTEND_SUM_HASU_MINUTES_D T1 ");
        unionAll = "    UNION ALL ";
        stb.append(unionAll); // ATTEND_SUBCLASS_DAT
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.SEMESTER, ");
        stb.append("        SUM(VALUE(T1.LESSON,0) ");
        stb.append("            - VALUE(CASE WHEN T2.SUB_OFFDAYS <> '1' THEN T1.OFFDAYS END, 0) ");
        stb.append("            - VALUE(T1.ABROAD, 0)");
        stb.append("        ) AS LESSON, ");
        stb.append("        SUM(VALUE(T1.LESSON,0) ");
        stb.append("            - VALUE(CASE WHEN T2.SUB_OFFDAYS <> '1' THEN T1.OFFDAYS END, 0) ");
        stb.append("            - VALUE(T1.ABROAD, 0)");
        stb.append("            - VALUE(CASE WHEN T2.SUB_SUSPEND <> '1' THEN T1.SUSPEND END, 0) ");
        stb.append("            - VALUE(CASE WHEN T2.SUB_MOURNING <> '1' THEN T1.MOURNING END, 0) ");
        if ("true".equals(useVirus)) {
            stb.append("            - VALUE(CASE WHEN T2.SUB_VIRUS <> '1' THEN T1.VIRUS END, 0) ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("            - VALUE(CASE WHEN T2.SUB_KOUDOME <> '1' THEN T1.KOUDOME END, 0) ");
        }
        stb.append("        ) AS MLESSON, ");
        stb.append("        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ");
        stb.append("        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("        SUM(VALUE(T1.ABSENT, 0)) AS KOUKETSU, ");
        stb.append("        SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ");
        stb.append("        SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ");
        stb.append("        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ");
        stb.append("        SUM(VALUE(T1.LATE,0)) AS LATE, ");
        stb.append("        SUM(VALUE(T1.EARLY,0)) AS EARLY, ");
        if ("true".equals(useVirus)) {
            stb.append("        SUM(VALUE(T1.VIRUS,0)) AS VIRUS, ");
        } else {
            stb.append("        0 AS VIRUS, ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("        SUM(VALUE(T1.KOUDOME,0)) AS KOUDOME, ");
        } else {
            stb.append("        0 AS KOUDOME, ");
        }

        stb.append("        SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0) ");
        stb.append("             + VALUE(CASE WHEN T2.SUB_OFFDAYS = '1' THEN T1.OFFDAYS END, 0) ");
        stb.append("             + VALUE(CASE WHEN T2.SUB_SUSPEND = '1' THEN T1.SUSPEND END, 0) ");
        if ("true".equals(useVirus)) {
            stb.append("             + VALUE(CASE WHEN T2.SUB_VIRUS = '1' THEN T1.VIRUS END, 0) ");
        }
        if ("true".equals(useKoudome)) {
            stb.append("             + VALUE(CASE WHEN T2.SUB_KOUDOME = '1' THEN T1.KOUDOME END, 0) ");
        }
        stb.append("             + VALUE(CASE WHEN T2.SUB_MOURNING = '1' THEN T1.MOURNING END, 0) ");
        stb.append("             + VALUE(CASE WHEN T2.SUB_ABSENT = '1' THEN T1.ABSENT END, 0) ");
        stb.append("        ) AS ABSENT1, ");
        stb.append("        SUM(VALUE(T1.SICK,0)) AS SICK_ONLY, ");
        stb.append("        SUM(VALUE(T1.NOTICE,0)) AS NOTICE_ONLY, ");
        stb.append("        SUM(VALUE(T1.NONOTICE,0)) AS NONOTICE_ONLY, ");
        stb.append("        SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) AS LATE_EARLY ");
        stb.append("    FROM ");
        stb.append("        ATTEND_SUBCLASS_DAT T1 ");
        stb.append("        INNER JOIN (SELECT SCHREGNO, MAX(SEMESTER) AS SEMESTER FROM SCHNO GROUP BY SCHREGNO) T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("        INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SEMESTER = T3.SEMESTER ");
        stb.append("    WHERE ");
        stb.append("        T1.YEAR = '" + year + "' ");
        stb.append("        AND T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
        if (gradeSemeMonth.length() != 0) {
            stb.append("        AND " + gradeSemeMonth + " ");
        } else {
            stb.append("        AND T1.SEMESTER || T1.MONTH IN " + semesInState + " ");
        }
        stb.append("    GROUP BY ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD ");

        if (isAttendCalcKwansei) {
            // 時間割で1日すべて同一の勤怠が入力された日付
            stb.append("   ), SAME_DI_CD_ATT_DAT AS (   ");
            stb.append("    SELECT  ");
            stb.append("        T1.ATTENDDATE ");
            stb.append("      , T1.SCHREGNO ");
            stb.append("      , T1.DI_CD ");
            stb.append("    FROM ATTEND_DAY_DAT T1 ");
            stb.append("    INNER JOIN SEMESTER_MST L1 ON ");
            stb.append("         L1.YEAR = T1.YEAR ");
            stb.append("       AND L1.SEMESTER <> '9' ");
            stb.append("       AND T1.ATTENDDATE BETWEEN L1.SDATE AND L1.EDATE  ");
            stb.append("    INNER JOIN SCHREG_REGD_DAT REGD ON ");
            stb.append("         REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND REGD.YEAR = L1.YEAR ");
            stb.append("       AND REGD.SEMESTER = L1.SEMESTER ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON ");
            stb.append("           GDAT.YEAR = L1.YEAR ");
            stb.append("       AND GDAT.GRADE = REGD.GRADE ");
            stb.append("       AND GDAT.SCHOOL_KIND = 'H' ");
            stb.append("    WHERE ");
            stb.append("        T1.ATTENDDATE BETWEEN '" + paramMap.get(KEY_sdate) + "' AND '" + date + "' ");
            stb.append("       AND VALUE(T1.DI_CD, '0') NOT IN ('0', '15', '16') ");
            stb.append("   ), SAME_DI_CD_ATT AS (   ");
            stb.append("    SELECT  ");
            stb.append("        T1.SCHREGNO ");
            stb.append("      , SEME.SEMESTER ");
            stb.append("      , CHR.CLASSCD ");
            stb.append("      , CHR.CURRICULUM_CD ");
            stb.append("      , CHR.SCHOOL_KIND ");
            stb.append("      , CHR.SUBCLASSCD ");
            stb.append("      , SUM(CASE WHEN T1.DI_CD = '2' THEN 1 ELSE 0 END) AS SUSPEND ");
            stb.append("      , SUM(CASE WHEN T1.DI_CD = '3' THEN 1 ELSE 0 END) AS MOURNING ");
            stb.append("      , SUM(CASE WHEN T1.DI_CD = '4' THEN 1 ELSE 0 END) AS SICK_ONLY ");
            stb.append("      , SUM(CASE WHEN T1.DI_CD = '5' THEN 1 ELSE 0 END) AS NOTICE_ONLY ");
            stb.append("      , SUM(CASE WHEN T1.DI_CD = '6' THEN 1 ELSE 0 END) AS NONOTICE_ONLY ");
            stb.append("    FROM ATTEND_DAT T1 ");
            stb.append("    INNER JOIN SEMESTER_MST SEME ");
            stb.append("      ON SEME.SEMESTER <> '9' ");
            stb.append("     AND T1.ATTENDDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append("    INNER JOIN CHAIR_DAT CHR ");
            stb.append("      ON CHR.YEAR = SEME.YEAR ");
            stb.append("     AND CHR.SEMESTER = SEME.SEMESTER ");
            stb.append("     AND CHR.CHAIRCD = T1.CHAIRCD ");
            stb.append("    INNER JOIN  ");
            stb.append("        SAME_DI_CD_ATT_DAT SAME ON SAME.ATTENDDATE = T1.ATTENDDATE AND SAME.SCHREGNO = T1.SCHREGNO ");
            stb.append("                          AND ( T1.DI_CD = SAME.DI_CD ");
            stb.append("                             OR T1.DI_CD = '2' AND SAME.DI_CD IN ('4', '5', '6') "); // 1日勤怠が欠席なら出停をカウントしない
            stb.append("                             OR T1.DI_CD = '3' AND SAME.DI_CD IN ('4', '5', '6') "); // 1日勤怠が欠席なら忌引をカウントしない
            stb.append("                             OR T1.DI_CD = '4' AND SAME.DI_CD IN ('4', '5', '6') "); // 1日勤怠が欠席なら病欠をカウントしない
            stb.append("                             OR T1.DI_CD = '5' AND SAME.DI_CD IN ('4', '5', '6') "); // 1日勤怠が欠席なら届出有をカウントしない
            stb.append("                             OR T1.DI_CD = '6' AND SAME.DI_CD IN ('4', '5', '6') "); // 1日勤怠が欠席なら届出無をカウントしない
            stb.append("                          ) ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO ");
            stb.append("      , SEME.SEMESTER ");
            stb.append("      , CHR.CLASSCD ");
            stb.append("      , CHR.CURRICULUM_CD ");
            stb.append("      , CHR.SCHOOL_KIND ");
            stb.append("      , CHR.SUBCLASSCD ");

            stb.append("), SCH_ATTEND_SUM2 AS(");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        SUM(VALUE(T1.LESSON, 0)) AS LESSON, ");
            stb.append("        SUM(VALUE(T1.MLESSON, 0)) AS MLESSON, ");
            stb.append("        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ");
            stb.append("        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("        SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ");
            stb.append("        SUM(VALUE(T1.VIRUS,0)) AS VIRUS, ");
            stb.append("        SUM(VALUE(T1.KOUDOME,0)) AS KOUDOME, ");
            stb.append("        SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ");
            stb.append("        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ");
            stb.append("        SUM(VALUE(T1.KOUKETSU,0)) AS KOUKETSU, ");
            stb.append("        SUM(VALUE(T1.LATE,0)) AS LATE, ");
            stb.append("        SUM(VALUE(T1.EARLY,0)) AS EARLY, ");
            stb.append("        SUM(VALUE(T1.ABSENT1,0)) AS ABSENT1, ");
            stb.append("        SUM(VALUE(T1.SICK_ONLY,0)) AS SICK_ONLY, ");
            stb.append("        SUM(VALUE(T1.NOTICE_ONLY,0)) AS NOTICE_ONLY, ");
            stb.append("        SUM(VALUE(T1.NONOTICE_ONLY,0)) AS NONOTICE_ONLY, ");
            stb.append("        SUM(VALUE(T1.LATE_EARLY,0)) AS LATE_EARLY ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_SUM1 T1 ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD ");

            stb.append("), SCH_ATTEND_SUM AS(");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        SUM(VALUE(T1.LESSON, 0)) AS LESSON, ");
            stb.append("        SUM(VALUE(T1.MLESSON, 0)) AS MLESSON, ");
            stb.append("        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ");
            stb.append("        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("        SUM(VALUE(T1.SUSPEND, 0)) - SUM(VALUE(SAME.SUSPEND, 0)) AS SUSPEND, ");
            stb.append("        SUM(VALUE(T1.VIRUS,0)) AS VIRUS, ");
            stb.append("        SUM(VALUE(T1.KOUDOME,0)) AS KOUDOME, ");
            stb.append("        SUM(VALUE(T1.MOURNING, 0)) - SUM(VALUE(SAME.MOURNING, 0)) AS MOURNING, ");
            stb.append("        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ");
            stb.append("        SUM(VALUE(T1.KOUKETSU,0)) AS KOUKETSU, ");
            stb.append("        SUM(VALUE(T1.LATE,0)) AS LATE, ");
            stb.append("        SUM(VALUE(T1.EARLY,0)) AS EARLY, ");
            stb.append("        SUM(VALUE(T1.ABSENT1,0)) ");
            stb.append("             - SUM(VALUE(CASE WHEN T2.SUB_SUSPEND = '1' THEN SAME.SUSPEND END, 0)) ");
            stb.append("             - SUM(VALUE(CASE WHEN T2.SUB_MOURNING = '1' THEN SAME.MOURNING END, 0)) ");
            stb.append("             - SUM(VALUE(SAME.SICK_ONLY, 0)) ");
            stb.append("             - SUM(VALUE(SAME.NOTICE_ONLY, 0)) ");
            stb.append("             - SUM(VALUE(SAME.NONOTICE_ONLY, 0)) AS ABSENT1, ");
            stb.append("        SUM(VALUE(T1.SICK_ONLY,0)) - SUM(VALUE(SAME.SICK_ONLY, 0)) AS SICK_ONLY, ");
            stb.append("        SUM(VALUE(T1.NOTICE_ONLY,0)) - SUM(VALUE(SAME.NOTICE_ONLY, 0)) AS NOTICE_ONLY, ");
            stb.append("        SUM(VALUE(T1.NONOTICE_ONLY,0)) - SUM(VALUE(SAME.NONOTICE_ONLY, 0)) AS NONOTICE_ONLY, ");
            stb.append("        SUM(VALUE(T1.LATE_EARLY,0)) AS LATE_EARLY ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_SUM2 T1 ");
            stb.append("        INNER JOIN (SELECT SCHREGNO, MAX(SEMESTER) AS SEMESTER FROM SCHNO GROUP BY SCHREGNO) T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("        INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SEMESTER = T3.SEMESTER ");
            stb.append("        LEFT JOIN  ");
            stb.append("            SAME_DI_CD_ATT SAME ");
            stb.append("                ON SAME.SCHREGNO = T1.SCHREGNO ");
            stb.append("               AND SAME.CLASSCD = T1.CLASSCD ");
            stb.append("               AND SAME.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("               AND SAME.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("               AND SAME.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("               AND SAME.SEMESTER = T1.SEMESTER ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD ");

        } else {
            stb.append("), SCH_ATTEND_SUM AS(");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        SEMESTER, ");
            stb.append("        SUM(VALUE(T1.LESSON, 0)) AS LESSON, ");
            stb.append("        SUM(VALUE(T1.MLESSON, 0)) AS MLESSON, ");
            stb.append("        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ");
            stb.append("        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("        SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ");
            stb.append("        SUM(VALUE(T1.VIRUS,0)) AS VIRUS, ");
            stb.append("        SUM(VALUE(T1.KOUDOME,0)) AS KOUDOME, ");
            stb.append("        SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ");
            stb.append("        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ");
            stb.append("        SUM(VALUE(T1.KOUKETSU,0)) AS KOUKETSU, ");
            stb.append("        SUM(VALUE(T1.LATE,0)) AS LATE, ");
            stb.append("        SUM(VALUE(T1.EARLY,0)) AS EARLY, ");
            stb.append("        SUM(VALUE(T1.ABSENT1,0)) AS ABSENT1, ");
            stb.append("        SUM(VALUE(T1.SICK_ONLY,0)) AS SICK_ONLY, ");
            stb.append("        SUM(VALUE(T1.NOTICE_ONLY,0)) AS NOTICE_ONLY, ");
            stb.append("        SUM(VALUE(T1.NONOTICE_ONLY,0)) AS NONOTICE_ONLY, ");
            stb.append("        SUM(VALUE(T1.LATE_EARLY,0)) AS LATE_EARLY ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_SUM1 T1 ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CURRICULUM_CD, ");
                stb.append("    T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD ");
        }

        stb.append("), ATTEND_SPECIAL AS(");
        stb.append(     "SELECT W1.SCHREGNO, W1.SEMESTER, W1.SUBCLASSCD, W3.SPECIAL_GROUP_CD,");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    W1.CLASSCD, ");
            stb.append("    W1.CURRICULUM_CD, ");
            stb.append("    W1.SCHOOL_KIND, ");
        }
        stb.append("            INT(W2.MINUTES) * W1.LESSON AS LESSON_MINUTES0, ");
        stb.append("            INT(W2.MINUTES) * W1.SUSPEND AS SUSPEND_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * W1.MOURNING AS MOURNING_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * W1.MLESSON AS LESSON_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * W1.ABSENT1 AS ABSENT_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * W1.SICK_ONLY AS SICK_ONLY_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * W1.NOTICE_ONLY AS NOTICE_ONLY_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * W1.NONOTICE_ONLY AS NONOTICE_ONLY_MINUTES, ");
        stb.append("            INT(W2.MINUTES) * (W1.LATE + W1.EARLY) AS LATE_MINUTES ");
        stb.append("     FROM   SCH_ATTEND_SUM W1 ");
        stb.append("     INNER JOIN ATTEND_SUBCLASS_SPECIAL_DAT W2 ON ");
        stb.append("       W2.YEAR = '" + year + "' ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    AND W1.CLASSCD = W2.CLASSCD ");
            stb.append("    AND W1.CURRICULUM_CD = W2.CURRICULUM_CD ");
            stb.append("    AND W1.SCHOOL_KIND = W2.SCHOOL_KIND ");
        }
        stb.append("       AND W1.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append("     INNER JOIN ATTEND_SUBCLASS_SPECIAL_MST W3 ON ");
        stb.append("       W3.SPECIAL_GROUP_CD = W2.SPECIAL_GROUP_CD ");

        stb.append(" ) ,SCH_ATTEND_UNI0 AS ( ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    TT0.CLASSCD, ");
            stb.append("    TT0.CURRICULUM_CD, ");
            stb.append("    TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD, ");
        stb.append("    '0' AS IS_COMBINED_SUBCLASS, ");
        stb.append("    SUM(TT0.LESSON) AS LESSON, ");
        stb.append("    SUM(TT0.MLESSON) AS MLESSON, ");
        stb.append("    SUM(TT0.OFFDAYS) AS OFFDAYS, ");
        stb.append("    SUM(TT0.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(TT0.VIRUS) AS VIRUS, ");
        stb.append("    SUM(TT0.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(TT0.MOURNING) AS MOURNING, ");
        stb.append("    SUM(TT0.NURSEOFF) AS NURSEOFF, ");
        stb.append("    SUM(TT0.KOUKETSU) AS KOUKETSU, ");
        stb.append("    SUM(TT0.LATE) AS LATE, ");
        stb.append("    SUM(TT0.EARLY) AS EARLY, ");
        stb.append("    SUM(TT0.LATE) + SUM(TT0.EARLY) AS LATE_EARLY, ");
        stb.append("    SUM(TT0.ABSENT1) AS ABSENT1, ");
        stb.append("    SUM(TT0.SICK_ONLY) AS SICK_ONLY, ");
        stb.append("    SUM(TT0.NOTICE_ONLY) AS NOTICE_ONLY, ");
        stb.append("    SUM(TT0.NONOTICE_ONLY) AS NONOTICE_ONLY, ");
        stb.append("    L1.SPECIAL_GROUP_CD, ");
        stb.append("    SUM(VALUE(L1.LESSON_MINUTES0, 0)) AS SPECIAL_LESSON_MINUTES0,");
        stb.append("    SUM(VALUE(L1.SUSPEND_MINUTES, 0)) AS SPECIAL_SUSPEND_MINUTES,");
        stb.append("    SUM(VALUE(L1.MOURNING_MINUTES, 0)) AS SPECIAL_MOURNING_MINUTES,");
        stb.append("    SUM(VALUE(L1.LESSON_MINUTES, 0)) AS SPECIAL_LESSON_MINUTES,");
        stb.append("    SUM(VALUE(L1.ABSENT_MINUTES, 0)) AS SPECIAL_ABSENT_MINUTES,");
        stb.append("    SUM(VALUE(L1.SICK_ONLY_MINUTES, 0)) AS SPECIAL_SICK_ONLY_MINUTES,");
        stb.append("    SUM(VALUE(L1.NOTICE_ONLY_MINUTES, 0)) AS SPECIAL_NOTICE_ONLY_MINUTES,");
        stb.append("    SUM(VALUE(L1.NONOTICE_ONLY_MINUTES, 0)) AS SPECIAL_NONOTICE_ONLY_MINUTES,");
        stb.append("    SUM(VALUE(L1.LATE_MINUTES, 0)) AS SPECIAL_LATE_MINUTES");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_SUM TT0 ");
        stb.append("       LEFT JOIN ATTEND_SPECIAL L1 ON TT0.SCHREGNO = L1.SCHREGNO AND TT0.SEMESTER = L1.SEMESTER AND TT0.SUBCLASSCD = L1.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND TT0.CLASSCD = L1.CLASSCD ");
            stb.append("      AND TT0.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("      AND TT0.SCHOOL_KIND = L1.SCHOOL_KIND ");
        }
        stb.append(" GROUP BY ");
        stb.append("    TT0.SCHREGNO, TT0.SEMESTER, TT0.SUBCLASSCD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    TT0.CLASSCD, ");
            stb.append("    TT0.CURRICULUM_CD, ");
            stb.append("    TT0.SCHOOL_KIND, ");
        }
        stb.append("    L1.SPECIAL_GROUP_CD ");
        stb.append("   UNION ALL ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CMB.COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("    CMB.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("    CMB.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
        }
        stb.append("    CMB.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    '1' AS IS_COMBINED_SUBCLASS, ");
        stb.append("    SUM(TT0.LESSON) AS LESSON, ");
        stb.append("    SUM(TT0.MLESSON) AS MLESSON, ");
        stb.append("    SUM(TT0.OFFDAYS) AS OFFDAYS, ");
        stb.append("    SUM(TT0.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(TT0.VIRUS) AS VIRUS, ");
        stb.append("    SUM(TT0.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(TT0.MOURNING) AS MOURNING, ");
        stb.append("    SUM(TT0.NURSEOFF) AS NURSEOFF, ");
        stb.append("    SUM(TT0.KOUKETSU) AS KOUKETSU, ");
        stb.append("    SUM(TT0.LATE) AS LATE, ");
        stb.append("    SUM(TT0.EARLY) AS EARLY, ");
        stb.append("    SUM(TT0.LATE) + SUM(TT0.EARLY) AS LATE_EARLY, ");
        stb.append("    SUM(TT0.ABSENT1) AS ABSENT1, ");
        stb.append("    SUM(TT0.SICK_ONLY) AS SICK_ONLY, ");
        stb.append("    SUM(TT0.NOTICE_ONLY) AS NOTICE_ONLY, ");
        stb.append("    SUM(TT0.NONOTICE_ONLY) AS NONOTICE_ONLY, ");
        stb.append("    MIN(L1.SPECIAL_GROUP_CD) AS SPECIAL_GROUP_CD, ");
        stb.append("    SUM(VALUE(L1.LESSON_MINUTES0, 0)) AS SPECIAL_LESSON_MINUTES0,");
        stb.append("    SUM(VALUE(L1.SUSPEND_MINUTES, 0)) AS SPECIAL_SUSPEND_MINUTES,");
        stb.append("    SUM(VALUE(L1.MOURNING_MINUTES, 0)) AS SPECIAL_MOURNING_MINUTES,");
        stb.append("    SUM(VALUE(L1.LESSON_MINUTES, 0)) AS SPECIAL_LESSON_MINUTES,");
        stb.append("    SUM(VALUE(L1.ABSENT_MINUTES, 0)) AS SPECIAL_ABSENT_MINUTES,");
        stb.append("    SUM(VALUE(L1.SICK_ONLY_MINUTES, 0)) AS SPECIAL_SICK_ONLY_MINUTES,");
        stb.append("    SUM(VALUE(L1.NOTICE_ONLY_MINUTES, 0)) AS SPECIAL_NOTICE_ONLY_MINUTES,");
        stb.append("    SUM(VALUE(L1.NONOTICE_ONLY_MINUTES, 0)) AS SPECIAL_NONOTICE_ONLY_MINUTES,");
        stb.append("    SUM(VALUE(L1.LATE_MINUTES, 0)) AS SPECIAL_LATE_MINUTES");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_SUM TT0 ");
        stb.append("       LEFT JOIN ATTEND_SPECIAL L1 ON TT0.SCHREGNO = L1.SCHREGNO AND TT0.SEMESTER = L1.SEMESTER AND TT0.SUBCLASSCD = L1.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND TT0.CLASSCD = L1.CLASSCD ");
            stb.append("      AND TT0.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("      AND TT0.SCHOOL_KIND = L1.SCHOOL_KIND ");
        }
        stb.append("       INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT CMB ON CMB.YEAR = '" + year + "' ");
        stb.append("          AND CMB.ATTEND_SUBCLASSCD = TT0.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND CMB.ATTEND_CLASSCD = TT0.CLASSCD ");
            stb.append("      AND CMB.ATTEND_CURRICULUM_CD = TT0.CURRICULUM_CD ");
            stb.append("      AND CMB.ATTEND_SCHOOL_KIND = TT0.SCHOOL_KIND ");
        }
        if (isCombinedSubclassNotRequiredCreditMst) {
            stb.append("       LEFT JOIN CREDIT_MST CRM ON CRM.YEAR = '" + year + "' ");
        } else {
            stb.append("       INNER JOIN CREDIT_MST CRM ON CRM.YEAR = '" + year + "' ");
        }
        stb.append("          AND CRM.SUBCLASSCD = TT0.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND CRM.CLASSCD = TT0.CLASSCD ");
            stb.append("      AND CRM.CURRICULUM_CD = TT0.CURRICULUM_CD ");
            stb.append("      AND CRM.SCHOOL_KIND = TT0.SCHOOL_KIND ");
        }
        if (isCombinedSubclassNotRequiredCreditMst) {
            stb.append("       LEFT JOIN SCHNO ON SCHNO.SCHREGNO = TT0.SCHREGNO ");
        } else {
            stb.append("       INNER JOIN SCHNO ON SCHNO.SCHREGNO = TT0.SCHREGNO ");
        }
        stb.append("          AND SCHNO.GRADE = CRM.GRADE ");
        stb.append("          AND SCHNO.COURSECD = CRM.COURSECD ");
        stb.append("          AND SCHNO.MAJORCD = CRM.MAJORCD ");
        stb.append("          AND SCHNO.COURSECODE = CRM.COURSECODE ");
        stb.append("          AND SCHNO.SEMESTER = TT0.SEMESTER ");
        stb.append(" GROUP BY ");
        stb.append("    TT0.SCHREGNO, TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    CMB.COMBINED_CLASSCD, ");
            stb.append("    CMB.COMBINED_CURRICULUM_CD, ");
            stb.append("    CMB.COMBINED_SCHOOL_KIND, ");
        }
        stb.append("    CMB.COMBINED_SUBCLASSCD ");

        stb.append(" ) ,SCH_ATTEND_UNI1 AS ( ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    TT0.CLASSCD, ");
            stb.append("    TT0.CURRICULUM_CD, ");
            stb.append("    TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD, ");
        stb.append("    TT0.IS_COMBINED_SUBCLASS, ");
        stb.append("    TT0.LESSON, ");
        stb.append("    TT0.MLESSON, ");
        stb.append("    TT0.OFFDAYS, ");
        stb.append("    TT0.SUSPEND, ");
        stb.append("    TT0.VIRUS, ");
        stb.append("    TT0.KOUDOME, ");
        stb.append("    TT0.MOURNING, ");
        stb.append("    TT0.NURSEOFF, ");
        stb.append("    TT0.KOUKETSU, ");
        stb.append("    TT0.LATE, ");
        stb.append("    TT0.EARLY, ");
        stb.append("    TT0.LATE_EARLY, ");
        stb.append("    TT0.ABSENT1, ");
        stb.append("    TT0.SICK_ONLY, ");
        stb.append("    TT0.NOTICE_ONLY, ");
        stb.append("    TT0.NONOTICE_ONLY, ");
        stb.append("    TT0.SPECIAL_GROUP_CD, ");
        stb.append("    TT0.SPECIAL_LESSON_MINUTES0,");
        stb.append("    TT0.SPECIAL_SUSPEND_MINUTES, ");
        stb.append("    TT0.SPECIAL_MOURNING_MINUTES, ");
        stb.append("    TT0.SPECIAL_LESSON_MINUTES,");
        stb.append("    TT0.SPECIAL_ABSENT_MINUTES,");
        stb.append("    TT0.SPECIAL_SICK_ONLY_MINUTES,");
        stb.append("    TT0.SPECIAL_NOTICE_ONLY_MINUTES,");
        stb.append("    TT0.SPECIAL_NONOTICE_ONLY_MINUTES,");
        stb.append("    TT0.SPECIAL_LATE_MINUTES");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_UNI0 TT0 ");
        if (null != subclasscd || null != subclasscdArray) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if (null != subclasscdArray) {
                stb.append("    TT0.SUBCLASSCD IN " + whereIn(true, subclasscdArray) + " ");
            } else if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append("   UNION ALL ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    '9' AS SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    TT0.CLASSCD, ");
            stb.append("    TT0.CURRICULUM_CD, ");
            stb.append("    TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD, ");
        stb.append("    MAX(TT0.IS_COMBINED_SUBCLASS) AS IS_COMBINED_SUBCLASS, ");
        stb.append("    SUM(TT0.LESSON) AS LESSON, ");
        stb.append("    SUM(TT0.MLESSON) AS MLESSON, ");
        stb.append("    SUM(TT0.OFFDAYS) AS OFFDAYS, ");
        stb.append("    SUM(TT0.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(TT0.VIRUS) AS VIRUS, ");
        stb.append("    SUM(TT0.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(TT0.MOURNING) AS MOURNING, ");
        stb.append("    SUM(TT0.NURSEOFF) AS NURSEOFF, ");
        stb.append("    SUM(TT0.KOUKETSU) AS KOUKETSU, ");
        stb.append("    SUM(TT0.LATE) AS LATE, ");
        stb.append("    SUM(TT0.EARLY) AS EARLY, ");
        stb.append("    SUM(TT0.LATE_EARLY) AS LATE_EARLY, ");
        stb.append("    SUM(TT0.ABSENT1) AS ABSENT1, ");
        stb.append("    SUM(TT0.SICK_ONLY) AS SICK_ONLY, ");
        stb.append("    SUM(TT0.NOTICE_ONLY) AS NOTICE_ONLY, ");
        stb.append("    SUM(TT0.NONOTICE_ONLY) AS NONOTICE_ONLY, ");
        stb.append("    MIN(TT0.SPECIAL_GROUP_CD) AS SPECIAL_GROUP_CD, ");
        stb.append("    SUM(TT0.SPECIAL_LESSON_MINUTES0) AS SPECIAL_LESSON_MINUTES0,");
        stb.append("    SUM(TT0.SPECIAL_SUSPEND_MINUTES) AS SPECIAL_SUSPEND_MINUTES, ");
        stb.append("    SUM(TT0.SPECIAL_MOURNING_MINUTES) AS SPECIAL_MOURNING_MINUTES, ");
        stb.append("    SUM(TT0.SPECIAL_LESSON_MINUTES) AS SPECIAL_LESSON_MINUTES,");
        stb.append("    SUM(TT0.SPECIAL_ABSENT_MINUTES) AS SPECIAL_ABSENT_MINUTES,");
        stb.append("    SUM(TT0.SPECIAL_SICK_ONLY_MINUTES) AS SPECIAL_SICK_ONLY_MINUTES,");
        stb.append("    SUM(TT0.SPECIAL_NOTICE_ONLY_MINUTES) AS SPECIAL_NOTICE_ONLY_MINUTES,");
        stb.append("    SUM(TT0.SPECIAL_NONOTICE_ONLY_MINUTES) AS SPECIAL_NONOTICE_ONLY_MINUTES,");
        stb.append("    SUM(TT0.SPECIAL_LATE_MINUTES) AS SPECIAL_LATE_MINUTES");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_UNI0 TT0 ");
        if (null != subclasscd || null != subclasscdArray) {
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    TT0.CLASSCD || '-' || TT0.SCHOOL_KIND || '-' || TT0.CURRICULUM_CD || '-' || ");
            }
            if (null != subclasscdArray) {
                stb.append("    TT0.SUBCLASSCD IN " + whereIn(true, subclasscdArray) + " ");
            } else if ("?".equals(subclasscd)) {
                stb.append("    TT0.SUBCLASSCD = ? ");
            } else {
                stb.append("    TT0.SUBCLASSCD = '" + subclasscd + "' ");
            }
        }
        stb.append(" GROUP BY ");
        stb.append("    TT0.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    TT0.CLASSCD, ");
            stb.append("    TT0.CURRICULUM_CD, ");
            stb.append("    TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD ");
        stb.append(" ) ");

        stb.append(" , SCH_ATTEND_UNI AS ( ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("    TT0.CLASSCD, ");
            stb.append("    TT0.CURRICULUM_CD, ");
            stb.append("    TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD, ");
        stb.append("    TT0.IS_COMBINED_SUBCLASS, ");
        stb.append("    TT0.LESSON, ");
        stb.append("    TT0.MLESSON, ");
        stb.append("    TT0.OFFDAYS, ");
        stb.append("    TT0.SUSPEND, ");
        stb.append("    TT0.VIRUS, ");
        stb.append("    TT0.KOUDOME, ");
        stb.append("    TT0.MOURNING, ");
        stb.append("    TT0.NURSEOFF, ");
        stb.append("    TT0.KOUKETSU, ");
        stb.append("    TT0.LATE, ");
        stb.append("    TT0.EARLY, ");
        stb.append("    TT0.LATE_EARLY, ");
        stb.append("    TT0.ABSENT1, ");
        stb.append("    TT0.SICK_ONLY, ");
        stb.append("    TT0.NOTICE_ONLY, ");
        stb.append("    TT0.NONOTICE_ONLY, ");
        stb.append("    TT0.SPECIAL_GROUP_CD, ");
        stb.append("    TT0.SPECIAL_LESSON_MINUTES0,");
        stb.append("    TT0.SPECIAL_SUSPEND_MINUTES, ");
        stb.append("    TT0.SPECIAL_MOURNING_MINUTES, ");
        stb.append("    TT0.SPECIAL_LESSON_MINUTES,");
        stb.append("    TT0.SPECIAL_ABSENT_MINUTES,");
        stb.append("    TT0.SPECIAL_SICK_ONLY_MINUTES,");
        stb.append("    TT0.SPECIAL_NOTICE_ONLY_MINUTES,");
        stb.append("    TT0.SPECIAL_NONOTICE_ONLY_MINUTES,");
        stb.append("    TT0.SPECIAL_LATE_MINUTES");
        stb.append("  , T2.ABSENT_COV ");
        stb.append("  , T2.ABSENT_COV_LATE ");
        stb.append("  , T2.AMARI_KURIAGE ");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_UNI1 TT0 ");
        stb.append("    INNER JOIN (SELECT SCHREGNO, MAX(SEMESTER) AS SEMESTER FROM SCHNO GROUP BY SCHREGNO) T3 ON T3.SCHREGNO = TT0.SCHREGNO ");
        stb.append("    INNER JOIN SCHNO T2 ON T2.SCHREGNO = TT0.SCHREGNO AND T2.SEMESTER = T3.SEMESTER ");
        stb.append(" ) ");

        stb.append(", ATTEND_B0 AS(");
        stb.append("     SELECT ");
        stb.append("          SCHREGNO, ");
        stb.append("          ABSENT_COV, ");
        stb.append("          ABSENT_COV_LATE, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
        }
        stb.append("          SUBCLASSCD, ");
        stb.append("          SEMESTER, ");
        stb.append("          CASE WHEN T1.ABSENT_COV_LATE > 0 THEN VALUE(SUM(LATE_EARLY),0) / T1.ABSENT_COV_LATE END AS ABSENT, ");
        stb.append("          CASE WHEN T1.ABSENT_COV_LATE > 0 THEN DECIMAL(VALUE(SUM(LATE_EARLY),0), 4, 1) / T1.ABSENT_COV_LATE END AS ABSENT_FLOAT ");
        stb.append("      FROM ");
        stb.append("          SCH_ATTEND_UNI T1 ");
        stb.append("      WHERE ");
        stb.append("          SEMESTER <> '9' ");
        stb.append("          AND VALUE(T1.ABSENT_COV_LATE, 0) <> 0 ");
        stb.append("      GROUP BY ");
        stb.append("          SCHREGNO, ");
        stb.append("          ABSENT_COV, ");
        stb.append("          ABSENT_COV_LATE, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
        }
        stb.append("          SUBCLASSCD, ");
        stb.append("          SEMESTER ");
        stb.append(" ) ");
        stb.append(", ATTEND_B AS(");
        //if ("1".equals(knjSchoolMst._absentCov) || "3".equals(knjSchoolMst._absentCov)) {
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        ABSENT_COV, ");
            stb.append("        ABSENT_COV_LATE, ");
            stb.append("        SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD, ");
            stb.append("        CASE WHEN ABSENT_COV = '3' THEN SUM(ABSENT_FLOAT) ELSE SUM(ABSENT) END AS PENALTY_ABSENT ");
            stb.append("    FROM ATTEND_B0 T1 ");
            stb.append("    WHERE ");
            stb.append("        (ABSENT_COV = '1' OR ABSENT_COV = '3') ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            stb.append("        ABSENT_COV, ");
            stb.append("        ABSENT_COV_LATE, ");
            stb.append("        SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD ");
            stb.append(" UNION ALL ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        ABSENT_COV, ");
            stb.append("        ABSENT_COV_LATE, ");
            stb.append("        '9' AS SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD, ");
            stb.append("        CASE WHEN ABSENT_COV = '3' THEN SUM(ABSENT_FLOAT) ELSE SUM(ABSENT) END AS PENALTY_ABSENT ");
            stb.append("    FROM ATTEND_B0 T1 ");
            stb.append("    WHERE ");
            stb.append("        (ABSENT_COV = '1' OR ABSENT_COV = '3') ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            stb.append("        ABSENT_COV, ");
            stb.append("        ABSENT_COV_LATE, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
            }
            stb.append("        SUBCLASSCD ");
        //} else if ("2".equals(knjSchoolMst._absentCov) || "4".equals(knjSchoolMst._absentCov)) {
            stb.append("    UNION ALL ");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, ");
                stb.append("      T1.CURRICULUM_CD, ");
                stb.append("      T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T2.ABSENT AS PENALTY_ABSENT ");
            stb.append("    FROM ");
            stb.append("        (SELECT ");
            stb.append("             T1.SCHREGNO, ");
            stb.append("             T1.ABSENT_COV, ");
            stb.append("             T1.ABSENT_COV_LATE, ");
            stb.append("             T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("             T1.SUBCLASSCD ");
            stb.append("         FROM ");
            stb.append("             SCH_ATTEND_UNI T1 ");
            stb.append("         WHERE ");
            stb.append("             T1.SEMESTER <> '9' ");
            stb.append("         GROUP BY ");
            stb.append("             T1.SCHREGNO, ");
            stb.append("             T1.ABSENT_COV, ");
            stb.append("             T1.ABSENT_COV_LATE, ");
            stb.append("             T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("             T1.SUBCLASSCD ");
            stb.append("        )T1, ");
            stb.append("        (");
            stb.append("              SELECT ");
            stb.append("                  T1.SCHREGNO, ");
            stb.append("                  T1.ABSENT_COV, ");
            stb.append("                  T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("                  T1.SUBCLASSCD, ");
            stb.append("                  CASE WHEN ABSENT_COV = '4' THEN DECIMAL(VALUE(SUM(LATE_EARLY),0), 4, 1) / T1.ABSENT_COV_LATE ELSE VALUE(SUM(LATE_EARLY),0) / T1.ABSENT_COV_LATE END AS ABSENT ");
            stb.append("              FROM ");
            stb.append("                  SCH_ATTEND_UNI T1 ");
            stb.append("              WHERE ");
            stb.append("                  T1.SEMESTER <> '9' ");
            stb.append("                  AND (T1.ABSENT_COV = '2' OR T1.ABSENT_COV = '4') ");
            stb.append("                  AND VALUE(T1.ABSENT_COV_LATE, 0) <> 0 ");
            stb.append("              GROUP BY ");
            stb.append("                  T1.SCHREGNO, ");
            stb.append("                  T1.ABSENT_COV, ");
            stb.append("                  T1.ABSENT_COV_LATE, ");
            stb.append("                  T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("                  T1.SUBCLASSCD ");
            stb.append("        ) T2 ");
            stb.append("    WHERE ");
            stb.append("        T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("        AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            }
            stb.append(" UNION ALL ");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            stb.append("        '9' AS SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        CASE WHEN ABSENT_COV = '4' THEN DECIMAL(VALUE(SUM(LATE_EARLY),0), 4, 1) / T1.ABSENT_COV_LATE ELSE VALUE(SUM(LATE_EARLY),0) / T1.ABSENT_COV_LATE END AS PENALTY_ABSENT ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_UNI T1 ");
            stb.append("    WHERE ");
            stb.append("        SEMESTER <> '9' ");
            stb.append("        AND (ABSENT_COV = '2' OR ABSENT_COV = '4') ");
            stb.append("        AND VALUE(T1.ABSENT_COV_LATE, 0) <> 0 ");
            stb.append("    GROUP BY  ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      CLASSCD, CURRICULUM_CD, SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD ");
            //} else if ("5".equals(knjSchoolMst._absentCov)) {
            stb.append(" UNION ALL ");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T2.ABSENT AS PENALTY_ABSENT ");
            stb.append("    FROM ");
            stb.append("        (SELECT ");
            stb.append("             SCHREGNO, ");
            stb.append("             ABSENT_COV, ");
            stb.append("             ABSENT_COV_LATE, ");
            stb.append("             SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("             SUBCLASSCD ");
            stb.append("         FROM ");
            stb.append("             SCH_ATTEND_UNI T1 ");
            stb.append("         GROUP BY ");
            stb.append("             SCHREGNO, ");
            stb.append("             ABSENT_COV, ");
            stb.append("             ABSENT_COV_LATE, ");
            stb.append("             SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("             SUBCLASSCD ");
            stb.append("        )T1, ");
            stb.append("        (");
            stb.append("              SELECT ");
            stb.append("                  SCHREGNO, ");
            stb.append("                  T1.AMARI_KURIAGE, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("                  SUBCLASSCD, ");
            stb.append("                  SEMESTER, ");
            stb.append("                  VALUE(SUM(LATE_EARLY),0) / ABSENT_COV_LATE ");
            stb.append("                   +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , ABSENT_COV_LATE) >= INT(VALUE(T1.AMARI_KURIAGE, '999')) THEN 1 ELSE 0 END) AS ABSENT ");
            stb.append("              FROM ");
            stb.append("                  SCH_ATTEND_UNI T1 ");
            stb.append("              WHERE ");
            stb.append("                  SEMESTER <> '9' ");
            stb.append("                  AND ABSENT_COV = '5' ");
            stb.append("                  AND VALUE(T1.ABSENT_COV_LATE, 0) <> 0 ");
            stb.append("              GROUP BY ");
            stb.append("                  SCHREGNO, ");
            stb.append("                  T1.ABSENT_COV_LATE, ");
            stb.append("                  T1.AMARI_KURIAGE, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("                  SEMESTER, ");
            stb.append("                  SUBCLASSCD ");
            stb.append("        ) T2 ");
            stb.append("    WHERE ");
            stb.append("        T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("        AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            }
            stb.append(" UNION ALL ");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            stb.append("        '9' AS SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        VALUE(SUM(LATE_EARLY),0) / ABSENT_COV_LATE ");
            stb.append("          +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , ABSENT_COV_LATE) >= INT(VALUE(T1.AMARI_KURIAGE, '999')) THEN 1 ELSE 0 END) AS PENALTY_ABSENT ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_UNI T1 ");
            stb.append("    WHERE ");
            stb.append("        SEMESTER <> '9' ");
            stb.append("        AND T1.ABSENT_COV = '5' ");
            stb.append("        AND VALUE(T1.ABSENT_COV_LATE, 0) <> 0 ");
            stb.append("    GROUP BY  ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            stb.append("        T1.AMARI_KURIAGE, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD ");
            stb.append(" UNION ALL ");
        //} else {
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        0 AS PENALTY_ABSENT ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_UNI T1 ");
            stb.append("    WHERE ");
            stb.append("        VALUE(ABSENT_COV, '0') = '0' ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            stb.append("        T1.ABSENT_COV, ");
            stb.append("        T1.ABSENT_COV_LATE, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("      T1.CLASSCD, T1.CURRICULUM_CD, T1.SCHOOL_KIND, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T1.SEMESTER ");
        //}

        stb.append(" ), ATTEND_C AS(");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      T1.CLASSCD, ");
            stb.append("      T1.CURRICULUM_CD, ");
            stb.append("      T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.SEMESTER, ");
        stb.append("        T1.ABSENT_COV, ");
        stb.append("        T1.ABSENT_COV_LATE, ");
        stb.append("        SUM(PENALTY_ABSENT) AS PENALTY_ABSENT, ");
        stb.append("        SUM(PENALTY_ABSENT) * VALUE(ABSENT_COV_LATE, 0) AS PENALTY_LATE_EARLY, ");
        stb.append("        SUM(INT(VALUE(T2.MINUTES, '0')) * INT(PENALTY_ABSENT)) AS PENALTY_SPECIAL_ABSENT_MINUTES ");
        stb.append("    FROM ");
        stb.append("        ATTEND_B T1 ");
        stb.append("        LEFT JOIN ATTEND_SUBCLASS_SPECIAL_DAT T2 ON T2.YEAR = '" + year +"' ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("            AND T1.CLASSCD = T2.CLASSCD ");
            stb.append("            AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("            AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("            AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("    GROUP BY ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.ABSENT_COV_LATE, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      T1.CLASSCD, ");
            stb.append("      T1.CURRICULUM_CD, ");
            stb.append("      T1.SCHOOL_KIND, ");
        }
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.SEMESTER, ");
        stb.append("        T1.ABSENT_COV, ");
        stb.append("        T1.ABSENT_COV_LATE ");

        stb.append(" ) ,ATTEND_REPLACE AS ( ");
        stb.append(" SELECT ");
        stb.append("    TT2.SCHREGNO, TT2.SEMESTER, TT2.SUBCLASSCD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      TT2.CLASSCD, ");
            stb.append("      TT2.CURRICULUM_CD, ");
            stb.append("      TT2.SCHOOL_KIND, ");
        }
        stb.append("    TT2.LESSON AS LESSON, ");
        stb.append("    TT2.MLESSON AS MLESSON, ");
        stb.append("    TT2.OFFDAYS AS OFFDAYS, ");
        stb.append("    TT2.SUSPEND AS SUSPEND, ");
        stb.append("    TT2.VIRUS AS VIRUS, ");
        stb.append("    TT2.KOUDOME AS KOUDOME, ");
        stb.append("    TT2.MOURNING AS MOURNING, ");
        stb.append("    TT2.NURSEOFF AS NURSEOFF, ");
        stb.append("    TT2.KOUKETSU AS KOUKETSU, ");
        stb.append("    TT2.LATE AS LATE, ");
        stb.append("    TT2.EARLY AS EARLY, ");
        stb.append("    TT2.ABSENT1 AS RAW_REPLACED_ABSENT, ");
        stb.append("    TT2.SICK_ONLY AS RAW_REPLACED_SICK_ONLY, ");
        stb.append("    TT2.NOTICE_ONLY AS RAW_REPLACED_NOTICE_ONLY, ");
        stb.append("    TT2.NONOTICE_ONLY AS RAW_REPLACED_NONOTICE_ONLY, ");
        stb.append("    TT2.ABSENT1+ VALUE(TT3.PENALTY_ABSENT, 0) AS REPLACED_ABSENT, ");
        stb.append("    TT2.ABSENT_COV, ");
        stb.append("    TT2.ABSENT_COV_LATE ");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_UNI TT2 ");
        stb.append("    LEFT JOIN ATTEND_B TT3 ON ");
        stb.append("       TT2.SCHREGNO = TT3.SCHREGNO ");
        stb.append("       AND TT2.SEMESTER = TT3.SEMESTER ");
        stb.append("       AND TT2.SUBCLASSCD= TT3.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("   AND TT2.CLASSCD = TT3.CLASSCD ");
            stb.append("   AND TT2.CURRICULUM_CD = TT3.CURRICULUM_CD ");
            stb.append("   AND TT2.SCHOOL_KIND = TT3.SCHOOL_KIND ");
        }
        stb.append(" WHERE ");
        stb.append("    TT2.IS_COMBINED_SUBCLASS = '1' ");
        stb.append(" ), MAIN AS (   ");

        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      TT0.CLASSCD, ");
            stb.append("      TT0.CURRICULUM_CD, ");
            stb.append("      TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD, ");
        stb.append("    '0' AS IS_COMBINED_SUBCLASS, ");
        stb.append("    TT0.LESSON, ");
        stb.append("    TT0.MLESSON, ");
        stb.append("    TT0.OFFDAYS, ");
        stb.append("    TT0.SUSPEND, ");
        stb.append("    TT0.VIRUS, ");
        stb.append("    TT0.KOUDOME, ");
        stb.append("    TT0.MOURNING, ");
        stb.append("    TT0.NURSEOFF, ");
        stb.append("    TT0.KOUKETSU AS ABSENT, ");
        stb.append("    TT0.LATE, ");
        stb.append("    TT0.EARLY, ");
        stb.append("    VALUE(TT1.PENALTY_LATE_EARLY, 0) AS PENALTY_LATE_EARLY, ");
        stb.append("    (CASE WHEN TT0.LATE < VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ");
        stb.append("          ELSE TT0.LATE - VALUE(TT1.PENALTY_LATE_EARLY, 0) ");
        stb.append("     END) AS LATE2, ");
        stb.append("    (CASE WHEN VALUE(TT1.PENALTY_LATE_EARLY, 0) = 0 THEN TT0.EARLY ");
        stb.append("          WHEN TT0.LATE + TT0.EARLY <= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ");
        stb.append("          ELSE TT0.LATE + TT0.EARLY - VALUE(TT1.PENALTY_LATE_EARLY, 0) ");
        stb.append("                         - (CASE WHEN TT0.LATE < VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ");
        stb.append("                                 ELSE TT0.LATE - VALUE(TT1.PENALTY_LATE_EARLY, 0) END) ");
        stb.append("     END) AS EARLY2, ");
        stb.append("    TT0.ABSENT1 AS SICK1, ");
        stb.append("    TT0.ABSENT1 + VALUE(TT1.PENALTY_ABSENT, 0) AS SICK2, ");
        stb.append("    TT0.SICK_ONLY, ");
        stb.append("    TT0.NOTICE_ONLY, ");
        stb.append("    TT0.NONOTICE_ONLY, ");
        stb.append("    0 AS RAW_REPLACED_SICK, ");
        stb.append("    0 AS REPLACED_SICK, ");
        stb.append("    TT0.SPECIAL_GROUP_CD, ");
        stb.append("    TT0.SPECIAL_LESSON_MINUTES0, ");
        stb.append("    TT0.SPECIAL_SUSPEND_MINUTES, ");
        stb.append("    TT0.SPECIAL_MOURNING_MINUTES, ");
        stb.append("    TT0.SPECIAL_LESSON_MINUTES, ");
        stb.append("    TT0.SPECIAL_ABSENT_MINUTES + VALUE(TT1.PENALTY_SPECIAL_ABSENT_MINUTES,0) AS SPECIAL_SICK_MINUTES1, ");
        stb.append("    TT0.SPECIAL_ABSENT_MINUTES AS SPECIAL_SICK_MINUTES2, ");
        stb.append("    TT0.SPECIAL_ABSENT_MINUTES + TT0.SPECIAL_LATE_MINUTES AS SPECIAL_SICK_MINUTES3, ");
        stb.append("    TT0.ABSENT_COV, ");
        stb.append("    TT0.ABSENT_COV_LATE ");
        stb.append(" FROM ");
        stb.append("    SCH_ATTEND_UNI TT0 ");
        stb.append("    LEFT JOIN ATTEND_C TT1 ON ");
        stb.append("       TT0.SCHREGNO = TT1.SCHREGNO ");
        stb.append("       AND TT0.SEMESTER = TT1.SEMESTER ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("   AND TT0.CLASSCD = TT1.CLASSCD ");
            stb.append("   AND TT0.CURRICULUM_CD = TT1.CURRICULUM_CD ");
            stb.append("   AND TT0.SCHOOL_KIND = TT1.SCHOOL_KIND ");
        }
        stb.append("       AND TT0.SUBCLASSCD = TT1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("    IS_COMBINED_SUBCLASS = '0' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      TT0.CLASSCD, ");
            stb.append("      TT0.CURRICULUM_CD, ");
            stb.append("      TT0.SCHOOL_KIND, ");
        }
        stb.append("    TT0.SUBCLASSCD, ");
        stb.append("    '1' AS IS_COMBINED_SUBCLASS, ");
        stb.append("    TT0.LESSON AS LESSON, ");
        stb.append("    TT0.MLESSON AS MLESSON, ");
        stb.append("    TT0.OFFDAYS AS OFFDAYS, ");
        stb.append("    TT0.SUSPEND AS SUSPEND, ");
        stb.append("    TT0.VIRUS AS VIRUS, ");
        stb.append("    TT0.KOUDOME AS KOUDOME, ");
        stb.append("    TT0.MOURNING AS MOURNING, ");
        stb.append("    TT0.NURSEOFF AS NURSEOFF, ");
        stb.append("    TT0.KOUKETSU AS ABSENT, ");
        stb.append("    TT0.LATE AS LATE, ");
        stb.append("    TT0.EARLY AS EARLY, ");
        stb.append("    0 AS PENALTY_LATE_EARLY, ");
        stb.append("    TT0.LATE AS LATE2, ");
        stb.append("    TT0.EARLY AS EARLY2, ");
        stb.append("    0 AS SICK1, ");
        stb.append("    0 AS SICK2, ");
        stb.append("    TT0.RAW_REPLACED_SICK_ONLY AS SICK_ONLY, ");
        stb.append("    TT0.RAW_REPLACED_NOTICE_ONLY AS NOTICE_ONLY, ");
        stb.append("    TT0.RAW_REPLACED_NONOTICE_ONLY AS NONOTICE_ONLY, ");
        stb.append("    TT0.RAW_REPLACED_ABSENT AS RAW_REPLACED_SICK, ");
        stb.append("    TT0.REPLACED_ABSENT AS REPLACED_SICK, ");
        stb.append("    CAST(NULL AS VARCHAR(4)) AS SPECIAL_GROUP_CD, ");
        stb.append("    0 AS SPECIAL_LESSON_MINUTES0, ");
        stb.append("    0 AS SPECIAL_SUSPEND_MINUTES, ");
        stb.append("    0 AS SPECIAL_MOURNING_MINUTES, ");
        stb.append("    0 AS SPECIAL_LESSON_MINUTES, ");
        stb.append("    0 AS SPECIAL_SICK_MINUTES1, ");
        stb.append("    0 AS SPECIAL_SICK_MINUTES2, ");
        stb.append("    0 AS SPECIAL_SICK_MINUTES3, ");
        stb.append("    TT0.ABSENT_COV, ");
        stb.append("    TT0.ABSENT_COV_LATE ");
        stb.append(" FROM ");
        stb.append("    ATTEND_REPLACE TT0 ");
        stb.append(" ) ");


        stb.append(" , ABSENCE_HIGH AS (   ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      TT1.CLASSCD, ");
            stb.append("      TT1.SCHOOL_KIND, ");
            stb.append("      TT1.CURRICULUM_CD, ");
        }
        stb.append("    TT1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    TT1.ABSENCE_HIGH AS ABSENCE_HIGH, ");
        stb.append("    TT1.GET_ABSENCE_HIGH, ");
        stb.append("    CREM.ABSENCE_WARN, ");
        stb.append("    CREM.ABSENCE_WARN2, ");
        stb.append("    CREM.ABSENCE_WARN3, ");
        stb.append("    CREM.CREDITS ");
        stb.append(" FROM ");
        stb.append("    (SELECT SCHREGNO, SEMESTER, GRADE, COURSECD, MAJORCD, COURSECODE ");
        stb.append("     FROM SCHNO ");
        stb.append("     WHERE JUGYOU_JISU_FLG = '1' ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT SCHREGNO, '9' AS SEMESTER, GRADE, COURSECD, MAJORCD, COURSECODE ");
        stb.append("     FROM SCHNO ");
        stb.append("     WHERE JUGYOU_JISU_FLG = '1' ");
        stb.append("       AND (SCHREGNO, SEMESTER) IN ");
        stb.append("           (SELECT SCHREGNO, MAX(SEMESTER) ");
        stb.append("            FROM SCHNO ");
        stb.append("            GROUP BY SCHREGNO) ");
        stb.append("    )TT0 ");
        stb.append("    INNER JOIN CREDIT_MST TT1 ON TT1.YEAR = '" + year + "' ");
        stb.append("       AND TT0.COURSECD = TT1.COURSECD ");
        stb.append("       AND TT0.MAJORCD = TT1.MAJORCD ");
        stb.append("       AND TT0.GRADE = TT1.GRADE ");
        stb.append("       AND TT0.COURSECODE = TT1.COURSECODE ");
        stb.append("    LEFT JOIN CREDIT_MST CREM ON TT1.YEAR = CREM.YEAR ");
        stb.append("       AND TT0.COURSECD = CREM.COURSECD ");
        stb.append("       AND TT0.MAJORCD = CREM.MAJORCD ");
        stb.append("       AND TT0.GRADE = CREM.GRADE ");
        stb.append("       AND TT0.COURSECODE = CREM.COURSECODE ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND TT1.CLASSCD = CREM.CLASSCD ");
            stb.append("      AND TT1.CURRICULUM_CD = CREM.CURRICULUM_CD ");
            stb.append("      AND TT1.SCHOOL_KIND = CREM.SCHOOL_KIND ");
        }
        stb.append("      AND TT1.SUBCLASSCD = CREM.SUBCLASSCD ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("    TT0.SCHREGNO, ");
        stb.append("    TT0.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      TT1.CLASSCD, ");
            stb.append("      TT1.SCHOOL_KIND, ");
            stb.append("      TT1.CURRICULUM_CD, ");
        }
        stb.append("    TT1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    TT1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH, ");
        stb.append("    TT1.GET_ABSENCE_HIGH, ");
        stb.append("    CREM.ABSENCE_WARN, ");
        stb.append("    CREM.ABSENCE_WARN2, ");
        stb.append("    CREM.ABSENCE_WARN3, ");
        stb.append("    CREM.CREDITS ");
        stb.append(" FROM ");
        stb.append("    (SELECT SCHREGNO, SEMESTER, GRADE, COURSECD, MAJORCD, COURSECODE ");
        stb.append("     FROM SCHNO ");
        stb.append("     WHERE JUGYOU_JISU_FLG = '2' ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT SCHREGNO, '9' AS SEMESTER, GRADE, COURSECD, MAJORCD, COURSECODE ");
        stb.append("     FROM SCHNO ");
        stb.append("     WHERE JUGYOU_JISU_FLG = '2' ");
        stb.append("       AND (SCHREGNO, SEMESTER) IN ");
        stb.append("           (SELECT SCHREGNO, MAX(SEMESTER) ");
        stb.append("            FROM SCHNO ");
        stb.append("            GROUP BY SCHREGNO) ");
        stb.append("    )TT0 ");
        stb.append("    INNER JOIN SCHREG_ABSENCE_HIGH_DAT TT1 ON TT1.YEAR = '" + year + "' ");
        stb.append("       AND TT1.DIV = '" + absenceDiv + "' ");
        stb.append("       AND TT1.SCHREGNO = TT0.SCHREGNO ");
        stb.append("    LEFT JOIN CREDIT_MST CREM ON TT1.YEAR = CREM.YEAR ");
        stb.append("       AND TT0.COURSECD = CREM.COURSECD ");
        stb.append("       AND TT0.MAJORCD = CREM.MAJORCD ");
        stb.append("       AND TT0.GRADE = CREM.GRADE ");
        stb.append("       AND TT0.COURSECODE = CREM.COURSECODE ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND TT1.CLASSCD = CREM.CLASSCD ");
            stb.append("      AND TT1.CURRICULUM_CD = CREM.CURRICULUM_CD ");
            stb.append("      AND TT1.SCHOOL_KIND = CREM.SCHOOL_KIND ");
        }
        stb.append("      AND TT1.SUBCLASSCD = CREM.SUBCLASSCD ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    T1.IS_COMBINED_SUBCLASS, ");
        stb.append("    T1.LESSON, ");
        stb.append("    T1.MLESSON, ");
        stb.append("    T1.OFFDAYS, ");
        stb.append("    T1.SUSPEND, ");
        stb.append("    T1.VIRUS, ");
        stb.append("    T1.KOUDOME, ");
        stb.append("    T1.MOURNING, ");
        stb.append("    T1.NURSEOFF, ");
        stb.append("    T1.ABSENT, ");
        stb.append("    T1.LATE, ");
        stb.append("    T1.EARLY, ");
        if ("USE_STUDENT_ABSENT_COV".equals(useAbsentCov)) {
            stb.append("    TRIM(CAST(T1.PENALTY_LATE_EARLY AS VARCHAR(10))) AS PENALTY_LATE_EARLY, ");
            stb.append("    TRIM(CAST(T1.LATE2 AS VARCHAR(10))) AS LATE2, ");
            stb.append("    TRIM(CAST(T1.EARLY2 AS VARCHAR(10) )) AS EARLY2, ");
        } else if ("3".equals(useAbsentCov) || "4".equals(useAbsentCov)) {
            stb.append("    DECIMAL(ROUND(T1.PENALTY_LATE_EARLY, 1), 4, 1) AS PENALTY_LATE_EARLY, ");
            stb.append("    DECIMAL(ROUND(T1.LATE2, 1), 4, 1) AS LATE2, ");
            stb.append("    DECIMAL(ROUND(T1.EARLY2, 1), 4, 1) AS EARLY2, ");
        } else {
            stb.append("    DECIMAL(ROUND(T1.PENALTY_LATE_EARLY, 0), 4, 0) AS PENALTY_LATE_EARLY, ");
            stb.append("    DECIMAL(ROUND(T1.LATE2, 0), 4, 0) AS LATE2, ");
            stb.append("    DECIMAL(ROUND(T1.EARLY2, 0), 4, 0) AS EARLY2, ");
        }
        stb.append("    T1.ABSENT_COV, ");
        stb.append("    T1.SICK1, ");
        stb.append("    T1.RAW_REPLACED_SICK, ");
        if ("USE_STUDENT_ABSENT_COV".equals(useAbsentCov)) {
            stb.append("    CASE WHEN VALUE(ABSENT_COV, '0') IN ('3', '4') THEN TRIM(CAST( DECIMAL(ROUND(T1.SICK2        , 1), 4, 1) AS VARCHAR(10))) ELSE TRIM(CAST( DECIMAL(ROUND(T1.SICK2        , 0), 4, 0) AS VARCHAR(10))) END AS SICK2, ");
            stb.append("    CASE WHEN VALUE(ABSENT_COV, '0') IN ('3', '4') THEN TRIM(CAST( DECIMAL(ROUND(T1.REPLACED_SICK, 1), 4, 1) AS VARCHAR(10))) ELSE TRIM(CAST( DECIMAL(ROUND(T1.REPLACED_SICK, 0), 4, 0) AS VARCHAR(10))) END AS REPLACED_SICK, ");
        } else if ("3".equals(useAbsentCov) || "4".equals(useAbsentCov)) {
            stb.append("    DECIMAL(ROUND(T1.SICK2, 1), 4, 1) AS SICK2, ");
            stb.append("    DECIMAL(ROUND(T1.REPLACED_SICK, 1), 4, 1) AS REPLACED_SICK, ");
        } else {
            stb.append("    DECIMAL(ROUND(T1.SICK2, 0), 4, 0) AS SICK2, ");
            stb.append("    DECIMAL(ROUND(T1.REPLACED_SICK, 0), 4, 0) AS REPLACED_SICK, ");
        }
        stb.append("    T1.SICK_ONLY, ");
        stb.append("    T1.NOTICE_ONLY, ");
        stb.append("    T1.NONOTICE_ONLY, ");
        stb.append("    T1.SPECIAL_GROUP_CD, ");
        stb.append("    T1.SPECIAL_LESSON_MINUTES0, ");
        stb.append("    T1.SPECIAL_SUSPEND_MINUTES, ");
        stb.append("    T1.SPECIAL_MOURNING_MINUTES, ");
        stb.append("    T1.SPECIAL_LESSON_MINUTES, ");
        stb.append("    T1.SPECIAL_SICK_MINUTES1, ");
        stb.append("    T1.SPECIAL_SICK_MINUTES2, ");
        stb.append("    T1.SPECIAL_SICK_MINUTES3, ");
        stb.append("    T2.ABSENCE_HIGH, ");
        stb.append("    T2.GET_ABSENCE_HIGH, ");
        stb.append("    T2.ABSENCE_WARN, ");
        stb.append("    T2.ABSENCE_WARN2, ");
        stb.append("    T2.ABSENCE_WARN3, ");
        stb.append("    T2.CREDITS ");
        stb.append(" FROM ");
        stb.append("  MAIN T1 ");
        stb.append(" LEFT JOIN ABSENCE_HIGH T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("      AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("    SUBCLASSCD, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER ");
        logInfo(paramMap, " revision " + revision + " getAttendSubclassSql = " + stb.toString(), !"1".equals(paramMap.get(KEY_noOutputSql)));
        return stb.toString();
    }

} // AttendAccumulate

// eof
