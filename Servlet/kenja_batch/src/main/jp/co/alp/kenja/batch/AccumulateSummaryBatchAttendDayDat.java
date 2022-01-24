// kanji=漢字
/*
 * $Id: AccumulateSummaryBatchAttendDayDat.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2011/10/14 10:46:03 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.AccumulateAttendDayDat;
import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix.DateAttendanceMap;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.accumulate.AccumulateMeiboMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateScheduleMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateSemes;
import jp.co.alp.kenja.batch.accumulate.AccumulateSemesConstants;
import jp.co.alp.kenja.batch.accumulate.AttendSemesSubl;
import jp.co.alp.kenja.batch.accumulate.AttendSemesSubm;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.AttendanceAdjuster;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.EntDate;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.batch.accumulate.OnedayAttendanceJudge;
import jp.co.alp.kenja.batch.dao.DaoAccumulateSchedule;
import jp.co.alp.kenja.batch.dao.DaoAttendDat;
import jp.co.alp.kenja.batch.dao.DaoAttendDayDat;
import jp.co.alp.kenja.batch.dao.DaoAttendDaySublDat;
import jp.co.alp.kenja.batch.dao.DaoAttendDaySubmDat;
import jp.co.alp.kenja.batch.dao.loader.DaoInitLoaderAccumulateBatch;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendAbsenceDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSemesDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSemesSublDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSemesSubmDat;
import jp.co.alp.kenja.batch.domain.CourseMst;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoInitLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 日々出欠累積、欠席データを更新するバッチ。
 * @author maesiro
 * @version $Id: AccumulateSummaryBatchAttendDayDat.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public class AccumulateSummaryBatchAttendDayDat extends AbstractAccumulateSummaryBatch {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateSummaryBatchAttendDayDat.class);

    /**
     * コンストラクタ。
     * @param options オプション
     */
    public AccumulateSummaryBatchAttendDayDat(final AccumulateOptions options) {
        super(options);
    }

    /**
     * {@inheritDoc}
     * 授業時数累積・欠課数上限値を算出し、DBに保存する。
     */
    protected void batch(final DbConnection dbcon) throws Exception {

        final ControlMaster cm = getContext().getControlMaster();

        final DaoInitLoader daoInitLoader = new DaoInitLoaderAccumulateBatch(_options);

        loadMasterData(_category, daoInitLoader, dbcon, cm, _options.getProperties(), _options.getKenjaParameters());

        final Semester semester = getSemester();
        final List<Header> headerList = _options.getHeaderList(semester, cm.getCurrentYear());

        accumulate(dbcon, cm, headerList);
    }

    private void accumulate(
            final DbConnection dbcon,
            final ControlMaster cm,
            final List<Header> headerList) throws SQLException {

        final KintaiManager kintaiManager = new KintaiManager(_category);
        final OnedayAttendanceJudge judge = new OnedayAttendanceJudge(_options, BatchSchoolMaster.getBatchSchoolMaster(), kintaiManager);

        final List<Student> studentList = createStudentList();
        for (final Header header : headerList) {

            final Connection conn = dbcon.getRWConnection();

            final AccumulateAttendMatrix attendMatrix = loadAttendMatrix(header, dbcon, cm);
            final List<Student> preparedStudentListSemes = prepareStudentList(studentList, attendMatrix);
            final Map<Student, AccumulateAttendDayDat> studentAccumulateDayDat = loadAttendDayDat(header, kintaiManager, dbcon, cm);

            accumulate(cm, preparedStudentListSemes, header, kintaiManager, attendMatrix, studentAccumulateDayDat, judge, conn);

            DbUtils.commitAndClose(conn);
        }
    }

    private void accumulate(final ControlMaster cm,
            final List<Student> studentList,
            final Header header,
            final KintaiManager kintaiManager,
            final AccumulateAttendMatrix attendMatrix,
            final Map<Student, AccumulateAttendDayDat> studentAccumulateDayDat,
            final OnedayAttendanceJudge judge,
            final Connection conn) throws SQLException {

        final DaoUpdateAttendAbsenceDat daoUpdateKesseki = new DaoUpdateAttendAbsenceDat(conn, cm, _options);
        final DaoUpdateAttendSemesDat daoUpdateAttendSemesDat = new DaoUpdateAttendSemesDat(conn, cm, _options);
        final DaoUpdateAttendSemesSublDat daoUpdateAttendSemesSubl = new DaoUpdateAttendSemesSublDat(conn, cm, _options);
        final DaoUpdateAttendSemesSubmDat daoUpdateAttendSemesSubm = new DaoUpdateAttendSemesSubmDat(conn, cm, _options);
        daoUpdateKesseki.delete(header);
        daoUpdateAttendSemesDat.delete(header);
        daoUpdateAttendSemesSubl.delete(header);
        daoUpdateAttendSemesSubm.delete(header);

        for (final Student student : studentList) {

            final DateAttendanceMap attendances = attendMatrix.getAttendances(student);

            final CourseMst courseMst = getCourseMst(student);
            if (null == courseMst) {
                log.fatal("課程マスタに存在しない。: " + student.getCourseInfo().getCourseCd());
                continue;
            }

            final EntDate entDate = EntDate.getInstance(cm.getCategory(), student.getCode());
            final AccumulateSemes total = calcAccumulateSemes(student, entDate, courseMst, attendances, judge);

            if (total.getLesson().isEmpty()) {
                continue;
            }
            daoUpdateKesseki.save(header, student, total.getAttendAbsenceDate());

            final AccumulateAttendDayDat aadd = studentAccumulateDayDat.get(student);

            final AccumulateSemes semes = getAttendDayDatAccumulated(aadd, kintaiManager, total);
            log.trace("月別累積=" + student + ", 日付の数=" + semes.getLesson());
            daoUpdateAttendSemesDat.save(header, student, semes);

            if (null == aadd) {
                continue;
            }

            final List<AttendSemesSubl> sublList = getAttendSemesSublList(student, aadd);
            log.trace("月別大分類累積=" + student + ", 件数=" + getSublCount(sublList));
            daoUpdateAttendSemesSubl.save(header, student, sublList);

            final List<AttendSemesSubm> submList = getAttendSemesSubmList(student, aadd);
            log.trace("月別中分類累積=" + student + ", 件数=" + getSubmCount(submList));
            daoUpdateAttendSemesSubm.save(header, student, submList);


            if (null != conn) { conn.commit(); }
        }
    }

    private static AccumulateSemes calcAccumulateSemes(final Student student, final EntDate entDate, final CourseMst courseMst, final DateAttendanceMap attendances, final OnedayAttendanceJudge judge) {
        final Map<KenjaDateImpl, AccumulateSemes> _map = new HashMap<KenjaDateImpl, AccumulateSemes>(); // <日付, 累積>
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
            final Collection<Attendance> coll = mask(attendances.getAttendanceList(date), courseMst); // コアタイム以外を除去。
            if (coll.isEmpty()) {
                continue;
            }
            if (null == _map.get(date)) {
                _map.put(date, new AccumulateSemes());
            }
            final AccumulateSemes rui = _map.get(date);
            rui.calc(null, student, coll, date, judge);
            total.add(rui);
        }
        return total;
    }

    private static Collection<Attendance> mask(final Collection<Attendance> collection, final CourseMst courseMst) {
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

    /**
     * 日々出欠を集計する。
     * @param aadd 日々出欠
     * @param kintaiManager 勤怠マネージャー
     * @param asc 月別累積カウンタ
     */
    private static AccumulateSemes getAttendDayDatAccumulated(final AccumulateAttendDayDat aadd, final KintaiManager kintaiManager, final AccumulateSemes as) {
        Collection<KenjaDateImpl> absent = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> suspend = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> mourning = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> sick = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> notice = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> nonotice = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> late = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> early = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> lateNonotice = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> earlyNonotice = new ArrayList<KenjaDateImpl>();

        if (null != aadd) {
            for (final Kintai kintai : aadd.getDayDatMap().keySet()) {
                final Collection<KenjaDateImpl> dates = aadd.getDayDatMap().get(kintai);
                final Collection<KenjaDateImpl> count = getCount(aadd.getStudent(), dates);
                if (count.isEmpty()) {
                    continue;
                }
                final Collection<KenjaDateImpl> target;
                switch (kintai.getAltKintai().getCode()) {
                    case KintaiManager.CODE_ABSENT:
                        target = absent;
                        break;
                    case KintaiManager.CODE_SUSPEND:
                        target = suspend;
                        break;
                    case KintaiManager.CODE_MOURNING:
                        target = mourning;
                        break;
                    case KintaiManager.CODE_SICK:
                        target = sick;
                        break;
                    case KintaiManager.CODE_NOTICE:
                        target = notice;
                        break;
                    case KintaiManager.CODE_NONOTICE:
                        target = nonotice;
                        break;
                    case KintaiManager.CODE_LATE:
                        target = late;
                        break;
                    case KintaiManager.CODE_EARLY:
                        target = early;
                        break;
                    case KintaiManager.CODE_LATE_NONOTICE:
                        target = lateNonotice;
                        break;
                    case KintaiManager.CODE_EARLY_NONOTICE:
                        target = earlyNonotice;
                        break;
                    default:
                        target = null;
                }
                if (null != target) {
                    target.addAll(count);
                }
            }
        }
        final AccumulateSemes total = new AccumulateSemesConstants(as.getLesson(), as.getOffdays(), as.getAbroad(), absent, suspend, mourning,
                sick, notice, nonotice, late, early, lateNonotice, earlyNonotice);
        return total;
    }

    /**
     * 大分類日々出欠を集計する。
     * @param aadd 日々出欠
     */
    private static List<AttendSemesSubl> getAttendSemesSublList(final Student student, final AccumulateAttendDayDat aadd) {
        List<AttendSemesSubl> attendSemesSublList = new ArrayList<AttendSemesSubl>();
        for (final Kintai kintai : aadd.getDayDatSublMap().keySet()) {
            final Map<String, Collection<KenjaDateImpl>> sublMap = aadd.getDayDatSublMap().get(kintai);
            for (final String sublCd : sublMap.keySet()) {
                final Collection<KenjaDateImpl> dates = sublMap.get(sublCd);
                final Collection<KenjaDateImpl> activeDates = getActiveDates(student, dates);
                if (activeDates.isEmpty()) {
                    continue;
                }
                final AttendSemesSubl subl = new AttendSemesSubl(student, kintai, sublCd, activeDates);
                attendSemesSublList.add(subl);
            }
        }
        return attendSemesSublList;
    }

    private int getSublCount(final List<AttendSemesSubl> list) {
        int totalCount = 0;
        for (final AttendSemesSubl subl : list) {
            totalCount += subl.getCount();
        }
        return totalCount;
    }

    /**
     * 中分類日々出欠を集計する。
     * @param aadd 日々出欠
     */
    private static List<AttendSemesSubm> getAttendSemesSubmList(final Student student, final AccumulateAttendDayDat aadd) {
        final List<AttendSemesSubm> attendSemesSubmList = new ArrayList<AttendSemesSubm>();
        for (final Kintai kintai : aadd.getDayDatSubmMap().keySet()) {
            final Map<String, Map<String, Collection<KenjaDateImpl>>> sublMap = aadd.getDayDatSubmMap().get(kintai);
            for (final String sublCd : sublMap.keySet()) {
                final Map<String, Collection<KenjaDateImpl>> submMap = sublMap.get(sublCd);

                for (final String submCd : submMap.keySet()) {
                    final Collection<KenjaDateImpl> dates = submMap.get(submCd);
                    final Collection<KenjaDateImpl> activeDates = getActiveDates(student, dates);
                    if (activeDates.isEmpty()) {
                        continue;
                    }
                    final AttendSemesSubm subm = new AttendSemesSubm(student, kintai, sublCd, submCd, activeDates);
                    attendSemesSubmList.add(subm);
                }
            }
        }
        return attendSemesSubmList;
    }

    private static Collection<KenjaDateImpl> getActiveDates(final Student student, final Collection<KenjaDateImpl> dates) {
        final Set<KenjaDateImpl> rtn = new HashSet<KenjaDateImpl>();
        for (final KenjaDateImpl date : dates) {
            if (student.isActive(date)) {
                rtn.add(date);
            }
        }
        return rtn;
    }

    private static Collection<KenjaDateImpl> getCount(final Student student, final Collection<KenjaDateImpl> dates) {
        final List<KenjaDateImpl> empty1 = new ArrayList<KenjaDateImpl>();
        Collection<KenjaDateImpl> count = empty1;
        for (final KenjaDateImpl date : dates) {
            if (student.isActive(date)) {
                if (count == empty1) {
                    count = new ArrayList<KenjaDateImpl>();
                }
                count.add(date);
            }
        }
        return count;
    }


    private int getSubmCount(final List<AttendSemesSubm> list) {
        int totalCount = 0;
        for (final AttendSemesSubm subm : list) {
            totalCount += subm.getCount();
        }
        return totalCount;

    }

    private CourseMst getCourseMst(final Student student) {
        final String courseCd = student.getCourseInfo().getCourseCd();
        if (null == courseCd) {
            return null;
        }
        final CourseMst courseMst = CourseMst.getInstance(_category, courseCd);
        return courseMst;
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

            _options.getTracer().traceAttendaces(student, attendances, getCourseMst(student));
        }
        return list;
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

    /**
     * 時間割、名簿、出欠を読み込む。
     * @param term 読む込む期間
     * @return 生徒の勤怠マトリックス
     * @throws SQLException
     */
    private AccumulateAttendMatrix loadAttendMatrix(final Header header, final DbConnection dbcon, final ControlMaster cm) throws SQLException {
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
        final DaoAttendDat daoAtt = new DaoAttendDat(_category, schMatrix, meiboMatrix, term.getSDate(), term.getEDate(), attendMatrix, header, null);
        daoAtt.load(dbcon.getROConnection(), cm);

        log.debug("時間割、名簿、出欠の読み込み時間: " + sw);

        attendMatrix.generateSeatedAttendance(schMatrix, meiboMatrix, _category);
        return attendMatrix;
    }

    /**
     * 出欠を読み込む。
     * @param term 読む込む期間
     * @param kintaiManager 勤怠マネージャー
     * @return 生徒の勤怠マトリックス
     * @throws SQLException
     */
    private Map<Student, AccumulateAttendDayDat> loadAttendDayDat(final Header header, final KintaiManager kintaiManager, final DbConnection dbcon, final ControlMaster cm) throws SQLException {
        final StopWatch sw = new StopWatch();
        sw.start();
        log.debug("日々の出欠の読み込み開始");

        final Map<Student, AccumulateAttendDayDat> schregnoAccumulateAttendDayDat = new HashMap<Student, AccumulateAttendDayDat>();

        // 期間内の日々の出欠を取り込む
        final Term term = header.getTerm();
        final DaoAttendDayDat daoAttendDayDat = new DaoAttendDayDat(header, kintaiManager, schregnoAccumulateAttendDayDat, null, null);
        daoAttendDayDat.load(dbcon.getROConnection(), cm);
        final DaoAttendDaySublDat daoAttendDaySublDat = new DaoAttendDaySublDat(term, schregnoAccumulateAttendDayDat);
        daoAttendDaySublDat.load(dbcon.getROConnection(), cm);
        final DaoAttendDaySubmDat daoAttendDaySubmDat = new DaoAttendDaySubmDat(term, schregnoAccumulateAttendDayDat);
        daoAttendDaySubmDat.load(dbcon.getROConnection(), cm);

        log.debug("日々の出欠の読み込み時間: " + sw);

        return schregnoAccumulateAttendDayDat;
    }

    /**
     * 学期を得る。
     * @return 学期
     */
    protected Semester getSemester() {
        final List<Semester> list = Semester.getEnumList(_category);
        for (final Semester semester : list) {

            final KenjaDateImpl sDate = semester.getSDate();
            final KenjaDateImpl eDate = semester.getEDate();
            final KenjaDateImpl date = _options.getDate();

            if (date.compareTo(sDate) >= 0 && date.compareTo(eDate) <= 0) {
                log.info("semester=" + semester + " , date=" + _options.getDate());
                return semester;
            }
        }
        log.fatal("学期が得られない");
        return null;
    }
} // AccumulateSummaryBatchAttendDayDat

// eof
