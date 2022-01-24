-- $Id: c77232a6c782c109a305858db89d93101621ea0f $

drop table RECRUIT_PS_EVENT_DAT

create table RECRUIT_PS_EVENT_DAT( \
    YEAR                varchar(4)  not null, \
    RECRUIT_NO          varchar(14) not null, \
    TOUROKU_DATE        date        not null, \
    EVENT_CLASS_CD      varchar(3)  not null, \
    EVENT_CD            varchar(3)  not null, \
    MEDIA_CD            varchar(2)  not null, \
    PRISCHOOLCD         varchar(7)  not null, \
    PRISCHOOL_CLASS_CD  varchar(7)  not null, \
    STATE_CD            varchar(2), \
    ATTEND_MEETING_FLG  varchar(1), \
    DOC_REQ_NUMBER      varchar(3), \
    REMARK              varchar(250), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_EVENT_DAT add constraint PK_RECRUIT_PS_E_D primary key (YEAR, RECRUIT_NO, TOUROKU_DATE, EVENT_CLASS_CD, EVENT_CD, MEDIA_CD)
