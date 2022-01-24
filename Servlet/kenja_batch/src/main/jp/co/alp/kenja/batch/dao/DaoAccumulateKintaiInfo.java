// kanji=漢字
/*
 * $Id: DaoAccumulateKintaiInfo.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/15 15:23:01 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AccumulateKintaiInfo;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Kintai;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * describe table V_NAME_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * NAMECD1                        SYSIBM    VARCHAR                   4     0 いいえ
 * NAMECD2                        SYSIBM    VARCHAR                   4     0 いいえ
 * NAME1                          SYSIBM    VARCHAR                  60     0 はい
 * NAME2                          SYSIBM    VARCHAR                  60     0 はい
 * NAME3                          SYSIBM    VARCHAR                  60     0 はい
 * ABBV1                          SYSIBM    VARCHAR                  30     0 はい
 * ABBV2                          SYSIBM    VARCHAR                  30     0 はい
 * ABBV3                          SYSIBM    VARCHAR                  30     0 はい
 * NAMESPARE1                     SYSIBM    VARCHAR                  30     0 はい
 * NAMESPARE2                     SYSIBM    VARCHAR                  30     0 はい
 * NAMESPARE3                     SYSIBM    VARCHAR                  30     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     13 レコードが選択されました。
 */

/**
 * 勤怠コードの情報を読み込む。
 * @author takaesu
 * @version $Id: DaoAccumulateKintaiInfo.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoAccumulateKintaiInfo extends AbstractDaoLoader<AccumulateKintaiInfo> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_NAME_MST";

    /*pkg*/static final Log log = LogFactory.getLog(DaoAccumulateKintaiInfo.class);
    private static final AbstractDaoLoader<AccumulateKintaiInfo> INSTANCE = new DaoAccumulateKintaiInfo();

    /*
     * コンストラクタ。
     */
    private DaoAccumulateKintaiInfo() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<AccumulateKintaiInfo> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    NAMECD2 as code,"
                + "    ABBV2 as count"
                + "  from "
                + TABLE_NAME
                + "  where "
                + "    NAMECD1 = 'C001' ";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {

        final Integer defaultCount = Integer.valueOf(1);
        String msg = null;

        final String diCd = MapUtils.getString(map, "code");
        final Kintai kintai = Kintai.getInstance(_cm.getCategory(), diCd);

        if (null != kintai && (KintaiManager.isLate2(kintai) || KintaiManager.isLate3(kintai))) {
            Integer count = MapUtils.getInteger(map, "count");
            if (null == count) {
                msg = "勤怠コード" + diCd + "にはカウント数が設定されていません。" + defaultCount + "を使用します。";
                count = defaultCount;
            }
            AccumulateKintaiInfo.getInstance().setCountInfo(kintai, count);
        }

        return msg;
    }
} // DaoAccumulateKintaiInfo

// eof
