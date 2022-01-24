// kanji=漢字
/*
 * $Id: MkSeito.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/21 11:34:21 - JST
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 生徒マスタデータ。
 * @author takaesu
 * @version $Id: MkSeito.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSeito extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkSeito.class);

    private final static String _FILE = "MK_SEITO.csv";

    private final static String _専攻コード = "00";

    public MkSeito(final DB2UDB db, final Param param, final String title) throws SQLException {
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
                        rs.getString("schregno"),
                        rs.getString("name"),
                        rs.getString("name_kana"),
                        cutDateDelimit(rs.getString("ent_date")),
                        rs.getString("annual"),
                        rs.getString("grade"),
                        rs.getString("hr_class"),
                        rs.getString("coursecd"),
                        rs.getString("majorcd"),
                        _専攻コード,
                        rs.getString("student_div"),
                        cutDateDelimit(param.getUpdate()),
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

        // CSVファイルに書く
        toCsv("生徒", _FILE, list);
    }

    void setHead(List list) {
        final String[] header = {
                "学校区分",
                "学籍番号",
                "氏名",
                "かな氏名",
                "入学年月日",
                "在籍年次",
                "学習拠点コード",
                "クラスコード",
                "課程コード",
                "学科コード",
                "専攻コード",
                "コースコード",
                "更新日",
        };
        list.add(header);
    }
    
    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.name,"
            + "  t1.name_kana,"
            + "  t1.ent_date,"
            + "  CASE WHEN t2.annual IS NULL THEN '' ELSE SUBSTR(t2.annual,2,1) END AS annual,"
            + "  t2.grade,"
            + "  CASE WHEN t2.hr_class IS NULL THEN '' ELSE SUBSTR(t2.hr_class,2,2) END AS hr_class,"
            + "  t2.coursecd,"
            + "  CASE WHEN t2.majorcd IS NULL THEN '' ELSE SUBSTR(t2.majorcd,2,2) END AS majorcd,"
            + "  t2.student_div"
            + " FROM"
            + "  schreg_base_mst t1 inner join schreg_regd_dat t2 on t1.schregno=t2.schregno"
            + " WHERE"
            + "  t2.year='" + _param.getYear() + "' AND"
            + "  semester='" + _param.getSemester() + "'"
            ;
        return sql;
    }
} // MkSeito

// eof
