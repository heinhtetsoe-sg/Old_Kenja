// kanji=漢字
/*
 * $Id: KnjGuardianDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GUARDIAN_DATを作る。
 * @author takaesu
 * @version $Id: KnjGuardianDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjGuardianDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjGuardianDat.class);

    public static final String ICASS_TABLE = "SEITO";

    public KnjGuardianDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍保護者データ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        try {
            saveKnj(list);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("更新処理中にエラー! rollback した。");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final GuardianDat guardianDat = new GuardianDat(map);
            rtn.add(guardianDat);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.HOGOSHA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_KANA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_ZOKUGARA_CODE, ");
        stb.append("     T1.HOGOSHA_YUBIN_NO, ");
        stb.append("     T1.HOGOSHA_TODOFUKEN_NO, ");
        stb.append("     T1.HOGOSHA_ADDRESS1, ");
        stb.append("     T1.HOGOSHA_ADDRESS2, ");
        stb.append("     T1.HOGOSHA_TEL_NO ");
        stb.append(" FROM ");
        stb.append("     SEITO T1 ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T1.SEITO_NO, '') <> '' ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class GuardianDat {
        final String _shiganshaRenban;
        final String _schregno;
        final String _relationship;
        final String _guardName;
        final String _guardKana;
        final String _guardBirthday;
        final String _guardSex;
        final String _guardZipcd;
        final String _guardPrefCd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _guardAddr3;
        final String _guardTelno;
        final String _guardTelnoAbb;
        final String _guardFaxno;
        final String _guardEMail;
        final String _guardJobcd;
        final String _guardWorkName;
        final String _guardWorkTelno;
        final String _guarantorRelationship;
        final String _guarantorName;
        final String _guarantorKana;
        final String _guarantorSex;
        final String _guarantorZipcd;
        final String _guarantorPrefCd;
        final String _guarantorAddr1;
        final String _guarantorAddr2;
        final String _guarantorAddr3;
        final String _guarantorTelno;
        final String _guarantorTelnoAbb;
        final String _guarantorJobcd;
        final String _publicOffice;

        public GuardianDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _schregno = _param.getSchregno(_shiganshaRenban);
            final String zokugara = (String) map.get("HOGOSHA_ZOKUGARA_CODE");
            _relationship = _param.getZokugara(zokugara);
            _guardSex = _param.getZokugaraSex(zokugara);
            _guardName = (String) map.get("HOGOSHA_SHIMEI");
            _guardKana = (String) map.get("HOGOSHA_KANA_SHIMEI");
            _guardBirthday = "";    // TODO:未定
            _guardZipcd = (String) map.get("HOGOSHA_YUBIN_NO");
            _guardPrefCd = (String) map.get("HOGOSHA_TODOFUKEN_NO");
            final String[] gAddr = divideStr((String) map.get("HOGOSHA_ADDRESS1"));
            _guardAddr1 = gAddr[0];
            _guardAddr2 = gAddr[1];
            _guardAddr3 = (String) map.get("HOGOSHA_ADDRESS2");
            _guardTelno = (String) map.get("HOGOSHA_TEL_NO");
            _guardTelnoAbb = deleteStr(_guardTelno, "-");
            _guardFaxno = "";    // TODO:未定
            _guardEMail = "";    // TODO:未定
            _guardJobcd = "";    // TODO:未定
            _guardWorkName = "";    // TODO:未定
            _guardWorkTelno = "";    // TODO:未定
            _guarantorRelationship = "";    // TODO:未定
            _guarantorName = "";    // TODO:未定
            _guarantorKana = "";    // TODO:未定
            _guarantorSex = "";    // TODO:未定
            _guarantorZipcd = "";    // TODO:未定
            _guarantorPrefCd = "";    // TODO:未定
            _guarantorAddr1 = "";    // TODO:未定
            _guarantorAddr2 = "";    // TODO:未定
            _guarantorAddr3 = "";    // TODO:未定
            _guarantorTelno = "";    // TODO:未定
            _guarantorTelnoAbb = "";    // TODO:未定
            _guarantorJobcd = "";    // TODO:未定
            _publicOffice = "";    // TODO:未定
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table GUARDIAN_DAT
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        RELATIONSHIP                   SYSIBM    VARCHAR                   2     0 いいえ
        GUARD_NAME                     SYSIBM    VARCHAR                  60     0 はい
        GUARD_KANA                     SYSIBM    VARCHAR                 120     0 はい
        GUARD_BIRTHDAY                 SYSIBM    DATE                      4     0 はい
        GUARD_SEX                      SYSIBM    VARCHAR                   1     0 はい
        GUARD_ZIPCD                    SYSIBM    VARCHAR                   8     0 はい
        GUARD_PREF_CD                  SYSIBM    VARCHAR                   2     0 はい
        GUARD_ADDR1                    SYSIBM    VARCHAR                  75     0 はい
        GUARD_ADDR2                    SYSIBM    VARCHAR                  75     0 はい
        GUARD_ADDR3                    SYSIBM    VARCHAR                  75     0 はい
        GUARD_TELNO                    SYSIBM    VARCHAR                  14     0 はい
        GUARD_TELNO_ABB                SYSIBM    VARCHAR                  14     0 はい
        GUARD_FAXNO                    SYSIBM    VARCHAR                  14     0 はい
        GUARD_E_MAIL                   SYSIBM    VARCHAR                  20     0 はい
        GUARD_JOBCD                    SYSIBM    VARCHAR                   2     0 はい
        GUARD_WORK_NAME                SYSIBM    VARCHAR                  60     0 はい
        GUARD_WORK_TELNO               SYSIBM    VARCHAR                  14     0 はい
        GUARANTOR_RELATIONSHIP         SYSIBM    VARCHAR                   2     0 はい
        GUARANTOR_NAME                 SYSIBM    VARCHAR                  60     0 はい
        GUARANTOR_KANA                 SYSIBM    VARCHAR                 120     0 はい
        GUARANTOR_SEX                  SYSIBM    VARCHAR                   1     0 はい
        GUARANTOR_ZIPCD                SYSIBM    VARCHAR                   8     0 はい
        GUARANTOR_PREF_CD              SYSIBM    VARCHAR                   2     0 はい
        GUARANTOR_ADDR1                SYSIBM    VARCHAR                  75     0 はい
        GUARANTOR_ADDR2                SYSIBM    VARCHAR                  75     0 はい
        GUARANTOR_ADDR3                SYSIBM    VARCHAR                  75     0 はい
        GUARANTOR_TELNO                SYSIBM    VARCHAR                  14     0 はい
        GUARANTOR_TELNO_ABB            SYSIBM    VARCHAR                  14     0 はい
        GUARANTOR_JOBCD                SYSIBM    VARCHAR                   2     0 はい
        PUBLIC_OFFICE                  SYSIBM    VARCHAR                  30     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          33 レコードが選択されました。

     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final GuardianDat guardianDat = (GuardianDat) it.next();
            final String insSql = getInsertSql(guardianDat);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final GuardianDat guardianDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO GUARDIAN_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(guardianDat._schregno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._relationship) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardName) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardKana) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardBirthday) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardSex) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardZipcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardPrefCd) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guardAddr1) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guardAddr2) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guardAddr3) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardTelno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardTelnoAbb) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardFaxno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardEMail) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardJobcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardWorkName) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardWorkTelno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorRelationship) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorName) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorKana) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorSex) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorZipcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorPrefCd) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guarantorAddr1) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guarantorAddr2) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guarantorAddr3) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorTelno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorTelnoAbb) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorJobcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._publicOffice) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

