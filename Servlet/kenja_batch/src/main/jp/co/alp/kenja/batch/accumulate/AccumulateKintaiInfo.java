// kanji=漢字
/*
 * $Id: AccumulateKintaiInfo.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/21 10:39:22 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.HashMap;
import java.util.Map;

import jp.co.alp.kenja.common.domain.Kintai;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 出欠集計の勤怠コード情報
 * @author maesiro
 * @version $Id: AccumulateKintaiInfo.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class AccumulateKintaiInfo {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateKintaiInfo.class);

    private static AccumulateKintaiInfo instance_;

    private final Map<Kintai, Integer> _kintaiAccumulateCounts;

    private AccumulateKintaiInfo() {
        _kintaiAccumulateCounts = new HashMap<Kintai, Integer>();
    }

    /**
     * コンストラクタ。
     * @return インスタンス
     */
    public static synchronized AccumulateKintaiInfo getInstance() {
        if (null == instance_) {
            instance_ = new AccumulateKintaiInfo();
        }
        return instance_;
    }

    /**
     * 1勤怠の集計カウント数を設定する。
     * @param kintai 勤怠
     * @param count 1勤怠の集計カウント数
     */
    public void setCountInfo(final Kintai kintai, final Integer count) {
        _kintaiAccumulateCounts.put(kintai, count);
    }

    /**
     * 1勤怠の集計カウント数を得る。
     * @param kintai 勤怠
     * @return 集計カウント数
     */
    public int getCount(final Kintai kintai) {
        Integer rtn = new Integer(1);
        if (_kintaiAccumulateCounts.containsKey(kintai)) {
            rtn = MapUtils.getInteger(_kintaiAccumulateCounts, kintai, rtn);
        }
        return rtn.intValue();
    }
} // AccumulateKintaiInfo

// eof
