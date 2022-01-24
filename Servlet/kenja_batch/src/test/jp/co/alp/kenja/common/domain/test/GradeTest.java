// kanji=漢字
/*
 * $Id: GradeTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/11/15 14:04:20 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.Grade;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class GradeTest extends TestCase {

    MyEnum.Category category;
    Grade _g1;

    public GradeTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _g1 = Grade.create(category, "01");
    }

    //====

    public void testGetCode() {
        assertEquals("01", _g1.getCode());
    }

    public void testClear() {
        assertEquals(0, _g1.getHomeRooms().size());
        HomeRoom.create(category, _g1, "2", "1年2組", "1-2");
        assertEquals("clear()前はHRが1個", 1, _g1.getHomeRooms().size());

        _g1.clear();

        assertEquals("clear()後はHRがゼロ個", 0, _g1.getHomeRooms().size());
    }

    public void testWithHomeRoom() {
        assertEquals(0, _g1.getHomeRooms().size());
        final HomeRoom hr = HomeRoom.create(category, _g1, "2", "1年2組", "1-2"); // HomeRoom.create()からaddHomeRoom()が呼ばれる
        assertEquals(1, _g1.getHomeRooms().size());
        final Collection<HomeRoom> coll = _g1.getHomeRooms();
        assertTrue(coll.contains(hr));
    }

    public void testAddChair() {
        // TODO: 後日
    }

    public void testGetChairs() {
        // TODO: 後日
    }

    public void testToString() {
        assertEquals("01学年", _g1.toString());
    }

    public void testCreate() {
        // Grade.create()は他のテストと一緒に利用している
    }

    public void testGetInstance() {
        assertEquals(_g1, Grade.getInstance(category, "01"));
    }

    public void testGetEnumList() {
        final List<Grade> list = Grade.getEnumList(category);
        assertEquals(1, list.size());
        assertTrue(list.contains(_g1));
    }

    public void testGetEnumMap() {
        final Map<String, Grade> map = Grade.getEnumMap(category);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("01"));
        assertTrue(map.containsValue(_g1));
    }

    public void testSize() {
        assertEquals(1, Grade.size(category));
        Grade.create(category, "01"); // すでにある_g1とコードが同じなので、size()は増えない
        assertEquals(1, Grade.size(category));

        Grade.create(category, "03"); // create()すると、size()が増える
        assertEquals(2, Grade.size(category));
    }

    public void testClearAll() {
        assertTrue(_g1.isAlive());
        assertEquals(1, Grade.size(category));

        Grade.clearAll(category);

        assertFalse( _g1.isAlive());
        assertEquals(0, Grade.size(category));
    }
} // GradeTest

// eof
