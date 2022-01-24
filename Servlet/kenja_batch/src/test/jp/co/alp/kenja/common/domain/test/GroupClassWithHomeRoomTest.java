// kanji=漢字
/*
 * $Id: GroupClassWithHomeRoomTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/23 20:55:23 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Collection;
import java.util.Set;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class GroupClassWithHomeRoomTest extends TestCase {

    MyEnum.Category category;
    private GroupClass _group;
    private SubClass _subClass;
    private Chair _chair;

    public GroupClassWithHomeRoomTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _group = GroupClass.create(category, "0001", "理科1", "理1", "理1の備考");
        _subClass = SubClass.create(category, "01", "H", "2", "010000", "理科", "理");
        _chair = Chair.create(category, "0002582", _group, _subClass, "理1 106", new Integer(4), new Integer(1), true);
    }

    public void testWithHomeRoom() {
        final HomeRoom hr = HomeRoom.create(category, "1", "6", "1年6組", "1-6");
        _group.addHomeRoom(hr);

        final Collection<Chair> chairs = hr.getGrade().getChairs();
        assertNotNull(chairs);
        assertTrue(chairs.contains(_chair));
        assertEquals(1, chairs.size());

        final Set<HomeRoom> homeRooms = _group.getHomeRooms();
        assertNotNull(homeRooms);
        assertTrue(homeRooms.contains(hr));
        assertEquals(1, homeRooms.size());
    }
} // GroupClassWithHomeRoomTest

// eof
