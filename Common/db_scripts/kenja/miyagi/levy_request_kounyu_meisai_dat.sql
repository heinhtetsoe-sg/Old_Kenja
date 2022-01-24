-- kanji=漢字
-- $Id: levy_request_kounyu_meisai_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金購入伺い明細データ

drop table LEVY_REQUEST_KOUNYU_MEISAI_DAT

create table LEVY_REQUEST_KOUNYU_MEISAI_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "KOUNYU_L_CD"           varchar(2)  not null, \
        "KOUNYU_M_CD"           varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "KOUNYU_S_CD"           varchar(2)  not null, \
        "LINE_NO"               smallint    not null, \
        "COMMODITY_PRICE"       integer, \
        "COMMODITY_CNT"         integer, \
        "TOTAL_PRICE_ZEINUKI"   integer, \
        "TOTAL_TAX"             integer, \
        "TOTAL_PRICE"           integer, \
        "REMARK"                varchar(120), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_KOUNYU_MEISAI_DAT add constraint PK_LEVY_KOUNYU_M primary key (YEAR, KOUNYU_L_CD, KOUNYU_M_CD, REQUEST_NO, KOUNYU_S_CD)
