-- kanji=漢字
-- $Id: 0539b87c10521523c0b5609230f99f7e572faf0b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table STATION_NETMST

create table STATION_NETMST \
(  \
    RR_CD          varchar(2) not null, \
    LINE_CD        varchar(5) not null, \
    STATION_CD     varchar(7) not null, \
    LINE_SORT      varchar(5), \
    STATION_SORT   varchar(7), \
    STATION_G_CD   varchar(7), \
    R_TYPE         varchar(1), \
    RR_NAME        varchar(96), \
    LINE_NAME      varchar(192), \
    STATION_NAME   varchar(192), \
    PREF_CD        varchar(2), \
    LON            decimal(9,6), \
    LAT            decimal(9,6), \
    F_FLAG         varchar(1), \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STATION_NETMST add constraint PK_STATION_NETMST \
primary key (STATION_CD)
