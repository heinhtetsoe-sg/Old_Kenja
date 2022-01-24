// kanji=漢字
/*
 * $Id: MkKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
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
import org.apache.commons.lang.StringUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 科目マスタデータ。
 * @author takaesu
 * @version $Id: MkKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKamoku extends Mk {
    private final static String _FILE = "MK_KAMOKU.csv";

    public MkKamoku(final DB2UDB db, final Param param, final String title) throws SQLException {
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
                final String showOrder = rs.getString("showorder");
                final String[] fields = {
                        param.getSchoolDiv(),
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        rs.getString("classcd"),
                        rs.getString("subclasscd"),
                        rs.getString("subclassname"),
                        rs.getString("subclassabbv"),
                        StringUtils.isEmpty(showOrder) ? "0" : showOrder,
                        cutDateDelimit(param.getUpdate()),
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

        // CSVファイルに書く
        toCsv("科目", _FILE, list);
    }
    
    void setHead(final List list) {
        final String[] header = {
                "学校区分",
                "教育課程適用年度コード",
                "教科コード",
                "科目コード",
                "科目名",
                "科目略名",
                "出力順位",
                "更新日",
        };
        list.add(header);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.classcd,"
            + "  t1.curriculum_cd,"
            + "  SUBSTR(t1.subclasscd,3,4) AS subclasscd,"
            + "  t1.subclassname,"
            + "  t1.subclassabbv,"
            + "  value(t1.showorder, 0) AS showorder"
            + " FROM"
            + "  v_subclass_mst t1"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.inout_div='0'" // 自校内外区分: 0=自校内, 1=自校外
            ;
        return sql;
    }
} // MkKamoku

// eof
