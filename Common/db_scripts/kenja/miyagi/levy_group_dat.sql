-- kanji=漢字
-- $Id: levy_group_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計科目マスタ

DROP TABLE LEVY_GROUP_DAT

CREATE TABLE LEVY_GROUP_DAT ( \
        "YEAR"          VARCHAR(4) NOT NULL, \
        "LEVY_GROUP_CD" VARCHAR(4) NOT NULL, \
        "LEVY_L_CD"     VARCHAR(2) NOT NULL, \
        "REGISTERCD"    VARCHAR(10), \
        "UPDATED"      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_GROUP_DAT ADD CONSTRAINT PK_LEVY_GROUP_DAT PRIMARY KEY (YEAR, LEVY_GROUP_CD, LEVY_L_CD)
