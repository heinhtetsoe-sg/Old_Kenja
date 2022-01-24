// kanji=漢字
/*
 * $Id: GroupClassDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/24 13:53:02 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.djtest;

import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class GroupClassDJTest extends DJUnitTestCase {

    MyEnum.Category category;
    private GroupClass _group;

    public GroupClassDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _group = GroupClass.create(category, "0001", "理科1", "理1", "理1の備考");
    }

    public void test_getCodeカバレッジ対策() {
        addReturnValue("MyEnum", "getKey", new Integer(0));

        try {
            _group.getCode();
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalStateException.class, e.getClass());
        }
    }
} // GroupClassDJTest

// eof
