// kanji=漢字
/*
 * $Id: 69b92d38d675aa1cebbdd4b0b4974bd805d6776c $
 *
 * 作成日: 2008/01/29 15:51:14 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.sql.ResultSet;
import java.sql.SQLException;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 69b92d38d675aa1cebbdd4b0b4974bd805d6776c $
 */
public class KNJWP300SubSchPayMent extends KNJWP300SubAbstract {

    /**
     * コンストラクタ。
     * @param param
     * @throws Exception
     */
    protected KNJWP300SubSchPayMent(KNJWP300Param param) throws Exception {
        super(param);
        
    }

    /**
     * {@inheritDoc}
     */
    protected String getSql() {
        final String sql = ""
            + " WITH PAY_HIST_DETAIL AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     VALUE(L1.TAX_PERCENT, 0) AS TAX_PERCENT, "
            + "     SUM(VALUE(T1.PAYMENT_MONEY, 0)) AS PAYMENT_MONEY "
            + " FROM "
            + "     PAYMENT_MONEY_HIST_DETAILS_DAT T1 "
            + "     INNER JOIN CLAIM_DAT I1 ON T1.SLIP_NO = I1.SLIP_NO "
            + "           AND T1.APPLICANTNO = I1.APPLICANTNO "
            + "           AND VALUE(I1.CANCEL_FLG, '0') = '0' "
            + "     LEFT JOIN COMMODITY_MST L1 ON T1.COMMODITY_CD = L1.COMMODITY_CD "
            + " WHERE "
            + "     T1.PAYMENT_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     L1.TAX_PERCENT "
            + " ), MAIN_T AS ( "
            + " SELECT "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     VALUE(T1.PAYMENT_MONEY, 0) AS PAYMENT_MONEY, "
            + "     CASE WHEN T1.TAX_PERCENT = 0 "
            + "          THEN VALUE(T1.PAYMENT_MONEY, 0) "
            + "          ELSE (trunc(float(VALUE(T1.PAYMENT_MONEY, 0) / (T1.TAX_PERCENT / 100  + 1) * 10) / 10, 0)) "
            + "     END AS PRICE, "
            + "     VALUE(T1.PAYMENT_MONEY, 0) - (trunc(float(VALUE(T1.PAYMENT_MONEY, 0) / (T1.TAX_PERCENT / 100  + 1) * 10) / 10, 0)) AS TAX "
            + " FROM "
            + "     PAY_HIST_DETAIL T1 "
            + " GROUP BY "
            + "     T1.SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     T1.PAYMENT_MONEY, "
            + "     T1.TAX_PERCENT "
            + " UNION ALL "
            + " SELECT "
            + "     '*' AS SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     '99001' AS COMMODITY_CD, "
            + "     VALUE(P_OVER.OVER_PAYMENT, 0) AS PAYMENT_MONEY, "
            + "     0 AS PRICE, "
            + "     0 AS TAX "
            + " FROM "
            + "     APPLICANT_BASE_MST T1 "
            + "     LEFT JOIN (SELECT "
            + "                    L1.APPLICANTNO, "
            + "                    SUM(L1.OVER_PAYMENT) AS OVER_PAYMENT "
            + "                FROM "
            + "                    OVER_PAYMENT_HIST_DAT L1 "
            + "                WHERE "
            + "                    L1.OVER_PAY_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + "                GROUP BY "
            + "                    L1.APPLICANTNO "
            + "          ) P_OVER ON P_OVER.APPLICANTNO = T1.APPLICANTNO "
            + " WHERE "
            + "     VALUE(P_OVER.OVER_PAYMENT, 0) > 0 "
            + " UNION ALL "
            + " SELECT "
            + "     '*' AS SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     '99003' AS COMMODITY_CD, "
            + "     VALUE(P_OVER.OVER_PAYMENT, 0) * -1 AS PAYMENT_MONEY, "
            + "     0 AS PRICE, "
            + "     0 AS TAX "
            + " FROM "
            + "     APPLICANT_BASE_MST T1 "
            + "     LEFT JOIN (SELECT "
            + "                    L1.APPLICANTNO, "
            + "                    SUM(L1.OVER_PAYMENT) AS OVER_PAYMENT "
            + "                FROM "
            + "                    OVER_PAYMENT_HIST_DAT L1 "
            + "                WHERE "
            + "                    L1.OVER_PAY_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + "                    AND L1.OVER_PAYMENT_DIV = '02' "
            + "                GROUP BY "
            + "                    L1.APPLICANTNO "
            + "          ) P_OVER ON P_OVER.APPLICANTNO = T1.APPLICANTNO "
            + " WHERE "
            + "     VALUE(P_OVER.OVER_PAYMENT, 0) > 0 "
            + " UNION ALL "
            + " SELECT "
            + "     '*' AS SLIP_NO, "
            + "     T1.APPLICANTNO, "
            + "     '99002' AS COMMODITY_CD, "
            + "     VALUE(RE_PAY.RE_PAYMENT, 0) * -1 AS PAYMENT_MONEY, "
            + "     0 AS PRICE, "
            + "     0 AS TAX "
            + " FROM "
            + "     APPLICANT_BASE_MST T1 "
            + "     LEFT JOIN (SELECT "
            + "                    L2.APPLICANTNO, "
            + "                    SUM(L2.RE_PAYMENT) AS RE_PAYMENT "
            + "                FROM "
            + "                    RE_PAYMENT_HIST_DAT L2 "
            + "                WHERE "
            + "                    L2.RE_PAY_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + "                GROUP BY "
            + "                    L2.APPLICANTNO "
            + "          ) RE_PAY ON RE_PAY.APPLICANTNO = T1.APPLICANTNO "
            + " WHERE "
            + "     VALUE(RE_PAY.RE_PAYMENT, 0) > 0 "
            + " ) "
            + " SELECT "
            + "     T1.APPLICANTNO, "
            + "     T1.COMMODITY_CD, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L1.GRADE "
            + "          ELSE I1.BELONGING_DIV "
            + "     END AS BELONGING_DIV, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L1.STUDENT_DIV "
            + "          ELSE I1.STUDENT_DIV "
            + "     END AS STUDENT_DIV, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L2.COMMUTING_DIV "
            + "          ELSE L3.COMMUTING_DIV "
            + "     END AS COMMUTING_DIV, "
            + "     SUM(T1.PAYMENT_MONEY) AS PAYMENT_MONEY, "
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
            + "          THEN L1.GRADE "
            + "          ELSE I1.BELONGING_DIV "
            + "     END, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L1.STUDENT_DIV "
            + "          ELSE I1.STUDENT_DIV "
            + "     END, "
            + "     CASE WHEN L1.SCHREGNO IS NOT NULL "
            + "          THEN L2.COMMUTING_DIV "
            + "          ELSE L3.COMMUTING_DIV "
            + "     END "
            + "";

        return sql;
    }

    /**
     * {@inheritDoc}
     */
    protected String deleteSql(ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " DELETE FROM MONTH_SCH_PAYMENT_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._yearMonth + "' ";
        return rtnSql;
    }

    /**
     * {@inheritDoc}
     */
    protected String insertSql(ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " INSERT INTO MONTH_SCH_PAYMENT_DAT "
            + " values ( "
            + " '" + rs.getString("APPLICANTNO") + "', "
            + " '" + _param._yearMonth + "', "
            + " '" + rs.getString("COMMODITY_CD") + "', "
            + rs.getString("PAYMENT_MONEY") + ", "
            + rs.getString("PRICE") + ", "
            + rs.getString("TAX") + ", "
            + " '" + rs.getString("BELONGING_DIV") + "', "
            + " '" + rs.getString("STUDENT_DIV") + "', "
            + " '" + rs.getString("COMMUTING_DIV") + "', "
            + " '" + _param._staffcd + "', "
            + " current timestamp "
            + ") ";

        return rtnSql;
    }

}
 // KNJWP300SubSchPayMent

// eof
