-- kanji=漢字
-- $Id: expense_grp_m_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目グループ費目中分類データ
DROP TABLE EXPENSE_GRP_M_DAT

CREATE TABLE EXPENSE_GRP_M_DAT \
( \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "EXPENSE_GRP_CD"  VARCHAR(4) NOT NULL, \
        "EXPENSE_M_CD"    VARCHAR(2) NOT NULL, \
        "REGISTERCD"      VARCHAR(8), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EXPENSE_GRP_M_DAT \
ADD CONSTRAINT PK_EXP_GRP_M_DAT \
PRIMARY KEY \
(YEAR,EXPENSE_GRP_CD,EXPENSE_M_CD)
