// kanji=����
/*
 * $Id: KnjHtrainremarkHdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HTRAINREMARK_HDAT �����B
 * @author takaesu
 * @version $Id: KnjHtrainremarkHdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjHtrainremarkHdat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjHtrainremarkHdat.class);

    public static final String ICASS_TABLE = "SEITO_SOGO_GAKUSHU_KIROKU";

    public KnjHtrainremarkHdat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�w���v�^�����w�b�_�f�[�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());

        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        // SQL��
        final String sql;
        sql = " SELECT "
            + "     T1.SHIGANSHA_RENBAN, "
            + "     T1.GAKUSHU_KATSUDO, "
            + "     T1.HYOKA "
            + " FROM "
            + "     " + ICASS_TABLE + " T1, "
            + "     SEITO T2 "
            + " WHERE "
            + "      T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN "
            + " AND VALUE(T2.SEITO_NO, '') <> '' ";
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
        /*
            [takaesu@withus takaesu]$ db2 describe table htrainremark_hdat

                                           �^�C�v�E
            ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
            TOTALSTUDYACT                  SYSIBM    VARCHAR                 726     0 �͂�
            TOTALSTUDYVAL                  SYSIBM    VARCHAR                 802     0 �͂�
            REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
            UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�

              5 ���R�[�h���I������܂����B
         */
        final String sql = "INSERT INTO htrainremark_hdat VALUES(?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, mapToHtrainremarkHdatArray(map));
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

    private Object[] mapToHtrainremarkHdatArray(final Map map) {
        /*
            [takaesu@withus takaesu]$ db2 describe table SEITO_SOGO_GAKUSHU_KIROKU
                                           �^�C�v�E
            ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            SHIGANSHA_RENBAN               SYSIBM    VARCHAR                  10     0 ������
            GAKUSHU_KATSUDO                SYSIBM    VARCHAR                 800     0 �͂�
            HYOKA                          SYSIBM    VARCHAR                 800     0 �͂�
            TOROKU_DATE                    SYSIBM    VARCHAR                  20     0 �͂�
            KOSHIN_DATE                    SYSIBM    VARCHAR                  20     0 �͂�

              5 ���R�[�h���I������܂����B
         */
        // TAKAESU: �ȉ��̕���loadIcass���\�b�h����SQL�����܂Ƃ܂��Ă���ƌ��₷���͂��BHoge.java �� static ���\�b�h�Ƃ��ďW��!?
        final String schregno = _param.getSchregno((String) map.get("SHIGANSHA_RENBAN"));
        final Object[] rtn = {
                schregno,
                map.get("GAKUSHU_KATSUDO"),
                map.get("HYOKA"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

