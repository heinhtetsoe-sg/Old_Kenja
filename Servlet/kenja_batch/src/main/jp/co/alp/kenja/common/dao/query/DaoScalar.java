// kanji=漢字
/*
 * $Id: DaoScalar.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/05/12 19:14:56 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import org.apache.commons.logging.Log;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;

/**
 * DBからスカラー値を取得する抽象クラス。
 * @author tamura
 * @version $Id: DaoScalar.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class DaoScalar extends AbstractDaoScalar {
    private boolean _get;
    private Object _val;

    /**
     * コンストラクタ。
     * @param log log
     * @param index インデックス
     */
    protected DaoScalar(final Log log, final int index) {
        super(log, index);
    }

    /**
     * コンストラクタ。
     * @param log log
     * @param columnName カラム名
     */
    protected DaoScalar(final Log log, final String columnName) {
        super(log, columnName);
    }

    /**
     * スカラー値を取得する。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param clazz Javaでの型
     * @return 読み込んだ値
     * @throws SQLException 例外
     */
    protected final synchronized Object get(
            final Connection conn,
            final ControlMaster cm,
            final Class<?> clazz
    ) throws SQLException {
        if (!_get) {
            _val = query(conn, cm);
            _get = true;
            _log.info("query=" + _val);
        }

        if (null == _val) {
            return null;
        }

        if (!clazz.isInstance(_val)) {
            final String msg = "型の代入互換がない:" + clazz.getName() + "," + _val.getClass().getName();
            _log.warn(msg);
            throw new ClassCastException(msg);
        }
        return _val;
    }

    /**
     * DBからスカラー値を得る。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @return 読み込んだ値
     * @throws SQLException SQL例外
     */
    public Object getObject(
            final Connection conn,
            final ControlMaster cm
    ) throws SQLException {
        return get(conn, cm, Object.class);
    }

    //========================================================================

    /**
     * DBからスカラー値(int)を取得する抽象クラス。
     */
    public abstract static class AsInt extends DaoScalar {
        /**
         * コンストラクタ。
         * @param log log
         * @param index インデックス
         */
        protected AsInt(final Log log, final int index) {
            super(log, index);
        }

        /**
         * コンストラクタ。
         * @param log log
         * @param columnName カラム名
         */
        protected AsInt(final Log log, final String columnName) {
            super(log, columnName);
        }

        /**
         * DBからスカラー値を得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @return 読み込んだ値
         * @throws SQLException SQL例外
         * @throws NullPointerException nullの場合
         */
        public int getInt(
                final Connection conn,
                final ControlMaster cm
        ) throws SQLException {
            final Object obj = get(conn, cm, Integer.class);
            if (null == obj) {
                _log.warn("スカラー値がnull");
                throw new NullPointerException("スカラー値がnull");
            }
            return ((Integer) obj).intValue();
        }
    } // AsInt

    //========================================================================

    /**
     * DBからスカラー値(long)を取得する抽象クラス。
     */
    public abstract static class AsLong extends DaoScalar {
        /**
         * コンストラクタ。
         * @param log log
         * @param index インデックス
         */
        protected AsLong(final Log log, final int index) {
            super(log, index);
        }

        /**
         * コンストラクタ。
         * @param log log
         * @param columnName カラム名
         */
        protected AsLong(final Log log, final String columnName) {
            super(log, columnName);
        }

        /**
         * DBからスカラー値を得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @return 読み込んだ値
         * @throws SQLException SQL例外
         * @throws NullPointerException nullの場合
         */
        public long getLong(
                final Connection conn,
                final ControlMaster cm
        ) throws SQLException {
            final Object obj = get(conn, cm, Long.class);
            if (null == obj) {
                _log.warn("スカラー値がnull");
                throw new NullPointerException("スカラー値がnull");
            }
            return ((Long) obj).longValue();
        }
    } // AsLong

    //========================================================================

    /**
     * DBからスカラー値(double)を取得する抽象クラス。
     */
    public abstract static class AsDouble extends DaoScalar {
        /**
         * コンストラクタ。
         * @param log log
         * @param index インデックス
         */
        protected AsDouble(final Log log, final int index) {
            super(log, index);
        }

        /**
         * コンストラクタ。
         * @param log log
         * @param columnName カラム名
         */
        protected AsDouble(final Log log, final String columnName) {
            super(log, columnName);
        }

        /**
         * DBからスカラー値を得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @return 読み込んだ値
         * @throws SQLException SQL例外
         * @throws NullPointerException nullの場合
         */
        public double getDouble(
                final Connection conn,
                final ControlMaster cm
        ) throws SQLException {
            final Object obj = get(conn, cm, Double.class);
            if (null == obj) {
                _log.warn("スカラー値がnull");
                throw new NullPointerException("スカラー値がnull");
            }
            return ((Double) obj).doubleValue();
        }
    } // AsDouble

    //========================================================================

    /**
     * DBからスカラー値(String)を取得する抽象クラス。
     */
    public abstract static class AsString extends DaoScalar {
        /**
         * コンストラクタ。
         * @param log log
         * @param index インデックス
         */
        protected AsString(final Log log, final int index) {
            super(log, index);
        }

        /**
         * コンストラクタ。
         * @param log log
         * @param columnName カラム名
         */
        protected AsString(final Log log, final String columnName) {
            super(log, columnName);
        }

        /**
         * DBからスカラー値を得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @return 読み込んだ値
         * @throws SQLException SQL例外
         */
        public String getString(
                final Connection conn,
                final ControlMaster cm
        ) throws SQLException {
            final Object obj = get(conn, cm, String.class);
            return (String) obj;
        }
    } // AsString

    //========================================================================

    /**
     * DBからスカラー値(Date/KenjaDateImpl)を取得する抽象クラス。
     */
    public abstract static class AsDate extends DaoScalar {
        /**
         * コンストラクタ。
         * @param log log
         * @param index インデックス
         */
        protected AsDate(final Log log, final int index) {
            super(log, index);
        }

        /**
         * コンストラクタ。
         * @param log log
         * @param columnName カラム名
         */
        protected AsDate(final Log log, final String columnName) {
            super(log, columnName);
        }

        /**
         * DBからスカラー値を得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @return 読み込んだ値
         * @throws SQLException SQL例外
         */
        public Date getDate(
                final Connection conn,
                final ControlMaster cm
        ) throws SQLException {
            final Object obj = get(conn, cm, Date.class);
            return (Date) obj;
        }

        /**
         * DBからスカラー値を得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @return 読み込んだ値
         * @throws SQLException SQL例外
         */
        public KenjaDateImpl getKenjaDate(
                final Connection conn,
                final ControlMaster cm
        ) throws SQLException {
            final Date date = getDate(conn, cm);
            if (null == date) {
                return null;
            }
            return KenjaDateImpl.getInstance(date);
        }
    } // AsDate
} // DaoScalar

// eof
