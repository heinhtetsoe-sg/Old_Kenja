-- kanji=漢字
-- $Id: rep-month_keeping_money_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MONTH_KEEPING_MONEY_DAT_OLD
create table MONTH_KEEPING_MONEY_DAT_OLD like MONTH_KEEPING_MONEY_DAT
insert into MONTH_KEEPING_MONEY_DAT_OLD select * from MONTH_KEEPING_MONEY_DAT

drop table MONTH_KEEPING_MONEY_DAT

create table MONTH_KEEPING_MONEY_DAT \
    (APPLICANTNO          varchar(7) not null, \
     YEAR_MONTH           varchar(6) not null, \
     COMMODITY_CD         varchar(5) not null, \
     S_YEAR_MONTH         varchar(6) not null, \
     E_YEAR_MONTH         varchar(6) not null, \
     SALES_SCHEDULE_MONEY integer, \
     KEEPING_MONEY        integer, \
     DIFFERENCE           integer, \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MONTH_KEEPING_MONEY_DAT add constraint PK_MONTH_KEEPING primary key \
      (APPLICANTNO, YEAR_MONTH, COMMODITY_CD, S_YEAR_MONTH, E_YEAR_MONTH)

insert into MONTH_KEEPING_MONEY_DAT select * from MONTH_KEEPING_MONEY_DAT_OLD
