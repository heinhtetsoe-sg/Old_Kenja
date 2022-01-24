// kanji=漢字
/*
 * $Id: AbstractDaoLoader.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 15:43:02 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.util.KenjaUtils;

/**
 * DB読み込みの抽象クラス。
 * @author tamura
 * @version $Id: AbstractDaoLoader.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class AbstractDaoLoader<T> {
    protected final String _name = ClassUtils.getShortClassName(this.getClass());

    protected Connection _conn;
    protected ControlMaster _cm;

    private final Log _log;

    /**
     * コンストラクタ。
     * @param log 出力用のlog
     */
    protected AbstractDaoLoader(final Log log) {
        super();
        _log = log;
    }

    /**
     * 問い合わせのSQL文を得る。
     * @return SQL文
     */
    public abstract String getQuerySql();

    /**
     * 問い合わせのパラメータを得る。
     * @param cm コントロール・マスタ
     * @return パラメータ
     */
    public abstract Object[] getQueryParams(final ControlMaster cm);

    /**
     * マップから、domainインスタンスを生成する。
     * @param map beanmap
     * @return domainのインスタンス。または、インスタンスを生成できない理由の文字列
     */
    public abstract Object mapToInstance(final Map<String, Object> map);

    /**
     * DBにテーブルがあるか問い合わせ
     * @param conn
     * @param tablename 指定テーブル名
     * @return DBにテーブルがあるならtrue、それ以外はfalse
     * @throws SQLException
     */
    public boolean hasTable(
            final Connection conn,
            final String tablename
    ) throws SQLException {
        final String sql = "select"
                + "    COUNT(*) as count "
                + "  from SYSCAT.TABLES "
                + "  where"
                + "    TABNAME = '" + tablename.toUpperCase() + "' " 
                + KenjaUtils.LINE_SEPA
            ;

        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new ScalarHandler("count");
        final Integer count = (Integer) qr.query(conn, sql, null, rsh);
        _log.info(" table " + tablename + " count = " + count);
        return (null == count ? 0 : count.intValue()) > 0;
    }

    /**
     * DBにテーブルのカラムがあるか問い合わせ
     * @param conn
     * @param tablename 指定テーブル名
     * @param columnname 指定カラム名
     * @return DBにテーブルのカラムがあるならtrue、それ以外はfalse
     * @throws SQLException
     */
    public boolean hasTableColumn(
            final Connection conn,
            final String tablename,
            final String columnname
    ) throws SQLException {
        final String sql = "select"
                + "    COUNT(*) as count "
                + "  from SYSCAT.COLUMNS "
                + "  where"
                + "    TABNAME = '" + tablename.toUpperCase() + "' " 
                + "    AND COLNAME = '" + columnname.toUpperCase() + "' " 
                + KenjaUtils.LINE_SEPA
            ;

        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new ScalarHandler("count");
        final Integer count = (Integer) qr.query(conn, sql, null, rsh);
        _log.info(" column " + tablename + "." + columnname + " count = " + count);
        return (null == count ? 0 : count.intValue()) > 0;
    }

    /**
     * loadの前処理。
     * 必要に応じて、サブクラスで再実装してください。
     * @throws SQLException 例外
     */
    protected void preLoad() throws SQLException {
        // 空実装です。
    }

    /*
     * loadの前処理。
     * @throws Exception 例外
     */
    private void preLoad0() throws SQLException {
        try {
            preLoad();
        } finally {
            ;
        }
    }

    /**
     * loadの後処理。
     * 必要に応じて、サブクラスで再実装してください。
     * @throws SQLException 例外
     */
    protected void postLoad() throws SQLException {
        // 空実装です。
    }

    /*
     * loadの後処理。
     * @throws Exception 例外
     */
    private void postLoad0() throws SQLException {
        try {
            postLoad();
        } finally {
            _conn = null;
            _cm = null;
        }
    }

    /**
     * beanmapのListから、domainインスタンスを生成する。
     * @param list MapのList
     * @return 正しく変換できたインスタンスの数
     */
    protected int convert(final List<Map<String, Object>> list) {
        int count = 0;
        for (final Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();) {
            final Map<String, Object> map = it.next();
            try {
                final Object obj = mapToInstance(map);
                if (obj instanceof String) {
                    if (0 != obj.toString().length()) {
                        _log.warn(obj + ":" + _name + ":" + map);
                    }
                } else {
                    count++;
                }
            } catch (final NullPointerException e) {
                _log.warn("インスタンス化失敗(NPE:" + e.getMessage() + ")" + _name + ":" + map);
            } catch (final IllegalArgumentException e) {
                _log.warn("インスタンス化失敗(IArE:" + e.getMessage() + ")" + _name + ":" + map);
            } finally {
                if (null != map) {
                    map.clear();
                }
            }
        }
        return count;
    }

    /**
     * DBから読み込む。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @throws SQLException 例外
     */
    public void load(
            final Connection conn,
            final ControlMaster cm
    ) throws SQLException {
        _conn = conn;
        _cm = cm;

        try {
            // loadの前処理
            preLoad0();
        } catch (final SQLException e) {
            _log.error(e.getMessage(), e);
        }

        final long start = System.currentTimeMillis();

        final String sql = getQuerySql() + KenjaUtils.LINE_SEPA;
        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new MapListHandler(new MyBasicRowProcessor());
        List list = null;
        try {
            list = (List) qr.query(conn, sql, cm == null ? null : getQueryParams(cm), rsh);

            // 「読み込み専用」ならコミットする。※パフォーマンス改善のため。
            if (_conn.isReadOnly()) {
                _conn.commit();
            }

            final int size = convert(list);
            final long elapsed = System.currentTimeMillis() - start;
            final String strElapsed = StringUtils.leftPad(String.valueOf(elapsed), 4) + "ミリ秒 :";
            if (0 < size) {
                _log.fatal(strElapsed + StringUtils.leftPad(String.valueOf(size), 4) + "件:" + _name);
            } else {
                _log.fatal(strElapsed + "ZERO件:" + _name);
            }

            // loadの後処理
            postLoad0();
        } finally {
            if (null != list) {
                list.clear();
            }
        }
    }
} // AbstractDaoLoader

// eof
