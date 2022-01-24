-- kanji=����
-- $Id: rep-claim_details_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CLAIM_DETAILS_DAT_OLD
create table CLAIM_DETAILS_DAT_OLD like CLAIM_DETAILS_DAT
insert into CLAIM_DETAILS_DAT_OLD select * from CLAIM_DETAILS_DAT

drop table CLAIM_DETAILS_DAT

create table CLAIM_DETAILS_DAT \
(  \
        SLIP_NO           varchar(8) not null, \
        SEQ               varchar(2) not null, \
        APPLICANTNO       varchar(7) not null, \
        COMMODITY_CD      varchar(5) not null, \
        AMOUNT            varchar(2), \
        CLAIM_DATE        date, \
        TOTAL_CLAIM_MONEY integer, \
        PRICE             integer, \
        TOTAL_PRICE       integer, \
        TAX               integer, \
        S_YEAR_MONTH      varchar(6) not null, \
        E_YEAR_MONTH      varchar(6) not null, \
        PRIORITY_LEVEL    varchar(2), \
        PAYMENT_MONEY     integer, \
        PAYMENT_DATE      date, \
        SUMMING_UP_MONEY  integer, \
        SUMMING_UP_DATE   date, \
        DUMMY_FLG         varchar(1), \
        REMARK            varchar(150), \
        REGISTERCD        varchar(8), \
        UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLAIM_DETAILS_DAT  \
add constraint PK_CLAIM_DETAILS  \
primary key  \
(SLIP_NO, SEQ)

insert into CLAIM_DETAILS_DAT \
  select \
        SLIP_NO, \
        SEQ, \
        APPLICANTNO, \
        COMMODITY_CD, \
        AMOUNT, \
        CLAIM_DATE, \
        TOTAL_CLAIM_MONEY, \
        PRICE, \
        value(PRICE, 0) * value(cast(AMOUNT as smallint), 0) as TOTAL_PRICE, \
        TAX, \
        S_YEAR_MONTH, \
        E_YEAR_MONTH, \
        PRIORITY_LEVEL, \
        PAYMENT_MONEY, \
        PAYMENT_DATE, \
        SUMMING_UP_MONEY, \
        SUMMING_UP_DATE, \
        DUMMY_FLG, \
        REMARK, \
        REGISTERCD, \
        UPDATED \
  from CLAIM_DETAILS_DAT_OLD
