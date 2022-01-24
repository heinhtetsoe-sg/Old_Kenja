// kanji=漢字
/*
 * $Id: AbstractMappedMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/10/13 14:05:03 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * 「MapのMap」の部分実装。
 * @author tamura
 * @version $Id: AbstractMappedMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class AbstractMappedMap<K1, K2, V> implements MappedMap<K1, K2, V> {
    private final Map<K1, Map<K2, V>> _topMap;

    /**
     * コンストラクタ。
     */
    protected AbstractMappedMap() {
       super();
        _topMap = createTopMap();
    }

    /**
     * 外側のMapを作成する。
     * @return Map
     */
    protected abstract Map<K1, Map<K2, V>> createTopMap();

    /**
     * 内側のMapを作成する。
     * @return Map
     */
    protected abstract Map<K2, V> createNestedMap();

    /*
     *
     */
    private Map<K2, V> getNestedMap(final boolean createMap, final K1 key1) {
        Map<K2, V> nested = _topMap.get(key1);
        if (null == nested && createMap) {
            nested = createNestedMap();
            _topMap.put(key1, nested);
        }
        return nested;
    }

    /**
     * {@inheritDoc}
     */
    public Map<K2, V> getNestedMap(final K1 key1) {
        return getNestedMap(false, key1);
    }

    /**
     * {@inheritDoc}
     */
    public int size(final K1 key1) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return 0;
        }
        return nested.size();
    }

    /**
     * {@inheritDoc}
     */
    public int sizeAll() {
        int size = 0;
        for (final Iterator<Map<K2, V>> it = _topMap.values().iterator(); it.hasNext();) {
            final Map<K2, V> o = it.next();
            if (o instanceof Map) {
                size += o.size();
            } else {
                size++;
            }
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty(final K1 key1) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return true;
        }
        return nested.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final K1 key1, final K2 key2) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return false;
        }
        return nested.containsKey(key2);
    }

    /**
     * {@inheritDoc}
     */
    public V get(final K1 key1, final K2 key2) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return null;
        }
        return nested.get(key2);
    }

    /**
     * {@inheritDoc}
     */
    public V put(final K1 key1, final K2 key2, final V value) {
        final Map<K2, V> nested = getNestedMap(true, key1);
        return nested.put(key2, value);
    }

    /**
     * {@inheritDoc}
     */
    public V remove(final K1 key1, final K2 key2) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return null;
        }
        return nested.remove(key2);
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(final K1 key1, final Map<K2, V> t) {
        final Map<K2, V> nested = getNestedMap(true, key1);
        nested.putAll(t);
    }

    /**
     * {@inheritDoc}
     */
    public void clear(final K1 key1) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return;
        }
        nested.clear();
    }

    /**
     * {@inheritDoc}
     */
    public Set<K2> keySet(final K1 key1) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return Collections.emptySet();
        }
        return nested.keySet();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<V> values(final K1 key1) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return Collections.emptyList();
        }
        return nested.values();
    }

    /**
     * {@inheritDoc}
     */
    public Set<Map.Entry<K2, V>> entrySet(final K1 key1) {
        final Map<K2, V> nested = getNestedMap(key1);
        if (null == nested) {
            return Collections.emptySet();
        }
        return nested.entrySet();
    }


    //========================================================================

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        return _topMap.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return _topMap.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _topMap.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected void finalize() throws Throwable {
        try {
            // 内側のMapをそれぞれクリア
            for (final K1 k : _topMap.keySet()) {
                final Map<K2, V> o = _topMap.get(k);
                if (o instanceof Map) {
                    final Map<K2, V> nested = o;
                    nested.clear();
                }
            }

            // 外側のMapをクリア
            _topMap.clear();
        } finally {
            super.finalize();
        }
    }

    //========================================================================

    /**
     * {@inheritDoc}
     */
    public int size() {
        return _topMap.size();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return 0 == size();
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final Object key) {
        return _topMap.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(final Object value) {
        return _topMap.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public Object get(final Object key) {
        return _topMap.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public Object remove(final K1 key) {
        return _topMap.remove(key);
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        _topMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    public Set<K1> keySet() {
        return _topMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Map<K2, V>> values() {
        return _topMap.values();
    }

    /**
     * {@inheritDoc}
     */
    public Set<Map.Entry<K1, Map<K2, V>>> entrySet() {
        return _topMap.entrySet();
    }
} // AbstractMappedMap

// eof
