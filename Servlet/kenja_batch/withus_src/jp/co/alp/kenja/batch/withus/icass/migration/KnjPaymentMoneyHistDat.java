// kanji=漢字
/*
 * $Id: KnjPaymentMoneyHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * PAYMENT_MONEY_HIST_DATを作る。
 * @author takaesu
 * @version $Id: KnjPaymentMoneyHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjPaymentMoneyHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjPaymentMoneyHistDat.class);

    private static final String KNJTABLE = "PAYMENT_MONEY_HIST_DAT";

    public KnjPaymentMoneyHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "入金データ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("データ件数=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MIN_DATE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     MIN(T1.KEIJO_NENTSUKI) AS KEIJO_NENTSUKI ");
        stb.append(" FROM ");
        stb.append("     SEITO_URIAGE T1, ");
        stb.append("     SEITO T2 ");
        stb.append(" WHERE ");
        stb.append("     URIAGE_NENTSUKI >= '2008-04-01' ");
        stb.append("     AND T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T2.SHIGANSHA_NO IS NOT NULL ");
        stb.append("     AND T1.KEIJO_NENTSUKI IS NOT NULL ");
        stb.append("     AND T1.KEIJO_NENTSUKI <> '' ");
        stb.append("     AND INT(T1.JUTO_KINGAKU) > 0 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN ");
        stb.append(" ORDER BY ");
        stb.append("     INT(SHIGANSHA_RENBAN) ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUHI_NONYU_DATE, ");
        stb.append("     SUM(INT(T1.GAKUHI_NONYU_KINGAKU)) AS PAYMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     SEITO_GAKUHI_NONYU_JISSEKI T1, ");
        stb.append("     MIN_DATE T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND DATE(T1.GAKUHI_NONYU_DATE) >= DATE(T2.KEIJO_NENTSUKI) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUHI_NONYU_DATE ");
        stb.append(" HAVING ");
        stb.append("     SUM(INT(T1.GAKUHI_NONYU_KINGAKU)) > 0 ");

// TODO:7月分の臨時対応
//        stb.append(" UNION ");
//        stb.append(" SELECT ");
//        stb.append("     REMARK AS SHIGANSHA_RENBAN, ");
//        stb.append("     DATE('2007-11-19') AS GAKUHI_NONYU_DATE, ");
//        stb.append("     226300 AS PAYMENT_MONEY ");
//        stb.append(" FROM ");
//        stb.append("     APPLICANT_BASE_MST ");
//        stb.append(" WHERE ");
//        stb.append("     REMARK = '9204' ");
//
//        stb.append(" UNION ");
//        stb.append(" SELECT ");
//        stb.append("     REMARK AS SHIGANSHA_RENBAN, ");
//        stb.append("     DATE('2007-12-10') AS GAKUHI_NONYU_DATE, ");
//        stb.append("     218300 AS PAYMENT_MONEY ");
//        stb.append(" FROM ");
//        stb.append("     APPLICANT_BASE_MST ");
//        stb.append(" WHERE ");
//        stb.append("     REMARK = '9296' ");

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     GAKUHI_NONYU_DATE, ");
        stb.append("     ROW_NUMBER() OVER(PARTITION BY SHIGANSHA_RENBAN) AS INQUIRY_NO, ");
        stb.append("     PAYMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" ORDER BY ");
        stb.append("     INT(SHIGANSHA_RENBAN), ");
        stb.append("     DATE(GAKUHI_NONYU_DATE) ");

        log.debug("sql=" + stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        return result;
    }

    /**
        [db2inst1@withus db2inst1]$ db2 describe table PAYMENT_MONEY_HIST_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        PAYMENT_DATE                   SYSIBM    DATE                      4     0 いいえ
        PAYMENT_DIV                    SYSIBM    VARCHAR                   2     0 いいえ
        INQUIRY_NO                     SYSIBM    VARCHAR                   6     0 いいえ
        PAYMENT_MONEY                  SYSIBM    INTEGER                   4     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          7 レコードが選択されました。
     */
    public Object[] mapToArray(final Map map) {
        final String shiganshaRenBan = (String) map.get("SHIGANSHA_RENBAN");
        final Object[] rtn = {
                _param.getApplicantNo(shiganshaRenBan),
                map.get("GAKUHI_NONYU_DATE"),
                "01",       // TODO:固定
                new Integer(((Number) map.get("INQUIRY_NO")).intValue()),
                new Integer(((Number) map.get("PAYMENT_MONEY")).intValue()),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

