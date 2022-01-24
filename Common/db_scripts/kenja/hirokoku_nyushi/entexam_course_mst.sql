drop table ENTEXAM_COURSE_MST

create table ENTEXAM_COURSE_MST \
( \
    ENTEXAMYEAR      varchar(4)  not null, \
    APPLICANTDIV     varchar(1)  not null, \
    TESTDIV          varchar(1)  not null, \
    COURSECD         varchar(1)  not null, \
    MAJORCD          varchar(3)  not null, \
    EXAMCOURSECD     varchar(4)  not null, \
    EXAMCOURSE_NAME  varchar(45), \
    EXAMCOURSE_ABBV  varchar(30), \
    EXAMCOURSE_MARK  varchar(6), \
    CAPACITY         smallint, \
    ENTER_COURSECD   varchar(1), \
    ENTER_MAJORCD    varchar(3), \
    ENTER_COURSECODE varchar(4), \
    REGISTERCD       varchar(8), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_COURSE_MST add constraint \
PK_ENTEXAM_COURSE primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, COURSECD, MAJORCD, EXAMCOURSECD)
