-- $Id: c79ddc544a9774e97c891bf502cc6cfd6e4c47d4 $

drop table SCH_PTRN_PRE_DAT

create table SCH_PTRN_PRE_DAT \
        (YEAR          varchar(4) not null, \
         SEMESTER      varchar(1) not null, \
         PRESEQ        smallint   not null, \
         COURSECD      varchar(1) not null, \
         MAJORCD       varchar(3) not null, \
         GRADE         varchar(2) not null, \
         COURSECODE    varchar(4) not null, \
         PRE_ORDER     smallint   not null, \
         CLASSCD       varchar(2) not null, \
         SCHOOL_KIND   varchar(2) not null, \
         CURRICULUM_CD varchar(2) not null, \
         SUBCLASSCD    varchar(6) not null, \
         REGISTERCD    varchar(10), \
         UPDATED       timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCH_PTRN_PRE_DAT add constraint PK_SCH_PTRN_PRED primary key (YEAR,SEMESTER,PRESEQ,COURSECD,MAJORCD,GRADE,COURSECODE,PRE_ORDER)
