-- $Id: 080e535b29e863987147df6d6fafb68e805b47d5 $

drop table NURSEOFF_VISITREC_DETAIL_DAT
create table NURSEOFF_VISITREC_DETAIL_DAT( \
    SCHREGNO            varchar(8)    not null, \
    VISIT_DATE          date          not null, \
    VISIT_HOUR          varchar(2)    not null, \
    VISIT_MINUTE        varchar(2)    not null, \
    TYPE                varchar(1)    not null, \
    SEQ                 varchar(2)    not null, \
    REMARK1             varchar(150), \
    REMARK2             varchar(150), \
    REMARK3             varchar(150), \
    REMARK4             varchar(150), \
    REMARK5             varchar(150), \
    REMARK6             varchar(150), \
    REMARK7             varchar(150), \
    REMARK8             varchar(150), \
    REMARK9             varchar(150), \
    REMARK10            varchar(150), \
    REMARK_L1           varchar(2500), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NURSEOFF_VISITREC_DETAIL_DAT add constraint PK_NRS_VIS_DET primary key (SCHREGNO,VISIT_DATE,VISIT_HOUR,VISIT_MINUTE,TYPE,SEQ)