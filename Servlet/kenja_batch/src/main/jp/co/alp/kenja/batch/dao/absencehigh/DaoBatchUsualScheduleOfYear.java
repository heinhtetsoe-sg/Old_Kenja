// kanji=漢字
/*
 * $Id: DaoBatchUsualScheduleOfYear.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.absencehigh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.dao.DaoSemester9;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDate;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 通常時間割(年間)を読み込む。
 * @version $Id: DaoBatchUsualScheduleOfYear.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class DaoBatchUsualScheduleOfYear extends AbstractDaoLoader<UsualSchedule> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCH_CHR_DAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoBatchUsualScheduleOfYear.class);
    private static DaoBatchUsualScheduleOfYear instance_;

    /** 年間の時間割リスト */
    private static final List<UsualSchedule> SCHEDULE_LIST = new ArrayList<UsualSchedule>();

    private final KenjaDateImpl _startDate;

    /*
     * コンストラクタ。
     */
    private DaoBatchUsualScheduleOfYear(final KenjaDateImpl startDate) {
        super(log);
        _startDate = startDate;
    }

    /**
     * インスタンスを得る。
     * @param startDate 時間割集計開始日
     * @return インスタンス
     */
    public static DaoBatchUsualScheduleOfYear getInstance(final KenjaDateImpl startDate
    ) {
        if (instance_ == null) {
            log.debug(" 時間割読み込み開始日 = " + startDate);
            instance_ = new DaoBatchUsualScheduleOfYear(startDate);
        }
        return instance_;
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static DaoBatchUsualScheduleOfYear getInstance() {
        return instance_;
    }

    /**
     * 読み込んだ時間割のリストを得る。
     * @return 読み込んだ時間割のリスト
     */
    public List<UsualSchedule> getScheduleList() {
        return SCHEDULE_LIST;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        Object rtn = null;
    check:
        {
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

            // 通常時間割のインスタンスを生成(施設や年組毎の集計フラグなどは別途)
            final UsualSchedule schedule = new UsualSchedule(date, period, chair, null, UsualSchedule.DataDiv.USUAL);

            SCHEDULE_LIST.add(schedule);
            rtn = schedule;
        } // check:

        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    EXECUTEDATE as date,"
                + "    PERIODCD as period,"
                + "    CHAIRCD as chair"
                + "  from " + TABLE_NAME
                + "  where EXECUTEDATE between ? and ?"
                + "    and EXECUTEDATE >= ? "
                + "    and YEAR = ?"
                + "  order by"
                + "    EXECUTEDATE, PERIODCD, CHAIRCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        final Semester sem9 = Semester.getInstance(_cm.getCategory(), DaoSemester9.SEMESTER_9);

        final KenjaDate[] term = new KenjaDate[] {
                sem9.getSDate(),
                sem9.getEDate(),
        };

        return new Object[] {
            ((KenjaDateImpl) term[0]).getSQLDate(),
            ((KenjaDateImpl) term[1]).getSQLDate(),
            _startDate.getSQLDate(),
            cm.getCurrentYearAsString(),
        };
    }
} // DaoBatchUsualScheduleOfYear

// eof
