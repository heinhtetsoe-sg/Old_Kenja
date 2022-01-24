// kanji=漢字
/*
 * $Id: MkKinmusakiGakushuKyoten.java 56574 2017-10-22 11:21:06Z maeshiro $
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
import org.apache.commons.lang.StringUtils;

import nao_package.db.DB2UDB;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 勤務先学習拠点マスタデータ。
 * @author takaesu
 * @version $Id: MkKinmusakiGakushuKyoten.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKinmusakiGakushuKyoten extends Mk {
    private final static String _FILE = "MK_KINMUSAKI_GAKUSHU_KYOTEN.csv";

    public MkKinmusakiGakushuKyoten(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // ヘッダを設定
        setHead(list);
        
        final String inaugurationStart = _param.getYear() + "0401"; // その年度の4月1日

        final int nextYear = Integer.parseInt(_param.getYear()) + 1;
        final String inaugurationEnd = String.valueOf(nextYear) + "0331";   // 翌年度の3月31日

        // DBから取り込む
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String belongingDiv = rs.getString("belonging_div");
                final String[] fields = {
                        param.getSchoolDiv(),
                        convStaffCd(rs.getString("staffcd")),
                        StringUtils.isEmpty(belongingDiv) ? "001" : belongingDiv,
                        inaugurationStart,
                        inaugurationEnd,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("勤務先学習拠点の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSVファイルに書く
        toCsv("勤務先学習拠点", _FILE, list);
    }

    void setHead(final List list) {
        final String[] header = {
                "学校区分",
                "職員コード",
                "学習拠点コード",
                "就任年月日",
                "退任年月日",
                "更新日",
        };
        list.add(header);
    }
    
    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  staffcd,"
            + "  belonging_div"
            + " FROM"
            + "  v_staff_mst"
            + " WHERE"
            + "  year='" + _param.getYear() + "'"
            + " ORDER BY belonging_div, staffcd"
            ;
        return sql;
    }
} // MkStaffBelonging

// eof
