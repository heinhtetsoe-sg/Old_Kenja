-- $Id: 6baaf19d57a31238784ce3a8d86954708d391531 $

drop table JVIEWNAME_GRADE_SEMES_DETAIL_MST
create table JVIEWNAME_GRADE_SEMES_DETAIL_MST( \
    YEAR            varchar(4)      not null, \
    SEMESTER        varchar(1)      not null, \
    GRADE           varchar(2)      not null, \
    CLASSCD         varchar(2)      not null, \
    SCHOOL_KIND     varchar(2)      not null, \
    CURRICULUM_CD   varchar(2)      not null, \
    SUBCLASSCD      varchar(6)      not null, \
    VIEWCD          varchar(4)      not null, \
    REMARK1         varchar(500), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table JVIEWNAME_GRADE_SEMES_DETAIL_MST add constraint PK_JVN_GR_SM_D_M \
    primary key (YEAR, SEMESTER, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)
