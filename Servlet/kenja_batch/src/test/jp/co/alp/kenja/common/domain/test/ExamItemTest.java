// kanji=漢字
/*
 * $Id: ExamItemTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/03/24 16:11:48 - JST
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

public class ExamItemTest extends TestCase {

    MyEnum.Category category;
    private ExamItem.Kind _kind_01;
    private ExamItem.Kind _kind_02;
    private ExamItem.Kind _kind_03;
    private ExamItem _item_0101;
    private ExamItem _item_0201;
    private ExamItem _item_0301;
    private ExamItem _item_0302;

    public ExamItemTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _kind_01 = ExamItem.Kind.create(category, "01", "中間テスト");
        _kind_02 = ExamItem.Kind.create(category, "02", "期末テスト");
        _kind_03 = ExamItem.Kind.create(category, "03", "実力テスト");
        _item_0101 = ExamItem.create(category, 1, _kind_01, "01", null, "中間テスト", false);
        _item_0201 = ExamItem.create(category, 1, _kind_02, "01", null, "期末テスト", true);
        _item_0301 = ExamItem.create(category, 1, _kind_03, "01", null, "第１回実力テスト", false);
        _item_0302 = ExamItem.create(category, 1, _kind_03, "02", null, "第２回実力テスト", true);
    }

    public void testGetKind() {
        assertSame(_kind_01, _item_0101.getKind());
        assertSame(_kind_02, _item_0201.getKind());
        assertSame(_kind_03, _item_0301.getKind());
        assertSame(_kind_03, _item_0302.getKind());
    }

    public void testGetCode() {
        assertEquals("01", _item_0101.getCode());
        assertEquals("01", _item_0201.getCode());
        assertEquals("01", _item_0301.getCode());
        assertEquals("02", _item_0302.getCode());
    }

    public void testGetName() {
        assertEquals("中間テスト", _item_0101.getName());
        assertEquals("期末テスト", _item_0201.getName());
        assertEquals("第１回実力テスト", _item_0301.getName());
        assertEquals("第２回実力テスト", _item_0302.getName());
    }

    public void testGetCountFlag() {
        assertEquals(false, _item_0101.getCountFlag());
        assertEquals(true, _item_0201.getCountFlag());
        assertEquals(false, _item_0301.getCountFlag());
        assertEquals(true, _item_0302.getCountFlag());
    }

    public void testToString() {
        assertEquals("中間テスト-01:中間テスト,集計しない", _item_0101.toString());
        assertEquals("期末テスト-01:期末テスト,集計する", _item_0201.toString());
        assertEquals("実力テスト-01:第１回実力テスト,集計しない", _item_0301.toString());
        assertEquals("実力テスト-02:第２回実力テスト,集計する", _item_0302.toString());
    }

    public void testGetInstance() {
        int semesterCode = 1;
        assertSame(_item_0101, ExamItem.getInstance(category, semesterCode , _kind_01, "01", null));
        assertSame(_item_0201, ExamItem.getInstance(category, semesterCode, _kind_02, "01", null));
        assertSame(_item_0301, ExamItem.getInstance(category, semesterCode, _kind_03, "01", null));
        assertSame(_item_0302, ExamItem.getInstance(category, semesterCode, _kind_03, "02", null));
    }

    public void testGetInstanceNULL() {
        int semesterCode = 1;
        assertNull(ExamItem.getInstance(category, semesterCode, _kind_01, "not exist code", null));
        assertNull(ExamItem.getInstance(category, semesterCode, _kind_01, "00", null));
    }

    public void testGetInstanceNULLEx() {
        try {
            int semesterCode = 1;
            ExamItem.getInstance(category, semesterCode, null, "01", null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(NullPointerException.class, e.getClass());
        }
    }

    public void testCreateEx() {
        try {
            ExamItem.create(null, 1, _kind_01, "code", null, "name", false);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            ExamItem.create(category, 1, null, "code", null, "name", false);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            ExamItem.create(category, 1, _kind_01, null, null, "name", false);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            ExamItem.create(null, 1, _kind_01, "code", null, null, false);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testCreateFOUND() {
        assertSame(_item_0101, ExamItem.create(category, 1, _kind_01, "01", null, "既存とは違う【名称】", false));
        assertSame(_item_0201, ExamItem.create(category, 1, _kind_02, "01", null, "既存とは違う【名称】", false));
        assertSame(_item_0301, ExamItem.create(category, 1, _kind_03, "01", null, "既存とは違う【名称】", false));
        assertSame(_item_0302, ExamItem.create(category, 1, _kind_03, "02", null, "既存とは違う【名称】", false));
    }

    public void testGetEnumList() {
        final List list = ExamItem.getEnumList(category);
        assertNotNull(list);
        assertEquals(4, list.size());
        assertTrue(list.contains(_item_0101));
        assertTrue(list.contains(_item_0201));
        assertTrue(list.contains(_item_0301));
        assertTrue(list.contains(_item_0302));
    }

    public void testGetEnumListEMPTY() {
        final MyEnum.Category c = new MyEnum.Category();
        final List list = ExamItem.getEnumList(c);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    public void testGetEnumMap() {
        final Map map = ExamItem.getEnumMap(category);
        assertNotNull(map);
        assertEquals(4, map.size());
        assertTrue(map.containsValue(_item_0101));
        assertTrue(map.containsValue(_item_0201));
        assertTrue(map.containsValue(_item_0301));
        assertTrue(map.containsValue(_item_0302));
    }

    public void testGetEnumMapEMPTY() {
        final MyEnum.Category c = new MyEnum.Category();
        final Map map = ExamItem.getEnumMap(c);
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    public void testSize() {
        assertEquals(4, ExamItem.size(category));
    }

    public void testSizeEMPTY() {
        final MyEnum.Category c = new MyEnum.Category();
        assertEquals(0, ExamItem.size(c));
    }

    public void testClearAll() {
        assertEquals(4, ExamItem.size(category));

        ExamItem.clearAll(category);

        assertEquals(0, ExamItem.size(category));
    }
} // ExamItemTest

// eof
