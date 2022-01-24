// kanji=漢字
/*
 * $Id: KnjClassMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * CLASS_MST を作る。
 * @author takaesu
 * @version $Id: KnjClassMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClassMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClassMst.class);

    public static final String ICASS_TABLE = "KYOKA";
    
    public KnjClassMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "教科マスタ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        saveKnj(list);
    }
    
    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL文
        final String sql=
            "SELECT"+
            "    T1.kyoka_code,"+
            "    T1.kyoka_name,"+
            "    T1.kyoka_r_name,"+
            "    row_number() over() AS ROW_NUMBER" +
            "    FROM" +
            "    " + ICASS_TABLE  + " T1 "+
            "    GROUP BY" +
            "       T1.kyoka_code,"+
            "       T1.kyoka_name,"+
            "       T1.kyoka_r_name"
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

            final Kyoka record = new Kyoka(_param, map);
            rtn.add(record);
        }
        return rtn;
    }
    
    private static class Kyoka {
        final Param _param;
        
        final String _kyoukaCode;
        final String _kyoukaName;
        final String _kyoukaRName;
        final String _levelSu;
        final String _futsuKyokaFlag;
        final Long _outJuni;
        
        private Kyoka(Param param, final Map map) {
            _param = param;
            
            _kyoukaCode = (String) map.get("kyoka_code");   
            _kyoukaName =  (String) map.get("kyoka_name");
            _kyoukaRName = (String) map.get("kyoka_r_name");
            _levelSu = (String) map.get("level_su");
            _futsuKyokaFlag = (String) map.get("futsu_kyoka_flag");
            _outJuni = (Long) map.get("ROW_NUMBER");
        }
        public Object[] toRecTestDat() {

            final Object[] rtn = {
                   _kyoukaCode,
                   _kyoukaName,
                   _kyoukaRName,
                   null,
                   null,
                   null,
                   null,
                   null,
                   _outJuni,
                   _outJuni,
                   _outJuni,
                   null,
                    Param.REGISTERCD,
            };
            return rtn;
        
        }
        
        public String toString(){
            return _kyoukaCode+","+_kyoukaName+","+_kyoukaRName+","+_outJuni+",Param.REGISTERCD";
        }
    }
        

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;

        final String sql = "INSERT INTO class_mst VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Kyoka k = (Kyoka) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, k.toRecTestDat());
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

