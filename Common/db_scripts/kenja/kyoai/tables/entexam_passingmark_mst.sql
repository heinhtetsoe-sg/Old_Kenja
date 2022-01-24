-- kanji=´Á»ú
drop table ENTEXAM_PASSINGMARK_MST

create table ENTEXAM_PASSINGMARK_MST \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    TESTDIV             varchar(1)  not null, \
    EXAM_TYPE           varchar(1)  not null, \
    SHDIV               varchar(1)  not null, \
    COURSECD            varchar(1)  not null, \
    MAJORCD             varchar(3)  not null, \
    EXAMCOURSECD        varchar(4)  not null, \
    BORDER_SCORE        smallint, \
    SUCCESS_CNT         smallint, \
    BACK_RATE           smallint, \
    CAPA_CNT            smallint, \
    BORDER_SCORE_CANDI  smallint, \
    SUCCESS_CNT_CANDI   smallint, \
    BORDER_DEVIATION    decimal(4,1), \
    SUCCESS_CNT_SPECIAL  SMALLINT, \
    SUCCESS_CNT_SPECIAL2 SMALLINT, \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_PASSINGMARK_MST add constraint \
PK_ENTEXAM_PASS primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,SHDIV,COURSECD,MAJORCD,EXAMCOURSECD)
