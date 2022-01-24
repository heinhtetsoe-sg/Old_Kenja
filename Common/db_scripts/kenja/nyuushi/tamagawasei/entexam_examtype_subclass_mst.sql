-- $Id: e6bece6c437308e073ee68fe9b1d79e7f22bd959 $

drop table ENTEXAM_EXAMTYPE_SUBCLASS_MST

create table ENTEXAM_EXAMTYPE_SUBCLASS_MST( \
    ENTEXAMYEAR         varchar(4)   not null, \
    APPLICANTDIV        varchar(1)   not null, \
    EXAM_TYPE           varchar(2)   not null, \
    SUBCLASSCD          varchar(2)   not null, \
    SUBCLASS_SELECT     varchar(1), \
    JUDGE_SUMMARY       varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_EXAMTYPE_SUBCLASS_MST \
add constraint PK_ENT_EXAMT_SU_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, EXAM_TYPE, SUBCLASSCD)
