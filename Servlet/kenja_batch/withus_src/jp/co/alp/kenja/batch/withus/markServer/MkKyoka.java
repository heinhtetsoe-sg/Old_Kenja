// kanji=漢字
/*
 * $Id: MkKyoka.java 56574 2017-10-22 11:21:06Z maeshiro $
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

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 教科マスタデータ。
 * @author takaesu
 * @version $Id: MkKyoka.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKyoka extends Mk {
    private final static String _FILE = "MK_KYOKA.csv";

    public MkKyoka(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // ヘッダを設定
        setHead(list);

        // DBから取り込む
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String[] fields = {
                        param.getSchoolDiv(),
                        Curriculum.getCurriculumFirstYear(_param.getYear()),
                        rs.getString("classcd"),
                        rs.getString("classname"),
                        rs.getString("classabbv"),
                        rs.getString("showorder"),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("教科の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSVファイルに書く
        toCsv("教科", _FILE, list);
    }

    void setHead(final List list) {
        final String[] header = {
                "学校区分",
                "教育課程適用年度コード",
                "教科コード",
                "教科名",
                "教科略名",
                "出力順位",
                "更新日",
        };
        list.add(header);
    }
    
    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.classcd,"
            + "  t1.classname,"
            + "  t1.classabbv,"
            + "  value(t1.showorder, 0) AS showorder"
            + " FROM"
            + "  v_class_mst t1"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.inout_div='0'" // 自校内外区分: 0=自校内, 1=自校外
           ;
        return sql;
    }
} // MkKyoka

// eof
