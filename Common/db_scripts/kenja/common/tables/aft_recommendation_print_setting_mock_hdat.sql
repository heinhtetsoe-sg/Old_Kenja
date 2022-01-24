-- $Id: aft_recommendation_print_setting_mock_hdat.sql 77568 2020-11-19 06:30:30Z ishii $

DROP TABLE AFT_RECOMMENDATION_PRINT_SETTING_MOCK_HDAT
CREATE TABLE AFT_RECOMMENDATION_PRINT_SETTING_MOCK_HDAT( \
    YEAR                varchar(4)    not null, \
    SETTING_SEQ         smallint      not null, \
    SEQ                 smallint      not null, \
    SEMESTER            varchar(1)    not null, \
    TESTKINDCD          varchar(2)    not null, \
    TESTITEMCD          varchar(2)    not null, \
    SCORE_DIV           varchar(2)    not null, \
    GRADE               varchar(2)    not null, \
    CHECK_FLG           varchar(1)    , \
    REGISTERCD          varchar(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE AFT_RECOMMENDATION_PRINT_SETTING_MOCK_HDAT ADD CONSTRAINT PK_AFT_REC_PRINT_SETTING_MOCK_HD PRIMARY KEY (YEAR, SETTING_SEQ, SEQ)
