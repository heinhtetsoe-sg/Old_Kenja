// kanji=漢字
/*
 * $Id: KenjaDateImpl.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2004/05/18 10:48:41 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.lang.time.DateUtils;


/**
 * 賢者パッケージの日付の実装。
 * @author tamura
 * @version $Id: KenjaDateImpl.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public final class KenjaDateImpl implements KenjaDate, Comparable<Object> {
    private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy/MM/dd z");

    static {
        SDF_YMD.setLenient(false);
    }

    private static final Map<String, KenjaDateImpl> INSTANCES = new HashMap<String, KenjaDateImpl>();

    protected final Calendar    _cal;

    protected final long        _millis;
    protected final long        _elapseDays;
    protected final int         _y;
    protected final int         _m;
    protected final int         _d;
    protected final int         _nendo;

    protected final DayOfWeek   _dow;
    protected final String      _str;   //例："2004-12-31(金)"
    private final int           _hash;
    private final java.sql.Date _sqlDate;

    /*
     * コンストラクタ。
     * @param timeInMillis 経過ミリ秒
     */
    private KenjaDateImpl(final long timeInMillis) {
        _cal = calendar();
        _millis = round(timeInMillis);
        _elapseDays = (long) Math.floor(_millis / DateUtils.MILLIS_PER_DAY);
        _cal.setTime(new Date(_millis));

        _y = _cal.get(Calendar.YEAR);
        _m = _cal.get(Calendar.MONTH) + 1;
        _d = _cal.get(Calendar.DATE);
        _nendo = _m <= 3 ? _y - 1 : _y;

        _cal.clear();
        _cal.set(_y, _m - 1, _d, 0, 0, 0);

        _sqlDate = new java.sql.Date(_cal.getTime().getTime());

        _dow = DayOfWeek.getInstance(_cal);
    string:
        {
            final StringBuffer sb = new StringBuffer(16);
            sb.append(_y).append('-');
            if (_m < 10) {
                sb.append('0');
            }
            sb.append(_m).append('-');
            if (_d < 10) {
                sb.append('0');
            }
            sb.append(_d);
            sb.append('(').append(_dow.getShortName()).append(')');
            _str = sb.toString();
        }

        _hash = _str.hashCode();
    }

    /*
     */
    private static long round(final long timeInMillis) {
        return ((long) Math.floor(timeInMillis / DateUtils.MILLIS_PER_DAY)) * DateUtils.MILLIS_PER_DAY;
    }

    /**
     * 日付のインスタンスを得る。
     * @param timeInMillis 経過ミリ秒
     * @return 日付のインスタンス
     */
    public static KenjaDateImpl getInstance(final long timeInMillis) {
        final long millis = round(timeInMillis);
        final String key = String.valueOf(millis);
        KenjaDateImpl found = (KenjaDateImpl) INSTANCES.get(key);
        if (null == found) {
            found = new KenjaDateImpl(millis);
            INSTANCES.put(key, found);
        }
        return found;
    }

    /**
     * 日付のインスタンスを得る。
     * @param cal カレンダー
     * @return 日付のインスタンス
     */
    public static KenjaDateImpl getInstance(final Calendar cal) {
        return getInstance(cal.get(Calendar.YEAR), 1 + cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 日付のインスタンスを得る。
     * @param year 西暦年
     * @param month 月(1...12)
     * @param dayOfMonth 月の日(1...28,29,30,31)
     * @return 日付のインスタンス
     */
    public static KenjaDateImpl getInstance(
            final int year,
            final int month,
            final int dayOfMonth
    ) {
        final Calendar cal = calendar(year, month, dayOfMonth);
        return getInstance(
                cal.getTime().getTime()
        );
    }

    /*
     * Calendarのインスタンスを得る。
     * @return Calendarのインスタンス
     */
    private static Calendar calendar() {
        final Calendar cal = Calendar.getInstance(DateUtils.UTC_TIME_ZONE);
        cal.setFirstDayOfWeek(Calendar.MONDAY); // 週の始まりは「月曜日」
        cal.clear();
        cal.setLenient(false);
        return cal;
    }

    /**
     * 年月日からCalendarのインスタンスを得る。
     * @param y 西暦年
     * @param m 月(1...12)
     * @param d 月の日(1...28,29,30,31)
     * @return Calendarのインスタンス
     */
    public static Calendar calendar(
            final int y,
            final int m,
            final int d
    ) {
        final Calendar cal = calendar();
        cal.set(y, m - 1, d, 0, 0, 0);
        return cal;
    }

    /**
     * 日付のインスタンスを得る。
     * @param date 日付。java.util.Dateまたはjava.sql.Dateのどちらでも。
     * @return 日付のインスタンス
     */
    public static KenjaDateImpl getInstance(final Date date) {
        return getInstance(dateTime(date));
    }

    /**
     * 文字列から、日付のインスタンスを得る。
     * @param str 日付を表す文字列。例 "2005-02-17" または "2005/02/17"
     * @return 日付のインスタンス
     * @throws ParseException パース失敗
     */
    public static KenjaDateImpl getInstance(final String str) throws ParseException {
        final String str0 = str.replace('-', '/');
        final Date d;
        synchronized (SDF_YMD) {
            // Java5.0(1.5.0_02-b09)のapplet Plug-inでは、"GMT" だとParseExceptionが発生する。"UTC" だと発生しない。
//            d = SDF_YMD.parse(str0 + " GMT");
            d = SDF_YMD.parse(str0 + " UTC");
        }
        return KenjaDateImpl.getInstance(d);
    }

    private static long dateTime(final Date date) {
        if (null == date) {
            throw new IllegalArgumentException("引数が不正");
        }
        if (date instanceof java.sql.Date) {
            return date.getTime() + 9L * DateUtils.MILLIS_PER_HOUR;
        } else {
            return date.getTime();
        }
    }

    /**
     * 文字列に変換する。
     * @return 文字列
     */
    public String toString() { return _str; }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof KenjaDateImpl)) { return false; }

        final KenjaDateImpl that = (KenjaDateImpl) obj;
        return _cal.equals(that._cal);
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (o instanceof KenjaDateImpl) {
            return compareTo((KenjaDateImpl) o);
        }
        if (o instanceof Calendar) {
            return compareTo((Calendar) o);
        }

        return -1;
    }

    private int compareTo(final KenjaDateImpl that) {
        int rtn = 0;
    check:
        {
            rtn = _y - that._y;
            if (0 != rtn) { break check; }

            rtn = _m - that._m;
            if (0 != rtn) { break check; }

            rtn = _d - that._d;
            if (0 != rtn) { break check; }
        } // check:

        return rtn;
    }

    private int compareTo(final Calendar that) {
        int rtn = 0;
    check:
        {
            rtn = _y - that.get(Calendar.YEAR);
            if (0 != rtn) { break check; }

            rtn = _m - (that.get(Calendar.MONTH) + 1);
            if (0 != rtn) { break check; }

            rtn = _d - that.get(Calendar.DAY_OF_MONTH);
            if (0 != rtn) { break check; }
        } // check:

        return rtn;
    }

    /**
     * この日付のハッシュコードを返します。
     * {@inheritDoc}
     */
    public int hashCode() {
        return _hash;
    }

    /**
     * 西暦年を得る。
     * @return 西暦年
     */
    public int getYear() { return _y; }

    /**
     * 月を得る。
     * @return 月(1...12)
     */
    public int getMonth() { return _m; }

    /**
     * 日を得る。
     * @return 日(1...28,29,30,31)
     */
    public int getDay() { return _d; }

    /**
     * 西暦年度を得る。
     * @return 西暦年度
     */
    public int getNendo() { return _nendo; }

    /**
     * SQL-Dateを得る。
     * @return SQLDateのインスタンス
     */
    public java.sql.Date getSQLDate() {
        return _sqlDate;
    }

    /**
     * 1970 年 1 月 1 日 00:00:00 GMT からのミリ秒数を得る。
     * @return 1970 年 1 月 1 日 00:00:00 GMT からのミリ秒数
     * @see java.util.Calendar#getTimeInMillis()
     */
    public long getTimeInMillis() { return _millis; }

    /**
     * 曜日を得る。
     * {@inheritDoc}
     */
    public DayOfWeek getDayOfWeek() { return _dow; }

    /**
     * 曜日の表示順を得る。
     * {@inheritDoc}
     */
    public int getIndex() { return _dow.getIndex(); }

    /**
     * 翌日を表す新しいインスタンスを得る。
     * @return 翌日
     */
    public KenjaDateImpl nextDate() {
        return add(1);
    }

    /**
     * 現在の日付に <code>days</code>日を加えた日付を表すインスタンスを返す。
     * 尚、<code>days</code> がゼロなら、このインスタンス自身を返す。
     * <code>days</code>が負なら、日を減じた日付を表すインスタンスを返す。
     * @param days 加える日数（負の場合は減ずる日数）
     * @return 現在の日付に <code>days</code>日を加えた日付を表すインスタンス
     */
    public KenjaDateImpl add(final int days) {
        if (0 == days) {
            return this;
        }
        return getInstance(this.getTimeInMillis() + days * DateUtils.MILLIS_PER_DAY);
    }

    /**
     * この日を含む一週間分（月〜土、日）の日付を配列で得る。
     * @return 一週間分（月〜土、日）の日付の配列
     * @see jp.co.alp.kenja.common.domain.KenjaDate#getSevenDays()
     */
    public KenjaDate[] getSevenDays() {
        final KenjaDateImpl[] rtn = new KenjaDateImpl[DayOfWeek.DAYS_OF_A_WEEK];

        final int today = getIndex();

        // 当日より前（当日含まず）
        for (int i = 0; i < today; i++) {
            rtn[i] = add(i - today);
        }

        // 当日
        rtn[today] = this;

        // 当日より後（当日含まず）
        for (int i = today + 1; i < rtn.length; i++) {
            rtn[i] = add(i - today);
        }

        return rtn;
    }

    /**
     * 1970年1月1日の週を1として何週目かを得る。
     * 週は月曜日で始まる。
     * 例：1970-1-1(木)から1970-1-4(日)は1を返す。
     * 例：1970-1-5(月)から1970-1-11(日)は2を返す。
     * <pre>
     * $ LANG=C /usr/bin/cal 1 1970
     *
     *     January 1970
     * Su Mo Tu We Th Fr Sa
     *  -  -  -  -  1  2  3
     *  4  5  6  7  8  9 10
     * 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24
     * 25 26 27 28 29 30 31
     * </pre>
     * @return 週
     */
    public int getElapseWeeks() {
        return (int) Math.floor((_elapseDays + 1 + 9) / 7);
    }

    /**
     * 1970年1月1日からの経過日数を得る。
     * @return 経過日数
     */
    public long getElapseDays() {
        return _elapseDays;
    }

    /**
     * 開始日を含む週から、終了日を含む週の一週間分（月〜土、日）の日付の配列のリストを得る。
     * 開始日が月曜日以外の場合、開始日以前の日付も含まれる。
     * @param start 開始日
     * @param end 終了日
     * @return リスト
     * @throws IllegalArgumentException 開始日が終了日より未来の場合
     */
    public static List<KenjaDate[]> getSevenDaysList(
            final KenjaDateImpl start,
            final KenjaDateImpl end
    ) {
        if (end.compareTo(start) <  0) {
            throw new IllegalArgumentException();
        }

        final List<KenjaDate[]> rtn = new LinkedList<KenjaDate[]>();

        KenjaDateImpl d = start;
        while (d.compareTo(end) <= 0) {
            final KenjaDate[] array = d.getSevenDays();
            rtn.add(array);
            //
            d = ((KenjaDateImpl) array[array.length - 1]).add(1);
        }

        return rtn;
    }

    /**
     * 開始日、終了日の範囲内にあるであろう一週間分の日付の配列から、その週の期間を得る。
     * @param start 開始日
     * @param end 終了日
     * @param week 一週間分の日付
     * @return 期間の日付。要素数2の配列。
     */
    public static KenjaDateImpl[] getTerm(
            final KenjaDateImpl start,
            final KenjaDateImpl end,
            final KenjaDateImpl[] week
    ) {
        if (end.compareTo(start) <  0) {
            throw new IllegalArgumentException();
        }

        final KenjaDateImpl[] rtn = new KenjaDateImpl[2];
        final KenjaDateImpl mon = week[0];
        if (start.compareTo(mon) < 0) {
            rtn[0] = mon;
        } else {
            rtn[0] = start;
        }

        final KenjaDateImpl sun = week[week.length - 1];
        if (sun.compareTo(end) < 0) {
            rtn[1] = sun;
        } else {
            rtn[1] = end;
        }

        return rtn;
    }

    /**
     * 同じか直近過去の日付を、コレクションから探す。
     * コレクションの最初の要素よりも<code>this</code>の方が過去なら、最初の要素を返す。
     * @param coll コレクション
     * @return 見つけた日付
     */
    public KenjaDateImpl findNearestPast(final Collection<KenjaDateImpl> coll) {
        if (null == coll)   { throw new IllegalArgumentException("collがnull"); }
        if (coll.isEmpty()) { throw new IllegalArgumentException("collが空"); }

        if (coll.contains(this)) {
            return this;
        }

        if (coll instanceof SortedSet) {
            return findNearestPast0(coll);
        } else {
            final List<KenjaDateImpl> list = new ArrayList<KenjaDateImpl>(coll);
            Collections.sort(list);
            return findNearestPast0(list);
        }
    }

    private KenjaDateImpl findNearestPast0(final Collection<KenjaDateImpl> coll) {
        final Iterator<KenjaDateImpl> it = coll.iterator();
        KenjaDateImpl prev = it.next();
        while (it.hasNext()) {
            final KenjaDateImpl next = it.next();
            if (this.compareTo(next) < 0) {
                return prev;
            }
            prev = next;
        }
        return prev;
    }

    /**
     * 大きい(未来)方の日付を返す。
     * @param that もう一つの日付
     * @return 大きい(未来)方の日付
     */
    public KenjaDateImpl max(final KenjaDateImpl that) {
        if (null == that) {
            return this;
        }
        if (this.compareTo(that) < 0) {
            return that;
        } else {
            return this;
        }
    }
} // KenjaDateImpl

// eof
