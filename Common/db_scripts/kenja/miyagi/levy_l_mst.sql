-- kanji=����
-- $Id: levy_l_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ�����ײ��ܥޥ���

DROP TABLE LEVY_L_MST

CREATE TABLE LEVY_L_MST ( \
        "YEAR"         varchar(4)  not null, \
        "LEVY_L_CD"    VARCHAR(2) NOT NULL, \
        "LEVY_L_NAME"  VARCHAR(90), \
        "LEVY_L_ABBV"  VARCHAR(90), \
        "REGISTERCD"   VARCHAR(10), \
        "UPDATED"      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_L_MST ADD CONSTRAINT PK_LEVY_L_MST PRIMARY KEY (YEAR, LEVY_L_CD)
