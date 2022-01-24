// kanji=漢字
/*
 * $Id: KnjSubclassDetailsMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * TODO: <賢者のテーブル名に書き換えてください。例) REC_REPORT_DAT>を作る。
 * @author takaesu
 * @version $Id: KnjSubclassDetailsMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSubclassDetailsMst extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjSubclassDetailsMst.class);

    public KnjSubclassDetailsMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "科目詳細データ"; }

    void migrate() throws SQLException {
        final String[] nendos = {"2005", "2006", "2007", "2008"};

        for (int i = 0; i < nendos.length; i++) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH R_KADAI AS ( ");
            stb.append(" SELECT ");
            stb.append("     KATEI_CODE, ");
            stb.append("     KYOIKUKATEI_TEKIYO_NENDO_CODE, ");
            stb.append("     KYOKA_CODE, ");
            stb.append("     KAMOKU_CODE, ");
            stb.append("     MAX(CASE WHEN RISHU_KADAI_SHUBETSU_CODE = '1' ");
            stb.append("              THEN INT(JISSHI_NO) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS R_FLG, ");
            stb.append("     SUM(CASE WHEN RISHU_KADAI_SHUBETSU_CODE = '2' ");
            stb.append("              THEN 1 ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS T_FLG ");
            stb.append(" FROM ");
            stb.append("     RISHU_KADAI ");
            stb.append(" WHERE ");
            stb.append("     NENDO_CODE = '" + nendos[i] + "' ");
            stb.append(" GROUP BY ");
            stb.append("     KATEI_CODE, ");
            stb.append("     KYOIKUKATEI_TEKIYO_NENDO_CODE, ");
            stb.append("     KYOKA_CODE, ");
            stb.append("     KAMOKU_CODE ");
            stb.append(" ), R_KAMOKU AS ( ");
            stb.append(" SELECT ");
            stb.append("     KYOIKUKATEI_TEKIYO_NENDO_CODE, ");
            stb.append("     KYOKA_CODE, ");
            stb.append("     KAMOKU_CODE, ");
            stb.append("     MAX(SCHOOLING_JIKAN) AS SCHOOLING_JIKAN ");
            stb.append(" FROM ");
            stb.append("     KAISETSU_RISHU_KAMOKU ");
            stb.append(" WHERE ");
            stb.append("     NENDO_CODE = '" + nendos[i] + "' ");
            stb.append(" GROUP BY ");
            stb.append("     KYOIKUKATEI_TEKIYO_NENDO_CODE, ");
            stb.append("     KYOKA_CODE, ");
            stb.append("     KAMOKU_CODE ");
            stb.append(" ), MAIN_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     '" + nendos[i] + "' AS YEAR, ");
            stb.append("     T1.KYOKA_CODE AS CLASSCD, ");
            stb.append("     L1.NAMECD2 AS CURRICULUM_CD, ");
            stb.append("     T1.KYOKA_CODE || T1.KAMOKU_CODE AS SUBCLASSCD, ");
            stb.append("     SMALLINT(T1.HYOJUN_TANI) AS CREDITS, ");
            stb.append("     CASE WHEN R_KAMOKU.KYOKA_CODE IS NOT NULL ");
            stb.append("          THEN '0' ");
            stb.append("          ELSE '1' ");
            stb.append("     END AS INOUT_DIV, ");
            stb.append("     SMALLINT(R_KAMOKU.SCHOOLING_JIKAN) AS SCHOOLING_SEQ, ");
            stb.append("     CASE WHEN VALUE(R_KADAI.T_FLG, 0) = 0 ");
            stb.append("          THEN null ");
            stb.append("          ELSE '1' ");
            stb.append("     END AS TEST_FLG, ");
            stb.append("     R_KADAI.R_FLG AS REPORT_SEQ ");
            stb.append(" FROM ");
            stb.append("     KAMOKU T1 ");
            stb.append("     LEFT JOIN R_KAMOKU ON T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = R_KAMOKU.KYOIKUKATEI_TEKIYO_NENDO_CODE ");
            stb.append("          AND T1.KYOKA_CODE = R_KAMOKU.KYOKA_CODE ");
            stb.append("          AND T1.KAMOKU_CODE = R_KAMOKU.KAMOKU_CODE ");
            stb.append("     LEFT JOIN R_KADAI ON R_KADAI.KATEI_CODE = '1' ");
            stb.append("          AND R_KAMOKU.KYOIKUKATEI_TEKIYO_NENDO_CODE = R_KADAI.KYOIKUKATEI_TEKIYO_NENDO_CODE ");
            stb.append("          AND R_KAMOKU.KYOKA_CODE = R_KADAI.KYOKA_CODE ");
            stb.append("          AND R_KAMOKU.KAMOKU_CODE = R_KADAI.KAMOKU_CODE ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'W002' ");
            stb.append("          AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L1.NAMESPARE1 AND L1.NAMESPARE2 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     VALUE(NVI.REQUIRE_FLG, '3') AS REQUIRE_FLG ");
            stb.append(" FROM ");
            stb.append("     MAIN_T T1 ");
            stb.append("     LEFT JOIN SUBCLASS_DETAILS_MST_NVI NVI ON NVI.YEAR = '2008' ");
            stb.append("          AND T1.CLASSCD = NVI.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = NVI.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = NVI.SUBCLASSCD ");

            final String sql = stb.toString();
            log.debug("sql=" + sql);

            final List result = _runner.mapListQuery(sql);
            log.debug("データ件数=" + result.size());

            _runner.listToKnj(result, "SUBCLASS_DETAILS_MST", this);
        }
    }

    /** [db2inst1@withus db2inst1]$ db2 describe table SUBCLASS_DETAILS_MST

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        CLASSCD                        SYSIBM    VARCHAR                   2     0 いいえ
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 いいえ
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 いいえ
        CREDITS                        SYSIBM    SMALLINT                  2     0 はい
        INOUT_DIV                      SYSIBM    VARCHAR                   1     0 いいえ
        REQUIRE_FLG                    SYSIBM    VARCHAR                   1     0 いいえ
        SCHOOLING_SEQ                  SYSIBM    SMALLINT                  2     0 はい
        REPORT_SEQ                     SYSIBM    SMALLINT                  2     0 はい
        TEST_FLG                       SYSIBM    VARCHAR                   1     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          12 レコードが選択されました。
     */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("YEAR"),
                map.get("CLASSCD"),
                map.get("CURRICULUM_CD"),
                map.get("SUBCLASSCD"),
                map.get("CREDITS"),
                map.get("INOUT_DIV"),
                map.get("REQUIRE_FLG"),
                map.get("SCHOOLING_SEQ"),
                map.get("REPORT_SEQ"),
                map.get("TEST_FLG"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

