// kanji=漢字
/*
 * $Id: DaoHomeRoom.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/06 16:55:10 - JST
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
import jp.co.alp.kenja.common.domain.HomeRoom;

/*
 * describe table SCHREG_REGD_HDAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * GRADE                          SYSIBM    VARCHAR                   2     0 いいえ
 * HR_CLASS                       SYSIBM    VARCHAR                   2     0 いいえ
 * HR_NAME                        SYSIBM    VARCHAR                  15     0 はい
 * HR_NAMEABBV                    SYSIBM    VARCHAR                   5     0 はい
 * HR_FACCD                       SYSIBM    VARCHAR                   4     0 はい
 * TR_CD1                         SYSIBM    VARCHAR                   8     0 はい
 * TR_CD2                         SYSIBM    VARCHAR                   8     0 はい
 * TR_CD3                         SYSIBM    VARCHAR                   8     0 はい
 * SUBTR_CD1                      SYSIBM    VARCHAR                   8     0 はい
 * SUBTR_CD2                      SYSIBM    VARCHAR                   8     0 はい
 * SUBTR_CD3                      SYSIBM    VARCHAR                   8     0 はい
 * CLASSWEEKS                     SYSIBM    SMALLINT                  2     0 はい
 * CLASSDAYS                      SYSIBM    SMALLINT                  2     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     17 レコードが選択されました。
 */

/**
 * ホームルーム（学年と組）を読み込む。
 * @author tamura
 * @version $Id: DaoHomeRoom.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoHomeRoom extends AbstractDaoLoader<HomeRoom> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCHREG_REGD_HDAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoHomeRoom.class);
    private static final AbstractDaoLoader<HomeRoom> INSTANCE = new DaoHomeRoom();

    /*
     * コンストラクタ。
     */
    private DaoHomeRoom() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<HomeRoom> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return HomeRoom.create(
                _cm.getCategory(),
                MapUtils.getString(map, "grade"),
                MapUtils.getString(map, "room"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "nameAbbr")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    GRADE as grade,"
                + "    HR_CLASS as room,"
                + "    HR_NAME as name,"
                + "    HR_NAMEABBV as nameAbbr"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  and"
                + "    SEMESTER = ?"
                + "  order by"
                + "    GRADE,"
                + "    HR_CLASS"
            ;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
            cm.getCurrentSemester().getCodeAsString(),
        };
    }
} // DaoHomeRoom

// eof
