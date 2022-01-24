// kanji=漢字
/*
 * $Id: AbstractDaoScalar.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/05/08 14:53:10 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.util.KenjaUtils;

/**
 * DBからスカラー値を読み込む抽象クラス。
 * @author tamura
 * @version $Id: AbstractDaoScalar.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class AbstractDaoScalar {
    protected final String _name = ClassUtils.getShortClassName(this.getClass());

    protected Connection _conn;
    protected ControlMaster _cm;

    protected final Log _log;

    private final int _index;
    private final String _columnName;

    /*
     * コンストラクタ。
     */
    private AbstractDaoScalar(
            final Log log,
            final int index,
            final String columnName
    ) {
        super();
        _log = log;
        _index = index;
        _columnName = columnName;
    }

    /**
     * コンストラクタ。
     * @param log log
     * @param index インデックス
     */
    protected AbstractDaoScalar(
            final Log log,
            final int index
    ) {
        this(log, index, null);
    }

    /**
     * コンストラクタ。
     * @param log log
     * @param columnName カラム名
     */
    protected AbstractDaoScalar(
            final Log log,
            final String columnName
    ) {
        this(log, -1, columnName);
    }

    /**
     * 問い合わせのSQL文を得る。
     * @return SQL文
     */
    protected abstract String getQuerySql();

    /**
     * 問い合わせのパラメータを得る。
     * @return パラメータ
     */
    protected abstract Object[] getQueryParams();

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
     * DBからスカラーを読み込む。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @return 読み込んだ値
     * @throws SQLException 例外
     */
    protected final Object query(
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
        final ResultSetHandler rsh = (_columnName == null) ? new ScalarHandler(_index) : new ScalarHandler(_columnName);
        final Object object = qr.query(conn, sql, getQueryParams(), rsh);

        // 「読み込み専用」ならコミットする。※パフォーマンス改善のため。
        if (_conn.isReadOnly()) {
            _conn.commit();
        }

        final long elapsed = System.currentTimeMillis() - start;
        final String strElapsed = StringUtils.leftPad(String.valueOf(elapsed), 4) + "ミリ秒 :";
        _log.fatal(strElapsed + ":" + _name);

        // loadの後処理
        postLoad0();

        return object;
    }
} // AbstractDaoScalar

// eof
