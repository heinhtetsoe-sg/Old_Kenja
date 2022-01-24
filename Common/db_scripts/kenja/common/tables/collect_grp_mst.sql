-- kanji=漢字
-- $Id: 720a154ef73afe83a821439ef2842759803014b0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目グループマスタ
DROP TABLE COLLECT_GRP_MST

CREATE TABLE COLLECT_GRP_MST \
( \
        "SCHOOLCD"              VARCHAR(12) NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)  NOT NULL, \
        "YEAR"                  VARCHAR(4)  NOT NULL, \
        "COLLECT_GRP_CD"        VARCHAR(4)  NOT NULL, \
        "COLLECT_GRP_NAME"      VARCHAR(60), \
        "COLLECT_KOJIN_FLG"     VARCHAR(1), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_GRP_MST \
ADD CONSTRAINT PK_COLLECT_GRP_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_GRP_CD)
