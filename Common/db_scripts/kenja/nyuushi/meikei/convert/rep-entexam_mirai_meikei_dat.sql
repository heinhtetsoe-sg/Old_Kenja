-- kanji=漢字
-- $Id: aef50034579ebe3e8051b22d807b3ab0937aa2eb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_MIRAI_MEIKEI_DAT_OLD
create table ENTEXAM_MIRAI_MEIKEI_DAT_OLD like ENTEXAM_MIRAI_MEIKEI_DAT
insert into ENTEXAM_MIRAI_MEIKEI_DAT_OLD select * from ENTEXAM_MIRAI_MEIKEI_DAT

drop table ENTEXAM_MIRAI_MEIKEI_DAT

create table ENTEXAM_MIRAI_MEIKEI_DAT \
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
    TESTDIV             varchar(2) not null, \
    EXAM_TYPE           varchar(2) not null, \
    GRADE               varchar(2), \
    DISTINCT_ID         varchar(3), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_MEIKEI_DAT add constraint \
PK_ENTE_MIRAI_MEI primary key (ENTEXAMYEAR, TEST_DATE, EXAMNO)

insert into ENTEXAM_MIRAI_MEIKEI_DAT \
select \
        ENTEXAMYEAR, \
        ITEM2, \
        ITEM3, \
        ITEM4, \
        SHIGANSYA_SEQ, \
        MIRAI_TESTDIV, \
        NAME_SEI, \
        NAME_MEI, \
        NAME_KANA_SEI, \
        NAME_KANA_MEI, \
        NOT_PC_KANJI_NAME, \
        SEX, \
        BIRTHDAY, \
        ZIPCD, \
        PREF_NAME, \
        CITY_NAME, \
        BANCHI_NAME, \
        ADDRESS2, \
        TELNO, \
        MIRAI_FS_CD, \
        MIRAI_FS_NAME, \
        ITEM22, \
        MIRAI_PS_CD, \
        MIRAI_PS_NAME, \
        ITEM25, \
        GNAME_SEI, \
        GNAME_MEI, \
        GKANA_SEI, \
        GKANA_MEI, \
        GTELNO, \
        ITEM31, \
        ITEM32, \
        ITEM33, \
        ITEM34, \
        ITEM35, \
        ITEM36, \
        ITEM37, \
        ITEM38, \
        ITEM39, \
        ITEM40, \
        ITEM41, \
        ITEM42, \
        ITEM43, \
        ITEM44, \
        ITEM45, \
        ITEM46, \
        FS_GRDYEAR, \
        ITEM48, \
        ITEM49, \
        DORMITORY_FLG, \
        ITEM51, \
        ITEM52, \
        ITEM53, \
        ITEM54, \
        ITEM55, \
        ITEM56, \
        ITEM57, \
        ITEM58, \
        ITEM59, \
        ITEM60, \
        RELATIONSHIP, \
        RELATIONSHIP_OTHER, \
        RELANAME1, \
        RELATIONSHIP1, \
        RELA_AGE1, \
        RELA_JOB1, \
        RELANAME2, \
        RELATIONSHIP2, \
        RELA_AGE2, \
        RELA_JOB2, \
        RELANAME3, \
        RELATIONSHIP3, \
        RELA_AGE3, \
        RELA_JOB3, \
        RELANAME4, \
        RELATIONSHIP4, \
        RELA_AGE4, \
        RELA_JOB4, \
        RELANAME5, \
        RELATIONSHIP5, \
        RELA_AGE5, \
        RELA_JOB5, \
        TEST_CD, \
        TEST_DATE, \
        TEST_NAME, \
        TEST_NAME_ABBV, \
        EXAMNO, \
        ITEM88, \
        SH_FLG, \
        MIRAI_FS_ADDR, \
        APPLICANTDIV, \
        '0' || TESTDIV, \
        EXAM_TYPE, \
        GRADE, \
        DISTINCT_ID, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_MIRAI_MEIKEI_DAT_OLD
