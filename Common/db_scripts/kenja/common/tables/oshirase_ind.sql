-- $Id: 93d77dd91d6bba44e0b332055e4a319c2ffadd8b $

drop table OSHIRASE_IND

create table OSHIRASE_IND \
    (SCHOOLCD        varchar(12) not null, \
     SCHOOL_KIND     varchar(2)  not null, \
     DATA_DIV        varchar(2)  not null, \
     DATA_NO         int         not null, \
     OSHIRASE_NO     int         not null, \
     STAFFCD         varchar(10), \
     REGISTERCD      varchar(10), \
     UPDATED         timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table OSHIRASE_IND add constraint OSHIRASE_IND \
primary key (SCHOOLCD, SCHOOL_KIND, DATA_DIV, DATA_NO)

create index \
IDX_OSHIRASE_NO on OSHIRASE_IND \
(OSHIRASE_NO ASC) \
pctfree 10 allow reverse scans \
page split symmetric collect sampled detailed statistics;
