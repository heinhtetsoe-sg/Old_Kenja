// kanji=漢字
/*
 * $Id: MyCaseInsensitiveHashMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2012/07/06 17:30:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004,2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * org.apache.commons.dbutils.BasicRowProcessor.CaseInsensitiveHashMap
 */
public class MyCaseInsensitiveHashMap extends HashMap<String, Object> {

    public MyCaseInsensitiveHashMap() {
        super();
    }

    public MyCaseInsensitiveHashMap(Map<String, Object> map) {
        this();
        putAll(map);
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return super.containsKey(key.toString().toLowerCase());
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return super.get(key.toString().toLowerCase());
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(String key, Object value) {
        return super.put(key.toString().toLowerCase(), value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends Object> m) {
        Iterator<? extends String> iter = m.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (key instanceof String) {
                key = ((String) key).toLowerCase();
            }
            Object value = m.get(key);
            this.put(key.toString(), value);
        }
    }

    /**
     * @see java.util.Map#remove(java.lang.ObjecT)
     */
    public Object remove(Object key) {
        return super.remove(key.toString().toLowerCase());
    }
} // MyCaseInsensitiveHashMap

