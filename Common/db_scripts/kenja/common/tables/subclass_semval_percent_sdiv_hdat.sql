-- $Id: e469c046eac55c03e31a250ef9661c05d9c918c4 $

drop table SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT
create table SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT( \
    YEAR                varchar(4)  not null, \
    SEMESTER            varchar(1)  not null, \
    GRADE               varchar(2)  not null, \
    COURSECODE          varchar(4)  not null, \
    GROUP_CD            varchar(3)  not null, \
    CLASSCD             varchar(2)  not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    CURRICULUM_CD       varchar(2)  not null, \
    SUBCLASSCD          varchar(6)  not null, \
    COMBINED_TESTKINDCD varchar(2)  not null, \
    COMBINED_TESTITEMCD varchar(2)  not null, \
    COMBINED_SCORE_DIV  varchar(2)  not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT add constraint PK_SEM_PERCSDIVH \
primary key (YEAR, SEMESTER, GRADE, COURSECODE, GROUP_CD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, \
COMBINED_TESTKINDCD, COMBINED_TESTITEMCD, COMBINED_SCORE_DIV)
