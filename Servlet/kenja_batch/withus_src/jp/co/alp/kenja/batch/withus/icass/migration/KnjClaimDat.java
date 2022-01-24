// kanji=����
/*
 * $Id: KnjClaimDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CLAIM_DAT�����B
 * @author takaesu
 * @version $Id: KnjClaimDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClaimDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClaimDat.class);

    final public static DecimalFormat _slipFormat = new DecimalFormat("000000");
    private static DecimalFormat _claimNoFormat = new DecimalFormat("00000000");
    private static DecimalFormat _monthFormat = new DecimalFormat("00");

    private static final String KNJTABLE = "CLAIM_DAT";
    private static final String COMMODITY_MENJO = "90001";

    public KnjClaimDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�`�[�f�[�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("�f�[�^����=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

        makeMenjoClaimData();

    }

    /**
     * ���Ɨ��Ə��̓`�[�쐬
     */
    private void makeMenjoClaimData() throws SQLException {
        final String maxClaimSql = "SELECT MAX(CLAIM_NO) AS CLAIM_NO FROM CLAIM_PRINT_HIST_DAT";
        _db2.query(maxClaimSql);
        ResultSet rs = null;
        rs = _db2.getResultSet();
        int maxClaimNo = 0;
        if (rs.next()) {
            maxClaimNo = rs.getInt("CLAIM_NO");
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH JUGYORYO AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     SLIP_NO ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DETAILS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             COMMODITY_MST T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.COMMODITY_CD = T2.COMMODITY_CD ");
        stb.append("             AND T2.COMMODITY_NAME LIKE '���Ɨ�' ");
        stb.append("     ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     '1' AS DATA_DIV, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     '' AS SLIP_NO, ");
        stb.append("     T4.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T1, ");
        stb.append("     SCHREG_REGD_DAT T2, ");
        stb.append("     APPLICANT_BASE_MST T4 ");
        stb.append(" WHERE ");
        stb.append("     T1.GRD_DIV IS NULL ");
        stb.append("     AND T2.YEAR = '2008' ");
        stb.append("     AND T2.SEMESTER = '1' ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T1.APPLICANTNO = T4.APPLICANTNO ");
        stb.append("     AND T4.REMARK NOT IN ('3401', '3521', '4506') ");
        stb.append("     AND NOT EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             CLAIM_DAT T3 ");
        stb.append("         WHERE ");
        stb.append("             T1.APPLICANTNO = T3.APPLICANTNO ");
        stb.append("     ) ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     '' AS REMARK ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             JUGYORYO T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.SLIP_NO = T2.SLIP_NO ");
        stb.append("     ) ");

        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("���Ɨ��Ə��̓`�[�쐬�ŃG���[", e);
            throw e;
        }

        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String div = (String) map.get("DATA_DIV");
            final String applicantNo = (String) map.get("APPLICANTNO");
            final String shiganshaRenBan = (String) map.get("REMARK");
            final String slipNo = div.equals("1") ? "08" + _slipFormat.format(Integer.parseInt(shiganshaRenBan)) : (String) map.get("SLIP_NO");

            if (div.equals("1")) {
                claimInsertSql(applicantNo, slipNo);
                claimDetailsInsertSql(applicantNo, slipNo, shiganshaRenBan);
                maxClaimNo++;
                claimPrintInsertSql(applicantNo, slipNo, maxClaimNo);
                salesPlanInsertSql(applicantNo, slipNo);
            } else {
                claimUpdateSql(slipNo);
                claimDetailsInsertSql(applicantNo, slipNo, shiganshaRenBan);
                salesPlanInsertSql(applicantNo, slipNo);
            }
        }
    }

    /*[db2inst1@withus db2inst1]$ db2 describe table CLAIM_DAT
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 ������
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        SLIP_DIV                       SYSIBM    VARCHAR                   1     0 �͂�
        MANNER_PAYMENT                 SYSIBM    VARCHAR                   1     0 �͂�
        PAYMENT_SEQ                    SYSIBM    VARCHAR                   2     0 �͂�
        TOTAL_MONEY                    SYSIBM    INTEGER                   4     0 �͂�
        CLAIM_DATE                     SYSIBM    DATE                      4     0 �͂�
        TOTAL_CLAIM_MONEY              SYSIBM    INTEGER                   4     0 �͂�
        PRICE                          SYSIBM    INTEGER                   4     0 �͂�
        TAX                            SYSIBM    INTEGER                   4     0 �͂�
        PAYMENT_MONEY                  SYSIBM    INTEGER                   4     0 �͂�
        PAYMENT_DATE                   SYSIBM    DATE                      4     0 �͂�
        SUMMING_UP_MONEY               SYSIBM    INTEGER                   4     0 �͂�
        SUMMING_UP_DATE                SYSIBM    DATE                      4     0 �͂�
        AZCASHIN_FLG                   SYSIBM    VARCHAR                   1     0 �͂�
        CANCEL_FLG                     SYSIBM    VARCHAR                   1     0 �͂�
        TEMP_CREDITS                   SYSIBM    SMALLINT                  2     0 �͂�
        COMP_ENT_FLG                   SYSIBM    VARCHAR                   1     0 �͂�
        BATCH_FLG                      SYSIBM    VARCHAR                   1     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
    */
    private void claimInsertSql(final String applicantNo, final String slipNo) {
        final Integer money0 = new Integer(0);
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO CLAIM_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(slipNo) + ", ");
        stb.append(" " + getInsertVal(applicantNo) + ", ");
        stb.append(" " + getInsertVal("1") + ", ");
        stb.append(" " + getInsertVal("1") + ", ");
        stb.append(" " + getInsertVal("1") + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal("2008-04-01") + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" " + getInsertVal(new Integer(32)) + ", ");
        stb.append(" " + getInsertVal("1") + ", ");
        stb.append(" null, ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }
    private void claimUpdateSql(final String slipNo) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" UPDATE CLAIM_DAT ");
        stb.append(" SET ");
        stb.append("    (TEMP_CREDITS, COMP_ENT_FLG) = (32, '1') ");
        stb.append(" WHERE ");
        stb.append("    SLIP_NO = '" + slipNo + "' ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }


    /*[db2inst1@withus db2inst1]$ db2 describe table CLAIM_DETAILS_DAT
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
    */
    private void claimDetailsInsertSql(final String applicantNo, final String slipNo, final String shiganshaRenBan) {
        final Integer money0 = new Integer(0);
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO CLAIM_DETAILS_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(slipNo) + ", ");
        stb.append(" " + getInsertVal("99") + ", ");
        stb.append(" " + getInsertVal(applicantNo) + ", ");
        stb.append(" " + getInsertVal(COMMODITY_MENJO) + ", ");
        stb.append(" " + getInsertVal("32") + ", ");
        stb.append(" " + getInsertVal("2008-04-01") + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal("200804") + ", ");
        stb.append(" " + getInsertVal("200903") + ", ");
        stb.append(" " + getInsertVal("99") + ", ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" " + getInsertVal(shiganshaRenBan) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    /*[db2inst1@withus db2inst1]$ db2 describe table CLAIM_PRINT_HIST_DAT
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
    */
    private void claimPrintInsertSql(final String applicantNo, final String slipNo, final int maxClaimNo) {
        final Integer money0 = new Integer(0);
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO CLAIM_PRINT_HIST_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(_claimNoFormat.format(maxClaimNo)) + ", ");
        stb.append(" " + getInsertVal("01") + ", ");
        stb.append(" " + getInsertVal("01") + ", ");
        stb.append(" " + getInsertVal("01") + ", ");
        stb.append(" " + getInsertVal(slipNo) + ", ");
        stb.append(" " + getInsertVal(applicantNo) + ", ");
        stb.append(" null, ");
        stb.append(" " + getInsertVal("2008-04-01") + ", ");
        stb.append(" " + getInsertVal(money0) + ", ");
        stb.append(" " + getInsertVal("2008-04-15") + ", ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    /*[db2inst1@withus db2inst1]$ db2 describe table SALES_PLAN_DAT
        YEAR                           SYSIBM    VARCHAR                   4     0 ������
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        PLAN_YEAR                      SYSIBM    VARCHAR                   4     0 ������
        PLAN_MONTH                     SYSIBM    VARCHAR                   2     0 ������
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 ������
        SEQ                            SYSIBM    VARCHAR                   2     0 ������
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 ������
        TOTAL_CLAIM_MONEY              SYSIBM    INTEGER                   4     0 �͂�
        PRICE                          SYSIBM    INTEGER                   4     0 �͂�
        TAX                            SYSIBM    INTEGER                   4     0 �͂�
        KEEPING_MONEY                  SYSIBM    INTEGER                   4     0 �͂�
        KEEPING_DATE                   SYSIBM    DATE                      4     0 �͂�
        SUMMING_UP_MONEY               SYSIBM    INTEGER                   4     0 �͂�
        SUMMING_UP_DATE                SYSIBM    DATE                      4     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
    */
    private void salesPlanInsertSql(final String applicantNo, final String slipNo) {
        final Integer money0 = new Integer(0);

        for (int i = 1; i <= 12; i++) {
            final StringBuffer stb = new StringBuffer();
            final String year = i < 4 ? "2009" : "2008";
            final String month = _monthFormat.format(i);

            stb.append(" INSERT INTO SALES_PLAN_DAT ");
            stb.append(" VALUES( ");
            stb.append(" " + getInsertVal("2008") + ", ");
            stb.append(" " + getInsertVal(applicantNo) + ", ");
            stb.append(" " + getInsertVal(year) + ", ");
            stb.append(" " + getInsertVal(month) + ", ");
            stb.append(" " + getInsertVal(slipNo) + ", ");
            stb.append(" " + getInsertVal("99") + ", ");
            stb.append(" " + getInsertVal(COMMODITY_MENJO) + ", ");
            stb.append(" " + getInsertVal(money0) + ", ");
            stb.append(" " + getInsertVal(money0) + ", ");
            stb.append(" " + getInsertVal(money0) + ", ");
            stb.append(" null, ");
            stb.append(" null, ");
            stb.append(" null, ");
            stb.append(" null, ");
            stb.append(" '" + Param.REGISTERCD + "', ");
            stb.append(" current timestamp ");
            stb.append(" ) ");

            try {
                _db2.stmt.executeUpdate(stb.toString());
            } catch (SQLException e) {
                log.debug("SQLException" + e + " \n" + stb.toString());
            }
        }
    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     SUM(TOTAL_CLAIM_MONEY) AS TOTAL_MONEY, ");
        stb.append("     MIN(CLAIM_DATE) AS CLAIM_DATE, ");
        stb.append("     SUM(TOTAL_PRICE) AS PRICE, ");
        stb.append("     SUM(TAX) AS TAX, ");
        stb.append("     SUM(PAYMENT_MONEY) AS PAYMENT_MONEY, ");
        stb.append("     MAX(PAYMENT_DATE) AS PAYMENT_DATE, ");
        stb.append("     SUM(SUMMING_UP_MONEY) AS SUMMING_UP_MONEY, ");
        stb.append("     MAX(SUMMING_UP_DATE) AS SUMMING_UP_DATE, ");
        stb.append("     SUM(CASE WHEN AMOUNT > '1' ");
        stb.append("              THEN CAST(AMOUNT AS INT) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS TEMP_CREDITS, ");
        stb.append("     CASE WHEN MAX(PAYMENT_MONEY) > 0 ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE null ");
        stb.append("     END AS COMP_ENT_FLG ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DETAILS_DAT ");
        stb.append(" GROUP BY ");
        stb.append("     SLIP_NO, ");
        stb.append("     APPLICANTNO ");

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
        [db2inst1@withus db2inst1]$ db2 describe table CLAIM_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 ������
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        SLIP_DIV                       SYSIBM    VARCHAR                   1     0 �͂�
        MANNER_PAYMENT                 SYSIBM    VARCHAR                   1     0 �͂�
        PAYMENT_SEQ                    SYSIBM    VARCHAR                   2     0 �͂�
        TOTAL_MONEY                    SYSIBM    INTEGER                   4     0 �͂�
        CLAIM_DATE                     SYSIBM    DATE                      4     0 �͂�
        TOTAL_CLAIM_MONEY              SYSIBM    INTEGER                   4     0 �͂�
        PRICE                          SYSIBM    INTEGER                   4     0 �͂�
        TAX                            SYSIBM    INTEGER                   4     0 �͂�
        PAYMENT_MONEY                  SYSIBM    INTEGER                   4     0 �͂�
        PAYMENT_DATE                   SYSIBM    DATE                      4     0 �͂�
        SUMMING_UP_MONEY               SYSIBM    INTEGER                   4     0 �͂�
        SUMMING_UP_DATE                SYSIBM    DATE                      4     0 �͂�
        AZCASHIN_FLG                   SYSIBM    VARCHAR                   1     0 �͂�
        CANCEL_FLG                     SYSIBM    VARCHAR                   1     0 �͂�
        TEMP_CREDITS                   SYSIBM    SMALLINT                  2     0 �͂�
        COMP_ENT_FLG                   SYSIBM    VARCHAR                   1     0 �͂�
        BATCH_FLG                      SYSIBM    VARCHAR                   1     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�

          21 ���R�[�h���I������܂����B
     */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("SLIP_NO"),
                map.get("APPLICANTNO"),
                "1",    // TODO:�Œ�
                "1",    // TODO:�Œ�
                "1",    // TODO:�Œ�
                map.get("TOTAL_MONEY"),
                map.get("CLAIM_DATE"),
                null,
                map.get("PRICE"),
                map.get("TAX"),
                map.get("PAYMENT_MONEY"),
                map.get("PAYMENT_DATE"),
                map.get("SUMMING_UP_MONEY"),
                map.get("SUMMING_UP_DATE"),
                null,
                null,
                map.get("TEMP_CREDITS"),
                map.get("COMP_ENT_FLG"),
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

