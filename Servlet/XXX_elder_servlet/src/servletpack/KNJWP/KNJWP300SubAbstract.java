// kanji=漢字
/*
 * $Id: 3aed8752c0699828b6a68c09a15abb5ec40a128a $
 *
 * 作成日: 2008/01/16 17:29:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 3aed8752c0699828b6a68c09a15abb5ec40a128a $
 */
public abstract class KNJWP300SubAbstract {
    protected final KNJWP300Param _param;
    protected final DB2UDB _db2;
    protected final List _exeList;

    /**
     * コンストラクタ。
     */
    protected KNJWP300SubAbstract(final KNJWP300Param param) throws Exception {
        _param = param;
        _exeList = new ArrayList();
        _db2 = _param.createDb();
        _db2.open();
    }

    // 業務処理実行
    void createSqls() throws SQLException {

        ResultSet rs = null;
        try {
            _db2.query(getSql());

            rs = _db2.getResultSet();
            _exeList.add(deleteSql(rs));
            while (rs.next()) {
                _exeList.add(insertSql(rs));
            }

        } catch (final SQLException excp) {
            excp.printStackTrace();
            _db2.conn.rollback();
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    // 対象SQL
    abstract protected String getSql();

    // 削除SQL
    abstract protected String deleteSql(final ResultSet rs) throws SQLException;

    // 更新SQL
    abstract protected String insertSql(final ResultSet rs) throws SQLException;

    protected List getSqlList() {
        return _exeList;
    }
}
 // AbstractKnjwp300Sub

// eof
