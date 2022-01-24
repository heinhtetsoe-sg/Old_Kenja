/*
 * $Id: OnedayAttendanceJudge.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/03/08
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 1日出欠判定
 * @author maesiro
 * @version $Id: OnedayAttendanceJudge.java 74552 2020-05-27 04:41:22Z maeshiro $ $Date: 2020-05-27 13:41:22 +0900 (水, 27 5 2020) $
 */
public final class OnedayAttendanceJudge {

    private static Log log = LogFactory.getLog(OnedayAttendanceJudge.class);

    private final BatchSchoolMaster _batchSchoolMaster;
    private final KintaiManager _km;
    private final AccumulateOptions _options;

    private final List<Kintai> _suspendMourning;
    private final List<Kintai> _suspendKesseki;
    private final List<Kintai> _mourningKesseki;
    private final List<Kintai> _syusseki;
    private final List<Kintai> _kesseki;
    private final List<Kintai> _suspendMourningKesseki;

    /**
     * コンストラクタ。
     * @param batchSchoolMaster 学校マスタ
     * @param kintaiManager 勤怠マネージャー
     */
    public OnedayAttendanceJudge(final AccumulateOptions options, final BatchSchoolMaster batchSchoolMaster, final KintaiManager kintaiManager) {
        _options = options;
        _batchSchoolMaster = batchSchoolMaster;
        _km = kintaiManager;

        final List<Kintai> suspend = union(_km.suspend());
        final List<Kintai> suspendMourning = union(suspend, _km.mourning());
        final List<Kintai> syusseki = union(_km.seated(), _km.nurseoff(), _km.late(), _km.early(), _km.late2(), _km.late3());
        final List<Kintai> kesseki = union(_km.sick(), _km.notice(), _km.nonotice());

        _suspendMourning = notNull("出停忌引", suspendMourning);
        _syusseki = notNull("出席扱い", syusseki);
        _kesseki = notNull("欠席扱い", kesseki);
        _suspendKesseki = notNull("出停欠席", union(_kesseki, suspend));
        _mourningKesseki = notNull("忌引欠席", union(_kesseki, _km.mourning()));
        _suspendMourningKesseki = notNull("出停忌引欠席", union(_suspendMourning, _kesseki));
    }

    private boolean isTrace(final Student student) {
        return null != _options && null != student && _options.getTracer().isTrace(student);
    }

    private static <T> List<T> notNull(final String message, final Collection<T> c1) {
        final List<T> list = new ArrayList<T>(c1);
        for (final Iterator<T> it = list.iterator(); it.hasNext();) {
            final T t = it.next();
            if (null == t) {
                it.remove();
            }
        }
        if (list.size() != c1.size()) {
            log.info(" 勤怠コードチェック : " + message + " = " + list);
        }
        return list;
    }

    private static <T> List<T> union(final T ... ts) {
        final List<T> list = new ArrayList<T>();
        if (null != ts) {
            list.addAll(Arrays.asList(ts));
        }
        return list;
    }

    private static <T> List<T> union(final Collection<T> c1, final T t) {
        final List<T> list = new ArrayList<T>();
        list.addAll(c1);
        list.add(t);
        return list;
    }

    private static <T> List<T> union(final Collection<T> c1, final Collection<T> c2) {
        final List<T> list = new ArrayList<T>();
        list.addAll(c1);
        list.addAll(c2);
        return list;
    }

    private static boolean eq(final Integer v1, final Integer v2) {
        if (null == v1 || null == v2) {
            return false;
        }
        if (v1 == v2) {
            return true;
        }
        if (v1.intValue() == v2.intValue()) {
            log.warn(" primitive equals : " + v1 + ", " + v2 + " / (object equals : " + (v1 == v2) + ")");
            return true;
        }
        return false;
    }

    private static boolean containsKintai(final Collection<Kintai> c, final Kintai kintai) {
        if (null == kintai) {
            return false;
        }
        boolean contains = false;
        for (final Kintai checkKintai : c) {
            if (null == checkKintai || null == checkKintai.getAltCode()) {
                continue;
            }
            if (eq(checkKintai.getAltCode(), kintai.getAltCode())) {
                contains = true;
                break;
            }
//            if (!contains) {
//                log.info(" ??? contains? " + contains + " / " + checkKintai.getAltCode() + " <> "+ kintai.getAltCode() + " (" + (checkKintai.getAltCode().equals(kintai.getAltCode())) + ")");
//            }
        }
//        log.info(" contains? " + contains + " / " + kintai.getAltCode() + ", " + c);
        return contains;
    }

    private boolean isSyusseki(final Kintai kintai) {
        return containsKintai(_syusseki, kintai);
    }

    /**
     * 「欠席」か？
     * @param kintai 勤怠
     * @return 欠席ならtrue
     */
    private boolean isKesseki(final Kintai kintai) {
        return containsKintai(_kesseki, kintai);
    }

    /**
     * １日出欠の勤怠を得る。
     * @param schedule １日時間割
     * @return １日出欠の勤怠
     */
    public Kintai getOnedayKintai(final OnedaySchedule schedule) {
        if (_batchSchoolMaster.syussekiSansyutuhouIsSuspendMourning()) {
            if (isSuspend(schedule) || isMourning(schedule)) {
                return schedule.getFirstKintaiIn(_suspendMourning);
            } else {
                return schedule.getFirstKintaiIn(_kesseki);
            }
        }
        return schedule.getFirstKintai();
    }

    /**
     * 早退（届無）か？
     * @param schedule １日時間割
     * @return 早退（届無）ならtrue
     */
    public boolean isEarlyNonotice(final OnedaySchedule schedule) {
        // 最後の授業が早退
        final Kintai lastKintai = schedule.getLastKintai();
        if (KintaiManager.isEarlyNonotice(lastKintai)) {
            // 最後の授業が早退(無)
            return true;
        }
        return false;
    }

    /**
     * 早退か？
     * @param schedule １日時間割
     * @return 早退ならtrue
     */
    public boolean isEarly(final OnedaySchedule schedule) {
        // 最後の授業が早退
        final Kintai lastKintai = schedule.getLastKintai();
        if (KintaiManager.isEarly(lastKintai)) {
            return true;
        } else {
            // 最後の授業=欠席:[病欠/事故欠(届)/事故欠(無)] && その他=出席がある
            final boolean isSaigoniKesseki = isKesseki(lastKintai);
            final boolean isSaigoIgainiSyusseki = saigoIgainiSyusseki(schedule);
            if (isSaigoniKesseki && isSaigoIgainiSyusseki) {
                return true;
            }
        }
        return false;
    }

    /**
     * 遅刻（届無）か？
     * @param schedule １日時間割
     * @return 遅刻（届無）ならtrue
     */
    public boolean isLateNonotice(final OnedaySchedule schedule) {
        // 最初の授業が遅刻
        if (KintaiManager.isLateNonotice(schedule.getFirstKintai())) {
            // 最初の授業が遅刻(無)
            return true;
        }
        return false;
    }

    /**
     * 遅刻か？
     * @param schedule １日時間割
     * @return 遅刻ならtrue
     */
    public boolean isLate(final OnedaySchedule schedule) {
        // 最初の授業が遅刻
        final Kintai firstKintai = schedule.getFirstKintai();
        if (isTrace(schedule.getStudent())) {
            log.info(" ** " + schedule.getStudent() + " | " + schedule._date + " firstKinai = " + firstKintai);
        }
        if (KintaiManager.isLate(firstKintai)) {
            return true;
        } else {
            // 最初の授業=欠席:[病欠/事故欠(届)/事故欠(無)] && その他=出席がある
            final boolean isSaisyoniKesseki = isKesseki(firstKintai);
            if (isTrace(schedule.getStudent())) {
                log.info(" ** " + schedule.getStudent() + " | " + schedule._date + " 最初に欠席? " + isSaisyoniKesseki);
            }
            final boolean isSaisyoIgainiSyusseki = saisyoIgainiSyusseki(schedule);
            if (isSaisyoniKesseki && isSaisyoIgainiSyusseki) {
                return true;
            }
        }
        return false;
    }

    /**
     * 最初の授業以外に[出席]が含まれているか判定する。
     * @param schedule １日時間割
     * @return 最初の授業以外に[出席]が含まれていればtrue
     */
    public boolean saisyoIgainiSyusseki(final OnedaySchedule schedule) {
        final int size = schedule.getAltKintaiList().size();
        boolean saisyoIgainiSyusseki = false;
        if (1 < size) {
            for (final Kintai kintai : schedule.getAltKintaiList().subList(1, size)) {
                if (isSyusseki(kintai)) {
                    saisyoIgainiSyusseki = true;
                    break;
                }
            }
        }
        if (isTrace(schedule._student)) {
            log.info(" ** " + schedule._date + " 最初以外に出席? " + saisyoIgainiSyusseki);
        }
        return saisyoIgainiSyusseki;
    }

    /**
     * 最後の授業以外に[出席]が含まれているか判定する。
     * @param schedule １日時間割
     * @return 最後の授業以外に[出席]が含まれていればtrue
     */
    public boolean saigoIgainiSyusseki(final OnedaySchedule schedule) {
        final int size = schedule.getAltKintaiList().size();
        boolean saigoIgainiSyusseki = false;
        if (1 < size) {
            for (final Kintai kintai : schedule.getAltKintaiList().subList(0, size - 1)) {
                if (isSyusseki(kintai)) {
                    saigoIgainiSyusseki = true;
                    break;
                }
            }
        }
        if (isTrace(schedule._student)) {
            log.info(" ** " + schedule._date + " 最後以外に出席? " + saigoIgainiSyusseki);
        }
        return saigoIgainiSyusseki;
    }

    /**
     * 勤怠がすべて欠課か
     * @param schedule １日時間割
     * @return 勤怠がすべて欠課ならtrue
     */
    public boolean isKesseki(final OnedaySchedule schedule) {
        final List<Kintai> kintaiList = schedule.getAltKintaiList();
        if (null == kintaiList || kintaiList.isEmpty()) {
            return false;
        }
        boolean isKesseki;
        if (_batchSchoolMaster.syussekiSansyutuhouIsSuspendMourning()) {
            if (!isMourning(schedule) && !isSuspend(schedule)
                    && (schedule.isAll(_suspendMourningKesseki)
                     || schedule.isAll(_suspendKesseki)
                     || schedule.isAll(_mourningKesseki))) {
                isKesseki = true;
            } else {
                isKesseki = false;
            }
        } else {
            isKesseki = true;
            for (final Kintai kintai : kintaiList) {
                if (!isKesseki(kintai)) {
                    isKesseki = false;
                }
            }
        }
        return isKesseki;
    }

    /**
     * 忌引か
     * @param schedule １日時間割
     * @return 忌引ならtrue
     */
    public boolean isMourning(final OnedaySchedule schedule) {
        if (_batchSchoolMaster.syussekiSansyutuhouIsSuspendMourning()) {
            return schedule.isAll(_km.mourning()) || (!schedule.isAll(_km.suspend()) && schedule.isAll(_suspendMourning));
        } else {
            return schedule.contains(_km.mourning());
        }
    }

    /**
     * 出停か
     * @param schedule １日時間割
     * @return 出停ならtrue
     */
    public boolean isSuspend(final  OnedaySchedule schedule) {
        if (_batchSchoolMaster.syussekiSansyutuhouIsSuspendMourning()) {
            return schedule.isAll(_km.suspend()) || (!schedule.isAll(_km.mourning()) && schedule.isAll(_suspendMourning));
        } else {
            return schedule.contains(_km.suspend());
        }
    }

    /**
     * 遅刻・早退をカウントするか
     * @param schedule １日時間割
     * @return 遅刻・早退をカウントするならtrue
     */
    public boolean isCountLateEarly(final OnedaySchedule schedule) {
        return !(isSuspend(schedule) || isMourning(schedule));
    }

    /**
     * 公欠か
     * @param onedaySchedule １日時間割
     * @return 公欠ならtrue
     */
    public boolean isAbsent(final OnedaySchedule onedaySchedule) {
        return onedaySchedule.contains(_km.absent());
    }

    /**
     * 出停（伝染病）か
     * @param onedaySchedule １日時間割
     * @return 出停（伝染病）ならtrue
     */
    public boolean isVirus(final OnedaySchedule onedaySchedule) {
        return onedaySchedule.contains(_km.virus());
    }

    /**
     * カウントなしか
     * @param onedaySchedule １日時間割
     * @return カウントなしならtrue
     */
    public boolean isNoCount(final OnedaySchedule onedaySchedule) {
        return onedaySchedule.contains(_km.noCount()) || onedaySchedule.isAll(_km.noCount2());
    }

    /**
     * 勤怠マネージャーを得る
     * @return 勤怠マネージャー
     */
    public KintaiManager getKintaiManager() {
        return _km;
    }
}
