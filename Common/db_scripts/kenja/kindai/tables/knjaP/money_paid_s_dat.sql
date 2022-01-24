-- kanji=漢字
-- $Id: money_paid_s_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金済小分類データ
DROP TABLE MONEY_PAID_S_DAT

CREATE TABLE MONEY_PAID_S_DAT \
( \
        "YEAR"              VARCHAR(4)  NOT NULL, \
        "SCHREGNO"          VARCHAR(8)  NOT NULL, \
        "EXPENSE_M_CD"      VARCHAR(2)  NOT NULL, \
        "EXPENSE_S_CD"      VARCHAR(2)  NOT NULL, \
        "PAID_INPUT_FLG"    VARCHAR(1)  NOT NULL, \
        "PAID_MONEY_DATE"   DATE, \
        "PAID_MONEY"        INTEGER, \
        "PAID_MONEY_DIV"    VARCHAR(2), \
        "REPAY_MONEY_DATE"  DATE, \
        "REPAY_MONEY"       INTEGER, \
        "REPAY_MONEY_DIV"   VARCHAR(2), \
        "REPAY_FLG"         VARCHAR(1), \
        "REMARK"            VARCHAR(30), \
        "REGISTERCD"        VARCHAR(8), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MONEY_PAID_S_DAT \
ADD CONSTRAINT PK_MONEY_PAID_S_DA \
PRIMARY KEY \
(YEAR,SCHREGNO,EXPENSE_M_CD,EXPENSE_S_CD,PAID_INPUT_FLG)
