-- kanji=漢字
-- $Id: 6cfa47d09873d92edcdc166462a541cbe7087240 $

drop table ENTEXAM_MIRAI_HGRM_DAT

create table ENTEXAM_MIRAI_HGRM_DAT \
( \
    ENTEXAMYEAR         varchar(4) not null, \
    ITEM2               varchar(100), \
    ITEM3               varchar(100), \
    ITEM4               varchar(100), \
    EXAMNO              varchar(10) not null, \
    MIRAI_TESTDIV       varchar(100), \
    NAME_SEI            varchar(60), \
    NAME_MEI            varchar(60), \
    NAME_KANA_SEI       varchar(120), \
    NAME_KANA_MEI       varchar(120), \
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
    ITEM21              varchar(100), \
    MIRAI_PS_CD         varchar(10), \
    MIRAI_PS_NAME       varchar(150), \
    ITEM24              varchar(100), \
    GNAME_SEI           varchar(60), \
    GNAME_MEI           varchar(60), \
    GKANA_SEI           varchar(120), \
    GKANA_MEI           varchar(120), \
    GZIPCD              varchar(8), \
    GPREF_NAME          varchar(30), \
    GCITY_NAME          varchar(90), \
    GBANCHI_NAME        varchar(90), \
    GADDRESS2           varchar(150), \
    GTELNO              varchar(14), \
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
    ITEM47              varchar(100), \
    ITEM48              varchar(100), \
    ITEM49              varchar(100), \
    TEST_CD             varchar(10), \
    TEST_DATE           varchar(10) not null, \
    TEST_NAME           varchar(100), \
    TEST_NAME_ABBV      varchar(100), \
    RECEPTNO            varchar(10) not null, \
    ITEM55              varchar(30), \
    APPLICANTDIV        varchar(1) not null, \
    TESTDIV             varchar(2) not null, \
    SHDIV               varchar(1), \
    COURSEDIV           varchar(1), \
    EXAM_TYPE           varchar(2), \
    TORIKOMI_DATE       DATE, \
    TORIKOMI_TIME       TIME, \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_HGRM_DAT add constraint \
PK_ENTE_MIRAI_HGRM primary key (ENTEXAMYEAR, RECEPTNO)
