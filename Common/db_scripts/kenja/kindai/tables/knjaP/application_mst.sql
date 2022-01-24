-- kanji=����
-- $Id: application_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--APPLICATION_MST�����ޥ���
--2005/05/30 ����������ԥǡ����ι����ɲ�

DROP TABLE APPLICATION_MST

CREATE TABLE APPLICATION_MST \
( \
        "YEAR"             VARCHAR(4)   NOT NULL, \
        "APPLICATIONCD"    VARCHAR(4)   NOT NULL, \
        "APPLICATIONNAME"  VARCHAR(60), \
        "APPLICATIONMONEY" INTEGER, \
        "BANKCD"           VARCHAR(4),  \
        "BRANCHCD"         VARCHAR(3),  \
        "DEPOSIT_ITEM"     VARCHAR(1),  \
        "ACCOUNTNO"        VARCHAR(7),  \
        "ACCOUNTNAME"      VARCHAR(48), \
        "REGISTERCD"       VARCHAR(8), \
        "UPDATED"          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE APPLICATION_MST \
ADD CONSTRAINT PK_APPLICATION_MST \
PRIMARY KEY \
(YEAR,APPLICATIONCD)
