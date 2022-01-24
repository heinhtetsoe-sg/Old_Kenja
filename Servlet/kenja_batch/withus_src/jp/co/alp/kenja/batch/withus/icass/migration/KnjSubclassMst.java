// kanji=����
/*
 * $Id: KnjSubclassMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * @version $Id: KnjSubclassMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSubclassMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSubclassMst.class);

    public static final String ICASS_TABLE = "KAMOKU";
    
    public KnjSubclassMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�Ȗڃ}�X�^"; }

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
//        " T1.* ," +
        "     T1.KYOKA_CODE ," +
        "     T1.KAMOKU_CODE ," +
        "     T1.KAMOKU_NAME ," +
        "     T1.KAMOKU_R_NAME ," +
        "     T1.HITSURISHU_SENTAKU_CODE ," +
        "     row_number() over() AS ROW_NUMBER ," +
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
    
    
    private class Kyoka {
        final Param _param;
        
        final String _kyoikukateiTekiyoNendoCode;
        final String _kyokaCode;
        final String _kamokuCode;
        final String _kamokuName;
        final String _kamokuRName;
        final String _hitsurishuSentakuCode;
        final Long _outJuni;
        
        private Kyoka(Param param, final Map map) {
            _param = param;
            
            _kyoikukateiTekiyoNendoCode = (String) map.get("CURRICULUM_CD");
            _kyokaCode = (String) map.get("KYOKA_CODE");
            _kamokuCode = (String) map.get("KAMOKU_CODE");
            _kamokuName = (String) map.get("KAMOKU_NAME");
            _kamokuRName = (String) map.get("KAMOKU_R_NAME");
            _hitsurishuSentakuCode = (String) map.get("HITSURISHU_SENTAKU_CODE");   
            _outJuni = (Long) map.get("ROW_NUMBER");
        }

        public Object[] toRecTestDat() {
            String subClassCd = _kyokaCode + _subClassCdFormat.format(Integer.valueOf(_kamokuCode));
            String[] subClassAbbvArray = retDividString(_kamokuRName, 5, 1);
            String subClassAbbv = subClassAbbvArray[0];
            final Object[] rtn = {
                    _kyokaCode,
                    _kyoikukateiTekiyoNendoCode,
                    subClassCd,                    
                    _kamokuName,
                    subClassAbbv,
                    null,
                    null,
                    null,
                    null,
                    null,
                    _outJuni,
                    null,
                    null,
                    null,
                    null,
                    "0", // ���O�敪��"0"
                    Param.REGISTERCD,
            };
            return rtn;
        
        }        
    }
        

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;

        final String sql = "INSERT INTO subclass_mst VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

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

