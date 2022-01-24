-- kanji=����
-- $Id: 7d0f6e487389ab883a7b08b8105a67bf2aae1db8 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܥ��롼�ץޥ���
DROP TABLE COLLECT_GRP_DAT

CREATE TABLE COLLECT_GRP_DAT \
( \
        "SCHOOLCD"           VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"        VARCHAR(2)  NOT NULL, \
        "YEAR"               VARCHAR(4) NOT NULL, \
        "COLLECT_GRP_CD"     VARCHAR(4) NOT NULL, \
        "COLLECT_L_CD"       VARCHAR(2) NOT NULL, \
        "COLLECT_M_CD"       VARCHAR(2) NOT NULL, \
        "COLLECT_S_CD"       VARCHAR(2) NOT NULL, \
        "REGISTERCD"         VARCHAR(10), \
        "UPDATED"            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_GRP_DAT \
ADD CONSTRAINT PK_COLLECT_GRP_DAT \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_GRP_CD, COLLECT_L_CD, COLLECT_M_CD, COLLECT_S_CD)
