-- $Id: v_schreg_base_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW V_SCHREG_BASE_MST

CREATE VIEW V_SCHREG_BASE_MST \
   (SCHREGNO, \
    INOUTCD, \
    NAME, \
    NAME_SHOW, \
    NAME_KANA, \
    NAME_ENG, \
    REAL_NAME, \
    REAL_NAME_KANA, \
    BIRTHDAY, \
    SEX, \
    BLOODTYPE, \
    BLOOD_RH, \
    HANDICAP, \
    NATIONALITY, \
    FINSCHOOLCD, \
    FINISH_DATE, \
    PRISCHOOLCD, \
    ENT_DATE, \
    ENT_DIV, \
    ENT_REASON, \
    ENT_SCHOOL, \
    ENT_ADDR, \
    GRD_DATE, \
    GRD_DIV, \
    GRD_REASON, \
    GRD_SCHOOL, \
    GRD_ADDR, \
    GRD_NO, \
    GRD_TERM, \
    REMARK1, \
    REMARK2, \
    REMARK3, \
    EMERGENCYCALL, \
    EMERGENCYNAME, \
    EMERGENCYRELA_NAME, \
    EMERGENCYTELNO, \
    EMERGENCYCALL2, \
    EMERGENCYNAME2, \
    EMERGENCYRELA_NAME2, \
    EMERGENCYTELNO2, \
    TENGAKU_SAKI_ZENJITU, \
    NYUGAKUMAE_SYUSSIN_JOUHOU, \
    EXAMNO, \
    SATEI_TANNI, \
    JIKOUGAI_NYUURYOKU, \
    TOKKATU_JISU, \
    MUSYOU_KAISU, \
    REGISTERCD, \
    UPDATED) \
AS \
SELECT \
    T1.SCHREGNO, \
    T1.INOUTCD, \
    T1.NAME, \
    T1.NAME_SHOW, \
    T1.NAME_KANA, \
    T1.NAME_ENG, \
    T1.REAL_NAME, \
    T1.REAL_NAME_KANA, \
    T1.BIRTHDAY, \
    T1.SEX, \
    T1.BLOODTYPE, \
    T1.BLOOD_RH, \
    T1.HANDICAP, \
    T1.NATIONALITY, \
    T1.FINSCHOOLCD, \
    T1.FINISH_DATE, \
    T1.PRISCHOOLCD, \
    T1.ENT_DATE, \
    T1.ENT_DIV, \
    T1.ENT_REASON, \
    T1.ENT_SCHOOL, \
    T1.ENT_ADDR, \
    T1.GRD_DATE, \
    T1.GRD_DIV, \
    T1.GRD_REASON, \
    T1.GRD_SCHOOL, \
    T1.GRD_ADDR, \
    T1.GRD_NO, \
    T1.GRD_TERM, \
    T1.REMARK1, \
    T1.REMARK2, \
    T1.REMARK3, \
    T1.EMERGENCYCALL, \
    T1.EMERGENCYNAME, \
    T1.EMERGENCYRELA_NAME, \
    T1.EMERGENCYTELNO, \
    T1.EMERGENCYCALL2, \
    T1.EMERGENCYNAME2, \
    T1.EMERGENCYRELA_NAME2, \
    T1.EMERGENCYTELNO2, \
    L1.BASE_REMARK1, \
    L2.BASE_REMARK1, \
    L3.BASE_REMARK1, \
    L4.BASE_REMARK1, \
    L4.BASE_REMARK5, \
    L4.BASE_REMARK2, \
    L4.BASE_REMARK4, \
    T1.REGISTERCD, \
    T1.UPDATED \
FROM \
    SCHREG_BASE_MST T1 \
    LEFT JOIN SCHREG_BASE_DETAIL_MST L1 ON T1.SCHREGNO = L1.SCHREGNO \
         AND L1.BASE_SEQ = '001' \
    LEFT JOIN SCHREG_BASE_DETAIL_MST L2 ON T1.SCHREGNO = L2.SCHREGNO \
         AND L2.BASE_SEQ = '002' \
    LEFT JOIN SCHREG_BASE_DETAIL_MST L3 ON T1.SCHREGNO = L3.SCHREGNO \
         AND L3.BASE_SEQ = '003' \
    LEFT JOIN SCHREG_BASE_DETAIL_MST L4 ON T1.SCHREGNO = L4.SCHREGNO \
         AND L4.BASE_SEQ = '004'
