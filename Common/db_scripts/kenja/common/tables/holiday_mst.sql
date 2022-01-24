-- $Id: 23aebb78371e8887d769326c0a84b5f53f2e21cd $

drop table HOLIDAY_MST

create table HOLIDAY_MST \
    (HOLIDAY             date            not null, \
     REMARK              varchar(75), \
     REGISTERCD          varchar(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HOLIDAY_MST add constraint PK_HOLIDAY_MST primary key (HOLIDAY)
