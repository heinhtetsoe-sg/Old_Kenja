-- kanji=漢字
-- $Id: 009b8afdfa032cedebda142749191a7797962e08 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
-- 振込予定データ
drop table COLLECT_SLIP_PLAN_CSV_DAT

create table COLLECT_SLIP_PLAN_CSV_DAT \
( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SCHREGNO                varchar(8)  not null, \
    PLAN_YEAR               varchar(4)  not null, \
    PLAN_MONTH              varchar(2)  not null, \
    PAY_DIV                 varchar(1)  not null, \
    PLAN_MONEY              integer, \
    PAID_MONEY              integer, \
    PAID_YEARMONTH          varchar(6), \
    PAID_FLG                varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_PLAN_CSV_DAT \
add constraint PK_SLIP_PLAN_CSV \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO, PLAN_YEAR, PLAN_MONTH, PAY_DIV)
