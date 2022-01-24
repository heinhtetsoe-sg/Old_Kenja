-- kanji=漢字
-- $Id: be114ac6bd94d24e806bf35fb159dec005c3859d $

drop table ENTEXAM_EXAMTYPE_MST

create table ENTEXAM_EXAMTYPE_MST( \
    ENTEXAMYEAR         varchar(4)   not null, \
    APPLICANTDIV        varchar(1)   not null, \
    EXAM_TYPE           varchar(2)   not null, \
    EXAMTYPE_NAME       varchar(60)  not null, \
    EXAMTYPE_NAME_ABBV  varchar(15), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_EXAMTYPE_MST \
add constraint PK_ENT_EXAMTYPE_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, EXAM_TYPE)
