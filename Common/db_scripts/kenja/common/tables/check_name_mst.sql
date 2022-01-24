-- $Id: 2e152355d760e66fdaff34965fd1a9db982b8c96 $

drop table CHECK_NAME_MST

create table CHECK_NAME_MST( \
    CHECK_CD            varchar(3)    not null, \
    CHECK_NAME          varchar(90)   not null, \
    REGISTERCD          varchar(10)           , \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CHECK_NAME_MST add constraint PK_CHK_NAME_MST primary key (CHECK_CD, CHECK_NAME)