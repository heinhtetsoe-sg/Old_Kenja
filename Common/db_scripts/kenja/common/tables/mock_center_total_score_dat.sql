-- $Id: 6fd79d391127c9830ea0a6c63bcff3f1297232d3 $

drop table MOCK_CENTER_TOTAL_SCORE_DAT
create table MOCK_CENTER_TOTAL_SCORE_DAT( \
    YEAR                varchar(4)  not null, \
    CENTERCD            varchar(9)  not null, \
    SCHREGNO            varchar(8)  not null, \
    CENTER_CLASS_CD     varchar(2)  not null, \
    CENTER_SUBCLASS_CD  varchar(6)  not null, \
    RANK_RANGE          varchar(1)  not null, \
    RANK_DIV            varchar(2)  not null, \
    CENTERDIV           varchar(1)  not null, \
    SCORE               decimal(5,1), \
    GTZ                 varchar(5), \
    DEVIATION           decimal(5,1), \
    RANK                integer, \
    CNT                 integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CENTER_TOTAL_SCORE_DAT add constraint PK_CENTER_TOTAL primary key (YEAR, CENTERCD, SCHREGNO, CENTER_CLASS_CD, CENTER_SUBCLASS_CD, RANK_RANGE, RANK_DIV, CENTERDIV)
