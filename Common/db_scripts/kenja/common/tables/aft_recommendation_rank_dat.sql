-- kanji=漢字
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table AFT_SCHREG_RECOMMENDATION_RANK_DAT

create table AFT_SCHREG_RECOMMENDATION_RANK_DAT ( \
    YEAR                           varchar(4) not null, \
    SCHREGNO                       varchar(8) not null, \
    GRADE                          varchar(2) not null, \
    MOCK_NATIONAL_LANGUAGE_AVG     decimal(7,3), \
    MOCK_MATH_AVG                  decimal(7,3), \
    MOCK_ENGLISH_AVG               decimal(7,3), \
    MOCK_TOTAL_SCORE               smallint, \
    MOCK_TOTAL_AVG                 decimal(7,3), \
    MOCK_TOTAL_PERCENT_SCORE       smallint, \
    TEST_VALUATION_AVG             decimal(7,3), \
    TEST_VALUATION_AVG_PERCENT     decimal(7,3), \
    TEST_VALUATION_PERCENT_SCORE   decimal(7,3), \
    MOCK_TOTAL_SCORE_GRADE_RANK    smallint, \
    MOCK_TOTAL_SCORE_CLASS_RANK    smallint, \
    MOCK_TOTAL_SCORE_COURSE_RANK   smallint, \
    REGISTERCD                     varchar(10), \
    UPDATED                        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_SCHREG_RECOMMENDATION_RANK_DAT add constraint PK_AFT_SCHREG_RECOMMENDATION_RANK_DAT primary key (YEAR, SCHREGNO)
