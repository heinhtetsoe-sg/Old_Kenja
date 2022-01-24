-- $Id: 99ceed23aaa2dfc93285090f63cdf2e1e9dc4607 $

drop table HREPORT_GUIDANCE_SCHREG_IMPPT_DAT

create table HREPORT_GUIDANCE_SCHREG_IMPPT_DAT \
    (YEAR               varchar(4)      not null, \
     SEMESTER           varchar(1)      not null, \
     SCHREGNO           varchar(8)      not null, \
     REMARK1            varchar(150), \
     REMARK2            varchar(150), \
     REMARK3            varchar(150), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_IMPPT_DAT add constraint PK_HREP_GD_SCH_IMP primary key \
        (YEAR, SEMESTER, SCHREGNO)
