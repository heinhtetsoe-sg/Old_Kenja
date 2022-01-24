-- $Id: 76e9ffb4a1845256215704446a05a2d7316e8ff8 $

drop table RECRUIT_PS_DAT

create table RECRUIT_PS_DAT( \
    YEAR                varchar(4)   not null, \
    RECRUIT_NO          varchar(14)  not null, \
    NAME                varchar(120) not null, \
    NAME_KANA           varchar(240) , \
    PRISCHOOLCD         varchar(7)   not null, \
    PRISCHOOL_CLASS_CD  varchar(7)   not null, \
    PRISCHOOL_TEACHER   varchar(120), \
    ZIPCD               varchar(8),   \
    ADDR1               varchar(150), \
    ADDR2               varchar(150), \
    TELNO               varchar(14),  \
    TELNO2              varchar(14),  \
    FAXNO               varchar(14),  \
    EMAIL               varchar(120), \
    REMARK              varchar(250), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_PS_DAT add constraint PK_RECRUIT_PS_DAT primary key (YEAR, RECRUIT_NO)
