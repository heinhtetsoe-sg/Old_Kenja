-- kanji=漢字
-- $Id: w_rateassesshist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
-- 評定算出履歴データ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table W_RATEASSESSHIST_DAT

create table W_RATEASSESSHIST_DAT ( \
    YEAR               varchar(4) not null, \
    SEMESTER           varchar(1) not null, \
    GRADE              varchar(2) not null, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp, \
    primary key ( YEAR,SEMESTER,GRADE ) \
) in usr1dms index in idx1dms

