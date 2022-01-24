/*
 * $Id: DaoAccumulateScheduleExam.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/03/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateScheduleMatrix;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.ExamItem;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

/*
 * describe table SCH_CHR_TEST
 *
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * EXECUTEDATE                    SYSIBM    DATE                      4     0 いいえ
 * PERIODCD                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 いいえ
 * TESTKINDCD                     SYSIBM    VARCHAR                   2     0 はい
 * TESTITEMCD                     SYSIBM    VARCHAR                   2     0 はい
 * YEAR                           SYSIBM    VARCHAR                   4     0 はい
 * EXECUTED                       SYSIBM    VARCHAR                   1     0 はい
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     10 レコードが選択されました。
 */

/**
 * 考査を読み込む。
 * @author maesiro
 * @version $Id: DaoAccumulateScheduleExam.java 74552 2020-05-27 04:41:22Z maeshiro $
 */

public final class DaoAccumulateScheduleExam extends AbstractDaoLoader<UsualSchedule> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCH_CHR_TEST";

    /*pkg*/static final Log log = LogFactory.getLog(DaoAccumulateScheduleExam.class);

    private static final DaoAccumulateScheduleExam INSTANCE = new DaoAccumulateScheduleExam();

    private AccumulateScheduleMatrix _matrix;
    private Term _term;

    private DaoAccumulateScheduleExam() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @param matrix 時間割マトリックス
     * @param term 期間
     * @return インスタンス
     */
    public static DaoAccumulateScheduleExam getInstance(
            final AccumulateScheduleMatrix matrix,
            final Term term
    ) {
        INSTANCE._matrix = matrix;
        INSTANCE._term = term;
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        Object rtn = null;
    check:
        {
            final MyEnum.Category cat = _cm.getCategory();
            final KenjaDateImpl date = KenjaMapUtils.getKenjaDateImpl(map, "date");
            final Period period = Period.getInstance(cat, MapUtils.getString(map, "period"));
            if (null == period) {
                rtn = "不明な校時コード(period)";
                break check;
            }
            final Chair chair = Chair.getInstance(cat, MapUtils.getString(map, "chair"));
            if (null == chair) {
                rtn = "不明な講座コード(chair)";
                break check;
            }

            final List<UsualSchedule.DataDiv> allowedDatadivList = Collections.singletonList(UsualSchedule.DataDiv.EXAM);
            // マトリックスから、時間割を探す
            final AccumulateSchedule schedule = _matrix.get(date, period, chair, allowedDatadivList);
            if (null == schedule) {
                rtn = "時間割が存在しない(date, period, chair)";
                break check;
            }

            final UsualSchedule.DataDiv dataDiv = schedule.getDataDiv();
            if (!UsualSchedule.DataDiv.EXAM.equals(dataDiv)) {
                rtn = "考査時間割ではない:" + dataDiv;
                break check;
            }

            final ExamItem examItem = ExamItem.getExamItemWithScoreDiv(
                    cat,
                    _cm,
                    MapUtils.getString(map, "examKindCd"),
                    MapUtils.getString(map, "examItemCd")
            );

            // 通常時間割に考査項目を設定する
            schedule.setExamItem(examItem);

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
                + "    CHAIRCD as chair,"
                + "    TESTKINDCD as examKindCd,"
                + "    TESTITEMCD as examItemCd "
                + "  from " + TABLE_NAME
                + "  where EXECUTEDATE between ? and ?"
                + "    and YEAR = ?"
                + "    and SEMESTER = ?"
                + "  order by"
                + "    EXECUTEDATE, PERIODCD, CHAIRCD, TESTKINDCD, TESTITEMCD";
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
} // DaoAccumulateScheduleExam

// eof
