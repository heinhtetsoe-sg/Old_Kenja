// kanji=漢字
/*
 * $Id: KenjaDateImpl.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2009/03/18 11:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//TODO: リリース時は以下のライブラリをjarインポートに!!
import org.apache.commons.lang.time.DateUtils;


/**
 * 賢者パッケージの日付の実装。
 * @version $Id: KenjaDateImpl.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class KenjaDateImpl implements Comparable {
    private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy/MM/dd z");

    static {
        SDF_YMD.setLenient(false);
    }

    private static final Map INSTANCES = new HashMap();

    protected final Calendar    _cal;

    protected final long        _millis;
    protected final long        _elapseDays;
    protected final int         _y;
    protected final int         _m;
    protected final int         _d;

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
        _elapseDays = (long) Math.floor(_millis / DateUtils.MILLIS_IN_DAY);
        _cal.setTime(new Date(_millis));

        _y = _cal.get(Calendar.YEAR);
        _m = _cal.get(Calendar.MONTH) + 1;
        _d = _cal.get(Calendar.DATE);

        _cal.clear();
        _cal.set(_y, _m - 1, _d, 0, 0, 0);

        _sqlDate = new java.sql.Date(_cal.getTime().getTime());

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
            _str = sb.toString();
        }

        _hash = _str.hashCode();
    }

    /*
     */
    private static long round(final long timeInMillis) {
        return ((long) Math.floor(timeInMillis / DateUtils.MILLIS_IN_DAY)) * DateUtils.MILLIS_IN_DAY;
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

    private static long dateTime(final Date date) {
        if (null == date) {
            throw new IllegalArgumentException("引数が不正");
        }
        if (date instanceof java.sql.Date) {
            return date.getTime() + 9L * DateUtils.MILLIS_IN_HOUR;
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
        if (!(o instanceof KenjaDateImpl)) {
            return -1;
        }

        final KenjaDateImpl that = (KenjaDateImpl) o;

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
        return getInstance(this.getTimeInMillis() + days * DateUtils.MILLIS_IN_DAY);
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
