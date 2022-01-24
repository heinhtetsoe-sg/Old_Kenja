-- kanji=����
-- $Id: virtual_account_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table VIRTUAL_ACCOUNT_DAT

create table VIRTUAL_ACCOUNT_DAT \
(  \
        VIRTUAL_BANK_CD       varchar(3) not null, \
        VIRTUAL_ACCOUNT_DIV   varchar(1) not null, \
        VIRTUAL_ACCOUNT_NO    varchar(7) not null, \
        VIRTUAL_SDATE         date not null, \
        VIRTUAL_EDATE         date, \
        APPLICANTNO           varchar(7), \
        REGISTERCD            varchar(8), \
        UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table VIRTUAL_ACCOUNT_DAT  \
add constraint PK_VIRTUAL_ACCOUNT \
primary key  \
(VIRTUAL_BANK_CD, VIRTUAL_ACCOUNT_DIV, VIRTUAL_ACCOUNT_NO, VIRTUAL_SDATE)
