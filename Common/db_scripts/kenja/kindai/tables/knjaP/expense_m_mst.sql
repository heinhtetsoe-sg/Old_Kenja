-- kanji=����
-- $Id: expense_m_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--������ʬ��ޥ���
DROP TABLE EXPENSE_M_MST \

CREATE TABLE EXPENSE_M_MST \
( \
        "YEAR"                VARCHAR(4) NOT NULL, \
        "EXPENSE_M_CD"        VARCHAR(2) NOT NULL, \
        "EXPENSE_L_CD"        VARCHAR(2), \
        "EXPENSE_M_NAME"      VARCHAR(60), \
        "EXPENSE_S_EXIST_FLG" VARCHAR(1), \
        "EXPENSE_M_MONEY"     INTEGER, \
        "DUE_DATE"            DATE, \
        "BANK_TRANS_SDATE"    DATE, \
        "REGISTERCD"          VARCHAR(8), \
        "UPDATED"             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EXPENSE_M_MST \
ADD CONSTRAINT PK_EXPENSE_M_MST \
PRIMARY KEY \
(YEAR,EXPENSE_M_CD)
