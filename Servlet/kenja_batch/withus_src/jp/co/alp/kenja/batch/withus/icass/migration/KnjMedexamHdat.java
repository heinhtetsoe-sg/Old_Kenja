// kanji=漢字

package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MEDEXAM_DET_DAT を作る。
 * @author takaesu
 */
public class KnjMedexamHdat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjClassMst.class);

    public static final String ICASS_TABLE = "SEITO_KENKO_SHINDAN_KEKKA";
    
    public KnjMedexamHdat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "健康診断ヘッダデータ"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "select" +
        "    T1.SHIGANSHA_RENBAN, " +
        "    T1.NENDO_CODE, " +
        "    T1.JISSHI_NENGAPPI " +
        " FROM " +
        "    " + ICASS_TABLE + " T1 " +
        "  LEFT JOIN SEITO L1 ON " +
        "     T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN AND "+
        "     VALUE(L1.SEITO_NO,'') <> '' " ;
        
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "MEDEXAM_HDAT", this);
    }

   
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        String schregNo = _param.getSchregno((String) map.get("SHIGANSHA_RENBAN"));
        String dateStr = (String) map.get("JISSHI_NENGAPPI");
        java.sql.Date date = dateStr == null ? null : Date.valueOf((String) map.get("JISSHI_NENGAPPI"));
        final Object[] rtn = {
                map.get("NENDO_CODE"),
                schregNo,
                date,
                Param.REGISTERCD
        };
        return rtn;
    }
}
// eof

