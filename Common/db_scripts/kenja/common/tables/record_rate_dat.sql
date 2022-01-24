-- kanji=����
-- $Id: 8733eead325057296f401a2e9425a473720b9bec $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_RATE_DAT

create table RECORD_RATE_DAT ( \
    YEAR               varchar(4) not null, \
    SUBCLASSCD         varchar(6) not null, \
    RATE               smallint, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp \
) in usr1dms index in idx1dms

alter table RECORD_RATE_DAT add constraint PK_RECORD_RATE_DAT \
        primary key (YEAR, SUBCLASSCD)
