-- kanji=´Á»ú
-- $Id: rep-expense_s_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table EXPENSE_S_MST_OLD
create table EXPENSE_S_MST_OLD like EXPENSE_S_MST
insert into EXPENSE_S_MST_OLD select * from EXPENSE_S_MST

drop table EXPENSE_S_MST

create TABLE EXPENSE_S_MST \
( \
        "YEAR"            varchar(4) not null, \
        "EXPENSE_S_CD"    varchar(2) not null, \
        "EXPENSE_S_NAME"  varchar(60), \
        "EXPENSE_S_MONEY" integer, \
        "SEX"             varchar(1), \
        "REGISTERCD"      varchar(8), \
        "UPDATED"         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EXPENSE_S_MST add constraint PK_EXPENSE_S_MST primary key (YEAR,EXPENSE_S_CD)

insert into EXPENSE_S_MST \
select \
    YEAR, \
    EXPENSE_S_CD, \
    EXPENSE_S_NAME, \
    EXPENSE_S_MONEY, \
    SEX, \
    REGISTERCD, \
    UPDATED \
from \
    EXPENSE_S_MST_OLD
