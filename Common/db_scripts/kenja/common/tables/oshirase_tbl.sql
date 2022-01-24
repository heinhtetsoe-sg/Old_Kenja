-- $Id: 0072186d1c6b6907d828d64e68f661ff4ed8e1fe $

drop table OSHIRASE_TBL

create table OSHIRASE_TBL \
    (SCHOOLCD        varchar(12) not null, \
     SCHOOL_KIND     varchar(2)  not null, \
     DATA_DIV        varchar(2)  not null, \
     OSHIRASE_NO     int         not null, \
     ENTRY_DATE      date, \
     ANNOUNCE        varchar(600), \
     STAFFCD         varchar(10), \
     START_DATE      date, \
     END_DATE        date, \
     REGISTERCD      varchar(10), \
     UPDATED         timestamp default current timestamp, \
     ANNOUNCE_ENG    varchar(600) \
    ) in usr1dms index in idx1dms

alter table OSHIRASE_TBL add constraint PK_OSHIRASE_TBL \
primary key (SCHOOLCD, SCHOOL_KIND, DATA_DIV, OSHIRASE_NO)

create index \
IDX_END_DATE on OSHIRASE_TBL \
(END_DATE DESC) \
pctfree 10 allow reverse scans \
page split symmetric collect sampled detailed statistics
