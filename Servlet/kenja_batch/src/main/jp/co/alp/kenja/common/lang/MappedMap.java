// kanji=漢字
/*
 * $Id: MappedMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/10/13 14:48:36 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * MapのMap。
 * @author tamura
 * @version $Id: MappedMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface MappedMap<K1, K2, V> {
    /**
     * 外側のkey1から、内側のMapを得る。
     * @param key1 外側のkey
     * @return 内側のMap
     */
    Map<K2, V> getNestedMap(final K1 key1);

    /**
     * 内側のMapの要素数を得る。
     * @param key1 外側のkey
     * @return 要素の数
     */
    int size(final K1 key1);

    /**
     * すべての内側のMapの要素数を得る。
     * @return 要素の数
     */
    int sizeAll();

    /**
     * 内側のMapが空か判定する。
     * @param key1 外側のkey
     * @return 空なら<code>true</code>
     */
    boolean isEmpty(final K1 key1);

    /**
     * 内側のMapにkeyが含まれるか判定する。
     * @param key1 外側のkey
     * @param key2 内側のkey
     * @return 含まれるなら<code>true</code>
     */
    boolean containsKey(final K1 key1, final K2 key2);

    /**
     * 内側のMapから値を得る。
     * @param key1 外側のkey
     * @param key2 内側のkey
     * @return 値
     */
    V get(final K1 key1, final K2 key2);

    /**
     * 内側のMapに値を関連付ける。
     * @param key1 外側のkey
     * @param key2 内側のkey
     * @param value 値
     * @return 指定されたkey1,key2に関連した以前の値。
     */
    V put(final K1 key1, final K2 key2, final V value);

    /**
     * 内側のMapから削除する。
     * @param key1 外側のkey
     * @param key2 内側のkey
     * @return 指定されたキーに関連した以前の値。
     */
    V remove(final K1 key1, final K2 key2);

    /**
     * 指定されたマップのすべてのマッピングをこの内側のMapにコピーします。
     * @param key1 外側のkey
     * @param t コピーするMap
     */
    void putAll(final K1 key1, final Map<K2, V> t);

    /**
     * 内側のMapをクリアする。
     * @param key1 外側のkey
     */
    void clear(final K1 key1);

    /**
     * 内側のMapのキー集合を得る。
     * @param key1 外側のkey
     * @return キー集合
     */
    Set<K2> keySet(final K1 key1);

    /**
     * 内側のMapの値コレクションを得る。
     * @param key1 外側のkey
     * @return 値コレクション。
     */
    Collection<V> values(final K1 key1);

    /**
     * 内側のMapのエントリーセットのビューを得る。
     * @param key1 外側のkey
     * @return エントリーセットのビュー
     */
    Set<Map.Entry<K2, V>> entrySet(final K1 key1);
} // MappedMap

// eof
