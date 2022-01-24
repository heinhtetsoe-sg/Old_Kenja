-- $Id: rep-entexam_applicantbase_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table ENTEXAM_APPLICANTBASE_DAT_OLD

create table ENTEXAM_APPLICANTBASE_DAT_OLD like ENTEXAM_APPLICANTBASE_DAT

insert into ENTEXAM_APPLICANTBASE_DAT_OLD select * from ENTEXAM_APPLICANTBASE_DAT

drop table ENTEXAM_APPLICANTBASE_DAT

create table ENTEXAM_APPLICANTBASE_DAT \
( \
    ENTEXAMYEAR          varchar(4)  not null, \
    APPLICANTDIV         varchar(1)  not null, \
    EXAMNO               varchar(5)  not null, \
    TESTDIV              varchar(1)  not null, \
    SHDIV                varchar(1)  not null, \
    DESIREDIV            varchar(1)  not null, \
    TESTDIV1             varchar(1), \
    TESTDIV2             varchar(1), \
    TESTDIV3             varchar(1), \
    TESTDIV4             varchar(1), \
    TESTDIV5             varchar(1), \
    TESTDIV6             varchar(1), \
    SPECIAL_REASON_DIV   varchar(2), \
    RECEPTDATE           date, \
    NAME                 varchar(60), \
    NAME_KANA            varchar(120), \
    SEX                  varchar(1), \
    ERACD                varchar(1), \
    BIRTH_Y              varchar(2), \
    BIRTH_M              varchar(2), \
    BIRTH_D              varchar(2), \
    BIRTHDAY             date, \
    FS_CD                varchar(7), \
    FS_NAME              varchar(45), \
    FS_AREA_CD           varchar(2), \
    FS_NATPUBPRIDIV      varchar(1), \
    FS_GRDYEAR           varchar(4), \
    FS_ERACD             varchar(1), \
    FS_Y                 varchar(2), \
    FS_M                 varchar(2), \
    FS_DAY               date, \
    PRISCHOOLCD          varchar(7), \
    INTERVIEW_ATTEND_FLG varchar(1), \
    SUC_COURSECD         varchar(1), \
    SUC_MAJORCD          varchar(3), \
    SUC_COURSECODE       varchar(4), \
    JUDGEMENT            varchar(1), \
    SPECIAL_MEASURES     varchar(1), \
    PROCEDUREDIV         varchar(1), \
    PROCEDUREDATE        date, \
    ENTDIV               varchar(1), \
    HONORDIV             varchar(1), \
    SUCCESS_NOTICENO     varchar(6), \
    FAILURE_NOTICENO     varchar(6), \
    REMARK1              varchar(60), \
    REMARK2              varchar(120), \
    RECOM_EXAMNO         varchar(5), \
    PICTURE_ERACD        varchar(1), \
    PICTURE_Y            varchar(2), \
    PICTURE_M            varchar(2), \
    PICTURE_DAY          date, \
    SELECT_SUBCLASS_DIV  varchar(1), \
    SHIFT_DESIRE_FLG     varchar(1), \
    REGISTERCD           varchar(8),  \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DAT add constraint \
PK_ENTEXAM_APP primary key (ENTEXAMYEAR, EXAMNO)

insert into ENTEXAM_APPLICANTBASE_DAT \
  select \
    ENTEXAMYEAR, \
    APPLICANTDIV, \
    EXAMNO, \
    TESTDIV, \
    SHDIV, \
    DESIREDIV, \
    TESTDIV1, \
    TESTDIV2, \
    TESTDIV3, \
    TESTDIV4, \
    TESTDIV5, \
    TESTDIV6, \
    cast(null as varchar(2)), \
    RECEPTDATE, \
    NAME, \
    NAME_KANA, \
    SEX, \
    ERACD, \
    BIRTH_Y, \
    BIRTH_M, \
    BIRTH_D, \
    BIRTHDAY, \
    FS_CD, \
    FS_NAME, \
    FS_AREA_CD, \
    FS_NATPUBPRIDIV, \
    FS_GRDYEAR, \
    FS_ERACD, \
    FS_Y, \
    FS_M, \
    FS_DAY, \
    PRISCHOOLCD, \
    INTERVIEW_ATTEND_FLG, \
    SUC_COURSECD, \
    SUC_MAJORCD, \
    SUC_COURSECODE, \
    JUDGEMENT, \
    SPECIAL_MEASURES, \
    PROCEDUREDIV, \
    PROCEDUREDATE, \
    ENTDIV, \
    HONORDIV, \
    SUCCESS_NOTICENO, \
    FAILURE_NOTICENO, \
    REMARK1, \
    REMARK2, \
    RECOM_EXAMNO, \
    PICTURE_ERACD, \
    PICTURE_Y, \
    PICTURE_M, \
    PICTURE_DAY, \
    SELECT_SUBCLASS_DIV, \
    SHIFT_DESIRE_FLG, \
    REGISTERCD, \
    UPDATED \
  from ENTEXAM_APPLICANTBASE_DAT_OLD

