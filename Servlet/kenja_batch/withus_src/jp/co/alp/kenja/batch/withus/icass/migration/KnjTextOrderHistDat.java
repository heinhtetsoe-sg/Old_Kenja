// kanji=����
/*
 * $Id: KnjTextOrderHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TEXT_ORDER_HIST_DAT�����B
 * @author takaesu
 * @version $Id: KnjTextOrderHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextOrderHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjTextOrderHistDat.class);
    static final String KNJTABLE = "TEXT_ORDER_HIST_DAT";

    public KnjTextOrderHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���ȏ������Ǘ��f�[�^"; }

    void migrate() throws SQLException {
        final String sql = getSql();
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }
        _runner.listToKnj(result, KNJTABLE, this);
        log.debug("�f�[�^����" + result.size());
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table text_order_hist_dat

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        ORDER_SEQ                      SYSIBM    INTEGER                   4     0 ������
        ORDER_DATE                     SYSIBM    DATE                      4     0 ������
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          4 ���R�[�h���I������܂����B
     */

    private String getSql() {
        /*TEXT_KONYU_NO �����Ԃ��Ă킢���Ȃ��̂� TEXT_HATCHU_DATE ���Ƃ肠����MAX���Ƃ��Ă��܂��B
        :TAKARA �ǋL(2008/10/03)�A
        �A�Ԃ��ӂ����e�[�u������ORDER_SEQ���Q�Ƃ��Ă��邽�߁A
        GROUP BY�����Ȃ��悤�ɕύX���܂����B
        */
        final String resql = " SELECT "
                            + "     L1.ORDER_SEQ AS TEXT_KONYU_NO, "
                            + "     T1.TEXT_HATCHU_DATE "
                            + " FROM "
                            + "     SEITO_TEXT_KONYU T1"
                            + " LEFT JOIN TEXT_SEQ_MASTER_TMP L1 ON L1.NENDO_CODE = T1.NENDO_CODE"
                            + "     AND L1.TEXT_KONYU_NO = T1.TEXT_KONYU_NO"
                            + " WHERE "
                            + "     T1.TEXT_HATCHU_DATE IS NOT NULL ";

        log.debug(resql);
        return resql;
    }

    public Object[] mapToArray(Map map) {
        final Object[] rtn = {
                map.get("TEXT_KONYU_NO"),
                map.get("TEXT_HATCHU_DATE"),
                Param.REGISTERCD,
        };

        return rtn;
    }
}
// eof

