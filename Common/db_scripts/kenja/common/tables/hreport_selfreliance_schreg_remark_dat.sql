-- $Id: aa3e1226b775e865824cb33eae0c0cc25beb7d7e $

drop table HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT

create table HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT( \
    YEAR                varchar(4) not null, \
    SEMESTER            varchar(1) not null, \
    SCHREGNO            varchar(8) not null, \
    GOALS               varchar(1500), \
    ACCOMMODATION       varchar(1500), \
    KEY_GOALS           varchar(1500), \
    GOALS_REASON        varchar(1500), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr16dms index in idx1dms

alter table HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT add constraint PK_HREP_SELFRELIANCE_SCH_RDAT primary key (YEAR, SEMESTER, SCHREGNO)
