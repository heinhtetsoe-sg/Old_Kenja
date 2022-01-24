// kanji=漢字
/*
 * $Id: DaoUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/17 11:42:07 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

/**
 * DAOやSQL関連のユーティリティ。
 * @author tamura
 * @version $Id: DaoUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoUtils {
    /*
     * コンストラクタ。
     */
    private DaoUtils() {
    }

    private static boolean compareSQLException0(
            final SQLException exc,
            final boolean checkSqlState,
            final String sqlState,
            final boolean checkErrorCode,
            final int errorCode
    ) {
        if ((!checkSqlState) && (!checkErrorCode)) {
            return false;
        }

        for (SQLException e = exc; null != e; e = e.getNextException()) {
            boolean sql = true;
            if (checkSqlState) {
                if (!StringUtils.equals(sqlState, e.getSQLState())) {
                    sql = false;
                }
            }

            boolean err = true;
            if (checkErrorCode) {
                if (errorCode != e.getErrorCode()) {
                    err = false;
                }
            }

            if (sql && err) {
                return true;
            }
        }

        return false;
    }

    /**
     * SQL例外の「ベンダー固有の例外コード」と「SQLState」が、それぞれ指定した値に一致するか検査する。
     * チェーンされた例外があれば、再帰的に検査する。
     * @param exc SQL例外
     * @param errorCode ベンダー固有の例外コード
     * @param sqlState SQLState
     * @return 一致したら<code>true</code>
     */
    public static boolean compareSQLException(
            final SQLException exc,
            final int errorCode,
            final String sqlState
    ) {
        return compareSQLException0(exc, true, sqlState, true, errorCode);
    }

    /**
     * SQL例外のSQLStateが指定した値に一致するか検査する。
     * チェーンされた例外があれば、再帰的に検査する。
     * @param exc SQL例外
     * @param sqlState SQLState
     * @return 一致したら<code>true</code>
     */
    public static boolean compareSQLException(
            final SQLException exc,
            final String sqlState
    ) {
        return compareSQLException0(exc, true, sqlState, false, 0);
    }

    /**
     * SQL例外のベンダー固有の例外コードが指定した値に一致するか検査する。
     * チェーンされた例外があれば、再帰的に検査する。
     * @param exc SQL例外
     * @param errorCode ベンダー固有の例外コード
     * @return 一致したら<code>true</code>
     */
    public static boolean compareSQLException(
            final SQLException exc,
            final int errorCode
    ) {
        return compareSQLException0(exc, false, null, true, errorCode);
    }

    /**
     * DB接続情報を閉じる。
     * @param dbcon DB接続情報
     * @throws SQLException SQL例外
     */
    public static void close(final DbConnection dbcon) throws SQLException {
        if (null != dbcon) {
            dbcon.close();
        }
    }

    /**
     * DB接続情報を静かに閉じる。
     * @param dbcon DB接続情報
     */
    public static void closeQuietly(final DbConnection dbcon) {
        if (null != dbcon) {
            dbcon.closeQuietly();
        }
    }
} // DaoUtils

// eof
