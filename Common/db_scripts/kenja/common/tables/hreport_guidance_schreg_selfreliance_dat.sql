-- $Id: bd6097e9843be3e57b6c5833697af79a9d3e14bb $

drop table HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT

create table HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT( \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    SELF_TARGET         varchar(2) not null, \
    SELF_DIV            varchar(1) not null, \
    SELF_SEQ            varchar(2) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT add constraint PK_HREP_GUID_SELFRELIANCE_SCH_DAT primary key (YEAR, SCHREGNO, SELF_TARGET, SELF_DIV, SELF_SEQ)
