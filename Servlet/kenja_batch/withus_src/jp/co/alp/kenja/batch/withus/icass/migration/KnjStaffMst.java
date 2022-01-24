// kanji=漢字
/*
 * $Id: KnjStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * STAFF_MST を作る。
 * @author takaesu
 * @version $Id: KnjStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjStaffMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjStaffMst.class);

    public static final DecimalFormat _staffCdFormat = new DecimalFormat("00000000");
    public static final DecimalFormat _jobCdFormat = new DecimalFormat("0000");

    public KnjStaffMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "職員マスタ"; }

    void migrate() throws SQLException {
        /* GAKKO_KANKEISHA.GAKKO_KANKEISHA_SHUBETSU_CODE
            0：システム管理者
            1：学校教職員
            2：法人職員
            3：サポート校職員
            4：アルバイト
            5：協力校職員
            6：面接指導実施施設職員
            7：保護者
            8：業者
            9：官庁
            99：その他
         */
        final List list = loadIcass();
        log.debug("gakko_kankeisha データ件数=" + list.size());

        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        // SQL文
        final String sql;
        sql = "WITH KYOTEN AS ("
            + "  SELECT"
            + "     T1.* "
            + " FROM "
            + "     KINMUSAKI_GAKUSHU_KYOTEN T1, "
            + "     (SELECT "
            + "          GAKKO_KANKEISHA_NO, "
            + "          MAX(SHUNIN_NENGAPPI) AS NENGAPPI "
            + "      FROM "
            + "          KINMUSAKI_GAKUSHU_KYOTEN "
            + "      GROUP BY "
            + "          GAKKO_KANKEISHA_NO "
            + "     ) T2 "
            + " WHERE "
            + "     T1.GAKKO_KANKEISHA_NO = T2.GAKKO_KANKEISHA_NO "
            + "     AND T1.SHUNIN_NENGAPPI = T2.NENGAPPI "
            + " ), SHOKU AS ( "
            + " SELECT "
            + "     T1.* "
            + " FROM "
            + "     SHOKUMEI T1, "
            + "     (SELECT "
            + "          GAKKO_KANKEISHA_NO, "
            + "          MAX(SHUNIN_NENGAPPI) AS NENGAPPI "
            + "      FROM "
            + "          SHOKUMEI "
            + "      GROUP BY "
            + "          GAKKO_KANKEISHA_NO "
            + "     ) T2 "
            + " WHERE "
            + "     T1.GAKKO_KANKEISHA_NO = T2.GAKKO_KANKEISHA_NO "
            + "     AND T1.SHUNIN_NENGAPPI = T2.NENGAPPI "
            + " ) "
            + " SELECT "
            + "     T1.*, "
            + "     T2.GAKUSHU_KYOTEN_CODE, "
            + "     T3.SHOKUMEI_CODE "
            + " FROM "
            + "     GAKKO_KANKEISHA T1 "
            + "     LEFT JOIN KYOTEN T2 ON T1.GAKKO_KANKEISHA_NO=T2.GAKKO_KANKEISHA_NO "
            + "     LEFT JOIN SHOKU T3 ON T1.GAKKO_KANKEISHA_NO=T3.GAKKO_KANKEISHA_NO "
            ;
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
        final String sql = "INSERT INTO STAFF_MST VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, mapToStaffMstArray(map));
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

    private Object[] mapToStaffMstArray(final Map map) {
        /*
            [db2inst1@withus db2inst1]$ db2 describe table STAFF_MST

            列名                           スキーマ  タイプ名           長さ    位取り NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            STAFFCD                        SYSIBM    VARCHAR                   8     0 いいえ
            STAFFNAME                      SYSIBM    VARCHAR                  60     0 はい
            STAFFNAME_SHOW                 SYSIBM    VARCHAR                  15     0 はい
            STAFFNAME_KANA                 SYSIBM    VARCHAR                 120     0 はい
            STAFFNAME_ENG                  SYSIBM    VARCHAR                  60     0 はい
            BELONGING_DIV                  SYSIBM    VARCHAR                   3     0 はい
            JOBCD                          SYSIBM    VARCHAR                   4     0 はい
            SECTIONCD                      SYSIBM    VARCHAR                   4     0 はい
            DUTYSHARECD                    SYSIBM    VARCHAR                   4     0 はい
            CHARGECLASSCD                  SYSIBM    VARCHAR                   1     0 はい
            STAFFSEX                       SYSIBM    VARCHAR                   1     0 はい
            STAFFBIRTHDAY                  SYSIBM    DATE                      4     0 はい
            STAFFZIPCD                     SYSIBM    VARCHAR                   8     0 はい
            STAFFPREF_CD                   SYSIBM    VARCHAR                   2     0 はい
            STAFFADDR1                     SYSIBM    VARCHAR                  75     0 はい
            STAFFADDR2                     SYSIBM    VARCHAR                  75     0 はい
            STAFFADDR3                     SYSIBM    VARCHAR                  75     0 はい
            STAFFTELNO                     SYSIBM    VARCHAR                  14     0 はい
            STAFFTELNO_SEARCH              SYSIBM    VARCHAR                  14     0 はい
            STAFFFAXNO                     SYSIBM    VARCHAR                  14     0 はい
            STAFFE_MAIL                    SYSIBM    VARCHAR                  25     0 はい
            REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
            UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
            
              23 レコードが選択されました。
         */
        // TODO: 実装せよ!
        // TAKAESU: 以下の文とloadIcassメソッド内のSQL文がまとまっていると見やすいはず。Hoge.java に static メソッドとして集約!?
        String shimei = (String) map.get("SHIMEI");
        String sex = (String) map.get("SEIBETSU");
        String staffCd = (String) map.get("GAKKO_KANKEISHA_NO");
        final String shokumei = (String) map.get("shokumei_code");
        final Object[] rtn = {
                _staffCdFormat.format(Integer.parseInt(staffCd)),//TODO: ゼロ埋めせよ
                shimei,
                shimei.length() > 5 ? shimei.substring(0, 5) : shimei,
                map.get("KANA_SHIMEI"),
                map.get(""),// 5.職員氏名英字
                map.get("gakushu_kyoten_code"),
                null == shokumei ? null : _jobCdFormat.format(Integer.parseInt(shokumei)),//TODO: ゼロ埋めせよ
                map.get(""),// 8.所属コード
                map.get(""),// 9.校務分掌部コード
                map.get(""),// 10.授業受持区分
                null != sex && sex.equals("男") ? "1" : "2", // 11.職員性別
                map.get("SEINENGAPPI"),
                map.get("YUBIN_NO"),
                map.get("TODOFUKEN_NO"),
                map.get("ADDRESS1"),// 15.職員住所1
                map.get(""),// 16.職員FAX番号
                map.get("ADDRESS2"),
                map.get("KEITAI_TEL_NO"),
                map.get(""),// 19.職員電話番号(検索用)
                map.get(""),// 20.職員FAX番号
                map.get("PC_E_MAIL"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

