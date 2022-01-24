-- $Id: v_finschool_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW V_FINSCHOOL_MST

CREATE VIEW V_FINSCHOOL_MST \
  ( YEAR, \
    FINSCHOOLCD, \
    FINSCHOOL_DISTCD, \
    FINSCHOOL_NAME, \
    FINSCHOOL_KANA, \
    PRINCNAME, \
    PRINCNAME_SHOW, \
    PRINCKANA, \
    DISTRICTCD, \
    FINSCHOOL_ZIPCD, \
    FINSCHOOL_ADDR1, \
    FINSCHOOL_ADDR2, \
    FINSCHOOL_TELNO, \
    FINSCHOOL_FAXNO, \
    EDBOARDCD, \
    UPDATED ) \
AS SELECT \
    T1.YEAR, \
    T2.FINSCHOOLCD, \
    T2.FINSCHOOL_DISTCD, \
    T2.FINSCHOOL_NAME, \
    T2.FINSCHOOL_KANA, \
    T2.PRINCNAME, \
    T2.PRINCNAME_SHOW, \
    T2.PRINCKANA, \
    T2.DISTRICTCD, \
    T2.FINSCHOOL_ZIPCD, \
    T2.FINSCHOOL_ADDR1, \
    T2.FINSCHOOL_ADDR2, \
    T2.FINSCHOOL_TELNO, \
    T2.FINSCHOOL_FAXNO, \
    T2.EDBOARDCD, \
    T2.UPDATED \
FROM     FINSCHOOL_YDAT T1, \
    FINSCHOOL_MST T2 \
WHERE    T1.FINSCHOOLCD = T2.FINSCHOOLCD

