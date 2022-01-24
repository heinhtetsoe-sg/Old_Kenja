-- $Id: a68ac53d3134df6e41c57eb74e988fe1feee3395 $

drop table SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT_OLD

rename table SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT to SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT_OLD

create table SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT( \
    YEAR                                varchar(4)    not null, \
    SCHREGNO                            varchar(8)    not null, \
    RECORD_DATE                         varchar(10)   not null, \
    WRITING_DATE                        date          not null, \
    CHALLENGED_NAMES                    varchar(300), \
    CHALLENGED_STATUS                   varchar(500), \
    RECORD_STAFFNAME                    varchar(75), \
    REGISTERCD                          varchar(10), \
    UPDATED                             timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT add constraint PK_SCH_CHA_ASS_M_D primary key (YEAR, SCHREGNO, RECORD_DATE)

insert into SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT( \
    YEAR, \
    SCHREGNO, \
    RECORD_DATE, \
    WRITING_DATE, \
    CHALLENGED_NAMES, \
    CHALLENGED_STATUS, \
    RECORD_STAFFNAME, \
    REGISTERCD, \
    UPDATED \
 ) select  \
    YEAR, \
    SCHREGNO, \
    RECORD_DATE, \
    WRITING_DATE, \
    CHALLENGED_NAMES, \
    CHALLENGED_STATUS, \
    cast(null as varchar(75)) AS RECORD_STAFFNAME, \
    REGISTERCD, \
    UPDATED \
 from SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT_OLD

