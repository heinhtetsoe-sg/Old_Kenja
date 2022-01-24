// kanji=漢字
/*
 * $Id: KnjOverPaymentHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OVER_PAYMENT_HIST_DATを作る。
 * @author takaesu
 * @version $Id: KnjOverPaymentHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjOverPaymentHistDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjOverPaymentHistDat.class);
    public static final DecimalFormat _inquiryNoFormat = new DecimalFormat("000000");
    private static final String KNJTABLE = "OVER_PAYMENT_HIST_DAT"; 

    public KnjOverPaymentHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "過入金履歴データ"; }

    void migrate() throws SQLException {
        final StringBuffer sql = getSql();
        final List result;
        int inquiryNo = 0;
        String beforMoji = "";
        try {
            result = (List) _runner.query(_db2.conn, sql.toString(), _handler);
        } catch (SQLException e){
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (HashMap) iter.next();
            final String applicantNo = (String) map.get("SHIGANSHA_RENBAN");
            final String overPayDate = (String) map.get("URIAGE_NENTSUKI");
            if (beforMoji == null || beforMoji != applicantNo + overPayDate) {
                inquiryNo = 0;
            } else {
                inquiryNo++;
            }
            final String setInquiryNo = _inquiryNoFormat.format(inquiryNo);
            map.put("INQUIRY_NO", setInquiryNo);
            beforMoji = applicantNo + overPayDate;
        }

        _runner.listToKnj(result, KNJTABLE, this);
    }

    private StringBuffer getSql() {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KANYU_ST AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     MAX(T1.URIAGE_NENTSUKI) AS URIAGE_NENTSUKI, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     SEITO_KANYUKIN T1, ");
        stb.append("     SEITO T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T1.ZENGETSU_MATSU_ZAN_KINGAKU = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     T1.TOUGETSU_KANYUKIN_KINGAKU AS OVER_PAYMENT ");
        stb.append(" FROM ");
        stb.append("     SEITO_KANYUKIN T1 ");
        stb.append(" WHERE ");
        stb.append("     INT(T1.TOUGETSU_KANYUKIN_KINGAKU) > 0 ");
        stb.append("     AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             KANYU_ST T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("             AND DATE(T1.URIAGE_NENTSUKI) >= T2.URIAGE_NENTSUKI ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     INT(T1.SHIGANSHA_RENBAN), ");
        stb.append("     DATE(T1.URIAGE_NENTSUKI) ");

        return stb;
    }
    /*
     * [db2inst1@withus script]$ db2 describe table over_payment_hist_dat

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        OVER_PAY_DATE                  SYSIBM    DATE                      4     0 いいえ
        OVER_PAYMENT_DIV               SYSIBM    VARCHAR                   2     0 いいえ
        INQUIRY_NO                     SYSIBM    VARCHAR                   6     0 いいえ
        OVER_PAYMENT                   SYSIBM    INTEGER                   4     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい

        7 レコードが選択されました。
    */
    public Object[] mapToArray(Map map) {
        final String siganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String applicantNo = _param.getApplicantNo(siganshaRenban);
        final Object[] rtn = {
                applicantNo,
                map.get("URIAGE_NENTSUKI"),
                "02",
                map.get("INQUIRY_NO"),
                map.get("OVER_PAYMENT"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

