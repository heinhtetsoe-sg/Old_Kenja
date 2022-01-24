-- $Id: rep-entexam_applicantbase_detail_dat.sql 63855 2018-12-10 06:31:23Z tawada $

drop table ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD

create table ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD like ENTEXAM_APPLICANTBASE_DETAIL_DAT

insert into ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD select * from ENTEXAM_APPLICANTBASE_DETAIL_DAT

drop table ENTEXAM_APPLICANTBASE_DETAIL_DAT

create table ENTEXAM_APPLICANTBASE_DETAIL_DAT( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
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
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DETAIL_DAT add constraint PK_ENTEXAM_APP_DE primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO, SEQ)

insert into ENTEXAM_APPLICANTBASE_DETAIL_DAT \
 SELECT \
    ENTEXAMYEAR, \
    '1' as APPLICANTDIV, \
    EXAMNO, \
    SEQ, \
    REMARK1, \
    REMARK2, \
    REMARK3, \
    REMARK4, \
    REMARK5, \
    REMARK6, \
    REMARK7, \
    REMARK8, \
    REMARK9, \
    REMARK10, \
    REGISTERCD, \
    UPDATED \
 FROM \
    ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD
