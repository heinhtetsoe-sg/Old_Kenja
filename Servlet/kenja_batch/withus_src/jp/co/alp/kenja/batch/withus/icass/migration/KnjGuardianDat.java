// kanji=����
/*
 * $Id: KnjGuardianDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GUARDIAN_DAT�����B
 * @author takaesu
 * @version $Id: KnjGuardianDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjGuardianDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjGuardianDat.class);

    public static final String ICASS_TABLE = "SEITO";

    public KnjGuardianDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�w�Еی�҃f�[�^"; }

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

            final GuardianDat guardianDat = new GuardianDat(map);
            rtn.add(guardianDat);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.HOGOSHA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_KANA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_ZOKUGARA_CODE, ");
        stb.append("     T1.HOGOSHA_YUBIN_NO, ");
        stb.append("     T1.HOGOSHA_TODOFUKEN_NO, ");
        stb.append("     T1.HOGOSHA_ADDRESS1, ");
        stb.append("     T1.HOGOSHA_ADDRESS2, ");
        stb.append("     T1.HOGOSHA_TEL_NO ");
        stb.append(" FROM ");
        stb.append("     SEITO T1 ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T1.SEITO_NO, '') <> '' ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class GuardianDat {
        final String _shiganshaRenban;
        final String _schregno;
        final String _relationship;
        final String _guardName;
        final String _guardKana;
        final String _guardBirthday;
        final String _guardSex;
        final String _guardZipcd;
        final String _guardPrefCd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _guardAddr3;
        final String _guardTelno;
        final String _guardTelnoAbb;
        final String _guardFaxno;
        final String _guardEMail;
        final String _guardJobcd;
        final String _guardWorkName;
        final String _guardWorkTelno;
        final String _guarantorRelationship;
        final String _guarantorName;
        final String _guarantorKana;
        final String _guarantorSex;
        final String _guarantorZipcd;
        final String _guarantorPrefCd;
        final String _guarantorAddr1;
        final String _guarantorAddr2;
        final String _guarantorAddr3;
        final String _guarantorTelno;
        final String _guarantorTelnoAbb;
        final String _guarantorJobcd;
        final String _publicOffice;

        public GuardianDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _schregno = _param.getSchregno(_shiganshaRenban);
            final String zokugara = (String) map.get("HOGOSHA_ZOKUGARA_CODE");
            _relationship = _param.getZokugara(zokugara);
            _guardSex = _param.getZokugaraSex(zokugara);
            _guardName = (String) map.get("HOGOSHA_SHIMEI");
            _guardKana = (String) map.get("HOGOSHA_KANA_SHIMEI");
            _guardBirthday = "";    // TODO:����
            _guardZipcd = (String) map.get("HOGOSHA_YUBIN_NO");
            _guardPrefCd = (String) map.get("HOGOSHA_TODOFUKEN_NO");
            final String[] gAddr = divideStr((String) map.get("HOGOSHA_ADDRESS1"));
            _guardAddr1 = gAddr[0];
            _guardAddr2 = gAddr[1];
            _guardAddr3 = (String) map.get("HOGOSHA_ADDRESS2");
            _guardTelno = (String) map.get("HOGOSHA_TEL_NO");
            _guardTelnoAbb = deleteStr(_guardTelno, "-");
            _guardFaxno = "";    // TODO:����
            _guardEMail = "";    // TODO:����
            _guardJobcd = "";    // TODO:����
            _guardWorkName = "";    // TODO:����
            _guardWorkTelno = "";    // TODO:����
            _guarantorRelationship = "";    // TODO:����
            _guarantorName = "";    // TODO:����
            _guarantorKana = "";    // TODO:����
            _guarantorSex = "";    // TODO:����
            _guarantorZipcd = "";    // TODO:����
            _guarantorPrefCd = "";    // TODO:����
            _guarantorAddr1 = "";    // TODO:����
            _guarantorAddr2 = "";    // TODO:����
            _guarantorAddr3 = "";    // TODO:����
            _guarantorTelno = "";    // TODO:����
            _guarantorTelnoAbb = "";    // TODO:����
            _guarantorJobcd = "";    // TODO:����
            _publicOffice = "";    // TODO:����
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table GUARDIAN_DAT
        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
        RELATIONSHIP                   SYSIBM    VARCHAR                   2     0 ������
        GUARD_NAME                     SYSIBM    VARCHAR                  60     0 �͂�
        GUARD_KANA                     SYSIBM    VARCHAR                 120     0 �͂�
        GUARD_BIRTHDAY                 SYSIBM    DATE                      4     0 �͂�
        GUARD_SEX                      SYSIBM    VARCHAR                   1     0 �͂�
        GUARD_ZIPCD                    SYSIBM    VARCHAR                   8     0 �͂�
        GUARD_PREF_CD                  SYSIBM    VARCHAR                   2     0 �͂�
        GUARD_ADDR1                    SYSIBM    VARCHAR                  75     0 �͂�
        GUARD_ADDR2                    SYSIBM    VARCHAR                  75     0 �͂�
        GUARD_ADDR3                    SYSIBM    VARCHAR                  75     0 �͂�
        GUARD_TELNO                    SYSIBM    VARCHAR                  14     0 �͂�
        GUARD_TELNO_ABB                SYSIBM    VARCHAR                  14     0 �͂�
        GUARD_FAXNO                    SYSIBM    VARCHAR                  14     0 �͂�
        GUARD_E_MAIL                   SYSIBM    VARCHAR                  20     0 �͂�
        GUARD_JOBCD                    SYSIBM    VARCHAR                   2     0 �͂�
        GUARD_WORK_NAME                SYSIBM    VARCHAR                  60     0 �͂�
        GUARD_WORK_TELNO               SYSIBM    VARCHAR                  14     0 �͂�
        GUARANTOR_RELATIONSHIP         SYSIBM    VARCHAR                   2     0 �͂�
        GUARANTOR_NAME                 SYSIBM    VARCHAR                  60     0 �͂�
        GUARANTOR_KANA                 SYSIBM    VARCHAR                 120     0 �͂�
        GUARANTOR_SEX                  SYSIBM    VARCHAR                   1     0 �͂�
        GUARANTOR_ZIPCD                SYSIBM    VARCHAR                   8     0 �͂�
        GUARANTOR_PREF_CD              SYSIBM    VARCHAR                   2     0 �͂�
        GUARANTOR_ADDR1                SYSIBM    VARCHAR                  75     0 �͂�
        GUARANTOR_ADDR2                SYSIBM    VARCHAR                  75     0 �͂�
        GUARANTOR_ADDR3                SYSIBM    VARCHAR                  75     0 �͂�
        GUARANTOR_TELNO                SYSIBM    VARCHAR                  14     0 �͂�
        GUARANTOR_TELNO_ABB            SYSIBM    VARCHAR                  14     0 �͂�
        GUARANTOR_JOBCD                SYSIBM    VARCHAR                   2     0 �͂�
        PUBLIC_OFFICE                  SYSIBM    VARCHAR                  30     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          33 ���R�[�h���I������܂����B

     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final GuardianDat guardianDat = (GuardianDat) it.next();
            final String insSql = getInsertSql(guardianDat);
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

    private String getInsertSql(final GuardianDat guardianDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO GUARDIAN_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(guardianDat._schregno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._relationship) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardName) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardKana) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardBirthday) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardSex) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardZipcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardPrefCd) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guardAddr1) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guardAddr2) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guardAddr3) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardTelno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardTelnoAbb) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardFaxno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardEMail) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardJobcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardWorkName) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guardWorkTelno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorRelationship) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorName) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorKana) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorSex) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorZipcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorPrefCd) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guarantorAddr1) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guarantorAddr2) + ", ");
        stb.append(" " + getInsertChangeVal(guardianDat._guarantorAddr3) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorTelno) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorTelnoAbb) + ", ");
        stb.append(" " + getInsertVal(guardianDat._guarantorJobcd) + ", ");
        stb.append(" " + getInsertVal(guardianDat._publicOffice) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

