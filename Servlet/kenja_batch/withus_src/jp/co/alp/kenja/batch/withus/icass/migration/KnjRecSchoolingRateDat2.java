// kanji=����
/*
 * $Id: KnjRecSchoolingRateDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/11/24 15:46:35 - JST
 * �쐬��: m-yama
 *
 * Copyright(C) 2008-2012 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REC_SCHOOLING_RATE_DAT �����B
 * @author m-yama
 * @version $Id: KnjRecSchoolingRateDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecSchoolingRateDat2 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecSchoolingRateDat2.class);

    /** �^�C�v */
    public final String S1_TYPE = "S1";
    public final String M1_TYPE = "M1";
    public final String M2_TYPE = "M2";

    /** �� */
    public final Integer S1_WARI = new Integer(2);
    public final Integer M1_WARI = new Integer(6);
    public final Integer M2_WARI = new Integer(2);

    /** ���� */
    public final Integer MINUTES = new Integer(50);

    public KnjRecSchoolingRateDat2() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�ʐM�X�N�[�����O����"; }

    void migrate() throws SQLException {

        updateSchoolingRate();
        insertSchoolingRate();

    }

    private String updateSchoolingRate() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" UPDATE ");
        stb.append("     REC_SCHOOLING_RATE_DAT T1 ");
        stb.append(" SET ");
        stb.append("     T1.RATE = " + S1_WARI + " ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '2008' ");   // TODO:2008�Œ�
        stb.append("     AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             SCHREG_BASE_MST T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("             AND T2.GRD_DIV IS NULL ");
        stb.append("     ) ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
            _db2.commit();
        } catch (SQLException e) {
            log.error("SQLException = " + stb.toString(), e);
        }

        return null;
    }

    /**
     * 
     */
    private void insertSchoolingRate() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     'INS' AS DIV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     VALUE(L1.SCHOOLING_SEQ, 0) * " + MINUTES + " AS GET_VALUE ");
        stb.append(" FROM ");
        stb.append("     COMP_REGIST_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_DETAILS_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD, ");
        stb.append("     SCHREG_BASE_MST T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '2008' ");   // TODO:2008�Œ�
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T2.GRD_DIV IS NULL ");
        stb.append("     AND NOT EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             REC_SCHOOLING_RATE_DAT E1 ");
        stb.append("         WHERE ");
        stb.append("             E1.YEAR = '2008' ");   // TODO:2008�Œ�
        stb.append("             AND T1.CLASSCD = E1.CLASSCD ");
        stb.append("             AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ");
        stb.append("             AND T1.SUBCLASSCD = E1.SUBCLASSCD ");
        stb.append("             AND T1.SCHREGNO = E1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'UPD' AS DIV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     VALUE(L1.SCHOOLING_SEQ, 0) * " + MINUTES + " AS GET_VALUE ");
        stb.append(" FROM ");
        stb.append("     REC_SCHOOLING_RATE_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_DETAILS_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD, ");
        stb.append("     SCHREG_BASE_MST T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '2008' ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T2.GRD_DIV IS NULL ");

        log.debug(stb);

        // SQL���s
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String div = (String) map.get("DIV");

            if (div.equals("INS")) {
                String sql = getInsRateSql(map, S1_TYPE, S1_WARI);
                try {
                    _db2.stmt.executeUpdate(sql);
                    _db2.commit();
                } catch (SQLException e) {
                    log.error("SQLException = " + sql, e);
                }
            }
            String sql = getInsRateSql(map, M1_TYPE, M1_WARI);
            try {
                _db2.stmt.executeUpdate(sql);
                _db2.commit();
            } catch (SQLException e) {
                log.error("SQLException = " + sql, e);
            }

            sql = getInsDatSql(map, M1_TYPE, M1_WARI);
            try {
                _db2.stmt.executeUpdate(sql);
                _db2.commit();
            } catch (SQLException e) {
                log.error("SQLException = " + sql, e);
            }

            sql = getInsRateSql(map, M2_TYPE, M2_WARI);
            try {
                _db2.stmt.executeUpdate(sql);
                _db2.commit();
            } catch (SQLException e) {
                log.error("SQLException = " + sql, e);
            }
        }
    }

    /**[db2inst1@withus script]$ db2 describe table REC_SCHOOLING_RATE_DAT
        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 ������
        CLASSCD                        SYSIBM    VARCHAR                   2     0 ������
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 ������
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 ������
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
        SCHOOLING_TYPE                 SYSIBM    VARCHAR                   2     0 ������
        RATE                           SYSIBM    SMALLINT                  2     0 �͂�
        COMMITED_S                     SYSIBM    DATE                      4     0 �͂�
        COMMITED_E                     SYSIBM    DATE                      4     0 �͂�
        GRAD_REGURATE                  SYSIBM    VARCHAR                   1     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
     */
    private String getInsRateSql(final Map map, final String type, final Integer wari) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO REC_SCHOOLING_RATE_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal((String) map.get("YEAR")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CLASSCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CURRICULUM_CD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("SUBCLASSCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("SCHREGNO")) + ", ");
        stb.append(" " + getInsertVal(type) + ", ");
        stb.append(" " + getInsertVal(wari) + ", ");
        if (M1_TYPE.equals(type)) {
            stb.append(" '2008-12-03', ");
            stb.append(" '2008-12-03', ");
        } else {
            stb.append(" " + null + ", ");
            stb.append(" " + null + ", ");
        }
        stb.append(" " + null + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }

    /**[db2inst1@withus script]$ db2 describe table REC_SCHOOLING_DAT
        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 ������
        CLASSCD                        SYSIBM    VARCHAR                   2     0 ������
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 ������
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 ������
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
        SCHOOLING_TYPE                 SYSIBM    VARCHAR                   2     0 ������
        SEQ                            SYSIBM    SMALLINT                  2     0 ������
        ATTENDDATETIME                 SYSIBM    TIMESTAMP                10     0 �͂�
        GET_VALUE                      SYSIBM    SMALLINT                  2     0 �͂�
        INPUT_TYPE                     SYSIBM    VARCHAR                   1     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
     */
    private String getInsDatSql(final Map map, final String type, final Integer wari) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO REC_SCHOOLING_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal((String) map.get("YEAR")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CLASSCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CURRICULUM_CD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("SUBCLASSCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("SCHREGNO")) + ", ");
        stb.append(" " + getInsertVal(type) + ", ");
        stb.append(" " + 1 + ", "); // TODO:�Œ�
        stb.append(" " + null + ", ");
        final int getValue = ((Number) map.get("GET_VALUE")).intValue();
        final int setValue = getValue * wari.intValue() / 10;
        stb.append(" " + getInsertVal(new Integer(setValue)) + ", ");
        stb.append(" '2', ");   // TODO:�Œ� ICASS
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }

}
// eof

