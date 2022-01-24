/*
 * $Id: AbstractKenjaBatch.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.sql.SQLException;

import jp.co.alp.kenja.batch.dao.DaoDateSemester;
import jp.co.alp.kenja.common.dao.DaoUtils;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.DbConnectionImpl;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractKenjaBatch {
    /*pkg*/static final Log log = LogFactory.getLog(AbstractAccumulateSummaryBatch.class);
    final MyEnum.Category _category = new MyEnum.Category(); // アプリケーションで唯一のnew MyEnum.Category()

    private KenjaBatchContext _ctx;

    public AbstractKenjaBatch(final KenjaBatchContext ctx) {
        _ctx = ctx;
    }

    /**
     * 各クラスで実装するバッチ処理メソッド。
     * @param dbcon DB接続
     * @throws Exception 例外
     */
    protected abstract void batch(DbConnection dbcon) throws Exception;

    protected KenjaBatchContext getContext() {
        return _ctx;
    }

    /**
     * バッチ処理を起動する。
     */
    public void invoke() {
        DbConnection dbcon = null;
        try {
            dbcon = connection(_ctx.getKenjaParameters());
            _ctx.setControlMaster(getControlMaster(_ctx.getKenjaParameters(), dbcon, _ctx.getDate()));

            batch(dbcon);

        } catch (final SQLException e) {
            /*
             * SQLSTATE 23505: ユニーク索引またはユニーク制約で定められている制約に対する違反が
             * 発生しました。
             */
            if (DaoUtils.compareSQLException(e, -803, "23505")) {
                log.fatal("ユニーク索引またはユニーク制約で定められている制約に対する違反が発生しました。");
            }
            log.fatal("== invoke failure(SQLException)", e);
            // CHECKSTYLE:OFF -don't worry <Illegal Catch: 'Exception' をキャッチすることは許可されていません。>
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            log.fatal("== invoke failure", e);
        } finally {
            if (null != dbcon) {
                DbConnectionImpl.destroy();
                log.debug("DBCP Closed");
            }
        }
    }

    /**
     * DB接続を得る
     * @param kenjaParams パラメータ
     * @return DB接続
     * @throws Exception 例外
     */
    private DbConnection connection(
            final KenjaParameters kenjaParams
    ) throws Exception {
        final DbConnection dbcon;
        try {
            dbcon = DbConnectionImpl.create(kenjaParams);
            if (null == dbcon) {
                throw new Exception("DB接続情報を作成できない");
            }
            return dbcon;
        } catch (final SQLException e) {
            log.fatal("DB接続情報を作成中に例外", e);
            throw e;
        }
    }

    /**
     * コントロールマスタを得る
     * @param kenjaParameter パラメータ
     * @param dbcon DB接続
     * @param date 日付
     * @return コントロールマスタ
     * @throws SQLException SQL例外
     */
    protected ControlMaster getControlMaster(
            final KenjaParameters kenjaParameter,
            final DbConnection dbcon,
            final KenjaDateImpl date
    ) throws SQLException {
        final DaoDateSemester daoSemes = new DaoDateSemester(date);
        daoSemes.load(dbcon.getROConnection());

        return new ControlMaster(
                _category,
                daoSemes.getYear(),
                daoSemes.getSemester(),
                date,
                ControlMaster.DISPLAY_SUBCLASS,
                null
        );
    }
}
