// kanji=漢字
/*
 * $Id: GradeInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/30 14:55:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.miyagi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学年情報テーブル。
 * @author takaesu
 * @version $Id: GradeInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class GradeInfo {
    /*pkg*/static final Log log = LogFactory.getLog(GradeInfo.class);

    protected final Param _param;

    public GradeInfo(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        _param = param;

        log.info("★" + title);

        final List list = loadKnj(knj);
        log.debug("賢者:データ数=" + list.size());

        saveVqs(list, vqs);
    }

    private void saveVqs(final List list, final Database vqs) throws SQLException {
        int max_id_int = 1;
        ResultSet rs = null;
        try {
            final String sql = getMaxIdSql();
            vqs.query(sql);
            rs = vqs.getResultSet();
            if (rs.next()) {
                final String max_id = rs.getString("MAX_ID");
                if (!"".equals(max_id) && max_id != null) {
                    max_id_int = Integer.parseInt(max_id) + 1;
                }
            }
        } catch (SQLException e) {
            log.error("MAX_ID取得でエラー", e);
            throw e;
        } finally {
            vqs.commit();
            rs.close();
        }

        log.debug("連携:insert start id=" + max_id_int);

        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Data data = (Data) it.next();

            boolean isALLMatch = false;
            ResultSet rsM = null;
            try {
                final String sql = getALLMatchCntSql(data);
                vqs.query(sql);
                rsM = vqs.getResultSet();
                if (rsM.next()) {
                    final String match_cnt = rsM.getString("MATCH_CNT");
                    if (!"".equals(match_cnt) && match_cnt != null) {
                        if (Integer.parseInt(match_cnt) > 0) {
                            isALLMatch = true;
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("MATCH_CNT取得でエラー", e);
                throw e;
            } finally {
                vqs.commit();
                rsM.close();
            }

            if (isALLMatch) {
                // log.debug("連携:全ての項目と一致するか=" + isALLMatch);
                continue;
            }

            try {
                final String id = String.valueOf(max_id_int);
                final String insertSql = getInsertSql(data, id);
                int cnt = vqs.executeUpdate(insertSql);
                count += cnt;
                max_id_int += cnt;
            } catch (final SQLException e) {
                log.error("連携に更新でエラー");
                throw e;
            } finally {
                vqs.commit();
            }
        }
        log.debug("連携:データ数=" + count);
    }

    private String getMaxIdSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     max(id) AS MAX_ID ");
        stb.append(" FROM ");
        stb.append("     k_grade_info ");

        return stb.toString();
    }

    private String getALLMatchCntSql(final Data data) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     count(*) AS MATCH_CNT ");
        stb.append(" FROM ");
        stb.append("     k_grade_info ");
        stb.append(" WHERE ");
        stb.append("     id  IN ( ");
        stb.append("        SELECT ");
        stb.append("            max(id) AS MATCH_MAX_ID ");
        stb.append("        FROM ");
        stb.append("            k_grade_info ");
        stb.append("        WHERE ");
        stb.append("            year       =  " + _param._year + " ");
        stb.append("        AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("        AND course_cd  =  " + _param._course_cd + " ");
        stb.append("        AND grade      = '" + data._grade + "' ");
        stb.append("     ) ");
        stb.append("     AND year       =  " + _param._year + " ");
        stb.append("     AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("     AND course_cd  =  " + _param._course_cd + " ");
        stb.append("     AND grade      = '" + data._grade + "' ");
        stb.append("     AND grade_name = '" + data._grade_name + "' ");

        return stb.toString();
    }

    private String getInsertSql(final Data data, final String id) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO k_grade_info ( ");
        stb.append("     id, ");
        stb.append("     year, ");
        stb.append("     schoolcd, ");
        stb.append("     course_cd, ");
        stb.append("     grade, ");
        stb.append("     grade_name, ");
        stb.append("     updated) ");
        stb.append(" VALUES ( ");
        stb.append("      " + id + ", ");
        stb.append("      " + _param._year + ", ");
        stb.append("     '" + _param._schoolcd + "', ");
        stb.append("      " + _param._course_cd + ", ");
        stb.append("     '" + data._grade + "', ");
        stb.append("     '" + data._grade_name + "', ");
        stb.append("     current_timestamp) ");

        return stb.toString();
    }

    private List loadKnj(final Database knj) throws SQLException {
        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getDataSql();
            ps = knj.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Data data = new Data(rs.getString("GRADE"),
                                     rs.getString("GRADE_NAME"));
                rtn.add(data);
            }
        } catch (SQLException e) {
            log.error("学年情報テーブルでエラー", e);
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            knj.commit();
        }

        return rtn;
    }

    private String getDataSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.GRADE AS GRADE, ");
        stb.append("     T1.GRADE_NAME1 AS GRADE_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        //NOT NULLフィールド条件
        stb.append("     AND T1.GRADE IS NOT NULL ");
        stb.append("     AND T1.GRADE_NAME1 IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE ");

        return stb.toString();
    }

    private class Data {
        final String _grade;
        final String _grade_name;

        Data(
                final String grade,
                final String grade_name
                ) {
            _grade = grade;
            _grade_name = grade_name;
        }

        public String toString() {
            return _grade + "/" + _grade_name;
        }
    }
} // GradeInfo

// eof
