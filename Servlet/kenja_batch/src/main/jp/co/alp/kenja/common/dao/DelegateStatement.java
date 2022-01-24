// kanji=漢字
/*
 * $Id: DelegateStatement.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/28 14:34:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * 委譲するだけのStatement。
 * @author tamura
 * @version $Id: DelegateStatement.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class DelegateStatement implements Statement {
    private final Statement _stmt;

    /**
     * コンストラクタ。
     * @param stmt Statement
     */
    public DelegateStatement(final Statement stmt) {
        super();
        _stmt = stmt;
    }

    //========================================================================
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _stmt.toString();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return _stmt.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        return _stmt.equals(obj);
    }

    //========================================================================

    /**
     * {@inheritDoc}
     */
    public void addBatch(final String sql) throws SQLException {
        _stmt.addBatch(sql);
    }

    /**
     * {@inheritDoc}
     */
    public void cancel() throws SQLException {
        _stmt.cancel();
    }

    /**
     * {@inheritDoc}
     */
    public void clearBatch() throws SQLException {
        _stmt.clearBatch();
    }

    /**
     * {@inheritDoc}
     */
    public void clearWarnings() throws SQLException {
        _stmt.clearWarnings();
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        _stmt.close();
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(final String sql) throws SQLException {
        return _stmt.execute(sql);
    }

    /**
     * {@inheritDoc}
     */
    public int[] executeBatch() throws SQLException {
        return _stmt.executeBatch();
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet executeQuery(final String sql) throws SQLException {
        return _stmt.executeQuery(sql);
    }

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(final String sql) throws SQLException {
        return _stmt.executeUpdate(sql);
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        return _stmt.getConnection();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchDirection() throws SQLException {
        return _stmt.getFetchDirection();
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() throws SQLException {
        return _stmt.getFetchSize();
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxFieldSize() throws SQLException {
        return _stmt.getMaxFieldSize();
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxRows() throws SQLException {
        return _stmt.getMaxRows();
    }

    /**
     * {@inheritDoc}
     */
    public boolean getMoreResults() throws SQLException {
        return _stmt.getMoreResults();
    }

    /**
     * {@inheritDoc}
     */
    public int getQueryTimeout() throws SQLException {
        return _stmt.getQueryTimeout();
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet getResultSet() throws SQLException {
        return _stmt.getResultSet();
    }

    /**
     * {@inheritDoc}
     */
    public int getResultSetConcurrency() throws SQLException {
        return _stmt.getResultSetConcurrency();
    }

    /**
     * {@inheritDoc}
     */
    public int getResultSetType() throws SQLException {
        return _stmt.getResultSetType();
    }

    /**
     * {@inheritDoc}
     */
    public int getUpdateCount() throws SQLException {
        return _stmt.getUpdateCount();
    }

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() throws SQLException {
        return _stmt.getWarnings();
    }

    /**
     * {@inheritDoc}
     */
    public void setCursorName(final String name) throws SQLException {
        _stmt.setCursorName(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        _stmt.setEscapeProcessing(enable);
    }

    /**
     * {@inheritDoc}
     */
    public void setFetchDirection(final int direction) throws SQLException {
        _stmt.setFetchDirection(direction);
    }

    /**
     * {@inheritDoc}
     */
    public void setFetchSize(final int rows) throws SQLException {
        _stmt.setFetchSize(rows);
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxFieldSize(final int max) throws SQLException {
        _stmt.setMaxFieldSize(max);
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxRows(final int max) throws SQLException {
        _stmt.setMaxRows(max);
    }

    /**
     * {@inheritDoc}
     */
    public void setQueryTimeout(final int seconds) throws SQLException {
        _stmt.setQueryTimeout(seconds);
    }

    public Object unwrap(Class iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    public boolean isWrapperFor(Class iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public boolean getMoreResults(int current) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public int getResultSetHoldability() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    public boolean isClosed() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public boolean isPoolable() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

} // DelegateStatement

// eof
