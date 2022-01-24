-- $Id$

drop table SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT
create table SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT( \
    YEAR                                varchar(4)    not null, \
    SCHREGNO                            varchar(8)    not null, \
    RECORD_DATE                         varchar(10)   not null, \
    WRITING_DATE                        date          not null, \
    ONES_HOPE_PRESENT                   varchar(460), \
    GUARDIAN_HOPE_PRESENT               varchar(460), \
    ONES_HOPE_FUTURE                    varchar(460), \
    GUARDIAN_HOPE_FUTURE                varchar(460), \
    REASONABLE_ACCOMMODATION            varchar(2800), \
    SELFRELIANCE_GOAL                   varchar(1400), \
    SUPPORT_GOAL                        varchar(620), \
    SUPPORT_PLAN                        varchar(1200), \
    RECORD                              varchar(2550), \
    RECORD_STAFFNAME                    varchar(75), \
    REGISTERCD                          varchar(10), \
    UPDATED                             timestamp default current timestamp, \ 
    CHALLENGED_NAMES                    varchar(270) \
) in usr16dms index in idx1dms

alter table SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT add constraint PK_SCH_CHA_SP_M_D primary key (YEAR, SCHREGNO, RECORD_DATE)
