-- $Id: c78f25845480fee5c68f95b104dcd2f490b3ae8f $

drop table SCHREG_DOMITORY_HIST_DAT
create table SCHREG_DOMITORY_HIST_DAT( \
    SCHREGNO        varchar(8)    not null, \
    DOMI_CD         varchar(3)    not null, \
    DOMI_ENTDAY     date          not null, \
    DOMI_OUTDAY     date,         \
    REMARK          varchar(150), \
    REGISTERCD      varchar(10),  \
    UPDATED    timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_DOMITORY_HIST_DAT add constraint PK_SCH_DOMI_HIS_D primary key (SCHREGNO, DOMI_ENTDAY)