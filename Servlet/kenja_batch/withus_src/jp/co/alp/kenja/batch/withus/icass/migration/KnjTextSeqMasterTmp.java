// kanji=����
/*
 * $Id: KnjTextSeqMasterTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/09/04 17:05:35 - JST
 * �쐬��: m-yama
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
 * TEXT_SEQ_MASTER_TMP�����B
 * @author m-yama
 * @version $Id: KnjTextSeqMasterTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextSeqMasterTmp extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjGakuhiMasterTmp.class);

    public static final String ICASS_TABLE = "SEITO_TEXT_KONYU";

    public KnjTextSeqMasterTmp() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�e�L�X�g�V�[�P���X�}�X�^TMP"; }

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
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final TextSeqMasterTmp textSeqMasterTmp = new TextSeqMasterTmp(map);
            rtn.add(textSeqMasterTmp);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    NENDO_CODE, ");
        stb.append("    TEXT_KONYU_NO, ");
        stb.append("    ROW_NUMBER() OVER(ORDER BY TEXT_HATCHU_DATE, INT(TEXT_KONYU_NO)) AS ORDER_SEQ ");
        stb.append("FROM ");
        stb.append("    SEITO_TEXT_KONYU ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class TextSeqMasterTmp {
        final String _nendoCode;
        final String _textKonyuNo;
        final Integer _orderSeq;

        public TextSeqMasterTmp(final Map map) {
            _nendoCode   = (String) map.get("NENDO_CODE");
            _textKonyuNo = (String) map.get("TEXT_KONYU_NO");
            _orderSeq    = new Integer(((Number) map.get("ORDER_SEQ")).intValue());
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table TEXT_SEQ_MASTER_TMP

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        NENDO_CODE                     SYSIBM    VARCHAR                   4     0 ������
        TEXT_KONYU_NO                  SYSIBM    VARCHAR                   4     0 ������
        ORDER_SEQ                      SYSIBM    INTEGER                   4     0 ������
        
          3 ���R�[�h���I������܂����B
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final TextSeqMasterTmp textSeqMasterTmp = (TextSeqMasterTmp) it.next();
            final String insSql = getInsertSql(textSeqMasterTmp);
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

    private String getInsertSql(final TextSeqMasterTmp textSeqMasterTmp) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO TEXT_SEQ_MASTER_TMP ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(textSeqMasterTmp._nendoCode) + ", ");
        stb.append(" " + getInsertVal(textSeqMasterTmp._textKonyuNo) + ", ");
        stb.append(" " + getInsertVal(textSeqMasterTmp._orderSeq) + " ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof
