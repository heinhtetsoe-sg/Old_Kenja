// kanji=����
/*
 * $Id: SeitoSchoolingJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/09/25 14:25:27 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration.table_icass;

import java.math.BigDecimal;
import java.util.Map;

import jp.co.alp.kenja.batch.withus.Curriculum;
import jp.co.alp.kenja.batch.withus.icass.migration.AbstractKnj;
import jp.co.alp.kenja.batch.withus.icass.migration.Param;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * ���k�X�N�[�����O���сB
 * <pre>
[takaesu@withus takaesu]$ db2 describe table seito_schooling_jisseki

                               �^�C�v�E
��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
------------------------------ --------- ------------------ -------- ----- ------
SHIGANSHA_RENBAN               SYSIBM    VARCHAR                  10     0 ������
KATEI_CODE                     SYSIBM    VARCHAR                   1     0 ������
KYOIKUKATEI_TEKIYO_NENDO_CODE  SYSIBM    VARCHAR                   4     0 ������
NENDO_CODE                     SYSIBM    VARCHAR                   4     0 ������
KYOKA_CODE                     SYSIBM    VARCHAR                   5     0 ������
KAMOKU_CODE                    SYSIBM    VARCHAR                   5     0 ������
RISHU_KIKAN_CODE               SYSIBM    VARCHAR                   1     0 ������
JUKO_NENGAPPI                  SYSIBM    VARCHAR                  10     0 ������
JUKO_JIKAN                     SYSIBM    VARCHAR                  10     0 �͂�
SCHOOLING_LEVEL                SYSIBM    VARCHAR                   1     0 �͂�
TOKUTEN                        SYSIBM    VARCHAR                  10     0 �͂�
HYOTEI                         SYSIBM    VARCHAR                   1     0 �͂�
HYOKA_GAKKO_KANKEISHA_NO       SYSIBM    VARCHAR                   6     0 �͂�
TOROKU_DATE                    SYSIBM    VARCHAR                  20     0 �͂�
KOSHIN_DATE                    SYSIBM    VARCHAR                  20     0 �͂�

  15 ���R�[�h���I������܂����B
 * </pre>
 * @author takaesu
 * @version $Id: SeitoSchoolingJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class SeitoSchoolingJisseki {
    final Param _param;

    public final String _shiganshaRenban;
    public final String _kateiCode;
    public final String _kyouikukatei�K�p�N�x;
    public final String _nendo;
    public final String _kyoka;
    public final String _kamoku;
    public final String _rishuKikanCode;
    public final String _jukoNengappi;
    public final BigDecimal _jukoJikan;
    public final String _schoolingLevel;
    public final String _tokuten;
    public final String _hyotei;
    public final String _hyokaGakkoKankeishaNo;
    public final String _periodCd;

    public SeitoSchoolingJisseki(final Param param, final Map map) {
        _param = param;

        _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        _kateiCode = (String) map.get("KATEI_CODE");
        _kyouikukatei�K�p�N�x = (String) map.get("KYOIKUKATEI_TEKIYO_NENDO_CODE");
        _nendo = (String) map.get("NENDO_CODE");
        _kyoka = (String) map.get("KYOKA_CODE");
        final String kamokuCode = (String) map.get("KAMOKU_CODE");
        _kamoku = AbstractKnj._subClassCdFormat.format(Integer.valueOf(kamokuCode));
        _rishuKikanCode = (String) map.get("RISHU_KIKAN_CODE");
        _jukoNengappi = (String) map.get("JUKO_NENGAPPI");

        final String jukoJikan = (String) map.get("JUKO_JIKAN");
        _jukoJikan = new BigDecimal(jukoJikan);
        final Integer periodCd = (Integer) map.get("KOUJI_CODE");
        _periodCd = periodCd == null ? null : periodCd.toString();

        _schoolingLevel = (String) map.get("SCHOOLING_LEVEL");
        _tokuten = (String) map.get("TOKUTEN");
        _hyotei = (String) map.get("HYOTEI");
        _hyokaGakkoKankeishaNo = (String) map.get("HYOKA_GAKKO_KANKEISHA_NO");
    }

    public Object[] toRecCommutingDat() {
        final String schregno = _param.getSchregno(_shiganshaRenban);
        final String subclassCd = _kyoka + AbstractKnj._subClassCdFormat.format(Integer.parseInt(_kamoku));
        final String curriculumCd = Curriculum.getCurriculumCd(_kyouikukatei�K�p�N�x);

        final Object[] rtn = {
                _nendo,
                _kyoka,
                curriculumCd,
                subclassCd,
                schregno,
                _jukoNengappi,
                _periodCd,
                Param.REGISTERCD,
        };
        return rtn;
    }


    public Object[] toRecSchoolingDat(final int seq) {
        final String schregno = _param.getSchregno(_shiganshaRenban);
        final String subclassCd = _kyoka + AbstractKnj._subClassCdFormat.format(Integer.parseInt(_kamoku));
        final String curriculumCd = Curriculum.getCurriculumCd(_kyouikukatei�K�p�N�x);
        final String attendDateTime = _jukoNengappi + " 00:00:00";

        final Object[] rtn = {
                _nendo,
                _kyoka,
                curriculumCd,
                subclassCd,
                schregno,
                "S1",
                new Integer(seq),
                attendDateTime,
                _jukoJikan,
                "2",
                Param.REGISTERCD,
        };
        return rtn;
    }
} // SeitoSchoolingJisseki

// eof
