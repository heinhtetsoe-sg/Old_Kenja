-- kanji=����
-- $Id: payment_money_cancel_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table PAYMENT_MONEY_CANCEL_DAT

create table PAYMENT_MONEY_CANCEL_DAT \
(  \
        APPLICANTNO           varchar(7) not null, \
        PAYMENT_DATE          date not null, \
        PAYMENT_DIV           varchar(2) not null, \
        INQUIRY_NO            varchar(6) not null, \
        PAYMENT_MONEY         integer not null, \
        CANCEL_REGISTERCD     varchar(8) not null, \
        CANCEL_UPDATED        timestamp default not null, \
        REGISTERCD            varchar(8) not null, \
        UPDATED               timestamp default current timestamp not null \
) in usr1dms index in idx1dms

alter table PAYMENT_MONEY_CANCEL_DAT  \
add constraint PK_PAYMENT_CANCEL  \
primary key  \
(APPLICANTNO, UPDATED)
