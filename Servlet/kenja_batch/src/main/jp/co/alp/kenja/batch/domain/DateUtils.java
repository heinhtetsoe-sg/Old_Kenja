// kanji=漢字
/*
 * $Id: DateUtils.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/14 16:46:06 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: DateUtils.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DateUtils {
    private DateUtils() {
    }

    /**
     * 2つの日付から、何ヶ月の差かを得るメソッド。（当月含む)
     * @param date1 日付
     * @param date2 日付
     * @return 月の差
     */
    public static int monthCount(final KenjaDateImpl date1, final KenjaDateImpl date2) {
        final KenjaDateImpl a;
        final KenjaDateImpl b;

        a = min(date1, date2);
        b = date1.max(date2);

        if (a.getYear() == b.getYear()) {
            return b.getMonth() - a.getMonth() + 1;
        }

        final int n1 = ((b.getYear() - 1) - a.getYear()) * 12;   // 年間の差
        final int n2 = 12 - a.getMonth() + 1;
        return n1 + n2 + b.getMonth();
    }

    /**
     * 両者の年月が同じか？
     * @param date1 日付
     * @param date2 日付
     * @return 同じなら<code>true</code>
     */
    public static boolean isSameYearMonth(final KenjaDateImpl date1, final KenjaDateImpl date2) {
        if (date1.getYear() != date2.getYear()) {
            return false;
        }
        return date1.getMonth() == date2.getMonth();
    }

    /**
     * 指定日の一日(ついたち)の日付を得る。
     * @param date 日付
     * @return 指定日の一日(ついたち)の日付
     */
    public static KenjaDateImpl getFirstDate(final KenjaDateImpl date) {
        return addMonthOfFirstDay(date, 0);
    }

    /**
     * 月末の日付を得る。
     * @param date 日付
     * @return 月末の日付
     */
    public static KenjaDateImpl getLastdayOfMonth(final KenjaDateImpl date) {
        final Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth() - 1, 1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, -1);
        return KenjaDateImpl.getInstance(cal);
    }

    /**
     * 月の加減算を行う。
     * 日は1日(ついたち)とする。<br>
     * 例1) 2006/11/10 + 1 = 2006/12/1<br>
     * 例2) 2006/11/10 + 2 = 2007/1/1
     * @param p 年月日
     * @param n 月数
     * @return p に nヶ月を加算した年月の1日の日付
     */
    public static KenjaDateImpl addMonthOfFirstDay(final KenjaDateImpl p, final int n) {
        final Calendar cal = Calendar.getInstance();
        cal.set(p.getYear(), p.getMonth() - 1, 1);

        cal.add(Calendar.MONTH, n);
        return KenjaDateImpl.getInstance(cal);
    }

    /**
     * 連続した年月日の入った<code>List</code>を得る。
     * 日は1日となる。
     * @param sdate 開始年月日
     * @param edate 終了年月日
     * @return 連続した年月日の入った<code>List</code>
     */
    public static List<KenjaDateImpl> getDateList(final KenjaDateImpl sdate, final KenjaDateImpl edate) {
        final List<KenjaDateImpl> rtn = new ArrayList<KenjaDateImpl>();

        for (int i = 0; i < monthCount(sdate, edate); i++) {
            rtn.add(addMonthOfFirstDay(sdate, i));
        }

        return rtn;
    }

    /**
     * 小さい(過去)方の日付を返す。
     * @param date1 日付
     * @param date2 日付
     * @return 小さい(過去)方の日付
     */
    public static KenjaDateImpl min(final KenjaDateImpl date1, final KenjaDateImpl date2) {
        if (null == date1) {
            return date2;
        }
        if (null == date2) {
            return date1;
        }
        return date1.compareTo(date2) < 0 ? date1 : date2;
    }

} // DateUtils

// eof
