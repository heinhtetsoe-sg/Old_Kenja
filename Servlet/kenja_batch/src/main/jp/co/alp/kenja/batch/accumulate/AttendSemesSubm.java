/*
 * $Id: AttendSemesSubm.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/10/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Collection;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 日々出欠中分類累積データ
 * @author maesiro
 * @version $Id: AttendSemesSubm.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AttendSemesSubm extends AttendSemesSubl {

    private final String _submCd;

    /**
     * コンストラクタ
     * @param student 生徒
     * @param kintai 勤怠
     * @param sublCd 大分類コード
     * @param submCd 中分類コード
     * @param dateList 日付のコレクション
     */
    public AttendSemesSubm(
            final Student student,
            final Kintai kintai,
            final String sublCd,
            final String submCd,
            final Collection<KenjaDateImpl> dateList) {
        super(student, kintai, sublCd, dateList);
        _submCd = submCd;
    }

    /**
     * 中分類コードを得る。
     * @return 中分類コード
     */
    public String getSubmCd() {
        return _submCd;
    }
}
