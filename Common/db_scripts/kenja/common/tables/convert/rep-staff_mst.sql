-- $Id: e78a1444ae79dfd0ec68cc8156bc0a70fd1ae77c $

DROP VIEW V_STAFF_MST
drop table STAFF_MST_OLD
create table STAFF_MST_OLD like STAFF_MST
insert into  STAFF_MST_OLD select * from STAFF_MST

drop   table STAFF_MST
CREATE TABLE STAFF_MST( \
    STAFFCD             VARCHAR(10)    NOT NULL, \
    STAFFNAME           VARCHAR(60), \
    STAFFNAME_SHOW      VARCHAR(60), \
    STAFFNAME_KANA      VARCHAR(120), \
    STAFFNAME_ENG       VARCHAR(60), \
    STAFFNAME_REAL      VARCHAR(120), \
    STAFFNAME_KANA_REAL VARCHAR(240), \
    JOBCD               VARCHAR(4), \
    SECTIONCD           VARCHAR(4), \
    DUTYSHARECD         VARCHAR(4), \
    CHARGECLASSCD       VARCHAR(1), \
    STAFFSEX            VARCHAR(1), \
    STAFFBIRTHDAY       DATE, \
    STAFFZIPCD          VARCHAR(8), \
    STAFFADDR1          VARCHAR(150), \
    STAFFADDR2          VARCHAR(150), \
    STAFFTELNO          VARCHAR(14), \
    STAFFFAXNO          VARCHAR(14), \
    STAFFE_MAIL         VARCHAR(50), \
    EDBOARD_STAFFCD     VARCHAR(10), \
    EDBOARD_TORIKOMI_FLG VARCHAR(1), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STAFF_MST ADD CONSTRAINT PK_STAFF_MST PRIMARY KEY (STAFFCD)

INSERT INTO STAFF_MST \
    SELECT \
        STAFFCD, \
        STAFFNAME, \
        STAFFNAME_SHOW, \
        STAFFNAME_KANA, \
        STAFFNAME_ENG, \
        STAFFNAME_REAL, \
        STAFFNAME_KANA_REAL, \
        JOBCD, \
        SECTIONCD, \
        DUTYSHARECD, \
        CHARGECLASSCD, \
        STAFFSEX, \
        STAFFBIRTHDAY, \
        STAFFZIPCD, \
        STAFFADDR1, \
        STAFFADDR2, \
        STAFFTELNO, \
        STAFFFAXNO, \
        STAFFE_MAIL, \
        EDBOARD_STAFFCD, \
        EDBOARD_TORIKOMI_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        STAFF_MST_OLD