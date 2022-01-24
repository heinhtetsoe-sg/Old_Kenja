// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/30 14:23:11 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * パラメータ。
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    final String _knjUrl;
    final String _vqsUrl;
    final String _vqsUser;
    final String _vqsPass;
    /** 基準日. */
    final String _date;
    /** 更新日時. */
    private Date _now = new Date();

    /** 年度。(学期マスタ) */
    protected String _year;
    /** 学期。(学期マスタ) */
    protected String _semester;

    public Param(final String[] args) {
        if (5 != args.length) {
            System.err.println("Usage: java Main <//localhost:50000/witestdb> <//postgreHost/vqsDB> <postgre user> <postgre passwd> <yyyy-mm-dd>");
            throw new IllegalArgumentException("引数の数が違う");
        }
        _knjUrl = args[0];
        _vqsUrl = args[1];
        _vqsUser = args[2];
        _vqsPass = args[3];
        _date = args[4];
    }

    public void load(final Database db) throws SQLException {
        loadSemesterMst(db);
    }

    private void loadSemesterMst(final Database db) throws SQLException {
        try {
            db.query("SELECT year, semester FROM semester_mst WHERE '" + _date + "' BETWEEN sdate AND edate");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("year");
                _semester = rs.getString("semester");
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

    /**
     * 年度を得る。
     * (学期マスタ)
     * @return 年度
     */
    public String getYear() {
        return _year;
    }

    /**
     * 学期を得る。
     * (学期マスタ)
     * @return 学期
     */
    public String getSemester() {
        return _semester;
    }

    /**
     * 更新日時を得る。
     * @return 更新日時
     */
    public Date getUpdate() {
        return _now;
    }

    public String toString() {
        final String update = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(_now);
        return "基準日=" + _date + ", 年度=" + _year + ", 学期=" + _semester + ", 実行日時=" + update;
    }
} // Param

// eof
