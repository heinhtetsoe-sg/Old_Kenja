// kanji=漢字
/*
 * $Id: DaoChairsUtilsTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/05/09 15:46:09 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query.test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import jp.co.alp.kenja.common.dao.query.DaoChairsUtils;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChairsHolder;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.test.ClassUtils;

import junit.framework.TestCase;

public class DaoChairsUtilsTest extends TestCase {

    MyEnum.Category category;
    private SubClass _s1;
    private Chair _c1, _c2, _c3;
    private Collection<Chair> _coll1, _coll3;
    private ChairsHolder _ch1, _ch3;

    public DaoChairsUtilsTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _s1 = SubClass.create(category, "01", "H", "2", "010000", "国語I", "国I");

        _c1 = Chair.create(category, "0002582", GroupClass.ZERO, _s1, "情Ａ 106", new Integer(4), new Integer(1), true);
        _c2 = Chair.create(category, "0002600", GroupClass.ZERO, _s1, "生一 204", new Integer(3), new Integer(2), true);
        _c3 = Chair.create(category, "0004000", GroupClass.ZERO, _s1, "LHR 309", new Integer(2), new Integer(3), true);

        _coll1 = new LinkedList<Chair>();
        _coll1.add(_c1);

        _coll3 = new LinkedList<Chair>();
        _coll3.add(_c1);
        _coll3.add(_c2);
        _coll3.add(_c3);

        _ch1 = new MyChairsHolder(_coll1);
        _ch3 = new MyChairsHolder(_coll3);
    }

    protected void tearDown() throws Exception {
        try {
        } finally {
            super.tearDown();
        }
    }

    /*
     * 'jp.co.alp.kenja.common.dao.query.DaoChairsUtils.sqlChairCondition(String, StringBuffer, Collection)' のためのテスト・メソッド
     */
    public void testSqlChairCondition_Collection版_null() {
        final StringBuffer sql = new StringBuffer();
        DaoChairsUtils.sqlChairCondition("where", "CHAIRCDX", sql, (Collection) null);

        assertEquals("", sql.toString());
    }

    public void testSqlChairCondition_Collection版_空() {
        final StringBuffer sql = new StringBuffer();
        DaoChairsUtils.sqlChairCondition("where", "CHAIRCD0", sql, Collections.EMPTY_LIST);

        assertEquals("", sql.toString());
    }

    public void testSqlChairCondition_Collection版_1() {
        final StringBuffer sql = new StringBuffer();
        DaoChairsUtils.sqlChairCondition("where", "CHAIRCD1", sql, _coll1);

        assertEquals("where CHAIRCD1 = '0002582'", sql.toString());
    }

    public void testSqlChairCondition_Collection版_3() {
        final StringBuffer sql = new StringBuffer();
        DaoChairsUtils.sqlChairCondition("where", "CHAIRCD3", sql, _coll3);

        assertEquals("where CHAIRCD3 in ('0002582','0002600','0004000')", sql.toString());
    }

    public void test_カバレッジ対策() throws Exception {
        final Object obj = ClassUtils.newInstance(DaoChairsUtils.class);
        assertNotNull(obj);
    }

    //========================================================================

    private static class MyChairsHolder implements ChairsHolder {
        private final Collection<Chair> _coll;

        /*pkg*/MyChairsHolder(final Collection<Chair> coll) {
            super();
            _coll = Collections.unmodifiableCollection(coll);
        }

        public Collection<Chair> getChairs() {
            return _coll;
        }        
    } // MyChairsHolder

    //========================================================================

    private static class MyChairsHolderNULL implements ChairsHolder {
        /*pkg*/MyChairsHolderNULL() {
            super();
        }

        public Collection<Chair> getChairs() {
            return null;
        }        
    } // MyChairsHolderNULL
} // DaoChairsUtilsTest

// eof
