// kanji=����
/*
 * $Id: KnjSchregTransferDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
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
 * SCHREG_TRANSFER_DAT�����B
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
    String getTitle() { return "�w�Јٓ��f�[�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());

        try {
            saveKnj(list);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("�X�V�������ɃG���[! rollback �����B");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL���s
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: �f�[�^�ʂ������̂ň��Ă��܂��B�΍���l����!
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
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
            _abroadClassdays = "";  // TODO:����
            _abroadCredits = "";    // TODO:����
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_TRANSFER_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
        TRANSFERCD                     SYSIBM    VARCHAR                   2     0 ������
        TRANSFER_SDATE                 SYSIBM    DATE                      4     0 ������
        TRANSFER_EDATE                 SYSIBM    DATE                      4     0 �͂�
        TRANSFERREASON                 SYSIBM    VARCHAR                  75     0 �͂�
        TRANSFERPLACE                  SYSIBM    VARCHAR                  60     0 �͂�
        TRANSFERADDR                   SYSIBM    VARCHAR                  75     0 �͂�
        ABROAD_CLASSDAYS               SYSIBM    SMALLINT                  2     0 �͂�
        ABROAD_CREDITS                 SYSIBM    SMALLINT                  2     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          11 ���R�[�h���I������܂����B
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
        log.warn("�}������=" + totalCount);
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

