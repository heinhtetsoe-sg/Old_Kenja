// kanji=漢字
/*
 * $Id: DaoBatchHomeRoomOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.absencehigh.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.dao.query.DaoHomeRoom;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.HomeRoom;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 年間ホームルーム（学年と組）を読み込む。
 * @version $Id: DaoBatchHomeRoomOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoBatchHomeRoomOfYear extends AbstractDaoLoader<HomeRoom> {
    /** テーブル名 */
    public static final String TABLE_NAME = DaoHomeRoom.TABLE_NAME;

    /** log */
    private static final Log log = LogFactory.getLog(DaoBatchHomeRoomOfYear.class);
    private static final AbstractDaoLoader<HomeRoom> INSTANCE = new DaoBatchHomeRoomOfYear();
    private static final Map<String, List<HomeRoom>> SEMESTER_HOMEROOM_MAP = new HashMap<String, List<HomeRoom>>();

    /*
     * コンストラクタ。
     */
    private DaoBatchHomeRoomOfYear() {
        super(log);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////// =>
    /**
     * 指定学期のホームルームリストを得る。
     * @param semesterCd 指定学期のコード
     * @return 指定学期のホームルームリスト
     */
    private static List<HomeRoom> getHomeRoomList(final String semesterCd) {
        if (!SEMESTER_HOMEROOM_MAP.containsKey(semesterCd)) {
            return Collections.emptyList();
        }
        return SEMESTER_HOMEROOM_MAP.get(semesterCd);
    }

    /**
     * 指定のホームルームを得る。
     * @param semesterCd 指定学期のコード
     * @param gradeCd 学年のコード
     * @param roomCd 組のコード
     * @return 指定のホームルーム
     */
    public static HomeRoom getHomeRoom(final String semesterCd, final String gradeCd, final String roomCd) {
    	for (final HomeRoom hr : getHomeRoomList(semesterCd)) {
            if (!hr.getGrade().getCode().equals(gradeCd)) {
                continue;
            }
            if (!hr.getRoom().equals(roomCd)) {
                continue;
            }
            return hr;
        }
        return null;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////// <=

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
        final HomeRoom homeRoom = HomeRoom.create(
                _cm.getCategory(),
                MapUtils.getString(map, "grade"),
                MapUtils.getString(map, "room"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "nameAbbr")
        );

        ///////////////////////////////////////////////////////////////////////////////////////////////////////// =>
        final String semesterCd = MapUtils.getString(map, "semester");
        if (!SEMESTER_HOMEROOM_MAP.containsKey(semesterCd)) {
            SEMESTER_HOMEROOM_MAP.put(semesterCd, new ArrayList<HomeRoom>());
        }
        getHomeRoomList(semesterCd).add(homeRoom);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////// <=
        return homeRoom;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    SEMESTER as semester,"
                + "    GRADE as grade,"
                + "    HR_CLASS as room,"
                + "    HR_NAME as name,"
                + "    HR_NAMEABBV as nameAbbr"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
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
        };
    }
} // DaoBatchHomeRoomOfYear

// eof
