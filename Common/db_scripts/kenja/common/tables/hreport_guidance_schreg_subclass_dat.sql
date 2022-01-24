-- $Id: 736ac2b9fbf54601d639980b09d99a87628f2cb1 $

drop table HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT

create table HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT( \
    YEAR                varchar(4)     not null, \
    SEMESTER            varchar(1)     not null, \
    SCHREGNO            varchar(8)     not null, \
    CLASSCD             varchar(2)     not null, \
    SCHOOL_KIND         varchar(2)     not null, \
    CURRICULUM_CD       varchar(2)     not null, \
    SUBCLASSCD          varchar(6)     not null, \
    UNITCD              varchar(2)     not null, \
    GUIDANCE_PATTERN    varchar(1)     not null, \
    SEQ                 smallint       not null, \
    REMARK              varchar(6100) , \
    REGISTERCD          varchar(10)   , \
    UPDATED             timestamp      default current timestamp \
 ) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT add constraint PK_HR_GU_SC_SU_D \
primary key (YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, GUIDANCE_PATTERN, UNITCD, SEQ)
