-- $Id: d1f5746f7a0d11a9403fd5479644599685ff6dbf $

drop table HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT

create table HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT ( \
    YEAR            varchar(4) not null, \
    CONDITION       varchar(1) not null, \
    CLASSGROUP_CD   varchar(2) not null, \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT add constraint PK_HGUID_CONT_CLASS_GRP_D primary key (YEAR, CONDITION, CLASSGROUP_CD, CLASSCD, SCHOOL_KIND)

