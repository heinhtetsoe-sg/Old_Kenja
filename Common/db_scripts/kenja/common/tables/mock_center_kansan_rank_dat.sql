-- $Id: d4c660c34af5b512d6a125f64e4dc37b2071a75a $

drop table MOCK_CENTER_KANSAN_RANK_DAT
create table MOCK_CENTER_KANSAN_RANK_DAT( \
    YEAR                varchar(4)  not null, \
    CENTERCD            varchar(9)  not null, \
    SCHREGNO            varchar(8)  not null, \
    KANSAN_DIV          varchar(2)  not null, \
    BUNRIDIV            varchar(1)  not null, \
    RANK_RANGE          varchar(1)  not null, \
    RANK_DIV            varchar(2)  not null, \
    CENTERDIV           varchar(1)  not null, \
    SCORE               smallint, \
    GTZ                 varchar(5), \
    DEVIATION           decimal(5,1), \
    RANK                integer, \
    CNT                 integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CENTER_KANSAN_RANK_DAT add constraint PK_CENTER_KANSAN primary key (YEAR, CENTERCD, SCHREGNO, KANSAN_DIV, BUNRIDIV, RANK_RANGE, RANK_DIV, CENTERDIV)
