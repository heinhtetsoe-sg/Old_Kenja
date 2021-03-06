-- $Id: 99048b4e37c544c58ee5732b518f183afd8d3ec2 $

DROP VIEW V_COLLECT_S_MST

CREATE VIEW V_COLLECT_S_MST \
   (SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    COLLECT_L_CD, \
    COLLECT_M_CD, \
    COLLECT_S_CD, \
    COLLECT_S_NAME, \
    COLLECT_S_MONEY, \
    SEX, \
    GRD_YOTEI, \
    MUSYOU) \
AS \
SELECT \
    T1.SCHOOLCD, \
    T1.SCHOOL_KIND, \
    T1.YEAR, \
    T1.COLLECT_L_CD, \
    T1.COLLECT_M_CD, \
    T1.COLLECT_S_CD, \
    T1.COLLECT_S_NAME, \
    T1.COLLECT_S_MONEY, \
    L1.TOKUSYU_VAL AS SEX, \
    L2.TOKUSYU_VAL AS GRD_YOTEI, \
    L3.TOKUSYU_VAL AS MUSYOU \
FROM \
    COLLECT_S_MST T1 \
    LEFT JOIN COLLECT_S_DETAIL_DAT L1 ON T1.YEAR = L1.YEAR \
         AND T1.SCHOOLCD     = L1.SCHOOLCD \
         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND \
         AND T1.COLLECT_L_CD = L1.COLLECT_L_CD \
         AND T1.COLLECT_M_CD = L1.COLLECT_M_CD \
         AND T1.COLLECT_S_CD = L1.COLLECT_S_CD \
         AND L1.TOKUSYU_CD = '001' \
    LEFT JOIN COLLECT_S_DETAIL_DAT L2 ON T1.YEAR = L2.YEAR \
         AND T1.SCHOOLCD     = L2.SCHOOLCD \
         AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND \
         AND T1.COLLECT_L_CD = L2.COLLECT_L_CD \
         AND T1.COLLECT_M_CD = L2.COLLECT_M_CD \
         AND T1.COLLECT_S_CD = L2.COLLECT_S_CD \
         AND L2.TOKUSYU_CD = '002' \
    LEFT JOIN COLLECT_S_DETAIL_DAT L3 ON T1.YEAR = L3.YEAR \
         AND T1.SCHOOLCD     = L3.SCHOOLCD \
         AND T1.SCHOOL_KIND  = L3.SCHOOL_KIND \
         AND T1.COLLECT_L_CD = L3.COLLECT_L_CD \
         AND T1.COLLECT_M_CD = L3.COLLECT_M_CD \
         AND T1.COLLECT_S_CD = L3.COLLECT_S_CD \
         AND L3.TOKUSYU_CD = '003'
