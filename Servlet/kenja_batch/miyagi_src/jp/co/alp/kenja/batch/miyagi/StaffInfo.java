// kanji=漢字
/*
 * $Id: StaffInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * 職員情報テーブル。
 * 
 * @author takaesu
 * @version $Id: StaffInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class StaffInfo {
    /* pkg */static final Log log = LogFactory.getLog(StaffInfo.class);

    protected final Param _param;

    public StaffInfo(final Param param, final Database knj, final Database vqs, final Database iinkai, final String title) throws SQLException {
        _param = param;

        log.info("★" + title);

        final List list = loadKnj(knj);
        log.debug("賢者:データ数=" + list.size());

        loadIinkai(list, iinkai);

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

            boolean isIdouExists = false;
            ResultSet rsIdou = null;
            try {
                final String sql = getIdouExistsCntSql(data);
                vqs.query(sql);
                rsIdou = vqs.getResultSet();
                if (rsIdou.next()) {
                    final String exists_cnt = rsIdou.getString("EXISTS_CNT");
                    if (!"".equals(exists_cnt) && exists_cnt != null) {
                        if (Integer.parseInt(exists_cnt) > 0) {
                            isIdouExists = true;
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("EXISTS_CNT取得でエラー", e);
                throw e;
            } finally {
                vqs.commit();
                rsIdou.close();
            }

            if (isIdouExists) {
                // log.debug("連携:異動情報が既に登録されているか=" + isIdouExists);
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

    private String getInsertSql(final Data data, final String id) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO k_staff_info ( ");
        stb.append("     id, ");
        stb.append("     year, ");
        stb.append("     schoolcd, ");
        stb.append("     course_cd, ");
        stb.append("     staffcd, ");
        stb.append("     loginid, ");
        stb.append("     password, ");
        if (null != data._staff_name_sei) {
            stb.append("     staff_name_sei, ");
        }
        if (null != data._staff_name_mei) {
            stb.append("     staff_name_mei, ");
        }
        if (null != data._staff_name_sei_kana) {
            stb.append("     staff_name_sei_kana, ");
        }
        if (null != data._staff_name_mei_kana) {
            stb.append("     staff_name_mei_kana, ");
        }
        if (null != data._staffsex) {
            stb.append("     staffsex, ");
        }
        if (null != data._staff_mail) {
            stb.append("     staff_mail, ");
        }
        if (null != data._jobcd) {
            stb.append("     jobcd, ");
        }
        if (null != data._sectioncd) {
            stb.append("     sectioncd, ");
        }
        if (null != data._dutysharecd) {
            stb.append("     dutysharecd, ");
        }
        if (null != data._main_work_flag) {
            stb.append("     main_work_flag, ");
        }
        if (null != data._transfer_schoolcd) {
            stb.append("     transfer_schoolcd, ");
        }
        if (null != data._transfer_course_cd) {
            stb.append("     transfer_course_cd, ");
        }
        if (null != data._changes_info) {
            stb.append("     changes_info, ");
        }
        if (null != data._assignment_date) {
            stb.append("     assignment_date, ");
        }
        if (null != data._transfer_date) {
            stb.append("     transfer_date, ");
        }
        if (null != data._retired_date) {
            stb.append("     retired_date, ");
        }
        stb.append("     updated) ");
        stb.append(" VALUES ( ");
        stb.append("      " + id + ", ");
        stb.append("      " + _param._year + ", ");
        stb.append("     '" + _param._schoolcd + "', ");
        stb.append("      " + _param._course_cd + ", ");
        stb.append("     '" + data._staffcd + "', ");
        stb.append("     '" + data._loginid + "', ");
        stb.append("     '" + data._password + "', ");
        if (null != data._staff_name_sei) {
            stb.append("     '" + data._staff_name_sei + "', ");
        }
        if (null != data._staff_name_mei) {
            stb.append("     '" + data._staff_name_mei + "', ");
        }
        if (null != data._staff_name_sei_kana) {
            stb.append("     '" + data._staff_name_sei_kana + "', ");
        }
        if (null != data._staff_name_mei_kana) {
            stb.append("     '" + data._staff_name_mei_kana + "', ");
        }
        if (null != data._staffsex) {
            stb.append("     '" + data._staffsex + "', ");
        }
        if (null != data._staff_mail) {
            stb.append("     '" + data._staff_mail + "', ");
        }
        if (null != data._jobcd) {
            stb.append("     '" + data._jobcd + "', ");
        }
        if (null != data._sectioncd) {
            stb.append("     '" + data._sectioncd + "', ");
        }
        if (null != data._dutysharecd) {
            stb.append("     '" + data._dutysharecd + "', ");
        }
        if (null != data._main_work_flag) {
            stb.append("      " + data._main_work_flag + ", ");
        }
        if (null != data._transfer_schoolcd) {
            stb.append("     '" + data._transfer_schoolcd + "', ");
        }
        if (null != data._transfer_course_cd) {
            stb.append("     '" + data._transfer_course_cd + "', ");
        }
        if (null != data._changes_info) {
            stb.append("      " + data._changes_info + ", ");
        }
        if (null != data._assignment_date) {
            stb.append("     '" + data._assignment_date + "', ");
        }
        if (null != data._transfer_date) {
            stb.append("     '" + data._transfer_date + "', ");
        }
        if (null != data._retired_date) {
            stb.append("     '" + data._retired_date + "', ");
        }
        stb.append("     current_timestamp) ");

        return stb.toString();
    }

    private String getUpdateSql(final Data data) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" UPDATE k_staff_info SET ");
        stb.append("     loginid            = '" + data._loginid + "', ");
        stb.append("     password           = '" + data._password + "', ");
        stb.append("     staff_name_sei      = '" + data._staff_name_sei + "', ");
        stb.append("     staff_name_mei      = '" + data._staff_name_mei + "', ");
        stb.append("     staff_name_sei_kana = '" + data._staff_name_sei_kana + "', ");
        stb.append("     staff_name_mei_kana = '" + data._staff_name_mei_kana + "', ");
        stb.append("     staffsex           = '" + data._staffsex + "', ");
        stb.append("     staff_mail         = '" + data._staff_mail + "', ");
        stb.append("     jobcd              = '" + data._jobcd + "', ");
        stb.append("     sectioncd          = '" + data._sectioncd + "', ");
        stb.append("     dutysharecd        = '" + data._dutysharecd + "', ");
        stb.append("     main_work_flag     =  " + data._main_work_flag + ", ");
        stb.append("     transfer_schoolcd  = '" + data._transfer_schoolcd + "', ");
        stb.append("     transfer_course_cd = '" + data._transfer_course_cd + "', ");
        stb.append("     changes_info       =  " + data._changes_info + ", ");
        stb.append("     assignment_date    = '" + data._assignment_date + "', ");
        stb.append("     transfer_date      = '" + data._transfer_date + "', ");
        stb.append("     retired_date       = '" + data._retired_date + "', ");
        stb.append("     updated = current_timestamp ");
        stb.append(" WHERE ");
        stb.append("         year       =  " + _param._year + " ");
        stb.append("     AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("     AND course_cd  =  " + _param._course_cd + " ");
        stb.append("     AND staffcd    = '" + data._staffcd + "' ");

        return stb.toString();
    }

    private String getALLMatchCntSql(final Data data) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     count(*) AS MATCH_CNT ");
        stb.append(" FROM ");
        stb.append("     k_staff_info ");
        stb.append(" WHERE ");
        stb.append("     id  IN ( ");
        stb.append("        SELECT ");
        stb.append("            max(id) AS MATCH_MAX_ID ");
        stb.append("        FROM ");
        stb.append("            k_staff_info ");
        stb.append("        WHERE ");
        stb.append("            year       =  " + _param._year + " ");
        stb.append("        AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("        AND course_cd  =  " + _param._course_cd + " ");
        stb.append("        AND staffcd    = '" + data._staffcd + "' ");
        stb.append("     ) ");
        stb.append("     AND year       =  " + _param._year + " ");
        stb.append("     AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("     AND course_cd  =  " + _param._course_cd + " ");
        stb.append("     AND staffcd    = '" + data._staffcd + "' ");
        stb.append("     AND loginid    = '" + data._loginid + "' ");
        stb.append("     AND password   = '" + data._password + "' ");

        if (null != data._staff_name_sei) {
            stb.append("     AND staff_name_sei      = '" + data._staff_name_sei + "' ");
        } else {
            stb.append("     AND staff_name_sei IS NULL ");
        }
        if (null != data._staff_name_mei) {
            stb.append("     AND staff_name_mei      = '" + data._staff_name_mei + "' ");
        } else {
            stb.append("     AND staff_name_mei IS NULL ");
        }
        if (null != data._staff_name_sei_kana) {
            stb.append("     AND staff_name_sei_kana = '" + data._staff_name_sei_kana + "' ");
        } else {
            stb.append("     AND staff_name_sei_kana IS NULL ");
        }
        if (null != data._staff_name_mei_kana) {
            stb.append("     AND staff_name_mei_kana = '" + data._staff_name_mei_kana + "' ");
        } else {
            stb.append("     AND staff_name_mei_kana IS NULL ");
        }
        if (null != data._staffsex) {
            stb.append("     AND staffsex           = '" + data._staffsex + "' ");
        } else {
            stb.append("     AND staffsex IS NULL ");
        }
        if (null != data._staff_mail) {
            stb.append("     AND staff_mail         = '" + data._staff_mail + "' ");
        } else {
            stb.append("     AND staff_mail IS NULL ");
        }
        if (null != data._jobcd) {
            stb.append("     AND jobcd              = '" + data._jobcd + "' ");
        } else {
            stb.append("     AND jobcd IS NULL ");
        }
        if (null != data._sectioncd) {
            stb.append("     AND sectioncd          = '" + data._sectioncd + "' ");
        } else {
            stb.append("     AND sectioncd IS NULL ");
        }
        if (null != data._dutysharecd) {
            stb.append("     AND dutysharecd        = '" + data._dutysharecd + "' ");
        } else {
            stb.append("     AND dutysharecd IS NULL ");
        }
        if (null != data._main_work_flag) {
            stb.append("     AND main_work_flag     =  " + data._main_work_flag + " ");
        } else {
            stb.append("     AND main_work_flag IS NULL ");
        }
        if (null != data._transfer_schoolcd) {
            stb.append("     AND transfer_schoolcd  = '" + data._transfer_schoolcd + "' ");
        } else {
            stb.append("     AND transfer_schoolcd IS NULL ");
        }
        if (null != data._transfer_course_cd) {
            stb.append("     AND transfer_course_cd = '" + data._transfer_course_cd + "' ");
        } else {
            stb.append("     AND transfer_course_cd IS NULL ");
        }
        if (null != data._changes_info) {
            stb.append("     AND changes_info       =  " + data._changes_info + " ");
        } else {
            stb.append("     AND changes_info IS NULL ");
        }
        if (null != data._assignment_date) {
            stb.append("     AND assignment_date    = '" + data._assignment_date + "' ");
        } else {
            stb.append("     AND assignment_date IS NULL ");
        }
        if (null != data._transfer_date) {
            stb.append("     AND transfer_date      = '" + data._transfer_date + "' ");
        } else {
            stb.append("     AND transfer_date IS NULL ");
        }
        if (null != data._retired_date) {
            stb.append("     AND retired_date       = '" + data._retired_date + "' ");
        } else {
            stb.append("     AND retired_date IS NULL ");
        }

        return stb.toString();
    }

    private String getIdouExistsCntSql(final Data data) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     count(*) AS EXISTS_CNT ");
        stb.append(" FROM ");
        stb.append("     k_staff_info ");
        stb.append(" WHERE ");
        stb.append("     id  IN ( ");
        stb.append("        SELECT ");
        stb.append("            max(id) AS MATCH_MAX_ID ");
        stb.append("        FROM ");
        stb.append("            k_staff_info ");
        stb.append("        WHERE ");
        stb.append("            year       =  " + _param._year + " ");
        stb.append("        AND schoolcd   = '" + _param._schoolcd + "' ");
        stb.append("        AND course_cd  =  " + _param._course_cd + " ");
        stb.append("        AND staffcd    = '" + data._staffcd + "' ");
        stb.append("     ) ");

        stb.append("     AND transfer_schoolcd  = '" + _param._schoolcd + "' ");
        stb.append("     AND transfer_course_cd = '" + _param._course_cd + "' ");
        stb.append("     AND changes_info       IN (2,3) "); //2:転出、3:退職

        return stb.toString();
    }

    private String getMaxIdSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     max(id) AS MAX_ID ");
        stb.append(" FROM ");
        stb.append("     k_staff_info ");

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
                Data data = new Data(rs.getString("STAFFCD"), rs.getString("LOGINID"), rs.getString("PASSWORD"), rs.getString("STAFFNAME"), rs.getString("STAFFNAME_KANA"), rs.getString("STAFFSEX"),
                        rs.getString("STAFF_MAIL"), rs.getString("JOBCD"), rs.getString("SECTIONCD"), rs.getString("DUTYSHARECD"), rs.getString("MAIN_WORK_FLAG"), rs.getString("TRANSFER_SCHOOLCD"),
                        rs.getString("TRANSFER_COURSE_CD"), rs.getString("CHANGES_INFO"), rs.getString("ASSIGNMENT_DATE"), rs.getString("TRANSFER_DATE"), rs.getString("RETIRED_DATE"));
                rtn.add(data);
            }
        } catch (SQLException e) {
            log.error("職員情報テーブルでエラー", e);
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
        stb.append("     T1.STAFFCD, ");
        stb.append("     L1.USERID AS LOGINID, ");
        stb.append("     L1.PASSWD AS PASSWORD, ");
        stb.append("     T1.STAFFNAME, ");
        stb.append("     T1.STAFFNAME_KANA, ");
        stb.append("     T1.STAFFSEX, "); // TODO:コード、名称、略称のどれをセットするのか。名称マスタ「Z002」
        stb.append("     T1.STAFFE_MAIL AS STAFF_MAIL, ");
        stb.append("     value(T1.JOBCD, '0006') AS JOBCD, "); // 初期値「0006:教諭」
        stb.append("     T1.SECTIONCD, ");
        stb.append("     T1.DUTYSHARECD, ");
        stb.append("     0 AS MAIN_WORK_FLAG, ");
        stb.append("     cast(null as varchar(7)) AS TRANSFER_SCHOOLCD, ");
        stb.append("     cast(null as varchar(1)) AS TRANSFER_COURSE_CD, ");
        stb.append("     0 AS CHANGES_INFO, "); // NOT NULLフィールド
        stb.append("     cast(null as date) AS ASSIGNMENT_DATE, ");
        stb.append("     cast(null as date) AS TRANSFER_DATE, ");
        stb.append("     cast(null as date) AS RETIRED_DATE ");
        stb.append(" FROM ");
        stb.append("     V_STAFF_MST T1 ");
        stb.append("     LEFT JOIN USER_MST L1 ON L1.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        // ０で始まる職員は作成しない
        stb.append("     AND T1.STAFFCD NOT LIKE '0%' ");
        // NOT NULLフィールド条件
        stb.append("     AND L1.USERID IS NOT NULL ");
        stb.append("     AND L1.PASSWD IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.STAFFCD ");

        return stb.toString();
    }

    private class Data {
        final String _staffcd;

        final String _loginid;

        final String _password;

        final String _staff_name_sei;

        final String _staff_name_mei;

        final String _staff_name_sei_kana;

        final String _staff_name_mei_kana;

        final String _staffname;

        final String _staffname_kana;

        final String _staffsex;

        final String _staff_mail;

        final String _jobcd;

        final String _sectioncd;

        final String _dutysharecd;

        final String _main_work_flag;

        String _transfer_schoolcd;

        String _transfer_course_cd;

        String _changes_info;

        String _assignment_date;

        String _transfer_date;

        String _retired_date;

        Data(
                final String staffcd,
                final String loginid,
                final String password,
                final String staffname,
                final String staffname_kana,
                final String staffsex,
                final String staff_mail,
                final String jobcd,
                final String sectioncd,
                final String dutysharecd,
                final String main_work_flag,
                final String transfer_schoolcd,
                final String transfer_course_cd,
                final String changes_info,
                final String assignment_date,
                final String transfer_date,
                final String retired_date) {
            _staffcd = staffcd;
            _loginid = loginid;
            _password = password;
            final String[] seimeiName = getSeiMei(staffname);
            _staff_name_sei = seimeiName[0];
            _staff_name_mei = seimeiName[1];
            final String[] seimeiKana = getSeiMei(staffname_kana);
            _staff_name_sei_kana = seimeiKana[0];
            _staff_name_mei_kana = seimeiKana[1];
            _staffname = staffname;
            _staffname_kana = staffname_kana;
            _staffsex = staffsex;
            _staff_mail = staff_mail;
            _jobcd = jobcd;
            _sectioncd = sectioncd;
            _dutysharecd = dutysharecd;
            _main_work_flag = main_work_flag;
            _transfer_schoolcd = transfer_schoolcd;
            _transfer_course_cd = transfer_course_cd;
            _changes_info = changes_info;
            _assignment_date = assignment_date;
            _transfer_date = transfer_date;
            _retired_date = retired_date;
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
            return _staffcd + "/" + _staffname;
        }
    }

    private void loadIinkai(final List list, final Database iinkai) throws SQLException {
/***
        //「処理日の前日」を取得
        String idou_date = null;
        PreparedStatement psDate = null;
        ResultSet rsDate = null;
        try {
            final String sqlDate = "values(date('" + _param._date + "') - 1 day)";
            psDate = iinkai.prepareStatement(sqlDate);
            rsDate = psDate.executeQuery();
            if (rsDate.next()) {
                idou_date = rsDate.getString(1);
            }
        } catch (SQLException e) {
            log.error("処理日の前日でエラー", e);
            throw e;
        } finally {
            DbUtils.closeQuietly(null, psDate, rsDate);
            iinkai.commit();
        }

        log.debug("委員会:処理日の前日=" + idou_date);
***/
        //対象データの条件：異動日が「処理日」
        String idou_date = _param._date;

        log.debug("委員会:異動日=" + idou_date);

        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Data data = (Data) it.next();

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = getIdouDataSql(data, idou_date);
                    ps = iinkai.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        final String idou_div = rs.getString("IDOU_DIV");

                        //異動区分をセット
                        data._changes_info = idou_div;
                        //異動区分（２：転出）の場合、転出日をセット
                        if ("2".equals(idou_div)) {
                            data._transfer_date = idou_date;
                        }
                        //異動区分（３：退職）の場合、退職日をセット
                        if ("3".equals(idou_div)) {
                            data._retired_date = idou_date;
                        }

                        //転出元学校コード・転出元学校課程コードをセット
                        data._transfer_schoolcd = _param._schoolcd;
                        data._transfer_course_cd = _param._course_cd;

                        count++;
                    }
                } catch (SQLException e) {
                    log.error("職員異動情報テーブルでエラー", e);
                    throw e;
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    iinkai.commit();
                }

        }//list

        log.debug("委員会:異動データ数=" + count);
    }

    private String getIdouDataSql(final Data data, final String idou_date) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     N1.NAMESPARE1 AS IDOU_DIV ");
        stb.append(" FROM ");
        stb.append("     EDBOARD_STAFF_WORK_HIST_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z041' AND N1.NAMECD2 = T1.TO_DIV ");
        stb.append(" WHERE ");
        stb.append("     T1.STAFFCD = '" + data._staffcd + "' ");
        stb.append("     AND T1.TO_DATE = '" + idou_date + "' "); //処理日の前日
        stb.append("     AND N1.NAMESPARE1 IN ('2','3') "); //2:転出、3:退職
        stb.append(" GROUP BY ");
        stb.append("     N1.NAMESPARE1 ");

        return stb.toString();
    }
} // StaffInfo

// eof
