-- kanji=´Á»ú
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
