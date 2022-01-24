-- $Id: 3c0e55462ae743e30264af52c01b2cfa5f9340f9 $

drop table REDUCTION_SCHOOL_PLAN_DAT_OLD

create table REDUCTION_SCHOOL_PLAN_DAT_OLD like REDUCTION_SCHOOL_PLAN_DAT

insert into REDUCTION_SCHOOL_PLAN_DAT_OLD select * from REDUCTION_SCHOOL_PLAN_DAT

drop table REDUCTION_SCHOOL_PLAN_DAT
create table REDUCTION_SCHOOL_PLAN_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SLIP_NO                 varchar(15) not null, \
    REDUCTION_TARGET        varchar(1)  not null, \
    PLAN_YEAR               varchar(4)  not null, \
    PLAN_MONTH              varchar(2)  not null, \
    SCHREGNO                varchar(8)  not null, \
    PLAN_MONEY              integer, \
    DECISION_MONEY          integer, \
    DECISION_MONEY1         integer, \
    DECISION_MONEY2         integer, \
    PLAN_CANCEL_FLG         varchar(1), \
    PAID_MONEY              integer, \
    PAID_YEARMONTH          varchar(6), \
    PLAN_LOCK_FLG           varchar(1), \
    OFFSET_FLG              varchar(1), \
    REFUND_FLG              varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_PLAN_DAT add constraint PK_RED_SCH_PLAN_D primary key(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, REDUCTION_TARGET, PLAN_YEAR, PLAN_MONTH)

INSERT INTO REDUCTION_SCHOOL_PLAN_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    SLIP_NO, \
    REDUCTION_TARGET, \
    PLAN_YEAR, \
    PLAN_MONTH, \
    SCHREGNO, \
    PLAN_MONEY, \
    DECISION_MONEY, \
    CASE WHEN PLAN_MONTH IN ('04', '05', '06') THEN DECISION_MONEY ELSE 0 END AS DECISION_MONEY1, \
    CASE WHEN PLAN_MONTH NOT IN ('04', '05', '06') THEN DECISION_MONEY ELSE 0 END AS DECISION_MONEY2, \
    PLAN_CANCEL_FLG, \
    PAID_MONEY, \
    PAID_YEARMONTH, \
    PLAN_LOCK_FLG, \
    OFFSET_FLG, \
    REFUND_FLG, \
    REGISTERCD, \
    UPDATED \
FROM REDUCTION_SCHOOL_PLAN_DAT_OLD
