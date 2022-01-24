drop table ENTEXAM_PERFECT_MST

create table ENTEXAM_PERFECT_MST \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    TESTDIV             varchar(1)  not null, \
    COURSECD            varchar(1)  not null, \
    MAJORCD             varchar(3)  not null, \
    EXAMCOURSECD        varchar(4)  not null, \
    TESTSUBCLASSCD      varchar(1)  not null, \
    PERFECT             smallint, \
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_PERFECT_MST add constraint \
PK_ENTEXAM_PERF primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, COURSECD, MAJORCD, EXAMCOURSECD, TESTSUBCLASSCD)
