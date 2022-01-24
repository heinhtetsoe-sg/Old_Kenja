-- $Id: 9eabc30e69574c811b5bcfaff9ea4007c2b558e3 $

drop table SCHREG_BASE_REMARK_DAT

create table SCHREG_BASE_REMARK_DAT( \
    YEAR                varchar(4)    not null, \
    SCHREGNO            varchar(8)    not null, \
    CODE                varchar(2)    not null, \
    SEQ                 varchar(2)    not null, \
    REMARK              VARCHAR(410)  , \
    ANSWER_PATTERN      VARCHAR(1)    , \
    REGISTERCD          varchar(10)   , \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_BASE_REMARK_DAT add constraint PK_SCH_BASE_REMARK primary key (YEAR, SCHREGNO, CODE, SEQ)