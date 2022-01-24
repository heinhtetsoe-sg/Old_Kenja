-- $Id: 46588c9af175d756b128f1f350eaa4b15bfb680c $

drop table RECRUIT_VISIT_ACTIVE_DAT

create table RECRUIT_VISIT_ACTIVE_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    SEQ_DIV             varchar(1) not null, \
    SEQ                 varchar(3) not null, \
    REMARK1             varchar(500), \
    REMARK2             varchar(500), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_VISIT_ACTIVE_DAT add constraint PK_RECRUIT_VIS_ACT primary key (YEAR, RECRUIT_NO, SEQ_DIV, SEQ)
