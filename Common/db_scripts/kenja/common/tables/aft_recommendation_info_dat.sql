-- kanji=漢字
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table AFT_RECOMMENDATION_INFO_DAT

create table AFT_RECOMMENDATION_INFO_DAT ( \
    YEAR                           varchar(4) not null, \
    SCHREGNO                       varchar(8) not null, \
    SCORE_JUDGE                    varchar(4), \
    ATTEND_STATUS_JUDGE            varchar(4), \
    INTERDISCIPLINARY_JUDGE        varchar(4), \
    CLUB_OTHER_JUDGE               varchar(4), \
    SCHOOL_LIFE_JUDGE              varchar(4), \
    GRADE_GROUP_JUDGE              varchar(4), \
    BIBLE_JUDGE                    varchar(4), \
    READING_JUDGE                  varchar(4), \
    CLASS_STF_JUDGE1               smallint, \
    CLASS_STF_JUDGE2               varchar(4), \
    CLASS_STF_JUDGE_TOTAL          varchar(4), \
    JUDGE_TOTAL_POINT              smallint, \
    ALL_GRADE_TOTAL_SCORE          integer, \
    ALL_GRADE_TOTAL_SCORE_ORDER    smallint, \
    HOPE_CD1                       varchar(4), \
    HOPE_CONFIRM_FLG1              varchar(1), \
    HOPE_CD2                       varchar(4), \
    HOPE_CONFIRM_FLG2              varchar(1), \
    HOPE_CD3                       varchar(4), \
    HOPE_CONFIRM_FLG3              varchar(1), \
    DONT_RECOMMENDATION_FLG        varchar(1), \
    REGISTERCD                     varchar(10), \
    UPDATED                        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_INFO_DAT add constraint PK_AFT_RECOMMENDATION_INFO_DAT primary key (YEAR, SCHREGNO)
