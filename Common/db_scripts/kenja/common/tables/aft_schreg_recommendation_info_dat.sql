-- kanji=漢字
-- $Id: a97bb0a274c3592f731cdb18262ba1c60cc33a07 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table AFT_SCHREG_RECOMMENDATION_INFO_DAT

create table AFT_SCHREG_RECOMMENDATION_INFO_DAT ( \
    YEAR                   varchar(4) not null, \
    SCHREGNO               varchar(8) not null, \
    ACTIVITY_CD            varchar(2), \
    ACTIVITY_CONTENT       varchar(600), \
    DECLINE_FLG            varchar(1), \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_SCHREG_RECOMMENDATION_INFO_DAT add constraint PK_AFT_SCHREG_RECOMMENDATION_INFO_DAT primary key (YEAR, SCHREGNO)
