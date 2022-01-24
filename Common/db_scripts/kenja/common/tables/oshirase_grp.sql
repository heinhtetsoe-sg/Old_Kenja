-- $Id: e5d614da548e00fa254f8b1d69e0ed0e6622ee10 $

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
