-- kanji=漢字
-- $Id: 30e2480a3408c78a756cb5a520f701fe1d450966 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_PLAN_PAID_DAT

create table COLLECT_SLIP_PLAN_PAID_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SLIP_NO"               varchar(15) not null, \
        "PLAN_YEAR"             varchar(4)  not null, \
        "PLAN_MONTH"            varchar(2)  not null, \
        "SEQ"                   varchar(2)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "PLAN_PAID_MONEY_DATE"  date, \
        "PLAN_PAID_MONEY"       integer, \
        "PLAN_PAID_MONEY_DIV"   varchar(1), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_PLAN_PAID_DAT \
add constraint PK_COLL_SL_PP_DAT \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, PLAN_YEAR, PLAN_MONTH, SEQ)


ALTER TABLE COLLECT_SLIP_PLAN_PAID_DAT ADD COLUMN SGL_OUTPUT_FLG VARCHAR(1)

reorg table COLLECT_SLIP_PLAN_PAID_DAT
