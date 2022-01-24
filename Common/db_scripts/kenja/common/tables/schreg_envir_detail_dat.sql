-- $Id: 10c12e87c2967a6ccaac29e25d0877be17557acd $

drop table SCHREG_ENVIR_DETAIL_DAT

create table SCHREG_ENVIR_DETAIL_DAT \
    (SCHREGNO           varchar(8)    not null, \
     SEQ                varchar(3)    not null, \
     REMARK1            varchar(150), \
     REMARK2            varchar(150), \
     REMARK3            varchar(150), \
     REMARK4            varchar(150), \
     REMARK5            varchar(150), \
     REMARK6            varchar(150), \
     REMARK7            varchar(150), \
     REMARK8            varchar(150), \
     REMARK9            varchar(150), \
     REMARK10           varchar(150), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_ENVIR_DETAIL_DAT add constraint PK_SCH_ENVIR_DE_D primary key (SCHREGNO, SEQ)
