-- $Id: 14cab9716b8eb9b9768be0d52a068c86ed07dde6 $

drop table MOCK_RANK_RANGE_DAT
create table MOCK_RANK_RANGE_DAT( \
    YEAR                varchar(4)  not null, \
    MOCKCD              varchar(9)  not null, \
    SCHREGNO            varchar(8)  not null, \
    MOCK_SUBCLASS_CD    varchar(6)  not null, \
    RANK_RANGE          varchar(1)  not null, \
    RANK_DIV            varchar(2)  not null, \
    MOCKDIV             varchar(1)  not null, \
    SCORE               smallint, \
    GTZ                 varchar(5), \
    DEVIATION           decimal(5,1), \
    RANK                integer, \
    CNT                 integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp, \
    LEVEL               varchar(30) \
) in usr1dms index in idx1dms

alter table MOCK_RANK_RANGE_DAT add constraint PK_MOCK_RANK_RANGE primary key (YEAR, MOCKCD, SCHREGNO, MOCK_SUBCLASS_CD, RANK_RANGE, RANK_DIV, MOCKDIV)
