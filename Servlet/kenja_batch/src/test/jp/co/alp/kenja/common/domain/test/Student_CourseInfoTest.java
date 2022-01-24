// kanji=漢字
/*
 * $Id: Student_CourseInfoTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/22 21:48:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import jp.co.alp.kenja.common.domain.CourseInfo;
import jp.co.alp.kenja.common.domain.Grade;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class Student_CourseInfoTest extends TestCase {

    MyEnum.Category category;
    private Grade _grade;
    private CourseInfo _courseInfo;

    public Student_CourseInfoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _grade = Grade.create(category, "01");
        _courseInfo = CourseInfo.create(category, "1", "001", "0000", _grade);
    }

    public void testGetCourseCd() {
        assertEquals("1", _courseInfo.getCourseCd());
    }

    public void testGetMajorCd() {
        assertEquals("001", _courseInfo.getMajorCd());
    }

    public void testGetCourseCode() {
        assertEquals("0000", _courseInfo.getCourseCode());
    }

    public void testGetGrade() {
        final Grade g = _courseInfo.getGrade();
        assertNotNull(g);
        assertSame(_grade, g);
    }

    public void testCreate() {
        final CourseInfo ci = CourseInfo.create(category, "2", "001", "0000", _grade);
        assertNotNull(ci);
        assertNotSame(ci, _courseInfo);
    }

    public void testCreateFOUND() {
        final CourseInfo ci = CourseInfo.create(category, "1", "001", "0000", _grade);
        assertNotNull(ci);
        assertSame(ci, _courseInfo);
    }

    public void testGetInstance() {
        final CourseInfo ci = CourseInfo.getInstance(category, "1", "001", "0000", _grade);
        assertNotNull(ci);
        assertSame(ci, _courseInfo);
    }

    public void testGetInstanceNULL() {
        final CourseInfo ci = CourseInfo.getInstance(category, "1", "001", "0000", null);
        assertNull(ci);
    }
} // Student_CourseInfoTest

// eof
