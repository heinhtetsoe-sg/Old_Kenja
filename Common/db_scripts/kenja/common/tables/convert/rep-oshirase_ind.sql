-- $Id: b9b1511a0a8af5d3fbcdb988eb0420254d675c74 $

drop table OSHIRASE_IND_OLD
create table OSHIRASE_IND_OLD like OSHIRASE_IND
insert into OSHIRASE_IND_OLD select * from OSHIRASE_IND

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

insert into OSHIRASE_IND \
    SELECT \
        SCHOOLCD, \
        SCHOOL_KIND, \
        '01' AS DATA_DIV, \
        DATA_NO, \
        OSHIRASE_NO, \
        STAFFCD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        OSHIRASE_IND_OLD

