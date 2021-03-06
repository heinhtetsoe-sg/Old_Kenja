-- kanji=漢字
-- $Id: b673df4909c00aa61554d5df967275057d4dd1f2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金収入伺い明細データ

drop table LEVY_REQUEST_INCOME_MEISAI_DAT

create table LEVY_REQUEST_INCOME_MEISAI_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "INCOME_L_CD"           varchar(2)  not null, \
        "INCOME_M_CD"           varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "INCOME_S_CD"           varchar(3)  not null, \
        "LINE_NO"               smallint    not null, \
        "COMMODITY_NAME"        varchar(120), \
        "COMMODITY_PRICE"       integer, \
        "COMMODITY_CNT"         integer, \
        "TOTAL_PRICE"           integer, \
        "WARIHURI_DIV"          varchar(1)  not null, \
        "REMARK"                varchar(120), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_MEISAI_DAT add constraint PK_LEVY_REQIN_ME primary key (SCHOOLCD, SCHOOL_KIND, YEAR, INCOME_L_CD, INCOME_M_CD, REQUEST_NO, INCOME_S_CD)
