// kanji=漢字
/*
 * $Id: HomeRoomTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2004/06/07 17:28:27 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jp.co.alp.kenja.common.domain.Gender;
import jp.co.alp.kenja.common.domain.Grade;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import junit.framework.TestCase;

public class HomeRoomTest extends TestCase {

    MyEnum.Category category;
    private HomeRoom _i1;
    private HomeRoom _i2;
    private HomeRoom _i3;

    public HomeRoomTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _i1 = HomeRoom.create(category, "1", "1", "1年1組", "1x1");
        _i2 = HomeRoom.create(category, "1", "2", "1年2組", "1x2");
        _i3 = HomeRoom.create(category, "1", "3", "1年3組", "1x3");
    }

//    public void testToString() {
//        assertEquals("1-1:1x1", _i1.toString());
//        assertEquals("1-2:1x2", _i2.toString());
//        assertEquals("1-3:1x3", _i3.toString());
//    }

    public void testToString() {
        assertEquals("1x1", _i1.toString());
        assertEquals("1x2", _i2.toString());
        assertEquals("1x3", _i3.toString());
    }

    public void testGetGrade() {
        final Grade gr1 = Grade.getInstance(category, "1");
        assertEquals(gr1, _i1.getGrade());
        assertEquals(gr1, _i2.getGrade());
        assertEquals(gr1, _i3.getGrade());
    }

    public void testGetRoom() {
        assertEquals("1", _i1.getRoom());
        assertEquals("2", _i2.getRoom());
        assertEquals("3", _i3.getRoom());
    }

    public void testGetName() {
        assertEquals("1年1組", _i1.getName());
        assertEquals("1年2組", _i2.getName());
        assertEquals("1年3組", _i3.getName());
    }

    public void testGetAbbr() {
        assertEquals("1x1", _i1.getAbbr());
        assertEquals("1x2", _i2.getAbbr());
        assertEquals("1x3", _i3.getAbbr());
    }

    public void testGetInstance() {
        assertSame(_i1, HomeRoom.getInstance(category, "1", "1"));
        assertSame(_i2, HomeRoom.getInstance(category, "1", "2"));
        assertSame(_i3, HomeRoom.getInstance(category, "1", "3"));
    }

    public void testGetInstanceNotFound() {
        assertNull(HomeRoom.getInstance(category, "99", "99"));
    }

    public void testGetInstanceNotFound_WHEN_GradeIsNULL() {
        assertNull(HomeRoom.getInstance(category, (Grade) null, "1"));
    }

    public void testIsAlive() {
        assertTrue(_i1.isAlive());
        assertTrue(_i2.isAlive());
        assertTrue(_i3.isAlive());

        HomeRoom.clearAll(category);

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
        assertTrue(_i1.hashCode() == HomeRoom.getInstance(category, "1", "1").hashCode());
        assertTrue(_i1.hashCode() != HomeRoom.getInstance(category, "1", "2").hashCode());
        assertTrue(_i1.hashCode() != HomeRoom.getInstance(category, "1", "3").hashCode());

        assertTrue(_i2.hashCode() != HomeRoom.getInstance(category, "1", "1").hashCode());
        assertTrue(_i2.hashCode() == HomeRoom.getInstance(category, "1", "2").hashCode());
        assertTrue(_i2.hashCode() != HomeRoom.getInstance(category, "1", "3").hashCode());

        assertTrue(_i3.hashCode() != HomeRoom.getInstance(category, "1", "1").hashCode());
        assertTrue(_i3.hashCode() != HomeRoom.getInstance(category, "1", "2").hashCode());
        assertTrue(_i3.hashCode() == HomeRoom.getInstance(category, "1", "3").hashCode());
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
        assertEquals(3, HomeRoom.size(category));
        try {
            HomeRoom.create(category, (String) null, "4", "n年4組", "0x4");
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        try {
            HomeRoom.create(category, "2", null, "2年n組", "2x0");
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        try {
            HomeRoom.create(category, "2", "2", "2年2組", null);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        try {
            HomeRoom.create(category, "3", "3", null, "3x3");
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        assertEquals(3, HomeRoom.size(category));
    }

    public void testCreateFound() {
        final HomeRoom hr = HomeRoom.create(category, "1", "1", "既存の1年1組と同じ【名称】", "既存の1年1組と同じ【略称】");
        assertSame(_i1, hr);
        assertEquals("1x1", hr.getAbbr());
        assertEquals("1年1組", hr.getName());
    }

    public void testGetEnumList() {
        final List<HomeRoom> list = HomeRoom.getEnumList(category);
        assertNotNull(list);
        assertEquals(3, list.size());

        assertSame(_i1, list.get(0));
        assertSame(_i2, list.get(1));
        assertSame(_i3, list.get(2));
    }

    public void testGetEnumMap() {
        final Map<String, HomeRoom> map = HomeRoom.getEnumMap(category);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(_i1));
        assertTrue(map.containsValue(_i2));
        assertTrue(map.containsValue(_i3));
    }

    public void testSize() {
        assertEquals(3, HomeRoom.size(category));
        HomeRoom.clearAll(category);
        assertEquals(0, HomeRoom.size(category));
    }

    public void testClear() {
        assertEquals(3, HomeRoom.size(category));
        HomeRoom.clearAll(category);
        assertEquals(0, HomeRoom.size(category));
        HomeRoom.clearAll(category);
        assertEquals(0, HomeRoom.size(category));
    }

    public void testStudents() {
        assertEquals(0, _i1.getStudents().size());  // この時点で生徒はいない
        Student student = Student.create(category,
                "1234",
                "内外くぶん",
                "表示用氏名",
                null,
                null,
                Gender.MALE,
                Student.GrdDiv.NORMAL,
                KenjaDateImpl.getInstance(2005, 2, 25),
                _i1,
                "4321",
                true
        );
        assertEquals(1, _i1.getStudents().size());  // 生徒が１人、HRにぶら下がった
        assertTrue(_i1.getStudents().contains(student));
    }

    public void testToString_STATIC_0() {
        assertEquals("なし", HomeRoom.toString(new TreeSet<HomeRoom>(), "なし"));
    }

    public void testToString_STATIC_1() {
        final Set<HomeRoom> set = new TreeSet<HomeRoom>();
        set.add(_i3);
        assertEquals(1, set.size());
        assertEquals("1x3", HomeRoom.toString(set, "なし"));
    }

    public void testToString_STATIC_2() {
        final Set<HomeRoom> set = new TreeSet<HomeRoom>();
        set.add(_i2);
        set.add(_i3);
        assertEquals(2, set.size());
        assertEquals("1x2", HomeRoom.toString(set, "なし"));
    }

    public void testToString_STATIC_3() {
        final Set<HomeRoom> set = new TreeSet<HomeRoom>();
        set.add(_i1);
        set.add(_i2);
        set.add(_i3);
        assertEquals(3, set.size());
        assertEquals("1x1", HomeRoom.toString(set, "なし"));
    }

    public void testWithStaffs() {
        final Staff namihei = Staff.create(category, "00100004", "00100004", "磯野　波平", "磯野波平");
        final Staff isasaka = Staff.create(category, "00202001", "00202001", "伊佐坂　難物", "伊佐坂難物");
        final Staff norisuke = Staff.create(category, "00204001", "00204001", "波野　ノリスケ", "波野ノリスケ");

        _i1.addStaff(namihei);
        _i1.addStaff(isasaka);

        final Collection<Staff> coll_1 = _i1.getStaffs();
        assertNotNull(coll_1);
        assertEquals(2, coll_1.size());
        assertTrue(coll_1.contains(namihei));
        assertTrue(coll_1.contains(isasaka));
        assertFalse(coll_1.contains(norisuke));

        final Collection<Staff> coll_2 = _i2.getStaffs();
        assertNotNull(coll_2);
        assertEquals(0, coll_2.size());
    }
}
 // HomeRoomTest

// eof