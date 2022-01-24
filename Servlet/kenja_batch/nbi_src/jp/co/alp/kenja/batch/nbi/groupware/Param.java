// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/04
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * パラメータ。
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** CSVファイルの文字コード */
    public static final String encode = "MS932";

    /** 学校コード。高校 */
    public static final String SCHOOL_CODE_HIGH = "1";

    private DB2UDB _db;

    private final String _dbUrl;
    /** 基準となる日 */
    private final String _date;
    /** 基準となる日(Calendar型) */
    private final Calendar _calDate;
    private final boolean _isOutputMode;

    /** フォルダ */
    private final String _folder;

    /** 更新日 */
    private final String _update;

    /** 年度。(学期マスタ) */
    protected String _year;
    /** 学期。(学期マスタ) */
    protected String _semester;
    /** 学期の開始日 */
    protected String _sdate;
    /** 学期の終了日 */
    protected String _edate;

    /** 学校識別コード */
    private final String _schoolDiv;

    /** SimpleDateFormat */
    public static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /** デバッグ用の出欠届けデータCSVファイル名. */
    String _debugFileName;

    /**
     * コンストラクタ。
     * @param args
     */
    public Param(final String[] args) throws ParseException {
        if (args.length < 5) {
            System.err.println("Usage: java Main <//localhost:50000/dbname> <yyyy-mm-dd> <-out|-in> <folder> <SchoolDiv> [groupYYYYMMDD.csv]");
            throw new IllegalArgumentException("引数の数が違う");
        }

        _dbUrl = args[0];
        _date = args[1];
        final String option = args[2].toLowerCase();
        _folder = args[3];
        _schoolDiv = args[4];
        if (6 <= args.length) {
            _debugFileName = args[5];
            log.debug("デバッグ用の出欠届けデータCSVファイル名=" + _debugFileName);
        }

        if (!"-out".equals(option) && !"-in".equals(option)) {
            throw new IllegalArgumentException("第3引数は -in or -out");
        }
        _isOutputMode = "-out".equals(option) ? true : false;
        final File file = new File(_folder);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("'" + _folder + "'はディレクトリではない");
        }

        _update = SDF.format(new Date());
        log.info("更新日=" + _update + ", 学校コード=" + _schoolDiv);

        _calDate = Calendar.getInstance();
        try {
            final Date date = SDF.parse(_date);
            _calDate.setTime(date);
            
        } catch (final ParseException e) {
            log.fatal("日付文字列を解析できない: " + _date);
            throw e;
        }
        log.debug("基準となる日(Calendar型)=" + _calDate.getTime());

        log.fatal("CSVファイルの文字コード=" + encode);
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public String getDate() {
        return _date;
    }

    public Calendar getCalDate() {
        return _calDate;
    }

    public String getFolder() {
        return _folder;
    }

    public boolean isOutputMode() {
        return _isOutputMode;
    }

    public void load(final DB2UDB db) throws SQLException {
        loadSemesterMst(db);
    }

    private void loadSemesterMst(final DB2UDB db) throws SQLException {
        try {
            db.query("SELECT year, semester, sdate, edate FROM semester_mst WHERE '" + _date + "' BETWEEN sdate AND edate");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("year");
                _semester = rs.getString("semester");
                _sdate = rs.getString("sdate");
                _edate = rs.getString("edate");
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
        log.info("対象年度=" + _year + ", 学期=" + _semester + ", 開始日=" + _sdate + ", 終了日=" + _edate);
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
     * フルパスのファイル名を得る。
     * @param file ファイル名
     * @return フルパスなファイル名
     */
    public String getFullPath(final String file) {
        return getFolder() + "/" + file;  // TAKAESU: パラメータは区切り文字が付いている場合あり。and 区切り文字は '/' と '\' など。
    }

    /**
     * 学校識別コードを得る。
     * @return 学校識別コード
     */
    public String getSchoolDiv() {
        return _schoolDiv;
    }

    /**
     * 学校コードを得る。
     * @return 学校コード
     */
    public String getSchoolCode() {
        return SCHOOL_CODE_HIGH;
    }
} // Param

// eof
