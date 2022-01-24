// kanji=漢字
/*
 * $Id: HashMappedMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/10/13 15:30:49 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import java.util.HashMap;
import java.util.Map;


/**
 * HashMapによる「MapのMap」の実装。
 * @author tamura
 * @version $Id: HashMappedMap.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class HashMappedMap<K1, K2, V> extends AbstractMappedMap<K1, K2, V> {

    /**
     * {@inheritDoc}
     */
    protected Map<K1, Map<K2, V>> createTopMap() {
        return new HashMap<K1, Map<K2, V>>();
    }

    /**
     * {@inheritDoc}
     */
    protected Map<K2, V> createNestedMap() {
        return new HashMap<K2, V>();
    }

} // HashMappedMap

// eof
