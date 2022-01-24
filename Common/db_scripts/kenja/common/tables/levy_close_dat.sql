-- kanji=´Á»ú
-- $Id: 18b5d7305751e53c67892e7ff08423ae01ba4dde $

drop table LEVY_CLOSE_DAT

create table LEVY_CLOSE_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "CLOSE_L_CD"            varchar(2)  not null, \
        "INCOME_TOTAL"          integer, \
        "OUTGO_TOTAL"           integer, \
        "ZANKIN_TOTAL"          integer, \
        "CLOSE_FLG"             varchar(1), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CLOSE_DAT add constraint PK_LEVY_CLOSE_DAT primary key (SCHOOLCD, SCHOOL_KIND, YEAR, CLOSE_L_CD)
