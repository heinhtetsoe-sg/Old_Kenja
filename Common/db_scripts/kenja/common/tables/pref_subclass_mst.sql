-- kanji=����
-- $Id: 9b038ed9d61297deeb920cf790fa207f95d7a73b $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table PREF_SUBCLASS_MST

create table PREF_SUBCLASS_MST ( \
    PREF_SUBCLASSCD    VARCHAR(6) NOT NULL, \
    SUBCLASS_NAME      VARCHAR(60), \
    SUBCLASS_ABBV      VARCHAR(60), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table PREF_SUBCLASS_MST add constraint PK_PREF_SUBCLASS_M \
        primary key (PREF_SUBCLASSCD)
