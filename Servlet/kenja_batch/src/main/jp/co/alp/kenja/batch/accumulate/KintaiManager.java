// kanji=漢字
/*
 * $Id: KintaiManager.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/30 11:50:02 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import jp.co.alp.kenja.common.lang.enums.MyEnum.Category;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Kintai;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: KintaiManager.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class KintaiManager {
    /** 勤怠コード: 出席 */
    public static final int CODE_SEATED = 0;

    /** 勤怠コード: 公欠 */
    public static final int CODE_ABSENT = 1;

    /** 勤怠コード: 出停 */
    public static final int CODE_SUSPEND = 2;

    /** 勤怠コード: 忌引 */
    public static final int CODE_MOURNING = 3;

    /** 勤怠コード: 病欠 */
    public static final int CODE_SICK = 4;

    /** 勤怠コード: 事故欠(届) */
    public static final int CODE_NOTICE = 5;

    /** 勤怠コード: 事故欠(無) */
    public static final int CODE_NONOTICE = 6;

    /** 勤怠コード: 保健室欠課 */
    public static final int CODE_NURSEOFF = 14;

    /** 勤怠コード: 遅刻 */
    public static final int CODE_LATE = 15;

    /** 勤怠コード: 早退 */
    public static final int CODE_EARLY = 16;

    /** 勤怠コード: 出停(伝染病) */
    public static final int CODE_VIRUS = 19;

    /** 勤怠コード: 遅刻(無) */
    public static final int CODE_LATE_NONOTICE = 21;

    /** 勤怠コード: 早退(無) */
    public static final int CODE_EARLY_NONOTICE = 22;

    /** 勤怠コード: 遅刻2 */
    public static final int CODE_LATE2 = 23;

    /** 勤怠コード: 遅刻3 */
    public static final int CODE_LATE3 = 24;

    /** 勤怠コード: カウントなし(1日出欠、科目出欠) */
    public static final int CODE_NO_COUNT = 27;

    /** 勤怠コード: カウントなし(科目出欠) */
    public static final int CODE_NO_COUNT2 = 28;

    /** 勤怠コード: 出停(交止) */
    public static final int CODE_KOUDOME = 25;

    /*pkg*/static final Log log = LogFactory.getLog(KintaiManager.class);

    private final Kintai _seated;
    private final Kintai _absent;
    private final Kintai _suspend;
    private final Kintai _mourning;
    private final Kintai _sick;
    private final Kintai _notice;
    private final Kintai _nonotice;
    private final Kintai _nurseoff;
    private final Kintai _late;
    private final Kintai _early;
    private final Kintai _virus;
    private final Kintai _koudome;
    private final Kintai _lateNonotice;
    private final Kintai _earlyNonotice;
    private final Kintai _late2;
    private final Kintai _late3;
    private final Kintai _noCount;
    private final Kintai _noCount2;

    /**
     * コンストラクタ。
     * @param seated 出席
     * @param absent 公欠
     * @param suspend 出停
     * @param mourning 忌引
     * @param sick 病欠
     * @param notice 事故欠(届)
     * @param nonotice 事故欠(無)
     * @param nurseoff 保健室欠課
     * @param late 遅刻
     * @param early 早退
     * @param virus 出停(伝染病)
     * @param koudome 出停(交止)
     * @param lateNonotice 遅刻(無)
     * @param earlyNonotice 早退(無)
     * @param late2 遅刻2
     * @param late3 遅刻3
     * @param noCount カウントなし
     * @param noCount2 カウントなし2
     */
    public KintaiManager(
            final Kintai seated,
            final Kintai absent,
            final Kintai suspend,
            final Kintai mourning,
            final Kintai sick,
            final Kintai notice,
            final Kintai nonotice,
            final Kintai nurseoff,
            final Kintai late,
            final Kintai early,
            final Kintai virus,
            final Kintai koudome,
            final Kintai lateNonotice,
            final Kintai earlyNonotice,
            final Kintai late2,
            final Kintai late3,
            final Kintai noCount,
            final Kintai noCount2
    ) {
        _seated = seated;
        _absent = absent;
        _suspend = suspend;
        _mourning = mourning;
        _sick = sick;
        _notice = notice;
        _nonotice = nonotice;
        _nurseoff = nurseoff;
        _late = late;
        _early = early;
        _virus = virus;
        _koudome = koudome;
        _lateNonotice = lateNonotice;
        _earlyNonotice = earlyNonotice;
        _late2 = late2;
        _late3 = late3;
        _noCount = noCount;
        _noCount2 = noCount2;
    }

    /**
     * コンストラクタ。
     * @param category
     */
    public KintaiManager(
            final Category category
    ) {
        this(
                Kintai.getInstance(category, KintaiManager.CODE_SEATED),
                Kintai.getInstance(category, KintaiManager.CODE_ABSENT),
                Kintai.getInstance(category, KintaiManager.CODE_SUSPEND),
                Kintai.getInstance(category, KintaiManager.CODE_MOURNING),
                Kintai.getInstance(category, KintaiManager.CODE_SICK),
                Kintai.getInstance(category, KintaiManager.CODE_NOTICE),
                Kintai.getInstance(category, KintaiManager.CODE_NONOTICE),
                Kintai.getInstance(category, KintaiManager.CODE_NURSEOFF),
                Kintai.getInstance(category, KintaiManager.CODE_LATE),
                Kintai.getInstance(category, KintaiManager.CODE_EARLY),
                Kintai.getInstance(category, KintaiManager.CODE_VIRUS),
                Kintai.getInstance(category, KintaiManager.CODE_KOUDOME),
                Kintai.getInstance(category, KintaiManager.CODE_LATE_NONOTICE),
                Kintai.getInstance(category, KintaiManager.CODE_EARLY_NONOTICE),
                Kintai.getInstance(category, KintaiManager.CODE_LATE2),
                Kintai.getInstance(category, KintaiManager.CODE_LATE3),
                Kintai.getInstance(category, KintaiManager.CODE_NO_COUNT),
                Kintai.getInstance(category, KintaiManager.CODE_NO_COUNT2)
                );
    }

    /**
     * 勤怠を得る。
     * @return 出席
     */
    public Kintai seated() {
        return _seated;
    }

    /**
     * 勤怠を得る。
     * @return 公欠
     */
    public Kintai absent() {
        return _absent;
    }

    /**
     * 勤怠を得る。
     * @return 出停
     */
    public Kintai suspend() {
        return _suspend;
    }

    /**
     * 勤怠を得る。
     * @return 忌引
     */
    public Kintai mourning() {
        return _mourning;
    }

    /**
     * 勤怠を得る。
     * @return 病欠
     */
    public Kintai sick() {
        return _sick;
    }

    /**
     * 勤怠を得る。
     * @return 事故欠(届)
     */
    public Kintai notice() {
        return _notice;
    }

    /**
     * 勤怠を得る。
     * @return 事故欠(無)
     */
    public Kintai nonotice() {
        return _nonotice;
    }

    /**
     * 勤怠を得る。
     * @return 保健室欠課
     */
    public Kintai nurseoff() {
        return _nurseoff;
    }

    /**
     * 勤怠を得る。
     * @return 遅刻
     */
    public Kintai late() {
        return _late;
    }

    /**
     * 勤怠を得る。
     * @return 早退
     */
    public Kintai early() {
        return _early;
    }

    /**
     * 勤怠を得る。
     * @return 公欠
     */
    public Kintai virus() {
        return _virus;
    }

    /**
     * 勤怠を得る。
     * @return 校止
     */
    public Kintai koudome() {
        return _koudome;
    }

    /**
     * 勤怠を得る。
     * @return 遅刻(無)
     */
    public Kintai lateNonotice() {
        return _lateNonotice;
    }

    /**
     * 勤怠を得る。
     * @return 早退(無)
     */
    public Kintai earlyNonotice() {
        return _earlyNonotice;
    }

    /**
     * 勤怠を得る。
     * @return 遅刻2
     */
    public Kintai late2() {
        return _late2;
    }

    /**
     * 勤怠を得る。
     * @return 遅刻3
     */
    public Kintai late3() {
        return _late3;
    }

    /**
     * 勤怠を得る。
     * @return カウントなし
     */
    public Kintai noCount() {
        return _noCount;
    }

    /**
     * 勤怠を得る。
     * @return カウントなし2
     */
    public Kintai noCount2() {
        return _noCount2;
    }

    /**
     * 勤怠が指定コードか？
     * @param kintai 勤怠
     * @return 指定コードならtrue
     */
    public static boolean is(final Kintai kintai, final int code) {
        return null != kintai && kintai.getAltCode().intValue() == code;
    }

    /**
     * 病欠か？
     * @param kintai 勤怠
     * @return 病欠ならtrue
     */
    public static boolean isSick(final Kintai kintai) {
        return is(kintai, CODE_SICK);
    }

    /**
     * 事故欠(届)か？
     * @param kintai 勤怠
     * @return 事故欠(届)ならtrue
     */
    public static boolean isNotice(final Kintai kintai) {
        return is(kintai, CODE_NOTICE);
    }

    /**
     * 事故欠(無)か？
     * @param kintai 勤怠
     * @return 事故欠(無)ならtrue
     */
    public static boolean isNoNotice(final Kintai kintai) {
        return is(kintai, CODE_NONOTICE);
    }

    /**
     * 遅刻か？
     * @param kintai 勤怠
     * @return 遅刻ならtrue
     */
    public static boolean isLate(final Kintai kintai) {
        return is(kintai, CODE_LATE);
    }

    /**
     * 早退か？
     * @param kintai 勤怠
     * @return 早退ならtrue
     */
    public static boolean isEarly(final Kintai kintai) {
        return is(kintai, CODE_EARLY);
    }

    /**
     * 出停(伝染病)か？
     * @param kintai 勤怠
     * @return 出停(伝染病)ならtrue
     */
    public static boolean isVirus(final Kintai kintai) {
        return is(kintai, CODE_VIRUS);
    }

    /**
     * 遅刻(無)か？
     * @param kintai 勤怠
     * @return 遅刻(無)ならtrue
     */
    public static boolean isLateNonotice(final Kintai kintai) {
        return is(kintai, CODE_LATE_NONOTICE);
    }

    /**
     * 早退か(無)？
     * @param kintai 勤怠
     * @return 早退(無)ならtrue
     */
    public static boolean isEarlyNonotice(final Kintai kintai) {
        return is(kintai, CODE_EARLY_NONOTICE);
    }

    /**
     * 遅刻2か？
     * @param kintai 勤怠
     * @return 遅刻2ならtrue
     */
    public static boolean isLate2(final Kintai kintai) {
        return is(kintai, CODE_LATE2);
    }

    /**
     * 遅刻3か？
     * @param kintai 勤怠
     * @return 遅刻3ならtrue
     */
    public static boolean isLate3(final Kintai kintai) {
        return is(kintai, CODE_LATE3);
    }

    /**
     * カウントなし？
     * @param kintai 勤怠
     * @return カウントなしならtrue
     */
    public static boolean isNoCount(final Kintai kintai) {
        return is(kintai, CODE_NO_COUNT);
    }

    /**
     * カウントなし2？
     * @param kintai 勤怠
     * @return カウントなし2ならtrue
     */
    public static boolean isNoCount2(final Kintai kintai) {
        return is(kintai, CODE_NO_COUNT2);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return seated().toString() + ", "
            + absent().toString() + ", "
            + suspend().toString() + ", "
            + mourning().toString() + ", "
            + sick().toString() + ", "
            + notice().toString() + ", "
            + nonotice().toString() + ", "
            + late().toString() + ", "
            + early().toString() + ", "
            + virus().toString() + ", "
            + lateNonotice().toString() + ", "
            + earlyNonotice().toString() + ", "
            + late2().toString() + ", "
            + late3().toString();
    }
} // KintaiManager

// eof
