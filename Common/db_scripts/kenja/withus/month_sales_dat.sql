-- kanji=����
-- $Id: month_sales_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MONTH_SALES_DAT

create table MONTH_SALES_DAT \
    (YEAR_MONTH          varchar(6) not null, \
     STUDENT_DIV         varchar(2) not null, \
     COMMODITY_CD        varchar(5) not null, \
     COMMUTING_DIV       varchar(1), \
     SALES_MONEY         integer, \
     SALES_PRICE         integer, \
     SALES_TAX           integer, \
     SALES_CNT           integer, \
     TOTAL_SALES_MONEY   integer, \
     TOTAL_PRICE         integer, \
     TOTAL_TAX           integer, \
     TOTAL_SALES_CNT     integer, \
     REGISTERCD          varchar(8)  , \
     UPDATED             timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MONTH_SALES_DAT add constraint PK_MONTH_SALES_DAT primary key \
      (YEAR_MONTH, STUDENT_DIV, COMMODITY_CD)
