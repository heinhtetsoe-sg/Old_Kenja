// kanji=漢字
/*
 * $Id: DayOfWeekTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/05/18 13:05:14 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.enums.EnumUtils;

import jp.co.alp.kenja.common.domain.DayOfWeek;
import jp.co.alp.kenja.common.domain.KenjaDate;

import junit.framework.TestCase;

public class DayOfWeekTest extends TestCase {

    public DayOfWeekTest(String name) {
        super(name);
    }

    public void testGetDayOfWeek() {
        assertSame(DayOfWeek.MONDAY,    DayOfWeek.MONDAY.getDayOfWeek());
        assertSame(DayOfWeek.TUESDAY,   DayOfWeek.TUESDAY.getDayOfWeek());
        assertSame(DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY.getDayOfWeek());
        assertSame(DayOfWeek.THURSDAY,  DayOfWeek.THURSDAY.getDayOfWeek());
        assertSame(DayOfWeek.FRIDAY,    DayOfWeek.FRIDAY.getDayOfWeek());
        assertSame(DayOfWeek.SATURDAY,  DayOfWeek.SATURDAY.getDayOfWeek());
        assertSame(DayOfWeek.SUNDAY,    DayOfWeek.SUNDAY.getDayOfWeek());
    }

    public void testGetIndex() {
        assertEquals(0, DayOfWeek.MONDAY.getIndex());
        assertEquals(1, DayOfWeek.TUESDAY.getIndex());
        assertEquals(2, DayOfWeek.WEDNESDAY.getIndex());
        assertEquals(3, DayOfWeek.THURSDAY.getIndex());
        assertEquals(4, DayOfWeek.FRIDAY.getIndex());
        assertEquals(5, DayOfWeek.SATURDAY.getIndex());
        assertEquals(6, DayOfWeek.SUNDAY.getIndex());
    }

    public void testGetShortName() {
        assertEquals("月", DayOfWeek.MONDAY.getShortName());
        assertEquals("火", DayOfWeek.TUESDAY.getShortName());
        assertEquals("水", DayOfWeek.WEDNESDAY.getShortName());
        assertEquals("木", DayOfWeek.THURSDAY.getShortName());
        assertEquals("金", DayOfWeek.FRIDAY.getShortName());
        assertEquals("土", DayOfWeek.SATURDAY.getShortName());
        assertEquals("日", DayOfWeek.SUNDAY.getShortName());
    }

    public void testGetSevenDays() {
        final KenjaDate[] sevenDays = DayOfWeek.MONDAY.getSevenDays();
        assertNotNull(sevenDays);
        assertEquals(7, sevenDays.length);
    }

    public void testGetInstance() {
        assertSame(DayOfWeek.MONDAY,    DayOfWeek.getInstance(2));
        assertSame(DayOfWeek.TUESDAY,   DayOfWeek.getInstance(3));
        assertSame(DayOfWeek.WEDNESDAY, DayOfWeek.getInstance(4));
        assertSame(DayOfWeek.THURSDAY,  DayOfWeek.getInstance(5));
        assertSame(DayOfWeek.FRIDAY,    DayOfWeek.getInstance(6));
        assertSame(DayOfWeek.SATURDAY,  DayOfWeek.getInstance(7));
        assertSame(DayOfWeek.SUNDAY,    DayOfWeek.getInstance(1));
    }

    public void testToString() {
        assertEquals("月曜日", DayOfWeek.MONDAY.toString());
        assertEquals("火曜日", DayOfWeek.TUESDAY.toString());
        assertEquals("水曜日", DayOfWeek.WEDNESDAY.toString());
        assertEquals("木曜日", DayOfWeek.THURSDAY.toString());
        assertEquals("金曜日", DayOfWeek.FRIDAY.toString());
        assertEquals("土曜日", DayOfWeek.SATURDAY.toString());
        assertEquals("日曜日", DayOfWeek.SUNDAY.toString());
    }

    public void testGetValue() {
        assertEquals(2, DayOfWeek.MONDAY.getValue());
        assertEquals(3, DayOfWeek.TUESDAY.getValue());
        assertEquals(4, DayOfWeek.WEDNESDAY.getValue());
        assertEquals(5, DayOfWeek.THURSDAY.getValue());
        assertEquals(6, DayOfWeek.FRIDAY.getValue());
        assertEquals(7, DayOfWeek.SATURDAY.getValue());
        assertEquals(1, DayOfWeek.SUNDAY.getValue());
    }

    public void testEquals() {
        assertEquals(DayOfWeek.MONDAY,      DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.TUESDAY,     DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.WEDNESDAY,   DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.THURSDAY,    DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.FRIDAY,      DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.SATURDAY,    DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.SUNDAY,      DayOfWeek.SUNDAY);
    }

    public void testSame() {
        assertSame(DayOfWeek.MONDAY,      DayOfWeek.MONDAY);
        assertSame(DayOfWeek.TUESDAY,     DayOfWeek.TUESDAY);
        assertSame(DayOfWeek.WEDNESDAY,   DayOfWeek.WEDNESDAY);
        assertSame(DayOfWeek.THURSDAY,    DayOfWeek.THURSDAY);
        assertSame(DayOfWeek.FRIDAY,      DayOfWeek.FRIDAY);
        assertSame(DayOfWeek.SATURDAY,    DayOfWeek.SATURDAY);
        assertSame(DayOfWeek.SUNDAY,      DayOfWeek.SUNDAY);
    }

    public void testNotEquals() {
        assertNotSame(DayOfWeek.MONDAY,      DayOfWeek.TUESDAY);
        assertNotSame(DayOfWeek.MONDAY,      DayOfWeek.WEDNESDAY);
        assertNotSame(DayOfWeek.MONDAY,      DayOfWeek.THURSDAY);
        assertNotSame(DayOfWeek.MONDAY,      DayOfWeek.FRIDAY);
        assertNotSame(DayOfWeek.MONDAY,      DayOfWeek.SATURDAY);
        assertNotSame(DayOfWeek.MONDAY,      DayOfWeek.SUNDAY);
    }

    public void testGetName() {
        assertEquals("月曜日", DayOfWeek.MONDAY.getName());
        assertEquals("火曜日", DayOfWeek.TUESDAY.getName());
        assertEquals("水曜日", DayOfWeek.WEDNESDAY.getName());
        assertEquals("木曜日", DayOfWeek.THURSDAY.getName());
        assertEquals("金曜日", DayOfWeek.FRIDAY.getName());
        assertEquals("土曜日", DayOfWeek.SATURDAY.getName());
        assertEquals("日曜日", DayOfWeek.SUNDAY.getName());
    }

    public void testGetEnumClass() {
        assertEquals(DayOfWeek.class, DayOfWeek.MONDAY.getEnumClass());
    }

    //== 以下はEnumUtils関連のテスト

    public void testGetEnumName() {
        assertEquals(DayOfWeek.MONDAY,      EnumUtils.getEnum(DayOfWeek.class, "月曜日"));
        assertEquals(DayOfWeek.TUESDAY,     EnumUtils.getEnum(DayOfWeek.class, "火曜日"));
        assertEquals(DayOfWeek.WEDNESDAY,   EnumUtils.getEnum(DayOfWeek.class, "水曜日"));
        assertEquals(DayOfWeek.THURSDAY,    EnumUtils.getEnum(DayOfWeek.class, "木曜日"));
        assertEquals(DayOfWeek.FRIDAY,      EnumUtils.getEnum(DayOfWeek.class, "金曜日"));
        assertEquals(DayOfWeek.SATURDAY,    EnumUtils.getEnum(DayOfWeek.class, "土曜日"));
        assertEquals(DayOfWeek.SUNDAY,      EnumUtils.getEnum(DayOfWeek.class, "日曜日"));
    }

    /*
     * EnumUtilsを使うと、「値」からオブジェクトを得ることができる
     */
    public void testGetEnumValue() {
        assertEquals(DayOfWeek.MONDAY,      EnumUtils.getEnum(DayOfWeek.class, 2));
        assertEquals(DayOfWeek.TUESDAY,     EnumUtils.getEnum(DayOfWeek.class, 3));
        assertEquals(DayOfWeek.WEDNESDAY,   EnumUtils.getEnum(DayOfWeek.class, 4));
        assertEquals(DayOfWeek.THURSDAY,    EnumUtils.getEnum(DayOfWeek.class, 5));
        assertEquals(DayOfWeek.FRIDAY,      EnumUtils.getEnum(DayOfWeek.class, 6));
        assertEquals(DayOfWeek.SATURDAY,    EnumUtils.getEnum(DayOfWeek.class, 7));
        assertEquals(DayOfWeek.SUNDAY,      EnumUtils.getEnum(DayOfWeek.class, 1));
    }

    /*
     * MapはHashMapで、かつ「名前がキー」なので、順序は不定
     */
    public void testGetEnumMap() {
        final Map<String, DayOfWeek> map = EnumUtils.getEnumMap(DayOfWeek.class);

        assertEquals(7, map.size());
        assertEquals(DayOfWeek.MONDAY,      map.get("月曜日"));
        assertEquals(DayOfWeek.TUESDAY,     map.get("火曜日"));
        assertEquals(DayOfWeek.WEDNESDAY,   map.get("水曜日"));
        assertEquals(DayOfWeek.THURSDAY,    map.get("木曜日"));
        assertEquals(DayOfWeek.FRIDAY,      map.get("金曜日"));
        assertEquals(DayOfWeek.SATURDAY,    map.get("土曜日"));
        assertEquals(DayOfWeek.SUNDAY,      map.get("日曜日"));
    }

    /*
     * Listはオブジェクトが生成された順に入っている（ソースコード上の順）
     */
    public void testGetEnumList() {
        final List<DayOfWeek> list = EnumUtils.getEnumList(DayOfWeek.class);

        assertEquals(7, list.size());
        assertEquals(DayOfWeek.MONDAY,      list.get(0));
        assertEquals(DayOfWeek.TUESDAY,     list.get(1));
        assertEquals(DayOfWeek.WEDNESDAY,   list.get(2));
        assertEquals(DayOfWeek.THURSDAY,    list.get(3));
        assertEquals(DayOfWeek.FRIDAY,      list.get(4));
        assertEquals(DayOfWeek.SATURDAY,    list.get(5));
        assertEquals(DayOfWeek.SUNDAY,      list.get(6));
        try {
            list.get(7);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IndexOutOfBoundsException.class, e.getClass());
        }
    }

    /*
     * Iteratorは、List#iterator()と同じ
     */
    public void testIterator() {
        final Iterator<DayOfWeek> it = EnumUtils.iterator(DayOfWeek.class);
        /* 上の１行は、
         * final Iterator it = EnumUtils.getEnumList(DayOfWeek.class).iterator();
         * と同じ。ってゆーか、そのもの。
         */

        assertEquals(DayOfWeek.MONDAY,      it.next());
        assertEquals(DayOfWeek.TUESDAY,     it.next());
        assertEquals(DayOfWeek.WEDNESDAY,   it.next());
        assertEquals(DayOfWeek.THURSDAY,    it.next());
        assertEquals(DayOfWeek.FRIDAY,      it.next());
        assertEquals(DayOfWeek.SATURDAY,    it.next());
        assertEquals(DayOfWeek.SUNDAY,      it.next());

        assertFalse(it.hasNext());

        try {
            it.next();
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(NoSuchElementException.class, e.getClass());
        }
    }

    public void testGetInstanceString() {
        assertEquals(DayOfWeek.MONDAY,      DayOfWeek.getInstance("MON"));
        assertEquals(DayOfWeek.TUESDAY,     DayOfWeek.getInstance("TUE"));
        assertEquals(DayOfWeek.WEDNESDAY,   DayOfWeek.getInstance("WED"));
        assertEquals(DayOfWeek.THURSDAY,    DayOfWeek.getInstance("THU"));
        assertEquals(DayOfWeek.FRIDAY,      DayOfWeek.getInstance("FRI"));
        assertEquals(DayOfWeek.SATURDAY,    DayOfWeek.getInstance("SAT"));
        assertEquals(DayOfWeek.SUNDAY,      DayOfWeek.getInstance("SUN"));

        assertEquals(DayOfWeek.MONDAY,      DayOfWeek.getInstance("2"));
        assertEquals(DayOfWeek.TUESDAY,     DayOfWeek.getInstance("3"));
        assertEquals(DayOfWeek.WEDNESDAY,   DayOfWeek.getInstance("4"));
        assertEquals(DayOfWeek.THURSDAY,    DayOfWeek.getInstance("5"));
        assertEquals(DayOfWeek.FRIDAY,      DayOfWeek.getInstance("6"));
        assertEquals(DayOfWeek.SATURDAY,    DayOfWeek.getInstance("7"));
        assertEquals(DayOfWeek.SUNDAY,      DayOfWeek.getInstance("1"));

        assertNull(DayOfWeek.getInstance("0"));
        assertNull(DayOfWeek.getInstance("AAA"));
    }
} // DayOfWeekTest

// eof
