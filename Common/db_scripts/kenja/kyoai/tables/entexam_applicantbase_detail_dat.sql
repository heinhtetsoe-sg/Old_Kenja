-- $Id: entexam_applicantbase_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table ENTEXAM_APPLICANTBASE_DETAIL_DAT

create table ENTEXAM_APPLICANTBASE_DETAIL_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    EXAMNO              varchar(5)  not null, \
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
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DETAIL_DAT add constraint \
PK_ENTEXAM_APP_DE primary key (ENTEXAMYEAR, EXAMNO, SEQ)

