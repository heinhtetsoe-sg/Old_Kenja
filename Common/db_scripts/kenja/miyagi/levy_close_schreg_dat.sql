-- kanji=´Á»ú
-- $Id: levy_close_schreg_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table LEVY_CLOSE_SCHREG_DAT

create table LEVY_CLOSE_SCHREG_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "CLOSE_L_CD"            varchar(2)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "INCOME_TOTAL"          INTEGER, \
        "OUTGO_TOTAL"           INTEGER, \
        "ZANKIN_TOTAL"          INTEGER, \
        "CLOSE_FLG"             varchar(1), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CLOSE_SCHREG_DAT add constraint PK_LEVY_CL_SCH_D primary key (YEAR, CLOSE_L_CD, SCHREGNO)
