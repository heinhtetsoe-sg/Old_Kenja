drop table ENTEXAM_APPLICANTCONFRPT_DAT

create table ENTEXAM_APPLICANTCONFRPT_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    EXAMNO              varchar(5)  not null, \
    CONFIDENTIAL_RPT01  smallint, \
    CONFIDENTIAL_RPT02  smallint, \
    CONFIDENTIAL_RPT03  smallint, \
    CONFIDENTIAL_RPT04  smallint, \
    CONFIDENTIAL_RPT05  smallint, \
    CONFIDENTIAL_RPT06  smallint, \
    CONFIDENTIAL_RPT07  smallint, \
    CONFIDENTIAL_RPT08  smallint, \
    CONFIDENTIAL_RPT09  smallint, \
    CONFIDENTIAL_RPT10  smallint, \
    CONFIDENTIAL_RPT11  smallint, \
    CONFIDENTIAL_RPT12  smallint, \
    ABSENCE_DAYS        smallint, \
    AVERAGE5            decimal(4,1), \
    AVERAGE_ALL         decimal(4,1), \
    TOTAL_ALL           smallint, \
    KASANTEN_ALL        smallint, \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTCONFRPT_DAT add constraint \
PK_ENTEXAM_APCNRPT primary key (ENTEXAMYEAR, EXAMNO)
