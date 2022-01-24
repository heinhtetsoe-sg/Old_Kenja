// kanji=����
/*
 * $Id: KnjHexamEmpremarkDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
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
 * TODO: <���҂̃e�[�u�����ɏ��������Ă��������B��) REC_REPORT_DAT>�����B
 * @author takaesu
 * @version $Id: KnjHexamEmpremarkDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjHexamEmpremarkDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjHexamEmpremarkDat.class);

    public static final String ICASS_TABLE = "CHOSASHO_BIKO_KIROKU_SHOJIKO";
    
    public KnjHexamEmpremarkDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�������A�E�p�����f�[�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());

        saveKnj(list);
    }
    
    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL��
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
        
        private ChosashoBikoKirokuShojiko(Param param, final Map map) {
            _param = param;
            
            _shiganshaRenban = (String) map.get("shigansha_renban");   
            _nendoCode =  (String) map.get("nendo_code");
            _shukketsuBiko = (String) map.get("shukketsu_biko");
            _tokubetsuKatsudoKiroku = (String) map.get("tokubetsu_katsudo_kiroku");
    
        }
        public Object[] toRecTestDat() {
            final String schregno = _param.getSchregno(_shiganshaRenban);

            final Object[] rtn = {
                    _nendoCode,
                    schregno,
                    null,
                    _tokubetsuKatsudoKiroku,
                    _shukketsuBiko,
                    Param.REGISTERCD,
            };
            return rtn;
        
        }        
    }
        

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;

        final String sql = "INSERT INTO hexam_empremark_dat VALUES(?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final ChosashoBikoKirokuShojiko cbks = (ChosashoBikoKirokuShojiko) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, cbks.toRecTestDat());
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

