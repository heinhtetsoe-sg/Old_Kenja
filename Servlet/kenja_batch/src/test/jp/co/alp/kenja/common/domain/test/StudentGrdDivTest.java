// kanji=漢字
/*
 * $Id: StudentGrdDivTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/17 14:38:09 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import jp.co.alp.kenja.common.domain.Student;

import junit.framework.TestCase;

public class StudentGrdDivTest extends TestCase {

    public StudentGrdDivTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testToString() {
        assertEquals("-", Student.GrdDiv.NORMAL.toString());
        assertEquals("卒業", Student.GrdDiv.GRADUATED.toString());
        assertEquals("退学", Student.GrdDiv.GAVE_UP.toString());
        assertEquals("転学", Student.GrdDiv.TRANSFERRED.toString());
    }

    public void testGetCode() {
        assertEquals("0", Student.GrdDiv.NORMAL.getCode());
        assertEquals("1", Student.GrdDiv.GRADUATED.getCode());
        assertEquals("2", Student.GrdDiv.GAVE_UP.getCode());
        assertEquals("3", Student.GrdDiv.TRANSFERRED.getCode());
    }

    public void testGetInstance() {
        assertSame(Student.GrdDiv.NORMAL, Student.GrdDiv.getInstance(0));
        assertSame(Student.GrdDiv.GRADUATED, Student.GrdDiv.getInstance(1));
        assertSame(Student.GrdDiv.GAVE_UP, Student.GrdDiv.getInstance(2));
        assertSame(Student.GrdDiv.TRANSFERRED, Student.GrdDiv.getInstance(3));
    }

    public void testGetInstance_FAIL() {
        assertNull(Student.GrdDiv.getInstance(-1));
        assertNull(Student.GrdDiv.getInstance(4));
    }
} // StudentGrdDivTest

// eof
