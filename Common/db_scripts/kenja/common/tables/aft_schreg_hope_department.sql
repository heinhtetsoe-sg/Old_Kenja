-- kanji=漢字
-- $Id: 84b52bdf1ebc6938d4e2e09c4b4fed5165dfc439 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table AFT_SCHREG_HOPE_DEPARTMENT

create table AFT_SCHREG_HOPE_DEPARTMENT ( \
    YEAR                     varchar(4) not null, \
    SCHREGNO                 varchar(8) not null, \
    HOPE_ORDER               varchar(2) not null, \
    DEPARTMENT_CD            varchar(2), \
    RECOMMENDATION_BASE_DIV  varchar(3), \
    DEPARTMENT_BASE_DIV      varchar(1), \
    REGISTERCD               varchar(10), \
    UPDATED                  timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_SCHREG_HOPE_DEPARTMENT add constraint PK_AFT_SCHREG_HOPE_DEPARTMENT primary key (YEAR, SCHREGNO, HOPE_ORDER)
