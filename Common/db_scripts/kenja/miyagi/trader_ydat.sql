-- kanji=漢字
-- $Id: trader_ydat.sql 56577 2017-10-22 11:35:50Z maeshiro $

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
