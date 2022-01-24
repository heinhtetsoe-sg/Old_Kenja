-- kanji=漢字
-- $Id: expense_s_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目小分類マスタ
DROP TABLE EXPENSE_S_MST

CREATE TABLE EXPENSE_S_MST \
( \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "EXPENSE_S_CD"    VARCHAR(2) NOT NULL, \
        "EXPENSE_S_NAME"  VARCHAR(60), \
        "EXPENSE_S_MONEY" INTEGER, \
        "SEX"             VARCHAR(1), \
        "REGISTERCD"      VARCHAR(8), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EXPENSE_S_MST \
ADD CONSTRAINT PK_EXPENSE_S_MST \
PRIMARY KEY \
(YEAR,EXPENSE_S_CD)
