-- $Id: a3212f792a0bb128ebb63c01a33839ddd6fe9ac9 $

drop table REDUCTION_BURDEN_CHARGE_DAT
create table REDUCTION_BURDEN_CHARGE_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SLIP_NO                 varchar(15) not null, \
    SCHREGNO                varchar(8)  not null, \
    BURDEN_CHARGE1          integer, \
    BURDEN_CHARGE2          integer, \
    TOTAL_BURDEN_CHARGE     integer, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_BURDEN_CHARGE_DAT add constraint PK_REDUCTION_B_C_D primary key(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, SCHREGNO)
