// kanji=漢字
/*
 * $Id: Semester.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2004/06/04 21:03:10 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学期。
 * @version $Id: Semester.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Semester {
    /** log */
    private static final Log log = LogFactory.getLog(Semester.class);
    private static final Class MYCLASS = Semester.class;

    private final String _semes;
    private final String _year;
    private final String _name;
    private final KenjaDateImpl _sDate;
    private final KenjaDateImpl _eDate;

    /*
     * コンストラクタ。
     */
    private Semester(
            final String year,
            final String semes,
            final String name,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        _year = year;
        _semes = semes;
        _name = name;
        _sDate = sDate;
        _eDate = eDate;
    }

    /**
     * 年を得る。
     * @return 年
     */
    public String getYear() {
        return _year;
    }

    /**
     * 学期を得る。
     * @return 学期
     */
    public String getSemesterString() {
        return _semes;
    }

    /**
     * 学期名を得る。
     * @return 学期名
     */
    public String getName() {
        return _name;
    }

    /**
     * 学期開始日付を得る。
     * @return 学期開始日付
     */
    public KenjaDateImpl getSDate() {
        return _sDate;
    }

    /**
     * 学期終了日付を得る。
     * @return 学期終了日付
     */
    public KenjaDateImpl getEDate() {
        return _eDate;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _semes + ":" + _name + "[" + _sDate + "," + _eDate + "]";
    }

    /**
     * 学期
     * @param year 年
     * @param semes 学期コード
     * @param name 学期名
     * @param sDate 学期開始日付
     * @param eDate 学期終了日付
     * @return 学期
     */
    public static Semester create(
            final String year,
            final String semes,
            final String name,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        if (null == year)  { throw new IllegalArgumentException("引数が不正(year)"); }
        if (null == semes) { throw new IllegalArgumentException("引数が不正(code)"); }
        if (null == name)  { throw new IllegalArgumentException("引数が不正(name)"); }
        if (null == sDate) { throw new IllegalArgumentException("引数が不正(sDate)"); }
        if (null == eDate) { throw new IllegalArgumentException("引数が不正(eDate)"); }

        return new Semester(year, semes, name, sDate, eDate);
    }

    /**
     * 指定されたdateの学期を返す
     * @param db DB
     * @param date 指定された年月日
     * @return dateの学期
     * @throws SQLException SQL例外
     */
    public static Semester load(final DB2UDB db, final String date) throws SQLException {
        Semester semester = null;
        boolean found = false;
        db.query("SELECT year, semester, semestername, sdate, edate FROM semester_mst WHERE '" + date + "' BETWEEN sdate AND edate AND semester <> '9' ");
        final ResultSet rs = db.getResultSet();
        if (rs.next()) {
            found = true;
            final String year = rs.getString("year");
            final String semes = rs.getString("semester");
            final String name = rs.getString("semestername");
            final Date sqlSdate = Date.valueOf(rs.getString("sdate"));
            final Date sqlEdate = Date.valueOf(rs.getString("edate"));
            final KenjaDateImpl sdate = KenjaDateImpl.getInstance(sqlSdate);
            final KenjaDateImpl edate = KenjaDateImpl.getInstance(sqlEdate);

            semester = new Semester(year, semes, name, sdate, edate);
        }
        if (!found) {
            throw new SQLException("学期データがセットされていません : " + date);
        }
        
        log.info("対象年度=" + semester.getYear() + ", 学期=" + semester.getSemesterString() + ", 開始日=" + semester.getSDate() + ", 終了日=" + semester.getEDate());
        db.commit();
        rs.close();
        return semester;
    }

} // Semester

// eof
