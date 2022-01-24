-- kanji=����
-- $Id: jbank_class_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--JBANK_CLASS_MST      ��ػ���ԥ��饹�ޥ���

DROP TABLE JBANK_CLASS_MST

CREATE TABLE JBANK_CLASS_MST \
( \
        "YEAR"          VARCHAR(4)  NOT NULL, \
        "GRADE"         VARCHAR(2)  NOT NULL, \
        "HR_CLASS"      VARCHAR(3)  NOT NULL, \
        "BANK_MAJORCD"  VARCHAR(2), \
        "BANK_HR_CLASS" VARCHAR(2), \
        "REGISTERCD"    VARCHAR(8), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JBANK_CLASS_MST \
ADD CONSTRAINT PK_JBANK_CLASS_MST \
PRIMARY KEY \
(YEAR,GRADE,HR_CLASS)

