-- kanji=漢字
-- $Id: e446fadf2c9dd1386f9f530c0f9d67e963ea6ff3 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--全銀協フォーマット（トレーラ・レコード）

drop table COLLECT_ZENGIN_TRAILER_RECORD_DAT

create table COLLECT_ZENGIN_TRAILER_RECORD_DAT \
( \
        "YEAR"                  varchar(4) not null, \
        "DIRECT_DEBIT"          varchar(4) not null, \
        "DATA_DIV"              varchar(1) , \
        "TOTAL_CNT"             varchar(6) , \
        "TOTAL_MONEY"           varchar(12), \
        "TRANSFER_CNT"          varchar(6) , \
        "TRANSFER_MONEY"        varchar(12), \
        "NOT_TRANSFER_CNT"      varchar(6) , \
        "NOT_TRANSFER_MONEY"    varchar(12), \
        "DUMMY"                 varchar(65), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_TRAILER_RECORD_DAT \
add constraint PK_C_ZEN_TRA_RC_D \
primary key \
(YEAR, DIRECT_DEBIT)
