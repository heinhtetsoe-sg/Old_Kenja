-- kanji=漢字
-- $Id: rep-bank_result_tmp_data.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--銀行処理結果一次保管データ

drop table BANK_RESULT_TMP_DATA_OLD
create table BANK_RESULT_TMP_DATA_OLD like BANK_RESULT_TMP_DATA
insert into BANK_RESULT_TMP_DATA_OLD select * from BANK_RESULT_TMP_DATA

drop table BANK_RESULT_TMP_DATA

create table BANK_RESULT_TMP_DATA \
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

alter table BANK_RESULT_TMP_DATA \
add constraint PK_BANK_RES_TMP_dt \
primary key \
(file_line_number)

insert into BANK_RESULT_TMP_DATA \
( \
    select \
        FILE_LINE_NUMBER, \
        PROCESS1_UPDATED, \
        PROCESS1_STS, \
        PROCESS2_UPDATED, \
        PROCESS2_STS, \
        PROCESS3_UPDATED, \
        PROCESS3_STS, \
        DATA_DATE, \
        SCHOOL_CODE, \
        COURSE_CODE, \
        SCHREGNO, \
        BIRTHDAY, \
        NAME_KANA, \
        NAME, \
        PAYER_NAME_KANA, \
        PAYER_NAME, \
        PAYER_TELNO, \
        BANKCD, \
        BRANCHCD, \
        BANKNAME_KANA, \
        BRANCHNAME_KANA, \
        DEPOSIT_ITEM, \
        ACCOUNTNO, \
        ACCOUNTNAME, \
        EXPENSE01_PAID_MONEY, \
        EXPENSE01_NECE_MONEY, \
        EXPENSE02_PAID_MONEY, \
        EXPENSE02_NECE_MONEY, \
        EXPENSE03_PAID_MONEY, \
        EXPENSE03_NECE_MONEY, \
        EXPENSE04_PAID_MONEY, \
        EXPENSE04_NECE_MONEY, \
        EXPENSE05_PAID_MONEY, \
        EXPENSE05_NECE_MONEY, \
        EXPENSE06_PAID_MONEY, \
        EXPENSE06_NECE_MONEY, \
        TERM01_EXPENSE01_MONEY_DUE, \
        TERM01_EXPENSE02_MONEY_DUE, \
        TERM01_EXPENSE03_MONEY_DUE, \
        TERM01_EXPENSE04_MONEY_DUE, \
        TERM01_EXPENSE05_MONEY_DUE, \
        TERM01_EXPENSE06_MONEY_DUE, \
        TERM01_SUM_MONEY_DUE, \
        TERM01_FLAG01, \
        TERM01_FLAG02, \
        TERM01_FLAG03, \
        TERM01_FLAG04, \
        TERM01_FLAG05, \
        TERM01_PAID_MONEY_DATE, \
        TERM01_PAID_MONEY_DATE, \
        TERM02_EXPENSE01_MONEY_DUE, \
        TERM02_EXPENSE02_MONEY_DUE, \
        TERM02_EXPENSE03_MONEY_DUE, \
        TERM02_EXPENSE04_MONEY_DUE, \
        TERM02_EXPENSE05_MONEY_DUE, \
        TERM02_EXPENSE06_MONEY_DUE, \
        TERM02_SUM_MONEY_DUE, \
        TERM02_FLAG01, \
        TERM02_FLAG02, \
        TERM02_FLAG03, \
        TERM02_FLAG04, \
        TERM02_FLAG05, \
        TERM02_PAID_MONEY_DATE, \
        TERM02_PAID_MONEY_DATE, \
        TERM03_EXPENSE01_MONEY_DUE, \
        TERM03_EXPENSE02_MONEY_DUE, \
        TERM03_EXPENSE03_MONEY_DUE, \
        TERM03_EXPENSE04_MONEY_DUE, \
        TERM03_EXPENSE05_MONEY_DUE, \
        TERM03_EXPENSE06_MONEY_DUE, \
        TERM03_SUM_MONEY_DUE, \
        TERM03_FLAG01, \
        TERM03_FLAG02, \
        TERM03_FLAG03, \
        TERM03_FLAG04, \
        TERM03_FLAG05, \
        TERM03_PAID_MONEY_DATE, \
        TERM03_PAID_MONEY_DATE, \
        TERM04_EXPENSE01_MONEY_DUE, \
        TERM04_EXPENSE02_MONEY_DUE, \
        TERM04_EXPENSE03_MONEY_DUE, \
        TERM04_EXPENSE04_MONEY_DUE, \
        TERM04_EXPENSE05_MONEY_DUE, \
        TERM04_EXPENSE06_MONEY_DUE, \
        TERM04_SUM_MONEY_DUE, \
        TERM04_FLAG01, \
        TERM04_FLAG02, \
        TERM04_FLAG03, \
        TERM04_FLAG04, \
        TERM04_FLAG05, \
        TERM04_PAID_MONEY_DATE, \
        TERM04_PAID_MONEY_DATE, \
        TERM05_EXPENSE01_MONEY_DUE, \
        TERM05_EXPENSE02_MONEY_DUE, \
        TERM05_EXPENSE03_MONEY_DUE, \
        TERM05_EXPENSE04_MONEY_DUE, \
        TERM05_EXPENSE05_MONEY_DUE, \
        TERM05_EXPENSE06_MONEY_DUE, \
        TERM05_SUM_MONEY_DUE, \
        TERM05_FLAG01, \
        TERM05_FLAG02, \
        TERM05_FLAG03, \
        TERM05_FLAG04, \
        TERM05_FLAG05, \
        TERM05_PAID_MONEY_DATE, \
        TERM05_PAID_MONEY_DATE, \
        TERM06_EXPENSE01_MONEY_DUE, \
        TERM06_EXPENSE02_MONEY_DUE, \
        TERM06_EXPENSE03_MONEY_DUE, \
        TERM06_EXPENSE04_MONEY_DUE, \
        TERM06_EXPENSE05_MONEY_DUE, \
        TERM06_EXPENSE06_MONEY_DUE, \
        TERM06_SUM_MONEY_DUE, \
        TERM06_FLAG01, \
        TERM06_FLAG02, \
        TERM06_FLAG03, \
        TERM06_FLAG04, \
        TERM06_FLAG05, \
        TERM06_PAID_MONEY_DATE, \
        TERM06_PAID_MONEY_DATE, \
        TERM07_EXPENSE01_MONEY_DUE, \
        TERM07_EXPENSE02_MONEY_DUE, \
        TERM07_EXPENSE03_MONEY_DUE, \
        TERM07_EXPENSE04_MONEY_DUE, \
        TERM07_EXPENSE05_MONEY_DUE, \
        TERM07_EXPENSE06_MONEY_DUE, \
        TERM07_SUM_MONEY_DUE, \
        TERM07_FLAG01, \
        TERM07_FLAG02, \
        TERM07_FLAG03, \
        TERM07_FLAG04, \
        TERM07_FLAG05, \
        TERM07_PAID_MONEY_DATE, \
        TERM07_PAID_MONEY_DATE, \
        TERM08_EXPENSE01_MONEY_DUE, \
        TERM08_EXPENSE02_MONEY_DUE, \
        TERM08_EXPENSE03_MONEY_DUE, \
        TERM08_EXPENSE04_MONEY_DUE, \
        TERM08_EXPENSE05_MONEY_DUE, \
        TERM08_EXPENSE06_MONEY_DUE, \
        TERM08_SUM_MONEY_DUE, \
        TERM08_FLAG01, \
        TERM08_FLAG02, \
        TERM08_FLAG03, \
        TERM08_FLAG04, \
        TERM08_FLAG05, \
        TERM08_PAID_MONEY_DATE, \
        TERM08_PAID_MONEY_DATE, \
        TERM09_EXPENSE01_MONEY_DUE, \
        TERM09_EXPENSE02_MONEY_DUE, \
        TERM09_EXPENSE03_MONEY_DUE, \
        TERM09_EXPENSE04_MONEY_DUE, \
        TERM09_EXPENSE05_MONEY_DUE, \
        TERM09_EXPENSE06_MONEY_DUE, \
        TERM09_SUM_MONEY_DUE, \
        TERM09_FLAG01, \
        TERM09_FLAG02, \
        TERM09_FLAG03, \
        TERM09_FLAG04, \
        TERM09_FLAG05, \
        TERM09_PAID_MONEY_DATE, \
        TERM09_PAID_MONEY_DATE, \
        TERM10_EXPENSE01_MONEY_DUE, \
        TERM10_EXPENSE02_MONEY_DUE, \
        TERM10_EXPENSE03_MONEY_DUE, \
        TERM10_EXPENSE04_MONEY_DUE, \
        TERM10_EXPENSE05_MONEY_DUE, \
        TERM10_EXPENSE06_MONEY_DUE, \
        TERM10_SUM_MONEY_DUE, \
        TERM10_FLAG01, \
        TERM10_FLAG02, \
        TERM10_FLAG03, \
        TERM10_FLAG04, \
        TERM10_FLAG05, \
        TERM10_PAID_MONEY_DATE, \
        TERM10_PAID_MONEY_DATE, \
        TERM11_EXPENSE01_MONEY_DUE, \
        TERM11_EXPENSE02_MONEY_DUE, \
        TERM11_EXPENSE03_MONEY_DUE, \
        TERM11_EXPENSE04_MONEY_DUE, \
        TERM11_EXPENSE05_MONEY_DUE, \
        TERM11_EXPENSE06_MONEY_DUE, \
        TERM11_SUM_MONEY_DUE, \
        TERM11_FLAG01, \
        TERM11_FLAG02, \
        TERM11_FLAG03, \
        TERM11_FLAG04, \
        TERM11_FLAG05, \
        TERM11_PAID_MONEY_DATE, \
        TERM11_PAID_MONEY_DATE, \
        TERM12_EXPENSE01_MONEY_DUE, \
        TERM12_EXPENSE02_MONEY_DUE, \
        TERM12_EXPENSE03_MONEY_DUE, \
        TERM12_EXPENSE04_MONEY_DUE, \
        TERM12_EXPENSE05_MONEY_DUE, \
        TERM12_EXPENSE06_MONEY_DUE, \
        TERM12_SUM_MONEY_DUE, \
        TERM12_FLAG01, \
        TERM12_FLAG02, \
        TERM12_FLAG03, \
        TERM12_FLAG04, \
        TERM12_FLAG05, \
        TERM12_PAID_MONEY_DATE, \
        TERM12_PAID_MONEY_DATE, \
        EXTR_EXPENSE01_TRANS_SDATE, \
        EXTR_EXPENSE01_MONEY, \
        EXTR_EXPENSE01_FLAG01, \
        EXTR_EXPENSE01_FLAG02, \
        EXTR_EXPENSE01_FLAG03, \
        EXTR_EXPENSE01_FLAG04, \
        EXTR_EXPENSE01_FLAG05, \
        EXTR_EXPENSE01_PAID_MONEY_DATE, \
        EXTR_EXPENSE02_TRANS_SDATE, \
        EXTR_EXPENSE02_MONEY, \
        EXTR_EXPENSE02_FLAG01, \
        EXTR_EXPENSE02_FLAG02, \
        EXTR_EXPENSE02_FLAG03, \
        EXTR_EXPENSE02_FLAG04, \
        EXTR_EXPENSE02_FLAG05, \
        EXTR_EXPENSE02_PAID_MONEY_DATE, \
        EXTR_EXPENSE03_TRANS_SDATE, \
        EXTR_EXPENSE03_MONEY, \
        EXTR_EXPENSE03_FLAG01, \
        EXTR_EXPENSE03_FLAG02, \
        EXTR_EXPENSE03_FLAG03, \
        EXTR_EXPENSE03_FLAG04, \
        EXTR_EXPENSE03_FLAG05, \
        EXTR_EXPENSE03_PAID_MONEY_DATE, \
        EXTR_EXPENSE04_TRANS_SDATE, \
        EXTR_EXPENSE04_MONEY, \
        EXTR_EXPENSE04_FLAG01, \
        EXTR_EXPENSE04_FLAG02, \
        EXTR_EXPENSE04_FLAG03, \
        EXTR_EXPENSE04_FLAG04, \
        EXTR_EXPENSE04_FLAG05, \
        EXTR_EXPENSE04_PAID_MONEY_DATE, \
        EXTR_EXPENSE05_TRANS_SDATE, \
        EXTR_EXPENSE05_MONEY, \
        EXTR_EXPENSE05_FLAG01, \
        EXTR_EXPENSE05_FLAG02, \
        EXTR_EXPENSE05_FLAG03, \
        EXTR_EXPENSE05_FLAG04, \
        EXTR_EXPENSE05_FLAG05, \
        EXTR_EXPENSE05_PAID_MONEY_DATE, \
        EXTR_EXPENSE06_TRANS_SDATE, \
        EXTR_EXPENSE06_MONEY, \
        EXTR_EXPENSE06_FLAG01, \
        EXTR_EXPENSE06_FLAG02, \
        EXTR_EXPENSE06_FLAG03, \
        EXTR_EXPENSE06_FLAG04, \
        EXTR_EXPENSE06_FLAG05, \
        EXTR_EXPENSE06_PAID_MONEY_DATE, \
        REGISTERCD, \
        UPDATED \
    from \
        BANK_RESULT_TMP_DATA_OLD \
)
