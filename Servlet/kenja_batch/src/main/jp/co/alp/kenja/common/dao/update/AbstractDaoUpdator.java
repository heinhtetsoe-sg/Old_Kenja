// kanji=漢字
/*
 * $Id: AbstractDaoUpdator.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/08/14 16:59:13 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.update;

import java.util.Arrays;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;

import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.lang.QuietlyClosable;
import jp.co.alp.kenja.common.util.KenjaParameters;

/*
 * insert into テーブル values (?, ?, ?)
 * insert into テーブル (カラム1, カラム2) values (?, ?)
 *
 * update テーブル set カラム1 = ?, カラム2 = ? where カラムX = ?
 * update テーブル set (カラム1, カラム2, カラム3) = (?, ?, ?) where カラムX = ?
 *
 * delete from テーブル where カラムX = ?
 */

/**
 * DB書き込みの抽象クラス。
 * 更新は、org.apache.commons.dbutilsとJDBCの比較の結果、JDBCを採用した。
 *   -コーディングの手間があまり変わらない。
 *   -dbutilsは「PreparedStatementの使いまわし」などの融通が利かない。
 * @author tamura
 * @version $Id: AbstractDaoUpdator.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class AbstractDaoUpdator implements QuietlyClosable {
    /** 改行 */
    public static final String LINE_SEPA = System.getProperty("line.separator", "\n");

    private final Log _log;
    private final Connection _conn;
    private final ControlMaster _cm;
    private final KenjaParameters _params;

    private String _sql;
    private KenjaPS _ps;

    /**
     * コンストラクタ。
     * @param log 出力用のlog
     * @param conn コネクション
     * @param cm コントロールマスタ
     * @param params パラメータ
     */
    public AbstractDaoUpdator(
            final Log log,
            final Connection conn,
            final ControlMaster cm,
            final KenjaParameters params
    ) {
        _log = log;
        _conn = conn;
        _cm = cm;
        _params = params;
    }

    /**
     * SQL文を得る。
     * @return SQL文
     */
    public abstract String getUpdateSql();

    /**
     * コントロールマスタを得る。
     * @return コントロールマスタ
     */
    public ControlMaster getControlMaster() { return _cm; }


    /**
     * パラメータを得る。
     * @return パラメータ
     */
    public KenjaParameters getParameters() { return _params; }

    /**
     * psを得る。
     * @param doClearParameters パラメータをクリアするか否か
     * @return ps
     * @throws SQLException 例外
     */
    protected synchronized KenjaPS ps(final boolean doClearParameters) throws SQLException {
        if (null == _ps) {
            _sql = getUpdateSql();
            _ps = new KenjaPS(_conn.prepareStatement(_sql));
        }
        if (doClearParameters) {
            _ps.clearParameters();
        }
        return _ps;
    }

    /**
     * psを得る。
     * @return ps
     * @throws SQLException 例外
     */
    protected synchronized KenjaPS ps() throws SQLException {
        return ps(true);
    }

    /**
     * 例外を再作成する。
     * @param cause 元の例外
     * @param sql SQL文
     * @param params パラメータの配列
     * @return 再作成した例外
     */
    protected SQLException rethrow(
            final SQLException cause,
            final String sql,
            final Object[] params
    ) {
        final StringBuffer msg = new StringBuffer(cause.getMessage());

        msg.append(" Query: ").append(sql).append(LINE_SEPA);

        if (null != params && 0 < params.length) {
            msg.append(" Parameters: ").append(Arrays.asList(params));
        }

        final SQLException e = new SQLException(msg.toString());
        e.setNextException(cause);

        return e;
    }

    /**
     * パラメータを設定する。
     * @param params パラメータ
     * @throws SQLException 例外
     * @deprecated このコードでは、nullを書き込めない様なので、注意！
     */
    protected void fillStatement(final Object[] params) throws SQLException {
        if (null == params || 0 >= params.length) {
            return;
        }

        ps(false);

        for (int i = 0; i < params.length; i++) {
            if (null != params[i]) {
                _ps.setObject(i + 1, params[i]);
            } else {
                _ps.setNull(i + 1, Types.OTHER);
            }
        }
    }

    /**
     * 更新系を実行する。
     * @param params パラメータ
     * @return 更新したレコード数
     * @throws SQLException 例外
     * @deprecated このコードでは、nullを書き込めない様なので、注意！
     */
    public int executeUpdate(final Object[] params) throws SQLException {
        try {
            ps(false);
            fillStatement(params);
            return _ps.executeUpdate();
        } catch (final SQLException e) {
            throw rethrow(e, _sql, params);
        }
    }

    /**
     * 更新系を実行する。
     * @return 更新したレコード数
     * @throws SQLException 例外
     */
    public int executeUpdate() throws SQLException {
        try {
            ps(false);
            return _ps.executeUpdate();
        } catch (final SQLException e) {
            throw rethrow(e, _sql, null);
        }
    }

    /**
     * 静かに閉じる。
     * {@inheritDoc}
     */
    public void closeQuietly() {
        DbUtils.closeQuietly(_ps);
    }
} // AbstractDaoUpdator

// eof
