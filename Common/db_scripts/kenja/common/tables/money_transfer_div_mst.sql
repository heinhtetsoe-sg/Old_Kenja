-- kanji=´Á»ú
-- $Id: 53d0d88b5d5310411b900a72402c13ab595bf1d7 $

drop table MONEY_TRANSFER_DIV_MST

create table MONEY_TRANSFER_DIV_MST ( \
        SCHOOLCD        varchar(12) not null, \
        SCHOOL_KIND     varchar(2)  not null, \
        YEAR            varchar(4)  not null, \
        TRANSFER_DIV    varchar(2)  not null, \
        TRANSFER_NAME   varchar(90), \
        REGISTERCD      varchar(10), \
        UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MONEY_TRANSFER_DIV_MST add constraint PK_MONEY_TRANSFER_DIV_MST primary key (SCHOOLCD, SCHOOL_KIND, YEAR, TRANSFER_DIV)
