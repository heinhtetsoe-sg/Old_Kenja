-- kanji=漢字
-- $Id: ae4c428b5453bac9b65ca818491949af912e9d77 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--全銀協フォーマット（エンド・レコード）

drop table COLLECT_ZENGIN_END_RECORD_DAT

create table COLLECT_ZENGIN_END_RECORD_DAT \
( \
        "YEAR"              varchar(4) not null, \
        "DIRECT_DEBIT"      varchar(4) not null, \
        "DATA_DIV"          varchar(1)  , \
        "DUMMY"             varchar(119), \
        "REGISTERCD"        varchar(10) , \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_END_RECORD_DAT \
add constraint PK_C_ZEN_END_RC_D \
primary key \
(YEAR, DIRECT_DEBIT)
