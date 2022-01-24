-- kanji=漢字
-- $Id: ba9bc7e2d0276b22d0fc2643ae199909eb807283 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table AFT_RECOMMENDATION_ASSESS_AVG_DAT

create table AFT_RECOMMENDATION_ASSESS_AVG_DAT ( \
    YEAR                   varchar(4) not null, \
    RECOMMENDATION_CD      varchar(4) not null, \
    SEQ                    smallint not null, \
    ASSESS_AVG             decimal(5, 2) not null, \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_ASSESS_AVG_DAT add constraint PK_AFT_RECOMMENDATION_ASSESS_AVG_DAT \
primary key (YEAR, RECOMMENDATION_CD, SEQ)
