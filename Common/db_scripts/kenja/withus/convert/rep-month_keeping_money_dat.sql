-- kanji=����
-- $Id: rep-month_keeping_money_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
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
