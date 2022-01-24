// kanji=漢字
/*
 * $Id: 9099f7425a35881daf3a9ef1849440e250d414e7 $
 *
 * 作成日: 2008/01/17 11:56:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 9099f7425a35881daf3a9ef1849440e250d414e7 $
 */
public class KNJWP300SubKeepMoney extends KNJWP300SubAbstract {
    private static final Log log = LogFactory.getLog(KNJWP300SubKeepMoney.class);
    /**
     * コンストラクタ。
     * @throws Exception
     */
    protected KNJWP300SubKeepMoney(final KNJWP300Param param) throws Exception {
        super(param);
    }

    protected String getSql() {
        final String sql = ""
            + " WITH SLIP_T AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     MIN(T1.PLAN_YEAR || T1.PLAN_MONTH) AS S_YEAR_MONTH, "
            + "     MAX(T1.PLAN_YEAR || T1.PLAN_MONTH) AS E_YEAR_MONTH, "
            + "     SUM(VALUE(T1.TOTAL_CLAIM_MONEY, 0)) AS SALES_SCHEDULE_MONEY, "
            + "     MAX(L1.DIVIDING_MULTIPLICATION_DIV) AS DIVIDING_MULTIPLICATION_DIV "
            + " FROM "
            + "     SALES_PLAN_DAT T1 "
            + "     LEFT JOIN COMMODITY_MST L1 ON L1.COMMODITY_CD = T1.COMMODITY_CD "
            + " WHERE "
            + "     EXISTS (SELECT "
            + "                 T2.SLIP_NO "
            + "             FROM "
            + "                 CLAIM_PRINT_HIST_DAT T2 "
            + "             WHERE "
            + "                 T2.CLAIM_DATE <= '" + _param._toDate + "' "
            + "                 AND VALUE(T2.CLAIM_NONE_FLG, '0') = '0' "
            + "                 AND T2.SLIP_NO = T1.SLIP_NO "
            + "             GROUP BY "
            + "                 T2.SLIP_NO "
            + "            ) "
            + "     AND T1.PLAN_YEAR || T1.PLAN_MONTH > '" + _param._yearMonth + "' "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD "
            + " ), SUB_T AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     T1.SALES_SCHEDULE_MONEY, "
            + "     MAX(L1.SUMMING_UP_DATE) AS SUMMING_UP_DATE, "
            + "     SUM(VALUE(L1.SUMMING_UP_MONEY, 0)) AS SUMMING_UP_MONEY, "
            + "     T1.DIVIDING_MULTIPLICATION_DIV "
            + " FROM "
            + "     SLIP_T T1 "
            + "     LEFT JOIN SALES_PLAN_DAT L1 ON T1.SLIP_NO = L1.SLIP_NO "
            + "          AND L1.APPLICANTNO = T1.APPLICANTNO "
            + "          AND L1.COMMODITY_CD = T1.COMMODITY_CD "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     T1.SALES_SCHEDULE_MONEY, "
            + "     T1.DIVIDING_MULTIPLICATION_DIV "
            + " ), MAIN_T AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     T1.SALES_SCHEDULE_MONEY, "
            + "     T1.SUMMING_UP_DATE, "
            + "     T1.SUMMING_UP_MONEY + SUM(VALUE(L1.TOTAL_CLAIM_MONEY, 0)) AS SUMMING_UP_MONEY, "
            + "     T1.DIVIDING_MULTIPLICATION_DIV "
            + " FROM "
            + "     SUB_T T1 "
            + "     LEFT JOIN SALES_PLAN_DAT L1 ON T1.SLIP_NO = L1.SLIP_NO "
            + "          AND L1.PLAN_YEAR || L1.PLAN_MONTH = '" + _param._yearMonth + "' "
            + "          AND L1.APPLICANTNO = T1.APPLICANTNO "
            + "          AND L1.COMMODITY_CD = T1.COMMODITY_CD "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     T1.SALES_SCHEDULE_MONEY, "
            + "     T1.SUMMING_UP_DATE, "
            + "     T1.SUMMING_UP_MONEY, "
            + "     T1.DIVIDING_MULTIPLICATION_DIV "
            + " ), MAIN_SUM AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     T1.SALES_SCHEDULE_MONEY, "
            + "     SUM(VALUE(L1.PAYMENT_MONEY, 0)) - VALUE(T1.SUMMING_UP_MONEY, 0) AS KEEPING_MONEY, "
            + "     CASE WHEN MAX(T1.DIVIDING_MULTIPLICATION_DIV) = '2' "
            + "               AND MAX(T1.SUMMING_UP_DATE) IS NOT NULL "
            + "          THEN NULL "
            + "          ELSE SUM(VALUE(L1.PAYMENT_MONEY, 0)) - VALUE(T1.SUMMING_UP_MONEY, 0) - VALUE(T1.SALES_SCHEDULE_MONEY, 0) "
            + "     END AS DIFFERENCE "
            + " FROM "
            + "     MAIN_T T1 "
            + "     LEFT JOIN PAYMENT_MONEY_HIST_DETAILS_DAT L1 ON L1.APPLICANTNO = T1.APPLICANTNO "
            + "          AND L1.PAYMENT_DATE <= '" + _param._toDate + "' "
            + "          AND L1.SLIP_NO = T1.SLIP_NO "
            + "          AND L1.COMMODITY_CD = T1.COMMODITY_CD "
            + "          AND (L1.KEEPING_DIV = '1' "
            + "               OR L1.KEEPING_DIV = '2') "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     T1.SALES_SCHEDULE_MONEY, "
            + "     T1.SUMMING_UP_MONEY, "
            + "     T1.SUMMING_UP_DATE, "
            + "     T1.DIVIDING_MULTIPLICATION_DIV "
            + " ) "
            + " SELECT "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH, "
            + "     SUM(T1.SALES_SCHEDULE_MONEY) AS SALES_SCHEDULE_MONEY, "
            + "     CASE WHEN SUM(T1.SALES_SCHEDULE_MONEY) < SUM(T1.KEEPING_MONEY) "
            + "          THEN SUM(T1.SALES_SCHEDULE_MONEY) "
            + "          ELSE SUM(T1.KEEPING_MONEY) "
            + "     END AS KEEPING_MONEY, "
            + "     CASE WHEN SUM(T1.SALES_SCHEDULE_MONEY) < SUM(T1.KEEPING_MONEY) "
            + "          THEN 0 "
            + "          ELSE SUM(T1.DIFFERENCE) "
            + "     END AS DIFFERENCE "
            + " FROM "
            + "     MAIN_SUM T1 "
            + " GROUP BY "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.S_YEAR_MONTH, "
            + "     T1.E_YEAR_MONTH "
            + "";

        return sql;
    }

    protected String insertSql(final ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " INSERT INTO MONTH_KEEPING_MONEY_DAT "
            + " values ( "
            + " '" + rs.getString("APPLICANTNO") + "', "
            + " '" + _param._yearMonth + "', "
            + " '" + rs.getString("COMMODITY_CD") + "', "
            + " '" + rs.getString("S_YEAR_MONTH") + "', "
            + " '" + rs.getString("E_YEAR_MONTH") + "', "
            + rs.getString("SALES_SCHEDULE_MONEY") + ", "
            + rs.getString("KEEPING_MONEY") + ", "
            + rs.getString("DIFFERENCE") + ", "
            + " '" + _param._staffcd + "', "
            + " current timestamp "
            + ") ";
        return rtnSql;
    }

    protected String deleteSql(final ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " DELETE FROM MONTH_KEEPING_MONEY_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._yearMonth + "' ";
        return rtnSql;
    }

}

// eof
