-- kanji=漢字
-- $Id: money_due_m_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
DROP TABLE MONEY_DUE_M_DAT

CREATE TABLE MONEY_DUE_M_DAT \
( \
        "YEAR"                  VARCHAR(4) NOT NULL, \
        "SCHREGNO"              VARCHAR(8) NOT NULL, \
        "EXPENSE_M_CD"          VARCHAR(2) NOT NULL, \
        "MONEY_DUE"             INTEGER, \
        "REDUCTION_REASON"      VARCHAR(2), \
        "BANK_TRANS_RESULTCD"   VARCHAR(1), \
        "INST_CD"               VARCHAR(2), \
        "BANK_TRANS_STOP_RESON" VARCHAR(2), \
        "UN_AUTO_PAYFLG"        VARCHAR(1), \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MONEY_DUE_M_DAT \
ADD CONSTRAINT PK_MONEY_DUE_M_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,EXPENSE_M_CD)
