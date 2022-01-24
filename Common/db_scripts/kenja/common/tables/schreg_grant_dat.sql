-- kanji=����
-- $Id: e68f6e6aafebefcec7e92fd8a1a577495c5147da $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--SCHREG_GRANT_DAT	���̸��եǡ���

DROP TABLE SCHREG_GRANT_DAT

CREATE TABLE SCHREG_GRANT_DAT \
( \
        "YEAR"          VARCHAR(4)  NOT NULL, \
        "SCHREGNO"      VARCHAR(8)  NOT NULL, \
        "GRANTCD"       VARCHAR(2)  NOT NULL, \
        "GRANTSDATE"    DATE, \
        "GRANTEDATE"    DATE, \
        "GRANT_MONEY"   INTEGER, \
        "REMARK"        VARCHAR(75), \
        "REGISTERCD"    VARCHAR(10), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_GRANT_DAT \
ADD CONSTRAINT PK_SCREG_GRANT_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,GRANTCD)

