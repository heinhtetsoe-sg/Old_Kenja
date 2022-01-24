-- $Id: 8222453fef6991bc892176570646376a9c416ac9 $

drop table SCHREG_SCHOLARSHIP_HIST_DAT_OLD

create table SCHREG_SCHOLARSHIP_HIST_DAT_OLD like SCHREG_SCHOLARSHIP_HIST_DAT

insert into SCHREG_SCHOLARSHIP_HIST_DAT_OLD select * from SCHREG_SCHOLARSHIP_HIST_DAT

drop table SCHREG_SCHOLARSHIP_HIST_DAT

create table SCHREG_SCHOLARSHIP_HIST_DAT( \
    SCHOOLCD            varchar(12)     not null, \
    SCHOOL_KIND         varchar(2)      not null, \
    SCHOLARSHIP         varchar(2)      not null, \
    SCHREGNO            varchar(8)      not null, \
    S_YEAR_MONTH        varchar(7)      not null, \
    E_YEAR_MONTH        varchar(7),   \
    REMARK              varchar(150), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_SCHOLARSHIP_HIST_DAT add constraint PK_SCH_SCHOLARSHIP primary key (SCHOOLCD,SCHOOL_KIND,SCHOLARSHIP,SCHREGNO,S_YEAR_MONTH)

INSERT INTO SCHREG_SCHOLARSHIP_HIST_DAT \
SELECT \
    '000000000000' AS SCHOOLCD, \
    'H' AS SCHOOL_KIND, \
    SCHOLARSHIP, \
    SCHREGNO, \
    SUBSTR(CAST(FROM_DATE AS VARCHAR(10)), 1, 7) AS S_YEAR_MONTH, \
    SUBSTR(CAST(TO_DATE   AS VARCHAR(10)), 1, 7) AS E_YEAR_MONTH, \
    REMARK, \
    REGISTERCD, \
    UPDATED \
FROM SCHREG_SCHOLARSHIP_HIST_DAT_OLD
