// kanji=漢字
/*
 * $Id: KnjPartnerMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
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
 * PARTNER_MSTを作る。
 * @author takaesu
 * @version $Id: KnjPartnerMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjPartnerMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjPartnerMst.class);

    public static final String ICASS_TABLE = "GAKUSHU_KYOTEN";
    public static final DecimalFormat _partnerFormat = new DecimalFormat("0000");

    public KnjPartnerMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "提携先マスタ"; }

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

            final PartnerMst partnerMst = new PartnerMst(map);
            rtn.add(partnerMst);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     ROW_NUMBER() OVER() AS RENBAN, ");
        stb.append("     GAKUSHU_KYOTEN_CODE, ");
        stb.append("     GAKUSHU_KYOTEN_NAME, ");
        stb.append("     YUBIN_NO, ");
        stb.append("     TODOFUKEN_NO, ");
        stb.append("     ADDRESS1, ");
        stb.append("     ADDRESS2, ");
        stb.append("     TEL_NO ");
        stb.append(" FROM ");
        stb.append("     GAKUSHU_KYOTEN ");
        stb.append(" WHERE  ");
        stb.append("     GAKUSHU_KYOTEN_CODE > '050'  ");
        stb.append(" ORDER BY ");
        stb.append("     GAKUSHU_KYOTEN_CODE ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class PartnerMst {
        final String _partnerCd;
        final String _partnerName;
        final String _partnerZipcd;
        final String _partnerPrefCd;
        final String _partnerAddr1;
        final String _partnerAddr2;
        final String _partnerAddr3;
        final String _partnerTelno;

        public PartnerMst(final Map map) {
            if (map.get("RENBAN") != null) {
                final Long partnerCd = (Long) map.get("RENBAN");
                _partnerCd = _partnerFormat.format(Integer.parseInt(partnerCd.toString()));
            } else {
                _partnerCd = null;
            }
            _partnerName = (String) map.get("GAKUSHU_KYOTEN_NAME");
            _partnerZipcd = (String) map.get("YUBIN_NO");
            _partnerPrefCd = (String) map.get("TODOFUKEN_NO");
            final String[] addr = divideStr((String) map.get("ADDRESS1"));
            _partnerAddr1 = addr[0];
            _partnerAddr2 = addr[1];
            _partnerAddr3 = (String) map.get("ADDRESS2");
            _partnerTelno = (String) map.get("TEL_NO");
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table PARTNER_MST

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        PARTNER_CD                     SYSIBM    VARCHAR                   4     0 いいえ
        PARTNER_NAME                   SYSIBM    VARCHAR                  90     0 はい
        PARTNER_ZIPCD                  SYSIBM    VARCHAR                   8     0 はい
        PARTNER_PREF_CD                SYSIBM    VARCHAR                   2     0 はい
        PARTNER_ADDR1                  SYSIBM    VARCHAR                  75     0 はい
        PARTNER_ADDR2                  SYSIBM    VARCHAR                  75     0 はい
        PARTNER_ADDR3                  SYSIBM    VARCHAR                  75     0 はい
        PARTNER_TELNO                  SYSIBM    VARCHAR                  14     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          10 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final PartnerMst partnerMst = (PartnerMst) it.next();
            final String insSql = getInsertSql(partnerMst);
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

    private String getInsertSql(final PartnerMst partnerMst) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO PARTNER_MST ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(partnerMst._partnerCd) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerName) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerZipcd) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerPrefCd) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerAddr1) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerAddr2) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerAddr3) + ", ");
        stb.append(" " + getInsertVal(partnerMst._partnerTelno) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

