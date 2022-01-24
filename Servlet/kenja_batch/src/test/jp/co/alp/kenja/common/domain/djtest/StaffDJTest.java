// kanji=漢字
/*
 * $Id: StaffDJTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2006/03/28 15:23:52 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.djtest;

import java.util.Set;
import java.util.TreeSet;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.dgic.testing.framework.DJUnitTestCase;

public class StaffDJTest extends DJUnitTestCase {

    MyEnum.Category category;
    private Staff _c1;

    public StaffDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _c1 = Staff.create(category, "12345678", "12345678", "職員", "職員");
    }

    public void test_getCodeカバレッジ対策() {
        addReturnValue("MyEnum", "getKey", new Integer(0));

        try {
            _c1.getCode();
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalStateException.class, e.getClass());
        }
    }

    public void test_getChairsカバレッジ対策() {
        addReturnValue("MultiHashMap", "values", new TreeSet());
        final Set<Chair> chairs = _c1.getChairs();
        assertNotNull(chairs);
        assertEquals(true, chairs.isEmpty());
    }
} // StaffDJTest

// eof
