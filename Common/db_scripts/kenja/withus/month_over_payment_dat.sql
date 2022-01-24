-- kanji=����
-- $Id: month_over_payment_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MONTH_OVER_PAYMENT_DAT

create table MONTH_OVER_PAYMENT_DAT \
    (APPLICANTNO             varchar(7) not null, \
     YEAR_MONTH              varchar(6) not null, \
     LAST_MONTH_OVER_REMAIN  integer, \
     MONTH_OVER_MONEY        integer, \
     RE_PAY_MONEY            integer, \
     MONTH_OVER_MONEY_REMAIN integer, \
     REGISTERCD              varchar(8), \
     UPDATED                 timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MONTH_OVER_PAYMENT_DAT add constraint PK_MONTH_OVER_PAY primary key \
      (APPLICANTNO, YEAR_MONTH)
