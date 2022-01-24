-- kanji=����
-- $Id: money_paid_m_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--�������ʬ��ǡ���
DROP TABLE MONEY_PAID_M_DAT

CREATE TABLE MONEY_PAID_M_DAT \
( \
        "YEAR"              VARCHAR(4)  NOT NULL, \
        "SCHREGNO"          VARCHAR(8)  NOT NULL, \
        "EXPENSE_L_CD"      VARCHAR(2)  NOT NULL, \
        "EXPENSE_M_CD"      VARCHAR(2)  NOT NULL, \
        "PAID_INPUT_FLG"    VARCHAR(1)  NOT NULL, \
        "PAID_MONEY_DATE"   DATE, \
        "PAID_MONEY"        INTEGER, \
        "PAID_MONEY_DIV"    VARCHAR(2), \
        "REPAY_DATE"        DATE, \
        "REPAY_MONEY"       INTEGER, \
        "REPAY_DEV"         VARCHAR(2), \
        "REMARK"            VARCHAR(60), \
        "REGISTERCD"        VARCHAR(8), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MONEY_PAID_M_DAT \
ADD CONSTRAINT PK_MONEY_PAID_M_DT \
PRIMARY KEY \
(YEAR,SCHREGNO,EXPENSE_L_CD,EXPENSE_M_CD,PAID_INPUT_FLG)
