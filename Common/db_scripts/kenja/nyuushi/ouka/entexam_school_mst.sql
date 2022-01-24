-- $Id: c2f79dc04bdbf369ddf0111870cb7b7a9e36fed0 $

drop table ENTEXAM_SCHOOL_MST

create table ENTEXAM_SCHOOL_MST( \
    ENTEXAMYEAR          varchar(4) NOT NULL, \
    ENTEXAM_SCHOOLCD     varchar(4) NOT NULL, \
    FINSCHOOLCD          varchar(12), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SCHOOL_MST add constraint PK_EXAM_SCHOOL_M primary key (ENTEXAMYEAR, ENTEXAM_SCHOOLCD)
