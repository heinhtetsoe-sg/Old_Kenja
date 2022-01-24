-- kanji=漢字
-- $Id: 07e53feebce54335efdf0da849e43d6e6779ed14 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金支出伺い明細データ

DROP TABLE LEVY_REQUEST_OUTGO_MEISAI_DAT

create table LEVY_REQUEST_OUTGO_MEISAI_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "LINE_NO"               smallint    not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "OUTGO_S_CD"            varchar(2)  not null, \
        "COMMODITY_PRICE"       integer, \
        "COMMODITY_CNT"         integer, \
        "TOTAL_PRICE_ZEINUKI"   integer, \
        "TOTAL_TAX"             integer, \
        "TOTAL_PRICE"           integer, \
        "SCH_PRICE"             integer, \
        "SCH_CNT"               integer, \
        "HASUU"                 integer, \
        "WARIHURI_DIV"          varchar(1) not null, \
        "TRADER_SEIKYU_NO"      varchar(10), \
        "SEIKYU_MONTH"          varchar(2), \
        "REMARK"                varchar(120), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_OUTGO_MEISAI_DAT add constraint PK_LEVY_OUTGO_M PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR, REQUEST_NO, LINE_NO, OUTGO_L_CD, OUTGO_M_CD, OUTGO_S_CD)
