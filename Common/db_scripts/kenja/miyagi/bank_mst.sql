-- kanji=漢字
-- $Id: bank_mst.sql 62563 2018-09-28 05:10:09Z yamashiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--銀行マスタ
DROP TABLE BANK_MST

CREATE TABLE BANK_MST ( \
    BANKCD              VARCHAR(4)      NOT NULL, \
    BRANCHCD            VARCHAR(3)      NOT NULL, \
    BANKNAME            VARCHAR(90), \
    BANKNAME_KANA       VARCHAR(90), \
    BANKNAME_ROMAJI     VARCHAR(180), \
    BRANCHNAME          VARCHAR(90), \
    BRANCHNAME_KANA     VARCHAR(90), \
    BRANCHNAME_ROMAJI   VARCHAR(180), \
    BANKZIPCD           VARCHAR(8), \
    BANKADDR1           VARCHAR(150), \
    BANKADDR2           VARCHAR(150), \
    BANKTELNO           VARCHAR(14), \
    BANKFAXNO           VARCHAR(14), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE BANK_MST \
ADD CONSTRAINT PK_BANK_MST \
PRIMARY KEY \
(BANKCD,BRANCHCD)

drop index id_bankname on BANK_MST

create index id_bankname on BANK_MST (BANKNAME)
