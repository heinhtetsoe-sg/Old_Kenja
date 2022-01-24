-- kanji=漢字
-- $Id: a623c4bcb6c7a4bcbe321c9355feeb47b5f80d7a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_PLAN_LIMITDATE_DAT_OLD

create table COLLECT_SLIP_PLAN_LIMITDATE_DAT_OLD like COLLECT_SLIP_PLAN_LIMITDATE_DAT

insert into COLLECT_SLIP_PLAN_LIMITDATE_DAT_OLD select * from COLLECT_SLIP_PLAN_LIMITDATE_DAT

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
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_PLAN_LIMITDATE_DAT \
add constraint PK_SLIP_PLAN_LI_D \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO, SLIP_NO, PLAN_YEAR, PLAN_MONTH)

INSERT INTO COLLECT_SLIP_PLAN_LIMITDATE_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    SCHREGNO, \
    SLIP_NO, \
    PLAN_YEAR, \
    PLAN_MONTH, \
    SUBSTR(CAST(PAID_LIMIT_DATE AS VARCHAR(10)), 6, 2) AS PAID_LIMIT_MONTH, \
    PAID_LIMIT_DATE, \
    REGISTERCD, \
    UPDATED \
FROM COLLECT_SLIP_PLAN_LIMITDATE_DAT_OLD
