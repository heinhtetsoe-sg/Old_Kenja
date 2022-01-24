-- $Id: aft_recommendation_print_setting_hr_dat.sql 77568 2020-11-19 06:30:30Z ishii $

DROP TABLE AFT_RECOMMENDATION_PRINT_SETTING_HR_DAT
CREATE TABLE AFT_RECOMMENDATION_PRINT_SETTING_HR_DAT( \
    YEAR                varchar(4)    not null, \
    SETTING_SEQ         smallint      not null, \
    GRADE               varchar(2)    not null, \
    HR_CLASS            varchar(3)    not null, \
    SEMESTER            varchar(1)    not null, \
    TESTKINDCD          varchar(2)    not null, \
    TESTITEMCD          varchar(2)    not null, \
    SCORE_DIV           varchar(2)    not null, \
    REGISTERCD          varchar(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE AFT_RECOMMENDATION_PRINT_SETTING_HR_DAT ADD CONSTRAINT PK_AFT_REC_PRINT_SETTING_HRDAT PRIMARY KEY (YEAR, SETTING_SEQ, GRADE, HR_CLASS)
