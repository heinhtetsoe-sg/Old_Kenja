-- kanji=´Á»ú
-- $Id: rep-money_due_s_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table MONEY_DUE_S_DAT_OLD
create table MONEY_DUE_S_DAT_OLD like MONEY_DUE_S_DAT
insert into MONEY_DUE_S_DAT_OLD select * from MONEY_DUE_S_DAT

drop table MONEY_DUE_S_DAT

create table MONEY_DUE_S_DAT \
( \
        "YEAR"         varchar(4) not null, \
        "SCHREGNO"     varchar(8) not null, \
        "EXPENSE_M_CD" varchar(2) not null, \
        "EXPENSE_S_CD" varchar(2) not null, \
        "MONEY_DUE"    integer, \
        "REGISTERCD"   varchar(8), \
        "UPDATED"      timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MONEY_DUE_S_DAT add constraint PK_MONEY_DUE_S_DAT primary key (YEAR,SCHREGNO,EXPENSE_M_CD,EXPENSE_S_CD)

insert into MONEY_DUE_S_DAT \
select \
    t1.YEAR, \
    t1.SCHREGNO, \
    t2.EXPENSE_M_CD, \
    t1.EXPENSE_S_CD, \
    t1.MONEY_DUE, \
    t1.REGISTERCD, \
    t1.UPDATED \
from \
    MONEY_DUE_S_DAT_OLD t1 \
    left join EXPENSE_S_MST t2 on t1.YEAR = t2.YEAR AND t1.EXPENSE_S_CD = t2.EXPENSE_S_CD
