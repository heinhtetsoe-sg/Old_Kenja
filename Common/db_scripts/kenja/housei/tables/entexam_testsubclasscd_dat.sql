-- kanji=´Á»ú
drop table ENTEXAM_TESTSUBCLASSCD_DAT

create table ENTEXAM_TESTSUBCLASSCD_DAT \
( \
    ENTEXAMYEAR     varchar(4)  not null, \
    APPLICANTDIV    varchar(1)  not null, \
    TESTDIV         varchar(1)  not null, \
    EXAM_TYPE       varchar(1)  not null, \
    TESTSUBCLASSCD  varchar(1)  not null, \
    SHOWORDER       smallint, \
    LINK_JUDGE_DIV  varchar(1),  \
    REGISTERCD      varchar(8),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_TESTSUBCLASSCD_DAT add constraint \
PK_ENTEXAM_TESTSUB primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, TESTSUBCLASSCD)
