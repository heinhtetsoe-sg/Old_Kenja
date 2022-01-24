-- kanji=漢字
-- $Id: 09a54e8d295bd4ab8e1b4e6e731e8d60a642de14 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金購入伺い明細データ

drop table LEVY_REQUEST_KOUNYU_MEISAI_DAT

create table LEVY_REQUEST_KOUNYU_MEISAI_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "LINE_NO"               smallint    not null, \
        "KOUNYU_L_CD"           varchar(2)  not null, \
        "KOUNYU_M_CD"           varchar(2)  not null, \
        "KOUNYU_S_CD"           varchar(2)  not null, \
        "COMMODITY_PRICE"       integer, \
        "COMMODITY_CNT"         integer, \
        "TOTAL_PRICE_ZEINUKI"   integer, \
        "TOTAL_TAX"             integer, \
        "TOTAL_PRICE"           integer, \
        "REMARK"                varchar(120), \
        "SCH_PRICE"             integer, \
        "SCH_CNT"               integer, \
        "HASUU"                 integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_KOUNYU_MEISAI_DAT add constraint PK_LEVY_KOUNYU_M primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REQUEST_NO, LINE_NO, KOUNYU_L_CD, KOUNYU_M_CD, KOUNYU_S_CD)
