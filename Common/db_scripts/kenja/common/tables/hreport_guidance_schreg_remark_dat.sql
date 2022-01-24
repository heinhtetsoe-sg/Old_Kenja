-- $Id: 351d19f709120f9480cee2d75858fc4805568b31 $

drop table HREPORT_GUIDANCE_SCHREG_REMARK_DAT

create table HREPORT_GUIDANCE_SCHREG_REMARK_DAT( \
    YEAR           varchar(4)     not null, \
    SEMESTER       varchar(1)     not null, \
    RECORD_DATE    date           not null, \
    SCHREGNO       varchar(8)     not null, \
    DIV            varchar(2)     not null, \
    SEQ            smallint       not null, \
    REMARK         varchar(6100) , \
    REGISTERCD     varchar(10)   , \
    UPDATED        timestamp      default current timestamp \
 ) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_REMARK_DAT add constraint PK_HR_GU_SCH_R_D \
primary key (YEAR, SEMESTER, RECORD_DATE, SCHREGNO, DIV, SEQ)
