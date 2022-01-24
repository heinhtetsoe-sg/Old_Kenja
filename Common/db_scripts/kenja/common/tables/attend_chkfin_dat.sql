-- $Id: 136535c9e5d43d7fe1624d1f4726386f6577538a $

drop table ATTEND_CHKFIN_DAT
create table ATTEND_CHKFIN_DAT ( \
    YEAR            VARCHAR (4) not null, \
    MONTH           VARCHAR (2) not null, \
    SEMESTER        VARCHAR (1) not null, \
    ATTEND_DIV      VARCHAR (1) not null, \
    GRADE           VARCHAR (2) not null, \
    HR_CLASS        VARCHAR (3) not null, \
    CHAIRCD         VARCHAR (7) not null, \
    EXECUTED        VARCHAR (1), \
    REGISTERCD      VARCHAR (10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_CHKFIN_DAT add constraint PK_ATTEND_CF_DAT primary key (YEAR, MONTH, SEMESTER, ATTEND_DIV, GRADE, HR_CLASS, CHAIRCD)
