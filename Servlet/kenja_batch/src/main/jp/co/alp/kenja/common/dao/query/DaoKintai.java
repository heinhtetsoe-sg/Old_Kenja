// kanji=漢字
/*
 * $Id: DaoKintai.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/02/11 13:19:22 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaTableStatus;
import jp.co.alp.kenja.common.domain.Kintai;

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
 * 勤怠を名前マスタから読み込む。
 * @author takaesu
 * @version $Id: DaoKintai.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoKintai extends AbstractDaoLoader<Kintai> {
    /** テーブル名 */
    public static final String V_NAME_MST = "V_NAME_MST";
    public static final String ATTEND_DI_CD_DAT = "ATTEND_DI_CD_DAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoKintai.class);
    private static final AbstractDaoLoader<Kintai> INSTANCE = new DaoKintai();

    /*
     * コンストラクタ。
     */
    private DaoKintai() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Kintai> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    protected void preLoad() throws SQLException {
        DaoKenjaTableDataStatus.getInstance().load(_conn, _cm);
    }

    /**
     * {@inheritDoc}
     */
    protected void postLoad() throws SQLException {
        if (0 == Kintai.size(_cm.getCategory())) {
            log.fatal("勤怠コードがマスタに設定されていません");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return Kintai.create(
                _cm.getCategory(),
                MapUtils.getString(map, "code"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "mark"),
                MapUtils.getString(map, "altCode"),
                MapUtils.getString(map, "betsuCode")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        if (KenjaTableStatus.getInstance().useAttendDiCdDat()) {
            return "select"
                    + "    ltrim(DI_CD) as code,"
                    + "    DI_NAME1 as name,"
                    + "    coalesce(DI_MARK,'') as mark," // ABBV1がnullだったら、empty値とする（例、“出席”の場合）
                    + "    ONEDAY_DI_CD as altCode,"
                    + "    REP_DI_CD as betsuCode"
                    + "  from " + ATTEND_DI_CD_DAT
                    + "  where"
                    + "    YEAR = ?"
                    + "  order by"
                    + "    ORDER,"
                    + "    smallint(DI_CD)"
                    ;
        } else {
            return "select"
                    + "    ltrim(NAMECD2) as code,"
                    + "    NAME1 as name,"
                    + "    coalesce(ABBV1,'') as mark," // ABBV1がnullだったら、empty値とする（例、“出席”の場合）
                    + "    NAMESPARE1 as altCode,"
                    + "    CAST(NULL AS VARCHAR(2)) as betsuCode"
                    + "  from " + V_NAME_MST
                    + "  where"
                    + "    YEAR = ?"
                    + "  and"
                    + "    NAMECD1 = 'C001'"
                    + "  order by"
                    + "    NAMESPARE2,"
                    + "    smallint(NAMECD2)"
                    ;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoKintai

// eof
