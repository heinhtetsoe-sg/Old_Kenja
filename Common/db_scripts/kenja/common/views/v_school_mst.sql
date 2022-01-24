-- $Id: ba8be9360d8c5a3dadd43986554538cb8fa0b768 $

DROP VIEW V_SCHOOL_MST

CREATE VIEW V_SCHOOL_MST \
   (YEAR, \
    SCHOOLCD, \
    SCHOOL_KIND, \
    FOUNDEDYEAR, \
    PRESENT_EST, \
    CLASSIFICATION, \
    SCHOOLNAME1, \
    SCHOOLNAME2, \
    SCHOOLNAME3, \
    SCHOOLNAME_ENG, \
    SCHOOLZIPCD, \
    SCHOOLADDR1, \
    SCHOOLADDR2, \
    SCHOOLADDR1_ENG, \
    SCHOOLADDR2_ENG, \
    SCHOOLTELNO, \
    SCHOOLFAXNO, \
    SCHOOLMAIL, \
    SCHOOLURL, \
    SCHOOLDIV, \
    SEMESTERDIV, \
    GRADE_HVAL, \
    ENTRANCE_DATE, \
    GRADUATE_DATE, \
    GRAD_CREDITS, \
    GRAD_COMP_CREDITS, \
    SEMES_ASSESSCD, \
    SEMES_FEARVAL, \
    GRADE_FEARVAL, \
    ABSENT_COV, \
    ABSENT_COV_LATE, \
    GVAL_CALC, \
    SUB_OFFDAYS, \
    SUB_ABSENT, \
    SUB_SUSPEND, \
    SUB_MOURNING, \
    SUB_VIRUS, \
    SEM_OFFDAYS, \
    JUGYOU_JISU_FLG, \
    RISYU_BUNSI, \
    RISYU_BUNBO, \
    SYUTOKU_BUNSI, \
    SYUTOKU_BUNBO, \
    RISYU_BUNSI_SPECIAL, \
    RISYU_BUNBO_SPECIAL, \
    SYUTOKU_BUNSI_SPECIAL, \
    SYUTOKU_BUNBO_SPECIAL, \
    JITU_JIFUN, \
    JITU_JIFUN_SPECIAL, \
    JITU_SYUSU, \
    JOUGENTI_SANSYUTU_HOU, \
    AMARI_KURIAGE, \
    KESSEKI_WARN_BUNSI, \
    KESSEKI_WARN_BUNBO, \
    KESSEKI_OUT_BUNSI, \
    KESSEKI_OUT_BUNBO, \
    TOKUBETU_KATUDO_KANSAN, \
    SYUKESSEKI_HANTEI_HOU, \
    SUB_KOUDOME, \
    PREF_CD, \
    KYOUIKU_IINKAI_SCHOOLCD, \
    HOUTEI_SYUSU_SEMESTER1, \
    HOUTEI_SYUSU_SEMESTER2, \
    HOUTEI_SYUSU_SEMESTER3, \
    HOUTEI_SYUSU_SEMESTER4, \
    HOUTEI_SYUSU_SEMESTER5, \
    HOUTEI_SYUSU_SEMESTER6, \
    HOUTEI_SYUSU_SEMESTER7, \
    HOUTEI_SYUSU_SEMESTER8, \
    HOUTEI_SYUSU_SEMESTER9, \
    HOUTEI_SYUSU_SEMESTER10, \
    PARTS_HYOUKA_HYOUTEI_KEISAN) \
AS \
SELECT \
    T1.YEAR, \
    T1.SCHOOLCD, \
    T1.SCHOOL_KIND, \
    T1.FOUNDEDYEAR, \
    T1.PRESENT_EST, \
    T1.CLASSIFICATION, \
    T1.SCHOOLNAME1, \
    T1.SCHOOLNAME2, \
    T1.SCHOOLNAME3, \
    T1.SCHOOLNAME_ENG, \
    T1.SCHOOLZIPCD, \
    T1.SCHOOLADDR1, \
    T1.SCHOOLADDR2, \
    T1.SCHOOLADDR1_ENG, \
    T1.SCHOOLADDR2_ENG, \
    T1.SCHOOLTELNO, \
    T1.SCHOOLFAXNO, \
    T1.SCHOOLMAIL, \
    T1.SCHOOLURL, \
    T1.SCHOOLDIV, \
    T1.SEMESTERDIV, \
    T1.GRADE_HVAL, \
    T1.ENTRANCE_DATE, \
    T1.GRADUATE_DATE, \
    T1.GRAD_CREDITS, \
    T1.GRAD_COMP_CREDITS, \
    T1.SEMES_ASSESSCD, \
    T1.SEMES_FEARVAL, \
    T1.GRADE_FEARVAL, \
    T1.ABSENT_COV, \
    T1.ABSENT_COV_LATE, \
    T1.GVAL_CALC, \
    T1.SUB_OFFDAYS, \
    T1.SUB_ABSENT, \
    T1.SUB_SUSPEND, \
    T1.SUB_MOURNING, \
    T1.SUB_VIRUS, \
    T1.SEM_OFFDAYS, \
    L1.SCHOOL_REMARK1, \
    L1.SCHOOL_REMARK2, \
    L1.SCHOOL_REMARK3, \
    L1.SCHOOL_REMARK4, \
    L1.SCHOOL_REMARK5, \
    L3.SCHOOL_REMARK2, \
    L3.SCHOOL_REMARK3, \
    L3.SCHOOL_REMARK4, \
    L3.SCHOOL_REMARK5, \
    L1.SCHOOL_REMARK6, \
    L3.SCHOOL_REMARK7, \
    L1.SCHOOL_REMARK7, \
    L1.SCHOOL_REMARK8, \
    VALUE(L1.SCHOOL_REMARK9, '99'), \
    L4.SCHOOL_REMARK2, \
    L4.SCHOOL_REMARK3, \
    L4.SCHOOL_REMARK4, \
    L4.SCHOOL_REMARK5, \
    L3.SCHOOL_REMARK6, \
    L5.SCHOOL_REMARK1, \
    L5.SCHOOL_REMARK2, \
    L2.SCHOOL_REMARK1, \
    L2.SCHOOL_REMARK3, \
    L6.SCHOOL_REMARK1, \
    L6.SCHOOL_REMARK2, \
    L6.SCHOOL_REMARK3, \
    L6.SCHOOL_REMARK4, \
    L6.SCHOOL_REMARK5, \
    L6.SCHOOL_REMARK6, \
    L6.SCHOOL_REMARK7, \
    L6.SCHOOL_REMARK8, \
    L6.SCHOOL_REMARK9, \
    L6.SCHOOL_REMARK10, \
    L7.SCHOOL_REMARK1 \
FROM \
    SCHOOL_MST T1 \
    LEFT JOIN SCHOOL_DETAIL_DAT L1 ON T1.YEAR = L1.YEAR \
         AND L1.SCHOOL_SEQ = '001' \
         AND L1.SCHOOLCD = T1.SCHOOLCD \
         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND \
    LEFT JOIN SCHOOL_DETAIL_DAT L2 ON T1.YEAR = L2.YEAR \
         AND L2.SCHOOL_SEQ = '002' \
         AND L2.SCHOOLCD = T1.SCHOOLCD \
         AND L2.SCHOOL_KIND = T1.SCHOOL_KIND \
    LEFT JOIN SCHOOL_DETAIL_DAT L3 ON T1.YEAR = L3.YEAR \
         AND L3.SCHOOL_SEQ = '003' \
         AND L3.SCHOOLCD = T1.SCHOOLCD \
         AND L3.SCHOOL_KIND = T1.SCHOOL_KIND \
    LEFT JOIN SCHOOL_DETAIL_DAT L4 ON T1.YEAR = L4.YEAR \
         AND L4.SCHOOL_SEQ = '004' \
         AND L4.SCHOOLCD = T1.SCHOOLCD \
         AND L4.SCHOOL_KIND = T1.SCHOOL_KIND \
    LEFT JOIN SCHOOL_DETAIL_DAT L5 ON T1.YEAR = L5.YEAR \
         AND L5.SCHOOL_SEQ = '005' \
         AND L5.SCHOOLCD = T1.SCHOOLCD \
         AND L5.SCHOOL_KIND = T1.SCHOOL_KIND \
    LEFT JOIN SCHOOL_DETAIL_DAT L6 ON T1.YEAR = L6.YEAR \
         AND L6.SCHOOL_SEQ = '006' \
         AND L6.SCHOOLCD = T1.SCHOOLCD \
         AND L6.SCHOOL_KIND = T1.SCHOOL_KIND \
    LEFT JOIN SCHOOL_DETAIL_DAT L7 ON T1.YEAR = L7.YEAR \
         AND L7.SCHOOL_SEQ = '007' \
         AND L7.SCHOOLCD = T1.SCHOOLCD \
         AND L7.SCHOOL_KIND = T1.SCHOOL_KIND
