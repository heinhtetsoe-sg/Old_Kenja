-- kanji=漢字
-- $Id: collect_m_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目中分類マスタ
DROP TABLE COLLECT_M_MST \

CREATE TABLE COLLECT_M_MST \
( \
        "YEAR"                VARCHAR(4) NOT NULL, \
        "COLLECT_L_CD"        VARCHAR(2) NOT NULL, \
        "COLLECT_M_CD"        VARCHAR(2) NOT NULL, \
        "COLLECT_M_NAME"      VARCHAR(90), \
        "COLLECT_S_EXIST_FLG" VARCHAR(1), \
        "COLLECT_M_MONEY"     INTEGER, \
        "PAY_DIV"             VARCHAR(1), \
        "PAY_DATE"            DATE, \
        "IS_JUGYOURYOU"       VARCHAR(1), \
        "REMARK"              VARCHAR(60), \
        "REGISTERCD"          VARCHAR(10), \
        "UPDATED"             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_M_MST \
ADD CONSTRAINT PK_COL_M_MST \
PRIMARY KEY \
(YEAR, COLLECT_L_CD, COLLECT_M_CD)
