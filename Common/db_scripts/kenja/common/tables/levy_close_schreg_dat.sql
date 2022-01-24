-- kanji=´Á»ú
-- $Id: 82794a59c418af1574137a04d7a7afb0147aee0a $

drop table LEVY_CLOSE_SCHREG_DAT

create table LEVY_CLOSE_SCHREG_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "CLOSE_L_CD"            varchar(2)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "INCOME_TOTAL"          integer, \
        "OUTGO_TOTAL"           integer, \
        "ZANKIN_TOTAL"          integer, \
        "CLOSE_FLG"             varchar(1), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CLOSE_SCHREG_DAT add constraint PK_LEVY_CL_SCH_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, CLOSE_L_CD, SCHREGNO)
