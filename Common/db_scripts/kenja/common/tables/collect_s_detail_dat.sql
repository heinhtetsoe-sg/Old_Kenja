-- kanji=漢字
-- $Id: 68a51dd6a9e40015c195e49821f2c48c72206d68 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目小分類マスタ
DROP TABLE COLLECT_S_DETAIL_DAT

CREATE TABLE COLLECT_S_DETAIL_DAT \
( \
        "SCHOOLCD"        VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"     VARCHAR(2)  NOT NULL, \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "COLLECT_L_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_M_CD"    VARCHAR(2) NOT NULL, \
        "COLLECT_S_CD"    VARCHAR(2) NOT NULL, \
        "TOKUSYU_CD"      VARCHAR(3) NOT NULL, \
        "TOKUSYU_VAL"     VARCHAR(1) NOT NULL, \
        "REGISTERCD"      VARCHAR(10), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_S_DETAIL_DAT \
ADD CONSTRAINT PK_COLLECT_S_D_DAT \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD, COLLECT_S_CD, TOKUSYU_CD)
