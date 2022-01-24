-- $Id: 28240d2083023f2cae4c084b22f03bc641f2f528 $

drop table SCHREG_CHALLENGED_STATUSSHEET_STATUS_GROWUP_DAT
create table SCHREG_CHALLENGED_STATUSSHEET_STATUS_GROWUP_DAT( \
    YEAR                                varchar(4)    not null, \
    SCHREGNO                            varchar(8)    not null, \
    RECORD_DATE                         date          not null, \
    DATA_DIV                            varchar(2)    not null, \
    STATUS                              varchar(3900), \
    GROWUP                              varchar(1800), \
    REGISTERCD                          varchar(10), \
    UPDATED                             timestamp default current timestamp \ 
) in usr16dms index in idx1dms

alter table SCHREG_CHALLENGED_STATUSSHEET_STATUS_GROWUP_DAT add constraint PK_SCH_CHA_STATS_GROW_D primary key (YEAR, SCHREGNO, RECORD_DATE, DATA_DIV)