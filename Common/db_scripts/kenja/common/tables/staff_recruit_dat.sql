-- $Id: c3c03453d403e733814146a7c5bfb9bb9a8559a5 $

drop table STAFF_RECRUIT_DAT

create table STAFF_RECRUIT_DAT( \
    YEAR                varchar(4)  not null, \
    RECRUIT_DIV         varchar(1)  not null, \
    STAFFCD             varchar(10) not null, \
    FINSCHOOLCD         varchar(12) not null, \
    PRISCHOOLCD         varchar(7)  not null, \
    PRISCHOOL_CLASS_CD  varchar(7)  not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STAFF_RECRUIT_DAT add constraint PK_STAF_RECRUIT_D primary key (YEAR, RECRUIT_DIV, STAFFCD, FINSCHOOLCD, PRISCHOOLCD, PRISCHOOL_CLASS_CD)
