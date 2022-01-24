-- $Id: 932d888c4cd86136972229c23d2f57dcca3488f6 $

drop table SCHREG_CHALLENGED_STATUSSHEET_MAIN_DAT
create table SCHREG_CHALLENGED_STATUSSHEET_MAIN_DAT( \
    YEAR                                varchar(4)    not null, \
    SCHREGNO                            varchar(8)    not null, \
    RECORD_DATE                         date          not null, \
    WRITING_DATE                        date          not null, \
    CHALLENGED_NAMES                    varchar(300), \
    CHALLENGED_STATUS                   varchar(500), \
    RECORD_STAFFNAME                    varchar(75), \
    REGISTERCD                          varchar(10), \
    UPDATED                             timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table SCHREG_CHALLENGED_STATUSSHEET_MAIN_DAT add constraint PK_SCH_CHA_STATSHEET_MAIN_D primary key (YEAR, SCHREGNO, RECORD_DATE)