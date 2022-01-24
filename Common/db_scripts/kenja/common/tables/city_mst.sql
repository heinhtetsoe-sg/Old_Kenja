-- kanji=漢字
-- $Id: 62eb2f9e225c74a3045262abc43fce4875d3d8ea $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE CITY_MST

CREATE TABLE CITY_MST \
(  \
    PREF_CD           VARCHAR(2) NOT NULL, \
    CITY_CD           VARCHAR(3)  NOT NULL, \
    CITY_NAME         VARCHAR(120), \
    CITY_KANA         VARCHAR(120), \
    CITY_FLG1         VARCHAR(1), \
    CITY_FLG2         VARCHAR(1), \
    CITY_FLG3         VARCHAR(1), \
    CITY_FLG4         VARCHAR(1), \
    CITY_FLG5         VARCHAR(1), \
    REGISTERCD        VARCHAR(8), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CITY_MST ADD CONSTRAINT PK_CITY_MST \
PRIMARY KEY (PREF_CD,CITY_CD)
