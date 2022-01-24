// kanji=漢字
/*
 * $Id: DayOfWeek.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/18 11:05:23 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.lang.enums.ValuedEnum;

/**
 * 曜日。
 * @author tamura
 * @version $Id: DayOfWeek.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DayOfWeek extends ValuedEnum implements KenjaDate {
    /** 一週間の日数。つまり7。 */
    public static final int DAYS_OF_A_WEEK = 7;

    /** 月曜日 */
    public static final DayOfWeek MONDAY    = new DayOfWeek("月曜日", 2, 0); // (name, value, index)
    /** 火曜日 */
    public static final DayOfWeek TUESDAY   = new DayOfWeek("火曜日", 3, 1);
    /** 水曜日 */
    public static final DayOfWeek WEDNESDAY = new DayOfWeek("水曜日", 4, 2);
    /** 木曜日 */
    public static final DayOfWeek THURSDAY  = new DayOfWeek("木曜日", 5, 3);
    /** 金曜日 */
    public static final DayOfWeek FRIDAY    = new DayOfWeek("金曜日", 6, 4);
    /** 土曜日 */
    public static final DayOfWeek SATURDAY  = new DayOfWeek("土曜日", 7, 5);
    /** 日曜日 */
    public static final DayOfWeek SUNDAY    = new DayOfWeek("日曜日", 1, 6);

    private static final Class<DayOfWeek> MYCLASS = DayOfWeek.class;
    private static final KenjaDate[] SEVEN_DAYS = {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};

    private static final Map<String, DayOfWeek> ENGLISH_NAME_MAP = new HashMap<String, DayOfWeek>();

    static {
        ENGLISH_NAME_MAP.put("MON", MONDAY);
        ENGLISH_NAME_MAP.put("TUE", TUESDAY);
        ENGLISH_NAME_MAP.put("WED", WEDNESDAY);
        ENGLISH_NAME_MAP.put("THU", THURSDAY);
        ENGLISH_NAME_MAP.put("FRI", FRIDAY);
        ENGLISH_NAME_MAP.put("SAT", SATURDAY);
        ENGLISH_NAME_MAP.put("SUN", SUNDAY);
    }

    /** 表示の順序 */
    private final int _index;

    private final String _shortName;

    /*
     * コンストラクタ。
     * @param name 曜日
     * @param value 値
     * @param index 表示の順序
     */
    private DayOfWeek(final String name, final int value, final int index) {
        super(name, value);
        _index = index;
        _shortName = name.substring(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    public DayOfWeek getDayOfWeek() { return this; }

    /**
     * 表示の順序を得る。
     * @return 表示の順序
     * @see jp.co.alp.kenja.common.domain.KenjaDate#getIndex()
     */
    public int getIndex() { return _index; }

    /**
     * 短い名前を得る。
     * @return 短い名前
     */
    public String getShortName() { return _shortName; }

    /**
     * {@inheritDoc}
     */
    public KenjaDate[] getSevenDays() {
        return SEVEN_DAYS;
    }

    /**
     * 曜日コードから、曜日を得る。
     * @param daycd 曜日コード(1=日曜日,2=月曜日,...7=土曜日)
     * @return 曜日
     */
    public static DayOfWeek getInstance(final int daycd) {
        return (DayOfWeek) EnumUtils.getEnum(MYCLASS, daycd);
    }

    /**
     * CalendarのDAY_OF_WEEKの値から、曜日を得る。
     * @param cal Calendarのインスタンス
     * @return 曜日
     */
    public static DayOfWeek getInstance(final Calendar cal) {
        return getInstanceByCalendar(cal.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * 文字列から、曜日を得る。
     * @param str 文字列。曜日コード(1=日曜日,2=月曜日,...7=土曜日)または英字表記。
     * @return 曜日
     */
    public static DayOfWeek getInstance(final String str) {
        final String upper = str.toUpperCase();
        if (ENGLISH_NAME_MAP.containsKey(upper)) {
            return (DayOfWeek) ENGLISH_NAME_MAP.get(upper);
        }

        try {
            final int daycd = Integer.parseInt(str);
            return (DayOfWeek) EnumUtils.getEnum(MYCLASS, daycd);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /*
     *
     */
    private static DayOfWeek getInstanceByCalendar(final int dayOfWeek) {
        final DayOfWeek dow;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dow = MONDAY;
                break;
            case Calendar.TUESDAY:
                dow = TUESDAY;
                break;
            case Calendar.WEDNESDAY:
                dow = WEDNESDAY;
                break;
            case Calendar.THURSDAY:
                dow = THURSDAY;
                break;
            case Calendar.FRIDAY:
                dow = FRIDAY;
                break;
            case Calendar.SATURDAY:
                dow = SATURDAY;
                break;
            case Calendar.SUNDAY:
                dow = SUNDAY;
                break;
            default:
                throw new IllegalArgumentException("曜日が得られない:" + dayOfWeek);
        }
        return dow;
    }
} // DayOfWeek

// eof
