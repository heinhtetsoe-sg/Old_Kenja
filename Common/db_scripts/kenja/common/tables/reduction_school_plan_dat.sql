-- $Id: c193c83459042b414dbf38af9da62fd5aded971f $

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
