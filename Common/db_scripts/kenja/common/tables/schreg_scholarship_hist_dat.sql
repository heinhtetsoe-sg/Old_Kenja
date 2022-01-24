-- $Id: 467aae559f35a1b18f3550291a0796aa5e64e688 $

drop table SCHREG_SCHOLARSHIP_HIST_DAT
create table SCHREG_SCHOLARSHIP_HIST_DAT( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    SCHOLARSHIP         varchar(2)  not null, \
    SCHREGNO            varchar(8)  not null, \
    FROM_DATE           date        not null, \
    TO_DATE             date, \
    REMARK              varchar(150), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_SCHOLARSHIP_HIST_DAT add constraint PK_SCH_SCHOLARSHIP primary key (SCHOOLCD, SCHOOL_KIND, SCHOLARSHIP, SCHREGNO, FROM_DATE)
