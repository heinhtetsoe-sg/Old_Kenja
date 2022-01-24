-- kanji=漢字
-- $Id: collect_grp_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目グループマスタ
DROP TABLE COLLECT_GRP_MST

CREATE TABLE COLLECT_GRP_MST \
( \
        "YEAR"               VARCHAR(4) NOT NULL, \
        "COLLECT_GRP_CD"     VARCHAR(4) NOT NULL, \
        "COLLECT_GRP_NAME"   VARCHAR(60), \
        "COLLECT_KOJIN_FLG"  VARCHAR(1), \
        "REGISTERCD"         VARCHAR(10), \
        "UPDATED"            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_GRP_MST \
ADD CONSTRAINT PK_COLLECT_GRP_MST \
PRIMARY KEY \
(YEAR, COLLECT_GRP_CD)
