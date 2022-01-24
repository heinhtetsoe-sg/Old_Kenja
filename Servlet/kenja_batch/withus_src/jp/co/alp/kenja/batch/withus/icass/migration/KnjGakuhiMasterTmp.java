// kanji=漢字
/*
 * $Id: KnjGakuhiMasterTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/09/04 17:05:35 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GAKUHI_MASTER_TMPを作る。
 * @author m-yama
 * @version $Id: KnjGakuhiMasterTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjGakuhiMasterTmp extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjGakuhiMasterTmp.class);

    public static final String ICASS_TABLE = "GAKUHI_MASTER";
    final public static DecimalFormat _commodityCdFormat = new DecimalFormat("000");

    public KnjGakuhiMasterTmp() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "商品マスタTMP"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        try {
            saveKnj(list);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("更新処理中にエラー! rollback した。");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        String befHimoku = "";
        int setHimokuRenBan = 1;
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final String himoku = (String) map.get("GAKUHI_HIMOKU_CODE");
            setHimokuRenBan = befHimoku.equals(himoku) ? setHimokuRenBan : 1;
            final GakuhiMasterTmp gakuhiMasterTmp = new GakuhiMasterTmp(map, setHimokuRenBan);
            rtn.add(gakuhiMasterTmp);
            setHimokuRenBan++;
            befHimoku = himoku;
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH URIAGE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     MAX(T1.SEITO_URIAGE_RENBAN) AS SEQ, ");
        stb.append("     T1.URIAGE_NENTSUKI, ");
        stb.append("     YEAR(T1.URIAGE_NENTSUKI) AS U_YEAR, ");
        stb.append("     MONTH(T1.URIAGE_NENTSUKI) AS U_MONTH, ");
        stb.append("     MAX(T1.KEIJO_NENTSUKI) AS SUMMING_UP_DATE, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE, ");
        stb.append("     CASE WHEN T1.GAKUHI_HIMOKU_CODE = '02' ");
        stb.append("          THEN '99' ");
        stb.append("          ELSE MAX(T1.SEITO_URIAGE_RENBAN) ");
        stb.append("     END AS ORDER_CD, ");
        stb.append("     SUM(CAST(T1.URIAGE_KINGAKU_ZEINUKI AS INT)) AS PRICE, ");
        stb.append("     SUM(CAST(T1.URIAGE_KINGAKU_ZEIKIN AS INT)) AS TAX, ");
        stb.append("     SUM(CAST(T1.JUTO_KINGAKU AS INT)) AS PAYMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     SEITO_URIAGE T1, ");
        stb.append("     SEITO T2 ");
        stb.append(" WHERE ");
        stb.append("     URIAGE_NENTSUKI >= '2008-04-01' ");
        stb.append("     AND T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T2.SHIGANSHA_NO IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.URIAGE_NENTSUKI, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE ");
        stb.append(" ORDER BY ");
        stb.append("     CAST(SHIGANSHA_RENBAN AS INT), ");
        stb.append("     SEQ ");
        stb.append(" ), NYUKIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     SEITO_GAKUHI_NONYU_JISSEKI ");
        stb.append(" GROUP BY ");
        stb.append("     SHIGANSHA_RENBAN ");
        stb.append(" HAVING ");
        stb.append("     COUNT(*) > 0 ");
        stb.append(" ), SHIBO_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUSHU_KYOTEN_CODE, ");
        stb.append("     T1.KATEI_CODE || T1.GAKKA_CODE || T1.SENKO_CODE || T1.COURSE_CODE AS COURESE ");
        stb.append(" FROM ");
        stb.append("     SHIBO T1, ");
        stb.append("     (SELECT ");
        stb.append("         TT1.SHIGANSHA_RENBAN, ");
        stb.append("         MIN(TT1.SHIBO_JUNI) AS SHIBO_JUNI ");
        stb.append("     FROM ");
        stb.append("         SHIBO TT1 ");
        stb.append("     GROUP BY ");
        stb.append("         TT1.SHIGANSHA_RENBAN ");
        stb.append("     ) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T1.SHIBO_JUNI = T2.SHIBO_JUNI ");
        stb.append(" ), COURSE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.KATEI_CODE || T1.GAKKA_CODE || T1.SENKO_CODE || T1.COURSE_CODE AS COURESE, ");
        stb.append("     CAST(CASE WHEN MONTH(T2.NENGAPPI_MIN) < 4 ");
        stb.append("               THEN YEAR(T2.NENGAPPI_MIN) - 1 ");
        stb.append("               ELSE YEAR(T2.NENGAPPI_MIN) ");
        stb.append("          END AS CHAR(4) ");
        stb.append("     ) AS NENDO ");
        stb.append(" FROM ");
        stb.append("     SEITO_SHOZOKU T1, ");
        stb.append("     (SELECT ");
        stb.append("         TT1.SHIGANSHA_RENBAN, ");
        stb.append("         MAX(TT1.SHOZOKU_KAISHI_NENGAPPI) AS NENGAPPI_MAX, ");
        stb.append("         MIN(TT1.SHOZOKU_KAISHI_NENGAPPI) AS NENGAPPI_MIN ");
        stb.append("     FROM ");
        stb.append("         SEITO_SHOZOKU TT1 ");
        stb.append("     GROUP BY ");
        stb.append("         TT1.SHIGANSHA_RENBAN ");
        stb.append("     ) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T1.SHOZOKU_KAISHI_NENGAPPI = T2.NENGAPPI_MIN ");
        stb.append(" ), SEITO_COURSE AS ( ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     COURESE, ");
        stb.append("     NENDO ");
        stb.append(" FROM ");
        stb.append("     COURSE ");
        stb.append(" UNION   ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.COURESE, ");
        stb.append("     CAST(CASE WHEN MONTH(L1.ENT_SCHEDULE_DATE) < 4 ");
        stb.append("               THEN YEAR(L1.ENT_SCHEDULE_DATE) - 1 ");
        stb.append("               ELSE YEAR(L1.ENT_SCHEDULE_DATE) ");
        stb.append("          END AS CHAR(4) ");
        stb.append("     ) AS NENDO ");
        stb.append(" FROM ");
        stb.append("     SHIBO_MAX T1 ");
        stb.append("     LEFT JOIN APPLICANT_BASE_MST L1 ON T1.SHIGANSHA_RENBAN = L1.REMARK ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS(SELECT ");
        stb.append("                     'x' ");
        stb.append("                 FROM ");
        stb.append("                     COURSE T2 ");
        stb.append("                 WHERE ");
        stb.append("                     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("                 ) ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE, ");
        stb.append("     CASE WHEN CAST(T1.PRICE AS INT) < 0 OR T1.GAKUHI_HIMOKU_CODE != '02'           THEN 1           ELSE (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) / CAST(L2.KINGAKU AS INT)      END AS AMOUNT, ");
        stb.append("     (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) AS TOTAL_CLAIM, ");
        stb.append("     CASE WHEN L2.KINGAKU IS NOT NULL ");
        stb.append("          THEN INT(L2.KINGAKU) ");
        stb.append("          ELSE (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) ");
        stb.append("     END AS KINGAKU, ");
        stb.append("     T1.PRICE, ");
        stb.append("     T1.TAX, ");
        stb.append("     T1.U_YEAR, ");
        stb.append("     T1.U_MONTH, ");
        stb.append("     T1.ORDER_CD, ");
        stb.append("     CASE WHEN L5.CNT IS NULL ");
        stb.append("          THEN 0 ");
        stb.append("          ELSE T1.PAYMENT_MONEY ");
        stb.append("     END AS PAYMENT_MONEY, ");
        stb.append("     T1.SUMMING_UP_DATE, ");
        stb.append("     L1.COURESE ");
        stb.append(" FROM ");
        stb.append("     URIAGE T1 ");
        stb.append("     LEFT JOIN SEITO_COURSE L1 ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN GAKUHI_MASTER L2 ON L2.NYUGAKU_NENDO = L1.NENDO ");
        stb.append("          AND L1.COURESE = L2.KATEI_CODE || L2.GAKKA_CODE || L2.SENKO_CODE || L2.COURSE_CODE ");
        stb.append("          AND T1.GAKUHI_HIMOKU_CODE = L2.GAKUHI_HIMOKU_CODE ");
        stb.append("     LEFT JOIN GAKUHI_MASTER_TMP L4 ON T1.GAKUHI_HIMOKU_CODE = L4.GAKUHI_HIMOKU_CODE ");
        stb.append("          AND (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) = L4.KINGAKU ");
        stb.append("     LEFT JOIN NYUKIN L5 ON T1.SHIGANSHA_RENBAN = L5.SHIGANSHA_RENBAN ");
        stb.append(" WHERE ");
        stb.append("     (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) > 0 ");
        stb.append(" ORDER BY ");
        stb.append("     CAST(T1.SHIGANSHA_RENBAN AS INT), ");
        stb.append("     CAST(T1.ORDER_CD AS INT) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     GAKUHI_HIMOKU_CODE, ");
        stb.append("     KINGAKU ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     GAKUHI_HIMOKU_CODE, ");
        stb.append("     KINGAKU ");
        stb.append(" ORDER BY ");
        stb.append("     GAKUHI_HIMOKU_CODE, ");
        stb.append("     KINGAKU ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class GakuhiMasterTmp {
        final String _gakuhiHimokuCode;
        final Integer _kingaku;
        final String _commodityCd;

        public GakuhiMasterTmp(final Map map, final int setHimokuRenBan) {
            _gakuhiHimokuCode = (String) map.get("GAKUHI_HIMOKU_CODE");
            _kingaku = new Integer(((Number) map.get("KINGAKU")).intValue());
            _commodityCd = _gakuhiHimokuCode + _commodityCdFormat.format(setHimokuRenBan);
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table GAKUHI_MASTER_TMP

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        GAKUHI_HIMOKU_CODE             SYSIBM    VARCHAR                   2     0 いいえ
        KINGAKU                        SYSIBM    INTEGER                   4     0 いいえ
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 はい
        
          3 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final GakuhiMasterTmp gakuhiMasterTmp = (GakuhiMasterTmp) it.next();
            final String insSql = getInsertSql(gakuhiMasterTmp);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final GakuhiMasterTmp gakuhiMasterTmp) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO GAKUHI_MASTER_TMP ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(gakuhiMasterTmp._gakuhiHimokuCode) + ", ");
        stb.append(" " + getInsertVal(gakuhiMasterTmp._kingaku) + ", ");
        stb.append(" " + getInsertVal(gakuhiMasterTmp._commodityCd) + " ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof
