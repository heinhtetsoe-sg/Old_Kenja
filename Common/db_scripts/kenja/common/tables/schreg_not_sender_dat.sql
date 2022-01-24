-- $Id: 3fa8b5c67769558eccc8d609b12d46c2389cb1e3 $

drop table SCHREG_NOT_SENDER_DAT
create table SCHREG_NOT_SENDER_DAT( \
    YEAR            varchar(4)    not null, \
    SCHREGNO        varchar(8)    not null, \
    REGISTERCD      varchar(10),  \
    UPDATED    timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_NOT_SENDER_DAT add constraint PK_SCH_NOT_SEND_D primary key (YEAR, SCHREGNO)