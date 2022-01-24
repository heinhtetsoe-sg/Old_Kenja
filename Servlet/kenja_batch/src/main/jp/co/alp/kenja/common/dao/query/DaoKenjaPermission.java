// kanji=漢字
/*
 * $Id: DaoKenjaPermission.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/02/24 21:57:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Properties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.domain.KenjaPermission;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 賢者システムの権限を問い合わせる。
 * @author tamura
 * @version $Id: DaoKenjaPermission.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoKenjaPermission {
    private static final String KEY_PERMISSION_ADMIN_IDS = "permission.adminIDs";
    private static final String KEY_PERMISSION_DO_CHECK  = "permission.doCheck";

    private static final Log log = LogFactory.getLog(DaoKenjaPermission.class);
    private static final DaoKenjaPermission INSTANCE = new DaoKenjaPermission();

    /*
     * コンストラクタ。
     */
    private DaoKenjaPermission() {
        super();
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static DaoKenjaPermission getInstance() {
        return INSTANCE;
    }

    /*
     * ログインした職員が管理者か否か判定する。
     * @param staffCd ログインした職員コード
     * @param adminIds 管理者ID判定のPerl5互換正規表現
     * @return 管理者なら<code>true</code>
     * @throws MalformedPerl5PatternException 不正な正規表現
     */
    private static boolean isAdmin(final String staffCd, final String adminIds) {
        log.trace("プロパティファイルによる管理者判定:" + staffCd + "," + adminIds);

        if (StringUtils.isEmpty(staffCd))  { return false; }
        if (StringUtils.isEmpty(adminIds)) { return false; }

        if (staffCd.equals(adminIds)) {
            log.debug("  equalsで一致");
            return true;
        }

        // adminIds が「m/」で始まっていたらそのまま。否なら、「m/^」「$/」で囲む。
        final String re = adminIds.startsWith("m/") ? adminIds : "m/^" + adminIds + "$/";
        final Perl5Util p5 = new Perl5Util();

        final boolean rtn;
        try {
            rtn = p5.match(re, staffCd);
        } catch (final MalformedPerl5PatternException e) {
            log.warn("  正規表現が不正", e);
            throw e;
        }

        log.debug("  正規表現で判定:" + rtn);
        return rtn;
    }

    /**
     * ログインした職員が管理者("更新可"とは別の意。グループ9999)か否か判定する。
     * @param dbcon DB接続情報
     * @return グループ9999なら<code>true</code>
     */
    public boolean getAdministratorGroup(final DbConnection dbcon) {

        final KenjaParameters params = dbcon.getParameters();
        final String year = params.getParameter(DaoControlMaster.CTRL_M_CTRL_YEAR);
        final String staffCd = params.getStaffCd();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        final StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        sql.append("    (CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END) AS IS_GROUP9999 ");
        sql.append("FROM ");
        sql.append("    USERGROUP_DAT ");
        sql.append("WHERE ");
        sql.append("    YEAR = '" + year + "' ");
        sql.append("    AND GROUPCD = '9999' ");
        sql.append("    AND STAFFCD = '" + staffCd + "' ");

        try {
            boolean isGroup9999 = false;
            conn = dbcon.getROConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.clearParameters();
            rs = ps.executeQuery();

            if (rs.next()) {
                isGroup9999 = Boolean.valueOf(rs.getString(1)).booleanValue();
            }
            final boolean rtn = "00999999".equals(staffCd) || isGroup9999;
            log.trace("管理者グループ判定:" + rtn);
            return rtn;
        } catch (final SQLException e) {
            log.warn("管理者グループ判定:問い合わせ失敗:" + staffCd + ":" + e.getMessage());
            return false;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /*
     * 権限をDBに問い合わせる。
     * @param dbcon DB接続情報
     * @return 権限のインスタンス
     */
    private KenjaPermission query0(
            final DbConnection dbcon
    ) {
        final KenjaParameters params = dbcon.getParameters();
        final String staffCd = params.getStaffCd();
        final String programId = params.getProgramInfo().getProgramId();
        final String year = params.getParameter(DaoControlMaster.CTRL_M_CTRL_YEAR);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbcon.getROConnection();
            // ※ SQL文字列に'?' を使うとエラー。SQL0418N。スカラー関数だから？
            final String sql;
            if (StringUtils.isNotEmpty(params.getMenuSCHOOLCD()) && StringUtils.isNotEmpty(params.getMenuSCHOOLKIND())) {
                sql = "values SECURITY_CHK_PRG("
                        + " '" + StringEscapeUtils.escapeSql(staffCd) + "'"
                        + ",'" + StringEscapeUtils.escapeSql(programId) + "' "
                        + ",'" + StringEscapeUtils.escapeSql(year) + "'"
                        + ",'" + StringEscapeUtils.escapeSql(params.getMenuSCHOOLKIND()) + "'"
                        + ",'" + StringEscapeUtils.escapeSql(params.getMenuSCHOOLCD()) + "'"
                        + ")"
                        ;
            } else {
                sql = "values SECURITY_CHK_PRG("
                        + " '" + StringEscapeUtils.escapeSql(staffCd) + "'"
                        + ",'" + StringEscapeUtils.escapeSql(programId) + "' "
                        + ",'" + StringEscapeUtils.escapeSql(year) + "'"
                        + ")"
                        ;
            }
            
            ps = conn.prepareStatement(sql);
            ps.clearParameters();
            rs = ps.executeQuery();

            KenjaPermission perm = KenjaPermission.DENIED;
            if (rs.next()) {
                final String permCode = rs.getString(1);
                perm = KenjaPermission.getInstance(permCode);
            }

            log.debug(perm + "<==" + staffCd + "," + programId);
            return perm;
        } catch (final SQLException e) {
            final KenjaPermission perm = KenjaPermission.DENIED;
            log.warn("権限:問い合わせ失敗:" + e.getMessage());
            log.warn("権限:問い合わせ失敗:" + perm + "<==" + staffCd + "," + programId);
            return perm;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 権限をプロパティまたはDBに問い合わせる。
     * @param dbcon DB接続情報
     * @param prop プロパティ
     * @return 権限のインスタンス
     */
    public KenjaPermission query(
            final DbConnection dbcon,
            final Properties prop
    ) {
        final String adminIds = prop.getProperty(KEY_PERMISSION_ADMIN_IDS, null);
        try {
            if (isAdmin(dbcon.getParameters().getStaffCd(), adminIds)) {
                final KenjaPermission rtn = KenjaPermission.WRITABLE;
                log.info("権限:管理者として>>" + rtn);
                return rtn;
            }
        } catch (final MalformedPerl5PatternException e) {
            ; // nothing
        }

        final boolean doCheck = BooleanUtils.toBoolean(prop.getProperty(KEY_PERMISSION_DO_CHECK, "true"));
        if (doCheck) {
            final KenjaPermission rtn = query0(dbcon);
            log.info("権限:DBから得た>>" + rtn);
            return rtn;
        } else {
            final KenjaPermission rtn = KenjaPermission.RESTRICTED_WRITABLE;
            log.info("権限:権限チェックをしない>>" + rtn);
            return rtn;
        }
    }
} // DaoKenjaPermission

// eof
