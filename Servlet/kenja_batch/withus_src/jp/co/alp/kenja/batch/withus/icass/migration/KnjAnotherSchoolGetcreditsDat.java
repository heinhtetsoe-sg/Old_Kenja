// kanji=����
/*
 * $Id: KnjAnotherSchoolGetcreditsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ANOTHER_SCHOOL_GETCREDITS_DAT�����B
 * @author takaesu
 * @version $Id: KnjAnotherSchoolGetcreditsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjAnotherSchoolGetcreditsDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjAnotherSchoolGetcreditsDat.class);

    public static final String ICASS_TABLE = "SEITO_JIKOGAI_RISHU_KAMOKU";

    public int _countter99 = 0;
    public static final DecimalFormat _subClassCd99Format = new DecimalFormat("990000");
    public static final DecimalFormat _subClassCd = new DecimalFormat("0000");

    public static final Map _kyouka_henkan = new HashMap();

    public KnjAnotherSchoolGetcreditsDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���Z�O�C���P�ʃf�[�^"; }

    void migrate() throws SQLException {
        setKyoukaHenkan();
        for (int strSep = 1; strSep < 60000; strSep += 5000) {
            final int endSep = strSep + 4999;
            final List list = loadIcass(strSep, endSep);
            log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());
            log.debug(String.valueOf(strSep) + "�`" + String.valueOf(endSep));

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("�X�V�������ɃG���[! rollback �����B");
                throw e;
            }
        }
        _db2.commit();
        _countter99 = 0;
        for (int strSep = 60001; strSep < 120000; strSep += 5000) {
            final int endSep = strSep + 4999;
            final List list = loadIcass(strSep, endSep);
            log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());
            log.debug(String.valueOf(strSep) + "�`" + String.valueOf(endSep));

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("�X�V�������ɃG���[! rollback �����B");
                throw e;
            }
        }
        _db2.commit();
    }

    private void setKyoukaHenkan() {
        _kyouka_henkan.put("�H��", "42");
        _kyouka_henkan.put("����", "12");
        _kyouka_henkan.put("�_��", "41");
        _kyouka_henkan.put("�w�Z�ݒ�", "13");
        _kyouka_henkan.put("���̑�", "81");
        _kyouka_henkan.put("�ƒ�", "45");
        _kyouka_henkan.put("���Ȗ�", "81");
        _kyouka_henkan.put("���y", "51");
        _kyouka_henkan.put("���", "47");
        _kyouka_henkan.put("�O����", "8");
        _kyouka_henkan.put("�̈�", "50");
        _kyouka_henkan.put("�p��", "53");
        _kyouka_henkan.put("����", "14");
        _kyouka_henkan.put("�@��", "13");
        _kyouka_henkan.put("���", "81");
        _kyouka_henkan.put("����", "11");
        _kyouka_henkan.put("���p", "52");
        _kyouka_henkan.put("���ۗ���", "13");
        _kyouka_henkan.put("����", "1");
        _kyouka_henkan.put("���ʊ���", "82");
        _kyouka_henkan.put("�H��(�d�C)", "42");
        _kyouka_henkan.put("����", "45");
        _kyouka_henkan.put("����", "15");
        _kyouka_henkan.put("�ی��̈�", "6");
        _kyouka_henkan.put("�Ō�", "17");
        _kyouka_henkan.put("���Y", "44");
        _kyouka_henkan.put("��剹�y", "51");
        _kyouka_henkan.put("���w", "4");
        _kyouka_henkan.put("�@", "41");
        _kyouka_henkan.put("�H�Ɓi�@�B�j", "42");
        _kyouka_henkan.put("�Љ��", "14");
        _kyouka_henkan.put("��勳��", "81");
        _kyouka_henkan.put("�I��", "13");
        _kyouka_henkan.put("�I������", "13");
        _kyouka_henkan.put("�w�Z�ݒ苳��", "13");
        _kyouka_henkan.put("���{", "13");
        _kyouka_henkan.put("����", "13");
        _kyouka_henkan.put("����", "5");
        _kyouka_henkan.put("�f���|�p", "55");
        _kyouka_henkan.put("���y���", "51");
        _kyouka_henkan.put("�w�Z�ݒ�Ȗ�", "13");
        _kyouka_henkan.put("���ۃR�~���j�P�[�V����", "13");
        _kyouka_henkan.put("������", "42");
        _kyouka_henkan.put("�n��", "2");
        _kyouka_henkan.put("����", "82");
        _kyouka_henkan.put("�ۈ�", "45");
        _kyouka_henkan.put("����", "81");
        _kyouka_henkan.put("�|�p", "55");
        _kyouka_henkan.put("�Y�ƎЉ�", "16");
        _kyouka_henkan.put("���D", "81");
        _kyouka_henkan.put("�����Ȋw�n��", "81");
        _kyouka_henkan.put("��������", "13");
        _kyouka_henkan.put("���p�H�|", "52");
        _kyouka_henkan.put("�������C�t", "14");
        _kyouka_henkan.put("�h�s", "47");
        _kyouka_henkan.put("�|�\����", "81");
        _kyouka_henkan.put("�H�Ɓi�@�B�e�N�m���W�[�j", "42");
        _kyouka_henkan.put("�H�Ɓi�@�B�ȁj", "42");
        _kyouka_henkan.put("�H�Ɓi�d�q�@�B�j", "42");
        _kyouka_henkan.put("���ۗ���", "12");
        _kyouka_henkan.put("�Y�ƉȊw", "13");
        _kyouka_henkan.put("����", "13");
        _kyouka_henkan.put("��勳��Ɋւ���e���ȁE�Ȗ�", "81");
        _kyouka_henkan.put("�����w�K", "11");
        _kyouka_henkan.put("�y�j���ʍu��", "81");
        _kyouka_henkan.put("�`", "41");
        _kyouka_henkan.put("�g�q", "83");
        _kyouka_henkan.put("�h�s�r�W�l�X", "12");
        _kyouka_henkan.put("�r�r�g", "81");
        _kyouka_henkan.put("���K", "81");
        _kyouka_henkan.put("��b", "81");
        _kyouka_henkan.put("����", "3");
        _kyouka_henkan.put("�H�Ɓi�d�q�j", "42");
        _kyouka_henkan.put("�H�Ɓi���H���j", "42");
        _kyouka_henkan.put("�����p��b", "81");
        _kyouka_henkan.put("�S�����", "81");
        _kyouka_henkan.put("���ʋ��犈��", "82");
        _kyouka_henkan.put("���p���", "52");
        _kyouka_henkan.put("�L�����A", "81");
        _kyouka_henkan.put("�N���G�C�e�B�u�X�^�f�B", "81");
        _kyouka_henkan.put("�R�~���j�P�[�V����", "81");
        _kyouka_henkan.put("�[�~�i�[��", "81");
        _kyouka_henkan.put("�z�[�����[��", "83");
        _kyouka_henkan.put("���C�t�f�U�C��", "45");
        _kyouka_henkan.put("�w�Z�ٗ�", "13");
        _kyouka_henkan.put("�w�Z�ݒu�Ȗ�", "13");
        _kyouka_henkan.put("�؍���", "8");
        _kyouka_henkan.put("�@�B�V�X�e��", "42");
        _kyouka_henkan.put("���㕶���_", "13");
        _kyouka_henkan.put("���R�I���Ȗ�", "13");
        _kyouka_henkan.put("�Љ�Y��", "13");
        _kyouka_henkan.put("���Ȋw", "47");
        _kyouka_henkan.put("�l��", "13");
        _kyouka_henkan.put("�����E����", "45");
        _kyouka_henkan.put("�����I�Ȋw�K�̎���", "11");
        _kyouka_henkan.put("��", "81");
        _kyouka_henkan.put("����", "13");
        _kyouka_henkan.put("����", "13");
        _kyouka_henkan.put("��@", "13");
        _kyouka_henkan.put("C A D", "42");
        _kyouka_henkan.put("LHR", "83");
        _kyouka_henkan.put("O.A", "81");
        _kyouka_henkan.put("�r�W���A���f�U�C��", "81");
        _kyouka_henkan.put("�z�[���E���[��", "83");
        _kyouka_henkan.put("�z�[�����[������", "83");
        _kyouka_henkan.put("������", "13");
        _kyouka_henkan.put("��ÊŌ�", "17");
        _kyouka_henkan.put("���y���Z1", "51");
        _kyouka_henkan.put("�ƒ�i���j", "45");
        _kyouka_henkan.put("�Ȋw�Z�p", "42");
        _kyouka_henkan.put("�C�m", "44");
        _kyouka_henkan.put("�G��", "52");
        _kyouka_henkan.put("�w��", "83");
        _kyouka_henkan.put("�w�Z�O�w�C", "11");
        _kyouka_henkan.put("�w�Z�O�ݒ�", "81");
        _kyouka_henkan.put("�ό�", "19");
        _kyouka_henkan.put("��b�u��", "81");
        _kyouka_henkan.put("���y�wS", "81");
        _kyouka_henkan.put("����r�W�l�X", "12");
        _kyouka_henkan.put("�H�ƁE���H�w", "42");
        _kyouka_henkan.put("�Y��", "12");
        _kyouka_henkan.put("�Y�ƋZ�p", "12");
        _kyouka_henkan.put("�Y�ƎЉ�Ɛl��", "16");
        _kyouka_henkan.put("�Y��", "16");
        _kyouka_henkan.put("���R�I���Ȗ�1", "13");
        _kyouka_henkan.put("���H�s���w", "81");
        _kyouka_henkan.put("��񏈗�1", "47");
        _kyouka_henkan.put("�E��", "11");
        _kyouka_henkan.put("�E�Ƌ���I", "11");
        _kyouka_henkan.put("�l�ԂƉȊw", "13");
        _kyouka_henkan.put("�l�ԉ�", "13");
        _kyouka_henkan.put("�l���ƎЉ�", "13");
        _kyouka_henkan.put("�ݒ�", "13");
        _kyouka_henkan.put("��卑��", "1");
        _kyouka_henkan.put("�O�Ѝ��Z", "81");
        _kyouka_henkan.put("�n���I���ʊ���", "82");
        _kyouka_henkan.put("���݌�", "81");
        _kyouka_henkan.put("�����̊w�K", "11");
        _kyouka_henkan.put("�����Ȋw", "81");
        _kyouka_henkan.put("������b", "81");
        _kyouka_henkan.put("�������K", "41");
        _kyouka_henkan.put("�̌��w�K", "11");
        _kyouka_henkan.put("�n��", "2");
        _kyouka_henkan.put("�n�����j", "2");
        _kyouka_henkan.put("�d�C��b", "42");
        _kyouka_henkan.put("�d�C���}", "42");
        _kyouka_henkan.put("���`", "81");
        _kyouka_henkan.put("����", "81");
        _kyouka_henkan.put("��HR", "83");
        _kyouka_henkan.put("���g�q", "83");
        _kyouka_henkan.put("����", "82");
        _kyouka_henkan.put("���ʊ����̌���", "82");
        _kyouka_henkan.put("���ʍu��", "81");
        _kyouka_henkan.put("���ʐݒ�", "81");
        _kyouka_henkan.put("���e", "81");
        _kyouka_henkan.put("�\��", "81");
        _kyouka_henkan.put("����", "81");
        _kyouka_henkan.put("�ۈ畟��", "45");
        _kyouka_henkan.put("��d", "81");
        _kyouka_henkan.put("���w", "81");
        _kyouka_henkan.put("��q", "13");
        _kyouka_henkan.put("��q�E�I��E���{�u��", "13");
        _kyouka_henkan.put("�A�g", "81");
        _kyouka_henkan.put("�J��", "13");
    }

    private List loadIcass(final int strSep, final int endSep) throws SQLException {
        final List rtn = new ArrayList();

        // SQL���s
        final List result;
        try {
            final String sql = getSql(strSep, endSep);
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: �f�[�^�ʂ������̂ň��Ă��܂��B�΍���l����!
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final AnotherSchoolGetcreditsDat anotherSchoolGetcreditsDat = new AnotherSchoolGetcreditsDat(map);
            rtn.add(anotherSchoolGetcreditsDat);
        }
        return rtn;
    }

    private String getSql(final int strSep, final int endSep) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ANOTHER_KAMOKU AS ( ");
        stb.append(" SELECT  ");
        stb.append("     RANK() OVER(PARTITION BY SHUTOKU_NENDO, KYOIKUKATEI_TEKIYO_NENDO_CODE, KYOKA_CODE ");
        stb.append("                 ORDER BY T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,KYOKA_CODE,KAMOKU_NAME) AS KAMOKU_CODE, ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     T1.TANI_SHUTOKU_SHUDAN_CODE,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     T1.KYOKA_CODE ");
        stb.append(" FROM  ");
        stb.append("     SEITO_JIKOGAI_RISHU_KAMOKU T1  ");
        stb.append(" WHERE  ");
        stb.append("     VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '') IN ('01', '02', '03', '04', '11', '99')  ");
        stb.append("     AND VALUE(T1.KAMOKU_CODE, '') = '' ");
        stb.append(" GROUP BY  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     T1.TANI_SHUTOKU_SHUDAN_CODE,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     T1.KYOKA_CODE ");
        stb.append(" ), MK AS (  ");
        stb.append(" SELECT  ");
        stb.append("     CAST(ROW_NUMBER() OVER() AS INTEGER) AS COUN,   ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     CASE VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '')  ");
        stb.append("          WHEN '01' THEN '0'  ");
        stb.append("          WHEN '02' THEN '1'  ");
        stb.append("          WHEN '03' THEN '9'  ");
        stb.append("          WHEN '04' THEN '0'  ");
        stb.append("          WHEN '11' THEN '9'  ");
        stb.append("          WHEN '12' THEN 'null'  ");
        stb.append("          WHEN '99' THEN '9'  ");
        stb.append("          ELSE '9'  ");
        stb.append("     END AS GET_METHOD,  ");
        stb.append("     MAX(L2.SCHOOL_CD) AS SCHOOL_CD,  ");
        stb.append("     SUM(CASE WHEN T1.FURIKAE_TANI = ''  ");
        stb.append("              THEN 0  ");
        stb.append("              ELSE CAST(T1.FURIKAE_TANI AS SMALLINT)  ");
        stb.append("         END  ");
        stb.append("     ) AS FURIKAE_TANI,  ");
        stb.append("     MAX(CASE WHEN T1.HYOTEI = ''  ");//���p�C���B�uSUM�v�ł�Ȃ���MAX�B�{�邳��̕Ԏ������莟��B�C���B�B�B�B
        stb.append("              THEN 0  ");
        stb.append("              WHEN T1.HYOTEI = 'A'  ");
        stb.append("              THEN 0  ");
        stb.append("              WHEN T1.HYOTEI = 'B'  ");
        stb.append("              THEN 0  ");
        stb.append("              WHEN T1.HYOTEI = 'C'  ");
        stb.append("              THEN 0  ");
        stb.append("              ELSE CAST(T1.HYOTEI AS INTEGER)  ");
        stb.append("         END  ");
        stb.append("     ) AS HYOTEI,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     L1.NAMECD2 AS CURRICULUM_CD,  ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     CASE WHEN L3.KAMOKU_CODE IS NOT NULL ");
        stb.append("          THEN '99' || CAST(L3.KAMOKU_CODE AS CHAR(3)) ");
        stb.append("          ELSE T1.KAMOKU_CODE ");
        stb.append("     END AS KAMOKU_CODE,  ");
        stb.append("     CASE WHEN MAX(T1.HYOTEI) = 'A' OR MAX(T1.HYOTEI) = 'B' OR MAX(T1.HYOTEI) = 'C'  ");
        stb.append("          THEN MAX(T1.BIKO) || ' ' || MAX(T1.HYOTEI)  ");
        stb.append("          ELSE MAX(T1.BIKO)  ");
        stb.append("     END AS BIKO,  ");
        stb.append("     COUNT(*) AS CNT  ");
        stb.append(" FROM  ");
        stb.append("     SEITO_JIKOGAI_RISHU_KAMOKU T1  ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'W002'  ");
        stb.append("          AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L1.NAMESPARE1 AND L1.NAMESPARE2  ");
        stb.append("     LEFT JOIN KOKO_YOMIKAE L2 ON T1.KOKO_KANRI_NO = L2.KOKO_KANRI_NO ");
        stb.append("     LEFT JOIN ANOTHER_KAMOKU L3 ON T1.KYOKA_NAME = L3.KYOKA_NAME ");
        stb.append("          AND T1.KAMOKU_NAME = L3.KAMOKU_NAME ");
        stb.append("          AND T1.SHUTOKU_NENDO = L3.SHUTOKU_NENDO ");
        stb.append("          AND T1.TANI_SHUTOKU_SHUDAN_CODE = L3.TANI_SHUTOKU_SHUDAN_CODE ");
        stb.append("          AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = L3.KYOIKUKATEI_TEKIYO_NENDO_CODE ");
        stb.append("          AND T1.KYOKA_CODE = L3.KYOKA_CODE ");
        stb.append(" WHERE  ");
        stb.append("     VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '') IN ('01', '02', '03', '04', '11', '99')  ");
        stb.append(" GROUP BY  ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     CASE VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '')  ");
        stb.append("          WHEN '01' THEN '0'  ");
        stb.append("          WHEN '02' THEN '1'  ");
        stb.append("          WHEN '03' THEN '9'  ");
        stb.append("          WHEN '04' THEN '0'  ");
        stb.append("          WHEN '11' THEN '9'  ");
        stb.append("          WHEN '12' THEN 'null'  ");
        stb.append("          WHEN '99' THEN '9'  ");
        stb.append("          ELSE '9'  ");
        stb.append("     END,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     L1.NAMECD2,  ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     CASE WHEN L3.KAMOKU_CODE IS NOT NULL ");
        stb.append("          THEN '99' || CAST(L3.KAMOKU_CODE AS CHAR(3)) ");
        stb.append("          ELSE T1.KAMOKU_CODE ");
        stb.append("     END  ");
        stb.append(" )  ");
        stb.append(" SELECT  ");
        stb.append("     T1.COUN,   ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     T1.GET_METHOD,  ");
        stb.append("     T1.SCHOOL_CD,  ");
        stb.append("     T1.FURIKAE_TANI,  ");
        stb.append("     T1.HYOTEI,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     CASE VALUE(T1.CURRICULUM_CD, '')  ");
        stb.append("        WHEN '' THEN L2.NAMECD2 ");
        stb.append("        ELSE T1.CURRICULUM_CD ");
        stb.append("     END AS CURRICULUM_CD, ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     RTRIM(T1.KAMOKU_CODE) AS KAMOKU_CODE,  ");
        stb.append("     T1.BIKO  ");
        stb.append(" FROM  ");
        stb.append("     MK T1 ");
        stb.append("     LEFT JOIN SEITO L1 ON L1.SHIGANSHA_RENBAN = T1.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'W002'  ");
        stb.append("          AND L1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L2.NAMESPARE1 AND L2.NAMESPARE2  ");
        stb.append(" WHERE COUN BETWEEN " + strSep +  " AND " + endSep + " ");
        stb.append(" ORDER BY COUN ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class AnotherSchoolGetcreditsDat {
        final String _shiganshaRenban;
        final String _year;
        final String _getDiv;
        final String _applicantno;
        final String _getMethod;
        final String _classcd;
        final String _curriculumCd;
        final String _subclasscd;
        final String _creditCurriculumCd;
        final String _creditAdmitscd;
        final String _subclassname;
        final String _subclassabbv;
        final Integer _getCredit;
        final Integer _valuation;
        final String _formerRegSchoolcd;
        final String _getDate;
        final String _remark;

        public AnotherSchoolGetcreditsDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _year = (String) map.get("SHUTOKU_NENDO");
            _getDiv = "1";  // TODO:�Ƃ肠����
            _applicantno = _param.getApplicantNo(_shiganshaRenban);
            _getMethod = (String) map.get("GET_METHOD");
            String classCd = (String) map.get("KYOKA_CODE");
            String curriculumCd = (String) map.get("CURRICULUM_CD");
            String subclassCd = (String) map.get("KAMOKU_CODE");
            if (null == classCd || (classCd).equals("")) {
                classCd = "99";
                final String kyokaName = (String) map.get("KYOKA_NAME");
                if (_kyouka_henkan.containsKey(kyokaName)) {
                    classCd = (String) _kyouka_henkan.get(kyokaName);
                }
            }
            if (null == curriculumCd || (curriculumCd).equals("")) {
                curriculumCd = "2"; // TODO:�Ƃ肠�����Œ�
            }
            if (null == subclassCd || (subclassCd).equals("")) {
                subclassCd = _subClassCd99Format.format(_countter99);
                _countter99++;
            }
            _classcd = classCd;
            _curriculumCd = curriculumCd;
            if (subclassCd.startsWith("99")) {
                _subclasscd = "99" + _subClassCd.format(Integer.parseInt(subclassCd.substring(2)));
            } else {
                _subclasscd = _classcd + _subClassCd.format(Integer.parseInt(subclassCd));
            }
            _creditCurriculumCd = "2";  // TODO:�Ƃ肠�����Œ�
            _creditAdmitscd = "000000"; // TODO:�Ƃ肠�����I�[��'0'
            _subclassname = (String) map.get("KAMOKU_NAME");
            _subclassabbv = "";    // TODO:����
            _getCredit = (Integer) map.get("FURIKAE_TANI");
            _valuation = (Integer) map.get("HYOTEI");
            _formerRegSchoolcd = (String) map.get("SCHOOL_CD");
            _getDate = "";    // TODO:����
            final String biko = (String) map.get("BIKO");
            if (biko != null && biko.length() > 30) {
                _remark = biko.substring(0,30);
                log.debug(_remark);
                log.debug("********************" + _shiganshaRenban);
            } else {
                _remark = biko;
            }
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table ANOTHER_SCHOOL_GETCREDITS_DAT

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 ������
        GET_DIV                        SYSIBM    VARCHAR                   1     0 ������
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        GET_METHOD                     SYSIBM    VARCHAR                   1     0 ������
        CLASSCD                        SYSIBM    VARCHAR                   2     0 ������
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 ������
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 ������
        CREDIT_CURRICULUM_CD           SYSIBM    VARCHAR                   1     0 ������
        CREDIT_ADMITSCD                SYSIBM    VARCHAR                   6     0 ������
        SUBCLASSNAME                   SYSIBM    VARCHAR                  60     0 �͂�
        SUBCLASSABBV                   SYSIBM    VARCHAR                  15     0 �͂�
        GET_CREDIT                     SYSIBM    SMALLINT                  2     0 �͂�
        VALUATION                      SYSIBM    SMALLINT                  2     0 �͂�
        FORMER_REG_SCHOOLCD            SYSIBM    VARCHAR                  11     0 �͂�
        GET_DATE                       SYSIBM    DATE                      4     0 �͂�
        REMARK                         SYSIBM    VARCHAR                  90     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          18 ���R�[�h���I������܂����B
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final AnotherSchoolGetcreditsDat anotherSchoolGetcreditsDat = (AnotherSchoolGetcreditsDat) it.next();
            final String insSql = getInsertSql(anotherSchoolGetcreditsDat);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.debug(insSql);
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("�}������=" + totalCount);
    }

    private String getInsertSql(final AnotherSchoolGetcreditsDat anotherSchoolGetcreditsDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO ANOTHER_SCHOOL_GETCREDITS_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._year) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getDiv) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._applicantno) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getMethod) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._classcd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._curriculumCd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._subclasscd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._creditCurriculumCd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._creditAdmitscd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._subclassname) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._subclassabbv) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getCredit) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._valuation) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._formerRegSchoolcd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getDate) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._remark) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

