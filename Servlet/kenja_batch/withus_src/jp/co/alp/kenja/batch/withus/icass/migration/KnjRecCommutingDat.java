// kanji=����
/*
 * $Id: KnjRecCommutingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.icass.migration.table_icass.SeitoSchoolingJisseki;

/**
 * REC_COMMUTING_DAT �����B
 * @author takaesu
 * @version $Id: KnjRecCommutingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecCommutingDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecCommutingDat.class);

    public KnjRecCommutingDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�ʊw�X�N�[�����O����"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug("�f�[�^����=" + list.size());
        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL��
        final String sql;
        sql = " WITH TAB_KAISU AS( " +
        " SELECT " +
        "     T1.SHIGANSHA_RENBAN, " +
        "     T2.SEITO_NO as SCHREGNO, " +
        "     T1.NENDO_CODE, " +
        "     T1.KYOKA_CODE, " +
        "     T1.KAMOKU_CODE, " +
        "     COUNT(*) AS JISSHI_KAISU, " +
        "     T4.NINTEI_TANI, " +
        "     T5.SCHOOLING_SEQ " +
        " FROM " +
        "     SEITO_SCHOOLING_JISSEKI T1 " +
        "     INNER JOIN SEITO T2 ON T1.SHIGANSHA_RENBAN=T2.SHIGANSHA_RENBAN " +
        "     INNER JOIN SCHREG_REGD_DAT T3 ON T2.SEITO_NO=T3.SCHREGNO " +
        "     INNER JOIN SEITO_RISHU_KAMOKU T4 ON  " +
        "         T1.SHIGANSHA_RENBAN = T4.SHIGANSHA_RENBAN AND " +
        "         T1.KATEI_CODE = T4.KATEI_CODE AND " +
        "         T1.NENDO_CODE = T4.NENDO_CODE AND " +
        "         T1.KYOKA_CODE = T4.KYOKA_CODE AND " +
        "         T1.KAMOKU_CODE = T4.KAMOKU_CODE AND " +
        "         T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = T4.KYOIKUKATEI_TEKIYO_NENDO_CODE " +
        "     LEFT JOIN NAME_MST T6 ON " +
        "         T6.NAMECD1 = 'W002' AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN T6.NAMESPARE1 AND T6.NAMESPARE2 " +
        "     INNER JOIN SUBCLASS_DETAILS_MST T5 ON " +
        "         T1.NENDO_CODE = T5.YEAR AND " +
        "         T1.KYOKA_CODE = T5.CLASSCD AND " +
        "         T1.KYOKA_CODE || T1.KAMOKU_CODE = T5.SUBCLASSCD " +
        "         AND T5.CURRICULUM_CD = T6.NAMECD2 " +
        " WHERE " +
        "     T1.NENDO_CODE=T3.YEAR AND " +
        "     T3.SEMESTER = '1' AND " +
        "     T3.STUDENT_DIV='01' " +
        // TODO: ������������E�L�̏����ǉ�?��SEITO_SCHOOLING_JISSEKI.JUKO_NENGAPPI ���ʊw���̓��ł��鎖
        " GROUP BY " +
        "     T1.SHIGANSHA_RENBAN, T2.SEITO_NO, T1.NENDO_CODE, T1.KYOKA_CODE, " +
        "     T1.KAMOKU_CODE, T4.NINTEI_TANI, T5.SCHOOLING_SEQ " +
        " ),TAB_JISSHI_NO AS( " +
        " SELECT " +
        "     T2.SHIGANSHA_RENBAN, " +
        "     T2.SCHREGNO, " +
        "     T2.NENDO_CODE, " +
        "     T2.KYOKA_CODE, " +
        "     T2.KAMOKU_CODE, " +
        "     T1.JUKO_JIKAN, " +
        "     int(T2.SCHOOLING_SEQ) AS KIJUN_JUKO_JIKAN, " +
        "     T2.JISSHI_KAISU, " +
        "     T1.JUKO_NENGAPPI, " +
        "     ROW_NUMBER() OVER(PARTITION BY T2.SHIGANSHA_RENBAN, T2.SCHREGNO, T2.NENDO_CODE, T2.KYOKA_CODE,T2.KAMOKU_CODE, INT(T2.SCHOOLING_SEQ) * JISSHI_KAISU, T2.JISSHI_KAISU ORDER BY T1.JUKO_NENGAPPI) AS JISSHI_NO " +
        " FROM " +
        "     SEITO_SCHOOLING_JISSEKI T1 " +
        "     INNER JOIN TAB_KAISU  T2 ON " +
        "      T1.SHIGANSHA_RENBAN=T2.SHIGANSHA_RENBAN AND " +
        "      T1.NENDO_CODE = T2.NENDO_CODE AND " +
        "      T1.KYOKA_CODE = T2.KYOKA_CODE AND " +
        "      T1.KAMOKU_CODE = T2.KAMOKU_CODE " +
        " )SELECT " +
        "     T2.SCHREGNO, " +
        "     T1.SHIGANSHA_RENBAN, " +
        "     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE, " +
        "     T1.NENDO_CODE, " +
        "     T1.KYOKA_CODE, " +
        "     T1.KAMOKU_CODE, " +
        "     T1.JUKO_NENGAPPI, " +
        "     RTRIM(CHAR(T2.KIJUN_JUKO_JIKAN)) AS JUKO_JIKAN, " +
        "     T2.JISSHI_KAISU, " +
        "     T2.JISSHI_NO, " +
        "     (CASE WHEN T2.JISSHI_NO = T2.JISSHI_KAISU "+
        "      THEN INT(DOUBLE(T2.KIJUN_JUKO_JIKAN)) / T2.JISSHI_KAISU + MOD(INT(DOUBLE(T2.KIJUN_JUKO_JIKAN)), T2.JISSHI_KAISU) " +
        "      ELSE INT(DOUBLE(T2.KIJUN_JUKO_JIKAN)) / T2.JISSHI_KAISU " +
        "      END) AS KOUJI_CODE " +
        "   FROM      SEITO_SCHOOLING_JISSEKI T1 " +
        "     INNER JOIN TAB_JISSHI_NO T2 ON " +
        "        T1.SHIGANSHA_RENBAN=T2.SHIGANSHA_RENBAN AND " +
        "        T1.NENDO_CODE = T2.NENDO_CODE AND " +
        "        T1.KYOKA_CODE = T2.KYOKA_CODE AND " +
        "        T1.KAMOKU_CODE = T2.KAMOKU_CODE AND " +
        "        T1.JUKO_NENGAPPI = T2.JUKO_NENGAPPI " +
        "   ORDER BY 1,2,3,4,5,6,7 " 
        ;
        log.debug("sql=" + sql);

        // SQL���s
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final SeitoSchoolingJisseki ssj = new SeitoSchoolingJisseki(_param, map);
            rtn.add(ssj);
        }
        return rtn;
    }

    private void saveKnj(final List list) throws SQLException {
        /*
[takaesu@withus takaesu]$ db2 describe table rec_commuting_dat

                               �^�C�v�E
��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
------------------------------ --------- ------------------ -------- ----- ------
YEAR                           SYSIBM    VARCHAR                   4     0 ������
CLASSCD                        SYSIBM    VARCHAR                   2     0 ������
CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 ������
SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 ������
SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
ATTEND_DATE                    SYSIBM    DATE                      4     0 ������
PERIODCD                       SYSIBM    VARCHAR                   2     0 ������
REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�

  9 ���R�[�h���I������܂����B
         */
        int totalCount = 0;
        final String sql = "INSERT INTO rec_commuting_dat VALUES(?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SeitoSchoolingJisseki object = (SeitoSchoolingJisseki) it.next();
            Object[] objects = object.toRecCommutingDat();
            String periodCd = (String) objects[objects.length-2];
            int iPeriodCd = Integer.parseInt(periodCd);
            
            for(int i=1; i<=iPeriodCd; i++) {
                objects[objects.length-2] =new Integer(i).toString();
                try {
                    final int insertCount = _runner.update(_db2.conn, sql, objects);
                    if (1 != insertCount) {
                        throw new IllegalStateException("INSERT������1���ȊO!:" + insertCount);
                    }
                    totalCount += insertCount;
                } catch (final SQLException e) {
                    log.error("���҂ւ�INSERT�ŃG���[", e);
                    throw e;
                }
            }
        }
        _db2.commit();
        log.warn("�}������=" + totalCount);
    }
}

// eof
