-- $Id: rep-finschool_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE FINSCHOOL_MST_OLD

CREATE TABLE FINSCHOOL_MST_OLD LIKE FINSCHOOL_MST

INSERT INTO FINSCHOOL_MST_OLD SELECT * FROM FINSCHOOL_MST

DROP TABLE FINSCHOOL_MST

CREATE TABLE FINSCHOOL_MST \
    (FINSCHOOLCD         VARCHAR(7) NOT NULL, \
     FINSCHOOL_TIPE      VARCHAR(1), \
     FINSCHOOL_DISTCD    VARCHAR(3), \
     FINSCHOOL_DISTCD2   VARCHAR(3), \
     FINSCHOOL_DIV       VARCHAR(1), \
     FINSCHOOL_NAME      VARCHAR(75), \
     FINSCHOOL_KANA      VARCHAR(75), \
     FINSCHOOL_NAME_ABBV VARCHAR(30), \
     FINSCHOOL_KANA_ABBV VARCHAR(75), \
     PRINCNAME           VARCHAR(60), \
     PRINCNAME_SHOW      VARCHAR(30), \
     PRINCKANA           VARCHAR(120), \
     DISTRICTCD          VARCHAR(2), \
     FINSCHOOL_ZIPCD     VARCHAR(8), \
     FINSCHOOL_ADDR1     VARCHAR(75), \
     FINSCHOOL_ADDR2     VARCHAR(75), \
     FINSCHOOL_TELNO     VARCHAR(14), \
     FINSCHOOL_FAXNO     VARCHAR(14), \
     EDBOARDCD           VARCHAR(6), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE FINSCHOOL_MST ADD CONSTRAINT PK_FINSCHOOL_MST PRIMARY KEY (FINSCHOOLCD)

INSERT INTO FINSCHOOL_MST \
SELECT \
    FINSCHOOLCD, \
    FINSCHOOL_TIPE, \
    FINSCHOOL_DISTCD, \
    CAST(NULL AS VARCHAR(3)) AS FINSCHOOL_DISTCD2, \
    FINSCHOOL_DIV, \
    FINSCHOOL_NAME, \
    FINSCHOOL_KANA, \
    FINSCHOOL_NAME_ABBV, \
    FINSCHOOL_KANA_ABBV, \
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
    REGISTERCD, \
    UPDATED \
FROM \
    FINSCHOOL_MST_OLD

