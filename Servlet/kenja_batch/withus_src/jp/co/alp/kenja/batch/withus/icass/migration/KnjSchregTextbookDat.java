// kanji=
/*
 * $Id: KnjSchregTextbookDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * ?: 2008/08/15 15:46:35 - JST
 * ?: takaesu
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
 * SCHREG_TEXTBOOK_DATB
 * @author takaesu
 * @version $Id: KnjSchregTextbookDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregTextbookDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregTextbookDat.class);

    public static final String ICASS_TABLE = "SEITO_TEXT_KONYU";
    public static final String ICASS_TABLE2 = "SEITO_TEXT_KONYU_MEISAI";

    public KnjSchregTextbookDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "w??wf[^"; }

    void migrate() throws SQLException {
        int start = 250001;
        int kizami = 5000;
        int max = 260000;   //250000åèÇÆÇÁÇ¢Ç≈ÇµÇΩ
        for (int strSep = start; strSep < max; strSep += kizami) {
            final int endSep = strSep + kizami - 1;
            final List list = loadIcass(strSep, endSep);
            log.debug(ICASS_TABLE + "" + ICASS_TABLE2 + " f[^=" + list.size());
            log.debug(" "+String.valueOf(strSep) + "`" + String.valueOf(endSep));

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("XV?G[! rollback B");
                throw e;
            }
        }
    }

    private List loadIcass(int begin, int end) throws SQLException {

        
        final List rtn = new ArrayList();

        // SQLs
        final List result;
        try {
            final String sql = getSql(begin ,end);
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: f[^?????B?l!
        } catch (final SQLException e) {
            log.error("ICASSf[^???G[", e);
            throw e;
        }

        // ??
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final SchregTextbookDat schregTextbookDat = new SchregTextbookDat(map);
            rtn.add(schregTextbookDat);
        }
        return rtn;
    }

    private String getSql(int begin, int end) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH MK AS (");
        stb.append("SELECT  ");
        stb.append("    ROW_NUMBER() OVER() AS COUN,          ");
        stb.append("    T1.NENDO_CODE,  ");
        stb.append("    L4.ORDER_SEQ AS TEXT_KONYU_NO,  ");
        stb.append("    T1.SHIGANSHA_RENBAN,  ");
        stb.append("    L2.NAMECD2 AS CURRICULUM_CD,  ");
        stb.append("    L1.KYOKA_CODE,  ");
        stb.append("    L1.TEXT_SHUMOKU_CODE,  ");
        stb.append("    L1.TEXT_NO,  ");
        stb.append("    L3.KAMOKU_CODE  ");
        stb.append("FROM  ");
        stb.append("    SEITO_TEXT_KONYU T1  ");
        stb.append("    INNER JOIN SEITO L0 ON T1.SHIGANSHA_RENBAN = L0.SHIGANSHA_RENBAN  ");
        stb.append("       AND VALUE(L0.SEITO_NO, '') <> ''  ");
        stb.append("    INNER JOIN SEITO_TEXT_KONYU_MEISAI L1 ON T1.NENDO_CODE = L1.NENDO_CODE  ");
        stb.append("       AND T1.TEXT_KONYU_NO = L1.TEXT_KONYU_NO  ");
        stb.append("    LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'W002'  ");
        stb.append("       AND L1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L2.NAMESPARE1 AND L2.NAMESPARE2  ");
        stb.append("    LEFT JOIN USE_TEXT L3 ON T1.NENDO_CODE = L3.NENDO_CODE  ");
        stb.append("       AND L1.KYOIKUKATEI_TEKIYO_NENDO_CODE = L3.KYOIKUKATEI_TEKIYO_NENDO_CODE  ");
        stb.append("       AND L1.KYOKA_CODE = L3.KYOKA_CODE  ");
        stb.append("       AND L1.TEXT_SHUMOKU_CODE = L3.KYOKASHO_SHUMOKU_CODE  ");
        stb.append("       AND L1.TEXT_NO = L3.TEXT_NO  ");
        stb.append("    LEFT JOIN TEXT_SEQ_MASTER_TMP L4 ON L4.NENDO_CODE = T1.NENDO_CODE ");
        stb.append("       AND L4.TEXT_KONYU_NO = T1.TEXT_KONYU_NO ");
        stb.append("WHERE  ");
        stb.append("    VALUE(T1.KONYU_JOTAI_CODE, '') = '2'  ");
        stb.append("ORDER BY L4.ORDER_SEQ ");
        stb.append(")");
        stb.append(" SELECT ");
        stb.append("     COUN, ");
        stb.append("     NENDO_CODE, ");
        stb.append("     TEXT_KONYU_NO, ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     KYOKA_CODE, ");
        stb.append("     TEXT_SHUMOKU_CODE, ");
        stb.append("     TEXT_NO, ");
        stb.append("     KAMOKU_CODE ");
        stb.append("  FROM MK ");
        stb.append("  WHERE COUN BETWEEN "+begin+" AND "+end+" ");

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
        final Integer _orderSeq;
        final String _getFlg;

        public SchregTextbookDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _year = (String) map.get("NENDO_CODE");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _classcd = (String) map.get("KYOKA_CODE");
            _curriculumCd = (String) map.get("CURRICULUM_CD");
            final String subclassCd = (String) map.get("KAMOKU_CODE");
            Integer iSubclassCd = null;
            try {
                iSubclassCd = Integer.valueOf(subclassCd);
            } catch (NumberFormatException ex) {
                // l????R[h(2)+"9999"
                iSubclassCd = Integer.valueOf("9999");              
            }
            _subclasscd = iSubclassCd == null ? null : _classcd + _subClassCdFormat.format(iSubclassCd);
            _textbookcd = (String) map.get("KYOKA_CODE") + (String) map.get("TEXT_SHUMOKU_CODE") + (String) map.get("TEXT_NO");
            _orderSeq = new Integer(((Number) map.get("TEXT_KONYU_NO")).intValue());
            _getFlg = "1";  // TODO:?
        }
        
        public String toString() {
            return _shiganshaRenban+","+_year+","+_schregno+","+_classcd+","+_subclasscd+","+_textbookcd+","+_orderSeq;
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_TEXTBOOK_DAT

                                   XL[}  ^Cv               ? NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 
        CLASSCD                        SYSIBM    VARCHAR                   2     0 
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 
        TEXTBOOKCD                     SYSIBM    VARCHAR                   8     0 
        ORDER_SEQ                      SYSIBM    INTEGER                   4     0 
        GET_FLG                        SYSIBM    VARCHAR                   1     0 ?
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 ?
        UPDATED                        SYSIBM    TIMESTAMP                10     0 ?
        
          10 R[hI?B
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
                log.error("SQLException: " + schregTextbookDat);
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("}=" + totalCount);
    }

    private String getInsertSql(final SchregTextbookDat schregTextbookDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_TEXTBOOK_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregTextbookDat._year) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._schregno) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._classcd) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._curriculumCd) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._subclasscd) + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._textbookcd) + ", ");
        stb.append(" " + schregTextbookDat._orderSeq + ", ");
        stb.append(" " + getInsertVal(schregTextbookDat._getFlg) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

