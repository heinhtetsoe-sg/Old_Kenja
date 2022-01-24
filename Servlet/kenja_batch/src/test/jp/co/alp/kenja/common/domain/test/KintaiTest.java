// kanji=漢字
/*
 * $Id: KintaiTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/02/11 13:58:30 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class KintaiTest extends TestCase {
    MyEnum.Category category;
    private Kintai _k0;
    private Kintai _k1;
    private Kintai _k2;
    private Kintai _k3;
    private Kintai _k4;
    private Kintai _k5;
    private Kintai _k51;

    public KintaiTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _k0 = Kintai.create(category, "0", "ほんとうの出席", "", null); // 本当の出席はゼロ
        _k1 = Kintai.create(category, "1", "出席", "", null);    // 出席
        _k2 = Kintai.create(category, "2", "公欠", "欠", null);  // 通常
        _k3 = Kintai.create(category, "3", "出停", "△", null);  // 記号
        _k4 = Kintai.create(category, "4", "病欠", "/ビ", null); // 半角カタカナ混じり
        _k5 = Kintai.create(category, "8", "一日公欠", "公", "2"); // 代替コードあり
        _k51 = Kintai.create(category, "51", "病欠1", "欠1", null, "4"); // 代替コードあり
    }

    public void testToString() {
        assertEquals("1:出席", _k1.toString());
        assertEquals("2:公欠", _k2.toString());
        assertEquals("3:出停", _k3.toString());
        assertEquals("4:病欠", _k4.toString());
        assertEquals("8:2:一日公欠", _k5.toString());
    }

    public void testGetCode() {
        assertEquals(1, _k1.getCode());
        assertEquals(2, _k2.getCode());
        assertEquals(3, _k3.getCode());
        assertEquals(4, _k4.getCode());
        assertEquals(8, _k5.getCode());
        assertEquals(51, _k51.getCode());
    }

    public void testGetAltCode() {
        assertEquals(1, _k1.getAltCode().intValue());
        assertEquals(2, _k2.getAltCode().intValue());
        assertEquals(3, _k3.getAltCode().intValue());
        assertEquals(4, _k4.getAltCode().intValue());
        assertEquals(2, _k5.getAltCode().intValue()); // 代替コードあり
        assertEquals(4, _k51.getAltCode().intValue()); // 代替コードあり
    }

    public void testIsSeated() {
        assertTrue(_k0.isSeated());
        assertFalse(_k1.isSeated());
        assertFalse(_k2.isSeated());
        assertFalse(_k3.isSeated());
        assertFalse(_k4.isSeated());
        assertFalse(_k5.isSeated());
        assertFalse(_k51.isSeated());
    }

    public void testGetName() {
        assertEquals("出席", _k1.getName());
        assertEquals("公欠", _k2.getName());
        assertEquals("出停", _k3.getName());
        assertEquals("病欠", _k4.getName());
        assertEquals("一日公欠", _k5.getName());
        assertEquals("病欠1", _k51.getName());
    }

    public void testGetMark() {
        assertEquals("", _k1.getMark());
        assertEquals("欠", _k2.getMark());
        assertEquals("△", _k3.getMark());
        assertEquals("/ビ", _k4.getMark());
        assertEquals("公", _k5.getMark());
        assertEquals("欠1", _k51.getMark());
    }

    public void testGetInstance() {
        assertSame(_k1, Kintai.getInstance(category, "1"));
        assertSame(_k2, Kintai.getInstance(category, "2"));
        assertSame(_k3, Kintai.getInstance(category, "3"));
        assertSame(_k4, Kintai.getInstance(category, "4"));
        assertSame(_k5, Kintai.getInstance(category, "8"));
        assertSame(_k51, Kintai.getInstance(category, "51"));
    }

    public void testGetInstanceNotFound() {
        assertNull(Kintai.getInstance(category, 99));
    }

    public void testIsAlive() {
        assertTrue(_k1.isAlive());
        assertTrue(_k2.isAlive());
        assertTrue(_k3.isAlive());
        assertTrue(_k4.isAlive());
        assertTrue(_k5.isAlive());
        assertTrue(_k51.isAlive());

        Kintai.clearAll(category);

        assertFalse(_k1.isAlive());
        assertFalse(_k2.isAlive());
        assertFalse(_k3.isAlive());
        assertFalse(_k4.isAlive());
        assertFalse(_k5.isAlive());
        assertFalse(_k51.isAlive());
    }

    public void testCompareTo() {
        assertTrue(_k1.compareTo(_k1) == 0);
        assertTrue(_k1.compareTo(_k2) < 0);
        assertTrue(_k1.compareTo(_k3) < 0);
        assertTrue(_k1.compareTo(_k4) < 0);

        assertTrue(_k2.compareTo(_k1) > 0);
        assertTrue(_k2.compareTo(_k2) == 0);
        assertTrue(_k2.compareTo(_k3) < 0);
        assertTrue(_k2.compareTo(_k4) < 0);

        assertTrue(_k3.compareTo(_k1) > 0);
        assertTrue(_k3.compareTo(_k2) > 0);
        assertTrue(_k3.compareTo(_k3) == 0);
        assertTrue(_k3.compareTo(_k4) < 0);

        assertTrue(_k4.compareTo(_k1) > 0);
        assertTrue(_k4.compareTo(_k2) > 0);
        assertTrue(_k4.compareTo(_k3) > 0);
        assertTrue(_k4.compareTo(_k4) == 0);
    }

    public void testHashCode() {
        assertTrue(_k1.hashCode() == Kintai.getInstance(category, "1").hashCode());
        assertTrue(_k1.hashCode() != Kintai.getInstance(category, "2").hashCode());
        assertTrue(_k1.hashCode() != Kintai.getInstance(category, "3").hashCode());
        assertTrue(_k1.hashCode() != Kintai.getInstance(category, "4").hashCode());

        assertTrue(_k2.hashCode() != Kintai.getInstance(category, "1").hashCode());
        assertTrue(_k2.hashCode() == Kintai.getInstance(category, "2").hashCode());
        assertTrue(_k2.hashCode() != Kintai.getInstance(category, "3").hashCode());
        assertTrue(_k2.hashCode() != Kintai.getInstance(category, "4").hashCode());

        assertTrue(_k3.hashCode() != Kintai.getInstance(category, "1").hashCode());
        assertTrue(_k3.hashCode() != Kintai.getInstance(category, "2").hashCode());
        assertTrue(_k3.hashCode() == Kintai.getInstance(category, "3").hashCode());
        assertTrue(_k3.hashCode() != Kintai.getInstance(category, "4").hashCode());

        assertTrue(_k4.hashCode() != Kintai.getInstance(category, "1").hashCode());
        assertTrue(_k4.hashCode() != Kintai.getInstance(category, "2").hashCode());
        assertTrue(_k4.hashCode() != Kintai.getInstance(category, "3").hashCode());
        assertTrue(_k4.hashCode() == Kintai.getInstance(category, "4").hashCode());
    }

    public void testEqualsObject() {
        assertTrue(_k1.equals(_k1));
        assertFalse(_k1.equals(_k2));
        assertFalse(_k1.equals(_k3));
        assertFalse(_k1.equals(_k4));

        assertFalse(_k2.equals(_k1));
        assertTrue(_k2.equals(_k2));
        assertFalse(_k2.equals(_k3));
        assertFalse(_k2.equals(_k4));

        assertFalse(_k3.equals(_k1));
        assertFalse(_k3.equals(_k2));
        assertTrue(_k3.equals(_k3));
        assertFalse(_k3.equals(_k4));

        assertFalse(_k4.equals(_k1));
        assertFalse(_k4.equals(_k2));
        assertFalse(_k4.equals(_k3));
        assertTrue(_k4.equals(_k4));
    }

    public void testCreate_Fail() {
        assertEquals(7, Kintai.size(category));
        try {
            Kintai.create(category, "-1", null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(7, Kintai.size(category));
    }

    public void testCreate_Fail_NumberFormatException() {
        try {
            Kintai.create(category, "99", "<<非数字>>", "<<非数字>>", "<<非数字>>");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testCreate() {
        assertSame(_k0, Kintai.create(category, "0", "<<既存のコード>>", "<<既存のコード>>", null));
    }

    public void testGetEnumList() {
        final List<Kintai> list = Kintai.getEnumList(category);
        assertNotNull(list);
        assertEquals(7, list.size());

        assertSame(_k0, list.get(0));
        assertSame(_k1, list.get(1));
        assertSame(_k2, list.get(2));
        assertSame(_k3, list.get(3));
        assertSame(_k4, list.get(4));
        assertSame(_k5, list.get(5));
        assertSame(_k51, list.get(6));
    }

    public void testGetEnumMap() {
        final Map<Integer, Kintai> map = Kintai.getEnumMap(category);
        assertNotNull(map);
        assertEquals(7, map.size());
        assertTrue(map.containsValue(_k0));
        assertTrue(map.containsValue(_k1));
        assertTrue(map.containsValue(_k2));
        assertTrue(map.containsValue(_k3));
        assertTrue(map.containsValue(_k4));
        assertTrue(map.containsValue(_k5));
        assertTrue(map.containsValue(_k51));
    }

    public void testSize() {
        assertEquals(7, Kintai.size(category));
        Kintai.clearAll(category);
        assertEquals(0, Kintai.size(category));
    }

    public void testClear() {
        assertEquals(7, Kintai.size(category));
        Kintai.clearAll(category);
        assertEquals(0, Kintai.size(category));
        Kintai.clearAll(category);
        assertEquals(0, Kintai.size(category));
    }

    public void testHasAltCode() {
        assertFalse(_k1.hasAltCode());
        assertFalse(_k2.hasAltCode());
        assertFalse(_k3.hasAltCode());
        assertFalse(_k4.hasAltCode());

        assertTrue(_k5.hasAltCode());
        assertTrue(_k51.hasAltCode());
    }

    public void testGetAltKinta() {
        assertEquals(_k1, _k1.getAltKintai());
        assertEquals(_k2, _k2.getAltKintai());
        assertEquals(_k3, _k3.getAltKintai());
        assertEquals(_k4, _k4.getAltKintai());

        assertEquals(_k2, _k5.getAltKintai());
        assertEquals(_k4, _k51.getAltKintai());
    }

    public void testGetSeated() {
        assertSame(_k0, Kintai.getSeated(category));
    }
} // KintaiTest

// eof
