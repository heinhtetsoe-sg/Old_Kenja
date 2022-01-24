-- kanji=����
-- $Id: expense_grp_s_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܥ��롼�����ܾ�ʬ��ǡ���
DROP TABLE EXPENSE_GRP_S_DAT

CREATE TABLE EXPENSE_GRP_S_DAT \
( \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "EXPENSE_GRP_CD"  VARCHAR(4) NOT NULL, \
        "EXPENSE_M_CD"    VARCHAR(2) NOT NULL, \
        "EXPENSE_S_CD"    VARCHAR(2) NOT NULL, \
        "REGISTERCD"      VARCHAR(8), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EXPENSE_GRP_S_DAT \
ADD CONSTRAINT PK_EXP_GRP_S_DAT \
PRIMARY KEY \
(YEAR,EXPENSE_GRP_CD,EXPENSE_M_CD,EXPENSE_S_CD)
