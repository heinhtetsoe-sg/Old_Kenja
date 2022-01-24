-- kanji=漢字
-- $Id: 5aa50aa8219442611d45c8f3bad377a278a5e922 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_PLAN_PAID_M_DAT

create table COLLECT_SLIP_PLAN_PAID_M_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SLIP_NO"               varchar(15) not null, \
        "COLLECT_L_CD"          varchar(2)  not null, \
        "COLLECT_M_CD"          varchar(2)  not null, \
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

alter table COLLECT_SLIP_PLAN_PAID_M_DAT \
add constraint PK_COLL_SL_PP_MDA \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, COLLECT_L_CD, COLLECT_M_CD, PLAN_YEAR, PLAN_MONTH, SEQ)
