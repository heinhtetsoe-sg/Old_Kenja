-- kanji=����
-- $Id: application_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--APPLICATION_DAT�����ǡ���

DROP TABLE APPLICATION_DAT

CREATE TABLE APPLICATION_DAT \
( \
        "YEAR"             VARCHAR(4)  NOT NULL, \
        "SCHREGNO"         VARCHAR(8)  NOT NULL, \
        "APPLICATIONCD"    VARCHAR(4)  NOT NULL, \
        "APPLI_MONEY_DUE"  INTEGER, \
        "APPLIED_DATE"     DATE, \
        "APPLI_PAID_MONEY" INTEGER, \
        "APPLI_PAID_FLG"   VARCHAR(1), \
        "APPLI_PAID_DIV"   VARCHAR(2), \
        "APPLI_PAID_DATE"  DATE, \
        "REGISTERCD"       VARCHAR(8), \
        "UPDATED"          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE APPLICATION_DAT \
ADD CONSTRAINT PK_APPLICATION_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,APPLICATIONCD)
