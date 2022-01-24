// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/22 10:21:37 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * パラメータ。
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** CSVファイルの文字コード */
    public static final String encode = "MS932";

    private DB2UDB _db;

    private final String _dbUrl;
    private final String _date;
    private final boolean _isOutputMode;
    private final String _file;
    private final String _schoolDiv;
    private final String _update;

    /** 年度。(学期マスタ) */
    protected String _year;
    /** 学期。(学期マスタ) */
    protected String _semester;

    /**
     * コンストラクタ。
     * @param args
     */
    public Param(final String[] args) {
        if (5 != args.length) {
            System.err.println("Usage: java Main <//localhost:50000/witestdb> <yyyy-mm-dd> <-out|-in> <outputFolder|inputFile> <GakkoKubun>");
            throw new IllegalArgumentException("引数の数が違う");
        }

        _dbUrl = args[0];
        _date = args[1];
        final String option = args[2].toLowerCase();
        _file = args[3];

        if (!"-out".equals(option) && !"-in".equals(option)) {
            throw new IllegalArgumentException("第3引数は -in or -out");
        }
        _isOutputMode = "-out".equals(option) ? true : false;
        if (_isOutputMode) {
            final File file = new File(_file);
            if (!file.isDirectory()) {
                throw new IllegalArgumentException("'" + _file + "'はディレクトリではない");
            }
        }

        _schoolDiv = args[4];

        _update = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        log.debug("更新日=" + _update);
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public String getDate() {
        return _date;
    }

    public String getFile() {
        return _file;
    }

    public boolean isOutputMode() {
        return _isOutputMode;
    }

    public void load(final DB2UDB db) throws SQLException {
        loadSemesterMst(db);
        Curriculum.loadCurriculumMst(db);
    }

    private void loadSemesterMst(final DB2UDB db) throws SQLException {
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
     * 更新日を得る。
     * @return 更新日
     */
    public String getUpdate() {
        return _update;
    }

    /**
     * 学校区分を得る。
     * @return 学校区分
     */
    public String getSchoolDiv() {
        return _schoolDiv;
    }

    /**
     * フルパスのファイル名を得る。
     * @param file ファイル名
     * @return フルパスなファイル名
     */
    public String getFullPath(final String file) {
        return getFile() + "/" + file;  // TAKAESU: パラメータは区切り文字が付いている場合あり。and 区切り文字は '/' と '\' など。
    }
} // Param

// eof
