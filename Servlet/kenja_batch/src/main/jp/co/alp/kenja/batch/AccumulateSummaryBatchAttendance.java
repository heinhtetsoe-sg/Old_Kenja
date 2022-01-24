// kanji=漢字
/*
 * $Id: AccumulateSummaryBatchAttendance.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2006/09/22 14:55:41 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.accumulate.AccumulateAttendDayDat;
import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.AccumulateMeiboMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateScheduleMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateSemes;
import jp.co.alp.kenja.batch.accumulate.AccumulateSubclass;
import jp.co.alp.kenja.batch.accumulate.AttendDayDat;
import jp.co.alp.kenja.batch.accumulate.AttendanceAdjuster;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.EntDate;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.batch.accumulate.OnedayAttendanceJudge;
import jp.co.alp.kenja.batch.accumulate.OnedaySchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix.DateAttendanceMap;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.dao.DaoAttendDat;
import jp.co.alp.kenja.batch.dao.DaoAccumulateSchedule;
import jp.co.alp.kenja.batch.dao.DaoAttendDayDat;
import jp.co.alp.kenja.batch.dao.DaoBatchWithCurriculum;
import jp.co.alp.kenja.batch.dao.loader.DaoInitLoaderAccumulateBatch;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendAbsenceDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendDayDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSemesDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSubclassDat;
import jp.co.alp.kenja.batch.domain.CourseMst;
import jp.co.alp.kenja.batch.domain.HogeUtils;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoInitLoader;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.domain.Student.TransferCd;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 累積データ生成。
 * @author takaesu
 * @version $Id: AccumulateSummaryBatchAttendance.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public class AccumulateSummaryBatchAttendance extends AbstractAccumulateSummaryBatch {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateSummaryBatchAttendance.class);

    /**
     * コンストラクタ。
     * @param options オプション
     */
    public AccumulateSummaryBatchAttendance(final AccumulateOptions options) {
        super(options);
    }

    /**
     * {@inheritDoc}
     * 出欠の累積データを生成する。
     */
    protected void batch(final DbConnection dbcon) throws Exception {

        final ControlMaster cm = getContext().getControlMaster();

        final DaoInitLoader daoInitLoader = new DaoInitLoaderAccumulateBatch(_options);

        loadMasterData(_category, daoInitLoader, dbcon, cm, _options.getProperties(), _options.getKenjaParameters());

        final Semester semester = getSemesterOfDate(Semester.getEnumList(_category), _options.getDate());
        final List<Header> headerList = _options.getHeaderList(semester, cm.getCurrentYear());

        accumulate(dbcon, cm, headerList);
    }

    private List<Student> createStudentList() {
        final List<Student> rtn = new ArrayList<Student>(1024);

        final List<Student> list = Student.getEnumList(_category);
        for (final Student student : list) {

            final KenjaDateImpl grdDate = student.getGrdDate();
            if (null != grdDate) {
                if (grdDate.compareTo(_options.getUnit().getLowerDate()) < 0) {
                    log.debug("卒業で対象外:" + student + ", 卒業日付:" + grdDate);
                    continue;
                }
            }
            rtn.add(student);
        }
        return rtn;
    }

    private List<Student> prepareStudentList(final List<Student> studentList, final AccumulateAttendMatrix attendMatrix) {

        final List<Student> list = new ArrayList<Student>();

        for (final Student student : studentList) {

            final DateAttendanceMap attendances = attendMatrix.getAttendances(student);
            if (null == attendances) {
                continue;
            }

            list.add(student);

            AttendanceAdjuster.adjust(student, attendances);

            final String courseCd = student.getCourseInfo().getCourseCd();
            final CourseMst courseMst = CourseMst.getInstance(_category, courseCd);

            _options.getTracer().traceAttendaces(student, attendances, courseMst);
        }
        return list;
    }

    /**
     * 時間割、名簿、出欠を読み込む。
     * @param term 読む込む期間
     * @return 生徒の勤怠マトリックス
     * @throws SQLException
     */
    private AccumulateAttendMatrix load(final Header header, final DbConnection dbcon, final ControlMaster cm) throws SQLException {
        final StopWatch sw = new StopWatch();
        sw.start();
        log.debug("時間割、名簿、出欠の読み込み開始");

        // 期間内の全時間割を取り込む
        final Term term = header.getTerm();
        final AccumulateScheduleMatrix schMatrix = new AccumulateScheduleMatrix();
        final DaoAccumulateSchedule daoSch = new DaoAccumulateSchedule(schMatrix, term);
        daoSch.load(dbcon.getROConnection(), cm);

        // 期間内の名簿を取り込む
        final AccumulateMeiboMatrix meiboMatrix = new AccumulateMeiboMatrix(dbcon, cm);
        meiboMatrix.load(schMatrix, term);

        // 期間内の全生徒の出欠を取り込む。「出席」は上記の「時間割」と「名簿」が必要。
        final AccumulateAttendMatrix attendMatrix = new AccumulateAttendMatrix();
        final DaoAttendDat daoAtt = new DaoAttendDat(_category, schMatrix, meiboMatrix, term.getSDate(), term.getEDate(), attendMatrix, header, _options.getEnabledSchoolKind());
        daoAtt.load(dbcon.getROConnection(), cm);

        log.debug("時間割、名簿、出欠の読み込み時間: " + sw);

        attendMatrix.generateSeatedAttendance(schMatrix, meiboMatrix, _category);
        return attendMatrix;
    }

    private void accumulate(final DbConnection dbcon, final ControlMaster cm, final List<Header> headerList) throws SQLException {

        final KintaiManager kintaiManager = new KintaiManager(_category);
        final OnedayAttendanceJudge judge = new OnedayAttendanceJudge(_options, BatchSchoolMaster.getBatchSchoolMaster(), kintaiManager);

        final List<Student> studentList = createStudentList();
        for (final Header header : headerList) {
            Map<Student, Map<KenjaDateImpl, List<Kintai>>> notUpdateSchregnoAttendDayDat = null;
            if (!ArrayUtils.isEmpty(_options.getAttendDayDatUpdateRegisterCd())) {
                notUpdateSchregnoAttendDayDat = getNotUpdateAttendDayDat(dbcon, cm, kintaiManager, header);
            }

            final AccumulateAttendMatrix attendMatrix = load(header, dbcon, cm);
            final List<Student> preparedStudentList = prepareStudentList(studentList, attendMatrix);

            final Connection conn = dbcon.getRWConnection();
            accumulateAttendDay(cm, preparedStudentList, header, attendMatrix, conn, judge, notUpdateSchregnoAttendDayDat);
            accumulateSemes(cm, preparedStudentList, header, attendMatrix, conn, judge);
            accumulateSubclass(cm, preparedStudentList, header, attendMatrix, conn);
            DbUtils.commitAndClose(conn);
        }
    }

    private Map<Student, Map<KenjaDateImpl, List<Kintai>>> getNotUpdateAttendDayDat(final DbConnection dbcon, final ControlMaster cm, final KintaiManager kintaiManager, final Header header) throws SQLException {
        final Map<Student, AccumulateAttendDayDat> schregnoAccumulateAttendDayDat = new HashMap<Student, AccumulateAttendDayDat>();
        // 期間内の日々の出欠を取り込む
        final DaoAttendDayDat daoAttendDayDat = new DaoAttendDayDat(header, kintaiManager, schregnoAccumulateAttendDayDat, _options.getEnabledSchoolKind(), _options.getAttendDayDatUpdateRegisterCd());
        log.info(" daoAttendDayDat sql = " + daoAttendDayDat.getQuerySql());
        daoAttendDayDat.load(dbcon.getROConnection(), cm);

        final List<String> info = new LinkedList<String>();
        final Map<Student, Map<KenjaDateImpl, List<Kintai>>> schregnoAttendDayDat = new TreeMap<Student, Map<KenjaDateImpl, List<Kintai>>>();
        for (final Map.Entry<Student, AccumulateAttendDayDat> e : schregnoAccumulateAttendDayDat.entrySet()) {
            final Student student = e.getKey();
            if (!schregnoAttendDayDat.containsKey(student)) {
                schregnoAttendDayDat.put(student, new TreeMap<KenjaDateImpl, List<Kintai>>());
            }
            for (final Map.Entry<Kintai, Set<KenjaDateImpl>> e2 : e.getValue().getDayDatMap().entrySet()) {
                for (final KenjaDateImpl date : e2.getValue()) {
                    if (!schregnoAttendDayDat.get(student).containsKey(date)) {
                        schregnoAttendDayDat.get(student).put(date, new ArrayList<Kintai>());
                    }
                    schregnoAttendDayDat.get(student).get(date).add(e2.getKey());
                    info.add(student.getCode() + " " + date.toString() + " " + e2.getKey());
                }
            }
        }
        log.info(" ATTEND_DAY_DAT not update record size = " + info.size());
        return schregnoAttendDayDat;
    }

    private void accumulateAttendDay(final ControlMaster cm,
            final List<Student> studentList,
            final Header header,
            final AccumulateAttendMatrix attendMatrix,
            final Connection conn,
            final OnedayAttendanceJudge judge,
            final Map<Student, Map<KenjaDateImpl, List<Kintai>>> notUpdateAttendDayDat) throws SQLException {
        final DaoUpdateAttendDayDat daoUpdateAttendDay = new DaoUpdateAttendDayDat(conn, cm, _options);
        daoUpdateAttendDay.delete(header);

        for (final Student student : studentList) {

            final DateAttendanceMap attendances = attendMatrix.getAttendances(student);

            final String courseCd = student.getCourseInfo().getCourseCd();
            if (null == courseCd) {
                continue;
            }
            final CourseMst courseMst = CourseMst.getInstance(_category, courseCd);
            if (null == courseMst) {
                continue;
            }

            final List<AttendDayDat> records = computeAttendDayDat(judge, courseMst, student, attendances);
            if (null != notUpdateAttendDayDat && notUpdateAttendDayDat.containsKey(student)) {
                removeNotUpdateRecords(notUpdateAttendDayDat.get(student), student, records);
            }
            log.trace("日別=" + student + ", レコードの数=" + records.size());

            daoUpdateAttendDay.save(header, student, records);

            if (null != conn) { conn.commit(); }
        }
    }

    private void removeNotUpdateRecords(final Map<KenjaDateImpl, List<Kintai>> dateKintaiMap, final Student student, final List<AttendDayDat> records) {
        final List<AttendDayDat> removes = new ArrayList<AttendDayDat>();
        for (final Iterator<AttendDayDat> it = records.iterator(); it.hasNext();) {
            final AttendDayDat a = it.next();
            if (dateKintaiMap.containsKey(a.getDate())) {
                removes.add(a);
                it.remove();
            }
        }
        if (!removes.isEmpty()) {
            log.info(" ATTEND_DAY_DAT not update " + student.getCode() + ", size : " +  removes.size());
            for (final AttendDayDat a : removes) {
                log.debug("  " + student.getCode() + " " + a.getDate() + " " + a.getKintai() + " / on db = " + dateKintaiMap.get(a.getDate()));
            }
        }
    }

    /** {@inheritDoc} */
    private static List<AttendDayDat> computeAttendDayDat(final OnedayAttendanceJudge judge, final CourseMst courseMst, final Student student, final DateAttendanceMap attendances) {
        final List<AttendDayDat> records = new ArrayList<AttendDayDat>();
        for (final KenjaDateImpl date : attendances.getDateKeySet()) {
            final Collection<Attendance> coll = semesMask(courseMst, attendances.getAttendanceList(date)); // コアタイム以外を除去。
            if (coll.isEmpty()) {
                continue;
            }
            records.addAll(calcDays(judge, student, coll, date));
        }
        return records;
    }

    /**
     * 一日出欠のリストを計算する。
     * @param student 生徒
     * @param coll 出欠の<code>Collection</code>
     * @param date 日付
     * @return 一日出欠のリスト
     */
    private static List<AttendDayDat> calcDays(final OnedayAttendanceJudge judge, final Student student, final Collection<Attendance> coll, final KenjaDateImpl date) {
        final KintaiManager km = judge.getKintaiManager();

        if (null == coll || coll.isEmpty() || calcTransfer(student, date)) {
            return Collections.emptyList();
        }

        final OnedaySchedule onedaySchdule = new OnedaySchedule(student, date, coll);
        final List<AttendDayDat> list = new ArrayList<AttendDayDat>();

        // 公欠が1つ以上あるか？
        if (judge.isAbsent(onedaySchdule)) {
            final Kintai k = km.absent();
            list.add(new AttendDayDat(student, date, k, getRemark(filter(k, coll))));
        }

        // 出停が1つ以上あるか？
        if (judge.isSuspend(onedaySchdule)) {
            final Kintai k = km.suspend();
            list.add(new AttendDayDat(student, date, k, getRemark(filter(k, coll))));
        }

        // 出停(伝染病)が1つ以上あるか？
        if (judge.isVirus(onedaySchdule)) {
            final Kintai k = km.virus();
            list.add(new AttendDayDat(student, date, k, getRemark(filter(k, coll))));
        }

        // 忌引が1つ以上あるか？
        if (judge.isMourning(onedaySchdule)) {
            final Kintai k = km.mourning();
            list.add(new AttendDayDat(student, date, k, getRemark(filter(k, coll))));
        }

        // 全て欠席の場合
        if (judge.isKesseki(onedaySchdule)) {
            list.addAll(addAbsenceDays(judge, student, coll, date, km, onedaySchdule));
        }

        // [遅刻/早退]をカウントする?
        if (judge.isCountLateEarly(onedaySchdule)) {
            list.addAll(addLateEarlyDays(judge, student, coll, date, km, onedaySchdule));
        }
        return list;
    }

    private static List<AttendDayDat> addAbsenceDays(final OnedayAttendanceJudge judge, final Student student,
            final Collection<Attendance> coll, final KenjaDateImpl date, final KintaiManager km,
            final OnedaySchedule onedaySchdule) {
        final List<AttendDayDat> absence = new ArrayList<AttendDayDat>();
        final Kintai firstKintai = judge.getOnedayKintai(onedaySchdule);
        Kintai kintai = null;
        if (KintaiManager.isSick(firstKintai)) {
            kintai = km.sick();
        } else  if (KintaiManager.isNotice(firstKintai)) {
            kintai = km.notice();
        } else if (KintaiManager.isNoNotice(firstKintai)) {
            kintai = km.nonotice();
        }
        if (null != kintai) {
            absence.add(new AttendDayDat(student, date, kintai, getRemark(filter(kintai, coll))));
        }
        return absence;
    }

    private static List<AttendDayDat> addLateEarlyDays(final OnedayAttendanceJudge judge, final Student student,
            final Collection<Attendance> coll, final KenjaDateImpl date, final KintaiManager km,
            final OnedaySchedule onedaySchdule) {
        final String blank = "";
        final List<AttendDayDat> lateEarly = new ArrayList<AttendDayDat>();
        // [遅刻/早退]をカウントするなら遅刻・早退のチェック
        Kintai kintaiLate = null;
        if (judge.isLate(onedaySchdule)) {
            kintaiLate = km.late();
        } else if (judge.isLateNonotice(onedaySchdule)) {
            kintaiLate = km.lateNonotice();
        }
        if (null != kintaiLate) {
            lateEarly.add(new AttendDayDat(student, date, kintaiLate, blank));
        }
        Kintai kintaiEarly = null;
        if (judge.isEarly(onedaySchdule)) {
            kintaiEarly = km.early();
        } else if (judge.isEarlyNonotice(onedaySchdule)) {
            kintaiEarly = km.earlyNonotice();
        }
        if (null != kintaiEarly) {
            lateEarly.add(new AttendDayDat(student, date, kintaiEarly, blank));
        }
        return lateEarly;
    }

    /**
     * 出欠備考を得る。
     * @param filtered 出欠データのリスト
     * @return 出欠備考
     */
    private static String getRemark(final List<Attendance> filtered) {
        String rtn = "";
        boolean setRemark = false;
        // 最初の時間割の出欠備考がセットされていればこれを使用する。
        for (final Attendance attendance : filtered) {
            if (StringUtils.isBlank(attendance.getRemark())) {
                break;
            }
            rtn = attendance.getRemark();
            setRemark = true;
        }
        // 最初の時間割の出欠備考がセットされていなければ、セットされている最大校時の出欠備考を使用する。
        if (!setRemark) {
            for (final ListIterator<Attendance> it = filtered.listIterator(filtered.size()); it.hasPrevious();) {
                final Attendance attendance = it.previous();
                if (StringUtils.isBlank(attendance.getRemark())) {
                    continue;
                }
                rtn = attendance.getRemark();
                break;
            }
        }
        return rtn;
    }

    private static List<Attendance> filter(final Kintai kintai, final Collection<Attendance> coll) {
        final List<Attendance> filtered = new ArrayList<Attendance>();
        for (final Attendance attendance : coll) {
            if (kintai.getAltCode().equals(attendance.getKintai().getAltCode())) {
                filtered.add(attendance);
            }
        }
        return filtered;
    }

    /**
     * 指定日が留学なら abroad を、休学なら offdays を、インクリメントする。
     * どちらでもないなら、何もしない。
     * @param student 生徒
     * @param date 日付
     * @return インクリメントしたらtrue
     */
    private static boolean calcTransfer(final Student student, final KenjaDateImpl date) {
        if (student.isActive(date)) {
            return false;
        }

        final TransferCd transferCd = HogeUtils.getTransferCd(student, date);
        if (TransferCd.TAKE_OFF_SCHOOL.equals(transferCd)) {
            return true;
        } else  if (TransferCd.STUDY_ABROAD.equals(transferCd)) {
            return true;
        }

        return false;
    }

    private void accumulateSemes(final ControlMaster cm, final List<Student> studentList, final Header header, final AccumulateAttendMatrix attendMatrix,
            final Connection conn, final OnedayAttendanceJudge judge) throws SQLException {
        final DaoUpdateAttendSemesDat daoUpdateTuki = new DaoUpdateAttendSemesDat(conn, cm, _options);
        final DaoUpdateAttendAbsenceDat daoUpdateKesseki = new DaoUpdateAttendAbsenceDat(conn, cm, _options);

        daoUpdateTuki.delete(header);
        daoUpdateKesseki.delete(header);

        for (final Student student : studentList) {

            final DateAttendanceMap attendances = attendMatrix.getAttendances(student);

            final String courseCd = student.getCourseInfo().getCourseCd();
            if (null == courseCd) {
                continue;
            }

            final CourseMst courseMst = CourseMst.getInstance(_category, courseCd);
            if (null == courseMst) {
                log.fatal("課程マスタに存在しない。: " + courseCd);
                continue;
            }

            final Map<KenjaDateImpl, AccumulateSemes> dateSemesMap = new HashMap<KenjaDateImpl, AccumulateSemes>(); // <日付, 累積>
            final EntDate entDate = EntDate.getInstance(_category, student.getCode());
            final AccumulateSemes total = calcSemes(student, entDate, attendances, judge, courseMst, dateSemesMap);

            _options.getTracer().traceCounter(student, total);
            log.trace("月別累積=" + student + ", 日付の数=" + dateSemesMap.size());

            daoUpdateTuki.save(header, student, total);
            daoUpdateKesseki.save(header, student, total.getAttendAbsenceDate());

            if (null != conn) { conn.commit(); }
        }
    }

    /** {@inheritDoc} */
    private AccumulateSemes calcSemes(final Student student, final EntDate entDate, final DateAttendanceMap attendances, final OnedayAttendanceJudge judge, final CourseMst courseMst, final Map<KenjaDateImpl, AccumulateSemes> dateSemesMap) {
        final AccumulateSemes total = new AccumulateSemes();
        final KenjaDateImpl grdDate = student.getGrdDate();
        for (final KenjaDateImpl date : attendances.getDateKeySet()) {
            if (null != entDate && null != entDate.getDate() && date.compareTo(entDate.getDate()) < 0) {
                log.debug("集計対象日付 " + date + " が " + entDate + " 以前のため集計から除きます。");
                continue;
            }
            if (date.compareTo(grdDate) > 0) {
                continue;
            }
            final Collection<Attendance> coll = semesMask(courseMst, attendances.getAttendanceList(date)); // コアタイム以外を除去。
            if (coll.isEmpty()) {
                continue;
            }
            if (null == dateSemesMap.get(date)) {
                dateSemesMap.put(date, new AccumulateSemes());
            }
            final AccumulateSemes rui = dateSemesMap.get(date);
            rui.calc(_options, student, coll, date, judge);

            _options.getTracer().traceAccumulateSemes(student, date, coll, rui);

            total.add(rui);
        }
        return total;
    }

    /**
     * {@inheritDoc}
     */
    private static Collection<Attendance> semesMask(final CourseMst courseMst, final Collection<Attendance> collection) {
        final Collection<Attendance> rtn = new ArrayList<Attendance>();
        for (final Attendance attendance : collection) {
            if (null == attendance) {
                continue;
            }
            if (!courseMst.isActive(attendance.getPeriod())) {
                continue;
            }
            rtn.add(attendance);
        }
        return rtn;
    }

    private void accumulateSubclass(final ControlMaster cm,
            final List<Student> studentList,
            final Header header,
            final AccumulateAttendMatrix attendMatrix,
            final Connection conn) throws SQLException {
        final DaoBatchWithCurriculum.Updaters updaters = new DaoBatchWithCurriculum().getUpdaters(_options.getKenjaParameters());
        final DaoUpdateAttendSubclassDat daoUpdateKamoku = updaters.getDaoUpdateAttendSubclassDatInstance(conn, cm, _options);
        daoUpdateKamoku.delete(header);

        for (final Student student : studentList) {

            final DateAttendanceMap attendances = attendMatrix.getAttendances(student);
            final Map<SubClass, AccumulateSubclass> kamo = new HashMap<SubClass, AccumulateSubclass>(); // <科目, AccumulateSubclass>

            final KenjaDateImpl grdDate = student.getGrdDate();
            final EntDate entDate = EntDate.getInstance(_category, student.getCode());

            for (final KenjaDateImpl date : attendances.getDateKeySet()) {
                if (date.compareTo(grdDate) > 0) {
                    continue;
                }
                if (null != entDate && null != entDate.getDate() && date.compareTo(entDate.getDate()) < 0) {
                    log.debug("集計対象日付 " + date + " が " + entDate + " 以前のため集計から除きます。");
                    continue;
                }
                final Collection<Attendance> coll = subclassMask(student, attendances.getAttendanceList(date));
                for (final Attendance attendance : coll) {
                    if (null == attendance || null == attendance.getKintai()) {
                        continue;
                    }
                    final SubClass subClass = attendance.getChair().getSubClass();
                    if (null == kamo.get(subClass)) {
                        kamo.put(subClass, new AccumulateSubclass());
                    }

                    final AccumulateSubclass rui = kamo.get(subClass);
                    rui.calc(student, attendance);
                }
            }

            _options.getTracer().traceCounter(student, kamo);
            log.trace("科目別累積=" + student + ", 科目の数=" + kamo.size());

            daoUpdateKamoku.save(header, student, kamo);

            if (null != conn) { conn.commit(); }
        }
    }

    /**
     * 学期を得る。
     * @return 学期
     */
    private static Semester getSemesterOfDate(final List<Semester> list, final KenjaDateImpl date) {
        for (final Semester semester : list) {

            final KenjaDateImpl sDate = semester.getSDate();
            final KenjaDateImpl eDate = semester.getEDate();

            if (date.compareTo(sDate) >= 0 && date.compareTo(eDate) <= 0) {
                log.info("semester=" + semester + " , date=" + date);
                return semester;
            }
        }
        log.fatal("学期が得られない");
        return null;
    }

    private static Collection<Attendance> subclassMask(final Student student, final Collection<Attendance> accumulateAttendances) {
        final Collection<Attendance> rtn = new ArrayList<Attendance>();
        final Map<Period, Chair> periodChairMap = new HashMap<Period, Chair>();
        for (final Attendance attendance : accumulateAttendances) {
            if (null == attendance) {
                continue;
            }
            final Chair oldChair = periodChairMap.get(attendance.getPeriod());
            if (null != oldChair && oldChair.getSubClass().equals(attendance.getChair().getSubClass())) {
                continue;
            }
            periodChairMap.put(attendance.getPeriod(), attendance.getChair());
            if (!isCountSchedule(attendance.getSchedule(), student.getHomeRoom())) {
                continue;
            }
            rtn.add(attendance);
        }
        return rtn;
    }

    private static boolean isCountSchedule(final AccumulateSchedule sch, final HomeRoom homeRoom) {
        // 集計フラグレコードを持っていなければ「集計する」
        if (!sch.hasCountFlag(homeRoom)) {
            return true;
        }

        return sch.countFlag(homeRoom);
    }

} // AccumulateSummaryBatch

// eof
