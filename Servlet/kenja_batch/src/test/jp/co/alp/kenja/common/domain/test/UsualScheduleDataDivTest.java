// kanji=漢字
/*
 * $Id: UsualScheduleDataDivTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/17 14:53:52 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.awt.Color;

import javax.swing.JLabel;

import jp.co.alp.kenja.common.domain.UsualSchedule;

import junit.framework.TestCase;

public class UsualScheduleDataDivTest extends TestCase {

    public UsualScheduleDataDivTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testToString() {
        assertEquals("基本時間割から反映", UsualSchedule.DataDiv.BASIC.toString());
        assertEquals("通常時間割で編集", UsualSchedule.DataDiv.USUAL.toString());
        assertEquals("定期考査", UsualSchedule.DataDiv.EXAM.toString());
    }

    public void testGetCode() {
        assertEquals("0", UsualSchedule.DataDiv.BASIC.getCode());
        assertEquals("1", UsualSchedule.DataDiv.USUAL.getCode());
        assertEquals("2", UsualSchedule.DataDiv.EXAM.getCode());
    }

    public void testGetInstance() {
        assertSame(UsualSchedule.DataDiv.BASIC, UsualSchedule.DataDiv.getInstance(0));
        assertSame(UsualSchedule.DataDiv.USUAL, UsualSchedule.DataDiv.getInstance(1));
        assertSame(UsualSchedule.DataDiv.EXAM, UsualSchedule.DataDiv.getInstance(2));
    }

    public void testGetInstance_FAIL() {
        assertNull(UsualSchedule.DataDiv.getInstance(-1));
        assertNull(UsualSchedule.DataDiv.getInstance(3));
    }

    public void testUpdate() throws Exception {
        final JLabel label = new JLabel("");
        label.setForeground(Color.red);
        label.setBackground(Color.red);
        assertEquals(Color.red, label.getForeground());
        assertEquals(Color.red, label.getBackground());

        _update(UsualSchedule.DataDiv.BASIC, label);
        assertEquals("赤のまま", Color.red, label.getForeground());
        assertEquals("赤のまま", Color.red, label.getBackground());

        _update(UsualSchedule.DataDiv.USUAL, label);
        assertEquals("赤のまま", Color.red, label.getForeground());
        assertEquals("ライトグレイになる", Color.lightGray, label.getBackground());

        _update(UsualSchedule.DataDiv.EXAM, label);
        assertEquals("黒になる", Color.black, label.getForeground());
        assertEquals("淡い緑になる", new Color(144, 238, 144), label.getBackground());
    }

    private void _update(
            final UsualSchedule.DataDiv dataDiv,
            final Object obj
    ) throws SecurityException,
            NoSuchMethodException,
            IllegalArgumentException,
            IllegalAccessException,
            InvocationTargetException
    {
        final Method method = UsualSchedule.DataDiv.class.getDeclaredMethod("update", new Class[] { Object.class, });
        method.setAccessible(true);
        method.invoke(dataDiv, new Object[] { obj, });
    }
}
 // UsualScheduleDataDivTest

// eof
