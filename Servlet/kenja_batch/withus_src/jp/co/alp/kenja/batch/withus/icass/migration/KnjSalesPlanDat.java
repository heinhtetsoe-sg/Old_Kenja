// kanji=漢字
/*
 * $Id: KnjSalesPlanDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
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
 * SALES_PLAN_DATを作る。
 * @author takaesu
 * @version $Id: KnjSalesPlanDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSalesPlanDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSalesPlanDat.class);

    final public static DecimalFormat _monthFormat = new DecimalFormat("00");
    final public static DecimalFormat _slipFormat = new DecimalFormat("000000");

    public KnjSalesPlanDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "売上計画データ"; }

    void migrate() throws SQLException {
        int oneRoopCnt = 1000;
        for (int i = 0; i < 12000; i += oneRoopCnt) {
            final int sren = i + 1;
            final int eren = i + oneRoopCnt;
            final List list = loadIcass(sren, eren);
            log.debug(sren + "～" + eren + " SEITO_URIAGE データ件数=" + list.size());

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("更新処理中にエラー! rollback した。");
                throw e;
            }
        }
    }

    private List loadIcass(final int sren, final int eren) throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql(sren, eren);
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        String befApplicantNo = "";
        int maeukeKin = 0;
        String maeukeDate = null;
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String shiganshaRenBan = (String) map.get("SHIGANSHA_RENBAN");
            final String applicantNo = shiganshaRenBan;

            if (!befApplicantNo.equals(applicantNo)) {
                maeukeKin = 0;
                maeukeDate = null;
                final String maeukeSql = getMaeukeSql(shiganshaRenBan);
                _db2.query(maeukeSql);
                ResultSet rs = null;
                rs = _db2.getResultSet();
                if (rs.next()) {
                    maeukeKin = rs.getInt("MAEUKEKIN_KINGAKU");
                    maeukeDate = rs.getString("URIAGE_NENTSUKI");
                }
            }

            befApplicantNo = applicantNo;

            final int setSumming = ((Number) map.get("PAYMENT_MONEY")).intValue();
            final int totalClaim = ((Number) map.get("TOTAL_CLAIM")).intValue();

            int setMaeuke = 0;
            if (setSumming <= 0) {
                if (totalClaim <= maeukeKin) {
                    setMaeuke = totalClaim;
                } else if (maeukeKin > 0) {
                    setMaeuke = maeukeKin;
                }
                maeukeKin = maeukeKin - totalClaim;
            }

            final SalesPlanDat salesPlanDat = new SalesPlanDat(map,
                    totalClaim,
                    setMaeuke,
                    maeukeDate,
                    setSumming);
            rtn.add(salesPlanDat);
        }
        return rtn;
    }

    private String getSql(final int sren, final int eren) {
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
        stb.append("     INT(T1.SHIGANSHA_RENBAN) between " + sren + " AND " + eren + " ");
        stb.append("     AND URIAGE_NENTSUKI >= '2008-04-01' ");
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
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.U_YEAR, ");
        stb.append("     T1.U_MONTH, ");
        stb.append("     T1.SEQ, ");
        stb.append("     CASE WHEN L4.COMMODITY_CD IS NOT NULL ");
        stb.append("          THEN L4.COMMODITY_CD ");
        stb.append("          ELSE T1.GAKUHI_HIMOKU_CODE ");
        stb.append("     END AS COMMODITY_CD, ");
        stb.append("     (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) AS TOTAL_CLAIM, ");
        stb.append("     CASE WHEN L2.KINGAKU IS NOT NULL ");
        stb.append("          THEN INT(L2.KINGAKU) ");
        stb.append("          ELSE (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) ");
        stb.append("     END AS KINGAKU, ");
        stb.append("     T1.PRICE, ");
        stb.append("     T1.TAX, ");
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
        stb.append("          AND CASE WHEN L2.KINGAKU IS NOT NULL ");
        stb.append("                   THEN INT(L2.KINGAKU) ");
        stb.append("                   ELSE (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) ");
        stb.append("              END = L4.KINGAKU ");
        stb.append("     LEFT JOIN NYUKIN L5 ON T1.SHIGANSHA_RENBAN = L5.SHIGANSHA_RENBAN ");
        stb.append(" WHERE ");
        stb.append("     (CAST(T1.PRICE AS INT) + CAST(T1.TAX AS INT)) > 0 ");
        stb.append(" ORDER BY ");
        stb.append("     CAST(T1.SHIGANSHA_RENBAN AS INT), ");
        stb.append("     CAST(T1.ORDER_CD AS INT) ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private String getMaeukeSql(final String renban) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TIGHTENS AS ( ");
        stb.append(" SELECT ");
        stb.append("     S_TIGHTENS_DATE ");
        stb.append(" FROM ");
        stb.append("     SALES_TIGHTENS_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     TEMP_TIGHTENS_FLAG = '0' ");
        stb.append(" ), MISHU AS ( ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN ");
        stb.append(" FROM ");
        stb.append("     SEITO_MISHU T1 ");
        stb.append(" WHERE ");
        stb.append("     DATE(URIAGE_NENTSUKI) IN (SELECT S_TIGHTENS_DATE FROM TIGHTENS) ");
        stb.append("     AND INT(ZENGETSU_MATSU_MISHU_KINGAKU) > 0 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     SEITO_MAEUKEKIN T1, ");
        stb.append("     (SELECT ");
        stb.append("          TT1.SHIGANSHA_RENBAN, ");
        stb.append("          MAX(TT1.URIAGE_NENTSUKI) AS URIAGE_NENTSUKI ");
        stb.append("      FROM ");
        stb.append("          SEITO_MAEUKEKIN TT1 ");
        stb.append("      WHERE ");
        stb.append("          TT1.SHIGANSHA_RENBAN = '" + renban + "' ");
        stb.append("      GROUP BY ");
        stb.append("          TT1.SHIGANSHA_RENBAN ");
        stb.append("     ) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T1.URIAGE_NENTSUKI = T2.URIAGE_NENTSUKI ");
        stb.append("     AND NOT EXISTS( ");
        stb.append("                SELECT ");
        stb.append("                    'x' ");
        stb.append("                FROM ");
        stb.append("                    MISHU T3 ");
        stb.append("                WHERE ");
        stb.append("                    T1.SHIGANSHA_RENBAN = T3.SHIGANSHA_RENBAN ");
        stb.append("             ) ");

        return stb.toString();
    }

    private class SalesPlanDat {
        final String _year;
        final String _applicantNo;
        final String _planYear;
        final String _planMonth;
        final String _slipNo;
        final String _seq;
        final String _commodityCd;
        final Integer _totalClaimMoney;
        final Integer _price;
        final Integer _tax;
        final Integer _keepingMoney;
        final String _keepingDate;
        final Integer _summingUpMoney;
        final String _summingUpDate;

        public SalesPlanDat(
                final Map map,
                final int money,
                final int keepMoney,
                final String keepDate,
                final int setSumming
        ) {
            _year = "2008";
            final String shiganshaRenBan = (String) map.get("SHIGANSHA_RENBAN");
            _applicantNo = _param.getApplicantNo(shiganshaRenBan);
            final int planYear = ((Number) map.get("U_YEAR")).intValue();
            _planYear = String.valueOf(planYear);
            final int planMonth = ((Number) map.get("U_MONTH")).intValue();
            _planMonth = _monthFormat.format(planMonth);
            _slipNo = "08" + _slipFormat.format(Integer.parseInt(shiganshaRenBan));
            _seq = (String) map.get("SEQ");
            _commodityCd = (String) map.get("COMMODITY_CD");
            _totalClaimMoney = new Integer(money);
            final int tax = ((Number) map.get("TAX")).intValue();
            final int price = ((Number) map.get("PRICE")).intValue();
            _price = new Integer(price);
            _tax = new Integer(tax);
            _keepingMoney = keepMoney == 0 ? null : new Integer(keepMoney);
            _keepingDate = keepMoney == 0 ? null : keepDate;
            final String summingDate = (String) map.get("SUMMING_UP_DATE");
            _summingUpMoney = null == summingDate ? null : new Integer(setSumming);
            _summingUpDate = null == summingDate ? null : (summingDate).toString();
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _applicantNo + ":" + _planYear + "/" + _planMonth + " slip:" + _slipNo + " price:" + _price + " keep:" + _keepingMoney + " sum:" + _summingUpMoney;
        }
    }

    /*
     * [db2inst1@withus script]$ db2 describe table SALES_PLAN_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        PLAN_YEAR                      SYSIBM    VARCHAR                   4     0 いいえ
        PLAN_MONTH                     SYSIBM    VARCHAR                   2     0 いいえ
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 いいえ
        SEQ                            SYSIBM    VARCHAR                   2     0 いいえ
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 いいえ
        TOTAL_CLAIM_MONEY              SYSIBM    INTEGER                   4     0 はい
        PRICE                          SYSIBM    INTEGER                   4     0 はい
        TAX                            SYSIBM    INTEGER                   4     0 はい
        KEEPING_MONEY                  SYSIBM    INTEGER                   4     0 はい
        KEEPING_DATE                   SYSIBM    DATE                      4     0 はい
        SUMMING_UP_MONEY               SYSIBM    INTEGER                   4     0 はい
        SUMMING_UP_DATE                SYSIBM    DATE                      4     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          16 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SalesPlanDat salesPlanDat = (SalesPlanDat) it.next();
            final String insSql = getInsertSql(salesPlanDat);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.debug(insSql);
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final SalesPlanDat salesPlanDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SALES_PLAN_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(salesPlanDat._year) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._applicantNo) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._planYear) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._planMonth) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._slipNo) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._seq) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._commodityCd) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._totalClaimMoney) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._price) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._tax) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._keepingMoney) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._keepingDate) + ", ");
        stb.append(" " + getInsertVal(null == salesPlanDat._summingUpMoney || salesPlanDat._summingUpMoney.equals("0") ? null : salesPlanDat._summingUpMoney) + ", ");
        stb.append(" " + getInsertVal(salesPlanDat._summingUpDate) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

