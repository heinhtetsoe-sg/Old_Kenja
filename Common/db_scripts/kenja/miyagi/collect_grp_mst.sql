-- kanji=����
-- $Id: collect_grp_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܥ��롼�ץޥ���
DROP TABLE COLLECT_GRP_MST

CREATE TABLE COLLECT_GRP_MST \
( \
        "YEAR"               VARCHAR(4) NOT NULL, \
        "COLLECT_GRP_CD"     VARCHAR(4) NOT NULL, \
        "COLLECT_GRP_NAME"   VARCHAR(60), \
        "COLLECT_KOJIN_FLG"  VARCHAR(1), \
        "REGISTERCD"         VARCHAR(10), \
        "UPDATED"            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_GRP_MST \
ADD CONSTRAINT PK_COLLECT_GRP_MST \
PRIMARY KEY \
(YEAR, COLLECT_GRP_CD)
