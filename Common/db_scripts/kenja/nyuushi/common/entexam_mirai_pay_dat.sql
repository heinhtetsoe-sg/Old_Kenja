-- $Id: 41a7d09ee6e5d427191800cbdfb1a313d700fecd $

drop table ENTEXAM_MIRAI_PAY_DAT

create table ENTEXAM_MIRAI_PAY_DAT \
( \
    MIRAI_SCHOOLCD          varchar(100), \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPNO                   varchar(100), \
    SHIGANSYA_ID            varchar(100), \
    EXAMNO                  varchar(10)  not null, \
    EXAM_NAME_SEI           varchar(150), \
    EXAM_NAME_MEI           varchar(150), \
    SEX                     varchar(10), \
    MIRAI_FS_NAME           varchar(150), \
    MIRAI_PS_NAME           varchar(150), \
    ITEM_GROUP              varchar(100), \
    ITEM_ID                 varchar(100), \
    ITEM_NAME               varchar(100), \
    BULK_MONEY              varchar(100), \
    DELAY_PAY_LUMP          varchar(100), \
    ACCEPT_START_DATE       varchar(100), \
    PAY_DUE_DATE_MST        varchar(100), \
    DELAY_DUE_DATE          varchar(100), \
    ITEM_EXPLAIN            varchar(300), \
    PAY_DEC                 varchar(100), \
    PAY_DEC_DATE            varchar(100), \
    PAY_DEC_REASON          varchar(100), \
    PAY_DEC_REASON_DETAIL   varchar(150), \
    MEMO_MST                varchar(150), \
    ITEM_PAY_DIV            varchar(100), \
    PAY_TYPE_CD             varchar(100), \
    APP_EXAMNO              varchar(100), \
    NAME_SEI                varchar(60), \
    NAME_MEI                varchar(60), \
    NAME_KANA_SEI           varchar(120), \
    NAME_KANA_MEI           varchar(120), \
    BIRTHDAY                varchar(10), \
    TELNO                   varchar(14), \
    MAIL                    varchar(100), \
    SETTLE_MONEY            varchar(100), \
    DEMAND_TOTAL_MONEY      varchar(100), \
    FEES                    varchar(100), \
    PAY_INFO_DIV            varchar(100), \
    APP_DATED               varchar(100), \
    PAY_DUE_DATE            varchar(100), \
    PAY_DATED               varchar(100), \
    ENT_DEC                 varchar(100), \
    ENT_DEC_DATE            varchar(100), \
    ENT_DEC_REASON          varchar(100), \
    ENT_DEC_REASON_DETAIL   varchar(150), \
    MEMO_PAY                varchar(150), \
    APPLICANTDIV            varchar(1) not null, \
    TESTDIV                 varchar(2), \
    PROCEDUREDATE           varchar(10), \
    REGISTERCD              varchar(10),  \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_PAY_DAT add constraint \
PK_ENTE_MIRAI_PAY primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
