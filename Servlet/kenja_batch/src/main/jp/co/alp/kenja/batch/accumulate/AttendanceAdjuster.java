/*
 * $Id: AttendanceAdjuster.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2010/10/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix.DateAttendanceMap;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 出欠データを集計用に修正する。
 * @version $Id: AttendanceAdjuster.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class AttendanceAdjuster {
    /*pkg*/static final Log log = LogFactory.getLog(AttendanceAdjuster.class);

    private static PeriodComparator comparator_ = new PeriodComparator();

    /**
     * コンストラクタ
     */
    private AttendanceAdjuster() {
    }

    /**
     * 校時順にソートするコンパレーター
     * @author maesiro
     */
    private static class PeriodComparator implements Comparator<Attendance> {
        public int compare(final Attendance a1, final Attendance a2) {
            return a1.getPeriod().compareTo(a2.getPeriod());
        }
    }

    /**
     * 生徒の時間割を修正する
     * @param student 生徒
     * @param attendances 生徒の時間割マップ
     */
    public static void adjust(final Student student, final DateAttendanceMap attendances) {
        final KenjaDateImpl grdDate = student.getGrdDate();
        for (final KenjaDateImpl date : attendances.getDateKeySet()) {
            if (date.compareTo(grdDate) > 0) {
                return;
            }
            final Collection<Attendance> coll = attendances.getAttendanceList(date);
            if (null == coll || coll.isEmpty()) {
                continue;
            }

            adjustKintaiInSamePeriod(coll);
        }
    }

    /**
     * 同一日付同一校時の時間割が複数ある場合出欠データを修正する
     * @param coll 1日の時間割の出欠のコレクション
     */
    public static void adjustKintaiInSamePeriod(final Collection<Attendance> coll) {

        final List<Attendance> sortedList = new ArrayList<Attendance>();
        sortedList.addAll(coll);
        Collections.sort(sortedList, comparator_);

        final Map<Period, Collection<Attendance>> periodAttendancesMap = getSamePeriodAttendancesMap(sortedList);

        for (final Period period : periodAttendancesMap.keySet()) {
            final Collection<Attendance> samePeriodnAttedance = periodAttendancesMap.get(period);

            if (1 < samePeriodnAttedance.size()) { // 同一日付同一校時の時間割に時間割が2件以上設定されている
                final Kintai notSeated = getNotSeated(samePeriodnAttedance);

                if (notSeated == null) {
                    continue;
                }

                // 同一校時の出欠データのうち入力された勤怠がある
                for (final Attendance att : samePeriodnAttedance) {
                    att.adjustKintai(notSeated);
                }
                log.info(" 同一日付同一校時の時間割に勤怠を設定します。:" + notSeated + " (" + samePeriodnAttedance + ")");
            }
        }
    }

    /**
     * 同一校時の出欠データのうち入力された勤怠を得る
     * @param samePeriodAttedances 同一校時の出欠データ
     * @return 同一校時の出欠データの勤怠
     */
    private static Kintai getNotSeated(final Collection<Attendance> samePeriodAttedances) {
        Kintai notSeated = null;
        int count = 0;
        for (final Attendance att : samePeriodAttedances) {
            final Kintai kintai = att.getKintai();
            if (null != kintai && !kintai.isSeated()) {
                notSeated = kintai;
                count++;
                if (count > 1) {
                    // テーブル ATTEND_DAT から読み込む同一日付同一校時の勤怠データは多くても1件のはず
                    log.fatal("同一日付同一校時に出欠データが2件以上設定されています。");
                }
            }
        }
        return notSeated;
    }

    /**
     * 校時をキーとする同一校時の時間割出欠のリストのマップを得る
     * @param sortedList 校時でソートされた時間割出欠のリスト
     * @return 校時をキーとする同一校時の時間割出欠のリストのマップ
     */
    private static Map<Period, Collection<Attendance>> getSamePeriodAttendancesMap(final List<Attendance> sortedList) {
        final Map<Period, Collection<Attendance>> periodAttendances = new HashMap<Period, Collection<Attendance>>();
        for (final Attendance attendance : sortedList) {
            final Period period = attendance.getPeriod();
            if (!periodAttendances.containsKey(period)) {
                periodAttendances.put(period, new ArrayList<Attendance>());
            }
            periodAttendances.get(period).add(attendance);
        }
        return periodAttendances;
    }
}
