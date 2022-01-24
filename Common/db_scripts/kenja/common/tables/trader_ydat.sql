-- kanji=漢字
-- $Id: 7a676b9715fe01013c7b5c1262f56f97adb2c590 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--業者マスタ
DROP TABLE TRADER_YDAT \

CREATE TABLE TRADER_YDAT \
( \
        "YEAR"          VARCHAR(4) NOT NULL, \
        "TRADER_CD"     VARCHAR(8) NOT NULL, \
        "REGISTERCD"    VARCHAR(10), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE TRADER_YDAT \
ADD CONSTRAINT PK_TRADER_YDAT \
PRIMARY KEY \
(YEAR, TRADER_CD)
