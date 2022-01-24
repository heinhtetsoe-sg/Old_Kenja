// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/30 14:23:11 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.miyagi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * パラメータ。
 * 
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /* pkg */static final Log log = LogFactory.getLog(Param.class);

    final String _knjUrl;

    final String _knjUser;

    final String _knjPass;

    final String _vqsUrl;

    final String _vqsUser;

    final String _vqsPass;

    final String _iinkaiUrl;

    final String _iinkaiUser;

    final String _iinkaiPass;

    /** 基準日. */
    final String _date;

    /** 更新日時. */
    private final Date _now = new Date();

    /** 年度。(学期マスタ) */
    protected String _year;

    /** 学期。(学期マスタ) */
    protected String _semester;

    /** 学校コード。(学校詳細データ００８) */
    protected String _schoolcd;

    /** 課程コード。(学校詳細データ００８) */
    protected String _course_cd;

    public Param(final String[] args) {
        if (10 != args.length) {
            System.err.println("Usage: java Main <//db2Host:50000/db2DB> <db2 user> <db2 passwd> <//postgreHost/postgreDB> <postgre user> <postgre passwd> <//iinkaiHost:50000/iinkaiDB> <iinkai user> <iinkai passwd> <yyyy-mm-dd>");
            throw new IllegalArgumentException("引数の数が違う");
        }
        _knjUrl = args[0];
        _knjUser = args[1];
        _knjPass = args[2];
        _vqsUrl = args[3];
        _vqsUser = args[4];
        _vqsPass = args[5];
        _iinkaiUrl = args[6];
        _iinkaiUser = args[7];
        _iinkaiPass = args[8];
        _date = args[9];
    }

    public void load(final Database db) throws SQLException {
        loadSemesterMst(db);
        loadSchoolDetailDat(db);
    }

    private void loadSemesterMst(final Database db) throws SQLException {
        try {
            db.query("SELECT year, semester FROM semester_mst WHERE '" + _date + "' BETWEEN sdate AND edate AND semester<>'9'");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("year");
                _semester = rs.getString("semester");
            }
            db.commit();
            rs.close();

            // 学期範囲外の場合、直近の学期をセット
            if (_semester == null) {
                final String[] split = StringUtils.split(_date, "-");
                final String year = Integer.parseInt(split[1]) < 4 ? String.valueOf(Integer.parseInt(split[0]) - 1) : split[0];
                db.query("SELECT year, semester, sdate FROM semester_mst WHERE year = '" + year + "' AND semester<>'9' order by semester");
                ResultSet rs2 = db.getResultSet();
                while (rs2.next()) {
                    _year = rs2.getString("year");
                    _semester = rs2.getString("semester");
                    if (_date.compareTo(rs2.getString("sdate")) < 0) {
                        break;
                    }
                }
                db.commit();
                rs2.close();
                log.debug(_date + "が学期範囲外のため、直近の学期をセット。年度=" + _year + ", 学期=" + _semester);
            }
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

    private void loadSchoolDetailDat(final Database db) throws SQLException {
        try {
            final String sql = getDataSql();
            db.query(sql);
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _schoolcd = rs.getString("SCHOOLCD");
                _course_cd = rs.getString("COURSE_CD");
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            log.fatal("学校コード、課程コードが得られない! SCHOOL_DETAIL_DATから取得出来ない");
            throw e;
        }
        try {
            Integer.parseInt(_schoolcd);
            Integer.parseInt(_course_cd);
        } catch (final NumberFormatException e) {
            log.fatal("学校コード、課程コードが正しくない(SCHOOL_DETAIL_DATから取得出来ない)⇒学校コード=" + _schoolcd + ", 課程コード=" + _course_cd);
            throw e;
        }
    }

    private String getDataSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.SCHOOL_REMARK1 AS SCHOOLCD, ");
        stb.append("     T1.SCHOOL_REMARK3 AS COURSE_CD ");
        stb.append(" FROM ");
        stb.append("     SCHOOL_DETAIL_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR        = '" + _year + "' ");
        stb.append("     AND T1.SCHOOL_SEQ  = '008' ");
        // NOT NULLフィールド条件
        // stb.append("     AND T1.SCHOOL_REMARK1 IS NOT NULL ");
        // stb.append("     AND T1.SCHOOL_REMARK3 IS NOT NULL ");

        return stb.toString();
    }

    /**
     * 年度を得る。 (学期マスタ)
     * 
     * @return 年度
     */
    public String getYear() {
        return _year;
    }

    /**
     * 学期を得る。 (学期マスタ)
     * 
     * @return 学期
     */
    public String getSemester() {
        return _semester;
    }

    /**
     * 更新日時を得る。
     * 
     * @return 更新日時
     */
    public Date getUpdate() {
        return _now;
    }

    /** 各テーブル共通フィールドがあるか */
    public boolean isNotNullCommonField() {
        return _schoolcd != null && _course_cd != null;
    }

    public String toString() {
        final String update = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(_now);
        return "基準日=" + _date + ", 年度=" + _year + ", 学期=" + _semester + ", 実行日時=" + update + ", 学校コード=" + _schoolcd + ", 課程コード=" + _course_cd;
    }
} // Param

// eof
