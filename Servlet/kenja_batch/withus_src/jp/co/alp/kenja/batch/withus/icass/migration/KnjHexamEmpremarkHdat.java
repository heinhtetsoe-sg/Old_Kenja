// kanji=漢字
/*
 * $Id: KnjHexamEmpremarkHdat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * TODO: <賢者のテーブル名に書き換えてください。例) REC_REPORT_DAT>を作る。
 * @author takaesu
 * @version $Id: KnjHexamEmpremarkHdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjHexamEmpremarkHdat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjHexamEmpremarkHdat.class);

    public static final String ICASS_TABLE = "SEITO_CHOSASHO_KISAI_JOHO";
    
    public KnjHexamEmpremarkHdat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "調査書進学用所見ヘッタデータ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        saveKnj(list);
    }
    
    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL文
        final String sql;
        sql = "SELECT "
            + " T1.* "
            + "FROM "
            + ICASS_TABLE + " T1, "
            + " SEITO T2"
            + " WHERE "
            + "     T1.shigansha_renban = T2.shigansha_renban "
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

            final SeitoChosashoKisaiJoho record = new SeitoChosashoKisaiJoho(_param, map);
            rtn.add(record);
        }
        return rtn;
    }
    
    private static class SeitoChosashoKisaiJoho {
        final Param _param;
        
        final String _shiganshaRenban;
        final String _chosho;
        
        private SeitoChosashoKisaiJoho(Param param, final Map map) {
            _param = param;
            
            _shiganshaRenban = (String) map.get("shigansha_renban");   
            _chosho =  (String) map.get("chosho");
    
        }
        public Object[] toRecTestDat() {
            final String schregno = _param.getSchregno(_shiganshaRenban);

            final Object[] rtn = {
                    schregno,
                    null,
                    _chosho,
                    null,
                    null,
                    Param.REGISTERCD,
            };
            return rtn;
        
        }        
    }
        

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;

        final String sql = "INSERT INTO hexam_empremark_hdat VALUES(?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SeitoChosashoKisaiJoho sckj = (SeitoChosashoKisaiJoho) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, sckj.toRecTestDat());
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

