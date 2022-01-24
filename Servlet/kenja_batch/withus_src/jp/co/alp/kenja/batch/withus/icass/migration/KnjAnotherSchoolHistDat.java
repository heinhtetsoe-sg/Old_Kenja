// kanji=����
/*
 * $Id: KnjAnotherSchoolHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ANOTHER_SCHOOL_HIST_DAT �����B
 * @author takaesu
 * @version $Id: KnjAnotherSchoolHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjAnotherSchoolHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjAnotherSchoolHistDat.class);

    public KnjAnotherSchoolHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�O�ЍZ�����f�[�^"; }

    void migrate() throws SQLException {
        final String sql;
        sql = " SELECT "
            + " T1.*, "
            + "     CASE L1.DN_DIV "
            + "          WHEN '0' THEN '1' "
            + "          WHEN '1' THEN '2' "
            + "          WHEN '2' THEN '3' "
            + "     END AS DN_DIV, "
            + "     L1.SCHOOL_CD, "
            + "     L2.GAKKA_NAME "
            + " FROM "
            + "     SEITO_TAKO_ZAISEKI_RIREKI T1 "
            + "     LEFT JOIN KOKO_YOMIKAE L1 ON T1.KOKO_KANRI_NO = L1.KOKO_KANRI_NO "
            + "     LEFT JOIN KOKO_MASTER L2 ON T1.KOKO_KANRI_NO = L2.KOKO_KANRI_NO "
            ;
        log.debug("sql=" + sql);
        
        // SQL���s
        final List result;
        try {
            result = _runner.mapListQuery(sql);
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }
        log.debug("�f�[�^����=" + result.size());

        _runner.listToKnj(result, "another_school_hist_dat", this);
    }

    /**[db2inst1@withus script]$ db2 describe table ANOTHER_SCHOOL_HIST_DAT
        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 ������
        SEQ                            SYSIBM    SMALLINT                  2     0 ������
        STUDENT_DIV                    SYSIBM    VARCHAR                   1     0 �͂�
        FORMER_REG_SCHOOLCD            SYSIBM    VARCHAR                  11     0 �͂�
        MAJOR_NAME                     SYSIBM    VARCHAR                 120     0 �͂�
        REGD_S_DATE                    SYSIBM    DATE                      4     0 �͂�
        REGD_E_DATE                    SYSIBM    DATE                      4     0 �͂�
        PERIOD_MONTH_CNT               SYSIBM    VARCHAR                   2     0 �͂�
        ABSENCE_CNT                    SYSIBM    VARCHAR                   2     0 �͂�
        MONTH_CNT                      SYSIBM    VARCHAR                   2     0 �͂�
        ENT_FORM                       SYSIBM    VARCHAR                   1     0 �͂�
        REASON                         SYSIBM    VARCHAR                 150     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
     */
    public Object[] mapToArray(final Map map) {
// TAKAESU: SEITO_TAKO_KYUGAKU_RIREKI���g���Ȃ�ȉ��̃��W�b�N������
//        final String kyugakuKikanF = (String) map.get("KYUGAKU_KIKAN_F");
//        final String kyugakuKikanT = (String) map.get("KYUGAKU_KIKAN_T");
//        final Integer absenceCnt = get����(kyugakuKikanF, kyugakuKikanT);
        String rslt = null;
        if (map.get("ZAISEKI_KIKAN_F") != null && map.get("ZAISEKI_KIKAN_T") != null) {
            final String zaiseki_kikan_f = map.get("ZAISEKI_KIKAN_F").toString();
            final String zaiseki_kikan_t = map.get("ZAISEKI_KIKAN_T").toString();
            final int f_year  = Integer.parseInt(zaiseki_kikan_f.substring(0, 4));
            final int t_year  = Integer.parseInt(zaiseki_kikan_t.substring(0, 4));
            final int f_month = Integer.parseInt(zaiseki_kikan_f.substring(5, 7));
            final int t_month = Integer.parseInt(zaiseki_kikan_t.substring(5, 7));
            final int year  = t_year - f_year;
            final int month = t_month - f_month;
            rslt  = String.valueOf(year * 12 + month + 1); //�����Ԍ���
            if (rslt.length() > 2) {
                rslt  = "99";
            }
        }
        final String shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String applicantNo = _param.getApplicantNo(shiganshaRenban);
        final Object[] rtn = {
                applicantNo,
                map.get("ZAISEKI_RIREKI_RENBAN"),
                map.get("DN_DIV"),// 3.�O�ЍZ�w���敪
                map.get("SCHOOL_CD"),
                map.get("GAKKA_NAME"),
                map.get("ZAISEKI_KIKAN_F"),
                map.get("ZAISEKI_KIKAN_T"),
                rslt,
                null,// 9.�x�w����
                rslt,
                map.get("TAKO_NYUGAKU_KEITAI_CODE"),
                null,// 12. ���R
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

