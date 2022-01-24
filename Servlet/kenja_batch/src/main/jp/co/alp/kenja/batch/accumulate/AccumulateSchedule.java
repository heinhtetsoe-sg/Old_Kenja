// kanji=漢字
/*
 * $Id: AccumulateSchedule.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/21 10:39:22 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ExamItem;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.UsualSchedule;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: AccumulateSchedule.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AccumulateSchedule extends UsualSchedule {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateSchedule.class);

    private final Map<HomeRoom, Boolean> _countFlags;

    /**
     * コンストラクタ。
     * @param date 日付
     * @param period 校時
     * @param chair 講座
     * @param rollCalledDiv 出欠（点呼）実施区分
     * @param dataDiv 時間割データ区分
     */
    public AccumulateSchedule(
            final KenjaDateImpl date,
            final Period period,
            final Chair chair,
            final RollCalledDiv rollCalledDiv,
            final UsualSchedule.DataDiv dataDiv
    ) {
        super(date, period, chair, rollCalledDiv, dataDiv);
        _countFlags = new TreeMap<HomeRoom, Boolean>();
    }

    /**
     * 集計フラグを設定する。
     * @param homeRoom 年組
     * @param countFlag 集計フラグ
     */
    public void setCountFlag(
            final HomeRoom homeRoom,
            final boolean countFlag
    ) {
        if (null == homeRoom) {
            return;
        }

        if (!getChair().getHomeRooms().contains(homeRoom)) {
            return;
        }

        if (!countFlag) {
            log.debug("集計フラグセット: " + toString() + ", " + homeRoom + ", bool=" + countFlag);
        }
        _countFlags.put(homeRoom, BooleanUtils.toBooleanObject(countFlag));
    }

    private boolean isExam() {
        return DataDiv.EXAM == getDataDiv();
    }

    /**
     * 集計フラグを得る。<br>
     * 年組の集計フラグが無い場合、講座の集計フラグを得る。
     * @param homeRoom 年組
     * @return 集計フラグ
     */
    public boolean countFlag(final HomeRoom homeRoom) {
        if (isExam() && null != getExamItem()) {
            final ExamItem examItem = getExamItem();
            return examItem.getCountFlag();
        } else if (!isExam() && hasCountFlag(homeRoom)) {
            final Boolean bool = (Boolean) _countFlags.get(homeRoom);
            return bool.booleanValue();
        }
        return getChair().getCountFlag();
    }

    /**
     * 集計フラグの有無を判定する。
     * @param homeRoom 年組
     * @return 集計フラグ情報を持っていれば true
     */
    public boolean hasCountFlag(final HomeRoom homeRoom) {
        return isExam() && null != getExamItem() || !isExam() && _countFlags.containsKey(homeRoom);
    }

    /**
     * 実施区分が未実施かを判定する。
     * @return 実施区分が未実施ならtrue
     */
    public boolean isRollCalledDivNOTYET() {
        return RollCalledDiv.NOTYET.equals(getRollCalledDiv());
    }
} // AccumulateSchedule

// eof
