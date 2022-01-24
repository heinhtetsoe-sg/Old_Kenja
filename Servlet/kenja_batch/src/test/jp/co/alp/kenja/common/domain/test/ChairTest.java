// kanji=漢字
/*
 * $Id: ChairTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2004/06/08 11:38:18 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiHashMap;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChairsHolder;
import jp.co.alp.kenja.common.domain.ChargeDiv;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class ChairTest extends TestCase {

    MyEnum.Category category;
    private SubClass _s1;
    private HomeRoom _hr1, _hr2, _hr3;
    private Chair _c1, _c2, _c3;

    public ChairTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _s1 = SubClass.create(category, "01", "H", "2", "010000", "国語I", "国I");
        _hr1 = HomeRoom.create(category, "1", "6", "1年6組", "1-6");
        _hr2 = HomeRoom.create(category, "2", "4", "2年4組", "2-4");
        _hr3 = HomeRoom.create(category, "3", "9", "3年9組", "3-9");
        _c1 = Chair.create(category, "0002582", GroupClass.ZERO, _s1, "情Ａ 106", new Integer(4), new Integer(1), true);
        _c2 = Chair.create(category, "0002600", GroupClass.ZERO, _s1, "生一 204", new Integer(3), new Integer(2), true);
        _c3 = Chair.create(category, "0004000", GroupClass.ZERO, _s1, "LHR 309", new Integer(2), new Integer(3), true);
        _c1.addHomeRoom(_hr1);
        _c1.addHomeRoom(_hr2);
        _c1.addHomeRoom(_hr3);
        _c2.addHomeRoom(_hr2);
        _c2.addHomeRoom(_hr3);
        _c3.addHomeRoom(_hr3);
    }

    protected void tearDown() throws Exception {
//        KenjaReInitializer.runAll(category);
//        HomeRoom.clearAll(category);
//        Chair.clearAll(category);
//        GroupClass.clearAll(category);
//        SubClass.clearAll(category);
    }

    public void testGetCode() {
        assertEquals("0002582", _c1.getCode());
        assertEquals("0002600", _c2.getCode());
        assertEquals("0004000", _c3.getCode());
    }

    public void testGetGroup() {
        assertSame(GroupClass.ZERO, _c1.getGroup());
        assertSame(GroupClass.ZERO, _c2.getGroup());
        assertSame(GroupClass.ZERO, _c3.getGroup());
    }

    public void testGetSubClass() {
        assertSame(_s1, _c1.getSubClass());
        assertSame(_s1, _c2.getSubClass());
        assertSame(_s1, _c3.getSubClass());
    }

    public void testGetName() {
        assertEquals("情Ａ 106", _c1.getName());
        assertEquals("生一 204", _c2.getName());
        assertEquals("LHR 309", _c3.getName());
    }

    public void testToString() {
        assertEquals("0002582:情Ａ 106/", _c1.toString());
        assertEquals("0002600:生一 204/", _c2.toString());
        assertEquals("0004000:LHR 309/", _c3.toString());
    }

    public void testGetChairs() {
        try {
            _c1.getChairs().clear();
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }

        try {
            _c1.getChairs().add(_c2);
            fail("例外が発生するので、ここには来ない");
        } catch (UnsupportedOperationException e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }
    }

    public void testAddHomeRoom() {
        /* 講座に年組を追加すると、その年組に職員が追加される。
         */

        // [_c3] の年組は[_hr3]一つ(※1)
        assertEquals(1, _c3.getHomeRooms().size());
        assertTrue(_c3.getHomeRooms().contains(_hr3));

        // [staff]を作成し、[_c3]に追加
        final Staff staff = Staff.create(category, "00000001", "00000001", "職員1", "職員1");
        _c3.addStaff(staff, ChargeDiv.REGULAR);

        // [3年4組]を作る
        final HomeRoom hr34 = HomeRoom.create(category, "3", "4", "3年4組", "3-4");
        assertEquals("3-4に職員はいない", 0, hr34.getStaffs().size());

        // [3年4組]に職員はいない(※2)
        assertEquals(0, hr34.getStaffs().size());

        // [_hr3] の職員はいない
        assertEquals(0, _hr3.getStaffs().size());

        // ★ [_c3]に[3年4組]を追加する
        _c3.addHomeRoom(hr34);

        // [_c3]に、この[3年4組]が追加されたことを確認(>>※1)
        assertEquals(2, _c3.getHomeRooms().size());
        assertTrue(_c3.getHomeRooms().contains(_hr3));
        assertTrue(_c3.getHomeRooms().contains(hr34));

        // [3年4組]に、[staff]が追加されたことを確認(>>※2)
        assertEquals(1, hr34.getStaffs().size());
        assertTrue(hr34.getStaffs().contains(staff));

        // ただし、[_hr3] の職員はいない (_c3への_h3追加時(setUp())には、職員がいなかったから)
        assertEquals(0, _hr3.getStaffs().size());
    }

    public void testGetInstance() {
        assertSame(_c1, Chair.getInstance(category, "0002582"));
        assertSame(_c2, Chair.getInstance(category, "0002600"));
        assertSame(_c3, Chair.getInstance(category, "0004000"));
    }

    public void testGetInstanceNotFound() {
        assertNull(Chair.getInstance(category, "xx"));
    }

    public void testIsAlive() {
        assertTrue(_c1.isAlive());
        assertTrue(_c2.isAlive());
        assertTrue(_c3.isAlive());

        Chair.clearAll(category);

        assertFalse(_c1.isAlive());
        assertFalse(_c2.isAlive());
        assertFalse(_c3.isAlive());
    }

    public void testGetIndex() {
        assertEquals(0, _c1.getIndex());
        assertEquals(1, _c2.getIndex());
        assertEquals(2, _c3.getIndex());
    }

    public void testCompareTo() {
        assertTrue(_c1.compareTo(_c1) == 0);
        assertTrue(_c1.compareTo(_c2) < 0);
        assertTrue(_c1.compareTo(_c3) < 0);

        assertTrue(_c2.compareTo(_c1) > 0);
        assertTrue(_c2.compareTo(_c2) == 0);
        assertTrue(_c2.compareTo(_c3) < 0);

        assertTrue(_c3.compareTo(_c1) > 0);
        assertTrue(_c3.compareTo(_c2) > 0);
        assertTrue(_c3.compareTo(_c3) == 0);
    }

    public void testHashCode() {
        assertTrue(_c1.hashCode() == Chair.getInstance(category, "0002582").hashCode());
        assertTrue(_c1.hashCode() != Chair.getInstance(category, "0002600").hashCode());
        assertTrue(_c1.hashCode() != Chair.getInstance(category, "0004000").hashCode());

        assertTrue(_c2.hashCode() != Chair.getInstance(category, "0002582").hashCode());
        assertTrue(_c2.hashCode() == Chair.getInstance(category, "0002600").hashCode());
        assertTrue(_c2.hashCode() != Chair.getInstance(category, "0004000").hashCode());

        assertTrue(_c3.hashCode() != Chair.getInstance(category, "0002582").hashCode());
        assertTrue(_c3.hashCode() != Chair.getInstance(category, "0002600").hashCode());
        assertTrue(_c3.hashCode() == Chair.getInstance(category, "0004000").hashCode());
    }

    public void testEqualsObject() {
        assertTrue(_c1.equals(_c1));
        assertFalse(_c1.equals(_c2));
        assertFalse(_c1.equals(_c3));

        assertFalse(_c2.equals(_c1));
        assertTrue(_c2.equals(_c2));
        assertFalse(_c2.equals(_c3));

        assertFalse(_c3.equals(_c1));
        assertFalse(_c3.equals(_c2));
        assertTrue(_c3.equals(_c3));
    }

    public void testCreate() {
        assertEquals(3, Chair.size(category));
        try {
            Chair.create(category, null, null, null, null, null, null, true);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(3, Chair.size(category));
    }

    public void testCreate_FOUND() {
        final Chair chair = Chair.create(category, "0002582", null, null, "既存と違う【コード以外】", new Integer(400), new Integer(100), true);
        assertSame(_c1, chair);
    }

    public void testGetEnumList() {
        final List<Chair> list = Chair.getEnumList(category);
        assertNotNull(list);
        assertEquals(3, list.size());

        assertSame(_c1, list.get(0));
        assertSame(_c2, list.get(1));
        assertSame(_c3, list.get(2));
    }

    public void testGetEnumMap() {
        final Map<String, Chair> map = Chair.getEnumMap(category);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(_c1));
        assertTrue(map.containsValue(_c2));
        assertTrue(map.containsValue(_c3));

        assertFalse(map.containsValue(Chair.NULL));
    }

    public void testSize() {
        assertEquals(3, Chair.size(category));
        Chair.clearAll(category);
        assertEquals(0, Chair.size(category));
    }

    public void testClear() {
        assertEquals(3, Chair.size(category));
        Chair.clearAll(category);
        assertEquals(0, Chair.size(category));
        Chair.clearAll(category);
        assertEquals(0, Chair.size(category));
    }

    public void testGetHomeRooms1() {
        final Set<HomeRoom> homerooms = _c1.getHomeRooms();
        assertEquals(3, homerooms.size());

        final Iterator<HomeRoom> it = homerooms.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());

        assertSame(_hr1, it.next());
        assertSame(_hr2, it.next());
        assertSame(_hr3, it.next());

        assertFalse(it.hasNext());
    }

    public void testGetHomeRooms2() {
        final Set<HomeRoom> homerooms = _c2.getHomeRooms();
        assertEquals(2, homerooms.size());

        final Iterator<HomeRoom> it = homerooms.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());

        assertSame(_hr2, it.next());
        assertSame(_hr3, it.next());

        assertFalse(it.hasNext());
    }

    public void testGetHomeRooms3() {
        final Set<HomeRoom> homerooms = _c3.getHomeRooms();
        assertEquals(1, homerooms.size());

        final Iterator <HomeRoom>it = homerooms.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());

        assertSame(_hr3, it.next());

        assertFalse(it.hasNext());
    }

    public void testNULL() {
        assertSame(_c1.getEnumClass(), _c2.getEnumClass());
        assertNotSame(_c1.getEnumClass(), Chair.NULL.getEnumClass());

        assertEquals("", Chair.NULL.toString());
        assertEquals("0000000", Chair.NULL.getCode());

        Chair.NULL.addHomeRoom(_hr1);
        assertEquals(0, Chair.NULL.getHomeRooms().size());

        assertSame(Chair.NULL, Chair.getInstance(category, "0000000"));
//        System.out.println(Chair.NULL.indexOf());

    }

    public void testNULLAddStaff() {
        Chair.NULL.addStaff(null, null);
    }

    public void test_ChairsHolder() {
        assertTrue(_c1 instanceof ChairsHolder);
    }

    public void test_MultiHashMap_createCollection_カバレッジ対策() throws Exception {
        final Field field = Chair.class.getDeclaredField("_staffs");
        field.setAccessible(true);
        final MultiHashMap map = (MultiHashMap) field.get(_c1);
        final Method method = MultiHashMap.class.getDeclaredMethod("createCollection", new Class[] {Collection.class, });
        method.setAccessible(true);
        method.invoke(map, new Object[] { new ArrayList(), });
    }
} // ChairTest

// eof
