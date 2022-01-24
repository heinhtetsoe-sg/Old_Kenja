-- $Id: cbb31fd3a07f4936be3b2e1543aa20c1afebac4a $

drop table FAMILY_DAT

create table FAMILY_DAT( \
    FAMILY_NO       varchar(9)  not null, \
    RELANO          varchar(2)  not null, \
    RELANAME        varchar(60), \
    RELAKANA        varchar(120), \
    RELASEX         varchar(1), \
    RELABIRTHDAY    date, \
    OCCUPATION      varchar(60), \
    REGIDENTIALCD   varchar(2), \
    RELATIONSHIP    varchar(2), \
    RELA_SCHREGNO   varchar(8), \
    REGD_GRD_FLG    varchar(2), \
    RELA_GRADE      varchar(2), \
    TYOUSHI_FLG     varchar(1), \
    REMARK          varchar(45), \
    GRD_DIV         varchar(1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table FAMILY_DAT \
add constraint PK_FAMILY_DAT \
primary key (FAMILY_NO, RELANO)
