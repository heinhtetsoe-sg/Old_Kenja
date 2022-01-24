-- $Id: 5a22ce0a28f149504b6d2cea96833159ab70df0d $

drop table HREPORT_GUIDANCE_KIND_DAT

create table HREPORT_GUIDANCE_KIND_DAT( \
    YEAR                varchar(4) not null, \
    GRADE               varchar(2) not null, \
    HR_CLASS            varchar(3) not null, \
    SCHREGNO            varchar(8) not null, \
    KIND_NO             varchar(4) not null, \
    SHOWORDER           smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_KIND_DAT add constraint PK_HREP_GUID_K_D primary key (YEAR, GRADE, HR_CLASS, SCHREGNO, KIND_NO)
