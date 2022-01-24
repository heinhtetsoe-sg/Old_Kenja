// kanji=漢字
/*
 * $Id: KnjSchregAttendrecDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * <KnjSchregAttendrecDat>を作る。
 * @author takaesu
 * @version $Id: KnjSchregAttendrecDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregAttendrecDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregAttendrecDat.class);

    public KnjSchregAttendrecDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍出欠記録データ"; }

    //TODO: 休学日数を決定する
    void migrate() throws SQLException {
        final String sql;
        sql = "select " +
        "    T1.NENDO," +
        "    T1.SHIGANSHA_RENBAN," +
        "    SUM(VALUE(INT(T1.JUGYO_NISSU),0)) AS JUGYO_NISSU," +
        "    SUM(VALUE(INT(T1.TEISHI_KIBIKI_NISSU),0)) AS TEISHI_KIBIKI_NISSU," +
        "    SUM(VALUE(INT(T1.RYUGAKU_NISSU),0)) AS RYUGAKU_NISSU," +
        "    SUM(VALUE(INT(T1.KESSEKI_NISSU),0)) AS KESSEKI_NISSU," +
        "    SUM(VALUE(INT(T1.SHUSSEKI_NISSU),0)) AS SHUSSEKI_NISSU " +
        " from " +
        "    SEITO_TAKO_ZAISEKI_RIREKI_MEIS T1 " +
        "        LEFT JOIN SEITO L1 " +
        "          ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN AND " +
        "          VALUE(L1.SEITO_NO, '') <> '' " +
        " GROUP BY " +
        "    T1.NENDO, T1.SHIGANSHA_RENBAN ";
        
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "SCHREG_ATTENDREC_DAT", this);
    }

   
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final String schregNo = _param.getSchregno((String) map.get("SHIGANSHA_RENBAN"));
        if (schregNo == null) {
            return null;
        }
        final Integer classDays = null == map.get("JUGYO_NISSU") ? new Integer(0) : (Integer) map.get("JUGYO_NISSU");
        final Integer offdays = new Integer(0);
        final Integer absent = new Integer(0);
        final Integer suspend = null == map.get("TEISHI_KIBIKI_NISSU") ? new Integer(0) : (Integer) map.get("TEISHI_KIBIKI_NISSU");
        final Integer mourning = new Integer(0);
        final Integer abroad = null == map.get("RYUGAKU_NISSU") ? new Integer(0) : (Integer) map.get("RYUGAKU_NISSU");
        final Integer requirePresent = new Integer(classDays.intValue() - (abroad.intValue()+offdays.intValue()+suspend.intValue()+mourning.intValue()));
        final Integer sick = new Integer(0);
        final Integer accidentnotice = new Integer(0);
        final Integer noaccidentnotice = null == map.get("KESSEKI_NISSU") ? new Integer(0) : (Integer) map.get("KESSEKI_NISSU");
        final Integer present = null == map.get("SHUSSEKI_NISSU") ? new Integer(0) : (Integer) map.get("SHUSSEKI_NISSU");
        final Object[] rtn = {
                "1",
                map.get("NENDO"),
                schregNo,
                "00",
                null,
                classDays,
                offdays,
                absent,
                suspend,
                mourning,
                abroad,
                requirePresent,
                sick,
                accidentnotice,
                noaccidentnotice,
                present,
                Param.REGISTERCD,
        };
 
        return rtn;
    }
}
// eof

