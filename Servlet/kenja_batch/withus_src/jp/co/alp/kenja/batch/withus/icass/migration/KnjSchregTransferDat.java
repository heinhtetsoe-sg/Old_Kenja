// kanji=漢字
/*
 * $Id: KnjSchregTransferDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * SCHREG_TRANSFER_DATを作る。
 * @author takaesu
 * @version $Id: KnjSchregTransferDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregTransferDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregTransferDat.class);

    public static final String ICASS_TABLE = "SEITO_GAKUSEKI_IDO_RIREKI";

    public KnjSchregTransferDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍異動データ"; }

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

            final SchregTransferDat schregTransferDat = new SchregTransferDat(map);
            rtn.add(schregTransferDat);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUSEKI_JOTAI_KAISHI_NENGAPPI, ");
        stb.append("     T1.GAKUSEKI_JOTAI_SHURYO_NENGAPPI, ");
        stb.append("     CASE VALUE(T1.GAKUSEKI_JOTAI_CODE, '') ");
        stb.append("          WHEN '1' THEN '2' ");
        stb.append("          WHEN '2' THEN '1' ");
        stb.append("          WHEN '4' THEN '3' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS TRANSFERCD, ");
        stb.append("     T1.GAKUSEKI_IDO_RIYU, ");
        stb.append("     T1.RYUGAKUSAKI_KUNI_NAME, ");
        stb.append("     T1.RYUGAKUSAKI_GAKKO_NAME ");
        stb.append(" FROM ");
        stb.append("     SEITO_GAKUSEKI_IDO_RIREKI T1, ");
        stb.append("     SEITO T2 ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T1.GAKUSEKI_JOTAI_CODE, '') IN ('1', '2', '4') ");
        stb.append("     AND T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class SchregTransferDat {
        final String _shiganshaRenban;
        final String _schregno;
        final String _transfercd;
        final String _transferSdate;
        final String _transferEdate;
        final String _transferreason;
        final String _transferplace;
        final String _transferaddr;
        final String _abroadClassdays;
        final String _abroadCredits;

        public SchregTransferDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _transfercd = (String) map.get("TRANSFERCD");
            _transferSdate = (String) map.get("GAKUSEKI_JOTAI_KAISHI_NENGAPPI");
            _transferEdate = (String) map.get("GAKUSEKI_JOTAI_SHURYO_NENGAPPI");
            final String transferreason = (String) map.get("GAKUSEKI_IDO_RIYU");
            if (transferreason != null && transferreason.length() > 25) {
                _transferreason = transferreason.substring(0,25);
            } else {
                _transferreason = transferreason;
            }
            _transferplace = (String) map.get("RYUGAKUSAKI_GAKKO_NAME");
            _transferaddr = (String) map.get("RYUGAKUSAKI_KUNI_NAME");
            _abroadClassdays = "";  // TODO:未定
            _abroadCredits = "";    // TODO:未定
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_TRANSFER_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        TRANSFERCD                     SYSIBM    VARCHAR                   2     0 いいえ
        TRANSFER_SDATE                 SYSIBM    DATE                      4     0 いいえ
        TRANSFER_EDATE                 SYSIBM    DATE                      4     0 はい
        TRANSFERREASON                 SYSIBM    VARCHAR                  75     0 はい
        TRANSFERPLACE                  SYSIBM    VARCHAR                  60     0 はい
        TRANSFERADDR                   SYSIBM    VARCHAR                  75     0 はい
        ABROAD_CLASSDAYS               SYSIBM    SMALLINT                  2     0 はい
        ABROAD_CREDITS                 SYSIBM    SMALLINT                  2     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          11 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SchregTransferDat schregTransferDat = (SchregTransferDat) it.next();
            final String insSql = getInsertSql(schregTransferDat);
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

    private String getInsertSql(final SchregTransferDat schregTransferDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_TRANSFER_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregTransferDat._schregno) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._transfercd) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._transferSdate) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._transferEdate) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._transferreason) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._transferplace) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._transferaddr) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._abroadClassdays) + ", ");
        stb.append(" " + getInsertVal(schregTransferDat._abroadCredits) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

