// kanji=漢字
/*
 * $Id: MkClass.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * クラスCSV。
 * @author takaesu
 * @version $Id: MkClass.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkClass extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkClass.class);

    private final static String _FILE = "group0401.csv";

    public MkClass(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // DBから取り込む
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String tannin = getStaff(rs.getString("tr_cd1"), rs.getString("tr_cd2"), rs.getString("tr_cd3"));
                final String fukutannin = getStaff(rs.getString("subtr_cd1"), rs.getString("subtr_cd2"), rs.getString("subtr_cd3"));

                final String grade = rs.getString("grade");
                final String hrClass = rs.getString("hr_class");

                final String[] fields = {
                        param.getSchoolDiv(),
                        param.getYear(),
                        rs.getString("hr_name"),
                        rs.getString("hr_nameabbv"),
                        grade + hrClass,
                        convStaffCd(tannin),
                        convStaffCd(fukutannin),
                        Param.SCHOOL_CODE_HIGH,
                        convGrade(grade),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("クラスの情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("クラスのレコード数=" + list.size());

        // CSVファイルに書く
        toCsv("クラス", _FILE, list);
    }

    private static String convGrade(final String grade) {
        final int i = Integer.parseInt(grade);
        if (i < 1 || i > 9) {
            log.warn("学年コードが1～9の範囲外。");
        }
        return String.valueOf(i);
    }

    private String getStaff(final String s1, final String s2, final String s3) {
        if (null != s1) {
            return s1;
        }
        return (null != s2) ? s2 : s3;
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  hr_name,"
            + "  hr_nameabbv,"
            + "  grade,"
            + "  hr_class,"
            + "  tr_cd1,"
            + "  tr_cd2,"
            + "  tr_cd3,"
            + "  subtr_cd1,"
            + "  subtr_cd2,"
            + "  subtr_cd3"
            + " FROM"
            + "  schreg_regd_hdat"
            + " WHERE"
            + "  year='" + _param.getYear() + "' AND"
            + "  semester='" + _param.getSemester() + "'"
            ;
        return sql;
    }
} // MkClass

// eof
