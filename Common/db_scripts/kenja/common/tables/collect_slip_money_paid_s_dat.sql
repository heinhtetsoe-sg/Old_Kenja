-- kanji=漢字
-- $Id: 913010149a5804e97006c73b296f250f297114bb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定小分類データ
drop table COLLECT_SLIP_MONEY_PAID_S_DAT

create table COLLECT_SLIP_MONEY_PAID_S_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4) not null, \
        "SLIP_NO"               varchar(10) not null, \
        "MSEQ"                  varchar(2) not null, \
        "SSEQ"                  varchar(2) not null, \
        "SCHREGNO"              varchar(8) not null, \
        "COLLECT_L_CD"          varchar(2) not null, \
        "COLLECT_M_CD"          varchar(2) not null, \
        "COLLECT_S_CD"          varchar(2) not null, \
        "PAID_MONEY_DATE"       date, \
        "PAID_MONEY"            integer, \
        "PAID_MONEY_DIV"        varchar(1), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_MONEY_PAID_S_DAT \
add constraint PK_COLLECT_SLIP_SP \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, MSEQ, SSEQ)
