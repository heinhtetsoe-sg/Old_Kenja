-- $Id: f39bce2d62885e86982e987524b9a0388ded671e $

drop table OSHIRASE_GRP_OLD
create table OSHIRASE_GRP_OLD like OSHIRASE_GRP
insert into OSHIRASE_GRP_OLD select * from OSHIRASE_GRP

drop table OSHIRASE_GRP

create table OSHIRASE_GRP \
    (SCHOOLCD        varchar(12) not null, \
     SCHOOL_KIND     varchar(2)  not null, \
     DATA_DIV        varchar(2)  not null, \
     DATA_NO         int         not null, \
     OSHIRASE_NO     int         not null, \
     GROUP_CD        varchar(4), \
     REGISTERCD      varchar(10), \
     UPDATED         timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table OSHIRASE_GRP add constraint PK_OSHIRASE_GRP \
primary key (SCHOOLCD, SCHOOL_KIND, DATA_DIV, DATA_NO)

create index \
IDX_GROUP_CD on OSHIRASE_GRP \
(GROUP_CD ASC) \
pctfree 10 allow reverse scans \
page split symmetric collect sampled detailed statistics

insert into OSHIRASE_GRP \
    SELECT \
        SCHOOLCD, \
        SCHOOL_KIND, \
        '01' AS DATA_DIV, \
        DATA_NO, \
        OSHIRASE_NO, \
        GROUP_CD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        OSHIRASE_GRP_OLD

