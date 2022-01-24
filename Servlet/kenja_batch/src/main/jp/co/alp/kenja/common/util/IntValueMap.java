// kanji=漢字
/*
 * $Id: IntValueMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/04 20:36:41 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 値としてプリミティブintを扱うマップ。
 * @author tamura
 * @version $Id: IntValueMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class IntValueMap implements Map {
    private final Map _map = new HashMap();

    /**
     * コンストラクタ。
     */
    public IntValueMap() {
        super();
    }

    /**
     * 値を1増やす。
     * @param key キー
     * @param defaultValue キーがなかった場合のデフォルト値
     * @return 増やしたあとの値
     */
    public synchronized int increment(final Object key, final int defaultValue) {
        final Object old = _map.get(key);
        if (null == old) {
            final int[] arr = new int[1];
            arr[0] = defaultValue;
            _map.put(key, arr);
            return arr[0];
        } else {
            final int[] arr = (int[]) old;
            arr[0]++;
            return arr[0];
        }
    }

    /**
     * put。
     * @param key キー
     * @param value 値
     */
    public void put(final Object key, final int value) {
        final Object old = _map.get(key);
        int[] arr;
        if (null == old) {
            arr = new int[1];
            _map.put(key, arr);
        } else {
            arr = (int[]) old;
        }

        arr[0] = value;
    }

    /**
     * get。
     * @param key キー
     * @param defaultValue 未登録時の戻り値
     * @return 値
     */
    public int get(final Object key, final int defaultValue) {
        final Object old = _map.get(key);
        if (null == old) {
            return defaultValue;
        }

        final int[] arr = (int[]) old;
        return arr[0];
    }

    /**
     * {@inheritDoc}
     */
    public int size() { return _map.size(); }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() { return _map.isEmpty(); }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final Object key) { return _map.containsKey(key); }

    /**
     * {@inheritDoc}
     */
    public Object remove(final Object key) { return _map.remove(key); }

    /**
     * {@inheritDoc}
     */
    public void clear() { _map.clear(); }

    /**
     * {@inheritDoc}
     */
    public Set keySet() { return _map.keySet(); }

    /**
     * {@inheritDoc}
     */
    public Set entrySet() { return _map.entrySet(); }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException
     * @deprecated
     */
    public void putAll(final Map t) { throw new UnsupportedOperationException(); }


    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException
     * @deprecated
     */
    public boolean containsValue(final Object value) { throw new UnsupportedOperationException(); }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException
     * @deprecated
     */
    public Object put(final Object key, final Object value) { throw new UnsupportedOperationException(); }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException
     * @deprecated
     */
    public Collection values() { throw new UnsupportedOperationException(); }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException
     * @deprecated
     */
    public Object get(final Object key) { throw new UnsupportedOperationException(); }
} // IntValueMap

// eof
