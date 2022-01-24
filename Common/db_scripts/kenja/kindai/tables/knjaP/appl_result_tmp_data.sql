-- kanji=����
-- $Id: appl_result_tmp_data.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--APPL_RESULT_TMP_DATA ���������̰켡�ݴɥǡ���
--2005/06/23 CLIENT_NAME_ORG �ɲ�

DROP TABLE APPL_RESULT_TMP_DATA

CREATE TABLE APPL_RESULT_TMP_DATA \
( \
        "FILE_LINE_NUMBER"      INTEGER     NOT NULL, \
        "PROCESS1_UPDATED"      TIMESTAMP, \
        "PROCESS1_STS"          VARCHAR(1), \
        "PROCESS2_UPDATED"      TIMESTAMP, \
        "PROCESS2_STS"          VARCHAR(1), \
        "MAKE_DATE"             DATE, \
        "BANKCD"                VARCHAR(4), \
        "BRANCHCD"              VARCHAR(3), \
        "DEPOSIT_ITEM"          VARCHAR(1), \
        "ACCOUNTNO"             VARCHAR(10), \
        "ACCOUNTNAME"           VARCHAR(120), \
        "APPLI_PAID_DATE"       DATE, \
        "INOUT_FG"              VARCHAR(1), \
        "JOB_FG"                VARCHAR(2), \
        "APPLI_PAID_MONEY"      INTEGER, \
        "REFERENCE_NUMBER"      VARCHAR(10), \
        "CLIENT_NAME"           VARCHAR(144), \
        "CLIENT_NAME_ORG"       VARCHAR(144), \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE APPL_RESULT_TMP_DATA \
ADD CONSTRAINT PK_RES_TMP_MST \
PRIMARY KEY \
(FILE_LINE_NUMBER)

COMMENT ON TABLE APPL_RESULT_TMP_DATA IS '���������̰켡�ݴɥǡ��� 2005/06/23'
