-- kanji=����
-- $Id: 720a154ef73afe83a821439ef2842759803014b0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܥ��롼�ץޥ���
DROP TABLE COLLECT_GRP_MST

CREATE TABLE COLLECT_GRP_MST \
( \
        "SCHOOLCD"              VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)  NOT NULL, \
        "YEAR"                  VARCHAR(4)  NOT NULL, \
        "COLLECT_GRP_CD"        VARCHAR(4)  NOT NULL, \
        "COLLECT_GRP_NAME"      VARCHAR(60), \
        "COLLECT_KOJIN_FLG"     VARCHAR(1), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_GRP_MST \
ADD CONSTRAINT PK_COLLECT_GRP_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_GRP_CD)
