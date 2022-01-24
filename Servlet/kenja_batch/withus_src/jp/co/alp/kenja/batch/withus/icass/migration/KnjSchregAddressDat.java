// kanji=����
/*
 * $Id: KnjSchregAddressDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * SCHREG_ADDRESS_DAT�����B
 * @author takaesu
 * @version $Id: KnjSchregAddressDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregAddressDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregAddressDat.class);

    public static final String ICASS_TABLE = "SEITO";

    public KnjSchregAddressDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�w�ЏZ���f�[�^"; }

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

            final SchregAddressDat schregAddressDat = new SchregAddressDat(map);
            rtn.add(schregAddressDat);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.NYUGAKU_NENGAPPI, ");
        stb.append("     T1.SOTSUGYO_NENGAPPI, ");
        stb.append("     T1.YUBIN_NO, ");
        stb.append("     T1.TODOFUKEN_NO, ");
        stb.append("     T1.ADDRESS1, ");
        stb.append("     T1.ADDRESS2, ");
        stb.append("     T1.TEL_NO, ");
        stb.append("     T1.PC_E_MAIL ");
        stb.append(" FROM ");
        stb.append("     SEITO T1 ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T1.SEITO_NO, '') <> '' ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class SchregAddressDat {
        final String _shiganshaRenban;
        final String _schregno;
        final String _issuedate;
        final String _expiredate;
        final String _areacd;
        final String _zipcd;
        final String _prefCd;
        final String _addr1;
        final String _addr2;
        final String _addr3;
        final String _addr1Eng;
        final String _addr2Eng;
        final String _telno;
        final String _telnoSearch;
        final String _faxno;
        final String _email;

        public SchregAddressDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _issuedate = (String) map.get("NYUGAKU_NENGAPPI");
            _expiredate = (String) map.get("SOTSUGYO_NENGAPPI");
            _areacd = "";    // TODO:����
            _zipcd = (String) map.get("YUBIN_NO");
            _prefCd = (String) map.get("TODOFUKEN_NO");
            final String[] addr = divideStr((String) map.get("ADDRESS1"));
            _addr1 = addr[0];
            _addr2 = addr[1]; 
            _addr3 = (String) map.get("ADDRESS2");
            _addr1Eng = "";    // TODO:����
            _addr2Eng = "";    // TODO:����
            _telno = (String) map.get("TEL_NO");
            _telnoSearch = deleteStr(_telno, "-");
            _faxno = "";    // TODO:����
            _email = (String) map.get("PC_E_MAIL");
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_ADDRESS_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
        ISSUEDATE                      SYSIBM    DATE                      4     0 ������
        EXPIREDATE                     SYSIBM    DATE                      4     0 �͂�
        AREACD                         SYSIBM    VARCHAR                   2     0 �͂�
        ZIPCD                          SYSIBM    VARCHAR                   8     0 �͂�
        PREF_CD                        SYSIBM    VARCHAR                   2     0 �͂�
        ADDR1                          SYSIBM    VARCHAR                  75     0 �͂�
        ADDR2                          SYSIBM    VARCHAR                  75     0 �͂�
        ADDR3                          SYSIBM    VARCHAR                  75     0 �͂�
        ADDR1_ENG                      SYSIBM    VARCHAR                  75     0 �͂�
        ADDR2_ENG                      SYSIBM    VARCHAR                  75     0 �͂�
        TELNO                          SYSIBM    VARCHAR                  14     0 �͂�
        TELNO_SEARCH                   SYSIBM    VARCHAR                  14     0 �͂�
        FAXNO                          SYSIBM    VARCHAR                  14     0 �͂�
        EMAIL                          SYSIBM    VARCHAR                  20     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          17 ���R�[�h���I������܂����B

     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SchregAddressDat schregAddressDat = (SchregAddressDat) it.next();
            final String insSql = getInsertSql(schregAddressDat);
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

    private String getInsertSql(final SchregAddressDat schregAddressDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_ADDRESS_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregAddressDat._schregno) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._issuedate) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._expiredate) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._areacd) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._zipcd) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._prefCd) + ", ");
        stb.append(" " + getInsertChangeVal(schregAddressDat._addr1) + ", ");
        stb.append(" " + getInsertChangeVal(schregAddressDat._addr2) + ", ");
        stb.append(" " + getInsertChangeVal(schregAddressDat._addr3) + ", ");
        stb.append(" " + getInsertChangeVal(schregAddressDat._addr1Eng) + ", ");
        stb.append(" " + getInsertChangeVal(schregAddressDat._addr2Eng) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._telno) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._telnoSearch) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._faxno) + ", ");
        stb.append(" " + getInsertVal(schregAddressDat._email) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

