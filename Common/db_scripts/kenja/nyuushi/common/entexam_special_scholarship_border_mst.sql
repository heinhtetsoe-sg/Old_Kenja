drop table ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST

create table ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST \
( \
    ENTEXAMYEAR      varchar(4)  not null, \
    APPLICANTDIV     varchar(1)  not null, \
    COURSECD         varchar(1)  not null, \
    MAJORCD          varchar(3)  not null, \
    EXAMCOURSECD     varchar(4)  not null, \
    SHDIV            varchar(1)  not null, \
    BORDER1          varchar(3)  , \
    BORDER2          varchar(3)  , \
    BORDER3          varchar(3)  , \
    BORDER4          varchar(3)  , \
    BORDER5          varchar(3)  , \
    BORDER6          varchar(3)  , \
    BORDER7          varchar(3)  , \
    BORDER8          varchar(3)  , \
    BORDER9          varchar(3)  , \
    BORDER10         varchar(3)  , \
    BORDER11         varchar(3)  , \
    BORDER12         varchar(3)  , \
    REGISTERCD       varchar(10) , \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST add constraint \
PK_ENTEXAM_SP_SCHOLAR_BORDER_M primary key (ENTEXAMYEAR, APPLICANTDIV, COURSECD, MAJORCD, EXAMCOURSECD, SHDIV)
