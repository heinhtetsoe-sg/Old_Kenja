// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.edboardStaffTorikomi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nao_package.db.Database;

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

    public String toString() {
        final String update = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(_now);
        return " 実行日時=" + update;
    }
} // Param

// eof
