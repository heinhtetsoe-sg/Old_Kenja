-- $Id: 8fcb23a7c0e5c7cce947af124e53d49946e68fd0 $
drop table ENTEXAM_APPLICANTBASE_DETAIL_DAT

create table ENTEXAM_APPLICANTBASE_DETAIL_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    EXAMNO              varchar(10) not null, \
    SEQ                 varchar(3)  not null, \
    REMARK1             varchar(150), \
    REMARK2             varchar(150), \
    REMARK3             varchar(150), \
    REMARK4             varchar(150), \
    REMARK5             varchar(150), \
    REMARK6             varchar(150), \
    REMARK7             varchar(150), \
    REMARK8             varchar(240), \
    REMARK9             varchar(240), \
    REMARK10            varchar(1500), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DETAIL_DAT add constraint \
PK_ENTEXAM_APP_DE primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO, SEQ)

