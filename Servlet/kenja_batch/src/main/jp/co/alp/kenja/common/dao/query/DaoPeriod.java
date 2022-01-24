// kanji=漢字
/*
 * $Id: DaoPeriod.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/07/06 17:19:22 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Period;

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
 * 校時を名前マスタから読み込む。
 * @author tamura
 * @version $Id: DaoPeriod.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class DaoPeriod extends AbstractDaoLoader<Period> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_NAME_MST";

    /** log */
    private static final Log log = LogFactory.getLog(DaoPeriod.class);
    private static final AbstractDaoLoader<Period> INSTANCE = new DaoPeriod();

    /*
     * コンストラクタ。
     */
    private DaoPeriod() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Period> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return Period.create(
                _cm.getCategory(),
                MapUtils.getString(map, "code"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "shortName"),
                MapUtils.getString(map, "special"),
                MapUtils.getInteger(map, "period_minutes")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    ltrim(NAMECD2) as code,"
                + "    NAME1 as name,"
                + "    coalesce(ABBV1,NAME1) as shortName,"     // ABBV1がnullだったら、NAME1を 'shortName'とする
                + "    NAMESPARE1 as special,"                  // 一日編集可能校時
                + "    NAMESPARE3 as period_minutes"            // 校時実施時間（分）
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  and"
                + "    NAMECD1 = 'B001'"
                + "  order by"
                + "    NAMECD2";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoPeriod

// eof
