-- kanji=漢字
-- $Id: 759fdcca8e8b3ba55cdffbc5dccb8b7a36faa984 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--請求書発行履歴データ
drop table COLLECT_SLIP_PRINT_HIST_DAT

create table COLLECT_SLIP_PRINT_HIST_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4) not null, \
        "CLAIM_NO"              varchar(10) not null, \
        "CLAIM_EDA"             varchar(2) not null, \
        "SLIP_NO"               varchar(10) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "CLAIM_DIV"             varchar(1), \
        "CLAIM_DATE"            date, \
        "CLAIM_STAFFCD"         varchar(10), \
        "CLAIM_MONEY"           integer, \
        "PAY_DIV"               varchar(1), \
        "LIMIT_DATE"            date, \
        "CLAIM_NONE_FLG"        varchar(1), \
        "CLAIM_NONE_REASON"     varchar(90), \
        "REMARK"                varchar(90), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_PRINT_HIST_DAT \
add constraint PK_SLIP_PRINT \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, CLAIM_NO, CLAIM_EDA)
