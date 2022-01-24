-- $Id: 382a28e6f0e5f556acc75eec617463eb11a1c39a $

drop table RECRUIT_PS_SEND_HIST_PS_DAT

create table RECRUIT_PS_SEND_HIST_PS_DAT( \
    YEAR                varchar(4)  not null, \
    EVENT_CLASS_CD      varchar(3)  not null, \
    EVENT_CD            varchar(3)  not null, \
    SEND_CD             varchar(3)  not null, \
    SEND_COUNT          varchar(2)  not null, \
    RECRUIT_NO          varchar(14) not null, \
    PRISCHOOLCD         varchar(7)  not null, \
    PRISCHOOL_CLASS_CD  varchar(7)  not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_SEND_HIST_PS_DAT add constraint PK_RECRU_PS_SN_PS primary key (YEAR, EVENT_CLASS_CD, EVENT_CD, SEND_CD, SEND_COUNT, RECRUIT_NO)
