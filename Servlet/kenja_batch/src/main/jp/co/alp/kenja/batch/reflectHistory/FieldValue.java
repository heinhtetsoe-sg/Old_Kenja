/*
 * $Id: FieldValue.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory;

import jp.co.alp.kenja.batch.reflectHistory.collection.PairStringString;

/**
 * フィールドと値のペア
 */
public class FieldValue extends PairStringString {

    /**
     * 値が文字列ではないか
     */
    private boolean _valueIsNotString;

    /**
     * コンストラクタ
     * @param field フィールド
     * @param value 値
     * @param valueIsNotString 値が文字列ではないか
     */
    public FieldValue(final String field, final String value, final boolean valueIsNotString) {
        super(field, value);
        _valueIsNotString = valueIsNotString;
    }

    /**
     * コンストラクタ
     * @param field フィールド
     * @param value 値
     */
    public FieldValue(final String field, final String value) {
        this(field, value, false);
    }

    /**
     * フィールドを返す
     * @return フィールド
     */
    public String getField() {
        return getStringKey();
    }

    /**
     * 値を返す
     * @return 値
     */
    public String getValue() {
        return getStringValue();
    }

    /**
     * 値が文字列ではないか
     * @return 値が文字列でないなら<code>true</code>、それ以外は<code>false</code>を返す
     */
    public boolean valueIsNotString() {
        return _valueIsNotString;
    }
}
