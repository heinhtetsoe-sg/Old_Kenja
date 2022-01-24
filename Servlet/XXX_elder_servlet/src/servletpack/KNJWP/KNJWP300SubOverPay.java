// kanji=漢字
/*
 * $Id: 6fe631a8f44f40d207b0027aee86d0721c64aac4 $
 *
 * 作成日: 2008/01/16 17:07:35 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 6fe631a8f44f40d207b0027aee86d0721c64aac4 $
 */
public class KNJWP300SubOverPay extends KNJWP300SubAbstract {

    private static final Log log = LogFactory.getLog(KNJWP300SubOverPay.class);

    /**
     * コンストラクタ。
     * @throws Exception
     */
    protected KNJWP300SubOverPay(final KNJWP300Param param) throws Exception {
        super(param);
    }

    protected String getSql() {
        final String sql = ""
            + " WITH BASE_T AS ( "
            + " SELECT "
            + "     APPLICANTNO "
            + " FROM "
            + "     MONTH_OVER_PAYMENT_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._lastYearMonth + "' "
            + "     AND VALUE(MONTH_OVER_MONEY_REMAIN, 0) > 0 "
            + " UNION "
            + " SELECT "
            + "     APPLICANTNO "
            + " FROM "
            + "     OVER_PAYMENT_HIST_DAT "
            + " WHERE "
            + "     OVER_PAY_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + " ) "
            + " SELECT "
            + "     T1.APPLICANTNO, "
            + "     VALUE(M_OVER.MONTH_OVER_MONEY_REMAIN, 0) AS LAST_MONTH_OVER_REMAIN, "
            + "     VALUE(P_OVER.OVER_PAYMENT, 0) AS OVER_PAYMENT, "
            + "     VALUE(RE_PAY.RE_PAYMENT, 0) AS RE_PAYMENT, "
            + "     VALUE(M_OVER.MONTH_OVER_MONEY_REMAIN, 0) "
            + "      + VALUE(P_OVER.OVER_PAYMENT, 0) "
            + "      - VALUE(RE_PAY.RE_PAYMENT, 0) AS TOTAL "
            + " FROM "
            + "     BASE_T T1 "
            + "     LEFT JOIN MONTH_OVER_PAYMENT_DAT M_OVER ON M_OVER.APPLICANTNO = T1.APPLICANTNO "
            + "          AND M_OVER.YEAR_MONTH = '" + _param._lastYearMonth + "' "
            + "     LEFT JOIN (SELECT "
            + "                    L1.APPLICANTNO, "
            + "                    SUM(L1.OVER_PAYMENT) AS OVER_PAYMENT "
            + "                FROM "
            + "                    OVER_PAYMENT_HIST_DAT L1 "
            + "                WHERE "
            + "                    L1.APPLICANTNO IN (SELECT I1.APPLICANTNO FROM BASE_T I1) "
            + "                    AND L1.OVER_PAY_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + "                GROUP BY "
            + "                    L1.APPLICANTNO "
            + "          ) P_OVER ON P_OVER.APPLICANTNO = T1.APPLICANTNO "
            + "     LEFT JOIN (SELECT "
            + "                    L2.APPLICANTNO, "
            + "                    SUM(L2.RE_PAYMENT) AS RE_PAYMENT "
            + "                FROM "
            + "                    RE_PAYMENT_HIST_DAT L2 "
            + "                WHERE "
            + "                    L2.APPLICANTNO IN (SELECT I2.APPLICANTNO FROM BASE_T I2) "
            + "                    AND L2.RE_PAY_DATE BETWEEN '" + _param._fromData + "' AND '" + _param._toDate + "' "
            + "                GROUP BY "
            + "                    L2.APPLICANTNO "
            + "          ) RE_PAY ON RE_PAY.APPLICANTNO = T1.APPLICANTNO ";

        return sql;
    }

    protected String insertSql(final ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " INSERT INTO MONTH_OVER_PAYMENT_DAT "
            + " values ( "
            + " '" + rs.getString("APPLICANTNO") + "', "
            + " '" + _param._yearMonth + "', "
            + rs.getString("LAST_MONTH_OVER_REMAIN") + ", "
            + rs.getString("OVER_PAYMENT") + ", "
            + rs.getString("RE_PAYMENT") + ", "
            + rs.getString("TOTAL") + ", "
            + " '" + _param._staffcd + "', "
            + " current timestamp "
            + ") ";
        return rtnSql;
    }

    protected String deleteSql(final ResultSet rs) throws SQLException {
        final String rtnSql = ""
            + " DELETE FROM MONTH_OVER_PAYMENT_DAT "
            + " WHERE "
            + "     YEAR_MONTH = '" + _param._yearMonth + "' ";
        return rtnSql;
    }
}

// eof
