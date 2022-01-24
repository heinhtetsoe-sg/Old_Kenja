-- $Id: 38c285ac8d36dd022d1f29a0907e0b19c01de1b4 $

drop table DOMITORY_MST
create table DOMITORY_MST( \
    DOMI_CD         varchar(3)    not null, \
    DOMI_NAME       varchar(120), \
    DOMI_ZIPCD      varchar(8),   \
    DOMI_ADDR1      varchar(150), \
    DOMI_ADDR2      varchar(150), \
    DOMI_TELNO      varchar(14),  \
    DOMI_TELNO2     varchar(14),  \
    DOMI_FAXNO      varchar(14),  \
    DOMI_LEADER     varchar(120), \
    REGISTERCD      varchar(10),  \
    UPDATED    timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table DOMITORY_MST add constraint PK_DOMITORY_MST primary key (DOMI_CD)