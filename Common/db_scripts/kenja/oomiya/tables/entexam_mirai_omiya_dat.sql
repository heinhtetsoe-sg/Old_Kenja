-- $Id: entexam_mirai_omiya_dat.sql 64338 2019-01-08 01:48:32Z tawada $

drop TABLE ENTEXAM_MIRAI_OMIYA_DAT
create table ENTEXAM_MIRAI_OMIYA_DAT( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICATION_NO      varchar(20), \
    LOGIN_ID            varchar(20), \
    EMAIL               varchar(60), \
    VOLUNTEER           varchar(20), \
    APPLICANTDIV        varchar(30)  not null, \
    NAME_SEI            varchar(120), \
    NAME_MEI            varchar(120), \
    NAME_KANA_SEI       varchar(120), \
    NAME_KANA_MEI       varchar(120), \
    SEX                 varchar(3), \
    BIRTHDAY            varchar(10), \
    ZIPCD               varchar(8), \
    ADDRESS1            varchar(30), \
    ADDRESS2            varchar(150), \
    ADDRESS3            varchar(150), \
    ADDRESS4            varchar(150), \
    TELNO               varchar(14), \
    FS_CD               varchar(20), \
    FS_NAME             varchar(120), \
    FS_NAME_ETC         varchar(120), \
    SHIBOU_CD1          varchar(20), \
    SHIBOU_NAME1        varchar(120), \
    SHIBOU_NAME_ETC1    varchar(120), \
    SHIBOU_CD2          varchar(20), \
    SHIBOU_NAME2        varchar(120), \
    SHIBOU_NAME_ETC2    varchar(120), \
    SHIBOU_CD3          varchar(20), \
    SHIBOU_NAME3        varchar(120), \
    SHIBOU_NAME_ETC3    varchar(120), \
    SHIBOU_CD4          varchar(20), \
    SHIBOU_NAME4        varchar(120), \
    SHIBOU_NAME_ETC4    varchar(120), \
    PRISCHOOLCD         varchar(20), \
    PRISCHOOL_NAME      varchar(200), \
    PRISCHOOL_NAME_ETC  varchar(120), \
    GNAME_SEI           varchar(120), \
    GNAME_MEI           varchar(120), \
    GKANA_SEI           varchar(120), \
    GKANA_MEI           varchar(120), \
    GTELNO              varchar(15), \
    GTELNO2             varchar(120), \
    APPLICANT_INFO_DIV  varchar(15), \
    TRANSCRIPT_DIV      varchar(15), \
    WINDOW_RECEPTNO     varchar(20), \
    REMARK              varchar(150), \
    SETTLEMENT_FIGURE   varchar(7), \
    EXAM_FEE            varchar(7), \
    ADMINISTRATIVE_FEE  varchar(7), \
    PAY_DIVCD           varchar(65), \
    PAY_LIMITDATE       varchar(20), \
    PAID_MONEY_DATE     varchar(20), \
    PAID_INFO_DIV       varchar(15), \
    REQUEST_COMP_DATE   varchar(20), \
    CANCEL_FLG          varchar(30), \
    CANCEL_DATE         varchar(20), \
    RECEPT_REASON       varchar(300), \
    ETC8                varchar(700), \
    WHEN_DECISION       varchar(90), \
    BRO_SCH_FLG         varchar(90), \
    BRO_SCH_NAME        varchar(120), \
    BRO_SCH_RELA        varchar(10), \
    BRO_SCH_GRADE       varchar(40), \
    BRO_SCH_GRD_FLG     varchar(60), \
    BRO_SCH_GRD_NAME    varchar(60), \
    BRO_SCH_GRD_RELA    varchar(10), \
    BRO_SCH_GRD_YEAR    varchar(4), \
    FREE_ENTRY          varchar(1500), \
    TESTDIV             varchar(2)   not null, \
    TEST_DATE           varchar(20), \
    TEST_NAME           varchar(40), \
    TEST_NAME_ABBV      varchar(30), \
    EXAMNO              varchar(10)  not null, \
    EXAMNO_HAND         varchar(10), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_OMIYA_DAT add constraint PK_ENT_MIRAI_OM_D primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAMNO)
