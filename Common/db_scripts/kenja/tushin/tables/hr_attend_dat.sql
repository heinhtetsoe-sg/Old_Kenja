-- kanji=����
-- $Id: hr_attend_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback


DROP TABLE HR_ATTEND_DAT

CREATE TABLE HR_ATTEND_DAT \
    (YEAR               VARCHAR(4) NOT NULL, \
     SCHREGNO           VARCHAR(8) NOT NULL, \
     EXECUTEDATE        DATE       NOT NULL, \
     CHAIRCD            VARCHAR(7) NOT NULL, \
     PERIODCD           VARCHAR(1) NOT NULL, \
     RECEIPT_DATE       DATE, \
     RECEIPT_TIME       TIME, \
     TERMINAL_CD        VARCHAR(5), \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HR_ATTEND_DAT ADD CONSTRAINT PK_HR_ATTEND_DAT PRIMARY KEY (YEAR, SCHREGNO, EXECUTEDATE, CHAIRCD, PERIODCD)
