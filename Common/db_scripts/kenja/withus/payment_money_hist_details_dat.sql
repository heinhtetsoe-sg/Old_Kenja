-- kanji=����
-- $Id: payment_money_hist_details_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table PAYMENT_MONEY_HIST_DETAILS_DAT

create table PAYMENT_MONEY_HIST_DETAILS_DAT \
(  \
        APPLICANTNO           varchar(7) not null, \
        PAYMENT_DATE          date not null, \
        SEQ                   varchar(4) not null, \
        SLIP_NO               varchar(8) not null, \
        SLIP_SEQ              varchar(2) not null, \
        PLAN_YEAR             varchar(4) not null, \
        PLAN_MONTH            varchar(2) not null, \
        COMMODITY_CD          varchar(5) not null, \
        PAYMENT_MONEY         integer not null, \
        KEEPING_DIV           varchar(1) not null, \
        REGISTERCD            varchar(8), \
        UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PAYMENT_MONEY_HIST_DETAILS_DAT  \
add constraint PK_PAYMENT_DETAILS \
primary key  \
(APPLICANTNO, PAYMENT_DATE, SEQ)
