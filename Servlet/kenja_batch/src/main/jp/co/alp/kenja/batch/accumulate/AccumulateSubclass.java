// kanji=漢字
/*
 * $Id: AccumulateSubclass.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/10/19 13:53:38 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.domain.HogeUtils;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.Student.TransferCd;
import jp.co.alp.kenja.common.util.CollectionWrapper;

/**
 * AccumulateSubclass。
 * @author takaesu
 * @version $Id: AccumulateSubclass.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class AccumulateSubclass {

    private static Log log = LogFactory.getLog(AccumulateSubclass.class);

    protected static final int CODE_LESSON = 99999;
    protected static final int CODE_OFFDAYS = 99998;
    protected static final int CODE_ABROAD = 99997;
    
    /**
     * 計算する。
     * @param student 生徒
     * @param attendance 出欠
     */
    // CSOFF: Cyclomatic Complexity
    public void calc(
            final Student student,
            final Attendance attendance
    ) {
        final int kintaiCode = attendance.getKintai().getAltCode();
        if (kintaiCode == KintaiManager.CODE_NO_COUNT || kintaiCode == KintaiManager.CODE_NO_COUNT2) {
            log.info("時間割カウントなし: schregno = " + student.getCode() + ", date = " + attendance.getDate() + ", period = " + attendance.getPeriod());
            return;
        }
        list(CODE_LESSON).add(attendance);

        if (!student.isActive(attendance.getDate())) {
            final TransferCd transferCd = HogeUtils.getTransferCd(student, attendance.getDate());
            if (TransferCd.TAKE_OFF_SCHOOL.equals(transferCd)) {
                list(AccumulateSubclass.CODE_OFFDAYS).add(attendance);
                return;
            } else  if (TransferCd.STUDY_ABROAD.equals(transferCd)) {
                list(AccumulateSubclass.CODE_ABROAD).add(attendance);
                return;
            }
        }

        final Collection<Attendance> attendances;
        switch (kintaiCode) {
        case KintaiManager.CODE_LATE2:
        case KintaiManager.CODE_LATE3:
            attendances = new ArrayList<Attendance>();
            attendances.add(attendance);
            for (int i = 1, c = AccumulateKintaiInfo.getInstance().getCount(attendance.getKintai()); i < c; i++) {
                attendances.add(attendance);
            }
            break;
        default:
            attendances = Collections.singleton(attendance);
            break;
        }
        final CollectionWrapper<Attendance> tgt;
        switch (kintaiCode) {
            case KintaiManager.CODE_ABSENT:
            case KintaiManager.CODE_SUSPEND:
            case KintaiManager.CODE_MOURNING:
            case KintaiManager.CODE_SICK:
            case KintaiManager.CODE_NOTICE:
            case KintaiManager.CODE_NONOTICE:
            case KintaiManager.CODE_NURSEOFF:
            case KintaiManager.CODE_LATE:
            case KintaiManager.CODE_EARLY:
            case KintaiManager.CODE_VIRUS:
            case KintaiManager.CODE_LATE_NONOTICE:
            case KintaiManager.CODE_EARLY_NONOTICE:
                tgt = list(kintaiCode);
                break;
            case KintaiManager.CODE_LATE2:
            case KintaiManager.CODE_LATE3:
                tgt = list(KintaiManager.CODE_LATE);
                break;
            default:
                tgt = null;
                break;
        }
        if (null != tgt) {
            tgt.addAll(new CollectionWrapper<Attendance>(attendances));
        }
    }

    private final Map<Integer, CollectionWrapper<Attendance>> _wrappers = new HashMap<Integer, CollectionWrapper<Attendance>>();

    private CollectionWrapper<Attendance> list(final int kintaiCode0) {
        final Integer kintaiCode = new Integer(kintaiCode0);
        if (null == _wrappers.get(kintaiCode)) {
            _wrappers.put(kintaiCode, new CollectionWrapper<Attendance>());
        }
        return _wrappers.get(kintaiCode);
    }

    private int count(final int kintaicode) {
        boolean hasPeriodMinutes = false;
        for (final Attendance at : list(kintaicode).forAdd()) {
            if (null != at.getPeriod().getPeriodMinutes()) {
                hasPeriodMinutes = true;
                break;
            }
        }
        if (hasPeriodMinutes) {
            Integer jituJifun = BatchSchoolMaster.getBatchSchoolMaster().getJituJifun();
            if (null == jituJifun) {
                jituJifun = 50;
            }
            final Map<Integer, Integer> histgram = new TreeMap<Integer, Integer>();
            for (final Attendance at : list(kintaicode).forAdd()) {
                Integer periodMinutes = at.getPeriod().getPeriodMinutes();
                if (null == periodMinutes) {
                    periodMinutes = jituJifun;
                }
                histgram.put(periodMinutes, (!histgram.containsKey(periodMinutes) ? 0 : histgram.get(periodMinutes)) + 1);
            }
            BigDecimal totalMinutes = BigDecimal.ZERO;
            for (final Map.Entry<Integer, Integer> e : histgram.entrySet()) {
                final Integer periodMinutes = e.getKey();
                final Integer count = e.getValue();
                totalMinutes = totalMinutes.add(BigDecimal.valueOf(periodMinutes).multiply(BigDecimal.valueOf(count)));
            }
            final int jisu = totalMinutes.divide(new BigDecimal(jituJifun), 0, BigDecimal.ROUND_UP).intValue();
            log.debug(" totalMinutes = " + totalMinutes + ", jisu = " + jisu + " (" + totalMinutes.divide(new BigDecimal(jituJifun), 2, BigDecimal.ROUND_UP) + "), histgram " + histgram);
            return jisu;
        }
        return list(kintaicode).size();
    }
    
    /**
     * 回数出席/授業日数を得る。
     * @return 回数出席/授業日数
     */
    public int getLesson() {
        return count(CODE_LESSON);
    }

    /**
     * 休学の数を得る。
     * @return 休学の数
     */
    public int getOffdays() {
        return count(CODE_OFFDAYS);
    }

    /**
     * 留学の数を得る。
     * @return 留学の数
     */
    public int getAbroad() {
        return count(CODE_ABROAD);
    }
    
    /**
     * 公欠の数を得る。
     * @return 公欠
     */
    public int getAbsent() {
        return count(KintaiManager.CODE_ABSENT);
    }

    /**
     * 出停の数を得る。
     * @return 出停の数
     */
    public int getSuspend() {
        return count(KintaiManager.CODE_SUSPEND);
    }

    /**
     * 忌引の数を得る。
     * @return 忌引の数
     */
    public int getMourning() {
        return count(KintaiManager.CODE_MOURNING);
    }

    /**
     * 病欠の数を得る。
     * @return 病欠の数
     */
    public int getSick() {
        return count(KintaiManager.CODE_SICK);
    }

    /**
     * 事故欠(届)の数を得る。
     * @return 事故欠(届)の数
     */
    public int getNotice() {
        return count(KintaiManager.CODE_NOTICE);
    }

    /**
     * 事故欠(無)の数を得る。
     * @return 事故欠(無)の数
     */
    public int getNonotice() {
        return count(KintaiManager.CODE_NONOTICE);
    }

    /**
     * 遅刻の数を得る。
     * @return 遅刻の数
     */
    public int getLate() {
        return count(KintaiManager.CODE_LATE);
    }

    /**
     * 早退の数を得る。
     * @return 早退の数
     */
    public int getEarly() {
        return count(KintaiManager.CODE_EARLY);
    }

    /**
     * 出停(伝染病)の数を得る。
     * @return 出停(伝染病)の数
     */
    public int getVirus() {
        return count(KintaiManager.CODE_VIRUS);
    }

    /**
     * 出停(交止)の数を得る。
     * @return 出停(伝染病)の数
     */
    public int getKoudome() {
        return count(KintaiManager.CODE_KOUDOME);
    }

    /**
     * 遅刻(無)の数を得る。
     * @return 遅刻(無)の数
     */
    public int getLateNonotice() {
        return count(KintaiManager.CODE_LATE_NONOTICE);
    }

    /**
     * 早退(無)の数を得る。
     * @return 早退(無)の数
     */
    public int getEarlyNonotice() {
        return count(KintaiManager.CODE_EARLY_NONOTICE);
    }

    /**
     * 回数保健室欠課を得る。
     * @return 回数保健室欠課
     */
    public int getNurseoff() {
        return count(KintaiManager.CODE_NURSEOFF);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Les:" + list(CODE_LESSON) + ", "
                + "Off:" + list(CODE_OFFDAYS) + ", "
                + "Abr:" + list(CODE_ABROAD) + ", "
                + "Abs:" + list(KintaiManager.CODE_ABSENT) + ", "
                + "Sus:" + list(KintaiManager.CODE_SUSPEND) + ", "
                + "Mou:" + list(KintaiManager.CODE_MOURNING) + ", "
                + "Sic:" + list(KintaiManager.CODE_SICK) + ", "
                + "Not:" + list(KintaiManager.CODE_NOTICE) + ", "
                + "Non:" + list(KintaiManager.CODE_NONOTICE) + ", "
                + "Nur:" + list(KintaiManager.CODE_NURSEOFF) + ", "
                + "Lat:" + list(KintaiManager.CODE_LATE) + ", "
                + "Ear:" + list(KintaiManager.CODE_EARLY) + ", "
                + "Vir:" + list(KintaiManager.CODE_VIRUS);
    }
} // AccumulateSubclass
