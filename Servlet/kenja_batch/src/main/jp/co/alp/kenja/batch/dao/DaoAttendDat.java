// kanji=漢字
/*
 * $Id: DaoAttendDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 14:10:12 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.domain.UsualSchedule.DataDiv;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.KenjaMapUtils;
import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.AccumulateMeiboMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateScheduleMatrix;
import jp.co.alp.kenja.batch.accumulate.option.Header;

/*
 * describe table ATTEND_DAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
 * ATTENDDATE                     SYSIBM    DATE                      4     0 いいえ
 * PERIODCD                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 はい
 * DI_CD                          SYSIBM    VARCHAR                   2     0 はい
 * DI_REMARK                      SYSIBM    VARCHAR                  60     0 はい
 * YEAR                           SYSIBM    VARCHAR                   4     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     9 レコードが選択されました。
 */

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: DaoAttendDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class DaoAttendDat extends AbstractDaoLoader<Attendance> {
    /*pkg*/static final Log log = LogFactory.getLog(DaoAttendDat.class);

    static final String TABLE_NAME = "ATTEND_DAT";
    private final MyEnum.Category _category;
    private final AccumulateScheduleMatrix _schMatrix;
    private final AccumulateMeiboMatrix _meiboMatrix;
    private final KenjaDateImpl _sDate;
    private final KenjaDateImpl _eDate;
    private final AccumulateAttendMatrix _attendMatrix;
    private final Header _header;
    private final String[] _schoolKind;

    /**
     * コンストラクタ。
     * @param category カテゴリ
     * @param schMatrix 時間割マトリックス
     * @param meiboMatrix 名簿マトリックス
     * @param sDate 開始日
     * @param eDate 終了日
     * @param attendMatrix 出欠マトリックス
     */
    public DaoAttendDat(
            final MyEnum.Category category,
            final AccumulateScheduleMatrix schMatrix,
            final AccumulateMeiboMatrix meiboMatrix,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate,
            final AccumulateAttendMatrix attendMatrix,
            final Header header,
            final String[] schoolKind
    ) {
        super(log);

        _category = category;
        _schMatrix = schMatrix;
        _meiboMatrix = meiboMatrix;
        _sDate = sDate;
        _eDate = eDate;
        _attendMatrix = attendMatrix;
        _header = header;
        _schoolKind = schoolKind;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final StringBuffer sql = new StringBuffer();
        sql.append("select");
        sql.append("    SCHREGNO as schregno,");
        sql.append("    ATTENDDATE as date,");
        sql.append("    PERIODCD as period,");
        sql.append("    CHAIRCD as chair,");
        sql.append("    DI_CD as kintai,");
        sql.append("    DI_REMARK as remark");
        sql.append("  from ").append(TABLE_NAME);
        sql.append("  where");
        sql.append("    YEAR = ?");
        sql.append("  and");
        sql.append("    ATTENDDATE between ? AND ?");
        if (!ArrayUtils.isEmpty(_schoolKind)) {
            sql.append("  and SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT I1 INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE WHERE I1.YEAR = ? AND I2.SCHOOL_KIND IN " + SQLUtils.whereIn(true, _schoolKind) + ") ");
        }
        sql.append("  order by");
        sql.append("    SCHREGNO, PERIODCD");
        return sql.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        final List<Object> args = new ArrayList<Object>();
        args.add(cm.getCurrentYearAsString());
        args.add(_sDate.getSQLDate());
        args.add(_eDate.getSQLDate());
        if (!ArrayUtils.isEmpty(_schoolKind)) {
            args.add(_header.getNendo());
        }
        return args.toArray(new Object[args.size()]);
    }

    /**
     * {@inheritDoc}
     */
    // CSOFF: ExecutableStatementCount
    public Object mapToInstance(final Map<String, Object> map) {
        // CSOFF: ExecutableStatementCount
        Object rtn = null;
    check:
        {
            final Student student = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "schregno"));
            if (null == student) {
                rtn = "不明な学籍番号(schregno)";
                break check;
            }
            final KenjaDateImpl date = KenjaMapUtils.getKenjaDateImpl(map, "date");
            final Period period = Period.getInstance(_cm.getCategory(), MapUtils.getString(map, "period"));
            if (null == period) {
                rtn = "不明な校時コード(period)";
                break check;
            }
            final Chair chair = Chair.getInstance(_cm.getCategory(), MapUtils.getString(map, "chair"));
            if (null == chair) {
                rtn = "不明な講座コード(chair)";
                break check;
            }
            final Kintai kintai = Kintai.getInstance(_cm.getCategory(), MapUtils.getString(map, "kintai"));
            if (null == kintai) {
                rtn = "不明な勤怠コード(kintai)";
                break check;
            }
            final String remark = MapUtils.getString(map, "remark");
            AccumulateSchedule schedule = findSchedule(student, date, period, chair);
            if (null == schedule) {
                rtn = "時間割が無い";
                break check;
            }
            // TAKAESU: 名簿のチェックの前に、在籍しているか否かをチェックした方が処理効率良いはず
            if (!_meiboMatrix.isEnable(date, schedule.getChair(), student)) {
                rtn = "名簿に無い";
                break check;
            }
            if (schedule.isRollCalledDivNOTYET() && kintai.isSeated()) {
                rtn = "時間割が未出欠、かつ、出欠データが「出席」";
                break check;
            }

            //
            final Attendance attendance = new Attendance(_category, student, schedule, kintai, remark);
            _attendMatrix.add(attendance);
        }
        return rtn;
    }

    private AccumulateSchedule findSchedule(final Student student, final KenjaDateImpl date, final Period period, final Chair chair) {
        final List<DataDiv> allowedDatadivList = Arrays.asList(
                UsualSchedule.DataDiv.BASIC,
                UsualSchedule.DataDiv.USUAL,
                UsualSchedule.DataDiv.EXAM
            );
        final AccumulateSchedule schedule0 = _schMatrix.get(date, period, chair, allowedDatadivList);
        if (null != schedule0) {
            return schedule0;
        }
        AccumulateSchedule schedule = null;
        final List<AccumulateSchedule> schedules = _schMatrix.get(date, period);
        if (null == schedules) {
            return null;
        }
        for (final AccumulateSchedule sch : schedules) {
            if (_meiboMatrix.isEnable(date, sch.getChair(), student)) {
                schedule = sch;
                break;
            }
        }
        if (null != schedule) {
            logErrorReplacedSchedule(student, date, period, chair, schedule);
        }
        return schedule;
    }

    private static void logErrorReplacedSchedule(final Student student, final KenjaDateImpl date, final Period period, final Chair chair,
            final AccumulateSchedule replacedSchedule) {
        final String spr = " ";
        final StringBuffer info1 = new StringBuffer();
        final StringBuffer info2 = new StringBuffer();
        info1.append("出欠データの講座が正しくありません。").append(spr).append(student).append(spr).append(date).append(spr).append(period).append(chair);
        info2.append("  時間割 ").append(replacedSchedule).append(" に置き換えて集計します。");
        log.info(info1);
        log.info(info2);
    }

} // DaoAccumulateAttendance

// eof
