-- kanji=漢字
-- $Id: sales_plan_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SALES_PLAN_DAT

create table SALES_PLAN_DAT \
(  \
        "YEAR"                  varchar(4) not null, \
        "APPLICANTNO"           varchar(7) not null, \
        "PLAN_YEAR"             varchar(4) not null, \
        "PLAN_MONTH"            varchar(2) not null, \
        "SLIP_NO"               varchar(8) not null, \
        "SEQ"                   varchar(2) not null, \
        "COMMODITY_CD"          varchar(5) not null, \
        "TOTAL_CLAIM_MONEY"     integer, \
        "PRICE"                 integer, \
        "TAX"                   integer, \
        "KEEPING_MONEY"         integer, \
        "KEEPING_DATE"          date, \
        "SUMMING_UP_MONEY"      integer, \
        "SUMMING_UP_DATE"       date, \
        "REGISTERCD"            varchar(8), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SALES_PLAN_DAT  \
add constraint PK_SALES_PLAN_DAT \
primary key  \
(YEAR, APPLICANTNO, PLAN_YEAR, PLAN_MONTH, SLIP_NO, SEQ)
