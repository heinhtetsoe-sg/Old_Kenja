-- $Id: 97a6d0622cca878e7039eca81335b903cc626e66 $

drop table ENTEXAM_APPLICANTCONFRPT_DAT_OLD
create table ENTEXAM_APPLICANTCONFRPT_DAT_OLD like ENTEXAM_APPLICANTCONFRPT_DAT
insert into ENTEXAM_APPLICANTCONFRPT_DAT_OLD select * from ENTEXAM_APPLICANTCONFRPT_DAT

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

insert into ENTEXAM_APPLICANTCONFRPT_DAT \
SELECT \
    T1.ENTEXAMYEAR, \
    BASE.APPLICANTDIV, \
    T1.EXAMNO, \
    T1.CONFIDENTIAL_RPT01, \
    T1.CONFIDENTIAL_RPT02, \
    T1.CONFIDENTIAL_RPT03, \
    T1.CONFIDENTIAL_RPT04, \
    T1.CONFIDENTIAL_RPT05, \
    T1.CONFIDENTIAL_RPT06, \
    T1.CONFIDENTIAL_RPT07, \
    T1.CONFIDENTIAL_RPT08, \
    T1.CONFIDENTIAL_RPT09, \
    T1.CONFIDENTIAL_RPT10, \
    T1.CONFIDENTIAL_RPT11, \
    T1.CONFIDENTIAL_RPT12, \
    T1.ABSENCE_DAYS, \
    T1.ABSENCE_DAYS2, \
    T1.ABSENCE_DAYS3, \
    T1.AVERAGE3, \
    T1.AVERAGE5, \
    T1.AVERAGE_ALL, \
    T1.TOTAL3, \
    T1.TOTAL5, \
    T1.TOTAL_ALL, \
    T1.KASANTEN_ALL, \
    T1.ABSENCE_REMARK, \
    T1.ABSENCE_REMARK2, \
    T1.ABSENCE_REMARK3, \
    T1.BASE_FLG, \
    T1.HEALTH_FLG, \
    T1.ACTIVE_FLG, \
    T1.RESPONSIBLE_FLG, \
    T1.ORIGINAL_FLG, \
    T1.MIND_FLG, \
    T1.NATURE_FLG, \
    T1.WORK_FLG, \
    T1.JUSTICE_FLG, \
    T1.PUBLIC_FLG, \
    T1.SPECIALACTREC, \
    T1.TOTALSTUDYTIME, \
    T1.SPECIALREPORT, \
    T1.REMARK1, \
    T1.REMARK2, \
    T1.REGISTERCD, \
    T1.UPDATED \
FROM \
    ENTEXAM_APPLICANTCONFRPT_DAT_OLD T1 \
    INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE \
        ON  T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR \
        AND T1.EXAMNO = BASE.EXAMNO
