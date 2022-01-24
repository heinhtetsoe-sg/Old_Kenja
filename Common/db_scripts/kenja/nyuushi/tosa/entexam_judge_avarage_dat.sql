-- $Id: 517d9781d18a5d0956e01e9579a68dad02c639f7 $
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
    COUNT           SMALLINT, \
    CALC_STDDEV     DECIMAL(8,5), \
    CALC_AVG        DECIMAL(8,5), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_JUDGE_AVARAGE_DAT add constraint PK_ENTEXAM_JUD_AVG primary key(ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, TESTSUBCLASSCD)
