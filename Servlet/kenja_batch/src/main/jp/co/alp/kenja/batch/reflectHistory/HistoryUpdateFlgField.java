/*
 * $Id: HistoryUpdateFlgField.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory;

import jp.co.alp.kenja.batch.reflectHistory.collection.PairStringString;

/**
 * 履歴データの更新フラグフィールド
 */
public class HistoryUpdateFlgField extends PairStringString {

    /**
     * コンストラクタ
     * @param updateFlgField 更新フラグフィールド
     * @param targetField 更新対象フィールド
     */
    public HistoryUpdateFlgField(final String updateFlgField, final String targetField) {
        super(updateFlgField, targetField);
    }

    /**
     * 更新フラグフィールドを返す
     * @return 更新フラグフィールド
     */
    public String getUpdateFlgField() {
        return getStringKey();
    }

    /**
     * 更新対象フィールドを返す
     * @return 更新対象フィールド
     */
    public String getTargetField() {
        return getStringValue();
    }
}
