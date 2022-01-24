// kanji=漢字
/*
 * $Id: 57ea0a0088c4817f54a680a80cc7dd419069019b $
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
 * @version $Id: 57ea0a0088c4817f54a680a80cc7dd419069019b $
 */
public class KNJWP300SubNoSalse extends KNJWP300SubAbstract {
    private static final Log log = LogFactory.getLog(KNJWP300SubNoSalse.class);
    /**
     * コンストラクタ。
     * @throws Exception
     */
    protected KNJWP300SubNoSalse(final KNJWP300Param param) throws Exception {
        super(param);
    }

    protected String getSql() {
        final String setInstate = "2013".equals(_param._year) ? ", '1200593'" : "";
        final String sql = ""
            + " WITH BASE_T AS ( "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     '********' AS SLIP_NO, "
            + "     '**' AS SEQ "
            + " FROM "
            + "     MONTH_NO_SALES_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._lastYearMonth + "' "
            + "     AND VALUE(TOTAL_NO_SALES_MONEY, 0) < 0 "
            + " UNION "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     SLIP_NO, "
            + "     SEQ "
            + " FROM "
            + "     SALES_PLAN_DAT "
            + " WHERE "
            + "     YEAR = '" + _param._year + "' "
            + "     AND PLAN_YEAR || PLAN_MONTH = '" + _param._yearMonth + "' "
            + "     AND ((SUMMING_UP_DATE IS NULL "
            + "           AND VALUE(TOTAL_CLAIM_MONEY, 0) > VALUE(KEEPING_MONEY, 0)) "
            + "          OR "
            + "          (SUMMING_UP_DATE IS NOT NULL "
            + "           AND SUMMING_UP_DATE > '" + _param._toDate + "' )"
            + "         )"
            + " UNION "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     SLIP_NO, "
            + "     SEQ "
            + " FROM "
            + "     SALES_PLAN_DAT "
            + " WHERE "
            + "     YEAR = '" + _param._year + "' "
            + "     AND PLAN_YEAR || PLAN_MONTH = '" + _param._yearMonth + "' "
            + "     AND SUMMING_UP_DATE IS NULL "
            + "     AND KEEPING_DATE > '" + _param._toDate + "' "
            + " ), PAYMENT_T AS ( "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     SUM(PAYMENT_MONEY) AS PAYMENT_MONEY "
            + " FROM "
            + "     PAYMENT_MONEY_HIST_DAT "
            + " WHERE "
            + "     APPLICANTNO IN (SELECT APPLICANTNO FROM BASE_T) "
            + "     AND PAYMENT_DATE BETWEEN  '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + " GROUP BY "
            + "     APPLICANTNO "
            + " ), RE_PAYMENT_T AS ( "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     SUM(RE_PAYMENT) AS RE_PAYMENT "
            + " FROM "
            + "     RE_PAYMENT_HIST_DAT "
            + " WHERE "
            + "     APPLICANTNO IN (SELECT APPLICANTNO FROM BASE_T) "
            + "     AND RE_PAY_DATE BETWEEN  '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + " GROUP BY "
            + "     APPLICANTNO "
            + " ), SALSE_T AS ( "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     SUM(CASE WHEN VALUE(TOTAL_CLAIM_MONEY, 0) > 0 "
            + "              THEN VALUE(TOTAL_CLAIM_MONEY, 0) "
            + "              ELSE VALUE(SUMMING_UP_MONEY, 0) "
            + "         END "
            + "     ) AS SALSE_MONEY, "
            + "     SUM(CASE WHEN VALUE(TOTAL_CLAIM_MONEY, 0) > 0 "
            + "              THEN 0 "
            + "              ELSE VALUE(SUMMING_UP_MONEY, 0) "
            + "         END "
            + "     ) AS MINUS_SALSE_MONEY "
            + " FROM "
            + "     SALES_PLAN_DAT "
            + " WHERE "
            + "     YEAR = '" + _param._year + "' "
            + "     AND APPLICANTNO IN (SELECT APPLICANTNO FROM BASE_T) "
            + "     AND PLAN_YEAR || PLAN_MONTH = '" + _param._yearMonth + "' "
            + " GROUP BY "
            + "     APPLICANTNO "
            + " ), NO_SALSE_T AS ( "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     SUM(CASE WHEN VALUE(TOTAL_CLAIM_MONEY, 0) > 0 "
            + "              THEN VALUE(TOTAL_CLAIM_MONEY, 0) - "
            + "                   CASE WHEN KEEPING_DATE > '" + _param._toDate + "' "
            + "                        THEN 0 "
            + "                        ELSE VALUE(KEEPING_MONEY, 0) "
            + "                   END "
            + "              ELSE VALUE(SUMMING_UP_MONEY, 0) "
            + "         END "
            + "     ) AS NO_SALSE_MONEY "
            + " FROM "
            + "     SALES_PLAN_DAT "
            + " WHERE "
            + "     YEAR = '" + _param._year + "' "
            + "     AND APPLICANTNO || SLIP_NO || SEQ IN (SELECT APPLICANTNO || SLIP_NO || SEQ FROM BASE_T) "
            + "     AND PLAN_YEAR || PLAN_MONTH = '" + _param._yearMonth + "' "
            + " GROUP BY "
            + "     APPLICANTNO "
            + " ), MAIN_T AS ( "
            + " SELECT "
            + "     T1.APPLICANTNO, "
            + "     VALUE(M_NO.TOTAL_NO_SALES_MONEY_DISP, 0) AS LAST_MONTH_NO_SALES_MONEY, "
            + "     VALUE(PAY.PAYMENT_MONEY, 0) - VALUE(RE_PAY.RE_PAYMENT, 0) AS MONTH_PAYMENT_MONEY, "
            + "     CASE WHEN VALUE(M_NO.TOTAL_NO_SALES_MONEY_DISP, 0) > VALUE(PAY.PAYMENT_MONEY, 0) - VALUE(RE_PAY.RE_PAYMENT, 0) "
            + "          THEN VALUE(PAY.PAYMENT_MONEY, 0) - VALUE(RE_PAY.RE_PAYMENT, 0) "
            + "          ELSE VALUE(M_NO.TOTAL_NO_SALES_MONEY_DISP, 0) "
            + "     END AS MONTH_APPROPRIATED_MONEY_DISP, "
            + "     VALUE(SALSE.SALSE_MONEY, 0) AS MONTH_SALES_MONEY, "
            + "     VALUE(SALSE.MINUS_SALSE_MONEY, 0) AS MONTH_MINUS_SALES_MONEY, "
            + "     0 - VALUE(NOSALSE.NO_SALSE_MONEY, 0) AS MONTH_NO_SALES_MONEY, "
            + "     CASE WHEN (VALUE(PAY.PAYMENT_MONEY, 0) - VALUE(RE_PAY.RE_PAYMENT, 0)) - VALUE(M_NO.TOTAL_NO_SALES_MONEY_DISP, 0) < 0 "
            + "          THEN (VALUE(PAY.PAYMENT_MONEY, 0) - VALUE(RE_PAY.RE_PAYMENT, 0)) - VALUE(M_NO.TOTAL_NO_SALES_MONEY_DISP, 0) + (0- VALUE(NOSALSE.NO_SALSE_MONEY, 0)) "
            + "          ELSE 0 - VALUE(NOSALSE.NO_SALSE_MONEY, 0) "
            + "     END AS TOTAL_NO_SALES_MONEY "
            + " FROM "
            + "     (SELECT DISTINCT APPLICANTNO FROM BASE_T) T1 "
            + "     LEFT JOIN MONTH_NO_SALES_DAT M_NO ON M_NO.APPLICANTNO = T1.APPLICANTNO "
            + "          AND M_NO.YEAR_MONTH = '" + _param._lastYearMonth + "' "
            + "     LEFT JOIN PAYMENT_T PAY ON PAY.APPLICANTNO = T1.APPLICANTNO "
            + "     LEFT JOIN RE_PAYMENT_T RE_PAY ON RE_PAY.APPLICANTNO = T1.APPLICANTNO "
            + "     LEFT JOIN SALSE_T SALSE ON SALSE.APPLICANTNO = T1.APPLICANTNO "
            + "     LEFT JOIN NO_SALSE_T NOSALSE ON NOSALSE.APPLICANTNO = T1.APPLICANTNO "
            + " ) "
            + " SELECT "
            + "     APPLICANTNO, "
            + "     LAST_MONTH_NO_SALES_MONEY, "
            + "     MONTH_PAYMENT_MONEY, "
            + "     MONTH_APPROPRIATED_MONEY_DISP, "
            + "     MONTH_SALES_MONEY, "
            + "     CASE WHEN APPLICANTNO = '' "
            + "          THEN 0 "
            + "          ELSE MONTH_NO_SALES_MONEY "
            + "     END AS MONTH_NO_SALES_MONEY, "
            + "     CASE WHEN (APPLICANTNO = '1200445' AND '" + _param._yearMonth + "' = '201205') OR (APPLICANTNO = '1200952' AND '" + _param._yearMonth + "' = '201303')  "
            + "          THEN TOTAL_NO_SALES_MONEY + abs(MONTH_MINUS_SALES_MONEY) "
            + "          ELSE CASE WHEN MONTH_SALES_MONEY >= 0 "
            + "                    THEN TOTAL_NO_SALES_MONEY + abs(MONTH_MINUS_SALES_MONEY) "
            + "                    ELSE CASE WHEN TOTAL_NO_SALES_MONEY < 0 "
            + "                              THEN TOTAL_NO_SALES_MONEY + abs(MONTH_SALES_MONEY) + abs(MONTH_MINUS_SALES_MONEY) "
            + "                              ELSE TOTAL_NO_SALES_MONEY + MONTH_SALES_MONEY + abs(MONTH_MINUS_SALES_MONEY) "
            + "                         END "
            + "               END "
            + "     END AS TOTAL_NO_SALES_MONEY "
            + " FROM "
            + "     MAIN_T "
            + " WHERE "
            + "     APPLICANTNO NOT IN ('0900900', '0900019', '1100976', '1101061', '1101626', '1000228' " + setInstate + ") ";
log.debug(sql);
            return sql;
    }

    protected String insertSql(final ResultSet rs) throws SQLException {
        final int mNoSalse = rs.getInt("MONTH_NO_SALES_MONEY");
        final String mNoSalseDisp = mNoSalse > 0 ? "0" : String.valueOf(mNoSalse * -1);
        final int tNoSalse = rs.getInt("TOTAL_NO_SALES_MONEY");
        final String tNoSalseDisp = tNoSalse > 0 ? "0" : String.valueOf(tNoSalse * -1);

        final String rtnSql = ""
            + " INSERT INTO MONTH_NO_SALES_DAT "
            + " values ( "
            + " '" + rs.getString("APPLICANTNO") + "', "
            + " '" + _param._yearMonth + "', "
            + rs.getString("LAST_MONTH_NO_SALES_MONEY") + ", "
            + " CAST(NULL AS integer), "
            + rs.getString("MONTH_PAYMENT_MONEY") + ", "
            + " CAST(NULL AS integer), "
            + rs.getString("MONTH_APPROPRIATED_MONEY_DISP") + ", "
            + rs.getString("MONTH_SALES_MONEY") + ", "
            + " CAST(NULL AS integer), "
            + mNoSalse + ", "
            + mNoSalseDisp  + ", "
            + tNoSalse + ", "
            + tNoSalseDisp  + ", "
            + " '" + _param._staffcd + "', "
            + " current timestamp "
            + ") ";

        return rtnSql;
    }

    protected String deleteSql(final ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " DELETE FROM MONTH_NO_SALES_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._yearMonth + "' ";
        return rtnSql;
    }

}

// eof
