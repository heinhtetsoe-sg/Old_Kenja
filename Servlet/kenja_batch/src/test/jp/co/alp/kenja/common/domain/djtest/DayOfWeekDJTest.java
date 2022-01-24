// kanji=漢字
/*
 * $Id: DayOfWeekDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/16 21:19:12 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.djtest;

import java.util.Calendar;

import jp.co.alp.kenja.common.domain.DayOfWeek;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class DayOfWeekDJTest extends DJUnitTestCase {

    public DayOfWeekDJTest(String name) {
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

    public void test_カバレッジ対策() {
        final Calendar cal = Calendar.getInstance();
        try {
            addReturnValue("Calendar", "get", new Integer(0));
            DayOfWeek.getInstance(cal);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }
} // DayOfWeekDJTest

// eof
