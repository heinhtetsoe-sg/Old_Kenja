// kanji=漢字
/*
 * $Id: KnjClaimPrintHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CLAIM_PRINT_HIST_DATを作る。
 * @author takaesu
 * @version $Id: KnjClaimPrintHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClaimPrintHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClaimPrintHistDat.class);

    private static final String KNJTABLE = "CLAIM_PRINT_HIST_DAT";

    private static DecimalFormat _claimNoFormat = new DecimalFormat("08000000");

    public KnjClaimPrintHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "請求書発行履歴データ"; }


    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("データ件数=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

    }

    private List loadIcass() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ROW_NUMBER() OVER() AS CLAIM_NO, ");
        stb.append("     SLIP_NO, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     CLAIM_DATE, ");
        stb.append("     TOTAL_CLAIM_MONEY, ");
        stb.append("     Add_days(DATE(CLAIM_DATE), 14) AS TIMELIMIT_DAY ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DAT ");
        stb.append(" ORDER BY ");
        stb.append("     CLAIM_DATE, ");
        stb.append("     APPLICANTNO ");
        stb.append("  ");

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

    /** [db2inst1@withus db2inst1]$ db2 describe table CLAIM_PRINT_HIST_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        CLAIM_NO                       SYSIBM    VARCHAR                   8     0 いいえ
        SEQ                            SYSIBM    VARCHAR                   2     0 いいえ
        REISSUE_CNT                    SYSIBM    VARCHAR                   2     0 いいえ
        RE_CLAIM_CNT                   SYSIBM    VARCHAR                   2     0 いいえ
        SLIP_NO                        SYSIBM    VARCHAR                   8     0 いいえ
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        RE_CLAIM_NO                    SYSIBM    VARCHAR                   8     0 はい
        CLAIM_DATE                     SYSIBM    DATE                      4     0 はい
        CLAIM_MONEY                    SYSIBM    INTEGER                   4     0 はい
        TIMELIMIT_DAY                  SYSIBM    DATE                      4     0 はい
        FORM_NO                        SYSIBM    VARCHAR                   2     0 はい
        REMARK                         SYSIBM    VARCHAR                 150     0 はい
        CLAIM_NONE_FLG                 SYSIBM    VARCHAR                   1     0 はい
        COMPLETE_FLG                   SYSIBM    VARCHAR                   1     0 はい
        ABANDONMENT_FLG                SYSIBM    VARCHAR                   1     0 はい
        PROCEDURE_DIV                  SYSIBM    VARCHAR                   1     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          18 レコードが選択されました。
     */
    public Object[] mapToArray(final Map map) {
        final int claimNo = ((Number) map.get("CLAIM_NO")).intValue();
        final Object[] rtn = {
                _claimNoFormat.format(claimNo),
                "01",    // TODO:固定
                "01",    // TODO:固定
                "01",    // TODO:固定
                map.get("SLIP_NO"),
                map.get("APPLICANTNO"),
                null,
                map.get("CLAIM_DATE"),
                map.get("TOTAL_CLAIM_MONEY"),
                map.get("TIMELIMIT_DAY"),
                null,
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

