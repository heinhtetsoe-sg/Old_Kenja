// kanji=漢字
/*
 * $Id: ExamItem_KindTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/25 17:29:12 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.domain.ExamItem;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class ExamItem_KindTest extends TestCase {

    MyEnum.Category category;
    private ExamItem.Kind _kind_01;
    private ExamItem.Kind _kind_02;
    private ExamItem.Kind _kind_03;

    public ExamItem_KindTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _kind_01 = ExamItem.Kind.create(category, "01", "中間テスト");
        _kind_02 = ExamItem.Kind.create(category, "02", "期末テスト");
        _kind_03 = ExamItem.Kind.create(category, "03", "実力テスト");
    }

    public void testToString() {
        assertEquals("中間テスト", _kind_01.toString());
        assertEquals("期末テスト", _kind_02.toString());
        assertEquals("実力テスト", _kind_03.toString());
    }

    public void testGetCode() {
        assertEquals("01", _kind_01.getCode());
        assertEquals("02", _kind_02.getCode());
        assertEquals("03", _kind_03.getCode());
    }

    public void testGetName() {
        assertEquals("中間テスト", _kind_01.getName());
        assertEquals("期末テスト", _kind_02.getName());
        assertEquals("実力テスト", _kind_03.getName());
    }

    public void testIsFixed() {
        assertEquals(true, _kind_01.isFixed());
        assertEquals(true, _kind_02.isFixed());
        assertEquals(false, _kind_03.isFixed());
    }

    public void testCreateEx() {
        try {
            ExamItem.Kind.create(null, "code", "name");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            ExamItem.Kind.create(category, null, "name");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            ExamItem.Kind.create(category, "code", null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testCreateFOUND() {
        assertSame(_kind_01, ExamItem.Kind.create(category, "01", "既存とは違う【名称】"));
        assertSame(_kind_02, ExamItem.Kind.create(category, "02", "既存とは違う【名称】"));
        assertSame(_kind_03, ExamItem.Kind.create(category, "03", "既存とは違う【名称】"));
    }

    public void testGetInstance() {
        assertSame(_kind_01, ExamItem.Kind.getInstance(category, "01"));
        assertSame(_kind_02, ExamItem.Kind.getInstance(category, "02"));
        assertSame(_kind_03, ExamItem.Kind.getInstance(category, "03"));
    }

    public void testGetInstanceNULL() {
        assertNull(ExamItem.Kind.getInstance(category, "not exist code"));
        assertNull(ExamItem.Kind.getInstance(category, "00"));
        assertNull(ExamItem.Kind.getInstance(category, null));
    }

    public void testGetEnumList() {
        final List list = ExamItem.Kind.getEnumList(category);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertTrue(list.contains(_kind_01));
        assertTrue(list.contains(_kind_02));
        assertTrue(list.contains(_kind_03));
    }

    public void testGetEnumListEMPTY() {
        final MyEnum.Category c = new MyEnum.Category();
        final List list = ExamItem.Kind.getEnumList(c);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    public void testGetEnumMap() {
        final Map map = ExamItem.Kind.getEnumMap(category);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(_kind_01));
        assertTrue(map.containsValue(_kind_02));
        assertTrue(map.containsValue(_kind_03));
    }

    public void testGetEnumMapEMPTY() {
        final MyEnum.Category c = new MyEnum.Category();
        final Map map = ExamItem.Kind.getEnumMap(c);
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    public void testSize() {
        assertEquals(3, ExamItem.Kind.size(category));
    }

    public void testSizeEMPTY() {
        final MyEnum.Category c = new MyEnum.Category();
        assertEquals(0, ExamItem.Kind.size(c));
    }

    public void testClearAll() {
        assertEquals(3, ExamItem.Kind.size(category));

        ExamItem.Kind.clearAll(category);

        assertEquals(0, ExamItem.Kind.size(category));
    }
} // ExamItem_KindTest

// eof
