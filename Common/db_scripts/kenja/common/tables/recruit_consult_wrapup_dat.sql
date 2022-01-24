-- $Id: 2d25f9e47b9aaea552adfb3e35643fea7d084d8f $

drop table RECRUIT_CONSULT_WRAPUP_DAT

create table RECRUIT_CONSULT_WRAPUP_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    REMARK              varchar(60), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_CONSULT_WRAPUP_DAT add constraint PK_RECRUIT_CON_WRA primary key (YEAR, RECRUIT_NO)
