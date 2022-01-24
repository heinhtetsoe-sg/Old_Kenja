-- kanji=����
-- $Id: 5643f40716591f1aff16d8c2b0307dc54160b1c0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ�����ײ��ܥޥ���

DROP TABLE LEVY_GROUP_DAT

CREATE TABLE LEVY_GROUP_DAT ( \
        "SCHOOLCD"      varchar(12) not null, \
        "SCHOOL_KIND"   varchar(2)  not null, \
        "YEAR"          varchar(4) not null, \
        "LEVY_GROUP_CD" varchar(4) not null, \
        "LEVY_L_CD"     varchar(2) not null, \
        "REGISTERCD"    varchar(10), \
        "UPDATED"       timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_GROUP_DAT ADD CONSTRAINT PK_LEVY_GROUP_DAT PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_GROUP_CD, LEVY_L_CD)
