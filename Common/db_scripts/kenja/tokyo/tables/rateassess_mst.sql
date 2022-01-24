-- kanji=漢字
-- $Id: rateassess_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $
-- 評定算出変換マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table RATEASSESS_MST

create table RATEASSESS_MST ( \
    YEAR               varchar(4) not null, \
    ASSESSLEVEL        smallint   not null, \
    RATE               smallint, \
    ASSESSLEVEL5       smallint, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp, \
    primary key ( YEAR,ASSESSLEVEL ) \
) in usr1dms index in idx1dms

