// kanji=漢字
/*
 * $Id: MkKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/12 16:13:05 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 科目CSV。
 * @author takaesu
 * @version $Id: MkKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKamoku extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkKamoku.class);

    private final static String _FILE = "group0301.csv";

    public MkKamoku(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // DBから取り込む
        setGun(param, list);
        setKamoku(param, list);

        // CSVファイルに書く
        toCsv("科目", _FILE, list);
    }

    private void setGun(final Param param, final List list) throws SQLException {
        ResultSet rs = null;
        try {
            _db.query(getSqlGun());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String gunCode = param._year + param._semester + rs.getString("groupcd");
                final String[] fields = {
                        param.getSchoolDiv(),
                        param.getYear(),
                        gunCode,
                        rs.getString("groupname"),
                        rs.getString("groupabbv"),
                        null,
                        null,
                        null,
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("科目の情報取得(群)でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("科目(群)のレコード数=" + list.size());
    }

    private String getSqlGun() {
        final String sql;
        sql = "SELECT DISTINCT"
            + "  t1.groupcd,"
            + "  t2.groupname,"
            + "  t2.groupabbv"
            + " FROM"
            + "  chair_dat t1 inner join electclass_mst t2 on t1.groupcd = t2.groupcd"
            + " WHERE"
            + "  year='" + _param.getYear() + "' AND"
            + "  semester='" + _param.getSemester() + "'"
            ;
        return sql;
    }

    private void setKamoku(final Param param, final List list) throws SQLException {
        ResultSet rs = null;
        try {
            _db.query(getSqlKamoku());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String[] fields = {
                        param.getSchoolDiv(),
                        param.getYear(),
                        null,
                        null,
                        null,
                        rs.getString("subclasscd"),
                        rs.getString("subclassname"),
                        rs.getString("subclassabbv"),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("科目の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
    }

    private String getSqlKamoku() {
        final String sql;
        sql = "SELECT DISTINCT"
            + "  t2.subclasscd,"
            + "  t2.subclassname,"
            + "  t2.subclassabbv"
            + " FROM"
            + "  chair_dat t1 inner join v_subclass_mst t2 on t1.year=t2.year and t1.subclasscd=t2.subclasscd"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.semester='" + _param.getSemester() + "' AND"
            + "  t1.groupcd='0000'"
            ;
        return sql;
    }
} // MkKamoku

// eof
