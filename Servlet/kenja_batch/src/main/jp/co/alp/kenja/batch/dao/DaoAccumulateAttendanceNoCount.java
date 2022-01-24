// kanji=漢字
/*
 * $Id: DaoAccumulateAttendanceNoCount.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 14:10:12 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDate;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @version $Id: DaoAccumulateAttendanceNoCount.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class DaoAccumulateAttendanceNoCount extends AbstractDaoLoader<KenjaDateImpl> {
    /*pkg*/static final Log log = LogFactory.getLog(DaoAccumulateAttendanceNoCount.class);

    private static DaoAccumulateAttendanceNoCount instance_;

    private static final Map<String, Collection<KenjaDateImpl>> _noCountMap = new HashMap<String, Collection<KenjaDateImpl>>(); // 

    private final KenjaDateImpl _startDate;

    /*
     * コンストラクタ。
     */
    private DaoAccumulateAttendanceNoCount(final KenjaDateImpl startDate) {
        super(log);
        _startDate = startDate;
    }

    /**
     * インスタンスを得る。
     * @param startDate 時間割集計開始日
     * @return インスタンス
     */
    public static DaoAccumulateAttendanceNoCount getInstance(final KenjaDateImpl startDate
    ) {
        if (instance_ == null) {
            log.debug(" 時間割読み込み開始日 = " + startDate);
            instance_ = new DaoAccumulateAttendanceNoCount(startDate);
        }
        return instance_;
    }

    public static boolean isNoCount(final String schregno, final KenjaDate date) {
        if (null == _noCountMap.get(schregno)) {
            return false;
        }
        for (final Iterator<KenjaDateImpl> it = _noCountMap.get(schregno).iterator(); it.hasNext();) {
            final KenjaDateImpl date0 = it.next();
            if (date0.equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final StringBuffer sql = new StringBuffer();
        sql.append("select");
        sql.append("    SCHREGNO as schregno,");
        sql.append("    ATTENDDATE as date ");
        sql.append("  from ").append(DaoAttendDat.TABLE_NAME);
        sql.append("  where");
        sql.append("    YEAR = ?");
        sql.append("  and");
        sql.append("    ATTENDDATE >= ?");
        sql.append("    AND DI_CD = ?");
        sql.append("  order by");
        sql.append("    SCHREGNO, PERIODCD");
        return sql.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
                cm.getCurrentYearAsString(),
                _startDate.getSQLDate(),
                Integer.valueOf(KintaiManager.CODE_NO_COUNT),
        };
    }

    /**
     * {@inheritDoc}
     */
    // CSOFF: ExecutableStatementCount
    public Object mapToInstance(final Map<String, Object> map) {
        // CSOFF: ExecutableStatementCount
        final String schregno = MapUtils.getString(map, "schregno");
        final KenjaDateImpl date = KenjaMapUtils.getKenjaDateImpl(map, "date");
        //
        if (!_noCountMap.containsKey(schregno)) {
            _noCountMap.put(schregno, new ArrayList<KenjaDateImpl>());
        }
        _noCountMap.get(schregno).add(date);
        return _noCountMap;
    }
} // DaoAccumulateAttendanceNoCount

// eof
