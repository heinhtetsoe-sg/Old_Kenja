-- $Id: 6952e9f8ee23a62abbdbb2c394cf6033048cb4f7 $

drop table RECRUIT_CLASS_MST

create table RECRUIT_CLASS_MST( \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CLASS_NAME    varchar(120) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_CLASS_MST add constraint PK_RECRUIT_CLASS primary key (EVENT_CLASS_CD)
