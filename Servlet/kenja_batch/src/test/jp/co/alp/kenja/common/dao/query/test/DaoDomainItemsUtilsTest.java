// kanji=漢字
/*
 * $Id: DaoDomainItemsUtilsTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/05/24 13:26:27 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query.test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import jp.co.alp.kenja.common.dao.query.DaoDomainItemsUtils;
import jp.co.alp.kenja.common.domain.DomainItem;
import jp.co.alp.kenja.common.util.test.ClassUtils;

import junit.framework.TestCase;

public class DaoDomainItemsUtilsTest extends TestCase {

    private StringBuffer sql;
    private MyDomainItem _c1, _c2, _c3;
    private Collection<MyDomainItem> _coll1, _coll3;

    public DaoDomainItemsUtilsTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        sql = new StringBuffer();
        _c1 = new MyDomainItem("あ", "111");
        _c2 = new MyDomainItem("い", "022");
        _c3 = new MyDomainItem("う", "003");

        _coll1 = new LinkedList<MyDomainItem>();
        _coll1.add(_c1);

        _coll3 = new LinkedList<MyDomainItem>();
        _coll3.add(_c1);
        _coll3.add(_c2);
        _coll3.add(_c3);
    }

    protected void tearDown() throws Exception {
        try {
        } finally {
            super.tearDown();
        }
    }

    public void testSqlCondition_null() {
        DaoDomainItemsUtils.sqlCondition("where", "FIELDX", sql, null);
        assertEquals("", sql.toString());
    }

    public void testSqlCondition_空() {
        DaoDomainItemsUtils.sqlCondition("where", "FIELD0", sql, Collections.EMPTY_LIST);
        assertEquals("", sql.toString());
    }

    public void testSqlCondition_1() {
        DaoDomainItemsUtils.sqlCondition("where", "FIELD1", sql, _coll1);
        assertEquals("where FIELD1 = '111'", sql.toString());
    }

    public void testSqlCondition_3() {
        DaoDomainItemsUtils.sqlCondition("where", "FIELD3", sql, _coll3);
        assertEquals("where FIELD3 in ('111','022','003')", sql.toString());
    }

    public void test_カバレッジ対策() throws Exception {
        final Object obj = ClassUtils.newInstance(DaoDomainItemsUtils.class);
        assertNotNull(obj);
    }

    public void test_カバレッジ対策_MyDomainItem() {
        assertEquals("あ", _c1.getUniqueName());
        assertEquals("い", _c2.getUniqueName());
        assertEquals("う", _c3.getUniqueName());
    }

    //========================================================================

    private static class MyDomainItem implements DomainItem {
        private final String _name;
        private final String _code;

        public MyDomainItem(final String name, final String code) {
            super();
            _name = name;
            _code = code;
        }

        public String getUniqueName() { return _name; }

        public String getCode() { return _code; }
    } // MyDomainItem
} // DaoDomainItemsUtilsTest

// eof
