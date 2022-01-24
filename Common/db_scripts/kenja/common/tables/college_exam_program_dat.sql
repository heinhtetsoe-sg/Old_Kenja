-- $Id: 346af26d1555c44803db8da35cc5d4ec3cf32439 $

drop table COLLEGE_EXAM_PROGRAM_DAT
create table COLLEGE_EXAM_PROGRAM_DAT( \
    YEAR            varchar(4) not null, \
    ADVERTISE_DIV   varchar(2) not null, \
    PROGRAM_CD      varchar(2) not null, \
    PROGRAM_NAME    varchar(75), \
    PROGRAM_ABBV    varchar(75), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_EXAM_PROGRAM_DAT add constraint PK_COLLEGE_EXAMP primary key (YEAR, ADVERTISE_DIV, PROGRAM_CD)
