-- kanji=����
-- $Id: schreg_grant_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

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
        "REGISTERCD"    VARCHAR(8), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_GRANT_DAT \
ADD CONSTRAINT PK_SCREG_GRANT_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,GRANTCD)

