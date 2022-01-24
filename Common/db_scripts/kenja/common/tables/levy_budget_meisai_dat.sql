-- $Id: 9adc6df9e1e93e4c5610f9d139f21fb7bc1ce6a4 $

drop table LEVY_BUDGET_MEISAI_DAT

create table LEVY_BUDGET_MEISAI_DAT( \
    SCHOOLCD        varchar(12)   not null  ,  \
    SCHOOL_KIND     varchar(2)    not null  ,  \
    YEAR            varchar(4)    not null  ,  \
    BUDGET_L_CD     varchar(2)    not null  ,  \
    BUDGET_M_CD     varchar(2)    not null  ,  \
    BUDGET_S_CD     varchar(3)    not null  ,  \
    REQUEST_NO      VARCHAR(10)   not null  ,  \
    LINE_NO         SMALLINT      not null  ,  \
    OUTGO_L_CD      VARCHAR(2)    not null  ,  \
    OUTGO_M_CD      VARCHAR(2)    not null  ,  \
    OUTGO_S_CD      VARCHAR(3)    not null  ,  \
    REGISTERCD      varchar(10)             ,  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_BUDGET_MEISAI_DAT add constraint PK_LEVY_BUDGET_MEISAI_DAT primary key (SCHOOLCD, SCHOOL_KIND, YEAR, BUDGET_L_CD, BUDGET_M_CD, BUDGET_S_CD, REQUEST_NO, LINE_NO, OUTGO_L_CD, OUTGO_M_CD, OUTGO_S_CD)
