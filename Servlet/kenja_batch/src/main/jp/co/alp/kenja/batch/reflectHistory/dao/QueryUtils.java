/*
 * $Id: QueryUtils.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.reflectHistory.FieldValue;
import jp.co.alp.kenja.batch.reflectHistory.HistoryUpdateFlgField;
import jp.co.alp.kenja.batch.reflectHistory.ReflectHistoryContext;
import jp.co.alp.kenja.batch.reflectHistory.Utils;
import jp.co.alp.kenja.common.dao.DbConnection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryUtils {

    private static Log log = LogFactory.getLog(QueryUtils.class);

    /**
     * クエリの結果を各行のマップのリストで返す
     * @param dbcon
     * @param query クエリ
     * @param parameters queryのパラメータ
     * @return クエリの結果の各行のマップのリスト
     * @throws SQLException
     */
    public static Collection<Map<String, String>> fetchAssoc(final DbConnection dbcon, final String query, final String[] parameters) throws SQLException {
        final List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dbcon.getROConnection();
            ps = con.prepareStatement(query);
            log.debug(" query = " + query + ", parameter = " + ArrayUtils.toString(parameters));
            if (null != parameters) {
                for (int i = 0; i < parameters.length; i++) {
                    ps.setString(i + 1, parameters[i]);
                }
            }
            rs = ps.executeQuery();
            final ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                list.add(toMap(meta, rs));
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            con.commit();
        }
        //log.info("クエリ件数:" + list.size());
        return list;
    }

    /**
     * 
     * @param ctx
     * @param reflectHist
     * @param col クエリの結果(Map<String, String>)のコレクション
     * @return 更新件数
     * @throws SQLException
     */
    public static int update(final DbConnection dbcon, final ReflectHistoryContext ctx, final ReflectHistoryUpdate reflectHist, final Collection<Map<String, String>> col) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        int count = 0;
        try {
            con = dbcon.getRWConnection();
            con.setAutoCommit(false);
            for (final Map<String, String> assoc : col) {
                final Collection<String> sqlList = reflectHist.getUpdateSqlList(dbcon, ctx, assoc);
                if (null == sqlList) {
                    continue;
                }
                for (final String sql : sqlList) {
                    if (null == sql) {
                        continue;
                    }
                    log.debug(" update sql = " + sql);
                    ps = con.prepareStatement(sql);
                    count += ps.executeUpdate();
                    DbUtils.closeQuietly(ps);
                }
            }
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        }
        log.info("更新件数:" + count);
        return count;
    }

    /**
     * フィールドに関連付けられた値を得る
     * @param assoc
     * @param field フィールド
     * @return フィールドに関連付けられた値
     */
    public static String getString(final Map<String, String> assoc, final String field) {
        if (assoc.isEmpty()) {
            return null;
        }
        if (assoc.containsKey(field)) {
            return (String) assoc.get(field);
        }
        throw new IllegalStateException("not field \"" + field + "\" in " + assoc.keySet());
    }

    /**
     * 履歴データの更新SQL
     * @param historyUpdateFlgFieldList 履歴データの更新フラグフィールドのリスト
     * @param assoc
     * @param tgtAssoc 更新対象テーブルのデータ (比較しない場合、null)
     * @return 履歴データの更新SQLを作成する。更新なしの場合、nullを返す。
     */
    public static String createUpdateSql(final ReflectHistoryContext ctx, final String tablename, final List<HistoryUpdateFlgField> historyUpdateFlgFieldList, final Map<String, String> assoc, final Map<String, String> tgtAssoc, final List<FieldValue> historyUpdateFlgFieldWhereList) {
        final Collection<FieldValue> updateFieldValueList = Utils.getUpdateFieldValueList(tablename, historyUpdateFlgFieldList, assoc, tgtAssoc);
        if (updateFieldValueList.isEmpty()) {
            return null;
        }
        updateFieldValueList.add(new FieldValue("REGISTERCD", ctx.getKenjaParameters().getStaffCd()));
        updateFieldValueList.add(new FieldValue("UPDATED", "current timestamp", true));
        final StringBuffer sql = new StringBuffer();
        String comma = "";
        sql.append(" update ").append(tablename);
        sql.append(" set ");
        for (final Iterator<FieldValue> it = updateFieldValueList.iterator(); it.hasNext();) {
            final FieldValue fieldValue = (FieldValue) it.next();
            sql.append(comma).append(fieldValue.getField()).append(" = ");
            appendQuotedValue(sql, fieldValue);
            comma = ", ";
        }
        if (!historyUpdateFlgFieldWhereList.isEmpty()) {
            sql.append(" where ");
            String whereand = "";
            for (final Iterator<FieldValue> it = historyUpdateFlgFieldWhereList.iterator(); it.hasNext();) {
                final FieldValue fieldValue = (FieldValue) it.next();
                sql.append(whereand).append(fieldValue.getField()).append(" = ");
                appendQuotedValue(sql, fieldValue);
                whereand = " and ";
            }
        }
        return sql.toString();
    }

    private static void appendQuotedValue(final StringBuffer stb, final FieldValue fv) {
        if (null == fv.getValue()) {
            stb.append(" null ");
        } else if (fv.valueIsNotString()) {
            stb.append(fv.getValue());
        } else {
            stb.append("'").append(fv.getValue()).append("'");
        }
    }

    /**
     * rsのカラムとその値でMapを作成し返す
     * @param meta
     * @param rs
     * @return rsのカラムとその値のMap
     * @throws SQLException
     */
    private static Map<String, String> toMap(final ResultSetMetaData meta, final ResultSet rs) throws SQLException {
        final Map<String, String> map = new HashMap<String, String>();
        for (int c = 1, max = meta.getColumnCount(); c <= max; c++) {
            final String column = meta.getColumnName(c);
            final String value = rs.getString(column);
            map.put(column, value);
        }
        return map;
    }
}
