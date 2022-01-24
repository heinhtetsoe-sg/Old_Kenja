// kanji=漢字
/*
 * $Id: KnjTextOrderHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TEXT_ORDER_HIST_DATを作る。
 * @author takaesu
 * @version $Id: KnjTextOrderHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextOrderHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjTextOrderHistDat.class);
    static final String KNJTABLE = "TEXT_ORDER_HIST_DAT";

    public KnjTextOrderHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "教科書発注管理データ"; }

    void migrate() throws SQLException {
        final String sql = getSql();
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        _runner.listToKnj(result, KNJTABLE, this);
        log.debug("データ件数" + result.size());
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table text_order_hist_dat

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        ORDER_SEQ                      SYSIBM    INTEGER                   4     0 いいえ
        ORDER_DATE                     SYSIBM    DATE                      4     0 いいえ
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          4 レコードが選択されました。
     */

    private String getSql() {
        /*TEXT_KONYU_NO がかぶってわいけないので TEXT_HATCHU_DATE をとりあえずMAXをとっています。
        :TAKARA 追記(2008/10/03)、
        連番をふったテーブルからORDER_SEQを参照しているため、
        GROUP BYをしないように変更しました。
        */
        final String resql = " SELECT "
                            + "     L1.ORDER_SEQ AS TEXT_KONYU_NO, "
                            + "     T1.TEXT_HATCHU_DATE "
                            + " FROM "
                            + "     SEITO_TEXT_KONYU T1"
                            + " LEFT JOIN TEXT_SEQ_MASTER_TMP L1 ON L1.NENDO_CODE = T1.NENDO_CODE"
                            + "     AND L1.TEXT_KONYU_NO = T1.TEXT_KONYU_NO"
                            + " WHERE "
                            + "     T1.TEXT_HATCHU_DATE IS NOT NULL ";

        log.debug(resql);
        return resql;
    }

    public Object[] mapToArray(Map map) {
        final Object[] rtn = {
                map.get("TEXT_KONYU_NO"),
                map.get("TEXT_HATCHU_DATE"),
                Param.REGISTERCD,
        };

        return rtn;
    }
}
// eof

