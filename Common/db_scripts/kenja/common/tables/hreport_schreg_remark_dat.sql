-- $Id: daea9b5dea8297d845f37e38e16c73833da6d1b8 $

drop table HREPORT_SCHREG_REMARK_DAT

create table HREPORT_SCHREG_REMARK_DAT( \
    YEAR           varchar(4)     not null, \
    SEMESTER       varchar(1)     not null, \
    SCHREGNO       varchar(8)     not null, \
    DIV            varchar(2)     not null, \
    GOALS          varchar(2000) , \
    REMARK         varchar(2000) , \
    TOTALREMARK    varchar(3500) , \
    REGISTERCD     varchar(10)   , \
    UPDATED        timestamp      default current timestamp \
 ) in usr1dms index in idx1dms

alter table HREPORT_SCHREG_REMARK_DAT add constraint PK_HR_SCH_REMARK_D \
primary key (YEAR, SEMESTER, SCHREGNO, DIV)
