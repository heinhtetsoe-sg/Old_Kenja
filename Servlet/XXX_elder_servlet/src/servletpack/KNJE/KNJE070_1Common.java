package servletpack.KNJE;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;
import static servletpack.KNJZ.detail.KnjDbUtils.getString;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE070_1.SqlStudyrec;
import servletpack.KNJG.KNJG010_1;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;

public class KNJE070_1Common {

    private static final Log log = LogFactory.getLog(KNJE070_1Common.class);

    protected static final String ps1Key = "ps1";

    protected static final String newLine = "\n";
    protected static final String _88 = "88";
    protected static final String _90 = "90";
    protected static final String _94 = "94";
    protected static final String _95 = "95";
    protected static final String _ABROAD = "ABROAD";

    public KNJE070_1Common() {
        log.fatal("$Id: 9594efdcc48082958c920d82586a58a21e0f0f27 $");
    }

    protected static String defstr(final Object str1) {
        return defstr(str1, "");
    }

    protected static String defstr(final Object str1, final String ... str2) {
        return null == str1 ? defval(null, str2) : str1.toString();
    }

    protected static <T> T defval(final T v, final T ... vs) {
        if (null != v) {
            return v;
        }
        if (null != vs) {
            for (final T s : vs) {
                if (null != s) {
                    return s;
                }
            }
        }
        return null;
    }

    protected static <T> T defval(final Collection<T> vs) {
        if (null != vs) {
            for (final T s : vs) {
                if (null != s) {
                    return s;
                }
            }
        }
        return null;
    }

    protected static String blankDefstr(final String str1, final String str2) {
        return StringUtils.isBlank(str1) ? str2 : str1;
    }

    protected static int getTextKeta(final String str) {
        try {
            return KNJ_EditEdit.getTextKeta(str);
        } catch (Throwable t) {
            log.warn("exception!" + t.getMessage());
        }
        return getMS932ByteLength(str);
    }

    /**
     * 変更できないリスト
     * @param <T>
     */
    protected static class ImmutableList<T> implements Iterable<T> {
        private final List<T> _source;
        private final int _size;
        public static <T> ImmutableList<T> of(final List<T> source) {
            return new ImmutableList(source);
        }
        private ImmutableList(final List<T> source) {
            _source = new ArrayList<T>(source);
            _size = _source.size();
        }
        public T get(final int idx) {
            return _source.get(idx);
        }
        public int size() {
            return _size;
        }
        public boolean isEmpty() {
            return _size == 0;
        }
        public List<T> subList(final int fromIndex, final int toIndex) {
            return _source.subList(fromIndex, toIndex);
        }
        public List<T> getSourceCopy() {
            return new ArrayList<T>(_source);
        }
        @Override
        public Iterator<T> iterator() {
            return new It();
        }

        private class It implements Iterator<T> {
            private int _idx = 0;

            @Override
            public boolean hasNext() {
                return _idx < _size;
            }

            @Override
            public T next() {
                return _source.get(_idx++);
            }

            @Override
            public void remove() {
                throw new java.lang.IllegalStateException("immutables is not modifirable.");
            }
        }
    }

    protected static class Util {

        public static String decodeISO8859_1(final String s) {
            if (null != s) {
                try {
                    return new String(s.getBytes("ISO8859-1"));
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return null;
        }

        public static String hitoketaHaZenkaku(final String str) {
            if (StringUtils.isBlank(str) || !NumberUtils.isDigits(str) || str.length() > 1) {
                return str;
            }
            return String.valueOf((char) (str.charAt(0) - '0' + '０'));
        }

        private static List<Integer> asInt(final Collection<String> list) {
            if (null == list) {
                return null;
            } else if (list.size() == 0) {
                return new ArrayList();
            }
            final List<Integer> rtn = new ArrayList<Integer>();
            for (final String s : list) {
                if (NumberUtils.isDigits(s)) {
                    rtn.add(Integer.valueOf(s));
                } else {
                    if (null != s) {
                        log.warn("failed: convertint integer: " + s);
                    }
                    rtn.add(null);
                }
            }
            return rtn;
        }

        private static <E> int notNullCount(final List<E> list) {
            if (null == list) {
                return 0;
            }
            int n = 0;
            for (final E e : list) {
                if (null == e) {
                    continue;
                }
                n += 1;
            }
            return n;
        }

        private static BigDecimal bigDecimalAvg(final List<BigDecimal> list, int keta, int roundingMode) {
            if (null == list || list.size() == 0) {
                return null;
            }
            final int n = notNullCount(list);
            return n == 0 ? null : divide(bigDecimalSum(list), new BigDecimal(n), keta, roundingMode);
        }

        private static BigDecimal divide(final BigDecimal bunsi, final BigDecimal bunbo, final int keta, final int roundingMode) {
            if (null == bunsi || null == bunbo || bunbo.intValue() == 0) {
                return null;
            }
            return bunsi.divide(bunbo, keta, roundingMode);
        }

        public static BigDecimal bigDecimalSum(final List<BigDecimal> bigDecimalList) {
            BigDecimal bd = null;
            for (final BigDecimal v : bigDecimalList) {
                if (null == v) {
                    continue;
                }
                if (null == bd) {
                    bd = new BigDecimal(0);
                }
                bd = bd.add(v);
            }
            return bd;
        }

        public static Integer integerSum(final Collection<Integer> integerList) {
            Integer sum = null;
            for (final Integer v : integerList) {
                if (null == v) {
                    continue;
                }
                if (null == sum) {
                    sum = new Integer(0);
                }
                sum = new Integer(sum.intValue() + v.intValue());
            }
            return sum;
        }

        public static List<BigDecimal> toBigDecimalList(final Collection<String> numberStringList) {
            final List<BigDecimal> list = new ArrayList<BigDecimal>();
            for (final String s : numberStringList) {
                if (NumberUtils.isNumber(s)) {
                    list.add(new BigDecimal(s));
                } else {
                    list.add(null);
                }
            }
            return list;
        }

        public static List<Integer> toIntegerList(final Collection<String> integerStringList) {
            final List<Integer> list = new ArrayList<Integer>();
            for (final String s : integerStringList) {
                if (NumberUtils.isDigits(s)) {
                    list.add(Integer.valueOf(s));
                } else {
                    list.add(null);
                }
            }
            return list;
        }

        public static int toInt(final String s, final int def) {
            return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
        }

        public static double toDouble(final String s, final double def) {
            return NumberUtils.isNumber(s) ? Double.parseDouble(s) : def;
        }

        public static Calendar toCalendar(final String date) {
            final Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(Date.valueOf(date));
            } catch (Exception e) {
                log.error("exception! " + date, e);
            }
            return cal;
        }

        public static String date0401(final String date) {
            final Calendar cal = toCalendar(date);
            final int nendo;
            if (cal.get(Calendar.MONTH) < Calendar.APRIL) {
                nendo = cal.get(Calendar.YEAR) - 1;
            } else {
                nendo = cal.get(Calendar.YEAR);
            }
            return String.valueOf(nendo) + "-04-01";
        }

        public static boolean isNextDate(final String date1, final String date2) {
            final Calendar cal1 = toCalendar(date1);
            final Calendar cal2 = toCalendar(date2);
            cal1.add(Calendar.DATE, 1);
            return cal1.equals(cal2);
        }

        public static Integer enterYear(final String date) {
            if (null != date) {
                final Calendar cal = toCalendar(date);
                if (cal.get(Calendar.MONTH) < Calendar.APRIL) {
                    return new Integer(cal.get(Calendar.YEAR) - 1);
                } else {
                    return new Integer(cal.get(Calendar.YEAR));
                }
            }
            return null;
        }

        public static String h_format_Seireki(final String date) {
            String rtn = null;
            try {
                final SimpleDateFormat sdfy = new SimpleDateFormat("yyyy年M月d日");
                rtn = sdfy.format(Date.valueOf(date.replace('/', '-')));
            } catch (Exception e) {
                log.error("Exception! date = " + date, e);
                rtn = "";
            }
            return rtn;
        }

        /*----------------------------------------------------------------------------------------------*
         * 日付の編集
         * ※使い方
         *   String dat = h_format_JP_M("2002-04-27", true)        :"平成14年 4月"
         *   String dat = h_format_JP_M("2002-04-27", false)       :"平成14年4月"
         *   String dat = h_format_JP_M("2002-10-27", false)       :"平成14年10月"
         *----------------------------------------------------------------------------------------------*/
        public static String h_format_M(final DB2UDB db2, final Param param, final String date, final boolean isM2) {
            if (null == date) {
                return null;
            }
            final String entNendoStr;
            if (param._isSeireki) {
                entNendoStr = new SimpleDateFormat("yyyy年").format(Date.valueOf(date));
            } else {
                entNendoStr = KNJ_EditDate.h_format_JP_N(db2, date);
            }
            final String entNendoMonthStr = KNJ_EditDate.h_format_JP_M(db2, date);
            final String entMonthStr = entNendoMonthStr.indexOf("年") != -1 ? entNendoMonthStr.substring(entNendoMonthStr.indexOf("年") + 1) : "";
            final String rtn;
            if (entMonthStr.indexOf("月") <= 1 && isM2) { // 月が1桁ならスペースを挿入する。
                rtn = entNendoStr + " " + entMonthStr;
            } else {
                rtn = entNendoStr + entMonthStr;
            }
            return rtn;
        }

        public static <K, T, U> Map<T, U> getMappedHashMap(final Map<K, Map<T, U>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap<T, U>());
            }
            return map.get(key1);
        }

        public static <K, T, U> Map<T, U> getMappedMap(final Map<K, Map<T, U>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap<T, U>());
            }
            return map.get(key1);
        }

        public static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<T>());
            }
            return map.get(key1);
        }

        public static String mkString(final Collection<String> list, final String comma) {
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

        public static String mkString(final TreeMap<String, String> map, final String comma) {
            final List<String> list = new ArrayList<String>();
            for (final Map.Entry<String, String> e : map.entrySet()) {
                if (StringUtils.isEmpty(e.getKey()) || StringUtils.isEmpty(e.getValue())) {
                    continue;
                }
                list.add(e.getKey() + "=" + e.getValue());
            }
            return mkString(list, comma);
        }

        public static <T> int getNotNullCount(final List<T> arr) {
            if (null == arr) {
                return 0;
            }
            int count = 0;
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i) != null) {
                    count += 1;
                }
            }
            return count;
        }

        public static List<String> getTokenList(final String source0, final ShokenSize size, final Param param) {
            List<String> tokenList = Util.getTokenList(source0, size.keta(), param);
            if (tokenList.size() > size._gyo) {
               tokenList = tokenList.subList(0, size._gyo);
            }
            return tokenList;
        }
        /**
         * @param bytePerLine 1行あたりのバイト数
         * @param source 元文字列
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        public static List<String> getTokenList(final String source0, final int bytePerLine, final Param param) {
            return getTokenList(source0, bytePerLine, param._useEditKinsoku, param.kinsokuConfigMap());
        }

        /**
         * @param bytePerLine 1行あたりのバイト数
         * @param source 元文字列
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        public static List<String> getTokenList(final String source0, final int bytePerLine, final boolean useEditKinsoku, final Map kinsokuConfigMap) {
            if (useEditKinsoku) {
                return Util.KNJ_EditKinsokuGetTokenList(source0, bytePerLine, kinsokuConfigMap);
            }

            if (source0 == null || source0.length() == 0) {
                return Collections.emptyList();
            }
            final String source = StringUtils.replace(StringUtils.replace(source0, "\r\n", newLine), "\r", newLine);

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

        public static String addDigits(final String s1, final String s2) {
            if (!NumberUtils.isDigits(s1) && !NumberUtils.isDigits(s2)) {
                return null;
            }
            final int si1 = Integer.parseInt(NumberUtils.isDigits(s1) ? s1 : "0");
            final int si2 = Integer.parseInt(NumberUtils.isDigits(s2) ? s2 : "0");
            return String.valueOf(si1 + si2);
        }

        public static String add(final String s1, final String s2) {
            if (!NumberUtils.isNumber(s1)) { return s2; }
            if (!NumberUtils.isNumber(s2)) { return s1; }
            return new BigDecimal(s1).add(new BigDecimal(s2)).toString();
        }

        public static Integer add(final Integer i1, final Integer i2) {
            if (null == i1) { return i2; }
            if (null == i2) { return i1; }
            return i1 + i2;
        }

        public static <T> List<List<T>> splitByCount(final List<T> list, final int splitCount) {
            final List<List<T>> rtn = new ArrayList<List<T>>();
            List<T> current = null;
            for (final T item : list) {
                if (null == current || splitCount <= current.size()) {
                    current = new ArrayList<T>();
                    rtn.add(current);
                }
                current.add(item);
            }
            return rtn;
        }

        /**
         * textをMS932でデコードして16進数表示（改行コードデバッグ用）
         * @param comment 表示用コメント
         * @param text デバッグ対象文字列
         */
        public static void dumpHex(final String comment, final String text) {
            try {
                for (int j = 0; j < text.length(); j++) {
                    final String s = String.valueOf(text.charAt(j));
                    log.fatal(comment + " " + j + " " + s + " => " + toHex(s.getBytes("MS932")));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }

        private static String toHex(final byte[] bytes) {
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(" ").append(toHex(bytes[i]));
            }
            return sb.toString();
        }

        private static String toHex(final byte n) {
            final String[] ss = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
            final int nn = (n < 0) ? n + 256 : n;
            return ss[nn / 16] + ss[nn % 16];
        }

        private static <T> List<T> reverse(final Collection<T> col) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (final ListIterator<T> it = new ArrayList(col).listIterator(col.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        public static String debugRecordList(final String debugText, final List<String> headerCols, final List<Map<String, String>> recordList) {
            final StringBuffer stb = new StringBuffer(debugText + newLine);
            if (0 < recordList.size()) {
                stb.append(" header = " + Util.mkString(headerCols, ",   ")).append(newLine);
                for (int i = 0; i < recordList.size(); i++) {
                    final List<String> vals = new ArrayList<String>();
                    for (final String header : headerCols) {
                        vals.add(defstr(recordList.get(i).get(header), "-"));
                    }
                    stb.append(" [" + i + "] = " + Util.mkString(vals, ",   ")).append(newLine);
                }
            }
            return stb.toString();
        }

        public static <T> String debugCollectionToStr(final String debugText, final Collection<T> col, final String comma) {
            final StringBuffer stb = new StringBuffer();
            final List<T> list = new ArrayList<T>(col);
            stb.append(defstr(debugText) + " [\n");
            for (int i = 0; i < list.size(); i++) {
                stb.append(i == 0 ? StringUtils.repeat(" ", StringUtils.defaultString(comma).length()) : comma).append(i).append(": ").append(list.get(i)).append(newLine);
            }
            stb.append("]");
            return stb.toString();
        }

        public static String debugSqlLines(final String debugText, final List<String> list) {
            final StringBuffer stb = new StringBuffer();
            stb.append(defstr(debugText) + " [\n");
            for (int i = 0; i < list.size(); i++) {
                stb.append(list.get(i)).append(newLine);
            }
            stb.append("]");
            return stb.toString();
        }

        public static String debugArrayToStr(final String debugText, final Object[] arr) {
            final StringBuffer stb = new StringBuffer();
            stb.append(defstr(debugText) + " [\n");
            for (int i = 0; i < arr.length; i++) {
                stb.append(i == 0 ? "   " : " , ").append(i).append(": ").append(arr[i]).append(newLine);
            }
            stb.append("]");
            return stb.toString();
        }

        public static String debugMapToStr(final String debugText, final Map map0) {
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
            stb.append(defstr(debugText));
            stb.append(" [");
            final List keys = new ArrayList(map.keySet());
            try {
                Collections.sort(keys);
            } catch (Exception e) {
            }
            for (int i = 0; i < keys.size(); i++) {
                final Object key = keys.get(i);
                stb.append(i == 0 ? "\n   " : " , ").append(key).append(": ").append(map.get(key)).append(newLine);
            }
            stb.append("]");
            return stb.toString();
        }

        public static List<String> KNJ_EditKinsokuGetTokenList(final String text, final int keta, final Param param) {
            return KNJ_EditKinsokuGetTokenList(text, keta, param.kinsokuConfigMap());
        }

        public static List<String> KNJ_EditKinsokuGetTokenList(final String text, final int keta, final Map kinsokuConfigMap) {
            try {
                return KNJ_EditKinsoku.getTokenList(text, keta, kinsokuConfigMap);
            } catch (Throwable t) {
                //log.warn("old kinsoku module.");
            }
            return KNJ_EditKinsoku.getTokenList(text, keta);
        }

        public static String prepend(final String prep, final Object o) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : prep + o.toString();
        }

        public static String append(final Object o, final String app) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : o.toString() + app;
        }

        /**
         * 文字列のリストの改行文字を除去して連結する
         * @param stringList 文字列のリスト
         * @return 改行文字を除去して文字列のリストの連結する
         */
        public static String concat(final List<String> stringList) {
            final StringBuffer stb = new StringBuffer();

            for (final String str : stringList) {
                stb.append(newLineReplace(str, ""));
            }
            return stb.toString();
        }

        protected static String concatList(final List<String> arr) {
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < arr.size(); i++) {
                stb.append(defstr(arr.get(i)));
            }
            return stb.toString();
        }

        public static String newLineReplace(final String text, final String with) {
            if (null == text) {
                return null;
            }
            String text1 = text;
            final String[] newlines = {"\r\n", "\r", newLine}; // 順番に注意
            for (int i = 0; i < newlines.length; i++) {
                text1 = StringUtils.replace(text1, newlines[i], with);
            }
            return text1;
        }

        public static <E> E defObject(final E o1, final E ... o2) {
            if (null != o1) {
                return o1;
            }
            for (final E o : o2) {
                if (null != o) {
                    return o;
                }
            }
            return null;
        }

        public static <E> List<E> take(final int count, final List<E> list) {
            if (count < list.size()) {
                return list.subList(0, count);
            }
            return new ArrayList<E>(list);
        }

        public static <E> List<E> drop(final int count, final List<E> list) {
            if (list.size() <= count) {
                return new ArrayList<E>();
            }
            return new ArrayList<E>(list.subList(count, list.size()));
        }

        public static <T> List<List<T>> getPageList(final Iterable<T> list, final int maxLine) {
            final List<List<T>> rtn = new ArrayList<List<T>>();
            List<T> current = null;
            for (final T t : list) {
                if (null == current || current.size() >= maxLine) {
                    current = new ArrayList<T>();
                    rtn.add(current);
                }
                current.add(t);
            }
            return rtn;
        }


        public static <A, B> void destructuringAddAll(final Tuple<List<A>, List<B>> a, final Tuple<List<A>, List<B>> b) {
            a._first.addAll(b._first);
            a._second.addAll(b._second);
        }

        public static int mmToDot(final String mm) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
            return dot;
        }

        public static BigDecimal dotToMm(final String dot) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final BigDecimal mm = new BigDecimal(dot).multiply(mmPerInch).divide(dpi, 1, BigDecimal.ROUND_HALF_UP);
            return mm;
        }

        public static List<Integer> seq(final int start, final int n) {
            final List<Integer> rtn = new ArrayList<Integer>();
            for (int i = start; i < n; i++) {
                rtn.add(i);
            }
            return rtn;
        }

        public static List<Integer> seqInclusive(final int start, final int n) {
            return seq(start, n + 1);
        }

        public static <T> List<String> toStrList(final List<T> list) {
            final List<String> rtn = new ArrayList<String>();
            for (T t : list) {
                rtn.add(null == t ? null : t.toString());
            }
            return rtn;
        }

        public static String[] splitParam(final String parameter, final String splitChar) {
            String[] splitted = StringUtils.split(parameter, splitChar);
            if (null != splitted) {
                for (int i = 0; i < splitted.length; i++) {
                    splitted[i] = StringUtils.trim(splitted[i]);
                }
            }
            return splitted;
        }

        public static void closeQuietly(final InputStream is) {
            if (null == is) {
                return;
            }
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        public static void closeQuietly(final OutputStream os) {
            if (null == os) {
                return;
            }
            try {
                os.close();
            } catch (Exception e) {
            }
        }
        public static File copyFile(final File file, final String destFilepath) {
            File destFile = new File(destFilepath);
            if (destFile.exists()) {
                destFile.delete();
            }
            BufferedInputStream is = null;
            BufferedOutputStream os = null;
            byte[] buffer = new byte[4096];
            try {
                is = new BufferedInputStream(new FileInputStream(file));
                os = new BufferedOutputStream(new FileOutputStream(destFile));
                int count = 0;
                while (-1 != (count = is.read(buffer))) {
                    os.write(buffer, 0, count);
                }
            } catch (Exception e) {
                log.error("exception! destFile = " + destFile, e);
            } finally {
                closeQuietly(is);
                closeQuietly(os);
            }
            if (!destFile.exists()) {
                return null;
            }
            return destFile;
        }

        public static <T> Collection<T> intersection(final Collection<T> as, final Collection<T> bs) {
            final Set<T> rtn = new TreeSet<T>();
            for (final T a : as) {
                if (null != a && bs.contains(a)) {
                    rtn.add(a);
                }
            }
            return rtn;
        }
    }

    protected static class CommonPrintData {

        static final int _shingakuYou = 1;
        static final int _shusyokuYou = 2;
        final int _output;
        final String _schregno;
        final String _year;
        final String _semes;
        final String _date;
        final String _staffCd;
        final String _kanji;
        final String _comment;
        final String _os;           // OS区分
        final String _certifNumber;
        final String _ctrlYear;

        PersonInfo _personInfo = null; // 個人情報
        PersonInfo _personinfoJ; // 個人情報（中学）

        List<StudyrecDat> _studyrecData1 = Collections.emptyList();
        List<Map<String, String>> _tStudyrec = Collections.emptyList();
        Map<String, AttendrecDat> _attendMap = new HashMap<String, AttendrecDat>(); // 出欠

        final boolean _notPrintShoken;
        final boolean _notUseDaitai;
        final boolean _notUseAttend;
        final boolean _isPrintStamp;
        final boolean _isKisaiPrintStamp;
        final boolean _isHankiNinteiForm;
        final String _certifSchoolstampImagePath;
        private int _avgGradesScale = 1;
        final boolean _notUseE014;
        String _e014Subclasscd;
        Map _d020Map;
        Map<String, Title> _titles = new HashMap<String, Title>();  // 学年（年度）項目名 ** キー0に注意。_titles.keySet() **
        protected List<String> _subclassDetailDatSeq006SubclasscdList = Collections.emptyList();

        CommonSqlStudyrec _sqlStudyrec;
        StudyrecSubstitutionNote _studyrecSubstNote;

        final boolean _isPrintCertifSchoolDatRemark123ToField9;
        final String _jiritsuKatudouRemark;
        final boolean _tyousashoCheckZenbuDaitaiSubstitutedYear;
        final String _tyousasyoPrintChairSubclassSemester2; // 講座科目

        final boolean _isMiekenForm;   // 三重県フォーム使用
        int _nenyoform;
        boolean _isHesetuKou;   // 併設校:Z010.NAMESPARE2='1'
        boolean _isChuKouIkkan; // 中高一貫:Z010.NAMESPARE2='1' || '2'
        boolean _isTsushin;   // 通信制:Z001.NAMESPARE3='1'
        boolean _isKyoto;   // 京都はTrue
        boolean _isSagaken;   // 佐賀県はTrue
        boolean _isSubclassOrderNotContainCurriculumcd;
        boolean _isKanendoHyoteiYomikae; // 「評定1を2に読替」を過年度にも適用する
        boolean _useNewForm;
        boolean _isMiyagikenTsushin;
        boolean _isHirokoudaiTsushin;
        boolean _isSagakenTsushin;
        /** 出欠欄はスラッシュ */
        boolean _isFormAttendAllSlash;
        boolean _isConfigFormAttendAllSlash;
        boolean _isConfigFormEarNullSlash;
        boolean _isPrintStudyrecSubstitutionToBiko; // 教科コード'90'の代替科目備考も9.備考に表示する。

        // 熊本仕様
        protected Map<String, String> _nameMstE020Map;
        protected Map _majorCategoryDatMap;
        Map<String, String> _schoolMstMap;
        protected GakunenSeiseki _gakunenSeiseki;

        boolean _useStudyrecSql2;
        boolean _notUsePrintClass;
        String _notUseClassMstSpecialDiv; // 教科マスタの専門区分を使用しない
        protected String _majorYdatSchooldiv;
        List<String> _dropYears = Collections.emptyList(); // 留年した年度

        protected PrintClass _hanasuClass1;
        protected Map<String, String> _regdDat = Collections.emptyMap();
        protected List<String> _vNameMstD077List = Collections.emptyList(); // 科目の表示位置で評定をスラッシュ
        protected List<String> _vNameMstD077name2List = Collections.emptyList(); // 科目の表示位置で評定をスラッシュ (総学のフィールドに出力)
        protected List<String> _vNameMstD081List = Collections.emptyList(); // 総合的な学習の時間と同様評定欄は／、位置は総合的な学習の時間の上
        protected List<String> _vNameMstE065List = Collections.emptyList();

        Collection<String> _ryunenYears = new HashSet<String>();
        List<String> _abroadYears = Collections.emptyList();
        List<String> _abroadPrintDropYears = Collections.emptyList();
        List<String> _offdaysYears = Collections.emptyList();

        final String _tyousasyo2020;
        final List<String> _tyousasho2020CertifnoPage;
        private int _hyoteiKeisanMinGrades;

        private final Param _param;
        final Map<String, Object> _paramap;

        public CommonPrintData(final Param param, final Map<String, Object> paramap, final int output, final String schregno, final String year,
                final String semes, final String date, final String staffCd, final String kanji, final String comment, final String os, final String certifNumber) {
            _param = param;
            _paramap = paramap;
            _output = output;
            _schregno = schregno;
            _year = year;
            _semes = semes;
            _date = date;
            _staffCd = staffCd;
            _kanji = kanji;
            _comment = comment;
            _os = os;
            _certifNumber = certifNumber;
            _ctrlYear = getParameter(Parameter.CTRL_YEAR);
            _notPrintShoken = "1".equals(getParameter(Parameter.notUseShoken)) || "1".equals(getParameter(Parameter.certifSchoolOnly));
            _notUseDaitai = "1".equals(getParameter(Parameter.notUseDaitai)) || param._tableInfo._SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT_count == 0 || "1".equals(getParameter(Parameter.certifSchoolOnly));
            _notUseAttend = "1".equals(getParameter(Parameter.notUseAttend)) || "1".equals(getParameter(Parameter.certifSchoolOnly));
            _isMiekenForm = param._z010.in(Z010Info.Mieken, Z010Info.ChiyodaKudan);
            final String avgGradesScale = getParameter(Parameter.avgGradesScale);
            if (NumberUtils.isDigits(avgGradesScale) && 0 <= Integer.parseInt(avgGradesScale)) {
                _avgGradesScale = Integer.parseInt(avgGradesScale);
            } else if (!StringUtils.isEmpty(avgGradesScale)) {
                log.warn("not integer : " + avgGradesScale);
            }
            _notUseE014 = _isMiekenForm || _output == _shusyokuYou && (param._z010.in(Z010Info.Fukuiken, Z010Info.Miyagiken));

            _jiritsuKatudouRemark = Util.decodeISO8859_1(property(Property.tyousasyoJiritsuKatsudouRemark));
            _tyousashoCheckZenbuDaitaiSubstitutedYear = "1".equals(property(Property.tyousashoCheckZenbuDaitaiSubstitutedYear));
            final String printChairSeme2 = property(Property.tyousasyoPrintChairSubclassSemester2);
            _tyousasyoPrintChairSubclassSemester2 = !StringUtils.isBlank(printChairSeme2) ? (!"0".equals(printChairSeme2) ? printChairSeme2 : null) : param._z010.in(Z010Info.Miyagiken) ? "1" : null; // 講座科目

            final boolean isPrintKisaiStampWhenPrintStamp = param._z010.in(Z010Info.Matsudo) && _output == _shingakuYou;

            final boolean isPrintStampAlways = (param._z010.in(Z010Info.Osakatoin, Z010Info.KaichiNihonbashi) || "1".equals(property(Property.KNJE070_PRINT_STAMP))) && null == getParameter(Parameter.KNJE070_CHECK_PRINT_STAMP_PRINCIPAL);
            final List<String> stampImageExts = new ArrayList<String>();
            stampImageExts.add(".bmp");
            if (!(isPrintStampAlways || "1".equals(property(Property.knjg010HakkouPrintInei)) || "1".equals(getParameter(Parameter.KNJE070_CHECK_PRINT_STAMP_PRINCIPAL)))) {
                stampImageExts.add(".jpg");
            }
            String stampImagePath = null;
            for (final String stampImageExt : stampImageExts) {
                stampImagePath = defstr(param.getImageFilePath(property(Property.KNJE070_SCHOOLSTAMP_FILE)), param.getImageFilePath("CERTIF_SCHOOLSTAMP_H" + stampImageExt), param.getImageFilePath("SCHOOLSTAMP_H" + stampImageExt));
                if (null != stampImagePath) {
                    break;
                }
            }
            _certifSchoolstampImagePath = stampImagePath;

            _isPrintStamp = isPrintStampAlways || "1".equals(getParameter(Parameter.PRINT_STAMP)) || "1".equals(getParameter(Parameter.KNJE070_CHECK_PRINT_STAMP_PRINCIPAL));
            final boolean isPrintKisaiStampAlways = param._z010.in(Z010Info.Osakatoin) || "1".equals(property(Property.KNJE070_PRINT_KISAI_STAMP));
            _isKisaiPrintStamp = (isPrintKisaiStampAlways || _isPrintStamp && isPrintKisaiStampWhenPrintStamp) && null == getParameter(Parameter.KNJE070_CHECK_PRINT_STAMP_HR_STAFF) || "1".equals(getParameter(Parameter.KNJE070_CHECK_PRINT_STAMP_HR_STAFF));
            if (param._isOutputDebug) {
                log.info(" isPrintStamp = " + _isPrintStamp + ", isKisaiPrintStamp = " + _isKisaiPrintStamp);
            }
            _isHankiNinteiForm = "1".equals(getParameter(Parameter.HANKI_NINTEI));

            _tyousasyo2020 = output == CommonPrintData._shingakuYou ? getParameter(Parameter.tyousasyo2020) : null;
            _tyousasho2020CertifnoPage = new ArrayList<String>();
            final String tyousasho2020CertifnoPage = property(Property.tyousasho2020CertifnoPage);
            if (StringUtils.isEmpty(tyousasho2020CertifnoPage)) {
                _tyousasho2020CertifnoPage.add("all");
            } else {
                for (final String split : tyousasho2020CertifnoPage.split("\\s*,\\s*")) {
                    _tyousasho2020CertifnoPage.add(split);
                }
            }
            if (param._isOutputDebug) {
                log.info(" tyousasho2020CertifnoPage = " + _tyousasho2020CertifnoPage);
            }
            _isPrintCertifSchoolDatRemark123ToField9 = param._z010.in(Z010Info.Rakunan);

            if (_isMiekenForm) {
                _paramap.put(Property.tyousasyoAttendrecRemarkFieldSizeForPrint._name, "5 * 3");
                _paramap.put(Property.tyousasyoSpecialactrecFieldSizeForPrint._name, "12 * 10"); // 6.特別活動のフィールドサイズ (印刷)
                _paramap.put(Property.train_ref_1_2_3_field_sizeForPrint._name, "15-15-15"); // 7.指導上参考となる諸事項の桁数変更フラグ (印刷)
                _paramap.put(Property.train_ref_1_2_3_gyo_sizeForPrint._name, "17"); // 7.指導上参考となる諸事項の行数変更フラグ (印刷)
                _paramap.put(Property.tyousasyoTotalstudyactFieldSizeForPrint._name, "41 * 6"); // 8.総合的な学習の時間の内容フィールドサイズ (印刷)
                _paramap.put(Property.tyousasyoTotalstudyvalFieldSizeForPrint._name, "41 * 6"); // 8.総合的な学習の時間の評価フィールドサイズ (印刷)
                _paramap.put(Property.tyousasyoRemarkFieldSizeForPrint._name, "51 * 5"); // 9.備考フィールドサイズ (印刷)
            } else if (param._z010.in(Z010Info.Fukuiken)) {
                _paramap.put(Property.tyousasyoAttendrecRemarkFieldSizeForPrint._name, "10 * 5");
                _paramap.put(Property.tyousasyoSpecialactrecFieldSizeForPrint._name, "16 * 7"); // 6.特別活動のフィールドサイズ (印刷)
                _paramap.put(Property.train_ref_1_2_3_field_sizeForPrint._name, "18-18-18"); // 7.指導上参考となる諸事項の桁数変更フラグ (印刷)
                _paramap.put(Property.train_ref_1_2_3_gyo_sizeForPrint._name, "8"); // 7.指導上参考となる諸事項の行数変更フラグ (印刷)
                _paramap.put(Property.tyousasyoTotalstudyactFieldSizeForPrint._name, "41 * 6"); // 8.総合的な学習の時間の内容フィールドサイズ (印刷)
                _paramap.put(Property.tyousasyoTotalstudyvalFieldSizeForPrint._name, "41 * 6"); // 8.総合的な学習の時間の評価フィールドサイズ (印刷)
            }
        }

        protected void setSchoolMst(final DB2UDB db2, final Param param, final String year) {
            final String debugPreSql2 = KnjDbUtils.getDbPrginfoProperties(db2, "KNJE070", "debugPreSql2");
            final String knje070useSql2 = property(Property.knje070useSql2);
            _useStudyrecSql2 = (param._z010.in(Z010Info.Fukuiken) || "1".equals(property(Property.tyousasyoHankiNintei)) || "1".equals(debugPreSql2) || param._z010.in(Z010Info.Mieken, Z010Info.naraken, Z010Info.RitsumeikanKeisho) || param._z010.isKaichi() || param._z010.in(Z010Info.Hibarigaoka, Z010Info.Rakunan, Z010Info.NaganoSeisen, Z010Info.TamagawaSei, Z010Info.Yamamura, Z010Info.Komazawa, Z010Info.Reitaku, Z010Info.Matsudo) || "1".equals(knje070useSql2) || "1".equals(_tyousasyo2020) || param._setSogakuKoteiTanni) && !"0".equals(debugPreSql2);
            _notUsePrintClass = _useStudyrecSql2;

            if (param._isOutputDebug) {
                log.info(" useStudyrecSql2 = " + _useStudyrecSql2);
            }

            _schoolMstMap = param.getSchoolMst(db2, _year);

            final Map<String, String> z001 = param.getNameMstMap(db2, "Z001", getString(_schoolMstMap, "SCHOOLDIV"));
            _isTsushin = "1".equals(getString(z001, "NAMESPARE3"));

            if (NumberUtils.isDigits(getParameter(Parameter.NENYOFORM))) {
                _nenyoform = Integer.parseInt(getParameter(Parameter.NENYOFORM));
            } else {
                final String z001Namespare2 = getString(z001, "NAMESPARE2");
                if (Arrays.asList("3","4").contains(z001Namespare2)) {
                    _nenyoform = Integer.parseInt(z001Namespare2);
                } else if (isGakunensei(param)) {
                    _nenyoform = 3;
                } else {
                    _nenyoform = 4;
                }
            }

            if (param._z010.in(Z010Info.Fukuiken) || param._z010.in(Z010Info.naraken)) {
                _isTsushin = false;
            }
            if (param._isOutputDebug) {
                log.info(" isTsushin = " + _isTsushin);
            }
            final String z010Namespare2 = getString(param._z010Map, "NAMESPARE2");
            _isHesetuKou = "1".equals(z010Namespare2) && !_isTsushin;
            _isChuKouIkkan = ("1".equals(z010Namespare2) || "2".equals(z010Namespare2)) && !_isTsushin;
            _isKyoto = "kyoto".equals(param._z010Name1);
            _isSagaken = "sagaken".equals(param._z010Name1);

            final String tmp = KnjDbUtils.getDbPrginfoProperties(db2, "KNJE070", "useKinsokuAlphabetCharsBlock");
            param.kinsokuConfigMap().put("useAlphabetCharsBlock", _isKyoto && !"0".equals(tmp) ? "1" : null);
            //log.debug(" kinsokuConfigMap = " + kinsokuConfigMap());

            _isSubclassOrderNotContainCurriculumcd = _isKyoto;
            _isKanendoHyoteiYomikae = _isKyoto;
            _useNewForm = _isKyoto || param._z010.in(Z010Info.Miyagiken) || param._z010.in(Z010Info.Kumamoto) && !_isTsushin || _isMiekenForm || param._z010.in(Z010Info.Sundaikoufu, Z010Info.Sapporo, Z010Info.Higashiosaka, Z010Info.Seijyo, Z010Info.risshisha, Z010Info.Yamamura, Z010Info.naraken) || "1".equals(_tyousasyo2020);

            if (_isTsushin) {
                if (_isKyoto) {
                    _isFormAttendAllSlash = true;
                    _isConfigFormAttendAllSlash = true;
                    _isConfigFormEarNullSlash = true;
                } else if (param._z010.in(Z010Info.Hirokoudai)) {
                    _isHirokoudaiTsushin = true;
                } else if (param._z010.in(Z010Info.Miyagiken)) {
                    _isMiyagikenTsushin = true;
                } else if (_isSagaken) {
                    _isSagakenTsushin = true;
                }
            }

            if (param._isOutputDebug) {
                log.info(" 併設校 = " + _isHesetuKou);
                log.info(" 中高一貫 = " + _isChuKouIkkan);
            }
        }

        protected static int PRINT_GAKUNEN = 1;
        protected static int PRINT_NENDO = 2;
        protected static int PRINT_ANNUAL = 3;
        protected int getPrintGradeTitleMethod(final Param param) {
            int rtn;
            if (CommonPrintData._shusyokuYou == _output) {
                if (isGakunensei(param) || param._z010.in(Z010Info.Kumamoto) || param._isShusyokuyouKinkiToitsu2) {
                    rtn = PRINT_GAKUNEN;
                } else {
                    rtn = PRINT_NENDO;
                }
            } else {
                if (param._z010.in(Z010Info.Rakunan, Z010Info.ChiyodaKudan)) {
                    rtn = PRINT_ANNUAL;
                } else if (isGakunensei(param)) {
                    rtn = PRINT_GAKUNEN;
                } else {
                    rtn = PRINT_NENDO;
                }
            }
            return rtn;
        }

        /**
         * 個人情報
         */
        public void setPersonInfoMap(final DB2UDB db2, final Param param) {
            final String sMikomiNensu = param.getG009Name1(db2, _year);
            final int mikomiNensu = NumberUtils.isDigits(sMikomiNensu) ? Integer.valueOf(sMikomiNensu).intValue() : 3;
            final String sql = PersonInfo.getPersonInfoSql(param, mikomiNensu);
            if (param._isOutputDebugQuery) {
                log.info(" person sql = " + sql);
            }
            final List<Map<String, String>> list = KnjDbUtils.query(db2, sql, new String[] {_schregno, _year, _semes});
            if (!list.isEmpty()) {
                final Map<String, String> map = KnjDbUtils.firstRow(list);
                if (param._isOutputDebugBase) {
                    log.info(Util.debugMapToStr(" personinfo ", map));
                }

                Map<String, String> address = null;
                if (param._isPrintGrd) {
                    final String grdBaseSql = " SELECT CUR_ADDR1 AS ADDR1, CUR_ADDR2 AS ADDR2, CUR_TELNO AS TELNO, CUR_ZIPCD AS ZIPCD, CUR_ADDR_FLG AS ADDR_FLG FROM GRD_BASE_MST WHERE SCHREGNO = '" + _schregno + "' ";
                    address = KnjDbUtils.lastRow(KnjDbUtils.query(db2, grdBaseSql));
                } else {
                    final boolean useSonotaJuusyo = !param._isPrintGrd && "on".equals(getParameter(Parameter.SONOTAJUUSYO));
                    if (param._isOutputDebugBase) {
                        log.info(" certifkind = " + getParameter(Parameter.CERTIFKIND) + " sonotaJuusyo? " + useSonotaJuusyo);
                    }
                    if (useSonotaJuusyo) {
                        final String sendAddressSql = " SELECT SEND_ADDR1 AS ADDR1, SEND_ADDR2 AS ADDR2, SEND_TELNO AS TELNO, SEND_ZIPCD AS ZIPCD, SEND_ADDR_FLG AS ADDR_FLG FROM SCHREG_SEND_ADDRESS_DAT WHERE SCHREGNO = '" + _schregno + "' AND DIV = '1' ";
                        final Map<String, String> sendAddress = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sendAddressSql));
                        if (!StringUtils.isBlank(getString(sendAddress, "ADDR1"))) {
                            address = sendAddress;
                            if (param._isOutputDebug) {
                                log.info("その他住所使用");
                            }
                        }
                    }
                    if (null == address) {
                        final String schregAddressSql = " SELECT * FROM SCHREG_ADDRESS_DAT WHERE SCHREGNO = '" + _schregno + "' AND FISCALYEAR(ISSUEDATE) <= '" + _year + "' ORDER BY ISSUEDATE ";
                        address = KnjDbUtils.lastRow(KnjDbUtils.query(db2, schregAddressSql));
                    }
                }
                if (param._isOutputDebugBase) {
                    log.info(" address = " + address);
                }
                _personInfo = new PersonInfo(param, map, address, false);
            }
            if (null == _personInfo) {
                _personInfo = new PersonInfo(param, null, null, false);
                return;
            }
            if (param._preferEntInfoJ) {
                final String sqlj = PersonInfo.getPersonInfoSchoolKindJSql(param);
                if (param._isOutputDebugQuery) {
                    log.info(" person j sql = " + sqlj);
                }
                final List<Map<String, String>> listJ = KnjDbUtils.query(db2, sqlj, new String[] {_schregno, _year, _semes});
                if (!listJ.isEmpty()) {
                    final Map<String, String> mapJ = KnjDbUtils.firstRow(listJ);
                    if (param._isOutputDebugBase) {
                        log.info(Util.debugMapToStr(" personinfo j ", mapJ));
                    }
                    _personinfoJ = new PersonInfo(param, mapJ, null, true);
                }
            }
        }

        public AttendrecDat getAttendrecDat(final String key) {
            AttendrecDat a = _attendMap.get(key);
            if (_param._z010.in(Z010Info.Meikei)) {
                // 休学の年度の留年分の出欠を加算する
                final Title title = Title.getTitle(_param, _titles.values(), key);
                if (null != title) {
                    for (final String offdaysDropYear : getOffdaysDropYears(title)) {
                        a = AttendrecDat.add(_param, a, _attendMap.get(offdaysDropYear));
                    }
                }
            }
            return a;
        }

        // 休学した年度で留年した年度がある場合、最初の留年した年度の出欠を返す （奈良県）
        protected AttendrecDat getOffdaysDropAttendrecDat(final Title title) {
            final List<String> offdaysDropYears = getOffdaysDropYears(title);
            final AttendrecDat offAtt = offdaysDropYears.isEmpty() ? null : getAttendrecDat(offdaysDropYears.get(0));
            return offAtt;
        }

        /**
         * 休学した年度で留年した年度　（茗溪、奈良県で使用）
         * @param title
         * @return　休学した年度で留年した年度
         */
        protected List<String> getOffdaysDropYears(final Title title) {
            final List<String> sameGradecdDropYears = new ArrayList<String>();
            for (final Title t : _titles.values()) {
                if (t != title && _dropYears.contains(t._year) && title._gradeCd.equals(t._gradeCd)) {
                    sameGradecdDropYears.add(t._year);
                }
            }
            final List<String> offdaysDropYears = new ArrayList<String>(Util.intersection(_offdaysYears, sameGradecdDropYears));
            if (_param._isOutputDebugCalc) {
                _param.logOnce(" offdaysYears = " + _offdaysYears + ", title = " + title + ", sameGradecdDropYears = " + sameGradecdDropYears + ", intersection = " + offdaysDropYears);
            }
            return offdaysDropYears;
        }

        public Collection<String> getYears() {
            final List<String> years = new ArrayList<String>();
            for (final Title title : titleValues()) {
                if (NumberUtils.isDigits(title._year)) {
                    years.add(title._year);
                }
            }
            return years;
        }

        public Collection<Title> titleValues() {
            return _titles.values();
        }

        public String getSchoolDiv(final Param param) {
            final String schooldiv;
            if ("1".equals(property(Property.useGakkaSchoolDiv))) {
                schooldiv = !StringUtils.isBlank(_majorYdatSchooldiv) ? _majorYdatSchooldiv : getString(_schoolMstMap, "SCHOOLDIV");
            } else {
                schooldiv = getString(_schoolMstMap, "SCHOOLDIV");
            }
            return schooldiv;
        }

        public boolean isGakunensei(final Param param) {
            return "0".equals(getSchoolDiv(param));
        }

        public void load(final DB2UDB db2, final Param param) {
            _hanasuClass1 = PrintClass.create(param, this);

            if (null != _hanasuClass1) {
                log.info(" hanasuClasscd = " + _hanasuClass1);
            }

            _regdDat = KnjDbUtils.lastRow(KnjDbUtils.query(db2, " SELECT YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + _schregno + "' AND YEAR = '" + _year + "' ORDER BY SEMESTER "));

            _notUseClassMstSpecialDiv = setNotUseClassMstSpecialDiv(db2, _year, getParameter(Parameter.CERTIFKIND), _schregno);

            if ("1".equals(property(Property.useGakkaSchoolDiv))) {
                setMajorYdatSchooldiv(db2, param, _schregno, _year);
            }

            setDropYearList(db2, param);

            _vNameMstD077List = _output == _shusyokuYou ? new ArrayList<String>() : param.getD077List(db2, _year);
            _vNameMstD077name2List = _output == _shusyokuYou ? new ArrayList<String>() : param.getD077ListName2(db2, _year);
            _vNameMstD081List = param.getD081List(db2, _year);
            _vNameMstE065List = param.getE065List(db2, _year);

            _hyoteiKeisanMinGrades = "Y".equals(param.getD015Namespare1(db2, _year)) ? 0 : 1;

            if ("print100".equals(getParameter(Parameter.HYOTEI))) {
                _gakunenSeiseki = GakunenSeiseki.load(db2, param, this);
            }
        }

        private void setDropYearList(final DB2UDB db2, final Param param) {
            if (isGakunensei(param)) {
                final String psKey = "PS_REGD_DROP_YEAR";
                if (null == param.getPs(psKey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT DISTINCT T1.YEAR");
                    stb.append(" FROM SCHREG_REGD_DAT T1");
                    stb.append(" WHERE T1.SCHREGNO = ? ");
                    stb.append("   AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 WHERE T2.SCHREGNO = ? AND T2.YEAR <= ? GROUP BY T2.GRADE)");

                    param.setPs(db2, psKey, stb.toString());
                }

                _dropYears = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _schregno, _year}), "YEAR");
//                log.info(" dropYears = " + _dropYears);
            }
        }

        /**
         * 指定生徒・年度の学科年度データの学校区分を得る
         */
        private void setMajorYdatSchooldiv(final DB2UDB db2, final Param param, final String schregno, final String year) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("   SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   WHERE SCHREGNO = '" + schregno + "' AND YEAR = '" + year + "' ");
            stb.append("   GROUP BY SCHREGNO, YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T4.SCHOOLDIV ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
            stb.append(" INNER JOIN MAJOR_YDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("     AND T4.COURSECD = T1.COURSECD ");
            stb.append("     AND T4.MAJORCD = T1.MAJORCD ");
            //log.debug(" majorYdatSchooldivSql = " + stb.toString());

            _majorYdatSchooldiv = getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "SCHOOLDIV");

            if (param._isOutputDebug) {
                log.info(" majorYdatSchoolDiv = " + _majorYdatSchooldiv);
            }
        }

        /**
         * 教科マスタの専門区分を使用の設定
         * ・生徒の入学日付の年度が、証明書学校データのREMARK7の値（年度）以前の場合
         *  1) 成績欄データのソートに教科マスタの専門区分を使用しない。
         *  2) 成績欄に教科マスタの専門区分によるタイトルを表示しない。（名称マスタ「E015」設定に優先する。）
         *   ※証明書学校データのREMARK7の値（年度）が null の場合
         *    1) 専門区分をソートに使用する。
         *    2) タイトルの表示/非表示は名称マスタ「E015」の設定による。
         */
        private static String setNotUseClassMstSpecialDiv(final DB2UDB db2, final String year, final String certifKind, final String schregno) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH T_SCHOOL_KIND AS ( ");
            sql.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
            sql.append("     FROM SCHREG_REGD_DAT T1 ");
            sql.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            sql.append("         AND T2.GRADE = T1.GRADE ");
            sql.append("     WHERE ");
            sql.append("         T1.SCHREGNO = '" + schregno + "' ");
            sql.append("         AND T2.YEAR = '" + year + "' ");
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("     T1.SCHREGNO, ");
            sql.append("     FISCALYEAR(T1.ENT_DATE) AS ENT_YEAR, ");
            sql.append("     T4.REMARK7 ");
            sql.append(" FROM ");
            sql.append("     SCHREG_ENT_GRD_HIST_DAT T1 ");
            sql.append("     INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("     INNER JOIN CERTIF_SCHOOL_DAT T4 ON T4.YEAR = T2.YEAR AND T4.CERTIF_KINDCD = '" + certifKind + "' ");

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
            final String entYear = getString(row, "ENT_YEAR");
            final String remark7 = getString(row, "REMARK7");
            if (!NumberUtils.isDigits(entYear) || !NumberUtils.isDigits(remark7)) {
                return "0";
            }
            return Integer.parseInt(entYear) <= Integer.parseInt(remark7) ? "1" : "0";
        }

        public boolean isMeijiSogo(final Param param) {
            if (!param._z010.in(Z010Info.Meiji)) {
                return false;
            }
            boolean contain2010Lower = false;
            for (final String year : getYears()) {
                if (Integer.parseInt(year) <= 2010) {
                    contain2010Lower = true;
                }
            }
            return param._z010.in(Z010Info.Meiji) && contain2010Lower;
        }

        public static List<String> get90OverSubclasscdList(final List<String> subclasscdList) {
            final List<String> rtn = new ArrayList<String>();
            for (final String subclasscd : subclasscdList) {
                if (defstr(subclasscd).length() < 2 || !NumberUtils.isDigits(subclasscd.substring(0, 2))) {
                    log.warn("not digits : " + subclasscd);
                    continue;
                }
                final String classcd = subclasscd.substring(0, 2);
                if (Integer.parseInt(classcd) > 90) {
                    rtn.add(subclasscd);
                }
            }
            return rtn;
        }

        /**
         * （熊本仕様）名称マスタE020読み込み
         */
        protected void setMajorCategoryDat(final Param param, final DB2UDB db2) {
            _nameMstE020Map = new HashMap<String, String>();
            final Map<String, Map<String, String>> e020Map = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'E020' "), "NAMECD2");
            for (final String cd : Arrays.asList("1", "2", "3")) {
                final Map<String, String> m =Param.getMap(e020Map, cd);
                final String name;
                if (CommonPrintData._shusyokuYou == _output) {
                    name = (!StringUtils.isBlank(getString(m, "NAME2")) ? getString(m, "NAME2") : getString(m, "NAME1"));
                } else {
                    name = getString(m, "NAME1");
                }
                _nameMstE020Map.put(cd, name);
            }

            _majorCategoryDatMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT COURSECD || '-' || MAJORCD AS CD, CATEGORYCD FROM MAJOR_CATEGORY_DAT "), "CD", "CATEGORYCD");
        }

        protected String getMajorCategoryCd(final String coursecd, final String majorcd) {
            final String key = coursecd + "-" + majorcd;
            final String categoryCd = !_majorCategoryDatMap.containsKey(key) ? null : getString(_majorCategoryDatMap, key);
            if (null == categoryCd) {
                log.warn(" no major category dat : coursecd = " + coursecd + ", majorcd = " + majorcd);
            }
            return categoryCd;
        }

        protected String getMajorNameWithMajorCategoryDat(final Param param, final String categoryCd, final String majorname) {
            String e020Name1 = null;
            if (null != categoryCd) {
                if (param._z010.in(Z010Info.Kumamoto)) {
                    e020Name1 = defstr(_nameMstE020Map.get(categoryCd));
                    if ("2".equals(categoryCd)) { // 専門
                        e020Name1 = e020Name1 + "（" + majorname + "）";
                    }
                } else {
                    if ("2".equals(categoryCd)) { // 専門
                        e020Name1 = defstr(_nameMstE020Map.get(categoryCd)) + "（" + majorname + "）";
                    } else {
                        e020Name1 = defstr(majorname);
                    }
                }
            }
            return e020Name1;
        }

        protected void preprocessStudyrecDat(final DB2UDB db2, final Param param) throws SQLException {
            //名称マスタD020：登録されている科目は、明細から除外する。
//            PreparedStatement ps1 = param.getPs(ps1Key);
//            ResultSet rs = ps1.executeQuery();
//            final Map d020Map0 = new HashMap();
//            String e014Subclasscd0 = null;
//            while (rs.next()) {
//                final String d020Cd = rs.getString("D020");
//                if (!param._isMusashi || (param._isMusashi && null != d020Cd && null != rs.getString("GRADES"))) {
//                    int d020Cnt = 1;
//                    if (d020Map0.containsKey(d020Cd)) {
//                        d020Cnt += ((Integer) d020Map0.get(d020Cd)).intValue();
//                    }
//                    d020Map0.put(d020Cd, new Integer(d020Cnt));
//                }
//                if (null != rs.getString("E014")) {
//                    e014Subclasscd0 = rs.getString("E014");
//                }
//            }
//            DbUtils.closeQuietly(rs);
            PreparedStatement ps1 = param.getPs(ps1Key);
            final Map d020Map0 = new HashMap();
            String e014Subclasscd0 = null;
            for (final Map<String, String> row : KnjDbUtils.query(db2, ps1, null)) {
                final String d020Cd = getString(row, "D020");
                if (!param._z010.in(Z010Info.Musashi) || (param._z010.in(Z010Info.Musashi) && null != d020Cd && null != getString(row, "GRADES"))) {
                    int d020Cnt = 1;
                    if (d020Map0.containsKey(d020Cd)) {
                        d020Cnt += ((Integer) d020Map0.get(d020Cd)).intValue();
                    }
                    d020Map0.put(d020Cd, new Integer(d020Cnt));
                }
                if (null != getString(row, "E014")) {
                    e014Subclasscd0 = getString(row, "E014");
                }
            }
            if (_notUseE014) {
                // フォームがないので使用しない
            } else {
                _e014Subclasscd = e014Subclasscd0;
            }
            _d020Map = d020Map0;
        }

        private int getStudyrecCredit(final String keySubclasscd, final Param param) {
            int rtn = 0;
            for (final StudyrecDat studyrecDat : _studyrecData1) {
                if (keySubclasscd.equals(studyrecDat.keySubclasscd(param))) {
                    final String cre = studyrecDat.credit();
                    if (StringUtils.isNumeric(cre)) {
                        rtn += Integer.parseInt(cre);
                    }
                }
            }
            return rtn;
        }

        private int getStudyrecSogoCredit() {
            for (final StudyrecDat studyrecDat : _studyrecData1) {
                if (SqlStudyrec.sogo.equals(studyrecDat._classname)) { // TODO: classname
                    final String cre = studyrecDat.creditSuraSum1();
                    if (StringUtils.isNumeric(cre)) {
                        return Integer.parseInt(cre);
                    } else {
                        return 0;
                    }

                }
            }
            return 0;
        }

        public String getParameter(final Parameter parameter) {
            return (String) _paramap.get(parameter._name);
        }

        public String property(final Property property) {
            return _param.property(_paramap, property._name, property._preferPropertyFile);
        }

        public String property(final String name) {
            return _param.property(_paramap, name);
        }

        public boolean paramapContains(final Parameter parameter) {
            return _paramap.containsKey(parameter._name);
        }
    }

    // 個人情報
    protected static class PersonInfo {

        private String _name;
        private String _nameKana;
        private String _realName;
        boolean _useRealName;
        boolean _isPrintNameBoth;
        String _sex;
        String _birthday;
        String _addr1;
        String _addr2;
        String _addrFlg;
        final String _entDiv;
        String _grdDiv;
        String _grdDate;
        String _graduDate;
        String _coursename;
        String _majorname;

        String _entDate;
        String _entSchool;
        String _entYear;
        String _curriculumYear;
        String _entYearGradeCd;
        String _regdGradeCd;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        boolean _graduateAble;

        final Map<String, String> _map;
        PersonInfo(final Param param, final Map<String, String> map, final Map<String, String> address, final boolean isJ) {
            _map = map;
            _entDiv = getString("ENT_DIV");
            if (!isJ) {
                _name = getString("NAME");
                _nameKana = getString("NAME_KANA");
                _realName = getString("REAL_NAME");
                _useRealName = "1".equals(getString("USE_REAL_NAME"));
                _isPrintNameBoth = "1".equals(getString("NAME_OUTPUT_FLG"));
                _sex = getString("SEX");
                _birthday = getString("BIRTHDAY");
                _addr1 = KnjDbUtils.getString(address, "ADDR1");
                _addr2 = KnjDbUtils.getString(address, "ADDR2");
                _addrFlg = KnjDbUtils.getString(address, "ADDR_FLG");
                _grdDiv = getString("GRD_DIV");
                _grdDate = getString("GRD_DATE");
                _graduDate = getString("GRADU_DATE");
                _coursename = getString("COURSENAME");
                _majorname = getString("MAJORNAME");

                _entDate = getString("ENT_DATE");
                _entSchool = getString("ENT_SCHOOL");
                _entYear = getString("ENT_YEAR");
                _curriculumYear = getString("CURRICULUM_YEAR");
                _entYearGradeCd = getString("ENT_YEAR_GRADE_CD");
                _regdGradeCd = getString("GRADE_CD");
                _graduateAble = "1".equals(getString("GRADUATE_ABLE"));
                if (param._isOutputDebugBase) {
                    log.info(" curriculumYear = " + _curriculumYear + ", _entYearGradeCd = " + _entYearGradeCd + ", entSchool = " + _entSchool + ", graduateAble = " + _graduateAble);
                }
                _coursecd = getString("COURSECD");
                _majorcd = getString("MAJORCD");
                _coursecode = getString("COURSECODE");
            }
        }

        public String name() {
            return _name;
        }

        public String nameKana() {
            return _nameKana;
        }

        public String realName() {
            return _realName;
        }

        String getPrintName() {
            if (_isPrintNameBoth) {
                return defstr(_realName) + "（" + defstr(_name) + "）";
            } else if (_useRealName) {
                return _realName;
            }
            return _name;
        }

        protected String getString(final String field) {
            return KnjDbUtils.getString(_map, field);
        }

        public static boolean isNyugaku(final String entDiv) {
            return "1".equals(entDiv) || "2".equals(entDiv) || "3".equals(entDiv);
        }

        public static boolean isTennyuHennyu(final String entDiv) {
            return "4".equals(entDiv) || "5".equals(entDiv);
        }

        private static String getPersonInfoSql(final Param param, final int mikomiNensu) {
            final String tableRegd;
            if (!param._isPrintGrd) {
                tableRegd = "SCHREG_REGD_DAT";
            } else {
                tableRegd = "GRD_REGD_DAT";
            }

            final DecimalFormat zero2 = new DecimalFormat("00");
            final String q = "?";

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append(" BASE.NAME,");
            sql.append(" BASE.NAME_KANA,");
            sql.append(" BASE.BIRTHDAY, ");
            sql.append(" BASE.SEX, ");
            sql.append(" BASE.REAL_NAME, ");
            sql.append(" BASE.REAL_NAME_KANA, ");

            sql.append(" KGLED.BIRTHDAY_FLG, ");
            sql.append(" REGD.GRADE, ");
            sql.append(" REGD.ATTENDNO, ");
            sql.append(" REGD.ANNUAL, ");
            sql.append(" REGDH.HR_NAME,");
            sql.append(" REGDH.HR_NAMEABBV,");
            //課程・学科・コース
            sql.append(" CRSM.COURSECD, ");
            sql.append(" CRSM.COURSENAME, ");
            sql.append(" CRSM.COURSEABBV, ");
            sql.append(" MAJM.MAJORCD, ");
            if (param._tableInfo._hasMAJOR_MST_MAJORNAME2) {
                sql.append(" VALUE(MAJM.MAJORNAME2, MAJM.MAJORNAME) AS MAJORNAME, ");
            } else {
                sql.append(" MAJM.MAJORNAME, ");
            }
            sql.append(" MAJM.MAJORABBV,");
            sql.append(" CCM.COURSECODE, ");
            sql.append(" CCM.COURSECODENAME, ");
            if (param._tableInfo._hasCOURSECODE_MST_COURSECODEABBV1) {
                sql.append("  CCM.COURSECODEABBV1,");
            } else {
                sql.append("  CAST(NULL AS VARCHAR(1)) AS COURSECODEABBV1,");
            }
            if (param._tableInfo._hasCOURSECODE_MST_COURSECODEABBV3) {
                sql.append("  CCM.COURSECODEABBV3,");
            } else {
                sql.append("  CAST(NULL AS VARCHAR(1)) AS COURSECODEABBV3,");
            }

            sql.append(" EGHIST.CURRICULUM_YEAR, ");

            // 卒業
            sql.append(" EGHIST.GRD_TERM, ");
            sql.append(" EGHIST.GRD_DATE, ");
            sql.append(" EGHIST.GRD_DIV, ");
            sql.append(" FISCALYEAR(EGHIST.ENT_DATE) AS ENT_YEAR,");
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN ");
            if (!param._isPrintGrd) {
                sql.append(" RTRIM(CHAR(INT(REGD.YEAR) ");
                sql.append("            + case REGD.annual ");
                for (int grade = 1; grade < mikomiNensu; grade++) {
                    sql.append("                when '" + zero2.format(param._schoolKindStartGradeInt - 1 + grade) + "' then " + String.valueOf(mikomiNensu - grade + 1) + " ");
                }
                sql.append("                else 1 end ");
                sql.append("           )) || '-' || RTRIM(CHAR(MONTH(SCM.GRADUATE_DATE))) || '-'  || RTRIM(CHAR(DAY(SCM.GRADUATE_DATE))) ");
            } else {
                //卒業
                sql.append(" CASE WHEN INT(REGD.ANNUAL) < 3 THEN NULL ELSE RTRIM(CHAR(INT(REGD.YEAR) + 1)) || '-' || RTRIM(CHAR(MONTH(SCM.GRADUATE_DATE))) || '-01' END ");
            }
            sql.append("  ELSE VARCHAR(EGHIST.GRD_DATE) END AS GRADU_DATE,");
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN '卒業見込み' ELSE NMA003.NAME1 END AS GRADU_NAME,");
            // 入学
            sql.append(" EGHIST.ENT_DATE, ");
            sql.append(" EGHIST.ENT_SCHOOL, ");
            sql.append(" EGHIST.ENT_DIV,");
            sql.append(" (SELECT DISTINCT ST1.ANNUAL FROM " + tableRegd + " ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("  WHERE ST1.SCHREGNO = REGD.SCHREGNO  AND ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) ");
            sql.append(" )  AS ENTER_GRADE,");

            sql.append(" (SELECT DISTINCT ST3.GRADE_CD FROM " + tableRegd + " ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("   WHERE ST1.SCHREGNO = REGD.SCHREGNO AND ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) ");
            sql.append(" ) AS ENT_YEAR_GRADE_CD,");
            sql.append(" NMA002.NAME1 AS ENTER_NAME,");
            sql.append(" NMA002.NAME2 AS ENTER_NAME2,");

            sql.append(" CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            sql.append(" T11.NAME_OUTPUT_FLG, ");

            sql.append(" BASE.NATIONALITY, ");
            sql.append(" REGDG.GRADE_CD, ");
            sql.append(" CASE WHEN VALUE(NMA023.NAMESPARE2, NMA023.NAME3) <= REGD.GRADE THEN 1 END AS GRADUATE_ABLE, ");
            sql.append(" REGDG.SCHOOL_KIND, ");
            sql.append(" REGD.SCHREGNO ");
            sql.append("FROM " + tableRegd + " REGD ");
            sql.append("LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER ");
            sql.append("    AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            sql.append("LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
            sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = REGD.SCHREGNO AND EGHIST.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
            if (!param._isPrintGrd) {
                // 基礎情報
                sql.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                // 卒業情報有りの場合
                sql.append("INNER JOIN SCHOOL_MST SCM ON SCM.YEAR = REGD.YEAR ");
            } else {
                //基礎情報
                sql.append("INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                //卒業情報有りの場合
                sql.append("LEFT JOIN SCHOOL_MST SCM ON SCM.YEAR = REGD.YEAR ");
            }
            if (param._tableInfo._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(" AND SCM.SCHOOL_KIND = 'H' ");
            }
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST CRSM ON CRSM.COURSECD = REGD.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST MAJM ON MAJM.COURSECD = REGD.COURSECD AND MAJM.MAJORCD = REGD.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST CCM ON CCM.YEAR = REGD.YEAR AND VALUE(CCM.COURSECODE,'0000') = VALUE(REGD.COURSECODE,'0000')");
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '01' ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT KGLED ON KGLED.SCHREGNO = BASE.SCHREGNO AND KGLED.BIRTHDAY_FLG = '1' ");
            sql.append("LEFT JOIN NAME_MST NMA002 ON 'A002' = NMA002.NAMECD1 AND EGHIST.ENT_DIV = NMA002.NAMECD2 ");
            sql.append("LEFT JOIN NAME_MST NMA003 ON 'A003' = NMA003.NAMECD1 AND EGHIST.GRD_DIV = NMA003.NAMECD2 ");
            sql.append("LEFT JOIN NAME_MST NMA023 ON 'A023' = NMA023.NAMECD1 AND EGHIST.SCHOOL_KIND = NMA023.NAME1 ");
            sql.append("WHERE REGD.SCHREGNO = " + q + " ");
            sql.append("  AND REGD.YEAR = " + q + " ");
            sql.append("  AND REGD.SEMESTER = " + q + " ");

            return sql.toString();
        }

        private static String getPersonInfoSchoolKindJSql(final Param param) {
            final String tableRegd;
            if (!param._isPrintGrd) {
                tableRegd = "SCHREG_REGD_DAT";
            } else {
                tableRegd = "GRD_REGD_DAT";
            }

            final String q = "?";

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");

            sql.append(" EGHIST.CURRICULUM_YEAR, ");

            sql.append(" FISCALYEAR(EGHIST.ENT_DATE) AS ENT_YEAR,");
            // 入学
            sql.append(" EGHIST.ENT_DATE, ");
            sql.append(" EGHIST.ENT_SCHOOL, ");
            sql.append(" EGHIST.ENT_DIV,");
            sql.append(" (SELECT DISTINCT ST1.ANNUAL FROM " + tableRegd + " ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("  WHERE ST1.SCHREGNO = REGD.SCHREGNO  AND ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) ");
            sql.append(" )  AS ENTER_GRADE,");

            sql.append(" (SELECT DISTINCT ST3.GRADE_CD FROM " + tableRegd + " ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("   WHERE ST1.SCHREGNO = REGD.SCHREGNO AND ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) ");
            sql.append(" ) AS ENT_YEAR_GRADE_CD,");
            sql.append(" NMA002.NAME1 AS ENTER_NAME,");
            sql.append(" NMA002.NAME2 AS ENTER_NAME2,");

            sql.append(" REGD.SCHREGNO ");
            sql.append("FROM " + tableRegd + " REGD ");
            sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = REGD.SCHREGNO AND EGHIST.SCHOOL_KIND = 'J' ");
            if (!param._isPrintGrd) {
                // 基礎情報
                sql.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                // 卒業情報有りの場合
                sql.append("INNER JOIN SCHOOL_MST SCM ON SCM.YEAR = REGD.YEAR ");
                if (param._tableInfo._hasSCHOOL_MST_SCHOOL_KIND) {
                    sql.append(" AND SCM.SCHOOL_KIND = 'J' ");
                }
            } else {
                //基礎情報
                sql.append("INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            }
            // 課程、学科、コース
            sql.append("LEFT JOIN NAME_MST NMA002 ON 'A002' = NMA002.NAMECD1 AND EGHIST.ENT_DIV = NMA002.NAMECD2 ");
            sql.append("WHERE REGD.SCHREGNO = " + q + " ");
            sql.append("  AND REGD.YEAR = " + q + " ");
            sql.append("  AND REGD.SEMESTER = " + q + " ");

            return sql.toString();
        }
    }

    protected static class Title implements Cloneable {
        static final String NYUGAKUMAE = "入学前";

        static int INVALID_POS = -100;

        final int _intKey;
        final String _year;
        final String _gradeCd;
        final String _annual;
        final String _name;
        final List<String> _nameArray1;
        final List<String> _nameArrayShushoku;
        final List<String> _nameArray2020Shojikou;
        final List<String> _nameArrayAttend;
        final String _gengouNen;
        int _pos;
        int _seisekiPos;
        public Title(final int intKey, final String year, final String annual, final DB2UDB db2, final String gradeCd, final CommonPrintData printData, final Param param) {
            _intKey = intKey;
            _year = year;
            _gradeCd = gradeCd;
            _annual = annual;
            _gengouNen = KNJ_EditDate.gengou(db2, intKey);

            final String titlename;
            final List<String> titlenameArray1;
            final List<String> titlenameArrayShushoku;
            final List<String> titlenameArray2020Shojikou;
            final List<String> titlenameAttend;
            final int method = printData.getPrintGradeTitleMethod(param);
            if (0 == intKey) {
                if (CommonPrintData.PRINT_GAKUNEN == method || CommonPrintData.PRINT_ANNUAL == method) {
                    titlename = NYUGAKUMAE;
                } else {
                    if (param._z010.in(Z010Info.Mieken) || param._z010.in(Z010Info.KaichiTsushin)) {
                        titlename = NYUGAKUMAE;
                    } else {
                        titlename = NYUGAKUMAE + "年度";
                    }
                }
                titlenameArray1 = Arrays.asList("入", "学", "前");
                titlenameArrayShushoku = Arrays.asList("入", "学", "前");
                titlenameArray2020Shojikou = Arrays.asList("入", "学", "前");
                titlenameAttend = Arrays.asList("入", "学", "前");
            } else if (CommonPrintData.PRINT_ANNUAL == method) {
                final String ann;
                if (NumberUtils.isDigits(annual)) {
                    ann = String.valueOf(Integer.parseInt(annual));
                } else {
                    ann = " ";
                }
                titlename = "第" + ann + "学年";
                final String _annualForPrint = NumberUtils.isDigits(_annual) ? Integer.valueOf(_annual).toString() : "";
                titlenameArray1 = Arrays.asList("第", _annualForPrint, "学年");
                titlenameArrayShushoku = Arrays.asList(_annualForPrint + "年");
                titlenameArray2020Shojikou = Arrays.asList("第", _annualForPrint, "学", "年");
                titlenameAttend = Arrays.asList("第", _annualForPrint, "学年");
            } else if (CommonPrintData.PRINT_GAKUNEN == method) {
                final String ann;
                if (NumberUtils.isDigits(_gradeCd)) {
                    ann = String.valueOf(Integer.parseInt(_gradeCd));
                } else if (NumberUtils.isDigits(annual)) {
                    ann = String.valueOf(Integer.parseInt(annual));
                } else {
                    ann = " ";
                }
                titlename = "第" + ann + "学年";
                titlenameArray1 = Arrays.asList("第", ann, "学年");
                titlenameArrayShushoku = Arrays.asList(ann + "年");
                titlenameArray2020Shojikou = Arrays.asList("第", ann, "学", "年");
                titlenameAttend = Arrays.asList("第", ann, "学年");
            } else {
                if (param._isSeireki) {
                    final String nen = String.valueOf(intKey);
                    titlename = nen + "年度";
                    titlenameArray1 = Arrays.asList("", nen, "年度");
                    titlenameArrayShushoku = Arrays.asList(nen.substring(0, 2), nen.substring(2), "年度");
                    titlenameArray2020Shojikou = Arrays.asList(nen, "年", "度");
                    titlenameAttend = Arrays.asList("", nen, "年度");
                } else {
                    final String gengou = _gengouNen.substring(0, 2);
                    final String nen = _gengouNen.substring(2);
                    titlename = _gengouNen + "年度";
                    titlenameArray1 = Arrays.asList(gengou, nen, "年度");
                    titlenameArrayShushoku = Arrays.asList(gengou, nen, "年度");
                    titlenameArray2020Shojikou = Arrays.asList(gengou, nen, "年", "度");
                    titlenameAttend = Arrays.asList(gengou, nen, "年度");
                }
            }
            _name = titlename;
            _nameArray1 = titlenameArray1;
            _nameArrayShushoku = titlenameArrayShushoku;
            _nameArray2020Shojikou = titlenameArray2020Shojikou;
            _nameArrayAttend = titlenameAttend;
            if (param._isOutputDebug) {
                log.info(" intKey = " + intKey + ", year = " + year + ", method = " + method + ", _name = " + _name + ", nameArray1 = " + ArrayUtils.toString(_nameArray1) + ", nameArray2 = " + ArrayUtils.toString(_nameArrayShushoku));
            }
        }

        private Title(final int intKey, final String year, final String annual, final String name, final String gradeCd
                , final List<String> nameArray1
                , final List<String> nameArrayShushoku
                , final List<String> nameArray2020Shojikou
                , final List<String> nameArrayAttend
                , final String gengouNen) {
            _intKey = intKey;
            _year = year;
            _annual = annual;
            _name = name;
            _gradeCd = gradeCd;
            _nameArray1 = nameArray1;
            _nameArrayShushoku = nameArrayShushoku;
            _nameArray2020Shojikou = nameArray2020Shojikou;
            _nameArrayAttend = nameArrayAttend;
            _gengouNen = gengouNen;
        }

        public Object clone() {
            final Title title = new Title(_intKey, _year, _annual, _name, _gradeCd, _nameArray1, _nameArrayShushoku, _nameArray2020Shojikou, _nameArrayAttend, _gengouNen);
            title._pos = _pos;
            title._seisekiPos = _seisekiPos;
            return title;
        }

        public static Title getTitle(final Param param, final Collection<Title> titleList, final String year) {
            Title rtn = null;
            if (null == year) {
                return rtn;
            }
            for (final Title title : titleList) {
                if (year.equals(title._year)) {
                    rtn = title;
                    break;
                } else if (Util.toInt(year, -1) == 0 && Util.toInt(title._year, -1) == 0) {
                    rtn = title;
                    log.warn("different key : " + year + " <> " + title._year);
                    break;
                }
            }
            if (null == rtn) {
                param.logOnce(getCaller() + " >> no title : " + year + " in " + titleList);
            }
            return rtn;
        }

        public static List<Title> getValidated(final Collection<Title> titleList) {
            final List<Title> titles = new ArrayList<Title>();
            for (final Title title : titleList) {
                if (title._pos == Title.INVALID_POS) {
                    continue;
                }
                titles.add(title);
            }
            return titles;
        }

        public static void setPosition(final Param param, final CommonPrintData printData, final Collection<Title> titles) {
            String caller = "";
            if (param._isOutputDebugData) {
                caller = getCaller();
            }

            if (param._isOutputDebugData) {
                log.info(caller + " >> titles = " + titles);
            }
            int pos = 0;
            int seisekiPos = 0;
            for (final Title title : titles) {
                if (printData._ryunenYears.contains(title._year) && !printData._abroadPrintDropYears.contains(title._year)) {
                    title._pos = Title.INVALID_POS;
                } else {
                    pos += 1;
                    title._pos = pos;
                    if (printData._abroadPrintDropYears.contains(title._year)) {
                        title._seisekiPos = Title.INVALID_POS;
                    } else {
                        seisekiPos += 1;
                        title._seisekiPos = seisekiPos;
                    }
                }
                if (param._isOutputDebugData) {
                    log.info(caller + " >> pos = " + title._pos + ", title = " + title);
                }
            }
        }

        private static String getCaller() {
            String caller = "";
            try {
                final StackTraceElement[] sts = Thread.currentThread().getStackTrace();
//            		for (final StackTraceElement st : sts) {
//            			log.info(" : " + st.getClassName() + "." + st.getMethodName() + " : " + st.getLineNumber());
//            		}
                final StackTraceElement st = sts[4];
                caller =  st.getClassName() + "." + st.getMethodName() + ":" + st.getLineNumber();
            } catch (Throwable e) {
                log.error("exception in getStackTrace(): " + e.getMessage());
            }
            return caller;
        }

        public String toString() {
            return "Title(year = " + _year + ", annual = " + _annual + ", name = " + _name + ", pos = " + _pos + ")";
        }
    }

    public static class CommonSqlStudyrec { // アクセス指定子変更しないで!

        public static final String abroad = "abroad";
        public static final String hirokokulhr = "lhr";
        public static final String sogo = "sogo";
        public static final String zenseki = "zenseki";
        public static final String daiken = "daiken";
        public static final String total = "total";
        public static final String totalCredit = "totalCredit";
        public static final String e014 = "e014";
        public static final String E014SUBCLASSNAME = "E014SUBCLASSNAME";
//        public static final String KYOTO88_SUBCLASSNAME = "KYOTO88SUBCLASSNAME";
//        public static final String D081List = "D081List";

        public static final String RECORD_FLG_00_STUDYREC = "RECORD_FLG_00_STUDYREC";
        public static final String RECORD_FLG_01_CHAIR_SUBCLASS = "RECORD_FLG_01_CHAIR_SUBCLASS";

        private static final String hyoteiHyde = "hyde";

        public static int DAIKEN_DIV0_SUM = 0;
        public static int DAIKEN_DIV1_DETAIL = 1;

        protected String _hyoutei;  // 評定の読替え  １を２と評定
        private String _TableName_StudyRec;
        private String _TableName_Transfer;
        private String _TableName_Schreg_Regd;
        private int _daiken_div_code;  // 大検の集計方法 0:合計 1:明細
        private String _zensekiSubclassCd;  // 前籍校の成績専用科目コード

        public void setParam(final CommonPrintData printData, final Param param) {
            if ("1".equals(printData.getParameter(Parameter.PRINT_GRD))) {
                _TableName_StudyRec = "GRD_STUDYREC_DAT";
                _TableName_Schreg_Regd = "GRD_REGD_DAT";
                _TableName_Transfer = "GRD_TRANSFER_DAT";
            } else {
                _TableName_StudyRec = "SCHREG_STUDYREC_DAT";
                _TableName_Schreg_Regd = "SCHREG_REGD_DAT";
                _TableName_Transfer = "SCHREG_TRANSFER_DAT";
            }
            //log.debug("学習記録データは " + _TableName_StudyRec + " を使用。異動データは " + _TableName_Transfer + " を使用。学籍データは " + _TableName_Schreg_Regd + " を使用。");

            if (CommonPrintData._shusyokuYou == printData._output) {
                if (!param.isKindaifuzoku()) {
                    _daiken_div_code = DAIKEN_DIV1_DETAIL;
                    _zensekiSubclassCd = param._e011cd01Namespare1;
                }
            } else {
                _daiken_div_code = param._e011cd02Namespare1;
                _zensekiSubclassCd = param._e011cd01Namespare1;
            }

            _hyoutei = defstr(printData.getParameter(Parameter.HYOTEI), "off");
        }

//        public String getStudentE014Subclass(final DB2UDB db2, final PrintData printData) {
//            String e014Subclasscd = null;
//            final StringBuffer stb = new StringBuffer();
//            stb.append(getStudyrecSqlString(printData));
//            stb.append(" SELECT ");
//            stb.append("    T1.SUBCLASSCD ");
//            stb.append(" FROM ");
//            stb.append("    STUDYREC T1 ");
//            stb.append(" INNER JOIN NAME_MST L1 ON L1.NAMECD1 = 'E014' AND L1.NAME1 = T1.SUBCLASSCD ");
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                //名称マスタE014：登録されている科目は、明細から除外する。
//                if (rs.next() && null != rs.getString("SUBCLASSCD")) {
//                    e014Subclasscd = rs.getString("SUBCLASSCD");
//                }
//            } catch (SQLException e) {
//                log.error("Exception!" + stb.toString(), e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return e014Subclasscd;
//        }

//        /**
//         * @param _definecode 設定する _definecode。
//         */
//        public void setDefinecode(final KNJDefineSchool definecode) {
//            _definecode = definecode;
//        }

        public Tuple<List<Map<String, String>>, List<Map<String, String>>> pre_sql2(final DB2UDB db2, final CommonPrintData printData, final Param param) {

            final List<String> dropYears = new ArrayList<String>();
            if (printData.isGakunensei(param)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH DROP_YEAR AS(");
                stb.append("        SELECT DISTINCT YEAR");
                stb.append("        FROM SCHREG_REGD_DAT T1");
                stb.append("        WHERE SCHREGNO = '" + printData._schregno + "' ");
                stb.append("        AND T1.YEAR NOT IN (SELECT MAX(YEAR) FROM SCHREG_REGD_DAT T2 WHERE SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' GROUP BY GRADE)");
                stb.append(" ) ");
                stb.append(" SELECT * FROM DROP_YEAR ");
                dropYears.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "YEAR"));
            }

            // 該当生徒の成績データ表
            final List<String> stb1 = new ArrayList<String>();
            getStudyrecSqlString1(printData, param, stb1);
            stb1.add(" SELECT * FROM STUDYREC_DAT ");
            final String stb = Util.mkString(stb1, newLine);  // 調査書仕様の学習記録データの抽出
            if (param._isOutputDebugQuery) {
                log.info(" record sql = " + stb.toString());
            }

            final List<Map<String, String>> tStudyrecDat = KnjDbUtils.query(db2, stb.toString());
            final List<String> headerCols = new ArrayList<String>(Arrays.asList("RECORD_FLG", "SCHREGNO", "YEAR", "ANNUAL", "SCHOOLCD", "GRADES", "CREDIT", "COMP_CREDIT", "GET_CREDIT", "ADD_CREDIT", "CLASSCD"));
            if ("1".equals(param._useCurriculumcd)) {
                headerCols.add("SCHOOL_KIND");
                headerCols.add("CURRICULUM_CD");
            }
            headerCols.addAll(Arrays.asList("SUBCLASSCD", "CREDIT_MST_CREDIT", "VALID_FLG", "D065FLG", "CLASSNAME", "SUBCLASSNAME"));

            final Rows tStudyrecDatRows = new Rows(headerCols, tStudyrecDat);

            final List<Tuple<String, String>> orderByField = new LinkedList<Tuple<String, String>>();
            final String typeDefault = Listp.Comparator.TYPE_String;
            orderByField.add(Tuple.of("D065FLG", Listp.Comparator.TYPE_NUMBER));
            if (!"1".equals(printData._notUseClassMstSpecialDiv)) {
                orderByField.add(Tuple.of("SPECIALDIV", typeDefault));
            }
            orderByField.add(Tuple.of("SHOWORDERCLASS", Listp.Comparator.TYPE_NUMBER));
            orderByField.add(Tuple.of("CLASSCD", typeDefault));
            if (!printData._isSubclassOrderNotContainCurriculumcd && "1".equals(param._useCurriculumcd)) {
                orderByField.add(Tuple.of("SCHOOL_KIND", typeDefault));
                orderByField.add(Tuple.of("CURRICULUM_CD", typeDefault));
            }
            orderByField.add(Tuple.of("SHOWORDERSUBCLASS", Listp.Comparator.TYPE_NUMBER));
            orderByField.add(Tuple.of("SUBCLASSCD", typeDefault));
            orderByField.add(Tuple.of("YEAR", typeDefault));
            if (param.isOutputDebugSql2(null)) {
                log.info(" orderByField = " + orderByField);
            }

            // :STUDYREC
            final Rows tStudyrec = Listp.getStudyrec(db2, printData, param, tStudyrecDatRows);
            if (param.isOutputDebugSql2(null)) {
                log.info(Util.debugRecordList(" pre_sql2 record ", tStudyrec._headerCols, tStudyrec._recordList));
            }

            // :MAIN
            final Rows main = Listp.getMain(printData, param, this, tStudyrec);
            if (param.isOutputDebugSql2(null)) {
                log.info(" pre_sql2 proc 1 end.");
                log.info(Util.debugRecordList("rtn3", main._headerCols, main._recordList));
            }

            Rows rtn3 = new Rows(new ArrayList(), new ArrayList());
            rtn3 = rtn3.unionAll(param, main);

            Rows rtn3_2 = new Rows(new ArrayList(), new ArrayList());
            // :SOGO
            rtn3_2 = rtn3_2.unionAll(param, Listp.get90(printData, param, tStudyrec));
            // :ABROAD
            rtn3_2 = rtn3_2.unionAll(param, Listp.getAbroad(db2, this, printData, param));
            if (!param._z010.in(Z010Info.Hirokoku)) {
                // :LHR
                rtn3_2 = rtn3_2.unionAll(param, Listp.getLhr(printData, param, tStudyrec));
            }
            if (param.isOutputDebugSql2(null)) { log.info(" pre_sql2 proc 2 end."); }

            if (CommonPrintData._shingakuYou == printData._output) {
                // :TOTAL_HYOTEI_HEIKIN
                rtn3_2 = rtn3_2.unionAll(param, Listp.getTotalHyoteiHeikin(printData, param, this, tStudyrec));
                // :TOTAL_CREDIT
                rtn3_2 = rtn3_2.unionAll(param, Listp.getTotalCredit(printData, param, this, tStudyrec));

                if (param.isOutputDebugSql2(null)) { log.info(" pre_sql2 proc 3."); }
            }
            if (null != _zensekiSubclassCd) {
                // :ZENSEKI
                rtn3_2 = rtn3_2.unionAll(param, Listp.getZenseki(this, param, tStudyrecDat));
            }
            // :E014
            if (!param.getE014Name1List(printData._notUseE014).isEmpty()) {
                rtn3_2 = rtn3_2.unionAll(param, Listp.getE014(printData, param, tStudyrec, dropYears));
            }
            if (DAIKEN_DIV0_SUM == _daiken_div_code) {
                // :DAIKEN
                rtn3_2 = rtn3_2.unionAll(param, Listp.getDaiken(param, tStudyrecDat, dropYears));
            }
            if (param._z010.in(Z010Info.Hirokoku)) {
                // :HIROKOKU94
                rtn3_2 = rtn3_2.unionAll(param, Listp.getHirokoku94(printData, param, tStudyrec));
            }
            if (param.isOutputDebugSql2(null)) {
                log.info(" pre_sql2 proc 4 end.");
                log.info(Util.debugRecordList("rtn3_2", rtn3_2._headerCols, rtn3_2._recordList));
            }
            rtn3 = rtn3.unionAll(param, rtn3_2);

            Collections.sort(rtn3._recordList, new Listp.Comparator(orderByField));
            if (param.isOutputDebugSql2(null)) {
                log.info(" pre_sql2 proc end.");
                log.info(Util.debugRecordList("rtn3", rtn3._headerCols, rtn3._recordList));
            }
            return Tuple.of(tStudyrec._recordList, rtn3._recordList);
        }

        private static class Rows {
            final List<String> _headerCols;
            final List<Map<String, String>> _recordList;
            Rows(final List<String> headerCols, final List<Map<String, String>> recordList) {
                _headerCols = headerCols;
                _recordList = recordList;
            }
            public Rows unionAll(final Param param, final Rows otherRows) {
                if (param._isOutputDebug || param._isOutputDebugQuery) {
                    keyCheck();
                }
                final Rows newRows = new Rows(new ArrayList<String>(_headerCols.isEmpty() ? otherRows._headerCols : _headerCols), new ArrayList<Map<String, String>>(_recordList));
                if (param._isOutputDebug || param._isOutputDebugQuery) {
                    otherRows.keyCheck();
                }
                newRows._recordList.addAll(otherRows._recordList);
                if (param._isOutputDebug || param._isOutputDebugQuery) {
                    newRows.keyCheck();
                }
                return newRows;
            }

            public Map keyCheck() {
                Map oldRow = null;
                for (final Map<String, String> row : _recordList) {
                    final Set ks = stringOnly(new HashSet(row.keySet()));
                    if (null == oldRow) {
                        oldRow = row;
                        continue;
                    }
                    Set ks2 = stringOnly(new HashSet(oldRow.keySet()));
                    if (!ks.equals(ks2)) {
                        log.fatal(" rows key diff : " + CollectionUtils.disjunction(ks, ks2));
                        log.fatal("               : row1 = " + stringKeyOnly(oldRow));
                        log.fatal("               : row2 = " + stringKeyOnly(row));
                        oldRow = row;
//            		} else {
//            			log.info(" same key : " + keySet + " <> " + ks);
                    }
                }
                return oldRow;
            }

            private Set<Object> stringOnly(final Set<Object> set) {
                for (final Iterator<Object> it = set.iterator(); it.hasNext();) {
                    final Object e = it.next();
                    if (!(e instanceof String)) {
                        it.remove();
                        continue;
                    }
                    final String se = (String) e;
                    if (NumberUtils.isDigits(se)) {
                        it.remove();
                        continue;
                    }
                }
                return set;
            }

            private Map stringKeyOnly(final Map map) {
                for (final Iterator<Object> it = map.keySet().iterator(); it.hasNext();) {
                    final Object e = it.next();
                    if (!(e instanceof String)) {
                        it.remove();
                        continue;
                    }
                    final String se = (String) e;
                    if (NumberUtils.isDigits(se)) {
                        it.remove();
                        continue;
                    }
                }
                return map;
            }
        }

        private static abstract class Listp {
            static String INNER_JOIN = "INNER_JOIN";
            static String LEFT_JOIN = "LEFT_JOIN";

            private static class Comparator implements java.util.Comparator<Map<String, String>> {
                static String TYPE_NUMBER = "0";
                static String TYPE_String = "1";
                final List<Tuple<String, String>> _fields;
                Comparator(final List<Tuple<String, String>> fields) {
                    _fields = fields;
                }
                @Override
                public int compare(final Map<String, String> row1, final Map<String, String> row2) {
                    for (final Tuple<String, String> field : _fields) {
                        final int type; // 0: String, 1:Number
                        if (TYPE_NUMBER.equals(field._second)) {
                            type = 1;
                        } else {
                            type = 0;
                        }
                        final String val1 = getString(row1, field._first);
                        final String val2 = getString(row2, field._first);
                        switch (type) {
                        case 1: // Number
                            if (NumberUtils.isNumber(val1)) {
                                if (NumberUtils.isNumber(val2)) {
                                    final int cmp = new BigDecimal(val1).compareTo(new BigDecimal(val2));
                                    if (0 != cmp) {
                                        return cmp;
                                    }
                                } else {
                                    return -1;
                                }
                            } else {
                                if (NumberUtils.isNumber(val2)) {
                                    return 1;
                                } else {
                                    // return 0;
                                }
                            }
                        default: // String
                            if (null != val1) {
                                if (null != val2) {
                                    final int cmp = val1.compareTo(val2);
                                    if (0 != cmp) {
                                        return cmp;
                                    }
                                } else {
                                    return -1;
                                }
                            } else {
                                if (null != val2) {
                                    return 1;
                                } else {
                                    // return 0;
                                }
                            }
                        }
                    }
                    return 0;
                }
            }

            private static String valueKey(final Map<String, String> row, final List<String> fields) {
                final List<String> values = new ArrayList<String>();
                for (int i = 0; i < fields.size(); i++) {
                    values.add(getString(row, fields.get(i)));
                }
                final String valueKey = Util.mkString(values, "-");
                return valueKey;
            }

            // :SUBCLASSGROUP
            public static Rows getSubclassgroupOnlySubclasscd2(final CommonPrintData printData, final Param param, final List<Map<String, String>> tStudyrecDat) {

                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                } else {
                    groupByField = Arrays.asList(                                           "SUBCLASSCD");
                }

                final Map grouped = new HashMap();
                for (final Map<String, String> row : tStudyrecDat) {

                    final String valueKey = Listp.getMappedMapKey(grouped, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(grouped, valueKey), "ROW_LIST").add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                for (final Iterator it = grouped.keySet().iterator(); it.hasNext();) {
                    final String key = (String) it.next();
                    final Map group = (Map) grouped.get(key);

                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    final String subclasscd = getString(group, "SUBCLASSCD");
                    if ("1".equals(param._useCurriculumcd)) {
                        classcd = getString(group, "CLASSCD");
                        schoolKind = getString(group, "SCHOOL_KIND");
                        curriculumCd = getString(group, "CURRICULUM_CD");
                    } else {
                        classcd = subclasscd.substring(0, 2);
                    }

                    final SubclassMst t2 = SubclassMst.getSubclassMst(param._subclassMstMap, SubclassMst.key(param, classcd, schoolKind, curriculumCd, subclasscd));
                    if (null == t2 || null == t2._subclasscd2) {
                        continue;
                    }

                    final SubclassMst sclm = SubclassMst.getSubclassMst(param._subclassMstMap, SubclassMst.key(param, classcd, schoolKind, curriculumCd, t2._subclasscd2));
                    if (null == sclm) {
                        continue;
                    }

                    final ClassMst clm = ClassMst.getClassMst(param._classMstMap, ClassMst.key(param, classcd, schoolKind));
                    if (null == clm) {
                        continue;
                    }

                    final Map<String, String> record = new HashMap<String, String>();
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("CLASSCD", classcd);
                        record.put("SCHOOL_KIND", schoolKind);
                        record.put("CURRICULUM_CD", curriculumCd);
                    }
                    record.put("SUBCLASSCD", subclasscd);
                    record.put("SUBCLASSCD2", t2._subclasscd2);
                    record.put("CLASSNAME", clm._classname);
                    record.put("CLASSORDERNAME1", clm._classordername1);
                    record.put("SHOWORDERCLASS", clm._showorder2.toString());
                    record.put("SUBCLASSNAME", sclm._subclassname);
                    record.put("SUBCLASSORDERNAME1", sclm._subclassordername1);
                    record.put("SHOWORDERSUBCLASS", sclm._showorder2.toString());

                    resultList.add(record);

                }

                final Rows rows = new Rows(Arrays.asList("SUBCLASSCD" ,"SUBCLASSCD2", "CLASSNAME", "CLASSORDERNAME1", "SHOWORDERCLASS", "SUBCLASSNAME", "SUBCLASSORDERNAME1", "SHOWORDERSUBCLASS"), resultList);
                  if (param._isOutputDebugSeiseki) {
                      log.info(Util.debugCollectionToStr(" getSubclassgroupOnlySubclasscd2 = ", rows._recordList, newLine));
                  }
                return rows;
            }


            // :STUDYREC_SUBCLASSGROUP
            public static Rows getStudyrecSubclassgroupOnlySubclasscd2MinClassnameSubclassname(final CommonPrintData printData, final Param param, final Rows subclassgroupOnlySubclasscd2, final Rows tStudyrecDat) {

//                stb.add("   SELECT ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.add("    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD,");
//                }
//                stb.add("    T2.SUBCLASSCD");
//                stb.add("  , MIN(T2.CLASSNAME) AS CLASSNAME");
//                stb.add("  , MIN(T2.SUBCLASSNAME) AS SUBCLASSNAME");
//                stb.add(" FROM  SUBCLASSGROUP T1 INNER JOIN STUDYREC_DAT T2 ON T1.SUBCLASSCD2 = T2.SUBCLASSCD");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.add("        AND T1.CLASSCD = T2.CLASSCD");
//                    stb.add("        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND");
//                    stb.add("        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD");
//                }
//                stb.add("         AND (T2.SUBCLASSNAME IS NOT NULL OR T2.CLASSNAME IS NOT NULL)");
//                stb.add(" GROUP BY ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.add("    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
//                }
//                stb.add("        T2.SUBCLASSCD");

                final Rows tSubclassnameClassname = new Rows(tStudyrecDat._headerCols, new ArrayList(tStudyrecDat._recordList));
                for (final Iterator<Map<String, String>> it = tSubclassnameClassname._recordList.iterator(); it.hasNext();) {
                    final Map<String, String> row = it.next();
                    if (null == getString(row, "CLASSNAME") && null == getString(row, "SUBCLASSNAME")) {
                        it.remove();
                    }
                }

                final List<String> joinField;
                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    joinField    = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                } else {
                    joinField    = Arrays.asList(                                           "SUBCLASSCD");
                    groupByField = Arrays.asList(                                           "SUBCLASSCD");
                }

                final List<Map<String, Map<String, String>>> joined = Listp.join(INNER_JOIN,
                        subclassgroupOnlySubclasscd2, "T1",
                        tSubclassnameClassname, "T2", joinField);

                final Map grouped = new HashMap();
                for (final Map<String, Map<String, String>> row : joined) {

                    final Map row1 = Util.getMappedMap(row, "T1");

                    final String valueKey = Listp.getMappedMapKey(grouped, groupByField, row1);
                    Util.getMappedList(Util.getMappedMap(grouped, valueKey), "ROW_LIST").add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                for (final Iterator it = grouped.keySet().iterator(); it.hasNext();) {
                    final String key = (String) it.next();
                    final Map group = (Map) grouped.get(key);

                    final List rowList = Util.getMappedList(group, key);

                    final List t2List = new ArrayList();
                    for (final Iterator t2it = rowList.iterator(); t2it.hasNext();) {
                        final Map row = (Map) t2it.next();
                        final Map rowT2 = Util.getMappedMap(row, "T2");
                        t2List.add(rowT2);
                    }

                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    final String subclasscd = getString(group, "SUBCLASSCD");
                    if ("1".equals(param._useCurriculumcd)) {
                        classcd = getString(group, "CLASSCD");
                        schoolKind = getString(group, "SCHOOL_KIND");
                        curriculumCd = getString(group, "CURRICULUM_CD");
                    } else {
                        classcd = subclasscd.substring(0, 2);
                    }

                    final Map<String, String> record = new HashMap<String, String>();
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("CLASSCD", classcd);
                        record.put("SCHOOL_KIND", schoolKind);
                        record.put("CURRICULUM_CD", curriculumCd);
                    }
                    record.put("SUBCLASSCD", subclasscd);
                    record.put("CLASSNAME", min(KnjDbUtils.getColumnDataList(t2List, "CLASSNAME")));
                    record.put("SUBCLASSNAME", min(KnjDbUtils.getColumnDataList(t2List, "SUBCLASSNAME")));

                    resultList.add(record);

                }

                final Rows rows = new Rows(Arrays.asList("SUBCLASSCD", "CLASSNAME", "SUBCLASSNAME"), resultList);
                  if (param._isOutputDebugSeiseki) {
                      log.info(Util.debugCollectionToStr(" getStudyrecSubclassgroupOnlySubclasscd2MinClassnameSubclassname = ", rows._recordList, newLine));
                  }
                return rows;
            }

            // :STUDYREC
            public static Rows getStudyrec(final DB2UDB db2, final CommonPrintData printData, final Param param, final Rows tStudyrecDat) {

                // :SUBCLASSGROUP
                final Rows subclassgroupOnlySubclasscd2 = Listp.getSubclassgroupOnlySubclasscd2(printData, param, tStudyrecDat._recordList);

                // :STUDYREC_SUBCLASSGROUP
                final Rows studyrecSubclassGroupMinClassnameSubclassname = Listp.getStudyrecSubclassgroupOnlySubclasscd2MinClassnameSubclassname(printData, param, subclassgroupOnlySubclasscd2, tStudyrecDat);

                final Map t2Group = new HashMap();
                final List<String> t2GroupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    t2GroupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                } else {
                    t2GroupByField = Arrays.asList(                                           "SUBCLASSCD");
                }

                for (final Map<String, String> row : subclassgroupOnlySubclasscd2._recordList) {

                    final String valueKey = Listp.getMappedMapKey(t2Group, t2GroupByField, row);
                    Util.getMappedList(Util.getMappedMap(t2Group, valueKey), "ROW_LIST").add(row);
                }

                final Map t3Group = new HashMap();
                final List<String> t3GroupByField;
                final List<String> t3GroupByFieldt2;
                if ("1".equals(param._useCurriculumcd)) {
                    t3GroupByField   = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                    t3GroupByFieldt2 = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD2");
                } else {
                    t3GroupByField   = Arrays.asList(                                           "SUBCLASSCD");
                    t3GroupByFieldt2 = Arrays.asList(                                           "SUBCLASSCD2");
                }

                for (final Map<String, String> row : studyrecSubclassGroupMinClassnameSubclassname._recordList) {

                    final String valueKey = Listp.getMappedMapKey(t3Group, t3GroupByField, row);
                    Util.getMappedList(Util.getMappedMap(t3Group, valueKey), "ROW_LIST").add(row);
                }

                final Map provFlgGroup = new HashMap();
                final List<String> provFlgGroupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    provFlgGroupByField = Arrays.asList("SCHOOLCD", "YEAR", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                } else {
                    provFlgGroupByField = Arrays.asList("SCHOOLCD", "YEAR",                                            "SUBCLASSCD");
                }
                if ("1".equals(printData.property(Property.useProvFlg))) {
                    final List<Map<String, String>> provFlgList = KnjDbUtils.query(db2, " SELECT * FROM STUDYREC_PROV_FLG_DAT WHERE SCHREGNO = '" + printData._schregno + "' ");

                    for (final Map<String, String> row : provFlgList) {

                        final String valueKey = Listp.getMappedMapKey(provFlgGroup, provFlgGroupByField, row);
                        Util.getMappedList(Util.getMappedMap(provFlgGroup, valueKey), "ROW_LIST").add(row);
                    }
                }

                boolean _isPrintRisyu = "1".equals(defstr(printData.getParameter(Parameter.RISYU), "1"));  // 履修のみ科目出力 1:出力する 2:出力しない
                boolean _isPrintMirisyu = "1".equals(defstr(printData.getParameter(Parameter.MIRISYU), "2"));  // 未履修科目出力 1:出力する 2:出力しない

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

                for (final Map<String, String> row : tStudyrecDat._recordList) {

                    Map prv = null;
                    if ("1".equals(printData.property(Property.useProvFlg))) {
                        final String provFlgValueKey = Listp.getMappedMapKey(provFlgGroup, provFlgGroupByField, row);
                        prv = KnjDbUtils.firstRow(Util.getMappedList(Util.getMappedMap(provFlgGroup, provFlgValueKey), "ROW_LIST"));
                    }

                    boolean isTarget;
                    final String year = getString(row, "YEAR");
                    if (param.isKindaifuzoku()) {

                        final String subclasscd = getString(row, "CLASSCD") + "-" + getString(row, "SCHOOL_KIND") + "-" + getString(row, "CURRICULUM_CD") + "-" + getString(row, "SUBCLASSCD");
                        final boolean isAttendSubclass = Util.getMappedList(param._yearSubclassRepaceCombineDatAttendSubclasscdListMap, year).contains(subclasscd);
                        isTarget = !isAttendSubclass;

                    } else {
                        boolean isTarget1;
                        boolean isTarget2;
                        final String schoolcd = getString(row, "SCHOOLCD");
                        if (DAIKEN_DIV0_SUM == printData._sqlStudyrec._daiken_div_code) {
                            isTarget1 = "0".equals(schoolcd);
                        } else {
                            isTarget1 = "0".equals(schoolcd) || "2".equals(schoolcd) && null != getString(row, "CREDIT");
                        }
                        if (null != printData._sqlStudyrec._zensekiSubclassCd) {
                            isTarget2 = ("1".equals(schoolcd) || "0".equals(year)) && !printData._sqlStudyrec._zensekiSubclassCd.equals(getString(row, "SUBCLASSCD"));
                        } else {
                            isTarget2 = "1".equals(schoolcd) || "0".equals(year);
                        }
                        isTarget = isTarget1 || isTarget2;

                        final String classcd = getString(row, "CLASSCD");
                        //履修のみ科目出力・・・「履修のみ」とは、「修得単位がゼロ　かつ　履修単位がゼロ以外」
                        if (!_isPrintRisyu) {
                            if ("on".equals(printData._sqlStudyrec._hyoutei)){ //----->評定読み替えのON/OFF  評定１を２と読み替え
                                //final String chkProvFlg = "1".equals(param._useProvFlg) ? " PROV_FLG = '1' AND " : "";
                                // 単位数は
                                //  GET_CREDIT が 0の時、
                                //  SCHREG_STUDYREC_DAT に登録された COMP_CREDIT（履修単位）の値が0以外は COMP_CREDIT（履修単位）を GET_CREDIT として取得、
                                //  0の時は単位マスタの CREDITS を GET_CREDIT として取得して、
                                //  ADD_CREDITを加算する。

                                Integer getCredit;
                                final boolean chkProvFlg;
                                if ("1".equals(printData.property(Property.useProvFlg))) {
                                    chkProvFlg = "1".equals(getString(prv, "PROV_FLG"));
                                } else if (printData._isKanendoHyoteiYomikae) {
                                    chkProvFlg = true;
                                } else {
                                    chkProvFlg = defstr(year).equals(printData._year);
                                }
                                if (chkProvFlg && KnjDbUtils.getInt(row, "GRADES", 0) == 1 && KnjDbUtils.getInt(row, "GET_CREDIT", -1) == 0) {
                                    getCredit = defval(KnjDbUtils.getInt(row, "COMP_CREDIT", null), KnjDbUtils.getInt(row, "CREDIT_MST_CREDIT", null));
                                } else {
                                    getCredit = KnjDbUtils.getInt(row, "GET_CREDIT", null);
                                }
                                final Integer addCredit = KnjDbUtils.getInt(row, "ADD_CREDIT", 0);
                                if (null == getCredit && null == addCredit) {
                                    isTarget = false;
                                } else {
                                    isTarget = isTarget && ((defval(getCredit, 0) + defval(addCredit, 0)) != 0 || param._setSogakuKoteiTanni && KNJDefineSchool.subject_T.equals(classcd));
                                }
                            } else {
                                //「修得単位がゼロ　かつ　履修単位がゼロ以外」のレコードは印刷しない。
                                isTarget = isTarget && (KnjDbUtils.getInt(row, "CREDIT", 0) > 0 || param._setSogakuKoteiTanni && KNJDefineSchool.subject_T.equals(classcd));
                            }
                        }
                        if (!_isPrintMirisyu) {
                            if (PrintClass.is94_(printData) && CommonPrintData._shingakuYou == printData._output) {

                                isTarget = isTarget && (KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && classcd.compareTo(KNJDefineSchool.subject_U) <= 0 || KNJDefineSchool.subject_T.equals(classcd)) && KnjDbUtils.getInt(row, "COMP_CREDIT", 0) > 0;

                                isTarget = isTarget || PrintClass.isHanasu(printData._hanasuClass1, classcd) || param._setSogakuKoteiTanni && KNJDefineSchool.subject_T.equals(classcd);
                            } else {
                                isTarget = isTarget && (KnjDbUtils.getInt(row, "COMP_CREDIT", 0) > 0 || param._setSogakuKoteiTanni && KNJDefineSchool.subject_T.equals(classcd));
                            }
                        }
                    }
                    if (!isTarget) {
                        continue;
                    }

                    final String t2ValueKey = valueKey(row, t2GroupByField);
                    final Map t2 = KnjDbUtils.firstRow(Util.getMappedList(Util.getMappedMap(t2Group, t2ValueKey), "ROW_LIST"));

                    final String t3ValueKeyT2 = valueKey(t2, t3GroupByFieldt2);
                    final Map t3 = KnjDbUtils.firstRow(Util.getMappedList(Util.getMappedMap(t3Group, t3ValueKeyT2), "ROW_LIST"));

                    final String classMstKey;
                    final String subclassMstKey;
                    if ("1".equals(param._useCurriculumcd)) {
                        classMstKey = ClassMst.key(param, getString(row, "CLASSCD"), getString(row, "SCHOOL_KIND"));
                        subclassMstKey = SubclassMst.key(param, getString(row, "CLASSCD"), getString(row, "SCHOOL_KIND"), getString(row, "CURRICULUM_CD"), getString(row, "SUBCLASSCD"));
                    } else {
                        classMstKey = ClassMst.key(param, getString(row, "CLASSCD"), null);
                        subclassMstKey = SubclassMst.key(param, null, null, null, getString(row, "SUBCLASSCD"));
                    }
                    final ClassMst clm = ClassMst.getClassMst(param._classMstMap, classMstKey);
                    final SubclassMst sclm = SubclassMst.getSubclassMst(param._subclassMstMap, subclassMstKey);
                    final String schoolcd = getString(row, "SCHOOLCD");
                    final ClassMst anotherClassMst;
                    if ("1".equals(schoolcd)) {
                        final AnotherClassMst a = AnotherClassMst.getAnotherClassMst(param._anotherClassMstMap, classMstKey);
                        if (AnotherClassMst.Null == a) {
                            anotherClassMst = ClassMst.getClassMst(param._classMstMap, classMstKey);
                        } else {
                            anotherClassMst = a;
                        }
                    } else {
                        anotherClassMst = AnotherClassMst.Null;
                    }
                    final SubclassMst anotherSubclassMst;
                    if ("1".equals(schoolcd)) {
                        final AnotherSubclassMst a = AnotherSubclassMst.getAnotherSubclassMst(param._anotherSubclassMstMap, subclassMstKey);
                        if (AnotherSubclassMst.Null == a) {
                            anotherSubclassMst = SubclassMst.getSubclassMst(param._subclassMstMap, subclassMstKey);
                        } else {
                            anotherSubclassMst = a;
                        }
                    } else {
                        anotherSubclassMst = AnotherSubclassMst.Null;
                    }

                    final Map<String, String> newRow = new HashMap<String, String>();
                    newRow.put("RECORD_FLG", getString(row, "RECORD_FLG"));
                    newRow.put("SCHREGNO", getString(row, "SCHREGNO"));
                    newRow.put("YEAR", year);
                    newRow.put("ANNUAL", getString(row, "ANNUAL"));
                    newRow.put("SCHOOLCD", schoolcd);
                    newRow.put("GRADES", getString(row, "GRADES"));
                    newRow.put("CREDIT", getString(row, "CREDIT"));
                    newRow.put("COMP_CREDIT", getString(row, "COMP_CREDIT"));
                    newRow.put("GET_CREDIT", getString(row, "GET_CREDIT"));
                    newRow.put("ADD_CREDIT", getString(row, "ADD_CREDIT"));
                    newRow.put("CLASSCD", getString(row, "CLASSCD"));
                    if ("1".equals(param._useCurriculumcd)) {
                        newRow.put("SCHOOL_KIND", getString(row, "SCHOOL_KIND"));
                        newRow.put("CURRICULUM_CD", getString(row, "CURRICULUM_CD"));
                    }
                    newRow.put("STUDYREC_SUBCLASSCD", getString(row, "SUBCLASSCD"));
                    newRow.put("SUBCLASSCD", defstr(getString(t2, "SUBCLASSCD2"), getString(row, "SUBCLASSCD")));

                    if (sclm.isZenkiKamoku(year)) {
                        newRow.put("ZENKI", "1");
                    }

                    String classname = null;
                    if ("1".equals(schoolcd)) {
                        classname = defstr(anotherClassMst._classordername1, anotherClassMst._classname);
                    }
                    classname = defstr(classname, getString(t3, "CLASSNAME"), getString(t2, "CLASSORDERNAME1"), getString(t2, "CLASSNAME"), getString(row, "CLASSNAME"), clm._classordername1, clm._classname);
                    newRow.put("CLASSNAME", classname);

                    String subclassname = null;
                    if ("1".equals(schoolcd)) {
                        subclassname = defstr(anotherSubclassMst._subclassordername1, anotherSubclassMst._subclassname);
                    }
                    subclassname = defstr(subclassname, getString(t3, "SUBCLASSNAME"), getString(t2, "SUBCLASSORDERNAME1"), getString(t2, "SUBCLASSNAME"), getString(row, "SUBCLASSNAME"), sclm._subclassordername1, sclm._subclassname);
                    newRow.put("SUBCLASSNAME", subclassname);

                    Integer showorderclass = null;
                    if ("1".equals(schoolcd)) {
                        showorderclass = anotherClassMst._showorder2;
                    }
                    showorderclass = Util.defObject(showorderclass, KnjDbUtils.getInt(t2, "SHOWORDERCLASS", null), clm._showorder2, new Integer(999));
                    newRow.put("SHOWORDERCLASS", showorderclass.toString());

                    Integer showordersubclass = null;
                    if ("1".equals(schoolcd)) {
                        showordersubclass = anotherSubclassMst._showorder2;
                    }
                    showordersubclass = Util.defObject(showordersubclass, KnjDbUtils.getInt(t2, "SHOWORDERSUBCLASS", null), sclm._showorder2, new Integer(999));
                    newRow.put("SHOWORDERSUBCLASS", showordersubclass.toString());

                    final String specialdiv;
                    if ("1".equals(schoolcd)) {
                        specialdiv = defstr(anotherClassMst._specialDiv, clm._specialDiv, "0");
                    } else {
                        specialdiv = defstr(clm._specialDiv, "0");
                    }
                    newRow.put("SPECIALDIV", specialdiv);

                    newRow.put("CREDIT_MST_CREDIT", getString(row, "CREDIT_MST_CREDIT"));
                    if ("1".equals(printData.property(Property.useProvFlg))) {
                        newRow.put("PROV_FLG", getString(prv, "PROV_FLG"));
                    } else {
                        newRow.put("PROV_FLG", null);
                    }
                    if ("1".equals(printData.property(Property.useProvFlg)) && param._tableInfo._hasSTUDYREC_PROV_FLG_DAT_PROV_SEMESTER) {
                        newRow.put("PROV_SEMESTER", getString(prv, "PROV_SEMESTER"));
                    } else {
                        newRow.put("PROV_SEMESTER", null);
                    }
                    newRow.put("VALID_FLG", getString(row, "VALID_FLG"));
                    newRow.put("D065FLG", getString(row, "D065FLG"));

                    resultList.add(newRow);
                }

                final Rows rows = new Rows(Arrays.asList("RECORD_FLG", "SCHREGNO", "YEAR", "ANNUAL", "SCHOOLCD", "GRADES", "CREDIT", "COMP_CREDIT", "GET_CREDIT", "ADD_CREDIT", "CLASSCD", "STUDYREC_SUBCLASSCD", "SUBCLASSCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "SPECIALDIV", "CREDIT_MST_CREDIT", "PROV_FLG", "PROV_SEMESTER", "VALID_FLG", "D065FLG", "CLASSNAME", "SUBCLASSNAME"), resultList);
                  if (param._isOutputDebugSeiseki) {
                      log.info(Util.debugCollectionToStr(" getStudyrec = ", rows._recordList, newLine));
                  }
                return rows;
            }

            // :LHR
            public static Rows getLhr(final CommonPrintData printData, final Param param, final Rows tStudyrec) {
                // LHR
                final String cd94name = PrintClass.cd94name(printData);
                if (null == cd94name) {
                    return new Rows(new ArrayList(), new ArrayList());
                }
                final boolean sqlIsNotNendogoto = !printData._hanasuClass1._sqlCreditIsNendogoto;

                  final Map groupLhr = new HashMap();
                final List<String> groupByField;
                if (sqlIsNotNendogoto) {
                    if (CommonPrintData._shusyokuYou == printData._output) {
                        groupByField = Arrays.asList("YEAR");
                    } else {
                        groupByField = Arrays.asList();
                    }
                } else if (param._z010.in(Z010Info.Nishiyama)) {
                    groupByField = Arrays.asList("YEAR");
                } else {
                    groupByField = Arrays.asList();
                }

                for (final Map<String, String> row : tStudyrec._recordList) {

                    boolean isTarget = PrintClass.isHanasu(printData._hanasuClass1, getString(row, "CLASSCD"));
                    if (!isTarget) {
                        continue;
                    }

                    final String valueKey = Listp.getMappedMapKey(groupLhr, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(groupLhr, valueKey), "ROW_LIST").add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                for (final Iterator it = groupLhr.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final List list = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("RECORD_FLG", null);
                    if (sqlIsNotNendogoto) {
                        if (CommonPrintData._shusyokuYou == printData._output) {
                            record.put("YEAR", getString(group, "YEAR"));
                        } else {
                            record.put("YEAR", "0");
                        }
                    } else if (param._z010.in(Z010Info.Nishiyama)) {
                        record.put("YEAR", getString(group, "YEAR"));
                    }
                    record.put("SPECIALDIV", null);
                    record.put("CLASSCD", cd94name);
                    record.put("CLASSNAME", cd94name);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", max(KnjDbUtils.getColumnDataList(list, "SCHOOL_KIND")));
                        record.put("CURRICULUM_CD", max(KnjDbUtils.getColumnDataList(list, "CURRICULUM_CD")));
                    }
                    record.put("SUBCLASSCD", max(KnjDbUtils.getColumnDataList(list, "SUBCLASSCD")));
                    record.put("SUBCLASSNAME", max(KnjDbUtils.getColumnDataList(list, "SUBCLASSNAME")));

                    record.put("GRADES", "0");
                    record.put("AVG_GRADES", "0");
                    record.put("GRADE_CREDIT", "0");
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                    if (sqlIsNotNendogoto) {
                        record.put("COMP_CREDIT", null);
                    } else if (param._z010.in(Z010Info.Nishiyama)) {
                        record.put("COMP_CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "COMP_CREDIT")))));
                    }
                    record.put("VALID_FLG", defstr(max(KnjDbUtils.getColumnDataList(list, "VALID_FLG")), "0"));
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", max(KnjDbUtils.getColumnDataList(list, "PROV_FLG")));
                    record.put("PROV_SEMESTER", min(KnjDbUtils.getColumnDataList(list, "PROV_SEMESTER")));
                    record.put("D065FLG", null);
                    resultList.add(record);
                }

                final Rows rowsLHR = new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
                  if (param._isOutputDebugSeiseki) {
                      log.info(Util.debugCollectionToStr(" getLhr = ", rowsLHR._recordList, newLine));
                  }
                return rowsLHR;
            }

            // :MAIN
            public static Rows getMain(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {

                // :GROUP_BY_YEAR_SUBCLASS
                final Rows groupByYearSubclass = Listp.groupByYearSubclass(printData, param, sqlStudyrec, tStudyrec);
                // :GROUP_BY_SUBCLASS
                final Rows groupBySubclassSumCredit = Listp.groupBySubclassSumCredit(printData, param, sqlStudyrec, tStudyrec);
                // :HYOTEI_HEIKIN_GROUP_BY_CLASS
                final Rows hyoteiHeikinGroupByClass = Listp.getHyoteiHeikinGroupByClass(printData, param, sqlStudyrec, tStudyrec);

                final Rows main = Listp.joinToMain(printData, param, sqlStudyrec, groupByYearSubclass, groupBySubclassSumCredit, hyoteiHeikinGroupByClass);

                return main;
            }

            // :ZENSEKI
            public static Rows getZenseki(final CommonSqlStudyrec sqlStudyrec, final Param param, final List<Map<String, String>> studyrecDatList) {

                final List<Map<String, String>> filteredList = new ArrayList<Map<String, String>>();
                  for (final Map<String, String> row : studyrecDatList) {

                      final String schoolcd = getString(row, "SCHOOLCD");
                    final String year = getString(row, "YEAR");
                    final String subclasscd = getString(row, "SUBCLASSCD");
                    final boolean isZenseki = ("1".equals(schoolcd) || "0".equals(year)) && sqlStudyrec._zensekiSubclassCd.equals(subclasscd);

                      if (!isZenseki) {
                          continue;
                      }

                      filteredList.add(row);
                  }

                  final List<Map<String, String>> recordList = new ArrayList<Map<String, String>>();
                  if (!filteredList.isEmpty()) {
                      final List list = filteredList;

                      final Map<String, String> record = new HashMap<String, String>();

                      record.put("RECORD_FLG", null);
                      record.put("YEAR", "0");
                      record.put("SPECIALDIV", null);
                      record.put("CLASSCD", zenseki);
                      record.put("CLASSNAME", zenseki);
                      if ("1".equals(param._useCurriculumcd)) {
                          record.put("CURRICULUM_CD", zenseki);
                          record.put("SCHOOL_KIND", zenseki);
                      }
                      record.put("SUBCLASSCD", zenseki);
                      record.put("SUBCLASSNAME", zenseki);

                      record.put("GRADES", "0");
                      record.put("AVG_GRADES", "0");
                      record.put("GRADE_CREDIT", "0");
                      record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                      record.put("COMP_CREDIT", null);
                      record.put("VALID_FLG", "0");
                      record.put("SCHOOLCD", "1");
                      record.put("SHOWORDERCLASS", "0");
                      record.put("SHOWORDERSUBCLASS", "0");
                      record.put("PROV_FLG", null);
                      record.put("PROV_SEMESTER", null);
                      record.put("D065FLG", max(KnjDbUtils.getColumnDataList(list, "D065FLG")));
                      recordList.add(record);
                      if (param._isOutputDebug) {
                          log.info(" zenseki = " + sqlStudyrec._zensekiSubclassCd + ", recordList = " + recordList);
                      }
                  }
                  return new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), recordList);
            }

            // :E014
            public static Rows getE014(final CommonPrintData printData, final Param param, final Rows tStudyrec, final List<String> dropYears) {

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

                final Map groupE014 = new HashMap();
                final List<String> groupByField = Arrays.asList("YEAR", "SUBCLASSCD");

                  for (final Map<String, String> row : tStudyrec._recordList) {
                      final String subclasscd = getString(row, "SUBCLASSCD");

                    if (!param.getE014Name1List(printData._notUseE014).contains(subclasscd)) {
                        continue;
                    }
                      if (dropYears.contains(getString(row, "YEAR"))) {
                          continue;
                      }

                    final String valueKey = Listp.getMappedMapKey(groupE014, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(groupE014, valueKey), "ROW_LIST").add(row);
                  }

                  if (param._isOutputDebug && !groupE014.isEmpty()) {
                      log.info(" e014 = " + groupE014);
                  }

                for (final Iterator it = groupE014.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final List list = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("RECORD_FLG", null);
                    record.put("YEAR", getString(group, "YEAR"));
                    record.put("SPECIALDIV", null);
                    record.put("CLASSCD", getString(group, "SUBCLASSCD"));
                    record.put("CLASSNAME", e014);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("CURRICULUM_CD", e014);
                        record.put("SCHOOL_KIND", e014);
                    }
                    record.put("SUBCLASSCD", getString(group, "SUBCLASSCD"));
                    record.put("SUBCLASSNAME", defstr(max(KnjDbUtils.getColumnDataList(list, "SUBCLASSNAME")), "0"));

                    record.put("GRADES", "0");
                    record.put("AVG_GRADES", "0");
                    record.put("GRADE_CREDIT", "0");
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                    record.put("COMP_CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "COMP_CREDIT")))));
                    record.put("VALID_FLG", "0");
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", max(KnjDbUtils.getColumnDataList(list, "PROV_FLG")));
                    record.put("PROV_SEMESTER", min(KnjDbUtils.getColumnDataList(list, "PROV_SEMESTER")));
                    record.put("D065FLG", max(KnjDbUtils.getColumnDataList(list, "D065FLG")));
                    resultList.add(record);
                }
                return new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
            }

            // :Daiken
            public static Rows getDaiken(final Param param, final List<Map<String, String>> studyrecDatList, final List<String> dropYears) {

                final List<Map<String, String>> filteredList = new ArrayList<Map<String, String>>();
                  for (final Map<String, String> row : studyrecDatList) {

                      if (!"2".equals(getString(row, "SCHOOLCD"))) {
                          continue;
                      }
                      if (dropYears.contains(getString(row, "YEAR"))) {
                          continue;
                      }

                      filteredList.add(row);
                  }

                  final List<Map<String, String>> rtn = new ArrayList<Map<String, String>>();
                  if (!filteredList.isEmpty()) {
                      final List<Map<String, String>> list = filteredList;

                      final Map<String, String> record = new HashMap<String, String>();

                      record.put("RECORD_FLG", null);
                      record.put("YEAR", "0");
                      record.put("SPECIALDIV", null);
                      record.put("CLASSCD", daiken);
                      record.put("CLASSNAME", daiken);
                      if ("1".equals(param._useCurriculumcd)) {
                          record.put("CURRICULUM_CD", daiken);
                          record.put("SCHOOL_KIND", daiken);
                      }
                      record.put("SUBCLASSCD", daiken);
                      record.put("SUBCLASSNAME", daiken);

                      record.put("GRADES", "0");
                      record.put("AVG_GRADES", "0");
                      record.put("GRADE_CREDIT", "0");
                      record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                      record.put("COMP_CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "COMP_CREDIT")))));
                      record.put("VALID_FLG", "0");
                      record.put("SCHOOLCD", "2");
                      record.put("SHOWORDERCLASS", "0");
                      record.put("SHOWORDERSUBCLASS", "0");
                      record.put("PROV_FLG", null);
                      record.put("PROV_SEMESTER", null);
                      record.put("D065FLG", max(KnjDbUtils.getColumnDataList(list, "D065FLG")));
                      rtn.add(record);
                  }
                  return new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), rtn);
            }

            // :HIROKOKU94
            public static Rows getHirokoku94(final CommonPrintData printData, final Param param, final Rows tStudyrec) {
                // ＬＨＲにおける認定単位（レコードがある場合のみ）

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                final Map<String, Map<String, String>> group94 = new HashMap();
                final Map<String, List<Map<String, String>>> rowListMap = new HashMap();
                final List<String> groupByField;
                if (CommonPrintData._shingakuYou == printData._output) {
                    groupByField = Arrays.asList();
                } else {
                    groupByField = Arrays.asList("YEAR");
                }
                for (final Map<String, String> row : tStudyrec._recordList) {

                    if (!(_94.equals(getString(row, "CLASSCD")) && "0".equals(getString(row, "SCHOOLCD")))) {
                        continue;
                    }

                    final String valueKey = Listp.getMappedMapKey(group94, groupByField, row);
                    Util.getMappedList(rowListMap, valueKey).add(row);
                }

                log.info(" group94 = " + group94);

                for (final Map.Entry<String, Map<String, String>> e : group94.entrySet()) {
                    final String valueKey = (String) e.getKey();
                    final Map<String, String> group = e.getValue();
                    final List<Map<String, String>> list = Util.getMappedList(rowListMap, valueKey);

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("RECORD_FLG", null);
                    if (CommonPrintData._shingakuYou == printData._output) {
                        record.put("YEAR", "0");
                    } else {
                        record.put("YEAR", getString(group, "YEAR"));
                    }
                    record.put("SPECIALDIV", null);
                    record.put("CLASSCD", _94);
                    record.put("CLASSNAME", hirokokulhr);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("CURRICULUM_CD", hirokokulhr);
                        record.put("SCHOOL_KIND", hirokokulhr);
                    }
                    record.put("SUBCLASSCD", _94 + "01");
                    record.put("SUBCLASSNAME", hirokokulhr);

                    record.put("GRADES", "0");
                    record.put("AVG_GRADES", "0");
                    record.put("GRADE_CREDIT", "0");
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                    record.put("COMP_CREDIT", null);
                    record.put("VALID_FLG", defstr(max(KnjDbUtils.getColumnDataList(list, "VALID_FLG")), "0"));
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", max(KnjDbUtils.getColumnDataList(list, "PROV_FLG")));
                    record.put("PROV_SEMESTER", min(KnjDbUtils.getColumnDataList(list, "PROV_SEMESTER")));
                    record.put("D065FLG", max(KnjDbUtils.getColumnDataList(list, "D065FLG")));
                    resultList.add(record);
                }

                return new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
            }

            // :SOGO
            public static Rows get90(final CommonPrintData printData, final Param param, final Rows tStudyrec) {

                final Map group90 = new HashMap();
                final List<String> groupByField = Arrays.asList("YEAR");
                for (final Map<String, String> row : tStudyrec._recordList) {

                    if (param.getE014Name1List(printData._notUseE014).contains(getString(row, "SUBCLASSCD"))) {
                        continue;
                    }

                    if (!KNJDefineSchool.subject_T.equals(getString(row, "CLASSCD"))) {
                        continue;
                    }

                    final String valueKey = Listp.getMappedMapKey(group90, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(group90, valueKey), "ROW_LIST").add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                for (final Iterator it = group90.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final List list = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("RECORD_FLG", null);
                    record.put("YEAR", getString(group, "YEAR"));
                    record.put("SPECIALDIV", max(KnjDbUtils.getColumnDataList(list, "SPECIALDIV")));
                    record.put("CLASSCD", KNJDefineSchool.subject_T);
                    record.put("CLASSNAME", sogo);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("CURRICULUM_CD", KNJDefineSchool.subject_T);
                        record.put("SCHOOL_KIND", KNJDefineSchool.subject_T);
                    }
                    record.put("SUBCLASSCD", KNJDefineSchool.subject_T + "01");
                    record.put("SUBCLASSNAME", sogo);

                    record.put("GRADES", "0");
                    record.put("AVG_GRADES", "0");
                    record.put("GRADE_CREDIT", "0");
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                    record.put("COMP_CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "COMP_CREDIT")))));
                    record.put("VALID_FLG", defstr(max(KnjDbUtils.getColumnDataList(list, "VALID_FLG")), "0"));
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", max(KnjDbUtils.getColumnDataList(list, "PROV_FLG")));
                    record.put("PROV_SEMESTER", min(KnjDbUtils.getColumnDataList(list, "PROV_SEMESTER")));
                    record.put("D065FLG", max(KnjDbUtils.getColumnDataList(list, "D065FLG")));
                    resultList.add(record);
                }
                //log.info(Util.debugCollectionToStr(" row90 = ", row90._recordList, newLine));
                return new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
            }

            // :ABROAD
            public static Rows getAbroad(final DB2UDB db2, final CommonSqlStudyrec sqlStudyrec, final CommonPrintData printData, final Param param) {

                final StringBuffer sql = new StringBuffer();
                sql.append("  SELECT ");
                if (CommonPrintData._shingakuYou == printData._output) {
                    sql.append("   '0' AS YEAR");
                } else {
                    sql.append(" YEAR ");
                }
                sql.append("            , SUM(ABROAD_CREDITS) AS CREDIT ");
                sql.append("         FROM(");
                sql.append("              SELECT ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
                sql.append("              FROM " + sqlStudyrec._TableName_Transfer + " ");
                sql.append("              WHERE SCHREGNO = '" + printData._schregno + "' AND TRANSFERCD = '1' ");
                sql.append("         )ST1");
                if (CommonPrintData._shingakuYou == printData._output) {
                    sql.append("     WHERE TRANSFER_YEAR <= " + printData._year + " ");
                } else {
                    sql.append("   , (");
                    sql.append("     SELECT YEAR ");
                    sql.append("         FROM " + sqlStudyrec._TableName_Schreg_Regd + " ");
                    sql.append("         WHERE SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' ");
                    sql.append("     GROUP BY YEAR ");
                    sql.append("     )ST2 ");
                    sql.append("     WHERE ST1.TRANSFER_YEAR <= " + printData._year + " AND INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                    sql.append(" GROUP BY YEAR ");
                }


                final Map abroads = new HashMap();
                final List<String> groupByField = Arrays.asList("YEAR");

                for (final Map<String, String> row : KnjDbUtils.query(db2, sql.toString())) {

                    final String valueKey = Listp.getMappedMapKey(abroads, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(abroads, valueKey), "ROW_LIST").add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                for (final Iterator it = abroads.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final List list = Util.getMappedList(group, "ROW_LIST");

                    final Map record = new HashMap();

                    record.put("SPECIALDIV", null);
                    record.put("RECORD_FLG", null);
                    record.put("YEAR", getString(group, "YEAR"));
                    record.put("CLASSCD", abroad);
                    record.put("CLASSNAME", abroad);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("CURRICULUM_CD", abroad);
                        record.put("SCHOOL_KIND", abroad);
                    }
                    record.put("SUBCLASSCD", abroad);
                    record.put("SUBCLASSNAME", abroad);

                    record.put("GRADES", "0");
                    record.put("AVG_GRADES", "0");
                    record.put("GRADE_CREDIT", "0");
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT")))));
                    record.put("COMP_CREDIT", null);
                    record.put("VALID_FLG", "0");
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", null);
                    record.put("PROV_SEMESTER", null);
                    record.put("D065FLG", null);
                    resultList.add(record);
                }
                final Rows rowAbroad = new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
                //log.info(" rowAbroad = " + rowAbroad._recordList);
                return rowAbroad;
            }

            // :TOTAL_HYOTEI_HEIKIN
            public static Rows getTotalHyoteiHeikin(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {

                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "YEAR");
                } else {
                    groupByField = Arrays.asList("CLASSCD",                                 "SUBCLASSCD", "YEAR");
                }

                final Map<String, Map<String, String>> groupHyoteiHeikin = new HashMap();
                final Map<String, List<Map<String, String>>> groupValueRowListMap = new HashMap<String, List<Map<String, String>>>();
                for (final Map<String, String> row : tStudyrec._recordList) {

                    final String grades = getString(row, "GRADES");
                    if (null == grades || NumberUtils.isNumber(grades) && 0 == Double.parseDouble(grades)) {
                        continue;
                    }
                    if (null != getString(row, "D065FLG")) {
                        continue;
                    }
                    final String classcd = getString(row, "CLASSCD");
                    final String subclasscd = getString(row, "SUBCLASSCD");

                    if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                        if (printData._subclassDetailDatSeq006SubclasscdList.contains(classcd + "-" + getString(row, "SCHOOL_KIND") + "-" + getString(row, "CURRICULUM_CD") + "-" + subclasscd)) {
                            continue;
                        }
                    } else {
                        if (param._d020Name1List.contains(subclasscd)) {
                            continue;
                        }
                    }

                    if (printData.isGakunensei(param)) {
                        if (printData._dropYears.contains(getString(row, "YEAR"))) {
                            continue;
                        }
                    }

                    if (!CommonSqlStudyrec.RECORD_FLG_00_STUDYREC.equals(getString(row, "RECORD_FLG"))) {
                        continue;
                    }

                    if (param.getE014Name1List(printData._notUseE014).contains(subclasscd)) {
                        continue;
                    }

                    if (param._z010.in(Z010Info.Jisyuukan)) {
                        if (!(KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd) || "941001".equals(subclasscd))) {
                            continue;
                        }
                    } else if (param._z010.in(Z010Info.Hosei)) {
                        if (!(KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= "86".compareTo(classcd))) {
                            continue;
                        }
                    } else {
                        if (!(KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd))) {
                            continue;
                        }
                    }

                    final String valueKey = valueKey(row, groupByField);

                    Listp.getMappedMapKey2(groupHyoteiHeikin, groupByField, row, valueKey);

                    Util.getMappedList(groupValueRowListMap, valueKey).add(row);
                }

                final List<Map<String, String>> resultList0 = new ArrayList<Map<String, String>>();
                for (final Map.Entry<String, Map<String, String>> e : groupHyoteiHeikin.entrySet()) {
                    final Map<String, String> group = e.getValue();
                    final String valueKey = valueKey(group, groupByField);
                    final List<Map<String, String>> rowList = Util.getMappedList(groupValueRowListMap, valueKey);

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("CLASSCD", getString(group, "CLASSCD"));
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", getString(group, "SCHOOL_KIND"));
                        record.put("CURRICULUM_CD", getString(group, "CURRICULUM_CD"));
                    }
                    record.put("SUBCLASSCD", getString(group, "SUBCLASSCD"));
                    record.put("YEAR", getString(group, "YEAR"));
                    record.put("PROV_FLG", min(KnjDbUtils.getColumnDataList(rowList, "PROV_FLG")));

                    final String gvalCalc = param._schoolMstYearGvalCalcMap.get(getString(group, "YEAR"));

                    if (printData._isHankiNinteiForm) {
                        final List<Map<String, String>> zenkiList = new ArrayList<Map<String, String>>();
                        for (final Iterator<Map<String, String>> rit = rowList.iterator(); rit.hasNext();) {
                            final Map<String, String> zrow = rit.next();
                            if ("1".equals(zrow.get("ZENKI"))) {
                                zenkiList.add(zrow);
                                rit.remove();
                            }
                        }

                        if (!zenkiList.isEmpty()) {
                            final Map record1 = new HashMap(record);
                            record1.put("CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, zenkiList, "GRADES", "CREDIT")))));
                            record1.put("GRADES", toString(getGrades("total-zenki " + valueKey, param, printData, zenkiList, gvalCalc, "GRADES", false)));
                            resultList0.add(record1);
                        }

                        record.put("CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, rowList, "GRADES", "CREDIT")))));
                        record.put("GRADES", toString(getGrades("total-kouki " + valueKey, param, printData, rowList, gvalCalc, "GRADES", false)));

                        resultList0.add(record);

                    } else {
                        final BigDecimal grades = getGrades("total " + valueKey, param, printData, rowList, gvalCalc, "GRADES", false);

                        record.put("GRADES", toString(grades));

                        // ※pre_sql2のみ
                        record.put("CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, rowList, "GRADES", "CREDIT")))));

                        resultList0.add(record);
                    }
                }
                //log.info(Util.debugCollectionToStr(" 評定平均 resultList0 (" + resultList0.size() + ") = ", resultList0, ","));

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                if (!resultList0.isEmpty()) {

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("RECORD_FLG", null);
                    record.put("YEAR", "0");
                    record.put("SPECIALDIV", null);
                    record.put("CLASSCD", total);
                    record.put("CLASSNAME", total);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", total);
                        record.put("CURRICULUM_CD", total);
                    }
                    record.put("SUBCLASSCD", total);
                    record.put("SUBCLASSNAME", total);
                    record.put("GRADES", null);
                    if (hyoteiHyde.equals(sqlStudyrec._hyoutei)) {           //----->評定の出力有無
                        record.put("AVG_GRADES", "0");
                    } else {
                        final List<Tuple<String, String>> hyoteiCreditStringList = hyoteiCreditStringList(sqlStudyrec._hyoutei, param, printData, resultList0);

                        final String avgGrades;
                        if (printData.paramapContains(Parameter.GVAL_CALC_CHECK) && "2".equals(printData.getParameter(Parameter.GVAL_CALC_CHECK))) {
                            final List<BigDecimal> hyoteiMultiplyCredit = hyoteiMultiplyCredit(hyoteiCreditStringList);
                            final List<String> creditList = creditList(hyoteiCreditStringList);
                            final BigDecimal bunshi = Util.bigDecimalSum(hyoteiMultiplyCredit);
                            final BigDecimal bunbo = Util.bigDecimalSum(asFloats(creditList));
                            avgGrades = toString(Util.divide(bunshi, bunbo, printData._avgGradesScale, BigDecimal.ROUND_DOWN)); // 切り捨て
                            if (param.isOutputDebugSql2("hyoteiHeikin") || param._isOutputDebugCalc) {
                                log.info(" hyoteiheikin (GVAL_CALC_CHECK=2) bunshi = " + bunshi + ", bunbo = " + bunbo + ", round(printAvg, 5) = " + Util.divide(bunshi, bunbo, 5, BigDecimal.ROUND_HALF_UP) + ", printAvg = " + avgGrades + ", hyoteiMultiplyCredit = " + hyoteiMultiplyCredit + ", creditList = " + creditList + ", hyoteiCreditStringList = " + hyoteiCreditStringList);
                            }
                        } else {
                            final List<BigDecimal> hyoteiList = asFloats(hyoteiList(hyoteiCreditStringList));
                            avgGrades = toString(Util.bigDecimalAvg(hyoteiList, printData._avgGradesScale, BigDecimal.ROUND_HALF_UP));
                            if (param.isOutputDebugSql2("hyoteiHeikin") || param._isOutputDebugCalc) {
                                final int count = Util.notNullCount(hyoteiList);
                                final BigDecimal hyoteiGoukei = Util.bigDecimalSum(hyoteiList);
                                final String calcAvg = count == 0 ? null : hyoteiGoukei.divide(new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP).toString();
                                log.info(" hyoteiheikin sum = " + hyoteiGoukei + ", count = " + count + ", round(average, 5) = " + calcAvg + ", printAvg = " + avgGrades + ", " + hyoteiList);
                            }
                        }
                        record.put("AVG_GRADES", avgGrades);
                    }
                    record.put("GRADE_CREDIT", null);
                    record.put("CREDIT", null);
                    record.put("COMP_CREDIT", null);
                    record.put("VALID_FLG", "0");
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", null);
                    record.put("PROV_SEMESTER", null);
                    record.put("D065FLG", null);
                    resultList.add(record);
                }

                final Rows rowHyoteiHeikin = new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
                //log.info(" rowHyoteiHeikin = " + rowHyoteiHeikin._recordList);
                return rowHyoteiHeikin;
            }

            private static List<BigDecimal> hyoteiMultiplyCredit(final List<Tuple<String, String>> hyoteiCreditStringList) {
                final List<BigDecimal> hyoteiList = asFloats(hyoteiList(hyoteiCreditStringList));
                final List<BigDecimal> creditList = asFloats(creditList(hyoteiCreditStringList));
                final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
                if (hyoteiList.size() != creditList.size()) {
                    log.info(" not length matches : " + hyoteiList + ", " + creditList);
                    return rtn;
                }
                for (int i = 0; i < hyoteiList.size(); i++) {
                    final BigDecimal hyotei = hyoteiList.get(i);
                    final BigDecimal credit = creditList.get(i);
                    if (null == hyotei || null == credit) {
                        rtn.add(null);
                    } else {
                        rtn.add(hyotei.multiply(credit));
                    }
                }
                return rtn;
            }

            // :TOTAL_CREDIT
            public static Rows getTotalCredit(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {

                final List<String> _90OverD077 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD077List);
                final List<String> _90OverD081 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD081List);
                final List<String> _90OverE065 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstE065List);

                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "YEAR");
                } else {
                    groupByField = Arrays.asList("CLASSCD",                                 "SUBCLASSCD", "YEAR");
                }

                final Map groupHyoteiHeikin = new HashMap();
                for (final Map<String, String> row : tStudyrec._recordList) {

                    final String classcd = getString(row, "CLASSCD");
                    final String subclasscd = getString(row, "SUBCLASSCD");

                    if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                        if (printData._subclassDetailDatSeq006SubclasscdList.contains(classcd + "-" + getString(row, "SCHOOL_KIND") + "-" + getString(row, "CURRICULUM_CD") + "-" + subclasscd)) {
                            continue;
                        }
                    } else {
                        if (param._d020Name1List.contains(subclasscd)) {
                            continue;
                        }
                    }

                    if (printData.isGakunensei(param)) {
                        if (printData._dropYears.contains(getString(row, "YEAR"))) {
                            if (param.isOutputDebugSql2("totalCredit")) {
                                log.info("単位数 not target : " + row);
                            }
                            continue;
                        }
                    }

                    boolean isTarget;
                    if (param._z010.in(Z010Info.Jisyuukan)) {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd) || "941001".equals(subclasscd);
                    } else if (param._z010.in(Z010Info.Hosei)) {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= "85".compareTo(classcd);
                    } else if (printData._isKyoto && CommonPrintData._shingakuYou == printData._output) {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd) && !PrintClass.isHanasu(printData._hanasuClass1, classcd); // 含めない
                    } else {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd);
                    }
                    isTarget = isTarget || subclasscdListContains(param, _90OverD077, row, classcd, subclasscd);
                    isTarget = isTarget || subclasscdListContains(param, _90OverD081, row, classcd, subclasscd);
                    isTarget = isTarget || subclasscdListContains(param, _90OverE065, row, classcd, subclasscd);
                    if (!isTarget) {
                        continue;
                    }
                    final String valueKey = Listp.valueKey(row, groupByField);
                    Listp.getMappedMapKey2(groupHyoteiHeikin, groupByField, row, valueKey);

                    Util.getMappedList(Util.getMappedMap(groupHyoteiHeikin, valueKey), "ROW_LIST").add(row);
                }

                final List resultList0 = new ArrayList();
                for (final Iterator it = groupHyoteiHeikin.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final List list = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                      record.put("CLASSCD", getString(group, "CLASSCD"));
                      if ("1".equals(param._useCurriculumcd)) {
                          record.put("SCHOOL_KIND", getString(group, "SCHOOL_KIND"));
                          record.put("CURRICULUM_CD", getString(group, "CURRICULUM_CD"));
                      }
                      record.put("SUBCLASSCD", getString(group, "SUBCLASSCD"));
                      record.put("YEAR", getString(group, "YEAR"));

                      record.put("CREDIT", defstr(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, list, "GRADES", "CREDIT"))), (String[]) null));

                      resultList0.add(record);
                }

                if (param.isOutputDebugSql2("totalCredit")) {
                    log.info(Util.debugCollectionToStr("単位数 resultList0 = ", resultList0, ""));
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                if (!resultList0.isEmpty()) {

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("RECORD_FLG", null);
                    record.put("YEAR", "0");
                    record.put("SPECIALDIV", null);
                    record.put("CLASSCD", totalCredit);
                    record.put("CLASSNAME", totalCredit);
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", totalCredit);
                        record.put("CURRICULUM_CD", totalCredit);
                    }
                    record.put("SUBCLASSCD", totalCredit);
                    record.put("SUBCLASSNAME", totalCredit);
                    record.put("GRADES", "0");
                    record.put("AVG_GRADES", "0");
                    record.put("GRADE_CREDIT", "0");
                    record.put("CREDIT", defstr(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(resultList0, "CREDIT"))), (String[]) null));
                    record.put("COMP_CREDIT", null);
                    record.put("VALID_FLG", "0");
                    record.put("SCHOOLCD", "0");
                    record.put("SHOWORDERCLASS", "0");
                    record.put("SHOWORDERSUBCLASS", "0");
                    record.put("PROV_FLG", null);
                    record.put("PROV_SEMESTER", null);
                    record.put("D065FLG", null);
                    resultList.add(record);
                }
                final Rows rowTotalCredit = new Rows(Arrays.asList("RECORD_FLG", "YEAR", "SPECIALDIV", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "AVG_GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), resultList);
                if (param.isOutputDebugSql2("totalCredit")) {
                    log.info(" rowTotalCredit = " + rowTotalCredit._recordList);
                }
                return rowTotalCredit;
            }

            private static void getMappedMapKey2(final Map<String, Map<String, String>> m, final List<String> fields, final Map<String, String> row, final String valueKey) {
                if (null == m.get(valueKey)) {
                    final Map<String, String> nm = new HashMap<String, String>();
                    for (final String field : fields) {
                        nm.put(field, getString(row, field));
                    }
                    m.put(valueKey, nm);
                }
            }

            private static String getMappedMapKey(final Map<String, Map<String, String>> m, final List<String> fields, final Map row) {
                final String valueKey = valueKey(row, fields);
                getMappedMapKey2(m, fields, row, valueKey);
                return valueKey;
            }

            protected static String toString(final Object o) {
                return null == o ? null : o.toString();
            }

            protected static List<BigDecimal> asFloats(final List<String> list) {
                if (null == list) {
                    return null;
                } else if (list.size() == 0) {
                    return new ArrayList<BigDecimal>();
                }
                final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
                for (final String s : list) {
                    if (NumberUtils.isNumber(s)) {
                        rtn.add(new BigDecimal(s));
                    } else {
                        if (null != s) {
                            log.warn("failed: convertfloat: " + s);
                        }
                        rtn.add(null);
                    }
                }
                return rtn;
            }

            protected static <T extends Comparable<T>> T min(final List<T> list) {
                if (null == list || list.size() == 0) {
                    return null;
                }
                T rtn = list.get(0);
                for (int i = 1; i < list.size(); i++) {
                    final T o = list.get(i);
                    if (null != o) {
                        if (null == rtn) {
                            rtn = o;
                        } else {
                            final int c = rtn.compareTo(o);
                            if (c > 0) {
                                rtn = o;
                            }
                        }
                    }
                }
                return rtn;
            }

            protected static <T extends Comparable<T>> T max(final List<T> list) {
                if (null == list || list.size() == 0) {
                    return null;
                }
                T rtn = list.get(0);
                for (int i = 1; i < list.size(); i++) {
                    final T o = list.get(i);
                    if (null != o) {
                        if (null == rtn) {
                            rtn = o;
                        } else {
                            final int c = rtn.compareTo(o);
                            if (c < 0) {
                                rtn = o;
                            }
                        }
                    }
                }
                return rtn;
            }

            protected static String mulIfBothNotNull(final String s1, final String s2) {
                if (!(NumberUtils.isDigits(s1) && NumberUtils.isDigits(s2))) {
                    return null;
                }
                final int si1 = Integer.parseInt(s1);
                final int si2 = Integer.parseInt(s2);
                return String.valueOf(si1 * si2);
            }

            protected static String addIfBothNotNull(final String s1, final String s2) {
                if (!(NumberUtils.isDigits(s1) && NumberUtils.isDigits(s2))) {
                    return null;
                }
                return Util.addDigits(s1, s2);
            }

            protected static List<String> creditMstCreditsString(final String _hyoutei, final Param param, final CommonPrintData printData, final List<Map<String, String>> list, final String gradeField, final String creditField) {
                final List<String> creditList;
                if ("on".equals(_hyoutei)) { //----->評定読み替えのON/OFF  評定１を２と読み替え
                    creditList = new ArrayList();
                    for (final Map<String, String> row : list) {

                        final boolean chkProvFlg;
                        if ("1".equals(printData.property(Property.useProvFlg))) {
                            chkProvFlg = "1".equals(getString(row, "PROV_FLG"));
                        } else if (printData._isKanendoHyoteiYomikae) {
                            chkProvFlg = true;
                        } else {
                            chkProvFlg = defstr(getString(row, "YEAR")).equals(printData._ctrlYear);
                        }
                        final String grades = getString(row, gradeField);
                        final boolean grades1To2 = chkProvFlg && "1".equals(grades) && "0".equals(getString(row, "GET_CREDIT"));
                        final String compCredit = getString(row, "COMP_CREDIT");
                        final String rowGetCredit = grades1To2 ? (null != compCredit && !"0".equals(compCredit) ? compCredit : getString(row, "CREDIT_MST_CREDIT")) : getString(row, "GET_CREDIT");
                        final String rowAddCredit = getString(row, "ADD_CREDIT");
                        if (StringUtils.isBlank(rowAddCredit)) {
                            creditList.add(rowGetCredit);
                        } else if (StringUtils.isBlank(rowGetCredit)) {
                            creditList.add(rowAddCredit);
                        } else {
                            creditList.add(addIfBothNotNull(rowGetCredit, rowAddCredit));
                        }
                    }
                } else {
                    creditList = KnjDbUtils.getColumnDataList(list, creditField);
                }
                return creditList;
            }

            protected static String gradesString(final String _hyoutei, final Param param, final String year, final CommonPrintData printData, final String provFlg, final String rowGrades) {
                final boolean chkProvFlg;
                final String grades;
                if ("on".equals(_hyoutei)) { //----->評定読み替えのON/OFF  評定１を２と読み替え
                    if ("1".equals(printData.property(Property.useProvFlg))) {
                        chkProvFlg = "1".equals(provFlg);
                    } else if (printData._isKanendoHyoteiYomikae) {
                        chkProvFlg = true;
                    } else {
                        chkProvFlg = defstr(year).equals(printData._year);
                    }
                    grades = chkProvFlg && "1".equals(rowGrades) ? "2" : rowGrades;
                } else {
                    grades = rowGrades;
                }
                //log.info(" grades = " + grades);
                return grades;
            }

            protected static List<Tuple<String, String>> hyoteiCreditStringList(final String _hyoutei, final Param param, final CommonPrintData printData, final List<Map<String, String>> list) {
                final List<Tuple<String, String>> gradesCreditsList = new ArrayList<Tuple<String, String>>();
                for (final Map<String, String> row : list) {
                    final String year = getString(row, "YEAR");
                    final String provFlg = getString(row, "PROV_FLG");
                    if (printData._isHankiNinteiForm) {
                        if (row.containsKey("ZENKI_GRADES")) {
                            gradesCreditsList.add(Tuple.of(gradesString(_hyoutei, param, year, printData, provFlg, getString(row, "ZENKI_GRADES")), getString(row, "ZENKI_CREDIT")));
                        }
                        if (row.containsKey("KOUKI_GRADES")) {
                            gradesCreditsList.add(Tuple.of(gradesString(_hyoutei, param, year, printData, provFlg, getString(row, "KOUKI_GRADES")), getString(row, "KOUKI_CREDIT")));
                        } else if (!row.containsKey("ZENKI_GRADES")) {
                            gradesCreditsList.add(Tuple.of(gradesString(_hyoutei, param, year, printData, provFlg, getString(row, "GRADES")), getString(row, "CREDIT")));
                        }
                    } else {
                        gradesCreditsList.add(Tuple.of(gradesString(_hyoutei, param, year, printData, provFlg, getString(row, "GRADES")), getString(row, "CREDIT")));
                    }
                }
                //log.info(" gradesCreditsList = " + gradesCreditsList);
                return gradesCreditsList;
            }

            protected static List<Tuple<String, String>> hyoteiCreditCaseList(final CommonPrintData printData, final List<Map<String, String>> list, final boolean checkZenkiKouki) {
                final List<Tuple<String, String>> hyoteiCreditList = new ArrayList<Tuple<String, String>>();

                final int[] lowers = printData._hyoteiKeisanMinGrades > 0 ? new int[] {printData._hyoteiKeisanMinGrades, 0} : new int[] {printData._hyoteiKeisanMinGrades};
                for (int i = 0; i < lowers.length; i++) {
                    final int lower = lowers[i];
                    for (final Map<String, String> row : list) {
                        if (printData._isHankiNinteiForm && checkZenkiKouki) {
                            if (row.containsKey("ZENKI_GRADES") && Util.toInt(getString(row, "ZENKI_GRADES"), 0) > lower) {
                                hyoteiCreditList.add(Tuple.of(getString(row, "ZENKI_GRADES"), getString(row, "ZENKI_CREDIT")));
                            }
                            if (row.containsKey("KOUKI_GRADES") && Util.toInt(getString(row, "KOUKI_GRADES"), 0) > lower) {
                                hyoteiCreditList.add(Tuple.of(getString(row, "KOUKI_GRADES"), getString(row, "KOUKI_CREDIT")));
                            } else if (!row.containsKey("ZENKI_GRADES")) {
                                if (Util.toInt(getString(row, "GRADES"), 0) > lower) {
                                    hyoteiCreditList.add(Tuple.of(getString(row, "GRADES"), getString(row, "CREDIT")));
                                }
                            }
                        } else if (Util.toInt(getString(row, "GRADES"), 0) > lower) {
                            hyoteiCreditList.add(Tuple.of(getString(row, "GRADES"), getString(row, "CREDIT")));
                        }
                    }
                    if (!hyoteiCreditList.isEmpty()) {
                        break;
                    }
                }
                return hyoteiCreditList;
            }

            protected static List<String> hyoteiList(final List<Tuple<String, String>> hyoteiCreditList) {
                final List<String> hyoteiList = new ArrayList<String>();
                for (final Tuple<String, String> hyoteiCredit : hyoteiCreditList) {
                    hyoteiList.add(hyoteiCredit._first);
                }
                return hyoteiList;
            }

            protected static List<String> creditList(final List<Tuple<String, String>> hyoteiCreditList) {
                final List<String> creditList = new ArrayList<String>();
                for (final Tuple<String, String> hyoteiCredit : hyoteiCreditList) {
                    creditList.add(hyoteiCredit._second);
                }
                return creditList;
            }

            protected static List<BigDecimal> gradesMulCreditList(final List<String> gradesCaseString, final List<String> creditCaseString) {
                if (null == gradesCaseString || null == creditCaseString || gradesCaseString.size() != creditCaseString.size()) {
                    log.warn("評定のレコード数と単位数のレコード数が合わない:" + gradesCaseString + " / " + creditCaseString);
                    return null;
                }
                final List<BigDecimal> gradesCase = asFloats(gradesCaseString);
                final List<Integer> creditCase = Util.asInt(creditCaseString);
                final List<BigDecimal> gradesMulCreditList = new ArrayList<BigDecimal>();
                for (int i = 0; i < gradesCase.size(); i++) {
                    final BigDecimal grades = gradesCase.get(i);
                    final Integer credit = creditCase.get(i);
                    if (null == grades || null == credit) {
                        gradesMulCreditList.add(null);
                    } else {
                        gradesMulCreditList.add(grades.multiply(new BigDecimal(credit.intValue())));
                    }
                }
                return gradesMulCreditList;
            }

            protected static String fieldsValueKey(final Map<String, String> row, final List<String> fields) {
                final List<String> accum = new ArrayList<String>();
                for (final String field : fields) {
                    accum.add(field + "=" + getString(row, field));
                }
                return Util.mkString(accum, "|");
            }

            protected static List<Map<String, Map<String, String>>> join(final String joinDiv, final Rows record1, final String name1, final Rows record2, final String name2, final List<String> fields) {
                if (INNER_JOIN.equals(joinDiv) || LEFT_JOIN.equals(joinDiv)) {
                    final Map recordList2Map = new HashMap();
                    for (final Map<String, String> row2 : record2._recordList) {
                        Util.getMappedList(recordList2Map, fieldsValueKey(row2, fields)).add(row2);
                    }
                    final List<Map<String, Map<String, String>>> joinedList = new ArrayList<Map<String, Map<String, String>>>();
                    for (final Map<String, String> row1 : record1._recordList) {
                        final List joinedRow2List = Util.getMappedList(recordList2Map, fieldsValueKey(row1, fields));
                        if (joinedRow2List.isEmpty()) {
                            if (INNER_JOIN.equals(joinDiv)) {
                            } else if (LEFT_JOIN.equals(joinDiv)) {
                                final Map<String, Map<String, String>> newRow = new HashMap<String, Map<String, String>>();
                                joinedList.add(newRow);
                                newRow.put(name1, row1);
                                newRow.put(name2, null);
                            }
                        } else {
                            for (final Iterator itRow2 = joinedRow2List.iterator(); itRow2.hasNext();) {
                                final Map row2 = (Map) itRow2.next();
                                final Map<String, Map<String, String>> newRow = new HashMap<String, Map<String, String>>();
                                joinedList.add(newRow);
                                newRow.put(name1, row1);
                                newRow.put(name2, row2);
                            }
                        }
                    }
                    return joinedList;
                }
                return null;
            }

            protected static Rows joinToMain(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows groupByYearSubclass, final Rows groupBySubclassSumCredit, final Rows hyoteiHeikinGroupByClass) {

                final List<String> _90OverD077 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD077List);
                final List<String> _90OverD081 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD081List);
                final List<String> _90OverE065 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstE065List);

                // groupByYearSubclassLeftJoinGroupBySubclass
                final List<String> joinField;
                if ("1".equals(param._useCurriculumcd)) {
                    joinField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                } else {
                    joinField = Arrays.asList("CLASSCD",                                 "SUBCLASSCD");
                }

                final Rows rtn = new Rows(Arrays.asList("SPECIALDIV", "RECORD_FLG", "YEAR", "CLASSCD", "CLASSNAME", "SUBCLASSCD", "SUBCLASSNAME", "GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG"), new ArrayList<Map<String, String>>());
                for (final Map<String, Map<String, String>> joined : join(INNER_JOIN,
                        groupByYearSubclass, "T1",
                        groupBySubclassSumCredit, "T2", joinField)) {

                    final Map<String, String> row1 = joined.get("T1");
                    final Map<String, String> row2 = joined.get("T2");

                    final String year = getString(row1, "YEAR");
                    final String classcd = getString(row1, "CLASSCD");
                    final String subclasscd = getString(row1, "SUBCLASSCD");
                    boolean isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd);
                    if (param._z010.in(Z010Info.Jisyuukan)) {
                        isTarget = isTarget || "941001".equals(subclasscd);
                    } else if (null != classcd && null != printData._hanasuClass1) {
                        isTarget = isTarget && !classcd.equals(printData._hanasuClass1._classcd);
                    }
                    isTarget = isTarget || subclasscdListContains(param, _90OverD077, row1, classcd, subclasscd);
                    isTarget = isTarget || subclasscdListContains(param, _90OverD081, row1, classcd, subclasscd);
                    isTarget = isTarget || subclasscdListContains(param, _90OverE065, row1, classcd, subclasscd);

                    if (!isTarget) {
                        continue;
                    }

                    final Map<String, String> newRow = new HashMap<String, String>();
                    rtn._recordList.add(newRow);
                    newRow.put("SPECIALDIV", getString(row1, "SPECIALDIV"));
                    newRow.put("RECORD_FLG", getString(row1, "RECORD_FLG"));
                    newRow.put("YEAR", getString(row1, "YEAR"));
                    newRow.put("CLASSCD", classcd);
                    newRow.put("CLASSNAME", getString(row1, "CLASSNAME"));
                    if ("1".equals(param._useCurriculumcd)) {
                        newRow.put("SCHOOL_KIND", getString(row1, "SCHOOL_KIND"));
                        newRow.put("CURRICULUM_CD", getString(row1, "CURRICULUM_CD"));
                    }
                    newRow.put("SUBCLASSCD", subclasscd);
                    newRow.put("SUBCLASSNAME", getString(row1, "SUBCLASSNAME"));
                    if (hyoteiHyde.equals(sqlStudyrec._hyoutei)) {
                        newRow.put("GRADES", "0");
                    } else {
                        newRow.put("GRADES", gradesString(sqlStudyrec._hyoutei, param, year, printData, getString(row1, "PROV_FLG"), getString(row1, "GRADES")));
                        if (row1.containsKey("ZENKI_GRADES")) {
                            newRow.put("ZENKI_GRADES", gradesString(sqlStudyrec._hyoutei, param, year, printData, getString(row1, "PROV_FLG"), getString(row1, "ZENKI_GRADES")));
                        }
                        if (row1.containsKey("KOUKI_GRADES")) {
                            newRow.put("KOUKI_GRADES", gradesString(sqlStudyrec._hyoutei, param, year, printData, getString(row1, "PROV_FLG"), getString(row1, "KOUKI_GRADES")));
                        }
                    }
                    newRow.put("GRADE_CREDIT", getString(row1, "CREDIT")); // 年度or年次の単位数
                    newRow.put("CREDIT", getString(row2, "CREDIT")); // 科目の合計の単位数
                    newRow.put("COMP_CREDIT", getString(row2, "COMP_CREDIT")); // 科目の合計履修単位数
                    newRow.put("VALID_FLG", defstr(getString(row1, "VALID_FLG"), "0"));
                    newRow.put("SCHOOLCD", getString(row1, "SCHOOLCD"));
                    newRow.put("SHOWORDERCLASS", getString(row1, "SHOWORDERCLASS"));
                    newRow.put("SHOWORDERSUBCLASS", getString(row1, "SHOWORDERSUBCLASS"));
                    newRow.put("PROV_FLG", getString(row1, "PROV_FLG"));
                    newRow.put("PROV_SEMESTER", getString(row1, "PROV_SEMESTER"));
                    newRow.put("D065FLG", getString(row1, "D065FLG"));
                }

                // leftJoinHyoteiHeikinGroupByClass
                final List<String> joinField2;
                if ("1".equals(param._useCurriculumcd)) {
                    joinField2 = Arrays.asList("CLASSCD", "SCHOOL_KIND");
                } else {
                    joinField2 = Arrays.asList("CLASSCD");
                }

                final List<Map<String, String>> rtn2 = new ArrayList<Map<String, String>>();
                for (final Map<String, Map<String, String>> joined : join(LEFT_JOIN,
                        rtn, "T1",
                        hyoteiHeikinGroupByClass, "T5", joinField2)) {

                    final Map<String, String> row1 = joined.get("T1");
                    final Map<String, String> row2 = joined.get("T5");

                    final Map<String, String> newRow = new HashMap(row1);
                    rtn2.add(newRow);
                    newRow.put("AVG_GRADES", getString(row2, "AVG_GRADES"));
                }
                return new Rows(Arrays.asList("SPECIALDIV", "RECORD_FLG", "YEAR", "CLASSCD", "SUBCLASSCD", "GRADES", "GRADE_CREDIT", "CREDIT", "COMP_CREDIT", "VALID_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "PROV_FLG", "PROV_SEMESTER", "D065FLG", "AVG_GRADES", "CLASSNAME", "SUBCLASSNAME"), rtn2);
            }

            // :GROUP_BY_YEAR_SUBCLASS
            public static Rows groupByYearSubclass(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {

                final Map groupByYearSubclass = new HashMap();
                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "YEAR");
                } else {
                    groupByField = Arrays.asList("CLASSCD",                                 "SUBCLASSCD", "YEAR");
                }
                for (final Map<String, String> row : tStudyrec._recordList) {

                    if (param.getE014Name1List(printData._notUseE014).contains(getString(row, "SUBCLASSCD"))) {
                        continue;
                    }
                    final String valueKey = Listp.valueKey(row, groupByField);
                    Listp.getMappedMapKey2(groupByYearSubclass, groupByField, row, valueKey);

                    Util.getMappedList(Util.getMappedMap(groupByYearSubclass, valueKey), "ROW_LIST").add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList();
                for (final Iterator it = groupByYearSubclass.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final String debugKey = "year subclass " + valueKey(group, groupByField);
                    final List<Map<String, String>> rowList = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("SPECIALDIV", max(KnjDbUtils.getColumnDataList(rowList, "SPECIALDIV")));
                    record.put("CLASSCD", getString(group, "CLASSCD"));
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", getString(group, "SCHOOL_KIND"));
                        record.put("CURRICULUM_CD", getString(group, "CURRICULUM_CD"));
                    }
                    record.put("SUBCLASSCD", getString(group, "SUBCLASSCD"));
                    record.put("YEAR", getString(group, "YEAR"));
                    record.put("RECORD_FLG", min(KnjDbUtils.getColumnDataList(rowList, "RECORD_FLG")));
                    record.put("CLASSNAME", min(KnjDbUtils.getColumnDataList(rowList, "CLASSNAME")));
                    record.put("SUBCLASSNAME", min(KnjDbUtils.getColumnDataList(rowList, "SUBCLASSNAME")));
                    record.put("PROV_FLG", defstr(min(KnjDbUtils.getColumnDataList(rowList, "PROV_FLG")), "0"));
                    record.put("SCHOOLCD", defstr(min(KnjDbUtils.getColumnDataList(rowList, "SCHOOLCD"))));
                    record.put("SHOWORDERCLASS", toString(min(Util.asInt(KnjDbUtils.getColumnDataList(rowList, "SHOWORDERCLASS")))));
                    record.put("SHOWORDERSUBCLASS", toString(min(Util.asInt(KnjDbUtils.getColumnDataList(rowList, "SHOWORDERSUBCLASS")))));

                    record.put("VALID_FLG", defstr(min(KnjDbUtils.getColumnDataList(rowList, "VALID_FLG")), "0"));
                    record.put("PROV_SEMESTER", min(KnjDbUtils.getColumnDataList(rowList, "PROV_SEMESTER")));
                    record.put("D065FLG", max(KnjDbUtils.getColumnDataList(rowList, "D065FLG")));

                    final String gvalCalc = param._schoolMstYearGvalCalcMap.get(getString(group, "YEAR"));
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, rowList, "GRADES", "CREDIT")))));
                    record.put("GRADES", toString(getGrades(debugKey, param, printData, rowList, gvalCalc, "GRADES", false)));

                    if (printData._isHankiNinteiForm) {
                        final List<Map<String, String>> zenkiList = new ArrayList<Map<String, String>>();
                        for (final Iterator<Map<String, String>> rit = rowList.iterator(); rit.hasNext();) {
                            final Map<String, String> zrow = rit.next();
                            if ("1".equals(zrow.get("ZENKI"))) {
                                zenkiList.add(zrow);
                                rit.remove();
                            }
                        }
                        if (!zenkiList.isEmpty()) {
                            record.put("ZENKI_CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, zenkiList, "GRADES", "CREDIT")))));
                            record.put("ZENKI_GRADES", toString(getGrades(debugKey, param, printData, zenkiList, gvalCalc, "GRADES", false)));
                        }

                        record.put("KOUKI_CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, rowList, "GRADES", "CREDIT")))));
                        record.put("KOUKI_GRADES", toString(getGrades(debugKey, param, printData, rowList, gvalCalc, "GRADES", false)));
                    }

                    resultList.add(record);

                }

                return new Rows(Arrays.asList("SPECIALDIV", "CLASSCD", "SUBCLASSCD", "YEAR", "RECORD_FLG", "CLASSNAME", "SUBCLASSNAME", "PROV_FLG", "SCHOOLCD", "SHOWORDERCLASS", "SHOWORDERSUBCLASS", "VALID_FLG", "PROV_SEMESTER", "D065FLG", "CREDIT", "GRADES"), resultList);
            }

            private static BigDecimal getGrades(final String debugkey, final Param param, final CommonPrintData printData, final List<Map<String, String>> list, final String gvalCalc, final String gradeField, final boolean checkZenkiKouki) {
                final BigDecimal grades;
                if (list.size() == 1) {
                    final List<String> data = KnjDbUtils.getColumnDataList(list, gradeField);
                    grades = max(asFloats(data)); //１レコードの場合、評定はそのままの値。
                } else {
                    String debug = null;
                    if ("0".equals(gvalCalc)) {

                        final List<String> gradesCaseList = hyoteiList(hyoteiCreditCaseList(printData, list, checkZenkiKouki));
                        grades = Util.bigDecimalAvg(asFloats(gradesCaseList), 0, BigDecimal.ROUND_HALF_UP);
                        if (param._isOutputDebugCalc) {
                            debug = "avg grades = " + grades + ", " + gradesCaseList;
                        }

                    } else if ("1".equals(gvalCalc)) {
                        final List<String> gradesCaseList = hyoteiList(hyoteiCreditCaseList(printData, list, checkZenkiKouki));
                        final List<String> creditCaseList = creditList(hyoteiCreditCaseList(printData, list, checkZenkiKouki));
                        final Integer sumCreditCase = Util.integerSum(Util.asInt(creditCaseList));

                        if (null != sumCreditCase && sumCreditCase.intValue() > 0) {
                            final List<BigDecimal> gradesMulCreditList = gradesMulCreditList(gradesCaseList, creditCaseList);
                            final BigDecimal bunshi = Util.bigDecimalSum(gradesMulCreditList);
                            final BigDecimal bunbo = new BigDecimal(sumCreditCase.intValue());
                            grades = Util.divide(bunshi, bunbo, 0, BigDecimal.ROUND_HALF_UP);
                            if (param._isOutputDebugCalc) {
                                debug = "divide grades = " + grades + ", calc = " + bunshi + " / " + bunbo + ", bunshi =　( " + gradesCaseList + " × " + creditCaseList + "), bunbo = " + creditCaseList;
                            }
                        } else {
                            final List<String> data = KnjDbUtils.getColumnDataList(list, gradeField);
                            grades = max(asFloats(data)); // 指定無しの場合、評定はMAX。
                            if (param._isOutputDebugCalc) {
                                debug = "divide but max grades = " + grades + ", data = " + data;
                            }
                        }

                    } else {
                        final List<String> data = KnjDbUtils.getColumnDataList(list, gradeField);
                        grades = max(asFloats(data)); // 指定無しの場合、評定はMAX。
                        if (param._isOutputDebugCalc) {
                            debug = "max = " + grades + ", data = " + data;
                        }
                    }
                    if (param._isOutputDebugCalc) {
                        log.info(" gvalCalc = " + gvalCalc + ": (" + debugkey + ")  " + debug);
                    }
                }
                return grades;
            }

            public static Rows groupBySubclassSumCredit(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {

                final List<String> _90OverD077 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD077List);
                final List<String> _90OverD081 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD081List);
                final List<String> _90OverE065 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstE065List);

                final Map groupBySubclass = new HashMap();
                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
                } else {
                    groupByField = Arrays.asList("CLASSCD",                                 "SUBCLASSCD");
                }
                for (final Map<String, String> row : tStudyrec._recordList) {

                    final String classcd = getString(row, "CLASSCD");
                    final String subclasscd = getString(row, "SUBCLASSCD");
                    if (null == classcd) {
                        continue;
                    }
                    boolean isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd);
                    if (param._z010.in(Z010Info.Jisyuukan)) {
                        isTarget = isTarget || "941001".equals(subclasscd);
                    } else if (null != classcd && null != printData._hanasuClass1) {
                        isTarget = isTarget && !classcd.equals(printData._hanasuClass1._classcd);
                    }
                    isTarget = isTarget || subclasscdListContains(param, _90OverD077, row, classcd, subclasscd);
                    isTarget = isTarget || subclasscdListContains(param, _90OverD081, row, classcd, subclasscd);
                    isTarget = isTarget || subclasscdListContains(param, _90OverE065, row, classcd, subclasscd);
                    if (!isTarget) {
                        continue;
                    }
                    if (printData.isGakunensei(param)) {
                        if (printData._dropYears.contains(getString(row, "YEAR"))) {
                            continue;
                        }
                    }

                    final String valueKey = Listp.getMappedMapKey(groupBySubclass, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(groupBySubclass, valueKey), "ROW_LIST").add(row);
                    // prvGetMappedMapListAddRow(m, fields, row, 0, addRowField);
                }

                final List<Map<String, String>> resultList = new ArrayList();
                for (final Iterator it = groupBySubclass.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();
                    final List list = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("CLASSCD", getString(group, "CLASSCD"));
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", getString(group, "SCHOOL_KIND"));
                        record.put("CURRICULUM_CD", getString(group, "CURRICULUM_CD"));
                    }
                    record.put("SUBCLASSCD", getString(group, "SUBCLASSCD"));

                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, list, "GRADES", "CREDIT")))));
                    record.put("COMP_CREDIT", toString(Util.integerSum(Util.asInt(KnjDbUtils.getColumnDataList(list, "COMP_CREDIT")))));
                    record.put("CREDIT_MST_CREDIT", toString(max(Util.asInt(KnjDbUtils.getColumnDataList(list, "CREDIT_MST_CREDIT")))));

                    resultList.add(record);
                }

                if (param.isOutputDebugSql2(null)) {
                    log.info(Util.debugRecordList("groupBySubclassSumCredit", Arrays.asList("CLASSCD", "SUBCLASSCD", "CREDIT", "COMP_CREDIT", "CREDIT_MST_CREDIT"), resultList));
                }

                return new Rows(Arrays.asList("CLASSCD", "SUBCLASSCD", "CREDIT", "COMP_CREDIT", "CREDIT_MST_CREDIT"), resultList);
            }

            private static boolean subclasscdListContains(final Param param, final List<String> subclasscdList, final Map<String, String> row, final String classcd, final String subclasscd) {
                boolean isTarget = false;
                if (!subclasscdList.isEmpty()) {
                    if ("1".equals(param._useCurriculumcd)) {
                        final String schoolKind = getString(row, "SCHOOL_KIND");
                        final String curriculumCd = getString(row, "CURRICULUM_CD");
                        isTarget = isTarget || subclasscdList.contains(classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd);
                    } else {
                        isTarget = isTarget || subclasscdList.contains(subclasscd);
                    }
                }
                return isTarget;
            }

            public static Rows groupByHyoteiHeikinSubclass(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {
                final Map groupByYearSubclass = new HashMap();
                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "YEAR");
                } else {
                    groupByField = Arrays.asList("CLASSCD",                                 "SUBCLASSCD", "YEAR");
                }
                for (final Map<String, String> row : tStudyrec._recordList) {

                    if (printData.isGakunensei(param)) {
                        if (printData._dropYears.contains(getString(row, "YEAR"))) {
                            continue;
                        }
                    }

                    if (null != getString(row, "D065FLG")) {
                        continue;
                    }

                    if (!CommonSqlStudyrec.RECORD_FLG_00_STUDYREC .equals(getString(row, "RECORD_FLG"))) {
                        continue;
                    }

                    final String valueKey = Listp.getMappedMapKey(groupByYearSubclass, groupByField, row);
                    Util.getMappedList(Util.getMappedMap(groupByYearSubclass, valueKey), "ROW_LIST").add(row);
                }


                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                for (final Iterator it = groupByYearSubclass.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Map group = (Map) e.getValue();

                    final String debugKey = "hyotei heikin " + valueKey(group, groupByField);
                    final List<Map<String, String>> rowList = Util.getMappedList(group, "ROW_LIST");

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("CLASSCD", getString(group, "CLASSCD"));
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", getString(group, "SCHOOL_KIND"));
                        record.put("CURRICULUM_CD", getString(group, "CURRICULUM_CD"));
                    }
                    record.put("SUBCLASSCD", getString(group, "SUBCLASSCD"));
                    record.put("YEAR", getString(group, "YEAR"));
                    final String gvalCalc = printData._param._schoolMstYearGvalCalcMap.get(getString(group, "YEAR"));

                    record.put("GRADES", toString(getGrades(debugKey, param, printData, rowList, gvalCalc, "GRADES", true)));
                    record.put("PROV_FLG", max(KnjDbUtils.getColumnDataList(rowList, "PROV_FLG")));

                    // ※pre_sql2のみ
                    record.put("CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, rowList, "GRADES", "CREDIT")))));

                    if (printData._isHankiNinteiForm) {
                        final List<Map<String, String>> zenkiList = new ArrayList<Map<String, String>>();
                        for (final Iterator<Map<String, String>> rit = rowList.iterator(); rit.hasNext();) {
                            final Map<String, String> zrow = rit.next();
                            if ("1".equals(zrow.get("ZENKI"))) {
                                zenkiList.add(zrow);
                                rit.remove();
                            }
                        }
                        if (!zenkiList.isEmpty()) {
                            record.put("ZENKI_CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, zenkiList, "GRADES", "CREDIT")))));
                            record.put("ZENKI_GRADES", toString(getGrades(debugKey, param, printData, zenkiList, gvalCalc, "GRADES", false)));
                        }

                        record.put("KOUKI_CREDIT", toString(Util.integerSum(Util.asInt(creditMstCreditsString(sqlStudyrec._hyoutei, param, printData, rowList, "GRADES", "CREDIT")))));
                        record.put("KOUKI_GRADES", toString(getGrades(debugKey, param, printData, rowList, gvalCalc, "GRADES", false)));
                    }

                    resultList.add(record);
                }

                return new Rows(Arrays.asList("CLASSCD", "SUBCLASSCD", "YEAR", "GRADES", "PROV_FLG"), resultList);
            }

            public static Rows getHyoteiHeikinGroupByClass(final CommonPrintData printData, final Param param, final CommonSqlStudyrec sqlStudyrec, final Rows tStudyrec) {
                if (hyoteiHyde.equals(sqlStudyrec._hyoutei)) {        //----->評定の出力有無
                    final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
                    return new Rows(new ArrayList<String>(), resultList);
                }

                final Rows i1rows = groupByHyoteiHeikinSubclass(printData, param, sqlStudyrec, tStudyrec);

                //log.info(Util.debugCollectionToStr(" groupByHyoteiHeikinSubclass = ", i1rows._recordList, newLine));

                final Map<String, Map<String, String>> hyoteiHeikinGroupByClass = new HashMap<String, Map<String, String>>();
                final Map<String, List<Map<String, String>>> sameClassRowList = new HashMap<String, List<Map<String, String>>>();
                final List<String> groupByField;
                if ("1".equals(param._useCurriculumcd)) {
                    groupByField = Arrays.asList("CLASSCD", "SCHOOL_KIND");
                } else {
                    groupByField = Arrays.asList("CLASSCD");
                }
                for (final Map<String, String> row : i1rows._recordList) {

                    final String classcd = getString(row, "CLASSCD");
                    final String subclasscd = getString(row, "SUBCLASSCD");
                    if (null == classcd) {
                        continue;
                    }
                    final boolean isTarget;
                    if (param._z010.in(Z010Info.Jisyuukan)) {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd) || "941001".equals(subclasscd);
                    } else if (param._z010.in(Z010Info.Hosei)) {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= "86".compareTo(classcd);
                    } else {
                        isTarget = KNJDefineSchool.subject_D.compareTo(classcd) <= 0 && 0 <= KNJDefineSchool.subject_U.compareTo(classcd);
                    }
                    if (!isTarget) {
                        continue;
                    }

                    if (param.getE014Name1List(printData._notUseE014).contains(subclasscd)) {
                        continue;
                    }

                    if (printData.isGakunensei(param)) {
                        if (printData._dropYears.contains(getString(row, "YEAR"))) {
                            continue;
                        }
                    }

                    final String grades = getString(row, "GRADES");
                    if (null == grades || "0".equals(grades)) {
                        continue;
                    }

                    if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                        if (printData._subclassDetailDatSeq006SubclasscdList.contains(classcd + "-" + getString(row, "SCHOOL_KIND") + "-" + getString(row, "CURRICULUM_CD") + "-" + subclasscd)) {
                            continue;
                        }
                    } else {
                        if (param._d020Name1List.contains(subclasscd)) {
                            continue;
                        }
                    }
                    final String valueKey = Listp.valueKey(row, groupByField);
                    Listp.getMappedMapKey2(hyoteiHeikinGroupByClass, groupByField, row, valueKey);

                    Util.getMappedList(sameClassRowList, valueKey).add(row);
                }

                final List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

                for (final Map.Entry<String, Map<String, String>> e : hyoteiHeikinGroupByClass.entrySet()) {
                    final String valueKey = e.getKey();
                    final Map<String, String> group = e.getValue();
                    final List<Map<String, String>> list = Util.getMappedList(sameClassRowList, valueKey);

                    final Map<String, String> record = new HashMap<String, String>();

                    record.put("CLASSCD", getString(group, "CLASSCD"));
                    if ("1".equals(param._useCurriculumcd)) {
                        record.put("SCHOOL_KIND", getString(group, "SCHOOL_KIND"));
                    }

                    final String classAvgGrades;
                    final List<Tuple<String, String>> hyoteiCreditStringList = hyoteiCreditStringList(sqlStudyrec._hyoutei, param, printData, list);
                    if (printData.paramapContains(Parameter.GVAL_CALC_CHECK) && "2".equals(printData.getParameter(Parameter.GVAL_CALC_CHECK))) {
                        final List<BigDecimal> hyoteiMultiplyCredit = hyoteiMultiplyCredit(hyoteiCreditStringList);
                        final BigDecimal bunshi = Util.bigDecimalSum(hyoteiMultiplyCredit);
                        final List<BigDecimal> creditList = asFloats(creditList(hyoteiCreditStringList));
                        final BigDecimal bunbo = Util.bigDecimalSum(creditList);
                        classAvgGrades = toString(Util.divide(bunshi, bunbo, 1, BigDecimal.ROUND_DOWN));

                        if (param.isOutputDebugSql2("hyoteiHeikin") || param._isOutputDebugCalc) {
                            log.info(" hyoteiheikin (GVAL_CALC_CHECK=2) " + valueKey + " bunshi = " + bunshi + ", bunbo = " + bunbo + ", round(printAvg, 5) = " + Util.divide(bunshi, bunbo, 5, BigDecimal.ROUND_DOWN) + ", classAvgGrades = " + classAvgGrades + ", hyoteiMultiplyCredit = " + hyoteiMultiplyCredit + ", creditList = " + creditList + ", hyoteiCreditStringList = " + hyoteiCreditStringList);
                        }
                    } else {
                        final List<BigDecimal> hyoteiList = asFloats(hyoteiList(hyoteiCreditStringList));
                        classAvgGrades = toString(Util.bigDecimalAvg(hyoteiList, 1, BigDecimal.ROUND_HALF_UP));

                        if (param.isOutputDebugSql2("hyoteiHeikin") || param._isOutputDebugCalc) {
                            final int count = Util.notNullCount(hyoteiList);
                            final BigDecimal hyoteiGoukei = Util.bigDecimalSum(hyoteiList);
                            final String calcAvg = toString(Util.divide(hyoteiGoukei, new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP));
                            log.info(" hyoteiheikin " + valueKey + " sum = " + hyoteiGoukei + ", count = " + count + ", round(average, 5) = " + calcAvg + ", printAvg = " + classAvgGrades + ", " + hyoteiList);
                        }
                    }
                    record.put("AVG_GRADES", classAvgGrades);
                    resultList.add(record);
                }

                return new Rows(Arrays.asList("CLASSCD", "AVG_GRADES"), resultList);
            }
        }

        /**
         * 学習記録データ(全て)の SQL SELECT 文を戻します。
         * @return
         */
        public List<String> pre_sql(final CommonPrintData printData, final Param param) {

            final List<String> stb = new LinkedList<String>();
            // 評定１を２と判定
            final String gradesString;
            final String creditMstCreditsString;
            if ("on".equals(_hyoutei)) { //----->評定読み替えのON/OFF  評定１を２と読み替え
                // 仮評定を使用するなら、仮評定フラグが1のデータのみ
                final String chkProvFlg;
                if ("1".equals(printData.property(Property.useProvFlg))) {
                    chkProvFlg = " PROV_FLG = '1' AND ";
                } else if (printData._isKanendoHyoteiYomikae) {
                    chkProvFlg = "";
                } else {
                    chkProvFlg = " T1.YEAR = '" + printData._year + "' AND ";
                }

                gradesString = "CASE WHEN " + chkProvFlg + " VALUE(T1.GRADES,0) = 1 THEN 2 ELSE T1.GRADES END ";

                // 単位数は
                //  GET_CREDIT が 0の時、
                //  SCHREG_STUDYREC_DAT に登録された COMP_CREDIT（履修単位）の値が0以外は COMP_CREDIT（履修単位）を GET_CREDIT として取得、
                //  0の時は単位マスタの CREDITS を GET_CREDIT として取得して、
                //  ADD_CREDITを加算する。
                final StringBuffer stbCredit = new StringBuffer();
                stbCredit.append(" (CASE WHEN COMP_CREDIT IS NULL AND CREDIT_MST_CREDIT IS NULL AND GET_CREDIT IS NULL AND ADD_CREDIT IS NULL THEN CAST(NULL AS SMALLINT) ");
                stbCredit.append("       WHEN VALUE(ADD_CREDIT, 0) > 0 THEN ADD_CREDIT + VALUE(GET_CREDIT, 0) ");
                stbCredit.append("       ELSE (CASE WHEN " + chkProvFlg + " GRADES = 1 AND GET_CREDIT = 0 THEN ");
                stbCredit.append("              (CASE WHEN COMP_CREDIT <> 0 THEN COMP_CREDIT ELSE CREDIT_MST_CREDIT END) ");
                stbCredit.append("             ELSE GET_CREDIT END ");
                stbCredit.append("             ) + VALUE(ADD_CREDIT,0) ");
                stbCredit.append("  END) ");
                creditMstCreditsString = stbCredit.toString();
            } else{
                gradesString = "T1.GRADES ";
                creditMstCreditsString = "CREDIT ";
            }

            // (DROP_YEAR), MAX_SEMESTER0, STUDYREC_DAT, STUDYREC
            // MAIN_T ... E014以外
            //    各科目: AVG_GRADES使用
            //      科目毎修得単位数計
            //      教科毎評定平均
            //  ++
            //  総合学習の修得単位数 [年度毎] ... 修得単位、履修単位のみ、E014以外
            //  ++
            //  留学中の修得単位数 [年度毎 or 合計] ... 修得単位、履修単位のみ
            // (++
            //  常盤ホームルーム(教科コード94) [年度毎 or 合計] ... 修得単位、履修単位のみ)
            // (++
            //  全体の評定平均値 [合計] ... 評定平均のみ、E014以外、D020以外
            //  ++
            //  全体の修得単位数 [合計] ... 修得単位、履修単位のみ、E014以外、D020以外)
            // (++
            //  前籍校における修得単位 [合計] ... 修得単位、履修単位のみ、SCHOOLCD = '1'のみ)
            // (++
            //  名称マスタE014が設定されている科目の修得単位 [年度毎] ... 修得単位、履修単位のみ、E014のみ)
            // (++
            //  大検における認定単位 [合計] ... 修得単位、履修単位のみ、SCHOOLCD = '2'のみ)
            // (++
            //  ＬＨＲにおける認定単位 広国 (教科コード94) [年度毎 or 合計] ... 修得単位、履修単位のみ)

            // 該当生徒の成績データ表
            stb.addAll(getStudyrecSqlString(printData, param));  // 調査書仕様の学習記録データの抽出

            // :GROUP_BY_YEAR_SUBCLASS
            stb.add(" , GROUP_BY_YEAR_SUBCLASS AS ( ");
            // 同一年度同一科目の場合単位は合計とします。
            //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
            final String gradesCase0 = "case when " + String.valueOf(0) + " < GRADES then GRADES end";
            final String gradesCase = "case when " + String.valueOf(printData._hyoteiKeisanMinGrades) + " < GRADES then GRADES end";
            final String creditCase = "case when " + String.valueOf(printData._hyoteiKeisanMinGrades) + " < GRADES then CREDIT end";
            stb.add("  SELECT ");
            stb.add("         MIN(RECORD_FLG) AS RECORD_FLG ");
            stb.add("       , MAX(SPECIALDIV) AS SPECIALDIV ");
            stb.add("       , CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
                stb.add("   , CURRICULUM_CD ");
            }
            stb.add("    , SUBCLASSCD ");
            stb.add("    , YEAR ");
            stb.add("    , GVAL_CALC");
            stb.add("    , MIN(CLASSNAME) AS CLASSNAME");
            stb.add("    , MIN(SUBCLASSNAME) AS SUBCLASSNAME");
            stb.add("    , case when COUNT(*) = 1 then MAX(GRADES)");//１レコードの場合、評定はそのままの値。
            stb.add("           when GVAL_CALC = '0' then ");
            if (printData._hyoteiKeisanMinGrades != 0) {
                stb.add("           CASE WHEN MAX(GRADES) <= " + String.valueOf(printData._hyoteiKeisanMinGrades) + " THEN MAX(" + gradesCase0 + ") ");
                stb.add("                ELSE ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                stb.add("           END ");
            } else {
                stb.add("           ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
            }
            stb.add("           when GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * CREDIT)) / SUM(" + creditCase + "), 0) ");
            stb.add("           else MAX(GRADES) end AS GRADES ");
            stb.add("    , MIN(VALUE(PROV_FLG, '0')) AS PROV_FLG ");
            stb.add("    , SUM(" + creditMstCreditsString + ") AS CREDIT");
            stb.add("    , MIN(SCHOOLCD) AS SCHOOLCD");
            stb.add("    , MIN(SHOWORDERCLASS) AS SHOWORDERCLASS");
            stb.add("    , MIN(SHOWORDERSUBCLASS) AS SHOWORDERSUBCLASS");
            stb.add("    , MAX(VALUE(T1.VALID_FLG, '0')) AS VALID_FLG ");
            stb.add("    , MIN(T1.PROV_SEMESTER) AS PROV_SEMESTER ");
            stb.add("    , MAX(T1.D065FLG) AS D065FLG ");
            stb.add("      FROM STUDYREC T1 ");
            if (null != printData._e014Subclasscd) {
                stb.add("      WHERE ");
                stb.add("          NOT EXISTS( ");
                stb.add("              SELECT ");
                stb.add("                'x' ");
                stb.add("              FROM ");
                stb.add("                NAME_MST E1 ");
                stb.add("              WHERE ");
                stb.add("                E1.NAMECD1 = 'E014' ");
                stb.add("                AND E1.NAME1 = SUBCLASSCD) ");
            }
            stb.add("  GROUP BY CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
                stb.add("   , CURRICULUM_CD ");
            }
            stb.add("       ,SUBCLASSCD ");
            stb.add("  ,YEAR ");
            stb.add("  , GVAL_CALC");
            stb.add(" ) ");

            //  修得単位数の計
            // :GROUP_BY_SUBCLASS
            stb.add(" , GROUP_BY_SUBCLASS AS ( ");
            stb.add("    SELECT  CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
                stb.add("   , CURRICULUM_CD ");
            }
            stb.add("        , SUBCLASSCD ");
            stb.add("       , MAX(SPECIALDIV) AS SPECIALDIV ");
            stb.add("        , SUM(" + creditMstCreditsString + ") AS CREDIT ");
            stb.add("        , SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
            stb.add("        , MAX(CREDIT_MST_CREDIT) AS CREDIT_MST_CREDIT ");
            stb.add("    FROM STUDYREC T1");
            stb.add("    WHERE ");
            stb.add("    (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            if (param._z010.in(Z010Info.Jisyuukan)) {
                stb.add("    OR SUBCLASSCD = '941001' ");
            }
            stb.add("    )");
            if (printData.isGakunensei(param)) {
                stb.add("   AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
            }
            stb.add("    GROUP BY CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
                stb.add("   , CURRICULUM_CD ");
            }
            stb.add("           ,SUBCLASSCD");
            stb.add(" ) ");


            //  各教科の評定平均値
            // :HYOTEI_HEIKIN_GROUP_BY_CLASS
            stb.add(" , HYOTEI_HEIKIN_GROUP_BY_CLASS AS ( ");
            stb.add("    SELECT  CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
            }
            stb.add("          , DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + gradesString + ")),5,2),1),5,1) AS AVG_GRADES");
            stb.add(" FROM (");
            // 同一年度同一科目の場合評価はどちらか一方とします。
            stb.add("  SELECT ");
            stb.add("       CLASSCD, SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
                stb.add("   , CURRICULUM_CD ");
            }
            stb.add("  , YEAR ");
            stb.add("  , GVAL_CALC");
            stb.add("  , case when COUNT(*) = 1 then MAX(GRADES)"); //１レコードの場合、評定はそのままの値。
            stb.add("         when GVAL_CALC = '0' then ");
            if (printData._hyoteiKeisanMinGrades != 0) {
                stb.add("           CASE WHEN MAX(GRADES) <= " + String.valueOf(printData._hyoteiKeisanMinGrades) + " THEN MAX(" + gradesCase0 + ") ");
                stb.add("                ELSE ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                stb.add("           END ");
            } else {
                stb.add("           ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
            }
            stb.add("         when GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * CREDIT)) / SUM(" + creditCase + "), 0)");
            stb.add("         else MAX(GRADES) ");
            stb.add("    end AS GRADES ");
            stb.add("  , MAX(PROV_FLG) AS PROV_FLG ");
            stb.add("  , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
            stb.add("  FROM STUDYREC T1 ");
            stb.add("  WHERE ");
            stb.add("    RECORD_FLG = '" + CommonSqlStudyrec.RECORD_FLG_00_STUDYREC + "' ");
            if (printData.isGakunensei(param)) {
                stb.add("   AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
            }
            stb.add("    AND T1.D065FLG IS NULL ");
            stb.add("  GROUP BY CLASSCD,SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
                stb.add("   , CURRICULUM_CD ");
            }
            stb.add("  ,YEAR");
            stb.add("  , GVAL_CALC");
            stb.add("     ) T1");
            stb.add("    WHERE   (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND ");
            if (param._z010.in(Z010Info.Jisyuukan)) {
                stb.add("    '" + KNJDefineSchool.subject_U + "' OR SUBCLASSCD = '941001' ");
            } else if (param._z010.in(Z010Info.Hosei)) {
                stb.add("    '86' ");
            } else {
                stb.add("    '" + KNJDefineSchool.subject_U + "'");
            }
            stb.add("    ) ");
            stb.add("        AND GRADES <> 0 ");
            stb.add("        AND NOT EXISTS ( ");
            if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                stb.add("              SELECT ");
                stb.add("                'x' ");
                stb.add("              FROM ");
                stb.add("                SUBCLASS_DETAIL_DAT E1 ");
                stb.add("              WHERE ");
                stb.add("                E1.YEAR = '" + printData._year + "' ");
                stb.add("                AND E1.SUBCLASS_SEQ = '006' ");
                stb.add("                AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = ");
                stb.add("                    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            } else {
                stb.add("              SELECT ");
                stb.add("                'x' ");
                stb.add("              FROM ");
                stb.add("                NAME_MST E1 ");
                stb.add("              WHERE ");
                stb.add("                E1.NAMECD1 = 'D020' ");
                stb.add("                AND E1.NAME1 = T1.SUBCLASSCD ");
            }
            stb.add("          ) ");
            if (null != printData._e014Subclasscd) {
                stb.add("        AND NOT EXISTS ( ");
                stb.add("              SELECT ");
                stb.add("                'x' ");
                stb.add("              FROM ");
                stb.add("                NAME_MST E1 ");
                stb.add("              WHERE ");
                stb.add("                E1.NAMECD1 = 'E014' ");
                stb.add("                AND E1.NAME1 = T1.SUBCLASSCD ");
                stb.add("          ) ");
            }
            stb.add("    GROUP BY CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , SCHOOL_KIND ");
            }
            stb.add(" ) ");

            stb.add(" , MAIN_T AS ( "); // :MAIN
            // 該当生徒の科目評定、修得単位及び教科評定平均
            stb.add("SELECT ");
            stb.add("     RECORD_FLG ");
            stb.add("     , T1.YEAR ");
            stb.add("     , T1.SPECIALDIV ");
            stb.add("     , T1.CLASSCD");
            stb.add("     , T1.CLASSNAME");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   , T1.SCHOOL_KIND ");
                stb.add("   , T1.CURRICULUM_CD ");
            }
            stb.add("     , T1.SUBCLASSCD");
            stb.add("     , T1.SUBCLASSNAME");
            if (hyoteiHyde.equals(_hyoutei)) {
                stb.add(" , 0 AS GRADES");
                stb.add(" , 0 AS AVG_GRADES");
            } else {
                stb.add(" , " +  gradesString + " AS GRADES");
                stb.add(" , T5.AVG_GRADES");
            }
            stb.add("     , T1.CREDIT AS GRADE_CREDIT"); // 年度or年次の単位数
            stb.add("     , T4.CREDIT AS CREDIT "); // 科目の合計の単位数
            stb.add("     , T4.COMP_CREDIT"); // 科目の合計履修単位数
            stb.add("     , VALUE(T1.VALID_FLG, '0') AS VALID_FLG ");
            stb.add("     , T1.SCHOOLCD");
            stb.add("     , T1.SHOWORDERCLASS");
            stb.add("     , T1.SHOWORDERSUBCLASS");
            stb.add("     , T1.PROV_SEMESTER ");
            stb.add("     , T1.D065FLG ");
            stb.add(" FROM GROUP_BY_YEAR_SUBCLASS T1");
            stb.add(" INNER JOIN GROUP_BY_SUBCLASS T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD AND T4.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("   AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("   AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }

            if (hyoteiHyde.equals(_hyoutei)) {        //----->評定の出力有無
            } else {
                //  各教科の評定平均値
                stb.add(" LEFT JOIN HYOTEI_HEIKIN_GROUP_BY_CLASS T5 ON T5.CLASSCD = T1.CLASSCD");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("   AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
            }
            stb.add(" WHERE  (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            if (param._z010.in(Z010Info.Jisyuukan)) {
                stb.add(" OR T1.SUBCLASSCD = '941001'");
            } else if (null != printData._hanasuClass1) {
                stb.add(" AND T1.CLASSCD <> '" + PrintClass.getHanasuClasscd(printData) + "' ");
            }
            stb.add(" ) ");

            // 総合学習の修得単位数 年度毎 留年を含む :SOGO
            stb.add(" UNION ");
            stb.add(" SELECT ");
            stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
            stb.add("      , YEAR ");
            stb.add("      , MAX(T1.SPECIALDIV) AS SPECIALDIV ");
            stb.add("      , '" + KNJDefineSchool.subject_T + "' AS CLASSCD");
            stb.add("      , '" + sogo + "' AS CLASSNAME");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("  , '" + KNJDefineSchool.subject_T + "' AS SCHOOL_KIND");
                stb.add("  , '" + KNJDefineSchool.subject_T + "' AS CURRICULUM_CD");
            }
            stb.add("      , '" + KNJDefineSchool.subject_T + "01' AS SUBCLASSCD");
            stb.add("      , '" + sogo + "' AS SUBCLASSNAME");
            stb.add("      , 0 AS GRADES");
            stb.add("      , 0 AS AVG_GRADES");
            stb.add("      , 0 AS GRADE_CREDIT");
            stb.add("      , SUM(CREDIT) AS CREDIT"); // 年度or年次の単位数
            stb.add("      , SUM(COMP_CREDIT) AS COMP_CREDIT"); // 年度or年次の履修単位数
            stb.add("      , MAX(T1.VALID_FLG) AS VALID_FLG ");
            stb.add("      , '0' AS SCHOOLCD ");
            stb.add("      , 0 AS SHOWORDERCLASS ");
            stb.add("      , 0 AS SHOWORDERSUBCLASS ");
            stb.add("      , MIN(T1.PROV_SEMESTER) AS PROV_SEMESTER ");
            stb.add("      , MAX(T1.D065FLG) AS D065FLG ");
            stb.add(" FROM   STUDYREC T1");
            stb.add(" WHERE  T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            if (null != printData._e014Subclasscd) {
                stb.add(" AND T1.SUBCLASSCD <> '" + printData._e014Subclasscd + "'");
            }
            stb.add(" GROUP BY YEAR ");

            // 留学中の修得単位数（学年別）  :ABROAD
            stb.add("  UNION SELECT ");
            stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
            if (CommonPrintData._shingakuYou == printData._output) {
                stb.add("   , '0' AS YEAR");
            } else {
                stb.add(" , YEAR ");
            }
            stb.add("      , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
            stb.add("            , '" + abroad + "' AS CLASSCD");
            stb.add("            , '" + abroad + "' AS CLASSNAME");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("  , '" + abroad + "' AS SCHOOL_KIND");
                stb.add("  , '" + abroad + "' AS CURRICULUM_CD");
            }
            stb.add("            , '" + abroad + "' AS SUBCLASSCD");
            stb.add("            , '" + abroad + "' AS SUBCLASSNAME");
            stb.add("            , 0 AS GRADES");
            stb.add("            , 0 AS AVG_GRADES");
            stb.add("            , 0 AS GRADE_CREDIT");
            stb.add("            , SUM(ABROAD_CREDITS) AS CREDIT ");
            stb.add("            , CAST(NULL AS SMALLINT) AS COMP_CREDIT ");
            stb.add("            , '0' AS VALID_FLG ");
            stb.add("            , '0' AS SCHOOLCD ");
            stb.add("            , 0 AS SHOWORDERCLASS ");
            stb.add("            , 0 AS SHOWORDERSUBCLASS ");
            stb.add("            , CAST(NULL AS VARCHAR(1)) AS PROV_SEMESTER ");
            stb.add("            , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
            stb.add("         FROM(");
            stb.add("              SELECT ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
            stb.add("              FROM " + _TableName_Transfer + " ");
            stb.add("              WHERE SCHREGNO = '" + printData._schregno + "' AND TRANSFERCD = '1' ");
            stb.add("         )ST1");
            if (CommonPrintData._shingakuYou == printData._output) {
                stb.add("     WHERE TRANSFER_YEAR <= " + printData._year + " ");
            } else {
                stb.add("   , (");
                stb.add("     SELECT YEAR ");
                stb.add("         FROM " + _TableName_Schreg_Regd + " ");
                stb.add("         WHERE SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' ");
                stb.add("     GROUP BY YEAR ");
                stb.add("     )ST2 ");
                stb.add("     WHERE ST1.TRANSFER_YEAR <= " + printData._year + " AND INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                stb.add(" GROUP BY YEAR ");
            }

            if (!param._z010.in(Z010Info.Hirokoku) && PrintClass.is94_(printData) && CommonPrintData._shingakuYou == printData._output) {
                // LHR :LHR
                final boolean sqlIsNotNendogoto = !printData._hanasuClass1._sqlCreditIsNendogoto;
                stb.add(" UNION ");
                stb.add(" SELECT ");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                if (sqlIsNotNendogoto) {
                    if (CommonPrintData._shusyokuYou == printData._output) {
                        stb.add("     , YEAR ");
                    } else {
                        stb.add("     , '0' AS YEAR ");
                    }
                } else if (param._z010.in(Z010Info.Nishiyama)) {
                    stb.add("  , YEAR ");
                }
                stb.add("      , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("      ,'" + PrintClass.cd94name(printData) + "' AS CLASSCD ");
                stb.add("      ,'" + PrintClass.cd94name(printData) + "' AS CLASSNAME ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , MAX(SCHOOL_KIND) AS SCHOOL_KIND ");
                    stb.add("  , MAX(CURRICULUM_CD) AS CURRICULUM_CD ");
                }
                stb.add("      , MAX(SUBCLASSCD) AS SUBCLASSCD ");
                stb.add("      , MAX(SUBCLASSNAME) AS SUBCLASSNAME ");
                stb.add("      , 0 AS GRADES ");
                stb.add("      , 0 AS AVG_GRADES ");
                stb.add("      , 0 AS GRADE_CREDIT ");
                stb.add("      , SUM(T1.CREDIT) AS CREDIT ");
                if (sqlIsNotNendogoto) {
                    stb.add("       , CAST(NULL AS SMALLINT) AS COMP_CREDIT");
                } else if (param._z010.in(Z010Info.Nishiyama)) {
                    stb.add("      , SUM(COMP_CREDIT) AS COMP_CREDIT");
                }
                stb.add("       , MAX(VALUE(T1.VALID_FLG, '0')) AS VALID_FLG ");
                stb.add("       , '0' AS SCHOOLCD ");
                stb.add("       , 0 AS SHOWORDERCLASS ");  // 表示順教科
                stb.add("       , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                stb.add("       , MIN(PROV_SEMESTER) AS PROV_SEMESTER ");
                stb.add("       , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.add(" FROM STUDYREC T1 ");
                stb.add(" WHERE ");
                stb.add("   CLASSCD = '" + PrintClass.getHanasuClasscd(printData) + "' ");
                if (sqlIsNotNendogoto) {
                    if (CommonPrintData._shusyokuYou == printData._output) {
                        stb.add(" GROUP BY T1.YEAR ");
                    }
                } else if (param._z010.in(Z010Info.Nishiyama)) {
                    stb.add(" GROUP BY YEAR");
                }
            }
            if (CommonPrintData._shingakuYou == printData._output) {
                // 全体の評定平均値 :TOTAL_HYOTEI_HEIKIN
                stb.add(" UNION SELECT ");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                stb.add("     , '0' AS YEAR ");
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("     , '" + total + "' AS CLASSCD ");
                stb.add("     , '" + total + "' AS CLASSNAME ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add(" , '" + total + "' AS SCHOOL_KIND ");
                    stb.add(" , '" + total + "' AS CURRICULUM_CD ");
                }
                stb.add("     , '" + total + "' AS SUBCLASSCD ");
                stb.add("     , '" + total + "' AS SUBCLASSNAME ");
                stb.add("     , CAST(NULL AS SMALLINT) AS GRADES ");

                if (hyoteiHyde.equals(_hyoutei)) {           //----->評定の出力有無
                    stb.add("    , 0 AS AVG_GRADES ");
                } else{
                    stb.add("    , DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + gradesString + ")),5,2),1),5,1) AS AVG_GRADES ");
                }
                stb.add("     , CAST(NULL AS SMALLINT) AS GRADE_CREDIT ");
                stb.add("     , CAST(NULL AS SMALLINT) AS CREDIT ");
                stb.add("     , CAST(NULL AS SMALLINT) AS COMP_CREDIT");
                stb.add("     , '0' AS VALID_FLG ");
                stb.add("     , '0' AS SCHOOLCD ");
                stb.add("     , 0 AS SHOWORDERCLASS ");  // 表示順教科
                stb.add("     , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS PROV_SEMESTER ");
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.add(" FROM ( ");
                stb.add("  SELECT ");
                stb.add("      SCHREGNO ");
                stb.add("      , CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , SCHOOL_KIND");
                    stb.add("  , CURRICULUM_CD");
                }
                stb.add("       , SUBCLASSCD ");
                stb.add("  , YEAR ");
                stb.add("           , GVAL_CALC");
                stb.add("           , case when COUNT(*) = 1 then MAX(GRADES)");//１レコードの場合、評定はそのままの値。
                stb.add("                  when GVAL_CALC = '0' then ");
                if (printData._hyoteiKeisanMinGrades != 0) {
                    stb.add("                   CASE WHEN MAX(GRADES) <= " + String.valueOf(printData._hyoteiKeisanMinGrades) + " THEN MAX(" + gradesCase0 + ") ");
                    stb.add("                        ELSE ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                    stb.add("                   END ");
                } else {
                    stb.add("                   ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                }
                stb.add("                  when GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * CREDIT)) / SUM(" + creditCase + "), 0)");
                stb.add("                  else MAX(GRADES) end AS GRADES");
                stb.add("           , MIN(VALUE(PROV_FLG, '0')) AS PROV_FLG ");
                stb.add("      FROM STUDYREC T1 ");
                stb.add("      WHERE ");
                stb.add("          GRADES <> 0 ");
                stb.add("          AND D065FLG IS NULL ");
                stb.add("          AND RECORD_FLG = '" + CommonSqlStudyrec.RECORD_FLG_00_STUDYREC + "' ");
                stb.add("          AND NOT EXISTS ( ");
                if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                    stb.add("              SELECT ");
                    stb.add("                'x' ");
                    stb.add("              FROM ");
                    stb.add("                SUBCLASS_DETAIL_DAT E1 ");
                    stb.add("              WHERE ");
                    stb.add("                E1.YEAR = '" + printData._year + "' ");
                    stb.add("                AND E1.SUBCLASS_SEQ = '006' ");
                    stb.add("                AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = ");
                    stb.add("                    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                } else {
                    stb.add("              SELECT ");
                    stb.add("                'x' ");
                    stb.add("              FROM ");
                    stb.add("                NAME_MST E1 ");
                    stb.add("              WHERE ");
                    stb.add("                E1.NAMECD1 = 'D020' ");
                    stb.add("                AND E1.NAME1 = T1.SUBCLASSCD ");
                }
                stb.add("          ) ");
                if (printData.isGakunensei(param)) {
                    stb.add("           AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                }
                if (null != printData._e014Subclasscd) {
                    stb.add("          AND NOT EXISTS ( ");
                    stb.add("              SELECT ");
                    stb.add("                'x' ");
                    stb.add("              FROM ");
                    stb.add("                NAME_MST E1 ");
                    stb.add("              WHERE ");
                    stb.add("                E1.NAMECD1 = 'E014' ");
                    stb.add("                AND E1.NAME1 = T1.SUBCLASSCD ");
                    stb.add("          ) ");
                }
                stb.add("  GROUP BY SCHREGNO,CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , SCHOOL_KIND");
                    stb.add("  , CURRICULUM_CD");
                }
                stb.add("      , SUBCLASSCD ");
                stb.add("  ,YEAR ");
                stb.add("  , GVAL_CALC");
                stb.add("     ) T1");
                if (param._z010.in(Z010Info.Jisyuukan)) {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBCLASSCD = '941001'");
                } else if (param._z010.in(Z010Info.Hosei)) {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '86'");
                } else {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                }

                // 全体の修得単位数 :TOTAL_CREDIT
                stb.add(" UNION SELECT ");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                stb.add("     , '0' AS YEAR ");
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("     , '" + totalCredit + "' AS CLASSCD ");
                stb.add("     , '" + totalCredit + "' AS CLASSNAME ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add(" , '" + totalCredit + "' AS SCHOOL_KIND ");
                    stb.add(" ,  '" + totalCredit + "' AS CURRICULUM_CD ");
                }
                stb.add("     , '" + totalCredit + "' AS SUBCLASSCD ");
                stb.add("     , '" + totalCredit + "' AS SUBCLASSNAME ");
                stb.add("     , 0 AS GRADES ");
                stb.add("     , 0 AS AVG_GRADES ");
                stb.add("     , 0 AS GRADE_CREDIT ");
                stb.add("     , SUM(T1.CREDIT) AS CREDIT ");
                stb.add("     , CAST(NULL AS SMALLINT) AS COMP_CREDIT");
                stb.add("     , '0' AS VALID_FLG ");
                stb.add("     , '0' AS SCHOOLCD ");
                stb.add("     , 0 AS SHOWORDERCLASS ");  // 表示順教科
                stb.add("     , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS PROV_SEMESTER ");
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.add(" FROM (");
                stb.add("  SELECT SCHREGNO, CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , SCHOOL_KIND");
                    stb.add("  , CURRICULUM_CD");
                }
                stb.add("  , SUBCLASSCD ");
                stb.add("  , YEAR");
                stb.add("   , SUM(" + creditMstCreditsString + ") AS CREDIT");
                stb.add("      FROM STUDYREC T1 ");
                stb.add("      WHERE ");
                stb.add("          NOT EXISTS( ");
                if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                    stb.add("              SELECT ");
                    stb.add("                'x' ");
                    stb.add("              FROM ");
                    stb.add("                SUBCLASS_DETAIL_DAT E1 ");
                    stb.add("              WHERE ");
                    stb.add("                E1.YEAR = '" + printData._year + "' ");
                    stb.add("                AND E1.SUBCLASS_SEQ = '006' ");
                    stb.add("                AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = ");
                    stb.add("                    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                } else {
                    stb.add("              SELECT ");
                    stb.add("                'x' ");
                    stb.add("              FROM ");
                    stb.add("                NAME_MST E1 ");
                    stb.add("              WHERE ");
                    stb.add("                E1.NAMECD1 = 'D020' ");
                    stb.add("                AND E1.NAME1 = T1.SUBCLASSCD ");
                }
                stb.add("          ) ");
                if (null != printData._e014Subclasscd) {
                    stb.add("          AND NOT EXISTS( ");
                    stb.add("              SELECT ");
                    stb.add("                'x' ");
                    stb.add("              FROM ");
                    stb.add("                NAME_MST E1 ");
                    stb.add("              WHERE ");
                    stb.add("                E1.NAMECD1 = 'E014' ");
                    stb.add("                AND E1.NAME1 = T1.SUBCLASSCD ");
                    stb.add("          ) ");
                }
                if (printData.isGakunensei(param)) {
                    stb.add("          AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                }
                stb.add("  GROUP BY SCHREGNO, CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , SCHOOL_KIND");
                    stb.add("  , CURRICULUM_CD");
                }
                stb.add("  ,SUBCLASSCD ");
                stb.add("  ,YEAR ");
                stb.add("     ) T1");
                if (param._z010.in(Z010Info.Jisyuukan)) {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBCLASSCD = '941001'");
                } else if (param._z010.in(Z010Info.Hosei)) {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '85'");
                } else if (printData._isKyoto && CommonPrintData._shingakuYou == printData._output) {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' AND CLASSCD <> '" + PrintClass.getHanasuClasscd(printData) + "' ");
                } else {
                    stb.add(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                }
            }

            // 前籍校における修得単位（レコードがある場合のみ）:ZENSEKI
            if (null != _zensekiSubclassCd) {
                stb.add(" UNION SELECT");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                stb.add("    , '0' AS YEAR");
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("    , '" + zenseki + "' AS CLASSCD");
                stb.add("    , '" + zenseki + "' AS CLASSNAME");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  ,'" + zenseki + "' AS SCHOOL_KIND");
                    stb.add("  ,'" + zenseki + "' AS CURRICULUM_CD");
                }
                stb.add("    , '" + zenseki + "' AS SUBCLASSCD");
                stb.add("    , '" + zenseki + "' AS SUBCLASSNAME");
                stb.add("    , 0 AS GRADES");
                stb.add("    , 0 AS AVG_GRADES");
                stb.add("    , 0 AS GRADE_CREDIT");
                stb.add("    , S1.CREDIT ");
                stb.add("    , CAST(NULL AS SMALLINT) AS COMP_CREDIT ");
                stb.add("    , '0' AS VALID_FLG ");
                stb.add("    , '1' AS SCHOOLCD ");
                stb.add("    , 0 AS SHOWORDERCLASS ");  // 表示順教科
                stb.add("    , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS PROV_SEMESTER ");
                stb.add("    , S1.D065FLG ");
                stb.add(" FROM (");
                stb.add("      SELECT SCHREGNO, SUM(T1.CREDIT) AS CREDIT, SUM(T1.COMP_CREDIT) AS COMP_CREDIT, MAX(D065FLG) AS D065FLG ");
                stb.add("      FROM (");
                stb.add("           SELECT T1.SCHREGNO, CREDIT, COMP_CREDIT, D065FLG ");
                stb.add("           FROM STUDYREC_DAT T1");
                stb.add("           WHERE ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') AND T1.SUBCLASSCD = '" + _zensekiSubclassCd + "')");
                if (printData.isGakunensei(param)) {
                    stb.add("          AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                }
                stb.add("      ) T1");
                stb.add("      GROUP BY T1.SCHREGNO");
                stb.add("      HAVING T1.SCHREGNO IS NOT NULL");
                stb.add(" ) S1");
            }

            // 名称マスタE014が設定されている科目の修得単位（レコードがある場合のみ）:E014
            if (null != printData._e014Subclasscd) {
                stb.add(" UNION SELECT");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                stb.add("    , YEAR ");
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("      , '" + printData._e014Subclasscd + "' AS CLASSCD");
                stb.add("      , '" + e014 + "' AS CLASSNAME");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , '" + e014 + "' AS SCHOOL_KIND");
                    stb.add("  , '" + e014 + "' AS CURRICULUM_CD");
                }
                stb.add("      , '" + printData._e014Subclasscd + "' AS SUBCLASSCD");
                stb.add("      , MAX(SUBCLASSNAME) AS SUBCLASSNAME");
                stb.add("      , 0 AS GRADES");
                stb.add("      , 0 AS AVG_GRADES");
                stb.add("      , 0 AS GRADE_CREDIT");
                stb.add("      , SUM(CREDIT) AS CREDIT");
                stb.add("      , SUM(COMP_CREDIT) AS COMP_CREDIT");
                stb.add("      , '0' AS VALID_FLG ");
                stb.add("      , '0' AS SCHOOLCD ");
                stb.add("      , 0 AS SHOWORDERCLASS ");
                stb.add("      , 0 AS SHOWORDERSUBCLASS ");
                stb.add("      , MIN(T1.PROV_SEMESTER) AS PROV_SEMESTER ");
                stb.add("      , MAX(T1.D065FLG) AS D065FLG ");
                stb.add(" FROM   STUDYREC T1");
                stb.add(" WHERE  T1.SUBCLASSCD = '" + printData._e014Subclasscd + "'");
                if (printData.isGakunensei(param)) {
                    stb.add("          AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                }
                stb.add(" GROUP BY YEAR ");
            }

            // 大検における認定単位（レコードがある場合のみ）:DAIKEN
            if (DAIKEN_DIV0_SUM == _daiken_div_code) {
                stb.add(" UNION SELECT");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                stb.add("    , '0' AS YEAR");
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("    , '" + daiken + "' AS CLASSCD");
                stb.add("    , '" + daiken + "' AS CLASSNAME");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , '" + daiken + "' AS SCHOOL_KIND");
                    stb.add("  , '" + daiken + "' AS CURRICULUM_CD");
                }
                stb.add("    , '" + daiken + "' AS SUBCLASSCD");
                stb.add("    , '" + daiken + "' AS SUBCLASSNAME");
                stb.add("    , 0 AS GRADES");
                stb.add("    , 0 AS AVG_GRADES");
                stb.add("    , 0 AS GRADE_CREDIT");
                stb.add("    , S1.CREDIT ");
                stb.add("    , CAST(NULL AS SMALLINT) AS COMP_CREDIT ");
                stb.add("    , '0' AS VALID_FLG ");
                stb.add("    , '2' AS SCHOOLCD ");
                stb.add("    , 0 AS SHOWORDERCLASS ");  // 表示順教科
                stb.add("    , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS PROV_SEMESTER ");
                stb.add("    , S1.D065FLG ");
                stb.add(" FROM (");
                stb.add("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT,SUM(T1.COMP_CREDIT ) AS COMP_CREDIT, MAX(D065FLG) AS D065FLG ");
                stb.add("      FROM (");
                stb.add("           SELECT T1.SCHREGNO, CREDIT, COMP_CREDIT, D065FLG");
                stb.add("           FROM STUDYREC_DAT T1");
                stb.add("           WHERE T1.SCHOOLCD = '2'");
                if (printData.isGakunensei(param)) {
                    stb.add("          AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                }
                stb.add("      ) T1");
                stb.add("      GROUP BY T1.SCHREGNO");
                stb.add("      HAVING T1.SCHREGNO IS NOT NULL");
                stb.add(" ) S1");
            }

            // ＬＨＲにおける認定単位（レコードがある場合のみ）:HIROKOKU94
            if (param._z010.in(Z010Info.Hirokoku)) {
                stb.add(" UNION SELECT");
                stb.add("     CAST(NULL AS VARCHAR(1)) AS RECORD_FLG ");
                if (CommonPrintData._shingakuYou == printData._output) {
                    stb.add("       , '0' AS YEAR");
                } else {
                    stb.add("   ,  YEAR");
                }
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV ");
                stb.add("      , '" + _94 + "' AS CLASSCD");
                stb.add("      , '" + hirokokulhr + "' AS CLASSNAME");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("  , '" + hirokokulhr + "' AS SCHOOL_KIND");
                    stb.add("  , '" + hirokokulhr + "' AS CURRICULUM_CD");
                }
                stb.add("      , '" + _94 + "01' AS SUBCLASSCD");
                stb.add("      , '" + hirokokulhr + "' AS SUBCLASSNAME");
                stb.add("      , 0 AS GRADES");
                stb.add("      , 0 AS AVG_GRADES");
                stb.add("      , 0 AS GRADE_CREDIT");
                stb.add("      , SUM(CREDIT) AS CREDIT"); // 年度or年次毎の単位
                stb.add("      , CAST(NULL AS SMALLINT) AS COMP_CREDIT"); // 年度or年次毎の履修単位
                stb.add("      , MAX(VALUE(T1.VALID_FLG, '0')) AS VALID_FLG ");
                stb.add("      , '0' AS SCHOOLCD ");
                stb.add("      , 0 AS SHOWORDERCLASS ");
                stb.add("      , 0 AS SHOWORDERSUBCLASS ");
                stb.add("      , MIN(PROV_SEMESTER) AS PROV_SEMESTER ");
                stb.add("      , MAX(D065FLG) AS D065FLG ");
                stb.add(" FROM   STUDYREC T1");
                stb.add(" WHERE  T1.CLASSCD = '" + _94 + "'");
                stb.add(" AND    T1.SCHOOLCD = '0'");
                if (printData.isGakunensei(param)) {
                    stb.add("          AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                }
                if (CommonPrintData._shusyokuYou == printData._output) {
                    stb.add(" GROUP BY YEAR ");
                }
            }
            stb.add(" ) ");

            stb.add(" SELECT");
            stb.add("      T1.RECORD_FLG ");
            stb.add("    , T1.YEAR ");
            stb.add("    , T1.CLASSCD ");
            stb.add("    , T1.CLASSNAME ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("  , T1.SCHOOL_KIND ");
                stb.add("  , T1.CURRICULUM_CD ");
            }
            stb.add("    , T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                stb.add("    , L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS D020 ");
            } else {
                stb.add("    , L1.NAME1 AS D020 ");
            }
            stb.add("    , L3.NAME1 AS E014 ");
            stb.add("    , T1.SUBCLASSNAME ");
            stb.add("    , T1.GRADES ");
            stb.add("    , T1.AVG_GRADES ");
            stb.add("    , T1.GRADE_CREDIT ");
            stb.add("    , T1.CREDIT ");
            stb.add("    , T1.COMP_CREDIT ");
            stb.add("    , T1.SCHOOLCD ");
            stb.add("    , T1.SHOWORDERCLASS ");
            stb.add("    , T1.SHOWORDERSUBCLASS ");
            stb.add("    , T1.PROV_SEMESTER ");
            stb.add("    , VALUE(T1.SPECIALDIV, '0') AS SPECIALDIV ");
            stb.add("    , VALUE(T1.VALID_FLG, '0') AS VALID_FLG ");
            stb.add("    , T1.D065FLG AS D065FLG ");
            stb.add(" FROM ");
            stb.add("    MAIN_T T1 ");
            if ("1".equals(param._useCurriculumcd) && "1".equals(printData.property(Property.useClassDetailDat))) {
                stb.add("    LEFT JOIN SUBCLASS_DETAIL_DAT L1 ON L1.YEAR = '" + printData._year + "' AND L1.SUBCLASS_SEQ = '006' ");
                stb.add("         AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = ");
                stb.add("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            } else {
                stb.add("    LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'D020' ");
                stb.add("         AND T1.SUBCLASSCD = L1.NAME1 ");
            }
            stb.add("    LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'E014' ");
            stb.add("         AND T1.SUBCLASSCD = L3.NAME1 ");

            stb.add(" ORDER BY ");
            stb.add("    CASE WHEN D065FLG IS NOT NULL THEN 999 ELSE 0 END, ");
            if (!"1".equals(printData._notUseClassMstSpecialDiv)) {
                if (!StringUtils.isBlank(printData._tyousasyoPrintChairSubclassSemester2)) {
                    stb.add("    SPECIALDIV, ");
                } else {
                    stb.add("    VALUE(T1.SPECIALDIV, '0'), ");
                }
            }
            stb.add("    SHOWORDERCLASS ");
            stb.add("  , CLASSCD ");
            if (!printData._isSubclassOrderNotContainCurriculumcd && "1".equals(param._useCurriculumcd)) {
                stb.add(" , SCHOOL_KIND ");
                stb.add(" , CURRICULUM_CD ");
            }
            stb.add("  , SHOWORDERSUBCLASS ");
            stb.add("  , SUBCLASSCD ");
            stb.add("  , YEAR");
            return stb;
        }

        private void addSqlChairSubclass(final CommonPrintData printData, final Param param, final List<String> stb) {

            stb.add(" , CHAIR_STD AS ( ");
            stb.add("     SELECT ");
            stb.add("         T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T3.ANNUAL, ");
            stb.add("         T2.CLASSCD, ");
            stb.add("         T2.SCHOOL_KIND, ");
            stb.add("         T2.CURRICULUM_CD, ");
            stb.add("         T2.SUBCLASSCD ");
            stb.add("     FROM CHAIR_STD_DAT T1 ");
            stb.add("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.add("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.add("         AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.add("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T3.YEAR = T1.YEAR ");
            stb.add("         AND T3.SEMESTER = T1.SEMESTER ");
            if ("1".equals(printData.property(Property.printSubclassLastChairStd))) {
                stb.add("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = T1.YEAR ");
                stb.add("       AND STDSEME.SEMESTER = T1.SEMESTER ");
                stb.add("       AND STDSEME.EDATE = T1.APPENDDATE ");
                // 最終学期と同じ学期学科の講座のみ
                stb.add("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
                stb.add("         FROM SCHREG_REGD_DAT ");
                stb.add("         GROUP BY SCHREGNO, YEAR) LASTREGD_KEY ");
                stb.add("       ON LASTREGD_KEY.SCHREGNO = T1.SCHREGNO ");
                stb.add("      AND LASTREGD_KEY.YEAR = T1.YEAR ");
                stb.add("     INNER JOIN SCHREG_REGD_DAT LASTREGD ");
                stb.add("       ON LASTREGD.SCHREGNO = LASTREGD_KEY.SCHREGNO ");
                stb.add("      AND LASTREGD.YEAR = LASTREGD_KEY.YEAR ");
                stb.add("      AND LASTREGD.SEMESTER = LASTREGD_KEY.SEMESTER ");
                stb.add("      AND LASTREGD.COURSECD = T3.COURSECD ");
                stb.add("      AND LASTREGD.MAJORCD = T3.MAJORCD ");
            }
            stb.add("     WHERE ");
            stb.add("         T1.YEAR = '" + printData._year + "' ");
            stb.add("         AND T1.SCHREGNO = '" + printData._schregno + "' ");
            if ("1".equals(printData._tyousasyoPrintChairSubclassSemester2)) {
                // 履修期間区分後期
                stb.add("         AND T2.TAKESEMES = '2' ");
            } else if ("ALL".equals(printData._tyousasyoPrintChairSubclassSemester2)) {
                // 全て
            } else {
                stb.add("         AND T1.SEMESTER = '999999' ");
                log.warn(" tyousasyoPrintChairSubclassSemester2 = " + printData._tyousasyoPrintChairSubclassSemester2);
            }
            stb.add(" ) ");
            stb.add(" , MAX_SEMESTER_THIS_YEAR AS ( ");
            stb.add("     SELECT ");
            stb.add("         SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
            stb.add("     FROM CHAIR_STD ");
            stb.add("     GROUP BY ");
            stb.add("         SCHREGNO, YEAR ");
            stb.add(" ) ");
            stb.add(" , CREDIT_MST_CREDITS AS ( ");
            stb.add("     SELECT DISTINCT ");
            stb.add("         T1.YEAR, T1.SCHREGNO, T2.ANNUAL, ");
            stb.add("         T1.CLASSCD, ");
            stb.add("         T1.SCHOOL_KIND, ");
            stb.add("         T1.CURRICULUM_CD, ");
            stb.add("         T1.SUBCLASSCD, ");
            stb.add("         T3.CREDITS ");
            stb.add("     FROM CHAIR_STD T1 ");
            stb.add("     INNER JOIN MAX_SEMESTER_THIS_YEAR SEM ON SEM.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND SEM.YEAR = T1.YEAR ");
            stb.add("     LEFT JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T2.YEAR = T1.YEAR ");
            stb.add("         AND T2.SEMESTER = SEM.SEMESTER ");
            stb.add("     LEFT JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
            stb.add("         AND T3.COURSECD = T2.COURSECD ");
            stb.add("         AND T3.MAJORCD = T2.MAJORCD ");
            stb.add("         AND T3.GRADE = T2.GRADE ");
            stb.add("         AND T3.COURSECODE = T2.COURSECODE ");
            stb.add("         AND T3.CLASSCD = T1.CLASSCD ");
            stb.add("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add(" ) ");
            stb.add(" , CHAIR_STD_COMBINED AS ( ");
            stb.add("     SELECT ");
            stb.add("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            stb.add("            T1.CLASSCD, ");
            stb.add("            T1.SCHOOL_KIND, ");
            stb.add("            T1.CURRICULUM_CD, ");
            stb.add("            T1.SUBCLASSCD, ");
            stb.add("            T5.CREDITS ");
            stb.add("     FROM CHAIR_STD T1 ");
            stb.add("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.add("         AND T3.COMBINED_CLASSCD = T1.CLASSCD ");
            stb.add("         AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.add("         AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.add("         AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T4.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
            stb.add("         AND T5.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T5.CLASSCD = T1.CLASSCD ");
            stb.add("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("     WHERE ");
            stb.add("         T3.COMBINED_SUBCLASSCD IS NULL ");
            stb.add("         AND T4.ATTEND_SUBCLASSCD IS NULL ");
            stb.add("     UNION ");
            stb.add("     SELECT ");
            stb.add("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            stb.add("            T3.COMBINED_CLASSCD AS CLASSCD, ");
            stb.add("            T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.add("            T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.add("            T3.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.add("            CASE WHEN '2' = MAX(T3.CALCULATE_CREDIT_FLG) THEN SUM(T5.CREDITS) ");
            stb.add("                 ELSE MAX(T6.CREDITS) ");
            stb.add("            END AS CREDITS ");
            stb.add("     FROM CHAIR_STD T1 ");
            stb.add("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.add("         AND T3.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.add("         AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
            stb.add("         AND T5.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T5.CLASSCD = T3.ATTEND_CLASSCD ");
            stb.add("         AND T5.SCHOOL_KIND = T3.ATTEND_SCHOOL_KIND ");
            stb.add("         AND T5.CURRICULUM_CD = T3.ATTEND_CURRICULUM_CD ");
            stb.add("         AND T5.SUBCLASSCD = T3.ATTEND_SUBCLASSCD ");
            stb.add("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
            stb.add("         AND T6.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T6.CLASSCD = T3.COMBINED_CLASSCD ");
            stb.add("         AND T6.SCHOOL_KIND = T3.COMBINED_SCHOOL_KIND ");
            stb.add("         AND T6.CURRICULUM_CD = T3.COMBINED_CURRICULUM_CD ");
            stb.add("         AND T6.SUBCLASSCD = T3.COMBINED_SUBCLASSCD ");
            stb.add("     GROUP BY ");
            stb.add("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            stb.add("            T3.COMBINED_CLASSCD, ");
            stb.add("            T3.COMBINED_SCHOOL_KIND, ");
            stb.add("            T3.COMBINED_CURRICULUM_CD, ");
            stb.add("            T3.COMBINED_SUBCLASSCD ");
            stb.add(" ) ");
            stb.add(" , CHAIR_STD_SUBCLASSCD2 AS ( ");
            stb.add("     SELECT ");
            stb.add("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            stb.add("         T1.CLASSCD, ");
            stb.add("         T1.SCHOOL_KIND, ");
            stb.add("         T1.CURRICULUM_CD, ");
            stb.add("         T1.SUBCLASSCD AS CHAIR_SUBCLASSCD, ");
            stb.add("         T1.SUBCLASSCD, ");
            stb.add("         T1.CREDITS ");
            stb.add("     FROM CHAIR_STD_COMBINED T1 ");
            stb.add("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.add("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("         AND T2.SUBCLASSCD2 IS NULL ");
            stb.add("     UNION ");
            stb.add("     SELECT ");
            stb.add("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            stb.add("         T1.CLASSCD, ");
            stb.add("         T1.SCHOOL_KIND, ");
            stb.add("         T1.CURRICULUM_CD, ");
            stb.add("         T1.SUBCLASSCD AS CHAIR_SUBCLASSCD, ");
            stb.add("         T2.SUBCLASSCD2 AS SUBCLASSCD, ");
            stb.add("         VALUE(T6_2.CREDITS, T6.CREDITS) AS CREDITS ");
            stb.add("     FROM CHAIR_STD_COMBINED T1 ");
            stb.add("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.add("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("         AND T2.SUBCLASSCD2 IS NOT NULL ");
            stb.add("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
            stb.add("         AND T6.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T6.CLASSCD = T1.CLASSCD ");
            stb.add("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("     LEFT JOIN CREDIT_MST_CREDITS T6_2 ON T6_2.YEAR = T1.YEAR ");
            stb.add("         AND T6_2.SCHREGNO = T1.SCHREGNO ");
            stb.add("         AND T6_2.CLASSCD = T1.CLASSCD ");
            stb.add("         AND T6_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("         AND T6_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("         AND T6_2.SUBCLASSCD = T2.SUBCLASSCD2 ");
            stb.add(" ) ");
            stb.add(" , CHAIR_STD_SUBCLASS_MAIN AS (");
            stb.add(" SELECT ");
            stb.add("     T1.YEAR, T1.SCHREGNO, T1.ANNUAL ");
            stb.add("   , T1.CLASSCD ");
            stb.add("   , T1.SCHOOL_KIND ");
            stb.add("   , T1.CURRICULUM_CD ");
            stb.add("   , T1.CHAIR_SUBCLASSCD ");
            stb.add("   , T1.SUBCLASSCD ");
            stb.add("   , T1.CREDITS");
            stb.add("   , VALUE(T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME ");
            stb.add("   , VALUE(SCLM.SUBCLASSORDERNAME1, SCLM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.add(" FROM CHAIR_STD_SUBCLASSCD2 T1 ");
            stb.add(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.add("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add(" LEFT JOIN SUBCLASS_MST SCLM ON SCLM.CLASSCD = T1.CLASSCD ");
            stb.add("     AND SCLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.add("     AND SCLM.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.add("     AND SCLM.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add(" ) ");
        }

        /**
         * 代替科目備考データのSQLを返します。
         * @return
         */
        public List<String> getSubstitutionSubclassNoteSql(final CommonPrintData printData, final Param param, final String substitutionTypeFlg) {

            final List<String> stb = new LinkedList<String>();

            stb.addAll(getStudyrecSqlString(printData, param));
            stb.add(" , MAX_SEMESTER AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.add("   FROM SCHREG_REGD_DAT ");
            stb.add("   GROUP BY YEAR, SCHREGNO ");
            stb.add(" ) ");

            stb.add(" SELECT  T1.SCHREGNO, T4.YEAR, T1.ANNUAL, T1.SCHOOLCD ");
            stb.add("       , T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("        , T1.SCHOOL_KIND ");
                stb.add("        , T1.CURRICULUM_CD ");
            }
            stb.add("       , T1.SUBCLASSCD AS SUBCLASSCD2_SUBCLASSCD ");
            stb.add("       , T1.STUDYREC_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            stb.add("       , RTRIM(T1.CLASSNAME) AS ATTEND_CLASSNAME ");
            stb.add("       , RTRIM(T1.SUBCLASSNAME) AS ATTEND_SUBCLASSNAME");
            stb.add("       , CASE WHEN T1.GET_CREDIT IS NOT NULL OR T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) END AS ATTEND_CREDIT ");
            stb.add("       , T1.SHOWORDERCLASS");
            stb.add("       , T1.SHOWORDERSUBCLASS");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("       , T4.SUBSTITUTION_CLASSCD ");
                stb.add("       , T4.SUBSTITUTION_SCHOOL_KIND ");
                stb.add("       , T4.SUBSTITUTION_CURRICULUM_CD ");
            } else {
                stb.add("       , SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) AS SUBSTITUTION_CLASSCD");
            }
            stb.add("       , T4.SUBSTITUTION_SUBCLASSCD ");
            stb.add("       , RTRIM(CLMSB.CLASSNAME) AS SUBSTITUTION_CLASSNAME");
            stb.add("       , VALUE(RTRIM(SCLM.SUBCLASSORDERNAME1), RTRIM(SCLM.SUBCLASSNAME)) AS SUBSTITUTION_SUBCLASSNAME");
            stb.add("       , CREM.CREDITS AS SUBSTITUTION_CREDIT");
            stb.add(" FROM SUBCLASS_REPLACE_SUBSTITUTION_DAT T4 ");
            stb.add(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T5 ON ");
            stb.add("        T5.YEAR = T4.YEAR AND T5.SUBSTITUTION_SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("        AND T5.SUBSTITUTION_CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                stb.add("        AND T5.SUBSTITUTION_SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
                stb.add("        AND T5.SUBSTITUTION_CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            }
            stb.add("        AND T5.ATTEND_SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("        AND T5.ATTEND_CLASSCD = T4.ATTEND_CLASSCD ");
                stb.add("        AND T5.ATTEND_SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.add("        AND T5.ATTEND_CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            }

            stb.add(" LEFT JOIN CLASS_MST CLMSB ON ");
            stb.add("     CLMSB.CLASSCD = SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add(" AND CLMSB.SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
            }
            if ("1".equals(printData.property(Property.tyousashoDaitaiCheckMasterYear))) {
                stb.add(" INNER JOIN SUBCLASS_MST SCLM ON ");
                stb.add("     SCLM.SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            } else {
                stb.add(" INNER JOIN V_SUBCLASS_MST SCLM ON SCLM.YEAR = T4.YEAR ");
                stb.add(" AND SCLM.SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.add(" AND SCLM.CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                stb.add(" AND SCLM.SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
                stb.add(" AND SCLM.CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            }
            if ("1".equals(printData.property(Property.tyousashoDaitaiCheckMasterYear))) {
                stb.add(" INNER JOIN SUBCLASS_MST SCLMAT ON ");
                stb.add("     SCLMAT.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            } else {
                stb.add(" INNER JOIN V_SUBCLASS_MST SCLMAT ON SCLMAT.YEAR = T4.YEAR ");
                stb.add(" AND SCLMAT.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.add(" AND SCLMAT.CLASSCD = T4.ATTEND_CLASSCD ");
                stb.add(" AND SCLMAT.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.add(" AND SCLMAT.CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            }

            stb.add(" INNER JOIN MAX_SEMESTER SEM ON SEM.YEAR = T4.YEAR AND SEM.SCHREGNO = '" + printData._schregno + "' ");
            stb.add(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T4.YEAR AND REGD.SEMESTER = SEM.SEMESTER AND REGD.SCHREGNO = '" + printData._schregno + "' ");
            stb.add("        AND T5.GRADE = REGD.GRADE ");
            stb.add("        AND T5.COURSECD = REGD.COURSECD ");
            stb.add("        AND T5.MAJORCD = REGD.MAJORCD ");
            stb.add("        AND T5.COURSECODE = REGD.COURSECODE ");

            stb.add(" LEFT JOIN CREDIT_MST CREM ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add(" CREM.CLASSCD = SCLMAT.CLASSCD ");
                stb.add(" AND CREM.SCHOOL_KIND = SCLMAT.SCHOOL_KIND ");
                stb.add(" AND CREM.CURRICULUM_CD = SCLMAT.CURRICULUM_CD ");
            } else {
                stb.add(" CREM.CLASSCD = SUBSTR(SCLMAT.SUBCLASSCD, 1, 2) ");
            }
            stb.add("       AND CREM.SUBCLASSCD = SCLMAT.SUBCLASSCD ");
            stb.add("       AND CREM.YEAR = REGD.YEAR ");
            stb.add("       AND CREM.GRADE = REGD.GRADE ");
            stb.add("       AND CREM.COURSECD = REGD.COURSECD ");
            stb.add("       AND CREM.MAJORCD = REGD.MAJORCD ");
            stb.add("       AND CREM.COURSECODE = REGD.COURSECODE ");

            stb.add(" LEFT JOIN STUDYREC T1 ON T1.YEAR = T4.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("        AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.add("        AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("        AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.add("        AND T4.ATTEND_SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ");

            stb.add(" WHERE");
            stb.add("     T4.SUBSTITUTION_TYPE_FLG = '" + substitutionTypeFlg + "' ");
            if (printData.isGakunensei(param)) {
                stb.add("   AND (T1.YEAR IS NULL ");
                stb.add("     OR T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR)");
                stb.add("      ) ");
            }
            return stb;
        }

        /**
         * 学習記録データの SQL SELECT 文を戻します。
         * @return
         */
        protected List<String> getStudyrecSqlString(final CommonPrintData printData, final Param param) {
            final String chkProvFlg;
            if ("1".equals(printData.property(Property.useProvFlg))) {
                chkProvFlg = " PROV_FLG = '1' AND ";
            } else if (printData._isKanendoHyoteiYomikae) {
                chkProvFlg = "";
            } else {
                chkProvFlg = " T1.YEAR = '" + printData._year + "' AND ";
            }
            boolean _isPrintRisyu = "1".equals(defstr(printData.getParameter(Parameter.RISYU), "1"));  // 履修のみ科目出力 1:出力する 2:出力しない
            boolean _isPrintMirisyu = "1".equals(defstr(printData.getParameter(Parameter.MIRISYU), "2"));  // 未履修科目出力 1:出力する 2:出力しない

            final List<String> stb = new LinkedList<String>();
            getStudyrecSqlString1(printData, param, stb);

            stb.add(",SUBCLASSGROUP AS("); // :SUBCLASSGROUP
            stb.add("   SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD,");
            }
            stb.add("    T1.SUBCLASSCD, T2.SUBCLASSCD2");
            stb.add("  , CLM.CLASSNAME, CLM.CLASSORDERNAME1");
            stb.add("  , CLM.SHOWORDER2 AS SHOWORDERCLASS");
            stb.add("  , SCLM.SUBCLASSNAME, SCLM.SUBCLASSORDERNAME1");
            stb.add("  , SCLM.SHOWORDER2 AS SHOWORDERSUBCLASS");
            stb.add(" FROM ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("  (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM STUDYREC_DAT GROUP BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) T1");
            } else {
                stb.add("  (SELECT SUBCLASSCD FROM STUDYREC_DAT GROUP BY SUBCLASSCD) T1");
            }
            stb.add("   INNER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND");
                stb.add("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD");
                stb.add("     AND T1.CLASSCD = T2.CLASSCD");
            }
            stb.add("      AND T2.SUBCLASSCD2 IS NOT NULL");
            stb.add("   INNER JOIN SUBCLASS_MST SCLM ON T2.SUBCLASSCD2 = SCLM.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("     AND T2.SCHOOL_KIND = SCLM.SCHOOL_KIND");
                stb.add("     AND T2.CURRICULUM_CD = SCLM.CURRICULUM_CD");
                stb.add("     AND T2.CLASSCD = SCLM.CLASSCD");
            }
            stb.add("   INNER JOIN CLASS_MST CLM ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("    T2.CLASSCD = CLM.CLASSCD AND T2.SCHOOL_KIND = CLM.SCHOOL_KIND");
            } else {
                stb.add("    SUBSTR(T2.SUBCLASSCD,1,2) = CLM.CLASSCD");
            }
            stb.add(" )");

            stb.add(", STUDYREC_SUBCLASSGROUP AS("); // :STUDYREC_SUBCLASSGROUP
            stb.add("   SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD,");
            }
            stb.add("    T2.SUBCLASSCD");
            stb.add("  , MIN(T2.CLASSNAME) AS CLASSNAME");
            stb.add("  , MIN(T2.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.add(" FROM  SUBCLASSGROUP T1, STUDYREC_DAT T2");
            stb.add(" WHERE  T1.SUBCLASSCD2 = T2.SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("        AND T1.CLASSCD = T2.CLASSCD");
                stb.add("        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND");
                stb.add("        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD");
            }
            stb.add("         AND (T2.SUBCLASSNAME IS NOT NULL OR T2.CLASSNAME IS NOT NULL)");
            stb.add(" GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            }
            stb.add("        T2.SUBCLASSCD");
            stb.add(" )");
            stb.add(", STUDYREC AS("); // :STUDYREC
            stb.add("   SELECT ");
            stb.add("      T1.RECORD_FLG, T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.SCHOOLCD");
            stb.add("    , T1.GRADES, T1.CREDIT, T1.COMP_CREDIT, T1.GET_CREDIT, T1.ADD_CREDIT");
            stb.add("    , T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("     , T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.add("    , T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD ");
            stb.add("    , VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.add("    , CASE WHEN T1.SCHOOLCD = '1' THEN ");
            stb.add("         VALUE( ");
            if (param._tableInfo._hasANOTHER_CLASS_MST) {
                stb.add("           ANTCLM.CLASSORDERNAME1, ANTCLM.CLASSNAME, ");
            }
            stb.add("               T3.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME, T1.CLASSNAME, CLM.CLASSORDERNAME1, CLM.CLASSNAME) ");
            stb.add("         ELSE ");
            stb.add("         VALUE(T3.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME, T1.CLASSNAME, CLM.CLASSORDERNAME1, CLM.CLASSNAME) ");
            stb.add("      END AS CLASSNAME");
            stb.add("    , CASE WHEN T1.SCHOOLCD = '1' THEN ");
            stb.add("         VALUE( ");
            if (param._tableInfo._hasANOTHER_SUBCLASS_MST) {
                stb.add("           ANTSCLM.SUBCLASSORDERNAME1, ANTSCLM.SUBCLASSNAME, ");
            }
            stb.add("               T3.SUBCLASSNAME, T2.SUBCLASSORDERNAME1, T2.SUBCLASSNAME, T1.SUBCLASSNAME, SCLM.SUBCLASSORDERNAME1, SCLM.SUBCLASSNAME) ");
            stb.add("         ELSE ");
            stb.add("         VALUE(T3.SUBCLASSNAME, T2.SUBCLASSORDERNAME1, T2.SUBCLASSNAME, T1.SUBCLASSNAME, SCLM.SUBCLASSORDERNAME1, SCLM.SUBCLASSNAME) ");
            stb.add("      END AS SUBCLASSNAME");
            stb.add("    , CASE WHEN T1.SCHOOLCD = '1' THEN ");
            stb.add("         VALUE( ");
            if (param._tableInfo._hasANOTHER_CLASS_MST) {
                stb.add("           ANTCLM.SHOWORDER2, ");
            }
            stb.add("               T2.SHOWORDERCLASS, CLM.SHOWORDER2, 999) ");
            stb.add("         ELSE ");
            stb.add("         VALUE(T2.SHOWORDERCLASS, CLM.SHOWORDER2, 999) ");
            stb.add("      END AS SHOWORDERCLASS");
            stb.add("    , CASE WHEN T1.SCHOOLCD = '1' THEN ");
            stb.add("         VALUE( ");
            if (param._tableInfo._hasANOTHER_SUBCLASS_MST) {
                stb.add("           ANTSCLM.SHOWORDER2, ");
            }
            stb.add("               T2.SHOWORDERSUBCLASS, SCLM.SHOWORDER2, 999) ");
            stb.add("         ELSE ");
            stb.add("         VALUE(T2.SHOWORDERSUBCLASS, SCLM.SHOWORDER2, 999) ");
            stb.add("      END AS SHOWORDERSUBCLASS");
            stb.add("    , SC.GVAL_CALC");
            stb.add("    , CASE WHEN T1.SCHOOLCD = '1' THEN ");
            stb.add("         VALUE( ");
            if (param._tableInfo._hasANOTHER_CLASS_MST) {
                stb.add("           ANTCLM.SPECIALDIV, ");
            }
            stb.add("               CLM.SPECIALDIV, '0') ");
            stb.add("         ELSE ");
            stb.add("         VALUE(CLM.SPECIALDIV, '0') ");
            stb.add("      END AS SPECIALDIV");
            stb.add("    , T1.CREDIT_MST_CREDIT");
            if ("1".equals(printData.property(Property.useProvFlg))) {
                stb.add("    , PRV.PROV_FLG");
            } else {
                stb.add("    , CAST(NULL AS VARCHAR(1)) AS PROV_FLG ");
            }
            if ("1".equals(printData.property(Property.useProvFlg)) && param._tableInfo._hasSTUDYREC_PROV_FLG_DAT_PROV_SEMESTER) {
                stb.add("    , PRV.PROV_SEMESTER ");
            } else {
                stb.add("    , CAST(NULL AS VARCHAR(7)) AS PROV_SEMESTER ");
            }
            stb.add("    , T1.VALID_FLG ");
            stb.add("    , T1.D065FLG ");
            stb.add("    FROM  STUDYREC_DAT T1");
            stb.add("    LEFT JOIN SUBCLASSGROUP T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      AND T2.CLASSCD = T1.CLASSCD ");
                stb.add("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.add("    LEFT JOIN STUDYREC_SUBCLASSGROUP T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD2");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      AND T3.CLASSCD = T1.CLASSCD ");
                stb.add("      AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("      AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.add("    LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.add("    LEFT JOIN SUBCLASS_MST SCLM ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      SCLM.CLASSCD = T1.CLASSCD AND ");
                stb.add("      SCLM.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                stb.add("      SCLM.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.add("          SCLM.SUBCLASSCD = T1.SUBCLASSCD");
            if (param._tableInfo._hasANOTHER_CLASS_MST) {
                stb.add("        LEFT JOIN ANOTHER_CLASS_MST ANTCLM ON ANTCLM.CLASSCD = T1.CLASSCD");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("          AND ANTCLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
            }
            if (param._tableInfo._hasANOTHER_SUBCLASS_MST) {
                stb.add("        LEFT JOIN ANOTHER_SUBCLASS_MST ANTSCLM ON ANTSCLM.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("      AND ANTSCLM.CLASSCD = T1.CLASSCD ");
                    stb.add("      AND ANTSCLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.add("      AND ANTSCLM.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
            }
            if ("1".equals(printData.property(Property.useProvFlg))) {
                stb.add("        LEFT JOIN STUDYREC_PROV_FLG_DAT PRV ON PRV.SCHOOLCD = T1.SCHOOLCD ");
                stb.add("          AND PRV.YEAR = T1.YEAR ");
                stb.add("          AND PRV.SCHREGNO = T1.SCHREGNO ");
                stb.add("          AND PRV.CLASSCD = T1.CLASSCD ");
                stb.add("          AND PRV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("          AND PRV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.add("          AND PRV.SUBCLASSCD = T1.SUBCLASSCD ");
            }
            stb.add("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR");
            stb.add((param._tableInfo._hasSCHOOL_MST_SCHOOL_KIND ? " AND SC.SCHOOL_KIND = 'H' " : ""));
            if (param.isKindaifuzoku()) {
                stb.add("    WHERE NOT EXISTS(SELECT  'X' FROM  SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                stb.add("                     WHERE   T2.YEAR = T1.YEAR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("      AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                    stb.add("      AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.add("      AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.add("          AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD)");
                stb.add("          AND T1.SCHOOLCD = '0'");
            } else {
                stb.add("    WHERE  (");
                if (DAIKEN_DIV0_SUM == _daiken_div_code) {
                    stb.add("        T1.SCHOOLCD = '0'");
                } else {
                    stb.add("        T1.SCHOOLCD = '0'");
                    stb.add("     OR (T1.SCHOOLCD = '2' AND T1.CREDIT IS NOT NULL)");
                }
                if (null != _zensekiSubclassCd) {
                    stb.add("     OR ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') AND T1.SUBCLASSCD <> '" + _zensekiSubclassCd + "')");
                } else {
                    stb.add("     OR (T1.SCHOOLCD = '1' OR T1.YEAR = '0')");
                }
                stb.add(         ")");
                //履修のみ科目出力・・・「履修のみ」とは、「修得単位がゼロ　かつ　履修単位がゼロ以外」
                if (!_isPrintRisyu) {
                    if ("on".equals(_hyoutei)){ //----->評定読み替えのON/OFF  評定１を２と読み替え
                        // 単位数は
                        //  GET_CREDIT が 0の時、
                        //  SCHREG_STUDYREC_DAT に登録された COMP_CREDIT（履修単位）の値が0以外は COMP_CREDIT（履修単位）を GET_CREDIT として取得、
                        //  0の時は単位マスタの CREDITS を GET_CREDIT として取得して、
                        //  ADD_CREDITを加算する。
                        stb.add("     AND ( ");
                        stb.add("                  (CASE WHEN " + chkProvFlg + " GRADES = 1 AND T1.GET_CREDIT = 0 THEN ");
                        stb.add("                    (CASE WHEN VALUE(COMP_CREDIT, 0) <> 0 THEN ");
                        stb.add("                     COMP_CREDIT ");
                        stb.add("                     ELSE (CASE WHEN VALUE(ADD_CREDIT, 0) > 0 THEN VALUE(CREDIT_MST_CREDIT, 0) ELSE CREDIT_MST_CREDIT END) ");
                        stb.add("                     END");
                        stb.add("                    ) ");
                        stb.add("                   ELSE (CASE WHEN VALUE(ADD_CREDIT, 0) > 0 THEN VALUE(T1.GET_CREDIT, 0) ELSE T1.GET_CREDIT END) ");
                        stb.add("                   END");
                        stb.add("                  ) ");
                        stb.add("                  + VALUE(ADD_CREDIT, 0) ");
                        stb.add("                    <> 0) ");
                    } else {
                        //「修得単位がゼロ　かつ　履修単位がゼロ以外」のレコードは印刷しない。
                        stb.add("     AND (T1.CREDIT <> 0) ");
                    }
                }
                if (!_isPrintMirisyu) {
                    if (PrintClass.is94_(printData)) {
                        stb.add("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U +"' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                        stb.add("            AND T1.COMP_CREDIT <> 0 ");
                        stb.add("            OR T1.CLASSCD = '" + PrintClass.getHanasuClasscd(printData) + "') ");
                    } else {
                        stb.add("     AND T1.COMP_CREDIT <> 0 ");
                    }
                }
            }
            stb.add(" )");
            return stb;
        }

        private void getStudyrecSqlString1(final CommonPrintData printData, final Param param, final List<String> stb) {

            final boolean isOutput94 = (PrintClass.is94_(printData)) && CommonPrintData._shingakuYou == printData._output || param._z010.in(Z010Info.Hirokoku);
            final List<String> _90OverD077 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD077List);
            final List<String> _90OverD081 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstD081List);
            final List<String> _90OverE065 = CommonPrintData.get90OverSubclasscdList(printData._vNameMstE065List);

            final boolean useRecordScoreDat = "RECORD_SCORE_DAT".equals(printData.getParameter(Parameter.hyoteiheikin__table));

            stb.add(" WITH");
            if (printData.isGakunensei(param)) {
                stb.add(" DROP_YEAR AS(");
                stb.add("        SELECT DISTINCT YEAR");
                stb.add("        FROM SCHREG_REGD_DAT T1");
                stb.add("        WHERE SCHREGNO = '" + printData._schregno + "' ");
                stb.add("        AND T1.YEAR NOT IN (SELECT MAX(YEAR) FROM SCHREG_REGD_DAT T2 WHERE SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' GROUP BY GRADE)");
                stb.add(" ),");
            }
            stb.add(" MAX_SEMESTER0 AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.add("   FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + printData._schregno + "' ");
            stb.add("   GROUP BY YEAR, SCHREGNO ");
            stb.add(" ), ");

            if (useRecordScoreDat) {
                final String recordScoreDatSemester = defstr(printData.getParameter(Parameter.hyoteiheikin__table__RECORD_SCORE_DAT__SEMESTER), "9");

                stb.add(" RECORD_SCORE_DAT00 AS ( ");
                stb.add("   SELECT ");
                stb.add("        '0' AS SCHOOLCD ");
                stb.add("      , T1.YEAR ");
                stb.add("      , T1.SCHREGNO ");
                stb.add("      , T3.ANNUAL ");
                stb.add("      , T1.CLASSCD ");
                stb.add("      , T1.SCHOOL_KIND ");
                stb.add("      , T1.CURRICULUM_CD ");
                stb.add("      , T1.SUBCLASSCD ");
                stb.add("      , CAST(NULL AS VARCHAR(1)) AS CLASSNAME ");
                stb.add("      , CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME ");
                stb.add("      , T1.SCORE AS VALUATION ");
                stb.add("      , T4.CREDITS AS GET_CREDIT ");
                stb.add("      , CAST(NULL AS SMALLINT) AS ADD_CREDIT ");
                stb.add("      , T4.CREDITS AS COMP_CREDIT ");
                stb.add("      , CAST(NULL AS VARCHAR(1)) AS VALID_FLG ");
                stb.add("   FROM  RECORD_SCORE_DAT T1");
                stb.add("   LEFT JOIN MAX_SEMESTER0 T2 ");
                stb.add("       ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.add("      AND T2.YEAR = T1.YEAR ");
                stb.add("   LEFT JOIN SCHREG_REGD_DAT T3 ");
                stb.add("       ON T3.SCHREGNO = T2.SCHREGNO ");
                stb.add("      AND T3.YEAR = T2.YEAR ");
                stb.add("      AND T3.SEMESTER = T2.SEMESTER ");
                stb.add("   LEFT JOIN CREDIT_MST T4 ");
                stb.add("       ON T4.YEAR = T2.YEAR ");
                stb.add("      AND T4.COURSECD = T3.COURSECD ");
                stb.add("      AND T4.MAJORCD = T3.MAJORCD ");
                stb.add("      AND T4.GRADE = T3.GRADE ");
                stb.add("      AND T4.COURSECODE = T3.COURSECODE ");
                stb.add("      AND T4.CLASSCD = T1.CLASSCD ");
                stb.add("      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.add("      AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.add("   WHERE  T1.SCHREGNO = '" + printData._schregno + "' ");
                stb.add("      AND T1.YEAR =  '" + printData._year + "' ");
                stb.add("      AND T1.SEMESTER =  '" + recordScoreDatSemester + "' ");
                stb.add("      AND T1.TESTKINDCD = '99' ");
                stb.add("      AND T1.TESTITEMCD = '00' ");
                stb.add("      AND T1.SCORE_DIV = '09' ");
                stb.add(" ), ");
            }

            stb.add(" STUDYREC_DAT00 AS(");
            stb.add("   SELECT ");
            stb.add("        T1.SCHOOLCD ");
            stb.add("      , T1.YEAR ");
            stb.add("      , T1.SCHREGNO ");
            stb.add("      , T1.ANNUAL ");
            stb.add("      , T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      , T1.SCHOOL_KIND ");
                stb.add("      , T1.CURRICULUM_CD ");
            }
            stb.add("      , T1.SUBCLASSCD ");
            stb.add("      , T1.CLASSNAME ");
            stb.add("      , T1.SUBCLASSNAME ");
            stb.add("      , T1.VALUATION ");
            stb.add("      , T1.GET_CREDIT ");
            stb.add("      , T1.ADD_CREDIT ");
            stb.add("      , T1.COMP_CREDIT ");
            if (param._tableInfo._hasSCHREG_STUDYREC_DETAIL_DAT) {
                stb.add("        , VALUE(TDET.REMARK1, '0') AS VALID_FLG ");
            } else {
                stb.add("        , '0' AS VALID_FLG ");
            }
            stb.add("   FROM  " + _TableName_StudyRec + " T1");
            if (param._tableInfo._hasSCHREG_STUDYREC_DETAIL_DAT) {
                stb.add("         LEFT JOIN SCHREG_STUDYREC_DETAIL_DAT TDET ON TDET.SCHOOLCD = T1.SCHOOLCD ");
                stb.add("            AND TDET.YEAR = T1.YEAR ");
                stb.add("            AND TDET.SCHREGNO = T1.SCHREGNO ");
                stb.add("            AND TDET.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("            AND TDET.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.add("            AND TDET.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.add("            AND TDET.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.add("            AND TDET.SEQ = '002' ");
            }
            stb.add("   WHERE  T1.SCHREGNO = '" + printData._schregno + "' ");
            stb.add("          AND value(T1.PRINT_FLG, '0') NOT IN('1')");//印刷有無フラグが１のレコードは印刷しない。
            if (!useRecordScoreDat) {
                stb.add("      AND T1.YEAR <= '" + printData._year + "' ");
            } else {

                stb.add("      AND T1.YEAR <  '" + printData._year + "' ");
                stb.add("   UNION ALL ");
                stb.add("   SELECT ");
                stb.add("        T1.SCHOOLCD ");
                stb.add("      , T1.YEAR ");
                stb.add("      , T1.SCHREGNO ");
                stb.add("      , T1.ANNUAL ");
                stb.add("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("      , T1.SCHOOL_KIND ");
                    stb.add("      , T1.CURRICULUM_CD ");
                }
                stb.add("      , T1.SUBCLASSCD ");
                stb.add("      , T1.CLASSNAME ");
                stb.add("      , T1.SUBCLASSNAME ");
                stb.add("      , T1.VALUATION ");
                stb.add("      , T1.GET_CREDIT ");
                stb.add("      , T1.ADD_CREDIT ");
                stb.add("      , T1.COMP_CREDIT ");
                stb.add("      , T1.VALID_FLG ");
                stb.add("   FROM RECORD_SCORE_DAT00 T1");
                stb.add("   WHERE ");
                stb.add("      (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN ( ");
                stb.add("          SELECT ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
                stb.add("          FROM SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.add("          WHERE YEAR = '" + printData._year + "' ");
//                stb.add("          UNION ");
//                stb.add("          SELECT COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD ");
//                stb.add("          FROM SUBCLASS_REPLACE_COMBINED_DAT ");
//                stb.add("          WHERE YEAR = '" + printData._year + "' ");
                stb.add("      ) ");

//            	stb.add("   UNION ALL ");
//                stb.add("   SELECT ");
//                stb.add("        T1.SCHOOLCD ");
//                stb.add("      , T1.YEAR ");
//                stb.add("      , T1.SCHREGNO ");
//                stb.add("      , T1.ANNUAL ");
//                stb.add("      , T1.COMBINED_CLASSCD AS CLASSCD ");
//                stb.add("      , T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND ");
//                stb.add("      , T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD ");
//                stb.add("      , T1.COMBINED_SUBCLASSCD AS SUBCLASSCD ");
//                stb.add("      , I2.CLASSNAME ");
//                stb.add("      , I3.SUBCLASSNAME ");
//                stb.add("      , T1.VALUATION ");
//                stb.add("      , SUM(T1.GET_CREDIT) AS GET_CREDIT ");
//                stb.add("      , SUM(T1.ADD_CREDIT) AS ADD_CREDIT ");
//                stb.add("      , SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
//                stb.add("      , MAX(T1.VALID_FLG) AS VALID_FLG ");
//                stb.add("   FROM RECORD_SCORE_DAT00 T1");
//                stb.add("   INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT I1 ");
//                stb.add("       ON I1.YEAR = T1.YEAR ");
//                stb.add("      AND I1.ATTEND_CLASSCD = T1.CLASSCD ");
//                stb.add("      AND I1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
//                stb.add("      AND I1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
//                stb.add("      AND I1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
//                stb.add("   INNER JOIN CLASS_MST I2 ");
//                stb.add("       ON I2.CLASSCD = I1.COMBINED_CLASSCD ");
//                stb.add("      AND I2.SCHOOL_KIND = I1.COMBINED_SCHOOL_KIND ");
//                stb.add("   INNER JOIN SUBCLASS_MST I3 ");
//                stb.add("       ON I3.CLASSCD = I1.COMBINED_CLASSCD ");
//                stb.add("      AND I3.SCHOOL_KIND = I1.COMBINED_SCHOOL_KIND ");
//                stb.add("      AND I3.CURRICULUM_CD = I1.COMBINED_CURRICULUM_CD ");
//                stb.add("      AND I3.SUBCLASSCD = I1.COMBINED_SUBCLASSCD ");
//                stb.add("   GROUP BY ");
//                stb.add("        T1.SCHOOLCD ");
//                stb.add("      , T1.YEAR ");
//                stb.add("      , T1.SCHREGNO ");
//                stb.add("      , T1.ANNUAL ");
//                stb.add("      , I1.COMBINED_CLASSCD ");
//                stb.add("      , I1.COMBINED_SCHOOL_KIND ");
//                stb.add("      , I1.COMBINED_CURRICULUM_CD ");
//                stb.add("      , I1.COMBINED_SUBCLASSCD ");
//                stb.add("      , I2.CLASSNAME ");
//                stb.add("      , I3.SUBCLASSNAME ");
            }

            stb.add(" ), ");

            stb.add(" STUDYREC_DAT0 AS(");
            stb.add("   SELECT ");
            stb.add("        T1.SCHREGNO");
            stb.add("      , CASE WHEN INT(T1.YEAR) = 0 THEN '0' ELSE T1.YEAR END AS YEAR");
            stb.add("      , CASE WHEN INT(T1.ANNUAL) = 0 THEN '0' ELSE T1.ANNUAL END AS ANNUAL");
            stb.add("      , T1.SCHOOLCD");
            stb.add("      , VALUE(T1.VALUATION, 0) AS GRADES");
            stb.add("      , CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0)");
            stb.add("             ELSE T1.GET_CREDIT END AS CREDIT");
            if (param._isNotCreditDefault0) {
                stb.add("      , T1.COMP_CREDIT AS COMP_CREDIT");
                stb.add("      , T1.GET_CREDIT AS GET_CREDIT");
                stb.add("      , T1.ADD_CREDIT AS ADD_CREDIT");
            } else {
                stb.add("      , VALUE(T1.COMP_CREDIT, 0) AS COMP_CREDIT");
                stb.add("      , VALUE(T1.GET_CREDIT, 0) AS GET_CREDIT");
                stb.add("      , VALUE(T1.ADD_CREDIT, 0) AS ADD_CREDIT");
            }
            if (param._z010.in(Z010Info.Hosei)) {
                stb.add("      , CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD");
            } else {
                stb.add("      , T1.CLASSCD");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      , T1.SCHOOL_KIND");
                stb.add("      , T1.CURRICULUM_CD");
            }
            stb.add("      , T1.SUBCLASSCD");
            stb.add("      , T1.CLASSNAME, T1.SUBCLASSNAME");
            stb.add("      , T1.VALID_FLG");
            if (param._isNotCreditDefault0) {
                stb.add("      , CRE_M.CREDITS AS CREDIT_MST_CREDIT ");
            } else {
                stb.add("      , VALUE(CRE_M.CREDITS, 0) AS CREDIT_MST_CREDIT ");
            }
            stb.add("   FROM  STUDYREC_DAT00 T1");
            stb.add("   LEFT JOIN MAX_SEMESTER0 T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.add("   LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T2.YEAR ");
            stb.add("       AND REGD.SEMESTER = T2.SEMESTER ");
            stb.add("       AND REGD.SCHREGNO = T2.SCHREGNO ");
            stb.add("   LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
            stb.add("       AND T1.ANNUAL BETWEEN A023.NAME2 AND A023.NAME3 ");
            stb.add("   LEFT JOIN CREDIT_MST CRE_M ON CRE_M.YEAR = REGD.YEAR ");
            stb.add("       AND CRE_M.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("       AND CRE_M.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.add("       AND CRE_M.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.add("       AND CRE_M.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.add("       AND CRE_M.COURSECD = REGD.COURSECD ");
            stb.add("       AND CRE_M.MAJORCD = REGD.MAJORCD ");
            stb.add("       AND CRE_M.COURSECODE = REGD.COURSECODE ");
            stb.add("       AND CRE_M.GRADE = REGD.GRADE ");
            stb.add("   WHERE  (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
            if (isOutput94) {
                stb.add("       OR T1.CLASSCD = '" + PrintClass.getHanasuClasscd(printData) + "' ");
            } else if (param._z010.in(Z010Info.Jisyuukan)) {
                stb.add("       OR T1.SUBCLASSCD = '941001' ");
            }
            if (!_90OverD077.isEmpty()) {
                stb.add("       OR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.add("       T1.SUBCLASSCD IN ('" + Util.mkString(_90OverD077, "','") + "') ");
            }
            if (!_90OverD081.isEmpty()) {
                stb.add("       OR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.add("       T1.SUBCLASSCD IN ('" + Util.mkString(_90OverD081, "','") + "') ");
            }
            if (!_90OverE065.isEmpty()) {
                stb.add("       OR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.add("       T1.SUBCLASSCD IN ('" + Util.mkString(_90OverE065, "','") + "') ");
            }
            stb.add("          )");
            if ("1".equals(printData.property(Property.tyousasyoNotPrintAnotherStudyrec))) {
                stb.add("       AND T1.SCHOOLCD <> '1' ");
            }
            stb.add("           AND VALUE(A023.NAME1, 'H') = 'H' ");
            if ("notPrint1".equals(_hyoutei)) {
                stb.add("         AND (T1.VALUATION IS NULL OR T1.VALUATION <> 1) ");
            }
            stb.add(" )");

            if (!StringUtils.isBlank(printData._tyousasyoPrintChairSubclassSemester2)) {
                // 講座名簿
                addSqlChairSubclass(printData, param, stb);
            }

            stb.add(" , STUDYREC_DAT AS(");
            stb.add("  SELECT ");
            stb.add("    '" + CommonSqlStudyrec.RECORD_FLG_00_STUDYREC + "' AS RECORD_FLG ");
            stb.add("    , T1.SCHREGNO");
            stb.add("    , T1.YEAR");
            stb.add("    , T1.ANNUAL");
            stb.add("    , T1.SCHOOLCD");
            stb.add("    , T1.GRADES");
            stb.add("    , T1.CREDIT");
            stb.add("    , T1.COMP_CREDIT");
            stb.add("    , T1.GET_CREDIT");
            stb.add("    , T1.ADD_CREDIT");
            stb.add("    , T1.CLASSCD, T1.SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("     , T1.SCHOOL_KIND");
                stb.add("     , T1.CURRICULUM_CD");
            }
            stb.add("    , T1.CLASSNAME, T1.SUBCLASSNAME");
            stb.add("    , T1.CREDIT_MST_CREDIT ");
            stb.add("    , VALUE(T1.VALID_FLG, '0') AS VALID_FLG ");
            stb.add("    , NMD065.NAME1 AS D065FLG ");
            stb.add("   FROM  STUDYREC_DAT0 T1");
            stb.add("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.add("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.add("       T1.SUBCLASSCD ");
            if (!StringUtils.isBlank(printData._tyousasyoPrintChairSubclassSemester2)) {
                stb.add("   UNION ALL ");
                stb.add("   SELECT ");
                stb.add("     '" + CommonSqlStudyrec.RECORD_FLG_01_CHAIR_SUBCLASS + "' AS RECORD_FLG ");
                stb.add("     , T1.SCHREGNO");
                stb.add("     , T1.YEAR");
                stb.add("     , T1.ANNUAL");
                stb.add("     , '0' AS SCHOOLCD");
                stb.add("     , CAST (NULL AS SMALLINT) AS GRADES");
                stb.add("     , T1.CREDITS AS CREDIT");
                stb.add("     , T1.CREDITS AS COMP_CREDIT");
                stb.add("     , T1.CREDITS AS GET_CREDIT");
                stb.add("     , 0 AS ADD_CREDIT");
                stb.add("     , T1.CLASSCD, T1.SUBCLASSCD");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("      , T1.SCHOOL_KIND");
                    stb.add("      , T1.CURRICULUM_CD");
                }
                stb.add("     , T1.CLASSNAME, T1.SUBCLASSNAME");
                stb.add("     , T1.CREDITS AS CREDIT_MST_CREDIT ");
                stb.add("     , '0' AS VALID_FLG ");
                stb.add("     , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.add("    FROM  CHAIR_STD_SUBCLASS_MAIN T1");
                stb.add("    WHERE ");
                stb.add("        NOT EXISTS (SELECT 'X' ");
                stb.add("            FROM STUDYREC_DAT0 ");
                stb.add("            WHERE SCHREGNO = T1.SCHREGNO ");
                stb.add("              AND YEAR = T1.YEAR ");
                stb.add("              AND ANNUAL = T1.ANNUAL ");
                stb.add("              AND SCHOOLCD = '0' ");
                stb.add("              AND CLASSCD = T1.CLASSCD ");
                stb.add("              AND (SUBCLASSCD = T1.SUBCLASSCD ");
                stb.add("                OR SUBCLASSCD = T1.CHAIR_SUBCLASSCD) ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.add("              AND SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.add("              AND CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.add("        ) ");

                stb.add("      AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                if (isOutput94) {
                    stb.add("       OR T1.CLASSCD = '" + PrintClass.getHanasuClasscd(printData) + "' ");
                } else if (param._z010.in(Z010Info.Jisyuukan)) {
                    stb.add("       OR T1.SUBCLASSCD = '941001' ");
                }
                if (!_90OverD077.isEmpty()) {
                    stb.add("       OR ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.add("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.add("       T1.SUBCLASSCD IN ('" + Util.mkString(_90OverD077, "','") + "') ");
                }
                if (!_90OverD081.isEmpty()) {
                    stb.add("       OR ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.add("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.add("       T1.SUBCLASSCD IN ('" + Util.mkString(_90OverD081, "','") + "') ");
                }
                stb.add("          )");
            }
            stb.add(" )");
        }

        /**
         * 年度/学年タイトルの SQL SELECT 文を戻します。
         * 学年制の場合、留年した年度は除外します。
         * @return
         */
        public List<String> getGakusekiSqlString(final CommonPrintData printData, final Param param, final boolean notContainSchoolcd1) {
            final List<String> stb = new LinkedList<String>();
            // 該当生徒の成績データ表
            stb.addAll(getStudyrecSqlString(printData, param));  // 調査書仕様の学習記録データの抽出

            final boolean useAnnualKey = printData.isGakunensei(param);
            stb.add(" , GAKUSEKI AS ( ");
            if (useAnnualKey) {
                stb.add(" SELECT T1.ANNUAL, T1.YEAR, T2.YEAR AS DROP_YEAR ");
                stb.add("     FROM   SCHREG_REGD_DAT T1 ");
                stb.add("     LEFT JOIN DROP_YEAR T2 ON T2.YEAR = T1.YEAR ");
                stb.add("     WHERE  T1.SCHREGNO = '" + printData._schregno + "' ");
                stb.add("        AND T1.YEAR <= '" + printData._year + "' ");
            } else {
                stb.add(" SELECT MAX(ANNUAL) AS ANNUAL, YEAR, CAST(NULL AS VARCHAR(1)) AS DROP_YEAR ");
                stb.add("     FROM   SCHREG_REGD_DAT");
                stb.add("     WHERE  SCHREGNO = '" + printData._schregno + "' ");
                stb.add("        AND YEAR <= '" + printData._year + "' ");
                stb.add("     GROUP BY YEAR ");
            }
            stb.add(" UNION ");
            if (useAnnualKey) {
                stb.add(" SELECT T1.ANNUAL, T1.YEAR, T2.YEAR AS DROP_YEAR ");
                stb.add("     FROM ( ");
                stb.add("        SELECT ANNUAL, YEAR FROM STUDYREC ");
                if (notContainSchoolcd1) {
                    stb.add("       WHERE SCHOOLCD <> '1' ");
                }
                stb.add("        UNION ");
                stb.add("        SELECT ANNUAL, YEAR FROM SCHREG_ATTENDREC_DAT ");
                stb.add("        WHERE  SCHREGNO = '" + printData._schregno + "' ");
                stb.add("           AND YEAR <= '" + printData._year + "' ");
                if ("on".equals(printData.property(Property.tyousasyoNotPrintAnotherAttendrec)) || notContainSchoolcd1) {
                    stb.add("       AND SCHOOLCD <> '1' ");
                }
                stb.add("         ) T1 ");
                stb.add("     LEFT JOIN DROP_YEAR T2 ON T2.YEAR = T1.YEAR ");
            } else {
                stb.add(" SELECT MAX(T1.ANNUAL) AS ANNUAL, T1.YEAR, CAST(NULL AS VARCHAR(1)) AS DROP_YEAR ");
                stb.add("     FROM ( ");
                stb.add("        SELECT ANNUAL, YEAR FROM STUDYREC ");
                if (notContainSchoolcd1) {
                    stb.add("       WHERE SCHOOLCD <> '1' ");
                }
                stb.add("        UNION ");
                stb.add("        SELECT ANNUAL, YEAR FROM SCHREG_ATTENDREC_DAT ");
                stb.add("        WHERE  SCHREGNO = '" + printData._schregno + "' ");
                stb.add("           AND YEAR <= '" + printData._year + "' ");
                if ("on".equals(printData.property(Property.tyousasyoNotPrintAnotherAttendrec)) || notContainSchoolcd1) {
                    stb.add("       AND SCHOOLCD <> '1' ");
                }
                stb.add("         ) T1 ");
                stb.add("     GROUP BY T1.YEAR ");
            }
            stb.add(" ) ");
            if (useAnnualKey) {
                stb.add(" SELECT T1.ANNUAL, T1.YEAR, T1.DROP_YEAR, T2.GRADE_CD ");
                stb.add("     FROM   GAKUSEKI T1 ");
                stb.add("     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + printData._year + "' AND T2.GRADE = T1.ANNUAL ");
                stb.add("     WHERE T1.ANNUAL NOT IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND IN ('P', 'J')) ");
            } else {
                stb.add(" SELECT MAX(T1.ANNUAL) AS ANNUAL, T1.YEAR, CAST(NULL AS VARCHAR(1)) AS DROP_YEAR, MAX(T2.GRADE_CD) AS GRADE_CD ");
                stb.add("     FROM   GAKUSEKI T1 ");
                stb.add("     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + printData._year + "' AND T2.GRADE = T1.ANNUAL ");
                stb.add("     WHERE T1.ANNUAL NOT IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND IN ('P', 'J')) ");
                stb.add("     GROUP BY T1.YEAR ");
            }
            stb.add(" ORDER BY T1.YEAR ");
            return stb;
        }
    }

    protected static class GakunenSeiseki {

        final Param _param;
        Map<SubclassMst, Map<String, Integer>> _gakunenSeisekiMap;

        GakunenSeiseki(final Param param) {
            _param = param;
        }

        public static GakunenSeiseki load(final DB2UDB db2, final Param param, final CommonPrintData printData) {
            final GakunenSeiseki c = new GakunenSeiseki(param);

            final String psKey = "PS_GAKUNENSEISEKI";
            if (null == param.getPs(psKey)) {
                final String sql = sqlGakunenSeiseki(param);
                param.setPs(db2, psKey, sql);
            }
            Map<SubclassMst, Map<String, Integer>> subclassYearGakunenSeisekiMap = new TreeMap<SubclassMst, Map<String, Integer>>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { printData._year, printData._schregno})) {
                final SubclassMst mst = SubclassMst.getSubclassMst(param._subclassMstMap, SubclassMst.key(param, KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CURRICULUM_CD"), KnjDbUtils.getString(row, "SUBCLASSCD")));
                if (null == mst) {
                    continue;
                }
                final String year = KnjDbUtils.getString(row, "YEAR");
                final Integer score = Integer.valueOf(KnjDbUtils.getString(row, "SCORE"));
                Util.getMappedMap(subclassYearGakunenSeisekiMap, mst).put(year, score);
            }
            if (param._isOutputDebug) {
                log.info(" subclassYearGakunenSeisekiMap = " + subclassYearGakunenSeisekiMap);
            }
            c._gakunenSeisekiMap = subclassYearGakunenSeisekiMap;

            return c;
        }

        public String getGakunenSeisekiString(final SubclassMst mst, final String year) {
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

    protected static class PrintClass {

        final String _classcd;
        String _sqlclassname;
        boolean _sqlCreditIsNendogoto;
        String _printSubclassname;
        boolean _isPrintWhenHasCredit; // 単位がある場合のみ出力する
        boolean _notUseRecordSubclassname; // 科目名は固定

        boolean _setCredit;
        String _credit;

        public PrintClass(final String classcd) {
            _classcd = classcd;
        }
//    	public String getCredit(final PrintData printData) {
//    		return null == printData._hanasuClass1 ? null : printData._hanasuClass1._credit;
//    	}
//    	public static List hanasuClassnames2() {
//			return Arrays.asList(new String[] {tokiwahr,                 bunkyoLhr, rakunanLhr, higashiosakaKeiaiLhr ,kyotoJiritsu});
//		}
        public static List<String> hanasuClassnames(final CommonPrintData printData) {
            if (null != printData._hanasuClass1 && null != printData._hanasuClass1._sqlclassname) {
                return Arrays.asList(printData._hanasuClass1._sqlclassname);
            }
            return Arrays.asList();
        }
//		public static boolean nishiyamaIgai(final Param param) {
//    		return param._isTokiwa || param._isBunkyo || param._isRakunan || param._isHigashiosakaKeiai || param._isKyoto;
//		}
//    	public static boolean is94_(final Param param) {
//    		return param._isNishiyama || param._isTokiwa || param._isBunkyo || param._isRakunan || param._isHigashiosakaKeiai || param._isKyoto;
//    	}
        public static boolean is94_(final CommonPrintData printData) {
            return null != printData._hanasuClass1;
        }
//    	public static boolean isSql94_2(final Param param) {
//    		return param._isNishiyama || param._isTokiwa || param._isBunkyo || param._isRakunan || param._isKyoto;
//    	}
        public static String getHanasuClasscd(CommonPrintData printData) {
            return null == printData._hanasuClass1 ? null : printData._hanasuClass1._classcd;
        }
        public static String cd94name(final CommonPrintData printData) {
            if (null == printData._hanasuClass1) {
                return null;
            }
            if (CommonPrintData._shingakuYou != printData._output) {
                return null;
            }
            return printData._hanasuClass1._sqlclassname;
        }
        public static PrintClass create(final Param param, final CommonPrintData printData) {
            PrintClass hanasuClass1 = null;
            if (!StringUtils.isBlank(printData.getParameter(Parameter.tyousasyoHanasuClasscd))) {
                if (printData._notUsePrintClass) {
                    log.info(" no use PrintClass : val = " + printData.getParameter(Parameter.tyousasyoHanasuClasscd));
                } else {
                    final String classcd = printData.getParameter(Parameter.tyousasyoHanasuClasscd);
                    hanasuClass1 = new PrintClass(classcd);
                    hanasuClass1._sqlclassname = "propHanasuClasscd" + classcd;
                    hanasuClass1._isPrintWhenHasCredit = true;
                }
            } else {
                if (param._z010.in(Z010Info.Tokiwa)) {
                    hanasuClass1 = new PrintClass(_94);
                    hanasuClass1._sqlclassname = "tokiwahr";
                    hanasuClass1._notUseRecordSubclassname = true;
                    hanasuClass1._printSubclassname = "特別活動　　ﾎｰﾑﾙｰﾑ";
                } else if (param._z010.in(Z010Info.Nishiyama)) {
                    hanasuClass1 = new PrintClass(_94);
                    hanasuClass1._sqlclassname = "nishiyamaLhr";
                    hanasuClass1._sqlCreditIsNendogoto = true;
                    hanasuClass1._notUseRecordSubclassname = true;
                    hanasuClass1._printSubclassname = "特別活動　　ＬＨＲ";
                } else if (param._z010.in(Z010Info.Bunkyo)) {
                    hanasuClass1 = new PrintClass(_94);
                    hanasuClass1._sqlclassname = "bunkyoLhr";
                    hanasuClass1._notUseRecordSubclassname = true;
                    hanasuClass1._printSubclassname = "ＬＨＲ";
                } else if (param._z010.in(Z010Info.Rakunan)) {
                    hanasuClass1 = new PrintClass(_95);
                    hanasuClass1._sqlclassname = "rakunanLhr";
                    hanasuClass1._notUseRecordSubclassname = true;
                    hanasuClass1._printSubclassname = "ＬＨＲ";
                } else if (printData._isKyoto && printData._output == CommonPrintData._shingakuYou) {
                    hanasuClass1 = new PrintClass(_88);
                    hanasuClass1._sqlclassname = "kyotoJiritsu";
                    hanasuClass1._isPrintWhenHasCredit = true;
                } else if (param._isHigashiosakaKeiai) {
                    hanasuClass1 = new PrintClass(_94);
                    hanasuClass1._sqlclassname = "higashiosakaKeiaiLhr";
                    hanasuClass1._notUseRecordSubclassname = true;
                    hanasuClass1._printSubclassname = "ＬＨＲ";
                } else if (param._z010.in(Z010Info.Hirokoku)) {
                    hanasuClass1 = new PrintClass(_94);
                    hanasuClass1._sqlclassname = CommonSqlStudyrec.hirokokulhr;
                    hanasuClass1._notUseRecordSubclassname = true;
                    hanasuClass1._printSubclassname = "ＬＨＲ";
                }
            }

            return hanasuClass1;
        }

        public static boolean isHanasu(final PrintClass printClass, final String classcd) {
            return null != printClass && printClass._classcd.equals(classcd);
        }

        public String toString() {
            return "PrintClass (classcd=" + _classcd + ", printSubclassname=" + _printSubclassname + ", sqlclassname = " + _sqlclassname + ")";
        }
    }

    protected static class SchoolInfo {

        final String _staffCd;
        final String _certifSchoolOnly;

        protected String anname;                          //学年・年次名称
        protected String shoshoname;  // 証書番号
        protected String shoshoname2;
        protected String remark5;  // 備考５

        protected boolean _isOutputCertifNo;
        protected String _syosyoNameField;

        boolean hasHeaddata;
        boolean isPrintSchoolRemark;
        String schoolAddr = null;
        String schooltelo;  // 学校電話番号
        String schoolname1;  // 学校名
        String staff2Name;
        String staff2Jobname;
        String[] certifSchoolRemark = new String[10 + 1];

        String t4schoolname1 = null;
        String schoolzipcd = null;
        String t4classification = null;
        String kisaibi = null;
        String principalName = null;
        String principalJobName = null;

        SchoolInfo(final String staffCd, final String certifSchoolOnly) {
            _staffCd = staffCd;
            _certifSchoolOnly = certifSchoolOnly;
        }

        /**
         *  学校情報
         */
        public void loadSchoolInfo(final DB2UDB db2, final Param param, final String _date, final String _ctrlYear, final String _year,
                final String certifkind, final String certifkind2, final String tyousasyo2020, final boolean isGakunensei) {
            final String psKey = "ps7";
            final boolean notHasCertifSchoolDatOrKindai = param.notHasCertifSchoolDatOrKindai();
            if (null == param.getPs(psKey)) {
                String sql7;
                // 学校データ
                if (notHasCertifSchoolDatOrKindai) {
                    final servletpack.KNJZ.detail.KNJ_SchoolinfoSql o = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("10100");
                    final Map paramMap = new HashMap();
                    paramMap.put("schoolMstSchoolKind", param._tableInfo._hasSCHOOL_MST_SCHOOL_KIND ? "H" : null);
                    sql7 = o.pre_sql(paramMap); //
                } else {
                    final servletpack.KNJG.detail.KNJ_SchoolinfoSql o = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("10100");
                    o.setHasCertifSchoolName(true);
                    try {
                        final Map paramMap = new HashMap();
                        paramMap.put("useZdetail", notHasCertifSchoolDatOrKindai ? "1" : null);
                        paramMap.put("schoolMstSchoolKind", param._tableInfo._hasSCHOOL_MST_SCHOOL_KIND ? "H" : null);
                        sql7 = o.pre_sql(paramMap);
                    } catch (Throwable t) {
                        log.warn(" old SchoolinfoSql :" + t);
                        sql7 = o.pre_sql();
                    }
                }
                param.setPs(db2, psKey, sql7);
            }
            final Object[] qparam;
            final String dateYear = ((_date != null) ? KNJG010_1.b_year(_date) : _ctrlYear);
            if (notHasCertifSchoolDatOrKindai) {
                qparam = new Object[] {_ctrlYear, dateYear, _year};
            } else {
                qparam = new Object[] {_ctrlYear, certifkind, certifkind2, dateYear, _year, certifkind};
            }
            if ("1".equals(tyousasyo2020)) {
                _syosyoNameField = "SYOSYO_NAME_2020";
            } else {
                _syosyoNameField = "SYOSYO_NAME_L";
            }

            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), qparam);
            if (rowList.size() > 0) {
                final Map<String, String> row = rowList.get(0);

                hasHeaddata = true;
                t4schoolname1 = getString(row, "T4SCHOOLNAME1");
                schoolname1 = defstr(getString(row, "SCHOOLNAME1"));
                schoolzipcd = getString(row, "SCHOOLZIPCD");
                t4classification = defstr(getString(row, "T4CLASSIFICATION"));
                if (_date != null) {
                    kisaibi = param._isSeireki ? Util.h_format_Seireki(_date) : KNJ_EditDate.h_format_JP(db2, _date);
                } else {
                    if (param._z010.in(Z010Info.naraken)) {
                        final String[] hoge = KNJ_EditDate.tate_format4(db2, String.valueOf(Integer.parseInt(_ctrlYear) + 1) + "-03-31");
                        final String gengou = null != hoge && hoge.length >= 1 ? defstr(hoge[0], "　　") : "　　";
                        kisaibi = gengou  + "　年　月　日";
                    } else {
                        kisaibi = "　　年　 月　 日";
                    }
                }
                principalName = defstr(getString(row, "PRINCIPAL_NAME"));
                principalJobName = defstr(getString(row, "PRINCIPAL_JOBNAME"));

                anname = isGakunensei || param._z010.in(Z010Info.Sapporo) ? "学年" : "年次";

                if (notHasCertifSchoolDatOrKindai) {
                } else {
                    shoshoname = getString(row, "SYOSYO_NAME");  // 証書番号
                    remark5 = getString(row, "REMARK5");  // 備考５
                    final String remark10 = getString(row, "REMARK10");
                    if (!"1".equals(tyousasyo2020)) {
                        if ("1".equals(remark10) || "2".equals(remark10)) {
                            _syosyoNameField = "SYOSYO_NAME_R";
                        }
                    }
                    if (getString(row, "SYOSYO_NAME") != null) {
                        shoshoname = getString(row, "SYOSYO_NAME");
                    }
                    if (getString(row, "SYOSYO_NAME2") != null) {
                        shoshoname2 = getString(row, "SYOSYO_NAME2");
                    }
                    if ("0".equals(getString(row, "CERTIF_NO"))) {
                        _isOutputCertifNo = true;  //証書番号の印刷 0:あり,1:なし
                    }
                    for (int i = 1; i <= 10; i++) {
                        certifSchoolRemark[i] = getString(row, "REMARK" + i);
                    }
                }

                final StringBuffer stb = new StringBuffer();
                if (getString(row, "SCHOOLADDR1") != null) stb.append(getString(row, "SCHOOLADDR1"));
                if (getString(row, "SCHOOLADDR2") != null) stb.append(getString(row, "SCHOOLADDR2"));
                schoolAddr = stb.toString();

                schooltelo = getString(row, "SCHOOLTELNO");  // 学校電話番号
            }
            if (param._isOutputDebug) {
                log.info(Util.debugArrayToStr("CERTIF_SCHOOL_DAT REMARK", certifSchoolRemark));
            }

            if (hasHeaddata) {
                setStaff(db2, _year);
            }
        }

        public void setStaff(final DB2UDB db2, final String _year) {
            if (null == _staffCd || "1".equals(_certifSchoolOnly)) {
                return;
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   W1.STAFFNAME ");
            stb.append(" , W1.STAFFNAME_REAL ");
            stb.append(" , (CASE WHEN W2.STAFFCD IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME ");
            stb.append(" , W2.NAME_OUTPUT_FLG ");
            stb.append(" , W3.JOBNAME ");
            stb.append(" FROM ");
            stb.append("   STAFF_MST W1 ");
            stb.append("   LEFT JOIN STAFF_NAME_SETUP_DAT W2 ON W2.YEAR = '" + _year + "' AND W2.STAFFCD = W1.STAFFCD AND W2.DIV = '01' ");
            stb.append("   LEFT JOIN JOB_MST W3 ON W3.JOBCD = W1.JOBCD ");
            stb.append(" WHERE ");
            stb.append("   W1.STAFFCD = '" + _staffCd  + "' ");

            final List rowList = KnjDbUtils.query(db2, stb.toString());
            if (rowList.size() > 0) {
                final Map row = (Map) rowList.get(0);
                if ("1".equals(getString(row, "USE_REAL_NAME")) && !StringUtils.isBlank(getString(row, "STAFFNAME_REAL"))) {
                    if ("1".equals(getString(row, "NAME_OUTPUT_FLG")) && !StringUtils.isBlank(getString(row, "STAFFNAME"))) {
                        staff2Name = getString(row, "STAFFNAME_REAL") + "（" + getString(row, "STAFFNAME") + "）";
                    } else {
                        staff2Name = getString(row, "STAFFNAME_REAL");
                    }
                } else {
                    staff2Name = getString(row, "STAFFNAME");
                }
                staff2Jobname = getString(row, "JOBNAME");
            }
        }
    }

    protected static class Form {
        private final Param _param;
        protected Vrw32alp _svf;
        String _currentForm;
        protected List<List<String>> _csvOutputLines;
        protected Set<String> _notFoundFieldname = new TreeSet<String>();
        boolean _isCsv;

        Form(final Param param) {
            _param = param;
        }

        private Param param() {
            return _param;
        }

        public void setForm(final Vrw32alp svf, final String form, final int n, final Param param) throws FileNotFoundException {
            if (param._isOutputDebug || param._isOutputDebugSvfForm || param._isOutputDebugSvfFormCreate || param._isOutputDebugSvfFormModify) {
                log.info(" setForm " + form);
            }
            final int rtnSetForm = svf.VrSetForm(form, n);
            if (rtnSetForm < 0) {
                throw new FileNotFoundException(form);
            }
            _currentForm = form;
            if (null != form && null == param.formFieldInfoMap().get(_currentForm)) {
                param.formFieldInfoMap().put(_currentForm, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
                //debugFormInfo(param);
            }
            _notFoundFieldname.clear();
        }

        protected int svfVrsOut(final String field, final String data) {
            if (null == field) {
                return 0;
            }
            if (!formHasField(field)) {
                _notFoundFieldname.add(field);
                if (param()._isOutputDebugField) {
                    log.warn("フィールドがない: " + field + ", data = " + data);
                }
                return -1;
            }
            int rtn = _svf.VrsOut(field, data);
            if (param().isOutputDebugVrsout(field)) {
                if (rtn < 0) {
                    log.warn("svf.VrsOut(\"" + field + "\", " + data + ") ==> " + rtn);
                } else if (param().isOutputDebugField(field)) {
                    log.info("svf.VrsOut(\"" + field + "\", " + data + ") ==> " + rtn);
                }
            }
            return rtn;
        }

        protected int svfVrsOutForData(final List<String> fields, final String data, final boolean checkUnicodeChars) {
            return svfVrsOut(getFieldForData(fields, data, checkUnicodeChars), data);
        }

        protected String getFieldForData(final List<String> fields, final String data, final boolean checkUnicodeChars) {
            final int dataKeta;
            if (checkUnicodeChars) {
                dataKeta = getTextKeta(data);
            } else {
                dataKeta = getMS932ByteLength(data);
            }
            String formFieldName = null;
            String ketteiField = null;
            String kouho = null;
            for (int i = 0; i < fields.size(); i++) {
                final int fieldKeta = getFieldKeta(fields.get(i));
                if (0 < fieldKeta) {
                    if (dataKeta <= fieldKeta) {
                        ketteiField = fields.get(i);
                        break;
                    }
                    kouho = fields.get(i);
                }
            }
            if (null != ketteiField) {
                formFieldName = ketteiField;
            } else if (null != kouho) {
                formFieldName = kouho;
            }
            if (_param._isOutputDebugField) {
                log.info(" fields " + fields + ", field = " + formFieldName + " (ketteiField = " + ketteiField + ", kouho = " + kouho + ", data = " + data + ", keta = " + dataKeta + ")");
            }
            return formFieldName;
        }

        protected int svfVrAttributeUchikeshi(final String field, final int keta) {
            return svfVrAttribute(field, "UnderLine=(0,3,5), Keta=" + String.valueOf(keta));
        }

        protected int svfVrAttribute(final String field, final String attr) {
            if (null == field) {
                return 0;
            }
            if (!formHasField(field)) {
                _notFoundFieldname.add(field);
                if (param()._isOutputDebugField) {
                    log.warn("フィールドがない: " + field + ", attribute = " + attr);
                }
                return -1;
            }
            int rtn = _svf.VrAttribute(field, attr);
            if (param().isOutputDebugVrsout(field)) {
                if (rtn < 0) {
                    log.warn("svf.VrAttribute(\"" + field + "\", " + attr + ") ==> " + rtn);
                } else if (param().isOutputDebugField(field)) {
                    log.info("svf.VrAttribute(\"" + field + "\", " + attr + ") ==> " + rtn);
                }
            }
            return rtn;
        }
        protected String attributeIntPlus(final String fieldname, final String intProperty, final int plus) {
            SvfField field = getField(fieldname);
            if (!formHasField(fieldname)) {
                log.warn(" not found " + fieldname + " attribute " + intProperty);
                return null;
            }
            final int propVal = Util.toInt((String) field.getAttributeMap().get(intProperty), 10000);
            return intProperty + "=" + String.valueOf(propVal + plus);
        }
        protected int svfVrsOutn(final String field, final int gyo, final String data) {
            if (null == field) {
                return 0;
            }
            if (!formHasField(field)) {
                _notFoundFieldname.add(field);
                if (param()._isOutputDebugField) {
                    log.warn("フィールドがない: " + field + ", " + gyo + ", data = " + data);
                }
                return -1;
            }
            int rtn = _svf.VrsOutn(field, gyo, data);
            if (param().isOutputDebugVrsout(field)) {
                if (rtn < 0) {
                    log.warn("svf.svfVrsOutn(\"" + field + "\", " + gyo + ", " + data + ") ==> " + rtn);
                } else if (param().isOutputDebugField(field)) {
                    log.info("svf.svfVrsOutn(\"" + field + "\", " + gyo + ", " + data + ") ==> " + rtn);
                }
            }
            return rtn;
        }
        protected int svfVrsOutn(final String field, final int gyo, final String data, final boolean check) {
            if (check == false) {
                if (param().isOutputDebugVrsout(field)) {
                    log.info("svf.svfVrsOutn : notPrint (\"" + field + "\", " + gyo + ", " + data + ")");
                }
                return -1;
            }
            return svfVrsOutn(field, gyo, data);
        }

        protected int svfVrAttributen(final String field, final int gyo, final String attr) {
            if (null == field) {
                return 0;
            }
            if (!formHasField(field)) {
                _notFoundFieldname.add(field);
                if (param()._isOutputDebugField) {
                    log.warn("フィールドがない: " + field + ", " + gyo + ", attribute = " + attr);
                }
                return -1;
            }
            int rtn = _svf.VrAttributen(field, gyo, attr);
            if (param().isOutputDebugVrsout(field)) {
                if (rtn < 0) {
                    log.warn("svf.VrAttributen(\"" + field + "\", " + gyo + ", " + attr + ") ==> " + rtn);
                } else if (param().isOutputDebugField(field)) {
                    log.info("svf.VrAttributen(\"" + field + "\", " + gyo + ", " + attr + ") ==> " + rtn);
                }
            }
            return rtn;
        }
        protected int svfVrImageOut(final String field, final String path) {
            if (null == field || null == path) {
                return 0;
            }
            if (!formHasField(field)) {
                _notFoundFieldname.add(field);
                if (param()._isOutputDebugField) {
                    log.warn("イメージフィールドがない: " + field + ", path = " + path);
                }
                return -1;
            }
            int rtn = _svf.VrsOut(field, path);
            if (param().isOutputDebugVrsout(field)) {
                if (rtn < 0) {
                    log.warn("svf.VrsOut(\"" + field + "\", " + path + ") ==> " + rtn);
                } else {
                    log.info("svf.VrsOut(\"" + field + "\", " + path + ") ==> " + rtn);
                }
            }
            return rtn;
        }
        protected int svfVrImageOutn(final String field, final int g, final String path) {
            if (null == field || null == path) {
                return 0;
            }
            if (!formHasField(field)) {
                _notFoundFieldname.add(field);
                if (param()._isOutputDebugField) {
                    log.warn("イメージフィールドがない: " + field + ", path = " + path);
                }
                return -1;
            }
            int rtn = _svf.VrsOutn(field, g, path);
            if (param().isOutputDebugVrsout(field)) {
                if (rtn < 0) {
                    log.warn("svf.VrsOutn(\"" + field + "\", " + g + ", " + path + ") ==> " + rtn);
                } else {
                    log.info("svf.VrsOutn(\"" + field + "\", " + g + ", " + path + ") ==> " + rtn);
                }
            }
            return rtn;
        }
        protected int svfVrEndRecord() {
            final int rtn = _svf.VrEndRecord();
            if (param().isOutputDebugVrsout(null)) {
                log.info("svf.VrEndRecord() ==> " + rtn);
            }
            return rtn;
        }

        public void debugFormInfo(final Param param) {
            if (null == _currentForm || null == param.formFieldInfoMap().get(_currentForm)) {
                return;
            }
            final Map fieldInfo = (Map) param.formFieldInfoMap().get(_currentForm);
            for (final Iterator it = fieldInfo.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final SvfField field = (SvfField) e.getValue();
                log.debug(" field  = " + field);
            }
        }

        public boolean formHasField(final String fieldRegex) {
            return null != getField(fieldRegex);
        }

        public SvfField getField(final String fieldRegex) {
            if (_isCsv) {
                return null;
            }
            SvfFieldAreaInfo.Param p = new SvfFieldAreaInfo.Param();
            p._isOutputDebug = param()._isOutputDebugField && param().isOutputDebugField(fieldRegex);
            final List<SvfField> fieldList = Util.getMappedList(SvfFieldAreaInfo.getSearchFieldResult(p, param().formFieldInfoMap(), _currentForm, fieldRegex, "Regex", "SvfField"), "resultList");
            if (fieldList.size() > 0) {
                return fieldList.get(0);
            }
            return null;
        }

        public List<SvfField> searchField(final String fieldRegex) {
            if (_isCsv) {
                return Collections.emptyList();
            }
            SvfFieldAreaInfo.Param p = new SvfFieldAreaInfo.Param();
            p._isOutputDebug = param()._isOutputDebugField && param().isOutputDebugField(fieldRegex);
            final List<SvfField> fieldList = Util.getMappedList(SvfFieldAreaInfo.getSearchFieldResult(p, param().formFieldInfoMap(), _currentForm, fieldRegex, "Regex", "SvfField"), "resultList");
            return fieldList;
        }

        public int getFieldKeta(final String fieldRegex) {
            final SvfField field = getField(fieldRegex);
            if (null == field) {
                return 0;
            }
            return field._fieldLength;
        }
    }

    protected static class FormRecord {
        private static final String Data = "Data";
        private static final String Attribute = "Atrribute";
        public int _debugIdx = -1;
        final Map<String, String> _dataMap = new HashMap<String, String>();
        final Map<Tuple<String, Integer>, String> _datanMap = new HashMap<Tuple<String, Integer>, String>();
        final Map<String, List<String>> _attrMap = new HashMap<String, List<String>>();
        final Map<Tuple<String, Integer>, List<String>> _attrnMap = new HashMap<Tuple<String, Integer>, List<String>>();
        public void setData(final String field, final String data) {
            if (null != field) {
                _dataMap.put(field, data);
            }
        }
        public void setImage(final String field, final String path) {
            setData(field, path);
        }
        public FormRecord merge(final FormRecord formRecord) {
            // TODO: キーチェック
            FormRecord rtn = new FormRecord();
            rtn._dataMap.putAll(_dataMap);
            rtn._dataMap.putAll(formRecord._dataMap);
            rtn._datanMap.putAll(_datanMap);
            rtn._datanMap.putAll(formRecord._datanMap);
            rtn._attrMap.putAll(_attrMap);
            rtn._attrMap.putAll(formRecord._attrMap);
            rtn._attrnMap.putAll(_attrnMap);
            rtn._attrnMap.putAll(formRecord._attrnMap);
            return rtn;
        }
        public void addAttr(final String field, final String attr) {
            if (null != field) {
                Util.getMappedList(_attrMap, field).add(attr);
            }
        }
        public void setDatan(final String field, final int gyo, final String data) {
            if (null != field) {
                _datanMap.put(Tuple.of(field, gyo), data);
            }
        }
        public void setDatan(final String field, final int gyo, final String data, final boolean validates) {
            if (!validates) {
                return;
            }
            setDatan(field, gyo, data);
        }
        public void addAttrn(final String field, final int gyo, final String attr) {
            if (null != field) {
                Util.getMappedList(_attrnMap, Tuple.of(field, gyo)).add(attr);
            }
        }
        public static FormRecord createRecord(final List<FormRecord> recordList) {
            final FormRecord record = new FormRecord();
            record._debugIdx = recordList.size();
            recordList.add(record);
            return record;
        }
        public String toString() {
            return "FormRecord(" + _dataMap + ")";
        }
        public boolean hasData() {
            boolean hasData = false;
            if (!hasData) {
                for (final String data : _dataMap.values()) {
                    if (!StringUtils.isBlank(data)) {
                        hasData = true;
                        break;
                    }
                }
            }
            if (!hasData) {
                for (final String data : _datanMap.values()) {
                    if (!StringUtils.isBlank(data)) {
                        hasData = true;
                        break;
                    }
                }
            }
            return hasData;
        }
    }

    protected static class KNJSvfFieldInfo {
        int _x1;   //開始位置X(ドット)
        int _x2;   //終了位置X(ドット)
        int _height;  //フィールドの高さ(ドット)
        int _ystart;  //開始位置Y(ドット)
        int _minnum;  //最小設定文字数
        int _maxnum;  //最大設定文字数
        public KNJSvfFieldInfo() {
            _x1 = -1;
            _x2 = -1;
            _height = -1;
            _ystart = -1;
            _minnum = -1;
            _maxnum = -1;
        }
        public int getWidth() {
            return _x2 - _x1;
        }
        public String toString() {
            return "KNJSvfFieldInfo(x:" + _x1 + ", x2:" + _x2 + ", height:" + _height + ", ystart:" + _ystart + ", minnum:" + _minnum + ", maxnum:" + _maxnum + ")";
        }
    }


    //--- 内部クラス -------------------------------------------------------
    protected static class KNJSvfFieldModify {

        private final String _fieldname; // フィールド名
        private final double _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        private static final double dpi = 400.0;
        private static final double pointPerInch = 72;

        public KNJSvfFieldModify(String fieldname, double width, int height, int ystart, int minnum, int maxnum) {
            _fieldname = fieldname;
            _width = width;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        /**
         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
         * @param posx1 フィールドの左端X
         * @param posx2 フィールドの右端X
         * @param num フィールド指定の文字数
         * @param charSize 変更後の文字サイズ
         * @return ずれ幅の値
         */
        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, double charSize) {
//            final int maxWidth = (int) getStringLengthPixel(charSize, num, 0); // 文字の大きさを考慮したフィールドの最大幅
            final int maxWidth = (int) fieldWidth(charSize, 0, num); // 文字の大きさを考慮したフィールドの最大幅
            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2;
            return offset;
        }

//        private static double getStringLengthPixel(final double charSize, final int num, final int upperOrLower) {
////            return charSizeToPixel(charSize) * num / 2;
//            return charPointToPixel(charSize, upperOrLower) * num / 2;
//        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final SvfField field, final int upperOrLower) {
            final double charSize = Double.parseDouble((String) field.getAttributeMap().get(SvfField.AttributeSize));
            final int keta = Integer.parseInt((String) field.getAttributeMap().get(SvfField.AttributeKeta));
            double width = fieldWidth(charSize, upperOrLower, keta);
//			log.info(" charSize = " + charSize + ", keta = " + keta + ", field width = " + width);
            return width;
        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final double charSize, final int upperOrLower, final int keta) {
            return SvfFieldAreaInfo.KNJSvfFieldModify.fieldWidth("", charSize, upperOrLower, keta);
            //return charPointToPixel(charSize, upperOrLower) * keta / 2;
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
        public double getCharSize(String str) {
            return Math.min(pixelToCharPoint(_height), retFieldPoint(_width, getStringByteSize(str)));                  //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        public int getStringByteSize(String str) {
            return Math.min(Math.max(getMS932ByteLength(str), _minnum), _maxnum);
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charPoint 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static double charPointToPixel(final double charPoint, final int upperOrLower) {
            return SvfFieldAreaInfo.KNJSvfFieldModify.charPointToPixel("", charPoint, upperOrLower);
            //return charPointEnabled(charPoint, upperOrLower) * dpi / pointPerInch;
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
        public float getYjiku(final int hnum, final double charSize) {
            float jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
            return jiku;
        }

        /**
         *  文字サイズを設定
         */
        public static double retFieldPoint(double width, int num) {
            return Math.round((double) width / (num / 2 + (num % 2 == 0 ? 0 : 1)) * pointPerInch / dpi * 10) / 10;
        }

        public static double charHeightPixel(final double charSize) {
            return charPointToPixel(charSize, 0);
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        public static float retFieldY(int height, double charSize) {
            return SvfFieldAreaInfo.KNJSvfFieldModify.retFieldY(height, charSize);
        }

        public String toString() {
            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    protected static class Year implements Comparable<Year> {
        private final String _val;
        public static Year of(final String val) {
            return new Year(val);
        }
        private Year(final String val) {
            _val = val;
        }
        public String value() {
            return _val;
        }
        public int compareTo(final Year year) {
            return StringUtils.defaultString(_val).compareTo(StringUtils.defaultString(year._val));
        }
        public int hashCode() {
            return toString().hashCode() * 2027;
        }
        public boolean equals(final Object o) {
            if (o instanceof Year) {
                return 0 == compareTo((Year) o);
            }
            return false;
        }
        public String toString() {
            return "Year(" + _val + ")";
        }
    }

    protected static class Seq implements Comparable<Seq> {
        private final String _val;
        public static Seq of(final String val) {
            return new Seq(val);
        }
        private Seq(final String val) {
            _val = val;
        }
        public String value() {
            return _val;
        }
        public int compareTo(final Seq seq) {
            return StringUtils.defaultString(_val).compareTo(StringUtils.defaultString(seq._val));
        }
        public int hashCode() {
            return toString().hashCode() * 2029;
        }
        public boolean equals(final Object o) {
            if (o instanceof Seq) {
                return 0 == compareTo((Seq) o);
            }
            return false;
        }
        public String toString() {
            return "Seq(" + _val + ")";
        }
    }

    protected static class ClassMst {
        private static final ClassMst ABROAD = new ClassMst("AA", "AA", _ABROAD, _ABROAD, "0", new Integer(0));
        private static final ClassMst Null = new ClassMst("XX", "XX", null, null, "9", new Integer(99999999));
        final String _classcd;
        final String _schoolKind;
        final String _classname;
        final String _classordername1;
        final String _specialDiv;
        final Integer _showorder2;

        protected ClassMst(final String classcd, final String schoolKind, final String classname, final String classordername1, final String specialdiv, final Integer showorder2) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _classname = classname;
            _classordername1 = classordername1;
            _specialDiv = specialdiv;
            _showorder2 = showorder2;
        }

        public String getKey(final Param param) {
            return key(param, _classcd, _schoolKind);
        }

        public String toString() {
            return "ClassMst(" + getKey(null) + ", " + _classname + ")";
        }

        public static String key(final Param param, final String classcd, final String schoolKind) {
            if (null != param && !"1".equals(param._useCurriculumcd)) {
                return classcd;
            }
            return classcd + "-" + schoolKind;
        }

        public static ClassMst getClassMst(final Map<String, ClassMst> classMstMap, final String key) {
            final ClassMst classMst = classMstMap.get(key);
            if (null == classMst) {
                log.warn(" null class mst:" + key);
                return Null;
            }
            return classMst;
        }

        protected static int compareOrder(final ClassMst classMst1, final ClassMst classMst2) {
            if (Null == classMst1 && Null == classMst2) {
                return 0;
            } else if (Null == classMst1) {
                return 1;
            } else if (Null == classMst2) {
                return -1;
            }
            int rtn;
            rtn = classMst1._specialDiv.compareTo(classMst2._specialDiv);
            if (0 != rtn) { return rtn; }
            rtn = classMst1._showorder2.compareTo(classMst2._showorder2);
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
        protected static final AnotherClassMst Null = new AnotherClassMst("XX", "XX", null, null, "9", new Integer(99999999));

        protected AnotherClassMst(final String classcd, final String schoolKind, final String classname, final String classordername1, final String specialdiv, final Integer showorder) {
            super(classcd, schoolKind, classname, classordername1, specialdiv, showorder);
        }

        public String toString() {
            return "AnotherClassMst(" + getKey(null) + ", " + _classname + ")";
        }

        public static AnotherClassMst getAnotherClassMst(final Map<String, AnotherClassMst> classMstMap, final String key) {
            final AnotherClassMst anotherClassMst = classMstMap.get(key);
            if (null == anotherClassMst) {
                log.warn(" null another class mst:" + key);
                return Null;
            }
            return anotherClassMst;
        }
    }

    protected static class SubclassMst implements Comparable<SubclassMst> {
        protected static final SubclassMst ABROAD = new SubclassMst("AA", "AA", "AA", "AAAAAA", _ABROAD, null, new Integer(0), null);
        protected static final SubclassMst Null = new SubclassMst("XX", "XX", "XX", "XX", null, null, new Integer(99999999), null);
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassordername1;
        final Integer _showorder2;
        final String _subclasscd2;
        Map _yearZenkiKamokuFlg = new HashMap();

        public SubclassMst(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final String subclassname, final String subclassordername1, final Integer showorder2, final String subclasscd2) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassordername1 = subclassordername1;
            _showorder2 = showorder2;
            _subclasscd2 = subclasscd2;
        }

        public SubclassMst setSubclassordername(final String subclassordername1) {
            return new SubclassMst(_classcd, _schoolKind, _curriculumCd, _subclasscd, _subclassname, subclassordername1, _showorder2, null);
        }

        public String subclassname() {
            return StringUtils.defaultString(_subclassordername1, _subclassname);
        }

        public String getKey(final Param param) {
            return key(param, _classcd, _schoolKind, _curriculumCd, _subclasscd);
        }

        public boolean isZenkiKamoku(final String year) {
            return "1".equals(_yearZenkiKamokuFlg.get(year));
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
            if (null != param && !"1".equals(param._useCurriculumcd)) {
                return subclasscd;
            }
            return classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
        }

        public static SubclassMst getSubclassMst(final Map<String, SubclassMst> subclassMstMap, final String key) {
            final SubclassMst subclassMst = subclassMstMap.get(key);
            if (null == subclassMst) {
                log.warn(" null subclass mst:" + key);
                return Null;
            }
            return subclassMst;
        }

        @Override
        public int compareTo(final SubclassMst other) {
            return (_classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd).compareTo(other._classcd + "-" + other._schoolKind + "-" + other._curriculumCd + "-" + other._subclasscd);
        }

        protected static int compareOrder(final Param param, final boolean isSubclassOrderNotContainCurriculumcd, final SubclassMst subclassMst1, final SubclassMst subclassMst2) {
            if (Null == subclassMst1 && Null == subclassMst2) {
                return 0;
            } else if (Null == subclassMst1) {
                return 1;
            } else if (Null == subclassMst2) {
                return -1;
            }
            int rtn;
            if ("1".equals(param._useCurriculumcd)) {
                if (!isSubclassOrderNotContainCurriculumcd) {
                    if (null != subclassMst1._curriculumCd && null != subclassMst2._curriculumCd) {
                        rtn = subclassMst1._curriculumCd.compareTo(subclassMst2._curriculumCd);
                        if (0 != rtn) {
                            return rtn;
                        }
                    }
                }
            }
            rtn = subclassMst1._showorder2.compareTo(subclassMst2._showorder2);
            if (0 != rtn) {
                return rtn;
            }
            rtn = subclassMst1._subclasscd.compareTo(subclassMst2._subclasscd);
            return rtn;
        }

        protected static boolean isSameKey(final Param param, final boolean isSubclassOrderNotContainCurriculumcd, final SubclassMst subclassMst1, final SubclassMst subclassMst2) {
            if (!"1".equals(param._useCurriculumcd)) {
                return subclassMst1._subclasscd.equals(subclassMst2._subclasscd);
            }
            if (isSubclassOrderNotContainCurriculumcd) {
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
    }

    protected static class AnotherSubclassMst extends SubclassMst {
        protected static final AnotherSubclassMst Null = new AnotherSubclassMst("XX", "XX", "XX", "XX", null, null, new Integer(99999999));

        public AnotherSubclassMst(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final String subclassname, final String subclassordername1, final Integer showorder) {
            super(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassordername1, showorder, null);
        }

        public AnotherSubclassMst setAnotherSubclassMstSubclassordername(final String subclassordername1) {
            return new AnotherSubclassMst(_classcd, _schoolKind, _curriculumCd, _subclasscd, _subclassname, subclassordername1, _showorder2);
        }

        public String toString() {
            return "AnotherSubclassMst(" + getKey(null) + ", " + subclassname() + ")";
        }

        public static AnotherSubclassMst getAnotherSubclassMst(final Map<String, AnotherSubclassMst> anotherSubclassMstMap, final String key) {
            final AnotherSubclassMst anotherSubclassMst = anotherSubclassMstMap.get(key);
            if (null == anotherSubclassMst) {
                log.warn(" null another subclass mst:" + key);
                return Null;
            }
            return anotherSubclassMst;
        }
    }

    protected static class StudyrecDat {

        protected enum Kind {
            Sogo
          , Abroad
          , Total
          , None
        }


        final Kind _kind;
        final CommonPrintData _printData;
        final String _schoolcd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _avgGrades;
        final String _specialDiv;
        final String _compCredit2;
        final List<Grades> _gradesList;
        boolean _isE014; // PreSql2

        private String getNotDroppedMaxYear(final String annual) {
            String maxYear = null;
            for (final Title title : _printData.titleValues()) {
                if (_printData._ryunenYears.contains(title._year)) {
                    continue;
                }
                if (annual.compareTo(title._annual) < 0) {
                    break;
                }
                maxYear = title._year;
            }
            return maxYear;
        }

        public List<Grades> getNotDropGradesList() {
            final List<Grades> notDropped = new ArrayList<Grades>();
            final List<Grades> droppedValid = new ArrayList<Grades>();
            for (final Grades g : _gradesList) {
                if (_printData._ryunenYears.contains(g._year)) {
                    final Title title = Title.getTitle(_printData._param, _printData.titleValues(), g._year);
                    if (null == title) {
                        continue;
                    }
                    if ("1".equals(g._validFlg)) {
                        final Grades ng = (Grades) g.clone();
                        ng._year = getNotDroppedMaxYear(title._annual);
                        droppedValid.add(ng);
                    }
                } else {
                    notDropped.add(g);
                }
            }
            if (!droppedValid.isEmpty()) {
                return droppedValid;
            }
            return notDropped;
        }

        public StudyrecDat(
                final Kind kind,
                final CommonPrintData printData,
                final String schoolcd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String classname,
                final String subclassname,
                final String avgGrades,
                final String specialDiv,
                final String compCredit2) {
            _kind = kind;
            _printData = printData;
            _schoolcd = schoolcd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _avgGrades = avgGrades;
            _specialDiv = specialDiv;
            _compCredit2 = compCredit2;
            _gradesList = new ArrayList();
        }

        public String keyClasscd(final Param param) {
            if ("1".equals(param._useCurriculumcd)) {
                return _classcd + "-" + _schoolKind;
            }
            return _classcd;
        }

        public String keySubclasscd(final Param param) {
            if ("1".equals(param._useCurriculumcd)) {
                return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
            }
            return _subclasscd;
        }

        // 合計
        public String creditSuraSum1() {
            final List<String> list = new ArrayList<String>();
            for (final Grades g : getNotDropGradesList()) {
                //log.info(" " + _classname + ", g = " + g);
                if (NumberUtils.isNumber(g._credit)) {
                    list.add(g._credit);
                }
            }
            //log.info(" " + _classname + ", list = " + list);
            return bdStringSum(list);
        }

        // SQLで合計しているのでどれか1つ
        public String creditSuraSum2(final Param param) {
            final List<String> list = new ArrayList<String>();
            final List<String> others = new ArrayList<String>();
            for (final Grades g : getNotDropGradesList()) {
                if (param._isOutputDebug) {
                    log.info(" " + _classname + ", g = " + g);
                }
                if (NumberUtils.isNumber(g._credit)) {
                    if (g._isKoteiTanniToAbroad) {
                        others.add(g._credit);
                    } else {
                        list.add(g._credit);
                    }
                }
            }
            //log.info(" " + _classname + ", list = " + list);
            String creditSura = null;
            if (!list.isEmpty()) {
                creditSura = list.get(0);
            }
            return Util.add(creditSura, StudyrecDat.bdStringSum(others));
        }

        public String creditSuraElse() {
            return credit();
        }

        public String credit() {
            final List<String> list = new ArrayList<String>();
            for (final Grades g : getNotDropGradesList()) {
                if (NumberUtils.isNumber(g._gradeCredit)) {
                    list.add(g._gradeCredit);
                }
            }
            return bdStringSum(list);
        }

        public String compCredit() {
            final List<String> list = new ArrayList<String>();
            for (final Grades g : getNotDropGradesList()) {
                if (NumberUtils.isDigits(g._compCredit)) {
                    list.add(g._compCredit);
                }
            }
            return list.isEmpty() ? null : list.get(0);
        }

        private static String bdStringSum(final List<String> list) {
            if (list.isEmpty()) {
                return null;
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (final String v : list) {
                sum = sum.add(new BigDecimal(v));
            }
            return sum.toString();
        }

        public boolean hasGrades(final CommonPrintData printData) {
            boolean hasGrades = false;
            for (final Grades grades : getNotDropGradesList()) {
                final Title title = Title.getTitle(printData._param, printData.titleValues(), grades._year);
                if (null != title) {
                    hasGrades = true;
                    break;
                }
            }
            return hasGrades;
        }

        public String toString() {
            return " StudyrecDat(classcd=" + _classcd +
            ", schoolKind=" + _schoolKind +
            ", curriculumCd=" + _curriculumCd +
            ", subclasscd=" + _subclasscd +
            ", classname=" + _classname +
            ", subclassname=" + _subclassname +
            ", avgGrades=" + _avgGrades +
            ", specialDiv=" + _specialDiv +
            ", gradesList=" + _gradesList +
            ")";
        }
    }

    protected static class Grades implements Cloneable {
        final String _recordFlg;
        String _year;
        final String _grades;
        final String _credit;
        final String _gradeCredit;
        final String _compCredit;
        final String _validFlg;
        final String _provFlg;
        final String _provSemester;
        final String _d065Flg;
        final String _schoolcd;
        final boolean _isKoteiTanniToAbroad;
        String _zenkiGrades;
        String _koukiGrades;
        public Grades(
                final String recordFlg,
                final String year,
                final String grades,
                final String credit,
                final String gradeCredit,
                final String compCredit,
                final String validFlg,
                final String provFlg,
                final String provSemester,
                final String d065Flg,
                final String schoolcd,
                final boolean isKoteiTanniToAbroad
        ) {
            _recordFlg = recordFlg;
            _year = year;
            _grades = grades;
            _credit = credit;
            _gradeCredit = gradeCredit;
            _compCredit = compCredit;
            _validFlg = validFlg;
            _provFlg = provFlg;
            _provSemester = provSemester;
            _d065Flg = d065Flg;
            _schoolcd = schoolcd;
            _isKoteiTanniToAbroad = isKoteiTanniToAbroad;
        }
        public String getPrintGrade(final Param param, final String grades) {
            if (null == grades) {
                return null;
            }
            final int gradeInt = Double.valueOf(grades).intValue();
            if (null != _d065Flg) {
                return param._d001NameMstAbbv1Map.get(String.valueOf(gradeInt));
            } else {
                if (0 != gradeInt) {
                    return String.valueOf(gradeInt);
                }
            }
            return null;
        }
        public Object clone() {
            final Grades g = new Grades(_recordFlg, _year, _grades, _credit, _gradeCredit, _compCredit, _validFlg, _provFlg, _provSemester, _d065Flg, _schoolcd, _isKoteiTanniToAbroad);
            g._zenkiGrades = _zenkiGrades;
            g._koukiGrades = _koukiGrades;
            return g;
        }
        public String toString() {
            return "[FLG = " + _recordFlg + " 年次=" + _year + " 評定=" + _grades + " 単位=" + _credit + " 履修単位=" + _compCredit + (null != _zenkiGrades ? " 前期評定=" + _zenkiGrades : "") + (null != _koukiGrades ? " 後期評定=" + _koukiGrades : "") + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 代替科目備考
     */
    protected static class StudyrecSubstitutionNote {

        private final CommonPrintData _printData;
        StudyrecSubstitutionNote(final CommonPrintData printData) {
            _printData = printData;
        }

        private static final String ZENBU = "1";  // 代替フラグ:全部
        private static final String ICHIBU = "2";  // 代替フラグ:一部

        private static final String div90 = "90";
        private static final String divNo90 = "No90";

        final List<String> TYPE_FLG_LIST = Arrays.asList(ZENBU, ICHIBU);

        private Map<String, Map<String, Map<String, List<SubstitutionInfo>>>> _substitutionNotice = new HashMap<String, Map<String, Map<String, List<SubstitutionInfo>>>>(); // 代替科目備考
        protected List<StudyrecSubstitutionNote.SubstitutionInfo> _substitutionNoticeAllList = Collections.emptyList(); // 代替科目備考リスト (全て)

        private Map<String, Map<String, Integer>> _creditMstMap = new TreeMap<String, Map<String, Integer>>();

        private Map<String, List<SubstitutionInfo>> getDivMap(final String subclassdiv, final String typeFlg) {
            return Util.getMappedMap(Util.getMappedMap(_substitutionNotice, subclassdiv), typeFlg);
        }

        /**
         * 代替科目備考を設定します。
         * @param object
         * @return
         */
        public void setSubstitutionSubclassNote(final DB2UDB db2, final Param param, final CommonPrintData printData) {

            final List<SubstitutionInfo> noticeAllList = new ArrayList<SubstitutionInfo>();
            if (printData._useStudyrecSql2) {
                final List<Map<String, String>> _tStudyrec = printData._tStudyrec;
                final Map<String, List<Map<String, String>>> yearListMap = new TreeMap<String, List<Map<String, String>>>();
                for (Map<String, String> row : _tStudyrec) {
                    Util.getMappedList(yearListMap, getString(row, "YEAR")).add(row);
                }

                final String gcmKey = "GCM_SQL" + (printData.isGakunensei(param) ? "1" : "0");
                if (null == param.getPs(gcmKey)) {
                    final List gcmSql = new ArrayList();
                    gcmSql.add(" SELECT ");
                    gcmSql.add("     T1.YEAR, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
                    gcmSql.add(" FROM SCHREG_REGD_DAT T1 ");
                    gcmSql.add(" INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
                    gcmSql.add("             FROM SCHREG_REGD_DAT I1 ");
                    gcmSql.add("             GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
                    gcmSql.add(" WHERE T1.SCHREGNO = ? ");
                    if (printData.isGakunensei(param)) {
                        gcmSql.add(" AND ");
                        gcmSql.add("   (T1.SCHREGNO, T1.YEAR) IN (SELECT SCHREGNO, MAX(YEAR) AS YEAR FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, GRADE)");
                    }
                    param.setPs(db2, gcmKey, Util.mkString(gcmSql, ""));
                }

                final List<Map<String, String>> ygcmRowList = KnjDbUtils.query(db2, param.getPs(gcmKey), new Object[] {printData._schregno});

                final Map<String, List<String>> zenbuDaitaiSubstitutedYearsMap = new HashMap<String, List<String>>();
                final Map<String, List<String>> ichibuDaitaiNotSubstitutedYearsMap = new HashMap<String, List<String>>();

                for (final Map<String, String> ygcm : ygcmRowList) {
                    final String year = getString(ygcm, "YEAR");
                    final String grade = getString(ygcm, "GRADE");
                    final String coursecd = getString(ygcm, "COURSECD");
                    final String majorcd = getString(ygcm, "MAJORCD");
                    final String coursecode = getString(ygcm, "COURSECODE");
                    final List<Map<String, String>> subStMst = param.getSubStMst(db2, year, grade, coursecd, majorcd, coursecode);

                    for (final Map<String, String> row : subStMst) {
                        final String substitutionTypeFlg = getString(row, "SUBSTITUTION_TYPE_FLG");
                        final String attendClassCd = getString(row, "ATTEND_CLASSCD");
                        final String attendSchoolKind = "1".equals(param._useCurriculumcd) ? getString(row, "ATTEND_SCHOOL_KIND") : "";
                        final String attendCurriculumCd = "1".equals(param._useCurriculumcd) ? getString(row, "ATTEND_CURRICULUM_CD") : "";
                        final String attendSubclassCd = getString(row, "ATTEND_SUBCLASSCD");
                        final SubclassMst attendSubclass = SubclassMst.getSubclassMst(param._subclassMstMap, SubclassMst.key(param, attendClassCd, attendSchoolKind, attendCurriculumCd, attendSubclassCd));
                        if (!param.isSubclassInYear(db2, year, attendSubclass)) {
                            continue;
                        }

                        final String substitutionClasscd = getString(row, "SUBSTITUTION_CLASSCD");

                        Map<String, String> found = null;
                        for (final Map<String, String> trow : Util.getMappedList(yearListMap, year)) {
                            final String classcd = "1".equals(param._useCurriculumcd) ? getString(trow, "CLASSCD") : "";
                            final String schoolKind = "1".equals(param._useCurriculumcd) ? getString(trow, "SCHOOL_KIND") : "";
                            final String curriculumCd = "1".equals(param._useCurriculumcd) ? getString(trow, "CURRICULUM_CD") : "";
                            final String studyrecSubclasscd = getString(trow, "STUDYREC_SUBCLASSCD");
                            final SubclassMst subclass = SubclassMst.getSubclassMst(param._subclassMstMap, SubclassMst.key(param, classcd, schoolKind, curriculumCd, studyrecSubclasscd));
                            if (SubclassMst.Null != subclass &&  SubclassMst.isSameKey(param, printData._isSubclassOrderNotContainCurriculumcd, attendSubclass, subclass)) {
                                found = trow;
                                break;
                            }
                        }

                        final String substitutionSchoolKind = "1".equals(param._useCurriculumcd) ? getString(row, "SUBSTITUTION_SCHOOL_KIND") : "";
                        final String substitutionCurriculumCd = "1".equals(param._useCurriculumcd) ? getString(row, "SUBSTITUTION_CURRICULUM_CD") : "";
                        final String substitutionSubclasscd = getString(row, "SUBSTITUTION_SUBCLASSCD");

                        final String keySubclasscd;
                        if (_90.equals(substitutionClasscd)) {
                            keySubclasscd = _90;
                        } else if ("1".equals(param._useCurriculumcd)) {
                            keySubclasscd = substitutionClasscd + "-" + substitutionSchoolKind + "-" + substitutionCurriculumCd + "-" + substitutionSubclasscd;
                        } else {
                            keySubclasscd = substitutionSubclasscd;
                        }

                        if (null == found) {
                            if (!"1".equals(_printData.property(Property.tyousashoDaitaiCheckMasterYear))) {
                                if (ICHIBU.equals(substitutionTypeFlg)) {
                                    Util.getMappedList(ichibuDaitaiNotSubstitutedYearsMap, keySubclasscd).add(year);
                                }
                            }
                            continue;
                        }

                        final SubclassMst substSubclass = SubclassMst.getSubclassMst(param._subclassMstMap, SubclassMst.key(param, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd));
                        if (!param.isSubclassInYear(db2, year, substSubclass)) {
                            continue;
                        }
                        final ClassMst substClass = ClassMst.getClassMst(param._classMstMap, ClassMst.key(param, substitutionClasscd, substitutionSchoolKind));

                        // 代替元科目のSCHREG_STUDYREC_DAT単位数
                        final String attendCredit = Util.addDigits(getString(found, "GET_CREDIT"), getString(found, "ADD_CREDIT"));
                        if (param._z010.in(Z010Info.Mieken) && Util.toInt(attendCredit, 0) <= 0) {
                            // TODO:
                            log.info("代替単位数なし : " + attendSubclass);
                            continue;
                        }
                        // 代替元科目の単位マスタ単位数
                        final String substCredit = param.getCreditMstCredits(db2, attendSubclass, year, grade, coursecd, majorcd, coursecode);

                        final String subclassCd2SubclassCd = getString(found, "SUBCLASSCD");

                        final String[] checkYears;
                        if ("1".equals(_printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                            checkYears = new String[] { year, "NO_KEY"};
                        } else {
                            checkYears = new String[] { "NO_KEY", };
                        }

                        // 進学用は教科コード90とそれ以外を分けて出力する。
                        final String divMapKey;
                        if (_90.equals(substitutionClasscd)) {
                            divMapKey = div90;
                        } else {
                            divMapKey = divNo90;
                        }
                        final Map<String, List<SubstitutionInfo>> divMap = getDivMap(divMapKey, substitutionTypeFlg); // 代替科目備考 ("総合的な学習の時間"以外)

                        for (int j = 0; j < checkYears.length; j++) {
                            final String checkYear = checkYears[j];
                            if (null == SubstitutionInfo.getSubstitutionInfo(substitutionTypeFlg, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd, Util.getMappedList(divMap, checkYear))) {
                                final SubstitutionInfo i = new SubstitutionInfo(param, substitutionTypeFlg, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd, substClass._classname, substSubclass.subclassname());
                                Util.getMappedList(divMap, checkYear).add(i);
                                if ("NO_KEY".equals(checkYear)) {
                                    noticeAllList.add(i);
                                }
                            }
                            final SubstitutionInfo info = SubstitutionInfo.getSubstitutionInfo(substitutionTypeFlg, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd, Util.getMappedList(divMap, checkYear));

                            if (null == SubstitutionInfo.AttendSubclass.getAttendSubclass(subclassCd2SubclassCd, info._attendSubclassList)) {
                                final String attendSubclassname = getString(found, "SUBCLASSNAME");
                                final String attendClassname = getString(found, "CLASSNAME");
                                info.addAttendSubclass(new SubstitutionInfo.AttendSubclass(subclassCd2SubclassCd, attendSubclassCd, attendClassname, attendSubclassname));
                            }
                            final SubstitutionInfo.AttendSubclass asub = SubstitutionInfo.AttendSubclass.getAttendSubclass(subclassCd2SubclassCd, info._attendSubclassList);
                            asub._yearAttendCreditMap.put(year, attendCredit);
                            asub._yearSubstitutionCreditMap.put(year, substCredit);

                            final List<String> zenbuDaitaiSubstitutedYears  = Util.getMappedList(zenbuDaitaiSubstitutedYearsMap, keySubclasscd);
                            if (ZENBU.equals(substitutionTypeFlg)) {
                                zenbuDaitaiSubstitutedYears.add(year);
                            } else if (ICHIBU.equals(substitutionTypeFlg)) {
                                final List<String> ichibuDaitaiNotSubstitutedYears  = Util.getMappedList(ichibuDaitaiNotSubstitutedYearsMap, keySubclasscd);
                                info._creditsMstCredits = getCreditMstCreditsTotal(keySubclasscd, zenbuDaitaiSubstitutedYears, ichibuDaitaiNotSubstitutedYears, param);
                                if (_90.equals(substitutionClasscd)) {
                                    info._studyrecCredits = _printData.getStudyrecSogoCredit();
                                } else {
                                    info._studyrecCredits = _printData.getStudyrecCredit(keySubclasscd, param);
                                }
                                if (param._isOutputDebugSubst) {
                                    log.info(" daitai " + keySubclasscd + " (ichibuDaitaiNotSubstitutedYears " + divMapKey + " " + ichibuDaitaiNotSubstitutedYears + ") creditsMstCredits " + info._creditsMstCredits + ", studyrecCredits = " + info._studyrecCredits);
                                }
                            }
                        }
                    }
                }


            } else {
                final Map<String, List<String>> zenbuDaitaiSubstitutedYearsMap = new HashMap<String, List<String>>();
                final CommonSqlStudyrec object = printData._sqlStudyrec;
                for (final String substitutionTypeFlg : TYPE_FLG_LIST) {

                    final List<String> sqlLineList = object.getSubstitutionSubclassNoteSql(_printData, param, substitutionTypeFlg);
                    final String sql = Util.mkString(sqlLineList, "");
                    if (param._isOutputDebugQuery) {
                        log.info("代替科目備考(" + substitutionTypeFlg + ")SQL = " + Util.mkString(sqlLineList, newLine));
                    }

                    final Map<String, List<String>> ichibuDaitaiNotSubstitutedYearsMap = new HashMap<String, List<String>>();

                    for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                        final String year = getString(row, "YEAR");
                        final String substSubclassName = getString(row, "SUBSTITUTION_SUBCLASSNAME");
                        final String substClassName = getString(row, "SUBSTITUTION_CLASSNAME");
                        final String attendCredit = getString(row, "ATTEND_CREDIT");

                        final String subclassCd2SubclassCd = getString(row, "SUBCLASSCD2_SUBCLASSCD");
                        final String substitutionClasscd = getString(row, "SUBSTITUTION_CLASSCD");
                        final String substitutionSchoolKind = "1".equals(param._useCurriculumcd) ? getString(row, "SUBSTITUTION_SCHOOL_KIND") : "";
                        final String substitutionCurriculumCd = "1".equals(param._useCurriculumcd) ? getString(row, "SUBSTITUTION_CURRICULUM_CD") : "";
                        final String substitutionSubclasscd = getString(row, "SUBSTITUTION_SUBCLASSCD");

                        final String substitutionCredit = defstr(getString(row, "SUBSTITUTION_CREDIT"));

                        final String keySubclasscd;
                        if (_90.equals(substitutionClasscd)) {
                            keySubclasscd = _90;
                        } else if ("1".equals(param._useCurriculumcd)) {
                            keySubclasscd = substitutionClasscd + "-" + substitutionSchoolKind + "-" + substitutionCurriculumCd + "-" + substitutionSubclasscd;
                        } else {
                            keySubclasscd = substitutionSubclasscd;
                        }

                        if (null == getString(row, "SCHREGNO")) {
                            if (!"1".equals(_printData.property(Property.tyousashoDaitaiCheckMasterYear))) {
                                if (ICHIBU.equals(substitutionTypeFlg)) {
                                    Util.getMappedList(ichibuDaitaiNotSubstitutedYearsMap, keySubclasscd).add(year);
                                }
                            }
                            continue;
                        }


                        final String[] checkYears;
                        if ("1".equals(_printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                            checkYears = new String[] { year, "NO_KEY"};
                        } else {
                            checkYears = new String[] { "NO_KEY", };
                        }

//                        /**
//                         *  鳥取（名称マスタ「Z010」「00」のname1=tottori）は
//                         *  代替処理の備考について、2010年3月31日以前に卒業した生徒は表示しない
//                         */
//                        final String grdDate = getString(row, "GRD_DATE");
//                        final boolean notDisplayInTottori = param._isTottori && grdDate != null && java.sql.Date.valueOf(grdDate).compareTo(java.sql.Date.valueOf("2010-03-31")) <= 0;
//                        if (notDisplayInTottori) {
//                            continue;
//                        }
                        // 進学用は教科コード90とそれ以外を分けて出力する。
                        final String divMapKey;
                        if (_90.equals(substitutionClasscd)) {
                            divMapKey = div90;
                        } else {
                            divMapKey = divNo90;
                        }
                        final Map<String, List<SubstitutionInfo>> divMap = getDivMap(divMapKey, substitutionTypeFlg); // 代替科目備考 ("総合的な学習の時間"以外)

                        for (int j = 0; j < checkYears.length; j++) {
                            final String checkYear = checkYears[j];
                            if (null == SubstitutionInfo.getSubstitutionInfo(substitutionTypeFlg, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd, Util.getMappedList(divMap, checkYear))) {
                                final SubstitutionInfo i = new SubstitutionInfo(param, substitutionTypeFlg, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd, substClassName, substSubclassName);
                                Util.getMappedList(divMap, checkYear).add(i);
                                if ("NO_KEY".equals(checkYear)) {
                                    noticeAllList.add(i);
                                }
                            }
                            final SubstitutionInfo info = SubstitutionInfo.getSubstitutionInfo(substitutionTypeFlg, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd, Util.getMappedList(divMap, checkYear));

                            if (null == SubstitutionInfo.AttendSubclass.getAttendSubclass(subclassCd2SubclassCd, info._attendSubclassList)) {
                                final String attendSubclassCd = getString(row, "ATTEND_SUBCLASSCD");
                                final String attendSubclassname = getString(row, "ATTEND_SUBCLASSNAME");
                                final String attendClassname = getString(row, "ATTEND_CLASSNAME");
                                info.addAttendSubclass(new SubstitutionInfo.AttendSubclass(subclassCd2SubclassCd, attendSubclassCd, attendClassname, attendSubclassname));
                            }
                            final SubstitutionInfo.AttendSubclass asub = SubstitutionInfo.AttendSubclass.getAttendSubclass(subclassCd2SubclassCd, info._attendSubclassList);
                            asub._yearAttendCreditMap.put(year, attendCredit);
                            asub._yearSubstitutionCreditMap.put(year, substitutionCredit);

                            final List<String> zenbuDaitaiSubstitutedYears  = Util.getMappedList(zenbuDaitaiSubstitutedYearsMap, keySubclasscd);
                            if (ZENBU.equals(substitutionTypeFlg)) {
                                zenbuDaitaiSubstitutedYears.add(year);
                            } else if (ICHIBU.equals(substitutionTypeFlg)) {
                                final List<String> ichibuDaitaiNotSubstitutedYears  = Util.getMappedList(ichibuDaitaiNotSubstitutedYearsMap, keySubclasscd);
                                info._creditsMstCredits = getCreditMstCreditsTotal(keySubclasscd, zenbuDaitaiSubstitutedYears, ichibuDaitaiNotSubstitutedYears, param);
                                if (_90.equals(substitutionClasscd)) {
                                    info._studyrecCredits = _printData.getStudyrecSogoCredit();
                                } else {
                                    info._studyrecCredits = _printData.getStudyrecCredit(keySubclasscd, param);
                                }
                                if (param._isOutputDebugSubst) {
                                    log.info(" daitai " + keySubclasscd + " (ichibuDaitaiNotSubstitutedYears " + divMapKey + " " + ichibuDaitaiNotSubstitutedYears + ") creditsMstCredits " + info._creditsMstCredits + ", studyrecCredits = " + info._studyrecCredits);
                                }
                            }
                        }
                    }
                }
            }

            _substitutionNoticeAllList = noticeAllList;
            if (param._isOutputDebug || param._isOutputDebugSubst) {
                log.fatal(" 代替科目備考 = " + Util.debugCollectionToStr("", _substitutionNoticeAllList, ",") + " / " + Util.debugMapToStr("", _substitutionNotice));
            }
        }

        public void setCreditMst(final DB2UDB db2, final CommonPrintData printData, final Param param) {

            final Set<String> yearSet = new TreeSet<String>();
            final Set<String> notDroppedYearSet = new TreeSet<String>();
            for (final Title title : printData.titleValues()) {
                yearSet.add(" ('" + title._year + "') ");
                if ("1".equals(printData.property(Property.tyousashoDaitaiNotContainDropYearCreditMst)) && title._pos < 0) {
                    continue;
                }
                notDroppedYearSet.add(" ('" + title._year + "') ");
            }
            final Map<String, Map<String, Integer>> creditMstMap = new TreeMap<String, Map<String, Integer>>();
            if (!yearSet.isEmpty()) {

                final StringBuffer sql = new StringBuffer();
                sql.append(" WITH REGD_YEAR(YEAR) AS (");
                sql.append(" VALUES ");
                sql.append(Util.mkString(yearSet, ","));
                sql.append(" ) ");
                sql.append(" , REGD_YEAR_NOT_DROPPED(YEAR) AS (");
                sql.append(" VALUES ");
                if (!notDroppedYearSet.isEmpty()) {
                    sql.append(Util.mkString(notDroppedYearSet, ","));
                } else {
                    sql.append("(CAST(NULL AS VARCHAR(4))(");
                }
                sql.append(" ) ");
                sql.append(" , MAX_SEMESTER AS ( ");
                sql.append("   SELECT T1.YEAR, T1.SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
                sql.append("        , CASE WHEN T3.YEAR IS NULL THEN 1 END AS DROPPED ");
                sql.append("   FROM SCHREG_REGD_DAT T1 ");
                sql.append("   INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
                sql.append("   LEFT JOIN REGD_YEAR_NOT_DROPPED T3 ON T3.YEAR = T1.YEAR ");
                sql.append("   WHERE T1.SCHREGNO = '" + printData._schregno + "' ");
                sql.append("   GROUP BY T1.YEAR, T1.SCHREGNO, T3.YEAR ");
                sql.append(" ) ");
                sql.append("   SELECT T1.YEAR, T1.SCHREGNO, ");
                sql.append("   T3.CLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("   T3.SCHOOL_KIND, ");
                    sql.append("   T3.CURRICULUM_CD, ");
                }
                sql.append("          T3.SUBCLASSCD, ");
                sql.append("          T3.CREDITS, ");
                sql.append("          T2.DROPPED ");
                sql.append("   FROM SCHREG_REGD_DAT T1 ");
                sql.append("   INNER JOIN MAX_SEMESTER T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                sql.append("       AND T2.YEAR = T1.YEAR ");
                sql.append("       AND T2.SEMESTER = T1.SEMESTER ");
                sql.append("   INNER JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
                sql.append("       AND T3.COURSECD = T1.COURSECD ");
                sql.append("       AND T3.MAJORCD = T1.MAJORCD ");
                sql.append("       AND T3.GRADE = T1.GRADE ");
                sql.append("       AND T3.COURSECODE = T1.COURSECODE ");
                sql.append("       AND T3.CREDITS IS NOT NULL ");
                sql.append("   INNER JOIN V_SUBCLASS_MST T4 ON T4.YEAR = T1.YEAR AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("       AND T4.CLASSCD = T3.CLASSCD ");
                    sql.append("       AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
                    sql.append("       AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
                }

                for (final Map<String, String> row : KnjDbUtils.query(db2, sql.toString())) {
                    final String keySubclasscd;
                    if ("1".equals(param._useCurriculumcd)) {
                        keySubclasscd = getString(row, "CLASSCD") + "-" + getString(row, "SCHOOL_KIND") + "-" + getString(row, "CURRICULUM_CD") + "-" + getString(row, "SUBCLASSCD");
                    } else {
                        keySubclasscd = getString(row, "SUBCLASSCD");
                    }
                    final String key = _90.equals(getString(row, "CLASSCD")) ? div90 : keySubclasscd;
                    if ("1".equals(getString(row, "DROPPED")) && !div90.equals(key)) {
                        continue;
                    } else {
                        Util.getMappedHashMap(creditMstMap, key).put(getString(row, "YEAR"), Integer.valueOf(getString(row, "CREDITS")));
                    }
                }
            }
            _creditMstMap = creditMstMap;
            if (param._isOutputDebugSubst) {
                log.info(" creditMstMap = " + _creditMstMap);
            }
        }

        private Map<String, Integer> getCreditMstCreditsTotal(final String keySubclasscd, final List<String> zenbuDaitaiSubstitutedYears, final List<String> ichibuDaitaiNotSubstitutedYears, final Param param) {
            final Map<String, Integer> map = _creditMstMap.get(keySubclasscd);
            //log.fatal(" key = " + key + ", creditMstMap = " + map);
            final Map<String, Integer> creditsTotal = new TreeMap<String, Integer>();
            final Map<String, Integer> creditsTotalCheck = new TreeMap<String, Integer>();
            if (null != map) {
                for (final Map.Entry<String, Integer> yearCredits : map.entrySet()) {
                    final String year = yearCredits.getKey();
                    final Integer cred = yearCredits.getValue();
                    if ((!_printData._tyousashoCheckZenbuDaitaiSubstitutedYear || _printData._tyousashoCheckZenbuDaitaiSubstitutedYear && !zenbuDaitaiSubstitutedYears.contains(year)) && !ichibuDaitaiNotSubstitutedYears.contains(year)) {
                        creditsTotal.put(year, Util.add(creditsTotal.get(year), cred));
                    }
                    creditsTotalCheck.put(year, Util.add(creditsTotalCheck.get(year), cred));
                }
            }
            if (param._isOutputDebugSubst) {
                log.fatal(" zenbuDaitaiSubstitutedYears = " + zenbuDaitaiSubstitutedYears + ", ichibuDaitaiNotSubstitutedYears = " + ichibuDaitaiNotSubstitutedYears + ", daitai " + keySubclasscd + " add " + creditsTotal + (creditsTotal.equals(creditsTotalCheck) ? "" : " / " + creditsTotalCheck));
            }
            return creditsTotal;
        }

        /**
         * 教科コードが90(総合的な学習の時間)の指定のキーの代替科目備考を得る
         * @return
         */
        public List<String> getDaitaiBiko90(final String debug, final String key0, final boolean isPrintSubstSubclass, final String typeFlg0, final Param param) {
            return getDaitaiBiko(debug, div90, key0, isPrintSubstSubclass, typeFlg0, param);
        }

        /**
         * 代替備考("[代替先科目]は[代替元科目]で代替")文字列のリストを得る
         * @param divCd 総学(div90)か総学以外(divNo90)
         * @param key0 年度か全て("NO_KEY")
         * @param isPrintSubstSubclass 「[代替先科目]は」を表示するか trueなら表示する
         * @param typeFlg0 全部代替 or 一部代替、nullなら両方（全部、一部代替の順）
         * @return 代替備考文字列のリスト
         */
        private List<String> getDaitaiBiko(final String debug, final String divCd, final String key0, final boolean isPrintSubstSubclass, final String typeFlg0, final Param param) {
            final List<SubstitutionInfo> targetlist = new ArrayList<SubstitutionInfo>();
            for (final String typeFlg : TYPE_FLG_LIST) {
                if (null != typeFlg0 && !typeFlg0.equals(typeFlg)) {
                    continue;
                }
                final List<SubstitutionInfo> typeflgList = new ArrayList<SubstitutionInfo>();
                final Map<String, List<SubstitutionInfo>> m = getDivMap(divCd, typeFlg);
                for (final String key : m.keySet()) {
                    if (null != key0 && !key0.equals(key)) {
                        continue;
                    }
                    typeflgList.addAll(Util.getMappedList(m, key));
                }
                targetlist.addAll(typeflgList);
            }
            Collections.sort(targetlist);
            final List<String> rtn = new ArrayList<String>();
            for (final SubstitutionInfo substInfo : targetlist) {
                final String text = substInfo.toText(isPrintSubstSubclass, false, param._daitaiTextNotPrintIchibu, param._printEachSubclassCreditSubstZenbu);
                if (param._isOutputDebugSubst) {
                    log.info(debug + " : add " + divCd + ":" + key0 + ":" + typeFlg0 + " ... SubstitutionInfo " + substInfo._substitutionSubclasscd + ":" + substInfo._substClassname + " (" + substInfo._attendSubclassList + ") = " + text);
                }
                rtn.add(text);
            }
            return rtn;
        }

        /**
         * 教科コードが90以外の科目のすべてのキーの代替科目備考を得る
         * @return
         */
        public List<String> getDaitaiBikoNo90All(final String debug, final Param param) {
            return getDaitaiBiko(debug, divNo90, "NO_KEY", true, null, param);
        }

        protected static class SubstitutionInfo implements Comparable<SubstitutionInfo> {
            final Param _param;
            final String _substitutionTypeFlg;
            final String _substitutionClassCd;
            final String _substitutionSchoolKind;
            final String _substitutionCurriculumCd;
            final String _substitutionSubclasscd;
            final String _substClassname;
            final String _substSubclassName;
            Map<String, Integer> _creditsMstCredits;
            int _studyrecCredits;
            final List<AttendSubclass> _attendSubclassList;

            public SubstitutionInfo(
                    final Param param,
                    final String substitutionTypeFlg,
                    final String substitutionClassCd,
                    final String substitutionSchoolKind,
                    final String substitutionCurriculumCd,
                    final String substitutionSubclassCd,
                    final String substClassName,
                    final String substSubclassName
                    ) {
                _param = param;
                _substitutionTypeFlg = substitutionTypeFlg;
                _substitutionClassCd = substitutionClassCd;
                _substitutionSchoolKind = substitutionSchoolKind;
                _substitutionCurriculumCd = substitutionCurriculumCd;
                _substitutionSubclasscd = substitutionSubclassCd;
                _substClassname = substClassName;
                _substSubclassName = substSubclassName;
                _attendSubclassList = new LinkedList<AttendSubclass>();
            }

            public void addAttendSubclass(final AttendSubclass attendSubclass) {
                _attendSubclassList.add(attendSubclass);
            }

            public int compareTo(SubstitutionInfo o) {
                int rtn;
                rtn = _substitutionClassCd.compareTo(o._substitutionClassCd);
                if (0 != rtn) return rtn;
                rtn = _substitutionSubclasscd.compareTo(o._substitutionSubclasscd);
                return rtn;
            }

            private static class AttendSubclass {
                final String _subclassCd2SubclassCd;
                final String _subclassCd;
                final String _className;
                final String _subclassName;
                /** 代替元科目のSCHREG_STUDYREC_DAT単位数 */
                final Map<String, String> _yearAttendCreditMap; // Map<SCHREG_STUDYREC_DAT.YEAR, SCHREG_STUDYREC_DAT.GET_CREDIT + SCHREG_STUDYREC_DAT.ADD_CREDIT>
                /** 代替元科目の単位マスタ単位数 */
                final Map<String, String> _yearSubstitutionCreditMap;
                public AttendSubclass(
                        final String subclassCd2SubclassCd,
                        final String subclassCd,
                        final String className,
                        final String subclassName) {
                    _subclassCd2SubclassCd = subclassCd2SubclassCd;
                    _subclassCd = subclassCd;
                    _className = className;
                    _subclassName = subclassName;
                    _yearAttendCreditMap = new TreeMap<String, String>();
                    _yearSubstitutionCreditMap = new TreeMap<String, String>();
                }

                public static AttendSubclass getAttendSubclass(final String subclassCd2SubclassCd, final List<AttendSubclass> list) {
                    AttendSubclass rtn = null;
                    for (final AttendSubclass asub : list) {
                        if (asub._subclassCd2SubclassCd.equals(subclassCd2SubclassCd)) {
                            rtn = asub;
                            break;
                        }
                    }
                    return rtn;
                }

                /**
                 * 代替元科目の単位マスタ単位数
                 * @return
                 */
                public String getTotalSubstitutionCredit() {
                    return defstr(Util.integerSum(Util.asInt(_yearSubstitutionCreditMap.values())), (String[]) null);
                }

                /**
                 * 代替元科目のSCHREG_STUDYREC_DAT単位数
                 * @return
                 */
                public String attendCredit() {
                    return defstr(Util.integerSum(Util.asInt(_yearAttendCreditMap.values())), (String[]) null);
                }

                public String toString() {
                    return "AttendSubclass(" + _subclassCd + ":" + _subclassName + ", attCredit = " + _yearAttendCreditMap + ", yearSubstitutionCreditMap = " + _yearSubstitutionCreditMap + ")";
                }
            }

            public static SubstitutionInfo getSubstitutionInfo(final String substitutionTypeFlg, final String substitutionClassCd, final String substitutionSchoolKind, final String substitutionCurriculumCd, final String substitutionSubclassCd, final List<SubstitutionInfo> list) {
                SubstitutionInfo rtn = null;
                for (final SubstitutionInfo info : list) {
                    if (info._substitutionTypeFlg.equals(substitutionTypeFlg) &&
                            info._substitutionClassCd.equals(substitutionClassCd) &&
                            info._substitutionSchoolKind.equals(substitutionSchoolKind) &&
                            info._substitutionCurriculumCd.equals(substitutionCurriculumCd) &&
                            info._substitutionSubclasscd.equals(substitutionSubclassCd)) {
                        rtn = info;
                        break;
                    }
                }
                return rtn;
            }

            public String toText(
                    final boolean isPrintSubstSubclass,
                    final boolean isPrintDebug,
                    final boolean daitaiTextIsNotPrintIchibu,
                    final boolean printEachSubclassCreditSubstZenbu) {
//            	final String format;
//            	if (_param._isMieken) {
//            		format = "{{代替先科目}}は{{代替元科目}}{{代替元単位数}}単位で{{代替名}}";
//            	} else {
//            		format = "{{代替先科目}}は{{代替元科目}}で{{単位数}}単位{{代替名}}";
//            	}
//            	String rtn = format;
                final String daitaiSakiKamokumei;
                if (isPrintSubstSubclass) {
                    final String subclasscd = isPrintDebug ? _substitutionSubclasscd  + ":" : "";
                    if (_90.equals(_substitutionClassCd)) { // 教科コードが90の科目は教科名を表示しない。
                        daitaiSakiKamokumei = subclasscd + _substSubclassName + "は";
                    } else {
                        daitaiSakiKamokumei = subclasscd + _substClassname + "・" + _substSubclassName + "は";
                    }
                } else {
                    daitaiSakiKamokumei = "";
                }
                final List<String> attendTextList = new ArrayList<String>();
                for (final AttendSubclass asub : _attendSubclassList) {
                    final String subclasscd = isPrintDebug ? asub._subclassCd + ":" : "";
                    final String daitaiMotoKamokumei;
                    if (_90.equals(_substitutionClassCd)) {
                        daitaiMotoKamokumei = asub._subclassName;
                    } else {
                        daitaiMotoKamokumei = asub._className + "・" + asub._subclassName;
                    }
                    final String tanni;
                    if (_param._z010.in(Z010Info.Mieken)) {
                        tanni = Util.append(asub.attendCredit(), "単位") + "で";
                    } else if (printEachSubclassCreditSubstZenbu && StudyrecSubstitutionNote.ZENBU.equals(_substitutionTypeFlg)) {
                        tanni = "で" + asub.getTotalSubstitutionCredit() + "単位";
                    } else {
                        tanni = "";
                    }
                    attendTextList.add(subclasscd + daitaiMotoKamokumei + tanni);
                }
                final String attendText = Util.mkString(attendTextList, "、");
                final String totaltanni;
                String totalSubstitutionCredit = null;
                for (final AttendSubclass asub : _attendSubclassList) {
                    totalSubstitutionCredit = Util.addDigits(totalSubstitutionCredit, asub.getTotalSubstitutionCredit());
                }
                totalSubstitutionCredit = defstr(totalSubstitutionCredit);

                if (_param._z010.in(Z010Info.Mieken)) {
                    // 代替先単位印字しない
                    totaltanni = "";
                } else if (StudyrecSubstitutionNote.ICHIBU.equals(_substitutionTypeFlg)) {
                    final int creditsMstCredits = Util.defObject(Util.integerSum(_creditsMstCredits.values()), 0);
                    final int subtractCredit = creditsMstCredits - _studyrecCredits;
                    final StringBuffer debugString = new StringBuffer();
                    if (isPrintDebug && _param._isOutputDebugSubst) {
                        debugString.append(" " + daitaiSakiKamokumei + " daitaitanni : crem - studyrec = " + creditsMstCredits + "(" + _creditsMstCredits + ") - " + _studyrecCredits + " = " + subtractCredit);
                    }
                    int substCredit;
                    if (NumberUtils.isDigits(totalSubstitutionCredit)) {
                        if (subtractCredit < 0) {
                            substCredit = Integer.parseInt(totalSubstitutionCredit);
                            if (isPrintDebug && _param._isOutputDebugSubst) {
                                debugString.append("  substCredit(total) : " + substCredit);
                            }
                        } else {
                            substCredit = Math.min(Integer.parseInt(totalSubstitutionCredit), subtractCredit);
                            if (isPrintDebug && _param._isOutputDebugSubst) {
                                debugString.append("  substCredit : min(" + totalSubstitutionCredit + ", " + subtractCredit + ") = " + substCredit);
                            }
                        }
                        totaltanni = "で" + String.valueOf(substCredit) + "単位";
                        //log.info(" substCredit1 = " + _creditsMstCredits + " - " + _studyrecCredits + " = " + (substCredit0) + ", totalSubstitutionCredit = " + totalSubstitutionCredit + ", substCredit = " + substCredit);
                    } else {
                        totaltanni = "で" + String.valueOf(subtractCredit) + "単位";
                        //log.info(" substCredit2 = " + _creditsMstCredits + " - " + _studyrecCredits + " = " + (substCredit) + ", totalSubstitutionCredit = " + totalSubstitutionCredit + ", subtractCredit = " + subtractCredit);
                    }
                    if (isPrintDebug && _param._isOutputDebugSubst) {
                        log.info(debugString);
                    }
                } else { // if (StudyrecSubstitutionNote.ZENBU.equals(_substitutionTypeFlg)
                    if (printEachSubclassCreditSubstZenbu) {
                        totaltanni = "";
                    } else {
                        totaltanni = "で" + totalSubstitutionCredit + "単位";
                    }
                }
                final String daitaiMei;
                if (daitaiTextIsNotPrintIchibu) {
                    daitaiMei = "代替";
                } else if (StudyrecSubstitutionNote.ICHIBU.equals(_substitutionTypeFlg)) {
                    daitaiMei = "一部代替";
                } else if (_param._z010.in(Z010Info.Mieken)) {
                    daitaiMei = "全部代替";
                } else {
                    daitaiMei = "代替";
                }
                return daitaiSakiKamokumei + attendText + totaltanni + daitaiMei;
            }

            public String toString() {
                return "SubstitutionInfo(" + toText(true, true, false, true) + ")";
            }
        }
    }

    protected static class HexamEntremarkDat {
        final Map<String, String> _row;
        HexamEntremarkDat(final Map<String, String> row) {
            _row = row;
        }
        String data(final String fieldname) {
            return getString(_row, fieldname);
        }
        String attendrecRemark() {
            return data("ATTENDREC_REMARK");
        }
        String attendrecRemarkSlashFlg() {
            return data("ATTENDREC_REMARK_SLASH_FLG");
        }
        String specialactrec() {
            return data("SPECIALACTREC");
        }
        String trainRef() {
            return data("TRAIN_REF");
        }
        String trainRef1() {
            return data("TRAIN_REF1");
        }
        String trainRef2() {
            return data("TRAIN_REF2");
        }
        String trainRef3() {
            return data("TRAIN_REF3");
        }
        String datTotalstudyact() {
            return data("DAT_TOTALSTUDYACT");
        }
        String datTotalstudyactSlashFlg() {
            return data("DAT_TOTALSTUDYACT_SLASH_FLG");
        }
        String datTotalstudyval() {
            return data("DAT_TOTALSTUDYVAL");
        }
        String datTotalstudyvalSlashFlg() {
            return data("DAT_TOTALSTUDYVAL_SLASH_FLG");
        }
        public String toString() {
            return "HexamEntremarkDat(" + Util.debugMapToStr("", _row) + ")";
        }
    }

    protected static class AttendrecDat {
        static final String ATTEND_1 = "ATTEND_1";
        static final String SUSP_MOUR = "SUSP_MOUR";
        static final String ABROAD = "ABROAD";
        static final String REQUIREPRESENT = "REQUIREPRESENT";
        static final String ATTEND_6 = "ATTEND_6";
        static final String PRESENT = "PRESENT";

        final Map<String, String> _row;
        AttendrecDat(final Map<String, String> row) {
            _row = row;
        }
        public String schoolcd() {
            return getString(_row, "SCHOOLCD");
        }
        //授業日数
        public String attend1() {
            return getString(_row, ATTEND_1);
        }
        //出停・忌引日数
        public String suspMour() {
            return getString(_row, SUSP_MOUR);
        }
        //留学日数
        public String abroad() {
            return getString(_row, ABROAD);
        }
        //要出席日数
        public String requirepresent() {
            return getString(_row, REQUIREPRESENT);
        }
        //欠席日数
        public String attend6() {
            return getString(_row, ATTEND_6);
        }
        //出席日数
        public String present() {
            return getString(_row, PRESENT);
        }
        //本校(学校区分0)の出席日数
        public String schoolcd0Present() {
            return getString(_row, "SCHOOLCD0_PRESENT");
        }

        public boolean isEmpty() {
            return _row.isEmpty();
        }

        @Override
        public Object clone() {
            return new AttendrecDat(new HashMap(_row));
        }

        public static AttendrecDat add(final Param param, final AttendrecDat a, final AttendrecDat b) {
            if (null == a) {
                return (AttendrecDat) b.clone();
            }
            if (null == b) {
                return (AttendrecDat) a.clone();
            }
            final AttendrecDat rtn = (AttendrecDat) a.clone();
            for (final String key : Arrays.asList(ATTEND_1, SUSP_MOUR, ABROAD, REQUIREPRESENT, ATTEND_6, PRESENT)) {
                rtn._row.put(key, Util.add(getString(a._row, key), getString(b._row, key)));
            }
            if (param._isOutputDebugCalc) {
                param.logOnce(" attend " + a + " + attend " + b + " = " + rtn);
            }
            return rtn;
        }

        protected static boolean isNull(final AttendrecDat att, final CommonPrintData printData) {
            if (null == att || att.isEmpty()) return true;
            boolean rtn = true;
            if (printData._isConfigFormAttendAllSlash) {
                return Util.toInt(att.attend1(), 0) == 0;
            }
            final String[] check = new String[] {att.attend1(), att.suspMour(), att.abroad(), att.requirepresent(), att.attend6(), att.present()};
            for (int i = 0; i < check.length; i++) {
                if (Util.toInt(check[i], 0) > 0) {
                    rtn = false;
                }
            }
            return rtn;
        }

        public static boolean isAllNull(final Param param, final CommonPrintData printData, final List<Title> titleList) {
            boolean rtn = true;
            for (String strKey : printData._attendMap.keySet()) {
                if (null == strKey) {
                    continue;
                }
                if (0 == Integer.parseInt(strKey)) {
                    strKey = "0";
                }
                final Title title = Title.getTitle(param, titleList, strKey);
                if (null == title) {
                    continue;
                }
                final AttendrecDat att = printData.getAttendrecDat(strKey);
                if (!AttendrecDat.isNull(att, printData)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        public static Map<String, AttendrecDat> getAttendMap(final DB2UDB db2, final Param param, final CommonPrintData printData) {
            final Map<String, AttendrecDat> attendMap = new HashMap<String, AttendrecDat>();
            if (printData._notUseAttend) {
                return attendMap;
            }
            final String sql = getAttendrecSql(printData, param);

            final Object[] sqlParam;
            if (param._z010.in(Z010Info.Musashinohigashi)) {
                sqlParam = new Object[] {printData._schregno, printData._schregno, printData._year};
            } else {
                sqlParam = new Object[] {printData._schregno, printData._year};
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql, sqlParam)) {

                final String year = getString(row, "YEAR");
                if (null == year) { continue; }
                final int intKey = Integer.parseInt(year);
                final String k = (0 == intKey) ? "0" : year;
                if (0 > intKey) {
                    continue;
                }
                final Title title = Title.getTitle(param, printData.titleValues(), year);
                if (null == title) {
                    continue;
//                        if (isGakunensei() && printData._ryunenYears.contains(year)) {
//                            // log.debug(" 出欠データが存在するが留年している年度のため表示対象外 = " + year);
//                        } else {
//                            final Title title = new Title(k, intKey, year, annual);
//                            printData._titles.put(k, title);
//                        }
                }
                attendMap.put(k, new AttendrecDat(row));
            }
            return attendMap;
        }

        /**
         *  出欠記録取得のSQL
         */
        private static String getAttendrecSql(final CommonPrintData printData, final Param param) {

            final String tname1;
            if (param._isPrintGrd) {
                tname1 = "GRD_ATTENDREC_DAT";
            } else {
                tname1 = "SCHREG_ATTENDREC_DAT";
            }
            final boolean useLateEarly = param._z010.in(Z010Info.Musashinohigashi);

            final StringBuffer sql = new StringBuffer();
            if (useLateEarly) {
                sql.append(" WITH SEMES AS ( ");
                sql.append("    SELECT ");
                sql.append("        YEAR, SCHREGNO, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
                sql.append("    FROM ");
                sql.append("        ATTEND_SEMES_DAT ");
                sql.append("   WHERE ");
                sql.append("        SCHREGNO = ? ");
                sql.append("   GROUP BY ");
                sql.append("        YEAR, SCHREGNO ");
                sql.append(" )");
            }
            sql.append(" SELECT DISTINCT ");
            sql.append("     T1.YEAR,");
            sql.append("     T1.SCHOOLCD,");
            sql.append("     ANNUAL,");
            sql.append("     VALUE(CLASSDAYS,0) AS CLASSDAYS,");                           //授業日数
            sql.append("     CASE WHEN S1.SEM_OFFDAYS = '1' ");

            if (CommonPrintData._shingakuYou == printData._output && param.isKindaifuzoku()) {
               /**
               *
               *  出欠記録(SCHREG_ATTENDREC_DAT)ＳＱＬの作成
               *  2005/07/10 Build 授業日数は休学日数を引かない日数とする
               *  2005/10/26 Modify 授業日数は留学日数と休学日数を引いた値とする
               *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
               */
               sql.append("          THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
               sql.append("          ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            } else {
               /**
               *
               *  出欠記録(SCHREG_ATTENDREC_DAT)ＳＱＬの作成
               *
               *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
               */
               sql.append("          THEN VALUE(CLASSDAYS,0) ");
               sql.append("          ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            }
            sql.append("          END AS ATTEND_1,"); //授業日数-休学日数:1
            sql.append("     VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,");         //出停・忌引
            sql.append("     VALUE(SUSPEND,0) AS SUSPEND,");                               //出停:2
            sql.append("     VALUE(MOURNING,0) AS MOURNING,");                             //忌引:3
            sql.append("     VALUE(ABROAD,0) AS ABROAD,");                                 //留学:4
            sql.append("     VALUE(REQUIREPRESENT,0)  + CASE WHEN S1.SEM_OFFDAYS = '1' ");
            sql.append("          THEN VALUE(OFFDAYS,0) ");
            sql.append("          ELSE 0 ");
            sql.append("          END AS REQUIREPRESENT,"); //要出席日数:5
            sql.append("     VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + CASE WHEN S1.SEM_OFFDAYS = '1' ");
            sql.append("          THEN VALUE(OFFDAYS,0) ");
            sql.append("          ELSE 0 ");
            sql.append("          END AS ATTEND_6,"); //病欠＋事故欠（届・無）:6
            sql.append("     VALUE(PRESENT,0) AS PRESENT,");                               //出席日数:7
            sql.append("     VALUE(SCHOOLCD0_PRESENT,0) AS SCHOOLCD0_PRESENT ");           //出席日数(内本校)
            if (useLateEarly) {
                sql.append("       , VALUE(SEMES.LATE,0) AS LATE"); // 遅刻日数
                sql.append("       , VALUE(SEMES.EARLY,0) AS EARLY"); // 早退日数
            }
            sql.append(" FROM ");
            sql.append("     (");
            sql.append("         SELECT ");
            sql.append("             SCHREGNO,");
            sql.append("             MAX(SCHOOLCD) AS SCHOOLCD,");
            sql.append("             YEAR,");
            sql.append("             ANNUAL,");
            sql.append("             SUM(CLASSDAYS) AS CLASSDAYS,");
            sql.append("             SUM(OFFDAYS) AS OFFDAYS,");
            sql.append("             SUM(ABSENT) AS ABSENT,");
            sql.append("             SUM(SUSPEND) AS SUSPEND,");
            sql.append("             SUM(MOURNING) AS MOURNING,");
            sql.append("             SUM(ABROAD) AS ABROAD,");
            sql.append("             SUM(REQUIREPRESENT) AS REQUIREPRESENT,");
            sql.append("             SUM(SICK) AS SICK,");
            sql.append("             SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,");
            sql.append("             SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,");
            sql.append("             SUM(PRESENT) AS PRESENT, ");
            sql.append("             SUM(CASE WHEN SCHOOLCD = '0' THEN PRESENT END) AS SCHOOLCD0_PRESENT ");
            sql.append("         FROM ");
            sql.append("  " + tname1 + "  ");
            sql.append("         WHERE ");
            sql.append("                 SCHREGNO = ? ");
            sql.append("             AND YEAR <= ? ");
            if ("on".equals(printData.property(Property.tyousasyoNotPrintAnotherAttendrec))) {
                sql.append("AND SCHOOLCD <> '1' ");
            }
            sql.append("         GROUP BY ");
            sql.append("             SCHREGNO,");
            sql.append("             ANNUAL,");
            sql.append("             YEAR ");
            sql.append("    )T1 ");
            sql.append("    LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._tableInfo._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("    AND S1.SCHOOL_KIND = 'H' ");
            }
            if (useLateEarly) {
                sql.append("     LEFT JOIN SEMES ON SEMES.YEAR = T1.YEAR AND SEMES.SCHREGNO = T1.SCHREGNO ");
            }
            sql.append("    ORDER BY ");
            sql.append("        T1.ANNUAL");
            return sql.toString();
        }

        public String toString() {
            return "AttendrecDat(" + Util.debugMapToStr("", _row) + ")";
        }
    }

    protected static class MedexamDetDat {
        final Map<String, String> _row;
        MedexamDetDat(final Map<String, String> row) {
            _row = row;
        }
        public String get(final String field) {
            return getString(_row, field);
        }
        public String rBarevisionMark() {
            return get("R_BAREVISION_MARK");
        }
        public String rVisionMark() {
            return get("R_VISION_MARK");
        }
        public String lBarevisionMark() {
            return get("L_BAREVISION_MARK");
        }
        public String lVisionMark() {
            return get("L_VISION_MARK");
        }
        public String rBarevision() {
            return get("R_BAREVISION");
        }
        public String rVision() {
            return get("R_VISION");
        }
        public String lBarevision() {
            return get("L_BAREVISION");
        }
        public String lVision() {
            return get("L_VISION");
        }
        public String rEar() {
            return get("R_EAR");
        }
        public String lEar() {
            return get("L_EAR");
        }

        public boolean isPrintVisionMark(final Param param, final String visionMark) {
            if ("X".equals(visionMark) && param._z010.in(Z010Info.naraken)) {
                return false;
            }
            return true;
        }

        public boolean isPrintEar(final Param param, final String ear) {
            if ("99".equals(ear) && param._z010.in(Z010Info.naraken)) {
                return false;
            }
            return true;
        }

        public boolean isEmpty() {
            return null == _row || _row.isEmpty();
        }
        public boolean isEarSlash(final Param param, final String ear) {
            final Map<String, String> f010Ear = Util.getMappedHashMap(param._f010namecd2Map, ear);
            final String earClearFlg = getString(f010Ear, "NAMESPARE2");
            return NumberUtils.isDigits(earClearFlg);
        }
        public String getEarName(final Param param, final String ear) {
            final Map<String, String> f010Ear = Util.getMappedHashMap(param._f010namecd2Map, ear);
            final String earName = defstr(getString(f010Ear, "NAME2"), getString(f010Ear, "NAME1"));
            return earName;
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

        public static <T> List<Indexed<T>> indexed(final List<T> list) {
            return indexed(0, list);
        }

        public static <T> List<Indexed<T>> indexed(final int startIdx, final List<T> list) {
            final List<Indexed<T>> rtn = new ArrayList<Indexed<T>>();
            int idx = startIdx;
            for (final T t : list) {
                rtn.add(new Indexed(idx, t));
                idx += 1;
            }
            return rtn;
        }
    }

    protected static class Tuple<A, B> implements Comparable<Tuple<A, B>> {
        final A _first;
        final B _second;
        private Tuple(final A first, final B second) {
            _first = first;
            _second = second;
        }
        public static <A, B> Tuple<A, B> of(A a, B b) {
            return new Tuple<A, B>(a, b);
        }
        public int hashCode() {
            return _first.hashCode() * 234201017 + _second.hashCode();
        }
        public int compareTo(final Tuple<A, B> o) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == o._first && !(o._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(o._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == o._second && !(o._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(o._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    protected static class ShokenSize {
        boolean _useKeta;
        final int _keta;
        final int _moji;
        final int _gyo;
        public ShokenSize(final int moji, final int gyo) {
            _moji = moji;
            _gyo = gyo;
            _keta = _moji * 2;
        }
        public ShokenSize(final int keta, final int moji, final int gyo) {
            _moji = moji;
            _gyo = gyo;
            _keta = keta;
        }
        public int keta() {
            if (_useKeta) {
                return _keta;
            }
            return _moji * 2;
        }
        public ShokenSize setMoji(final int moji) {
            return new ShokenSize(moji, _gyo);
        }
        public ShokenSize addKeta(final int keta) {
            final ShokenSize newSize = new ShokenSize(_keta + keta, -1, _gyo);
            newSize._useKeta = true;
            return newSize;
        }
        public static ShokenSize getWithKeta(final int defketa, final int defgyo) {
            final ShokenSize size = new ShokenSize(defketa, -1, defgyo);
            size._useKeta = true;
            return size;
        }
        public static ShokenSize get(final String prop, final int defmoji, final int defgyo) {
            final int moji = getParamSizeNum(prop, 0);
            final int gyo = getParamSizeNum(prop, 1);

            final ShokenSize size;
            if (moji == -1 || gyo == -1) {
                size = new ShokenSize(defmoji, defgyo);
            } else {
                size = new ShokenSize(moji, gyo);
            }
            return size;
        }

        /**
         * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
         * @param prop サイズタイプのパラメータ文字列
         * @param pos split後のインデクス (0:w, 1:h)
         * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
         */
        public static int getParamSizeNum(final String prop, final int pos) {
            if (StringUtils.isBlank(prop)) {
                return -1;
            }
            int num = -1;
            String[] nums = StringUtils.split(prop, prop.indexOf("+*+") >= 0 ? "+*+" : " * ");
            if (!(0 <= pos && pos < nums.length)) {
                num = -1;
            } else {
                try {
                    num = Integer.valueOf(nums[pos]).intValue();
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
            }
            return num;
        }

        public String toString() {
            return "ShokenSize(moji = " + _moji + ", keta = " + keta() + ", gyo = " + _gyo + ")";
        }
    }

    enum Z010Info {
        KINDAI("近大附属高校"),
        KINJUNIOR("近大附属中学"),
        Jisyuukan("jisyukan", "自修館"),
        Hosei("HOUSEI", "法政"),
        Musashi("MUSASHI", "武蔵"),
        Kumamoto("kumamoto", "熊本県"),
        Hirokoku("hirogaku", "広島国際"),
        Tottori("tottori", "鳥取県"),
        Kyoai("kyoai", "共愛"),
        Meiji("meiji", "明治"),
        Tokiwa("tokiwa", "常磐"),
        Chukyo("chukyo", "中京"),
        Nishiyama("nishiyama", "西山"),
        Kaijyo("kaijyo", "海城"),
        Bunkyo("bunkyo", "文京"),
        Miyagiken("miyagiken", "宮城県"),
        Seijyo("seijyo", "成城"),
        Chiben("CHIBEN", "智辯"),
        Mieken("mieken", "三重県"),
        Sundaikoufu("sundaikoufu", "駿台甲府"),
        Musashinohigashi("musashinohigashi", "武蔵野東"),
        Rakunan("rakunan", "洛南"),
        Sapporo("sapporo", "札幌開成"),
        Fukuiken("fukuiken", "福井県"),
        Sakae("sakae", "埼玉栄"),
        Higashiosaka("higashiosaka", "東大阪"),
        ChiyodaKudan("chiyoda", "九段"),
        Tosa("tosa", "土佐塾"),
        Hagoromo("hagoromo", "羽衣"),
        Hirokoudai("hirokoudai", "広島工業大学付属"),
        Tokiwagi("tokiwagi", "常盤木"),
        Osakatoin("osakatoin", "大阪桐蔭"),
        risshisha("立志舎"),
        Yamamura("yamamura", "山村学園"),
        Tosajoshi("tosajoshi", "土佐女子"),
        naraken("奈良県"),
        jyoto("福岡工業大附属城東高校"),
        TamagawaSei("Tamagawa-sei", "玉川聖"),
        Meikei("meikei", "茗渓"),
        RitsumeikanKeisho("Keisho", "立命館慶祥"),
        RitsumeikanMoriyama("Moriyama", "立命館守山"),
        RitsumeikanNagaokakyo("Nagaokakyo", "立命館長岡京"),
        Hibarigaoka("hibarigaoka", "雲雀丘"),
        KaichiTsushin("ktsushin", "開智通信制"),
        KaichiMirai("kmirai", "開智未来"),
        KaichiNozomi("knozomi", "開智望"),
        KaichiSougou("ksogo", "開智総合部"),
        KaichiIkkan("kikan", "開智一貫"),
        KaichiNihonbashi("knihon", "開智日本橋"),
        KaichiKoutou("kkotou", "開智高等部"),
        NaganoSeisen("seisen", "長野清泉"),
        Nagisa("nagisa", "広島なぎさ"),
        Komazawa("koma", "駒沢大学"),
        Reitaku("reitaku", "麗澤"),
        Matsudo("matsudo", "専大松戸"),
        Ryukei("ryukei", "流経大"),
        Kenja("", "賢者");

        private final String _name1;
        final String _title;
        Z010Info(final String title) {
            this(null, title);
        }
        Z010Info(final String z010Name1, final String title) {
            _name1 = z010Name1;
            _title = title;
        }
        public static Z010Info fromName1(final String z010Name1) {
            for (final Z010Info z010 : Z010Info.values()) {
                if (defstr(z010._name1, z010.name()).equals(z010Name1)) {
                    return z010;
                }
            }
            return Kenja;
        }
        public boolean in(final Z010Info... z010s) {
            return ArrayUtils.contains(z010s, this);
        }
        // 開智はTrue
        public boolean isKaichi() {
            return in(KaichiMirai, KaichiNozomi, KaichiSougou, KaichiIkkan, KaichiNihonbashi, KaichiKoutou);
        }
        // 立命館はTrue
        public boolean isRitsumeikan() {
            return in(RitsumeikanKeisho, RitsumeikanMoriyama, RitsumeikanNagaokakyo);
        }
    }

    protected static class TableInfo {
        final boolean _hasANOTHER_CLASS_MST;
        final boolean _hasANOTHER_SUBCLASS_MST;
        final boolean _hasSCHREG_STUDYREC_DETAIL_DAT;        // テーブル SCHREG_STUDYREC_DETAIL_DATがあるか
        final boolean _hasSTUDYREC_PROV_FLG_DAT_PROV_SEMESTER;
        final boolean _hasMAJOR_MST_MAJORNAME2;
        final boolean _hasCERTIF_SCHOOL_DAT;
        final boolean _hasSUBCLASS_DETAIL_DAT;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasCOURSECODE_MST_COURSECODEABBV1;
        final boolean _hasCOURSECODE_MST_COURSECODEABBV3;
        final boolean _hasHEXAM_ENTREMARK_DAT_ATTENDREC_REMARK_SLASH_FLG;
        final boolean _hasHEXAM_ENTREMARK_DAT_TOTALSTUDYACT_SLASH_FLG;
        final boolean _hasHEXAM_ENTREMARK_HDAT_TOTALSTUDYACT_SLASH_FLG;
        final boolean _hasGRD_HEXAM_ENTREMARK_DAT_ATTENDREC_REMARK_SLASH_FLG;
        final boolean _hasGRD_HEXAM_ENTREMARK_DAT_TOTALSTUDYACT_SLASH_FLG;
        final boolean _hasGRD_HEXAM_ENTREMARK_HDAT_TOTALSTUDYACT_SLASH_FLG;
        final boolean _hasSCHREG_TRANSFER_DAT_ABROAD_PRINT_DROP_REGD;
        int _SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT_count = 0;

        TableInfo(final DB2UDB db2) {
            _hasCERTIF_SCHOOL_DAT = KnjDbUtils.setTableColumnCheck(db2, "CERTIF_SCHOOL_DAT", null);
            _hasANOTHER_CLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
            _hasANOTHER_SUBCLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);
            _hasSCHREG_STUDYREC_DETAIL_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_STUDYREC_DETAIL_DAT", null);
            _hasSUBCLASS_DETAIL_DAT = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_DETAIL_DAT", null);
            _hasSTUDYREC_PROV_FLG_DAT_PROV_SEMESTER = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", "PROV_SEMESTER");
            _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasCOURSECODE_MST_COURSECODEABBV1 = KnjDbUtils.setTableColumnCheck(db2, "COURSECODE_MST", "COURSECODEABBV1");
            _hasCOURSECODE_MST_COURSECODEABBV3 = KnjDbUtils.setTableColumnCheck(db2, "COURSECODE_MST", "COURSECODEABBV3");
            _hasHEXAM_ENTREMARK_DAT_ATTENDREC_REMARK_SLASH_FLG = KnjDbUtils.setTableColumnCheck(db2, "HEXAM_ENTREMARK_DAT", "ATTENDREC_REMARK_SLASH_FLG");
            _hasHEXAM_ENTREMARK_DAT_TOTALSTUDYACT_SLASH_FLG = KnjDbUtils.setTableColumnCheck(db2, "HEXAM_ENTREMARK_DAT", "TOTALSTUDYACT_SLASH_FLG");
            _hasHEXAM_ENTREMARK_HDAT_TOTALSTUDYACT_SLASH_FLG = KnjDbUtils.setTableColumnCheck(db2, "HEXAM_ENTREMARK_HDAT", "TOTALSTUDYACT_SLASH_FLG");
            _hasGRD_HEXAM_ENTREMARK_DAT_ATTENDREC_REMARK_SLASH_FLG = KnjDbUtils.setTableColumnCheck(db2, "GRD_HEXAM_ENTREMARK_DAT", "ATTENDREC_REMARK_SLASH_FLG");
            _hasGRD_HEXAM_ENTREMARK_DAT_TOTALSTUDYACT_SLASH_FLG = KnjDbUtils.setTableColumnCheck(db2, "GRD_HEXAM_ENTREMARK_DAT", "TOTALSTUDYACT_SLASH_FLG");
            _hasGRD_HEXAM_ENTREMARK_HDAT_TOTALSTUDYACT_SLASH_FLG = KnjDbUtils.setTableColumnCheck(db2, "GRD_HEXAM_ENTREMARK_HDAT", "TOTALSTUDYACT_SLASH_FLG");
            _hasSCHREG_TRANSFER_DAT_ABROAD_PRINT_DROP_REGD = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_TRANSFER_DAT", "ABROAD_PRINT_DROP_REGD");
            _SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT_count = KnjDbUtils.query(db2, "SELECT * FROM SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT ").size();
        }
    }

    protected static enum Parameter {
        CTRL_YEAR,
        CTRL_DATE,
        HYOTEI,
        GVAL_CALC_CHECK,
        PRINT_AVG_RANK,
        FORM6,
        NENYOFORM,
        CERTIFKIND,
        CERTIFKIND2,
        RISYU,
        MIRISYU,
        TANIPRINT_SOUGOU,
        TANIPRINT_RYUGAKU,
        OUTPUT_PRINCIPAL,
        SONOTAJUUSYO,
        HANKI_NINTEI,
        SELECT_PATTERN,
        PRINT_STAMP, // KNJG010.class
        cmd,
        KNJE070_CHECK_PRINT_STAMP_HR_STAFF, // KNJE070D 記載責任者印を出力する 1:する 2:しない
        KNJE070_CHECK_PRINT_STAMP_PRINCIPAL, // KNJE070D 学校印を出力する 1:する 2:しない
        RYUGAKU_CREDIT, // KNJG010.class
        SOUGAKU_CREDIT, // KNJG010.class
        PRINT_GRD, // KNJI060_1.class
        tyousasyo2020, // KNJE070D、KNJE010D、KNJE011D 諸事項6分割
        KNJE070D_PRINTHEADERNAME, // KNJE070D、KNJG010 偶数ページに氏名を出力する 1:する 2:しない
        tyousasyoHanasuClasscd,
        avgGradesScale,
        remarkOnly, // KNJE010D、 KNJE011D
        certifSchoolOnly, // KNJZ251.class
        hyoteiheikin__table("hyoteiheikin.table"), // KNJE370IHyoteiHeikin.class
        hyoteiheikin__table__RECORD_SCORE_DAT__SEMESTER("hyoteiheikin.table.RECORD_SCORE_DAT.SEMESTER"), // KNJE370IHyoteiHeikin.class
        notUseDaitai, // KNJE070_1.getHyoteiHeikinList
        notUseShoken, // KNJE070_1.getHyoteiHeikinList
        notUseAttend, // KNJE070_1.getHyoteiHeikinList
        notOutputLog, // KNJE070_1.getHyoteiHeikinList
        ;
        final String _name;
        Parameter() {
            this(null);
        }
        Parameter(final String name) {
            _name = null == name ? name() : name;
        }
    }

    protected static enum Property {
        tyousasyo_shokenTable_Seq,
        tyousasyo2020shojikouExtends,
        tyousasyo2020FormNen,
        tyousasyoRemarkFieldSize,
        knjf030PrintVisionNumber,
        gaihyouGakkaBetu,
        tyousasho2020NotPrintInnMark,
        tyousasho2020CertifnoPage,
        tyousasho2020AttendremarkGyou,
        tyousasyo2020TotalstudyactGyou,
        tyousasyo2020TotalstudyvalGyou,
        tyousasyo2020specialactrecGyou,
        tyousasyo2020shojikouGyou,
        tyousasho2020SogakuShokenShasen,
        tyousasyo2020seisekiTitleTategaki,
        tyousasyoAttendrecRemarkFieldSize, // 5.出欠備考のフィールドサイズ
        tyousasyoTokuBetuFieldSize, // 6.特別活動のフィールドサイズ
        tyousasyoEMPTokuBetuFieldSize, // 6.特別活動のフィールドサイズ（就職用）
        tyousasyoSpecialactrecFieldSize, // 6.特別活動の記録のフィールドサイズ
        tyousasyoSyusyokuPrintGappeiTougou, // 調査書就職用で合併統合用表記する。
        train_ref_1_2_3_field_size(true), // 7.指導上参考となる諸事項の桁数変更フラグ
        train_ref_1_2_3_gyo_size(true), // 7.指導上参考となる諸事項の行数変更フラグ
        tyousasyoTotalstudyactFieldSize, // 8.総合的な学習の時間の内容フィールドサイズ
        tyousasyoTotalstudyvalFieldSize, // 8.総合的な学習の時間の評価フィールドサイズ
        tyousasyoAttendrecRemarkFieldSizeForPrint, // 5.出欠備考のフィールドサイズ
        tyousasyoSpecialactrecFieldSizeForPrint, // 6.特別活動のフィールドサイズ (印刷)
        train_ref_1_2_3_field_sizeForPrint, // 7.指導上参考となる諸事項の桁数変更フラグ (印刷)
        train_ref_1_2_3_gyo_sizeForPrint, // 7.指導上参考となる諸事項の行数変更フラグ (印刷)
        tyousasyoTotalstudyactFieldSizeForPrint, // 8.総合的な学習の時間の内容フィールドサイズ (印刷)
        tyousasyoTotalstudyvalFieldSizeForPrint, // 8.総合的な学習の時間の評価フィールドサイズ (印刷)
        tyousasyoRemarkFieldSizeForPrint, // 9.備考フィールドサイズ (印刷)
        tyousasyoSougouHyoukaNentani, // 8.総合的な学習の時間の内容・評価の年毎/通年フラグ
        tyousasyoSougouHyoukaNentaniPrintCombined, // 8.総合的な学習の時間の内容・評価の年毎ごとのデータを連結して出力する
        tyousasyoNotPrintAnotherAttendrec, // 前籍校の出席（SCHOOLCD='1'のSCHREG_ATTENDREC_DAT）を含まない
        tyousasyoNotPrintAnotherStudyrec, // 前籍校の成績（SCHOOLCD='1'のSCHREG_STUDYREC_DAT）を表示しない
        tyousasyoNotPrintEnterGrade, // 入学の学年・年次を表示しない
        tyousasyoPrintHomeRoomStaff, // 記載責任者は最終学年もしくは除籍時の学期の担任を印字する（茗渓）
        tyousasyoHankiNintei, // 半期認定
        tyousashoIkkanNotUseDefault4YearForm, // 中高一貫校でデフォルト4年用フォームを使用しない
        tyousasyoPrintGradeCdAsEntGrade, // 調査書入学学年表示 1:GRADE_CDを表示
        tyousashoDaitaiNotContainDropYearCreditMst, // 代替に留年時の単位マスタ単位を含めない
        tyousasyoPrintAttendrecRemarkKaikinDat, // 出欠備考欄にKAIKIN_DATの皆勤・精勤を印字する
        tyousasyoShushokuyouShowTextInn, // 就職用で学校印の「印」を表示
        tyousasyoPrintCoursecodename,
        certifPrintRealName, // 設定によらず戸籍名を印字する
        useClassDetailDat,
        useAddrField2,
        useProvFlg,
        printSubclassLastChairStd,
        useGakkaSchoolDiv,
        useAssessCourseMst,
        useMaruA_avg,
        sogoTankyuStartYear,
        tyousasyoPrintMaruAigai, // 評定平均マークがA以外でも〇を表示する
        KNJE070_PRINT_STAMP, // 印影表示
        KNJE070_PRINT_KISAI_STAMP, // 記載者印影表示
        tyousasyoJiritsuKatsudouRemark,
        tyousashoDaitaiCheckMasterYear,
        tyousashoCheckZenbuDaitaiSubstitutedYear,
        tyousasyoPrintChairSubclassSemester2,
        useTitleShukkou,
        knjg010HakkouPrintInei,
        KNJE070_SCHOOLSTAMP_FILE,
        knje070useSql2,
        stampSizeMm, // 印影サイズ (単位:mm)
        knje070_1StampSizeMm, // 進学用印影サイズ (単位:mm)
        knje070_1StampPositionXmm, // 進学用印影位置X (単位:mm)
        knje070_1StampPositionYmm, // 進学用印影位置Y (単位:mm)
        knje070_2StampSizeMm, // 就職用印影サイズ (単位:mm)
        knje070_2StampPositionXmm, // 就職用印影位置X (単位:mm)
        knje070_2StampPositionYmm, // 就職用印影位置Y (単位:mm)
        staffStampSizeMm, // 記載者印影サイズ (単位:mm)
        tyousasho2020PrintHeaderName,
        knje070_1KisaiStampSizeMm,
        knje070_1KisaiStampPositionXmm,
        knje070_1KisaiStampPositionYmm,
        knje070_2KisaiStampSizeMm,
        knje070_2KisaiStampPositionXmm,
        knje070_2KisaiStampPositionYmm,
        ;

        final String _name;
        final boolean _preferPropertyFile;
        Property() {
            this(false, null);
        }
        Property(final boolean preferPropertyFile) {
            this(preferPropertyFile, null);
        }
        Property(final boolean preferPropertyFile, final String name) {
            _preferPropertyFile = preferPropertyFile;
            _name = null == name ? name() : name;
        }
    }

    protected static class Param {

        final boolean _printEachSubclassCreditSubstZenbu = false;

        static String SCHOOL_KIND = "H";

        static String z010Name2Keiai = "30270254001";
        static String z010Name2Kashiwara = "30270247001";

        final boolean _useTotalstudySlashFlg;
        final boolean _useAttendrecRemarkSlashFlg;

        final TableInfo _tableInfo;
        final String _useCurriculumcd;

        final String _prgid;
        final String _useSyojikou3;   // 指導上参考となる諸事項の分割
        final String _documentroot;
        protected Properties _prgInfoPropertiesFilePrperties;
        final boolean _isSeireki; // 西暦表示するならtrue
        final boolean _dbHasSchoolKindPJH;
        final int _schoolKindStartGradeInt;

        final String _tyousasyoUseEditKinsoku; // 禁則処理
        final boolean _useEditKinsoku; // SVFの禁則フォームの替わりにKNJ_EditKinsokuを使用する
        final boolean _isPrintGrd;
        final Map<String, String> _isNotPrintClassTitle; // 普通/専門教育に関する教科のタイトル表示設定
        final boolean _isShusyokuyouKinkiToitsu; // 近畿統一用紙
        final boolean _isShusyokuyouKinkiToitsu2; // 近畿統一用紙

        final boolean _isOutputDebugAll;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugKinsoku;
        final boolean _isOutputDebugField;
        final boolean _isOutputDebugSvfForm;
        final boolean _isOutputDebugSvfFormCreate;
        final boolean _isOutputDebugSvfFormModify;
        final boolean _isOutputDebugTime;
        final boolean _isOutputDebugSeiseki;
        final boolean _isOutputDebugSubst;
        final boolean _isOutputDebugShoken;
        final boolean _isOutputDebugBase;
        final boolean _isOutputDebugData;
        final boolean _isOutputDebugCalc;
        protected List<String> _outputDebugA4PageList;
        protected List<String> _outputDebugFieldList;
        protected List<String> _outputDebugSql2List;
        protected List<String> _outputDebugVrsOutList;
        private Set<String> _logOnce = new HashSet<String>();
        private Set<String> _logOnceWarn = new HashSet<String>();

        final Z010Info _z010;
        final Map<String, String> _z010Map;
        final String _z010Name1;
        final String _z010Name2;
        final boolean _isHigashiosakaKeiai; // 東大阪敬愛はTrue
        final boolean _isHigashiosakaKashiwara; // 東大阪柏原はTrue
        final String _e028Name1;
        final boolean _isNotOutputSogotekinaGakushunoJikanTanni;
        final boolean _isNotOutputSogotekinaGakushunoJikanTanniIfReplaceSubstitute;
        final String _e019_01_namespare1;
        final Map<String, String> _z002Abbv1Map; // Z002性別

        final Map<String, ClassMst> _classMstMap = new HashMap<String, ClassMst>();
        final Map<String, AnotherClassMst> _anotherClassMstMap = new HashMap<String, AnotherClassMst>();
        final Map<String, SubclassMst> _subclassMstMap = new HashMap<String, SubclassMst>();
        final Map<String, AnotherSubclassMst> _anotherSubclassMstMap = new HashMap<String, AnotherSubclassMst>();

        final Map _sessionCache = new HashMap();
        List<Map<String, String>> _l007List = null;
        final List<String> _e014Name1List;
        final List<String> _d020Name1List;
        List<Map<String, String>> _e050List = Collections.emptyList();
        String _f011Namespare2Max;
        Map<String, Map<String, String>> _f010namecd2Map;

        final SvfFieldAreaInfo _fieldAreaInfo = new SvfFieldAreaInfo();
        final Map<String, PreparedStatement> _psMap = new HashMap();
        final List<BigDecimal> _elapsedPrintTimeList = new ArrayList();
        final List<BigDecimal> _elapsedQueryTimeList = new ArrayList();
        final Set<String> _errorMessageOutputs = new HashSet<String>();

        final boolean _isNotCreditDefault0;
        final boolean _isPrintCompCreditWhenCreditIsZero;
        final boolean _preferEntInfoJ; // 中学の入学情報を使用する

        final String _imagepath;
        final String _slashImagePath;
        final String _knje070GradeSlashImagePath;

        final String _e011cd01Namespare1;
        final int _e011cd02Namespare1;

        boolean _setSogakuKoteiTanni;
        final Map<Integer, BigDecimal> _sogakuKoteiTanniMap = new TreeMap<Integer, BigDecimal>();

        final Map<String, Map<String, String>> _a029NameMstMap;
        final Set<String> _d065NameMstName1Set; // D065に設定された科目コード
        final Map<String, String> _d001NameMstAbbv1Map; // D001に評定変換マップ
        final boolean _daitaiTextNotPrintIchibu;

        private final Map _yearSubclassRepaceCombineDatAttendSubclasscdListMap;
        private final Map<String, String> _schoolMstYearGvalCalcMap; // GVAL_CALCは0:平均値、1:単位による重み付け、2:最大値

        protected Param(final DB2UDB db2, final Map paramap, final boolean isPrintGrd) {
            _isPrintGrd = isPrintGrd;
            _prgid = (String) paramap.get("PRGID");
            _useSyojikou3 = (String) paramap.get("useSyojikou3");
            _documentroot = getParameter(paramap, "DOCUMENTROOT");
            if (!StringUtils.isEmpty(_documentroot)) {
                _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
            }
            _isSeireki = "2".equals(getString(getNameMstMap(db2, "Z012", "00"), "NAME1"));
            final List<String> schoolKinds = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT DISTINCT T1.SCHOOL_KIND FROM SCHREG_REGD_GDAT T1 INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE "), "SCHOOL_KIND");
            _dbHasSchoolKindPJH = schoolKinds.contains("H") && schoolKinds.contains("J") && schoolKinds.contains("P");

            _schoolKindStartGradeInt = Util.toInt(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME2 FROM NAME_MST T1 WHERE T1.NAMECD1 = 'A023' AND NAME1 = 'H' ")), 1);

            _tableInfo = new TableInfo(db2);
            _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "SCHOOL_KIND") ? "1" : null;

            _z010Map = getNameMstMap(db2, "Z010", "00");
            _z010Name1 = defstr((String) paramap.get("SET_Z010_NAME1"), getString(_z010Map, "NAME1"));
            _z010Name2 = defstr((String) paramap.get("SET_Z010_NAME2"), getString(_z010Map, "NAME2"));
            _z010 = Z010Info.fromName1(_z010Name1);
            log.info(" z010 name1 = " + _z010Name1 + ", z010 = " + _z010);
            _isHigashiosakaKeiai = _z010.in(Z010Info.Higashiosaka) && z010Name2Keiai.equals(_z010Name2);
            _isHigashiosakaKashiwara = _z010.in(Z010Info.Higashiosaka) && z010Name2Kashiwara.equals(_z010Name2);

            _useTotalstudySlashFlg = _z010.in(Z010Info.Miyagiken);
            _useAttendrecRemarkSlashFlg = _z010.in(Z010Info.Miyagiken);

            _e014Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'E014' AND NAME1 IS NOT NULL "), "NAME1");
            _d020Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D020' AND NAME1 IS NOT NULL "), "NAME1");

            if (_z010.in(Z010Info.Sundaikoufu)) {
                _e050List = KnjDbUtils.query(db2, "SELECT NAMESPARE2, NAMESPARE3 FROM NAME_MST WHERE NAMECD1 = 'E050' AND NAMESPARE1 = 'H' ");
            }

            _isNotCreditDefault0 = _z010.in(Z010Info.Mieken, Z010Info.Tokiwagi);
            _isPrintCompCreditWhenCreditIsZero = !_z010.in(Z010Info.Tokiwagi);
            _preferEntInfoJ = _z010.in(Z010Info.ChiyodaKudan);

            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _slashImagePath = getImageFilePath("slash.jpg");
            _knje070GradeSlashImagePath = getImageFilePath("KNJE070_GRADE_SLASH.jpg");

            _e011cd01Namespare1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '01'"));
            _e011cd02Namespare1 = ("Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '02'")))) ? CommonSqlStudyrec.DAIKEN_DIV0_SUM : CommonSqlStudyrec.DAIKEN_DIV1_DETAIL;

            final Map<String, String> e016 = getNameMstMap(db2, "E016", "01"); // 総合的な学習の時間の単位に"／"を表示するか1
            _isNotOutputSogotekinaGakushunoJikanTanni = "1".equals(getString(e016, "NAMESPARE1"));
            _isNotOutputSogotekinaGakushunoJikanTanniIfReplaceSubstitute = "1".equals(getString(e016, "NAMESPARE2"));
            _e019_01_namespare1 = getString(getNameMstMap(db2, "E019", "01"), "NAMESPARE1");
            _e028Name1 = getString(getNameMstMap(db2, "E028", "1"), "NAME1");
            _z002Abbv1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z002' "), "NAMECD2", "ABBV1");

            final String[] outputDebug = StringUtils.split(KnjDbUtils.getDbPrginfoProperties(db2, "KNJE070", "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugKinsoku = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "kinsoku");
            _isOutputDebugTime = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "time");
            _isOutputDebugSeiseki = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "seiseki");
            _isOutputDebugSubst = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "subst");
            _isOutputDebugShoken = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "shoken");
            _isOutputDebugBase = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "base");
            _isOutputDebugData = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "data");
            _isOutputDebugCalc = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "calc");
            _isOutputDebugSvfForm = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "SvfForm");
            _isOutputDebugSvfFormCreate = ArrayUtils.contains(outputDebug, "SvfFormCreate");
            _isOutputDebugSvfFormModify = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "SvfFormModify");
            for (final Map.Entry<String, String> e : getDbPrginfoProperties(db2, "KNJE070").entrySet()) {
                if (!paramap.containsKey(e.getKey())) {
                    paramap.put(e.getKey(), e.getValue());
                    if (_isOutputDebug) {
                        log.info(" #add prop " + e.getKey() + " = " + e.getValue());
                    }
                }
            }

            if (null != outputDebug) {
                _outputDebugFieldList = debugSetList("field", outputDebug);
                _outputDebugSql2List = debugSetList("sql2", outputDebug);
                _outputDebugVrsOutList = debugSetList("vrsout", outputDebug);
                _outputDebugA4PageList = debugSetList("a4page", outputDebug);
                log.info(" outputDebug = " + ArrayUtils.toString(outputDebug));
                log.info(" debug? " + _isOutputDebug);
                log.info(" debugQuery? " + _isOutputDebugQuery);
                log.info(" debugKinsoku? " + _isOutputDebugKinsoku);
                log.info(" debugTime? " + _isOutputDebugTime);
                log.info(" debugSeiseki? " + _isOutputDebugSeiseki);
                log.info(" debugFieldList? " + _outputDebugFieldList);
                log.info(" debugVrsout = " + _outputDebugVrsOutList);
                log.info(" debugA4PageList = " + _outputDebugA4PageList);
            }
            _isOutputDebugField = null != _outputDebugFieldList;

            _tyousasyoUseEditKinsoku = property(paramap, "tyousasyoUseEditKinsoku");
            _useEditKinsoku = !(_z010.in(Z010Info.Jisyuukan, Z010Info.Hosei, Z010Info.Musashi, Z010Info.Kumamoto, Z010Info.Hirokoku, Z010Info.Tottori, Z010Info.Meiji, Z010Info.Chukyo, Z010Info.Kaijyo, Z010Info.Seijyo, Z010Info.Chiben)) || "1".equals(_tyousasyoUseEditKinsoku);

            _isNotPrintClassTitle = Collections.unmodifiableMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E015' "), "NAMECD2", "NAMESPARE1"));

            _isShusyokuyouKinkiToitsu = _z010.in(Z010Info.Nishiyama);
            _isShusyokuyouKinkiToitsu2 = _z010.in(Z010Info.naraken);

            _a029NameMstMap = Collections.unmodifiableMap(KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'A029' "), "NAMECD2"));
            _d065NameMstName1Set = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'D065' "), "NAME1").keySet();
            _d001NameMstAbbv1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'D001' "), "NAMECD2", "ABBV1");

            _daitaiTextNotPrintIchibu = _z010.in(Z010Info.Kumamoto);

            for (int g = 1; g <= 12; g++) {
                final String sogakuKoteiTanni = property(paramap, "sogakuKoteiTanni_" + String.valueOf(g));
                if (NumberUtils.isNumber(sogakuKoteiTanni)) {
                    _setSogakuKoteiTanni = true;
                    _sogakuKoteiTanniMap.put(g, new BigDecimal(sogakuKoteiTanni));
                }
            }
            if (_isOutputDebug) {
                if (_setSogakuKoteiTanni) {
                    log.info(" _sogakuKoteiTanni = " + _sogakuKoteiTanniMap);
                }
            }

            if (isKindaifuzoku()) {
                final List<Map<String, String>> subclassRepaceCombineDatList = KnjDbUtils.query(db2, " SELECT * FROM SUBCLASS_REPLACE_COMBINED_DAT ");
                _yearSubclassRepaceCombineDatAttendSubclasscdListMap = new HashMap();
                for (final Map<String, String> row : subclassRepaceCombineDatList) {
                    Util.getMappedList(_yearSubclassRepaceCombineDatAttendSubclasscdListMap, getString(row, "YEAR")).add(getString(row, "ATTEND_CLASSCD") + "-" + getString(row, "ATTEND_SCHOOL_KIND") + "-" + getString(row, "ATTEND_CURRICULUM_CD") + "-" + getString(row, "ATTEND_SUBCLASSCD"));
                }
            } else {
                _yearSubclassRepaceCombineDatAttendSubclasscdListMap = Collections.EMPTY_MAP;
            }

            final StringBuffer schoolMstSql = new StringBuffer();
            schoolMstSql.append(" SELECT YEAR, GVAL_CALC FROM SCHOOL_MST ");
            if (_tableInfo._hasSCHOOL_MST_SCHOOL_KIND) {
                schoolMstSql.append(" WHERE SCHOOL_KIND = '" + SCHOOL_KIND + "' ");
            }
            _schoolMstYearGvalCalcMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, schoolMstSql.toString()), "YEAR", "GVAL_CALC");

//          log.debug(" isTsushin? = " + _isTsushin + ", name1 = " + z010Name1);
//          _useNewForm = _isTottori || _isKyoto || _isTokiwa || _isMiyagiken; // フォーム対応後に有効とする

            _f011Namespare2Max = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT MAX(NAMESPARE2) FROM NAME_MST WHERE NAMECD1 = 'F011'"));
            _f010namecd2Map = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'F010' "), "NAMECD2");
            // debug
            setClassMst(db2);
            setSubclassMst(db2);

            _fieldAreaInfo._param._setKinsoku = _useEditKinsoku;
            if (_isOutputDebugKinsoku) {
                kinsokuConfigMap().put("outputDebug", "1");
            }

            _fieldAreaInfo._param._isOutputDebugKinsoku = _isOutputDebugKinsoku;
            if (_isOutputDebug) {
                log.info(" useEditKinsoku = " + _useEditKinsoku);
                log.info(" isPrintGrd = " + _isPrintGrd);
                log.info(Util.debugMapToStr(" z010 = ", _z010Map));
            }
        }

        public void close() {
            if (_isOutputDebugTime) {
                log.info(" record query elapsed time = " + _elapsedQueryTimeList);
                log.info("              average time = " + Util.bigDecimalAvg(_elapsedQueryTimeList, 2, BigDecimal.ROUND_HALF_UP) + " [s]");
                log.info("              total time = " + Util.bigDecimalSum(_elapsedQueryTimeList) + " [s]");
                log.info(" print        elapsed time = " + _elapsedPrintTimeList);
                log.info("              average time = " + Util.bigDecimalAvg(_elapsedPrintTimeList, 2, BigDecimal.ROUND_HALF_UP) + " [s]");
                log.info("              total time = " + Util.bigDecimalSum(_elapsedPrintTimeList) + " [s]");
            }
            for (final Iterator<String> it = _psMap.keySet().iterator(); it.hasNext();) {
                final String psKey = it.next();
                DbUtils.closeQuietly(getPs(psKey));
                it.remove();
            }
            int deletedCount = 0;
            for (final File file : modifyFormPathMap().values()) {
                if (null == file) {
                    continue;
                }
                if (_isOutputDebug) {
                    log.info(" file " + file.getAbsolutePath() + " " + file.exists());
                }
                if (file.exists()) {
                    if (!_isOutputDebugSvfFormCreate && file.delete()) {
                        deletedCount += 1;
                    }
                }
            }
            if (_isOutputDebug) {
                if (deletedCount > 0) {
                    log.info(" deletedCount = " + deletedCount);
                }
            }
        }

        public void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("exception!" + psKey, e);
            }
        }

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        public String getImageFilePath(final String filename) {
            if (StringUtils.isEmpty(filename)) {
                return null;
            }
            String path = "";
            if (null != _documentroot) {
                path += _documentroot;
                if (!path.endsWith("/")) {
                    path += "/";
                }
            }
            if (null != _imagepath) {
                path += _imagepath;
                if (!path.endsWith("/")) {
                    path += "/";
                }
            }
            path += filename;
            final File file = new File(path);
            if (!file.exists()) {
                logOnce(" file " + file.getPath() +" exists? = " + file.exists());
                return null;
            }
            return file.getPath();
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

        public String property(final Map paramap, final String name, final boolean preferPropertyFile) {
            String val = null;
            if (preferPropertyFile) {
                boolean found = false;
                if (null != _prgInfoPropertiesFilePrperties) {
                    val = fileProp(name);
                    found = true;
                }
                if (paramap.containsKey(name)) {
                    if (found) {
                        if (!defstr(getParameter(paramap, name), "parameter." + name).equals(val)) {
                            if (_isOutputDebug) {
                                logOnce(" not use parameter " + name + "(" + getParameter(paramap, name) + ") but property file (" + val + ") ");
                            }
                        }
                    } else {
                        return getParameter(paramap, name);
                    }
                }
            } else {
                if (paramap.containsKey(name)) {
                    return getParameter(paramap, name);
                }
                if (null != _prgInfoPropertiesFilePrperties) {
                    val = fileProp(name);
                }
            }
            return val;
        }

        public String fileProp(final String name) {
            String val;
            val = _prgInfoPropertiesFilePrperties.getProperty(name);
            if (_isOutputDebug) {
                if (!_prgInfoPropertiesFilePrperties.containsKey(name)) {
                    logOnceWarn("property not exists in file: " + name);
                } else {
                    logOnce("property in file: " + name + " = " + val);
                }
            }
            return val;
        }

        public String property(final Map paramap, final String name) {
            return property(paramap, name, false);
        }

        public boolean isOutputDebugSql2(final String field) {
            if (null == _outputDebugSql2List) {
                return false;
            }
            if (null == field || _outputDebugSql2List.isEmpty()) {
                return true;
            }
            for (final String debugField : _outputDebugSql2List) {
                if (field.indexOf(debugField) >= 0) {
                    return true;
                }
            }
            return false;
        }

        public boolean isOutputDebugField(final String field) {
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

        public boolean isOutputDebugVrsout(final String field) {
            if (null == _outputDebugVrsOutList) {
                return false;
            }
            if (null == field || _outputDebugVrsOutList.isEmpty()) {
                return true;
            }
            for (final String debugField : _outputDebugVrsOutList) {
                if (field.indexOf(debugField) >= 0) {
                    return true;
                }
            }
            return false;
        }

        protected Map formFieldInfoMap() {
            return Util.getMappedHashMap(_sessionCache, "FORM_FIELD_INFO");
        }

        protected Map kinsokuConfigMap() {
            return Util.getMappedHashMap(_sessionCache, "KINSOKU_CONFIG_MAP");
        }

        protected Map<String, File> modifyFormPathMap() {
            return Util.getMappedMap(Util.getMappedHashMap(_sessionCache, "MODIFY_FORM_MAP"), "PATH");
        }

        protected static String getParameter(final Map paramap, final String name) {
            return (String) paramap.get(name);
        }

        private List<String> debugSetList(final String item, final String[] outputDebug) {
            List<String> setList = null;
            for (int i = 0; i < outputDebug.length; i++) {
                if (null != outputDebug[i]) {
                    if (outputDebug[i].startsWith(item)) {
                        if (null == setList) {
                            setList = new ArrayList<String>();
                        }
                        if (outputDebug[i].startsWith(item + "=")) {
                            final String[] split = StringUtils.split(outputDebug[i].substring((item + "=").length()), ",");
                            for (int j = 0; j < split.length; j++) {
                                setList.add(split[j]);
                            }
                            log.info(item + " setList = " + setList);
                        }
                    }
                }
            }
            return setList;
        }

        public static Map<String, String> getDbPrginfoProperties(final DB2UDB db2, final String programid) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = '" + programid + "' "), "NAME", "VALUE");
        }

        /**
         * 名称マスタ読み込み
         * @param db2
         * @param namecd1 名称コード1
         * @param namecd2 名称コード2
         * @return レコードのマップ
         */
        public Map<String, String> getNameMstMap(final DB2UDB db2, final String namecd1, final String namecd2) {
            final Map<String, Map<String, String>> nameMst = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' "), "NAMECD2");
            if (null == nameMst.get(namecd2)) {
                return Collections.EMPTY_MAP;
            } else {
                return nameMst.get(namecd2);
            }
        }

        /**
         * 名称マスタ読み込み
         * @param db2
         * @param map 名称マスタのマップ
         * @param key キー(名称コード2)
         * @return レコードのマップ
         */
        public static <A, B, C> Map<B, C> getMap(final Map<A, Map<B, C>> map, final A key) {
            if (null == map || null == map.get(key)) {
                return Collections.EMPTY_MAP;
            }
            return map.get(key);
        }

        /**
         * 近大付属とその他で処理が異なる場合、近大付属を判別するために用います。
         * @return KNJDefineSchool.schoolmarkが"KIN"または"KINJUNIOR"はtrueを戻します。
         */
        protected boolean isKindaifuzoku() {
            return _z010.in(Z010Info.KINDAI, Z010Info.KINJUNIOR);
        }

        // 実質近大のみ
        protected boolean notHasCertifSchoolDatOrKindai() {
            return !_tableInfo._hasCERTIF_SCHOOL_DAT || isKindaifuzoku();
        }

        protected void setClassMst(final DB2UDB db2) {
            _classMstMap.clear();
            final Integer showOrderDefault = new Integer(999);
            final String sql = " SELECT * FROM CLASS_MST ";
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                String schoolKind = null;
                if ("1".equals(_useCurriculumcd)) {
                    schoolKind = getString(row, "SCHOOL_KIND");
                }
                final String classcd = getString(row, "CLASSCD");
                final String classname = getString(row, "CLASSNAME");
                final String classordername = getString(row, "CLASSORDERNAME1");

                final String specialDiv = defstr(getString(row, "SPECIALDIV"), "0");
                Integer showorder2 = KnjDbUtils.getInt(row, "SHOWORDER2", null);
                if (null == showorder2) {
                    showorder2 = showOrderDefault;
                }
                final ClassMst cm = new ClassMst(classcd, schoolKind, classname, classordername, specialDiv, showorder2);

                _classMstMap.put(cm.getKey(this), cm);
            }
            if (_tableInfo._hasANOTHER_CLASS_MST) {
                _anotherClassMstMap.clear();
                final String sql2 = " SELECT * FROM ANOTHER_CLASS_MST ";
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql2)) {
                    String schoolKind = null;
                    if ("1".equals(_useCurriculumcd)) {
                        schoolKind = getString(row, "SCHOOL_KIND");
                    }
                    final String classcd = getString(row, "CLASSCD");
                    final String classname = getString(row, "CLASSNAME");
                    final String classordername = getString(row, "CLASSORDERNAME1");

                    final String specialDiv = defstr(getString(row, "SPECIALDIV"), "0");
                    Integer showorder2 = KnjDbUtils.getInt(row, "SHOWORDER2", null);
                    if (null == showorder2) {
                        showorder2 = showOrderDefault;
                    }
                    final AnotherClassMst cm = new AnotherClassMst(classcd, schoolKind, classname, classordername, specialDiv, showorder2);

                    _anotherClassMstMap.put(cm.getKey(this), cm);
                }
            }
        }

        protected void setSubclassMst(final DB2UDB db2) {
            _subclassMstMap.clear();
            final Integer showOrderDefault = new Integer(999);
            final String sql = " SELECT * FROM SUBCLASS_MST ";
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                String classcd = null;
                String schoolKind = null;
                String curriculumCd = null;
                if ("1".equals(_useCurriculumcd)) {
                    classcd = getString(row, "CLASSCD");
                    schoolKind = getString(row, "SCHOOL_KIND");
                    curriculumCd = getString(row, "CURRICULUM_CD");
                }
                final String subclasscd = getString(row, "SUBCLASSCD");
                final String subclasscd2 = getString(row, "SUBCLASSCD2");
                final String subclassname = getString(row, "SUBCLASSNAME");
                final String subclassordername1 = getString(row, "SUBCLASSORDERNAME1");
                Integer showorder2 = KnjDbUtils.getInt(row, "SHOWORDER2", null);
                if (null == showorder2) {
                    showorder2 = showOrderDefault;
                }
                final SubclassMst subclassMst = new SubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassordername1, showorder2, subclasscd2);

                _subclassMstMap.put(subclassMst.getKey(this), subclassMst);
            }

            if (_tableInfo._hasSUBCLASS_DETAIL_DAT) {
                final String sqlZenkiKamoku = " SELECT * FROM SUBCLASS_DETAIL_DAT WHERE SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1' ";
                for (final Map<String, String> row : KnjDbUtils.query(db2, sqlZenkiKamoku)) {
                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    if ("1".equals(_useCurriculumcd)) {
                        classcd = getString(row, "CLASSCD");
                        schoolKind = getString(row, "SCHOOL_KIND");
                        curriculumCd = getString(row, "CURRICULUM_CD");
                    }
                    final String subclasscd = getString(row, "SUBCLASSCD");
                    final String key = SubclassMst.key(this, classcd, schoolKind, curriculumCd, subclasscd);
                    final SubclassMst subclassMst = _subclassMstMap.get(key);
                    if (null == subclassMst) {
                        continue;
                    }
                    final String year = getString(row, "YEAR");
                    if (_isOutputDebugSeiseki) {
                        log.info(" zenkikamoku : " + subclassMst + " (" + year + ")");
                    }
                    subclassMst._yearZenkiKamokuFlg.put(year, "1");
                }
            }

            if (_tableInfo._hasANOTHER_SUBCLASS_MST) {
                _anotherSubclassMstMap.clear();
                final String sql2 = " SELECT * FROM ANOTHER_SUBCLASS_MST ";
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql2)) {
                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    if ("1".equals(_useCurriculumcd)) {
                        classcd = getString(row, "CLASSCD");
                        schoolKind = getString(row, "SCHOOL_KIND");
                        curriculumCd = getString(row, "CURRICULUM_CD");
                    }
                    final String subclasscd = getString(row, "SUBCLASSCD");
                    final String subclassname = getString(row, "SUBCLASSNAME");
                    final String subclassordername1 = getString(row, "SUBCLASSORDERNAME1");
                    Integer showorder2 = KnjDbUtils.getInt(row, "SHOWORDER2", null);
                    if (null == showorder2) {
                        showorder2 = showOrderDefault;
                    }
                    final AnotherSubclassMst anotherSubclassMst = new AnotherSubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassordername1, showorder2);

                    final String key = SubclassMst.key(this, anotherSubclassMst._classcd, anotherSubclassMst._schoolKind, anotherSubclassMst._curriculumCd, anotherSubclassMst._subclasscd);
                    _anotherSubclassMstMap.put(key, anotherSubclassMst);
                }
            }
        }

        protected List<String> getE014Name1List(final boolean _notUseE014) {
            if (_notUseE014) {
                return new ArrayList<String>();
            }
            return _e014Name1List;
        }

        protected List<Map<String, String>> getE048List(final DB2UDB db2, final String year) {
            final Map<String, List<Map<String, String>>> cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_E048");
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.query(db2, "SELECT NAME1, NAME2 FROM V_NAME_MST WHERE NAMECD1 = 'E048' AND YEAR = '" + year + "' "));
            }
            return Util.getMappedList(cache, year);
        }

        // D077に設定された科目コード => 総合的な学習の時間と同様単位数欄は／ (総学と同じ出力フィールドを使用する)
        protected List<String> getD077ListName2(final DB2UDB db2, final String year) {
            final String namecd1 = "D077";
            final Map cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_" + namecd1 + "_NAME2");
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND YEAR = '" + year + "' AND NAME1 IS NOT NULL AND NAME2 = '1' "), "NAME1"));
            }
            final List<String> rtn = Util.getMappedList(cache, year);
            if (_isOutputDebugSeiseki) {
                log.info(" " + namecd1 + "(NAME2) " + year + " = " + rtn);
            }
            return rtn;
        }

        // D077に設定された科目コード => 総合的な学習の時間と同様単位数欄は／
        protected List<String> getD077List(final DB2UDB db2, final String year) {
            final String namecd1 = "D077";
            final Map cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_" + namecd1);
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND YEAR = '" + year + "' AND NAME1 IS NOT NULL "), "NAME1"));
            }
            final List<String> rtn = Util.getMappedList(cache, year);
            if (_isOutputDebugSeiseki) {
                log.info(" " + namecd1 + " " + year + " = " + rtn);
            }
            return rtn;
        }

        // D081に設定された科目コード => 総合的な学習の時間と同様評定欄は／、位置は総合的な学習の時間の上
        protected List<String> getD081List(final DB2UDB db2, final String year) {
            final String namecd1 = "D081";
            final Map cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_" + namecd1);
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND YEAR = '" + year + "' AND NAME1 IS NOT NULL "), "NAME1"));
            }
            final List<String> rtn = Util.getMappedList(cache, year);
            if (_isOutputDebugSeiseki) {
                log.info(" " + namecd1 + " " + year + " = " + rtn);
            }
            return rtn;
        }

        // E065に設定された科目コード => 自立活動の科目コード 評定はなし 位置は総合的な学習の時間の下
        protected List<String> getE065List(final DB2UDB db2, final String year) {
            final String namecd1 = "E065";
            final Map cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_" + namecd1);
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND YEAR = '" + year + "' AND NAME1 IS NOT NULL "), "NAME1"));
            }
            final List<String> rtn = Util.getMappedList(cache, year);
            if (_isOutputDebugSeiseki) {
                log.info(" " + namecd1 + " " + year + " = " + rtn);
            }
            return rtn;
        }

        // D015に設定された名称予備1 調査書のに評定1を含めるか 1:含める それ以外:含めない (ちなみに母集団が評定1のみの場合は評定1を含める)
        protected String getD015Namespare1(final DB2UDB db2, final String year) {
            final String namecd1 = "D015";
            final Map<String, String> cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_" + namecd1);
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND YEAR = '" + year + "' ORDER BY NAMECD2 ")));
            }
            final String rtn = cache.get(year);
            if (_isOutputDebug) {
                log.info(" " + namecd1 + " " + year + " = " + rtn);
            }
            return rtn;
        }

        // G009に設定された名称1 調査書卒業見込年月（指定なしは3）
        protected String getG009Name1(final DB2UDB db2, final String year) {
            final String namecd1 = "G009";
            final Map<String, String> cache = Util.getMappedHashMap(_sessionCache, "V_NAME_MST_MAP_" + namecd1);
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '00' AND YEAR = '" + year + "' ORDER BY NAMECD2 ")));
            }
            final String rtn = cache.get(year);
            if (_isOutputDebug) {
                log.info(" " + namecd1 + " " + year + " = " + rtn);
            }
            return rtn;
        }

        protected List<String> getSubclassD006(final DB2UDB db2, final String year) {
            final String key = "SUBCLASS_DETAIL_DAT006";
            final Map<String, List<String>> cache = Util.getMappedHashMap(_sessionCache, key);
            if (null == cache.get(year)) {
                cache.put(year, KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT WHERE YEAR = '" + year + "' AND SUBCLASS_SEQ = '006' "), "SUBCLASSCD"));
            }
            return cache.get(year);
        }

        protected List<Map<String, String>> getL007List(final DB2UDB db2) {
            if (null == _l007List) {
                _l007List = KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' ORDER BY NAMECD2 ");
            }
            return _l007List;
        }

        protected Map<String, String> getSchoolMst(final DB2UDB db2, final String year) {
            final Map<String, Map<String, String>> cache = Util.getMappedHashMap(_sessionCache, "SCHOOL_MST_MAP");
            if (null == cache.get(year)) {
                String sql = "SELECT * FROM SCHOOL_MST WHERE YEAR = '" + year + "' ";
                if (_tableInfo._hasSCHOOL_MST_SCHOOL_KIND) {
                    sql += " AND SCHOOL_KIND = '" + SCHOOL_KIND + "' ";
                }
                cache.put(year, KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql)));
            }
            return Util.getMappedMap(cache, year);
        }

        // 単位マスタ
        protected String getCreditMstCredits(final DB2UDB db2, final SubclassMst sm, final String year, final String grade, final String coursecd, final String majorcd, final String coursecode) {
            final Map<String, String> cache = Util.getMappedHashMap(_sessionCache, "CREDIT_MST_CREDIT_" + sm.getKey(this));
            if (cache.isEmpty()) {
                final List sql = new ArrayList();
                sql.add(" SELECT ");
                sql.add("     T1.YEAR || T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS YGCM_KEY ");
                sql.add("   , T1.CREDITS ");
                sql.add(" FROM CREDIT_MST T1 ");
                sql.add(" WHERE ");
                if ("1".equals(_useCurriculumcd)) {
                    sql.add(" CLASSCD = '" + sm._classcd + "' AND SCHOOL_KIND = '" + sm._schoolKind + "' AND CURRICULUM_CD = '" + sm._curriculumCd + "' AND SUBCLASSCD = '" + sm._subclasscd + "' ");
                } else {
                    sql.add(" SUBCLASSCD = '" + sm._subclasscd + "' ");
                }
                cache.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, Util.mkString(sql, "")), "YGCM_KEY", "CREDITS"));
            }
            return cache.get(year + grade + coursecd + majorcd + coursecode);
        }

        // 科目年度データ
        protected boolean isSubclassInYear(final DB2UDB db2, final String year, final SubclassMst sm) {
            final Map<String, List<String>> cache = Util.getMappedHashMap(_sessionCache, "SUBCLASS_YEAR_" + sm.getKey(this));
            if (null == cache.get(year)) {
                final List sql = new ArrayList();
                sql.add(" SELECT ");
                sql.add("     T1.YEAR ");
                sql.add(" FROM V_SUBCLASS_MST T1 ");
                sql.add(" WHERE ");
                if ("1".equals(_useCurriculumcd)) {
                    sql.add(" CLASSCD = '" + sm._classcd + "' AND SCHOOL_KIND = '" + sm._schoolKind + "' AND CURRICULUM_CD = '" + sm._curriculumCd + "' AND SUBCLASSCD = '" + sm._subclasscd + "' ");
                } else {
                    sql.add(" SUBCLASSCD = '" + sm._subclasscd + "' ");
                }
                cache.put(year, KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, Util.mkString(sql, "")), "YEAR"));
            }
            return cache.get(year).contains(year);
        }

        // 代替マスタ
        protected List<Map<String, String>> getSubStMst(final DB2UDB db2, final String year, final String grade, final String coursecd, final String majorcd, final String coursecode) {
            final String ygcmKey = "Y" + year + "|G" + grade + "|C" + coursecd + "|M" + majorcd + "|CC" + coursecode;
            final Map<String, List<Map<String, String>>> cache = Util.getMappedHashMap(_sessionCache, "SUBST_MST_");
            if (null == cache.get(ygcmKey)) {

                final String substSql2Key = "SUBST_SQL2_KEY";
                if (null == getPs(substSql2Key)) {
                    final List substSql = new ArrayList();
                    substSql.add(" SELECT ");
                    substSql.add("     T4.SUBSTITUTION_TYPE_FLG ");
                    substSql.add("   , T5.* ");
                    substSql.add(" FROM SUBCLASS_REPLACE_SUBSTITUTION_DAT T4 ");
                    substSql.add(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T5 ON T5.YEAR = T4.YEAR ");
                    if ("1".equals(_useCurriculumcd)) {
                        substSql.add("        AND T5.SUBSTITUTION_CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                        substSql.add("        AND T5.SUBSTITUTION_SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
                        substSql.add("        AND T5.SUBSTITUTION_CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
                    }
                    substSql.add("        AND T5.SUBSTITUTION_SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
                    if ("1".equals(_useCurriculumcd)) {
                        substSql.add("        AND T5.ATTEND_CLASSCD = T4.ATTEND_CLASSCD ");
                        substSql.add("        AND T5.ATTEND_SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                        substSql.add("        AND T5.ATTEND_CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
                    }
                    substSql.add("        AND T5.ATTEND_SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
                    substSql.add(" WHERE (T4.YEAR, T5.GRADE, T5.COURSECD, T5.MAJORCD, T5.COURSECODE) = (?, ?, ?, ?, ?) ");
                    substSql.add(" ORDER BY ");
                    substSql.add("     T4.SUBSTITUTION_TYPE_FLG ");
                    setPs(db2, substSql2Key, Util.mkString(substSql, ""));
                }
                cache.put(ygcmKey, KnjDbUtils.query(db2, getPs(substSql2Key), new Object[] {year, grade, coursecd, majorcd, coursecode}));
            }
            final List<Map<String, String>> rtn = cache.get(ygcmKey);
            if (_isOutputDebugSubst) {
                Util.debugCollectionToStr(ygcmKey, rtn, ",");
            }
            return rtn;
        }


        /**
         * 普通/専門教育に関する教科のタイトルを表示するか
         * @param certifKindCd 証明書種別コード
         * @return 普通/専門教育に関する教科のタイトルを表示するか
         */
        protected boolean isPrintClassTitle(final String certifKindCd, final CommonPrintData printData) {
            return !"1".equals(_isNotPrintClassTitle.get(certifKindCd)) && !(printData._output == CommonPrintData._shusyokuYou && _isShusyokuyouKinkiToitsu2);
        }

        /**
         * 普通・専門の文言
         * @param div 普通・専門区分　0:普通、1:専門、2:その他
         * @return 文言
         */
        public String getSpecialDivName(final boolean isNewForm, final String div) {
            final String defaultname;
            final String namecd2;
            if ("1".equals(div)) {
                //　専門教科
                namecd2 = "2";
                defaultname = isNewForm ? "主として専門学科において開設される各教科・科目" : "専門教育に関する教科・科目";  // 46桁 or 26桁
            } else if ("2".equals(div)) {
                // その他
                namecd2 = "3";
                defaultname = "その他特に必要な教科・科目";
            } else { // if (null == div || "0".equals(div)) {
                // 普通教育
                namecd2 = "1";
                defaultname = isNewForm ? "各学科に共通する各教科・科目" : "普通教育に関する教科・科目"; // 28桁 or 26桁
            }
            final Map<String, String> nameMstRec = getMap(_a029NameMstMap, namecd2);
            return "【" + defstr(getString(nameMstRec, "NAME1"), defaultname) + "】";
        }

        public static boolean isNewForm(final Param param, final CommonPrintData printData) {
            if ("1".equals(printData._tyousasyo2020)) {
                return true;
            }
            final int checkYear = 2013; // 切替年度
            boolean rtn = false;
            if (null != printData && printData._useNewForm) {
                if (NumberUtils.isDigits(printData._personInfo._curriculumYear)) {
                    // 教育課程年度が入力されている場合
                    if (checkYear > Integer.parseInt(printData._personInfo._curriculumYear)) {
                        rtn = false;
                    } else {
                        rtn = true;
                    }
                } else if (null != Util.enterYear(printData._personInfo._entDate)) {
                    final int iEntYear = Util.enterYear(printData._personInfo._entDate).intValue();
                    if (checkYear > iEntYear) {
                        rtn = false;
                    } else if (checkYear <= iEntYear) {
                        if (NumberUtils.isDigits(printData._personInfo._entYearGradeCd)) {
                            final int iAnnual = Integer.parseInt(printData._personInfo._entYearGradeCd);
                            if ((checkYear + 0) == iEntYear && iAnnual >= 2 ||
                                (checkYear + 1) == iEntYear && iAnnual >= 3 ||
                                (checkYear + 2) == iEntYear && iAnnual >= 4) { // 転入生を考慮
                                rtn = false;
                            } else {
                                rtn = true;
                            }
                        } else {
                            rtn = true;
                        }
                    }
                }
            }
            return rtn;
        }

        public boolean isSameClasscd(final String classcd, final String schoolKind, final String s_classcd, final String s_schoolKind) {
            if ("1".equals(_useCurriculumcd)) {
                return classcd.equals(s_classcd) && schoolKind.equals(s_schoolKind);
            }
            return classcd.equals(s_classcd);
        }

        public void logOnce(final String str) {
            if (!_logOnce.contains(str)) {
                log.info(str);
                _logOnce.add(str);
            }
        }

        public void logOnceWarn(final String str) {
            if (!_logOnceWarn.contains(str)) {
                log.warn(str);
                _logOnceWarn.add(str);
            }
        }
    }
}
