-- kanji=´Á»ú
-- $Id: rep-money_paid_m_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop   table MONEY_PAID_M_DAT_OLD
create table MONEY_PAID_M_DAT_OLD like MONEY_PAID_M_DAT
insert into  MONEY_PAID_M_DAT_OLD select * from MONEY_PAID_M_DAT

drop table   MONEY_PAID_M_DAT
create table MONEY_PAID_M_DAT \
( \
        "YEAR"              varchar(4)  not null, \
        "SCHREGNO"          varchar(8)  not null, \
        "EXPENSE_L_CD"      varchar(2)  not null, \
        "EXPENSE_M_CD"      varchar(2)  not null, \
        "PAID_INPUT_FLG"    varchar(1)  not null, \
        "PAID_MONEY_DATE"   date, \
        "PAID_MONEY"        integer, \
        "PAID_MONEY_DIV"    varchar(2), \
        "REPAY_DATE"        date, \
        "REPAY_MONEY"       integer, \
        "REPAY_DEV"         varchar(2), \
        "REMARK"            varchar(60), \
        "REGISTERCD"        varchar(8), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MONEY_PAID_M_DAT \
add constraint PK_MONEY_PAID_M_DT \
primary key \
(YEAR,SCHREGNO,EXPENSE_L_CD,EXPENSE_M_CD,PAID_INPUT_FLG)

insert into MONEY_PAID_M_DAT \
select \
    YEAR, \
    SCHREGNO, \
    EXPENSE_L_CD, \
    EXPENSE_M_CD, \
    PAID_INPUT_FLG, \
    PAID_MONEY_DATE, \
    PAID_MONEY, \
    PAID_MONEY_DIV, \
    REPAY_DATE, \
    REPAY_MONEY, \
    REPAY_DEV, \
    cast(null as varchar(60)) as REMARK, \
    REGISTERCD, \
    UPDATED \
from \
    MONEY_PAID_M_DAT_OLD

