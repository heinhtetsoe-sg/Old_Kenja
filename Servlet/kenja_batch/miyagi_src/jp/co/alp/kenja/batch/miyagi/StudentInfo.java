// kanji=漢字
/*
 * $Id: StudentInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 生徒情報テーブル。
 * @author takaesu
 * @version $Id: StudentInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class StudentInfo {
    /*pkg*/static final Log log = LogFactory.getLog(StudentInfo.class);

    protected final Param _param;

    public StudentInfo(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
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
        stb.append("     k_student_info ");

        return stb.toString();
    }

    private String getALLMatchCntSql(final Data data) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     count(*) AS MATCH_CNT ");
        stb.append(" FROM ");
        stb.append("     k_student_info ");
        stb.append(" WHERE ");
        stb.append("     id  IN ( ");
        stb.append("        SELECT ");
        stb.append("            max(id) AS MATCH_MAX_ID ");
        stb.append("        FROM ");
        stb.append("            k_student_info ");
        stb.append("        WHERE ");
        stb.append("            year       =  " + _param._year + " ");
        stb.append("        AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("        AND course_cd  =  " + _param._course_cd + " ");
        stb.append("        AND schregno   = '" + data._schregno + "' ");
        stb.append("     ) ");
        stb.append("     AND year       =  " + _param._year + " ");
        stb.append("     AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("     AND course_cd  =  " + _param._course_cd + " ");
        stb.append("     AND grade      = '" + data._grade + "' ");
        stb.append("     AND class_code = '" + data._class_code + "' ");
        stb.append("     AND schregno   = '" + data._schregno + "' ");
        stb.append("     AND attendno   = '" + data._attendno + "' ");
        if (null != data._student_name_sei) {
            stb.append("     AND student_name_sei = '" + data._student_name_sei + "' ");
        } else {
            stb.append("     AND student_name_sei IS NULL ");
        }
        if (null != data._student_name_mei) {
            stb.append("     AND student_name_mei = '" + data._student_name_mei + "' ");
        } else {
            stb.append("     AND student_name_mei IS NULL ");
        }
        if (null != data._student_name_sei_kana) {
            stb.append("     AND student_name_sei_kana = '" + data._student_name_sei_kana + "' ");
        } else {
            stb.append("     AND student_name_sei_kana IS NULL ");
        }
        if (null != data._student_name_mei_kana) {
            stb.append("     AND student_name_mei_kana = '" + data._student_name_mei_kana + "' ");
        } else {
            stb.append("     AND student_name_mei_kana IS NULL ");
        }

        return stb.toString();
    }

    private String getInsertSql(final Data data, final String id) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO k_student_info ( ");
        stb.append("     id, ");
        stb.append("     year, ");
        stb.append("     schoolcd, ");
        stb.append("     course_cd, ");
        stb.append("     grade, ");
        stb.append("     class_code, ");
        stb.append("     schregno, ");
        stb.append("     attendno, ");
        if (null != data._student_name_sei) {
            stb.append("     student_name_sei, ");
        }
        if (null != data._student_name_mei) {
            stb.append("     student_name_mei, ");
        }
        if (null != data._student_name_sei_kana) {
            stb.append("     student_name_sei_kana, ");
        }
        if (null != data._student_name_mei_kana) {
            stb.append("     student_name_mei_kana, ");
        }
        stb.append("     updated) ");
        stb.append(" VALUES ( ");
        stb.append("      " + id + ", ");
        stb.append("      " + _param._year + ", ");
        stb.append("     '" + _param._schoolcd + "', ");
        stb.append("      " + _param._course_cd + ", ");
        stb.append("     '" + data._grade + "', ");
        stb.append("     '" + data._class_code + "', ");
        stb.append("     '" + data._schregno + "', ");
        stb.append("     '" + data._attendno + "', ");
        if (null != data._student_name_sei) {
            stb.append("     '" + data._student_name_sei + "', ");
        }
        if (null != data._student_name_mei) {
            stb.append("     '" + data._student_name_mei + "', ");
        }
        if (null != data._student_name_sei_kana) {
            stb.append("     '" + data._student_name_sei_kana + "', ");
        }
        if (null != data._student_name_mei_kana) {
            stb.append("     '" + data._student_name_mei_kana + "', ");
        }
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
                                     rs.getString("CLASS_CODE"),
                                     rs.getString("SCHREGNO"),
                                     rs.getString("ATTENDNO"),
                                     rs.getString("NAME"),
                                     rs.getString("NAME_KANA"));
                rtn.add(data);
            }
        } catch (SQLException e) {
            log.error("生徒情報テーブルでエラー", e);
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
        stb.append("     T1.HR_CLASS AS CLASS_CODE, ");
        stb.append("     T1.SCHREGNO AS SCHREGNO, ");
        stb.append("     T1.ATTENDNO AS ATTENDNO, ");
        stb.append("     L1.NAME AS NAME, ");
        stb.append("     L1.NAME_KANA AS NAME_KANA ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        //NOT NULLフィールド条件
        stb.append("     AND T1.GRADE IS NOT NULL ");
        stb.append("     AND T1.HR_CLASS IS NOT NULL ");
        stb.append("     AND T1.SCHREGNO IS NOT NULL ");
        stb.append("     AND T1.ATTENDNO IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }

    private class Data {
        final String _grade;
        final String _class_code;
        final String _schregno;
        final String _attendno;

        final String _student_name_sei;
        final String _student_name_mei;
        final String _student_name_sei_kana;
        final String _student_name_mei_kana;
        final String _name;
        final String _name_kana;

        Data(
                final String grade,
                final String class_code,
                final String schregno,
                final String attendno,
                final String name,
                final String name_kana
                ) {
            _grade = grade;
            _class_code = class_code;
            _schregno = schregno;
            _attendno = attendno;

            final String[] seimeiName = getSeiMei(name);
            _student_name_sei = seimeiName[0];
            _student_name_mei = seimeiName[1];
            final String[] seimeiKana = getSeiMei(name_kana);
            _student_name_sei_kana = seimeiKana[0];
            _student_name_mei_kana = seimeiKana[1];
            _name = name;
            _name_kana = name_kana;
        }

        private String[] getSeiMei(final String name) {
            final String[] split = StringUtils.split(StringUtils.replace(name, " ", "　"), "　");
            // log.debug("split = (" + split + ")");
            String sei = null;
            String mei = null;
            if (split != null) {
                sei = (split.length > 0) ? split[0] : null;
                mei = (split.length > 1) ? split[1] : null;
                int i = 2;
                while (i < split.length) {
                    mei += split[i];
                    i++;
                }
                // log.debug("length = (" + split.length + ")");
            }
            final String[] seimei = new String[2];
            seimei[0] = sei;
            seimei[1] = mei;
            // log.debug("sei = (" + seimei[0] + "), mei = (" + seimei[1] +
            // ")");
            return seimei;
        }

        public String toString() {
            return _grade + "/" + _class_code + "/" + _schregno + "/" + _attendno + "/" + _name;
        }
    }
} // StudentInfo

// eof
