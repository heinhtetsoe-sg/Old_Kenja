-- $Id: 3edabeff0aa90bcb29411ecab6dd4a20638fdffa $

drop table BANK_HOLIDAY_DAT

create table BANK_HOLIDAY_DAT( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    EXECUTEDATE         date        not null, \
    HOLIDAY_NAME        varchar(150), \
    REMARK              varchar(150), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table BANK_HOLIDAY_DAT add constraint PK_BANK_HOLIDAY primary key (SCHOOLCD, SCHOOL_KIND, EXECUTEDATE)
