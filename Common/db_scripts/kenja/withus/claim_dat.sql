-- kanji=����
-- $Id: claim_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CLAIM_DAT

create table CLAIM_DAT \
(  \
            SLIP_NO           varchar(8) not null, \
            APPLICANTNO       varchar(7) not null, \
            SLIP_DIV          varchar(1), \
            MANNER_PAYMENT    varchar(1), \
            PAYMENT_SEQ       varchar(2), \
            TOTAL_MONEY       integer, \
            CLAIM_DATE        date, \
            TOTAL_CLAIM_MONEY integer, \
            PRICE             integer, \
            TAX               integer, \
            PAYMENT_MONEY     integer, \
            PAYMENT_DATE      date, \
            SUMMING_UP_MONEY  integer, \
            SUMMING_UP_DATE   date, \
            AZCASHIN_FLG      varchar(1), \
            CANCEL_FLG        varchar(1), \
            TEMP_CREDITS      smallint, \
            COMP_ENT_FLG      varchar(1), \
            BATCH_FLG         varchar(1), \
            REGISTERCD        varchar(8), \
            UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLAIM_DAT  \
add constraint PK_CLAIM_DAT  \
primary key  \
(SLIP_NO)
