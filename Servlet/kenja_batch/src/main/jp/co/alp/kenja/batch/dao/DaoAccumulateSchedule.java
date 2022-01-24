// kanji=漢字
/*
 * $Id: DaoAccumulateSchedule.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/21 10:55:20 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Map;
import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.domain.UsualSchedule.RollCalledDiv;
import jp.co.alp.kenja.common.util.KenjaMapUtils;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateScheduleMatrix;
import jp.co.alp.kenja.batch.domain.Term;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: DaoAccumulateSchedule.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class DaoAccumulateSchedule extends AbstractDaoLoader<AccumulateSchedule> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCH_CHR_DAT";

    /*pkg*/static final Log log = LogFactory.getLog(DaoAccumulateSchedule.class);

    private final AccumulateScheduleMatrix _matrix;
    private final Term _term;

    /**
     * コンストラクタ。
     * @param matrix 時間割マトリックス
     * @param term 期間
     */
    public DaoAccumulateSchedule(final AccumulateScheduleMatrix matrix, final Term term) {
        super(log);
        _matrix = matrix;
        _term = term;
    }

    /**
     * {@inheritDoc}
     */
    protected void postLoad() throws SQLException {
        log.debug("集計フラグの読み込み開始");
        DaoAccumulateSchduleCountFlag.getInstance(_matrix).load(_conn, _cm);
        DaoAccumulateScheduleExam.getInstance(_matrix, _term).load(_conn, _cm);
        log.debug("集計フラグの読み込み終了");
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    EXECUTEDATE as date,"
                + "    PERIODCD as period,"
                + "    CHAIRCD as chair,"
                + "    EXECUTED as rollCalledDiv, "
                + "    DATADIV as dataDiv "
                + "  from " + TABLE_NAME
                + "  where EXECUTEDATE between ? and ?"
                + "    and YEAR = ?"
                + "    and SEMESTER = ?"
                + "  order by"
                + "    EXECUTEDATE, PERIODCD, CHAIRCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
                _term.getSDate().getSQLDate(),
                _term.getEDate().getSQLDate(),
                cm.getCurrentYearAsString(),
                cm.getCurrentSemester().getCodeAsString(),
        };
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        Object rtn = null;
    check:
        {
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

            final RollCalledDiv roll = RollCalledDiv.getInstance(MapUtils.getIntValue(map, "rollCalledDiv", -1));
            if (null == roll) {
                rtn = "不明な出欠実施区分(rollCalledDiv)";
                break check;
            }

            final UsualSchedule.DataDiv dataDiv = UsualSchedule.DataDiv.getInstance(MapUtils.getIntValue(map, "dataDiv", 1));
            if (null == dataDiv) {
                rtn = "不明な時間割データ区分(dataDiv)";
                break check;
            }

            // 時間割インスタンスを生成
            final KenjaDateImpl date = KenjaMapUtils.getKenjaDateImpl(map, "date");
            final AccumulateSchedule sch = new AccumulateSchedule(date, period, chair, null, dataDiv);

            // マトリックスのアサイン
            _matrix.assign(sch);
            rtn = sch;
        }

        return rtn;
    }

} // DaoAccumulateSchedule

// eof
