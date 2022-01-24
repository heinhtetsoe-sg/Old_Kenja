-- kanji=漢字
-- $Id: b65fb6c26e73ea8a3deaa22b3cf694e7c3af5c87 $

drop table ENTEXAM_EXAMTYPE_SUBCLASS_MST

create table ENTEXAM_EXAMTYPE_SUBCLASS_MST( \
    ENTEXAMYEAR         varchar(4)   not null, \
    APPLICANTDIV        varchar(1)   not null, \
    EXAM_TYPE           varchar(2)   not null, \
    TESTSUBCLASSCD      varchar(1)   not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_EXAMTYPE_SUBCLASS_MST \
add constraint PK_ENT_EXAMT_SU_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, EXAM_TYPE, TESTSUBCLASSCD)
