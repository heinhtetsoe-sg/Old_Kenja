-- $Id: e0709ae93bf5eb551170f442b7f49c9b778eb9a9 $

drop table HREPORT_SELFRELIANCE_SCHREG_REMARK_DIV_DAT

create table HREPORT_SELFRELIANCE_SCHREG_REMARK_DIV_DAT( \
    YEAR                varchar(4) not null, \
    SEMESTER            varchar(1) not null, \
    SCHREGNO            varchar(8) not null, \
    DIV                 varchar(2) not null, \
    LONG_GOALS          varchar(1500), \
    SHORT_GOALS         varchar(1500), \
    MEANS               varchar(1500), \
    EVALUATION          varchar(1500), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr16dms index in idx1dms

alter table HREPORT_SELFRELIANCE_SCHREG_REMARK_DIV_DAT add constraint PK_HREP_SELFRELIANCE_SCH_RDIV_DAT primary key (YEAR, SEMESTER, SCHREGNO, DIV)
