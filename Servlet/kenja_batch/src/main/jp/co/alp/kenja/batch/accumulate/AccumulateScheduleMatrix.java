// kanji=漢字
/*
 * $Id: AccumulateScheduleMatrix.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/21 10:50:32 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.UsualSchedule.DataDiv;

/**
 * 時間割のマトリックス。
 * @author takaesu
 * @version $Id: AccumulateScheduleMatrix.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class AccumulateScheduleMatrix implements Iterable<AccumulateSchedule> {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateScheduleMatrix.class);

    private final Map<KenjaDateImpl, List<AccumulateSchedule>> _dateSchedulesMap = new HashMap<KenjaDateImpl, List<AccumulateSchedule>>();
    private final Set<AccumulateSchedule> _accumulateScheduleSet = new HashSet<AccumulateSchedule>();

    /**
     * 時間割を設定する。
     * @param sch 時間割
     */
    public void assign(final AccumulateSchedule sch) {

        final KenjaDateImpl date = (KenjaDateImpl) sch.getDate();
        List<AccumulateSchedule> list = _dateSchedulesMap.get(date);
        if (null != list && list.contains(sch)) {
            final Period period = sch.getPeriod();
            final Chair chair = sch.getChair();
            log.warn("コマへの読み込みが複数ある:" + date + "/" + period + "/講座=" + chair);
            return;
        }

        if (null == list) {
            list = new ArrayList<AccumulateSchedule>();
            _dateSchedulesMap.put(date, list);
        }
        list.add(sch);
        _accumulateScheduleSet.add(sch);
    }

    /**
     * 指定日の時間割の<code>List</code>を得る。
     * @param date 日付
     * @return 時間割の<code>List</code>
     */
    public List<AccumulateSchedule> get(final KenjaDateImpl date) {
        return _dateSchedulesMap.get(date);
    }

    /**
     * 指定日の指定校時の時間割の<code>List</code>を得る。
     * @param date 日付
     * @param period 校時
     * @return 時間割の<code>List</code>
     */
    public List<AccumulateSchedule> get(final KenjaDateImpl date, final Period period) {
        final List<AccumulateSchedule> list = get(date);
        if (null == list) {
            return null;
        }
        final List<AccumulateSchedule> rtn = new ArrayList<AccumulateSchedule>();
        for (final AccumulateSchedule schedule : list) {
            if (null == schedule) {
                continue;
            }
            if (!schedule.getDate().equals(date)) {
                continue;
            }
            if (!schedule.getPeriod().equals(period)) {
                continue;
            }
            rtn.add(schedule);
        }
        return rtn;
    }

    public List<KenjaDateImpl> getDateSet() {
        return Collections.unmodifiableList(new ArrayList<KenjaDateImpl>(new TreeSet<KenjaDateImpl>(_dateSchedulesMap.keySet())));
    }

    /**
     * マトリックスから、条件にあう時間割を得る。
     * @param date 曜日/日付
     * @param period 校時
     * @param chair 講座
     * @param allowedDatadivList データ区分のリスト
     * @return 時間割。なければ<code>null</code>
     */
    public AccumulateSchedule get(
            final KenjaDateImpl date,
            final Period period,
            final Chair chair,
            final List<DataDiv> allowedDatadivList
    ) {
        final List<AccumulateSchedule> list = get(date);
        if (null == list) {
            return null;
        }
        for (final AccumulateSchedule schedule : list) {
            if (null == schedule) {
                continue;
            }
            if (!schedule.getDate().equals(date)) {
                continue;
            }
            if (!schedule.getPeriod().equals(period)) {
                continue;
            }
            if (!schedule.getChair().equals(chair)) {
                continue;
            }
            if (null != allowedDatadivList && !allowedDatadivList.contains(schedule.getDataDiv())) {
                continue;
            }
            return schedule;
        }
        return null;
    }

    /**
     * イテレータを得る。
     * @return イテレータ
     */
    public Iterator<AccumulateSchedule> iterator() {
        return _accumulateScheduleSet.iterator();
    }
} // AccumulateScheduleMatrix

// eof
