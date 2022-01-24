-- kanji=����
-- $Id: bank_result_tmp_data.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��Խ�����̰켡�ݴɥǡ���

DROP TABLE BANK_RESULT_TMP_DATA

CREATE TABLE BANK_RESULT_TMP_DATA \
( \
        "FILE_LINE_NUMBER"              INTEGER     NOT NULL, \
        "PROCESS1_UPDATED"              TIMESTAMP, \
        "PROCESS1_STS"                  VARCHAR(1), \
        "PROCESS2_UPDATED"              TIMESTAMP, \
        "PROCESS2_STS"                  VARCHAR(1), \
        "PROCESS3_UPDATED"              TIMESTAMP, \
        "PROCESS3_STS"                  VARCHAR(1), \
        "DATA_DATE"                     DATE, \
        "SCHOOL_CODE"                   VARCHAR(5), \
        "COURSE_CODE"                   VARCHAR(8), \
        "SCHREGNO"                      VARCHAR(8), \
        "BIRTHDAY"                      DATE, \
        "NAME_KANA"                     VARCHAR(48), \
        "NAME"                          VARCHAR(36), \
        "PAYER_NAME_KANA"               VARCHAR(48), \
        "PAYER_NAME"                    VARCHAR(36), \
        "PAYER_TELNO"                   VARCHAR(15), \
        "BANKCD"                        VARCHAR(4), \
        "BRANCHCD"                      VARCHAR(3), \
        "BANKNAME_KANA"                 VARCHAR(45), \
        "BRANCHNAME_KANA"               VARCHAR(45), \
        "DEPOSIT_ITEM"                  VARCHAR(1), \
        "ACCOUNTNO"                     VARCHAR(7), \
        "ACCOUNTNAME"                   VARCHAR(48), \
        "EXPENSE01_PAID_MONEY"          INTEGER, \
        "EXPENSE01_NECE_MONEY"          INTEGER, \
        "EXPENSE02_PAID_MONEY"          INTEGER, \
        "EXPENSE02_NECE_MONEY"          INTEGER, \
        "EXPENSE03_PAID_MONEY"          INTEGER, \
        "EXPENSE03_NECE_MONEY"          INTEGER, \
        "EXPENSE04_PAID_MONEY"          INTEGER, \
        "EXPENSE04_NECE_MONEY"          INTEGER, \
        "EXPENSE05_PAID_MONEY"          INTEGER, \
        "EXPENSE05_NECE_MONEY"          INTEGER, \
        "EXPENSE06_PAID_MONEY"          INTEGER, \
        "EXPENSE06_NECE_MONEY"          INTEGER, \
        "TERM01_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM01_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM01_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM01_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM01_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM01_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM01_SUM_MONEY_DUE"          INTEGER, \
        "TERM01_FLAG01"                 VARCHAR(1), \
        "TERM01_FLAG02"                 VARCHAR(1), \
        "TERM01_FLAG03"                 VARCHAR(1), \
        "TERM01_FLAG04"                 VARCHAR(1), \
        "TERM01_FLAG05"                 VARCHAR(1), \
        "TERM01_PAID_MONEY_DATE"        DATE, \
        "TERM01_FURI_MONEY_DATE"        DATE, \
        "TERM02_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM02_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM02_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM02_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM02_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM02_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM02_SUM_MONEY_DUE"          INTEGER, \
        "TERM02_FLAG01"                 VARCHAR(1), \
        "TERM02_FLAG02"                 VARCHAR(1), \
        "TERM02_FLAG03"                 VARCHAR(1), \
        "TERM02_FLAG04"                 VARCHAR(1), \
        "TERM02_FLAG05"                 VARCHAR(1), \
        "TERM02_PAID_MONEY_DATE"        DATE, \
        "TERM02_FURI_MONEY_DATE"        DATE, \
        "TERM03_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM03_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM03_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM03_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM03_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM03_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM03_SUM_MONEY_DUE"          INTEGER, \
        "TERM03_FLAG01"                 VARCHAR(1), \
        "TERM03_FLAG02"                 VARCHAR(1), \
        "TERM03_FLAG03"                 VARCHAR(1), \
        "TERM03_FLAG04"                 VARCHAR(1), \
        "TERM03_FLAG05"                 VARCHAR(1), \
        "TERM03_PAID_MONEY_DATE"        DATE, \
        "TERM03_FURI_MONEY_DATE"        DATE, \
        "TERM04_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM04_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM04_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM04_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM04_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM04_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM04_SUM_MONEY_DUE"          INTEGER, \
        "TERM04_FLAG01"                 VARCHAR(1), \
        "TERM04_FLAG02"                 VARCHAR(1), \
        "TERM04_FLAG03"                 VARCHAR(1), \
        "TERM04_FLAG04"                 VARCHAR(1), \
        "TERM04_FLAG05"                 VARCHAR(1), \
        "TERM04_PAID_MONEY_DATE"        DATE, \
        "TERM04_FURI_MONEY_DATE"        DATE, \
        "TERM05_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM05_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM05_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM05_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM05_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM05_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM05_SUM_MONEY_DUE"          INTEGER, \
        "TERM05_FLAG01"                 VARCHAR(1), \
        "TERM05_FLAG02"                 VARCHAR(1), \
        "TERM05_FLAG03"                 VARCHAR(1), \
        "TERM05_FLAG04"                 VARCHAR(1), \
        "TERM05_FLAG05"                 VARCHAR(1), \
        "TERM05_PAID_MONEY_DATE"        DATE, \
        "TERM05_FURI_MONEY_DATE"        DATE, \
        "TERM06_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM06_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM06_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM06_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM06_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM06_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM06_SUM_MONEY_DUE"          INTEGER, \
        "TERM06_FLAG01"                 VARCHAR(1), \
        "TERM06_FLAG02"                 VARCHAR(1), \
        "TERM06_FLAG03"                 VARCHAR(1), \
        "TERM06_FLAG04"                 VARCHAR(1), \
        "TERM06_FLAG05"                 VARCHAR(1), \
        "TERM06_PAID_MONEY_DATE"        DATE, \
        "TERM06_FURI_MONEY_DATE"        DATE, \
        "TERM07_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM07_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM07_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM07_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM07_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM07_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM07_SUM_MONEY_DUE"          INTEGER, \
        "TERM07_FLAG01"                 VARCHAR(1), \
        "TERM07_FLAG02"                 VARCHAR(1), \
        "TERM07_FLAG03"                 VARCHAR(1), \
        "TERM07_FLAG04"                 VARCHAR(1), \
        "TERM07_FLAG05"                 VARCHAR(1), \
        "TERM07_PAID_MONEY_DATE"        DATE, \
        "TERM07_FURI_MONEY_DATE"        DATE, \
        "TERM08_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM08_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM08_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM08_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM08_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM08_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM08_SUM_MONEY_DUE"          INTEGER, \
        "TERM08_FLAG01"                 VARCHAR(1), \
        "TERM08_FLAG02"                 VARCHAR(1), \
        "TERM08_FLAG03"                 VARCHAR(1), \
        "TERM08_FLAG04"                 VARCHAR(1), \
        "TERM08_FLAG05"                 VARCHAR(1), \
        "TERM08_PAID_MONEY_DATE"        DATE, \
        "TERM08_FURI_MONEY_DATE"        DATE, \
        "TERM09_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM09_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM09_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM09_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM09_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM09_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM09_SUM_MONEY_DUE"          INTEGER, \
        "TERM09_FLAG01"                 VARCHAR(1), \
        "TERM09_FLAG02"                 VARCHAR(1), \
        "TERM09_FLAG03"                 VARCHAR(1), \
        "TERM09_FLAG04"                 VARCHAR(1), \
        "TERM09_FLAG05"                 VARCHAR(1), \
        "TERM09_PAID_MONEY_DATE"        DATE, \
        "TERM09_FURI_MONEY_DATE"        DATE, \
        "TERM10_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM10_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM10_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM10_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM10_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM10_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM10_SUM_MONEY_DUE"          INTEGER, \
        "TERM10_FLAG01"                 VARCHAR(1), \
        "TERM10_FLAG02"                 VARCHAR(1), \
        "TERM10_FLAG03"                 VARCHAR(1), \
        "TERM10_FLAG04"                 VARCHAR(1), \
        "TERM10_FLAG05"                 VARCHAR(1), \
        "TERM10_PAID_MONEY_DATE"        DATE, \
        "TERM10_FURI_MONEY_DATE"        DATE, \
        "TERM11_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM11_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM11_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM11_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM11_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM11_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM11_SUM_MONEY_DUE"          INTEGER, \
        "TERM11_FLAG01"                 VARCHAR(1), \
        "TERM11_FLAG02"                 VARCHAR(1), \
        "TERM11_FLAG03"                 VARCHAR(1), \
        "TERM11_FLAG04"                 VARCHAR(1), \
        "TERM11_FLAG05"                 VARCHAR(1), \
        "TERM11_PAID_MONEY_DATE"        DATE, \
        "TERM11_FURI_MONEY_DATE"        DATE, \
        "TERM12_EXPENSE01_MONEY_DUE"    INTEGER, \
        "TERM12_EXPENSE02_MONEY_DUE"    INTEGER, \
        "TERM12_EXPENSE03_MONEY_DUE"    INTEGER, \
        "TERM12_EXPENSE04_MONEY_DUE"    INTEGER, \
        "TERM12_EXPENSE05_MONEY_DUE"    INTEGER, \
        "TERM12_EXPENSE06_MONEY_DUE"    INTEGER, \
        "TERM12_SUM_MONEY_DUE"          INTEGER, \
        "TERM12_FLAG01"                 VARCHAR(1), \
        "TERM12_FLAG02"                 VARCHAR(1), \
        "TERM12_FLAG03"                 VARCHAR(1), \
        "TERM12_FLAG04"                 VARCHAR(1), \
        "TERM12_FLAG05"                 VARCHAR(1), \
        "TERM12_PAID_MONEY_DATE"        DATE, \
        "TERM12_FURI_MONEY_DATE"        DATE, \
        "EXTR_EXPENSE01_TRANS_SDATE"    DATE, \
        "EXTR_EXPENSE01_MONEY"              INTEGER, \
        "EXTR_EXPENSE01_FLAG01"             VARCHAR(1), \
        "EXTR_EXPENSE01_FLAG02"             VARCHAR(1), \
        "EXTR_EXPENSE01_FLAG03"             VARCHAR(1), \
        "EXTR_EXPENSE01_FLAG04"             VARCHAR(1), \
        "EXTR_EXPENSE01_FLAG05"             VARCHAR(1), \
        "EXTR_EXPENSE01_PAID_MONEY_DATE"    DATE, \
        "EXTR_EXPENSE02_TRANS_SDATE"    DATE, \
        "EXTR_EXPENSE02_MONEY"              INTEGER, \
        "EXTR_EXPENSE02_FLAG01"             VARCHAR(1), \
        "EXTR_EXPENSE02_FLAG02"             VARCHAR(1), \
        "EXTR_EXPENSE02_FLAG03"             VARCHAR(1), \
        "EXTR_EXPENSE02_FLAG04"             VARCHAR(1), \
        "EXTR_EXPENSE02_FLAG05"             VARCHAR(1), \
        "EXTR_EXPENSE02_PAID_MONEY_DATE"    DATE, \
        "EXTR_EXPENSE03_TRANS_SDATE"    DATE, \
        "EXTR_EXPENSE03_MONEY"              INTEGER, \
        "EXTR_EXPENSE03_FLAG01"             VARCHAR(1), \
        "EXTR_EXPENSE03_FLAG02"             VARCHAR(1), \
        "EXTR_EXPENSE03_FLAG03"             VARCHAR(1), \
        "EXTR_EXPENSE03_FLAG04"             VARCHAR(1), \
        "EXTR_EXPENSE03_FLAG05"             VARCHAR(1), \
        "EXTR_EXPENSE03_PAID_MONEY_DATE"    DATE, \
        "EXTR_EXPENSE04_TRANS_SDATE"    DATE, \
        "EXTR_EXPENSE04_MONEY"              INTEGER, \
        "EXTR_EXPENSE04_FLAG01"             VARCHAR(1), \
        "EXTR_EXPENSE04_FLAG02"             VARCHAR(1), \
        "EXTR_EXPENSE04_FLAG03"             VARCHAR(1), \
        "EXTR_EXPENSE04_FLAG04"             VARCHAR(1), \
        "EXTR_EXPENSE04_FLAG05"             VARCHAR(1), \
        "EXTR_EXPENSE04_PAID_MONEY_DATE"    DATE, \
        "EXTR_EXPENSE05_TRANS_SDATE"    DATE, \
        "EXTR_EXPENSE05_MONEY"              INTEGER, \
        "EXTR_EXPENSE05_FLAG01"             VARCHAR(1), \
        "EXTR_EXPENSE05_FLAG02"             VARCHAR(1), \
        "EXTR_EXPENSE05_FLAG03"             VARCHAR(1), \
        "EXTR_EXPENSE05_FLAG04"             VARCHAR(1), \
        "EXTR_EXPENSE05_FLAG05"             VARCHAR(1), \
        "EXTR_EXPENSE05_PAID_MONEY_DATE"    DATE, \
        "EXTR_EXPENSE06_TRANS_SDATE"    DATE, \
        "EXTR_EXPENSE06_MONEY"              INTEGER, \
        "EXTR_EXPENSE06_FLAG01"             VARCHAR(1), \
        "EXTR_EXPENSE06_FLAG02"             VARCHAR(1), \
        "EXTR_EXPENSE06_FLAG03"             VARCHAR(1), \
        "EXTR_EXPENSE06_FLAG04"             VARCHAR(1), \
        "EXTR_EXPENSE06_FLAG05"             VARCHAR(1), \
        "EXTR_EXPENSE06_PAID_MONEY_DATE"    DATE, \
        "REGISTERCD"                        VARCHAR(8), \
        "UPDATED"                           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE BANK_RESULT_TMP_DATA \
ADD CONSTRAINT PK_BANK_RES_TMP_DT \
PRIMARY KEY \
(FILE_LINE_NUMBER)