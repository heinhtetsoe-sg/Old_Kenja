-- $Id: 97a43464b6686809f9c29895689567fd4777165f $

drop table ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD
create table ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD like ENTEXAM_APPLICANTBASE_DETAIL_DAT
insert into ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD select * from ENTEXAM_APPLICANTBASE_DETAIL_DAT

drop table ENTEXAM_APPLICANTBASE_DETAIL_DAT
create table ENTEXAM_APPLICANTBASE_DETAIL_DAT \
( \
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
select \
    T1.ENTEXAMYEAR, \
    BASE.APPLICANTDIV, \
    T1.EXAMNO, \
    T1.SEQ, \
    T1.REMARK1, \
    T1.REMARK2, \
    T1.REMARK3, \
    T1.REMARK4, \
    T1.REMARK5, \
    T1.REMARK6, \
    T1.REMARK7, \
    T1.REMARK8, \
    T1.REMARK9, \
    T1.REMARK10, \
    T1.REGISTERCD, \
    T1.UPDATED \
from \
    ENTEXAM_APPLICANTBASE_DETAIL_DAT_OLD T1 \
    INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE \
        ON  T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR \
        AND T1.EXAMNO = BASE.EXAMNO

insert into ENTEXAM_APPLICANTBASE_DETAIL_DAT \
select \
    T1.ENTEXAMYEAR, \
    BASE.APPLICANTDIV, \
    T1.EXAMNO, \
    T1.SEQ, \
    T1.REMARK1, \
    T1.REMARK2, \
    T1.REMARK3, \
    T1.REMARK4, \
    T1.REMARK5, \
    T1.REMARK6, \
    T1.REMARK7, \
    T1.REMARK8, \
    T1.REMARK9, \
    T1.REMARK10, \
    T1.REGISTERCD, \
    T1.UPDATED \
from \
    ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T1 \
    INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE \
        ON  T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR \
        AND T1.EXAMNO = BASE.EXAMNO \
where \
    T1.SEQ IN ('010', '011', '012')
