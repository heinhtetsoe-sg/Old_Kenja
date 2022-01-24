-- $Id: b81563073d1d1d9de0376529c80df703f605b51b $

drop table ADMIN_CONTROL_ATTEND_ITEMNAME_DAT
create table ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ( \
    "YEAR"              VARCHAR(4)      NOT NULL, \
    "SCHOOL_KIND"       VARCHAR(2)      NOT NULL, \
    "ATTEND_DIV"        VARCHAR(1)      NOT NULL, \
    "GRADE"             VARCHAR(2)      NOT NULL, \
    "COURSECD"          VARCHAR(1)      NOT NULL, \
    "MAJORCD"           VARCHAR(3)      NOT NULL, \
    "ATTEND_ITEM"       VARCHAR(20)     NOT NULL, \
    "ATTEND_ITEMNAME"   VARCHAR(30), \
    "REGISTERCD"        VARCHAR(10), \
    "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_ATTEND_ITEMNAME_DAT add constraint PK_ADMNCL_ATTNAME \
primary key (YEAR, SCHOOL_KIND, ATTEND_DIV, GRADE, COURSECD, MAJORCD, ATTEND_ITEM)
