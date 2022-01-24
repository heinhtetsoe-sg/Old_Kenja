-- kanji=����
-- $Id: collect_grp_hr_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܥ��롼��HR���饹�ǡ���
DROP TABLE COLLECT_GRP_HR_DAT

CREATE TABLE COLLECT_GRP_HR_DAT \
( \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "COLLECT_GRP_CD"  VARCHAR(4) NOT NULL, \
        "GRADE"           VARCHAR(2) NOT NULL, \
        "HR_CLASS"        VARCHAR(3) NOT NULL, \
        "REGISTERCD"      VARCHAR(10), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_GRP_HR_DAT \
ADD CONSTRAINT PK_EXP_GRP_HR_DAT \
PRIMARY KEY \
(YEAR,COLLECT_GRP_CD,GRADE,HR_CLASS)
