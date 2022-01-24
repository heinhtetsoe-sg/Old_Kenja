-- kanji=����
-- $Id: hreportremark_t_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback


DROP TABLE HREPORTREMARK_T_DAT

CREATE TABLE HREPORTREMARK_T_DAT \
    (REMARKID           VARCHAR(1) NOT NULL, \
     REMARK             VARCHAR(210), \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORTREMARK_T_DAT ADD CONSTRAINT PK_HREP_T_DAT PRIMARY KEY (REMARKID)
