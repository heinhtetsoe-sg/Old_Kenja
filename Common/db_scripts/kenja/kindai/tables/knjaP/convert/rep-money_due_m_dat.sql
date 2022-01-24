-- kanji=´Á»ú
-- $Id: rep-money_due_m_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop   table MONEY_DUE_M_DAT_OLD
create table MONEY_DUE_M_DAT_OLD like MONEY_DUE_M_DAT
insert into  MONEY_DUE_M_DAT_OLD select * from MONEY_DUE_M_DAT

drop table   MONEY_DUE_M_DAT
create table MONEY_DUE_M_DAT \
( \
        "YEAR"                  varchar(4) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "EXPENSE_M_CD"          varchar(2) not null, \
        "MONEY_DUE"             integer, \
        "REDUCTION_REASON"      varchar(2), \
        "BANK_TRANS_RESULTCD"   varchar(1), \
        "INST_CD"               varchar(2), \
        "BANK_TRANS_STOP_RESON" varchar(2), \
        "UN_AUTO_PAYFLG"        varchar(1), \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MONEY_DUE_M_DAT \
add constraint PK_MONEY_DUE_M_DAT \
primary key \
(YEAR,SCHREGNO,EXPENSE_M_CD)

insert into MONEY_DUE_M_DAT \
select \
    YEAR, \
    SCHREGNO, \
    EXPENSE_M_CD, \
    MONEY_DUE, \
    REDUCTION_REASON, \
    BANK_TRANS_RESULTCD, \
    INST_CD, \
    BANK_TRANS_STOP_RESON, \
    cast(null as varchar(1)) as UN_AUTO_PAYFLG, \
    REGISTERCD, \
    UPDATED \
from \
    MONEY_DUE_M_DAT_OLD

