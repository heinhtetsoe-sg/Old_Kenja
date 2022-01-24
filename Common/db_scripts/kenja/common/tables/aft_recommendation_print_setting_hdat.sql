-- $Id: aft_recommendation_print_setting_hdat.sql 77568 2020-11-19 06:30:30Z ishii $

DROP TABLE AFT_RECOMMENDATION_PRINT_SETTING_HDAT
CREATE TABLE AFT_RECOMMENDATION_PRINT_SETTING_HDAT( \
    YEAR                varchar(4)    not null, \
    SETTING_SEQ         smallint      not null, \
    SEMESTER            varchar(1)    not null, \
    TESTKINDCD          varchar(2)    not null, \
    TESTITEMCD          varchar(2)    not null, \
    SCORE_DIV           varchar(2)    not null, \
    GRADE               varchar(2)    not null, \
    SETTING_NAME        varchar(90)   , \
    ATTEND_SDATE        varchar(10)   , \
    ATTEND_EDATE        varchar(10)   , \
    SCHOOL_TEST_FLG1    varchar(1)    , \
    G1_FLG              varchar(1)    , \
    G1_SCORE_FLG        varchar(1)    , \
    G1_DEV_FLG          varchar(1)    , \
    G1_VALUATION_FLG    varchar(1)    , \
    G2_FLG              varchar(1)    , \
    G2_SCORE_FLG        varchar(1)    , \
    G2_DEV_FLG          varchar(1)    , \
    G2_VALUATION_FLG    varchar(1)    , \
    G3_FLG              varchar(1)    , \
    G3_SCORE_FLG        varchar(1)    , \
    G3_DEV_FLG          varchar(1)    , \
    G3_VALUATION_FLG    varchar(1)    , \
    VALUATION_TOTAL_FLG varchar(1)    , \
    G1_G3_FLG           varchar(1)    , \
    G1_G2_FLG           varchar(1)    , \
    ALL_FLG             varchar(1)    , \
    QUALIFIED_FLG       varchar(1)    , \
    SCHOOL_TEST_FLG2    varchar(1)    , \
    REGISTERCD          varchar(10)    , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE AFT_RECOMMENDATION_PRINT_SETTING_HDAT ADD CONSTRAINT PK_AFT_REC_PRINT_SETTING_HDAT PRIMARY KEY (YEAR,SETTING_SEQ)