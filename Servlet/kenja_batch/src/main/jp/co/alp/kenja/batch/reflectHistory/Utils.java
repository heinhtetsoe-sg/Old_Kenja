/*
 * $Id: Utils.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.reflectHistory.dao.QueryUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    private Utils() {}

    /**
     * 履歴データの更新フィールドと値のペアのリストを得る
     * @param tablename テーブル名
     * @param historyUpdateFlgFieldList 履歴データの更新フラグフィールドのリスト
     * @param assoc
     * @param tgtAssoc 更新対象テーブルのデータ
     * @return 履歴データの更新フィールドと値のペアのリスト
     */
    public static final Collection<FieldValue> getUpdateFieldValueList(final String tablename, final List<HistoryUpdateFlgField> historyUpdateFlgFieldList, final Map<String, String> assoc, final Map<String, String> tgtAssoc) {
        final List<FieldValue> targetFieldList = new ArrayList<FieldValue>();
        for (final HistoryUpdateFlgField historyUpdateFlgField : historyUpdateFlgFieldList) {
            final String updateFlgField = historyUpdateFlgField.getUpdateFlgField();
            final String updateFlg = QueryUtils.getString(assoc, updateFlgField);
            if (null == updateFlg) {
                continue;
            }
            final String targetField = historyUpdateFlgField.getTargetField();
            final String updateValue = QueryUtils.getString(assoc, targetField);
            String currentTargetValue = null;
            if (null != tgtAssoc) {
                currentTargetValue = QueryUtils.getString(tgtAssoc, targetField);
            }
            if (null == currentTargetValue && null == updateValue || null != updateValue && updateValue.equals(currentTargetValue)) {
                // 更新対象と内容が同一のため更新しない
                log.debug(" 更新対象外: " + tablename + "." + targetField + " = " + updateValue + " / SCHREGNO = " + QueryUtils.getString(assoc, "SCHREGNO"));
                continue;
            }
            targetFieldList.add(new FieldValue(targetField, updateValue));
        }
        return targetFieldList;
    }

}
