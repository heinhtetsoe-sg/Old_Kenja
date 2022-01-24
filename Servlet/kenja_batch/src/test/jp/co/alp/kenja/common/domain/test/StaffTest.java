// kanji=漢字
/*
 * $Id: StaffTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2004/06/07 11:45:24 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.MultiHashMap;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChargeDiv;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import junit.framework.TestCase;

public class StaffTest extends TestCase {
    MyEnum.Category category;
    private Staff _i1;
    private Staff _i2;
    private Staff _i3;

    public StaffTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();

        Period.create(category, "1", "SHRa", "Sa", "1", null);
        Period.create(category, "2", "１校時", "1", null, null);
        Period.create(category, "3", "２校時", "2", null, null);
        Period.create(category, "4", "３校時", "3", null, null);

        _i1 = Staff.create(category, "00100004", "00100004", "磯野　波平", "磯野波平");
        _i2 = Staff.create(category, "00202001", "00202001", "伊佐坂　難物", "伊佐坂難物");
        _i3 = Staff.create(category, "00204001", "00204001", "波野　ノリスケ", "波野ノリスケ");
    }

    protected void tearDown() throws Exception {
//        Staff.clearAll(category);
//        Section.clearAll(category);
    }

    public void testGetChairsEx() {
        /* 2004-08-31.講座を１つも持たない職員の場合、getChairs(ChargeDiv.REGULAR)を呼び出した場合、
         * 1回目は空Setを返すが、2回目は空Setを含むSetを返すというバグを発見し、
         * このメソッドで再現および修正後の正常動作を確認した。
         */
        Staff st = Staff.create(category, "99999999", "99999999", "職員", "職員");
        Collection<Chair> o1 = st.getChairs(ChargeDiv.REGULAR);
        assertSame(o1, st.getChairs(ChargeDiv.REGULAR));
        assertTrue(Set.class.isInstance(o1));
        assertEquals(0, o1.size());
    }

//    public void testToString() {
//        assertEquals("2001:国語/00100004:磯野波平", _i1.toString());
//        assertEquals("2002:社会/00202001:伊佐坂難物", _i2.toString());
//        assertEquals("2004:数学/00204001:波野ノリスケ", _i3.toString());
//    }

    public void testToString() {
        assertEquals("磯野波平", _i1.toString());
        assertEquals("伊佐坂難物", _i2.toString());
        assertEquals("波野ノリスケ", _i3.toString());
    }

    public void testGetCode() {
        assertEquals("00100004", _i1.getCode());
        assertEquals("00202001", _i2.getCode());
        assertEquals("00204001", _i3.getCode());
    }

    public void testGetName() {
        assertEquals("磯野　波平", _i1.getName());
        assertEquals("伊佐坂　難物", _i2.getName());
        assertEquals("波野　ノリスケ", _i3.getName());
    }

    public void testGetShowName() {
        assertEquals("磯野波平", _i1.getShowName());
        assertEquals("伊佐坂難物", _i2.getShowName());
        assertEquals("波野ノリスケ", _i3.getShowName());
    }

    public void testGetInstance() {
        assertSame(_i1, Staff.getInstance(category, "00100004"));
        assertSame(_i2, Staff.getInstance(category, "00202001"));
        assertSame(_i3, Staff.getInstance(category, "00204001"));
    }

    public void testGetInstanceNotFound() {
        assertNull(Staff.getInstance(category, "xx"));
    }

    public void testIsAlive() {
        assertTrue(_i1.isAlive());
        assertTrue(_i2.isAlive());
        assertTrue(_i3.isAlive());

        Staff.clearAll(category);

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
        assertTrue(_i1.hashCode() == Staff.getInstance(category, "00100004").hashCode());
        assertTrue(_i1.hashCode() != Staff.getInstance(category, "00202001").hashCode());
        assertTrue(_i1.hashCode() != Staff.getInstance(category, "00204001").hashCode());

        assertTrue(_i2.hashCode() != Staff.getInstance(category, "00100004").hashCode());
        assertTrue(_i2.hashCode() == Staff.getInstance(category, "00202001").hashCode());
        assertTrue(_i2.hashCode() != Staff.getInstance(category, "00204001").hashCode());

        assertTrue(_i3.hashCode() != Staff.getInstance(category, "00100004").hashCode());
        assertTrue(_i3.hashCode() != Staff.getInstance(category, "00202001").hashCode());
        assertTrue(_i3.hashCode() == Staff.getInstance(category, "00204001").hashCode());
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
        assertEquals(3, Staff.size(category));

        try {
            Staff.create(category, null, null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        assertEquals(3, Staff.size(category));
    }

    public void testCreateFOUND() {
        assertEquals(3, Staff.size(category));

        final Staff staff = Staff.create(category, "00100004", "00100004", "既存と違う【名称】", "既存と違う【表示名】");
        assertSame(staff, _i1);

        assertEquals(3, Staff.size(category));
    }

    public void testGetEnumList() {
        final List<Staff> list = Staff.getEnumList(category);
        assertNotNull(list);
        assertEquals(3, list.size());

        assertSame(_i1, list.get(0));
        assertSame(_i2, list.get(1));
        assertSame(_i3, list.get(2));
    }

    public void testGetEnumMap() {
        final Map<String, Staff> map = Staff.getEnumMap(category);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(_i1));
        assertTrue(map.containsValue(_i2));
        assertTrue(map.containsValue(_i3));
    }

    public void testSize() {
        assertEquals(3, Staff.size(category));
        Staff.clearAll(category);
        assertEquals(0, Staff.size(category));
    }

    public void testClear() {
        assertEquals(3, Staff.size(category));
        Staff.clearAll(category);
        assertEquals(0, Staff.size(category));
        Staff.clearAll(category);
        assertEquals(0, Staff.size(category));
    }

    public void test_MultiHashMap_createCollection_カバレッジ対策() throws Exception {
        final Field field = Staff.class.getDeclaredField("_chairs");
        field.setAccessible(true);
        final MultiHashMap map = (MultiHashMap) field.get(_i1);
        final Method method = MultiHashMap.class.getDeclaredMethod("createCollection", new Class[] {Collection.class, });
        method.setAccessible(true);
        method.invoke(map, new Object[] { new ArrayList(), });
    }

    public void testToStringSTATIC() {
        final TreeSet<Staff> staffs = new TreeSet<Staff>();
        staffs.add(_i1);
        staffs.add(_i2);
        staffs.add(_i3);

        assertEquals("<<00100004 磯野波平,,00202001 伊佐坂難物,,00204001 波野ノリスケ>>", Staff.toString(staffs, "<<", ",,", ">>"));
    }

    public void testToStringSTATIC_null() {
        final TreeSet<Staff> staffs = null;
        assertNull(Staff.toString(staffs, "<<", ",,", ">>"));
    }

    public void testToStringSTATIC_empty() {
        final TreeSet<Staff> staffs = new TreeSet<Staff>();
        assertEquals("<<>>", Staff.toString(staffs, "<<", ",,", ">>"));
    }
} // StaffTest

// eof
