// kanji=漢字
/*
 * $Id: IntValueMapTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/11/04 21:03:15 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util.test;

import java.util.Set;

import jp.co.alp.kenja.common.util.IntValueMap;

import junit.framework.TestCase;

public class IntValueMapTest extends TestCase {
    private IntValueMap _intMap;

    public IntValueMapTest(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        _intMap = new IntValueMap();
        _intMap.put("one", 1);
        _intMap.put("three", 3);
        _intMap.put("five", 5);
        _intMap.put("seven", 7);
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        _intMap.clear();
        _intMap = null;
    }

    public void testIntValueMap() {
        final IntValueMap map = new IntValueMap();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    public void testIncrement() {
        assertEquals(1, _intMap.get("one", -1));
        assertEquals(3, _intMap.get("three", -1));
        assertEquals(5, _intMap.get("five", -1));
        assertEquals(7, _intMap.get("seven", -1));

        //

        assertEquals(2, _intMap.increment("one", -1));
        assertEquals(4, _intMap.increment("three", -1));
        assertEquals(6, _intMap.increment("five", -1));
        assertEquals(8, _intMap.increment("seven", -1));

        assertEquals(2, _intMap.get("one", -1));
        assertEquals(4, _intMap.get("three", -1));
        assertEquals(6, _intMap.get("five", -1));
        assertEquals(8, _intMap.get("seven", -1));

        //

        assertEquals(3, _intMap.increment("one", -1));
        assertEquals(5, _intMap.increment("three", -1));
        assertEquals(7, _intMap.increment("five", -1));
        assertEquals(9, _intMap.increment("seven", -1));

        assertEquals(3, _intMap.get("one", -1));
        assertEquals(5, _intMap.get("three", -1));
        assertEquals(7, _intMap.get("five", -1));
        assertEquals(9, _intMap.get("seven", -1));
    }

    public void testIncrementNotFound() {
        final String key = "unknown";
        assertEquals("未登録なので-1が返すはず", -1, _intMap.increment(key, -1));
        assertTrue("キーは登録されるはず", _intMap.containsKey(key));
        assertEquals("値は-1のはず", -1, _intMap.get(key, 999));
        assertEquals("1増えてゼロを返すはず", 0, _intMap.increment(key, 999));
    }

    public void testPut() {
        assertEquals(4, _intMap.size());

        _intMap.put("eleven", 11);
        assertEquals(11, _intMap.get("eleven", -1));

        assertEquals(5, _intMap.size());

        _intMap.put("eleven", 111);
        assertEquals(111, _intMap.get("eleven", -1));

        assertEquals(5, _intMap.size());
    }

    public void testGet() {
        assertEquals(1, _intMap.get("one", -1));
        assertEquals(3, _intMap.get("three", -1));
        assertEquals(5, _intMap.get("five", -1));
        assertEquals(7, _intMap.get("seven", -1));

        assertEquals(-1, _intMap.get("unknown", -1));
    }

    public void testGetNotFound() {
        final String key = "unknown";
        assertEquals(-1, _intMap.get(key, -1));
        assertFalse("キーは登録されていないはず", _intMap.containsKey(key));
    }

    public void testSize() {
        assertEquals(4, _intMap.size());
    }

    public void testIsEmpty() {
        assertFalse(_intMap.isEmpty());
    }

    public void testContainsKey() {
        assertTrue(_intMap.containsKey("one"));
        assertTrue(_intMap.containsKey("three"));
        assertTrue(_intMap.containsKey("five"));
        assertTrue(_intMap.containsKey("seven"));

        assertFalse(_intMap.containsKey("unknown"));
    }

    public void testRemove() {
        assertEquals(4, _intMap.size());
        final Object five = _intMap.remove("five");
        assertEquals(3, _intMap.size());

        assertSame(int[].class, five.getClass());
        final int[] arr = (int []) five;
        assertEquals(5, arr[0]);
        assertEquals(1, arr.length);


        final Object unk = _intMap.remove("unknown");
        assertEquals(3, _intMap.size());
        assertNull(unk);
    }

    public void testClear() {
        assertEquals(4, _intMap.size());
        _intMap.clear();
        assertEquals(0, _intMap.size());
    }

    public void testKeySet() {
        final Set<String> keys = _intMap.keySet();
        assertNotNull(keys);
        assertEquals(4, keys.size());

        assertTrue(keys.contains("one"));
        assertTrue(keys.contains("three"));
        assertTrue(keys.contains("five"));
        assertTrue(keys.contains("seven"));
    }

    public void testEntrySet() {
        final Set<Integer> entries = _intMap.entrySet();
        assertNotNull(entries);
        assertEquals(4, entries.size());
    }

    /** @deprecated */
    public void testDepPutAll() {
        try {
            _intMap.putAll(null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }
    }

    /** @deprecated */
    public void testDepContainsValue() {
        try {
            _intMap.containsValue("");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }
    }

    /** @deprecated */
    public void testDepPut() {
        try {
            _intMap.put("", "");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }
    }

    /** @deprecated */
    public void testDepValues() {
        try {
            _intMap.values();
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }
    }

    /** @deprecated */
    public void testDepGet() {
        try {
            _intMap.get("");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(UnsupportedOperationException.class, e.getClass());
        }
    }
} // IntValueMapTest

// eof
