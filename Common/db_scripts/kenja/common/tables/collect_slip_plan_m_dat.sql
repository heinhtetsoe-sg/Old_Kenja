-- kanji=漢字
-- $Id: 7afa7591287d32e838253de51b33f2304a97fab9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_PLAN_M_DAT

create table COLLECT_SLIP_PLAN_M_DAT \
( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SCHREGNO                varchar(8)  not null, \
    SLIP_NO                 varchar(15) not null, \
    COLLECT_L_CD            varchar(2)  not null, \
    COLLECT_M_CD            varchar(2)  not null, \
    PLAN_YEAR               varchar(4)  not null, \
    PLAN_MONTH              varchar(2)  not null, \
    PLAN_MONEY              integer, \
    REDUCTION_MONEY         integer, \
    REDUCTION_COUNTRY_MONEY integer, \
    REDUCTION_SCHOOL_MONEY  integer, \
    DECISON_MONEY           integer, \
    PLAN_CANCEL_FLG         varchar(1), \
    PAID_MONEY              integer, \
    PAID_YEARMONTH          varchar(6), \
    PLAN_LOCK_FLG           varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_PLAN_M_DAT \
add constraint PK_SLIP_PLAN_M \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO, SLIP_NO, COLLECT_L_CD, COLLECT_M_CD, PLAN_YEAR, PLAN_MONTH)
