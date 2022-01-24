-- $Id: e88d2931dc83cb4fb59027abce989c82344c3854 $
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
    REMARK8             varchar(150), \
    REMARK9             varchar(150), \
    REMARK10            varchar(150), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DETAIL_DAT add constraint \
PK_ENTEXAM_APP_DE primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO, SEQ)

