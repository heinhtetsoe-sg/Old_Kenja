-- kanji=漢字
-- $Id: 073bad8157133111613a7b53d6e542354acea5b9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定小分類データ
drop table COLLECT_SLIP_MONEY_DUE_S_DAT

create table COLLECT_SLIP_MONEY_DUE_S_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4) not null, \
        "SLIP_NO"               varchar(10) not null, \
        "MSEQ"                  varchar(2) not null, \
        "SSEQ"                  varchar(2) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "COLLECT_L_CD"          varchar(2) not null, \
        "COLLECT_M_CD"          varchar(2) not null, \
        "COLLECT_S_CD"          varchar(2) not null, \
        "MONEY_DUE"             integer, \
        "COLLECT_CNT"           integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_MONEY_DUE_S_DAT \
add constraint PK_COLLECT_SLIP_SD \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, MSEQ, SSEQ)
