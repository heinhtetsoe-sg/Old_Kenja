-- kanji=漢字
-- $Id: levy_request_kounyu_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金購入伺いデータ

drop table LEVY_REQUEST_KOUNYU_DAT

create table LEVY_REQUEST_KOUNYU_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "KOUNYU_L_CD"           varchar(2)  not null, \
        "KOUNYU_M_CD"           varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "TRADER_CD1"            varchar(8), \
        "TRADER_NAME1"          varchar(120), \
        "TRADER_KAKUTEI1"       varchar(1), \
        "TRADER_CD2"            varchar(8), \
        "TRADER_NAME2"          varchar(120), \
        "TRADER_KAKUTEI2"       varchar(1), \
        "TRADER_CD3"            varchar(8), \
        "TRADER_NAME3"          varchar(120), \
        "TRADER_KAKUTEI3"       varchar(1), \
        "TRADER_CD4"            varchar(8), \
        "TRADER_NAME4"          varchar(120), \
        "TRADER_KAKUTEI4"       varchar(1), \
        "KOUNYU_MITUMORI_DATE"  date, \
        "KEIYAKU_HOUHOU"        varchar(120), \
        "NOUNYU_LIMIT_DATE"     date, \
        "NOUNYU_PLACE"          varchar(120), \
        "REMARK"                varchar(120), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_KOUNYU_DAT add constraint PK_LEVY_KOUNYU primary key (YEAR, KOUNYU_L_CD, KOUNYU_M_CD, REQUEST_NO)
