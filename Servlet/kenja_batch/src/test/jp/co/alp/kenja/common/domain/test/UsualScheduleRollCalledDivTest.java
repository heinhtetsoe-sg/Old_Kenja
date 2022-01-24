// kanji=漢字
/*
 * $Id: UsualScheduleRollCalledDivTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/17 11:46:25 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.awt.Color;

import javax.swing.JLabel;

import jp.co.alp.kenja.common.domain.UsualSchedule;

import junit.framework.TestCase;

public class UsualScheduleRollCalledDivTest extends TestCase {
    public UsualScheduleRollCalledDivTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testToString() {
        assertEquals("未実施", UsualSchedule.RollCalledDiv.NOTYET.toString());
        assertEquals("実施済み", UsualSchedule.RollCalledDiv.FINISHED.toString());
        assertEquals("未実施(一部済み)", UsualSchedule.RollCalledDiv.MIXED.toString());
    }

    public void testGetCode() {
        assertEquals("0", UsualSchedule.RollCalledDiv.NOTYET.getCode());
        assertEquals("1", UsualSchedule.RollCalledDiv.FINISHED.getCode());
        assertEquals("99", UsualSchedule.RollCalledDiv.MIXED.getCode());
    }

    public void testGetLabel() {
        assertEquals("出欠未", UsualSchedule.RollCalledDiv.NOTYET.getLabel());
        assertEquals("出欠済", UsualSchedule.RollCalledDiv.FINISHED.getLabel());
        assertEquals("出欠未(一部済み)", UsualSchedule.RollCalledDiv.MIXED.getLabel());
    }

    public void testUpdate() {
        final JLabel label = new JLabel("");
        label.setForeground(Color.black);
        assertEquals(Color.black, label.getForeground());
    }

    public void testGetInstance() {
        assertSame(UsualSchedule.RollCalledDiv.NOTYET, UsualSchedule.RollCalledDiv.getInstance(0));
        assertSame(UsualSchedule.RollCalledDiv.FINISHED, UsualSchedule.RollCalledDiv.getInstance(1));
        assertNull("99はMIXEDのコードだが、getInstance(int)では扱わない", UsualSchedule.RollCalledDiv.getInstance(99));
    }

    public void testGetInstance_NULL() {
        assertNull(UsualSchedule.RollCalledDiv.getInstance(-1));
        assertNull(UsualSchedule.RollCalledDiv.getInstance(2));
        assertNull("99はMIXEDのコードだが、getInstance(int)では扱わない", UsualSchedule.RollCalledDiv.getInstance(99));
    }
} // UsualScheduleRollCalledDivTest

// eof
