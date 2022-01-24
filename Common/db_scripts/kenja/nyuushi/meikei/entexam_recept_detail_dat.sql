-- $Id: 1c137fb4412c161155749b29901f37a7c13b2a69 $

drop table ENTEXAM_RECEPT_DETAIL_DAT
create table ENTEXAM_RECEPT_DETAIL_DAT( \
    ENTEXAMYEAR               varchar(4)    not null, \
    APPLICANTDIV              varchar(1)    not null, \
    TESTDIV                   varchar(1)    not null, \
    EXAM_TYPE                 varchar(2)    not null, \
    RECEPTNO                  varchar(20)   not null, \
    SEQ                       varchar(3)    not null, \
    REMARK1                   varchar(150), \
    REMARK2                   varchar(150), \
    REMARK3                   varchar(150), \
    REMARK4                   varchar(150), \
    REMARK5                   varchar(150), \
    REMARK6                   varchar(150), \
    REMARK7                   varchar(150), \
    REMARK8                   varchar(150), \
    REMARK9                   varchar(150), \
    REMARK10                  varchar(150), \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_RECEPT_DETAIL_DAT add constraint PK_ENTEXAM_RCPTD primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO, SEQ)