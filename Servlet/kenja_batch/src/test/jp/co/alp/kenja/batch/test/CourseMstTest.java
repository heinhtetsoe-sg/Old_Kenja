// kanji=漢字
/*
 * $Id: CourseMstTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2007/01/07 17:42:53 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import jp.co.alp.kenja.batch.domain.CourseMst;

import junit.framework.TestCase;

public class CourseMstTest extends TestCase {
    private MyEnum.Category category;
    private CourseMst _courseMst;

    private Period s;
    private Period e;

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        s = Period.create(category, "2", "2校時", "2校時", null, null);
        e = Period.create(category, "4", "4校時", "4校時", null, null);
        _courseMst = CourseMst.create(category, "1", "全日制", "全日", s, e);
    }

    public void testGetCode() throws Exception {
        assertEquals("1", _courseMst.getCode());
    }

    public void testGetName() throws Exception {
        assertEquals("全日制", _courseMst.getName());
    }

    public void testGetXPeriod() throws Exception {
        assertEquals(s, _courseMst.getSPeriod());
        assertEquals(e, _courseMst.getEPeriod());
    }

    public void testIsActive() throws Exception {
        try {
            assertTrue(_courseMst.isActive(null));
            fail("例外が発生するので、ここには来ない");            
        } catch (final Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

        Period p;

        p = Period.create(category, "0", "0校時", "0校時", null, null);
        assertFalse(_courseMst.isActive(p));

        p = Period.create(category, "1", "1校時", "1校時", null, null);
        assertFalse(_courseMst.isActive(p));

        p = Period.create(category, "2", "2校時", "2校時", null, null);
        assertTrue(_courseMst.isActive(p)); // true

        p = Period.create(category, "3", "3校時", "3校時", null, null);
        assertTrue(_courseMst.isActive(p)); // true

        p = Period.create(category, "5", "5校時", "5校時", null, null);
        assertFalse(_courseMst.isActive(p));
    }

    public void testToString() throws Exception {
        assertEquals("1:全日制[開始校時=2校時, 終了校時=4校時]", _courseMst.toString());
    }
} // CourseMstTest

// eof
