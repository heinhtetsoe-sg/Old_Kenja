// kanji=漢字
/*
 * $Id: SemesterTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/06/04 21:22:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class SemesterTest extends TestCase {
    MyEnum.Category category;
    private Semester _i1;
    private Semester _i2;
    private Semester _i3;
    private Semester _i9;

    public SemesterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _i1 = Semester.create(category, 1, "一学期", KenjaDateImpl.getInstance(2004, 4, 1), KenjaDateImpl.getInstance(2004, 7, 21));
        _i2 = Semester.create(category, 2, "二学期", KenjaDateImpl.getInstance(2004, 9, 1), KenjaDateImpl.getInstance(2004, 12, 24));
        _i3 = Semester.create(category, 3, "三学期", KenjaDateImpl.getInstance(2005, 1, 6), KenjaDateImpl.getInstance(2005, 3, 23));
        _i9 = Semester.create(category, 9, "年間",   KenjaDateImpl.getInstance(2004, 4, 1), KenjaDateImpl.getInstance(2005, 3, 23));
    }

    protected void tearDown() throws Exception {
//        Semester.clearAll(category);
    }

    public void testToString() {
        assertEquals("1:一学期[2004-04-01(木),2004-07-21(水)]", _i1.toString());
        assertEquals("2:二学期[2004-09-01(水),2004-12-24(金)]", _i2.toString());
        assertEquals("3:三学期[2005-01-06(木),2005-03-23(水)]", _i3.toString());
        assertEquals("9:年間[2004-04-01(木),2005-03-23(水)]", _i9.toString());
    }

    public void testGetCode() {
        assertEquals(1, _i1.getCode());
        assertEquals(2, _i2.getCode());
        assertEquals(3, _i3.getCode());
        assertEquals(9, _i9.getCode());
    }

    public void testGetCodeAsString() {
        assertEquals("1", _i1.getCodeAsString());
        assertEquals("2", _i2.getCodeAsString());
        assertEquals("3", _i3.getCodeAsString());
        assertEquals("9", _i9.getCodeAsString());
    }

    public void testGetName() {
        assertEquals("一学期", _i1.getName());
        assertEquals("二学期", _i2.getName());
        assertEquals("三学期", _i3.getName());
        assertEquals("年間", _i9.getName());
    }

    public void testGetInstance() {
        assertSame(_i1, Semester.getInstance(category, 1));
        assertSame(_i2, Semester.getInstance(category, 2));
        assertSame(_i3, Semester.getInstance(category, 3));
        assertSame(_i9, Semester.getInstance(category, 9));
    }

    public void testGetInstanceNotFound() {
        assertNull(Semester.getInstance(category, 4));
    }

    public void testIsAlive() {
        assertTrue(_i1.isAlive());
        assertTrue(_i2.isAlive());
        assertTrue(_i3.isAlive());
        assertTrue(_i9.isAlive());

        Semester.clearAll(category);

        assertFalse(_i1.isAlive());
        assertFalse(_i2.isAlive());
        assertFalse(_i3.isAlive());
        assertFalse(_i9.isAlive());
    }

    public void testGetSDate() {
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 1), _i1.getSDate());
        assertEquals(KenjaDateImpl.getInstance(2004, 9, 1), _i2.getSDate());
        assertEquals(KenjaDateImpl.getInstance(2005, 1, 6), _i3.getSDate());
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 1), _i9.getSDate());
    }

    public void testGetEDate() {
        assertEquals(KenjaDateImpl.getInstance(2004,  7, 21), _i1.getEDate());
        assertEquals(KenjaDateImpl.getInstance(2004, 12, 24), _i2.getEDate());
        assertEquals(KenjaDateImpl.getInstance(2005,  3, 23), _i3.getEDate());
        assertEquals(KenjaDateImpl.getInstance(2005,  3, 23), _i9.getEDate());
    }

    public void testIsValidDate() {
        assertFalse(_i2.isValidDate(KenjaDateImpl.getInstance(2004,  8, 31)));
        assertTrue(_i2.isValidDate(KenjaDateImpl.getInstance(2004, 9, 1)));
        assertTrue(_i2.isValidDate(KenjaDateImpl.getInstance(2004, 12, 24)));
        assertFalse(_i2.isValidDate(KenjaDateImpl.getInstance(2004, 12, 25)));
    }

    public void testCompareTo() {
        assertTrue(_i1.compareTo(_i1) == 0);
        assertTrue(_i1.compareTo(_i2) < 0);
        assertTrue(_i1.compareTo(_i3) < 0);

        assertTrue(_i2.compareTo(_i1) > 0);
        assertTrue(_i2.compareTo(_i2) == 0);
        assertTrue(_i2.compareTo(_i3) < 0);

        assertTrue(_i3.compareTo(_i1) > 0);
        assertTrue(_i3.compareTo(_i2) > 0);
        assertTrue(_i3.compareTo(_i3) == 0);
    }

    public void testHashCode() {
        assertTrue(_i1.hashCode() == Semester.getInstance(category, 1).hashCode());
        assertTrue(_i1.hashCode() != Semester.getInstance(category, 2).hashCode());
        assertTrue(_i1.hashCode() != Semester.getInstance(category, 3).hashCode());

        assertTrue(_i2.hashCode() != Semester.getInstance(category, 1).hashCode());
        assertTrue(_i2.hashCode() == Semester.getInstance(category, 2).hashCode());
        assertTrue(_i2.hashCode() != Semester.getInstance(category, 3).hashCode());

        assertTrue(_i3.hashCode() != Semester.getInstance(category, 1).hashCode());
        assertTrue(_i3.hashCode() != Semester.getInstance(category, 2).hashCode());
        assertTrue(_i3.hashCode() == Semester.getInstance(category, 3).hashCode());
    }

    public void testEqualsObject() {
        assertTrue(_i1.equals(_i1));
        assertFalse(_i1.equals(_i2));
        assertFalse(_i1.equals(_i3));

        assertFalse(_i2.equals(_i1));
        assertTrue(_i2.equals(_i2));
        assertFalse(_i2.equals(_i3));

        assertFalse(_i3.equals(_i1));
        assertFalse(_i3.equals(_i2));
        assertTrue(_i3.equals(_i3));
    }

    public void testThroughTheYear() {
        assertSame(_i9, Semester.throughTheYear(category));
    }

    public void testCreate() {
        assertSame(_i1, Semester.create(category, 1, null, null, null));
    }

    public void testCreateFail() {
        assertEquals(4, Semester.size(category));
        try {
            Semester.create(category, 0, null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(4, Semester.size(category));
    }

    public void testGetEnumList() {
        final List<Semester> list = Semester.getEnumList(category);
        assertNotNull(list);
        assertEquals(4, list.size());

        assertSame(_i1, list.get(0));
        assertSame(_i2, list.get(1));
        assertSame(_i3, list.get(2));
        assertSame(_i9, list.get(3));
    }

    public void testGetEnumMap() {
        final Map<Integer, Semester> map = Semester.getEnumMap(category);
        assertNotNull(map);
        assertEquals(4, map.size());
        assertTrue(map.containsValue(_i1));
        assertTrue(map.containsValue(_i2));
        assertTrue(map.containsValue(_i3));
        assertTrue(map.containsValue(_i9));
    }

    public void testSize() {
        assertEquals(4, Semester.size(category));
        Semester.clearAll(category);
        assertEquals(0, Semester.size(category));
    }

    public void testClear() {
        assertEquals(4, Semester.size(category));
        Semester.clearAll(category);
        assertEquals(0, Semester.size(category));
        Semester.clearAll(category);
        assertEquals(0, Semester.size(category));
    }
}
 // SemesterTest

// eof