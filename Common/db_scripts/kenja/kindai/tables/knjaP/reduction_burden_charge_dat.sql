-- $Id: reduction_burden_charge_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table REDUCTION_BURDEN_CHARGE_DAT
create table REDUCTION_BURDEN_CHARGE_DAT( \
    YEAR                    varchar(4)    not null, \
    SCHREGNO                varchar(8)    not null, \
    BURDEN_CHARGE1          integer, \
    BURDEN_CHARGE2          integer, \
    TOTAL_BURDEN_CHARGE     integer, \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_BURDEN_CHARGE_DAT add constraint PK_REDUCTION_B_C_D primary key(YEAR, SCHREGNO)
