// kanji=漢字
/*
 * $Id: ChairWithGroupClassTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/27 14:59:13 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class ChairWithGroupClassTest extends TestCase {

    MyEnum.Category category;
    private GroupClass _group;
    private Chair _chair;
    private HomeRoom _hr;

    public ChairWithGroupClassTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _group = GroupClass.create(category, "0001", "理科1", "理1", "理1の備考");
        _hr = HomeRoom.create(category, "1", "6", "1年6組", "106");
        _group.addHomeRoom(_hr);
        final SubClass subClass = SubClass.create(category, "01", "H", "2", "010000", "理科I", "理I");
        _chair = Chair.create(category, "0002582", _group, subClass, "理I 106", new Integer(4), new Integer(1), true);
    }

    public void testGetGroup() {
        assertSame(_group, _chair.getGroup());
    }

    public void testGetHomeRooms() {
        assertEquals(1, _chair.getHomeRooms().size());
        assertTrue(_chair.getHomeRooms().contains(_hr));
    }

    public void testGetInfoGroup() {
        assertEquals("0001:理科1", _chair.getInfoGroup());
    }
} // ChairWithGroupClassTest

// eof
