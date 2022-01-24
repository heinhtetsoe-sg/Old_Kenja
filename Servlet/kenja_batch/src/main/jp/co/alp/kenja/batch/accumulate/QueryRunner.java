// kanji=漢字
/*
 * $Id: QueryRunner.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2008/05/07 10:48:53 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Arrays;

import java.sql.SQLException;

/**
 * org.apache.commons.dbutils.QueryRunner のバグ修正版。
 * @author takaesu
 * @version $Id: QueryRunner.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class QueryRunner extends org.apache.commons.dbutils.QueryRunner {
    protected void rethrow(SQLException cause, String sql, Object[] params) throws SQLException {
        StringBuffer msg = new StringBuffer(cause.getMessage());

        msg.append(" Query: ");
        msg.append(sql);
        msg.append(" Parameters: ");

        if (params == null) {
            msg.append("[]");
        } else {
            msg.append(Arrays.asList(params));
        }

        // 以下の第2、第3パラメータを追加
        SQLException e = new SQLException(
                msg.toString(),
                cause.getSQLState(),
                cause.getErrorCode());

        e.setNextException(cause);

        throw e;
    }
} // QueryRunner

// eof
