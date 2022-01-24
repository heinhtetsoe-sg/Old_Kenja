-- kanji=漢字
-- $Id: levy_request_outgo_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金支出伺いデータ

drop table LEVY_REQUEST_OUTGO_DAT

create table LEVY_REQUEST_OUTGO_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "INCOME_L_CD"           varchar(2)  not null, \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "TRADER_CD"             varchar(8), \
        "TRADER_NAME"           varchar(120), \
        "BANKCD"                varchar(4), \
        "BRANCHCD"              varchar(3), \
        "BANK_DEPOSIT_ITEM"     varchar(1), \
        "BANK_ACCOUNTNO"        varchar(7), \
        "ACCOUNTNAME"           varchar(120), \
        "ACCOUNTNAME_KANA"      varchar(120), \
        "PAY_DIV"               varchar(2), \
        "OUTGO_CHECK1"          varchar(1), \
        "OUTGO_CHECK1_DATE"     date, \
        "OUTGO_CHECK1_STAFF"    varchar(10), \
        "OUTGO_CHECK2"          varchar(1), \
        "OUTGO_CHECK3"          varchar(1), \
        "OUTGO_DATE"            date, \
        "OUTGO_EXPENSE_FLG"     varchar(1), \
        "OUTGO_CERTIFICATE_CNT" smallint, \
        "OUTGO_CANCEL"          varchar(1), \
        "OUTGO_APPROVAL"        varchar(1), \
        "KOUNYU_NO"             varchar(10), \
        "SEKOU_NO"              varchar(10), \
        "SEISAN_NO"             varchar(10), \
        "HENKIN_FLG"            varchar(1), \
        "HENKIN_APPROVAL"       varchar(1), \
        "HENKIN_DATE"           date, \
        "COLLECT_GRP_CD"        varchar(4), \
        "COLLECT_L_CD"          varchar(2), \
        "COLLECT_M_CD"          varchar(2), \
        "COLLECT_S_CD"          varchar(2), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_OUTGO_DAT add constraint PK_LEVY_REQ_OUT primary key (YEAR, OUTGO_L_CD, OUTGO_M_CD, REQUEST_NO)
