// kanji=漢字
/*
 * $Id: KnjRecGraduateDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REC_GRADUATE_DAT を作る。
 * @author takaesu
 * @version $Id: KnjRecGraduateDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecGraduateDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecGraduateDat.class);

    public KnjRecGraduateDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "卒業認定結果"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.grd_date,"
            + "  t1.grd_div,"
            + "  t2.total"
            + " FROM"
            + "  schreg_base_mst t1 LEFT JOIN ("
            + "      SELECT"
            + "        schregno,"
            + "        sum(get_credit) as total"
            + "      FROM"
            + "        rec_credit_admits"
            + "      GROUP BY"
            + "        schregno"
            + "      ) t2"
            + "  ON t1.schregno=t2.schregno"
            + " WHERE"
            + "  t1.grd_div = '1'"// 1=卒業
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "REC_GRADUATE_DAT", this);
    }

    /** 特別活動時間数. */
    public static Integer SPECIAL_COUNT = new Integer(30);

    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("schregno"),
                "1",// 1=卒業を認める. 0=認めない
                "1",// 1=学費済み
                "1",// 1=必履修済み
                map.get("total"),
                SPECIAL_COUNT,
                Param.REGISTERCD,
        };

        return rtn;
    }
}
// eof

