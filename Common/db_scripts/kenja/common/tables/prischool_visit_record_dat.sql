-- $Id: 0077cb18a9020d2199b3bb63fe8ab4fe6eb92ecb $

drop table PRISCHOOL_VISIT_RECORD_DAT

create table PRISCHOOL_VISIT_RECORD_DAT \
    (PRISCHOOLCD        varchar(7) not null, \
     VISIT_DATE         date not null, \
     SEQ                varchar(3) not null, \
     PRISCHOOL_CLASS_CD varchar(7), \
     STAFFCD            varchar(10), \
     PRISCHOOL_STAFF    varchar(60), \
     COMMENT            varchar(120), \
     EXAM_STD_INFO      varchar(60), \
     REMARK             varchar(60), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table PRISCHOOL_VISIT_RECORD_DAT add constraint PK_PRI_VISIT_DAT primary key (PRISCHOOLCD, VISIT_DATE, SEQ)
