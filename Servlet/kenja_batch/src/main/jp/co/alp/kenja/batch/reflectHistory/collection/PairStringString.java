/*
 * $Id: PairStringString.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory.collection;

/**
 * 文字列のキーと値のペア
 * @author maesiro
 */
public abstract class PairStringString extends Pair {

    public PairStringString(final String key, final String value) {
        super(key, value);
    }

    /**
     * 文字列のキーを得る
     * @return 文字列のキー
     */
    protected String getStringKey() {
        return (String) getPairKey();
    }

    /**
     * 文字列の値を得る
     * @return 文字列の値
     */
    protected String getStringValue() {
        return (String) getPairValue();
    }
}
