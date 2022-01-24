-- kanji=漢字
-- $Id: collect_money_repay_m_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金済中分類データ
DROP TABLE COLLECT_MONEY_REPAY_M_DAT

CREATE TABLE COLLECT_MONEY_REPAY_M_DAT \
( \
        "YEAR"              VARCHAR(4)  NOT NULL, \
        "SCHREGNO"          VARCHAR(8)  NOT NULL, \
        "REPAY_SEQ"         SMALLINT    NOT NULL, \
        "COLLECT_GRP_CD"    VARCHAR(4)  NOT NULL, \
        "COLLECT_L_CD"      VARCHAR(2)  NOT NULL, \
        "COLLECT_M_CD"      VARCHAR(2)  NOT NULL, \
        "REPAY_INPUT_FLG"   VARCHAR(1)  NOT NULL, \
        "REPAY_MONEY_DATE"  DATE, \
        "REPAY_MONEY"       INTEGER, \
        "REPAY_MONEY_DIV"   VARCHAR(2), \
        "REGISTERCD"        VARCHAR(10), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_MONEY_REPAY_M_DAT \
ADD CONSTRAINT PK_MONEY_REPAY_M_D \
PRIMARY KEY \
(YEAR, SCHREGNO, REPAY_SEQ)
