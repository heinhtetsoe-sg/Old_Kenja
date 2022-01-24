-- kanji=漢字
-- $Id: installment_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--分納・延納データ
DROP TABLE INSTALLMENT_DAT

CREATE TABLE INSTALLMENT_DAT \
( \
        "YEAR"             VARCHAR(4) NOT NULL, \
        "SCHREGNO"         VARCHAR(8) NOT NULL, \
        "INST_CD"          VARCHAR(2) NOT NULL, \
        "INST_SEQ"         SMALLINT   NOT NULL, \
        "INST_DUE_DATE"    DATE, \
        "INST_MONEY_DUE"   INTEGER, \
        "PAID_MONEY_DATE"  DATE, \
        "PAID_MONEY"       INTEGER,  \
        "PAID_MONEY_DIV"   VARCHAR(2), \
        "REPAY_DATE"       DATE,  \
        "REPAY_MONEY"      INTEGER,  \
        "REMARK"           VARCHAR(75), \
        "REGISTERCD"       VARCHAR(8), \
        "UPDATED"          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE INSTALLMENT_DAT \
ADD CONSTRAINT PK_MONEY_PAID_DAT \
PRIMARY KEY \
(YEAR,SCHREGNO,INST_CD,INST_SEQ)
