// kanji=漢字
/*
 * $Id: DbConnection.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/10
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * DB接続のパラメータや、データソースを保持するインタフェース。
 * rev1.27まではclass。rev1.28からinterface。
 * @author tamura
 * @version $Id: DbConnection.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface DbConnection {
    /** プロパティファイルのファイル名 */
    String PROPERTYFILE = "dbcp.properties";

    /**
     * データソースを得る。
     * @return データソース
     */
    DataSource getDataSource();

    /**
     * 読み書き両用のコネクションを得る。
     * @return コネクション
     * @throws SQLException DBMS例外
     */
    Connection getRWConnection() throws SQLException;

    /**
     * 読み込み専用のコネクションを得る。
     * @return コネクション
     * @throws SQLException DBMS例外
     */
    Connection getROConnection() throws SQLException;

    /**
     * DB接続のURLを返す。
     * 例：<code>jdbc:db2:database</code>
     * 例：<code>jdbc:db2://host/database</code>
     * @return DB接続のURLを表す文字列
     */
    String getDbUrl();

    /**
     * パラメータを得る。
     * @return パラメータ
     */
    KenjaParameters getParameters();

    /**
     * DBホスト名を得る。
     * @return DBホスト名
     */
    String getDbHost();

    /**
     * DB名を得る。
     * @return DB名
     */
    String getDbName();

    /**
     * JDBCドライバの名前を得る
     * @return JDBCドライバの名前
     */
    String getDriverName();

    /**
     * JDBCドライバのバージョンを得る
     * @return JDBCドライバのバージョン
     */
    String getDriverVersion();

    /**
     * データソース（プーリング）を閉じる。
     * @throws SQLException SQL例外
     */
    void close() throws SQLException;

    /**
     * データソース（プーリング）を静かに閉じる。
     */
    void closeQuietly();

} // DbConnection

// eof
