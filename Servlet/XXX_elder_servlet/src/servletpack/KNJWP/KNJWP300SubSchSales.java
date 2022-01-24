// kanji=漢字
/*
 * $Id: cc729b5c34cb2dbb09f375e14dd17cc11ea4875d $
 *
 * 作成日: 2008/01/29 16:34:52 - JST
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
 * @version $Id: cc729b5c34cb2dbb09f375e14dd17cc11ea4875d $
 */
public class KNJWP300SubSchSales extends KNJWP300SubAbstract {

    /**
     * コンストラクタ。
     * @param param
     * @throws Exception
     */
    protected KNJWP300SubSchSales(KNJWP300Param param) throws Exception {
        super(param);
        
    }

    /**
     * {@inheritDoc}
     */
    protected String getSql() {
        final String sql = ""
            + " WITH MAIN_T AS ( "
            + " SELECT "
            + "     M1.SLIP_NO, "
            + "     M1.APPLICANTNO, "
            + "     M1.COMMODITY_CD, "
            + "     SUM(VALUE(M1.PRICE, 0)) AS PRICE, "
            + "     SUM(VALUE(M1.TAX, 0)) AS TAX, "
            + "     SUM(VALUE(M1.SUMMING_UP_MONEY, 0)) AS SUMMING_UP_MONEY "
            + " FROM "
            + "     ( "
            + "     SELECT "
            + "         T1.SLIP_NO, "
            + "         T1.APPLICANTNO, "
            + "         T1.COMMODITY_CD, "
            + "         SUM(VALUE(T1.PRICE, 0)) AS PRICE, "
            + "         SUM(VALUE(T1.TAX, 0)) AS TAX, "
            + "         SUM(VALUE(T1.SUMMING_UP_MONEY, 0)) AS SUMMING_UP_MONEY "
            + "     FROM "
            + "         SALES_PLAN_DAT T1 "
            + "     WHERE "
            + "         T1.PLAN_YEAR || T1.PLAN_MONTH = '" + _param._yearMonth + "' "
            + "         AND T1.SUMMING_UP_DATE IS NOT NULL "
            + "     GROUP BY "
            + "         T1.SLIP_NO, "
            + "         T1.APPLICANTNO, "
            + "         T1.COMMODITY_CD "
            + "     UNION ALL "
            + "     SELECT "
            + "         T1.SLIP_NO, "
            + "         T1.APPLICANTNO, "
            + "         T1.COMMODITY_CD, "
            + "         SUM(VALUE(T1.PRICE, 0)) AS PRICE, "
            + "         SUM(VALUE(T1.TAX, 0)) AS TAX, "
            + "         SUM(VALUE(T1.TOTAL_CLAIM_MONEY, 0)) AS SUMMING_UP_MONEY "
            + "     FROM "
            + "         SALES_PLAN_DAT T1 "
            + "     WHERE "
            + "         T1.PLAN_YEAR || T1.PLAN_MONTH = '" + _param._yearMonth + "' "
            + "         AND T1.SUMMING_UP_DATE IS NULL "
            + "     GROUP BY "
            + "         T1.SLIP_NO, "
            + "         T1.APPLICANTNO, "
            + "         T1.COMMODITY_CD "
            + "     ) AS M1 "
            + " GROUP BY "
            + "     M1.SLIP_NO, "
            + "     M1.APPLICANTNO, "
            + "     M1.COMMODITY_CD "
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
            + "     VALUE(SUM(T1.SUMMING_UP_MONEY), 0) AS SALES_MONEY, "
            + "     VALUE(SUM(T1.PRICE), 0) AS PRICE, "
            + "     VALUE(SUM(T1.TAX), 0) AS TAX "
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
            + " DELETE FROM MONTH_SCH_SALES_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._yearMonth + "' ";
        return rtnSql;
    }

    /**
     * {@inheritDoc}
     */
    protected String insertSql(ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " INSERT INTO MONTH_SCH_SALES_DAT "
            + " values ( "
            + " '" + rs.getString("APPLICANTNO") + "', "
            + " '" + _param._yearMonth + "', "
            + " '" + rs.getString("COMMODITY_CD") + "', "
            + rs.getString("SALES_MONEY") + ", "
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
 // KNJWP300SubSchSales

// eof
