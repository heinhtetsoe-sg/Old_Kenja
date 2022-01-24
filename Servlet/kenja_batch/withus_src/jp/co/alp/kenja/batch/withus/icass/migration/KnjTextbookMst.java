// kanji=漢字
/*
 * $Id: KnjTextbookMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TEXTBOOK_MST を作る。
 * @author takaesu
 * @version $Id: KnjTextbookMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjTextbookMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjTextbookMst.class);
    public static final DecimalFormat _textHakkoshaNo = new DecimalFormat("0000");

    public static final String ICASS_TABLE = "TEXT_MASTER";

    public KnjTextbookMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "教科書マスタ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        // SQL文
        final String sql;
        //sql = "SELECT * FROM " + ICASS_TABLE;
        sql = " WITH MAX_MASTER AS ( "
            + " SELECT "
            + "     KYOKA_CODE, "
            + "     KYOKASHO_SHUMOKU_CODE, "
            + "     TEXT_NO, "
            + "     MAX(KYOIKUKATEI_TEKIYO_NENDO_CODE) AS KYOIKUKATEI_TEKIYO_NENDO_CODE "
            + " FROM "
            + "     TEXT_MASTER "
            + " GROUP BY "
            + "     KYOKA_CODE, "
            + "     KYOKASHO_SHUMOKU_CODE, "
            + "     TEXT_NO "
            + " ORDER BY "
            + "     KYOKA_CODE, "
            + "     KYOKASHO_SHUMOKU_CODE, "
            + "     TEXT_NO "
            + " ) "
            + " SELECT "
            + "     T1.* "
            + " FROM "
            + "     TEXT_MASTER T1, "
            + "     MAX_MASTER T2 "
            + " WHERE "
            + "     T1.KYOKA_CODE = T2.KYOKA_CODE "
            + "     AND T1.KYOKASHO_SHUMOKU_CODE = T2.KYOKASHO_SHUMOKU_CODE "
            + "     AND T1.TEXT_NO = T2.TEXT_NO "
            + "     AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = T2.KYOIKUKATEI_TEKIYO_NENDO_CODE ";
        log.debug("sql=" + sql);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        return (null != result) ? result : Collections.EMPTY_LIST;
    }

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        /*
            [takaesu@withus takaesu]$ db2 describe table textbook_mst

                                           タイプ・
            列名                           スキーマ  タイプ名           長さ    位取り NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            TEXTBOOKCD                     SYSIBM    VARCHAR                   8     0 いいえ
            TEXT_GROUP_CD                  SYSIBM    VARCHAR                   3     0 いいえ
            TEXTBOOKDIV                    SYSIBM    VARCHAR                   1     0 いいえ
            TEXTBOOKNAME                   SYSIBM    VARCHAR                  90     0 いいえ
            TEXTBOOKABBV                   SYSIBM    VARCHAR                  30     0 はい
            TEXTBOOKMK                     SYSIBM    VARCHAR                   9     0 はい
            TEXTBOOKMS                     SYSIBM    VARCHAR                   3     0 はい
            TEXTBOOKWRITINGNAME            SYSIBM    VARCHAR                  60     0 はい
            TEXTBOOKPRICE                  SYSIBM    SMALLINT                  2     0 はい
            TEXTBOOKUNITPRICE              SYSIBM    SMALLINT                  2     0 はい
            TEXTBOOKAMOUNT                 SYSIBM    VARCHAR                   2     0 はい
            ISSUECOMPANYCD                 SYSIBM    VARCHAR                   4     0 いいえ
            ORDER_CD                       SYSIBM    VARCHAR                   4     0 いいえ
            REMARK                         SYSIBM    VARCHAR                  60     0 はい
            REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
            UPDATED                        SYSIBM    TIMESTAMP                10     0 はい

              16 レコードが選択されました。
         */
        final String sql = "INSERT INTO textbook_mst VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, mapToTextbookMstArray(map));
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT件数が1件以外!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("賢者へのINSERTでエラー", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("挿入件数=" + totalCount);
    }

    private Object[] mapToTextbookMstArray(final Map map) {
        /*
            [takaesu@withus takaesu]$ db2 describe table text_master

                                           タイプ・
            列名                           スキーマ  タイプ名           長さ    位取り NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            KYOIKUKATEI_TEKIYO_NENDO_CODE  SYSIBM    VARCHAR                   4     0 いいえ
            KYOKA_CODE                     SYSIBM    VARCHAR                   5     0 いいえ
            KYOKASHO_SHUMOKU_CODE          SYSIBM    VARCHAR                   3     0 いいえ
            TEXT_NO                        SYSIBM    VARCHAR                   3     0 いいえ
            TEXT_NAME                      SYSIBM    VARCHAR                  40     0 はい
            TEXT_HAKKOSHA_NO               SYSIBM    SMALLINT                  2     0 はい
            TANKA                          SYSIBM    SMALLINT                  2     0 はい
            HANBAI_KAISHI_NENGAPPI         SYSIBM    DATE                      4     0 はい
            HANBAI_SHURYO_NENGAPPI         SYSIBM    DATE                      4     0 はい
            TOROKU_DATE                    SYSIBM    TIMESTAMP                10     0 はい
            KOSHIN_DATE                    SYSIBM    TIMESTAMP                10     0 はい

              11 レコードが選択されました。
         */
        // TAKAESU: 以下の文とloadIcassメソッド内のSQL文がまとまっていると見やすいはず。Hoge.java に static メソッドとして集約!?
        final String kyokaCode = (String) map.get("KYOKA_CODE");
        final String kyokashoShumokuCode = (String) map.get("KYOKASHO_SHUMOKU_CODE");
        final String textBookDiv = (null != kyokashoShumokuCode && "999".equals(kyokashoShumokuCode)) ? "3" : "1";
        final String textNo = (String) map.get("TEXT_NO");
        final String textBookCd = kyokaCode + kyokashoShumokuCode + textNo;

        final String hakkoshaNo = (String) map.get("TEXT_HAKKOSHA_NO");
        int iHakkoshaNo = Integer.parseInt(hakkoshaNo);
        if (90 == iHakkoshaNo) {
            iHakkoshaNo = 902; // 発行社コード:ウィザス
        }
        final String issueConpanyCd = _textHakkoshaNo.format(iHakkoshaNo);

        final Object[] rtn = {
                textBookCd,
                kyokashoShumokuCode,
                textBookDiv,// 教科書区分
                map.get("TEXT_NAME"),
                null,
                null,
                null,
                null,
                null,
                map.get("TANKA"),
                "1",// 数量。(とりあえず、固定１で移行する。移行後に目検で修正する)
                issueConpanyCd,
                "0001",// 発注先コード。("0001"で固定)
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

