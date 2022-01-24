// kanji=����
/*
 * $Id: KnjStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * STAFF_MST �����B
 * @author takaesu
 * @version $Id: KnjStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjStaffMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjStaffMst.class);

    public static final DecimalFormat _staffCdFormat = new DecimalFormat("00000000");
    public static final DecimalFormat _jobCdFormat = new DecimalFormat("0000");

    public KnjStaffMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�E���}�X�^"; }

    void migrate() throws SQLException {
        /* GAKKO_KANKEISHA.GAKKO_KANKEISHA_SHUBETSU_CODE
            0�F�V�X�e���Ǘ���
            1�F�w�Z���E��
            2�F�@�l�E��
            3�F�T�|�[�g�Z�E��
            4�F�A���o�C�g
            5�F���͍Z�E��
            6�F�ʐڎw�����{�{�ݐE��
            7�F�ی��
            8�F�Ǝ�
            9�F����
            99�F���̑�
         */
        final List list = loadIcass();
        log.debug("gakko_kankeisha �f�[�^����=" + list.size());

        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        // SQL��
        final String sql;
        sql = "WITH KYOTEN AS ("
            + "  SELECT"
            + "     T1.* "
            + " FROM "
            + "     KINMUSAKI_GAKUSHU_KYOTEN T1, "
            + "     (SELECT "
            + "          GAKKO_KANKEISHA_NO, "
            + "          MAX(SHUNIN_NENGAPPI) AS NENGAPPI "
            + "      FROM "
            + "          KINMUSAKI_GAKUSHU_KYOTEN "
            + "      GROUP BY "
            + "          GAKKO_KANKEISHA_NO "
            + "     ) T2 "
            + " WHERE "
            + "     T1.GAKKO_KANKEISHA_NO = T2.GAKKO_KANKEISHA_NO "
            + "     AND T1.SHUNIN_NENGAPPI = T2.NENGAPPI "
            + " ), SHOKU AS ( "
            + " SELECT "
            + "     T1.* "
            + " FROM "
            + "     SHOKUMEI T1, "
            + "     (SELECT "
            + "          GAKKO_KANKEISHA_NO, "
            + "          MAX(SHUNIN_NENGAPPI) AS NENGAPPI "
            + "      FROM "
            + "          SHOKUMEI "
            + "      GROUP BY "
            + "          GAKKO_KANKEISHA_NO "
            + "     ) T2 "
            + " WHERE "
            + "     T1.GAKKO_KANKEISHA_NO = T2.GAKKO_KANKEISHA_NO "
            + "     AND T1.SHUNIN_NENGAPPI = T2.NENGAPPI "
            + " ) "
            + " SELECT "
            + "     T1.*, "
            + "     T2.GAKUSHU_KYOTEN_CODE, "
            + "     T3.SHOKUMEI_CODE "
            + " FROM "
            + "     GAKKO_KANKEISHA T1 "
            + "     LEFT JOIN KYOTEN T2 ON T1.GAKKO_KANKEISHA_NO=T2.GAKKO_KANKEISHA_NO "
            + "     LEFT JOIN SHOKU T3 ON T1.GAKKO_KANKEISHA_NO=T3.GAKKO_KANKEISHA_NO "
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

        return (null != result) ? result : Collections.EMPTY_LIST;
    }

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO STAFF_MST VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, mapToStaffMstArray(map));
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT������1���ȊO!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("���҂ւ�INSERT�ŃG���[", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("�}������=" + totalCount);
    }

    private Object[] mapToStaffMstArray(final Map map) {
        /*
            [db2inst1@withus db2inst1]$ db2 describe table STAFF_MST

            ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            STAFFCD                        SYSIBM    VARCHAR                   8     0 ������
            STAFFNAME                      SYSIBM    VARCHAR                  60     0 �͂�
            STAFFNAME_SHOW                 SYSIBM    VARCHAR                  15     0 �͂�
            STAFFNAME_KANA                 SYSIBM    VARCHAR                 120     0 �͂�
            STAFFNAME_ENG                  SYSIBM    VARCHAR                  60     0 �͂�
            BELONGING_DIV                  SYSIBM    VARCHAR                   3     0 �͂�
            JOBCD                          SYSIBM    VARCHAR                   4     0 �͂�
            SECTIONCD                      SYSIBM    VARCHAR                   4     0 �͂�
            DUTYSHARECD                    SYSIBM    VARCHAR                   4     0 �͂�
            CHARGECLASSCD                  SYSIBM    VARCHAR                   1     0 �͂�
            STAFFSEX                       SYSIBM    VARCHAR                   1     0 �͂�
            STAFFBIRTHDAY                  SYSIBM    DATE                      4     0 �͂�
            STAFFZIPCD                     SYSIBM    VARCHAR                   8     0 �͂�
            STAFFPREF_CD                   SYSIBM    VARCHAR                   2     0 �͂�
            STAFFADDR1                     SYSIBM    VARCHAR                  75     0 �͂�
            STAFFADDR2                     SYSIBM    VARCHAR                  75     0 �͂�
            STAFFADDR3                     SYSIBM    VARCHAR                  75     0 �͂�
            STAFFTELNO                     SYSIBM    VARCHAR                  14     0 �͂�
            STAFFTELNO_SEARCH              SYSIBM    VARCHAR                  14     0 �͂�
            STAFFFAXNO                     SYSIBM    VARCHAR                  14     0 �͂�
            STAFFE_MAIL                    SYSIBM    VARCHAR                  25     0 �͂�
            REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
            UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
            
              23 ���R�[�h���I������܂����B
         */
        // TODO: ��������!
        // TAKAESU: �ȉ��̕���loadIcass���\�b�h����SQL�����܂Ƃ܂��Ă���ƌ��₷���͂��BHoge.java �� static ���\�b�h�Ƃ��ďW��!?
        String shimei = (String) map.get("SHIMEI");
        String sex = (String) map.get("SEIBETSU");
        String staffCd = (String) map.get("GAKKO_KANKEISHA_NO");
        final String shokumei = (String) map.get("shokumei_code");
        final Object[] rtn = {
                _staffCdFormat.format(Integer.parseInt(staffCd)),//TODO: �[�����߂���
                shimei,
                shimei.length() > 5 ? shimei.substring(0, 5) : shimei,
                map.get("KANA_SHIMEI"),
                map.get(""),// 5.�E�������p��
                map.get("gakushu_kyoten_code"),
                null == shokumei ? null : _jobCdFormat.format(Integer.parseInt(shokumei)),//TODO: �[�����߂���
                map.get(""),// 8.�����R�[�h
                map.get(""),// 9.�Z���������R�[�h
                map.get(""),// 10.���Ǝ󎝋敪
                null != sex && sex.equals("�j") ? "1" : "2", // 11.�E������
                map.get("SEINENGAPPI"),
                map.get("YUBIN_NO"),
                map.get("TODOFUKEN_NO"),
                map.get("ADDRESS1"),// 15.�E���Z��1
                map.get(""),// 16.�E��FAX�ԍ�
                map.get("ADDRESS2"),
                map.get("KEITAI_TEL_NO"),
                map.get(""),// 19.�E���d�b�ԍ�(�����p)
                map.get(""),// 20.�E��FAX�ԍ�
                map.get("PC_E_MAIL"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

