drop table ENTEXAM_WISHDIV_MST

create table ENTEXAM_WISHDIV_MST \
( \
    ENTEXAMYEAR     varchar(4)  not null, \
    APPLICANTDIV    varchar(1)  not null, \
    TESTDIV         varchar(1)  not null, \
    DESIREDIV       varchar(1)  not null, \
    WISHNO          varchar(1)  not null, \
    COURSECD        varchar(1), \
    MAJORCD         varchar(3), \
    EXAMCOURSECD    varchar(4), \
    REGISTERCD      varchar(8),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_WISHDIV_MST add constraint \
PK_ENTEXAM_WISH primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, DESIREDIV, WISHNO)


