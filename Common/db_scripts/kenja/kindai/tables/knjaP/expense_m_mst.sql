-- kanji=漢字
-- $Id: expense_m_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目中分類マスタ
DROP TABLE EXPENSE_M_MST \

CREATE TABLE EXPENSE_M_MST \
( \
        "YEAR"                VARCHAR(4) NOT NULL, \
        "EXPENSE_M_CD"        VARCHAR(2) NOT NULL, \
        "EXPENSE_L_CD"        VARCHAR(2), \
        "EXPENSE_M_NAME"      VARCHAR(60), \
        "EXPENSE_S_EXIST_FLG" VARCHAR(1), \
        "EXPENSE_M_MONEY"     INTEGER, \
        "DUE_DATE"            DATE, \
        "BANK_TRANS_SDATE"    DATE, \
        "REGISTERCD"          VARCHAR(8), \
        "UPDATED"             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EXPENSE_M_MST \
ADD CONSTRAINT PK_EXPENSE_M_MST \
PRIMARY KEY \
(YEAR,EXPENSE_M_CD)
