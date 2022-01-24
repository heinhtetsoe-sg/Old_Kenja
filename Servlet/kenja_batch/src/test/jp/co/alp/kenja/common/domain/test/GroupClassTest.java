// kanji=漢字
/*
 * $Id: GroupClassTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/06/07 20:24:44 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.co.alp.kenja.common.domain.ChairsHolder;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class GroupClassTest extends TestCase {

    MyEnum.Category category;
    private HomeRoom _hr1, _hr2, _hr3;

    private GroupClass _i1;
    private GroupClass _i2;
    private GroupClass _i3;

    public GroupClassTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _hr1 = HomeRoom.create(category, "1", "6", "1年6組", "1-6");
        _hr2 = HomeRoom.create(category, "2", "4", "2年4組", "2-4");
        _hr3 = HomeRoom.create(category, "3", "9", "3年9組", "3-9");
        _i1 = GroupClass.create(category, "0001", "理科1", "理1", "理1の備考");
        _i2 = GroupClass.create(category, "0002", "理科2", "理2", "理2の備考");
        _i3 = GroupClass.create(category, "0003", "社会文化", "社文", "社文の備考");
        _i1.addHomeRoom(_hr1);
        _i1.addHomeRoom(_hr2);
        _i1.addHomeRoom(_hr3);
        _i2.addHomeRoom(_hr2);
        _i2.addHomeRoom(_hr3);
        _i3.addHomeRoom(_hr3);
    }

    protected void tearDown() throws Exception {
//        GroupClass.clearAll(category);
    }

    public void testToString() {
        assertEquals("0001:理1[0]", _i1.toString());
        assertEquals("0002:理2[0]", _i2.toString());
        assertEquals("0003:社文[0]", _i3.toString());
    }

    public void testGetCode() {
        assertEquals("0001", _i1.getCode());
        assertEquals("0002", _i2.getCode());
        assertEquals("0003", _i3.getCode());
    }

    public void testGetName() {
        assertEquals("理科1", _i1.getName());
        assertEquals("理科2", _i2.getName());
        assertEquals("社会文化", _i3.getName());
    }

    public void testGetAbbr() {
        assertEquals("理1", _i1.getAbbr());
        assertEquals("理2", _i2.getAbbr());
        assertEquals("社文", _i3.getAbbr());
    }

    public void testGetRemark() {
        assertEquals("理1の備考", _i1.getRemark());
        assertEquals("理2の備考", _i2.getRemark());
        assertEquals("社文の備考", _i3.getRemark());
    }

    public void testGetInstance() {
        assertSame(_i1, GroupClass.getInstance(category, "0001"));
        assertSame(_i2, GroupClass.getInstance(category, "0002"));
        assertSame(_i3, GroupClass.getInstance(category, "0003"));
    }

    public void testGetInstanceNotFound() {
        assertNull(GroupClass.getInstance(category, "xx"));
    }

    public void testIsAlive() {
        assertEquals(4, GroupClass.size(category));
        assertTrue(GroupClass.ZERO.isAlive());
        assertTrue(_i1.isAlive());
        assertTrue(_i2.isAlive());
        assertTrue(_i3.isAlive());

        GroupClass.clearAll(category);

        assertEquals(1, GroupClass.size(category));
        assertTrue(GroupClass.ZERO.isAlive());
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
        assertTrue(_i1.hashCode() == GroupClass.getInstance(category, "0001").hashCode());
        assertTrue(_i1.hashCode() != GroupClass.getInstance(category, "0002").hashCode());
        assertTrue(_i1.hashCode() != GroupClass.getInstance(category, "0003").hashCode());

        assertTrue(_i2.hashCode() != GroupClass.getInstance(category, "0001").hashCode());
        assertTrue(_i2.hashCode() == GroupClass.getInstance(category, "0002").hashCode());
        assertTrue(_i2.hashCode() != GroupClass.getInstance(category, "0003").hashCode());

        assertTrue(_i3.hashCode() != GroupClass.getInstance(category, "0001").hashCode());
        assertTrue(_i3.hashCode() != GroupClass.getInstance(category, "0002").hashCode());
        assertTrue(_i3.hashCode() == GroupClass.getInstance(category, "0003").hashCode());
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

    public void testCreateEx() {
        assertEquals(4, GroupClass.size(category));
        try {
            GroupClass.create(category, null, null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(4, GroupClass.size(category));
    }

    public void testCreateFOUND() {
        assertEquals(4, GroupClass.size(category));
        final GroupClass group = GroupClass.create(category, "0001", "既存と違う【名称】", "既存と違う【略称】", "既存と違う【備考】");
        assertSame(group, _i1);
        assertEquals("理科1", group.getName());
        assertEquals("理1", group.getAbbr());
        assertEquals("理1の備考", group.getRemark());
        assertEquals(4, GroupClass.size(category));
    }

    public void testCreate_コードがnull() {
        assertEquals(4, GroupClass.size(category));
        try {
            GroupClass.create(category, null, "群名称", "群略称", "群備考");
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(4, GroupClass.size(category));
    }

    public void testCreate_名称がnull() {
        assertEquals(4, GroupClass.size(category));
        final GroupClass g91 = GroupClass.create(category, "0091", null, "群略称91", "群備考91");

        assertEquals(5, GroupClass.size(category));
        assertEquals("群略称91", g91.getName());
        assertEquals("群略称91", g91.getAbbr());
    }

    public void testCreate_略称がnull() {
        assertEquals(4, GroupClass.size(category));
        try {
            GroupClass.create(category, "0090", "群名称", null, "群備考");
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        assertEquals(4, GroupClass.size(category));
    }

    public void testCreate_備考がnull() {
        assertEquals(4, GroupClass.size(category));
        final GroupClass g92 = GroupClass.create(category, "0092", "群名称92", "群略称92", null);

        assertEquals(5, GroupClass.size(category));
        assertEquals("", g92.getRemark());
        assertEquals("群名称92", g92.getName());
        assertEquals("群略称92", g92.getAbbr());
    }

    public void testZero() {
        GroupClass.clearAll(category);
        assertEquals(1, GroupClass.size(category));

        assertNotNull(GroupClass.ZERO);
        assertSame(GroupClass.ZERO, GroupClass.getInstance(category, "0000"));
        assertTrue(GroupClass.ZERO.isAlive());
        assertEquals("0000", GroupClass.ZERO.getCode());
        assertEquals("", GroupClass.ZERO.getAbbr());
        assertEquals("", GroupClass.ZERO.toString());
    }

    public void testGetEnumList() {
        final List<GroupClass> list = GroupClass.getEnumList(category);
        assertNotNull(list);
        assertEquals(4, list.size());

        assertSame(GroupClass.ZERO, list.get(0));
        assertSame(_i1, list.get(1));
        assertSame(_i2, list.get(2));
        assertSame(_i3, list.get(3));
    }

    public void testGetEnumListEMPTY() {
        MyEnum.Category c = new MyEnum.Category();
        final List<GroupClass> list = GroupClass.getEnumList(c);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertTrue(list.contains(GroupClass.ZERO));
    }

    public void testGetEnumMap() {
        final Map<String, GroupClass> map = GroupClass.getEnumMap(category);
        assertNotNull(map);
        assertEquals(4, map.size());

        assertTrue(map.containsValue(GroupClass.ZERO));
        assertTrue(map.containsValue(_i1));
        assertTrue(map.containsValue(_i2));
        assertTrue(map.containsValue(_i3));
    }

    public void testSize() {
        assertEquals(4, GroupClass.size(category));
        GroupClass.clearAll(category);
        assertEquals(1, GroupClass.size(category));
    }

    public void testClear() {
        assertEquals(4, GroupClass.size(category));
        GroupClass.clearAll(category);
        assertEquals(1, GroupClass.size(category));
        GroupClass.clearAll(category);
        assertEquals(1, GroupClass.size(category));
    }

    public void testGetInfoGroup() {
        assertEquals("0001:理科1", _i1.getInfoGroup());
        assertEquals("0002:理科2", _i2.getInfoGroup());
        assertEquals("0003:社会文化", _i3.getInfoGroup());
    }

    public void testGetInfoGroupZERO() {
        assertEquals("", GroupClass.ZERO.getInfoGroup());
    }

    public void testGetHomeRooms1() {
        final Set<HomeRoom> homerooms = _i1.getHomeRooms();
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
        final Set<HomeRoom> homerooms = _i2.getHomeRooms();
        assertEquals(2, homerooms.size());

        final Iterator<HomeRoom> it = homerooms.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());

        assertSame(_hr2, it.next());
        assertSame(_hr3, it.next());

        assertFalse(it.hasNext());
    }

    public void testGetHomeRooms3() {
        final Set<HomeRoom> homerooms = _i3.getHomeRooms();
        assertEquals(1, homerooms.size());

        final Iterator<HomeRoom> it = homerooms.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());

        assertSame(_hr3, it.next());

        assertFalse(it.hasNext());
    }

    public void test_ZERO_カバレッジ対策() {
        final Set<HomeRoom> homeRooms = GroupClass.ZERO.getHomeRooms();
        assertNotNull(homeRooms);
        assertEquals(0, homeRooms.size());
        GroupClass.ZERO.addHomeRoom(null);
    }

    public void test_ChairsHolder() {
        assertTrue(_i1 instanceof ChairsHolder);
    }

    public void testIsEmpty() {
        assertFalse(GroupClass.isEmpty(category));

        GroupClass.clearAll(category);

        assertTrue(GroupClass.isEmpty(category));
    }
} // GroupClassTest

// eof
