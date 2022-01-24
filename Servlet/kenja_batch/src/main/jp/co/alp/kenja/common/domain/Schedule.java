// kanji=漢字
/*
 * $Id: Schedule.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/06/16 16:43:46 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 基本時間割と通常時間割のスーパークラス。
 * @author tamura
 * @version $Id: Schedule.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class Schedule implements Comparable<Schedule> {
    /** log */
    private static final Log log = LogFactory.getLog(Schedule.class);

    private final KenjaDate _date;
    private final Period _period;
    private final Chair _chair;

    /**  @since APPLET_5_62_0 */
    private final Map<HomeRoom, Boolean> _countFlags;
    /**  @since APPLET_5_62_0 */
    private final Map<HomeRoom, Boolean> _unmodCountFlags;

    /**
     * コンストラクタ。
     * @param dirty 時間割の状態
     * @param date 曜日/日時
     * @param period 校時
     * @param chair 講座
     * @param usingCountFlags 年組毎の集計フラグを使用するか否か
     * @param doInitCountFlags 年組毎の集計フラグを講座の集計フラグで初期化するか否か
     */
    protected Schedule(
            final KenjaDate date,
            final Period period,
            final Chair chair,
            final boolean usingCountFlags,
            final boolean doInitCountFlags
    ) {
        _date = date;
        _period = period;
        _chair = chair;

        if (usingCountFlags) {
            _countFlags = new TreeMap<HomeRoom, Boolean>();
            _unmodCountFlags = Collections.unmodifiableMap(_countFlags);
            if (doInitCountFlags) {
                initCountFlags(chair);
            }
        } else {
            _countFlags = null;
            _unmodCountFlags = null;
        }
    }

    private void initCountFlags(final Chair chair) {
        final Set<HomeRoom> homeRooms = chair.getHomeRooms();
        for (final HomeRoom hr : homeRooms) {
            setCountFlag0(hr, chair.getCountFlag());
        }
    }

    /**
     * 集計フラグを設定する。
     * @param homeRoom 年組
     * @param countFlag 集計フラグ
     * @since APPLET_5_62_0
     */
    private void setCountFlag0(
            final HomeRoom homeRoom,
            final boolean countFlag
    ) {
        _countFlags.put(homeRoom, BooleanUtils.toBooleanObject(countFlag));
    }

    /**
     * 集計フラグを設定する。
     * @param homeRoom 年組
     * @param countFlag 集計フラグ
     * @since APPLET_5_62_0
     */
    public void setCountFlag(
            final HomeRoom homeRoom,
            final boolean countFlag
    ) {
        if (null == homeRoom) {
            throw new NullPointerException("homeRoomがnull");
        }

        if (getChair().getHomeRooms().contains(homeRoom)) {
            setCountFlag0(homeRoom, countFlag);
        } else {
            log.warn(homeRoom + "は、対象年組ではない");
        }
    }

    /**
     * 指定した年組の集計フラグを得る。
     * @param homeRoom 年組
     * @return 集計フラグ
     * @since APPLET_5_62_0
     */
    public boolean getCountFlag(final HomeRoom homeRoom) {
        if (null == homeRoom) {
            throw new NullPointerException("homeRoomがnull");
        }

        if (_unmodCountFlags.containsKey(homeRoom)) {
            return ((Boolean) _unmodCountFlags.get(homeRoom)).booleanValue();
        }

        // 集計フラグのMapに含まれていない年組の場合
        return CountFlagUtils.DEFAULT_COUNT_FLAG.booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        if (obj instanceof Schedule) {
            final Schedule that = (Schedule) obj;
            if (that._date != _date) {
                return false;
            }
            if (that._period != _period) {
                return false;
            }
            if (that.getChair() != getChair()) {
                return false;
            }
        }
        return true;
    }

    /**
     * このオブジェクトと指定されたオブジェクトの順序を比較する。
     * @param that オブジェクト
     * @return 比較結果
     */
    public int compareTo(final Schedule that) {
        int rtn = 0;

        rtn = getDate().compareTo(that.getDate());
        if (0 != rtn) {
            return rtn;
        }

        rtn = getPeriod().compareTo(that.getPeriod());
        if (0 != rtn) {
            return rtn;
        }

        rtn = getChair().compareTo(that.getChair());
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getDate() + "/" + getPeriod() + "/" + getChair().getCode();
    }

    //get
    //========================================================================
    /**
     * 曜日/日時を得る。
     * @return 曜日/日時
     */
    public final KenjaDate getDate() { return _date; }

    /**
     * 校時を得る。
     * @return 校時
     */
    public final Period getPeriod() { return _period; }

    /**
     * 講座を得る。
     * @return 講座
     */
    public Chair getChair() { return _chair; }

} // Schedule

// eof
