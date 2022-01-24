-- $Id: b0f74495500fdbeffb9c9bb6212a7f926565e1af $

drop table MOCK_CENTER_DAT
create table MOCK_CENTER_DAT( \
    YEAR                varchar(4)  not null, \
    CENTERCD            varchar(9)  not null, \
    SCHREGNO            varchar(8)  not null, \
    RANK_RANGE          varchar(1)  not null, \
    RANK_DIV            varchar(2)  not null, \
    CENTERDIV           varchar(1)  not null, \
    BUNRIDIV            varchar(1), \
    CENTER_ABSENT       varchar(1), \
    MARK_ABSENT         varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CENTER_DAT add constraint PK_MOCK_CENTER_DAT primary key (YEAR, CENTERCD, SCHREGNO, RANK_RANGE, RANK_DIV, CENTERDIV)
