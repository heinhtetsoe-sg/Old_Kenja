// kanji=漢字
/*
 * $Id: KnjCompRegistDat3.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * COMP_REGIST_DATを作る。
 * @author takaesu
 * @version $Id: KnjCompRegistDat3.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjCompRegistDat3 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjCompRegistDat3.class);

    public static final String ICASS_TABLE = "SEITO_RISHU_KAMOKU";

    public KnjCompRegistDat3() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "履修登録データ"; }

    void migrate() throws SQLException {
        //for (int strSep = 1; strSep < 80000; strSep += 5000) {
        //for (int strSep = 80001; strSep < 160000; strSep += 5000) {
        for (int strSep = 160001; strSep < 240000; strSep += 5000) {
            final int endSep = strSep + 4999;
            final List list = loadIcass(strSep, endSep);
            log.debug(ICASS_TABLE + "データ件数=" + list.size());
            log.debug(String.valueOf(strSep) + "～" + String.valueOf(endSep));

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("更新処理中にエラー! rollback した。");
                throw e;
            }
        }
    }

    private List loadIcass(final int strSep, final int endSep) throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql(strSep, endSep);
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final CompRegistDat compRegistDat = new CompRegistDat(map);
            rtn.add(compRegistDat);
        }
        return rtn;
    }

    private String getSql(final int strSep, final int endSep) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MK AS (  ");
        stb.append(" SELECT  ");
        stb.append("     ROW_NUMBER() OVER() AS COUN,  ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     L2.NAMECD2 AS CURRICULUM_CD,  ");
        stb.append("     T1.NENDO_CODE,  ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     T1.KAMOKU_CODE,  ");
        stb.append("     L1.HYOJUN_TANI  ");
        stb.append(" FROM  ");
        stb.append("     SEITO_RISHU_KAMOKU T1  ");
        stb.append("     LEFT JOIN KAMOKU L1 ON T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = L1.KYOIKUKATEI_TEKIYO_NENDO_CODE  ");
        stb.append("          AND T1.KYOKA_CODE = L1.KYOKA_CODE  ");
        stb.append("          AND T1.KAMOKU_CODE = L1.KAMOKU_CODE  ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'W002'  ");
        stb.append("          AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L2.NAMESPARE1 AND L2.NAMESPARE2,  ");
        stb.append("     SEITO T2  ");
        stb.append(" WHERE  ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN  ");
        stb.append("     AND VALUE(T2.SEITO_NO, '') <> ''  ");
        stb.append(" )  ");
        stb.append(" SELECT  ");
        stb.append("     SHIGANSHA_RENBAN,  ");
        stb.append("     KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     CURRICULUM_CD,  ");
        stb.append("     NENDO_CODE,  ");
        stb.append("     KYOKA_CODE,  ");
        stb.append("     KAMOKU_CODE,  ");
        stb.append("     HYOJUN_TANI  ");
        stb.append(" FROM  ");
        stb.append("     MK  ");
        stb.append(" WHERE  ");
        stb.append("     COUN BETWEEN " + strSep + " AND " + endSep + "  ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class CompRegistDat {
        final String _shiganshaRenban;
        final String _year;
        final String _schregno;
        final String _classcd;
        final String _curriculumCd;
        final String _subclasscd;
        final Integer _compCredit;
        final String _againCompFlg;

        public CompRegistDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _year = (String) map.get("NENDO_CODE");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _classcd = (String) map.get("KYOKA_CODE");
            _curriculumCd = (String) map.get("CURRICULUM_CD");
            final String subclassCd = (String) map.get("KAMOKU_CODE");
            _subclasscd = _classcd + _subClassCdFormat.format(Integer.valueOf(subclassCd));
            _compCredit = Integer.valueOf((String) map.get("HYOJUN_TANI"));
            _againCompFlg = "";    // TODO:データ移行後
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table COMP_REGIST_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        CLASSCD                        SYSIBM    VARCHAR                   2     0 いいえ
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 いいえ
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 いいえ
        COMP_CREDIT                    SYSIBM    SMALLINT                  2     0 はい
        AGAIN_COMP_FLG                 SYSIBM    VARCHAR                   1     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          9 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final CompRegistDat compRegistDat = (CompRegistDat) it.next();
            final String insSql = getInsertSql(compRegistDat);
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

    private String getInsertSql(final CompRegistDat compRegistDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO COMP_REGIST_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(compRegistDat._year) + ", ");
        stb.append(" " + getInsertVal(compRegistDat._schregno) + ", ");
        stb.append(" " + getInsertVal(compRegistDat._classcd) + ", ");
        stb.append(" " + getInsertVal(compRegistDat._curriculumCd) + ", ");
        stb.append(" " + getInsertVal(compRegistDat._subclasscd) + ", ");
        stb.append(" " + getInsertVal(compRegistDat._compCredit) + ", ");
        stb.append(" " + getInsertVal(compRegistDat._againCompFlg) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

