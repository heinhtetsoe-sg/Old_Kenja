-- kanji=´Á»ú

drop table ENTEXAM_RECEPT_DAT_OLD
create table ENTEXAM_RECEPT_DAT_OLD like ENTEXAM_RECEPT_DAT
insert into ENTEXAM_RECEPT_DAT_OLD select * from ENTEXAM_RECEPT_DAT

drop table ENTEXAM_RECEPT_DAT

create table ENTEXAM_RECEPT_DAT \
( \
    ENTEXAMYEAR                 varchar(4)  not null, \
    APPLICANTDIV                varchar(1)  not null, \
    TESTDIV                     varchar(1)  not null, \
    EXAM_TYPE                   varchar(1)  not null, \
    RECEPTNO                    varchar(4)  not null, \
    EXAMNO                      varchar(5)  not null, \
    ATTEND_ALL_FLG              varchar(1), \
    TOTAL2                      smallint, \
    AVARAGE2                    decimal(4,1), \
    TOTAL_RANK2                 smallint, \
    DIV_RANK2                   smallint, \
    TOTAL4                      smallint, \
    AVARAGE4                    decimal(4,1), \
    TOTAL_RANK4                 smallint, \
    DIV_RANK4                   smallint, \
    TOTAL1                      smallint, \
    AVARAGE1                    decimal(4,1), \
    TOTAL_RANK1                 smallint, \
    DIV_RANK1                   smallint, \
    TOTAL3                      smallint, \
    AVARAGE3                    decimal(4,1), \
    TOTAL_RANK3                 smallint, \
    DIV_RANK3                   smallint, \
    JUDGE_DEVIATION             decimal(4,1), \
    JUDGE_DEVIATION_DIV         varchar(1), \
    LINK_JUDGE_DEVIATION        decimal(4,1), \
    LINK_JUDGE_DEVIATION_DIV    varchar(1), \
    JUDGE_EXAM_TYPE             varchar(1), \
    JUDGEDIV                    varchar(1), \
    HONORDIV                    varchar(1), \
    ADJOURNMENTDIV              varchar(1), \
    PROCEDUREDIV1               varchar(1), \
    PROCEDUREDATE1              date, \
    REGISTERCD                  varchar(10), \
    UPDATED                     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_RECEPT_DAT add constraint \
PK_ENTEXAM_RCPT primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO)

insert into ENTEXAM_RECEPT_DAT \
select \
    ENTEXAMYEAR, \
    APPLICANTDIV, \
    TESTDIV, \
    EXAM_TYPE, \
    RECEPTNO, \
    EXAMNO, \
    ATTEND_ALL_FLG, \
    TOTAL2, \
    AVARAGE2, \
    TOTAL_RANK2, \
    DIV_RANK2, \
    TOTAL4, \
    AVARAGE4, \
    TOTAL_RANK4, \
    DIV_RANK4, \
    CAST(NULL AS SMALLINT) AS TOTAL1, \
    CAST(NULL AS DECIMAL(4,1)) AS AVARAGE1, \
    CAST(NULL AS SMALLINT) AS TOTAL_RANK1, \
    CAST(NULL AS SMALLINT) AS DIV_RANK1, \
    CAST(NULL AS SMALLINT) AS TOTAL3, \
    CAST(NULL AS DECIMAL(4,1)) AS AVARAGE3, \
    CAST(NULL AS SMALLINT) AS TOTAL_RANK3, \
    CAST(NULL AS SMALLINT) AS DIV_RANK3, \
    JUDGE_DEVIATION, \
    JUDGE_DEVIATION_DIV, \
    LINK_JUDGE_DEVIATION, \
    LINK_JUDGE_DEVIATION_DIV, \
    JUDGE_EXAM_TYPE, \
    JUDGEDIV, \
    HONORDIV, \
    ADJOURNMENTDIV, \
    PROCEDUREDIV1, \
    PROCEDUREDATE1, \
    REGISTERCD, \
    UPDATED \
from \
    ENTEXAM_RECEPT_DAT_OLD
