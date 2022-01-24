// kanji=漢字
/*
 * $Id: KnjRecReschoolingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * REC_RESCHOOLING_DAT(通信スクーリング不足)を作る。
 * @author takaesu
 * @version $Id: KnjRecReschoolingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecReschoolingDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReschoolingDat.class);

    public KnjRecReschoolingDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "通信スクーリング不足"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "SELECT DISTINCT"
            + "  nendo_code,"
            + "  shigansha_renban"
            + " FROM"
            + "  seito_shukko_schooling_sei"
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "rec_reschooling_dat", this);
    }

    /** {@inheritDoc} */
    public Object[] mapToArray(Map map) {
        final String shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String schregno = _param.getSchregno(shiganshaRenban);

        final Object[] rtn = {
                map.get("NENDO_CODE"),
                schregno,
                "S2",// 集中スクーリング不足(=補習)のコードは S2 決めうち
                Param.REGISTERCD,
        };
        return rtn;
    }

    /**
     * @deprecated 仕様変更でテーブルが SEITO_SHUKKO_JISSEKI から seito_shukko_schooling_sei に変わったため
     */
    class SeitoShukkoJisseki {
        final String _shiganshaRenban;
        final String _nendo;
        final String _shukkoNengappi;

        public SeitoShukkoJisseki(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _nendo = (String) map.get("NENDO_CODE");
            _shukkoNengappi = (String) map.get("SHUKKO_NENGAPPI");
        }

        public Object[] toArray() {
            final String schregno = _param.getSchregno(_shiganshaRenban);

            final Object[] rtn = {
                    _nendo,
                    schregno,
                    "S2",//TODO: 何が入る?
                    Param.REGISTERCD,
            };
            return rtn;
        }
    }
}
// eof
