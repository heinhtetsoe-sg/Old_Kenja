-- $Id: 66696bde1f5b1a976079b4b2dc8b6d860ed507f1 $

drop table ATTEND_SYUSU_MST
create table ATTEND_SYUSU_MST( \
    YEAR        varchar(4) not null, \
    MONTH       varchar(2) not null, \
    SEMESTER    varchar(1) not null, \
    GRADE       varchar(2) not null, \
    COURSECD    varchar(1) not null, \
    MAJORCD     varchar(3) not null, \
    SYUSU       smallint, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_SYUSU_MST add constraint PK_ATTEND_SYUSU primary key (YEAR, MONTH, SEMESTER, GRADE, COURSECD, MAJORCD)
