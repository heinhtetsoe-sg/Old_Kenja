-- $Id: v_collect_s_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW V_COLLECT_S_MST

CREATE VIEW V_COLLECT_S_MST \
   (YEAR, \
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
         AND T1.COLLECT_L_CD = L1.COLLECT_L_CD \
         AND T1.COLLECT_M_CD = L1.COLLECT_M_CD \
         AND T1.COLLECT_S_CD = L1.COLLECT_S_CD \
         AND L1.TOKUSYU_CD = '001' \
    LEFT JOIN COLLECT_S_DETAIL_DAT L2 ON T1.YEAR = L2.YEAR \
         AND T1.COLLECT_L_CD = L2.COLLECT_L_CD \
         AND T1.COLLECT_M_CD = L2.COLLECT_M_CD \
         AND T1.COLLECT_S_CD = L2.COLLECT_S_CD \
         AND L2.TOKUSYU_CD = '001' \
    LEFT JOIN COLLECT_S_DETAIL_DAT L3 ON T1.YEAR = L3.YEAR \
         AND T1.COLLECT_L_CD = L3.COLLECT_L_CD \
         AND T1.COLLECT_M_CD = L3.COLLECT_M_CD \
         AND T1.COLLECT_S_CD = L3.COLLECT_S_CD \
         AND L3.TOKUSYU_CD = '001'
