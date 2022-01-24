-- $Id: bf235f27fefb68001f0aad8b5397e9a0d041e739 $

drop table ENTEXAM_MIRAI_TAMASEI_DAT_OLD
create table ENTEXAM_MIRAI_TAMASEI_DAT_OLD like ENTEXAM_MIRAI_TAMASEI_DAT
insert into ENTEXAM_MIRAI_TAMASEI_DAT_OLD select * from ENTEXAM_MIRAI_TAMASEI_DAT

drop table ENTEXAM_MIRAI_TAMASEI_DAT

create table ENTEXAM_MIRAI_TAMASEI_DAT \
( \
    ENTEXAMYEAR             varchar(4)  not null, \
    MIRAI_TESTDIV           varchar(100), \
    TEST_DATE               varchar(10), \
    TEST_NAME               varchar(150), \
    TEST_NAME_ABBV          varchar(100), \
    EXAMNO                  varchar(10)  not null, \
    EXAMNO_MANUAL           varchar(30), \
    APPNO                   varchar(100), \
    LOGIN_USER_SEQ          varchar(100), \
    LOGIN_MAIL              varchar(100), \
    SHIGANSYA_SEQ           varchar(100), \
    NAME_SEI                varchar(60), \
    NAME_MEI                varchar(60), \
    NAME_KANA_SEI           varchar(120), \
    NAME_KANA_MEI           varchar(120), \
    BIRTHDAY                varchar(10), \
    ZIPCD                   varchar(8), \
    PREF_NAME               varchar(30), \
    CITY_NAME               varchar(90), \
    BANCHI_NAME             varchar(90), \
    ADDRESS2                varchar(150), \
    TELNO                   varchar(14), \
    MIRAI_FS_CD             varchar(10), \
    MIRAI_FS_NAME           varchar(150), \
    MIRAI_FS_NAME_OTHER     varchar(100), \
    MIRAI_SH_CD1            varchar(10), \
    MIRAI_SH_NAME1          varchar(150), \
    MIRAI_SH_NAME_OTHER1    varchar(100), \
    MIRAI_SH_CD2            varchar(10), \
    MIRAI_SH_NAME2          varchar(150), \
    MIRAI_SH_NAME_OTHER2    varchar(100), \
    MIRAI_SH_CD3            varchar(10), \
    MIRAI_SH_NAME3          varchar(150), \
    MIRAI_SH_NAME_OTHER3    varchar(100), \
    MIRAI_PS_CD             varchar(10), \
    MIRAI_PS_NAME           varchar(150), \
    MIRAI_PS_NAME_OTHER     varchar(100), \
    GNAME_SEI               varchar(60), \
    GNAME_MEI               varchar(60), \
    GKANA_SEI               varchar(120), \
    GKANA_MEI               varchar(120), \
    GTELNO                  varchar(14), \
    EXPLAIN                 varchar(100), \
    APP_INFO_DIV            varchar(100), \
    SRV_RCPT_DIV            varchar(100), \
    COUNTER_RCPTNO          varchar(100), \
    REMARKS                 varchar(100), \
    SETTLE_MONEY            varchar(100), \
    EXAM_MONEY              varchar(100), \
    FEES                    varchar(100), \
    PAY_TYPE_CD             varchar(100), \
    PAY_DUE_DATE            varchar(100), \
    PAY_DAY                 varchar(100), \
    PAY_INFO_DIV            varchar(100), \
    APP_COMP_DAY            varchar(100), \
    CANCEL_FLG              varchar(100), \
    CANCEL_DATE             varchar(100), \
    TEST_NO                 varchar(10), \
    STATUS_CD               varchar(10), \
    APPLICANTDIV            varchar(1) not null, \
    TESTDIV                 varchar(2) not null, \
    EXAM_TYPE               varchar(2) not null, \
    REGISTERCD              varchar(10),  \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_TAMASEI_DAT add constraint \
PK_ENTE_MIRAI_MEI primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAMNO)

insert into ENTEXAM_MIRAI_TAMASEI_DAT \
select \
    ENTEXAMYEAR, \
    MIRAI_TESTDIV, \
    TEST_DATE, \
    TEST_NAME, \
    TEST_NAME_ABBV, \
    EXAMNO, \
    EXAMNO_MANUAL, \
    APPNO, \
    LOGIN_USER_SEQ, \
    LOGIN_MAIL, \
    SHIGANSYA_SEQ, \
    NAME_SEI, \
    NAME_MEI, \
    NAME_KANA_SEI, \
    NAME_KANA_MEI, \
    BIRTHDAY, \
    ZIPCD, \
    PREF_NAME, \
    CITY_NAME, \
    BANCHI_NAME, \
    ADDRESS2, \
    TELNO, \
    MIRAI_FS_CD, \
    MIRAI_FS_NAME, \
    MIRAI_FS_NAME_OTHER, \
    MIRAI_SH_CD1, \
    MIRAI_SH_NAME1, \
    MIRAI_SH_NAME_OTHER1, \
    MIRAI_SH_CD2, \
    MIRAI_SH_NAME2, \
    MIRAI_SH_NAME_OTHER2, \
    MIRAI_SH_CD3, \
    MIRAI_SH_NAME3, \
    MIRAI_SH_NAME_OTHER3, \
    MIRAI_PS_CD, \
    MIRAI_PS_NAME, \
    MIRAI_PS_NAME_OTHER, \
    GNAME_SEI, \
    GNAME_MEI, \
    GKANA_SEI, \
    GKANA_MEI, \
    GTELNO, \
    EXPLAIN, \
    APP_INFO_DIV, \
    SRV_RCPT_DIV, \
    COUNTER_RCPTNO, \
    REMARKS, \
    SETTLE_MONEY, \
    EXAM_MONEY, \
    FEES, \
    PAY_TYPE_CD, \
    PAY_DUE_DATE, \
    PAY_DAY, \
    PAY_INFO_DIV, \
    APP_COMP_DAY, \
    CANCEL_FLG, \
    CANCEL_DATE, \
    cast(null as varchar(10)) as TEST_NO, \
    cast(null as varchar(10)) as STATUS_CD, \
    APPLICANTDIV, \
    TESTDIV, \
    EXAM_TYPE, \
    REGISTERCD, \
    UPDATED \
from ENTEXAM_MIRAI_TAMASEI_DAT_OLD
