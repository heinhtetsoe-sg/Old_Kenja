-- kanji=漢字
drop table ENTEXAM_CLASSIFY_PASSINGMARK_MST

create table ENTEXAM_CLASSIFY_PASSINGMARK_MST \
( \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPLICANTDIV            varchar(1)  not null, \
    TESTDIV                 varchar(2)  not null, \
    CLASSIFY_CD             varchar(2)  not null, \
    EXECDATE                timestamp default current timestamp not null, \
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
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_CLASSIFY_PASSINGMARK_MST add constraint \
PK_ENTEXAM_PASS primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,CLASSIFY_CD,EXECDATE)
