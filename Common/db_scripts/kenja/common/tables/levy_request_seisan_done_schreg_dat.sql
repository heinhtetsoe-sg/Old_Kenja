-- kanji=����
-- $Id: c78dc4ea2780f417ffd4f28727a18ad96baad2e0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ����������ǡ���

drop table LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT

create table LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT \
( \
        "SCHREGNO"      varchar(8) not null, \
        "INCOME_MONEY"  integer, \
        "OUTGO_MONEY"   integer, \
        "CARRYOVER"     integer, \
        "KYUFU_MONEY"   integer, \
        "HASUU_HENKIN"  integer, \
        "HENKIN"        integer, \
        "ZANDAKA"       integer, \
        "REGISTERCD"    varchar(10), \
        "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT add constraint PK_SEISAN_DONE primary key (SCHREGNO)
