-- $Id: d30e673d3b19747ecf85bbb0d3b575ee95aeb9db $

drop table RECRUIT_PS_EVENT_YMST

create table RECRUIT_PS_EVENT_YMST( \
    YEAR                varchar(4)   not null, \
    EVENT_CLASS_CD      varchar(3)   not null, \
    EVENT_CD            varchar(3)   not null, \
    EVENT_NAME          varchar(120) not null, \
    EVENT_ABBV          varchar(60) , \
    REMARK              varchar(250), \
    REGISTERCD          varchar(10) , \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_EVENT_YMST add constraint PK_RECRUIT_PS_E_Y primary key (YEAR, EVENT_CLASS_CD, EVENT_CD)
