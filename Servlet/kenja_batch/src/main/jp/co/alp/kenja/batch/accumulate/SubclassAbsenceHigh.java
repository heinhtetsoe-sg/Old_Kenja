/*
 * $Id: SubclassAbsenceHigh.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.math.BigDecimal;

import jp.co.alp.kenja.common.domain.SubClass;

/**
 * 生徒毎の科目の欠課上限値
 * @version $Id: SubclassAbsenceHigh.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class SubclassAbsenceHigh {
    /** NULL オブジェクト */
    public static final SubclassAbsenceHigh NULL = new SubclassAbsenceHigh(null);

    /** 科目 */
    private final SubClass _subClass;

    /** 授業時数 */
    private int _lessonCount;

    /** 出席すべき授業時数 */
    private int _lessonCountMustBeAttended;

    /**
     * コンストラクタ
     * @param subClass 科目
     */
    public SubclassAbsenceHigh(final SubClass subClass) {
        _subClass = subClass;
        _lessonCount = 0;
        _lessonCountMustBeAttended = 0;
    }

    public SubClass getSubClass() {
        return _subClass;
    }

    /**
     * 授業時数を追加する。
     * @param lessonCount 加える授業時数
     */
    public void plusLesson(final int lessonCount) {
        _lessonCount += lessonCount;
    }

    /**
     * 出席すべき授業時数を追加する。
     * @param lessonCount 加える授業時数
     */
    public void plusLessonMustBeAttended(final int lessonCount) {
        _lessonCountMustBeAttended += lessonCount;
    }

    /**
     * 授業時数を得る。
     * @return 授業時数
     */
    public int getLessonCount() {
        return _lessonCount;
    }

    /**
     * 出席すべき授業時数を得る。
     * @return 出席すべき授業時数
     */
    public int getLessonMustBeAttendedCount() {
        return _lessonCountMustBeAttended;
    }

    /**
     * 欠課上限値を得る。
     * @param numerator 欠課上限値分子
     * @param denominator 欠課上限値分母
     * @param scale 指定桁
     * @param rounding 丸め法
     * @return 欠課上限値
     */
    public BigDecimal getAbsenceHigh(final int numerator, final int denominator, final int scale, final int rounding) {
        final BigDecimal bgNumerator = new BigDecimal(_lessonCountMustBeAttended * numerator);
        final BigDecimal bdDenominator = (denominator == 0) ? new BigDecimal(1) : new BigDecimal(denominator);
        return bgNumerator.divide(bdDenominator, scale, rounding).setScale(1);
    }
}
