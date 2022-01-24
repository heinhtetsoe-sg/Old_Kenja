-- $Id: $

drop table RECRUIT_EVENT_YMST

create table RECRUIT_EVENT_YMST( \
    YEAR                varchar(4) not null, \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CD            varchar(3) not null, \
    EVENT_NAME          varchar(120) not null, \
    EVENT_ABBV          varchar(60), \
    EVENT_DATE          DATE, \
    EVENT_VENUE         varchar(100), \
    REMARK              varchar(250), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_EVENT_YMST add constraint PK_RECRUIT_E_Y primary key (YEAR, EVENT_CLASS_CD, EVENT_CD)
