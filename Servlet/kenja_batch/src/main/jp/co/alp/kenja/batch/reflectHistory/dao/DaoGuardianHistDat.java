/*
 * $Id: DaoGuardianHistDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.reflectHistory.FieldValue;
import jp.co.alp.kenja.batch.reflectHistory.HistoryUpdateFlgField;
import jp.co.alp.kenja.batch.reflectHistory.ReflectHistoryContext;
import jp.co.alp.kenja.common.dao.DbConnection;

public class DaoGuardianHistDat implements ReflectHistoryQueryUpdate {

    private Log log = LogFactory.getLog(DaoGuarantorHistDat.class);

    /**
        $ db2 describe table guardian_hist_dat

                                       タイプ・
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        ISSUEDATE                      SYSIBM    DATE                      4     0 いいえ
        EXPIREDATE                     SYSIBM    DATE                      4     0 はい
        RELATIONSHIP                   SYSIBM    VARCHAR                   2     0 いいえ
        GUARD_NAME                     SYSIBM    VARCHAR                  60     0 はい
        GUARD_KANA                     SYSIBM    VARCHAR                 120     0 はい
        GUARD_REAL_NAME                SYSIBM    VARCHAR                 120     0 はい
        GUARD_REAL_KANA                SYSIBM    VARCHAR                 240     0 はい
        GUARD_SEX                      SYSIBM    VARCHAR                   1     0 はい
        GUARD_BIRTHDAY                 SYSIBM    DATE                      4     0 はい
        RELATIONSHIP_FLG               SYSIBM    VARCHAR                   1     0 はい
        GUARD_NAME_FLG                 SYSIBM    VARCHAR                   1     0 はい
        GUARD_KANA_FLG                 SYSIBM    VARCHAR                   1     0 はい
        GUARD_REAL_NAME_FLG            SYSIBM    VARCHAR                 120     0 はい
        GUARD_REAL_KANA_FLG            SYSIBM    VARCHAR                 240     0 はい
        GUARD_SEX_FLG                  SYSIBM    VARCHAR                   1     0 はい
        GUARD_BIRTHDAY_FLG             SYSIBM    VARCHAR                   1     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい

          19 レコードが選択されました。
    */

    private final static String SCHREGNO = "SCHREGNO";
    private final static String ISSUEDATE = "ISSUEDATE";
    private final static String EXPIREDATE = "EXPIREDATE";
    private final static String RELATIONSHIP = "RELATIONSHIP";
    private final static String GUARD_NAME = "GUARD_NAME";
    private final static String GUARD_KANA = "GUARD_KANA";
    private final static String GUARD_REAL_NAME = "GUARD_REAL_NAME";
    private final static String GUARD_REAL_KANA = "GUARD_REAL_KANA";
    private final static String GUARD_SEX = "GUARD_SEX";
    private final static String GUARD_BIRTHDAY = "GUARD_BIRTHDAY";
    private final static String RELATIONSHIP_FLG = "RELATIONSHIP_FLG";
    private final static String GUARD_NAME_FLG = "GUARD_NAME_FLG";
    private final static String GUARD_KANA_FLG = "GUARD_KANA_FLG";
    private final static String GUARD_REAL_NAME_FLG = "GUARD_REAL_NAME_FLG";
    private final static String GUARD_REAL_KANA_FLG = "GUARD_REAL_KANA_FLG";
    private final static String GUARD_SEX_FLG = "GUARD_SEX_FLG";
    private final static String GUARD_BIRTHDAY_FLG = "GUARD_BIRTHDAY_FLG";
    private final String TARGET = GUARDIAN_DAT;
    private final String SOURCE = GUARDIAN_HIST_DAT;
    private final List<HistoryUpdateFlgField> _flgList;

    public DaoGuardianHistDat() {
        _flgList = new ArrayList<HistoryUpdateFlgField>();
        _flgList.add(new HistoryUpdateFlgField(RELATIONSHIP_FLG, RELATIONSHIP));
        _flgList.add(new HistoryUpdateFlgField(GUARD_NAME_FLG, GUARD_NAME));
        _flgList.add(new HistoryUpdateFlgField(GUARD_KANA_FLG, GUARD_KANA));
        _flgList.add(new HistoryUpdateFlgField(GUARD_REAL_NAME_FLG, GUARD_REAL_NAME));
        _flgList.add(new HistoryUpdateFlgField(GUARD_REAL_KANA_FLG, GUARD_REAL_KANA));
        _flgList.add(new HistoryUpdateFlgField(GUARD_SEX_FLG, GUARD_SEX));
        _flgList.add(new HistoryUpdateFlgField(GUARD_BIRTHDAY_FLG, GUARD_BIRTHDAY));
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Map<String, String>> query(final DbConnection dbcon, final ReflectHistoryContext ctx) throws SQLException {
        final String sql = " SELECT * FROM " + SOURCE + " WHERE " + ISSUEDATE + " = ? ";
        return QueryUtils.fetchAssoc(dbcon, sql, new String[] { ctx.getDate().getSQLDate().toString() });
    }

    private Map<String, String> getTargetAssoc(final DbConnection dbcon, final Map<String, String> assoc) throws SQLException {
        final String sqlTgt = " SELECT * FROM " + TARGET + " WHERE " + SCHREGNO + " = ? ";
        final Collection<Map<String, String>> tgtList = QueryUtils.fetchAssoc(dbcon, sqlTgt, new String[] { QueryUtils.getString(assoc, SCHREGNO) });
        if (tgtList.isEmpty()) {
            log.info("対象データがないため更新無し :" + TARGET + ", SCHREGNO = " + QueryUtils.getString(assoc, SCHREGNO));
            return null;
        }
        final Map<String, String> tgtAssoc = tgtList.iterator().next();
        return tgtAssoc;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getUpdateSqlList(final DbConnection dbcon, final ReflectHistoryContext ctx, final Map<String, String> assoc) throws SQLException {
        final Map<String, String> tgtAssoc = getTargetAssoc(dbcon, assoc);
        if (null == tgtAssoc) {
            return null;
        }
        final List<FieldValue> where = new ArrayList<FieldValue>();
        where.add(new FieldValue(SCHREGNO, QueryUtils.getString(assoc, SCHREGNO)));

        return Collections.singletonList(QueryUtils.createUpdateSql(ctx, TARGET, _flgList, assoc, tgtAssoc, where));
    }
}
