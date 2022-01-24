// kanji=漢字
/*
 * $Id: KnjMonthOverPaymentDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * MONTH_OVER_PAYMENT_DATを作る。
 * @author takaesu
 * @version $Id: KnjMonthOverPaymentDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjMonthOverPaymentDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjMonthOverPaymentDat.class);

    public KnjMonthOverPaymentDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "月次生徒別過入金データ"; }


    void migrate() throws SQLException {
        final String sql;
        sql = " select " +
        "   SHIGANSHA_RENBAN, "+ 
        "   SUBSTR(DIGITS(SMALLINT(YEAR (DATE(URIAGE_NENTSUKI)))),2,4) || " +
        "   SUBSTR(DIGITS(SMALLINT(MONTH(DATE(URIAGE_NENTSUKI)))),4,2) AS YEAR_MONTH, "+
        "   ZENGETSU_MATSU_ZAN_KINGAKU AS LAST_MONTH_OVER_REMAIN, "+
        "   TOUGETSU_KANYUKIN_KINGAKU  AS MONTH_OVER_MONEY, "+
        "   HENKIN_KINGAKU             AS RE_PAY_MONEY, "+
        "   TOUGETSU_MATSU_ZAN_KINGAKU AS MONTH_OVER_MONEY_REMAIN "+
        " from " +
        "    SEITO_KANYUKIN ";
        
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "MONTH_OVER_PAYMENT_DAT ", this);
    }

   
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        
        final Object[] rtn = {
                _param.getApplicantNo((String) map.get("SHIGANSHA_RENBAN")),
                map.get("YEAR_MONTH"),
                map.get("LAST_MONTH_OVER_REMAIN"),
                map.get("MONTH_OVER_MONEY"),
                map.get("RE_PAY_MONEY"),
                map.get("MONTH_OVER_MONEY_REMAIN"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

