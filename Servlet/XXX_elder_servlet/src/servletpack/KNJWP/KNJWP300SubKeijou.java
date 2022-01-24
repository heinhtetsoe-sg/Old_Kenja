// kanji=漢字
/*
 * $Id: 10268a3e1f0873e58fd6170068c6c04fcb9486a6 $
 *
 * 作成日: 2008/01/22 1:04:35 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 10268a3e1f0873e58fd6170068c6c04fcb9486a6 $
 */
public class KNJWP300SubKeijou extends KNJWP300SubAbstract {
    private static final Log log = LogFactory.getLog(KNJWP300SubKeijou.class);

    /**
     * コンストラクタ。
     * @param param
     * @throws Exception
     */
    protected KNJWP300SubKeijou(KNJWP300Param param) throws Exception {
        super(param);
    }

    // 業務処理実行
    void createSqls() throws SQLException {

        ResultSet rs = null;
        try {
            _db2.query(getSql());
            rs = _db2.getResultSet();
            while (rs.next()) {
                final String salseExeSql = updateSalseSql(rs);
                _exeList.add(salseExeSql);
                final String detailExeSql = updateDetailSql(rs);
                _exeList.add(detailExeSql);
                final String claimExeSql = updateClaimSql(rs);
                _exeList.add(claimExeSql);
            }

        } catch (final SQLException excp) {
            log.error("計上処理エラー" + excp);
            excp.printStackTrace();
            _db2.conn.rollback();
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private String updateSalseSql(final ResultSet rs) throws SQLException {
        final String yearMonthDate = _param._yearMonth.substring(0, 4) + "-" + _param._yearMonth.substring(4) + "-01";
        final String sql = ""
            + " UPDATE "
            + "     SALES_PLAN_DAT "
            + " SET "
            + "     KEEPING_MONEY = 0, "
            + "     SUMMING_UP_MONEY = " + rs.getString("TOTAL_CLAIM_MONEY") + ", "
            + "     SUMMING_UP_DATE = (VALUES(LAST_DAY(DATE('" + yearMonthDate + "')))), "
            + "     REGISTERCD = '" + _param._staffcd + "', "
            + "     UPDATED = sysdate() "
            + " WHERE "
            + "     APPLICANTNO = '" + rs.getString("APPLICANTNO") + "' "
            + "     AND PLAN_YEAR = '" + rs.getString("PLAN_YEAR") + "' "
            + "     AND PLAN_MONTH = '" + rs.getString("PLAN_MONTH") + "' "
            + "     AND SLIP_NO = '" + rs.getString("SLIP_NO") + "' "
            + "     AND SEQ = '" + rs.getString("SEQ") + "' ";

        return sql;
    }

    private String updateDetailSql(final ResultSet rs) throws SQLException {
        final String yearMonthDate = _param._yearMonth.substring(0, 4) + "-" + _param._yearMonth.substring(4) + "-01";
        final String keepingDate = rs.getString("KEEPING_DATE") == null ? "CAST(NULL AS DATE)" : "'" + rs.getString("KEEPING_DATE") + "'";
        final String sql = ""
            + " UPDATE "
            + "     CLAIM_DETAILS_DAT "
            + " SET "
            + "     PAYMENT_MONEY = VALUE(PAYMENT_MONEY, 0) + " + rs.getString("KEEPING_MONEY") + ", "
            + "     PAYMENT_DATE = " + keepingDate + ", "
            + "     SUMMING_UP_MONEY = VALUE(SUMMING_UP_MONEY, 0) + " + rs.getString("TOTAL_CLAIM_MONEY") + ", "
            + "     SUMMING_UP_DATE = (VALUES(LAST_DAY(DATE('" + yearMonthDate + "')))), "
            + "     REGISTERCD = '" + _param._staffcd + "', "
            + "     UPDATED = sysdate() "
            + " WHERE "
            + "     SLIP_NO = '" + rs.getString("SLIP_NO") + "' "
            + "     AND SEQ = '" + rs.getString("SEQ") + "' "
            + "     AND APPLICANTNO = '" + rs.getString("APPLICANTNO") + "' ";

        return sql;
    }

    private String updateClaimSql(final ResultSet rs) throws SQLException {
        final String keepingDate = rs.getString("KEEPING_DATE") == null ? "CAST(NULL AS DATE)" : "'" + rs.getString("KEEPING_DATE") + "'";
        final String yearMonthDate = _param._yearMonth.substring(0, 4) + "-" + _param._yearMonth.substring(4) + "-01";
        final String sql = ""
            + " UPDATE "
            + "     CLAIM_DAT "
            + " SET "
            + "     PAYMENT_MONEY = VALUE(PAYMENT_MONEY, 0) + " + rs.getString("KEEPING_MONEY") + ", "
            + "     PAYMENT_DATE = " + keepingDate + ", "
            + "     SUMMING_UP_MONEY = VALUE(SUMMING_UP_MONEY, 0) + " + rs.getString("TOTAL_CLAIM_MONEY") + ", "
            + "     SUMMING_UP_DATE = (VALUES(LAST_DAY(DATE('" + yearMonthDate + "')))) "
            + " WHERE "
            + "     SLIP_NO = '" + rs.getString("SLIP_NO") + "' "
            + "     AND APPLICANTNO = '" + rs.getString("APPLICANTNO") + "' ";

            return sql;
    }

    /**
     * {@inheritDoc}
     */
    protected String getSql() {
        final String sql = ""
            + " SELECT "
            + "     YEAR, "
            + "     APPLICANTNO, "
            + "     PLAN_YEAR, "
            + "     PLAN_MONTH, "
            + "     SLIP_NO, "
            + "     SEQ, "
            + "     COMMODITY_CD, "
            + "     VALUE(TOTAL_CLAIM_MONEY, 0) AS TOTAL_CLAIM_MONEY, "
            + "     VALUE(KEEPING_MONEY, 0) AS KEEPING_MONEY, "
            + "     KEEPING_DATE, "
            + "     VALUE(SUMMING_UP_MONEY, 0) AS SUMMING_UP_MONEY, "
            + "     SUMMING_UP_DATE "
            + " FROM "
            + "     SALES_PLAN_DAT "
            + " WHERE "
            + "     PLAN_YEAR = '" + _param._yearMonth.substring(0, 4) + "' "
            + "     AND PLAN_MONTH = '" + _param._yearMonth.substring(4) + "' "
            + "     AND SUMMING_UP_DATE IS NULL ";

        return sql;
    }

    /**
     * {@inheritDoc}
     */
    protected String deleteSql(ResultSet rs) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected String insertSql(ResultSet rs) throws SQLException {
        return null;
    }

}
 // KNJWP300SubKeijou

// eof
