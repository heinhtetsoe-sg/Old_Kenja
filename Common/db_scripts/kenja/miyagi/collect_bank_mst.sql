-- kanji=漢字
-- $Id: collect_bank_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金済中分類データ
DROP TABLE COLLECT_BANK_MST

CREATE TABLE COLLECT_BANK_MST \
( \
        "COLLECT_BANK_CD"       VARCHAR(4)  NOT NULL, \
        "COLLECT_BANK_NAME"     VARCHAR(60), \
        "COLLECT_BANK_DIV"      VARCHAR(1)  NOT NULL, \
        "BANK_CD"               VARCHAR(4), \
        "BRAN_CHCD"             VARCHAR(3), \
        "BANK_DEPOSIT_ITEM"     VARCHAR(1), \
        "BANK_ACCOUNTNO"        VARCHAR(7), \
        "BANK_ACCOUNTNAME"      VARCHAR(60), \
        "YUUCYO_CD"             VARCHAR(5), \
        "YUUCYO_DEPOSIT_ITEM"   VARCHAR(1), \
        "YUUCYO_ACCOUNTNO"      VARCHAR(8), \
        "YUUCYO_ACCOUNTNAME"    VARCHAR(60), \
        "ZIPCD"                 VARCHAR(8), \
        "ADDR1"                 VARCHAR(150), \
        "ADDR2"                 VARCHAR(90), \
        "TELNO1"                VARCHAR(14), \
        "TELNO2"                VARCHAR(14), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_BANK_MST \
ADD CONSTRAINT PK_COL_BANK_MST \
PRIMARY KEY \
(COLLECT_BANK_CD, COLLECT_BANK_DIV)
