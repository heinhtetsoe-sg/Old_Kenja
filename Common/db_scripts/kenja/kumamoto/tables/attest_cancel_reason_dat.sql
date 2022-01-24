-- kanji=����
-- $Id: attest_cancel_reason_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_REASON_DAT

CREATE TABLE ATTEST_CANCEL_REASON_DAT  \
(   CANCEL_YEAR      VARCHAR(4) NOT NULL, \
    CANCEL_SEQ       SMALLINT   NOT NULL, \
    CANCEL_STAFFCD   VARCHAR(8) NOT NULL, \
    CANCEL_DATE      DATE       NOT NULL, \
    REASON           VARCHAR(122), \
    REGISTERCD       VARCHAR(8)  , \
    UPDATED          TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE ATTEST_CANCEL_REASON_DAT \
ADD CONSTRAINT PK_ATC_REASON_DAT \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ)