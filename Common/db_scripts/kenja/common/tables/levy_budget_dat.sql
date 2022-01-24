-- $Id: d203d089ad3dceba796abb1ced9783edb4bf60bd $

drop table LEVY_BUDGET_DAT

create table LEVY_BUDGET_DAT( \
    SCHOOLCD        varchar(12)   not null  ,  \
    SCHOOL_KIND     varchar(2)    not null  ,  \
    YEAR            varchar(4)    not null  ,  \
    BUDGET_L_CD     varchar(2)    not null  ,  \
    BUDGET_M_CD     varchar(2)    not null  ,  \
    BUDGET_S_CD     varchar(3)    not null  ,  \
    BUDGET_MONEY    integer                 ,  \
    REGISTERCD      varchar(10)             ,  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_BUDGET_DAT add constraint PK_LEVY_BUDGET_DAT primary key (SCHOOLCD, SCHOOL_KIND, YEAR, BUDGET_L_CD, BUDGET_M_CD, BUDGET_S_CD)
