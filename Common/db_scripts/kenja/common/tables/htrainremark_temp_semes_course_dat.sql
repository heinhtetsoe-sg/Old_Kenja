-- $Id: 003a71b2f62194aa57f284588eab288cb7d51719 $

drop table HTRAINREMARK_TEMP_SEMES_COURSE_DAT
create table HTRAINREMARK_TEMP_SEMES_COURSE_DAT( \
    YEAR             varchar(4)    not null, \
    SEMESTER         varchar(1)    not null, \
    GRADE            varchar(2)    not null, \
    COURSECD         varchar(1)    not null, \
    MAJORCD          varchar(3)    not null, \
    COURSECODE       varchar(4)    not null, \
    CLASSCD          varchar(2)    not null, \
    SCHOOL_KIND      varchar(2)    not null, \
    CURRICULUM_CD    varchar(2)    not null, \
    SUBCLASSCD       varchar(6)    not null, \
    DATA_DIV         varchar(2)    not null, \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HTRAINREMARK_TEMP_SEMES_COURSE_DAT add constraint PK_HTRAINRE_SEM_COURSE primary key \
(YEAR, SEMESTER, GRADE, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
