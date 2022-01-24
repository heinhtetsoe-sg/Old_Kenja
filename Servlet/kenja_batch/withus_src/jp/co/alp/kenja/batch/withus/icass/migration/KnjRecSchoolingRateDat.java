// kanji=漢字
/*
 * $Id: KnjRecSchoolingRateDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * REC_SCHOOLING_RATE_DAT を作る。
 * @author takaesu
 * @version $Id: KnjRecSchoolingRateDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecSchoolingRateDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecSchoolingRateDat.class);

    /** 割. 100%とする. */
    public final Integer WARI = new Integer(10);

    private String _nendo;
    private int _classcd;

    public KnjRecSchoolingRateDat() {
        super();
    }

    public void migrate(final String nendo,int classcd) throws SQLException {
        _nendo = nendo;
        _classcd = classcd;
        migrate();
    }

    /** {@inheritDoc} */
    String getTitle() { return "通信スクーリング割合"; }

    void migrate() throws SQLException {
        final String sql;
        sql = 
            " WITH COMMITED AS ( "
            + " SELECT "
            + "  T2.SEITO_NO AS SCHREGNO, "
            + "  T1.NENDO_CODE AS YEAR, "
            + "  MIN(T1.SHUKKO_NENGAPPI) AS COMMITED_S, "
            + "  MAX(T1.SHUKKO_NENGAPPI) AS COMMITED_E "
            + " FROM SEITO_SHUKKO_JISSEKI T1 "
            + "     INNER JOIN SEITO T2 ON "
            + "         T2.SHIGANSHA_RENBAN = T1.SHIGANSHA_RENBAN "
            + " GROUP BY T2.SEITO_NO, T1.NENDO_CODE"
            + " ), "
            + " BASE AS( "
            + " SELECT "
            + "  T1.YEAR, "
            + "  T1.CLASSCD, "
            + "  T1.CURRICULUM_CD, "
            + "  T1.SUBCLASSCD, "
            + "  T1.SCHREGNO, "
            + "  SUM(T1.GET_VALUE) AS TOTAL_VALUE "
            + " FROM "
            + "  REC_SCHOOLING_DAT T1 "
            + " WHERE "
            + "  YEAR='" + _nendo + "' AND "
            + "  INT(T1.CLASSCD) = "+_classcd+" "
            + " GROUP BY "
            + "  T1.YEAR, "
            + "  T1.CLASSCD, "
            + "  T1.CURRICULUM_CD, "
            + "  T1.SUBCLASSCD, "
            + "  T1.SCHREGNO "
            + " ) "
            + " SELECT "
            + "  BASE.YEAR, "
            + "  BASE.CLASSCD, "
            + "  BASE.CURRICULUM_CD, "
            + "  BASE.SUBCLASSCD, "
            + "  BASE.SCHREGNO, "
            + "  BASE.TOTAL_VALUE, "
            + "  COMMITED.COMMITED_S, "
            + "  COMMITED.COMMITED_E "
            + " FROM "
            + "  BASE " 
            + " LEFT JOIN COMMITED ON "
            + "   BASE.SCHREGNO = COMMITED.SCHREGNO AND "
            + "   BASE.YEAR = COMMITED.YEAR "
            ;
        log.debug("sql=" + sql);
        final List result = _runner.mapListQuery(sql);
        log.debug("REC_SCHOOLING_RATE_DAT に保存すべきデータ件数=" + result.size());

        _runner.listToKnj(result, "REC_SCHOOLING_RATE_DAT", this);
    }

    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        //TODO: 条件を満たしていれば、「完了」状態にしたい
        final Object[] rtn = {
                map.get("YEAR"),
                map.get("CLASSCD"),
                map.get("CURRICULUM_CD"),
                map.get("SUBCLASSCD"),
                map.get("SCHREGNO"),
                "S1",
                WARI,
                map.get("COMMITED_S"),   // 8. 完了期間開始日
                map.get("COMMITED_E"),   // 9. 完了期間終了日
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

