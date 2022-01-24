// kanji=漢字
/*
 * $Id: DaoBatchChairStudentOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.absencehigh;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.dao.DaoBatchWithCurriculum;
import jp.co.alp.kenja.batch.dao.DaoSemester9;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.dao.query.DaoChairsUtils;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChairsHolder;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.Student.Transfer;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.util.KenjaMapUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 年間の講座名簿を読み込む。
 * @see jp.co.alp.kenja.common.dao.query.DaoChairStudent
 * @version $Id: DaoBatchChairStudentOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoBatchChairStudentOfYear extends AbstractDaoLoader<Term> {
    /** テーブル名 */
    public static final String TABLE_NAME = "CHAIR_STD_DAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoBatchChairStudentOfYear.class);
    private static final DaoBatchChairStudentOfYear INSTANCE = new DaoBatchChairStudentOfYear();
    private final Map<Semester, Map<String, Map<String, List<Term>>>> _semesterChairs = new HashMap<Semester, Map<String, Map<String, List<Term>>>>();

    private ChairsHolder _chairsHolder;

    /*
     * コンストラクタ。
     */
    private DaoBatchChairStudentOfYear() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static DaoBatchChairStudentOfYear getInstance() {
        return getInstance(null, null);
    }

    /**
     * インスタンスを得る。
     * @param chairsHolder 講座または群
     * @param date 名簿の適用終了日付。nullの場合は学期末が指定されたものとして扱う。
     * @return インスタンス
     */
    public static DaoBatchChairStudentOfYear getInstance(
            final ChairsHolder chairsHolder,
            final KenjaDateImpl date
    ) {
        INSTANCE._chairsHolder = chairsHolder;
        return INSTANCE;
    }

    /**
     * 時間割が生徒の受講期間に含まれているか
     * @param category カテゴリー
     * @param schedule 時間割
     * @param student 生徒
     * @param params パラメータ
     * @return 時間割が生徒の受講期間に含まれているか
     */
    private boolean isChairContains(final UsualSchedule schedule, final Student student, final KenjaParameters params) {

        final KenjaDateImpl date = (KenjaDateImpl) schedule.getDate();
        final List<Term> chairTermList = getStudentChairTermList(schedule, student, params);
        if (chairTermList.isEmpty()) {
            //log.debug("  講座 " + schedule.getChair() + "に 生徒" + student + " は登録していません。");
            return false;
        }

        // 講座の時間割が受講期間リストに含まれているか
        boolean contains = false;
        for (final Term chairTerm : chairTermList) {
            contains = termContains(chairTerm, date) || contains;
        }
        if (!contains) {
            log.debug(" 時間割" + schedule + "を生徒" + student + "は受講していません。受講期間リスト=" + ArrayUtils.toString(chairTermList));
            return contains;
        }

        // 講座の時間割が異動期間に含まれているか
        final List<Transfer> list = student.getTransfers();
        for (final Transfer tf : list) {
            if (!termContains(new Term(tf.getSdate(), tf.getEdate()), date)) {
                continue;
            }
            if (Student.TransferCd.STUDY_ABROAD.equals(tf.getTransferCd())) {
                return false;
            }
            if (Student.TransferCd.TAKE_OFF_SCHOOL.equals(tf.getTransferCd())) {
                return BatchSchoolMaster.getBatchSchoolMaster().offdaysIsKekka();
            }
        }
        //log.debug("isActive? = " + student.isActive(date));
        return student.isActive(date);
    }

    /**
     * {@inheritDoc}
     */
    protected void preLoad() throws SQLException {
        if (null == _chairsHolder || _chairsHolder.getChairs().isEmpty()) {
            final Collection<Chair> chairs = Chair.getEnumList(_cm.getCategory());
            for (final Chair chair : chairs) {
                chair.removeAllStudents();
            }
        } else {
            final Collection<Chair> chairs = _chairsHolder.getChairs();
            for (final Chair chair : chairs) {
                chair.removeAllStudents();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        Object rtn = null;
    check:
        {
            final Chair chair = Chair.getInstance(_cm.getCategory(), MapUtils.getString(map, "chair"));
            if (null == chair) {
                rtn = "不明な講座コード(chair)";
                break check;
            }
            final Student student = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "schregno"));
            if (null == student) {
                rtn = "不明な学籍番号(schregno)";
                break check;
            }
            final KenjaDateImpl sDate = KenjaMapUtils.getKenjaDateImpl(map, "sDate");
            if (null == sDate) {
                rtn = "不明な日付(sDate)";
                break check;
            }
            if (!Semester.getInstance(_cm.getCategory(), DaoSemester9.SEMESTER_9).isValidDate(sDate)) {
                rtn = "日付が年間外(sDate)";
                break check;
            }

            chair.addStudent(sDate, student);

            final Semester semester = Semester.getInstance(_cm.getCategory(), MapUtils.getIntValue(map, "semester"));
            if (!_semesterChairs.containsKey(semester)) {
                _semesterChairs.put(semester, new TreeMap<String, Map<String, List<Term>>>());
            }

            final Map<String, Map<String, List<Term>>> chairStudents = _semesterChairs.get(semester);
            if (!chairStudents.containsKey(chair.getCode())) {
                chairStudents.put(chair.getCode(), new TreeMap<String, List<Term>>());
            }
            final Map<String, List<Term>> studentsChairTerms = chairStudents.get(chair.getCode());
            if (!studentsChairTerms.containsKey(student.getCode())) {
                studentsChairTerms.put(student.getCode(), new ArrayList<Term>());
            }

            final List<Term> studentChairTermList = studentsChairTerms.get(student.getCode());

            final KenjaDateImpl eDate = KenjaMapUtils.getKenjaDateImpl(map, "eDate");
            studentChairTermList.add(new Term(sDate, eDate));

            rtn = null;
        } // check:
        return rtn;
    }

    /**
     * 講座時間割の生徒を得る
     * @param schedule 講座時間割
     * @param params パラメータ
     * @return 講座時間割の生徒のコレクション
     */
    private Collection<Student> getChairStudent(final UsualSchedule schedule, final KenjaParameters params) {
        final List<Student> rtn = new ArrayList<Student>();
        for (final String schregno : getChairStudentMap(schedule, params).keySet()) {
            final Student student = Student.getInstance(_cm.getCategory(), schregno);
            rtn.add(student);
        }
        return rtn;
    }

    /**
     * 講座時間割の有効な生徒を得る
     * @param cm コントロール・マスタ
     * @param schedule 講座時間割
     * @param params パラメータ
     * @return 講座時間割の有効な生徒のコレクション
     */
    public Collection<Student> getChairEnabledStudent(final ControlMaster cm, final UsualSchedule schedule, final KenjaParameters params) {
        _cm = cm;
        final List<Student> rtn = new ArrayList<Student>();
        for (final Student student : getChairStudent(schedule, params)) {
            if (isChairContains(schedule, student, params)) {
                rtn.add(student);
            }
        }
        _cm = null;
        return rtn;
    }

    private List<Term> getStudentChairTermList(final UsualSchedule schedule, final Student student, final KenjaParameters params) {
        final Semester semester = DaoSemester9.getInstance().getSemester(_cm.getCategory(), (KenjaDateImpl) schedule.getDate());
        final DaoBatchWithCurriculum.Loaders loaders = new DaoBatchWithCurriculum().getLoaders(params);
        final DaoBatchChairOfYear daoChair = loaders.getDaoBatchChairOfYearInstance();
        if (!daoChair.contains(semester, schedule.getChair())) {
            return null;
        }
        final HomeRoom homeRoom = DaoBatchStudentOfYear.getHomeRoom(semester.getCodeAsString(), student);
        if (homeRoom == null || !schedule.getCountFlag(homeRoom)) {
            //log.error("生徒のホームルームが設定されていません。時間割=" + schedule + " 生徒= " + student + ".");
            return Collections.emptyList();
        }
        return getChairStudentMap(schedule, params).get(student.getCode());
    }

    private Map<String, List<Term>> getChairStudentMap(final UsualSchedule schedule, final KenjaParameters params) {
        final Semester semester = DaoSemester9.getInstance().getSemester(_cm.getCategory(), (KenjaDateImpl) schedule.getDate());
        final DaoBatchWithCurriculum.Loaders loaders = new DaoBatchWithCurriculum().getLoaders(params);
        final DaoBatchChairOfYear daoChair = loaders.getDaoBatchChairOfYearInstance();

        final Map<String, Map<String, List<Term>>> semesterChairs = _semesterChairs.get(semester);
        if (semesterChairs == null || !daoChair.contains(semester, schedule.getChair())) {
            log.error(" 学期" + semester + " に講座 " + schedule.getChair() + "は登録されていません。");
            return Collections.emptyMap();
        }
        final Map<String, List<Term>> chairStudents = semesterChairs.get(schedule.getChair().getCode());
        if (chairStudents == null) {
            //log.error("  学期" + semester + " の講座" + schedule.getChair() + " に生徒が登録されていません。");
            return Collections.emptyMap();
        }
        return chairStudents;
    }

    /**
     * 区間が日付を含んでいるか
     * @param term 区間
     * @param date 日付
     * @return 区間が日付を含んでいるならtrue、そうでなければfalse
     */
    private static boolean termContains(final Term term, final KenjaDateImpl date) {
        return term.getSDate().compareTo(date) <= 0 && (null == term.getEDate() || date.compareTo(term.getEDate()) <= 0);
    }

    private static void sqlCommon(final StringBuffer sql) {
        sql.append("select");
        sql.append("    SEMESTER as semester,");
        sql.append("    CHAIRCD as chair,");
        sql.append("    SCHREGNO as schregno,");
        sql.append("    APPDATE as sDate,");
        sql.append("    APPENDDATE as eDate");
        sql.append("  from ").append(TABLE_NAME);
        sql.append("  where YEAR = ?");
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {

        final StringBuffer sql;
        if (null == _chairsHolder || _chairsHolder.getChairs().isEmpty()) {
            log.debug("講座指定なし");
            sql = new StringBuffer(1024);

            sqlCommon(sql);
        } else {
            final Collection<Chair> chairs = _chairsHolder.getChairs();

            log.debug("講座指定あり:" + ClassUtils.getShortClassName(_chairsHolder.getClass()) + "; 講座数=" + chairs.size());
            sql = new StringBuffer(1024 + (16 * chairs.size()));

            sqlCommon(sql);
            DaoChairsUtils.sqlChairCondition(" and ", "CHAIRCD", sql, chairs);
        }

        // 学籍番号は'order by'句に含めない
        sql.append("  order by chair, sDate");

        final String rtn = sql.toString();
        if (log.isDebugEnabled()) {
            log.debug("sql=[" + rtn + "]");
        }

        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoBatchChairStudentOfYear

// eof
