-- kanji=漢字
-- $Id: collect_money_due_s_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定小分類データ
DROP TABLE COLLECT_MONEY_DUE_S_DAT

CREATE TABLE COLLECT_MONEY_DUE_S_DAT \
( \
        "YEAR"           VARCHAR(4) NOT NULL, \
        "SCHREGNO"       VARCHAR(8) NOT NULL, \
        "COLLECT_GRP_CD" VARCHAR(4) NOT NULL, \
        "COLLECT_L_CD"   VARCHAR(2) NOT NULL, \
        "COLLECT_M_CD"   VARCHAR(2) NOT NULL, \
        "COLLECT_S_CD"   VARCHAR(2) NOT NULL, \
        "MONEY_DUE"      INTEGER, \
        "COLLECT_CNT"    INTEGER, \
        "REGISTERCD"     VARCHAR(10), \
        "UPDATED"        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_MONEY_DUE_S_DAT \
ADD CONSTRAINT PK_COL_MON_DS_DAT \
PRIMARY KEY \
(YEAR, SCHREGNO, COLLECT_GRP_CD, COLLECT_L_CD, COLLECT_M_CD, COLLECT_S_CD)
