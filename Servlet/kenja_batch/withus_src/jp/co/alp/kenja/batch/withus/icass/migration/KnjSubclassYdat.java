// kanji=����
/*
 * $Id: KnjSubclassYdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: <���҂̃e�[�u�����ɏ��������Ă��������B��) REC_REPORT_DAT>�����B
 * @author takaesu
 * @version $Id: KnjSubclassYdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSubclassYdat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSubclassYdat.class);

    public static final String ICASS_TABLE = "KAISETSU_KAMOKU";
    
    public KnjSubclassYdat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�ȖڔN�x�f�[�^"; }


    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());

        saveKnj(list);
    }
    
    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL��
        final String sql;
        sql = "SELECT " +
        "     T1.* ," +
        "     L1.NAMECD2 AS CURRICULUM_CD " +        
        "FROM " + ICASS_TABLE + " T1 " +
        "    LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'W002' "+
        "        AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L1.NAMESPARE1 AND L1.NAMESPARE2";
            ;
        log.debug("sql=" + sql);

        // SQL���s
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: �f�[�^�ʂ������̂ň��Ă��܂��B�΍���l����!
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final Kyoka record = new Kyoka(_param, map);
            rtn.add(record);
        }
        return rtn;
    }
    
    private static class Kyoka {
        final Param _param;
        
        final String _nendoCode;
        final String _kyouikukateiTekiyoNendoCode;
        final String _kyokaCode;
        final String _kamokuCode;
        
        private Kyoka(Param param, final Map map) {
            _param = param;
            
            _nendoCode = (String) map.get("nendo_code");   
            _kyouikukateiTekiyoNendoCode = (String) map.get("CURRICULUM_CD");
            _kyokaCode = (String) map.get("kyoka_code");
            _kamokuCode = (String) map.get("kamoku_code");
        }
        public Object[] toRecTestDat() {
            String subClassCd = _kyokaCode + _subClassCdFormat.format(Integer.valueOf(_kamokuCode));
            
            final Object[] rtn = {
                   _nendoCode,
                   _kyokaCode,
                   _kyouikukateiTekiyoNendoCode,
                   subClassCd,
                   Param.REGISTERCD,
            };
            return rtn;
        
        }
        
        public String toString(){
            String subClassCd = _subClassCdFormat.format(Integer.valueOf(_kamokuCode));
            return _nendoCode+","+_kyokaCode+","+_kyouikukateiTekiyoNendoCode+","+subClassCd+","+Param.REGISTERCD;
        }
    }
        

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;

        final String sql = "INSERT INTO subclass_ydat VALUES(?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Kyoka k = (Kyoka) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, k.toRecTestDat());
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT������1���ȊO!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("���҂ւ�INSERT�ŃG���[", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("�}������=" + totalCount);
    }
}
// eof

