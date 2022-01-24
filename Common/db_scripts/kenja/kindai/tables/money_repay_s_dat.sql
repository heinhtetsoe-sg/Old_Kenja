-- kanji=����
-- $Id: money_repay_s_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--����Ѿ�ʬ��ǡ���
DROP TABLE MONEY_REPAY_S_DAT

CREATE TABLE MONEY_REPAY_S_DAT \
( \
        "YEAR"              VARCHAR(4)  NOT NULL, \
        "SCHREGNO"          VARCHAR(8)  NOT NULL, \
        "REPAY_SEQ"         SMALLINT    NOT NULL, \
        "REPAY_MONEY_DATE"  DATE        NOT NULL, \
        "EXPENSE_M_CD"      VARCHAR(2)  NOT NULL, \
        "EXPENSE_S_CD"      VARCHAR(2)  NOT NULL, \
        "REPAY_MONEY"       INTEGER     NOT NULL, \
        "REPAY_MONEY_DIV"   VARCHAR(2)  NOT NULL, \
        "REPAY_FLG"         VARCHAR(1), \
        "REMARK"            VARCHAR(30), \
        "REGISTERCD"        VARCHAR(8), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MONEY_REPAY_S_DAT \
ADD CONSTRAINT PK_MONEY_REPAID_S \
PRIMARY KEY \
(YEAR, SCHREGNO, REPAY_SEQ)
