-- $Id: 63e4c2f04e96080bc59fe2b0ff419f0a1ac83721 $

drop table REDUCTION_SCHOOL_STD_DAT
create table REDUCTION_SCHOOL_STD_DAT( \
    SCHOOLCD            varchar(12)     not null, \
    SCHOOL_KIND         varchar(2)      not null, \
    REDUCTION_DIV_CD    varchar(2)      not null, \
    SCHREGNO            varchar(8)      not null, \
    S_YEAR_MONTH        varchar(7)      not null, \
    E_YEAR_MONTH        varchar(7), \
    REMARK              varchar(150), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_STD_DAT add constraint PK_REDUC_SCH_STD primary key (SCHOOLCD,SCHOOL_KIND,REDUCTION_DIV_CD,SCHREGNO,S_YEAR_MONTH)