// kanji=漢字
/*
 * $Id: AccumulateSemes.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/27 11:48:36 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.domain.HogeUtils;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.Student.TransferCd;
import jp.co.alp.kenja.common.util.CollectionWrapper;

/**
 * AccumulateSemes。
 * @author takaesu
 * @version $Id: AccumulateSemes.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class AccumulateSemes {

    private static Log log = LogFactory.getLog(AccumulateSubclass.class);

    private static final int CODE_LESSON = 99999;
    private static final int CODE_OFFDAYS = 99998;
    private static final int CODE_ABROAD = 99997;

    private static final int CODE_KEKKA_JISU = 899999;  // 月別のみ
    private static final int CODE_KEKKA = 899998;        // 月別のみ
    private static final int CODE_LATEDETAIL = 89997;   // 月別のみ

    private final Set<KenjaDateImpl> _absenceDates = new TreeSet<KenjaDateImpl>();

    /**
     * 計算する。
     * @param student 生徒
     * @param coll 出欠の<code>Collection</code>
     * @param date 日付
     * @param judge 1日時間割判定
     */
    public void calc(
            final AccumulateOptions options,
            final Student student,
            final Collection<Attendance> coll,
            final KenjaDateImpl date,
            final OnedayAttendanceJudge judge
    ) {
        if (null == coll || coll.isEmpty()) {
            return;
        }
        final boolean isTrace = null != options && options.getTracer().isTrace(student);

        if (judge.isNoCount(new OnedaySchedule(student, date, coll))) {
            log.info("時間割カウントなし: schregno = " + student.getCode() + ", date = " + date);
            return;
        }

        final Collection<Attendance> exceptNoCount2 = new ArrayList<Attendance>(coll);
        for (final Iterator<Attendance> it = exceptNoCount2.iterator(); it.hasNext();) {
            final Attendance attendance = it.next();
            if (KintaiManager.isNoCount2(attendance.getKintai())) {
                it.remove();
            }
        }
        final OnedaySchedule onedaySchedule = new OnedaySchedule(student, date, exceptNoCount2);

        if (isTrace) {
            log.info(date + "時間割: " + onedaySchedule);
            log.info(date + "代替勤怠コード: " + onedaySchedule.getAltKintaiList());
        }

        lesson().add(date);

        if (!student.isActive(date)) {
            final TransferCd transferCd = HogeUtils.getTransferCd(student, date);
            if (TransferCd.TAKE_OFF_SCHOOL.equals(transferCd)) {
                offdays().add(date);
                return;
            } else  if (TransferCd.STUDY_ABROAD.equals(transferCd)) {
                abroad().add(date);
                return;
            }
        }

        // 公欠が1つ以上あるか？
        if (judge.isAbsent(onedaySchedule)) {
            absent().add(date);
        }

        // 出停が1つ以上あるか？
        if (judge.isSuspend(onedaySchedule)) {
            suspend().add(date);
        }

        // 出停(伝染病)が1つ以上あるか？
        if (judge.isVirus(onedaySchedule)) {
            virus().add(date);
        }

        // 忌引が1つ以上あるか？
        if (judge.isMourning(onedaySchedule)) {
            mourning().add(date);
        }

        // 全て欠席の場合
        if (judge.isKesseki(onedaySchedule)) {
            final Kintai firstKintai = judge.getOnedayKintai(onedaySchedule);
            if (KintaiManager.isSick(firstKintai)) {
                sick().add(date);
            } else  if (KintaiManager.isNotice(firstKintai)) {
                notice().add(date);
            } else if (KintaiManager.isNoNotice(firstKintai)) {
                nonotice().add(date);
            }
            _absenceDates.add(date);
        }

        // [遅刻/早退]をカウントする?
        final boolean isCountLateEarly = judge.isCountLateEarly(onedaySchedule);
        final boolean isLate = judge.isLate(onedaySchedule);
        final boolean isLateNonotice = judge.isLateNonotice(onedaySchedule);
        final boolean isEarly = judge.isEarly(onedaySchedule);
        final boolean isEarlyNonotice = judge.isEarlyNonotice(onedaySchedule);
        if (isTrace) {
            log.info("★" + date + "isCountLateEarly " + isCountLateEarly);
            if (isCountLateEarly) {
                log.info("★" + date + "isLate " + isLate);
                log.info("★" + date + "isLateNonotice " + isLateNonotice);
                log.info("★" + date + "isEarly " + isEarly);
                log.info("★" + date + "isEarlyNonotice " + isEarlyNonotice);
            }
        }
        if (isCountLateEarly) {
            // [遅刻/早退]をカウントするなら遅刻・早退のチェック
            if (isLate) {
                late().add(date);
            } else if (isLateNonotice) {
                lateNonotice().add(date);
            }
            if (isEarly) {
                early().add(date);
            } else if (isEarlyNonotice) {
                earlyNonotice().add(date);
            }
        }

        // 単コマ遅刻回数を得る
        lateDetail().addAll(new CollectionWrapper<Kintai>(onedaySchedule.getLateDetail()));

        // 欠課時数を得る
        kekkaJisu().addAll(new CollectionWrapper<Kintai>(onedaySchedule.getKekkaJisu()));

        // 欠課回数を得る
        if (!judge.isKesseki(onedaySchedule)) {
            kekka().addAll(new CollectionWrapper<Kintai>(onedaySchedule.getKekka()));
        }
    }

    private final Map<Integer, CollectionWrapper<KenjaDateImpl>> _wrappers = new HashMap<Integer, CollectionWrapper<KenjaDateImpl>>();
    private final Map<Integer, CollectionWrapper<Kintai>> _wrappersC = new HashMap<Integer, CollectionWrapper<Kintai>>();

    protected CollectionWrapper<KenjaDateImpl> wrapperFor(final int kintaiCode0) {
        final Integer kintaiCode = new Integer(kintaiCode0);
        if (null == _wrappers.get(kintaiCode)) {
            _wrappers.put(kintaiCode, new CollectionWrapper<KenjaDateImpl>());
        }
        return _wrappers.get(kintaiCode);
    }

    protected CollectionWrapper<Kintai> wrapperForC(final int kintaiCode0) {
        final Integer kintaiCode = new Integer(kintaiCode0);
        if (null == _wrappersC.get(kintaiCode)) {
            _wrappersC.put(kintaiCode, new CollectionWrapper<Kintai>());
        }
        return _wrappersC.get(kintaiCode);
    }


    /**
     * 回数出席/授業日数を得る。
     * @return 回数出席/授業日数
     */
    public Collection<KenjaDateImpl> getLesson() {
        return lesson().forAdd();
    }

    /**
     * 休学の数を得る。
     * @return 休学の数
     */
    public Collection<KenjaDateImpl> getOffdays() {
        return offdays().forAdd();
    }

    /**
     * 留学の数を得る。
     * @return 留学の数
     */
    public Collection<KenjaDateImpl> getAbroad() {
        return abroad().forAdd();
    }

    /**
     * 公欠の数を得る。
     * @return 公欠
     */
    public Collection<KenjaDateImpl> getAbsent() {
        return absent().forAdd();
    }

    /**
     * 出停の数を得る。
     * @return 出停の数
     */
    public Collection<KenjaDateImpl> getSuspend() {
        return suspend().forAdd();
    }

    /**
     * 忌引の数を得る。
     * @return 忌引の数
     */
    public Collection<KenjaDateImpl> getMourning() {
        return mourning().forAdd();
    }

    /**
     * 病欠の数を得る。
     * @return 病欠の数
     */
    public Collection<KenjaDateImpl> getSick() {
        return sick().forAdd();
    }

    /**
     * 事故欠(届)の数を得る。
     * @return 事故欠(届)の数
     */
    public Collection<KenjaDateImpl> getNotice() {
        return notice().forAdd();
    }

    /**
     * 事故欠(無)の数を得る。
     * @return 事故欠(無)の数
     */
    public Collection<KenjaDateImpl> getNonotice() {
        return nonotice().forAdd();
    }

    /**
     * 遅刻の数を得る。
     * @return 遅刻の数
     */
    public Collection<KenjaDateImpl> getLate() {
        return late().forAdd();
    }

    /**
     * 早退の数を得る。
     * @return 早退の数
     */
    public Collection<KenjaDateImpl> getEarly() {
        return early().forAdd();
    }

    /**
     * 出停(伝染病)の数を得る。
     * @return 出停(伝染病)の数
     */
    public Collection<KenjaDateImpl> getVirus() {
        return virus().forAdd();
    }

    /**
     * 出停(交止)の数を得る。
     * @return 出停(伝染病)の数
     */
    public Collection<KenjaDateImpl> getKoudome() {
        return koudome().forAdd();
    }

    /**
     * 遅刻(無)の数を得る。
     * @return 遅刻(無)の数
     */
    public Collection<KenjaDateImpl> getLateNonotice() {
        return lateNonotice().forAdd();
    }

    /**
     * 早退(無)の数を得る。
     * @return 早退(無)の数
     */
    public Collection<KenjaDateImpl> getEarlyNonotice() {
        return earlyNonotice().forAdd();
    }

    /**
     * 欠課時数を得る。
     * @return 欠課時数
     */
    public Collection<Kintai> getKekkaJisu() {
        return kekkaJisu().forAdd();
    }

    /**
     * 欠課回数を得る。
     * @return 欠課回数
     */
    public Collection<Kintai> getKekka() {
        return kekka().forAdd();
    }

    /**
     * 単コマ遅刻回数を得る。
     * @return 単コマ遅刻回数
     */
    public Collection<Kintai> getLateDetail() {
        return lateDetail().forAdd();
    }

    protected CollectionWrapper<KenjaDateImpl> lesson() {
        return wrapperFor(CODE_LESSON);
    }

    protected CollectionWrapper<KenjaDateImpl> offdays() {
        return wrapperFor(CODE_OFFDAYS);
    }

    protected CollectionWrapper<KenjaDateImpl> abroad() {
        return wrapperFor(CODE_ABROAD);
    }

    protected CollectionWrapper<KenjaDateImpl> absent() {
        return wrapperFor(KintaiManager.CODE_ABSENT);
    }

    protected CollectionWrapper<KenjaDateImpl> suspend() {
        return wrapperFor(KintaiManager.CODE_SUSPEND);
    }

    protected CollectionWrapper<KenjaDateImpl> mourning() {
        return wrapperFor(KintaiManager.CODE_MOURNING);
    }

    protected CollectionWrapper<KenjaDateImpl> sick() {
        return wrapperFor(KintaiManager.CODE_SICK);
    }

    protected CollectionWrapper<KenjaDateImpl> notice() {
        return wrapperFor(KintaiManager.CODE_NOTICE);
    }

    protected CollectionWrapper<KenjaDateImpl> nonotice() {
        return wrapperFor(KintaiManager.CODE_NONOTICE);
    }

    protected CollectionWrapper<KenjaDateImpl> late() {
        return wrapperFor(KintaiManager.CODE_LATE);
    }

    protected CollectionWrapper<KenjaDateImpl> early() {
        return wrapperFor(KintaiManager.CODE_EARLY);
    }

    protected CollectionWrapper<KenjaDateImpl> virus() {
        return wrapperFor(KintaiManager.CODE_VIRUS);
    }

    protected CollectionWrapper<KenjaDateImpl> koudome() {
        return wrapperFor(KintaiManager.CODE_KOUDOME);
    }

    protected CollectionWrapper<KenjaDateImpl> lateNonotice() {
        return wrapperFor(KintaiManager.CODE_LATE_NONOTICE);
    }

    protected CollectionWrapper<KenjaDateImpl> earlyNonotice() {
        return wrapperFor(KintaiManager.CODE_EARLY_NONOTICE);
    }

    /**
     * 欠課時数を得る。
     * @return 欠課時数
     */
    private CollectionWrapper<Kintai> kekkaJisu() {
        return wrapperForC(CODE_KEKKA_JISU);
    }

    /**
     * 欠課回数を得る。
     * @return 欠課回数
     */
    private CollectionWrapper<Kintai> kekka() {
        return wrapperForC(CODE_KEKKA);
    }

    /**
     * 単コマ遅刻回数を得る。
     * @return 単コマ遅刻回数
     */
    private CollectionWrapper<Kintai> lateDetail() {
        return wrapperForC(CODE_LATEDETAIL);
    }

    /**
     * 累積を加算する。
     * @param that 累積
     */
    public void add(final AccumulateSemes that) {
        for (final Map.Entry<Integer, CollectionWrapper<KenjaDateImpl>> e : that._wrappers.entrySet()) {
            final Integer key = e.getKey();
            wrapperFor(key.intValue()).addAll(e.getValue());
        }
        for (final Map.Entry<Integer, CollectionWrapper<Kintai>> e : that._wrappersC.entrySet()) {
            final Integer key = e.getKey();
            wrapperForC(key.intValue()).addAll(e.getValue());
        }
        _absenceDates.addAll(that._absenceDates);
    }

    /**
     * 欠席日付を追加する。
     * @return 欠席日付
     */
    public Set<KenjaDateImpl> getAttendAbsenceDate() {
        return _absenceDates;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Les:" + getLesson() + ", "
                + "Off:" + getOffdays() + ", "
                + "Abr:" + getAbroad() + ", "
                + "Abs:" + getAbsent() + ", "
                + "Sus:" + getSuspend() + ", "
                + "Mou:" + getMourning() + ", "
                + "Sic:" + getSick() + ", "
                + "Not:" + getNotice() + ", "
                + "Non:" + getNonotice() + ", "
                + "Lat:" + getLate() + ", "
                + "Ear:" + getEarly() + ", "
                + "Kej:" + getKekkaJisu() + ", "
                + "Kek:" + getKekka() + ", "
                + "Det:" + getLateDetail() + ", "
                + "Vir:" + getVirus();
    }
} // AccumulateSemes

// eof
