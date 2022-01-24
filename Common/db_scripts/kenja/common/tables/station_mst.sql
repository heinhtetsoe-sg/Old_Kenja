-- kanji=漢字
-- $Id: 98bf26a56e31e60c72b62861f4d3711a477376dc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table STATION_MST

create table STATION_MST \
(  \
    RR_CD          smallint not null, \
    LINE_CD        INT      not null, \
    STATION_CD     INT      not null, \
    LINE_SORT      INT, \
    STATION_SORT   INT, \
    STATION_G_CD   INT, \
    R_TYPE         smallint, \
    RR_NAME        varchar(96), \
    LINE_NAME      varchar(192), \
    STATION_NAME   varchar(192), \
    PREF_CD        smallint, \
    LON            float, \
    LAT            float, \
    F_FLAG         varchar(1), \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STATION_MST add constraint PK_STATION_MST \
primary key (STATION_CD)
