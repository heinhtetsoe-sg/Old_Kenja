-- kanji=漢字
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table AFT_RECOMMENDATION_RANK_HEAD_DAT

create table AFT_RECOMMENDATION_RANK_HEAD_DAT ( \
    YEAR                   varchar(4) not null, \
    GRADE                  varchar(2) not null, \
    TENTATIVE_FLG          varchar(1), \
    PERCENTAGE             smallint, \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_RANK_HEAD_DAT add constraint PK_AFT_RECOMMENDATION_RANK_HEAD_DAT primary key (YEAR, GRADE)
