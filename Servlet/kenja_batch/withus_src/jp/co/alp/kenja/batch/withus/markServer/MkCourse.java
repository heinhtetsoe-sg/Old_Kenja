// kanji=漢字
/*
 * $Id: MkCourse.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/24 16:23:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;

import nao_package.db.DB2UDB;

/**
 * コースマスタデータ。
 * @author takaesu
 * @version $Id: MkCourse.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkCourse extends Mk {
    private final static String _FILE = "MK_COURSE.csv";

    public MkCourse(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();
        setHead(list);

        // DBから取り込む
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String studentDiv = rs.getString("student_div");  // コースコードと出力順は同じ
                final String name = rs.getString("name");   // コース名とコース略名は同じ
                final String[] fields = {
                        param.getSchoolDiv(),
                        "1",
                        "01",
                        "00",   // 専攻コード
                        studentDiv,
                        name,
                        name,
                        studentDiv,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("コースマスタの情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSVファイルに書く
        toCsv("コースマスタ", _FILE, list);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  student_div,"
            + "  name"
            + " FROM"
            + "  studentdiv_mst"
            ;
        return sql;
    }

    void setHead(List list) {
        final String[] header = {
                "学校区分",
                "課程コード",
                "学科コード",
                "専攻コード",
                "コースコード",
                "コース名",
                "コース略名",
                "出力順位",
                "更新日",
        };
        list.add(header);
    }
} // MkStaffBelonging

// eof
