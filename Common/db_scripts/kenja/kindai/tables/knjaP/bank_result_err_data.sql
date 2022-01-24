-- kanji=����
-- $Id: bank_result_err_data.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��Խ�����̥��顼�ǡ���
DROP TABLE BANK_RESULT_ERR_DATA

CREATE TABLE BANK_RESULT_ERR_DATA \
( \
        "PROCESSCD"             VARCHAR(1)      NOT NULL, \
        "FILE_LINE_NUMBER"      INTEGER         NOT NULL, \
        "COLNAME"               VARCHAR(128)    NOT NULL, \
        "ERR_LEVEL"             VARCHAR(1), \
        "ERR_MSG"               VARCHAR(150), \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE BANK_RESULT_ERR_DATA \
ADD CONSTRAINT PK_BANK_RES_ERR_DT \
PRIMARY KEY \
(PROCESSCD,FILE_LINE_NUMBER,COLNAME)
