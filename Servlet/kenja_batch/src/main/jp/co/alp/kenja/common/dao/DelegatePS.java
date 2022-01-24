// kanji=漢字
/*
 * $Id: DelegatePS.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/24 15:03:59 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 委譲するだけのPreparedStatement。
 * @author tamura
 * @version $Id: DelegatePS.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class DelegatePS extends DelegateStatement implements PreparedStatement {
    private final PreparedStatement _ps;

    /**
     * コンストラクタ。
     * @param ps ps
     */
    protected DelegatePS(final PreparedStatement ps) {
        super(ps);
        _ps = ps;
    }

    //========================================================================
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _ps.toString();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return _ps.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        return _ps.equals(obj);
    }

    //========================================================================

    /**
     * {@inheritDoc}
     */
    public void addBatch() throws SQLException {
        _ps.addBatch();
    }

    /**
     * {@inheritDoc}
     */
    public void clearParameters() throws SQLException {
        _ps.clearParameters();
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute() throws SQLException {
        return _ps.execute();
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet executeQuery() throws SQLException {
        return _ps.executeQuery();
    }

    /**
     * {@inheritDoc}
     */
    public int executeUpdate() throws SQLException {
        return _ps.executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return _ps.getMetaData();
    }

    /**
     * {@inheritDoc}
     */
    public void setArray(final int i, final Array x) throws SQLException {
        _ps.setArray(i, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        _ps.setAsciiStream(parameterIndex, x, length);
    }

    /**
     * {@inheritDoc}
     */
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        _ps.setBigDecimal(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        _ps.setBinaryStream(parameterIndex, x, length);
    }

    /**
     * {@inheritDoc}
     */
    public void setBlob(final int i, final Blob x) throws SQLException {
        _ps.setBlob(i, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        _ps.setBoolean(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        _ps.setByte(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        _ps.setBytes(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        _ps.setCharacterStream(parameterIndex, reader, length);
    }

    /**
     * {@inheritDoc}
     */
    public void setClob(final int i, final Clob x) throws SQLException {
        _ps.setClob(i, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        _ps.setDate(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        _ps.setDate(parameterIndex, x, cal);
    }

    /**
     * {@inheritDoc}
     */
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        _ps.setDouble(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        _ps.setFloat(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        _ps.setInt(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        _ps.setLong(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        _ps.setNull(parameterIndex, sqlType);
    }

    /**
     * {@inheritDoc}
     */
    public void setNull(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        _ps.setNull(paramIndex, sqlType, typeName);
    }

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        _ps.setObject(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        _ps.setObject(parameterIndex, x, targetSqlType);
    }

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
        _ps.setObject(parameterIndex, x, targetSqlType, scale);
    }

    /**
     * {@inheritDoc}
     */
    public void setRef(final int i, final Ref x) throws SQLException {
        _ps.setRef(i, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        _ps.setShort(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setString(final int parameterIndex, final String x) throws SQLException {
        _ps.setString(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        _ps.setTime(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        _ps.setTime(parameterIndex, x, cal);
    }

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        _ps.setTimestamp(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        _ps.setTimestamp(parameterIndex, x, cal);
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        _ps.setUnicodeStream(parameterIndex, x, length);
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

    public Object unwrap(Class iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    public boolean isWrapperFor(Class iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ

    }
} // DelegatePS

// eof
