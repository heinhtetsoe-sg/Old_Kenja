-- $Id: f5dafb9b214fc395e587a814eaf3cbe8ed53ce41 $

drop table CERTIF_ISSUE_PRINT_ORDER_DAT

create table CERTIF_ISSUE_PRINT_ORDER_DAT \
    (YEAR           varchar(4) not null, \
     PRINT_NO       smallint not null, \
     CERTIF_INDEX   varchar(5) not null, \
     PRINT_STAFFCD  varchar(10), \
     PRINT_SYSDATE  timestamp, \
     REMARK1        varchar(120), \
     REMARK2        varchar(120), \
     REMARK3        varchar(120), \
     REMARK4        varchar(120), \
     REMARK5        varchar(120), \
     REMARK6        varchar(120), \
     REMARK7        varchar(120), \
     REMARK8        varchar(120), \
     REMARK9        varchar(120), \
     REMARK10       varchar(120), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CERTIF_ISSUE_PRINT_ORDER_DAT add constraint PK_CERTISSUE_POD primary key \
    (YEAR, PRINT_NO)
