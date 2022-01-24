// kanji=����
/*
 * $Id: KnjSchregBaseHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: <���҂̃e�[�u�����ɏ��������Ă��������B��) REC_REPORT_DAT>�����B
 * @author takaesu
 * @version $Id: KnjSchregBaseHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregBaseHistDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregBaseHistDat.class);

    public KnjSchregBaseHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�w�Њ�b�����f�[�^"; }


    void migrate() throws SQLException {
        final String sql;
        sql = "select "+
        "    shozoku.SHIGANSHA_RENBAN, "+
        "    shozoku.SHOZOKU_KAISHI_NENGAPPI, "+
        "    shozoku.SHOZOKU_SHURYO_NENGAPPI, "+
        "    shozoku.GAKUSHU_KYOTEN_CODE, "+
        "    shozoku.CLASS_CODE, "+
        "    shozoku.KATEI_CODE, "+
        "    shozoku.GAKKA_CODE, "+
        "    shozoku.SENKO_CODE, "+
        "    shozoku.COURSE_CODE "+
        "from SEITO_SHOZOKU shozoku" +
        "       join SEITO on shozoku.SHIGANSHA_RENBAN = SEITO.SHIGANSHA_RENBAN "+
        "where SHOZOKU_SHURYO_NENGAPPI is not null "
        ;
        
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("�f�[�^����=" + result.size());

        _runner.listToKnj(result, "SCHREG_BASE_HIST_DAT", this);
    }

    /**
     *     �V HR_CLASS
     *         ������̒��� 2 ���� 3 ��'0'����
     * 
     *     �X MAJORCD 
     *         ������̒��� 2 ���� 3 ��'0'����
     *         
     *     10 COURSECODE
     *         ������̒��� 2 ���� 4 ��'0'����
     */

    
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final String schregNo = _param.getSchregno((String) map.get("SHIGANSHA_RENBAN"));
        final String year = ((String) map.get("SHOZOKU_KAISHI_NENGAPPI")).substring(0,4);

        final String hrClass1 = (String) map.get("CLASS_CODE");        
        final String hrClass =  hrClass1 == null ? null : new DecimalFormat("000").format(Integer.valueOf(hrClass1));
        final String majorCd =  new DecimalFormat("000").format(Integer.valueOf((String) map.get("GAKKA_CODE")));
        final String courseCd = new DecimalFormat("0000").format(Integer.valueOf((String) map.get("COURSE_CODE")));
        final String courseMst = (String) map.get("KATEI_CODE") + (String) map.get("GAKKA_CODE") + (String) map.get("SENKO_CODE") + (String) map.get("COURSE_CODE");

        Date shozokuKaishiNengappi = Date.valueOf((String) map.get("SHOZOKU_KAISHI_NENGAPPI"));
        Date shozokuShuryoNengappi = Date.valueOf((String) map.get("SHOZOKU_SHURYO_NENGAPPI")); 
        final Object[] rtn = {
                schregNo,
                year,
                "1", // SEMESTER
                shozokuKaishiNengappi,
                shozokuShuryoNengappi,
                map.get("GAKUSHU_KYOTEN_CODE"),
                hrClass,
                map.get("KATEI_CODE"),
                majorCd,
                courseCd,
                null,
                _param.getStudentDiv((String) map.get("GAKUSHU_KYOTEN_CODE"), courseMst),
                null,
                null,
                null,
                null,
                null,
                Param.REGISTERCD,
        };
 
        return rtn;
    }
}
// eof

