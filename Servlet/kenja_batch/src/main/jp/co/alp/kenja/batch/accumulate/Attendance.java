// kanji=漢字
/*
 * $Id: Attendance.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 11:21:26 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 出欠。
 * @author takaesu
 * @version $Id: Attendance.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class Attendance implements Comparable<Attendance> {
    /*pkg*/static final Log log = LogFactory.getLog(Attendance.class);

    private final Student _student;
    private final AccumulateSchedule _schedule;

    private Kintai _kintai;
    private String _remark;

    /**
     * コンストラクタ。
     * @param category カテゴリ
     * @param student 生徒
     * @param schedule 時間割
     * @param kintai 勤怠
     * @param remark 出欠備考
     */
    public Attendance(
            final MyEnum.Category category,
            final Student student,
            final AccumulateSchedule schedule,
            final Kintai kintai,
            final String remark
    ) {
        if (null == student) {
            throw new IllegalArgumentException("生徒がnull");
        }
        if (null == schedule) {
            throw new IllegalArgumentException("時間割がnull");
        }

        _student = student;
        _schedule = schedule;
        if (null != kintai) {
            _kintai = kintai;
        } else {
            _kintai = schedule.isRollCalledDivNOTYET() ? null : Kintai.getSeated(category);
        }
        _remark = remark;
    }

    /**
     * 講座を得る。
     * @return 講座
     */
    public Chair getChair() {
        return _schedule.getChair();
    }

    /**
     * 日付を得る。
     * @return 日付
     */
    public KenjaDateImpl getDate() {
        return (KenjaDateImpl) _schedule.getDate();
    }

    /**
     * 勤怠を得る。
     * @return 勤怠
     */
    public Kintai getKintai() {
        return _kintai;
    }

    /**
     * 勤怠を修正する。
     * @param kintai 勤怠
     */
    public void adjustKintai(final Kintai kintai) {
        _kintai = kintai;
    }

    /**
     * 校時を得る。
     * @return 校時
     */
    public Period getPeriod() {
        return _schedule.getPeriod();
    }

    /**
     * 出欠備考を得る。
     * @return 出欠備考
     */
    public String getRemark() {
        return _remark;
    }

    /**
     * 生徒を得る。
     * @return 生徒
     */
    public Student getStudent() {
        return _student;
    }

    /**
     * 時間割を得る。
     * @return 時間割
     */
    public AccumulateSchedule getSchedule() {
        return _schedule;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "[" + _student + ", " + _schedule + ", " + _kintai + "]";
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Attendance that) {

        int rtn = 0;

        rtn = getStudent().compareTo(that.getStudent());
        if (0 != rtn) {
            return rtn;
        }

        rtn = getDate().compareTo(that.getDate());
        if (0 != rtn) {
            return rtn;
        }

        rtn = getPeriod().compareTo(that.getPeriod());
        if (0 != rtn) {
            return rtn;
        }

        rtn = getChair().compareTo(that.getChair());
        if (0 != rtn) {
            return rtn;
        }

        return rtn;
    }
} // AccumulateAttendance

// eof
