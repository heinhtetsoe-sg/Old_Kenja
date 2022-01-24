-- $Id: 49b2ba2b6f9c89c7fb9079bfb474313cb50a9d0c $

drop table ENTEXAM_APPLICANTCONFRPT_DAT
create table ENTEXAM_APPLICANTCONFRPT_DAT( \
    ENTEXAMYEAR        varchar(4)    not null, \
    APPLICANTDIV       varchar(1)    not null, \
    EXAMNO             varchar(5)    not null, \
    CONFIDENTIAL_RPT01 smallint, \
    CONFIDENTIAL_RPT02 smallint, \
    CONFIDENTIAL_RPT03 smallint, \
    CONFIDENTIAL_RPT04 smallint, \
    CONFIDENTIAL_RPT05 smallint, \
    CONFIDENTIAL_RPT06 smallint, \
    CONFIDENTIAL_RPT07 smallint, \
    CONFIDENTIAL_RPT08 smallint, \
    CONFIDENTIAL_RPT09 smallint, \
    CONFIDENTIAL_RPT10 smallint, \
    CONFIDENTIAL_RPT11 smallint, \
    CONFIDENTIAL_RPT12 smallint, \
    ABSENCE_DAYS       smallint, \
    ABSENCE_DAYS2      smallint, \
    ABSENCE_DAYS3      smallint, \
    AVERAGE3           decimal(4,1), \
    AVERAGE5           decimal(4,1), \
    AVERAGE_ALL        decimal(4,1), \
    TOTAL3             smallint, \
    TOTAL5             smallint, \
    TOTAL_ALL          smallint, \
    KASANTEN_ALL       smallint, \
    ABSENCE_REMARK     varchar(90), \
    ABSENCE_REMARK2    varchar(90), \
    ABSENCE_REMARK3    varchar(90), \
    BASE_FLG           varchar(1), \
    HEALTH_FLG         varchar(1), \
    ACTIVE_FLG         varchar(1), \
    RESPONSIBLE_FLG    varchar(1), \
    ORIGINAL_FLG       varchar(1), \
    MIND_FLG           varchar(1), \
    NATURE_FLG         varchar(1), \
    WORK_FLG           varchar(1), \
    JUSTICE_FLG        varchar(1), \
    PUBLIC_FLG         varchar(1), \
    SPECIALACTREC      varchar(90), \
    TOTALSTUDYTIME     varchar(90), \
    SPECIALREPORT      varchar(90), \
    REMARK1            varchar(240), \
    REMARK2            varchar(240), \
    REGISTERCD         varchar(10), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTCONFRPT_DAT add constraint PK_ENTEXAM_APCNRPT primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)