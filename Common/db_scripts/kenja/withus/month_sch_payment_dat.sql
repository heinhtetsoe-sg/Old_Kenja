-- kanji=����
-- $Id: month_sch_payment_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MONTH_SCH_PAYMENT_DAT

create table MONTH_SCH_PAYMENT_DAT \
    (APPLICANTNO         varchar(7) not null, \
     YEAR_MONTH          varchar(6) not null, \
     COMMODITY_CD        varchar(5) not null, \
     PAYMENT_MONEY       integer, \
     PRICE               integer, \
     TAX                 integer, \
     BELONGING_DIV       varchar(3), \
     STUDENT_DIV         varchar(2), \
     COMMUTING_DIV       varchar(1), \
     REGISTERCD          varchar(8)  , \
     UPDATED             timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table MONTH_SCH_PAYMENT_DAT add constraint PK_MON_SCH_PAYMENT primary key \
      (APPLICANTNO, YEAR_MONTH, COMMODITY_CD)
