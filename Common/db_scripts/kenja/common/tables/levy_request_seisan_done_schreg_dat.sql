-- kanji=漢字
-- $Id: c78dc4ea2780f417ffd4f28727a18ad96baad2e0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金清算書データ

drop table LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT

create table LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT \
( \
        "SCHREGNO"      varchar(8) not null, \
        "INCOME_MONEY"  integer, \
        "OUTGO_MONEY"   integer, \
        "CARRYOVER"     integer, \
        "KYUFU_MONEY"   integer, \
        "HASUU_HENKIN"  integer, \
        "HENKIN"        integer, \
        "ZANDAKA"       integer, \
        "REGISTERCD"    varchar(10), \
        "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT add constraint PK_SEISAN_DONE primary key (SCHREGNO)
