-- kanji=漢字
-- $Id: bfd64ab37c8dc966344aaa944e5079c0efeb7f68 $

drop table SUBCLASS_SEMVAL_PERCENT_EXEC_SDIV_DAT
create table SUBCLASS_SEMVAL_PERCENT_EXEC_SDIV_DAT( \
    CALC_DATE           date        not null, \
    CALC_TIME           time        not null, \
    YEAR                varchar(4), \
    SEMESTER            varchar(1), \
    GRADE               varchar(2), \
    COURSECODE          varchar(4), \
    GROUP_CD            varchar(3), \
    CLASSCD             varchar(2), \
    SCHOOL_KIND         varchar(2), \
    CURRICULUM_CD       varchar(2), \
    SUBCLASSCD          varchar(6), \
    COMBINED_TESTKINDCD varchar(2), \
    COMBINED_TESTITEMCD varchar(2), \
    COMBINED_SCORE_DIV  varchar(2), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_SEMVAL_PERCENT_EXEC_SDIV_DAT add constraint PK_SEM_PERCEXESD \
primary key (CALC_DATE, CALC_TIME)
