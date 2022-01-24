-- $Id: b8e597d9d8ee2b8cc85422540c980ff8a57abb89 $

drop table PRISCHOOL_CLASS_DETAIL_YDAT

create table PRISCHOOL_CLASS_DETAIL_YDAT \
    (YEAR               varchar(4) not null, \
     PRISCHOOLCD        varchar(7) not null, \
     PRISCHOOL_CLASS_CD varchar(7) not null, \
     SEQ                varchar(3) not null, \
     REMARK1            varchar(600), \
     REMARK2            varchar(600), \
     REMARK3            varchar(600), \
     REMARK4            varchar(600), \
     REMARK5            varchar(600), \
     REMARK6            varchar(600), \
     REMARK7            varchar(600), \
     REMARK8            varchar(600), \
     REMARK9            varchar(600), \
     REMARK10           varchar(600), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PRISCHOOL_CLASS_DETAIL_YDAT add constraint PK_PRI_DETAIL_Y primary key (YEAR, PRISCHOOLCD, PRISCHOOL_CLASS_CD, SEQ)
