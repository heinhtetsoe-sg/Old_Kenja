-- kanji=����
-- $Id: 68a51dd6a9e40015c195e49821f2c48c72206d68 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܾ�ʬ��ޥ���
DROP TABLE COLLECT_S_DETAIL_DAT

CREATE TABLE COLLECT_S_DETAIL_DAT \
( \
        "SCHOOLCD"        VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"     VARCHAR(2)  NOT NULL, \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "COLLECT_L_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_M_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_S_CD"    VARCHAR(2) NOT NULL, \
        "TOKUSYU_CD"      VARCHAR(3) NOT NULL, \
        "TOKUSYU_VAL"     VARCHAR(1) NOT NULL, \
        "REGISTERCD"      VARCHAR(10), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_S_DETAIL_DAT \
ADD CONSTRAINT PK_COLLECT_S_D_DAT \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD, COLLECT_S_CD, TOKUSYU_CD)
