// kanji=漢字
/*
 * $Id: SemesterDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/17 13:28:52 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.djtest;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class SemesterDJTest extends DJUnitTestCase {

    public SemesterDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        try {
            ;
        } finally {
            super.tearDown();
        }
    }

    public void test_カバレッジ対策_getCode() {
        final MyEnum.Category c = new MyEnum.Category();
        final Semester s1 = Semester.create(c, 1, "1学期", KenjaDateImpl.getInstance(2006, 4, 6), KenjaDateImpl.getInstance(2006, 10, 6));
        assertNotNull(s1);
        assertEquals(1, s1.getCode());

        addReturnValue("MyEnum", "getKey", "<<カバレッジ対策>>");
        try {
            s1.getCode();
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalStateException.class, e.getClass());
        }
    }
} // SemesterDJTest

// eof
