// kanji=漢字
/*
 * $Id: KnjRecCreditAdmits.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
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

/**
 * REC_CREDIT_ADMITS を作る。
 * @author takaesu
 * @version $Id: KnjRecCreditAdmits.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecCreditAdmits extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecCreditAdmits.class);
    private static final String KYOKA91 = "91";

    public KnjRecCreditAdmits() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "単位認定結果"; }


    void migrate() throws SQLException {
        final List renbanList = new ArrayList();
        renbanList.add(" AND int(t1.shigansha_renban) between 1 and 2000");
        renbanList.add(" AND int(t1.shigansha_renban) between 2001 and 4000");
        renbanList.add(" AND int(t1.shigansha_renban) between 4001 and 6000");
        renbanList.add(" AND int(t1.shigansha_renban) between 6001 and 8000");
        renbanList.add(" AND int(t1.shigansha_renban) >= 8001");

        for (final Iterator iter = renbanList.iterator(); iter.hasNext();) {
            final String where = (String) iter.next();
            final String sql;
            sql = "select"+
                "    t1.nendo_code,"+
                "    t1.kyoka_code,"+
                "    l1.namecd2 as CURRICULUM_CD,"+
                "    t1.kamoku_code,"+
                "    t1.shigansha_renban,"+
                "    l2.heijoten,"+
                "    t1.hyotei,"+
                "    t1.nintei_tani "+
                "from"+
                "    seito_rishu_kamoku t1"+
                "    left join name_mst l1"+
                "        on l1.namecd1 = 'W002' and "+
                "            t1.kyoikukatei_tekiyo_nendo_code between l1.namespare1 and l1.namespare2"+
                "    left join seito_rishu_heijoten l2"+
                "        on t1.nendo_code = l2.nendo_code and"+
                "           t1.kyoka_code = l2.kyoka_code and"+
                "           t1.kyoikukatei_tekiyo_nendo_code = l2.kyoikukatei_tekiyo_nendo_code and"+
                "           t1.kamoku_code = l2.kamoku_code and"+
                "           t1.shigansha_renban = l2.shigansha_renban, "+
                "    seito t2 "+
                "where "+
                "    t1.shigansha_renban = t2.shigansha_renban "+
                "    AND value(t1.kyoka_code, '') <> '" + KYOKA91 + "' "+
                "    AND value(t2.seito_no, '') <> '' "+
                where;
            
            log.debug("sql=" + sql);

            final List result = _runner.mapListQuery(sql);
            log.debug("データ件数=" + result.size());

            _runner.listToKnj(result, "REC_CREDIT_ADMITS", this);
        }
    }

    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final String schregNo = _param.getSchregno((String) map.get("SHIGANSHA_RENBAN"));

        final String strUsualScore = (String) map.get("HEIJOTEN");
        final Integer usualScore = null != strUsualScore && !strUsualScore.equals("") ? Integer.valueOf(strUsualScore) : null;

        final String strHyotei = (String) map.get("HYOTEI");
        final Integer hyotei = null != strHyotei && !strHyotei.equals("") ? Integer.valueOf(strHyotei) : null;

        final String strCredit = (String) map.get("NINTEI_TANI");
        final Integer getCredit = null != strCredit && !strCredit.equals("") ? Integer.valueOf(strCredit) : null;

        final String subclassCd = (String) map.get("KAMOKU_CODE");
        final Object[] rtn = {
                map.get("NENDO_CODE"),
                map.get("KYOKA_CODE"),
                map.get("CURRICULUM_CD"),
                map.get("KYOKA_CODE") + _subClassCdFormat.format(Integer.parseInt(subclassCd)),
                schregNo,
                usualScore,
                null,   // 7.総合得点
                hyotei,
                getCredit,
                null,   // 10.スクーリング評定
                null,   // 11.レポート平均点
                null,   // 12.試験得点
                null,   // 13.読替先フラグ
                Param.REGISTERCD,
        };
 
        return rtn;
    }
}
// eof

