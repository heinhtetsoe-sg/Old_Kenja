// kanji=漢字
/*
 * $Id: KintaiDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/17 20:52:04 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.djtest;

import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class KintaiDJTest extends DJUnitTestCase {
    private MyEnum.Category cat;

    public KintaiDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        cat = new MyEnum.Category();
    }

    public void test_カバレッジ対策() {
        final Kintai k = Kintai.create(cat, "0", "", "出席", null);
        assertNotNull(k);

        addReturnValue("MyEnum", "getKey", "<<カバレッジ対策>>");
        try {
            k.getCode();
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalStateException.class, e.getClass());
        }
    }
} // KintaiDJTest

// eof
