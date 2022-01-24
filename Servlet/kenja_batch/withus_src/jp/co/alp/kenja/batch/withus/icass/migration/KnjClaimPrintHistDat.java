// kanji=����
/*
 * $Id: KnjClaimPrintHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CLAIM_PRINT_HIST_DAT�����B
 * @author takaesu
 * @version $Id: KnjClaimPrintHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClaimPrintHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClaimPrintHistDat.class);

    private static final String KNJTABLE = "CLAIM_PRINT_HIST_DAT";

    private static DecimalFormat _claimNoFormat = new DecimalFormat("08000000");

    public KnjClaimPrintHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���������s�����f�[�^"; }


    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("�f�[�^����=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ROW_NUMBER() OVER() AS CLAIM_NO, ");
        stb.append("     SLIP_NO, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     CLAIM_DATE, ");
        stb.append("     TOTAL_CLAIM_MONEY, ");
        stb.append("     Add_days(DATE(CLAIM_DATE), 14) AS TIMELIMIT_DAY ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DAT ");
        stb.append(" ORDER BY ");
        stb.append("     CLAIM_DATE, ");
        stb.append("     APPLICANTNO ");
        stb.append("  ");

        log.debug("sql=" + stb);

        // SQL���s
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }
        return result;
    }

    /** [db2inst1@withus db2inst1]$ db2 describe table CLAIM_PRINT_HIST_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        CLAIM_NO                       SYSIBM    VARCHAR                   8     0 ������
        SEQ                            SYSIBM    VARCHAR                   2     0 ������
        REISSUE_CNT                    SYSIBM    VARCHAR                   2     0 ������
        RE_CLAIM_CNT                   SYSIBM    VARCHAR                   2     0 ������
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 ������
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        RE_CLAIM_NO                    SYSIBM    VARCHAR                   8     0 �͂�
        CLAIM_DATE                     SYSIBM    DATE                      4     0 �͂�
        CLAIM_MONEY                    SYSIBM    INTEGER                   4     0 �͂�
        TIMELIMIT_DAY                  SYSIBM    DATE                      4     0 �͂�
        FORM_NO                        SYSIBM    VARCHAR                   2     0 �͂�
        REMARK                         SYSIBM    VARCHAR                 150     0 �͂�
        CLAIM_NONE_FLG                 SYSIBM    VARCHAR                   1     0 �͂�
        COMPLETE_FLG                   SYSIBM    VARCHAR                   1     0 �͂�
        ABANDONMENT_FLG                SYSIBM    VARCHAR                   1     0 �͂�
        PROCEDURE_DIV                  SYSIBM    VARCHAR                   1     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          18 ���R�[�h���I������܂����B
     */
    public Object[] mapToArray(final Map map) {
        final int claimNo = ((Number) map.get("CLAIM_NO")).intValue();
        final Object[] rtn = {
                _claimNoFormat.format(claimNo),
                "01",    // TODO:�Œ�
                "01",    // TODO:�Œ�
                "01",    // TODO:�Œ�
                map.get("SLIP_NO"),
                map.get("APPLICANTNO"),
                null,
                map.get("CLAIM_DATE"),
                map.get("TOTAL_CLAIM_MONEY"),
                map.get("TIMELIMIT_DAY"),
                null,
                null,
                null,
                null,
                null,
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

