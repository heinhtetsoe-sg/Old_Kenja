// kanji=漢字
/*
 * $Id: SubClassTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/06/07 18:28:00 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class SubClassTest extends TestCase {

    MyEnum.Category category;
    private SubClass _i1;
    private SubClass _i2;
    private SubClass _i3;

    public SubClassTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _i1 = SubClass.create(category, "01", "H", "2", "010000", "国語I", "国I");
        _i2 = SubClass.create(category, "02", "H", "2", "020100", "世界史A", "世Ａ");
        _i3 = SubClass.create(category, "04", "H", "2", "040100", "数学I", "数I");
    }

    public void testToString() {
        assertEquals("010000:国I", _i1.toString());
        assertEquals("020100:世Ａ", _i2.toString());
        assertEquals("040100:数I", _i3.toString());
    }

    public void testGetCode() {
        assertEquals("010000", _i1.getCode());
        assertEquals("020100", _i2.getCode());
        assertEquals("040100", _i3.getCode());
    }

    public void testGetName() {
        assertEquals("国語I", _i1.getName());
        assertEquals("世界史A", _i2.getName());
        assertEquals("数学I", _i3.getName());
    }

    public void testGetAbbr() {
        assertEquals("国I", _i1.getAbbr());
        assertEquals("世Ａ", _i2.getAbbr());
        assertEquals("数I", _i3.getAbbr());
    }

    public void testGetInstance() {
        assertSame(_i1, SubClass.getInstance(category, "01", "H", "2", "010000"));
        assertSame(_i2, SubClass.getInstance(category, "02", "H", "2", "020100"));
        assertSame(_i3, SubClass.getInstance(category, "04", "H", "2", "040100"));
    }

    public void testGetInstanceNotFound() {
        assertNull(SubClass.getInstance(category, "xx", "x", "x", "xx"));
    }

    public void testIsAlive() {
        assertTrue(_i1.isAlive());
        assertTrue(_i2.isAlive());
        assertTrue(_i3.isAlive());

        SubClass.clearAll(category);

        assertFalse(_i1.isAlive());
        assertFalse(_i2.isAlive());
        assertFalse(_i3.isAlive());
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
        assertTrue(_i1.hashCode() == SubClass.getInstance(category, "01", "H", "2", "010000").hashCode());
        assertTrue(_i1.hashCode() != SubClass.getInstance(category, "02", "H", "2", "020100").hashCode());
        assertTrue(_i1.hashCode() != SubClass.getInstance(category, "04", "H", "2", "040100").hashCode());

        assertTrue(_i2.hashCode() != SubClass.getInstance(category, "01", "H", "2", "010000").hashCode());
        assertTrue(_i2.hashCode() == SubClass.getInstance(category, "02", "H", "2", "020100").hashCode());
        assertTrue(_i2.hashCode() != SubClass.getInstance(category, "04", "H", "2", "040100").hashCode());

        assertTrue(_i3.hashCode() != SubClass.getInstance(category, "01", "H", "2", "010000").hashCode());
        assertTrue(_i3.hashCode() != SubClass.getInstance(category, "02", "H", "2", "020100").hashCode());
        assertTrue(_i3.hashCode() == SubClass.getInstance(category, "04", "H", "2", "040100").hashCode());
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

    public void testCreate() {
        assertEquals(3, SubClass.size(category));
        try {
            SubClass.create(category, null, null, null, null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(3, SubClass.size(category));
    }

    public void testCreateFOUND() {
        final SubClass subClass = SubClass.create(category, "01", "H", "2", "010000", "既存と違う【名称】", "既存と違う【略称】");
        assertSame(_i1, subClass);
        assertEquals("国語I", subClass.getName());
        assertEquals("国I", subClass.getAbbr());
    }

    public void testGetEnumList() {
        final List<SubClass> list = SubClass.getEnumList(category);
        assertNotNull(list);
        assertEquals(3, list.size());

        assertSame(_i1, list.get(0));
        assertSame(_i2, list.get(1));
        assertSame(_i3, list.get(2));
    }

    public void testGetEnumMap() {
        final Map<String, SubClass> map = SubClass.getEnumMap(category);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(_i1));
        assertTrue(map.containsValue(_i2));
        assertTrue(map.containsValue(_i3));
    }

    public void testSize() {
        assertEquals(3, SubClass.size(category));
        SubClass.clearAll(category);
        assertEquals(0, SubClass.size(category));
    }

    public void testClear() {
        assertEquals(3, SubClass.size(category));
        SubClass.clearAll(category);
        assertEquals(0, SubClass.size(category));
        SubClass.clearAll(category);
        assertEquals(0, SubClass.size(category));
    }

    public void testNULL() {
        assertSame(_i1.getEnumClass(), _i2.getEnumClass());
        assertNotSame(_i1.getEnumClass(), SubClass.NULL.getEnumClass());

        assertEquals("", SubClass.NULL.toString());
        assertEquals("", SubClass.NULL.getCode());
        assertEquals("", SubClass.NULL.getAbbr());

        assertSame(SubClass.NULL, SubClass.getInstance(category, "", "", "", ""));
    }

}
 // SubClassTest

// eof
