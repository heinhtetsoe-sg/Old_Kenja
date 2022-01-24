-- kanji=漢字
-- $Id: rep-reduction_max_money_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table REDUCTION_MAX_MONEY_DAT_OLD
create table REDUCTION_MAX_MONEY_DAT_OLD like REDUCTION_MAX_MONEY_DAT
insert into REDUCTION_MAX_MONEY_DAT_OLD select * from REDUCTION_MAX_MONEY_DAT

drop table REDUCTION_MAX_MONEY_DAT

create table REDUCTION_MAX_MONEY_DAT ( \
    YEAR               varchar(4) not null, \
    PREFECTURESCD      varchar(2) not null, \
    GRADE              varchar(2) not null, \
    RANK_DIV           varchar(2) not null, \
    REDUCTIONMONEY_1   integer, \
    REDUCTIONMONEY_2   integer, \
    MAX_MONEY          integer, \
    MIN_MONEY_1        integer, \
    MIN_MONEY_2        integer, \
    MIN_MONEY          integer, \
    PARENTS_MONEY_1    integer, \
    PARENTS_MONEY_2    integer, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_MAX_MONEY_DAT add constraint PK_REDUC_MAX_DAT primary key (YEAR, PREFECTURESCD, GRADE, RANK_DIV)

insert into REDUCTION_MAX_MONEY_DAT \
( \
select \
    t1.YEAR, \
    t1.PREFECTURESCD, \
    t1.GRADE, \
    l1.namecd2, \
    t1.REDUCTIONMONEY_1, \
    t1.REDUCTIONMONEY_2, \
    t1.MAX_MONEY, \
    t1.REDUCTIONMONEY_1, \
    t1.REDUCTIONMONEY_2, \
    t1.MAX_MONEY, \
    0, \
    0, \
    t1.REGISTERCD, \
    t1.UPDATED \
from \
    REDUCTION_MAX_MONEY_DAT_OLD t1 \
    left join name_mst l1 on l1.namecd1 = 'G213' \
where \
    t1.YEAR < '2016' \
    OR \
    (t1.YEAR = '2016' AND t1.GRADE > '01') \
)

insert into REDUCTION_MAX_MONEY_DAT \
( \
select \
    t1.YEAR, \
    t1.PREFECTURESCD, \
    t1.GRADE, \
    l1.namecd2, \
    t1.REDUCTIONMONEY_1, \
    t1.REDUCTIONMONEY_2, \
    t1.MAX_MONEY, \
    t1.REDUCTIONMONEY_1, \
    t1.REDUCTIONMONEY_2, \
    t1.MAX_MONEY, \
    0, \
    0, \
    t1.REGISTERCD, \
    t1.UPDATED \
from \
    REDUCTION_MAX_MONEY_DAT_OLD t1 \
    left join name_mst l1 on l1.namecd1 = 'G218' \
where \
    t1.YEAR >= '2017' \
    OR \
    (t1.YEAR = '2016' AND t1.GRADE <= '01') \
)
