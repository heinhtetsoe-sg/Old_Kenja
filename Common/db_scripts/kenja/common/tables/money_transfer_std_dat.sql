-- kanji=´Á»ú
-- $Id: cc4e034e723dccdef94ed52252eea7d8f5d0404d $

drop table MONEY_TRANSFER_STD_DAT

create table MONEY_TRANSFER_STD_DAT ( \
        YEAR            varchar(4)  not null, \
        SCHREGNO        varchar(8)  not null, \
        TRANSFER_DIV    varchar(2)  not null, \
        TRANSFER_MONEY  integer, \
        REGISTERCD      varchar(10), \
        UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MONEY_TRANSFER_STD_DAT add constraint PK_MONEY_TRANSFER_STD_DAT primary key (YEAR, SCHREGNO, TRANSFER_DIV)
