-- kanji=´Á»ú

drop table ENTEXAM_SCORE_DAT_OLD
create table ENTEXAM_SCORE_DAT_OLD like ENTEXAM_SCORE_DAT
insert into ENTEXAM_SCORE_DAT_OLD select * from ENTEXAM_SCORE_DAT

drop table ENTEXAM_SCORE_DAT

create table ENTEXAM_SCORE_DAT \
( \
    ENTEXAMYEAR     varchar(4)  not null, \
    APPLICANTDIV    varchar(1)  not null, \
    TESTDIV         varchar(1)  not null, \
    EXAM_TYPE       varchar(1)  not null, \
    RECEPTNO        varchar(4)  not null, \
    TESTSUBCLASSCD  varchar(1)  not null, \
    ATTEND_FLG      varchar(1), \
    SCORE           smallint, \
    STD_SCORE       decimal(5,2), \
    RANK            smallint, \
    REGISTERCD      varchar(8),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SCORE_DAT add constraint \
PK_ENTEXAM_SCORE primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO,TESTSUBCLASSCD)

insert into ENTEXAM_SCORE_DAT select * from ENTEXAM_SCORE_DAT_OLD
