// kanji=漢字
/*
 * $Id: DaoAccumulateSchduleCountFlag.java 75777 2020-07-31 15:00:04Z maeshiro $
 *
 * 作成日: 2006/12/26 10:14:04 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.util.KenjaMapUtils;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateScheduleMatrix;

/*
 * describe table SCH_CHR_COUNTFLG
 *
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * EXECUTEDATE                    SYSIBM    DATE                      4     0 いいえ
 * PERIODCD                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 いいえ
 * GRADE                          SYSIBM    VARCHAR                   2     0 いいえ
 * HR_CLASS                       SYSIBM    VARCHAR                   3     0 いいえ
 * COUNTFLG                       SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     8 レコードが選択されました。
 */

// TAKAESU: DaoUsualScheduleCountFlag と共通化を図りたい
/**
 * 集計フラグを読み込む。
 * @author takaesu
 * @version $Id: DaoAccumulateSchduleCountFlag.java 75777 2020-07-31 15:00:04Z maeshiro $
 */
public final class DaoAccumulateSchduleCountFlag extends AbstractDaoLoader<AccumulateSchedule> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCH_CHR_COUNTFLG";

    private static final Log log = LogFactory.getLog(DaoAccumulateSchduleCountFlag.class);
    private static final DaoAccumulateSchduleCountFlag INSTANCE = new DaoAccumulateSchduleCountFlag();

    private AccumulateScheduleMatrix _matrix;

    /*
     * コンストラクタ。
     */
    private DaoAccumulateSchduleCountFlag() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @param matrix 時間割マトリックス
     * @return インスタンス
     */
    public static DaoAccumulateSchduleCountFlag getInstance(final AccumulateScheduleMatrix matrix) {
        INSTANCE._matrix = matrix;
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    protected void postLoad() throws SQLException {
        _matrix = null;
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

            final HomeRoom hr = HomeRoom.getInstance(
                    _cm.getCategory(),
                    MapUtils.getString(map, "grade"),
                    MapUtils.getString(map, "room")
            );
            if (null == hr) {
                rtn = "不明なホームルーム(grade,room)";
                break check;
            }
            final boolean countFlag = StringUtils.equals(MapUtils.getString(map, "countFlag"), "1");

            final List<UsualSchedule.DataDiv> allowedDatadivList = Arrays.asList(
                UsualSchedule.DataDiv.BASIC,
                UsualSchedule.DataDiv.USUAL
            );
            // マトリックスから、時間割を探す
            final AccumulateSchedule schedule = _matrix.get(date, period, chair, allowedDatadivList);
            if (null == schedule) {
                rtn = "時間割が存在しない(date, period, chair)";
                break check;
            }

            // 集計フラグを設定する。
            schedule.setCountFlag(hr, countFlag);
            rtn = null;
        } // check

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
                + "    GRADE as grade,"
                + "    HR_CLASS as room,"
                + "    COUNTFLG as countFlag"
                + "  from " + TABLE_NAME
                + "  where EXECUTEDATE between ? and ?"
                + "  order by"
                + "    EXECUTEDATE, PERIODCD, CHAIRCD, GRADE, HR_CLASS";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        final List<KenjaDateImpl> dateSet = _matrix.getDateSet();
        if (dateSet.size() == 0) {
            return new Object[] {
                    KenjaDateImpl.getInstance(9999, 12, 31).getSQLDate(),
                    KenjaDateImpl.getInstance(9999, 12, 31).getSQLDate(),
            };
        }
        return new Object[] {
                dateSet.get(0).getSQLDate(),
                dateSet.get(dateSet.size() - 1).getSQLDate(),
        };
    }
} // DaoAccumulateSchduleCountFlag

// eof
