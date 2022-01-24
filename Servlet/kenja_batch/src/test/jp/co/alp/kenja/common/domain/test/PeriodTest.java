// kanji=漢字
/*
 * $Id: PeriodTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2004/06/04 13:58:30 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class PeriodTest extends TestCase {
    MyEnum.Category category;
    private Period _p1;
    private Period _p2;
    private Period _p3;

    public PeriodTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _p1 = Period.create(category, "1", "SHR午前", "SHRa", "1", null);
        _p2 = Period.create(category, "2", "1校時", "1", null, null);
        _p3 = Period.create(category, "3", "2校時", "2", null, null);
    }

    protected void tearDown() throws Exception {
//        Period.clearAll(category);
    }

    public void testToString() {
        assertEquals("SHR午前", _p1.toString());
        assertEquals("1校時", _p2.toString());
        assertEquals("2校時", _p3.toString());
    }

    public void testGetCode() {
        assertEquals(1, _p1.getCode());
        assertEquals(2, _p2.getCode());
        assertEquals(3, _p3.getCode());
    }

    public void testGetCodeAsString() {
        assertEquals("1", _p1.getCodeAsString());
        assertEquals("2", _p2.getCodeAsString());
        assertEquals("3", _p3.getCodeAsString());
    }

    public void testGetName() {
        assertEquals("SHR午前", _p1.getName());
        assertEquals("1校時", _p2.getName());
        assertEquals("2校時", _p3.getName());
    }

    public void testGetShortName() {
        assertEquals("SHRa", _p1.getShortName());
        assertEquals("1", _p2.getShortName());
        assertEquals("2", _p3.getShortName());
    }

    public void testIsSpecial() {
        assertTrue(_p1.isSpecial());
        assertFalse(_p2.isSpecial());
        assertFalse(_p3.isSpecial());
    }

    public void testGetSpecial() {
        assertEquals("1", _p1.getSpecial());
        assertNull(_p2.getSpecial());
        assertNull(_p3.getSpecial());
    }

    public void testNext() {
        assertSame(_p2, _p1.next());
        assertSame(_p3, _p2.next());
        assertNull(_p3.next());
    }

    public void testPrevious() {
        assertSame(_p2, _p3.previous());
        assertSame(_p1, _p2.previous());
        assertNull(_p1.previous());
    }

    public void testGetInstance() {
        assertSame(_p1, Period.getInstance(category, "1"));
        assertSame(_p2, Period.getInstance(category, "2"));
        assertSame(_p3, Period.getInstance(category, "3"));
    }

    public void testGetInstanceNotFound() {
        assertNull(Period.getInstance(category, 99));
        assertNull(Period.getInstance(category, "Z"));
    }

    public void testIsAlive() {
        assertTrue(_p1.isAlive());
        assertTrue(_p2.isAlive());
        assertTrue(_p3.isAlive());

        Period.clearAll(category);

        assertFalse(_p1.isAlive());
        assertFalse(_p2.isAlive());
        assertFalse(_p3.isAlive());
    }

    public void testCompareTo() {
        assertTrue(_p1.compareTo(_p1) == 0);
        assertTrue(_p1.compareTo(_p2) < 0);
        assertTrue(_p1.compareTo(_p3) < 0);

        assertTrue(_p2.compareTo(_p1) > 0);
        assertTrue(_p2.compareTo(_p2) == 0);
        assertTrue(_p2.compareTo(_p3) < 0);

        assertTrue(_p3.compareTo(_p1) > 0);
        assertTrue(_p3.compareTo(_p2) > 0);
        assertTrue(_p3.compareTo(_p3) == 0);
    }

    public void testHashCode() {
        assertTrue(_p1.hashCode() == Period.getInstance(category, "1").hashCode());
        assertTrue(_p1.hashCode() != Period.getInstance(category, "2").hashCode());
        assertTrue(_p1.hashCode() != Period.getInstance(category, "3").hashCode());

        assertTrue(_p2.hashCode() != Period.getInstance(category, "1").hashCode());
        assertTrue(_p2.hashCode() == Period.getInstance(category, "2").hashCode());
        assertTrue(_p2.hashCode() != Period.getInstance(category, "3").hashCode());

        assertTrue(_p3.hashCode() != Period.getInstance(category, "1").hashCode());
        assertTrue(_p3.hashCode() != Period.getInstance(category, "2").hashCode());
        assertTrue(_p3.hashCode() == Period.getInstance(category, "3").hashCode());
    }

    public void testEqualsObject() {
        assertTrue(_p1.equals(_p1));
        assertFalse(_p1.equals(_p2));
        assertFalse(_p1.equals(_p3));

        assertFalse(_p2.equals(_p1));
        assertTrue(_p2.equals(_p2));
        assertFalse(_p2.equals(_p3));

        assertFalse(_p3.equals(_p1));
        assertFalse(_p3.equals(_p2));
        assertTrue(_p3.equals(_p3));
    }

    public void testCreate() {
        assertEquals(3, Period.size(category));
        final Period p = Period.create(category, "1", "<<既存コード>>", "<<既存コード>>", "<<既存コード.>", null);
        assertSame(_p1, p);
        assertEquals(3, Period.size(category));
    }

    public void testCreate_Fail() {
        assertEquals(3, Period.size(category));
        try {
            Period.create(category, "-1", null, null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(3, Period.size(category));
    }

    public void testGetEnumString() {
        assertSame(_p1, Period.getEnum(category, "SHR午前"));
        assertSame(_p2, Period.getEnum(category, "1校時"));
        assertSame(_p3, Period.getEnum(category, "2校時"));
    }

    public void testGetEnumList() {
        final List<Period> list = Period.getEnumList(category);
        assertNotNull(list);
        assertEquals(3, list.size());

        assertSame(_p1, list.get(0));
        assertSame(_p2, list.get(1));
        assertSame(_p3, list.get(2));
    }

    public void testGetEnumMap() {
        final Map<Integer, Period> map = Period.getEnumMap(category);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(_p1));
        assertTrue(map.containsValue(_p2));
        assertTrue(map.containsValue(_p3));
    }

    public void testSize() {
        assertEquals(3, Period.size(category));
        Period.clearAll(category);
        assertEquals(0, Period.size(category));
    }

    public void testClear() {
        assertEquals(3, Period.size(category));
        Period.clearAll(category);
        assertEquals(0, Period.size(category));
        Period.clearAll(category);
        assertEquals(0, Period.size(category));
    }
} // PeriodTest

// eof
