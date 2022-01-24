/*
 * $Id: 4cfea10dbae4fcccc116643ad0eb0927bc769968 $
 *
 * 作成日: 2007/06/11
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;

public class KnjDbUtils {
    
    private static Log log = LogFactory.getLog("KnjDbUtils.class");
    
    private static boolean _isDebug = false;

    public static String getString(final Map<String, String> row, String field) {
        if (null == field || null == row || row.isEmpty()) {
            return null;
        }
        if (!row.containsKey(field)) {
        	field = field.toUpperCase();
            if (!row.containsKey(field)) {
                throw new IllegalStateException("no such field : " + field + " / " + row);
            }
        } else {
            if (!row.containsKey(field)) {
                throw new IllegalStateException("no such field : " + field + " / " + row);
            }
        }
        return row.get(field);
    }

    public static Integer getInt(final Map<String, String> row, final String field, final Integer defaultNum) {
        final String s = getString(row, field);
        if (null == s || null != s && !NumberUtils.isNumber(s)) {
            if (null != s ) {
                log.warn("数値ではない : " + field + " = " + s + " in " + row);
            }
            return defaultNum;
        }
        return new Integer(Double.valueOf(s).intValue());
    }

    public static BigDecimal getBigDecimal(final Map<String, String> row, final String field, final BigDecimal defaultNum) {
        final String s = getString(row, field);
        if (null == s || null != s && !NumberUtils.isNumber(s)) {
            if (null != s ) {
                log.warn("数値ではない : " + field + " = " + s + " in " + row);
            }
            return defaultNum;
        }
        return new BigDecimal(s);
    }

    /**
     * queryしたリストの最初のレコード（Map）のカラム1列目を返す。Listが空の場合、nullを返す。
     * @param rowList queryしたリスト
     * @return queryしたリストの最初のレコード（Map）のカラム1列目。Listが空の場合、null。
     */
    public static String getOne(final List<Map<String, String>> rowList) {
        return firstRow(rowList).get("__1");
    }

    /**
     * キーカラム値とレコードのマップを得る
     * @param rowList レコードのリスト
     * @param keyColumn キーカラム
     * @return　キーカラム値とレコードのマップ
     */
    public static Map<String, Map<String, String>> getKeyMap(final List<Map<String, String>> rowList, final String keyColumn) {
        final Map<String, Map<String, String>> rtn = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> row : rowList) {
            rtn.put(getString(row, keyColumn), row);
        }
        return rtn;
    }

    /**
     * キーカラム値と値カラム値のマップを得る
     * @param rowList レコードのリスト
     * @param keyColumn キーカラム
     * @param valueColumn 値カラム
     * @return　キーカラム値と値カラム値のマップ
     */
    public static Map getColumnValMap(final List<Map<String, String>> rowList, final String keyColumn, final String valueColumn) {
        final Map<String, String> rtn = new HashMap<String, String>();
        for (final Map<String, String> row : rowList) {
            rtn.put(getString(row, keyColumn), getString(row, valueColumn));
        }
        return rtn;
    }

    /**
     * 指定カラムのデータのリストを得る
     * @param rowList レコードのリスト
     * @param column 指定カラム
     * @return　指定カラムのデータのリスト
     */
    public static List<String> getColumnDataList(final List<Map<String, String>> rowList, final String column) {
        final List<String> rtn = new ArrayList<String>();
        for (final Map<String, String> row : rowList) {
            rtn.add(getString(row, column));
        }
        return rtn;
    }
    
    private static Map<String, String> newRow() {
        return new HashMap<String, String>();
    }
    
    public static Map<String, String> resultSetToRowMap(final ResultSetMetaData meta, final ResultSet rs) throws SQLException {
        final Map<String, String> map = newRow();
        for (int i = 0; i < meta.getColumnCount(); i++) {
            final String columnName = meta.getColumnLabel(i + 1);
            final String val = rs.getString(columnName);
            map.put(columnName, val);
            map.put("__" + String.valueOf(i + 1), val);
        }
        return map;
    }
    
    /**
     * sqlを発行した結果のレコード（Map）のリストを得る
     * @param db2 DB2
     * @param ps statement
     * @param parameter パラメータ。ない場合はnull
     * @return レコードのリスト
     */
    public static List<Map<String, String>> query(final DB2UDB db2, final PreparedStatement ps, final Object[] parameter) {
        final List<Map<String, String>> rowList = new ArrayList<Map<String, String>>();
        ResultSet rs = null;
        try {
            if (null != parameter) {
                setStatementParameter(ps, parameter);
            }
            rs = ps.executeQuery();
            final ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                rowList.add(resultSetToRowMap(meta, rs));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rowList;
    }

    private static void setStatementParameter(final PreparedStatement ps, final Object[] parameter) throws SQLException {
        for (int i = 0; i < parameter.length; i++) {
            if (parameter[i] instanceof String) {
                ps.setString(i + 1, (String) parameter[i]);
            } else if (parameter[i] instanceof Date) {
                ps.setDate(i + 1, (Date) parameter[i]);
            } else if (parameter[i] instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) parameter[i]);
            } else if (parameter[i] instanceof Double) {
                ps.setDouble(i + 1, ((Double) parameter[i]).doubleValue());
            } else if (parameter[i] instanceof Float) {
                ps.setFloat(i + 1, ((Float) parameter[i]).floatValue());
            } else if (parameter[i] instanceof Integer) {
                ps.setInt(i + 1, ((Integer) parameter[i]).intValue());
            } else if (parameter[i] instanceof Long) {
                ps.setLong(i + 1, ((Long) parameter[i]).longValue());
            } else if (null == parameter[i]) {
                ps.setString(i + 1, null);
            } else {
                throw new IllegalArgumentException("unexpected argument:" + parameter[i] + " : " + (null == parameter[i] ? null : parameter[i].getClass()));
            }
        }
    }

    /**
     * sqlを発行した結果のレコード（Map）のリストを得る
     * @param db2 DB2
     * @param sql SQL
     * @param parameter パラメータ。ない場合はnull
     * @return レコードのリスト
     */
    public static List<Map<String, String>> query(final DB2UDB db2, final String sql, final Object[] parameter) {
        List<Map<String, String>> rowList = new ArrayList<Map<String, String>>();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(sql);
            rowList = query(db2, ps, parameter);
        } catch (Exception e) {
            log.error("exception! sql=" + sql, e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return rowList;
    }
    
    /**
     * sqlを発行した結果のレコード（Map）のリストを得る
     * @param db2 DB2
     * @param sql SQL
     * @return レコードのリスト
     */
    public static List<Map<String, String>> query(final DB2UDB db2, final String sql) {
        return query(db2, sql, null);
    }
    

    /**
     * statementの列名のマップを得る
     * @param db2 DB2
     * @param ps statement
     * @return 列名のマップ
     */
    public static Map<Integer, String> getColumnNameMap(final DB2UDB db2, final PreparedStatement ps) {
        final Map<Integer, String> colnameMap = new TreeMap<Integer, String>();
        try {
            final ResultSetMetaData meta = ps.getMetaData();
            for (int i = 0; i < meta.getColumnCount(); i++) {
                final String columnName = meta.getColumnName(i + 1);
                colnameMap.put(new Integer(i + 1), columnName);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            db2.commit();
        }
        return colnameMap;
    }
    
    public static Map<Integer, String> getColumnNameMap(final DB2UDB db2, final String sql) {
        Map colnameMap = new HashMap();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(sql);
            colnameMap = getColumnNameMap(db2, ps);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return colnameMap;
    }

    public static Map<String, String> firstRow(final List<Map<String, String>> rowList) {
        if (rowList.isEmpty()) {
            return newRow();
        }
        return rowList.get(0);
    }

    public static Map<String, String> lastRow(final List<Map<String, String>> rowList) {
        if (rowList.isEmpty()) {
            return newRow();
        }
        return rowList.get(rowList.size() - 1);
    }

    /**
     * @deprecated use {@link #lastRow(List rowList)} 
     */
    public static Map<String, String> LastRow(final List<Map<String, String>> rowList) {
    	return lastRow(rowList);
    }

    public static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT 1 FROM ");
        if (StringUtils.isBlank(colname)) {
            stb.append("SYSCAT.TABLES");
        } else {
            stb.append("SYSCAT.COLUMNS");
        }
        stb.append(" WHERE TABNAME = '" + tabname + "' ");
        if (!StringUtils.isBlank(colname)) {
            stb.append(" AND COLNAME = '" + colname + "' ");
        }
        boolean hasTableColumn = KnjDbUtils.query(db2, stb.toString()).size() > 0;
        if (_isDebug) {
        	log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
        }
        return hasTableColumn;
    }

    public static String getDbPrginfoProperties(final DB2UDB db2, final String programid, final String propName) {
        return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = '" + programid + "' AND NAME = '" + propName + "' "));
    }

}

// eof

