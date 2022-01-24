/*
 * $Id: SubclassAbsenceHighSpecial.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.math.BigDecimal;

/**
 * 生徒毎の科目グループの欠課上限値
 * @version $Id: SubclassAbsenceHighSpecial.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class SubclassAbsenceHighSpecial {
    /** NULL オブジェクト */
    public static final SubclassAbsenceHighSpecial NULL = new SubclassAbsenceHighSpecial(null);

    /** 科目グループ */
    private final String _specialGroupCd;

    /** 授業時分 */
    private int _lessonMinutes;

    /** 出席すべき授業時分 */
    private int _lessonMustBeAttendedMinutes;

    /**
     * コンストラクタ
     * @param subClassGroupCd 科目グループ
     */
    public SubclassAbsenceHighSpecial(final String subClassGroupCd) {
        _specialGroupCd = subClassGroupCd;
        _lessonMinutes = 0;
        _lessonMustBeAttendedMinutes = 0;
    }

    /**
     * 科目グループコードを得る。
     * @return 科目グループコード
     */
    public String getSpecialGroupCd() {
        return _specialGroupCd;
    }

    /**
     * 授業時分を追加する。
     * @param lessonMinutes 追加する授業時分
     */
    public void plusLessonMinutes(final int lessonMinutes) {
        _lessonMinutes += lessonMinutes;
    }

    /**
     * 出席すべき授業時分を追加する。
     * @param lessonMinutes 追加する授業時分
     */
    public void plusLessonMustBeAttendedMinutes(final int lessonMinutes) {
        _lessonMustBeAttendedMinutes += lessonMinutes;
    }

    /**
     * 授業時分を得る。
     * @return 授業時分
     */
    public int getLessonMinutes() {
        return _lessonMinutes;
    }

    /**
     * 出席すべき授業時分を得る。
     * @return 出席すべき授業時分
     */
    public int getLessonMustBeAttendedMinutes() {
        return _lessonMustBeAttendedMinutes;
    }

    /**
     * 授業時数を得る。
     * @param jituJihunArg 1時限あたりの分数
     * @return 授業時数
     */
    public int getLesson(final int jituJihunArg) {
        return calcLesson(_lessonMinutes, jituJihunArg);
    }

    /**
     * 出席すべき授業時数を得る。
     * @param jituJihunArg 1時限あたりの分数
     * @return 出席すべき授業時数
     */
    public int getLessonMustBeAttended(final int jituJihunArg) {
        return calcLesson(_lessonMustBeAttendedMinutes, jituJihunArg);
    }

    private static int calcLesson(final int lessonMinutes, final int jituJihunArg) {
        final int jituJihun = (jituJihunArg == 0) ? 50 : jituJihunArg;
        final BigDecimal lesson = new BigDecimal(lessonMinutes).divide(new BigDecimal(jituJihun), 0, BigDecimal.ROUND_HALF_UP);
        return lesson.intValue();
    }

    /**
     * 欠課上限値を得る。
     * @param lesson 授業時数
     * @param numerator 欠課上限値分子
     * @param denominator 欠課上限値分母
     * @param scale 指定桁
     * @param rounding 丸め法
     * @return 欠課上限値
     */
    public static BigDecimal getAbsenceHighSpecial(final int lesson, final int numerator, final int denominator, final int scale, final int rounding) {
        final BigDecimal bgNumerator = new BigDecimal(lesson * numerator);
        final BigDecimal bdDenominator = (denominator == 0) ? new BigDecimal(1) : new BigDecimal(denominator);
        final BigDecimal absenceHighSpecial = bgNumerator.divide(bdDenominator, scale, rounding).setScale(1);
        return absenceHighSpecial;
    }
}
