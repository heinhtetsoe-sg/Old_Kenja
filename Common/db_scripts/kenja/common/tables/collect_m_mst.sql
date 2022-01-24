-- kanji=漢字
-- $Id: 7f7c85eb92d0147207c9eb9afb9aee0f2d24a95b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目中分類マスタ
DROP TABLE COLLECT_M_MST

CREATE TABLE COLLECT_M_MST \
( \
        "SCHOOLCD"            VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"         VARCHAR(2)  NOT NULL, \
        "YEAR"                VARCHAR(4)  NOT NULL, \
        "COLLECT_L_CD"        VARCHAR(2)  NOT NULL, \
        "COLLECT_M_CD"        VARCHAR(2)  NOT NULL, \
        "COLLECT_M_NAME"      VARCHAR(90), \
        "COLLECT_S_EXIST_FLG" VARCHAR(1), \
        "COLLECT_M_MONEY"     INTEGER, \
        "KOUHI_SHIHI"         VARCHAR(1), \
        "GAKUNOKIN_DIV"       VARCHAR(1), \
        "REDUCTION_DIV"       VARCHAR(1), \
        "IS_REDUCTION_SCHOOL" VARCHAR(1), \
        "IS_CREDITCNT"        VARCHAR(1), \
        "IS_REFUND"           VARCHAR(1), \
        "IS_REPAY"            VARCHAR(1), \
        "CLASSCD"             VARCHAR(2), \
        "TEXTBOOKDIV"         VARCHAR(1), \
        "SHOW_ORDER"          VARCHAR(2), \
        "LMS_GRP_CD"          VARCHAR(6), \
        "REMARK"              VARCHAR(60), \
        "DIVIDE_PROCESS"      VARCHAR(1) , \
        "ROUND_DIGIT"         VARCHAR(1) , \
        "REGISTERCD"          VARCHAR(10), \
        "UPDATED"             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_M_MST \
ADD CONSTRAINT PK_COL_M_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD)
