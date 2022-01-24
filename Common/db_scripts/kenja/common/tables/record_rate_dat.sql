-- kanji=漢字
-- $Id: 8733eead325057296f401a2e9425a473720b9bec $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table RECORD_RATE_DAT

create table RECORD_RATE_DAT ( \
    YEAR               varchar(4) not null, \
    SUBCLASSCD         varchar(6) not null, \
    RATE               smallint, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp \
) in usr1dms index in idx1dms

alter table RECORD_RATE_DAT add constraint PK_RECORD_RATE_DAT \
        primary key (YEAR, SUBCLASSCD)
