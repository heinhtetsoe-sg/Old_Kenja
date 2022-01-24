-- kanji=$B4A;z(B
-- $Id: v_entexam_major_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW V_ENTEXAM_MAJOR_MST
CREATE VIEW V_ENTEXAM_MAJOR_MST \
    (MAJORCD, \
     MAJORLNAME, \
     MAJORLABBV, \
     MAJORSNAME, \
     MAJORSABBV, \
     MAIN_COURSECD, \
     MAIN_MAJORCD \
    ) \
AS SELECT \
    T1.MAJORLCD || L1.MAJORSCD AS MAJORCD, \
    T1.MAJORLNAME, \
    T1.MAJORLABBV, \
    L1.MAJORSNAME, \
    L1.MAJORSABBV, \
    L1.MAIN_COURSECD, \
    L1.MAIN_MAJORCD \
FROM \
    ENTEXAM_MAJORL_MST T1 \
LEFT JOIN \
    ENTEXAM_MAJORS_MST L1 ON L1.MAJORLCD = T1.MAJORLCD 

