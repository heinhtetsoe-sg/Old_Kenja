-- $Id: 4f500bc2c69ef1d6330abedda55b98df8badf0e4 $

drop table MOCK_CENTER_RANK_RANGE_DAT
create table MOCK_CENTER_RANK_RANGE_DAT( \
    YEAR                varchar(4)  not null, \
    CENTERCD            varchar(9)  not null, \
    SCHREGNO            varchar(8)  not null, \
    CENTER_CLASS_CD     varchar(2)  not null, \
    CENTER_SUBCLASS_CD  varchar(6)  not null, \
    RANK_RANGE          varchar(1)  not null, \
    RANK_DIV            varchar(2)  not null, \
    CENTERDIV           varchar(1)  not null, \
    SCORE               smallint, \
    GTZ                 varchar(5), \
    DEVIATION           decimal(5,1), \
    RANK                integer, \
    CNT                 integer, \
    DAIITI_SENTAKU      varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CENTER_RANK_RANGE_DAT add constraint PK_MOCK_CENTER_R_D primary key (YEAR, CENTERCD, SCHREGNO, CENTER_CLASS_CD, CENTER_SUBCLASS_CD, RANK_RANGE, RANK_DIV, CENTERDIV)
