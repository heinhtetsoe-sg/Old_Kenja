-- $Id: ac20fcc4f827553e9a088aefcc25a05956390d18 $

drop table REDUCTION_BURDEN_CHARGE_PLAN_DAT
create table REDUCTION_BURDEN_CHARGE_PLAN_DAT( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    YEAR                varchar(4)  not null, \
    REDUCTION_TARGET    varchar(1)  not null, \
    SLIP_NO             varchar(15) not null, \
    SCHREGNO            varchar(8)  not null, \
    PLAN_YEAR           varchar(4)  not null, \
    PLAN_MONTH          varchar(2)  not null, \
    BURDEN_CHARGE       integer, \
    BURDEN_CHARGE1      integer, \
    BURDEN_CHARGE2      integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_BURDEN_CHARGE_PLAN_DAT add constraint PK_REDUCTION_BC_PD primary key(SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_TARGET, SLIP_NO, SCHREGNO, PLAN_YEAR, PLAN_MONTH)
