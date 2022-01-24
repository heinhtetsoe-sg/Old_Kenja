-- $Id: 2533eb49a51a4eb71093a168850111c805cbef76 $

drop table ENTEXAM_MIRAI_MAIKEI_DAT

create table ENTEXAM_MIRAI_MAIKEI_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    ITEM2               varchar(100), \
    ITEM3               varchar(100), \
    ITEM4               varchar(100), \
    SHIGANSYA_SEQ       varchar(100), \
    MIRAI_TESTDIV       varchar(100) not null, \
    NAME_SEI            varchar(60), \
    NAME_MEI            varchar(60), \
    NAME_KANA_SEI       varchar(120), \
    NAME_KANA_MEI       varchar(120), \
    NOT_PC_KANJI_NAME   varchar(100), \
    SEX                 varchar(10), \
    BIRTHDAY            varchar(10), \
    ZIPCD               varchar(8), \
    PREF_NAME           varchar(30), \
    CITY_NAME           varchar(90), \
    BANCHI_NAME         varchar(90), \
    ADDRESS2            varchar(150), \
    TELNO               varchar(14), \
    MIRAI_FS_CD         varchar(10), \
    MIRAI_FS_NAME       varchar(150), \
    ITEM22              varchar(100), \
    MIRAI_PS_CD         varchar(10), \
    MIRAI_PS_NAME       varchar(150), \
    ITEM25              varchar(100), \
    GNAME_SEI           varchar(60), \
    GNAME_MEI           varchar(60), \
    GKANA_SEI           varchar(120), \
    GKANA_MEI           varchar(120), \
    GTELNO              varchar(14), \
    ITEM31              varchar(100), \
    ITEM32              varchar(100), \
    ITEM33              varchar(100), \
    ITEM34              varchar(100), \
    ITEM35              varchar(100), \
    ITEM36              varchar(100), \
    ITEM37              varchar(100), \
    ITEM38              varchar(100), \
    ITEM39              varchar(100), \
    ITEM40              varchar(100), \
    ITEM41              varchar(100), \
    ITEM42              varchar(100), \
    ITEM43              varchar(100), \
    ITEM44              varchar(100), \
    ITEM45              varchar(100), \
    ITEM46              varchar(100), \
    FS_GRDYEAR          varchar(60), \
    ITEM48              varchar(100), \
    ITEM49              varchar(100), \
    DORMITORY_FLG       varchar(100), \
    ITEM51              varchar(100), \
    ITEM52              varchar(100), \
    ITEM53              varchar(100), \
    ITEM54              varchar(100), \
    ITEM55              varchar(100), \
    ITEM56              varchar(100), \
    ITEM57              varchar(100), \
    ITEM58              varchar(100), \
    ITEM59              varchar(100), \
    ITEM60              varchar(100), \
    RELATIONSHIP        varchar(30), \
    RELATIONSHIP_OTHER  varchar(30), \
    RELANAME1           varchar(60), \
    RELATIONSHIP1       varchar(30), \
    RELA_AGE1           varchar(3), \
    RELA_JOB1           varchar(120), \
    RELANAME2           varchar(60), \
    RELATIONSHIP2       varchar(30), \
    RELA_AGE2           varchar(3), \
    RELA_JOB2           varchar(120), \
    RELANAME3           varchar(60), \
    RELATIONSHIP3       varchar(30), \
    RELA_AGE3           varchar(3), \
    RELA_JOB3           varchar(120), \
    RELANAME4           varchar(60), \
    RELATIONSHIP4       varchar(30), \
    RELA_AGE4           varchar(3), \
    RELA_JOB4           varchar(120), \
    RELANAME5           varchar(60), \
    RELATIONSHIP5       varchar(30), \
    RELA_AGE5           varchar(3), \
    RELA_JOB5           varchar(120), \
    TEST_CD             varchar(10), \
    TEST_DATE           varchar(10) not null, \
    TEST_NAME           varchar(60), \
    TEST_NAME_ABBV      varchar(100), \
    EXAMNO              varchar(20)  not null, \
    ITEM88              varchar(30), \
    SH_FLG              varchar(120), \
    MIRAI_FS_ADDR       varchar(300), \
    APPLICANTDIV        varchar(1) not null, \
    TESTDIV             varchar(1) not null, \
    EXAM_TYPE           varchar(2) not null, \
    GRADE               varchar(2), \
    DISTINCT_ID         varchar(3), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_MAIKEI_DAT add constraint \
PK_ENTE_MIRAI_MEI primary key (ENTEXAMYEAR, MIRAI_TESTDIV, EXAMNO)
