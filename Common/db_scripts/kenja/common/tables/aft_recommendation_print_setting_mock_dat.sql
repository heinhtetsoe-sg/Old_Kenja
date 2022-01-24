-- $Id: aft_recommendation_print_setting_mock_dat.sql 77568 2020-11-19 06:30:30Z ishii $

DROP TABLE AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT
CREATE TABLE AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT( \
    YEAR                varchar(4)    not null, \
    SETTING_SEQ         smallint      not null, \
    SEQ                 smallint      not null, \
    MOCKCD              varchar(9)    not null, \
    SEMESTER            varchar(1)    not null, \
    TESTKINDCD          varchar(2)    not null, \
    TESTITEMCD          varchar(2)    not null, \
    SCORE_DIV           varchar(2)    not null, \
    GRADE               varchar(2)    not null, \
    REGISTERCD          varchar(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE AFT_RECOMMENDATION_PRINT_SETTING_MOCK_DAT ADD CONSTRAINT PK_AFT_REC_PRINT_SETTING_MOCK_D PRIMARY KEY (YEAR, SETTING_SEQ, SEQ, MOCKCD)
