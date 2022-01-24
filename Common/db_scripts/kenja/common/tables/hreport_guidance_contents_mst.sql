-- $Id: 845a457f81e62ef21e84580e483a56bb1ff4ab11 $

drop table HREPORT_GUIDANCE_CONTENTS_MST

create table HREPORT_GUIDANCE_CONTENTS_MST ( \
    YEAR                varchar(4) not null, \
    CLASSCD             varchar(2) not null, \
    SCHOOL_KIND         varchar(2) not null, \
    STEP_CD             varchar(2) not null, \
    LEARNING_CONTENT_CD varchar(2) not null, \
    GUIDANCE_CONTENT_CD varchar(2) not null, \
    LEARNING_CONTENT    varchar(600), \
    GUIDANCE_CONTENT    varchar(600), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_CONTENTS_MST add constraint PK_HGUID_CONT_M primary key (YEAR, CLASSCD, SCHOOL_KIND, STEP_CD, LEARNING_CONTENT_CD, GUIDANCE_CONTENT_CD)
