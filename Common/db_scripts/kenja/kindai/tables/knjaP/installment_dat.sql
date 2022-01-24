-- kanji=����
-- $Id: installment_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ʬǼ����Ǽ�ǡ���
DROP TABLE INSTALLMENT_DAT

CREATE TABLE INSTALLMENT_DAT \
( \
        "YEAR"             VARCHAR(4) NOT NULL, \
        "SCHREGNO"         VARCHAR(8) NOT NULL, \
        "INST_CD"          VARCHAR(2) NOT NULL, \
        "INST_SEQ"         SMALLINT   NOT NULL, \
        "INST_DUE_DATE"    DATE, \
        "INST_MONEY_DUE"   INTEGER, \
        "PAID_MONEY_DATE"  DATE, \
        "PAID_MONEY"       INTEGER,  \
        "PAID_MONEY_DIV"   VARCHAR(2), \
        "REPAY_DATE"       DATE,  \
        "REPAY_MONEY"      INTEGER,  \
        "REMARK"           VARCHAR(75), \
        "REGISTERCD"       VARCHAR(8), \
        "UPDATED"          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE INSTALLMENT_DAT \
ADD CONSTRAINT PK_MONEY_PAID_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,INST_CD,INST_SEQ)
