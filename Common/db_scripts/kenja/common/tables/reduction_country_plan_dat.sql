-- $Id: 57ab812d6416491073764eb2a3d31d45b8e6ddfc $

drop table REDUCTION_COUNTRY_PLAN_DAT
create table REDUCTION_COUNTRY_PLAN_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SLIP_NO                 varchar(15) not null, \
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
    ADD_PLAN_MONEY          integer, \
    ADD_DECISION_MONEY      integer, \
    ADD_DECISION_MONEY1     integer, \
    ADD_DECISION_MONEY2     integer, \
    ADD_PLAN_CANCEL_FLG     varchar(1), \
    ADD_PAID_MONEY          integer, \
    ADD_PAID_YEARMONTH      varchar(6), \
    ADD_PLAN_LOCK_FLG       varchar(1), \
    ADD_OFFSET_FLG          varchar(1), \
    ADD_REFUND_FLG          varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_PLAN_DAT add constraint PK_REDUCTION_P_DAT primary key(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, PLAN_YEAR, PLAN_MONTH)
