-- kanji=����
-- $Id: over_payment_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table OVER_PAYMENT_HIST_DAT

create table OVER_PAYMENT_HIST_DAT \
    (APPLICANTNO        varchar(7) not null, \
     OVER_PAY_DATE      date not null, \
     OVER_PAYMENT_DIV   varchar(2) not null, \
     INQUIRY_NO         varchar(6) not null, \
     PAYMENT_DIV        varchar(2) not null, \
     PAYMENT_INQUIRY_NO varchar(6) not null, \
     OVER_PAYMENT       integer, \
     REGISTERCD         varchar(8)  , \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table OVER_PAYMENT_HIST_DAT add constraint PK_OVER_PAY_DAT primary key \
      (APPLICANTNO, OVER_PAY_DATE, OVER_PAYMENT_DIV, INQUIRY_NO)
