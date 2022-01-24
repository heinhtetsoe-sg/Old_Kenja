-- $Id: 21da602955043be5e4306db5b9dc3a3b95ca28f6 $

drop table REDUCTION_PLAN_DAT_OLD

create table REDUCTION_PLAN_DAT_OLD like REDUCTION_PLAN_DAT

insert into REDUCTION_PLAN_DAT_OLD select * from REDUCTION_PLAN_DAT

drop table REDUCTION_PLAN_DAT
create table REDUCTION_PLAN_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SLIP_NO                 varchar(15) not null, \
    PLAN_YEAR               varchar(4)  not null, \
    PLAN_MONTH              varchar(2)  not null, \
    SCHREGNO                varchar(8)  not null, \
    PLAN_MONEY              integer, \
    DECISION_MONEY          integer, \
    PLAN_CANCEL_FLG         varchar(1), \
    PAID_MONEY              integer, \
    PAID_YEARMONTH          varchar(6), \
    PLAN_LOCK_FLG           varchar(1), \
    OFFSET_FLG              varchar(1), \
    REFUND_FLG              varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_PLAN_DAT add constraint PK_REDUC_PLAN_DAT primary key(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, PLAN_YEAR, PLAN_MONTH)

INSERT INTO REDUCTION_PLAN_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    SLIP_NO, \
    PLAN_YEAR, \
    PLAN_MONTH, \
    SCHREGNO, \
    PLAN_MONEY, \
    DECISION_MONEY, \
    PLAN_CANCEL_FLG, \
    PAID_MONEY, \
    PAID_YEARMONTH, \
    PLAN_LOCK_FLG, \
    CAST(NULL AS VARCHAR(1)) AS OFFSET_FLG, \
    CAST(NULL AS VARCHAR(1)) AS REFUND_FLG, \
    REGISTERCD, \
    UPDATED \
FROM REDUCTION_PLAN_DAT_OLD
