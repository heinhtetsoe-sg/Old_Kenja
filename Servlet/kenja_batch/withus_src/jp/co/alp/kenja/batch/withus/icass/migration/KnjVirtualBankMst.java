// kanji=����
/*
 * $Id: KnjVirtualBankMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * VIRTUAL_BANK_MST�����B
 * @author takaesu
 * @version $Id: KnjVirtualBankMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjVirtualBankMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjVirtualBankMst.class);

    public static final String ICASS_TABLE = "BANK_MASTER";
    public static final String ICASS_TABLE2 = "BANK_BRANCH_MASTER";

    public KnjVirtualBankMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���z������s�}�X�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "��" + ICASS_TABLE2 + "�f�[�^����=" + list.size());

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

            final VirtualBankMst virtualBankMst = new VirtualBankMst(map);
            rtn.add(virtualBankMst);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RIGHT(RTRIM('000' || cast(row_number() over() AS CHAR(3))) ,3) AS VIRTUAL_BANK_CD, ");
        stb.append("     T1.BANK_CODE, ");
        stb.append("     L1.BRANCH_CODE, ");
        stb.append("     T1.BANK_NAME, ");
        stb.append("     T1.BANK_KANA_NAME, ");
        stb.append("     L1.BRANCH_NAME, ");
        stb.append("     L1.BRANCH_KANA_NAME ");
        stb.append(" FROM ");
        stb.append("     BANK_MASTER T1 ");
        stb.append("     LEFT JOIN BANK_BRANCH_MASTER L1 ON T1.BANK_CODE = L1.BANK_CODE ");
        stb.append("          AND VALUE(L1.BRANCH_CODE, '') <> '' ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class VirtualBankMst {
        final String _virtualBankCd;
        final String _bankCd;
        final String _branchCd;
        final String _bankName;
        final String _bankKana;
        final String _branchName;
        final String _branchKana;
        final String _bankZipcd;
        final String _bankAddr1;
        final String _bankAddr2;
        final String _bankTelno;
        final String _bankFaxno;
        final String _accountName;
        final String _accountKana;
        final String _accountZipcd;
        final String _accountAddr1;
        final String _accountAddr2;
        final String _accountAddr3;
        final String _accountTelno;

        public VirtualBankMst(final Map map) {
            _virtualBankCd = (String) map.get("VIRTUAL_BANK_CD");
            _bankCd = (String) map.get("BANK_CODE");
            _branchCd = (String) map.get("BRANCH_CODE");
            final String[] bankName = retDividString((String) map.get("BANK_NAME"), 15, 1);
            _bankName = bankName[0];
            final String[] bankNameKana = retDividString((String) map.get("BANK_KANA_NAME"), 15, 1);
            _bankKana = bankNameKana[0];
            final String[] branchName = retDividString((String) map.get("BRANCH_NAME"), 15, 1);
            _branchName = branchName[0];
            final String[] branchKana = retDividString((String) map.get("BRANCH_KANA_NAME"), 15, 1);
            _branchKana = branchKana[0];
            _bankZipcd = "";    // TODO:�����
            _bankAddr1 = "";    // TODO:�����
            _bankAddr2 = "";    // TODO:�����
            _bankTelno = "";    // TODO:�����
            _bankFaxno = "";    // TODO:�����
            _accountName = "";    // TODO:�����
            _accountKana = "";    // TODO:�����
            _accountZipcd = "";    // TODO:�����
            _accountAddr1 = "";    // TODO:�����
            _accountAddr2 = "";    // TODO:�����
            _accountAddr3 = "";    // TODO:�����
            _accountTelno = "";    // TODO:�����
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table VIRTUAL_BANK_MST

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        VIRTUAL_BANK_CD                SYSIBM    VARCHAR                   3     0 ������
        BANK_CD                        SYSIBM    VARCHAR                   4     0 ������
        BRANCH_CD                      SYSIBM    VARCHAR                   3     0 ������
        BANK_NAME                      SYSIBM    VARCHAR                  45     0 �͂�
        BANK_KANA                      SYSIBM    VARCHAR                  45     0 �͂�
        BRANCH_NAME                    SYSIBM    VARCHAR                  45     0 �͂�
        BRANCH_KANA                    SYSIBM    VARCHAR                  45     0 �͂�
        BANK_ZIPCD                     SYSIBM    VARCHAR                   8     0 �͂�
        BANK_ADDR1                     SYSIBM    VARCHAR                  75     0 �͂�
        BANK_ADDR2                     SYSIBM    VARCHAR                  75     0 �͂�
        BANK_TELNO                     SYSIBM    VARCHAR                  14     0 �͂�
        BANK_FAXNO                     SYSIBM    VARCHAR                  14     0 �͂�
        ACCOUNT_NAME                   SYSIBM    VARCHAR                  90     0 �͂�
        ACCOUNT_KANA                   SYSIBM    VARCHAR                 120     0 �͂�
        ACCOUNT_ZIPCD                  SYSIBM    VARCHAR                   8     0 �͂�
        ACCOUNT_ADDR1                  SYSIBM    VARCHAR                  75     0 �͂�
        ACCOUNT_ADDR2                  SYSIBM    VARCHAR                  75     0 �͂�
        ACCOUNT_ADDR3                  SYSIBM    VARCHAR                  75     0 �͂�
        ACCOUNT_TELNO                  SYSIBM    VARCHAR                  14     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          21 ���R�[�h���I������܂����B
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final VirtualBankMst virtualBankMst = (VirtualBankMst) it.next();
            final String insSql = getInsertSql(virtualBankMst);
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

    private String getInsertSql(final VirtualBankMst virtualBankMst) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO VIRTUAL_BANK_MST ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(virtualBankMst._virtualBankCd) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankCd) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._branchCd) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankName) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankKana) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._branchName) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._branchKana) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankZipcd) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankAddr1) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankAddr2) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankTelno) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._bankFaxno) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountName) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountKana) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountZipcd) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountAddr1) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountAddr2) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountAddr3) + ", ");
        stb.append(" " + getInsertVal(virtualBankMst._accountTelno) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

