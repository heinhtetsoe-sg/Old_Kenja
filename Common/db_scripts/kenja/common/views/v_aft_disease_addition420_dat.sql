-- $Id: 88d194e8c9f0f61dda7f3c2c09889f4dbfb1f58e $

drop view V_AFT_DISEASE_ADDITION420_DAT

CREATE VIEW V_AFT_DISEASE_ADDITION420_DAT \
( \
    EDBOARD_SCHOOLCD, \
    YEAR, \
    COURSECD, \
    MAJORCD, \
    SEX, \
    ZENTAI_GOUKEI, \
    SHINGAKU_GOUKEI, \
    SHUSHOKU_KIBOU_TOTAL_GAKKOU, \
    SHUSHOKU_KIBOU_KENNAI_GAKKOU, \
    SHUSHOKU_KIBOU_KENGAI_GAKKOU, \
    SHUSHOKU_KIBOU_TOTAL_JIBUN, \
    SHUSHOKU_KIBOU_KENNAI_JIBUN, \
    SHUSHOKU_KIBOU_KENGAI_JIBUN, \
    SHUSHOKU_KIBOU_TOTAL_KOUMUIN, \
    SHUSHOKU_KIBOU_KENNAI_KOUMUIN, \
    SHUSHOKU_KIBOU_KENGAI_KOUMUIN, \
    SHUSHOKU_KIBOU_TOTAL_GOUKEI, \
    SHUSHOKU_KIBOU_KENNAI_GOUKEI, \
    SHUSHOKU_KIBOU_KENGAI_GOUKEI, \
    SONOTA_KEIKAKU_ARI, \
    SONOTA_KEIKAKU_NASHI, \
    SHUSHOKU_NAITEI_TOTAL_GAKKOU, \
    SHUSHOKU_NAITEI_KENNAI_GAKKOU, \
    SHUSHOKU_NAITEI_KENGAI_GAKKOU, \
    SHUSHOKU_NAITEI_TOTAL_JIBUN, \
    SHUSHOKU_NAITEI_KENNAI_JIBUN, \
    SHUSHOKU_NAITEI_KENGAI_JIBUN, \
    SHUSHOKU_NAITEI_TOTAL_KOUMUIN, \
    SHUSHOKU_NAITEI_KENNAI_KOUMUIN, \
    SHUSHOKU_NAITEI_KENGAI_KOUMUIN, \
    SHUSHOKU_NAITEI_TOTAL_GOUKEI, \
    SHUSHOKU_NAITEI_KENNAI_GOUKEI, \
    SHUSHOKU_NAITEI_KENGAI_GOUKEI, \
    SHINGAKU_SHUSHOKU, \
    SHUSHOKU_KIBOU_TOTAL_MINAITEI, \
    SHUSHOKU_KIBOU_KENNAI_MINAITEI, \
    SHUSHOKU_KIBOU_KENGAI_MINAITEI, \
    SHINGAKU_IGAI_MIKKETEI \
)   AS \
SELECT \
    T1.EDBOARD_SCHOOLCD, \
    T1.YEAR, \
    T1.COURSECD, \
    T1.MAJORCD, \
    T1.SEX, \
    ZENTAI.COUNT, \
    SHINGAKU.COUNT, \
    KIBOU_GAKKOU_GOUKEI.COUNT, \
    KIBOU_GAKKOU_KENNAI.COUNT, \
    KIBOU_GAKKOU_KENGAI.COUNT, \
    KIBOU_JIBUN_GOUKEI.COUNT, \
    KIBOU_JIBUN_KENNAI.COUNT, \
    KIBOU_JIBUN_KENGAI.COUNT, \
    KIBOU_KOUMUIN_GOUKEI.COUNT, \
    KIBOU_KOUMUIN_KENNAI.COUNT, \
    KIBOU_KOUMUIN_KENGAI.COUNT, \
    KIBOU_GOUKEI_GOUKEI.COUNT, \
    KIBOU_GOUKEI_KENNAI.COUNT, \
    KIBOU_GOUKEI_KENGAI.COUNT, \
    SONOTA_KEIKAKU_ARI.COUNT, \
    SONOTA_KEIKAKU_NASHI.COUNT, \
    NAITEI_GAKKOU_GOUKEI.COUNT, \
    NAITEI_GAKKOU_KENNAI.COUNT, \
    NAITEI_GAKKOU_KENGAI.COUNT, \
    NAITEI_JIBUN_GOUKEI.COUNT, \
    NAITEI_JIBUN_KENNAI.COUNT, \
    NAITEI_JIBUN_KENGAI.COUNT, \
    NAITEI_KOUMUIN_GOUKEI.COUNT, \
    NAITEI_KOUMUIN_KENNAI.COUNT, \
    NAITEI_KOUMUIN_KENGAI.COUNT, \
    NAITEI_GOUKEI_GOUKEI.COUNT, \
    NAITEI_GOUKEI_KENNAI.COUNT, \
    NAITEI_GOUKEI_KENGAI.COUNT, \
    SHINGAKU_SHUSHOKU.COUNT, \
    KIBOU_MINAITEI_GOUKEI.COUNT, \
    KIBOU_MINAITEI_KENNAI.COUNT, \
    KIBOU_MINAITEI_KENGAI.COUNT, \
    SHINGAKU_IGAI_MIKKETEI.COUNT \
FROM \
    (SELECT \
        AFT1.EDBOARD_SCHOOLCD, \
        AFT1.YEAR, \
        AFT1.COURSECD, \
        AFT1.MAJORCD, \
        AFT1.SEX \
     FROM \
        AFT_DISEASE_ADDITION420_DAT AFT1 \
    GROUP BY \
        AFT1.EDBOARD_SCHOOLCD, \
        AFT1.YEAR, \
        AFT1.COURSECD, \
        AFT1.MAJORCD, \
        AFT1.SEX \
    ) T1 \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT ZENTAI ON T1.EDBOARD_SCHOOLCD = ZENTAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = ZENTAI.YEAR    AND T1.COURSECD    = ZENTAI.COURSECD    AND T1.MAJORCD     = ZENTAI.MAJORCD    AND T1.SEX         = ZENTAI.SEX    AND ZENTAI.LARGE_DIV = '99'    AND ZENTAI.MIDDLE_DIV = '99'    AND ZENTAI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT SHINGAKU ON T1.EDBOARD_SCHOOLCD = SHINGAKU.EDBOARD_SCHOOLCD    AND T1.YEAR        = SHINGAKU.YEAR    AND T1.COURSECD    = SHINGAKU.COURSECD    AND T1.MAJORCD     = SHINGAKU.MAJORCD    AND T1.SEX         = SHINGAKU.SEX    AND SHINGAKU.LARGE_DIV = '10'    AND SHINGAKU.MIDDLE_DIV = '99'    AND SHINGAKU.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_GAKKOU_GOUKEI ON T1.EDBOARD_SCHOOLCD = KIBOU_GAKKOU_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_GAKKOU_GOUKEI.YEAR    AND T1.COURSECD    = KIBOU_GAKKOU_GOUKEI.COURSECD    AND T1.MAJORCD     = KIBOU_GAKKOU_GOUKEI.MAJORCD    AND T1.SEX         = KIBOU_GAKKOU_GOUKEI.SEX    AND KIBOU_GAKKOU_GOUKEI.LARGE_DIV = '20'    AND KIBOU_GAKKOU_GOUKEI.MIDDLE_DIV = '01'    AND KIBOU_GAKKOU_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_GAKKOU_KENNAI ON T1.EDBOARD_SCHOOLCD = KIBOU_GAKKOU_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_GAKKOU_KENNAI.YEAR    AND T1.COURSECD    = KIBOU_GAKKOU_KENNAI.COURSECD    AND T1.MAJORCD     = KIBOU_GAKKOU_KENNAI.MAJORCD    AND T1.SEX         = KIBOU_GAKKOU_KENNAI.SEX    AND KIBOU_GAKKOU_KENNAI.LARGE_DIV = '20'    AND KIBOU_GAKKOU_KENNAI.MIDDLE_DIV = '01'    AND KIBOU_GAKKOU_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_GAKKOU_KENGAI ON T1.EDBOARD_SCHOOLCD = KIBOU_GAKKOU_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_GAKKOU_KENGAI.YEAR    AND T1.COURSECD    = KIBOU_GAKKOU_KENGAI.COURSECD    AND T1.MAJORCD     = KIBOU_GAKKOU_KENGAI.MAJORCD    AND T1.SEX         = KIBOU_GAKKOU_KENGAI.SEX    AND KIBOU_GAKKOU_KENGAI.LARGE_DIV = '20'    AND KIBOU_GAKKOU_KENGAI.MIDDLE_DIV = '01'    AND KIBOU_GAKKOU_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_JIBUN_GOUKEI ON T1.EDBOARD_SCHOOLCD = KIBOU_JIBUN_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_JIBUN_GOUKEI.YEAR    AND T1.COURSECD    = KIBOU_JIBUN_GOUKEI.COURSECD    AND T1.MAJORCD     = KIBOU_JIBUN_GOUKEI.MAJORCD    AND T1.SEX         = KIBOU_JIBUN_GOUKEI.SEX    AND KIBOU_JIBUN_GOUKEI.LARGE_DIV = '20'    AND KIBOU_JIBUN_GOUKEI.MIDDLE_DIV = '02'    AND KIBOU_JIBUN_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_JIBUN_KENNAI ON T1.EDBOARD_SCHOOLCD = KIBOU_JIBUN_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_JIBUN_KENNAI.YEAR    AND T1.COURSECD    = KIBOU_JIBUN_KENNAI.COURSECD    AND T1.MAJORCD     = KIBOU_JIBUN_KENNAI.MAJORCD    AND T1.SEX         = KIBOU_JIBUN_KENNAI.SEX    AND KIBOU_JIBUN_KENNAI.LARGE_DIV = '20'    AND KIBOU_JIBUN_KENNAI.MIDDLE_DIV = '02'    AND KIBOU_JIBUN_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_JIBUN_KENGAI ON T1.EDBOARD_SCHOOLCD = KIBOU_JIBUN_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_JIBUN_KENGAI.YEAR    AND T1.COURSECD    = KIBOU_JIBUN_KENGAI.COURSECD    AND T1.MAJORCD     = KIBOU_JIBUN_KENGAI.MAJORCD    AND T1.SEX         = KIBOU_JIBUN_KENGAI.SEX    AND KIBOU_JIBUN_KENGAI.LARGE_DIV = '20'    AND KIBOU_JIBUN_KENGAI.MIDDLE_DIV = '02'    AND KIBOU_JIBUN_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_KOUMUIN_GOUKEI ON T1.EDBOARD_SCHOOLCD = KIBOU_KOUMUIN_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_KOUMUIN_GOUKEI.YEAR    AND T1.COURSECD    = KIBOU_KOUMUIN_GOUKEI.COURSECD    AND T1.MAJORCD     = KIBOU_KOUMUIN_GOUKEI.MAJORCD    AND T1.SEX         = KIBOU_KOUMUIN_GOUKEI.SEX    AND KIBOU_KOUMUIN_GOUKEI.LARGE_DIV = '20'    AND KIBOU_KOUMUIN_GOUKEI.MIDDLE_DIV = '03'    AND KIBOU_KOUMUIN_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_KOUMUIN_KENNAI ON T1.EDBOARD_SCHOOLCD = KIBOU_KOUMUIN_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR = KIBOU_KOUMUIN_KENNAI.YEAR    AND T1.COURSECD    = KIBOU_KOUMUIN_KENNAI.COURSECD    AND T1.MAJORCD     = KIBOU_KOUMUIN_KENNAI.MAJORCD    AND T1.SEX         = KIBOU_KOUMUIN_KENNAI.SEX    AND KIBOU_KOUMUIN_KENNAI.LARGE_DIV = '20'    AND KIBOU_KOUMUIN_KENNAI.MIDDLE_DIV = '03'    AND KIBOU_KOUMUIN_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_KOUMUIN_KENGAI ON T1.EDBOARD_SCHOOLCD = KIBOU_KOUMUIN_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_KOUMUIN_KENGAI.YEAR    AND T1.COURSECD    = KIBOU_KOUMUIN_KENGAI.COURSECD    AND T1.MAJORCD     = KIBOU_KOUMUIN_KENGAI.MAJORCD    AND T1.SEX         = KIBOU_KOUMUIN_KENGAI.SEX    AND KIBOU_KOUMUIN_KENGAI.LARGE_DIV = '20'    AND KIBOU_KOUMUIN_KENGAI.MIDDLE_DIV = '03'    AND KIBOU_KOUMUIN_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_GOUKEI_GOUKEI ON T1.EDBOARD_SCHOOLCD = KIBOU_GOUKEI_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_GOUKEI_GOUKEI.YEAR    AND T1.COURSECD    = KIBOU_GOUKEI_GOUKEI.COURSECD    AND T1.MAJORCD     = KIBOU_GOUKEI_GOUKEI.MAJORCD    AND T1.SEX         = KIBOU_GOUKEI_GOUKEI.SEX    AND KIBOU_GOUKEI_GOUKEI.LARGE_DIV = '20'    AND KIBOU_GOUKEI_GOUKEI.MIDDLE_DIV = '99'    AND KIBOU_GOUKEI_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_GOUKEI_KENNAI ON T1.EDBOARD_SCHOOLCD = KIBOU_GOUKEI_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_GOUKEI_KENNAI.YEAR    AND T1.COURSECD    = KIBOU_GOUKEI_KENNAI.COURSECD    AND T1.MAJORCD     = KIBOU_GOUKEI_KENNAI.MAJORCD    AND T1.SEX         = KIBOU_GOUKEI_KENNAI.SEX    AND KIBOU_GOUKEI_KENNAI.LARGE_DIV = '20'    AND KIBOU_GOUKEI_KENNAI.MIDDLE_DIV = '99'    AND KIBOU_GOUKEI_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_GOUKEI_KENGAI ON T1.EDBOARD_SCHOOLCD = KIBOU_GOUKEI_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_GOUKEI_KENGAI.YEAR    AND T1.COURSECD    = KIBOU_GOUKEI_KENGAI.COURSECD    AND T1.MAJORCD     = KIBOU_GOUKEI_KENGAI.MAJORCD    AND T1.SEX         = KIBOU_GOUKEI_KENGAI.SEX    AND KIBOU_GOUKEI_KENGAI.LARGE_DIV = '20'    AND KIBOU_GOUKEI_KENGAI.MIDDLE_DIV = '99'    AND KIBOU_GOUKEI_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT SONOTA_KEIKAKU_ARI ON T1.EDBOARD_SCHOOLCD = SONOTA_KEIKAKU_ARI.EDBOARD_SCHOOLCD    AND T1.YEAR        = SONOTA_KEIKAKU_ARI.YEAR    AND T1.COURSECD    = SONOTA_KEIKAKU_ARI.COURSECD    AND T1.MAJORCD     = SONOTA_KEIKAKU_ARI.MAJORCD    AND T1.SEX         = SONOTA_KEIKAKU_ARI.SEX    AND SONOTA_KEIKAKU_ARI.LARGE_DIV = '21'    AND SONOTA_KEIKAKU_ARI.MIDDLE_DIV = '99'    AND SONOTA_KEIKAKU_ARI.SMALL_DIV = '003' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT SONOTA_KEIKAKU_NASHI ON T1.EDBOARD_SCHOOLCD = SONOTA_KEIKAKU_NASHI.EDBOARD_SCHOOLCD    AND T1.YEAR        = SONOTA_KEIKAKU_NASHI.YEAR    AND T1.COURSECD    = SONOTA_KEIKAKU_NASHI.COURSECD    AND T1.MAJORCD     = SONOTA_KEIKAKU_NASHI.MAJORCD    AND T1.SEX         = SONOTA_KEIKAKU_NASHI.SEX    AND SONOTA_KEIKAKU_NASHI.LARGE_DIV = '21'    AND SONOTA_KEIKAKU_NASHI.MIDDLE_DIV = '99'    AND SONOTA_KEIKAKU_NASHI.SMALL_DIV = '004' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_GAKKOU_GOUKEI ON T1.EDBOARD_SCHOOLCD = NAITEI_GAKKOU_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_GAKKOU_GOUKEI.YEAR    AND T1.COURSECD    = NAITEI_GAKKOU_GOUKEI.COURSECD    AND T1.MAJORCD     = NAITEI_GAKKOU_GOUKEI.MAJORCD    AND T1.SEX         = NAITEI_GAKKOU_GOUKEI.SEX    AND NAITEI_GAKKOU_GOUKEI.LARGE_DIV = '22'    AND NAITEI_GAKKOU_GOUKEI.MIDDLE_DIV = '01'    AND NAITEI_GAKKOU_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_GAKKOU_KENNAI ON T1.EDBOARD_SCHOOLCD = NAITEI_GAKKOU_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_GAKKOU_KENNAI.YEAR    AND T1.COURSECD    = NAITEI_GAKKOU_KENNAI.COURSECD    AND T1.MAJORCD     = NAITEI_GAKKOU_KENNAI.MAJORCD    AND T1.SEX         = NAITEI_GAKKOU_KENNAI.SEX    AND NAITEI_GAKKOU_KENNAI.LARGE_DIV = '22'    AND NAITEI_GAKKOU_KENNAI.MIDDLE_DIV = '01'    AND NAITEI_GAKKOU_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_GAKKOU_KENGAI ON T1.EDBOARD_SCHOOLCD = NAITEI_GAKKOU_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_GAKKOU_KENGAI.YEAR    AND T1.COURSECD    = NAITEI_GAKKOU_KENGAI.COURSECD    AND T1.MAJORCD     = NAITEI_GAKKOU_KENGAI.MAJORCD    AND T1.SEX         = NAITEI_GAKKOU_KENGAI.SEX    AND NAITEI_GAKKOU_KENGAI.LARGE_DIV = '22'    AND NAITEI_GAKKOU_KENGAI.MIDDLE_DIV = '01'    AND NAITEI_GAKKOU_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_JIBUN_GOUKEI ON T1.EDBOARD_SCHOOLCD = NAITEI_JIBUN_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_JIBUN_GOUKEI.YEAR    AND T1.COURSECD    = NAITEI_JIBUN_GOUKEI.COURSECD    AND T1.MAJORCD     = NAITEI_JIBUN_GOUKEI.MAJORCD    AND T1.SEX         = NAITEI_JIBUN_GOUKEI.SEX    AND NAITEI_JIBUN_GOUKEI.LARGE_DIV = '22'    AND NAITEI_JIBUN_GOUKEI.MIDDLE_DIV = '02'    AND NAITEI_JIBUN_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_JIBUN_KENNAI ON T1.EDBOARD_SCHOOLCD = NAITEI_JIBUN_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_JIBUN_KENNAI.YEAR    AND T1.COURSECD    = NAITEI_JIBUN_KENNAI.COURSECD    AND T1.MAJORCD     = NAITEI_JIBUN_KENNAI.MAJORCD    AND T1.SEX         = NAITEI_JIBUN_KENNAI.SEX    AND NAITEI_JIBUN_KENNAI.LARGE_DIV = '22'    AND NAITEI_JIBUN_KENNAI.MIDDLE_DIV = '02'    AND NAITEI_JIBUN_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_JIBUN_KENGAI ON T1.EDBOARD_SCHOOLCD = NAITEI_JIBUN_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_JIBUN_KENGAI.YEAR    AND T1.COURSECD    = NAITEI_JIBUN_KENGAI.COURSECD    AND T1.MAJORCD     = NAITEI_JIBUN_KENGAI.MAJORCD    AND T1.SEX         = NAITEI_JIBUN_KENGAI.SEX    AND NAITEI_JIBUN_KENGAI.LARGE_DIV = '22'    AND NAITEI_JIBUN_KENGAI.MIDDLE_DIV = '02'    AND NAITEI_JIBUN_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_KOUMUIN_GOUKEI ON T1.EDBOARD_SCHOOLCD = NAITEI_KOUMUIN_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_KOUMUIN_GOUKEI.YEAR    AND T1.COURSECD    = NAITEI_KOUMUIN_GOUKEI.COURSECD    AND T1.MAJORCD     = NAITEI_KOUMUIN_GOUKEI.MAJORCD    AND T1.SEX         = NAITEI_KOUMUIN_GOUKEI.SEX    AND NAITEI_KOUMUIN_GOUKEI.LARGE_DIV = '22'    AND NAITEI_KOUMUIN_GOUKEI.MIDDLE_DIV = '03'    AND NAITEI_KOUMUIN_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_KOUMUIN_KENNAI ON T1.EDBOARD_SCHOOLCD = NAITEI_KOUMUIN_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR = NAITEI_KOUMUIN_KENNAI.YEAR    AND T1.COURSECD    = NAITEI_KOUMUIN_KENNAI.COURSECD    AND T1.MAJORCD     = NAITEI_KOUMUIN_KENNAI.MAJORCD    AND T1.SEX         = NAITEI_KOUMUIN_KENNAI.SEX    AND NAITEI_KOUMUIN_KENNAI.LARGE_DIV = '22'    AND NAITEI_KOUMUIN_KENNAI.MIDDLE_DIV = '03'    AND NAITEI_KOUMUIN_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_KOUMUIN_KENGAI ON T1.EDBOARD_SCHOOLCD = NAITEI_KOUMUIN_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_KOUMUIN_KENGAI.YEAR    AND T1.COURSECD    = NAITEI_KOUMUIN_KENGAI.COURSECD    AND T1.MAJORCD     = NAITEI_KOUMUIN_KENGAI.MAJORCD    AND T1.SEX         = NAITEI_KOUMUIN_KENGAI.SEX    AND NAITEI_KOUMUIN_KENGAI.LARGE_DIV = '22'    AND NAITEI_KOUMUIN_KENGAI.MIDDLE_DIV = '03'    AND NAITEI_KOUMUIN_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_GOUKEI_GOUKEI ON T1.EDBOARD_SCHOOLCD = NAITEI_GOUKEI_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_GOUKEI_GOUKEI.YEAR    AND T1.COURSECD    = NAITEI_GOUKEI_GOUKEI.COURSECD    AND T1.MAJORCD     = NAITEI_GOUKEI_GOUKEI.MAJORCD    AND T1.SEX         = NAITEI_GOUKEI_GOUKEI.SEX    AND NAITEI_GOUKEI_GOUKEI.LARGE_DIV = '22'    AND NAITEI_GOUKEI_GOUKEI.MIDDLE_DIV = '99'    AND NAITEI_GOUKEI_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_GOUKEI_KENNAI ON T1.EDBOARD_SCHOOLCD = NAITEI_GOUKEI_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_GOUKEI_KENNAI.YEAR    AND T1.COURSECD    = NAITEI_GOUKEI_KENNAI.COURSECD    AND T1.MAJORCD     = NAITEI_GOUKEI_KENNAI.MAJORCD    AND T1.SEX         = NAITEI_GOUKEI_KENNAI.SEX    AND NAITEI_GOUKEI_KENNAI.LARGE_DIV = '22'    AND NAITEI_GOUKEI_KENNAI.MIDDLE_DIV = '99'    AND NAITEI_GOUKEI_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT NAITEI_GOUKEI_KENGAI ON T1.EDBOARD_SCHOOLCD = NAITEI_GOUKEI_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = NAITEI_GOUKEI_KENGAI.YEAR    AND T1.COURSECD    = NAITEI_GOUKEI_KENGAI.COURSECD    AND T1.MAJORCD     = NAITEI_GOUKEI_KENGAI.MAJORCD    AND T1.SEX         = NAITEI_GOUKEI_KENGAI.SEX    AND NAITEI_GOUKEI_KENGAI.LARGE_DIV = '22'    AND NAITEI_GOUKEI_KENGAI.MIDDLE_DIV = '99'    AND NAITEI_GOUKEI_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT SHINGAKU_SHUSHOKU ON T1.EDBOARD_SCHOOLCD = SHINGAKU_SHUSHOKU.EDBOARD_SCHOOLCD    AND T1.YEAR        = SHINGAKU_SHUSHOKU.YEAR    AND T1.COURSECD    = SHINGAKU_SHUSHOKU.COURSECD    AND T1.MAJORCD     = SHINGAKU_SHUSHOKU.MAJORCD    AND T1.SEX         = SHINGAKU_SHUSHOKU.SEX    AND SHINGAKU_SHUSHOKU.LARGE_DIV = '23'    AND SHINGAKU_SHUSHOKU.MIDDLE_DIV = '99'    AND SHINGAKU_SHUSHOKU.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_MINAITEI_GOUKEI ON T1.EDBOARD_SCHOOLCD = KIBOU_MINAITEI_GOUKEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_MINAITEI_GOUKEI.YEAR    AND T1.COURSECD    = KIBOU_MINAITEI_GOUKEI.COURSECD    AND T1.MAJORCD     = KIBOU_MINAITEI_GOUKEI.MAJORCD    AND T1.SEX         = KIBOU_MINAITEI_GOUKEI.SEX    AND KIBOU_MINAITEI_GOUKEI.LARGE_DIV = '24'    AND KIBOU_MINAITEI_GOUKEI.MIDDLE_DIV = '99'    AND KIBOU_MINAITEI_GOUKEI.SMALL_DIV = '999' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_MINAITEI_KENNAI ON T1.EDBOARD_SCHOOLCD = KIBOU_MINAITEI_KENNAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_MINAITEI_KENNAI.YEAR    AND T1.COURSECD    = KIBOU_MINAITEI_KENNAI.COURSECD    AND T1.MAJORCD     = KIBOU_MINAITEI_KENNAI.MAJORCD    AND T1.SEX         = KIBOU_MINAITEI_KENNAI.SEX    AND KIBOU_MINAITEI_KENNAI.LARGE_DIV = '24'    AND KIBOU_MINAITEI_KENNAI.MIDDLE_DIV = '99'    AND KIBOU_MINAITEI_KENNAI.SMALL_DIV = '001' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT KIBOU_MINAITEI_KENGAI ON T1.EDBOARD_SCHOOLCD = KIBOU_MINAITEI_KENGAI.EDBOARD_SCHOOLCD    AND T1.YEAR        = KIBOU_MINAITEI_KENGAI.YEAR    AND T1.COURSECD    = KIBOU_MINAITEI_KENGAI.COURSECD    AND T1.MAJORCD     = KIBOU_MINAITEI_KENGAI.MAJORCD    AND T1.SEX         = KIBOU_MINAITEI_KENGAI.SEX    AND KIBOU_MINAITEI_KENGAI.LARGE_DIV = '24'    AND KIBOU_MINAITEI_KENGAI.MIDDLE_DIV = '99'    AND KIBOU_MINAITEI_KENGAI.SMALL_DIV = '002' \
    LEFT JOIN AFT_DISEASE_ADDITION420_DAT SHINGAKU_IGAI_MIKKETEI ON T1.EDBOARD_SCHOOLCD = SHINGAKU_IGAI_MIKKETEI.EDBOARD_SCHOOLCD    AND T1.YEAR        = SHINGAKU_IGAI_MIKKETEI.YEAR    AND T1.COURSECD    = SHINGAKU_IGAI_MIKKETEI.COURSECD    AND T1.MAJORCD     = SHINGAKU_IGAI_MIKKETEI.MAJORCD    AND T1.SEX         = SHINGAKU_IGAI_MIKKETEI.SEX    AND SHINGAKU_IGAI_MIKKETEI.LARGE_DIV = '30'    AND SHINGAKU_IGAI_MIKKETEI.MIDDLE_DIV = '99'    AND SHINGAKU_IGAI_MIKKETEI.SMALL_DIV = '999' AND NAITEI_GAKKOU_KENGAI.SMALL_DIV = '002'