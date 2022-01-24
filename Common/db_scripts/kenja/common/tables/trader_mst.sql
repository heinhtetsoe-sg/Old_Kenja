-- kanji=漢字
-- $Id: d813ea6e3b265be068e3be6c673ea562abf9f7c1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--業者マスタ
DROP TABLE TRADER_MST \

CREATE TABLE TRADER_MST \
( \
        "TRADER_CD"         varchar(8) not null, \
        "TRADER_NAME"       varchar(120), \
        "ZIPCD"             varchar(8), \
        "ADDR1"             varchar(150), \
        "ADDR2"             varchar(150), \
        "BANKCD"            varchar(4), \
        "BRANCHCD"          varchar(3), \
        "BANK_DEPOSIT_ITEM" varchar(1), \
        "BANK_ACCOUNTNO"    varchar(7), \
        "ACCOUNTNAME"       varchar(120), \
        "ACCOUNTNAME_KANA"  varchar(120), \
        "PAY_DIV"           varchar(2), \
        "REGISTERCD"        varchar(10), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE TRADER_MST \
ADD CONSTRAINT PK_TRADER_MST \
PRIMARY KEY \
(TRADER_CD)
