-- kanji=漢字
-- $Id: 52e182aa810d3bf27f7cd192d5418804e0439a9f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT

CREATE TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT  \
(   CANCEL_YEAR      varchar(4) not null , \
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
 
ALTER TABLE ATTEST_CANCEL_SCHREG_STUDYREC_DAT \
ADD CONSTRAINT PK_ATC_SCHREGSTUDY \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, SCHOOLCD, YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
