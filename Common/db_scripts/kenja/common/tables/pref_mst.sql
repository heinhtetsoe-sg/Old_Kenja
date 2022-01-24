-- kanji=漢字
-- $Id: 0bb7dc895b031c61e5eed41754146fc0fcfccff0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE PREF_MST

CREATE TABLE PREF_MST \
(  \
    PREF_CD           VARCHAR(2) NOT NULL, \
    PREF_NAME         VARCHAR(12), \
    REGISTERCD        VARCHAR(8), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE PREF_MST ADD CONSTRAINT PK_PREF_MST \
PRIMARY KEY (PREF_CD)
