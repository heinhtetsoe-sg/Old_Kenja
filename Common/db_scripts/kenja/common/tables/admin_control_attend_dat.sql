-- $Id: 7fb6440dde2ca2f90d0c62b88885ebd1b3dbdd91 $

drop table ADMIN_CONTROL_ATTEND_DAT
create table ADMIN_CONTROL_ATTEND_DAT ( \
    "YEAR"          VARCHAR(4)      NOT NULL, \
    "SCHOOL_KIND"   VARCHAR(2)      NOT NULL, \
    "CONTROL_DIV"   VARCHAR(1)      NOT NULL, \
    "ATTEND_DIV"    VARCHAR(1)      NOT NULL, \
    "PROGRAMID"     VARCHAR(10)     NOT NULL, \
    "GROUPCD"       VARCHAR(4)      NOT NULL, \
    "GRADE"         VARCHAR(2)      NOT NULL, \
    "COURSECD"      VARCHAR(1)      NOT NULL, \
    "MAJORCD"       VARCHAR(3)      NOT NULL, \
    "ATTEND_ITEM"   VARCHAR(20)     NOT NULL, \
    "SHOWORDER"     VARCHAR(2), \
    "INPUT_FLG"     VARCHAR(1), \
    "REGISTERCD"    VARCHAR(10), \
    "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_ATTEND_DAT add constraint PK_ADMIN_CTRL_ATT \
primary key (YEAR, SCHOOL_KIND, CONTROL_DIV, ATTEND_DIV, PROGRAMID, GROUPCD, GRADE, COURSECD, MAJORCD, ATTEND_ITEM)
