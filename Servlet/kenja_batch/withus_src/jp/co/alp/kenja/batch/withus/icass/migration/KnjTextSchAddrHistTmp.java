// kanji=漢字
/*
 * $Id: KnjTextSchAddrHistTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TEXT_SCH_ADDR_HIST_TMPを作る。
 * @author takaesu
 * @version $Id: KnjTextSchAddrHistTmp.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextSchAddrHistTmp extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjTextSchAddrHistTmp.class);

    private static final String KNJTABLE = "TEXT_SCH_ADDR_HIST_TMP";

    public KnjTextSchAddrHistTmp() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "教科書生徒別住所発行履歴TMP"; }

    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("データ件数=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L1.GUARD_ZIPCD, ");
        stb.append("     L1.GUARD_PREF_CD, ");
        stb.append("     L1.GUARD_ADDR1, ");
        stb.append("     L1.GUARD_ADDR2, ");
        stb.append("     L1.GUARD_ADDR3, ");
        stb.append("     L1.GUARD_TELNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TEXTBOOK_TMP T1 ");
        stb.append("     LEFT JOIN GUARDIAN_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L1.GUARD_ZIPCD, ");
        stb.append("     L1.GUARD_PREF_CD, ");
        stb.append("     L1.GUARD_ADDR1, ");
        stb.append("     L1.GUARD_ADDR2, ");
        stb.append("     L1.GUARD_ADDR3, ");
        stb.append("     L1.GUARD_TELNO ");

        log.debug("sql=" + stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("YEAR"),
                map.get("SCHREGNO"),
                map.get("GUARD_ZIPCD"),
                map.get("GUARD_PREF_CD"),
                map.get("GUARD_ADDR1"),
                map.get("GUARD_ADDR2"),
                map.get("GUARD_ADDR3"),
                map.get("GUARD_TELNO"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

