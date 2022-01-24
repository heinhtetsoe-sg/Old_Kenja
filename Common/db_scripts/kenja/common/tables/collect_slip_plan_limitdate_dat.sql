-- kanji=漢字
-- $Id: e78299bab1f393734bb8044e2b993c3f3ff8226b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_PLAN_LIMITDATE_DAT

create table COLLECT_SLIP_PLAN_LIMITDATE_DAT \
( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SCHREGNO                varchar(8)  not null, \
    SLIP_NO                 varchar(15) not null, \
    PLAN_YEAR               varchar(4)  not null, \
    PLAN_MONTH              varchar(2)  not null, \
    PAID_LIMIT_MONTH        varchar(2), \
    PAID_LIMIT_DATE         date, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp, \
    PAID_LIMIT_MONTH_CALC   varchar(2), \
    PAID_LIMIT_DATE_CALC    date \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_PLAN_LIMITDATE_DAT \
add constraint PK_SLIP_PLAN_LI_D \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO, SLIP_NO, PLAN_YEAR, PLAN_MONTH)
