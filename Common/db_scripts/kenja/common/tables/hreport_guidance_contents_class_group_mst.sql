-- $Id: 3f43cf8d389225ba32bc8abb56a5581293df8c46 $

drop table HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST

create table HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ( \
    YEAR            varchar(4) not null, \
    CONDITION       varchar(1) not null, \
    CLASSGROUP_CD   varchar(2) not null, \
    CLASSGROUP_NAME varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST add constraint PK_HGUID_CONT_CLASS_GRP_M primary key (YEAR, CONDITION, CLASSGROUP_CD)

