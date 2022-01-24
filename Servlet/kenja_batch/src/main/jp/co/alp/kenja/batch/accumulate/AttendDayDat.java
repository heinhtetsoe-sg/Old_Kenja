/*
 * $Id: AttendDayDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/10/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 日々出欠
 * @author maesiro
 * @version $Id: AttendDayDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AttendDayDat {

    private final Student _student;
    private final KenjaDateImpl _date;
    private final Kintai _kintai;
    private final String _remark;

    /**
     * コンストラクタ
     * @param student 生徒
     * @param date 日付
     * @param kintai 勤怠
     * @param remark 出欠備考
     */
    public AttendDayDat(
            final Student student,
            final KenjaDateImpl date,
            final Kintai kintai,
            final String remark) {
        _student = student;
        _date = date;
        _kintai = kintai;
        _remark = remark;
    }

    /**
     * 生徒を得る。
     * @return 生徒
     */
    public Student getStudent() {
        return _student;
    }

    /**
     * 日付を得る。
     * @return 日付
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * 勤怠を得る。
     * @return 勤怠
     */
    public Kintai getKintai() {
        return _kintai;
    }

    /**
     * 出欠備考を得る。
     * @return 出欠備考
     */
    public String getRemark() {
        return _remark;
    }
}
