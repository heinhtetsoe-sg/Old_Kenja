/*
 * $Id: DaoSchregBaseHistDat.java 74552 2020-05-27 04:41:22Z maeshiro $
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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.reflectHistory.FieldValue;
import jp.co.alp.kenja.batch.reflectHistory.HistoryUpdateFlgField;
import jp.co.alp.kenja.batch.reflectHistory.ReflectHistoryContext;
import jp.co.alp.kenja.common.dao.DbConnection;

public class DaoSchregBaseHistDat implements ReflectHistoryQueryUpdate {

    private Log log = LogFactory.getLog(DaoGuarantorHistDat.class);

    /**
        $ db2 describe table schreg_base_hist_dat

                                       タイプ・
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        ISSUEDATE                      SYSIBM    DATE                      4     0 いいえ
        EXPIREDATE                     SYSIBM    DATE                      4     0 はい
        YEAR                           SYSIBM    VARCHAR                   4     0 はい
        SEMESTER                       SYSIBM    VARCHAR                   1     0 はい
        GRADE                          SYSIBM    VARCHAR                   2     0 はい
        HR_CLASS                       SYSIBM    VARCHAR                   3     0 はい
        ATTENDNO                       SYSIBM    VARCHAR                   3     0 はい
        ANNUAL                         SYSIBM    VARCHAR                   2     0 はい
        COURSECD                       SYSIBM    VARCHAR                   1     0 はい
        MAJORCD                        SYSIBM    VARCHAR                   3     0 はい
        COURSECODE                     SYSIBM    VARCHAR                   4     0 はい
        NAME                           SYSIBM    VARCHAR                 120     0 はい
        NAME_SHOW                      SYSIBM    VARCHAR                 120     0 はい
        NAME_KANA                      SYSIBM    VARCHAR                 240     0 はい
        NAME_ENG                       SYSIBM    VARCHAR                  40     0 はい
        REAL_NAME                      SYSIBM    VARCHAR                 120     0 はい
        REAL_NAME_KANA                 SYSIBM    VARCHAR                 240     0 はい
        GRADE_FLG                      SYSIBM    VARCHAR                   1     0 はい
        HR_CLASS_FLG                   SYSIBM    VARCHAR                   1     0 はい
        ATTENDNO_FLG                   SYSIBM    VARCHAR                   1     0 はい
        ANNUAL_FLG                     SYSIBM    VARCHAR                   1     0 はい
        COURSECD_FLG                   SYSIBM    VARCHAR                   1     0 はい
        MAJORCD_FLG                    SYSIBM    VARCHAR                   1     0 はい
        COURSECODE_FLG                 SYSIBM    VARCHAR                   1     0 はい
        NAME_FLG                       SYSIBM    VARCHAR                   1     0 はい
        NAME_SHOW_FLG                  SYSIBM    VARCHAR                   1     0 はい
        NAME_KANA_FLG                  SYSIBM    VARCHAR                   1     0 はい
        NAME_ENG_FLG                   SYSIBM    VARCHAR                   1     0 はい
        REAL_NAME_FLG                  SYSIBM    VARCHAR                   1     0 はい
        REAL_NAME_KANA_FLG             SYSIBM    VARCHAR                   1     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい

          33 レコードが選択されました。
    */

    private final static String SCHREGNO = "SCHREGNO";
    private final static String ISSUEDATE = "ISSUEDATE";
    private final static String YEAR = "YEAR";
    private final static String SEMESTER = "SEMESTER";
    private final static String GRADE = "GRADE";
    private final static String HR_CLASS = "HR_CLASS";
    private final static String ATTENDNO = "ATTENDNO";
    private final static String ANNUAL = "ANNUAL";
    private final static String COURSECD = "COURSECD";
    private final static String MAJORCD = "MAJORCD";
    private final static String COURSECODE = "COURSECODE";
    private final static String NAME = "NAME";
    private final static String NAME_SHOW = "NAME_SHOW";
    private final static String NAME_KANA = "NAME_KANA";
    private final static String NAME_ENG = "NAME_ENG";
    private final static String REAL_NAME = "REAL_NAME";
    private final static String REAL_NAME_KANA = "REAL_NAME_KANA";
    private final static String GRADE_FLG = "GRADE_FLG";
    private final static String HR_CLASS_FLG = "HR_CLASS_FLG";
    private final static String ATTENDNO_FLG = "ATTENDNO_FLG";
    private final static String ANNUAL_FLG = "ANNUAL_FLG";
    private final static String COURSECD_FLG = "COURSECD_FLG";
    private final static String MAJORCD_FLG = "MAJORCD_FLG";
    private final static String COURSECODE_FLG = "COURSECODE_FLG";
    private final static String NAME_FLG = "NAME_FLG";
    private final static String NAME_SHOW_FLG = "NAME_SHOW_FLG";
    private final static String NAME_KANA_FLG = "NAME_KANA_FLG";
    private final static String NAME_ENG_FLG = "NAME_ENG_FLG";
    private final static String REAL_NAME_FLG = "REAL_NAME_FLG";
    private final static String REAL_NAME_KANA_FLG = "REAL_NAME_KANA_FLG";
    private final String SOURCE = SCHREG_BASE_HIST_DAT;
    private final List<HistoryUpdateFlgField> _flgRegdList;
    private final List<HistoryUpdateFlgField> _flgBaseList;

    public DaoSchregBaseHistDat() {
        _flgRegdList = new ArrayList<HistoryUpdateFlgField>();
        _flgRegdList.add(new HistoryUpdateFlgField(GRADE_FLG, GRADE));
        _flgRegdList.add(new HistoryUpdateFlgField(HR_CLASS_FLG, HR_CLASS));
        _flgRegdList.add(new HistoryUpdateFlgField(ATTENDNO_FLG, ATTENDNO));
        _flgRegdList.add(new HistoryUpdateFlgField(ANNUAL_FLG, ANNUAL));
        _flgRegdList.add(new HistoryUpdateFlgField(COURSECD_FLG, COURSECD));
        _flgRegdList.add(new HistoryUpdateFlgField(MAJORCD_FLG, MAJORCD));
        _flgRegdList.add(new HistoryUpdateFlgField(COURSECODE_FLG, COURSECODE));

        _flgBaseList = new ArrayList<HistoryUpdateFlgField>();
        _flgBaseList.add(new HistoryUpdateFlgField(NAME_FLG, NAME));
        _flgBaseList.add(new HistoryUpdateFlgField(NAME_SHOW_FLG, NAME_SHOW));
        _flgBaseList.add(new HistoryUpdateFlgField(NAME_KANA_FLG, NAME_KANA));
        _flgBaseList.add(new HistoryUpdateFlgField(NAME_ENG_FLG, NAME_ENG));
        _flgBaseList.add(new HistoryUpdateFlgField(REAL_NAME_FLG, REAL_NAME));
        _flgBaseList.add(new HistoryUpdateFlgField(REAL_NAME_KANA_FLG, REAL_NAME_KANA));
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Map<String, String>> query(final DbConnection dbcon, final ReflectHistoryContext ctx) throws SQLException {
        final String sql = " SELECT * FROM " + SOURCE + " WHERE " + ISSUEDATE + " = ? ";
        return QueryUtils.fetchAssoc(dbcon, sql, new String[] { ctx.getDate().getSQLDate().toString() });
    }

    private Map<String, String> getTargetAssoc1(final DbConnection dbcon, final ReflectHistoryContext ctx, final Map<String, String> assoc) throws SQLException {
        final String sqlTgt = " SELECT * FROM " + SCHREG_REGD_DAT + " WHERE " + SCHREGNO + " = ? AND " + YEAR + " = ? AND " + SEMESTER + " = ? ";
        final String schregno = QueryUtils.getString(assoc, SCHREGNO);
        final String year = ctx.getControlMaster().getCurrentYearAsString();
        final String semester = ctx.getControlMaster().getCurrentSemester().getCodeAsString();
        final Collection<Map<String, String>> tgtList = QueryUtils.fetchAssoc(dbcon, sqlTgt, new String[] { schregno, year, semester});
        if (tgtList.isEmpty()) {
            log.info("対象データがないため更新無し :" + SCHREG_REGD_DAT + ", SCHREGNO = " + schregno + ", YEAR = " + year + ", SEMESTER = " + semester);
            return null;
        }
        final Map<String, String> tgtAssoc = tgtList.iterator().next();
        return tgtAssoc;
    }

    private Map<String, String> getTargetAssoc2(final DbConnection dbcon, final Map<String, String> assoc) throws SQLException {
        final String sqlTgt = " SELECT * FROM " + SCHREG_BASE_MST + " WHERE " + SCHREGNO + " = ? ";
        final String schregno = QueryUtils.getString(assoc, SCHREGNO);
        final Collection<Map<String, String>> tgtList = QueryUtils.fetchAssoc(dbcon, sqlTgt, new String[] { schregno });
        if (tgtList.isEmpty()) {
            log.info("対象データがないため更新無し :" + SCHREG_BASE_MST + ", SCHREGNO = " + schregno);
            return null;
        }
        final Map<String, String> tgtAssoc = tgtList.iterator().next();
        return tgtAssoc;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getUpdateSqlList(final DbConnection dbcon, final ReflectHistoryContext ctx, final Map<String, String> assoc) throws SQLException {
        final List<String> sqlList = new ArrayList<String>();
        final Map<String, String> tgtRegdAssoc = getTargetAssoc1(dbcon, ctx, assoc);
        if (null != tgtRegdAssoc) {
            final List<FieldValue> regdWhere = new ArrayList<FieldValue>();
            regdWhere.add(new FieldValue(SCHREGNO, QueryUtils.getString(assoc, SCHREGNO)));
            regdWhere.add(new FieldValue(YEAR, ctx.getControlMaster().getCurrentYearAsString()));
            regdWhere.add(new FieldValue(SEMESTER, ctx.getControlMaster().getCurrentSemester().getCodeAsString()));

            sqlList.add(QueryUtils.createUpdateSql(ctx, SCHREG_REGD_DAT, _flgRegdList, assoc, tgtRegdAssoc, regdWhere));
        }

        final Map<String, String> tgtBaseAssoc = getTargetAssoc2(dbcon, assoc);
        if (null != tgtBaseAssoc) {
            final List<FieldValue> baseWhere = new ArrayList<FieldValue>();
            baseWhere.add(new FieldValue(SCHREGNO, QueryUtils.getString(assoc, SCHREGNO)));

            sqlList.add(QueryUtils.createUpdateSql(ctx, SCHREG_BASE_MST, _flgBaseList, assoc, tgtBaseAssoc, baseWhere));
        }
        return sqlList;
    }
}
