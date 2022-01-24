// kanji=漢字
/*
 * $Id: AccumulateSummaryBatchAbsenceHigh.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2006/09/22 14:55:41 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AttendSubclassDat;
import jp.co.alp.kenja.batch.accumulate.AttendSubclassSpecialDat;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.EntDate;
import jp.co.alp.kenja.batch.accumulate.SubClassGroupScheduleCounter;
import jp.co.alp.kenja.batch.accumulate.SubClassScheduleCounter;
import jp.co.alp.kenja.batch.accumulate.SubclassAbsenceHighSpecial;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.dao.DaoAccumulateAttendanceNoCount;
import jp.co.alp.kenja.batch.dao.DaoBatchSchoolMaster;
import jp.co.alp.kenja.batch.dao.DaoBatchWithCurriculum;
import jp.co.alp.kenja.batch.dao.DaoSemester9;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchChairStudentOfYear;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchUsualScheduleOfYear;
import jp.co.alp.kenja.batch.dao.loader.DaoInitLoaderLessonCountBatch;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAbsenceHighDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAbsenceHighSpecialDat;
import jp.co.alp.kenja.batch.domain.CombinedSubClassManager;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoInitLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.domain.UsualSchedule;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 欠課数上限値データ生成。
 * @version $Id: AccumulateSummaryBatchAbsenceHigh.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public class AccumulateSummaryBatchAbsenceHigh extends AbstractAccumulateSummaryBatch {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateSummaryBatchAbsenceHigh.class);
    
    private enum Div {
        YEAR("1", "年間"),
        DATE("2", "随時")
        ;
        
        final String _val;
        final String _title;
        Div(final String val, final String title) {
            _val = val;
            _title = title;
        }
        public String getValue() {
            return _val;
        }
        public KenjaDateImpl getDate(final ControlMaster cm) {
            KenjaDateImpl date = null;
            switch (this) {
            case YEAR:
                final Semester semester9 = Semester.getInstance(cm.getCategory(), DaoSemester9.SEMESTER_9);
                date = semester9.getEDate();
                break;
            case DATE:
                date = cm.getCurrentDate();
                break;
            }
            return date;
        }
        public String toString() {
            return "Div(" + _val + ":" + _title + ")";
        }
    }

    /**
     * コンストラクタ。
     * @param options オプション
     */
    public AccumulateSummaryBatchAbsenceHigh(final AccumulateOptions options) {
        super(options);
    }

    /**
     * {@inheritDoc}
     * 授業時数累積・欠課数上限値を算出し、DBに保存する。
     */
    protected void batch(final DbConnection dbcon) throws Exception {
        final ControlMaster cm = getContext().getControlMaster();
        DaoBatchSchoolMaster.getInstance(_options).load(dbcon.getROConnection(), cm);
        final BatchSchoolMaster sm = BatchSchoolMaster.getBatchSchoolMaster();
        log.debug(" 授業数管理区分 = " + (sm.getJugyouJisuFlg() ==  BatchSchoolMaster.JUGYOU_JISU_FLG_JITU ? "実授業数管理" : "法定授業時数管理"));
        if (sm.getJugyouJisuFlg() != BatchSchoolMaster.JUGYOU_JISU_FLG_JITU) {
            log.debug(" 実授業数管理 の欠課数上限値データ生成処理中止");
            return;
        }
        final DaoInitLoader daoInitLoader = new DaoInitLoaderLessonCountBatch(_options);
        loadMasterData(_category, daoInitLoader, dbcon, cm, _options.getProperties(), _options.getKenjaParameters());
        
        final DaoBatchWithCurriculum.Loaders loaders = new DaoBatchWithCurriculum().getLoaders(_options.getKenjaParameters());
        final List<AttendSubclassDat> attendSubclassDatList = loaders.getDaoAttendSubClassDatInstance(_options).getAttendSubclassDatList();
        final List<UsualSchedule> scheduleList = DaoBatchUsualScheduleOfYear.getInstance().getScheduleList();
        final Map<Div, Map<Student, SubClassScheduleCounter>> sssCount = getSchregSubClassScheduleCounters(attendSubclassDatList, scheduleList, cm);
        countLesson(dbcon, cm, sssCount);
    }

    private void countLesson(final DbConnection dbcon, final ControlMaster cm, final Map<Div, Map<Student, SubClassScheduleCounter>> sssCount) throws SQLException {

        final Connection conn = dbcon.getRWConnection();

        final DaoBatchWithCurriculum.Updaters updaters = new DaoBatchWithCurriculum().getUpdaters(_options.getKenjaParameters());
        final DaoUpdateAbsenceHighDat daoUah = updaters.getDaoUpdateAbsenceHighDatInstance(conn, cm, _options, BatchSchoolMaster.getBatchSchoolMaster());
        daoUah.deleteByYear(cm.getCurrentYearAsString());
        int outputCount1 = 0;
        final List<Student> studentList = Student.getEnumList(cm.getCategory());
        for (final Student student : studentList) {
            for (final Div div : Div.values()) {
                final SubClassScheduleCounter ssc1 = getSubClassScheduleCounter(sssCount, student, div);
                if (!ssc1.isEmpty()) {
                    log.trace("SUBCLASS LESSON (" + div.toString() + ")=" + ssc1);
                    outputCount1 += daoUah.save(ssc1);
                }
            }
            if (null != conn) { conn.commit(); }
        }
        log.debug(" DB出力件数=" + outputCount1 + "件");

        // 特別科目コードごとに出力
        final DaoUpdateAbsenceHighSpecialDat daoUahSpe = new DaoUpdateAbsenceHighSpecialDat(conn, cm, _options, BatchSchoolMaster.getBatchSchoolMaster());
        daoUahSpe.deleteByYear(cm.getCurrentYearAsString());
        int outputCount2 = 0;
        final Map<Div, Map<Student, SubClassGroupScheduleCounter>> ssgsCount = getSchregSubClassGroupScheduleCounters(sssCount);
        for (final Student student : studentList) {
            for (final Div div : Div.values()) {
                final SubClassGroupScheduleCounter sgsc1 = getSubClassGroupScheduleCounter(sssCount, ssgsCount, student, div);
                if (!sgsc1.isEmpty()) {
                    log.trace("SUBCLASS GROUP LESSON (" + div.toString() + ")=" + sgsc1);
                    outputCount2 += daoUahSpe.save(sgsc1);
                }
            }
            if (null != conn) { conn.commit(); }
        }
        log.debug(" 特別科目グループ DB出力件数=" + outputCount2 + "件");

        DbUtils.commitAndClose(conn);
    }

    /**
     * 生徒の区分の講座科目カウンタを得る。
     * @param schregSubClassScheduleCountersMap 生徒の科目ごと講座カウント数マップ
     * @param student 生徒
     * @param div 区分
     * @return 生徒の講座科目カウンタ
     */
    private SubClassScheduleCounter getSubClassScheduleCounter(final Map<Div, Map<Student, SubClassScheduleCounter>> schregSubClassScheduleCountersMap, final Student student, final Div div) {
        final Map<Student, SubClassScheduleCounter> schregSubClassScheduleCounters = schregSubClassScheduleCountersMap.get(div);

        if (schregSubClassScheduleCounters.get(student) == null) {
            final ControlMaster cm = getContext().getControlMaster();
            schregSubClassScheduleCounters.put(student, new SubClassScheduleCounter(cm.getCurrentYearAsString(), div.getValue(), student, div.getDate(cm)));
        }
        return schregSubClassScheduleCounters.get(student);
    }

    /**
     * 生徒の区分の講座科目カウンタを得る。
     */
    private SubClassGroupScheduleCounter getSubClassGroupScheduleCounter(final Map<Div, Map<Student, SubClassScheduleCounter>> schregSubClassScheduleCountersMap, final Map<Div, Map<Student, SubClassGroupScheduleCounter>> schregSubClassGroupScheduleCountersMap, final Student student, final Div div) {
        final Map<Student, SubClassGroupScheduleCounter> schregSubClassGroupScheduleCounters = schregSubClassGroupScheduleCountersMap.get(div);

        if (schregSubClassGroupScheduleCounters.get(student) == null) {
            schregSubClassGroupScheduleCounters.put(student, new SubClassGroupScheduleCounter(getSubClassScheduleCounter(schregSubClassScheduleCountersMap, student, div)));
        }
        return schregSubClassGroupScheduleCounters.get(student);
    }

    /**
     * 生徒と科目講座カウンタのマップを得る。
     */
    private Map<Div, Map<Student, SubClassScheduleCounter>> getSchregSubClassScheduleCounters(final List<AttendSubclassDat> attendSubclassDatList, final List<UsualSchedule> scheduleList, final ControlMaster cm) {
        final Map<Div, Map<Student, SubClassScheduleCounter>> schregSubClassScheduleCounters = new HashMap<Div, Map<Student, SubClassScheduleCounter>>();
        schregSubClassScheduleCounters.put(Div.YEAR, new HashMap<Student, SubClassScheduleCounter>());
        schregSubClassScheduleCounters.put(Div.DATE, new HashMap<Student, SubClassScheduleCounter>());

        final DaoBatchChairStudentOfYear daoChairStudent = DaoBatchChairStudentOfYear.getInstance();

        int chairCount = 0;

        accumulateAttendSubclassDat(attendSubclassDatList, schregSubClassScheduleCounters);
        log.debug(" 累積テーブルデータ件数 = " + attendSubclassDatList.size() + "件");

        // 時間割ループ
        for (final UsualSchedule schedule : scheduleList) {
            if (schedule == null) {
                continue;
            }

            chairCount += 1;
            int studentCount = 0;
            // 時間割の講座生徒ループ
            for (final Student student : daoChairStudent.getChairEnabledStudent(cm, schedule, _options.getKenjaParameters())) {

                if (DaoAccumulateAttendanceNoCount.isNoCount(student.getCode(), schedule.getDate())) {
                    log.info(" カウントなし schregno = " + student.getCode() + ", date = " + schedule.getDate());
                    continue;
                }

                final EntDate entDate = EntDate.getInstance(cm.getCategory(), student.getCode());
                final boolean isEntered = null == entDate || null == entDate.getDate() || schedule.getDate().compareTo(entDate.getDate()) >= 0;
                final boolean mustBeAttended = isEntered && student.isActive((KenjaDateImpl) schedule.getDate());

                final SubClassScheduleCounter ssc1 = getSubClassScheduleCounter(schregSubClassScheduleCounters, student, Div.YEAR);
                final SubClass subClass = schedule.getChair().getSubClass();
                if (isEntered) {
                    plusSubclassLesson(ssc1, subClass, 1, false);
                }
                if (mustBeAttended) {
                    plusSubclassLesson(ssc1, subClass, 1, true);
                }

                if (0 < cm.getCurrentDate().compareTo(schedule.getDate())) { // 時間割の日付がプログラム実行日付より前であれば
                    final SubClassScheduleCounter ssc2 = getSubClassScheduleCounter(schregSubClassScheduleCounters, student, Div.DATE);
                    if (isEntered) {
                        plusSubclassLesson(ssc2, subClass, 1, false);
                    }
                    if (mustBeAttended) {
                        plusSubclassLesson(ssc2, subClass, 1, true);
                    }
                }
                studentCount += 1;
            } // 講座生徒ループ終了
            if (studentCount != 0) {
                log.debug(" 時間割 = " + schedule + "(生徒" + studentCount + "件)");
            }
        } // 時間割ループ終了
        log.debug(" 時間割講座処理件数=" + chairCount + "件");

        return schregSubClassScheduleCounters;
    }

    private void accumulateAttendSubclassDat(final List<AttendSubclassDat> attendSubclassDatList, final Map<Div, Map<Student, SubClassScheduleCounter>> schregSubClassScheduleCounters) {
        // 累積テーブル参照ループ
        for (final AttendSubclassDat asd : attendSubclassDatList) {
            final Student student = asd.getStudent();
            
            for (final Div div : Div.values()) {
                final SubClassScheduleCounter ssc = getSubClassScheduleCounter(schregSubClassScheduleCounters, student, div);
                plusSubclassLesson(ssc, asd.getSubClass(), asd.getLessonMustBeAttended(), true);
                plusSubclassLesson(ssc, asd.getSubClass(), asd.getLesson(), false);
            }
        } // 累積テーブル参照ループ終了
    }

    private void plusSubclassLesson(final SubClassScheduleCounter ssc, final SubClass subClass, final int lesson, final boolean mustBeAttended) {
        if (mustBeAttended) {
            ssc.plusLessonMustBeAttended(subClass, lesson);
        } else {
            ssc.plusLesson(subClass, lesson);
        }

        final CombinedSubClassManager manager = CombinedSubClassManager.getInstance();
        if (manager.isAnyAttendSubClass(subClass)) {
            for (final SubClass combined : manager.getCombinedSubClasses(subClass)) {
                final int combinedLessonSrc;
                final int combinedLessonDist;
                if (mustBeAttended) {
                    combinedLessonSrc = ssc.getLessonMustBeAttendedCount(combined);
                    ssc.plusLessonMustBeAttended(combined, lesson);
                    combinedLessonDist = ssc.getLessonMustBeAttendedCount(combined);
                } else {
                    combinedLessonSrc = ssc.getLessonCount(combined);
                    ssc.plusLesson(combined, lesson);
                    combinedLessonDist = ssc.getLessonCount(combined);
                }
                if (log.isInfoEnabled()) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" " + subClass + ":合併元科目です。合併先科目(" + combined + ") に対して" + (mustBeAttended ? "出席すべき授業時数" : "授業時数") + "を ");
                    stb.append(lesson + " 加算します。(" + combinedLessonSrc + " => " + combinedLessonDist + ") 生徒：" + ssc.getStudent());
                    log.info(stb.toString());
                }
            }
        }
    }

    /**
     * 生徒と科目グループ講座カウンタのマップを得る。
     */
    private Map<Div, Map<Student, SubClassGroupScheduleCounter>> getSchregSubClassGroupScheduleCounters(final Map<Div, Map<Student, SubClassScheduleCounter>> schregSubClassScheduleCounters) {
        final Map<Div, Map<Student, SubClassGroupScheduleCounter>> schregSubClassGroupScheduleCounters = new HashMap<Div, Map<Student, SubClassGroupScheduleCounter>>();
        schregSubClassGroupScheduleCounters.put(Div.YEAR, new HashMap<Student, SubClassGroupScheduleCounter>());
        schregSubClassGroupScheduleCounters.put(Div.DATE, new HashMap<Student, SubClassGroupScheduleCounter>());

        final Map<String, AttendSubclassSpecialDat> map = AttendSubclassSpecialDat.getAttendSubclassSpecialData();

        for (final Div div : schregSubClassScheduleCounters.keySet()) {
            final Map<Student, SubClassScheduleCounter> schregSubclassScheduleCounters = schregSubClassScheduleCounters.get(div);

            final Map<Student, SubClassGroupScheduleCounter> schregSubclassGroupScheduleCounters = schregSubClassGroupScheduleCounters.get(div);

            for (final Student student : schregSubclassScheduleCounters.keySet()) {
                final SubClassScheduleCounter ssc = getSubClassScheduleCounter(schregSubClassScheduleCounters, student, div); // 生徒ごとの科目別時間割カウンタを取得
                final SubClassGroupScheduleCounter sgsc = new SubClassGroupScheduleCounter(ssc);

                for (final String specialSubClassCd : map.keySet()) { // 特別科目グループコード
                    final AttendSubclassSpecialDat assd = map.get(specialSubClassCd);

                    // 特別科目グループごとが生徒の科目を含んでいれば、授業時分を加算する
                    for (final SubClass subClass : ssc.getSubClasses()) {
                        if (assd.contains(subClass)) {
                            final SubclassAbsenceHighSpecial sahs = sgsc.getSubclassAbsenceHighSpecial(specialSubClassCd);
                            sahs.plusLessonMinutes(ssc.getLessonCount(subClass) * assd.getMinutes(subClass));
                            sahs.plusLessonMustBeAttendedMinutes(ssc.getLessonMustBeAttendedCount(subClass) * assd.getMinutes(subClass));
                        }
                    }
                }

                schregSubclassGroupScheduleCounters.put(student, sgsc);
            }
        }

        return schregSubClassGroupScheduleCounters;
    }

} // AccumulateSummaryBatch

// eof
