-- $Id: reduction_adjustment_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table REDUCTION_ADJUSTMENT_DAT
create table REDUCTION_ADJUSTMENT_DAT( \
    YEAR                          varchar(4)    not null, \
    SCHREGNO                      varchar(8)    not null, \
    LESSON_MONEY1                 integer, \
    LESSON_MONEY2                 integer, \
    TOTAL_LESSON_MONEY            integer, \
    REDUCTIONMONEY1               integer, \
    REDUCTIONMONEY2               integer, \
    TOTAL_REDUCTIONMONEY          integer, \
    REDUCTION_COUNTRY_MONEY1      integer, \
    REDUCTION_COUNTRY_MONEY2      integer, \
    TOTAL_REDUCTION_COUNTRY_MONEY integer, \
    ADJUSTMENT_MONEY1             integer, \
    ADJUSTMENT_MONEY2             integer, \
    TOTAL_ADJUSTMENT_MONEY        integer, \
    REGISTERCD                    varchar(8), \
    UPDATED                       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_ADJUSTMENT_DAT add constraint PK_REDUCTION_A_DAT primary key(YEAR, SCHREGNO)
