// kanji=漢字
/*
 * $Id: SQLUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/12/26 16:06:34 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * SQLに関するユーティリティークラス。
 * @author tamura
 * @version $Id: SQLUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class SQLUtils {
    /**
     * コンストラクタ。
     */
    private SQLUtils() {
        super();
    }

    /**
     * 文字列の配列を、SQL文where節のin句で使える文字列に変換する。
     * 例:<br/>
     * <pre>
     * whereIn(*, null)                         = null
     * whereIn(*, [])                           = null
     * whereIn(false, [null])                   = "(null)"
     * whereIn(true, [null])                    = null
     * whereIn(*, ["can't"])                    = "('can''t')"
     * whereIn(*, ["abc", "don't"])             = "('abc', 'don''t')"
     * whereIn(false, ["abc", null, "xyz"])     = "('abc', null, 'xyz')"
     * whereIn(true, ["abc", null, "xyz"])      = "('abc', 'xyz')"
     * </pre>
     * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
     * @param array 文字列の配列
     * @return 変換後の文字列
     */
    public static String whereIn(final boolean skipNull, final String[] array) {
        if (null == array || 0 == array.length) {
            return null;
        }

        final StringBuffer sb = new StringBuffer();
        int n = 0;
        for (int i = 0; i < array.length; i++) {
            if (null == array[i] && skipNull) {
                continue;
            }

            if (0 == n) { sb.append("("); }
            if (0 != n) { sb.append(", "); }

            if (null == array[i]) {
                sb.append(String.valueOf(array[i])); // "null"
            } else {
                sb.append('\'');
                sb.append(StringEscapeUtils.escapeSql(array[i]));
                sb.append('\'');
            }
            //--
            n++;
        }

        if (0 == n) {
            return null;
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * 文字列の配列を、SQL文where節のin句で使える文字列に変換する。
     * 例:<br/>
     * <pre>
     * whereIn(*, *, null)                              = null
     * whereIn(*, *, [])                                = null
     * whereIn(false, "T.COL", [null])                  = "T.COL in (null)"
     * whereIn(true, 　"T.COL", [null])                 = null
     * whereIn(*, "T.COL", ["can't"])                   = "T.COL in ('can''t')"
     * whereIn(*, "T.COL", ["abc", "do'nt"])            = "T.COL in ('abc', 'do''nt')"
     * whereIn(false, "T.COL", ["abc", null, "xyz"])    = "T.COL in ('abc', null, 'xyz')"
     * whereIn(true, 　"T.COL", ["abc", null, "xyz"])   = "T.COL in ('abc', 'xyz')"
     * </pre>
     * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
     * @param column カラム名
     * @param array 文字列の配列
     * @return 変換後の文字列
     * @throws IllegalArgumentException columnがnullまたは空の場合
     */
    public static String whereIn(
            final boolean skipNull,
            final String column,
            final String[] array
    ) {
        if (StringUtils.isEmpty(column)) {
            throw new IllegalArgumentException("must not be empty 'column'");
        }
        if (null == array || 0 == array.length) {
            return null;
        }
        final String str = whereIn(skipNull, array);
        if (null == str) {
            return str;
        }

        return column + " in " + str;
    }

    /**
     * <code>PreparedStatement</code>用に、パラメータプレースホルダー(<code>?</code>)を使って、
     * SQL文where節のin句で使える文字列に変換する。
     * <pre>
     * psWhereIn(*, null)                          = null
     * psWhereIn(*, [])                            = null
     * psWhereIn(false, [null])                    = "(?)"
     * psWhereIn(true, [null])                     = null
     * psWhereIn(*, ["can't"])                     = "(?)"
     * psWhereIn(*, ["abc", "do'nt"])              = "(?, ?)"
     * psWhereIn(false, ["abc", null, "xyz"])      = "(?, ?, ?)"
     * psWhereIn(true, ["abc", null, "xyz"])       = "(?, ?)"
     * </pre>
     * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
     * @param array 配列
     * @return 文字列
     */
    public static String psWhereIn(
            final boolean skipNull,
            final Object[] array
    ) {
        if (null == array || 0 == array.length) {
            return null;
        }

        final StringBuffer sb = new StringBuffer();
        int n = 0;
        for (int i = 0; i < array.length; i++) {
            if (null == array[i] && skipNull) {
                continue;
            }

            if (0 == n) { sb.append("("); }
            if (0 != n) { sb.append(", "); }

            sb.append('?');
            //--
            n++;
        }

        if (0 == n) {
            return null;
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * <code>PreparedStatement</code>用に、パラメータプレースホルダー(<code>?</code>)を使って、
     * SQL文where節のin句で使える文字列に変換する。
     * <pre>
     * psWhereIn(*, *, null)                            = null
     * psWhereIn(*, *, [])                              = null
     * psWhereIn(false, "T.COL", [null])                = "T.COL in (?)"
     * psWhereIn(true,  "T.COL", [null])                = null
     * psWhereIn(*, "T.COL", ["can't"])                 = "T.COL in (?)"
     * psWhereIn(*, "T.COL", ["abc", "do'nt"])          = "T.COL in (?, ?)"
     * psWhereIn(false, "T.COL", ["abc", null, "xyz"])  = "T.COL in (?, ?, ?)"
     * psWhereIn(true,  "T.COL", ["abc", null, "xyz"])  = "T.COL in (?, ?)"
     * </pre>
     * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
     * @param column カラム名
     * @param array 配列
     * @return 文字列
     * @throws IllegalArgumentException columnがnullまたは空の場合
     */
    public static String psWhereIn(
            final boolean skipNull,
            final String column,
            final Object[] array
    ) {
        if (StringUtils.isEmpty(column)) {
            throw new IllegalArgumentException("must not be empty 'column'");
        }
        if (null == array || 0 == array.length) {
            return null;
        }
        final String str = psWhereIn(skipNull, array);
        if (null == str) {
            return str;
        }

        return column + " in " + str;
    }
    /**
     * <code>PreparedStatement</code>に、配列からパラメータを設定する。
     * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
     * @param ps PreparedStatement
     * @param start 開始インデックス
     * @param array 配列
     * @param type <code>java.sql.Types</code>で定義されるSQL 型。VARCHARなら<code>java.sql.Types.VARCHAR</code>
     * @return 次のインデックス
     * @throws SQLException SQL例外
     */
    public static int setPS(
            final boolean skipNull,
            final PreparedStatement ps,
            final int start,
            final int type,
            final Object[] array
    ) throws SQLException {
        if (null == array || 0 == array.length) {
            return start;
        }
        int pos = start;
        for (int i = 0; i < array.length; i++) {
            if (null == array[i] && skipNull) {
                continue;
            }
            ps.setObject(pos, array[i], type);
            pos++;
        }
        return pos;
    }

    /**
     * <code>PreparedStatement</code>に、文字列配列からVARCHARパラメータを設定する。
     * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
     * @param ps PreparedStatement
     * @param start 開始インデックス
     * @param array 文字列配列
     * @return 次のインデックス
     * @throws SQLException SQL例外
     */
    public static int setPS(
            final boolean skipNull,
            final PreparedStatement ps,
            final int start,
            final String[] array
    ) throws SQLException {
        if (null == array || 0 == array.length) {
            return start;
        }
        int pos = start;
        for (int i = 0; i < array.length; i++) {
            if (null == array[i] && skipNull) {
                continue;
            }
            ps.setString(pos, array[i]);
            pos++;
        }
        return pos;
    }
} // SQLUtils

// eof
