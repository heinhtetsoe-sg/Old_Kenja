-- $Id: 788f837dc8bbb84fa2b3445ffe8150788566e06c $

drop table DOMITORY_YDAT
create table DOMITORY_YDAT( \
    YEAR            varchar(4)    not null, \
    DOMI_CD         varchar(3)    not null, \
    REGISTERCD      varchar(10),  \
    UPDATED    timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table DOMITORY_YDAT add constraint PK_DOMITORY_YDAT primary key (YEAR, DOMI_CD)