// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.knjxTool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * パラメータ。
 *
 * @author m-yamashiro
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /* pkg */static final Log log = LogFactory.getLog(Param.class);

    public final String _knjUrl;
    public final String _knjUser;
    public final String _knjPass;

    /** 基準日. */
    public final String _date;

    /** 更新日時. */
    private final Date _now = new Date();

    public String _year;
    public final List _a023List = new ArrayList();

    public Param(final String[] args) {
        if (4 != args.length) {
            System.err.println("Usage: java Main <//db2Host:50000/db2DB> <db2 user> <db2 passwd> <yyyy-mm-dd>");
            throw new IllegalArgumentException("引数の数が違う");
        }
        _knjUrl = args[0];
        _knjUser = args[1];
        _knjPass = args[2];
        _date = args[3];
    }

    public void load(final Database db) throws SQLException {
        loadYear(db);
        loadA023(db);
    }

    private void loadYear(final Database db) throws SQLException {
        try {
            db.query("SELECT CTRL_YEAR FROM CONTROL_MST WHERE CTRL_NO = '01'");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("CTRL_YEAR");
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            log.fatal(_date + "から年度、学期が得られない! semester_mstから取得出来ない");
            throw e;
        }
        try {
            Integer.parseInt(_year);
        } catch (final NumberFormatException e) {
            log.fatal(_date + "から求めた年度が正しくない(semester_mstから取得出来ない)⇒" + _year);
            throw e;
        }
    }

    private void loadA023(final Database db) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A023'";
            ps = db.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String namecd2 = rs.getString("NAMECD2");
                final String name1 = rs.getString("NAME1");
                final String name2 = rs.getString("NAME2");
                final String name3 = rs.getString("NAME3");
                final String abbv1 = rs.getString("ABBV1");
                final String abbv2 = rs.getString("ABBV2");
                final String abbv3 = rs.getString("ABBV3");
                final String nmsp1 = rs.getString("NAMESPARE1");
                final String nmsp2 = rs.getString("NAMESPARE2");
                final String nmsp3 = rs.getString("NAMESPARE3");
                final NameMst nameMst = new NameMst(namecd2, name1, name2, name3, abbv1, abbv2, abbv3, nmsp1, nmsp2, nmsp3);
                _a023List.add(nameMst);
            }
            db.commit();
            rs.close();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    public class NameMst {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _name3;
        final String _abbv1;
        final String _abbv2;
        final String _abbv3;
        final String _nmsp1;
        final String _nmsp2;
        final String _nmsp3;

        public NameMst(
                final String namecd2,
                final String name1,
                final String name2,
                final String name3,
                final String abbv1,
                final String abbv2,
                final String abbv3,
                final String nmsp1,
                final String nmsp2,
                final String nmsp3
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _name3 = name3;
            _abbv1 = abbv1;
            _abbv2 = abbv2;
            _abbv3 = abbv3;
            _nmsp1 = nmsp1;
            _nmsp2 = nmsp2;
            _nmsp3 = nmsp3;
        }
    }

    /**
     * 更新日時を得る。
     *
     * @return 更新日時
     */
    public Date getUpdate() {
        return _now;
    }

    /** A023があるか */
    public boolean existA023() {
        return _a023List.size() > 0;
    }

    public String toString() {
        final String update = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(_now);
        return " 実行日時=" + update;
    }
} // Param

// eof
