// kanji=漢字
/*
 * $Id: KnjMonthNoSalesDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MONTH_NO_SALES_DATを作る。
 * @author takaesu
 * @version $Id: KnjMonthNoSalesDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjMonthNoSalesDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjMonthNoSalesDat.class);

    public KnjMonthNoSalesDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "月次生徒別未収金データ"; }

    void migrate() throws SQLException {

        final String exeMonth = "SELECT MAX(SALES_YEAR_MONTH) AS YEAR_MONTH FROM SALES_TIGHTENS_HIST_DAT WHERE TEMP_TIGHTENS_FLAG = '2'";

        ResultSet rs = null;
        String planYearMonth = "";
        try {
            _db2.query(exeMonth);
            rs = _db2.getResultSet();
            while (rs.next()) {
                planYearMonth = rs.getString("YEAR_MONTH").substring(0, 4) + "-" + rs.getString("YEAR_MONTH").substring(4) + "-01";
            }
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(rs);
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SALES AS( ");
        stb.append(" SELECT ");
        stb.append("     APPLICANTNO, ");
        stb.append("     PLAN_YEAR, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     SUM(CASE WHEN VALUE(TOTAL_CLAIM_MONEY, 0) > 0 ");
        stb.append("              THEN VALUE(TOTAL_CLAIM_MONEY, 0) ");
        stb.append("              ELSE VALUE(SUMMING_UP_MONEY, 0) ");
        stb.append("         END ");
        stb.append("     ) AS SALSE_MONEY ");
        stb.append(" FROM ");
        stb.append("     SALES_PLAN_DAT ");
        stb.append(" GROUP BY ");
        stb.append("     APPLICANTNO, ");
        stb.append("     PLAN_YEAR, ");
        stb.append("     PLAN_MONTH ");
        stb.append(" ), MISHU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     L1.APPLICANTNO, ");
        stb.append("     SUBSTR(DIGITS(SMALLINT(YEAR (DATE(T1.URIAGE_NENTSUKI)))),2,4) AS YEAR, ");
        stb.append("     SUBSTR(DIGITS(SMALLINT(MONTH(DATE(T1.URIAGE_NENTSUKI)))),4,2) AS MONTH, ");
        stb.append("     SUM(INT(T1.ZENGETSU_MATSU_MISHU_KINGAKU)) AS LAST_MONTH_NO_SALES_MONEY, ");
        stb.append("     SUM(INT(T1.NYUKIN_HENKIN_KINGAKU)) AS MONTH_PAYMENT_MONEY, ");
        stb.append("     SUM(INT(T1.TAI_MISHU_NYUKIN_KINGAKU)) AS MONTH_APPROPRIATED_MONEY_DISP, ");
        stb.append("     SUM(INT(T1.TOUGETSU_MISHU_KINGAKU)) AS MONTH_NO_SALES_MONEY, ");
        stb.append("     SUM(INT(T1.TOUGETSU_MATSU_MISHU_KINGAKU)) AS TOTAL_NO_SALES_MONEY ");
        stb.append(" FROM ");
        stb.append("     SEITO_MISHU T1 ");
        stb.append("     LEFT JOIN APPLICANT_BASE_MST L1 ON T1.SHIGANSHA_RENBAN = L1.REMARK ");
        stb.append(" WHERE ");
        stb.append("     T1.URIAGE_NENTSUKI = '" + planYearMonth + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     L1.APPLICANTNO, ");
        stb.append("     SUBSTR(DIGITS(SMALLINT(YEAR (DATE(T1.URIAGE_NENTSUKI)))),2,4), ");
        stb.append("     SUBSTR(DIGITS(SMALLINT(MONTH(DATE(T1.URIAGE_NENTSUKI)))),4,2) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     VALUE(SALES.SALSE_MONEY, 0) AS MONTH_SALES_MONEY, ");
        stb.append("     MISHU.* ");
        stb.append(" FROM ");
        stb.append("     MISHU ");
        stb.append("     LEFT JOIN SALES ON MISHU.APPLICANTNO = SALES.APPLICANTNO ");
        stb.append("          AND MISHU.YEAR = SALES.PLAN_YEAR ");
        stb.append("          AND MISHU.MONTH = SALES.PLAN_MONTH ");
        stb.append(" WHERE ");
        stb.append("     MISHU.APPLICANTNO IS NOT NULL ");

        log.debug("sql=" + stb.toString());

        final List result = _runner.mapListQuery(stb.toString());
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "MONTH_NO_SALES_DAT", this);
    }

   
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        // 確認のために~Dispには逆符号の値を入れておく
        int icassMonthNoSalesMoney = ((Integer) map.get("MONTH_NO_SALES_MONEY")).intValue();
        Integer monthNoSalesMoney = new Integer(-icassMonthNoSalesMoney);
        Integer monthNoSalesMoneyDisp = new Integer(icassMonthNoSalesMoney);
        int icassTotalNoSalesMoney = ((Integer) map.get("TOTAL_NO_SALES_MONEY")).intValue();
        Integer totalNoSalesMoney = new Integer(-icassTotalNoSalesMoney);
        Integer totalNoSalesMoneyDisp = new Integer(icassTotalNoSalesMoney);
        final Object[] rtn = {
                _param.getApplicantNo((String) map.get("SHIGANSHA_RENBAN")),
                (String) map.get("YEAR")+ (String) map.get("MONTH"),
                map.get("LAST_MONTH_NO_SALES_MONEY"),
                null,
                map.get("MONTH_PAYMENT_MONEY"),
                null,
                map.get("MONTH_APPROPRIATED_MONEY_DISP"),
                map.get("MONTH_SALES_MONEY"),
                null,
                monthNoSalesMoney,
                monthNoSalesMoneyDisp,
                totalNoSalesMoney,
                totalNoSalesMoneyDisp,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

