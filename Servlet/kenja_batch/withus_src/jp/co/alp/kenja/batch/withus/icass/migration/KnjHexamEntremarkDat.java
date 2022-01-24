// kanji=漢字
/*
 * $Id: KnjHexamEntremarkDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HEXAM_ENTREMARK_DAT>を作る。
 * @author takaesu
 * @version $Id: KnjHexamEntremarkDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjHexamEntremarkDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjHexamEntremarkDat.class);

    public static final String ICASS_TABLE = "CHOSASHO_BIKO_KIROKU_SHOJIKO";
    
    public KnjHexamEntremarkDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "調査書進学用所見データ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        saveKnj(list);
    }
    
    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL文
        final String sql =
              " WITH MK AS (SELECT  "
            + "     '1' AS DIV, "
            + "     t1.NENDO_CODE,  "
            + "     t1.SHIGANSHA_RENBAN, "
            + "     VALUE(t1.SHUKKETSU_BIKO, '') AS SHUKKETSU_BIKO,  "
            + "     VALUE(t1.TOKUBETSU_KATSUDO_KIROKU, '') AS TOKUBETSU_KATSUDO_KIROKU,  "
            + "     VALUE(t1.SHIDOJO_SHOJIKO, '') AS SHIDOJO_SHOJIKO "
            + " FROM  "
            + "     CHOSASHO_BIKO_KIROKU_SHOJIKO t1, "
            + "     seito "
            + " WHERE "
            + "     t1.SHIGANSHA_RENBAN = seito.SHIGANSHA_RENBAN  "
            + "     AND VALUE(SEITO.SEITO_NO, '') <> ''  "
            + " ), MK2 AS ( "
            + "  "
            + " SELECT  "
            + "     '2' AS DIV, "
            + "     t1.NENDO AS NENDO_CODE,  "
            + "     t1.SHIGANSHA_RENBAN,  "
            + "     MAX(CASE WHEN VALUE(MK.SHUKKETSU_BIKO, '') = '' "
            + "              THEN t1.SHUKKETSU_BIKO "
            + "              ELSE MK.SHUKKETSU_BIKO "
            + "         END "
            + "     ) AS SHUKKETSU_BIKO,  "
            + "     MAX(CASE WHEN VALUE(MK.TOKUBETSU_KATSUDO_KIROKU, '') = '' "
            + "              THEN t1.TOKUBETSU_KATSUDO_KIROKU "
            + "              ELSE MK.TOKUBETSU_KATSUDO_KIROKU "
            + "         END "
            + "     ) AS TOKUBETSU_KATSUDO_KIROKU,  "
            + "     MAX(CASE WHEN VALUE(MK.SHIDOJO_SHOJIKO, '') = '' "
            + "              THEN t1.SHIDOJO_SHOJIKO "
            + "              ELSE MK.SHIDOJO_SHOJIKO "
            + "         END "
            + "     ) AS SHIDOJO_SHOJIKO "
            + " FROM  "
            + "     SEITO_TAKO_ZAISEKI_RIREKI_MEIS t1 "
            + "     LEFT JOIN MK ON MK.SHIGANSHA_RENBAN = t1.SHIGANSHA_RENBAN "
            + "          AND MK.NENDO_CODE = t1.NENDO, "
            + "     seito    "
            + " WHERE "
            + "     t1.SHIGANSHA_RENBAN = seito.SHIGANSHA_RENBAN  "
            + "     AND VALUE(SEITO.SEITO_NO, '') <> ''  "
            + "     AND NOT EXISTS( "
            + "         SELECT "
            + "             'x' "
            + "         FROM "
            + "             MK "
            + "         WHERE "
            + "             MK.SHUKKETSU_BIKO <> '' "
            + "             AND MK.TOKUBETSU_KATSUDO_KIROKU <> ''  "
            + "             AND MK.SHIDOJO_SHOJIKO <> '' "
            + "             AND MK.NENDO_CODE = t1.NENDO "
            + "             AND MK.SHIGANSHA_RENBAN = t1.SHIGANSHA_RENBAN "
            + "     ) "
            + " GROUP BY  "
            + "     t1.NENDO,  "
            + "     t1.SHIGANSHA_RENBAN "
            + " ), MAIN_T AS ( "
            + " SELECT "
            + "     * "
            + " FROM "
            + "     MK "
            + " UNION "
            + " SELECT "
            + "     * "
            + " FROM "
            + "     MK2 "
            + " ) "
            + " SELECT  "
            + "     NENDO_CODE,  "
            + "     SHIGANSHA_RENBAN,  "
            + "     MAX(SHUKKETSU_BIKO) AS SHUKKETSU_BIKO,  "
            + "     MAX(TOKUBETSU_KATSUDO_KIROKU) AS TOKUBETSU_KATSUDO_KIROKU,  "
            + "     MAX(SHIDOJO_SHOJIKO) AS SHIDOJO_SHOJIKO"
            + " FROM "
            + "     MAIN_T "
            + " GROUP BY "
            + "     NENDO_CODE, "
            + "     SHIGANSHA_RENBAN  "
            ;
        log.debug("sql=" + sql);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final ChosashoBikoKirokuShojiko record = new ChosashoBikoKirokuShojiko(_param, map);
            rtn.add(record);
        }
        return rtn;
    }
    
    private static class ChosashoBikoKirokuShojiko {
        final Param _param;
        
        final String _shiganshaRenban;
        final String _nendoCode;
        final String _shukketsuBiko;
        final String _tokubetsuKatsudoKiroku;
        final String _shidojoShojiko;
        
        private ChosashoBikoKirokuShojiko(Param param, final Map map) {
            _param = param;
            
            _shiganshaRenban = (String) map.get("shigansha_renban");   
            _nendoCode =  (String) map.get("nendo_code");
            
            String shukketu = (String) map.get("shukketsu_biko");
            _shukketsuBiko = (null != shukketu && shukketu.length() > 79) ? shukketu.substring(0,100) : shukketu;
            String special = (String) map.get("tokubetsu_katsudo_kiroku");
            _tokubetsuKatsudoKiroku = (null != special && special.length() > 100) ? special.substring(0,100) : special;
            String shido = (String) map.get("shidojo_shojiko");
            _shidojoShojiko = (null != shido && shido.length() > 227) ? shido.substring(0,100) : shido;
    
        }
        public Object[] toRecTestDat() {
            final String schregno = _param.getSchregno(_shiganshaRenban);

            final Object[] rtn = {
                    _nendoCode,
                    schregno,
                    "00",
                    _shukketsuBiko,
                    _tokubetsuKatsudoKiroku,
                    _shidojoShojiko,
                    Param.REGISTERCD,
            };
            return rtn;
        
        }        
    }
        

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;

        final String sql = "INSERT INTO hexam_entremark_dat VALUES(?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final ChosashoBikoKirokuShojiko cbks = (ChosashoBikoKirokuShojiko) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, cbks.toRecTestDat());
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT件数が1件以外!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("賢者へのINSERTでエラー", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("挿入件数=" + totalCount);
    }
}
// eof

