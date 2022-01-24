-- $Id: 49c35e7e100dace897661f96cdcb3012a40d7d3a $

drop table COLLECT_SGL_SCHOLARSHIP_MAPPING_DAT

create table COLLECT_SGL_SCHOLARSHIP_MAPPING_DAT \
( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2) not null, \
    YEAR                varchar(4) not null, \
    SCHOLARSHIP         varchar(2) not null, \
    SGL_SCHOLARSHIP_CD  varchar(1) not null, \
    SGL_SCHOLARSHIP_DIV varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_SCHOLARSHIP_MAPPING_DAT add constraint PK_SGL_SCHOLA_MAP primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHOLARSHIP, SGL_SCHOLARSHIP_CD)
