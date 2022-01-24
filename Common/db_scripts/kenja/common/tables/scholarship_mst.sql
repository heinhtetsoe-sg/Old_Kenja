-- kanji=´Á»ú
-- $Id: d8d10fcc5bc0672ed4c732ecd88d2cc492263a2a $

drop table SCHOLARSHIP_MST

create table SCHOLARSHIP_MST ( \
        "SCHOOLCD"          varchar(12) not null, \
        "SCHOOL_KIND"       varchar(2)  not null, \
        "YEAR"              varchar(4)  not null, \
        "SCHOLARSHIP"       varchar(2)  not null, \
        "SCHOLARSHIP_NAME"  varchar(90), \
        "SCHOLARSHIP_ABBV"  varchar(15), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHOLARSHIP_MST add constraint PK_SCHOLARSHIP_MS primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHOLARSHIP)
