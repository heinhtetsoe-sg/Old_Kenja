-- $Id: 10f38c72a61904a40e2aaa1d8a8da400fedcd355 $

drop table RECRUIT_PS_SEND_HIST_DAT

create table RECRUIT_PS_SEND_HIST_DAT( \
    YEAR                varchar(4) not null, \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CD            varchar(3) not null, \
    SEND_CD             varchar(3) not null, \
    SEND_COUNT          varchar(2) not null, \
    SEND_DATE           date       not null, \
    SEND_METHOD         varchar(2),  \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_SEND_HIST_DAT add constraint PK_RECRU_PS_SEN_D primary key (YEAR, EVENT_CLASS_CD, EVENT_CD, SEND_CD, SEND_COUNT)
