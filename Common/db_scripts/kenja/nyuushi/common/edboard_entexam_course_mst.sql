-- $Id: f4ebd9bb607a9808f5bdb8284fc62ee8fc3054a4 $

drop table EDBOARD_ENTEXAM_COURSE_MST
create table EDBOARD_ENTEXAM_COURSE_MST \
( \
    EDBOARD_SCHOOLCD    varchar(12)     not null, \
    ENTEXAMYEAR         varchar(4)      not null, \
    APPLICANTDIV        varchar(1)      not null, \
    TESTDIV             varchar(2)      not null, \
    COURSECD            varchar(1)      not null, \
    MAJORCD             varchar(3)      not null, \
    EXAMCOURSECD        varchar(4)      not null, \
    EXAMCOURSE_NAME     varchar(45), \
    EXAMCOURSE_ABBV     varchar(30), \
    EXAMCOURSE_MARK     varchar(6), \
    CAPACITY            smallint, \
    CAPACITY2           smallint, \
    ENTER_COURSECD      varchar(1), \
    ENTER_MAJORCD       varchar(3), \
    ENTER_COURSECODE    varchar(4), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EDBOARD_ENTEXAM_COURSE_MST add constraint \
PK_ED_EEXAM_COURSE primary key (EDBOARD_SCHOOLCD, ENTEXAMYEAR, APPLICANTDIV, TESTDIV, COURSECD, MAJORCD, EXAMCOURSECD)
