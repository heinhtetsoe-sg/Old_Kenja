-- $Id: ac83b34bb3565ac7f15ac206c258b133f848c002 $

drop table RECRUIT_SEND_HIST_STD_DAT

create table RECRUIT_SEND_HIST_STD_DAT( \
    YEAR                varchar(4) not null, \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CD            varchar(3) not null, \
    SEND_CD             varchar(3) not null, \
    SEND_COUNT          varchar(2) not null, \
    RECRUIT_NO          varchar(8) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_SEND_HIST_STD_DAT add constraint PK_RECR_SEN_STD primary key (YEAR, EVENT_CLASS_CD, EVENT_CD, SEND_CD, SEND_COUNT, RECRUIT_NO)
