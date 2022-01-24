// kanji=����
/*
 * $Id: KnjTextbookMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * TEXTBOOK_MST �����B
 * @author takaesu
 * @version $Id: KnjTextbookMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextbookMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjTextbookMst.class);
    public static final DecimalFormat _textHakkoshaNo = new DecimalFormat("0000");

    public static final String ICASS_TABLE = "TEXT_MASTER";

    public KnjTextbookMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���ȏ��}�X�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());

        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        // SQL��
        final String sql;
        //sql = "SELECT * FROM " + ICASS_TABLE;
        sql = " WITH MAX_MASTER AS ( "
            + " SELECT "
            + "     KYOKA_CODE, "
            + "     KYOKASHO_SHUMOKU_CODE, "
            + "     TEXT_NO, "
            + "     MAX(KYOIKUKATEI_TEKIYO_NENDO_CODE) AS KYOIKUKATEI_TEKIYO_NENDO_CODE "
            + " FROM "
            + "     TEXT_MASTER "
            + " GROUP BY "
            + "     KYOKA_CODE, "
            + "     KYOKASHO_SHUMOKU_CODE, "
            + "     TEXT_NO "
            + " ORDER BY "
            + "     KYOKA_CODE, "
            + "     KYOKASHO_SHUMOKU_CODE, "
            + "     TEXT_NO "
            + " ) "
            + " SELECT "
            + "     T1.* "
            + " FROM "
            + "     TEXT_MASTER T1, "
            + "     MAX_MASTER T2 "
            + " WHERE "
            + "     T1.KYOKA_CODE = T2.KYOKA_CODE "
            + "     AND T1.KYOKASHO_SHUMOKU_CODE = T2.KYOKASHO_SHUMOKU_CODE "
            + "     AND T1.TEXT_NO = T2.TEXT_NO "
            + "     AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = T2.KYOIKUKATEI_TEKIYO_NENDO_CODE ";
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
            [takaesu@withus takaesu]$ db2 describe table textbook_mst

                                           �^�C�v�E
            ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            TEXTBOOKCD                     SYSIBM    VARCHAR                   8     0 ������
            TEXT_GROUP_CD                  SYSIBM    VARCHAR                   3     0 ������
            TEXTBOOKDIV                    SYSIBM    VARCHAR                   1     0 ������
            TEXTBOOKNAME                   SYSIBM    VARCHAR                  90     0 ������
            TEXTBOOKABBV                   SYSIBM    VARCHAR                  30     0 �͂�
            TEXTBOOKMK                     SYSIBM    VARCHAR                   9     0 �͂�
            TEXTBOOKMS                     SYSIBM    VARCHAR                   3     0 �͂�
            TEXTBOOKWRITINGNAME            SYSIBM    VARCHAR                  60     0 �͂�
            TEXTBOOKPRICE                  SYSIBM    SMALLINT                  2     0 �͂�
            TEXTBOOKUNITPRICE              SYSIBM    SMALLINT                  2     0 �͂�
            TEXTBOOKAMOUNT                 SYSIBM    VARCHAR                   2     0 �͂�
            ISSUECOMPANYCD                 SYSIBM    VARCHAR                   4     0 ������
            ORDER_CD                       SYSIBM    VARCHAR                   4     0 ������
            REMARK                         SYSIBM    VARCHAR                  60     0 �͂�
            REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
            UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�

              16 ���R�[�h���I������܂����B
         */
        final String sql = "INSERT INTO textbook_mst VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, mapToTextbookMstArray(map));
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

    private Object[] mapToTextbookMstArray(final Map map) {
        /*
            [takaesu@withus takaesu]$ db2 describe table text_master

                                           �^�C�v�E
            ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            KYOIKUKATEI_TEKIYO_NENDO_CODE  SYSIBM    VARCHAR                   4     0 ������
            KYOKA_CODE                     SYSIBM    VARCHAR                   5     0 ������
            KYOKASHO_SHUMOKU_CODE          SYSIBM    VARCHAR                   3     0 ������
            TEXT_NO                        SYSIBM    VARCHAR                   3     0 ������
            TEXT_NAME                      SYSIBM    VARCHAR                  40     0 �͂�
            TEXT_HAKKOSHA_NO               SYSIBM    SMALLINT                  2     0 �͂�
            TANKA                          SYSIBM    SMALLINT                  2     0 �͂�
            HANBAI_KAISHI_NENGAPPI         SYSIBM    DATE                      4     0 �͂�
            HANBAI_SHURYO_NENGAPPI         SYSIBM    DATE                      4     0 �͂�
            TOROKU_DATE                    SYSIBM    TIMESTAMP                10     0 �͂�
            KOSHIN_DATE                    SYSIBM    TIMESTAMP                10     0 �͂�

              11 ���R�[�h���I������܂����B
         */
        // TAKAESU: �ȉ��̕���loadIcass���\�b�h����SQL�����܂Ƃ܂��Ă���ƌ��₷���͂��BHoge.java �� static ���\�b�h�Ƃ��ďW��!?
        final String kyokaCode = (String) map.get("KYOKA_CODE");
        final String kyokashoShumokuCode = (String) map.get("KYOKASHO_SHUMOKU_CODE");
        final String textBookDiv = (null != kyokashoShumokuCode && "999".equals(kyokashoShumokuCode)) ? "3" : "1";
        final String textNo = (String) map.get("TEXT_NO");
        final String textBookCd = kyokaCode + kyokashoShumokuCode + textNo;

        final String hakkoshaNo = (String) map.get("TEXT_HAKKOSHA_NO");
        int iHakkoshaNo = Integer.parseInt(hakkoshaNo);
        if (90 == iHakkoshaNo) {
            iHakkoshaNo = 902; // ���s�ЃR�[�h:�E�B�U�X
        }
        final String issueConpanyCd = _textHakkoshaNo.format(iHakkoshaNo);

        final Object[] rtn = {
                textBookCd,
                kyokashoShumokuCode,
                textBookDiv,// ���ȏ��敪
                map.get("TEXT_NAME"),
                null,
                null,
                null,
                null,
                null,
                map.get("TANKA"),
                "1",// ���ʁB(�Ƃ肠�����A�Œ�P�ňڍs����B�ڍs��ɖڌ��ŏC������)
                issueConpanyCd,
                "0001",// ������R�[�h�B("0001"�ŌŒ�)
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

