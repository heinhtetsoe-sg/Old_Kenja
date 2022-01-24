/*
 * $Id: AttendSubclassDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/10/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;

/**
 * 科目別累積データ。
 * @author maesiro
 * @version $Id: AttendSubclassDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AttendSubclassDat {

    private static BatchSchoolMaster schoolMaster_;

    private final SubClass _subClass;
    private final Student _student;
    private final Term _calculatedTerm;
    private final int _lesson;
    private final int _abroad;
    private final int _offdays;
    private final int _suspend;
    private final int _mourning;

    /**
     * 科目累積データ
     * @param subClass 科目
     * @param student 生徒
     * @param calculatedTerm 集計範囲
     * @param lesson 累積データのLESSON
     * @param abroad 累積データのABROAD
     * @param offdays 累積データのOFFDAYS
     * @param suspend 累積データのSUSPEND
     * @param mourning 累積データのMOURNING
     */
    public AttendSubclassDat(
            final SubClass subClass,
            final Student student,
            final Term calculatedTerm,
            final int lesson,
            final int abroad,
            final int offdays,
            final int suspend,
            final int mourning
    ) {
        _subClass = subClass;
        _student = student;
        _calculatedTerm = calculatedTerm;
        _lesson = lesson;
        _abroad = abroad;
        _offdays = offdays;
        _suspend = suspend;
        _mourning = mourning;
    }

    private static BatchSchoolMaster getSchoolMaster() {
        if (schoolMaster_ == null) {
            schoolMaster_ = BatchSchoolMaster.getBatchSchoolMaster();
        }
        return schoolMaster_;
    }

    /**
     * 科目を得る
     * @return 科目
     */
    public SubClass getSubClass() {
        return _subClass;
    }

    /**
     * 生徒を得る
     * @return 生徒
     */
    public Student getStudent() {
        return _student;
    }

    /**
     * LESSON(授業時数ではない)を得る
     * @return LESSON
     */
    private int getLesson0() {
        return _lesson;
    }

    /**
     * 留学時数を得る
     * @return 留学時数
     */
    public int getAbroad() {
        return _abroad;
    }

    /**
     * 出停時数を得る
     * @return 出停時数
     */
    public int getOffdays() {
        return _offdays;
    }

    /**
     * 授業時数を得る
     * @return 授業時数
     */
    public int getLesson() {
        int lesson = getLesson0() - getAbroad() - getOffdays();
        if (getSchoolMaster() != null && getSchoolMaster().offdaysIsKekka()) {
            lesson += getOffdays();
        }
        return lesson;
    }

    /**
     * 出停時数を得る
     * @return 出停時数
     */
    private int getSuspend() {
        return _suspend;
    }

    /**
     * 忌引時数を得る
     * @return 忌引時数
     */
    private int getMourning() {
        return _mourning;
    }

    /**
     * 出席すべき授業時数を得る
     * @return 授業時数
     */
    public int getLessonMustBeAttended() {
        int mustBeAttendedlesson = getLesson() - getMourning() - getSuspend();
        if (getSchoolMaster() != null) {
            if (getSchoolMaster().mourningIsKekka()) {
                mustBeAttendedlesson += getMourning();
            }
            if (getSchoolMaster().suspendIsKekka()) {
                mustBeAttendedlesson += getSuspend();
            }
        }
        return mustBeAttendedlesson;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "科目別出欠累積データ = " + _subClass.toString() + " " + _student + " " + _calculatedTerm + " LESSON = " + _lesson;
    }

}
