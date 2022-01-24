// kanji=漢字
/*
 * $Id: KnjSchregTextbookTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SCHREG_TEXTBOOK_TMPを作る。
 * @author takaesu
 * @version $Id: KnjSchregTextbookTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregTextbookTmp extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregTextbookTmp.class);

    public static final String ICASS_TABLE = "SEITO_TEXT_KONYU";
    public static final String ICASS_TABLE2 = "SEITO_TEXT_KONYU_MEISAI";

    public KnjSchregTextbookTmp() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍教科書購入データTMP"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "と" + ICASS_TABLE2 + " データ件数=" + list.size());

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

            final SchregTextbookDat schregTextbookDat = new SchregTextbookDat(map);
            rtn.add(schregTextbookDat);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.NENDO_CODE, ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     L2.NAMECD2 AS CURRICULUM_CD, ");
        stb.append("     L1.KYOKA_CODE, ");
        stb.append("     L1.TEXT_SHUMOKU_CODE, ");
        stb.append("     L1.TEXT_NO ");
        stb.append(" FROM ");
        stb.append("     SEITO_TEXT_KONYU T1 ");
        stb.append("     INNER JOIN SEITO_TEXT_KONYU_MEISAI L1 ON T1.NENDO_CODE = L1.NENDO_CODE ");
        stb.append("           AND T1.TEXT_KONYU_NO = L1.TEXT_KONYU_NO ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'W002' ");
        stb.append("          AND L1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L2.NAMESPARE1 AND L2.NAMESPARE2 ");
        stb.append("     inner JOIN SEITO L3 ON T1.SHIGANSHA_RENBAN = L3.SHIGANSHA_RENBAN ");
        stb.append("          AND VALUE(L3.SEITO_NO, '') <> '' ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T1.KONYU_JOTAI_CODE, '') <> '2' ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class SchregTextbookDat {
        final String _shiganshaRenban;
        final String _year;
        final String _schregno;
        final String _classcd;
        final String _curriculumCd;
        final String _subclasscd;
        final String _textbookcd;

        public SchregTextbookDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _year = (String) map.get("NENDO_CODE");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _classcd = (String) map.get("KYOKA_CODE");
            _curriculumCd = (String) map.get("CURRICULUM_CD");
            final String subclassCd = (String) map.get("TEXT_SHUMOKU_CODE");
            _subclasscd = _classcd + _subClassCdFormat.format(Integer.parseInt(subclassCd));
            _textbookcd = (String) map.get("TEXT_NO");
        }

        public String toString() {
            return _shiganshaRenban+","+_year+","+_schregno+","+_classcd+","+_subclasscd+","+_textbookcd;
        }        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_TEXTBOOK_TMP

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        CLASSCD                        SYSIBM    VARCHAR                   2     0 いいえ
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 いいえ
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 いいえ
        TEXTBOOKCD                     SYSIBM    VARCHAR                   8     0 いいえ
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          8 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SchregTextbookDat schregTextbookDat = (SchregTextbookDat) it.next();
            final String insSql = getInsertSql(schregTextbookDat);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                log.error("SQLException: "  +schregTextbookDat);
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final SchregTextbookDat schregTextbookDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_TEXTBOOK_TMP ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregTextbookDat._year) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._schregno) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._classcd) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._curriculumCd) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._subclasscd) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._textbookcd) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

