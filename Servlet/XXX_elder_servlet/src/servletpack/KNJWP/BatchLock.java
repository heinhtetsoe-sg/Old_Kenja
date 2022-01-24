// kanji=漢字
/*
 * $Id: a91143bb05639cf2989ce1cb4abdc9028c314b1e $
 *
 * 作成日: 2008/01/17 9:58:43 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 排他制御。
 * @author takaesu
 * @version $Id: a91143bb05639cf2989ce1cb4abdc9028c314b1e $
 */
public class BatchLock {
    /**
     * ステータス、実行中。
     */
    private static final String STAT_RUNNING = "RUNNING";
    /**
     * ステータス、完了状態。
     */
    private static final String STAT_OK = "OK";

    /*pkg*/static final Log log = LogFactory.getLog(BatchLock.class);

    private final DB2UDB _db2;
    private final String _staffCd;
    private final String _uniqId;

    public static final String TABLE_NAME = "BATCH_LOCK";

    /**
     * コンストラクタ。
     * @param db2
     * @param staffCd
     * @param uniqId
     */
    public BatchLock(final DB2UDB db2, final String staffCd, final String uniqId) {
        _db2 = db2;
        _staffCd = staffCd;
        _uniqId = uniqId;
    }

    /**
     * ロック中か?
     * @return ロック状態なら true
     */
    public boolean isLock() {
        final Value value = select();
        if (null == value) {
            return false;
        }
        return STAT_RUNNING.equals(value._status);
    }

    /**
     * ロックする。
     * @return ロック成功なら true.
     */
    public boolean lock() {
        final Value value = select();
        try {
            if (null == value) {
                insert(STAT_RUNNING);
                log.fatal("ロックした。(レコードが無いので作成した)");
                return true;
            } else if (STAT_OK.equals(value._status)){
                update(STAT_RUNNING);
                log.fatal("ロックした。(レコードを書き換えた)");
                return true;
            } else {
                log.fatal("既にロック中");
            }
        } catch (final SQLException e) {
            log.fatal("ロック出来ない!", e);
        }

        return false;
    }

    private Value select() {
        ResultSet rs = null;

        Value value = null;
        try {
            _db2.query(sqlSelect());
            rs = _db2.getResultSet();
            if (rs.next()) {
                final String status = rs.getString("STATUS");
                final String registercd = rs.getString("REGISTERCD");
                final Timestamp updated = rs.getTimestamp("UPDATED");
                value = new Value(_uniqId, status, registercd, updated);
            }
        } catch (final SQLException e) {
            log.error("レコード取得時にエラー", e);
        }
        return value;
    }

    private void insert(final String status) throws SQLException {
        final String sql = sqlInsert(status);
        _db2.executeUpdate(sql);
    }

    private void update(final String status) throws SQLException {
        _db2.executeUpdate(sqlUpdate(status));
    }

    /**
     * ロックを解除する。
     */
    public void unlock() {
        final Value value = select();
        if (null == value) {
            log.fatal("アンロックできない(レコードが無い)");
            return;
        }
        if (STAT_OK.equals(value._status)) {
            log.fatal("アンロックしない(アンロック済みだから)");
            return;
        }
        try {
            _db2.executeUpdate(sqlUpdate(STAT_OK));
            log.fatal("アンロックした");
        } catch (final SQLException e) {
            log.fatal("アンロック出来ない!", e);
        }
    }

    private String sqlSelect() {
        final String sql;
        sql = "select * from " + TABLE_NAME
            + " where PGID='" + _uniqId + "'"
            ;
        return sql;
    }

    private String sqlInsert(final String status) {
        final String sql;
        sql = "insert into " + TABLE_NAME + " values("
            + "'" + _uniqId + "',"
            + "'" + status + "',"
            + "'" + _staffCd + "',"
            + "current timestamp"
            + ")"
            ;
        return sql;
    }

    private String sqlUpdate(final String status) {
        final String sql;
        sql = "update " + TABLE_NAME + " set"
            + "  STATUS='" + status + "',"
            + "  REGISTERCD='" + _staffCd + "',"
            + "  UPDATED=current timestamp"
            + " where pgid='" + _uniqId + "'"
            ;
        return sql;
    }

    private class Value {
        private final String _pgid;
        private final String _status;
        private final String _registercd;
        private final Timestamp _updated;

        public Value(
                final String pgid,
                final String status,
                final String registercd,
                final Timestamp updated
        ) {
            _pgid = pgid;
            _status = status;
            _registercd = registercd;
            _updated = updated;
        }
    }

    public static void main(final String[] args) throws Exception {
        final DB2UDB db2 = new DB2UDB("//withus:50000/WITESTDB", "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db2.open();
        final BatchLock lock = new BatchLock(db2, "takaesu", "test");

        lock.lock();
        db2.commit();

        lock.unlock();
        db2.commit();

        db2.close();
    }
} // BatchLock

// eof
