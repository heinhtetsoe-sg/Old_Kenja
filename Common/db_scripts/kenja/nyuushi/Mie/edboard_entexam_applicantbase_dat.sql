-- $Id: 082d9c21eefccc82bd3fabb17dd6b208019ff14d $

drop table EDBOARD_ENTEXAM_APPLICANTBASE_DAT
create table EDBOARD_ENTEXAM_APPLICANTBASE_DAT( \
    EDBOARD_SCHOOLCD     varchar(12)   NOT NULL, \
    ENTEXAMYEAR          varchar(4)    NOT NULL, \
    APPLICANTDIV         varchar(1)    NOT NULL, \
    EXAMNO               varchar(10)   NOT NULL, \
    TESTDIV              varchar(1)    NOT NULL, \
    SHDIV                varchar(1)    NOT NULL, \
    DESIREDIV            varchar(1)    NOT NULL, \
    TESTDIV0             varchar(1), \
    TESTDIV1             varchar(1), \
    TESTDIV2             varchar(1), \
    TESTDIV3             varchar(1), \
    TESTDIV4             varchar(1), \
    TESTDIV5             varchar(1), \
    SPECIAL_REASON_DIV   varchar(1), \
    RECEPTDATE           date, \
    NAME                 varchar(120), \
    NAME_KANA            varchar(240), \
    SEX                  varchar(1), \
    ERACD                varchar(1), \
    BIRTH_Y              varchar(2), \
    BIRTH_M              varchar(2), \
    BIRTH_D              varchar(2), \
    BIRTHDAY             date, \
    FS_CD                varchar(7), \
    FS_NAME              varchar(45), \
    FS_AREA_CD           varchar(2), \
    FS_AREA_DIV          varchar(2), \
    FS_NATPUBPRIDIV      varchar(1), \
    FS_GRDYEAR           varchar(4), \
    FS_ERACD             varchar(1), \
    FS_Y                 varchar(2), \
    FS_M                 varchar(2), \
    FS_DAY               date, \
    FS_GRDDIV            varchar(1), \
    PRISCHOOLCD          varchar(7), \
    INTERVIEW_ATTEND_FLG varchar(1), \
    SUC_COURSECD         varchar(1), \
    SUC_MAJORCD          varchar(3), \
    SUC_COURSECODE       varchar(4), \
    JUDGEMENT            varchar(1), \
    JUDGE_KIND           varchar(1), \
    SUB_ORDER            varchar(4), \
    SPECIAL_MEASURES     varchar(1), \
    PROCEDUREDIV         varchar(1), \
    PROCEDUREDATE        date, \
    PAY_MONEY            integer, \
    ENTDIV               varchar(1), \
    HONORDIV             varchar(1), \
    SUCCESS_NOTICENO     varchar(6), \
    FAILURE_NOTICENO     varchar(6), \
    REMARK1              varchar(246), \
    REMARK2              varchar(246), \
    RECOM_EXAMNO         varchar(10), \
    PICTURE_ERACD        varchar(1), \
    PICTURE_Y            varchar(2), \
    PICTURE_M            varchar(2), \
    PICTURE_DAY          date, \
    SELECT_SUBCLASS_DIV  varchar(1), \
    SHIFT_DESIRE_FLG     varchar(1), \
    SH_SCHOOLCD          varchar(7), \
    SLIDE_FLG            varchar(1), \
    GENERAL_FLG          varchar(1), \
    SPORTS_FLG           varchar(1), \
    DORMITORY_FLG        varchar(1), \
    RECOM_ITEM1          varchar(1), \
    RECOM_ITEM2          varchar(1), \
    RECOM_ITEM3          varchar(1), \
    RECOM_ITEM4          varchar(1), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EDBOARD_ENTEXAM_APPLICANTBASE_DAT add constraint \
PK_ED_ENTEXAM_APB primary key (EDBOARD_SCHOOLCD, ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
