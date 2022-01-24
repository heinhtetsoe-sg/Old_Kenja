// kanji=漢字
/*
 * $Id: ChargeDivTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/06/08 15:55:35 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.ChargeDiv;

import junit.framework.TestCase;

public class ChargeDivTest extends TestCase {

    public ChargeDivTest(String name) {
        super(name);
    }

    public void testGetCode() {
        assertEquals(1, ChargeDiv.REGULAR.getCode());
        assertEquals(0, ChargeDiv.VICE.getCode());
    }

    public void testGetName() {
        assertEquals("正担任", ChargeDiv.REGULAR.getName());
        assertEquals("副担任", ChargeDiv.VICE.getName());
    }

    public void testToString() {
        assertEquals("1:正担任", ChargeDiv.REGULAR.toString());
        assertEquals("0:副担任", ChargeDiv.VICE.toString());
    }

    public void testGetInstance() {
        assertSame(ChargeDiv.REGULAR, ChargeDiv.getInstance(1));
        assertSame(ChargeDiv.VICE, ChargeDiv.getInstance(0));
    }

    public void testGetInstanceNotFound() {
        assertNull(ChargeDiv.getInstance(-1));
        assertNull(ChargeDiv.getInstance(2));
        assertNull(ChargeDiv.getInstance(3));
    }

    public void testGetEnumList() {
        final List<ChargeDiv> list = ChargeDiv.getEnumList();
        assertEquals(2, list.size());

        assertEquals(ChargeDiv.REGULAR, list.get(0));
        assertEquals(ChargeDiv.VICE, list.get(1));

//        MyEnumUtils.clear(ChargeDiv.class);

        assertEquals(2, list.size());
    }

    public void testGetEnumMap() {
        final Map<Integer, ChargeDiv> map = ChargeDiv.getEnumMap();
        assertEquals(2, map.size());

        assertTrue(map.containsValue(ChargeDiv.REGULAR));
        assertTrue(map.containsValue(ChargeDiv.VICE));

//        MyEnumUtils.clear(ChargeDiv.class);

        assertEquals(2, map.size());
    }


    public void testSize() {
        assertEquals(2, ChargeDiv.size());
    }
} // ChargeDivTest

// eof
