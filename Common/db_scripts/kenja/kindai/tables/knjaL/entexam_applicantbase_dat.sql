-- kanji=´Á»ú
-- $Id: entexam_applicantbase_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table ENTEXAM_APPLICANTBASE_DAT

create table ENTEXAM_APPLICANTBASE_DAT \
( \
    ENTEXAMYEAR             varchar(4)  not null, \
    TESTDIV                 varchar(1)  not null, \
    EXAMNO                  varchar(4)  not null, \
    RECEPT_DATE             date, \
    SHDIV                   varchar(1), \
    DESIREDIV               varchar(2), \
    APPLICANTDIV            varchar(1), \
    CLUBCD                  varchar(4), \
    SPECIAL_REASON_DIV      varchar(1),  not null \
    NAME                    varchar(60), \
    NAME_KANA               varchar(120), \
    SEX                     varchar(1), \
    BIRTHDAY                date, \
    ADDRESSCD               varchar(2), \
    TELNO                   varchar(14), \
    LOCATIONCD              varchar(2), \
    NATPUBPRIDIV            varchar(1), \
    FS_CD                   varchar(7), \
    FS_GRDYEAR              varchar(4), \
    FS_HRCLASS              varchar(3), \
    FS_ATTENDNO             varchar(3), \
    PS_CD                   varchar(7), \
    APPROVAL_FLG            varchar(1), \
    GNAME                   varchar(60), \
    GKANA                   varchar(120), \
    GTELNO                  varchar(14), \
    EXAMHALLNO              varchar(4), \
    ATTEND_ALL_FLG          varchar(1), \
    A_TOTAL                 smallint, \
    A_AVERAGE               decimal(4,1), \
    A_TOTAL_RANK            smallint, \
    A_DIV_RANK              smallint, \
    B_TOTAL                 smallint, \
    B_AVERAGE               decimal(4,1), \
    B_TOTAL_RANK            smallint, \
    B_DIV_RANK              smallint, \
    JUDGEMENT               varchar(2), \
    REGULARSUCCESS_FLG      varchar(1), \
    JUDGEMENT_GROUP_NO      varchar(2), \
    INTERVIEW_ATTEND_FLG    varchar(1), \
    SCALASHIPDIV            varchar(2), \
    SUC_COURSECD            varchar(1), \
    SUC_MAJORCD             varchar(3), \
    SUC_COURSECODE          varchar(4), \
    PROCEDUREDIV            varchar(1), \
    ENTDIV                  varchar(1), \
    FORMNO                  varchar(6), \
    SUCCESS_NOTICENO        varchar(4), \
    OLD_SUCCESS_NOTICENO    varchar(4), \
    FAILURE_NOTICENO        varchar(4), \
    REGISTERCD              varchar(8),  \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DAT add constraint \
PK_ENTEXAM_APP primary key (ENTEXAMYEAR, TESTDIV, EXAMNO)
