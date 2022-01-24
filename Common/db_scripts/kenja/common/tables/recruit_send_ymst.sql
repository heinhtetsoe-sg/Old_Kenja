-- $Id: cdaaa55f2a4a82ae27e91ec233f589e63347e033 $

drop table RECRUIT_SEND_YMST

create table RECRUIT_SEND_YMST( \
    YEAR                varchar(4) not null, \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CD            varchar(3) not null, \
    SEND_CD             varchar(3) not null, \
    SEND_NAME           varchar(120) not null, \
    SEND_PRGID          varchar(10), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_SEND_YMST add constraint PK_RECRUIT_SEN_Y primary key (YEAR, EVENT_CLASS_CD, EVENT_CD, SEND_CD)
