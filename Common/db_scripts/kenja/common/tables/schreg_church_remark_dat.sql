-- $Id: fa9cf07f97120c5ae7beff371ed577987eeaf879 $

drop table SCHREG_CHURCH_REMARK_DAT

create table SCHREG_CHURCH_REMARK_DAT( \
    SCHREGNO            varchar(8)   not null, \
    CHURCH_NAME         varchar(150), \
    BAPTISM_DAY         date, \
    HOUSHI_TOU          varchar(400), \
    REMARK              varchar(400), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_CHURCH_REMARK_DAT add constraint PK_SCH_CRCH_REMARK primary key (SCHREGNO)