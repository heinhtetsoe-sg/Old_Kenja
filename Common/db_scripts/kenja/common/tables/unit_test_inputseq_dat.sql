-- $Id: 772f70b024474f4120c494dd0ad1ea3f69e12cc2 $

drop table UNIT_TEST_INPUTSEQ_DAT
create table UNIT_TEST_INPUTSEQ_DAT( \
    YEAR                varchar(4)      not null, \
    GRADE               varchar(2)      not null, \
    HR_CLASS            varchar(3)      not null, \
    CLASSCD             varchar(2)      not null, \
    SCHOOL_KIND         varchar(2)      not null, \
    CURRICULUM_CD       varchar(2)      not null, \
    SUBCLASSCD          varchar(6)      not null, \
    SEQ                 smallint        not null, \
    VIEWCD              varchar(4)      not null, \
    VIEWFLG             varchar(1), \
    UNIT_ASSESSHIGH     smallint, \
    WEIGHTING           smallint, \
    WEIGHTING_CALC      smallint, \
    WEIGHTING_EXE       smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table UNIT_TEST_INPUTSEQ_DAT add constraint PK_UNIT_TEST_IPSQ \
    primary key (YEAR, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEQ, VIEWCD)
