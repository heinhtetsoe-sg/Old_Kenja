package servletpack.KNJA;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfForm;

public class KNJA130CCommon {

    private static final Log log = LogFactory.getLog(KNJA130C.class);

    protected static String MARK_FROM_TO = "\uFF5E";

    protected static final String CERTIF_KINDCD = "107";

    protected static final String _88 = "88";
    protected static final String _90 = "90";
    protected static final String _92 = "92";
    protected static final String _94 = "94";
    protected static final String _95 = "95";
    protected static final String _ABROAD = "abroad";
    protected static final String ANOTHER_YEAR = "0";
    protected static final String SCHOOLCD1 = "1";

    private static final String CHITEKI1_知的障害 = "1";
    private static final String CHITEKI2_知的障害以外 = "2";

    protected static final Integer SHOWORDER_DEFAULT = new Integer(99999999);

    public final String revisionCommon() {
        return "$Id$";
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    protected static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        return Util.getPageList(list, max);
    }

    protected static <T> T def(final T ... ts) {
        return Util.def(ts);
    }

    protected static String defstr(final Object ... os) {
        return Util.defstr(os);
    }

    protected static String notblankstr(final Object ... os) {
        return Util.notblankstr(os);
    }

    protected static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        return Util.getMappedList(map, key1);
    }

    /**
     * listの要素でindexListに含まれる要素のリストを得る
     * @param list
     * @param indexList インデクスのリスト
     * @return listの要素でindexListに含まれる要素のリスト
     */
    protected static <T> List<T> getIndexedList(final List<T> list, final List<Integer> indexList) {
//        log.debug(" index = " + indexList);
        final List<T> rtn = new ArrayList<T>();
        for (final Integer idx : indexList) {
            rtn.add(list.get(idx.intValue()));
        }
        return rtn;
    }

    protected static Calendar getCalendarOfDate(final String date) {
        return Util.getCalendarOfDate(date);
    }

    protected static int getNendo(final Calendar cal) {
        return Util.getNendo(cal);
    }

    protected static boolean isDayOfMonth(final String date, final int month, final int dayOfMonth) {
        return Util.isDayOfMonth(date, month, dayOfMonth);
    }

    protected static String month(final String date) {
        return Util.month(date);
    }

    protected static String dayOfMonth(final String date) {
        return Util.dayOfMonth(date);
    }

    protected static String year(final String date) {
        return Util.year(date);
    }

    protected static String nendo(final String date) {
        return Util.nendo(date);
    }

    protected static boolean dateIsInNendo(final String date, final String year) {
        return Util.dateIsInNendo(date, year);
    }

    protected static String toDigit(final String s, final String def) {
        return Util.toDigit(s, def);
    }

    protected static String addNumber(final String i1, final String i2) {
        return Util.addNumber(i1, i2);
    }

    protected static Integer addNumber(final Integer i1, final Integer i2) {
        return Util.addNumber(i1, i2);
    }

    protected static BigDecimal addNumber(final BigDecimal i1, final BigDecimal i2) {
        return Util.addNumber(i1, i2);
    }

    protected static String addDay(final String date, final int d) {
        if (null == date) {
            return date;
        }
        String rtn = null;
        try {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            cal.add(Calendar.DAY_OF_MONTH, d);
            rtn = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        } catch (Exception e) {
            log.warn("exception!", e);
        }
        return rtn;
    }

    /**
     *  文字編集（ブランク挿入）
     */
    protected static StringBuffer setFormatInsertBlankFormatJp(final String date, final String blank1, final String blank2) {
        final StringBuffer stb = new StringBuffer();
        final StringBuffer digits = new StringBuffer();
        for (int i = 0; i < date.length(); i++) {
            final char ch = date.charAt(i);
            if (Character.isDigit(ch) || ch == '元') {
                digits.append(ch);
            } else {
                if (0 < digits.length()) {
                    // 数値が途切れた際に数値の前後にブランクを挿入
                    final int digitsKeta = getMS932ByteLength(digits.toString());
                    if (1 == digitsKeta) {
                        stb.append(blank1);
                    } else if (2 == digitsKeta) {
                        stb.append(blank2);
                    }
                    stb.append(digits);
                    stb.append(blank2);
                    digits.delete(0, digits.length());
                }
                stb.append(ch);
            }
        }
        return stb;
    }

    /**
     * 日付の編集（XXXX年XX月XX日の様式に編集）
     * @param hdate
     * @return
     */
    protected static String setDateFormat2(final String hdate) {
        if (hdate == null || 0 == hdate.length()) {
            return "    年  月  日";
        }
        return setFormatInsertBlankDate(hdate).toString();
    }

    /**
     * 文字編集（日付の数字が１桁の場合、ブランクを挿入）
     * @param stb
     * @return
     */
    protected static StringBuffer setFormatInsertBlankDate(final String hdate) {
        final StringBuffer stb = new StringBuffer(hdate);
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = stb.charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else if (0 < n) {
                if (1 == n) {
                    stb.insert(i - n, " ");
                    i++;
                    n = 0;
                }
            }
        }
        return stb;
    }

    protected static String nullzero(final Object o) {
        return null == o ? "0" : o.toString();
    }

    protected static <T extends Comparable<T>> T max(final Collection<T> elemList) {
        if (elemList.isEmpty()) {
            return null;
        }
        T max = elemList.iterator().next();
        for (final T e : elemList) {
            if (e.compareTo(max) > 0) {
                max = e;
            }
        }
        return max;
    }

    protected static boolean isAnotherSchoolYear(final String year) {
        return NumberUtils.isDigits(year) && Integer.parseInt(year) == 0;
    }

    protected static String kakko(final Object s) {
        return null == s || StringUtils.isBlank(s.toString()) ? "" : "(" + s + ")";
    }

    protected static List<Integer> getIndexList(final Param param, final String debug, final List<?> list) {
        final List<Integer> rtn = new LinkedList<Integer>();
        for (int i = 0; i < list.size(); i++) {
            rtn.add(new Integer(i));
        }
        if (param._isOutputDebugData) {
            log.info(" " + debug + " index = " + rtn + " / list = " + list);
        }
        return rtn;
    }

    protected static String seirekiFormat(final String date) {
        final String year = year(date);
        final String month = month(date);
        final String dayOfMonth = dayOfMonth(date);
        if (null == year || null == month || null == dayOfMonth) {
            return "";
        }
        return year + "年" + month + "月" + dayOfMonth + "日";
    }

    private static String seirekiFormatNenMonth(final String date) {
        final String year = year(date);
        final String month = month(date);
        if (null == year || null == month) {
            return "";
        }
        return year + "年" + month + "月";
    }

    protected static String seirekiFormatNen(final String date) {
        final String year = year(date);
        if (null == year) {
            return "";
        }
        return year + "年";
    }

    protected static String formatDate(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return seirekiFormat(sdate);
        }
        return KNJ_EditDate.h_format_JP(db2, sdate);
    }


    protected static String formatDate1(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return "　" + seirekiFormat(sdate);
        }
        return KNJ_EditDate.h_format_JP(db2, sdate);
    }

    protected static String formatDateNenMonth(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return "　" + seirekiFormatNenMonth(sdate);
        }
        return KNJ_EditDate.h_format_JP_M(db2, sdate);
    }

    protected static String formatDateNen(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return "　" + seirekiFormatNen(sdate);
        }
        return KNJ_EditDate.h_format_JP_N(db2, sdate);
    }

    /**
     *  年度の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象年度「平成3年度」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年度」-> 「平成 3年度」
     */
    protected static String setNendoFormatInsertBlank(final DB2UDB db2, final String hdate, final Param param, final String formatDateDefaultYear) {
        final StringBuffer stb = new StringBuffer();
        final String blank1, blank2;
        if (param._z010.in(Z010.tokiwa)) {
            blank1 = "";
            blank2 = "";
        } else {
            blank1 = "　";
            blank2 = " ";
        }
        if (hdate != null) {
            //「平成18年度」の様式とする => 数値は２桁
            return setFormatInsertBlankFormatJp(hdate, blank1, blank2).toString();
        } else {
            if (param._isSeireki) {
                stb.append("    " + blank2 + "年度");
            } else {
                try {
                    //日付が無い場合は「平成　年度」の様式とする
                    final String gengou0 = KNJ_EditDate.gengou(db2, Integer.parseInt(formatDateDefaultYear));
                    final String gengou = 2 < gengou0.length() ? gengou0.substring(0, 2) : gengou0;
                    stb.append(gengou);
                    stb.append("  " + blank1 + "年度");
                } catch (NumberFormatException e) {
                     log.error("NumberFormatException, formatDateDefaultYear = " + formatDateDefaultYear, e);
                }
            }
        }
        return stb.toString();
    }

    /**
     *  日付の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
     */
    protected static String setDateFormatInsertBlank(final DB2UDB db2, final String hdate, final Param param, final String formatDateDefaultYear) {
        if (hdate != null) {
            final String blank1 = "　", blank2 = " ";
            //「平成18年 1月 1日」の様式とする => 数値は２桁
            return setFormatInsertBlankFormatJp(hdate, blank1, blank2).toString();
        }
        return blankFormatDate(true, db2, param, formatDateDefaultYear);
    }

    protected static String blankFormatDate(final boolean insertBlank, final DB2UDB db2, final Param param, final String formatDateDefaultYear) {
        final StringBuffer stb = new StringBuffer();
        if (param._isSeireki) {
            if (insertBlank) {
                stb.append("　　");
                stb.append("    年    月    日");
            } else {
                stb.append("    年  月  日");
            }
        } else {
            try {
                //日付が無い場合は「平成　年  月  日」の様式とする
                final String hformat = KNJ_EditDate.gengou(db2, Integer.parseInt(formatDateDefaultYear) + 1, 3, 31);
                final String gengou = 2 < hformat.length() ? hformat.substring(0, 2) : hformat;
                stb.append(gengou);
                if (insertBlank) {
                    stb.append("    年    月    日");
                } else {
                    stb.append("  年  月  日");
                }
            } catch (NumberFormatException e) {
                log.error("NumberFormatException, formatDateDefaultYear = " + formatDateDefaultYear, e);
            }
        }
        return stb.toString();
    }

    protected static String getSubclasscd(final Map row, final Param param) {
        return KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
    }

    protected static String getSubclasscdM(final Map row, final Param param) {
        if (param._isSubclassOrderNotContainCurriculumcd) {
            return KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "- -" + KnjDbUtils.getString(row, "SUBCLASSCD");
        } else {
            return KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
        }
    }

    protected static String getSubclasscdM(final SubclassMst sm, final Param param) {
        if (param._isSubclassOrderNotContainCurriculumcd) {
            return SubclassMst.key(param, sm._classcd, sm._schoolKind, " ", sm._subclasscd);
        }
        return sm.getKey(param);
    }

    protected static Map<String, String> debugRow(final Map row, final String[] removeKey) {
        final Map<String, String> m = new HashMap<String, String>();
        for (final Object o : row.keySet()) {
            if (o instanceof Integer) {
                continue;
            } else if (o instanceof String) {
                final String key = (String) o;
                final int numIndex = StringUtils.indexOfAny(key, "123456789");
                if (1 <= numIndex && StringUtils.repeat("_", numIndex).equals(key.substring(0, numIndex))) {
                    continue;
                }
            }
            final String key = (String) o;
            if (ArrayUtils.contains(removeKey, key)) {
                continue;
            }
            m.put(key, KnjDbUtils.getString(row, key));
        }
        return m;
    }

    protected static void debugLogCheck(final Param param, final String key, final String c) {
        if (!param.debugOutputMap().containsKey(key)) {
            log.info(c);
            param.debugOutputMap().put(key, c);
        }
    }

    protected abstract static class PersonalInfo {

        static final int ENT_DIV_TENSEKI = 7;
        static final int GRD_DIV_TENSEKI = 7;
        static final int ENT_DIV_TENKA = 10008; // 実際のデータにはない仮番号
        static final int GRD_DIV_TENKA = 10008; // 実際のデータにはない仮番号

        static final String SOGOTEKI_NA_GAKUSHU_NO_JIKAN = "総合的な学習の時間";
        static final String SOGOTEKI_NA_TANKYU_NO_JIKAN = "総合的な探究の時間";

        protected final String _schregno;
        PersonalInfo(final String schregno) {
            _schregno = schregno;
        }

        protected static String getGradeName(final String gradeCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("第");
            if (StringUtils.isNumeric(gradeCd)) {
                stb.append(String.valueOf(Integer.parseInt(gradeCd)));
            }
            stb.append("学年");
            return stb.toString();
        }

        public abstract List<? extends Gakuseki> getGakusekiList();

        protected static <T> List<T> nullOrCopy(final List<T> list) {
            return (null == list) ? null : new ArrayList<T>(list);
        }

        protected static <T> List<T> emptyOrCopy(final List<T> list) {
            return new ArrayList<T>(list); // (Collections.emptyList() == list) ? Collections.emptyList() : ;
        }

        protected static class HistVal {
            String _val;
            String _histFirstVal;
            private HistVal(final String val, final String histFirstVal) {
                _val = val;
                _histFirstVal = histFirstVal;
            }
            public static HistVal of(final String val, final String histFirstVal) {
                return new HistVal(val, histFirstVal);
            }
            public String toString() {
                return "HistVal(val=" + _val + ", histFirstVal=" + _histFirstVal + ")";
            }
        }

        protected static class SchregBaseHistDat {
            final Map _row;
            SchregBaseHistDat(final Map row) {
                _row = row;
            }
            protected boolean isEmpty() {
                return null == _row || _row.isEmpty();
            }
            protected String date() {
                return KnjDbUtils.getString(_row, "EXPIREDATE");
            }
            protected void setDate(final String date) {
                if (null != _row) {
                    _row.put("EXPIREDATE", date);
                }
            }
            protected String year() {
                return KnjDbUtils.getString(_row, "YEAR");
            }
            protected String semester() {
                return KnjDbUtils.getString(_row, "SEMESTER");
            }
            protected String grade() {
                return KnjDbUtils.getString(_row, "GRADE");
            }
            protected String hrClass() {
                return KnjDbUtils.getString(_row, "HR_CLASS");
            }
            protected String attendno() {
                return KnjDbUtils.getString(_row, "ATTENDNO");
            }
            protected String courseName() {
                return KnjDbUtils.getString(_row, "COURSENAME");
            }
            protected String majorName() {
                return KnjDbUtils.getString(_row, "MAJORNAME");
            }
            protected String coursecode() {
                return KnjDbUtils.getString(_row, "COURSECODE");
            }
            protected String coursecodeName() {
                return KnjDbUtils.getString(_row, "COURSECODENAME");
            }
        }

        protected static List<Map<String, String>> getHistList(final DB2UDB db2, final String schregno, final String table, final int div, final Param param, final String entDate, final String grdDate) {
            final String psKey = "PS_HIST_" + table + div + param.SCHOOL_KIND + StringUtils.defaultString(entDate) + "|" + StringUtils.defaultString(grdDate);
            if (null == param.getPs(psKey)) {
                final String sql_state = getHistSql(table, param.SCHOOL_KIND, "?", div, param._year, entDate, grdDate);
                param.setPs(psKey, db2, sql_state);
            }

            return KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno});
        }

        protected static String getHistSql(final String table, final String schoolKind, final String schregno, final int div, final String year, final String startDate, final String endDate) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH ALL AS (");
            stb.append(" SELECT T1.* ");
            if ("GUARDIAN_HIST_DAT".equals(table)) {
                stb.append(" , GUARD_NAME AS NAME ");
                stb.append(" , GUARD_KANA AS KANA ");
                stb.append(" , GUARD_REAL_NAME AS REAL_NAME ");
                stb.append(" , GUARD_REAL_KANA AS REAL_KANA ");
                stb.append(" , GUARD_NAME_FLG AS NAME_FLG ");
                stb.append(" , GUARD_REAL_NAME_FLG AS REAL_NAME_FLG ");
                stb.append(" FROM GUARDIAN_HIST_DAT T1 ");
            } else if ("GUARANTOR_HIST_DAT".equals(table)) {
                stb.append(" , GUARANTOR_NAME AS NAME ");
                stb.append(" , GUARANTOR_KANA AS KANA ");
                stb.append(" , GUARANTOR_REAL_NAME AS REAL_NAME ");
                stb.append(" , GUARANTOR_REAL_KANA AS REAL_KANA ");
                stb.append(" , GUARANTOR_NAME_FLG AS NAME_FLG ");
                stb.append(" , GUARANTOR_REAL_NAME_FLG AS REAL_NAME_FLG ");
                stb.append(" FROM GUARANTOR_HIST_DAT T1 ");
            } else {
                stb.append(" , NAME_KANA AS KANA ");
                stb.append(" , REAL_NAME_KANA AS REAL_KANA ");
                stb.append(" FROM SCHREG_BASE_HIST_DAT T1 ");
            }
            if ("?".equals(schregno)) {
                stb.append(" WHERE T1.SCHREGNO = ? ");
            } else {
                stb.append(" WHERE T1.SCHREGNO = '" + schregno + "' ");
            }
            stb.append(" )");
            stb.append(" SELECT T1.* ");
            stb.append(" FROM ALL T1 ");
            stb.append(" INNER JOIN (SELECT SCHREGNO, ENT_DATE, VALUE(GRD_DATE, '9999-12-31') AS GRD_DATE  ");
            stb.append("            FROM SCHREG_ENT_GRD_HIST_DAT ");
            stb.append("            WHERE SCHOOL_KIND = '" + schoolKind + "' ");
            stb.append("           ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            if (div == 2) {
                stb.append(" (NAME_FLG = '1' OR REAL_NAME_FLG = '1') ");
            } else if (div == 1) {
                stb.append(" (REAL_NAME_FLG = '1') ");
            } else {
                stb.append(" (NAME_FLG = '1') ");
            }
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= '" + year + "' ");
            if (null != startDate && null != endDate) {
                stb.append("       AND (ISSUEDATE BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR VALUE(EXPIREDATE, '9999-12-31') BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR ISSUEDATE <= '" + startDate + "' AND '" + endDate + "' <= VALUE(EXPIREDATE, '9999-12-31') ");
                stb.append("          OR '" + startDate + "' <= ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') <= '" + endDate + "' ) ");
            } else if (null != startDate && null == endDate) {
                stb.append("       AND ('" + startDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR '" + startDate + "' <= ISSUEDATE) ");
            } else if (null == startDate && null != endDate) {
                stb.append("       AND ('" + endDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ISSUEDATE <= '" + endDate + "') ");
            } else { // if (null == startDate && null == endDate) {
                stb.append("       AND (ENT_DATE BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ENT_DATE <= ISSUEDATE)  ");
            }
            stb.append(" ORDER BY T1.ISSUEDATE ");

            return stb.toString();
        }

        protected static Map<String, String> getRegRow(final DB2UDB db2, final Student student, final Param param) {
            final String psKey = "PersonalInfo.loadPersonal";
            if (null == param.getPs(psKey)) {
                final String sql = PersonalInfo.sql_info_reg(param);
                if (param._isOutputDebugQuery) {
                    log.info(" personalinfo sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno }));
        }

        private static String sql_info_reg(final Param param) {

            final StringBuffer sql = new StringBuffer();

            sql.append("SELECT ");
            sql.append("  BASE.NAME,");
            sql.append("  BASE.REAL_NAME,");
            sql.append("  ENTGRD.GRD_DATE, ");
            sql.append("  BASE.NAME_KANA, ");
            sql.append("  BASE.REAL_NAME_KANA, ");
            sql.append("  BASE.BIRTHDAY, ");
            sql.append("  NMZ002.ABBV1 AS SEX,");
            sql.append("  LEDGS.BIRTHDAY_FLG,");
            sql.append("  REGD.GRADE, ");
            sql.append("  REGD.ATTENDNO, REGD.ANNUAL,");
            // 課程・学科・コース
            sql.append("  CM.COURSENAME,");
            if (param._hasMAJOR_MST_MAJORNAME2) {
                sql.append("  VALUE(MM.MAJORNAME2, MM.MAJORNAME) AS MAJORNAME,");
            } else {
                sql.append("  MM.MAJORNAME,");
            }
            sql.append("  REGD.COURSECODE, ");
            if (param._hasCOURSECODE_MST_COURSECODEABBV1) {
                sql.append("  VALUE(CCM.COURSECODEABBV1, CCM.COURSECODENAME) AS COURSECODENAME, ");
            } else {
                sql.append("  CCM.COURSECODENAME, ");
            }
            sql.append("  CM.COURSEABBV, MM.MAJORABBV,");
            // 卒業中学情報
            sql.append("  ENTGRD.FINISH_DATE,");
            sql.append("  FIN_S.FINSCHOOL_NAME AS J_NAME,");
            sql.append("  NM_MST.NAME1 AS INSTALLATION_DIV,");
            sql.append("  VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            if (param._hasFINSCHOOL_DETAIL_MST) {
                sql.append("  FIN_SD003.REMARK1 AS FINSCHOOL_NOT_PRINT_SOTSUGYO,");
            } else {
                sql.append("  CAST(NULL AS VARCHAR(1)) AS FINSCHOOL_NOT_PRINT_SOTSUGYO,");
            }
            sql.append("  (CASE WHEN NMSETUP.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("  NMSETUP.NAME_OUTPUT_FLG, ");
            sql.append("  (CASE WHEN G_NMSETUP.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("  G_NMSETUP.GUARD_NAME_OUTPUT_FLG, ");
//            if (param._hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE) {
//            	sql.append("  VALUE(ENTGRD.TENGAKU_SAKI_GRADE, DET001.BASE_REMARK2) AS TENGAKU_SAKI_GRADE, ");
//            } else {
                sql.append("  DET001.BASE_REMARK2 AS TENGAKU_SAKI_GRADE, ");
//            }
            sql.append("  REGD.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("  SCHREG_REGD_DAT REGD ");
            // 基礎情報
            sql.append("  INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            sql.append("  LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX ");
            sql.append("  LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO AND ENTGRD.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
            sql.append("  LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = ENTGRD.FINSCHOOLCD ");
            if (param._hasFINSCHOOL_DETAIL_MST) {
                sql.append("  LEFT JOIN FINSCHOOL_DETAIL_MST FIN_SD003 ON FIN_SD003.FINSCHOOLCD = ENTGRD.FINSCHOOLCD AND FIN_SD003.FINSCHOOL_SEQ = '003' ");
            }
            sql.append("  LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            sql.append("  LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            sql.append("  LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
            sql.append("  LEFT JOIN MAJOR_MST MM ON MM.COURSECD = REGD.COURSECD AND MM.MAJORCD = REGD.MAJORCD ");
            sql.append("  LEFT JOIN V_COURSECODE_MST CCM ON CCM.YEAR = REGD.YEAR AND VALUE(CCM.COURSECODE,'0000') = VALUE(REGD.COURSECODE,'0000')");
            sql.append("  LEFT JOIN SCHREG_NAME_SETUP_DAT NMSETUP ON NMSETUP.SCHREGNO = BASE.SCHREGNO AND NMSETUP.DIV = '02' ");

            sql.append("  LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT LEDGS ON LEDGS.SCHREGNO = BASE.SCHREGNO AND LEDGS.BIRTHDAY_FLG = '1' ");
            sql.append("  LEFT JOIN GUARDIAN_NAME_SETUP_DAT G_NMSETUP ON G_NMSETUP.SCHREGNO = BASE.SCHREGNO AND G_NMSETUP.DIV = '02' ");

            sql.append("  LEFT JOIN SCHREG_BASE_DETAIL_MST DET001 ON DET001.SCHREGNO = BASE.SCHREGNO AND DET001.BASE_SEQ = '001' ");

            sql.append("WHERE ");
            sql.append("    REGD.SCHREGNO = ? ");
            sql.append("    AND REGD.YEAR = '" + param._year + "' ");
            sql.append("    AND REGD.SEMESTER = '" + param._gakki + "' ");

            return sql.toString();
        }

        protected static Map<String, String> getEntGrdRow(final DB2UDB db2, final Student student, final String schoolKind, final String comebackDate, final Param param) {
            final String psKey;
            final boolean isComeback;
            if (null != comebackDate) {
                psKey = "PS_STATE_SCHREG_ENT_GRD_HIST_COMEBACK_DAT_" + schoolKind;
                isComeback = true;
            } else {
                psKey = "PS_STATE_SCHREG_ENT_GRD_HIST_DAT_" + schoolKind;
                isComeback = false;
            }
            if (null == param.getPs(psKey)) {
                final String sql = sqlSchregEntGrd(param, schoolKind, isComeback);
                if (param._isOutputDebugQuery) {
                    log.info(" entgrd sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }
            final String[] parameter = isComeback ? new String[] { student._schregno, student._schregno, comebackDate } : new String[] { student._schregno, student._schregno };

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), parameter));
        }

        private static String sqlSchregEntGrd(final Param param, final String schoolKind, final boolean isComeback) {
            final StringBuffer sql = new StringBuffer();


            sql.append(" WITH YEARS AS (");
            sql.append("      SELECT T2.YEAR, T4.GRADE, T4_2.GRADE_CD ");
            sql.append("      FROM SCHOOL_MST T2 ");
            sql.append("      LEFT JOIN ( ");
            sql.append("          SELECT DISTINCT YEAR, GRADE FROM SCHREG_REGD_DAT WHERE SCHREGNO = ? ");
            sql.append("      ) T4 ON T4.YEAR = T2.YEAR ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("      WHERE T2.SCHOOL_KIND = '" + schoolKind + "' ");
            }
            sql.append(" ) ");

            sql.append(" , MAIN AS (SELECT ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ESEME.SEMESTER AS ENT_SEMESTER, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            if (param._hasAddr2) {
                sql.append("    ENT_ADDR2,");
            } else {
                sql.append("    CAST(NULL AS VARCHAR(1)) AS ENT_ADDR2,");
            }
            sql.append("    ENT_DIV, ");
            sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    T3.NAME2 AS ENT_DIV_NAME2, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GSEME.SEMESTER AS GRD_SEMESTER, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
            if (param._hasAddr2) {
                sql.append("    GRD_ADDR2,");
            } else {
                sql.append("    CAST(NULL AS VARCHAR(1)) AS GRD_ADDR2,");
            }
            sql.append("    GRD_TERM, ");
            sql.append("    GRD_NO, ");
            sql.append("    GRD_DIV, ");
            sql.append("    T4.NAME1 AS GRD_DIV_NAME, ");
            sql.append("    T1.CURRICULUM_YEAR, ");
            sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
            sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
            sql.append(" FROM ");
            if (isComeback) {
                sql.append("    SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            } else {
                sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
            }
            sql.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ");
            sql.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ");
            sql.append("    LEFT JOIN SEMESTER_MST ESEME ON ESEME.SEMESTER <> '9' AND T1.ENT_DATE BETWEEN ESEME.SDATE AND ESEME.EDATE ");
            sql.append("    LEFT JOIN SEMESTER_MST GSEME ON GSEME.SEMESTER <> '9' AND T1.GRD_DATE BETWEEN GSEME.SDATE AND GSEME.EDATE ");
            sql.append(" WHERE ");
            sql.append("    T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + schoolKind + "' ");
            if (isComeback) {
                sql.append("    AND T1.COMEBACK_DATE = ? ");
            }
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("    MAIN.*, ");
            sql.append("    YE.GRADE AS ENT_YEAR_GRADE, ");
            sql.append("    YE.GRADE_CD AS ENT_YEAR_GRADE_CD, ");
            sql.append("    YG.GRADE AS GRD_YEAR_GRADE, ");
            sql.append("    YG.GRADE_CD AS GRD_YEAR_GRADE_CD ");
            sql.append(" FROM MAIN ");
            sql.append("    LEFT JOIN YEARS YE ON YE.YEAR = MAIN.ENT_YEAR ");
            sql.append("    LEFT JOIN YEARS YG ON YG.YEAR = MAIN.GRD_YEAR ");
            return sql.toString();
        }
    }

    protected static class EntGrdHist {
        public String _entYear;
        public Semester _entSemester;
        public String _entDate;
        public String _entReason;
        public String _entSchool;
        public String _entAddr;
        public String _entAddr2;
        public String _entDiv;
        public String _entDivName;
        public String _entYearGrade;
        public String _entYearGradeCd;
        public String _grdYear;
        public Semester _grdSemester;
        public String _grdDate;
        public String _grdReason;
        public String _grdSchool;
        public String _grdAddr;
        public String _grdAddr2;
        public String _grdNo;
        public String _grdDiv;
        public String _grdDivName;
        public String _grdYearGrade;
        public String _grdYearGradeCd;
        public String _curriculumYear;
        public String _finishDate;
        public String _installationDiv;
        public String _juniorSchoolName;
        public String _finschoolTypeName;
        public boolean _setData;

        public int grdDivInt() {
            if (!NumberUtils.isDigits(_grdDiv)) {
                return -1;
            }
            return Integer.parseInt(_grdDiv);
        }

        public boolean isTaigaku() {
            return 2 == grdDivInt();
        }

        public boolean isTengaku() {
            return 3 == grdDivInt();
        }

        public boolean isJoseki() {
            return 6 == grdDivInt();
        }

        public boolean isTenseki() {
            return 7 == grdDivInt();
        }


        public static EntGrdHist load(final DB2UDB db2, final Map<String, Semester> semesterMap, final KNJDefineSchool definecode, final String useAddrField2, final String schoolKind, final String schregno, final String comebackDate) {

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql_state(definecode, useAddrField2, schoolKind, schregno, comebackDate)));
            final EntGrdHist entgrd = new EntGrdHist();
            entgrd._entYear    = KnjDbUtils.getString(row, "ENT_YEAR");
            entgrd._entSemester = Semester.get(semesterMap, entgrd._entYear, KnjDbUtils.getString(row, "ENT_SEMESTER"));
            entgrd._entDate    = KnjDbUtils.getString(row, "ENT_DATE");
            entgrd._entReason  = KnjDbUtils.getString(row, "ENT_REASON");
            entgrd._entSchool  = KnjDbUtils.getString(row, "ENT_SCHOOL");
            entgrd._entAddr    = KnjDbUtils.getString(row, "ENT_ADDR");
            if ("1".equals(useAddrField2)) {
                entgrd._entAddr2 = KnjDbUtils.getString(row, "ENT_ADDR2");
            }
            entgrd._entDiv     = KnjDbUtils.getString(row, "ENT_DIV");
            entgrd._entDivName = KnjDbUtils.getString(row, "ENT_DIV_NAME");
            entgrd._entYearGrade = KnjDbUtils.getString(row, "ENT_YEAR_GRADE");
            entgrd._entYearGradeCd = KnjDbUtils.getString(row, "ENT_YEAR_GRADE_CD");

            entgrd._grdYear    = KnjDbUtils.getString(row, "GRD_YEAR");
            entgrd._grdSemester = Semester.get(semesterMap, entgrd._grdYear, KnjDbUtils.getString(row, "GRD_SEMESTER"));
            entgrd._grdDate    = KnjDbUtils.getString(row, "GRD_DATE");
            entgrd._grdReason  = KnjDbUtils.getString(row, "GRD_REASON");
            entgrd._grdSchool  = KnjDbUtils.getString(row, "GRD_SCHOOL");
            entgrd._grdAddr    = KnjDbUtils.getString(row, "GRD_ADDR");
            if ("1".equals(useAddrField2)) {
                entgrd._grdAddr2 = KnjDbUtils.getString(row, "GRD_ADDR2");
            }
            entgrd._grdNo      = KnjDbUtils.getString(row, "GRD_NO");
            entgrd._grdDiv     = KnjDbUtils.getString(row, "GRD_DIV");
            entgrd._grdDivName = KnjDbUtils.getString(row, "GRD_DIV_NAME");
            entgrd._grdYearGrade = KnjDbUtils.getString(row, "GRD_YEAR_GRADE");
            entgrd._grdYearGradeCd = KnjDbUtils.getString(row, "GRD_YEAR_GRADE_CD");
            entgrd._curriculumYear = KnjDbUtils.getString(row, "CURRICULUM_YEAR");

            entgrd._finishDate = KnjDbUtils.getString(row, "FINISH_DATE");
            if (!"HIRO".equals(definecode.schoolmark)) {
                entgrd._installationDiv = KnjDbUtils.getString(row, "INSTALLATION_DIV");
            }
            entgrd._juniorSchoolName = KnjDbUtils.getString(row, "J_NAME");
            entgrd._finschoolTypeName = KnjDbUtils.getString(row, "FINSCHOOL_TYPE_NAME");
            entgrd._setData = true;
            return entgrd;
        }

        public static String sql_state(final KNJDefineSchool definecode, final String useAddrField2, final String schoolKind, final String schregno, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();

            sql.append(" WITH YEARS AS (");
            sql.append("      SELECT T2.YEAR, T4.GRADE, T4_2.GRADE_CD ");
            sql.append("      FROM SCHOOL_MST T2 ");
            sql.append("      LEFT JOIN ( ");
            sql.append("          SELECT DISTINCT YEAR, GRADE FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + schregno + "' ");
            sql.append("      ) T4 ON T4.YEAR = T2.YEAR ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            sql.append(" ) ");

            sql.append(" , MAIN AS (SELECT ");
            sql.append("    SCHOOL_KIND, ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ESEME.SEMESTER AS ENT_SEMESTER, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            if ("1".equals(useAddrField2)) {
                sql.append("    ENT_ADDR2,");
            }
            sql.append("    ENT_DIV, ");
            sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GSEME.SEMESTER AS GRD_SEMESTER, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
            if ("1".equals(useAddrField2)) {
                sql.append("    GRD_ADDR2,");
            }
            sql.append("    GRD_NO, ");
            sql.append("    GRD_DIV, ");
            sql.append("    T4.NAME1 AS GRD_DIV_NAME, ");
            sql.append("    T1.CURRICULUM_YEAR, ");
            sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
            sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ");

            sql.append("    T1.FINISH_DATE,");
            sql.append("    FIN_S.FINSCHOOL_NAME AS J_NAME,");
            if (!"HIRO".equals(definecode.schoolmark)) {
                sql.append("    NM_MST.NAME1 AS INSTALLATION_DIV,");
            }
            sql.append("    VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME ");

            sql.append(" FROM ");
            if (null != comebackDate) {
                sql.append("    SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            } else {
                sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
            }
            sql.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ");
            sql.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ");
            sql.append("    LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = T1.FINSCHOOLCD ");
            if (!"HIRO".equals(definecode.schoolmark)) {
                sql.append("    LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            }
            sql.append("    LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            sql.append("    LEFT JOIN SEMESTER_MST ESEME ON ESEME.SEMESTER <> '9' AND T1.ENT_DATE BETWEEN ESEME.SDATE AND ESEME.EDATE ");
            sql.append("    LEFT JOIN SEMESTER_MST GSEME ON GSEME.SEMESTER <> '9' AND T1.GRD_DATE BETWEEN GSEME.SDATE AND GSEME.EDATE ");
            sql.append(" WHERE ");
            sql.append("    T1.SCHREGNO = '" + schregno + "' AND T1.SCHOOL_KIND = '" + schoolKind + "' ");
            if (null != comebackDate) {
                sql.append("    AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
            }
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("    MAIN.*, ");
            sql.append("    YE.GRADE AS ENT_YEAR_GRADE, ");
            sql.append("    YE.GRADE_CD AS ENT_YEAR_GRADE_CD, ");
            sql.append("    YG.GRADE AS GRD_YEAR_GRADE, ");
            sql.append("    YG.GRADE_CD AS GRD_YEAR_GRADE_CD ");
            sql.append(" FROM MAIN ");
            sql.append("    LEFT JOIN YEARS YE ON YE.YEAR = MAIN.ENT_YEAR ");
            sql.append("    LEFT JOIN YEARS YG ON YG.YEAR = MAIN.GRD_YEAR ");
            sql.append(" ORDER BY (CASE WHEN MAIN.SCHOOL_KIND = 'J' THEN 1  ");
            sql.append("                WHEN MAIN.SCHOOL_KIND = 'H' THEN 2  ");
            sql.append("                ELSE 99 END) ");
            return sql.toString();
        }
    }

    /**
     * <<生徒住所履歴クラス>>。
     */
    protected static class Address {
        static final int SQL_SCHREG = 0;
        static final int SQL_GUARDIAN = 1;
        static final int SQL_GUARANTOR = 2;

        final int _sqlflg;
        final Map<String, String> _map;

        public Address(final int sqlflg, final Map<String, String> map) {
            _sqlflg = sqlflg;
            _map = map;
        }
        protected int getIdx()           { return Integer.parseInt(KnjDbUtils.getString(_map, "IDX")); }
        protected String getIssuedate()   { return KnjDbUtils.getString(_map, "ISSUEDATE"); }
        protected String getExpiredate()  { return KnjDbUtils.getString(_map, "EXPIREDATE"); }
        protected String getAddr1()    { return KnjDbUtils.getString(_map, "ADDR1"); }
        protected String getAddr2()    { return KnjDbUtils.getString(_map, "ADDR2"); }
        protected String getZipCode()     { return KnjDbUtils.getString(_map, "ZIPCD"); }
        protected boolean isPrintAddr2() { return "1".equals(KnjDbUtils.getString(_map, "ADDR_FLG")); }

        public String toString() {
            return "Address(" + getIdx() + ":" + getIssuedate() + "," + getAddr1() + " " + getAddr2() + ")";
        }

        // 印刷対象のインデクス
        protected static List<Integer> getPrintAddressIndex(final Param param, final String debug, final List<Address> addressList, final int max) {
            final List<Integer> rtn = new LinkedList<Integer>();
            if (!addressList.isEmpty()) {
                final List<Integer> indexList = getIndexList(param, debug, addressList);
                if (param._isAddressOutput2) {
                    rtn.addAll(Util.reverse(Util.take(Math.min(max, indexList.size()), Util.reverse(indexList)))); // 履歴の最後から最大max分
                } else {
                    rtn.add(indexList.get(0)); // 履歴の最初
                    rtn.addAll(Util.reverse(Util.take(Math.min(max - 1, indexList.size()), Util.reverse(Util.drop(1, indexList))))); // 履歴の最後から最大max-1分
                }
                if (param._isOutputDebugData) {
                    log.info(" index = " + rtn + " / indexList = " + indexList + ", max = " + max + ", addressList ( size = " + addressList.size() + ") = " + addressList);
                }
            }
            return rtn;
        }

        protected static List<Address> uniq(final List<Address> list) {
            if (null == list || list.size() <= 1) {
                return list;
            }
            final List<Address> rtn = new ArrayList<Address>();
            rtn.add(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                final Address address1 = rtn.get(rtn.size() - 1);
                final Address address2 = list.get(i);
                if (isSameAddress(address1, address2)) {
                    log.warn(" same address : " + address1 + " <> " + address2);
                } else {
                    rtn.add(address2);
                }
            }
            return rtn;
        }

        static boolean isSameAddressList(final List<Address> addrListA, final List<Address> addrListB) {
            boolean rtn = true;
            if (addrListA == null || addrListA.isEmpty() || addrListB == null || addrListB.isEmpty() || addrListA.size() != addrListB.size()) {
                if (null != addrListA && null != addrListB && addrListA.size() != addrListB.size()) {
                    final List<Address> addrListA2 = uniq(addrListA);
                    final List<Address> addrListB2 = uniq(addrListB);
                    if (addrListA.equals(addrListA2) && addrListB.equals(addrListB2)) {
                        rtn = false;
                    } else {
                        rtn = isSameAddressList(addrListA2, addrListB2);
                    }
                } else {
                    rtn = false;
                }
            } else {
                final int max = addrListA.size(); // == addrList2.size();
                for (int i = 0; i < max; i++) {
                    final Address addressAi = addrListA.get(i);
                    final Address addressBi = addrListB.get(i);
                    if (!isSameAddress(addressAi, addressBi)) {
                        rtn = false;
                        break;
                    }
                }
            }
            return rtn;
        }

        static boolean isSameAddress(final Address addressAi, final Address addressBi) {
            boolean rtn = true;
            if (null == addressAi || null == addressBi) {
                rtn = false;
            } else {
                if (null == addressAi.getAddr1() && null == addressBi.getAddr1()) {
                } else if (null == addressAi.getAddr1() || null == addressBi.getAddr1() || !addressAi.getAddr1().equals(addressBi.getAddr1())) {
                    rtn = false;
                }
                if (null == addressAi.getAddr2() && null == addressBi.getAddr2()) {
                } else if (!addressAi.isPrintAddr2() && !addressBi.isPrintAddr2()) {
                } else if (null == addressAi.getAddr2() || null == addressBi.getAddr2() || !addressAi.getAddr2().equals(addressBi.getAddr2())) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 住所履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        protected static List<Address> loadAddress(final DB2UDB db2, final String schregno, final Param param, final int sqlflg, final String year, final String startDate, final String endDate) {
            final List<Address> addressList = new LinkedList<Address>();

            final String psKey = String.valueOf(sqlflg) + defstr(startDate) + defstr(endDate);
            if (null == param.getPs(psKey)) {
                final String sql = sqlAddressDat(sqlflg, year, param.SCHOOL_KIND, startDate, endDate);
                if (param._isOutputDebugQuery) {
                    log.info(" sql address = " + sql + " (schregno = " + schregno + ")");
                }
                param.setPs(psKey, db2, sql);
            }

            int idx = 0;
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {schregno})) {
                row.put("IDX", String.valueOf(idx));
                addressList.add(new Address(sqlflg, row));
                idx += 1;
            }
            return addressList;
        }

        /**
         * 住所のSQLを得る
         * @param sqlflg 0:生徒住所, 1:保護者住所, 2:保証人住所
         * @return
         */
        protected static String sqlAddressDat(final int sqlflg, final String year, final String schoolKind, final String startDate, final String endDate) {

            StringBuffer stb = new StringBuffer();
            if (Address.SQL_SCHREG == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.EXPIREDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            } else if (Address.SQL_GUARDIAN == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.EXPIREDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else if (Address.SQL_GUARANTOR == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.EXPIREDATE, ");
                stb.append("       T1.GUARANTOR_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARANTOR_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARANTOR_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARANTOR_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARANTOR_ADDRESS_DAT T1  ");
            }
            stb.append("INNER JOIN (SELECT SCHREGNO, ENT_DATE, VALUE(GRD_DATE, '9999-12-31') AS GRD_DATE  ");
            stb.append("            FROM SCHREG_ENT_GRD_HIST_DAT ");
            stb.append("            WHERE SCHOOL_KIND = '" + schoolKind + "' ");
            stb.append("           ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("       T1.SCHREGNO = ? ");
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= '" + year + "' ");
            if (null != startDate && null != endDate) {
                stb.append("       AND (ISSUEDATE BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR VALUE(EXPIREDATE, '9999-12-31') BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR ISSUEDATE <= '" + startDate + "' AND '" + endDate + "' <= VALUE(EXPIREDATE, '9999-12-31') ");
                stb.append("          OR '" + startDate + "' <= ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') <= '" + endDate + "' ) ");
            } else if (null != startDate && null == endDate) {
                stb.append("       AND ('" + startDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR '" + startDate + "' <= ISSUEDATE) ");
            } else if (null == startDate && null != endDate) {
                stb.append("       AND ('" + endDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ISSUEDATE <= '" + endDate + "') ");
            } else { // if (null == startDate && null == endDate) {
                stb.append("       AND (ENT_DATE BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ENT_DATE <= ISSUEDATE)  ");
            }
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE ");
            return stb.toString();
        }
    }

    protected static class Semester {
        static final Semester Null = new Semester(null, null, null, null);
        final String _year;
        final String _semester;
        final String _sdate;
        final String _edate;
        Semester(final String year, final String semester, final String sdate, final String edate) {
            _year = year;
            _semester = semester;
            _sdate = sdate;
            _edate = edate;
        }
        public String key() {
            return key(_year, _semester);
        }
        public static String key(final String year, final String semester) {
            return year + "-" + semester;
        }
        public static Semester get(final Map<String, Semester> _semesterMap, final String year, final String semester) {
            final Semester seme = _semesterMap.get(key(year, semester));
            if (null == seme) {
                return Null;
            }
            return seme;
        }

        public static Map<String, Semester> load(final DB2UDB db2) {
            final Map<String, Semester> rtn = new HashMap<String, Semester>();
            final String sql = " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE SEMESTER <> '9' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final Semester semester = new Semester(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE"));
                final String key = Semester.key(semester._year, semester._semester);
                rtn.put(key, semester);
            }
            return rtn;
        }
    }

    protected static class SchregRegdGdat {
        String _year;
        String _grade;
        String _gradeCd;
        String _gradeName2;

        String _gakunenSimple;

        private SchregRegdGdat() {}

        static SchregRegdGdat create() {
            return new SchregRegdGdat();
        }

        public static SchregRegdGdat getSchregRegdGdat(final String year, final String grade, final Map<String, Map<String, SchregRegdGdat>> map) {
            //log.debug(" valg = " + map.get(key) + " / " + key);
            if (null == map.get(year)) {
                return create();
            }
            if (null == Util.getMappedHashMap(map, year).get(grade)) {
                return create();
            }
            return Util.getMappedHashMap(map, year).get(grade);
        }

        public String toString() {
            return "Gdat (" + _year + ", " + _grade + ", " + _gradeCd + ", " + _gradeName2 + ", " + _gakunenSimple + ")";
        }
    }

    protected static class SchregRegdHdat {
        String _year;
        String _semester;
        String _grade;
        String _hrClass;
        String _hrname;

        private SchregRegdHdat() {}

        static SchregRegdHdat create() {
            return new SchregRegdHdat();
        }

        public static SchregRegdHdat getSchregRegdHdat(final String year, final String semester, final String grade, final String hrClass, final Map<String, SchregRegdHdat> map) {
            final String key = key(year, semester, grade, hrClass);
            //log.debug(" valh = " + map.get(key) + " / " + key);
            if (null == map.get(key)) {
                log.info(" not found regdh : " + key);
                return create();
            }
            return map.get(key);
        }

        static String key(final String year, final String semester, final String grade, final String hrClass) {
            return year + ":" + semester + ":" + grade + ":" + hrClass;
        }

        public String toString() {
            return "Hdat (" + key(_year, _semester, _grade, _hrClass) + ", " + _hrname + ")";
        }
    }

    protected static class SchregRegdDat {
        String _year;
        String _semester;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _coursecode;

        private SchregRegdDat() {}

        static SchregRegdDat create() {
            return new SchregRegdDat();
        }

        static Map<String, List<SchregRegdDat>> getYearRegdListMap(final Collection<SchregRegdDat> regdList) {
            final Map<String, List<SchregRegdDat>> rtn = new TreeMap<String, List<SchregRegdDat>>();
            for (final SchregRegdDat regd : regdList) {
                getMappedList(rtn, regd._year).add(regd);
            }
            return rtn;
        }

        static SchregRegdDat getMaxSemesterRegd(final Collection<SchregRegdDat> regdList, final String year) {
            if (null == regdList || regdList.size() == 0 || null == year) {
                return null;
            }
            final TreeMap<String, TreeMap<String, SchregRegdDat>> m = new TreeMap<String, TreeMap<String, SchregRegdDat>>();
            for (final SchregRegdDat regd : regdList) {
                if (null != regd._year && null != regd._semester) {
                    Util.getMappedMap(m, regd._year).put(regd._semester, regd);
                }
            }
            final TreeMap<String, SchregRegdDat> yearMap = Util.getMappedMap(m, year);
            if (yearMap.isEmpty()) {
                return null;
            }
            return yearMap.get(yearMap.lastKey());
        }

        public String toString() {
            return "Regd(year=" + _year + ", semester=" + _semester + ", grade=" + _grade + ", hrClass=" + _hrClass +", coursecd=" + _coursecd + ", majorcd=" + _majorcd + ", coursecode=" + _coursecode + ")";
        }
    }

    /**
     * <<スタッフマスタ>>。
     */
    protected static class StaffMst {
        /**pkg*/ static StaffMst Null = new StaffMst(null, null, null, null, null);
        final String _staffcd;
        final String _name;
        final String _kana;
        final String _nameReal;
        final String _kanaReal;
        private final Map<String, Map<String, String>> _yearStaffNameSetUp;
        private final Map<String, Map<String, String>> _yearStaffNameHist;
        public StaffMst(final String staffcd, final String name, final String kana, final String nameReal, final String kanaReal) {
            _staffcd = staffcd;
            _name = name;
            _kana = kana;
            _nameReal = nameReal;
            _kanaReal = kanaReal;
            _yearStaffNameSetUp = new HashMap<String, Map<String, String>>();
            _yearStaffNameHist = new HashMap<String, Map<String, String>>();
        }
        public boolean isPrintNameBoth(final String year) {
            final Map<String, String> nameSetup = _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year) {
            final Map<String, String> nameSetup = _yearStaffNameSetUp.get(year);
            return null != nameSetup;
        }

        public List<String> getNameLine(final String year, final Param param, final int keta) {
            return getNameLine(year, param.property(Property.certifPrintRealName), keta);
        }

        public List<String> getNameLine(final String year, final String property_certifPrintRealName, final int keta) {

            final String[] nameLine;
            final String name = getYearHistDatValue(year, "STAFFNAME", _name);
            final String nameReal = getYearHistDatValue(year, "STAFFNAME_REAL", _nameReal);
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(nameReal)) {
                    nameLine = new String[]{name};
                } else {
                    if (StringUtils.isBlank(name)) {
                        nameLine = new String[]{nameReal};
                    } else {
                        final String n = "（" + name + "）";
                        if ((null == nameReal ? "" : nameReal).equals(name)) {
                            nameLine =  new String[]{nameReal};
                        } else if (getMS932ByteLength(nameReal + n) > keta) {
                            nameLine =  new String[]{nameReal, n};
                        } else {
                            nameLine =  new String[]{nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year) || "1".equals(property_certifPrintRealName)) {
                if (StringUtils.isBlank(nameReal)) {
                    nameLine = new String[]{name};
                } else {
                    nameLine = new String[]{nameReal};
                }
            } else {
                nameLine = new String[]{name};
            }
            return Arrays.asList(nameLine);
        }

        /**
         * 指定年度を含む履歴の名前フィールドの値を得る
         * @param yearSearch 指定年度
         * @param nameField 名前フィールド
         * @param defVal デフォルトの値
         * @return 指定年度を含む履歴の名前フィールドの値。値がnullならデフォルトの値
         */
        private String getYearHistDatValue(final String yearSearch, final String nameField, final String defVal) {
            final TreeMap<String, Map<String, String>> sortedMap = new TreeMap<String, Map<String, String>>(_yearStaffNameHist);
            Map<String, String> histDat = null;
            for (final Map<String, String> histDat0 : sortedMap.values()) {
                final String syear = histDat0.get("SYEAR");
                final String eyear = histDat0.get("EYEAR");
                if (syear.compareTo(yearSearch) <= 0 && yearSearch.compareTo(eyear) <= 0) {
                    histDat = histDat0;
                }
            }
            String rtn;
            if (histDat == null) {
                // 履歴データがなければデフォルトの値
                rtn = defVal;
            } else {
                // 履歴データの指定フィールドの値がなければデフォルトの値
                rtn = StringUtils.isEmpty(histDat.get(nameField)) ? defVal : histDat.get(nameField);
            }
            // log.debug(" year search = " + yearSearch + ",  histDat = " + histDat + ", rtn = " + rtn);
            return rtn;
        }

        public static StaffMst get(final Map<String, StaffMst> staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return staffMstMap.get(staffcd);
        }

        public static Map<String, StaffMst> loadStaffMst(final DB2UDB db2, final String year) {
            final Map<String, StaffMst> rtn = new HashMap<String, StaffMst>();

            final String sql1 = "SELECT * FROM STAFF_MST ";
            for (final Map<String, String> m : KnjDbUtils.query(db2, sql1)) {
                final String staffcd = m.get("STAFFCD");
                final String name = m.get("STAFFNAME");
                final String kana = m.get("STAFFNAME_KANA");
                final String nameReal = m.get("STAFFNAME_REAL");
                final String kanaReal = m.get("STAFFNAME_KANA_REAL");

                final StaffMst s = new StaffMst(staffcd, name, kana, nameReal, kanaReal);

                rtn.put(s._staffcd, s);
            }
            return rtn;
        }

        public static Map<String, StaffMst> loadStaffMstEx(final DB2UDB db2, final DB2UDB db2staffNameHistDat, final String year) {
            final Map<String, StaffMst> rtn = loadStaffMst(db2, year);

            final String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT WHERE YEAR <= '" + year + "' AND DIV = '02' ";
            for (final Map<String, String> m : KnjDbUtils.query(db2, sql2)) {
                if (null == rtn.get(m.get("STAFFCD"))) {
                    continue;
                }
                final StaffMst s = rtn.get(m.get("STAFFCD"));

                Util.getMappedHashMap(s._yearStaffNameSetUp, m.get("YEAR")).put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
            }

            final boolean hasSTAFF_NAME_HIST_DAT = KnjDbUtils.setTableColumnCheck(db2, "STAFF_NAME_HIST_DAT", null);
            if (hasSTAFF_NAME_HIST_DAT) {
                final StringBuffer sqlHist = new StringBuffer();
                sqlHist.append(" WITH MAX_SDATE AS ( ");
                sqlHist.append("SELECT ");
                sqlHist.append("   STAFFCD, MAX(SDATE) AS SDATE ");
                sqlHist.append(" FROM STAFF_NAME_HIST_DAT ");
                sqlHist.append(" WHERE ");
                sqlHist.append("   FISCALYEAR(SDATE) <= '" + year + "' ");
                sqlHist.append(" GROUP BY ");
                sqlHist.append("   STAFFCD, FISCALYEAR(SDATE) ");
                sqlHist.append(" )");
                sqlHist.append("SELECT ");
                sqlHist.append("  T1.STAFFCD, ");
                sqlHist.append("   FISCALYEAR(T1.SDATE) AS SYEAR, ");
                sqlHist.append("   FISCALYEAR(VALUE(T1.EDATE, '9999-12-31')) AS EYEAR, ");
                sqlHist.append("  T1.STAFFNAME, ");
                sqlHist.append("  T1.STAFFNAME_REAL ");
                sqlHist.append(" FROM STAFF_NAME_HIST_DAT T1 ");
                sqlHist.append(" INNER JOIN MAX_SDATE T2 ON T2.STAFFCD = T1.STAFFCD AND T2.SDATE = T1.SDATE ");
                for (final Map<String, String> m : KnjDbUtils.query(null != db2staffNameHistDat ? db2staffNameHistDat : db2, sqlHist.toString())) { // 三重県はdb2_2から読込
                    if (null == rtn.get(m.get("STAFFCD"))) {
                        continue;
                    }
                    final StaffMst s = rtn.get(m.get("STAFFCD"));

                    final Map<String, String> nameHistDat = new HashMap<String, String>();
                    nameHistDat.put("STAFFCD", m.get("STAFFCD"));
                    nameHistDat.put("STAFFNAME", m.get("STAFFNAME"));
                    nameHistDat.put("STAFFNAME_REAL", m.get("STAFFNAME_REAL"));
                    nameHistDat.put("SYEAR", m.get("SYEAR"));
                    nameHistDat.put("EYEAR", m.get("EYEAR"));
                    s._yearStaffNameHist.put(m.get("SYEAR"), nameHistDat);
                }
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + (_yearStaffNameSetUp.isEmpty() ? "" : ", nameSetupDat=" + _yearStaffNameSetUp) + (_yearStaffNameHist.isEmpty() ? "" : ", yearStaffNameHist = " + _yearStaffNameHist) + ")";
        }
    }

    /**
     * <<スタッフクラス>>。
     */
    public static class Staff {
        /**pkg*/ static Staff Null = new Staff(null, StaffMst.Null, null, null, null);
        final String _year;
        final StaffMst _staffMst;
        final String _dateFrom;
        final String _dateTo;
        final String _stampNo;
        public Staff(final String year, final StaffMst staffMst, final String dateFrom, final String dateTo, final String stampNo) {
            _year = year;
            _staffMst = staffMst;
            _dateFrom = dateFrom;
            _dateTo = dateTo;
            _stampNo = stampNo;
        }

        public String getNameString(final Param param, final int keta) {
            return getNameString(param.property(Property.certifPrintRealName), keta);
        }

        public String getNameString(final String property_certifPrintRealName, final int keta) {
            return Util.mkString(_staffMst.getNameLine(_year, property_certifPrintRealName, keta), "").toString();
        }

        public List<String> getNameBetweenLine(final Param param, final int keta) {
            return getNameBetweenLine(param.property(Property.certifPrintRealName), keta);
        }

        public List<String> getNameBetweenLine(final String property_certifPrintRealName, final int keta) {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between;
            if (StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate)) {
                between = "";
            } else if (!StringUtils.isBlank(jpMonthName(fromDate)) && jpMonthName(fromDate).equals(jpMonthName(toDate))) {
                between = "(" + jpMonthName(fromDate) + ")";
            } else {
                between = "(" + jpMonthName(fromDate) + MARK_FROM_TO + jpMonthName(toDate) + ")";
            }

            final List rtn;
            if (getMS932ByteLength(getNameString(property_certifPrintRealName, keta) + between) > keta) {
                rtn = Arrays.asList(getNameString(property_certifPrintRealName, keta), between);
            } else {
                rtn = Arrays.asList(getNameString(property_certifPrintRealName, keta) + between);
            }
            return rtn;
        }

        protected String toYearDate(final String date, final String year) {
            if (null == date) {
                return null;
            }
            final String sdate = year + "-04-01";
            final String edate = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
            if (date.compareTo(sdate) <= 0) {
                return sdate;
            } else if (date.compareTo(edate) >= 0) {
                return edate;
            }
            return date;
        }

        protected String jpMonthName(final String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            return new SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
        }

        protected static Staff get(final int idx, final List<Staff> staffList) {
            return staffList.isEmpty() || idx >= staffList.size() ? Staff.Null : staffList.get(idx);
        }

        protected static List<Staff> getUniqueStaffList(final Staff ... staffs) {
            final List<Staff> rtn = new ArrayList<Staff>();
            final List<StaffMst> msts = new ArrayList<StaffMst>();
            for (final Staff staff : staffs) {
                if (staff == Staff.Null || rtn.contains(staff) || staff._staffMst == StaffMst.Null || msts.contains(staff._staffMst)) {
                    continue;
                }
                rtn.add(staff);
                msts.add(staff._staffMst);
            }
            return rtn;
        }

        public String toString() {
            return "Staff(year=" + _year + ", staffMst=" + _staffMst + ", dateFrom=" + _dateFrom + ", dateTo=" + _dateTo + ", stampNo="+ _stampNo + ")";
        }
    }

    public static class StaffInfo {

        public static final String TR_DIV1 = "1";
        public static final String TR_DIV2 = "2";
        public static final String TR_DIV3 = "3";

        final String _schoolKind;
        protected Map<String, TreeMap<Integer, String>> _inkanMap = Collections.emptyMap();
        protected Map<String, List<Map<String, String>>> _yearPrincipalListMap = Collections.emptyMap();
        protected Map<String, StaffMst> _staffMstMap = Collections.emptyMap();
        protected Map<String, TreeMap<String, List<Staff>>> _staffClassHistMap = Collections.emptyMap();

        /**
         * @deprecated use StaffInfo(String schoolKind)
         */
        StaffInfo() {
            this("H");
        }

        StaffInfo(final String schoolKind) {
            _schoolKind = schoolKind;
        }

        public void load(final DB2UDB db2, final DB2UDB db2_2, final String year) {
            setInkanMap(db2);
            setYearPrincipalMap(db2);
            _staffMstMap = StaffMst.loadStaffMstEx(db2, db2_2, year);
            setStaffClassHistMap(db2, year);
        }

        private void setYearPrincipalMap(final DB2UDB db2) {
            _yearPrincipalListMap = new TreeMap<String, List<Map<String, String>>>();

            final boolean _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");

            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH PRINCIPAL_HIST AS ( ");
            sql.append("     SELECT ");
            sql.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR ORDER BY T2.YEAR, T1.FROM_DATE) AS ORDER ");
            sql.append("     FROM ");
            sql.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,SCHOOL_MST T2 ");
            sql.append("     WHERE ");
            sql.append("         T1.SCHOOL_KIND = '" + _schoolKind + "' ");
            sql.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            sql.append(" ) ");
            sql.append("     SELECT ");
            sql.append("         T1.YEAR, T1.STAFFCD, T1.FROM_DATE, T1.TO_DATE ");
            sql.append("     FROM PRINCIPAL_HIST T1 ");
            sql.append("     ORDER BY T1.YEAR, T1.ORDER ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                getMappedList(_yearPrincipalListMap, KnjDbUtils.getString(row, "YEAR")).add(row);
            }
        }

        public void setInkanMap(final DB2UDB db2) {
            _inkanMap = new HashMap<String, TreeMap<Integer, String>>();
            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH TMP AS (");
            sql.append("   SELECT ");
            sql.append("     STAFFCD, ");
            sql.append("     VALUE(START_DATE, DATE, '1901-01-01') AS DATE, ");
            sql.append("     STAMP_NO ");
            sql.append("   FROM ATTEST_INKAN_DAT ");
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   FISCALYEAR(T1.DATE) AS YEAR, ");
            sql.append("   MAX(T1.STAMP_NO) AS STAMP_NO ");
            sql.append(" FROM TMP T1 ");
            sql.append(" INNER JOIN (SELECT ");
            sql.append("               L1.STAFFCD, ");
            sql.append("               FISCALYEAR(L1.DATE) AS YEAR, ");
            sql.append("               MAX(L1.DATE) AS DATE ");
            sql.append("             FROM TMP L1 ");
            sql.append("             GROUP BY ");
            sql.append("               L1.STAFFCD, ");
            sql.append("               FISCALYEAR(L1.DATE) ");
            sql.append("            ) T2 ON T2.STAFFCD = T1.STAFFCD ");
            sql.append("                AND T2.DATE = T1.DATE ");
            sql.append(" GROUP BY ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   FISCALYEAR(T1.DATE) ");
            sql.append(" ORDER BY ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   MAX(T1.STAMP_NO) ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                Util.getMappedMap(_inkanMap, KnjDbUtils.getString(row, "STAFFCD")).put(KnjDbUtils.getInt(row, "YEAR", null), KnjDbUtils.getString(row, "STAMP_NO"));
            }
        }

        public String getStampNo(final String staffcd, final String year) {
            if (null == _inkanMap.get(staffcd) || !NumberUtils.isDigits(year)) {
                return null;
            }
            String stampNo = null;
            final TreeMap<Integer, String> yearStampnoMap = Util.getMappedMap(_inkanMap, staffcd);
            if (yearStampnoMap.size() == 1) {
                final Map.Entry<Integer, String> e = yearStampnoMap.entrySet().iterator().next();
                stampNo = e.getValue();
                return stampNo;
            }
            final Integer iYear = Integer.valueOf(year);
            for (final Map.Entry<Integer, String> e : yearStampnoMap.entrySet()) {
                final Integer inkanYear = e.getKey();
                if (inkanYear.intValue() > iYear.intValue()) {
                    break;
                }
                stampNo = e.getValue();
            }
            return stampNo;
        }

        public void setStaffClassHistMap(final DB2UDB db2, final String maxYear) {

            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.TR_DIV, ");
            stb.append("         T1.STAFFCD, ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.FROM_DATE, ");
            stb.append("         VALUE(MAX(T1.TO_DATE), '9999-12-31') AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR <= '" + maxYear + "' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.TR_DIV, T1.STAFFCD, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append("     ORDER BY T1.TR_DIV, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE, T1.STAFFCD ");
            final String sql = stb.toString();

            final Map<String, TreeMap<String, List<Staff>>> rtn = new HashMap<String, TreeMap<String, List<Staff>>>();
            for (final Map row : KnjDbUtils.query(db2, sql, null)) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");

                final String trDiv = KnjDbUtils.getString(row, "TR_DIV");
                final String staffcd = KnjDbUtils.getString(row, "STAFFCD");
                final String staffFromDate = KnjDbUtils.getString(row, "FROM_DATE");
                final String staffToDate = KnjDbUtils.getString(row, "TO_DATE");
                if (null == staffFromDate) {
                    continue;
                }

                final Staff staff = new Staff(year, StaffMst.get(_staffMstMap, staffcd), staffFromDate, staffToDate, getStampNo(staffcd, year));

                getMappedList(Util.getMappedMap(rtn, staffClassHistKey(year, grade, hrClass, trDiv)), staffFromDate).add(staff);
            }
            _staffClassHistMap = rtn;
        }

        private static String staffClassHistKey(final String year, final String grade, final String hrClass, final String trDiv) {
            return year + ":" + grade + ":" + hrClass + ":" + trDiv;
        }

        private List<Staff> getHistStaffList(final String year, final String grade, final String hrClass, final String trDiv) {
            final List<Staff> staffList = new ArrayList();
            final TreeMap<String, List<Staff>> grhrStaffMap = Util.getMappedMap(_staffClassHistMap, staffClassHistKey(year, grade, hrClass, trDiv));
            for (final List<Staff> fromDateStaffList : grhrStaffMap.values()) {
                if (fromDateStaffList.size() > 0) {
                    staffList.add(fromDateStaffList.get(0)); // 同一日付なら最小職員コードの職員
                }
            }
            return staffList;
        }

        public List<Staff> getStudentStaffHistList(final boolean isDebug, final Map<String, Semester> semesterMap, final List<SchregRegdDat> regdList, final EntGrdHist entGrdHist, final String trDiv, final String year) {
            return getStudentStaffHistList(isDebug, semesterMap, regdList, entGrdHist._entSemester, entGrdHist._entDate, entGrdHist._grdSemester, entGrdHist._grdDate, trDiv, year);
        }

        public List<Staff> getStudentStaffHistList(final boolean isDebug, final Map<String, Semester> semesterMap, final List<SchregRegdDat> regdList, final Semester entSemester, final String entDate, final Semester grdSemester, final String grdDate, final String trDiv, final String year) {
            final List<SchregRegdDat> regdInYear = getMappedList(SchregRegdDat.getYearRegdListMap(regdList), year);
            final List<Staff> studentStaffHistList = new ArrayList<Staff>();
            SchregRegdDat beforeRegd = null;
            List<String> differentGradeHrclasses = new ArrayList<String>();
            for (final SchregRegdDat regd : regdInYear) {
                if (null != beforeRegd && !(regd._grade + regd._hrClass).equals(beforeRegd._grade + beforeRegd._hrClass)) {
                    differentGradeHrclasses.add("TO:" + beforeRegd._semester + beforeRegd._grade + beforeRegd._hrClass);
                    differentGradeHrclasses.add("FROM:" + regd._semester + regd._grade + regd._hrClass);
                }
                beforeRegd = regd;
            }
            if (isDebug) {
                log.info(" different grade hrclass = " + differentGradeHrclasses);
            }
            Staff beforeStaff = null;
            for (final SchregRegdDat regd : regdInYear) {
                final Semester semester = Semester.get(semesterMap, regd._year, regd._semester);
                String sdate = semester._sdate;
                String edate = semester._edate;
                boolean setFrom = false;
                boolean setTo = false;
                if (differentGradeHrclasses.contains("FROM:" + regd._semester + regd._grade + regd._hrClass)) {
                    setFrom = true;
                }
                if (differentGradeHrclasses.contains("TO:" + regd._semester + regd._grade + regd._hrClass)) {
                    setTo = true;
                }
                final String regdSemesterKey = Semester.get(semesterMap, regd._year, regd._semester).key();
                if (entSemester.key().equals(regdSemesterKey)) {
                    sdate = entDate;
                    setFrom = true;
                }
                boolean noMore = false;
                if (grdSemester.key().equals(regdSemesterKey)) {
                    edate = grdDate;
                    setTo = true;
                    noMore = true;
                }

                for (Staff staff : getHistStaffList(year, regd._grade, regd._hrClass, trDiv)) {
                    if (null != sdate && null != staff._dateTo && staff._dateTo.compareTo(sdate) < 0) {
                        continue;
                    }
                    if (null != edate && null != staff._dateFrom && edate.compareTo(staff._dateFrom) < 0) {
                        continue;
                    }
                    String dateFrom = staff._dateFrom;
                    if (null != beforeStaff && beforeStaff._staffMst == staff._staffMst) {
                        dateFrom = beforeStaff._dateFrom;
                    } else if (setFrom) {
                        dateFrom = Util.maxDate(sdate, dateFrom);
                    }
                    String dateTo = staff._dateTo;
                    if (setTo) {
                        dateTo = Util.minDate(edate, dateTo);
                        for (int i = studentStaffHistList.size() - 1; i >= 0; i--) {
                            final Staff befStaff = studentStaffHistList.get(i);
                            if (befStaff._staffMst == staff._staffMst && defstr(befStaff._dateTo).equals(defstr(staff._dateTo))) {
                                studentStaffHistList.set(i, new Staff(befStaff._year, befStaff._staffMst, befStaff._dateFrom, dateTo, befStaff._stampNo));
                            }
                        }
                    }
                    staff = new Staff(staff._year, staff._staffMst, dateFrom, dateTo, staff._stampNo);
                    if (isDebug) {
                        log.info(" add staff " + studentStaffHistList.size() + " " + semester.key() + "-" + regd._grade + "-" + regd._hrClass + " " + staff);
                    }
                    studentStaffHistList.add(staff);
                    beforeStaff = staff;
                }
                if (noMore) {
                    break;
                }
            }
            return studentStaffHistList;
        }
    }

    protected static class Student {
        protected String _useCertifKindcd = CERTIF_KINDCD;
        private Map _debugInfo = new HashMap();
        final String _schregno;

        protected Student(final String schregno) {
            _schregno = schregno;
        }


        /** 学科年度データの学校区分 */
        protected Map<String, String> _yearMajorYdatSchooldivMap;

        public CertifSchoolDat certifSchool(final Param param) {
            return CertifSchoolDat.get(param, _useCertifKindcd);
        }

        public static List<String> loadAfterGraduatedCourse(final DB2UDB db2, final String schregno, final Param param, final List gakusekiList) {
            final List<String> afterGraduatedCourseTextList = new ArrayList<String>();
            final TreeSet<String> yearSet = Gakuseki.gakusekiYearSet(gakusekiList);
            if (!param._hasAFT_GRAD_COURSE_DAT || yearSet.isEmpty()) {
                return afterGraduatedCourseTextList;
            }
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            final StringBuffer stb = new StringBuffer();
                // 進路用・就職用両方の最新の年度を取得
            stb.append(" with TA as( select ");
            stb.append("         SCHREGNO, ");
            stb.append("         '0' as SCH_SENKOU_KIND, ");
            stb.append("         MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, ");
            stb.append("         '1' as COMP_SENKOU_KIND, ");
            stb.append("         MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR ");
            stb.append(" from ");
            if (param._z010.in(Z010.shimaneken)) {
                stb.append("         AFT_GRAD_COURSE_SS_DAT ");
            } else {
                stb.append("         AFT_GRAD_COURSE_DAT ");
            }
            stb.append(" where ");
            stb.append("         SCHREGNO = '" + schregno + "'  and PLANSTAT = '1'");
            stb.append("         AND YEAR BETWEEN '" + minYear + "' AND '" + maxYear + "' ");
            stb.append(" group by ");
            stb.append("         SCHREGNO ");
            // 進路用・就職用どちらか(進路が優先)の最新の受験先種別コードを取得
            stb.append("), TA2 as( select ");
            stb.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND, ");
            stb.append("     MAX(T1.SEQ) AS SEQ ");
            stb.append(" from ");
            if (param._z010.in(Z010.shimaneken)) {
                stb.append("     AFT_GRAD_COURSE_SS_DAT T1 ");
            } else {
                stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            }
            stb.append(" inner join TA on ");
            stb.append("     T1.SCHREGNO = TA.SCHREGNO ");
            stb.append("     and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) ");
            stb.append("     and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) ");
            stb.append(" where ");
            stb.append("     T1.PLANSTAT = '1'");
            stb.append(" group by ");
            stb.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND ");
            stb.append(") ");
            // 最新の年度と受験先種別コードの感想を取得
            stb.append("select  ");
            stb.append("      T1.SENKOU_KIND ");
            stb.append("     ,T1.STAT_CD ");
            stb.append("     ,T1.THINKEXAM ");
            stb.append("     ,T1.JOB_THINK ");
            stb.append("     ,L1.NAME1 as E017NAME1 ");
            stb.append("     ,L2.NAME1 as E018NAME1 ");
            stb.append("     ,L3.SCHOOL_NAME ");
            stb.append("     ,T1.FACULTYCD ");
            stb.append("     ,L5.FACULTYNAME ");
            stb.append("     ,T1.DEPARTMENTCD ");
            stb.append("     ,L6.DEPARTMENTNAME ");
            stb.append("     ,L7.ADDR1 AS CAMPUSADDR1 ");
            stb.append("     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ");
            stb.append("     ,L4.COMPANY_NAME ");
            stb.append("     ,L4.ADDR1 AS COMPANYADDR1 ");
            stb.append("     ,L4.ADDR2 AS COMPANYADDR2 ");
            stb.append("from ");
            if (param._z010.in(Z010.shimaneken)) {
                stb.append("     AFT_GRAD_COURSE_SS_DAT T1 ");
            } else {
                stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            }
            stb.append("inner join TA2 on ");
            stb.append("     T1.YEAR = TA2.YEAR ");
            stb.append("     and T1.SCHREGNO = TA2.SCHREGNO ");
            stb.append("     and T1.SENKOU_KIND = TA2.SENKOU_KIND ");
            stb.append("     and T1.SEQ = TA2.SEQ ");
            stb.append("left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD ");
            stb.append("left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD ");
            stb.append("left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD ");
            stb.append("left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD ");
            stb.append("     and L5.FACULTYCD = T1.FACULTYCD ");
            stb.append("left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD ");
            stb.append("     and L6.FACULTYCD = T1.FACULTYCD ");
            stb.append("     and L6.DEPARTMENTCD = T1.DEPARTMENTCD ");
            stb.append("left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD ");
            stb.append("     and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD ");
            stb.append("left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD ");
            stb.append("     and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD ");
            stb.append("left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD ");
            stb.append("where ");
            stb.append("     T1.PLANSTAT = '1' ");
            stb.append("order by ");
            stb.append("     T1.YEAR, T1.SCHREGNO ");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {

                if ("0".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 進学
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E017NAME1")) {
                        afterGraduatedCourseTextList.addAll(Util.getTokenList(param, KnjDbUtils.getString(row, "THINKEXAM"), 50, 10));
                    } else {
                        afterGraduatedCourseTextList.add(defstr(KnjDbUtils.getString(row, "SCHOOL_NAME")));
                        final String faculutyname = "000".equals(KnjDbUtils.getString(row, "FACULTYCD")) || null == KnjDbUtils.getString(row, "FACULTYNAME") ?  "" : KnjDbUtils.getString(row, "FACULTYNAME");
                        final String departmentname = "000".equals(KnjDbUtils.getString(row, "DEPARTMENTCD")) || null == KnjDbUtils.getString(row, "DEPARTMENTNAME") ? "" : KnjDbUtils.getString(row, "DEPARTMENTNAME");
                        afterGraduatedCourseTextList.add(faculutyname + departmentname);
                        afterGraduatedCourseTextList.add(defstr(KnjDbUtils.getString(row, "CAMPUSFACULTYADDR1"), KnjDbUtils.getString(row, "CAMPUSADDR1")));
                    }
                } else if ("1".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 就職
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E018NAME1")) {
                        afterGraduatedCourseTextList.addAll(Util.getTokenList(param, KnjDbUtils.getString(row, "JOB_THINK"), 50, 10));
                    } else {
                        afterGraduatedCourseTextList.add(defstr(KnjDbUtils.getString(row, "COMPANY_NAME")));
                        afterGraduatedCourseTextList.add(defstr(KnjDbUtils.getString(row, "COMPANYADDR1")));
                        afterGraduatedCourseTextList.add(defstr(KnjDbUtils.getString(row, "COMPANYADDR2")));
                    }
                }
            }
            return afterGraduatedCourseTextList;
        }

        public static String loadAfterGraduatedCourseSenkouKindSub(final DB2UDB db2, final String schregno, final Param param) {
            if (!param._z010.in(Z010.kyoto)) {
                return null;
            }

            final String psKey = "PS_KYOTO_AFT_GRAD";
            if (null == param.getPs(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH YEAR_SEQ AS ( ");
                stb.append("     SELECT  ");
                stb.append("         T1.YEAR, T1.SEQ ");
                stb.append("     FROM ");
                stb.append("         AFT_GRAD_COURSE_DAT T1 ");
                stb.append("     WHERE ");
                stb.append("        SCHREGNO = ? ");
                stb.append("        AND SENKOU_KIND = '2' ");
                stb.append(" ), T_AFT_GRAD_COURSE AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.YEAR, MAX(T1.SEQ) AS SEQ ");
                stb.append("     FROM ");
                stb.append("         YEAR_SEQ T1 ");
                stb.append("         INNER JOIN (SELECT MAX(YEAR) AS YEAR FROM YEAR_SEQ) T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     GROUP BY ");
                stb.append("         T1.YEAR ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.SENKOU_KIND_SUB ");
                stb.append(" FROM ");
                stb.append("     AFT_GRAD_COURSE_DAT T1 ");
                stb.append("     INNER JOIN T_AFT_GRAD_COURSE T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.SEQ = T1.SEQ ");
                final String sql = stb.toString();

                param.setPs(psKey, db2, sql);
            }

            String rtn = null;
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno })) {

                rtn = KnjDbUtils.getString(row, "SENKOU_KIND_SUB");
            }
            return rtn;
        }
    }

    protected static class SchoolDiv {

        final String GAKUNENSEI = "0";
        final String TANNISEI = "1";

        final Param _param;
        final Map _yearSchoolDivMap;

        SchoolDiv(final Param param, final DB2UDB db2) {
            _param = param;
            String sql = " SELECT YEAR, SCHOOLDIV FROM SCHOOL_MST ";
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql += " WHERE SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ";
            }
            _yearSchoolDivMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "YEAR", "SCHOOLDIV");
        }

        public String getSchooldiv(final String setYear, final PersonalInfo pInfo, final Student student) {
            String year = null; // 学年制 or 単位制を決める年度
            final boolean useEachSchoolMstSchoolDiv = _param._z010.in(Z010.kyoto);
            if (useEachSchoolMstSchoolDiv) {
                if (null != setYear) {
                    year = setYear;
                } else {
                    if (null != pInfo && !pInfo.getGakusekiList().isEmpty()) {
                        for (final Gakuseki g : pInfo.getGakusekiList()) {
                            if (!g.isNyugakumae()) {
                                year = g._year;
                                break;
                            }
                        }
                    }
                    if (null == year) {
                        year = _param._year;
                    }
                }
            } else {
                year = _param._year;
            }
            String schooldiv = null;
            if ("1".equals(_param.property(Property.useGakkaSchoolDiv)) && null != student) {
                schooldiv = student._yearMajorYdatSchooldivMap.get(year);
            }
            schooldiv =
                    defstr(schooldiv,
                            defstr(
                                    _yearSchoolDivMap.get(year),
                                    GAKUNENSEI)); // 指定年度の学校マスタの学校区分がなければ0:学年制
            if (_param._isOutputDebug) {
                if (useEachSchoolMstSchoolDiv) {
                    if (null != student) {
                        final String info = " getSchooldiv((setYear " + setYear +", gakusekiYear " + (null == pInfo ? "" : Gakuseki.gakusekiYearSet(pInfo.getGakusekiList()).toString()) + ") => " + year + ", " + (null == student ? "" : " studentYearMajor " + student._yearMajorYdatSchooldivMap) + ", " + _yearSchoolDivMap.get(year) + ") => " + schooldiv;
                        final Set set = Util.getMappedTreeSet(student._debugInfo, "SCHOOLDIV");
                        if (!set.contains(info)) {
                            log.info(info);
                            set.add(info);
                        }
                    }
                } else {
                    debugLogCheck(_param, "schoolDivDebug (" + year + ") = ", schooldiv);
                }
            }
            return schooldiv;
        }

        /**
         * 学年制か?
         * @return 学年制なら<code>true</code>
         */
        public boolean isGakunenSei(final String year, final PersonalInfo pInfo, final Student student) {
            return GAKUNENSEI.equals(getSchooldiv(year, pInfo, student));
        }

        /**
         * 単位制か?
         * @return 単位制なら<code>true</code>
         */
        public boolean isTanniSei(final String year, final PersonalInfo pInfo, final Student student) {
            return TANNISEI.equals(getSchooldiv(year, pInfo, student));
        }

        public Boolean isKoumokuGakunen(final Param param, final String year, final PersonalInfo pInfo, final Student student) {
            Boolean rtn = null;
            if (null != _param.property(Property.seitoSidoYorokuKoumokuMei)) {
                if ("1".equals(_param.property(Property.seitoSidoYorokuKoumokuMei))) {
                    rtn = Boolean.TRUE;
                } else if ("2".equals(_param.property(Property.seitoSidoYorokuKoumokuMei))) {
                    rtn = Boolean.FALSE;
                }
            }
            if (null == rtn) {
                if (null != student) {
                    if (isGakunenSei(year, pInfo, student) || student.certifSchool(param)._isGenkyuRyuchi) {
                        rtn = Boolean.TRUE;
                    } else if (isTanniSei(year, pInfo, student) && !student.certifSchool(param)._isGenkyuRyuchi) {
                        rtn = Boolean.FALSE;
                    }
                }
            }
            if (null == rtn) {
                rtn = Boolean.TRUE;
            }
            return rtn;
        }

        public boolean containsGakunenSei(final PersonalInfo pInfo, final Student student) {
            for (final Gakuseki gakuseki : pInfo.getGakusekiList()) {
                if (isGakunenSei(gakuseki._year, null, student)) {
                    return true;
                }
            }
            return false;
        }

    }

    protected static class FormInfo {
        private final Param _param;
        protected String _formname;
        protected Map<String, TreeMap<String, SvfField>> _fieldInfoMap = new HashMap<String, TreeMap<String, SvfField>>();
        private Set<String> _errors = new HashSet<String>();
        FormInfo(final Param param) {
            _param = param;
        }
        public void setForm(final Vrw32alp svf) {
            if (!_fieldInfoMap.containsKey(_formname)) {
                _fieldInfoMap.put(_formname, null);
                try {
                    _fieldInfoMap.put(_formname, new TreeMap<String, SvfField>(SvfField.getSvfFormFieldInfoMapGroupByName(svf)));
                } catch (Throwable t) {
                    log.warn(" no class SvfField.");
                }
            }
        }
        public SvfField getSvfField(final String fieldname, final boolean isLog) {
            try {
                final Map<String, SvfField> fieldMap = Util.getMappedMap(_fieldInfoMap, _formname);
                SvfField f = fieldMap.get(fieldname);
                if (null == f) {
                    final Map correctMap = Util.getMappedMap(_fieldInfoMap, _formname + ":CORRECT");
                    if (!correctMap.containsKey(fieldname)) {
                        for (final String cand : fieldMap.keySet()) {
                            if (cand.equalsIgnoreCase(fieldname)) {
                                if (_param.isOutputDebugField(fieldname)) {
                                    log.info(" correct fieldname:" + fieldname + " -> " + cand);
                                }
                                correctMap.put(fieldname, cand);
                                break;
                            }
                        }
                        if (!correctMap.containsKey(fieldname)) {
                            if (_param.isOutputDebugField(fieldname)) {
                                log.info(" not found correct fieldname:" + fieldname);
                            }
                            correctMap.put(fieldname, null);
                        }
                    }
                    f = fieldMap.get(correctMap.get(fieldname));
                }
                return f;
            } catch (Throwable t) {
                final String key = _formname + "." + fieldname;
                if (_errors.contains(key)) {
                    if (null == _formname) {
                        log.error(" form not set!");
                    } else {
                        if (isLog) {
                            if (_param.isOutputDebugField(fieldname)) {
                                log.warn(" svf field not found:" + key);
                            }
                            _errors.add(key);
                        }
                    }
                }

            }
            return null;
        }

        protected String[] getFieldGroupForData(final String[][] fieldGroups, final String data) {
            String[] fieldGroupFound = {};
            searchFieldGroup:
            for (int i = 0; i < fieldGroups.length; i++) {
                final String[] fieldGroup = fieldGroups[i];
                for (final String fieldname : fieldGroup) {
                    final SvfField svfField = getSvfField(fieldname, false);
                    if (null == svfField) {
                        continue searchFieldGroup;
                    }
                }
                fieldGroupFound = fieldGroup;
                if (dataFitsFieldGroup(data, fieldGroup)) {
                    return fieldGroup;
                }
            }
            return fieldGroupFound;
        }

        protected List<String> splitToFieldSize(final String[] fields, final String data) {
            final List<Integer> ketas = getFieldKetaList(fields);
            if (ketas.size() == 0) {
                return Collections.emptyList();
            }
            final List<StringBuffer> wrk = new ArrayList<StringBuffer>();
            StringBuffer currentLine = null;
            for (final char ch : data.toCharArray()) {
                if (null == currentLine) {
                    currentLine = new StringBuffer();
                    wrk.add(currentLine);
                }
                if (ch == '\n') {
                    currentLine = new StringBuffer();
                    wrk.add(currentLine);
                    continue;
                }

                if (wrk.size() <= ketas.size()) {
                    final String chs = String.valueOf(ch);
                    final int lineKeta = wrk.size() < ketas.size() ? ketas.get(wrk.size() - 1) : ketas.get(ketas.size() - 1); // 行あふれした場合最後のフィールドを使用しておく
                    if (lineKeta < getMS932ByteLength(currentLine.toString() + chs)) {
                        currentLine = new StringBuffer();
                        wrk.add(currentLine);
                    }
                    currentLine.append(chs);
                } else {
                    break;
                }
            }
            final List<String> rtn = new ArrayList<String>();
            for (final StringBuffer stb : wrk) {
                rtn.add(stb.toString());
            }
            return rtn;
        }

        protected List<Integer> getFieldKetaList(final String[] fields) {
            final List<Integer> ketas = new ArrayList<Integer>();
            for (final String fieldname : fields) {
                final SvfField svfField = getSvfField(fieldname, false);
                if (null == svfField) {
                    continue;
                }
                ketas.add(svfField._fieldLength);
            }
            return ketas;
        }

        protected boolean dataFitsFieldGroup(final String data, final String[] fieldGroup) {
            List<String> splitToFieldSize = splitToFieldSize(fieldGroup, data);
            final boolean isFits = splitToFieldSize.size() <= fieldGroup.length;
            if (_param._isOutputDebugSvfOut) {
                log.info(" isFits? " + isFits + ", fieldGroup = " + ArrayUtils.toString(fieldGroup) + ", ketas = " + getFieldKetaList(fieldGroup) + ", data = " + data);
            }
            return isFits;
        }

        protected String getFieldForData(final List<String> fields, final String data) {
            final int datasize = getMS932ByteLength(data);
            String fieldFound = null;
            searchField:
            for (int i = 0; i < fields.size(); i++) {
                final String fieldname = fields.get(i);
                final SvfField svfField = getSvfField(fieldname, false);
                if (null == svfField) {
                    continue searchField;
                }
                fieldFound = fieldname;
                if (datasize <= svfField._fieldLength) {
                    return fieldname;
                }
            }
            return fieldFound;
        }

        public Map<String, String> getFieldStatusMap(final String fieldname, final boolean isLog) {
            final Map<String, String> m = new HashMap<String, String>();
            try {
                SvfField f = getSvfField(fieldname, isLog);
                if (null != f) {
                    m.put("X", String.valueOf(f.x()));
                    m.put("Y", String.valueOf(f.y()));
                    m.put("Size", String.valueOf(f.size()));
                    m.put("Keta", String.valueOf(f._fieldLength));
                    m.put("Direction", String.valueOf(f.getAttributeMap().get("Direction"))); // 0:横 1:縦
                }
            } catch (Throwable t) {
            }
            return m;
        }


        public Map<String, String> getFieldStatusMap(final String fieldname) {
            return getFieldStatusMap(fieldname, true);
        }

        public boolean hasField(final String fieldname) {
            return null != getSvfField(fieldname, false);
        }

        public KNJSvfFieldInfo getFieldInfo(final String fieldName, final String fieldNameY1, final String fieldNameY2, final int defCharSize, final int minnum) {
            final KNJSvfFieldInfo i = new KNJSvfFieldInfo();
            try {
                final SvfField f = getSvfField(fieldName, true);
                i._field = fieldName;
                int keta = -1;
                if (hasField(fieldName)) {
                    keta = f._fieldLength;
                    final double charSize = f.size();
                    i._x1 = f.x();
                    i._x2 = i._x1 + (int) KNJSvfFieldModify.fieldWidth("getFieldInfo " + fieldName, charSize, 1, keta);
                    i._height = defCharSize;
                    i._ystart = f.y();
                    i._maxnum = keta;
                }
                if (null != fieldNameY1) {
                    if (hasField(fieldNameY1)) {
                        i._field1 = fieldNameY1;
                        i._field1Status = getFieldStatusMap(fieldNameY1);
                        if (i._field1Status.isEmpty()) {
                        } else {
                            i._ystart1 = getSvfField(fieldNameY1, true).y();
                        }
                    }
                }
                if (null != fieldNameY2) {
                    if (hasField(fieldNameY2)) {
                        i._field2 = fieldNameY2;
                        i._field2Status = getFieldStatusMap(fieldNameY2);
                        if (i._field2Status.isEmpty()) {
                        } else {
                            i._ystart2 = getSvfField(fieldNameY2, true).y();
                        }
                    }
                }
                i._minnum = minnum;
                if (_param.isOutputDebugField(fieldName)) {
                    if (hasField(fieldName)) {
                        log.info(fieldName + ":: x1 = " + i._x1 + ", x2 = " + i._x2 + ",  height = " + i._height + ", keta = " + keta + " , ystart = " + i._ystart + (null != fieldNameY1 ? (", ystart1 = " + i._ystart1) : "") + (null != fieldNameY2 ? (", ystart2 = " + i._ystart2) : ""));
                    } else {
                        log.info(" no field " + fieldName);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } catch (Throwable t) {
            }
            return i;
        }

        /**
         * フィールドのリストからデータの桁が収まるインデクスを得る
         * @param dataKeta データの桁
         * @param candidateFieldList フィールドのリスト
         * @return Tuple.of(フィールドがあるインデクス、決定したインデクス);
         */
        protected Integer getPrintFieldIndex(final int dataKeta, final List<String> candidateFieldList) {
            Integer lastValidIdx = null;
            Integer ketteiIdx = null;
            for (int i = 0; i < candidateFieldList.size(); i++) {
                final int len = getFieldLength(candidateFieldList.get(i), -1);
                if (len == -1) {
                    // フィールドがない
                    continue;
                }
                lastValidIdx = new Integer(i);
                if (dataKeta <= len) {
                    ketteiIdx = new Integer(i);
                    break;
                }
            }
            if (null != ketteiIdx) {
                return ketteiIdx;
            } else if (null != lastValidIdx) {
                return lastValidIdx;
            }
            return null;
        }

        public int getFieldLength(final String fieldname, int defval) {
            int length = defval;
            try {
                length = Integer.parseInt(KnjDbUtils.getString(getFieldStatusMap(fieldname), "Keta"));
            } catch (Throwable t) {
            }
            return length;
        }

        public int getFieldX(final String fieldname, int defval) {
            int x = defval;
            try {
                x = Integer.parseInt(KnjDbUtils.getString(getFieldStatusMap(fieldname), "X"));
            } catch (Throwable t) {
            }
            return x;
        }

        public double getFieldCharSize(final String fieldname, double defval) {
            double x = defval;
            try {
                x = Double.parseDouble(KnjDbUtils.getString(getFieldStatusMap(fieldname), "Size"));
            } catch (Throwable t) {
            }
            return x;
        }
    }

    static class FormRecordInfo {
        private Param _param;
        private SvfForm _svfForm;
        private Map<SvfForm.SubForm, SvfForm.Record> _currentSubFormRecord;
        private Map<SvfForm.SubForm, List<SvfForm.Record>> _currentSubFormOutputRecordListMap;

        public FormRecordInfo(final Param param, final SvfForm svfForm) {
            _param = param;
            _svfForm = svfForm;
            newPage();
        }

        public void output(final String fieldname) {
            Tuple<SvfForm.Record, SvfForm.SubForm> recordAndSubForm = getRecordAndSubForm(fieldname);
            if (null == recordAndSubForm) {
                return;
            }
            final SvfForm.Record record = recordAndSubForm._first;
            final SvfForm.SubForm subForm = recordAndSubForm._second;
            final SvfForm.Record oldRecord = _currentSubFormRecord.get(subForm);
            if (null != oldRecord && oldRecord != record) {
                log.warn("重複record : " + oldRecord._name + " -> " + record._name);
            }
            _currentSubFormRecord.put(subForm, record);
        }

        public void newPage() {
            if (_param._isOutputDebugFormRecordInfo) {
                log.info("new page");
            }
            _currentSubFormOutputRecordListMap = new HashMap<SvfForm.SubForm, List<SvfForm.Record>>();
            _currentSubFormRecord = new HashMap<SvfForm.SubForm, SvfForm.Record>();
        }

        public void endRecord() {
            for (final Map.Entry<SvfForm.SubForm, SvfForm.Record> subFormRecord : _currentSubFormRecord.entrySet()) {
                final SvfForm.SubForm subForm = subFormRecord.getKey();
                final SvfForm.Record record = subFormRecord.getValue();

                if (getCurrentSubFormConsumed(record) + record.getHeight() > subForm.getHeight()) {
                    newPage();
                }

                getMappedList(_currentSubFormOutputRecordListMap, subForm).add(record);
                _currentSubFormRecord.remove(subForm);
            }
        }

        private Tuple<SvfForm.Record, SvfForm.SubForm> getRecordAndSubForm(final String fieldname) {
            SvfForm.Field field = _svfForm.getField(fieldname);
            if (null == field) {
                if (_param._isOutputDebugFormRecordInfo) {
                    _param.logOnce("no such field : " + fieldname + " / " + _svfForm._formFile.getName());
                }
                return null;
            }
            SvfForm.Record record = _svfForm.getRecordOfField(field);
            if (null == record) {
                if (_param._isOutputDebugFormRecordInfo) {
                    _param.logOnce("no record field : " + fieldname + " / " + _svfForm._formFile.getName());
                }
                return null;
            }
            SvfForm.SubForm subForm = record.getSubForm();
            if (null == subForm) {
                if (_param._isOutputDebug) {
                    log.warn("record has no subform : " + record + " / " + _svfForm._formFile.getName());
                }
                return null;
            }
            return Tuple.of(record, subForm);
        }

        /**
         * 現在のページで<code>fieldname</code>のレコードが収まる行
         * @param fieldname
         * @return 現在のページで<code>fieldname</code>のレコードが収まる行
         */
        public double getCurrentPageRestRecordCount(final String fieldname) {
            Tuple<SvfForm.Record, SvfForm.SubForm> recordAndSubForm = getRecordAndSubForm(fieldname);
            if (null == recordAndSubForm) {
                if (_param._isOutputDebugFormRecordInfo) {
                    log.info("no record field : " + fieldname);
                }
                return -1;
            }
            final int currentPageConsumed = getCurrentPageConsumed(fieldname);
            final SvfForm.Record record = recordAndSubForm._first;
            final SvfForm.SubForm subForm = recordAndSubForm._second;
            final int restHeight = subForm.getHeight() - currentPageConsumed;
            final double count = restHeight / record.getHeight();
            if (_param._isOutputDebugFormRecordInfo) {
                log.info("subForm = " + subForm._name + " count = " + count + ", subForm height = " + subForm.getHeight() + ", record height = " + record.getHeight());
            }
            return count;
        }

        /**
         * 現在のページで<code>fieldname</code>のサブフォームに出力したレコードの高さもしくは幅
         * @param fieldname
         * @return 現在のページで出力したレコードの高さもしくは幅
         */
        public int getCurrentPageConsumed(final String fieldname) {
            Tuple<SvfForm.Record, SvfForm.SubForm> recordAndSubForm = getRecordAndSubForm(fieldname);
            if (null == recordAndSubForm) {
                return -1;
            }
            final SvfForm.Record record = recordAndSubForm._first;
            final int currentSubFormConsumed = getCurrentSubFormConsumed(record);
            return currentSubFormConsumed;
        }

        private int getCurrentSubFormConsumed(final SvfForm.Record record) {
            final SvfForm.SubForm subForm = record.getSubForm();
            int consumed = 0;
            for (final SvfForm.Record outputRecord : getMappedList(_currentSubFormOutputRecordListMap, subForm)) {
                consumed += outputRecord.getHeight();
            }
            if (_param._isOutputDebugFormRecordInfo) {
                log.info(" subForm " + subForm._name + " consumed = " + consumed + ", record count = " + getMappedList(_currentSubFormOutputRecordListMap, subForm).size());
            }
            return consumed;
        }

        /**
         * <code>formRecordList</code>の出力する高さもしくは幅
         * @param formRecordList
         * @return <code>formRecordList</code>の出力する高さもしくは幅
         */
        public int getConsumed(final List<FormRecord> formRecordList) {
            final FormRecordInfo formRecordInfo = new FormRecordInfo(_param, _svfForm);
            String fieldname = null;
            for (final FormRecord formRecord : formRecordList) {
                for (final String recFieldname : formRecord.fieldSet()) {
                    if (StringUtils.isEmpty(fieldname) && !StringUtils.isEmpty(formRecord.data(recFieldname))) {
                        fieldname = recFieldname;
                    }
                    formRecordInfo.output(recFieldname);
                }
                formRecordInfo.endRecord();
            }
            return null == fieldname ? -1 : formRecordInfo.getCurrentPageConsumed(fieldname);
        }
    }

    static class Form {

        final Vrw32alp _svf;
        final Param _param;
        final FormInfo _formInfo;
        private Set _noFieldSet = new TreeSet();
        Map<String, File> _createFormFiles = new HashMap<String, File>();
        Map<String, SvfForm> _createSvfForms = new HashMap<String, SvfForm>();
        FormRecordInfo _formRecordInfo = null;

        public Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _formInfo = new FormInfo(param);
            _param = param;
        }

        public void svfVrEndRecord() {
            if (_param._isCsv) {
                return;
            }
            if (_param._isOutputDebugSvfOut) {
                log.info(" VrEndRecord");
            }
            if (null != _formRecordInfo) {
                _formRecordInfo.endRecord();
            }
            _svf.VrEndRecord();
        }

        public void svfVrEndPage() {
            if (_param._isCsv) {
                return;
            }
            if (_param._isOutputDebugSvfOut) {
                log.info(" VrEndPage");
            }
            _svf.VrEndPage();
        }

        public int svfVrsOutNotNull(final String field, final String data) {
            if (null == data || data.length() == 0) {
                return 0;
            }
            return svfVrsOut(field, data);
        }

        public int svfVrsOut(final String field, final String data) {
            if (null == field) {
                return 0;
            }
            final Map<String, String> stat = _formInfo.getFieldStatusMap(field, true);
            if (stat.isEmpty()) {
                if (_param._isOutputDebugSvfOut) {
                    log.warn(" フィールドがない [" + field + "] " + data);
                }
                _noFieldSet.add(field);
                return -1;
            }
            final String d = validate(field, data);
            if (_param._isOutputDebugSvfOut) {
                log.info(" VrsOut [" + field + "] " + d);
            }
            if (null != _formRecordInfo) {
                _formRecordInfo.output(field);
            }
            return _svf.VrsOut(field, d);
        }

        public int svfVrImageOut(final String field, final String path) {
            if (null == path) {
                if (_param._isOutputDebugSvfOut) {
                    log.warn(" ファイルがない [" + field + "] ");
                }
                return -1;
            }
            if (_param._isOutputDebugSvfOut) {
                log.info(" VrImageOut [" + field + "] " + path);
            }
            return _svf.VrsOut(field, path);
        }

        public int svfVrsOutn(final String field, final int gyo, final String data) {
            if (null == field) {
                return 0;
            }
            final Map<String, String> stat = _formInfo.getFieldStatusMap(field, true);
            if (stat.isEmpty()) {
                if (_param._isOutputDebugSvfOut) {
                    log.warn(" フィールドがない [" + field + ", " + gyo + "] " + data);
                }
                _noFieldSet.add(field + "." + gyo);
                return -1;
            }
            final String d = validate(field, data);
            if (_param._isOutputDebugSvfOut) {
                log.info(" VrsOut [" + field + ", " + gyo + "] " + d);
            }
            if (null != _formRecordInfo) {
                _formRecordInfo.output(field);
            }
            return _svf.VrsOutn(field, gyo, d);
        }

        public int svfVrAttribute(final String field, final String attr) {
            if (null == field) {
                return 0;
            }
            final Map<String, String> stat = _formInfo.getFieldStatusMap(field, true);
            if (stat.isEmpty()) {
                _noFieldSet.add(field);
                return -1;
            }
            if (_param.isOutputDebugField(field)) {
                log.info(" vrAttribute(" + field + ", " + attr + ")");
            }
            return _svf.VrAttribute(field, attr);
        }

        public int svfVrAttributen(final String field, final int gyo, final String attr) {
            if (null == field) {
                return 0;
            }
            final Map<String, String> stat = _formInfo.getFieldStatusMap(field, true);
            if (stat.isEmpty()) {
                _noFieldSet.add(field);
                return -1;
            }
            if (_param.isOutputDebugField(field)) {
                log.info(" vrAttributen(" + field + ", " + gyo + ", " + attr + ")");
            }
            return _svf.VrAttributen(field, gyo, attr);
        }

        public void svfVrSetForm(final String form, final int n) {
            _formInfo._formname = form;
            log.info(" setForm = " + _formInfo._formname);
            final int setFormReturn = _svf.VrSetForm(_formInfo._formname, n);
            if (setFormReturn != 0) {
                throw new IllegalStateException("フォームがありません :" + _formInfo._formname + ", " + setFormReturn);
            }
            _formInfo.setForm(_svf);
            if (n == 4) {
                final SvfForm svfForm = new SvfForm(new File(_svf.getPath(_formInfo._formname)));
                svfForm.readFile();
                _formRecordInfo = new FormRecordInfo(_param, svfForm);
            }
            _noFieldSet.clear();
        }

        public Map<String, Integer> getFieldRepeatCountMap() {
            Map<String, Integer> rtn = new HashMap<String, Integer>();
            try {
                final Map<String, SvfField> infoMap = Util.getMappedMap(_formInfo._fieldInfoMap, _formInfo._formname);
                for (final Map.Entry<String, SvfField> e : infoMap.entrySet()) {
                    final String fieldName = e.getKey();
                    final SvfField field = e.getValue();
                    if (field._fieldRepeatCount > 0) {
                        rtn.put(fieldName, new Integer(field._fieldRepeatCount));
                    }
                }
            } catch (Throwable t) {
            }
            return rtn;
        }

        public int getFieldRepeatCount(final String fieldname, final int defval) {
            int repeatCount = defval;
            try {
                SvfField f = Util.getMappedMap(_formInfo._fieldInfoMap, _formInfo._formname).get(fieldname);
                repeatCount = f._fieldRepeatCount;
            } catch (Throwable t) {
                log.info(" failsafe = " + t + ", fieldname = " + fieldname);
            }
            return repeatCount;
        }

        public double getCurrentPageRestRecordCount(final String fieldname) {
            if (null == _formRecordInfo) {
                throw new IllegalStateException("getCurrentPageRestRecordCount not available : " + _formInfo._formname + ", fieldname = " + fieldname);
            }
            return _formRecordInfo.getCurrentPageRestRecordCount(fieldname);
        }

        public int getCurrentPageConsumed(final String fieldname) {
            if (null == _formRecordInfo) {
                throw new IllegalStateException("getCurrentPageConsumed not available : " + _formInfo._formname + ", fieldname = " + fieldname);
            }
            return _formRecordInfo.getCurrentPageConsumed(fieldname);
        }

        public double getConsumed(final List<FormRecord> formRecordList) {
            if (null == _formRecordInfo) {
                throw new IllegalStateException("getConsumed not available : " + _formInfo._formname);
            }
            return _formRecordInfo.getConsumed(formRecordList);
        }

        public void recordInfoNewPage() {
            _formRecordInfo.newPage();
        }

        public String validate(final String field, final String data) {
            final Map stat = _formInfo.getFieldStatusMap(field, true);
            if ("1".equals(stat.get("Direction")) && !StringUtils.isEmpty(data)) {
                final int len = _formInfo.getFieldLength(field, 1) / 2; // 縦書きの"Keta"は文字数*2
                if (len < data.length()) {
                    if (null != _param._outputDebugFieldList) {
                        log.warn("縦書きフィールド文字数不足:印字数変更: " + field + ", " + data + ", 文字数" + len);
                    }
                    return data.substring(0, len);
                }
            }
            return data;
        }

        public void closeSession() {
            if (_param._isOutputDebug) {
                if (!_noFieldSet.isEmpty()) {
                    log.warn("フィールドがない " + _formInfo._formname + ", " + Util.listString(_noFieldSet, 0));
                }
            }
            _noFieldSet.clear();
        }

        public void close() {
            for (final File file : _createFormFiles.values()) {
                if (null != file) {
                    boolean deleted = false;
                    if (!_param._isOutputDebugFormCreate) {
                        deleted = file.delete();
                    }
                    if (_param._isOutputDebug) {
                        log.info(" delete file " + file.getAbsolutePath() + " : " + deleted);
                    }
                }
            }
        }
    }

    static class FormRecord {
        private final Map<String, String> _data;
        private final Map<String, String> _attribute;

        public FormRecord() {
            _data = new HashMap<String, String>();
            _attribute = new HashMap<String, String>();
        }
        public static FormRecord nextRecord(final List<FormRecord> formRecordList) {
            final FormRecord formRecord = new FormRecord();
            formRecordList.add(formRecord);
            return formRecord;
        }
        public void setData(final String field, final String data) {
            if (null == field) {
                return;
            }
            _data.put(field, data);
        }
        public void setAttribute(final String field, final String attribute) {
            if (null == field) {
                return;
            }
            _attribute.put(field, attribute);
        }
        public String data(final String field) {
            return _data.get(field);
        }
        public String attribute(final String field) {
            return _attribute.get(field);
        }
        public Set<String> fieldSet() {
            final Set<String> fieldSet = new HashSet<String>();
            fieldSet.addAll(_data.keySet());
            fieldSet.addAll(_attribute.keySet());
            return fieldSet;
        }
        public boolean isNotBlank() {
            boolean notBlank = false;
            for (final String field : fieldSet()) {
                final String data = data(field);
                if (!StringUtils.isBlank(data)) {
                    notBlank = true;
                    break;
                }
            }
            return notBlank;
        }
        public String toString() {
            return "FormRecord(data = " + _data + ", attribute = " + _attribute + ")";
        }
    }

    protected static abstract class KNJA130_0 {

        /** 中央寄せ */
        static final String ATTR_CENTERING = "Hensyu=3";
        /** 左詰 */
        static final String ATTR_LEFT = "Hensyu=2";
        /** 赤文字 */
        static final String ATTR_COLOR_RED = "Palette=9";

        private Param _param;

        protected boolean nonedata; // データ有りフラグ

        protected final Form _form;

        protected final int charSize11 = (int) KNJSvfFieldModify.charHeightPixel("", 11.0);

        protected int _gradeLineMax;
        protected boolean _isPrintEduDiv2CharsPerLine; // 様式2表の教科専門区分文言は1行2文字ずつ表示

        KNJA130_0(final Vrw32alp svf, final Param param) {
            _param = param;
            _form = new Form(svf, _param);
        }

        protected int getSvfFormFieldLength(final String fieldname, final int def) {
            if (!_form._createSvfForms.containsKey(_form._formInfo._formname)) {
                final String path = _form._svf.getPath(_form._formInfo._formname);
                final SvfForm svfForm = new SvfForm(new File(path));
                _form._createSvfForms.put(_form._formInfo._formname, svfForm.readFile() ? svfForm : null);
            }

            final SvfForm svfForm = _form._createSvfForms.get(_form._formInfo._formname);
            if (null == svfForm) {
                log.info(" no svfForm for " + _form._formInfo._formname + " / " + _form._createSvfForms.keySet());
                return def;
            }
            final SvfForm.Field field = svfForm.getField(fieldname);
            if (null == field) {
                log.info("no svfForm.field : " + _form._formInfo._formname + ", " + fieldname);
                return def;
            }
            return field._fieldLength;
        }

        protected int svfVrsOut(final String field, final String data) {
            return _form.svfVrsOut(field, data);
        }

        protected int svfVrsOutForData(final List<String> fields, final String data) {
            return _form.svfVrsOut(_form._formInfo.getFieldForData(fields, data), data);
        }

        protected int svfVrsOutSplit(final String[] fields, final String data) {
            final List<String> split = _form._formInfo.splitToFieldSize(fields, data);
            int rtn = 0;
            for (int i = 0; i < Math.min(fields.length, split.size()); i++) {
                rtn = svfVrsOut(fields[i], split.get(i));
            }
            return rtn;
        }
        protected int svfVrsOutGroupForData(final String[][] fieldGroups, final String data) {
            return svfVrsOutSplit(_form._formInfo.getFieldGroupForData(fieldGroups, data), data);
        }

        protected int svfVrImageOut(final String field, final String path) {
            return _form.svfVrImageOut(field, path);
        }

        protected int svfVrsOutNotNull(final String field, final String data) {
            return _form.svfVrsOutNotNull(field, data);
        }

        protected int svfVrsOutn(final String field, final int gyo, final String data) {
            return _form.svfVrsOutn(field, gyo, data);
        }

        protected int svfVrAttribute(final String field, final String attribute) {
            if (null == attribute) {
                return -1;
            }
            return _form.svfVrAttribute(field, attribute);
        }

        protected int svfVrAttributen(final String field, final int gyo, final String attribute) {
            if (null == attribute) {
                return -1;
            }
            return _form.svfVrAttributen(field, gyo, attribute);
        }

        protected void svfVrSetForm(final String form, final int n) {
            closeSession();
            _form.svfVrSetForm(form, n);
        }

        protected void svfVrEndPage() {
            _form.svfVrEndPage();
        }

        protected void svfVrEndRecord() {
            _form.svfVrEndRecord();
        }

        protected double getCurrentPageRestRecordCount(final String fieldname) {
            return _form.getCurrentPageRestRecordCount(fieldname);
        }

        protected int getCurrentPageConsumed(final String fieldname) {
            return _form.getCurrentPageConsumed(fieldname);
        }

        protected void recordInfoNewPage() {
            _form.recordInfoNewPage();
        }

        protected void closeSession() {
            _form.closeSession();
        }

        protected void close() {
            _form.close();
        }

        protected boolean hasField(final String fieldname) {
            return _form._formInfo.hasField(fieldname);
        }

        protected static String nendoWareki(final DB2UDB db2, final String year) {
            if (!NumberUtils.isDigits(year)) {
                return "";
            }
            return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
        }

        /**
         * 日付の和暦年度文言を得る
         * "2019-04-30" -> "平成31年度"
         * @param db2
         * @param date
         * @return
         */
        protected static String dateNendoWareki(final DB2UDB db2, final String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            try {
                final String[] format = KNJ_EditDate.tate_format4(db2, date);
                final String gengo = defstr(format[0]);
                String nen = defstr(format[1]);
                if (NumberUtils.isDigits(month(date)) && Integer.parseInt(month(date)) < 4) {
                    if (NumberUtils.isDigits(nen)) {
                        final int nenInt = Integer.parseInt(nen) - 1;
                        if (nenInt == 1) {
                            nen = "元";
                        } else {
                            nen = String.valueOf(nenInt);
                        }
                    }
                }
                return gengo + nen + "年度";
            } catch (Exception e) {
                log.error("exception! date = " + date, e);
            }
            return "";
        }

        public static int pos(final int p, final int max) {
            return 0 == p % max ? max : p % max;
        }

        /**
         * 打ち消し線
         * @param field
         * @param keta
         * @param other その他属性
         */
        protected void svfUchikeshi(final String field, final int keta, String other) {
            if (keta > 0) {
                svfVrAttribute(field, defstr(other) + "UnderLine=(0,3,5), Keta=" + String.valueOf(Math.min(keta, _form._formInfo.getFieldLength(field, 9999)))); // 打ち消し線
            }
        }

        protected void VrsOutnToken(final String field, final int keta, final int gyo, final String s) {
            final String[] token = Util.get_token(_param, s, keta, gyo);
            for (int i = 0; i < token.length; i++) {
                svfVrsOutn(field, i + 1, token[i]);
            }
        }

        protected void VrsOutToken(final String[] field, final int keta, final String s) {
            final String[] token = Util.get_token(_param, s, keta, field.length);
            for (int i = 0; i < token.length; i++) {
                svfVrsOut(field[i], token[i]);
            }
        }

        protected String eduDiv(final Param param, final String name, final int idx) {
            final int k = _isPrintEduDiv2CharsPerLine ? 2 : 1;
            final String eduDiv;
            if (null == name || name.length() <= idx * k) {
                eduDiv = "";
            } else {
                eduDiv = name.substring(idx * k, Math.min((idx + 1) * k, name.length()));
            }
            if (param._isOutputDebugInner) {
                log.info(" eduDiv = " + eduDiv);
            }
            return eduDiv;
        }

        /**
         * 所見等データを印刷します。
         * @param svf
         * @param field SVFフィールド名
         * @param data 編集元の文字列
         * @param keta 行当りの文字数（Byte)
         * @param gyo 行数
         * @param param
         * @param i
         */
        protected void printSvfRenban(final String field, final String data, final int keta, final int gyo) {
            printSvfRenban(field, Util.getTokenList(_param, data, keta, gyo));
        }

        protected void printSvfRenban(final String field, final String data, final KNJPropertiesShokenSize size) {
            printSvfRenban(field, data, size.getKeta(), size._gyo);
        }

        protected void printSvfRenban(final String field, final List<String> list) {
            if (null != list) {
                for (int i = 0 ; i < list.size(); i++) {
                    svfVrsOutn(field, i + 1, list.get(i));
                }
            }
        }
    }

    protected static class Indexed<T> {

        final int _idx;
        final T _val;
        public Indexed(final int idx, final T val) {
            _idx = idx;
            _val = val;
        }

        public String idxStr() {
            return String.valueOf(_idx);
        }

        public static <T> List<Indexed<T>> of(final List<T> list) {
            return of(0, list);
        }

        public static <T> List<Indexed<T>> of(final int startIdx, final List<T> list) {
            final List<Indexed<T>> rtn = new ArrayList<Indexed<T>>();
            int idx = startIdx;
            for (final T t : list) {
                rtn.add(new Indexed(idx, t));
                idx += 1;
            }
            return rtn;
        }
    }

    protected static class Mapped {
        protected final Map<String, String> _map;
        Mapped(final Map<String, String> map) {
            if (null == map) {
                _map = new HashMap<String, String>();
            } else {
                _map = map;
            }
        }
        Mapped(final String[] keys) {
            if (null == keys || keys.length == 0) {
                throw new IllegalArgumentException(ArrayUtils.toString(keys));
            }
            _map = new HashMap<String, String>();
            for (int i = 0; i < keys.length; i++) {
                _map.put(keys[i], null);
                if (null == keys[i]) {
                    throw new IllegalArgumentException(ArrayUtils.toString(keys));
                }
            }
        }
        String get(final String key) {
            return KnjDbUtils.getString(_map, key);
        }
        void putAll(final Map<String, String> m) {
            if (null != m) {
                for (final Iterator it = m.keySet().iterator(); it.hasNext();) {
                    final Object okey = it.next();
                    if (!(okey instanceof String)) {
                        continue;
                    }
                    final String key = (String) okey;
                    if (key.startsWith("__")) {
                        continue;
                    }
                    if (!_map.containsKey(key)) {
                        // スタックトレース表示
                        try {
                            throw new IllegalStateException("not contained key:" + key + " in " + _map.keySet());
                        } catch (Exception e) {
                            log.warn("exception!", e);
                        }
                    }
                    _map.put(key, m.get(key));
                }
            }
            //log.info(" create " + _map.keySet());
        }
    }

    protected static class Gakuseki {
        String _year;

        protected Gakuseki(final String year) {
            _year = year;
        }

        protected static int gakusekiMinYear(final List<? extends Gakuseki> gakusekiList) {
            int min = Integer.MAX_VALUE;
            for (final String year : gakusekiYearSet(gakusekiList)) {
                if (null == year) {
                    continue;
                }
                final int iyear = Integer.parseInt(year);
                if (0 != iyear) {
                    min = Math.min(min, iyear);
                }
            }
            return min == Integer.MAX_VALUE ? -1 : min;
        }

        protected static int gakusekiMaxYear(final List<? extends Gakuseki> gakusekiList) {
            int max = Integer.MIN_VALUE;
            for (final String year : gakusekiYearSet(gakusekiList)) {
                if (null == year) {
                    continue;
                }
                final int iyear = Integer.parseInt(year);
                if (0 != iyear) {
                    max = Math.max(max, iyear);
                }
            }
            return max == Integer.MIN_VALUE ? -1 : max;
        }

        protected static TreeSet<String> gakusekiYearSet(final List<? extends Gakuseki> gakusekiList) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        protected boolean isNyugakumae() {
            return isAnotherSchoolYear(_year);
        }

        protected String[] arNendo(final Param param, final String nendo) {
            final int len = nendo.length();
            final String[] arNendo = {"", "", "年度"};
            if (!isNyugakumae() && param._isSeireki) {
                try {
                    arNendo[0] = nendo.substring(0, Math.min(4, len));
                } catch (Exception e) {
                    log.error("exception! nendo = " + nendo + ", array = " + ArrayUtils.toString(arNendo), e);
                }
            } else {
                try {
                    arNendo[0] = nendo.substring(0, Math.min(2, len));
                    arNendo[1] = nendo.substring(Math.min(2, len), Math.max(0, Math.max(len - 2, Math.min(2, len))));
                } catch (Exception e) {
                    log.error("exception! nendo = " + nendo + ", array = " + ArrayUtils.toString(arNendo), e);
                }
            }
            return arNendo;
        }

        public static <G extends Gakuseki> TreeMap<String, G> getYearGakusekiMap(final Collection<G> gakusekis) {
            final TreeMap<String, G> yearGakusekiMap = new TreeMap<String, G>();
            for (final G gakuseki : gakusekis) {
                yearGakusekiMap.put(gakuseki._year, gakuseki);
            }
            return yearGakusekiMap;
        }
    }

    /**
     * 観点の教科
     */
    protected static class ClassView {
        final ClassMst _classMst;
        final List<ViewSubclass> _viewSubclassList;

        public ClassView(final ClassMst classMst) {
            _classMst = classMst;
            _viewSubclassList = new ArrayList<ViewSubclass>();
        }

        public static ClassView enabled(final ClassView cv, final List<String> pageYears) {
            final List<ViewSubclass> rtn = new ArrayList<ViewSubclass>();
            for (final ViewSubclass vs : cv._viewSubclassList) {
                final ViewSubclass enabled = ViewSubclass.enabled(vs, pageYears);
                if (null != enabled) {
                    rtn.add(enabled);
                }
            }
            if (rtn.isEmpty()) {
                return null;
            }
            ClassView enabled = new ClassView(cv._classMst);
            enabled._viewSubclassList.addAll(rtn);
            return enabled;
        }

        /**
         * 同一教科の科目コードを得る
         * @param student
         * @param classview
         * @return
         */
        private TreeSet<String> getViewSubclassCdSet(final List<String> pageYears) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final ViewSubclass viewSubclass : _viewSubclassList) {
                set.add(viewSubclass._subclassMst._subclasscd);
            }
            return set;
        }

        public int getViewNum(final String subclasscd, final List<String> pageYears) {
            int c = 0;
            if (getViewSubclassCdSet(pageYears).size() <= 1) {
                for (final ViewSubclass viewSubclass : _viewSubclassList) {
                    c += viewSubclass._viewList.size();
                }
            } else {
                for (final ViewSubclass viewSubclass : _viewSubclassList) {
                    if (subclasscd.equals(viewSubclass._subclassMst._subclasscd)) {
                        c += viewSubclass._viewList.size();
                    }
                }
            }
            return c;
        }

//        // 教科名のセット
//        private String setViewClassname(final String classname, final String subclasscd, final Param param, final List<String> pageYears) {
//            if (classname == null) {
//                return "";
//            }
//            final int viewnum = getViewNum(subclasscd, pageYears);
//            if (viewnum == 0) {
//                return classname;
//            }
//            final int newviewnum;
//            if (classname.length() <= viewnum && !"1".equals(param.property(Property.seitoSidoYorokuCyugakuKantenNoBlank))) {
//                newviewnum = viewnum + 1;  // 教科間の観点行に１行ブランクを挿入
//            } else {
//                newviewnum = viewnum;
//            }
//            final String newclassname;
//
//            if (classname.length() < newviewnum) {
//                final int i = (newviewnum - classname.length()) / 2;
//                String space = "";
//                for (int j = 0; j < i; j++) {
//                    space = " " + space;
//                }  // 教科名のセンタリングのため、空白を挿入
//                newclassname = space + classname;
//            } else {
//                newclassname = classname;
//            }
//            return newclassname;
//        }

        public String toString() {
            return "ViewClass(" + _classMst + " e = " + _classMst._electdiv + ")";
        }

        private static ClassView getClassView(final Param param, final List<ClassView> classViewList, final String classcd, final String classname, final String electdiv) {
            if (null == classcd) {
                return null;
            }
            ClassView classView = null;
            for (final ClassView classView0 : classViewList) {
                if (classView0._classMst.getKey(param).equals(classcd) && classView0._classMst._classname.equals(classname) && defstr(classView0._classMst._electdiv, "0").equals(electdiv)) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }

        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
//        public static List<ClassView> load(final DB2UDB db2, final Param param, final PersonalInfo pInfo, final SchregRegdDat regd) {
        public static List<ClassView> loadClassViewList(final DB2UDB db2, final Param param, final PersonalInfo pInfo) {
            final List<ClassView> classViewList = new ArrayList<ClassView>();
            final String psKey = "PS_VIEW";
            if (null == param._psMap.get(psKey)) {
                final String sql = getViewRecordSql(param);
                if (param._isOutputDebugQuery) {
                    log.info(" ClassView sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }

//            final SchregRegdDat regd = SchregRegdDat.getMaxSemesterRegd(pInfo._student._regdList, param._year);

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { pInfo._schregno, pInfo._schregno, })) {

                //教科コードの変わり目
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                final String curriculumcd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String viewcd = KnjDbUtils.getString(row, "VIEWCD");
                final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                final String status = KnjDbUtils.getString(row, "STATUS");
                final String electdiv = KnjDbUtils.getString(row, "ELECTDIV");
                final String grade = KnjDbUtils.getString(row, "GRADE");
//                final String subclassKey;
//                if ("1".equals(param._useCurriculumcd)) {
//                    subclassKey = classcd + "-" + curriculumcd + "-" + subclasscd;
//                } else {
//                    subclassKey = subclasscd;
//                }
//                if (!param.useSubclass(regd._grade, subclassKey)) { // KNJA133Jの処理
//                    if (param._isOutputDebug) {
//                        log.info(" not use subclass = " + subclassKey);
//                    }
//                    continue;
//                }

                ClassView classView = getClassView(param, classViewList, classcd, classname, electdiv);
                if (null == classView) {
                    classView = new ClassView(ClassMst.get(param, param._classMstMap, classcd));
                    classViewList.add(classView);
                }
                ViewSubclass viewSubclass = ViewSubclass.getViewSubclass(classView._viewSubclassList, subclasscd);
                if (null == viewSubclass) {
                    viewSubclass = new ViewSubclass(SubclassMst.get(param, param._subclassMstMap, classcd + "-" + curriculumcd + "-" + subclasscd));
                    classView._viewSubclassList.add(viewSubclass);
                }
                View view = View.getView(viewSubclass._viewList, viewcd);
                if (null == view) {
                    view = new View(viewcd, viewname);
                    viewSubclass._viewList.add(view);
                }
                view._yearViewMap.put(year, new ViewStatus(curriculumcd, status, year, grade));
            }

            return classViewList;
        }

        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //観点の表
            stb.append(" VIEW_DATA AS ( ");
            stb.append("  SELECT ");
            stb.append("      SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      ,CLASSCD ");
                stb.append("      ,SCHOOL_KIND ");
                stb.append("      ,CURRICULUM_CD ");
            }
            stb.append("     ,VIEWCD ");
            stb.append("     ,YEAR ");
            stb.append("     ,STATUS ");
            stb.append("  FROM ");
            stb.append("     JVIEWSTAT_SUB_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("    AND T1.YEAR <= '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(" ) ");

            //学籍の表
            stb.append(" , SCHREG_DATA AS ( ");
            stb.append("  SELECT  YEAR ");
            stb.append(         ",GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT T1  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
            stb.append("                     AND YEAR <='" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(" ) ");

            stb.append(" , MAIN AS ( ");
            stb.append("    SELECT ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , JVM.CLASSCD ");
                stb.append("          , JVM.SCHOOL_KIND ");
                stb.append("          , JVM.CURRICULUM_CD ");
            }
            stb.append("          , VALUE(SUBM.SUBCLASSCD2, JVM.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append("          , JVM.VIEWCD ");
            stb.append("          , JVM.VIEWNAME ");
            stb.append("          , VALUE(JVM.SHOWORDER, -1) AS SHOWORDERVIEW ");
            stb.append("          , MAX(VD.STATUS) AS STATUS ");
            stb.append("    FROM    JVIEWNAME_SUB_MST JVM ");
            stb.append("    INNER JOIN JVIEWNAME_SUB_YDAT JVY ON JVY.SUBCLASSCD = JVM.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND JVY.CLASSCD = JVM.CLASSCD ");
                stb.append("          AND JVY.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("          AND JVY.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            stb.append("              AND JVY.VIEWCD = JVM.VIEWCD ");
            stb.append("    INNER JOIN SCHREG_DATA W2 ON W2.YEAR = JVY.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = JVM.CLASSCD ");
                stb.append("          AND CLM.SCHOOL_KIND = JVM.SCHOOL_KIND ");
            } else {
                stb.append("    LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = SUBSTR(JVM.SUBCLASSCD, 1, 2) ");
            }
            stb.append("    LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = JVM.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND SUBM.CLASSCD = JVM.CLASSCD ");
                stb.append("          AND SUBM.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("          AND SUBM.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            stb.append("          AND VALUE(CLM.ELECTDIV, '0') <> '1' ");

            stb.append("    LEFT JOIN VIEW_DATA VD ON VD.YEAR = W2.YEAR ");
            stb.append("       AND VD.VIEWCD = JVM.VIEWCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND VD.CLASSCD = JVM.CLASSCD ");
                stb.append("       AND VD.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("       AND VD.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            stb.append("       AND (VD.SUBCLASSCD = JVM.SUBCLASSCD ");
            stb.append("           OR VD.SUBCLASSCD = SUBM.SUBCLASSCD2 ");
            stb.append("           ) ");
            stb.append("    WHERE ");
            stb.append("       JVM.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
            stb.append("    GROUP BY ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , JVM.CLASSCD ");
                stb.append("          , JVM.SCHOOL_KIND ");
                stb.append("          , JVM.CURRICULUM_CD ");
            }
            stb.append("          , VALUE(SUBM.SUBCLASSCD2, JVM.SUBCLASSCD) ");
            stb.append("          , JVM.VIEWCD ");
            stb.append("          , JVM.VIEWNAME ");
            stb.append("          , VALUE(JVM.SHOWORDER, -1) ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT ");
            stb.append("     MAIN.YEAR ");
            stb.append("   , MAIN.GRADE ");
            stb.append("   , VALUE(CLM.ELECTDIV, '0') AS ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   , CLM.CLASSCD || '-' || CLM.SCHOOL_KIND AS CLASSCD ");
                stb.append("   , MAIN.CURRICULUM_CD ");
                stb.append("   , MAIN.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("   , CLM.CLASSCD");
                stb.append("   , '' AS CURRICULUM_CD");
                stb.append("   , MAIN.SUBCLASSCD");
            }
            stb.append("   , VALUE(CLM.CLASSORDERNAME1, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SUBM.SUBCLASSORDERNAME1, SUBM.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("   , VALUE(CLM.SHOWORDER, -1) AS SHOWORDERCLASS ");
            stb.append("   , MAIN.VIEWCD ");
            stb.append("   , MAIN.VIEWNAME ");
            stb.append("   , MAIN.STATUS ");
            stb.append(" FROM MAIN ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = MAIN.CLASSCD ");
                stb.append("   AND CLM.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
            } else {
                stb.append(" INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = SUBSTR(MAIN.SUBCLASSCD,1,2) ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = MAIN.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND SUBM.CLASSCD = MAIN.CLASSCD ");
                stb.append("  AND SUBM.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
                stb.append("  AND SUBM.CURRICULUM_CD = MAIN.CURRICULUM_CD ");
            }

            stb.append(" ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    VALUE(CLM.ELECTDIV, '0'), ");
            stb.append("    CLM.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" CLM.SCHOOL_KIND, ");
            }
            stb.append("    MAIN.SUBCLASSCD, ");
            stb.append("    VALUE(MAIN.SHOWORDERVIEW, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" MAIN.CURRICULUM_CD, "); // 教育課程の昇順に取得（同一の観点コードの場合、観点名称は教育課程の小さいほうを表示）
            }
            stb.append("    MAIN.VIEWCD, ");
            stb.append("    MAIN.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 観点科目データ
     */
    protected static class ViewSubclass {
        final SubclassMst _subclassMst;
        final List<View> _viewList = new ArrayList<View>();

        public ViewSubclass(final SubclassMst subclassMst) {
            _subclassMst = subclassMst;
        }

        public static ViewSubclass enabled(final ViewSubclass vs, final List<String> pageYears) {
            final List<View> rtn = new ArrayList<View>();
            for (final View view : vs._viewList) {
                final View enabled = View.enabled(view, pageYears);
                if (null != enabled) {
                    rtn.add(enabled);
                }
            }
            if (rtn.isEmpty()) {
                return null;
            }
            final ViewSubclass nvs = new ViewSubclass(vs._subclassMst);
            nvs._viewList.addAll(rtn);
            return nvs;
        }

        private static ViewSubclass getViewSubclass(final List<ViewSubclass> viewSubclassList, final String subclasscd) {
            ViewSubclass subclassView = null;
            for (final ViewSubclass viewSubclass0 : viewSubclassList) {
                if (viewSubclass0._subclassMst._subclasscd.equals(subclasscd)) {
                    subclassView = viewSubclass0;
                    break;
                }
            }
            return subclassView;
        }

        public String toString() {
            return "Subclass(" + _subclassMst + ")";
        }
    }

    protected static class View {
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final Map<String, ViewStatus> _yearViewMap = new HashMap<String, ViewStatus>();
        private View(
                final String viewcd,
                final String viewname
        ) {
            _viewcd = viewcd;
            _viewname = viewname;
        }

        public static View enabled(final View v, final List<String> pageYears) {
            final Map<String, ViewStatus> filteredYearViewMap = new HashMap<String, ViewStatus>();
            if (null == pageYears) {
                filteredYearViewMap.putAll(v._yearViewMap);
            } else {
                for (final String year : v._yearViewMap.keySet()) {
                    if (pageYears.contains(year)) {
                        filteredYearViewMap.put(year, v._yearViewMap.get(year));
                    }
                }
            }
            //log.info(" years = " + v._yearViewMap.keySet() + " / filtered = " + filteredYearViewMap.keySet());
            if (filteredYearViewMap.isEmpty()) {
                return null;
            }
            final View nv = new View(v._viewcd, v._viewname);
            nv._yearViewMap.putAll(filteredYearViewMap);
            return nv;
        }

        private static View getView(final List<View> viewList, final String viewcd) {
            View view = null;
            for (final View view0 : viewList) {
                if (view0._viewcd.equals(viewcd)) {
                    view = view0;
                    break;
                }
            }
            return view;
        }

        public String toString() {
            return "View(" + _viewcd + ":" + _yearViewMap.toString() + ")";
        }
    }

    /**
     * 観点データ
     */
    protected static class ViewStatus {
        final String _curriculumcd;
        final String _status; //観点
        final String _year;
        final String _grade; // 学年

        public ViewStatus(
                final String curriculumcd,
                final String status,
                final String year,
                final String grade
        ) {
            _curriculumcd = curriculumcd;
            _status = status;
            _year = year;
            _grade = grade;
        }

        public String toString() {
            return "(" + _year + "/ curriculumcd = " + _curriculumcd + ":" + StringUtils.defaultString(_status, " ") + ")";
        }
    }

    protected static class StudyRec {

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        protected static String sqlStudyrecReplace(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4))))");
            stb.append(" SELECT ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ANNUAL, ");
            if (param._hasANOTHER_CLASS_MST) {
                stb.append("     VALUE(ACM.SPECIALDIV, CM.SPECIALDIV, '0') AS SPECIALDIV, ");
                stb.append("     VALUE(T1.CLASSNAME, ACM.CLASSNAME, CM.CLASSNAME) AS ANOTHER_CLASSNAME, ");
            } else {
                stb.append("     VALUE(CM.SPECIALDIV, '0') AS SPECIALDIV, ");
                stb.append("     VALUE(T1.CLASSNAME, CM.CLASSNAME) AS ANOTHER_CLASSNAME, ");
            }
            if (param._hasANOTHER_SUBCLASS_MST) {
                stb.append("     VALUE(T1.SUBCLASSNAME, ASCM.SUBCLASSORDERNAME1, ASCM.SUBCLASSNAME, SCM.SUBCLASSORDERNAME1, SCM.SUBCLASSNAME) AS ANOTHER_SUBCLASSNAME, ");
            } else {
                stb.append("     VALUE(T1.SUBCLASSNAME, SCM.SUBCLASSORDERNAME1, SCM.SUBCLASSNAME) AS ANOTHER_SUBCLASSNAME, ");
            }
            if (param._hasSCHREG_STUDYREC_REPLACE_DAT) {
                stb.append("     CM_SAKI.CLASSNAME AS CLASSNAME_SAKI, ");
                stb.append("     VALUE(SCM_SAKI.SUBCLASSORDERNAME1, SCM_SAKI.SUBCLASSNAME) AS SUBCLASSNAME_SAKI, ");
                stb.append("     VALUE(CM_SAKI.ELECTDIV, '0') AS ELECTDIV_SAKI, ");
                stb.append("     VALUE(CM_SAKI.SPECIALDIV, '0') AS SPECIALDIV_SAKI, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T11.CLASSCD_SAKI, ");
                    stb.append("     T11.SCHOOL_KIND_SAKI, ");
                    stb.append("     T11.CURRICULUM_CD_SAKI, ");
                } else {
                    stb.append("     SUBSTR(T11.SUBCLASSCD_SAKI, 1, 2) AS CLASSCD_SAKI, ");
                    stb.append("     '' AS SCHOOL_KIND_SAKI, ");
                    stb.append("     '' AS CURRICULUM_CD_SAKI, ");
                }
                stb.append("     T11.SUBCLASSCD_SAKI, ");
                stb.append("     VALUE(CM_SAKI.SHOWORDER, -1) AS SHOWORDER_CLASS, ");
                stb.append("     VALUE(SCM_SAKI.SHOWORDER, -1) AS SHOWORDER_SUBCLASS, ");
            } else {
                stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSNAME_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS ELECTDIV_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SPECIALDIV_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD_SAKI, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSCD_SAKI, ");
                stb.append("     VALUE(CAST(NULL AS SMALLINT), -1) AS SHOWORDER_CLASS, ");
                stb.append("     VALUE(CAST(NULL AS SMALLINT), -1) AS SHOWORDER_SUBCLASS, ");
            }
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            if (param._hasSCHREG_STUDYREC_REPLACE_DAT) {
                stb.append("     T11.FORMER_REG_SCHOOLCD, ");
                stb.append("     T2.FINSCHOOL_NAME, ");
            } else {
                stb.append("     CAST(NULL AS VARCHAR(1)) AS FORMER_REG_SCHOOLCD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS FINSCHOOL_NAME, ");
            }
            if (param._hasANOTHER_CLASS_MST) {
                stb.append("     VALUE(ACM.SHOWORDER, CM.SHOWORDER, -1) AS ANOTHER_SHOWORDER_CLASS, ");
            } else {
                stb.append("     VALUE(CM.SHOWORDER, -1) AS ANOTHER_SHOWORDER_CLASS, ");
            }
            if (param._hasANOTHER_SUBCLASS_MST) {
                stb.append("     VALUE(ASCM.SHOWORDER, SCM.SHOWORDER, -1) AS ANOTHER_SHOWORDER_SUBCLASS, ");
            } else {
                stb.append("     VALUE(SCM.SHOWORDER, -1) AS ANOTHER_SHOWORDER_SUBCLASS, ");
            }
            stb.append("     T1.COMP_CREDIT, ");
            stb.append("     CASE WHEN T1.GET_CREDIT IS NULL AND T1.ADD_CREDIT IS NULL THEN CAST(NULL AS SMALLINT) ELSE VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) END AS CREDIT, ");
            if (param._hasSCHREG_STUDYREC_REPLACE_DAT) {
                stb.append("     CASE WHEN T11.GET_CREDIT IS NULL AND T11.ADD_CREDIT IS NULL THEN CAST(NULL AS SMALLINT) ELSE VALUE(T11.GET_CREDIT, 0) + VALUE(T11.ADD_CREDIT, 0) END AS SATEI_CREDIT ");
            } else {
                stb.append("     CAST(NULL AS SMALLINT) AS SATEI_CREDIT ");
            }
            stb.append("     , T1.VALUATION ");
            stb.append(" FROM SCHREG_STUDYREC_DAT T1 ");
            if (param._hasSCHREG_STUDYREC_REPLACE_DAT) {
                stb.append(" LEFT JOIN SCHREG_STUDYREC_REPLACE_DAT T11 ON T11.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("     AND T11.YEAR = T1.YEAR ");
                stb.append("     AND T11.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T11.ANNUAL = T1.ANNUAL ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     AND T11.CLASSCD = T1.CLASSCD ");
                    stb.append("     AND T11.SCHOOL_KIND= T1.SCHOOL_KIND ");
                    stb.append("     AND T11.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("     AND T11.SUBCLASSCD = T1.SUBCLASSCD ");
            }
            stb.append(" INNER JOIN SCHBASE T0 ON T0.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T1.YEAR <= T0.YEAR ");
            if (param._hasSCHREG_STUDYREC_REPLACE_DAT) {
                stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T11.FORMER_REG_SCHOOLCD ");
            }
            if (param._hasANOTHER_CLASS_MST) {
                stb.append(" LEFT JOIN ANOTHER_CLASS_MST ACM ON ACM.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     AND ACM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
            }
            if (param._hasANOTHER_SUBCLASS_MST) {
                stb.append(" LEFT JOIN ANOTHER_SUBCLASS_MST ASCM ON ASCM.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     AND ASCM.CLASSCD = T1.CLASSCD ");
                    stb.append("     AND ASCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("     AND ASCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
            }
            stb.append(" LEFT JOIN CLASS_MST CM ON CM.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND SCM.CLASSCD = T1.CLASSCD ");
                stb.append("     AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            if (param._hasSCHREG_STUDYREC_REPLACE_DAT) {
                stb.append(" LEFT JOIN CLASS_MST CM_SAKI ON CM_SAKI.CLASSCD = T11.CLASSCD_SAKI ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     AND CM_SAKI.SCHOOL_KIND = T11.SCHOOL_KIND_SAKI ");
                }
                stb.append(" LEFT JOIN SUBCLASS_MST SCM_SAKI ON SCM_SAKI.SUBCLASSCD = T11.SUBCLASSCD_SAKI ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     AND SCM_SAKI.CLASSCD = T11.CLASSCD_SAKI ");
                    stb.append("     AND SCM_SAKI.SCHOOL_KIND = T11.SCHOOL_KIND_SAKI ");
                    stb.append("     AND SCM_SAKI.CURRICULUM_CD = T11.CURRICULUM_CD_SAKI ");
                }
            }
            stb.append(" WHERE ");
            stb.append("    T1.SCHOOLCD = '1' ");
            stb.append(" ORDER BY T1.YEAR ");
            return stb.toString();
        }
    }

    protected static class GakunenSeiseki {

        final Param _param;
        Map<SubclassMst, TreeMap<Year, Integer>> _gakunenSeisekiMap;

        GakunenSeiseki(final Param param) {
            _param = param;
        }

        public static GakunenSeiseki load(final DB2UDB db2, final Param param, final String schregno) {
            final GakunenSeiseki c = new GakunenSeiseki(param);

            final String psKey = "PS_GAKUNENSEISEKI";
            if (null == param.getPs(psKey)) {
                final String sql = sqlGakunenSeiseki(param);
                param.setPs(psKey, db2, sql);
            }
            Map<SubclassMst, TreeMap<Year, Integer>> subclassYearGakunenSeisekiMap = new TreeMap<SubclassMst, TreeMap<Year, Integer>>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { param._year, schregno})) {
                final SubclassMst mst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CURRICULUM_CD"), KnjDbUtils.getString(row, "SUBCLASSCD")));
                if (null == mst) {
                    continue;
                }
                final String year = KnjDbUtils.getString(row, "YEAR");
                final Integer score = Integer.valueOf(KnjDbUtils.getString(row, "SCORE"));
                Util.getMappedMap(subclassYearGakunenSeisekiMap, mst).put(Year.of(year), score);
            }
            c._gakunenSeisekiMap = subclassYearGakunenSeisekiMap;

            return c;
        }

        public String getGakunenSeisekiString(final SubclassMst mst, final Year year) {
            if (!_gakunenSeisekiMap.containsKey(mst) || !_gakunenSeisekiMap.get(mst).containsKey(year)) {
                return null;
            }
            return _gakunenSeisekiMap.get(mst).get(year).toString();
        }

        private static String sqlGakunenSeiseki(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     YEAR ");
            sql.append("   , CLASSCD ");
            sql.append("   , SCHOOL_KIND ");
            sql.append("   , CURRICULUM_CD ");
            sql.append("   , SUBCLASSCD ");
            sql.append("   , SCORE ");
            sql.append(" FROM ");
            sql.append("     RECORD_SCORE_DAT T1 ");
            sql.append(" WHERE ");
            sql.append("     YEAR <= ? ");
            sql.append("     AND SEMESTER = '9' ");
            sql.append("     AND TESTKINDCD = '99' ");
            sql.append("     AND TESTITEMCD = '00' ");
            sql.append("     AND SCORE_DIV = '08' ");
            sql.append("     AND SCHREGNO = ? ");
            sql.append("     AND SCORE IS NOT NULL ");
            return sql.toString();
        }
    }

    /**
     * <<生徒異動情報クラス>>。
     */
    protected static class TransferRec {

        protected static final String A004_NAMECD2_RYUGAKU = "1";
        protected static final String A004_NAMECD2_KYUGAKU = "2";

        final String _transfercd;
        final String _name;
        final String _sYear;
        final String _sDate;
        final String _sDateStr;
        protected String _eYear;
        protected String _eDate;
        protected String _eDateStr;
        protected String _reason;
        protected String _place;
        protected String _address;

        /**
         * コンストラクタ。
         */
        protected TransferRec(
                final String transfercd,
                final String name,
                final String sYear,
                final String sDate,
                final String sDateStr,
                final String eYear,
                final String eDate,
                final String eDateStr,
                final String reason,
                final String place,
                final String address
                ) {
            _transfercd = transfercd;
            _name = name;
            _sYear = sYear;
            _sDate = sDate;
            _sDateStr = sDateStr;
            _eYear = eYear;
            _eDate = eDate;
            _eDateStr = eDateStr;
            _reason = reason;
            _place = place;
            _address = address;
        }

        protected static int compareCalendar(final Calendar cal1, final Calendar cal2) {
            final Date d1 = cal1.getTime();
            final Date d2 = cal2.getTime();
            return d1.compareTo(d2);
        }

        /**
         * 3/31日時点で留学している年度の集合を得る
         * @param t
         * @param param
         * @return
         */
        public static Collection<String> get0331YearSet(final TransferRec t, final Param param) {
            final Set<String> yearSet = new TreeSet<String>(); // 3/31に留学している年度のSet
            final Calendar scal = getCalendarOfDate(t._sDate);
            final String edate;
            if (null == t._eDate) {
                final String year = String.valueOf(scal.get(Calendar.YEAR) + 1);
                edate = year + "-03-31";
            } else {
                edate = t._eDate;
            }
            final Calendar ecal = getCalendarOfDate(edate);
            while (scal.before(ecal)) {
                while (scal.get(Calendar.MONTH) != Calendar.MARCH) {
                    scal.add(Calendar.MONTH, 1);
                }
                final Calendar cal0331 = getCalendarOfDate(String.valueOf(scal.get(Calendar.YEAR)) + "-03-31");

                if (compareCalendar(scal, cal0331) <= 0 && compareCalendar(cal0331, ecal) <= 0) {
                    yearSet.add(String.valueOf(scal.get(Calendar.YEAR) - 1)); // 年度なので-1
                }
                scal.add(Calendar.MONTH, 1);
            }
            if (param._isOutputDebug) {
                log.info(" abroad 0331 year set = " + t + " -> " + yearSet);
            }
            return yearSet;
        }

        /**
         * 4/1時点で留学している年度の集合を得る
         * @param t
         * @param param
         * @return
         */
        public static Collection<String> get0401YearSet(final TransferRec t, final Param param) {
            final Set<String> yearSet = new TreeSet<String>(); // 4/1に留学している年度のSet
            final Calendar scal = getCalendarOfDate(t._sDate);
            final String edate;
            if (null == t._eDate) {
                final String year = String.valueOf(scal.get(Calendar.YEAR) + 1);
                edate = year + "-03-31";
            } else {
                edate = t._eDate;
            }
            final Calendar ecal = getCalendarOfDate(edate);
            while (scal.before(ecal)) {
                while (scal.get(Calendar.MONTH) != Calendar.APRIL) {
                    scal.add(Calendar.MONTH, 1);
                    scal.set(Calendar.DAY_OF_MONTH, 1);
                }
                final Calendar cal0401 = getCalendarOfDate(String.valueOf(scal.get(Calendar.YEAR)) + "-04-01");

                if (compareCalendar(scal, cal0401) <= 0 && compareCalendar(cal0401, ecal) <= 0) {
                    yearSet.add(String.valueOf(scal.get(Calendar.YEAR)));
                }
                scal.add(Calendar.MONTH, 1);
            }
            if (param._isOutputDebug) {
                log.info(" abroad 0401 year set = " + t + " -> " + yearSet);
            }
            return yearSet;
        }

        // 期間が4/1～3/31ならTrue
        public boolean isFrom0401To0331() {
            return isDayOfMonth(_sDate, 4, 1) && isDayOfMonth(_eDate, 3, 31);
        }

        protected static List<Integer> getPrintTransferRecIndexList(final List<TransferRec> transferRecList, final Param param) {
            final int max = 3;
            final List<Integer> printTransferRecIndexList = new ArrayList();
            int printAbroadCount = 0;
            int ia = 1; // 休学・留学回数
            for (int i = 0; i < transferRecList.size(); i++) {
                final TransferRec tr = transferRecList.get(i);

                if (ia > max) {
                } else {
                    if (TransferRec.A004_NAMECD2_RYUGAKU.equals(tr._transfercd)) { // 留学
                        if (param._z010.in(Z010.kyoto) && printAbroadCount > 0) { // 京都府は留学詳細は1件のみ表示可能
                        } else {
                            printTransferRecIndexList.add(new Integer(i));
                            printAbroadCount += 1;
                            ia++;
                        }
                    } else if (TransferRec.A004_NAMECD2_KYUGAKU.equals(tr._transfercd)) { // 休学
                        printTransferRecIndexList.add(new Integer(i));
                        ia++;
                    }
                }
            }
            return printTransferRecIndexList;
        }

        protected static List<TransferRec> getGradeOnlyTransferList(final Param param, final boolean isFirst, final List<TransferRec> allTransferRecList, final List<? extends Gakuseki> gakusekiList) {
            final int minYear = Gakuseki.gakusekiMinYear(gakusekiList);
            final int maxYear = Gakuseki.gakusekiMaxYear(gakusekiList);
            final Range<Integer> yearRange = Range.of(minYear, maxYear);
            final List<TransferRec> transferRecList = new ArrayList<TransferRec>();
            for (int i = 0; i < allTransferRecList.size(); i++) {
                final TransferRec tr = allTransferRecList.get(i);
                final boolean hitZaisekimae = param._seitoSidoYorokuZaisekiMae && isFirst && Integer.parseInt(tr._sYear) < minYear;
                final Range<Integer> trRange = Range.of(Integer.valueOf(Util.toInt(tr._sYear, minYear)), Integer.valueOf(Util.toInt(tr._eYear, maxYear)));
                final boolean hitZaisekichu = yearRange.isOverlapped(trRange, true);
                if (hitZaisekimae || hitZaisekichu) {
                    transferRecList.add(tr);
                }
            }
            return transferRecList;
        }

        /**
         * 異動履歴クラスを作成し、リストに加えます。
         */
        protected static List<TransferRec> loadTransferRec(final DB2UDB db2, final String schregno, final Param param) {
            final List<TransferRec> transferRecList = new LinkedList<TransferRec>();
            final String psKey = "TRANSFER_REC";
            if (null == param.getPs(psKey)) {

                final String sql = sql(param);
                if (param._isOutputDebugQuery) {
                    log.info(" transfer sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno})) {

                final String transfercd = KnjDbUtils.getString(row, "TRANSFERCD");
                final String sDate = KnjDbUtils.getString(row, "SDATE");
                final String sYear = KnjDbUtils.getString(row, "SDATE_YEAR");
                final String eDate = KnjDbUtils.getString(row, "EDATE");
                final String eYear = KnjDbUtils.getString(row, "EDATE_YEAR");
                final String reason = blankToNull(KnjDbUtils.getString(row, "REASON"));
                final String place = blankToNull(KnjDbUtils.getString(row, "PLACE"));
                final String address = blankToNull(KnjDbUtils.getString(row, "ADDR"));

                final boolean matomeru = TransferRec.A004_NAMECD2_RYUGAKU.equals(transfercd) || param._z010.in(Z010.mieken) && TransferRec.A004_NAMECD2_KYUGAKU.equals(transfercd);
                if (matomeru) {
                    TransferRec before = null;
                    for (final TransferRec b : transferRecList) {
                        if (transfercd.equals(b._transfercd)) {
                            if (Util.isNextDate(b._eDate, sDate)) {
                                before = b;
                                break;
                            }
                        }
                    }
                    if (!param._z010.in(Z010.kyoai)) {
                        if (null != before) {
                            before._eYear = eYear;
                            before._eDate = eDate;
                            before._eDateStr = setDateFormatInsertBlank(db2, formatDate1(db2, eDate, param), param, param._formatDateDefaultYear);
                            if (param._z010.in(Z010.mieken)) {
                                before._reason = (!StringUtils.isEmpty(before._reason) && StringUtils.isEmpty(reason)) ? before._reason : reason;
                                before._place = (!StringUtils.isEmpty(before._place) && StringUtils.isEmpty(place)) ? before._place : place;
                                before._address = (!StringUtils.isEmpty(before._address) && StringUtils.isEmpty(address)) ? before._address : address;
                            }
                            continue;
                        }
                    }
                }

                final TransferRec transferRec = new TransferRec(
                        transfercd,
                        defstr(KnjDbUtils.getString(row, "NAME1")),
                        sYear,
                        sDate,
                        setDateFormatInsertBlank(db2, formatDate1(db2, sDate, param), param, defstr(sYear, param._formatDateDefaultYear)),
                        eYear,
                        eDate,
                        setDateFormatInsertBlank(db2, formatDate1(db2, eDate, param), param, defstr(sYear, param._formatDateDefaultYear)),
                        reason,
                        place,
                        address);
                transferRecList.add(transferRec);
            }
            return transferRecList;
        }

        private static String blankToNull(final String s) {
            return "".equals(s) ? null : s;
        }

        private static String sql(final Param param) {

            final StringBuffer sql = new StringBuffer();

            sql.append("  SELECT ");
            sql.append("      T1.SDATE_YEAR,");
            sql.append("      T1.SDATE,");
            sql.append("      T1.EDATE_YEAR,");
            sql.append("      T1.EDATE,");
            sql.append("      T1.REASON,");
            sql.append("      T1.PLACE,");
            sql.append("      T1.ADDR,");
            sql.append("      T1.TRANSFERCD,");
            sql.append("      T3.NAME1 ");
            sql.append("  FROM ");
            sql.append("      (");
            sql.append("          SELECT ");
            sql.append("              FISCALYEAR(TRANSFER_SDATE) AS SDATE_YEAR,");
            sql.append("              TRANSFER_SDATE AS SDATE,");
            sql.append("              FISCALYEAR(TRANSFER_EDATE) AS EDATE_YEAR,");
            sql.append("              TRANSFER_EDATE AS EDATE,");
            sql.append("              TRANSFERREASON AS REASON,");
            sql.append("              TRANSFERPLACE AS PLACE,");
            sql.append("              TRANSFERADDR AS ADDR,");
            sql.append("              TRANSFERCD, ");
            sql.append("              SCHREGNO ");
            sql.append("          FROM ");
            sql.append("              SCHREG_TRANSFER_DAT ");
            sql.append("          WHERE ");
            sql.append("              SCHREGNO = ? ");
            sql.append("      )T1 ");
            sql.append("      INNER JOIN SCHOOL_MST T2 ON T2.YEAR = T1.SDATE_YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("      AND T2.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
            }
            sql.append("      INNER JOIN NAME_MST T3 ON T3.NAMECD1 = 'A004' AND T3.NAMECD2 = T1.TRANSFERCD ");
            sql.append("      LEFT JOIN (SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
            sql.append("                 FROM SCHREG_REGD_DAT T1 ");
            sql.append("                 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            sql.append("      ) L1 ON L1.YEAR = T1.SDATE_YEAR AND L1.SCHREGNO = T1.SCHREGNO ");
            sql.append("  WHERE ");
            sql.append("      T1.SDATE_YEAR <= '" + param._year + "' ");
            if (param._seitoSidoYorokuZaisekiMae) {
                sql.append("      AND VALUE(L1.SCHOOL_KIND, '" + param.SCHOOL_KIND + "') = '" + param.SCHOOL_KIND + "' ");
            } else {
                sql.append("      AND L1.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
            }
            sql.append("  ORDER BY ");
            sql.append("      TRANSFERCD, SDATE");

            return sql.toString();
        }

        public String toString() {
            return "TransferRec(" + _transfercd + ", " + _sDate + ", " + _eDate + ", " + _name + ")";
        }
    }

    /**
     * <<学習記録データクラス>>。
     */
    protected static class HtrainRemark extends Mapped implements Comparable<HtrainRemark> {

        private static String YEAR = "YEAR";
        private static String ANNUAL = "ANNUAL";
        private static String SPECIALACTREMARK = "SPECIALACTREMARK";
        private static String TOTALREMARK = "TOTALREMARK";
        private static String NARA_TIME = "NARA_TIME";
        private static String SAGA_SP_TIME = "SAGA_SP_TIME";
        private static String SHIMANE_JIRITSU = "SHIMANE_JIRITSU";
        private static String ATTENDREC_REMARK = "ATTENDREC_REMARK";
        private static String TOTALSTUDYACT = "TOTALSTUDYACT";
        private static String TOTALSTUDYVAL = "TOTALSTUDYVAL";
        private static String CAREER_PLAN_ACT = "CAREER_PLAN_ACT";
        private static String CAREER_PLAN_VAL = "CAREER_PLAN_VAL";
        static HtrainRemark Null = new HtrainRemark(null);

        private HtrainRemark(final Map<String, String> row) {
            super(new String[] {YEAR, ANNUAL, SPECIALACTREMARK, TOTALREMARK, NARA_TIME, SAGA_SP_TIME, ATTENDREC_REMARK, TOTALSTUDYACT, TOTALSTUDYVAL, CAREER_PLAN_ACT, CAREER_PLAN_VAL, SHIMANE_JIRITSU});
            putAll(row);
        }

        public String year() {
            return get(YEAR);
        }

        public String annual() {
            return get(ANNUAL);
        }

        /** 特別活動 */
        public String specialactremark() {
            return get(SPECIALACTREMARK);
        }

        /** 総合所見 */
        public String totalremark() {
            return get(TOTALREMARK);
        }

        /** 奈良Time */
        public String naraTime() {
            return get(NARA_TIME);
        }

        /** 佐賀県特別活動時数 */
        public String sagaSpTime() {
            return get(SAGA_SP_TIME);
        }

        /** 島根県自立活動 */
        public String shimaneJiritsuKatsudou() {
            return get(SHIMANE_JIRITSU);
        }

        /** 出欠備考 */
        public String attendrecRemark() {
            return get(ATTENDREC_REMARK);
        }

        /** 総合的な学習の時間学習活動 */
        public String totalstudyact() {
            return get(TOTALSTUDYACT);
        }

        /** 総合的な学習の時間評価 */
        public String totalstudyval() {
            return get(TOTALSTUDYVAL);
        }

        public String careerPlanAct() {
            return get(CAREER_PLAN_ACT);
        }

        public String careerPlanVal() {
            return get(CAREER_PLAN_VAL);
        }

        @Override
        public int compareTo(final HtrainRemark that) {
            return year().compareTo(that.year());
        }

        public String toString() {
            return "year=" + year() + ", totalStudyAct=[" + totalstudyact() + "], totalStudyVal=[" + totalstudyval() + "]";
        }

        /**
         * 所見クラスを作成し、マップに加えます。
         */
        protected static Map<String, HtrainRemark> loadHtrainRemark(final DB2UDB db2, final String schregno, final Param param) {
            final Map<String, HtrainRemark> htrainRemarkMap = new HashMap<String, HtrainRemark>();

            final String psKey = "PS_HTRAINREMARK_DAT";
            if (null == param.getPs(psKey)) {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT  YEAR, ANNUAL, TOTALSTUDYACT, TOTALSTUDYVAL, SPECIALACTREMARK, TOTALREMARK, ATTENDREC_REMARK ");
                sql.append(" FROM HTRAINREMARK_DAT");
                sql.append(" WHERE SCHREGNO = ? ");
                sql.append("   AND YEAR <= '" + param._year + "'");
                param.setPs(psKey, db2, sql.toString());
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno })) {
                htrainRemarkMap.put(new HtrainRemark(row).year(), new HtrainRemark(row));
            }

            if ("1".equals(param.property(Property.seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg))) {
                final String psKey2 = "PS_CARRER_PLAN";
                if (null == param.getPs(psKey2)) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT YEAR, REMARK1 AS CAREER_PLAN_ACT, REMARK2 AS CAREER_PLAN_VAL ");
                    sql.append(" FROM HTRAINREMARK_DETAIL2_DAT ");
                    sql.append(" WHERE SCHREGNO = ? ");
                    sql.append("   AND YEAR <= '" + param._year + "'");
                    sql.append("   AND HTRAIN_SEQ = '003' ");
                    param.setPs(psKey2, db2, sql.toString());
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey2), new String[] { schregno })) {

                    final String year = KnjDbUtils.getString(row, "YEAR");

                    if (null == htrainRemarkMap.get(year)) {
                        htrainRemarkMap.put(year, new HtrainRemark(null));
                    }
                    final HtrainRemark htrainRemark = htrainRemarkMap.get(year);
                    htrainRemark.putAll(row);
                }
            }

            if (param._z010.in(Z010.naraken)) {
                final String psKey2 = "PS_NARA_TIME";
                if (null == param.getPs(psKey2)) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT YEAR, REMARK1 AS NARA_TIME ");
                    sql.append(" FROM HTRAINREMARK_DETAIL2_DAT ");
                    sql.append(" WHERE SCHREGNO = ? ");
                    sql.append("   AND YEAR <= '" + param._year + "'");
                    sql.append("   AND HTRAIN_SEQ = '005' ");
                    param.setPs(psKey2, db2, sql.toString());
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey2), new String[] { schregno })) {

                    final String year = KnjDbUtils.getString(row, "YEAR");

                    if (null == htrainRemarkMap.get(year)) {
                        htrainRemarkMap.put(year, new HtrainRemark(null));
                    }
                    final HtrainRemark htrainRemark = htrainRemarkMap.get(year);
                    htrainRemark.putAll(row);
                }
            } else if (param._z010.in(Z010.sagaken)) {
                final String psKey2 = "PS_SAGA_SP_TIME";
                if (null == param.getPs(psKey2)) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT YEAR, REMARK1 AS SAGA_SP_TIME ");
                    sql.append(" FROM HTRAINREMARK_DETAIL2_DAT ");
                    sql.append(" WHERE SCHREGNO = ? ");
                    sql.append("   AND YEAR <= '" + param._year + "'");
                    sql.append("   AND HTRAIN_SEQ = '006' ");
                    param.setPs(psKey2, db2, sql.toString());
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey2), new String[] { schregno })) {

                    final String year = KnjDbUtils.getString(row, "YEAR");

                    if (null == htrainRemarkMap.get(year)) {
                        htrainRemarkMap.put(year, new HtrainRemark(null));
                    }
                    final HtrainRemark htrainRemark = htrainRemarkMap.get(year);
                    htrainRemark.putAll(row);
                }
            } else if (param._z010.in(Z010.shimaneken)) {

                final String psKey2 = "PS_SHIMANE_JIRITSU";
                if (null == param.getPs(psKey2)) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT YEAR, REMARK1 AS SHIMANE_JIRITSU ");
                    sql.append(" FROM HTRAINREMARK_DETAIL2_DAT ");
                    sql.append(" WHERE SCHREGNO = ? ");
                    sql.append("   AND YEAR <= '" + param._year + "'");
                    sql.append("   AND HTRAIN_SEQ = '001' ");
                    param.setPs(psKey2, db2, sql.toString());
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey2), new String[] { schregno })) {

                    final String year = KnjDbUtils.getString(row, "YEAR");

                    if (null == htrainRemarkMap.get(year)) {
                        htrainRemarkMap.put(year, new HtrainRemark(null));
                    }
                    final HtrainRemark htrainRemark = htrainRemarkMap.get(year);
                    htrainRemark.putAll(row);
                }
            }

            return htrainRemarkMap;
        }
    }

    /**
     * <<学習記録データクラス>>。
     */
    protected static class HtrainRemarkOnline extends Mapped implements Comparable<HtrainRemarkOnline> {

        private static String YEAR = "YEAR";
        private static String ANNUAL = "ANNUAL";
        private static String ABSENCE_REASON = "ABSENCE_REASON";
        private static String DAYS = "DAYS";
        private static String PARTICIPATION_DAYS = "PARTICIPATION_DAYS";
        private static String METHOD = "METHOD";
        private static String OTHER_LEARNING = "OTHER_LEARNING";
        static HtrainRemarkOnline Null = new HtrainRemarkOnline(null);

        private HtrainRemarkOnline(final Map<String, String> row) {
            super(new String[] {YEAR, ANNUAL, ABSENCE_REASON, DAYS, PARTICIPATION_DAYS, METHOD, OTHER_LEARNING});
            putAll(row);
        }

        public String year() {
            return get(YEAR);
        }

        public String annual() {
            return get(ANNUAL);
        }

        /** 登校できない事由 */
        public String absenceReason() {
            return get(ABSENCE_REASON);
        }

        /** オンラインを活用した特例の授業 実施日数 */
        public String days() {
            return get(DAYS);
        }

        /** オンラインを活用した特例の授業 実施日数 */
        public String participationDays() {
            return get(PARTICIPATION_DAYS);
        }

        /** オンラインを活用した特例の授業 実施日数 */
        public String method() {
            return get(METHOD);
        }

        /** その他の学習等 */
        public String otherLearning() {
            return get(OTHER_LEARNING);
        }

        @Override
        public int compareTo(final HtrainRemarkOnline that) {
            return year().compareTo(that.year());
        }

        public String toString() {
            return "HtrainRemarkOnline(year=" + year() + ")";
        }

        /**
         * 所見クラスを作成し、マップに加えます。
         */
        protected static Map<String, HtrainRemarkOnline> loadHtrainRemarkOnline(final DB2UDB db2, final PsStore psStore, final String schregno, final String maxYear) {
            final Map<String, HtrainRemarkOnline> htrainRemarkOnlineMap = new HashMap<String, HtrainRemarkOnline>();

            final String psKey = "PS_HTRAINREMARK_DAT";
            if (null == psStore.getPs(psKey)) {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT  YEAR, ANNUAL, ABSENCE_REASON, DAYS, PARTICIPATION_DAYS, METHOD, OTHER_LEARNING ");
                sql.append(" FROM HTRAINREMARK_ONLINE_DAT");
                sql.append(" WHERE SCHREGNO = ? ");
                sql.append("   AND YEAR <= '" + maxYear + "'");
                psStore.setPs(psKey, db2, sql.toString());
            }

            for (final Map row : KnjDbUtils.query(db2, psStore.getPs(psKey), new String[] { schregno })) {
                htrainRemarkOnlineMap.put(new HtrainRemarkOnline(row).year(), new HtrainRemarkOnline(row));
            }

            return htrainRemarkOnlineMap;
        }
    }

    /**
     * <<学習記録データクラス>>。
     */
    protected static class HtrainRemarkDetail {
        final String _year;
        final String _trainRef1;
        final String _trainRef2;
        final String _trainRef3;

        HtrainRemarkDetail(final Map row) {
            _year = KnjDbUtils.getString(row, "YEAR");
            _trainRef1 = KnjDbUtils.getString(row, "TRAIN_REF1");
            _trainRef2 = KnjDbUtils.getString(row, "TRAIN_REF2");
            _trainRef3 = KnjDbUtils.getString(row, "TRAIN_REF3");
        }

        /**
         * 所見詳細クラスを作成し、マップに加えます。
         */
        protected static Map<String, HtrainRemarkDetail> loadHtrainRemarkDetail(final DB2UDB db2, final String schregno, final Param param) {
            final Map<String, HtrainRemarkDetail> htrainRemarkDetailMap = new HashMap<String, HtrainRemarkDetail>();

            final String psKey = "HTRAINREMARK_DETAIL_DAT";
            if (null == param.getPs(psKey)) {
                final String sql = "SELECT  YEAR, TRAIN_REF1, TRAIN_REF2, TRAIN_REF3"
                        + " FROM HTRAINREMARK_DETAIL_DAT"
                        + " WHERE SCHREGNO = ? "
                        + " AND YEAR <= '" + param._year + "'"
                        ;

                param.setPs(psKey, db2, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno })) {
                final HtrainRemarkDetail htrainRemarkDetail = new HtrainRemarkDetail(row);
                htrainRemarkDetailMap.put(KnjDbUtils.getString(row, "YEAR"), htrainRemarkDetail);
            }
            return htrainRemarkDetailMap;
        }
    }

    protected static class HtrainRemarkTrainref {
        protected static final HtrainRemarkTrainref NULL = new HtrainRemarkTrainref("");

        final String _year;
        String _trainRef1;
        String _trainRef2;
        String _trainRef3;
        String _trainRef4;
        String _trainRef5;
        String _trainRef6;

        HtrainRemarkTrainref(final String year) {
            _year = year;
        }

        /**
         * 所見詳細クラスを作成し、マップに加えます。
         */
        protected static Map<Year, HtrainRemarkTrainref> loadHtrainRemarkTrainref(final DB2UDB db2, final String schregno, final Param param) {
            final Map<Year, HtrainRemarkTrainref> yearDataMap = new HashMap<Year, HtrainRemarkTrainref>();

            final String psKey = "HTRAINREMARK_TRAINREF_DAT";
            if (null == param.getPs(psKey)) {
                final String sql = "SELECT  YEAR, TRAIN_SEQ, REMARK "
                        + " FROM HTRAINREMARK_TRAINREF_DAT"
                        + " WHERE SCHREGNO = ? "
                        + " AND YEAR <= '" + param._year + "'"
                        ;

                param.setPs(psKey, db2, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno })) {
                final Year year = Year.of(KnjDbUtils.getString(row, "YEAR"));
                if (!yearDataMap.containsKey(year)) {
                    yearDataMap.put(year, new HtrainRemarkTrainref(year._get));
                }
                final String seq = KnjDbUtils.getString(row, "TRAIN_SEQ");
                final String remark = KnjDbUtils.getString(row, "REMARK");
                if ("101".equals(seq)) {
                    yearDataMap.get(year)._trainRef1 = remark;
                } else if ("102".equals(seq)) {
                    yearDataMap.get(year)._trainRef2 = remark;
                } else if ("103".equals(seq)) {
                    yearDataMap.get(year)._trainRef3 = remark;
                } else if ("104".equals(seq)) {
                    yearDataMap.get(year)._trainRef4 = remark;
                } else if ("105".equals(seq)) {
                    yearDataMap.get(year)._trainRef5 = remark;
                } else if ("106".equals(seq)) {
                    yearDataMap.get(year)._trainRef6 = remark;
                }
            }
            return yearDataMap;
        }
    }

    protected static class Tuple<K, V> implements Comparable<Tuple<K, V>> {
        final K _first;
        final V _second;
        private Tuple(final K first, final V second) {
            _first = first;
            _second = second;
        }
        public static <K, V, V2 extends V> Tuple<K, V> of(final K first, final V2 second) {
            return new Tuple<K, V>(first, second);
        }
        public int compareTo(final Tuple<K, V> to) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == to._first && !(to._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(to._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == to._second && !(to._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(to._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    protected static interface Predicate<T> {
        boolean test(T t);
    }

    protected static class NotNullPredicate<T> implements Predicate<T> {
        public boolean test(T t) {
            return null != t;
        }
    }

    protected static class NotPredicate<T> implements Predicate<T> {
        final Predicate<T> _other;
        public NotPredicate(final Predicate<T> other) {
            _other = other;
        }
        public boolean test(T t) {
            return !_other.test(t);
        }
    }

    protected static class Year implements Comparable<Year> {
        final String _get;
        public static Year of(final String get) {
            return new Year(get);
        }
        private Year(final String get) {
            _get = get;
        }
        @Override
        public int compareTo(final Year y) {
            if (!NumberUtils.isDigits(_get) && !NumberUtils.isDigits(y._get)) {
                return 0;
            } else if (!NumberUtils.isDigits(_get)) {
                return 100;
            } else if (!NumberUtils.isDigits(y._get)) {
                return -100;
            }
            return Integer.parseInt(_get) - Integer.parseInt(y._get);
        }
        @Override
        public boolean equals(final Object o) {
            if (null == o || !(o instanceof Year)) {
                return false;
            }
            final Year y = (Year) o;
            return compareTo(y) == 0;
        }
        @Override
        public int hashCode() {
            return _get.hashCode() * 8191;
        }
        @Override
        public String toString() {
            return "Year(" + _get + ")";
        }
    }

    protected static class StringTemplate {
        final String _template;
        StringTemplate(final String template) {
            _template = template;
        }
        public String format(final Map<String, String> dataMap) {
            if (dataMap.isEmpty()) {
                return "";
            }
            String t = _template;
            for (final Map.Entry<String, String> e : dataMap.entrySet()) {
                t = t.replace(e.getKey(), e.getValue());
            }
            return t;
        }
    }

    static interface RemarkContainer {
        String getRemark(final Param param);
    }


    static class StringRemarkContainer implements RemarkContainer {
        final String _remark;
        public StringRemarkContainer(final String remark) {
            _remark = remark;
        }
        @Override
        public String getRemark(final Param param) {
            return _remark;
        }

        static List<String> toStringList(final Param param, final List<RemarkContainer> list) {
            final List<String> rtn = new ArrayList<String>();
            for (final RemarkContainer c : list) {
                rtn.add(c.getRemark(param));
            }
            return rtn;
        }
    }

    protected static enum OptionCredit {

        /** 科目・単位表示オプション
         *   -+----------+---------------------------+
         *    |          | 様式１裏                  |
         *    |          |---------------------------+
         *    |          | 未履修       | 履修のみ   |
         *   -+----------+--------------+------------+
         *   0| 科目表示 | 有           | 有         |
         *    | 単位     | 0            | (履修単位) |
         *   -+----------+--------------+------------+
         *   1| 科目表示 | 無           | 無         |
         *    | 単位     | -            | -          |
         *   -+----------+--------------+------------+
         *   2| 科目表示 | 有           | 有         |
         *    | 単位     | 空欄         | 0          |
         *   ------------+--------------+------------+
         *
         *   -+----------+---------------------------+
         *    |          | 様式２表                  |
         *    |          |---------------------------+
         *    |          | 未履修       | 履修のみ   |
         *   -+----------+--------------+------------+
         *   0| 科目表示 | 有           | 有         |
         *    | 単位     | 0            | (履修単位) |
         *   -+----------+--------------+------------+
         *   1| 科目表示 | 有           | 有         |
         *    | 単位     |              | (履修単位) |
         *   -+----------+--------------+------------+
         *   2| 科目表示 | 有           | 有         |
         *    | 単位     | 空欄         | 0          |
         *   ------------+--------------+------------+
         *
         *   ※0:賢者 1:京都府 2:鳥取
         * */

          YOSHIKI1_URA_0
        , YOSHIKI1_URA_1
        , YOSHIKI1_URA_2
        , YOSHIKI2_OMOTE_0
        , YOSHIKI2_OMOTE_1
        , YOSHIKI2_OMOTE_2;
    }


    protected static class ClassMst {
        protected static final ClassMst ABROAD = new ClassMst("AA", "AA", _ABROAD, "0", "0", new Integer(0));
        protected static final ClassMst Null = new ClassMst("XX", "XX", null, "0", "9", SHOWORDER_DEFAULT);
        final String _classcd;
        final String _schoolKind;
        final String _classname;
        final String _electdiv;
        final String _specialDiv;
        final Integer _showorder;

        protected ClassMst(final String classcd, final String schoolKind, final String classname, final String electdiv, final String specialdiv, final Integer showorder) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _classname = classname;
            _electdiv = electdiv;
            _specialDiv = specialdiv;
            _showorder = showorder;
        }

        public ClassMst setClassname(final String classname) {
            return new ClassMst(_classcd, _schoolKind, classname, _electdiv, _specialDiv, _showorder);
        }

        public String getKey(final Param param) {
            return key(param, _classcd, _schoolKind);
        }

        public String toString() {
            return "ClassMst(" + getKey(null) + ", " + _classname + ")";
        }

        public static String key(final Param param, final String classcd, final String schoolKind) {
            return classcd + "-" + schoolKind;
        }

        public static ClassMst get(final Param param, final Map<String, ClassMst> classMstMap, final String key) {
            final ClassMst classMst = classMstMap.get(key);
            if (null == classMst) {
                param.logOnce(" null class mst:" + key);
                return Null;
            }
            return classMst;
        }

        protected static int compareOrder(final Param param, final ClassMst classMst1, final ClassMst classMst2) {
            if (Null == classMst1 && Null == classMst2) {
                return 0;
            } else if (Null == classMst1) {
                return 1;
            } else if (Null == classMst2) {
                return -1;
            }
            int rtn;
            if (!param._notUseClassMstSpecialDiv) {
                rtn = classMst1._specialDiv.compareTo(classMst2._specialDiv);
                if (0 != rtn) { return rtn; }
            }
            rtn = classMst1._showorder.compareTo(classMst2._showorder);
            if (0 != rtn) {
                return rtn;
            }
            rtn = classMst1._classcd.compareTo(classMst2._classcd);
            if (0 != rtn) {
                return rtn;
            }
            if (null != classMst1._schoolKind && null != classMst2._schoolKind) {
                rtn = classMst1._schoolKind.compareTo(classMst2._schoolKind);
            }
            return rtn;
        }

        protected static boolean isSameKey(final Param param, final ClassMst classMst1, final ClassMst classMst2) {
            if (classMst1._classcd.equals(classMst2._classcd) && classMst1._schoolKind.equals(classMst2._schoolKind)) {
                return true;
            }
            return false;
        }
    }

    protected static class AnotherClassMst extends ClassMst {
        protected static final AnotherClassMst Null = new AnotherClassMst("XX", "XX", null, "0", "9", SHOWORDER_DEFAULT);

        protected AnotherClassMst(final String classcd, final String schoolKind, final String classname, final String electdiv, final String specialdiv, final Integer showorder) {
            super(classcd, schoolKind, classname, electdiv, specialdiv, showorder);
        }

        public ClassMst setClassname(final String classname) {
            return new AnotherClassMst(_classcd, _schoolKind, classname, _electdiv, _specialDiv, _showorder);
        }

        public String toString() {
            return "AnotherClassMst(" + getKey(null) + ", " + _classname + ")";
        }

        public static ClassMst getAnother(final Param param, final Map<String, AnotherClassMst> classMstMap, final String key) {
            final AnotherClassMst anotherClassMst = classMstMap.get(key);
            if (null == anotherClassMst) {
                param.logOnce(" null another class mst:" + key);
                return Null;
            }
            return anotherClassMst;
        }
    }

    protected static class SubclassMst implements Comparable<SubclassMst> {
        protected static final SubclassMst ABROAD = new SubclassMst("AA", "AA", "AA", "AAAAAA", _ABROAD, null, new Integer(0), null);
        protected static final SubclassMst Null = new SubclassMst("XX", "XX", "XX", "XX", null, null, SHOWORDER_DEFAULT, null);
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassordername1;
        final Integer _showorder;
        final String _subclasscd2;
        final Map _isYearZenkiKamoku = new HashMap();

        public SubclassMst(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final String subclassname, final String subclassordername1, final Integer showorder, final String subclasscd2) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassordername1 = subclassordername1;
            _showorder = showorder;
            _subclasscd2 = subclasscd2;
        }

        public SubclassMst setSubclassordername(final String subclassordername1) {
            return new SubclassMst(_classcd, _schoolKind, _curriculumCd, _subclasscd, _subclassname, subclassordername1, _showorder, null);
        }

        public String subclassname() {
            return notblankstr(_subclassordername1, _subclassname);
        }

        public String getKey(final Param param) {
            return key(param, _classcd, _schoolKind, _curriculumCd, _subclasscd);
        }

        public boolean isZenkiKamoku(final String year) {
            return "1".equals(_isYearZenkiKamoku.get(year));
        }

//        public boolean equals(final Object o) {
//            if (Null == this || Null == o) {
//                return false;
//            }
//            if (o instanceof SubclassMst) {
//                final SubclassMst osub = (SubclassMst) o;
//                if (null != _classcd && null != osub._classcd && _classcd.equals(osub._classcd)) {
//                    return false;
//                }
//                if (null != _schoolKind && null != osub._schoolKind && _schoolKind.equals(osub._schoolKind)) {
//                    return false;
//                }
//                if (null != _curriculumCd && null != osub._curriculumCd && _curriculumCd.equals(osub._curriculumCd)) {
//                    return false;
//                }
//                if (null != _subclasscd && null != osub._classcd && _classcd.equals(osub._classcd)) {
//                    return false;
//                }
//            }
//            return false;
//        }

        public String toString() {
            return "SubclassMst(" + getKey(null) + ", " + subclassname() + ")";
        }

        protected static String key(final Param param, final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd) {
            return classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
        }

        public static SubclassMst get(final Param param, final Map<String, SubclassMst> subclassMstMap, final String key) {
            final SubclassMst subclassMst = subclassMstMap.get(key);
            if (null == subclassMst) {
                param.logOnce(" null subclass mst:" + key);
                return Null;
            }
            return subclassMst;
        }

        protected static int compareOrder(final Param param, final SubclassMst subclassMst1, final SubclassMst subclassMst2) {
            if (Null == subclassMst1 && Null == subclassMst2) {
                return 0;
            } else if (Null == subclassMst1) {
                return 1;
            } else if (Null == subclassMst2) {
                return -1;
            }
            int rtn;
            if (!param._isSubclassOrderNotContainCurriculumcd) {
                if (null != subclassMst1._curriculumCd && null != subclassMst2._curriculumCd) {
                    rtn = subclassMst1._curriculumCd.compareTo(subclassMst2._curriculumCd);
                    if (0 != rtn) {
                        return rtn;
                    }
                }
            }
            rtn = subclassMst1._showorder.compareTo(subclassMst2._showorder);
            if (0 != rtn) {
                return rtn;
            }
            rtn = subclassMst1._subclasscd.compareTo(subclassMst2._subclasscd);
            return rtn;
        }

        protected static boolean isSameKey(final Param param, final SubclassMst subclassMst1, final SubclassMst subclassMst2) {
            if (param._isSubclassOrderNotContainCurriculumcd) {
                if (subclassMst1._classcd.equals(subclassMst2._classcd) && subclassMst1._schoolKind.equals(subclassMst2._schoolKind) && subclassMst1._subclasscd.equals(subclassMst2._subclasscd)) {
                    return true;
                }
            } else {
                if (subclassMst1._classcd.equals(subclassMst2._classcd) && subclassMst1._schoolKind.equals(subclassMst2._schoolKind) && subclassMst1._curriculumCd.equals(subclassMst2._curriculumCd) && subclassMst1._subclasscd.equals(subclassMst2._subclasscd)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int compareTo(SubclassMst o) {
            return _subclasscd.compareTo(o._subclasscd);
        }
    }

    protected static class AnotherSubclassMst extends SubclassMst {
        protected static final AnotherSubclassMst Null = new AnotherSubclassMst("XX", "XX", "XX", "XX", null, null, SHOWORDER_DEFAULT);

        public AnotherSubclassMst(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final String subclassname, final String subclassordername1, final Integer showorder) {
            super(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassordername1, showorder, null);
        }

        public SubclassMst setSubclassordername(final String subclassordername1) {
            return new AnotherSubclassMst(_classcd, _schoolKind, _curriculumCd, _subclasscd, _subclassname, subclassordername1, _showorder);
        }

        public String toString() {
            return "AnotherSubclassMst(" + getKey(null) + ", " + subclassname() + ")";
        }

        public static SubclassMst getAnother(final Param param, final Map<String, AnotherSubclassMst> anotherSubclassMstMap, final String key) {
            final AnotherSubclassMst anotherSubclassMst = anotherSubclassMstMap.get(key);
            if (null == anotherSubclassMst) {
                param.logOnce(" null another subclass mst:" + key);
                return Null;
            }
            return anotherSubclassMst;
        }
    }

    protected static class CreditMst {

        public static String courseKey(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            return grade + ":" + coursecd + ":" + majorcd + ":" + coursecode;
        }

        public static String subclassKey(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd) {
            return classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
        }

        private static void loadCreditMst(final Param param, final DB2UDB db2, final String year) {
            final Map<String, TreeMap<String, BigDecimal>> creditMst = Util.getMappedMap(param.getYearSubclassCreditMap(), year);
            final StringBuffer sql = new StringBuffer();
            sql.append("   SELECT ");
            sql.append("     GRADE, COURSECD, MAJORCD, COURSECODE, ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("     CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ");
            } else {
                sql.append("     SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, '' AS SCHOOL_KIND, '' AS CURRICULUM_CD, SUBCLASSCD, ");
            }
            sql.append("     CREDITS ");
            sql.append("   FROM CREDIT_MST ");
            sql.append("   WHERE YEAR = '" + year + "' ");
            sql.append("     AND CREDITS IS NOT NULL ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                final String courseKey = CreditMst.courseKey(KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "COURSECD"), KnjDbUtils.getString(row, "MAJORCD"), KnjDbUtils.getString(row, "COURSECODE"));
                final String subclassKey = CreditMst.subclassKey(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CURRICULUM_CD"), KnjDbUtils.getString(row, "SUBCLASSCD"));

                Util.getMappedMap(creditMst, courseKey).put(subclassKey, KnjDbUtils.getBigDecimal(row, "CREDITS", null));
            }
        }

        public static BigDecimal getCredit(final DB2UDB db2, final Param param, final String year, final String grade, final String coursecd, final String majorcd, final String coursecode,
                final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd) {
            final Map<String, Map<String, TreeMap<String, BigDecimal>>> yearSubclassCreditMap = param.getYearSubclassCreditMap();
            if (!yearSubclassCreditMap.containsKey(year)) {
                loadCreditMst(param, db2, year);
            }
            final Map<String, TreeMap<String, BigDecimal>> creditMstMap = Util.getMappedHashMap(yearSubclassCreditMap, year);
            final String courseKey = courseKey(grade, coursecd, majorcd, coursecode);
            if (!creditMstMap.containsKey(courseKey)) {
                return null;
            }
            final String subclassKey = subclassKey(classcd, schoolKind, curriculumCd, subclasscd);
            return Util.getMappedMap(creditMstMap, courseKey).get(subclassKey);
        }
    }

    protected static class CertifSchoolDat {

        String _certifSchoolDatRemark1;
        String _bunkouSchoolAddress1;
        String _bunkouSchoolAddress2;
        String _bunkouSchoolName;
        String _schoolName1;
        /** 原級留置ならtrue
         * 異なる年度で同じ学年があるなら改頁する機能のこと
         */
        boolean _isGenkyuRyuchi;

        static CertifSchoolDat get(final Param param, final String certifKindcd) {
            final Map<String, String> certifSchoolDatMap = param._certifSchoolDatMap.get(certifKindcd);

            CertifSchoolDat certifSchoolDat = new CertifSchoolDat();
            certifSchoolDat._certifSchoolDatRemark1 = defstr(KnjDbUtils.getString(certifSchoolDatMap, "REMARK1"));
            certifSchoolDat._bunkouSchoolAddress1 = defstr(KnjDbUtils.getString(certifSchoolDatMap, "REMARK8"));
            certifSchoolDat._bunkouSchoolAddress2 = defstr(KnjDbUtils.getString(certifSchoolDatMap, "REMARK9"));
            certifSchoolDat._bunkouSchoolName = KnjDbUtils.getString(certifSchoolDatMap, "REMARK4");

            final String certifSchoolDatSchoolName = KnjDbUtils.getString(certifSchoolDatMap, "SCHOOL_NAME");
            if (param._isOutputDebugData) {
                debugLogCheck(param, "CertifSchoolDat.get.schoolName" + certifKindcd, "certifKindcd " + certifKindcd + " : CERTIF_SCHOOL_DAT の学校名称=[" + certifSchoolDatSchoolName + "]");
            }

            certifSchoolDat._schoolName1 = param._schoolMstSchoolName1;

            if (!StringUtils.isEmpty(certifSchoolDatSchoolName)) {
                certifSchoolDat._schoolName1 = certifSchoolDatSchoolName;
            }

            certifSchoolDat._isGenkyuRyuchi = param._is133m ? false : !param._z010.in(Z010.naraken) && "1".equals(KnjDbUtils.getString(certifSchoolDatMap, "REMARK6"));
            if (param._isOutputDebugData) {
                debugLogCheck(param, "CertifSchoolDat.get.genkyuRyuchi" + certifKindcd, "certifKindcd " + certifKindcd + " : 原級留置か? = " + certifSchoolDat._isGenkyuRyuchi);
            }
            return certifSchoolDat;
        }
    }

    protected static class KNJSvfFieldInfo {
        int _x1;   //開始位置X(ドット)
        int _x2;   //終了位置X(ドット)
        int _height;  //フィールドの高さ(ドット)
        int _ystart;  //開始位置Y(ドット)
        int _ystart1;  //開始位置Y(ドット)フィールド1
        int _ystart2;  //開始位置Y(ドット)フィールド2
        int _minnum;  //最小設定文字数
        int _maxnum;  //最大設定文字数
        String _field;
        String _field1;
        String _field2;
        Map _field1Status = Collections.EMPTY_MAP;
        Map _field2Status = Collections.EMPTY_MAP;
        Param _param;
        public KNJSvfFieldInfo(final int x1, final int x2, final int height, final int ystart, final int ystart1, final int ystart2, final int minnum, final int maxnum) {
            _x1 = x1;
            _x2 = x2;
            _height = height;
            _ystart = ystart;
            _ystart1 = ystart1;
            _ystart2 = ystart2;
            _minnum = minnum;
            _maxnum = maxnum;
        }
        public KNJSvfFieldInfo() {
            this(-1, -1, -1, -1, -1, -1, -1, -1);
        }
        public String toString() {
            return "KNJSvfFieldInfo(x1=" + _x1 + ", x2=" + _x2 + ", height=" + _height + ", ystart=" + _ystart + ", ystart1=" + _ystart1 + ", ystart2=" + _ystart2 + ", minnum=" + _minnum + ", maxnum=" + _maxnum + ")";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    protected static class KNJSvfFieldModify {

        private final String _fieldname; // フィールド名
        private final int _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        private static final double dpi = 400.0;
        private static final double pointPerInch = 72;
        protected Param _param;

        public KNJSvfFieldModify(String fieldname, int width, int height, int ystart, int minnum, int maxnum) {
            _fieldname = fieldname;
            _width = width;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

//        /**
//         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
//         * @param posx1 フィールドの左端X
//         * @param posx2 フィールドの右端X
//         * @param num フィールド指定の文字数
//         * @param charSize 変更後の文字サイズ
//         * @return ずれ幅の値
//         */
//        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, float charSize) {
//            final double maxWidth = fieldWidth((double) charSize, num); // 文字の大きさを考慮したフィールドの最大幅
//            final int offset = ((int) maxWidth / 2) - (posx2 - posx1) / 2 + 10;
//            return offset;
//        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public double getCharSize(final String str) {
            final double heightCharPoint = pixelToCharPoint(_height);
            final int byteSize = getStringByteSize(str);
            final double retFieldPoint = retFieldPoint(_width, byteSize);
            final double min = Math.min(heightCharPoint, retFieldPoint);
            if (null != _param && _param.isOutputDebugField(null)) {
                log.info(" getCharSize \"" + str + "\" height = " + _height + ", heightCharPoint = " + heightCharPoint + ", width = " + _width + ", byteSize = " + byteSize + ", retFieldPoint = " + retFieldPoint + ", min = " + min);
            }
            return min;                  //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private int getStringByteSize(String str) {
            return Math.min(Math.max(getMS932ByteLength(str), _minnum), _maxnum);
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charPoint 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static double charPointToPixel(final String debugString, final double charPoint, final int upperOrLower) {
            return SvfFieldAreaInfo.KNJSvfFieldModify.charPointToPixel(debugString, charPoint, upperOrLower);
        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final String debugString, final double charSize, final int upperOrLower, final int keta) {
            return charPointToPixel(debugString, charSize, upperOrLower) * keta / 2;
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharPoint(final int pixel) {
            return pixel * pointPerInch / dpi;
        }

        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public float getYjiku(int hnum, double charSize) {
            float jiku = 0;
            try {
                jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
            } catch (Exception ex) {
                log.error("setRetvalue error!", ex);
                log.debug(" jiku = " + jiku);
            }
            return jiku;
        }

        /**
         *  文字サイズを設定
         */
        protected static double retFieldPoint(int dotWidth, int num) {
            return (float) Math.round((double) dotWidth / (num / 2) * pointPerInch / dpi * 10) / 10;
//            final double rtn = new BigDecimal((double) width / (num / 2) * pointPerInch / dpi).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
//            log.info(" width = " + width + ", num = " + num + " => charwidth point = " + rtn);
//            log.info(" *** " + fieldWidth(rtn, 1, num));
//            return charPointEnabled(rtn, 1);
        }

        protected static double charHeightPixel(final String debugString, final double charSize) {
            return charPointToPixel(debugString, charSize, 0);
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        protected static float retFieldY(int height, double charSize) {
            return (float) Math.round(((double) height - charHeightPixel("retFieldY", charSize)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    enum YOSHIKI {
        NONE,
        _1_OMOTE,
        _1_URA,
        _2_OMOTE,
        _2_URA,
        HOSOKU,
        SHUKKETSUNOKIROKU
    }

    enum YOSHIKI2_OMOTE_VER {
        _BEFORE_3KANTEN
        , _3KANTEN // 2022年度開始3観点様式
        ;

        private static YOSHIKI2_OMOTE_VER next(final YOSHIKI2_OMOTE_VER v) {
            switch (v) {
            case _BEFORE_3KANTEN: return _3KANTEN;
            default:
                return null;
            }
        }

        public static int get3KantenFormStartYear(final Param param) {
            return Util.toInt(param.property(Property.seitoSidoYoroku3KantenStartYear), 2022);
        }

        public static <G extends Gakuseki> Map<YOSHIKI2_OMOTE_VER, List<G>> getYoshikiVerYearListMap(final Param param, final List<G> gakusekiList) {
            final int _3kantenStartYear = get3KantenFormStartYear(param);

            final Map<YOSHIKI2_OMOTE_VER, Integer> verStartYearMap = new TreeMap<YOSHIKI2_OMOTE_VER, Integer>();
            verStartYearMap.put(YOSHIKI2_OMOTE_VER._BEFORE_3KANTEN, -1);
            verStartYearMap.put(YOSHIKI2_OMOTE_VER._3KANTEN, _3kantenStartYear);

            final Map<YOSHIKI2_OMOTE_VER, List<G>> map = new TreeMap<YOSHIKI2_OMOTE_VER, List<G>>();
            YOSHIKI2_OMOTE_VER v = _BEFORE_3KANTEN;
            for (final G gakuseki : gakusekiList) {
                YOSHIKI2_OMOTE_VER next = YOSHIKI2_OMOTE_VER.next(v);
                if (null != next && Util.toInt(gakuseki._year, -1) >= verStartYearMap.get(next)) {
                    v = next;
                }
                getMappedList(map, v).add(gakuseki);
            }
            return map;
        }
    }

    protected static class Range<T extends Comparable<T>> {
        // null考慮しない
        final T _start;
        final T _end;
        private Range(final T start, final T end) {
            _start = start;
            _end = end;
        }
        public static <T extends Comparable<T>> Range<T> of(T t1, T t2) {
            if (t1.compareTo(t2) > 0) {
                final T temp = t1;
                t1 = t2;
                t2 = temp;
            }
            return new Range<T>(t1, t2);
        }
        public static <T extends Comparable<T>> Range<T> of(Collection<T> col) {
            if (null == col || col.isEmpty()) {
                throw new IllegalArgumentException("empty collection : " + col);
            }
            T min = null;
            T max = null;
            for (final T t : col) {
                if (null == t) {
                    continue;
                }
                if (null == min) {
                    min = t;
                }
                if (null == max) {
                    max = t;
                }
                if (t.compareTo(min) < 0) {
                    min = t;
                }
                if (max.compareTo(t) > 0) {
                    max = t;
                }
            }
            if (null == min || null == max) {
                throw new IllegalArgumentException("min is null or max is null : " + col);
            }
            return new Range<T>(min, max);
        }
        private static <T extends Comparable<T>> boolean isLower(final T t1, final T t2, final boolean whenEq) {
            return t1.compareTo(t2) < 0 || whenEq && t1.compareTo(t2) == 0;
        }
        public boolean containsAll(final Range<T> o, final boolean whenEq) {
            return contains(o._start, whenEq) && contains(o._end, whenEq);
        }
        public boolean contains(final T t, final boolean whenEq) {
            return isLower(_start, t, whenEq) && isLower(t, _end, whenEq);
        }
        public boolean isOverlapped(final Range<T> o, final boolean whenEq) {
            return contains(o._start, whenEq) || contains(o._end, whenEq);
        }
        public String toString() {
            return "Range(" + _start + " ~ " + _end + ")";
        }
    }

    protected static class Util {

        /**
         * listを最大数ごとにグループ化したリストを得る
         * @param list
         * @param max 最大数
         * @return listを最大数ごとにグループ化したリスト
         */
        protected static <T> List<List<T>> getPageList(final List<T> list, final int max) {
            final List<List<T>> rtn = new ArrayList();
            List<T> current = null;
            for (final T o : list) {
                if (null == current || current.size() >= max) {
                    current = new ArrayList<T>();
                    rtn.add(current);
                }
                current.add(o);
            }
            return rtn;
        }

        protected static <T> T def(final T ... ts) {
            if (null != ts) {
                for (final T t : ts) {
                    if (null != t) {
                        return t;
                    }
                }
            }
            return null;
        }

        protected static String defstr(final Object ... os) {
            if (null == os) {
                return "";
            }
            if (os.length == 1) {
                return StringUtils.defaultString(Util.str(os[0]));
            }
            for (final Object o : os) {
                if (null != o && null != o.toString()) {
                    return o.toString();
                }
            }
            return "";
        }

        protected static String notblankstr(final Object ... os) {
            if (null == os) {
                return "";
            }
            if (os.length == 1) {
                return StringUtils.defaultString(Util.str(os[0]));
            }
            for (final Object o : os) {
                if (null != o && !StringUtils.isBlank(o.toString())) {
                    return o.toString();
                }
            }
            return "";
        }

        protected static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<T>());
            }
            return map.get(key1);
        }

        protected static Calendar getCalendarOfDate(final String date) {
            final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            final Calendar cal = Calendar.getInstance();
            cal.setTime(sqlDate);
            return cal;
        }

        protected static int getNendo(final Calendar cal) {
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH);
            if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
                return year - 1;
            }
            return year;
        }

        protected static boolean isDayOfMonth(final String date, final int month, final int dayOfMonth) {
            return String.valueOf(month) == month(date) && String.valueOf(dayOfMonth) == dayOfMonth(date);
        }

        protected static String month(final String date) {
            try {
                return String.valueOf(getCalendarOfDate(date).get(Calendar.MONTH) + 1);
            } catch (Exception e) {
            }
            return null;
        }

        protected static String dayOfMonth(final String date) {
            try {
                return String.valueOf(getCalendarOfDate(date).get(Calendar.DAY_OF_MONTH));
            } catch (Exception e) {
            }
            return null;
        }

        protected static String year(final String date) {
            try {
                return String.valueOf(getCalendarOfDate(date).get(Calendar.YEAR));
            } catch (Exception e) {
            }
            return null;
        }

        protected static String nendo(final String date) {
            if (null == date) {
                return null;
            }
            try {
                final String year = year(date);
                final String month = month(date);
                String nendo = year;
                if (NumberUtils.isDigits(year) && NumberUtils.isDigits(month) && Integer.parseInt(month) <= 3) {
                    nendo = String.valueOf(Integer.parseInt(year) - 1);
                }
                return nendo;
            } catch (Exception e) {
                log.error("exception! date = " + date, e);
            }
            return null;
        }

        protected static boolean dateIsInNendo(final String date, final String year) {
            if (null != date && date.compareTo(year + "-04-01") >= 0 && date.compareTo((Integer.parseInt(year) + 1) + "-03-31") <= 0) {
                return true;
            }
            return false;
        }

        protected static String toDigit(final String s, final String def) {
            return NumberUtils.isDigits(s) ? String.valueOf(Integer.parseInt(s)) : def;
        }

        protected static String addNumber(final String i1, final String i2) {
            if (!NumberUtils.isDigits(i1)) return i2;
            if (!NumberUtils.isDigits(i2)) return i1;
            return String.valueOf((!NumberUtils.isDigits(i1) ? 0 : Integer.parseInt(i1)) + (!NumberUtils.isDigits(i2) ? 0 : Integer.parseInt(i2)));
        }

        protected static Integer addNumber(final Integer i1, final Integer i2) {
            if (null == i1) return i2;
            if (null == i2) return i1;
            return new Integer((null == i1 ? 0 : i1.intValue()) + (null == i2 ? 0 : i2.intValue()));
        }

        protected static BigDecimal addNumber(final BigDecimal i1, final BigDecimal i2) {
            if (null == i1) return i2;
            if (null == i2) return i1;
            return i1.add(i2);
        }

        protected static boolean isNyugakumae(final String year) {
            return NumberUtils.isDigits(year) && 0 == Integer.parseInt(year);
        }

        public static boolean isNextDate(final String date1, final String date2) {
            if (null == date1 || null == date2) {
                return false;
            }
            final Calendar cal1 = toCalendar(date1);
            final Calendar cal2 = toCalendar(date2);
            cal1.add(Calendar.DATE, 1);
            return cal1.equals(cal2);
        }

        public static Calendar toCalendar(final String date) {
            final Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(java.sql.Date.valueOf(date));
            } catch (Exception e) {
                log.error("exception! " + date, e);
            }
            return cal;
        }

        protected static <T> boolean containsAny(final Collection<T> a, final Collection<T> b) {
            return !intersection(a, b).isEmpty();
        }

        protected static <T> List<T> intersection(final Collection<T> a, final Collection<T> b) {
            final List<T> rtn = new ArrayList<T>();
            for (final T t : a) {
                if (b.contains(t)) {
                    rtn.add(t);
                }
            }
            return rtn;
        }

        protected static <T> List<T> filter(final Collection<T> c, final Predicate<T> p) {
            if (null == p) {
                return new ArrayList<T>(c);
            }
            final List<T> rtn = new ArrayList<T>();
            for (final T t : c) {
                if (p.test(t)) {
                    rtn.add(t);
                }
            }
            return rtn;
        }

        protected static int toInt(final String s, final int def) {
            return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
        }

        public static List<String> generate(final Param param, final int moji, final int gyo) {
            final Random r = new Random();
            final StringBuffer stb = new StringBuffer();
            final char min = 'あ';
            final char max = 'ん';
            for (int n = 0; n < moji * gyo; n++) {
                final char ch = (char) (min + r.nextInt(max - min));
                stb.append(ch);
                if (r.nextDouble() < 0.08) {
                    stb.append("、");
                } else if (r.nextDouble() < 0.04) {
                    stb.append("。");
                    if (r.nextDouble() < 0.25) {
                        stb.append("\n");
                    }
                }
            }
            return Util.getTokenList(param, stb.toString(), moji * 2);
        }

        protected static String minDate(final String date1, final String date2) {
            if (null == date1) {
                return date2;
            } else if (null == date2) {
                return date1;
            }
            if (date1.compareTo(date2) <= 0) {
                return date1;
            }
            return date2;
        }

        protected static String maxDate(final String date1, final String date2) {
            if (null == date1) {
                return date2;
            } else if (null == date2) {
                return date1;
            }
            if (date1.compareTo(date2) <= 0) {
                return date2;
            }
            return date1;
        }

        protected static String prepend(final String prep, final Object o) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : prep + o.toString();
        }

        protected static String append(final Object o, final String app) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : o.toString() + app;
        }

        protected static List<String> toCharList(final String src) {
            final List<String> rtn = new ArrayList<String>();
            if (null != src) {
                for (int j = 0; j < src.length(); j++) {
                    //1文字を取り出す
                    rtn.add(src.substring(j, j + 1));
                }
            }
            return rtn;
        }

        protected static String insertBetween(final String a, final String b, final String insert) {
            String rtn = defstr(a);
            if (!StringUtils.isEmpty(a) && !StringUtils.isEmpty(b)) {
                rtn += insert;
            }
            rtn += defstr(b);
            return rtn;
        }

        protected static List<String> getTokenList(final String source0, final int bytePerLine) {
            if (source0 == null || source0.length() == 0) {
                return Collections.emptyList();
            }
            final String source = StringUtils.replace(StringUtils.replace(source0, "\r\n", "\n"), "\r", "\n");

            final List<String> tokenList = new ArrayList<String>();        //分割後の文字列の配列
            int startIndex = 0;                         //文字列の分割開始位置
            int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
            for (int idx = 0; idx < source.length(); idx += 1) {
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
        } //String get_token()の括り

        protected static String setKeta(final String str, final int keta) {
            return defstr(str) + StringUtils.repeat(" " , keta - getMS932ByteLength(str) % keta);
        }

        protected static List<String> getTokenList(final Param param, final String strx, final int keta) {
            if (param._useEditKinsoku || param._z010.in(Z010.kyoto) && !"1".equals(param._seitoSidoYorokuKinsokuForm)) {
                return KNJ_EditKinsoku.getTokenList(strx, keta);
            }
            return getTokenList(strx, keta);
        }

        protected static boolean dateBetweenYear(final Param param, final String date, final int yearMin, final int yearMax, final String comment) {
            if (null == date) {
                return false;
            }
            final int year = getNendo(getCalendarOfDate(date));
            final boolean b = yearMin <= year && year <= yearMax;
            if (param._isOutputDebug && null != comment) {
                log.info(" " + comment + " date nendo = " + getNendo(getCalendarOfDate(date)) + ", " + yearMin + ", " + yearMax + ", " + b);
            }
            return b;
        }

        protected static List<String> getTokenList(final Param param, final String strx, final int keta, final int gyo) {
            List<String> tokenList = getTokenList(param, strx, keta);
            if (tokenList.size() > gyo) {
                log.info(" afure ...  keta = " + keta + ", gyo = " + gyo + ", data = " + Util.take(20, strx) + "..., tokenList size = " + tokenList.size());
                tokenList = tokenList.subList(0, gyo);
            }
            if (param._isOutputDebugKinsoku) {
                for (int i = 0; i < tokenList.size(); i++) {
                    log.info(" token (" + keta + " x " + gyo + ") " + i + " = " + tokenList.get(i));
                }
            }
            return tokenList;
        }


        public static String[] get_token(final Param param, final String strx, final KNJPropertiesShokenSize size) {
            return get_token(param, strx, size.getKeta(), size._gyo);
        }

        public static String[] get_token(final Param param, final String strx, final int keta, final int gyo) {
            List<String> tokens = new ArrayList<String>(getTokenList(param, strx, keta));
            for (int i = tokens.size(); i < gyo; i++) {
                tokens.add(null);
            }
            tokens = tokens.subList(0, gyo);
            final String[] rtn = new String[gyo];
            for (int i = 0; i < rtn.length; i++) {
                rtn[i] = tokens.get(i);
            }
            return rtn;
        }

        protected static List<String> valueList(final Map row, final List<String> fieldList) {
            final List<String> groupKeyVals = new ArrayList();
            for (final String field : fieldList) {
                groupKeyVals.add(KnjDbUtils.getString(row, field));
            }
            return groupKeyVals;
        }

        protected static Map<String, String> keyValueMap(final Map row, final List<String> fieldList) {
            final Map<String, String> keyValueMap = new TreeMap<String, String>();
            for (final String field : fieldList) {
                keyValueMap.put(field, KnjDbUtils.getString(row, field));
            }
            return keyValueMap;
        }

        protected static BigDecimal bdAvg(final List<BigDecimal> bigDecimalDataList, final int scale) {
            if (bigDecimalDataList.size() == 0) {
                return null;
            }
            BigDecimal sum = new BigDecimal("0");
            for (final BigDecimal bd : bigDecimalDataList) {
                sum = sum.add(bd);
            }
            return sum.divide(new BigDecimal(bigDecimalDataList.size()), scale, BigDecimal.ROUND_HALF_UP);
        }

        protected static BigDecimal bdSum(final Collection<BigDecimal> bigDecimalDataList) {
            if (bigDecimalDataList.size() == 0) {
                return null;
            }
            BigDecimal sum = new BigDecimal("0");
            for (final BigDecimal bd : bigDecimalDataList) {
                sum = sum.add(bd);
            }
            return sum;
        }

        protected static BigDecimal toBdSum(final Collection<String> dataList) {
            return bdSum(toBigDecimalList(dataList));
        }

        protected static List<BigDecimal> toBigDecimalList(final Collection<String> dataList) {
            final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
            for (final String s : dataList) {
                if (null == s) {
                    continue;
                }
                if (!NumberUtils.isNumber(s)) {
                    log.warn("not number: " + s);
                    continue;
                }
                rtn.add(new BigDecimal(s));
            }
            return rtn;
        }

        protected static BigDecimal numberMax(final List<BigDecimal> bigDecimalDataList) {
            final TreeSet<BigDecimal> set = new TreeSet<BigDecimal>(bigDecimalDataList);
            if (set.isEmpty()) {
                return null;
            }
            return set.last();
        }

        protected static String stringMin(final List<String> dataList) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final String s : dataList) {
                if (null != s) {
                    set.add(s);
                }
            }
            if (set.isEmpty()) {
                return null;
            }
            return set.first();
        }

        protected static List<BigDecimal> filterOver(final List<BigDecimal> bigDecimalDataList, final double lower) {
            final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
            for (final BigDecimal bd : bigDecimalDataList) {
                if (lower < bd.doubleValue()) {
                    rtn.add(bd);
                }
            }
            return rtn;
        }

        protected static Integer integerSum(final List<String> strNumList) {
            Integer sum = null;
            for (final BigDecimal bd : toBigDecimalList(strNumList)) {
                if (null == sum) {
                    sum = new Integer(0);
                }
                sum = new Integer(sum.intValue() + bd.intValue());
            }
            return sum;
        }

        protected static String rightPadding(final String s, final int keta) {
            return defstr(s) + StringUtils.replace(StringUtils.repeat(" ", keta - getMS932ByteLength(s)), "  ", "　");
        }

        protected static StringBuffer mkString(final Collection<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (final String line : list) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                stb.append(c).append(line);
                c = comma;
            }
            return stb;
        }

        protected static StringBuffer mkString(final Map<String, String> flgMap, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (final String key : flgMap.keySet()) {
                if (StringUtils.isBlank(key)) {
                    continue;
                }
                final String val = flgMap.get(key);
                stb.append(c).append(key).append("=").append(val);
                c = comma;
            }
            return stb;
        }

        protected static String centering(final String s, final int size) {
            if (StringUtils.isEmpty(s) || size < s.length()) {
                return null;
            }
            final int spaceSize = (size - s.length()) / 2;
            return StringUtils.repeat(" ", spaceSize) + s + StringUtils.repeat(" ", size - spaceSize - s.length());
        }

        protected static String kakko(final String v) {
            return (null == v) ? null : "(" + v + ")";
        }

        protected static String str(final Object o) {
            return (null == o) ? null : o.toString();
        }

        protected static String take(final int count, final String s) {
            return s.substring(0, Math.min(count, s.length()));
        }

        protected static <T> List<T> take(final int count, final List<T> list) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (int i = 0; i < count && i < list.size(); i++) {
                rtn.add(list.get(i));
            }
            return rtn;
        }
        protected static <T> List drop(final int count, final List<T> list) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (int i = count; i < list.size(); i++) {
                rtn.add(list.get(i));
            }
            return rtn;
        }
        protected static <T> T head(final List<T> list, final T t) {
            if (list.size() > 0) {
                return list.get(0);
            }
            return t;
        }
        protected static <T> T last(final List<T> list, final T t) {
            if (list.size() > 0) {
                return list.get(list.size() - 1);
            }
            return t;
        }
        protected static <T, A extends Collection<T>, B extends Collection<A>> List<T> flatten(final B list) {
            final List<T> rtn = new ArrayList<T>();
            for (final Collection<T> t : list) {
                rtn.addAll(t);
            }
            return rtn;
        }
        protected static <T, A extends Collection<T>> List<T> concat(final A ...list) {
            final List<T> rtn = new ArrayList<T>();
            for (final Collection<T> t : list) {
                rtn.addAll(t);
            }
            return rtn;
        }
        protected static <T> List<T> setMinSize(final List<T> list, final int size) {
            final List<T> rtn = new ArrayList<T>(list);
            while (rtn.size() < size) {
                rtn.add(null);
            }
            return rtn;
        }
        protected static <T> List<T> reverse(final Collection<T> col) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (final ListIterator<T> it = new ArrayList<T>(col).listIterator(col.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        protected static <K, V, U> TreeMap<V, U> getMappedMap(final Map<K, TreeMap<V, U>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap<V, U>());
            }
            return map.get(key1);
        }

        protected static <K, V, U> Map<V, U> getMappedHashMap(final Map<K, Map<V, U>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap<V, U>());
            }
            return map.get(key1);
        }

        protected static <A, B> TreeSet<B> getMappedTreeSet(final Map<A, TreeSet<B>> map, final A key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeSet<B>());
            }
            return map.get(key1);
        }

        private static Map keyValueArrayToMap(final Object[] keyValue) {
            if (null == keyValue || keyValue.length % 2 != 0) {
                throw new IllegalArgumentException("keyValue = " + keyValue);
            }
            final Map rtn = new HashMap();
            for (int i = 0; i < keyValue.length; i += 2) {
                rtn.put(keyValue[i], keyValue[i + 1]);
            }
            return rtn;
        }

        public static Integer parseInteger(final String s) {
            if (NumberUtils.isNumber(s) && -1 == s.indexOf(".")) {
                return Integer.valueOf(s);
            }
            if (!StringUtils.isBlank(s)) {
                log.warn("try to parseInteger not-integer-value:" + s);
            }
            return null;
        }

        public static int parseIntSafe(final String s, final int def) {
            if (NumberUtils.isNumber(s) && -1 == s.indexOf(".")) {
                return Integer.parseInt(s);
            }
            if (!StringUtils.isBlank(s)) {
                log.warn("try to parseInt not-int-value:" + s);
            }
            return def;
        }

        public static String listString(final Collection coll, final int depth) {
            if (null == coll) {
                return "null";
            } else if (coll.size() == 0) {
                return "[]";
            }
            final String space = StringUtils.repeat("  ", depth);
            final StringBuffer stb = new StringBuffer();
            stb.append(space).append("[").append("\n");
            String comma = "  ";
            final List list = new ArrayList(coll);
            for (int i = 0; i < list.size(); i++) {
                final Object o = list.get(i);
                stb.append(space).append(comma);
                if (o instanceof List) {
                    stb.append(listString((List) o, depth + 1));
                } else if (null == o) {
                    stb.append("null");
                } else if (o instanceof Map.Entry) {
                    final Map.Entry e = (Map.Entry) o;
                    final Object v = e.getValue();
                    stb.append(space).append(e.getKey()).append("=").append(v instanceof Collection ? listString((Collection) v, depth + 1) : v);
                } else if (list.size() == 0) {
                    stb.append("[]");
                } else {
                    stb.append(o.toString());
                }
                stb.append("\n");
                comma = ", ";
            }
            stb.append(space).append("]").append("\n");
            return stb.toString();
        }

        public static String debugMapToStr(final String debugText, final Map map0, final String split) {
            final Map m = new HashMap();
            m.putAll(map0);
            for (final Iterator<Map.Entry> it = m.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = it.next();
                if (e.getKey() instanceof Integer) {
                    it.remove();
                } else if (e.getKey() instanceof String) {
                    final String key = (String) e.getKey();
                    final int numIndex = StringUtils.indexOfAny(key, "123456789");
                    if (1 <= numIndex && StringUtils.repeat("_", numIndex).equals(key.substring(0, numIndex))) {
                        it.remove();
                    }
                }
            }
            final Map map = new TreeMap(m);
            final StringBuffer stb = new StringBuffer();
            stb.append(defstr(debugText));
            stb.append(" [");
            final List keys = new ArrayList(map.keySet());
            try {
                Collections.sort(keys);
            } catch (Exception e) {
            }
            final List<String> elems = new ArrayList<String>();
            for (int i = 0; i < keys.size(); i++) {
                final Object key = keys.get(i);
                final Object val = map.get(key);
                if (val instanceof Map) {
                    elems.add(key + ": " + debugMapToStr("", (Map) val, split));
                } else {
                    elems.add(key + ": " + val);
                }
            }
            stb.append(mkString(elems, split));
            stb.append("]");
            return stb.toString();
        }

        public static <T> List<List<T>> listToListList(final List<T> l) {
            final List<List<T>> rtn = new ArrayList<List<T>>();
            for (final T t : l) {
                rtn.add(Collections.singletonList(t));
            }
            return rtn;
        }

        public static String[] csvToArray(final String csv) {
            return null == csv ? new String[] {} : csv.split("\\s*,\\s*");
        }
    }

    protected static enum Z010 {
        HOUSEI("法政")
      , hirogaku("広島国際学院")
      , tokyoto("東京都") // 2018/03 終了
      //, Yuushinkan("熊本湧心館")
      , kumamoto("熊本県")
      , KINDAI("近大付属")
      , CHIBEN("智辯学園")
      , tottori("鳥取県")
      , kyoai("共愛")
      , kyoto("京都府")
      , nishiyama("西山")
      , meiji("明治")
      , tokiwa("常磐")
      , miyagiken("宮城県")
      , bunkyo("文京")
      , rakunan("洛南")
      , kaijyo("海城")
      , musashinohigashi("武蔵野東")
      , mieken("三重県")
      , sundaikoufu("駿台甲府")
      , seijyo("成城")
      , tosa("土佐塾")
      , fukuiken("福井県")
      , chiyodaKudan("chiyoda", "千代田区九段")
      , tokiwagi("常盤木")
      , sapporo("札幌開成")
      , chukyo("中京")
      , meikei("茗溪")
      , osakatoin("大阪桐蔭")
      , TamagawaSei("Tamagawa-sei", "玉川聖")
      , hirokoudai("広島工業大学付属")
      , yamamura("山村学園")
      , naraken("奈良県")
      , tosajoshi("土佐女子")
      , kikan("開智一貫部")
      , ktsushin("開智通信制")
      , sagaken("佐賀県")
      , osakashinnai("大阪信愛")
      , risshisha("立志舎")
      , hagoromo("羽衣")
      , sanonihon("佐野日本")
      , nagisa("広島なぎさ")
      , naganoSeisen("seisen", "長野清泉")
      , komazawa("koma", "駒澤大学")
      , reitaku("麗澤")
      , matsudo("松戸")
      , shimaneken("島根県")
      , ryukei("流通経済大付属")
      , hibarigaoka("雲雀丘")
      , aoyama("青山学院")
      , doshisha("同志社")
      , jogakkan("女学館")
      , teihachi("帝京八王子")
      , jyoto("福岡工業大学付属城東高校")
      , kenja("賢者")
      ;

        final String _name1;
        final String _gakkoumei;
        Z010(final String gakkoumei) {
            this(null, gakkoumei);
        }
        Z010(final String name1, final String gakkoumei) {
            _name1 = name1;
            _gakkoumei = gakkoumei;
        }

        public boolean in(final Z010 ...z010s) {
            return ArrayUtils.contains(z010s, this);
        }

        public String toString() {
            return "Z010(name = " + this.name() + ", " + _gakkoumei + ")";
        }

        public static Z010 fromName1(final String name1) {
            for (final Z010 z010 : values()) {
                if (null == z010._name1) {
                    if (z010.name().equals(name1)) {
                        return z010;
                    }
                } else {
                    if (z010._name1.equals(name1)) {
                        return z010;
                    }
                }
            }
            return kenja;
        }
    }

    protected enum Property {
        useSchregRegdHdat // 名称マスタA021を使用するなら"0" 組名称にSCHREG_REGD_HDATのHR_CLASS_NAME1を使用するなら"1"
      , seitoSidoYorokuFieldSize
      , seitoSidoYorokuSougouFieldSize
      , seitoSidoYorokuSpecialactremarkFieldSize
      , seitoSidoYorokuUseEditKinsokuH
      , seitoSidoYorokuKinsokuForm
      , seitoSidoYorokuZaisekiMae
      , seitoSidoYorokuKoumokuMei
      , seitoSidoYorokuHyotei0ToBlank // 指導要録の学習の記録で評定0はブランク表示にする
      , seitoSidoYorokuPrintOrder // 指導要録の表示順を変更する (留年と複数枚表示に影響する)
      , seitoSidoYorokuYoshiki2PrintOrder // 指導要録の様式２の表示順を変更する (留年と複数枚表示に影響する)
      , seitoSidoYorokuNotPrintAnotherStudyrec // 指導要録のSCHOOLCD='1'のSCHREG_STUDYREC_DATを読み込みしない
      , seitoSidoYorokuNotPrintAnotherAttendrec // 指導要録のSCHOOLCD='1'のSCHREG_ATTENDREC_DATを読み込みしない
      , seitoSidoYorokuHozonkikan
      , seitoSidoYorokuNotUseSubclassSubstitution
      , seitoSidoYorokuFinschoolFinishDateYearOnly // 中学校卒業日付のフォーマットは年のみ。それ以外は年月
      , seitoSidoYorokuZaisekiSubekiKikanMaxMonth // 在籍すべき期間MAX月
      , seitoSidoYorokuNotPrintZaisekiSubekiKikan // 在籍すべき期間を印字しない
      , seitoSidoYorokuZaisekiSubekiKikanSubtractEntGrade // 在籍すべき期間から入学時の学年-入学学年を引く
      , seitoSidoYorokuSougouHyoukaNentani // 総合的な学習の時間は年単位
      , seitoSidoYorokuHoushiNentani // 2なら奉仕の記録は通年
      , seitoSidoYorokuUsePrevSchoolKindGrdDivNameAsFinschoolGrdName // 中学の卒業区分名を出身学校の卒業名に使用する
      , seitoSidoYorokuNotPrintFinschoolGrdDivDefaultName // 出身学校がない場合固定の「卒業」を印字しない
      , seitoSidoYorokuPrintCoursecodename // 学科名の後にコース名を印字する
      , seitoSidoYorokuNotPrintCoursecodes
      , seitoSidoYorokuPrintDropRecInTenTaiYear // 「転学退学の年度に成績が無い場合、留年の年度の成績を印字」
      , seitoSidoYorokuPrintGappeimaeSchoolname // 「合併前の学校名を印字」
      , seitoSidoYorokuPrintForm1StaffGrade // 様式1表の担任欄の学年を表示
      , seitoSidoYorokuPrintHennyuEntReasonToKeireki // 様式1表で編入生の入学各文言を経歴欄に出力する
      , seitoSidoYorokuPrintEntGrdReason // 入学事由、除籍事由を出力する
      , seitoSidoYorokuPrintTitleHogoshaTou // 「保護者等」を出力する
      , seitoSidoYoroku3KantenStartYear // 3観点開始年度
      , sogoTankyuStartYear // 総合的な探究の時間開始年度
      , seitoSidoYoroku_dat_TotalstudyactSize // 指導要録所見入力の「活動内容」のサイズ（文字数 * 行数）
      , seitoSidoYoroku_dat_TotalstudyvalSize // 指導要録所見入力の「評価」のサイズ（文字数 * 行数）
      , seitoSidoYoroku_dat_TotalremarkSize // 指導要録所見入力の「総合所見」のサイズ（文字数 * 行数））
      , seitoSidoYoroku_dat_SpecialactremarkSize // 指導要録所見入力の「特別活動所見」のサイズ（文字数 * 行数）
      , seitoSidoYoroku_dat_Attendrec_RemarkSize // 指導要録所見入力の「出欠の記録備考」のサイズ（文字数 * 行数）
      , certifPrintRealName
      , seitoSidoYorokuFormType
      , seitoSidoYorokuTaniPrintAbroad
      , seitoSidoYorokuTaniPrintSogaku
      , seitoSidoYorokuTaniPrintTotal
      , useGakkaSchoolDiv
      , useAddrField2
      , useProvFlg
      , seitoSidoYorokuSogoShoken6BunkatsuStartYear
      , seitoSidoYorokuSogoShoken6Bunkatsu
      , seitoSidoYorokuSogoShoken3Bunkatsu
      , seitoSidoYoroku_train_ref_1_2_3_field_size
      , train_ref_1_2_3_field_size
      , seitoSidoYoroku_train_ref_1_2_3_gyo_size
      , train_ref_1_2_3_gyo_size
      , seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg // キャリアプラン
      , notPrintFinschooltypeName // FINSCHOOL_MST.FINSCHOOL_TYPE(名称マスタ「L019」、「中学校」「小学校」等)を表示しない
      , notPrintFinschooldistcdName // FINSCHOOL_MST.FINSCHOOL_DISTCD(名称マスタ「L001」、「～立」)を表示しない
      , HR_ATTEND_DAT_NotSansyou // (通信制用) 1ならSCHREG_ATTENDREC_DAT.PRESENTを印刷する
      , seitoSidoYorokuCreditOnlyClasscd
      , seitoSidoYorokuHanasuClasscd
      , useStudyrecRemarkQualifiedDat
      , seitoSidoYorokuTotalStudyCombineHtrainremarkDat
      , seitoSidoYorokuPrintHosoku
      , seitoSidoYorokuYoshiki1UraBunkatsuRishu
      , seitoSidoYorokuNotUseCompCredit
      , hyoteiYomikaeRadio
      ,
      // 以下KNJA130
      useSpecial_Support_School,
      seitoSidoYoroku_dat_TotalstudyactSize_disability,
      seitoSidoYoroku_dat_TotalstudyvalSize_disability,
      seitoSidoYoroku_dat_TotalremarkSize_disability,
      seitoSidoYoroku_dat_Attendrec_RemarkSize_disability,
      HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H,
      HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H,
      HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability,
      useCurriculumcd,
      // 以下KNJA129
      seitoSidoYorokuOnlineJiyuSize,
      seitoSidoYorokuOnlineJisshiHouhouSize,
      seitoSidoYorokuOnlineSonotaGakushuSize

    }

    protected interface PsStore {
        void setPs(final String psKey, final DB2UDB db2, final String sql);

        PreparedStatement getPs(final String psKey);

        void psCloseQuietly();
    }

    protected static class Param implements PsStore {

        protected final String SCHOOL_KIND;
        protected boolean _isSenkouka = false;

        final String _cmd;
        final boolean _isCsv;
        final String _year;
        final String _gakki;
        boolean _is133m = false;
        boolean _is130 = false;
        final Map<String, List<String>> _paramap;
        final Map<String, String> _otherParamMap;

        KNJDefineSchool _definecode = new KNJDefineSchool();
        final boolean _isDefinecodeSchoolMarkHiro;
        final boolean _isDefinecodeSchoolMarkK;
        /** 陰影保管場所(陰影出力に関係する) */
        final String _documentroot;

        /** 中高一貫ならtrue */
        final boolean _isChuKouIkkan;
        /** Z010 */
        final String _z010name1;
        final Z010 _z010;
        /** 併設校ならtrue */
        final boolean _isHeisetuKou;
        /** 東大阪大学付属高校(敬愛 or 柏原)ならtrue */
        final boolean _isHigashiosaka;
        /** 東大阪大学付属高校(柏原)ならtrue */
        final boolean _isHigashiosakaKashiwara;
        /** 開智ならtrue */
        final boolean _isKaichi;
        /** 佐野日本大付属高校ならtrue*/
        final boolean _isSanonihonHs;
        /** 佐野日本大付属中等教育学校ならtrue*/
        final boolean _isSanonihonSs;

        /** 西暦を使用するか */
        public boolean _isSeireki;

        /** 茗渓の様式1裏 IBコース用 */
        final String _ibCourse;
        final boolean _notUseClassMstSpecialDiv;

        /** 1なら true */
        final boolean _seitoSidoYorokuSougouFieldSize;
        /** 1なら true */
        final boolean _seitoSidoYorokuSpecialactremarkFieldSize;
        final boolean _useEditKinsoku;
        /** 1なら true */
        final String _seitoSidoYorokuKinsokuForm;

        /** 1なら true */
        final boolean _seitoSidoYorokuZaisekiMae;
        /** 指導要録保存期間 */
        final int _seitoSidoYorokuHozonkikan;
        /** 1なら代替科目を一切印字しない。1以外はCSV、1ura,2omote,2ura */
        final String[] _seitoSidoYorokuNotUseSubclassSubstitution;
        /** 学科名の後にコース名を印字しないコースコード */
        final List<String> _seitoSidoYorokuNotPrintCoursecodes;

        final boolean _replacePrintGradeCdWithGrade;

        /** 写真データ格納フォルダ */
        final String _imageDir;
        /** 写真データの拡張子 */
        final String _imageExt;

        final String _useCurriculumcd;
        final boolean _isSubclassOrderNotContainCurriculumcd;
        final boolean _isAddressOutput2;

        final String _formatDateDefaultYear;

        Map _hmap = null;
        protected String _mirishuRemarkFormat; // 未履修（履修不認定）備考フォーマット
        protected String _rishunomiRemarkFormat; // 履修のみ（修得不認定）備考フォーマット
        final StringTemplate _substRemark90Template;
        final StringTemplate _substRemarkNot90Template;
        final String _staffGroupcd; // 京都府は、印刷する職員のグループコードのMAXの頭3桁が999の場合、印影を表示する

        private Properties _prgInfoPropertiesFilePrperties;
        private Map<String, String> _dbPrgInfoProperties;

        boolean _isOutputDebugAll;
        boolean _isOutputDebug;
        boolean _isOutputDebugGakuseki;
        boolean _isOutputDebugSeiseki;
        boolean _isOutputDebugData;
        boolean _isOutputDebugKinsoku;
        List<String> _outputDebugFieldList = null;
        boolean _isOutputDebugQuery;
        boolean _isOutputDebugSvfOut;
        boolean _isOutputDebugFormCreate;
        boolean _isOutputDebugFormRecordInfo;
        boolean _isOutputDebugInner;
        boolean _isOutputDebugStaff;

        final boolean _hasAFT_GRAD_COURSE_DAT;               // テーブル AFT_GRAD_COURSE_DATがあるか
        final boolean _hasANOTHER_CLASS_MST;                // テーブル ANOTHER_CLASS_MSTがあるか
        final boolean _hasANOTHER_SUBCLASS_MST;             // テーブル ANOTHER_SUBCLASS_MSTがあるか
        final boolean _hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE; // カラム SCHREG_ENT_GRD_HIST_DAT.TENGAKU_SAKI_GRADEがあるか
        final boolean _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT;    // テーブル SCHREG_ENT_GRD_HIST_COMEBACK_DATがあるか
        final boolean _hasSCHREG_STUDYREC_DETAIL_DAT;        // テーブル SCHREG_STUDYREC_DETAIL_DATがあるか
        final boolean _hasMAJOR_MST_MAJORNAME2;             // カラム MAJOR_MST.MAJORNAME2があるか
        final boolean _hasCOURSECODE_MST_COURSECODEABBV1;             // カラム COURSECODE_MST.COURSECODEABBV1があるか
        final boolean _hasSCHREG_TRANSFER_DAT_REMARK1;       // カラム SCHREG_TRANSFER_DAT.REMARK1 があるか
        final boolean _hasSCHREG_TRANSFER_DAT_ABROAD_PRINT_DROP_REGD;
        final boolean _hasSCHREG_STUDYREC_REPLACE_DAT;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasSTUDYREC_PROV_FLG_DAT;
        final boolean _hasSUBCLASS_DETAIL_DAT;             // テーブル SUBCLASS_DETAIL_DATがあるか
        final boolean _hasUSERGROUP_DAT_SCHOOL_KIND;
        final boolean _hasAddr2;
        final boolean _hasFINSCHOOL_DETAIL_MST;
        final Set<String> _logOnce = new TreeSet<String>();

        String _schoolAddress1;
        String _schoolAddress2;
        String _schoolZipcode;
        String _schoolMstSchoolName1;

        final Map _sessionCache = new TreeMap();
        // Map<年度-学期, 学期名>
        protected Map<String, String> _semesterNameMap = new HashMap();
        protected Set<String> _gdatHYearGradeSet = Collections.emptySet();
        protected Map<String, Semester> _semesterMap = Collections.emptyMap();
        protected Map<String, Map<String, SchregRegdGdat>> _gdatMap = Collections.emptyMap();
        protected Map<String, SchregRegdHdat> _hrdatMap = Collections.emptyMap();
        protected Map<String, ClassMst> _classMstMap = Collections.emptyMap();
        protected Map<String, AnotherClassMst> _anotherClassMstMap = Collections.emptyMap();
        protected Map<String, SubclassMst> _subclassMstMap = Collections.emptyMap();
        protected Map<String, AnotherSubclassMst> _anotherSubclassMstMap = Collections.emptyMap();
        protected Map<String, String> _specialDivNameMap = Collections.emptyMap();
        // Map<証明書種別コード, レコード（フィールドと値のマップ）>
        Map<String, Map<String, String>> _certifSchoolDatMap = new HashMap();
        final Map<Integer, String> _d001Abbv1Map;
        final List _d065Name1List;
        final boolean _isPrintPageOrderByYear;

        protected Map<String, String> _gdatGradeName2 = Collections.EMPTY_MAP; // KNJA130
        protected Map<String, String> _gdatGradeCd = Collections.EMPTY_MAP; // KNJA130

        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();

        Param(final HttpServletRequest request, final DB2UDB db2, final String prgId, final String year, final String semester, final Map<String, String> otherParamMap) {
            _cmd = request.getParameter("cmd");
            _isCsv = "csv".equals(_cmd);
            _year = year; // 年度
            _gakki = semester;
            _paramap = getParameterMap(request);
            _otherParamMap = otherParamMap;

            if ("KNJA133M".equals(prgId)) {
                _is133m = true;
            } else if (Arrays.asList("KNJA130", "KNJA130A", "KNJA134H").contains(prgId) || "KNJA130".equals(_otherParamMap.get("printKNJA130"))) {
                _is130 = true;
            }

            if ("KNJA129".equals(prgId)) {
                SCHOOL_KIND = _otherParamMap.get("KNJA129_SCHOOL_KIND");
            } else if ("KNJA134A".equals(prgId)) {
                SCHOOL_KIND = "A";
                _isSenkouka = true;
            } else {
                SCHOOL_KIND = "H";
            }

            _documentroot = getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            if (!StringUtils.isEmpty(_documentroot)) {
                _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
            }

           _definecode.setSchoolCode(db2, _year);
           log.debug("schoolmark = " + _definecode.schoolmark);
           _isDefinecodeSchoolMarkHiro = "HIRO".equals(_definecode.schoolmark);
           _isDefinecodeSchoolMarkK = "K".equals(_definecode.schoolmark.substring(0, 1));
           _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD") ? "1" : null;

           final String z010namespare2 = getNameMstZ010(db2, "NAMESPARE2");
           _isHeisetuKou = "1".equals(z010namespare2);
           _isChuKouIkkan = "1".equals(z010namespare2) || "2".equals(z010namespare2);
           _z010name1 = getNameMstZ010(db2, "NAME1");
           _z010 = Z010.fromName1(_z010name1);

           final String z010name2 = getNameMstZ010(db2, "NAME2");
           log.fatal(" z010 name1 = " + _z010name1 + ", z010 = " + _z010);
           _isHigashiosaka = "higashiosaka".equals(_z010name1);
           _isHigashiosakaKashiwara = _isHigashiosaka && "30270247001".equals(z010name2);
           _isKaichi = Arrays.asList("kmirai", "knihon", "knozomi", "kkotou", "ksogo").contains(_z010name1) || _z010.in(Z010.kikan);
           _isSanonihonHs = _z010.in(Z010.sanonihon) && !_isChuKouIkkan;
           _isSanonihonSs = _z010.in(Z010.sanonihon) && _isChuKouIkkan;

           _hasAFT_GRAD_COURSE_DAT = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
           _hasANOTHER_CLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
           _hasANOTHER_SUBCLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);
           _hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_DAT", "TENGAKU_SAKI_GRADE");
           _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
           _hasSCHREG_STUDYREC_DETAIL_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_STUDYREC_DETAIL_DAT", null);
           _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
           _hasCOURSECODE_MST_COURSECODEABBV1 = KnjDbUtils.setTableColumnCheck(db2, "COURSECODE_MST", "COURSECODEABBV1");
           _hasSCHREG_TRANSFER_DAT_REMARK1 = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_TRANSFER_DAT", "REMARK1");
           _hasSCHREG_TRANSFER_DAT_ABROAD_PRINT_DROP_REGD = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_TRANSFER_DAT", "ABROAD_PRINT_DROP_REGD");
           _hasSCHREG_STUDYREC_REPLACE_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_STUDYREC_REPLACE_DAT", null);
           _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
           _hasSTUDYREC_PROV_FLG_DAT = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", null);
           _hasSUBCLASS_DETAIL_DAT = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_DETAIL_DAT", null);
           _hasUSERGROUP_DAT_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "USERGROUP_DAT", "SCHOOL_KIND");
           _hasAddr2 = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_DAT", "ENT_ADDR2");
           _hasFINSCHOOL_DETAIL_MST = KnjDbUtils.setTableColumnCheck(db2, "FINSCHOOL_DETAIL_MST", null);

           if (_is130) {
               final KNJ_Control.ReturnVal value = new KNJ_Control().Control(db2);
               _imageDir = value.val4;
               _imageExt = value.val5;
           } else {
               _imageDir = "image/stamp";
               _imageExt = "bmp";
           }
           _dbPrgInfoProperties = getDbPrginfoProperties(db2, prgId);
           final String[] outputDebugArray = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
           if (null != outputDebugArray) {
               log.info(" outputDebug = " + ArrayUtils.toString(outputDebugArray));
           }
           _isOutputDebugAll = ArrayUtils.contains(outputDebugArray, "all");
           _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "1");
           _isOutputDebugData = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "data");
           _isOutputDebugGakuseki = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "gakuseki");
           _isOutputDebugSeiseki = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "seiseki");
           _isOutputDebugKinsoku = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "kinsoku");
           _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "query");
           _isOutputDebugSvfOut = ArrayUtils.contains(outputDebugArray, "svfout");
           _isOutputDebugFormCreate = ArrayUtils.contains(outputDebugArray, "SvfFormCreate");
           _isOutputDebugFormRecordInfo = ArrayUtils.contains(outputDebugArray, "shokenRecordInfo");
           _isOutputDebugInner = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "inner");
           _isOutputDebugStaff = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "staff");
           if (null != outputDebugArray) {
               if (_isOutputDebugAll) {
                   _outputDebugFieldList = new ArrayList<String>();
               } else {
                   for (int i = 0; i < outputDebugArray.length; i++) {
                       if (null != outputDebugArray[i]) {
                           if (outputDebugArray[i].startsWith("field")) {
                               _outputDebugFieldList = new ArrayList<String>();
                               if (outputDebugArray[i].startsWith("field=")) {
                                   final String[] split = StringUtils.split(outputDebugArray[i].substring("field=".length()), ",");
                                   for (int j = 0; j < split.length; j++) {
                                       _outputDebugFieldList.add(split[j]);
                                   }
                                   log.info(" outputDebugFieldList = " + _outputDebugFieldList);
                               }
                           }
                       }
                   }
               }
           }
           if (null != outputDebugArray) {
               log.info(" isOutputDebugAll = " + _isOutputDebugAll);
               log.info(" isOutputDebug = " + _isOutputDebug);
               log.info(" isOutputDebugData = " + _isOutputDebugData);
               log.info(" isOutputDebugGakuseki = " + _isOutputDebugGakuseki);
               log.info(" isOutputDebugSeiseki = " + _isOutputDebugSeiseki);
               log.info(" isOutputDebugKinsoku = " + _isOutputDebugKinsoku);
               log.info(" isOutputDebugQuery = " + _isOutputDebugQuery);
               log.info(" isOutputDebugSvfOut = " + _isOutputDebugSvfOut);
               log.info(" isOutputDebugFormCreate = " + _isOutputDebugFormCreate);
               log.info(" isOutputDebugInner = " + _isOutputDebugInner);
               log.info(" isOutputDebugStaff = " + _isOutputDebugStaff);
           }
           if (_isOutputDebug) {
               log.info(" isCsv = " + _isCsv);
           }

           _isSeireki = KNJ_EditDate.isSeireki(db2);
           _hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組

           _ibCourse = request.getParameter("ib_course");
           _notUseClassMstSpecialDiv = _z010.in(Z010.meikei) && "1".equals(_ibCourse);

           _seitoSidoYorokuSougouFieldSize = "1".equals(property(Property.seitoSidoYorokuSougouFieldSize));
           _seitoSidoYorokuSpecialactremarkFieldSize = "1".equals(property(Property.seitoSidoYorokuSpecialactremarkFieldSize));
           _isSubclassOrderNotContainCurriculumcd = _z010.in(Z010.kyoto);
           _replacePrintGradeCdWithGrade = _z010.in(Z010.chiyodaKudan);
           _useEditKinsoku = (!_z010.in(Z010.KINDAI, Z010.chukyo, Z010.kyoto, Z010.nishiyama, Z010.kaijyo, Z010.sapporo)) || _z010.in(Z010.tokiwa, Z010.miyagiken, Z010.mieken, Z010.musashinohigashi, Z010.sundaikoufu, Z010.rakunan, Z010.kyoai, Z010.tosa, Z010.chiyodaKudan) || isTokubetsuShien() || "1".equals(property(Property.seitoSidoYorokuUseEditKinsokuH));
           _seitoSidoYorokuKinsokuForm = property(Property.seitoSidoYorokuKinsokuForm);
           _seitoSidoYorokuZaisekiMae = _z010.in(Z010.mieken) || "1".equals(property(Property.seitoSidoYorokuZaisekiMae));

           _seitoSidoYorokuHozonkikan = Util.toInt(property(Property.seitoSidoYorokuHozonkikan), 5);
           final String[] seitoSidoYorokuNotUseSubclassSubstitution = StringUtils.split(property(Property.seitoSidoYorokuNotUseSubclassSubstitution), ",");
           if (null != seitoSidoYorokuNotUseSubclassSubstitution) {
               _seitoSidoYorokuNotUseSubclassSubstitution = seitoSidoYorokuNotUseSubclassSubstitution;
               for (int i = 0; i < _seitoSidoYorokuNotUseSubclassSubstitution.length; i++) {
                   _seitoSidoYorokuNotUseSubclassSubstitution[i] = StringUtils.trim(_seitoSidoYorokuNotUseSubclassSubstitution[i]);
               }
               if (_isOutputDebug) {
                   log.info(" seitoSidoYorokuNotUseSubclassSubstitution = " + ArrayUtils.toString(_seitoSidoYorokuNotUseSubclassSubstitution));
               }
           } else {
               _seitoSidoYorokuNotUseSubclassSubstitution = new String[] {};
           }
           _seitoSidoYorokuNotPrintCoursecodes = csvToList(property(Property.seitoSidoYorokuNotPrintCoursecodes));

           _isAddressOutput2 = _z010.in(Z010.tosa);

           _formatDateDefaultYear = _year;

           log.debug("中高一貫か? = " + _isChuKouIkkan);
           log.debug("併設校か? = " + _isHeisetuKou);

           _certifSchoolDatMap.put(CERTIF_KINDCD, KnjDbUtils.firstRow(KnjDbUtils.query(db2,  "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "' ")));

           if (_z010.in(Z010.mieken)) {
               _substRemarkNot90Template = new StringTemplate("「{代替元科目}」{代替単位}単位で{代替名2}");
           } else if (_z010.in(Z010.naraken)) {
               _substRemarkNot90Template = new StringTemplate("{代替名}　{代替元科目}{代替単位}");
           } else {
               _substRemarkNot90Template = new StringTemplate("{代替名}{代替元科目}{代替単位}");
           }
           _substRemark90Template = new StringTemplate("{代替先科目}は{代替元科目}で{代替名}");

           _staffGroupcd = getUserGroupDatGroupcd(db2, _year, request.getParameter("PRINT_LOG_STAFFCD"));

           _specialDivNameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A029' "), "NAMECD2", "NAME1");
           _d001Abbv1Map = getD001Map(db2);
           _d065Name1List = getD065List(db2);
           _isPrintPageOrderByYear = _z010.in(Z010.KINDAI);
           setFuninteiRemarkFormat(db2);
           setGdatHYearGradeSet(db2, _year);
        }

        private Map<String, List<String>> getParameterMap(final HttpServletRequest request) {
            final Map<String, List<String>> paramMap = new TreeMap<String, List<String>>();
            for (final Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
                final String name = e.getKey();
                final String[] vals = e.getValue();
                if (null == vals || vals.length == 0) {
                    paramMap.put(name, null);
                } else {
                    paramMap.put(name, Arrays.asList(vals));
                }
            }
            return paramMap;
        }

        protected String getParameter(final String name) {
            if (!_paramap.containsKey(name)) {
                if (_isOutputDebug) {
                    logOnce(" no param : " + name);
                }
                return null;
            }
            if (null == _paramap.get(name)) {
                if (_isOutputDebug) {
                    logOnce(" parameter " + name + " = null");
                }
                return null;
            }
            final List<String> values = _paramap.get(name);
            if (_isOutputDebug) {
                logOnce(" parameter " + name + " = " + values);
            }
            final String value = values.get(0);
            return value;
        }

        private static List<String> csvToList(final String data) {
            final List<String> rtn = new ArrayList<String>();
            if (StringUtils.isEmpty(data)) {
                return rtn;
            }
            for (final String v : data.split(",")) {
                rtn.add(v.trim());
            }
            return rtn;
        }

        protected Properties loadPropertyFile(final String filename) {
            File file = null;
            if (null != _documentroot) {
                file = new File(new File(_documentroot).getParentFile().getAbsolutePath() + "/config/" + filename);
                if (_isOutputDebug) {
                    log.info("check prop : " + file.getAbsolutePath() + ", exists? " + file.exists());
                }
                if (!file.exists()) {
                    file = null;
                }
            }
            if (null == file) {
                file = new File(_documentroot + "/" + filename);
            }
            if (!file.exists()) {
                if (_isOutputDebug) {
                    log.error("file not exists: " + file.getAbsolutePath());
                }
                return null;
            }
            if (_isOutputDebug) {
                log.error("file : " + file.getAbsolutePath() + ", " + file.length());
            }
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
            return props;
        }

        protected String property(final Property p) {
            return property(p.name());
        }

        protected String property(final String name) {
            if (_paramap.containsKey(name)) {
                return getParameter(name);
            }
            String val = null;
            if (null != _dbPrgInfoProperties) {
                if (_dbPrgInfoProperties.containsKey(name)) {
                    val = _dbPrgInfoProperties.get(name);
                    if (_isOutputDebug) {
                        logOnce("property in db: " + name + " = " + val);
                    }
                    return val;
                }
            }
            if (null != _prgInfoPropertiesFilePrperties) {
                if (_prgInfoPropertiesFilePrperties.containsKey(name)) {
                    val = _prgInfoPropertiesFilePrperties.getProperty(name);
                    if (_isOutputDebug) {
                        logOnce("property in file: " + name + " = " + val);
                    }
                } else {
                    if (_isOutputDebug) {
                        logWarnOnce("property not exists in file: " + name);
                    }
                }
            }
            return val;
        }

        private Map<Integer, String> getD001Map(final DB2UDB db2) {
            final Map<Integer, String> map = new HashMap();
            try {
                for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' ORDER BY NAMECD2 ")) {
                    if (NumberUtils.isDigits(KnjDbUtils.getString(row, "NAMECD2"))) {
                        map.put(Integer.valueOf(KnjDbUtils.getString(row, "NAMECD2")), KnjDbUtils.getString(row, "ABBV1"));
                    }
                }
            } catch (Exception e) {
                log.error("SQLException", e);
            }
            return map;
        }

        private List<String> getD065List(final DB2UDB db2) {
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL ORDER BY NAMECD2 "), "NAME1");
        }

        protected boolean isTokubetsuShien() {
            return "TOKUBETSU_SHIEN".equals(_otherParamMap.get("OUTPUT_KIND")); // KNJA134Hからコールされた場合
        }

        protected boolean useSpecialDiv() {
            return isTokubetsuShien() || _z010.in(Z010.tokyoto);
        }

        private void setFuninteiRemarkFormat(final DB2UDB db2) {
            _mirishuRemarkFormat = null;
            _rishunomiRemarkFormat = null;
            if (ArrayUtils.contains(Util.csvToArray(property(Property.seitoSidoYorokuNotUseCompCredit)), "2omote")) {
                // 設定しない
                return;
            }
            final String sql = "SELECT NAMESPARE1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'A030' AND NAMECD2 = '00' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                _mirishuRemarkFormat = KnjDbUtils.getString(row, "NAMESPARE1");
                _rishunomiRemarkFormat = KnjDbUtils.getString(row, "NAMESPARE2");
            }
            if (!_z010.in(Z010.mieken)) {
                if (null == _mirishuRemarkFormat) {
                    _mirishuRemarkFormat = "x履修不認定";
                }
                if (null == _rishunomiRemarkFormat) {
                    if (_z010.in(Z010.kyoto)) {
                        _rishunomiRemarkFormat = "y単位履修認定";
                    } else {
                        _rishunomiRemarkFormat = "x履修単位数(y)";
                    }
                }
            }
        }

        /**
         * 普通・専門の文言
         * @param div 普通・専門区分　0:普通、1:専門
         * @return 文言
         */
        public String getSpecialDivName(final boolean isNewForm, String div) {
            if (null == div) {
                div = "0";
            }
            final String defaultname;
            if ("0".equals(div)) {
                // 普通教育
                defaultname = isNewForm ? "各学科に共通する各教科・科目" : "普通教育に関する各教科・科目";
            } else if ("1".equals(div)) {
                //　専門教科
                defaultname = isNewForm ? "主として専門学科において開設される各教科・科目" : "専門教育に関する各教科・科目";
            } else if ("2".equals(div)) {
                // その他
                defaultname = "その他特に必要な教科・科目";
            } else {
                defaultname = "";
            }
            final String key;
            if (NumberUtils.isDigits(div)) {
                key = String.valueOf(Integer.parseInt(div) + 1);
            } else {
                key = "";
            }
            return defstr(_specialDivNameMap.get(key), defaultname);
        }

        /**
         * 中高一貫か?
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        protected static String getNameMstZ010(final DB2UDB db2, final String fieldname) {
            return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT " + fieldname + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'")), fieldname);
        }

        protected static Map<String, String> getDbPrginfoProperties(final DB2UDB db2, final String prgid) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = '" + prgid + "' "), "NAME", "VALUE");
        }

        protected boolean isOutputDebugField(final String field) {
            if (null == _outputDebugFieldList) {
                return false;
            }
            if (null == field || _outputDebugFieldList.isEmpty()) {
                return true;
            }
            for (final String debugField : _outputDebugFieldList) {
                if (field.indexOf(debugField) >= 0) {
                    return true;
                }
            }
            return false;
        }

        public void logOnce(final String l) {
            if (_logOnce.contains(l)) {
                return;
            }
            log.info(l);
            _logOnce.add(l);
        }

        public void logWarnOnce(final String l) {
            if (_logOnce.contains(l)) {
                return;
            }
            log.warn(l);
            _logOnce.add(l);
        }

        protected void setSemester(final DB2UDB db2) {
            _semesterMap = Semester.load(db2);
        }

        private void setGdatHYearGradeSet(final DB2UDB db2, final String _year) {
            _gdatHYearGradeSet = new HashSet<String>();
            _gdatMap = new HashMap<String, Map<String, SchregRegdGdat>>();
            _gdatGradeName2 = new HashMap();
            _gdatGradeCd = new HashMap();
            final String sql = " SELECT YEAR, GRADE, GRADE_NAME2, GRADE_CD FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND = '" + SCHOOL_KIND + "' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final SchregRegdGdat gdat = SchregRegdGdat.create();
                gdat._year = KnjDbUtils.getString(row, "YEAR");
                gdat._grade = KnjDbUtils.getString(row, "GRADE");
                gdat._gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                gdat._gradeName2 = KnjDbUtils.getString(row, "GRADE_NAME2");
                gdat._gakunenSimple = toDigit(_replacePrintGradeCdWithGrade ? gdat._grade : gdat._gradeCd, " ");

                Util.getMappedHashMap(_gdatMap, gdat._year).put(gdat._grade, gdat);

                final String yg = gdat._year + ":" + gdat._grade;
                _gdatHYearGradeSet.add(yg);
                _gdatGradeName2.put(yg, KnjDbUtils.getString(row, "GRADE_NAME2"));
                _gdatGradeCd.put(yg, KnjDbUtils.getString(row, "GRADE_CD"));
            }
            final Collection<String> years = new ArrayList<String>(new TreeSet<String>(_gdatMap.keySet()));
            String defaultYear = null;
            for (final String year : years) {
                if (Integer.parseInt(year) > Integer.parseInt(_year)) {
                    break;
                }
                defaultYear = year;
            }
            if (null != defaultYear) {
                _gdatMap.put("-", _gdatMap.get(defaultYear));
            }
            if (_seitoSidoYorokuZaisekiMae) {
                _gdatHYearGradeSet.add("0:00");
            }
        }

        public boolean isGdatH(final String year, final String grade) {
            if (null == year || null == grade) {
                return false;
            }
            if (_is130) {
                if ("1".equals(property(Property.seitoSidoYorokuZaisekiMae)) && Util.isNyugakumae(year) && "00".equals(grade)) {
                    return true;
                }
            }
            return _gdatHYearGradeSet.contains(year + ":" + grade);
        }

        protected String getGdatGradeName2(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : _gdatGradeName2.get(year + ":" + grade);
            return rtn;
        }

        protected String getGdatGradeCd(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : _gdatGradeCd.get(year + ":" + grade);
            return rtn;
        }

        protected void setHdat(final DB2UDB db2) {
            _hrdatMap = new HashMap<String, SchregRegdHdat>();
            final String sql = " SELECT * FROM SCHREG_REGD_HDAT ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final SchregRegdHdat hrdat = SchregRegdHdat.create();
                hrdat._year = KnjDbUtils.getString(row, "YEAR");
                hrdat._semester = KnjDbUtils.getString(row, "SEMESTER");
                hrdat._grade = KnjDbUtils.getString(row, "GRADE");
                hrdat._hrClass = KnjDbUtils.getString(row, "HR_CLASS");

                String hrname = null;
                if ("1".equals(property(Property.useSchregRegdHdat))) {
                    hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                } else if ("0".equals(property(Property.useSchregRegdHdat))) {
                    hrname = KNJ_EditEdit.Ret_Num_Str(hrdat._hrClass, _hmap);
                }

                if (null == hrname) {
                    hrname = KNJ_EditEdit.Ret_Num_Str(hrdat._hrClass);
                }
                hrdat._hrname = hrname;

                final String key = SchregRegdHdat.key(hrdat._year, hrdat._semester, hrdat._grade, hrdat._hrClass);
                _hrdatMap.put(key, hrdat);
            }
        }

        protected void setClassMst(final DB2UDB db2) {
            _classMstMap = new HashMap<String, ClassMst>();
            final Integer showOrderDefault = new Integer(-1);
            final String sql = " SELECT * FROM CLASS_MST ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                String classcd = KnjDbUtils.getString(row, "CLASSCD");
                String schoolKind = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "SCHOOL_KIND") : "";
                String classname = KnjDbUtils.getString(row, "CLASSNAME");
                String electdiv = KnjDbUtils.getString(row, "ELECTDIV");

                String specialDiv;
                if (_is133m) {
                    specialDiv = _z010.in(Z010.tokyoto) ? "0" : StringUtils.isEmpty(KnjDbUtils.getString(row, "SPECIALDIV")) ? "0" : defstr(KnjDbUtils.getString(row, "SPECIALDIV"), "0");
                } else {
                    specialDiv = StringUtils.isEmpty(KnjDbUtils.getString(row, "SPECIALDIV")) ? "0" : defstr(KnjDbUtils.getString(row, "SPECIALDIV"), "0");
                }
                Integer showorder = KnjDbUtils.getInt(row, "SHOWORDER", null);
                if (null == showorder) {
                    showorder = showOrderDefault;
                }
                final ClassMst cm = new ClassMst(classcd, schoolKind, classname, electdiv, specialDiv, showorder);

                _classMstMap.put(cm.getKey(this), cm);
            }
            if (_hasANOTHER_CLASS_MST) {
                _anotherClassMstMap = new HashMap<String, AnotherClassMst>();
                final String sql2 = " SELECT * FROM ANOTHER_CLASS_MST ";
                for (final Map row : KnjDbUtils.query(db2, sql2)) {
                    String classcd = KnjDbUtils.getString(row, "CLASSCD");
                    String schoolKind = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "SCHOOL_KIND") : "";
                    String classname = KnjDbUtils.getString(row, "CLASSNAME");
                    String electdiv = KnjDbUtils.getString(row, "ELECTDIV");

                    String specialDiv;
                    if (_is133m) {
                        specialDiv = _z010.in(Z010.tokyoto) ? "0" : defstr(KnjDbUtils.getString(row, "SPECIALDIV"), "0");
                    } else {
                        specialDiv = defstr(KnjDbUtils.getString(row, "SPECIALDIV"), "0");
                    }
                    Integer showorder = KnjDbUtils.getInt(row, "SHOWORDER", null);
                    if (null == showorder) {
                        showorder = showOrderDefault;
                    }
                    final AnotherClassMst cm = new AnotherClassMst(classcd, schoolKind, classname, electdiv, specialDiv, showorder);

                    _anotherClassMstMap.put(cm.getKey(this), cm);
                }
            }
        }

        protected void setSubclassMst(final DB2UDB db2) {
            _subclassMstMap = new HashMap<String, SubclassMst>();
            final Integer showOrderDefault = new Integer(-1);
            final String sql = " SELECT * FROM SUBCLASS_MST ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String classcd = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "CLASSCD") : KnjDbUtils.getString(row, "SUBCLASSCD").substring(0, 2);
                final String schoolKind = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "SCHOOL_KIND") : "";
                final String curriculumCd = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "CURRICULUM_CD") : "";
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String subclasscd2 = KnjDbUtils.getString(row, "SUBCLASSCD2");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String subclassordername1 = KnjDbUtils.getString(row, "SUBCLASSORDERNAME1");
                Integer showorder = KnjDbUtils.getInt(row, "SHOWORDER", null);
                if (null == showorder) {
                    showorder = showOrderDefault;
                }
                final SubclassMst subclassMst = new SubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassordername1, showorder, subclasscd2);

                final String key = SubclassMst.key(this, subclassMst._classcd, subclassMst._schoolKind, subclassMst._curriculumCd, subclassMst._subclasscd);
                _subclassMstMap.put(key, subclassMst);
            }

            if (_hasSUBCLASS_DETAIL_DAT) {
                final String sqlZenkiKamoku = " SELECT * FROM SUBCLASS_DETAIL_DAT WHERE SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1' ";
                for (final Map row : KnjDbUtils.query(db2, sqlZenkiKamoku)) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String classcd = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "CLASSCD") : KnjDbUtils.getString(row, "SUBCLASSCD").substring(0, 2);
                    final String schoolKind = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "SCHOOL_KIND") : "";
                    final String curriculumCd = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "CURRICULUM_CD") : "";
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                    final String key = SubclassMst.key(this, classcd, schoolKind, curriculumCd, subclasscd);
                    final SubclassMst subclassMst = _subclassMstMap.get(key);
                    if (null == subclassMst) {
                        continue;
                    }
                    if (_isOutputDebugData) {
                        log.info(" year = " + year + " zenkikamoku : " + subclassMst);
                    }
                    subclassMst._isYearZenkiKamoku.put(year, "1");
                }
            }

            if (_hasANOTHER_SUBCLASS_MST) {
                _anotherSubclassMstMap = new HashMap<String, AnotherSubclassMst>();
                final String sql2 = " SELECT * FROM ANOTHER_SUBCLASS_MST ";
                for (final Map row : KnjDbUtils.query(db2, sql2)) {
                    final String classcd = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "CLASSCD") : KnjDbUtils.getString(row, "SUBCLASSCD").substring(0, 2);
                    final String schoolKind = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "SCHOOL_KIND") : "";
                    final String curriculumCd = "1".equals(_useCurriculumcd) ? KnjDbUtils.getString(row, "CURRICULUM_CD") : "";
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                    final String subclassordername1 = KnjDbUtils.getString(row, "SUBCLASSORDERNAME1");
                    Integer showorder = KnjDbUtils.getInt(row, "SHOWORDER", null);
                    if (null == showorder) {
                        showorder = showOrderDefault;
                    }
                    final AnotherSubclassMst anotherSubclassMst = new AnotherSubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassordername1, showorder);

                    final String key = SubclassMst.key(this, anotherSubclassMst._classcd, anotherSubclassMst._schoolKind, anotherSubclassMst._curriculumCd, anotherSubclassMst._subclasscd);
                    _anotherSubclassMstMap.put(key, anotherSubclassMst);
                }
            }
        }

        protected static List<String> getSchregnoList(final DB2UDB db2, final String flg, final HttpServletRequest request) {
            String year;
            String semester;
            String[] categorySelected;
            final String output;
            if ("KNJA133M".equals(flg)) {
                year = request.getParameter("YEAR");
                semester = request.getParameter("SEMESTER");
                output = "1";
                categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 複数生徒 or 複数年組

            } else {
                year = request.getParameter("YEAR");
                semester = request.getParameter("GAKKI");
                output = request.getParameter("OUTPUT");    // 1=個人, 2=クラス
                final String cmd = request.getParameter("cmd");
                if ("csv".equals(cmd)) {
                    categorySelected = StringUtils.split(request.getParameter("category_selected"), ","); // 複数生徒 or 複数年組
                } else {
                    categorySelected = request.getParameterValues("category_selected"); // 複数生徒 or 複数年組
                }
            }

            final List<String> schregnos = new ArrayList<String>(); // 出力対象学籍番号を格納

            if ("2".equals(output)) {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT SCHREGNO FROM SCHREG_REGD_DAT ");
                stb.append("WHERE YEAR = '" + year + "' ");
                stb.append("AND SEMESTER = '" + semester + "' ");
                stb.append("AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, categorySelected) + " ");
                stb.append("ORDER BY GRADE, HR_CLASS, ATTENDNO ");

                schregnos.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO"));
            } else {
                schregnos.addAll(Arrays.asList(categorySelected));
            }
            return schregnos;
        }

        protected String getCareerPlanItemName(final DB2UDB db2, final String year) {
            return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'A041' AND NAMECD2 = '01' ")), "NAME1");
        }

        protected String getSemesterName(final String year, final String semester) {
            return _semesterNameMap.get(year + "-" + semester);
        }

        protected Map<String, String> getSemesterNameMap(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT YEAR || '-' || SEMESTER AS KEY, SEMESTERNAME FROM SEMESTER_MST WHERE SEMESTER <> '9' "), "KEY", "SEMESTERNAME");
        }

        private String getUserGroupDatGroupcd(final DB2UDB db2, final String year, final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(GROUPCD) AS GROUPCD ");
            stb.append(" FROM ");
            stb.append("     USERGROUP_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            if (_hasUSERGROUP_DAT_SCHOOL_KIND) {
                stb.append("     AND T1.SCHOOL_KIND = '" + SCHOOL_KIND + "' ");
            }
            stb.append("     AND T1.STAFFCD = '" + staffcd + "' ");

            String groupcd = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "GROUPCD");
            log.info(" groupcd = " + groupcd);
            return groupcd;
        }

        /**
         * 写真データファイルの取得
         */
        String getImageFilePath(final String filename) {
            return getImageFilePath(_imageDir, filename, _imageExt);
        }

        String getImageFilePath(final String imageDir, final String filename, final String ext) {
            if (null == _documentroot || null == imageDir || null == ext || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(imageDir).append("/").append(filename).append(".").append(ext);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

        public Map getYearSubclassCreditMap() {
            return Util.getMappedMap(_sessionCache, "creditMst");
        }

        protected Map formFieldInfoMap(final String form, final String fieldInfoKey) {
            final String formFieldKey = form + "." + fieldInfoKey;
            return Util.getMappedHashMap(Util.getMappedHashMap(_sessionCache, "FORM_FIELD_INFO"), formFieldKey);
        }

        protected Map debugOutputMap() {
            return Util.getMappedHashMap(_sessionCache, "debugOutputMap");
        }

        protected Map debugCharMap() {
            return Util.getMappedHashMap(_sessionCache, "debugChar");
        }

        public void setPs(final String psKey, final DB2UDB db2, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        public void psCloseQuietly() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
        }
    }
}
