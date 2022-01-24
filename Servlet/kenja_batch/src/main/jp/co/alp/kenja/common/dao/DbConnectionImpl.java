// kanji=漢字
/*
 * $Id: DbConnectionImpl.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/01/10 13:32:37 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.SQLNestedException;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.KenjaThread;
import jp.co.alp.kenja.common.util.KenjaIOUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * DB接続のパラメータや、データソースを保持する実装。
 * DbConnection.javaのrev1.27から移動した。
 * @author tamura
 * @version $Id: DbConnectionImpl.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DbConnectionImpl implements DbConnection {
    /** log */
    /*pkg*/static final Log log = LogFactory.getLog(DbConnectionImpl.class);
    private static DbConnectionImpl singleton_;

    private static final String DBHOST = "<dbhost>";
    private static final String DBNAME = "<dbname>";

    protected final BasicDataSource _dataSource = new BasicDataSource();
    protected final KenjaParameters _params;
    protected String _driverName;
    protected String _driverVersion;

    /**
     * JDBCドライバを得るためのスレッド。
     */
    private class JDBCInfoThread extends KenjaThread {
        /*pkg*/JDBCInfoThread() {
            super("getJDBC");
        }

        public void run() {
            Connection conn = null;
            try {
                log.debug("get JDBC driver info...");
                conn = _dataSource.getConnection();
                conn.setReadOnly(true);
                final DatabaseMetaData meta = conn.getMetaData();
                _driverName = meta.getDriverName();
                _driverVersion = meta.getDriverVersion();

                log.debug("JDBC driver is [" + _driverName + "], [" + _driverVersion + "]");
            } catch (final SQLException e) {
                log.error("JDBCドライバのバージョンを取得できない", e);
            } catch (final StackOverflowError e) {
                log.error("JDBCドライバのバージョンを取得できない:" + e.getClass().getName());
                // CSOFF: IllegalCatch
            } catch (final Throwable e) {
                // CSON: IllegalCatch
                log.error("JDBCドライバのバージョンを取得できない", e);
            } finally {
                DbUtils.closeQuietly(conn);
                conn = null;
            }
        }
    } // JDBCInfoThread

    /*
     * コンストラクタ。
     * @param params パラメータ
     */
    private DbConnectionImpl(final KenjaParameters params) throws IOException {
        _params = params;
        final URL url = _params.getResourceURL(PROPERTYFILE);
        if (null == url) {
            log.fatal(PROPERTYFILE + "を開けません。");
            throw new FileNotFoundException(PROPERTYFILE + "を開けません。");
        }
        setDataSource(url);
        new JDBCInfoThread().start();
    }

    /**
     * DbConnection のインスタンスを得る。
     * インスタンスがまだなければ<code>null</code>を返す
     * @return DbConnectionのインスタンス
     */
    public static synchronized DbConnection getInstance() {
        if (null == singleton_) {
            log.warn("インスタンスがまだありません");
        }
        return singleton_;
    }

    /**
     * DbConnection のインスタンスを得る。
     * インスタンスがまだなければ作成する
     * @param params パラメータ
     * @return DbConnectionのインスタンス
     * @throws SQLException 例外
     */
    public static synchronized DbConnection create(final KenjaParameters params) throws SQLException {
        if (null != singleton_) {
            if (!singleton_.getDbName().equals(params.getDbName())) {
                destroy();
            }
        }

        if (null == singleton_) {
            try {
                singleton_ = new DbConnectionImpl(params);
            } catch (final IOException e) {
                throw new SQLNestedException("DBに接続できない", e);
//            } catch (SQLException e) {
//                throw new SQLNestedException("DBに接続できない", e);
            }
        }
        return singleton_;
    }

    /**
     * 破棄する。
     */
    public static synchronized void destroy() {
        log.debug("destroy...");
        if (null != singleton_) {
            log.debug("destroy: close...");
            singleton_.closeQuietly();
            // nullを代入し、次のif文で新しくインスタンスを作成する。
            singleton_ = null;
        }
    }

    /**
     * データソースを得る。
     * @return データソース
     * {@inheritDoc}
     */
    public DataSource getDataSource() { return _dataSource; }

    /**
     * 読み書き両用のコネクションを得る。
     * @return コネクション
     * @throws SQLException DBMS例外
     * {@inheritDoc}
     */
    public Connection getRWConnection() throws SQLException {
        return _dataSource.getConnection();
    }

    /**
     * 読み込み専用のコネクションを得る。
     * @return コネクション
     * @throws SQLException DBMS例外
     * {@inheritDoc}
     */
    public Connection getROConnection() throws SQLException {
        final Connection conn = _dataSource.getConnection();
        conn.setReadOnly(true);
        return conn;
    }

    /**
     * DB接続のURLを返す。
     * 例：<code>jdbc:db2:database</code>
     * 例：<code>jdbc:db2://host/database</code>
     * @return DB接続のURLを表す文字列
     * {@inheritDoc}
     */
    public String getDbUrl() {
        final String host = getDbHost();
        if (StringUtils.isEmpty(host)) {
            return "jdbc:db2:" + getDbName();
        } else {
            return "jdbc:db2://" + host + "/" + getDbName();
        }
    }

    /**
     * パラメータを得る。
     * @return パラメータ
     * {@inheritDoc}
     */
    public KenjaParameters getParameters() {
        return _params;
    }

    /**
     * DBホスト名を得る。
     * @return DBホスト名
     * {@inheritDoc}
     */
    public String getDbHost() { return _params.getDbHost(); }

    /**
     * DB名を得る。
     * @return DB名
     * {@inheritDoc}
     */
    public String getDbName() { return _params.getDbName(); }

    /**
     * JDBCドライバの名前を得る
     * @return JDBCドライバの名前
     * {@inheritDoc}
     */
    public String getDriverName() { return _driverName; }

    /**
     * JDBCドライバのバージョンを得る
     * @return JDBCドライバのバージョン
     * {@inheritDoc}
     */
    public String getDriverVersion() { return _driverVersion; }

    /**
     * データソース（プーリング）を閉じる。
     * @throws SQLException SQL例外
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        _dataSource.close();
    }

    /**
     * データソース（プーリング）を静かに閉じる。
     * {@inheritDoc}
     */
    public void closeQuietly() {
        try {
            close();
        } catch (final SQLException e) {
            log.warn("SQLException", e);
        }
    }

    /*
     * DB接続のURLを返す。
     * @param url URLのテンプレート
     * @return DB接続のURLを表す文字列
     * {@inheritDoc}
     */
    private String getDbUrl(final String url) {
        String rtn = url;
        final String host = getDbHost();

        // <dbhost>
        if (StringUtils.isEmpty(host)) {
            ;
        } else {
            if (0 <= rtn.indexOf(DBHOST)) {
                rtn = StringUtils.replace(rtn, DBHOST, host);
            } else {
                log.fatal("★urlに" + DBHOST + "がない！");
                return getDbUrl();
            }
        }

        // <dbname>
        if (0 <= rtn.indexOf(DBNAME)) {
            rtn = StringUtils.replace(rtn, DBNAME, getDbName());
        } else {
            log.fatal("★urlに" + DBNAME + "がない！");
            final String[] split = StringUtils.split(rtn, "/");
            final int minLen = 3;
            if (split == null || split.length < minLen || "".equals(split[split.length - 1])) {
                return getDbUrl();
            }
            log.fatal(" プロパティーファイルによるDBNAME = " + split[split.length - 1]);
        }

        log.debug("url[" + url + "] --> [" + rtn + "]");
        return rtn;
    }

    /*
     * URLからDB接続のデータソースを設定する。
     * @param url プロパティファイルのURL
     */
    private void setDataSource(final URL url) {
//        assert null != url;

        InputStream is = null;
        try {
            is = url.openStream();
//            assert null != is;

            final Properties prop = new Properties();
            try {
                // 入力ストリームから、propに読み込む
                prop.load(is);
            } catch (final IOException e) {
                log.error("InputStreamから読み出し中にエラー", e);
            }

            final String jdbcurl = prop.getProperty("url");
            prop.put("url", getDbUrl(jdbcurl));

            try {
                // propから、_dataSourceに設定する
                BeanUtils.populate(_dataSource, prop);
            } catch (final IllegalAccessException e) {
                log.error("DATASOURCEにプロパティを設定できない", e);
            } catch (final InvocationTargetException e) {
                log.error("DATASOURCEにプロパティを設定できない", e);
            }
        } catch (final IOException e) {
            log.error("url から InputStream を得られない", e);
        } finally {
            KenjaIOUtils.closeQuietly(is);
        }
    }
} // DbConnectionImpl

// eof
