-- $Id: b35582f72a8c96445c9b2801f13773d8c9096089 $

drop table HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT

create table HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT( \
    YEAR                varchar(4)     not null, \
    SCHREGNO            varchar(8)     not null, \
    CLASSCD             varchar(2)     not null, \
    SCHOOL_KIND         varchar(2)     not null, \
    CURRICULUM_CD       varchar(2)     not null, \
    SUBCLASSCD          varchar(6)     not null, \
    TARGET_CLASSCD      varchar(2)     not null, \
    TARGET_SCHOOL_KIND  varchar(2)     not null, \
    REGISTERCD          varchar(10)   , \
    UPDATED             timestamp      default current timestamp \
 ) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT add constraint PK_HR_GU_SC_TC_D \
primary key (YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TARGET_CLASSCD, TARGET_SCHOOL_KIND)
