// kanji=����
/*
 * $Id: KnjMonthKeepingMoneyDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MONTH_KEEPING_MONEY_DAT�����B
 * @author takaesu
 * @version $Id: KnjMonthKeepingMoneyDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjMonthKeepingMoneyDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjMonthKeepingMoneyDat.class);

    private static final String KNJTABLE = "MONTH_KEEPING_MONEY_DAT";

    public KnjMonthKeepingMoneyDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�����O���"; }


    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("�f�[�^����=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

    }

    private List loadIcass() throws SQLException {

        final String exeMonth = "SELECT MAX(SALES_YEAR_MONTH) AS YEAR_MONTH FROM SALES_TIGHTENS_HIST_DAT WHERE TEMP_TIGHTENS_FLAG = '2'";

        ResultSet rs = null;
        String planYearMonth = "";
        try {
            _db2.query(exeMonth);
            rs = _db2.getResultSet();
            while (rs.next()) {
                planYearMonth = rs.getString("YEAR_MONTH");
            }
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(rs);
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     '" + planYearMonth + "' AS YEAR_MONTH, ");
        stb.append("     T1.COMMODITY_CD, ");
        stb.append("     MIN(T1.PLAN_YEAR || T1.PLAN_MONTH) AS S_YEAR_MONTH, ");
        stb.append("     MAX(T1.PLAN_YEAR || T1.PLAN_MONTH) AS E_YEAR_MONTH, ");
        stb.append("     SUM(T1.TOTAL_CLAIM_MONEY) AS SALES_SCHEDULE_MONEY, ");
        stb.append("     SUM(VALUE(T1.KEEPING_MONEY, 0)) AS KEEPING_MONEY, ");
        stb.append("     SUM(VALUE(T1.KEEPING_MONEY, 0)) - SUM(T1.TOTAL_CLAIM_MONEY) AS DIFFERENCE ");
        stb.append(" FROM ");
        stb.append("     SALES_PLAN_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.PLAN_YEAR || T1.PLAN_MONTH > '" + planYearMonth + "' ");
        stb.append("     AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             COMMODITY_MST T2 ");
        stb.append("         WHERE ");
        stb.append("             T2.DIVIDING_MULTIPLICATION_DIV = '1' ");
        stb.append("             AND T1.COMMODITY_CD = T2.COMMODITY_CD ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.COMMODITY_CD ");

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

    /** [db2inst1@withus script]$ db2 describe table MONTH_KEEPING_MONEY_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        YEAR_MONTH                     SYSIBM    VARCHAR                   6     0 ������
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 ������
        S_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 ������
        E_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 ������
        SALES_SCHEDULE_MONEY           SYSIBM    INTEGER                   4     0 �͂�
        KEEPING_MONEY                  SYSIBM    INTEGER                   4     0 �͂�
        DIFFERENCE                     SYSIBM    INTEGER                   4     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          10 ���R�[�h���I������܂����B
     */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("APPLICANTNO"),
                map.get("YEAR_MONTH"),
                map.get("COMMODITY_CD"),
                map.get("S_YEAR_MONTH"),
                map.get("E_YEAR_MONTH"),
                new Integer(((Number) map.get("SALES_SCHEDULE_MONEY")).intValue()),
                new Integer(((Number) map.get("KEEPING_MONEY")).intValue()),
                new Integer(((Number) map.get("DIFFERENCE")).intValue()),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

