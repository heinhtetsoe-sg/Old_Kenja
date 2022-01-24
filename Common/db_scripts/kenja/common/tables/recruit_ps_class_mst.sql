-- $Id: 6ea1b9f3e0a447e1d1bf0988326be488db8347eb $

drop table RECRUIT_PS_CLASS_MST

create table RECRUIT_PS_CLASS_MST( \
    EVENT_CLASS_CD      varchar(3)   not null, \
    EVENT_CLASS_NAME    varchar(120) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_CLASS_MST add constraint PK_RECRU_PS_CLASS primary key (EVENT_CLASS_CD)
