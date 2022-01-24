-- kanji=´Á»ú
-- $Id: expense_ms_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table EXPENSE_MS_MST

create table EXPENSE_MS_MST \
( \
        "YEAR"            varchar(4) not null, \
        "EXPENSE_M_CD"    varchar(2) not null, \
        "EXPENSE_S_CD"    varchar(2) not null, \
        "REGISTERCD"      varchar(8), \
        "UPDATED"         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EXPENSE_MS_MST add constraint PK_EXPENSE_MS_MST primary key (YEAR,EXPENSE_M_CD,EXPENSE_S_CD)
