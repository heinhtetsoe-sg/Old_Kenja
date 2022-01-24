-- kanji=漢字
-- $Id: 84c448038f5e91f781d692e64312ff5da43d40d0 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table STATION_YDAT

create table STATION_YDAT ( \
    YEAR        varchar(4) not null, \
    STATIONCD   varchar(7) not null, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( YEAR,STATIONCD ) \
) in usr1dms index in idx1dms

