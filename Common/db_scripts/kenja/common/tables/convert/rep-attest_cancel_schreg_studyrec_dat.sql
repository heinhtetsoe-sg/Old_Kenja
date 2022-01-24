-- $Id: ea7749acf2c046e380214f7e78345dc15d3e06f0 $

DROP TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT_OLD
RENAME TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT TO ATTEST_CANCEL_SCHREG_STUDYREC_DAT_OLD
CREATE TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT( \
    CANCEL_YEAR      varchar(4) not null , \
    CANCEL_SEQ       smallint   not null , \
    CANCEL_STAFFCD   varchar(8) not null , \
    SCHOOLCD         varchar(1) not null , \
    YEAR             varchar(4) not null , \
    SCHREGNO         varchar(8) not null , \
    ANNUAL           varchar(2) not null , \
    CLASSCD          varchar(2) not null , \
    SCHOOL_KIND      varchar(2) not null , \
    CURRICULUM_CD    varchar(2) not null , \
    SUBCLASSCD       varchar(6) not null , \
    CLASSNAME        varchar(30)  , \
    CLASSABBV        varchar(15)  , \
    CLASSNAME_ENG    varchar(40)  , \
    CLASSABBV_ENG    varchar(30)  , \
    SUBCLASSES       smallint , \
    SUBCLASSNAME     varchar(90) , \
    SUBCLASSABBV     varchar(90) , \
    SUBCLASSNAME_ENG varchar(40) , \
    SUBCLASSABBV_ENG varchar(20) , \
    VALUATION        smallint , \
    GET_CREDIT       smallint , \
    ADD_CREDIT       smallint , \
    COMP_CREDIT      smallint , \
    PRINT_FLG        varchar(1)  , \
    REGISTERCD       varchar(8)  , \
    UPDATED          timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ATTEST_CANCEL_SCHREG_STUDYREC_DAT \
    SELECT \
        CANCEL_YEAR, \
        CANCEL_SEQ, \
        CANCEL_STAFFCD, \
        SCHOOLCD, \
        YEAR, \
        SCHREGNO, \
        ANNUAL, \
        CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        CLASSNAME, \
        CLASSABBV, \
        CLASSNAME_ENG, \
        CLASSABBV_ENG, \
        SUBCLASSES, \
        SUBCLASSNAME, \
        SUBCLASSABBV, \
        SUBCLASSNAME_ENG, \
        SUBCLASSABBV_ENG, \
        VALUATION, \
        GET_CREDIT, \
        ADD_CREDIT, \
        COMP_CREDIT, \
        PRINT_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ATTEST_CANCEL_SCHREG_STUDYREC_DAT_OLD

ALTER TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT \
ADD CONSTRAINT PK_ATC_SCHREGSTUDY \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, SCHOOLCD, YEAR, SCHREGNO, ANNUAL, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
