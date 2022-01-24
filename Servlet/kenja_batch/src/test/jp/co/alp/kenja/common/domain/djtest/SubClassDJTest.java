// kanji=漢字
/*
 * $Id: SubClassDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/22 20:40:30 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.djtest;

import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class SubClassDJTest extends DJUnitTestCase {

    MyEnum.Category category;
    private SubClass _s1;

    public SubClassDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _s1 = SubClass.create(category, "01", "H", "2", "010000", "国語I", "国I");
    }

    public void test_getCodeカバレッジ対策() {
        addReturnValue("MyEnum", "getKey", new Integer(0));

        try {
            _s1.getCode();
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalStateException.class, e.getClass());
        }
    }
} // SubClassDJTest

// eof
