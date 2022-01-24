-- kanji=����
-- $Id: rep-sch_attend_dat.sql 69616 2019-09-11 01:58:07Z yamashiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback


DROP TABLE SCH_ATTEND_DAT_OLD
RENAME TABLE SCH_ATTEND_DAT TO SCH_ATTEND_DAT_OLD

CREATE TABLE SCH_ATTEND_DAT \
    (YEAR               VARCHAR(4) NOT NULL, \
     SCHREGNO           VARCHAR(8) NOT NULL, \
     EXECUTEDATE        DATE       NOT NULL, \
     CHAIRCD            VARCHAR(7) NOT NULL, \
     PERIODCD           VARCHAR(1), \
     SCHOOLING_SEQ      SMALLINT, \
     RECEIPT_DATE       DATE, \
     RECEIPT_TIME       TIME, \
     STAFFCD            VARCHAR(10), \
     TERMINAL_CD        VARCHAR(5), \
     SCHOOLINGKINDCD    VARCHAR(2), \
     REMARK             VARCHAR(60), \
     CREDIT_TIME        DECIMAL(3,1), \
     REGISTERCD         VARCHAR(10), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO SCH_ATTEND_DAT \
  SELECT \
     YEAR, \
     SCHREGNO, \
     EXECUTEDATE, \
     CHAIRCD, \
     PERIODCD, \
     SCHOOLING_SEQ, \
     RECEIPT_DATE, \
     RECEIPT_TIME, \
     STAFFCD, \
     TERMINAL_CD, \
     SCHOOLINGKINDCD, \
     REMARK, \
     CREDIT_TIME, \
     REGISTERCD, \
     UPDATED \
  FROM SCH_ATTEND_DAT_OLD
