// kanji=����
/*
 * $Id: KnjClaimDetailsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * CLAIM_DETAILS_DAT�����B
 * @author takaesu
 * @version $Id: KnjClaimDetailsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClaimDetailsDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClaimDetailsDat.class);

    private static final String KNJTABLE = "CLAIM_DETAILS_DAT";

    private static DecimalFormat _seqFormat = new DecimalFormat("00");

    private static DecimalFormat _claimNoFormat = new DecimalFormat("000000");

    public KnjClaimDetailsDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�`�[���׃f�[�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("�f�[�^����=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

        updateSalesPlan();
    }

    /**
     * 
     */
    private void updateSalesPlan() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" UPDATE ");
        stb.append("     SALES_PLAN_DAT ");
        stb.append(" SET ");
        stb.append("     SUMMING_UP_MONEY = TOTAL_CLAIM_MONEY ");
        stb.append(" WHERE ");
        stb.append("     SUMMING_UP_DATE IS NOT NULL ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
        } catch (SQLException e) {
            log.error("SQLException", e);
        }

    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.COMMODITY_CD, ");
        stb.append("     CASE WHEN MAX(L1.DIVIDING_MULTIPLICATION_DIV) = '2' ");
        stb.append("          THEN 1 ");
        stb.append("          ELSE SUM(T1.TOTAL_CLAIM_MONEY) / MAX(L1.INCLUDING_TAX_PRICE) ");
        stb.append("     END AS AMOUNT, ");
        stb.append("     MIN(T1.SUMMING_UP_DATE) AS CLAIM_DATE, ");
        stb.append("     SUM(T1.TOTAL_CLAIM_MONEY) AS TOTAL_CLAIM_MONEY, ");
        stb.append("     CASE WHEN MAX(L1.DIVIDING_MULTIPLICATION_DIV) = '2' ");
        stb.append("          THEN SUM(T1.TOTAL_CLAIM_MONEY) ");
        stb.append("          ELSE MAX(T1.PRICE) * (SUM(T1.TOTAL_CLAIM_MONEY) / MAX(L1.INCLUDING_TAX_PRICE)) ");
        stb.append("     END AS TOTAL_PRICE, ");
        stb.append("     CASE WHEN MAX(L1.DIVIDING_MULTIPLICATION_DIV) = '2' ");
        stb.append("          THEN SUM(T1.PRICE) ");
        stb.append("          ELSE MAX(T1.PRICE) ");
        stb.append("     END AS PRICE, ");
        stb.append("     CASE WHEN MAX(L1.DIVIDING_MULTIPLICATION_DIV) = '2' ");
        stb.append("          THEN SUM(T1.PRICE) + SUM(T1.TAX) ");
        stb.append("          ELSE MAX(T1.PRICE) + MAX(T1.TAX) ");
        stb.append("     END AS TOTAL_PRICE, ");
        stb.append("     CASE WHEN MAX(L1.DIVIDING_MULTIPLICATION_DIV) = '2' ");
        stb.append("          THEN SUM(T1.TAX) ");
        stb.append("          ELSE MAX(T1.TAX) ");
        stb.append("     END AS TAX, ");
        stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS S_YEAR_MONTH, ");
        stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS E_YEAR_MONTH, ");
        stb.append("     CASE WHEN MAX(L1.DIVIDING_MULTIPLICATION_DIV) = '2' ");
        stb.append("          THEN T1.SEQ ");
        stb.append("          ELSE '99' ");
        stb.append("     END AS PRIORITY_LEVEL, ");
        stb.append("     SUM(SUMMING_UP_MONEY) AS PAYMENT_MONEY, ");
        stb.append("     MAX(SUMMING_UP_DATE) AS PAYMENT_DATE, ");
        stb.append("     SUM(SUMMING_UP_MONEY) AS SUMMING_UP_MONEY, ");
        stb.append("     MAX(SUMMING_UP_DATE) AS SUMMING_UP_DATE ");
        stb.append(" FROM ");
        stb.append("     SALES_PLAN_DAT T1 ");
        stb.append("     LEFT JOIN COMMODITY_MST L1 ON T1.COMMODITY_CD = L1.COMMODITY_CD ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.COMMODITY_CD ");
        stb.append(" ORDER BY ");
        stb.append("     COMMODITY_CD ");

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

    /**
        [db2inst1@withus db2inst1]$ db2 describe table CLAIM_DETAILS_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 ������
        SEQ                            SYSIBM    VARCHAR                   2     0 ������
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 ������
        AMOUNT                         SYSIBM    VARCHAR                   2     0 �͂�
        CLAIM_DATE                     SYSIBM    DATE                      4     0 �͂�
        TOTAL_CLAIM_MONEY              SYSIBM    INTEGER                   4     0 �͂�
        PRICE                          SYSIBM    INTEGER                   4     0 �͂�
        TOTAL_PRICE                    SYSIBM    INTEGER                   4     0 �͂�
        TAX                            SYSIBM    INTEGER                   4     0 �͂�
        S_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 ������
        E_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 ������
        PRIORITY_LEVEL                 SYSIBM    VARCHAR                   2     0 �͂�
        PAYMENT_MONEY                  SYSIBM    INTEGER                   4     0 �͂�
        PAYMENT_DATE                   SYSIBM    DATE                      4     0 �͂�
        SUMMING_UP_MONEY               SYSIBM    INTEGER                   4     0 �͂�
        SUMMING_UP_DATE                SYSIBM    DATE                      4     0 �͂�
        DUMMY_FLG                      SYSIBM    VARCHAR                   1     0 �͂�
        REMARK                         SYSIBM    VARCHAR                 150     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�

          21 ���R�[�h���I������܂����B
     */
    public Object[] mapToArray(final Map map) {

        final Object[] rtn = {
                map.get("SLIP_NO"),
                map.get("SEQ"),
                map.get("APPLICANTNO"),
                map.get("COMMODITY_CD"),
                map.get("AMOUNT"),
                map.get("CLAIM_DATE"),
                map.get("TOTAL_CLAIM_MONEY"),
                map.get("PRICE"),
                map.get("TOTAL_CLAIM_MONEY"),
                map.get("TAX"),
                map.get("S_YEAR_MONTH"),
                map.get("E_YEAR_MONTH"),
                map.get("PRIORITY_LEVEL"),
                map.get("PAYMENT_MONEY"),
                map.get("PAYMENT_DATE"),
                map.get("SUMMING_UP_MONEY"),
                map.get("SUMMING_UP_DATE"),
                null,
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

