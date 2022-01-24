// kanji=漢字
/*
 * $Id: BaseDay.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2007/01/20 16:17:03 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate.option;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

import jp.co.alp.kenja.batch.domain.DateUtils;
import jp.co.alp.kenja.batch.domain.Term;

/**
 * 基準日。
 * @author takaesu
 * @version $Id: BaseDay.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class BaseDay {
    private AbstractBaseDay _abd;

    private void setBaseDay(final int mode) {
        if (1 == mode) {
            _abd = new BaseDayNormal();
        } else {
            _abd = new BaseDayKindai();
        }
    }

    /**
     * インスタンスを得る。
     * @param mode モード
     * @return インスタンス
     */
    public static BaseDay getInstance(final int mode) {
        final BaseDay baseDay = new BaseDay();
        baseDay.setBaseDay(mode);
        return baseDay;
    }

    /**
     * 月を得る。
     * @param date 日付
     * @return 月
     */
    public String getMonthAsString(final KenjaDateImpl date) {
        return _abd.getMonthAsString(date);
    }

    /**
     * 期間を得る。
     * @param date 日付
     * @return 期間
     */
    public Term getTerm(final KenjaDateImpl date) {
        return _abd.getTerm(date);
    }

    /**
     * 開始日を得る。
     * @param date 日付
     * @return 開始日
     */
    public KenjaDateImpl getStartDate(final KenjaDateImpl date) {
        return _abd.getStartDate(date);
    }

    /**
     * 通常モードか？
     * @return 通常モードなら true
     */
    public boolean isNormal() {
        return _abd.isNormal();
    }

    /**
     * 基準日。
     */
    private abstract class AbstractBaseDay {
        public abstract String getMonthAsString(final KenjaDateImpl date);
        public abstract Term getTerm(final KenjaDateImpl date);
        public abstract KenjaDateImpl getStartDate(final KenjaDateImpl date);
        public abstract boolean isNormal();

        String monthAsString(final int month) {
            final String sMonth = String.valueOf(month);
            return (month <= 9) ? "0" + sMonth : sMonth;
        }
    } // AbstractBaseDay

    /**
     * 通常の基準日。
     */
    private class BaseDayNormal extends AbstractBaseDay {
        public String getMonthAsString(final KenjaDateImpl date) {
            final int month = date.getMonth();
            return monthAsString(month);
        }

        public Term getTerm(final KenjaDateImpl date) {
            // その月の1日〜月末
            final KenjaDateImpl start = DateUtils.getFirstDate(date);
            final KenjaDateImpl last = DateUtils.getLastdayOfMonth(date);
            return new Term(start, last);
        }

        public KenjaDateImpl getStartDate(final KenjaDateImpl date) {
            // 一日を返す
            return DateUtils.getFirstDate(date);
        }

        public boolean isNormal() {
            return true;
        }
    } // BaseDayNormal

    /**
     * 近大などの基準日。
     */
    private class BaseDayKindai extends AbstractBaseDay {
        public String getMonthAsString(final KenjaDateImpl date) {
            final int month;
            if (1 == date.getDay()) {
                month = prevMonth(date.getMonth());
            } else {
                month = date.getMonth();
            }

            return monthAsString(month);
        }

        public Term getTerm(final KenjaDateImpl date) {
            // その月の2日〜翌月1日
            final KenjaDateImpl start = DateUtils.getFirstDate(date).nextDate();
            final KenjaDateImpl last = DateUtils.getLastdayOfMonth(date).nextDate();
            return new Term(start, last);
        }

        public boolean isNormal() {
            return false;
        }

        public KenjaDateImpl getStartDate(final KenjaDateImpl date) {
            // 二日(ふつか)を返す
            if (1 == date.getDay()) {
                return DateUtils.addMonthOfFirstDay(date, -1).nextDate();
            }
            return DateUtils.getFirstDate(date).nextDate();
        }

        int prevMonth(final int month) {
            if (1 == month) {
                return 12;
            }
            return month - 1;
        }
    } // BaseDayKindai
} // BaseDay

// eof
