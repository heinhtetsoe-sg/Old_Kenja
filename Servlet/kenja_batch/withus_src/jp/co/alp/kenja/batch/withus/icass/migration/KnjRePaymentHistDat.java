// kanji=����
/*
 * $Id: KnjRePaymentHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * RE_PAYMENT_HIST_DAT�����B
 * @author takaesu
 * @version $Id: KnjRePaymentHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRePaymentHistDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjRePaymentHistDat.class);
    public static final DecimalFormat _shiganshaRenbanFormat = new DecimalFormat("000000");
    private static final String KNJTABLE = "RE_PAYMENT_HIST_DAT";

    public KnjRePaymentHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�ԋ������f�[�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("�f�[�^����=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);
    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MIN_DATE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     MIN(T1.KEIJO_NENTSUKI) AS KEIJO_NENTSUKI ");
        stb.append(" FROM ");
        stb.append("     SEITO_URIAGE T1, ");
        stb.append("     SEITO T2 ");
        stb.append(" WHERE ");
        stb.append("     URIAGE_NENTSUKI >= '2008-04-01' ");
        stb.append("     AND T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T2.SHIGANSHA_NO IS NOT NULL ");
        stb.append("     AND T1.KEIJO_NENTSUKI IS NOT NULL ");
        stb.append("     AND T1.KEIJO_NENTSUKI <> '' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN ");
        stb.append(" ORDER BY ");
        stb.append("     INT(SHIGANSHA_RENBAN) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUHI_NONYU_DATE, ");
        stb.append("     ROW_NUMBER() OVER(PARTITION BY T1.SHIGANSHA_RENBAN) AS INQUIRY_NO, ");
        stb.append("     ABS(SUM(INT(T1.GAKUHI_NONYU_KINGAKU))) AS PAYMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     SEITO_GAKUHI_NONYU_JISSEKI T1, ");
        stb.append("     MIN_DATE T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND DATE(T1.GAKUHI_NONYU_DATE) >= DATE(T2.KEIJO_NENTSUKI) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUHI_NONYU_DATE ");
        stb.append(" HAVING ");
        stb.append("     SUM(INT(T1.GAKUHI_NONYU_KINGAKU)) < 0 ");
        stb.append(" ORDER BY ");
        stb.append("     INT(T1.SHIGANSHA_RENBAN), ");
        stb.append("     DATE(T1.GAKUHI_NONYU_DATE) ");

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

    /*
     * [db2inst1@withus script]$ db2 describe table RE_PAYMENT_HIST_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        RE_PAY_DATE                    SYSIBM    DATE                      4     0 ������
        RE_PAY_DIV                     SYSIBM    VARCHAR                   2     0 ������
        INQUIRY_NO                     SYSIBM    VARCHAR                   6     0 ������
        RE_PAYMENT                     SYSIBM    INTEGER                   4     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�

          7 ���R�[�h���I������܂����B
    */
    
    public Object[] mapToArray(Map map) {
        final String shiganshaRenBan = (String) map.get("SHIGANSHA_RENBAN");
        final Object[] rtn = {
                _param.getApplicantNo(shiganshaRenBan),
                map.get("GAKUHI_NONYU_DATE"),
                "01",       // TODO:�Œ�
                new Integer(((Number) map.get("INQUIRY_NO")).intValue()),
                new Integer(((Number) map.get("PAYMENT_MONEY")).intValue()),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

