-- kanji=漢字
-- $Id: 61626ba64e1a69de011501dba8c46a7fe1bdcdf5 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table SCHREG_DETAILHIST_DAT

create table SCHREG_DETAILHIST_DAT ( \
    YEAR                   varchar(4) not null, \
    SCHREGNO               varchar(8) not null, \
    DETAIL_DIV             varchar(1) not null, \
    DETAIL_SDATE           date       not null, \
    DETAIL_EDATE           date, \
    DETAILCD               varchar(2), \
    CONTENT                varchar(1200), \
    REMARK                 varchar(210), \
    BICYCLE_CD             varchar(8), \
    BICYCLE_NO             varchar(4), \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp, \
    OCCURRENCE_DATE        date, \
    INVESTIGATION_DATE     date, \
    STD_GUID_MTG_DATE      date, \
    ORIGINAL_PLAN_CD       varchar(2), \
    STAFF_MTG_DATE         date, \
    PUNISH_CD              varchar(2), \
    OCCURRENCE_PLACE       varchar(90), \
    DIARY_FLG              varchar(1), \
    WRITTEN_OATH_FLG       varchar(1), \
    REPORT_FLG             varchar(1), \
    WRITTEN_STAFFCD        varchar(10), \
    INVESTIGATION_STAFFCD1 varchar(10), \
    INVESTIGATION_STAFFCD2 varchar(10), \
    INVESTIGATION_STAFFCD3 varchar(10), \
    INVESTIGATION_STAFFCD4 varchar(10) \
    primary key ( YEAR,SCHREGNO,DETAIL_DIV,DETAIL_SDATE ) \
) in usr1dms index in idx1dms

