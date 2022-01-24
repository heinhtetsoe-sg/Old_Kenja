-- kanji=漢字
-- $Id: 2ab8d200afc1e92e4b9293284b6e46481b73c81b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table APPOINTED_DAY_MST

CREATE TABLE APPOINTED_DAY_MST( \
        YEAR           VARCHAR(4) NOT NULL, \
        SCHOOL_KIND    VARCHAR(2) NOT NULL, \
        MONTH          VARCHAR(2) NOT NULL, \
        SEMESTER       VARCHAR(1) NOT NULL, \
        APPOINTED_DAY  VARCHAR(2) NOT NULL, \
        REGISTERCD     VARCHAR(10), \
        UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE APPOINTED_DAY_MST ADD CONSTRAINT PK_APPOINTED_DAY_M PRIMARY KEY (YEAR, SCHOOL_KIND, MONTH, SEMESTER)
