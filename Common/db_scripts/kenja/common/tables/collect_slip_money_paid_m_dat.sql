-- kanji=漢字
-- $Id: 157c7aaf4cacc54473b73b94e42f7d6c83d4cfe7 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_MONEY_PAID_M_DAT

create table COLLECT_SLIP_MONEY_PAID_M_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4) not null, \
        "SLIP_NO"               varchar(15) not null, \
        "SEQ"                   varchar(2) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "COLLECT_L_CD"          varchar(2) not null, \
        "COLLECT_M_CD"          varchar(2) not null, \
        "PAID_MONEY_DATE"       date, \
        "PAID_MONEY"            integer, \
        "PAID_MONEY_DIV"        varchar(1), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_MONEY_PAID_M_DAT \
add constraint PK_COLLECT_SLIP_MP \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, SEQ)
