-- $Id: 414192e33e8a5287921a283fe1af037991a3212d $

drop table REDUCTION_SLIP_BURDEN_CHARGE_DAT
create table REDUCTION_SLIP_BURDEN_CHARGE_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    REDUCTION_TARGET        varchar(1)  not null, \
    SLIP_NO                 varchar(15) not null, \
    SCHREGNO                varchar(8)  not null, \
    BURDEN_CHARGE1          integer, \
    BURDEN_CHARGE2          integer, \
    TOTAL_BURDEN_CHARGE     integer, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SLIP_BURDEN_CHARGE_DAT add constraint PK_REDUCTION_B_C_D primary key(SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_TARGET, SLIP_NO, SCHREGNO)
