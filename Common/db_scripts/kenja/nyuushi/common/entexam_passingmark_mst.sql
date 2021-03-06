-- kanji=漢字
drop table ENTEXAM_PASSINGMARK_MST

create table ENTEXAM_PASSINGMARK_MST \
( \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPLICANTDIV            varchar(1)  not null, \
    TESTDIV                 varchar(2)  not null, \
    EXAM_TYPE               varchar(2)  not null, \
    SHDIV                   varchar(1)  not null, \
    COURSECD                varchar(1)  not null, \
    MAJORCD                 varchar(3)  not null, \
    EXAMCOURSECD            varchar(4)  not null, \
    SUC_SHDIV               varchar(1)  , \
    BORDER_SCORE            smallint, \
    SUCCESS_CNT             smallint, \
    BACK_RATE               smallint, \
    CAPA_CNT                smallint, \
    BORDER_SCORE_CANDI      smallint, \
    SUCCESS_CNT_CANDI       smallint, \
    BORDER_DEVIATION        decimal(4,1), \
    SUCCESS_CNT_SPECIAL     SMALLINT, \
    SUCCESS_CNT_SPECIAL2    SMALLINT, \
    SUCCESS_RATE            decimal(4,1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp, \
    JUDGE_STATE             VARCHAR(1) \
) in usr1dms index in idx1dms

alter table ENTEXAM_PASSINGMARK_MST add constraint \
PK_ENTEXAM_PASS primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,SHDIV,COURSECD,MAJORCD,EXAMCOURSECD)
