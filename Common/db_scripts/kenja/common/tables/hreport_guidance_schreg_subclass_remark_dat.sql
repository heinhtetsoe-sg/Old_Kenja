-- $Id: a03f85c0e6c664ec921e685ca25188b0e9d3c7d4 $

drop table HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT

create table HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT( \
    YEAR           varchar(4)     not null, \
    SEMESTER       varchar(1)     not null, \
    RECORD_DATE    date           not null, \
    SCHREGNO       varchar(8)     not null, \
    DIV            varchar(2)     not null, \
    CLASSCD        varchar(2)     not null, \
    SCHOOL_KIND    varchar(2)     not null, \
    CURRICULUM_CD  varchar(2)     not null, \
    SUBCLASSCD     varchar(6)     not null, \
    REMARK         varchar(6100) , \
    REGISTERCD     varchar(10)   , \
    UPDATED        timestamp      default current timestamp \
 ) in usr16dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT add constraint PK_HR_GU_SCH_SUBR_D \
primary key (YEAR, SEMESTER, RECORD_DATE, SCHREGNO, DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
