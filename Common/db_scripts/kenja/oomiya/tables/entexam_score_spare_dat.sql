drop table ENTEXAM_SCORE_SPARE_DAT

create table ENTEXAM_SCORE_SPARE_DAT \
( \
    ENTEXAMYEAR     varchar(4)  not null, \
    APPLICANTDIV    varchar(1)  not null, \
    TESTDIV         varchar(1)  not null, \
    EXAM_TYPE       varchar(1)  not null, \
    RECEPTNO        varchar(5)  not null, \
    TESTSUBCLASSCD  varchar(1)  not null, \
    SEQ             varchar(3)  not null, \
    SCORE1          smallint, \
    SCORE2          smallint, \
    SCORE3          smallint, \
    SCORE4          smallint, \
    SCORE5          smallint, \
    AVG1            decimal(5, 2), \
    AVG2            decimal(5, 2), \
    AVG3            decimal(5, 2), \
    AVG4            decimal(5, 2), \
    AVG5            decimal(5, 2), \
    REMARK1         varchar(150), \
    REMARK2         varchar(150), \
    REMARK3         varchar(150), \
    REMARK4         varchar(150), \
    REMARK5         varchar(150), \
    REGISTERCD      varchar(8),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SCORE_SPARE_DAT add constraint \
PK_ENTEXAM_SCORE_S primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO, TESTSUBCLASSCD, SEQ)
