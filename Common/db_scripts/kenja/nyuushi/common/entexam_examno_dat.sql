-- $Id: ce2aeb45ffe4f048f5033fab80f3d400585145e9 $
drop table ENTEXAM_EXAMNO_DAT

create table ENTEXAM_EXAMNO_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    TESTDIV             varchar(2)  not null, \
    EXAM_TYPE           varchar(2)  not null, \
    EXAMNO_FROM         varchar(10) , \
    EXAMNO_TO           varchar(10) , \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_EXAMNO_DAT add constraint \
PK_ENTEXAM_EXAMNO_DAT primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE)
