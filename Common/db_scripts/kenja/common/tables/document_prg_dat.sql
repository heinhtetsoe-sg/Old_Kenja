-- $Id: c6cec485aa6f3af14811a3ecea9d89a98823f305 $

drop table DOCUMENT_PRG_DAT
create table DOCUMENT_PRG_DAT( \
    SCHOOLCD        varchar(12) not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    PROGRAMID       varchar(20) not null, \
    SEQ             varchar(2)  not null, \
    DOCUMENT1       varchar(1000), \
    DOCUMENT2       varchar(1000), \
    DOCUMENT3       varchar(1000), \
    DOCUMENT4       varchar(1000), \
    DOCUMENT5       varchar(1000), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table DOCUMENT_PRG_DAT add constraint PK_JVIEW_POINT_M primary key (SCHOOLCD, SCHOOL_KIND, PROGRAMID, SEQ)
