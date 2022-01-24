drop table ENTEXAM_MIRAI_COURSE_GRP_MST

create table ENTEXAM_MIRAI_COURSE_GRP_MST \
( \
    ENTEXAMYEAR          varchar(4)   not null, \
    APPLICANTDIV         varchar(1)   not null, \
    TESTDIV              varchar(2)   not null, \
    MIRAI_COURSECD       varchar(2)   not null, \
    MIRAI_COURSE_ABBV    varchar(150) not null, \
    EXAMCOURSECD1        varchar(4), \
    EXAMCOURSECD2        varchar(4), \
    EXAMCOURSECD3        varchar(4), \
    EXAMCOURSECD4        varchar(4), \
    EXAMCOURSECD5        varchar(4), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_COURSE_GRP_MST add constraint \
PK_ENTEXAM_MIRAI_COURSE_GRP_MST primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, MIRAI_COURSECD, MIRAI_COURSE_ABBV)
