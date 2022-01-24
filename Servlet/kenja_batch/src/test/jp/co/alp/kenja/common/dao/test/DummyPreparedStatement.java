// kanji=漢字
/*
 * $Id: DummyPreparedStatement.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/12/26 18:17:38 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.test;

import java.sql.SQLException;

public class DummyPreparedStatement extends NullPreparedStatement {
    private final StringBuffer _sb = new StringBuffer();

    public DummyPreparedStatement() {
    }

    public String toString() {
        return _sb.toString();
    }

    public void clearParameters() throws SQLException {
        _sb.delete(0, _sb.length());
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        if (0 < _sb.length()) {
            _sb.append(", ");
        }
        _sb.append("setObject(){");
        _sb.append("pos=").append(parameterIndex).append(',');
        _sb.append("obj=").append(x).append(',');
        _sb.append("type=").append(targetSqlType).append(',');
        _sb.append("scale=").append(scale).append('}');
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        if (0 < _sb.length()) {
            _sb.append(", ");
        }
        _sb.append("setObject(){");
        _sb.append("pos=").append(parameterIndex).append(',');
        _sb.append("obj=").append(x).append(',');
        _sb.append("type=").append(targetSqlType).append('}');
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        if (0 < _sb.length()) {
            _sb.append(", ");
        }
        _sb.append("setObject(){");
        _sb.append("pos=").append(parameterIndex).append(',');
        _sb.append("obj=").append(x).append('}');
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        if (0 < _sb.length()) {
            _sb.append(", ");
        }
        _sb.append("setString(){");
        _sb.append("pos=").append(parameterIndex).append(',');
        _sb.append("obj=").append(x).append('}');
    }
} // DummyPreparedStatement

// eof
