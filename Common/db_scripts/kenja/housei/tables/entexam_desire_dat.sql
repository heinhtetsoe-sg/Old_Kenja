drop table ENTEXAM_DESIRE_DAT

create table ENTEXAM_DESIRE_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    TESTDIV             varchar(1)  not null, \
    EXAM_TYPE           varchar(1)  not null, \
    EXAMNO              varchar(5)  not null, \
    APPLICANT_DIV       varchar(1), \
    EXAMINEE_DIV        varchar(1), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_DESIRE_DAT add constraint \
PK_ENTEXAM_DESIRE primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, EXAMNO)
