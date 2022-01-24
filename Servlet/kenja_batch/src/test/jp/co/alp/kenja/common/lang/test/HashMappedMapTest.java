// kanji=漢字
/*
 * $Id: HashMappedMapTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/10/13 15:38:29 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang.test;

import java.util.HashMap;
import java.util.Map;

import jp.co.alp.kenja.common.lang.HashMappedMap;

import junit.framework.TestCase;

public class HashMappedMapTest extends TestCase {
    HashMappedMap map;

    public HashMappedMapTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        map = new HashMappedMap();

        map.put("greeting", "before-noon", "good morning");
        map.put("greeting", "afternoon", "good afternoon");
        map.put("greeting", "day time", "hello!");
        map.put("greeting", "twilight", "good evening!");
        map.put("あいさつ", "午前中", "おはよう");
        map.put("あいさつ", "日中", "こんにちわ");
        map.put("あいさつ", "夕方", "こんばんわ");
    }

    protected void tearDown() throws Exception {
        if (null != map) {
            map.clear();
        }
        map = null;
    }

    public void testDummy() {
        map.hashCode();
    }

    public void testFinalize() throws Throwable {
        new HashMappedMap() {
            public Map createTopMap() {
                Map m2 = new HashMap();
                m2.put("key1-1", "val1-1");
                Map m1 = new HashMap();
                m1.put("key1", m2);
                return m1;
            }
            public void finalize() throws Throwable {
                super.finalize();
            }
        }.finalize();
    }

    public void test1() {
        map.clear();

        assertTrue(map.isEmpty("greeting"));
        assertTrue(map.isEmpty());
        assertEquals(0, map.size("greeting"));
        assertEquals(0, map.size());

        map.put("greeting", "before-noon", "good morning");
        map.put("greeting", "afternoon", "good afternoon");
        map.put("greeting", "day time", "hello!");
        map.put("greeting", "twilight", "good evening!");
        map.put("あいさつ", "午前中", "おはよう");
        map.put("あいさつ", "日中", "こんにちわ");
        map.put("あいさつ", "夕方", "こんばんわ");
        assertFalse(map.isEmpty("greeting"));
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertEquals(4, map.size("greeting"));
        assertEquals(3, map.size("あいさつ"));
        assertEquals(7, map.sizeAll());
        assertEquals("hello!", map.get("greeting", "day time"));
        assertEquals("good evening!", map.get("greeting", "twilight"));

        map.clear("greeting");
        assertTrue(map.isEmpty("greeting"));
        assertFalse(map.isEmpty());

        map.clear();
        assertTrue(map.isEmpty());
    }

    public void testEquals() {
        assertTrue(map.equals(map));
        assertFalse(map.equals(null));
    }

    public void testToString() {
        assertTrue(0 <= map.toString().indexOf("あいさつ={"));
        assertTrue(0 <= map.toString().indexOf("日中=こんにちわ"));
        assertTrue(0 <= map.toString().indexOf("午前中=おはよう"));
        assertTrue(0 <= map.toString().indexOf("夕方=こんばんわ"));
        assertTrue(0 <= map.toString().indexOf("greeting={"));
        assertTrue(0 <= map.toString().indexOf("afternoon=good afternoon"));
        assertTrue(0 <= map.toString().indexOf("twilight=good evening!"));
        assertTrue(0 <= map.toString().indexOf("before-noon=good morning"));
        assertTrue(0 <= map.toString().indexOf("day time=hello!"));
    }

    public void testContainsKey() {
        assertTrue(map.containsKey("greeting"));
        assertFalse(map.containsKey("hoge"));
    }

    public void testContainsKey2() {
        assertTrue(map.containsKey("greeting", "before-noon"));
        assertFalse(map.containsKey("hoge", "hoge"));
    }

    public void testContainsValue() {
        Object obj = map.get("greeting");
        assertTrue(map.containsValue(obj));
        assertFalse(map.containsValue("hoge"));
    }

    public void testRemove2() {
        assertEquals(4, map.size("greeting"));
        map.remove("greeting", "before-noon");
        assertEquals(3, map.size("greeting"));
        map.remove("hoge", "before-noon");
        assertEquals(3, map.size("greeting"));
    }

    public void testGet2() {
        assertNull(map.get("hoge", "hoge"));
    }

    public void testPutAll2() {
        final Map hoge = new HashMap();
        hoge.put("hoge-key1", "hoge=v1");
        hoge.put("hoge-key2", "hoge=v2");
        hoge.put("hoge-key3", "hoge=v3");

        assertEquals(7, map.sizeAll());
        map.putAll("hoge", hoge);
        assertEquals(10, map.sizeAll());
    }

    public void testClear2() {
        assertEquals(7, map.sizeAll());
        map.clear("hoge");
        assertEquals(7, map.sizeAll());
        map.clear("greeting");
        assertEquals(3, map.sizeAll());
    }

    public void testKeySet() {
        assertEquals(2, map.keySet().size());
        map.clear();
        assertEquals(0, map.keySet().size());
    }

    public void testKeySet2() {
        assertTrue(map.keySet("hoge").isEmpty());
        assertEquals(4, map.keySet("greeting").size());
    }

    public void testKeyValues() {
        assertEquals(2, map.values().size());
        map.clear();
        assertEquals(0, map.values().size());
    }

    public void testKeyValues2() {
        assertTrue(map.values("hoge").isEmpty());
        assertEquals(4, map.values("greeting").size());
    }

    public void testKeyEntrySet() {
        assertEquals(2, map.entrySet().size());
        map.clear();
        assertEquals(0, map.entrySet().size());
    }

    public void testKeyEntrySet2() {
        assertTrue(map.entrySet("hoge").isEmpty());
        assertEquals(4, map.entrySet("greeting").size());
    }
} // HashMappedMapTest

// eof
