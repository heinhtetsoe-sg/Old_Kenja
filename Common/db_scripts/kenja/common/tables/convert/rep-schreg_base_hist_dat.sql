-- kanji=����
-- $Id: 89be2143b64a73c94a22565a87ad1dcdae20f0c0 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
drop table SCHREG_BASE_HIST_DAT_OLD

create table SCHREG_BASE_HIST_DAT_OLD like SCHREG_BASE_HIST_DAT

insert into SCHREG_BASE_HIST_DAT_OLD select * from SCHREG_BASE_HIST_DAT

drop table SCHREG_BASE_HIST_DAT

create table SCHREG_BASE_HIST_DAT \
(  \
    SCHREGNO                        varchar(8)  not null, \
    ISSUEDATE                       date        not null, \
    EXPIREDATE                      date, \
    YEAR                            varchar(4), \
    SEMESTER                        varchar(1), \
    GRADE                           varchar(2), \
    HR_CLASS                        varchar(3), \
    ATTENDNO                        varchar(3), \
    ANNUAL                          varchar(2), \
    COURSECD                        varchar(1), \
    MAJORCD                         varchar(3), \
    COURSECODE                      varchar(4), \
    NAME                            varchar(120), \
    NAME_SHOW                       varchar(120), \
    NAME_KANA                       varchar(240), \
    NAME_ENG                        varchar(40), \
    REAL_NAME                       varchar(120), \
    REAL_NAME_KANA                  varchar(240), \
    HANDICAP                        varchar(3), \
    NATIONALITY2                    varchar(3), \
    NATIONALITY_NAME                varchar(120), \
    NATIONALITY_NAME_KANA           varchar(240), \
    NATIONALITY_NAME_ENG            varchar(40), \
    NATIONALITY_REAL_NAME           varchar(120), \
    NATIONALITY_REAL_NAME_KANA      varchar(240), \
    GRADE_FLG                       varchar(1), \
    HR_CLASS_FLG                    varchar(1), \
    ATTENDNO_FLG                    varchar(1), \
    ANNUAL_FLG                      varchar(1), \
    COURSECD_FLG                    varchar(1), \
    MAJORCD_FLG                     varchar(1), \
    COURSECODE_FLG                  varchar(1), \
    NAME_FLG                        varchar(1), \
    NAME_SHOW_FLG                   varchar(1), \
    NAME_KANA_FLG                   varchar(1), \
    NAME_ENG_FLG                    varchar(1), \
    REAL_NAME_FLG                   varchar(1), \
    REAL_NAME_KANA_FLG              varchar(1), \
    HANDICAP_FLG                    varchar(1), \
    NATIONALITY2_FLG                varchar(1), \
    NATIONALITY_NAME_FLG            varchar(1), \
    NATIONALITY_NAME_KANA_FLG       varchar(1), \
    NATIONALITY_NAME_ENG_FLG        varchar(1), \
    NATIONALITY_REAL_NAME_FLG       varchar(1), \
    NATIONALITY_REAL_NAME_KANA_FLG  varchar(1), \
    REGISTERCD                      varchar(10), \
    UPDATED                         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_BASE_HIST_DAT add constraint PK_SCH_BASE_HIST primary key \
    (SCHREGNO, ISSUEDATE)

insert into SCHREG_BASE_HIST_DAT \
    select \
        SCHREGNO, \
        ISSUEDATE, \
        EXPIREDATE, \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        ATTENDNO, \
        ANNUAL, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        NAME, \
        NAME_SHOW, \
        NAME_KANA, \
        NAME_ENG, \
        REAL_NAME, \
        REAL_NAME_KANA, \
        cast(null as varchar(3)) AS HANDICAP, \
        NATIONALITY2, \
        NATIONALITY_NAME, \
        NATIONALITY_NAME_KANA, \
        NATIONALITY_NAME_ENG, \
        NATIONALITY_REAL_NAME, \
        NATIONALITY_REAL_NAME_KANA, \
        GRADE_FLG, \
        HR_CLASS_FLG, \
        ATTENDNO_FLG, \
        ANNUAL_FLG, \
        COURSECD_FLG, \
        MAJORCD_FLG, \
        COURSECODE_FLG, \
        NAME_FLG, \
        NAME_SHOW_FLG, \
        NAME_KANA_FLG, \
        NAME_ENG_FLG, \
        REAL_NAME_FLG, \
        REAL_NAME_KANA_FLG, \
        cast(null as varchar(1)) AS HANDICAP_FLG, \
        NATIONALITY2_FLG, \
        NATIONALITY_NAME_FLG, \
        NATIONALITY_NAME_KANA_FLG, \
        NATIONALITY_NAME_ENG_FLG, \
        NATIONALITY_REAL_NAME_FLG, \
        NATIONALITY_REAL_NAME_KANA_FLG, \
        REGISTERCD, \
        UPDATED \
    from \
        SCHREG_BASE_HIST_DAT_OLD T1

