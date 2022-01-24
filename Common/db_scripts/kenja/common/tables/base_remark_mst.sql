-- $Id: f2a82905bb6aec1051adcef1a78457fc2605e2f1 $

drop table BASE_REMARK_MST

create table BASE_REMARK_MST( \
    CODE            varchar(2)   not null, \
    NAME            varchar(30), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table BASE_REMARK_MST add constraint PK_BASE_REMARK primary key (CODE)