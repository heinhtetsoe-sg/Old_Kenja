// kanji=漢字
/*
 * $Id: SQLUtilsTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/12/26 18:22:44 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.test;

import java.sql.PreparedStatement;
import java.sql.Types;

import jp.co.alp.kenja.common.dao.SQLUtils;
import jp.co.alp.kenja.common.util.test.ClassUtils;

import junit.framework.TestCase;

public class SQLUtilsTest extends TestCase {

    public SQLUtilsTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#whereIn(boolean, String[])
     */
    public void testWhereInBooleanStringArray() {
        assertEquals(null, SQLUtils.whereIn(false, null));
        assertEquals(null, SQLUtils.whereIn(true,  null));

        assertEquals(null, SQLUtils.whereIn(false, new String[] {}));
        assertEquals(null, SQLUtils.whereIn(true,  new String[] {}));

        assertEquals("(null)", SQLUtils.whereIn(false, new String[] {null, }));
        assertEquals(null,     SQLUtils.whereIn(true,  new String[] {null, }));

        assertEquals("('can''t')", SQLUtils.whereIn(false, new String[] {"can't", }));
        assertEquals("('can''t')", SQLUtils.whereIn(true,  new String[] {"can't", }));

        assertEquals("('abc', 'don''t')", SQLUtils.whereIn(false, new String[] {"abc", "don't", }));
        assertEquals("('abc', 'don''t')", SQLUtils.whereIn(true,  new String[] {"abc", "don't", }));

        assertEquals("('abc', null, 'xyz')", SQLUtils.whereIn(false, new String[] {"abc", null, "xyz", }));
        assertEquals("('abc', 'xyz')",       SQLUtils.whereIn(true,  new String[] {"abc", null, "xyz", }));
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#whereIn(String, boolean, String[])
     */
    public void testWhereInStringBooleanStringArray() {
        assertEquals(null, SQLUtils.whereIn(false, "T.COL", null));
        assertEquals(null, SQLUtils.whereIn(true,  "T.COL", null));

        assertEquals(null, SQLUtils.whereIn(false, "T.COL", new String[] {}));
        assertEquals(null, SQLUtils.whereIn(true,  "T.COL", new String[] {}));

        assertEquals("T.COL in (null)", SQLUtils.whereIn(false, "T.COL", new String[] {null, }));
        assertEquals(null,              SQLUtils.whereIn(true,  "T.COL", new String[] {null, }));

        assertEquals("T.COL in ('can''t')", SQLUtils.whereIn(false, "T.COL", new String[] {"can't", }));
        assertEquals("T.COL in ('can''t')", SQLUtils.whereIn(true,  "T.COL", new String[] {"can't", }));

        assertEquals("T.COL in ('abc', 'don''t')", SQLUtils.whereIn(false, "T.COL", new String[] {"abc", "don't", }));
        assertEquals("T.COL in ('abc', 'don''t')", SQLUtils.whereIn(true,  "T.COL", new String[] {"abc", "don't", }));

        assertEquals("T.COL in ('abc', null, 'xyz')", SQLUtils.whereIn(false, "T.COL", new String[] {"abc", null, "xyz", }));
        assertEquals("T.COL in ('abc', 'xyz')",       SQLUtils.whereIn(true,  "T.COL", new String[] {"abc", null, "xyz", }));
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#whereIn(String, boolean, String[])
     */
    public void testWhereInStringBooleanStringArray_カラム名がempty() {
        try {
            SQLUtils.whereIn(false, "", new String[] {"abc"});
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("must not be empty 'column'", e.getMessage());
        }

        try {
            SQLUtils.whereIn(false, null, new String[] {"abc"});
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("must not be empty 'column'", e.getMessage());
        }
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#psWhereIn(boolean, Object[])
     */
    public void testPsWhereInBooleanObjectArray() {
        assertEquals(null, SQLUtils.psWhereIn(false, null));
        assertEquals(null, SQLUtils.psWhereIn(true,  null));

        assertEquals(null, SQLUtils.psWhereIn(false, new String[] {}));
        assertEquals(null, SQLUtils.psWhereIn(true,  new String[] {}));

        assertEquals("(?)", SQLUtils.psWhereIn(false, new String[] {null, }));
        assertEquals(null,  SQLUtils.psWhereIn(true,  new String[] {null, }));

        assertEquals("(?)", SQLUtils.psWhereIn(false, new String[] {"can't", }));
        assertEquals("(?)", SQLUtils.psWhereIn(true,  new String[] {"can't", }));

        assertEquals("(?, ?)", SQLUtils.psWhereIn(false, new String[] {"abc", "don't", }));
        assertEquals("(?, ?)", SQLUtils.psWhereIn(true,  new String[] {"abc", "don't", }));

        assertEquals("(?, ?, ?)", SQLUtils.psWhereIn(false, new String[] {"abc", null, "xyz", }));
        assertEquals("(?, ?)",    SQLUtils.psWhereIn(true,  new String[] {"abc", null, "xyz", }));
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#psWhereIn(boolean, String, Object[])
     */
    public void testPsWhereInBooleanStringObjectArray() {
        assertEquals(null, SQLUtils.psWhereIn(false, "T.COL", null));
        assertEquals(null, SQLUtils.psWhereIn(true,  "T.COL", null));

        assertEquals(null, SQLUtils.psWhereIn(false, "T.COL", new String[] {}));
        assertEquals(null, SQLUtils.psWhereIn(true,  "T.COL", new String[] {}));

        assertEquals("T.COL in (?)", SQLUtils.psWhereIn(false, "T.COL", new String[] {null, }));
        assertEquals(null,           SQLUtils.psWhereIn(true,  "T.COL", new String[] {null, }));

        assertEquals("T.COL in (?)", SQLUtils.psWhereIn(false, "T.COL", new String[] {"can't", }));
        assertEquals("T.COL in (?)", SQLUtils.psWhereIn(true,  "T.COL", new String[] {"can't", }));

        assertEquals("T.COL in (?, ?)", SQLUtils.psWhereIn(false, "T.COL", new String[] {"abc", "don't", }));
        assertEquals("T.COL in (?, ?)", SQLUtils.psWhereIn(true,  "T.COL", new String[] {"abc", "don't", }));

        assertEquals("T.COL in (?, ?, ?)", SQLUtils.psWhereIn(false, "T.COL", new String[] {"abc", null, "xyz", }));
        assertEquals("T.COL in (?, ?)",    SQLUtils.psWhereIn(true,  "T.COL", new String[] {"abc", null, "xyz", }));
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#psWhereIn(boolean, String, Object[])
     */
    public void testPsWhereInBooleanStringObjectArray_カラム名がempty() {
        try {
            SQLUtils.psWhereIn(false, "", new String[] {"abc"});
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("must not be empty 'column'", e.getMessage());
        }

        try {
            SQLUtils.psWhereIn(false, null, new String[] {"abc"});
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("must not be empty 'column'", e.getMessage());
        }
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#setPS(boolean, PreparedStatement, int, int, Object[])
     */
    public void testSetPSBooleanPSIntIntObjectArray() {
        final PreparedStatement ps = new DummyPreparedStatement();

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 1, Types.INTEGER, new Integer[] { new Integer(123), });
            assertEquals(2, next);
            assertEquals("setObject(){pos=1,obj=123,type=4}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 32, Types.DOUBLE, new Double[] { new Double(123.45), new Double(456.7), });
            assertEquals(34, next);
            assertEquals("setObject(){pos=32,obj=123.45,type=8}, setObject(){pos=33,obj=456.7,type=8}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 63, Types.REAL, new Float[] { new Float(123.45), null, new Float(456.7), });
            assertEquals(66, next);
            assertEquals("setObject(){pos=63,obj=123.45,type=7}, setObject(){pos=64,obj=null,type=7}, setObject(){pos=65,obj=456.7,type=7}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(true, ps, 74, Types.REAL, new Float[] { new Float(123.45), null, new Float(456.7), });
            assertEquals(76, next);
            assertEquals("setObject(){pos=74,obj=123.45,type=7}, setObject(){pos=75,obj=456.7,type=7}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#setPS(boolean, PreparedStatement, int, int, Object[])
     */
    public void testSetPSBooleanPSIntIntObjectArray_配列がempty() {
        final PreparedStatement ps = new DummyPreparedStatement();

        try {
            assertEquals(100, SQLUtils.setPS(false, ps, 100, Types.INTEGER, new Integer[] {}));
            assertEquals(222, SQLUtils.setPS(false, ps, 222, Types.INTEGER, null));
        } catch (final Exception e) {
            fail("例外は発生しない");
        }
    }

    /**
     * @see jp.co.alp.kenja.common.dao.SQLUtils#setPS(boolean, PreparedStatement, int, String[])
     */
    public void testSetPSBooleanPSIntStringArray() {
        final PreparedStatement ps = new DummyPreparedStatement();

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 1, new String[] {});
            assertEquals(1, next);
            assertEquals("", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 10, new String[] {null});
            assertEquals(11, next);
            assertEquals("setString(){pos=10,obj=null}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(true, ps, 11, new String[] {null});
            assertEquals(11, next);
            assertEquals("", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 2, new String[] { "abc", "xyz" });
            assertEquals(4, next);
            assertEquals("setString(){pos=2,obj=abc}, setString(){pos=3,obj=xyz}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(false, ps, 3, new String[] { "abc", null, "xyz" });
            assertEquals(6, next);
            assertEquals("setString(){pos=3,obj=abc}, setString(){pos=4,obj=null}, setString(){pos=5,obj=xyz}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }

        try {
            ps.clearParameters();
            final int next = SQLUtils.setPS(true, ps, 61, new String[] { "abc", null, "xyz" });
            assertEquals(63, next);
            assertEquals("setString(){pos=61,obj=abc}, setString(){pos=62,obj=xyz}", ps.toString());
        } catch (final Exception e) {
            fail("例外は発生しない");
        }
    }

    public void test_カバレッジ対策() throws Exception {
        final Object object = ClassUtils.newInstance(SQLUtils.class);
        assertNotNull(object);
    }
} // SQLUtilsTest

// eof
