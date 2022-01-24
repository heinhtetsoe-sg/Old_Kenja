-- kanji=����
-- $Id: levy_group_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ�����ײ��ܥޥ���

DROP TABLE LEVY_GROUP_MST

CREATE TABLE LEVY_GROUP_MST ( \
        "LEVY_GROUP_CD"     VARCHAR(4) NOT NULL, \
        "LEVY_GROUP_NAME"   VARCHAR(60), \
        "REGISTERCD"        VARCHAR(10), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_GROUP_MST ADD CONSTRAINT PK_LEVY_GROUP_MST PRIMARY KEY (LEVY_GROUP_CD)
