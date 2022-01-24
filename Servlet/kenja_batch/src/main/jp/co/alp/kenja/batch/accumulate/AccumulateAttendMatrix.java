// kanji=漢字
/*
 * $Id: AccumulateAttendMatrix.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/18 10:49:10 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 生徒の勤怠マトリックス。
 * <code>Student</code>に勤怠をぶら下げたくないので作った。
 * @author takaesu
 * @version $Id: AccumulateAttendMatrix.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class AccumulateAttendMatrix {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateAttendMatrix.class);

    private final Map<Student, DateAttendanceMap> _studentDateAttandanceMapMap = new HashMap<Student, DateAttendanceMap>(1000);
    private final Map<Student, Collection<AccumulateSchedule>> _studentScheduleMap = new HashMap<Student, Collection<AccumulateSchedule>>(1000);

    /**
     * 出欠を設定する。
     * @param student 生徒
     * @param attendance 出欠
     */
    public void add(
            final Attendance attendance
    ) {
        final Student student = attendance.getStudent();
        final DateAttendanceMap studentsSchedules = findOrNew(student);
        studentsSchedules.put(attendance.getDate(), attendance);

        if (!_studentScheduleMap.containsKey(student)) {
            _studentScheduleMap.put(student, new ArrayList<AccumulateSchedule>());
        }
        _studentScheduleMap.get(student).add(attendance.getSchedule());
    }

    private boolean contains(final Student student, final AccumulateSchedule schedule) {
        if (_studentScheduleMap.containsKey(student)) {
            return _studentScheduleMap.get(student).contains(schedule);
        }
        return false;
    }

    private DateAttendanceMap findOrNew(final Student student) {
        if (!_studentDateAttandanceMapMap.containsKey(student)) {
            _studentDateAttandanceMapMap.put(student, new DateAttendanceMap());
        }
        return _studentDateAttandanceMapMap.get(student);

    }

    /**
     * 生徒から、全体の予定Map を得る。
     * @param student 生徒
     * @return 出欠の集合体
     */
    public DateAttendanceMap getAttendances(final Student student) {
        return _studentDateAttandanceMapMap.get(student);
    }

    /**
     * 「出席」な出欠を設定する。
     * @param schMatrix 時間割マトリックス
     * @param meiboMatrix 名簿マトリックス
     * @param category カテゴリ
     */
    public void generateSeatedAttendance(
            final AccumulateScheduleMatrix schMatrix,
            final AccumulateMeiboMatrix meiboMatrix,
            final MyEnum.Category category
    ) {
        final Kintai seated = Kintai.getSeated(category);
        for (final AccumulateSchedule schedule : schMatrix) {
            /* 「時間割=出欠済み」は「出席した」という見方と、「時間割が存在する=出席すべき」という見方がある。
            if (!RollCalledDiv.FINISHED.equals(schedule.getRollCalledDiv())) {
                continue;
            }
            */
            final List<Student> list = meiboMatrix.getStudentList(schedule);
            for (final Student student : list) {
                if (contains(student, schedule)) {
                    continue;
                }
                final Attendance attendance = new Attendance(category, student, schedule, seated, null);
                add(attendance);
            }
        }
    }

    // ===========

    /**
     * 出欠の集合体
     */
    public class DateAttendanceMap {
        private final Map<KenjaDateImpl, List<Attendance>> _dateAttendanceMap = new TreeMap<KenjaDateImpl, List<Attendance>>();

        /**
         * 出欠を設定する。
         * @param date 日付
         * @param attendance 出欠
         */
        public void put(final KenjaDateImpl date, final Attendance attendance) {
            if (!_dateAttendanceMap.containsKey(date)) {
                _dateAttendanceMap.put(date, new ArrayList<Attendance>());
            }
            _dateAttendanceMap.get(date).add(attendance);
        }

        /**
         * 出欠のキーのセットビューを得る。
         * @return マップに含まれているキーのセットビュー
         */
        public Collection<KenjaDateImpl> getDateKeySet() {
            return _dateAttendanceMap.keySet();
        }

        /**
         * 出欠の Collection を得る。
         * @param date 日付
         * @return 出欠の Collection
         */
        public Collection<Attendance> getAttendanceList(final KenjaDateImpl date) {
            final List<Attendance> rtn = _dateAttendanceMap.get(date);
            Collections.sort(rtn);
            return rtn;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _dateAttendanceMap.toString();
        }
    } // DateAttendanceMap
} // AccumulateAttendMatrix

// eof
