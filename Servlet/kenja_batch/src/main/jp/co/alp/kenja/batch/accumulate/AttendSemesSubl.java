/*
 * $Id: AttendSemesSubl.java 74552 2020-05-27 04:41:22Z maeshiro $
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
 * 日々出欠大分類累積データ
 * @author maesiro
 * @version $Id: AttendSemesSubl.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AttendSemesSubl {

    private final Student _student;
    private final Kintai _kintai;
    private final String _sublCd;
    private final Collection<KenjaDateImpl> _dates;

    /**
     * コンストラクタ
     * @param student 生徒
     * @param kintai 勤怠
     * @param sublCd 対分類コード
     * @param dates 日付のコレクション
     */
    public AttendSemesSubl(
            final Student student,
            final Kintai kintai,
            final String sublCd,
            final Collection<KenjaDateImpl> dates) {
        _student = student;
        _kintai = kintai;
        _sublCd = sublCd;
        _dates = dates;
    }

    /**
     * 生徒を得る。
     * @return 生徒
     */
    public Student getStudent() {
        return _student;
    }

    /**
     * 勤怠を得る。
     * @return 勤怠
     */
    public Kintai getKintai() {
        return _kintai;
    }

    /**
     * 大分類コードを得る。
     * @return 大分類コード
     */
    public String getSublCd() {
        return _sublCd;
    }

    /**
     * 日々出欠の数を得る。
     * @return 日々出欠の数
     */
    public int getCount() {
        return _dates.size();
    }
}
