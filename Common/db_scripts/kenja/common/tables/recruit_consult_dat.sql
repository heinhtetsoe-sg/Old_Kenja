-- $Id: 4e03adf0a76303809a783eeab0ffb4f7f265dd4b $

drop table RECRUIT_CONSULT_DAT

create table RECRUIT_CONSULT_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    TOUROKU_DATE        date not null, \
    CONSULT_CD          varchar(2), \
    METHOD_CD           varchar(2), \
    STAFFCD             varchar(10), \
    CONTENTS            varchar(1050), \
    REMARK              varchar(60), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_CONSULT_DAT add constraint PK_RECRUIT_CON_DAT primary key (YEAR, RECRUIT_NO, TOUROKU_DATE)
