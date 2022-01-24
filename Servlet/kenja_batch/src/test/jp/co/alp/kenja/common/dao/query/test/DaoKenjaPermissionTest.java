// kanji=漢字
/*
 * $Id: DaoKenjaPermissionTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/16 15:46:28 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query.test;

import java.net.URL;
import java.util.Properties;

import java.awt.Image;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jp.co.alp.kenja.common.KenjaProgramInfo;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoKenjaPermission;
import jp.co.alp.kenja.common.dao.test.NullConnection;
import jp.co.alp.kenja.common.dao.test.NullPreparedStatement;
import jp.co.alp.kenja.common.dao.test.NullResultSet;
import jp.co.alp.kenja.common.djtest.KenjaProgramInfoDJTest;
import jp.co.alp.kenja.common.domain.KenjaPermission;
import jp.co.alp.kenja.common.util.KenjaParameters;

import junit.framework.TestCase;

public class DaoKenjaPermissionTest extends TestCase {

    public DaoKenjaPermissionTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testQuery_文字列一致で管理者() {
        final DbConnection dbcon = new MyDbConn(true);
        final DaoKenjaPermission dkp = DaoKenjaPermission.getInstance();
        Properties prop = new Properties();
        prop.put("permission.adminIDs", "00999999");
        assertSame(KenjaPermission.WRITABLE, dkp.query(dbcon, prop));
    }

    public void testQuery_正規表現で管理者() {
        final DbConnection dbcon = new MyDbConn(true);
        final DaoKenjaPermission dkp = DaoKenjaPermission.getInstance();
        Properties prop = new Properties();
        prop.put("permission.adminIDs", "m/^(00999998|00999999)$/");
        assertSame(KenjaPermission.WRITABLE, dkp.query(dbcon, prop));
    }

    public void testQuery_権限チェックしないで_更新可能制限付き() {
        final DbConnection dbcon = new MyDbConn(true);
        final DaoKenjaPermission dkp = DaoKenjaPermission.getInstance();
        Properties prop = new Properties();
        prop.put("permission.adminIDs", "m/MalformedPerl5PatternException/a"); // 末尾の「/a」は、MalformedPerl5PatternException
        prop.put("permission.doCheck", "false");
        assertSame(KenjaPermission.RESTRICTED_WRITABLE, dkp.query(dbcon, prop));
    }

    public void testQuery_DB問い合わせで_参照可能() {
        final DbConnection dbcon = new MyDbConn(true);
        final DaoKenjaPermission dkp = DaoKenjaPermission.getInstance();
        Properties prop = new Properties();
        prop.put("permission.adminIDs", "m/admin/");
        prop.put("permission.doCheck", "true");
        assertSame(KenjaPermission.READABLE, dkp.query(dbcon, prop));
    }

    public void testQuery_DB問い合わせ失敗で_権限なし() {
        final DbConnection dbcon = new MyDbConn(false);
        final DaoKenjaPermission dkp = DaoKenjaPermission.getInstance();
        Properties prop = new Properties();
        prop.put("permission.adminIDs", "m/admin/");
        prop.put("permission.doCheck", "true");
        assertSame(KenjaPermission.DENIED, dkp.query(dbcon, prop));
    }

    //========================================================================

    private class MyDbConn implements DbConnection {
        final boolean _flag;
        MyKenjaParameters _params = new MyKenjaParameters();

        public MyDbConn(final boolean flag) {
            _flag = flag;
        }
        public DataSource getDataSource() { return null; }
        public Connection getRWConnection() throws SQLException { return null; }
        public Connection getROConnection() throws SQLException {
            return new MyConn(_flag);
        }
        public String getDbUrl() { return null; }
        public KenjaParameters getParameters() {
            return _params;
        }
        public String getDbHost() { return null; }
        public String getDbName() { return null; }
        public String getDriverName() { return null; }
        public String getDriverVersion() { return null; }
        public void close() throws SQLException {}
        public void closeQuietly() {}
    } // MyDbConn

    //========================================================================

    private class MyKenjaParameters extends KenjaParameters {
        KenjaProgramInfo _pinfo = new KenjaProgramInfoDJTest.MyKenjaProgramInfo();

        public String getStaffCd() {
            return "00999999";
        }

        public KenjaProgramInfo getProgramInfo() {
            return _pinfo;
        }

        public String getParameter(final String key) { return null; }
        public URL getResourceURL(final String filename) { return null; }
        public Image getImage(final URL url) { return null; }
    } // MyKenjaParameters

    //========================================================================

    private class MyConn extends NullConnection {
        final boolean _flag;
        public MyConn(final boolean flag) {
            _flag = flag;
        }
        public PreparedStatement prepareStatement(final String sql) throws SQLException {
            if (!_flag) {
                throw new SQLException("カバレッジ対策");
            }
            return new MyPrep(sql);
        }
    } // MyConn

    //========================================================================

    private class MyPrep extends NullPreparedStatement {
        final String _sql;
        public MyPrep(final String sql) {
            _sql = sql;
        }

        /**
         * {@inheritDoc}
         */
        public ResultSet executeQuery() throws SQLException {
            return new MyResultSet();
        }
    } // MyPrep

    //========================================================================

    private class MyResultSet extends NullResultSet {
        public boolean next() throws SQLException {
            return true;
        }

        public String getString(final int columnIndex) throws SQLException {
            return "2";
        }
    } // MyResultSet
} // DaoKenjaPermissionTest

// eof
