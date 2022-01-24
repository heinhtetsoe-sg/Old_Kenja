// kanji=漢字
/*
 * $Id: KnjUserMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * <USER_MST>を作る。
 * @author takaesu
 * @version $Id: KnjUserMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjUserMst extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjUserMst.class);

    public KnjUserMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "利用者マスタ"; }

    //TODO: 休学日数を決定する
    void migrate() throws SQLException {
        final String sql;
        sql = "select" +
        "    STAFFCD " +
        " from" +
        "    STAFF_MST";
        
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "USER_MST", this);
    }

   
    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("STAFFCD"),
                map.get("STAFFCD"),
                "1a1dc91c907325c69271ddf0c944bc72",
                "0",
                "0",
                Param.REGISTERCD,
        };
 
        return rtn;
    }
}
// eof

