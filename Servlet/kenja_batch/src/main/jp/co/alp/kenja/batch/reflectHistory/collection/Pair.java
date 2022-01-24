/*
 * $Id: Pair.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory.collection;

/**
 * キーと値のペア。不変。
 * @author maesiro
 */
public abstract class Pair {

    private final Object _key;
    private final Object _value;

    public Pair(final Object key, final Object value) {
        _key = key;
        _value = value;
    }

    /**
     * キーを返す
     * @return キー
     */
    public Object getPairKey() {
        return _key;
    }

    /**
     * 値を返す
     * @return 値
     */
    public Object getPairValue() {
        return _value;
    }
}
