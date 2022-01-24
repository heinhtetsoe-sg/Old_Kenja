-- kanji=����
-- $Id: virtual_account_sch_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table VIRTUAL_ACCOUNT_SCH_DAT

create table VIRTUAL_ACCOUNT_SCH_DAT \
(  \
        APPLICANTNO         varchar(7) not null, \
        ADJUST_SDATE        date not null, \
        ADJUST_EDATE        date, \
        VIRTUAL_BANK_CD     varchar(3), \
        VIRTUAL_ACCOUNT_NO  varchar(7), \
        REGISTERCD          varchar(8), \
        UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table VIRTUAL_ACCOUNT_SCH_DAT  \
add constraint PK_VIRTUAL_SCH \
primary key  \
(APPLICANTNO, ADJUST_SDATE)
