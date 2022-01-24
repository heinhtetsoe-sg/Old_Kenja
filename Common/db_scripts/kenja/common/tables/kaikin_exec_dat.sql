-- $Id$

drop table KAIKIN_EXEC_DAT

create table KAIKIN_EXEC_DAT( \
    YEAR        varchar(4) not null, \
    GRADE       varchar(2) not null, \
    HR_CLASS    varchar(3) not null, \
    EXEC_TIME   timestamp  not null, \
    BASE_DATE   date               , \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table KAIKIN_EXEC_DAT add constraint PK_KAIKIN_EXEC_DAT primary key (YEAR, GRADE, HR_CLASS, EXEC_TIME)
