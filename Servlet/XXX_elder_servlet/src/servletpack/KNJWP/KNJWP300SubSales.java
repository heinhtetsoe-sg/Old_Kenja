// kanji=漢字
/*
 * $Id: 8381a8c7f0fea76c46b7b903bd8c8f6482e87339 $
 *
 * 作成日: 2008/01/26 17:45:51 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.sql.ResultSet;
import java.sql.SQLException;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 8381a8c7f0fea76c46b7b903bd8c8f6482e87339 $
 */
public class KNJWP300SubSales extends KNJWP300SubAbstract {

    /**
     * コンストラクタ。
     * @param param
     * @throws Exception
     */
    protected KNJWP300SubSales(KNJWP300Param param) throws Exception {
        super(param);
        
    }

    /**
     * {@inheritDoc}
     */
    protected String getSql() {
        final String sql = ""
            + " WITH MAIN_T AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     SUM(VALUE(T1.SUMMING_UP_MONEY, 0)) AS SUMMING_UP_MONEY, "
            + "     SUM(VALUE(T1.PRICE, 0)) AS PRICE, "
            + "     SUM(VALUE(T1.TAX, 0)) AS TAX "
            + " FROM "
            + "     SALES_PLAN_DAT T1 "
            + " WHERE "
            + "     T1.PLAN_YEAR || T1.PLAN_MONTH = '" + _param._yearMonth + "' "
            + "     AND T1.SUMMING_UP_DATE IS NOT NULL "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD "
            + " UNION ALL "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     SUM(VALUE(T1.TOTAL_CLAIM_MONEY, 0)) AS SUMMING_UP_MONEY, "
            + "     SUM(VALUE(T1.PRICE, 0)) AS PRICE, "
            + "     SUM(VALUE(T1.TAX, 0)) AS TAX "
            + " FROM "
            + "     SALES_PLAN_DAT T1 "
            + " WHERE "
            + "     T1.PLAN_YEAR || T1.PLAN_MONTH = '" + _param._yearMonth + "' "
            + "     AND T1.SUMMING_UP_DATE IS NULL "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD "
            + " ), BASE_T AS ( "
            + " SELECT "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L1.STUDENT_DIV "
            + "          ELSE I1.STUDENT_DIV "
            + "     END AS STUDENT_DIV, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L2.COMMUTING_DIV "
            + "          ELSE L3.COMMUTING_DIV "
            + "     END AS COMMUTING_DIV, "
            + "     SUM(T1.SUMMING_UP_MONEY) AS SUMMING_UP_MONEY, "
            + "     SUM(T1.PRICE) AS PRICE, "
            + "     SUM(T1.TAX) AS TAX "
            + " FROM "
            + "     MAIN_T T1 "
            + "     INNER JOIN APPLICANT_BASE_MST I1 ON T1.APPLICANTNO = I1.APPLICANTNO "
            + "     LEFT JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = '" + _param._year + "' "
            + "          AND L1.SEMESTER = '1' "
            + "          AND L1.SCHREGNO = I1.SCHREGNO "
            + "     LEFT JOIN STUDENTDIV_MST L2 ON L2.STUDENT_DIV = L1.STUDENT_DIV "
            + "     LEFT JOIN STUDENTDIV_MST L3 ON L3.STUDENT_DIV = I1.STUDENT_DIV "
            + " GROUP BY "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L1.STUDENT_DIV "
            + "          ELSE I1.STUDENT_DIV "
            + "     END, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L2.COMMUTING_DIV "
            + "          ELSE L3.COMMUTING_DIV "
            + "     END "
            + " ) "
            + " SELECT "
            + "     T1.STUDENT_DIV, "
            + "     T1.COMMODITY_CD, "
            + "     T1.COMMUTING_DIV, "
            + "     SUM(T1.SUMMING_UP_MONEY) AS SALES_MONEY, "
            + "     SUM(VALUE(T1.PRICE, 0)) AS SALES_PRICE, "
            + "     SUM(VALUE(T1.TAX, 0)) AS SALES_TAX, "
            + "     COUNT(T1.STUDENT_DIV) AS SALES_CNT, "
            + "     VALUE(L1.TOTAL_SALES_MONEY, 0) + SUM(T1.SUMMING_UP_MONEY) AS TOTAL_SALES_MONEY, "
            + "     VALUE(L1.TOTAL_PRICE, 0) + SUM(VALUE(T1.PRICE, 0)) AS TOTAL_PRICE, "
            + "     VALUE(L1.TOTAL_TAX, 0) + SUM(VALUE(T1.TAX, 0)) AS TOTAL_TAX, "
            + "     VALUE(L1.TOTAL_SALES_CNT, 0) + COUNT(T1.STUDENT_DIV) AS TOTAL_SALES_CNT "
            + " FROM "
            + "     BASE_T T1 "
            + "     LEFT JOIN MONTH_SALES_DAT L1 ON L1.YEAR_MONTH = '" + _param._lastYearMonth + "' "
            + "          AND T1.STUDENT_DIV = L1.STUDENT_DIV "
            + "          AND T1.COMMODITY_CD = L1.COMMODITY_CD "
            + "          AND T1.COMMUTING_DIV = L1.COMMUTING_DIV "
            + "     LEFT JOIN COMMODITY_MST L2 ON T1.COMMODITY_CD = L2.COMMODITY_CD "
            + " GROUP BY "
            + "     T1.STUDENT_DIV, "
            + "     T1.COMMODITY_CD, "
            + "     T1.COMMUTING_DIV, "
            + "     L1.TOTAL_SALES_MONEY, "
            + "     L1.TOTAL_PRICE, "
            + "     L1.TOTAL_TAX, "
            + "     L1.TOTAL_SALES_CNT "
            + " UNION ALL "
            + " SELECT "
            + "     T1.STUDENT_DIV, "
            + "     T1.COMMODITY_CD, "
            + "     T1.COMMUTING_DIV, "
            + "     0 AS SALES_MONEY, "
            + "     0 AS SALES_PRICE, "
            + "     0 AS SALES_TAX, "
            + "     0 AS SALES_CNT, "
            + "     T1.TOTAL_SALES_MONEY, "
            + "     0 AS TOTAL_PRICE, "
            + "     0 AS TOTAL_TAX, "
            + "     T1.TOTAL_SALES_CNT "
            + " FROM "
            + "     MONTH_SALES_DAT T1 "
            + " WHERE "
            + "     T1.YEAR_MONTH = '" + _param._lastYearMonth + "' "
            + "     AND NOT EXISTS (SELECT "
            + "                         * "
            + "                     FROM "
            + "                         (SELECT "
            + "                              E1.STUDENT_DIV, "
            + "                              E1.COMMODITY_CD "
            + "                          FROM "
            + "                              BASE_T E1 "
            + "                          GROUP BY "
            + "                              E1.STUDENT_DIV, "
            + "                              E1.COMMODITY_CD "
            + "                         ) T2 "
            + "                     WHERE "
            + "                         T1.STUDENT_DIV = T2.STUDENT_DIV "
            + "                         AND T1.COMMODITY_CD = T2.COMMODITY_CD "
            + "     ) ";

        return sql;
    }

    /**
     * {@inheritDoc}
     */
    protected String deleteSql(ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " DELETE FROM MONTH_SALES_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._yearMonth + "' ";
        return rtnSql;
    }

    /**
     * {@inheritDoc}
     */
    protected String insertSql(ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " INSERT INTO MONTH_SALES_DAT "
            + " values ( "
            + " '" + _param._yearMonth + "', "
            + " '" + rs.getString("STUDENT_DIV") + "', "
            + " '" + rs.getString("COMMODITY_CD") + "', "
            + " '" + rs.getString("COMMUTING_DIV") + "', "
            + rs.getString("SALES_MONEY") + ", "
            + rs.getString("SALES_PRICE") + ", "
            + rs.getString("SALES_TAX") + ", "
            + rs.getString("SALES_CNT") + ", "
            + rs.getString("TOTAL_SALES_MONEY") + ", "
            + rs.getString("TOTAL_PRICE") + ", "
            + rs.getString("TOTAL_TAX") + ", "
            + rs.getString("TOTAL_SALES_CNT") + ", "
            + " '" + _param._staffcd + "', "
            + " current timestamp "
            + ") ";

        return rtnSql;
    }

}
 // KNJWP300SubSales

// eof
