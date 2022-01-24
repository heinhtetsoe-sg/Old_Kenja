-- $Id: c65357e47c4ec496d34dbfedbf56eaa0c4082648 $

drop table REDUCTION_INCOME_DAT
create table REDUCTION_INCOME_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SCHREGNO                varchar(8)  not null, \
    INCOME_1                integer, \
    INCOME_1_1              integer, \
    INCOME_1_2              integer, \
    INCOME_1_3              integer, \
    INCOME_1_4              integer, \
    INCOME_2                integer, \
    INCOME_2_1              integer, \
    INCOME_2_2              integer, \
    INCOME_2_3              integer, \
    INCOME_2_4              integer, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_INCOME_DAT add constraint PK_REDUCTION_INCOME primary key(SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO)

