// kanji=漢字
/*
 * $Id: DaoChairHomeRoom.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/07 17:49:19 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;

/*
 * describe table CHAIR_CLS_DAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 いいえ
 * GROUPCD                        SYSIBM    VARCHAR                   4     0 いいえ
 * TRGTGRADE                      SYSIBM    VARCHAR                   2     0 いいえ
 * TRGTCLASS                      SYSIBM    VARCHAR                   2     0 いいえ
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     8 レコードが選択されました。
 */

/**
 * 講座受講クラスデータを読み込む。
 * @author tamura
 * @version $Id: DaoChairHomeRoom.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoChairHomeRoom extends AbstractDaoLoader<HomeRoom> {
    /** テーブル名 */
    public static final String TABLE_NAME = "CHAIR_CLS_DAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoChairHomeRoom.class);
    private static final AbstractDaoLoader<HomeRoom> INSTANCE = new DaoChairHomeRoom();

    /*
     * コンストラクタ。
     */
    private DaoChairHomeRoom() {
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
        final Chair chair = Chair.getInstance(_cm.getCategory(), MapUtils.getString(map, "chair"));
        if (null == chair) {
            return "不明な講座コード(chair)";
        }
        final GroupClass group = GroupClass.getInstance(_cm.getCategory(), MapUtils.getString(map, "group"));
        if (null == group) {
            return "不明な選択科目コード(group)";
        }
        final HomeRoom hr = HomeRoom.getInstance(
                _cm.getCategory(),
                MapUtils.getString(map, "grade"),
                MapUtils.getString(map, "room")
        );
        if (null == hr) {
            return "不明なホームルーム(grade,room)";
        }

        chair.addHomeRoom(hr);
        group.addHomeRoom(hr);

        return hr;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    CHAIRCD as chair,"
                + "    GROUPCD as group,"
                + "    TRGTGRADE as grade,"
                + "    TRGTCLASS as room"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  and"
                + "    SEMESTER = ?"
                + "  order by"
                + "    CHAIRCD,"
                + "    GROUPCD,"
                + "    TRGTGRADE,"
                + "    TRGTCLASS";
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
} // DaoChairHomeRoom

// eof
