-- kanji=漢字
-- $Id: 81b832ebe7ec4807fc434cfb04b78ad185dce946 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

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
