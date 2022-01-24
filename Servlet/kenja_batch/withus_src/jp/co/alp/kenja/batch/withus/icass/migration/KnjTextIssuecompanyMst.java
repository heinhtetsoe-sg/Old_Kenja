// kanji=漢字
/*
 * $Id: KnjTextIssuecompanyMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TEXT_ISSUECOMPANY_MSTを作る。
 * @author takaesu
 * @version $Id: KnjTextIssuecompanyMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextIssuecompanyMst extends AbstractKnj implements IKnj {
    private static final String KNJTABLE = "TEXT_ISSUECOMPANY_MST";
    /*pkg*/static final Log log = LogFactory.getLog(KnjTextIssuecompanyMst.class);

    public KnjTextIssuecompanyMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "教科書発注社マスタ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug("データ件数=" + list.size());

    }

    private List loadIcass() throws SQLException {
        // SQL文
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     KYOKASHO_HAKKOSHA_NO, ");
        stb.append("     TEXT_HAKKOSHA_NAME, ");
        stb.append("     TEXT_HAKKOSHA_RYAKU_NAME, ");
        stb.append("     YUBIN_NO, ");
        stb.append("     ADDRESS1, ");
        stb.append("     ADDRESS2, ");
        stb.append("     TEL_NO ");
        stb.append(" FROM ");
        stb.append("     TEXT_HAKKOSHA_MASTER ");

        log.debug("sql=" + stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        _runner.listToKnj(result, KNJTABLE, this);
        return (null != result) ? result : Collections.EMPTY_LIST;
    }


    /*
     * [db2inst1@withus script]$ db2 describe table TEXT_ISSUECOMPANY_MST

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        ISSUECOMPANY_CD                SYSIBM    VARCHAR                   4     0 いいえ
        ISSUECOMPANY_NAME              SYSIBM    VARCHAR                  60     0 はい
        ISSUECOMPANY_ABBV              SYSIBM    VARCHAR                  30     0 はい
        ISSUECOMPANY_ZIPCD             SYSIBM    VARCHAR                   8     0 はい
        ISSUECOMPANY_PREF_CD           SYSIBM    VARCHAR                   2     0 はい
        ISSUECOMPANY_ADDR1             SYSIBM    VARCHAR                  75     0 はい
        ISSUECOMPANY_ADDR2             SYSIBM    VARCHAR                  75     0 はい
        ISSUECOMPANY_ADDR3             SYSIBM    VARCHAR                  75     0 はい
        ISSUECOMPANY_TELNO             SYSIBM    VARCHAR                  14     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          11 レコードが選択されました。
     */
    public Object[] mapToArray(final Map map) {
        final String[] addr = divideStr((String) map.get("ADDRESS1"));
        final Integer kyokashoHakkoshaNo = Integer.valueOf((String) map.get("KYOKASHO_HAKKOSHA_NO"));
        final String issuecompanyCd = new DecimalFormat("0000").format(kyokashoHakkoshaNo);
        final Object[] rtn = {
                issuecompanyCd,
                map.get("TEXT_HAKKOSHA_NAME"),
                map.get("TEXT_HAKKOSHA_RYAKU_NAME"),
                map.get("YUBIN_NO"),
                null,
                addr[0],
                addr[1],
                map.get("ADDRESS2"),
                map.get("TEL_NO"),
                Param.REGISTERCD,
        };
        return rtn;
    }

}
// eof

