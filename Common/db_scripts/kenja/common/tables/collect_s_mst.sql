-- kanji=漢字
-- $Id: 911788f3fdbcb1fd8920b815d8ffb618e08ddfb6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目小分類マスタ
DROP TABLE COLLECT_S_MST

CREATE TABLE COLLECT_S_MST \
( \
        "SCHOOLCD"        VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"     VARCHAR(2)  NOT NULL, \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "COLLECT_L_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_M_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_S_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_S_NAME"  VARCHAR(60), \
        "COLLECT_S_MONEY" INTEGER, \
        "REGISTERCD"      VARCHAR(10), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_S_MST \
ADD CONSTRAINT PK_COLLECT_S_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD, COLLECT_S_CD)
