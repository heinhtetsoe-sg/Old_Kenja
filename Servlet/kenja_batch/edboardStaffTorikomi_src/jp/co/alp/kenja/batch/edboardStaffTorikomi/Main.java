// kanji=漢字
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.edboardStaffTorikomi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * POSTGREサーバとの連携。
 *
 * @author m-yamashiro
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /* pkg */static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            final Package pkg = Main.class.getPackage();
            if (null != pkg) {
                final String implementationVersion = pkg.getImplementationVersion(); // クラスローダにより、nullの可能性あり
                if (null != implementationVersion) {
                    log.info("implementationVersion=" + implementationVersion);
                } else {
                    log.info("MANIFESTファイルからのバージョン取得には失敗しました。");
                }
            }

            final Param param = new Param(args);

            final Database iinkaiDB = new DB2UDB(param._knjUrl, param._knjUser, param._knjPass, DB2UDB.TYPE2);

            try {
                iinkaiDB.open();
            } catch (final InstantiationException e) {
                log.fatal("InstantiationException", e);
                throw e;
            } catch (final IllegalAccessException e) {
                log.fatal("IllegalAccessException", e);
                throw e;
            } catch (final ClassNotFoundException e) {
                log.fatal("ClassNotFoundException", e);
                throw e;
            }

            try {
                param.load(iinkaiDB);
                log.fatal("パラメータ: " + param);

                doIt(param, iinkaiDB);
            } catch (final SQLException e) {
                log.fatal("何らかのSQL例外が発生した!", e);
                throw e;
            }

            iinkaiDB.close();
            log.fatal("Done. 連携/賢者の双方の DBクローズ完了");
        } catch (final Throwable e) {
            log.fatal("重大エラー発生!", e);
        }
    }

    private static void doIt(final Param param, final Database iinkaiDB) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        final List schoolDbList = getSchoolDbList(param, iinkaiDB);
        final Date date = new Date();
        final String setFromDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        for (Iterator itDb = schoolDbList.iterator(); itDb.hasNext();) {
            final EdboardDb edboardDb = (EdboardDb) itDb.next();

            final String edWorkSql = getEdWorkSql(setFromDate, edboardDb);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = iinkaiDB.prepareStatement(edWorkSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffCd = rs.getString("STAFFCD");

                    String dbName = "//" + edboardDb._srvAddr;
                    dbName += null != edboardDb._srvPort && !"".equals(edboardDb._srvPort) ? ":" + edboardDb._srvPort : "";
                    dbName += "/" + edboardDb._dbname;
                    final Database schoolDB = new DB2UDB(dbName, edboardDb._user, edboardDb._userpass, DB2UDB.TYPE2);
                    try {
                        schoolDB.open();
                    } catch (final InstantiationException e) {
                        log.fatal("InstantiationException", e);
                        throw e;
                    } catch (final IllegalAccessException e) {
                        log.fatal("IllegalAccessException", e);
                        throw e;
                    } catch (final ClassNotFoundException e) {
                        log.fatal("ClassNotFoundException", e);
                        throw e;
                    }
                    updateSchoolStaff(iinkaiDB, schoolDB, param, staffCd, edboardDb);
                }
            } finally {
                iinkaiDB.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private static String getEdWorkSql(final String setFromDate, final EdboardDb edboardDb) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     EDBOARD_STAFF_WORK_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     FROM_DATE = '" + setFromDate + "' ");
        stb.append("     AND FROM_SCHOOLCD = '" + edboardDb._edboardSchoolcd + "' ");
        return stb.toString();
    }

    private static void updateSchoolStaff(final Database iinkaiDB, final Database schoolDB, final Param param, final String staffCd, final EdboardDb edboardDb) throws SQLException {
        final String edStaffCntSql = getStaffCntSql(staffCd);
        PreparedStatement psCnt = null;
        ResultSet rsCnt = null;
        int dataCnt;
        try {
            psCnt = schoolDB.prepareStatement(edStaffCntSql);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            dataCnt = rsCnt.getInt("CNT");
        } finally {
            schoolDB.commit();
            iinkaiDB.commit();
            DbUtils.closeQuietly(null, psCnt, rsCnt);
        }

        if (dataCnt > 0) {
            final String errMsgSql = getInsertErrSql(edboardDb._edboardSchoolcd, staffCd, "登録済み");
            iinkaiDB.executeUpdate(errMsgSql);
        } else {
            PreparedStatement ps = null;
            ResultSet rs = null;
            PreparedStatement psIns = null;
            try {
                final String edStaffSql = getStaffSql(staffCd);
                ps = iinkaiDB.prepareStatement(edStaffSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String insertStaffSql = getInsertStaffSql(rs);
                    psIns = schoolDB.prepareStatement(insertStaffSql);
                    psIns.executeUpdate();
                }
            } finally {
                iinkaiDB.commit();
                schoolDB.commit();
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(psIns);
            }

        }

    }

    private static String getStaffCntSql(final String staffCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     STAFF_MST ");
        stb.append(" WHERE ");
        stb.append("     STAFFCD = '" + staffCd + "' ");
        return stb.toString();
    }

    private static String getStaffSql(final String staffCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     STAFF_MST ");
        stb.append(" WHERE ");
        stb.append("     STAFFCD = '" + staffCd + "' ");
        return stb.toString();
    }

    private static String getInsertErrSql(final String schoolCd, final String staffCd, final String msg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO EDBOARD_STAFF_COPY_ERR_DAT  ");
        stb.append(" VALUES( ");
        stb.append("     '" + schoolCd + "', ");
        stb.append("     '" + staffCd + "', ");
        stb.append("     '" + msg + "', ");
        stb.append("     'alpBatch', ");
        stb.append("     SYSDATE() ");
        stb.append(" ) ");

        return stb.toString();
    }

    private static String getInsertStaffSql(final ResultSet rs) throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO STAFF_MST  ");
        stb.append(" VALUES( ");
        stb.append("     " + setData(rs.getString("STAFFCD")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFNAME")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFNAME_SHOW")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFNAME_KANA")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFNAME_ENG")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFNAME_REAL")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFNAME_KANA_REAL")) + ", ");
        stb.append("     " + setData(rs.getString("JOBCD")) + ", ");
        stb.append("     " + setData(rs.getString("SECTIONCD")) + ", ");
        stb.append("     " + setData(rs.getString("DUTYSHARECD")) + ", ");
        stb.append("     " + setData(rs.getString("CHARGECLASSCD")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFSEX")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFBIRTHDAY")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFZIPCD")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFADDR1")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFADDR2")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFTELNO")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFFAXNO")) + ", ");
        stb.append("     " + setData(rs.getString("STAFFE_MAIL")) + ", ");
        stb.append("     " + setData(rs.getString("EDBOARD_STAFFCD")) + ", ");
        stb.append("     " + setData(rs.getString("EDBOARD_TORIKOMI_FLG")) + ", ");
        stb.append("     'alpBatch', ");
        stb.append("     SYSDATE() ");
        stb.append(" ) ");

        return stb.toString();
    }

    private static String setData(final String setData) {
        if (null == setData) {
            return " null ";
        } else {
            return "'" + setData + "'";
        }
    }
    private static List getSchoolDbList(final Param param, final Database iinkaiDB) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String dbsql = getDbSql(param);
        try {
            ps = iinkaiDB.prepareStatement(dbsql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String edboardSchoolcd = rs.getString("EDBOARD_SCHOOLCD");
                final String srvAddr = rs.getString("SRV_ADDR");
                final String srvPort = rs.getString("SRV_PORT");
                final String dbname = rs.getString("DBNAME");
                final String user = rs.getString("USER");
                final String userpass = rs.getString("USERPASS");

                final EdboardDb edboardDb = new EdboardDb(edboardSchoolcd, srvAddr, srvPort, dbname, user, userpass);
                retList.add(edboardDb);
            }
        } finally {
            iinkaiDB.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private static String getDbSql(Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     EDBOARD_SCHOOL_DB_DAT ");
        return stb.toString();
    }

    private static class EdboardDb {
        final String _edboardSchoolcd;
        final String _srvAddr;
        final String _srvPort;
        final String _dbname;
        final String _user;
        final String _userpass;
        public EdboardDb(
                final String edboardSchoolcd,
                final String srvAddr,
                final String srvPort,
                final String dbname,
                final String user,
                final String userpass
        ) {
            _edboardSchoolcd = edboardSchoolcd;
            _srvAddr = srvAddr;
            _srvPort = srvPort;
            _dbname = dbname;
            _user = user;
            _userpass = userpass;
        }
    }
} // Main

// eof
