// kanji=漢字
/*
 * $Id: MkSeito.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/04 11:56:45 - JST
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
 * 生徒CSV。
 * @author takaesu
 * @version $Id: MkSeito.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSeito extends Mk {

    /*pkg*/static final Log log = LogFactory.getLog(MkSeito.class);

    private final static String _FILE = "group0201.csv";

    public MkSeito(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // DBから取り込む
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String grade = rs.getString("grade");
                final String hrClass = rs.getString("hr_class");

                final String[] fields = {
                        param.getSchoolDiv(),
                        param.getYear(),
                        rs.getString("name_show"),
                        rs.getString("name_kana"),
                        rs.getString("schregno"),
                        rs.getString("attendno"),
                        grade + hrClass,
                        param.getSchoolCode(),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("生徒の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("生徒のレコード数=" + list.size());

        // CSVファイルに書く
        toCsv("生徒", _FILE, list);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.name_show,"
            + "  t1.name_kana,"
            + "  t1.schregno,"
            + "  t2.attendno,"
            + "  t2.grade,"
            + "  t2.hr_class,"
            + "  t3.hr_name"
            + " FROM"
            + "  schreg_base_mst t1 inner join schreg_regd_dat t2 on t1.schregno=t2.schregno,"
            + "  schreg_regd_hdat t3"
            + " WHERE"
            + "  t2.year=t3.year AND"
            + "  t2.semester=t3.semester AND"
            + "  t2.grade=t3.grade AND"
            + "  t2.hr_class=t3.hr_class AND"
            + "  t2.year='" + _param.getYear() + "' AND"
            + "  t2.semester='" + _param.getSemester() + "'"
            ;
        return sql;
    }
} // MkKyousyokuin

// eof
