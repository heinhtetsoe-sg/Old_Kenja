drop table ENTEXAM_SPECIAL_SCHOLARSHIP_MST

create table ENTEXAM_SPECIAL_SCHOLARSHIP_MST \
( \
    ENTEXAMYEAR      varchar(4)  not null, \
    APPLICANTDIV     varchar(1)  not null, \
    TESTDIV          varchar(1)  not null, \
    SP_SCHOLAR_CD    varchar(3)  not null, \
    SP_SCHOLAR_NAME  varchar(60) not null, \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SPECIAL_SCHOLARSHIP_MST add constraint \
PK_ENTEXAM_SP_SCHOLAR_M primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SP_SCHOLAR_CD)
