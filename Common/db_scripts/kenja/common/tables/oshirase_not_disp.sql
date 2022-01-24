-- $Id: 034f752972cca83ff61a5d3acbe82bee9769ebc2 $

drop table OSHIRASE_NOT_DISP

create table OSHIRASE_NOT_DISP \
    (SCHOOLCD        varchar(12) not null, \
     SCHOOL_KIND     varchar(2)  not null, \
     DATA_DIV        varchar(2)  not null, \
     OSHIRASE_NO     int         not null, \
     STAFFCD         varchar(10) not null, \
     REGISTERCD      varchar(10), \
     UPDATED         timestamp default current timestamp \
    ) in usr1dms index in idx1dms


alter table OSHIRASE_NOT_DISP add constraint PK_OSHIRASE_NOT_DISP \
primary key (SCHOOLCD, SCHOOL_KIND, DATA_DIV, OSHIRASE_NO, STAFFCD)
