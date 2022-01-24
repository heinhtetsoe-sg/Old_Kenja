-- kanji=漢字
-- $Id: commodity_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table COMMODITY_MST

create table COMMODITY_MST \
(  \
        "COMMODITY_CD"                  varchar(5) not null, \
        "S_YEAR_MONTH"                  varchar(6), \
        "E_YEAR_MONTH"                  varchar(6), \
        "COMMODITY_NAME"                varchar(150) not null, \
        "COMMODITY_ABBV"                varchar(60) not null, \
        "ITEM_CD"                       varchar(10), \
        "CALCULATION_SUB_CD"            varchar(10), \
        "ASSISTANCE_SUB_CD"             varchar(10), \
        "INCLUDING_TAX_PRICE"           integer, \
        "PRICE"                         integer, \
        "TAX"                           integer, \
        "TAX_FLG"                       varchar(1), \
        "TAX_PERCENT"                   decimal (4,1), \
        "SALES_MONTH"                   varchar(2), \
        "DIVIDING_MULTIPLICATION_DIV"   varchar(1), \
        "FRACTION_DIV"                  varchar(1), \
        "TUITION_DIV"                   varchar(1), \
        "SALES_DIV"                     varchar(1), \
        "REGISTERCD"                    varchar(8), \
        "UPDATED"                       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMMODITY_MST \
add constraint PK_COMMODITY_MST \
primary key  \
(COMMODITY_CD)
