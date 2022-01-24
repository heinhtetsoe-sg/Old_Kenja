-- $Id: reduction_country_plan_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table REDUCTION_COUNTRY_PLAN_DAT
create table REDUCTION_COUNTRY_PLAN_DAT( \
    YEAR                    varchar(4)  not null, \
    SCHREGNO                varchar(8)  not null, \
    PLAN_YEAR               varchar(4)  not null, \
    PLAN_MONTH              varchar(2)  not null, \
    PLAN_MONEY              integer, \
    PLAN_CANCEL_FLG         varchar(1), \
    PAID_MONEY              integer, \
    PAID_YEARMONTH          varchar(6), \
    PLAN_LOCk_FLG           varchar(1), \
    ADD_PLAN_MONEY          integer, \
    ADD_PLAN_CANCEL_FLG     varchar(1), \
    ADD_PAID_MONEY          integer, \
    ADD_PAID_YEARMONTH      varchar(6), \
    ADD_PLAN_LOCk_FLG       varchar(1), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_PLAN_DAT add constraint PK_REDUCTION_P_DAT primary key(YEAR, SCHREGNO, PLAN_YEAR, PLAN_MONTH)
