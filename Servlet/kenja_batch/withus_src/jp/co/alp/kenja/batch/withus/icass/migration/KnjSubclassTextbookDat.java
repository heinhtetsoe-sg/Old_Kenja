// kanji=漢字
/*
 * $Id: KnjSubclassTextbookDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <SUBCLASS_TEXTBOOK_DAT>を作る。
 * @author takaesu
 * @version $Id: KnjSubclassTextbookDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSubclassTextbookDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjSubclassTextbookDat.class);

    public KnjSubclassTextbookDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "科目別教科書データ"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "select" +
        "    NENDO_CODE," +
        "    KYOKA_CODE," +
        "    l1.NAMECD2 AS CURRICULUM_CD," +
        "    KAMOKU_CODE," +
        "    KYOKA_CODE || KYOKASHO_SHUMOKU_CODE || TEXT_NO AS TEXTBOOKCD " +
        " from" +
        "    USE_TEXT " +
        " inner join NAME_MST l1 " +
        "    on l1.namecd1 = 'W002' and "+
        "    KYOIKUKATEI_TEKIYO_NENDO_CODE between l1.NAMESPARE1 and l1.NAMESPARE2"
        ;
        
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "SUBCLASS_TEXTBOOK_DAT", this);
    }

   
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        String classCd = (String) map.get("KYOKA_CODE");
        String kamokuCode = (String) map.get("KAMOKU_CODE");
        String subClassCd = classCd + _subClassCdFormat.format(Integer.valueOf(kamokuCode));
        final Object[] rtn = {
                map.get("NENDO_CODE"),
                classCd,
                map.get("CURRICULUM_CD"),
                subClassCd,
                map.get("TEXTBOOKCD"),
                Param.REGISTERCD,
        };
 
        return rtn;
    }
}
// eof

