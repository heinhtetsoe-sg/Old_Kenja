/*
 * $Id: SubClassScheduleCounter.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/07/31
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 科目毎の講座カウンタ。
 * @author maesiro
 * @version $Id: SubClassScheduleCounter.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class SubClassScheduleCounter {

    private static final Log log = LogFactory.getLog(SubClassScheduleCounter.class);

    private final String _year;
    private final String _div;
    private final Student _student;
    private final Map<SubClass, SubclassAbsenceHigh> _subclassSchedule;
    private final KenjaDateImpl _date;

    /**
     * コンストラクタ
     * @param year 年度
     * @param div 区分
     * @param student 生徒
     * @param date 日付
     */
    public SubClassScheduleCounter(final String year, final String div, final Student student, final KenjaDateImpl date) {
        _year = year;
        _div = div;
        _student = student;
        _subclassSchedule = new HashMap<SubClass, SubclassAbsenceHigh>();
        _date = date;
        //log.debug(" 年度 = " + _year + " , 区分 = " + _div + " (集計日付 = " + _date + ") 生徒 = " + _student);
    }

    /**
     * 指定科目の授業時数
     * @param subclass 科目
     * @return 授業時数
     */
    public int getLessonCount(final SubClass subclass) {
        final SubclassAbsenceHigh ah = (SubclassAbsenceHigh) MapUtils.getObject(_subclassSchedule, subclass, SubclassAbsenceHigh.NULL);
        return ah.getLessonCount();
    }

    /**
     * 指定科目の出席すべき授業時数
     * @param subclass 科目
     * @return 出席すべき授業時数
     */
    public int getLessonMustBeAttendedCount(final SubClass subclass) {
        final SubclassAbsenceHigh ah = (SubclassAbsenceHigh) MapUtils.getObject(_subclassSchedule, subclass, SubclassAbsenceHigh.NULL);
        return ah.getLessonMustBeAttendedCount();
    }

    /**
     * 科目の授業時数を追加する。
     * @param subClass 科目
     * @param lesson 授業時数
     */
    public void plusLesson(final SubClass subClass, final int lesson) {
        final SubclassAbsenceHigh ah = getSubclassAbsenceHigh(subClass);
        ah.plusLesson(lesson);
        //log.debug("   " + subclass + " , " + _student + " " + ah.getLessonCount());
    }

    /**
     * 科目の出席すべき授業時数を追加する。
     * @param subClass 科目
     * @param lesson 出席すべき授業時数
     */
    public void plusLessonMustBeAttended(final SubClass subClass, final int lesson) {
        final SubclassAbsenceHigh ah = getSubclassAbsenceHigh(subClass);
        ah.plusLessonMustBeAttended(lesson);
        //log.debug("   " + subclass + " , " + _student + " " + ah.getLessonCount());
    }

    /**
     * 指定科目のSubclassAbsenceHighを取得する。
     * @param subclass 科目
     */
    private SubclassAbsenceHigh getSubclassAbsenceHigh(final SubClass subclass) {
        if (_subclassSchedule.get(subclass) == null) {
            _subclassSchedule.put(subclass, new SubclassAbsenceHigh(subclass));
        }
        return (SubclassAbsenceHigh) MapUtils.getObject(_subclassSchedule, subclass, SubclassAbsenceHigh.NULL);
    }

    /**
     * 区分を得る。
     * @return 区分
     */
    public String getDiv() {
        return _div;
    }

    /**
     * 生徒を得る。
     * @return 生徒
     */
    public Student getStudent() {
        return _student;
    }

    /**
     * 科目を得る。
     * @return 科目
     */
    public Collection<SubClass> getSubClasses() {
        return _subclassSchedule.keySet();
    }

    /**
     * 欠課上限値を得る。
     * @param subClass 科目
     * @return 科目の欠課上限値
     */
    public SubclassAbsenceHigh getAbsenceHigh(final SubClass subClass) {
        return (SubclassAbsenceHigh) MapUtils.getObject(_subclassSchedule, subClass);
    }

    /**
     * データが空か
     * @return データが空ならtrue、そうでなければfalse
     */
    public boolean isEmpty() {
        return _subclassSchedule.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" 生徒=[" + _student + "]");
        for (final SubClass subClass : getSubClasses()) {
            stb.append("[" + subClass + " count=" + getLessonCount(subClass) + "]");
        }
        return stb.toString();
    }

    /**
     * 更新日付を得る。
     * @return 更新日付
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * 年度を得る。
     * @return 年度
     */
    public String getYearAsString() {
        return _year;
    }
}
