// kanji=漢字
/*
 * $Id: e573d975c432d985ecddfc21014abac135734aa9 $
 *
 * 作成日: 2008/01/21 22:56:25 - JST
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
 * @version $Id: e573d975c432d985ecddfc21014abac135734aa9 $
 */
public class KNJWP300SubSalesTightens extends KNJWP300SubAbstract {
    private static final Log log = LogFactory.getLog(KNJWP300SubSalesTightens.class);

    /**
     * コンストラクタ。
     * @param param
     * @throws Exception
     */
    protected KNJWP300SubSalesTightens(KNJWP300Param param) throws Exception {
        super(param);
    }

    // 業務処理実行
    void createSqls() throws SQLException {

        ResultSet rsEdate  = null;
        ResultSet rsToDate = null;
        try {
            _db2.query(getEdatePlusOne());
            rsEdate = _db2.getResultSet();
            String eDate = "";
            while (rsEdate.next()) {
                eDate = rsEdate.getString("E_TIGHTENS_DATE");
            }

            if (eDate == null || eDate.equals("")) {
                _db2.query(getEdatePlusOne(_param._toDate));
                rsToDate = _db2.getResultSet();
                while (rsToDate.next()) {
                    eDate = rsToDate.getString("E_TIGHTENS_DATE");
                }
            }
            _exeList.add(updateSql());
            if (_param._tightens.equals("2")) {
                _exeList.add(insertSql(eDate));
            }

        } catch (final SQLException excp) {
            log.error("月締めエラー" + excp);
            excp.printStackTrace();
            _db2.conn.rollback();
        } finally {
            DbUtils.closeQuietly(rsEdate);
            DbUtils.closeQuietly(rsToDate);
            _db2.commit();
        }
    }

    private String updateSql() {
        final String sql = ""
            + " UPDATE "
            + "     SALES_TIGHTENS_HIST_DAT "
            + " SET "
            + "     TEMP_TIGHTENS_FLAG = '" + _param._tightens + "', "
            + "     E_TIGHTENS_DATE = '" + _param._toDate + "', "
            + "     REGISTERCD = '" + _param._staffcd + "', "
            + "     UPDATED = sysdate() "
            + " WHERE "
            + "     SALES_YEAR_MONTH = '" + _param._yearMonth + "' ";

        return sql;
    }

    protected String insertSql(final String eDate) throws SQLException {
        final int intYear = Integer.parseInt(_param._yearMonth.substring(0, 4));
        final int intMonth = Integer.parseInt(_param._yearMonth.substring(4));

        final String nextYearMonth = getNextYearMonth(intYear, intMonth);
        String setYear = intMonth < 3 ? String.valueOf(intYear - 1) : String.valueOf(intYear);

        final String sql = ""
            + " INSERT INTO "
            + "     SALES_TIGHTENS_HIST_DAT "
            + " VALUES( "
            + "     '" + nextYearMonth + "', "
            + "     '" + setYear + "', "
            + "     '" + eDate + "', "
            + "     CAST(NULL AS DATE), "
            + "     '0', "
            + "     '" + _param._staffcd + "', "
            + "     sysdate() "
            + " ) ";

        return sql;
    }

    private String getNextYearMonth(final int intYear, final int intMonth) {

        if (12 == intMonth) {
            return String.valueOf(intYear + 1) + "01";
        }
        if (9 > intMonth) {
            return String.valueOf(intYear) + "0" + String.valueOf(intMonth + 1);
        } else {
            return String.valueOf(intYear) + String.valueOf(intMonth + 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String insertSql(ResultSet rs) throws SQLException {
        return null;
    }

    private String getEdatePlusOne(String toDate) {
        final String sql = ""
            + " WITH ADD_DATE (E_TIGHTENS_DATE) AS ( "
            + "     VALUES(Add_days(DATE('" + toDate + "'), 1)) "
            + " ) "
            + " SELECT "
            + "     * "
            + " FROM "
            + "     ADD_DATE ";

        return sql;
    }

    protected String getEdatePlusOne() {
        final String sql = ""
            + " SELECT "
            + "     Add_days(DATE(E_TIGHTENS_DATE), 1) AS E_TIGHTENS_DATE "
            + " FROM "
            + "     SALES_TIGHTENS_HIST_DAT "
            + " WHERE "
            + "     SALES_YEAR_MONTH = '" + _param._yearMonth + "' ";

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
    protected String getSql() {
        return null;
    }

}
 // KNJWP300SubSalesTightens

// eof
