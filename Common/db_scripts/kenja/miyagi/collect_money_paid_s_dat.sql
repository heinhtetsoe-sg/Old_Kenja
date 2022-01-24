-- kanji=����
-- $Id: collect_money_paid_s_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--����Ѿ�ʬ��ǡ���
DROP TABLE COLLECT_MONEY_PAID_S_DAT

CREATE TABLE COLLECT_MONEY_PAID_S_DAT \
( \
        "YEAR"              VARCHAR(4)  NOT NULL, \
        "SCHREGNO"          VARCHAR(8)  NOT NULL, \
        "PAID_SEQ"          SMALLINT    NOT NULL, \
        "COLLECT_GRP_CD"    VARCHAR(4)  NOT NULL, \
        "COLLECT_L_CD"      VARCHAR(2)  NOT NULL, \
        "COLLECT_M_CD"      VARCHAR(2)  NOT NULL, \
        "COLLECT_S_CD"      VARCHAR(2)  NOT NULL, \
        "PAID_INPUT_FLG"    VARCHAR(1)  NOT NULL, \
        "PAID_MONEY_DATE"   DATE, \
        "PAID_MONEY"        INTEGER, \
        "PAID_MONEY_DIV"    VARCHAR(2), \
        "REGISTERCD"        VARCHAR(10), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_MONEY_PAID_S_DAT \
ADD CONSTRAINT PK_MONEY_PAID_S_DA \
PRIMARY KEY \
(YEAR, SCHREGNO, PAID_SEQ)
