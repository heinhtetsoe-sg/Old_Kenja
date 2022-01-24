-- kanji=漢字
-- $Id: bank_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--銀行マスタ
DROP TABLE BANK_MST

CREATE TABLE BANK_MST \
( \
        "BANKCD"          VARCHAR(4)      NOT NULL, \
        "BRANCHCD"        VARCHAR(3)      NOT NULL, \
        "BANKNAME"        VARCHAR(45), \
        "BANKNAME_KANA"   VARCHAR(45), \
        "BRANCHNAME"      VARCHAR(45), \
        "BRANCHNAME_KANA" VARCHAR(45), \
        "BANKZIPCD"       VARCHAR(8), \
        "BANKADDR1"       VARCHAR(75), \
        "BANKADDR2"       VARCHAR(75), \
        "BANKTELNO"       VARCHAR(14), \
        "BANKFAXNO"       VARCHAR(14), \
        "REGISTERCD"      VARCHAR(8), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE BANK_MST \
ADD CONSTRAINT PK_BANK_MST \
PRIMARY KEY \
(BANKCD,BRANCHCD)
