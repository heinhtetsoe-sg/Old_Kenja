// kanji=漢字
/*
 * $Id: MkStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/02 0:38:13 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 講師情報テーブル。
 * @author takaesu
 * @version $Id: MkStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkStaffMst extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkStaffMst.class);

    public MkStaffMst(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        super(param, knj, vqs, title);

        final List list = loadKnj(knj);
        log.debug("賢者:データ数=" + list.size());

        saveVqs(list, vqs);
    }

    private List loadKnj(final Database knj) throws SQLException {
        final List rtn = new ArrayList();

        final String sql;
        sql = "SELECT"
            + "  staffcd,"
            + "  staffname,"
            + "  staffname_show,"
            + "  staffname_kana"
            + " FROM"
            + "  v_staff_mst"
            + " WHERE"
            + "  year=? AND"
            + "  staffcd like 'HS%'"
            ;
        log.debug("sql=" + sql);

        final List result;
        try {
            result = (List) _runner.query(knj.conn, sql, _param._year, _handler);
        } catch (final SQLException e) {
            log.error("講師情報テーブルでエラー", e);
            throw e;
        }

        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String staffcd = (String) map.get("staffcd");
            final String name = (String) map.get("staffname");
            final String nameShow = (String) map.get("staffname_show");
            final String nameKana = (String) map.get("staffname_kana");

            final String[] fields = {
                    staffcd,
                    name == null ? "" : name,
                    nameShow == null ? "" : nameShow,
                    nameKana == null ? "" : nameKana,
                    "0" // 削除区分: 0=有効, 1=無効
            };
            rtn.add(fields);
        }
        knj.commit();
        return rtn;
    }

    private void saveVqs(final List list, final Database vqs) throws SQLException {
        int count = 0;
        final String insertSql = "INSERT INTO staff_mst VALUES(?, ?, ?, ?, ?, current_timestamp)";
        final String updateSql = "UPDATE staff_mst SET"
                                    + "  staffname=?,"
                                    + "  staffname_show=?,"
                                    + "  staffname_kana=?,"
                                    + "  disabled=?,"
                                    + "  updated=current_timestamp"
                                    + " WHERE staffcd=?"
                                    ;

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] data = (String[]) it.next();
            final String[] updateParams = getUpdateParams(data);
            try {
                int cnt = _runner.update(vqs.conn, updateSql, updateParams);
                if (0 == cnt) {
                    cnt = _runner.update(vqs.conn, insertSql, data);
                }
                count += cnt;
            } catch (final SQLException e) {
                log.error("VQSに更新でエラー");
                throw e;
            }
        }
        vqs.commit();
        log.debug("VQS:データ数=" + count);
    }

    private String[] getUpdateParams(final String[] data) {
        final String[] rtn = new String[data.length];
        System.arraycopy(data, 1, rtn, 0, data.length - 1);
        rtn[rtn.length - 1] = data[0];
        return rtn;
    }
} // MkStaffMst

// eof
