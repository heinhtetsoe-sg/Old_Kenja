-- $Id: entexam_judge_avarage_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table ENTEXAM_JUDGE_AVARAGE_DAT
create table ENTEXAM_JUDGE_AVARAGE_DAT( \
    ENTEXAMYEAR     varchar(4) not null, \
    APPLICANTDIV    varchar(1) not null, \
    TESTDIV         varchar(1) not null, \
    EXAM_TYPE       varchar(1) not null, \
    TESTSUBCLASSCD  varchar(1) not null, \
    AVARAGE_MEN     decimal(4,1), \
    AVARAGE_WOMEN   decimal(4,1), \
    AVARAGE_TOTAL   decimal(4,1), \
    MAX_SCORE       smallint, \
    MIN_SCORE       smallint, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_JUDGE_AVARAGE_DAT add constraint PK_ENTEXAM_JUD_AVG primary key(ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, TESTSUBCLASSCD)
