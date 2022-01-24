// kanji=漢字
/*
 * $Id: KenjaDate.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/18 10:48:41 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;


/**
 * 賢者パッケージの日付。
 * @author tamura
 * @version $Id: KenjaDate.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface KenjaDate {
    /**
     * 曜日を得る。
     * @return 曜日
     */
    DayOfWeek getDayOfWeek();

    /**
     * 表示の順序を得る。
     * @return 表示の順序
     */
    int getIndex();

    /**
     * {@inheritDoc}
     */
    int compareTo(Object o);

    /**
     * この日を含む一週間分（月〜土、日）の日付を配列で得る。
     * @return 一週間分（月〜土、日）の日付の配列
     */
    KenjaDate[] getSevenDays();
} // KenjaDate

// eof
