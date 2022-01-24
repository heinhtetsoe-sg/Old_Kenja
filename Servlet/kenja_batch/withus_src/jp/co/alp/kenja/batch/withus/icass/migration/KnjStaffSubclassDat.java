// kanji=����
/*
 * $Id: KnjStaffSubclassDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * STAFF_SUBCLASS_DAT�����B
 * @author takaesu
 * @version $Id: KnjStaffSubclassDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjStaffSubclassDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjStaffSubclassDat.class);
    public static final DecimalFormat _gakkoKankeishaNoFormat = new DecimalFormat("00000000");
    private static final String KNJTABLE = "STAFF_SUBCLASS_DAT";

    public KnjStaffSubclassDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�E���󂯎����Ȗ�"; }

    void migrate() throws SQLException {
        final String sql = getSql();
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }
        _runner.listToKnj(result, KNJTABLE, this);
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GAKKO_KANKEISHA_NO, ");
        stb.append("     T1.KAMOKU_CODE, ");
        stb.append("     T1.KYOKA_CODE, ");
        stb.append("     T1.NENDO_CODE, ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE, ");
        stb.append("     L1.NAMECD2 AS CURRICULUM_CD ");
        stb.append(" FROM ");
        stb.append("     TANTO_KYOIN T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     NAME_MST L1 ON L1.NAMECD1 = 'W002'  ");
        stb.append(" AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE ");
        stb.append(" BETWEEN L1.NAMESPARE1 AND L1.NAMESPARE2 ");

        return stb.toString();
    }


    /*
     * [db2inst1@withus script]$ db2 describe table staff_subclass_dat
        
        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 ������
        STAFFCD                        SYSIBM    VARCHAR                   8     0 ������
        CLASSCD                        SYSIBM    VARCHAR                   2     0 ������
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 ������
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 ������
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
        7 ���R�[�h���I������܂����B    
    */
    public Object[] mapToArray(Map map) {
        final String kyokaCode = (String) map.get("KYOKA_CODE");
        final String kamokuCode = (String) map.get("KAMOKU_CODE");
        final String gakkoKankeishaNo = (String) map.get("GAKKO_KANKEISHA_NO");
        final String subClassCd = kyokaCode + _subClassCdFormat.format(Integer.valueOf(kamokuCode));
        final String staffCd = _gakkoKankeishaNoFormat.format(Integer.valueOf(gakkoKankeishaNo));
        final Object[] rtn = {
                map.get("NENDO_CODE"),
                staffCd,
                map.get("KYOKA_CODE"),
                map.get("CURRICULUM_CD"),
                subClassCd,
                Param.REGISTERCD,
        };

        return rtn;
    }
}
// eof

