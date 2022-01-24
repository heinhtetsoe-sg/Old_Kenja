-- $Id: 3da67e93cdd1e2ef07218197a6ca5560506e3db8 $

drop table RECRUIT_PS_SEND_YMST

create table RECRUIT_PS_SEND_YMST( \
    YEAR                varchar(4) not null, \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CD            varchar(3) not null, \
    SEND_CD             varchar(3) not null, \
    SEND_NAME           varchar(120) not null, \
    SEND_PRGID          varchar(10), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_SEND_YMST add constraint PK_RECRU_PS_SEN_Y primary key (YEAR, EVENT_CLASS_CD, EVENT_CD, SEND_CD)
