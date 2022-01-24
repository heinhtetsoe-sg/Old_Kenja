-- $Id: 52e3ae124e7c536ba1e3dece5f9fa50dc0e90c86 $

drop table SCHREG_SCHOLARSHIP_HIST_DAT_OLD

create table SCHREG_SCHOLARSHIP_HIST_DAT_OLD like SCHREG_SCHOLARSHIP_HIST_DAT

insert into SCHREG_SCHOLARSHIP_HIST_DAT_OLD select * from SCHREG_SCHOLARSHIP_HIST_DAT

drop table SCHREG_SCHOLARSHIP_HIST_DAT

create table SCHREG_SCHOLARSHIP_HIST_DAT( \
    SCHOOLCD            varchar(12)     not null, \
    SCHOOL_KIND         varchar(2)      not null, \
    SCHOLARSHIP         varchar(2)      not null, \
    SCHREGNO            varchar(8)      not null, \
    FROM_DATE           date            not null, \
    TO_DATE             date,         \
    REMARK              varchar(150), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_SCHOLARSHIP_HIST_DAT add constraint PK_SCH_SCHOLARSHIP primary key (SCHOOLCD, SCHOOL_KIND, SCHOLARSHIP, SCHREGNO, FROM_DATE)

INSERT INTO SCHREG_SCHOLARSHIP_HIST_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    SCHOLARSHIP, \
    SCHREGNO, \
    S_YEAR_MONTH || '-01', \
    LAST_DAY(DATE(E_YEAR_MONTH || '-01')), , \
    REMARK, \
    REGISTERCD, \
    UPDATED \
FROM SCHREG_SCHOLARSHIP_HIST_DAT_OLD
