-- kanji=����
-- $Id: ecd82f2cb3a8784383b21b606f7bd5bc1f9517ae $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table MOCK_SUBCLASS_MST

create table MOCK_SUBCLASS_MST ( \
    MOCK_SUBCLASS_CD   VARCHAR(6) NOT NULL, \
    SUBCLASS_NAME      VARCHAR(60), \
    SUBCLASS_ABBV      VARCHAR(15), \
    CLASSCD            VARCHAR(2), \
    SCHOOL_KIND        VARCHAR(2), \
    CURRICULUM_CD      VARCHAR(2), \
    SUBCLASSCD         VARCHAR(6), \
    PREF_SUBCLASSCD    VARCHAR(6), \
    SUBCLASS_DIV       VARCHAR(1), \
    REGISTERCD         VARCHAR(10), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table MOCK_SUBCLASS_MST add constraint PK_MOCK_SUBCLASS_M \
        primary key (MOCK_SUBCLASS_CD)
