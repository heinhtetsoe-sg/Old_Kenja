drop table ENTEXAM_SPECIAL_SCHOLARSHIP_DAT

create table ENTEXAM_SPECIAL_SCHOLARSHIP_DAT \
( \
    ENTEXAMYEAR      varchar(4)  not null, \
    APPLICANTDIV     varchar(1)  not null, \
    TESTDIV          varchar(1)  not null, \
    SP_SCHOLAR_CD    varchar(3)  not null, \
    COURSECD         varchar(1)  not null, \
    MAJORCD          varchar(3)  not null, \
    EXAMCOURSECD     varchar(4)  not null, \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SPECIAL_SCHOLARSHIP_DAT add constraint \
PK_ENTEXAM_SP_SCHOLAR_D primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SP_SCHOLAR_CD, COURSECD, MAJORCD, EXAMCOURSECD)
