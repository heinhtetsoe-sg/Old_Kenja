-- $Id: e4a09fb842856fa0fc409d90bab9f827eb5e53a7 $

drop table SUBCLASS_SEMVAL_PERCENT_SDIV_DAT
create table SUBCLASS_SEMVAL_PERCENT_SDIV_DAT( \
    YEAR                        varchar(4)  not null, \
    SEMESTER                    varchar(1)  not null, \
    GRADE                       varchar(2)  not null, \
    COURSECODE                  varchar(4)  not null, \
    GROUP_CD                    varchar(3)  not null, \
    CLASSCD                     varchar(2)  not null, \
    SCHOOL_KIND                 varchar(2)  not null, \
    CURRICULUM_CD               varchar(2)  not null, \
    SUBCLASSCD                  varchar(6)  not null, \
    COMBINED_TESTKINDCD         varchar(2)  not null, \
    COMBINED_TESTITEMCD         varchar(2)  not null, \
    COMBINED_SCORE_DIV          varchar(2)  not null, \
    SEQ                         smallint  not null, \
    TESTDIV                     varchar(1), \
    ATTEND_SEMESTER             varchar(1)  not null, \
    ATTEND_TESTKINDCD           varchar(2)  not null, \
    ATTEND_TESTITEMCD           varchar(2)  not null, \
    ATTEND_SCORE_DIV            varchar(2)  not null, \
    PROFICIENCYDIV              varchar(2), \
    PROFICIENCYCD               varchar(4), \
    PROFICIENCY_SUBCLASS_CD     varchar(6), \
    PERCENT                     smallint, \
    PERFECT                     smallint, \
    REGISTERCD                  varchar(10), \
    UPDATED                     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_SEMVAL_PERCENT_SDIV_DAT add constraint PK_SEM_PERCSDIVD \
primary key (YEAR, SEMESTER, GRADE, COURSECODE, GROUP_CD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, \
COMBINED_TESTKINDCD, COMBINED_TESTITEMCD, COMBINED_SCORE_DIV, SEQ)
